/**
 * 输入框草稿管理 Composable
 *
 * 职责：
 * - 管理聊天输入框的草稿保存/加载/清除
 * - 与 Vuex store 集成
 * - 自动保存输入内容（防抖优化）
 *
 * 使用方式：
 * ```js
 * const { messageContent, saveDraft, loadDraft, clearDraft } = useInputDraft({
 *   session,
 *   onDraftChange
 * })
 * ```
 */
import { ref, watch, onUnmounted } from 'vue'
import { useStore } from 'vuex'

const DRAFT_STORAGE_KEY = 'im_message_drafts'
const DEBOUNCE_DELAY = 500 // 草稿保存防抖延迟（毫秒）

/**
 * 简单的防抖函数实现
 * @param {Function} func - 要防抖的函数
 * @param {number} delay - 延迟时间（毫秒）
 * @returns {Function} 防抖后的函数
 */
function debounce(func, delay) {
  let timeoutId = null
  let lastArgs = null
  let lastThis = null

  const debounced = function (...args) {
    lastArgs = args
    lastThis = this

    if (timeoutId) {
      clearTimeout(timeoutId)
    }

    timeoutId = setTimeout(() => {
      func.apply(lastThis, lastArgs)
      timeoutId = null
      lastArgs = null
      lastThis = null
    }, delay)
  }

  // 立即执行待处理的方法
  debounced.flush = () => {
    if (timeoutId) {
      clearTimeout(timeoutId)
      func.apply(lastThis, lastArgs)
      timeoutId = null
      lastArgs = null
      lastThis = null
    }
  }

  return debounced
}

export function useInputDraft(options = {}) {
  const {
    session = ref(null),
    onDraftChange = null,
    autoSave = true
  } = options

  const store = useStore()
  const messageContent = ref('')
  const currentConversationId = ref(null)
  const isUnmounted = ref(false) // 标记组件是否已卸载

  /**
   * 保存草稿
   * @param {string} conversationId - 会话ID
   * @param {string} content - 草稿内容
   */
  const saveDraft = (conversationId, content) => {
    if (!conversationId) return

    // 只同步到 store，不触发可能导致 UI 更新的回调
    store.dispatch('im/session/saveDraft', { conversationId, content })

    // 仅在组件未卸载时触发回调
    if (onDraftChange && !isUnmounted.value) {
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
    // 创建防抖版本的保存函数
    const debouncedSaveDraft = debounce((conversationId, content) => {
      saveDraft(conversationId, content)
    }, DEBOUNCE_DELAY)

    watch(() => session.value?.id, (newId, oldId) => {
      // 保存旧会话的草稿（立即保存，不防抖）
      if (oldId && messageContent.value.trim()) {
        saveDraft(oldId, messageContent.value)
      }
      // 加载新会话的草稿
      if (newId && newId !== oldId) {
        loadDraft(newId)
      }
    }, { immediate: true })

    // 监听输入内容变化，防抖保存
    watch(messageContent, (newContent) => {
      if (currentConversationId.value && newContent !== undefined) {
        debouncedSaveDraft(currentConversationId.value, newContent)
      }
    })

    // 组件卸载时立即保存待处理的草稿
    onUnmounted(() => {
      isUnmounted.value = true
      // 只保存到 store，不触发 onDraftChange 回调
      if (currentConversationId.value && messageContent.value) {
        store.dispatch('im/session/saveDraft', {
          conversationId: currentConversationId.value,
          content: messageContent.value
        })
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
