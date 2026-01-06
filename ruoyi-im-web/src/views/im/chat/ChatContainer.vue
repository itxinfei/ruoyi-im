<template>
  <div class="chat-content">
    <template v-if="currentSession">
      <!-- 聊天头部 -->
      <div class="chat-header">
        <div class="header-left">
          <el-avatar :size="36" :src="currentSession.avatar || defaultAvatar" class="header-avatar">
            {{ currentSession.name?.charAt(0) || 'U' }}
          </el-avatar>
          <div class="header-info">
            <span class="chat-title">{{ currentSession.name }}</span>
            <span v-if="currentSession.type === 'private'" class="chat-status">
              {{ currentSession.status === 'online' ? '在线' : '离线' }}
            </span>
          </div>
        </div>

        <div class="header-right">
          <el-button :icon="Phone" circle size="small" @click="startCall('voice')" />
          <el-button :icon="VideoCamera" circle size="small" @click="startCall('video')" />
          <el-button :icon="Search" circle size="small" @click="showSearchDialog" />
          <el-button :icon="More" circle size="small" @click="showChatActions" />
        </div>
      </div>

      <!-- 消息列表 -->
      <div ref="messageListRef" class="message-list" @scroll="handleScroll">
        <div v-for="message in displayedMessages" :key="message.id" class="message-wrapper">
          <!-- 消息时间分组 -->
          <div v-if="shouldShowTime(message)" class="message-time">
            {{ formatTime(message.time) }}
          </div>

          <!-- 消息气泡 -->
          <div
            class="message-bubble"
            :class="{
              'message-own': message.isOwn,
              'message-group': currentSession.type === 'group',
              'message-read': message.read,
            }"
          >
            <!-- 群聊消息：显示发送者头像和姓名 -->
            <template v-if="currentSession.type === 'group' && !message.isOwn">
              <el-avatar
                :size="32"
                :src="message.sender?.avatar || defaultAvatar"
                class="sender-avatar"
                @click="showSenderInfo(message.sender)"
              />
              <span class="sender-name" @click="showSenderInfo(message.sender)">
                {{ message.sender?.name || message.senderName }}
              </span>
            </template>

            <!-- 消息内容 -->
            <div class="message-content">
              <!-- 文本消息 -->
              <div
                v-if="message.type === 'text'"
                class="text-content"
                @click="handleTextClick(message)"
              >
                <span v-html="formatMessageContent(message.content)"></span>
              </div>

              <!-- 图片消息 -->
              <div v-else-if="message.type === 'image'" class="image-content">
                <el-image
                  :src="message.content"
                  :preview-src-list="[message.content]"
                  preview-teleported
                  class="message-image"
                  fit="cover"
                />
              </div>

              <!-- 文件消息 -->
              <div v-else-if="message.type === 'file'" class="file-content">
                <div class="file-icon">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="file-info">
                  <div class="file-name">{{ message.fileName }}</div>
                  <div class="file-size">{{ formatFileSize(message.fileSize) }}</div>
                </div>
                <el-button
                  class="file-download-btn"
                  type="primary"
                  size="small"
                  @click="downloadFile(message)"
                >
                  下载
                </el-button>
              </div>

              <!-- 其他类型消息 -->
              <div v-else class="other-content">[{{ message.type }}消息]</div>
            </div>

            <!-- 消息状态 -->
            <div class="message-status">
              <el-icon v-if="message.status === 'sending'" class="status-icon sending">
                <Loading />
              </el-icon>
              <el-icon v-else-if="message.status === 'sent'" class="status-icon sent">
                <SuccessFilled />
              </el-icon>
              <el-icon v-else-if="message.status === 'read'" class="status-icon read">
                <SuccessFilled />
              </el-icon>
            </div>
          </div>
        </div>

        <div v-if="loadingMore" class="loading-more">
          <el-icon><Loading /></el-icon>
          <span>加载中...</span>
        </div>
      </div>

      <!-- 聊天输入区域 -->
      <div class="chat-input-area">
        <div class="chat-input-toolbar">
          <el-button :icon="Plus" circle size="small" @click="showQuickActions" />
          <el-button :icon="EditPen" circle size="small" @click="sendImage" />
          <el-button :icon="VideoPlay" circle size="small" @click="sendFile" />
          <el-button :icon="Location" circle size="small" @click="sendLocation" />
          <el-button :icon="Microphone" circle size="small" @click="startVoiceMessage" />
        </div>
        <div class="chat-input-container">
          <el-input
            v-model="inputMessage"
            type="textarea"
            class="message-input"
            :rows="2"
            :autosize="{ minRows: 2, maxRows: 6 }"
            placeholder="输入消息..."
            maxlength="2000"
            show-word-limit
            @keydown.enter="handleEnterPress"
          />
          <div class="chat-input-actions">
            <div class="input-hints">
              <div class="hint-item" @click="toggleEmojiPanel">
                <el-icon><ChatDotRound /></el-icon>
                <span>表情</span>
              </div>
              <div class="hint-item" @click="toggleRichText">
                <el-icon><Ticket /></el-icon>
                <span>富文本</span>
              </div>
            </div>
            <div class="send-actions">
              <el-button @click="clearInput">清空</el-button>
              <el-button type="primary" class="send-btn" @click="sendMessage">发送</el-button>
            </div>
          </div>
        </div>
      </div>
    </template>
    <template v-else>
      <!-- 未选择会话时的空状态 -->
      <div class="empty-chat-state">
        <div class="empty-content">
          <div class="empty-icon">
            <svg width="120" height="120" viewBox="0 0 120 120">
              <path
                fill="#e0e0e0"
                d="M60 10C32.4 10 10 32.4 10 60s22.4 50 50 50 50-22.4 50-50S87.6 10 60 10zm0 85c-13.8 0-25-11.2-25-25s11.2-25 25-25 25 11.2 25 25-11.2 25-25 25z"
              />
              <circle cx="45" cy="55" r="8" fill="#e0e0e0" />
              <circle cx="75" cy="55" r="8" fill="#e0e0e0" />
              <path fill="#e0e0e0" d="M60 85c-10.5 0-19-7.2-19-16h38c0 8.8-8.5 16-19 16z" />
            </svg>
          </div>
          <p>选择一个会话开始聊天</p>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Plus,
  More,
  Phone,
  VideoCamera,
  Loading,
  SuccessFilled,
  ChatLineSquare,
  User,
  Menu,
  Folder,
  Setting,
  ArrowLeft,
  EditPen,
  Document,
  VideoPlay,
  Location,
  Bell,
  ChatDotRound,
  Ticket,
  Microphone,
} from '@element-plus/icons-vue'
import { formatTime as formatTimeUtil } from '@/utils/format/time'
import { formatFileSize as formatFileSizeUtil } from '@/utils/format/file'

