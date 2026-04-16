import request from '@/api/request'

/**
 * 待办任务模块 API (纯 JS 规范)
 * 严格遵守 Doc-33 接口契约
 */
export const todoApi = {
  /**
   * 获取待办列表 (支持分页与状态过滤)
   */
  getList: (params) => {
    return request({
      url: '/api/im/todo/list',
      method: 'get',
      params
    })
  },

  /**
   * 创建待办 (支持从消息转化)
   */
  create: (data) => {
    return request({
      url: '/api/im/todo',
      method: 'post',
      data
    })
  },

  /**
   * 切换待办状态 (DONE/PENDING)
   */
  toggleStatus: (id) => {
    return request({
      url: `/api/im/todo/${id}/toggle`,
      method: 'put'
    })
  },

  /**
   * 删除待办
   */
  delete: (id) => {
    return request({
      url: `/api/im/todo/${id}`,
      method: 'delete'
    })
  }
}
