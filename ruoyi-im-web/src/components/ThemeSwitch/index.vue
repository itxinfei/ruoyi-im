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
  isDark.value = localStorage.getItem('app-theme') === 'dark'
  applyTheme()
})

const toggleTheme = () => {
  isDark.value = !isDark.value
  localStorage.setItem('app-theme', isDark.value ? 'dark' : 'light')
  applyTheme()
}

const applyTheme = () => {
  document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : 'light')
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
