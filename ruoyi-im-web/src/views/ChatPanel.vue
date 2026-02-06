<template>
  <div class="chat-panel" :class="{ 'is-dragging': isDragging, 'is-drag-over': isDragOver }"
    @dragover.prevent="handleDragOver" @dragenter.prevent="handleDragEnter" @dragleave.prevent="handleDragLeave"
    @drop.prevent="handleDrop" @paste="handlePaste">
    <div v-if="!session" class="empty-placeholder">
      <EmptyState type="chat" title="选择一个会话开始聊天" description="从左侧列表选择联系人或群组，开始你的对话" :compact="false" />
    </div>
    <template v-else>
      <div class="main-container">
        <!-- 左侧聊天主体 -->
        <div class="chat-viewport" :class="{ 'with-pinned-panel': showPinnedPanel }">
          <ChatHeader :session="session" :typing-users="typingUsers" :pinned-count="pinnedCount"
            @show-detail="handleToggleDetail" @toggle-sidebar="handleToggleSidebar"
            @multiselect="handleToggleMultiSelect" @toggle-multi-select="handleToggleMultiSelect"
            @toggle-pinned="showPinnedPanel = !showPinnedPanel" @clear-selection="handleClearSelection"
            @announcement="showAnnouncementDialog = true" @history="handleShowHistory" @search="handleSearchMessages"
            @files="handleShowFiles" @pin="handlePinSession" @mute="handleMuteSession" @clear="handleClearMessages"
            @voice-call="handleVoiceCall" @video-call="handleVideoCall" @scroll-to-message="handleScrollToMessage" />
          <MessageList ref="msgListRef" :session-id="session?.id" :messages="messages" :loading="loading"
            :current-user="currentUser" :session-type="session?.type" :multi-select-mode="isMultiSelectMode"
            @command="handleCommand" @at="handleAt" @load-more="handleLoadMore" @show-user="handleShowUser"
            @retry="handleRetry" @remind-unread="handleRemindUnread" @long-press="handleMultiSelect"
            @preview="handleImagePreview" @re-edit="handleReEdit" />
          <MessageInput ref="messageInputRef" :session="session" :sending="sending" :replying-message="replyingMessage"
            :editing-message="editingMessage" @send="handleSend" @send-voice="handleSendVoice"
            @cancel-reply="handleCancelReply" @cancel-edit="handleCancelEdit" @edit-confirm="handleEditConfirm"
            @start-call="handleStartCall" @start-video="handleStartVideo" @upload-image="handleImageUpload"
            @upload-file="handleFileUpload" @upload-video="handleVideoUpload" @send-location="handleSendLocation"
            @send-screenshot="handleScreenshotUpload" @input="handleInput"
            @create-announcement="handleCreateAnnouncement" />
        </div>

        <!-- 置顶消息面板 -->
        <Transition name="slide-left">
          <PinnedMessagesPanel v-if="showPinnedPanel" :messages="messages" @close="showPinnedPanel = false"
            @scroll-to-message="handleScrollToPinnedMessage" @update="handlePinnedUpdate" />
        </Transition>

        <!-- 移除旧的侧边栏，改用全局弹窗 -->
      </div>

      <!-- 群组详情弹窗 -->
      <GroupDetailDialog v-model:visible="showGroupDetail" :group-id="session?.targetId" />

      <!-- 隐藏的文件上传 input -->
      <input ref="fileInputRef" type="file" style="display: none" @change="handleFileUpload">
      <input ref="imageInputRef" type="file" style="display: none" accept="image/*" @change="handleImageUpload">

      <!-- 转发对话框 -->
      <ForwardDialog ref="forwardDialogRef" @forward="handleForwardConfirm"
        @batch-forward="handleBatchForwardConfirm" />

      <!-- 语音通话 -->
      <VoiceCallDialog v-model:visible="showVoiceCall" :remote-user="remoteCallUser" :is-incoming="isIncomingCall" />

      <!-- 视频通话 -->
      <VideoCallDialog v-model:visible="showVideoCall" :remote-user="remoteCallUser" :is-incoming="isIncomingCall" />

      <!-- 聊天记录面板 -->
      <ChatHistoryPanel :visible="showChatHistory" :conversation-id="session?.id" @close="showChatHistory = false"
        @jump-to-message="handleJumpToMessage" @clear-history="handleClearHistory" />

      <!-- 群公告对话框 -->
      <GroupAnnouncementDialog v-model="showAnnouncementDialog" :group-id="session?.targetId"
        :can-manage="session?.type === 'GROUP' && session?.memberRole === 'ADMIN'" />

      <!-- 搜索聊天记录面板 -->
      <ChatSearchPanel :visible="showSearchPanel" :session-id="session?.id" :messages="messages"
        @close="showSearchPanel = false" @jump-to-message="handleJumpToMessage" />

      <!-- 聊天内搜索弹窗 -->
      <ChatSearch v-model:visible="showChatSearch" :messages="messages" @select-message="handleScrollToMessage" />

      <!-- 查看文件面板 -->
      <ChatFilesPanel :visible="showFilesPanel" :session-id="session?.id" :messages="messages"
        @close="showFilesPanel = false" @open-file="handleOpenFile" @download-file="handleDownloadFile"
        @forward-file="handleForwardFile" />

      <!-- 群文件面板 -->
      <GroupFilePanel v-if="showGroupFilesPanel && session?.type === 'GROUP'" :group-id="session?.targetId"
        class="group-files-drawer" />

      <!-- 合并消息详情对话框 -->
      <CombineDetailDialog v-model="showCombineDetail" :messages="combineMessages"
        :conversation-title="combineConversationTitle" @forward="handleCombineForwardDetail" />

      <!-- 导出聊天记录对话框 -->
      <ExportChatDialog v-model="showExportDialog" :messages="messages"
        :contact-name="session?.peerName || session?.groupName || '聊天'" />

      <!-- 多选操作栏 -->
      <MultiSelectToolbar :active="isMultiSelectModeActive" :count="selectedMessages?.length || 0"
        @forward="handleBatchForward" @combine="handleCombineForward" @delete="handleBatchDelete"
        @cancel="handleClearSelection" />
    </template>
  </div>

  <!-- 图片预览器 -->
  <ImageViewerDialog v-model="showImagePreview" :images="conversationImages" :initial-index="imagePreviewIndex" />

  <!-- 快捷表情选择器 -->
  <teleport to="body">
    <div v-if="showEmojiPopover" class="emoji-popover"
      :style="{ left: emojiPopoverPosition.x + 'px', top: emojiPopoverPosition.y + 'px' }">
      <div class="emoji-popover-header">
        <span>添加表情回应</span>
        <el-icon class="close-icon" @click="showEmojiPopover = false">
          <Close />
        </el-icon>
      </div>
      <div class="emoji-grid">
        <span v-for="emoji in QUICK_EMOJIS" :key="emoji" class="emoji-item" @click="handleSelectEmoji(emoji)">
          {{ emoji }}
        </span>
      </div>
    </div>
  </teleport>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick, h } from 'vue'
