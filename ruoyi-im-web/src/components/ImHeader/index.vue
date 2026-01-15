<template>
  <header class="im-header">
    <!-- 左侧：Logo和搜索 -->
    <div class="header-left">
      <div class="header-logo">
        <el-icon :size="24" color="#1677ff">
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
          readonly
          @click="handleSearchClick"
          @keyup.enter="handleSearchClick"
        >
          <template #suffix>
            <span class="search-shortcut">⌘K</span>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 右侧：操作区 -->
    <div class="header-right">
      <!-- 通知按钮 -->
      <el-badge
        :value="notificationCount"
        :hidden="notificationCount === 0"
        :max="99"
        class="header-badge"
      >
        <el-tooltip content="通知" placement="bottom">
          <el-button
            :icon="Bell"
            text
            class="header-action-btn"
            @click="handleNotifications"
          />
        </el-tooltip>
      </el-badge>

      <!-- 快捷操作 -->
      <el-tooltip content="发起聊天" placement="bottom">
        <el-button
          :icon="Plus"
          text
          class="header-action-btn"
          @click="handleStartChat"
        />
      </el-tooltip>

      <!-- 主题切换 -->
      <ThemeSwitch />

      <!-- 设置 -->
      <el-dropdown trigger="click" placement="bottom-end">
        <el-button :icon="MoreFilled" text class="header-action-btn" />
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="handleProfile">
              <el-icon><User /></el-icon>
              个人设置
            </el-dropdown-item>
            <el-dropdown-item @click="handleTheme">
              <el-icon><Sunny /></el-icon>
              主题设置
            </el-dropdown-item>
            <el-dropdown-item @click="handleFeedback">
              <el-icon><Message /></el-icon>
              反馈建议
            </el-dropdown-item>
            <el-dropdown-item divided @click="handleCheckUpdate">
              <el-icon><Refresh /></el-icon>
              检查更新
              <el-tag size="small" type="success" class="version-tag">v1.0</el-tag>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <!-- 用户下拉 -->
      <el-dropdown trigger="click" placement="bottom-end" @command="handleUserCommand">
        <div class="header-user">
          <SmartAvatar
            :name="userInfo.name || '用户'"
            :avatar="userInfo.avatar || ''"
            :size="32"
            :show-status="true"
            :status="userOnlineStatus"
            class="user-avatar"
          />
          <span class="header-username">{{ userInfo.name || '用户' }}</span>
          <el-icon class="dropdown-arrow">
            <ArrowDown />
          </el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <div class="profile-item">
                <SmartAvatar
                  :name="userInfo.name || '用户'"
                  :avatar="userInfo.avatar || ''"
                  :size="36"
                  class="dropdown-avatar"
                />
                <div class="user-info-item">
                  <span class="user-name">{{ userInfo.name || '用户' }}</span>
                  <span class="user-id">ID: {{ userInfo.userId || 'unknown' }}</span>
                </div>
              </div>
            </el-dropdown-item>
            <el-dropdown-item divided command="status">
              <div class="status-item-wrapper">
                <div class="status-item-content">
                  <el-icon><CircleCheck /></el-icon>
                  <span>在线状态</span>
                </div>
                <div class="status-selector">
                  <el-tooltip content="在线" placement="top">
                    <span
                      class="status-btn"
                      :class="{ active: userOnlineStatus === 'online' }"
                      @click.stop="setOnlineStatus('online')"
                    >
                      <span class="status-dot online"></span>
                    </span>
                  </el-tooltip>
                  <el-tooltip content="离开" placement="top">
                    <span
                      class="status-btn"
                      :class="{ active: userOnlineStatus === 'away' }"
                      @click.stop="setOnlineStatus('away')"
                    >
                      <span class="status-dot away"></span>
                    </span>
                  </el-tooltip>
                  <el-tooltip content="忙碌" placement="top">
                    <span
                      class="status-btn"
                      :class="{ active: userOnlineStatus === 'busy' }"
                      @click.stop="setOnlineStatus('busy')"
                    >
                      <span class="status-dot busy"></span>
                    </span>
                  </el-tooltip>
                  <el-tooltip content="隐身" placement="top">
                    <span
                      class="status-btn"
                      :class="{ active: userOnlineStatus === 'hidden' }"
                      @click.stop="setOnlineStatus('hidden')"
                    >
                      <span class="status-dot hidden"></span>
                    </span>
                  </el-tooltip>
                </div>
                <div class="current-status-text">
                  {{ getOnlineStatusDisplay(userOnlineStatus) }}
                </div>
              </div>
            </el-dropdown-item>
            <el-dropdown-item command="settings">
              <el-icon><Setting /></el-icon>
              个人设置
            </el-dropdown-item>
            <el-dropdown-item command="theme">
              <el-icon><Sunny /></el-icon>
              主题设置
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound, Search, Bell, Plus, MoreFilled, ArrowDown,
  User, CircleCheck, Setting, Sunny, SwitchButton, Message, Refresh
} from '@element-plus/icons-vue'
import SmartAvatar from '@/components/SmartAvatar/index.vue'
import ThemeSwitch from '@/components/ThemeSwitch/index.vue'

