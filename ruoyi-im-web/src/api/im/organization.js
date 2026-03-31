/**
 * 组织架构相关 API
 */
import request from '../request'

/**
 * 获取组织架构树
 * @returns {Promise}
 */
export function getOrgTree() {
  return request({
    url: '/api/im/organization/department/tree',
    method: 'get'
  })
}

/**
 * 获取部门详情
 * @param {number} deptId - 部门ID
 * @returns {Promise}
 */
export function getDepartment(deptId) {
  return request({
    url: `/api/im/organization/department/${deptId}`,
    method: 'get'
  })
}

/**
 * 获取部门成员
 * @param {number} deptId - 部门ID
 * @returns {Promise}
 */
export function getDepartmentMembers(deptId) {
  return request({
    url: `/api/im/organization/department/${deptId}/members`,
    method: 'get'
  })
}

/**
 * 创建部门
 * @param {Object} data - 部门数据
 * @param {string} data.name - 部门名称
 * @param {number} data.parentId - 父部门ID
 * @param {string} data.description - 部门描述
 * @returns {Promise}
 */
export function createDepartment(data) {
  return request({
    url: '/api/im/organization/department',
    method: 'post',
    data
  })
}

/**
 * 更新部门
 * @param {Object} data - 部门数据
 * @param {number} data.departmentId - 部门ID
 * @param {string} data.name - 部门名称
 * @param {string} data.description - 部门描述
 * @returns {Promise}
 */
export function updateDepartment(data) {
  return request({
    url: '/api/im/organization/department',
    method: 'put',
    data
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
    method: 'delete'
  })
}

/**
 * 添加部门成员
 * @param {Object} data - 成员数据
 * @param {number} data.departmentId - 部门ID
 * @param {number} data.userId - 用户ID
 * @param {string} data.role - 角色
 * @returns {Promise}
 */
export function addDepartmentMember(data) {
  return request({
    url: '/api/im/organization/department/member',
    method: 'post',
    data
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
    method: 'delete'
  })
}

/**
 * 设置用户主部门
 * @param {number} userId - 用户ID
 * @param {number} departmentId - 部门ID
 * @returns {Promise}
 */
export function setPrimaryDepartment(userId, departmentId) {
  return request({
    url: `/api/im/organization/user/${userId}/primary-department/${departmentId}`,
    method: 'put'
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
    method: 'get'
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
    method: 'get'
  })
}
