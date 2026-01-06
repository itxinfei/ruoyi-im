<template>
  <div class="notification-toast">
    <transition-group name="toast" tag="div" class="toast-container">
      <div
        v-for="notification in notifications"
        :key="notification.id"
        class="toast-item"
        :class="notification.type"
      >
        <div class="toast-content">
          <el-icon class="toast-icon">
            <component :is="getIcon(notification.type)" />
          </el-icon>
          <div class="toast-message">
            <div class="toast-title">{{ notification.title }}</div>
            <div v-if="notification.message" class="toast-text">{{ notification.message }}</div>
          </div>
        </div>
        <el-button
          :icon="Close"
          circle
          size="small"
          text
          @click="removeNotification(notification.id)"
        />
      </div>
    </transition-group>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import {
  Close,
  SuccessFilled,
  InfoFilled,
  WarningFilled,
  CircleCloseFilled,
} from '@element-plus/icons-vue'

const store = useStore()

const notifications = ref([])
const notificationId = ref(0)

const getIcon = type => {
  const iconMap = {
    success: SuccessFilled,
    info: InfoFilled,
    warning: WarningFilled,
    error: CircleCloseFilled,
  }
  return iconMap[type] || InfoFilled
}

const addNotification = (title, message, type = 'info', duration = 3000) => {
  const id = ++notificationId.value
  const notification = { id, title, message, type }

  notifications.value.push(notification)

  if (duration > 0) {
    setTimeout(() => {
      removeNotification(id)
    }, duration)
  }

  return id
}

const removeNotification = id => {
  const index = notifications.value.findIndex(n => n.id === id)
  if (index !== -1) {
    notifications.value.splice(index, 1)
  }
}

// 暴露方法给全局使用
window.$notify = {
  success: (title, message, duration) => addNotification(title, message, 'success', duration),
  info: (title, message, duration) => addNotification(title, message, 'info', duration),
  warning: (title, message, duration) => addNotification(title, message, 'warning', duration),
  error: (title, message, duration) => addNotification(title, message, 'error', duration),
}
</script>

<style lang="scss" scoped>
@use '@/styles/dingtalk-theme.scss' as *;

.notification-toast {
  position: fixed;
  top: $spacing-lg;
  right: $spacing-lg;
  z-index: $z-index-tooltip;
  pointer-events: none;

  .toast-container {
    display: flex;
    flex-direction: column;
    gap: $spacing-md;
  }

  .toast-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $spacing-md;
    padding: $spacing-md $spacing-lg;
    background: $bg-white;
    border-radius: $border-radius-lg;
    box-shadow: $shadow-lg;
    pointer-events: auto;
    animation: slideInRight 0.3s $ease-out;
    min-width: 300px;
    max-width: 400px;

    .toast-content {
      display: flex;
      align-items: flex-start;
      gap: $spacing-md;
      flex: 1;

      .toast-icon {
        font-size: 20px;
        flex-shrink: 0;
        margin-top: 2px;
      }

      .toast-message {
        flex: 1;

        .toast-title {
          font-size: $font-size-base;
          font-weight: $font-weight-semibold;
          color: $text-primary;
          margin-bottom: 4px;
        }

        .toast-text {
          font-size: $font-size-sm;
          color: $text-secondary;
          line-height: $line-height-base;
        }
      }
    }

    :deep(.el-button) {
      color: $text-tertiary;
      transition: all $transition-base $ease-base;

      &:hover {
        color: $text-primary;
        background-color: $bg-hover;
      }
    }

    &.success {
      border-left: 4px solid $success-color;

      .toast-icon {
        color: $success-color;
      }
    }

    &.info {
      border-left: 4px solid $info-color;

      .toast-icon {
        color: $info-color;
      }
    }

    &.warning {
      border-left: 4px solid $warning-color;

      .toast-icon {
        color: $warning-color;
      }
    }

    &.error {
      border-left: 4px solid $error-color;

      .toast-icon {
        color: $error-color;
      }
    }
  }
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

.toast-move {
  transition: transform 0.3s ease;
}

@media (max-width: $breakpoint-md) {
  .notification-toast {
    top: $spacing-md;
    right: $spacing-md;
    left: $spacing-md;

    .toast-item {
      min-width: auto;
      max-width: none;
    }
  }
}
</style>
