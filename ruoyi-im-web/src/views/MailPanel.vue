<template>
  <div class="mail-panel">
    <div class="panel-header">
      <h2 class="panel-title">邮箱</h2>
      <button class="compose-btn" @click="openComposeDialog">
        <span class="material-icons-outlined">edit</span>
        写邮件
      </button>
    </div>

    <div class="panel-content">
      <div class="mail-layout">
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
                  starred: email.starred
                }"
                @click="handleViewEmail(email)"
              >
                <el-checkbox
                  class="email-checkbox"
                  :model-value="selectedEmails.includes(email.id)"
                  @update:model-value="toggleSelection(email.id, $event)"
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
    if (res.code === 200) {
      emails.value = (res.data?.list || res.data || []).map(email => ({
        ...email,
        avatarColor: getRandomColor()
      }))
      totalCount.value = res.data?.total || res.data?.length || 0
      updateFolderCounts()
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  } catch (error) {
    console.error('加载邮件列表失败', error)
    ElMessage.error('加载失败，请稍后重试')
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
  const colors = ['#1677ff', '#52c41a', '#fa8c16', '#722ed1', '#eb2f96']
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
        // 更新文件夹未读数
        const folder = folders.value.find(f => f.key === activeFolder.value)
        if (folder && folder.unreadCount > 0) {
          folder.unreadCount--
        }
      }
    } catch (error) {
      console.error('标记已读失败', error)
    }
  }
  selectedEmail.value = email
  showDetailDialog.value = true
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
      updateFolderCounts()
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
      updateFolderCounts()
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
      updateFolderCounts()
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
      updateFolderCounts()
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
      updateFolderCounts()
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
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.25);
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
  border-radius: 50%;
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
</style>
