/**
 * 通用js方法封装处理
 * @description 通用工具函数集合
 * @author RuoYi
 */

// 日期格式化
export function parseTime(time, pattern) {
  if (arguments.length === 0 || !time) {
    return null
  }
  const format = pattern || '{y}-{m}-{d} {h}:{i}:{s}'
  let date
  if (typeof time === 'object') {
    date = time
  } else {
    if (typeof time === 'string' && /^[0-9]+$/.test(time)) {
      time = parseInt(time)
    } else if (typeof time === 'string') {
      time = time
        .replace(new RegExp(/-/gm), '/')
        .replace('T', ' ')
        .replace(new RegExp(/\.[\d]{3}/gm), '')
    }
    if (typeof time === 'number' && time.toString().length === 10) {
      time = time * 1000
    }
    date = new Date(time)
  }
  const formatObj = {
    y: date.getFullYear(),
    m: date.getMonth() + 1,
    d: date.getDate(),
    h: date.getHours(),
    i: date.getMinutes(),
    s: date.getSeconds(),
    a: date.getDay(),
  }
  const time_str = format.replace(/{(y|m|d|h|i|s|a)+}/g, (result, key) => {
    let value = formatObj[key]
    // Note: getDay() returns 0 on Sunday
    if (key === 'a') {
      return ['日', '一', '二', '三', '四', '五', '六'][value]
    }
    if (result.length > 0 && value < 10) {
      value = '0' + value
    }
    return value || 0
  })
  return time_str
}

// 表单重置
export function resetForm(refName) {
  if (this.$refs[refName]) {
    this.$refs[refName].resetFields()
  }
}

// 添加日期范围
export function addDateRange(params, dateRange, propName) {
  let search = params
  search.params =
    typeof search.params === 'object' && search.params !== null && !Array.isArray(search.params)
      ? search.params
      : {}
  dateRange = Array.isArray(dateRange) ? dateRange : []
  if (typeof propName === 'undefined') {
    search.params['beginTime'] = dateRange[0]
    search.params['endTime'] = dateRange[1]
  } else {
    search.params['begin' + propName] = dateRange[0]
    search.params['end' + propName] = dateRange[1]
  }
  return search
}

// 回显数据字典
export function selectDictLabel(datas, value) {
  if (value === undefined) {
    return ''
  }
  const actions = []
  Object.keys(datas).some(key => {
    if (datas[key].value == '' + value) {
      actions.push(datas[key].label)
      return true
    }
  })
  if (actions.length === 0) {
    actions.push(value)
  }
  return actions.join('')
}

// 回显数据字典（字符串数组）
export function selectDictLabels(datas, value, separator) {
  if (value === undefined) {
    return ''
  }
  const actions = []
  const currentSeparator = undefined === separator ? ',' : separator
  const temp = value.split(currentSeparator)
  Object.keys(value.split(currentSeparator)).some(val => {
    let match = false
    Object.keys(datas).some(key => {
      if (datas[key].value == '' + temp[val]) {
        actions.push(datas[key].label + currentSeparator)
        match = true
      }
    })
    if (!match) {
      actions.push(temp[val] + currentSeparator)
    }
  })
  return actions.join('').substring(0, actions.join('').length - 1)
}

// 字符串格式化(%s )
export function sprintf(str) {
  const args = arguments
  let flag = true
  let i = 1
  str = str.replace(/%s/g, function () {
    const arg = args[i++]
    if (typeof arg === 'undefined') {
      flag = false
      return ''
    }
    return arg
  })
  return flag ? str : ''
}

// 转换字符串，undefined,null等转化为""
export function parseStrEmpty(str) {
  if (!str || str == 'undefined' || str == 'null') {
    return ''
  }
  return str
}

// 数据合并
export function mergeRecursive(source, target) {
  for (const p in target) {
    try {
      if (target[p].constructor == Object) {
        source[p] = mergeRecursive(source[p], target[p])
      } else {
        source[p] = target[p]
      }
    } catch (e) {
      source[p] = target[p]
    }
  }
  return source
}

/**
 * 构造树型结构数据
 * @param {Array} data 数据源
 * @param {String} id id字段 默认 'id'
 * @param {String} parentId 父节点字段 默认 'parentId'
 * @param {String} children 孩子节点字段 默认 'children'
 * @returns {Array} 树型结构数据
 */
