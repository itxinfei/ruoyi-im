<template>
  <el-dialog
    v-model="visible"
    :width="dialogWidth"
    class="el-dialog system-settings-dialog"
    destroy-on-close
    append-to-body
    :fullscreen="isFullscreen"
    :show-close="false"
    :close-on-click-modal="true"
    :close-on-press-escape="true"
  >
    <div class="settings-layout">
      <!-- 侧边导航栏 -->
      <aside class="settings-sidebar">
        <div class="sidebar-header">
          <div class="app-brand">
            <el-icon class="brand-icon"><Setting /></el-icon>
            <span class="brand-text">系统设置</span>
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
            <span v-if="item.badge" class="nav-badge">{{ item.badge }}</span>
          </div>
        </nav>
        
        <div class="sidebar-footer">
          <div class="user-info" @click="activeMenu = 'account'" :class="{ active: activeMenu === 'account' }">
            <el-avatar :size="36" :src="currentUser.avatar" :alt="currentUser.nickname || currentUser.username" />
            <div class="user-details">
              <span class="user-name">{{ currentUser.nickname || currentUser.username }}</span>
              <span class="user-status" :class="{ online: currentUser.status === 'online' }">
                {{ currentUser.status === 'online' ? '在线' : '离线' }}
              </span>
            </div>
          </div>
          <div class="app-version" v-if="appVersion">
            版本 {{ appVersion }}
          </div>
        </div>
      </aside>

      <!-- 主内容区域 -->
      <main class="settings-main">
        <header class="main-header">
          <div>
            <h2 class="header-title">{{ currentMenuLabel }}</h2>
            <div class="header-subtitle">{{ currentMenuDescription }}</div>
          </div>
          <div class="header-actions">
            <el-tooltip content="恢复默认设置" placement="bottom">
              <el-button class="action-btn" type="text" @click="resetToDefault">
                <el-icon><Refresh /></el-icon>
                <span class="btn-text">恢复默认</span>
              </el-button>
            </el-tooltip>
            <el-tooltip content="保存设置并关闭" placement="bottom">
              <el-button class="action-btn primary" type="primary" @click="onSave">
                <el-icon><Check /></el-icon>
                <span class="btn-text">保存并关闭</span>
              </el-button>
            </el-tooltip>
            <el-tooltip content="关闭设置" placement="bottom">
              <el-button class="action-btn close-btn" circle text @click="visible = false">
                <el-icon><Close /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
        </header>

        <div class="main-content custom-scrollbar">
          <transition name="settings-fade" mode="out-in">
            <component 
              :is="currentComponent" 
              v-bind="componentProps"
              v-on="finalComponentEvents"
              key="settings-" + activeMenu
            />
          </transition>
        </div>
      </main>
    </div>

    <!-- 全局弹窗 -->
    <ChangePasswordDialog v-model="showChangePassword" />
    <EditProfileDialog v-model="showEditProfile" @success="handleProfileUpdate" />
  </el-dialog>
</template>

<script setup>
import { ref, watch, reactive, computed, onMounted, onUnmounted, defineAsyncComponent } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  Bell,
  Setting,
  Files,
  QuestionFilled,
  InfoFilled,
  Close,
  Brush,
  Refresh,
  Check
} from '@element-plus/icons-vue'

// 动态导入子组件
const AccountSettings = defineAsyncComponent(() => import('../Settings/AccountSettings.vue'))
const NotificationSettings = defineAsyncComponent(() => import('../Settings/NotificationSettings.vue'))
const GeneralSettings = defineAsyncComponent(() => import('../Settings/GeneralSettings.vue'))
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

const visible = ref(false)
const activeMenu = ref('account')
const showChangePassword = ref(false)
const showEditProfile = ref(false)
const appVersion = ref('1.0.0')

// 响应式布局
const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 768)
const isFullscreen = computed(() => windowWidth.value < 640)

