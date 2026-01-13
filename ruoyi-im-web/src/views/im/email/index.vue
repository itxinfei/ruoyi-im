<!--
  邮箱模块
  功能：邮件收发、文件夹管理、已读/星标
-->
<template>
  <div class="email-container">
    <!-- 左侧导航栏 -->
    <div class="email-sidebar">
      <div class="sidebar-header">
        <h3>邮箱</h3>
        <el-button type="primary" size="small" @click="openCompose">
          <el-icon><Edit /></el-icon> 写邮件
        </el-button>
      </div>

      <el-menu
        :default-active="currentFolder"
        class="folder-menu"
        @select="handleFolderSelect"
      >
        <el-menu-item :index="FOLDER_TYPE.INBOX">
          <div class="menu-item-content">
            <el-icon><Message /></el-icon>
            <span>收件箱</span>
            <el-badge v-if="unreadCount > 0" :value="unreadCount" class="badge" />
          </div>
        </el-menu-item>

        <el-menu-item :index="FOLDER_TYPE.SENT">
          <div class="menu-item-content">
            <el-icon><Promotion /></el-icon>
            <span>已发送</span>
          </div>
        </el-menu-item>

        <el-menu-item :index="FOLDER_TYPE.DRAFTS">
          <div class="menu-item-content">
            <el-icon><Document /></el-icon>
            <span>草稿箱</span>
            <el-badge v-if="draftCount > 0" :value="draftCount" class="badge" />
          </div>
        </el-menu-item>

        <el-menu-item :index="FOLDER_TYPE.SPAM">
          <div class="menu-item-content">
            <el-icon><Warning /></el-icon>
            <span>垃圾邮件</span>
          </div>
        </el-menu-item>

        <el-menu-item :index="FOLDER_TYPE.TRASH">
          <div class="menu-item-content">
            <el-icon><Delete /></el-icon>
            <span>回收站</span>
          </div>
        </el-menu-item>
      </el-menu>

      <!-- 存储空间提示 -->
      <div class="storage-info">
        <div class="storage-bar">
          <div class="storage-used" :style="{ width: '35%' }"></div>
        </div>
        <span class="storage-text">已使用 3.5GB / 10GB</span>
      </div>
    </div>

    <!-- 右侧邮件列表 -->
    <div class="email-main">
      <!-- 搜索和工具栏 -->
      <div class="email-toolbar">
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索邮件..."
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <div class="toolbar-actions">
          <el-checkbox
            v-model="selectAll"
            :indeterminate="isIndeterminate"
            @change="handleSelectAll"
          >全选</el-checkbox>

          <el-dropdown @command="handleBatchAction" trigger="click">
            <el-button size="small" :disabled="selectedEmails.length === 0">
              批量操作 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="read">标记已读</el-dropdown-item>
                <el-dropdown-item command="unread">标记未读</el-dropdown-item>
                <el-dropdown-item command="star">标星</el-dropdown-item>
                <el-dropdown-item command="unstar">取消星标</el-dropdown-item>
                <el-dropdown-item command="trash" divided>移至垃圾箱</el-dropdown-item>
                <el-dropdown-item command="delete">永久删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <el-button size="small" @click="refreshList">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </div>

      <!-- 邮件列表 -->
      <div class="email-list" v-loading="loading">
        <div v-if="filteredEmails.length === 0" class="empty-state">
          <el-icon :size="64"><Message /></el-icon>
          <p>暂无邮件</p>
        </div>

        <div
          v-for="email in filteredEmails"
          :key="email.id"
          class="email-item"
          :class="{
            unread: !email.isRead,
            selected: selectedEmails.includes(email.id),
            starred: email.isStarred
          }"
          @click="openEmail(email)"
          @contextmenu.prevent="showContextMenu($event, email)"
        >
          <el-checkbox
            class="email-checkbox"
            :model-value="selectedEmails.includes(email.id)"
            @change="toggleSelect(email.id, $event)"
            @click.stop
          />

          <div class="email-star" @click.stop="toggleStar(email)">
            <el-icon><Star :fill="email.isStarred ? 'currentColor' : 'none'" /></el-icon>
          </div>

          <div class="email-sender">{{ email.senderName || email.senderEmail }}</div>

          <div class="email-content">
            <div class="email-subject" :class="{ unread: !email.isRead }">
              {{ email.subject || '(无主题)' }}
              <span v-if="email.attachmentCount > 0" class="attachment-icon">
                <el-icon><Paperclip /></el-icon>
              </span>
            </div>
            <div class="email-preview">{{ getEmailPreview(email) }}</div>
          </div>

          <div class="email-time">{{ formatEmailTime(email.sendTime || email.createTime) }}</div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="email-pagination" v-if="filteredEmails.length > 0">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="totalCount"
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 写邮件对话框 -->
    <compose-dialog
      v-model:visible="composeVisible"
      :reply-to="replyToEmail"
      @sent="handleEmailSent"
    />

    <!-- 邮件详情对话框 -->
    <email-detail-dialog
      v-model:visible="detailVisible"
      :email-id="currentEmailId"
      @reply="handleReply"
      @forward="handleForward"
    />

    <!-- 右键菜单 -->
    <context-menu
      ref="contextMenu"
      :menu-items="contextMenuItems"
      @select="handleContextMenuSelect"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Edit,
  Message,
  Promotion,
  Document,
  Warning,
  Delete,
  Search,
  Refresh,
  ArrowDown,
  Star,
  ChatDotSquare,
  Paperclip
} from '@element-plus/icons-vue'
import * as emailApi from '@/api/im/email'
import ComposeDialog from './components/ComposeDialog.vue'
import EmailDetailDialog from './components/EmailDetailDialog.vue'
import ContextMenu from './components/ContextMenu.vue'

