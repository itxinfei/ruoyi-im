<template>
  <div class="chat-container">
    <!-- 侧边栏 -->
    <div
      class="sidebar"
      :class="{ collapsed: isSidebarCollapsed, 'mobile-open': isMobileSidebarOpen }"
    >
      <!-- 用户信息 -->
      <div class="user-info">
        <el-avatar :src="currentUser.avatar" :size="40"></el-avatar>
        <div class="user-details" :class="{ hidden: isSidebarCollapsed }">
          <span class="username">{{ currentUser.nickname }}</span>
          <span class="user-status" :class="{ online: wsConnected }">
            {{ wsConnected ? '在线' : '离线' }}
          </span>
        </div>
        <div class="user-actions" :class="{ hidden: isSidebarCollapsed }">
          <el-tooltip content="发起会话" placement="bottom">
            <el-button :icon="Plus" circle size="small" @click="showNewSessionDialog"></el-button>
          </el-tooltip>
        </div>
      </div>

      <!-- 搜索框 -->
      <div class="session-search" :class="{ hidden: isSidebarCollapsed }">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索会话..."
          :prefix-icon="Search"
          clearable
          @input="handleSearchInput"
        />
      </div>

      <!-- 会话列表 -->
      <session-list
        :is-collapsed="isSidebarCollapsed"
        @select="handleSessionSelect"
      />
    </div>

    <!-- 主聊天区域 -->
    <div class="chat-main">
      <!-- 移动端头部 -->
      <div v-if="isMobile" class="mobile-header">
        <el-button :icon="Menu" circle @click="toggleMobileSidebar"></el-button>
        <span v-if="currentSession" class="mobile-title">{{ currentSession.name }}</span>
      </div>

      <!-- 聊天头部 -->
      <div v-if="currentSession" class="chat-header">
        <div class="session-info">
          <el-avatar :src="currentSession.avatar" :size="36"></el-avatar>
          <div class="session-details">
            <span class="session-name">{{ currentSession.name }}</span>
            <span v-if="currentSession.type === 'group'" class="member-count">
              {{ currentSession.memberCount || currentSession.members?.length || 0 }} 人
            </span>
            <span v-else class="online-status" :class="{ online: isTargetOnline }">
              {{ isTargetOnline ? '在线' : '离线' }}
            </span>
          </div>
        </div>
        <div class="header-actions">
          <el-tooltip content="语音通话" placement="bottom">
            <el-button :icon="Phone" circle @click="startCall('voice')"></el-button>
          </el-tooltip>
          <el-tooltip content="视频通话" placement="bottom">
            <el-button :icon="VideoCamera" circle @click="startCall('video')"></el-button>
          </el-tooltip>
          <el-popover placement="bottom-end" :width="180" trigger="click">
            <div class="more-actions">
              <div class="action-item" @click="searchMessages">
                <el-icon><Search /></el-icon>
                <span>搜索聊天记录</span>
              </div>
              <div class="action-item" @click="showSessionSettings">
                <el-icon><Setting /></el-icon>
                <span>会话设置</span>
              </div>
              <div class="action-item danger" @click="clearMessages">
                <el-icon><Delete /></el-icon>
                <span>清空聊天记录</span>
              </div>
            </div>
            <template #reference>
              <el-button :icon="More" circle></el-button>
            </template>
          </el-popover>
        </div>
      </div>

      <!-- 消息列表 -->
      <message-list
        v-if="currentSession"
        :session-id="currentSession.id"
        :is-group="currentSession.type === 'group'"
        @context-menu="handleMessageContextMenu"
      />

      <!-- 输入区域 -->
      <chat-input
        v-if="currentSession"
        :session-id="currentSession.id"
        :session-members="currentSession.members || []"
        :reply-message="replyMessage"
        @send-message="handleSendMessage"
        @send-file="handleSendFile"
        @send-image="handleSendImage"
        @send-voice="handleSendVoice"
        @start-call="startCall"
        @cancel-reply="replyMessage = null"
      />

      <!-- 空状态 -->
      <div v-if="!currentSession" class="no-session">
        <div class="welcome-container">
          <div class="welcome-icon">
            <el-icon :size="64" color="#c0c0c0"><ChatDotRound /></el-icon>
          </div>
          <h2>欢迎使用即时通讯</h2>
          <p>选择一个会话开始聊天，或创建新会话</p>
          <el-button type="primary" @click="showNewSessionDialog">
            <el-icon><Plus /></el-icon>
            <span>发起新会话</span>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 移动端侧边栏遮罩 -->
    <div
      class="sidebar-overlay"
      :class="{ active: isMobileSidebarOpen }"
      @click="toggleMobileSidebar"
    ></div>

    <!-- 消息右键菜单 -->
    <teleport to="body">
      <transition name="context-menu-fade">
        <div
          v-if="contextMenu.visible"
          class="context-menu"
          :style="{ top: contextMenu.y + 'px', left: contextMenu.x + 'px' }"
          @click.stop
        >
          <div class="menu-item" @click="handleCopyMessage">
            <el-icon><DocumentCopy /></el-icon>
            <span>复制</span>
          </div>
          <div class="menu-item" @click="handleReplyMessage">
            <el-icon><ChatLineSquare /></el-icon>
            <span>回复</span>
          </div>
          <div class="menu-item" @click="handleForwardMessage">
            <el-icon><Share /></el-icon>
            <span>转发</span>
          </div>
          <div v-if="canRecallMessage" class="menu-item" @click="handleRecallMessage">
            <el-icon><RefreshLeft /></el-icon>
            <span>撤回</span>
          </div>
          <div class="menu-item danger" @click="handleDeleteMessage">
            <el-icon><Delete /></el-icon>
            <span>删除</span>
          </div>
        </div>
      </transition>
    </teleport>

    <!-- 新建会话对话框 -->
    <el-dialog
      v-model="newSessionDialogVisible"
      title="发起新会话"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="new-session-form">
        <el-tabs v-model="newSessionType">
          <el-tab-pane label="私聊" name="private">
            <el-input
              v-model="newSessionSearch"
              placeholder="搜索用户..."
              :prefix-icon="Search"
              clearable
            />
            <div class="user-list">
              <div
                v-for="user in filteredUsers"
                :key="user.id"
                class="user-item"
                :class="{ selected: selectedUserId === user.id }"
                @click="selectedUserId = user.id"
              >
                <el-avatar :src="user.avatar" :size="36"></el-avatar>
                <div class="user-info">
                  <span class="name">{{ user.nickname }}</span>
                  <span class="dept">{{ user.deptName }}</span>
                </div>
                <el-icon v-if="selectedUserId === user.id" class="check-icon"><Check /></el-icon>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="群聊" name="group">
            <el-form label-width="80px">
              <el-form-item label="群名称">
                <el-input v-model="newGroupName" placeholder="请输入群名称"></el-input>
              </el-form-item>
              <el-form-item label="群成员">
                <el-select
                  v-model="selectedGroupMembers"
                  multiple
                  filterable
                  placeholder="选择群成员"
                  style="width: 100%"
                >
                  <el-option
                    v-for="user in availableUsers"
                    :key="user.id"
                    :label="user.nickname"
                    :value="user.id"
                  />
                </el-select>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
      <template #footer>
        <el-button @click="newSessionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createNewSession" :loading="creatingSession">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Menu, Phone, VideoCamera, More, Setting, Delete,
  DocumentCopy, ChatLineSquare, Share, RefreshLeft, Check, ChatDotRound
} from '@element-plus/icons-vue'
import SessionList from '@/components/Chat/SessionList.vue'
import MessageList from '@/components/Chat/MessageList.vue'
import ChatInput from '@/components/Chat/ChatInput.vue'
import { getUserInfo } from '@/api/user'
import { listUser } from '@/api/system/user'

