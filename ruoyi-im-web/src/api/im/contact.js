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
    url: '/im/contact/list',
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
    url: '/im/contact/search',
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
    url: `/im/contact/${contactId}`,
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
    url: '/im/contact/request',
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
export function handleFriendRequest(data) {
  return request({
    url: '/im/contact/request/handle',
    method: 'put',
    data
  })
}

/**
 * 获取好友申请列表
 * @returns {Promise}
 */
export function getFriendRequests() {
  return request({
    url: '/im/contact/requests',
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
export function updateContactRemark(data) {
  return request({
    url: '/im/contact/remark',
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
    url: `/im/contact/${contactId}`,
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
    url: '/im/contact/move',
    method: 'put',
    data
  })
}
