<template>
  <teleport to="body">
    <transition name="slide">
      <div v-if="visible" class="chat-files-overlay" @click.self="handleClose">
        <div class="chat-files-panel">
          <!-- 头部 -->
          <div class="files-header">
            <div class="header-title">
              <span class="material-icons-outlined">folder_open</span>
              <span>聊天文件</span>
              <span v-if="totalFiles > 0" class="files-count">({{ totalFiles }})</span>
            </div>
            <button class="close-btn" @click="handleClose">
              <span class="material-icons-outlined">close</span>
            </button>
          </div>

          <!-- 文件类型筛选和搜索 -->
          <div class="files-toolbar">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索文件名..."
              clearable
              size="small"
            >
              <template #prefix>
                <span class="material-icons-outlined">search</span>
              </template>
            </el-input>

            <div class="file-type-tabs">
              <button
                v-for="tab in fileTypeTabs"
                :key="tab.key"
                class="tab-btn"
                :class="{ active: activeTab === tab.key }"
                @click="activeTab = tab.key"
              >
                <span class="material-icons-outlined">{{ tab.icon }}</span>
                <span>{{ tab.label }}</span>
                <span v-if="tab.count > 0" class="tab-count">{{ tab.count }}</span>
              </button>
            </div>
          </div>

          <!-- 文件列表 -->
          <div class="files-list" ref="listRef">
            <!-- 加载状态 -->
            <div v-if="loading" class="loading-state">
              <el-icon class="is-loading" :size="32"><Loading /></el-icon>
              <span>加载中...</span>
            </div>

            <!-- 空状态 -->
            <div v-else-if="filteredFiles.length === 0" class="empty-state">
              <span class="material-icons-outlined">folder_off</span>
              <p v-if="searchKeyword">未找到匹配的文件</p>
              <p v-else>暂无文件</p>
              <span class="hint">聊天中发送的文件会显示在这里</span>
            </div>

            <!-- 文件列表 -->
            <div v-else class="file-items">
              <!-- 按日期分组 -->
              <template v-for="(group, date) in groupedFiles" :key="date">
                <div class="group-date">{{ formatDate(date) }}</div>

                <div
                  v-for="file in group"
                  :key="file.id"
                  class="file-item"
                  @click="handleFileClick(file)"
                >
                  <!-- 文件图标 -->
                  <div class="file-icon" :class="`type-${fileType(file.name)}`">
                    <span class="material-icons-outlined">{{ getFileIcon(file.name) }}</span>
                  </div>

                  <!-- 文件信息 -->
                  <div class="file-info">
                    <div class="file-name" :title="file.name">{{ file.name }}</div>
                    <div class="file-meta">
                      <span class="file-sender">{{ file.senderName }}</span>
                      <span class="file-size">{{ formatFileSize(file.size) }}</span>
                    </div>
                  </div>

                  <!-- 文件操作 -->
                  <div class="file-actions" @click.stop>
                    <el-button
                      type="primary"
                      size="small"
                      text
                      @click="handleDownload(file)"
                    >
                      <span class="material-icons-outlined">download</span>
                    </el-button>
                    <el-dropdown trigger="click" @command="(cmd) => handleFileAction(cmd, file)">
                      <el-button size="small" text>
                        <span class="material-icons-outlined">more_vert</span>
                      </el-button>
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item command="open">
                            <span class="material-icons-outlined">open_in_new</span>
                            打开
                          </el-dropdown-item>
                          <el-dropdown-item command="forward">
                            <span class="material-icons-outlined">forward</span>
                            转发
                          </el-dropdown-item>
                          <el-dropdown-item command="save" divided>
                            <span class="material-icons-outlined">save_alt</span>
                            保存到云盘
                          </el-dropdown-item>
                          <el-dropdown-item command="delete" class="danger">
                            <span class="material-icons-outlined">delete</span>
                            删除
                          </el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                  </div>
                </div>
              </template>
            </div>

            <!-- 加载更多 -->
            <div v-if="hasMore && !loading" class="load-more" @click="loadMore">
              <span>加载更多</span>
            </div>
          </div>

          <!-- 底部统计 -->
          <div v-if="filteredFiles.length > 0" class="files-footer">
            <div class="storage-info">
              <span class="material-icons-outlined">storage</span>
              <span>文件总大小：{{ formatTotalSize() }}</span>
            </div>
            <el-button size="small" @click="handleBatchDownload">
              <span class="material-icons-outlined">download</span>
              批量下载
            </el-button>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElLoading } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { confirmDelete, messageSuccess } from '@/utils/ui'
