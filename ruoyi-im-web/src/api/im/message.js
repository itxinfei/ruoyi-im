/**
 * 消息相关 API
 */
import request from '../request'

/**
 * 发送消息
 * @param {Object} data - 消息数据
 * @param {number} data.conversationId - 会话ID
 * @param {string} data.type - 消息类型 TEXT/IMAGE/FILE/VOICE/VIDEO
 * @param {string} data.content - 消息内容
 * @param {number} data.replyToMessageId - 回复的消息ID（可选）
 * @param {string} data.clientMsgId - 客户端消息ID（可选，用于去重）
 * @returns {Promise}
 */
export function sendMessage(data) {
  return request({
    url: '/api/im/messages',
    method: 'post',
    data
  })
}

/**
 * 重试发送失败的消息
 * 支持自动重试（最多3次，采用指数退避策略：1s, 2s, 4s）
 * @param {string} clientMsgId - 客户端消息ID
 * @returns {Promise}
 */
export function retryMessage(clientMsgId) {
  return request({
    url: `/api/im/messages/retry/${clientMsgId}`,
    method: 'post'
  })
}

/**
 * 获取会话消息列表
 * @param {Object} params - 查询参数
 * @param {number} params.conversationId - 会话ID
 * @param {number} params.lastMessageId - 上一条消息ID（用于分页）
 * @param {number} params.pageSize - 每页数量（默认20）
 * @returns {Promise}
 */
export function getMessages(conversationId, params = {}) {
  return request({
    url: `/api/im/messages/conversations/${conversationId}`,
    method: 'get',
    params
  })
}

/**
 * 撤回消息
 * @param {number} messageId - 消息ID
 * @returns {Promise}
 */
export function recallMessage(messageId) {
  return request({
    url: `/api/im/messages/${messageId}/recall`,
    method: 'post'
  })
}

/**
 * 编辑消息
 * @param {Object} data - 编辑数据
 * @param {number} data.messageId - 消息ID
 * @param {string} data.content - 新内容
 * @returns {Promise}
 */
export function editMessage(messageId, data) {
  return request({
    url: `/api/im/messages/${messageId}/edit`,
    method: 'put',
    data
  })
}

/**
 * 删除消息
 * @param {number} messageId - 消息ID
 * @returns {Promise}
 */
export function deleteMessage(messageId) {
  return request({
    url: `/api/im/messages/${messageId}`,
    method: 'delete'
  })
}

/**
 * 转发消息
 * @param {Object} data - 转发数据
 * @param {number} data.messageId - 原消息ID
 * @param {number} data.targetConversationId - 目标会话ID
 * @returns {Promise}
 */
export function forwardMessage(data) {
  return request({
    url: `/api/im/messages/${data.messageId}/forward`,
    method: 'post',
    data
  })
}

/**
 * 批量转发消息
 * @param {Object} data - 批量转发数据
 * @param {Array<number>} data.messageIds - 原消息ID列表
 * @param {number} data.toConversationId - 目标会话ID
 * @param {string} data.forwardType - 转发类型：batch=逐条转发, combine=合并转发
 * @param {string} data.content - 附加说明
 * @returns {Promise}
 */
export function batchForwardMessages(data) {
  return request({
    url: '/api/im/messages/forward/batch',
    method: 'post',
    data
  })
}

/**
 * 搜索消息
 * @param {Object} params - 查询参数
 * @param {number} params.conversationId - 会话ID
 * @param {string} params.keyword - 搜索关键词
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @returns {Promise}
 */
export function searchMessages(data) {
  return request({
    url: '/api/im/messages/search',
    method: 'post',
    data
  })
}

/**
 * 标记消息已读
 * @param {Object} data - 已读数据
 * @param {number} data.conversationId - 会话ID
 * @param {number} data.messageId - 消息ID
 * @returns {Promise}
 */
export function markAsRead(data) {
  return request({
    url: '/api/im/messages/mark-read',
    method: 'put',
    data
  })
}

/**
 * 获取消息已读用户列表
 * @param {number} conversationId - 会话ID
 * @param {number} messageId - 消息ID
 * @returns {Promise}
 */
export function getMessageReadUsers(conversationId, messageId) {
  return request({
    url: `/api/im/messages/receipts/${conversationId}/${messageId}`,
    method: 'get'
  })
}

/**
 * 批量获取消息已读用户列表
 * @param {Array<number>} messageIds - 消息ID列表
 * @returns {Promise} 返回 Map<messageId, 已读用户ID列表>
 */
