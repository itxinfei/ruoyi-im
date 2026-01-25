<template>
  <el-dialog
    v-model="visible"
    :title="null"
    width="380px"
    class="user-detail-dialog"
    :show-close="false"
    append-to-body
    destroy-on-close
  >
    <div v-if="userInfo" class="user-card-content">
      <!-- 顶部背景/封面 -->
      <div class="card-cover" :style="{ backgroundColor: avatarBgColor }">
        <button class="close-card-btn" @click="handleClose">
          <el-icon><Close /></el-icon>
        </button>
      </div>

      <!-- 核心信息区 -->
      <div class="user-profile-info">
        <div class="avatar-floating-wrapper">
          <DingtalkAvatar
            v-if="!isGroup"
            :src="userInfo?.avatar"
            :name="userName"
            :user-id="session?.targetId || session?.targetUserId"
            :size="80"
            shape="square"
            custom-class="profile-avatar"
          />
          <div v-else class="avatar-large group-avatar">
            <span class="material-icons-outlined">groups</span>
          </div>
          <span v-if="!isGroup && userInfo.online" class="online-status-dot"></span>
        </div>

        <div class="info-content">
          <div class="name-row">
            <h2 class="user-name">{{ userName }}</h2>
            <el-icon v-if="userInfo.gender === 1" class="gender-icon male"><Male /></el-icon>
            <el-icon v-else-if="userInfo.gender === 2" class="gender-icon female"><Female /></el-icon>
          </div>
          <p v-if="!isGroup" class="user-desc">
            {{ userInfo.position || '成员' }} | {{ userInfo.department || '默认部门' }}
          </p>
          <p v-else class="user-desc">{{ userInfo.memberCount || 0 }} 名成员</p>
        </div>
      </div>

      <!-- 快捷操作栏 -->
      <div class="action-bar">
        <div class="action-item" @click="handleSendMessage">
          <div class="action-icon chat-bg"><el-icon><ChatDotRound /></el-icon></div>
          <span>发消息</span>
        </div>
        <div class="action-item" @click="handleVoiceCall">
          <div class="action-icon phone-bg"><el-icon><Phone /></el-icon></div>
          <span>语音通话</span>
        </div>
        <div class="action-item" @click="handleVideoCall">
          <div class="action-icon video-bg"><el-icon><VideoCamera /></el-icon></div>
          <span>视频通话</span>
        </div>
      </div>

      <!-- 详细资料区域 -->
      <div class="details-section scrollbar-thin">
        <div class="detail-row">
          <span class="label">手机</span>
          <span class="value">{{ userInfo.phone || '未填写' }}</span>
        </div>
        <div class="detail-row">
          <span class="label">邮箱</span>
          <span class="value">{{ userInfo.email || '未填写' }}</span>
        </div>
        <div class="detail-row">
          <span class="label">个性签名</span>
          <p class="value text-italic">{{ userInfo.signature || '这个人很懒，什么都没留下' }}</p>
        </div>
      </div>

      <!-- 底部版权/标识 -->
      <div class="card-footer">
        <span>个人信息及状态已由钉钉加密保护</span>
      </div>
    </div>
    
    <div v-else-if="loading" class="card-loading">
      <el-skeleton animated>
        <template #template>
          <el-skeleton-item variant="image" style="height: 100px" />
          <div style="padding: 14px">
            <el-skeleton-item variant="h3" style="width: 50%" />
            <el-skeleton-item variant="text" style="margin-top: 10px" />
            <el-skeleton-item variant="text" style="width: 30%" />
          </div>
        </template>
      </el-skeleton>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Phone, VideoCamera, Close, Male, Female } from '@element-plus/icons-vue'
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

const isGroup = computed(() => props.session?.type === 'GROUP')
const userName = computed(() => {
  if (!userInfo.value) return ''
  return isGroup.value ? userInfo.value.name : userInfo.value.nickname || userInfo.value.username
})

// 根据名字计算一个温和的背景色
const avatarBgColor = computed(() => {
  const colors = ['#4A90E2', '#52c41a', '#f5222d', '#faad14', '#13c2c2', '#722ed1']
  const name = userName.value || 'IM'
  let hash = 0
  for (let i = 0; i < name.length; i++) hash += name.charCodeAt(i)
  return colors[hash % colors.length] + '22' // 15% opacity
})

