<template>
  <div class="user-profile-space">
    <!-- 头部封面 -->
    <div class="profile-header" :class="{ 'has-cover': user.coverUrl }">
      <div v-if="user.coverUrl" class="cover-image" :style="{ backgroundImage: `url(${user.coverUrl})` }"></div>
      <div v-else class="cover-default">
        <div class="cover-pattern"></div>
      </div>

      <!-- 返回按钮 -->
      <el-button class="back-btn" circle @click="$emit('back')">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>

      <!-- 操作按钮 -->
      <div class="header-actions">
        <el-button v-if="!isCurrentUser" @click="handleStartChat">
          <el-icon><ChatDotRound /></el-icon>
          发消息
        </el-button>
        <el-dropdown @command="handleMoreAction">
          <el-button circle>
            <el-icon><MoreFilled /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="share">分享名片</el-dropdown-item>
              <el-dropdown-item command="qrcode">二维码</el-dropdown-item>
              <el-dropdown-item v-if="!isCurrentUser" command="block" divided>加入黑名单</el-dropdown-item>
              <el-dropdown-item v-if="isCurrentUser" command="edit" divided>编辑资料</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <!-- 用户基本信息 -->
      <div class="profile-summary">
        <DingtalkAvatar
          :src="user.avatar"
          :name="user.nickname || user.username"
          :user-id="user.id"
          :size="88"
          shape="square"
          custom-class="profile-avatar"
        />
        <div class="summary-info">
          <h1 class="nickname">{{ user.nickname || user.username }}</h1>
          <p class="username">@{{ user.username }}</p>
          <div class="status-badge" :class="{ online: isOnline }">
            <span class="dot"></span>
            {{ isOnline ? '在线' : '离线' }}
          </div>
        </div>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="profile-content">
      <!-- 左侧边栏 -->
      <div class="profile-sidebar">
        <!-- 基本信息卡片 -->
        <div class="info-card">
          <h3 class="card-title">基本信息</h3>
          <div class="info-list">
            <div class="info-item">
              <span class="material-icons-outlined info-icon">badge</span>
              <div class="info-content">
                <span class="info-label">职位</span>
                <span class="info-value">{{ user.position || '暂无' }}</span>
              </div>
            </div>
            <div class="info-item">
              <span class="material-icons-outlined info-icon">business</span>
              <div class="info-content">
                <span class="info-label">部门</span>
                <span class="info-value">{{ user.departmentName || user.department || '未分配' }}</span>
              </div>
            </div>
            <div class="info-item">
              <span class="material-icons-outlined info-icon">email</span>
              <div class="info-content">
                <span class="info-label">邮箱</span>
                <span class="info-value">{{ user.email || '-' }}</span>
              </div>
            </div>
            <div class="info-item">
              <span class="material-icons-outlined info-icon">phone</span>
              <div class="info-content">
                <span class="info-label">手机</span>
                <span class="info-value">{{ user.mobile || '-' }}</span>
              </div>
            </div>
            <div v-if="user.signature" class="info-item">
              <span class="material-icons-outlined info-icon">format_quote</span>
              <div class="info-content">
                <span class="info-label">个性签名</span>
                <span class="info-value signature">{{ user.signature }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 共同群组 -->
        <div v-if="commonGroups.length > 0" class="info-card">
          <h3 class="card-title">共同群组 ({{ commonGroups.length }})</h3>
          <div class="groups-list">
            <div
              v-for="group in commonGroups.slice(0, 5)"
              :key="group.id"
              class="group-item"
              @click="handleGroupClick(group)"
            >
              <DingtalkAvatar :src="group.avatar" :name="group.name" :size="36" />
              <span class="group-name">{{ group.name }}</span>
            </div>
          </div>
        </div>

        <!-- 标签 -->
        <div v-if="userTags && userTags.length > 0" class="info-card">
          <h3 class="card-title">标签</h3>
          <div class="tags-container">
            <el-tag
              v-for="tag in userTags"
              :key="tag"
              size="small"
              type="info"
            >{{ tag }}</el-tag>
          </div>
        </div>
      </div>

      <!-- 主内容区 -->
      <div class="profile-main">
        <!-- Tab 切换 -->
        <el-tabs v-model="activeTab" class="profile-tabs">
          <!-- 动态 -->
          <el-tab-pane label="动态" name="activity">
            <div v-if="activities.length > 0" class="activity-list">
              <div v-for="activity in activities" :key="activity.id" class="activity-item">
                <div class="activity-icon" :class="`type-${activity.type}`">
                  <span class="material-icons-outlined">{{ getActivityIcon(activity.type) }}</span>
                </div>
                <div class="activity-content">
                  <div class="activity-text">
                    <span class="activity-user">{{ activity.userName }}</span>
                    <span class="activity-action">{{ activity.action }}</span>
                  </div>
                  <div class="activity-time">{{ formatTime(activity.createTime) }}</div>
                </div>
              </div>
            </div>
            <div v-else class="empty-state">
              <span class="material-icons-outlined">timeline</span>
              <p>暂无动态</p>
            </div>
          </el-tab-pane>

          <!-- 图片 -->
          <el-tab-pane name="images">
            <template #label>
              <span>图片</span>
              <el-badge v-if="imageCount > 0" :value="imageCount" :max="99" class="tab-badge" />
            </template>
            <div v-if="images.length > 0" class="image-gallery">
              <div
                v-for="image in images"
                :key="image.id"
                class="gallery-item"
                @click="handleImagePreview(image)"
              >
                <img :src="image.url" :alt="image.name" loading="lazy" />
              </div>
            </div>
            <div v-else class="empty-state">
              <span class="material-icons-outlined">photo_library</span>
              <p>暂无图片</p>
            </div>
          </el-tab-pane>

          <!-- 文件 -->
          <el-tab-pane name="files">
            <template #label>
              <span>文件</span>
              <el-badge v-if="fileCount > 0" :value="fileCount" :max="99" class="tab-badge" />
            </template>
            <div v-if="files.length > 0" class="file-list">
              <div
                v-for="file in files"
                :key="file.id"
                class="file-item"
                @click="handleFileClick(file)"
              >
                <div class="file-icon" :class="`type-${getFileTypeCategory(file.type)}`">
                  <span class="material-icons-outlined">{{ getFileIcon(file.type) }}</span>
                </div>
                <div class="file-info">
                  <div class="file-name">{{ file.name }}</div>
                  <div class="file-meta">{{ formatFileSize(file.size) }} · {{ formatTime(file.sendTime) }}</div>
                </div>
              </div>
            </div>
            <div v-else class="empty-state">
              <span class="material-icons-outlined">folder_open</span>
              <p>暂无文件</p>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 图片预览 -->
    <el-image-viewer
      v-if="previewVisible"
      :url-list="previewUrls"
      :initial-index="previewIndex"
      @close="previewVisible = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ArrowLeft, ChatDotRound, MoreFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { addTokenToUrl } from '@/utils/file'
import { getCommonGroups } from '@/api/im/group'
import { useStore } from 'vuex'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const props = defineProps({
  userId: {
    type: [String, Number],
    required: true
  },
  user: {
    type: Object,
    default: () => ({})
  },
  messages: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['back', 'start-chat', 'group-click', 'edit'])

const store = useStore()
const currentUser = computed(() => store.getters['user/currentUser'])

const isCurrentUser = computed(() => currentUser.value?.id == props.userId)
const activeTab = ref('activity')
const commonGroups = ref([])
const userTags = ref([])
const activities = ref([])
const images = ref([])
const files = ref([])
const previewVisible = ref(false)
const previewIndex = ref(0)

const isOnline = computed(() => {
  if (isCurrentUser.value) return true
  const status = store.state.im.contact?.userStatus?.[props.userId]
  return status === 'online'
})

const imageCount = computed(() => images.value.length)
const fileCount = computed(() => files.value.length)
const previewUrls = computed(() => images.value.map(img => img.url))

// 加载数据
const loadData = async () => {
  await loadCommonGroups()
  extractContentFromMessages()
  generateActivities()
}

// 加载共同群组
const loadCommonGroups = async () => {
  if (isCurrentUser.value) return

  try {
    const res = await getCommonGroups(props.userId)
    if (res.code === 200) {
      commonGroups.value = res.data || []
    }
  } catch (e) {
    console.error('加载共同群组失败', e)
  }
}

// 从消息中提取内容
const extractContentFromMessages = () => {
  if (!props.messages || props.messages.length === 0) {
    images.value = []
    files.value = []
    return
  }

  // 提取图片
  images.value = props.messages
    .filter(m => m.messageType === 'IMAGE' && m.senderId == props.userId)
    .map(m => {
      const content = typeof m.content === 'string' ? JSON.parse(m.content) : m.content
      return {
        id: m.id,
        url: addTokenToUrl(content.url || content.thumbUrl),
        name: content.fileName || 'image',
        sendTime: m.sendTime || m.createTime
      }
    })

  // 提取文件
  files.value = props.messages
    .filter(m => m.messageType === 'FILE' && m.senderId == props.userId)
    .map(m => {
      const content = typeof m.content === 'string' ? JSON.parse(m.content) : m.content
      return {
        id: m.id,
        name: content.fileName || content.name,
        size: content.fileSize || 0,
        type: content.fileType || getFileExtension(content.fileName),
        url: content.url || content.fileUrl,
        sendTime: m.sendTime || m.createTime
      }
    })
}

// 生成模拟动态数据
const generateActivities = () => {
  // 这里可以从后端获取真实的动态数据
  activities.value = [
    {
      id: 1,
      type: 'message',
      userName: props.user.nickname || props.user.username,
      action: '发送了一条消息',
      createTime: new Date()
    }
  ]
}

// 获取活动图标
const getActivityIcon = (type) => {
  const icons = {
    message: 'chat_bubble',
    image: 'image',
    file: 'attach_file',
    join: 'login',
    leave: 'logout'
  }
  return icons[type] || 'activity'
}

// 获取文件类型分类
const getFileTypeCategory = (type) => {
  if (/^image/.test(type)) return 'image'
  if (/^video/.test(type)) return 'video'
  if (/^audio/.test(type)) return 'audio'
  if (/pdf/.test(type)) return 'pdf'
  if (/doc|docx|txt/.test(type)) return 'doc'
  if (/xls|xlsx/.test(type)) return 'xls'
  if (/ppt|pptx/.test(type)) return 'ppt'
  if (/zip|rar|7z/.test(type)) return 'archive'
  return 'default'
}

// 获取文件图标
const getFileIcon = (type) => {
  const icons = {
    image: 'image',
    video: 'videocam',
    audio: 'audiotrack',
    pdf: 'picture_as_pdf',
    doc: 'description',
    xls: 'table_chart',
    ppt: 'slideshow',
    archive: 'archive',
    default: 'insert_drive_file'
  }
  const category = getFileTypeCategory(type)
  return icons[category] || icons.default
}

// 获取文件扩展名
const getFileExtension = (filename) => {
  if (!filename) return 'unknown'
  const ext = filename.split('.').pop()?.toLowerCase()
  return ext || 'unknown'
}

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).fromNow()
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return '未知'
  const units = ['B', 'KB', 'MB', 'GB']
  let size = bytes
  let unitIndex = 0
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }
  return `${size.toFixed(1)} ${units[unitIndex]}`
}

