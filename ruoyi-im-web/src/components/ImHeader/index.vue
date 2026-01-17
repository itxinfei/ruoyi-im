<template>
  <header class="ding-header">
    <div class="header-left">
      <div class="header-logo">
        <el-icon :size="24" color="#0089FF">
          <ChatDotRound />
        </el-icon>
      </div>
      <div class="header-divider"></div>
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
      <el-badge :value="5" :max="99" class="header-badge">
        <el-tooltip content="通知" placement="bottom">
          <el-button :icon="Bell" text class="header-action-btn" />
        </el-tooltip>
      </el-badge>

      <el-tooltip content="发起聊天" placement="bottom">
        <el-button :icon="Plus" text class="header-action-btn" />
      </el-tooltip>

      <el-dropdown trigger="click" placement="bottom-end">
        <div class="header-action-wrapper">
          <el-tooltip content="设置" placement="bottom">
            <el-button :icon="Setting" text class="header-action-btn" />
          </el-tooltip>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item>主题切换</el-dropdown-item>
            <el-dropdown-item>消息通知</el-dropdown-item>
            <el-dropdown-item>隐私与安全</el-dropdown-item>
            <el-dropdown-item divided>关于系统</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <el-dropdown trigger="click" placement="bottom-end">
        <div class="header-user">
          <el-avatar :size="32" :src="currentUser?.avatar">
            {{ currentUser?.name?.charAt(0) }}
          </el-avatar>
          <span class="header-username">{{ currentUser?.name }}</span>
          <el-icon class="dropdown-arrow">
            <ArrowDown />
          </el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item>个人设置</el-dropdown-item>
            <el-dropdown-item>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup>
import { ref } from 'vue'
import {
  Search,
  Bell,
  Plus,
  Setting,
  ArrowDown,
  ChatDotRound
} from '@element-plus/icons-vue'

const searchKeyword = ref('')
const currentUser = ref({
  id: 1,
  name: '测试用户',
  avatar: 'https://via.placeholder.com/40'
})
</script>

<style scoped>
.ding-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  padding: 0 24px;
  background-color: #ffffff;
  border-bottom: 1px solid #e8e8e8;
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
  background-color: #f0f0f0;
}

.header-search {
  width: 320px;
  max-width: 600px;
}

.global-search-input {
  :deep(.el-input__wrapper) {
    border-radius: 8px;
    background-color: #f5f5f5;
    box-shadow: none;
    border: 1px solid transparent;

    &:hover {
      background-color: #f0f0f0;
      border-color: #d9d9d9;
    }

    &.is-focus {
      background-color: #ffffff;
      border-color: #0089ff;
      box-shadow: 0 0 0 2px rgba(0, 137, 255, 0.1);
    }
  }

  :deep(.el-input__inner) {
    font-size: 14px;
    color: #262626;
  }

  .search-shortcut {
    font-size: 12px;
    color: #8c8c8c;
    background: rgba(0, 0, 0, 0.04);
    padding: 2px 6px;
    border-radius: 4px;
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
  border-radius: 8px;
  transition: all 0.2s ease;

  &:hover {
    background-color: rgba(0, 0, 0, 0.04);
  }

  :deep(.el-icon) {
    font-size: 18px;
    color: #8c8c8c;
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
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: rgba(0, 0, 0, 0.04);
  }
}

.header-username {
  font-size: 14px;
  font-weight: 500;
  color: #262626;
}

.dropdown-arrow {
  font-size: 12px;
  color: #8c8c8c;
}
</style>
