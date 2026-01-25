<template>
  <el-dialog
    v-model="visible"
    width="440px"
    :show-close="false"
    class="personal-profile-dialog"
    destroy-on-close
    append-to-body
  >
    <div v-if="loading" v-loading="loading" class="loading-state"></div>
    <div v-else class="profile-container">
      <div class="profile-cover">
        <div class="cover-pattern"></div>
        <el-button class="circle-btn close-btn" circle @click="handleClose">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>

      <div class="profile-header two-column">
        <div class="avatar-area" aria-label="用户头像">
          <div class="avatar-ring">
            <DingtalkAvatar
              :src="currentUser.avatar"
              :name="currentUser.nickname || currentUser.username"
              :user-id="currentUser.id"
              :size="88"
              shape="circle"
              custom-class="user-avatar"
            />
          </div>
          <span class="status-dot" aria-hidden="true"></span>
        </div>
        <div class="profile-info">
          <div class="name-row">
            <h2 class="nickname">{{ currentUser.nickname || currentUser.username }}</h2>
            <el-icon v-if="currentUser.gender === 1" class="gender-icon male"><Male /></el-icon>
            <el-icon v-else-if="currentUser.gender === 2" class="gender-icon female"><Female /></el-icon>
            <el-tag size="small" type="success" class="status-tag">在线</el-tag>
          </div>
          <p class="account">账号：{{ currentUser.username }}</p>
          <button class="edit-profile-btn" @click="showEditDialog = true" aria-label="编辑资料">
            <el-icon><Edit /></el-icon>
            编辑资料
          </button>
        </div>
      </div>

      <div class="profile-details">
        <div class="detail-item">
          <div class="detail-icon position-icon">
            <el-icon><Briefcase /></el-icon>
          </div>
          <div class="detail-content">
            <span class="label">职位</span>
            <span class="value">{{ currentUser.position || '成员' }}</span>
          </div>
        </div>
        <div class="detail-item">
          <div class="detail-icon dept-icon">
            <el-icon><OfficeBuilding /></el-icon>
          </div>
          <div class="detail-content">
            <span class="label">部门</span>
            <span class="value">{{ currentUser.departmentName || currentUser.department || '未分配' }}</span>
          </div>
        </div>
        <div class="detail-item">
          <div class="detail-icon phone-icon">
            <el-icon><Phone /></el-icon>
          </div>
          <div class="detail-content">
            <span class="label">手机</span>
            <span class="value">{{ currentUser.mobile || '未填写' }}</span>
          </div>
        </div>
        <div class="detail-item">
          <div class="detail-icon email-icon">
            <el-icon><Message /></el-icon>
          </div>
          <div class="detail-content">
            <span class="label">邮箱</span>
            <span class="value">{{ currentUser.email || '未填写' }}</span>
          </div>
        </div>
        <div class="detail-item signature">
          <div class="detail-icon sign-icon">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <div class="detail-content">
            <span class="label">签名</span>
            <span class="value">{{ currentUser.signature || '这个人很懒，什么都没写~' }}</span>
          </div>
        </div>
      </div>

      <div class="profile-actions">
        <div class="action-grid">
          <button class="action-card" @click="showEditDialog = true">
            <div class="action-icon-wrapper edit-bg">
              <el-icon><Edit /></el-icon>
            </div>
            <span class="action-label">编辑资料</span>
          </button>

          <button class="action-card" @click="handleChangePassword">
            <div class="action-icon-wrapper lock-bg">
              <el-icon><Lock /></el-icon>
            </div>
            <span class="action-label">修改密码</span>
          </button>

          <button class="action-card" @click="handleStatusToggle">
            <div class="action-icon-wrapper status-bg">
              <el-icon><Refresh /></el-icon>
            </div>
            <span class="action-label">切换状态</span>
          </button>

          <button class="action-card danger" @click="handleLogout">
            <div class="action-icon-wrapper logout-bg">
              <el-icon><Operation /></el-icon>
            </div>
            <span class="action-label">退出登录</span>
          </button>
        </div>
      </div>
    </div>

    <EditProfileDialog 
      v-model:visible="showEditDialog"
      :user-info="currentUser"
      @save="handleSaveProfile"
    />

    <ChangePasswordDialog v-model="showChangePassword" />
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { Close, Male, Female, Edit, Operation, Lock, Refresh, Briefcase, OfficeBuilding, Phone, Message, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import EditProfileDialog from '@/components/EditProfileDialog/index.vue'
import ChangePasswordDialog from '@/components/Common/ChangePasswordDialog.vue'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue'])

const store = useStore()
const router = useRouter()
const visible = ref(false)
const loading = ref(false)
const showEditDialog = ref(false)
const showChangePassword = ref(false)
const userStatus = ref('online')

const currentUser = computed(() => store.getters['user/currentUser'] || {})

const statusLabel = computed(() => {
  const map = { online: '在线', busy: '忙碌', away: '离开', meeting: '会议中' }
  return map[userStatus.value]
})

const statusType = computed(() => {
  const map = { online: 'success', busy: 'danger', away: 'warning', meeting: 'info' }
  return map[userStatus.value]
})

const handleStatusToggle = () => {
  const statusOptions = [
    { label: '在线', value: 'online' },
    { label: '忙碌', value: 'busy' },
    { label: '离开', value: 'away' },
    { label: '会议中', value: 'meeting' }
  ]
  
  const currentIndex = statusOptions.findIndex(s => s.value === userStatus.value)
  const nextIndex = (currentIndex + 1) % statusOptions.length
  userStatus.value = statusOptions[nextIndex].value
  ElMessage.success(`状态已切换为: ${statusOptions[nextIndex].label}`)
}

const handleChangePassword = () => {
  showChangePassword.value = true
}

const handleClose = () => {
  emit('update:modelValue', false)
}

const handleSaveProfile = async (formData) => {
  try {
    await store.dispatch('user/updateProfile', formData)
    ElMessage.success('更新成功')
    showEditDialog.value = false
  } catch (error) {
    ElMessage.error(error.message || '更新失败')
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await store.dispatch('user/logout')
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {})
}

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  if (!val) {
    emit('update:modelValue', false)
  }
})
</script>

