<template>
  <el-dialog v-model="visible" width="420px" class="personal-profile-dialog" destroy-on-close append-to-body
    :show-close="false" :close-on-click-modal="true">
    <div class="user-profile-container">
      <!-- 头部：头像 + 基本信息 -->
      <div class="header">
        <div class="header-bg"></div>
        <div class="avatar-wrapper">
          <DingtalkAvatar :src="currentUser.avatar" :name="currentUser.nickname || currentUser.username" :size="80"
            class="avatar" />
        </div>
        <div class="user-info">
          <h2 class="user-name">{{ currentUser.nickname || currentUser.username }}</h2>
          <label class="user-id">ID: {{ currentUser.id || '' }}</label>
        </div>
      </div>

      <!-- 内容：信息列表 -->
      <div class="content">
        <ul class="info-list">
          <li v-if="currentUser.mobile">
            <div class="info-item">
              <span class="material-icons-outlined icon">phone</span>
              <div class="info-content">
                <label>手机</label>
                <div class="value-wrapper">
                  <span>{{ currentUser.mobile }}</span>
                  <span class="icon-btn" @click="copyText(currentUser.mobile)">
                    <span class="material-icons-outlined">content_copy</span>
                  </span>
                </div>
              </div>
            </div>
          </li>
          <li v-if="currentUser.email">
            <div class="info-item">
              <span class="material-icons-outlined icon">email</span>
              <div class="info-content">
                <label>邮箱</label>
                <div class="value-wrapper">
                  <span class="email-text">{{ currentUser.email }}</span>
                  <span class="icon-btn" @click="copyText(currentUser.email)">
                    <span class="material-icons-outlined">content_copy</span>
                  </span>
                </div>
              </div>
            </div>
          </li>
          <li>
            <div class="info-item">
              <span class="material-icons-outlined icon">business</span>
              <div class="info-content">
                <label>部门</label>
                <div>{{ currentUser.dept?.deptName || currentUser.department || '-' }}</div>
              </div>
            </div>
          </li>
          <li>
            <div class="info-item">
              <span class="material-icons-outlined icon">badge</span>
              <div class="info-content">
                <label>职位</label>
                <div>{{ currentUser.position || '-' }}</div>
              </div>
            </div>
          </li>
          <li>
            <div class="info-item">
              <span class="material-icons-outlined icon">{{ genderIcon }}</span>
              <div class="info-content">
                <label>性别</label>
                <div>{{ genderText }}</div>
              </div>
            </div>
          </li>
          <li v-if="currentUser.birthday">
            <div class="info-item">
              <span class="material-icons-outlined icon">cake</span>
              <div class="info-content">
                <label>生日</label>
                <div>{{ currentUser.birthday }}</div>
              </div>
            </div>
          </li>
        </ul>
      </div>

      <!-- 底部操作按钮 -->
      <div class="footer">
        <a href="#" class="action-btn" @click.prevent="handleEdit">
          <span class="material-icons-outlined">edit</span>
          <span>编辑资料</span>
        </a>
        <a href="#" class="action-btn" @click.prevent="showChangePassword = true">
          <span class="material-icons-outlined">lock</span>
          <span>修改密码</span>
        </a>
        <a v-if="isAdmin" href="#" class="action-btn admin-btn" @click.prevent="handleAdminPanel">
          <span class="material-icons-outlined">admin_panel_settings</span>
          <span>后台管理</span>
        </a>
        <a href="#" class="action-btn logout-btn" @click.prevent="handleLogout">
          <span class="material-icons-outlined">logout</span>
          <span>退出登录</span>
        </a>
        <a href="#" class="action-btn close-btn" @click.prevent="handleClose">
          <span class="material-icons-outlined">close</span>
        </a>
      </div>
    </div>

    <!-- 编辑资料弹窗 -->
    <EditProfileDialog v-model="showEditDialog" @save="handleSaveProfile" />

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

// 数据源
const currentUser = computed(() => store.getters['user/currentUser'] || {})

// 判断是否为admin用户
const isAdmin = computed(() => {
  const username = currentUser.value?.username
  const roles = currentUser.value?.roles || []
  return username === 'admin' || roles.some(role => role === 'admin' || role === 'ROLE_ADMIN')
})

const genderText = computed(() => {
  const gender = currentUser.value?.gender
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '保密'
})

const genderIcon = computed(() => {
  const gender = currentUser.value?.gender
  if (gender === 1) return 'male'
  if (gender === 2) return 'female'
  return 'help'
})

