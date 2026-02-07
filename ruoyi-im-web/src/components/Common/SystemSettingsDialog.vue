<template>
  <el-dialog
    v-model="visible"
    width="900px"
    class="system-settings-dialog"
    destroy-on-close
    append-to-body
    :fullscreen="isFullscreen"
    :show-close="false"
    :close-on-click-modal="true"
  >
    <div class="settings-layout">
      <!-- 侧边导航栏 -->
      <aside class="settings-sidebar">
        <div class="sidebar-header">
          <span class="material-icons-outlined header-icon">settings</span>
          <span class="header-title">设置</span>
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
          <div
            class="user-info"
            @click="activeMenu = 'account'"
          >
            <el-avatar
              :size="32"
              :src="currentUser.avatar"
              class="user-avatar"
            />
            <span class="user-name">{{ currentUser.nickname || currentUser.username }}</span>
          </div>
        </div>
      </aside>

      <!-- 主内容区域 -->
      <main class="settings-main">
        <header class="main-header">
          <div class="header-content">
            <h2 class="header-title">
              {{ currentMenuLabel }}
            </h2>
            <p class="header-subtitle">
              {{ currentUser.name || currentUser.username }}
            </p>
          </div>
          <div class="header-actions">
            <el-button
              text
              @click="resetToDefault"
            >
              <span
                class="material-icons-outlined"
                style="font-size: 16px; margin-right: 4px;"
              >restart_alt</span>
              恢复默认
            </el-button>
            <el-button
              type="primary"
              @click="onSave"
            >
              <span
                class="material-icons-outlined"
                style="font-size: 16px; margin-right: 4px;"
              >save</span>
              保存
            </el-button>
            <el-button
              circle
              text
              class="close-btn"
              @click="visible = false"
            >
              <span class="material-icons-outlined">close</span>
            </el-button>
          </div>
        </header>

        <div class="main-content scrollbar-custom">
          <component
            :is="currentComponent"
            v-bind="componentProps"
            v-on="finalComponentEvents"
          />
        </div>
      </main>
    </div>

    <!-- 全局弹窗 -->
    <ChangePasswordDialog v-model="showChangePassword" />
    <EditProfileDialog
      v-model="showEditProfile"
      @success="handleProfileUpdate"
    />
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed, onMounted, defineAsyncComponent } from 'vue'
import { useStore } from 'vuex'
import { useTheme } from '@/composables/useTheme'
import { ElMessage, ElMessageBox } from 'element-plus'

// 动态导入子组件
const AccountSettings = defineAsyncComponent(() => import('../Settings/AccountSettings.vue'))
const NotificationSettings = defineAsyncComponent(() => import('../Settings/NotificationSettings.vue'))
const GeneralSettings = defineAsyncComponent(() => import('../Settings/GeneralSettings.vue'))
const ThemeSettings = defineAsyncComponent(() => import('../Settings/ThemeSettings.vue'))
const StorageSettings = defineAsyncComponent(() => import('../Settings/StorageSettings.vue'))
const HelpSettings = defineAsyncComponent(() => import('../Settings/HelpSettings.vue'))
const AboutSettings = defineAsyncComponent(() => import('../Settings/AboutSettings.vue'))

import ChangePasswordDialog from '@/components/Common/ChangePasswordDialog.vue'
import EditProfileDialog from '@/components/Common/EditProfileDialog.vue'

const props = defineProps({
  modelValue: Boolean,
  defaultMenu: { type: String, default: 'account' }
})

const emit = defineEmits(['update:modelValue'])

const store = useStore()
const { setThemeMode, themeMode } = useTheme()

const visible = ref(false)
const activeMenu = ref('account')
const showChangePassword = ref(false)
const showEditProfile = ref(false)
const isFullscreen = ref(false)
const windowWidth = ref(window.innerWidth)

