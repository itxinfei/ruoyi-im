<template>
  <div class="im-layout">
    <!-- 左侧导航栏 -->
    <side-nav />

    <!-- 主内容区域 -->
    <div class="im-content">
      <router-view v-slot="{ Component }">
        <keep-alive :include="cachedViews">
          <component :is="Component" />
        </keep-alive>
      </router-view>
    </div>

    <!-- 全局通知提示 -->
    <notification-toast />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import SideNav from '@/components/Layout/SideNav.vue'
import NotificationToast from '@/components/Common/NotificationToast.vue'

const store = useStore()

// 缓存的视图
const cachedViews = ref(['ChatContainer', 'ContactsIndex', 'FileIndex'])

// WebSocket连接
onMounted(() => {
  // 加载会话列表
  store.dispatch('im/loadSessions')

  // 连接WebSocket
  initWebSocket()
})

onUnmounted(() => {
  // 断开WebSocket
  disconnectWebSocket()
})

// 初始化WebSocket
const initWebSocket = () => {
  const token = localStorage.getItem('token')
  if (!token) return

  const wsUrl = `${import.meta.env.VITE_WS_URL || 'ws://localhost:8080'}/ws/im?token=${token}`

  try {
    const ws = new WebSocket(wsUrl)

    ws.onopen = () => {
      console.log('WebSocket连接成功')
      store.dispatch('im/setWsConnected', true)
    }

    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        handleWebSocketMessage(data)
      } catch (e) {
        console.error('WebSocket消息解析失败', e)
      }
    }

    ws.onclose = () => {
      console.log('WebSocket连接关闭')
      store.dispatch('im/setWsConnected', false)
      // 5秒后重连
      setTimeout(initWebSocket, 5000)
    }

    ws.onerror = (error) => {
      console.error('WebSocket错误', error)
    }

    window.__imWebSocket = ws
  } catch (e) {
    console.error('WebSocket连接失败', e)
  }
}

// 断开WebSocket
const disconnectWebSocket = () => {
  if (window.__imWebSocket) {
    window.__imWebSocket.close()
    window.__imWebSocket = null
  }
}

// 处理WebSocket消息
const handleWebSocketMessage = (data) => {
  switch (data.type) {
    case 'message':
      // 收到新消息
      store.dispatch('im/receiveMessage', data.payload)
      break
    case 'typing':
      // 对方正在输入
      store.commit('im/SET_TYPING_STATUS', data.payload)
      break
    case 'read':
      // 消息已读
      store.commit('im/UPDATE_MESSAGE_READ_STATUS', data.payload)
      break
    case 'online':
      // 用户上线
      store.dispatch('im/updateOnlineStatus', { userId: data.payload.userId, status: 'online' })
      break
    case 'offline':
      // 用户离线
      store.dispatch('im/updateOnlineStatus', { userId: data.payload.userId, status: 'offline' })
      break
    default:
      console.log('未知消息类型', data.type)
  }
}
</script>

<style lang="scss" scoped>
.im-layout {
  width: 100vw;
  height: 100vh;
  display: flex;
  overflow: hidden;
  background-color: #f0f2f5;
}

.im-content {
  flex: 1;
  height: 100%;
  overflow: hidden;
  display: flex;
}
</style>
