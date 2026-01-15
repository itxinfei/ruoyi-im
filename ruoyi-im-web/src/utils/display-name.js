/**
 * 显示名称工具函数
 * 钉钉7.6规范：备注 > 群昵称 > 原昵称
 */

/**
 * 获取用户显示名称
 * @param {Object} options - 选项
 * @param {string} options.userId - 用户ID
 * @param {string} options.conversationId - 会话ID（用于判断是否为群聊）
 * @param {string} options.conversationType - 会话类型：PRIVATE/GROUP
 * @param {string} options.remark - 备注名称
 * @param {string} options.groupNickname - 群昵称
 * @param {string} options.originalName - 原始昵称
 * @param {Object} options.store - Vuex store实例
 * @returns {string} 显示名称
 */
export function getUserDisplayName(options) {
  const {
    remark,
    groupNickname,
    originalName,
    conversationType
  } = options

  // 优先级1：备注（最高优先级）
  if (remark) {
    return remark
  }

  // 优先级2：群昵称（仅在群聊中生效）
  if (conversationType === 'GROUP' && groupNickname) {
    return groupNickname
  }

  // 优先级3：原昵称（默认）
  return originalName || '未知用户'
}

/**
 * 从Vuex store获取用户显示名称
 * @param {Object} store - Vuex store实例
 * @param {string} userId - 用户ID
 * @param {string} conversationId - 会话ID
 * @returns {string} 显示名称
 */
export function getUserDisplayNameFromStore(store, userId, conversationId) {
  if (!userId) return ''

  // 获取用户信息
  const user = store.getters['im/userById']?.(userId)
  if (!user) {
    return '未知用户'
  }

  // 获取会话信息
  const conversation = store.getters['im/conversationById']?.(conversationId)
  const isGroup = conversation?.type === 'GROUP'

  // 获取好友关系
  const friend = store.getters['im/friendById']?.(userId)

  // 获取群成员关系
  const groupMember = isGroup && conversationId
    ? store.getters['im/groupMemberByUser']?.(conversationId, userId)
    : null

  return getUserDisplayName({
    userId,
    conversationId,
    conversationType: conversation?.type,
    remark: friend?.remark,
    groupNickname: groupMember?.groupNickname,
    originalName: user.name || user.nickName || user.userName
  })
}

/**
 * 获取用户部门信息
 * @param {Object} store - Vuex store实例
 * @param {string} userId - 用户ID
 * @returns {Object} 部门信息 { id, name, path }
 */
export function getUserDepartment(store, userId) {
  const user = store.getters['im/userById']?.(userId)
  if (!user || !user.deptId) {
    return null
  }

  // 从组织架构中获取部门信息
  const department = store.getters['im/departmentById']?.(user.deptId)
  if (!department) {
    return null
  }

  return {
    id: department.id,
    name: department.deptName,
    path: department.ancestors || []
  }
}

/**
 * 格式化部门路径
 * @param {Array} path - 部门路径数组
 * @param {number} maxDepth - 最大显示深度
 * @returns {string} 部门路径字符串
 */
export function formatDepartmentPath(path, maxDepth = 2) {
  if (!path || path.length === 0) {
    return ''
  }

  const displayPath = path.slice(-maxDepth)
  return displayPath.map(dept => dept.name || dept.deptName).join(' / ')
}

/**
 * 获取用户部门显示文本
 * @param {Object} store - Vuex store实例
 * @param {string} userId - 用户ID
 * @param {Object} options - 选项
 * @param {boolean} options.showPath - 是否显示完整路径
 * @param {number} options.maxDepth - 最大显示深度
 * @returns {string} 部门显示文本
 */
export function getUserDepartmentText(store, userId, options = {}) {
  const { showPath = false, maxDepth = 2 } = options

  const dept = getUserDepartment(store, userId)
  if (!dept) {
    return ''
  }

  if (showPath && dept.path && dept.path.length > 0) {
    return formatDepartmentPath(dept.path, maxDepth)
  }

  return dept.name
}

// 默认导出所有函数
export default {
  getUserDisplayName,
  getUserDisplayNameFromStore,
  getUserDepartment,
  formatDepartmentPath,
  getUserDepartmentText
}
