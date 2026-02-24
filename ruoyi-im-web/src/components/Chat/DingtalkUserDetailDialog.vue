<template>
  <el-dialog
    v-model="dialogVisible"
    :width="420"
    :modal="true"
    :show-close="true"
    :close-on-click-modal="true"
    :close-on-press-escape="true"
    custom-class="dingtalk-user-detail-dialog"
    destroy-on-close
  >
    <div v-if="userInfo" class="user-detail-content">
      <!-- 用户头像和基本信息 -->
      <div class="user-header">
        <DingtalkAvatar
          :src="userInfo.avatar"
          :name="userInfo.nickName || userInfo.userName"
          :user-id="userInfo.userId"
          :size="80"
          shape="circle"
          class="user-large-avatar"
        />
        <div class="user-basic-info">
          <div class="user-name">{{ userInfo.nickName || userInfo.userName }}</div>
          <div class="user-status">
            <span :class="['status-indicator', userInfo.online ? 'online' : 'offline']"></span>
            <span class="status-text">{{ userInfo.online ? '在线' : '离线' }}</span>
          </div>
          <div v-if="userInfo.signature" class="user-signature">{{ userInfo.signature }}</div>
        </div>
      </div>

      <!-- 快捷操作 -->
      <div class="quick-actions">
        <el-button type="primary" @click="sendMessage" class="action-btn">
          <span class="material-icons-outlined btn-icon">chat</span>
          <span>发消息</span>
        </el-button>
        <el-button @click="makeVoiceCall" class="action-btn secondary">
          <span class="material-icons-outlined btn-icon">call</span>
          <span>语音</span>
        </el-button>
        <el-button @click="makeVideoCall" class="action-btn secondary">
          <span class="material-icons-outlined btn-icon">video_call</span>
          <span>视频</span>
        </el-button>
      </div>

      <!-- 用户详细信息 -->
      <div class="user-details">
        <div class="detail-item">
          <span class="detail-label">用户名</span>
          <span class="detail-value">{{ userInfo.userName }}</span>
        </div>
        <div v-if="userInfo.department" class="detail-item">
          <span class="detail-label">部门</span>
          <span class="detail-value">{{ userInfo.department }}</span>
        </div>
        <div v-if="userInfo.position" class="detail-item">
          <span class="detail-label">职位</span>
          <span class="detail-value">{{ userInfo.position }}</span>
        </div>
        <div v-if="userInfo.email" class="detail-item">
          <span class="detail-label">邮箱</span>
          <span class="detail-value">{{ userInfo.email }}</span>
        </div>
        <div v-if="userInfo.phone" class="detail-item">
          <span class="detail-label">电话</span>
          <span class="detail-value">{{ userInfo.phone }}</span>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElDialog, ElButton } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { getUserInfo } from '@/api/im/user'

const props = defineProps({
  userId: {
    type: [String, Number],
    required: true
  },
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'send-message', 'voice-call', 'video-call'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const userInfo = ref(null)
const loading = ref(false)

// 监听用户ID变化并加载用户信息
watch(
  () => props.userId,
  async (newUserId) => {
    if (newUserId) {
      await loadUserInfo()
    }
  },
  { immediate: true }
)

const loadUserInfo = async () => {
  if (!props.userId) return

  loading.value = true
  try {
    const response = await getUserInfo(props.userId)
    if (response.code === 200) {
      userInfo.value = response.data
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  } finally {
    loading.value = false
  }
}

const sendMessage = () => {
  emit('send-message', props.userId)
  emit('update:visible', false)
}

const makeVoiceCall = () => {
  emit('voice-call', props.userId)
  emit('update:visible', false)
}

const makeVideoCall = () => {
  emit('video-call', props.userId)
  emit('update:visible', false)
}
</script>

<style lang="scss">
.dingtalk-user-detail-dialog {
  .el-dialog {
    border-radius: 8px;
    overflow: hidden;
  }

  .el-dialog__header {
    padding: 0;
  }

  .el-dialog__body {
    padding: 0;
  }
}

.user-detail-content {
  padding: 24px;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;

  .user-large-avatar {
    border-radius: 50%;
  }

  .user-basic-info {
    flex: 1;

    .user-name {
      font-size: 18px;
      font-weight: 600;
      color: #1d2129;
      margin-bottom: 8px;
    }

    .user-status {
      display: flex;
      align-items: center;
      gap: 6px;
      margin-bottom: 8px;

      .status-indicator {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        display: inline-block;

        &.online {
          background: #00b42a;
        }

        &.offline {
          background: #86909c;
        }
      }

      .status-text {
        font-size: 14px;
        color: #86909c;
      }
    }

    .user-signature {
      font-size: 13px;
      color: #86909c;
      line-height: 1.5;
    }
  }
}

.quick-actions {
  display: flex;
  gap: 12px;
  padding: 20px 0;

  .action-btn {
    flex: 1;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;

    &.secondary {
      background: #f7f8fa;
      border: 1px solid #e5e6eb;
      color: #1d2129;

      &:hover {
        background: #f2f3f5;
      }
    }

    .btn-icon {
      font-size: 18px;
    }
  }
}

.user-details {
  .detail-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 0;
    border-bottom: 1px solid #f7f8fa;

    &:last-child {
      border-bottom: none;
    }

    .detail-label {
      font-size: 14px;
      color: #86909c;
      flex-shrink: 0;
      width: 60px;
    }

    .detail-value {
      font-size: 14px;
      color: #1d2129;
      text-align: right;
      flex: 1;
      margin-left: 16px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

// 暗色模式
:global(.dark) {
  .dingtalk-user-detail-dialog .el-dialog {
    background: #1d2129;
  }

  .user-header {
    border-bottom-color: #36393d;

    .user-name {
      color: #e2e4e9;
    }
  }

  .user-details .detail-item {
    border-bottom-color: #36393d;

    .detail-label {
      color: #929293;
    }

    .detail-value {
      color: #e2e4e9;
    }
  }

  .quick-actions .action-btn.secondary {
    background: #2a2d35;
    border-color: #3f424a;
    color: #e2e4e9;
  }
}
</style>