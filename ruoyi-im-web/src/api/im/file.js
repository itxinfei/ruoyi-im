/**
 * 文件上传相关 API
 */
import request from '../request'

/**
 * 上传图片
 * @param {FormData} formData - 文件数据
 * @returns {Promise}
 */
export function uploadImage(formData) {
    return request({
        url: '/api/im/file/upload/image',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

/**
 * 上传文件
 * @param {FormData} formData - 文件数据
 * @returns {Promise}
 */
export function uploadFile(formData) {
    return request({
        url: '/api/im/file/upload',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

/**
 * 下载文件
 * @param {string} fileId - 文件ID
 * @returns {Promise}
 */
export function downloadFile(fileId) {
    return request({
        url: `/api/im/file/download/${fileId}`,
        method: 'get',
        responseType: 'blob'
    })
}

/**
 * 获取文件信息
 * @param {string} fileId - 文件ID
 * @returns {Promise}
 */
export function getFileInfo(fileId) {
    return request({
        url: `/api/im/file/${fileId}`,
        method: 'get'
    })
}

/**
 * 删除文件
 * @param {string} fileId - 文件ID
 * @returns {Promise}
 */
export function deleteFile(fileId) {
    return request({
        url: `/api/im/file/${fileId}`,
        method: 'delete'
    })
}
