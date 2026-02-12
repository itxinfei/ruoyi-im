<template>
  <el-dialog
    v-model="visible"
    width="920px"
    class="personal-profile-dialog"
    destroy-on-close
    append-to-body
    :show-close="true"
    :close-on-click-modal="true"
    :close-on-press-escape="true"
  >
    <template #header>
      <div class="dialog-header">
        <h3 class="dialog-title">
          个人资料
        </h3>
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
          <h3 class="user-name">
            {{ currentUser.nickname || currentUser.username }}
          </h3>
          <p class="user-id">
            ID: {{ currentUser.id || '-' }}
          </p>
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
              <div class="info-value">
                {{ currentUser.username || '-' }}
              </div>
            </div>
            <div class="info-item">
              <label>昵称</label>
              <div class="info-value">
                {{ currentUser.nickname || '-' }}
              </div>
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
              <div class="info-value">
                {{ currentUser.birthday || '-' }}
              </div>
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
              <div class="info-value">
                {{ currentUser.dept?.deptName || currentUser.department || '-' }}
              </div>
            </div>
            <div class="info-item">
              <label>职位</label>
              <div class="info-value">
                {{ currentUser.position || '-' }}
              </div>
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
              <div class="info-value">
                {{ formatDate(currentUser.createTime) }}
              </div>
            </div>
            <div class="info-item">
              <label>最后登录</label>
              <div class="info-value">
                {{ formatDate(currentUser.loginDate) }}
              </div>
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
  if (!currentUser.value?.id) { return false }
  // 综合判断：联系人状态缓存 || 本端 WebSocket 连接状态
  const isOnlineInContact = store.state.im?.contact?.userStatus?.[currentUser.value.id] === 'online'
  return isOnlineInContact || (store.state.im?.wsConnected || false)
})

const genderText = computed(() => {
  const gender = currentUser.value?.gender
  if (gender === 1) { return '男' }
  if (gender === 2) { return '女' }
  return '保密'
})

const genderIcon = computed(() => {
  const gender = currentUser.value?.gender
  if (gender === 1) { return 'male' }
  if (gender === 2) { return 'female' }
  return 'help'
})

const copyText = text => {
  if (!text) { return }
  navigator.clipboard.writeText(String(text)).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

const formatDate = date => {
  if (!date) { return '-' }
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
  if (!val) { emit('update:modelValue', false) }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

:deep(.el-dialog) {
  border-radius: var(--dt-radius-xl);
  overflow: hidden;
  box-shadow: var(--dt-shadow-3xl);
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
}

:deep(.el-dialog__header) {
  padding: 16px 24px;
  border-bottom: 1px solid var(--dt-border-light);
  background: var(--dt-bg-subtle);
  margin: 0;
}

:deep(.el-dialog__body) {
  padding: 0;
  background: var(--dt-bg-card);
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid var(--dt-border-light);
  background: var(--dt-bg-subtle);
}

:deep(.el-dialog__headerbtn) {
  top: 14px;
  right: 20px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--dt-radius-md);
  transition: all 0.2s;

  &:hover {
    background: var(--dt-bg-subtle-hover);
    transform: rotate(90deg);
  }

  .el-dialog__close {
    font-size: 18px;
    color: var(--dt-text-secondary);
  }
}

.dialog-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
}

.profile-container {
  display: grid;
  grid-template-columns: 260px 1fr;
  min-height: 520px;
}

.profile-sidebar {
  background: var(--dt-bg-subtle);
  border-right: 1px solid var(--dt-border-light);
  padding: 32px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-section {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  margin-bottom: 32px;
}

.avatar-wrapper {
  position: relative;
  margin-bottom: 20px;
  padding: 4px;
  background: #fff;
  border-radius: 50%;
  box-shadow: var(--dt-shadow-md);
}

.avatar {
  border: 2px solid transparent;
}

.avatar-edit-btn {
  position: absolute;
  bottom: 0px;
  right: 4px;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--dt-brand-color);
  color: #fff;
  border: 2px solid #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  box-shadow: var(--dt-shadow-sm);

  &:hover {
    background: var(--dt-brand-dark);
    transform: scale(1.1);
  }

  .material-icons-outlined {
    font-size: 16px;
  }
}

.user-name {
  font-size: 20px;
  font-weight: 700;
  color: var(--dt-text-primary);
  margin: 0 0 6px 0;
}

.user-id {
  font-size: 12px;
  color: var(--dt-text-quaternary);
  margin: 0 0 16px 0;
  font-family: var(--dt-font-mono);
}

