<template>
  <div
    class="chat-panel"
    :class="{ 'is-dragging': isDragging, 'is-drag-over': isDragOver }"
    @dragover.prevent="handleDragOver"
    @dragenter.prevent="handleDragEnter"
    @dragleave.prevent="handleDragLeave"
    @drop.prevent="handleDrop"
    @paste="handlePaste"
  >
    <div v-if="!session" class="empty-placeholder">
      <EmptyState
        type="chat"
        title="选择一个会话开始聊天"
        description="从左侧列表选择联系人或群组，开始你的对话"
        :compact="false"
      />
    </div>
    <template v-else>
      <div class="main-container">
        <!-- 左侧聊天主体 -->
        <div class="chat-viewport" :class="{ 'with-pinned-panel': showPinnedPanel }">
          <ChatHeader
            :session="session"
            :typing-users="typingUsers"
            :pinned-count="pinnedCount"
            @toggle-sidebar="handleToggleDetail"
            @multiselect="handleToggleMultiSelect"
            @toggle-multi-select="handleToggleMultiSelect"
            @toggle-pinned="showPinnedPanel = !showPinnedPanel"
            @clear-selection="clearSelection"
            @announcement="dialogStates.showAnnouncementDialog = true"
            @history="dialogStates.showChatHistory = true"
            @search="dialogStates.showChatSearch = true"
            @files="handleShowFiles"
            @pin="handlePinSession"
            @mute="handleMuteSession"
            @clear="handleClearMessages"
            @voice-call="handleVoiceCall"
            @video-call="handleVideoCall"
            @scroll-to-message="handleScrollToMessage"
          />
          <MessageList
            ref="msgListRef"
            :session-id="session?.id"
            :messages="messages"
            :loading="loading"
            :current-user="currentUser"
            :session-type="session?.type"
            :multi-select-mode="isMultiSelectModeActive"
            @command="handleCommand"
            @at="handleAt"
            @load-more="loadMore"
            @show-user="$emit('show-user', $event)"
            @retry="handleRetry"
            @preview="handleImagePreview"
          />
          <MessageInput
            ref="messageInputRef"
            :session="session"
            :sending="sending"
            :replying-message="replyingMessage"
            :editing-message="editingMessage"
            @send="handleSend"
            @send-voice="handleSendVoice"
            @cancel-reply="handleCancelReply"
            @cancel-edit="handleCancelEdit"
            @edit-confirm="handleEditConfirm"
            @start-call="handleStartCall"
            @start-video="handleStartVideo"
            @upload-image="handleImageUpload"
            @upload-file="handleFileUpload"
            @upload-video="handleVideoUpload"
            @send-location="handleSendLocation"
            @send-screenshot="handleScreenshotUpload"
            @input="handleInput"
            @create-announcement="handleCreateAnnouncement"
          />
        </div>

        <!-- 置顶消息面板 -->
        <Transition name="slide-left">
          <PinnedMessagesPanel
            v-if="showPinnedPanel"
            :messages="messages"
            @close="showPinnedPanel = false"
            @scroll-to-message="handleScrollToPinnedMessage"
            @update="handlePinnedUpdate"
          />
        </Transition>
      </div>

      <!-- 群组详情弹窗 -->
      <GroupDetailDialog
        v-model="dialogStates.showGroupDetail"
        :group-id="session?.targetId"
      />

      <!-- 隐藏的文件上传 input -->
      <input type="file" ref="fileInputRef" style="display: none" @change="handleFileUpload" />
      <input type="file" ref="imageInputRef" style="display: none" accept="image/*" @change="handleImageUpload" />

      <!-- 转发对话框 -->
      <ForwardDialog
        ref="forwardDialogRef"
        @forward="handleForwardConfirm"
        @batch-forward="handleBatchForwardConfirm"
      />

      <!-- 通话对话框 -->
      <CallDialog ref="callDialogRef" :session="session" />

      <!-- 语音通话 -->
      <VoiceCallDialog
        v-model:visible="dialogStates.showVoiceCall"
        :remote-user="session"
        :is-incoming="isIncomingCall"
      />

      <!-- 视频通话 -->
      <VideoCallDialog
        v-model:visible="dialogStates.showVideoCall"
        :remote-user="session"
        :is-incoming="isIncomingCall"
      />

      <!-- 聊天记录面板 -->
      <ChatHistoryPanel
        :visible="dialogStates.showChatHistory"
        :conversation-id="session?.id"
        @close="dialogStates.showChatHistory = false"
        @jump-to-message="handleJumpToMessage"
        @clear-history="handleClearHistory"
      />

      <!-- 群公告对话框 -->
      <GroupAnnouncementDialog
        v-model="dialogStates.showAnnouncementDialog"
        :group-id="session?.targetId"
        :can-manage="session?.type === 'GROUP' && session?.memberRole === 'ADMIN'"
      />

      <!-- 搜索聊天记录面板 -->
      <ChatSearchPanel
        :visible="dialogStates.showSearchPanel"
        :session-id="session?.id"
        :messages="messages"
        @close="dialogStates.showSearchPanel = false"
        @jump-to-message="handleJumpToMessage"
      />

      <!-- 聊天内搜索弹窗 -->
      <ChatSearch
        v-model:visible="dialogStates.showChatSearch"
        :messages="messages"
        @select-message="handleScrollToMessage"
      />

      <!-- 查看文件面板 -->
      <ChatFilesPanel
        :visible="dialogStates.showFilesPanel"
        :session-id="session?.id"
        :messages="messages"
        @close="dialogStates.showFilesPanel = false"
        @open-file="handleOpenFile"
        @download-file="handleDownloadFile"
        @forward-file="handleForwardFile"
      />

      <!-- 群文件面板 -->
      <GroupFilePanel
        v-if="dialogStates.showGroupFilesPanel && session?.type === 'GROUP'"
        :group-id="session?.targetId"
        class="group-files-drawer"
      />

      <!-- 合并消息详情对话框 -->
      <CombineDetailDialog
        v-model="dialogStates.showCombineDetail"
        :messages="combineMessages"
        :conversation-title="combineConversationTitle"
        @forward="handleCombineForwardDetail"
      />

      <!-- 导出聊天记录对话框 -->
      <ExportChatDialog
        v-model="dialogStates.showExportDialog"
        :messages="messages"
        :contact-name="session?.peerName || session?.groupName || '聊天'"
      />

      <!-- 多选操作栏 -->
      <Transition name="slide-up">
        <div v-if="isMultiSelectModeActive" class="multi-select-toolbar">
          <div class="selection-info">
            <div class="selection-indicator"></div>
            <span class="selection-text">已选择 <strong>{{ selectedMessages.length }}</strong> 条消息</span>
          </div>
          <div class="actions">
            <button class="toolbar-btn toolbar-btn--forward" @click="handleBatchForward" :disabled="selectedMessages.length === 0">
              <span class="material-icons-outlined">share</span>
              <span>逐条转发</span>
            </button>
            <button class="toolbar-btn toolbar-btn--combine" @click="handleCombineForward" :disabled="selectedMessages.length === 0">
              <span class="material-icons-outlined">collections</span>
              <span>合并转发</span>
            </button>
            <button class="toolbar-btn toolbar-btn--delete" @click="handleBatchDelete" :disabled="selectedMessages.length === 0">
              <span class="material-icons-outlined">delete_outline</span>
              <span>删除</span>
            </button>
            <div class="toolbar-divider"></div>
            <button class="toolbar-btn toolbar-btn--cancel" @click="clearSelection">
              <span>取消</span>
            </button>
          </div>
        </div>
      </Transition>
    </template>
  </div>

  <!-- 图片预览器 -->
  <Teleport to="body">
    <el-image-viewer
      v-if="showImagePreview"
      :url-list="conversationImages"
      :initial-index="imagePreviewIndex"
      @close="closeImagePreview"
      :z-index="9999"
    />
  </Teleport>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, h, watch } from 'vue'
