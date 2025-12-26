export function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

export function getFileExtension(filename) {
  if (!filename) return ''
  const lastDotIndex = filename.lastIndexOf('.')
  if (lastDotIndex === -1) return ''
  return filename.slice(lastDotIndex + 1)
}

export function getFileName(filename) {
  if (!filename) return ''
  const lastSlashIndex = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'))
  if (lastSlashIndex === -1) return filename
  return filename.slice(lastSlashIndex + 1)
}

export function getFileTypeIcon(filename) {
  const extension = getFileExtension(filename).toLowerCase()
  const iconMap = {
    // 图片
    jpg: 'el-icon-picture',
    jpeg: 'el-icon-picture',
    png: 'el-icon-picture',
    gif: 'el-icon-picture',
    bmp: 'el-icon-picture',
    svg: 'el-icon-picture',
    webp: 'el-icon-picture',
    // 文档
    doc: 'el-icon-document',
    docx: 'el-icon-document',
    xls: 'el-icon-document',
    xlsx: 'el-icon-document',
    ppt: 'el-icon-document',
    pptx: 'el-icon-document',
    pdf: 'el-icon-document',
    txt: 'el-icon-document',
    rtf: 'el-icon-document',
    // 压缩包
    zip: 'el-icon-folder',
    rar: 'el-icon-folder',
    '7z': 'el-icon-folder',
    tar: 'el-icon-folder',
    gz: 'el-icon-folder',
    // 音频
    mp3: 'el-icon-headset',
    wav: 'el-icon-headset',
    flac: 'el-icon-headset',
    aac: 'el-icon-headset',
    ogg: 'el-icon-headset',
    // 视频
    mp4: 'el-icon-video-play',
    avi: 'el-icon-video-play',
    mov: 'el-icon-video-play',
    wmv: 'el-icon-video-play',
    flv: 'el-icon-video-play',
    mkv: 'el-icon-video-play',
    // 代码
    js: 'el-icon-document',
    jsx: 'el-icon-document',
    ts: 'el-icon-document',
    tsx: 'el-icon-document',
    vue: 'el-icon-document',
    json: 'el-icon-document',
    html: 'el-icon-document',
    css: 'el-icon-document',
    scss: 'el-icon-document',
    less: 'el-icon-document',
    py: 'el-icon-document',
    java: 'el-icon-document',
    php: 'el-icon-document',
    go: 'el-icon-document',
    rs: 'el-icon-document',
    // 其他
    default: 'el-icon-document',
  }
  return iconMap[extension] || iconMap.default
}

export function getFileTypeColor(filename) {
  const extension = getFileExtension(filename).toLowerCase()
  const colorMap = {
    // 图片
    jpg: '#1890ff',
    jpeg: '#1890ff',
    png: '#1890ff',
    gif: '#1890ff',
    bmp: '#1890ff',
    svg: '#1890ff',
    webp: '#1890ff',
    // 文档
    doc: '#1890ff',
    docx: '#1890ff',
    xls: '#52c41a',
    xlsx: '#52c41a',
    ppt: '#fa8c16',
    pptx: '#fa8c16',
    pdf: '#f5222d',
    txt: '#8c8c8c',
    rtf: '#8c8c8c',
    // 压缩包
    zip: '#fa8c16',
    rar: '#fa8c16',
    '7z': '#fa8c16',
    tar: '#fa8c16',
    gz: '#fa8c16',
    // 音频
    mp3: '#722ed1',
    wav: '#722ed1',
    flac: '#722ed1',
    aac: '#722ed1',
    ogg: '#722ed1',
    // 视频
    mp4: '#eb2f96',
    avi: '#eb2f96',
    mov: '#eb2f96',
    wmv: '#eb2f96',
    flv: '#eb2f96',
    mkv: '#eb2f96',
    // 代码
    js: '#fadb14',
    jsx: '#fadb14',
    ts: '#3178c6',
    tsx: '#3178c6',
    vue: '#42b883',
    json: '#f5a623',
    html: '#e34c26',
    css: '#563d7c',
    scss: '#c6538c',
    less: '#1d365d',
    py: '#3572a5',
    java: '#b07219',
    php: '#4f5d95',
    go: '#00add8',
    rs: '#dea584',
    // 其他
    default: '#8c8c8c',
  }
  return colorMap[extension] || colorMap.default
}

export function isImageFile(filename) {
  const ext = getFileExtension(filename).toLowerCase()
  const imageExts = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'svg', 'webp']
  return imageExts.includes(ext)
}

export function isVideoFile(filename) {
  const ext = getFileExtension(filename).toLowerCase()
  const videoExts = ['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv', 'webm']
  return videoExts.includes(ext)
}

export function isAudioFile(filename) {
  const ext = getFileExtension(filename).toLowerCase()
  const audioExts = ['mp3', 'wav', 'flac', 'aac', 'ogg', 'm4a']
  return audioExts.includes(ext)
}

export function isDocumentFile(filename) {
  const ext = getFileExtension(filename).toLowerCase()
  const docExts = ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'pdf', 'txt', 'rtf']
  return docExts.includes(ext)
}

export function isArchiveFile(filename) {
  const ext = getFileExtension(filename).toLowerCase()
  const archiveExts = ['zip', 'rar', '7z', 'tar', 'gz']
  return archiveExts.includes(ext)
}

export function isCodeFile(filename) {
  const ext = getFileExtension(filename).toLowerCase()
  const codeExts = ['js', 'jsx', 'ts', 'tsx', 'vue', 'json', 'html', 'css', 'scss', 'less', 'py', 'java', 'php', 'go', 'rs']
  return codeExts.includes(ext)
}
