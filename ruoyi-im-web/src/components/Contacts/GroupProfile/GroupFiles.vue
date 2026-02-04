<!--
  群组文件组件

  显示群组内分享的文件，支持按类型筛选和搜索
-->
<template>
  <div class="group-files-container">
    <!-- 文件操作栏 -->
    <div class="files-header">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索文件名"
        clearable
        class="file-search"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-dropdown @command="handleFilterCommand">
        <el-button>
          {{ currentFilterLabel }}<el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="all">全部文件</el-dropdown-item>
            <el-dropdown-item command="image">图片</el-dropdown-item>
            <el-dropdown-item command="document">文档</el-dropdown-item>
            <el-dropdown-item command="video">视频</el-dropdown-item>
            <el-dropdown-item command="audio">音频</el-dropdown-item>
            <el-dropdown-item command="other">其他</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- 文件统计 -->
    <div class="files-count">
      共 {{ filteredFiles.length }} 个文件，{{ formatTotalSize() }}
    </div>

    <!-- 空状态 -->
    <div v-if="filteredFiles.length === 0" class="empty-state">
      <el-icon :size="48" color="#dcdfe6"><FolderOpened /></el-icon>
      <p>{{ searchKeyword ? '未找到匹配的文件' : '群组暂无文件' }}</p>
    </div>

    <!-- 文件列表 -->
    <div v-else class="files-list">
      <div
        v-for="file in filteredFiles"
        :key="file.id"
        class="file-item"
        @click="handleFileClick(file)"
      >
        <div class="file-icon">
          <el-icon :size="32" :color="getFileColor(file.type)">
            <component :is="getFileIcon(file.type)" />
          </el-icon>
        </div>
        <div class="file-info">
          <div class="file-name">{{ file.name }}</div>
          <div class="file-meta">
            <span>{{ file.senderName }}</span>
            <span class="separator">·</span>
            <span>{{ formatTime(file.sendTime) }}</span>
            <span class="separator">·</span>
            <span>{{ formatFileSize(file.size) }}</span>
          </div>
        </div>
        <div class="file-actions">
          <el-button link type="primary" @click.stop="handleDownload(file)">
            <el-icon><Download /></el-icon>
          </el-button>
          <el-button link type="primary" @click.stop="handleOpenInChat(file)">
            <el-icon><ChatDotRound /></el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 加载更多 -->
    <div v-if="hasMore" class="load-more">
      <el-button text @click="loadMore" :loading="loading">
        加载更多
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import {
  Search, ArrowDown, Download, ChatDotRound, FolderOpened,
  Picture, Document, VideoPlay, Headset, Folder
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { formatFileSize, formatListItemTime } from '@/utils/format'

const props = defineProps({
  groupId: {
    type: [String, Number],
    required: true
  }
})

const emit = defineEmits(['open-in-chat'])

// 响应式数据
const searchKeyword = ref('')
const currentFilter = ref('all')
const files = ref([])
const loading = ref(false)
const hasMore = ref(false)

// 当前筛选标签
const currentFilterLabel = computed(() => {
  const labels = {
    all: '全部文件',
    image: '图片',
    document: '文档',
    video: '视频',
    audio: '音频',
    other: '其他'
  }
  return labels[currentFilter.value] || '全部文件'
})

// 过滤后的文件
const filteredFiles = computed(() => {
  let result = files.value

  // 按类型筛选
  if (currentFilter.value !== 'all') {
    result = result.filter(file => file.category === currentFilter.value)
  }

  // 按关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(file =>
      file.name.toLowerCase().includes(keyword)
    )
  }

  return result
})

// 获取文件图标
const getFileIcon = (type) => {
  const iconMap = {
    image: Picture,
    document: Document,
    video: VideoPlay,
    audio: Headset,
    other: Folder
  }
  return iconMap[type] || Folder
}

// 获取文件颜色
const getFileColor = (type) => {
  const colorMap = {
    image: '#409eff',
    document: '#67c23a',
    video: '#f56c6c',
    audio: '#e6a23c',
    other: '#909399'
  }
  return colorMap[type] || '#909399'
}

// 格式化总大小
const formatTotalSize = () => {
  const total = filteredFiles.value.reduce((sum, file) => sum + (file.size || 0), 0)
  return formatFileSize(total)
}

// 使用共享工具函数
const formatTime = formatListItemTime

// 处理筛选命令
const handleFilterCommand = (command) => {
  currentFilter.value = command
}

// 处理文件点击
const handleFileClick = (file) => {
  if (file.category === 'image') {
    // 图片预览
    emit('preview-image', file)
  } else {
    // 其他文件下载
    handleDownload(file)
  }
}

// 处理下载
const handleDownload = (file) => {
  const link = document.createElement('a')
  link.href = file.url
  link.download = file.name
  link.click()
  ElMessage.success(`开始下载 ${file.name}`)
}

// 在聊天中打开
const handleOpenInChat = (file) => {
  emit('open-in-chat', file)
}

// 加载更多
const loadMore = () => {
  loading.value = true
  // 模拟加载
  setTimeout(() => {
    loading.value = false
  }, 1000)
}

// 模拟初始化数据
const initMockData = () => {
  files.value = [
    {
      id: 1,
      name: '项目需求文档.docx',
      type: 'document',
      category: 'document',
      size: 256000,
      url: '#',
      senderName: '张三',
      sendTime: new Date(Date.now() - 3600000)
    },
    {
      id: 2,
      name: '设计稿.png',
      type: 'image',
      category: 'image',
      size: 1024000,
      url: '#',
      senderName: '李四',
      sendTime: new Date(Date.now() - 7200000)
    },
    {
      id: 3,
      name: '演示视频.mp4',
      type: 'video',
      category: 'video',
      size: 25600000,
      url: '#',
      senderName: '王五',
      sendTime: new Date(Date.now() - 86400000)
    },
    {
      id: 4,
      name: '会议记录.pdf',
      type: 'document',
      category: 'document',
      size: 512000,
      url: '#',
      senderName: '赵六',
      sendTime: new Date(Date.now() - 172800000)
    }
  ]
  hasMore.value = false
}

// 初始化
initMockData()
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.group-files-container {
  padding: 16px 0;
}

.files-header {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;

  .file-search {
    flex: 1;
  }
}

.files-count {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  margin-bottom: 12px;
  padding: 0 4px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--dt-text-tertiary);

  p {
    margin: 8px 0;
  }
}

.files-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: background-color var(--dt-transition-base);

  &:hover {
    background: var(--dt-bg-hover);

    .file-actions {
      opacity: 1;
    }
  }
}

.file-icon {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--dt-bg-secondary);
  border-radius: var(--dt-radius-md);
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-meta {
  font-size: 12px;
  color: var(--dt-text-tertiary);

  .separator {
    margin: 0 4px;
  }
}

.file-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity var(--dt-transition-base);
}

.load-more {
  display: flex;
  justify-content: center;
  padding: 16px 0;
}
</style>
