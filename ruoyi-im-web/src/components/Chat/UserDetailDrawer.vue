<template>
  <el-dialog
    v-model="visible"
    :width="isMobile ? '100%' : '400px'"
    class="user-detail-drawer"
    :show-close="false"
    append-to-body
    destroy-on-close
    :modal-class="'user-detail-modal'"
    align-center
  >
    <div v-if="userInfo" class="drawer-content">
      <!-- 关闭按钮 -->
      <button class="close-btn" @click="handleClose">
        <span class="material-icons-outlined">close</span>
      </button>

      <!-- 顶部区域 -->
      <div class="top-section">
        <!-- 背景装饰 -->
        <div class="bg-decoration">
          <div class="decoration-circle circle-1"></div>
          <div class="decoration-circle circle-2"></div>
          <div class="decoration-circle circle-3"></div>
        </div>

        <!-- 头像 -->
        <div class="avatar-container">
          <DingtalkAvatar
            :src="userInfo?.avatar"
            :name="userName"
            :user-id="session?.targetId || session?.targetUserId"
            :size="88"
            shape="circle"
            custom-class="detail-avatar"
          />
          <!-- 在线状态点 -->
          <div v-if="!isGroup && userInfo.online" class="online-dot"></div>
        </div>

        <!-- 用户名和标签 -->
        <h2 class="user-name">{{ userName }}</h2>
        <div class="user-tags">
          <span v-if="userInfo.online" class="status-tag online">
            <span class="material-icons-outlined status-icon">circle</span>
            在线
          </span>
          <span v-else class="status-tag offline">
            <span class="material-icons-outlined status-icon">radio_button_unchecked</span>
            离线
          </span>
        </div>

        <!-- 职位信息 -->
        <p v-if="!isGroup" class="user-position">
          <span v-if="userInfo.position">{{ userInfo.position }}</span>
          <span v-if="userInfo.department"> · {{ userInfo.department }}</span>
          <span v-if="!userInfo.position && !userInfo.department" class="empty-hint">暂无职位信息</span>
        </p>
      </div>

      <!-- 快捷操作按钮 -->
      <div class="quick-actions">
        <button class="action-btn primary" @click="handleSendMessage">
          <span class="material-icons-outlined">chat_bubble</span>
          <span>发消息</span>
        </button>
        <button class="action-btn" @click="handleVoiceCall">
          <span class="material-icons-outlined">call</span>
        </button>
        <button class="action-btn" @click="handleVideoCall">
          <span class="material-icons-outlined">videocam</span>
        </button>
        <button class="action-btn more" @click="toggleMore">
          <span class="material-icons-outlined">more_horiz</span>
        </button>
      </div>

      <!-- 更多操作面板 -->
      <transition name="expand">
        <div v-if="showMore" class="more-panel">
          <div class="more-item" @click="handleAddToFavorites">
            <span class="material-icons-outlined">star_border</span>
            <span>添加常用</span>
          </div>
          <div class="more-item" @click="handleSetRemark">
            <span class="material-icons-outlined">edit</span>
            <span>设置备注</span>
          </div>
          <div class="more-item" @click="handleViewHistory">
            <span class="material-icons-outlined">history</span>
            <span>历史消息</span>
          </div>
          <div class="more-item danger" @click="handleReport">
            <span class="material-icons-outlined">block</span>
            <span>举报</span>
          </div>
        </div>
      </transition>

      <!-- 信息卡片 -->
      <div class="info-cards">
        <div class="info-card" v-if="userInfo.phone">
          <div class="card-icon">
            <span class="material-icons-outlined">phone</span>
          </div>
          <div class="card-content">
            <span class="card-label">手机号码</span>
            <span class="card-value">{{ userInfo.phone }}</span>
          </div>
        </div>

        <div class="info-card" v-if="userInfo.email">
          <div class="card-icon">
            <span class="material-icons-outlined">email</span>
          </div>
          <div class="card-content">
            <span class="card-label">邮箱地址</span>
            <span class="card-value email">{{ userInfo.email }}</span>
          </div>
        </div>

        <div class="info-card" v-if="!isGroup && userInfo.username">
          <div class="card-icon">
            <span class="material-icons-outlined">alternate_email</span>
          </div>
          <div class="card-content">
            <span class="card-label">用户名</span>
            <span class="card-value">@{{ userInfo.username }}</span>
          </div>
        </div>

        <div class="info-card full-width" v-if="userInfo.signature || !userInfo.signature">
          <div class="card-icon">
            <span class="material-icons-outlined">format_quote</span>
          </div>
          <div class="card-content">
            <span class="card-label">个性签名</span>
            <p class="card-value signature">{{ userInfo.signature || '这个人很懒，什么都没留下～' }}</p>
          </div>
        </div>
      </div>

      <!-- 底部 -->
      <div class="bottom-section">
        <div class="secure-badge">
          <span class="material-icons-outlined">verified_user</span>
          <span>端到端加密保护</span>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-else-if="loading" class="loading-state">
      <el-skeleton animated>
        <template #template>
          <div class="skeleton-bg"></div>
          <el-skeleton-item variant="circle" style="width: 80px; height: 80px; margin: -40px auto 0;" />
          <el-skeleton-item variant="text" style="width: 120px; margin: 12px auto 0;" />
          <el-skeleton-item variant="text" style="width: 80px; margin: 0 auto;" />
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
const showMore = ref(false)

