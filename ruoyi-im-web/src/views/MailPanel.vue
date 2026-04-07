<template>
  <div class="mail-panel">
    <div class="panel-header">
      <h2 class="panel-title">
        邮箱
      </h2>
      <div class="mail-search">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索邮件主题/发件人"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearchClear"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      <button class="compose-btn" @click="showComposeDialog = true">
        <el-icon><Edit /></el-icon>
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
            @click="activeFolder = folder.key"
          >
            <el-icon class="folder-icon"><component :is="folder.iconEl" /></el-icon>
            <span class="folder-label">{{ folder.label }}</span>
            <span v-if="folder.count > 0" class="folder-count">{{ folder.count }}</span>
          </div>
        </div>

        <!-- 邮件列表 -->
        <div class="mail-list">
          <!-- 搜索结果提示 -->
          <div v-if="isSearchMode" class="search-result-tip">
            <span>搜索"{{ searchKeyword }}"的结果 ({{ searchResults.length }} 封邮件)</span>
            <el-button text size="small" @click="handleSearchClear">清除搜索</el-button>
          </div>

          <div v-if="loading" class="loading-state">
            <el-icon class="is-loading">
              <Loading />
            </el-icon>
            <span>加载中...</span>
          </div>

          <div v-else-if="displayEmails.length === 0" class="empty-state">
            <el-icon class="empty-icon"><Message /></el-icon>
            <p class="empty-text">
              {{ isSearchMode ? '未找到匹配的邮件' : '暂无邮件' }}
            </p>
          </div>

          <div v-else class="email-list">
            <div
              v-for="email in displayEmails"
              :key="email.id"
              class="email-item"
              :class="{ unread: !email.read }"
              @click="handleViewEmail(email)"
            >
              <div class="email-avatar" :style="{ background: email.avatarColor }">
                {{ email.sender.charAt(0) }}
              </div>
              <div class="email-content">
                <div class="email-subject">
                  {{ email.subject }}
                </div>
                <div class="email-preview">
                  {{ email.preview }}
                </div>
                <div class="email-meta">
                  <span class="email-sender">{{ email.sender }}</span>
                  <span class="email-time">{{ email.time }}</span>
                </div>
              </div>
              <div v-if="!email.read" class="unread-dot" />
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
      @reply="handleReply"
      @forward="handleForward"
      @delete="handleDeleteEmail"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { Loading, Edit, Box, Promotion, Document, Star, Delete, Message, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ComposeMailDialog from '@/components/ComposeMailDialog/index.vue'
import MailDetailDialog from '@/components/MailDetailDialog/index.vue'
import { getMailList, markAsRead, deleteMail, searchMail } from '@/api/im/mail'

const loading = ref(false)
const showComposeDialog = ref(false)
const showDetailDialog = ref(false)
const selectedEmail = ref(null)
const replyToEmail = ref(null)
const activeFolder = ref('inbox')
const emails = ref([])

// 搜索相关状态
const searchKeyword = ref('')
const isSearchMode = ref(false)
const searchResults = ref([])

// 计算属性：显示的邮件列表
const displayEmails = computed(() => {
  return isSearchMode.value ? searchResults.value : emails.value
})


const folders = ref([
  { key: 'inbox', label: '收件箱', icon: 'Box', iconEl: Box, count: 0 },
  { key: 'sent', label: '已发送', icon: 'Promotion', iconEl: Promotion, count: 0 },
  { key: 'draft', label: '草稿箱', icon: 'Document', iconEl: Document, count: 0 },
  { key: 'starred', label: '星标邮件', icon: 'Star', iconEl: Star, count: 0 },
  { key: 'trash', label: '已删除', icon: 'Delete', iconEl: Delete, count: 0 }
])

// 监听文件夹切换
watch(activeFolder, () => {
  loadMails()
  // 切换文件夹时退出搜索模式
  if (isSearchMode.value) {
    handleSearchClear()
  }
})

// 搜索邮件
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    handleSearchClear()
    return
  }
  loading.value = true
  isSearchMode.value = true
  try {
    const res = await searchMail(searchKeyword.value.trim())
    if (res.code === 200) {
      searchResults.value = (res.data || []).map(email => ({
        ...email,
        avatarColor: getRandomColor()
      }))
    } else {
      ElMessage.error(res.msg || '搜索失败')
    }
  } catch (error) {
    console.error('搜索邮件失败', error)
    ElMessage.error('搜索失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 清除搜索
const handleSearchClear = () => {
  searchKeyword.value = ''
  isSearchMode.value = false
  searchResults.value = []
}

// 加载邮件列表
const loadMails = async () => {
  loading.value = true
  try {
    const res = await getMailList(activeFolder.value)
    if (res.code === 200) {
      emails.value = (res.data || []).map(email => ({
        ...email,
        avatarColor: getRandomColor()
      }))
      // 更新文件夹计数
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
const updateFolderCounts = () => {
  folders.value.forEach(folder => {
    if (folder.key === activeFolder.value) {
      folder.count = emails.value.length
    }
  })
}

// 随机颜色
const getRandomColor = () => {
  const colors = ['var(--dt-brand-color)', 'var(--dt-success-color)', 'var(--dt-warning-color)', 'var(--dt-info-color)', 'var(--dt-error-color)']
  return colors[Math.floor(Math.random() * colors.length)]
}

const handleViewEmail = async (email) => {
  // 标记为已读
  if (!email.read) {
    try {
      const res = await markAsRead(email.id)
      if (res.code === 200) {
        email.read = true
      }
    } catch (error) {
      console.error('标记已读失败', error)
    }
  }
  // 显示详情对话框
  selectedEmail.value = email
  showDetailDialog.value = true
}

// 回复邮件
const handleReply = (email) => {
  replyToEmail.value = {
    id: email.id,
    to: email.sender,
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
    content: `\n\n---------- 转发的邮件 ----------\n发件人: ${email.sender}\n时间: ${email.time}\n主题: ${email.subject}\n\n${email.preview || ''}`,
    isForward: true
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
      ElMessage.success('删除成功')
      showDetailDialog.value = false
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch {
    // 用户取消
  }
}

// 发送邮件成功回调
const handleMailSent = () => {
  replyToEmail.value = null
  if (activeFolder.value === 'sent') {
    loadMails()
  } else {
    activeFolder.value = 'sent'
  }
}

// 组件加载时初始加载数据
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
  padding: var(--dt-spacing-lg) var(--dt-spacing-xl);
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);
  flex-shrink: 0;
}

.panel-title {
  font-size: var(--dt-font-size-xl);
  font-weight: var(--dt-font-weight-semibold);
  color: var(--dt-text-primary);
  margin: 0;
}

.mail-search {
  flex: 1;
  max-width: 320px;
  margin: 0 var(--dt-spacing-lg);
}

.compose-btn {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-sm);
  padding: var(--dt-spacing-sm) var(--dt-spacing-lg);
  background: var(--dt-brand-color);
  color: var(--dt-text-primary);
  border: none;
  border-radius: var(--dt-radius-lg);
  font-size: var(--dt-font-size-sm);
  font-weight: var(--dt-font-weight-medium);
  cursor: pointer;
  transition: background-color var(--dt-transition-fast);
}

.compose-btn:hover {
  background: var(--dt-brand-hover);
}

.compose-btn:active {
  opacity: 0.9;
}

.panel-content {
  flex: 1;
  padding: var(--dt-spacing-lg) var(--dt-spacing-xl);
  overflow: hidden;
}

.mail-layout {
  display: flex;
  height: 100%;
  gap: var(--dt-spacing-lg);
}

.mail-folders {
  width: var(--dt-folder-panel-width, 180px);
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-xl);
  padding: var(--dt-spacing-sm);
  flex-shrink: 0;
  border: 1px solid var(--dt-border-light);
}

.folder-item {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-sm);
  padding: var(--dt-spacing-sm) var(--dt-spacing-xs);
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: background-color var(--dt-transition-fast);
}

.folder-item:hover {
  background: var(--dt-bg-hover);
}

.folder-item.active {
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);
  font-weight: var(--dt-font-weight-medium);
}

