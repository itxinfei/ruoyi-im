/**
 * 群组模块API
 * @module api/im/group
 */
import request from '@/utils/request'

/**
 * 获取群组列表
 * @param {Object} [params] - 查询参数
 * @param {number} [params.pageSize=20] - 每页数量
 * @param {number} [params.pageNum=1] - 当前页码
 * @param {string} [params.keyword] - 搜索关键词
 * @returns {Promise}
 */
export function listGroup(params) {
  return request({
    url: '/api/im/group/list',
    method: 'get',
    params: {
      pageSize: params?.pageSize || 20,
      pageNum: params?.pageNum || 1,
      keyword: params?.keyword,
    },
  })
}

/**
 * 获取群组详情
 * @param {string} groupId - 群组ID
 * @returns {Promise}
 */
export function getGroup(groupId) {
  return request({
    url: `/api/im/group/${groupId}`,
    method: 'get',
  })
}

/**
 * 创建群组
 * @param {Object} data - 群组数据
 * @param {string} data.name - 群组名称
 * @param {string} [data.avatar] - 群组头像
 * @param {string} [data.description] - 群组描述
 * @param {string[]} data.memberIds - 初始成员ID数组
 * @returns {Promise}
 */
export function createGroup(data) {
  return request({
    url: '/api/im/group/create',
    method: 'post',
    data,
  })
}

/**
 * 更新群组信息
 * @param {string} groupId - 群组ID
 * @param {Object} data - 更新数据
 * @returns {Promise}
 */
export function updateGroup(groupId, data) {
  return request({
    url: `/api/im/group/${groupId}`,
    method: 'put',
    data,
  })
}

/**
 * 删除群组
 * @param {string} groupId - 群组ID
 * @returns {Promise}
 */
export function deleteGroup(groupId) {
  return request({
    url: `/api/im/group/${groupId}`,
    method: 'delete',
  })
}

/**
 * 更新群组设置
 * @param {string} groupId - 群组ID
 * @param {Object} data - 设置数据
 * @param {boolean} [data.muteAll] - 是否全员禁言
 * @param {boolean} [data.allowMemberInvite] - 是否允许成员邀请
 * @param {boolean} [data.allowMemberModify] - 是否允许成员修改群信息
 * @returns {Promise}
 */
export function updateGroupSettings(groupId, data) {
  return updateGroup(groupId, data)
}

/**
 * 获取群组设置
 * @param {string} groupId - 群组ID
 * @returns {Promise}
 */
export function getGroupSettings(groupId) {
  return getGroup(groupId)
}

/**
 * 更新群组公告
 * @param {string} groupId - 群组ID
 * @param {string} announcement - 公告内容
 * @returns {Promise}
 */
export function updateGroupAnnouncement(groupId, announcement) {
  return updateGroup(groupId, { announcement })
}

/**
 * 获取群组公告
 * @param {string} groupId - 群组ID
 * @returns {Promise}
 */
export function getGroupAnnouncement(groupId) {
  return getGroup(groupId).then(response => ({
    data: response.data?.announcement || '',
  }))
}

/**
 * 解散群组
 * @param {string} groupId - 群组ID
 * @returns {Promise}
 */
export function dismissGroup(groupId) {
  return deleteGroup(groupId)
}

/**
 * 退出群组
 * @param {string} groupId - 群组ID
 * @returns {Promise}
 */
export function quitGroup(groupId) {
  return request({
    url: `/api/im/group/${groupId}/quit`,
    method: 'post',
  })
}

/**
 * 移除群成员
 * @param {string} groupId - 群组ID
 * @param {string} userId - 用户ID
 * @returns {Promise}
 */
export function removeGroupMember(groupId, userId) {
  return request({
    url: `/api/im/group/${groupId}/members/${userId}`,
    method: 'delete',
  })
}

// ========== 别名 - 向后兼容 ==========

export const addGroup = createGroup
export const delGroup = deleteGroup

// 默认导出 - 方便批量引入
export default {
  listGroup,
  getGroup,
  createGroup,
  updateGroup,
  deleteGroup,
  updateGroupSettings,
  getGroupSettings,
  updateGroupAnnouncement,
  getGroupAnnouncement,
  dismissGroup,
  quitGroup,
  removeGroupMember,
  // 别名
  addGroup,
  delGroup,
}
