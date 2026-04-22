<template>
  <div class="chat-main-v4">
    <!-- 1. 核心会话区 (Header + List + Input) -->
    <div class="chat-container">
      <ChatWindowHeader
        :session="currentSession"
        :is-sidebar-open="isDetailOpen"
        @toggle-sidebar="isDetailOpen = !isDetailOpen"
        @toggle-selection="toggleSelectionMode"
        @open-search="showGlobalSearch = true"
        @voice-call="handleVoiceCall"
        @video-call="handleVideoCall"
      />

      <!-- 消息列表区 -->
      <div class="message-area-scroller custom-scrollbar" ref="messageListRef">
        <ChatMessageList
          ref="chatMessageListRef"
          :messages="messages"
          :is-selection-mode="isSelectionMode"
          :selected-messages="selectedMessages"
          @select-message="handleMessageClick"
          @load-more="handleLoadMore"
        />
      </div>

      <!-- 底部输入区 (对齐钉钉：通铺纯白) -->
      <ChatInputArea
        ref="chatInputAreaRef"
        v-model="currentDraft"
        @send="processSendMessage"
        @attach-click="handleOpenFilePicker"
        @emoji-click="handleOpenEmoji"
        @mention-click="handleOpenMention"
        @link-click="handleInsertLink"
      />

      <!-- @提及选择器 -->
      <AtMemberPicker
        v-if="mentionPickerVisible"
        :visible="mentionPickerVisible"
        :members="mentionMembers"
        :position="mentionPickerPosition"
        @select="handleSelectMention"
        @close="mentionPickerVisible = false"
      />

      <!-- 表情选择器 -->
      <div v-if="emojiPickerVisible" class="emoji-picker-popover">
        <div class="emoji-grid">
          <span
            v-for="emoji in commonEmojis"
            :key="emoji"
            class="emoji-item"
            @click="handleSelectEmoji(emoji)"
          >{{ emoji }}</span>
        </div>
      </div>

      <!-- 多选操作条 (悬浮覆盖) -->
      <SelectionActionBar
        v-if="isSelectionMode"
        :selected-count="selectedMessages.size"
        @cancel="isSelectionMode = false"
        @delete="handleBatchDelete"
        @forward="handleOpenForwardDialog"
        @favorite="handleBatchFavorite"
        @select-all="handleSelectAll"
      />

      <!-- 转发对话框 -->
      <ForwardDialog ref="forwardDialogRef" />

      <!-- 通话对话框 -->
      <CallDialog ref="callDialogRef" :session="currentSession" />
    </div>

    <!-- 2. 侧边详情区 (去虚构：Push 布局而非覆盖) -->
    <transition name="push-sidebar">
      <aside v-if="isDetailOpen" class="chat-right-sidebar">
        <ChatDetailDrawer :session="currentSession" @close="isDetailOpen = false" />
      </aside>
    </transition>
  </div>
</template>

<script setup lang="js">
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import ChatWindowHeader from './ChatWindow/ChatWindowHeader.vue'
import ChatMessageList from './ChatWindow/ChatMessageList.vue'
import ChatInputArea from './ChatInputArea.vue'
import SelectionActionBar from './ChatWindow/SelectionActionBar.vue'
import ChatDetailDrawer from '@/components/Chat/ChatDetailDrawer.vue'
import ForwardDialog from '@/components/ForwardDialog/index.vue'
import AtMemberPicker from './AtMemberPicker.vue'
import CallDialog from '@/components/Chat/CallDialog.vue'
import { uploadFile } from '@/api/im/file'
import { getGroupMembers } from '@/api/im/group'

const store = useStore()
const isDetailOpen = ref(false)
const isSelectionMode = ref(false)
const selectedMessages = ref(new Set())
const currentDraft = ref('')
const messageListRef = ref(null)
const chatMessageListRef = ref(null)
const chatInputAreaRef = ref(null)
const isLoadingHistory = ref(false)
const forwardDialogRef = ref(null)
const callDialogRef = ref(null)

// @提及选择器状态
const mentionPickerVisible = ref(false)
const mentionPickerPosition = ref({ top: 0, left: 0 })
const mentionMembers = ref([])

// 表情选择器状态
const emojiPickerVisible = ref(false)

