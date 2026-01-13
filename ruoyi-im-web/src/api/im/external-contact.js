/**
 * 外部联系人 API
 */
import request from '@/utils/request'

const BASE_URL = '/api/im/external-contact'

/**
 * 创建外部联系人
 * @param {Object} data - 联系人数据
 */
export function createExternalContact(data) {
  return request({
    url: `${BASE_URL}`,
    method: 'post',
    data,
  })
}

/**
 * 更新外部联系人
 * @param {number} contactId - 联系人ID
 * @param {Object} data - 联系人数据
 */
export function updateExternalContact(contactId, data) {
  return request({
    url: `${BASE_URL}/${contactId}`,
    method: 'put',
    data,
  })
}

/**
 * 删除外部联系人
 * @param {number} contactId - 联系人ID
 */
export function deleteExternalContact(contactId) {
  return request({
    url: `${BASE_URL}/${contactId}`,
    method: 'delete',
  })
}

/**
 * 获取外部联系人详情
 * @param {number} contactId - 联系人ID
 */
export function getExternalContact(contactId) {
  return request({
    url: `${BASE_URL}/${contactId}`,
    method: 'get',
  })
}

/**
 * 获取外部联系人列表
 */
export function getExternalContactList(params) {
  return request({
    url: `${BASE_URL}/list`,
    method: 'get',
    params,
  })
}

/**
 * 获取分组下的联系人列表
 * @param {number} groupId - 分组ID
 */
export function getContactsByGroup(groupId) {
  return request({
    url: `${BASE_URL}/group/${groupId}`,
    method: 'get',
  })
}

/**
 * 获取星标联系人列表
 */
export function getStarredContacts() {
  return request({
    url: `${BASE_URL}/starred`,
    method: 'get',
  })
}

/**
 * 搜索外部联系人
 * @param {string} keyword - 搜索关键词
 */
export function searchExternalContacts(keyword) {
  return request({
    url: `${BASE_URL}/search`,
    method: 'get',
    params: { keyword },
  })
}

/**
 * 切换星标状态
 * @param {number} contactId - 联系人ID
 */
export function toggleStarContact(contactId) {
  return request({
    url: `${BASE_URL}/${contactId}/star`,
    method: 'put',
  })
}

/**
 * 创建分组
 * @param {Object} data - 分组数据
 */
export function createContactGroup(data) {
  return request({
    url: `${BASE_URL}/group`,
    method: 'post',
    data,
  })
}

/**
 * 更新分组
 * @param {number} groupId - 分组ID
 * @param {Object} data - 分组数据
 */
export function updateContactGroup(groupId, data) {
  return request({
    url: `${BASE_URL}/group/${groupId}`,
    method: 'put',
    data,
  })
}

/**
 * 删除分组
 * @param {number} groupId - 分组ID
 */
export function deleteContactGroup(groupId) {
  return request({
    url: `${BASE_URL}/group/${groupId}`,
    method: 'delete',
  })
}

/**
 * 获取分组列表
 */
export function getContactGroups() {
  return request({
    url: `${BASE_URL}/group/list`,
    method: 'get',
  })
}
