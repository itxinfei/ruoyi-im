/**
 * WebRTC 统计工具
 * 用于获取和分析 WebRTC 连接质量指标
 * @author RuoYi-IM
 */

/**
 * 网络质量等级
 */
export const NETWORK_QUALITY = {
  EXCELLENT: 'excellent', // 优秀：4格
  GOOD: 'good',           // 良好：3格
  FAIR: 'fair',           // 一般：2格
  POOR: 'poor',           // 较差：1格
  DISCONNECTED: 'disconnected', // 断开：0格
}

/**
 * 网络质量等级对应的配置
 */
export const QUALITY_CONFIG = {
  [NETWORK_QUALITY.EXCELLENT]: {
    level: 4,
    color: '#52C41A',
    threshold: { delay: 50, packetLoss: 1, jitter: 30 },
    icon: '▂▄▆█',
  },
  [NETWORK_QUALITY.GOOD]: {
    level: 3,
    color: '#1890FF',
    threshold: { delay: 100, packetLoss: 3, jitter: 50 },
    icon: '▂▄▆_',
  },
  [NETWORK_QUALITY.FAIR]: {
    level: 2,
    color: '#FAAD14',
    threshold: { delay: 200, packetLoss: 10, jitter: 100 },
    icon: '▂▄__',
  },
  [NETWORK_QUALITY.POOR]: {
    level: 1,
    color: '#FF4D4F',
    threshold: { delay: 500, packetLoss: 20, jitter: 200 },
    icon: '▂___',
  },
  [NETWORK_QUALITY.DISCONNECTED]: {
    level: 0,
    color: '#8C8C8C',
    threshold: { delay: Infinity, packetLoss: 100, jitter: Infinity },
    icon: '✕___',
  },
}

/**
 * 解析 RTCStats 数据
 * @param {RTCStatsReport} stats - WebRTC 统计数据
 * @returns {Object} 解析后的统计数据
 */
export function parseRTCStats(stats) {
  const result = {
    // 连接状态
    connectionState: 'unknown',
    iceConnectionState: 'unknown',

    // 网络延迟
    currentRoundTripTime: 0,
    averageRoundTripTime: 0,

    // 丢包
    packetsSent: 0,
    packetsReceived: 0,
    packetsLost: 0,
    packetLossRate: 0,

    // 抖动
    jitter: 0,

    // 带宽
    bitrate: 0,
    bytesSent: 0,
    bytesReceived: 0,

    // 编解码
    codec: '',

    // 音频级别
    audioLevel: 0,

    // 视频分辨率
    frameWidth: 0,
    frameHeight: 0,
    framesPerSecond: 0,
  }

  stats.forEach(report => {
    switch (report.type) {
      case 'transport': {
        // 传输层统计
        if (report.bytesSent) result.bytesSent = Number(report.bytesSent)
        if (report.bytesReceived) result.bytesReceived = Number(report.bytesReceived)
        break
      }

      case 'candidate-pair': {
        // ICE 候选对统计
        if (report.state === 'succeeded' && report.currentRoundTripTime) {
          result.currentRoundTripTime = Number(report.currentRoundTripTime) * 1000 // 转换为毫秒
        }
        if (report.totalRoundTripTime) {
          result.averageRoundTripTime = Number(report.totalRoundTripTime) * 1000
        }
        break
      }

      case 'inbound-rtp': {
        // 入站 RTP 统计
        if (report.mediaType === 'video') {
          if (report.frameWidth) result.frameWidth = Number(report.frameWidth)
          if (report.frameHeight) result.frameHeight = Number(report.frameHeight)
          if (report.framesPerSecond) result.framesPerSecond = Number(report.framesPerSecond)
        }
        if (report.packetsReceived) {
          result.packetsReceived = Number(report.packetsReceived)
        }
        if (report.packetsLost) {
          result.packetsLost = Number(report.packetsLost)
        }
        if (report.jitter) {
          result.jitter = Number(report.jitter) * 1000 // 转换为毫秒
        }
        break
      }

      case 'outbound-rtp': {
        // 出站 RTP 统计
        if (report.packetsSent) {
          result.packetsSent = Number(report.packetsSent)
        }
        if (report.mediaType === 'audio' && report.codecId) {
          // 获取编解码信息
        }
        break
      }

      case 'track': {
        // 轨道统计
        if (report.audioLevel) {
          result.audioLevel = Number(report.audioLevel)
        }
        break
      }

      case 'codec': {
        // 编解码器统计
        if (report.mimeType) {
          result.codec = report.mimeType
        }
        break
      }
    }
  })

  // 计算丢包率
  const totalPackets = result.packetsSent + result.packetsReceived
  if (totalPackets > 0) {
    result.packetLossRate = (result.packetsLost / totalPackets) * 100
  }

  return result
}

