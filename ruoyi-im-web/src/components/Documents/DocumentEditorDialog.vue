<template>
  <el-dialog
    v-model="visible"
    :title="isEditing ? '编辑文档' : '文档预览'"
    width="900px"
    class="document-editor-dialog"
    destroy-on-close
    append-to-body
    :fullscreen="isFullscreen"
  >
    <template #header>
      <div class="dialog-header">
        <span class="dialog-title">{{ isEditing ? '编辑文档' : document?.name || '文档预览' }}</span>
        <div class="header-actions">
          <el-button text @click="toggleFullscreen">
            <el-icon><FullScreen /></el-icon>
          </el-button>
        </div>
      </div>
    </template>

    <div class="editor-container">
      <!-- 预览模式 -->
      <div v-if="!isEditing" class="preview-mode">
        <div class="doc-info">
          <div class="doc-icon" :class="iconClass">
            <el-icon><component :is="fileIcon" /></el-icon>
          </div>
          <div class="doc-meta">
            <h3>{{ document?.name }}</h3>
            <p>大小: {{ document?.meta }} | 所有者: {{ document?.owner }} | 修改时间: {{ document?.modifiedTime }}</p>
          </div>
        </div>
        <div class="preview-content">
          <div v-if="document?.documentType === 'TEXT'" class="text-preview">
            <pre>{{ documentContent }}</pre>
          </div>
          <div v-else-if="document?.documentType === 'IMAGE'" class="image-preview">
            <img :src="documentUrl" alt="预览">
          </div>
          <div v-else class="unsupported-preview">
            <el-icon><Document /></el-icon>
            <p>该文件类型暂不支持预览</p>
            <el-button type="primary" @click="downloadDocument">
              下载文件
            </el-button>
          </div>
        </div>
      </div>

      <!-- 编辑模式 -->
      <div v-else class="edit-mode">
        <el-input
          v-model="editForm.title"
          placeholder="文档标题"
          class="title-input"
        />
        <el-textarea
          v-model="editForm.content"
          :rows="20"
          placeholder="输入文档内容..."
          resize="none"
          class="content-textarea"
        />
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">
          关闭
        </el-button>
        <el-button v-if="!isEditing" type="primary" @click="enterEditMode">
          <el-icon><Edit /></el-icon>编辑
        </el-button>
        <template v-else>
          <el-button @click="cancelEdit">
            取消
          </el-button>
          <el-button type="primary" :loading="saving" @click="saveDocument">
            <el-icon><Check /></el-icon>保存
          </el-button>
        </template>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { FullScreen, Edit, Check, Picture, VideoCamera, Microphone, Folder, Document } from '@element-plus/icons-vue'
import { getDocument, updateDocument } from '@/api/im/document'

const props = defineProps({
  modelValue: Boolean,
  document: { type: Object, default: null }
})

const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isFullscreen = ref(false)
const isEditing = ref(false)
const saving = ref(false)
const documentContent = ref('')
const documentUrl = ref('')

const editForm = ref({
  title: '',
  content: ''
})

const fileIcon = computed(() => {
  const icons = {
    'TEXT': Document,
    'FILE': Document,
    'IMAGE': Picture,
    'VIDEO': VideoCamera,
    'VOICE': Microphone,
    'FOLDER': Folder
  }
  return icons[props.document?.documentType] || Document
})

const iconClass = computed(() => {
  const classes = {
    'TEXT': 'icon-doc',
    'FILE': 'icon-doc',
    'IMAGE': 'icon-image',
    'VIDEO': 'icon-video',
    'VOICE': 'icon-voice',
    'FOLDER': 'icon-folder'
  }
  return classes[props.document?.documentType] || 'icon-doc'
})

const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
}

const enterEditMode = () => {
  if (props.document?.documentType !== 'TEXT') {
    ElMessage.warning('只有文本文档支持编辑')
    return
  }
  editForm.value.title = props.document?.name || ''
  editForm.value.content = documentContent.value
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
}

