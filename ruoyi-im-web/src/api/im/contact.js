/**
 * 联系人相关 API
 */
import request from '../request'

/**
 * 获取联系人列表
 * @param {Object} params - 查询参数
 * @param {string} params.groupName - 分组名称（可选）
 * @returns {Promise}
 */
export function getContacts(params) {
  return request({
    url: '/api/im/contact/list',
    method: 'get',
    params
  })
}

/**
 * 搜索联系人
 * @param {Object} params - 查询参数
 * @param {string} params.keyword - 搜索关键词
 * @returns {Promise}
 */
export function searchContacts(params) {
  return request({
    url: '/api/im/contact/search',
    method: 'get',
    params
  })
}

/**
 * 获取联系人详情
 * @param {number} contactId - 联系人ID
 * @returns {Promise}
 */
export function getContact(contactId) {
  return request({
    url: `/api/im/contact/${contactId}`,
    method: 'get'
  })
}

/**
 * 发送好友申请
 * @param {Object} data - 申请数据
 * @param {number} data.userId - 目标用户ID
 * @param {string} data.remark - 备注信息
 * @returns {Promise}
 */
export function sendFriendRequest(data) {
  return request({
    url: '/api/im/contact/request/send',
    method: 'post',
    data
  })
}

/**
 * 处理好友申请
 * @param {Object} data - 处理数据
 * @param {number} data.requestId - 申请ID
 * @param {boolean} data.accept - 是否接受
 * @param {string} data.remark - 备注信息（接受时填写）
 * @returns {Promise}
 */
export function handleFriendRequest(requestId, approved) {
  return request({
    url: `/api/im/contact/request/${requestId}/handle`,
    method: 'post',
    params: { approved }
  })
}

/**
 * 获取好友申请列表
 * @returns {Promise}
 */
export function getFriendRequests() {
  return request({
    url: '/api/im/contact/request/received',
    method: 'get'
  })
}

/**
 * 修改联系人备注
 * @param {Object} data - 备注数据
 * @param {number} data.contactId - 联系人ID
 * @param {string} data.remark - 备注名称
 * @returns {Promise}
 */
export function updateContactRemark(contactId, data) {
  return request({
    url: `/api/im/contact/${contactId}`,
    method: 'put',
    data
  })
}

/**
 * 删除联系人
 * @param {number} contactId - 联系人ID
 * @returns {Promise}
 */
export function deleteContact(contactId) {
  return request({
    url: `/api/im/contact/${contactId}`,
    method: 'delete'
  })
}

/**
 * 移动联系人到分组
 * @param {Object} data - 分组数据
 * @param {number} data.contactId - 联系人ID
 * @param {string} data.groupName - 分组名称
 * @returns {Promise}
 */
export function moveContactToGroup(data) {
  return request({
    url: '/api/im/contact/group/move',
    method: 'put',
    data
  })
}

/**
 * 获取分组好友列表
 * @returns {Promise}
 */
export function getGroupedFriendList() {
  return request({
    url: '/api/im/contact/grouped',
    method: 'get'
  })
}

/**
 * 拉黑/解除拉黑好友
 * @param {number} contactId - 联系人ID
 * @param {boolean} blocked - 是否拉黑
 * @returns {Promise}
 */
export function blockFriend(contactId, blocked) {
  return request({
    url: `/api/im/contact/${contactId}/block`,
    method: 'put',
    params: { blocked }
  })
}

/**
 * 获取好友分组列表
 * @returns {Promise}
 */
export function getGroupList() {
  return request({
    url: '/api/im/contact/group/list',
    method: 'get'
  })
}

/**
 * 重命名好友分组
 * @param {string} oldName - 旧分组名称
 * @param {string} newName - 新分组名称
 * @returns {Promise}
 */
export function renameGroup(oldName, newName) {
  return request({
    url: `/api/im/contact/group/${encodeURIComponent(oldName)}`,
    method: 'put',
    data: { newName }
  })
}

/**
 * 删除好友分组
 * @param {string} groupName - 分组名称
 * @returns {Promise}
 */
export function deleteGroup(groupName) {
  return request({
    url: `/api/im/contact/group/${encodeURIComponent(groupName)}`,
    method: 'delete'
  })
}

/**
 * 获取发送的好友申请列表
 * @returns {Promise}
 */
export function getSentRequests() {
  return request({
    url: '/api/im/contact/request/sent',
    method: 'get'
  })
}