const loadUserInfo = async () => {
  if (!props.session) return
  loading.value = true
  try {
    if (isGroup.value) {
      userInfo.value = { ...props.session, online: false }
    } else {
      const targetUserId = props.session.targetUserId || props.session.userId || props.session.targetId
      if (targetUserId) {
        const res = await getUserInfo(targetUserId)
        if (res.code === 200) userInfo.value = { ...res.data, online: Math.random() > 0.3 }
      } else {
        userInfo.value = { ...props.session, online: false }
      }
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (isOpen) => {
  if (isOpen) loadUserInfo()
})

const handleClose = () => {
  visible.value = false
  userInfo.value = null
}

const handleSendMessage = () => {
  emit('send-message', props.session)
  handleClose()
}

const handleVoiceCall = () => {
  ElMessage.info('语音通话功能开发中')
}

const handleVideoCall = () => {
  ElMessage.info('视频通话功能开发中')
}
</script>

<style scoped lang="scss">
:deep(.user-detail-dialog) {
  border-radius: 16px;
  overflow: hidden;
  padding: 0;

  .el-dialog__header {
    display: none;
  }

  .el-dialog__body {
    padding: 0;
  }
}

.user-card-content {
  background: #fff;

  .dark & {
    background: #1e293b;
  }
}

.card-cover {
  height: 120px;
  position: relative;
  background-image: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, rgba(0, 0, 0, 0.05) 100%);

  .close-card-btn {
    position: absolute;
    top: 12px;
    right: 12px;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.1);
    border: none;
    color: #fff;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;

    &:hover {
      background: rgba(0, 0, 0, 0.2);
    }
  }
}

.user-profile-info {
  padding: 0 24px;
  margin-top: -40px;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 10;

  .avatar-floating-wrapper {
    position: relative;
    width: 80px;
    height: 80px;
    margin-bottom: 12px;

    :deep(.profile-avatar) {
      border: 4px solid #fff;
      border-radius: 18px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

      .dark & {
        border-color: #1e293b;
      }
    }
  }

  .info-content {
    .name-row {
      display: flex;
      align-items: center;
      gap: 8px;

      .user-name {
        font-size: 20px;
        font-weight: 600;
        color: #1f2329;
        margin: 0;

        .dark & {
          color: #f1f5f9;
        }
      }

      .gender-icon {
        font-size: 14px;

        &.male {
          color: #1677ff;
        }

        &.female {
          color: #ff4d4f;
        }
      }
    }

    .user-desc {
      font-size: 13px;
      color: #8f959e;
      margin: 4px 0 0;

      .dark & {
        color: #94a3b8;
      }
    }
  }
}

.action-bar {
  display: flex;
  justify-content: space-around;
  padding: 24px 12px;
  border-bottom: 1px solid #f2f3f5;

  .dark & {
    border-color: #334155;
  }

  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    transition: transform 0.2s;

    &:hover {
      transform: translateY(-2px);
    }

    .action-icon {
      width: 44px;
      height: 44px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      color: #fff;

      &.chat-bg {
        background: #1677ff;
      }

      &.phone-bg {
        background: #52c41a;
      }

      &.video-bg {
        background: #13c2c2;
      }
    }

    span {
      font-size: 12px;
      color: #646a73;

      .dark & {
        color: #94a3b8;
      }
    }
  }
}

.details-section {
  padding: 16px 24px;
  max-height: 200px;
  overflow-y: auto;

  .detail-row {
    margin-bottom: 16px;

    .label {
      font-size: 12px;
      color: #8f959e;
      display: block;
      margin-bottom: 4px;

      .dark & {
        color: #94a3b8;
      }
    }

    .value {
      font-size: 14px;
      color: #1f2329;
      margin: 0;

      .dark & {
        color: #e2e8f0;
      }
    }

    .text-italic {
      font-style: italic;
      color: #646a73;

      .dark & {
        color: #94a3b8;
      }
    }
  }
}

.card-footer {
  padding: 12px;
  text-align: center;
  background: #f9fafb;

  .dark & {
    background: rgba(0, 0, 0, 0.1);
  }

  span {
    font-size: 11px;
    color: #bbbfc4;
  }
}

.online-status-dot {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 14px;
  height: 14px;
  background: #52c41a;
  border: 3px solid #fff;
  border-radius: 50%;

  .dark & {
    border-color: #1e293b;
  }
}

.card-loading {
  padding: 40px;
}
</style>
