<template>
  <div class="dingtalk-56-layout">
    <!-- 顶部栏：根据当前模块显示不同内容 -->
    <div class="topbar">
      <!-- 左侧：搜索框 -->
      <div class="topbar-left">
        <el-input
          v-if="activeModule === 'chat'"
          v-model="sessionSearch"
          placeholder="搜索会话"
          :prefix-icon="Search"
          class="search-input"
        />
        <div v-else-if="activeModule === 'contacts'" class="module-title">通讯录</div>
        <div v-else-if="activeModule === 'workbench'" class="module-title">工作台</div>
        <div v-else-if="activeModule === 'drive'" class="module-title">钉盘</div>
      </div>

      <!-- 右侧：用户头像和窗口控制 -->
      <div class="topbar-right">
        <el-dropdown @command="handleUserCommand" trigger="click">
          <div class="user-info">
            <el-avatar :size="32" :src="currentUser?.avatar">
              {{ currentUser?.name?.charAt(0) || 'U' }}
            </el-avatar>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="settings">系统设置</el-dropdown-item>
              <el-dropdown-item command="exit">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <div class="window-controls">
          <span class="control-btn minimize">_</span>
          <span class="control-btn maximize">□</span>
          <span class="control-btn close">×</span>
        </div>
      </div>
    </div>

    <!-- 主体内容区 -->
    <div class="main-body">
      <!-- 左侧导航栏（可折叠） -->
      <div class="nav-sidebar" :class="{ collapsed: isNavCollapsed }">
        <div class="nav-items">
          <div
            v-for="item in navModules"
            :key="item.key"
            class="nav-item"
            :class="{ active: activeModule === item.key }"
            @click="switchModule(item.key)"
            :title="item.label"
          >
            <el-icon class="nav-icon">
              <component :is="item.icon" />
            </el-icon>
            <span v-if="!isNavCollapsed" class="nav-label">{{ item.label }}</span>
          </div>
        </div>

        <!-- 折叠按钮 -->
        <div class="collapse-btn" @click="toggleNavCollapse">
          <el-icon>
            <component :is="isNavCollapsed ? ArrowRight : ArrowLeft" />
          </el-icon>
        </div>
      </div>

      <!-- 中间工作区 -->
      <div class="workspace">
        <!-- 消息模块：左侧会话列表 + 右侧聊天内容 -->
        <div v-if="activeModule === 'chat'" class="chat-workspace">
          <!-- 会话列表 -->
          <div class="session-sidebar">
            <div class="session-list">
              <div
                v-for="session in sessions"
                :key="session.id"
                class="session-item"
                :class="{ active: currentSessionId === session.id }"
                @click="selectSession(session)"
              >
                <el-badge :value="session.unreadCount" :hidden="session.unreadCount === 0" class="session-badge">
                  <el-avatar :size="40" :src="session.avatar">
                    {{ session.name?.charAt(0) || 'U' }}
                  </el-avatar>
                </el-badge>
                <div class="session-info">
                  <div class="session-name">{{ session.name }}</div>
                  <div class="session-preview">{{ session.lastMessage || '暂无消息' }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 聊天内容区 -->
          <div class="chat-content">
            <div v-if="currentSessionId" class="chat-container">
              <!-- 聊天头部 -->
              <div class="chat-header">
                <span class="chat-title">{{ currentSession?.name }}</span>
                <div class="chat-actions">
                  <el-button :icon="Phone" circle size="small" />
                  <el-button :icon="VideoCamera" circle size="small" />
                </div>
              </div>

              <!-- 消息列表 -->
              <div class="message-area">
                <div v-for="msg in messages" :key="msg.id" class="message-item" :class="{ own: msg.isOwn }">
                  <el-avatar v-if="!msg.isOwn" :size="32" :src="msg.senderAvatar">
                    {{ msg.senderName?.charAt(0) || 'U' }}
                  </el-avatar>
                  <div class="message-bubble">
                    <div v-if="!msg.isOwn" class="sender-name">{{ msg.senderName }}</div>
                    <div class="message-text">{{ msg.content }}</div>
                    <div class="message-time">{{ formatTime(msg.time) }}</div>
                  </div>
                </div>
              </div>

              <!-- 输入区 -->
              <div class="input-area">
                <div class="input-toolbar">
                  <el-button :icon="Folder" circle size="small" />
                  <el-button :icon="PictureFilled" circle size="small" />
                  <el-button :icon="Microphone" circle size="small" />
                </div>
                <el-input
                  v-model="inputMessage"
                  type="textarea"
                  :rows="2"
                  placeholder="输入消息..."
                  @keydown.enter="sendMessage"
                />
                <div class="input-footer">
                  <el-button type="primary" size="small" @click="sendMessage">发送</el-button>
                </div>
              </div>
            </div>
            <div v-else class="empty-chat">
              <p>选择一个会话开始聊天</p>
            </div>
          </div>
        </div>

        <!-- 通讯录模块 -->
        <div v-else-if="activeModule === 'contacts'" class="contacts-workspace">
          <div class="contact-sidebar">
            <div class="contact-tree">
              <div class="tree-section">
                <div class="tree-title">企业组织架构</div>
                <div v-for="dept in departments" :key="dept.id" class="tree-item">
                  {{ dept.name }}
                </div>
              </div>
              <div class="tree-section">
                <div class="tree-title">星标联系人</div>
                <div class="tree-item">暂无星标联系人</div>
              </div>
            </div>
          </div>
          <div class="contact-content">
            <p>联系人列表</p>
          </div>
        </div>

        <!-- 工作台模块：网格应用 -->
        <div v-else-if="activeModule === 'workbench'" class="workbench-workspace">
          <div class="app-grid">
            <div
              v-for="app in workbenchApps"
              :key="app.key"
              class="app-item"
              @click="openApp(app.key)"
            >
              <div class="app-icon" :style="{ background: app.color }">
                <el-icon :size="24" color="white">
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
            <div class="drive-nav">
              <div class="drive-nav-item active">个人文件</div>
              <div class="drive-nav-item">企业共享</div>
              <div class="drive-nav-item">部门文件</div>
            </div>
          </div>
          <div class="drive-content">
            <p>文件列表</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Search,
  ChatLineSquare,
  User,
  Grid,
  Folder,
  ArrowLeft,
  ArrowRight,
  Phone,
  VideoCamera,
  PictureFilled,
  Microphone,
  Document,
  Calendar,
  Notification,
  Odometer,
  Edit,
  Setting,
  Files
} from '@element-plus/icons-vue'
import { formatTime as formatTimeUtil } from '@/utils/format/time'

