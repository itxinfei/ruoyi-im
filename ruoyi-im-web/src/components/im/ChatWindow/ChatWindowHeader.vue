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
  height: 52px; // Slack 标准高度
  padding: 0 16px 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  border-bottom: 1px solid #e3e5e8;
  flex-shrink: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;

  .channel-info {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 4px 8px;
    margin-left: -8px;
    border-radius: 6px;
    cursor: pointer;
    transition: background 0.15s;

    &:hover { background: #f8f9fa; }

    .channel-prefix { font-size: 18px; font-weight: 800; color: #86868b; }
    .user-avatar { border-radius: 4px; }
    .channel-name { font-size: 16px; font-weight: 800; color: #1d1d1f; }
    .arrow-down { font-size: 12px; color: #86868b; }
  }

  .channel-meta {
    .member-stack {
      display: flex; align-items: center;
      .stack-avatar { width: 24px; height: 24px; border-radius: 4px; background: #e3e5e8; border: 2px solid #fff; margin-left: -8px; &:first-child { margin-left: 0; } }
      .count { font-size: 13px; font-weight: 600; color: #5f6368; margin-left: 6px; }
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 4px;

  .s-divider { width: 1px; height: 16px; background: #e3e5e8; margin: 0 8px; }

  .s-tool-btn {
    width: 32px; height: 32px; border: none; background: transparent;
    color: #5f6368; border-radius: 6px; cursor: pointer;
    @include flex-center; font-size: 18px; transition: 0.15s;

    &:hover { background: #f8f9fa; color: #1d1d1f; }
    &.active { background: #e3e5e8; color: #1d1d1f; }
  }
}
</style>
