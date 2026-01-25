<template>
  <el-dialog
    v-model="visible"
    width="400px"
    :show-close="false"
    class="user-profile-dialog"
    destroy-on-close
    append-to-body
  >
    <div v-if="loading" v-loading="loading" class="loading-state"></div>
    <div v-else-if="userDetail" class="profile-container">
      <!-- 顶部封面与关闭按钮 -->
      <div class="profile-cover">
        <el-button class="close-btn" circle @click="handleClose">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>

      <!-- 用户核心信息 -->
      <div class="profile-header">
        <div class="avatar-wrapper">
          <el-avatar :size="80" :src="addTokenToUrl(userDetail.avatar)" class="user-avatar">
            {{ userDetail.nickname?.charAt(0) || userDetail.username?.charAt(0) }}
          </el-avatar>
          <div v-if="userDetail.online" class="online-status"></div>
        </div>
        
        <div class="user-main">
          <div class="name-row">
            <h2 class="nickname">{{ userDetail.nickname || userDetail.username }}</h2>
            <el-icon v-if="userDetail.gender === 1" class="gender-icon male"><Male /></el-icon>
            <el-icon v-else-if="userDetail.gender === 2" class="gender-icon female"><Female /></el-icon>
          </div>
          <p class="account">账号：{{ userDetail.username }}</p>
        </div>
      </div>

      <!-- 详细资料项 -->
      <div class="profile-details">
        <div class="detail-item">
          <span class="label">职位</span>
          <span class="value">{{ userDetail.position || '成员' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">部门</span>
          <span class="value">{{ userDetail.departmentName || userDetail.department || '未分配' }}</span>
        </div>
        <div v-if="userDetail.mobile" class="detail-item">
          <span class="label">手机</span>
          <span class="value">{{ userDetail.mobile }}</span>
        </div>
        <div v-if="userDetail.email" class="detail-item">
          <span class="label">邮箱</span>
          <span class="value">{{ userDetail.email }}</span>
        </div>
        <div v-if="userDetail.signature" class="detail-item signature">
          <span class="label">签名</span>
          <span class="value">{{ userDetail.signature }}</span>
        </div>
      </div>

      <!-- 底部操作栏 -->
      <div class="profile-actions">
        <el-button type="primary" class="action-btn" @click="handleStartChat">
          <el-icon><ChatDotRound /></el-icon>
          <span>发消息</span>
        </el-button>
        <el-button class="action-btn" @click="handleStartCall">
          <el-icon><Phone /></el-icon>
        </el-button>
        <el-button class="action-btn" @click="handleMore">
          <el-icon><MoreFilled /></el-icon>
        </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Close, Male, Female, ChatDotRound, Phone, MoreFilled } from '@element-plus/icons-vue'
import { getUserInfo } from '@/api/im/user'
import { createConversation } from '@/api/im/conversation'
import { addTokenToUrl } from '@/utils/file'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: Boolean,
  userId: [String, Number]
})

const emit = defineEmits(['update:modelValue', 'chat'])

const store = useStore()
const visible = ref(false)
const loading = ref(false)
const userDetail = ref(null)

const handleClose = () => {
  emit('update:modelValue', false)
}

const loadUserDetail = async () => {
  if (!props.userId) return
  loading.value = true
  try {
    const res = await getUserInfo(props.userId)
    if (res.code === 200) {
      userDetail.value = res.data
    }
  } catch (error) {
    console.error('获取用户详情失败:', error)
  } finally {
    loading.value = false
  }
}

const handleStartChat = async () => {
  try {
    const res = await createConversation({
      type: 'PRIVATE',
      targetId: props.userId
    })
    if (res.code === 200) {
      store.commit('im/session/SET_CURRENT_SESSION', res.data)
      window.dispatchEvent(new CustomEvent('switch-tab', { detail: 'chat' }))
      handleClose()
    }
  } catch (error) {
    ElMessage.error('无法发起聊天')
  }
}

const handleStartCall = () => {
  ElMessage.info('语音通话功能开发中...')
}

const handleMore = () => {
  // 更多操作逻辑
}

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.userId) {
    loadUserDetail()
  }
})

watch(visible, (val) => {
  if (!val) {
    emit('update:modelValue', false)
  }
})
</script>

<style scoped lang="scss">
.user-profile-dialog :deep(.el-dialog__header) {
  display: none;
}

.user-profile-dialog :deep(.el-dialog__body) {
  padding: 0;
  overflow: hidden;
  border-radius: 12px;
}

.loading-state {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-container {
  background: #fff;
}

.profile-cover {
  height: 120px;
  background: linear-gradient(135deg, #1677ff 0%, #00d2ff 100%);
  position: relative;

  .close-btn {
    position: absolute;
    top: 12px;
    right: 12px;
    background: rgba(0, 0, 0, 0.2);
    border: none;
    color: #fff;
    &:hover {
      background: rgba(0, 0, 0, 0.4);
    }
  }
}

.profile-header {
  padding: 0 24px;
  margin-top: -40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24px;

  .avatar-wrapper {
    position: relative;
    margin-bottom: 12px;
    .user-avatar {
      border: 4px solid #fff;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      background: #f0f2f5;
      font-size: 28px;
    }
    .online-status {
      position: absolute;
      right: 4px;
      bottom: 4px;
      width: 16px;
      height: 16px;
      background: #52c41a;
      border: 3px solid #fff;
      border-radius: 50%;
    }
  }

  .user-main {
    text-align: center;
    .name-row {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      .nickname {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
        color: #1f2329;
      }
      .gender-icon {
        font-size: 16px;
        &.male { color: #1677ff; }
        &.female { color: #ff4d4f; }
      }
    }
    .account {
      margin: 4px 0 0;
      font-size: 13px;
      color: #8f959e;
    }
  }
}

.profile-details {
  padding: 0 24px;
  margin-bottom: 24px;
  
  .detail-item {
    display: flex;
    justify-content: space-between;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;
    font-size: 14px;
    
    &:last-child {
      border-bottom: none;
    }

    .label {
      color: #8f959e;
      flex-shrink: 0;
      width: 60px;
    }
    
    .value {
      color: #1f2329;
      text-align: right;
      word-break: break-all;
    }
    
    &.signature {
      flex-direction: column;
      .value {
        text-align: left;
        margin-top: 4px;
        color: #646a73;
        font-style: italic;
      }
    }
  }
}

.profile-actions {
  padding: 16px 24px 24px;
  display: flex;
  gap: 12px;
  
  .action-btn {
    height: 40px;
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    
    &:not(.el-button--primary) {
      flex: 0 0 40px;
      padding: 0;
    }
  }
}

:global(.dark) {
  .profile-container {
    background: #1e293b;
    .detail-item {
      border-color: #334155;
      .nickname { color: #f1f5f9; }
      .value { color: #f1f5f9; }
    }
  }
}
</style>
