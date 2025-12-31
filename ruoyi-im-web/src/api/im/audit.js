import request from '@/utils/request'

// 获取审计日志列表
export function listAuditLogs(query) {
  return request({
    url: '/api/im/audit/list',
    method: 'get',
    params: query,
  })
}

// 获取审计日志详情
export function getAuditLog(logId) {
  return request({
    url: '/api/im/audit/' + logId,
    method: 'get',
  })
}

// 导出审计日志
export function exportAuditLogs(query) {
  return request({
    url: '/api/im/audit/export',
    method: 'get',
    params: query,
    responseType: 'blob',
  })
}

// 获取审计统计信息
export function getAuditStatistics(query) {
  return request({
    url: '/api/im/audit/statistics',
    method: 'get',
    params: query,
  })
}

// 获取敏感词列表
export function listSensitiveWords(query) {
  return request({
    url: '/api/im/audit/sensitive/list',
    method: 'get',
    params: query,
  })
}

// 添加敏感词
export function addSensitiveWord(data) {
  return request({
    url: '/api/im/audit/sensitive',
    method: 'post',
    data: data,
  })
}

// 删除敏感词
export function deleteSensitiveWord(wordId) {
  return request({
    url: '/api/im/audit/sensitive/' + wordId,
    method: 'delete',
  })
}

// 批量导入敏感词
export function importSensitiveWords(data) {
  return request({
    url: '/api/im/audit/sensitive/import',
    method: 'post',
    data: data,
  })
}

// 获取敏感事件列表
export function listSensitiveEvents(query) {
  return request({
    url: '/api/im/audit/events',
    method: 'get',
    params: query,
  })
}

// 处理敏感事件
export function handleSensitiveEvent(eventId, data) {
  return request({
    url: '/api/im/audit/events/' + eventId + '/handle',
    method: 'put',
    data: data,
  })
}
