import request from '@/utils/request'

// 获取会话列表
export function getSessionList(params) {
  return request({
    url: '/im/session/list',
    method: 'get',
    params,
  })
}

// 获取消息列表
export function getMessageList(params) {
  return request({
    url: '/im/message/list',
    method: 'get',
    params,
  })
}

// 发送消息
export function sendMessage(data) {
  return request({
    url: '/im/message/send',
    method: 'post',
    data,
  })
}

// 上传文件
export function uploadFile(data) {
  return request({
    url: '/im/file/upload',
    method: 'post',
    data,
  })
}

// 下载文件
export function downloadFile(params) {
  return request({
    url: '/im/file/download',
    method: 'get',
    params,
    responseType: 'blob',
  })
}
