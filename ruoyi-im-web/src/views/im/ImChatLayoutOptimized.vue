<template>
  <div class="web-im-layout">
    <!-- 顶部导航栏 -->
    <header class="header">
      <!-- Logo和品牌 -->
      <div class="header-brand">
        <div class="logo">
          <el-icon :size="24" color="#1677ff">
            <ChatLineSquare />
          </el-icon>
        </div>
        <span class="brand-name">企业IM</span>
      </div>

      <!-- 中间：搜索框或模块标题 -->
      <div class="header-center">
        <el-input
          v-if="activeModule === 'chat'"
          v-model="sessionSearch"
          placeholder="搜索会话、联系人..."
          :prefix-icon="Search"
          class="search-input"
          clearable
        />
        <div v-else class="module-title">
          {{ getModuleTitle(activeModule) }}
        </div>
      </div>

      <!-- 右侧：用户操作 -->
      <div class="header-right">
        <!-- 快捷操作 -->
        <div class="quick-actions">
          <el-tooltip content="通知" placement="bottom">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="badge-item">
              <el-button :icon="Bell" text />
            </el-badge>
          </el-tooltip>
          <el-tooltip content="设置" placement="bottom">
            <el-button :icon="Setting" text @click="showSettings" />
          </el-tooltip>
        </div>

        <!-- 用户信息 -->
        <el-dropdown @command="handleUserCommand" trigger="click">
          <div class="user-dropdown">
            <el-avatar :size="32" :src="currentUser?.avatar">
              {{ currentUser?.name?.charAt(0) || 'U' }}
            </el-avatar>
            <span class="user-name">{{ currentUser?.name || '用户' }}</span>
            <el-icon :size="12"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                个人资料
              </el-dropdown-item>
              <el-dropdown-item command="settings">
                <el-icon><Setting /></el-icon>
                系统设置
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

    <!-- 主体内容区 -->
    <div class="main-body">
      <!-- 左侧导航栏 -->
      <aside class="nav-sidebar" :class="{ collapsed: isNavCollapsed }">
        <nav class="nav-list">
          <div
            v-for="item in navModules"
            :key="item.key"
            class="nav-item"
            :class="{ active: activeModule === item.key }"
            @click="switchModule(item.key)"
          >
            <el-icon class="nav-icon">
              <component :is="item.icon" />
            </el-icon>
            <span v-if="!isNavCollapsed" class="nav-label">{{ item.label }}</span>
            <el-badge
              v-if="item.key === 'chat' && unreadCount > 0 && !isNavCollapsed"
              :value="unreadCount"
              class="nav-badge"
            />
          </div>
        </nav>

        <!-- 折叠按钮 -->
        <div class="collapse-trigger" @click="toggleNavCollapse">
          <el-icon>
            <component :is="isNavCollapsed ? ArrowRight : ArrowLeft" />
          </el-icon>
        </div>
      </aside>

      <!-- 内容工作区 -->
      <main class="workspace">
        <!-- 消息模块 -->
        <div v-if="activeModule === 'chat'" class="chat-workspace">
          <!-- 会话列表 -->
          <div class="session-panel">
            <div class="session-list">
              <div
                v-for="session in sessions"
                :key="session.id"
                class="session-item"
                :class="{ active: currentSessionId === session.id }"
                @click="selectSession(session)"
              >
                <el-badge :value="session.unreadCount" :hidden="session.unreadCount === 0">
                  <el-avatar :size="40" :src="session.avatar">
                    {{ session.name?.charAt(0) || 'U' }}
                  </el-avatar>
                </el-badge>
                <div class="session-info">
                  <div class="session-top">
                    <span class="session-name">{{ session.name }}</span>
                    <span class="session-time">{{ formatTime(session.lastMessageTime) }}</span>
                  </div>
                  <div class="session-preview">{{ session.lastMessage || '暂无消息' }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 聊天内容 -->
          <div class="chat-panel">
            <div v-if="currentSessionId" class="chat-container">
              <!-- 聊天头部 -->
              <div class="chat-header">
                <div class="chat-title">
                  <el-avatar :size="36" :src="currentSession?.avatar">
                    {{ currentSession?.name?.charAt(0) || 'U' }}
                  </el-avatar>
                  <div>
                    <div class="title-name">{{ currentSession?.name }}</div>
                    <div class="title-status">在线</div>
                  </div>
                </div>
                <div class="chat-actions">
                  <el-button :icon="Phone" circle size="small" />
                  <el-button :icon="VideoCamera" circle size="small" />
                  <el-button :icon="More" circle size="small" />
                </div>
              </div>

              <!-- 消息区 -->
              <div class="message-area">
                <div v-for="msg in messages" :key="msg.id" class="message-item" :class="{ isOwn: msg.isOwn }">
                  <el-avatar v-if="!msg.isOwn" :size="36" :src="msg.senderAvatar">
                    {{ msg.senderName?.charAt(0) || 'U' }}
                  </el-avatar>
                  <div class="message-content">
                    <div v-if="!msg.isOwn" class="sender-name">{{ msg.senderName }}</div>
                    <div class="message-bubble">{{ msg.content }}</div>
                    <div class="message-time">{{ formatTime(msg.time) }}</div>
                  </div>
                </div>
              </div>

              <!-- 输入区 -->
              <div class="input-area">
                <div class="input-toolbar">
                  <el-tooltip content="文件" placement="top">
                    <el-button :icon="Folder" text />
                  </el-tooltip>
                  <el-tooltip content="图片" placement="top">
                    <el-button :icon="PictureFilled" text />
                  </el-tooltip>
                  <el-tooltip content="语音" placement="top">
                    <el-button :icon="Microphone" text />
                  </el-tooltip>
                  <el-tooltip content="表情" placement="top">
                    <el-button :icon="ChatDotRound" text />
                  </el-tooltip>
                </div>
                <el-input
                  v-model="inputMessage"
                  type="textarea"
                  :rows="2"
                  :autosize="{ minRows: 2, maxRows: 6 }"
                  placeholder="输入消息... (Enter发送，Shift+Enter换行)"
                  @keydown.enter.prevent="handleEnter"
                />
                <div class="input-footer">
                  <span class="input-tip">按 Enter 发送</span>
                  <el-button type="primary" size="small" @click="sendMessage">发送</el-button>
                </div>
              </div>
            </div>
            <div v-else class="empty-state">
              <el-icon :size="64" color="#ddd"><ChatLineSquare /></el-icon>
              <p>选择一个会话开始聊天</p>
            </div>
          </div>
        </div>

        <!-- 通讯录模块 -->
        <div v-else-if="activeModule === 'contacts'" class="contacts-workspace">
          <div class="contacts-sidebar">
            <div class="section-title">企业组织架构</div>
            <div class="tree-list">
              <div v-for="dept in departments" :key="dept.id" class="tree-item">
                <el-icon><Folder /></el-icon>
                {{ dept.name }}
              </div>
            </div>
          </div>
          <div class="contacts-content">
            <p>联系人列表内容...</p>
          </div>
        </div>

        <!-- 工作台模块 -->
        <div v-else-if="activeModule === 'workbench'" class="workbench-workspace">
          <div class="app-grid">
            <div
              v-for="app in workbenchApps"
              :key="app.key"
              class="app-card"
              @click="openApp(app.key)"
            >
              <div class="app-icon" :style="{ background: app.color }">
                <el-icon :size="28" color="white">
                  <component :is="app.icon" />
                </el-icon>
              </div>
              <span class="app-name">{{ app.name }}</span>
            </div>
          </div>
        </div>

        <!-- 钉盘模块 -->
        <div v-else-if="activeModule === 'drive'" class="drive-workspace">
          <div class="drive-sidebar">
            <div class="drive-nav-item active">个人文件</div>
            <div class="drive-nav-item">企业共享</div>
            <div class="drive-nav-item">部门文件</div>
          </div>
          <div class="drive-content">
            <p>文件列表内容...</p>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  ChatLineSquare,
  User,
  Grid,
  Folder,
  ArrowLeft,
  ArrowRight,
  ArrowDown,
  Phone,
  VideoCamera,
  PictureFilled,
  Microphone,
  ChatDotRound,
  Document,
  Calendar,
  Notification,
  Odometer,
  Edit,
  Setting,
  Files,
  Bell,
  More,
  SwitchButton
} from '@element-plus/icons-vue'
import { formatTime as formatTimeUtil } from '@/utils/format/time'