const dialogWidth = computed(() => {
  if (windowWidth.value < 768) return '100%'
  if (windowWidth.value < 1024) return '800px'
  if (windowWidth.value < 1440) return '900px'
  return '1000px'
})

const sidebarWidth = computed(() => {
  if (windowWidth.value < 768) return '64px'
  if (windowWidth.value < 1024) return '180px'
  if (windowWidth.value < 1440) return '220px'
  return '260px'
})

const contentPadding = computed(() => {
  if (windowWidth.value < 768) return '16px'
  if (windowWidth.value < 1024) return '20px'
  if (windowWidth.value < 1440) return '24px'
  return '32px'
})

// 监听窗口大小变化
const handleResize = () => {
  windowWidth.value = window.innerWidth
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

// 菜单配置
const menuItems = [
  { id: 'account', label: '账号与安全', icon: User, description: '管理个人信息和账号安全' },
  { id: 'notification', label: '消息通知', icon: Bell, description: '设置消息通知偏好' },
  { id: 'general', label: '通用设置', icon: Setting, description: '基本功能 and 行为设置' },
  { id: 'storage', label: '存储管理', icon: Files, description: '管理本地存储和缓存' },
  { id: 'help', label: '帮助反馈', icon: QuestionFilled, description: '获取帮助和提交反馈' },
  { id: 'about', label: '关于', icon: InfoFilled, description: '了解应用信息和版本' }
]

const currentMenuLabel = computed(() => menuItems.find(i => i.id === activeMenu.value)?.label || '系统设置')
const currentMenuDescription = computed(() => menuItems.find(i => i.id === activeMenu.value)?.description || '')

// 数据源
const currentUser = computed(() => store.getters['user/currentUser'] || {})
const settings = computed(() => store.state.im.settings)

// 组件映射
const componentMap = {
  account: AccountSettings,
  notification: NotificationSettings,
  general: GeneralSettings,
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
    'update:modelValue': realHandleSettingsUpdate,
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
  try {
    for (let key in localStorage) {
      if (localStorage.hasOwnProperty(key)) {
        try {
          total += localStorage[key].length + key.length
        } catch (e) {
          // 忽略无法读取的项
        }
      }
    }
  } catch (e) {
    console.warn('计算缓存大小失败:', e)
  }
  cacheSize.value = `${(total / 1024 / 1024).toFixed(2)} MB`
}

// 业务逻辑处理
const handleProfileUpdate = () => store.dispatch('user/getInfo')

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



// 监听
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.defaultMenu) activeMenu.value = props.defaultMenu
  if (val) calculateCacheSize()
})
watch(visible, (val) => { if (!val) emit('update:modelValue', false) })

onMounted(() => {
  window.addEventListener('resize', () => {
    windowWidth.value = window.innerWidth
  })
})
</script>

<style scoped lang="scss">
// Dialog Styles Overlay
:deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  margin-top: 4vh !important;
  background: var(--dt-bg-body);
  border: 1px solid var(--dt-border-light);
}

:deep(.el-dialog__header) {
  display: none;
}

:deep(.el-dialog__body) {
  padding: 0 !important;
  margin: 0 !important;
  height: 720px;
  background: var(--dt-bg-body);
  border: none;
}

// 过渡动画
.settings-fade-enter-active,
.settings-fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.settings-fade-enter-from,
.settings-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.settings-layout {
  display: flex;
  height: 100%;
  width: 100%;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  background: var(--dt-bg-body);
}

// Sidebar - 优化后的侧边栏
.settings-sidebar {
  width: v-bind(sidebarWidth);
  background: var(--dt-bg-card);
  border-right: 1px solid var(--dt-border-light);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.02);
  min-width: 64px;
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid var(--dt-border-light);
  background: var(--dt-bg-card);
}

.app-brand {
  display: flex;
  align-items: center;
  gap: 8px;
}

.brand-icon {
  font-size: 20px;
  color: var(--dt-brand-color);
  font-weight: 600;
}

.brand-text {
  font-size: 16px;
  font-weight: 600;
  color: var(--dt-text-primary);
  letter-spacing: 0.5px;
}

