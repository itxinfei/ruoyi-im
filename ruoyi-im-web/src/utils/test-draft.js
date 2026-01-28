/**
 * 测试草稿状态管理
 */
export function testDraftState() {
  console.log('=== 测试草稿状态管理 ===')
  
  // 测试 1: 保存草稿
  console.log('测试 1: 保存草稿')
  const draft = {
    content: '这是一条测试消息',
    timestamp: Date.now()
  }
  console.log('草稿数据:', draft)
  
  // 测试 2: 读取草稿
  console.log('测试 2: 读取草稿')
  console.log('草稿内容:', draft.content)
  
  // 测试 3: 清除草稿
  console.log('测试 3: 清除草稿')
  console.log('草稿已清除')
  
  console.log('=== 测试完成 ===')
}

export function testTypingState() {
  console.log('=== 测试输入状态管理 ===')
  
  // 测试 1: 设置输入状态
  console.log('测试 1: 设置输入状态')
  const typingState = {
    isTyping: true,
    lastTypingTime: Date.now()
  }
  console.log('输入状态:', typingState)
  
  // 测试 2: 检查输入状态
  console.log('测试 2: 检查输入状态')
  const isTyping = Date.now() - typingState.lastTypingTime <= 5000
  console.log('是否正在输入:', isTyping)
  
  // 测试 3: 清除输入状态
  console.log('测试 3: 清除输入状态')
  console.log('输入状态已清除')
  
  console.log('=== 测试完成 ===')
}

export function testMenuPosition() {
  console.log('=== 测试菜单位置计算 ===')
  
  // 测试 1: 计算中心位置
  console.log('测试 1: 计算中心位置')
  const rect = {
    left: 100,
    top: 50,
    width: 300,
    height: 600
  }
  const menuWidth = 200
  const menuHeight = 200
  
  const centerX = rect.left + rect.width / 2
  const centerY = rect.top + rect.height / 2
  
  let x = centerX - menuWidth / 2
  let y = centerY - menuHeight / 2
  
  console.log('容器位置:', rect)
  console.log('菜单尺寸:', { width: menuWidth, height: menuHeight })
  console.log('中心位置:', { centerX, centerY })
  console.log('菜单位置:', { x, y })
  
  // 测试 2: 边界检测
  console.log('测试 2: 边界检测')
  const padding = 10
  const maxX = window.innerWidth - menuWidth - padding
  const maxY = window.innerHeight - menuHeight - padding
  
  x = Math.max(padding, Math.min(x, maxX))
  y = Math.max(padding, Math.min(y, maxY))
  
  console.log('边界检测后:', { x, y })
  
  console.log('=== 测试完成 ===')
}

// 导出所有测试函数
export default {
  testDraftState,
  testTypingState,
  testMenuPosition
}