import request from '@/utils/request'

// 获取MFA状态
export function getMfaStatus() {
  return request({
    url: '/im/mfa/status',
    method: 'get',
  })
}

// 启用MFA
export function enableMfa(data) {
  return request({
    url: '/im/mfa/enable',
    method: 'post',
    data: data,
  })
}

// 禁用MFA
export function disableMfa(data) {
  return request({
    url: '/im/mfa/disable',
    method: 'post',
    data: data,
  })
}

// 验证MFA
export function verifyMfa(data) {
  return request({
    url: '/im/mfa/verify',
    method: 'post',
    data: data,
  })
}

// 生成MFA密钥
export function generateMfaSecret() {
  return request({
    url: '/im/mfa/secret',
    method: 'get',
  })
}

// 获取备用码
export function getBackupCodes() {
  return request({
    url: '/im/mfa/backup-codes',
    method: 'get',
  })
}

// 重新生成备用码
export function regenerateBackupCodes() {
  return request({
    url: '/im/mfa/backup-codes/regenerate',
    method: 'post',
  })
}

// 发送验证码（短信/邮件）
export function sendVerificationCode(data) {
  return request({
    url: '/im/mfa/send-code',
    method: 'post',
    data: data,
  })
}

// 获取MFA设置
export function getMfaSettings() {
  return request({
    url: '/im/mfa/settings',
    method: 'get',
  })
}

// 更新MFA设置
export function updateMfaSettings(data) {
  return request({
    url: '/im/mfa/settings',
    method: 'put',
    data: data,
  })
}
