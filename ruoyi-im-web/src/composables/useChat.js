/**
 * Chat Composable - 统一入口
 *
 * 整合所有聊天逻辑，确保所有功能可用。
 * 遵循阿里巴巴 Vue 开发规范。
 *
 * 使用方式：
 * 1. 完整功能：import { useChat } from '@/composables/useChat'
 * 2. 按需导入：import { useChatMessages, useChatSend } from '@/composables/useChat'
 */

import { ref, computed, watch, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useImWebSocket } from './useImWebSocket'
import * as messageApi from '@/api/im/message'
import { addFavorite, removeFavorite } from '@/api/im/favorite'
import { markMessage as markMessageApi } from '@/api/im/marker'
import { uploadFile } from '@/api/file'

// 从独立文件导入子 composables
import {
  useChatMessages,
  useChatCommands,
  useChatDialogs,
  useChatSend,
  useChatWebSocket,
  useChatTyping,
  useChatSessionOps,
  useChatDragDrop
} from './useChat/index.js'

// 重新导出子 composables，支持按需导入
export {
  useChatMessages,
  useChatCommands,
  useChatDialogs,
  useChatSend,
  useChatWebSocket,
  useChatTyping,
  useChatSessionOps,
  useChatDragDrop
}

/**
 * 聊天功能主 Composable
 * 整合所有聊天相关功能，提供统一的 API
 * @param {Ref<String>} sessionId - 当前会话ID
 * @param {Ref<Object>} currentUser - 当前用户信息
 */
