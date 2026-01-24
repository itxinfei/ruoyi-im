<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'

// 处理 Material Icons 字体加载
onMounted(() => {
  // 方法1: 使用 document.fonts API
  if ('fonts' in document) {
    document.fonts.ready.then(() => {
      // 字体加载完成，显示所有图标
      document.querySelectorAll('.material-icons-outlined').forEach(el => {
        el.style.opacity = '1'
      })
    })
  }
  
  // 方法2: 定期检查（后备方案）
  let checkCount = 0
  const checkInterval = setInterval(() => {
    checkCount++
    
    // 创建测试元素
    const testEl = document.createElement('span')
    testEl.style.fontFamily = 'Material Icons Outlined'
    testEl.style.position = 'absolute'
    testEl.style.visibility = 'hidden'
    testEl.innerHTML = 'chat'
    testEl.style.fontSize = '20px'
    
    document.body.appendChild(testEl)
    const width = testEl.offsetWidth
    document.body.removeChild(testEl)
    
    // 如果字体已加载（宽度不为0）或超时
    if (width > 0 || checkCount > 30) {
      clearInterval(checkInterval)
      document.querySelectorAll('.material-icons-outlined').forEach(el => {
        el.classList.add('loaded')
      })
    }
  }, 100)
  
  // 清理函数
  setTimeout(() => {
    clearInterval(checkInterval)
  }, 3000)
})
</script>

<style lang="scss">
/* Material Icons 字体加载前的处理 */
.material-icons-outlined {
  opacity: 0;
  transition: opacity 0.2s ease-in-out;
  
  /* 使用默认符号作为占位，避免布局跳动 */
  &::before {
    content: "•";
    display: inline-block;
    width: 24px;
    text-align: center;
    font-family: monospace;
  }
}

/* 字体加载完成后 */
.material-icons-outlined.loaded {
  opacity: 1;
  
  &::before {
    display: none;
  }
}

/* 字体加载失败后的后备方案 */
@supports not (font-display: swap) {
  .material-icons-outlined:not(.loaded) {
    font-family: monospace;
  }
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  font-size: 14px;
  color: #262626;
  background-color: #f5f5f5;
  transition: background-color 0.3s ease, color 0.3s ease;
}

#app {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background-color: #f5f5f5;
  transition: background-color 0.3s ease;
}

/* 全局深色模式过渡 */
.dark body {
  background-color: #0f172a;
  color: #f1f5f9;
}

.dark #app {
  background-color: #0f172a;
}

/* 优化常见组件的过渡体验 */
.el-dialog, .el-card, .el-drawer, .el-menu {
  transition: background-color 0.3s ease, border-color 0.3s ease !important;
}

.dingtalk-app {
  transition: all 0.3s ease;
}
</style>
