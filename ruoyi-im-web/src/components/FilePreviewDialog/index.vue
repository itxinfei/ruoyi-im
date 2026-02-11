<template>
  <el-dialog
    v-model="visible"
    :title="file?.name || '文件预览'"
    width="800px"
    @close="handleClose"
  >
    <div
      v-if="file"
      class="file-preview"
    >
      <!-- 文件信息 -->
      <div class="preview-header">
        <div
          class="file-icon-large"
          :class="file.iconClass"
        >
          <span class="material-icons-outlined">{{ file.icon }}</span>
        </div>
        <div class="file-info">
          <div class="file-name">
            {{ file.name }}
          </div>
          <div class="file-meta">
            <span>{{ file.meta }}</span>
            <span>{{ file.modifiedTime }}</span>
          </div>
        </div>
      </div>

      <!-- 预览内容 -->
      <div class="preview-content">
        <div
          v-if="isImageFile"
          class="image-preview"
        >
          <div class="preview-placeholder">
            <span class="material-icons-outlined">image</span>
            <p>图片预览</p>
            <p class="preview-hint">
              {{ file.name }}
            </p>
          </div>
        </div>

        <div
          v-else-if="isPdfFile"
          class="pdf-preview"
        >
          <div class="preview-placeholder">
            <span class="material-icons-outlined">picture_as_pdf</span>
            <p>PDF 文件预览</p>
            <p class="preview-hint">
              {{ file.name }}
            </p>
          </div>
        </div>

        <div
          v-else-if="isDocFile"
          class="doc-preview"
        >
          <div class="preview-placeholder">
            <span class="material-icons-outlined">description</span>
            <p>Word 文档预览</p>
            <p class="preview-hint">
              {{ file.name }}
            </p>
          </div>
        </div>

        <div
          v-else-if="isSheetFile"
          class="sheet-preview"
        >
          <div class="preview-placeholder">
            <span class="material-icons-outlined">table_view</span>
            <p>Excel 表格预览</p>
            <p class="preview-hint">
              {{ file.name }}
            </p>
          </div>
        </div>

        <div
          v-else
          class="unknown-preview"
        >
          <div class="preview-placeholder">
            <span class="material-icons-outlined">insert_drive_file</span>
            <p>该文件类型暂不支持预览</p>
            <p class="preview-hint">
              您可以下载文件后使用本地程序打开
            </p>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button
          type="primary"
          @click="handleDownload"
        >
          <span class="material-icons-outlined">download</span>
          下载
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  file: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'download'])

const visible = ref(false)

// 判断文件类型
const isImageFile = computed(() => {
  return props.file?.iconClass === 'icon-image'
})

const isPdfFile = computed(() => {
  return props.file?.iconClass === 'icon-pdf'
})

const isDocFile = computed(() => {
  return props.file?.iconClass === 'icon-doc'
})

const isSheetFile = computed(() => {
  return props.file?.iconClass === 'icon-sheet'
})

watch(() => props.modelValue, val => {
  visible.value = val
})

watch(visible, val => {
  emit('update:modelValue', val)
})

const handleClose = () => {
  visible.value = false
}

const handleDownload = () => {
  emit('download', props.file)
}
</script>

<style scoped>
.file-preview {
  padding: 10px 0;
}

.preview-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 20px;
}

.file-icon-large {
  width: 64px;
  height: 64px;
  border-radius: var(--dt-radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.file-icon-large .material-icons-outlined {
  font-size: 32px;
}

.icon-folder { background: #fef3c7; color: #f59e0b; }
.icon-doc { background: #dbeafe; color: #2563eb; }
.icon-sheet { background: #d1fae5; color: #059669; }
.icon-pdf { background: #fee2e2; color: #ef4444; }
.icon-image { background: #f3e8ff; color: #a855f7; }

.file-info {
  flex: 1;
}

.file-name {
  font-size: 16px;
  font-weight: 500;
  color: #262626;
  margin-bottom: 4px;
}

.file-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #8c8c8c;
}

.preview-content {
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-placeholder {
  text-align: center;
  padding: 40px;
  background: #f9f9f9;
  border-radius: var(--dt-radius-lg);
  width: 100%;
}

.preview-placeholder .material-icons-outlined {
  font-size: 64px;
  color: #3296FA;
  margin-bottom: 16px;
}

.preview-placeholder p {
  font-size: 14px;
  color: #262626;
  margin: 4px 0 0 0;
}

.preview-hint {
  font-size: 12px;
  color: #8c8c8c;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  width: 100%;
}

.dialog-footer .material-icons-outlined {
  font-size: 16px;
  vertical-align: middle;
  margin-right: 4px;
}

/* 暗色模式 */
:deep(.dark) .file-name {
  color: #f1f5f9;
}

:deep(.dark) .preview-placeholder {
  background: var(--dt-overlay-50);
}

:deep(.dark) .preview-placeholder p {
  color: #e2e8f0;
}
</style>
