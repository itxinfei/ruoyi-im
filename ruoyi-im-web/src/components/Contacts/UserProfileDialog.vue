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
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  }

  .el-dialog__header {
    padding: 20px 24px;
    border-bottom: 1px solid var(--dt-border-lighter);
    margin: 0;
    background: linear-gradient(135deg, var(--dt-brand-color) 0%, #0e5fd9 100%);

    .dark & {
      border-bottom-color: var(--dt-border-dark);
    }

    .el-dialog__title {
      font-size: var(--dt-font-size-lg);
      font-weight: var(--dt-font-weight-semibold);
      color: #fff;
    }

    .el-dialog__headerbtn {
      top: 18px;
      right: 20px;
      width: 32px;
      height: 32px;
      border-radius: 8px;
      transition: all 0.2s;

      &:hover {
        background: rgba(255, 255, 255, 0.2);
      }

      .el-dialog__close {
        font-size: 18px;
        color: rgba(255, 255, 255, 0.9);
      }
    }
  }

  .el-dialog__body {
    padding: 28px;
    background: var(--dt-bg-body);
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
  width: 180px;
  flex-shrink: 0;
  padding: 20px;
  background: linear-gradient(180deg, rgba(22, 119, 255, 0.08) 0%, transparent 100%);
  border-radius: 12px;
  margin-right: 24px;

  .avatar-wrapper {
    position: relative;
    margin-bottom: 16px;

    :deep(.dingtalk-avatar) {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      transition: transform 0.3s ease;

      &:hover {
        transform: scale(1.05);
      }
    }

    .online-indicator {
      position: absolute;
      bottom: 4px;
      right: 4px;
      width: 20px;
      height: 20px;
      background: var(--dt-success-color);
      border: 3px solid #fff;
      border-radius: 50%;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    }
  }

  .user-name {
    font-size: 20px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0 0 12px;
    text-align: center;
    word-break: break-word;
    max-width: 100%;
  }


  .user-extra {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: var(--dt-text-secondary);
    text-align: center;

    .position {
      font-weight: 500;
      color: var(--dt-brand-color);
      background: rgba(22, 119, 255, 0.1);
      padding: 2px 10px;
      border-radius: 10px;
    }

    .department {
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
  gap: 24px;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 0;
  background: var(--dt-bg-card);
  border-radius: 12px;
  padding: 8px 16px;
  border: 1px solid var(--dt-border-lighter);

  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  .info-row {
    display: flex;
    align-items: center;
    padding: 14px 0;
    border-bottom: 1px solid var(--dt-border-lighter);
    transition: background 0.2s;

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background: var(--dt-bg-session-hover);
      margin: 0 -16px;
      padding-left: 16px;
      padding-right: 16px;
    }

    .dark & {
      border-bottom-color: var(--dt-border-dark);
    }

    &.full {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;

      &:hover {
        margin: 0 -16px;
        padding-left: 16px;
        padding-right: 16px;
      }
    }

    .info-label {
      width: 60px;
      font-size: 13px;
      color: var(--dt-text-tertiary);
      flex-shrink: 0;
      font-weight: 500;
    }

    .info-value {
      flex: 1;
      font-size: 14px;
      color: var(--dt-text-primary);
      font-weight: 500;

      &.signature {
        font-style: italic;
        color: var(--dt-text-secondary);
        font-weight: 400;
        line-height: 1.5;
      }
    }
  }
}

.action-buttons {
  display: flex;
  gap: 12px;

  .action-btn {
    flex: 1;
    height: 42px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    background: #ffffff;
    border: 1px solid var(--dt-border-color);
    border-radius: 10px;
    cursor: pointer;
    font-size: 14px;
    font-weight: 500;
    color: var(--dt-text-primary);
    transition: all 0.25s ease;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);

    .dark & {
      background: var(--dt-bg-card-dark);
      border-color: var(--dt-border-dark);
    }

    .material-icons-outlined {
      font-size: 20px;
    }

    &.primary {
      flex: 2;
      background: linear-gradient(135deg, var(--dt-brand-color) 0%, #0e5fd9 100%);
      border-color: transparent;
      color: #fff;
      box-shadow: 0 4px 12px rgba(22, 119, 255, 0.35);

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(22, 119, 255, 0.45);
      }

      &:active {
        transform: translateY(0);
      }
    }

    &:hover:not(.primary) {
      background: var(--dt-bg-session-hover);
      border-color: var(--dt-brand-color);
      color: var(--dt-brand-color);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }

    &:active:not(.primary) {
      transform: translateY(0);
    }
  }
}

.more-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;

  .more-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 10px 16px;
    background: var(--dt-bg-card);
    border: 1px solid var(--dt-border-lighter);
    border-radius: 10px;
    cursor: pointer;
    font-size: 13px;
    font-weight: 500;
    color: var(--dt-text-secondary);
    transition: all 0.25s ease;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);

    .dark & {
      background: var(--dt-bg-card-dark);
      border-color: var(--dt-border-dark);
    }

    .material-icons-outlined {
      font-size: 18px;
    }

    &:hover {
      background: var(--dt-bg-session-hover);
      border-color: var(--dt-brand-color);
      color: var(--dt-brand-color);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }

    &:active {
      transform: translateY(0);
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
