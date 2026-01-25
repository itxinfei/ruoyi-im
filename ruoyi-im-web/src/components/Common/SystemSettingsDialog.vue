<template>
  <el-dialog
    v-model="visible"
    title=""
    width="1000px"
    class="system-settings-dialog"
    destroy-on-close
    append-to-body
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
          <span>{{ item.label }}</span>
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
                  <span class="status-dot"></span>
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
  </el-dialog>
</template>

<script setup>
import { ref, watch, reactive, computed } from 'vue'
import { useStore } from 'vuex'
import { useTheme } from '@/composables/useTheme'
import { ElMessage } from 'element-plus'
import { VideoPlay, Lock, Edit, Bell, Camera, Position, View, Document, Sunny, Moon, Monitor, Refresh } from '@element-plus/icons-vue'
import ChangePasswordDialog from '@/components/Common/ChangePasswordDialog.vue'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue'])

const store = useStore()
const visible = ref(false)
const showChangePassword = ref(false)
const activeMenu = ref('account')
const { isDark, themeMode, setThemeMode } = useTheme()

const menuItems = [
  { id: 'account', label: '账号安全', icon: 'manage_accounts' },
  { id: 'notification', label: '通知设置', icon: 'notifications' },
  { id: 'privacy', label: '隐私安全', icon: 'security' },
  { id: 'general', label: '通用设置', icon: 'settings' },
  { id: 'about', label: '关于应用', icon: 'info' }
]

const currentUser = computed(() => store.getters['user/currentUser'] || {})

const settings = computed(() => store.state.im.settings)

const localSettings = reactive(JSON.parse(JSON.stringify(settings.value)))

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
  ElMessage.info('编辑资料功能开发中...')
}

const checkUpdate = () => {
  ElMessage.success('当前已是最新版本')
}

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
    gap: 8px;
    padding: 16px 24px;
    font-size: 15px;
    color: var(--dt-text-secondary);
    cursor: pointer;
    transition: all var(--dt-transition-fast);
    border-radius: var(--dt-radius-xl);
    position: relative;
    font-weight: 500;

    .tab-icon {
      font-size: 22px;
      transition: all var(--dt-transition-fast);
    }

    &:hover {
      background: linear-gradient(135deg, var(--dt-brand-color) 0%, var(--dt-brand-active) 100%);
      color: #fff;
      transform: translateY(-2px);
      box-shadow: var(--dt-shadow-3);

      .tab-icon {
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
    }
  }
}

.content-area {
  flex: 1;
  padding: 32px;
  background: var(--dt-bg-card);
  overflow-y: auto;
  scroll-behavior: smooth;

  .section-title {
    font-size: 20px;
    font-weight: 700;
    margin-bottom: 24px;
    color: var(--dt-text-primary);
  }

  .mt-6 {
    margin-top: 32px;
  }
}

.account-section {
  .account-card {
    background: linear-gradient(135deg, var(--dt-bg-body) 0%, var(--dt-bg-card) 100%);
    border-radius: var(--dt-radius-2xl);
    padding: 40px;
    border: 1.5px solid var(--dt-border-light);
    box-shadow: var(--dt-shadow-3);

    .user-info {
      display: flex;
      align-items: center;
      gap: 24px;
      margin-bottom: 32px;

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

          .dark & {
            border-color: var(--dt-bg-card);
          }
        }
      }

      .user-details {
        .username {
          font-size: 22px;
          font-weight: 700;
          color: var(--dt-text-primary);
          margin-bottom: 10px;
        }

        .user-id {
          font-size: 14px;
          color: var(--dt-text-secondary);
          margin-bottom: 6px;
        }

        .user-email {
          font-size: 14px;
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
        font-size: 15px;
        border-radius: var(--dt-radius-xl);
        transition: all var(--dt-transition-fast);
        font-weight: 600;

        &.primary-btn {
          background: linear-gradient(135deg, var(--dt-brand-color) 0%, var(--dt-brand-active) 100%);
          border: none;
          color: #fff;
          box-shadow: var(--dt-shadow-4);

          &:hover {
            transform: translateY(-3px);
            box-shadow: var(--dt-shadow-5);
          }
        }

        &:not(.primary-btn) {
          background: var(--dt-bg-card);
          border: 2px solid var(--dt-border-light);
          color: var(--dt-text-secondary);

          &:hover {
            border-color: var(--dt-brand-color);
            color: var(--dt-brand-color);
            transform: translateY(-2px);
            box-shadow: var(--dt-shadow-3);
          }
        }
      }
    }
  }
}

.settings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 16px;
}

.setting-card {
  background: var(--dt-bg-body);
  border-radius: var(--dt-radius-xl);
  padding: 20px;
  border: 1.5px solid var(--dt-border-light);
  transition: all var(--dt-transition-fast);
  display: flex;
  justify-content: space-between;
  align-items: center;

  &:hover {
    border-color: var(--dt-brand-color);
    transform: translateY(-2px);
    box-shadow: var(--dt-shadow-3);
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

      &.bell-bg { background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%); }
      &.sound-bg { background: linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%); }
      &.keyboard-bg { background: linear-gradient(135deg, #06b6d4 0%, #22d3ee 100%); }
      &.screenshot-bg { background: linear-gradient(135deg, #ec4899 0%, #f472b6 100%); }
      &.quick-bg { background: linear-gradient(135deg, #10b981 0%, #34d399 100%); }
      &.status-bg { background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%); }
      &.receipt-bg { background: linear-gradient(135deg, #6366f1 0%, #818cf8 100%); }
      &.theme-bg { background: linear-gradient(135deg, #f97316 0%, #fb923c 100%); }
      &.language-bg { background: linear-gradient(135deg, #14b8a6 0%, #2dd4bf 100%); }
    }

    .setting-info {
      h4 {
        font-size: 15px;
        font-weight: 600;
        color: var(--dt-text-primary);
        margin-bottom: 4px;
      }

      p {
        font-size: 13px;
        color: var(--dt-text-secondary);
        line-height: 1.4;
      }
    }
  }

  .setting-controls {
    display: flex;
    align-items: center;
    gap: 12px;
  }
}

.about-section {
  .about-card {
    background: var(--dt-bg-body);
    border-radius: var(--dt-radius-2xl);
    padding: 48px;
    text-align: center;
    border: 1.5px solid var(--dt-border-light);

    .app-logo {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 96px;
      height: 96px;
      background: linear-gradient(160deg, var(--dt-brand-color) 0%, var(--dt-brand-active) 100%);
      border-radius: var(--dt-radius-2xl);
      margin-bottom: 24px;
      box-shadow: 0 12px 32px rgba(22, 119, 255, 0.3);

      .logo-inner {
        font-size: 36px;
        font-weight: 800;
        color: #fff;
      }
    }

    .version-info {
      margin-bottom: 24px;

      .version-row {
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 12px;
        margin-bottom: 12px;

        .label {
          font-size: 14px;
          color: var(--dt-text-secondary);
        }

        .value {
          font-size: 14px;
          font-weight: 600;
          color: var(--dt-text-primary);
        }
      }
    }

    .copyright {
      font-size: 13px;
      color: var(--dt-text-tertiary);
      margin-bottom: 24px;
    }

    .check-update-btn {
      background: var(--dt-brand-color);
      border: none;
      color: #fff;
      height: 44px;
      padding: 0 32px;
      border-radius: var(--dt-radius-lg);
      font-size: 15px;
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