import { useStore } from 'vuex'
import { Share, Folder, Delete, Close } from '@element-plus/icons-vue'
import ChatHeader from '@/components/Chat/ChatHeader.vue'
import MessageList from '@/components/Chat/MessageList.vue'
import MessageInput from '@/components/Chat/MessageInputRefactored.vue'
import PinnedMessagesPanel from '@/components/Chat/PinnedMessagesPanel.vue'
import ForwardDialog from '@/components/ForwardDialog/index.vue'
import VoiceCallDialog from '@/components/Chat/VoiceCallDialog.vue'
import VideoCallDialog from '@/components/Chat/VideoCallDialog.vue'
import ChatHistoryPanel from '@/components/Chat/ChatHistoryPanel.vue'
import GroupAnnouncementDialog from '@/components/Chat/GroupAnnouncementDialog.vue'
import GroupDetailDialog from '@/components/Contacts/GroupProfileDialog.vue'
import ChatSearchPanel from '@/components/Chat/ChatSearchPanel.vue'
import ChatSearch from '@/components/Chat/ChatSearch.vue'
import ChatFilesPanel from '@/components/Chat/ChatFilesPanel.vue'
import EmptyState from '@/components/Common/EmptyState.vue'
import CombineDetailDialog from '@/components/Chat/CombineDetailDialog.vue'
import GroupFilePanel from '@/components/Chat/GroupFilePanel.vue'
import ExportChatDialog from '@/components/Chat/ExportChatDialog.vue'
import MultiSelectToolbar from '@/components/Chat/MultiSelectToolbar.vue'
import ImageViewerDialog from '@/components/Chat/ImageViewerDialog.vue'
import { getMessages, batchForwardMessages, clearConversationMessages, retryMessage } from '@/api/im/message'
import { pinConversation, muteConversation } from '@/api/im/conversation'
import { uploadFile } from '@/api/im/file'
import { markMessage, unmarkMessage, setTodoReminder, completeTodo, getUserTodoCount } from '@/api/im/marker'
import { useImWebSocket } from '@/composables/useImWebSocket'
import { useMessageRetry } from '@/composables/useMessageRetry'
import {
  useChatMessages,
  useChatCommands,
  useChatDialogs,
  useChatUpload
} from '@/composables/useChat'
import { ElMessage, ElMessageBox } from 'element-plus'
import { parseMessageContent } from '@/utils/message'
import { useMessageTransformation } from '@/composables/useMessageTransformation.js'

const props = defineProps({
  session: {
    type: Object,
    default: null,
    validator: value => {
      if (value === null) { return true }
      return typeof value.id === 'string' || typeof value.id === 'number'
    }
  }
})

const store = useStore()
const currentUser = computed(() => store.getters['user/currentUser'])

// ==================== Composables ====================
// 消息管理（加载、发送、重试）
const {
  messages,
  loading,
  sending,
  noMore,
  loadHistory,
  send: sendMessage,
  retry,
  removeMessage,
  markAsRead,
  loadMore
} = useChatMessages(computed(() => props.session?.id), currentUser)

// 消息转换（统一消息格式）
const { transformMsg } = useMessageTransformation({ currentUser })

// 消息命令（复制、回复、转发、撤回、删除、编辑等）
const {
  replyingMessage,
  editingMessage,
  forwardingMessages,
  copy,
  reply: cmdReply,
  cancelReply,
  forward,
  batchForward,
  recall,
  deleteMessage,
  edit,
  cancelEdit,
  confirmEdit,
  at,
  addReaction,
  addToTodo,
  favorite,
  unfavorite,
  pin
} = useChatCommands(currentUser, computed(() => props.session?.type))

// 弹窗状态管理
const {
  showVoiceCall,
  showVideoCall,
  isIncomingCall,
  remoteCallUser,
  showChatSearch,
  showFilesPanel,
  showChatHistory,
  showAnnouncementDialog,
  showExportDialog,
  showGroupFilesPanel,
  showCombineDetail,
  combineMessages,
  combineConversationTitle,
  showImagePreview,
  imagePreviewIndex,
  openVoiceCall,
  openVideoCall,
  closeAllCalls,
  openCombineDetail,
  openContactDetail,
  openImagePreview,
  closeAllDialogs
} = useChatDialogs()

// 文件上传
const {
  uploadImage,
  uploadImages,
  uploadCommonFile,
  uploadVideo,
  uploading
} = useChatUpload()

// ==================== 本地状态 ====================
const showGroupDetail = ref(false)
const msgListRef = ref(null)
const forwardDialogRef = ref(null)
const isUnmounted = ref(false) // 标记组件是否已卸载
const showSearchPanel = ref(false)  // 搜索面板（与聊天内搜索区分）
const fileInputRef = ref(null)
const imageInputRef = ref(null)
const messageInputRef = ref(null)

// 当前会话所有图片URL列表（用于预览时左右切换）
const conversationImages = computed(() => {
  if (!messages.value || !Array.isArray(messages.value)) { return [] }
  return messages.value
    .filter(m => {
      if (m.type !== 'IMAGE') { return false }
      const content = parseMessageContent(m)
      return content && (content.url || content.imageUrl)
    })
    .map(m => {
      const content = parseMessageContent(m)
      return content?.url || content?.imageUrl || ''
    })
    .filter(url => url)
})

// 多选模式状态（由头部按钮触发，而非基于选中状态判断）
const isMultiSelectModeActive = ref(false)

// 置顶消息面板状态
const showPinnedPanel = ref(false)
const pinnedCount = computed(() => messages.value.filter(m => m.isPinned).length)

const emit = defineEmits(['show-user'])

const { onMessage, onTyping, onMessageStatus, onReaction, sendTyping, sendStopTyping } = useImWebSocket()

// 失败消息重试管理
const {
  init: initMessageRetry,
  recordFailedMessage,
  removeFailedMessage,
  canRetry
} = useMessageRetry()

// 输入状态用户列表（用于显示"xxx正在输入..."）
const typingUsers = ref([])
const typingTimers = {} // userId -> timerId

// 发送输入状态（防抖）
let sendTypingTimer = null
const TYPING_DEBOUNCE = 500 // 500ms 防抖

// 发送正在输入状态（单聊和群聊都支持）
const sendMyTypingStatus = () => {
  if (!props.session) { return }

  // 清除之前的定时器
  if (sendTypingTimer) {
    clearTimeout(sendTypingTimer)
  }

  // 防抖：500ms 后才发送 typing 事件
  sendTypingTimer = setTimeout(() => {
    sendTyping(props.session.id)
  }, TYPING_DEBOUNCE)
}

// 发送停止输入状态
const sendMyStopTypingStatus = () => {
  if (!props.session) { return }

  if (sendTypingTimer) {
    clearTimeout(sendTypingTimer)
    sendTypingTimer = null
  }

  sendStopTyping(props.session.id)
}

// 处理输入事件（防抖发送 typing 状态）
const handleInput = content => {
  if (content && content.trim().length > 0) {
    sendMyTypingStatus()
  } else {
    sendMyStopTypingStatus()
  }
}

const handleLoadMore = async () => {
  if (loading.value || noMore.value) { return }

  const firstMsg = messages.value[0]
  if (!firstMsg) { return }

  loading.value = true
  const oldHeight = msgListRef.value?.$refs.listRef.scrollHeight

  try {
    const newMsgs = await store.dispatch('im/message/loadMessages', {
      sessionId: props.session.id,
      lastMessageId: firstMsg.id,
      pageSize: 20,
      isLoadMore: true
    })

    if (newMsgs && newMsgs.length > 0) {
      messages.value = [...newMsgs, ...messages.value]
      msgListRef.value?.maintainScroll(oldHeight)
    } else {
      noMore.value = true
    }
  } finally {
    loading.value = false
  }
}