const store = useStore()

// 响应式状态
const isSidebarCollapsed = ref(false)
const isMobile = ref(false)
const isMobileSidebarOpen = ref(false)
const searchKeyword = ref('')
const replyMessage = ref(null)

// 新建会话相关
const newSessionDialogVisible = ref(false)
const newSessionType = ref('private')
const newSessionSearch = ref('')
const selectedUserId = ref(null)
const newGroupName = ref('')
const selectedGroupMembers = ref([])
const creatingSession = ref(false)
const availableUsers = ref([])

// 右键菜单
const contextMenu = ref({
  visible: false,
  x: 0,
  y: 0,
  message: null,
})

// 计算属性
const currentUser = computed(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return {
    id: userInfo.userId,
    nickname: userInfo.nickName || userInfo.userName || '用户',
    avatar: userInfo.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
  }
})

const currentSession = computed(() => store.state.im?.currentSession)
const wsConnected = computed(() => store.state.im?.wsConnected ?? true)

const isTargetOnline = computed(() => {
  if (!currentSession.value || currentSession.value.type === 'group') return false
  return store.state.im?.onlineStatus?.[currentSession.value.targetUserId] ?? false
})

const canRecallMessage = computed(() => {
  if (!contextMenu.value.message) return false
  const message = contextMenu.value.message
  // 只能撤回自己发送的消息，且在2分钟内
  const isOwnMessage = message.senderId === currentUser.value.id
  const withinTime = Date.now() - (message.timestamp || 0) < 2 * 60 * 1000
  return isOwnMessage && withinTime
})