// 菜单配置 - 使用 Material Icons
const menuItems = [
  { id: 'account', label: '账号与安全', icon: 'person' },
  { id: 'notification', label: '消息通知', icon: 'notifications' },
  { id: 'general', label: '通用设置', icon: 'tune' },
  { id: 'theme', label: '主题外观', icon: 'palette' },
  { id: 'storage', label: '存储管理', icon: 'storage' },
  { id: 'help', label: '帮助反馈', icon: 'help_outline' },
  { id: 'about', label: '关于', icon: 'info' }
]

const currentMenuLabel = computed(() => menuItems.find(i => i.id === activeMenu.value)?.label || '设置')

// 数据源
const currentUser = computed(() => store.getters['user/currentUser'] || {})
const settings = computed(() => store.state.im.settings)

// 组件映射
const componentMap = {
  account: AccountSettings,
  notification: NotificationSettings,
  general: GeneralSettings,
  theme: ThemeSettings,
  storage: StorageSettings,
  help: HelpSettings,
  about: AboutSettings
}

const currentComponent = computed(() => componentMap[activeMenu.value])

// 组件 Props
const componentProps = computed(() => {
  switch (activeMenu.value) {
    case 'account':
      return { user: currentUser.value }
    case 'notification':
      return { modelValue: settings.value }
    case 'general':
      return { modelValue: settings.value }
    case 'storage':
      return { modelValue: settings.value, cacheSize: cacheSize.value }
    default:
      return {}
  }
})

// 组件事件
const componentEvents = computed(() => {
  const commonEvents = {
    'update:modelValue': handleSettingsUpdate,
    'change': saveSettings
  }

  switch (activeMenu.value) {
    case 'account':
      return {
        'edit-profile': () => showEditProfile.value = true,
        'change-password': () => showChangePassword.value = true
      }
    case 'storage':
      return {
        ...commonEvents,
        'clear-cache': handleClearCache,
        'export-chat': handleExportChat
      }
    default:
      return commonEvents
  }
})

// 缓存计算
const cacheSize = ref('0 MB')
const calculateCacheSize = () => {
  let total = 0
  for (const key in localStorage) {
    if (Object.prototype.hasOwnProperty.call(localStorage, key)) { total += localStorage[key].length + key.length }
  }
  cacheSize.value = `${(total / 1024 / 1024).toFixed(2)} MB`
}

// 业务逻辑处理
const handleProfileUpdate = () => store.dispatch('user/getInfo')

const handleSettingsUpdate = newSettings => {
  // 实时更新 store
}

const saveSettings = () => {
  try {
    store.commit('im/UPDATE_SETTINGS', store.state.im.settings)
    ElMessage.success('设置已保存')
  } catch (e) {
    console.error('保存设置失败', e)
    ElMessage.error('保存失败')
  }
}

// 更新设置映射
const updateSettingsMap = {
  notification: val => store.dispatch('im/updateNotificationSettings', val.notifications),
  general: val => store.dispatch('im/updateGeneralSettings', val.general),
  storage: val => store.dispatch('im/updateDataSettings', val.data)
}

const realHandleSettingsUpdate = newSettings => {
  if (updateSettingsMap[activeMenu.value]) {
    updateSettingsMap[activeMenu.value](newSettings)
  } else {
    store.commit('im/UPDATE_SETTINGS', newSettings)
  }
}

// 重新绑定事件
watch(activeMenu, () => {
  if (activeMenu.value === 'storage') { calculateCacheSize() }
})

const finalComponentEvents = computed(() => {
  const events = { ...componentEvents.value }
  if (events['update:modelValue']) {
    events['update:modelValue'] = realHandleSettingsUpdate
  }
  return events
})

// 头部操作
const resetToDefault = () => {
  ElMessageBox.confirm('恢复默认设置将移除本地自定义项，是否继续？', '恢复默认', {
    confirmButtonText: '恢复',
    cancelButtonText: '取消',
    type: 'warning',
    appendTo: document.body,
    center: true
  }).then(() => {
    try {
      localStorage.removeItem('im-system-settings')
      store.commit('im/LOAD_SETTINGS')
      ElMessage.success('已恢复默认设置')
    } catch (e) {
      console.error(e)
      ElMessage.error('恢复失败')
    }
  })
}

