/**
 * 统一消息发送 Composable
 *
 * 职责：
 * - 文本消息发送（乐观更新）
 * - 媒体消息发送（通用 sendMedia 模式：上传 → 发消息）
 * - 便捷方法：sendImage/sendFile/sendVideo/sendVoice/sendScreenshot/sendLocation
 * - 失败重试
 *
 * 所有消息操作通过 Vuex store mutations 更新，store 是唯一权威源。
 */

import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { uploadImage as uploadImageApi, uploadFile as uploadFileApi } from '@/api/im/file'
import { retryMessage as retryMessageApi } from '@/api/im/message'
import { useMessageTransformation } from '@/composables/useMessageTransformation.js'
import { useMessageRetry } from '@/composables/useMessageRetry.js'
import { MAX_RETRIES } from '@/constants/retry.js'

export function useChatSend(sessionId, currentUser) {
  const store = useStore()
  const { transformMsg } = useMessageTransformation({ currentUser })
  const { canRetry, recordRetryAttempt, removeRetryRecord } = useMessageRetry()

  const sending = ref(false)

  // 当前会话ID
  const currentSessionId = computed(() => sessionId?.value ?? sessionId)

  /**
   * 创建临时消息（乐观更新用）
   */
  const createTempMessage = ({ type, content, status = 'sending' }) => {
    const user = currentUser.value || currentUser
    const tempId = `temp-${type.toLowerCase()}-${Date.now()}`
    return {
      id: tempId,
      clientMsgId: tempId,
      type,
      content,
      senderId: user?.id,
      senderName: user?.nickName || user?.userName || '我',
      senderAvatar: user?.avatar,
      timestamp: Date.now(),
      isOwn: true,
      status,
      sendStatus: status === 'sending' ? 1 : 0,
      readCount: 0
    }
  }

  /**
   * 发送文本消息
   */
  const sendText = async (content, options = {}) => {
    if (!content || sending.value) { return }

    const sid = currentSessionId.value
    if (!sid) { return }

    // 检查发送队列是否已满
    const queueSize = store.getters['im/message/sendingQueueSize'] || 0
    if (queueSize >= 100) {
      ElMessage.warning({ message: '发送队列已满（100条），请稍后重试', duration: 3000 })
      return
    }

    sending.value = true

    // 乐观更新：先显示消息
    const tempMsg = createTempMessage({ type: 'TEXT', content })
    store.commit('im/message/ADD_MESSAGE', { sessionId: sid, message: tempMsg })
    store.commit('im/message/SET_REPLYING_MESSAGE', null)

    try {
      const msg = await store.dispatch('im/message/sendMessage', {
        sessionId: sid,
        type: 'TEXT',
        content,
        replyToMessageId: options.replyToMessageId || null
      })

      // 替换临时消息为真实消息
      const realMsg = transformMsg(msg)
      store.commit('im/message/REPLACE_TEMP_MESSAGE', {
        sessionId: sid,
        tempId: tempMsg.id,
        realMessage: { ...realMsg, status: null }
      })

      return realMsg
    } catch (error) {
      store.commit('im/message/MARK_MESSAGE_STATUS', {
        sessionId: sid,
        messageId: tempMsg.id,
        status: 'failed'
      })
      console.error('发送失败', error)
      ElMessage.error(error.message || '发送失败，请检查网络连接')
    } finally {
      sending.value = false
    }
  }

  /**
   * 通用媒体发送（核心方法）
   * 模式：创建临时消息 → 上传文件 → 发送消息 → 替换临时消息
   */
  const sendMedia = async ({ file, formData, type, uploadApi, buildContent, blobUrl, options = {} }) => {
    const sid = currentSessionId.value
    if (!sid) { return }

    const uploadSessionId = sid

    // 1. 创建临时消息并添加到 store
    const contentForTemp = buildContent ? buildContent(null) : {}
    const tempMsg = createTempMessage({
      type,
      content: blobUrl ? { [`${type.toLowerCase()}Url`]: blobUrl, ...(typeof contentForTemp === 'object' ? contentForTemp : {}) } : contentForTemp,
      status: 'uploading'
    })
    store.commit('im/message/ADD_MESSAGE', { sessionId: sid, message: tempMsg })

    try {
      // 2. 上传文件
      const uploadFormData = formData || new FormData()
      if (!formData && file) {
        uploadFormData.append('file', file)
      }

      const uploadRes = await uploadApi(uploadFormData)

      // 3. 竞态守卫：上传期间会话已切换
      if (currentSessionId.value !== uploadSessionId) {
        if (blobUrl) { URL.revokeObjectURL(blobUrl) }
        return
      }

      if (uploadRes.code !== 200) {
        throw new Error(uploadRes.msg || '上传失败')
      }

      // 4. 构建消息内容并发送
      const messageContent = buildContent(uploadRes.data)
      const msg = await store.dispatch('im/message/sendMessage', {
        sessionId: uploadSessionId,
        type,
        content: typeof messageContent === 'string' ? messageContent : JSON.stringify(messageContent),
        replyToMessageId: options.replyToMessageId || null
      })

      // 5. 替换临时消息
      const realMsg = transformMsg(msg)
      store.commit('im/message/REPLACE_TEMP_MESSAGE', {
        sessionId: uploadSessionId,
        tempId: tempMsg.id,
        realMessage: { ...realMsg, status: null }
      })

      // 释放 blobUrl
      if (blobUrl) { URL.revokeObjectURL(blobUrl) }

      return realMsg
    } catch (error) {
      // 标记失败
      store.commit('im/message/MARK_MESSAGE_STATUS', {
        sessionId: uploadSessionId,
        messageId: tempMsg.id,
        status: 'failed'
      })
      if (blobUrl) { URL.revokeObjectURL(blobUrl) }
      throw error
    }
  }

  /**
   * 发送图片消息
   */
  const sendImage = async (payload, options = {}) => {
    let file, formData
    if (payload instanceof FormData) {
      formData = payload
      file = payload.get('file')
    } else if (payload instanceof File) {
      file = payload
      formData = new FormData()
      formData.append('file', file)
    } else {
      // 事件对象
      file = payload?.target?.files?.[0]
      if (!file) { return }
      formData = new FormData()
      formData.append('file', file)
      if (payload?.target) { payload.target.value = '' }
    }

    const blobUrl = URL.createObjectURL(file)

    try {
      return await sendMedia({
        file, formData, type: 'IMAGE',
        uploadApi: uploadImageApi,
        blobUrl,
        buildContent: data => data
          ? { fileId: data.id, imageUrl: data.url }
          : { imageUrl: blobUrl },
        options
      })
    } catch (error) {
      ElMessage.error('图片发送失败')
      console.error('图片发送失败:', error)
    }
  }

  /**
   * 发送文件消息
   */
  const sendFile = async (payload, options = {}) => {
    let file, formData
    if (payload instanceof FormData) {
      formData = payload
      file = payload.get('file')
    } else if (payload instanceof File) {
      file = payload
      formData = new FormData()
      formData.append('file', file)
    } else {
      file = payload?.target?.files?.[0]
      if (!file) { return }
      formData = new FormData()
      formData.append('file', file)
      if (payload?.target) { payload.target.value = '' }
    }

    try {
      return await sendMedia({
        file, formData, type: 'FILE',
        uploadApi: uploadFileApi,
        buildContent: data => data
          ? { fileId: data.id, fileName: file.name, size: file.size, fileUrl: data.url }
          : { fileName: file.name, size: file.size, fileUrl: '' },
        options
      })
    } catch (error) {
      ElMessage.error('文件发送失败')
      console.error('文件发送失败:', error)
    }
  }

  /**
   * 发送视频消息
   */
  const sendVideo = async ({ file, url }, options = {}) => {
    const formData = new FormData()
    formData.append('file', file)

    try {
      return await sendMedia({
        file, formData, type: 'VIDEO',
        uploadApi: uploadFileApi,
        blobUrl: url,
        buildContent: data => data
          ? { fileId: data.id, videoUrl: data.url, fileName: file.name, size: file.size }
          : { videoUrl: url, fileName: file.name, size: file.size, duration: 0 },
        options
      })
    } catch (error) {
      ElMessage.error('视频发送失败')
      console.error('视频上传失败:', error)
    }
  }

  /**
   * 发送语音消息
   */
  const sendVoice = async ({ file, duration }, options = {}) => {
    const formData = new FormData()
    formData.append('file', file)

    try {
      return await sendMedia({
        file, formData, type: 'VOICE',
        uploadApi: uploadFileApi,
        buildContent: data => data
          ? { voiceUrl: data.fileUrl || data.url, duration }
          : { duration },
        options
      })
    } catch (error) {
      ElMessage.error('语音发送失败')
      console.error('语音发送失败:', error)
    }
  }

  /**
   * 发送截图消息（复用 sendImage）
   */
  const sendScreenshot = async (formData, options = {}) => {
    const file = formData instanceof FormData ? formData.get('file') : formData
    if (!file) { return }
    return sendImage(file, options)
  }

  /**
   * 发送位置消息（无需上传文件）
   */
  const sendLocation = async ({ latitude, longitude, address }, options = {}) => {
    const sid = currentSessionId.value
    if (!sid) { return }

    const tempMsg = createTempMessage({
      type: 'LOCATION',
      content: { latitude, longitude, address: address || '未知位置' },
      status: 'sending'
    })
    store.commit('im/message/ADD_MESSAGE', { sessionId: sid, message: tempMsg })

    try {
      const msg = await store.dispatch('im/message/sendMessage', {
        sessionId: sid,
        type: 'LOCATION',
        content: JSON.stringify({ latitude, longitude, address: address || '未知位置' }),
        replyToMessageId: options.replyToMessageId || null
      })

      const realMsg = transformMsg(msg)
      store.commit('im/message/REPLACE_TEMP_MESSAGE', {
        sessionId: sid,
        tempId: tempMsg.id,
        realMessage: { ...realMsg, status: null }
      })

      return realMsg
    } catch (error) {
      store.commit('im/message/MARK_MESSAGE_STATUS', {
        sessionId: sid,
        messageId: tempMsg.id,
        status: 'failed'
      })
      ElMessage.error('位置发送失败')
      console.error('位置发送失败:', error)
    }
  }

  /**
   * 重试失败的消息
   */
  const retryMessage = async msg => {
    const status = msg.sendStatus || msg.status
    if (status !== 4 && status !== 'FAILED' && status !== 'failed') { return }

    const clientMsgId = msg.clientMsgId || msg.id
    if (!clientMsgId) {
      ElMessage.error('无法重试：缺少消息标识')
      return
    }

    // 检查重试次数
    if (!canRetry(clientMsgId)) {
      ElMessage.warning(`重试次数已达上限（最多${MAX_RETRIES}次）`)
      return
    }

    const sid = currentSessionId.value

    // 记录重试次数
    recordRetryAttempt(clientMsgId)

    // 标记为发送中
    store.commit('im/message/MARK_MESSAGE_STATUS', {
      sessionId: sid,
      messageId: clientMsgId,
      status: 'sending'
    })

    try {
      const res = await retryMessageApi(clientMsgId)

      if (res.code === 200) {
        ElMessage.success({ message: '正在重试发送...', duration: 2000 })
        removeRetryRecord(clientMsgId)
      } else {
        throw new Error(res.msg || '重试失败')
      }
    } catch (error) {
      store.commit('im/message/MARK_MESSAGE_STATUS', {
        sessionId: sid,
        messageId: clientMsgId,
        status: 'failed'
      })
      ElMessage.error(error.message || '重试失败，请稍后重试')
      console.error('消息重试失败:', error)
    }
  }

  return {
    sending,
    sendText,
    sendImage,
    sendFile,
    sendVideo,
    sendVoice,
    sendScreenshot,
    sendLocation,
    sendMedia,
    retryMessage
  }
}
