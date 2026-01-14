<!--
  用户详情弹窗组件 - 钉钉5.6风格
  单聊会话中点击用户头像/名称时显示
  @author RuoYi-IM
-->
<template>
  <el-dialog
    v-model="dialogVisible"
    :show-close="true"
    :close-on-click-modal="true"
    width="560px"
    class="ding-user-detail-dialog"
    destroy-on-close
    @close="handleClose"
  >
    <!-- 头部区域 -->
    <template #header="{ close, titleId, titleClass }">
      <div class="dialog-header">
        <div class="header-left">
          <span class="header-title">详细资料</span>
        </div>
        <el-icon class="header-close" @click="close">
          <Close />
        </el-icon>
      </div>
    </template>

    <div class="dialog-body" v-loading="loading">
      <!-- 用户基本信息卡片 -->
      <div class="user-card">
        <div class="user-avatar-wrap">
          <el-avatar :size="64" :src="userInfo?.avatar">
            <img :src="userInfo?.avatar || defaultAvatar" alt="" />
          </el-avatar>
          <span class="online-dot" :class="{ online: userInfo?.online }"></span>
        </div>
        <div class="user-basic-info">
          <h3 class="user-name">{{ userInfo?.nickname || userInfo?.username }}</h3>
          <p class="user-signature" v-if="userInfo?.signature">{{ userInfo.signature }}</p>
        </div>
      </div>

      <!-- 详细信息列表 -->
      <div class="info-section">
        <div class="info-item" v-if="userInfo?.mobile">
          <span class="info-label">手机</span>
          <span class="info-value">{{ userInfo.mobile }}</span>
        </div>
        <div class="info-item" v-if="userInfo?.email">
          <span class="info-label">邮箱</span>
          <span class="info-value">{{ userInfo.email }}</span>
        </div>
        <div class="info-item" v-if="userInfo?.department">
          <span class="info-label">部门</span>
          <span class="info-value">{{ userInfo.department }}</span>
        </div>
        <div class="info-item" v-if="userInfo?.position">
          <span class="info-label">职位</span>
          <span class="info-value">{{ userInfo.position }}</span>
        </div>
      </div>

      <!-- 聊天内容统计 -->
      <div class="content-section" v-if="conversationId && hasContent">
        <div class="section-header">
          <span class="section-title">聊天内容</span>
        </div>

        <div class="content-stats">
          <div class="stat-item" @click="showContent('media')" v-if="stats.mediaCount > 0">
            <div class="stat-icon">
              <el-icon><Picture /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-label">图片和视频</span>
              <span class="stat-count">{{ stats.mediaCount }}</span>
            </div>
            <el-icon class="stat-arrow"><ArrowRight /></el-icon>
          </div>

          <div class="stat-item" @click="showContent('file')" v-if="stats.fileCount > 0">
            <div class="stat-icon">
              <el-icon><Folder /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-label">文件</span>
              <span class="stat-count">{{ stats.fileCount }}</span>
            </div>
            <el-icon class="stat-arrow"><ArrowRight /></el-icon>
          </div>

          <div class="stat-item" @click="showContent('link')" v-if="stats.linkCount > 0">
            <div class="stat-icon">
              <el-icon><Link /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-label">链接</span>
              <span class="stat-count">{{ stats.linkCount }}</span>
            </div>
            <el-icon class="stat-arrow"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部操作按钮 -->
    <template #footer>
      <div class="dialog-footer">
        <el-button class="footer-btn" @click="handleChat">
          <el-icon><ChatDotRound /></el-icon>
          发消息
        </el-button>
        <el-button class="footer-btn" @click="handleVoiceCall">
          <el-icon><Phone /></el-icon>
        </el-button>
        <el-button class="footer-btn" @click="handleVideoCall">
          <el-icon><VideoCamera /></el-icon>
        </el-button>
        <el-button class="footer-btn more-btn" @click="handleMore">
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
    class="ding-content-dialog"
    append-to-body
  >
    <div class="content-list">
      <!-- 媒体列表 -->
      <div v-if="contentType === 'media'" class="media-grid">
        <div
          v-for="item in mediaList"
          :key="item.id"
          class="media-item"
          @click="previewMedia(item)"
        >
          <el-image :src="item.url" fit="cover" lazy />
        </div>
      </div>

      <!-- 文件列表 -->
      <div v-if="contentType === 'file'" class="file-list">
        <div
          v-for="item in fileList"
          :key="item.id"
          class="file-item"
          @click="downloadFile(item)"
        >
          <el-icon class="file-icon"><Document /></el-icon>
          <div class="file-info">
            <span class="file-name">{{ item.name }}</span>
            <span class="file-size">{{ formatFileSize(item.size) }}</span>
          </div>
        </div>
      </div>

      <!-- 链接列表 -->
      <div v-if="contentType === 'link'" class="link-list">
        <a
          v-for="item in linkList"
          :key="item.id"
          :href="item.url"
          target="_blank"
          class="link-item"
        >
          <el-icon><Link /></el-icon>
          <span class="link-url">{{ item.title || item.url }}</span>
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
// 钉钉5.6风格弹窗
.ding-user-detail-dialog {
  :deep(.el-dialog) {
    border-radius: 12px;
    overflow: hidden;
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
.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 50px;
  padding: 0 20px;
  border-bottom: 1px solid #f0f0f0;

  .header-left {
    .header-title {
      font-size: 16px;
      font-weight: 500;
      color: #1d1d1f;
    }
  }

  .header-close {
    font-size: 20px;
    color: #858585;
    cursor: pointer;
    transition: color 0.2s;

    &:hover {
      color: #1d1d1f;
    }
  }
}

// 内容区域
.dialog-body {
  padding: 20px;
  max-height: 500px;
  overflow-y: auto;
}

// 用户卡片
.user-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 12px;
  margin-bottom: 20px;

  .user-avatar-wrap {
    position: relative;

    .online-dot {
      position: absolute;
      bottom: 2px;
      right: 2px;
      width: 14px;
      height: 14px;
      background: #d9d9d9;
      border: 2px solid #fff;
      border-radius: 50%;

      &.online {
        background: #52c41a;
      }
    }
  }

  .user-basic-info {
    flex: 1;

    .user-name {
      margin: 0 0 4px;
      font-size: 18px;
      font-weight: 500;
      color: #1d1d1f;
    }

    .user-signature {
      margin: 0;
      font-size: 14px;
      color: #858585;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      max-width: 300px;
    }
  }
}