const filteredUsers = computed(() => {
  if (!newSessionSearch.value) return availableUsers.value
  const keyword = newSessionSearch.value.toLowerCase()
  return availableUsers.value.filter(
    user => user.nickname?.toLowerCase().includes(keyword) ||
            user.userName?.toLowerCase().includes(keyword)
  )
})

// 处理窗口大小变化
const handleResize = () => {
  const width = window.innerWidth
  isMobile.value = width < 768
  if (width < 768) {
    isSidebarCollapsed.value = true
  } else if (width < 1024) {
    isSidebarCollapsed.value = true
  } else {
    isSidebarCollapsed.value = false
  }
}

// 切换移动端侧边栏
const toggleMobileSidebar = () => {
  isMobileSidebarOpen.value = !isMobileSidebarOpen.value
}

// 处理搜索输入
const handleSearchInput = () => {
  // 可以添加搜索防抖
}

// 处理会话选择
const handleSessionSelect = (session) => {
  store.dispatch('im/switchSession', session)
  if (isMobile.value) {
    isMobileSidebarOpen.value = false
  }
}

// 发送消息
const handleSendMessage = async (messageData) => {
  if (!currentSession.value) return

  try {
    await store.dispatch('im/sendMessage', {
      sessionId: currentSession.value.id,
      type: messageData.type,
      content: messageData.content,
      replyTo: messageData.replyTo,
    })
  } catch (error) {
    ElMessage.error('发送失败，请重试')
  }
}

// 发送文件
const handleSendFile = async (fileData) => {
  if (!currentSession.value) return

  try {
    await store.dispatch('im/sendMessage', {
      sessionId: currentSession.value.id,
      type: 'file',
      content: fileData,
    })
  } catch (error) {
    ElMessage.error('发送失败，请重试')
  }
}

// 发送图片
const handleSendImage = async (imageData) => {
  if (!currentSession.value) return

  try {
    await store.dispatch('im/sendMessage', {
      sessionId: currentSession.value.id,
      type: 'image',
      content: imageData.url,
    })
  } catch (error) {
    ElMessage.error('发送失败，请重试')
  }
}

// 发送语音
const handleSendVoice = async (voiceData) => {
  if (!currentSession.value) return

  try {
    await store.dispatch('im/sendMessage', {
      sessionId: currentSession.value.id,
      type: 'voice',
      content: voiceData,
    })
  } catch (error) {
    ElMessage.error('发送失败，请重试')
  }
}

// 发起通话
const startCall = (type) => {
  ElMessage.info(`${type === 'video' ? '视频' : '语音'}通话功能开发中...`)
}

// 搜索消息
const searchMessages = () => {
  ElMessage.info('搜索功能开发中...')
}

// 显示会话设置
const showSessionSettings = () => {
  ElMessage.info('会话设置开发中...')
}

// 清空聊天记录
const clearMessages = () => {
  ElMessageBox.confirm('确定要清空聊天记录吗？此操作不可恢复。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    ElMessage.success('聊天记录已清空')
  }).catch(() => {})
}

// 消息右键菜单处理
const handleMessageContextMenu = ({ event, message }) => {
  event.preventDefault()
  contextMenu.value = {
    visible: true,
    x: event.clientX,
    y: event.clientY,
    message,
  }
}

// 关闭右键菜单
const closeContextMenu = () => {
  contextMenu.value.visible = false
}