/**
 * 计算网络质量等级
 * @param {Object} stats - 统计数据
 * @returns {string} 网络质量等级
 */
export function calculateNetworkQuality(stats) {
  const { currentRoundTripTime: delay, packetLossRate: packetLoss, jitter } = stats

  // 检查是否断开连接
  if (delay === 0 && packetLoss === 0 && jitter === 0) {
    return NETWORK_QUALITY.DISCONNECTED
  }

  // 按照最严格的指标判断
  if (delay <= QUALITY_CONFIG[NETWORK_QUALITY.EXCELLENT].threshold.delay &&
      packetLoss <= QUALITY_CONFIG[NETWORK_QUALITY.EXCELLENT].threshold.packetLoss &&
      jitter <= QUALITY_CONFIG[NETWORK_QUALITY.EXCELLENT].threshold.jitter) {
    return NETWORK_QUALITY.EXCELLENT
  }

  if (delay <= QUALITY_CONFIG[NETWORK_QUALITY.GOOD].threshold.delay &&
      packetLoss <= QUALITY_CONFIG[NETWORK_QUALITY.GOOD].threshold.packetLoss &&
      jitter <= QUALITY_CONFIG[NETWORK_QUALITY.GOOD].threshold.jitter) {
    return NETWORK_QUALITY.GOOD
  }

  if (delay <= QUALITY_CONFIG[NETWORK_QUALITY.FAIR].threshold.delay &&
      packetLoss <= QUALITY_CONFIG[NETWORK_QUALITY.FAIR].threshold.packetLoss &&
      jitter <= QUALITY_CONFIG[NETWORK_QUALITY.FAIR].threshold.jitter) {
    return NETWORK_QUALITY.FAIR
  }

  if (delay <= QUALITY_CONFIG[NETWORK_QUALITY.POOR].threshold.delay &&
      packetLoss <= QUALITY_CONFIG[NETWORK_QUALITY.POOR].threshold.packetLoss &&
      jitter <= QUALITY_CONFIG[NETWORK_QUALITY.POOR].threshold.jitter) {
    return NETWORK_QUALITY.POOR
  }

  return NETWORK_QUALITY.POOR // 超过阈值也归为较差
}

/**
 * 获取网络质量配置
 * @param {string} quality - 网络质量等级
 * @returns {Object} 质量配置
 */
export function getQualityConfig(quality) {
  return QUALITY_CONFIG[quality] || QUALITY_CONFIG[NETWORK_QUALITY.DISCONNECTED]
}

/**
 * 格式化延迟显示
 * @param {number} ms - 延迟毫秒数
 * @returns {string} 格式化后的字符串
 */
export function formatDelay(ms) {
  if (ms < 1000) {
    return `${Math.round(ms)}ms`
  }
  return `${(ms / 1000).toFixed(1)}s`
}

/**
 * 格式化丢包率显示
 * @param {number} rate - 丢包率百分比
 * @returns {string} 格式化后的字符串
 */
export function formatPacketLoss(rate) {
  return `${rate.toFixed(1)}%`
}

/**
 * 格式化带宽显示
 * @param {number} bps - 每秒比特数
 * @returns {string} 格式化后的字符串
 */