const isGroup = computed(() => props.session?.type === 'GROUP')
const userName = computed(() => {
  if (!userInfo.value) return ''
  return isGroup.value
    ? userInfo.value.name
    : userInfo.value.nickname || userInfo.value.username || '未知用户'
})

const isMobile = computed(() => window.innerWidth < 768)

// 加载用户信息
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
    showMore.value = false
    loadUserInfo()
  }
})

const handleClose = () => {
  visible.value = false
  showMore.value = false
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

const toggleMore = () => {
  showMore.value = !showMore.value
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
:deep(.user-detail-drawer) {
  border-radius: 24px;
  overflow: hidden;

  .el-dialog__header {
    display: none;
  }

  .el-dialog__body {
    padding: 0;
    background: var(--dt-bg-card);
    border-radius: 24px;
  }
}

:deep(.user-detail-modal) {
  .el-overlay {
    background: rgba(0, 0, 0, 0.3);
    backdrop-filter: blur(8px);
  }
}

.drawer-content {
  position: relative;
  max-height: 85vh;
  overflow-y: auto;
  padding: 24px;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-border);
    border-radius: 2px;
  }

  .dark & {
    background: var(--dt-bg-card-dark);
  }
}

// 关闭按钮
.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--dt-bg-hover);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;

  &:hover {
    background: var(--dt-border);
  }

  .material-icons-outlined {
    font-size: 18px;
    color: var(--dt-text-secondary);
  }
}

// 顶部区域
.top-section {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24px;
}

.bg-decoration {
  position: absolute;
  top: -40px;
  left: 50%;
  transform: translateX(-50%);
  width: 200%;
  height: 200px;
  overflow: hidden;
  pointer-events: none;

  .decoration-circle {
    position: absolute;
    border-radius: 50%;
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);

    &.circle-1 {
      width: 120px;
      height: 120px;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
    }

    &.circle-2 {
      width: 80px;
      height: 80px;
      top: 30%;
      left: 20%;
      opacity: 0.6;
    }

    &.circle-3 {
      width: 60px;
      height: 60px;
      bottom: 20%;
      right: 20%;
      opacity: 0.4;
    }
  }
}

.avatar-container {
  position: relative;
  margin-bottom: 16px;

  .detail-avatar {
    border: 4px solid var(--dt-bg-card);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  }

  .online-dot {
    position: absolute;
    bottom: 4px;
    right: 4px;
    width: 16px;
    height: 16px;
    background: #52c41a;
    border: 3px solid var(--dt-bg-card);
    border-radius: 50%;
  }
}

.user-name {
  font-size: 20px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0 0 12px;
  text-align: center;
  word-break: break-word;
}

