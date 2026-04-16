<template>
  <header class="chat-header">
    <div class="header-left" @click="$emit('show-detail')">
      <h3 class="title">
        {{ session?.name || '聊天' }}
      </h3>
      <span v-if="session?.memberCount" class="member-count">({{ session.memberCount }})</span>
    </div>
    <div class="header-right">
      <el-icon title="多选" @click="$emit('toggle-selection')">
        <Operation />
      </el-icon>
      <el-icon title="搜索聊天记录" @click="$emit('open-search')">
        <Search />
      </el-icon>
      <el-icon title="语音通话" @click="$emit('voice-call')">
        <Phone />
      </el-icon>
      <el-icon title="视频通话" @click="$emit('video-call')">
        <VideoCamera />
      </el-icon>
      <el-dropdown trigger="click" @command="$emit('header-command', $event)">
        <el-icon title="更多">
          <MoreFilled />
        </el-icon>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="pin">
              <el-icon><Top /></el-icon>
              置顶会话
            </el-dropdown-item>
            <el-dropdown-item command="mute">
              <el-icon><Bell /></el-icon>
              消息免打扰
            </el-dropdown-item>
            <el-dropdown-item command="clear">
              <el-icon><Delete /></el-icon>
              清空聊天记录
            </el-dropdown-item>
            <el-dropdown-item command="mention">
              <el-icon><User /></el-icon>
              艾特我的消息
            </el-dropdown-item>
            <el-dropdown-item command="detail">
              <el-icon><MoreFilled /></el-icon>
              会话详情
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup>
import { Search, Phone, VideoCamera, MoreFilled, Operation, Top, Bell, Delete, User } from '@element-plus/icons-vue'

const props = defineProps({
  session: Object
})

defineEmits([
  'show-detail',
  'toggle-selection',
  'open-search',
  'voice-call',
  'video-call',
  'header-command'
])
</script>

<style scoped>
.chat-header {
  height: var(--dt-chat-header-height);
  padding: 0 var(--dt-chat-gutter);
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--dt-border-light);
  flex-shrink: 0;
  background: var(--dt-bg-card);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-sm);
  cursor: pointer;
  padding: var(--dt-spacing-sm) var(--dt-spacing-xs);
  border-radius: var(--dt-radius-lg);
  transition: background-color var(--dt-transition-fast);
}

.header-left:hover {
  background: var(--dt-bg-hover);
}

.title {
  font-size: 17px;
  font-weight: 600;
  color: var(--dt-text-primary);
}

.member-count {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
}

.header-right {
  display: flex;
  gap: var(--dt-spacing-xs);
  color: var(--dt-text-tertiary);
}

.header-right .el-icon {
  cursor: pointer;
  font-size: var(--dt-font-size-xl);
  padding: var(--dt-spacing-sm);
  border-radius: var(--dt-radius-lg);
  transition: background-color var(--dt-transition-fast), color var(--dt-transition-fast);
}

.header-right .el-icon:hover {
  background-color: var(--dt-bg-hover);
  color: var(--dt-brand-color);
}

.header-right .el-icon:active {
  background-color: var(--dt-brand-bg);
}

/* 下拉菜单 - 钉钉风格 */
.header-right :deep(.el-dropdown-menu) {
  padding: var(--dt-spacing-xs);
  border-radius: var(--dt-radius-xl);
  border: 1px solid var(--dt-border-light);
  box-shadow: var(--dt-shadow-3);
}

.header-right :deep(.el-dropdown-menu__item) {
  padding: var(--dt-spacing-sm) var(--dt-spacing-md);
  border-radius: var(--dt-radius-lg);
  font-size: var(--dt-font-size-base);
  color: var(--dt-text-primary);
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-sm);
}

.header-right :deep(.el-dropdown-menu__item:hover) {
  background-color: var(--dt-bg-hover);
  color: var(--dt-brand-color);
}

.header-right :deep(.el-dropdown-menu__item .el-icon) {
  font-size: var(--dt-font-size-lg);
  color: var(--dt-text-secondary);
}

.header-right :deep(.el-dropdown-menu__item:hover .el-icon) {
  color: var(--dt-brand-color);
}
</style>
