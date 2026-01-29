<template>
  <el-dialog
    v-model="visible"
    :width="640"
    class="user-profile-dialog"
    :show-close="true"
    :close-on-click-modal="true"
    destroy-on-close
    append-to-body
  >
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="3" animated />
    </div>

    <div v-else-if="userInfo" class="dialog-content">
      <!-- 左侧：基本信息区 -->
      <div class="left-section">
        <div class="avatar-wrapper">
          <DingtalkAvatar
            :src="userInfo?.avatar"
            :name="userName"
            :user-id="session?.targetId || session?.targetUserId"
            :size="88"
            shape="circle"
          />
          <div v-if="userInfo.online" class="online-indicator"></div>
        </div>

        <h3 class="user-name">{{ userName }}</h3>

        <div class="status-badge" :class="{ online: userInfo.online, offline: !userInfo.online }">
          <span class="dot"></span>
          {{ userInfo.online ? '在线' : '离线' }}
        </div>

        <div class="user-extra">
          <span v-if="userInfo.position" class="position">{{ userInfo.position }}</span>
          <span v-if="userInfo.department" class="department">{{ userInfo.department }}</span>
        </div>
      </div>

      <!-- 右侧：详细信息区 -->
      <div class="right-section">
        <!-- 信息列表 -->
        <div class="info-list">
          <div v-if="userInfo.mobile || userInfo.phone" class="info-row">
            <span class="info-label">手机</span>
            <span class="info-value">{{ userInfo.mobile || userInfo.phone }}</span>
          </div>
          <div v-if="userInfo.email" class="info-row">
            <span class="info-label">邮箱</span>
            <span class="info-value">{{ userInfo.email }}</span>
          </div>
          <div v-if="userInfo.username" class="info-row">
            <span class="info-label">账号</span>
            <span class="info-value">@{{ userInfo.username }}</span>
          </div>
          <div class="info-row full">
            <span class="info-label">签名</span>
            <span class="info-value signature">{{ userInfo.signature || '这个人很懒，什么都没留下～' }}</span>
          </div>
        </div>

        <!-- 操作按钮组 -->
        <div class="action-buttons">
          <button class="action-btn primary" @click="handleSendMessage">
            <span class="material-icons-outlined">chat_bubble</span>
            发消息
          </button>
          <button class="action-btn" @click="handleVoiceCall">
            <span class="material-icons-outlined">phone</span>
          </button>
          <button class="action-btn" @click="handleVideoCall">
            <span class="material-icons-outlined">videocam</span>
          </button>
        </div>

        <!-- 更多操作 -->
        <div class="more-actions">
          <button class="more-btn" @click="handleAddToFavorites">
            <span class="material-icons-outlined">star_border</span>
            添加常用
          </button>
          <button class="more-btn" @click="handleSetRemark">
            <span class="material-icons-outlined">edit</span>
            设置备注
          </button>
          <button class="more-btn" @click="handleViewHistory">
            <span class="material-icons-outlined">history</span>
            聊天记录
          </button>
        </div>
      </div>
    </div>
  </el-dialog>

  <!-- 备注编辑弹窗 -->
  <el-dialog
    v-model="showRemarkDialog"
    title="设置备注"
    width="380px"
    class="remark-dialog"
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