import { useStore } from 'vuex'
import { Share, Folder, Delete } from '@element-plus/icons-vue'
import { ElImageViewer, ElMessage, ElMessageBox } from 'element-plus'

// 组件导入
import ChatHeader from '@/components/Chat/ChatHeader.vue'
import MessageList from '@/components/Chat/MessageList.vue'
import MessageInput from '@/components/Chat/MessageInputRefactored.vue'
import PinnedMessagesPanel from '@/components/Chat/PinnedMessagesPanel.vue'
import ForwardDialog from '@/components/ForwardDialog/index.vue'
import CallDialog from '@/components/Chat/CallDialog.vue'
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

// API 导入
import {
  getMessages,
  batchForwardMessages,
  deleteMessage,
  clearConversationMessages
} from '@/api/im/message'
import { pinConversation, muteConversation } from '@/api/im/conversation'
import { addFavorite, removeFavorite } from '@/api/im/favorite'
import {
  markMessage,
  unmarkMessage,
  setTodoReminder,
  completeTodo,
  getUserTodoCount
} from '@/api/im/marker'

// Composables 导入
import { useImWebSocket } from '@/composables/useImWebSocket'
import { useTypingIndicator } from '@/composables/useTypingIndicator'
import { useFileUpload } from '@/composables/useFileUpload'

