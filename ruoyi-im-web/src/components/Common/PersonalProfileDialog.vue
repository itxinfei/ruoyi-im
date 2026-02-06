<template>
  <el-dialog
    v-model="visible"
    width="720px"
    class="personal-profile-dialog"
    destroy-on-close
    append-to-body
    :show-close="true"
    :close-on-click-modal="true"
    :close-on-press-escape="true"
  >
    <template #header>
      <div class="dialog-header">
        <h3 class="dialog-title">个人资料</h3>
      </div>
    </template>

    <div class="profile-container">
      <!-- 左侧：头像和基本信息 -->
      <div class="profile-sidebar">
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <DingtalkAvatar
              :src="currentUser.avatar"
              :name="currentUser.nickname || currentUser.username"
              :size="100"
              class="avatar"
            />
            <button
              class="avatar-edit-btn"
              @click="handleEdit"
            >
              <span class="material-icons-outlined">edit</span>
            </button>
          </div>
          <h3 class="user-name">{{ currentUser.nickname || currentUser.username }}</h3>
          <p class="user-id">ID: {{ currentUser.id || '-' }}</p>
          <div class="user-tags">
            <span
              v-if="isAdmin"
              class="tag admin-tag"
            >
              <span class="material-icons-outlined">admin_panel_settings</span>
              管理员
            </span>
            <span
              v-if="isUserOnline"
              class="tag online-tag"
            >
              <span class="material-icons-outlined">circle</span>
              在线
            </span>
          </div>
        </div>

        <div class="quick-actions">
          <button
            class="quick-btn"
            @click="handleEdit"
          >
            <span class="material-icons-outlined">edit</span>
            编辑资料
          </button>
          <button
            class="quick-btn"
            @click="showChangePassword = true"
          >
            <span class="material-icons-outlined">lock</span>
            修改密码
          </button>
          <button
            v-if="isAdmin"
            class="quick-btn admin-btn"
            @click="handleAdminPanel"
          >
            <span class="material-icons-outlined">admin_panel_settings</span>
            后台管理
          </button>
        </div>
      </div>

      <!-- 右侧：详细信息 -->
      <div class="profile-main">
        <div class="info-section">
          <h4 class="section-title">
            <span class="material-icons-outlined">person</span>
            基本信息
          </h4>
          <div class="info-grid">
            <div class="info-item">
              <label>用户名</label>
              <div class="info-value">{{ currentUser.username || '-' }}</div>
            </div>
            <div class="info-item">
              <label>昵称</label>
              <div class="info-value">{{ currentUser.nickname || '-' }}</div>
            </div>
            <div class="info-item">
              <label>性别</label>
              <div class="info-value">
                <span class="gender-icon">
                  <span class="material-icons-outlined">{{ genderIcon }}</span>
                </span>
                {{ genderText }}
              </div>
            </div>
            <div class="info-item">
              <label>生日</label>
              <div class="info-value">{{ currentUser.birthday || '-' }}</div>
            </div>
          </div>
        </div>

        <div class="info-section">
          <h4 class="section-title">
            <span class="material-icons-outlined">business</span>
            工作信息
          </h4>
          <div class="info-grid">
            <div class="info-item">
              <label>部门</label>
              <div class="info-value">{{ currentUser.dept?.deptName || currentUser.department || '-' }}</div>
            </div>
            <div class="info-item">
              <label>职位</label>
              <div class="info-value">{{ currentUser.position || '-' }}</div>
            </div>
          </div>
        </div>

        <div class="info-section">
          <h4 class="section-title">
            <span class="material-icons-outlined">contact_phone</span>
            联系方式
          </h4>
          <div class="info-grid">
            <div
              v-if="currentUser.mobile"
              class="info-item"
            >
              <label>手机</label>
              <div class="info-value">
                <span>{{ currentUser.mobile }}</span>
                <button
                  class="copy-btn"
                  @click="copyText(currentUser.mobile)"
                >
                  <span class="material-icons-outlined">content_copy</span>
                </button>
              </div>
            </div>
            <div
              v-if="currentUser.email"
              class="info-item"
            >
              <label>邮箱</label>
              <div class="info-value">
                <span class="email-text">{{ currentUser.email }}</span>
                <button
                  class="copy-btn"
                  @click="copyText(currentUser.email)"
                >
                  <span class="material-icons-outlined">content_copy</span>
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="info-section">
          <h4 class="section-title">
            <span class="material-icons-outlined">schedule</span>
            账户信息
          </h4>
          <div class="info-grid">
            <div class="info-item">
              <label>注册时间</label>
              <div class="info-value">{{ formatDate(currentUser.createTime) }}</div>
            </div>
            <div class="info-item">
              <label>最后登录</label>
              <div class="info-value">{{ formatDate(currentUser.loginDate) }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <button
          class="footer-btn logout-btn"
          @click="handleLogout"
        >
          <span class="material-icons-outlined">logout</span>
          退出登录
        </button>
        <button
          class="footer-btn close-btn"
          @click="handleClose"
        >
          关闭
        </button>
      </div>
    </template>

    <!-- 编辑资料弹窗 -->
    <EditProfileDialog
      v-model="showEditDialog"
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
import { ElMessage, ElMessageBox } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import EditProfileDialog from './EditProfileDialog.vue'
import ChangePasswordDialog from './ChangePasswordDialog.vue'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue'])

const store = useStore()
const router = useRouter()

const visible = ref(false)
const showEditDialog = ref(false)
const showChangePassword = ref(false)

const currentUser = computed(() => store.getters['user/currentUser'] || {})

const isAdmin = computed(() => {
  const username = currentUser.value?.username
  const roles = currentUser.value?.roles || []
  return username === 'admin' || roles.some(role => role === 'admin' || role === 'ROLE_ADMIN')
})

const isUserOnline = computed(() => {
  if (!currentUser.value?.id) {return false}
  return store.state.im?.userStatus?.[currentUser.value.id] === 'online'
})

const genderText = computed(() => {
  const gender = currentUser.value?.gender
  if (gender === 1) {return '男'}
  if (gender === 2) {return '女'}
  return '保密'
})

const genderIcon = computed(() => {
  const gender = currentUser.value?.gender
  if (gender === 1) {return 'male'}
  if (gender === 2) {return 'female'}
  return 'help'
})

const copyText = text => {
  if (!text) {return}
  navigator.clipboard.writeText(String(text)).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

const formatDate = date => {
  if (!date) {return '-'}
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleClose = () => emit('update:modelValue', false)

const handleEdit = () => {
  showEditDialog.value = true
}

const handleSaveProfile = async formData => {
  try {
    await store.dispatch('user/updateProfile', formData)
    ElMessage.success('资料已更新')
    showEditDialog.value = false
  } catch (error) {
    ElMessage.error(error.message || '更新失败')
  }
}

const handleAdminPanel = () => {
  const adminUrl = window.location.origin + '/admin'
  window.open(adminUrl, '_blank')
  ElMessage.success('正在打开后台管理...')
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '退出登录', {
    confirmButtonText: '退出',
    cancelButtonText: '取消',
    type: 'warning',
    center: true,
    appendTo: document.body
  }).then(() => {
    store.dispatch('user/logout').then(() => {
      ElMessage.success('已退出登录')
      handleClose()
      router.push('/login')
    }).catch(err => {
      console.error('退出登录失败:', err)
      ElMessage.error('退出失败')
    })
  }).catch(() => {
  })
}

watch(() => props.modelValue, val => visible.value = val)
watch(visible, val => {
  if (!val) {emit('update:modelValue', false)}
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

:deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.15);
  background: #ffffff;
}

:deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  margin: 0;
}

:deep(.el-dialog__body) {
  padding: 0;
  background: #ffffff;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #e5e7eb;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

:deep(.el-dialog__headerbtn) {
  top: 20px;
  right: 24px;
  width: 32px;
  height: 32px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(8px);

  &:hover {
    background: rgba(99, 102, 241, 0.1);
    transform: rotate(90deg);
  }

  .el-dialog__close {
    color: #6b7280;
    font-size: 20px;
    font-weight: 600;
  }
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.dialog-title {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
  margin: 0;
  letter-spacing: -0.5px;
}

.profile-container {
  display: flex;
  min-height: 480px;
}

.profile-sidebar {
  width: 240px;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  border-right: 1px solid #e5e7eb;
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  margin-bottom: 24px;
}

.avatar-wrapper {
  position: relative;
  margin-bottom: 16px;
}

.avatar {
  border-radius: 50%;
  border: 4px solid #ffffff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    transform: scale(1.05);
  }
}

.avatar-edit-btn {
  position: absolute;
  bottom: 6px;
  right: 6px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: #ffffff;
  border: 3px solid #ffffff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4);

  &:hover {
    background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
    transform: scale(1.15);
    box-shadow: 0 6px 20px rgba(99, 102, 241, 0.5);
  }

  .material-icons-outlined {
    font-size: 16px;
  }
}

.user-name {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 4px 0;
  letter-spacing: -0.5px;
}

.user-id {
  font-size: 12px;
  color: #6b7280;
  margin: 0 0 12px 0;
  font-weight: 500;
}

.user-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 16px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.3px;

  .material-icons-outlined {
    font-size: 12px;
  }

  &.admin-tag {
    background: linear-gradient(135deg, #ede9fe 0%, #ddd6fe 100%);
    color: #7c3aed;
    box-shadow: 0 2px 8px rgba(124, 58, 237, 0.15);
  }

  &.online-tag {
    background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
    color: #16a34a;
    box-shadow: 0 2px 8px rgba(22, 163, 74, 0.15);

    .material-icons-outlined {
      font-size: 8px;
    }
  }
}

.quick-actions {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.quick-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 12px;
  background: #ffffff;
  border: 1.5px solid #e5e7eb;
  border-radius: 8px;
  color: #374151;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

  &:hover {
    background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
    border-color: #6366f1;
    color: #6366f1;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(99, 102, 241, 0.2);
  }

  &:active {
    transform: translateY(0);
  }

  .material-icons-outlined {
    font-size: 18px;
  }

  &.admin-btn {
    color: #7c3aed;

    &:hover {
      border-color: #7c3aed;
      color: #7c3aed;
      box-shadow: 0 4px 12px rgba(124, 58, 237, 0.2);
    }
  }
}

.profile-main {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  max-height: 480px;

  &::-webkit-scrollbar {
    width: 5px;
  }

  &::-webkit-scrollbar-track {
    background: #f1f5f9;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb {
    background: #cbd5e1;
    border-radius: 3px;

    &:hover {
      background: #94a3b8;
    }
  }
}

.info-section {
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 700;
  color: #374151;
  margin: 0 0 12px 0;
  padding-bottom: 10px;
  border-bottom: 2px solid #e5e7eb;
  letter-spacing: -0.3px;

  .material-icons-outlined {
    font-size: 18px;
    color: #6366f1;
  }
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-item label {
  font-size: 11px;
  font-weight: 600;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #111827;
  padding: 10px 12px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 8px;
  border: 1.5px solid #e5e7eb;
  font-weight: 500;
  transition: all 0.2s;

  &:hover {
    border-color: #cbd5e1;
    background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
  }

  .email-text {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 1;
  }
}

.gender-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
  border-radius: 50%;
  color: #6366f1;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.15);

  .material-icons-outlined {
    font-size: 14px;
  }
}

.copy-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: transparent;
  border: none;
  border-radius: 6px;
  color: #9ca3af;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;

  &:hover {
    background: linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(139, 92, 246, 0.1) 100%);
    color: #6366f1;
    transform: scale(1.1);
  }

  .material-icons-outlined {
    font-size: 16px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.footer-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1.5px solid #e5e7eb;
  background: #ffffff;
  color: #374151;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

  &:hover {
    background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
    border-color: #cbd5e1;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }

  &:active {
    transform: translateY(0);
  }

  .material-icons-outlined {
    font-size: 18px;
  }

  &.close-btn {
    min-width: 80px;
  }

  &.logout-btn {
    color: #dc2626;
    background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
    border-color: #fecaca;

    &:hover {
      background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
      border-color: #dc2626;
      color: #b91c1c;
      box-shadow: 0 4px 12px rgba(220, 38, 38, 0.2);
    }
  }
}

.dark {
  :deep(.el-dialog) {
    background: #1f2937;
  }

  :deep(.el-dialog__header),
  :deep(.el-dialog__footer) {
    background: linear-gradient(135deg, #1f2937 0%, #111827 100%);
    border-color: #374151;
  }

  :deep(.el-dialog__headerbtn) {
    background: rgba(255, 255, 255, 0.1);

    &:hover {
      background: rgba(99, 102, 241, 0.2);
    }

    .el-dialog__close {
      color: #9ca3af;
    }
  }

  .dialog-title {
    color: #f9fafb;
  }

  .profile-sidebar {
    background: linear-gradient(180deg, #1f2937 0%, #111827 100%);
    border-right-color: #374151;
  }

  .user-name {
    color: #f9fafb;
  }

  .user-id {
    color: #9ca3af;
  }

  .quick-btn {
    background: #374151;
    border-color: #4b5563;
    color: #d1d5db;

    &:hover {
      background: linear-gradient(135deg, #374151 0%, #4b5563 100%);
      border-color: #6366f1;
      color: #6366f1;
    }
  }

  .profile-main {
    &::-webkit-scrollbar-track {
      background: #374151;
    }

    &::-webkit-scrollbar-thumb {
      background: #4b5563;

      &:hover {
        background: #6b7280;
      }
    }
  }

  .section-title {
    color: #d1d5db;
    border-bottom-color: #374151;
  }

  .info-item label {
    color: #9ca3af;
  }

  .info-value {
    background: linear-gradient(135deg, #374151 0%, #4b5563 100%);
    border-color: #4b5563;
    color: #f9fafb;

    &:hover {
      border-color: #6b7280;
      background: linear-gradient(135deg, #4b5563 0%, #6b7280 100%);
    }
  }

  .footer-btn {
    background: #374151;
    border-color: #4b5563;
    color: #d1d5db;

    &:hover {
      background: linear-gradient(135deg, #374151 0%, #4b5563 100%);
      border-color: #6b7280;
    }

    &.logout-btn {
      background: linear-gradient(135deg, #7f1d1d 0%, #991b1b 100%);
      border-color: #b91c1c;
      color: #fecaca;

      &:hover {
        background: linear-gradient(135deg, #991b1b 0%, #b91c1c 100%);
        border-color: #dc2626;
        color: #fee2e2;
      }
    }
  }
}
</style>
