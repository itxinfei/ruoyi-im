/**
 * 网络质量监测 Composable
 * 基于 WebRTC Stats API 实时监测网络连接质量
 * @author RuoYi-IM
 */
import { ref, computed, onUnmounted } from 'vue'
import { WebRTCStatsCollector, calculateNetworkQuality, getQualityConfig } from '@/utils/webrtc-stats'

/**
 * 使用网络质量监测
 * @param {Ref<RTCPeerConnection|null>} peerConnection - WebRTC 连接对象
 * @param {Object} options - 配置选项
 * @returns {Object} 网络质量相关的状态和方法
 */
export function useNetworkStats(peerConnection, options = {}) {
  const {
    updateInterval = 1000, // 更新间隔（毫秒）
    autoStart = true,      // 自动开始监测
  } = options

  // 状态
  const isMonitoring = ref(false)
  const networkQuality = ref('disconnected')
  const connectionStats = ref({
    delay: 0,
    packetLoss: 0,
    jitter: 0,
    bitrate: 0,
    bytesReceived: 0,
    bytesSent: 0,
  })
  const statsHistory = ref([])

  // 统计收集器
  let collector = null

  // 计算属性 - 质量等级配置
  const qualityConfig = computed(() => getQualityConfig(networkQuality.value))

  // 计算属性 - 质量等级（1-4）
  const qualityLevel = computed(() => qualityConfig.value.level)

  // 计算属性 - 质量颜色
  const qualityColor = computed(() => qualityConfig.value.color)

  // 计算属性 - 质量图标
  const qualityIcon = computed(() => qualityConfig.value.icon)

  // 计算属性 - 是否网络良好
  const isGoodNetwork = computed(() =>
    ['excellent', 'good'].includes(networkQuality.value)
  )

  // 计算属性 - 是否网络较差
  const isPoorNetwork = computed(() =>
    ['fair', 'poor', 'disconnected'].includes(networkQuality.value)
  )

  // 计算属性 - 延迟状态描述
  const delayStatus = computed(() => {
    const delay = connectionStats.value.delay
    if (delay < 50) return '优秀'
    if (delay < 100) return '良好'
    if (delay < 200) return '一般'
    if (delay < 500) return '较差'
    return '很差'
  })

  /**
   * 开始监测
   */
  const startMonitoring = () => {
    if (!peerConnection.value) {
      console.warn('PeerConnection 不存在，无法开始监测')
      return
    }

    if (isMonitoring.value) {
      return
    }

    // 创建收集器
    collector = new WebRTCStatsCollector(peerConnection.value)

    // 添加监听器
    collector.addListener((stats) => {
      connectionStats.value = {
        delay: stats.currentRoundTripTime,
        packetLoss: stats.packetLossRate,
        jitter: stats.jitter,
        bitrate: stats.bitrate,
        bytesReceived: stats.bytesReceived,
        bytesSent: stats.bytesSent,
      }
      networkQuality.value = stats.quality

      // 记录历史（保留最近30秒）
      statsHistory.value.push({
        timestamp: Date.now(),
        quality: stats.quality,
        delay: stats.currentRoundTripTime,
        packetLoss: stats.packetLossRate,
      })

      if (statsHistory.value.length > 30) {
        statsHistory.value.shift()
      }
    })

    // 开始收集
    collector.start(updateInterval)
    isMonitoring.value = true
  }

  /**
   * 停止监测
   */
  const stopMonitoring = () => {
    if (collector) {
      collector.stop()
      collector = null
    }
    isMonitoring.value = false
  }

  /**
   * 重置统计数据
   */
  const resetStats = () => {
    connectionStats.value = {
      delay: 0,
      packetLoss: 0,
      jitter: 0,
      bitrate: 0,
      bytesReceived: 0,
      bytesSent: 0,
    }
    networkQuality.value = 'disconnected'
    statsHistory.value = []
  }

  /**
   * 获取平均统计数据
   * @param {number} durationMs - 统计时长（毫秒）
   * @returns {Object|null} 平均统计数据
   */
  const getAverageStats = (durationMs = 5000) => {
    if (!collector) return null
    return collector.getAverage(durationMs)
  }

  // 自动开始监测
  if (autoStart && peerConnection.value) {
    // 延迟启动，等待连接建立
    setTimeout(() => {
      if (peerConnection.value) {
        startMonitoring()
      }
    }, 1000)
  }

  // 清理
  onUnmounted(() => {
    stopMonitoring()
  })

  return {
    // 状态
    isMonitoring,
    networkQuality,
    connectionStats,
    statsHistory,

    // 计算属性
    qualityLevel,
    qualityColor,
    qualityIcon,
    isGoodNetwork,
    isPoorNetwork,
    delayStatus,

    // 方法
    startMonitoring,
    stopMonitoring,
    resetStats,
    getAverageStats,
  }
}

/**
 * 使用简化的网络质量指示器
 * 只返回质量等级，不保留历史数据
 * @param {Ref<RTCPeerConnection|null>} peerConnection - WebRTC 连接对象
 * @returns {Object} 网络质量状态
 */
export function useNetworkQuality(peerConnection) {
  const quality = ref('disconnected')
  const stats = ref({ delay: 0, packetLoss: 0 })
  let intervalId = null

  const updateQuality = async () => {
    if (!peerConnection.value) {
      quality.value = 'disconnected'
      return
    }

    try {
      const rtcStats = await peerConnection.value.getStats()
      let totalDelay = 0
      let totalPacketLoss = 0
      let count = 0

      rtcStats.forEach(report => {
        if (report.type === 'candidate-pair' && report.state === 'succeeded') {
          if (report.currentRoundTripTime) {
            totalDelay += report.currentRoundTripTime * 1000
            count++
          }
        }
        if (report.type === 'inbound-rtp') {
          if (report.packetsLost && report.packetsReceived) {
            const total = report.packetsLost + report.packetsReceived
            totalPacketLoss += (report.packetsLost / total) * 100
          }
        }
      })

      if (count > 0) {
        stats.value.delay = totalDelay / count
        stats.value.packetLoss = totalPacketLoss
      }

      quality.value = calculateNetworkQuality(stats.value)
    } catch (error) {
      console.error('获取网络质量失败:', error)
    }
  }

  const start = (intervalMs = 2000) => {
    updateQuality()
    intervalId = setInterval(updateQuality, intervalMs)
  }

  const stop = () => {
    if (intervalId) {
      clearInterval(intervalId)
      intervalId = null
    }
  }

  // 组件卸载时清理
  onUnmounted(() => {
    stop()
  })

  return {
    quality,
    stats,
    start,
    stop,
  }
}

export default useNetworkStats
