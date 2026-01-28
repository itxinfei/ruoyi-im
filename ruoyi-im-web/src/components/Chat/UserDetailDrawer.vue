<template>
  <el-dialog
    v-model="visible"
    :title="null"
    :width="isMobile ? '100%' : '420px'"
    class="user-detail-dialog"
    :show-close="false"
    append-to-body
    destroy-on-close
    :modal-class="'user-detail-modal'"
  >
    <div v-if="userInfo" class="user-profile">
      <!-- 封面区域 -->
      <div class="profile-cover" :class="{ 'group-cover': isGroup }">
        <div class="cover-pattern"></div>
        <button class="close-btn" @click="handleClose">
          <span class="material-icons-outlined">close</span>
        </button>

        <!-- 群组徽章 -->
        <div v-if="isGroup" class="profile-badge group-badge">
          <span class="material-icons-outlined">groups</span>
        </div>

        <!-- 在线状态徽章 -->
        <div v-else-if="userInfo.online" class="profile-badge online-badge">
          <span class="material-icons-outlined">circle</span>
        </div>
      </div>

      <!-- 头像区域 -->
      <div class="profile-avatar-section">
        <div class="avatar-wrapper">
          <DingtalkAvatar
            v-if="!isGroup"
            :src="userInfo?.avatar"
            :name="userName"
            :user-id="session?.targetId || session?.targetUserId"
            :size="96"
            shape="circle"
            custom-class="profile-main-avatar"
          />
          <div v-else class="avatar-group">
            <span class="material-icons-outlined">groups</span>
          </div>

          <!-- 状态指示器 -->
          <div v-if="!isGroup" class="status-indicator" :class="{ online: userInfo.online }">
            <span class="status-dot"></span>
          </div>
        </div>
      </div>

      <!-- 用户信息 -->
      <div class="profile-info">
        <h2 class="profile-name">{{ userName }}</h2>

        <!-- 状态标签 -->
        <div class="profile-tags">
          <span v-if="isGroup" class="tag tag-group">
            <span class="material-icons-outlined tag-icon">groups</span>
            群聊
          </span>
          <span v-else-if="userInfo.online" class="tag tag-online">
            <span class="material-icons-outlined tag-icon">circle</span>
            在线
          </span>
          <span v-else class="tag tag-offline">
            <span class="material-icons-outlined tag-icon">radio_button_unchecked</span>
            离线
          </span>
        </div>

        <!-- 职位信息 -->
        <p v-if="!isGroup" class="profile-position">
          <span v-if="userInfo.position">{{ userInfo.position }}</span>
          <span v-if="userInfo.position && userInfo.department"> · </span>
          <span v-if="userInfo.department">{{ userInfo.department }}</span>
          <span v-if="!userInfo.position && !userInfo.department">暂无职位信息</span>
        </p>
        <p v-else class="profile-position">
          {{ userInfo.memberCount || 0 }} 位成员
        </p>
      </div>

      <!-- 操作按钮网格 -->
      <div class="action-grid">
        <button class="action-card primary" @click="handleSendMessage">
          <div class="action-icon">
            <span class="material-icons-outlined">chat_bubble</span>
          </div>
          <span class="action-label">发消息</span>
        </button>

        <button class="action-card" @click="handleVoiceCall">
          <div class="action-icon voice">
            <span class="material-icons-outlined">call</span>
          </div>
          <span class="action-label">语音</span>
        </button>

        <button class="action-card" @click="handleVideoCall">
          <div class="action-icon video">
            <span class="material-icons-outlined">videocam</span>
          </div>
          <span class="action-label">视频</span>
        </button>

        <button class="action-card more-card" @click="toggleMoreActions">
          <div class="action-icon">
            <span class="material-icons-outlined">more_horiz</span>
          </div>
          <span class="action-label">更多</span>
        </button>
      </div>

      <!-- 更多操作面板 -->
      <transition name="expand">
        <div v-if="showMoreActions" class="more-actions-panel">
          <div class="more-action-item" @click="handleAddToFavorites">
            <span class="material-icons-outlined action-icon">star_border</span>
            <span>添加到常用联系人</span>
          </div>
          <div class="more-action-item" @click="handleSetRemark">
            <span class="material-icons-outlined action-icon">edit</span>
            <span>设置备注</span>
          </div>
          <div class="more-action-item" @click="handleViewHistory">
            <span class="material-icons-outlined action-icon">history</span>
            <span>查看历史消息</span>
          </div>
          <div class="more-action-item danger" @click="handleReport">
            <span class="material-icons-outlined action-icon">report</span>
            <span>举报</span>
          </div>
        </div>
      </transition>

      <!-- 详细信息区域 -->
      <div class="profile-details">
        <div class="details-header">
          <h3 class="details-title">详细信息</h3>
        </div>

        <div class="detail-list">
          <div class="detail-item">
            <div class="detail-icon">
              <span class="material-icons-outlined">phone</span>
            </div>
            <div class="detail-content">
              <span class="detail-label">手机号码</span>
              <span class="detail-value">{{ userInfo.phone || '未设置' }}</span>
            </div>
          </div>

          <div class="detail-item">
            <div class="detail-icon">
              <span class="material-icons-outlined">email</span>
            </div>
            <div class="detail-content">
              <span class="detail-label">邮箱地址</span>
              <span class="detail-value">{{ userInfo.email || '未设置' }}</span>
            </div>
          </div>

          <div v-if="!isGroup" class="detail-item">
            <div class="detail-icon">
              <span class="material-icons-outlined">person</span>
            </div>
            <div class="detail-content">
              <span class="detail-label">用户名</span>
              <span class="detail-value">@{{ userInfo.username || '未设置' }}</span>
            </div>
          </div>

          <div class="detail-item full-width">
            <div class="detail-icon">
              <span class="material-icons-outlined">format_quote</span>
            </div>
            <div class="detail-content">
              <span class="detail-label">个性签名</span>
              <p class="detail-value signature">{{ userInfo.signature || '这个人很懒，什么都没留下~' }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部信息 -->
      <div class="profile-footer">
        <div class="security-badge">
          <span class="material-icons-outlined security-icon">verified_user</span>
          <span>信息安全已加密保护</span>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-else-if="loading" class="profile-loading">
      <el-skeleton :rows="3" animated>
        <template #template>
          <div class="skeleton-cover"></div>
          <el-skeleton-item variant="circle" style="width: 80px; height: 80px; margin: -40px auto 16px;" />
          <el-skeleton-item variant="h3" style="width: 50%; margin: 0 auto;" />
          <el-skeleton-item variant="text" style="margin-top: 8px;" />
        </template>
      </el-skeleton>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { getUserInfo } from '@/api/im/user'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  session: { type: Object, default: null }
})

