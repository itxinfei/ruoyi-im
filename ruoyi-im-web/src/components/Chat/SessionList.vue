<template>
  <div class="session-list">
    <div ref="sessionItemsContainer" class="session-items">
      <VirtualScrollList
        :data="sessions"
        :item-height="64"
        :height="'100%'"
        :buffer-count="10"
        key-prop="id"
        class="virtual-session-list"
      >
        <template #item="{ item: session, index }">
          <div
            class="session-item"
            :class="{
              active: activeSessionId === session.id,
              collapsed: isCollapsed,
              pinned: session.pinned,
              muted: session.muted,
            }"
            :data-index="index"
            @click="onSelect(session)"
            @mouseenter="handleSessionHover(session.id)"
            @mouseleave="handleSessionLeave(session.id)"
          >
            <div class="session-avatar">
              <el-badge :is-dot="getPeerOnline(session)" class="status-badge">
                <el-avatar :src="session.avatar" :size="isCollapsed ? 36 : 40"></el-avatar>
              </el-badge>
              <transition name="online-indicator">
                <div v-if="getPeerOnline(session)" class="online-indicator"></div>
              </transition>
            </div>
            <div class="session-info" :class="{ hidden: isCollapsed }">
              <div class="session-name-row">
                <span class="session-name">{{ session.name }}</span>
                <transition name="pin-icon">
                  <i v-if="session.pinned" class="el-icon-thumb pin-icon"></i>
                </transition>
                <transition name="mute-icon">
                  <i v-if="session.muted" class="el-icon-bell mute-icon"></i>
                </transition>
              </div>
              <div class="last-message">{{ getLastMessageText(session.lastMessage) }}</div>
            </div>
            <div class="session-meta" :class="{ hidden: isCollapsed }">
              <div class="timestamp">{{ formatTime(session.lastMessage?.timestamp) }}</div>
              <transition name="badge-pop">
                <el-badge
                  v-if="session.unreadCount > 0"
                  :value="formatUnreadCount(session.unreadCount)"
                  class="unread-badge"
                  type="primary"
                ></el-badge>
              </transition>
            </div>
            <div class="session-actions" :class="{ hidden: isCollapsed }" @click.stop>
              <el-dropdown trigger="click" @command="cmd => handleSessionAction(cmd, session.id)">
                <span class="el-dropdown-link">
                  <i class="el-icon-more"></i>
                </span>
                <el-dropdown-menu>
                  <el-dropdown-item command="pin">
                    <i :class="session.pinned ? 'el-icon-bottom' : 'el-icon-top'"></i>
                    {{ session.pinned ? '取消置顶' : '置顶' }}
                  </el-dropdown-item>
                  <el-dropdown-item command="mute">
                    <i :class="session.muted ? 'el-icon-bell' : 'el-icon-close-notification'"></i>
                    {{ session.muted ? '取消静音' : '静音' }}
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <i class="el-icon-delete"></i>
                    删除会话
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
          </div>
        </template>
      </VirtualScrollList>

      <transition name="empty-state">
        <div v-if="sessions.length === 0" class="empty-state">
          <div class="empty-icon">
            <i class="el-icon-chat-line-round"></i>
          </div>
          <div class="empty-text">暂无会话</div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { formatTime } from '@/utils/format/time'
import { getLastMessageText } from '@/utils/message'

// Props 定义
const props = defineProps({
  sessionsProp: { type: Array, default: null },
  currentSessionIdProp: { type: [String, Number], default: null },
  isCollapsed: { type: Boolean, default: false },
})

// Emits 定义
const emit = defineEmits(['select'])

// Store
const store = useStore()

// 响应式数据
const hoveredSessionId = ref(null)
const sessionItemsContainer = ref(null)

// 计算属性
const sessions = computed(() => {
  return props.sessionsProp || store?.state?.im?.sessions || []
})

const storeCurrentSessionId = computed(() => {
  return store?.state?.im?.currentSession?.id || null
})

const activeSessionId = computed(() => {
  return props.currentSessionIdProp ?? storeCurrentSessionId.value
})

const filteredSessions = computed(() => {
  const list = Array.isArray(sessions.value) ? [...sessions.value] : []
  return list.sort((a, b) => {
    if (a.pinned && !b.pinned) return -1
    if (!a.pinned && b.pinned) return 1
    const ta = a.lastMessage?.timestamp || 0
    const tb = b.lastMessage?.timestamp || 0
    return tb - ta
  })
})

// 方法
const onSelect = session => {
  emit('select', session)
  if (!props.sessionsProp && store?.dispatch) {
    store.dispatch('im/switchSession', session)
  }
}

const getPeerOnline = session => {
  return session.type !== 'group' && session.online === true
}

