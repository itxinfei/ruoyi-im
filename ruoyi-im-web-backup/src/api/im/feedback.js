/**
 * @file 反馈建议 API
 * @description 用户反馈建议相关接口
 */
import request from '@/utils/request'

/**
 * 提交反馈建议
 * @param {Object} data - 反馈数据
 * @param {string} data.type - 反馈类型 (bug/feature/improvement/other)
 * @param {string} data.title - 反馈标题
 * @param {string} data.content - 反馈内容
 * @param {string} data.contact - 联系方式
 * @param {Array} data.attachments - 附件列表
 * @returns {Promise}
 */
export function submitFeedback(data) {
  return request({
    url: '/im/feedback/submit',
    method: 'post',
    data: data,
  })
}

/**
 * 获取反馈列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getFeedbackList(params) {
  return request({
    url: '/im/feedback/list',
    method: 'get',
    params: params,
  })
}

/**
 * 获取反馈详情
 * @param {string} id - 反馈ID
 * @returns {Promise}
 */
export function getFeedbackDetail(id) {
  return request({
    url: `/im/feedback/${id}`,
    method: 'get',
  })
}

/**
 * 上传反馈截图
 * @param {FormData} formData - 文件数据
 * @returns {Promise}
 */
export function uploadFeedbackImage(formData) {
  return request({
    url: '/common/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

/**
 * 获取反馈类型列表
 * @returns {Promise}
 */
export function getFeedbackTypes() {
  return request({
    url: '/im/feedback/types',
    method: 'get',
  })
}