<style scoped lang="scss">
.personal-profile-dialog {
  :deep(.el-dialog__header) {
    display: none;
  }
  :deep(.el-dialog__body) {
    padding: 0;
    overflow: hidden;
    border-radius: var(--dt-radius-2xl);
  }
  :deep(.el-dialog) {
    border-radius: var(--dt-radius-2xl);
  }
}

.loading-state {
  height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-container {
  background: var(--dt-bg-card);
}

.profile-cover {
  height: 150px;
  background: linear-gradient(160deg, var(--dt-brand-color) 0%, var(--dt-brand-active) 100%);
  position: relative;
  overflow: hidden;

  .cover-pattern {
    position: absolute;
    inset: 0;
    background-image:
      radial-gradient(circle at 20% 30%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
      radial-gradient(circle at 80% 70%, rgba(255, 255, 255, 0.08) 0%, transparent 50%);
  }

  &::after {
    content: "";
    position: absolute;
    inset: 0;
    background-image: radial-gradient(circle at 2px 2px, rgba(255, 255, 255, 0.1) 1px, transparent 0);
    background-size: 24px 24px;
  }

  .close-btn {
    position: absolute;
    top: 16px;
    right: 16px;
    z-index: 10;
    background: rgba(255, 255, 255, 0.15);
    border: none;
    color: #fff;
    width: 36px;
    height: 36px;
    transition: all var(--dt-transition-base);
    backdrop-filter: blur(10px);

    &:hover {
      background: rgba(255, 255, 255, 0.25);
      transform: rotate(90deg) scale(1.1);
    }
  }
}

.profile-header.two-column {
  display: flex;
  align-items: center;
  padding: 0 28px 16px;
  gap: 16px;
  margin-top: -44px;
  position: relative;
  z-index: 10;

  .avatar-area {
    width: 88px;
    height: 88px;
    display: flex;
    flex-direction: column;
    align-items: center;
    position: relative;
    flex-shrink: 0;

    .avatar-ring {
      padding: 4px;
      background: linear-gradient(160deg, var(--dt-brand-color) 0%, var(--dt-brand-active) 100%);
      border-radius: 50%;
      box-shadow: var(--dt-shadow-float);

      .user-avatar {
        width: 80px;
        height: 80px;
        border-radius: 50%;
        border: 3px solid var(--dt-bg-card);
      }
    }

    .status-dot {
      position: absolute;
      bottom: 4px;
      right: 4px;
      width: 14px;
      height: 14px;
      border: 3px solid var(--dt-bg-card);
      border-radius: 50%;
      background: var(--dt-success-color);
      box-shadow: 0 2px 8px rgba(82, 196, 26, 0.4);
    }
  }

  .profile-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-width: 0;

    .name-row {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
      margin-bottom: 6px;

      .nickname {
        margin: 0;
        font-size: 20px;
        font-weight: 700;
        color: var(--dt-text-primary);
      }

      .gender-icon {
        font-size: 18px;

        &.male {
          color: var(--dt-brand-color);
        }

        &.female {
          color: #ec4899;
        }
      }

      .status-tag {
        background: var(--dt-success-color);
        border: none;
        color: #fff;
        font-weight: 500;
      }
    }

    .account {
      margin: 0 0 10px;
      font-size: 13px;
      color: var(--dt-text-secondary);
      font-weight: 500;
    }

    .edit-profile-btn {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      height: 28px;
      padding: 0 14px;
      border-radius: var(--dt-radius-md);
      font-size: 12px;
      border: 1px solid var(--dt-border-color);
      background: var(--dt-bg-body);
      color: var(--dt-text-secondary);
      cursor: pointer;
      transition: all var(--dt-transition-fast);
      font-weight: 500;

      &:hover {
        color: var(--dt-brand-color);
        border-color: var(--dt-brand-color);
        background: var(--dt-brand-bg);
        transform: translateY(-1px);
      }
    }
  }
}

.profile-details {
  padding: 0 28px 20px;

  .detail-item {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 14px 8px;
    border-bottom: 1px solid var(--dt-border-light);
    transition: all var(--dt-transition-fast);
    border-radius: var(--dt-radius-md);

    &:hover {
      background: var(--dt-bg-body);

      .detail-icon {
        transform: scale(1.1);
      }
    }

    &:last-child {
      border-bottom: none;
    }

    .detail-icon {
      width: 36px;
      height: 36px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18px;
      color: #fff;
      flex-shrink: 0;
      transition: all var(--dt-transition-fast);

      &.position-icon {
        background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
        box-shadow: 0 4px 12px rgba(245, 158, 11, 0.25);
      }

      &.dept-icon {
        background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
        box-shadow: 0 4px 12px rgba(59, 130, 246, 0.25);
      }

      &.phone-icon {
        background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
        box-shadow: 0 4px 12px rgba(16, 185, 129, 0.25);
      }

      &.email-icon {
        background: linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%);
        box-shadow: 0 4px 12px rgba(139, 92, 246, 0.25);
      }

      &.sign-icon {
        background: linear-gradient(135deg, #ec4899 0%, #f472b6 100%);
        box-shadow: 0 4px 12px rgba(236, 72, 153, 0.25);
      }
    }

    .detail-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 4px;
      min-width: 0;

      .label {
        font-size: 12px;
        color: var(--dt-text-tertiary);
        font-weight: 500;
      }

      .value {
        font-size: 14px;
        color: var(--dt-text-primary);
        font-weight: 600;
        word-break: break-all;
      }
    }

    &.signature {
      align-items: flex-start;

      .detail-icon {
        margin-top: 2px;
      }

      .value {
        color: var(--dt-text-secondary);
        font-weight: 500;
        line-height: 1.6;
      }
    }
  }
}

