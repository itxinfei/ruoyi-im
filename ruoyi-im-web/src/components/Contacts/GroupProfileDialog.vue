<template>
  <el-dialog
    v-model="visible"
    :width="720"
    class="group-profile-dialog"
    :show-close="false"
    :close-on-click-modal="true"
    destroy-on-close
    append-to-body
  >
    <div v-if="loading" v-loading="loading" class="loading-state"></div>
    <div v-else-if="groupDetail" class="group-container">
      <!-- 关闭按钮 -->
      <button class="close-btn" @click="handleClose">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M18 6L6 18M6 6l12 12" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>

      <!-- 顶部 Header -->
      <div class="group-header">
        <div class="header-content">
          <div class="group-avatar-wrapper">
            <img v-if="groupDetail.avatar" :src="groupDetail.avatar" class="group-avatar-img" />
            <div v-else class="group-avatar-icon">
              <span class="material-icons-outlined">groups</span>
            </div>
          </div>
          <div class="group-meta">
            <h2 class="group-name">{{ groupDetail.name }}</h2>
            <p class="group-info">
              <span v-if="groupDetail.type" class="group-type-badge">
                {{ groupTypeText }}
              </span>
              <span class="group-id">ID: {{ groupDetail.id }}</span>
            </p>
          </div>
        </div>
      </div>

      <div class="group-content">
        <!-- 群公告区域 -->
        <div v-if="groupDetail.notice || groupDetail.announcement" class="section announcement-section">
          <div class="section-title">
            <span class="material-icons-outlined section-icon">campaign</span>
            <span>群公告</span>
          </div>
          <div class="announcement-box">
            {{ groupDetail.notice || groupDetail.announcement }}
          </div>
        </div>

        <!-- 群成员区域 -->
        <div class="section">
          <div class="section-title clickable" @click="handleViewMembers">
            <span class="material-icons-outlined section-icon">people</span>
            <span>群成员 ({{ groupDetail.memberCount || members.length }})</span>
            <span class="material-icons-outlined arrow-icon">chevron_right</span>
          </div>
          <div class="members-preview">
            <div v-for="m in displayMembers" :key="m.id" class="member-item" :title="m.name">
              <DingtalkAvatar
                :src="m.avatar"
                :name="m.name"
                :user-id="m.id"
                :size="40"
                shape="circle"
              />
            </div>
            <div class="member-item add-btn" @click="handleAddMember" title="邀请成员">
              <div class="add-icon">
                <span class="material-icons-outlined">add</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 群组信息 -->
        <div class="section info-section">
          <div class="section-title">
            <span class="material-icons-outlined section-icon">info</span>
            <span>群组信息</span>
          </div>
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">群主</span>
              <span class="info-value">{{ groupDetail.ownerName || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">我的身份</span>
              <span class="info-value">{{ roleText }}</span>
            </div>
            <div v-if="groupDetail.description" class="info-item full-width">
              <span class="info-label">群描述</span>
              <span class="info-value">{{ groupDetail.description }}</span>
            </div>
            <div v-if="groupDetail.createTime" class="info-item">
              <span class="info-label">创建时间</span>
              <span class="info-value">{{ formatDate(groupDetail.createTime) }}</span>
            </div>
          </div>
        </div>

        <!-- 聊天记录入口 -->
        <div class="section actions-section">
          <div class="action-list">
            <div class="action-item clickable" @click="handleViewHistory">
              <span class="material-icons-outlined action-icon">history</span>
              <span class="action-text">查看聊天记录</span>
              <span class="material-icons-outlined arrow-icon">chevron_right</span>
            </div>
            <div class="action-item clickable" @click="handleSearchChat">
              <span class="material-icons-outlined action-icon">search</span>
              <span class="action-text">搜索聊天记录</span>
              <span class="material-icons-outlined arrow-icon">chevron_right</span>
            </div>
            <div class="action-item clickable" @click="handleViewFiles">
              <span class="material-icons-outlined action-icon">folder_open</span>
              <span class="action-text">群文件</span>
              <span class="material-icons-outlined arrow-icon">chevron_right</span>
            </div>
            <div class="action-item clickable" @click="handleQrCode">
              <span class="material-icons-outlined action-icon">qr_code_2</span>
              <span class="action-text">群二维码</span>
              <span class="material-icons-outlined arrow-icon">chevron_right</span>
            </div>
          </div>
        </div>

        <!-- 设置开关 -->
        <div class="section settings-section">
          <div class="setting-item">
            <div class="setting-left">
              <span class="material-icons-outlined setting-icon">push_pin</span>
              <span>置顶聊天</span>
            </div>
            <el-switch
              v-model="groupDetail.isPinned"
              size="small"
              @change="handleTogglePin"
            />
          </div>
          <div class="setting-item">
            <div class="setting-left">
              <span class="material-icons-outlined setting-icon">notifications_off</span>
              <span>消息免打扰</span>
            </div>
            <el-switch
              v-model="groupDetail.isMuted"
              size="small"
              @change="handleToggleMute"
            />
          </div>
        </div>
      </div>

      <!-- 底部操作 -->
      <div class="group-actions">
        <button class="action-btn primary-btn" @click="handleStartChat">
          <span class="material-icons-outlined">chat_bubble</span>
          进入群聊
        </button>
        <button v-if="isOwner || isAdmin" class="action-btn settings-btn" @click="handleGroupSettings">
          <span class="material-icons-outlined">settings</span>
          群设置
        </button>
        <button v-if="isOwner" class="action-btn danger-btn" @click="handleDismiss">
          解散群组
        </button>
        <button v-else class="action-btn danger-btn" @click="handleLeave">
          退出群组
        </button>
      </div>
    </div>

    <!-- 群二维码弹窗 -->
    <el-dialog
      v-model="showQrDialog"
      title="群二维码"
      width="320px"
      :before-close="() => showQrDialog = false"
      center
    >
      <div class="qr-container">
        <div class="qr-placeholder">
          <span class="material-icons-outlined qr-icon">qr_code_2</span>
          <p>扫码加入群聊</p>
        </div>
      </div>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { getGroup, getGroupMembers, dismissGroup, leaveGroup } from '@/api/im/group'
import { createConversation } from '@/api/im/conversation'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import dayjs from 'dayjs'

const props = defineProps({
  modelValue: Boolean,
  groupId: [String, Number]
})

const emit = defineEmits(['update:modelValue', 'refresh', 'history', 'search', 'files'])

const store = useStore()
const visible = ref(false)
const loading = ref(false)
const groupDetail = ref(null)
const members = ref([])
const showQrDialog = ref(false)

const currentUserId = computed(() => store.state.user?.id || store.state.user?.userId)
const isOwner = computed(() => groupDetail.value?.ownerId === currentUserId.value)
const isAdmin = computed(() => groupDetail.value?.myRole === 'ADMIN')

const roleText = computed(() => {
  const role = groupDetail.value?.myRole
  if (role === 'OWNER') return '群主'
  if (role === 'ADMIN') return '管理员'
  return '成员'
})

const groupTypeText = computed(() => {
  const type = groupDetail.value?.type
  if (type === 'PUBLIC') return '公开群'
  if (type === 'PRIVATE') return '私密群'
  return ''
})

// 显示前7个成员 + 邀请按钮
const displayMembers = computed(() => {
  return members.value.slice(0, 7)
})

const handleClose = () => emit('update:modelValue', false)

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD')
}

const loadData = async () => {
  if (!props.groupId) return
  loading.value = true
  try {
    const [gRes, mRes] = await Promise.all([
      getGroup(props.groupId),
      getGroupMembers(props.groupId)
    ])
    if (gRes.code === 200) {
      groupDetail.value = {
        ...gRes.data,
        isMuted: gRes.data.isMuted || false,
        isPinned: gRes.data.isPinned || false
      }
    }
    if (mRes.code === 200) {
      members.value = mRes.data.map(item => ({
        id: item.userId || item.id,
        name: item.userName || item.name || '未知',
        avatar: item.avatar || '',
        role: item.role || 'MEMBER',
        nickname: item.nickname
      }))
    }
  } catch (error) {
    console.error('加载群详情失败:', error)
  } finally {
    loading.value = false
  }
}

const handleStartChat = async () => {
  try {
    const res = await createConversation({ type: 'GROUP', targetId: props.groupId })
    if (res.code === 200) {
      emit('chat', res.data)
      handleClose()
    }
  } catch (e) { ElMessage.error('进入群聊失败') }
}

const handleViewMembers = () => {
  ElMessage.info('查看全部成员功能开发中...')
}

const handleAddMember = () => {
  ElMessage.info('邀请成员功能开发中...')
}

const handleGroupSettings = () => {
  ElMessage.info('群设置功能开发中...')
}

const handleViewHistory = () => {
  emit('history', { groupId: props.groupId })
  handleClose()
}

const handleSearchChat = () => {
  emit('search', { groupId: props.groupId })
  handleClose()
}

const handleViewFiles = () => {
  emit('files', { groupId: props.groupId })
  handleClose()
}

const handleQrCode = () => {
  showQrDialog.value = true
}

const handleToggleMute = async (val) => {
  ElMessage.success(val ? '已开启消息免打扰' : '已取消消息免打扰')
  // TODO: 调用 API 更新状态
}

const handleTogglePin = async (val) => {
  ElMessage.success(val ? '已置顶会话' : '已取消置顶')
  // TODO: 调用 API 更新状态
}

const handleDismiss = async () => {
  try {
    await ElMessageBox.confirm('确定要解散该群组吗？此操作不可撤销。', '解散群组', { type: 'error' })
    await dismissGroup(props.groupId)
    ElMessage.success('已解散群组')
    emit('refresh')
    handleClose()
  } catch (e) {}
}

const handleLeave = async () => {
  try {
    await ElMessageBox.confirm('确定要退出该群组吗？', '退出群组', { type: 'warning' })
    await leaveGroup(props.groupId)
    ElMessage.success('已退出群组')
    emit('refresh')
    handleClose()
  } catch (e) {}
}

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.groupId) loadData()
})

