/**
 * 快捷回复 API
 */
import request from '@/api/request'

// 获取用户的快捷回复列表
export function getQuickReplyList() {
  return request({
    url: '/api/im/quick-reply',
    method: 'get'
  })
}

// 获取指定分类的快捷回复
export function getQuickRepliesByCategory(category) {
  return request({
    url: `/api/im/quick-reply/category/${category}`,
    method: 'get'
  })
}

// 创建快捷回复
export function createQuickReply(data) {
  return request({
    url: '/api/im/quick-reply',
    method: 'post',
    data: data
  })
}

// 更新快捷回复
export function updateQuickReply(data) {
  return request({
    url: '/api/im/quick-reply',
    method: 'put',
    data: data
  })
}

// 删除快捷回复
export function deleteQuickReply(id) {
  return request({
    url: `/api/im/quick-reply/${id}`,
    method: 'delete'
  })
}

// 使用快捷回复（增加使用次数）
export function useQuickReply(id) {
  return request({
    url: `/api/im/quick-reply/use/${id}`,
    method: 'post'
  })
}

// 批量更新排序
export function updateSortOrder(idList) {
  return request({
    url: '/api/im/quick-reply/sort',
    method: 'put',
    data: idList
  })
}
