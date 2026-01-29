/**
 * 输入框草稿管理 Composable
 *
 * 职责：
 * - 管理聊天输入框的草稿保存/加载/清除
 * - 与 Vuex store 集成
 * - 自动保存输入内容
 *
 * 使用方式：
 * ```js
 * const { messageContent, saveDraft, loadDraft, clearDraft } = useInputDraft({
 *   session,
 *   onDraftChange
 * })
 * ```
 */
import { ref, watch } from 'vue'
import { useStore } from 'vuex'

const DRAFT_STORAGE_KEY = 'im_message_drafts'

export function useInputDraft(options = {}) {
  const {
    session = ref(null),
    onDraftChange = null,
    autoSave = true
  } = options

  const store = useStore()
  const messageContent = ref('')
  const currentConversationId = ref(null)

  /**
   * 保存草稿
   * @param {string} conversationId - 会话ID
   * @param {string} content - 草稿内容
   */
  const saveDraft = (conversationId, content) => {
    if (!conversationId) return

    store.dispatch('im/session/saveDraft', { conversationId, content })

    if (onDraftChange) {
      onDraftChange({
        conversationId,
        hasDraft: content.trim().length > 0,
        preview: content.trim().slice(0, 50)
      })
    }
  }

  /**
   * 清除草稿
   * @param {string} conversationId - 会话ID
   */
  const clearDraft = (conversationId) => {
    if (!conversationId) return

    store.dispatch('im/session/clearDraft', conversationId)

    if (onDraftChange) {
      onDraftChange({
        conversationId,
        hasDraft: false,
        preview: ''
      })
    }
  }

  /**
   * 加载草稿
   * @param {string} conversationId - 会话ID
   * @param {Function} callback - 加载完成后的回调
   */
  const loadDraft = (conversationId, callback = null) => {
    if (!conversationId) return

    const draftContent = store.getters['im/session/getDraftContent'](conversationId)
    if (draftContent) {
      messageContent.value = draftContent
      if (callback) callback(draftContent)
    } else {
      messageContent.value = ''
    }

    currentConversationId.value = conversationId
  }

  /**
   * 立即保存当前草稿
   */
  const saveCurrentDraft = () => {
    if (currentConversationId.value) {
      saveDraft(currentConversationId.value, messageContent.value)
    }
  }

  /**
   * 清空输入并清除草稿
   */
  const clear = () => {
    if (currentConversationId.value) {
      clearDraft(currentConversationId.value)
    }
    messageContent.value = ''
  }

  // 监听会话变化，自动加载草稿
  if (autoSave) {
    watch(() => session.value?.id, (newId, oldId) => {
      // 保存旧会话的草稿
      if (oldId && messageContent.value.trim()) {
        saveDraft(oldId, messageContent.value)
      }
      // 加载新会话的草稿
      if (newId && newId !== oldId) {
        loadDraft(newId)
      }
    }, { immediate: true })

    // 监听输入内容变化，自动保存
    watch(messageContent, (newContent) => {
      if (currentConversationId.value && newContent !== undefined) {
        saveDraft(currentConversationId.value, newContent)
      }
    })
  }

  return {
    // 状态
    messageContent,
    currentConversationId,

    // 方法
    saveDraft,
    clearDraft,
    loadDraft,
    saveCurrentDraft,
    clear
  }
}