watch(visible, (val) => {
  if (!val) emit('update:modelValue', false)
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

:deep(.group-profile-dialog) {
  .el-dialog {
    border-radius: 16px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15), 0 0 0 1px rgba(0, 0, 0, 0.05);
    animation: dialogFadeIn 0.25s cubic-bezier(0.4, 0, 0.2, 1);
    overflow: hidden;
  }

  .el-dialog__body {
    padding: 0;
  }
}

@keyframes dialogFadeIn {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(-10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.loading-state {
  min-height: 450px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.group-container {
  background: var(--dt-bg-card);
  max-height: 70vh;
  display: flex;
  gap: 28px;
  padding: 28px;

  .dark & {
    background: var(--dt-bg-card-dark);
  }
}

.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.03);
  border: none;
  color: var(--dt-text-secondary);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 10;

  svg {
    width: 18px;
    height: 18px;
    transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  }

  &:hover {
    background: rgba(0, 0, 0, 0.08);
    color: var(--dt-text-primary);
    transform: scale(1.05);

    svg {
      transform: rotate(90deg);
    }
  }

  &:active {
    transform: scale(0.95);
  }

  .dark & {
    background: rgba(255, 255, 255, 0.05);

    &:hover {
      background: rgba(255, 255, 255, 0.1);
    }
  }
}

// ============================================================================
// Header (左侧)
// ============================================================================
.group-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 180px;
  text-align: center;

  .header-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
  }

  .group-avatar-wrapper {
    width: 80px;
    height: 80px;
    flex-shrink: 0;

    .group-avatar-img {
      width: 100%;
      height: 100%;
      border-radius: 16px;
      object-fit: cover;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    }

    .group-avatar-icon {
      width: 100%;
      height: 100%;
      border-radius: 16px;
      background: linear-gradient(135deg, #1677ff 0%, #0e5fd9 100%);
      color: #fff;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 16px rgba(22, 119, 255, 0.3);

      .material-icons-outlined {
        font-size: 40px;
      }
    }
  }

  .group-meta {
    flex: 1;
    min-width: 0;

    .group-name {
      margin: 0 0 6px 0;
      font-size: 18px;
      font-weight: 600;
      color: var(--dt-text-primary);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      max-width: 180px;
    }

    .group-info {
      margin: 0;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;

      .group-type-badge {
        font-size: 11px;
        padding: 2px 8px;
        border-radius: 4px;
        background: rgba(22, 119, 255, 0.1);
        color: var(--dt-brand-color);
        font-weight: 500;
      }

      .group-id {
        font-size: 12px;
        color: var(--dt-text-tertiary);
      }
    }
  }
}

// ============================================================================
// Content (右侧)
// ============================================================================
.group-content {
  flex: 1;
  min-width: 0;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-thumb {
    background: var(--dt-border-color);
    border-radius: 3px;
  }
}

.section {
  margin-bottom: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0 0 12px 0;

  .section-icon {
    font-size: 18px;
    color: var(--dt-brand-color);
  }

  &.clickable {
    cursor: pointer;

    &:hover {
      color: var(--dt-brand-color);
    }
  }

  .arrow-icon {
    margin-left: auto;
    font-size: 18px;
    color: var(--dt-text-quaternary);
  }
}

// ============================================================================
// Announcement
// ============================================================================
.announcement-box {
  padding: 14px 16px;
  background: rgba(22, 119, 255, 0.05);
  border: 1px solid rgba(22, 119, 255, 0.1);
  border-radius: 10px;
  font-size: 14px;
  color: var(--dt-text-primary);
  line-height: 1.6;

  .dark & {
    background: rgba(22, 119, 255, 0.1);
    border-color: rgba(22, 119, 255, 0.2);
  }
}

// ============================================================================
// Members
// ============================================================================
.members-preview {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;

  .member-item {
    width: 44px;
    height: 44px;
    flex-shrink: 0;
    cursor: pointer;
    transition: transform 0.2s;

    &:hover {
      transform: scale(1.1);
    }

    &.add-btn {
      .add-icon {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        border: 1px dashed var(--dt-border-color);
        display: flex;
        align-items: center;
        justify-content: center;
        color: var(--dt-text-tertiary);
        margin: 2px;

        .material-icons-outlined {
          font-size: 20px;
        }

        &:hover {
          border-color: var(--dt-brand-color);
          color: var(--dt-brand-color);
        }
      }
    }
  }
}

// ============================================================================
// Info List
// ============================================================================
.info-list {
  background: var(--dt-bg-body);
  border: 1px solid var(--dt-border-light);
  border-radius: 10px;
  overflow: hidden;

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
}

.info-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid var(--dt-border-light);

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }

  &:last-child {
    border-bottom: none;
  }

  &.full-width {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }
}

