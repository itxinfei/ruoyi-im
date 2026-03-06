<template>
  <nav class="premium-nav-bar">
    <!-- 顶部 Logo -->
    <div class="nav-brand-cell">
      <div class="logo-text">IM</div>
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
          <div v-if="item.badge" class="red-dot"></div>
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

const props = defineProps({ activeModule: String })
const emit = defineEmits(['switch-module', 'go-conversation'])
const store = useStore()
const currentUser = computed(() => store.getters['user/currentUser'] || {})

// 页面/弹窗状态
const showGlobalSearch = ref(false)
const showSettingsPage = ref(false)
const showProfilePage = ref(false)

// 主题一键切换
const isDark = ref(localStorage.getItem('im_theme_dark') === 'true')
const toggleTheme = () => {
  isDark.value = !isDark.value
  document.documentElement.classList.toggle('dark', isDark.value)
  localStorage.setItem('im_theme_dark', String(isDark.value))
  ElMessage({
    message: isDark.value ? '已进入深色模式' : '已进入亮色模式',
    type: 'success',
    duration: 1000
  })
}

// 模块配置
const isAdmin = computed(() => store.getters['user/isAdmin'])
const navModules = computed(() => {
  const base = [
    { key: 'chat', label: '消息', icon: 'chat', badge: true },
    { key: 'contacts', label: '通讯录', icon: 'people_alt', badge: false },
    { key: 'workbench', label: '工作台', icon: 'apps', badge: false },
    { key: 'todo', label: '待办', icon: 'task_alt', badge: false },
    { key: 'approval', label: '审批', icon: 'approval', badge: false }
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
  width: 64px; height: 100vh;
  background: linear-gradient(180deg, #1a88ff 0%, #1565d8 100%);
  display: flex; flex-direction: column; align-items: center;
  padding: 8px 0; flex-shrink: 0; z-index: 100;
}

.nav-brand-cell {
  margin-bottom: 8px;
  .logo-text {
    width: 36px; height: 36px; background: rgba(255, 255, 255, 0.2);
    border-radius: 8px; display: flex; align-items: center; justify-content: center;
    color: #fff; font-weight: 900; font-size: 16px; border: 1px solid rgba(255, 255, 255, 0.1);
  }
}

.nav-menu-list {
  flex: 1; width: 100%; display: flex; flex-direction: column; gap: 2px; align-items: center;
  
  .nav-icon-cell {
    width: 100%; cursor: pointer; display: flex; flex-direction: column; align-items: center;
    
    .icon-frame {
      width: 38px; height: 36px; display: flex; align-items: center; justify-content: center;
      border-radius: 8px; transition: all 0.2s;
      span { color: rgba(255, 255, 255, 0.8); font-size: 22px; }
    }
    
    .icon-label { font-size: 10px; color: #fff; margin-top: 1px; opacity: 0.9; }

    &:hover .icon-frame { background: rgba(255, 255, 255, 0.15); span { color: #fff; } }
    &.active {
      .icon-frame { background: #fff; box-shadow: 0 4px 12px rgba(0,0,0,0.15); span { color: #1677ff; } }
      .icon-label { font-weight: 700; opacity: 1; }
    }
  }
}

.nav-footer-cell {
  margin-top: auto; display: flex; flex-direction: column; align-items: center; gap: 6px; padding-bottom: 6px;
  
  .footer-btn {
    width: 32px; height: 32px; display: flex; align-items: center; justify-content: center;
    color: rgba(255, 255, 255, 0.8); cursor: pointer; border-radius: 8px; transition: all 0.2s;
    span { font-size: 20px; }
    &:hover { background: rgba(255, 255, 255, 0.2); color: #fff; transform: translateY(-1px); }
  }

  .user-avatar-trigger {
    cursor: pointer;
    .avatar-hover-wrapper {
      padding: 2px; border-radius: 10px; transition: all 0.2s;
      &:hover { background: rgba(255, 255, 255, 0.25); transform: scale(1.05); }
    }
  }
}
</style>

<style lang="scss">
// 全局专业对话框样式 (Pro Desktop)
.pro-desktop-dialog {
  border-radius: 16px !important;
  overflow: hidden;
  .el-dialog__header { margin: 0; padding: 20px 24px; border-bottom: 1px solid #f1f5f9; }
  .el-dialog__title { font-weight: 700; font-size: 16px; color: #1e293b; }
  .el-dialog__body { padding: 0; height: 600px; overflow: hidden; }
}
</style>
