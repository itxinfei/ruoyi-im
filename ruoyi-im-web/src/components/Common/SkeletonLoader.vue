<template>
  <div class="skeleton-loader" :class="{ dark: isDark }">
    <!-- 会话项骨架屏 -->
    <div v-if="type === 'session'" class="skeleton-session">
      <div v-for="i in count" :key="i" class="skeleton-session-item">
        <div class="skeleton-avatar"></div>
        <div class="skeleton-content">
          <div class="skeleton-title"></div>
          <div class="skeleton-subtitle"></div>
        </div>
      </div>
    </div>

    <!-- 消息列表骨架屏 -->
    <div v-else-if="type === 'message'" class="skeleton-message">
      <div v-for="i in count" :key="i" :class="['skeleton-message-item', i % 2 === 0 ? 'left' : 'right']">
        <div class="skeleton-avatar"></div>
        <div class="skeleton-bubble">
          <div class="skeleton-line"></div>
          <div v-if="i % 3 !== 0" class="skeleton-line short"></div>
        </div>
      </div>
    </div>

    <!-- 列表骨架屏 -->
    <div v-else-if="type === 'list'" class="skeleton-list">
      <div v-for="i in count" :key="i" class="skeleton-list-item">
        <div class="skeleton-dot"></div>
        <div class="skeleton-line-long"></div>
      </div>
    </div>

    <!-- 卡片骨架屏 -->
    <div v-else-if="type === 'card'" class="skeleton-card">
      <div class="skeleton-card-header">
        <div class="skeleton-title-lg"></div>
        <div class="skeleton-actions"></div>
      </div>
      <div class="skeleton-card-body">
        <div class="skeleton-line"></div>
        <div class="skeleton-line"></div>
        <div class="skeleton-line short"></div>
      </div>
    </div>

    <!-- 通用块骨架屏 -->
    <div v-else class="skeleton-block">
      <div v-for="i in count" :key="i" class="skeleton-block-item" :style="{ width: width || '100%', height: height || '20px' }"></div>
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
@use '@/styles/design-tokens.scss' as *;

// 基础骨架样式
.skeleton-block-item {
  @include skeleton-loading;
  height: 20px;
  border-radius: var(--dt-radius-sm);
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
    border-radius: var(--dt-radius-md);
    transition: background var(--dt-transition-base);

    &:hover {
      background: var(--dt-bg-hover);
    }
  }

  .skeleton-avatar {
    width: 48px;
    height: 48px;
    border-radius: var(--dt-radius-md);
    flex-shrink: 0;
    @include skeleton-loading;
  }

  .skeleton-content {
    flex: 1;
    min-width: 0;
  }

  .skeleton-title {
    width: 60%;
    height: 16px;
    border-radius: var(--dt-radius-sm);
    @include skeleton-loading;
    margin-bottom: 8px;
  }

  .skeleton-subtitle {
    width: 40%;
    height: 12px;
    border-radius: var(--dt-radius-sm);
    @include skeleton-loading;
  }
}

// 消息骨架屏
.skeleton-message {
  padding: 16px;

  .skeleton-message-item {
    display: flex;
    margin-bottom: 20px;
    animation: messagePop 0.3s var(--dt-ease-out) both;

    &:nth-child(1) { animation-delay: 0s; }
    &:nth-child(2) { animation-delay: 0.05s; }
    &:nth-child(3) { animation-delay: 0.1s; }
    &:nth-child(4) { animation-delay: 0.15s; }
    &:nth-child(5) { animation-delay: 0.2s; }

    &.left {
      justify-content: flex-start;

      .skeleton-avatar {
        margin-right: 12px;
      }

      .skeleton-bubble {
        background: var(--dt-bubble-left-bg);
      }
    }

    &.right {
      justify-content: flex-end;

      .skeleton-avatar {
        margin-left: 12px;
      }

      .skeleton-bubble {
        background: var(--dt-bubble-right-bg);
      }
    }
  }

  .skeleton-avatar {
    width: 40px;
    height: 40px;
    border-radius: var(--dt-radius-md);
    flex-shrink: 0;
    @include skeleton-loading;
  }

  .skeleton-bubble {
    max-width: 60%;
    padding: 12px 16px;
    border-radius: var(--dt-radius-md);

    .skeleton-line {
      height: 14px;
      border-radius: var(--dt-radius-sm);
      @include skeleton-loading;
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
    border-bottom: 1px solid var(--dt-border-lighter);

    &:last-child {
      border-bottom: none;
    }
  }

  .skeleton-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    @include skeleton-loading;
  }

  .skeleton-line-long {
    flex: 1;
    height: 14px;
    border-radius: var(--dt-radius-sm);
    @include skeleton-loading;
  }
}

// 卡片骨架屏
.skeleton-card {
  padding: 20px;
  border-radius: var(--dt-radius-lg);
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  @include card-style;

  .skeleton-card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid var(--dt-border-lighter);
  }

  .skeleton-title-lg {
    width: 150px;
    height: 20px;
    border-radius: var(--dt-radius-sm);
    @include skeleton-loading;
  }

  .skeleton-actions {
    width: 80px;
    height: 28px;
    border-radius: var(--dt-radius-md);
    @include skeleton-loading;
  }

  .skeleton-card-body {
    .skeleton-line {
      height: 14px;
      border-radius: var(--dt-radius-sm);
      @include skeleton-loading;
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
      background: var(--dt-bg-hover-dark);
    }
  }

  .skeleton-card {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  .skeleton-card-header {
    border-color: var(--dt-border-light-dark);
  }

  .skeleton-list-item {
    border-color: var(--dt-border-dark);
  }
}
</style>
