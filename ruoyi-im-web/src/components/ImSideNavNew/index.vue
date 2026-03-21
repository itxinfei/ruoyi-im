<template>
  <nav class="premium-nav-bar">
    <!-- 顶部 Logo -->
    <div class="nav-brand-cell">
      <div class="logo-text">
        IM
      </div>
    </div>

    <!-- 功能导航 -->
    <div class="nav-menu-list">
      <div
        v-for="item in navModules"
        :key="item.key"
        class="nav-icon-cell"
        :class="{ 'active': activeModule === item.key }"
        @click="$emit('switch-module', item.key)"
      >
        <div class="icon-frame">
          <span class="material-icons-outlined">{{ item.icon }}</span>
          <div v-if="item.badge" class="red-dot" />
        </div>
        <span class="icon-label">{{ item.label }}</span>
      </div>
    </div>

    <!-- 底部功能区 (High Productivity) -->
    <div class="nav-footer-cell">
      <!-- 1. 全局搜索 (Spotlight Style) -->
      <div class="footer-btn" title="全局搜索 (Ctrl+K)" @click="showGlobalSearch = true">
        <span class="material-icons-outlined">search</span>
      </div>

      <!-- 2. 主题一键切换 -->
      <div class="footer-btn" :title="isDark ? '切换亮色' : '切换暗色'" @click="toggleTheme">
        <span class="material-icons-outlined">{{ isDark ? 'light_mode' : 'dark_mode' }}</span>
      </div>

      <!-- 3. 全局设置页面 -->
      <div class="footer-btn" title="系统设置" @click="showSettingsPage = true">
        <span class="material-icons-outlined">settings</span>
      </div>

      <!-- 4. 个人中心页面 -->
      <div class="user-avatar-trigger" title="管理个人信息" @click="showProfilePage = true">
        <div class="avatar-hover-wrapper">
          <DingtalkAvatar
            :src="currentUser.avatar"
            :name="currentUser.nickname"
            :user-id="currentUser.id"
            :size="36"
            shape="square"
          />
        </div>
      </div>
    </div>

    <!-- 全局搜索页面 (Spotlight) -->
    <GlobalSearchDialog v-model="showGlobalSearch" />

    <!-- 系统设置页面 (Pro Panel) -->
    <el-dialog
      v-model="showSettingsPage"
      title="系统设置"
      width="800px"
      append-to-body
      destroy-on-close
      class="pro-desktop-dialog"
    >
      <SettingsPanel />
    </el-dialog>

    <!-- 个人中心页面 (Management Page) -->
    <el-dialog
      v-model="showProfilePage"
      title="个人信息管理"
      width="800px"
      append-to-body
      destroy-on-close
      class="pro-desktop-dialog"
    >
      <ProfilePanel />
    </el-dialog>
  </nav>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import GlobalSearchDialog from '@/components/Common/GlobalSearchDialog.vue'
import SettingsPanel from '@/views/SettingsPanel.vue'
import ProfilePanel from '@/views/ProfilePanel.vue'
import { useTheme } from '@/composables/useTheme'

defineProps({ activeModule: String })
defineEmits(['switch-module', 'go-conversation'])
const store = useStore()
const currentUser = computed(() => store.getters['user/currentUser'] || {})

// 页面/弹窗状态
const showGlobalSearch = ref(false)
const showSettingsPage = ref(false)
const showProfilePage = ref(false)

// 主题一键切换
const theme = useTheme()
const isDark = computed(() => theme.isDark.value)
const toggleTheme = () => {
  const wasDark = theme.isDark.value
  theme.toggleTheme()
  // 显示切换提示（等待下一帧以获取更新后的状态）
  setTimeout(() => {
    const nowDark = theme.isDark.value
    if (wasDark !== nowDark) {
      ElMessage({
        message: nowDark ? '已进入深色模式' : '已进入亮色模式',
        type: 'success',
        duration: 1000
      })
    }
  }, 50)
}

