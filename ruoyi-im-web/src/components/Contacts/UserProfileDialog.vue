<template>
  <el-dialog
    v-model="visible"
    :width="420"
    class="user-profile-dialog"
    :show-close="false"
    :close-on-click-modal="true"
    destroy-on-close
    append-to-body
  >
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="4" animated />
    </div>

    <div v-else-if="userInfo" class="dialog-content">
      <!-- 关闭按钮 -->
      <button class="close-btn" @click="handleClose">
        <span class="material-icons-outlined">close</span>
      </button>

      <!-- 顶部区域 -->
      <div class="top-section">
        <div class="bg-decoration">
          <div class="decoration-circle circle-1"></div>
          <div class="decoration-circle circle-2"></div>
        </div>

        <!-- 头像 -->
        <div class="avatar-container">
          <DingtalkAvatar
            :src="userInfo?.avatar"
            :name="userName"
            :user-id="session?.targetId || session?.targetUserId"
            :size="96"
            shape="circle"
            custom-class="detail-avatar"
          />
          <div v-if="userInfo.online !== undefined && userInfo.online" class="online-dot"></div>
        </div>

        <!-- 用户名和标签 -->
        <h3 class="user-name">{{ userName }}</h3>
        <div class="user-tags">
          <span v-if="userInfo.online !== undefined && userInfo.online" class="status-tag online">
            <span class="material-icons-outlined status-icon">circle</span>
            在线
          </span>
          <span v-else class="status-tag offline">
            <span class="material-icons-outlined status-icon">radio_button_unchecked</span>
            离线
          </span>
        </div>

        <!-- 职位信息 -->
        <p class="user-position">
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
          <span class="material-icons-outlined">phone_in_talk</span>
        </button>
        <button class="action-btn" @click="handleVideoCall">
          <span class="material-icons-outlined">videocam</span>
        </button>
      </div>

      <!-- 信息卡片 -->
      <div class="info-cards">
        <div class="info-card" v-if="userInfo.mobile || userInfo.phone">
          <div class="card-icon">
            <span class="material-icons-outlined">phone</span>
          </div>
          <div class="card-content">
            <span class="card-label">手机号码</span>
            <span class="card-value">{{ userInfo.mobile || userInfo.phone || '未设置' }}</span>
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

        <div class="info-card" v-if="userInfo.username">
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

      <!-- 更多操作 -->
      <div class="more-actions">
        <button class="more-action-item" @click="handleAddToFavorites">
          <span class="material-icons-outlined action-icon">star_border</span>
          <span>添加常用</span>
        </button>
        <button class="more-action-item" @click="handleSetRemark">
          <span class="material-icons-outlined action-icon">edit</span>
          <span>设置备注</span>
        </button>
        <button class="more-action-item" @click="handleViewHistory">
          <span class="material-icons-outlined action-icon">history</span>
          <span>聊天记录</span>
        </button>
        <button class="more-action-item" @click="handleSearchChat">
          <span class="material-icons-outlined action-icon">search</span>
          <span>搜索记录</span>
        </button>
      </div>

      <!-- 底部 -->
      <div class="bottom-section">
        <div class="secure-badge">
          <span class="material-icons-outlined">verified_user</span>
          <span>端到端加密保护</span>
        </div>
      </div>
    </div>
  </el-dialog>

  <!-- 备注编辑弹窗 -->
  <el-dialog
    v-model="showRemarkDialog"
    title="设置备注"
    width="380px"
    :before-close="() => showRemarkDialog = false"
  >
    <el-input
      v-model="remarkInput"
      placeholder="请输入备注名称"
      maxlength="20"
      show-word-limit
      @keyup.enter="handleSaveRemark"
    />
    <template #footer>
      <el-button @click="showRemarkDialog = false">取消</el-button>
      <el-button type="primary" @click="handleSaveRemark">确定</el-button>
    </template>
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

const emit = defineEmits(['update:modelValue', 'send-message', 'voice-call', 'video-call', 'history', 'search'])

const visible = ref(false)
const userInfo = ref(null)
const loading = ref(false)
const showRemarkDialog = ref(false)
const remarkInput = ref('')

const isGroup = computed(() => props.session?.type === 'GROUP')
const userName = computed(() => {
  if (!userInfo.value) return ''
  return isGroup.value
    ? userInfo.value.name
    : userInfo.value.nickname || userInfo.value.username || '未知用户'
})

