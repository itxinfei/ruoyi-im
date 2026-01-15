<template>
  <el-drawer
    v-model="visible"
    title="群文件"
    :size="400"
    direction="rtl"
    class="group-files-drawer"
  >
    <template #header>
      <div class="drawer-header">
        <span class="header-title">群文件</span>
        <el-upload
          ref="uploadRef"
          :action="uploadUrl"
          :headers="uploadHeaders"
          :data="uploadData"
          :show-file-list="false"
          :before-upload="beforeUpload"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          accept="*/*"
        >
          <el-button type="primary" size="small">
            <el-icon><Upload /></el-icon>
            上传文件
          </el-button>
        </el-upload>
      </div>
    </template>

    <div class="files-content">
      <!-- 空状态 -->
      <el-empty v-if="files.length === 0" description="暂无群文件" :image-size="100">
        <template #image>
          <el-icon :size="80" color="#D9D9D9">
            <FolderOpened />
          </el-icon>
        </template>
      </el-empty>

      <!-- 文件列表 -->
      <div v-else class="files-list">
        <div
          v-for="file in displayFiles"
          :key="file.id"
          class="file-item"
          @click="handleFileClick(file)"
        >
          <div class="file-icon" :class="`file-type-${getFileType(file.name)}`">
            <el-icon>
              <component :is="getFileIcon(getFileType(file.name))" />
            </el-icon>
          </div>
          <div class="file-info">
            <div class="file-name" :title="file.name">{{ file.name }}</div>
            <div class="file-meta">
              <span class="file-uploader">{{ file.uploaderName }}</span>
              <span class="file-time">{{ formatTime(file.uploadTime) }}</span>
            </div>
          </div>
          <div class="file-size">{{ formatFileSize(file.size) }}</div>
        </div>
      </div>

      <!-- 加载更多 -->
      <div v-if="hasMore" class="load-more">
        <el-button text :loading="loading" @click="loadMore"> 加载更多 </el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Upload,
  FolderOpened,
  Document,
  Picture,
  VideoCamera,
  Files,
} from '@element-plus/icons-vue'
import { getToken } from '@/utils/auth'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  // 群组ID
  groupId: {
    type: [String, Number],
    default: '',
  },
  // 文件列表
  files: {
    type: Array,
    default: () => [],
  },
  // 是否有更多
  hasMore: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:modelValue', 'upload', 'download', 'load-more'])

const visible = computed({
  get: () => props.modelValue,
  set: val => emit('update:modelValue', val),
})

const uploadRef = ref(null)
const loading = ref(false)

// 上传配置
const uploadUrl = computed(() => {
  return `${import.meta.env.VITE_APP_BASE_API}/im/file/upload`
})

const uploadHeaders = computed(() => {
  return {
    Authorization: 'Bearer ' + getToken(),
  }
})

const uploadData = computed(() => {
  return {
    groupId: props.groupId,
  }
})

// 显示的文件列表
const displayFiles = computed(() => {
  return props.files.slice().sort((a, b) => {
    return new Date(b.uploadTime) - new Date(a.uploadTime)
  })
})

// 获取文件类型
const getFileType = fileName => {
  const ext = fileName.split('.').pop()?.toLowerCase()
  if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(ext)) return 'image'
  if (['mp4', 'avi', 'mov', 'wmv', 'flv'].includes(ext)) return 'video'
  if (['mp3', 'wav', 'flac', 'aac', 'm4a'].includes(ext)) return 'audio'
  if (['pdf'].includes(ext)) return 'pdf'
  if (['doc', 'docx'].includes(ext)) return 'word'
  if (['xls', 'xlsx'].includes(ext)) return 'excel'
  if (['ppt', 'pptx'].includes(ext)) return 'ppt'
  if (['zip', 'rar', '7z'].includes(ext)) return 'archive'
  return 'other'
}

// 获取文件图标
const getFileIcon = type => {
  const iconMap = {
    image: Picture,
    video: VideoCamera,
    audio: Files,
    pdf: Document,
    word: Document,
    excel: Document,
    ppt: Document,
    archive: Files,
    other: Document,
  }
  return iconMap[type] || Document
}

// 格式化文件大小
const formatFileSize = bytes => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
}

// 格式化时间
const formatTime = time => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  // 今天
  if (diff < 86400000 && date.getDate() === now.getDate()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  // 昨天
  if (diff < 172800000) {
    return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  // 更早
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

// 上传前验证
const beforeUpload = file => {
  const maxSize = 100 * 1024 * 1024 // 100MB
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过100MB')
    return false
  }
  return true
}

// 上传成功
const handleUploadSuccess = (response, file) => {
  ElMessage.success('文件上传成功')
  emit('upload', { file, response })
}

// 上传失败
const handleUploadError = () => {
  ElMessage.error('文件上传失败')
}

// 点击文件
const handleFileClick = file => {
  emit('download', file)
}

// 加载更多
const loadMore = () => {
  loading.value = true
  emit('load-more', { groupId: props.groupId })
  setTimeout(() => {
    loading.value = false
  }, 500)
}
</script>

<style lang="scss" scoped>
.group-files-drawer {
  :deep(.el-drawer__header) {
    margin-bottom: 0;
    padding: 16px 20px;
  }

  :deep(.el-drawer__body) {
    padding: 0;
    overflow: hidden;
  }
}

.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;

  .header-title {
    font-size: 16px;
    font-weight: 600;
    color: #171a1a;
  }

  .el-button {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.files-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.files-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;

  .file-item {
    display: flex;
    align-items: center;
    padding: 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: background-color 0.2s;

    &:hover {
      background-color: #f5f7fa;
    }

    .file-icon {
      width: 40px;
      height: 40px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 12px;
      font-size: 20px;
      flex-shrink: 0;

      &.file-type-image {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }

      &.file-type-video {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        color: #fff;
      }

      &.file-type-audio {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        color: #fff;
      }

      &.file-type-pdf {
        background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
        color: #fff;
      }

      &.file-type-word {
        background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);
        color: #fff;
      }

      &.file-type-excel {
        background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
        color: #fff;
      }

      &.file-type-ppt {
        background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
        color: #fff;
      }

      &.file-type-archive {
        background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
        color: #fff;
      }

      &.file-type-other {
        background: #f0f2f5;
        color: #858b8f;
      }
    }

    .file-info {
      flex: 1;
      min-width: 0;

      .file-name {
        font-size: 14px;
        color: #171a1a;
        margin-bottom: 4px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .file-meta {
        font-size: 12px;
        color: #858b8f;
        display: flex;
        gap: 8px;
      }
    }

    .file-size {
      font-size: 12px;
      color: #858b8f;
      margin-left: 12px;
      flex-shrink: 0;
    }
  }
}

.load-more {
  padding: 12px;
  text-align: center;
  border-top: 1px solid #e5e8eb;

  .el-button {
    width: 100%;
  }
}
</style>
