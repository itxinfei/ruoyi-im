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
 * @param {number} requestId - 申请ID
 * @param {boolean} approved - 是否通过
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
 * 创建好友分组
 * @param {Object} data - 分组数据
 * @param {string} data.groupName - 分组名称
 * @returns {Promise}
 */
export function createGroup(data) {
  return request({
    url: '/api/im/contact/group',
    method: 'post',
    data
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

/**
 * 获取用户标签列表
 * 获取当前用户所有的好友标签
 * @returns {Promise}
 */
export function getUserTags() {
  return request({
    url: '/api/im/contact/tags',
    method: 'get'
  })
}

/**
 * 更新用户标签
 * 为好友设置或更新标签
 * @param {number} userId - 用户ID（联系人ID）
 * @param {Array<string>} tags - 标签列表
 * @returns {Promise}
 */
export function updateUserTags(userId, tags) {
  return request({
    url: `/api/im/contact/${userId}/tags`,
    method: 'put',
    data: { tags }
  })
}

/**
 * 按标签获取联系人
 * 根据标签筛选联系人
 * @param {string} tag - 标签名称
 * @returns {Promise}
 */
export function getContactsByTag(tag) {
  return request({
    url: `/api/im/contact/tag/${encodeURIComponent(tag)}`,
    method: 'get'
  })
}

/**
 * 获取推荐好友列表
 * 基于部门、手机号等推荐可能认识的人
 * @param {Object} params - 查询参数
 * @param {string} params.type - 推荐类型: department(同事), phone(手机号), all(全部)
 * @param {number} params.limit - 返回数量限制
 * @returns {Promise}
 */
export function getRecommendedContacts(params) {
  return request({
    url: '/api/im/contact/recommendations',
    method: 'get',
    params
  })
}

/**
 * 上传通讯录用于好友匹配
 * @param {Object} data - 通讯录数据
 * @param {Array} data.contacts - 联系人列表 {name, phone}
 * @returns {Promise}
 */
export function uploadAddressBook(data) {
  return request({
    url: '/api/im/contact/address-book/upload',
    method: 'post',
    data
  })
}

/**
 * 获取通讯录匹配的好友
 * @returns {Promise}
 */
export function getAddressBookMatches() {
  return request({
    url: '/api/im/contact/address-book/matches',
    method: 'get'
  })
}

/**
 * 批量添加好友
 * @param {Object} data - 请求数据
 * @param {Array<number>} data.userIds - 用户ID列表
 * @param {string} data.remark - 备注信息
 * @returns {Promise}
 */
export function batchAddFriends(data) {
  return request({
    url: '/api/im/contact/batch-add',
    method: 'post',
    data
  })
}

/**
 * 批量移动联系人到分组
 * @param {Object} data - 请求数据
 * @param {Array<number>} data.contactIds - 联系人ID列表
 * @param {string} data.groupName - 分组名称
 * @returns {Promise}
 */
export function batchMoveToGroup(data) {
  return request({
    url: '/api/im/contact/batch-move',
    method: 'put',
    data
  })
}

/**
 * 批量删除联系人
 * @param {Array<number>} contactIds - 联系人ID列表
 * @returns {Promise}
 */
export function batchDeleteContacts(contactIds) {
  return request({
    url: '/api/im/contact/batch-delete',
    method: 'delete',
    data: { contactIds }
  })
}

/**
 * 清除好友列表缓存
 * 清除当前用户的好友列表Redis缓存，用于刷新数据
 * @returns {Promise}
 */
export function clearFriendListCache() {
  return request({
    url: '/api/im/contact/cache/clear',
    method: 'post'
  })
}

/**
 * 批量清除多个用户的好友列表缓存
 * @param {Array<number>} userIds - 用户ID列表
 * @returns {Promise}
 */
export function batchClearFriendListCache(userIds) {
  return request({
    url: '/api/im/contact/cache/clear-batch',
    method: 'post',
    data: { userIds }
  })
}
