// 环境变量配置
export const envConfig = {
  title: 'RuoYi-IM',
  baseAPI: 'http://localhost:8081',
  wsAPI: import.meta.env.VITE_WS_API || 'ws://localhost:8081/ws/im',

  // 功能开关
  useMock: false, // 关闭Mock，使用真实API
  showSettings: true,
  showTagsView: true,
  fixedHeader: true,
  showLogo: true,
  dynamicTitle: true,
  enableWebRTC: true,

  // WebRTC配置
  iceServers: [{ urls: 'stun:stun.l.google.com:19302' }],

  // 文件上传配置
  maxFileSize: 104857600, // 100MB
  allowedFileTypes: 'image/*,video/*,audio/*,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt',

  // 消息配置
  maxMessageLength: 1000,
  messagePageSize: 20,
  enableMessageEncryption: false,
}

export default envConfig
