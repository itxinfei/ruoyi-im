// 环境变量配置
export const envConfig = {
  // 应用配置
  title: 'RuoYi-IM',
  baseAPI: '/api',
  wsAPI: 'ws://localhost:9090/ws',

  // 功能开关
  useMock: true,
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