// Props / Emits
const props = defineProps({
  session: {
    type: Object,
    default: null,
    validator: (value) => {
      if (value === null) return true
      return typeof value.id === 'string' || typeof value.id === 'number'
    }
  }
})

const emit = defineEmits(['show-user'])

const store = useStore()
const currentUser = computed(() => store.getters['user/currentUser'])

// WebSocket hooks
const { onMessage, onTyping, onMessageStatus, onReaction, sendTyping, sendStopTyping } = useImWebSocket()

// Typing indicator composable
const {
  typingUsers,
  sendMyStopTypingStatus,
  handleInput: handleTypingInput
} = useTypingIndicator({
  sessionId: computed(() => props.session?.id),
  currentUser,
  sendTyping,
  sendStopTyping
})

// File upload composable
const {
  uploadImage: uploadImageFile,
  uploadFile: uploadNormalFile,
  uploadVideo: uploadVideoFile,
  uploadVoice: uploadVoiceFile,
  uploadScreenshot: uploadScreenshotFile
} = useFileUpload({
  currentUser,
  session: computed(() => props.session),
  messages: ref([]), // 将在下面初始化
  sendMessage: async (params) => {
    return await store.dispatch('im/message/sendMessage', params)
  },
  scrollToBottom: () => msgListRef.value?.scrollToBottom(),
  onUploadSuccess: () => {},
  onUploadError: (msg) => ElMessage.error(msg)
})

// ==================== 核心状态 ====================

const messages = ref([])
const loading = ref(false)
const sending = ref(false)
const noMore = ref(false)

// 状态引用（从 Vuex 获取）
const replyingMessage = computed(() => store.state.im.message.replyingMessage)
const editingMessage = ref(null)

// 组件引用
const msgListRef = ref(null)
const forwardDialogRef = ref(null)
const callDialogRef = ref(null)
const fileInputRef = ref(null)
const imageInputRef = ref(null)
const messageInputRef = ref(null)

// 弹窗状态
const dialogStates = ref({
  showGroupDetail: false,
  showVoiceCall: false,
  showVideoCall: false,
  showChatHistory: false,
  showAnnouncementDialog: false,
  showSearchPanel: false,
  showChatSearch: false,
  showFilesPanel: false,
  showExportDialog: false,
  showGroupFilesPanel: false,
  showCombineDetail: false
})

// 多选状态
const isMultiSelectModeActive = ref(false)
const selectedMessages = computed(() => store.getters['im/message/selectedMessageList'])

// 置顶面板
const showPinnedPanel = ref(false)
const pinnedCount = computed(() => messages.value.filter(m => m.isPinned).length)

// 图片预览
const showImagePreview = ref(false)
const imagePreviewIndex = ref(0)

const conversationImages = computed(() => {
  return messages.value
    .filter(m => {
      if (m.type !== 'IMAGE') return false
      try {
        const content = typeof m.content === 'string' ? JSON.parse(m.content) : m.content
        return content && (content.url || content.imageUrl)
      } catch {
        return false
      }
    })
    .map(m => {
      try {
        const content = typeof m.content === 'string' ? JSON.parse(m.content) : m.content
        return content.url || content.imageUrl
      } catch {
        return ''
      }
    })
    .filter(url => url)
})

// 通话相关
const isIncomingCall = ref(false)

// 合并转发相关
const combineMessages = ref([])
const combineConversationTitle = ref('')

// ==================== 工具函数 ====================

const transformMsg = (m) => {
  let isOwn = m.isSelf === true || m.isSelf === false ? m.isSelf : false
  if (m.isSelf !== true && m.isSelf !== false) {
    const msgSenderId = String(m.senderId || '')
    const currentUserId = String(currentUser.value?.id || '')
    isOwn = msgSenderId === currentUserId && msgSenderId !== ''
  }

  const messageType = m.type || m.messageType || 'TEXT'

  // 规范化名称和头像
  const senderName = m.senderName || m.senderNickname || m.nickname || m.userName || '未知用户'
  const senderAvatar = m.senderAvatar || m.avatar || ''

  let replyTo = m.replyTo || m.quotedMessage
  if (!replyTo && m.replyToMessageId) {
    const quoted = messages.value.find(msg => msg.id === m.replyToMessageId)
    if (quoted) {
      replyTo = { id: quoted.id, senderName: quoted.senderName, content: quoted.content }
    }
  }

  return {
    ...m,
    type: messageType,
    isOwn,
    senderName,
    senderAvatar,
    replyTo,
    timestamp: m.sendTime || m.createTime || m.timestamp || Date.now()
  }
}