const router = useRouter()
const route = useRoute()

// 状态
const isNavCollapsed = ref(false)
const activeModule = ref('chat')
const sessionSearch = ref('')
const currentSessionId = ref(null)
const inputMessage = ref('')
const unreadCount = ref(3)

// 当前用户
const currentUser = ref({ name: '测试用户', avatar: null })

// 导航模块
const navModules = ref([
  { key: 'chat', label: '消息', icon: ChatLineSquare },
  { key: 'contacts', label: '联系人', icon: User },
  { key: 'workbench', label: '工作台', icon: Grid },
  { key: 'drive', label: '钉盘', icon: Folder }
])

// 会话数据
const sessions = ref([
  { id: 1, name: '项目讨论组', avatar: null, lastMessage: '明天的会议时间确定了吗？', lastMessageTime: Date.now() - 3600000, unreadCount: 2 },
  { id: 2, name: '张三', avatar: null, lastMessage: '好的，收到了', lastMessageTime: Date.now() - 7200000, unreadCount: 1 },
  { id: 3, name: '李四', avatar: null, lastMessage: '文件已发送', lastMessageTime: Date.now() - 86400000, unreadCount: 0 },
  { id: 4, name: '产品群', avatar: null, lastMessage: '新版本已发布', lastMessageTime: Date.now() - 172800000, unreadCount: 0 },
])

