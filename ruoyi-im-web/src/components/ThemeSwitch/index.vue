<template>
  <div class="theme-switch">
    <el-tooltip :content="isDark ? '切换到浅色模式' : '切换到深色模式'" placement="bottom">
      <div class="theme-switch__toggle" @click="toggleTheme">
        <i :class="isDark ? 'el-icon-sunny' : 'el-icon-moon'"></i>
      </div>
    </el-tooltip>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const isDark = ref(false)

// 初始化主题
onMounted(() => {
  isDark.value = localStorage.getItem('theme') === 'dark'
  applyTheme()
})

const toggleTheme = () => {
  isDark.value = !isDark.value
  localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
  applyTheme()
}

const applyTheme = () => {
  document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : 'light')
}
</script>

<style lang="scss" scoped>
.theme-switch {
  display: inline-block;
  margin-right: 15px;

  &__toggle {
    cursor: pointer;
    padding: 8px;
    border-radius: 4px;
    transition: all 0.3s;

    &:hover {
      background: rgba(0, 0, 0, 0.025);
    }

    i {
      font-size: 20px;
      color: #606266;
    }
  }
}
</style>
