<template>
  <div class="selection-action-bar">
    <div class="selection-info">
      <span class="count-tag">{{ selectedCount }}</span>
      <span class="label">项已选择</span>
    </div>
    
    <div class="selection-actions">
      <button class="action-btn" @click="$emit('select-all')">
        <el-icon><Check /></el-icon>
        <span>全选</span>
      </button>
      
      <button class="action-btn" @click="$emit('forward')">
        <el-icon><Position /></el-icon>
        <span>转发</span>
      </button>
      
      <button class="action-btn" @click="$emit('favorite')">
        <el-icon><Star /></el-icon>
        <span>收藏</span>
      </button>
      
      <button class="action-btn is-danger" @click="$emit('delete')">
        <el-icon><Delete /></el-icon>
        <span>删除</span>
      </button>
      
      <div class="divider"></div>
      
      <button class="action-btn is-close" @click="$emit('cancel')">
        <el-icon><Close /></el-icon>
        <span>退出多选</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { Check, Position, Star, Delete, Close } from '@element-plus/icons-vue'

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
  'favorite',
  'delete'
])
</script>

<style scoped lang="scss">
.selection-action-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 24px;
  background-color: var(--dt-bg-card);
  border-top: 1px solid var(--dt-border-light);
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  transition: opacity 0.2s;
}

.selection-info {
  display: flex;
  align-items: center;
  gap: 6px;
  
  .count-tag {
    background: var(--dt-brand-color);
    color: var(--dt-text-white);
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 14px;
    font-weight: 600;
  }
  
  .label {
    font-size: 14px;
    color: var(--dt-text-secondary);
  }
}

.selection-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 32px;
  padding: 0 12px;
  border: 1px solid var(--dt-border-light);
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-md);
  font-size: 13px;
  color: var(--dt-text-primary);
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:hover {
    border-color: var(--dt-brand-color);
    color: var(--dt-brand-color);
    background: var(--dt-brand-bg);
  }

  &.is-danger:hover {
    border-color: var(--dt-error-color);
    color: var(--dt-error-color);
    background: var(--dt-error-bg);
  }
  
  &.is-close {
    border-color: transparent;
    background: var(--dt-bg-hover);
    
    &:hover {
      background: var(--dt-bg-active);
    }
  }

  .el-icon {
    font-size: 14px;
  }
}

.divider {
  width: 1px;
  height: 20px;
  background: var(--dt-border-lighter);
  margin: 0 8px;
}
</style>