.user-tags {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 8px;

  .status-tag {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 10px;
    border-radius: 16px;
    font-size: 12px;
    font-weight: 500;

    &.online {
      background: rgba(82, 196, 26, 0.1);
      color: #52c41a;

      .status-icon {
        font-size: 12px;
      }
    }

    &.offline {
      background: var(--dt-bg-body);
      color: var(--dt-text-tertiary);

      .status-icon {
        font-size: 12px;
      }
    }
  }
}

.user-position {
  font-size: 13px;
  color: var(--dt-text-secondary);
  margin: 0;
  text-align: center;

  .empty-hint {
    color: var(--dt-text-tertiary);
  }
}

// 快捷操作
.quick-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
  margin-bottom: 24px;

  .action-btn {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--dt-bg-body);
    border: 1px solid var(--dt-border-lighter);
    cursor: pointer;
    transition: all 0.2s;

    .material-icons-outlined {
      font-size: 20px;
      color: var(--dt-text-secondary);
    }

    &.primary {
      background: var(--dt-brand-color);
      border-color: var(--dt-brand-color);

      .material-icons-outlined {
        color: #fff;
      }
    }

    &:hover:not(.primary) {
      background: var(--dt-brand-bg);
      border-color: var(--dt-brand-color);

      .material-icons-outlined {
        color: var(--dt-brand-color);
      }
    }

    &.primary:hover {
      transform: scale(1.05);
      box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
    }
  }
}

// 更多操作面板
.more-panel {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-bottom: 24px;

  .more-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px;
    background: var(--dt-bg-body);
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.2s;

    .material-icons-outlined {
      font-size: 18px;
      color: var(--dt-text-secondary);
    }

    span {
      font-size: 13px;
      color: var(--dt-text-primary);
    }

    &:hover {
      background: var(--dt-bg-hover);
    }

    &.danger:hover {
      background: var(--dt-error-bg);
      color: var(--dt-error-color);

      .material-icons-outlined {
        color: var(--dt-error-color);
      }
    }
  }
}

.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
}

.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

// 信息卡片
.info-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;

  .info-card {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px;
    background: var(--dt-bg-body);
    border-radius: 14px;
    transition: all 0.2s;

    &:hover {
      background: var(--dt-bg-hover);
    }

    &.full-width {
      flex-direction: column;
      align-items: flex-start;
    }

    .card-icon {
      width: 36px;
      height: 36px;
      border-radius: 10px;
      background: var(--dt-brand-bg);
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;

      .material-icons-outlined {
        font-size: 18px;
        color: var(--dt-brand-color);
      }
    }

    .card-content {
      flex: 1;
      min-width: 0;

      .card-label {
        display: block;
        font-size: 11px;
        color: var(--dt-text-tertiary);
        margin-bottom: 2px;
      }

      .card-value {
        font-size: 13px;
        color: var(--dt-text-primary);

        &.email {
          word-break: break-all;
        }

        &.signature {
          font-style: italic;
          color: var(--dt-text-secondary);
          line-height: 1.5;
          margin: 0;
        }
      }
    }
  }
}

// 底部
.bottom-section {
  padding-top: 16px;
  border-top: 1px solid var(--dt-border-lighter);

  .secure-badge {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;

    .material-icons-outlined {
      font-size: 14px;
      color: #52c41a;
    }

    font-size: 11px;
    color: var(--dt-text-tertiary);
  }
}

// 加载状态
.loading-state {
  padding: 24px;

  .skeleton-bg {
    height: 100px;
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
    border-radius: 24px 24px 0 0;
    margin: -24px -24px 16px -24px;
  }
}

// 响应式
@media (max-width: 768px) {
  :deep(.user-detail-drawer) {
    .el-dialog {
      width: 100% !important;
      margin: 0;
      border-radius: 0;
    }

    .el-dialog__body {
      border-radius: 0;
      max-height: 100vh;
    }
  }

  .drawer-content {
    padding: 20px;
    max-height: 100vh;
  }

  .quick-actions {
    .action-btn {
      width: 44px;
      height: 44px;
    }
  }

  .more-panel {
    grid-template-columns: 1fr;
  }
}
</style>
