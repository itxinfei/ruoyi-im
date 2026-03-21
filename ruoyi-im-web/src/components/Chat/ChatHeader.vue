<template>
  <div class="chat-header">
    <!-- 左侧：头像 + 名称 + 状态 -->
    <div class="header-left">
      <div class="avatar-wrapper" @click="$emit('show-profile')">
        <el-avatar
          shape="square"
          :size="44"
          :src="session?.avatar"
          class="header-avatar"
        >
          {{ session?.name?.charAt(0) }}
        </el-avatar>
        <!-- 在线状态指示器 -->
        <span
          v-if="session?.type !== 'GROUP' && session?.peerOnline"
          class="online-indicator"
        />
      </div>
      <div class="session-info" @click="$emit('toggle-sidebar')">
        <div class="name">
          {{ session?.name }}
        </div>
        <div class="status">
          <!-- 在线状态/成员数 -->
          <template v-if="session?.type !== 'GROUP'">
            <span class="status-dot" :class="session?.peerOnline ? 'online' : 'offline'" />
            <span class="status-text">{{ session?.peerOnline ? '在线' : '离线' }}</span>
          </template>
          <template v-else>
            <span class="status-text">{{ session?.memberCount || 0 }} 人</span>
          </template>

          <!-- 正在输入 -->
          <template v-if="isTyping">
            <span class="status-separator">·</span>
            <span class="typing-indicator">
              <span class="typing-dot" />
              <span class="typing-dot" />
              <span class="typing-dot" />
            </span>
            <span class="typing-text">正在输入...</span>
          </template>
        </div>
      </div>
    </div>

    <!-- 右侧：操作按钮 -->
    <div class="header-right">
      <el-tooltip content="搜索聊天记录" placement="bottom">
        <el-button
          :icon="Search"
          link
          class="header-btn"
          @click="$emit('search', session)"
        />
      </el-tooltip>
      <el-tooltip content="语音通话" placement="bottom">
        <el-button
          :icon="Phone"
          link
          class="header-btn"
          @click="$emit('voice-call', session)"
        />
      </el-tooltip>
      <el-tooltip content="视频通话" placement="bottom">
        <el-button
          :icon="VideoCamera"
          link
          class="header-btn"
          @click="$emit('video-call', session)"
        />
      </el-tooltip>
      <el-dropdown trigger="click" @command="(c) => $emit(c, session)">
        <el-button :icon="MoreFilled" link class="header-btn" />
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="files">
              <el-icon><Folder /></el-icon>
              聊天文件
            </el-dropdown-item>
            <el-dropdown-item command="pin">
              <el-icon><Top /></el-icon>
              {{ session?.isPinned ? '取消置顶' : '置顶会话' }}
            </el-dropdown-item>
            <el-dropdown-item command="clear" divided>
              <el-icon><Delete /></el-icon>
              <span style="color: var(--dt-error-color);">清空聊天记录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Phone, VideoCamera, Search, MoreFilled, Folder, Top, Delete } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  session: Object,
  isTyping: {
    type: Boolean,
    default: false
  }
})
defineEmits(['toggle-sidebar', 'show-profile', 'voice-call', 'video-call', 'search', 'files', 'pin', 'clear'])

// Avatar size from Design Token (44px)
// 已改为使用 CSS 变量，此计算属性已废弃
</script>

