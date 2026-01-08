import request from '@/utils/request'

/**
 * 会话相关API
 */
export default {
  /**
   * 获取会话列表
   */
  list(params) {
    return request({
      url: '/api/im/conversation/list',
      method: 'get',
      params,
    })
  },

  /**
   * 获取会话详情
   */
  get(id) {
    return request({
      url: `/api/im/conversation/${id}`,
      method: 'get',
    })
  },

  /**
   * 创建会话
   */
  create(data) {
    return request({
      url: '/api/im/conversation/create',
      method: 'post',
      data,
    })
  },

  /**
   * 更新会话
   */
  update(id, data) {
    return request({
      url: `/api/im/conversation/${id}`,
      method: 'put',
      data,
    })
  },

  /**
   * 删除会话
   */
  delete(id) {
    return request({
      url: `/api/im/conversation/${id}`,
      method: 'delete',
    })
  },

  /**
   * 置顶/取消置顶会话
   */
  setPinned(id, pinned) {
    return request({
      url: `/api/im/conversation/${id}/pinned`,
      method: 'put',
      params: { pinned },
    })
  },

  /**
   * 设置免打扰
   */
  setMuted(id, muted) {
    return request({
      url: `/api/im/conversation/${id}/muted`,
      method: 'put',
      params: { muted },
    })
  },

  /**
   * 搜索会话
   */
  search(keyword) {
    return request({
      url: '/api/im/conversation/search',
      method: 'get',
      params: { keyword },
    })
  },

  /**
   * 标记会话为已读
   */
  markAsRead(id) {
    return request({
      url: `/api/im/conversation/${id}/markAsRead`,
      method: 'put',
    })
  },

  /**
   * 获取未读消息总数
   */
  getTotalUnreadCount() {
    return request({
      url: '/api/im/conversation/unreadCount',
      method: 'get',
    })
  },

  /**
   * 创建私聊会话
   */
  createPrivate(peerUserId) {
    return this.create({
      type: 'PRIVATE',
      targetId: peerUserId,
    })
  },

  /**
   * 创建群聊会话
   */
  createGroup(data) {
    return this.create({
      type: 'GROUP',
      ...data,
    })
  },
}
