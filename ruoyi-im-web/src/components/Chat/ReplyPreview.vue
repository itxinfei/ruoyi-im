<template>
  <div class="reply-preview-container">
    <div class="reply-content-box">
      <span class="reply-user">{{ senderName }}:</span>
      <span class="reply-text">{{ content }}</span>
      <el-icon class="cancel-reply" @click="$emit('cancel')">
        <Close />
      </el-icon>
    </div>
  </div>
</template>

<script setup>
import { Close } from '@element-plus/icons-vue'

const props = defineProps({
  senderName: {
    type: String,
    default: ''
  },
  content: {
    type: String,
    default: ''
  }
})

defineEmits(['cancel'])
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.reply-preview-container {
  padding: 10px 12px;
  margin-bottom: 12px;
  border-radius: var(--dt-radius-md);
  background: var(--dt-bg-body);
  border-left: 3px solid var(--dt-brand-color);

  .dark & {
    background: var(--dt-bg-hover-dark);
  }
}

.reply-content-box {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;

  .reply-user {
    color: var(--dt-brand-color);
    font-weight: 500;
    flex-shrink: 0;
  }

  .reply-text {
    flex: 1;
    color: var(--dt-text-secondary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;

    .dark & { color: var(--dt-text-secondary-dark); }
  }

  .cancel-reply {
    cursor: pointer;
    color: var(--dt-text-tertiary);
    flex-shrink: 0;

    &:hover { color: var(--dt-error-color); }
  }
}
</style>
