<template>
  <div class="message-multi-select-bar" :class="{ visible: visible || selectedCount > 0 }">
    <!-- 多选状态显示 -->
    <div class="select-info">
      <el-checkbox
        :model-value="isAllSelected"
        :indeterminate="isIndeterminate"
        @change="handleSelectAll"
        class="select-all-checkbox"
      >
        <span class="select-count">已选 {{ selectedCount }} 条</span>
      </el-checkbox>
    </div>

    <!-- 批量操作按钮 -->
    <div class="batch-actions">
      <!-- 转发 -->
      <el-tooltip content="转发 (F)" placement="top">
        <el-button
          type="text"
          :disabled="selectedCount === 0"
          @click="handleForward"
          class="action-btn"
        >
          <i class="el-icon-share"></i>
          <span class="btn-text">转发</span>
        </el-button>
      </el-tooltip>

      <!-- 收藏 -->
      <el-tooltip content="收藏 (C)" placement="top">
        <el-button
          type="text"
          :disabled="selectedCount === 0"
          @click="handleFavorite"
          class="action-btn"
        >
          <i class="el-icon-star-off"></i>
          <span class="btn-text">收藏</span>
        </el-button>
      </el-tooltip>

      <!-- 下载 -->
      <el-tooltip content="下载 (D)" placement="top">
        <el-button
          v-if="hasDownloadableContent"
          type="text"
          :disabled="selectedCount === 0"
          @click="handleDownload"
          class="action-btn"
        >
          <i class="el-icon-download"></i>
          <span class="btn-text">下载</span>
        </el-button>
      </el-tooltip>

      <!-- 删除 -->
      <el-tooltip content="删除 (Delete)" placement="top">
        <el-button
          type="text"
          :disabled="selectedCount === 0"
          @click="handleDelete"
          class="action-btn danger"
        >
          <i class="el-icon-delete"></i>
          <span class="btn-text">删除</span>
        </el-button>
      </el-tooltip>

      <!-- 取消选择 -->
      <el-button type="text" @click="handleCancel" class="action-btn cancel-btn">
        <span>取消</span>
      </el-button>
    </div>

    <!-- 转发对话框 -->
    <message-forward-dialog
      v-model="showForwardDialog"
      :messages="selectedMessages"
      @confirm="handleForwardConfirm"
    />

    <!-- 收藏对话框 -->
    <el-dialog
      v-model="showFavoriteDialog"
      title="收藏消息"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="favoriteForm" label-width="80px">
        <el-form-item label="收藏标签">
          <el-input
            v-model="favoriteForm.tags"
            placeholder="多个标签用逗号分隔"
          ></el-input>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="favoriteForm.remark"
            type="textarea"
            :rows="3"
            placeholder="添加备注信息（可选）"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showFavoriteDialog = false">取消</el-button>
        <el-button type="primary" @click="handleFavoriteConfirm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  selectedMessages: {
    type: Array,
    default: () => [],
  },
  totalMessages: {
    type: Number,
    default: 0,
  },
})

// Emits
const emit = defineEmits([
  'select-all',
  'cancel',
  'forward',
  'favorite',
  'delete',
  'download',
])

// 数据
const showForwardDialog = ref(false)
const showFavoriteDialog = ref(false)
const favoriteForm = ref({
  tags: '',
  remark: '',
})

// 计算属性
const selectedCount = computed(() => props.selectedMessages.length)
const isAllSelected = computed(() => {
  return props.totalMessages > 0 && selectedCount.value === props.totalMessages
})
const isIndeterminate = computed(() => {
  return selectedCount.value > 0 && selectedCount.value < props.totalMessages
})
const hasDownloadableContent = computed(() => {
  return props.selectedMessages.some(msg =>
    ['image', 'file', 'video', 'voice'].includes(msg.type)
  )
})

// 方法
const handleSelectAll = (checked) => {
  emit('select-all', checked)
}

const handleCancel = () => {
  emit('cancel')
}

const handleForward = () => {
  showForwardDialog.value = true
}

const handleForwardConfirm = (data) => {
  emit('forward', {
    messages: props.selectedMessages,
    ...data,
  })
  showForwardDialog.value = false
}

const handleFavorite = () => {
  showFavoriteDialog.value = true
}

