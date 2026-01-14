<!--
  用户详情弹窗组件
  单聊会话中点击用户头像/名称时显示
  @author RuoYi-IM
-->
<template>
  <el-dialog
    v-model="dialogVisible"
    :title="title"
    width="500px"
    :before-close="handleClose"
    class="user-detail-dialog"
    destroy-on-close
  >
    <div class="dialog-content" v-loading="loading">
      <!-- 用户头像和基本信息 -->
      <div class="user-profile">
        <el-avatar :size="100" :src="userInfo?.avatar" class="user-avatar">
          <img :src="userInfo?.avatar || defaultAvatar" alt="" />
        </el-avatar>
        <h2 class="user-name">{{ userInfo?.nickname || userInfo?.username }}</h2>
        <p class="user-signature" v-if="userInfo?.signature">{{ userInfo.signature }}</p>

        <!-- 用户标签 -->
        <div class="user-tags">
          <el-tag v-if="userInfo?.online" type="success" size="small">在线</el-tag>
          <el-tag v-else type="info" size="small">离线</el-tag>
          <el-tag v-if="userInfo?.gender === 1" type="primary" size="small">男</el-tag>
          <el-tag v-else-if="userInfo?.gender === 2" type="danger" size="small">女</el-tag>
        </div>
      </div>

      <!-- 快捷操作 -->
      <div class="quick-actions">
        <el-button type="primary" :icon="Message" @click="handleChat">
          发消息
        </el-button>
        <el-button :icon="Phone" @click="handleVoiceCall">
          语音通话
        </el-button>
        <el-button :icon="VideoCamera" @click="handleVideoCall">
          视频通话
        </el-button>
      </div>

      <!-- 详细信息 -->
      <div class="detail-section">
        <div class="detail-item" v-if="userInfo?.mobile">
          <span class="detail-label">
            <el-icon><Phone /></el-icon>
            手机号
          </span>
          <span class="detail-value">{{ userInfo.mobile }}</span>
        </div>
        <div class="detail-item" v-if="userInfo?.email">
          <span class="detail-label">
            <el-icon><Message /></el-icon>
            邮箱
          </span>
          <span class="detail-value">{{ userInfo.email }}</span>
        </div>
        <div class="detail-item" v-if="userInfo?.department">
          <span class="detail-label">
            <el-icon><OfficeBuilding /></el-icon>
            部门
          </span>
          <span class="detail-value">{{ userInfo.department }}</span>
        </div>
        <div class="detail-item" v-if="userInfo?.position">
          <span class="detail-label">
            <el-icon><User /></el-icon>
            职位
          </span>
          <span class="detail-value">{{ userInfo.position }}</span>
        </div>
      </div>

      <!-- 内容统计 -->
      <div class="stats-section" v-if="conversationId">
        <div class="stats-title">聊天内容</div>
        <div class="stats-tabs">
          <div
            class="stats-tab"
            :class="{ active: activeTab === 'media' }"
            @click="activeTab = 'media'"
          >
            <el-icon><Picture /></el-icon>
            <span>媒体</span>
            <span class="stats-count" v-if="stats.mediaCount">{{ stats.mediaCount }}</span>
          </div>
          <div
            class="stats-tab"
            :class="{ active: activeTab === 'file' }"
            @click="activeTab = 'file'"
          >
            <el-icon><Folder /></el-icon>
            <span>文件</span>
            <span class="stats-count" v-if="stats.fileCount">{{ stats.fileCount }}</span>
          </div>
          <div
            class="stats-tab"
            :class="{ active: activeTab === 'link' }"
            @click="activeTab = 'link'"
          >
            <el-icon><Link /></el-icon>
            <span>链接</span>
            <span class="stats-count" v-if="stats.linkCount">{{ stats.linkCount }}</span>
          </div>
        </div>

        <!-- 统计内容列表 -->
        <div class="stats-content" v-loading="loadingStats">
          <template v-if="activeTab === 'media' && mediaList.length > 0">
            <div class="media-grid">
              <div
                v-for="item in mediaList"
                :key="item.id"
                class="media-item"
                @click="previewMedia(item)"
              >
                <el-image
                  :src="item.url"
                  fit="cover"
                  lazy
                  class="media-image"
                >
                  <template #error>
                    <div class="image-slot">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
              </div>
            </div>
          </template>

          <template v-if="activeTab === 'file' && fileList.length > 0">
            <div class="file-list">
              <div
                v-for="item in fileList"
                :key="item.id"
                class="file-item"
                @click="downloadFile(item)"
              >
                <el-icon class="file-icon">
                  <Document />
                </el-icon>
                <span class="file-name">{{ item.name }}</span>
                <span class="file-size">{{ formatFileSize(item.size) }}</span>
              </div>
            </div>
          </template>

          <template v-if="activeTab === 'link' && linkList.length > 0">
            <div class="link-list">
              <a
                v-for="item in linkList"
                :key="item.id"
                :href="item.url"
                target="_blank"
                class="link-item"
              >
                <el-icon><Link /></el-icon>
                <span class="link-title">{{ item.title || item.url }}</span>
                <el-icon class="external-icon"><TopRight /></el-icon>
              </a>
            </div>
          </template>

          <el-empty
            v-if="!loadingStats && getCurrentList().length === 0"
            description="暂无内容"
            :image-size="80"
          />
        </div>
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
  Message,
  Phone,
  VideoCamera,
  Picture,
  Folder,
  Link,
  OfficeBuilding,
  User,
  Document,
  TopRight,
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
const activeTab = ref('media')
const loadingStats = ref(false)
const stats = ref({
  mediaCount: 0,
  fileCount: 0,
  linkCount: 0,
})
const mediaList = ref([])
const fileList = ref([])
const linkList = ref([])

