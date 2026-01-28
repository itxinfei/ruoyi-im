/**
 * 调试草稿状态管理
 */

// 调试函数：检查草稿状态
export function debugDraftState(store) {
  console.log('=== 调试草稿状态 ===')
  console.log('store.state.im.session.drafts:', store.state.im.session?.drafts)
  console.log('hasDraft getter:', store.getters['im/session/hasDraft'])
  console.log('getDraftPreview getter:', store.getters['im/session/getDraftPreview'])
  
  // 测试 getter
  const testConversationId = 'test_123'
  console.log(`hasDraft('${testConversationId}'):`, store.getters['im/session/hasDraft'](testConversationId))
  console.log(`getDraftPreview('${testConversationId}'):`, store.getters['im/session/getDraftPreview'](testConversationId))
  
  console.log('=== 调试完成 ===')
}

// 调试函数：检查输入状态
export function debugTypingState(store) {
  console.log('=== 调试输入状态 ===')
  console.log('store.state.im.session.typingSessions:', store.state.im.session?.typingSessions)
  console.log('isTyping getter:', store.getters['im/session/isTyping'])
  console.log('getTypingSessionIds getter:', store.getters['im/session/getTypingSessionIds'])
  
  // 测试 getter
  const testConversationId = 'test_456'
  console.log(`isTyping('${testConversationId}'):`, store.getters['im/session/isTyping'](testConversationId))
  
  console.log('=== 调试完成 ===')
}

// 调试函数：监听状态变化
export function watchDraftState(store, callback) {
  return store.watch(
    () => store.state.im.session?.drafts,
    (newDrafts, oldDrafts) => {
      console.log('草稿状态变化:', { oldDrafts, newDrafts })
      callback?.(newDrafts, oldDrafts)
    },
    { deep: true }
  )
}

// 调试函数：监听输入状态变化
export function watchTypingState(store, callback) {
  return store.watch(
    () => store.state.im.session?.typingSessions,
    (newTyping, oldTyping) => {
      console.log('输入状态变化:', { oldTyping, newTyping })
      callback?.(newTyping, oldTyping)
    },
    { deep: true }
  )
}

export default {
  debugDraftState,
  debugTypingState,
  watchDraftState,
  watchTypingState
}