// 当前会话
const currentSession = computed(() => {
  return sessions.value.find(s => s.id === currentSessionId.value)
})

// 消息数据
const messages = ref([
  { id: 1, senderId: 2, senderName: '张三', content: '你好，在吗？', time: Date.now() - 3600000, isOwn: false },
  { id: 2, senderId: 1, senderName: '我', content: '在的，有什么事？', time: Date.now() - 3000000, isOwn: true },
  { id: 3, senderId: 2, senderName: '张三', content: '明天下午开会，讨论新项目需求', time: Date.now() - 1800000, isOwn: false },
])

// 部门数据
const departments = ref([
  { id: 1, name: '技术部' },
  { id: 2, name: '产品部' },
  { id: 3, name: '市场部' },
  { id: 4, name: '人事部' },
])

// 工作台应用
const workbenchApps = ref([
  { key: 'approval', name: '审批', icon: Document, color: '#1677ff' },
  { key: 'attendance', name: '考勤打卡', icon: Odometer, color: '#52c41a' },
  { key: 'drive', name: '钉盘', icon: Files, color: '#faad14' },
  { key: 'calendar', name: '日程', icon: Calendar, color: '#722ed1' },
  { key: 'announcement', name: '公告', icon: Notification, color: '#eb2f96' },
  { key: 'meeting', name: '会议', icon: VideoCamera, color: '#13c2c2' },
  { key: 'mail', name: '邮件', icon: Edit, color: '#fa8c16' },
  { key: 'report', name: '汇报', icon: Document, color: '#1890ff' },
  { key: 'hr', name: '人事管理', icon: User, color: '#f5222d' },
  { key: 'finance', name: '财务', icon: Document, color: '#52c41a' },
])

// 方法
const getModuleTitle = (key) => {
  const titles = {
    chat: '消息',
    contacts: '联系人',
    workbench: '工作台',
    drive: '钉盘'
  }
  return titles[key] || ''
}

const toggleNavCollapse = () => {
  isNavCollapsed.value = !isNavCollapsed.value
  localStorage.setItem('navCollapsed', String(isNavCollapsed.value))
}

const switchModule = (moduleKey) => {
  activeModule.value = moduleKey
  // 更新URL
  router.push(`/im/${moduleKey}`)
}

const selectSession = (session) => {
  currentSessionId.value = session.id
  // 清除未读
  session.unreadCount = 0
  updateUnreadCount()
}

const updateUnreadCount = () => {
  unreadCount.value = sessions.value.reduce((sum, s) => sum + (s.unreadCount || 0), 0)
}

const handleEnter = (e) => {
  if (!e.shiftKey) {
    sendMessage()
  }
}

const sendMessage = () => {
  if (!inputMessage.value.trim() || !currentSessionId.value) return

  messages.value.push({
    id: Date.now(),
    senderId: 1,
    senderName: '我',
    content: inputMessage.value,
    time: Date.now(),
    isOwn: true,
  })
  inputMessage.value = ''
}

