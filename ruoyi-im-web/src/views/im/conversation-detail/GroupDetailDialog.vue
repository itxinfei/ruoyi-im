<!--
  群组详情弹窗组件 - 钉钉5.6风格
  群聊会话中点击群头像/群名时显示
  @author RuoYi-IM
-->
<template>
  <el-dialog
    v-model="dialogVisible"
    :show-close="false"
    :close-on-click-modal="true"
    width="560px"
    class="ding-group-detail-dialog"
    destroy-on-close
    @close="handleClose"
  >
    <!-- 头部区域 -->
    <template #header="{ close }">
      <div class="dialog-header">
        <span class="header-title">群聊信息</span>
        <el-icon class="header-close" @click="close">
          <Close />
        </el-icon>
      </div>
    </template>

    <div class="dialog-body" v-loading="loading">
      <!-- 群组基本信息卡片 -->
      <div class="group-card">
        <div class="group-avatar-wrap">
          <el-avatar :size="64" :src="groupInfo?.avatar">
            <img :src="groupInfo?.avatar || defaultAvatar" alt="" />
          </el-avatar>
        </div>
        <div class="group-basic-info">
          <h3 class="group-name">{{ groupInfo?.name }}</h3>
          <p class="group-desc" v-if="groupInfo?.description">{{ groupInfo.description }}</p>
          <div class="group-meta">
            <span class="member-count">{{ memberCount }}人</span>
            <el-tag v-if="isOwner" type="success" size="small">我是群主</el-tag>
            <el-tag v-else-if="isAdmin" type="warning" size="small">我是管理员</el-tag>
          </div>
        </div>
      </div>

      <!-- 快捷操作 -->
      <div class="quick-actions">
        <div class="action-item" @click="handleInviteMember">
          <el-icon><UserFilled /></el-icon>
          <span>邀请成员</span>
        </div>
        <div class="action-item" @click="handleMuteGroup">
          <el-icon><Bell /></el-icon>
          <span>{{ isMuted ? '取消免打扰' : '消息免打扰' }}</span>
        </div>
        <div class="action-item" @click="handleEditGroup" v-if="isOwner || isAdmin">
          <el-icon><Edit /></el-icon>
          <span>编辑群信息</span>
        </div>
      </div>

      <!-- 群成员列表 -->
      <div class="members-section">
        <div class="section-header">
          <span class="section-title">群成员</span>
          <span class="member-count-badge">{{ memberCount }}</span>
        </div>

        <div class="members-list" v-loading="loadingMembers">
          <div
            v-for="member in displayMembers"
            :key="member.id"
            class="member-item"
          >
            <el-avatar :size="36" :src="member.avatar">
              {{ member.nickname?.charAt(0) || member.username?.charAt(0) }}
            </el-avatar>
            <div class="member-info">
              <span class="member-name">{{ member.nickname || member.username }}</span>
              <span class="member-role" v-if="member.role === 'OWNER'">群主</span>
              <span class="member-role admin" v-else-if="member.role === 'ADMIN'">管理员</span>
            </div>
          </div>

          <div class="show-more" v-if="memberList.length > displayLimit" @click="showAllMembers = !showAllMembers">
            <span>{{ showAllMembers ? '收起' : `查看全部${memberCount}人` }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>
        </div>
      </div>

      <!-- 群文件和媒体 -->
      <div class="content-section" v-if="conversationId && hasContent">
        <div class="section-header">
          <span class="section-title">群文件</span>
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
        </div>
      </div>
    </div>

    <!-- 底部操作按钮 -->
    <template #footer>
      <div class="dialog-footer">
        <el-button class="footer-btn danger-btn" @click="handleLeaveOrDismiss">
          {{ isOwner ? '解散群聊' : '退出群聊' }}
        </el-button>
        <el-button class="footer-btn" @click="handleClearHistory">
          清空聊天记录
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
  UserFilled,
  Bell,
  Edit,
  ArrowDown,
  Picture,
  Folder,
  ArrowRight,
  Document,
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  groupId: {
    type: [Number, String],
    default: null,
  },
  conversationId: {
    type: [Number, String],
    default: null,
  },
})

// Emits
const emit = defineEmits(['update:visible', 'refresh'])

// 状态
const dialogVisible = ref(false)
const groupInfo = ref(null)
const loading = ref(false)
const memberList = ref([])
const loadingMembers = ref(false)
const showAllMembers = ref(false)
const displayLimit = 5
const isMuted = ref(false)
const stats = ref({ mediaCount: 0, fileCount: 0 })
const mediaList = ref([])
const fileList = ref([])

// 内容详情弹窗
const contentDialogVisible = ref(false)
const contentType = ref('')
const contentTitle = computed(() => {
  const titles = { media: '图片和视频', file: '文件' }
  return titles[contentType.value] || ''
})

// 图片预览
const previewVisible = ref(false)
const previewIndex = ref(0)
const previewUrls = ref([])

// 当前用户ID
const currentUserId = ref(parseInt(localStorage.getItem('userId') || '1'))

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 计算属性
const memberCount = computed(() => {
  return groupInfo.value?.memberCount || memberList.value.length || 0
})

const isOwner = computed(() => {
  return groupInfo.value?.ownerId === currentUserId.value
})

const isAdmin = computed(() => {
  const myMember = memberList.value.find(m => m.userId === currentUserId.value)
  return myMember?.role === 'ADMIN'
})

const displayMembers = computed(() => {
  if (showAllMembers.value) {
    return memberList.value
  }
  return memberList.value.slice(0, displayLimit)
})

const hasContent = computed(() => {
  return stats.value.mediaCount > 0 || stats.value.fileCount > 0
})

