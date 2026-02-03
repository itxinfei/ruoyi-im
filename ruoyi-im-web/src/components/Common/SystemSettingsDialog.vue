<template>
  <el-dialog
    v-model="visible"
    width="900px"
    class="system-settings-dialog"
    destroy-on-close
    append-to-body
    :show-close="false"
    :close-on-click-modal="true"
    :close-on-press-escape="true"
    align-center
  >
    <div class="settings-container">
      <!-- 侧边导航栏 -->
      <aside class="settings-sidebar">
        <div class="sidebar-header">
          <span class="header-title">设置</span>
        </div>
        
        <nav class="sidebar-menu">
          <div
            v-for="item in menuItems"
            :key="item.id"
            class="menu-item"
            :class="{ active: activeMenu === item.id }"
            @click="handleMenuClick(item)"
          >
            <el-icon class="menu-icon"><component :is="item.icon" /></el-icon>
            <span class="menu-label">{{ item.label }}</span>
          </div>
        </nav>
        
        <div class="sidebar-footer">
          <div class="user-info" @click="activeMenu = 'account'">
            <el-avatar :size="32" :src="currentUser.avatar" />
            <div class="user-details">
              <span class="user-name">{{ currentUser.nickname || currentUser.username }}</span>
              <span class="user-status">在线</span>
            </div>
          </div>
        </div>
      </aside>

      <!-- 内容区域 -->
      <section class="settings-content">
        <div class="content-header">
          <h2 class="content-title">{{ currentMenuLabel }}</h2>
          <button class="close-btn" @click="visible = false">
            <el-icon><Close /></el-icon>
          </button>
        </div>
        
        <div class="content-body scrollbar-custom">
          <component 
            :is="currentComponent" 
            v-bind="componentProps"
            v-on="componentEvents"
            :key="activeMenu"
          />
        </div>
      </section>
    </div>

    <!-- 弹窗 -->
    <ChangePasswordDialog v-model="showChangePassword" />
    <EditProfileDialog v-model="showEditProfile" @success="handleProfileUpdate" />
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, defineAsyncComponent, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  User,
  Bell,
  Setting,
  Files,
  QuestionFilled,
  InfoFilled,
  Close,
  Monitor
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
import { getUserSettings, batchUpdateSettings } from '@/api/im/userSettings'
import { exportChatMessages } from '@/api/im/message'

const props = defineProps({
  modelValue: Boolean,
  defaultMenu: { type: String, default: 'account' }
})

const emit = defineEmits(['update:modelValue'])

const store = useStore()
const router = useRouter()

const visible = ref(false)
const activeMenu = ref('account')
const showChangePassword = ref(false)
const showEditProfile = ref(false)

// 从后端加载的设置
const backendSettings = ref(null)
const loadingSettings = ref(false)

// 菜单配置
const menuItems = computed(() => {
  const items = [
    { id: 'account', label: '账号与安全', icon: User },
    { id: 'notification', label: '消息通知', icon: Bell },
    { id: 'general', label: '通用设置', icon: Setting },
    { id: 'storage', label: '存储管理', icon: Files },
    { id: 'help', label: '帮助反馈', icon: QuestionFilled },
    { id: 'about', label: '关于', icon: InfoFilled }
  ]

  if (store.getters['user/isAdmin']) {
    items.splice(items.length - 2, 0, { id: 'admin', label: '管理后台', icon: Monitor })
  }

  return items
})

const currentMenuLabel = computed(() =>
  menuItems.value.find(i => i.id === activeMenu.value)?.label || '设置'
)

const currentUser = computed(() => store.getters['user/currentUser'] || {})

// 组件映射
const componentMap = {
  account: AccountSettings,
  notification: NotificationSettings,
  general: GeneralSettings,
  storage: StorageSettings,
  help: HelpSettings,
  about: AboutSettings
}

const currentComponent = computed(() => componentMap[activeMenu.value] || AccountSettings)

// 组件 Props - 合并后端设置和 Vuex 设置
const componentProps = computed(() => ({
  user: currentUser.value,
  modelValue: {
    // 从后端加载的设置（优先使用）
    ...(backendSettings.value || {}),
    // Vuex 本地设置作为后备
    ...store.state.im.settings
  }
}))

// 加载后端用户设置
const loadBackendSettings = async () => {
  try {
    loadingSettings.value = true
    const res = await getUserSettings()
    if (res.code === 200 && res.data) {
      backendSettings.value = {
        general: res.data.general || {},
        notification: res.data.notification || {},
        security: res.data.security || {},
        storage: res.data.storage || {}
      }
    }
  } catch (error) {
    console.error('加载后端设置失败:', error)
    // 静默失败，使用默认值
  } finally {
    loadingSettings.value = false
  }
}

// 保存设置到后端
const saveBackendSettings = async () => {
  try {
    loadingSettings.value = true
    // 将本地 Vuex 设置转换为后端格式
    const backendData = {
      general: store.state.im.settings.general || {},
      notification: store.state.im.settings.notification || {},
      security: store.state.im.settings.security || {},
      storage: store.state.im.settings.storage || {}
    }
    
    // 合并后端设置
    const mergedSettings = { ...backendSettings.value, ...backendData }
    
    const res = await batchUpdateSettings({
      settings: Object.entries(mergedSettings).map(([key, value]) => ({ key, value: String(value) }))
    })
    
    if (res.code === 200) {
      // 更新后端设置缓存
      backendSettings.value = mergedSettings
      ElMessage.success('设置已保存')
    }
  } catch (error) {
    console.error('保存设置失败:', error)
  } finally {
    loadingSettings.value = false
  }
}