// 常用表情数据
const commonEmojis = [
  '😀', '😃', '😄', '😁', '😅', '😂', '🤣', '😊', '😇', '🙂',
  '😉', '😌', '😍', '🥰', '😘', '😋', '😛', '🤔', '👍', '👎',
  '👏', '🙌', '🤝', '🙏', '💪', '❤️', '🧡', '💛', '💚', '💙',
  '💜', '🖤', '🤍', '💯', '🔥', '⭐', '✨', '🎉', '🎊', '💬'
]

const currentSession = computed(() => store.state.im.session.currentSession)
const messages = computed(() => store.state.im.message.messages)

// 发送消息
const processSendMessage = (content) => {
  if (!currentSession.value?.id) return
  store.dispatch('im/message/sendMessage', {
    content,
    sessionId: currentSession.value.id
  })
}

// 工具栏功能处理
const handleOpenFilePicker = () => {
  // 触发文件选择
  const input = document.createElement('input')
  input.type = 'file'
  input.multiple = true
  input.onchange = (e) => {
    const files = e.target.files
    if (files?.length) {
      handleUploadFiles(files)
    }
  }
  input.click()
}

const handleUploadFiles = async (files) => {
  Array.from(files).forEach(async file => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('conversationId', currentSession.value.id)

    try {
      const res = await uploadFile(formData)
      if (res.code === 200) {
        ElMessage.success('文件发送成功')
      }
    } catch (error) {
      console.error('文件上传失败:', error)
      ElMessage.error('文件发送失败')
    }
  })
}

const handleOpenEmoji = () => {
  emojiPickerVisible.value = !emojiPickerVisible.value
}

// 选择表情
const handleSelectEmoji = (emoji) => {
  if (chatInputAreaRef.value) {
    chatInputAreaRef.value.insertText(emoji)
  }
  emojiPickerVisible.value = false
}

const handleOpenMention = async () => {
  if (!currentSession.value?.id) return

  // 获取群成员（如果是群聊）
  const session = currentSession.value
  if (session.type === 'group' || session.chatType === 'group') {
    try {
      const res = await getGroupMembers(session.id)
      if (res.code === 200 && res.data) {
        mentionMembers.value = res.data.map(m => ({
          userId: m.userId || m.id,
          nickname: m.nickname || m.name,
          name: m.nickname || m.name,
          avatar: m.avatar,
          role: m.role
        }))
      }
    } catch (e) {
      console.error('获取群成员失败:', e)
      mentionMembers.value = []
    }
  } else {
    // 私聊：只显示对方
    mentionMembers.value = [{
      userId: session.userId || session.peerId,
      nickname: session.nickname || session.name || '对方',
      name: session.nickname || session.name || '对方',
      avatar: session.avatar
    }]
  }

  // 计算弹窗位置（在输入框上方）
  mentionPickerPosition.value = {
    top: -300,
    left: 20
  }
  mentionPickerVisible.value = true
}

// 处理选择提及成员
const handleSelectMention = (member) => {
  if (chatInputAreaRef.value) {
    chatInputAreaRef.value.insertText(`@${member.nickname} `)
  }
  mentionPickerVisible.value = false
}

const handleInsertLink = async () => {
  try {
    const result = await ElMessageBox.prompt('请输入链接地址', '插入链接', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^(https?:\/\/)?[\w.-]+\.[a-zA-Z]{2,}(\/\S*)?$/,
      inputErrorMessage: '请输入有效的网址'
    })

    if (result.value && chatInputAreaRef.value) {
      let url = result.value
      if (!url.startsWith('http://') && !url.startsWith('https://')) {
        url = 'https://' + url
      }
      chatInputAreaRef.value.insertText(url)
    }
  } catch {
    // 用户取消
  }
}

// 加载更多历史消息
const handleLoadMore = async () => {
  if (isLoadingHistory.value || !currentSession.value) return

  const messages_data = messages.value
  if (!messages_data || messages_data.length === 0) return

  isLoadingHistory.value = true

  try {
    // 获取最早一条消息的 ID 作为分页起点
    const oldestMessage = messages_data[0]
    const lastMessageId = oldestMessage?.messageId || oldestMessage?.id

    await store.dispatch('im/message/loadMessages', {
      sessionId: currentSession.value.id,
      lastMessageId: lastMessageId,
      pageSize: 20,
      isLoadMore: true
    })
  } catch (error) {
    console.error('加载历史消息失败:', error)
  } finally {
    isLoadingHistory.value = false
  }
}

