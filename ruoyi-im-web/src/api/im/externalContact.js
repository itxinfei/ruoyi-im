/**
 * 外部联系人相关 API
 */
import request from '../request'

// ==================== 联系人管理 ====================

/**
 * 创建外部联系人
 * @param {Object} data - 联系人数据
 * @param {string} data.name - 姓名
 * @param {string} data.company - 公司
 * @param {string} data.position - 职位
 * @param {string} data.mobile - 手机号
 * @param {string} data.email - 邮箱
 * @param {string} data.remark - 备注
 * @param {number} data.groupId - 分组ID
 * @returns {Promise}
 */
export function createExternalContact(data) {
  return request({
    url: '/api/im/external-contact',
    method: 'post',
    data
  })
}

/**
 * 更新外部联系人
 * @param {number} contactId - 联系人ID
 * @param {Object} data - 更新数据
 * @returns {Promise}
 */
export function updateExternalContact(contactId, data) {
  return request({
    url: `/api/im/external-contact/${contactId}`,
    method: 'put',
    data
  })
}

/**
 * 删除外部联系人
 * @param {number} contactId - 联系人ID
 * @returns {Promise}
 */
export function deleteExternalContact(contactId) {
  return request({
    url: `/api/im/external-contact/${contactId}`,
    method: 'delete'
  })
}

/**
 * 获取外部联系人详情
 * @param {number} contactId - 联系人ID
 * @returns {Promise}
 */
export function getExternalContact(contactId) {
  return request({
    url: `/api/im/external-contact/${contactId}`,
    method: 'get'
  })
}

/**
 * 获取外部联系人列表
 * @returns {Promise}
 */
export function getExternalContactList() {
  return request({
    url: '/api/im/external-contact/list',
    method: 'get'
  })
}

/**
 * 获取分组下的外部联系人列表
 * @param {number} groupId - 分组ID
 * @returns {Promise}
 */
export function getExternalContactsByGroup(groupId) {
  return request({
    url: `/api/im/external-contact/group/${groupId}`,
    method: 'get'
  })
}

/**
 * 获取星标外部联系人列表
 * @returns {Promise}
 */
export function getStarredExternalContacts() {
  return request({
    url: '/api/im/external-contact/starred',
    method: 'get'
  })
}

/**
 * 搜索外部联系人
 * @param {string} keyword - 搜索关键词
 * @returns {Promise}
 */
export function searchExternalContacts(keyword) {
  return request({
    url: '/api/im/external-contact/search',
    method: 'get',
    params: { keyword }
  })
}

/**
 * 切换外部联系人星标状态
 * @param {number} contactId - 联系人ID
 * @returns {Promise}
 */
export function toggleExternalContactStar(contactId) {
  return request({
    url: `/api/im/external-contact/${contactId}/star`,
    method: 'put'
  })
}

// ==================== 分组管理 ====================

/**
 * 创建外部联系人分组
 * @param {string} name - 分组名称
 * @returns {Promise}
 */
export function createExternalContactGroup(name) {
  return request({
    url: '/api/im/external-contact/group',
    method: 'post',
    params: { name }
  })
}

/**
 * 更新外部联系人分组
 * @param {number} groupId - 分组ID
 * @param {string} name - 分组名称
 * @returns {Promise}
 */
export function updateExternalContactGroup(groupId, name) {
  return request({
    url: `/api/im/external-contact/group/${groupId}`,
    method: 'put',
    params: { name }
  })
}

/**
 * 删除外部联系人分组
 * @param {number} groupId - 分组ID
 * @returns {Promise}
 */
export function deleteExternalContactGroup(groupId) {
  return request({
    url: `/api/im/external-contact/group/${groupId}`,
    method: 'delete'
  })
}

/**
 * 获取外部联系人分组列表
 * @returns {Promise}
 */
export function getExternalContactGroupList() {
  return request({
    url: '/api/im/external-contact/group/list',
    method: 'get'
  })
}