// 复制消息
const handleCopyMessage = () => {
  const message = contextMenu.value.message
  if (message?.type === 'text') {
    navigator.clipboard.writeText(message.content).then(() => {
      ElMessage.success('已复制到剪贴板')
    })
  }
  closeContextMenu()
}

// 回复消息
const handleReplyMessage = () => {
  replyMessage.value = contextMenu.value.message
  closeContextMenu()
}

// 转发消息
const handleForwardMessage = () => {
  ElMessage.info('转发功能开发中...')
  closeContextMenu()
}

// 撤回消息
const handleRecallMessage = () => {
  ElMessage.info('撤回功能开发中...')
  closeContextMenu()
}

// 删除消息
const handleDeleteMessage = () => {
  ElMessageBox.confirm('确定要删除这条消息吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    ElMessage.success('消息已删除')
  }).catch(() => {})
  closeContextMenu()
}

// 显示新建会话对话框
const showNewSessionDialog = async () => {
  newSessionDialogVisible.value = true
  try {
    const response = await listUser({ pageSize: 100 })
    availableUsers.value = (response.rows || []).map(u => ({
      id: u.userId,
      nickname: u.nickName,
      userName: u.userName,
      avatar: u.avatar,
      deptName: u.dept?.deptName,
    }))
  } catch (error) {
    console.error('加载用户列表失败:', error)
  }
}

// 创建新会话
const createNewSession = async () => {
  if (newSessionType.value === 'private') {
    if (!selectedUserId.value) {
      ElMessage.warning('请选择聊天对象')
      return
    }
  } else {
    if (!newGroupName.value.trim()) {
      ElMessage.warning('请输入群名称')
      return
    }
    if (selectedGroupMembers.value.length < 2) {
      ElMessage.warning('群成员至少需要2人')
      return
    }
  }

  creatingSession.value = true
  try {
    // TODO: 调用创建会话API
    ElMessage.success('会话创建成功')
    newSessionDialogVisible.value = false
    // 重置表单
    selectedUserId.value = null
    newGroupName.value = ''
    selectedGroupMembers.value = []
  } catch (error) {
    ElMessage.error('创建失败，请重试')
  } finally {
    creatingSession.value = false
  }
}