export function handleTree(data, id, parentId, children) {
  const config = {
    id: id || 'id',
    parentId: parentId || 'parentId',
    childrenList: children || 'children',
  }

  const childrenListMap = {}
  const nodeIds = {}
  const tree = []

  for (const d of data) {
    const pId = d[config.parentId]
    if (childrenListMap[pId] == null) {
      childrenListMap[pId] = []
    }
    nodeIds[d[config.id]] = d
    childrenListMap[pId].push(d)
  }

  for (const d of data) {
    const pId = d[config.parentId]
    if (nodeIds[pId] == null) {
      tree.push(d)
    }
  }

  for (const t of tree) {
    adaptToChildrenList(t)
  }

  function adaptToChildrenList(o) {
    if (childrenListMap[o[config.id]] !== null) {
      o[config.childrenList] = childrenListMap[o[config.id]]
    }
    if (o[config.childrenList]) {
      for (const c of o[config.childrenList]) {
        adaptToChildrenList(c)
      }
    }
  }
  return tree
}

/**
 * 参数处理
 * @param {Object} params 参数
 * @returns {String} 处理后的参数字符串
 */
export function tansParams(params) {
  let result = ''
  for (const propName of Object.keys(params)) {
    const value = params[propName]
    const part = encodeURIComponent(propName) + '='
    if (value !== null && value !== '' && typeof value !== 'undefined') {
      if (typeof value === 'object') {
        for (const key of Object.keys(value)) {
          if (value[key] !== null && value[key] !== '' && typeof value[key] !== 'undefined') {
            const paramsPropName = propName + '[' + key + ']'
            const subPart = encodeURIComponent(paramsPropName) + '='
            result += subPart + encodeURIComponent(value[key]) + '&'
          }
        }
      } else {
        result += part + encodeURIComponent(value) + '&'
      }
    }
  }
  return result
}

// 验证是否为blob格式
export function blobValidate(data) {
  return data.type !== 'application/json'
}

/**
 * 复制文本到剪贴板
 * @param {String} text 要复制的文本
 * @returns {Promise<Boolean>} 是否成功
 */
export function copyToClipboard(text) {
  return new Promise((resolve, reject) => {
    try {
      if (navigator.clipboard && window.isSecureContext) {
        navigator.clipboard
          .writeText(text)
          .then(() => resolve(true))
          .catch(err => reject(err))
      } else {
        const textArea = document.createElement('textarea')
        textArea.value = text
        textArea.style.cssText = 'top:0;left:0;position:fixed'
        document.body.appendChild(textArea)
        textArea.focus()
        textArea.select()
        try {
          const successful = document.execCommand('copy')
          document.body.removeChild(textArea)
          successful ? resolve(true) : reject(new Error('复制失败'))
        } catch (err) {
          document.body.removeChild(textArea)
          reject(err)
        }
      }
    } catch (err) {
      reject(err)
    }
  })
}

// 文件相关函数请使用 @/utils/format/file.js

/**
 * 防抖函数
 * 在事件被触发n秒后再执行回调，如果在这n秒内又被触发，则重新计时
 * @param {Function} func 要防抖的函数
 * @param {Number} wait 防抖时间（毫秒）
 * @param {Boolean} immediate 是否立即执行
 * @returns {Function} 防抖后的函数
 */
export function debounce(func, wait = 300, immediate = false) {
  let timeout
  return function (...args) {
    const context = this
    clearTimeout(timeout)
    if (immediate) {
      const callNow = !timeout
      timeout = setTimeout(() => {
        timeout = null
      }, wait)
      if (callNow) {
        func.apply(context, args)
      }
    } else {
      timeout = setTimeout(() => {
        func.apply(context, args)
      }, wait)
    }
  }
}

/**
 * 节流函数
 * 规定在一个单位时间内，只能触发一次函数。如果这个单位时间内触发多次函数，只有一次生效
 * @param {Function} func 要节流的函数
 * @param {Number} wait 节流时间（毫秒）
 * @param {Object} options 配置选项
 * @param {Boolean} options.leading 是否在开始时执行
 * @param {Boolean} options.trailing 是否在结束时执行
 * @returns {Function} 节流后的函数
 */
export function throttle(func, wait = 300, options = {}) {
  let timeout, context, args
  let previous = 0

  const later = function () {
    previous = options.leading === false ? 0 : Date.now()
    timeout = null
    func.apply(context, args)
    if (!timeout) {
      context = args = null
    }
  }

  return function (...params) {
    const now = Date.now()
    if (!previous && options.leading === false) {
      previous = now
    }
    const remaining = wait - (now - previous)
    context = this
    args = params

    if (remaining <= 0 || remaining > wait) {
      if (timeout) {
        clearTimeout(timeout)
        timeout = null
      }
      previous = now
      func.apply(context, args)
      if (!timeout) {
        context = args = null
      }
    } else if (!timeout && options.trailing !== false) {
      timeout = setTimeout(later, remaining)
    }
  }
}

/**
 * 请求动画帧节流（用于滚动等高频事件）
 * @param {Function} func 要节流的函数
 * @returns {Function} 节流后的函数
 */
export function rafThrottle(func) {
  let locked = false
  return function (...args) {
    if (locked) return
    locked = true
    requestAnimationFrame(() => {
      func.apply(this, args)
      locked = false
    })
  }
}