const handleSend = async content => {
  // 发送消息时停止输入状态
  sendMyStopTypingStatus()

  // 先检查队列是否已满（避免添加临时消息后才发现队列满）
  const queueSize = store.getters['im/message/sendingQueueSize'] || 0
  const maxSize = 100 // 与 store 配置一致

  if (queueSize >= maxSize) {
    ElMessage.warning({
      message: `发送队列已满（${maxSize}条），请稍后重试`,
      duration: 3000,
      showClose: true
    })
    return
  }

  // 乐观更新：先显示消息，状态为 sending
  const tempId = `temp-${Date.now()}`
  const tempMsg = {
    id: tempId,
    clientMsgId: tempId,
    content,
    type: 'TEXT',
    senderId: currentUser.value?.id,
    senderName: currentUser.value?.nickName || currentUser.value?.userName || '我',
    senderAvatar: currentUser.value?.avatar,
    timestamp: Date.now(),
    isOwn: true,
    status: 'sending',
    sendStatus: 1, // SENDING
    readCount: 0
  }

  messages.value.push(tempMsg)
  store.commit('im/message/SET_REPLYING_MESSAGE', null)
  msgListRef.value?.scrollToBottom()

  try {
    // 发送消息到服务器
    const msg = await store.dispatch('im/message/sendMessage', {
      sessionId: props.session.id,
      type: 'TEXT',
      content,
      replyToMessageId: replyingMessage.value?.id
    })

    // 发送成功，更新消息状态和ID
    const index = messages.value.findIndex(m => m.id === tempId)
    if (index !== -1) {
      // 保持 status 为 success，且替换为真实数据
      const realMsg = transformMsg(msg)
      console.log('Real message:', realMsg)
      messages.value.splice(index, 1, { ...realMsg, status: null })
    }
  } catch (error) {
    // 发送失败，标记状态
    const index = messages.value.findIndex(m => m.id === tempId)
    if (index !== -1) {
      messages.value[index].status = 'failed'
      messages.value[index].sendStatus = 4 // FAILED

      // 记录失败消息到缓存（使用 tempId 作为 clientMsgId）
      recordFailedMessage({
        id: tempId,
        clientMsgId: tempId,
        sessionId: props.session.id,
        type: 'TEXT',
        content,
        timestamp: Date.now()
      })
    }

    console.error('发送失败', error)
    ElMessage.error(error.message || '发送失败，请检查网络连接')
  }
}

// 发送语音消息
const handleSendVoice = async ({ file, duration }) => {
  sendMyStopTypingStatus()
  const tempId = `temp-${Date.now()}`
  const tempMsg = {
    id: tempId,
    content: JSON.stringify({ duration }),
    type: 'VOICE',
    senderId: currentUser.value?.id,
    senderName: currentUser.value?.nickName || currentUser.value?.userName || '我',
    senderAvatar: currentUser.value?.avatar,
    timestamp: Date.now(),
    isOwn: true,
    status: 'uploading',
    readCount: 0
  }

  messages.value.push(tempMsg)
  msgListRef.value?.scrollToBottom()

  try {
    // 上传语音文件
    const formData = new FormData()
    formData.append('file', file)

    const uploadRes = await uploadFile(formData)
    const voiceUrl = uploadRes.data?.fileUrl

    // 发送语音消息
    const msg = await store.dispatch('im/message/sendMessage', {
      sessionId: props.session.id,
      type: 'VOICE',
      content: JSON.stringify({ voiceUrl, duration }),
      replyToMessageId: replyingMessage.value?.id
    })

    // 发送成功，更新消息状态和ID
    const index = messages.value.findIndex(m => m.id === tempId)
    if (index !== -1) {
      const realMsg = transformMsg(msg)
      messages.value.splice(index, 1, { ...realMsg, status: null })
    }
  } catch (error) {
    // 发送失败，标记状态
    const index = messages.value.findIndex(m => m.id === tempId)
    if (index !== -1) {
      messages.value[index].status = 'failed'
    }
    console.error('语音发送失败', error)
    ElMessage.error('语音发送失败')
  }
}


// Websocket handling
onMessage(msg => {
  if (msg.conversationId === props.session?.id) {
    const transformedMsg = transformMsg(msg, messages.value)
    messages.value.push(transformedMsg)
    msgListRef.value?.scrollToBottom()

    // 新消息提醒
    if (!transformedMsg.isOwn) {
      // 动态导入提醒工具,避免循环依赖
      import('@/utils/messageNotification').then(({ showMessageNotification, shouldNotify }) => {
        if (shouldNotify(msg, currentUser.value, props.session)) {
          let body = msg.content
          if (msg.type === 'IMAGE') { body = '[图片]' }
          else if (msg.type === 'FILE') { body = '[文件]' }
          else if (msg.type === 'RECALLED') { body = '撤回了一条消息' }

          showMessageNotification({
            title: msg.senderName || '新消息',
            body: body || '[消息]',
            icon: msg.senderAvatar || '',
            sound: true,
            notification: true,
            titleFlash: true
          })
        }
      })
    }
  }
})

watch(() => props.session, () => {
  // 清空消息列表
  messages.value.splice(0, messages.value.length)
  loadHistory()
})

const handleDelete = async message => {
  await deleteMessage(message)
  // 移除本地消息（composable 中已处理 store，这里只需处理本地列表）
  const index = messages.value.findIndex(m => m.id === message.id)
  if (index !== -1) {
    messages.value.splice(index, 1)
  }
}

const handleRecall = async message => {
  await recall(message)
  // 更新本地消息状态已在 recall 中处理
  const index = messages.value.findIndex(m => m.id === message.id)
  if (index !== -1) {
    messages.value[index].type = 'RECALLED'
    messages.value[index].content = ''
  }
}

// 处理菜单命令
const handleCommand = (cmd, msg) => {
  if (cmd === 'forward') {
    forwardDialogRef.value?.open(msg)
  } else if (cmd === 'reply') {
    handleReply(msg)
  } else if (cmd === 'recall') {
    handleRecall(msg)
  } else if (cmd === 'delete') {
    handleDelete(msg)
  } else if (cmd === 'edit') {
    handleEdit(msg)
  } else if (cmd === 'mark-read') {
    handleMarkRead(msg)
  } else if (cmd === 'todo') {
    handleAddToTodo(msg)
  } else if (cmd === 'favorite') {
    handleFavorite(msg)
  } else if (cmd === 'mark') {
    handleMarkMessage(msg)
  } else if (cmd === 'multi-select') {
    handleMultiSelect(msg)
  } else if (cmd === 'view-combine') {
    handleViewCombine(msg)
  } else if (cmd === 'export') {
    // useChatDialogs 已提供 showExportDialog 状态
  } else if (cmd === 'emoji') {
    handleShowEmojiPicker(msg)
  }
}

// 快捷表情列表（钉钉风格）
const QUICK_EMOJIS = ['👍', '👏', '❤️', '😂', '😮', '😢', '😡', '🎉']
const showEmojiPopover = ref(false)
const emojiPopoverPosition = ref({ x: 0, y: 0 })
const emojiTargetMessage = ref(null)

// 显示表情选择器
const handleShowEmojiPicker = msg => {
  emojiTargetMessage.value = msg
  // 计算位置：显示在输入框上方
  const inputArea = document.querySelector('.chat-input-container')
  if (inputArea) {
    const rect = inputArea.getBoundingClientRect()
    emojiPopoverPosition.value = {
      x: rect.right - 300,
      y: rect.top - 220
    }
  } else {
    // 默认位置：屏幕中央
    emojiPopoverPosition.value = {
      x: window.innerWidth / 2 - 140,
      y: window.innerHeight / 2 - 110
    }
  }
  showEmojiPopover.value = true
}

// 选择表情反应
const handleSelectEmoji = async emoji => {
  if (!emojiTargetMessage.value) { return }

  const msg = emojiTargetMessage.value

  // 使用 composable 的 addReaction 方法
  await addReaction(msg, emoji, 'EMOJI')

  showEmojiPopover.value = false
  emojiTargetMessage.value = null
}

// 处理设为待办
const handleAddToTodo = async msg => {
  await addToTodo(msg)
  // 更新本地消息标记状态
  if (msg.markers) {
    msg.markers.push({ markerType: 'TODO', isCompleted: false })
  } else {
    msg.markers = [{ markerType: 'TODO', isCompleted: false }]
  }
}

// 处理收藏消息
const handleFavorite = async msg => {
  await favorite(msg)
}

