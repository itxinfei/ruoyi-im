<template>
  <el-dialog
    v-model="visible"
    :width="dialogWidth"
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
          <div class="app-brand">
            <span class="brand-icon">S</span>
          </div>
        </div>
        
        <nav class="sidebar-nav">
          <div
            v-for="item in menuItems"
            :key="item.id"
            class="nav-item"
            :class="{ active: activeMenu === item.id }"
            @click="activeMenu = item.id"
            :title="item.label"
          >
            <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
            <span class="nav-label">{{ item.label }}</span>
          </div>
        </nav>
        
        <div class="sidebar-footer">
          <div class="user-avatar" @click="activeMenu = 'account'" :class="{ active: activeMenu === 'account' }">
            <el-avatar :size="32" :src="currentUser.avatar" />
          </div>
        </div>
      </aside>

      <!-- 主内容区域 -->
      <main class="settings-main">
        <header class="main-header">
          <div>
            <h2 class="header-title">{{ currentMenuLabel }}</h2>
            <div class="header-subtitle">{{ currentUser.name || '' }}</div>
          </div>
          <div class="header-actions">
            <el-button class="mr-2" type="text" @click="resetToDefault">恢复默认</el-button>
            <el-button class="mr-4" type="primary" @click="onSave">保存并关闭</el-button>
            <el-button class="close-btn" circle text @click="visible = false">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </header>

        <div class="main-content custom-scrollbar">
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
    <EditProfileDialog v-model="showEditProfile" @success="handleProfileUpdate" />
    <ThemeCustomizer v-model="showThemeCustomizer" @change="handleThemeChange" />
  </el-dialog>
</template>

<script setup>
import { ref, watch, reactive, computed, onMounted, onUnmounted, defineAsyncComponent } from 'vue'
import { useStore } from 'vuex'
import { useTheme } from '@/composables/useTheme'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  Bell,
  Setting,
  Files,
  QuestionFilled,
  InfoFilled,
  Close,
  Brush
} from '@element-plus/icons-vue'

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
import ThemeCustomizer from './ThemeCustomizer.vue'

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
const showThemeCustomizer = ref(false)

// 响应式布局
const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 768)
const isFullscreen = computed(() => windowWidth.value < 640)

const dialogWidth = computed(() => {
  if (windowWidth.value < 768) return '100%'
  if (windowWidth.value < 1024) return '800px'
  return '900px'
})

const sidebarWidth = computed(() => {
  if (windowWidth.value < 768) return '64px'
  if (windowWidth.value < 1024) return '180px'
  return '220px'
})

const contentPadding = computed(() => {
  if (windowWidth.value < 768) return '16px'
  if (windowWidth.value < 1024) return '20px'
  return '32px'
})

// 菜单配置
const menuItems = [
  { id: 'account', label: '账号与安全', icon: User },
  { id: 'notification', label: '消息通知', icon: Bell },
  { id: 'general', label: '通用设置', icon: Setting },
  { id: 'theme', label: '主题定制', icon: Brush },
  { id: 'storage', label: '存储管理', icon: Files },
  { id: 'help', label: '帮助反馈', icon: QuestionFilled },
  { id: 'about', label: '关于', icon: InfoFilled }
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
    case 'theme':
      return {
        'open-theme': () => showThemeCustomizer.value = true
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
  for (let key in localStorage) {
    if (localStorage.hasOwnProperty(key)) total += localStorage[key].length + key.length
  }
  cacheSize.value = `${(total / 1024 / 1024).toFixed(2)} MB`
}

// 业务逻辑处理
const handleProfileUpdate = () => store.dispatch('user/getInfo')

const handleSettingsUpdate = (newSettings) => {
  // 实时更新 store (Vuex store is reactive, usually handled by mutations)
  // 这里我们假设 newSettings 是完整的设置对象
  // 实际上 store.commit 会触发 UI 更新
}

const saveSettings = () => {
  // 主动保存：把当前 store 的设置写入本地（Vuex 已经会写 localStorage），并提示
  try {
    store.commit('im/UPDATE_SETTINGS', store.state.im.settings)
    ElMessage.success('设置已保存')
  } catch (e) {
    console.error('保存设置失败', e)
    ElMessage.error('保存失败')
  }
}

// 修正 settings 更新逻辑
// 子组件 emit 'update:modelValue' 时触发
const updateSettingsMap = {
  notification: (val) => store.dispatch('im/updateNotificationSettings', val.notifications),
  general: (val) => store.dispatch('im/updateGeneralSettings', val.general),
  storage: (val) => store.dispatch('im/updateDataSettings', val.data),
}

// 覆盖上面的 handleSettingsUpdate
const realHandleSettingsUpdate = (newSettings) => {
  if (updateSettingsMap[activeMenu.value]) {
    updateSettingsMap[activeMenu.value](newSettings)
  } else {
    // Fallback: update whole settings if needed, or specific sections
    // 我们的子组件返回的是整个 settings 结构副本
    store.commit('im/UPDATE_SETTINGS', newSettings)
  }
}

// 重新绑定事件
watch(activeMenu, () => {
    // 切换菜单时重新计算 cache
    if (activeMenu.value === 'storage') calculateCacheSize()
})

// 重写 computed events 以使用正确的 handler
const finalComponentEvents = computed(() => {
    const events = { ...componentEvents.value }
    if (events['update:modelValue']) {
        events['update:modelValue'] = realHandleSettingsUpdate
    }
    return events
})

// 头部操作：重置为默认、保存并关闭等
const resetToDefault = () => {
  ElMessageBox.confirm('恢复默认设置将移除本地自定义项，是否继续？', '恢复默认', {
    confirmButtonText: '恢复',
    cancelButtonText: '取消',
    type: 'warning'
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
    type: 'warning'
  }).then(() => {
    const keep = ['im-system-settings', 'token', 'user-info']
    Object.keys(localStorage).forEach(key => { if (!keep.includes(key)) localStorage.removeItem(key) })
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

// 主题变更处理
const handleThemeChange = (themeData) => {
  // 可以在这里将主题数据保存到后端或 store
  console.log('主题已变更:', themeData)
}

// 监听
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.defaultMenu) activeMenu.value = props.defaultMenu
  if (val) calculateCacheSize()
})
watch(visible, (val) => { if (!val) emit('update:modelValue', false) })
watch(() => settings.value.general?.theme, (val) => {
    if (val && val !== themeMode.value) setThemeMode(val)
}, { immediate: true })

onMounted(() => {
  window.addEventListener('resize', () => {
    windowWidth.value = window.innerWidth
  })
})
</script>

<style scoped lang="scss">
// Dialog Styles Overlay
:deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.2);
  margin-top: 8vh !important;
  background: transparent;
}

