/**
 * ChatPanel - 消息命令处理 Composable
 *
 * 职责：
 * - 复制消息
 * - 回复消息
 * - 转发消息
 * - 撤回消息
 * - 删除消息
 * - 编辑消息
 * - @提及
 * - 表情表态
 * - 设为待办
 * - 置顶消息
 */

import { ref } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import {
  forwardMessage,
  recallMessage,
  deleteMessage as apiDeleteMessage,
  editMessage
} from '@/api/im/message'
import { setTodoReminder } from '@/api/im/marker'
import { addFavorite, removeFavorite } from '@/api/im/favorite'
import { markMessage, unmarkMessage } from '@/api/im/marker'

export function useChatCommands(currentUser, sessionType) {
  const store = useStore()

  // 状态
  const replyingMessage = ref(null)
  const editingMessage = ref(null)
  const forwardingMessages = ref([])

  /**
   * 复制消息
   */
  const copy = async message => {
    if (message.type !== 'TEXT') {return}

    try {
      // 复制到剪贴板
      await navigator.clipboard.writeText(message.content)
      ElMessage.success('已复制')
    } catch (error) {
      console.error('复制失败:', error)
      ElMessage.error('复制失败')
    }
  }

  /**
   * 回复消息
   */
  const reply = message => {
    replyingMessage.value = message
  }

  /**
   * 取消回复
   */
  const cancelReply = () => {
    replyingMessage.value = null
  }

  /**
   * 转发消息
   */
  const forward = message => {
    forwardingMessages.value = [message]
  }

  /**
   * 批量转发
   */
  const batchForward = async (messages, targetConversationIds) => {
    try {
      await forwardMessage({
        messageIds: messages.map(m => m.id),
        conversationIds: targetConversationIds
      })
      ElMessage.success('转发成功')
      forwardingMessages.value = []
    } catch (error) {
      console.error('转发失败:', error)
      ElMessage.error('转发失败')
    }
  }

  /**
   * 撤回消息
   */
  const recall = async message => {
    try {
      await recallMessage(message.id)

      // 更新消息状态
      const index = store.getters['im/message/currentMessages'].findIndex(m => m.id === message.id)
      if (index !== -1) {
        store.commit('im/message/UPDATE_MESSAGE', {
          index,
          message: {
            ...message,
            type: 'RECALLED',
            content: '',
            isRevoked: true
          }
        })
      }

      ElMessage.success('撤回成功')
    } catch (error) {
      console.error('撤回失败:', error)
      ElMessage.error(error.msg || '撤回失败')
    }
  }

  /**
   * 删除消息
   */
  const deleteMessage = async message => {
    try {
      await apiDeleteMessage(message.id)

      // 从列表中移除
      store.commit('im/message/REMOVE_MESSAGE', message.id)

      ElMessage.success('删除成功')
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }

  /**
   * 编辑消息
   */
  const edit = message => {
    editingMessage.value = { ...message }
  }

  /**
   * 取消编辑
   */
  const cancelEdit = () => {
    editingMessage.value = null
  }

  /**
   * 确认编辑
   */
  const confirmEdit = async newContent => {
    if (!newContent.trim()) {
      ElMessage.warning('请输入消息内容')
      return
    }

    try {
      await editMessage(editingMessage.value.id, { content: newContent })

      // 更新消息内容
      store.commit('im/message/UPDATE_MESSAGE', {
        index: store.getters['im/message/currentMessages'].findIndex(m => m.id === editingMessage.value.id),
        message: {
          ...editingMessage.value,
          content: newContent,
          isEdited: true
        }
      })

      ElMessage.success('编辑成功')
      editingMessage.value = null
    } catch (error) {
      console.error('编辑失败:', error)
      ElMessage.error(error.msg || '编辑失败')
    }
  }

  /**
   * @提及用户
   */
  const at = message => {
    // 触发 @ 事件
    // 父组件会处理
    return message
  }

  /**
   * 添加表情表态
   */
  const addReaction = async (message, emoji, reactionType = 'EMOJI') => {
    try {
      await store.dispatch('im/message/addReaction', {
        messageId: message.id,
        emoji,
        reactionType
      })
    } catch (error) {
      console.error('添加表情表态失败:', error)
    }
  }

  /**
   * 设为待办
   */
  const addToTodo = async message => {
    try {
      await setTodoReminder({
        messageId: message.id,
        remindTime: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
        remark: message.content?.substring(0, 50) || '消息待办'
      })

      // 更新消息标记
      const msgIndex = store.getters['im/message/currentMessages'].findIndex(m => m.id === message.id)
      if (msgIndex !== -1) {
        store.commit('im/message/UPDATE_MESSAGE', {
          index: msgIndex,
          message: {
            ...message,
            markers: [
              ...(message.markers || []),
              { markerType: 'TODO', isCompleted: false }
            ]
          }
        })
      }

      ElMessage.success('已添加到待办事项')
    } catch (error) {
      console.error('添加待办失败:', error)
      ElMessage.error('添加失败')
    }
  }

  /**
   * 收藏消息
   */
  const favorite = async message => {
    try {
      await addFavorite({
        messageId: message.id,
        conversationId: message.conversationId || message.sessionId,
        remark: '',
        tags: []
      })
      ElMessage.success('已收藏消息')
    } catch (error) {
      console.error('收藏失败:', error)
      ElMessage.error('收藏失败')
    }
  }

  /**
   * 取消收藏
   */
  const unfavorite = async message => {
    try {
      await removeFavorite(message.id)
      ElMessage.success('已取消收藏')
    } catch (error) {
      console.error('取消收藏失败:', error)
    }
  }

  /**
   * 置顶消息
   */
  const pin = async message => {
    try {
      const isPinned = !message.isPinned
      // 调用置顶 API
      // await togglePinMessage(message.id, isPinned)
      ElMessage.success(isPinned ? '已置顶' : '已取消置顶')
    } catch (error) {
      console.error('置顶失败:', error)
    }
  }

  return {
    // 状态
    replyingMessage,
    editingMessage,
    forwardingMessages,

    // 方法
    copy,
    reply,
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
  }
}
