/**
 * WebRTC 服务
 * 处理音视频通话的 PeerConnection、媒体流等
 */

import { ElMessage } from 'element-plus'

// STUN 服务器配置（用于NAT穿透）
const ICE_SERVERS = {
  iceServers: [
    { urls: 'stun:stun.l.google.com:19302' },
    { urls: 'stun:stun1.l.google.com:19302' },
    { urls: 'stun:stun2.l.google.com:19302' },
    { urls: 'stun:stun3.l.google.com:19302' },
    { urls: 'stun:stun4.l.google.com:19302' },
  ],
}

class WebRTCService {
  constructor() {
    this.peerConnection = null
    this.localStream = null
    this.remoteStream = null
    this.dataChannel = null
    this.isInitiator = false

    // 回调函数
    this.onIceCandidate = null
    this.onTrack = null
    this.onDataChannelMessage = null
    this.onDataChannelOpen = null
    this.onDataChannelClose = null
  }

  /**
   * 初始化 PeerConnection
   */
  initPeerConnection() {
    const configuration = {
      ...ICE_SERVERS,
    }

    this.peerConnection = new RTCPeerConnection(configuration)

    // 监听 ICE 候选
    this.peerConnection.onicecandidate = (event) => {
      if (event.candidate && this.onIceCandidate) {
        this.onIceCandidate(event.candidate)
      }
    }

    // 监听远程媒体流
    this.peerConnection.ontrack = (event) => {
      console.log('收到远程流:', event.streams[0])
      this.remoteStream = event.streams[0]
      if (this.onTrack) {
        this.onTrack(event.streams[0])
      }
    }

    // 监听连接状态变化
    this.peerConnection.onconnectionstatechange = () => {
      console.log('连接状态:', this.peerConnection.connectionState)
      if (this.peerConnection.connectionState === 'disconnected') {
        ElMessage.warning('对方已断开连接')
      } else if (this.peerConnection.connectionState === 'failed') {
        ElMessage.error('连接失败')
      }
    }

    // 监听 ICE 连接状态变化
    this.peerConnection.oniceconnectionstatechange = () => {
      console.log('ICE连接状态:', this.peerConnection.iceConnectionState)
    }

    return this.peerConnection
  }

  /**
   * 创建 Offer（发起方）
   */
  async createOffer(options = {}) {
    this.isInitiator = true
    const offer = await this.peerConnection.createOffer(options)
    await this.peerConnection.setLocalDescription(offer)
    return offer
  }

  /**
   * 创建 Answer（接收方）
   */
  async createAnswer(options = {}) {
    this.isInitiator = false
    const answer = await this.peerConnection.createAnswer(options)
    await this.peerConnection.setLocalDescription(answer)
    return answer
  }

  /**
   * 设置远程描述
   */
  async setRemoteDescription(description) {
    if (!this.peerConnection) {
      this.initPeerConnection()
    }
    await this.peerConnection.setRemoteDescription(
      new RTCSessionDescription(description)
    )
  }

  /**
   * 添加 ICE 候选
   */
  async addIceCandidate(candidate) {
    if (!this.peerConnection) {
      return
    }
    try {
      await this.peerConnection.addIceCandidate(
        new RTCIceCandidate(candidate)
      )
    } catch (e) {
      console.error('添加ICE候选失败:', e)
    }
  }

  /**
   * 获取本地媒体流
   */
  async getLocalStream(constraints = { audio: true, video: true }) {
    try {
      this.localStream = await navigator.mediaDevices.getUserMedia(constraints)
      return this.localStream
    } catch (error) {
      console.error('获取本地媒体失败:', error)
      throw new Error('无法访问摄像头或麦克风')
    }
  }

  /**
   * 添加本地流到 PeerConnection
   */
  addLocalStream(stream) {
    if (!this.peerConnection) {
      this.initPeerConnection()
    }

    stream.getTracks().forEach(track => {
      this.peerConnection.addTrack(track, stream)
    })
  }

  /**
   * 创建数据通道（用于通话中聊天）
   */
  createDataChannel(label = 'chat', options = {}) {
    if (!this.peerConnection) {
      this.initPeerConnection()
    }

    const defaultOptions = {
      ordered: false,
      maxRetransmits: 0,
      ...options,
    }

    this.dataChannel = this.peerConnection.createDataChannel(label, defaultOptions)

    this.dataChannel.onopen = () => {
      console.log('数据通道已打开')
      if (this.onDataChannelOpen) {
        this.onDataChannelOpen()
      }
    }

    this.dataChannel.onclose = () => {
      console.log('数据通道已关闭')
      if (this.onDataChannelClose) {
        this.onDataChannelClose()
      }
    }

    this.dataChannel.onmessage = (event) => {
      console.log('收到数据通道消息:', event.data)
      if (this.onDataChannelMessage) {
        this.onDataChannelMessage(event.data)
      }
    }

    return this.dataChannel
  }

  /**
   * 监听数据通道（接收方）
   */
  listenDataChannel() {
    if (!this.peerConnection) {
      return
    }

    this.peerConnection.ondatachannel = (event) => {
      this.dataChannel = event.channel

      this.dataChannel.onopen = () => {
        console.log('数据通道已打开')
        if (this.onDataChannelOpen) {
          this.onDataChannelOpen()
        }
      }

      this.dataChannel.onclose = () => {
        console.log('数据通道已关闭')
        if (this.onDataChannelClose) {
          this.onDataChannelClose()
        }
      }

      this.dataChannel.onmessage = (event) => {
        console.log('收到数据通道消息:', event.data)
        if (this.onDataChannelMessage) {
          this.onDataChannelMessage(event.data)
        }
      }
    }
  }

