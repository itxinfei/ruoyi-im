<template>
  <div class="empty-state" :class="[variant, { dark: isDark, compact }]">
    <!-- 空状态图标 -->
    <div class="empty-icon" :style="{ color: iconColor }">
      <slot name="icon">
        <!-- 默认使用 Material Icons -->
        <span v-if="type === 'search'" class="material-icons-outlined">search_off</span>
        <span v-else-if="type === 'chat'" class="material-icons-outlined">chat_bubble_outline</span>
        <span v-else-if="type === 'file'" class="material-icons-outlined">folder_open</span>
        <span v-else-if="type === 'user'" class="material-icons-outlined">person_outline</span>
        <span v-else-if="type === 'notification'" class="material-icons-outlined">notifications_none</span>
        <span v-else-if="type === 'network'" class="material-icons-outlined">wifi_off</span>
        <span v-else-if="type === 'error'" class="material-icons-outlined">error_outline</span>
        <span v-else class="material-icons-outlined">inbox</span>
      </slot>
    </div>

    <!-- 空状态标题 -->
    <h3 class="empty-title">
      <slot name="title">{{ title || '暂无数据' }}</slot>
    </h3>

    <!-- 空状态描述 -->
    <p v-if="description || $slots.description" class="empty-description">
      <slot name="description">{{ description }}</slot>
    </p>

    <!-- 操作按钮 -->
    <div v-if="actionText || $slots.action" class="empty-action">
      <slot name="action">
        <el-button v-if="actionText" :type="actionType || 'primary'" :icon="actionIcon" @click="handleAction">
          {{ actionText }}
        </el-button>
      </slot>
    </div>

    <!-- 装饰性插图（可选） -->
    <div v-if="showIllustration" class="empty-illustration">
      <slot name="illustration"></slot>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useTheme } from '@/composables/useTheme'

const props = defineProps({
  type: {
    type: String,
    default: 'default' // search, chat, file, user, notification, network, error, default
  },
  variant: {
    type: String,
    default: 'default' // default, simple, illustration
  },
  title: String,
  description: String,
  actionText: String,
  actionType: {
    type: String,
    default: 'primary'
  },
  actionIcon: String,
  showIllustration: Boolean,
  compact: Boolean,
  iconColor: String
})

const emit = defineEmits(['action'])

const { isDark } = useTheme()

const handleAction = () => {
  emit('action')
}

// 根据类型自动设置图标颜色
const autoIconColor = computed(() => {
  if (props.iconColor) return props.iconColor

  const colors = {
    search: '#94a3b8',
    chat: '#94a3b8',
    file: '#94a3b8',
    user: '#94a3b8',
    notification: '#94a3b8',
    network: '#ef4444',
    error: '#ef4444',
    default: '#94a3b8'
  }
  return colors[props.type] || colors.default
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  text-align: center;
  min-height: 300px;

  // 统一 Material Icons 样式
  .material-icons-outlined {
    display: inline-flex;
    align-items: center;
    justify-content: center;
  }

  // === default 变体 ===
  &.default {
    .empty-icon {
      font-size: 64px;
      opacity: 0.5;
      margin-bottom: 16px;
      color: v-bind('autoIconColor');
    }
  }

  // === simple 变体 ===
  &.simple {
    padding: 32px 16px;
    min-height: 200px;

    .empty-icon {
      font-size: 48px;
      opacity: 0.4;
      margin-bottom: 12px;
    }
  }

  // === illustration 变体 ===
  &.illustration {
    padding: 64px 32px;

    .empty-icon {
      display: none;
    }

    .empty-illustration {
      margin-bottom: 24px;
    }
  }

  // === compact 变体 ===
  &.compact {
    padding: 24px 16px;
    min-height: 160px;

    .empty-icon {
      font-size: 36px;
      opacity: 0.4;
      margin-bottom: 8px;
    }
  }
}

.empty-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0 0 8px 0;
}

.empty-description {
  font-size: 14px;
  color: var(--dt-text-tertiary);
  margin: 0 0 24px 0;
  max-width: 400px;
  line-height: 1.6;
}

.empty-action {
  .el-button {
    min-width: 100px;
  }
}

// 装饰性插图区域
.empty-illustration {
  width: 200px;
  height: 160px;
  margin: 0 auto;

  img,
  svg {
    width: 100%;
    height: 100%;
    object-fit: contain;
  }
}

// 动画效果
.empty-state {
  animation: fadeInUp 0.4s var(--dt-ease-out);
}

.empty-icon {
  animation: scaleIn 0.4s var(--dt-ease-bounce);
}

// 暗色模式
.dark {
  .empty-title {
    color: var(--dt-text-primary-dark);
  }

  .empty-description {
    color: var(--dt-text-tertiary-dark);
  }
}

// 响应式
@media (max-width: 480px) {
  .empty-state {
    padding: 32px 16px;
    min-height: 200px;
  }

  .empty-icon {
    font-size: 48px;
  }

  .empty-title {
    font-size: 14px;
  }

  .empty-description {
    font-size: 13px;
  }
}
</style>