export function getBatchMessageReadUsers(messageIds) {
  return request({
    url: '/api/im/messages/read-status/batch/users',
    method: 'post',
    data: { messageIds }
  })
}

/**
 * 回复消息
 * @param {Object} data - 回复数据
 * @param {number} data.messageId - 原消息ID
 * @param {string} data.content - 回复内容
 * @returns {Promise}
 */
export function replyMessage(data) {
  return request({
    url: `/api/im/messages/${data.messageId}/reply`,
    method: 'post',
    data
  })
}

/**
 * 添加消息表情反应
 * @param {number} messageId - 消息ID
 * @param {Object} data - 反应数据
 * @param {string} data.emoji - 表情符号
 * @returns {Promise}
 */
export function addReaction(messageId, data) {
  return request({
    url: `/api/im/messages/${messageId}/reactions`,
    method: 'post',
    data
  })
}

/**
 * 删除消息表情反应
 * @param {number} messageId - 消息ID
 * @returns {Promise}
 */
export function removeReaction(messageId, emoji) {
  return request({
    url: `/api/im/messages/${messageId}/reactions`,
    method: 'delete',
    params: { emoji }
  })
}

/**
 * 获取消息的表情反应列表
 * @param {number} messageId - 消息ID
 * @returns {Promise}
 */
export function getMessageReactions(messageId) {
  return request({
    url: `/api/im/messages/${messageId}/reactions`,
    method: 'get'
  })
}

/**
 * 获取未读@提及列表
 * @returns {Promise}
 */
export function getUnreadMentions() {
  return request({
    url: '/api/im/mentions/unread',
    method: 'get'
  })
}

/**
 * 解析链接预览
 * @param {string} url - 链接地址
 * @returns {Promise} 解析结果
 */
export function parseLinkPreview(url) {
  return request({
    url: '/api/im/link-previews',
    method: 'post',
    data: { url }
  })
}

/**
 * 获取未读@提及数量
 * @returns {Promise}
 */
export function getUnreadMentionCount() {
  return request({
    url: '/api/im/mentions/unread/count',
    method: 'get'
  })
}

/**
 * 标记@提及为已读
 * @param {number} messageId - 消息ID
 * @returns {Promise}
 */
export function markMentionAsRead(messageId) {
  return request({
    url: `/api/im/mentions/${messageId}/read`,
    method: 'put'
  })
}

/**
 * 获取会话未读消息数
 * @param {number} conversationId - 会话ID
 * @returns {Promise}
 */
export function getUnreadCount(conversationId) {
  return request({
    url: '/api/im/messages/unread-count',
    method: 'get',
    params: { conversationId }
  })
}

/**
 * 置顶消息
 * @param {number} messageId - 消息ID
 * @returns {Promise}
 */
export function pinMessage(messageId) {
  return request({
    url: `/api/im/messages/${messageId}/pin`,
    method: 'post'
  })
}

/**
 * 取消置顶消息
 * @param {number} messageId - 消息ID
 * @returns {Promise}
 */
export function unpinMessage(messageId) {
  return request({
    url: `/api/im/messages/${messageId}/pin`,
    method: 'delete'
  })
}

/**
 * 获取会话的置顶消息列表
 * @param {number} conversationId - 会话ID
 * @returns {Promise}
 */
export function getPinnedMessages(conversationId) {
  return request({
    url: '/api/im/messages/pinned',
    method: 'get',
    params: { conversationId }
  })
}

/**
 * 清空会话聊天记录
 * @param {number} conversationId - 会话ID
 * @returns {Promise}
 */
export function clearConversationMessages(conversationId) {
  return request({
    url: `/api/im/messages/conversations/${conversationId}`,
    method: 'delete'
  })
}

/**
 * 导出会话聊天记录
 * @param {number} conversationId - 会话ID
 * @param {Object} options - 导出选项
 * @param {string} options.format - 导出格式 txt/html/pdf
 * @param {string} options.startTime - 开始时间（可选）
 * @param {string} options.endTime - 结束时间（可选）
 * @returns {Promise}
 */
export function exportChatMessages(conversationId, options) {
  return request({
    url: '/api/im/messages/export',
    method: 'post',
    params: { conversationId },
    data: options,
    responseType: 'blob'
  })
}

/**
 * 获取可导出的消息列表
 * @param {number} conversationId - 会话ID
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getExportableMessages(conversationId, params) {
  return request({
    url: '/api/im/messages/export/list',
    method: 'get',
    params: { ...params, conversationId }
  })
}

/**
 * 按类型获取会话消息
 * 用于聊天记录面板按类型筛选消息
 * @param {number} conversationId - 会话ID
 * @param {string} category - 消息类型分类（all/image/file/link/voice/video）
 * @param {Object} params - 查询参数
 * @param {number} params.lastId - 上一条消息ID（用于分页）
 * @param {number} params.limit - 每页数量
 * @returns {Promise}
 */