// 复制文本
const copyText = (text) => {
  if (!text) return
  navigator.clipboard.writeText(String(text)).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

// 处理方法
const handleClose = () => emit('update:modelValue', false)

const handleEdit = () => {
  showEditDialog.value = true
}

const handleSaveProfile = async (formData) => {
  try {
    await store.dispatch('user/updateProfile', formData)
    ElMessage.success('资料已更新')
    showEditDialog.value = false
  } catch (error) {
    ElMessage.error(error.message || '更新失败')
  }
}

const handleAdminPanel = () => {
  // 打开后台管理页面(新窗口)
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
    // 取消退出
  })
}

// 监听
watch(() => props.modelValue, (val) => visible.value = val)
watch(visible, (val) => {
  if (!val) emit('update:modelValue', false)
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// 对话框样式
:deep(.el-dialog) {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15);
  background: #fcfcfc;
}

:deep(.el-dialog__header) {
  display: none;
}

:deep(.el-dialog__body) {
  padding: 0;
  background: #fcfcfc;
}

.user-profile-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  background: #fcfcfc;
  color: #292a2c;
}

// 头部
.header {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px 20px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  opacity: 0.9;
}

.avatar-wrapper {
  position: relative;
  z-index: 1;
  margin-bottom: 12px;

  .avatar {
    border-radius: 50%;
    border: 4px solid rgba(255, 255, 255, 0.3);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

.user-info {
  position: relative;
  z-index: 1;
  text-align: center;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
  margin: 0 0 6px 0;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.user-id {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
  background: rgba(255, 255, 255, 0.15);
  padding: 4px 12px;
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

// 内容区
.content {
  width: 100%;
  text-align: left;
  padding: 0;
}

.info-list {
  list-style: none;
  margin: 0;
  padding: 16px;
}

.info-list li {
  margin-bottom: 2px;

  &:last-child {
    margin-bottom: 0;
  }
}

.info-item {
  display: flex;
  align-items: flex-start;
  padding: 14px 16px;
  background: #fafbfc;
  border-radius: 8px;
  transition: all 0.2s;
  gap: 12px;

  &:hover {
    background: #f3f4f6;
  }

  .icon {
    font-size: 20px;
    color: #667eea;
    margin-top: 2px;
    flex-shrink: 0;
  }

  .info-content {
    flex: 1;
    min-width: 0;

    label {
      display: block;
      font-size: 12px;
      color: #6b7280;
      margin-bottom: 4px;
      font-weight: 500;
    }

    >div {
      font-size: 14px;
      color: #111827;
      word-break: break-all;
    }
  }
}

.value-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;

  .email-text {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 1;
  }
}

.icon-btn {
  cursor: pointer;
  color: #9ca3af;
  transition: all 0.2s;
  padding: 4px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover {
    color: #667eea;
    background: rgba(102, 126, 234, 0.1);
  }

  .material-icons-outlined {
    font-size: 16px;
  }
}

// 底部操作区
.footer {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  border-top: 1px solid #e6e6e6;
  gap: 12px;
  background: #ffffff;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  text-decoration: none;
  color: #5d7ce8;
  font-size: 13px;
  font-weight: 500;
  padding: 10px 16px;
  border-radius: 6px;
  transition: all 0.2s;
  min-width: 90px;
  background: #f5f7fa;
  border: 1px solid transparent;

  &:hover {
    background: #e8edf5;
    border-color: #5d7ce8;
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(93, 124, 232, 0.15);
  }

  .material-icons-outlined {
    font-size: 18px;
  }

  &.close-btn {
    color: #7f7f7f;
    background: transparent;
    min-width: 40px;
    padding: 10px;

    &:hover {
      background: #f3f4f6;
      border-color: #d1d5db;
      color: #292a2c;
    }

    span:last-child {
      display: none;
    }
  }

  &.logout-btn {
    color: #dc2626;
    background: #fef2f2;
    border-color: #fecaca;

    &:hover {
      background: #fee2e2;
      border-color: #dc2626;
      color: #b91c1c;
      box-shadow: 0 2px 8px rgba(220, 38, 38, 0.15);
    }
  }

  &.admin-btn {
    color: #7c3aed;
    background: #f5f3ff;
    border-color: #ddd6fe;

    &:hover {
      background: #ede9fe;
      border-color: #7c3aed;
      color: #6d28d9;
      box-shadow: 0 2px 8px rgba(124, 58, 237, 0.15);
    }
  }
}

// 暗黑模式适配
.dark {

  .user-profile-container,
  :deep(.el-dialog) {
    background: #1a1a1a;
  }

  .header {
    border-bottom-color: #333;
  }

  .user-name {
    color: #e5e5e5;
  }

  .user-id,
  .info-list li label {
    color: #999;
  }

  .info-list li>div {
    color: #e5e5e5;
  }

  .footer {
    border-top-color: #333;
  }

  .action-btn {
    &.close-btn {
      color: #999;

      &:hover {
        background: rgba(255, 255, 255, 0.05);
        color: #e5e5e5;
      }
    }
  }
}
</style>
