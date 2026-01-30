<template>
  <el-dialog
    v-model="visible"
    title="文件预览"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="file-preview-container">
      <!-- 文件列表 -->
      <div v-if="files.length > 0" class="file-list">
        <div v-for="(file, index) in files" :key="index" class="file-item">
          <!-- 图片预览 -->
          <div v-if="file.type.startsWith('image/')" class="file-preview image-preview">
            <img :src="previewUrl(file)" :alt="file.name" />
          </div>

          <!-- 其他文件图标 -->
          <div v-else class="file-preview file-icon">
            <span class="material-icons-outlined">description</span>
          </div>

          <!-- 文件信息 -->
          <div class="file-info">
            <div class="file-name">{{ file.name }}</div>
            <div class="file-meta">{{ formatFileSize(file.size) }}</div>
          </div>

          <!-- 删除按钮 -->
          <button class="remove-btn" @click="removeFile(index)">
            <span class="material-icons-outlined">close</span>
          </button>
        </div>
      </div>

      <!-- 文件描述 -->
      <div class="file-description">
        <el-input
          v-model="description"
          type="textarea"
          :rows="3"
          placeholder="添加文件描述（可选）"
          maxlength="500"
          show-word-limit
        />
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleConfirm" :loading="uploading">
          发送 ({{ files.length }})
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * 文件上传预览对话框
 * 显示待上传文件的预览，允许用户添加描述或取消
 */

const props = defineProps({
  modelValue: Boolean,
  files: Array
})

const emit = defineEmits(['update:modelValue', 'confirm', 'remove'])

const visible = ref(false)
const uploading = ref(false)
const description = ref('')

// 预览 URL 缓存
const previewUrls = new Map()

// 监听 modelValue 变化
watch(() => props.modelValue, (newVal) => {
  visible.value = newVal
  if (newVal) {
    // 对话框打开时，清空之前的预览 URL
    clearPreviewUrls()
  }
})

// 监听 visible 变化
watch(visible, (newVal) => {
  if (!newVal) {
    emit('update:modelValue', false)
    clearPreviewUrls()
  }
})

/**
 * 生成文件预览 URL
 */
function previewUrl(file) {
  if (previewUrls.has(file)) {
    return previewUrls.get(file)
  }

  const url = URL.createObjectURL(file)
  previewUrls.set(file, url)
  return url
}

/**
 * 清理预览 URL
 */
function clearPreviewUrls() {
  previewUrls.forEach(url => URL.revokeObjectURL(url))
  previewUrls.clear()
}

/**
 * 格式化文件大小
 */
function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
}

/**
 * 移除文件
 */
function removeFile(index) {
  emit('remove', index)
}

/**
 * 关闭对话框
 */
function handleClose() {
  visible.value = false
  description.value = ''
}

/**
 * 确认上传
 */
function handleConfirm() {
  if (props.files.length === 0) {
    ElMessage.warning('请先选择文件')
    return
  }

  emit('confirm', {
    files: props.files,
    description: description.value.trim()
  })

  // 不立即关闭，等待上传完成
  // uploading.value = true
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.file-preview-container {
  padding: 8px 0;
}

.file-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
  padding: 8px;
  background: var(--dt-bg-body);
  border-radius: var(--dt-radius-md);
  margin-bottom: 16px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-border-color);
    border-radius: 3px;
  }
}

.file-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-color);
  border-radius: var(--dt-radius-md);
  transition: all var(--dt-transition-fast);

  &:hover {
    border-color: var(--dt-brand-color);
    box-shadow: 0 2px 8px rgba(0, 137, 255, 0.1);
  }
}

.file-preview {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--dt-radius-sm);
  background: var(--dt-bg-hover);

  &.image-preview {
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  &.file-icon {
    font-size: 32px;
    color: var(--dt-text-tertiary);

    .material-icons-outlined {
      font-size: inherit;
    }
  }
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}

.file-meta {
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.remove-btn {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: var(--dt-text-tertiary);
  cursor: pointer;
  border-radius: 50%;
  transition: all var(--dt-transition-fast);

  &:hover {
    background: rgba(239, 68, 68, 0.1);
    color: var(--dt-color-danger);
  }

  .material-icons-outlined {
    font-size: 18px;
  }
}

.file-description {
  margin-top: 16px;

  :deep(.el-textarea) {
    .el-textarea__inner {
      border-radius: var(--dt-radius-md);
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

// 暗色模式
.dark {
  .file-list {
    background: var(--dt-bg-body-dark);
  }

  .file-item {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);

    &:hover {
      border-color: var(--dt-brand-color);
      box-shadow: 0 2px 8px rgba(0, 137, 255, 0.2);
    }
  }

  .file-preview {
    background: var(--dt-bg-hover-dark);

    &.file-icon {
      color: var(--dt-text-tertiary-dark);
    }
  }

  .file-name {
    color: var(--dt-text-primary-dark);
  }

  .file-meta {
    color: var(--dt-text-tertiary-dark);
  }
}
</style>