export function useChat(sessionId, currentUser) {
  const store = useStore()
  const { onMessage, onMessageStatus, onReaction, onRecall, sendTyping, sendStopTyping } = useImWebSocket()

  // ==================== 状态 ====================
  const currentSessionId = computed(() => sessionId?.value ?? sessionId)
  const messages = computed(() => store.getters['im/message/messagesBySessionId'](currentSessionId.value) || [])

  const loading = ref(false)
  const noMore = ref(false)
  const sending = ref(false)
  const replyingMessage = computed(() => store.state.im.message.replyingMessage)
  const editingMessage = ref(null)

  // 编辑锁，防止并发编辑
  const editingLock = ref(false)

  // ==================== 核心方法 ====================

  /**
   * 加载历史记录
   */
  const loadHistory = async (targetId = currentSessionId.value) => {
    if (loading.value || !targetId) {return}
    loading.value = true
    try {
      const result = await store.dispatch('im/message/loadMessages', { sessionId: targetId })
      if (!result || result.length < 20) {noMore.value = true}
    } catch (err) {
      console.error('History load failed:', err)
    } finally {
      loading.value = false
    }
  }

  /**
   * 发送文本消息
   */
  const sendText = async (content, options = {}) => {
    if (!content?.trim() || sending.value) {return}
    sending.value = true
    try {
      await store.dispatch('im/message/sendMessage', {
        sessionId: currentSessionId.value,
        type: 'TEXT',
        content: content.trim(),
        replyToMessageId: options.replyToMessageId || null
      })
    } catch (err) {
      ElMessage.error('消息发送失败')
    } finally {
      sending.value = false
    }
  }

  /**
   * 发送媒体消息通用方法
   */
  const sendMedia = async (type, content, options = {}) => {
    if (sending.value) {return}
    sending.value = true
    try {
      await store.dispatch('im/message/sendMessage', {
        sessionId: currentSessionId.value,
        type,
        content: typeof content === 'string' ? content : JSON.stringify(content),
        replyToMessageId: options.replyToMessageId || null
      })
    } catch (err) {
      ElMessage.error('消息发送失败')
    } finally {
      sending.value = false
    }
  }

  const sendImage = (payload, options) => sendMedia('IMAGE', payload, options)
  const sendFile = (payload, options) => sendMedia('FILE', payload, options)
  const sendVoice = (payload, options) => {
    const content = {
      duration: payload.duration,
      url: payload.url || (payload.file ? URL.createObjectURL(payload.file) : '')
    }
    return sendMedia('VOICE', content, options)
  }
  const sendScreenshot = (payload, options) => sendMedia('IMAGE', payload, options)
  const sendLocation = (payload, options) => sendMedia('LOCATION', payload, options)
  const sendContact = (payload, options) => sendMedia('CONTACT', payload, options)

  /**
   * 重试发送失败的消息
   */
  const retryMessage = async message => {
    try {
      await store.dispatch('im/message/retryMessage', {
        sessionId: currentSessionId.value,
        messageId: message.id
      })
      ElMessage.success('重试发送')
    } catch (err) {
      ElMessage.error('重试失败')
    }
  }

  /**
   * 撤回消息
   */
  const recallMessage = async message => {
    try {
      await ElMessageBox.confirm('确定撤回这条消息吗？', '系统提示', { type: 'warning' })
      await store.dispatch('im/message/recallMessage', {
        sessionId: currentSessionId.value,
        messageId: message.id
      })
      ElMessage.success('消息已撤回')
    } catch (err) {
      if (err !== 'cancel') {ElMessage.error('撤回失败')}
    }
  }

  /**
   * 删除消息
   */
  const deleteMessage = async message => {
    try {
      await ElMessageBox.confirm('确定删除这条消息吗？', '系统提示', { type: 'warning' })
      await store.dispatch('im/message/deleteMessage', {
        sessionId: currentSessionId.value,
        messageId: message.id
      })
      ElMessage.success('消息已删除')
    } catch (err) {
      if (err !== 'cancel') {ElMessage.error('删除失败')}
    }
  }

  /**
   * 编辑消息 - 实际调用API
   */
  const editMessage = message => {
    // 防止重复进入编辑模式
    if (editingMessage.value) {
      ElMessage.warning('请先完成当前编辑')
      return
    }
    editingMessage.value = message
  }

  /**
   * 取消编辑
   */
  const cancelEdit = () => {
    editingMessage.value = null
    editingLock.value = false
  }

  /**
   * 确认编辑 - 使用乐观更新 + 服务端确认
   */
  const confirmEdit = async newContent => {
    if (!editingMessage.value || !newContent?.trim()) {return}
    if (editingLock.value) {
      ElMessage.warning('正在处理中，请稍候')
      return
    }

    const message = editingMessage.value
    const oldContent = message.content
    const sessionId = currentSessionId.value

    // 加锁
    editingLock.value = true

    try {
      // 乐观更新：先更新本地状态
      store.commit('im/message/UPDATE_MESSAGE', {
        sessionId,
        message: {
          id: message.id,
          content: newContent.trim(),
          isEdited: true,
          isEditing: true // 标记为编辑中（等待服务端确认）
        }
      })

      // 调用 API
      await messageApi.editMessage(message.id, {
        content: newContent.trim()
      })

      // 服务端确认成功，移除编辑中标记
      store.commit('im/message/UPDATE_MESSAGE', {
        sessionId,
        message: {
          id: message.id,
          isEditing: false
        }
      })

      editingMessage.value = null
      ElMessage.success('消息已编辑')
    } catch (err) {
      // 回滚乐观更新
      store.commit('im/message/UPDATE_MESSAGE', {
        sessionId,
        message: {
          id: message.id,
          content: oldContent,
          isEdited: message.isEdited || false,
          isEditing: false
        }
      })
      ElMessage.error('编辑失败: ' + (err.message || '未知错误'))
      throw err
    } finally {
      editingLock.value = false
    }
  }

  /**
   * 处理 WebSocket 推送的编辑更新（用于同步其他端的编辑）
   */
  const handleEditUpdate = (data) => {
    if (!data?.messageId || !data?.content) {return}
    store.commit('im/message/UPDATE_MESSAGE', {
      sessionId: data.conversationId || currentSessionId.value,
      message: {
        id: data.messageId,
        content: data.content,
        isEdited: true,
        isEditing: false
      }
    })
  }

  /**
   * 收藏消息
   */
  const favoriteMessage = async message => {
    try {
      await addFavorite({
        messageId: message.id,
        conversationId: message.conversationId || message.sessionId
      })
      ElMessage.success('已存入收藏夹')
    } catch (err) {
      ElMessage.error('收藏失败')
    }
  }

  /**
   * 取消收藏
   */
  const unfavoriteMessage = async message => {
    try {
      await removeFavorite(message.id)
      ElMessage.success('已取消收藏')
    } catch (err) {
      ElMessage.error('取消收藏失败')
    }
  }

  /**
   * 标记消息
   */
  const markMessage = async (message, type = 'FLAG') => {
    try {
      await markMessageApi({ messageId: message.id, markerType: type })
      ElMessage.success('标记成功')
    } catch (err) {
      ElMessage.error('标记失败')
    }
  }

  /**
   * 添加表情反应 - 调用API
   */
  const addReaction = async (message, emoji) => {
    try {
      await messageApi.addReaction(message.id, { emoji })
      // 本地更新状态
      const existingReactions = message.reactions || []
      const newReaction = { emoji, count: 1, hasReacted: true }
      store.commit('im/message/UPDATE_MESSAGE', {
        sessionId: currentSessionId.value,
        message: {
          id: message.id,
          reactions: [...existingReactions, newReaction]
        }
      })
      ElMessage.success('添加表情成功')
    } catch (err) {
      if (err.message?.includes('已存在')) {
        // 如果已存在则删除
        await removeReaction(message, emoji)
      } else {
        ElMessage.error('添加表情失败')
      }
    }
  }

  /**
   * 移除表情反应
   */
  const removeReaction = async (message, emoji) => {
    try {
      await messageApi.removeReaction(message.id, emoji)
      // 本地更新状态
      const existingReactions = message.reactions || []
      store.commit('im/message/UPDATE_MESSAGE', {
        sessionId: currentSessionId.value,
        message: {
          id: message.id,
          reactions: existingReactions.filter(r => r.emoji !== emoji)
        }
      })
    } catch (err) {
      console.error('移除表情失败:', err)
    }
  }

  /**
   * 转发消息 - 实际调用API
   */
  const forwardMessage = async (message, targetConversationId) => {
    try {
      await messageApi.forwardMessage({
        messageId: message.id,
        targetConversationId
      })
      ElMessage.success('转发成功')
    } catch (err) {
      ElMessage.error('转发失败')
      throw err
    }
  }

  /**
   * 置顶消息
   */
  const pinMessage = async message => {
    try {
      await messageApi.pinMessage(message.id)
      store.commit('im/message/UPDATE_MESSAGE', {
        sessionId: currentSessionId.value,
        message: { id: message.id, isPinned: true }
      })
      ElMessage.success('消息已置顶')
    } catch (err) {
      ElMessage.error('置顶失败')
    }
  }

  /**
   * 取消置顶消息
   */
  const unpinMessage = async message => {
    try {
      await messageApi.unpinMessage(message.id)
      store.commit('im/message/UPDATE_MESSAGE', {
        sessionId: currentSessionId.value,
        message: { id: message.id, isPinned: false }
      })
      ElMessage.success('已取消置顶')
    } catch (err) {
      ElMessage.error('取消置顶失败')
    }
  }

  /**
   * 回复消息设置
   */
  const replyTo = message => {
    store.commit('im/message/SET_REPLYING_MESSAGE', message)
  }

  /**
   * 取消回复
   */
  const cancelReply = () => {
    store.commit('im/message/SET_REPLYING_MESSAGE', null)
  }

  /**
   * 初始化监听
   */
  const initWebSocket = (callbacks = {}) => {
    onMessage(msg => {
      if (msg.conversationId === currentSessionId.value) {
        store.dispatch('im/message/receiveMessage', { sessionId: msg.conversationId, message: msg })
        callbacks.onNewMessage?.(msg)
      }
    })
    onRecall(data => {
      store.dispatch('im/message/handleMessageRecalled', { sessionId: data.conversationId, messageId: data.messageId })
    })
    onReaction(data => {
      // 更新消息的反应状态
      store.commit('im/message/UPDATE_MESSAGE', {
        sessionId: data.conversationId,
        message: { id: data.messageId, reactions: data.reactions }
      })
    })
    // 监听消息编辑事件
    if (callbacks.onEdit) {
      onMessageStatus?.(data => {
        if (data.type === 'EDIT') {
          handleEditUpdate(data)
          callbacks.onEdit?.(data)
        }
      })
    }
  }

  return {
    // 状态
    messages, loading, noMore, sending, replyingMessage, editingMessage, editingLock,
    // 发送方法
    loadHistory, sendText, sendImage, sendFile, sendVoice, sendScreenshot, sendLocation, retryMessage,
    // 操作方法
    recallMessage, deleteMessage, favoriteMessage, unfavoriteMessage, markMessage,
    edit: editMessage, cancelEdit, confirmEdit, handleEditUpdate,
    addReaction, removeReaction,
    forward: forwardMessage,
    pinMessage, unpinMessage,
    replyTo, cancelReply,
    // WebSocket
    initWebSocket
  }
}

export default useChat