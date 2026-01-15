<!--
  用户详情弹窗组件 - 钉钉6.5.x风格
  单聊会话中点击用户头像/名称时显示
  @author RuoYi-IM
  @updated 2026-01-15 升级至钉钉6.5.x风格
-->
<template>
  <el-dialog
    v-model="dialogVisible"
    :show-close="false"
    :close-on-click-modal="true"
    width="560px"
    class="dt-user-detail-dialog"
    destroy-on-close
    @close="handleClose"
  >
    <!-- 头部区域 -->
    <template #header="{ close, titleId, titleClass }">
      <div class="dt-dialog-header">
        <span class="dt-header-title">详细资料</span>
        <el-icon class="dt-header-close" @click="close">
          <Close />
        </el-icon>
      </div>
    </template>

    <div class="dt-dialog-body" v-loading="loading">
      <!-- 用户基本信息卡片 -->
      <div class="dt-user-card">
        <div class="dt-user-avatar-wrap">
          <el-avatar :size="72" :src="userInfo?.avatar">
            <img :src="userInfo?.avatar || defaultAvatar" alt="" />
          </el-avatar>
          <span class="dt-online-dot" :class="{ online: userInfo?.online }"></span>
        </div>
        <div class="dt-user-basic-info">
          <h3 class="dt-user-name">{{ userInfo?.nickname || userInfo?.username }}</h3>
          <p class="dt-user-signature" v-if="userInfo?.signature">{{ userInfo.signature }}</p>
        </div>
      </div>

      <!-- 详细信息列表 -->
      <div class="dt-info-section">
        <div class="dt-info-item" v-if="userInfo?.mobile">
          <span class="dt-info-label">手机</span>
          <span class="dt-info-value">{{ userInfo.mobile }}</span>
        </div>
        <div class="dt-info-item" v-if="userInfo?.email">
          <span class="dt-info-label">邮箱</span>
          <span class="dt-info-value">{{ userInfo.email }}</span>
        </div>
        <div class="dt-info-item" v-if="userInfo?.department">
          <span class="dt-info-label">部门</span>
          <span class="dt-info-value">{{ userInfo.department }}</span>
        </div>
        <div class="dt-info-item" v-if="userInfo?.position">
          <span class="dt-info-label">职位</span>
          <span class="dt-info-value">{{ userInfo.position }}</span>
        </div>
      </div>

      <!-- 聊天内容统计 -->
      <div class="dt-content-section" v-if="conversationId && hasContent">
        <div class="dt-section-header">
          <span class="dt-section-title">聊天内容</span>
        </div>

        <div class="dt-content-stats">
          <div class="dt-stat-item" @click="showContent('media')" v-if="stats.mediaCount > 0">
            <div class="dt-stat-icon">
              <el-icon><Picture /></el-icon>
            </div>
            <div class="dt-stat-info">
              <span class="dt-stat-label">图片和视频</span>
              <span class="dt-stat-count">{{ stats.mediaCount }}</span>
            </div>
            <el-icon class="dt-stat-arrow"><ArrowRight /></el-icon>
          </div>

          <div class="dt-stat-item" @click="showContent('file')" v-if="stats.fileCount > 0">
            <div class="dt-stat-icon">
              <el-icon><Folder /></el-icon>
            </div>
            <div class="dt-stat-info">
              <span class="dt-stat-label">文件</span>
              <span class="dt-stat-count">{{ stats.fileCount }}</span>
            </div>
            <el-icon class="dt-stat-arrow"><ArrowRight /></el-icon>
          </div>

          <div class="dt-stat-item" @click="showContent('link')" v-if="stats.linkCount > 0">
            <div class="dt-stat-icon">
              <el-icon><Link /></el-icon>
            </div>
            <div class="dt-stat-info">
              <span class="dt-stat-label">链接</span>
              <span class="dt-stat-count">{{ stats.linkCount }}</span>
            </div>
            <el-icon class="dt-stat-arrow"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部操作按钮 -->
    <template #footer>
      <div class="dt-dialog-footer">
        <el-button class="dt-footer-btn" @click="handleChat">
          <el-icon><ChatDotRound /></el-icon>
          发消息
        </el-button>
        <el-button class="dt-footer-btn" @click="handleVoiceCall">
          <el-icon><Phone /></el-icon>
        </el-button>
        <el-button class="dt-footer-btn" @click="handleVideoCall">
          <el-icon><VideoCamera /></el-icon>
        </el-button>
        <el-button class="dt-footer-btn dt-more-btn" @click="handleMore">
          <el-icon><MoreFilled /></el-icon>
        </el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 内容详情弹窗 -->
  <el-dialog
    v-model="contentDialogVisible"
    :title="contentTitle"
    width="500px"
    class="dt-content-dialog"
    append-to-body
  >
    <div class="dt-content-list">
      <!-- 媒体列表 -->
      <div v-if="contentType === 'media'" class="dt-media-grid">
        <div
          v-for="item in mediaList"
          :key="item.id"
          class="dt-media-item"
          @click="previewMedia(item)"
        >
          <el-image :src="item.url" fit="cover" lazy />
        </div>
      </div>

      <!-- 文件列表 -->
      <div v-if="contentType === 'file'" class="dt-file-list">
        <div
          v-for="item in fileList"
          :key="item.id"
          class="dt-file-item"
          @click="downloadFile(item)"
        >
          <el-icon class="dt-file-icon"><Document /></el-icon>
          <div class="dt-file-info">
            <span class="dt-file-name">{{ item.name }}</span>
            <span class="dt-file-size">{{ formatFileSize(item.size) }}</span>
          </div>
        </div>
      </div>

      <!-- 链接列表 -->
      <div v-if="contentType === 'link'" class="dt-link-list">
        <a
          v-for="item in linkList"
          :key="item.id"
          :href="item.url"
          target="_blank"
          class="dt-link-item"
        >
          <el-icon><Link /></el-icon>
          <span class="dt-link-url">{{ item.title || item.url }}</span>
        </a>
      </div>
    </div>
  </el-dialog>

  <!-- 图片预览 -->
  <el-image-viewer
    v-if="previewVisible"
    :url-list="previewUrls"
    :initial-index="previewIndex"
    @close="previewVisible = false"
  />
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import {
  Close,
  Picture,
  Folder,
  Link,
  ArrowRight,
  ChatDotRound,
  Phone,
  VideoCamera,
  MoreFilled,
  Document,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  userId: {
    type: [Number, String],
    default: null,
  },
  conversationId: {
    type: [Number, String],
    default: null,
  },
})

