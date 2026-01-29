<template>
  <div class="edit-preview-container">
    <div class="edit-content-box">
      <span class="edit-label">正在编辑:</span>
      <span class="edit-text">{{ content }}</span>
      <el-icon class="cancel-edit" @click="$emit('cancel')">
        <Close />
      </el-icon>
    </div>
  </div>
</template>

<script setup>
import { Close } from '@element-plus/icons-vue'

const props = defineProps({
  content: {
    type: String,
    default: ''
  }
})

defineEmits(['cancel'])
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.edit-preview-container {
  padding: 10px 12px;
  margin-bottom: 12px;
  border-radius: var(--dt-radius-md);
  background: var(--dt-bg-body);
  border-left: 3px solid var(--dt-success-color);

  .dark & {
    background: var(--dt-bg-hover-dark);
  }
}

.edit-content-box {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;

  .edit-label {
    color: var(--dt-success-color);
    font-weight: 500;
    flex-shrink: 0;
  }

  .edit-text {
    flex: 1;
    color: var(--dt-text-secondary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;

    .dark & { color: var(--dt-text-secondary-dark); }
  }

  .cancel-edit {
    cursor: pointer;
    color: var(--dt-text-tertiary);
    flex-shrink: 0;

    &:hover { color: var(--dt-error-color); }
  }
}
</style>
