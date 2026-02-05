<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useStore } from 'vuex'
const store = useStore()

onMounted(() => {
  // 加载本地设置
  store.commit('im/LOAD_SETTINGS')
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
}

.im-app {
  height: 100%;
}
</style>
