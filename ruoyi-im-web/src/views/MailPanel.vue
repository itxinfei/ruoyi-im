<template>
  <div class="mail-panel">
    <div class="panel-header">
      <h2 class="panel-title">邮箱</h2>
      <button class="compose-btn" @click="showComposeDialog = true">
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
            @click="activeFolder = folder.key"
          >
            <span class="material-icons-outlined folder-icon">{{ folder.icon }}</span>
            <span class="folder-label">{{ folder.label }}</span>
            <span v-if="folder.count > 0" class="folder-count">{{ folder.count }}</span>
          </div>
        </div>

        <!-- 邮件列表 -->
        <div class="mail-list">
          <div v-if="loading" class="loading-state">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载中...</span>
          </div>

          <div v-else-if="emails.length === 0" class="empty-state">
            <span class="material-icons-outlined empty-icon">email</span>
            <p class="empty-text">暂无邮件</p>
          </div>

          <div v-else class="email-list">
            <div
              v-for="email in emails"
              :key="email.id"
              class="email-item"
              :class="{ unread: !email.read }"
              @click="handleViewEmail(email)"
            >
              <div class="email-avatar" :style="{ background: email.avatarColor }">
                {{ email.sender.charAt(0) }}
              </div>
              <div class="email-content">
                <div class="email-subject">{{ email.subject }}</div>
                <div class="email-preview">{{ email.preview }}</div>
                <div class="email-meta">
                  <span class="email-sender">{{ email.sender }}</span>
                  <span class="email-time">{{ email.time }}</span>
                </div>
              </div>
              <div v-if="!email.read" class="unread-dot"></div>
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
import { ref, computed, watch } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ComposeMailDialog from '@/components/ComposeMailDialog/index.vue'
import MailDetailDialog from '@/components/MailDetailDialog/index.vue'
import { getMailList, markAsRead, deleteMail } from '@/api/im/mail'

const loading = ref(false)
const showComposeDialog = ref(false)
const showDetailDialog = ref(false)
const selectedEmail = ref(null)
const replyToEmail = ref(null)
const activeFolder = ref('inbox')
const emails = ref([])


const folders = ref([
  { key: 'inbox', label: '收件箱', icon: 'inbox', count: 0 },
  { key: 'sent', label: '已发送', icon: 'send', count: 0 },
  { key: 'draft', label: '草稿箱', icon: 'draft', count: 0 },
  { key: 'starred', label: '星标邮件', icon: 'star', count: 0 },
  { key: 'trash', label: '已删除', icon: 'delete', count: 0 }
])

// 监听文件夹切换
watch(activeFolder, () => {
  loadMails()
})

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
  const colors = ['#1677ff', '#52c41a', '#fa8c16', '#722ed1', '#eb2f96']
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
loadMails()
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

.mail-list {
  flex: 1;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-xl);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--dt-border-light);
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
