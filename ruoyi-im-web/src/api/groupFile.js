import request from '@/api/request'

/**
 * 群文件模块 API (纯 JS 规范)
 * 严格遵守 Doc-33 接口契约
 */
export const groupFileApi = {
  /**
   * 上传群组文件
   * @param {FormData} payload 包含 groupId, file, category 等
   * @returns {Promise} AjaxResult
   */
  upload: (payload) => {
    return request({
      url: '/api/im/group/file',
      method: 'post',
      data: payload,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 获取群组文件列表
   * @param {Object} payload 包含 groupId, category, keyword, pageNum, pageSize
   * @returns {Promise} AjaxResult
   */
  getList: (payload) => {
    return request({
      url: '/api/im/group/file/list',
      method: 'post',
      data: payload
    })
  },

  /**
   * 获取群组文件统计信息
   * @param {Long} groupId
   * @returns {Promise} AjaxResult
   */
  getStatistics: (groupId) => {
    return request({
      url: `/api/im/group/file/statistics/${groupId}`,
      method: 'get'
    })
  },

  /**
   * 删除群组文件
   * @param {Long} groupFileId
   * @returns {Promise} AjaxResult
   */
  delete: (groupFileId) => {
    return request({
      url: `/api/im/group/file/${groupFileId}`,
      method: 'delete'
    })
  },

  /**
   * 下载群组文件
   * @param {Long} groupFileId
   * @returns {Promise} AjaxResult (包含 fileUrl)
   */
  download: (groupFileId) => {
    return request({
      url: `/api/im/group/file/download/${groupFileId}`,
      method: 'get'
    })
  }
}
