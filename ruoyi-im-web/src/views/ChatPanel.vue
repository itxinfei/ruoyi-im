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
            @clear-selection="handleClearSelection"
            @announcement="showAnnouncementDialog = true"
            @history="handleShowHistory"
            @search="handleSearchMessages"
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
            :multi-select-mode="isMultiSelectMode"
            @command="handleCommand"
            @at="handleAt"
            @load-more="handleLoadMore"
            @show-user="handleShowUser"
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

        <!-- 移除旧的侧边栏，改用全局弹窗 -->
      </div>

      <!-- 群组详情弹窗 -->
      <GroupDetailDialog
        v-model="showGroupDetail"
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
      <CallDialog
        ref="callDialogRef"
        :session="session"
      />

      <!-- 语音通话 -->
      <VoiceCallDialog
        v-model:visible="showVoiceCall"
        :remote-user="session"
        :is-incoming="isIncomingCall"
      />

      <!-- 视频通话 -->
      <VideoCallDialog
        v-model:visible="showVideoCall"
        :remote-user="session"
        :is-incoming="isIncomingCall"
      />

      <!-- 聊天记录面板 -->
      <ChatHistoryPanel
        :visible="showChatHistory"
        :conversation-id="session?.id"
        @close="showChatHistory = false"
        @jump-to-message="handleJumpToMessage"
        @clear-history="handleClearHistory"
      />

      <!-- 群公告对话框 -->
      <GroupAnnouncementDialog
        v-model="showAnnouncementDialog"
        :group-id="session?.targetId"
        :can-manage="session?.type === 'GROUP' && session?.memberRole === 'ADMIN'"
      />

      <!-- 搜索聊天记录面板 -->
      <ChatSearchPanel
        :visible="showSearchPanel"
        :session-id="session?.id"
        :messages="messages"
        @close="showSearchPanel = false"
        @jump-to-message="handleJumpToMessage"
      />

      <!-- 聊天内搜索弹窗 -->
      <ChatSearch
        v-model:visible="showChatSearch"
        :messages="messages"
        @select-message="handleScrollToMessage"
      />

      <!-- 查看文件面板 -->
      <ChatFilesPanel
        :visible="showFilesPanel"
        :session-id="session?.id"
        :messages="messages"
        @close="showFilesPanel = false"
        @open-file="handleOpenFile"
        @download-file="handleDownloadFile"
        @forward-file="handleForwardFile"
      />

      <!-- 群文件面板 -->
      <GroupFilePanel
        v-if="showGroupFilesPanel && session?.type === 'GROUP'"
        :group-id="session?.targetId"
        class="group-files-drawer"
      />

      <!-- 合并消息详情对话框 -->
      <CombineDetailDialog
        v-model="showCombineDetail"
        :messages="combineMessages"
        :conversation-title="combineConversationTitle"
        @forward="handleCombineForwardDetail"
      />

      <!-- 导出聊天记录对话框 -->
      <ExportChatDialog
        v-model="showExportDialog"
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
            <button class="toolbar-btn toolbar-btn--cancel" @click="handleClearSelection">
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
import { ref, computed, watch, onMounted, onUnmounted, h } from 'vue'
import { useStore } from 'vuex'
import { Share, Folder, Delete } from '@element-plus/icons-vue'
import { ElImageViewer } from 'element-plus'
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
import { getMessages, batchForwardMessages, deleteMessage, clearConversationMessages } from '@/api/im/message'
import { pinConversation, muteConversation } from '@/api/im/conversation'
import { uploadFile, uploadImage } from '@/api/im/file'
import { addFavorite, removeFavorite } from '@/api/im/favorite'
import { markMessage, unmarkMessage, setTodoReminder, completeTodo, getUserTodoCount } from '@/api/im/marker'
import { useImWebSocket } from '@/composables/useImWebSocket'
import { ElMessage, ElMessageBox } from 'element-plus'
import { parseMessageContent } from '@/utils/message'

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

const store = useStore()
const currentUser = computed(() => store.getters['user/currentUser'])
const messages = ref([])
const loading = ref(false)
const sending = ref(false)
const noMore = ref(false)
const showGroupDetail = ref(false)
const replyingMessage = computed(() => store.state.im.message.replyingMessage)
const editingMessage = ref(null)
const msgListRef = ref(null)
const forwardDialogRef = ref(null)
const callDialogRef = ref(null)
const showVoiceCall = ref(false)
const showVideoCall = ref(false)
const showChatHistory = ref(false)
const isIncomingCall = ref(false)
const showAnnouncementDialog = ref(false)
const showSearchPanel = ref(false)
const showChatSearch = ref(false)
const showFilesPanel = ref(false)
const showExportDialog = ref(false)
const showGroupFilesPanel = ref(false)
const showCombineDetail = ref(false)
const combineMessages = ref([])
const combineConversationTitle = ref('')
const fileInputRef = ref(null)
const imageInputRef = ref(null)
const messageInputRef = ref(null)