const handleFavoriteConfirm = () => {
  emit('favorite', {
    messages: props.selectedMessages,
    tags: favoriteForm.value.tags,
    remark: favoriteForm.value.remark,
  })
  showFavoriteDialog.value = false
  favoriteForm.value = { tags: '', remark: '' }
}

const handleDelete = () => {
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedCount.value} 条消息吗？删除后无法恢复。`,
    '确认删除',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(() => {
      emit('delete', props.selectedMessages)
    })
    .catch(() => {
      // 取消删除
    })
}

const handleDownload = () => {
  emit('download', props.selectedMessages)
}

// 键盘快捷键
const handleKeyDown = (event) => {
  if (!props.visible && selectedCount.value === 0) return

  // 阻止在输入框中的快捷键
  const target = event.target
  if (
    target.tagName === 'INPUT' ||
    target.tagName === 'TEXTAREA' ||
    target.isContentEditable
  ) {
    return
  }

  switch (event.key) {
    case 'Escape':
      event.preventDefault()
      handleCancel()
      break
    case 'f':
    case 'F':
      if (selectedCount.value > 0) {
        event.preventDefault()
        handleForward()
      }
      break
    case 'c':
    case 'C':
      if (!event.ctrlKey && !event.metaKey && selectedCount.value > 0) {
        event.preventDefault()
        handleFavorite()
      }
      break
    case 'd':
    case 'D':
      if (!event.ctrlKey && !event.metaKey && selectedCount.value > 0 && hasDownloadableContent.value) {
        event.preventDefault()
        handleDownload()
      }
      break
    case 'Delete':
      if (selectedCount.value > 0) {
        event.preventDefault()
        handleDelete()
      }
      break
  }
}

// 生命周期
onMounted(() => {
  window.addEventListener('keydown', handleKeyDown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown)
})

// 监听收藏对话框关闭，重置表单
watch(showFavoriteDialog, (val) => {
  if (!val) {
    favoriteForm.value = { tags: '', remark: '' }
  }
})
</script>

<style lang="scss" scoped>
@use '@/styles/dingtalk-theme.scss' as *;

.message-multi-select-bar {
  position: fixed;
  bottom: -100px;
  left: 50%;
  transform: translateX(-50%);
  z-index: $z-index-fixed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-xl;
  padding: $spacing-md $spacing-xl;
  background: $bg-white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  transition: all $transition-base $ease-base;
  min-width: 500px;
  max-width: 90vw;

  &.visible {
    bottom: 24px;
    animation: slideUp 0.3s ease-out;
  }

  .select-info {
    display: flex;
    align-items: center;

    .select-all-checkbox {
      :deep(.el-checkbox__label) {
        .select-count {
          font-size: 14px;
          font-weight: 500;
          color: $text-primary;
        }
      }
    }
  }

  .batch-actions {
    display: flex;
    align-items: center;
    gap: $spacing-sm;

    .action-btn {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;
      padding: $spacing-sm $spacing-md;
      min-width: 60px;
      border-radius: $border-radius-base;
      transition: all $transition-base $ease-base;

      &.cancel-btn {
        color: $text-secondary;

        &:hover {
          background: $bg-hover;
        }
      }

      &:not(.cancel-btn):disabled {
        opacity: 0.4;
        cursor: not-allowed;
      }

      &:not(.cancel-btn):not(:disabled) {
        &:hover {
          background: $primary-color-light;

          i,
          .btn-text {
            color: $primary-color;
          }
        }
      }

      i {
        font-size: 18px;
        color: $text-secondary;
        transition: color $transition-base $ease-base;
      }

      .btn-text {
        font-size: 12px;
        color: $text-secondary;
        transition: color $transition-base $ease-base;
      }

      &.danger {
        &:hover {
          background: rgba($error-color, 0.1);

          i,
          .btn-text {
            color: $error-color;
          }
        }
      }
    }
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

// 响应式
@media (max-width: $breakpoint-md) {
  .message-multi-select-bar {
    min-width: auto;
    width: calc(100vw - 48px);
    padding: $spacing-sm $spacing-md;
    gap: $spacing-md;

    .batch-actions {
      gap: 4px;

      .action-btn {
        min-width: 50px;
        padding: $spacing-xs;

        i {
          font-size: 16px;
        }

        .btn-text {
          display: none;
        }
      }
    }
  }
}
</style>
