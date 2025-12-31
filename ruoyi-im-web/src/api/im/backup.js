import request from '@/utils/request'

// 获取备份列表
export function listBackups(query) {
  return request({
    url: '/im/backup/list',
    method: 'get',
    params: query,
  })
}

// 创建备份
export function createBackup(data) {
  return request({
    url: '/im/backup/create',
    method: 'post',
    data: data,
  })
}

// 下载备份
export function downloadBackup(backupId) {
  return request({
    url: '/im/backup/download/' + backupId,
    method: 'get',
    responseType: 'blob',
  })
}

// 删除备份
export function deleteBackup(backupId) {
  return request({
    url: '/im/backup/' + backupId,
    method: 'delete',
  })
}

// 恢复备份
export function restoreBackup(backupId) {
  return request({
    url: '/im/backup/restore/' + backupId,
    method: 'post',
  })
}

// 获取备份设置
export function getBackupSettings() {
  return request({
    url: '/im/backup/settings',
    method: 'get',
  })
}

// 更新备份设置
export function updateBackupSettings(data) {
  return request({
    url: '/im/backup/settings',
    method: 'put',
    data: data,
  })
}

// 获取备份状态
export function getBackupStatus() {
  return request({
    url: '/im/backup/status',
    method: 'get',
  })
}

// 取消备份任务
export function cancelBackup(backupId) {
  return request({
    url: '/im/backup/cancel/' + backupId,
    method: 'post',
  })
}
