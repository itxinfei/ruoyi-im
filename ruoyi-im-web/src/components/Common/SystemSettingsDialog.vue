<template>
  <el-dialog
    v-model="visible"
    title=""
    :width="dialogWidth"
    :class="['system-settings-dialog', { 'is-mobile': isMobile }]"
    destroy-on-close
    append-to-body
    :fullscreen="isFullscreen"
  >
    <div class="settings-container">
      <div class="tabs-header">
        <div 
          v-for="item in menuItems" 
          :key="item.id"
          class="tab-item"
          :class="{ active: activeMenu === item.id }"
          @click="activeMenu = item.id"
        >
          <span class="material-icons-outlined tab-icon">{{ item.icon }}</span>
          <span class="tab-label">{{ item.label }}</span>
        </div>
      </div>

      <div class="content-area scrollbar-thin">
        <template v-if="activeMenu === 'account'">
          <div class="account-section">
            <h2 class="section-title">账号安全</h2>
            <div class="account-card">
              <div class="user-info">
                <div class="avatar-wrapper">
                  <el-avatar :size="64" :src="currentUser.avatar">
                    {{ currentUser.nickname?.charAt(0) || currentUser.username?.charAt(0) }}
                  </el-avatar>
                  <el-tooltip :content="currentUser.status === 'online' ? '在线' : '离线'" placement="bottom">
                    <span class="status-dot" :class="{ 'status-online': currentUser.status === 'online', 'status-offline': currentUser.status === 'offline' }"></span>
                  </el-tooltip>
                </div>
                <div class="user-details">
                  <h3 class="username">{{ currentUser.nickname || currentUser.username }}</h3>
                  <p class="user-id">UID: {{ currentUser.id }}</p>
                  <p class="user-email">{{ currentUser.email || '未设置邮箱' }}</p>
                </div>
              </div>
              <div class="action-buttons">
                <el-button type="primary" class="action-btn primary-btn" @click="showChangePassword = true">
                  <el-icon><Lock /></el-icon>
                  修改密码
                </el-button>
                <el-button class="action-btn" @click="handleEditProfile">
                  <el-icon><Edit /></el-icon>
                  编辑资料
                </el-button>
              </div>
            </div>
          </div>
        </template>

        <template v-else-if="activeMenu === 'notification'">
          <div class="notification-section">
            <h2 class="section-title">消息通知</h2>
            <div class="settings-grid">
              <div class="setting-card">
                <div class="setting-header">
                  <div class="icon-wrapper bell-bg">
                    <el-icon><Bell /></el-icon>
                  </div>
                  <div class="setting-info">
                    <h4>新消息提醒</h4>
                    <p>在桌面显示新消息通知</p>
                  </div>
                </div>
                <el-switch v-model="localSettings.notifications.enabled" size="large" />
              </div>
              <div class="setting-card">
                <div class="setting-header">
                  <div class="icon-wrapper sound-bg">
                    <el-icon><VideoPlay /></el-icon>
                  </div>
                  <div class="setting-info">
                    <h4>声音提醒</h4>
                    <p>播放新消息提示音</p>
                  </div>
                </div>
                <div class="setting-controls">
                  <el-button link type="primary" size="small" @click="testSound" v-if="localSettings.notifications.sound">
                    <el-icon><VideoPlay /></el-icon>
                    测试音效
                  </el-button>
                  <el-switch v-model="localSettings.notifications.sound" size="large" />
                </div>
              </div>
            </div>

            <h2 class="section-title mt-6">快捷键</h2>
            <div class="settings-grid">
              <div class="setting-card">
                <div class="setting-header">
                  <div class="icon-wrapper keyboard-bg">
                    <el-icon><Keyboard /></el-icon>
                  </div>
                  <div class="setting-info">
                    <h4>发送消息</h4>
                    <p>设置发送消息的快捷按键</p>
                  </div>
                </div>
                <el-select v-model="localSettings.shortcuts.send" size="large" style="width: 160px">
                  <el-option label="Enter" value="enter" />
                  <el-option label="Ctrl + Enter" value="ctrl-enter" />
                </el-select>
              </div>
              <div class="setting-card">
                <div class="setting-header">
                  <div class="icon-wrapper screenshot-bg">
                    <el-icon><Camera /></el-icon>
                  </div>
                  <div class="setting-info">
                    <h4>截图快捷键</h4>
                    <p>全局唤起截图工具 (模拟)</p>
                  </div>
                </div>
                <code class="shortcut-key">Alt + A</code>
              </div>
              <div class="setting-card">
                <div class="setting-header">
                  <div class="icon-wrapper quick-bg">
                    <el-icon><Position /></el-icon>
                  </div>
                  <div class="setting-info">
                    <h4>快捷唤起</h4>
                    <p>快速显示/隐藏 IM 窗口</p>
                  </div>
                </div>
                <code class="shortcut-key">Alt + Q</code>
              </div>
            </div>
          </div>
        </template>

        <template v-else-if="activeMenu === 'privacy'">
          <div class="privacy-section">
            <h2 class="section-title">隐私与安全</h2>
            <div class="settings-grid">
              <div class="setting-card">
                <div class="setting-header">
                  <div class="icon-wrapper status-bg">
                    <el-icon><View /></el-icon>
                  </div>
                  <div class="setting-info">
                    <h4>在线状态</h4>
                    <p>允许他人查看我的在线/离线状态</p>
                  </div>
                </div>
                <el-switch v-model="localSettings.privacy.showStatus" size="large" />
              </div>
              <div class="setting-card">
                <div class="setting-header">
                  <div class="icon-wrapper receipt-bg">
                    <el-icon><Document /></el-icon>
                  </div>
                  <div class="setting-info">
                    <h4>已读回执</h4>
                    <p>发送消息已读回执给对方</p>
                  </div>
                </div>
                <el-switch v-model="localSettings.privacy.readReceipt" size="large" />
              </div>
            </div>
          </div>
        </template>

        <template v-else-if="activeMenu === 'general'">
          <div class="general-section">
            <h2 class="section-title">通用设置</h2>
            <div class="settings-grid">
              <div class="setting-card">
                <div class="setting-header">
                  <div class="icon-wrapper theme-bg">
                    <el-icon><Sunny /></el-icon>
                  </div>
                  <div class="setting-info">
                    <h4>外观主题</h4>
                    <p>切换系统的视觉配色</p>
                  </div>
                </div>
                <el-radio-group v-model="localSettings.general.theme" size="large">
                  <el-radio-button label="light">
                    <el-icon><Sunny /></el-icon>
                    浅色
                  </el-radio-button>
                  <el-radio-button label="dark">
                    <el-icon><Moon /></el-icon>
                    深色
                  </el-radio-button>
                  <el-radio-button label="auto">
                    <el-icon><Monitor /></el-icon>
                    跟随系统
                  </el-radio-button>
                </el-radio-group>
              </div>
              <div class="setting-card">
                <div class="setting-header">
                  <div class="icon-wrapper language-bg">
                    <el-icon><Position /></el-icon>
                  </div>
                  <div class="setting-info">
                    <h4>多语言</h4>
                    <p>切换系统显示语言</p>
                  </div>
                </div>
                <el-select v-model="localSettings.general.language" style="width: 160px" size="large">
                  <el-option label="简体中文" value="zh-CN" />
                  <el-option label="English" value="en-US" />
                </el-select>
              </div>
            </div>
          </div>
        </template>

        <!-- 聊天设置 -->
        <template v-else-if="activeMenu === 'chat'">
          <div class="chat-section">
            <h2 class="section-title">聊天设置</h2>
            <div class="settings-grid">
              <div class="setting-card">
                <div class="setting-header">
                  <div class="icon-wrapper font-bg">
                    <span class="font-icon">A</span>
                  </div>
                  <div class="setting-info">
                    <h4>字体大小</h4>
                    <p>调整聊天消息的字体大小</p>
                  </div>
                </div>
                <el-select v-model="localSettings.chat.fontSize" size="large" style="width: 140px" @change="handleChatSettingChange">
                  <el-option label="小" value="small" />
                  <el-option label="中" value="medium" />
                  <el-option label="大" value="large" />
                  <el-option label="特大" value="xlarge" />
                </el-select>
              </div>
              <div class="setting-card">
                <div class="setting-header">
                  <div class="icon-wrapper bubble-bg">
                    <span class="bubble-icon">💬</span>
                  </div>
                  <div class="setting-info">
                    <h4>气泡样式</h4>
                    <p>调整消息气泡的显示样式</p>
                  </div>
                </div>
                <el-select v-model="localSettings.chat.bubbleStyle" size="large" style="width: 140px" @change="handleChatSettingChange">
                  <el-option label="默认" value="default" />
                  <el-option label="紧凑" value="compact" />
                  <el-option label="宽松" value="loose" />
                </el-select>
              </div>
            </div>
          </div>
        </template>

        <!-- 文件管理 -->
        <template v-else-if="activeMenu === 'file'">
          <div class="file-section">
            <h2 class="section-title">文件管理</h2>
            <div class="settings-grid">
              <div class="setting-card">
                <div class="setting-header">
                  <div class="icon-wrapper image-bg">
                    <span class="file-icon">🖼️</span>
                  </div>
                  <div class="setting-info">
                    <h4>自动下载图片</h4>
                    <p>接收图片时自动下载到本地</p>
                  </div>
                </div>
                <el-switch v-model="localSettings.file.autoDownloadImage" size="large" @change="handleFileSettingChange" />
              </div>
              <div class="setting-card">
                <div class="setting-header">
                  <div class="icon-wrapper file-bg">
                    <span class="file-icon">📁</span>
                  </div>
                  <div class="setting-info">
                    <h4>自动下载文件</h4>
                    <p>接收文件时自动下载到本地</p>
                  </div>
                </div>
                <el-switch v-model="localSettings.file.autoDownloadFile" size="large" @change="handleFileSettingChange" />
              </div>
              <div class="setting-card">
                <div class="setting-header">
                  <div class="icon-wrapper warning-bg">
                    <span class="file-icon">⚠️</span>
                  </div>
                  <div class="setting-info">
                    <h4>文件大小警告</h4>
                    <p>下载大文件前显示确认提示</p>
                  </div>
                </div>
                <el-switch v-model="localSettings.file.sizeWarning" size="large" @change="handleFileSettingChange" />
              </div>
            </div>
          </div>
        </template>

        <!-- 存储与数据 -->
        <template v-else-if="activeMenu === 'storage'">
          <div class="storage-section">
            <h2 class="section-title">存储与数据</h2>
            <div class="settings-grid">
              <div class="setting-card storage-card">
                <div class="setting-header">
                  <div class="icon-wrapper cache-bg">
                    <span class="storage-icon">💾</span>
                  </div>
                  <div class="setting-info">
                    <h4>缓存大小</h4>
                    <p>当前缓存占用约 {{ cacheSize }}</p>
                  </div>
                </div>
                <el-button type="danger" plain @click="handleClearCache">
                  <el-icon><Delete /></el-icon>
                  清理缓存
                </el-button>
              </div>
              <div class="setting-card">
                <div class="setting-header">
                  <div class="icon-wrapper export-bg">
                    <span class="storage-icon">📤</span>
                  </div>
                  <div class="setting-info">
                    <h4>导出聊天记录</h4>
                    <p>将聊天记录导出为 JSON 文件</p>
                  </div>
                </div>
                <el-button type="primary" plain @click="handleExportChat">
                  <el-icon><Download /></el-icon>
                  导出记录
                </el-button>
              </div>
            </div>

            <h2 class="section-title mt-6">数据保留</h2>
            <div class="settings-grid">
              <div class="setting-card">
                <div class="setting-header">
                  <div class="icon-wrapper logout-bg">
                    <span class="data-icon">🔒</span>
                  </div>
                  <div class="setting-info">
                    <h4>退出保留数据</h4>
                    <p>退出登录后保留本地聊天记录</p>
                  </div>
                </div>
                <el-switch v-model="localSettings.data.keepOnLogout" size="large" @change="handleDataSettingChange" />
              </div>
            </div>
          </div>
        </template>

        <template v-else-if="activeMenu === 'about'">
          <div class="about-section">
            <h2 class="section-title">关于 IM</h2>
            <div class="about-card">
              <div class="app-logo">
                <div class="logo-inner">IM</div>
              </div>
              <div class="version-info">
                <div class="version-row">
                  <span class="label">当前版本</span>
                  <span class="value">v4.1.0</span>
                </div>
                <div class="version-row">
                  <span class="label">更新日期</span>
                  <span class="value">2025-01-25</span>
                </div>
                <div class="version-row">
                  <span class="label">开发团队</span>
                  <span class="value">RuoYi-IM Team</span>
                </div>
              </div>
              <div class="copyright">
                © 2025 RuoYi-IM Team. All rights reserved.
              </div>
              <el-button type="primary" class="check-update-btn" @click="checkUpdate">
                <el-icon><Refresh /></el-icon>
                检查更新
              </el-button>
            </div>
          </div>
        </template>
      </div>
    </div>
    <ChangePasswordDialog v-model="showChangePassword" />
    <EditProfileDialog v-model="showEditProfile" @success="handleProfileUpdate" />
  </el-dialog>