// 事件处理
const handleStartChat = () => {
  emit('start-chat', props.userId)
}

const handleGroupClick = (group) => {
  emit('group-click', group)
}

const handleMoreAction = (command) => {
  switch (command) {
    case 'share':
      ElMessage.success('名片链接已复制')
      break
    case 'qrcode':
      ElMessage.info('二维码功能开发中')
      break
    case 'block':
      ElMessage.info('加入黑名单功能开发中')
      break
    case 'edit':
      emit('edit')
      break
  }
}

const handleImagePreview = (image) => {
  previewIndex.value = images.value.findIndex(img => img.id === image.id)
  previewVisible.value = true
}

const handleFileClick = (file) => {
  if (file.url) {
    window.open(file.url, '_blank')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.user-profile-space {
  min-height: 100vh;
  background: var(--dt-bg-page);
}

// 头部封面
.profile-header {
  position: relative;
  height: 280px;
  overflow: hidden;

  .cover-image {
    width: 100%;
    height: 100%;
    background-size: cover;
    background-position: center;
  }

  .cover-default {
    width: 100%;
    height: 100%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

    .cover-pattern {
      width: 100%;
      height: 100%;
      opacity: 0.1;
      background-image:
        repeating-linear-gradient(45deg, transparent, transparent 10px, rgba(255,255,255,.1) 10px, rgba(255,255,255,.1) 20px);
    }
  }

  .back-btn {
    position: absolute;
    top: 16px;
    left: 16px;
    background: rgba(0, 0, 0, 0.3);
    color: #fff;
    border: none;
    z-index: 10;

    &:hover {
      background: rgba(0, 0, 0, 0.5);
    }
  }

  .header-actions {
    position: absolute;
    top: 16px;
    right: 16px;
    display: flex;
    gap: 8px;
    z-index: 10;

    .el-button {
      background: rgba(255, 255, 255, 0.9);
      border: none;

      &:hover {
        background: #fff;
      }
    }
  }

  .profile-summary {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 20px 24px;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
    display: flex;
    align-items: flex-end;
    gap: 16px;

    .profile-avatar {
      border: 3px solid #fff;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
    }

    .summary-info {
      flex: 1;
      padding-bottom: 8px;

      .nickname {
        font-size: 24px;
        font-weight: 700;
        color: #fff;
        margin: 0 0 4px 0;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
      }

      .username {
        font-size: 14px;
        color: rgba(255, 255, 255, 0.8);
        margin: 0 0 8px 0;
      }

      .status-badge {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        padding: 4px 10px;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 12px;
        font-size: 12px;
        color: rgba(255, 255, 255, 0.9);

        .dot {
          width: 6px;
          height: 6px;
          border-radius: 50%;
          background: rgba(255, 255, 255, 0.5);
        }

        &.online {
          background: rgba(82, 196, 26, 0.3);
          color: #fff;

          .dot {
            background: #52c41a;
          }
        }
      }
    }
  }
}

// 内容区域
.profile-content {
  max-width: 1200px;
  margin: -20px auto 0;
  padding: 0 20px 40px;
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 24px;
  position: relative;
  z-index: 5;
}

// 侧边栏
.profile-sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-card {
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg);
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .card-title {
    font-size: 15px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0 0 16px 0;
  }

  .info-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .info-item {
    display: flex;
    align-items: flex-start;
    gap: 12px;

    .info-icon {
      font-size: 20px;
      color: var(--dt-brand-color);
      margin-top: 2px;
    }

    .info-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 2px;

      .info-label {
        font-size: 12px;
        color: var(--dt-text-tertiary);
      }

      .info-value {
        font-size: 14px;
        color: var(--dt-text-primary);

        &.signature {
          font-style: italic;
          color: var(--dt-text-secondary);
        }
      }
    }
  }

  .groups-list {
    display: flex;
    flex-direction: column;
    gap: 8px;

    .group-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 8px;
      border-radius: var(--dt-radius-md);
      cursor: pointer;
      transition: all var(--dt-transition-base);

      &:hover {
        background: var(--dt-bg-hover);
      }

      .group-name {
        font-size: 14px;
        color: var(--dt-text-primary);
      }
    }
  }

  .tags-container {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
}