// Emits
const emit = defineEmits(['update:visible', 'chat', 'voiceCall', 'videoCall'])

// 状态
const dialogVisible = ref(false)
const userInfo = ref(null)
const loading = ref(false)
const stats = ref({ mediaCount: 0, fileCount: 0, linkCount: 0 })
const mediaList = ref([])
const fileList = ref([])
const linkList = ref([])

// 内容详情弹窗
const contentDialogVisible = ref(false)
const contentType = ref('')
const contentTitle = computed(() => {
  const titles = { media: '图片和视频', file: '文件', link: '链接' }
  return titles[contentType.value] || ''
})

// 图片预览
const previewVisible = ref(false)
const previewIndex = ref(0)
const previewUrls = ref([])

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 是否有内容
const hasContent = computed(() => {
  return stats.value.mediaCount > 0 || stats.value.fileCount > 0 || stats.value.linkCount > 0
})

// 监听 visible 变化
watch(() => props.visible, (val) => {
  dialogVisible.value = val
  if (val && props.userId) {
    loadUserInfo()
    if (props.conversationId) {
      loadContentStats()
    }
  }
})

watch(dialogVisible, (val) => {
  emit('update:visible', val)
})

// 加载用户信息
const loadUserInfo = async () => {
  if (!props.userId) return

  loading.value = true
  try {
    const response = await request({
      url: `/api/im/user/${props.userId}`,
      method: 'get',
    })
    if (response.code === 200) {
      userInfo.value = response.data
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载内容统计
const loadContentStats = async () => {
  if (!props.conversationId) return

  try {
    const response = await request({
      url: `/api/im/message/list/${props.conversationId}`,
      method: 'get',
      params: { limit: 100 },
    })

    if (response.code === 200 && response.data) {
      const messages = response.data || []
      const media = []
      const files = []
      const links = []

      messages.forEach(msg => {
        let content = {}
        try {
          content = typeof msg.content === 'string' ? JSON.parse(msg.content) : msg.content
        } catch {
          content = { text: msg.content || '' }
        }

        if (msg.messageType === 'IMAGE' || msg.messageType === 'VIDEO') {
          media.push({
            id: msg.id,
            url: content.url || content.thumbUrl,
          })
        } else if (msg.messageType === 'FILE') {
          files.push({
            id: msg.id,
            name: content.fileName || content.name || '未知文件',
            url: content.url,
            size: content.fileSize || 0,
          })
        } else if (msg.messageType === 'TEXT') {
          const text = content.text || ''
          const linkRegex = /https?:\/\/[^\s]+/g
          const matches = text.match(linkRegex)
          if (matches) {
            matches.forEach((url, idx) => {
              links.push({
                id: `link-${msg.id}-${idx}`,
                url,
                title: url,
              })
            })
          }
        }
      })

      stats.value = {
        mediaCount: media.length,
        fileCount: files.length,
        linkCount: links.length,
      }
      mediaList.value = media
      fileList.value = files
      linkList.value = links
    }
  } catch (error) {
    console.error('获取内容统计失败:', error)
  }
}

// 显示内容详情
const showContent = (type) => {
  contentType.value = type
  contentDialogVisible.value = true
}

// 事件处理
const handleClose = () => {
  dialogVisible.value = false
}

const handleChat = () => {
  emit('chat', { userId: props.userId })
  handleClose()
}

const handleVoiceCall = () => {
  emit('voiceCall', { userId: props.userId })
}

const handleVideoCall = () => {
  emit('videoCall', { userId: props.userId })
}

const handleMore = () => {
  ElMessage.info('更多功能开发中...')
}

const previewMedia = (item) => {
  const urls = mediaList.value.map(m => m.url)
  previewUrls.value = urls
  previewIndex.value = urls.indexOf(item.url)
  previewVisible.value = true
}

const downloadFile = (item) => {
  window.open(item.url, '_blank')
}

const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}
</script>

<style lang="scss" scoped>
// 钉钉6.5.x风格弹窗
.dt-user-detail-dialog {
  :deep(.el-dialog) {
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  }

  :deep(.el-dialog__header) {
    padding: 0;
    margin: 0;
  }

  :deep(.el-dialog__body) {
    padding: 0;
  }

  :deep(.el-dialog__footer) {
    padding: 12px 20px;
    border-top: 1px solid #f0f0f0;
  }
}

// 头部
.dt-dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 52px;
  padding: 0 20px;
  border-bottom: 1px solid #f0f0f0;
  background: #ffffff;

  .dt-header-title {
    font-size: 16px;
    font-weight: 500;
    color: #262626;
  }

  .dt-header-close {
    font-size: 20px;
    color: #8c8c8c;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      color: #262626;
      transform: rotate(90deg);
    }
  }
}