const FOLDER_TYPE = emailApi.FOLDER_TYPE

// 状态
const loading = ref(false)
const currentFolder = ref(FOLDER_TYPE.INBOX)
const searchKeyword = ref('')
const emails = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const totalCount = ref(0)
const unreadCount = ref(0)
const draftCount = ref(0)

// 选择状态
const selectedEmails = ref([])
const selectAll = ref(false)
const isIndeterminate = ref(false)

// 对话框状态
const composeVisible = ref(false)
const detailVisible = ref(false)
const currentEmailId = ref(null)
const replyToEmail = ref(null)

// 右键菜单
const contextMenu = ref(null)
const contextMenuEmail = ref(null)

// 计算属性
const filteredEmails = computed(() => {
  if (!searchKeyword.value) {
    return emails.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return emails.value.filter(email =>
    (email.subject && email.subject.toLowerCase().includes(keyword)) ||
    (email.senderName && email.senderName.toLowerCase().includes(keyword)) ||
    (email.senderEmail && email.senderEmail.toLowerCase().includes(keyword))
  )
})

const contextMenuItems = computed(() => [
  { label: '查看', command: 'view' },
  { label: '标记已读', command: 'read' },
  { label: '标记未读', command: 'unread' },
  { label: '标星', command: 'star' },
  { label: '取消星标', command: 'unstar' },
  { divider: true },
  { label: '回复', command: 'reply' },
  { label: '转发', command: 'forward' },
  { divider: true },
  { label: '移至垃圾箱', command: 'trash' },
  { label: '永久删除', command: 'delete', danger: true },
])

// 方法
const loadEmails = async () => {
  loading.value = true
  try {
    let api
    switch (currentFolder.value) {
      case FOLDER_TYPE.INBOX:
        api = emailApi.getInboxEmails
        break
      case FOLDER_TYPE.SENT:
        api = emailApi.getSentEmails
        break
      case FOLDER_TYPE.DRAFTS:
        api = emailApi.getDraftEmails
        break
      case FOLDER_TYPE.SPAM:
        api = emailApi.getSpamEmails
        break
      case FOLDER_TYPE.TRASH:
        api = emailApi.getTrashEmails
        break
    }

    const response = await api()
    if (response.code === 200) {
      emails.value = response.data || []
      totalCount.value = emails.value.length
    }
  } catch (error) {
    console.error('加载邮件失败:', error)
  } finally {
    loading.value = false
  }
}

const loadUnreadCount = async () => {
  try {
    const response = await emailApi.getUnreadCount()
    if (response.code === 200) {
      unreadCount.value = response.data || 0
    }
  } catch (error) {
    console.error('获取未读数失败:', error)
  }
}

const handleFolderSelect = (folder) => {
  currentFolder.value = folder
  currentPage.value = 1
  selectedEmails.value = []
  loadEmails()
}

const handleSearch = () => {
  // 搜索已在 computed 中处理
}

const handlePageChange = (page) => {
  currentPage.value = page
  // 可以在此实现分页加载
}

const handleSelectAll = (val) => {
  selectedEmails.value = val ? filteredEmails.value.map(e => e.id) : []
  isIndeterminate.value = false
}

const toggleSelect = (id, checked) => {
  if (checked) {
    selectedEmails.value.push(id)
  } else {
    const index = selectedEmails.value.indexOf(id)
    if (index > -1) {
      selectedEmails.value.splice(index, 1)
    }
  }
  updateSelectAllState()
}

const updateSelectAllState = () => {
  const total = filteredEmails.value.length
  const selected = selectedEmails.value.length
  selectAll.value = total > 0 && selected === total
  isIndeterminate.value = selected > 0 && selected < total
}

const openCompose = () => {
  replyToEmail.value = null
  composeVisible.value = true
}

const openEmail = async (email) => {
  currentEmailId.value = email.id
  detailVisible.value = true

  // 标记为已读
  if (!email.isRead) {
    try {
      await emailApi.markAsRead(email.id)
      email.isRead = true
      loadUnreadCount()
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
}

const toggleStar = async (email) => {
  try {
    await emailApi.markAsStarred(email.id, !email.isStarred)
    email.isStarred = !email.isStarred
    ElMessage.success(email.isStarred ? '已标星' : '已取消星标')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const refreshList = () => {
  loadEmails()
  loadUnreadCount()
}

const handleBatchAction = async (command) => {
  const ids = selectedEmails.value
  if (ids.length === 0) return

  try {
    switch (command) {
      case 'read':
        await emailApi.batchMarkAsRead(ids)
        emails.value.forEach(e => {
          if (ids.includes(e.id)) e.isRead = true
        })
        break
      case 'unread':
        emails.value.forEach(e => {
          if (ids.includes(e.id)) e.isRead = false
        })
        break
      case 'star':
        emails.value.forEach(e => {
          if (ids.includes(e.id)) e.isStarred = true
        })
        break
      case 'unstar':
        emails.value.forEach(e => {
          if (ids.includes(e.id)) e.isStarred = false
        })
        break
      case 'trash':
        await emailApi.batchMoveToTrash(ids)
        emails.value = emails.value.filter(e => !ids.includes(e.id))
        break
      case 'delete':
        await ElMessageBox.confirm('确定永久删除选中的邮件吗？', '提示', {
          type: 'warning'
        })
        await emailApi.batchPermanentlyDelete(ids)
        emails.value = emails.value.filter(e => !ids.includes(e.id))
        break
    }
    selectedEmails.value = []
    updateSelectAllState()
    ElMessage.success('操作成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleEmailSent = () => {
  composeVisible.value = false
  if (currentFolder.value === FOLDER_TYPE.SENT) {
    loadEmails()
  }
}

const handleReply = (email) => {
  replyToEmail.value = email
  detailVisible.value = false
  composeVisible.value = true
}

const handleForward = (email) => {
  replyToEmail.value = { ...email, isForward: true }
  detailVisible.value = false
  composeVisible.value = true
}

const showContextMenu = (event, email) => {
  contextMenuEmail.value = email
  contextMenu.value?.show(event)
}

const handleContextMenuSelect = async (command) => {
  const email = contextMenuEmail.value
  if (!email) return

  switch (command) {
    case 'view':
      openEmail(email)
      break
    case 'read':
      await emailApi.markAsRead(email.id)
      email.isRead = true
      break
    case 'unread':
      email.isRead = false
      break
    case 'star':
      await emailApi.markAsStarred(email.id, true)
      email.isStarred = true
      break
    case 'unstar':
      await emailApi.markAsStarred(email.id, false)
      email.isStarred = false
      break
    case 'reply':
      handleReply(email)
      break
    case 'forward':
      handleForward(email)
      break
    case 'trash':
      await emailApi.moveToTrash(email.id)
      emails.value = emails.value.filter(e => e.id !== email.id)
      break
    case 'delete':
      await ElMessageBox.confirm('确定永久删除此邮件吗？', '提示', {
        type: 'warning'
      })
      await emailApi.permanentlyDelete(email.id)
      emails.value = emails.value.filter(e => e.id !== email.id)
      break
  }
}

const getEmailPreview = (email) => {
  const text = email.textContent || email.htmlContent || ''
  // 移除 HTML 标签获取纯文本预览
  const plainText = text.replace(/<[^>]*>/g, '').trim()
  return plainText.substring(0, 100) || '(无内容)'
}

const formatEmailTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  // 今天
  if (diff < 86400000 && date.getDate() === now.getDate()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  // 昨天
  if (diff < 172800000) {
    return '昨天'
  }
  // 今年
  if (date.getFullYear() === now.getFullYear()) {
    return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
  }
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

// 生命周期
onMounted(() => {
  loadEmails()
  loadUnreadCount()
})

watch(selectedEmails, updateSelectAllState)
</script>

<style lang="scss" scoped>
@use '@/styles/dingtalk-theme.scss' as *;

.email-container {
  display: flex;
  height: 100%;
  background: #f5f5f5;
}

// 左侧导航栏
.email-sidebar {
  width: 220px;
  background: #fff;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;

  .sidebar-header {
    padding: 16px;
    border-bottom: 1px solid #e8e8e8;

    h3 {
      margin: 0 0 12px 0;
      font-size: 18px;
      font-weight: 500;
    }

    .el-button {
      width: 100%;
    }
  }

  .folder-menu {
    flex: 1;
    border: none;

    .el-menu-item {
      height: 44px;

      .menu-item-content {
        display: flex;
        align-items: center;
        width: 100%;
        gap: 8px;

        .badge {
          margin-left: auto;
        }
      }
    }
  }

  .storage-info {
    padding: 16px;
    border-top: 1px solid #e8e8e8;

    .storage-bar {
      height: 4px;
      background: #e8e8e8;
      border-radius: 2px;
      margin-bottom: 8px;
      overflow: hidden;

      .storage-used {
        height: 100%;
        background: linear-gradient(90deg, #0089ff, #00c2ff);
        border-radius: 2px;
      }
    }

    .storage-text {
      font-size: 12px;
      color: #999;
    }
  }
}

// 右侧主区域
.email-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
}

// 工具栏
.email-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #e8e8e8;

  .search-box {
    width: 300px;
  }

  .toolbar-actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }
}

// 邮件列表
.email-list {
  flex: 1;
  overflow-y: auto;

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: #999;

    i {
      font-size: 64px;
      margin-bottom: 16px;
      opacity: 0.5;
    }
  }
}

// 邮件项
.email-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: #f5f9ff;
  }

  &.selected {
    background: #e6f7ff;
  }

  &.unread {
    background: #fafbff;

    .email-subject {
      font-weight: 600;
    }
  }

  &.starred .email-star .el-icon {
    color: #fadb14;
  }

  .email-checkbox {
    margin-right: 12px;
  }

  .email-star {
    margin-right: 8px;
    color: #d9d9d9;
    display: flex;
    align-items: center;

    .el-icon {
      font-size: 18px;
      transition: color 0.2s;
    }
  }

  .email-sender {
    width: 140px;
    font-size: 14px;
    color: #333;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .email-content {
    flex: 1;
    min-width: 0;
    padding: 0 16px;

    .email-subject {
      font-size: 14px;
      color: #333;
      margin-bottom: 4px;
      display: flex;
      align-items: center;
      gap: 4px;

      &.unread {
        font-weight: 600;
      }

      .attachment-icon {
        color: #999;
        font-size: 12px;
        display: flex;
        align-items: center;
      }
    }

    .email-preview {
      font-size: 13px;
      color: #999;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .email-time {
    width: 80px;
    text-align: right;
    font-size: 12px;
    color: #999;
  }
}

// 分页
.email-pagination {
  padding: 12px 16px;
  border-top: 1px solid #e8e8e8;
  display: flex;
  justify-content: center;
}
</style>