// 图片预览
const previewVisible = ref(false)
const previewIndex = ref(0)
const previewUrls = ref([])

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 计算属性
const title = computed(() => {
  return userInfo.value?.nickname || userInfo.value?.username || '用户详情'
})

const getCurrentList = () => {
  switch (activeTab.value) {
    case 'media':
      return mediaList.value
    case 'file':
      return fileList.value
    case 'link':
      return linkList.value
    default:
      return []
  }
}

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
    } else {
      ElMessage.error('获取用户信息失败')
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

// 加载内容统计
const loadContentStats = async () => {
  if (!props.conversationId) return

  loadingStats.value = true
  try {
    // 获取最近消息列表来统计
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
            type: msg.messageType.toLowerCase(),
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
      mediaList.value = media.slice(0, 20)
      fileList.value = files.slice(0, 20)
      linkList.value = links.slice(0, 20)
    }
  } catch (error) {
    console.error('获取内容统计失败:', error)
  } finally {
    loadingStats.value = false
  }
}

// 切换 tab 时加载数据
watch(activeTab, () => {
  if (getCurrentList().length === 0 && props.conversationId) {
    loadContentStats()
  }
})

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
.user-detail-dialog {
  .el-dialog__body {
    padding: 0;
    max-height: 60vh;
    overflow-y: auto;
  }
}

.dialog-content {
  padding: 20px;
}

// 用户基本信息
.user-profile {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
  text-align: center;
  border-bottom: 1px solid var(--el-border-color-lighter);

  .user-avatar {
    margin-bottom: 16px;
    border: 3px solid var(--el-border-color-lighter);
  }

  .user-name {
    margin: 0 0 8px;
    font-size: 20px;
    font-weight: 500;
  }

  .user-signature {
    margin: 0 0 16px;
    font-size: 14px;
    color: var(--el-text-color-secondary);
  }

  .user-tags {
    display: flex;
    gap: 8px;
  }
}

// 快捷操作
.quick-actions {
  display: flex;
  gap: 8px;
  margin: 20px 0;
  justify-content: center;

  .el-button {
    flex: 1;
  }
}

// 详细信息
.detail-section {
  padding: 16px 0;
  border-top: 1px solid var(--el-border-color-lighter);
  border-bottom: 1px solid var(--el-border-color-lighter);

  .detail-item {
    display: flex;
    align-items: center;
    padding: 12px 0;

    .detail-label {
      display: flex;
      align-items: center;
      gap: 8px;
      width: 80px;
      font-size: 14px;
      color: var(--el-text-color-secondary);

      .el-icon {
        font-size: 16px;
      }
    }

    .detail-value {
      flex: 1;
      font-size: 14px;
      color: var(--el-text-color-primary);
    }
  }
}

// 统计区域
.stats-section {
  margin-top: 16px;

  .stats-title {
    font-size: 14px;
    font-weight: 500;
    color: var(--el-text-color-secondary);
    margin-bottom: 12px;
  }

  .stats-tabs {
    display: flex;
    gap: 8px;
    margin-bottom: 16px;

    .stats-tab {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 16px;
      border-radius: 8px;
      background: var(--el-fill-color-light);
      cursor: pointer;
      font-size: 14px;
      color: var(--el-text-color-regular);
      transition: all 0.2s;

      .el-icon {
        font-size: 16px;
      }

      .stats-count {
        font-size: 12px;
        color: var(--el-color-danger);
      }

      &:hover {
        background: var(--el-fill-color);
      }

      &.active {
        background: var(--el-color-primary);
        color: #fff;

        .stats-count {
          color: rgba(255, 255, 255, 0.9);
        }
      }
    }
  }

  .stats-content {
    min-height: 150px;
  }
}

// 媒体网格
.media-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;

  .media-item {
    aspect-ratio: 1;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;

    .media-image {
      width: 100%;
      height: 100%;
    }

    .image-slot {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      height: 100%;
      background: var(--el-fill-color-light);
      color: var(--el-text-color-placeholder);
    }
  }
}

// 文件列表
.file-list {
  .file-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: background 0.2s;

    &:hover {
      background: var(--el-fill-color-light);
    }

    .file-icon {
      font-size: 24px;
      color: var(--el-color-primary);
    }

    .file-name {
      flex: 1;
      font-size: 14px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .file-size {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }
}

// 链接列表
.link-list {
  .link-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px;
    border-radius: 8px;
    text-decoration: none;
    color: var(--el-text-color-primary);
    transition: background 0.2s;

    &:hover {
      background: var(--el-fill-color-light);
    }

    .link-title {
      flex: 1;
      font-size: 14px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .external-icon {
      font-size: 14px;
      color: var(--el-text-color-secondary);
    }
  }
}
</style>