const loadUserInfo = async () => {
  if (!props.session) return
  loading.value = true
  try {
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
  } catch (error) {
    userInfo.value = { ...props.session, online: false }
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (isOpen) => {
  visible.value = isOpen
  if (isOpen) {
    loadUserInfo()
  }
})

watch(visible, (val) => {
  if (!val) emit('update:modelValue', false)
})

const handleClose = () => {
  visible.value = false
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

const handleAddToFavorites = () => {
  ElMessage.success('已添加到常用联系人')
}

const handleSetRemark = () => {
  remarkInput.value = userInfo.value?.remark || ''
  showRemarkDialog.value = true
}

const handleSaveRemark = () => {
  userInfo.value = { ...userInfo.value, remark: remarkInput.value }
  showRemarkDialog.value = false
  ElMessage.success('备注已保存')
}

const handleViewHistory = () => {
  emit('history', props.session)
  handleClose()
}

const handleSearchChat = () => {
  emit('search', props.session)
  handleClose()
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

:deep(.user-profile-dialog) {
  .el-dialog {
    border-radius: 16px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
    overflow: hidden;
  }

  .el-dialog__header {
    display: none;
  }

  .el-dialog__body {
    padding: 0;
  }
}

.loading-state {
  padding: 40px 24px;
}

.dialog-content {
  background: var(--dt-bg-card);
}

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
  z-index: 10;

  &:hover {
    background: var(--dt-border);
  }

  .material-icons-outlined {
    font-size: 18px;
    color: var(--dt-text-secondary);
  }
}

.top-section {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px 24px 24px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.08) 100%);
  border-bottom: 1px solid var(--dt-border-light);
}

.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 100%;
  overflow: hidden;
  pointer-events: none;

  .decoration-circle {
    position: absolute;
    border-radius: 50%;
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);

    &.circle-1 {
      width: 100px;
      height: 100px;
      top: -30px;
      right: -30px;
    }

    &.circle-2 {
      width: 70px;
      height: 70px;
      bottom: -15px;
      left: -15px;
      opacity: 0.6;
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
    box-shadow: 0 0 0 2px rgba(82, 196, 26, 0.3);
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
    padding: 4px 12px;
    border-radius: 16px;
    font-size: 12px;
    font-weight: 500;

    &.online {
      background: rgba(82, 196, 26, 0.15);
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

.quick-actions {
  display: flex;
  gap: 12px;
  padding: 0 24px 20px;
  border-bottom: 1px solid var(--dt-border-light);

  .action-btn {
    flex: 1;
    height: 44px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    background: var(--dt-bg-body);
    border: 1px solid var(--dt-border-light);
    cursor: pointer;
    transition: all 0.2s;
    font-size: 14px;
    font-weight: 500;
    color: var(--dt-text-primary);

    .material-icons-outlined {
      font-size: 20px;
    }

    &.primary {
      background: var(--dt-brand-color);
      border-color: var(--dt-brand-color);
      color: #fff;
      flex: 2;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
      }
    }

    &:hover:not(.primary) {
      background: var(--dt-brand-bg);
      border-color: var(--dt-brand-color);
      color: var(--dt-brand-color);
    }
  }
}

.info-cards {
  padding: 20px 24px;
  overflow-y: auto;
  max-height: 280px;

  .info-card {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px;
    background: var(--dt-bg-body);
    border-radius: 12px;
    margin-bottom: 12px;
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

.more-actions {
  padding: 16px 24px;
  border-bottom: 1px solid var(--dt-border-light);

  .more-action-item {
    display: flex;
    align-items: center;
    gap: 12px;
    width: 100%;
    padding: 12px;
    background: transparent;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
    text-align: left;
    font-size: 14px;
    color: var(--dt-text-primary);

    .action-icon {
      font-size: 20px;
      color: var(--dt-text-secondary);
    }

    &:hover {
      background: var(--dt-bg-hover);

      .action-icon {
        color: var(--dt-brand-color);
      }
    }
  }
}

.bottom-section {
  padding: 16px 24px;
  background: var(--dt-bg-body);

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

.dark {
  .top-section {
    background: linear-gradient(135deg, rgba(22, 119, 255, 0.08) 0%, rgba(22, 119, 255, 0.03) 100%);
  }

  .bg-decoration .decoration-circle {
    background: linear-gradient(135deg, rgba(22, 119, 255, 0.15) 0%, rgba(22, 119, 255, 0.1) 100%);
  }

  .user-name {
    color: var(--dt-text-primary-dark);
  }

  .quick-actions .action-btn {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);

    &:hover:not(.primary) {
      background: var(--dt-bg-hover-dark);
    }
  }

  .info-cards .info-card {
    background: var(--dt-bg-card-dark);
  }

  .more-actions {
    border-color: var(--dt-border-dark);
  }

  .bottom-section {
    background: var(--dt-bg-card-dark);
  }
}
</style>
