/**
 * 功能开关配置
 * 用于控制功能的启用/禁用状态
 *
 * 注意：这些开关应与后端配置保持一致
 * 部分功能可能需要后端 API 支持才能正常工作
 */

/**
 * 功能开关枚举
 */
export const FeatureFlags = {
  // 定时消息功能（需要后端 API 支持）
  SCHEDULED_MESSAGE: 'SCHEDULED_MESSAGE',

  // AI 智能回复
  AI_SMART_REPLY: 'AI_SMART_REPLY',

  // AI 表情反应
  AI_EMOJI_REACTION: 'AI_EMOJI_REACTION',

  // 语音通话
  VOICE_CALL: 'VOICE_CALL',

  // 视频通话
  VIDEO_CALL: 'VIDEO_CALL',

  // 截图功能
  SCREENSHOT: 'SCREENSHOT',

  // 消息撤回
  MESSAGE_RECALL: 'MESSAGE_RECALL'
}

/**
 * 默认功能开关配置
 * 可以通过环境变量或后端配置覆盖
 */
export const defaultFeatureFlags = {
  [FeatureFlags.SCHEDULED_MESSAGE]: false, // 默认关闭，后端尚未实现
  [FeatureFlags.AI_SMART_REPLY]: true,
  [FeatureFlags.AI_EMOJI_REACTION]: true,
  [FeatureFlags.VOICE_CALL]: true,
  [FeatureFlags.VIDEO_CALL]: true,
  [FeatureFlags.SCREENSHOT]: true,
  [FeatureFlags.MESSAGE_RECALL]: true
}

/**
 * 检查功能是否启用
 * @param {string} featureName - 功能名称
 * @param {Object} customFlags - 自定义开关配置（可选）
 * @returns {boolean}
 */
export function isFeatureEnabled(featureName, customFlags = null) {
  const flags = customFlags || defaultFeatureFlags
  return !!flags[featureName]
}

/**
 * 从后端加载功能开关配置
 * @returns {Promise<Object>}
 */
export async function loadFeatureFlagsFromApi() {
  try {
    // 这里可以调用后端 API 获取功能开关配置
    // const res = await getFeatureFlags()
    // return res.data || defaultFeatureFlags
    return defaultFeatureFlags
  } catch (error) {
    console.warn('[FeatureFlags] 加载失败，使用默认配置:', error)
    return defaultFeatureFlags
  }
}
