/**
 * 消息转换 Composable
 *
 * 职责：
 * - 统一处理消息数据转换
 * - 处理 isOwn 判断逻辑
 * - 处理引用回复数据
 * - 时间戳标准化
 *
 * 使用方式：
 * ```js
 * const { transformMsg } = useMessageTransformation({ currentUser })
 * const normalizedMsg = transformMsg(rawMessage)
 * ```
 */
import { computed } from 'vue'

export function useMessageTransformation(options = {}) {
  const {
    currentUser = computed(() => ({})),
    existingMessages = []
  } = options

  /**
   * 判断消息是否为自己发送
   * @param {Object} message - 原始消息对象
   * @param {Object} currentUser - 当前用户对象
   * @returns {boolean}
   */
  const isOwnMessage = (message, user) => {
    // 优先使用后端返回的 isSelf 字段
    if (message.isSelf === true || message.isSelf === false) {
      return message.isSelf
    }

    // 降级：通过 senderId 判断
    const msgSenderId = String(message.senderId || '')
    const currentUserId = String(user?.id || '')

    return msgSenderId === currentUserId && msgSenderId !== ''
  }

  /**
   * 获取消息类型
   * @param {Object} message - 原始消息对象
   * @returns {string}
   */
  const getMessageType = message => {
    return (message.type || message.messageType || 'TEXT').toUpperCase()
  }

  /**
   * 处理引用回复数据
   * @param {Object} message - 原始消息对象
   * @param {Array} localMessages - 本地消息列表（用于查找引用）
   * @returns {Object|null}
   */
  const processReplyTo = (message, localMessages = []) => {
    // 优先使用后端返回的完整信息
    let replyTo = message.replyTo || message.quotedMessage

    // 降级：尝试在本地消息列表中查找
    if (!replyTo && message.replyToMessageId) {
      const quoted = localMessages.find(msg => msg.id === message.replyToMessageId)
      if (quoted) {
        replyTo = {
          id: quoted.id,
          senderName: quoted.senderName,
          content: quoted.content
        }
      }
    }

    return replyTo
  }

  /**
   * 标准化时间戳
   * @param {Object} message - 原始消息对象
   * @returns {number}
   */
  const normalizeTimestamp = message => {
    return message.sendTime || message.createTime || message.timestamp || Date.now()
  }

  /**
   * 转换单条消息
   * @param {Object} rawMessage - 原始消息对象
   * @param {Array} localMessages - 本地消息列表（用于查找引用）
   * @returns {Object} 转换后的消息对象
   */
  const transformMsg = (rawMessage, localMessages = []) => {
    const user = currentUser.value || currentUser

    // 规范化名称：兼容字段名差异
    const senderName = rawMessage.senderName ||
      rawMessage.senderNickname ||
      rawMessage.nickname ||
      rawMessage.userName ||
      '未知用户'

    // 规范化头像：兼容字段名差异
    const senderAvatar = rawMessage.senderAvatar || rawMessage.avatar || ''

    return {
      ...rawMessage,
      // 标准化字段
      type: getMessageType(rawMessage),
      isOwn: isOwnMessage(rawMessage, user),
      senderName,
      senderAvatar,
      replyTo: processReplyTo(rawMessage, localMessages),
      timestamp: normalizeTimestamp(rawMessage)
    }
  }

  /**
   * 批量转换消息列表
   * @param {Array} rawMessages - 原始消息数组
   * @param {Array} localMessages - 本地消息列表
   * @returns {Array} 转换后的消息数组
   */
  const transformMessageList = (rawMessages, localMessages = []) => {
    if (!Array.isArray(rawMessages)) {return []}
    return rawMessages.map(msg => transformMsg(msg, localMessages))
  }

  /**
   * 创建临时消息（用于乐观更新）
   * @param {Object} params - 消息参数
   * @returns {Object} 临时消息对象
   */
  const createTempMessage = (params = {}) => {
    const user = currentUser.value || currentUser

    const {
      type = 'TEXT',
      content = '',
      status = 'sending'
    } = params

    return {
      id: `temp-${Date.now()}`,
      type,
      content,
      senderId: user?.id,
      senderName: user?.nickName || user?.userName || '我',
      senderAvatar: user?.avatar,
      timestamp: Date.now(),
      isOwn: true,
      status,
      readCount: 0,
      ...params
    }
  }

  /**
   * 创建各类消息的临时对象
   */
  const tempMessageFactories = {
    // 文本消息
    text: content => createTempMessage({ type: 'TEXT', content }),

    // 图片消息
    image: imageUrl => createTempMessage({
      type: 'IMAGE',
      content: { imageUrl }
    }),

    // 文件消息
    file: (fileName, size, fileUrl = '') => createTempMessage({
      type: 'FILE',
      content: { fileName, size, fileUrl }
    }),

    // 语音消息
    voice: (duration, voiceUrl = '') => createTempMessage({
      type: 'VOICE',
      content: JSON.stringify({ duration, voiceUrl })
    }),

    // 视频消息
    video: (videoUrl, fileName, size) => createTempMessage({
      type: 'VIDEO',
      content: { videoUrl, fileName, size }
    }),

    // 位置消息
    location: (latitude, longitude, address) => createTempMessage({
      type: 'LOCATION',
      content: { latitude, longitude, address }
    })
  }

  return {
    // 核心方法
    transformMsg,
    transformMessageList,
    createTempMessage,

    // 工厂方法
    tempMessageFactories,

    // 工具方法（可单独使用）
    isOwnMessage,
    getMessageType,
    processReplyTo,
    normalizeTimestamp
  }
}