import { saveFileToCloud } from '@/api/im/cloud'
import { parseMessageContent, formatFileSize } from '@/utils/message'

const props = defineProps({
  visible: { type: Boolean, default: false },
  sessionId: { type: [String, Number], default: null },
  messages: { type: Array, default: () => [] }
})

const emit = defineEmits(['close', 'open-file', 'download-file', 'forward-file', 'load-more'])

const searchKeyword = ref('')
const loading = ref(false)
const hasMore = ref(false)
const activeTab = ref('all')
const listRef = ref(null)

// 文件类型标签页
const fileTypeTabs = computed(() => [
  { key: 'all', label: '全部', icon: 'apps', count: 0 },
  { key: 'image', label: '图片', icon: 'image', count: 0 },
  { key: 'document', label: '文档', icon: 'description', count: 0 },
  { key: 'video', label: '视频', icon: 'videocam', count: 0 },
  { key: 'audio', label: '音频', icon: 'audiotrack', count: 0 },
  { key: 'archive', label: '压缩包', icon: 'archive', count: 0 }
])

// 从消息中提取文件列表
const fileList = computed(() => {
  const files = []

  props.messages.forEach(msg => {
    if (msg.type === 'FILE' || msg.type === 'IMAGE') {
      let fileInfo = null
      const content = parseMessageContent(msg) || {}

      if (msg.type === 'IMAGE') {
        fileInfo = {
          id: `img_${msg.id}`,
          name: content.fileName || `图片_${msg.timestamp}.${content.fileType || 'jpg'}`,
          type: 'image',
          size: content.fileSize || 0,
          url: content.imageUrl || content.url || '',
          senderName: msg.senderName,
          senderId: msg.senderId,
          timestamp: msg.timestamp,
          messageId: msg.id
        }
      } else if (msg.type === 'FILE') {
        fileInfo = {
          id: msg.id,
          name: content.fileName || '未知文件',
          type: getFileType(content.fileName),
          size: content.fileSize || 0,
          url: content.fileUrl || content.url || '',
          senderName: msg.senderName,
          senderId: msg.senderId,
          timestamp: msg.timestamp,
          messageId: msg.id
        }
      }

      if (fileInfo) {
        files.push(fileInfo)
      }
    }
  })

  // 按时间倒序排序
  return files.sort((a, b) => b.timestamp - a.timestamp)
})

// 更新标签页计数
watch(fileList, (files) => {
  fileTypeTabs.value.forEach(tab => {
    if (tab.key === 'all') {
      tab.count = files.length
    } else {
      tab.count = files.filter(f => {
        if (tab.key === 'image') return f.type === 'image'
        if (tab.key === 'document') return ['document', 'text', 'pdf'].includes(f.type)
        if (tab.key === 'video') return f.type === 'video'
        if (tab.key === 'audio') return f.type === 'audio'
        if (tab.key === 'archive') return f.type === 'archive'
        return false
      }).length
    }
  })
}, { immediate: true })

// 过滤后的文件
const filteredFiles = computed(() => {
  let files = fileList.value

  // 按类型筛选
  if (activeTab.value !== 'all') {
    if (activeTab.value === 'image') {
      files = files.filter(f => f.type === 'image')
    } else if (activeTab.value === 'document') {
      files = files.filter(f => ['document', 'text', 'pdf'].includes(f.type))
    } else if (activeTab.value === 'video') {
      files = files.filter(f => f.type === 'video')
    } else if (activeTab.value === 'audio') {
      files = files.filter(f => f.type === 'audio')
    } else if (activeTab.value === 'archive') {
      files = files.filter(f => f.type === 'archive')
    }
  }

  // 按关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    files = files.filter(f => f.name.toLowerCase().includes(keyword))
  }

  return files
})

// 总文件数
const totalFiles = computed(() => fileList.value.length)

// 按日期分组
const groupedFiles = computed(() => {
  const groups = {}
  filteredFiles.value.forEach(file => {
    const date = new Date(file.timestamp).toDateString()
    if (!groups[date]) {
      groups[date] = []
    }
    groups[date].push(file)
  })
  return groups
})

