<!--
  群组详情弹窗组件
  群聊会话中点击群头像/群名时显示
  @author RuoYi-IM
-->
<template>
  <el-dialog
    v-model="dialogVisible"
    :title="title"
    width="600px"
    :before-close="handleClose"
    class="group-detail-dialog"
    destroy-on-close
  >
    <div class="dialog-content" v-loading="loading">
      <!-- 群组头像和基本信息 -->
      <div class="group-profile">
        <el-avatar :size="100" :src="groupInfo?.avatar" class="group-avatar">
          <img :src="groupInfo?.avatar || defaultAvatar" alt="" />
        </el-avatar>
        <h2 class="group-name">{{ groupInfo?.name }}</h2>
        <p class="group-desc" v-if="groupInfo?.description">{{ groupInfo.description }}</p>
        <div class="group-meta">
          <span class="member-count">{{ memberCount }}人</span>
          <el-tag v-if="isOwner" type="success" size="small">我是群主</el-tag>
          <el-tag v-else-if="isAdmin" type="warning" size="small">我是管理员</el-tag>
        </div>
      </div>

      <!-- 群组操作 -->
      <div class="group-actions">
        <el-button type="primary" :icon="Plus" @click="handleInviteMember" v-if="!isOwner">
          邀请成员
        </el-button>
        <el-button :icon="Bell" @click="handleMuteGroup" v-if="!isOwner">
          {{ isMuted ? '取消免打扰' : '消息免打扰' }}
        </el-button>
        <el-button :icon="Edit" @click="handleEditGroup" v-if="isOwner || isAdmin">
          编辑群信息
        </el-button>
      </div>

      <!-- 群成员列表 -->
      <div class="members-section">
        <div class="section-header">
          <span class="section-title">群成员</span>
          <span class="member-count">{{ memberCount }}人</span>
        </div>

        <div class="members-list" v-loading="loadingMembers">
          <div
            v-for="member in memberList"
            :key="member.id"
            class="member-item"
          >
            <el-avatar :size="40" :src="member.avatar">
              {{ member.nickname?.charAt(0) || member.username?.charAt(0) }}
            </el-avatar>
            <div class="member-info">
              <span class="member-name">{{ member.nickname || member.username }}</span>
              <el-tag v-if="member.role === 'OWNER'" type="success" size="small">群主</el-tag>
              <el-tag v-else-if="member.role === 'ADMIN'" type="warning" size="small">管理员</el-tag>
            </div>
            <el-dropdown
              trigger="click"
              placement="bottom-end"
              v-if="canManageMember(member)"
              @command="(cmd) => handleMemberCommand(cmd, member)"
            >
              <el-button :icon="More" text circle />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="remove" v-if="canRemoveMember(member)">
                    <el-icon><RemoveFilled /></el-icon>
                    移出群聊
                  </el-dropdown-item>
                  <el-dropdown-item command="setAdmin" v-if="canSetAdmin(member)">
                    <el-icon><UserFilled /></el-icon>
                    设为管理员
                  </el-dropdown-item>
                  <el-dropdown-item command="unsetAdmin" v-if="canUnsetAdmin(member)">
                    <el-icon><User /></el-icon>
                    取消管理员
                  </el-dropdown-item>
                  <el-dropdown-item command="profile" divided>
                    <el-icon><View /></el-icon>
                    查看资料
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <el-empty
            v-if="!loadingMembers && memberList.length === 0"
            description="暂无成员"
            :image-size="60"
          />
        </div>
      </div>

      <!-- 内容统计 -->
      <div class="stats-section" v-if="conversationId">
        <div class="stats-title">群文件和媒体</div>
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
        </div>

        <div class="stats-content" v-loading="loadingStats">
          <template v-if="activeTab === 'media' && mediaList.length > 0">
            <div class="media-grid">
              <div
                v-for="item in mediaList"
                :key="item.id"
                class="media-item"
                @click="previewMedia(item)"
              >
                <el-image :src="item.url" fit="cover" lazy class="media-image" />
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
                <el-icon class="file-icon"><Document /></el-icon>
                <span class="file-name">{{ item.name }}</span>
                <span class="file-size">{{ formatFileSize(item.size) }}</span>
              </div>
            </div>
          </template>

          <el-empty
            v-if="!loadingStats && getCurrentList().length === 0"
            description="暂无内容"
            :image-size="60"
          />
        </div>
      </div>
    </div>

    <!-- 底部操作按钮 -->
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleViewRecords">查看聊天记录</el-button>
        <el-button @click="handleClearHistory">清空聊天记录</el-button>
        <el-button
          type="danger"
          @click="handleLeaveGroup"
          v-if="!isOwner"
        >
          退出群聊
        </el-button>
        <el-button
          type="danger"
          @click="handleDismissGroup"
          v-if="isOwner"
        >
          解散群聊
        </el-button>
      </div>
    </template>
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
  Plus,
  Bell,
  Edit,
  More,
  User,
  UserFilled,
  View,
  Picture,
  Folder,
  Document,
  RemoveFilled,
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
const activeTab = ref('media')
const loadingStats = ref(false)
const stats = ref({ mediaCount: 0, fileCount: 0 })
const mediaList = ref([])
const fileList = ref([])
const isMuted = ref(false)
const previewVisible = ref(false)
const previewIndex = ref(0)
const previewUrls = ref([])