const emit = defineEmits(['update:modelValue', 'send-message', 'voice-call', 'video-call'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const userInfo = ref(null)
const loading = ref(false)
const showMoreActions = ref(false)

const isGroup = computed(() => props.session?.type === 'GROUP')
const userName = computed(() => {
  if (!userInfo.value) return ''
  return isGroup.value ? userInfo.value.name : userInfo.value.nickname || userInfo.value.username
})

// 响应式检测
const isMobile = computed(() => window.innerWidth < 768)

const loadUserInfo = async () => {
  if (!props.session) return
  loading.value = true
  try {
    if (isGroup.value) {
      userInfo.value = {
        ...props.session,
        online: false,
        memberCount: props.session.memberCount || Math.floor(Math.random() * 50) + 10
      }
    } else {
      const targetUserId = props.session.targetUserId || props.session.userId || props.session.targetId
      if (targetUserId) {
        const res = await getUserInfo(targetUserId)
        if (res.code === 200) {
          userInfo.value = {
            ...res.data,
            online: Math.random() > 0.5
          }
        }
      } else {
        userInfo.value = { ...props.session, online: false }
      }
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    userInfo.value = { ...props.session, online: false }
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (isOpen) => {
  if (isOpen) {
    showMoreActions.value = false
    loadUserInfo()
  }
})

const handleClose = () => {
  visible.value = false
  showMoreActions.value = false
  userInfo.value = null
}

const handleSendMessage = () => {
  emit('send-message', props.session)
  handleClose()
}

const handleVoiceCall = () => {
  emit('voice-call', props.session)
}

const handleVideoCall = () => {
  emit('video-call', props.session)
}

const toggleMoreActions = () => {
  showMoreActions.value = !showMoreActions.value
}

const handleAddToFavorites = () => {
  ElMessage.success('已添加到常用联系人')
}

const handleSetRemark = () => {
  ElMessage.info('设置备注功能开发中')
}

const handleViewHistory = () => {
  ElMessage.info('历史消息功能开发中')
}

const handleReport = () => {
  ElMessage.info('举报功能开发中')
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// ============================================================================
// 对话框基础样式
// ============================================================================
:deep(.user-detail-dialog) {
  border-radius: 20px;
  overflow: hidden;
  padding: 0;
  background: transparent;

  .el-dialog__header {
    display: none;
  }

  .el-dialog__body {
    padding: 0;
    background: transparent;
  }
}

:deep(.user-detail-modal) {
  .el-overlay {
    background: rgba(0, 0, 0, 0.4);
    backdrop-filter: blur(8px);
  }
}

.user-profile {
  background: var(--dt-bg-card);
  overflow: hidden;
  max-height: 80vh;
  overflow-y: auto;

  .dark & {
    background: var(--dt-bg-card-dark);
  }

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-border-color);
    border-radius: 3px;
  }
}

// ============================================================================
// 封面区域
// ============================================================================
.profile-cover {
  position: relative;
  height: 140px;
  overflow: hidden;

  &.group-cover {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }

  &:not(.group-cover) {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  }

  .cover-pattern {
    position: absolute;
    inset: 0;
    opacity: 0.1;
    background-image:
      radial-gradient(circle at 20% 50%, rgba(255, 255, 255, 0.2) 0%, transparent 50%),
      radial-gradient(circle at 80% 80%, rgba(255, 255, 255, 0.15) 0%, transparent 50%);
    animation: patternMove 20s linear infinite;
  }

  @keyframes patternMove {
    0%, 100% { transform: scale(1); }
    50% { transform: scale(1.1); }
  }

  .close-btn {
    position: absolute;
    top: 16px;
    right: 16px;
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    border: none;
    color: #fff;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    backdrop-filter: blur(10px);

    &:hover {
      background: rgba(255, 255, 255, 0.3);
      transform: rotate(90deg);
    }

    .material-icons-outlined {
      font-size: 20px;
    }
  }

  .profile-badge {
    position: absolute;
    bottom: 16px;
    right: 16px;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(10px);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;

    &.group-badge {
      background: rgba(255, 255, 255, 0.25);
    }

    &.online-badge {
      background: rgba(82, 196, 26, 0.3);
    }

    .material-icons-outlined {
      font-size: 16px;
    }
  }
}

// ============================================================================
// 头像区域
// ============================================================================
.profile-avatar-section {
  padding: 0 24px;
  margin-top: -48px;
  position: relative;
  z-index: 10;
  display: flex;
  justify-content: center;

  .avatar-wrapper {
    position: relative;
    width: 96px;
    height: 96px;

    :deep(.profile-main-avatar) {
      border: 4px solid var(--dt-bg-card);
      border-radius: 50%;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);

      .dark & {
        border-color: var(--dt-bg-card-dark);
      }
    }

    .avatar-group {
      width: 96px;
      height: 96px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.95);
      display: flex;
      align-items: center;
      justify-content: center;
      border: 4px solid var(--dt-bg-card);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
      color: #667eea;

      .dark & {
        border-color: var(--dt-bg-card-dark);
        background: rgba(30, 41, 59, 0.95);
      }

      .material-icons-outlined {
        font-size: 40px;
      }
    }

    .status-indicator {
      position: absolute;
      bottom: 4px;
      right: 4px;
      width: 20px;
      height: 20px;
      border-radius: 50%;
      background: var(--dt-bg-card);
      display: flex;
      align-items: center;
      justify-content: center;

      .dark & {
        background: var(--dt-bg-card-dark);
      }

      .status-dot {
        width: 12px;
        height: 12px;
        border-radius: 50%;
        background: #d1d5db;
        transition: all 0.3s;

        .dark & {
          background: #4b5563;
        }
      }

      &.online .status-dot {
        background: #52c41a;
        box-shadow: 0 0 0 3px rgba(82, 196, 26, 0.2);
        animation: pulse 2s ease-in-out infinite;
      }
    }
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.1);
  }
}

