<template>
  <div class="theme-switch">
    <el-tooltip :content="isDark ? '切换到浅色模式' : '切换到深色模式'" placement="bottom">
      <div class="theme-switch__toggle" @click="toggleTheme">
        <Sunny v-if="isDark" class="w-5 h-5" />
        <Moon v-else class="w-5 h-5" />
      </div>
    </el-tooltip>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Sunny, Moon } from '@element-plus/icons-vue'

const isDark = ref(false)

// 初始化主题
onMounted(() => {
  // 优先使用 im_theme，其次使用 app-theme，默认 light
  const savedTheme =
    localStorage.getItem('im_theme') || localStorage.getItem('app-theme') || 'light'
  isDark.value = savedTheme === 'dark'
  applyTheme()
})

const toggleTheme = () => {
  isDark.value = !isDark.value
  const theme = isDark.value ? 'dark' : 'light'

  // 同时更新两个主题存储，保持兼容性
  localStorage.setItem('app-theme', theme)
  localStorage.setItem('im_theme', theme)

  applyTheme()

  // 触发主题变更事件，通知其他组件
  window.dispatchEvent(new CustomEvent('themeChange', { detail: { theme } }))
}

const applyTheme = () => {
  const theme = isDark.value ? 'dark' : 'light'
  document.documentElement.setAttribute('data-theme', theme)

  // 更新 CSS 变量
  const root = document.documentElement
  if (isDark.value) {
    root.style.setProperty('--dt-color-bg-hover', 'rgba(255, 255, 255, 0.08)')
    root.style.setProperty('--dt-color-text-secondary', '#b0b0b0')
  } else {
    root.style.setProperty('--dt-color-bg-hover', 'rgba(0, 0, 0, 0.04)')
    root.style.setProperty('--dt-color-text-secondary', '#606266')
  }
}
</script>

<style lang="scss" scoped>
.theme-switch {
  display: inline-block;

  &__toggle {
    cursor: pointer;
    padding: 8px;
    border-radius: 4px;
    transition: all 0.3s;

    &:hover {
      background: var(--dt-color-bg-hover, rgba(0, 0, 0, 0.04));
    }

    svg {
      color: var(--dt-color-text-secondary, #606266);
    }
  }
}
</style>