const onSave = () => {
  saveSettings()
  visible.value = false
}

// 缓存清理
const handleClearCache = () => {
  ElMessageBox.confirm('清理缓存将释放本地空间，但图片和文件需要重新下载。是否继续？', '清理缓存', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    appendTo: document.body,
    center: true
  }).then(() => {
    const keep = ['im-system-settings', 'token', 'user-info']
    Object.keys(localStorage).forEach(key => { if (!keep.includes(key)) { localStorage.removeItem(key) } })
    calculateCacheSize()
    ElMessage.success('缓存清理成功')
  })
}

// 聊天导出
const handleExportChat = () => {
  const dataStr = JSON.stringify(store.state.im.message?.messages || [], null, 2)
  const blob = new Blob([dataStr], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `im-backup-${new Date().getTime()}.json`
  a.click()
  ElMessage.success('备份已导出')
}

// 监听
watch(() => props.modelValue, val => {
  visible.value = val
  if (val && props.defaultMenu) { activeMenu.value = props.defaultMenu }
  if (val) { calculateCacheSize() }
})
watch(visible, val => { if (!val) { emit('update:modelValue', false) } })
watch(() => settings.value.general?.theme, val => {
  if (val && val !== themeMode.value) { setThemeMode(val) }
}, { immediate: true })

onMounted(() => {
  window.addEventListener('resize', () => {
    windowWidth.value = window.innerWidth
  })
})
</script>

<style scoped lang="scss">
// 对话框样式
:deep(.el-dialog) {
  border-radius: var(--dt-radius-xl);
  overflow: hidden;
  box-shadow: var(--dt-shadow-dialog);
  margin-top: 5vh !important;
  background: var(--dt-bg-body);
}

:deep(.el-dialog) {
  border: none;
  box-shadow: var(--dt-shadow-2xl), var(--dt-shadow-3);
}

:deep(.el-dialog__header) {
  display: none;
}

:deep(.el-dialog__body) {
  padding: 0;
  height: 650px;
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
}

.settings-layout {
  display: flex;
  height: 650px;
  width: 100%;
  background: #ffffff;
}

// 侧边栏样式
.settings-sidebar {
  width: 240px;
  background: #f9fafb;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  border-bottom: none;
  background: #f9fafb;
  gap: 10px;

  .header-icon {
    font-size: 22px;
    color: var(--dt-brand-color);
  }

  .header-title {
    font-size: 16px;
    font-weight: 600;
    color: #111827;
  }
}

.sidebar-nav {
  flex: 1;
  padding: 12px 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow-y: auto;
  overflow-x: hidden;

  // 自定义滚动条样式
  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background-color: #d1d5db;
    border-radius: 2px;

    &:hover {
      background-color: #9ca3af;
    }
  }
}

.nav-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  height: 44px;
  padding: 0 16px;
  border-radius: 8px;
  cursor: pointer;
  color: #6b7280;
  transition: all 0.2s ease;
  user-select: none;
  gap: 12px;

  &:hover {
    background: #f3f4f6;
    color: #374151;
  }

  &.active {
    background: var(--dt-brand-color);
    color: #ffffff;
    font-weight: 500;

    .nav-icon,
    .nav-label {
      color: #ffffff;
    }
  }
}

.nav-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.nav-label {
  font-size: 14px;
  font-weight: 500;
  flex: 1;
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
  cursor: pointer;
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
.settings-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: #ffffff;
}

.main-header {
  height: 56px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f3f4f6;
  background: #ffffff;
  flex-shrink: 0;
}

.header-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
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
  padding: 20px;
  overflow-y: auto;
  background: #fafbfc;
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
  .settings-sidebar {
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
