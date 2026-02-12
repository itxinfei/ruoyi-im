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
        <div class="chat-viewport" :class="{ 'with-pinned-panel': showPinnedPanel }" role="region"
          :aria-label="ARIA_LABELS.chatPanel">
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

      <!-- 群组详情弹窗 - Teleport 到 body 避免 contain: layout 限制 fixed 定位 -->
      <Teleport to="body">
        <GroupDetailDialog v-model:visible="showGroupDetail" :group-id="session?.targetId"
          @refresh-group="handleRefreshGroup" @show-files="handleShowGroupFiles"
          @show-announcement="handleShowGroupAnnouncement" />
      </Teleport>

      <!-- 隐藏的文件上传 input -->
      <input ref="fileInputRef" type="file" class="hidden-input" @change="handleFileUpload">
      <input ref="imageInputRef" type="file" class="hidden-input" accept="image/*" @change="handleImageUpload">

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
import { Close } from '@element-plus/icons-vue'
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
import { ARIA_LABELS } from '@/config/a11y'
import ExportChatDialog from '@/components/Chat/ExportChatDialog.vue'
import MultiSelectToolbar from '@/components/Chat/MultiSelectToolbar.vue'
import ImageViewerDialog from '@/components/Chat/ImageViewerDialog.vue'
import { batchForwardMessages } from '@/api/im/message'
import { markMessage, setTodoReminder } from '@/api/im/marker'
import {
  useChatMessages,
  useChatCommands,
  useChatDialogs,
  useChatSend,
  useChatWebSocket,
  useChatTyping,
  useChatSessionOps,
  useChatDragDrop
} from '@/composables/useChat'
import { ElMessage, ElMessageBox } from 'element-plus'
import { parseMessageContent } from '@/utils/message'

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
const sessionId = computed(() => props.session?.id)

// ==================== Composables ====================
const { messages, loading, noMore, loadHistory, loadMore } = useChatMessages(sessionId, currentUser)

const {
  sending, sendText, sendImage, sendFile, sendVideo,
  sendVoice, sendScreenshot, sendLocation, retryMessage
} = useChatSend(sessionId, currentUser)

const {
  replyingMessage, editingMessage,
  copy, reply: cmdReply, cancelReply, forward, batchForward,
  recall, deleteMessage, edit, cancelEdit, confirmEdit,
  at, addReaction, addToTodo, favorite, unfavorite, pin
} = useChatCommands(currentUser, computed(() => props.session?.type))

const {
  showVoiceCall, showVideoCall, isIncomingCall, remoteCallUser,
  showChatSearch, showFilesPanel, showChatHistory, showAnnouncementDialog,
  showExportDialog, showGroupFilesPanel, showCombineDetail,
  combineMessages, combineConversationTitle,
  showImagePreview, imagePreviewIndex,
  openVoiceCall, openVideoCall, openCombineDetail, openImagePreview
} = useChatDialogs()

const {
  typingUsers, handleInput, sendMyStopTypingStatus,
  initTypingListener, resetTypingState, cleanup: cleanupTyping
} = useChatTyping(sessionId)

const { initListeners } = useChatWebSocket(sessionId, currentUser)
const { pinSession, muteSession, clearMessages } = useChatSessionOps(computed(() => props.session))
const { isDragging, isDragOver, handleDragEnter, handleDragOver, handleDragLeave, handleDrop, handlePaste } = useChatDragDrop(sendImage, sendFile)

// ==================== 本地状态 ====================
const showGroupDetail = ref(false)
const msgListRef = ref(null)
const forwardDialogRef = ref(null)
const isUnmounted = ref(false)
const showSearchPanel = ref(false)
const fileInputRef = ref(null)
const imageInputRef = ref(null)
const messageInputRef = ref(null)
const showPinnedPanel = ref(false)
const isMultiSelectModeActive = ref(false)

const emit = defineEmits(['show-user'])

// ==================== Computed ====================
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

const pinnedCount = computed(() => messages.value.filter(m => m.isPinned).length)
const isMultiSelectMode = computed(() => (store.getters['im/message/selectedMessageCount'] || 0) > 0)
const selectedMessages = computed(() => store.getters['im/message/selectedMessageList'] || [])

// ==================== 快捷表情 ====================
const QUICK_EMOJIS = ['👍', '👏', '❤️', '😂', '😮', '😢', '😡', '🎉']
const showEmojiPopover = ref(false)
const emojiPopoverPosition = ref({ x: 0, y: 0 })
const emojiTargetMessage = ref(null)

