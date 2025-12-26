/**
 * 本地存储工具类
 * 提供localStorage和sessionStorage的统一封装
 */

/**
 * 设置本地存储
 * @param {string} key 存储键名
 * @param {*} value 存储值
 * @param {string} type 存储类型，默认为localStorage
 */
export function setStorage(key, value, type = 'local') {
  const storage = type === 'session' ? sessionStorage : localStorage
  try {
    storage.setItem(key, JSON.stringify(value))
  } catch (error) {
    console.error('设置本地存储失败:', error)
  }
}

/**
 * 获取本地存储
 * @param {string} key 存储键名
 * @param {*} defaultValue 默认值
 * @param {string} type 存储类型，默认为localStorage
 * @returns {*} 存储值
 */
export function getStorage(key, defaultValue = null, type = 'local') {
  const storage = type === 'session' ? sessionStorage : localStorage
  try {
    const value = storage.getItem(key)
    return value ? JSON.parse(value) : defaultValue
  } catch (error) {
    console.error('获取本地存储失败:', error)
    return defaultValue
  }
}

/**
 * 移除本地存储
 * @param {string} key 存储键名
 * @param {string} type 存储类型，默认为localStorage
 */
export function removeStorage(key, type = 'local') {
  const storage = type === 'session' ? sessionStorage : localStorage
  try {
    storage.removeItem(key)
  } catch (error) {
    console.error('移除本地存储失败:', error)
  }
}

/**
 * 清空本地存储
 * @param {string} type 存储类型，默认为localStorage
 */
export function clearStorage(type = 'local') {
  const storage = type === 'session' ? sessionStorage : localStorage
  try {
    storage.clear()
  } catch (error) {
    console.error('清空本地存储失败:', error)
  }
}

/**
 * 设置本地存储（localStorage）
 */
export const localStorageUtil = {
  set: (key, value) => setStorage(key, value, 'local'),
  get: (key, defaultValue) => getStorage(key, defaultValue, 'local'),
  remove: key => removeStorage(key, 'local'),
  clear: () => clearStorage('local'),
}

/**
 * 设置会话存储（sessionStorage）
 */
export const sessionStorageUtil = {
  set: (key, value) => setStorage(key, value, 'session'),
  get: (key, defaultValue) => getStorage(key, defaultValue, 'session'),
  remove: key => removeStorage(key, 'session'),
  clear: () => clearStorage('session'),
}

export default {
  setStorage,
  getStorage,
  removeStorage,
  clearStorage,
  localStorageUtil,
  sessionStorageUtil,
}
