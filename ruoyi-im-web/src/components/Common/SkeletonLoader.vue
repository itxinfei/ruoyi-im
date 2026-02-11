<template>
  <div
    class="skeleton-loader"
    :class="{ dark: isDark }"
  >
    <!-- 会话项骨架屏 -->
    <div
      v-if="type === 'session'"
      class="skeleton-session"
    >
      <div
        v-for="i in count"
        :key="i"
        class="skeleton-session-item"
      >
        <div class="skeleton-avatar" />
        <div class="skeleton-content">
          <div class="skeleton-title" />
          <div class="skeleton-subtitle" />
        </div>
      </div>
    </div>

    <!-- 消息列表骨架屏 -->
    <div
      v-else-if="type === 'message'"
      class="skeleton-message"
    >
      <div
        v-for="i in count"
        :key="i"
        :class="['skeleton-message-item', i % 2 === 0 ? 'left' : 'right']"
      >
        <div class="skeleton-avatar" />
        <div class="skeleton-bubble">
          <div class="skeleton-line" />
          <div
            v-if="i % 3 !== 0"
            class="skeleton-line short"
          />
        </div>
      </div>
    </div>

    <!-- 列表骨架屏 -->
    <div
      v-else-if="type === 'list'"
      class="skeleton-list"
    >
      <div
        v-for="i in count"
        :key="i"
        class="skeleton-list-item"
      >
        <div class="skeleton-dot" />
        <div class="skeleton-line-long" />
      </div>
    </div>

    <!-- 卡片骨架屏 -->
    <div
      v-else-if="type === 'card'"
      class="skeleton-card"
    >
      <div class="skeleton-card-header">
        <div class="skeleton-title-lg" />
        <div class="skeleton-actions" />
      </div>
      <div class="skeleton-card-body">
        <div class="skeleton-line" />
        <div class="skeleton-line" />
        <div class="skeleton-line short" />
      </div>
    </div>

    <!-- 通用块骨架屏 -->
    <div
      v-else
      class="skeleton-block"
    >
      <div
        v-for="i in count"
        :key="i"
        class="skeleton-block-item"
        :style="{ width: width || '100%', height: height || '20px' }"
      />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useTheme } from '@/composables/useTheme'

const props = defineProps({
  type: {
    type: String,
    default: 'block'
  },
  count: {
    type: Number,
    default: 3
  },
  width: String,
  height: String
})

const { isDark } = useTheme()
</script>

<style scoped lang="scss">
// 骨架屏组件 - 野火IM风格

// 载入动画
@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }

  100% {
    background-position: 200% 0;
  }
}

@keyframes messagePop {
  from {
    opacity: 0;
    transform: translateY(10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 骨架基础样式
@mixin skeleton-base {
  background: linear-gradient(90deg, var(--dt-bg-tertiary) 25%, var(--dt-bg-hover) 50%, var(--dt-bg-tertiary) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

// 基础骨架样式
.skeleton-block-item {
  @include skeleton-base;
  height: 20px;
  border-radius: 4px;
  margin-bottom: 12px;

  &:last-child {
    margin-bottom: 0;
  }
}

// 会话骨架屏
.skeleton-session {
  padding: 16px 0;

  .skeleton-session-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    margin-bottom: 8px;
    border-radius: 8px;
    transition: background 0.2s;

    &:hover {
      background: #f5f5f5;
    }
  }

  .skeleton-avatar {
    width: 48px;
    height: 48px;
    border-radius: 8px;
    flex-shrink: 0;
    @include skeleton-base;
  }

  .skeleton-content {
    flex: 1;
    min-width: 0;
  }

  .skeleton-title {
    width: 60%;
    height: 16px;
    border-radius: 4px;
    @include skeleton-base;
    margin-bottom: 8px;
  }

  .skeleton-subtitle {
    width: 40%;
    height: 12px;
    border-radius: 4px;
    @include skeleton-base;
  }
}

// 消息骨架屏
.skeleton-message {
  padding: 16px;

  .skeleton-message-item {
    display: flex;
    margin-bottom: 20px;
    animation: messagePop 0.3s ease-out both;

    &:nth-child(1) {
      animation-delay: 0s;
    }

    &:nth-child(2) {
      animation-delay: 0.05s;
    }

    &:nth-child(3) {
      animation-delay: 0.1s;
    }

    &:nth-child(4) {
      animation-delay: 0.15s;
    }

    &:nth-child(5) {
      animation-delay: 0.2s;
    }

    &.left {
      justify-content: flex-start;

      .skeleton-avatar {
        margin-right: 12px;
      }

      .skeleton-bubble {
        background: #ffffff;
        border: 1px solid #e0e0e0;
      }
    }

    &.right {
      justify-content: flex-end;

      .skeleton-avatar {
        margin-left: 12px;
      }

      .skeleton-bubble {
        background: var(--dt-brand-light);
        border: 1px solid var(--dt-brand-extra-light);
      }
    }
  }

  .skeleton-avatar {
    width: 40px;
    height: 40px;
    border-radius: 8px;
    flex-shrink: 0;
    @include skeleton-base;
  }

  .skeleton-bubble {
    max-width: 60%;
    padding: 12px 16px;
    border-radius: 8px;

    .skeleton-line {
      height: 14px;
      border-radius: 4px;
      @include skeleton-base;
      margin-bottom: 8px;

      &.short {
        width: 40%;
        margin-bottom: 0;
      }
    }
  }
}

// 列表骨架屏
.skeleton-list {
  padding: 16px;

  .skeleton-list-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }
  }

  .skeleton-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    @include skeleton-base;
  }

  .skeleton-line-long {
    flex: 1;
    height: 14px;
    border-radius: 4px;
    @include skeleton-base;
  }
}

// 卡片骨架屏
.skeleton-card {
  padding: 20px;
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  box-shadow: var(--dt-shadow-sm);

  .skeleton-card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid #f0f0f0;
  }

  .skeleton-title-lg {
    width: 150px;
    height: 20px;
    border-radius: 4px;
    @include skeleton-base;
  }

  .skeleton-actions {
    width: 80px;
    height: 28px;
    border-radius: 8px;
    @include skeleton-base;
  }

  .skeleton-card-body {
    .skeleton-line {
      height: 14px;
      border-radius: 4px;
      @include skeleton-base;
      margin-bottom: 12px;

      &.short {
        width: 30%;
        margin-bottom: 0;
      }
    }
  }
}

// 暗色模式
.dark {
  .skeleton-session-item {
    &:hover {
      background: #2d2d2d;
    }
  }

  .skeleton-message-item {
    &.left .skeleton-bubble {
      background: #2c2c2c;
      border-color: #374151;
    }

    &.right .skeleton-bubble {
      background: var(--dt-brand-bg-dark);
      border-color: var(--dt-brand-extra-light);
    }
  }

  .skeleton-card {
    background: #1e1e1e;
    border-color: #374151;
  }

  .skeleton-card-header {
    border-color: #374151;
  }

  .skeleton-list-item {
    border-color: #374151;
  }

  // 暗色模式骨架动画
  .skeleton-avatar,
  .skeleton-title,
  .skeleton-subtitle,
  .skeleton-line,
  .skeleton-dot,
  .skeleton-line-long,
  .skeleton-title-lg,
  .skeleton-actions,
  .skeleton-block-item {
    background: linear-gradient(90deg, #2d2d2d 25%, #3d3d3d 50%, #2d2d2d 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
  }
}
</style>
