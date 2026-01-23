<template>
  <div class="chat-header">
    <div class="header-left">
      <el-avatar :size="40" :src="session?.avatar" shape="square" class="header-avatar" :class="getAvatarBgClass(session)">
        {{ session?.name?.charAt(0) }}
      </el-avatar>
      <div class="header-info">
        <div class="header-name-wrapper">
          <h2 class="header-name">{{ session?.name }}</h2>
          <span v-if="session?.type === 'GROUP'" class="member-count">{{ session?.memberCount || 0 }}人</span>
        </div>
        <div class="header-status">
          <span class="status-dot" :class="{ online: session?.isOnline }"></span>
          {{ session?.isOnline ? '在线' : '离线' }}
        </div>
      </div>
    </div>
    <div class="header-actions">
      <button class="action-btn" title="语音通话">
        <el-icon><Phone /></el-icon>
      </button>
      <button class="action-btn" title="视频通话">
        <el-icon><VideoCamera /></el-icon>
      </button>
      <button class="action-btn" title="更多">
        <el-icon><MoreFilled /></el-icon>
      </button>
    </div>
  </div>
</template>

<script setup>
import { Phone, VideoCamera, MoreFilled } from '@element-plus/icons-vue'

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
  height: var(--dt-chat-header-height);
  border-bottom: 1px solid var(--dt-border-light);
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
}

.header-avatar {
  border-radius: 8px !important;
  font-weight: bold;
}

.bg-primary { background-color: var(--dt-brand-color); }
.bg-blue-500 { background-color: #3b82f6; }
.bg-orange-500 { background-color: #f97316; }
.bg-emerald-500 { background-color: #10b981; }
.bg-purple-500 { background-color: #a855f7; }

.header-info {
  margin-left: 12px;
}

.header-name-wrapper {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.header-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
  line-height: 1.2;
}

.member-count {
  font-size: 11px;
  color: var(--dt-text-tertiary);
}

.header-status {
  font-size: 11px;
  color: var(--dt-text-tertiary);
  display: flex;
  align-items: center;
  margin-top: 2px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  margin-right: 4px;
  background-color: #cbd5e1;
}

.status-dot.online {
  background-color: #52c41a;
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
  color: var(--dt-text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: color 0.2s;
  
  &:hover {
    color: var(--dt-brand-color);
  }
  
  .el-icon {
    font-size: 20px;
  }
}
</style>