const formatTime = formatTimeUtil

const openApp = (appKey) => {
  ElMessage.info(`打开应用：${appKey}`)
}

const showSettings = () => {
  ElMessage.info('打开设置')
}

const handleUserCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      ElMessage.success('已退出登录')
      router.push('/login')
    } catch {
      // 用户取消
    }
  } else if (command === 'profile') {
    ElMessage.info('个人资料')
  } else if (command === 'settings') {
    showSettings()
  }
}

// 初始化
const init = () => {
  // 读取折叠状态
  const savedCollapsed = localStorage.getItem('navCollapsed')
  if (savedCollapsed !== null) {
    isNavCollapsed.value = savedCollapsed === 'true'
  }
  updateUnreadCount()
}

init()
</script>

<style lang="scss" scoped>
// Web IM 布局变量
$nav-width: 180px;
$nav-width-collapsed: 56px;
$header-height: 56px;
$session-panel-width: 280px;
$primary-color: #1677ff;
$success-color: #52c41a;
$warning-color: #faad14;
$danger-color: #ff4d4f;
$bg-gray: #f5f5f5;
$bg-hover: #e6f7ff;
$border-color: #e8e8e8;
$text-primary: #262626;
$text-secondary: #595959;
$text-tertiary: #8c8c8c;
$text-light: #bfbfbf;

// 滚动条样式
@mixin web-scrollbar {
  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  &::-webkit-scrollbar-thumb {
    background: #d9d9d9;
    border-radius: 3px;
    &:hover {
      background: #bfbfbf;
    }
  }
}

