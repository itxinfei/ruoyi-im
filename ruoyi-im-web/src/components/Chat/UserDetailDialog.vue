<template>
  <el-dialog
    v-model="visible"
    :width="640"
    class="user-detail-dialog"
    :close-on-click-modal="true"
    append-to-body
    destroy-on-close
    align-center
  >
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 用户信息内容 -->
    <div v-else-if="userInfo" class="dialog-content">
      <!-- 左侧：用户信息 -->
      <div class="left-section">
        <div class="avatar-wrapper">
          <DingtalkAvatar
            :src="userInfo.avatar"
            :name="userName"
            :user-id="session?.targetId || session?.targetUserId"
            :size="88"
            shape="circle"
          />
          <span v-if="userInfo.online !== undefined" :class="['online-status', userInfo.online ? 'online' : 'offline']">
            {{ userInfo.online ? '在线' : '离线' }}
          </span>
        </div>
        <h3 class="user-name">{{ userName }}</h3>
        <div class="user-tags">
          <span v-if="userInfo.position" class="tag">
            <span class="material-icons-outlined tag-icon">badge</span>
            {{ userInfo.position }}
          </span>
          <span v-if="userInfo.department" class="tag">
            <span class="material-icons-outlined tag-icon">business</span>
            {{ userInfo.department }}
          </span>
          <span v-if="userInfo.gender !== undefined" class="tag gender-tag">
            <span class="material-icons-outlined tag-icon">{{ genderIcon }}</span>
            {{ genderText }}
          </span>
        </div>
      </div>

      <!-- 右侧：信息和操作 -->
      <div class="right-section">
        <!-- 信息列表 -->
        <div class="info-list">
          <div class="info-item">
            <span class="material-icons-outlined info-icon">person</span>
            <span class="info-label">用户名</span>
            <span class="info-value">{{ userInfo.username || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="material-icons-outlined info-icon">phone</span>
            <span class="info-label">手机</span>
            <span class="info-value">{{ userInfo.mobile || userInfo.phone || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="material-icons-outlined info-icon">email</span>
            <span class="info-label">邮箱</span>
            <span class="info-value">{{ userInfo.email || '未设置' }}</span>
          </div>
          <div v-if="userInfo.birthday" class="info-item">
            <span class="material-icons-outlined info-icon">cake</span>
            <span class="info-label">生日</span>
            <span class="info-value">{{ formatDate(userInfo.birthday) }}</span>
          </div>
          <div v-if="userInfo.lastOnlineTime" class="info-item">
            <span class="material-icons-outlined info-icon">schedule</span>
            <span class="info-label">最近在线</span>
            <span class="info-value">{{ formatDateTime(userInfo.lastOnlineTime) }}</span>
          </div>
          <div v-if="userInfo.signature" class="info-item full-width">
            <span class="material-icons-outlined info-icon">format_quote</span>
            <span class="info-value signature">{{ userInfo.signature }}</span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-group">
          <button class="action-btn primary" @click="handleSendMessage">
            <span class="material-icons-outlined">chat_bubble</span>
            发消息
          </button>
          <button class="action-btn" @click="handleVoiceCall">
            <span class="material-icons-outlined">phone_in_talk</span>
            语音
          </button>
          <button class="action-btn" @click="handleVideoCall">
            <span class="material-icons-outlined">videocam</span>
            视频
          </button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { getUserInfo } from '@/api/im/user'
import dayjs from 'dayjs'

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

const isGroup = computed(() => props.session?.type === 'GROUP')
const userName = computed(() => {
  if (!userInfo.value) return ''
  return isGroup.value
    ? userInfo.value.name
    : userInfo.value.nickname || userInfo.value.username || '未知用户'
})

// 性别相关
const genderIcon = computed(() => {
  const gender = userInfo.value?.gender
  if (gender === 1) return 'male'
  if (gender === 2) return 'female'
  return 'help'
})

const genderText = computed(() => {
  const gender = userInfo.value?.gender
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '未知'
})

// 日期格式化
const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD')
}

const formatDateTime = (dateTime) => {
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm')
}

const loadUserInfo = async () => {
  if (!props.session) return
  loading.value = true
  try {
    const targetUserId = props.session.targetUserId || props.session.userId || props.session.targetId
    if (targetUserId) {
      const res = await getUserInfo(targetUserId)
      if (res.code === 200) {
        userInfo.value = res.data
      }
    }
  } catch (error) {
    userInfo.value = { ...props.session }
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (isOpen) => {
  if (isOpen) {
    loadUserInfo()
  }
})

const handleSendMessage = () => {
  emit('send-message', props.session)
  visible.value = false
}

const handleVoiceCall = () => {
  emit('voice-call', props.session)
}

const handleVideoCall = () => {
  emit('video-call', props.session)
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

:deep(.user-detail-dialog) {
  .el-dialog__header {
    display: none;
  }

  .el-dialog__body {
    padding: 32px 32px;
  }
}

.loading-state {
  padding: 44px 32px;
}

.dialog-content {
  display: flex;
  gap: 32px;

  .left-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    min-width: 120px;
    text-align: center;

    .avatar-wrapper {
      position: relative;
      display: inline-block;

      .online-status {
        position: absolute;
        bottom: -2px;
        left: 50%;
        transform: translateX(-50%);
        font-size: 11px;
        padding: 3px 10px;
        border-radius: 12px;
        font-weight: 500;

        &.online {
          background: #67c23a;
          color: #fff;
        }

        &.offline {
          background: var(--dt-text-tertiary);
          color: #fff;
        }
      }
    }

    .user-name {
      font-size: 18px;
      font-weight: 500;
      color: var(--dt-text-primary);
      margin: 14px 0 12px;
      max-width: 120px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .user-tags {
      display: flex;
      flex-direction: column;
      gap: 6px;
      width: 100%;

      .tag {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 4px;
        font-size: 12px;
        color: var(--dt-text-secondary);
        background: var(--dt-bg-secondary);
        padding: 5px 10px;
        border-radius: 4px;
        white-space: nowrap;

        .tag-icon {
          font-size: 13px;
        }

        &.gender-tag {
          .tag-icon {
            font-size: 15px;
          }
        }
      }
    }
  }

  .right-section {
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  .info-list {
    display: flex;
    flex-direction: column;
    gap: 3px;
    margin-bottom: 24px;

    .info-item {
      display: flex;
      align-items: center;
      gap: 14px;
      padding: 12px 16px;
      border-radius: 6px;
      transition: background 0.2s;

      &:hover {
        background: var(--dt-bg-secondary);
      }

      &.full-width {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;
      }

      .info-icon {
        font-size: 20px;
        color: var(--dt-text-tertiary);
        width: 22px;
        text-align: center;
      }

      .info-label {
        font-size: 13px;
        color: var(--dt-text-tertiary);
        min-width: 60px;
      }

      .info-value {
        flex: 1;
        font-size: 15px;
        color: var(--dt-text-primary);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;

        &.signature {
          white-space: normal;
          color: var(--dt-text-secondary);
          line-height: 1.5;
        }
      }
    }
  }

  .action-group {
    display: flex;
    gap: 10px;
    margin-top: auto;

    .action-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      height: 42px;
      padding: 0 18px;
      border-radius: 6px;
      font-size: 15px;
      cursor: pointer;
      transition: all 0.2s;
      border: 1px solid var(--dt-border-light);

      .material-icons-outlined {
        font-size: 19px;
        color: var(--dt-text-secondary);
      }

      &.primary {
        flex: 1;
        background: var(--dt-brand-color);
        border-color: var(--dt-brand-color);
        color: #fff;

        .material-icons-outlined {
          color: #fff;
        }
      }

      &:not(.primary) {
        background: var(--dt-bg-secondary);
        color: var(--dt-text-secondary);
      }

      &:hover {
        opacity: 0.85;
      }
    }
  }
}

.dark {
  .action-group .action-btn:not(.primary) {
    background: var(--dt-bg-hover-dark);
  }
}
</style>