// 内容区域
.dt-dialog-body {
  padding: 24px;
  max-height: 520px;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: #e8e8e8;
    border-radius: 3px;

    &:hover {
      background: #d9d9d9;
    }
  }
}

// 用户卡片
.dt-user-card {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 24px;
  background: linear-gradient(135deg, #f8f9fa 0%, #f0f2f5 100%);
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .dt-user-avatar-wrap {
    position: relative;

    .dt-online-dot {
      position: absolute;
      bottom: 3px;
      right: 3px;
      width: 16px;
      height: 16px;
      background: #d9d9d9;
      border: 3px solid #ffffff;
      border-radius: 50%;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
      transition: background 0.3s ease;

      &.online {
        background: #52c41a;
      }
    }
  }

  .dt-user-basic-info {
    flex: 1;

    .dt-user-name {
      margin: 0 0 6px;
      font-size: 20px;
      font-weight: 600;
      color: #262626;
      letter-spacing: -0.3px;
    }

    .dt-user-signature {
      margin: 0;
      font-size: 14px;
      color: #8c8c8c;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      max-width: 320px;
      line-height: 1.5;
    }
  }
}

// 详细信息
.dt-info-section {
  .dt-info-item {
    display: flex;
    padding: 16px 0;
    border-bottom: 1px solid #f0f0f0;
    transition: background 0.2s ease;

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background: rgba(0, 0, 0, 0.02);
    }

    .dt-info-label {
      width: 68px;
      font-size: 14px;
      color: #8c8c8c;
      flex-shrink: 0;
      font-weight: 400;
    }

    .dt-info-value {
      flex: 1;
      font-size: 14px;
      color: #262626;
      font-weight: 400;
      line-height: 1.5;
    }
  }
}