// 处理标记消息
const handleMarkMessage = async msg => {
  // 使用 ElMessageBox 显示标记选项
  try {
    const result = await ElMessageBox({
      title: '标记消息',
      message: h('div', { class: 'marker-options' }, [
        h('p', { style: 'margin-bottom: 16px; color: #64748b;' }, '请选择标记类型：'),
        h('div', { class: 'marker-buttons', style: 'display: flex; gap: 8px; flex-wrap: wrap;' }, [
          h('el-button', {
            onClick: () => {
              markMessageAction(msg, 'FLAG', '#ff4d4f')
              ElMessageBox.close(result)
            },
            style: 'flex: 1; min-width: 80px;'
          }, () => [
            h('span', { class: 'material-icons-outlined', style: 'vertical-align: middle; margin-right: 4px; color: #ff4d4f;' }, 'flag'),
            '标记'
          ]),
          h('el-button', {
            type: 'warning',
            onClick: () => {
              markMessageAction(msg, 'IMPORTANT', '#faad14')
              ElMessageBox.close(result)
            },
            style: 'flex: 1; min-width: 80px;'
          }, () => [
            h('span', { class: 'material-icons-outlined', style: 'vertical-align: middle; margin-right: 4px;' }, 'star'),
            '重要'
          ]),
          h('el-button', {
            type: 'success',
            onClick: () => {
              setTodoReminder({
                messageId: msg.id,
                remindTime: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
                remark: msg.content?.substring(0, 50) || '消息待办'
              })
              ElMessageBox.close(result)
              ElMessage.success('已添加到待办')
            },
            style: 'flex: 1; min-width: 80px;'
          }, () => [
            h('span', { class: 'material-icons-outlined', style: 'vertical-align: middle; margin-right: 4px;' }, 'check_circle'),
            '待办'
          ])
        ])
      ]),
      showCancelButton: true,
      showConfirmButton: false,
      closeOnClickModal: true,
      closeOnPressEscape: true
    })
  } catch (e) {
    // 用户取消，不做处理
  }
}

// 执行标记操作
const markMessageAction = async (msg, markerType, color) => {
  try {
    await markMessage({
      messageId: msg.id,
      markerType,
      color
    })
    ElMessage.success(markerType === 'FLAG' ? '已标记消息' : '已标记为重要')
    // 更新消息标记状态
    if (msg.markers) {
      msg.markers.push({ markerType, color })
    } else {
      msg.markers = [{ markerType, color }]
    }
  } catch (e) {
    console.error('标记失败', e)
    ElMessage.error('标记失败')
  }
}

// 处理多选
const isMultiSelectMode = computed(() => (store.getters['im/message/selectedMessageCount'] || 0) > 0)
const selectedMessages = computed(() => store.getters['im/message/selectedMessageList'] || [])

const handleMultiSelect = msg => {
  // 切换选中状态
  store.commit('im/message/TOGGLE_MESSAGE_SELECTION', msg.id)

  // 如果是第一次选中（之前没有选中消息），进入多选模式
  if (!isMultiSelectModeActive.value && store.getters['im/message/selectedMessageCount'] > 0) {
    isMultiSelectModeActive.value = true

    // 触感反馈
    if (navigator.vibrate) {
      navigator.vibrate(50) // 轻微震动 50ms
    }
  }
}

// 查看合并转发消息详情
const handleViewCombine = msg => {
  // msg 包含 messages 数组（合并的消息内容）
  if (msg.messages && msg.messages.length > 0) {
    openCombineDetail(msg.messages, `${msg.senderName}的聊天记录`)
  } else {
    ElMessage.warning('无法查看聊天记录详情')
  }
}

// 合并消息详情中的转发
const handleCombineForwardDetail = messages => {
  const messageIds = messages.map(m => m.id)
  if (messageIds.length > 0) {
    forwardDialogRef.value?.openForBatch(messageIds, 'combine')
  }
}

// 批量转发 - 逐条转发
const handleBatchForward = async () => {
  const messageIds = selectedMessages.value.map(msg => msg.id)
  if (messageIds.length === 0) { return }

  // 使用 ForwardDialog 选择目标会话
  forwardDialogRef.value?.openForBatch(messageIds, 'batch')
}

// 批量转发 - 合并转发
const handleCombineForward = async () => {
  const messageIds = selectedMessages.value.map(msg => msg.id)
  if (messageIds.length === 0) { return }

  // 使用 ForwardDialog 选择目标会话
  forwardDialogRef.value?.openForBatch(messageIds, 'combine')
}

