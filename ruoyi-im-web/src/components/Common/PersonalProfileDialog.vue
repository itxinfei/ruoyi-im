<template>
  <el-dialog
    v-model="visible"
    width="960px"
    class="personal-profile-dialog"
    destroy-on-close
    append-to-body
    :show-close="false"
    :close-on-click-modal="true"
  >
    <div class="profile-layout">
      <!-- 侧边导航栏 -->
      <aside class="profile-sidebar">
        <div class="sidebar-header">
          <span class="material-icons-outlined header-icon">person</span>
          <span class="header-title">个人资料</span>
        </div>

        <nav class="sidebar-nav scrollbar-custom">
          <div
            v-for="item in menuItems"
            :key="item.id"
            class="nav-item"
            :class="{ active: activeMenu === item.id }"
            @click="activeMenu = item.id"
          >
            <span class="material-icons-outlined nav-icon">{{ item.icon }}</span>
            <span class="nav-label">{{ item.label }}</span>
          </div>
        </nav>

        <div class="sidebar-footer">
          <div class="user-info">
            <el-avatar :size="32" :src="currentUser.avatar" class="user-avatar" />
            <span class="user-name">{{ currentUser.nickname || currentUser.username }}</span>
          </div>
        </div>
      </aside>

      <!-- 主内容区域 -->
      <main class="profile-main">
        <header class="main-header">
          <div class="header-content">
            <h2 class="header-title">{{ currentMenuLabel }}</h2>
            <p class="header-subtitle">{{ currentUser.nickname || currentUser.username }}</p>
          </div>
          <div class="header-actions">
            <el-button
              v-if="showEditButton"
              type="primary"
              @click="handleEdit"
            >
              <span class="material-icons-outlined" style="font-size: 16px; margin-right: 4px;">edit</span>
              编辑
            </el-button>
            <el-button circle text class="close-btn" @click="handleClose">
              <span class="material-icons-outlined">close</span>
            </el-button>
          </div>
        </header>

        <div class="main-content scrollbar-custom">
          <component :is="currentComponent" :user="currentUser" @open-password="showChangePassword = true" />
        </div>
      </main>
    </div>

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
import { ref, watch, computed, defineAsyncComponent } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'

// 动态导入子组件
const ProfileOverview = defineAsyncComponent(() => import('../Profile/Overview.vue'))
const ProfileBasicInfo = defineAsyncComponent(() => import('../Profile/BasicInfo.vue'))
const ProfileContactInfo = defineAsyncComponent(() => import('../Profile/ContactInfo.vue'))
const ProfileSecurity = defineAsyncComponent(() => import('../Profile/Security.vue'))

import EditProfileDialog from './EditProfileDialog.vue'
import ChangePasswordDialog from './ChangePasswordDialog.vue'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue'])

const store = useStore()

const visible = ref(false)
const activeMenu = ref('overview')
const showEditDialog = ref(false)
const showChangePassword = ref(false)

// 菜单配置
const menuItems = [
  { id: 'overview', label: '个人资料', icon: 'person' },
  { id: 'basic', label: '基本信息', icon: 'badge' },
  { id: 'contact', label: '联系方式', icon: 'contact_phone' },
  { id: 'security', label: '账号安全', icon: 'security' }
]

const currentMenuLabel = computed(() => menuItems.find(i => i.id === activeMenu.value)?.label || '个人资料')

// 数据源
const currentUser = computed(() => store.getters['user/currentUser'] || {})

// 组件映射
const componentMap = {
  overview: ProfileOverview,
  basic: ProfileBasicInfo,
  contact: ProfileContactInfo,
  security: ProfileSecurity
}

const currentComponent = computed(() => componentMap[activeMenu.value])

// 是否显示编辑按钮
const showEditButton = computed(() => {
  return ['overview', 'basic'].includes(activeMenu.value)
})

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

// 监听
watch(() => props.modelValue, (val) => visible.value = val)
watch(visible, (val) => {
  if (!val) emit('update:modelValue', false)
  if (val) activeMenu.value = 'overview' // 打开时重置为首页
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// 对话框样式
:deep(.el-dialog) {
  border-radius: var(--dt-radius-xl);
  overflow: hidden;
  box-shadow: var(--dt-shadow-dialog);
  margin-top: 5vh !important;
  background: var(--dt-bg-body);
}

:deep(.el-dialog__header) {
  display: none;
}

:deep(.el-dialog__body) {
  padding: 0;
  height: 720px;
  background: var(--dt-bg-body);
}

.profile-layout {
  display: flex;
  height: 100%;
  width: 100%;
  background: var(--dt-bg-body);
}

// 侧边栏样式
.profile-sidebar {
  width: 240px;
  background: var(--dt-bg-card);
  border-right: 1px solid var(--dt-border-color);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid var(--dt-border-color);
  background: var(--dt-bg-card);
  gap: 12px;

  .header-icon {
    font-size: 24px;
    color: var(--dt-brand-color);
  }

  .header-title {
    font-size: 18px;
    font-weight: var(--dt-font-weight-semibold);
    color: var(--dt-text-primary);
  }
}

.sidebar-nav {
  flex: 1;
  padding: 12px 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  height: 44px;
  padding: 0 16px;
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  color: var(--dt-text-secondary);
  transition: all 0.2s ease;
  user-select: none;
  font-size: 14px;
  gap: 12px;

  &:hover {
    background: var(--dt-bg-hover);
    color: var(--dt-text-primary);
  }

  &.active {
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);
    font-weight: var(--dt-font-weight-medium);
  }
}

.nav-icon {
  font-size: 20px;
}

.nav-label {
  font-size: 14px;
}

.sidebar-footer {
  padding: 12px;
  border-top: 1px solid var(--dt-border-color);
  background: var(--dt-bg-card);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: var(--dt-radius-md);
  transition: background 0.2s ease;

  &:hover {
    background: var(--dt-bg-hover);
  }

  .user-avatar {
    border: 2px solid var(--dt-border-light);
  }

  .user-name {
    font-size: 14px;
    font-weight: var(--dt-font-weight-medium);
    color: var(--dt-text-primary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

// 主内容区
.profile-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: var(--dt-bg-body);
}

.main-header {
  height: 64px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--dt-border-color);
  background: var(--dt-bg-body);
  flex-shrink: 0;
}

.header-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.header-title {
  font-size: 18px;
  font-weight: var(--dt-font-weight-semibold);
  color: var(--dt-text-primary);
  margin: 0;
}

.header-subtitle {
  font-size: 13px;
  color: var(--dt-text-secondary);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.close-btn {
  font-size: 20px;
  color: var(--dt-text-secondary);

  &:hover {
    color: var(--dt-text-primary);
    background: var(--dt-bg-hover);
  }
}

.main-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

// 滚动条样式
.scrollbar-custom {
  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background-color: var(--dt-border-color);
    border-radius: var(--dt-radius-sm);

    &:hover {
      background-color: var(--dt-text-quaternary);
    }
  }
}

// 暗黑模式适配
.dark {
  .profile-sidebar {
    background: var(--dt-bg-card-dark);
    border-right-color: var(--dt-border-dark);
  }

  .sidebar-header,
  .sidebar-footer {
    border-color: var(--dt-border-dark);
  }

  .nav-item.active {
    background: var(--dt-brand-bg-dark);
  }

  .main-header {
    border-bottom-color: var(--dt-border-dark);
  }

  .user-info:hover {
    background: var(--dt-bg-hover-dark);
  }
}
</style>