const store = useStore()

// 状态
const inputMessage = ref('')
const currentSession = ref(null)
const loadingMore = ref(false)
const hasMoreMessages = ref(true)

// 状态
const sessions = ref([])
const displayedMessages = ref([])
const messageListRef = ref(null)

// 默认头像
const defaultAvatar = ref('/static/avatar-default.png')

// 计算属性来获取当前会话
const currentSessionComputed = computed(() => {
  // 尝试从store获取，如果失败则返回本地状态
  const storeSession = store.state.im?.currentSession
  return storeSession || currentSession.value
})

// 监听当前会话变化，自动滚动到底部
watch(
  currentSessionComputed,
  async () => {
    await nextTick()
    scrollToBottom()
  },
  { immediate: false }
)

// 初始化会话数据
const initializeSession = () => {
  // 模拟会话数据
  displayedMessages.value = [
    {
      id: 1,
      content: '你好！有什么可以帮助你的吗？',
      type: 'text',
      time: Date.now() - 3600000,
      isOwn: false,
      sender: { name: '测试联系人' },
    },
    {
      id: 2,
      content: '你好，我想了解一下项目进度',
      type: 'text',
      time: Date.now() - 1800000,
      isOwn: true,
    },
    {
      id: 3,
      content: '项目进展顺利，预计下周可以完成第一阶段',
      type: 'text',
      time: Date.now() - 600000,
      isOwn: false,
      sender: { name: '测试联系人' },
    },
  ]
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

// 格式化时间 - 使用工具函数
const formatTime = formatTimeUtil

// 是否显示时间（每5分钟显示一次）
const shouldShowTime = (message, index) => {
  if (index === 0) return true

  const prevMessage = displayedMessages.value[index - 1]
  if (!prevMessage) return true

  const timeDiff = Math.abs(new Date(message.time) - new Date(prevMessage.time))
  return timeDiff > 5 * 60 * 1000 // 5分钟
}

// 发送消息
const sendMessage = () => {
  if (!inputMessage.value.trim() || !currentSessionComputed.value) return

  const newMessage = {
    id: Date.now(),
    content: inputMessage.value,
    type: 'text',
    time: Date.now(),
    isOwn: true,
  }

  displayedMessages.value.push(newMessage)
  inputMessage.value = ''

  scrollToBottom()
}

// 处理回车键
const handleEnterPress = event => {
  if (event.shiftKey) return
  event.preventDefault()
  sendMessage()
}

// 清空输入
const clearInput = () => {
  inputMessage.value = ''
}

// 其他方法
const startCall = type => {
  ElMessage.info(`${type === 'voice' ? '语音' : '视频'}通话功能开发中...`)
}

const showSearchDialog = () => {
  ElMessage.info('消息搜索功能开发中...')
}

const showChatActions = () => {
  ElMessage.info('聊天操作功能开发中...')
}

const sendImage = () => {
  ElMessage.info('发送图片功能开发中...')
}

const sendFile = () => {
  ElMessage.info('发送文件功能开发中...')
}

const sendLocation = () => {
  ElMessage.info('发送位置功能开发中...')
}

const showQuickActions = () => {
  ElMessage.info('快捷操作功能开发中...')
}

const downloadFile = file => {
  ElMessage.info('下载功能开发中...')
}

// 格式化文件大小 - 使用工具函数
const formatFileSize = formatFileSizeUtil

// 格式化消息内容（支持链接、@等）
const formatMessageContent = content => {
  if (!content) return ''

  // 转义HTML
  let escaped = content
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#x27;')

  // 匹配URL链接
  escaped = escaped.replace(
    /https?:\/\/(www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)/g,
    '<a href="$&" target="_blank" class="message-link">$&</a>'
  )

  return escaped
}

// 处理文本点击
const handleTextClick = message => {
  // 可以在这里处理点击消息的逻辑，如回复、转发等
}

// 显示发送者信息
const showSenderInfo = sender => {
  if (!sender) return
  ElMessage.info(`用户信息: ${sender.name}`)
}

// 处理滚动事件（用于加载更多消息）
const handleScroll = () => {
  if (!messageListRef.value || !hasMoreMessages.value) return

  const { scrollTop } = messageListRef.value
  if (scrollTop <= 20 && !loadingMore.value) {
    loadMoreMessages()
  }
}

// 加载更多消息
const loadMoreMessages = async () => {
  if (!currentSessionComputed.value || !hasMoreMessages.value || loadingMore.value) return

  loadingMore.value = true
  try {
    // 模拟加载更多消息
    const moreMessages = [
      {
        id: Date.now() + 1,
        content: '这是更早之前的消息',
        type: 'text',
        time: Date.now() - 86400000, // 1天前
        isOwn: false,
        sender: { name: '测试联系人' },
      },
    ]
    displayedMessages.value.unshift(...moreMessages)
  } catch (error) {
    console.error('加载更多消息失败:', error)
  } finally {
    loadingMore.value = false
  }
}

// 切换表情面板
const toggleEmojiPanel = () => {
  ElMessage.info('表情功能开发中...')
}

// 切换富文本
const toggleRichText = () => {
  ElMessage.info('富文本功能开发中...')
}

// 开始录制语音消息
const startVoiceMessage = () => {
  ElMessage.info('语音消息功能开发中...')
}

onMounted(() => {
  initializeSession()
  scrollToBottom()
})
</script>

<style lang="scss" scoped>
@use '@/styles/dingtalk-theme.scss' as *;

.chat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  background: $chat-bg;

  .chat-header {
    height: 56px;
    min-height: 56px;
    padding: 0 $spacing-lg;
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1px solid $border-light;
    background: $bg-white;
    flex-shrink: 0;

    .header-left {
      display: flex;
      align-items: center;
      gap: $spacing-md;

      .header-avatar {
        flex-shrink: 0;
      }

      .header-info {
        .chat-title {
          font-size: $font-size-lg;
          font-weight: $font-weight-semibold;
          color: $text-primary;
          display: block;
        }

        .chat-status {
          font-size: $font-size-sm;
          color: $success-color;
          display: block;
        }
      }
    }

    .header-right {
      display: flex;
      gap: $spacing-xs;

      .el-button {
        border: none;
        background: transparent;
        padding: 6px;

        &:hover {
          background: $bg-hover;
        }
      }
    }
  }

  .message-list {
    flex: 1;
    overflow-y: auto;
    padding: $spacing-lg $spacing-xl;
    background: $chat-bg;

    @include custom-scrollbar(6px, $border-base);

    .message-wrapper {
      margin-bottom: $spacing-lg;

      .message-time {
        text-align: center;
        font-size: $font-size-sm;
        color: $text-tertiary;
        margin: $spacing-md 0;
      }

      .message-bubble {
        display: flex;
        gap: $spacing-sm;
        max-width: 65%;
        align-items: flex-end;

        &.message-own {
          margin-left: auto;
          flex-direction: row-reverse;

          .message-content {
            background: #d0e7ff;
            color: #000;
            border-radius: $border-radius-lg $border-radius-xs $border-radius-lg $border-radius-lg;

            .message-link {
              color: #006eff;
              text-decoration: underline;
            }
          }

          .message-status {
            order: -1;
            margin-right: $spacing-sm;
            margin-left: 0;
          }
        }

        &.message-group {
          .sender-avatar {
            cursor: pointer;
            flex-shrink: 0;
            width: 32px;
            height: 32px;
          }

          .sender-name {
            font-size: $font-size-sm;
            color: #4a7fee;
            cursor: pointer;
            margin-bottom: $spacing-xs;
            padding: 0 $spacing-sm;
            font-weight: $font-weight-medium;

            &:hover {
              text-decoration: underline;
            }
          }
        }

        .sender-avatar {
          cursor: pointer;
          flex-shrink: 0;
          width: 36px;
          height: 36px;
        }

        .sender-name {
          font-size: $font-size-sm;
          color: $text-secondary;
          cursor: pointer;
          margin-bottom: $spacing-xs;
          padding: 0 $spacing-sm;

          &:hover {
            text-decoration: underline;
          }
        }

        .message-content {
          padding: $spacing-sm $spacing-md;
          background: $bg-white;
          border: 1px solid $border-light;
          border-radius: $border-radius-xs $border-radius-lg $border-radius-lg $border-radius-lg;
          box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
          word-wrap: break-word;
          max-width: 400px;

          .text-content {
            line-height: 1.6;
            white-space: pre-wrap;
            word-break: break-word;
            font-size: $font-size-base;

            .message-link {
              color: #006eff;
              text-decoration: underline;
              cursor: pointer;

              &:hover {
                opacity: 0.8;
              }
            }
          }

          .image-content {
            .message-image {
              max-width: 180px;
              max-height: 180px;
              border-radius: $border-radius-sm;
              cursor: pointer;
              object-fit: cover;
            }
          }

          .file-content {
            display: flex;
            align-items: center;
            gap: $spacing-md;
            padding: $spacing-md;
            background: $bg-light;
            border-radius: $border-radius-sm;

            .file-icon {
              width: 44px;
              height: 44px;
              background: #f0f5ff;
              border-radius: $border-radius-sm;
              display: flex;
              align-items: center;
              justify-content: center;
              color: #409eff;
              flex-shrink: 0;

              .el-icon {
                font-size: 20px;
              }
            }

            .file-info {
              flex: 1;
              min-width: 0;

              .file-name {
                font-weight: $font-weight-medium;
                color: $text-primary;
                margin-bottom: $spacing-xs;
                @include text-ellipsis;
                font-size: $font-size-sm;
              }

              .file-size {
                font-size: $font-size-xs;
                color: $text-tertiary;
              }
            }

            .file-download-btn {
              margin-left: $spacing-md;
              flex-shrink: 0;
            }
          }
        }

        .message-status {
          margin: 0 $spacing-sm;
          display: flex;
          align-items: center;

          .status-icon {
            font-size: 13px;
            width: 16px;
            height: 16px;

            &.sent {
              color: $text-tertiary;
            }

            &.read {
              color: #006eff;
            }

            &.sending {
              color: $text-tertiary;
              animation: spin 1s linear infinite;
            }
          }
        }
      }
    }

    .loading-more {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: $spacing-sm;
      color: $text-tertiary;
      padding: $spacing-md;
    }
  }

  .chat-input-area {
    padding: $spacing-lg;
    background: $bg-white;
    border-top: 1px solid $border-light;

    .chat-input-toolbar {
      display: flex;
      gap: $spacing-sm;
      margin-bottom: $spacing-md;
      padding: 0 $spacing-sm;

      .el-button {
        border: none;
        background: transparent;
        padding: 8px;
        border-radius: $border-radius-sm;
        color: $text-secondary;

        &:hover {
          background: $bg-hover;
          color: $primary-color;
        }

        &.primary-action {
          &:hover {
            background: $primary-color-light;
            color: $primary-color;
          }
        }
      }
    }

    .chat-input-container {
      display: flex;
      flex-direction: column;
      gap: $spacing-sm;

      .message-input {
        :deep(.el-textarea__inner) {
          border: 1px solid $border-base;
          border-radius: $border-radius-lg;
          padding: $spacing-md;
          min-height: 80px;
          max-height: 160px;
          font-size: $font-size-base;
          line-height: 1.6;

          &:focus {
            border-color: $primary-color;
            box-shadow: 0 0 0 2px rgba($primary-color, 0.2);
          }
        }
      }

      .chat-input-actions {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .input-hints {
          display: flex;
          gap: $spacing-md;
          color: $text-tertiary;
          font-size: $font-size-sm;

          .hint-item {
            display: flex;
            align-items: center;
            gap: $spacing-xs;

            &:hover {
              color: $primary-color;
              cursor: pointer;
            }
          }
        }

        .send-actions {
          display: flex;
          gap: $spacing-sm;

          .el-button {
            border-radius: $border-radius-sm;
            padding: 8px 16px;
            font-size: $font-size-sm;

            &.send-btn {
              background: $primary-color;
              color: white;
              border: none;

              &:hover {
                background: $primary-color-dark;
              }
            }
          }
        }
      }
    }
  }

  .empty-chat-state {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;

    .empty-content {
      text-align: center;

      .empty-icon {
        margin-bottom: $spacing-lg;
      }

      p {
        color: $text-tertiary;
        font-size: $font-size-base;
      }
    }
  }
}
</style>
