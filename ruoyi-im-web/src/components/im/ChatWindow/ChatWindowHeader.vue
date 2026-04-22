<template>
  <header class="slack-chat-header">
    <div class="header-left">
      <div class="channel-info" @click="$emit('show-detail')">
        <span class="channel-prefix" v-if="session?.type === 'GROUP'">#</span>
        <DingtalkAvatar v-else :src="session?.avatar" :name="session?.name" :size="24" shape="square" class="user-avatar" />
        <h3 class="channel-name">{{ session?.name || '频道' }}</h3>
        <el-icon class="arrow-down"><ArrowDown /></el-icon>
      </div>
      <div class="channel-meta" v-if="session?.type === 'GROUP'">
        <div class="member-stack">
          <!-- 模拟头像堆叠 -->
          <div class="stack-avatar"></div>
          <div class="stack-avatar"></div>
          <div class="stack-avatar"></div>
          <span class="count">{{ session?.memberCount || 0 }}</span>
        </div>
      </div>
    </div>

    <div class="header-right">
      <!-- 搜索栏嵌在头部 (类似 Slack 顶部搜索) -->
      <button class="s-tool-btn" title="发起通话" @click="$emit('voice-call')"><el-icon><Phone /></el-icon></button>
      <button class="s-tool-btn" title="视频会议" @click="$emit('video-call')"><el-icon><VideoCamera /></el-icon></button>
      <div class="s-divider"></div>
      <button 
        class="s-tool-btn details-toggle" 
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
.slack-chat-header {
  height: var(--dt-header-height, 56px);
  padding: 0 var(--dt-spacing-lg) 0 var(--dt-spacing-xl);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);
  flex-shrink: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-md);

  .channel-info {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 4px var(--dt-spacing-sm);
    margin-left: -8px;
    border-radius: var(--dt-radius-md);
    cursor: pointer;
    transition: background 0.15s;

    &:hover { background: var(--dt-bg-hover); }

    .channel-prefix { font-size: 18px; font-weight: 800; color: var(--dt-text-tertiary); }
    .user-avatar { border-radius: var(--dt-radius-sm); }
    .channel-name { font-size: 16px; font-weight: 800; color: var(--dt-text-primary); }
    .arrow-down { font-size: 12px; color: var(--dt-text-tertiary); }
  }

  .channel-meta {
    .member-stack {
      display: flex; align-items: center;
      .stack-avatar { width: 24px; height: 24px; border-radius: var(--dt-radius-sm); background: var(--dt-border-light); border: 2px solid var(--dt-bg-card); margin-left: -8px; &:first-child { margin-left: 0; } }
      .count { font-size: 13px; font-weight: 600; color: var(--dt-text-secondary); margin-left: 6px; }
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-xs);

  .s-divider { width: 1px; height: 16px; background: var(--dt-border-light); margin: 0 var(--dt-spacing-sm); }

  .s-tool-btn {
    width: 32px; height: 32px; border: none; background: transparent;
    color: var(--dt-text-secondary); border-radius: var(--dt-radius-md); cursor: pointer;
    @include flex-center; font-size: 18px; transition: var(--dt-transition-fast);

    &:hover { background: var(--dt-bg-hover); color: var(--dt-text-primary); }
    &.active { background: var(--dt-border-light); color: var(--dt-text-primary); }
  }
}
</style>