// 获取文件类型
const getFileType = (fileName) => {
  const ext = fileName.split('.').pop()?.toLowerCase() || ''

  const imageExts = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg', 'bmp', 'ico']
  const documentExts = ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'md', 'rtf']
  const videoExts = ['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv', 'webm']
  const audioExts = ['mp3', 'wav', 'ogg', 'aac', 'flac', 'm4a']
  const archiveExts = ['zip', 'rar', '7z', 'tar', 'gz', 'bz2']

  if (imageExts.includes(ext)) return 'image'
  if (documentExts.includes(ext)) return ext === 'pdf' ? 'pdf' : 'document'
  if (videoExts.includes(ext)) return 'video'
  if (audioExts.includes(ext)) return 'audio'
  if (archiveExts.includes(ext)) return 'archive'

  return 'file'
}

// 获取文件图标
const getFileIcon = (fileName) => {
  const type = getFileType(fileName)
  const icons = {
    image: 'image',
    pdf: 'picture_as_pdf',
    document: 'description',
    text: 'text_snippet',
    video: 'videocam',
    audio: 'audiotrack',
    archive: 'folder_zip',
    file: 'insert_drive_file'
  }
  return icons[type] || icons.file
}

// 格式化总大小（使用全局 formatFileSize 函数）
const formatTotalSize = () => {
  const total = fileList.value.reduce((sum, f) => sum + (f.size || 0), 0)
  return formatFileSize(total)
}

// 格式化日期
const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)

  if (date.toDateString() === today.toDateString()) {
    return '今天'
  } else if (date.toDateString() === yesterday.toDateString()) {
    return '昨天'
  } else {
    const diff = Math.floor((today - date) / (1000 * 60 * 60 * 24))
    if (diff < 7) {
      const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      return weekdays[date.getDay()]
    }
    return `${date.getMonth() + 1}月${date.getDate()}日`
  }
}

// 文件操作
const handleFileClick = (file) => {
  emit('open-file', file)
}

const handleDownload = (file) => {
  emit('download-file', file)
}

const handleFileAction = async (cmd, file) => {
  switch (cmd) {
    case 'open':
      window.open(file.url, '_blank')
      break
    case 'forward':
      emit('forward-file', file)
      break
    case 'save':
      await handleSaveToCloud(file)
      break
    case 'delete':
      if (await confirmDelete(`"${file.name}"`)) {
        messageSuccess('文件已删除')
      }
      break
  }
}

const loadMore = () => {
  emit('load-more')
}

// 保存文件到云盘
const handleSaveToCloud = async (file) => {
  try {
    const loading = ElLoading.service({
      lock: true,
      text: '正在保存到云盘...',
      background: 'rgba(0, 0, 0, 0.7)'
    })

    try {
      await saveFileToCloud({
        fileName: file.name,
        fileUrl: file.url,
        folderId: null // 保存到根目录
      })

      ElMessage.success('已保存到云盘')
    } finally {
      loading.close()
    }
  } catch (err) {
    console.error('保存到云盘失败:', err)
    ElMessage.error('保存失败，请重试')
  }
}

const handleBatchDownload = () => {
  emit('download-file', filteredFiles.value)
  ElMessage.success('正在批量下载...')
}

const handleClose = () => {
  emit('close')
}

// 监听 visible 变化
watch(() => props.visible, (val) => {
  if (val) {
    searchKeyword.value = ''
    activeTab.value = 'all'
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
}

.slide-enter-from .chat-files-panel,
.slide-leave-to .chat-files-panel {
  transform: translateX(100%);
}

.chat-files-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 2000;
  display: flex;
  justify-content: flex-end;
  backdrop-filter: blur(4px);
}

.chat-files-panel {
  width: 420px;
  height: 100%;
  background: var(--dt-bg-card);
  display: flex;
  flex-direction: column;
  box-shadow: -4px 0 30px rgba(0, 0, 0, 0.2);

  .dark & {
    background: var(--dt-bg-card-dark);
  }
}

// 头部
.files-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--dt-border-light);

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }

  .header-title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 16px;
    font-weight: 600;
    color: var(--dt-text-primary);

    .material-icons-outlined {
      font-size: 20px;
      color: var(--dt-brand-color);
    }

    .files-count {
      color: var(--dt-text-tertiary);
      font-size: 14px;
      font-weight: 400;
    }
  }

  .close-btn {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: transparent;
    border: none;
    border-radius: var(--dt-radius-full);
    cursor: pointer;
    color: var(--dt-text-secondary);
    transition: all 0.2s;

    &:hover {
      background: var(--dt-bg-hover);
    }
  }
}

