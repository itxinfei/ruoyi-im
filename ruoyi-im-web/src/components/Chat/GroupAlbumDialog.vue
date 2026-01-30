<template>
  <el-dialog
    v-model="visible"
    :title="`${groupInfo.name || '群聊'} - 相册`"
    :width="900"
    :modal="true"
    :close-on-click-modal="false"
    class="group-album-dialog"
    @closed="handleClosed"
  >
    <div class="album-container">
      <!-- 工具栏 -->
      <div class="album-toolbar">
        <div class="toolbar-left">
          <el-select v-model="filterType" size="small" style="width: 120px">
            <el-option label="全部" value="all" />
            <el-option label="按日期" value="date" />
            <el-option label="按发送人" value="sender" />
          </el-select>
          <el-select v-if="filterType === 'sender'" v-model="selectedSender" size="small" style="width: 140px" placeholder="选择成员">
            <el-option label="全部成员" value="" />
            <el-option
              v-for="member in senders"
              :key="member.id"
              :label="member.name"
              :value="member.id"
            />
          </el-select>
          <el-date-picker
            v-if="filterType === 'date'"
            v-model="selectedDate"
            type="date"
            size="small"
            placeholder="选择日期"
            style="width: 150px"
            clearable
          />
        </div>
        <div class="toolbar-right">
          <span class="image-count">共 {{ filteredImages.length }} 张图片</span>
          <el-button type="primary" size="small" @click="handleUpload">
            <el-icon><Upload /></el-icon>
            上传图片
          </el-button>
        </div>
      </div>

      <!-- 图片网格 -->
      <div v-loading="loading" class="album-content">
        <div v-if="filteredImages.length === 0" class="empty-state">
          <span class="material-icons-outlined empty-icon">photo_library</span>
          <p>暂无图片</p>
          <el-button type="primary" plain @click="handleUpload">上传第一张图片</el-button>
        </div>

        <!-- 按日期分组 -->
        <template v-else-if="filterType === 'date'">
          <div v-for="(group, date) in groupedByDate" :key="date" class="date-group">
            <div class="date-header">
              <span class="material-icons-outlined">calendar_today</span>
              {{ formatDateHeader(date) }}
            </div>
            <div class="image-grid">
              <div
                v-for="image in group"
                :key="image.id"
                class="image-item"
                @click="handlePreview(image)"
              >
                <div class="image-wrapper">
                  <img :src="image.url" :alt="image.name" loading="lazy" />
                  <div class="image-overlay">
                    <div class="overlay-actions">
                      <el-button circle size="small" @click.stop="handleDownload(image)">
                        <el-icon><Download /></el-icon>
                      </el-button>
                      <el-button v-if="canManage" circle size="small" type="danger" @click.stop="handleDelete(image)">
                        <el-icon><Delete /></el-icon>
                      </el-button>
                    </div>
                  </div>
                </div>
                <div class="image-info">
                  <span class="sender-name">{{ image.senderName }}</span>
                  <span class="send-time">{{ formatTime(image.sendTime) }}</span>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- 按发送人分组 -->
        <template v-else-if="filterType === 'sender'">
          <div v-for="(group, senderId) in groupedBySender" :key="senderId" class="sender-group">
            <div class="sender-header">
              <DingtalkAvatar :src="group[0]?.senderAvatar" :name="group[0]?.senderName" :size="32" />
              <span class="sender-name">{{ group[0]?.senderName }}</span>
              <span class="image-count-badge">{{ group.length }} 张</span>
            </div>
            <div class="image-grid">
              <div
                v-for="image in group"
                :key="image.id"
                class="image-item"
                @click="handlePreview(image)"
              >
                <div class="image-wrapper">
                  <img :src="image.url" :alt="image.name" loading="lazy" />
                  <div class="image-overlay">
                    <div class="overlay-actions">
                      <el-button circle size="small" @click.stop="handleDownload(image)">
                        <el-icon><Download /></el-icon>
                      </el-button>
                      <el-button v-if="canManage" circle size="small" type="danger" @click.stop="handleDelete(image)">
                        <el-icon><Delete /></el-icon>
                      </el-button>
                    </div>
                  </div>
                </div>
                <div class="image-info">
                  <span class="send-time">{{ formatTime(image.sendTime) }}</span>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- 全部图片 -->
        <div v-else class="image-grid">
          <div
            v-for="image in filteredImages"
            :key="image.id"
            class="image-item"
            @click="handlePreview(image)"
          >
            <div class="image-wrapper">
              <img :src="image.url" :alt="image.name" loading="lazy" />
              <div class="image-overlay">
                <div class="overlay-actions">
                  <el-button circle size="small" @click.stop="handleDownload(image)">
                    <el-icon><Download /></el-icon>
                  </el-button>
                  <el-button v-if="canManage" circle size="small" type="danger" @click.stop="handleDelete(image)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
            <div class="image-info">
              <span class="sender-name">{{ image.senderName }}</span>
              <span class="send-time">{{ formatTime(image.sendTime) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 图片预览 -->
    <el-image-viewer
      v-if="previewVisible"
      :url-list="previewUrls"
      :initial-index="previewIndex"
      @close="previewVisible = false"
      @switch="handlePreviewSwitch"
    />

    <!-- 上传对话框 -->
    <input
      ref="fileInputRef"
      type="file"
      accept="image/*"
      multiple
      style="display: none"
      @change="handleFileSelect"
    />

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { Upload, Download, Delete } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { addTokenToUrl } from '@/utils/file'
import { confirmDelete, deleteSuccess } from '@/utils/ui'
import dayjs from 'dayjs'
import { formatTime } from '@/utils/format'
import { parseMessageContent } from '@/utils/message'

const props = defineProps({
  modelValue: Boolean,
  groupId: [String, Number],
  groupInfo: {
    type: Object,
    default: () => ({})
  },
  messages: {
    type: Array,
    default: () => []
  },
  canManage: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'upload', 'delete', 'preview'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const filterType = ref('all')
const selectedSender = ref('')
const selectedDate = ref(null)
const previewVisible = ref(false)
const previewIndex = ref(0)
const fileInputRef = ref(null)
const images = ref([])

// 从消息中提取图片
const extractImages = () => {
  if (!props.messages || props.messages.length === 0) {
    images.value = []
    return
  }

  const imageList = props.messages
    .filter(m => m.messageType === 'IMAGE')
    .map(m => {
      const content = parseMessageContent(m) || {}
      return {
        id: m.id,
        url: addTokenToUrl(content.url || content.thumbUrl || content.fileUrl),
        name: content.fileName || 'image',
        senderId: m.senderId,
        senderName: m.senderName,
        senderAvatar: m.senderAvatar,
        sendTime: m.sendTime || m.createTime,
        width: content.width,
        height: content.height,
        size: content.fileSize || 0
      }
    })

  images.value = imageList
}

// 过滤后的图片
const filteredImages = computed(() => {
  let result = images.value

  if (filterType.value === 'sender' && selectedSender.value) {
    result = result.filter(img => img.senderId == selectedSender.value)
  }

  if (filterType.value === 'date' && selectedDate.value) {
    const targetDate = dayjs(selectedDate.value).format('YYYY-MM-DD')
    result = result.filter(img => dayjs(img.sendTime).format('YYYY-MM-DD') === targetDate)
  }

  return result
})

// 按日期分组
const groupedByDate = computed(() => {
  const groups = {}
  filteredImages.value.forEach(img => {
    const date = dayjs(img.sendTime).format('YYYY-MM-DD')
    if (!groups[date]) groups[date] = []
    groups[date].push(img)
  })
  return groups
})

// 按发送人分组
const groupedBySender = computed(() => {
  const groups = {}
  filteredImages.value.forEach(img => {
    const senderId = img.senderId || 'unknown'
    if (!groups[senderId]) groups[senderId] = []
    groups[senderId].push(img)
  })
  return groups
})

// 发送人列表
const senders = computed(() => {
  const senderMap = new Map()
  images.value.forEach(img => {
    if (!senderMap.has(img.senderId)) {
      senderMap.set(img.senderId, {
        id: img.senderId,
        name: img.senderName,
        avatar: img.senderAvatar
      })
    }
  })
  return Array.from(senderMap.values())
})

// 预览URL列表
const previewUrls = computed(() => filteredImages.value.map(img => img.url))

// 格式化日期标题
const formatDateHeader = (dateStr) => {
  const date = dayjs(dateStr)
  const today = dayjs()
  const yesterday = today.subtract(1, 'day')

  if (date.format('YYYY-MM-DD') === today.format('YYYY-MM-DD')) {
    return '今天'
  } else if (date.format('YYYY-MM-DD') === yesterday.format('YYYY-MM-DD')) {
    return '昨天'
  } else {
    return date.format('YYYY年MM月DD日')
  }
}

// 预览图片
const handlePreview = (image) => {
  previewIndex.value = filteredImages.value.findIndex(img => img.id === image.id)
  previewVisible.value = true
}

const handlePreviewSwitch = (index) => {
  previewIndex.value = index
  const image = filteredImages.value[index]
  emit('preview', image)
}

// 上传图片
const handleUpload = () => {
  fileInputRef.value?.click()
}

const handleFileSelect = async (event) => {
  const files = Array.from(event.target.files)
  if (files.length === 0) return

  emit('upload', files)

  // 清空input，允许重复选择同一文件
  event.target.value = ''
}

// 下载图片
const handleDownload = (image) => {
  const link = document.createElement('a')
  link.href = image.url
  link.download = image.name
  link.click()
}

// 删除图片
const handleDelete = async (image) => {
  if (await confirmDelete('这张图片')) {
    emit('delete', image)
    deleteSuccess()
  }
}

// 对话框关闭
const handleClosed = () => {
  filterType.value = 'all'
  selectedSender.value = ''
  selectedDate.value = null
  previewVisible.value = false
}

// 监听消息变化
watch(() => props.messages, () => {
  extractImages()
}, { deep: true, immediate: true })

// 监听对话框打开
watch(() => props.modelValue, (val) => {
  if (val) {
    extractImages()
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.group-album-dialog {
  .album-container {
    display: flex;
    flex-direction: column;
    height: 600px;
  }

  .album-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    background: var(--dt-bg-tertiary);
    border-radius: var(--dt-radius-md);
    margin-bottom: 16px;

    .toolbar-left {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .toolbar-right {
      display: flex;
      align-items: center;
      gap: 16px;

      .image-count {
        font-size: 13px;
        color: var(--dt-text-tertiary);
      }
    }
  }

  .album-content {
    flex: 1;
    overflow-y: auto;
    padding: 4px;
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: var(--dt-text-tertiary);

    .empty-icon {
      font-size: 64px;
      opacity: 0.5;
      margin-bottom: 16px;
    }

    p {
      font-size: 14px;
      margin-bottom: 20px;
    }
  }

  .date-group,
  .sender-group {
    margin-bottom: 24px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .date-header,
  .sender-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    background: var(--dt-bg-tertiary);
    border-radius: var(--dt-radius-md);
    margin-bottom: 12px;
    font-size: 14px;
    font-weight: 600;
    color: var(--dt-text-primary);

    .material-icons-outlined {
      font-size: 18px;
      color: var(--dt-brand-color);
    }

    .image-count-badge {
      margin-left: auto;
      font-size: 12px;
      font-weight: 500;
      color: var(--dt-text-tertiary);
      background: var(--dt-bg-hover);
      padding: 2px 8px;
      border-radius: 12px;
    }
  }

  .image-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 12px;

    .image-item {
      position: relative;
      aspect-ratio: 1;
      border-radius: var(--dt-radius-md);
      overflow: hidden;
      cursor: pointer;
      transition: all var(--dt-transition-base);

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);

        .image-overlay {
          opacity: 1;
        }
      }

      .image-wrapper {
        width: 100%;
        height: 100%;
        position: relative;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }

      .image-overlay {
        position: absolute;
        inset: 0;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        align-items: center;
        justify-content: center;
        opacity: 0;
        transition: opacity var(--dt-transition-base);

        .overlay-actions {
          display: flex;
          gap: 8px;

          .el-button {
            background: rgba(255, 255, 255, 0.9);
            border: none;

            &:hover {
              background: #fff;
            }
          }
        }
      }

      .image-info {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        padding: 8px;
        background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
        display: flex;
        justify-content: space-between;
        align-items: center;

        .sender-name,
        .send-time {
          font-size: 12px;
          color: #fff;
          text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
        }
      }
    }
  }
}

// 暗色模式
.dark .group-album-dialog {
  .album-toolbar {
    background: var(--dt-bg-tertiary-dark);
  }

  .date-header,
  .sender-header {
    background: var(--dt-bg-tertiary-dark);

    .image-count-badge {
      background: var(--dt-bg-hover-dark);
    }
  }

  .image-info {
    .sender-name,
    .send-time {
      color: #fff;
    }
  }
}

// 响应式
@media (max-width: 768px) {
  .group-album-dialog {
    .album-toolbar {
      flex-direction: column;
      gap: 12px;
      align-items: stretch;

      .toolbar-left,
      .toolbar-right {
        justify-content: space-between;
      }
    }

    .image-grid {
      grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
      gap: 8px;
    }
  }
}

// 解决 el-image-viewer 样式问题
:deep(.el-image-viewer__mask) {
  z-index: 9999;
}
</style>
