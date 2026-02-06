/**
 * 生产环境安全的日志工具
 * 开发环境输出日志，生产环境自动禁用
 */

const isDevelopment = import.meta.env.MODE === 'development'

/**
 * 日志级别
 */
const LogLevel = {
  DEBUG: 0,
  INFO: 1,
  WARN: 2,
  ERROR: 3,
  NONE: 4
}

const normalizeLevel = value => {
  if (value === null || value === undefined) {
    return null
  }
  const v = String(value).trim().toLowerCase()
  if (v === 'debug') {
    return LogLevel.DEBUG
  }
  if (v === 'info') {
    return LogLevel.INFO
  }
  if (v === 'warn' || v === 'warning') {
    return LogLevel.WARN
  }
  if (v === 'error') {
    return LogLevel.ERROR
  }
  if (v === 'none' || v === 'off') {
    return LogLevel.NONE
  }
  const n = Number(v)
  if (Number.isFinite(n)) {
    return n
  }
  return null
}

const getStoredLevel = () => {
  try {
    return normalizeLevel(localStorage.getItem('im:logLevel'))
  } catch (e) {
    return null
  }
}

const getRuntimeLevel = () => {
  if (isDevelopment) {
    return LogLevel.DEBUG
  }
  const globalLevel = normalizeLevel(globalThis?.__IM_LOG_LEVEL__)
  if (globalLevel !== null) {
    return globalLevel
  }
  const stored = getStoredLevel()
  if (stored !== null) {
    return stored
  }
  return LogLevel.NONE
}

/**
 * 格式化日志前缀
 */
const formatPrefix = (level, tag) => {
  const timestamp = new Date().toISOString().slice(11, 19)
  const prefix = tag ? `[${timestamp}] [${level}] [${tag}]` : `[${timestamp}] [${level}]`
  return prefix
}

/**
 * 日志工具类
 */
class Logger {
  /**
   * 调试日志
   */
  debug(tag, ...args) {
    if (getRuntimeLevel() <= LogLevel.DEBUG) {
      console.debug(formatPrefix('DEBUG', tag), ...args)
    }
  }

  /**
   * 信息日志
   */
  info(tag, ...args) {
    if (getRuntimeLevel() <= LogLevel.INFO) {
      console.info(formatPrefix('INFO', tag), ...args)
    }
  }

  /**
   * 警告日志
   */
  warn(tag, ...args) {
    if (getRuntimeLevel() <= LogLevel.WARN) {
      console.warn(formatPrefix('WARN', tag), ...args)
    }
  }

  /**
   * 错误日志
   */
  error(tag, ...args) {
    if (getRuntimeLevel() <= LogLevel.ERROR) {
      console.error(formatPrefix('ERROR', tag), ...args)
    }
  }

  /**
   * 分组日志
   */
  group(tag, title) {
    if (getRuntimeLevel() <= LogLevel.DEBUG) {
      console.group(`${formatPrefix('DEBUG', tag)} ${title}`)
    }
  }

  /**
   * 分组结束
   */
  groupEnd() {
    if (getRuntimeLevel() <= LogLevel.DEBUG) {
      console.groupEnd()
    }
  }

  /**
   * 表格日志
   */
  table(tag, data) {
    if (getRuntimeLevel() <= LogLevel.DEBUG) {
      console.log(formatPrefix('DEBUG', tag))
      console.table(data)
    }
  }
}

// 导出单例
const logger = new Logger()

// 导出便捷方法
export const debug = (tag, ...args) => logger.debug(tag, ...args)
export const info = (tag, ...args) => logger.info(tag, ...args)
export const warn = (tag, ...args) => logger.warn(tag, ...args)
export const error = (tag, ...args) => logger.error(tag, ...args)
export const group = (tag, title) => logger.group(tag, title)
export const groupEnd = () => logger.groupEnd()
export const table = (tag, data) => logger.table(tag, data)

export const setLogLevel = level => {
  const normalized = normalizeLevel(level)
  if (normalized === null) {
    return false
  }
  const value =
    normalized === LogLevel.DEBUG
      ? 'debug'
      : normalized === LogLevel.INFO
        ? 'info'
        : normalized === LogLevel.WARN
          ? 'warn'
          : normalized === LogLevel.ERROR
            ? 'error'
            : 'none'
  try {
    localStorage.setItem('im:logLevel', value)
    return true
  } catch (e) {
    return false
  }
}

export const clearLogLevel = () => {
  try {
    localStorage.removeItem('im:logLevel')
    return true
  } catch (e) {
    return false
  }
}

export default logger