:deep(.el-dialog__header) {
  display: none;
}

:deep(.el-dialog__body) {
  padding: 0;
  height: 640px;
  background: var(--dt-bg-body);
}

.settings-layout {
  display: flex;
  height: 100%;
  background: var(--dt-bg-body);
}

// Sidebar
.settings-sidebar {
  width: 220px;
  background: var(--dt-bg-sidebar); // slightly darker/lighter than body
  border-right: 1px solid var(--dt-border-light);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: width 0.3s ease;
  position: relative;
  
  @media (max-width: 1024px) {
    width: 180px;
  }
  
  @media (max-width: 768px) {
    width: 64px;
  }

  .dark & {
    background: #181818;
    border-right-color: var(--dt-border-dark);
  }
  
  // 添加滑动手势提示（移动端）
  @media (max-width: 768px) {
    &::after {
      content: '';
      position: absolute;
      right: -8px;
      top: 50%;
      transform: translateY(-50%);
      width: 4px;
      height: 40px;
      background: var(--dt-brand-color);
      border-radius: 2px;
      opacity: 0.5;
      transition: opacity 0.3s ease;
    }
    
    &:hover::after {
      opacity: 0.8;
    }
  }
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 24px;
}

.app-brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-icon {
  width: 32px;
  height: 32px;
  background: var(--dt-brand-color);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 800;
  font-size: 18px;
}

.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  height: 40px;
  padding: 0 12px;
  border-radius: 8px;
  cursor: pointer;
  color: var(--dt-text-secondary);
  transition: all 0.2s;
  
  &:hover {
    background: var(--dt-bg-hover);
    color: var(--dt-text-primary);
  }
  
  &.active {
    background: var(--dt-brand-color-light);
    color: var(--dt-brand-color);
    font-weight: 500;
    
    .nav-icon {
      color: var(--dt-brand-color);
    }
  }
}

.nav-icon {
  font-size: 18px;
  margin-right: 12px;
}

.nav-label {
  font-size: 14px;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid var(--dt-border-light);
  display: flex;
  align-items: center;
  
  .dark & {
    border-top-color: var(--dt-border-dark);
  }
}

.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background: var(--dt-bg-hover);
  }
  
  &.active {
    box-shadow: 0 0 0 2px var(--dt-brand-color);
  }
}

// Main Content
.settings-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: var(--dt-bg-body);
}

.main-header {
  height: 64px;
  padding: 0 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--dt-border-light);
  flex-shrink: 0;
  
  @media (max-width: 768px) {
    padding: 0 16px;
  }
  
  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
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
  padding: 32px;
  overflow-y: auto;
  // 确保滚动区域正确
  -webkit-overflow-scrolling: touch;
  
  @media (max-width: 768px) {
    padding: 20px;
  }
  
  // 优化滚动条样式
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  
  &::-webkit-scrollbar-thumb {
    background-color: rgba(0, 0, 0, 0.2);
    border-radius: 3px;
    
    &:hover {
      background-color: rgba(0, 0, 0, 0.3);
    }
  }
}

// 增强的移动端适配
@media (max-width: 768px) {
  .settings-sidebar {
    width: 64px; // Collapsed sidebar
    
    .sidebar-header {
      justify-content: center;
      padding: 0;
      height: 56px; // 稍微减小高度
    }
    
    .nav-item {
      justify-content: center;
      padding: 0;
      height: 48px; // 增大触摸区域
      
      // 添加点击波纹效果
      &:active {
        transform: scale(0.95);
      }
    }
    
    .nav-icon {
      margin-right: 0;
      font-size: 22px;
    }
    
    .nav-label {
      display: none;
    }
    
    .sidebar-footer {
      justify-content: center;
      padding: 12px;
    }
  }
  
  .main-header {
    padding: 0 16px;
    height: 56px; // 与侧边栏保持一致
    
    .header-title {
      font-size: 16px;
    }
  }
  
  .main-content {
    padding: 16px;
  }
  
  // 对话框全屏时的优化
  :deep(.el-dialog) {
    height: 100vh;
    margin: 0 !important;
    
    .el-dialog__body {
      height: 100vh;
      padding: 0;
    }
  }
}

// 平板适配
@media (min-width: 769px) and (max-width: 1024px) {
  .settings-sidebar {
    width: 180px;
    
    .nav-label {
      font-size: 13px;
    }
  }
  
  .main-content {
    padding: 24px;
  }
}
</style>
