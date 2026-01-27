<template>
  <div class="chat-panel">
    <div v-if="!session" class="empty-placeholder">
      <el-empty description="选择一个会话开始聊天" />
    </div>
    <template v-else>
      <div class="main-container">
        <!-- 左侧聊天主体 -->
        <div class="chat-viewport">
          <ChatHeader 
            :session="session" 
            @toggle-sidebar="handleToggleDetail" 
          />
          <MessageList 
            ref="msgListRef"
            :session-id="session?.id"
            :messages="messages" 
            :loading="loading" 
            :current-user="currentUser" 
            :session-type="session?.type"
            @command="handleCommand"
            @at="handleAt"
            @load-more="handleLoadMore"
            @show-user="handleShowUser"
            @retry="handleRetry"
          />
          <MessageInput
            ref="messageInputRef"
            :session="session"
            :sending="sending"
            :replying-message="replyingMessage"
            :editing-message="editingMessage"
            @send="handleSend"
            @cancel-reply="handleCancelReply"
            @cancel-edit="handleCancelEdit"
            @edit-confirm="handleEditConfirm"
            @start-call="handleStartCall"
            @start-video="handleStartVideo"
            @upload-image="handleImageUpload"
            @upload-file="handleFileUpload"
          />
        </div>

        <!-- 移除旧的侧边栏，改用全局弹窗 -->
      </div>

      <!-- 群组详情弹窗 -->
      <GroupDetailDrawer
        v-model="showGroupDetail"
        :group-id="session?.targetId"
        @refresh="$emit('refresh-sessions')"
      />

      <!-- 隐藏的文件上传 input -->
      <input type="file" ref="fileInputRef" style="display: none" @change="handleFileUpload" />
      <input type="file" ref="imageInputRef" style="display: none" accept="image/*" @change="handleImageUpload" />

      <!-- 转发对话框 -->
      <ForwardDialog
        ref="forwardDialogRef"
        @forward="handleForwardConfirm"
      />

      <!-- 通话对话框 -->
      <CallDialog
        ref="callDialogRef"
        :session="session"
      />
      <!-- 多选操作栏 -->
      <Transition name="slide-up">
        <div v-if="isMultiSelectMode" class="multi-select-toolbar">
          <div class="selection-info">已选择 {{ selectedMessages.length }} 条消息</div>
          <div class="actions">
            <el-button type="primary" plain @click="handleBatchForward"><el-icon><Share /></el-icon> 逐条转发</el-button>
            <el-button type="primary" plain @click="handleCombineForward"><el-icon><Collection /></el-icon> 合并转发</el-button>
            <el-button type="danger" plain @click="handleBatchDelete"><el-icon><Delete /></el-icon> 删除</el-button>
            <el-divider direction="vertical" />
            <el-button link @click="handleClearSelection">取消</el-button>
          </div>
        </div>
      </Transition>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useStore } from 'vuex'
import { Share, Collection, Delete } from '@element-plus/icons-vue'
import ChatHeader from '@/components/Chat/ChatHeader.vue'
import MessageList from '@/components/Chat/MessageList.vue'
import MessageInput from '@/components/Chat/MessageInput.vue'
import ForwardDialog from '@/components/ForwardDialog/index.vue'
import CallDialog from '@/components/Chat/CallDialog.vue'
import GroupDetailDrawer from '@/components/GroupDetailDrawer/index.vue'
import { getMessages } from '@/api/im/message'
import { uploadFile, uploadImage } from '@/api/im/file'
import { useImWebSocket } from '@/composables/useImWebSocket'
import { ElMessage } from 'element-plus'

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
const fileInputRef = ref(null)
const imageInputRef = ref(null)
const messageInputRef = ref(null)

const emit = defineEmits(['show-user'])

const { onMessage } = useImWebSocket()