// 批量删除
const handleBatchDelete = async () => {
  const selected = selectedMessages.value
  if (selected.length === 0) { return }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selected.length} 条消息吗？`,
      '批量删除',
      { type: 'warning' }
    )

    // 调用删除 API
    for (const msg of selected) {
      // 使用已有的删除消息 API
      await deleteMessage(msg.id)
    }

    ElMessage.success(`已删除 ${selected.length} 条消息`)
    handleClearSelection()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 切换多选模式
const handleToggleMultiSelect = active => {
  isMultiSelectModeActive.value = active
  if (!active) {
    // 退出多选时清空选择
    store.commit('im/message/CLEAR_MESSAGE_SELECTION')
  }
}

const handleClearSelection = () => {
  store.commit('im/message/CLEAR_MESSAGE_SELECTION')
  isMultiSelectModeActive.value = false
}

// 拖拽上传相关
const isDragging = ref(false)
const isDragOver = ref(false)
let dragEnterCounter = 0 // 用于防止子元素触发 dragleave

const handleDragEnter = event => {
  dragEnterCounter++
  const files = event.dataTransfer?.files
  if (files && files.length > 0) {
    isDragOver.value = true
  }
}

const handleDragOver = event => {
  const files = event.dataTransfer?.files
  if (files && files.length > 0) {
    isDragging.value = true
  }
}

const handleDragLeave = event => {
  dragEnterCounter--
  if (dragEnterCounter <= 0) {
    dragEnterCounter = 0
    isDragging.value = false
    isDragOver.value = false
  }
}

const handleDrop = async event => {
  isDragging.value = false
  isDragOver.value = false
  dragEnterCounter = 0

  const files = event.dataTransfer?.files
  if (!files || files.length === 0) { return }

  for (const file of files) {
    if (file.type.startsWith('image/')) {
      await uploadImageFile(file)
    } else {
      await uploadFileFile(file)
    }
  }
}

const uploadImageFile = async file => {
  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('type', 'image')

    // 创建本地预览
    const blobUrl = URL.createObjectURL(file)

    // 发送消息
    const tempMessage = {
      id: Date.now(),
      type: 'IMAGE',
      content: JSON.stringify({ imageUrl: blobUrl }),
      senderId: currentUser.value?.id,
      senderName: currentUser.value?.name,
      timestamp: new Date().toISOString(),
      isOwn: true,
      status: 'sending'
    }

    messages.value.push(transformMsg(tempMessage))

    // 上传图片
    const res = await uploadImage(formData)
    if (res.code === 200 && res.data) {
      // 更新消息
      const index = messages.value.findIndex(m => m.id === tempMessage.id)
      if (index !== -1) {
        messages.value[index] = {
          ...messages.value[index],
          content: JSON.stringify({ imageUrl: res.data.url }),
          status: null
        }
      }
    }
  } catch (error) {
    ElMessage.error('上传失败')
    console.error(error)
  }
}

const uploadFileFile = async file => {
  if (!props.session?.id) {
    ElMessage.warning('请先选择一个会话')
    return
  }

  // 验证文件大小（限制 100MB）
  const maxSize = 100 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过 100MB')
    return
  }

  try {
    // 上传文件到服务器
    const uploadResult = await uploadCommonFile(file, props.session.id)

    if (uploadResult && uploadResult.fileUrl) {
      // 构建文件消息内容
      const fileContent = {
        fileName: file.name,
        fileUrl: uploadResult.fileUrl,
        fileId: uploadResult.fileId,
        fileSize: file.size,
        fileType: file.type || 'application/octet-stream'
      }

      // 发送文件消息
      await handleSend(JSON.stringify(fileContent), 'FILE')

      ElMessage.success(`文件 "${file.name}" 上传成功`)
    }
  } catch (error) {
    console.error('文件上传失败:', error)
    ElMessage.error(error.message || '文件上传失败')
  }
}

const handlePaste = async event => {
  const items = event.clipboardData?.items
  if (!items) { return }

  for (const item of items) {
    if (item.type.startsWith('image/')) {
      const file = item.getAsFile()
      if (file) {
        await uploadImageFile(file)
        break // 只处理第一张图片
      }
    }
  }
}

// 处理已读上报
const handleMarkRead = async msg => {
  try {
    await store.dispatch('im/message/markMessageAsRead', {
      conversationId: props.session.id,
      messageId: msg.id
    })
    // 本地标记已读，避免重复触发
    msg.isRead = true
  } catch (e) {
    console.warn('上报已读状态失败', e)
  }
}

const handleToggleDetail = () => {
  if (props.session?.type === 'GROUP') {
    showGroupDetail.value = true
  } else if (props.session) {
    // 触发父组件处理用户详情显示
    emit('show-user', props.session.targetId)
  }
}

// 处理侧边栏切换（成员列表等），当前 ChatPanel 无此功能
const handleToggleSidebar = tab => {
  // 侧边栏切换逻辑（如成员列表），暂无实现
  console.log('toggle-sidebar:', tab)
}

// 图片预览处理
const handleImagePreview = imageUrl => {
  const index = conversationImages.value.indexOf(imageUrl)
  openImagePreview(index, conversationImages.value)
}

const handleCancelReply = () => {
  cancelReply()
}

const handleCancelEdit = () => {
  cancelEdit()
}

const handleRetry = async msg => {
  // 检查消息状态
  const status = msg.sendStatus || msg.status
  if (status !== 4 && status !== 'FAILED' && status !== 'failed') { return }

  // 获取客户端消息ID
  const clientMsgId = msg.clientMsgId || msg.id
  if (!clientMsgId) {
    ElMessage.error('无法重试：缺少消息标识')
    return
  }

  // 检查是否可以重试
  if (!canRetry(clientMsgId)) {
    ElMessage.warning('重试次数已达上限（最多3次）')
    return
  }

  // 重置为发送中（使用数字状态：1 = SENDING）
  msg.sendStatus = 1
  msg.status = 'sending'

  try {
    // 调用后端重试 API（支持自动重试：1s, 2s, 4s）
    const res = await retryMessage(clientMsgId)

    if (res.code === 200) {
      ElMessage.success({
        message: '正在重试发送...',
        duration: 2000
      })

      // 注意：实际发送结果是异步的，后端会通过 WebSocket 推送更新
      // 这里只需更新 UI 状态为"发送中"
      msg.sendStatus = 1
    } else {
      // 服务端返回错误（如已达重试上限）
      throw new Error(res.msg || '重试失败')
    }
  } catch (error) {
    // 重置为失败状态（使用数字状态：4 = FAILED）
    msg.sendStatus = 4
    msg.status = 'failed'

    ElMessage.error(error.message || '重试失败，请稍后重试')
    console.error('消息重试失败:', error)
  }
}

const handleMemberClick = member => {
  // 触发父组件显示用户详情
  emit('show-user', member.id)
}

// 处理 MessageList 组件中的用户显示请求
const handleShowUser = userId => {
  emit('show-user', userId)
}

// 处理转发确认
const handleForwardConfirm = async ({ message, targetSessionId }) => {
  try {
    await store.dispatch('im/message/forwardMessage', {
      messageId: message.id,
      targetConversationId: targetSessionId
    })
    ElMessage.success('转发成功')
  } catch (error) {
    ElMessage.error('转发失败')
    console.error(error)
  }
}

// 批量转发确认处理
const handleBatchForwardConfirm = async ({ messageIds, targetSessionId, forwardType }) => {
  try {
    await batchForwardMessages({
      messageIds: messageIds,
      toConversationId: targetSessionId,
      forwardType: forwardType,
      content: ''
    })
    const typeText = forwardType === 'combine' ? '合并转发' : '逐条转发'
    ElMessage.success(`${typeText}成功`)
    handleClearSelection()
  } catch (error) {
    ElMessage.error('转发失败')
    console.error(error)
  }
}

const handleReply = message => {
  cmdReply(message)
  // 聚焦输入框，提升用户体验
  nextTick(() => {
    if (isUnmounted.value) { return }
    messageInputRef.value?.focus()
  })
}

const handleEdit = message => {
  edit(message)
}

const handleAt = message => {
  if (!message) { return }
  messageInputRef.value?.insertAt(message.senderName)
}

/**
 * 一键提醒未读成员
 * 发送 @ 提醒消息给所有未读成员
 */
const handleRemindUnread = async ({ conversationId, messageId, unreadMembers }) => {
  if (!unreadMembers || unreadMembers.length === 0) {
    ElMessage.info('暂无未读成员')
    return
  }

  try {
    // 构建消息内容：@所有未读成员
    const mentions = unreadMembers.map(m => ({
      userId: m.userId,
      nickname: m.nickname || m.userName
    }))

    // 构建 @ 提及文本
    const mentionText = unreadMembers
      .map(m => `@${m.nickname || m.userName}`)
      .join(' ')

    // 发送提醒消息
    await store.dispatch('im/message/sendMessage', {
      sessionId: conversationId,
      type: 'TEXT',
      content: `${mentionText} 请查看上方消息`,
      replyToMessageId: messageId, // 引用原消息
      atUserIds: mentions.map(m => m.userId) // @ 提及的用户ID列表
    })

    ElMessage.success(`已提醒 ${unreadMembers.length} 位成员`)
  } catch (error) {
    console.error('提醒失败:', error)
    ElMessage.error('提醒失败，请稍后重试')
  }
}

const handleEditConfirm = async content => {
  if (!editingMessage.value) { return }

  await confirmEdit(content)

  // 更新本地消息列表
  const index = messages.value.findIndex(m => m.id === editingMessage.value.id)
  if (index !== -1) {
    messages.value[index].content = content
    messages.value[index].isEdited = true
  }
}

/**
 * 处理撤回消息重新编辑
 */
const handleReEdit = ({ content }) => {
  if (!content) { return }

  // 使用 composable 的编辑状态
  edit({
    id: `reedit-${Date.now()}`,
    content,
    isReEdit: true
  })

  // 将内容填充到输入框
  messageInputRef.value?.setContent(content)

  // 聚焦输入框
  nextTick(() => {
    if (isUnmounted.value) { return }
    messageInputRef.value?.focus()
  })

  ElMessage.info('已恢复消息内容，修改后点击发送')
}

// 通话功能
const handleStartCall = () => {
  if (!props.session) {
    ElMessage.warning('请先选择会话')
    return
  }
  isIncomingCall.value = false
  // 设置通话用户信息
  remoteCallUser.value = {
    userId: props.session?.targetId,
    userName: props.session?.name,
    avatar: props.session?.avatar
  }
  showVoiceCall.value = true
}

const handleStartVideo = () => {
  if (!props.session) {
    ElMessage.warning('请先选择会话')
    return
  }
  isIncomingCall.value = false
  // 设置通话用户信息
  remoteCallUser.value = {
    userId: props.session?.targetId,
    userName: props.session?.name,
    avatar: props.session?.avatar
  }
  showVideoCall.value = true
}

// ChatHeader 通话按钮事件
const handleVoiceCall = () => {
  if (!props.session) {
    ElMessage.warning('请先选择会话')
    return
  }
  // 构建通话用户信息
  const user = {
    userId: props.session?.targetId,
    userName: props.session?.name,
    avatar: props.session?.avatar
  }
  openVoiceCall(user, false)
}

const handleVideoCall = () => {
  if (!props.session) {
    ElMessage.warning('请先选择会话')
    return
  }
  // 构建通话用户信息
  const user = {
    userId: props.session?.targetId,
    userName: props.session?.name,
    avatar: props.session?.avatar
  }
  openVideoCall(user, false)
}

// 创建公告
const handleCreateAnnouncement = () => {
  if (props.session?.type === 'GROUP') {
    showAnnouncementDialog.value = true
  } else {
    ElMessage.warning('只有群聊可以发布公告')
  }
}

// 查看文件
const handleShowFiles = () => {
  // 群组显示群文件面板，单聊显示会话文件面板
  if (props.session?.type === 'GROUP') {
    showGroupFilesPanel.value = true
  } else {
    showFilesPanel.value = true
  }
}

// 搜索消息
const handleSearchMessages = () => {
  showChatSearch.value = true
}

// 会话操作
const handlePinSession = async () => {
  const currentSession = store.state.im.session?.currentSession
  if (!currentSession) { return }

  const newState = !currentSession.isPinned
  try {
    await pinConversation(currentSession.id, newState)
    store.commit('im/session/UPDATE_SESSION', {
      id: currentSession.id,
      isPinned: newState
    })
    ElMessage.success(newState ? '已置顶' : '已取消置顶')
  } catch (e) {
    ElMessage.error('操作失败，请重试')
  }
}

const handleMuteSession = async () => {
  const currentSession = store.state.im.session?.currentSession
  if (!currentSession) { return }

  const newState = !currentSession.isMuted
  try {
    await muteConversation(currentSession.id, newState)
    store.commit('im/session/UPDATE_SESSION', {
      id: currentSession.id,
      isMuted: newState
    })
    ElMessage.success(newState ? '已开启免打扰' : '已关闭免打扰')
  } catch (e) {
    ElMessage.error('操作失败，请重试')
  }
}

const handleClearMessages = async () => {
  if (!props.session) {
    ElMessage.warning('请先选择会话')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要清空与 ${props.session?.name} 的聊天记录吗？`,
      '清空聊天记录',
      {
        type: 'warning',
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger',
        dangerouslyUseHTMLString: false
      }
    )

    // 调用 API 清空消息
    await clearConversationMessages(props.session?.id)

    // 清空本地消息列表
    messages.value.splice(0, messages.value.length)

    // 重置分页状态
    noMore.value = false

    ElMessage.success('聊天记录已清空')
  } catch {
    // 用户取消或出错
  }
}

