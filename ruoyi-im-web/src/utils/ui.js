/**
 * UI 交互统一管理
 * 封装常用的确认对话框、消息提示等交互逻辑
 */

import { ElMessageBox, ElMessage } from 'element-plus'

// ========== 确认对话框 ==========

/**
 * 基础确认对话框
 * @param {string} message - 提示消息
 * @param {string} title - 标题
 * @param {string} confirmText - 确认按钮文本
 * @param {string} cancelText - 取消按钮文本
 * @param {string} type - 类型: 'warning' | 'info' | 'success' | 'error'
 * @returns {Promise<boolean>}
 */
export function confirm(message, title = '提示', confirmText = '确定', cancelText = '取消', type = 'warning') {
  return ElMessageBox.confirm(message, title, {
    confirmButtonText: confirmText,
    cancelButtonText: cancelText,
    type,
    distinguishCancelAndClose: true
  }).then(() => true).catch(action => {
    // action: 'cancel' | 'close'
    return false
  })
}

/**
 * 删除确认对话框
 * @param {string} itemName - 要删除的项名称（如：消息、群组、用户）
 * @param {string} customMessage - 自定义消息（可选）
 * @returns {Promise<boolean>}
 */
export function confirmDelete(itemName = '此项目', customMessage) {
  const message = customMessage || `确定要删除${itemName}吗？删除后将无法恢复。`
  return confirm(message, '删除确认', '删除', '取消', 'warning')
}

/**
 * 移除确认对话框
 * @param {string} itemName - 要移除的项名称
 * @returns {Promise<boolean>}
 */
export function confirmRemove(itemName) {
  return confirm(`确定要移除${itemName}吗？`, '移除确认', '移除', '取消')
}

/**
 * 退出确认对话框
 * @param {string} itemName - 要退出的项名称（如：群组）
 * @returns {Promise<boolean>}
 */
export function confirmExit(itemName) {
  return confirm(`确定要退出${itemName}吗？`, '退出确认', '退出', '取消')
}

/**
 * 解散确认对话框
 * @param {string} itemName - 要解散的项名称（如：群组）
 * @returns {Promise<boolean>}
 */
export function confirmDissolve(itemName) {
  return confirm(`确定要解散${itemName}吗？解散后将无法恢复。`, '解散确认', '解散', '取消', 'warning')
}

/**
 * 操作确认对话框（通用）
 * @param {string} action - 操作名称（如：保存、修改、重置）
 * @param {string} target - 操作目标（如：设置、数据）
 * @param {string} detail - 详细说明（可选）
 * @returns {Promise<boolean>}
 */
export function confirmAction(action, target, detail) {
  let message = `确定要${action}${target}吗？`
  if (detail) {
    message = detail
  }
  return confirm(message, `${action}确认`, '确定', '取消')
}

/**
 * 危险操作确认对话框
 * @param {string} message - 警告消息
 * @param {string} title - 标题
 * @returns {Promise<boolean>}
 */
export function confirmDanger(message, title = '危险操作') {
  return confirm(message, title, '确定执行', '取消', 'error')
}

// ========== 消息提示 ==========

/**
 * 成功消息
 * @param {string} message - 消息内容
 * @param {number} duration - 持续时间（毫秒）
 */
export function messageSuccess(message, duration = 3000) {
  return ElMessage.success({ message, duration })
}

/**
 * 错误消息
 * @param {string} message - 消息内容
 * @param {number} duration - 持续时间（毫秒）
 */
export function messageError(message, duration = 3000) {
  return ElMessage.error({ message, duration })
}

/**
 * 警告消息
 * @param {string} message - 消息内容
 * @param {number} duration - 持续时间（毫秒）
 */
export function messageWarning(message, duration = 3000) {
  return ElMessage.warning({ message, duration })
}

/**
 * 信息消息
 * @param {string} message - 消息内容
 * @param {number} duration - 持续时间（毫秒）
 */
export function messageInfo(message, duration = 3000) {
  return ElMessage.info({ message, duration })
}

/**
 * 关闭所有消息
 */
export function closeAllMessages() {
  ElMessage.closeAll()
}

// ========== 通知（带图标和更长时间） ==========

/**
 * 成功通知
 * @param {string} message - 消息内容
 * @param {string} title - 标题（可选）
 */
export function notifySuccess(message, title = '操作成功') {
  return ElMessage.success({
    message,
    duration: 4000,
    showClose: true
  })
}

