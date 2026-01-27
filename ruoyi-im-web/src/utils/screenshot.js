/**
 * 截图工具
 * 使用 Screen Capture API 实现浏览器截图功能
 */

/**
 * 检查浏览器是否支持截图功能
 * @returns {boolean}
 */
export function isScreenshotSupported() {
  return !!(
    navigator.mediaDevices &&
    navigator.mediaDevices.getDisplayMedia
  )
}

/**
 * 捕获屏幕截图
 * @param {Object} options - 截图选项
 * @param {string} options.cursor - 是否包含鼠标光标 'always' | 'never' | 'motion'
 * @returns {Promise<string>} - 返回图片 dataURL
 */
export async function captureScreenshot(options = {}) {
  if (!isScreenshotSupported()) {
    throw new Error('您的浏览器不支持截图功能，请使用 Chrome 72+、Edge 79+ 或 Firefox 66+')
  }

  const {
    cursor = 'never'
  } = options

  try {
    // 请求屏幕共享
    const stream = await navigator.mediaDevices.getDisplayMedia({
      video: {
        cursor
      },
      audio: false
    })

    // 创建视频元素
    const video = document.createElement('video')
    video.srcObject = stream
    video.play()

    // 等待视频加载
    await new Promise((resolve) => {
      video.onloadedmetadata = () => {
        resolve()
      }
    })

    // 等待一帧确保有画面
    await new Promise((resolve) => setTimeout(resolve, 100))

    // 创建画布
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')

    canvas.width = video.videoWidth
    canvas.height = video.videoHeight

    // 绘制视频帧到画布
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height)

    // 停止视频流
    stream.getTracks().forEach(track => track.stop())

    // 转换为 dataURL
    const dataURL = canvas.toDataURL('image/png')

    return dataURL
  } catch (error) {
    if (error.name === 'NotAllowedError') {
      throw new Error('您取消了截图选择')
    } else if (error.name === 'NotSupportedError') {
      throw new Error('浏览器不支持屏幕捕获')
    } else {
      throw error
    }
  }
}

/**
 * 捕获指定区域的截图
 * @param {Object} area - 区域 {x, y, width, height}
 * @param {Object} options - 截图选项
 * @returns {Promise<string>}
 */
export async function captureAreaScreenshot(area, options = {}) {
  const fullScreenshot = await captureScreenshot(options)

  return new Promise((resolve, reject) => {
    const img = new Image()
    img.onload = () => {
      const canvas = document.createElement('canvas')
      const ctx = canvas.getContext('2d')

      canvas.width = area.width
      canvas.height = area.height

      ctx.drawImage(
        img,
        area.x,
        area.y,
        area.width,
        area.height,
        0,
        0,
        area.width,
        area.height
      )

      resolve(canvas.toDataURL('image/png'))
    }
    img.onerror = reject
    img.src = fullScreenshot
  })
}

/**
 * 下载截图到本地
 * @param {string} dataURL - 图片 dataURL
 * @param {string} filename - 文件名
 */
export function downloadScreenshot(dataURL, filename = 'screenshot.png') {
  const link = document.createElement('a')
  link.href = dataURL
  link.download = filename
  link.click()
}

/**
 * 从 dataURL 创建 Blob
 * @param {string} dataURL - 图片 dataURL
 * @returns {Blob}
 */
export function dataURLToBlob(dataURL) {
  const parts = dataURL.split(',')
  const mime = parts[0].match(/:(.*?);/)[1]
  const bstr = atob(parts[1])
  let n = bstr.length
  const u8arr = new Uint8Array(n)

  while (n--) {
    u8arr[n] = bstr.charCodeAt(n)
  }

  return new Blob([u8arr], { type: mime })
}