// 模块配置
const isAdmin = computed(() => store.getters['user/isAdmin'])
const navModules = computed(() => {
  const base = [
    { key: 'chat', label: '消息', icon: 'chat', badge: true },
    { key: 'contacts', label: '通讯录', icon: 'people_alt', badge: false },
    { key: 'workbench', label: '工作台', icon: 'apps', badge: false },
    { key: 'profile', label: '我的', icon: 'person_outline', badge: false }
  ]
  if (isAdmin.value) base.push({ key: 'admin', label: '管理', icon: 'admin_panel_settings', badge: false })
  return base
})

const handleKeydown = (e) => {
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault()
    showGlobalSearch.value = true
  }
}

onMounted(() => document.addEventListener('keydown', handleKeydown))
onUnmounted(() => document.removeEventListener('keydown', handleKeydown))
</script>

<style scoped lang="scss">
.premium-nav-bar {
  width: var(--dt-nav-sidebar-width, 64px);
  height: 100vh;
  background: linear-gradient(180deg, var(--dt-brand-color) 0%, var(--dt-brand-active) 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--dt-spacing-sm) 0;
  flex-shrink: 0;
  z-index: var(--dt-z-fixed);
}

.nav-brand-cell {
  margin-bottom: var(--dt-spacing-sm);
  
  .logo-text {
    width: var(--dt-avatar-size-md, 36px);
    height: var(--dt-avatar-size-md, 36px);
    background: rgba(255, 255, 255, 0.2);
    border-radius: var(--dt-radius-lg);
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--dt-text-white);
    font-weight: var(--dt-font-weight-bold);
    font-size: var(--dt-font-size-lg);
    border: 1px solid rgba(255, 255, 255, 0.1);
    transition: all var(--dt-transition-fast);
    
    &:hover {
      background: rgba(255, 255, 255, 0.25);
      transform: scale(1.05);
    }
  }
}

.nav-menu-list {
  flex: 1;
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-xs);
  align-items: center;
  padding: var(--dt-spacing-xs) 0;

  .nav-icon-cell {
    width: 100%;
    cursor: pointer;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: var(--dt-spacing-xs) 0;

    .icon-frame {
      width: var(--dt-btn-size-lg, 40px);
      height: var(--dt-btn-height-md, 36px);
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: var(--dt-radius-md);
      transition: all var(--dt-transition-fast);
      position: relative;

      span {
        color: rgba(255, 255, 255, 0.85);
        font-size: var(--dt-icon-size-xl, 22px);
        line-height: 1;
      }
    }

    .icon-label {
      font-size: var(--dt-font-size-xs);
      color: var(--dt-text-white);
      margin-top: var(--dt-spacing-xs, 2px);
      opacity: 0.85;
      font-weight: var(--dt-font-weight-normal);
      transition: all var(--dt-transition-fast);
    }

    &:hover .icon-frame {
      background: rgba(255, 255, 255, 0.15);
      span { color: var(--dt-text-white); }
    }
    
    &:hover .icon-label {
      opacity: 1;
    }
    
    &.active {
      .icon-frame {
        background: var(--dt-text-white);
        box-shadow: var(--dt-shadow-2);
        span {
          color: var(--dt-brand-color);
        }
      }
      .icon-label {
        font-weight: var(--dt-font-weight-semibold);
        opacity: 1;
      }
    }
    
    // 徽标红点
    .red-dot {
      position: absolute;
      top: var(--dt-spacing-xs, 4px);
      right: var(--dt-spacing-xs, 4px);
      width: var(--dt-spacing-xs, 8px);
      height: var(--dt-spacing-xs, 8px);
      background: var(--dt-error-color);
      border-radius: 50%;
      border: 1.5px solid var(--dt-brand-color);
    }
  }
}