// 处理消息点击（多选模式）
const handleMessageClick = (message) => {
  if (isSelectionMode.value) {
    // 多选模式：切换选中状态
    if (selectedMessages.value.has(message.messageId)) {
      selectedMessages.value.delete(message.messageId)
    } else {
      selectedMessages.value.add(message.messageId)
    }
  }
}

// 切换多选模式
const toggleSelectionMode = () => {
  isSelectionMode.value = !isSelectionMode.value
  if (!isSelectionMode.value) {
    selectedMessages.value.clear()
  }
}

// 批量删除消息
const handleBatchDelete = async () => {
  const messageIds = Array.from(selectedMessages.value)
  if (messageIds.length === 0) return

  try {
    await store.dispatch('im/message/batchDeleteMessagesAction', messageIds)
    ElMessage.success(`已删除 ${messageIds.length} 条消息`)
    isSelectionMode.value = false
    selectedMessages.value.clear()
  } catch (error) {
    console.error('批量删除失败:', error)
    ElMessage.error('删除失败，请重试')
  }
}

// 批量收藏消息
const handleBatchFavorite = async () => {
  const messageIds = Array.from(selectedMessages.value)
  if (messageIds.length === 0) return

  try {
    for (const messageId of messageIds) {
      await store.dispatch('im/message/addFavorite', {
        messageId,
        conversationId: currentSession.value.id
      })
    }
    ElMessage.success(`已收藏 ${messageIds.length} 条消息`)
    isSelectionMode.value = false
    selectedMessages.value.clear()
  } catch (error) {
    console.error('批量收藏失败:', error)
    ElMessage.error('收藏失败，请重试')
  }
}

// 全选消息
const handleSelectAll = () => {
  messages.value.forEach(msg => {
    selectedMessages.value.add(msg.messageId)
  })
}

// 打开转发对话框
const handleOpenForwardDialog = () => {
  const selectedMsgs = messages.value.filter(msg => selectedMessages.value.has(msg.messageId))
  if (forwardDialogRef.value) {
    forwardDialogRef.value.open(selectedMsgs)
  }
}

// 发起语音通话
const handleVoiceCall = () => {
  if (!currentSession.value?.id) {
    ElMessage.warning('请先选择一个会话')
    return
  }
  const session = currentSession.value
  if (callDialogRef.value) {
    callDialogRef.value.open('voice', {
      peerId: session.peerId || session.userId,
      peerName: session.nickname || session.name || '对方',
      peerAvatar: session.avatar,
      callId: `call-${Date.now()}`
    })
  }
}

// 发起视频通话
const handleVideoCall = () => {
  if (!currentSession.value?.id) {
    ElMessage.warning('请先选择一个会话')
    return
  }
  const session = currentSession.value
  if (callDialogRef.value) {
    callDialogRef.value.open('video', {
      peerId: session.peerId || session.userId,
      peerName: session.nickname || session.name || '对方',
      peerAvatar: session.avatar,
      callId: `call-${Date.now()}`
    })
  }
}
</script>

<style scoped lang="scss">
.chat-main-v4 {
  flex: 1;
  display: flex;
  height: 100%;
  overflow: hidden;
  background-color: var(--dt-bg-card);
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 400px;
  height: 100%;
  position: relative;
}

.message-area-scroller {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

/* 右侧侧边栏 (钉钉 Push 布局) */
.chat-right-sidebar {
  width: 320px;
  background-color: var(--dt-bg-card);
  border-left: 1px solid var(--dt-border-light);
  height: 100%;
  flex-shrink: 0;
  z-index: 50;
}

// 侧边栏滑出动效 (挤压感)
.push-sidebar-enter-active, .push-sidebar-leave-active {
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}
.push-sidebar-enter-from, .push-sidebar-leave-to {
  width: 0;
}

/* 表情选择器 */
.emoji-picker-popover {
  position: absolute;
  bottom: 140px;
  left: 20px;
  width: 320px;
  max-height: 200px;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg);
  box-shadow: var(--dt-shadow-float);
  border: 1px solid var(--dt-border-light);
  padding: var(--dt-spacing-sm);
  z-index: var(--dt-z-popover);
  overflow-y: auto;

  .emoji-grid {
    display: grid;
    grid-template-columns: repeat(8, 1fr);
    gap: 2px;

    .emoji-item {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      cursor: pointer;
      border-radius: var(--dt-radius-sm);
      transition: background-color var(--dt-transition-fast);

      &:hover {
        background: var(--dt-bg-hover);
      }
    }
  }
}
</style>
