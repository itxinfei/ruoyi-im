/**
 * 联系人模块API
 * @module api/im/contact
 */
import request from '@/utils/request'

/**
 * 获取联系人列表
 * @param {Object} [params] - 查询参数
 * @param {number} [params.pageSize=20] - 每页数量
 * @param {number} [params.pageNum=1] - 当前页码
 * @param {string} [params.keyword] - 搜索关键词
 * @param {string} [params.groupId] - 分组ID
 * @returns {Promise}
 */
export function listContact(params) {
  return request({
    url: '/api/im/contact/list',
    method: 'get',
    params: {
      pageSize: params?.pageSize || 20,
      pageNum: params?.pageNum || 1,
      keyword: params?.keyword,
      groupId: params?.groupId,
    },
  })
}

/**
 * 获取联系人详情
 * @param {string} friendId - 好友关系ID
 * @returns {Promise}
 */
export function getContact(friendId) {
  return request({
    url: `/api/im/contact/${friendId}`,
    method: 'get',
  })
}

/**
 * 添加联系人（发送好友申请）
 * @param {Object} data - 申请数据
 * @param {string} data.userId - 目标用户ID
 * @param {string} [data.reason] - 申请理由
 * @returns {Promise}
 */
export function addContact(data) {
  return request({
    url: '/api/im/contact/request/send',
    method: 'post',
    data,
  })
}

/**
 * 删除联系人
 * @param {string} friendId - 好友关系ID
 * @returns {Promise}
 */
export function deleteContact(friendId) {
  return request({
    url: `/api/im/contact/${friendId}`,
    method: 'delete',
  })
}

/**
 * 更新联系人备注
 * @param {string} friendId - 好友关系ID
 * @param {Object} data - 更新数据
 * @param {string} data.remark - 备注名称
 * @returns {Promise}
 */
export function updateContactRemark(friendId, data) {
  return request({
    url: `/api/im/contact/${friendId}`,
    method: 'put',
    data,
  })
}

/**
 * 搜索联系人
 * @param {string} keyword - 搜索关键词
 * @returns {Promise}
 */
export function searchContacts(keyword) {
  return request({
    url: '/api/im/contact/search',
    method: 'get',
    params: { keyword },
  })
}

/**
 * 获取收到的好友申请列表
 * @returns {Promise}
 */
export function getReceivedFriendRequests() {
  return request({
    url: '/api/im/contact/request/received',
    method: 'get',
  })
}

/**
 * 获取发送的好友申请列表
 * @returns {Promise}
 */
export function getSentFriendRequests() {
  return request({
    url: '/api/im/contact/request/sent',
    method: 'get',
  })
}

/**
 * 处理好友申请
 * @param {string} requestId - 申请ID
 * @param {boolean} approved - 是否同意
 * @returns {Promise}
 */
export function handleFriendRequest(requestId, approved) {
  return request({
    url: `/api/im/contact/request/${requestId}/handle`,
    method: 'post',
    params: { approved },
  })
}

/**
 * 获取好友分组列表
 * @returns {Promise}
 */
export function getFriendGroups() {
  return request({
    url: '/api/im/contact/grouped',
    method: 'get',
  })
}

/**
 * 创建好友分组
 * @param {Object} data - 分组数据
 * @param {string} data.name - 分组名称
 * @returns {Promise}
 */
export function createFriendGroup(data) {
  return request({
    url: '/api/im/contact/group',
    method: 'post',
    data,
  })
}

/**
 * 更新好友分组
 * @param {string} groupId - 分组ID
 * @param {Object} data - 更新数据
 * @returns {Promise}
 */
export function updateFriendGroup(groupId, data) {
  return request({
    url: `/api/im/contact/group/${groupId}`,
    method: 'put',
    data,
  })
}

/**
 * 删除好友分组
 * @param {string} groupId - 分组ID
 * @returns {Promise}
 */
export function deleteFriendGroup(groupId) {
  return request({
    url: `/api/im/contact/group/${groupId}`,
    method: 'delete',
  })
}

/**
 * 移动好友到分组
 * @param {Object} data - 移动数据
 * @param {string[]} data.friendIds - 好友关系ID数组
 * @param {string} data.groupId - 目标分组ID
 * @returns {Promise}
 */
export function moveFriendToGroup(data) {
  return request({
    url: '/api/im/contact/group/move',
    method: 'put',
    data,
  })
}

/**
 * 获取联系人在线状态
 * @param {number[]} userIds - 用户ID数组
 * @returns {Promise}
 */
export function getContactStatus(userIds) {
  return request({
    url: '/api/im/user/batch',
    method: 'get',
    params: { ids: userIds.join(',') },
  })
}

// ========== 别名 - 向后兼容 ==========

export const getContactInfo = getContact

// 默认导出 - 方便批量引入
export default {
  listContact,
  getContact,
  addContact,
  deleteContact,
  updateContactRemark,
  searchContacts,
  getReceivedFriendRequests,
  getSentFriendRequests,
  handleFriendRequest,
  getFriendGroups,
  createFriendGroup,
  updateFriendGroup,
  deleteFriendGroup,
  moveFriendToGroup,
  getContactStatus,
  // 别名
  getContactInfo,
}
