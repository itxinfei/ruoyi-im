/**
 * 邮件附件配置
 * 与后端 EmailAttachmentConfig 保持一致
 */

// 附件配置对象
export const emailAttachmentConfig = {
  // 最大文件大小（100MB，与后端保持一致）
  maxSize: 100 * 1024 * 1024,
  maxSizeMB: 100,

  // 允许的文件扩展名
  allowedExtensions: [
    'pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx',
    'jpg', 'jpeg', 'png', 'gif', 'bmp',
    'zip', 'rar', '7z',
    'txt', 'csv'
  ],

  // 禁止的文件扩展名
  forbiddenExtensions: [
    'exe', 'bat', 'cmd', 'sh', 'com',
    'js', 'vbs', 'ps1', 'msi',
    'dll', 'sys', 'drv', 'jar'
  ],

  // 单次上传最大文件数
  maxFilesPerUpload: 10,

  // 文件类型描述（用于提示）
  acceptTypes: '.doc,.docx,.xls,.xlsx,.ppt,.pptx,.pdf,.jpg,.jpeg,.png,.gif,.bmp,.zip,.rar,.7z,.txt,.csv',

  // MIME 类型映射（用于文件类型验证）
  mimeTypeMap: {
    'pdf': 'application/pdf',
    'doc': 'application/msword',
    'docx': 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'xls': 'application/vnd.ms-excel',
    'xlsx': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'ppt': 'application/vnd.ms-powerpoint',
    'pptx': 'application/vnd.openxmlformats-officedocument.presentationml.presentation',
    'jpg': 'image/jpeg',
    'jpeg': 'image/jpeg',
    'png': 'image/png',
    'gif': 'image/gif',
    'bmp': 'image/bmp',
    'zip': 'application/zip',
    'rar': 'application/x-rar-compressed',
    '7z': 'application/x-7z-compressed',
    'txt': 'text/plain',
    'csv': 'text/csv'
  }
}

/**
 * 验证文件扩展名是否允许
 * @param {string} fileName - 文件名
 * @returns {boolean} 是否允许
 */
export function isExtensionAllowed(fileName) {
  if (!fileName) {return false}
  const ext = fileName.split('.').pop().toLowerCase()
  return emailAttachmentConfig.allowedExtensions.includes(ext) &&
         !emailAttachmentConfig.forbiddenExtensions.includes(ext)
}

/**
 * 验证文件大小是否允许
 * @param {number} size - 文件大小（字节）
 * @returns {boolean} 是否允许
 */
export function isSizeAllowed(size) {
  return size > 0 && size <= emailAttachmentConfig.maxSize
}

/**
 * 获取文件扩展名
 * @param {string} fileName - 文件名
 * @returns {string} 扩展名（不含点号）
 */
export function getFileExtension(fileName) {
  if (!fileName) {return ''}
  return fileName.split('.').pop().toLowerCase()
}

/**
 * 格式化文件大小
 * @param {number} bytes - 字节数
 * @returns {string} 格式化后的字符串
 */
export function formatFileSize(bytes) {
  if (bytes === 0) {return '0 B'}
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 验证文件
 * @param {File} file - 文件对象
 * @returns {{valid: boolean, message?: string}} 验证结果
 */
export function validateFile(file) {
  // 验证文件类型
  if (!isExtensionAllowed(file.name)) {
    return {
      valid: false,
      message: `不支持的文件类型！允许的文件类型：${emailAttachmentConfig.allowedExtensions.join(', ')}`
    }
  }

  // 验证文件大小
  if (!isSizeAllowed(file.size)) {
    return {
      valid: false,
      message: `文件大小不能超过 ${emailAttachmentConfig.maxSizeMB}MB！当前文件：${formatFileSize(file.size)}`
    }
  }

  return { valid: true }
}

/**
 * 批量验证文件
 * @param {File[]} files - 文件列表
 * @returns {{valid: boolean, validFiles: File[], errors: string[]}} 验证结果
 */
export function validateFiles(files) {
  const validFiles = []
  const errors = []

  // 检查文件数量
  if (files.length > emailAttachmentConfig.maxFilesPerUpload) {
    return {
      valid: false,
      validFiles: [],
      errors: [`一次最多只能上传 ${emailAttachmentConfig.maxFilesPerUpload} 个文件`]
    }
  }

  for (const file of files) {
    const result = validateFile(file)
    if (result.valid) {
      validFiles.push(file)
    } else {
      errors.push(`${file.name}: ${result.message}`)
    }
  }

  return {
    valid: errors.length === 0,
    validFiles,
    errors
  }
}