<style scoped lang="scss">
.chat-header {
  height: var(--dt-chat-header-height, 60px);
  padding: 0 var(--dt-spacing-lg);
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
  transition: background var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-session-hover);
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-md);
    min-width: 0;

    .avatar-wrapper {
      position: relative;
      flex-shrink: 0;
      cursor: pointer;
      padding: var(--dt-spacing-xs);
      border-radius: var(--dt-radius-md);
      transition: background var(--dt-transition-fast);

      &:hover {
        background: var(--dt-brand-bg);
      }

      .header-avatar {
        border-radius: var(--dt-radius-sm);
        background: var(--dt-brand-bg);
        font-size: var(--dt-font-size-lg);
        font-weight: var(--dt-font-weight-semibold);
        color: var(--dt-brand-color);
      }

      .online-indicator {
        position: absolute;
        bottom: var(--dt-spacing-xxs, 2px);
        right: var(--dt-spacing-xxs, 2px);
        width: var(--dt-status-dot-size, 10px);
        height: var(--dt-status-dot-size, 10px);
        background: var(--dt-success-color);
        border: var(--dt-border-thin, 2px) solid var(--dt-bg-card);
        border-radius: 50%;
      }
    }

    .session-info {
      display: flex;
      flex-direction: column;
      min-width: 0;
      cursor: pointer;
      padding: var(--dt-spacing-xs) var(--dt-spacing-md);
      margin: calc(-1 * var(--dt-spacing-xs)) calc(-1 * var(--dt-spacing-md));
      border-radius: var(--dt-radius-md);
      transition: background var(--dt-transition-fast);

      &:hover {
        background: var(--dt-bg-session-active);
      }

      .name {
        font-size: var(--dt-font-size-lg);
        font-weight: var(--dt-font-weight-medium);
        color: var(--dt-text-primary);
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        max-width: var(--dt-session-name-max-width, 180px);
        line-height: 1.3;
      }

      .status {
        display: flex;
        align-items: center;
        gap: var(--dt-spacing-xs);
        font-size: var(--dt-font-size-sm);
        color: var(--dt-text-tertiary);
        margin-top: var(--dt-spacing-xxs, 2px);

        .status-dot {
          width: var(--dt-status-dot-size-sm, 6px);
          height: var(--dt-status-dot-size-sm, 6px);
          border-radius: 50%;
          &.online { background: var(--dt-success-color); }
          &.offline { background: var(--dt-text-quaternary); }
        }

        .status-separator {
          margin: 0 var(--dt-spacing-xs);
          color: var(--dt-text-quaternary);
        }

        .typing-text {
          color: var(--dt-brand-color);
          font-size: var(--dt-font-size-sm);
        }

        .typing-indicator {
          display: flex;
          align-items: center;
          gap: var(--dt-spacing-xxs, 3px);
          margin-right: var(--dt-spacing-xs, 4px);

          .typing-dot {
            width: var(--dt-typing-dot-size, 4px);
            height: var(--dt-typing-dot-size, 4px);
            background: var(--dt-brand-color);
            border-radius: 50%;
            animation: typing-bounce 1.4s infinite ease-in-out both;

            &:nth-child(1) { animation-delay: -0.32s; }
            &:nth-child(2) { animation-delay: -0.16s; }
            &:nth-child(3) { animation-delay: 0s; }
          }
        }

        .status-dot {
          width: var(--dt-status-dot-size, 10px);
          height: var(--dt-status-dot-size, 10px);
          border-radius: 50%;
          flex-shrink: 0;

          &.online {
            background: var(--dt-success-color);
          }

          &.offline {
            background: var(--dt-text-quaternary);
          }
        }

        .status-text {
          flex-shrink: 0;
        }

        .el-icon {
          font-size: var(--dt-icon-size-xs, 12px);
        }
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 4px;

    .header-btn {
      width: 32px;
      height: 32px;
      padding: 0;
      font-size: 18px;
      color: var(--dt-text-secondary);
      border-radius: var(--dt-radius-md);
      transition: all var(--dt-transition-fast);

      &:hover {
        background: var(--dt-brand-bg);
        color: var(--dt-brand-color);
      }

      &:active {
        background: var(--dt-brand-lighter);
      }
    }

    :deep(.el-dropdown-menu__item) {
      display: flex;
      align-items: center;
      gap: var(--dt-spacing-sm);
      padding: var(--dt-spacing-sm) var(--dt-spacing-md);
      font-size: var(--dt-font-size-base);
      color: var(--dt-text-primary);

      .el-icon {
        font-size: var(--dt-icon-size-md, 16px);
        color: var(--dt-text-secondary);
      }

      &:hover {
        background: var(--dt-bg-session-hover);
      }
    }
  }
}

@keyframes typing-bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}
</style>
