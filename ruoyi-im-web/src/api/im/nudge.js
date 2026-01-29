/**
 * 拍一拍相关 API
 */
import request from '../request'

/**
 * 发送拍一拍
 * @param {Object} data - 拍一拍数据
 * @param {number} data.conversationId - 会话ID
 * @param {number} data.nudgedUserId - 被拍用户ID
 * @returns {Promise}
 */
export function sendNudge(data) {
  return request({
    url: '/im/nudge/send',
    method: 'post',
    data
  })
}

/**
 * 获取拍一拍配置
 * @returns {Promise}
 */
export function getNudgeConfig() {
  return request({
    url: '/im/nudge/config',
    method: 'get'
  })
}

/**
 * 更新拍一拍配置
 * @param {Object} data - 配置数据
 * @returns {Promise}
 */
export function updateNudgeConfig(data) {
  return request({
    url: '/im/nudge/config',
    method: 'put',
    data
  })
}