// 滚动到置顶消息
const handleScrollToPinnedMessage = messageId => {
  if (msgListRef.value) {
    msgListRef.value.scrollToMessage(messageId)
  }
}

// 处理搜索结果滚动
const handleScrollToMessage = messageId => {
  if (msgListRef.value) {
    msgListRef.value.scrollToMessage(messageId)
  }
}

// 处理置顶状态更新
const handlePinnedUpdate = ({ messageId, isPinned }) => {
  const index = messages.value.findIndex(m => m.id === messageId)
  if (index !== -1) {
    messages.value[index].isPinned = isPinned
  }
}

// 显示聊天记录
const handleShowHistory = () => {
  showChatHistory.value = true
}

// 跳转到指定消息
const handleJumpToMessage = message => {
  if (msgListRef.value) {
    msgListRef.value.scrollToMessage(message.id || message.messageId)
  }
  showSearchPanel.value = false
  showChatHistory.value = false
}

// 打开文件
const handleOpenFile = file => {
  if (file.url) {
    window.open(file.url, '_blank')
  }
}

// 下载文件
const handleDownloadFile = file => {
  if (Array.isArray(file)) {
    // 批量下载
    ElMessage.info(`正在下载 ${file.length} 个文件...`)
  } else {
    if (file.url) {
      const link = document.createElement('a')
      link.href = file.url
      link.download = file.name
      link.target = '_blank'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      ElMessage.success('开始下载')
    }
  }
}

// 转发文件
const handleForwardFile = file => {
  if (forwardDialogRef.value) {
    forwardDialogRef.value.open([{ type: 'FILE', content: JSON.stringify(file) }])
  }
}

// 清空历史记录
const handleClearHistory = async () => {
  // 复用 handleClearMessages 函数
  await handleClearMessages()
  showChatHistory.value = false
}

// 处理输入状态指示
const handleTypingIndicator = (userId, userName) => {
  // 清除该用户的旧定时器
  if (typingTimers[userId]) {
    clearTimeout(typingTimers[userId])
  }

  // 添加到正在输入的用户列表
  if (!typingUsers.value.find(u => u.userId === userId)) {
    typingUsers.value.push({ userId, userName })
  }

  // 设置5秒后移除输入状态
  typingTimers[userId] = setTimeout(() => {
    const index = typingUsers.value.findIndex(u => u.userId === userId)
    if (index !== -1) {
      typingUsers.value.splice(index, 1)
    }
    delete typingTimers[userId]
  }, 5000)
}

// 文件上传相关
const triggerFileUpload = () => fileInputRef.value?.click()
const triggerImageUpload = () => imageInputRef.value?.click()

const handleFileUpload = async payload => {
  sendMyStopTypingStatus()
  let file, formData
  if (payload instanceof FormData) {
    formData = payload
    file = payload.get('file')
  } else if (payload instanceof File) {
    // 直接是 File 对象（来自 MessageInput 的 emit）
    file = payload
    formData = new FormData()
    formData.append('file', file)
  } else {
    // 事件对象
    file = payload?.target?.files?.[0]
    if (!file) { return }
    formData = new FormData()
    formData.append('file', file)
    if (payload?.target) {
      payload.target.value = ''
    }
  }

  // 1. 乐观更新：立即显示文件消息
  const tempId = `temp-file-${Date.now()}`
  const tempMsg = {
    id: tempId,
    type: 'FILE',
    content: {
      fileName: file.name,
      size: file.size,
      fileUrl: '' // 上传中暂无 URL
    },
    senderId: currentUser.value?.id,
    senderName: currentUser.value?.nickName || '我',
    senderAvatar: currentUser.value?.avatar,
    timestamp: Date.now(),
    isOwn: true,
    status: 'uploading', // 新状态: 上传中
    readCount: 0
  }
  messages.value.push(tempMsg)
  msgListRef.value?.scrollToBottom()

  try {
    // 2. 上传文件
    const res = await uploadFile(formData)
    if (res.code === 200) {
      // 3. 发送消息
      const msg = await store.dispatch('im/message/sendMessage', {
        sessionId: props.session.id,
        type: 'FILE',
        content: JSON.stringify({
          fileId: res.data.id,
          fileName: file.name,
          size: file.size,
          fileUrl: res.data.url
        })
      })

      // 4. 更新状态
      const index = messages.value.findIndex(m => m.id === tempId)
      if (index !== -1) {
        messages.value.splice(index, 1, { ...transformMsg(msg), status: null })
      }
    } else {
      throw new Error(res.msg || 'Upload failed')
    }
  } catch (error) {
    const index = messages.value.findIndex(m => m.id === tempId)
    if (index !== -1) {
      messages.value[index].status = 'failed'
    }
    ElMessage.error('文件发送失败')
  }
}