const handleShowEmojiPicker = msg => {
  emojiTargetMessage.value = msg
  const inputArea = document.querySelector('.chat-input-container')
  if (inputArea) {
    const rect = inputArea.getBoundingClientRect()
    emojiPopoverPosition.value = { x: rect.right - 300, y: rect.top - 220 }
  } else {
    emojiPopoverPosition.value = { x: window.innerWidth / 2 - 140, y: window.innerHeight / 2 - 110 }
  }
  showEmojiPopover.value = true
}

const handleSelectEmoji = async emoji => {
  if (!emojiTargetMessage.value) { return }
  await addReaction(emojiTargetMessage.value, emoji, 'EMOJI')
  showEmojiPopover.value = false
  emojiTargetMessage.value = null
}

// ==================== 消息发送代理 ====================
const handleSend = content => {
  sendMyStopTypingStatus()
  sendText(content, { replyToMessageId: replyingMessage.value?.id })
}

const handleSendVoice = ({ file, duration }) => {
  sendMyStopTypingStatus()
  sendVoice({ file, duration }, { replyToMessageId: replyingMessage.value?.id })
}

const handleImageUpload = payload => { sendMyStopTypingStatus(); sendImage(payload) }
const handleFileUpload = payload => { sendMyStopTypingStatus(); sendFile(payload) }
const handleVideoUpload = payload => { sendMyStopTypingStatus(); sendVideo(payload, { replyToMessageId: replyingMessage.value?.id }) }
const handleScreenshotUpload = formData => { sendMyStopTypingStatus(); sendScreenshot(formData) }
const handleSendLocation = location => { sendMyStopTypingStatus(); sendLocation(location, { replyToMessageId: replyingMessage.value?.id }) }

const handleRetry = msg => retryMessage(msg)
const handleCancelReply = () => cancelReply()
const handleCancelEdit = () => cancelEdit()

const handleLoadMore = async () => {
  const oldHeight = msgListRef.value?.$refs?.listRef?.scrollHeight
  const result = await loadMore()
  if (result && result.length > 0) {
    msgListRef.value?.maintainScroll(oldHeight)
  }
}

// ==================== 通话处理 ====================
const handleStartCall = () => {
  if (!props.session) { return ElMessage.warning('请先选择会话') }
  const user = { userId: props.session.targetId, userName: props.session.name, avatar: props.session.avatar }
  openVoiceCall(user, false)
}

const handleStartVideo = () => {
  if (!props.session) { return ElMessage.warning('请先选择会话') }
  const user = { userId: props.session.targetId, userName: props.session.name, avatar: props.session.avatar }
  openVideoCall(user, false)
}

const handleVoiceCall = () => handleStartCall()
const handleVideoCall = () => handleStartVideo()

// ==================== 会话操作代理 ====================
const handlePinSession = () => pinSession()
const handleMuteSession = () => muteSession()
const handleClearMessages = () => clearMessages()

// ==================== 页面事件处理 ====================
const handleToggleDetail = () => {
  if (props.session?.type === 'GROUP') {
    showGroupDetail.value = true
  } else if (props.session) {
    emit('show-user', props.session.targetId)
  }
}

const handleToggleSidebar = tab => { console.log('toggle-sidebar:', tab) }
const handleShowUser = userId => emit('show-user', userId)
const handleSearchMessages = () => { showChatSearch.value = true }
const handleShowHistory = () => { showChatHistory.value = true }

const handleShowFiles = () => {
  if (props.session?.type === 'GROUP') {
    showGroupFilesPanel.value = true
  } else {
    showFilesPanel.value = true
  }
}

const handleCreateAnnouncement = () => {
  if (props.session?.type === 'GROUP') {
    showAnnouncementDialog.value = true
  } else {
    ElMessage.warning('只有群聊可以发布公告')
  }
}

const handleRefreshGroup = async () => {
  try {
    await store.dispatch('im/conversation/loadConversations')
    ElMessage.success('群组信息已刷新')
  } catch (error) {
    console.error('刷新群组信息失败:', error)
  }
}

const handleShowGroupFiles = () => { showGroupFilesPanel.value = true }
const handleShowGroupAnnouncement = () => { showAnnouncementDialog.value = true }

// ==================== 图片预览 ====================
const handleImagePreview = imageUrl => {
  const index = conversationImages.value.indexOf(imageUrl)
  openImagePreview(index, conversationImages.value)
}

// ==================== 滚动定位 ====================
const handleScrollToMessage = messageId => { msgListRef.value?.scrollToMessage(messageId) }
const handleScrollToPinnedMessage = messageId => { msgListRef.value?.scrollToMessage(messageId) }