// 监听 visible 变化
watch(() => props.visible, (val) => {
  dialogVisible.value = val
  if (val && props.groupId) {
    loadGroupInfo()
    loadMembers()
    if (props.conversationId) {
      loadContentStats()
    }
  }
})

watch(dialogVisible, (val) => {
  emit('update:visible', val)
})

// 加载群组信息
const loadGroupInfo = async () => {
  if (!props.groupId) return

  loading.value = true
  try {
    const response = await request({
      url: `/api/im/group/${props.groupId}`,
      method: 'get',
    })
    if (response.code === 200) {
      groupInfo.value = response.data
    }
  } catch (error) {
    console.error('获取群组信息失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载成员列表
const loadMembers = async () => {
  if (!props.groupId) return

  loadingMembers.value = true
  try {
    const response = await request({
      url: `/api/im/groups/${props.groupId}/members`,
      method: 'get',
    })
    if (response.code === 200) {
      memberList.value = response.data || []
    }
  } catch (error) {
    console.error('获取成员列表失败:', error)
    // 如果接口不存在，使用群组信息中的成员
    if (groupInfo.value?.members) {
      memberList.value = groupInfo.value.members
    }
  } finally {
    loadingMembers.value = false
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

      messages.forEach(msg => {
        let content = {}
        try {
          content = typeof msg.content === 'string' ? JSON.parse(msg.content) : msg.content
        } catch {
          content = {}
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
        }
      })

      stats.value = {
        mediaCount: media.length,
        fileCount: files.length,
      }
      mediaList.value = media
      fileList.value = files
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

// 操作处理
const handleInviteMember = () => {
  ElMessage.info('邀请成员功能开发中...')
}

const handleMuteGroup = () => {
  isMuted.value = !isMuted.value
  ElMessage.success(isMuted.value ? '已开启消息免打扰' : '已取消消息免打扰')
}

const handleEditGroup = () => {
  ElMessage.info('编辑群信息功能开发中...')
}

const handleLeaveOrDismiss = async () => {
  const action = isOwner.value ? '解散群聊' : '退出群聊'
  const confirmMsg = isOwner.value
    ? '确定要解散群聊吗？解散后群组将被永久删除，所有成员将被移出。'
    : '确定要退出群聊吗？退出后将无法接收群消息。'

  try {
    await ElMessageBox.confirm(confirmMsg, action, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: isOwner.value ? 'error' : 'warning',
    })

    let url = isOwner.value
      ? `/api/im/group/${props.groupId}`
      : `/api/im/group/${props.groupId}/quit`
    let method = isOwner.value ? 'delete' : 'post'

    const response = await request({ url, method })

    if (response.code === 200) {
      ElMessage.success(`已${action}`)
      handleClose()
      emit('refresh')
    } else {
      ElMessage.error(response.msg || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${action}失败:`, error)
      ElMessage.error(`${action}失败`)
    }
  }
}

const handleClearHistory = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空聊天记录吗？此操作不可恢复。',
      '清空聊天记录',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    ElMessage.success('聊天记录已清空')
  } catch {
    // 取消操作
  }
}

const handleClose = () => {
  dialogVisible.value = false
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
.ding-group-detail-dialog {
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

  .header-title {
    font-size: 16px;
    font-weight: 500;
    color: #1d1d1f;
  }

  .header-close {
    font-size: 20px;
    color: #858585;
    cursor: pointer;

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

// 群组卡片
.group-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 12px;
  margin-bottom: 20px;

  .group-basic-info {
    flex: 1;

    .group-name {
      margin: 0 0 4px;
      font-size: 18px;
      font-weight: 500;
      color: #1d1d1f;
    }

    .group-desc {
      margin: 0 0 8px;
      font-size: 14px;
      color: #858585;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      max-width: 300px;
    }

    .group-meta {
      display: flex;
      align-items: center;
      gap: 8px;

      .member-count {
        font-size: 14px;
        color: #858585;
      }
    }
  }
}

// 快捷操作
.quick-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;

  .action-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 12px;
    background: #f5f5f5;
    border-radius: 10px;
    cursor: pointer;
    transition: background 0.2s;

    &:hover {
      background: #e8e8e8;
    }

    .el-icon {
      font-size: 20px;
      color: #0089ff;
      margin-bottom: 6px;
    }

    span {
      font-size: 13px;
      color: #1d1d1f;
    }
  }
}

// 成员区域
.members-section {
  margin-bottom: 20px;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .section-title {
      font-size: 14px;
      font-weight: 500;
      color: #1d1d1f;
    }

    .member-count-badge {
      font-size: 13px;
      color: #858585;
      background: #f5f5f5;
      padding: 2px 8px;
      border-radius: 10px;
    }
  }

  .members-list {
    .member-item {
      display: flex;
      align-items: center;
      padding: 10px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .member-info {
        flex: 1;
        display: flex;
        align-items: center;
        gap: 8px;
        margin-left: 12px;

        .member-name {
          font-size: 15px;
          color: #1d1d1f;
        }

        .member-role {
          font-size: 12px;
          color: #52c41a;
          background: #f0f9ff;
          padding: 2px 6px;
          border-radius: 4px;

          &.admin {
            color: #faad14;
            background: #fffbe6;
          }
        }
      }
    }

    .show-more {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 4px;
      padding: 12px;
      cursor: pointer;
      color: #0089ff;
      font-size: 14px;

      .el-icon {
        transition: transform 0.2s;
      }
    }
  }
}

// 内容区域
.content-section {
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

    &.danger-btn {
      background: #ffeef0;
      color: #ff4d4f;

      &:hover {
        background: #ffd6d9;
      }
    }
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
}
</style>
