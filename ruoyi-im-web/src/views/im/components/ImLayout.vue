<template>
  <div class="im-layout">
    <!-- 顶部导航栏 -->
    <ImHeader />

    <!-- 主体内容区 -->
    <div class="main-body">
      <!-- 左侧导航栏 -->
      <ImSideNav :activeModule="activeModule" :navWidth="navWidth" @switchModule="switchModule" />

      <!-- 内容工作区 -->
      <main class="workspace">
        <!-- 动态组件切换 -->
        <component :is="currentWorkspace" v-if="activeModule" />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, defineAsyncComponent, onMounted } from 'vue'
import { useStore } from 'vuex'
import ImHeader from './ImHeader/index.vue'
import ImSideNav from './ImSideNav/index.vue'

// 异步加载工作区组件
const ChatWorkspace = defineAsyncComponent(() => import('../workspaces/ChatWorkspace.vue'))
const ContactsWorkspace = defineAsyncComponent(() => import('../workspaces/ContactsWorkspace.vue'))
const WorkbenchWorkspace = defineAsyncComponent(() => import('../workspaces/WorkbenchWorkspace.vue'))
const DriveWorkspace = defineAsyncComponent(() => import('../workspaces/DriveWorkspace.vue'))

const store = useStore()

// 响应式状态
const activeModule = ref('chat')
const navWidth = ref(60)

// 当前工作区组件
const currentWorkspace = computed(() => {
  const workspaceMap = {
    chat: ChatWorkspace,
    contacts: ContactsWorkspace,
    workbench: WorkbenchWorkspace,
    drive: DriveWorkspace
  }
  return workspaceMap[activeModule.value] || ChatWorkspace
})

// 切换模块
const switchModule = (moduleKey) => {
  activeModule.value = moduleKey
}

// 生命周期钩子
onMounted(() => {
  // 初始化用户数据
  store.dispatch('user/getInfo')
})
</script>

<style scoped>
.im-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background-color: #f0f2f5;
}

.main-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.workspace {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
</style>