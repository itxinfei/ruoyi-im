/**
 * 测试状态管理
 */
import { useStore } from 'vuex'

export function testStateManagement() {
  const store = useStore()
  
  console.log('=== 测试状态管理 ===')
  
  // 测试 1: 检查 store 结构
  console.log('1. 检查 store 结构')
  console.log('store.state.im.session:', store.state.im.session)
  console.log('store.getters["im/session/hasDraft"]:', store.getters['im/session/hasDraft'])
  console.log('store.getters["im/session/getDraftPreview"]:', store.getters['im/session/getDraftPreview'])
  console.log('store.getters["im/session/isTyping"]:', store.getters['im/session/isTyping'])
  
  // 测试 2: 测试草稿保存和读取
  console.log('2. 测试草稿保存和读取')
  const testConversationId = 'test_conversation_123'
  
  // 保存草稿
  store.dispatch('im/session/saveDraft', {
    conversationId: testConversationId,
    content: '这是一条测试草稿消息'
  })
  
  // 立即读取草稿
  setTimeout(() => {
    const hasDraft = store.getters['im/session/hasDraft'](testConversationId)
    const getDraftPreview = store.getters['im/session/getDraftPreview'](testConversationId)
    const getDraftContent = store.getters['im/session/getDraftContent'](testConversationId)
    
    console.log('hasDraft:', hasDraft)
    console.log('getDraftPreview:', getDraftPreview)
    console.log('getDraftContent:', getDraftContent)
    
    if (hasDraft && getDraftContent === '这是一条测试草稿消息') {
      console.log('✅ 草稿保存和读取功能正常')
    } else {
      console.log('❌ 草稿保存和读取功能异常')
    }
    
    // 测试 3: 测试输入状态
    console.log('3. 测试输入状态')
    store.dispatch('im/session/setTyping', {
      conversationId: testConversationId,
      isTyping: true
    })
    
    setTimeout(() => {
      const isTyping = store.getters['im/session/isTyping'](testConversationId)
      console.log('isTyping:', isTyping)
      
      if (isTyping) {
        console.log('✅ 输入状态功能正常')
      } else {
        console.log('❌ 输入状态功能异常')
      }
      
      // 清理测试数据
      store.dispatch('im/session/clearDraft', testConversationId)
      store.dispatch('im/session/clearTyping', testConversationId)
      
      console.log('=== 测试完成 ===')
    }, 100)
  }, 100)
}

export default testStateManagement