const router = useRouter()

// 状态
const isNavCollapsed = ref(false)
const activeModule = ref('chat')
const sessionSearch = ref('')
const currentSessionId = ref(null)
const inputMessage = ref('')

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
  { id: 1, name: '项目讨论组', avatar: null, lastMessage: '明天的会议时间确定了吗？', unreadCount: 2 },
  { id: 2, name: '张三', avatar: null, lastMessage: '好的，收到了', unreadCount: 0 },
  { id: 3, name: '李四', avatar: null, lastMessage: '文件已发送', unreadCount: 0 },
])

// 当前会话
const currentSession = computed(() => {
  return sessions.value.find(s => s.id === currentSessionId.value)
})

// 消息数据
const messages = ref([
  { id: 1, senderId: 2, senderName: '张三', content: '你好，在吗？', time: Date.now() - 3600000, isOwn: false },
  { id: 2, senderId: 1, senderName: '我', content: '在的，有什么事？', time: Date.now() - 3000000, isOwn: true },
])

// 部门数据
const departments = ref([
  { id: 1, name: '技术部' },
  { id: 2, name: '产品部' },
  { id: 3, name: '市场部' },
])

// 工作台应用
const workbenchApps = ref([
  { key: 'approval', name: '审批', icon: Document, color: '#1890ff' },
  { key: 'attendance', name: '考勤打卡', icon: Odometer, color: '#52c41a' },
  { key: 'drive', name: '钉盘', icon: Files, color: '#faad14' },
  { key: 'calendar', name: '日程', icon: Calendar, color: '#722ed1' },
  { key: 'announcement', name: '公告', icon: Notification, color: '#eb2f96' },
  { key: 'meeting', name: '会议', icon: VideoCamera, color: '#13c2c2' },
  { key: 'mail', name: '邮件', icon: Edit, color: '#fa8c16' },
  { key: 'settings', name: '企业设置', icon: Setting, color: '#595959' },
])

