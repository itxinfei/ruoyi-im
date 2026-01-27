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
  const theme = store.state.im.settings.general.theme
  if (theme) {
    setThemeMode(theme)
  }
})
</script>

<style lang="scss">
/* Material Icons 字体样式优化 */
.material-icons-outlined {
  /* 默认显示图标，移除 opacity: 0 */
  display: inline-block;
  width: 1em;
  height: 1em;
  font-size: 24px;
  line-height: 1;
  vertical-align: middle;
  font-family: 'Material Icons Outlined', sans-serif;
  font-weight: normal;
  font-style: normal;
  letter-spacing: normal;
  text-transform: none;
  white-space: nowrap;
  word-wrap: normal;
  direction: ltr;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

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
