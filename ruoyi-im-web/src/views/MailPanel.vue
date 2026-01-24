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
      @success="handleMailSent"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import ComposeMailDialog from '@/components/ComposeMailDialog/index.vue'
import { getMailList, markAsRead } from '@/api/im/mail'

const loading = ref(false)
const showComposeDialog = ref(false)
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
  ElMessage.info(`查看邮件: ${email.subject}`)
}

// 发送邮件成功回调
const handleMailSent = () => {
  if (activeFolder.value === 'sent') {
    loadMails()
  } else {
    activeFolder.value = 'sent'
  }
}

// 组件加载时初始加载数据
loadMails()
</script>

<style scoped>
.mail-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  flex: 1;
  min-width: 0;
  background: #f4f7f9;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  flex-shrink: 0;
}

.panel-title {
  font-size: 18px;
  font-weight: 600;
  color: #262626;
  margin: 0;
}

.compose-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #1677ff;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.compose-btn:hover {
  background: #4096ff;
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
  background: #fff;
  border-radius: 12px;
  padding: 8px;
  flex-shrink: 0;
}

.folder-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.folder-item:hover {
  background: #f5f5f5;
}

.folder-item.active {
  background: #e6f7ff;
  color: #1677ff;
}

.folder-icon {
  font-size: 20px;
  color: #8c8c8c;
}

.folder-label {
  flex: 1;
  font-size: 14px;
}

.folder-count {
  font-size: 12px;
  color: #8c8c8c;
}

.mail-list {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #8c8c8c;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  color: #d9d9d9;
}

.empty-text {
  font-size: 14px;
  margin: 0;
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
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s;
}

.email-item:hover {
  background: #f9f9f9;
}

.email-item.unread {
  background: #f0f7ff;
}

.email-avatar {
  width: 40px;
  height: 40px;
  border-radius: 10px;
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
  color: #262626;
  margin-bottom: 4px;
}

.email-item.unread .email-subject {
  font-weight: 600;
}

.email-preview {
  font-size: 12px;
  color: #8c8c8c;
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
  color: #8c8c8c;
}

.unread-dot {
  width: 8px;
  height: 8px;
  background: #1677ff;
  border-radius: 50%;
  position: absolute;
  right: 16px;
}

/* 暗色模式 */
:deep(.dark) .mail-panel {
  background: #0f172a;
}

:deep(.dark) .panel-header {
  background: #1e293b;
  border-color: #334155;
}

:deep(.dark) .panel-title {
  color: #f1f5f9;
}

:deep(.dark) .mail-folders,
:deep(.dark) .mail-list {
  background: #1e293b;
}

:deep(.dark) .email-item {
  border-color: #334155;
}

:deep(.dark) .email-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

:deep(.dark) .email-item.unread {
  background: rgba(22, 119, 255, 0.1);
}

:deep(.dark) .email-subject {
  color: #f1f5f9;
}

:deep(.dark) .folder-item.active {
  background: rgba(22, 119, 255, 0.15);
  color: #60a5fa;
}
</style>