// 工具栏
.files-toolbar {
  padding: 16px 20px;
  border-bottom: 1px solid var(--dt-border-light);

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }

  :deep(.el-input) {
    margin-bottom: 12px;

    .el-input__wrapper {
      border-radius: var(--dt-radius-2xl);
    }

    .el-input__prefix {
      color: var(--dt-text-tertiary);

      .material-icons-outlined {
        font-size: 16px;
      }
    }
  }

  .file-type-tabs {
    display: flex;
    gap: 6px;
    overflow-x: auto;
    padding-bottom: 4px;

    &::-webkit-scrollbar {
      height: 4px;
    }

    .tab-btn {
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 6px 10px;
      background: var(--dt-bg-body);
      border: 1px solid var(--dt-border-light);
      border-radius: var(--dt-radius-2xl);
      font-size: 12px;
      color: var(--dt-text-secondary);
      cursor: pointer;
      transition: all 0.2s;
      white-space: nowrap;

      .material-icons-outlined {
        font-size: 14px;
      }

      .tab-count {
        padding: 2px 6px;
        background: var(--dt-bg-hover);
        border-radius: var(--dt-radius-lg);
        font-size: 10px;
      }

      &:hover {
        border-color: var(--dt-brand-color);
        color: var(--dt-brand-color);
      }

      &.active {
        background: var(--dt-brand-color);
        border-color: var(--dt-brand-color);
        color: #fff;

        .tab-count {
          background: rgba(255, 255, 255, 0.25);
        }
      }
    }
  }
}

// 文件列表
.files-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-border);
    border-radius: var(--dt-radius-sm);
  }
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--dt-text-tertiary);
  gap: 16px;

  .material-icons-outlined {
    font-size: 56px;
    opacity: 0.4;
  }

  p {
    margin: 0;
    font-size: 14px;
  }

  .hint {
    font-size: 12px;
    opacity: 0.7;
  }
}

.file-items {
  .group-date {
    position: sticky;
    top: 0;
    padding: 8px 12px;
    margin-bottom: 8px;
    font-size: 12px;
    font-weight: 500;
    color: var(--dt-text-tertiary);
    background: var(--dt-bg-card);
    z-index: 1;

    .dark & {
      background: var(--dt-bg-card-dark);
    }
  }
}

.file-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: var(--dt-radius-lg);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--dt-bg-hover);
  }

  .file-icon {
    width: 48px;
    height: 48px;
    border-radius: var(--dt-radius-lg);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    .material-icons-outlined {
      font-size: 24px;
      color: #fff;
    }

    &.type-image {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    &.type-pdf,
    &.type-document {
      background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
    }

    &.type-text {
      background: linear-gradient(135deg, #10b981 0%, #059669 100%);
    }

    &.type-video {
      background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
    }

    &.type-audio {
      background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
    }

    &.type-archive {
      background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
    }

    &.type-file {
      background: linear-gradient(135deg, #94a3b8 0%, #64748b 100%);
    }
  }

  .file-info {
    flex: 1;
    min-width: 0;

    .file-name {
      font-size: 13px;
      font-weight: 500;
      color: var(--dt-text-primary);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      margin-bottom: 2px;
    }

    .file-meta {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 11px;
      color: var(--dt-text-tertiary);

      .file-sender {
        max-width: 120px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .file-size::before {
        content: '·';
        margin-right: 8px;
      }
    }
  }

  .file-actions {
    display: flex;
    gap: 4px;
    opacity: 0;
    transition: opacity 0.2s;

    .el-button {
      .material-icons-outlined {
        font-size: 16px;
      }
    }
  }

  &:hover .file-actions {
    opacity: 1;
  }
}

.load-more {
  padding: 12px;
  text-align: center;
  color: var(--dt-brand-color);
  font-size: 13px;
  cursor: pointer;
  border-radius: var(--dt-radius-md);
  transition: background 0.2s;

  &:hover {
    background: var(--dt-brand-bg);
  }
}

// 底部
.files-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-top: 1px solid var(--dt-border-light);

  .dark & {
    border-top-color: var(--dt-border-dark);
  }

  .storage-info {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: var(--dt-text-secondary);

    .material-icons-outlined {
      font-size: 16px;
      color: var(--dt-brand-color);
    }
  }

  .el-button {
    .material-icons-outlined {
      font-size: 14px;
      margin-right: 4px;
    }
  }
}

// 响应式
@media (max-width: 768px) {
  .chat-files-panel {
    width: 100%;
  }

  .file-type-tabs {
    justify-content: flex-start;
  }
}
</style>
