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
          <!-- 搜索框 -->
          <div class="sidebar-search" v-if="!isFullscreen">
            <el-input
              v-model="searchQuery"
              placeholder="搜索设置..."
              :prefix-icon="Search"
              clearable
              size="small"
              class="search-input"
            />
          </div>

          <div
            v-for="item in filteredMenuItems"
            :key="item.id"
            class="nav-item"
            :class="{ active: activeMenu === item.id }"
            @click="handleNavClick(item)"
            :title="item.label"
          >
            <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
            <span class="nav-label">{{ item.label }}</span>
            <span v-if="item.badge" class="nav-badge">{{ item.badge }}</span>
          </div>
          
          <div v-if="filteredMenuItems.length === 0" class="no-results">
            未找到结果
          </div>
        </nav>
        
        <div class="sidebar-footer">
          <div class="user-info" @click="activeMenu = 'account'" :class="{ active: activeMenu === 'account' }">
            <el-avatar :size="36" :src="currentUser.avatar" :alt="currentUser.nickname || currentUser.username" />
            <div class="user-details">
              <span class="user-name">{{ currentUser.nickname || currentUser.username }}</span>
            </div>
          </div>
          <div class="app-version" v-if="appVersion">
            版本 {{ appVersion }}
          </div>
        </div>
      </aside>

      <!-- 设置详情区 - 采用标准 Native App 布局 -->
      <section class="settings-content">
        <!-- 关闭按钮 -->
        <button class="settings-close-x" title="关闭" @click="visible = false">
          <el-icon><Close /></el-icon>
        </button>

        <div class="content-scroll-area custom-scrollbar">
          <!-- 页面头部 -->
          <div class="page-header">
            <div class="page-title-group">
              <h2 class="page-title">{{ currentMenuLabel }}</h2>
              <p class="page-desc">{{ currentMenuDescription }}</p>
            </div>
          </div>
          
          <!-- 动态组件内容 -->
          <transition name="settings-fade" mode="out-in">
            <component 
              :is="currentComponent" 
              v-bind="componentProps"
              v-on="finalComponentEvents"
              :key="'settings-' + activeMenu"
              class="active-content"
            />
          </transition>
        </div>
      </section>
    </div>

    <!-- 全局弹窗 -->
    <ChangePasswordDialog v-model="showChangePassword" />
    <EditProfileDialog v-model="showEditProfile" @success="handleProfileUpdate" />
  </el-dialog>
</template>

<script setup>
import { ref, watch, reactive, computed, onMounted, onUnmounted, defineAsyncComponent } from 'vue'
import { removeItem } from '@/utils/storage'

import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
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
  Check,
  Monitor,
  Search
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
const router = useRouter()

const visible = ref(false)
const activeMenu = ref('account')
const searchQuery = ref('')
const showChangePassword = ref(false)
const showEditProfile = ref(false)
const appVersion = ref('1.0.0')

const dialogWidth = '900px'
const sidebarWidth = '240px'

// 移除响应式宽度逻辑
const isFullscreen = computed(() => false) // 始终不全屏，保持 App 感

// 菜单配置
const menuItems = computed(() => {
  const baseItems = [
    { id: 'account', label: '账号与安全', icon: User, description: '管理个人信息和账号安全' },
    { id: 'notification', label: '消息通知', icon: Bell, description: '设置消息通知偏好' },
    { id: 'general', label: '通用设置', icon: Setting, description: '基本功能 and 行为设置' },
    { id: 'storage', label: '存储管理', icon: Files, description: '管理本地存储和缓存' },
    { id: 'help', label: '帮助反馈', icon: QuestionFilled, description: '获取帮助和提交反馈' },
    { id: 'about', label: '关于', icon: InfoFilled, description: '了解应用信息和版本' }
  ]

  // 如果是管理员，添加管理后台入口
  const isAdmin = store.getters['user/isAdmin']
  if (isAdmin) {
    baseItems.splice(baseItems.length - 2, 0, {
      id: 'admin',
      label: '管理后台',
      icon: Monitor,
      description: '进入系统管理后台'
    })
  }

  return baseItems
})

