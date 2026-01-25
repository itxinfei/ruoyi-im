/**
 * 群机器人相关 API
 */
import request from '../request'

/**
 * 创建群机器人
 * @param {Object} data - 机器人数据
 * @param {number} data.groupId - 群组ID
 * @param {string} data.botName - 机器人名称
 * @param {string} data.botType - 机器人类型：SERVICE客服/NOTIFY通知/MANAGE管理
 * @param {string} data.avatar - 机器人头像
 * @param {string} data.description - 机器人描述
 * @param {Array<Object>} data.rules - 机器人规则列表
 * @returns {Promise}
 */
export function createBot(data) {
  return request({
    url: '/api/im/group/bot/create',
    method: 'post',
    data
  })
}

/**
 * 更新群机器人
 * @param {Object} data - 更新数据
 * @param {number} data.botId - 机器人ID
 * @param {string} data.botName - 机器人名称
 * @param {string} data.avatar - 机器人头像
 * @param {string} data.description - 机器人描述
 * @param {number} data.isEnabled - 是否启用
 * @returns {Promise}
 */
export function updateBot(data) {
  return request({
    url: '/api/im/group/bot/update',
    method: 'put',
    data
  })
}

/**
 * 删除群机器人
 * @param {number} botId - 机器人ID
 * @returns {Promise}
 */
export function deleteBot(botId) {
  return request({
    url: `/api/im/group/bot/${botId}`,
    method: 'delete'
  })
}

/**
 * 获取群组机器人列表
 * @param {number} groupId - 群组ID
 * @returns {Promise}
 */
export function getGroupBots(groupId) {
  return request({
    url: `/api/im/group/bot/list/${groupId}`,
    method: 'get'
  })
}

/**
 * 获取机器人详情
 * @param {number} botId - 机器人ID
 * @returns {Promise}
 */
export function getBotDetail(botId) {
  return request({
    url: `/api/im/group/bot/${botId}`,
    method: 'get'
  })
}

/**
 * 添加机器人规则
 * @param {number} botId - 机器人ID
 * @param {Object} data - 规则数据
 * @param {string} data.ruleName - 规则名称
 * @param {string} data.triggerType - 触发类型：KEYWORD关键词/TIME定时/COMMAND命令
 * @param {string} data.triggerContent - 触发内容
 * @param {string} data.replyType - 回复类型：TEXT文本/IMAGE图片/CARD卡片
 * @param {string} data.replyContent - 回复内容
 * @param {number} data.priority - 优先级
 * @param {string} data.matchMode - 匹配模式：EXACT精确/CONTAINS包含/REGEX正则
 * @returns {Promise}
 */
export function addBotRule(botId, data) {
  return request({
    url: `/api/im/group/bot/${botId}/rule`,
    method: 'post',
    data
  })
}

/**
 * 更新机器人规则
 * @param {number} ruleId - 规则ID
 * @param {Object} data - 规则数据
 * @returns {Promise}
 */
export function updateBotRule(ruleId, data) {
  return request({
    url: `/api/im/group/bot/rule/${ruleId}`,
    method: 'put',
    data
  })
}

/**
 * 删除机器人规则
 * @param {number} ruleId - 规则ID
 * @returns {Promise}
 */
export function deleteBotRule(ruleId) {
  return request({
    url: `/api/im/group/bot/rule/${ruleId}`,
    method: 'delete'
  })
}

/**
 * 设置机器人启用状态
 * @param {number} botId - 机器人ID
 * @param {boolean} enabled - 是否启用
 * @returns {Promise}
 */
export function setBotEnabled(botId, enabled) {
  return request({
    url: `/api/im/group/bot/${botId}/enabled`,
    method: 'put',
    params: { enabled }
  })
}
