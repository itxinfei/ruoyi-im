/**
 * 消息类型常量
 */
export const MESSAGE_TYPES = {
  TEXT: 'TEXT',
  IMAGE: 'IMAGE',
  FILE: 'FILE',
  VOICE: 'VOICE',
  VIDEO: 'VIDEO',
  LOCATION: 'LOCATION',
  CARD: 'CARD',
  COMBINE: 'COMBINE',
  SYSTEM: 'SYSTEM'
}

/**
 * 系统消息子类型
 */
export const SYSTEM_MESSAGE_TYPES = {
  FRIEND_REQUEST: 'FRIEND_REQUEST',
  FRIEND_ACCEPT: 'FRIEND_ACCEPT',
  GROUP_CREATE: 'GROUP_CREATE',
  GROUP_JOIN: 'GROUP_JOIN',
  GROUP_LEAVE: 'GROUP_LEAVE',
  GROUP_DISMISS: 'GROUP_DISMISS',
  GROUP_ANNOUNCEMENT: 'GROUP_ANNOUNCEMENT',
  APPROVAL: 'APPROVAL',
  SCHEDULE_REMIND: 'SCHEDULE_REMIND',
  TODO_REMIND: 'TODO_REMIND'
}

/**
 * 消息状态
 */
export const MESSAGE_STATUS = {
  SENDING: 'SENDING',
  SENT: 'SENT',
  FAILED: 'FAILED',
  DELIVERED: 'DELIVERED',
  READ: 'READ'
}

/**
 * 消息标记类型
 */
export const MARKER_TYPES = {
  FLAG: 'FLAG',
  IMPORTANT: 'IMPORTANT',
  TODO: 'TODO'
}

/**
 * 消息限制
 */
export const MESSAGE_LIMITS = {
  TEXT_MAX_LENGTH: 5000,
  IMAGE_MAX_SIZE: 20 * 1024 * 1024,
  IMAGE_FORMATS: ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp'],
  FILE_MAX_SIZE: 100 * 1024 * 1024,
  VOICE_MAX_SIZE: 50 * 1024 * 1024,
  VOICE_MAX_DURATION: 5 * 60,
  VIDEO_MAX_SIZE: 200 * 1024 * 1024,
  VIDEO_MAX_DURATION: 10 * 60,
  COMBINE_MAX_COUNT: 50,
  RECALL_TIME_LIMIT: 5 * 60 * 1000,
  EDIT_TIME_LIMIT: 5 * 60 * 1000
}

/**
 * 文件类型图标映射
 */
export const FILE_TYPE_ICONS = {
  image: 'image',
  video: 'video_file',
  audio: 'audio_file',
  pdf: 'picture_as_pdf',
  word: 'description',
  excel: 'table_chart',
  ppt: 'slideshow',
  zip: 'folder_zip',
  code: 'code',
  text: 'text_snippet',
  default: 'insert_drive_file'
}

/**
 * 根据文件扩展名获取图标
 */
export function getFileIcon(fileName) {
  const ext = fileName?.split('.').pop()?.toLowerCase() || ''
  const iconMap = {
    jpg: 'image', jpeg: 'image', png: 'image', gif: 'image', bmp: 'image', svg: 'image', webp: 'image',
    mp4: 'video_file', avi: 'video_file', mkv: 'video_file', mov: 'video_file', wmv: 'video_file',
    mp3: 'audio_file', wav: 'audio_file', flac: 'audio_file', aac: 'audio_file',
    pdf: 'picture_as_pdf',
    doc: 'description', docx: 'description',
    xls: 'table_chart', xlsx: 'table_chart',
    ppt: 'slideshow', pptx: 'slideshow',
    zip: 'folder_zip', rar: 'folder_zip', '7z': 'folder_zip',
    js: 'code', ts: 'code', py: 'code', java: 'code', go: 'code', rs: 'code',
    txt: 'text_snippet', md: 'text_snippet', json: 'text_snippet'
  }
  return iconMap[ext] || 'insert_drive_file'
}

/**
 * 根据文件扩展名获取文件类型
 */
export function getFileCategory(fileName) {
  const ext = fileName?.split('.').pop()?.toLowerCase() || ''
  const categoryMap = {
    image: ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'svg', 'webp'],
    video: ['mp4', 'avi', 'mkv', 'mov', 'wmv', 'flv'],
    audio: ['mp3', 'wav', 'flac', 'aac', 'ogg', 'm4a'],
    pdf: ['pdf'],
    word: ['doc', 'docx'],
    excel: ['xls', 'xlsx'],
    ppt: ['ppt', 'pptx'],
    zip: ['zip', 'rar', '7z', 'tar', 'gz'],
    code: ['js', 'ts', 'py', 'java', 'go', 'rs', 'c', 'cpp', 'h', 'vue', 'jsx', 'tsx'],
    text: ['txt', 'md', 'json', 'xml', 'yaml', 'yml', 'ini', 'conf']
  }
  
  for (const [category, extensions] of Object.entries(categoryMap)) {
    if (extensions.includes(ext)) {
      return category
    }
  }
  return 'other'
}