const handleImageUpload = async payload => {
  sendMyStopTypingStatus()
  let file, formData
  if (payload instanceof FormData) {
    formData = payload
    file = payload.get('file')
  } else if (payload instanceof File) {
    // 直接是 File 对象（来自 MessageInput 的 emit）
    file = payload
    formData = new FormData()
    formData.append('file', file)
  } else {
    // 事件对象
    file = payload?.target?.files?.[0]
    if (!file) { return }
    formData = new FormData()
    formData.append('file', file)
    if (payload?.target) {
      payload.target.value = ''
    }
  }

  // 1. 乐观更新：立即显示图片
  const blobUrl = URL.createObjectURL(file)
  const tempId = `temp-img-${Date.now()}`
  const tempMsg = {
    id: tempId,
    type: 'IMAGE',
    content: {
      imageUrl: blobUrl // 使用本地 Blob URL 预览
    },
    senderId: currentUser.value?.id,
    senderName: currentUser.value?.nickName || '我',
    senderAvatar: currentUser.value?.avatar,
    timestamp: Date.now(),
    isOwn: true,
    status: 'uploading',
    readCount: 0
  }
  messages.value.push(tempMsg)
  msgListRef.value?.scrollToBottom()

  try {
    const res = await uploadImage(formData)
    if (res.code === 200) {
      const msg = await store.dispatch('im/message/sendMessage', {
        sessionId: props.session.id,
        type: 'IMAGE',
        content: JSON.stringify({
          fileId: res.data.id,
          imageUrl: res.data.url
        })
      })

      const index = messages.value.findIndex(m => m.id === tempId)
      if (index !== -1) {
        messages.value.splice(index, 1, { ...transformMsg(msg), status: null })
      }
      // 释放 blob
      URL.revokeObjectURL(blobUrl)
    } else {
      throw new Error(res.msg || 'Upload failed')
    }
  } catch (error) {
    const index = messages.value.findIndex(m => m.id === tempId)
    if (index !== -1) {
      messages.value[index].status = 'failed'
    }
    ElMessage.error('图片发送失败')
  }
}

// 截图上传处理
const handleScreenshotUpload = async formData => {
  sendMyStopTypingStatus()
  const file = formData.get('file')
  if (!file) { return }

  // 1. 乐观更新：立即显示截图
  const blobUrl = URL.createObjectURL(file)
  const tempId = `temp-screenshot-${Date.now()}`
  const tempMsg = {
    id: tempId,
    type: 'IMAGE',
    content: {
      imageUrl: blobUrl
    },
    senderId: currentUser.value?.id,
    senderName: currentUser.value?.nickName || '我',
    senderAvatar: currentUser.value?.avatar,
    timestamp: Date.now(),
    isOwn: true,
    status: 'uploading',
    readCount: 0
  }
  messages.value.push(tempMsg)
  msgListRef.value?.scrollToBottom()

  try {
    const res = await uploadImage(formData)
    if (res.code === 200) {
      const msg = await store.dispatch('im/message/sendMessage', {
        sessionId: props.session.id,
        type: 'IMAGE',
        content: JSON.stringify({
          fileId: res.data.id,
          imageUrl: res.data.url
        })
      })

      const index = messages.value.findIndex(m => m.id === tempId)
      if (index !== -1) {
        messages.value.splice(index, 1, { ...transformMsg(msg), status: null })
      }
      URL.revokeObjectURL(blobUrl)
    } else {
      throw new Error(res.msg || 'Upload failed')
    }
  } catch (error) {
    const index = messages.value.findIndex(m => m.id === tempId)
    if (index !== -1) {
      messages.value[index].status = 'failed'
    }
    ElMessage.error('截图发送失败')
  }
}

// 视频上传处理
const handleVideoUpload = async ({ file, url }) => {
  sendMyStopTypingStatus()
  // 1. 乐观更新：立即显示视频消息
  const tempId = `temp-video-${Date.now()}`
  const tempMsg = {
    id: tempId,
    type: 'VIDEO',
    content: {
      videoUrl: url, // 使用本地 Blob URL 预览
      fileName: file.name,
      size: file.size,
      duration: 0 // 可以后续获取视频时长
    },
    senderId: currentUser.value?.id,
    senderName: currentUser.value?.nickName || '我',
    senderAvatar: currentUser.value?.avatar,
    timestamp: Date.now(),
    isOwn: true,
    status: 'uploading',
    readCount: 0
  }
  messages.value.push(tempMsg)
  msgListRef.value?.scrollToBottom()

  try {
    // 2. 上传视频文件
    const formData = new FormData()
    formData.append('file', file)

    const res = await uploadFile(formData)
    if (res.code === 200) {
      // 3. 发送视频消息
      const msg = await store.dispatch('im/message/sendMessage', {
        sessionId: props.session.id,
        type: 'VIDEO',
        content: JSON.stringify({
          fileId: res.data.id,
          videoUrl: res.data.url,
          fileName: file.name,
          size: file.size
        }),
        replyToMessageId: replyingMessage.value?.id
      })

      // 4. 更新状态
      const index = messages.value.findIndex(m => m.id === tempId)
      if (index !== -1) {
        messages.value.splice(index, 1, { ...transformMsg(msg), status: null })
      }
      // 释放 blob
      URL.revokeObjectURL(url)
    } else {
      throw new Error(res.msg || 'Upload failed')
    }
  } catch (error) {
    const index = messages.value.findIndex(m => m.id === tempId)
    if (index !== -1) {
      messages.value[index].status = 'failed'
    }
    ElMessage.error('视频发送失败')
    console.error('视频上传失败', error)
  }
}

// 发送位置消息
const handleSendLocation = async ({ latitude, longitude, address }) => {
  sendMyStopTypingStatus()
  // 1. 乐观更新：立即显示位置消息
  const tempId = `temp-location-${Date.now()}`
  const tempMsg = {
    id: tempId,
    type: 'LOCATION',
    content: {
      latitude,
      longitude,
      address: address || '未知位置'
    },
    senderId: currentUser.value?.id,
    senderName: currentUser.value?.nickName || '我',
    senderAvatar: currentUser.value?.avatar,
    timestamp: Date.now(),
    isOwn: true,
    status: 'sending',
    readCount: 0
  }
  messages.value.push(tempMsg)
  msgListRef.value?.scrollToBottom()

  try {
    // 2. 发送位置消息
    const msg = await store.dispatch('im/message/sendMessage', {
      sessionId: props.session.id,
      type: 'LOCATION',
      content: JSON.stringify({
        latitude,
        longitude,
        address: address || '未知位置'
      }),
      replyToMessageId: replyingMessage.value?.id
    })

    // 3. 更新状态
    const index = messages.value.findIndex(m => m.id === tempId)
    if (index !== -1) {
      messages.value.splice(index, 1, { ...transformMsg(msg), status: null })
    }
  } catch (error) {
    const index = messages.value.findIndex(m => m.id === tempId)
    if (index !== -1) {
      messages.value[index].status = 'failed'
    }
    ElMessage.error('位置发送失败')
    console.error('位置发送失败', error)
  }
}

onMounted(() => {
  // 初始化失败消息重试管理
  initMessageRetry()

  if (props.session) { loadHistory() }

  // 请求浏览器通知权限
  import('@/utils/messageNotification').then(({ requestNotificationPermission }) => {
    requestNotificationPermission().then(permission => {
      // 权限结果静默处理
    })
  })

  // 监听输入状态事件
  onTyping(data => {
    if (data.conversationId !== props.session?.id) { return }
    if (data.userId === currentUser.value?.id) { return } // 忽略自己的输入状态

    handleTypingIndicator(data.userId, data.userName || data.senderName)
  })

  // 监听消息状态更新（发送成功/失败）
  onMessageStatus(data => {
    if (data.conversationId !== props.session?.id) { return }

    const index = messages.value.findIndex(m => m.id === data.messageId)
    if (index !== -1) {
      // 映射后端 sendStatus 数值到前端状态字符串
      // 0=PENDING, 1=SENDING, 2=DELIVERED(不显示), 3=READ(已读), 4=FAILED
      const statusMap = {
        0: 'sending',  // PENDING - 显示发送中
        1: 'sending',  // SENDING - 显示发送中
        2: null,       // DELIVERED - 不显示任何状态（已移除已送达显示）
        3: 'read',     // READ - 显示已读
        4: 'failed'    // FAILED - 显示失败
      }
      const sendStatus = parseInt(data.sendStatus)
      messages.value[index].status = statusMap[sendStatus] ?? null
    }
  })

  // 监听表情回复更新
  onReaction(data => {
    // WebSocket 推送的数据格式: { messageId, emoji, userId, userName, userAvatar, isAdd }
    store.dispatch('im/message/handleReactionUpdate', {
      messageId: data.messageId,
      emoji: data.emoji,
      userId: data.userId,
      userName: data.userName,
      userAvatar: data.userAvatar,
      isAdd: data.isAdd !== false // 默认为添加
    })
  })

  // 键盘快捷键：Ctrl/Cmd + Alt + A 截图
  const handleKeydown = e => {
    // Ctrl/Cmd + Alt + A 触发截图
    if ((e.ctrlKey || e.metaKey) && e.altKey && (e.key === 'a' || e.key === 'A')) {
      e.preventDefault()
      // 只在有会话时触发截图
      if (props.session) {
        messageInputRef.value?.triggerScreenshot?.()
      }
    }
  }
  window.addEventListener('keydown', handleKeydown)

  // 点击外部关闭表情弹窗
  const handleClickOutside = e => {
    if (showEmojiPopover.value) {
      const popover = document.querySelector('.emoji-popover')
      if (popover && !popover.contains(e.target)) {
        showEmojiPopover.value = false
        emojiTargetMessage.value = null
      }
    }
  }
  document.addEventListener('click', handleClickOutside)

  // 清理函数（在组件卸载时调用）
  onUnmounted(() => {
    isUnmounted.value = true // 标记组件已卸载
    window.removeEventListener('keydown', handleKeydown)
    document.removeEventListener('click', handleClickOutside)
  })
})
</script>