/**
 * 空闲执行函数（在浏览器空闲时执行）
 * @param {Function} func 要执行的函数
 * @param {Number} timeout 超时时间（毫秒）
 * @returns {Function} 包装后的函数
 */
export function runIdle(func, timeout = 2000) {
  return function (...args) {
    const context = this
    if ('requestIdleCallback' in window) {
      requestIdleCallback(() => {
        func.apply(context, args)
      }, { timeout })
    } else {
      // 降级方案：使用setTimeout
      setTimeout(() => {
        func.apply(context, args)
      }, 0)
    }
  }
}

/**
 * 批处理函数（累积批量执行）
 * @param {Function} func 要执行的函数
 * @param {Number} wait 等待时间（毫秒）
 * @param {Number} batchSize 批量大小
 * @returns {Object} 包含 add 和 flush 方法的对象
 */
export function batchProcessor(func, wait = 100, batchSize = 10) {
  let items = []
  let timeout = null

  const flush = () => {
    if (items.length > 0) {
      const batch = items.splice(0, batchSize)
      func(batch)
      if (items.length > 0) {
        timeout = setTimeout(flush, 0)
      }
    }
  }

  return {
    add: (item) => {
      items.push(item)
      if (items.length >= batchSize) {
        flush()
      } else if (!timeout) {
        timeout = setTimeout(flush, wait)
      }
    },
    flush: () => {
      if (timeout) {
        clearTimeout(timeout)
        timeout = null
      }
      while (items.length > 0) {
        flush()
      }
    },
  }
}

/**
 * 深度克隆对象
 * @param {*} source 源对象
 * @returns {*} 克隆后的对象
 */
export function deepClone(source) {
  if (source === null || typeof source !== 'object') {
    return source
  }

  if (source instanceof Date) {
    return new Date(source.getTime())
  }

  if (source instanceof Array) {
    return source.map(item => deepClone(item))
  }

  if (source instanceof Object) {
    const cloneObj = {}
    for (const key in source) {
      if (source.hasOwnProperty(key)) {
        cloneObj[key] = deepClone(source[key])
      }
    }
    return cloneObj
  }
}

/**
 * 格式化文件大小
 * @param {Number} bytes 字节数
 * @returns {String} 格式化后的文件大小
 */
export function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

/**
 * 格式化时间显示（聊天消息用）
 * @param {Number|String|Date} time 时间
 * @returns {String} 格式化后的时间显示
 */
export function formatChatTime(time) {
  const now = new Date()
  const date = new Date(time)
  const diff = now - date

  // 小于1分钟
  if (diff < 60000) {
    return '刚刚'
  }

  // 小于1小时
  if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  }

  // 今天
  const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  if (date >= todayStart) {
    return date.getHours().toString().padStart(2, '0') + ':' +
           date.getMinutes().toString().padStart(2, '0')
  }

  // 昨天
  const yesterdayStart = new Date(todayStart - 86400000)
  if (date >= yesterdayStart) {
    return '昨天 ' + date.getHours().toString().padStart(2, '0') + ':' +
           date.getMinutes().toString().padStart(2, '0')
  }

  // 本周
  const weekStart = new Date(todayStart - (now.getDay() || 7) * 86400000)
  if (date >= weekStart) {
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return weekdays[date.getDay()] + ' ' + date.getHours().toString().padStart(2, '0') + ':' +
           date.getMinutes().toString().padStart(2, '0')
  }

  // 今年
  if (date.getFullYear() === now.getFullYear()) {
    return (date.getMonth() + 1) + '月' + date.getDate() + '日'
  }

  // 更早
  return date.getFullYear() + '年' + (date.getMonth() + 1) + '月' + date.getDate() + '日'
}

/**
 * 生成唯一ID
 * @returns {String} 唯一ID
 */
export function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    const r = Math.random() * 16 | 0
    const v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

/**
 * 安全的JSON解析
 * @param {String} json JSON字符串
 * @param {*} defaultValue 默认值
 * @returns {*} 解析结果
 */
export function safeJSONParse(json, defaultValue = null) {
  try {
    return JSON.parse(json)
  } catch (e) {
    return defaultValue
  }
}

/**
 * 安全的JSON字符串化
 * @param {*} obj 要转换的对象
 * @param {String} defaultValue 默认值
 * @returns {*} JSON字符串
 */
export function safeJSONStringify(obj, defaultValue = '{}') {
  try {
    return JSON.stringify(obj)
  } catch (e) {
    return defaultValue
  }
}

/**
 * 检查是否为移动设备
 * @returns {Boolean} 是否为移动设备
 */
export function isMobile() {
  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
}

/**
 * 检查是否为微信浏览器
 * @returns {Boolean} 是否为微信浏览器
 */
export function isWeixin() {
  return /MicroMessenger/i.test(navigator.userAgent)
}
