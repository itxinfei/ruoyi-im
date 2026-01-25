<template>
  <el-dialog
    v-model="visible"
    width="420px"
    :show-close="false"
    class="personal-profile-dialog"
    destroy-on-close
    append-to-body
  >
    <div v-if="loading" v-loading="loading" class="loading-state"></div>
    <div v-else class="profile-container">
      <!-- 顶部封面与关闭按钮 -->
      <div class="profile-cover">
        <el-button class="circle-btn close-btn" circle @click="handleClose">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>

      <!-- 用户核心信息（新：两列布局，头像左、信息右） -->
      <div class="profile-header two-column">
        <div class="avatar-area" aria-label="用户头像">
          <DingtalkAvatar
            :src="currentUser.avatar"
            :name="currentUser.nickname || currentUser.username"
            :user-id="currentUser.id"
            :size="88"
            shape="circle"
            custom-class="user-avatar"
          />
          <span class="status-dot" aria-hidden="true"></span>
        </div>
        <div class="profile-info">
          <div class="name-row" style="justify-content:flex-start; align-items:center;">
            <h2 class="nickname">{{ currentUser.nickname || currentUser.username }}</h2>
            <el-icon v-if="currentUser.gender === 1" class="gender-icon male"><Male /></el-icon>
            <el-icon v-else-if="currentUser.gender === 2" class="gender-icon female"><Female /></el-icon>
            <el-tag size="small" type="success" class="status-tag" style="margin-left:8px;">在线</el-tag>
            <button class="edit-profile-btn" @click="showEditDialog = true" aria-label="编辑资料">编辑资料</button>
          </div>
          <p class="account" style="margin:4px 0 0;">账号：{{ currentUser.username }}</p>
        </div>
      </div>

      <!-- 详细资料项 -->
      <div class="profile-details">
        <div class="detail-item">
          <span class="label">职位</span>
          <span class="value">{{ currentUser.position || '成员' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">部门</span>
          <span class="value">{{ currentUser.departmentName || currentUser.department || '未分配' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">手机</span>
          <span class="value">{{ currentUser.mobile || '未填写' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">邮箱</span>
          <span class="value">{{ currentUser.email || '未填写' }}</span>
        </div>
        <div class="detail-item signature">
          <span class="label">签名</span>
          <span class="value">{{ currentUser.signature || '这个人很懒，什么都没写~' }}</span>
        </div>
      </div>

      <!-- 底部操作栏 -->
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
              <el-icon><SwitchButton /></el-icon>
            </div>
            <span class="action-label">退出登录</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 内嵌编辑弹窗 -->
    <EditProfileDialog 
      v-model:visible="showEditDialog"
      :user-info="currentUser"
      @save="handleSaveProfile"
    />

    <!-- 修改密码弹窗 -->
    <ChangePasswordDialog v-model="showChangePassword" />
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { Close, Male, Female, Edit, SwitchButton, Lock, Refresh, Right } from '@element-plus/icons-vue'
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

const showChangePassword = ref(false)

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
.personal-profile-dialog :deep(.el-dialog__header) {
  display: none;
}

.personal-profile-dialog :deep(.el-dialog__body) {
  padding: 0;
  overflow: hidden;
  border-radius: 12px;
}

.loading-state {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-container {
  background: #fff;
  .dark & {
    background: #1e293b;
  }
}

.profile-cover {
  height: 140px;
  background: linear-gradient(135deg, #0089ff 0%, #00d2ff 100%);
  position: relative;
  overflow: hidden;

  &::after {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: radial-gradient(circle at 2px 2px, rgba(255, 255, 255, 0.15) 1px, transparent 0);
    background-size: 20px 20px;
  }

  .dark & {
    background: linear-gradient(135deg, #1e40af 0%, #1e3a8a 100%);
  }

  .close-btn {
    position: absolute;
    top: 16px;
    right: 16px;
    z-index: 10;
    background: rgba(255, 255, 255, 0.2);
    border: none;
    color: #fff;
    width: 32px;
    height: 32px;
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
    backdrop-filter: blur(8px);

    &:hover {
      background: rgba(255, 255, 255, 0.35);
      transform: rotate(90deg);
    }
  }
}

.profile-header.two-column {
  display: flex;
  align-items: center;
  padding: 12px 32px;
  gap: 20px;
  border-bottom: 1px solid #f2f3f5;
  margin-top: -44px;
  position: relative;
  z-index: 10;

  .dark & {
    border-bottom-color: #334155;
  }

  .avatar-area {
    width: 88px;
    height: 88px;
    display: flex;
    flex-direction: column;
    align-items: center;
    position: relative;
    flex-shrink: 0;

    .user-avatar {
      width: 88px;
      height: 88px;
      border-radius: 50%;
      border: 4px solid #fff;
      box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);

      .dark & {
        border-color: #1e293b;
      }
    }

    .status-dot {
      position: absolute;
      bottom: 6px;
      right: 6px;
      width: 12px;
      height: 12px;
      border: 2px solid #fff;
      border-radius: 50%;
      background: #52c41a;

      .dark & {
        border-color: #1e293b;
      }
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

      .nickname {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
        color: #1f2329;

        .dark & {
          color: #f1f5f9;
        }
      }

      .gender-icon {
        font-size: 16px;

        &.male {
          color: #1677ff;
        }

        &.female {
          color: #ff4d4f;
        }
      }

      .edit-profile-btn {
        margin-left: auto;
        height: 28px;
        padding: 0 12px;
        border-radius: 6px;
        font-size: 12px;
        border: 1px solid #dcdfe6;
        background: #fff;
        color: #606266;
        cursor: pointer;
        transition: all 0.2s;

        &:hover {
          color: #409eff;
          border-color: #409eff;
          background: #ecf5ff;
        }

        .dark & {
          background: #334155;
          border-color: #475569;
          color: #94a3b8;

          &:hover {
            background: #475569;
            color: #60a5fa;
            border-color: #60a5fa;
          }
        }
      }
    }

    .account {
      margin: 4px 0 0;
      font-size: 13px;
      color: #8f959e;

      .dark & {
        color: #94a3b8;
      }
    }
  }
}

.profile-details {
  padding: 0 32px;
  margin-bottom: 32px;

  .detail-item {
    display: flex;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f2f3f5;
    transition: background 0.2s;

    .dark & {
      border-bottom-color: #334155;
    }

    &:last-child {
      border-bottom: none;
    }

    .label {
      width: 60px;
      font-size: 13px;
      color: #8f959e;
      flex-shrink: 0;

      .dark & {
        color: #94a3b8;
      }
    }

    .value {
      flex: 1;
      font-size: 14px;
      color: #1f2329;
      font-weight: 500;
      word-break: break-all;

      .dark & {
        color: #f1f5f9;
      }
    }

    &.signature {
      align-items: flex-start;

      .value {
        color: #64748b;
        font-weight: normal;
        line-height: 1.5;

        .dark & {
          color: #94a3b8;
        }
      }
    }
  }
}

.profile-actions {
  padding: 0 32px 32px;

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
    padding: 20px 16px;
    border: 1px solid #e5e7eb;
    border-radius: 12px;
    background: #fff;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    .dark & {
      background: #334155;
      border-color: #475569;
    }

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
      border-color: #409eff;

      .dark & {
        box-shadow: 0 8px 20px rgba(0, 0, 0, 0.3);
        border-color: #60a5fa;
      }

      .action-icon-wrapper {
        transform: scale(1.1);
      }
    }

    &:active {
      transform: translateY(0);
    }

    &.danger:hover {
      border-color: #ef4444;

      .dark & {
        border-color: #f87171;
      }
    }

    .action-icon-wrapper {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

      .el-icon {
        font-size: 22px;
        color: #fff;
      }

      &.edit-bg {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
      }

      &.lock-bg {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        box-shadow: 0 4px 12px rgba(245, 87, 108, 0.3);
      }

      &.status-bg {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        box-shadow: 0 4px 12px rgba(79, 172, 254, 0.3);
      }

      &.logout-bg {
        background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
        box-shadow: 0 4px 12px rgba(250, 112, 154, 0.3);
      }
    }

    .action-label {
      font-size: 14px;
      font-weight: 500;
      color: #374151;
      transition: color 0.2s;

      .dark & {
        color: #e5e7eb;
      }
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
    background: #52c41a;
  }

  &.busy {
    background: #f5222d;
  }

  &.away {
    background: #faad14;
  }

  &.meeting {
    background: #1890ff;
  }
}
</style>