// 详细信息
.info-section {
  .info-item {
    display: flex;
    padding: 14px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .info-label {
      width: 60px;
      font-size: 14px;
      color: #858585;
      flex-shrink: 0;
    }

    .info-value {
      flex: 1;
      font-size: 14px;
      color: #1d1d1f;
    }
  }
}

// 内容统计区域
.content-section {
  margin-top: 20px;

  .section-header {
    margin-bottom: 12px;

    .section-title {
      font-size: 14px;
      font-weight: 500;
      color: #1d1d1f;
    }
  }

  .content-stats {
    .stat-item {
      display: flex;
      align-items: center;
      padding: 12px 16px;
      background: #f5f5f5;
      border-radius: 8px;
      margin-bottom: 8px;
      cursor: pointer;
      transition: background 0.2s;

      &:last-child {
        margin-bottom: 0;
      }

      &:hover {
        background: #e8e8e8;
      }

      .stat-icon {
        width: 40px;
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #fff;
        border-radius: 8px;
        color: #0089ff;
        font-size: 20px;
      }

      .stat-info {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-left: 12px;

        .stat-label {
          font-size: 15px;
          color: #1d1d1f;
        }

        .stat-count {
          font-size: 14px;
          color: #858585;
        }
      }

      .stat-arrow {
        color: #c8c8c8;
        font-size: 16px;
      }
    }
  }
}

// 底部按钮
.dialog-footer {
  display: flex;
  gap: 8px;

  .footer-btn {
    flex: 1;
    height: 40px;
    border: none;
    background: #f5f5f5;
    color: #1d1d1f;
    font-size: 14px;

    &:hover {
      background: #e8e8e8;
    }

    .el-icon {
      font-size: 18px;
    }
  }

  .more-btn {
    flex: 0 0 48px;
  }
}

// 内容详情弹窗
.ding-content-dialog {
  :deep(.el-dialog__body) {
    padding: 16px;
    max-height: 400px;
    overflow-y: auto;
  }
}

.content-list {
  .media-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;

    .media-item {
      aspect-ratio: 1;
      border-radius: 8px;
      overflow: hidden;
      cursor: pointer;

      :deep(.el-image) {
        width: 100%;
        height: 100%;
      }
    }
  }

  .file-list {
    .file-item {
      display: flex;
      align-items: center;
      padding: 12px;
      border-radius: 8px;
      cursor: pointer;

      &:hover {
        background: #f5f5f5;
      }

      .file-icon {
        font-size: 32px;
        color: #0089ff;
        margin-right: 12px;
      }

      .file-info {
        flex: 1;

        .file-name {
          display: block;
          font-size: 14px;
          color: #1d1d1f;
          margin-bottom: 4px;
        }

        .file-size {
          font-size: 12px;
          color: #858585;
        }
      }
    }
  }

  .link-list {
    .link-item {
      display: flex;
      align-items: center;
      padding: 12px;
      border-radius: 8px;
      text-decoration: none;
      color: #1d1d1f;

      &:hover {
        background: #f5f5f5;
      }

      .el-icon {
        color: #0089ff;
        margin-right: 12px;
      }

      .link-url {
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
