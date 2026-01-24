<template>
  <div class="chat-header">
    <div class="header-left">
      <div class="header-avatar" :class="getAvatarBgClass(session)" aria-hidden="true">
        {{ session?.name?.charAt(0) || '?' }}
      </div>
      <div class="header-info">
        <h2 class="header-name">{{ session?.name }}</h2>
        <span v-if="session?.type === 'GROUP'" class="member-count" aria-label="群组成员数量">{{ session?.memberCount || 0 }}人</span>
      </div>
    </div>
    <div class="header-actions">
      <button class="action-btn" aria-label="语音通话" title="语音通话">
        <span class="material-icons-outlined" aria-hidden="true">phone</span>
      </button>
      <button class="action-btn" aria-label="视频通话" title="视频通话">
        <span class="material-icons-outlined" aria-hidden="true">videocam</span>
      </button>
      <button class="action-btn" aria-label="更多选项" title="更多">
        <span class="material-icons-outlined" aria-hidden="true">more_horiz</span>
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  session: Object
})

const getAvatarBgClass = (session) => {
  if (!session) return 'bg-primary'
  if (session.type === 'GROUP') return 'bg-primary'
  const colors = ['bg-blue-500', 'bg-orange-500', 'bg-emerald-500', 'bg-purple-500']
  return colors[(session.id || 0) % colors.length]
}
</script>

<style scoped>
.chat-header {
  height: 64px;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #fff;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-avatar {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #1677ff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
}

.bg-primary { background-color: #1677ff; }
.bg-blue-500 { background-color: #3b82f6; }
.bg-orange-500 { background-color: #f97316; }
.bg-emerald-500 { background-color: #10b981; }
.bg-purple-500 { background-color: #a855f7; }

.header-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.header-name {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  margin: 0;
  line-height: 1.2;
}

.member-count {
  font-size: 10px;
  color: #8c8c8c;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-btn {
  background: transparent;
  border: none;
  padding: 4px;
  color: #8c8c8c;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: color 0.2s;
}

.action-btn:hover {
  color: #1677ff;
}

.action-btn .material-icons-outlined {
  font-size: 20px;
}

/* 暗色模式 */
:deep(.dark) .chat-header {
  background: #1e293b;
  border-color: #334155;
}

:deep(.dark) .header-name {
  color: #f1f5f9;
}

:deep(.dark) .member-count {
  color: #64748b;
}

:deep(.dark) .action-btn {
  color: #64748b;
}

:deep(.dark) .action-btn:hover {
  color: #60a5fa;
}
</style>