const handleClearCache = () => {
  // 清理 localStorage 缓存
  try {
    const keysToRemove = []
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i)
      if (key && (key.startsWith('im-') || key.includes('cache'))) {
        keysToRemove.push(key)
      }
    }
    keysToRemove.forEach(key => localStorage.removeItem(key))
    ElMessage.success(`已清理 ${keysToRemove.length} 项缓存`)
  } catch (error) {
    console.error('清理缓存失败:', error)
    ElMessage.error('清理缓存失败')
  }
}

const handleExportChat = async () => {
  try {
    // 获取当前会话ID
    const currentConversationId = store.state.im.chat?.currentConversationId
    if (!currentConversationId) {
      ElMessage.warning('请先选择要导出聊天记录的会话')
      return
    }

    // 显示格式选择
    ElMessage.info('聊天记录导出功能开发中，敬请期待')

    // API 调用示例（待后端实现）
    // const res = await exportChatMessages(currentConversationId, {
    //   format: 'txt', // txt, html, pdf
    //   startTime: null,
    //   endTime: null
    // })
    // 处理下载...
  } catch (error) {
    console.error('导出聊天记录失败:', error)
  }
}

const componentEvents = computed(() => ({
  'edit-profile': () => { showEditProfile.value = true },
  'change-password': () => { showChangePassword.value = true },
  'update:modelValue': (val) => {
    visible.value = val
    if (!val) {
      // 对话框关闭时保存设置
      saveBackendSettings()
    }
  },
  'clear-cache': handleClearCache,
  'export-chat': handleExportChat
}))

const handleMenuClick = (item) => {
  if (item.id === 'admin') {
    visible.value = false
    router.push('/admin')
  } else {
    activeMenu.value = item.id
  }
}

const handleProfileUpdate = () => {
  store.dispatch('user/getCurrentUser')
}

// 加载后端设置
onMounted(() => {
  loadBackendSettings()
})

// 监听
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.defaultMenu) activeMenu.value = props.defaultMenu
})
watch(visible, (val) => { if (!val) emit('update:modelValue', false) })
</script>

<style scoped lang="scss">
.settings-container {
  display: flex;
  width: 100%;
  height: 600px;
  background: var(--bg-color);
  border-radius: 8px;
  overflow: hidden;

  .settings-sidebar {
    width: 220px;
    min-width: 220px;
    background: var(--bg-color-overlay);
    border-right: 1px solid var(--border-color-light);
    display: flex;
    flex-direction: column;
    padding: 16px 0;

    .sidebar-header {
      padding: 0 24px;
      margin-bottom: 16px;
      
      .header-title {
        font-size: 20px;
        font-weight: 600;
        color: var(--text-color-primary);
      }
    }

    .sidebar-menu {
      flex: 1;
      padding: 0 12px;
      overflow-y: auto;

      .menu-item {
        display: flex;
        align-items: center;
        height: 40px;
        padding: 0 12px;
        margin-bottom: 4px;
        border-radius: 8px;
        cursor: pointer;
        color: var(--text-color-regular);
        font-size: 14px;
        transition: all 0.2s ease;
        
        &:hover {
          background: var(--bg-color-hover);
          color: var(--text-color-primary);
        }
        
        &.active {
          background: var(--el-color-primary-light-9);
          color: var(--el-color-primary);
          font-weight: 500;
          
          .menu-icon {
            color: var(--el-color-primary);
          }
        }
        
        .menu-icon {
          font-size: 18px;
          margin-right: 12px;
          color: var(--text-color-secondary);
          transition: color 0.2s;
        }
      }
    }

    .sidebar-footer {
      padding: 16px 20px 0;
      margin-top: 8px;
      border-top: 1px solid var(--border-color-light);

      .user-info {
        display: flex;
        align-items: center;
        gap: 12px;
        cursor: pointer;
        padding: 8px;
        border-radius: 8px;
        transition: background 0.2s;
        
        &:hover {
          background: var(--bg-color-hover);
        }
        
        .user-details {
          display: flex;
          flex-direction: column;
          overflow: hidden;
          
          .user-name {
            font-size: 14px;
            font-weight: 500;
            color: var(--text-color-primary);
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
          
          .user-status {
            font-size: 12px;
            color: var(--el-color-success);
            margin-top: 2px;
          }
        }
      }
    }
  }

  .settings-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-width: 0;
    background: var(--bg-color);

    .content-header {
      height: 64px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 32px;
      flex-shrink: 0;

      .content-title {
        font-size: 18px;
        font-weight: 600;
        color: var(--text-color-primary);
        margin: 0;
      }

      .close-btn {
        width: 32px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: transparent;
        border: none;
        cursor: pointer;
        color: var(--text-color-secondary);
        border-radius: 50%;
        transition: all 0.2s;
        
        &:hover {
          background: var(--bg-color-hover);
          color: var(--text-color-primary);
        }
        
        .el-icon {
          font-size: 20px;
        }
      }
    }

    .content-body {
      flex: 1;
      overflow-y: auto;
      overflow-x: hidden;
      padding: 0 32px 32px;
    }
  }
}

// 深色模式适配
.dark {
  .settings-container {
    background: #1a1a1a;
    
    .settings-sidebar {
      background: #252525;
      border-color: #333;
      
      .menu-item.active {
        background: rgba(var(--el-color-primary-rgb), 0.1);
      }
      
      .sidebar-footer {
        border-color: #333;
      }
    }
  }
}

// 对话框样式重写
:deep(.system-settings-dialog) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);

  .el-dialog__header {
    display: none;
  }
  
  .el-dialog__body {
    padding: 0;
    margin: 0;
  }
}
</style>