.info-label {
  font-size: 14px;
  color: var(--dt-text-secondary);
  min-width: 70px;
}

.info-value {
  flex: 1;
  font-size: 15px;
  color: var(--dt-text-primary);
}

// ============================================================================
// Action List
// ============================================================================
.action-list {
  background: var(--dt-bg-body);
  border: 1px solid var(--dt-border-light);
  border-radius: 10px;
  overflow: hidden;

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }
}

.action-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid var(--dt-border-light);
  cursor: pointer;
  transition: background 0.2s;

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: var(--dt-bg-hover);
  }

  .action-icon {
    font-size: 20px;
    color: var(--dt-brand-color);
    margin-right: 12px;
  }

  .action-text {
    flex: 1;
    font-size: 15px;
    color: var(--dt-text-primary);
  }

  .arrow-icon {
    font-size: 18px;
    color: var(--dt-text-quaternary);
  }
}

// ============================================================================
// Settings
// ============================================================================
.settings-section {
  .setting-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 14px 16px;
    background: var(--dt-bg-body);
    border: 1px solid var(--dt-border-light);
    border-radius: 10px;
    margin-bottom: 8px;

    .dark & {
      background: var(--dt-bg-card-dark);
      border-color: var(--dt-border-dark);
    }

    &:last-child {
      margin-bottom: 0;
    }

    .setting-left {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 15px;
      color: var(--dt-text-primary);

      .setting-icon {
        font-size: 18px;
        color: var(--dt-text-secondary);
      }
    }
  }
}