// 图片预览状态
const showImagePreview = ref(false)
const imagePreviewUrl = ref('')
const imagePreviewIndex = ref(0)

// 当前会话所有图片URL列表（用于预览时左右切换）
const conversationImages = computed(() => {
  return messages.value
    .filter(m => {
      if (m.type !== 'IMAGE') return false
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

// 输入状态用户列表（用于显示"xxx正在输入..."）
const typingUsers = ref([])
let typingTimers = {} // userId -> timerId

// 发送输入状态（防抖）
let sendTypingTimer = null
const TYPING_DEBOUNCE = 500 // 500ms 防抖

// 发送正在输入状态
const sendMyTypingStatus = () => {
  // 只在单聊时发送输入状态
  if (!props.session || props.session.type !== 'PRIVATE') return

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
  // 只在单聊时发送停止输入状态
  if (!props.session || props.session.type !== 'PRIVATE') return

  if (sendTypingTimer) {
    clearTimeout(sendTypingTimer)
    sendTypingTimer = null
  }

  sendStopTyping(props.session.id)
}

// 处理输入事件（防抖发送 typing 状态）
const handleInput = (content) => {
  if (content && content.trim().length > 0) {
    sendMyTypingStatus()
  } else {
    sendMyStopTypingStatus()
  }
}

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

const handleLoadMore = async () => {
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

const transformMsg = (m) => {
  // 优先使用后端返回的 isSelf 字段
  let isOwn = m.isSelf === true || m.isSelf === false
    ? m.isSelf 
    : false
  
  // 如果没有 isSelf 字段，通过比较 senderId 判断
  // 注意：ID 可能是字符串或数字，需要统一转换为字符串比较
  if (m.isSelf !== true && m.isSelf !== false) {
    const msgSenderId = String(m.senderId || '')
    const currentUserId = String(currentUser.value?.id || '')
    isOwn = msgSenderId === currentUserId && msgSenderId !== ''
  }

  const messageType = m.type || m.messageType || 'TEXT'
  
  // 处理引用回复的数据结构，优先使用后端返回的完整信息
  let replyTo = m.replyTo || m.quotedMessage
  if (!replyTo && m.replyToMessageId) {
    // 降级：尝试在本地消息列表中查找被引用的消息
    const quoted = messages.value.find(msg => msg.id === m.replyToMessageId)
    if (quoted) {
      replyTo = {
        id: quoted.id,
        senderName: quoted.senderName,
        content: quoted.content
      }
    }
  }

  return {
    ...m,
    type: messageType,
    isOwn,
    replyTo: replyTo,
    timestamp: m.sendTime || m.createTime || m.timestamp || Date.now()
  }
}

const handleSend = async (content) => {
  // 发送消息时停止输入状态
  sendMyStopTypingStatus()

  // 乐观更新：先显示消息，状态为 sending
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

    // 发送成功，更新消息状态和ID
    const index = messages.value.findIndex(m => m.id === tempId)
    if (index !== -1) {
      // 保持 status 为 success，且替换为真实数据
      const realMsg = transformMsg(msg)
      messages.value.splice(index, 1, { ...realMsg, status: null })
    }
  } catch (error) {
    // 发送失败，标记状态
    const index = messages.value.findIndex(m => m.id === tempId)
    if (index !== -1) {
      messages.value[index].status = 'failed'
    }
    console.error('发送失败', error)
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
onMessage((msg) => {
  if (msg.conversationId === props.session?.id) {
    const transformedMsg = transformMsg(msg)
    messages.value.push(transformedMsg)
    msgListRef.value?.scrollToBottom()
    
    // 新消息提醒
    if (!transformedMsg.isOwn) {
      // 动态导入提醒工具,避免循环依赖
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

watch(() => props.session, () => {
  messages.value = []
  loadHistory()
})

const handleDelete = async (messageId) => {
  try {
    await store.dispatch('im/message/deleteMessage', messageId)
    // 移除本地消息
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
    // 更新本地消息状态
    const index = messages.value.findIndex(m => m.id === messageId)
    if (index !== -1) {
      messages.value[index].type = 'RECALLED' // 或其他处理
      messages.value[index].content = '消息已撤回'
    }
  } catch (error) {
    console.error('撤回失败', error)
  }
}

// 处理菜单命令
const handleCommand = (cmd, msg) => {
  if (cmd === 'forward') {
    forwardDialogRef.value?.open(msg)
  } else if (cmd === 'reply') {
    handleReply(msg)
  } else if (cmd === 'recall') {
    handleRecall(msg.id)
  } else if (cmd === 'delete') {
    handleDelete(msg.id)
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
    showExportDialog.value = true
  }
}

// 处理设为待办
const handleAddToTodo = async (msg) => {
  try {
    await setTodoReminder({
      messageId: msg.id,
      remindTime: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(), // 默认明天提醒
      remark: msg.content?.substring(0, 50) || '消息待办'
    })
    ElMessage.success('已添加到待办事项')
    // 更新消息标记状态
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

// 处理收藏消息
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

// 处理标记消息
const handleMarkMessage = async (msg) => {
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
const isMultiSelectMode = computed(() => store.getters['im/message/selectedMessageCount'] > 0)
const selectedMessages = computed(() => store.getters['im/message/selectedMessageList'])

const handleMultiSelect = (msg) => {
  store.commit('im/message/TOGGLE_MESSAGE_SELECTION', msg.id)
  ElMessage.info('进入多选模式')
}

// 查看合并转发消息详情
const handleViewCombine = (msg) => {
  // msg 包含 messages 数组（合并的消息内容）
  if (msg.messages && msg.messages.length > 0) {
    combineMessages.value = msg.messages
    combineConversationTitle.value = `${msg.senderName}的聊天记录`
    showCombineDetail.value = true
  } else {
    ElMessage.warning('无法查看聊天记录详情')
  }
}

// 合并消息详情中的转发
const handleCombineForwardDetail = (messages) => {
  const messageIds = messages.map(m => m.id)
  if (messageIds.length > 0) {
    forwardDialogRef.value?.openForBatch(messageIds, 'combine')
  }
}

// 批量转发 - 逐条转发
const handleBatchForward = async () => {
  const messageIds = selectedMessages.value.map(msg => msg.id)
  if (messageIds.length === 0) return

  // 使用 ForwardDialog 选择目标会话
  forwardDialogRef.value?.openForBatch(messageIds, 'batch')
}

// 批量转发 - 合并转发
const handleCombineForward = async () => {
  const messageIds = selectedMessages.value.map(msg => msg.id)
  if (messageIds.length === 0) return

  // 使用 ForwardDialog 选择目标会话
  forwardDialogRef.value?.openForBatch(messageIds, 'combine')
}

// 批量删除
const handleBatchDelete = async () => {
  const selected = selectedMessages.value
  if (selected.length === 0) return

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
const handleToggleMultiSelect = (active) => {
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

const uploadImageFile = async (file) => {
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

const handlePaste = async (event) => {
  const items = event.clipboardData?.items
  if (!items) return

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
const handleMarkRead = async (msg) => {
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
  if (props.session.type === 'GROUP') {
    showGroupDetail.value = true
  } else {
    handleShowUser(props.session.targetId)
  }
}

// 图片预览处理
const handleImagePreview = (imageUrl) => {
  imagePreviewUrl.value = imageUrl
  imagePreviewIndex.value = conversationImages.value.indexOf(imageUrl)
  showImagePreview.value = true
}

// 关闭图片预览
const closeImagePreview = () => {
  showImagePreview.value = false
}

const handleCancelReply = () => {
  store.commit('im/message/SET_REPLYING_MESSAGE', null)
}

const handleCancelEdit = () => {
  editingMessage.value = null
}

const handleRetry = async (msg) => {
  if (msg.status !== 'failed') return
  
  // 重置为发送中
  msg.status = 'sending'
  
  try {
    const res = await store.dispatch('im/message/sendMessage', {
      sessionId: props.session.id,
      type: msg.type,
      content: typeof msg.content === 'object' ? JSON.stringify(msg.content) : msg.content
    })
    
    // 更新
    const realMsg = transformMsg(res)
    Object.assign(msg, { ...realMsg, status: null })
  } catch (error) {
    msg.status = 'failed'
    ElMessage.error('重试失败')
  }
}

const handleMemberClick = (member) => {
  handleShowUser(member.id)
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
      content: content
    })
    
    // 更新本地消息列表
    const index = messages.value.findIndex(m => m.id === editingMessage.value.id)
    if (index !== -1) {
      messages.value[index].content = content
      messages.value[index].isEdited = true // 标记已编辑
    }
    
    editingMessage.value = null
    ElMessage.success('已编辑')
  } catch (error) {
    console.error('编辑失败', error)
  }
}

// 通话功能
const handleStartCall = () => {
  isIncomingCall.value = false
  showVoiceCall.value = true
}

const handleStartVideo = () => {
  isIncomingCall.value = false
  showVideoCall.value = true
}

// ChatHeader 通话按钮事件
const handleVoiceCall = () => {
  isIncomingCall.value = false
  showVoiceCall.value = true
}

const handleVideoCall = () => {
  isIncomingCall.value = false
  showVideoCall.value = true
}

// 创建公告
const handleCreateAnnouncement = () => {
  if (session.value?.type === 'GROUP') {
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
  if (!currentSession) return

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
  if (!currentSession) return

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
  try {
    await ElMessageBox.confirm(
      `确定要清空与 ${session.value?.name} 的聊天记录吗？`,
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
    await clearConversationMessages(session.value?.id)

    // 清空本地消息列表
    messages.value = []

    // 重置分页状态
    noMore.value = false

    ElMessage.success('聊天记录已清空')
  } catch {
    // 用户取消
  }
}

// 滚动到置顶消息
const handleScrollToPinnedMessage = (messageId) => {
  if (msgListRef.value) {
    msgListRef.value.scrollToMessage(messageId)
  }
}

// 处理搜索结果滚动
const handleScrollToMessage = (messageId) => {
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
const handleJumpToMessage = (message) => {
  if (msgListRef.value) {
    msgListRef.value.scrollToMessage(message.id || message.messageId)
  }
  showSearchPanel.value = false
  showChatHistory.value = false
}

// 打开文件
const handleOpenFile = (file) => {
  if (file.url) {
    window.open(file.url, '_blank')
  }
}

// 下载文件
const handleDownloadFile = (file) => {
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
const handleForwardFile = (file) => {
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

const handleFileUpload = async (payload) => {
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
    if (!file) return
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

const handleImageUpload = async (payload) => {
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
    if (!file) return
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
const handleScreenshotUpload = async (formData) => {
  sendMyStopTypingStatus()
  const file = formData.get('file')
  if (!file) return

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
  if (props.session) loadHistory()

  // 请求浏览器通知权限
  import('@/utils/messageNotification').then(({ requestNotificationPermission }) => {
    requestNotificationPermission().then(permission => {
      // 权限结果静默处理
    })
  })

  // 监听输入状态事件
  onTyping((data) => {
    if (data.conversationId !== props.session?.id) return
    if (data.userId === currentUser.value?.id) return // 忽略自己的输入状态

    handleTypingIndicator(data.userId, data.userName || data.senderName)
  })

  // 监听消息状态更新（发送成功/失败）
  onMessageStatus((data) => {
    if (data.conversationId !== props.session?.id) return

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
  onReaction((data) => {
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
  const handleKeydown = (e) => {
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

  // 清理函数（在组件卸载时调用）
  onUnmounted(() => {
    window.removeEventListener('keydown', handleKeydown)
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
  min-height: 0;              // 修复 flex 子元素高度问题
  overflow: hidden;           // 防止内容溢出
  background: var(--dt-bg-body);
  position: relative;
  z-index: var(--dt-z-base);  // 使用设计令牌替代魔法值


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
  min-height: 0;    // flex: 1 配合 min-height: 0 正确处理高度
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
// 多选工具栏
// ============================================================================
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
    border: 1px solid rgba(0, 137, 255, 0.2);

    .selection-indicator {
      width: 12px;
      height: 12px;
      background: var(--dt-brand-color);
      border-radius: 50%;
      animation: selectionPulse 2s ease-in-out infinite;
      box-shadow: 0 0 8px rgba(0, 137, 255, 0.5);
    }

    .selection-text {
      font-size: 14px;
      font-weight: 500;
      color: var(--dt-brand-color);

      strong {
        font-weight: 700;
        font-size: 16px;
      }
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
    position: relative;
    overflow: hidden;

    .material-icons-outlined {
      font-size: 16px;
      transition: transform 0.3s;
    }

    &:disabled {
      opacity: 0.4;
      cursor: not-allowed;

      &:hover {
        transform: none;
        background: transparent;
      }
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
  0%, 100% {
    opacity: 0.3;
    transform: scale(1);
  }
  50% {
    opacity: 0.6;
    transform: scale(1.02);
  }
}
</style>
