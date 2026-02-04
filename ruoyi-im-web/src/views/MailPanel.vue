<template>
  <div class="mail-panel">
    <div class="panel-header">
      <h2 class="panel-title">邮箱</h2>
      <div class="header-actions">
        <!-- 视图切换按钮 -->
        <div class="view-toggle">
          <button
            class="view-btn"
            :class="{ active: viewMode === 'list' }"
            @click="setViewMode('list')"
            title="列表模式"
          >
            <span class="material-icons-outlined">view_list</span>
          </button>
          <button
            class="view-btn"
            :class="{ active: viewMode === 'preview' }"
            @click="setViewMode('preview')"
            title="预览模式"
          >
            <span class="material-icons-outlined">view_column</span>
          </button>
        </div>
        <button class="compose-btn" @click="openComposeDialog">
          <span class="material-icons-outlined">edit</span>
          写邮件
        </button>
      </div>
    </div>

    <div class="panel-content">
      <div class="mail-layout" :class="`layout-${viewMode}`">
        <!-- 邮箱文件夹列表 -->
        <div class="mail-folders">
          <div
            v-for="folder in folders"
            :key="folder.key"
            class="folder-item"
            :class="{ active: activeFolder === folder.key }"
            @click="switchFolder(folder.key)"
          >
            <span class="material-icons-outlined folder-icon">{{ folder.icon }}</span>
            <span class="folder-label">{{ folder.label }}</span>
            <span v-if="folder.unreadCount > 0" class="folder-count">{{ folder.unreadCount }}</span>
          </div>
        </div>

        <!-- 邮件列表区域 -->
        <div class="mail-list-area">
          <!-- 搜索栏 -->
          <div class="search-bar">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索邮件..."
              :prefix-icon="Search"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch"
              class="search-input"
            >
              <template #append>
                <el-button :icon="Search" @click="handleSearch">搜索</el-button>
              </template>
            </el-input>
          </div>

          <!-- 批量操作工具栏 -->
          <div v-if="selectedEmails.length > 0" class="batch-toolbar">
            <div class="selection-info">
              已选择 {{ selectedEmails.length }} 封邮件
            </div>
            <div class="batch-actions">
              <el-button size="small" @click="batchMarkRead">标为已读</el-button>
              <el-button size="small" @click="batchMarkUnread">标为未读</el-button>
              <el-dropdown @command="handleBatchMove" trigger="click">
                <el-button size="small">
                  移动到 <span class="material-icons-outlined">expand_more</span>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="INBOX">收件箱</el-dropdown-item>
                    <el-dropdown-item command="STARRED">星标邮件</el-dropdown-item>
                    <el-dropdown-item command="TRASH">已删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-button size="small" type="danger" @click="batchDelete">删除</el-button>
              <el-button size="small" link @click="clearSelection">取消选择</el-button>
            </div>
          </div>

          <!-- 邮件列表 -->
          <div class="mail-list">
            <!-- 列表头部 (全选) -->
            <div v-if="emails.length > 0" class="email-list-header">
              <el-checkbox
                v-model="selectAll"
                :indeterminate="isIndeterminate"
                @change="handleSelectAll"
              >
                全选
              </el-checkbox>
              <span class="list-count">{{ emails.length }} 封邮件</span>
            </div>

            <div v-if="loading" class="loading-state">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>加载中...</span>
            </div>

            <div v-else-if="emails.length === 0" class="empty-state">
              <span class="material-icons-outlined empty-icon">{{ searchKeyword ? 'search_off' : 'email' }}</span>
              <p class="empty-text">{{ searchKeyword ? '未找到相关邮件' : '暂无邮件' }}</p>
            </div>

            <div v-else class="email-list">
              <div
                v-for="email in emails"
                :key="email.id"
                class="email-item"
                :class="{
                  unread: !email.read,
                  selected: selectedEmails.includes(email.id),
                  starred: email.starred,
                  previewing: previewEmail?.id === email.id
                }"
                @click="handleViewEmail(email)"
              >
                <el-checkbox
                  class="email-checkbox"
                  :model-value="selectedEmails.includes(email.id)"
                  @update:model-value="(checked) => toggleSelection(email.id, checked)"
                  @click.stop
                />
                <div class="email-avatar" :style="{ background: email.avatarColor }">
                  {{ email.sender?.charAt(0) || '?' }}
                </div>
                <div class="email-content">
                  <div class="email-subject">
                    <span v-if="email.starred" class="material-icons-outlined star-icon">star</span>
                    {{ email.subject }}
                  </div>
                  <div class="email-preview">{{ email.preview }}</div>
                  <div class="email-meta">
                    <span class="email-sender">{{ email.sender }}</span>
                    <span class="email-time">{{ email.time }}</span>
                  </div>
                </div>
                <div v-if="!email.read" class="unread-dot"></div>
              </div>
            </div>

            <!-- 分页 -->
            <div v-if="totalCount > pageSize" class="pagination-wrapper">
              <el-pagination
                v-model:current-page="currentPage"
                :page-size="pageSize"
                :total="totalCount"
                layout="prev, pager, next, jumper"
                small
                @current-change="handlePageChange"
              />
            </div>
          </div>
        </div>

        <!-- 邮件预览面板 -->
        <div v-if="viewMode === 'preview'" class="mail-preview-panel" :class="{ 'mobile-show': showMobilePreview }">
          <!-- 移动端返回按钮 -->
          <div class="mobile-back-header">
            <button class="back-btn" @click="closeMobilePreview">
              <span class="material-icons-outlined">arrow_back</span>
              <span>返回列表</span>
            </button>
            <button class="close-btn" @click="closeMobilePreview">
              <span class="material-icons-outlined">close</span>
            </button>
          </div>

          <div v-if="previewEmail" class="preview-content">
            <!-- 预览头部 -->
            <div class="preview-header">
              <div class="preview-title-row">
                <h3 class="preview-subject">{{ previewEmail.subject }}</h3>
                <div class="preview-actions">
                  <button
                    class="icon-btn"
                    :class="{ active: previewEmail.starred }"
                    @click="handlePreviewStar"
                    title="星标"
                  >
                    <span class="material-icons-outlined">{{ previewEmail.starred ? 'star' : 'star_border' }}</span>
                  </button>
                  <button class="icon-btn" @click="openDetailDialog" title="查看详情">
                    <span class="material-icons-outlined">open_in_full</span>
                  </button>
                </div>
              </div>
              <div class="preview-meta">
                <span class="preview-time">{{ previewEmail.time }}</span>
                <span v-if="previewEmail.cc?.length" class="preview-cc">
                  抄送: {{ previewEmail.cc.join(', ') }}
                </span>
              </div>
            </div>

            <!-- 发件人信息 -->
            <div class="preview-sender">
              <div class="sender-avatar" :style="{ background: previewEmail.avatarColor }">
                {{ previewEmail.sender?.charAt(0) || '?' }}
              </div>
              <div class="sender-info">
                <div class="sender-name">{{ previewEmail.sender }}</div>
                <div class="sender-email">{{ previewEmail.senderEmail || previewEmail.sender }}</div>
              </div>
            </div>

            <!-- 邮件正文 -->
            <div class="preview-body">
              <div class="body-content">
                {{ previewEmail.fullContent || previewEmail.preview || '暂无内容' }}
              </div>
            </div>

            <!-- 附件 -->
            <div v-if="previewEmail.attachments?.length" class="preview-attachments">
              <div class="attachments-header">
                <span class="material-icons-outlined">attach_file</span>
                <span>附件 ({{ previewEmail.attachments.length }})</span>
              </div>
              <div class="attachments-list">
                <div
                  v-for="(file, index) in previewEmail.attachments"
                  :key="index"
                  class="attachment-item"
                >
                  <span class="material-icons-outlined file-icon" :style="{ color: getFileIconColor(file.name) }">
                    {{ getFileIcon(file.name) }}
                  </span>
                  <span class="file-name">{{ file.name }}</span>
                  <span class="file-size">{{ formatFileSize(file.size) }}</span>
                  <button
                    class="download-btn"
                    @click="downloadAttachment(file)"
                    title="下载"
                  >
                    <span class="material-icons-outlined">download</span>
                  </button>
                </div>
              </div>
            </div>

            <!-- 预览操作栏 -->
            <div class="preview-toolbar">
              <button class="toolbar-btn primary" @click="handlePreviewReply">
                <span class="material-icons-outlined">reply</span>
                <span>回复</span>
              </button>
              <button class="toolbar-btn" @click="handlePreviewForward">
                <span class="material-icons-outlined">forward</span>
                <span>转发</span>
              </button>
              <button class="toolbar-btn danger" @click="handlePreviewDelete">
                <span class="material-icons-outlined">delete</span>
                <span>删除</span>
              </button>
              <div class="toolbar-spacer"></div>
              <button
                v-if="hasPrevPreview"
                class="toolbar-btn icon-only"
                @click="showPrevPreview"
                title="上一封"
              >
                <span class="material-icons-outlined">chevron_left</span>
              </button>
              <button
                v-if="hasNextPreview"
                class="toolbar-btn icon-only"
                @click="showNextPreview"
                title="下一封"
              >
                <span class="material-icons-outlined">chevron_right</span>
              </button>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-else class="preview-empty">
            <span class="material-icons-outlined empty-icon">email</span>
            <p class="empty-text">选择一封邮件预览</p>
            <p class="empty-hint">点击邮件列表中的邮件查看详情</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 写邮件对话框 -->
    <ComposeMailDialog
      v-model="showComposeDialog"
      :reply-to="replyToEmail"
      @success="handleMailSent"
    />

    <!-- 邮件详情对话框 -->
    <MailDetailDialog
      v-model="showDetailDialog"
      :email="selectedEmail"
      :has-prev="hasPrevEmail"
      :has-next="hasNextEmail"
      @reply="handleReply"
      @forward="handleForward"
      @delete="handleDeleteEmail"
      @star="handleStarChange"
      @prev="showPrevEmail"
      @next="showNextEmail"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { Loading, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ComposeMailDialog from '@/components/ComposeMailDialog/index.vue'
import MailDetailDialog from '@/components/MailDetailDialog/index.vue'
import {
  getMailList,
  markAsRead,
  markAsUnread,
  deleteMail,
  moveToFolder,
  starMail,
  getFolderStats
} from '@/api/im/mail'

// 状态管理
const loading = ref(false)
const showComposeDialog = ref(false)
const showDetailDialog = ref(false)
const selectedEmail = ref(null)
const replyToEmail = ref(null)
const activeFolder = ref('inbox')
const emails = ref([])
const searchKeyword = ref('')
const selectedEmails = ref([])
const selectAll = ref(false)

// 视图模式：list（列表模式）、preview（预览模式）
const viewMode = ref('preview')
// 预览邮件
const previewEmail = ref(null)
// 移动端预览面板显示状态
const showMobilePreview = ref(false)
// 预览面板中当前邮件索引
const previewIndex = computed(() => {
  return emails.value.findIndex(e => e.id === previewEmail.value?.id)
})
// 是否有上一封/下一封预览
const hasPrevPreview = computed(() => previewIndex.value > 0)
const hasNextPreview = computed(() => previewIndex.value >= 0 && previewIndex.value < emails.value.length - 1)

// 分页
const currentPage = ref(1)
const pageSize = ref(20)
const totalCount = ref(0)

// 文件夹数据
const folders = ref([
  { key: 'inbox', label: '收件箱', icon: 'inbox', unreadCount: 0 },
  { key: 'sent', label: '已发送', icon: 'send', unreadCount: 0 },
  { key: 'draft', label: '草稿箱', icon: 'draft', unreadCount: 0 },
  { key: 'starred', label: '星标邮件', icon: 'star', unreadCount: 0 },
  { key: 'trash', label: '已删除', icon: 'delete', unreadCount: 0 }
])

// 当前邮件在列表中的索引
const currentIndex = computed(() => {
  return emails.value.findIndex(e => e.id === selectedEmail.value?.id)
})

// 是否有上一封
const hasPrevEmail = computed(() => currentIndex.value > 0)

// 是否有下一封
const hasNextEmail = computed(() => currentIndex.value >= 0 && currentIndex.value < emails.value.length - 1)

// 全选状态是否为半选
const isIndeterminate = computed(() => {
  return selectedEmails.value.length > 0 && selectedEmails.value.length < emails.value.length
})

// 切换文件夹
const switchFolder = (folderKey) => {
  if (activeFolder.value !== folderKey) {
    activeFolder.value = folderKey
    currentPage.value = 1
    selectedEmails.value = []
    selectAll.value = false
    searchKeyword.value = ''
    loadMails()
  }
}

// 加载邮件列表
const loadMails = async () => {
  loading.value = true
  try {
    const res = await getMailList(activeFolder.value, {
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value || undefined
    })
    if (res && res.code === 200) {
      emails.value = (res.data?.list || res.data || []).map(email => ({
        ...email,
        avatarColor: getRandomColor()
      }))
      totalCount.value = res.data?.total || res.data?.length || 0
      await updateFolderCounts()
    } else {
      emails.value = []
      totalCount.value = 0
    }
  } catch (error) {
    console.warn('加载邮件列表失败:', error.message)
    emails.value = []
    totalCount.value = 0
  } finally {
    loading.value = false
  }
}

// 更新文件夹计数
const updateFolderCounts = async () => {
  try {
    const res = await getFolderStats()
    if (res.code === 200) {
      const stats = res.data || {}
      folders.value.forEach(folder => {
        const folderKey = folder.key.toUpperCase()
        folder.unreadCount = stats[folderKey]?.unread || 0
      })
    }
  } catch (error) {
    console.error('获取文件夹统计失败', error)
  }
}

// 随机颜色
const getRandomColor = () => {
  const colors = ['#0089FF', '#52c41a', '#fa8c16', '#722ed1', '#eb2f96']
  return colors[Math.floor(Math.random() * colors.length)]
}

// 搜索邮件
const handleSearch = () => {
  currentPage.value = 1
  selectedEmails.value = []
  loadMails()
}

// 打开写邮件对话框
const openComposeDialog = () => {
  replyToEmail.value = null
  showComposeDialog.value = true
}

// 查看邮件
const handleViewEmail = async (email) => {
  // 标记为已读
  if (!email.read) {
    try {
      const res = await markAsRead(email.id)
      if (res.code === 200) {
        email.read = true
        // 重新获取未读数以保持数据一致性
        await updateFolderCounts()
      }
    } catch (error) {
      console.error('标记已读失败', error)
    }
  }

  // 根据视图模式处理
  if (viewMode.value === 'preview') {
    // 预览模式：更新预览面板
    selectEmailForPreview(email)
    // 移动端：显示全屏预览
    if (window.innerWidth < 768) {
      showMobilePreview.value = true
    }
  } else {
    // 列表模式：打开详情对话框
    selectedEmail.value = email
    showDetailDialog.value = true
  }
}

// 关闭移动端预览
const closeMobilePreview = () => {
  showMobilePreview.value = false
  // 可选：短暂延迟后清空预览
  setTimeout(() => {
    if (!showMobilePreview.value) {
      previewEmail.value = null
    }
  }, 300)
}

// 切换选择状态
const toggleSelection = (emailId, checked) => {
  if (checked) {
    if (!selectedEmails.value.includes(emailId)) {
      selectedEmails.value.push(emailId)
    }
  } else {
    selectedEmails.value = selectedEmails.value.filter(id => id !== emailId)
  }
  // 更新全选状态
  selectAll.value = selectedEmails.value.length === emails.value.length
}

// 全选/取消全选
const handleSelectAll = (checked) => {
  if (checked) {
    selectedEmails.value = emails.value.map(e => e.id)
  } else {
    selectedEmails.value = []
  }
}

// 清除选择
const clearSelection = () => {
  selectedEmails.value = []
  selectAll.value = false
}

// 批量标记已读
const batchMarkRead = async () => {
  if (selectedEmails.value.length === 0) return
  try {
    const res = await markAsRead(selectedEmails.value)
    if (res.code === 200) {
      emails.value.forEach(email => {
        if (selectedEmails.value.includes(email.id)) {
          email.read = true
        }
      })
      ElMessage.success('已标记为已读')
      clearSelection()
      await updateFolderCounts()
    }
  } catch (error) {
    console.error('批量标记已读失败', error)
    ElMessage.error('操作失败')
  }
}

// 批量标记未读
const batchMarkUnread = async () => {
  if (selectedEmails.value.length === 0) return
  try {
    const res = await markAsUnread(selectedEmails.value)
    if (res.code === 200) {
      emails.value.forEach(email => {
        if (selectedEmails.value.includes(email.id)) {
          email.read = false
        }
      })
      ElMessage.success('已标记为未读')
      clearSelection()
      await updateFolderCounts()
    }
  } catch (error) {
    console.error('批量标记未读失败', error)
    ElMessage.error('操作失败')
  }
}

// 批量移动
const handleBatchMove = async (targetFolder) => {
  if (selectedEmails.value.length === 0) return
  try {
    const res = await moveToFolder(selectedEmails.value, targetFolder)
    if (res.code === 200) {
      emails.value = emails.value.filter(e => !selectedEmails.value.includes(e.id))
      ElMessage.success('移动成功')
      clearSelection()
      await updateFolderCounts()
    } else {
      ElMessage.error(res.msg || '移动失败')
    }
  } catch (error) {
    console.error('批量移动失败', error)
    ElMessage.error('操作失败')
  }
}

// 批量删除
const batchDelete = async () => {
  if (selectedEmails.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedEmails.value.length} 封邮件吗？`, '确认删除', {
      type: 'warning'
    })
    const res = await deleteMail(selectedEmails.value)
    if (res.code === 200) {
      emails.value = emails.value.filter(e => !selectedEmails.value.includes(e.id))
      totalCount.value -= selectedEmails.value.length
      ElMessage.success('删除成功')
      clearSelection()
      await updateFolderCounts()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch {
    // 用户取消
  }
}

// 回复邮件
const handleReply = (email) => {
  replyToEmail.value = {
    id: email.id,
    senderId: email.senderId,
    toIds: [email.senderId],
    subject: `Re: ${email.subject}`,
    isReply: true
  }
  showDetailDialog.value = false
  showComposeDialog.value = true
}

// 转发邮件
const handleForward = (email) => {
  replyToEmail.value = {
    id: email.id,
    subject: `Fwd: ${email.subject}`,
    content: `\n\n---------- 转发的邮件 ----------\n发件人: ${email.sender}\n时间: ${email.time}\n主题: ${email.subject}\n\n${email.fullContent || email.preview || ''}`,
    isForward: true,
    attachments: email.attachments || []
  }
  showDetailDialog.value = false
  showComposeDialog.value = true
}

// 删除邮件
const handleDeleteEmail = async (email) => {
  try {
    await ElMessageBox.confirm('确定要删除这封邮件吗？', '确认删除', {
      type: 'warning'
    })
    const res = await deleteMail(email.id)
    if (res.code === 200) {
      emails.value = emails.value.filter(e => e.id !== email.id)
      totalCount.value--
      ElMessage.success('删除成功')
      showDetailDialog.value = false
      selectedEmail.value = null
      await updateFolderCounts()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch {
    // 用户取消
  }
}

// 星标状态变更
const handleStarChange = ({ id, starred }) => {
  const email = emails.value.find(e => e.id === id)
  if (email) {
    email.starred = starred
  }
}

// 上一封邮件
const showPrevEmail = () => {
  if (hasPrevEmail.value) {
    selectedEmail.value = emails.value[currentIndex.value - 1]
  }
}

// 下一封邮件
const showNextEmail = () => {
  if (hasNextEmail.value) {
    selectedEmail.value = emails.value[currentIndex.value + 1]
  }
}

// 页码变更
const handlePageChange = (page) => {
  currentPage.value = page
  selectedEmails.value = []
  loadMails()
}

// 发送邮件成功回调
const handleMailSent = () => {
  replyToEmail.value = null
  showComposeDialog.value = false
  // 如果在已发送文件夹，刷新列表
  if (activeFolder.value === 'sent') {
    loadMails()
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadMails()
})

// ==================== 预览面板相关方法 ====================

// 设置视图模式
const setViewMode = (mode) => {
  viewMode.value = mode
  if (mode === 'list') {
    previewEmail.value = null
  }
}

// 获取文件图标
const getFileIcon = (fileName) => {
  const ext = fileName?.split('.').pop()?.toLowerCase() || ''
  const iconMap = {
    jpg: 'image', jpeg: 'image', png: 'image', gif: 'image', bmp: 'image', svg: 'image', webp: 'image',
    doc: 'description', docx: 'description',
    xls: 'table_chart', xlsx: 'table_chart',
    ppt: 'slideshow', pptx: 'slideshow',
    pdf: 'picture_as_pdf',
    txt: 'text_snippet',
    zip: 'folder_zip', rar: 'folder_zip', '7z': 'folder_zip',
    mp3: 'music_note', wav: 'music_note',
    mp4: 'video_file', avi: 'video_file', mkv: 'video_file', mov: 'video_file'
  }
  return iconMap[ext] || 'insert_drive_file'
}

// 获取文件图标颜色
const getFileIconColor = (fileName) => {
  const ext = fileName?.split('.').pop()?.toLowerCase() || ''
  const colorMap = {
    jpg: '#722ed1', jpeg: '#722ed1', png: '#722ed1', gif: '#722ed1', bmp: '#722ed1',
    doc: '#1890ff', docx: '#1890ff',
    xls: '#52c41a', xlsx: '#52c41a',
    ppt: '#faad14', pptx: '#faad14',
    pdf: '#f5222d',
    txt: '#8c8c8c',
    zip: '#fa8c16', rar: '#fa8c16',
    mp3: '#f5222d', mp4: '#f5222d'
  }
  return colorMap[ext] || '#8c8c8c'
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

// 下载附件
const downloadAttachment = async (file) => {
  try {
    const { downloadAttachment: downloadApi } = await import('@/api/im/mail')
    const blob = await downloadApi(file.id)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = file.name
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (error) {
    console.error('下载附件失败', error)
    ElMessage.error('下载失败')
  }
}

// 预览面板星标操作
const handlePreviewStar = async () => {
  if (!previewEmail.value) return
  try {
    const newStarred = !previewEmail.value.starred
    const res = await starMail(previewEmail.value.id, newStarred)
    if (res.code === 200) {
      previewEmail.value.starred = newStarred
      // 同时更新列表中的状态
      const email = emails.value.find(e => e.id === previewEmail.value.id)
      if (email) email.starred = newStarred
      ElMessage.success(newStarred ? '已标星' : '已取消标星')
    }
  } catch (error) {
    console.error('星标操作失败', error)
    ElMessage.error('操作失败')
  }
}

// 打开详情对话框
const openDetailDialog = () => {
  selectedEmail.value = previewEmail.value
  showDetailDialog.value = true
}

// 预览面板回复
const handlePreviewReply = () => {
  if (!previewEmail.value) return
  replyToEmail.value = {
    id: previewEmail.value.id,
    senderId: previewEmail.value.senderId,
    toIds: [previewEmail.value.senderId],
    subject: `Re: ${previewEmail.value.subject}`,
    isReply: true
  }
  showComposeDialog.value = true
}

// 预览面板转发
const handlePreviewForward = () => {
  if (!previewEmail.value) return
  replyToEmail.value = {
    id: previewEmail.value.id,
    subject: `Fwd: ${previewEmail.value.subject}`,
    content: `\n\n---------- 转发的邮件 ----------\n发件人: ${previewEmail.value.sender}\n时间: ${previewEmail.value.time}\n主题: ${previewEmail.value.subject}\n\n${previewEmail.value.fullContent || previewEmail.value.preview || ''}`,
    isForward: true,
    attachments: previewEmail.value.attachments || []
  }
  showComposeDialog.value = true
}

// 预览面板删除
const handlePreviewDelete = async () => {
  if (!previewEmail.value) return
  try {
    await ElMessageBox.confirm('确定要删除这封邮件吗？', '确认删除', {
      type: 'warning'
    })
    const res = await deleteMail(previewEmail.value.id)
    if (res.code === 200) {
      // 从列表中移除
      const idx = emails.value.findIndex(e => e.id === previewEmail.value.id)
      emails.value = emails.value.filter(e => e.id !== previewEmail.value.id)
      totalCount.value--
      ElMessage.success('删除成功')
      // 显示下一封或清空预览
      if (hasNextPreview.value) {
        showNextPreview()
      } else if (emails.value.length > 0) {
        previewEmail.value = emails.value[Math.max(0, idx - 1)]
      } else {
        previewEmail.value = null
      }
      await updateFolderCounts()
    }
  } catch {
    // 用户取消
  }
}

// 上一封预览
const showPrevPreview = () => {
  if (hasPrevPreview.value) {
    const prevEmail = emails.value[previewIndex.value - 1]
    selectEmailForPreview(prevEmail)
  }
}

// 下一封预览
const showNextPreview = () => {
  if (hasNextPreview.value) {
    const nextEmail = emails.value[previewIndex.value + 1]
    selectEmailForPreview(nextEmail)
  }
}

// 选择邮件进行预览
const selectEmailForPreview = async (email) => {
  previewEmail.value = email
  // 标记为已读
  if (!email.read) {
    try {
      const res = await markAsRead(email.id)
      if (res.code === 200) {
        email.read = true
        await updateFolderCounts()
      }
    } catch (error) {
      console.error('标记已读失败', error)
    }
  }
}
</script>

<style scoped lang="scss">
.mail-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  flex: 1;
  min-width: 0;
  background: var(--dt-bg-body);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-color);
  flex-shrink: 0;
}

.panel-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
}

.compose-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: var(--dt-brand-color);
  color: #fff;
  border: none;
  border-radius: var(--dt-radius-lg);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--dt-transition-fast);
}

.compose-btn:hover {
  background: var(--dt-brand-hover);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 137, 255, 0.25);
}

.compose-btn:active {
  transform: translateY(0);
}

.panel-content {
  flex: 1;
  padding: 16px 24px;
  overflow: hidden;
}

.mail-layout {
  display: flex;
  height: 100%;
  gap: 16px;
}

.mail-folders {
  width: 180px;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-xl);
  padding: 8px;
  flex-shrink: 0;
  border: 1px solid var(--dt-border-light);
}

.folder-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all var(--dt-transition-fast);
}

.folder-item:hover {
  background: var(--dt-bg-hover);
}

.folder-item.active {
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);
  font-weight: 500;
}

.folder-icon {
  font-size: 20px;
  color: var(--dt-text-tertiary);
}

.folder-label {
  flex: 1;
  font-size: 14px;
}

.folder-count {
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.folder-item.active .folder-count {
  color: var(--dt-brand-color);
}

// 邮件列表区域
.mail-list-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

// 搜索栏
.search-bar {
  flex-shrink: 0;

  .search-input {
    width: 100%;
  }
}

// 批量操作工具栏
.batch-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  background: var(--dt-brand-bg);
  border-radius: var(--dt-radius-lg);
  flex-shrink: 0;
}

.selection-info {
  font-size: 14px;
  color: var(--dt-brand-color);
  font-weight: 500;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 8px;

  .material-icons-outlined {
    font-size: 18px;
  }
}

// 邮件列表
.mail-list {
  flex: 1;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-xl);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--dt-border-light);
}

.email-list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  border-bottom: 1px solid var(--dt-border-light);
  flex-shrink: 0;
}

.list-count {
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: var(--dt-text-tertiary);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  color: var(--dt-border-color);
}

.empty-text {
  font-size: 14px;
  margin: 0;
  color: var(--dt-text-secondary);
}

.email-list {
  flex: 1;
  overflow-y: auto;
}

.email-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  cursor: pointer;
  position: relative;
  border-bottom: 1px solid var(--dt-border-light);
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-hover);
  }

  &.unread {
    background: var(--dt-brand-bg);

    &:hover {
      filter: brightness(0.98);
    }
  }

  &.selected {
    background: var(--dt-brand-bg);
  }

  &.starred .email-subject {
    padding-left: 0;
  }
}

.email-checkbox {
  margin-right: 12px;
  flex-shrink: 0;
}

.email-avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--dt-radius-lg);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.email-content {
  flex: 1;
  min-width: 0;
}

.email-subject {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.star-icon {
  font-size: 16px;
  color: #faad14;
}

.email-item.unread .email-subject {
  font-weight: 600;
  color: var(--dt-brand-color);
}

.email-preview {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.email-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.unread-dot {
  width: 8px;
  height: 8px;
  background: var(--dt-brand-color);
  border-radius: var(--dt-radius-full);
  position: absolute;
  right: 16px;
  box-shadow: 0 0 0 3px var(--dt-brand-bg);
}

.pagination-wrapper {
  padding: 12px 16px;
  border-top: 1px solid var(--dt-border-light);
  display: flex;
  justify-content: center;
  flex-shrink: 0;
}

// ==================== 预览面板样式 ====================
.mail-preview-panel {
  width: 450px;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-xl);
  border: 1px solid var(--dt-border-light);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  flex-shrink: 0;
}

.preview-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.preview-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--dt-border-light);
  flex-shrink: 0;
}

.preview-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.preview-subject {
  font-size: 16px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
  flex: 1;
  line-height: 1.4;
}

.preview-actions {
  display: flex;
  gap: 4px;
}

.preview-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.preview-cc {
  display: flex;
  align-items: center;
  gap: 4px;
}

.preview-sender {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  background: var(--dt-bg-hover);
  margin: 0 20px;
  flex-shrink: 0;
}

.sender-avatar {
  width: 44px;
  height: 44px;
  border-radius: var(--dt-radius-full);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  flex-shrink: 0;
}

.sender-info {
  flex: 1;
  min-width: 0;
}

.sender-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin-bottom: 2px;
}

.sender-email {
  font-size: 12px;
  color: var(--dt-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-body {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.body-content {
  font-size: 14px;
  line-height: 1.8;
  color: var(--dt-text-primary);
  white-space: pre-wrap;
  word-break: break-word;
}

.preview-attachments {
  padding: 16px 20px;
  border-top: 1px solid var(--dt-border-light);
  flex-shrink: 0;
}

.attachments-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
  margin-bottom: 12px;
}

.attachments-header .material-icons-outlined {
  font-size: 18px;
  color: var(--dt-text-tertiary);
}

.attachments-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: var(--dt-bg-hover);
  border-radius: var(--dt-radius-md);
  transition: all var(--dt-transition-fast);
}

.attachment-item:hover {
  background: var(--dt-bg-active);
}

.file-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.file-name {
  flex: 1;
  font-size: 13px;
  color: var(--dt-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
}

.file-size {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  margin-right: 8px;
}

.download-btn {
  padding: 4px 8px;
  background: transparent;
  border: none;
  border-radius: var(--dt-radius-sm);
  color: var(--dt-text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--dt-transition-fast);
}

.download-btn:hover {
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);
}

.preview-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border-top: 1px solid var(--dt-border-light);
  background: var(--dt-bg-hover);
  flex-shrink: 0;
}

.toolbar-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: transparent;
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-md);
  font-size: 13px;
  color: var(--dt-text-secondary);
  cursor: pointer;
  transition: all var(--dt-transition-fast);
}

.toolbar-btn:hover {
  background: var(--dt-bg-active);
  color: var(--dt-text-primary);
}

.toolbar-btn.primary {
  background: var(--dt-brand-color);
  border-color: var(--dt-brand-color);
  color: #fff;
}

.toolbar-btn.primary:hover {
  background: var(--dt-brand-hover);
}

.toolbar-btn.danger:hover {
  background: var(--dt-color-danger);
  border-color: var(--dt-color-danger);
  color: #fff;
}

.toolbar-btn.icon-only {
  padding: 8px;
}

.toolbar-spacer {
  flex: 1;
}

// 空状态
.preview-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

// 移动端返回头部
.mobile-back-header {
  display: none;
}

@media (max-width: 767px) {
  .mobile-back-header {
    display: flex;
  }

  .preview-header .preview-actions {
    display: none;
  }
}

.preview-empty .empty-icon {
  font-size: 64px;
  color: var(--dt-border-color);
  margin-bottom: 16px;
}

.preview-empty .empty-text {
  font-size: 16px;
  color: var(--dt-text-secondary);
  margin: 0 0 8px;
}

.preview-empty .empty-hint {
  font-size: 13px;
  color: var(--dt-text-tertiary);
  margin: 0;
}

// 视图切换按钮
.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.view-toggle {
  display: flex;
  background: var(--dt-bg-hover);
  border-radius: var(--dt-radius-md);
  padding: 2px;
}

.view-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  background: transparent;
  border: none;
  border-radius: var(--dt-radius-md);
  color: var(--dt-text-secondary);
  cursor: pointer;
  transition: all var(--dt-transition-fast);
}

.view-btn:hover {
  color: var(--dt-text-primary);
}

.view-btn.active {
  background: var(--dt-bg-card);
  color: var(--dt-brand-color);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.view-btn .material-icons-outlined {
  font-size: 20px;
}

.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6px;
  background: transparent;
  border: none;
  border-radius: var(--dt-radius-sm);
  color: var(--dt-text-secondary);
  cursor: pointer;
  transition: all var(--dt-transition-fast);
}

.icon-btn:hover {
  background: var(--dt-bg-hover);
  color: var(--dt-text-primary);
}

.icon-btn.active {
  color: #faad14;
}

// 预览中状态的邮件项
.email-item.previewing {
  background: var(--dt-bg-active);
  border-left: 3px solid var(--dt-brand-color);
}

// 布局模式
.layout-preview .mail-list-area {
  flex: 1;
}

.layout-preview .mail-folders {
  width: 160px;
}

.layout-list .mail-preview-panel {
  display: none;
}

/* 暗色模式 */
.dark .mail-panel {
  background: var(--dt-bg-body-dark);
}

.dark .panel-header {
  background: var(--dt-bg-card-dark);
  border-color: var(--dt-border-dark);
}

.dark .panel-title {
  color: var(--dt-text-primary-dark);
}

.dark .mail-folders,
.dark .mail-list {
  background: var(--dt-bg-card-dark);
  border-color: var(--dt-border-dark);
}

.dark .email-list-header {
  border-color: var(--dt-border-dark);
}

.dark .email-item {
  border-color: var(--dt-border-dark);
}

.dark .email-item:hover {
  background: var(--dt-bg-hover-dark);
}

.dark .email-item.unread,
.dark .email-item.selected,
.dark .batch-toolbar {
  background: var(--dt-brand-bg-dark);
}

.dark .email-subject {
  color: var(--dt-text-primary-dark);
}

.dark .folder-item {
  color: var(--dt-text-secondary-dark);
}

.dark .folder-item:hover {
  background: var(--dt-bg-hover-dark);
}

.dark .folder-item.active {
  background: var(--dt-brand-bg-dark);
  color: var(--dt-brand-color);
}

.dark .folder-icon,
.dark .folder-count {
  color: var(--dt-text-tertiary-dark);
}

.dark .folder-item.active .folder-icon,
.dark .folder-item.active .folder-count {
  color: var(--dt-brand-color);
}

.dark .empty-icon {
  color: var(--dt-border-dark);
}

.dark .email-preview,
.dark .email-meta,
.dark .list-count {
  color: var(--dt-text-tertiary-dark);
}

// 预览面板暗色模式
.dark .mail-preview-panel {
  background: var(--dt-bg-card-dark);
  border-color: var(--dt-border-dark);
}

.dark .preview-header {
  border-color: var(--dt-border-dark);
}

.dark .preview-subject {
  color: var(--dt-text-primary-dark);
}

.dark .preview-sender {
  background: rgba(255, 255, 255, 0.05);
}

.dark .sender-name {
  color: var(--dt-text-primary-dark);
}

.dark .sender-email {
  color: var(--dt-text-secondary-dark);
}

.dark .body-content {
  color: var(--dt-text-primary-dark);
}

.dark .preview-attachments {
  border-color: var(--dt-border-dark);
}

.dark .attachments-header {
  color: var(--dt-text-primary-dark);
}

.dark .attachment-item {
  background: rgba(255, 255, 255, 0.05);
}

.dark .attachment-item:hover {
  background: rgba(255, 255, 255, 0.08);
}

.dark .attachment-item:hover {
  background: var(--dt-bg-hover-dark);
}

.dark .file-name {
  color: var(--dt-text-primary-dark);
}

.dark .preview-toolbar {
  background: rgba(255, 255, 255, 0.03);
  border-color: var(--dt-border-dark);
}

.dark .toolbar-btn {
  border-color: var(--dt-border-dark);
  color: var(--dt-text-secondary-dark);
}

.dark .toolbar-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  color: var(--dt-text-primary-dark);
}

.dark .view-toggle {
  background: rgba(255, 255, 255, 0.05);
}

.dark .view-btn.active {
  background: var(--dt-bg-card-dark);
}

.dark .preview-empty .empty-icon {
  color: var(--dt-border-dark);
}

/* 响应式设计 */
@media (max-width: 1023px) {
  .panel-content {
    padding: 12px 16px;
  }

  .mail-folders {
    width: 140px;
  }

  .folder-item {
    padding: 8px 10px;
  }

  .folder-label {
    font-size: 13px;
  }

  .batch-toolbar {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
  }

  .batch-actions {
    justify-content: space-between;
  }
}

@media (max-width: 767px) {
  .panel-header {
    padding: 12px 16px;
  }

  .panel-title {
    font-size: 16px;
  }

  .compose-btn {
    padding: 6px 12px;

    span {
      display: none;
    }
  }

  .mail-layout {
    flex-direction: column;
  }

  .mail-folders {
    width: 100%;
    display: flex;
    overflow-x: auto;
    padding: 6px;
    gap: 4px;

    .folder-item {
      flex-shrink: 0;
      padding: 6px 10px;
      white-space: nowrap;

      .folder-icon {
        display: none;
      }
    }
  }

  .batch-actions {
    flex-wrap: wrap;

    .el-button {
      font-size: 12px;
      padding: 4px 8px;

      span {
        display: none;
      }
    }
  }

  .email-item {
    padding: 10px 12px;
  }

  .email-avatar {
    width: 32px;
    height: 32px;
    font-size: 12px;
  }

  .email-subject {
    font-size: 13px;
  }

  .email-preview {
    font-size: 11px;
  }

  .pagination-wrapper {
    :deep(.el-pagination) {
      .el-pager li,
      .el-pagination__jump {
        display: none;
      }

      .btn-prev,
      .btn-next {
        padding: 0 8px;
      }
    }
  }
}

@media (max-width: 479px) {
  .panel-content {
    padding: 8px 12px;
  }

  .mail-list-area {
    gap: 8px;
  }

  .batch-toolbar {
    padding: 8px 12px;
  }

  .selection-info {
    font-size: 12px;
  }

  .email-list-header {
    padding: 8px 12px;
  }

  .email-item {
    padding: 8px 10px;

    .email-checkbox {
      margin-right: 8px;
    }
  }
}

// 预览面板响应式
@media (max-width: 1200px) {
  .layout-preview .mail-preview-panel {
    width: 380px;
  }
}

@media (max-width: 1023px) {
  .layout-preview .mail-preview-panel {
    width: 320px;
  }

  .preview-toolbar {
    flex-wrap: wrap;
  }

  .toolbar-btn {
    font-size: 12px;
    padding: 6px 10px;

    span:not(.material-icons-outlined) {
      display: none;
    }
  }
}

@media (max-width: 767px) {
  // 在小屏幕上，预览面板全屏覆盖
  .layout-preview .mail-layout {
    position: relative;
  }

  .mail-preview-panel {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    width: 100% !important;
    height: 100%;
    z-index: 1000;
    border-radius: 0;
    transform: translateX(100%);
    transition: transform 0.3s ease;
  }

  .mail-preview-panel.mobile-show {
    transform: translateX(0);
  }

  // 移动端返回头部
  .mobile-back-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    background: var(--dt-bg-card);
    border-bottom: 1px solid var(--dt-border-light);
  }

  .back-btn,
  .close-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 12px;
    background: transparent;
    border: none;
    border-radius: var(--dt-radius-md);
    font-size: 14px;
    color: var(--dt-text-secondary);
    cursor: pointer;
  }

  .back-btn .material-icons-outlined,
  .close-btn .material-icons-outlined {
    font-size: 20px;
  }

  .preview-header {
    padding: 12px 16px;
  }

  .preview-subject {
    font-size: 14px;
  }

  .preview-sender {
    margin: 0 16px;
  }

  .preview-body {
    padding: 16px;
  }

  .preview-attachments {
    padding: 12px 16px;
  }

  .preview-toolbar {
    padding: 12px 16px;
  }
}
</style>
