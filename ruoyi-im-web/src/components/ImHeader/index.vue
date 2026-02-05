<template>
  <header class="ding-header">
    <div class="header-left">
      <div class="header-logo">
        <el-icon
          :size="24"
          color="#0089FF"
        >
          <ChatDotRound />
        </el-icon>
      </div>
      <div class="header-divider" />
      <div class="header-search">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索联系人、群聊、消息..."
          :prefix-icon="Search"
          class="global-search-input"
          clearable
        >
          <template #suffix>
            <span class="search-shortcut">⌘K</span>
          </template>
        </el-input>
      </div>
    </div>

    <div class="header-right">
      <el-badge
        :value="5"
        :max="99"
        class="header-badge"
      >
        <el-tooltip
          content="通知"
          placement="bottom"
        >
          <el-button
            :icon="Bell"
            text
            class="header-action-btn"
          />
        </el-tooltip>
      </el-badge>

      <el-tooltip
        content="发起聊天"
        placement="bottom"
      >
        <el-button
          :icon="Plus"
          text
          class="header-action-btn"
        />
      </el-tooltip>

      <el-dropdown
        trigger="click"
        placement="bottom-end"
      >
        <div class="header-action-wrapper">
          <el-tooltip
            content="设置"
            placement="bottom"
          >
            <el-button
              :icon="Setting"
              text
              class="header-action-btn"
            />
          </el-tooltip>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item>主题切换</el-dropdown-item>
            <el-dropdown-item>消息通知</el-dropdown-item>
            <el-dropdown-item>隐私与安全</el-dropdown-item>
            <el-dropdown-item divided>
              关于系统
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <el-dropdown
        trigger="click"
        placement="bottom-end"
        @command="handleCommand"
      >
        <div class="header-user">
          <DingtalkAvatar 
            :src="currentUserAvatar" 
            :name="currentUser?.name" 
            :size="32" 
            shape="square"
            custom-class="user-info-avatar"
          />
          <span class="header-username">{{ currentUser?.name }}</span>
          <el-icon class="dropdown-arrow">
            <ArrowDown />
          </el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              个人设置
            </el-dropdown-item>
            <el-dropdown-item
              command="logout"
              divided
            >
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { clearAuth, getStoredUserInfo } from '@/utils/storage'

import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Search,
  Bell,
  Plus,
  Setting,
  ArrowDown,
  ChatDotRound
} from '@element-plus/icons-vue'
import { logout } from '@/api/im/auth'
import { confirm, messageSuccess } from '@/utils/ui'
import { addTokenToUrl } from '@/utils/file'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const router = useRouter()
const searchKeyword = ref('')
const currentUser = ref({
  id: null,
  name: '用户',
  avatar: ''
})

// 当前用户头像（带 token）
const currentUserAvatar = computed(() => {
  if (!currentUser.value.avatar) {return ''}
  return addTokenToUrl(currentUser.value.avatar)
})

// 从 localStorage 加载用户信息
const loadUserInfo = () => {
  const userInfo = getStoredUserInfo()
  if (userInfo?.id || userInfo?.userId) {
    currentUser.value = {
      id: userInfo.userId || userInfo.id,
      name: userInfo.userName || userInfo.name || '用户',
      avatar: userInfo.avatar || ''
    }
  } else {
    console.error('未找到用户信息')
  }
}

// 处理登出
const handleLogout = async () => {
  try {
    if (!await confirm('确定要退出登录吗？', '提示')) {
      return
    }

    // 调用登出接口
    try {
      await logout()
    } catch (error) {
      console.error('登出接口调用失败:', error)
      // 即使接口失败也继续清除本地数据
    }

    // 清除本地存储
    clearAuth()

    messageSuccess('已退出登录')

    // 跳转到登录页
    router.push('/login')
  } catch (error) {
    // 用户取消登出
    if (error !== 'cancel') {
      console.error('登出失败:', error)
    }
  }
}

// 处理下拉菜单点击
const handleCommand = command => {
  switch (command) {
    case 'profile':
      // 跳转到个人设置页面
      break
    case 'logout':
      handleLogout()
      break
    default:
      break
  }
}

// 组件挂载时加载用户信息
onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.ding-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  padding: 0 24px;
  background-color: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-color);
}

.header-left {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
}

.header-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
}

.header-divider {
  width: 1px;
  height: 24px;
  margin: 0 20px;
  background-color: var(--dt-border-light);
}

.header-search {
  width: 320px;
  max-width: 600px;
}

.global-search-input {
  :deep(.el-input__wrapper) {
    border-radius: var(--dt-radius-md);
    background-color: var(--dt-bg-body);
    box-shadow: none;
    border: 1px solid transparent;

    &:hover {
      background-color: var(--dt-bg-session-hover);
      border-color: var(--dt-border-input);
    }

    &.is-focus {
      background-color: var(--dt-bg-card);
      border-color: var(--dt-brand-color);
      box-shadow: 0 0 0 2px var(--dt-brand-lighter);
    }
  }

  :deep(.el-input__inner) {
    font-size: 14px;
    color: var(--dt-text-primary);
  }

  .search-shortcut {
    font-size: 12px;
    color: var(--dt-text-tertiary);
    background: var(--dt-bg-session-hover);
    padding: 2px 6px;
    border-radius: var(--dt-radius-sm);
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-badge {
  margin-right: 4px;
}

.header-action-btn {
  width: 36px;
  height: 36px;
  border-radius: var(--dt-radius-md);
  transition: all 0.2s ease;

  &:hover {
    background-color: var(--dt-bg-session-hover);
  }

  :deep(.el-icon) {
    font-size: 18px;
    color: var(--dt-text-tertiary);
  }
}

.header-action-wrapper {
  display: flex;
  align-items: center;
}

.header-user {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px 4px 8px;
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: var(--dt-bg-session-hover);
  }
}

.header-username {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
}

.dropdown-arrow {
  font-size: 12px;
  color: var(--dt-text-tertiary);
}
</style>