.profile-actions {
  padding: 0 28px 28px;

  .action-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .action-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 12px;
    padding: 20px 12px;
    border: 1.5px solid var(--dt-border-color);
    border-radius: var(--dt-radius-xl);
    background: var(--dt-bg-card);
    cursor: pointer;
    transition: all var(--dt-transition-base);

    &:hover {
      transform: translateY(-3px);
      box-shadow: var(--dt-shadow-float);
      border-color: var(--dt-brand-color);

      .action-icon-wrapper {
        transform: scale(1.15) rotate(5deg);
      }
    }

    &:active {
      transform: translateY(-1px);
    }

    &.danger:hover {
      border-color: var(--dt-error-color);
    }

    .action-icon-wrapper {
      width: 52px;
      height: 52px;
      border-radius: var(--dt-radius-lg);
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all var(--dt-transition-base);

      .el-icon {
        font-size: 24px;
        color: #fff;
      }

      &.edit-bg {
        background: linear-gradient(135deg, var(--dt-brand-color) 0%, var(--dt-brand-active) 100%);
        box-shadow: 0 6px 16px rgba(22, 119, 255, 0.35);
      }

      &.lock-bg {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        box-shadow: 0 6px 16px rgba(245, 87, 108, 0.35);
      }

      &.status-bg {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        box-shadow: 0 6px 16px rgba(79, 172, 254, 0.35);
      }

      &.logout-bg {
        background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
        box-shadow: 0 6px 16px rgba(250, 112, 154, 0.35);
      }
    }

    .action-label {
      font-size: 14px;
      font-weight: 600;
      color: var(--dt-text-primary);
      transition: color var(--dt-transition-fast);
    }
  }
}

.status-indicator {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 8px;

  &.online {
    background: var(--dt-success-color);
  }

  &.busy {
    background: var(--dt-error-color);
  }

  &.away {
    background: var(--dt-warning-color);
  }

  &.meeting {
    background: var(--dt-brand-color);
  }
}

// 暗色模式
.dark .personal-profile-dialog {
  .profile-container {
    background: var(--dt-bg-card-dark);
  }

  .profile-header .avatar-area .avatar-ring .user-avatar {
    border-color: var(--dt-bg-card-dark);
  }

  .profile-header .avatar-area .status-dot {
    border-color: var(--dt-bg-card-dark);
  }

  .profile-header .profile-info {
    .name-row .nickname {
      color: var(--dt-text-primary-dark);
    }

    .account {
      color: var(--dt-text-secondary-dark);
    }

    .edit-profile-btn {
      background: var(--dt-bg-hover-dark);
      border-color: var(--dt-border-dark);
      color: var(--dt-text-secondary-dark);

      &:hover {
        background: var(--dt-bg-active-dark);
        color: var(--dt-brand-color);
        border-color: var(--dt-brand-color);
      }
    }
  }

  .profile-details .detail-item {
    border-bottom-color: var(--dt-border-dark);

    &:hover {
      background: var(--dt-bg-hover-dark);
    }

    .detail-content .value {
      color: var(--dt-text-primary-dark);
    }

    &.signature .value {
      color: var(--dt-text-secondary-dark);
    }
  }

  .profile-actions .action-card {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);

    .action-label {
      color: var(--dt-text-primary-dark);
    }
  }
}
</style>