// ==================== 消息操作 ====================

const loadHistory = async () => {
  if (!props.session?.id) return
  loading.value = true
  noMore.value = false

  try {
    const res = await store.dispatch('im/message/loadMessages', {
      sessionId: props.session.id,
      pageSize: 50
    })
    messages.value = (res || []).map(m => transformMsg(m))
  } finally {
    loading.value = false
    msgListRef.value?.scrollToBottom()
  }
}

const loadMore = async () => {
  if (loading.value || noMore.value) return
  const firstMsg = messages.value[0]
  if (!firstMsg) return

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

const handleSend = async (content) => {
  sendMyStopTypingStatus()

  const tempId = `temp-${Date.now()}`
  const tempMsg = {
    id: tempId,
    content,
    type: 'TEXT',
    senderId: currentUser.value?.id,
    senderName: currentUser.value?.nickName || currentUser.value?.userName || '我',
    senderAvatar: currentUser.value?.avatar,
    timestamp: Date.now(),
    isOwn: true,
    status: 'sending',
    readCount: 0
  }

  messages.value.push(tempMsg)
  store.commit('im/message/SET_REPLYING_MESSAGE', null)
  msgListRef.value?.scrollToBottom()

  try {
    const msg = await store.dispatch('im/message/sendMessage', {
      sessionId: props.session.id,
      type: 'TEXT',
      content,
      replyToMessageId: replyingMessage.value?.id
    })

    const index = messages.value.findIndex(m => m.id === tempId)
    if (index !== -1) {
      messages.value.splice(index, 1, { ...transformMsg(msg), status: null })
    }
  } catch (error) {
    const index = messages.value.findIndex(m => m.id === tempId)
    if (index !== -1) {
      messages.value[index].status = 'failed'
    }
    console.error('发送失败', error)
  }
}

const handleSendVoice = async ({ file, duration }) => {
  sendMyStopTypingStatus()
  await uploadVoiceFile({ file, duration })
}

const handleRetry = async (msg) => {
  if (msg.status !== 'failed') return
  msg.status = 'sending'

  try {
    const res = await store.dispatch('im/message/sendMessage', {
      sessionId: props.session.id,
      type: msg.type,
      content: typeof msg.content === 'object' ? JSON.stringify(msg.content) : msg.content
    })

    const realMsg = transformMsg(res)
    Object.assign(msg, { ...realMsg, status: null })
  } catch (error) {
    msg.status = 'failed'
    ElMessage.error('重试失败')
  }
}

// ==================== 事件处理 ====================

const handleCommand = (cmd, msg) => {
  const commandHandlers = {
    forward: () => forwardDialogRef.value?.open(msg),
    reply: () => handleReply(msg),
    recall: () => handleRecall(msg.id),
    delete: () => handleDelete(msg.id),
    edit: () => handleEdit(msg),
    'mark-read': () => handleMarkRead(msg),
    todo: () => handleAddToTodo(msg),
    favorite: () => handleFavorite(msg),
    mark: () => handleMarkMessage(msg),
    'multi-select': () => handleMultiSelect(msg),
    'view-combine': () => handleViewCombine(msg),
    export: () => (dialogStates.showExportDialog = true)
  }

  const handler = commandHandlers[cmd]
  if (handler) handler()
}

const handleReply = (message) => {
  store.commit('im/message/SET_REPLYING_MESSAGE', message)
}

const handleEdit = (message) => {
  editingMessage.value = message
}

const handleAt = (message) => {
  if (!message) return
  messageInputRef.value?.insertAt(message.senderName)
}

const handleEditConfirm = async (content) => {
  if (!editingMessage.value) return

  try {
    await store.dispatch('im/message/editMessage', {
      messageId: editingMessage.value.id,
      content
    })

    const index = messages.value.findIndex(m => m.id === editingMessage.value.id)
    if (index !== -1) {
      messages.value[index].content = content
      messages.value[index].isEdited = true
    }

    editingMessage.value = null
    ElMessage.success('已编辑')
  } catch (error) {
    console.error('编辑失败', error)
  }
}

const handleCancelReply = () => {
  store.commit('im/message/SET_REPLYING_MESSAGE', null)
}

const handleCancelEdit = () => {
  editingMessage.value = null
}

const handleInput = (content) => {
  handleTypingInput(content)
}

const handleMultiSelect = (msg) => {
  store.commit('im/message/TOGGLE_MESSAGE_SELECTION', msg.id)
  ElMessage.info('进入多选模式')
}

const handleMarkRead = async (msg) => {
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

const handleAddToTodo = async (msg) => {
  try {
    await setTodoReminder({
      messageId: msg.id,
      remindTime: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
      remark: msg.content?.substring(0, 50) || '消息待办'
    })
    ElMessage.success('已添加到待办事项')
    if (msg.markers) {
      msg.markers.push({ markerType: 'TODO', isCompleted: false })
    } else {
      msg.markers = [{ markerType: 'TODO', isCompleted: false }]
    }
  } catch (e) {
    console.error('添加待办失败', e)
    ElMessage.error('添加失败')
  }
}

const handleFavorite = async (msg) => {
  try {
    await addFavorite({
      messageId: msg.id,
      conversationId: props.session?.id,
      remark: '',
      tags: []
    })
    ElMessage.success('已收藏消息')
  } catch (e) {
    ElMessage.error('收藏失败')
  }
}

const handleMarkMessage = async (msg) => {
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
          ])
        ])
      ]),
      showCancelButton: true,
      showConfirmButton: false,
      closeOnClickModal: true,
      closeOnPressEscape: true
    })
  } catch (e) {
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

const handleViewCombine = (msg) => {
  if (msg.messages && msg.messages.length > 0) {
    combineMessages.value = msg.messages
    combineConversationTitle.value = `${msg.senderName}的聊天记录`
    dialogStates.showCombineDetail = true
  } else {
    ElMessage.warning('无法查看聊天记录详情')
  }
}

const handleCombineForwardDetail = (msgs) => {
  const messageIds = msgs.map(m => m.id)
  if (messageIds.length > 0) {
    forwardDialogRef.value?.openForBatch(messageIds, 'combine')
  }
}

const handleBatchForward = async () => {
  const messageIds = selectedMessages.value.map(msg => msg.id)
  if (messageIds.length === 0) return
  forwardDialogRef.value?.openForBatch(messageIds, 'batch')
}

const handleCombineForward = async () => {
  const messageIds = selectedMessages.value.map(msg => msg.id)
  if (messageIds.length === 0) return
  forwardDialogRef.value?.openForBatch(messageIds, 'combine')
}

const handleBatchDelete = async () => {
  const selected = selectedMessages.value
  if (selected.length === 0) return

  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selected.length} 条消息吗？`, '批量删除', { type: 'warning' })

    for (const msg of selected) {
      await deleteMessage(msg.id)
    }

    ElMessage.success(`已删除 ${selected.length} 条消息`)
    clearSelection()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleToggleMultiSelect = (active) => {
  isMultiSelectModeActive.value = active
  if (!active) {
    store.commit('im/message/CLEAR_MESSAGE_SELECTION')
  }
}

const clearSelection = () => {
  store.commit('im/message/CLEAR_MESSAGE_SELECTION')
  isMultiSelectModeActive.value = false
}

const handleDelete = async (messageId) => {
  try {
    await store.dispatch('im/message/deleteMessage', messageId)
    const index = messages.value.findIndex(m => m.id === messageId)
    if (index !== -1) {
      messages.value.splice(index, 1)
    }
  } catch (error) {
    console.error('删除失败', error)
  }
}

const handleRecall = async (messageId) => {
  try {
    await store.dispatch('im/message/recallMessage', messageId)
    const index = messages.value.findIndex(m => m.id === messageId)
    if (index !== -1) {
      messages.value[index].type = 'RECALLED'
      messages.value[index].content = '消息已撤回'
    }
  } catch (error) {
    console.error('撤回失败', error)
  }
}

// ==================== 拖拽上传 ====================

const isDragging = ref(false)
const isDragOver = ref(false)
let dragEnterCounter = 0

const handleDragEnter = (event) => {
  dragEnterCounter++
  const files = event.dataTransfer?.files
  if (files && files.length > 0) {
    const hasImage = Array.from(files).some(file => file.type.startsWith('image/'))
    if (hasImage) {
      isDragOver.value = true
    }
  }
}

const handleDragOver = (event) => {
  const files = event.dataTransfer?.files
  if (files && files.length > 0) {
    const hasImage = Array.from(files).some(file => file.type.startsWith('image/'))
    if (hasImage) {
      isDragging.value = true
    }
  }
}

const handleDragLeave = (event) => {
  dragEnterCounter--
  if (dragEnterCounter <= 0) {
    dragEnterCounter = 0
    isDragging.value = false
    isDragOver.value = false
  }
}

const handleDrop = async (event) => {
  isDragging.value = false
  isDragOver.value = false
  dragEnterCounter = 0

  const files = event.dataTransfer?.files
  if (!files || files.length === 0) return

  for (const file of files) {
    if (file.type.startsWith('image/')) {
      await uploadImageFile(file)
    }
  }
}

const handlePaste = async (event) => {
  const items = event.clipboardData?.items
  if (!items) return

  for (const item of items) {
    if (item.type.startsWith('image/')) {
      const file = item.getAsFile()
      if (file) {
        await uploadImageFile(file)
        break
      }
    }
  }
}

const handleImageUpload = async (payload) => {
  sendMyStopTypingStatus()
  if (payload instanceof File) {
    await uploadImageFile(payload)
  } else if (payload instanceof FormData) {
    await uploadImageFile(payload.get('file'))
  } else if (payload?.target?.files?.[0]) {
    await uploadImageFile(payload.target.files[0])
    if (payload.target) payload.target.value = ''
  }
}

const handleFileUpload = async (payload) => {
  sendMyStopTypingStatus()
  if (payload instanceof File) {
    await uploadNormalFile(payload)
  } else if (payload instanceof FormData) {
    await uploadNormalFile(payload.get('file'))
  } else if (payload?.target?.files?.[0]) {
    await uploadNormalFile(payload.target.files[0])
    if (payload.target) payload.target.value = ''
  }
}

const handleVideoUpload = async ({ file, url }) => {
  sendMyStopTypingStatus()
  await uploadVideoFile({ file, url })
}

const handleScreenshotUpload = async (formData) => {
  sendMyStopTypingStatus()
  const file = formData.get('file')
  if (!file) return

  await uploadImageFile(file)
}

const handleSendLocation = async ({ latitude, longitude, address }) => {
  sendMyStopTypingStatus()
  const tempId = `temp-location-${Date.now()}`
  const tempMsg = {
    id: tempId,
    type: 'LOCATION',
    content: { latitude, longitude, address: address || '未知位置' },
    senderId: currentUser.value?.id,
    senderName: currentUser.value?.nickName || currentUser.value?.userName || '我',
    senderAvatar: currentUser.value?.avatar,
    timestamp: Date.now(),
    isOwn: true,
    status: 'sending',
    readCount: 0
  }
  messages.value.push(tempMsg)
  msgListRef.value?.scrollToBottom()

  try {
    const msg = await store.dispatch('im/message/sendMessage', {
      sessionId: props.session.id,
      type: 'LOCATION',
      content: JSON.stringify({ latitude, longitude, address: address || '未知位置' }),
      replyToMessageId: replyingMessage.value?.id
    })

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

// ==================== 其他事件 ====================

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
    clearSelection()
  } catch (error) {
    ElMessage.error('转发失败')
    console.error(error)
  }
}

const handleToggleDetail = () => {
  if (props.session.type === 'GROUP') {
    dialogStates.showGroupDetail = true
  } else {
    emit('show-user', props.session.targetId)
  }
}

const handleShowFiles = () => {
  if (props.session?.type === 'GROUP') {
    dialogStates.showGroupFilesPanel = true
  } else {
    dialogStates.showFilesPanel = true
  }
}

const handleScrollToPinnedMessage = (messageId) => {
  if (msgListRef.value) {
    msgListRef.value.scrollToMessage(messageId)
  }
}

const handleScrollToMessage = (messageId) => {
  if (msgListRef.value) {
    msgListRef.value.scrollToMessage(messageId)
  }
}

const handlePinnedUpdate = ({ messageId, isPinned }) => {
  const index = messages.value.findIndex(m => m.id === messageId)
  if (index !== -1) {
    messages.value[index].isPinned = isPinned
  }
}

const handleJumpToMessage = (message) => {
  if (msgListRef.value) {
    msgListRef.value.scrollToMessage(message.id || message.messageId)
  }
  dialogStates.showSearchPanel = false
  dialogStates.showChatHistory = false
}

const handleOpenFile = (file) => {
  if (file.url) window.open(file.url, '_blank')
}

const handleDownloadFile = (file) => {
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

const handleForwardFile = (file) => {
  if (forwardDialogRef.value) {
    forwardDialogRef.value.open([{ type: 'FILE', content: JSON.stringify(file) }])
  }
}

const handleClearHistory = async () => {
  await handleClearMessages()
  dialogStates.showChatHistory = false
}

const handlePinSession = async () => {
  const currentSession = store.state.im.session?.currentSession
  if (!currentSession) return

  const newState = !currentSession.isPinned
  try {
    await pinConversation(currentSession.id, newState)
    store.commit('im/session/UPDATE_SESSION', { id: currentSession.id, isPinned: newState })
    ElMessage.success(newState ? '已置顶' : '已取消置顶')
  } catch (e) {
    ElMessage.error('操作失败，请重试')
  }
}

const handleMuteSession = async () => {
  const currentSession = store.state.im.session?.currentSession
  if (!currentSession) return

  const newState = !currentSession.isMuted
  try {
    await muteConversation(currentSession.id, newState)
    store.commit('im/session/UPDATE_SESSION', { id: currentSession.id, isMuted: newState })
    ElMessage.success(newState ? '已开启免打扰' : '已关闭免打扰')
  } catch (e) {
    ElMessage.error('操作失败，请重试')
  }
}

const handleClearMessages = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要清空与 ${props.session?.name || '对方'} 的聊天记录吗？`,
      '清空聊天记录',
      { type: 'warning', confirmButtonText: '确定清空', cancelButtonText: '取消' }
    )

    await clearConversationMessages(props.session?.id)
    messages.value = []
    noMore.value = false
    ElMessage.success('聊天记录已清空')
  } catch {
    // 用户取消
  }
}

