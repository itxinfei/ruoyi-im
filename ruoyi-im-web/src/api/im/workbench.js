import request from '@/utils/request'

// 获取工作台数据概览
export function getWorkbenchOverview() {
  return request({
    url: '/api/im/workbench/overview',
    method: 'get',
  })
}

// 获取待办列表
export function getTodos() {
  return request({
    url: '/api/im/workbench/todos',
    method: 'get',
  })
}

// 创建待办
export function createTodo(data) {
  return request({
    url: '/api/im/workbench/todos',
    method: 'post',
    data: data,
  })
}

// 完成待办
export function completeTodo(id) {
  return request({
    url: `/api/im/workbench/todos/${id}/complete`,
    method: 'put',
  })
}

// 删除待办
export function deleteTodo(id) {
  return request({
    url: `/api/im/workbench/todos/${id}`,
    method: 'delete',
  })
}

// 更新待办
export function updateTodo(id, data) {
  return request({
    url: `/api/im/workbench/todos/${id}`,
    method: 'put',
    data: data,
  })
}
