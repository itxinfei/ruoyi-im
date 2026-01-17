/**
 * 组织架构模块API
 * @module api/im/organization
 */
import request from '@/utils/request'

/**
 * 获取部门树形结构
 * 获取完整的部门树，包含所有层级的部门和成员数量
 * @returns {Promise}
 */
export function getDepartmentTree() {
  return request({
    url: '/api/im/organization/department/tree',
    method: 'get',
  })
}

/**
 * 根据ID获取部门信息
 * @param {number} departmentId - 部门ID
 * @returns {Promise}
 */
export function getDepartmentById(departmentId) {
  return request({
    url: `/api/im/organization/department/${departmentId}`,
    method: 'get',
  })
}

/**
 * 创建部门
 * @param {Object} data - 部门数据
 * @param {string} data.name - 部门名称
 * @param {number} [data.parentId] - 父部门ID
 * @param {number} [data.leaderId] - 负责人ID
 * @param {string} [data.phone] - 联系电话
 * @param {string} [data.email] - 邮箱
 * @param {number} [data.orderNum] - 显示顺序
 * @param {string} [data.remark] - 部门描述
 * @returns {Promise}
 */
export function createDepartment(data) {
  return request({
    url: '/api/im/organization/department',
    method: 'post',
    data,
  })
}

/**
 * 更新部门信息
 * @param {Object} data - 更新数据
 * @param {number} data.id - 部门ID
 * @param {string} [data.name] - 部门名称
 * @param {number} [data.parentId] - 父部门ID
 * @param {number} [data.leaderId] - 负责人ID
 * @param {string} [data.phone] - 联系电话
 * @param {string} [data.email] - 邮箱
 * @param {number} [data.orderNum] - 显示顺序
 * @param {string} [data.remark] - 部门描述
 * @returns {Promise}
 */
export function updateDepartment(data) {
  return request({
    url: '/api/im/organization/department',
    method: 'put',
    data,
  })
}

/**
 * 删除部门
 * @param {number} departmentId - 部门ID
 * @returns {Promise}
 */
export function deleteDepartment(departmentId) {
  return request({
    url: `/api/im/organization/department/${departmentId}`,
    method: 'delete',
  })
}

/**
 * 获取部门成员列表
 * @param {number} departmentId - 部门ID
 * @returns {Promise}
 */
export function getDepartmentMembers(departmentId) {
  return request({
    url: `/api/im/organization/department/${departmentId}/members`,
    method: 'get',
  })
}

/**
 * 添加部门成员
 * @param {Object} data - 添加数据
 * @param {number} data.departmentId - 部门ID
 * @param {number} data.userId - 用户ID
 * @param {boolean} [data.isPrimary] - 是否主部门
 * @returns {Promise}
 */
export function addDepartmentMember(data) {
  return request({
    url: '/api/im/organization/department/member',
    method: 'post',
    data,
  })
}

/**
 * 移除部门成员
 * @param {number} departmentId - 部门ID
 * @param {number} userId - 用户ID
 * @returns {Promise}
 */
export function removeDepartmentMember(departmentId, userId) {
  return request({
    url: `/api/im/organization/department/${departmentId}/member/${userId}`,
    method: 'delete',
  })
}

/**
 * 设置用户主部门
 * @param {Object} data - 设置数据
 * @param {number} data.userId - 用户ID
 * @param {number} data.departmentId - 部门ID
 * @returns {Promise}
 */
export function setPrimaryDepartment(data) {
  return request({
    url: '/api/im/organization/user/primary-department',
    method: 'put',
    data,
  })
}

/**
 * 获取用户所属部门列表
 * @param {number} userId - 用户ID
 * @returns {Promise}
 */
export function getUserDepartments(userId) {
  return request({
    url: `/api/im/organization/user/${userId}/departments`,
    method: 'get',
  })
}

/**
 * 获取用户主部门
 * @param {number} userId - 用户ID
 * @returns {Promise}
 */
export function getUserPrimaryDepartment(userId) {
  return request({
    url: `/api/im/organization/user/${userId}/primary-department`,
    method: 'get',
  })
}

/**
 * 检查部门名称是否重复
 * @param {Object} params - 查询参数
 * @param {string} params.name - 部门名称
 * @param {number} [params.parentId] - 父部门ID
 * @param {number} [params.excludeId] - 排除的部门ID（用于更新时检查）
 * @returns {Promise}
 */
export function checkDepartmentNameExists(params) {
  return request({
    url: '/api/im/organization/department/check-name',
    method: 'get',
    params,
  })
}

/**
 * 获取部门及其所有子部门ID
 * @param {number} departmentId - 部门ID
 * @returns {Promise}
 */
export function getDepartmentAndChildrenIds(departmentId) {
  return request({
    url: `/api/im/organization/department/${departmentId}/children-ids`,
    method: 'get',
  })
}

// 默认导出 - 方便批量引入
export default {
  getDepartmentTree,
  getDepartmentById,
  createDepartment,
  updateDepartment,
  deleteDepartment,
  getDepartmentMembers,
  addDepartmentMember,
  removeDepartmentMember,
  setPrimaryDepartment,
  getUserDepartments,
  getUserPrimaryDepartment,
  checkDepartmentNameExists,
  getDepartmentAndChildrenIds,
}