.sidebar-nav {
  flex: 1;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow-y: auto;
  
  // 优化滚动条样式
  &::-webkit-scrollbar {
    width: 4px;
  }
  
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  
  &::-webkit-scrollbar-thumb {
    background-color: rgba(0, 0, 0, 0.1);
    border-radius: 2px;
    
    &:hover {
      background-color: rgba(0, 0, 0, 0.2);
    }
  }
}

.nav-item {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  height: 44px;
  padding: 0 16px;
  border-radius: 8px;
  cursor: pointer;
  color: var(--dt-text-secondary);
  transition: all 0.2s ease;
  user-select: none;
  font-size: 14px;
  font-weight: 500;
  position: relative;
  overflow: hidden;
  flex-direction: row;
}

.nav-item:hover {
  background: var(--dt-bg-hover);
  color: var(--dt-text-primary);
  transform: translateX(2px);
}

.nav-item.active {
  background: rgba(22, 119, 255, 0.1);
  color: var(--dt-brand-color);
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(22, 119, 255, 0.15);
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 12px;
  bottom: 12px;
  width: 3px;
  background: var(--dt-brand-color);
  border-radius: 0 3px 3px 0;
}

.nav-icon {
  font-size: 18px;
  margin-right: 12px;
  width: 20px;
  text-align: center;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-label {
  flex: 1;
  font-size: 14px;
  transition: all 0.2s ease;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-right: 8px;
  display: flex;
  align-items: center;
}

.nav-badge {
  background: var(--dt-brand-color);
  color: white;
  font-size: 12px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 10px;
  min-width: 20px;
  text-align: center;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid var(--dt-border-light);
  background: var(--dt-bg-card);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  width: 100%;
}

.user-info:hover {
  background: var(--dt-bg-hover);
}

.user-info.active {
  background: rgba(22, 119, 255, 0.1);
  box-shadow: 0 2px 8px rgba(22, 119, 255, 0.15);
  
  .user-name {
    color: var(--dt-brand-color);
  }
  
  .user-status {
    color: var(--dt-brand-color);
  }
}

.user-details {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 2px;
}

.user-status {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  transition: color 0.2s ease;
}

.user-status.online {
  color: #67C23A;
}

.app-version {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  text-align: center;
  padding-top: 8px;
  border-top: 1px solid var(--dt-border-light);
}

// Main Content - 优化后的主内容区
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
  background: var(--dt-bg-body);
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.header-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;
  letter-spacing: 0.5px;
}

.header-subtitle {
  font-size: 13px;
  color: var(--dt-text-secondary);
  margin-top: 4px;
  font-weight: 400;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 36px;
  padding: 0 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
  
  .btn-text {
    white-space: nowrap;
  }
  
  &:hover {
    background: var(--dt-bg-hover);
  }
}

.action-btn.primary {
  background: var(--dt-brand-color);
  color: white;
  
  &:hover {
    background: var(--dt-brand-color-dark);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
  }
}

.close-btn {
  font-size: 20px;
  color: var(--dt-text-secondary);
  padding: 4px;
  border-radius: 6px;
  transition: all 0.2s ease;
  
  &:hover {
    color: var(--dt-text-primary);
    background: var(--dt-bg-hover);
  }
}

.main-content {
  flex: 1;
  padding: v-bind(contentPadding);
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  
  // 优化滚动条样式
  &::-webkit-scrollbar {
    width: 8px;
  }
  
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  
  &::-webkit-scrollbar-thumb {
    background-color: rgba(0, 0, 0, 0.15);
    border-radius: 4px;
    
    &:hover {
      background-color: rgba(0, 0, 0, 0.25);
    }
  }
}