// 过滤后的菜单项
const filteredMenuItems = computed(() => {
  if (!searchQuery.value) return menuItems.value
  const query = searchQuery.value.toLowerCase()
  return menuItems.value.filter(item => 
    item.label.toLowerCase().includes(query) || 
    item.description.toLowerCase().includes(query)
  )
})

const currentMenuLabel = computed(() => menuItems.value.find(i => i.id === activeMenu.value)?.label || '系统设置')
const currentMenuDescription = computed(() => menuItems.value.find(i => i.id === activeMenu.value)?.description || '')

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
      removeItem('im-system-settings')
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

// 处理导航点击
const handleNavClick = (item) => {
  // 如果是管理后台入口，跳转到管理后台
  if (item.id === 'admin') {
    visible.value = false
    router.push('/admin/dashboard')
  } else {
    activeMenu.value = item.id
  }
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
  height: 640px; // 固定高度
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

.sidebar-search {
  padding: 0 8px 12px;
  margin-bottom: 8px;
  border-bottom: 1px solid var(--dt-border-lighter);
  
  :deep(.el-input__wrapper) {
    background-color: var(--dt-bg-body);
    box-shadow: none;
    border: 1px solid var(--dt-border-light);
    border-radius: 6px;
    padding-left: 8px;
    
    &.is-focus {
      border-color: var(--dt-brand-color);
      background-color: #fff;
    }
  }

  .dark & {
    border-bottom-color: var(--dt-border-dark);
    :deep(.el-input__wrapper) {
      background-color: var(--dt-bg-body-dark);
      border-color: var(--dt-border-dark);
      
      &.is-focus {
        background-color: rgba(0, 137, 255, 0.05);
      }
    }
  }
}

.no-results {
  padding: 20px;
  text-align: center;
  color: var(--dt-text-tertiary);
  font-size: 13px;
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
  
  .nav-icon {
    font-size: 18px;
    margin-right: 12px;
    width: 20px;
    text-align: center;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;
  }
}

.nav-item:hover {
  background: var(--dt-bg-hover);
  color: var(--dt-text-primary);
  transform: translateX(2px);
}

.nav-item.active {
  background: var(--dt-bg-hover);
  color: var(--dt-brand-color);
  font-weight: 600;
  
  .nav-icon {
    color: var(--dt-brand-color);
    transform: scale(1.1);
  }

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 12px;
    bottom: 12px;
    width: 3px;
    background: var(--dt-brand-color);
    border-radius: 0 3px 3px 0;
  }
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
  background: var(--dt-bg-hover);
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



.app-version {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  text-align: center;
  padding-top: 8px;
  border-top: 1px solid var(--dt-border-light);
}

// Main Content - 优化后的内容区
.settings-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: #ffffff; // 纯白内容区，对比侧边栏
  position: relative;

  .dark & {
    background: #1d1d1f; // 深色模式专用背景
  }
}

.settings-close-x {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  cursor: pointer;
  color: var(--dt-text-tertiary);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 100;
  
  &:hover {
    background: var(--dt-bg-hover);
    color: var(--dt-text-primary);
    transform: rotate(90deg);
  }
}

.content-scroll-area {
  flex: 1;
  overflow-y: auto;
  padding: 40px 60px; // 增加留白，显得更高端
  scroll-behavior: smooth;
  
  &::-webkit-scrollbar {
    width: 6px;
  }
}

.page-header {
  margin-bottom: 40px;
  
  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0 0 8px 0;
    letter-spacing: -0.5px;
  }
  
  .page-desc {
    font-size: 14px;
    color: var(--dt-text-tertiary);
    margin: 0;
    line-height: 1.5;
  }
}

.active-content {
  animation: contentSlideIn 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes contentSlideIn {
  from {
    opacity: 0;
    transform: translateX(10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

// 移除多余的 main-content 避免混淆
// .main-content 已重命名为 .main-body 在模版中

// 移动端适配


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
    background: rgba(0, 137, 255, 0.2);
    color: var(--dt-brand-color);
  }
  
  .user-info {
    &:hover {
      background: var(--dt-bg-hover-dark);
    }
  }
  
  .user-info.active {
    background: rgba(0, 137, 255, 0.2);
  }
  
  .user-name {
    color: var(--dt-text-primary-dark);
  }
  
  .user-name {
    color: var(--dt-text-primary-dark);
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