const loadHistory = async () => {
  if (!props.session?.id) return
  loading.value = true
  noMore.value = false
  try {
    const res = await store.dispatch('im/message/loadMessages', {
      sessionId: props.session.id,
      pageSize: 50
    })
    console.log('===== ChatPanel loadHistory 开始 =====')
    console.log('ChatPanel - 当前登录用户:', currentUser.value)
    console.log('ChatPanel - 会话ID:', props.session.id)
    console.log('ChatPanel - 原始消息数量:', res?.length)

    messages.value = (res || []).map(m => {
      const transformed = transformMsg(m)
      console.log('消息详情:', {
        id: m.id,
        content: m.content,
        senderId: m.senderId,
        senderName: m.senderName,
        后端isSelf: m.isSelf,
        前端isOwn: transformed.isOwn,
        当前userId: currentUser.value?.id,
        匹配结果: m.senderId === currentUser.value?.id
      })
      return transformed
    })
    console.log('===== ChatPanel loadHistory 结束 =====')
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
  const isOwn = m.isSelf === true || m.isSelf === false
    ? m.isSelf 
    : m.senderId === currentUser.value?.id

  const messageType = m.type || m.messageType || 'TEXT'
  
  // 处理引用回复的数据结构
  let replyTo = m.replyTo
  if (!replyTo && m.replyToMessageId) {
    // 尝试在本地消息列表中查找被引用的消息
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
      messages.value.splice(index, 1, { ...realMsg, status: 'success' })
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
  } else if (cmd === 'multi-select') {
    handleMultiSelect(msg)
  }
}

// 处理设为待办
const handleAddToTodo = async (msg) => {
  try {
    // 这里应调用待办 API，暂时模拟提示
    ElMessage.success('已添加到待办事项')
    console.log('添加到待办:', msg)
  } catch (e) {
    ElMessage.error('添加失败')
  }
}

// 处理多选
const isMultiSelectMode = computed(() => store.getters['im/message/selectedMessageCount'] > 0)
const selectedMessages = computed(() => store.getters['im/message/selectedMessageList'])

const handleMultiSelect = (msg) => {
  store.commit('im/message/TOGGLE_MESSAGE_SELECTION', msg.id)
  ElMessage.info('进入多选模式')
}

const handleClearSelection = () => {
  store.commit('im/message/CLEAR_MESSAGE_SELECTION')
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
    Object.assign(msg, { ...realMsg, status: 'success' })
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
  callDialogRef.value?.open('voice')
}

const handleStartVideo = () => {
  callDialogRef.value?.open('video')
}

// 文件上传相关
const triggerFileUpload = () => fileInputRef.value?.click()
const triggerImageUpload = () => imageInputRef.value?.click()

const handleFileUpload = async (payload) => {
  let file, formData
  if (payload instanceof FormData) {
    formData = payload
    file = payload.get('file')
  } else {
    file = payload.target.files[0]
    if (!file) return
    formData = new FormData()
    formData.append('file', file)
    payload.target.value = ''
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
        messages.value.splice(index, 1, { ...transformMsg(msg), status: 'success' })
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
  let file, formData
  if (payload instanceof FormData) {
    formData = payload
    file = payload.get('file')
  } else {
    file = payload.target.files[0]
    if (!file) return
    formData = new FormData()
    formData.append('file', file)
    payload.target.value = ''
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
        messages.value.splice(index, 1, { ...transformMsg(msg), status: 'success' })
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

onMounted(() => {
  if (props.session) loadHistory()
  
  // 请求浏览器通知权限
  import('@/utils/messageNotification').then(({ requestNotificationPermission }) => {
    requestNotificationPermission().then(permission => {
      if (permission === 'granted') {
        console.log('[消息提醒] 通知权限已授予')
      } else if (permission === 'denied') {
        console.warn('[消息提醒] 通知权限被拒绝')
      }
    })
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
  background: var(--dt-bg-body);
}

.main-container {
  display: flex;
  flex: 1;
  height: 0;
  overflow: hidden;
}

.chat-viewport {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: var(--dt-bg-card);
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

  :deep(.el-empty) {
    --el-empty-padding: 40px 0;
  }

  :deep(.el-empty__description p) {
    color: var(--dt-text-tertiary);
    font-size: 14px;
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
  height: 64px;
  background: var(--dt-bg-card);
  border-top: 1px solid var(--dt-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 -4px 16px rgba(0, 0, 0, 0.08);
  z-index: 100;

  .selection-info {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: 500;
    color: var(--dt-text-primary);

    &::before {
      content: '';
      width: 8px;
      height: 8px;
      background: var(--dt-brand-color);
      border-radius: 50%;
    }
  }

  .actions {
    display: flex;
    align-items: center;
    gap: 8px;

    .el-button {
      font-size: 13px;
      font-weight: 500;
      border-radius: var(--dt-radius-md);
      height: 32px;
      padding: 0 12px;
      display: inline-flex;
      align-items: center;
      gap: 4px;

      .el-icon {
        font-size: 14px;
      }

      &.el-button--danger.is-plain {
        &:hover {
          background: var(--dt-error-bg);
          border-color: var(--dt-error-color);
          color: var(--dt-error-color);
        }
      }

      &.el-button--primary.is-plain {
        &:hover {
          background: var(--dt-brand-bg);
          border-color: var(--dt-brand-color);
          color: var(--dt-brand-color);
        }
      }
    }

    .el-divider--vertical {
      height: 20px;
      margin: 0 4px;
      border-color: var(--dt-border-color);
    }

    .el-button--link {
      height: 32px;
      padding: 0 12px;
      color: var(--dt-text-secondary);

      &:hover {
        color: var(--dt-brand-color);
      }
    }
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
  background: var(--dt-bg-card-dark);
  border-color: var(--dt-border-dark);
  box-shadow: 0 -4px 16px rgba(0, 0, 0, 0.2);

  .selection-info {
    color: var(--dt-text-primary-dark);
  }

  .actions {
    .el-button--primary.is-plain {
      &:hover {
        background: var(--dt-brand-bg-dark);
        border-color: var(--dt-brand-color);
        color: var(--dt-brand-color);
      }
    }

    .el-button--danger.is-plain {
      &:hover {
        background: var(--dt-error-bg);
        border-color: var(--dt-error-color);
        color: var(--dt-error-color);
      }
    }

    .el-button--link {
      color: var(--dt-text-secondary-dark);

      &:hover {
        color: var(--dt-brand-color);
      }
    }

    .el-divider--vertical {
      border-color: var(--dt-border-dark);
    }
  }
}

// ============================================================================
// 响应式布局
// ============================================================================
@media (max-width: 479px) {
  .multi-select-toolbar {
    height: auto;
    min-height: 56px;
    padding: 12px 16px;
    flex-direction: column;
    gap: 12px;

    .selection-info {
      width: 100%;
      justify-content: center;
      font-size: 13px;
    }

    .actions {
      width: 100%;
      justify-content: space-between;
      flex-wrap: wrap;

      .el-button {
        font-size: 12px;
        height: 32px;
        padding: 0 10px;
        flex: 1;
        min-width: calc(50% - 4px);
        justify-content: center;

        span { display: none; }
        .el-icon { margin: 0; }
      }

      .el-divider--vertical { display: none; }

      .el-button--link {
        flex: 0 0 auto;
        min-width: auto;
        width: auto;
        span { display: inline; }
      }
    }
  }

  .empty-placeholder { padding: 40px 16px; }
}

@media (min-width: 480px) and (max-width: 767px) {
  .multi-select-toolbar {
    padding: 0 16px;
    height: 60px;

    .selection-info { font-size: 13px; }

    .actions {
      gap: 6px;

      .el-button {
        font-size: 12px;
        padding: 0 10px;
        height: 32px;
        span { display: none; }
      }

      .el-button--link span { display: inline; }
    }
  }
}

@media (min-width: 768px) and (max-width: 1023px) {
  .multi-select-toolbar {
    padding: 0 20px;

    .actions .el-button {
      font-size: 13px;
      padding: 0 10px;
    }
  }
}
</style>