</template>

<script setup>
import { ref, watch, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { useTheme } from '@/composables/useTheme'
import { ElMessage, ElMessageBox } from 'element-plus'
import { VideoPlay, Lock, Edit, Bell, Camera, Position, View, Document, Sunny, Moon, Monitor, Refresh, Delete, Download, Keyboard } from '@element-plus/icons-vue'
import ChangePasswordDialog from '@/components/Common/ChangePasswordDialog.vue'
import EditProfileDialog from '@/components/Common/EditProfileDialog.vue'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue'])

const store = useStore()
const visible = ref(false)
const showChangePassword = ref(false)
const showEditProfile = ref(false)
const activeMenu = ref('account')
const { isDark, themeMode, setThemeMode } = useTheme()

// 响应式状态
const windowWidth = ref(window.innerWidth)

// 响应式计算属性
const isMobile = computed(() => windowWidth.value < 768)
const isTablet = computed(() => windowWidth.value >= 768 && windowWidth.value < 1024)
const isSmallDesktop = computed(() => windowWidth.value >= 1024 && windowWidth.value < 1366)
const isFullscreen = computed(() => windowWidth.value < 480)

const dialogWidth = computed(() => {
  if (windowWidth.value < 480) return '100%'
  if (windowWidth.value < 768) return '95%'
  if (windowWidth.value < 1024) return '700px'
  if (windowWidth.value < 1366) return '900px'
  return '1000px'
})

// 窗口大小变化监听
const handleResize = () => {
  windowWidth.value = window.innerWidth
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

const menuItems = [
  { id: 'account', label: '账号安全', icon: 'manage_accounts' },
  { id: 'notification', label: '通知设置', icon: 'notifications' },
  { id: 'privacy', label: '隐私安全', icon: 'security' },
  { id: 'general', label: '通用设置', icon: 'settings' },
  { id: 'chat', label: '聊天设置', icon: 'chat' },
  { id: 'file', label: '文件管理', icon: 'folder' },
  { id: 'storage', label: '存储与数据', icon: 'storage' },
  { id: 'about', label: '关于应用', icon: 'info' }
]

const currentUser = computed(() => store.getters['user/currentUser'] || { status: 'online' })

const settings = computed(() => store.state.im.settings)

const localSettings = reactive(JSON.parse(JSON.stringify(settings.value)))

// 确保新设置分类有默认值
if (!localSettings.chat) {
  localSettings.chat = {
    fontSize: 'medium',
    background: 'default',
    bubbleStyle: 'default',
    sendShortcut: 'enter'
  }
}
if (!localSettings.file) {
  localSettings.file = {
    autoDownloadImage: true,
    autoDownloadFile: false,
    sizeWarning: true
  }
}
if (!localSettings.data) {
  localSettings.data = {
    keepOnLogout: true
  }
}

watch(localSettings, (newVal) => {
  if (JSON.stringify(newVal) !== JSON.stringify(settings.value)) {
    store.commit('im/UPDATE_SETTINGS', JSON.parse(JSON.stringify(newVal)))
  }
}, { deep: true })

watch(() => settings.value, (newVal) => {
  if (JSON.stringify(newVal) !== JSON.stringify(localSettings)) {
    Object.assign(localSettings, JSON.parse(JSON.stringify(newVal)))
  }
}, { deep: true })

watch(() => localSettings.general.theme, (val) => {
  if (val !== themeMode.value) {
    setThemeMode(val)
  }
}, { immediate: true })

const testSound = () => {
  ElMessage.success('测试音效播放中...')
}

const handleEditProfile = () => {
  showEditProfile.value = true
}

const checkUpdate = () => {
  ElMessage.success('当前已是最新版本')
}

// 缓存大小估算
const cacheSize = ref('0 MB')

const calculateCacheSize = () => {
  let total = 0
  for (let key in localStorage) {
    if (localStorage.hasOwnProperty(key)) {
      total += localStorage[key].length + key.length
    }
  }
  const sizeInMB = (total / 1024 / 1024).toFixed(2)
  cacheSize.value = `${sizeInMB} MB`
}

// 聊天设置变更处理
const handleChatSettingChange = () => {
  store.dispatch('im/updateChatSettings', localSettings.chat)
}

// 文件设置变更处理
const handleFileSettingChange = () => {
  store.dispatch('im/updateFileSettings', localSettings.file)
}

// 数据设置变更处理
const handleDataSettingChange = () => {
  store.dispatch('im/updateDataSettings', localSettings.data)
}

// 清理缓存
const handleClearCache = () => {
  ElMessageBox.confirm(
    '清理缓存后将清除所有本地缓存的图片和文件，聊天记录不受影响。是否继续？',
    '清理缓存',
    {
      confirmButtonText: '确认清理',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    const keysToKeep = ['im-system-settings', 'token', 'user-info']
    const keysToRemove = []
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i)
      if (!keysToKeep.includes(key)) {
        keysToRemove.push(key)
      }
    }
    keysToRemove.forEach(key => localStorage.removeItem(key))
    calculateCacheSize()
    ElMessage.success('缓存清理完成')
  }).catch(() => {})
}