export function getMessagesByCategory(conversationId, category, params = {}) {
  return request({
    url: `/api/im/messages/conversations/${conversationId}/category/${category}`,
    method: 'get',
    params
  })
}

// ==================== 定时消息 ====================

/**
 * 创建定时消息
 * @param {Object} data - 定时消息数据
 * @param {number} data.conversationId - 会话ID
 * @param {string} data.messageType - 消息类型 TEXT/IMAGE/FILE/VOICE/VIDEO
 * @param {string} data.content - 消息内容
 * @param {string} data.scheduledTime - 定时发送时间 yyyy-MM-dd HH:mm
 * @param {number} data.replyToMessageId - 回复的消息ID（可选）
 * @returns {Promise}
 */
export function scheduleMessage(data) {
  return request({
    url: '/api/im/scheduled-messages',
    method: 'post',
    data
  })
}

/**
 * 获取定时消息列表
 * @param {Object} params - 查询参数
 * @param {number} params.conversationId - 会话ID（可选）
 * @param {number} params.status - 状态筛选（可选）: pending=待发送, sent=已发送, cancelled=已取消
 * @returns {Promise}
 */
export function getScheduledMessages(params = {}) {
  return request({
    url: '/api/im/scheduled-messages',
    method: 'get',
    params
  })
}

/**
 * 取消定时消息
 * @param {number} scheduledMessageId - 定时消息ID
 * @returns {Promise}
 */
export function cancelScheduledMessage(scheduledMessageId) {
  return request({
    url: `/api/im/scheduled-messages/${scheduledMessageId}/cancel`,
    method: 'put'
  })
}

/**
 * 修改定时消息发送时间
 * @param {number} scheduledMessageId - 定时消息ID
 * @param {string} scheduledTime - 新的定时发送时间
 * @returns {Promise}
 */
export function rescheduleMessage(scheduledMessageId, scheduledTime) {
  return request({
    url: `/api/im/scheduled-messages/${scheduledMessageId}/reschedule`,
    method: 'put',
    data: { scheduledTime }
  })
}

// ==================== 共享文件 ====================

/**
 * 获取与指定联系人的共享文件列表
 * @param {number} contactId - 联系人ID
 * @param {Object} params - 查询参数
 * @param {string} params.category - 文件分类: all/image/document/video/other
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @returns {Promise}
 */
export function getSharedFiles(contactId, params = {}) {
  return request({
    url: '/api/im/messages/shared-files',
    method: 'get',
    params: { ...params, contactId }
  })
}

/**
 * 获取群组共享文件列表
 * @param {number} groupId - 群组ID
 * @param {Object} params - 查询参数
 * @param {string} params.category - 文件分类
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @returns {Promise}
 */
export function getGroupSharedFiles(groupId, params = {}) {
  return request({
    url: `/api/im/groups/${groupId}/files/shared`,
    method: 'get',
    params
  })
}

// ==================== 消息同步 ====================

/**
 * 同步消息
 * 用于断线重连后获取遗漏的消息
 * @param {Object} params - 同步参数
 * @param {number} params.lastSyncTime - 上次同步时间戳（毫秒），可选
 * @param {string} params.deviceId - 设备ID（如：web_abc123）
 * @param {number} params.limit - 单次同步最大条数，默认100，最大500
 * @returns {Promise} 同步响应
 * @returns {Array} messages - 消息列表
 * @returns {boolean} hasMore - 是否还有更多消息
 * @returns {number} newSyncTime - 新的同步时间戳
 * @returns {number} lastMessageId - 最后一条消息ID
 * @returns {number} totalCount - 本次同步的消息数量
 */
export function syncMessages(params) {
  return request({
    url: '/api/im/messages/sync',
    method: 'get',
    params
  })
}

/**
 * 获取用户的所有设备同步点
 * @returns {Promise} 同步点列表
 */
export function getSyncPoints() {
  return request({
    url: '/api/im/messages/sync/points',
    method: 'get'
  })
}

/**
 * 重置设备的同步点
 * 删除后下次同步将从头开始
 * @param {string} deviceId - 设备ID
 * @returns {Promise}
 */
export function resetSyncPoint(deviceId) {
  return request({
    url: `/api/im/messages/sync/points/${deviceId}`,
    method: 'delete'
  })
}
