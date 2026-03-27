<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useStore } from 'vuex'
import { useTheme } from '@/composables/useTheme'

const store = useStore()
const { setThemeMode } = useTheme()

onMounted(() => {
  // 加载本地设置
  store.commit('im/LOAD_SETTINGS')

  // 同步主题设置
  const { theme } = store.state.im.settings.general
  if (theme) {
    setThemeMode(theme)
  }
})
</script>

<style lang="scss">
#app {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background-color: var(--dt-bg-body);
  transition: background-color var(--dt-transition-base);
}

/* 优化常见组件的过渡体验 */
.el-dialog, .el-card, .el-drawer, .el-menu {
  transition: background-color var(--dt-transition-base), border-color var(--dt-transition-base) !important;
}

.dingtalk-app {
  height: 100%;
  transition: all var(--dt-transition-base);
}
</style>