const handleJumpToMessage = message => {
  msgListRef.value?.scrollToMessage(message.id || message.messageId)
  showSearchPanel.value = false
  showChatHistory.value = false
}

// ==================== 置顶消息更新 ====================
const handlePinnedUpdate = ({ messageId, isPinned }) => {
  if (props.session?.id) {
    store.commit('im/message/UPDATE_MESSAGE', {
      sessionId: props.session.id,
      message: { id: messageId, isPinned }
    })
  }
}

// ==================== 回复和编辑 ====================
const handleReply = message => {
  cmdReply(message)
  setTimeout(() => {
    if (isUnmounted.value) { return }
    messageInputRef.value?.focus()
  }, 50)
}

const handleEdit = message => edit(message)

const handleEditConfirm = async content => {
  if (!editingMessage.value) { return }
  const messageId = editingMessage.value.id
  try {
    await confirmEdit(content)
    if (props.session?.id) {
      store.commit('im/message/UPDATE_MESSAGE', {
        sessionId: props.session.id,
        message: { id: messageId, content, isEdited: true }
      })
    }
  } catch {
    // confirmEdit 已处理错误提示
  }
}

const handleReEdit = ({ content }) => {
  if (!content) { return }
  edit({ id: `reedit-${Date.now()}`, content, isReEdit: true })
  messageInputRef.value?.setContent(content)
  nextTick(() => {
    if (isUnmounted.value) { return }
    messageInputRef.value?.focus()
  })
  ElMessage.info('已恢复消息内容，修改后点击发送')
}

const handleAt = message => {
  if (!message) { return }
  messageInputRef.value?.insertAt(message.senderName)
}

// ==================== 已读上报 ====================
const handleMarkRead = async msg => {
  try {
    await store.dispatch('im/message/markMessageAsRead', {
      conversationId: props.session.id,
      messageId: msg.id
    })
    msg.isRead = true
  } catch (e) {
    console.warn('上报已读状态失败', e)
  }
}

// ==================== 消息操作 ====================
const handleDelete = async message => {
  await deleteMessage(message)
}

const handleRecall = async message => {
  await recall(message)
}

// ==================== 多选操作 ====================
const handleMultiSelect = msg => {
  store.commit('im/message/TOGGLE_MESSAGE_SELECTION', msg.id)
  if (!isMultiSelectModeActive.value && store.getters['im/message/selectedMessageCount'] > 0) {
    isMultiSelectModeActive.value = true
    if (navigator.vibrate) { navigator.vibrate(50) }
  }
}

const handleToggleMultiSelect = active => {
  isMultiSelectModeActive.value = active
  if (!active) { store.commit('im/message/CLEAR_MESSAGE_SELECTION') }
}

const handleClearSelection = () => {
  store.commit('im/message/CLEAR_MESSAGE_SELECTION')
  isMultiSelectModeActive.value = false
}

const handleBatchForward = () => {
  const messageIds = selectedMessages.value.map(msg => msg.id)
  if (messageIds.length > 0) { forwardDialogRef.value?.openForBatch(messageIds, 'batch') }
}

const handleCombineForward = () => {
  const messageIds = selectedMessages.value.map(msg => msg.id)
  if (messageIds.length > 0) { forwardDialogRef.value?.openForBatch(messageIds, 'combine') }
}