.web-im-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #fff;
  overflow: hidden;

  // 顶部导航栏
  .header {
    height: $header-height;
    min-height: $header-height;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    background: #fff;
    border-bottom: 1px solid $border-color;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
    z-index: 100;

    .header-brand {
      display: flex;
      align-items: center;
      gap: 10px;
      min-width: 140px;

      .logo {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 36px;
        height: 36px;
        background: linear-gradient(135deg, #1677ff 0%, #69b1ff 100%);
        border-radius: 8px;
      }

      .brand-name {
        font-size: 18px;
        font-weight: 600;
        color: $text-primary;
        letter-spacing: 0.5px;
      }
    }

    .header-center {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      max-width: 500px;

      .search-input {
        width: 100%;
        max-width: 400px;

        :deep(.el-input__wrapper) {
          border-radius: 20px;
          background: $bg-gray;
          border: 1px solid transparent;
          box-shadow: none;
          transition: all 0.2s;

          &:hover {
            background: #fafafa;
            border-color: #d9d9d9;
          }

          &.is-focus {
            background: #fff;
            border-color: $primary-color;
            box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.1);
          }
        }
      }

      .module-title {
        font-size: 16px;
        font-weight: 500;
        color: $text-primary;
      }
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 8px;

      .quick-actions {
        display: flex;
        align-items: center;
        gap: 4px;

        .el-button {
          color: $text-secondary;
          padding: 8px;

          &:hover {
            color: $primary-color;
            background: $bg-hover;
          }
        }

        .badge-item {
          :deep(.el-badge__content) {
            transform: translateY(-50%) translateX(50%);
          }
        }
      }

      .user-dropdown {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 6px 12px;
        border-radius: 20px;
        cursor: pointer;
        transition: all 0.2s;

        &:hover {
          background: $bg-gray;
        }

        .el-avatar {
          background: linear-gradient(135deg, #1677ff 0%, #69b1ff 100%);
        }

        .user-name {
          font-size: 14px;
          color: $text-primary;
          max-width: 80px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }
  }

  // 主体区域
  .main-body {
    flex: 1;
    display: flex;
    overflow: hidden;

    // 左侧导航栏
    .nav-sidebar {
      width: $nav-width;
      background: #fff;
      border-right: 1px solid $border-color;
      display: flex;
      flex-direction: column;
      transition: width 0.2s ease;
      position: relative;

      &.collapsed {
        width: $nav-width-collapsed;

        .nav-label,
        .nav-badge {
          display: none;
        }

        .nav-item {
          justify-content: center;
          padding: 12px 0;
        }
      }

      .nav-list {
        flex: 1;
        padding: 12px 8px;

        .nav-item {
          display: flex;
          align-items: center;
          padding: 10px 12px;
          margin-bottom: 4px;
          cursor: pointer;
          color: $text-secondary;
          border-radius: 6px;
          transition: all 0.2s;
          position: relative;

          .nav-icon {
            font-size: 20px;
            margin-right: 10px;
          }

          .nav-label {
            flex: 1;
            font-size: 14px;
          }

          .nav-badge {
            :deep(.el-badge__content) {
              transform: translateY(-50%) translateX(0);
            }
          }

          &:hover {
            background: $bg-gray;
            color: $text-primary;
          }

          &.active {
            background: $bg-hover;
            color: $primary-color;
            font-weight: 500;
          }
        }
      }

      .collapse-trigger {
        height: 48px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        color: $text-tertiary;
        border-top: 1px solid $border-color;
        transition: all 0.2s;

        &:hover {
          background: $bg-gray;
          color: $text-primary;
        }
      }
    }

    // 工作区
    .workspace {
      flex: 1;
      display: flex;
      overflow: hidden;
      background: #fafafa;

      // 聊天工作区
      .chat-workspace {
        display: flex;
        width: 100%;

        .session-panel {
          width: $session-panel-width;
          background: #fff;
          border-right: 1px solid $border-color;

          .session-list {
            height: 100%;
            overflow-y: auto;
            @include web-scrollbar;

            .session-item {
              display: flex;
              align-items: center;
              padding: 12px 16px;
              cursor: pointer;
              border-bottom: 1px solid #f0f0f0;
              transition: all 0.15s;

              &:hover {
                background: $bg-gray;
              }

              &.active {
                background: $bg-hover;
              }

              .el-badge {
                margin-right: 12px;
                flex-shrink: 0;
              }

              .session-info {
                flex: 1;
                min-width: 0;

                .session-top {
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                  margin-bottom: 4px;

                  .session-name {
                    font-size: 14px;
                    font-weight: 500;
                    color: $text-primary;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                  }

                  .session-time {
                    font-size: 12px;
                    color: $text-tertiary;
                    flex-shrink: 0;
                  }
                }

                .session-preview {
                  font-size: 12px;
                  color: $text-tertiary;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                }
              }
            }
          }
        }

        .chat-panel {
          flex: 1;
          display: flex;
          flex-direction: column;
          background: #fff;

          .chat-container {
            flex: 1;
            display: flex;
            flex-direction: column;
            overflow: hidden;

            .chat-header {
              height: 60px;
              padding: 0 20px;
              display: flex;
              align-items: center;
              justify-content: space-between;
              border-bottom: 1px solid $border-color;

              .chat-title {
                display: flex;
                align-items: center;
                gap: 12px;

                .title-name {
                  font-size: 16px;
                  font-weight: 500;
                  color: $text-primary;
                }

                .title-status {
                  font-size: 12px;
                  color: $success-color;
                }
              }

              .chat-actions {
                display: flex;
                gap: 8px;

                .el-button {
                  --el-button-border-color: transparent;
                  --el-button-bg-color: transparent;
                  --el-button-text-color: $text-secondary;
                  --el-button-hover-text-color: $primary-color;
                  --el-button-hover-bg-color: $bg-hover;
                }
              }
            }

            .message-area {
              flex: 1;
              padding: 20px;
              overflow-y: auto;
              @include web-scrollbar;

              .message-item {
                display: flex;
                margin-bottom: 20px;

                &.isOwn {
                  flex-direction: row-reverse;

                  .message-content {
                    align-items: flex-end;

                    .message-bubble {
                      background: $primary-color;
                      color: #fff;
                    }

                    .message-time {
                      text-align: right;
                    }
                  }
                }

                .message-content {
                  max-width: 60%;

                  .sender-name {
                    font-size: 12px;
                    color: $text-tertiary;
                    margin-bottom: 6px;
                  }

                  .message-bubble {
                    padding: 10px 14px;
                    border-radius: 8px;
                    background: $bg-gray;
                    color: $text-primary;
                    font-size: 14px;
                    line-height: 1.5;
                    word-break: break-word;
                  }

                  .message-time {
                    font-size: 12px;
                    color: $text-tertiary;
                    margin-top: 6px;
                  }
                }
              }
            }

            .input-area {
              padding: 16px 20px;
              border-top: 1px solid $border-color;

              .input-toolbar {
                display: flex;
                gap: 4px;
                margin-bottom: 12px;

                .el-button {
                  --el-button-border-color: transparent;
                  --el-button-bg-color: transparent;
                  --el-button-text-color: $text-secondary;
                  padding: 8px;

                  &:hover {
                    --el-button-text-color: $primary-color;
                    --el-button-hover-bg-color: $bg-hover;
                  }
                }
              }

              .input-footer {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-top: 8px;

                .input-tip {
                  font-size: 12px;
                  color: $text-tertiary;
                }
              }
            }
          }

          .empty-state {
            flex: 1;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            color: $text-light;

            p {
              margin-top: 16px;
              font-size: 14px;
            }
          }
        }
      }

      // 通讯录工作区
      .contacts-workspace {
        display: flex;
        width: 100%;

        .contacts-sidebar {
          width: 220px;
          background: #fff;
          border-right: 1px solid $border-color;
          padding: 16px 0;

          .section-title {
            padding: 0 16px 8px;
            font-size: 12px;
            color: $text-tertiary;
            font-weight: 500;
          }

          .tree-list {
            .tree-item {
              display: flex;
              align-items: center;
              gap: 8px;
              padding: 8px 16px;
              cursor: pointer;
              font-size: 14px;
              color: $text-primary;

              &:hover {
                background: $bg-gray;
              }

              .el-icon {
                color: $warning-color;
              }
            }
          }
        }

        .contacts-content {
          flex: 1;
          padding: 20px;
        }
      }

      // 工作台工作区
      .workbench-workspace {
        flex: 1;
        padding: 24px;
        overflow-y: auto;
        @include web-scrollbar;

        .app-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
          gap: 20px;

          .app-card {
            display: flex;
            flex-direction: column;
            align-items: center;
            padding: 20px;
            background: #fff;
            border-radius: 8px;
            cursor: pointer;
            transition: all 0.2s;
            box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);

            &:hover {
              transform: translateY(-2px);
              box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            }

            .app-icon {
              width: 56px;
              height: 56px;
              border-radius: 12px;
              display: flex;
              align-items: center;
              justify-content: center;
              margin-bottom: 12px;
            }

            .app-name {
              font-size: 14px;
              color: $text-primary;
              text-align: center;
            }
          }
        }
      }

      // 钉盘工作区
      .drive-workspace {
        display: flex;
        width: 100%;

        .drive-sidebar {
          width: 180px;
          background: #fff;
          border-right: 1px solid $border-color;
          padding: 16px 0;

          .drive-nav-item {
            padding: 10px 16px;
            cursor: pointer;
            font-size: 14px;
            color: $text-primary;

            &:hover {
              background: $bg-gray;
            }

            &.active {
              background: $bg-hover;
              color: $primary-color;
              font-weight: 500;
            }
          }
        }

        .drive-content {
          flex: 1;
          padding: 20px;
        }
      }
    }
  }
}