// 移动端适配
@media (max-width: 768px) {
  :deep(.el-dialog) {
    border-radius: 0;
    margin: 0 !important;
    height: 100vh;
    max-height: 100vh;
    border: none;
    
    .el-dialog__body {
      padding: 0 !important;
      margin: 0 !important;
      height: 100vh;
      max-height: 100vh;
      border: none;
    }
  }
  
  .settings-sidebar {
    width: 72px;
    
    .sidebar-header {
      justify-content: center;
      padding: 0;
    }
    
    .brand-text {
      display: none;
    }
    
    .nav-item {
      justify-content: center;
      padding: 0;
      height: 52px;
    }
    
    .nav-item.active::before {
      left: 12px;
    }
    
    .nav-icon {
      margin-right: 0;
      font-size: 22px;
    }
    
    .nav-label,
    .nav-badge {
      display: none;
    }
    
    .sidebar-footer {
      align-items: center;
      padding: 12px 8px;
    }
    
    .user-details {
      display: none;
    }
    
    .app-version {
      font-size: 11px;
      padding: 4px;
    }
  }
  
  .main-header {
    padding: 0 16px;
    height: 56px;
  }
  
  .header-title {
    font-size: 18px;
  }
  
  .action-btn {
    padding: 0 12px;
    font-size: 13px;
    
    .btn-text {
      display: none;
    }
  }
  
  .main-content {
    padding: 16px;
  }
}

// 平板适配
@media (min-width: 769px) and (max-width: 1024px) {
  .settings-sidebar {
    width: 220px;
    min-width: 220px;
  }
  
  .main-header {
    padding: 0 24px;
  }
  
  .main-content {
    padding: 24px;
  }
  
  :deep(.el-dialog) {
    margin-top: 3vh !important;
  }
  
  :deep(.el-dialog__body) {
    height: 680px;
  }
}

// 小屏幕适配
@media (min-width: 1025px) and (max-width: 1200px) {
  .settings-sidebar {
    width: 240px;
    min-width: 240px;
  }
}

// 暗黑模式适配
.dark {
  :deep(.el-dialog) {
    border-color: var(--dt-border-dark);
  }
  
  .settings-sidebar {
    background: var(--dt-bg-card-dark);
    border-right-color: var(--dt-border-dark);
    box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
  }
  
  .sidebar-header,
  .sidebar-footer {
    border-color: var(--dt-border-dark);
    background: var(--dt-bg-card-dark);
  }
  
  .nav-item {
    color: var(--dt-text-secondary-dark);
    
    &:hover {
      color: var(--dt-text-primary-dark);
      background: var(--dt-bg-hover-dark);
    }
  }
  
  .nav-item.active {
    background: rgba(22, 119, 255, 0.2);
    color: var(--dt-brand-color);
  }
  
  .user-info {
    &:hover {
      background: var(--dt-bg-hover-dark);
    }
  }
  
  .user-info.active {
    background: rgba(22, 119, 255, 0.2);
  }
  
  .user-name {
    color: var(--dt-text-primary-dark);
  }
  
  .user-status {
    color: var(--dt-text-tertiary-dark);
  }
  
  .main-header {
    border-bottom-color: var(--dt-border-dark);
    background: var(--dt-bg-body-dark);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
  
  .header-title {
    color: var(--dt-text-primary-dark);
  }
  
  .header-subtitle {
    color: var(--dt-text-secondary-dark);
  }
  
  .action-btn {
    &:hover {
      background: var(--dt-bg-hover-dark);
    }
  }
  
  .close-btn {
    color: var(--dt-text-secondary-dark);
    
    &:hover {
      color: var(--dt-text-primary-dark);
      background: var(--dt-bg-hover-dark);
    }
  }
}

// 高对比度模式
@media (prefers-contrast: high) {
  .settings-sidebar {
    border-right-width: 2px;
  }
  
  .nav-item.active {
    border-left: 3px solid var(--dt-brand-color);
  }
}

// 减少动画偏好
@media (prefers-reduced-motion: reduce) {
  .settings-sidebar,
  .nav-item,
  .user-info,
  .action-btn {
    transition: none;
  }
  
  .settings-fade-enter-active,
  .settings-fade-leave-active {
    transition: none;
  }
}
</style>
