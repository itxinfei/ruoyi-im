/**
 * 会话级操作 Composable
 *
 * 职责：
 * - 置顶/取消置顶
 * - 免打扰
 * - 清空聊天记录
 */

import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pinConversation, muteConversation } from '@/api/im/conversation'
import { clearConversationMessages } from '@/api/im/message'

export function useChatSessionOps(session) {
  const store = useStore()

  /**
   * 置顶/取消置顶
   */
  const pinSession = async () => {
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

  /**
   * 免打扰
   */
  const muteSession = async () => {
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

  /**
   * 清空聊天记录
   */
  const clearMessages = async () => {
    const s = session?.value ?? session
    if (!s) {
      ElMessage.warning('请先选择会话')
      return
    }

    try {
      await ElMessageBox.confirm(
        `确定要清空与 ${s.name} 的聊天记录吗？`,
        '清空聊天记录',
        {
          type: 'warning',
          confirmButtonText: '确定清空',
          cancelButtonText: '取消',
          confirmButtonClass: 'el-button--danger'
        }
      )

      await clearConversationMessages(s.id)

      // 清空 store 中该会话的消息
      store.commit('im/message/SET_MESSAGES', { sessionId: s.id, messages: [] })

      ElMessage.success('聊天记录已清空')
    } catch {
      // 用户取消或出错
    }
  }

  return {
    pinSession,
    muteSession,
    clearMessages
  }
}
