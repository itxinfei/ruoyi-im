import envConfig from '../env.config.js'

export default {
  title: envConfig.title,

  /**
   * 是否系统布局配置
   */
  showSettings: envConfig.showSettings,

  /**
   * 是否显示 tagsView
   */
  tagsView: envConfig.showTagsView,

  /**
   * 是否固定头部
   */
  fixedHeader: envConfig.fixedHeader,

  /**
   * 是否显示logo
   */
  sidebarLogo: envConfig.showLogo,

  /**
   * 是否显示动态标题
   */
  dynamicTitle: envConfig.dynamicTitle,

  /**
   * WebSocket配置
   */
  websocket: {
    url: envConfig.wsAPI,
    options: {
      reconnectionAttempts: 5,
      reconnectionDelay: 3000,
      autoConnect: false,
    },
  },

  /**
   * WebRTC配置
   */
  webrtc: {
    enabled: envConfig.enableWebRTC,
    iceServers: envConfig.iceServers,
  },

  /**
   * 文件上传配置
   */
  upload: {
    maxSize: envConfig.maxFileSize,
    allowedTypes: envConfig.allowedFileTypes,
  },

  /**
   * 消息配置
   */
  message: {
    maxLength: envConfig.maxMessageLength,
    pageSize: envConfig.messagePageSize,
    enableEncryption: envConfig.enableMessageEncryption,
  },
}