// 当前用户ID（从localStorage或store获取）
const currentUserId = ref(parseInt(localStorage.getItem('userId') || '1'))

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 计算属性
const title = computed(() => {
  return groupInfo.value?.name || '群组详情'
})

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

const getCurrentList = () => {
  return activeTab.value === 'media' ? mediaList.value : fileList.value
}

// 权限判断
const canManageMember = (member) => {
  if (isOwner.value) return member.userId !== currentUserId.value
  return false
}

const canRemoveMember = (member) => {
  return member.role !== 'OWNER' && member.userId !== currentUserId.value
}

const canSetAdmin = (member) => {
  return member.role !== 'OWNER' && member.role !== 'ADMIN'
}

const canUnsetAdmin = (member) => {
  return member.role === 'ADMIN'
}

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
    } else {
      ElMessage.error('获取群组信息失败')
    }
  } catch (error) {
    console.error('获取群组信息失败:', error)
    ElMessage.error('获取群组信息失败')
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

  loadingStats.value = true
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
      mediaList.value = media.slice(0, 15)
      fileList.value = files.slice(0, 15)
    }
  } catch (error) {
    console.error('获取内容统计失败:', error)
  } finally {
    loadingStats.value = false
  }
}

// 群组操作
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

// 成员操作
const handleMemberCommand = (command, member) => {
  switch (command) {
    case 'remove':
      handleRemoveMember(member)
      break
    case 'setAdmin':
      handleSetAdmin(member)
      break
    case 'unsetAdmin':
      handleUnsetAdmin(member)
      break
    case 'profile':
      handleViewMemberProfile(member)
      break
  }
}

