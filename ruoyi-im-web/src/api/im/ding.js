/**
 * DING消息 API
 */
import request from '@/utils/request'

const BASE_URL = '/im/ding'

/**
 * 获取DING消息列表
 * @param {Object} params - 查询参数
 * @param {string} params.type - 类型: all/sent/received/unread/urgent
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 */
export function getDingList(params) {
  return request({
    url: `${BASE_URL}/list`,
    method: 'get',
    params,
  })
}

/**
 * 获取DING消息详情
 * @param {number} id - DING消息ID
 */
export function getDingDetail(id) {
  return request({
    url: `${BASE_URL}/${id}`,
    method: 'get',
  })
}

/**
 * 发送DING消息
 * @param {Object} data - DING消息数据
 */
export function sendDing(data) {
  return request({
    url: `${BASE_URL}/send`,
    method: 'post',
    data,
  })
}

/**
 * 再次提醒
 * @param {number} id - DING消息ID
 */
export function remindDing(id) {
  return request({
    url: `${BASE_URL}/${id}/remind`,
    method: 'post',
  })
}

/**
 * 标记已读
 * @param {number} id - DING消息ID
 */
export function markDingRead(id) {
  return request({
    url: `${BASE_URL}/${id}/read`,
    method: 'put',
  })
}

/**
 * 确认DING（针对需要确认的DING）
 * @param {number} id - DING消息ID
 * @param {Object} data - 确认数据
 */
export function confirmDing(id, data) {
  return request({
    url: `${BASE_URL}/${id}/confirm`,
    method: 'post',
    data,
  })
}

/**
 * 删除DING消息
 * @param {number} id - DING消息ID
 */
export function deleteDing(id) {
  return request({
    url: `${BASE_URL}/${id}`,
    method: 'delete',
  })
}

/**
 * 获取DING回执列表
 * @param {number} id - DING消息ID
 */
export function getDingReceipt(id) {
  return request({
    url: `${BASE_URL}/${id}/receipt`,
    method: 'get',
  })
}

/**
 * 获取未读DING数量
 */
export function getUnreadDingCount() {
  return request({
    url: `${BASE_URL}/unread/count`,
    method: 'get',
  })
}

/**
 * 获取DING统计信息
 */
export function getDingStatistics() {
  return request({
    url: `${BASE_URL}/statistics`,
    method: 'get',
  })
}
