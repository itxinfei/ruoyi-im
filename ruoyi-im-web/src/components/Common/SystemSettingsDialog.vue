<template>
  <el-dialog
    v-model="visible"
    width="1000px"
    class="system-settings-dialog"
    destroy-on-close
    append-to-body
    :show-close="false"
    :close-on-click-modal="true"
    :close-on-press-escape="true"
    title=""
  >
    <div class="settings-container">
      <!-- 侧边导航栏 -->
      <aside class="settings-sidebar">
        <div class="sidebar-header">
          <el-icon class="header-icon"><Setting /></el-icon>
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
            <el-avatar :size="28" :src="currentUser.avatar" />
            <span class="user-name">{{ currentUser.nickname || currentUser.username }}</span>
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
        
        <div class="content-body">
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
import { getUserSettings } from '@/api/im/userSettings'

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
    ElMessage.error('加载设置失败')
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
    } else {
      ElMessage.error('保存失败: ' + (res.msg || '未知错误'))
    }
  } catch (error) {
    console.error('保存设置失败:', error)
    ElMessage.error('保存设置失败')
  } finally {
    loadingSettings.value = false
  }
}

const handleClearCache = () => {
  store.dispatch('im/clearCache').then(() => {
    ElMessage.success('缓存已清理')
  }).catch(() => {
    ElMessage.error('清理缓存失败')
  })
}

const handleExportChat = () => {
  store.dispatch('im/exportChatHistory').then(() => {
    ElMessage.success('聊天记录导出成功')
  }).catch(() => {
    ElMessage.error('导出失败')
  })
}

const componentEvents = computed(() => ({
  'edit-profile': () => { showEditProfile.value = true },
  'change-password': () => { showChangePassword.value = true },
  'update:modelValue': (val) => {
    // 保存到 Vuex 并同步到后端
    store.dispatch('im/updateSettings', val)
    saveBackendSettings()
  },
  'change': () => store.dispatch('im/saveSettings'),
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
  background: #fff;

  .settings-sidebar {
    width: 140px;
    min-width: 140px;
    background: #f5f5f5;
    border-right: 1px solid #e0e0e0;
    display: flex;
    flex-direction: column;

    .sidebar-header {
      height: 48px;
      display: flex;
      align-items: center;
      padding: 0 12px;
      border-bottom: 1px solid #e0e0e0;
      background: #f5f5f5;
      
      .header-icon {
        font-size: 16px;
        color: #666;
        margin-right: 8px;
      }
      
      .header-title {
        font-size: 14px;
        font-weight: 600;
        color: #333;
      }
    }

    .sidebar-menu {
      flex: 1;
      padding: 4px;
      overflow-y: auto;

      .menu-item {
        display: flex;
        align-items: center;
        height: 32px;
        padding: 0 8px;
        cursor: pointer;
        color: #666;
        font-size: 12px;
        transition: all 0.15s;
        margin-bottom: 1px;
        
        &:hover {
          background: #e8e8e8;
          color: #333;
        }
        
        &.active {
          background: #fff;
          color: #1890ff;
          font-weight: 500;
        }
        
        .menu-icon {
          font-size: 14px;
          margin-right: 8px;
        }
        
        .menu-label {
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }
    }

    .sidebar-footer {
      padding: 8px;
      border-top: 1px solid #e0e0e0;
      background: #f5f5f5;

      .user-info {
        display: flex;
        align-items: center;
        gap: 8px;
        cursor: pointer;
        padding: 4px;
        border-radius: 4px;
        
        &:hover {
          background: #e8e8e8;
        }
        
        .user-name {
          font-size: 12px;
          color: #333;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }
    }
  }

  .settings-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-width: 0;
    max-width: calc(1000px - 140px);
    background: #fff;

    .content-header {
      height: 48px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 16px;
      border-bottom: 1px solid #e8e8e8;
      background: #fff;

      .content-title {
        font-size: 14px;
        font-weight: 600;
        color: #333;
        margin: 0;
      }

      .close-btn {
        width: 24px;
        height: 24px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: transparent;
        border: none;
        cursor: pointer;
        color: #999;
        border-radius: 4px;
        
        &:hover {
          background: #f5f5f5;
          color: #666;
        }
      }
    }

    .content-body {
      flex: 1;
      overflow-y: auto;
      overflow-x: hidden;
      padding: 12px 16px;
      max-width: 100%;
      box-sizing: border-box;
    }
  }
}

// 深色模式
.dark {
  .settings-container {
    background: #1a1a1a;

    .settings-sidebar {
      background: #252525;
      border-color: #333;

      .sidebar-header {
        background: #252525;
        border-color: #333;
        
        .header-icon,
        .header-title {
          color: #ccc;
        }
      }

      .menu-item {
        color: #999;
        
        &:hover {
          background: #333;
          color: #ccc;
        }
        
        &.active {
          background: #1a1a1a;
          color: #1890ff;
        }
      }

      .sidebar-footer {
        background: #252525;
        border-color: #333;

        .user-info {
          .user-name {
            color: #ccc;
          }
          
          &:hover {
            background: #333;
          }
        }
      }
    }

    .settings-content {
      background: #1a1a1a;

      .content-header {
        background: #1a1a1a;
        border-color: #333;

        .content-title {
          color: #ccc;
        }
      }
    }
  }
}

// 对话框样式
:deep(.system-settings-dialog) {
  .el-dialog__body {
    padding: 0 !important;
    margin: 0 !important;
  }

  .el-dialog__header {
    display: none;
  }
}
</style>