const formatUnreadCount = count => {
  return count > 99 ? '99+' : count
}

const handleSessionHover = sessionId => {
  hoveredSessionId.value = sessionId
}

const handleSessionLeave = sessionId => {
  if (hoveredSessionId.value === sessionId) {
    hoveredSessionId.value = null
  }
}

const handleSessionAction = (command, sessionId) => {
  switch (command) {
    case 'pin':
      togglePin(sessionId)
      break
    case 'mute':
      toggleMute(sessionId)
      break
    case 'delete':
      deleteSession(sessionId)
      break
  }
}

const togglePin = sessionId => {
  store?.dispatch('im/toggleSessionPin', sessionId)
}

const toggleMute = sessionId => {
  store?.dispatch('im/toggleSessionMute', sessionId)
}

const deleteSession = sessionId => {
  store?.dispatch('im/deleteSession', sessionId)
}
</script>

<style scoped lang="scss">
@use '@/styles/dingtalk-theme.scss' as *;

.session-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: $bg-white;

  .session-items {
    flex: 1;
    overflow-y: auto;
    position: relative;
    @include custom-scrollbar(6px, $border-dark);

    .session-list-inner {
      min-height: 100%;
    }

    .session-item {
      display: flex;
      align-items: center;
      padding: $spacing-md $spacing-md;
      cursor: pointer;
      position: relative;
      transition: all $transition-base $ease-out;
      border-left: 3px solid transparent;
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        width: 100%;
        height: 100%;
        background: linear-gradient(90deg, transparent 0%, rgba($primary-color, 0.02) 100%);
        opacity: 0;
        transition: opacity $transition-base $ease-base;
        pointer-events: none;
      }

      &:hover {
        background-color: $bg-hover;
        transform: translateX(2px);

        &::before {
          opacity: 1;
        }
      }

      &.active {
        background-color: $primary-color-light;
        border-left-color: $primary-color;

        &::before {
          background: linear-gradient(90deg, rgba($primary-color, 0.08) 0%, transparent 100%);
          opacity: 1;
        }

        &:hover {
          background-color: $primary-color-light;
          transform: translateX(0);
        }

        .session-name {
          color: $primary-color;
        }

        .last-message {
          color: $text-secondary;
        }
      }

      &.pinned {
        .session-name-row {
          .pin-icon {
            color: $warning-color;
          }
        }
      }

      &.muted {
        .session-name-row {
          .mute-icon {
            color: $text-tertiary;
          }
        }

        .last-message {
          opacity: 0.6;
        }
      }

      &.collapsed {
        justify-content: center;
        padding: $spacing-md;

        .session-avatar {
          margin-right: 0;
        }
      }

      .session-avatar {
        position: relative;
        margin-right: $spacing-md;
        transition: margin-right $transition-base $ease-base;
        flex-shrink: 0;

        .status-badge {
          :deep(.el-badge__content) {
            border: 2px solid $bg-white;
            box-shadow: $shadow-sm;
            transition: all $transition-base $ease-base;
          }
        }

        .online-indicator {
          position: absolute;
          bottom: 2px;
          right: 2px;
          width: 10px;
          height: 10px;
          background: $success-color;
          border: 2px solid $bg-white;
          border-radius: $border-radius-round;
          box-shadow: 0 2px 4px rgba($success-color, 0.3);
          animation: online-pulse 2s infinite;
        }

        :deep(.el-avatar) {
          transition: all $transition-base $ease-base;
          border: 2px solid transparent;

          &:hover {
            border-color: $primary-color;
            transform: scale(1.05);
          }
        }
      }

      .session-info {
        flex: 1;
        min-width: 0;
        transition: all $transition-base $ease-base;

        &.hidden {
          display: none;
        }

        .session-name-row {
          display: flex;
          align-items: center;
          gap: $spacing-xs;
          margin-bottom: 4px;

          .session-name {
            font-size: 14px;
            font-weight: 500;
            color: $text-primary;
            @include text-ellipsis;
            line-height: 1.4;
            transition: color $transition-base $ease-base;
          }

          .pin-icon,
          .mute-icon {
            font-size: 12px;
            flex-shrink: 0;
            transition: all $transition-base $ease-base;
          }

          .pin-icon {
            animation: pin-bounce 0.5s ease;
          }
        }

        .last-message {
          font-size: 12px;
          color: $text-tertiary;
          @include text-ellipsis;
          line-height: 1.4;
          transition: color $transition-base $ease-base;
        }
      }

      .session-meta {
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        margin-left: $spacing-sm;
        transition: all $transition-base $ease-base;

        &.hidden {
          display: none;
        }

        .timestamp {
          font-size: 11px;
          color: $text-placeholder;
          margin-bottom: 4px;
          font-weight: 400;
          transition: color $transition-base $ease-base;
        }

        .unread-badge {
          :deep(.el-badge__content) {
            background: linear-gradient(135deg, $error-color 0%, #ff7875 100%);
            border: none;
            font-weight: 500;
            min-width: 18px;
            height: 18px;
            line-height: 18px;
            padding: 0 5px;
            box-shadow: 0 2px 6px rgba($error-color, 0.3);
            transition: all $transition-base $ease-base;
            animation: badge-pop 0.3s ease;
          }
        }
      }

      .session-actions {
        margin-left: $spacing-sm;
        opacity: 0;
        transform: translateX(10px);
        transition: all $transition-base $ease-base;

        &.hidden {
          display: none;
        }

        .el-dropdown-link {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 28px;
          height: 28px;
          border-radius: $border-radius-sm;
          color: $text-tertiary;
          transition: all $transition-base $ease-base;
          background: transparent;

          &:hover {
            background-color: $bg-hover;
            color: $text-secondary;
            transform: scale(1.1);
          }

          &:active {
            transform: scale(0.95);
          }
        }
      }

      &:hover {
        .session-actions {
          opacity: 1;
          transform: translateX(0);
        }

        .timestamp {
          color: $text-tertiary;
        }
      }
    }

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 60px $spacing-xl;
      color: $text-tertiary;

      .empty-icon {
        font-size: 64px;
        color: $border-dark;
        margin-bottom: $spacing-lg;
        animation: empty-float 3s ease-in-out infinite;
      }

      .empty-text {
        font-size: 14px;
        color: $text-secondary;
      }
    }
  }
}

