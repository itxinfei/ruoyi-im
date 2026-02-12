<template>
  <div
    class="recalled-bubble"
    :class="{ 'is-right': message.isOwn }"
  >
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
  justify-content: center;
  gap: var(--dt-space-1);
  padding: 4px 12px;
  background: transparent;
  font-size: 12px;
  color: var(--dt-text-quaternary);
  animation: fadeIn 0.3s var(--dt-ease-out);

  .recall-icon {
    display: none;
  }

  .recall-content {
    display: inline-flex;
    flex-direction: row;
    align-items: center;
    gap: 8px;
  }

  .recall-text {
    color: inherit;
  }

  &.is-right {
    background: transparent;
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

</style>
