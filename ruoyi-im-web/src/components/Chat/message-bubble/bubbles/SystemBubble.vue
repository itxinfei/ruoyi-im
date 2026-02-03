<template>
  <div class="system-bubble">
    <span class="system-text">{{ message.content }}</span>
  </div>
</template>

<script setup>
defineProps({
  message: { type: Object, required: true }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.system-bubble {
  display: flex;
  justify-content: center;
  width: 100%;
  margin: var(--dt-space-2) 0;
  animation: fadeIn 0.3s var(--dt-ease-out);
}

.system-text {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
  padding: 4px 12px;
  background: var(--dt-bg-body);
  border-radius: var(--dt-radius-full);
}

// 使用全局 fadeIn 动画 (@/styles/animations.scss)
</style>
