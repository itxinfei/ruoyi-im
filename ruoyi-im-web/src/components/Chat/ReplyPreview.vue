<template>
  <div class="reply-preview-container">
    <div class="reply-content-box">
      <div class="reply-icon">
        <span class="material-icons-outlined">reply</span>
      </div>
      <div class="reply-info">
        <span class="reply-user">{{ senderName }}</span>
        <span class="reply-text">{{ content }}</span>
      </div>
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
  padding: 8px 12px;
  margin-bottom: 8px;
  border-radius: var(--dt-radius-sm);
  background: var(--dt-bg-tertiary);
  border-left: 3px solid var(--dt-brand-color);

  .dark & {
    background: rgba(0, 137, 255, 0.08);
    border-left-color: var(--dt-brand-color);
  }
}

.reply-content-box {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;

  .reply-icon {
    flex-shrink: 0;
    width: 20px;
    height: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--dt-brand-color);

    .material-icons-outlined {
      font-size: 16px;
    }
  }

  .reply-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
    flex: 1;
    min-width: 0;
  }

  .reply-user {
    color: var(--dt-text-primary);
    font-weight: 500;
    font-size: 12px;
    flex-shrink: 0;

    .dark & { color: #e2e8f0; }
  }

  .reply-text {
    color: var(--dt-text-secondary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 13px;

    .dark & { color: var(--dt-text-secondary-dark); }
  }

  .cancel-reply {
    cursor: pointer;
    color: var(--dt-text-tertiary);
    flex-shrink: 0;
    padding: 4px;
    border-radius: 4px;
    transition: all 0.2s;

    &:hover {
      color: var(--dt-error-color);
      background: var(--dt-bg-hover);
    }

    .dark &:hover {
      background: rgba(255, 255, 255, 0.1);
    }
  }
}
</style>