const saveDocument = async () => {
  if (!editForm.value.title.trim()) {
    ElMessage.warning('请输入文档标题')
    return
  }
  saving.value = true
  try {
    await updateDocument(props.document?.id, {
      title: editForm.value.title,
      content: editForm.value.content
    })
    ElMessage.success('文档保存成功')
    isEditing.value = false
    emit('saved')
  } catch (error) {
    console.error('保存文档失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const downloadDocument = () => {
  ElMessage.info('文档下载功能即将推出，请直接复制内容')
}

const loadDocumentDetail = async () => {
  if (!props.document?.id) return
  try {
    const res = await getDocument(props.document.id)
    if (res.code === 200 && res.data) {
      documentContent.value = res.data.content || ''
      documentUrl.value = res.data.url || ''
    }
  } catch (error) {
    console.error('加载文档详情失败:', error)
  }
}

watch(() => props.modelValue, (val) => {
  if (val && props.document) {
    isEditing.value = false
    loadDocumentDetail()
  }
})
</script>

<style scoped lang="scss">
.document-editor-dialog {
  :deep(.el-dialog__header) {
    padding: 0;
    margin: 0;
  }

  :deep(.el-dialog__body) {
    padding: 0;
    max-height: 70vh;
    overflow: hidden;
  }

  :deep(.el-dialog__footer) {
    padding: 16px 24px;
    border-top: 1px solid var(--dt-border-light);
  }
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid var(--dt-border-light);

  .dialog-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--dt-text-primary);
  }

  .header-actions {
    display: flex;
    gap: 8px;
  }
}

.editor-container {
  min-height: 400px;
  max-height: 60vh;
  overflow-y: auto;
}

.preview-mode {
  .doc-info {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px 24px;
    background: var(--dt-bg-body);
    border-bottom: 1px solid var(--dt-border-light);

    .doc-icon {
      width: 48px;
      height: 48px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;

      .el-icon { font-size: 24px; }

      &.icon-doc {
        background: var(--dt-brand-bg);
        color: var(--dt-brand-color);
      }

      &.icon-image {
        background: var(--dt-success-bg);
        color: var(--dt-success-color);
      }
    }

    .doc-meta {
      h3 {
        font-size: 16px;
        font-weight: 600;
        color: var(--dt-text-primary);
        margin: 0 0 4px 0;
      }

      p {
        font-size: 13px;
        color: var(--dt-text-secondary);
        margin: 0;
      }
    }
  }

  .preview-content {
    padding: 24px;

    .text-preview {
      pre {
        white-space: pre-wrap;
        word-wrap: break-word;
        font-family: inherit;
        font-size: 14px;
        line-height: 1.6;
        color: var(--dt-text-primary);
        background: var(--dt-bg-body);
        padding: 16px;
        border-radius: 8px;
        margin: 0;
      }
    }

    .image-preview {
      text-align: center;

      img {
        max-width: 100%;
        max-height: 50vh;
        border-radius: 8px;
      }
    }

    .unsupported-preview {
      text-align: center;
      padding: 60px 20px;

      .el-icon {
        font-size: 64px;
        color: var(--dt-text-tertiary);
        margin-bottom: 16px;
      }

      p {
        font-size: 14px;
        color: var(--dt-text-secondary);
        margin-bottom: 20px;
      }
    }
  }
}

.edit-mode {
  padding: 20px 24px;

  .title-input {
    margin-bottom: 16px;

    :deep(.el-input__inner) {
      font-size: 18px;
      font-weight: 600;
    }
  }

  .content-textarea {
    :deep(.el-textarea__inner) {
      font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
      font-size: 14px;
      line-height: 1.6;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dark {
  .document-editor-dialog {
    :deep(.el-dialog__footer) {
      border-top-color: var(--dt-border-dark);
    }
  }

  .dialog-header {
    border-bottom-color: var(--dt-border-dark);
  }

  .preview-mode {
    .doc-info {
      background: var(--dt-bg-hover-dark);
      border-bottom-color: var(--dt-border-dark);
    }

    .preview-content {
      .text-preview pre {
        background: var(--dt-bg-hover-dark);
        color: var(--dt-text-primary-dark);
      }
    }
  }
}
</style>