// 内容统计区域
.dt-content-section {
  margin-top: 24px;

  .dt-section-header {
    margin-bottom: 16px;

    .dt-section-title {
      font-size: 15px;
      font-weight: 600;
      color: #262626;
      letter-spacing: -0.2px;
    }
  }

  .dt-content-stats {
    .dt-stat-item {
      display: flex;
      align-items: center;
      padding: 14px 18px;
      background: #fafafa;
      border-radius: 10px;
      margin-bottom: 10px;
      cursor: pointer;
      transition: all 0.2s ease;
      border: 1px solid transparent;

      &:last-child {
        margin-bottom: 0;
      }

      &:hover {
        background: #f5f5f5;
        border-color: #e8e8e8;
        transform: translateX(4px);
      }

      .dt-stat-icon {
        width: 44px;
        height: 44px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #ffffff;
        border-radius: 10px;
        color: #1677ff;
        font-size: 22px;
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
        transition: all 0.2s ease;
      }

      .dt-stat-info {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-left: 14px;

        .dt-stat-label {
          font-size: 15px;
          color: #262626;
          font-weight: 400;
        }

        .dt-stat-count {
          font-size: 14px;
          color: #8c8c8c;
          font-weight: 500;
        }
      }

      .dt-stat-arrow {
        color: #c8c8c8;
        font-size: 18px;
        transition: all 0.2s ease;
      }

      &:hover .dt-stat-icon {
        transform: scale(1.05);
        box-shadow: 0 4px 10px rgba(22, 119, 255, 0.15);
      }

      &:hover .dt-stat-arrow {
        color: #1677ff;
        transform: translateX(4px);
      }
    }
  }
}

// 底部按钮
.dt-dialog-footer {
  display: flex;
  gap: 10px;

  .dt-footer-btn {
    flex: 1;
    height: 42px;
    border: none;
    background: #fafafa;
    color: #262626;
    font-size: 14px;
    font-weight: 400;
    border-radius: 8px;
    transition: all 0.2s ease;

    &:hover {
      background: #f0f0f0;
      transform: translateY(-1px);
    }

    &:active {
      transform: translateY(0);
    }

    .el-icon {
      font-size: 18px;
    }
  }

  .dt-more-btn {
    flex: 0 0 52px;
  }
}

// 内容详情弹窗
.dt-content-dialog {
  :deep(.el-dialog__body) {
    padding: 18px;
    max-height: 420px;
    overflow-y: auto;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background: #e8e8e8;
      border-radius: 3px;

      &:hover {
        background: #d9d9d9;
      }
    }
  }
}

.dt-content-list {
  .dt-media-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 10px;

    .dt-media-item {
      aspect-ratio: 1;
      border-radius: 10px;
      overflow: hidden;
      cursor: pointer;
      transition: all 0.2s ease;

      &:hover {
        transform: scale(1.02);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
      }

      :deep(.el-image) {
        width: 100%;
        height: 100%;
      }
    }
  }

  .dt-file-list {
    .dt-file-item {
      display: flex;
      align-items: center;
      padding: 14px;
      border-radius: 10px;
      cursor: pointer;
      transition: all 0.2s ease;
      background: #fafafa;
      margin-bottom: 8px;

      &:last-child {
        margin-bottom: 0;
      }

      &:hover {
        background: #f5f5f5;
        transform: translateX(4px);
      }

      .dt-file-icon {
        font-size: 36px;
        color: #1677ff;
        margin-right: 14px;
      }

      .dt-file-info {
        flex: 1;

        .dt-file-name {
          display: block;
          font-size: 14px;
          color: #262626;
          margin-bottom: 4px;
          font-weight: 400;
        }

        .dt-file-size {
          font-size: 12px;
          color: #8c8c8c;
        }
      }
    }
  }

  .dt-link-list {
    .dt-link-item {
      display: flex;
      align-items: center;
      padding: 14px;
      border-radius: 10px;
      text-decoration: none;
      color: #262626;
      background: #fafafa;
      margin-bottom: 8px;
      transition: all 0.2s ease;

      &:last-child {
        margin-bottom: 0;
      }

      &:hover {
        background: #f5f5f5;
        transform: translateX(4px);
      }

      .el-icon {
        color: #1677ff;
        margin-right: 14px;
        font-size: 18px;
      }

      .dt-link-url {
        flex: 1;
        font-size: 14px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}
</style>
