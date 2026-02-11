/**
 * 自定义表情包 API
 */
import request from '@/api/request'

// 获取用户的表情列表
export function getEmojiList() {
  return request({
    url: '/api/im/emojis/list',
    method: 'get'
  })
}

// 获取指定分类的表情列表
export function getEmojisByCategory(category) {
  return request({
    url: `/api/im/emojis/category/${category}`,
    method: 'get'
  })
}

// 获取公开的表情列表
export function getPublicEmojis() {
  return request({
    url: '/api/im/emojis/public',
    method: 'get'
  })
}

// 上传自定义表情
export function uploadEmoji(data) {
  return request({
    url: '/api/im/emojis/upload',
    method: 'post',
    data: data
  })
}

// 删除自定义表情
export function deleteEmoji(emojiId) {
  return request({
    url: `/api/im/emojis/${emojiId}`,
    method: 'delete'
  })
}

// 使用表情（增加使用次数）
export function useEmoji(emojiId) {
  return request({
    url: `/api/im/emojis/use/${emojiId}`,
    method: 'post'
  })
}

// 分享表情
export function shareEmoji(emojiId) {
  return request({
    url: `/api/im/emojis/share/${emojiId}`,
    method: 'post'
  })
}
