/**
 * 用户通知设置 API
 *
 * @author ruoyi
 */
import request from '../request'

/**
 * 获取用户通知设置
 */
export function getNotificationSetting() {
  return request({
    url: '/api/im/user/notification',
    method: 'get'
  })
}

/**
 * 更新用户通知设置
 * @param {Object} setting - 通知设置对象
 */
export function updateNotificationSetting(setting) {
  return request({
    url: '/api/im/user/notification',
    method: 'put',
    data: setting
  })
}

/**
 * 初始化默认通知设置
 */
export function initDefaultNotificationSetting() {
  return request({
    url: '/api/im/user/notification/init',
    method: 'post'
  })
}
