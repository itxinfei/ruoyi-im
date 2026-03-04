<template>
  <nav class="premium-nav-bar">
    <!-- 顶部 Logo -->
    <div class="nav-brand-cell">
      <div class="logo-text">IM</div>
    </div>

    <!-- 功能导航 (全图标显示) -->
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

    <!-- 底部功能 -->
    <div class="nav-footer-cell">
      <div class="footer-btn" title="全局搜索 (Ctrl+K)" @click="showGlobalSearch = true">
        <span class="material-icons-outlined">search</span>
      </div>
      <div class="footer-btn" title="切换主题" @click="toggleTheme">
        <span class="material-icons-outlined">{{ isDark ? 'light_mode' : 'dark_mode' }}</span>
      </div>
      <div class="footer-btn" title="系统设置" @click="showSettings = true">
        <span class="material-icons-outlined">settings</span>
      </div>
      <div class="user-avatar-trigger" title="个人资料" @click="showProfile = true">
        <DingtalkAvatar :src="currentUser.avatar" :name="currentUser.nickname" :user-id="currentUser.id" :size="40" shape="square" />
      </div>
    </div>

    <!-- 全局搜索弹窗 -->
    <GlobalSearchDialog
      v-model="showGlobalSearch"
      @go-message="handleGoMessage"
      @go-contact="handleGoContact"
      @go-group="handleGoGroup"
    />

    <!-- 系统设置弹窗 -->
    <SystemSettingsDialog v-model="showSettings" />

    <!-- 个人资料弹窗 -->
    <PersonalProfileDialog v-model="showProfile" />
  </nav>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import GlobalSearchDialog from '@/components/Common/GlobalSearchDialog.vue'
import SystemSettingsDialog from '@/components/Common/SystemSettingsDialog.vue'
import PersonalProfileDialog from '@/components/Common/PersonalProfileDialog.vue'
import { ElMessage } from 'element-plus'

const props = defineProps({ activeModule: String })
const emit = defineEmits(['switch-module', 'go-conversation'])
const store = useStore()
const currentUser = computed(() => store.getters['user/currentUser'])

// 弹窗状态
const showGlobalSearch = ref(false)
const showSettings = ref(false)
const showProfile = ref(false)

// 主题状态
const isDark = ref(false)

// 切换主题
const toggleTheme = () => {
  isDark.value = !isDark.value
  document.documentElement.classList.toggle('dark', isDark.value)
  ElMessage.success(isDark.value ? '已切换到深色模式' : '已切换到浅色模式')
}

// 快捷键处理
const handleKeydown = (e) => {
  // Ctrl+K 打开全局搜索
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault()
    showGlobalSearch.value = true
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})

// 搜索结果跳转处理
const handleGoMessage = (msg) => {
  if (msg.conversationId) {
    emit('go-conversation', msg.conversationId)
  }
}

const handleGoContact = (contact) => {
  emit('switch-module', 'contacts')
}

const handleGoGroup = (group) => {
  if (group.conversationId) {
    emit('go-conversation', group.conversationId)
  }
}

// 判断是否为管理员
const isAdmin = computed(() => {
  const role = localStorage.getItem('im_user_role')
  return role === 'ADMIN' || role === 'SUPER_ADMIN'
})

const navModules = computed(() => {
  const modules = [
    { key: 'chat', label: '消息', icon: 'chat', badge: true },
    { key: 'contacts', label: '通讯录', icon: 'people_alt', badge: false },
    { key: 'workbench', label: '工作台', icon: 'apps', badge: false },
    { key: 'todo', label: '待办', icon: 'task_alt', badge: false },
    { key: 'approval', label: '审批', icon: 'approval', badge: false },
    { key: 'calendar', label: '日历', icon: 'calendar_today', badge: false },
    { key: 'drive', label: '云文档', icon: 'cloud_queue', badge: false },
    { key: 'assistant', label: 'AI助手', icon: 'smart_toy', badge: false }
  ]
  // 只有管理员可见管理入口
  if (isAdmin.value) {
    modules.push({ key: 'admin', label: '管理', icon: 'admin_panel_settings', badge: false })
  }
  return modules
})
</script>

<style scoped lang="scss">
// 设计方向：现代、专业、精致
// 主色调：#1677ff（钉钉蓝）
// 悬停/激活态增强视觉反馈

.premium-nav-bar {
  width: 64px;
  height: 100%;
  background: linear-gradient(180deg, #1a88ff 0%, #1565d8 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 0;
  flex-shrink: 0;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

// Logo区域
.nav-brand-cell {
  margin-bottom: 16px;
  .logo-text {
    width: 40px;
    height: 40px;
    background: rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(8px);
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-weight: 700;
    font-size: 16px;
    letter-spacing: 1px;
    border: 1px solid rgba(255, 255, 255, 0.15);
  }
}

// 导航菜单
.nav-menu-list {
  flex: 1;
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 2px;
  align-items: center;
  overflow-y: auto;

  .nav-icon-cell {
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 4px 0;
    cursor: pointer;
    transition: all 0.2s;
    position: relative;

    .icon-frame {
      width: 48px;
      height: 48px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 12px;
      position: relative;
      transition: all 0.2s;
      background: transparent;

      span {
        color: #fff;
        font-size: 26px;
        font-weight: 400 !important;
      }

      .red-dot {
        position: absolute;
        top: 4px;
        right: 4px;
        width: 10px;
        height: 10px;
        background: #fff;
        border-radius: 50%;
        animation: pulse 2s infinite;
      }
    }

    .icon-label {
      font-size: 11px;
      color: #fff;
      margin-top: 2px;
      font-weight: 500;
    }

    &:hover {
      .icon-frame {
        background: rgba(255, 255, 255, 0.2);
        span { color: #fff; }
      }
      .icon-label { color: #fff; }
    }

    &.active {
      .icon-frame {
        background: #fff;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
        span { color: #1677ff; }
        .red-dot { background: #1677ff; animation: none; }
      }
      .icon-label { color: #fff; font-weight: 600; }
    }
  }
}

// 底部功能区
.nav-footer-cell {
  margin-top: auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  width: 100%;
  padding: 8px 0;
  border-top: 1px solid rgba(255, 255, 255, 0.1);

  .footer-btn {
    color: #fff;
    cursor: pointer;
    padding: 6px;
    border-radius: 10px;
    transition: all 0.2s;

    &:hover {
      color: #fff;
      background: rgba(255, 255, 255, 0.2);
    }

    .material-icons-outlined {
      font-size: 24px;
    }
  }

  .user-avatar-trigger {
    cursor: pointer;
    margin-top: 6px;
    transition: all 0.25s ease;
    overflow: hidden;
    border-radius: 8px;

    &:hover {
      transform: scale(1.05);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.25);
    }
  }
}

// 呼吸动画
@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.1); }
}
</style>