.nav-footer-cell {
  margin-top: auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--dt-spacing-xs);
  padding-bottom: var(--dt-spacing-md);
  position: relative;

  // 装饰分隔线
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 50%;
    transform: translateX(-50%);
    width: var(--dt-spacing-lg, 32px);
    height: 1px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: var(--dt-radius-xs);
  }

  .footer-btn {
    width: var(--dt-btn-size-lg, 40px);
    height: var(--dt-btn-size-lg, 40px);
    display: flex;
    align-items: center;
    justify-content: center;
    color: rgba(255, 255, 255, 0.75);
    cursor: pointer;
    border-radius: var(--dt-radius-lg);
    transition: all var(--dt-transition-base);
    position: relative;

    span {
      font-size: var(--dt-icon-size-xl, 22px);
      line-height: 1;
      transition: all var(--dt-transition-base);
    }

    // 微光效果
    &::after {
      content: '';
      position: absolute;
      inset: 0;
      border-radius: var(--dt-radius-lg);
      background: linear-gradient(135deg, rgba(255,255,255,0.1) 0%, transparent 50%);
      opacity: 0;
      transition: opacity var(--dt-transition-base);
    }

    &:hover {
      background: rgba(255, 255, 255, 0.18);
      color: var(--dt-text-white);
      transform: translateY(var(--dt-spacing-xs, -2px)) scale(1.02);
      box-shadow: var(--dt-shadow-float, 0 4px 12px rgba(0, 0, 0, 0.15));

      span {
        transform: scale(1.1);
      }

      &::after {
        opacity: 1;
      }
    }

    &:active {
      transform: translateY(0) scale(0.98);
      background: rgba(255, 255, 255, 0.25);
    }
  }

  .user-avatar-trigger {
    cursor: pointer;
    margin-top: var(--dt-spacing-sm);
    position: relative;

    .avatar-hover-wrapper {
      padding: var(--dt-spacing-xs, 3px);
      border-radius: var(--dt-radius-lg);
      background: rgba(255, 255, 255, 0.1);
      transition: all var(--dt-transition-base);
      position: relative;

      // 状态指示器环
      &::before {
        content: '';
        position: absolute;
        inset: calc(var(--dt-spacing-xs, 2px) * -1);
        border-radius: var(--dt-radius-lg);
        background: linear-gradient(135deg, var(--dt-brand-light) 0%, var(--dt-brand-color) 100%);
        opacity: 0;
        transition: opacity var(--dt-transition-base);
        z-index: -1;
      }

      &:hover {
        background: rgba(255, 255, 255, 0.25);
        transform: scale(1.08);
        box-shadow: var(--dt-shadow-float, 0 4px 16px rgba(0, 0, 0, 0.2));

        &::before {
          opacity: 1;
        }
      }
    }

    // 个人中心提示文字
    &::after {
      content: '个人中心';
      position: absolute;
      left: 50%;
      bottom: calc(100% + var(--dt-spacing-sm, 8px));
      transform: translateX(-50%);
      background: var(--dt-bg-card-dark);
      color: var(--dt-text-white);
      padding: var(--dt-spacing-xs, 4px) var(--dt-spacing-sm, 10px);
      border-radius: var(--dt-radius-sm);
      font-size: var(--dt-font-size-xs);
      white-space: nowrap;
      opacity: 0;
      visibility: hidden;
      transition: all var(--dt-transition-base);
      box-shadow: var(--dt-shadow-2);
      z-index: 100;
    }

    &:hover::after {
      opacity: 1;
      visibility: visible;
      bottom: calc(100% + var(--dt-spacing-md, 12px));
    }
  }
}
</style>

<style lang="scss">
// 全局专业对话框样式 (Pro Desktop)
.pro-desktop-dialog {
  border-radius: var(--dt-radius-2xl) !important;
  overflow: hidden;
  box-shadow: var(--dt-shadow-modal) !important;

  .el-dialog__header {
    margin: 0;
    padding: var(--dt-spacing-lg) var(--dt-spacing-xl);
    border-bottom: 1px solid var(--dt-border-light);
    background: var(--dt-bg-card);
  }

  .el-dialog__title {
    font-weight: var(--dt-font-weight-semibold);
    font-size: var(--dt-font-size-lg);
    color: var(--dt-text-primary);
  }

  .el-dialog__body {
    padding: 0;
    height: var(--dt-dialog-height, 600px);
    overflow: hidden;
    background: var(--dt-bg-card);
  }

  .el-dialog__close {
    color: var(--dt-text-secondary);
    font-size: var(--dt-icon-size-xl, 20px);

    &:hover {
      color: var(--dt-brand-color);
    }
  }
}
</style>
