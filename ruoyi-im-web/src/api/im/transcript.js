/**
 * 语音转文字相关 API
 * 对应后端 ImVoiceTranscriptController
 */
import request from '../request'

/**
 * 创建语音转文字任务
 * @param {Object} data - 转写数据
 * @param {number} data.messageId - 消息ID
 * @param {string} data.voiceUrl - 语音文件URL
 * @param {number} data.duration - 语音时长（秒）
 * @param {string} data.language - 语言类型：zh-CN中文, en-US英文
 * @returns {Promise}
 */
export function createTranscript(data) {
  return request({
    url: '/api/im/voice/transcript/create',
    method: 'post',
    params: {
      messageId: data.messageId,
      voiceUrl: data.voiceUrl,
      duration: data.duration,
      language: data.language || 'zh-CN'
    }
  })
}

/**
 * 获取转写结果
 * @param {number} messageId - 消息ID
 * @returns {Promise}
 */
export function getTranscript(messageId) {
  return request({
    url: '/api/im/voice/transcript/result/' + messageId,
    method: 'get'
  })
}

/**
 * 重新转写
 * @param {number} messageId - 消息ID
 * @returns {Promise}
 */
export function reTranscribe(messageId) {
  return request({
    url: '/api/im/voice/transcript/retranscribe/' + messageId,
    method: 'post'
  })
}

/**
 * 获取用户转写列表
 * @returns {Promise}
 */
export function getUserTranscripts() {
  return request({
    url: '/api/im/voice/transcript/list',
    method: 'get'
  })
}

/**
 * 获取转写统计
 * @returns {Promise}
 */
export function getTranscriptStats() {
  return request({
    url: '/api/im/voice/transcript/stats',
    method: 'get'
  })
}

export default {
  createTranscript,
  getTranscript,
  reTranscribe,
  getUserTranscripts,
  getTranscriptStats
}
