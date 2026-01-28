/**
 * 响应式系统测试工具
 * 用于验证 Vuex mutations 是否正确触发 Vue 3 响应式更新
 */

export const testDraftReactivity = (store) => {
  console.log('=== 草稿响应式测试 ===')
  
  const testConversationId = 'test-reactivity-conversation'
  
  // 测试1: 设置草稿
  console.log('1. 设置草稿')
  store.dispatch('im/session/saveDraft', {
    conversationId: testConversationId,
    content: '测试草稿内容'
  })
  
  const hasDraft1 = store.getters['im/session/hasDraft'](testConversationId)
  const preview1 = store.getters['im/session/getDraftPreview'](testConversationId)
  console.log('  是否有草稿:', hasDraft1, '(期望: true)')
  console.log('  草稿预览:', preview1, '(期望: "测试草稿内容")')
  
  // 测试2: 更新草稿
  console.log('\n2. 更新草稿')
  store.dispatch('im/session/saveDraft', {
    conversationId: testConversationId,
    content: '更新的草稿内容'
  })
  
  const hasDraft2 = store.getters['im/session/hasDraft'](testConversationId)
  const preview2 = store.getters['im/session/getDraftPreview'](testConversationId)
  console.log('  是否有草稿:', hasDraft2, '(期望: true)')
  console.log('  草稿预览:', preview2, '(期望: "更新的草稿内容")')
  
  // 测试3: 清除草稿
  console.log('\n3. 清除草稿')
  store.dispatch('im/session/clearDraft', testConversationId)
  
  const hasDraft3 = store.getters['im/session/hasDraft'](testConversationId)
  const preview3 = store.getters['im/session/getDraftPreview'](testConversationId)
  console.log('  是否有草稿:', hasDraft3, '(期望: false)')
  console.log('  草稿预览:', preview3, '(期望: "")')
  
  // 清理测试数据
  store.dispatch('im/session/clearDraft', testConversationId)
  
  console.log('\n=== 测试完成 ===')
}

export const testTypingReactivity = (store) => {
  console.log('=== 输入状态响应式测试 ===')
  
  const testConversationId = 'test-reactivity-conversation'
  
  // 测试1: 设置输入状态
  console.log('1. 设置输入状态')
  store.dispatch('im/session/setTyping', {
    conversationId: testConversationId,
    isTyping: true
  })
  
  const isTyping1 = store.getters['im/session/isTyping'](testConversationId)
  console.log('  是否正在输入:', isTyping1, '(期望: true)')
  
  // 测试2: 清除输入状态
  console.log('\n2. 清除输入状态')
  store.dispatch('im/session/clearTyping', testConversationId)
  
  const isTyping2 = store.getters['im/session/isTyping'](testConversationId)
  console.log('  是否正在输入:', isTyping2, '(期望: false)')
  
  // 测试3: 重新设置输入状态
  console.log('\n3. 重新设置输入状态')
  store.dispatch('im/session/setTyping', {
    conversationId: testConversationId,
    isTyping: true
  })
  
  const isTyping3 = store.getters['im/session/isTyping'](testConversationId)
  console.log('  是否正在输入:', isTyping3, '(期望: true)')
  
  // 测试4: 通过 false 清除
  console.log('\n4. 通过 isTyping=false 清除')
  store.dispatch('im/session/setTyping', {
    conversationId: testConversationId,
    isTyping: false
  })
  
  const isTyping4 = store.getters['im/session/isTyping'](testConversationId)
  console.log('  是否正在输入:', isTyping4, '(期望: false)')
  
  // 清理测试数据
  store.dispatch('im/session/clearTyping', testConversationId)
  
  console.log('\n=== 测试完成 ===')
}

export const testStateFlow = (store) => {
  console.log('=== 完整状态流测试 ===')
  
  const testConversationId = 'test-reactivity-conversation'
  
  // 模拟完整流程
  console.log('1. 开始输入')
  store.dispatch('im/session/setTyping', {
    conversationId: testConversationId,
    isTyping: true
  })
  
  console.log('2. 保存草稿')
  store.dispatch('im/session/saveDraft', {
    conversationId: testConversationId,
    content: '这是一条测试消息'
  })
  
  console.log('3. 获取状态')
  const isTyping = store.getters['im/session/isTyping'](testConversationId)
  const hasDraft = store.getters['im/session/hasDraft'](testConversationId)
  const preview = store.getters['im/session/getDraftPreview'](testConversationId)
  
  console.log('  是否正在输入:', isTyping, '(期望: true)')
  console.log('  是否有草稿:', hasDraft, '(期望: true)')
  console.log('  草稿预览:', preview, '(期望: "这是一条测试消息")')
  
  console.log('\n4. 模拟发送消息')
  // 先清除输入状态
  store.dispatch('im/session/clearTyping', testConversationId)
  console.log('  输入状态已清除')
  
  // 延迟清除草稿
  setTimeout(() => {
    store.dispatch('im/session/clearDraft', testConversationId)
    console.log('  草稿已清除')
    
    console.log('\n5. 验证最终状态')
    const finalIsTyping = store.getters['im/session/isTyping'](testConversationId)
    const finalHasDraft = store.getters['im/session/hasDraft'](testConversationId)
    
    console.log('  是否正在输入:', finalIsTyping, '(期望: false)')
    console.log('  是否有草稿:', finalHasDraft, '(期望: false)')
    
    console.log('\n=== 测试完成 ===')
  }, 100)
}

// 在浏览器控制台中使用
if (typeof window !== 'undefined') {
  window.testDraftReactivity = testDraftReactivity
  window.testTypingReactivity = testTypingReactivity
  window.testStateFlow = testStateFlow
  
  console.log('响应式测试工具已加载到 window 对象')
  console.log('使用方法:')
  console.log('  window.testDraftReactivity(store)')
  console.log('  window.testTypingReactivity(store)')
  console.log('  window.testStateFlow(store)')
}