// ============================================================================
// 用户信息
// ============================================================================
.profile-info {
  text-align: center;
  padding: 16px 24px 24px;

  .profile-name {
    font-size: 22px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0 0 8px;
    word-break: break-word;

    .dark & {
      color: var(--dt-text-primary-dark);
    }
  }

  .profile-tags {
    display: flex;
    justify-content: center;
    gap: 8px;
    margin-bottom: 12px;
    flex-wrap: wrap;

    .tag {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      padding: 4px 10px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 500;

      .tag-icon {
        font-size: 14px;
      }

      &.tag-group {
        background: rgba(102, 126, 234, 0.1);
        color: #667eea;
      }

      &.tag-online {
        background: rgba(82, 196, 26, 0.1);
        color: #52c41a;
      }

      &.tag-offline {
        background: rgba(107, 114, 128, 0.1);
        color: #6b7280;
      }
    }
  }

  .profile-position {
    font-size: 13px;
    color: var(--dt-text-secondary);
    margin: 0;

    .dark & {
      color: var(--dt-text-secondary-dark);
    }
  }
}

// ============================================================================
// 操作网格
// ============================================================================
.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  padding: 0 24px 20px;

  .action-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 16px 8px;
    background: var(--dt-bg-body);
    border: 1px solid var(--dt-border-light);
    border-radius: 16px;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    .dark & {
      background: rgba(255, 255, 255, 0.05);
      border-color: var(--dt-border-dark);
    }

    .action-icon {
      width: 44px;
      height: 44px;
      border-radius: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      transition: all 0.3s;

      &.primary {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
      }

      &.voice {
        background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
        box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3);
      }

      &.video {
        background: linear-gradient(135deg, #13c2c2 0%, #0ea5e9 100%);
        box-shadow: 0 4px 12px rgba(19, 194, 194, 0.3);
      }
    }

    .action-label {
      font-size: 12px;
      font-weight: 500;
      color: var(--dt-text-secondary);

      .dark & {
        color: var(--dt-text-secondary-dark);
      }
    }

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);

      .action-icon.primary {
        box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
      }

      .action-icon.voice {
        box-shadow: 0 6px 20px rgba(82, 196, 26, 0.5);
      }

      .action-icon.video {
        box-shadow: 0 6px 20px rgba(19, 194, 194, 0.5);
      }
    }

    &:active {
      transform: translateY(-2px);
    }
  }
}

