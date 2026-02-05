/**
 * 重试相关常量
 * 统一管理项目中所有的重试次数限制
 */

/**
 * 消息重试最大次数
 *
 * 语义说明：
 * - 包括首次发送，总共最多尝试 MAX_RETRIES 次
 * - 例如：MAX_RETRIES = 3 表示首次发送 + 最多 2 次重试
 * - 重试计数从 0 开始，每次重试前递增
 * - 当 retryCount >= MAX_RETRIES 时，阻止重试
 */
export const MAX_RETRIES = 3

/**
 * 转写任务轮询配置
 */
export const TRANSCRIPT_POLLING = {
  MAX_POLLS: 30,        // 最大轮询次数
  POLL_INTERVAL: 1000,  // 轮询间隔（毫秒）
  PROGRESS_MIN: 0,      // 进度条最小值
  PROGRESS_MAX: 90      // 进度条最大值（完成时设为100）
}

/**
 * API 请求重试配置
 */
export const API_RETRY = {
  MAX_RETRIES: 3,           // 最大重试次数
  INITIAL_DELAY: 1000,      // 初始延迟（毫秒）
  MAX_DELAY: 10000,         // 最大延迟（毫秒）
  BACKOFF_FACTOR: 2         // 退避因子
}

export default {
  MAX_RETRIES,
  TRANSCRIPT_POLLING,
  API_RETRY
}
