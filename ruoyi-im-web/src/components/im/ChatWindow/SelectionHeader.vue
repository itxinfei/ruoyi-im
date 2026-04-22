<template>
  <div class="selection-header">
    <div class="selection-info">
      <span>已选择 {{ selectedCount }} 条消息</span>
    </div>
    <div class="selection-actions">
      <el-button size="small" @click="$emit('select-all')">
        全选
      </el-button>
      <el-button size="small" @click="$emit('cancel')">
        取消
      </el-button>
      <el-button type="primary" size="small" @click="$emit('forward')">
        转发
      </el-button>
      <el-button size="small" @click="$emit('mark-as-read')">
        已读
      </el-button>
      <el-button size="small" @click="$emit('favorite')">
        收藏
      </el-button>
      <el-button type="danger" size="small" @click="$emit('delete')">
        删除
      </el-button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  selectedCount: {
    type: Number,
    default: 0
  }
})

defineEmits([
  'select-all',
  'cancel',
  'forward',
  'mark-as-read',
  'favorite',
  'delete'
])
</script>

<style scoped>
.selection-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--dt-spacing-md) var(--dt-chat-gutter);
  background-color: var(--dt-brand-color);
  color: var(--dt-text-white);
  flex-shrink: 0;
}

.selection-info {
  font-size: var(--dt-font-size-base);
  font-weight: var(--dt-font-weight-medium);
}

.selection-actions {
  display: flex;
  gap: var(--dt-spacing-sm);
}

.selection-actions :deep(.el-button) {
  padding: var(--dt-spacing-sm) var(--dt-spacing-lg);
  border-radius: var(--dt-radius-md);
  font-size: var(--dt-font-size-sm);
}

.selection-actions :deep(.el-button--small) {
  height: var(--dt-btn-height-sm);
}

.selection-actions :deep(.el-button:not(.el-button--primary):not(.el-button--danger)) {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  color: var(--dt-text-white);
}

.selection-actions :deep(.el-button:not(.el-button--primary):not(.el-button--danger)):hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
}
</style>