// 主内容区
.profile-main {
  .profile-tabs {
    background: var(--dt-bg-card);
    border-radius: var(--dt-radius-lg);
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    min-height: 400px;

    :deep(.el-tabs__header) {
      margin-bottom: 20px;
    }

    :deep(.el-tabs__item) {
      font-size: 15px;
      font-weight: 500;

      .tab-badge {
        margin-left: 6px;
      }
    }
  }
}

// 动态列表
.activity-list {
  .activity-item {
    display: flex;
    gap: 16px;
    padding: 16px 0;
    border-bottom: 1px solid var(--dt-border-lighter);

    &:last-child {
      border-bottom: none;
    }

    .activity-icon {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;

      &.type-message {
        background: rgba(22, 119, 255, 0.1);
        color: #1677ff;
      }

      &.type-image {
        background: rgba(82, 196, 26, 0.1);
        color: #52c41a;
      }

      &.type-file {
        background: rgba(250, 173, 20, 0.1);
        color: #faad14;
      }

      .material-icons-outlined {
        font-size: 20px;
      }
    }

    .activity-content {
      flex: 1;

      .activity-text {
        font-size: 14px;
        color: var(--dt-text-primary);
        margin-bottom: 4px;

        .activity-user {
          font-weight: 600;
        }

        .activity-action {
          color: var(--dt-text-secondary);
        }
      }

      .activity-time {
        font-size: 12px;
        color: var(--dt-text-tertiary);
      }
    }
  }
}