.folder-icon {
  font-size: var(--dt-icon-size-lg);
  color: var(--dt-text-tertiary);
}

.folder-label {
  flex: 1;
  font-size: var(--dt-font-size-sm);
}

.folder-count {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-tertiary);
}

.folder-item.active .folder-count {
  color: var(--dt-brand-color);
}

.mail-list {
  flex: 1;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-xl);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--dt-border-light);
}

.search-result-tip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background-color: var(--dt-bg-hover);
  border-bottom: 1px solid var(--dt-border-light);
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: var(--dt-empty-state-height, 200px);
  color: var(--dt-text-tertiary);
}

.empty-icon {
  font-size: var(--dt-icon-size-xl, 64px);
  margin-bottom: var(--dt-spacing-lg);
  color: var(--dt-border-color);
}

.empty-text {
  font-size: var(--dt-font-size-sm);
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
  padding: var(--dt-spacing-md) var(--dt-spacing-md);
  cursor: pointer;
  position: relative;
  border-bottom: 1px solid var(--dt-border-light);
  transition: background-color var(--dt-transition-fast);
}

.email-item:hover {
  background: var(--dt-bg-hover);
}

.email-item.unread {
  background: var(--dt-brand-bg);
}

.email-item.unread:hover {
  background: var(--dt-brand-bg);
  filter: brightness(0.98);
}

.email-avatar {
  width: var(--dt-avatar-size-lg, 40px);
  height: var(--dt-avatar-size-lg, 40px);
  border-radius: var(--dt-radius-lg);
  color: var(--dt-text-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--dt-font-size-sm);
  font-weight: var(--dt-font-weight-semibold);
  flex-shrink: 0;
}

.email-content {
  flex: 1;
  min-width: 0;
}

.email-subject {
  font-size: var(--dt-font-size-sm);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
  margin-bottom: var(--dt-spacing-xs);
}

.email-item.unread .email-subject {
  font-weight: var(--dt-font-weight-semibold);
  color: var(--dt-brand-color);
}

.email-preview {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-tertiary);
  margin-bottom: var(--dt-spacing-xs);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.email-meta {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-md);
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-tertiary);
}

.unread-dot {
  width: var(--dt-dot-size, 8px);
  height: var(--dt-dot-size, 8px);
  background: var(--dt-brand-color);
  border-radius: 50%;
  position: absolute;
  right: var(--dt-spacing-lg);
  box-shadow: 0 0 0 var(--dt-border-thick, 3px) var(--dt-brand-bg);
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

.dark .email-item {
  border-color: var(--dt-border-dark);
}

.dark .email-item:hover {
  background: var(--dt-bg-hover-dark);
}

.dark .email-item.unread {
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
.dark .email-meta {
  color: var(--dt-text-tertiary-dark);
}
</style>
