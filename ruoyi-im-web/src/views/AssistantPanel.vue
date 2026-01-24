<template>
  <div class="assistant-panel">
    <div class="panel-header">
      <h2 class="panel-title">AI 助理</h2>
    </div>

    <div class="panel-content">
      <!-- 欢迎区 -->
      <div class="welcome-section">
        <div class="ai-avatar">
          <span class="material-icons-outlined">smart_toy</span>
        </div>
        <h3 class="welcome-title">你好，我是你的 AI 助理</h3>
        <p class="welcome-desc">有什么可以帮您的吗？</p>
      </div>

      <!-- 快捷功能 -->
      <div class="quick-actions">
        <div class="section-title">快捷功能</div>
        <div class="action-grid">
          <div
            v-for="action in quickActions"
            :key="action.id"
            class="action-card"
            @click="handleAction(action)"
          >
            <div class="action-icon" :style="{ background: action.bgColor }">
              <span class="material-icons-outlined">{{ action.icon }}</span>
            </div>
            <span class="action-label">{{ action.label }}</span>
          </div>
        </div>
      </div>

      <!-- 对话历史 -->
      <div class="chat-section">
        <div class="section-title">最近对话</div>
        <div v-if="chats.length === 0" class="empty-chats">
          <p class="empty-text">暂无对话记录</p>
        </div>
        <div v-else class="chat-list">
          <div
            v-for="chat in chats"
            :key="chat.id"
            class="chat-item"
            @click="handleChat(chat)"
          >
            <div class="chat-icon">
              <span class="material-icons-outlined">chat_bubble</span>
            </div>
            <div class="chat-content">
              <div class="chat-title">{{ chat.title }}</div>
              <div class="chat-preview">{{ chat.preview }}</div>
            </div>
            <span class="chat-time">{{ chat.time }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const quickActions = [
  { id: 1, label: '写文案', icon: 'edit', bgColor: '#1677ff' },
  { id: 2, label: '翻译', icon: 'translate', bgColor: '#52c41a' },
  { id: 3, label: '总结', icon: 'summarize', bgColor: '#fa8c16' },
  { id: 4, label: '代码', icon: 'code', bgColor: '#722ed1' },
  { id: 5, label: '分析', icon: 'analytics', bgColor: '#eb2f96' },
  { id: 6, label: '创意', icon: 'lightbulb', bgColor: '#8b5cf6' }
]

const chats = ref([
  {
    id: 1,
    title: '周报总结',
    preview: '帮我生成本周工作总结...',
    time: '昨天'
  },
  {
    id: 2,
    title: '代码优化',
    preview: '这段代码如何优化...',
    time: '3天前'
  }
])

const handleAction = (action) => {
  ElMessage.info(`打开功能: ${action.label}`)
}

const handleChat = (chat) => {
  ElMessage.info(`继续对话: ${chat.title}`)
}
</script>

<style scoped>
.assistant-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  flex: 1;
  min-width: 0;
  background: #f4f7f9;
}

.panel-header {
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

.panel-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.welcome-section {
  text-align: center;
  margin-bottom: 32px;
}

.ai-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.ai-avatar .material-icons-outlined {
  font-size: 40px;
}

.welcome-title {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 8px 0;
}

.welcome-desc {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #595959;
  margin: 0 0 16px 0;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.action-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.action-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-icon .material-icons-outlined {
  font-size: 24px;
}

.action-label {
  font-size: 13px;
  color: #595959;
}

.chat-section {
  margin-top: 32px;
}

.chat-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.empty-chats {
  text-align: center;
  padding: 32px 0;
}

.empty-text {
  font-size: 14px;
  color: #bfbfbf;
}

.chat-item {
  background: #fff;
  border-radius: 12px;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.chat-item:hover {
  background: #fafafa;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.chat-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: #f0f0f0;
  color: #1677ff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-icon .material-icons-outlined {
  font-size: 18px;
}

.chat-content {
  flex: 1;
  min-width: 0;
}

.chat-title {
  font-size: 14px;
  font-weight: 500;
  color: #262626;
  margin-bottom: 2px;
}

.chat-preview {
  font-size: 12px;
  color: #8c8c8c;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-time {
  font-size: 11px;
  color: #bfbfbf;
}

/* 暗色模式 */
:deep(.dark) .assistant-panel {
  background: #0f172a;
}

:deep(.dark) .panel-header {
  background: #1e293b;
  border-color: #334155;
}

:deep(.dark) .panel-title,
:deep(.dark) .welcome-title {
  color: #f1f5f9;
}

:deep(.dark) .action-card,
:deep(.dark) .chat-item {
  background: #1e293b;
}

:deep(.dark) .action-card:hover,
:deep(.dark) .chat-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

:deep(.dark) .action-label {
  color: #cbd5e1;
}

:deep(.dark) .section-title {
  color: #94a3b8;
}

:deep(.dark) .chat-preview {
  color: #64748b;
}
</style>
