<template>
  <el-dialog
    v-model="visible"
    width="580px"
    :show-close="false"
    class="dingtalk-profile-dialog"
    append-to-body
    destroy-on-close
  >
    <div v-if="loading" v-loading="loading" class="loading-state" />
    <div v-else-if="userDetail" class="profile-container">
      <!-- 顶部背景 -->
      <div class="profile-header-bg" />

      <!-- 主体内容 -->
      <div class="profile-body">
        <!-- 左侧头像 -->
        <div class="profile-left">
          <div class="avatar-wrapper">
            <img
              v-if="userDetail.avatar"
              :src="userDetail.avatar"
              alt="avatar"
              class="avatar-img"
            >
            <div v-else class="avatar-placeholder">
              {{ (userDetail.nickname || userDetail.username || '?').charAt(0) }}
            </div>
          </div>
          <div class="user-status">
            <span class="status-dot online" />
            <span class="status-text">在线</span>
          </div>
        </div>

        <!-- 右侧信息 -->
        <div class="profile-right">
          <div class="user-base-info">
            <h2 class="user-name">
              {{ userDetail.nickname || userDetail.username }}
            </h2>
            <div class="user-dept">
              {{ userDetail.departmentName || userDetail.department || '公司组织' }}
            </div>
            <div class="user-position">
              {{ userDetail.position || '成员' }}
            </div>
          </div>

          <div class="user-detail-list">
            <div class="detail-row">
              <span class="label">工号</span>
              <span class="value">{{ userDetail.username }}</span>
            </div>
            <div class="detail-row">
              <span class="label">手机</span>
              <span class="value">{{ userDetail.mobile || '未公开' }}</span>
            </div>
            <div class="detail-row">
              <span class="label">邮箱</span>
              <span class="value">{{ userDetail.email || '未设置' }}</span>
            </div>
            <div v-if="userDetail.signature" class="detail-row">
              <span class="label">签名</span>
              <span class="value signature">{{ userDetail.signature }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部操作栏 -->
      <div class="profile-footer">
        <div class="footer-left">
          <el-button class="more-btn" @click="handleOpenMore">
            <el-icon><MoreFilled /></el-icon>
            更多
          </el-button>
        </div>
        <div class="footer-right">
          <el-button class="call-btn voice" @click="handleStartCall('voice')">
            <el-icon><Phone /></el-icon>
            语音
          </el-button>
          <el-button class="call-btn video" @click="handleStartCall('video')">
            <el-icon><VideoCamera /></el-icon>
            视频
          </el-button>
          <el-button type="primary" class="chat-btn" @click="handleStartChat">
            <el-icon><ChatDotRound /></el-icon>
            发消息
          </el-button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { MoreFilled, Phone, VideoCamera, ChatDotRound } from '@element-plus/icons-vue'
import { getUserInfo } from '@/api/im/user'
import { createConversation } from '@/api/im/conversation'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'

const props = defineProps({ modelValue: Boolean, userId: [String, Number] })
const emit = defineEmits(['update:modelValue', 'chat', 'start-call'])

const store = useStore()
const visible = ref(false)
const loading = ref(false)
const userDetail = ref(null)

const handleClose = () => emit('update:modelValue', false)

const loadUserDetail = async () => {
  if (!props.userId) return
  loading.value = true
  try {
    const res = await getUserInfo(props.userId)
    if (res.code === 200) userDetail.value = res.data
  } finally { loading.value = false }
}

const handleStartChat = async () => {
  try {
    const res = await createConversation({ type: 'PRIVATE', targetId: props.userId })
    if (res.code === 200) {
      store.commit('im/session/SET_CURRENT_SESSION', res.data)
      handleClose()
    }
  } catch { ElMessage.error('无法发起对话') }
}

const handleStartCall = (type) => { emit('start-call', { type, user: userDetail.value }); handleClose() }
const handleOpenMore = () => ElMessage.info('权限管理/加入黑名单功能即将上线')

watch(() => props.modelValue, (val) => { visible.value = val; if (val && props.userId) loadUserDetail() })
watch(visible, (val) => { if (!val) emit('update:modelValue', false) })
</script>

<style scoped lang="scss">
.dingtalk-profile-dialog {
  :deep(.el-dialog) {
    border-radius: 8px;
    overflow: hidden;
    padding: 0;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  }
  :deep(.el-dialog__header) { display: none; }
  :deep(.el-dialog__body) { padding: 0; }
}

.profile-container {
  background: #fff;
}

.profile-header-bg {
  height: 100px;
  background: linear-gradient(135deg, #0089ff 0%, #00c7c7 100%);
}

.profile-body {
  display: flex;
  padding: 0 32px 24px;
  margin-top: -50px;
  position: relative;
}

.profile-left {
  flex-shrink: 0;
  margin-right: 24px;
  text-align: center;

  .avatar-wrapper {
    width: 100px;
    height: 100px;
    border-radius: 8px;
    overflow: hidden;
    border: 4px solid #fff;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    background: #f0f2f5;

    .avatar-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .avatar-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 36px;
      font-weight: 600;
      color: #0089ff;
    }
  }

  .user-status {
    margin-top: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;

    .status-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;

      &.online {
        background: #52c41a;
      }
    }

    .status-text {
      font-size: 12px;
      color: #666;
    }
  }
}

.profile-right {
  flex: 1;
  min-width: 0;
}

.user-base-info {
  margin-bottom: 20px;

  .user-name {
    font-size: 22px;
    font-weight: 600;
    color: #171a1d;
    margin: 0 0 8px 0;
  }

  .user-dept {
    font-size: 14px;
    color: #171a1d;
    margin-bottom: 4px;
  }

  .user-position {
    font-size: 13px;
    color: #858e99;
  }
}

.user-detail-list {
  background: #f6f8fa;
  border-radius: 8px;
  padding: 16px;

  .detail-row {
    display: flex;
    padding: 8px 0;
    font-size: 14px;

    .label {
      width: 48px;
      color: #858e99;
      flex-shrink: 0;
    }

    .value {
      color: #171a1d;
      flex: 1;
      word-break: break-all;

      &.signature {
        color: #666;
        font-style: italic;
      }
    }
  }
}

.profile-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 32px;
  border-top: 1px solid #e7e9e8;
  background: #fafbfc;

  .footer-right {
    display: flex;
    gap: 12px;

    .el-button {
      padding: 8px 20px;
      font-size: 14px;
      border-radius: 4px;
    }

    .call-btn {
      display: flex;
      align-items: center;
      gap: 6px;
      background: #f0f5ff;
      border-color: #d9e4ff;
      color: #0089ff;

      &:hover {
        background: #e5f0ff;
        border-color: #adc6ff;
      }
    }

    .chat-btn {
      background: #0089ff;
      border-color: #0089ff;
      display: flex;
      align-items: center;
      gap: 6px;

      &:hover {
        background: #4096ff;
        border-color: #4096ff;
      }
    }
  }

  .more-btn {
    color: #858e99;
    font-size: 13px;

    &:hover {
      color: #171a1d;
    }
  }
}
</style>