/**
 * 错误通知
 * @param {string} message - 消息内容
 * @param {string} title - 标题（可选）
 */
export function notifyError(message, title = '操作失败') {
  return ElMessage.error({
    message,
    duration: 5000,
    showClose: true
  })
}

// ========== 加载提示 ==========

/**
 * 显示加载消息
 * @param {string} message - 加载提示文字
 * @returns {MessageHandler} - 消息处理器，用于关闭
 */
export function showLoading(message = '加载中...') {
  return ElMessage.info({
    message,
    duration: 0,
    iconClass: 'el-icon-loading'
  })
}

// ========== 常用快捷方法 ==========

/**
 * 保存成功提示
 */
export function saveSuccess() {
  return messageSuccess('保存成功')
}

/**
 * 保存失败提示
 * @param {string} error - 错误信息
 */
export function saveFail(error) {
  return messageError(error || '保存失败')
}

/**
 * 删除成功提示
 */
export function deleteSuccess() {
  return messageSuccess('删除成功')
}

/**
 * 操作成功提示
 * @param {string} action - 操作名称
 */
export function actionSuccess(action = '操作') {
  return messageSuccess(`${action}成功`)
}

/**
 * 操作失败提示
 * @param {string} action - 操作名称
 * @param {string} error - 错误信息
 */
export function actionFail(action = '操作', error) {
  return messageError(error || `${action}失败`)
}

/**
 * 开发中提示
 */
export function inDevelopment() {
  return messageInfo('功能开发中')
}

/**
 * 权限不足提示
 */
export function noPermission() {
  return messageWarning('权限不足')
}

/**
 * 网络错误提示
 */
export function networkError() {
  return messageError('网络连接失败，请检查网络')
}

/**
 * 请求失败提示（通用）
 * @param {Error} error - 错误对象
 * @param {string} defaultMessage - 默认消息
 */
export function requestFail(error, defaultMessage = '请求失败') {
  const msg = error?.response?.data?.msg || error?.message || defaultMessage
  return messageError(msg)
}

// ========== 输入框 ==========

/**
 * 显示输入框
 * @param {string} title - 标题
 * @param {string} message - 提示消息
 * @param {string} defaultValue - 默认值
 * @param {string} placeholder - 占位符
 * @returns {Promise<string|null>}
 */
export function prompt(title, message = '请输入', defaultValue = '', placeholder = '') {
  return ElMessageBox.prompt(message, title, {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: defaultValue,
    inputPlaceholder: placeholder || '请输入',
    distinguishCancelAndClose: true
  }).then(({ value }) => value || null).catch(() => null)
}

/**
 * 显示密码输入框
 * @param {string} title - 标题
 * @param {string} message - 提示消息
 * @returns {Promise<string|null>}
 */
export function promptPassword(title = '密码验证', message = '请输入密码') {
  return ElMessageBox.prompt(message, title, {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputType: 'password',
    distinguishCancelAndClose: true
  }).then(({ value }) => value || null).catch(() => null)
}

// ========== 批量操作确认 ==========

/**
 * 批量删除确认
 * @param {number} count - 删除数量
 * @param {string} itemName - 项目名称
 * @returns {Promise<boolean>}
 */
export function confirmBatchDelete(count, itemName = '项') {
  return confirmDelete(`${count} 个${itemName}`)
}

/**
 * 批量导出确认
 * @param {number} count - 导出数量
 * @returns {Promise<boolean>}
 */
export function confirmBatchExport(count) {
  return confirm(`确定要导出 ${count} 条数据吗？`, '导出确认', '导出', '取消', 'info')
}

// ========== 导出默认导出 ==========

export {
  ElMessageBox,
  ElMessage
}

export default {
  // 确认对话框
  confirm,
  confirmDelete,
  confirmRemove,
  confirmExit,
  confirmDissolve,
  confirmAction,
  confirmDanger,
  confirmBatchDelete,
  confirmBatchExport,

  // 消息提示
  messageSuccess,
  messageError,
  messageWarning,
  messageInfo,
  closeAllMessages,
  notifySuccess,
  notifyError,

  // 加载提示
  showLoading,

  // 快捷方法
  saveSuccess,
  saveFail,
  deleteSuccess,
  actionSuccess,
  actionFail,
  inDevelopment,
  noPermission,
  networkError,
  requestFail,

  // 输入框
  prompt,
  promptPassword,

  // Element Plus 原生
  ElMessageBox,
  ElMessage
}
