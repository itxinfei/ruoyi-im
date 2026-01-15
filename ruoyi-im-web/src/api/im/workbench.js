import request from '@/utils/request'

// ==================== 工作台概览 ====================

// 获取工作台数据概览
export function getWorkbenchOverview() {
  return request({
    url: '/api/im/workbench/overview',
    method: 'get',
  })
}

// ==================== 待办事项 ====================

// 获取待办列表
export function getTodos() {
  return request({
    url: '/api/im/workbench/todos',
    method: 'get',
  })
}

// 创建待办（后端使用@RequestParam接收）
export function createTodo(data) {
  // 优先级映射：HIGH=3, NORMAL=2, LOW=1
  const priorityMap = { HIGH: 3, NORMAL: 2, LOW: 1 }
  const priorityValue = data.priority ? priorityMap[data.priority] : 2

  return request({
    url: '/api/im/workbench/todos',
    method: 'post',
    params: {
      title: data.title,
      description: data.description,
      type: data.type || 'TASK',
      relatedId: data.relatedId,
      priority: priorityValue,
    },
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

// ==================== 考勤打卡 ====================

// 上班打卡
export function checkIn(data) {
  return request({
    url: '/api/im/attendance/checkIn',
    method: 'post',
    params: data,
  })
}

// 下班打卡
export function checkOut(data) {
  return request({
    url: '/api/im/attendance/checkOut',
    method: 'post',
    params: data,
  })
}

// 获取今日打卡状态
export function getTodayAttendance() {
  return request({
    url: '/api/im/attendance/today',
    method: 'get',
  })
}

// 获取打卡记录列表
export function getAttendanceList(params) {
  return request({
    url: '/api/im/attendance/list',
    method: 'get',
    params,
  })
}

// 获取月度统计
export function getAttendanceStatistics(params) {
  return request({
    url: '/api/im/attendance/statistics',
    method: 'get',
    params,
  })
}

// 补卡申请
export function applySupplement(id, reason) {
  return request({
    url: `/api/im/attendance/${id}/supplement`,
    method: 'post',
    params: { reason },
  })
}

// ==================== 审批流程 ====================

// 获取审批列表
export function getApprovalList(params) {
  return request({
    url: '/api/im/approval/list',
    method: 'get',
    params,
  })
}

// 创建审批
export function createApproval(data) {
  return request({
    url: '/api/im/approval/create',
    method: 'post',
    data,
  })
}

// 审批操作
export function approveApproval(id, data) {
  return request({
    url: `/api/im/approval/${id}/approve`,
    method: 'post',
    data,
  })
}

// 获取审批详情
export function getApprovalDetail(id) {
  return request({
    url: `/api/im/approval/${id}`,
    method: 'get',
  })
}

// ==================== 日程管理 ====================

// 获取日程列表
export function getScheduleList(params) {
  return request({
    url: '/api/im/schedule/list',
    method: 'get',
    params,
  })
}

// 创建日程
export function createSchedule(data) {
  return request({
    url: '/api/im/schedule/create',
    method: 'post',
    data,
  })
}

// 更新日程
export function updateSchedule(id, data) {
  return request({
    url: `/api/im/schedule/${id}`,
    method: 'put',
    data,
  })
}

// 删除日程
export function deleteSchedule(id) {
  return request({
    url: `/api/im/schedule/${id}`,
    method: 'delete',
  })
}

// ==================== 工作报告 ====================

// 获取报告列表
export function getReportList(params) {
  return request({
    url: '/api/im/report/list',
    method: 'get',
    params,
  })
}

// 创建报告
export function createReport(data) {
  return request({
    url: '/api/im/report/create',
    method: 'post',
    data,
  })
}

// 提交报告
export function submitReport(id) {
  return request({
    url: `/api/im/report/${id}/submit`,
    method: 'post',
  })
}

// 获取报告详情
export function getReportDetail(id) {
  return request({
    url: `/api/im/report/${id}`,
    method: 'get',
  })
}

// 点赞/取消点赞报告
export function toggleReportLike(id) {
  return request({
    url: `/api/im/report/${id}/like`,
    method: 'post',
  })
}

// 获取报告评论
export function getReportComments(id) {
  return request({
    url: `/api/im/report/${id}/comments`,
    method: 'get',
  })
}

// 发送报告评论
export function sendReportComment(id, data) {
  return request({
    url: `/api/im/report/${id}/comments`,
    method: 'post',
    data,
  })
}