export function formatBitrate(bps) {
  if (bps < 1000) {
    return `${bps} bps`
  }
  if (bps < 1000000) {
    return `${(bps / 1000).toFixed(1)} Kbps`
  }
  return `${(bps / 1000000).toFixed(1)} Mbps`
}

/**
 * WebRTC 统计收集器类
 */
export class WebRTCStatsCollector {
  constructor(peerConnection) {
    this.peerConnection = peerConnection
    this.interval = null
    this.statsHistory = []
    this.maxHistoryLength = 60 // 保留最近60秒的数据
    this.listeners = new Set()
  }

  /**
   * 开始收集统计数据
   * @param {number} intervalMs - 收集间隔（毫秒）
   */
  async start(intervalMs = 1000) {
    if (this.interval) {
      this.stop()
    }

    // 立即收集一次
    await this.collect()

    // 定期收集
    this.interval = setInterval(async () => {
      await this.collect()
    }, intervalMs)
  }

  /**
   * 收集统计数据
   */
  async collect() {
    if (!this.peerConnection) {
      return
    }

    try {
      const stats = await this.peerConnection.getStats()
      const parsed = parseRTCStats(stats)
      const quality = calculateNetworkQuality(parsed)

      const statsWithQuality = {
        ...parsed,
        quality,
        timestamp: Date.now(),
      }

      // 添加到历史记录
      this.statsHistory.push(statsWithQuality)

      // 限制历史记录长度
      if (this.statsHistory.length > this.maxHistoryLength) {
        this.statsHistory.shift()
      }

      // 通知监听器
      this.notify(statsWithQuality)

      return statsWithQuality
    } catch (error) {
      console.error('收集 WebRTC 统计失败:', error)
      return null
    }
  }

  /**
   * 停止收集
   */
  stop() {
    if (this.interval) {
      clearInterval(this.interval)
      this.interval = null
    }
  }

  /**
   * 添加监听器
   * @param {Function} listener - 监听函数
   */
  addListener(listener) {
    this.listeners.add(listener)
  }

  /**
   * 移除监听器
   * @param {Function} listener - 监听函数
   */
  removeListener(listener) {
    this.listeners.delete(listener)
  }

  /**
   * 通知所有监听器
   * @param {Object} stats - 统计数据
   */
  notify(stats) {
    this.listeners.forEach(listener => {
      try {
        listener(stats)
      } catch (error) {
        console.error('统计监听器错误:', error)
      }
    })
  }

  /**
   * 获取最新统计数据
   * @returns {Object|null} 最新统计数据
   */
  getLatest() {
    return this.statsHistory[this.statsHistory.length - 1] || null
  }

  /**
   * 获取平均统计数据
   * @param {number} durationMs - 统计时长（毫秒）
   * @returns {Object} 平均统计数据
   */
  getAverage(durationMs = 5000) {
    const now = Date.now()
    const startTime = now - durationMs

    const relevantStats = this.statsHistory.filter(s => s.timestamp >= startTime)

    if (relevantStats.length === 0) {
      return null
    }

    // 计算平均值
    const sum = relevantStats.reduce((acc, s) => ({
      currentRoundTripTime: acc.currentRoundTripTime + s.currentRoundTripTime,
      packetLossRate: acc.packetLossRate + s.packetLossRate,
      jitter: acc.jitter + s.jitter,
    }), {
      currentRoundTripTime: 0,
      packetLossRate: 0,
      jitter: 0,
    })

    const count = relevantStats.length

    return {
      currentRoundTripTime: sum.currentRoundTripTime / count,
      packetLossRate: sum.packetLossRate / count,
      jitter: sum.jitter / count,
    }
  }

  /**
   * 清空历史记录
   */
  clear() {
    this.statsHistory = []
  }

  /**
   * 销毁收集器
   */
  destroy() {
    this.stop()
    this.listeners.clear()
    this.statsHistory = []
  }
}

export default {
  NETWORK_QUALITY,
  QUALITY_CONFIG,
  parseRTCStats,
  calculateNetworkQuality,
  getQualityConfig,
  formatDelay,
  formatPacketLoss,
  formatBitrate,
  WebRTCStatsCollector,
}