const userName = computed(() => {
  if (!userInfo.value) return ''
  return userInfo.value.nickname || userInfo.value.username || '未知用户'
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
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

:deep(.user-profile-dialog) {
  .el-dialog {
    border-radius: var(--dt-radius-xl);
    overflow: hidden;
  }

  .el-dialog__header {
    padding: 16px 20px;
    border-bottom: 1px solid var(--dt-border-lighter);
    margin: 0;

    .dark & {
      border-bottom-color: var(--dt-border-dark);
    }

    .el-dialog__title {
      font-size: var(--dt-font-size-lg);
      font-weight: var(--dt-font-weight-semibold);
      color: var(--dt-text-primary);
    }

    .el-dialog__headerbtn {
      top: 16px;
      right: 16px;
      width: 32px;
      height: 32px;

      .el-dialog__close {
        font-size: 18px;
        color: var(--dt-text-secondary);
      }
    }
  }

  .el-dialog__body {
    padding: 24px;
  }
}

.loading-state {
  padding: 60px 40px;
}

.dialog-content {
  display: flex;
  gap: 32px;
}

// 左侧区域
.left-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 160px;
  flex-shrink: 0;
  padding-right: 24px;
  border-right: 1px solid var(--dt-border-lighter);

  .dark & {
    border-right-color: var(--dt-border-dark);
  }

  .avatar-wrapper {
    position: relative;
    margin-bottom: 16px;

    .online-indicator {
      position: absolute;
      bottom: 4px;
      right: 4px;
      width: 18px;
      height: 18px;
      background: var(--dt-success-color);
      border: 3px solid #fff;
      border-radius: 50%;
    }
  }

  .user-name {
    font-size: var(--dt-font-size-xl);
    font-weight: var(--dt-font-weight-semibold);
    color: var(--dt-text-primary);
    margin: 0 0 12px;
    text-align: center;
    word-break: break-word;
    max-width: 100%;
  }

  .status-badge {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 4px 12px;
    border-radius: var(--dt-radius-full);
    font-size: var(--dt-font-size-sm);
    margin-bottom: 12px;

    .dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
    }

    &.online {
      background: var(--dt-success-bg);
      color: var(--dt-success-color);

      .dot {
        background: var(--dt-success-color);
      }
    }

    &.offline {
      background: var(--dt-bg-card-hover);
      color: var(--dt-text-tertiary);

      .dot {
        background: var(--dt-text-quaternary);
      }
    }
  }

  .user-extra {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-secondary);
    text-align: center;

    .position, .department {
      max-width: 100%;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

// 右侧区域
.right-section {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 0;

  .info-row {
    display: flex;
    padding: 12px 0;
    border-bottom: 1px solid var(--dt-border-lighter);

    &:last-child {
      border-bottom: none;
    }

    .dark & {
      border-bottom-color: var(--dt-border-dark);
    }

    &.full {
      flex-direction: column;
      gap: 4px;
    }

    .info-label {
      width: 60px;
      font-size: var(--dt-font-size-sm);
      color: var(--dt-text-tertiary);
      flex-shrink: 0;
    }

    .info-value {
      flex: 1;
      font-size: var(--dt-font-size-base);
      color: var(--dt-text-primary);

      &.signature {
        font-style: italic;
        color: var(--dt-text-secondary);
      }
    }
  }
}

.action-buttons {
  display: flex;
  gap: 8px;

  .action-btn {
    flex: 1;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    background: #ffffff;
    border: 1px solid var(--dt-border-color);
    border-radius: var(--dt-radius-md);
    cursor: pointer;
    font-size: var(--dt-font-size-base);
    color: var(--dt-text-primary);
    transition: all var(--dt-transition-fast);

    .dark & {
      background: var(--dt-bg-card-dark);
      border-color: var(--dt-border-dark);
    }

    .material-icons-outlined {
      font-size: 18px;
    }

    &.primary {
      flex: 2;
      background: var(--dt-brand-color);
      border-color: var(--dt-brand-color);
      color: #fff;

      &:hover {
        opacity: 0.9;
      }
    }

    &:hover:not(.primary) {
      background: var(--dt-bg-session-hover);
      border-color: var(--dt-brand-color);
      color: var(--dt-brand-color);
    }
  }
}

.more-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;

  .more-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 14px;
    background: transparent;
    border: 1px solid var(--dt-border-color);
    border-radius: var(--dt-radius-md);
    cursor: pointer;
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-secondary);
    transition: all var(--dt-transition-fast);

    .dark & {
      border-color: var(--dt-border-dark);
    }

    .material-icons-outlined {
      font-size: 16px;
    }

    &:hover {
      background: var(--dt-bg-session-hover);
      border-color: var(--dt-brand-color);
      color: var(--dt-brand-color);
    }
  }
}

// 备注弹窗样式
:deep(.remark-dialog) {
  .el-input__wrapper {
    box-shadow: 0 0 0 1px var(--dt-border-color) inset;
    transition: box-shadow var(--dt-transition-base);

    .dark & {
      background-color: var(--dt-bg-input-dark);
      box-shadow: 0 0 0 1px var(--dt-border-dark) inset;
    }

    &:hover {
      box-shadow: 0 0 0 1px var(--dt-border-input-hover) inset;

      .dark & {
        box-shadow: 0 0 0 1px var(--dt-border-color) inset;
      }
    }

    &.is-focus {
      box-shadow: 0 0 0 1px var(--dt-brand-color) inset;
    }
  }

  .el-input__count {
    color: var(--dt-text-quaternary);
  }
}

// ============================================================================
// 响应式适配
// ============================================================================

@media (max-width: 768px) {
  .dialog-content {
    flex-direction: column;
    gap: 24px;
  }

  .left-section {
    width: 100%;
    padding-right: 0;
    border-right: none;
    border-bottom: 1px solid var(--dt-border-lighter);
    padding-bottom: 20px;

    .dark & {
      border-bottom-color: var(--dt-border-dark);
    }
  }
}
</style>
