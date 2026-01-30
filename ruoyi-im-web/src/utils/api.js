/**
 * API 请求/响应统一处理
 * 封装常用的 API 调用模式
 */

import { ElMessage } from 'element-plus'
import { messageError } from './ui'

// ========== 响应码常量 ==========
export const ResponseCode = {
  SUCCESS: 200,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  ERROR: 500
}

// ========== 响应处理 ==========

/**
 * 检查响应是否成功
 * @param {Object} response - API 响应对象
 * @returns {boolean}
 */
export function isSuccess(response) {
  return response?.code === ResponseCode.SUCCESS
}

/**
 * 检查响应是否失败
 * @param {Object} response - API 响应对象
 * @returns {boolean}
 */
export function isFailure(response) {
  return !isSuccess(response)
}

/**
 * 获取响应数据
 * @param {Object} response - API 响应对象
 * @param {any} defaultValue - 默认值
 * @returns {any}
 */
export function getData(response, defaultValue = null) {
  return isSuccess(response) ? response.data : defaultValue
}

/**
 * 获取响应消息
 * @param {Object} response - API 响应对象
 * @param {string} defaultMessage - 默认消息
 * @returns {string}
 */
export function getMessage(response, defaultMessage = '操作失败') {
  return response?.msg || response?.message || defaultMessage
}

/**
 * 处理 API 响应（通用）
 * @param {Object} response - API 响应对象
 * @param {Object} options - 选项
 * @param {boolean} options.showError - 是否显示错误提示
 * @param {boolean} options.showSuccess - 是否显示成功提示
 * @param {string} options.successMsg - 成功提示消息
 * @param {string} options.errorMsg - 失败提示消息
 * @returns {boolean} - 是否成功
 */
export function handleResponse(response, options = {}) {
  const {
    showError = true,
    showSuccess = false,
    successMsg = '操作成功',
    errorMsg = null
  } = options

  if (isSuccess(response)) {
    if (showSuccess) {
      ElMessage.success(successMsg)
    }
    return true
  } else {
    if (showError) {
      messageError(errorMsg || getMessage(response))
    }
    return false
  }
}

/**
 * 处理删除响应
 * @param {Object} response - API 响应对象
 * @param {string} itemName - 删除项名称
 * @returns {boolean}
 */
export function handleDeleteResponse(response, itemName = '') {
  return handleResponse(response, {
    showSuccess: true,
    successMsg: itemName ? `${itemName}删除成功` : '删除成功',
    errorMsg: itemName ? `删除${itemName}失败` : '删除失败'
  })
}

/**
 * 处理保存响应
 * @param {Object} response - API 响应对象
 * @param {string} itemName - 保存项名称
 * @returns {boolean}
 */
export function handleSaveResponse(response, itemName = '') {
  return handleResponse(response, {
    showSuccess: true,
    successMsg: itemName ? `${itemName}保存成功` : '保存成功',
    errorMsg: itemName ? `保存${itemName}失败` : '保存失败'
  })
}

/**
 * 处理更新响应
 * @param {Object} response - API 响应对象
 * @param {string} itemName - 更新项名称
 * @returns {boolean}
 */
export function handleUpdateResponse(response, itemName = '') {
  return handleResponse(response, {
    showSuccess: true,
    successMsg: itemName ? `${itemName}更新成功` : '更新成功',
    errorMsg: itemName ? `更新${itemName}失败` : '更新失败'
  })
}

/**
 * 异步操作包装器（统一处理错误）
 * @param {Function} asyncFn - 异步函数
 * @param {Object} options - 选项
 * @param {boolean} options.showError - 是否显示错误提示
 * @param {string} options.errorMsg - 错误提示消息
 * @param {Function} options.onError - 错误回调
 * @returns {Promise<{success: boolean, data: any, error: any}>}
 */
export async function withErrorHandling(asyncFn, options = {}) {
  const {
    showError = true,
    errorMsg = null,
    onError = null
  } = options

  try {
    const response = await asyncFn()
    return {
      success: isSuccess(response),
      data: getData(response),
      error: null
    }
  } catch (error) {
    const message = errorMsg || getMessage(error.response || error, '请求失败')
    if (showError) {
      messageError(message)
    }
    if (onError) {
      onError(error)
    }
    return {
      success: false,
      data: null,
      error
    }
  }
}

/**
 * 分页数据处理
 * @param {Object} response - API 响应对象
 * @returns {Object} - { list, total, pageSize, currentPage }
 */
export function getPaginatedData(response) {
  if (isFailure(response)) {
    return { list: [], total: 0, pageSize: 10, currentPage: 1 }
  }

  const data = response.data || {}
  return {
    list: data.rows || data.list || data.items || [],
    total: data.total || 0,
    pageSize: data.pageSize || 10,
    currentPage: data.currentPage || data.page || 1
  }
}

/**
 * 构建分页参数
 * @param {number} page - 页码
 * @param {number} pageSize - 每页数量
 * @returns {Object}
 */
export function buildPageParams(page = 1, pageSize = 10) {
  return {
    page,
    pageSize,
    pageNum: page,
    limit: pageSize,
    offset: (page - 1) * pageSize
  }
}

// ========== 导出默认 ==========
export default {
  isSuccess,
  isFailure,
  getData,
  getMessage,
  handleResponse,
  handleDeleteResponse,
  handleSaveResponse,
  handleUpdateResponse,
  withErrorHandling,
  getPaginatedData,
  buildPageParams,
  ResponseCode
}