@keyframes online-pulse {
  0%,
  100% {
    box-shadow: 0 2px 4px rgba($success-color, 0.3);
  }
  50% {
    box-shadow: 0 2px 8px rgba($success-color, 0.6);
  }
}

@keyframes pin-bounce {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes badge-pop {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes empty-float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.session-list-enter-active,
.session-list-leave-active {
  transition: all 0.3s ease;
}

.session-list-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.session-list-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

.session-list-move {
  transition: transform 0.3s ease;
}

.online-indicator-enter-active {
  transition: all 0.3s ease;
}

.online-indicator-enter-from {
  opacity: 0;
  transform: scale(0);
}

.pin-icon-enter-active,
.mute-icon-enter-active {
  transition: all 0.3s ease;
}

.pin-icon-enter-from,
.mute-icon-enter-from {
  opacity: 0;
  transform: scale(0);
}

.badge-pop-enter-active {
  transition: all 0.3s ease;
}

.badge-pop-enter-from {
  opacity: 0;
  transform: scale(0);
}

.actions-slide-enter-active,
.actions-slide-leave-active {
  transition: all 0.3s ease;
}

.actions-slide-enter-from,
.actions-slide-leave-to {
  opacity: 0;
  transform: translateX(10px);
}

.empty-state-enter-active,
.empty-state-leave-active {
  transition: all 0.3s ease;
}

.empty-state-enter-from,
.empty-state-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

@media (max-width: $breakpoint-md) {
  .session-list {
    .session-items {
      .session-item {
        padding: $spacing-md $spacing-sm;

        .session-avatar {
          margin-right: $spacing-md;

          :deep(.el-avatar) {
            width: 36px !important;
            height: 36px !important;
          }
        }

        .session-info {
          .session-name-row {
            .session-name {
              font-size: 13px;
            }
          }

          .last-message {
            font-size: 11px;
          }
        }

        .session-meta {
          margin-left: $spacing-sm;

          .timestamp {
            font-size: 10px;
          }
        }

        .session-actions {
          .el-dropdown-link {
            width: 24px;
            height: 24px;
          }
        }
      }
    }
  }
}

@media (prefers-reduced-motion: reduce) {
  .session-list {
    .session-item {
      transition: none;

      &:hover {
        transform: none;
      }
    }

    .session-avatar {
      :deep(.el-avatar) {
        transition: none;
      }
    }

    .online-indicator {
      animation: none;
    }

    .pin-icon {
      animation: none;
    }

    :deep(.unread-badge .el-badge__content) {
      animation: none;
    }

    .empty-icon {
      animation: none;
    }
  }

  .session-list-enter-active,
  .session-list-leave-active,
  .search-hint-enter-active,
  .search-hint-leave-active,
  .online-indicator-enter-active,
  .pin-icon-enter-active,
  .mute-icon-enter-active,
  .badge-pop-enter-active,
  .actions-slide-enter-active,
  .actions-slide-leave-active,
  .empty-state-enter-active,
  .empty-state-leave-active {
    transition: none;
  }
}
</style>