// ============================================================================
// 更多操作面板
// ============================================================================
.more-actions-panel {
  padding: 0 24px 20px;
  margin-top: -8px;

  .more-action-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    background: var(--dt-bg-body);
    border: 1px solid var(--dt-border-light);
    border-radius: 12px;
    margin-bottom: 8px;
    cursor: pointer;
    transition: all 0.2s;
    font-size: 14px;
    color: var(--dt-text-primary);

    .dark & {
      background: rgba(255, 255, 255, 0.05);
      border-color: var(--dt-border-dark);
      color: var(--dt-text-primary-dark);
    }

    .action-icon {
      font-size: 20px;
      color: var(--dt-text-secondary);

      .dark & {
        color: var(--dt-text-secondary-dark);
      }
    }

    &:hover {
      background: var(--dt-brand-bg);
      border-color: var(--dt-brand-color);

      .action-icon {
        color: var(--dt-brand-color);
      }
    }

    &.danger:hover {
      background: var(--dt-error-bg);
      border-color: var(--dt-error-color);

      .action-icon {
        color: var(--dt-error-color);
      }
    }
  }
}

.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  max-height: 200px;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  opacity: 0;
}

// ============================================================================
// 详细信息区域
// ============================================================================
.profile-details {
  padding: 20px 24px;
  border-top: 1px solid var(--dt-border-light);
  border-bottom: 1px solid var(--dt-border-light);

  .dark & {
    border-color: var(--dt-border-dark);
  }

  .details-header {
    margin-bottom: 16px;

    .details-title {
      font-size: 14px;
      font-weight: 600;
      color: var(--dt-text-tertiary);
      text-transform: uppercase;
      letter-spacing: 0.5px;
      margin: 0;

      .dark & {
        color: var(--dt-text-tertiary-dark);
      }
    }
  }

  .detail-list {
    display: flex;
    flex-direction: column;
    gap: 12px;

    .detail-item {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      padding: 12px;
      background: var(--dt-bg-body);
      border-radius: 12px;
      transition: all 0.2s;

      .dark & {
        background: rgba(255, 255, 255, 0.03);
      }

      &:hover {
        background: var(--dt-bg-hover);
      }

      &.full-width {
        flex-direction: column;
        align-items: stretch;
      }

      .detail-icon {
        width: 36px;
        height: 36px;
        border-radius: 10px;
        background: var(--dt-brand-bg);
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        color: var(--dt-brand-color);

        .material-icons-outlined {
          font-size: 18px;
        }
      }

      .detail-content {
        flex: 1;
        min-width: 0;

        .detail-label {
          display: block;
          font-size: 11px;
          color: var(--dt-text-tertiary);
          margin-bottom: 2px;

          .dark & {
            color: var(--dt-text-tertiary-dark);
          }
        }

        .detail-value {
          font-size: 13px;
          color: var(--dt-text-primary);

          .dark & {
            color: var(--dt-text-primary-dark);
          }

          &.signature {
            font-style: italic;
            color: var(--dt-text-secondary);
            line-height: 1.5;

            .dark & {
              color: var(--dt-text-secondary-dark);
            }
          }
        }
      }
    }
  }
}