<style scoped lang="scss">
// ============================================================================
// 容器
// ============================================================================
.chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  flex: 1;
  min-width: 0;
  min-height: 0; // 修复 flex 子元素高度问题
  overflow: hidden; // 防止内容溢出
  background: var(--dt-bg-body);
  position: relative;
  z-index: var(--dt-z-base); // 使用设计令牌替代魔法值


  &.is-dragging {
    background: #e6f7ff;
    border: 2px dashed #1890ff;

    &::after {
      content: '释放以上传文件';
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      font-size: 18px;
      color: #1890ff;
      font-weight: 600;
      pointer-events: none;
    }
  }

  // 拖拽进入状态 - 更明显的视觉反馈
  &.is-drag-over {
    background: rgba(24, 144, 255, 0.08);
    box-shadow: inset 0 0 0 2px var(--dt-brand-color, #1890ff);
    transition: all 0.2s ease;

    &::before {
      content: '';
      position: absolute;
      inset: 0;
      background: radial-gradient(circle at center, rgba(24, 144, 255, 0.1) 0%, transparent 70%);
      pointer-events: none;
      animation: pulse-drag 1.5s ease-in-out infinite;
    }

    &::after {
      content: '拖放文件到此处';
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      padding: 16px 24px;
      background: var(--dt-brand-color, #1890ff);
      color: #fff;
      font-size: 16px;
      font-weight: 500;
      border-radius: var(--dt-radius-md);
      box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
      pointer-events: none;
      z-index: 100;
      display: flex;
      align-items: center;
      gap: 8px;

      .material-icons-outlined {
        font-size: 20px;
      }
    }
  }
}

.main-container {
  display: flex;
  flex: 1;
  min-height: 0; // flex: 1 配合 min-height: 0 正确处理高度
  overflow: hidden;
}

.chat-viewport {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  background: var(--dt-bg-card);
  transition: all 0.3s var(--dt-ease-out);

  &.with-pinned-panel {
    flex: 0 0 calc(100% - 320px);
  }
}

// 置顶面板滑入动画
.slide-left-enter-active,
.slide-left-leave-active {
  transition: all 0.3s var(--dt-ease-out);
}

.slide-left-enter-from {
  transform: translateX(100%);
  opacity: 0;
}

.slide-left-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

// ============================================================================
// 空状态
// ============================================================================
.empty-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  flex: 1;
  padding: 60px 20px;
  text-align: center;

  :deep(.empty-state) {
    width: 100%;
    max-width: 400px;
  }
}

// ============================================================================
// 动画
// ============================================================================
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all var(--dt-transition-slow);
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(100%);
  opacity: 0;
}

.slide-right-enter-active,
.slide-right-leave-active {
  transition: all var(--dt-transition-slow);
}

.slide-right-enter-from,
.slide-right-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

// ============================================================================
// 暗色模式
// ============================================================================
.dark .chat-viewport {
  background: var(--dt-bg-card-dark);
}

.dark .multi-select-toolbar {
  background: linear-gradient(135deg, #2d2d2d 0%, #1e1e1e 100%);
  border-color: rgba(255, 255, 255, 0.1);
  box-shadow: 0 -8px 24px rgba(0, 0, 0, 0.3);

  .selection-info {
    background: linear-gradient(135deg, rgba(0, 137, 255, 0.2) 0%, rgba(0, 137, 255, 0.15) 100%);
    border-color: rgba(0, 137, 255, 0.3);

    .selection-text {
      color: #ffffff;
    }
  }

  .actions {
    .toolbar-btn {
      &--forward {
        background: linear-gradient(135deg, rgba(2, 132, 199, 0.15) 0%, rgba(2, 132, 199, 0.1) 100%);
        color: #38bdf8;
        border-color: rgba(2, 132, 199, 0.3);

        &:hover:not(:disabled) {
          background: linear-gradient(135deg, #0284c7 0%, #0ea5e9 100%);
          color: #fff;
        }
      }

      &--combine {
        background: linear-gradient(135deg, rgba(217, 119, 6, 0.15) 0%, rgba(217, 119, 6, 0.1) 100%);
        color: #fbbf24;
        border-color: rgba(217, 119, 6, 0.3);

        &:hover:not(:disabled) {
          background: linear-gradient(135deg, #d97706 0%, #f59e0b 100%);
          color: #fff;
        }
      }

      &--delete {
        background: linear-gradient(135deg, rgba(220, 38, 38, 0.15) 0%, rgba(220, 38, 38, 0.1) 100%);
        color: #f87171;
        border-color: rgba(220, 38, 38, 0.3);

        &:hover:not(:disabled) {
          background: linear-gradient(135deg, #dc2626 0%, #ef4444 100%);
          color: #fff;
        }
      }

      &--cancel {
        background: rgba(255, 255, 255, 0.06);
        color: #999;

        &:hover {
          background: rgba(255, 255, 255, 0.1);
          color: #e8e8e8;
        }
      }
    }
  }

  .toolbar-divider {
    background: rgba(255, 255, 255, 0.1);
  }
}

// 拖拽动画
@keyframes pulse-drag {

  0%,
  100% {
    opacity: 0.3;
    transform: scale(1);
  }

  50% {
    opacity: 0.6;
    transform: scale(1.02);
  }
}

// 快捷表情选择器
.emoji-popover {
  position: fixed;
  z-index: 9999;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-md);
  box-shadow: var(--dt-shadow-3);
  border: 1px solid var(--dt-border-light);
  padding: 12px;
  min-width: 280px;
  animation: popoverFadeIn 0.2s var(--dt-ease-out);

  .dark & {
    background: #2d2d2d;
    border-color: #3e3e3e;
  }

  .emoji-popover-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid var(--dt-border-light);

    span {
      font-size: 14px;
      font-weight: 500;
      color: var(--dt-text-primary);
    }

    .close-icon {
      cursor: pointer;
      color: var(--dt-text-tertiary);
      font-size: 16px;

      &:hover {
        color: var(--dt-text-secondary);
      }
    }
  }

  .emoji-grid {
    display: grid;
    grid-template-columns: repeat(8, 1fr);
    gap: 8px;

    .emoji-item {
      font-size: 24px;
      text-align: center;
      padding: 8px 4px;
      border-radius: var(--dt-radius-sm);
      cursor: pointer;
      transition: all 0.15s;
      user-select: none;

      &:hover {
        background: var(--dt-bg-hover);
        transform: scale(1.15);
      }

      &:active {
        transform: scale(1.05);
      }
    }
  }
}

@keyframes popoverFadeIn {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
