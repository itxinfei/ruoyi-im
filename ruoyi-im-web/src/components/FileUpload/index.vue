<template>
  <div class="file-upload-component">
    <el-upload
      ref="uploadRef"
      :action="uploadUrl"
      :headers="uploadHeaders"
      :data="uploadData"
      :before-upload="beforeUpload"
      :on-progress="handleProgress"
      :on-success="handleSuccess"
      :on-error="handleError"
      :show-file-list="false"
      :accept="accept"
      :multiple="multiple"
      :disabled="disabled"
    >
      <slot>
        <el-button :icon="Upload" :loading="uploading">
          {{ uploadText }}
        </el-button>
      </slot>
    </el-upload>

    <!-- 上传进度 -->
    <el-dialog
      v-model="showProgress"
      title="上传进度"
      width="400px"
      :close-on-click-modal="false"
      :show-close="true"
    >
      <div class="upload-progress">
        <div class="file-info">
          <el-icon class="file-icon"><Document /></el-icon>
          <div class="file-details">
            <div class="file-name">{{ currentFile?.name }}</div>
            <div class="file-size">{{ formatFileSize(currentFile?.size) }}</div>
          </div>
        </div>
        <el-progress
          :percentage="uploadPercentage"
          :status="uploadStatus"
        />
        <div class="progress-text">
          {{ uploadPercentage }}% - {{ uploadStatusText }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, Document } from '@element-plus/icons-vue'

const props = defineProps({
  // 上传类型: image, file
  type: {
    type: String,
    default: 'file',
    validator: (value) => ['image', 'file'].includes(value)
  },
  // 接受的文件类型
  accept: {
    type: String,
    default: '*'
  },
  // 是否支持多选
  multiple: {
    type: Boolean,
    default: false
  },
  // 文件大小限制（MB）
  maxSize: {
    type: Number,
    default: 10
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  // 上传按钮文本
  uploadText: {
    type: String,
    default: '上传文件'
  },
  // 额外的上传数据
  data: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['success', 'error', 'progress'])

const uploadRef = ref(null)
const uploading = ref(false)
const showProgress = ref(false)
const uploadPercentage = ref(0)
const uploadStatus = ref('')
const currentFile = ref(null)

// 上传地址
const uploadUrl = computed(() => {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
  return props.type === 'image'
    ? `${baseUrl}/im/file/upload/image`
    : `${baseUrl}/im/file/upload`
})

// 上传请求头
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('access_token')
  return {
    'Authorization': token ? `Bearer ${token}` : ''
  }
})

// 额外上传数据
const uploadData = computed(() => {
  return {
    ...props.data
  }
})

// 上传状态文本
const uploadStatusText = computed(() => {
  if (uploadStatus.value === 'success') return '上传成功'
  if (uploadStatus.value === 'exception') return '上传失败'
  return '正在上传...'
})

// 上传前验证
const beforeUpload = (file) => {
  currentFile.value = file

  // 验证文件类型
  if (props.type === 'image') {
    const isImage = file.type.startsWith('image/')
    if (!isImage) {
      ElMessage.error('只能上传图片文件!')
      return false
    }
  }

  // 验证文件大小
  const isLtMaxSize = file.size / 1024 / 1024 < props.maxSize
  if (!isLtMaxSize) {
    ElMessage.error(`文件大小不能超过 ${props.maxSize}MB!`)
    return false
  }

  uploading.value = true
  showProgress.value = true
  uploadPercentage.value = 0
  uploadStatus.value = ''

  return true
}

// 上传进度
const handleProgress = (event, file) => {
  uploadPercentage.value = Math.floor(event.percent)
  emit('progress', {
    percentage: uploadPercentage.value,
    file: file
  })
}

// 上传成功
const handleSuccess = (response, file) => {
  uploading.value = false
  uploadPercentage.value = 100
  uploadStatus.value = 'success'

  setTimeout(() => {
    showProgress.value = false
  }, 1000)

  if (response.code === 200) {
    ElMessage.success('上传成功')
    emit('success', {
      file: file,
      data: response.data
    })
  } else {
    uploadStatus.value = 'exception'
    ElMessage.error(response.msg || '上传失败')
    emit('error', {
      file: file,
      error: response.msg
    })
  }
}

// 上传失败
const handleError = (error, file) => {
  uploading.value = false
  uploadStatus.value = 'exception'
  
  setTimeout(() => {
    showProgress.value = false
  }, 2000)

  ElMessage.error('上传失败')
  emit('error', {
    file: file,
    error: error
  })
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// 手动触发上传
const triggerUpload = () => {
  uploadRef.value?.$el.querySelector('input').click()
}

// 暴露方法
defineExpose({
  triggerUpload
})
</script>

<style scoped lang="scss">
.file-upload-component {
  display: inline-block;
}

.upload-progress {
  padding: 20px 0;

  .file-info {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 20px;

    .file-icon {
      font-size: 40px;
      color: #0089ff;
    }

    .file-details {
      flex: 1;

      .file-name {
        font-size: 14px;
        font-weight: 500;
        color: #262626;
        margin-bottom: 4px;
        word-break: break-all;
      }

      .file-size {
        font-size: 12px;
        color: #8c8c8c;
      }
    }
  }

  .progress-text {
    margin-top: 12px;
    text-align: center;
    font-size: 12px;
    color: #595959;
  }
}
</style>
