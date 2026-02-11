/**
 * 公告管理 API
 * 提供公告发布、管理、已读统计等功能
 */
import request from '../request'

/**
 * 创建公告
 * @param {Object} data - 公告数据
 * @param {string} data.title - 公告标题
 * @param {string} data.content - 公告内容
 * @param {string} data.announcementType - 公告类型：SYSTEM/NOTICE/ACTIVITY
 * @param {Array} data.targetType - 目标类型：ALL/DEPARTMENT/USER/GROUP
 * @param {Array} data.targetIds - 目标ID列表
 * @param {string} data.priority - 优先级：LOW/MEDIUM/HIGH
 * @returns {Promise}
 */
export function createAnnouncement(data) {
  return request({
    url: '/api/im/announcements',
    method: 'post',
    data
  })
}

/**
 * 更新公告
 * @param {Object} data - 公告数据
 * @param {number} data.id - 公告ID
 * @param {string} data.title - 公告标题
 * @param {string} data.content - 公告内容
 * @param {string} data.priority - 优先级：LOW/MEDIUM/HIGH
 * @returns {Promise}
 */
export function updateAnnouncement(data) {
  return request({
    url: '/api/im/announcements',
    method: 'put',
    data
  })
}

/**
 * 删除公告
 * @param {number} announcementId - 公告ID
 * @returns {Promise}
 */
export function deleteAnnouncement(announcementId) {
  return request({
    url: `/api/im/announcements/${announcementId}`,
    method: 'delete'
  })
}

/**
 * 获取公告详情
 * @param {number} announcementId - 公告ID
 * @returns {Promise}
 */
export function getAnnouncementDetail(announcementId) {
  return request({
    url: `/api/im/announcements/${announcementId}`,
    method: 'get'
  })
}

/**
 * 分页查询公告列表
 * @param {Object} data - 查询条件
 * @param {string} data.keyword - 关键词
 * @param {string} data.announcementType - 公告类型
 * @param {string} data.status - 状态：DRAFT/PUBLISHED/WITHDRAWN
 * @param {number} data.pageNum - 页码
 * @param {number} data.pageSize - 每页数量
 * @returns {Promise}
 */
export function getAnnouncementPage(data) {
  return request({
    url: '/api/im/announcements/page',
    method: 'post',
    data
  })
}

/**
 * 获取置顶公告列表
 * @returns {Promise}
 */
export function getPinnedAnnouncements() {
  return request({
    url: '/api/im/announcements/pinned',
    method: 'get'
  })
}

/**
 * 获取最新公告列表
 * @param {number} limit - 限制数量，默认10条
 * @returns {Promise}
 */
export function getLatestAnnouncements(limit = 10) {
  return request({
    url: '/api/im/announcements/latest',
    method: 'get',
    params: { limit }
  })
}

/**
 * 发布公告
 * @param {number} announcementId - 公告ID
 * @returns {Promise}
 */
export function publishAnnouncement(announcementId) {
  return request({
    url: `/api/im/announcements/${announcementId}/publish`,
    method: 'post'
  })
}

/**
 * 撤回公告
 * @param {number} announcementId - 公告ID
 * @returns {Promise}
 */
export function withdrawAnnouncement(announcementId) {
  return request({
    url: `/api/im/announcements/${announcementId}/withdraw`,
    method: 'post'
  })
}

/**
 * 标记公告为已读
 * @param {number} announcementId - 公告ID
 * @returns {Promise}
 */
export function markAnnouncementAsRead(announcementId) {
  return request({
    url: `/api/im/announcements/${announcementId}/read`,
    method: 'post'
  })
}

/**
 * 全部标记为已读
 * @returns {Promise}
 */
export function markAllAnnouncementsAsRead() {
  return request({
    url: '/api/im/announcements/read-all',
    method: 'post'
  })
}

/**
 * 点赞/取消点赞公告
 * @param {number} announcementId - 公告ID
 * @returns {Promise}
 */
export function toggleLikeAnnouncement(announcementId) {
  return request({
    url: `/api/im/announcements/${announcementId}/like`,
    method: 'post'
  })
}

/**
 * 添加公告评论
 * @param {number} announcementId - 公告ID
 * @param {string} content - 评论内容
 * @returns {Promise}
 */
export function addAnnouncementComment(announcementId, content) {
  return request({
    url: `/api/im/announcements/${announcementId}/comments`,
    method: 'post',
    params: { content }
  })
}

/**
 * 获取公告统计信息
 * @returns {Promise}
 */
export function getAnnouncementStatistics() {
  return request({
    url: '/api/im/announcements/statistics',
    method: 'get'
  })
}

/**
 * 获取已读用户列表
 * @param {number} announcementId - 公告ID
 * @returns {Promise}
 */
export function getAnnouncementReadUsers(announcementId) {
  return request({
    url: `/api/im/announcements/${announcementId}/read-users`,
    method: 'get'
  })
}

/**
 * 获取点赞用户列表
 * @param {number} announcementId - 公告ID
 * @returns {Promise}
 */
export function getAnnouncementLikedUsers(announcementId) {
  return request({
    url: `/api/im/announcements/${announcementId}/liked-users`,
    method: 'get'
  })
}

/**
 * 置顶/取消置顶公告
 * @param {number} announcementId - 公告ID
 * @param {boolean} pinned - 是否置顶
 * @returns {Promise}
 */
export function setAnnouncementPinned(announcementId, pinned) {
  return request({
    url: `/api/im/announcements/${announcementId}/pin`,
    method: 'put',
    params: { pinned }
  })
}