// ============================================================================
// Actions Footer (左侧)
// ============================================================================
.group-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 180px;

  .action-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    height: 42px;
    border: none;
    border-radius: 10px;
    font-size: 15px;
    cursor: pointer;
    transition: all 0.2s;

    .material-icons-outlined {
      font-size: 20px;
    }
  }

  .primary-btn {
    background: var(--dt-brand-color);
    color: #fff;

    &:hover {
      opacity: 0.9;
    }
  }

  .settings-btn {
    background: var(--dt-bg-body);
    color: var(--dt-text-primary);

    &:hover {
      background: var(--dt-bg-hover);
    }

    .dark & {
      background: var(--dt-bg-card-dark);
    }
  }

  .danger-btn {
    background: transparent;
    color: var(--dt-error-color);
    border: 1px solid var(--dt-error-color);

    &:hover {
      background: var(--dt-error-color);
      color: #fff;
    }
  }
}

// ============================================================================
// QR Dialog
// ============================================================================
.qr-container {
  display: flex;
  justify-content: center;
  padding: 20px;

  .qr-placeholder {
    text-align: center;

    .qr-icon {
      font-size: 120px;
      color: var(--dt-border-color);
    }

    p {
      margin: 16px 0 0;
      font-size: 14px;
      color: var(--dt-text-secondary);
    }
  }
}
</style>