const props = defineProps({
  userInfo: {
    type: Object,
    default: () => ({})
  },
  notificationCount: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits([
  'handle-user-command',
  'show-notifications',
  'show-start-chat-dialog',
  'toggle-theme'
])

// 搜索关键词
const searchKeyword = ref('')

// 用户在线状态
const userOnlineStatus = ref(localStorage.getItem('onlineStatus') || 'online')

// 处理搜索点击
const handleSearchClick = () => {
  // 打开全局搜索对话框
  emit('show-notifications')
}

// 处理通知点击
const handleNotifications = () => {
  emit('show-notifications')
}

// 处理发起聊天
const handleStartChat = () => {
  emit('show-start-chat-dialog')
}

// 处理用户命令
const handleUserCommand = (command) => {
  emit('handle-user-command', command)
}

// 设置在线状态
const setOnlineStatus = (status) => {
  userOnlineStatus.value = status
  localStorage.setItem('onlineStatus', status)
  
  // 更新用户状态到服务器
  ElMessage.success(`已切换到${getOnlineStatusDisplay(status)}状态`)
}

// 获取在线状态显示
const getOnlineStatusDisplay = (status) => {
  const statusMap = {
    online: '在线',
    away: '离开',
    busy: '忙碌',
    hidden: '隐身'
  }
  return statusMap[status] || '未知'
}

// 处理个人资料
const handleProfile = () => {
  emit('handle-user-command', 'profile')
}

// 处理主题设置
const handleTheme = () => {
  emit('toggle-theme')
}

// 处理反馈
const handleFeedback = () => {
  emit('handle-user-command', 'feedback')
}

// 处理检查更新
const handleCheckUpdate = () => {
  emit('handle-user-command', 'update')
}
</script>

<style scoped lang="scss">
.im-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 24px;
  background: #ffffff;
  border-bottom: 1px solid #f0f0f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  flex-shrink: 0;
  z-index: 100;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .header-logo {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      border-radius: 8px;
      background: linear-gradient(135deg, rgba(22, 119, 255, 0.1) 0%, rgba(22, 119, 255, 0.05) 100%);
      cursor: pointer;
      transition: all 0.2s ease;

      &:hover {
        background: linear-gradient(135deg, rgba(22, 119, 255, 0.15) 0%, rgba(22, 119, 255, 0.08) 100%);
      }
    }

    .header-divider {
      width: 1px;
      height: 24px;
      background: #e8e8e8;
    }

    .header-search {
      .global-search-input {
        width: 280px;
        
        :deep(.el-input__wrapper) {
          background: #f5f5f5;
          border: 1px solid transparent;
          border-radius: 8px;
          padding: 0 12px;
          transition: all 0.2s ease;
          
          &:hover {
            background: #eaeaea;
            border-color: #d9d9d9;
          }

          &:focus-within {
            background: #ffffff;
            border-color: #1677ff;
            box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.1);
          }
        }

        :deep(.el-input__inner) {
          font-size: 13px;
          color: #595959;
          background: transparent;
          border: none;
          padding: 0;
        }

        .search-shortcut {
          font-size: 11px;
          color: #8c8c8c;
          font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
        }
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 8px;

    .header-badge {
      :deep(.el-badge__content) {
        background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
        border: none;
        font-size: 11px;
        height: 18px;
        line-height: 18px;
        padding: 0 5px;
        border-radius: 9px;
        font-weight: 600;
        box-shadow: 0 2px 6px rgba(255, 77, 79, 0.3);
      }
    }

    .header-action-btn {
      --el-button-border-color: transparent;
      --el-button-bg-color: transparent;
      --el-button-text-color: #8c8c8c;
      width: 36px;
      height: 36px;
      border-radius: 8px;
      font-size: 16px;
      transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);

      &:hover {
        --el-button-text-color: #1677ff;
        --el-button-hover-bg-color: rgba(22, 119, 255, 0.06);
        transform: scale(1.05);
      }

      &:active {
        transform: scale(0.95);
      }
    }

    .header-user {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 4px 8px 4px 12px;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s ease;

      &:hover {
        background: rgba(0, 0, 0, 0.04);
      }

      .user-avatar {
        border: 2px solid #f0f0f0;
        transition: all 0.2s ease;
      }

      .header-username {
        font-size: 14px;
        font-weight: 500;
        color: #262626;
        max-width: 100px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .dropdown-arrow {
        font-size: 12px;
        color: #8c8c8c;
        transition: transform 0.2s ease;
      }

      &:hover .dropdown-arrow {
        transform: rotate(180deg);
      }
    }

    :deep(.el-dropdown-menu__item) {
      padding: 10px 16px;
      font-size: 13px;
      transition: all 0.2s ease;

      &:hover {
        background: rgba(22, 119, 255, 0.06);
        color: #1677ff;
      }

      .el-icon {
        margin-right: 8px;
        font-size: 14px;
      }

      .profile-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 4px 0;

        .dropdown-avatar {
          flex-shrink: 0;
        }

        .user-info-item {
          flex: 1;

          .user-name {
            font-size: 14px;
            font-weight: 500;
            color: #262626;
            display: block;
            margin-bottom: 2px;
          }

          .user-id {
            font-size: 12px;
            color: #8c8c8c;
          }
        }
      }

      .status-item-wrapper {
        width: 100%;

        .status-item-content {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 8px;

          .el-icon {
            font-size: 16px;
            color: #52c41a;
          }

          span {
            font-size: 13px;
            color: #595959;
          }
        }

        .status-selector {
          display: flex;
          gap: 8px;
          padding: 4px 0;
          border: 1px solid #f0f0f0;
          border-radius: 8px;
          background: #fafafa;

          .status-btn {
            width: 28px;
            height: 28px;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            transition: all 0.2s ease;
            border-radius: 4px;

            &:hover {
              background: rgba(0, 0, 0, 0.04);
            }

            &.active {
              background: rgba(22, 119, 255, 0.1);
            }

            .status-dot {
              width: 8px;
              height: 8px;
              border-radius: 50%;
              border: 2px solid #fff;

              &.online {
                background: #52c41a;
                box-shadow: 0 0 0 2px rgba(82, 196, 26, 0.2);
              }

              &.away {
                background: #faad14;
                box-shadow: 0 0 0 2px rgba(250, 173, 20, 0.2);
              }

              &.busy {
                background: #ff4d4f;
                box-shadow: 0 0 0 2px rgba(255, 77, 79, 0.2);
              }

              &.hidden {
                background: #8c8c8c;
                box-shadow: 0 0 0 2px rgba(140, 140, 140, 0.2);
              }
            }
          }
        }

        .current-status-text {
          text-align: center;
          font-size: 12px;
          color: #8c8c8c;
          font-weight: 500;
        }
      }
    }
  }
}
</style>