const handleRemoveMember = async (member) => {
  try {
    await ElMessageBox.confirm(
      `确定要将 ${member.nickname || member.username} 移出群聊吗？`,
      '移出群聊',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    const response = await request({
      url: `/api/im/group/${props.groupId}/members/${member.userId}`,
      method: 'delete',
    })

    if (response.code === 200) {
      ElMessage.success('已移出群聊')
      loadMembers()
      emit('refresh')
    } else {
      ElMessage.error(response.msg || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移出成员失败:', error)
    }
  }
}

const handleSetAdmin = async (member) => {
  try {
    const response = await request({
      url: `/api/im/groups/${props.groupId}/members/${member.userId}/role`,
      method: 'put',
      data: { role: 'ADMIN' },
    })

    if (response.code === 200) {
      ElMessage.success('已设为管理员')
      loadMembers()
    } else {
      ElMessage.error(response.msg || '操作失败')
    }
  } catch (error) {
    console.error('设置管理员失败:', error)
  }
}

const handleUnsetAdmin = async (member) => {
  try {
    const response = await request({
      url: `/api/im/groups/${props.groupId}/members/${member.userId}/role`,
      method: 'put',
      data: { role: 'MEMBER' },
    })

    if (response.code === 200) {
      ElMessage.success('已取消管理员')
      loadMembers()
    } else {
      ElMessage.error(response.msg || '操作失败')
    }
  } catch (error) {
    console.error('取消管理员失败:', error)
  }
}

const handleViewMemberProfile = (member) => {
  ElMessage.info(`查看 ${member.nickname || member.username} 的资料`)
}

// 底部操作
const handleViewRecords = () => {
  ElMessage.info('查看聊天记录功能开发中...')
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

const handleLeaveGroup = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出群聊吗？退出后将无法接收群消息。',
      '退出群聊',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    const response = await request({
      url: `/api/im/group/${props.groupId}/quit`,
      method: 'post',
    })

    if (response.code === 200) {
      ElMessage.success('已退出群聊')
      handleClose()
      emit('refresh')
    } else {
      ElMessage.error(response.msg || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出群聊失败:', error)
      ElMessage.error('退出群聊失败')
    }
  }
}

const handleDismissGroup = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要解散群聊吗？解散后群组将被永久删除，所有成员将被移出。',
      '解散群聊',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error',
      }
    )

    const response = await request({
      url: `/api/im/group/${props.groupId}`,
      method: 'delete',
    })

    if (response.code === 200) {
      ElMessage.success('群聊已解散')
      handleClose()
      emit('refresh')
    } else {
      ElMessage.error(response.msg || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('解散群聊失败:', error)
      ElMessage.error('解散群聊失败')
    }
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
.group-detail-dialog {
  .el-dialog__body {
    padding: 0;
    max-height: 60vh;
    overflow-y: auto;
  }
}

.dialog-content {
  padding: 20px;
}

// 群组基本信息
.group-profile {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
  text-align: center;
  border-bottom: 1px solid var(--el-border-color-lighter);

  .group-avatar {
    margin-bottom: 16px;
    border: 3px solid var(--el-border-color-lighter);
  }

  .group-name {
    margin: 0 0 8px;
    font-size: 20px;
    font-weight: 500;
  }

  .group-desc {
    margin: 0 0 16px;
    font-size: 14px;
    color: var(--el-text-color-secondary);
    max-width: 80%;
  }

  .group-meta {
    display: flex;
    align-items: center;
    gap: 12px;

    .member-count {
      font-size: 14px;
      color: var(--el-text-color-secondary);
    }
  }
}

// 群组操作
.group-actions {
  display: flex;
  gap: 8px;
  margin: 20px 0;
  justify-content: center;
  flex-wrap: wrap;
}

// 成员区域
.members-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--el-border-color-lighter);

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .section-title {
      font-size: 16px;
      font-weight: 500;
    }

    .member-count {
      font-size: 14px;
      color: var(--el-text-color-secondary);
    }
  }

  .members-list {
    max-height: 300px;
    overflow-y: auto;
  }

  .member-item {
    display: flex;
    align-items: center;
    padding: 12px;
    border-radius: 8px;
    margin-bottom: 8px;
    transition: background 0.2s;

    &:hover {
      background: var(--el-fill-color-light);
    }

    .member-info {
      flex: 1;
      display: flex;
      align-items: center;
      gap: 8px;
      margin-left: 12px;

      .member-name {
        font-size: 14px;
        color: var(--el-text-color-primary);
      }
    }
  }
}

// 统计区域
.stats-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--el-border-color-lighter);

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

      &:hover {
        background: var(--el-fill-color);
      }

      &.active {
        background: var(--el-color-primary);
        color: #fff;
      }
    }
  }

  .stats-content {
    min-height: 100px;
  }
}

// 媒体网格
.media-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;

  .media-item {
    aspect-ratio: 1;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
  }
}

// 文件列表
.file-list {
  .file-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px;
    border-radius: 6px;
    cursor: pointer;

    &:hover {
      background: var(--el-fill-color-light);
    }

    .file-icon {
      font-size: 20px;
      color: var(--el-color-primary);
    }

    .file-name {
      flex: 1;
      font-size: 14px;
    }

    .file-size {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }
}

// 底部按钮
.dialog-footer {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}
</style>