const handleVoiceCall = () => {
  isIncomingCall.value = false
  dialogStates.showVoiceCall = true
}

const handleVideoCall = () => {
  isIncomingCall.value = false
  dialogStates.showVideoCall = true
}

const handleStartCall = () => {
  isIncomingCall.value = false
  dialogStates.showVoiceCall = true
}

const handleStartVideo = () => {
  isIncomingCall.value = false
  dialogStates.showVideoCall = true
}

const handleCreateAnnouncement = () => {
  if (props.session?.type === 'GROUP') {
    dialogStates.showAnnouncementDialog = true
  } else {
    ElMessage.warning('只有群聊可以发布公告')
  }
}

const handleImagePreview = (imageUrl) => {
  imagePreviewIndex.value = conversationImages.value.indexOf(imageUrl)
  showImagePreview.value = true
}

const closeImagePreview = () => {
  showImagePreview.value = false
}

// ==================== WebSocket 事件 ====================

onMessage((msg) => {
  if (msg.conversationId === props.session?.id) {
    const transformedMsg = transformMsg(msg)
    messages.value.push(transformedMsg)
    msgListRef.value?.scrollToBottom()

    if (!transformedMsg.isOwn) {
      import('@/utils/messageNotification').then(({ showMessageNotification, shouldNotify }) => {
        if (shouldNotify(msg, currentUser.value, props.session)) {
          let body = msg.content
          if (msg.type === 'IMAGE') body = '[图片]'
          else if (msg.type === 'FILE') body = '[文件]'
          else if (msg.type === 'RECALLED') body = '撤回了一条消息'

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

onTyping((data) => {
  if (data.conversationId !== props.session?.id) return
  if (data.userId === currentUser.value?.id) return

  // 处理输入状态 - 已在 useTypingIndicator 中统一处理
})

onMessageStatus((data) => {
  if (data.conversationId !== props.session?.id) return

  const index = messages.value.findIndex(m => m.id === data.messageId)
  if (index !== -1) {
    const statusMap = { 0: 'sending', 1: 'sending', 2: null, 3: 'read', 4: 'failed' }
    const sendStatus = parseInt(data.sendStatus)
    messages.value[index].status = statusMap[sendStatus] ?? null
  }
})

onReaction((data) => {
  store.dispatch('im/message/handleReactionUpdate', {
    messageId: data.messageId,
    emoji: data.emoji,
    userId: data.userId,
    userName: data.userName,
    userAvatar: data.userAvatar,
    isAdd: data.isAdd !== false
  })
})

// ==================== 生命周期 ====================

watch(() => props.session, () => {
  messages.value = []
  loadHistory()
})

onMounted(() => {
  if (props.session) loadHistory()

  // 请求浏览器通知权限
  import('@/utils/messageNotification').then(({ requestNotificationPermission }) => {
    requestNotificationPermission()
  })

  // 键盘快捷键：Ctrl/Cmd + Alt + A 截图
  const handleKeydown = (e) => {
    if ((e.ctrlKey || e.metaKey) && e.altKey && (e.key === 'a' || e.key === 'A')) {
      e.preventDefault()
      if (props.session) {
        messageInputRef.value?.triggerScreenshot?.()
      }
    }
  }
  window.addEventListener('keydown', handleKeydown)

  onUnmounted(() => {
    window.removeEventListener('keydown', handleKeydown)
  })
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// 容器
.chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  flex: 1;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
  background: var(--dt-bg-body);
  position: relative;
  z-index: var(--dt-z-base);

  &.is-dragging {
    background: #e6f7ff;
    border: 2px dashed #1890ff;

    &::after {
      content: '释放以上传图片';
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
      content: '拖放图片到此处';
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      padding: 16px 24px;
      background: var(--dt-brand-color, #1890ff);
      color: #fff;
      font-size: 16px;
      font-weight: 500;
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
      pointer-events: none;
      z-index: 100;
    }
  }
}

.main-container {
  display: flex;
  flex: 1;
  min-height: 0;
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

.slide-left-enter-from,
.slide-left-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

// 空状态
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

// 多选工具栏
.multi-select-toolbar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 72px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 -8px 24px rgba(0, 0, 0, 0.08);
  z-index: var(--dt-z-sticky);

  .selection-info {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 8px 16px;
    background: linear-gradient(135deg, var(--dt-brand-bg) 0%, var(--dt-brand-hover) 100%);
    border-radius: 24px;
    border: 1px solid rgba(22, 119, 255, 0.2);

    .selection-indicator {
      width: 12px;
      height: 12px;
      background: var(--dt-brand-color);
      border-radius: 50%;
      animation: selectionPulse 2s ease-in-out infinite;
      box-shadow: 0 0 8px rgba(22, 119, 255, 0.5);
    }

    @keyframes selectionPulse {
      0%, 100% { transform: scale(1); opacity: 1; }
      50% { transform: scale(1.2); opacity: 0.8; }
    }

    .selection-text {
      font-size: 14px;
      font-weight: 500;
      color: var(--dt-brand-color);

      strong { font-weight: 700; font-size: 16px; }
    }
  }

  .actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .toolbar-btn {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    height: 36px;
    padding: 0 16px;
    border-radius: 20px;
    border: none;
    background: transparent;
    color: #666;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    .material-icons-outlined { font-size: 16px; }

    &:disabled {
      opacity: 0.4;
      cursor: not-allowed;

      &:hover { transform: none; background: transparent; }
    }

    &--forward {
      background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
      color: #0284c7;
      border: 1px solid rgba(2, 132, 199, 0.2);

      &:hover:not(:disabled) {
        background: linear-gradient(135deg, #0284c7 0%, #0ea5e9 100%);
        color: #fff;
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(2, 132, 199, 0.3);
      }
    }

    &--combine {
      background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
      color: #d97706;
      border: 1px solid rgba(217, 119, 6, 0.2);

      &:hover:not(:disabled) {
        background: linear-gradient(135deg, #d97706 0%, #f59e0b 100%);
        color: #fff;
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(217, 119, 6, 0.3);
      }
    }

    &--delete {
      background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
      color: #dc2626;
      border: 1px solid rgba(220, 38, 38, 0.2);

      &:hover:not(:disabled) {
        background: linear-gradient(135deg, #dc2626 0%, #ef4444 100%);
        color: #fff;
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(220, 38, 38, 0.3);
      }
    }

    &--cancel {
      background: rgba(0, 0, 0, 0.04);
      color: #666;

      &:hover {
        background: rgba(0, 0, 0, 0.08);
        color: #1a1a1a;
      }
    }
  }

  .toolbar-divider {
    width: 1px;
    height: 24px;
    background: rgba(0, 0, 0, 0.1);
  }
}

// 动画
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all var(--dt-transition-slow);
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(100%);
  opacity: 0;
}

// 拖拽动画
@keyframes pulse-drag {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(1.02); }
}

// 暗色模式
:global(.dark) {
  .chat-viewport { background: var(--dt-bg-card-dark); }

  .multi-select-toolbar {
    background: linear-gradient(135deg, #2d2d2d 0%, #1e1e1e 100%);
    border-color: rgba(255, 255, 255, 0.1);
    box-shadow: 0 -8px 24px rgba(0, 0, 0, 0.3);

    .selection-info {
      background: linear-gradient(135deg, rgba(22, 119, 255, 0.2) 0%, rgba(22, 119, 255, 0.15) 100%);
      border-color: rgba(22, 119, 255, 0.3);

      .selection-text { color: #ffffff; }
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

    .toolbar-divider { background: rgba(255, 255, 255, 0.1); }
  }
}
</style>