// 图片画廊
.image-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;

  .gallery-item {
    aspect-ratio: 1;
    border-radius: var(--dt-radius-md);
    overflow: hidden;
    cursor: pointer;
    transition: all var(--dt-transition-base);

    &:hover {
      transform: scale(1.05);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
}

// 文件列表
.file-list {
  display: flex;
  flex-direction: column;
  gap: 8px;

  .file-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    border-radius: var(--dt-radius-md);
    cursor: pointer;
    transition: all var(--dt-transition-base);

    &:hover {
      background: var(--dt-bg-hover);
    }

    .file-icon {
      width: 40px;
      height: 40px;
      border-radius: var(--dt-radius-sm);
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--dt-bg-tertiary);
      color: var(--dt-brand-color);

      .material-icons-outlined {
        font-size: 24px;
      }

      &.type-image {
        background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
        color: #1677ff;
      }

      &.type-pdf {
        background: linear-gradient(135deg, #fff1f0 0%, #ffccc7 100%);
        color: #ff4d4f;
      }

      &.type-doc {
        background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
        color: #52c41a;
      }
    }

    .file-info {
      flex: 1;

      .file-name {
        font-size: 14px;
        color: var(--dt-text-primary);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .file-meta {
        font-size: 12px;
        color: var(--dt-text-tertiary);
      }
    }
  }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--dt-text-tertiary);

  .material-icons-outlined {
    font-size: 64px;
    opacity: 0.5;
    margin-bottom: 16px;
  }

  p {
    font-size: 14px;
    margin: 0;
  }
}

// 暗色模式
.dark .user-profile-space {
  .profile-summary {
    .nickname {
      color: #fff;
    }

    .username {
      color: rgba(255, 255, 255, 0.8);
    }
  }
}

// 响应式
@media (max-width: 768px) {
  .profile-content {
    grid-template-columns: 1fr;
  }

  .profile-header {
    height: 240px;

    .profile-summary {
      flex-direction: column;
      align-items: center;
      text-align: center;

      .summary-info {
        .status-badge {
          display: inline-flex;
        }
      }
    }
  }

  .image-gallery {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: 8px;
  }
}
</style>