// 生命周期
onMounted(() => {
  handleResize()
  window.addEventListener('resize', handleResize)
  document.addEventListener('click', closeContextMenu)

  // 加载会话列表
  store.dispatch('im/loadSessions').catch(error => {
    console.error('加载会话列表失败:', error)
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  document.removeEventListener('click', closeContextMenu)
})
</script>

<style scoped lang="scss">
.chat-container {
  display: flex;
  height: calc(100vh - 60px);
  background-color: #f5f5f5;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  position: relative;

  .sidebar {
    width: 300px;
    background-color: #fff;
    display: flex;
    flex-direction: column;
    border-right: 1px solid #e8e8e8;
    transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    flex-shrink: 0;

    &.collapsed {
      width: 64px;

      .user-details.hidden,
      .session-search.hidden,
      .user-actions.hidden {
        display: none;
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      padding: 20px 16px;
      border-bottom: 1px solid #f0f0f0;
      background: linear-gradient(180deg, #fafafa 0%, #fff 100%);

      .user-details {
        flex: 1;
        margin-left: 14px;

        .username {
          display: block;
          font-weight: 600;
          font-size: 15px;
          color: #1a1a1a;
          margin-bottom: 4px;
        }

        .user-status {
          font-size: 12px;
          color: #999;
          display: flex;
          align-items: center;

          &::before {
            content: '';
            display: inline-block;
            width: 6px;
            height: 6px;
            background-color: #999;
            border-radius: 50%;
            margin-right: 6px;
          }

          &.online {
            color: #52c41a;

            &::before {
              background-color: #52c41a;
            }
          }
        }
      }

      .user-actions {
        :deep(.el-button) {
          border: none;
          background-color: #f5f5f5;
          transition: all 0.2s ease;

          &:hover {
            background-color: #1890ff;
            color: #fff;
          }
        }
      }
    }

    .session-search {
      padding: 14px 16px;
      border-bottom: 1px solid #f0f0f0;

      :deep(.el-input__wrapper) {
        background-color: #f5f5f5;
        border-radius: 20px;
        box-shadow: none;

        &:focus-within {
          background-color: #fff;
          box-shadow: 0 0 0 1px #1890ff;
        }
      }
    }
  }

  .chat-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    background-color: #fafafa;
    min-width: 0;

    .mobile-header {
      display: none;
      align-items: center;
      padding: 14px 16px;
      background-color: #fff;
      border-bottom: 1px solid #f0f0f0;
      gap: 12px;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);

      .mobile-title {
        font-weight: 600;
        font-size: 15px;
        color: #1a1a1a;
      }
    }

    .chat-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 16px 20px;
      background-color: #fff;
      border-bottom: 1px solid #f0f0f0;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);

      .session-info {
        display: flex;
        align-items: center;

        .session-details {
          margin-left: 14px;

          .session-name {
            font-weight: 600;
            font-size: 15px;
            color: #1a1a1a;
          }

          .member-count,
          .online-status {
            font-size: 12px;
            color: #999;
            margin-left: 8px;

            &.online {
              color: #52c41a;
            }
          }
        }
      }

      .header-actions {
        display: flex;
        align-items: center;

        :deep(.el-button) {
          margin-left: 6px;
          border: none;
          background-color: #f5f5f5;
          transition: all 0.2s ease;

          &:hover {
            background-color: #1890ff;
            color: #fff;
          }
        }
      }
    }

    .no-session {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #fafafa;

      .welcome-container {
        text-align: center;
        color: #999;

        h2 {
          color: #666;
          margin: 16px 0 12px;
        }

        p {
          margin-bottom: 24px;
        }
      }
    }
  }

  .sidebar-overlay {
    display: none;
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    z-index: 1000;
    opacity: 0;
    transition: opacity 0.3s ease;

    &.active {
      opacity: 1;
    }
  }
}

.more-actions {
  .action-item {
    display: flex;
    align-items: center;
    padding: 10px 12px;
    cursor: pointer;
    border-radius: 6px;
    transition: all 0.2s ease;

    .el-icon {
      margin-right: 10px;
      font-size: 16px;
    }

    &:hover {
      background-color: #f5f5f5;
    }

    &.danger {
      color: #ff4d4f;

      &:hover {
        background-color: #fff1f0;
      }
    }
  }
}

.context-menu {
  position: fixed;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  padding: 6px 0;
  min-width: 140px;
  z-index: 3000;

  .menu-item {
    display: flex;
    align-items: center;
    padding: 10px 16px;
    cursor: pointer;
    transition: all 0.2s ease;

    .el-icon {
      margin-right: 10px;
      font-size: 16px;
    }

    &:hover {
      background-color: #f5f5f5;
    }

    &.danger {
      color: #ff4d4f;

      &:hover {
        background-color: #fff1f0;
      }
    }
  }
}

.context-menu-fade-enter-active,
.context-menu-fade-leave-active {
  transition: all 0.2s ease;
}

.context-menu-fade-enter-from,
.context-menu-fade-leave-to {
  opacity: 0;
  transform: scale(0.9);
}

.new-session-form {
  .user-list {
    max-height: 300px;
    overflow-y: auto;
    margin-top: 16px;

    .user-item {
      display: flex;
      align-items: center;
      padding: 12px;
      cursor: pointer;
      border-radius: 8px;
      transition: all 0.2s ease;

      &:hover {
        background-color: #f5f5f5;
      }

      &.selected {
        background-color: #e6f7ff;
      }

      .user-info {
        flex: 1;
        margin-left: 12px;

        .name {
          display: block;
          font-weight: 500;
          color: #1a1a1a;
        }

        .dept {
          font-size: 12px;
          color: #999;
        }
      }

      .check-icon {
        color: #1890ff;
        font-size: 18px;
      }
    }
  }
}

@media (max-width: 768px) {
  .chat-container {
    height: calc(100vh - 50px);
    border-radius: 0;

    .sidebar {
      position: fixed;
      left: 0;
      top: 50px;
      bottom: 0;
      width: 280px;
      z-index: 1001;
      transform: translateX(-100%);
      transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);

      &.mobile-open {
        transform: translateX(0);
      }
    }

    .chat-main {
      .mobile-header {
        display: flex;
      }
    }

    .sidebar-overlay {
      display: block;
    }
  }
}
</style>
