/**
 * WebSocket 事件处理 Composable
 *
 * 职责：
 * - 监听 WebSocket 消息到达事件，commit 到 store
 * - 监听消息状态更新事件
 * - 监听表情回复事件
 * - 新消息通知
 */

import { computed } from 'vue'
import { useStore } from 'vuex'
import { useImWebSocket } from '@/composables/useImWebSocket'
import { useMessageTransformation } from '@/composables/useMessageTransformation.js'

export function useChatWebSocket(sessionId, currentUser) {
  const store = useStore()
  const { onMessage, onMessageStatus, onReaction } = useImWebSocket()
  const { transformMsg } = useMessageTransformation({ currentUser })

  const currentSessionId = computed(() => sessionId?.value ?? sessionId)

  /**
   * 初始化 WebSocket 事件监听器
   * @param {Object} callbacks - 可选的附加回调
   * @param {Function} callbacks.onNewMessage - 新消息到达时的额外处理（如滚动到底部）
   */
  const initListeners = (callbacks = {}) => {
    // 监听新消息
    onMessage(msg => {
      try {
        const sid = currentSessionId.value
        if (msg.conversationId !== sid) { return }

        // 通过 store action 处理消息（包含去重、ACK 等）
        const transformedMsg = transformMsg(msg)
        store.dispatch('im/message/receiveMessage', transformedMsg)

        // 额外回调（如滚动到底部）
        if (callbacks.onNewMessage) {
          callbacks.onNewMessage(transformedMsg)
        }

        // 新消息通知
        if (!transformedMsg.isOwn) {
          import('@/utils/messageNotification').then(({ showMessageNotification, shouldNotify }) => {
            const session = store.state.im?.session?.currentSession
            if (shouldNotify(msg, currentUser.value, session)) {
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
          }).catch(() => {})
        }
      } catch (err) {
        console.error('处理WebSocket消息失败:', err)
      }
    })

    // 监听消息状态更新
    onMessageStatus(data => {
      const sid = currentSessionId.value
      if (data.conversationId !== sid) { return }

      // 映射后端 sendStatus 数值到前端状态字符串
      const statusMap = {
        0: 'sending',
        1: 'sending',
        2: 'delivered',
        3: 'read',
        4: 'failed'
      }
      const sendStatus = data.sendStatus != null ? Number(data.sendStatus) : -1

      if (statusMap[sendStatus]) {
        // 通过 messageId 或 clientMsgId 定位消息
        const messageId = data.messageId || data.clientMsgId
        if (messageId) {
          store.commit('im/message/MARK_MESSAGE_STATUS', {
            sessionId: sid,
            messageId,
            status: statusMap[sendStatus]
          })
        }
      }
    })

    // 监听表情回复更新
    onReaction(data => {
      store.dispatch('im/message/handleReactionUpdate', {
        messageId: data.messageId,
        emoji: data.emoji,
        userId: data.userId,
        userName: data.userName,
        userAvatar: data.userAvatar,
        isAdd: data.isAdd !== false
      })
    })
  }

  return { initListeners }
}
