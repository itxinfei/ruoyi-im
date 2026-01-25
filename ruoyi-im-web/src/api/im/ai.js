/**
 * AI助手相关API
 */
import request from '../request'

/**
 * AI聊天对话
 * @param {Object} data 聊天请求
 * @param {string} data.content 用户发送的消息内容
 * @param {string} data.conversationId 会话ID（用于上下文记忆）
 * @param {number} data.userId 用户ID
 * @param {string} data.model AI模型（可选）
 * @param {number} data.maxTokens 最大token数（可选）
 * @param {number} data.temperature 温度参数（可选）
 */
export function chat(data) {
  return request({
    url: '/im/ai/chat',
    method: 'post',
    data
  })
}

/**
 * 生成文档摘要
 * @param {Object} data 摘要请求
 * @param {string} data.content 待摘要的文本内容
 * @param {string} data.summaryType 摘要类型：brief/normal/detailed
 * @param {string} data.language 摘要语言
 */
export function summarize(data) {
  return request({
    url: '/im/ai/summarize',
    method: 'post',
    data
  })
}

/**
 * 清除对话上下文
 * @param {string} conversationId 会话ID
 * @param {number} userId 用户ID
 */
export function clearConversation(conversationId, userId) {
  return request({
    url: `/im/ai/conversation/${conversationId}`,
    method: 'delete',
    params: { userId }
  })
}

/**
 * 获取支持的AI模型列表
 */
export function getSupportedModels() {
  return request({
    url: '/im/ai/models',
    method: 'get'
  })
}

// 默认导出
export default {
  chat,
  summarize,
  clearConversation,
  getSupportedModels
}
