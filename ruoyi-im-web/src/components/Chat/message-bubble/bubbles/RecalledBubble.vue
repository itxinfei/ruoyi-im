<template>
  <div class="recalled-bubble" :class="{ 'is-own': message.isOwn }">
    <span class="recall-icon material-icons-outlined">history</span>
    <div class="recall-content">
      <span class="recall-text">
        {{ message.isOwn ? '你撤回了一条消息' : `${message.senderName}撤回了一条消息` }}
      </span>
      <span
        v-if="message.isOwn && canReEdit"
        class="recall-reedit"
        @click="$emit('re-edit', { content: originalContent })"
      >
        点击重新编辑
      </span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  message: { type: Object, required: true }
})

const emit = defineEmits(['re-edit'])

const originalContent = computed(() => {
  return props.message.originalContent || ''
})

const canReEdit = computed(() => {
  return props.message.isOwn && originalContent.value
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.recalled-bubble {
  display: flex;
  align-items: center;
  gap: var(--dt-space-2);
  padding: 8px 12px;
  background: var(--dt-bg-body);
  border-radius: var(--dt-radius-md);
  font-size: var(--dt-font-size-sm);
  max-width: 280px;
  animation: fadeIn 0.3s var(--dt-ease-out);

  .recall-icon {
    color: var(--dt-text-tertiary);
    font-size: 16px;
  }

  .recall-content {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  .recall-text {
    color: var(--dt-text-secondary);
  }

  &.is-own {
    background: rgba(255, 59, 48, 0.05);

    .recall-icon {
      color: #ff3b30;
    }

    .recall-text {
      color: var(--dt-text-primary);
    }
  }
}

.recall-reedit {
  color: var(--dt-brand-color);
  cursor: pointer;
  font-size: var(--dt-font-size-xs);
  transition: color var(--dt-transition-fast);

  &:hover {
    color: var(--dt-brand-hover);
    text-decoration: underline;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