const handleBatchDelete = async () => {
  const selected = selectedMessages.value
  if (selected.length === 0) { return }
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selected.length} 条消息吗？`, '批量删除', { type: 'warning' })
    for (const msg of selected) { await deleteMessage(msg) }
    ElMessage.success(`已删除 ${selected.length} 条消息`)
    handleClearSelection()
  } catch (e) {
    if (e !== 'cancel') { ElMessage.error('删除失败') }
  }
}

// ==================== 转发处理 ====================
const handleForwardConfirm = async ({ message, targetSessionId }) => {
  try {
    await store.dispatch('im/message/forwardMessage', { messageId: message.id, targetConversationId: targetSessionId })
    ElMessage.success('转发成功')
  } catch (error) {
    ElMessage.error('转发失败')
    console.error(error)
  }
}

const handleBatchForwardConfirm = async ({ messageIds, targetSessionId, forwardType }) => {
  try {
    await batchForwardMessages({ messageIds, toConversationId: targetSessionId, forwardType, content: '' })
    ElMessage.success(`${forwardType === 'combine' ? '合并转发' : '逐条转发'}成功`)
    handleClearSelection()
  } catch (error) {
    ElMessage.error('转发失败')
    console.error(error)
  }
}

// ==================== 合并消息详情 ====================
const handleViewCombine = msg => {
  if (msg.messages && msg.messages.length > 0) {
    openCombineDetail(msg.messages, `${msg.senderName}的聊天记录`)
  } else {
    ElMessage.warning('无法查看聊天记录详情')
  }
}

const handleCombineForwardDetail = messages => {
  const messageIds = messages.map(m => m.id)
  if (messageIds.length > 0) { forwardDialogRef.value?.openForBatch(messageIds, 'combine') }
}

// ==================== 标记消息 ====================
const handleAddToTodo = async msg => await addToTodo(msg)
const handleFavorite = async msg => await favorite(msg)

const handleMarkMessage = async msg => {
  try {
    await ElMessageBox({
      title: '标记消息',
      message: h('div', { class: 'marker-options' }, [
        h('p', { style: 'margin-bottom: 16px; color: #64748b;' }, '请选择标记类型：'),
        h('div', { class: 'marker-buttons', style: 'display: flex; gap: 8px; flex-wrap: wrap;' }, [
          h('el-button', {
            onClick: () => { markMessageAction(msg, 'FLAG', '#ff4d4f'); ElMessageBox.close() },
            style: 'flex: 1; min-width: 80px;'
          }, () => [
            h('span', { class: 'material-icons-outlined', style: 'vertical-align: middle; margin-right: 4px; color: #ff4d4f;' }, 'flag'),
            '标记'
          ]),
          h('el-button', {
            type: 'warning',
            onClick: () => { markMessageAction(msg, 'IMPORTANT', '#faad14'); ElMessageBox.close() },
            style: 'flex: 1; min-width: 80px;'
          }, () => [
            h('span', { class: 'material-icons-outlined', style: 'vertical-align: middle; margin-right: 4px;' }, 'star'),
            '重要'
          ]),
          h('el-button', {
            type: 'success',
            onClick: () => {
              setTodoReminder({ messageId: msg.id, remindTime: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(), remark: msg.content?.substring(0, 50) || '消息待办' })
              ElMessageBox.close()
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
  } catch {
    // 用户取消
  }
}

const markMessageAction = async (msg, markerType, color) => {
  try {
    await markMessage({ messageId: msg.id, markerType, color })
    ElMessage.success(markerType === 'FLAG' ? '已标记消息' : '已标记为重要')
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

// ==================== 提醒未读 ====================
const handleRemindUnread = async ({ conversationId, messageId, unreadMembers }) => {
  if (!unreadMembers || unreadMembers.length === 0) { return ElMessage.info('暂无未读成员') }
  try {
    const mentionText = unreadMembers.map(m => `@${m.nickname || m.userName}`).join(' ')
    await store.dispatch('im/message/sendMessage', {
      sessionId: conversationId,
      type: 'TEXT',
      content: `${mentionText} 请查看上方消息`,
      replyToMessageId: messageId,
      atUserIds: unreadMembers.map(m => m.userId)
    })
    ElMessage.success(`已提醒 ${unreadMembers.length} 位成员`)
  } catch (error) {
    console.error('提醒失败:', error)
    ElMessage.error('提醒失败，请稍后重试')
  }
}

// ==================== 文件操作 ====================
const handleOpenFile = file => { if (file.url) { window.open(file.url, '_blank') } }

const handleDownloadFile = file => {
  if (Array.isArray(file)) {
    ElMessage.info(`正在下载 ${file.length} 个文件...`)
  } else if (file.url) {
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

const handleForwardFile = file => { forwardDialogRef.value?.open([{ type: 'FILE', content: JSON.stringify(file) }]) }
const handleClearHistory = async () => { await clearMessages(); showChatHistory.value = false }

// ==================== 命令分发器 ====================
const handleCommand = (cmd, msg) => {
  const commands = {
    forward: () => forwardDialogRef.value?.open(msg),
    reply: () => handleReply(msg),
    recall: () => handleRecall(msg),
    delete: () => handleDelete(msg),
    edit: () => handleEdit(msg),
    'mark-read': () => handleMarkRead(msg),
    todo: () => handleAddToTodo(msg),
    favorite: () => handleFavorite(msg),
    mark: () => handleMarkMessage(msg),
    'multi-select': () => handleMultiSelect(msg),
    'view-combine': () => handleViewCombine(msg),
    emoji: () => handleShowEmojiPicker(msg)
  }
  commands[cmd]?.()
}

// ==================== 会话切换 ====================
watch(() => props.session?.id, () => {
  resetTypingState()
})

// ==================== 生命周期 ====================
onMounted(() => {
  if (props.session) { loadHistory() }

  // 初始化 WebSocket 监听
  initListeners({
    onNewMessage: () => nextTick(() => msgListRef.value?.scrollToBottom())
  })

  // 初始化输入状态监听
  initTypingListener(currentUser)

  // 请求浏览器通知权限
  import('@/utils/messageNotification').then(({ requestNotificationPermission }) => {
    requestNotificationPermission().catch(() => {})
  })

  // 键盘快捷键：Ctrl/Cmd + Alt + A 截图
  const handleKeydown = e => {
    if ((e.ctrlKey || e.metaKey) && e.altKey && (e.key === 'a' || e.key === 'A')) {
      e.preventDefault()
      if (props.session) { messageInputRef.value?.triggerScreenshot?.() }
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

  onUnmounted(() => {
    isUnmounted.value = true
    cleanupTyping()
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
  min-width: 0; // 允许收缩，关键修复
  min-height: 0; // 修复 flex 子元素高度问题
  overflow: hidden; // 防止内容溢出
  background: var(--dt-bg-body);
  position: relative;
  z-index: var(--dt-z-base); // 使用设计令牌替代魔法值
  contain: layout style; // 性能优化：限制浏览器重排范围


  &.is-dragging {
    background: var(--dt-brand-bg);
    border: 2px dashed var(--dt-brand-color);

    &::after {
      content: '释放以上传文件';
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      font-size: 18px;
      color: var(--dt-brand-color);
      font-weight: 600;
      pointer-events: none;
    }
  }

  // 拖拽进入状态 - 更明显的视觉反馈
  &.is-drag-over {
    background: var(--dt-brand-light);
    box-shadow: inset 0 0 0 2px var(--dt-brand-color);
    transition: all 0.2s ease;

    &::before {
      content: '';
      position: absolute;
      inset: 0;
      background: radial-gradient(circle at center, var(--dt-brand-light) 0%, transparent 70%);
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
      background: var(--dt-brand-color);
      color: var(--dt-bubble-right-text);
      font-size: 16px;
      font-weight: 500;
      border-radius: var(--dt-radius-md);
      box-shadow: var(--dt-shadow-brand);
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
  contain: layout; // 性能优化
}

.chat-viewport {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0; // 允许收缩，关键修复
  min-height: 0; // 允许收缩，关键修复
  background: var(--dt-bg-card);
  transition: all 0.3s var(--dt-ease-out);
  overflow: hidden; // 防止溢出

  &.with-pinned-panel {
    flex: 1 1 auto;
    min-width: 0;
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
  background: linear-gradient(135deg, var(--dt-multiselect-dark-bg-start) 0%, var(--dt-multiselect-dark-bg-end) 100%);
  border-color: var(--dt-multiselect-dark-border);
  box-shadow: 0 -8px 24px var(--dt-multiselect-dark-shadow);

  .selection-info {
    background: var(--dt-multiselect-selection-info-bg);
    border-color: var(--dt-multiselect-selection-info-border);

    .selection-text {
      color: var(--dt-text-inverse);
    }
  }

  .actions {
    .toolbar-btn {
      &--forward {
        background: var(--dt-multiselect-forward-bg);
        color: #38bdf8;
        border-color: var(--dt-multiselect-forward-border);

        &:hover:not(:disabled) {
          background: linear-gradient(135deg, #0284c7 0%, #0ea5e9 100%);
          color: var(--dt-text-inverse);
        }
      }

      &--combine {
        background: var(--dt-multiselect-combine-bg);
        color: #fbbf24;
        border-color: var(--dt-multiselect-combine-border);

        &:hover:not(:disabled) {
          background: linear-gradient(135deg, #d97706 0%, #f59e0b 100%);
          color: var(--dt-text-inverse);
        }
      }

      &--delete {
        background: var(--dt-multiselect-delete-bg);
        color: #f87171;
        border-color: var(--dt-multiselect-delete-border);

        &:hover:not(:disabled) {
          background: linear-gradient(135deg, #dc2626 0%, #ef4444 100%);
          color: var(--dt-text-inverse);
        }
      }

      &--cancel {
        background: var(--dt-multiselect-cancel-bg);
        color: var(--dt-text-light-gray);

        &:hover {
          background: var(--dt-multiselect-cancel-hover-bg);
          color: var(--dt-text-dark-mode);
        }
      }
    }
  }

  .toolbar-divider {
    background: var(--dt-multiselect-divider-bg);
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
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
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

// 隐藏的文件输入框
.hidden-input {
  position: absolute;
  width: 0;
  height: 0;
  padding: 0;
  margin: 0;
  overflow: hidden;
  border: 0;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
}
</style>
