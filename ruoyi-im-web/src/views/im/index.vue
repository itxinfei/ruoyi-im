<template>
  <div class="im-page">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useStore } from 'vuex'

const store = useStore()

onMounted(() => {
  // 初始化 WebSocket 连接
  store.dispatch('im/initWebSocket')

  // 加载初始数据
  Promise.all([
    store.dispatch('im/loadContacts'),
    store.dispatch('im/loadGroups'),
    store.dispatch('im/loadSessions'),
  ]).catch(err => {
    console.error('加载初始数据失败:', err)
  })
})
</script>

<style lang="scss" scoped>
.im-page {
  height: 100%;
  width: 100%;
  overflow: hidden;
}
</style>