.user-tags {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;

  &.admin-tag {
    background: rgba(var(--dt-brand-rgb), 0.1);
    color: var(--dt-brand-color);
  }

  &.online-tag {
    background: rgba(var(--dt-success-rgb), 0.1);
    color: var(--dt-success-color);

    .material-icons-outlined {
      font-size: 8px;
    }
  }

  .material-icons-outlined {
    font-size: 14px;
  }
}

.quick-actions {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: auto;
}

.quick-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 10px 14px;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  color: var(--dt-text-secondary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--dt-bg-subtle-hover);
    border-color: var(--dt-brand-lighter);
    color: var(--dt-brand-color);
    transform: translateX(4px);
  }

  .material-icons-outlined {
    font-size: 18px;
  }

  &.admin-btn {
    &:hover {
      border-color: var(--dt-warning-color);
      color: var(--dt-warning-color);
    }
  }
}

.profile-main {
  flex: 1;
  padding: 24px 32px;
  overflow-y: auto;
}

.info-section {
  margin-bottom: 28px;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--dt-border-lighter);

  .material-icons-outlined {
    font-size: 18px;
    color: var(--dt-brand-color);
  }
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;

  label {
    font-size: 12px;
    font-weight: 500;
    color: var(--dt-text-quaternary);
  }
}

.info-value {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--dt-text-primary);
  padding: 8px 12px;
  background: var(--dt-bg-subtle);
  border-radius: var(--dt-radius-md);
  border: 1px solid transparent;
  min-height: 40px;
  transition: all 0.2s;

  &:hover {
    background: var(--dt-bg-subtle-hover);
    border-color: var(--dt-border-light);
  }

  .email-text {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.gender-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);

  .material-icons-outlined {
    font-size: 14px;
  }
}

.copy-btn {
  margin-left: auto;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  border: none;
  background: transparent;
  color: var(--dt-text-quaternary);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--dt-bg-subtle-hover);
    color: var(--dt-brand-color);
  }

  .material-icons-outlined {
    font-size: 14px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 20px;
  border-radius: var(--dt-radius-md);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid var(--dt-border-light);
  background: var(--dt-bg-card);
  color: var(--dt-text-secondary);

  &:hover {
    background: var(--dt-bg-subtle-hover);
    border-color: var(--dt-border-dark);
  }

  .material-icons-outlined {
    font-size: 18px;
  }

  &.logout-btn {
    border-color: transparent;
    color: var(--dt-error-color);
    background: rgba(var(--dt-error-rgb), 0.05);

    &:hover {
      background: rgba(var(--dt-error-rgb), 0.1);
      color: var(--dt-error-dark);
    }
  }

  &.close-btn {
    background: var(--dt-brand-color);
    color: #fff;
    border: none;

    &:hover {
      background: var(--dt-brand-dark);
    }
  }
}

// 响应式
@media (max-width: 920px) {
  .profile-container {
    grid-template-columns: 1fr;
  }

  .profile-sidebar {
    flex-direction: row;
    border-right: none;
    border-bottom: 1px solid var(--dt-border-light);
    padding: 24px;
    align-items: center;
  }

  .avatar-section {
    margin-bottom: 0;
    text-align: left;
    align-items: flex-start;
    width: auto;
  }

  .avatar-wrapper {
    margin-bottom: 0;
    margin-right: 20px;
  }

  .quick-actions {
    margin-left: auto;
    width: auto;
    flex-direction: row;
    margin-top: 0;
  }

  .quick-btn {
    width: auto;

    span:last-child {
      display: none;
    }
  }

  .profile-main {
    max-height: 400px;
  }
}

@media (max-width: 640px) {
  .info-grid {
    grid-template-columns: 1fr;
  }

  .profile-sidebar {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }

  .quick-actions {
    margin-left: 0;
    width: 100%;
  }

  .quick-btn {
    flex: 1;

    span:last-child {
      display: inline;
    }
  }
}

// 暗色模式支持
.dark {
  .profile-sidebar {
    background: var(--dt-bg-subtle-dark);
  }

  .avatar-wrapper {
    background: var(--dt-bg-card-dark);
  }

  .info-value {
    background: var(--dt-bg-subtle-dark);
  }

  .footer-btn {
    background: var(--dt-bg-card-dark);
    color: var(--dt-text-secondary-dark);
    border-color: var(--dt-border-dark);
  }
}
</style>
```
