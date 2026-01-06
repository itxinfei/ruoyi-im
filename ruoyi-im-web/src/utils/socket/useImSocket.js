// Very lightweight WebSocket wrapper for IM stream
export function createImSocket(url, onMessage) {
  let ws = null
  let reconnectDelay = 1000
  let shouldReconnect = true

  function connect() {
    ws = new WebSocket(url)

    ws.onopen = () => {
      reconnectDelay = 1000
    }

    ws.onmessage = event => {
      try {
        const data = typeof event.data === 'string' ? JSON.parse(event.data) : event.data
        onMessage?.(data)
      } catch (e) {
        // ignore non-JSON
      }
    }

    ws.onclose = () => {
      if (!shouldReconnect) return
      // 简单回连策略
      setTimeout(() => {
        reconnectDelay = Math.min(reconnectDelay * 1.5, 30_000)
        connect()
      }, reconnectDelay)
    }

    ws.onerror = () => {
      ws.close()
    }
  }

  connect()

  return {
    close() {
      shouldReconnect = false
      ws && ws.close()
    }
  }
}
