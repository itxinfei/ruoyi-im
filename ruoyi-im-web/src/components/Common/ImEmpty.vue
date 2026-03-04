<template>
  <div class="im-empty" :class="{ 'is-full': full }">
    <div class="im-empty__container">
      <!-- 1. 图标区 -->
      <div class="im-empty__icon-wrapper">
        <slot name="icon">
          <el-icon class="default-icon"><component :is="icon" /></el-icon>
        </slot>
      </div>

      <!-- 2. 文本区 -->
      <div class="im-empty__content">
        <h4 class="im-empty__title">{{ title }}</h4>
        <p v-if="description" class="im-empty__desc">{{ description }}</p>
      </div>

      <!-- 3. 引导动作区 -->
      <div v-if="actionText" class="im-empty__footer">
        <el-button type="primary" plain round @click="$emit('action')">
          {{ actionText }}
        </el-button>
      </div>
      <slot name="extra"></slot>
    </div>
  </div>
</template>

<script setup>
import { Search } from '@element-plus/icons-vue'

defineProps({
  icon: { type: [Object, String], default: Search },
  title: { type: String, default: '暂无数据' },
  description: String,
  actionText: String,
  full: { type: Boolean, default: true }
})

defineEmits(['action'])
</script>

<style scoped lang="scss">
.im-empty {
  display: flex; align-items: center; justify-content: center;
  padding: var(--dt-spacing-2xl); width: 100%;
  
  &.is-full { height: 100%; }

  &__container {
    display: flex; flex-direction: column; align-items: center;
    text-align: center; max-width: 320px;
  }

  &__icon-wrapper {
    margin-bottom: var(--dt-spacing-xl);
    .default-icon {
      font-size: 64px; color: var(--dt-border-dark); opacity: 0.5;
    }
    :deep(img) { width: 120px; height: auto; opacity: 0.8; }
  }

  &__title {
    font-size: 16px; font-weight: 600; color: var(--dt-text-secondary);
    margin: 0 0 var(--dt-spacing-sm) 0;
  }

  &__desc {
    font-size: 13px; color: var(--dt-text-tertiary);
    line-height: 1.6; margin: 0;
  }

  &__footer {
    margin-top: var(--dt-spacing-xl);
    .el-button { padding: 8px 24px; font-weight: 500; }
  }
}
</style>