  /**
   * 通过数据通道发送消息
   */
  sendDataChannelMessage(data) {
    if (!this.dataChannel || this.dataChannel.readyState !== 'open') {
      console.warn('数据通道未打开')
      return false
    }

    try {
      this.dataChannel.send(typeof data === 'string' ? data : JSON.stringify(data))
      return true
    } catch (e) {
      console.error('发送数据通道消息失败:', e)
      return false
    }
  }

  /**
   * 切换音频轨道
   */
  toggleAudio(enabled) {
    if (this.localStream) {
      this.localStream.getAudioTracks().forEach(track => {
        track.enabled = enabled
      })
    }
  }

  /**
   * 切换视频轨道
   */
  toggleVideo(enabled) {
    if (this.localStream) {
      this.localStream.getVideoTracks().forEach(track => {
        track.enabled = enabled
      })
    }
  }

  /**
   * 替换视频轨道（用于切换摄像头）
   */
  async replaceVideoTrack(newStream) {
    if (!this.peerConnection || !this.localStream) {
      return
    }

    const videoTrack = newStream.getVideoTracks()[0]
    if (!videoTrack) {
      return
    }

    const sender = this.peerConnection.getSenders().find(s => {
      return s.track && s.track.kind === 'video'
    })

    if (sender) {
      await sender.replaceTrack(videoTrack)
    }

    // 停止旧的视频轨道
    const oldVideoTrack = this.localStream.getVideoTracks()[0]
    if (oldVideoTrack) {
      oldVideoTrack.stop()
    }

    this.localStream = newStream
  }

  /**
   * 关闭连接
   */
  close() {
    // 停止本地流
    if (this.localStream) {
      this.localStream.getTracks().forEach(track => track.stop())
      this.localStream = null
    }

    // 关闭数据通道
    if (this.dataChannel) {
      this.dataChannel.close()
      this.dataChannel = null
    }

    // 关闭 PeerConnection
    if (this.peerConnection) {
      this.peerConnection.close()
      this.peerConnection = null
    }

    this.remoteStream = null
    this.isInitiator = false
  }

  /**
   * 获取统计信息
   */
  async getStats() {
    if (!this.peerConnection) {
      return null
    }

    try {
      const stats = await this.peerConnection.getStats(null)
      return stats
    } catch (e) {
      console.error('获取统计信息失败:', e)
      return null
    }
  }

  /**
   * 创建模拟视频流（用于测试）
   * @param {Object} options - 选项
   * @param {number} options.duration - 测试时长（秒）
   * @param {boolean} options.withAudio - 是否包含音频
   * @returns {MediaStream} 模拟媒体流
   */
  createMockStream(options = {}) {
    const { duration = 30, withAudio = false } = options

    // 创建Canvas绘制模拟视频
    const canvas = document.createElement('canvas')
    canvas.width = 640
    canvas.height = 480
    const ctx = canvas.getContext('2d')

    let frame = 0
    const drawFrame = () => {
      // 绘制渐变背景
      const gradient = ctx.createLinearGradient(0, 0, canvas.width, canvas.height)
      gradient.addColorStop(0, '#0089FF')
      gradient.addColorStop(1, '#00A0FF')
      ctx.fillStyle = gradient
      ctx.fillRect(0, 0, canvas.width, canvas.height)

      // 绘制文字
      ctx.fillStyle = '#fff'
      ctx.font = 'bold 32px Arial'
      ctx.textAlign = 'center'
      ctx.textBaseline = 'middle'
      ctx.fillText('测试视频', canvas.width / 2, canvas.height / 2 - 20)

      ctx.font = '18px Arial'
      ctx.fillText(`帧: ${frame}`, canvas.width / 2, canvas.height / 2 + 20)

      frame++
    }

    // 启动动画
    const interval = setInterval(drawFrame, 33) // 30fps

    // 获取Canvas流
    const stream = canvas.captureStream(30)
    this.localStream = stream

    // 添加音频轨道（如果需要）
    if (withAudio) {
      const audioContext = new AudioContext()
      const oscillator = audioContext.createOscillator()
      const destination = audioContext.createMediaStreamDestination()
      oscillator.connect(destination)
      oscillator.start()
      stream.addTrack(destination.stream.getAudioTracks()[0])
    }

    // 自动停止
    setTimeout(() => {
      clearInterval(interval)
      stream.getTracks().forEach(track => track.stop())
    }, duration * 1000)

    return stream
  }

  /**
   * 检查浏览器支持
   * @returns {Object} 支持情况
   */
  checkBrowserSupport() {
    const support = {
      getUserMedia: !!navigator.mediaDevices?.getUserMedia,
      getDisplayMedia: !!navigator.mediaDevices?.getDisplayMedia,
      enumerateDevices: !!navigator.mediaDevices?.enumerateDevices,
      rtcPeerConnection: !!window.RTCPeerConnection,
      rtcDataChannel: !!window.RTCDataChannel
    }

    support.allSupported = Object.values(support).every(v => v)

    return support
  }
}

export default new WebRTCService()
