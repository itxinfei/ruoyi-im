<template>
  <header class="chat-window-header">
    <div class="header-left">
      <div class="channel-info" @click="$emit('show-detail')">
        <DingtalkAvatar :src="session?.avatar" :name="session?.name" :size="32" shape="square" />
        <div class="channel-text">
          <span class="channel-prefix" v-if="session?.type === 'GROUP'">#</span>
          <span class="channel-name">{{ session?.name || '频道' }}</span>
          <el-icon class="arrow-down"><ArrowDown /></el-icon>
        </div>
      </div>
      <div class="channel-meta" v-if="session?.type === 'GROUP'">
        <span class="member-count">{{ session?.memberCount || 0 }}人</span>
      </div>
    </div>

    <div class="header-right">
      <button class="header-tool-btn" title="发起通话" @click="$emit('voice-call')">
        <el-icon><Phone /></el-icon>
      </button>
      <button class="header-tool-btn" title="视频会议" @click="$emit('video-call')">
        <el-icon><VideoCamera /></el-icon>
      </button>
      <div class="header-divider"></div>
      <button 
        class="header-tool-btn" 
        :class="{ active: isSidebarOpen }"
        title="隐藏/显示详细信息" 
        @click="$emit('toggle-sidebar')"
      >
        <el-icon><InfoFilled /></el-icon>
      </button>
    </div>
  </header>
</template>

<script setup lang="js">
import { Phone, VideoCamera, InfoFilled, ArrowDown } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

defineProps({
  session: Object,
  isSidebarOpen: Boolean
})

defineEmits(['show-detail', 'toggle-sidebar', 'voice-call', 'video-call'])
</script>

<style scoped lang="scss">
// ============================================================================
// 聊天窗口头部 - 钉钉 8.2 规范
// 高度: 56px, 背景: 白色, 底部边框: 1px
// ============================================================================

.chat-window-header {
  height: var(--dt-chat-header-height, 56px);
  padding: 0 var(--dt-chat-gutter);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);
  flex-shrink: 0;
  z-index: var(--dt-z-sticky);
}

// ============================================================================
// 左侧：频道信息
// ============================================================================

.header-left {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-md);
}

.channel-info {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-sm);
  padding: 4px var(--dt-spacing-sm);
  margin-left: calc(-1 * var(--dt-spacing-sm));
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: background var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-hover);
  }
}

.channel-text {
  display: flex;
  align-items: center;
  gap: 4px;
}

.channel-prefix {
  font-size: 16px;
  font-weight: 700;
  color: var(--dt-text-tertiary);
}

// 钉钉规范：标题 16px，字重 600
.channel-name {
  font-size: var(--dt-font-size-lg);
  font-weight: var(--dt-font-weight-semibold);
  color: var(--dt-text-primary);
}

.arrow-down {
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.channel-meta {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-sm);
}

.member-count {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
  padding: 2px 8px;
  background: var(--dt-bg-hover);
  border-radius: var(--dt-radius-sm);
}

// ============================================================================
// 右侧：工具按钮
// ============================================================================

.header-right {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-xs);
}

.header-divider {
  width: 1px;
  height: 16px;
  background: var(--dt-border-light);
  margin: 0 var(--dt-spacing-sm);
}

// 钉钉规范：工具按钮 32x32px
.header-tool-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: var(--dt-text-secondary);
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-hover);
    color: var(--dt-text-primary);
  }

  &.active {
    background: var(--dt-border-light);
    color: var(--dt-text-primary);
  }
}
</style>