// 导出聊天记录
const handleExportChat = () => {
  try {
    const messages = store.state.im.message.messages || []
    const dataStr = JSON.stringify(messages, null, 2)
    const blob = new Blob([dataStr], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `chat-export-${new Date().toISOString().slice(0, 10)}.json`
    link.click()
    URL.revokeObjectURL(url)
    ElMessage.success('聊天记录导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

// 编辑资料成功回调
const handleProfileUpdate = () => {
  // 刷新用户信息
  const currentUser = computed(() => store.getters['user/currentUser'] || { status: 'online' })
}

// 组件挂载时计算缓存大小
calculateCacheSize()

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  if (!val) {
    emit('update:modelValue', false)
  }
})
</script>

<style scoped lang="scss">
.system-settings-dialog {
  :deep(.el-dialog__body) {
    padding: 0;
    height: 560px;
  }

  :deep(.el-dialog__header) {
    display: none;
  }

  :deep(.el-dialog) {
    border-radius: var(--dt-radius-2xl);
  }

  // 移动端全屏模式
  &.is-mobile {
    :deep(.el-dialog__body) {
      height: 100vh;
      max-height: 100vh;
    }
  }
}

// ==================== 响应式断点 ====================

// 超小屏幕 (< 480px)
@media (max-width: 479px) {
  .system-settings-dialog {
    :deep(.el-dialog) {
      border-radius: 0;
      margin: 0;
    }

    :deep(.el-dialog__body) {
      height: 100vh;
    }
  }

  .tabs-header {
    padding: 12px 16px;
    gap: 4px;
    overflow-x: auto;
    overflow-y: hidden;
    white-space: nowrap;
    -webkit-overflow-scrolling: touch;
    scrollbar-width: none;

    &::-webkit-scrollbar {
      display: none;
    }

    .tab-item {
      padding: 10px 14px;
      flex-shrink: 0;

      .tab-icon {
        font-size: 20px;
      }

      .tab-label {
        font-size: 14px;
      }
    }
  }

  .content-area {
    padding: 16px;

    .section-title {
      font-size: 18px;
    }
  }

  .account-section .account-card {
    padding: 20px;

    .user-info {
      flex-direction: column;
      text-align: center;
      gap: 16px;

      .user-details {
        .username {
          font-size: 20px;
        }
      }
    }

    .action-buttons {
      flex-direction: column;

      .action-btn {
        width: 100%;
      }
    }
  }

  .settings-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .setting-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;

    .setting-header {
      width: 100%;

      .icon-wrapper {
        width: 40px;
        height: 40px;
        font-size: 20px;
      }

      .setting-info {
        h4 {
          font-size: 16px;
        }

        p {
          font-size: 14px;
        }
      }
    }

    .setting-controls {
      width: 100%;
      justify-content: space-between;

      .el-select {
        width: 100% !important;
      }
    }
  }

  .shortcut-key {
    font-size: 12px;
    padding: 6px 12px;
  }

  .about-section .about-card {
    padding: 24px 16px;

    .app-logo {
      width: 64px;
      height: 64px;

      .logo-inner {
        font-size: 24px;
      }
    }
  }
}

// 小屏幕 (480px - 767px)
@media (min-width: 480px) and (max-width: 767px) {
  .system-settings-dialog {
    :deep(.el-dialog__body) {
      height: 70vh;
    }
  }

  .tabs-header {
    padding: 16px 20px;
    gap: 6px;
    overflow-x: auto;
    overflow-y: hidden;
    white-space: nowrap;
    -webkit-overflow-scrolling: touch;
    scrollbar-width: none;

    &::-webkit-scrollbar {
      display: none;
    }

    .tab-item {
      padding: 12px 18px;
      flex-shrink: 0;

      .tab-icon {
        font-size: 21px;
      }

      .tab-label {
        font-size: 15px;
      }
    }
  }

  .content-area {
    padding: 20px;

    .section-title {
      font-size: 19px;
    }
  }

  .account-section .account-card {
    padding: 24px;

    .user-info {
      gap: 16px;

      .avatar-wrapper {
        :deep(.el-avatar) {
          width: 56px !important;
          height: 56px !important;
        }
      }

      .user-details {
        .username {
          font-size: 20px;
        }

        .user-id,
        .user-email {
          font-size: 14px;
        }
      }
    }

    .action-buttons {
      gap: 12px;

      .action-btn {
        height: 44px;
        font-size: 15px;
      }
    }
  }

  .settings-grid {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .setting-card {
    padding: 16px;

    .setting-header {
      gap: 12px;

      .icon-wrapper {
        width: 44px;
        height: 44px;
        font-size: 22px;
      }

      .setting-info {
        h4 {
          font-size: 17px;
        }

        p {
          font-size: 14px;
        }
      }
    }
  }

  .shortcut-key {
    font-size: 13px;
    padding: 7px 14px;
  }
}

// 平板 (768px - 1023px)
@media (min-width: 768px) and (max-width: 1023px) {
  .settings-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .tabs-header {
    padding: 16px 20px;

    .tab-item {
      padding: 14px 20px;

      .tab-icon {
        font-size: 21px;
      }

      .tab-label {
        font-size: 16px;
      }
    }
  }

  .account-section .account-card {
    padding: 28px;

    .user-info {
      gap: 18px;

      .avatar-wrapper {
        :deep(.el-avatar) {
          width: 60px !important;
          height: 60px !important;
        }
      }
    }
  }
}

// 小桌面 (1024px - 1365px)
@media (min-width: 1024px) and (max-width: 1365px) {
  .settings-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .account-section .account-card {
    padding: 28px;
  }
}

// 大屏幕 (>= 1366px)
@media (min-width: 1366px) {
  .settings-grid {
    grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
    gap: 18px;
  }
}

.settings-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.tabs-header {
  display: flex;
  gap: 8px;
  padding: 20px 24px;
  background: var(--dt-bg-body);
  border-bottom: 1px solid var(--dt-border-light);

  .tab-item {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    padding: 16px 24px;
    font-size: 18px;
    color: var(--dt-text-secondary);
    cursor: pointer;
    transition: all var(--dt-transition-fast);
    border-radius: var(--dt-radius-lg);
    position: relative;
    font-weight: 600;

    .tab-icon {
      font-size: 22px;
      transition: all var(--dt-transition-fast);
    }

    .tab-label {
      font-size: 18px;
      font-weight: 600;
      color: var(--dt-text-primary);
    }

    &:hover {
      background: linear-gradient(135deg, var(--dt-brand-color) 0%, var(--dt-brand-active) 100%);
      color: #fff;
      transform: translateY(-2px);
      box-shadow: var(--dt-shadow-3);

      .tab-icon {
        color: #fff;
      }

      .tab-label {
        color: #fff;
      }
    }

    &.active {
      background: linear-gradient(135deg, var(--dt-brand-color) 0%, var(--dt-brand-active) 100%);
      color: #fff;
      font-weight: 600;
      box-shadow: var(--dt-shadow-4);
      transform: translateY(-2px);

      .tab-icon {
        color: #fff;
      }

      .tab-label {
        color: #fff;
      }
    }
  }
}

.content-area {
  flex: 1;
  padding: 24px;
  background: var(--dt-bg-card);
  overflow-y: auto;
  scroll-behavior: smooth;

  .section-title {
    font-size: 20px;
    font-weight: 700;
    margin-bottom: 20px;
    color: var(--dt-text-primary);
    border-bottom: 1px solid var(--dt-border-light);
    padding-bottom: 8px;
  }

  .mt-6 {
    margin-top: 24px;
  }
}

.account-section {
  .account-card {
    background: var(--dt-bg-body);
    border-radius: var(--dt-radius-xl);
    padding: 32px;
    border: 1px solid var(--dt-border-light);
    box-shadow: var(--dt-shadow-2);

    .user-info {
      display: flex;
      align-items: center;
      gap: 20px;
      margin-bottom: 28px;

      .avatar-wrapper {
        position: relative;
        padding: 4px;
        background: linear-gradient(135deg, var(--dt-brand-color) 0%, var(--dt-brand-active) 100%);
        border-radius: 50%;
        box-shadow: 0 8px 24px rgba(0, 137, 255, 0.3);

        .status-dot {
          position: absolute;
          bottom: 4px;
          right: 4px;
          width: 16px;
          height: 16px;
          background: #22c55e;
          border: 3px solid #fff;
          border-radius: 50%;
          box-shadow: 0 2px 8px rgba(34, 197, 94, 0.3);

          &.status-online {
            background: #22c55e;
          }

          &.status-offline {
            background: #6b7280;
          }

          .dark & {
            border-color: var(--dt-bg-card);
          }
        }
      }

      .user-details {
        .username {
          font-size: 24px;
          font-weight: 700;
          color: var(--dt-text-primary);
          margin-bottom: 8px;
        }

        .user-id {
          font-size: 15px;
          color: var(--dt-text-secondary);
          margin-bottom: 6px;
        }

        .user-email {
          font-size: 15px;
          color: var(--dt-text-tertiary);
        }
      }
    }

    .action-buttons {
      display: flex;
      gap: 16px;

      .action-btn {
        flex: 1;
        height: 48px;
        font-size: 16px;
        border-radius: var(--dt-radius-lg);
        transition: all var(--dt-transition-fast);
        font-weight: 600;

        &.primary-btn {
          background: linear-gradient(135deg, var(--dt-brand-color) 0%, var(--dt-brand-active) 100%);
          border: none;
          color: #fff;
          box-shadow: var(--dt-shadow-3);

          &:hover {
            transform: translateY(-2px);
            box-shadow: var(--dt-shadow-4);
          }
        }

        &:not(.primary-btn) {
          background: var(--dt-bg-card);
          border: 1px solid var(--dt-border-light);
          color: var(--dt-text-secondary);

          &:hover {
            border-color: var(--dt-brand-color);
            color: var(--dt-brand-color);
            transform: translateY(-1px);
            box-shadow: var(--dt-shadow-2);
          }
        }
      }
    }
  }
}

.settings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.setting-card {
  background: var(--dt-bg-body);
  border-radius: var(--dt-radius-lg);
  padding: 20px;
  border: 1px solid var(--dt-border-light);
  transition: all var(--dt-transition-fast);
  display: flex;
  justify-content: space-between;
  align-items: center;

  &:hover {
    border-color: var(--dt-brand-color);
    transform: translateY(-1px);
    box-shadow: var(--dt-shadow-2);
  }

  .setting-header {
    display: flex;
    align-items: center;
    gap: 16px;
    flex: 1;

    .icon-wrapper {
      width: 48px;
      height: 48px;
      border-radius: var(--dt-radius-lg);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      color: #fff;
      flex-shrink: 0;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);

      &.bell-bg { background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%); }
      &.sound-bg { background: linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%); }
      &.keyboard-bg { background: linear-gradient(135deg, #06b6d4 0%, #22d3ee 100%); }
      &.screenshot-bg { background: linear-gradient(135deg, #ec4899 0%, #f472b6 100%); }
      &.quick-bg { background: linear-gradient(135deg, #10b981 0%, #34d399 100%); }
      &.status-bg { background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%); }
      &.receipt-bg { background: linear-gradient(135deg, #6366f1 0%, #818cf8 100%); }
      &.theme-bg { background: linear-gradient(135deg, #f97316 0%, #fb923c 100%); }
      &.language-bg { background: linear-gradient(135deg, #14b8a6 0%, #2dd4bf 100%); }
      &.font-bg { background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%); }
      &.bubble-bg { background: linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%); }
      &.image-bg { background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%); }
      &.file-bg { background: linear-gradient(135deg, #10b981 0%, #34d399 100%); }
      &.warning-bg { background: linear-gradient(135deg, #ef4444 0%, #f87171 100%); }
      &.cache-bg { background: linear-gradient(135deg, #06b6d4 0%, #22d3ee 100%); }
      &.export-bg { background: linear-gradient(135deg, #6366f1 0%, #818cf8 100%); }
      &.logout-bg { background: linear-gradient(135deg, #f97316 0%, #fb923c 100%); }
    }

    .setting-info {
      flex: 1;

      h4 {
        font-size: 18px;
        font-weight: 600;
        color: var(--dt-text-primary);
        margin-bottom: 6px;
      }

      p {
        font-size: 15px;
        color: var(--dt-text-secondary);
        line-height: 1.5;
      }
    }
  }

  .setting-controls {
    display: flex;
    align-items: center;
    gap: 12px;
  }
}

.font-icon, .bubble-icon, .file-icon, .storage-icon, .data-icon {
  font-size: 24px;
}

.about-section {
  .about-card {
    background: var(--dt-bg-body);
    border-radius: var(--dt-radius-xl);
    padding: 36px;
    text-align: center;
    border: 1px solid var(--dt-border-light);
    box-shadow: var(--dt-shadow-2);

    .app-logo {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 88px;
      height: 88px;
      background: linear-gradient(160deg, var(--dt-brand-color) 0%, var(--dt-brand-active) 100%);
      border-radius: var(--dt-radius-xl);
      margin-bottom: 20px;
      box-shadow: 0 12px 32px rgba(22, 119, 255, 0.3);

      .logo-inner {
        font-size: 32px;
        font-weight: 800;
        color: #fff;
      }
    }

    .version-info {
      margin-bottom: 20px;

      .version-row {
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 12px;
        margin-bottom: 10px;

        .label {
          font-size: 15px;
          color: var(--dt-text-secondary);
        }

        .value {
          font-size: 15px;
          font-weight: 600;
          color: var(--dt-text-primary);
        }
      }
    }

    .copyright {
      font-size: 14px;
      color: var(--dt-text-tertiary);
      margin-bottom: 20px;
    }

    .check-update-btn {
      background: var(--dt-brand-color);
      border: none;
      color: #fff;
      height: 48px;
      padding: 0 32px;
      border-radius: var(--dt-radius-lg);
      font-size: 16px;
      font-weight: 600;
      box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
      transition: all var(--dt-transition-fast);

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(22, 119, 255, 0.4);
      }
    }
  }
}

.shortcut-key {
  background: var(--dt-bg-card);
  padding: 8px 16px;
  border-radius: var(--dt-radius-md);
  font-family: 'Courier New', monospace;
  font-size: 14px;
  font-weight: 600;
  color: var(--dt-text-primary);
  border: 1px solid var(--dt-border-color);
}

.scrollbar-thin::-webkit-scrollbar { width: 6px; }
.scrollbar-thin::-webkit-scrollbar-track { background: transparent; }
.scrollbar-thin::-webkit-scrollbar-thumb {
  background: rgba(0,0,0,0.1);
  border-radius: 3px;
}

// 暗色模式
.dark .system-settings-dialog {
  .tabs-header {
    background: var(--dt-bg-hover-dark);
    border-bottom-color: var(--dt-border-dark);

    .tab-item {
      color: var(--dt-text-secondary-dark);

      &:hover {
        background: var(--dt-bg-active-dark);
      }
    }
  }

  .content-area {
    background: var(--dt-bg-card-dark);

    .section-title {
      color: var(--dt-text-primary-dark);
    }
  }

  .account-card {
    background: var(--dt-bg-hover-dark);
    border-color: var(--dt-border-dark);

    .user-details {
      .username {
        color: var(--dt-text-primary-dark);
      }

      .user-id {
        color: var(--dt-text-secondary-dark);
      }

      .user-email {
        color: var(--dt-text-tertiary-dark);
      }
    }

    .action-buttons .action-btn:not(.primary-btn) {
      background: var(--dt-bg-card-dark);
      border-color: var(--dt-border-dark);
      color: var(--dt-text-secondary-dark);

      &:hover {
        border-color: var(--dt-brand-color);
        color: var(--dt-brand-color);
      }
    }
  }

  .setting-card {
    background: var(--dt-bg-hover-dark);
    border-color: var(--dt-border-dark);

    .setting-header .setting-info {
      h4 {
        color: var(--dt-text-primary-dark);
      }

      p {
        color: var(--dt-text-secondary-dark);
      }
    }
  }

  .about-card {
    background: var(--dt-bg-hover-dark);
    border-color: var(--dt-border-dark);

    .version-info .version-row {
      .label {
        color: var(--dt-text-secondary-dark);
      }

      .value {
        color: var(--dt-text-primary-dark);
      }
    }

    .copyright {
      color: var(--dt-text-tertiary-dark);
    }
  }

  .shortcut-key {
    background: var(--dt-bg-card-dark);
    color: var(--dt-text-primary-dark);
    border-color: var(--dt-border-dark);
  }

  .scrollbar-thin::-webkit-scrollbar-thumb {
    background: rgba(255,255,255,0.1);
  }
}
</style>