// 方法
const toggleNavCollapse = () => {
  isNavCollapsed.value = !isNavCollapsed.value
}

const switchModule = (moduleKey) => {
  activeModule.value = moduleKey
}

const selectSession = (session) => {
  currentSessionId.value = session.id
}

const sendMessage = () => {
  if (!inputMessage.value.trim()) return
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

const handleUserCommand = (command) => {
  if (command === 'settings') {
    ElMessage.info('系统设置')
  } else if (command === 'exit') {
    ElMessage.info('退出登录')
  }
}
</script>

<style lang="scss" scoped>
// 钉钉5.6风格变量
$nav-width: 180px;
$nav-width-collapsed: 56px;
$topbar-height: 48px;
$session-list-width: 240px;
$primary-blue: #2E7D32;  // 钉钉5.6的绿色主色调
$bg-gray: #F5F5F5;
$border-color: #E0E0E0;
$text-primary: #333333;
$text-secondary: #666666;
$text-light: #999999;

.dingtalk-56-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #fff;
  overflow: hidden;

  // 顶部栏
  .topbar {
    height: $topbar-height;
    min-height: $topbar-height;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 16px;
    background: #fff;
    border-bottom: 1px solid $border-color;

    .topbar-left {
      flex: 1;

      .search-input {
        width: 200px;

        :deep(.el-input__wrapper) {
          border-radius: 4px;
          background: $bg-gray;
          border: none;
        }
      }

      .module-title {
        font-size: 16px;
        font-weight: 500;
        color: $text-primary;
      }
    }

    .topbar-right {
      display: flex;
      align-items: center;
      gap: 12px;

      .user-info {
        cursor: pointer;

        .el-avatar {
          background: $primary-blue;
        }
      }

      .window-controls {
        display: flex;
        gap: 8px;

        .control-btn {
          width: 14px;
          height: 14px;
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          color: $text-secondary;
          font-size: 14px;

          &:hover {
            background: $bg-gray;
          }

          &.close:hover {
            background: #f56565;
            color: white;
          }
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
      background: $bg-gray;
      border-right: 1px solid $border-color;
      display: flex;
      flex-direction: column;
      transition: width 0.2s;

      &.collapsed {
        width: $nav-width-collapsed;

        .nav-label {
          display: none;
        }
      }

      .nav-items {
        flex: 1;
        padding: 8px 0;

        .nav-item {
          display: flex;
          align-items: center;
          padding: 10px 16px;
          cursor: pointer;
          color: $text-secondary;
          transition: all 0.15s;

          .nav-icon {
            font-size: 20px;
            margin-right: 10px;
          }

          .nav-label {
            font-size: 14px;
          }

          &:hover {
            background: rgba(0, 0, 0, 0.06);
          }

          &.active {
            background: rgba(0, 0, 0, 0.12);
            color: $text-primary;
            font-weight: 500;
          }
        }
      }

      .collapse-btn {
        height: 48px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        color: $text-secondary;
        border-top: 1px solid $border-color;

        &:hover {
          background: rgba(0, 0, 0, 0.06);
        }
      }
    }

    // 中间工作区
    .workspace {
      flex: 1;
      display: flex;
      overflow: hidden;

      // 聊天工作区
      .chat-workspace {
        display: flex;
        width: 100%;

        .session-sidebar {
          width: $session-list-width;
          border-right: 1px solid $border-color;
          overflow-y: auto;

          .session-list {
            .session-item {
              display: flex;
              align-items: center;
              padding: 12px 16px;
              cursor: pointer;
              border-bottom: 1px solid $border-color;

              &:hover {
                background: $bg-gray;
              }

              &.active {
                background: #E3F2FD;
              }

              .session-badge {
                margin-right: 12px;
              }

              .session-info {
                flex: 1;
                min-width: 0;

                .session-name {
                  font-size: 14px;
                  color: $text-primary;
                  margin-bottom: 4px;
                }

                .session-preview {
                  font-size: 12px;
                  color: $text-light;
                  white-space: nowrap;
                  overflow: hidden;
                  text-overflow: ellipsis;
                }
              }
            }
          }
        }

        .chat-content {
          flex: 1;
          display: flex;
          flex-direction: column;
          background: #fff;

          .chat-container {
            flex: 1;
            display: flex;
            flex-direction: column;

            .chat-header {
              height: 56px;
              padding: 0 16px;
              display: flex;
              align-items: center;
              justify-content: space-between;
              border-bottom: 1px solid $border-color;

              .chat-title {
                font-size: 16px;
                font-weight: 500;
              }

              .chat-actions {
                display: flex;
                gap: 8px;

                .el-button {
                  border: none;
                  background: transparent;
                  color: $text-secondary;

                  &:hover {
                    color: $text-primary;
                  }
                }
              }
            }

            .message-area {
              flex: 1;
              padding: 16px;
              overflow-y: auto;

              .message-item {
                display: flex;
                margin-bottom: 16px;

                &.own {
                  flex-direction: row-reverse;

                  .message-bubble {
                    background: #1890ff;
                    color: white;
                    align-items: flex-end;

                    .message-text {
                      color: white;
                    }

                    .message-time {
                      color: rgba(255, 255, 255, 0.7);
                    }
                  }
                }

                .message-bubble {
                  max-width: 60%;
                  padding: 8px 12px;
                  border-radius: 4px;
                  background: $bg-gray;
                  margin-left: 8px;

                  .sender-name {
                    font-size: 12px;
                    color: $text-secondary;
                    margin-bottom: 4px;
                  }

                  .message-text {
                    font-size: 14px;
                    color: $text-primary;
                  }

                  .message-time {
                    font-size: 12px;
                    color: $text-light;
                    margin-top: 4px;
                  }
                }
              }
            }

            .input-area {
              border-top: 1px solid $border-color;
              padding: 12px 16px;

              .input-toolbar {
                display: flex;
                gap: 8px;
                margin-bottom: 8px;

                .el-button {
                  border: none;
                  background: transparent;
                  color: $text-secondary;

                  &:hover {
                    color: $text-primary;
                  }
                }
              }

              .input-footer {
                display: flex;
                justify-content: flex-end;
                margin-top: 8px;
              }
            }
          }

          .empty-chat {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            color: $text-light;
          }
        }
      }

      // 通讯录工作区
      .contacts-workspace {
        display: flex;
        width: 100%;

        .contact-sidebar {
          width: 200px;
          border-right: 1px solid $border-color;
          padding: 12px 0;

          .tree-section {
            margin-bottom: 20px;

            .tree-title {
              font-size: 12px;
              color: $text-secondary;
              padding: 0 12px;
              margin-bottom: 8px;
            }

            .tree-item {
              padding: 8px 12px;
              cursor: pointer;
              font-size: 14px;

              &:hover {
                background: $bg-gray;
              }
            }
          }
        }

        .contact-content {
          flex: 1;
          padding: 16px;
        }
      }

      // 工作台工作区
      .workbench-workspace {
        padding: 24px;
        overflow-y: auto;

        .app-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, 100px);
          gap: 24px;

          .app-item {
            display: flex;
            flex-direction: column;
            align-items: center;
            cursor: pointer;
            padding: 16px;
            border-radius: 4px;
            transition: background 0.15s;

            &:hover {
              background: $bg-gray;
            }

            .app-icon {
              width: 48px;
              height: 48px;
              border-radius: 8px;
              display: flex;
              align-items: center;
              justify-content: center;
              margin-bottom: 8px;
            }

            .app-name {
              font-size: 12px;
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
          border-right: 1px solid $border-color;
          padding: 12px 0;

          .drive-nav-item {
            padding: 10px 16px;
            cursor: pointer;
            font-size: 14px;

            &:hover {
              background: $bg-gray;
            }

            &.active {
              background: $bg-gray;
              font-weight: 500;
            }
          }
        }

        .drive-content {
          flex: 1;
          padding: 16px;
        }
      }
    }
  }
}
</style>
