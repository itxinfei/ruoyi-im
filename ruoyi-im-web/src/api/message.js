import request from '@/utils/request';

/**
 * 消息模块核心 API (纯 JS 规范)
 * 严格遵守 Doc-33 接口契约
 */
export const messageApi = {
  /**
   * 发送即时消息
   * @param {Object} payload 包含 conversationId, messageType, content, clientMsgId 等
   * @returns {Promise} AjaxResult
   */
  send: (payload) => {
    return request({
      url: '/api/message/send',
      method: 'post',
      data: payload
    });
  },

  /**
   * 分页拉取历史消息
   * @param {Object} params 包含 conversationId, lastMessageId, pageSize
   * @returns {Promise} AjaxResult
   */
  getList: (params) => {
    return request({
      url: '/api/message/list',
      method: 'get',
      params: params
    });
  },

  /**
   * 撤回消息 (2分钟内)
   * @param {Long} messageId 
   * @returns {Promise} AjaxResult
   */
  revoke: (messageId) => {
    return request({
      url: `/api/message/${messageId}/revoke`,
      method: 'put'
    });
  },

  /**
   * 编辑消息 (5分钟内)
   * @param {Long} messageId 
   * @param {String} newContent 
   * @returns {Promise} AjaxResult
   */
  edit: (messageId, newContent) => {
    return request({
      url: `/api/message/${messageId}/edit`,
      method: 'put',
      data: { content: newContent }
    });
  },

  /**
   * 批量标记已读
   * @param {Object} payload 包含 conversationId, lastReadMessageId
   * @returns {Promise} AjaxResult
   */
  readBatch: (payload) => {
    return request({
      url: '/api/message/read',
      method: 'post',
      data: payload
    });
  },

  /**
   * 解析 URL 元数据 (Doc-33 §4.7)
   * @param {String} url 
   * @returns {Promise} AjaxResult
   */
  parseUrl: (url) => {
    return request({
      url: '/api/im/url/parse',
      method: 'get',
      params: { url }
    });
  }
};