// 响应式适配
@media (max-width: 1024px) {
  .web-im-layout {
    .header {
      .header-brand {
        min-width: auto;

        .brand-name {
          display: none;
        }
      }
    }

    .main-body {
      .workspace {
        .chat-workspace {
          .session-panel {
            width: 240px;
          }
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .web-im-layout {
    .header {
      padding: 0 12px;

      .header-center {
        .search-input {
          max-width: 200px;
        }
      }

      .header-right {
        .user-dropdown {
          .user-name {
            display: none;
          }
        }
      }
    }

    .main-body {
      .nav-sidebar {
        &.collapsed {
          width: 48px;
        }
      }

      .workspace {
        .chat-workspace {
          flex-direction: column;

          .session-panel {
            width: 100%;
            height: 40%;
            border-right: none;
            border-bottom: 1px solid $border-color;
          }

          .chat-panel {
            height: 60%;
          }
        }

        .contacts-workspace {
          .contacts-sidebar {
            width: 160px;
          }
        }

        .workbench-workspace {
          .app-grid {
            grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
            gap: 12px;
            padding: 16px;

            .app-card {
              padding: 16px;

              .app-icon {
                width: 48px;
                height: 48px;
              }

              .app-name {
                font-size: 12px;
              }
            }
          }
        }
      }
    }
  }
}
</style>
