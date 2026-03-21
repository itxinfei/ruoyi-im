/**
 * useWebSocket.js (Vuex 回归修复版)
 */
import { ref, onUnmounted } from 'vue';
import { useStore } from 'vuex';
import { ElMessage } from 'element-plus';

export function useWebSocket() {
  const store = useStore();
  const ws = ref(null);
  const isConnected = ref(false);
  
  let reconnectTimer = null;
  let heartbeatTimer = null;
  let reconnectAttempts = 0;
  const MAX_RECONNECT = 5;

  const connect = (token) => {
    const wsUrl = `ws://${window.location.host}/ws/im?token=${token}`;
    ws.value = new WebSocket(wsUrl);

    ws.value.onopen = () => {
      isConnected.value = true;
      reconnectAttempts = 0;
      startHeartbeat();
    };

    ws.value.onmessage = (event) => {
      try {
        const frame = JSON.parse(event.data);
        processFrame(frame);
      } catch (e) {
        console.error('WS Frame Parse Error', e);
      }
    };

    ws.value.onclose = () => {
      isConnected.value = false;
      stopHeartbeat();
      attemptReconnect(token);
    };

    ws.value.onerror = () => ws.value.close();
  };

  const processFrame = (frame) => {
    const { type, action, data } = frame;

    if (type === 'MESSAGE' && action === 'RECEIVE') {
      // 通过 Vuex 分发接收消息逻辑
      store.dispatch('imMessage/receiveMessage', data);
    }
  };

  const startHeartbeat = () => {
    heartbeatTimer = setInterval(() => {
      if (ws.value?.readyState === WebSocket.OPEN) {
        ws.value.send(JSON.stringify({ type: 'AUTH', action: 'PING' }));
      }
    }, 30000);
  };

  const stopHeartbeat = () => {
    if (heartbeatTimer) clearInterval(heartbeatTimer);
  };

  const attemptReconnect = (token) => {
    if (reconnectAttempts >= MAX_RECONNECT) return;
    reconnectAttempts++;
    reconnectTimer = setTimeout(() => connect(token), 3000);
  };

  onUnmounted(() => {
    stopHeartbeat();
    if (reconnectTimer) clearTimeout(reconnectTimer);
    if (ws.value) ws.value.close();
  });

  return { isConnected, connect };
}