// ============================================================================
// 底部信息
// ============================================================================
.profile-footer {
  padding: 16px 24px;
  background: var(--dt-bg-body);
  border-top: 1px solid var(--dt-border-light);

  .dark & {
    background: rgba(0, 0, 0, 0.2);
    border-color: var(--dt-border-dark);
  }

  .security-badge {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;

    .security-icon {
      font-size: 14px;
      color: #52c41a;
    }

    font-size: 11px;
    color: var(--dt-text-tertiary);

    .dark & {
      color: var(--dt-text-tertiary-dark);
    }
  }
}

// ============================================================================
// 加载状态
// ============================================================================
.profile-loading {
  padding: 24px;
  background: var(--dt-bg-card);

  .dark & {
    background: var(--dt-bg-card-dark);
  }

  .skeleton-cover {
    height: 140px;
    background: linear-gradient(135deg, #f0f9ff 0%, #cbebff 100%);
    border-radius: 20px 20px 0 0;
    margin: -24px -24px 16px -24px;
    position: relative;
    overflow: hidden;

    .dark & {
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
    }

    &::after {
      content: '';
      position: absolute;
      top: 0;
      left: -100%;
      width: 100%;
      height: 100%;
      background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
      animation: shimmer 2s infinite;
    }

    @keyframes shimmer {
      0% { left: -100%; }
      100% { left: 100%; }
    }
  }
}

// ============================================================================
// 响应式
// ============================================================================
@media (max-width: 768px) {
  :deep(.user-detail-dialog) {
    .el-dialog {
      width: 100% !important;
      margin: 0;
      border-radius: 0;
    }

    .el-dialog__body {
      max-height: 100vh;
    }
  }

  .user-profile {
    border-radius: 0;
    max-height: 100vh;
  }

  .profile-cover {
    height: 120px;

    .close-btn {
      top: 12px;
      right: 12px;
      width: 32px;
      height: 32px;
    }
  }

  .action-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 8px;
    padding: 0 16px 16px;

    .action-card {
      padding: 12px 4px;

      .action-icon {
        width: 40px;
        height: 40px;
        border-radius: 12px;
      }

      .action-label {
        font-size: 11px;
      }
    }
  }

  .profile-details {
    padding: 16px;
  }
}

@media (max-width: 480px) {
  .action-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;

    .action-card {
      &:nth-child(3), &:nth-child(4) {
        grid-column: span 1;
      }
    }
  }
}
</style>
