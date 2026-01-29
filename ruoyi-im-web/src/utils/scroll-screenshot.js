/**
 * 滚动截图工具
 * 用于捕获超出屏幕高度的完整页面
 */

/**
 * 计算两个图像的重叠区域
 * @param {ImageData} img1 - 第一张图像数据
 * @param {ImageData} img2 - 第二张图像数据
 * @param {number} minOverlap - 最小重叠像素
 * @returns {number} - 重叠像素高度
 */
function calculateOverlap(img1, img2, minOverlap = 50) {
  const w = img1.width
  const h1 = img1.height
  const h2 = img2.height

  // 从底部向上搜索最佳匹配位置
  let maxMatch = 0
  let bestY = 0

  // 限制搜索范围在合理的重叠区域
  const searchStart = Math.max(minOverlap, h1 - h2)
  const searchEnd = h1 - minOverlap

  for (let y = searchStart; y >= searchEnd; y -= 5) {
    let matchCount = 0
    const totalCheck = Math.min(w, 500)

    // 水平采样比较
    for (let x = 0; x < totalCheck; x += 5) {
      const idx1 = ((y - 1) * w + x) * 4
      const idx2 = (h2 - (h1 - y + 1)) * w * 4 + x * 4

      if (idx1 >= 0 && idx1 < img1.data.length &&
          idx2 >= 0 && idx2 < img2.data.length) {
        const rDiff = Math.abs(img1.data[idx1] - img2.data[idx2])
        const gDiff = Math.abs(img1.data[idx1 + 1] - img2.data[idx2 + 1])
        const bDiff = Math.abs(img1.data[idx1 + 2] - img2.data[idx2 + 2])

        if (rDiff < 10 && gDiff < 10 && bDiff < 10) {
          matchCount++
        }
      }
    }

    const matchRatio = matchCount / (totalCheck / 5)
    if (matchRatio > maxMatch) {
      maxMatch = matchRatio
      bestY = y
    }

    // 如果找到足够好的匹配，提前结束
    if (maxMatch > 0.85) break
  }

  return maxMatch > 0.7 ? h1 - bestY : 0
}

/**
 * 拼接两张图像
 * @param {ImageData} img1 - 第一张图像
 * @param {ImageData} img2 - 第二张图像
 * @param {number} overlap - 重叠像素
 * @returns {ImageData} - 拼接后的图像
 */
function stitchImages(img1, img2, overlap) {
  const w = img1.width
  const h1 = img1.height
  const h2 = img2.height
  const newHeight = h1 + h2 - overlap

  const result = new ImageData(w, newHeight)

  // 复制第一张图
  result.data.set(img1.data)

  // 复制第二张图（非重叠部分）
  const startY = h1 - overlap
  const srcStartY = overlap

  for (let y = startY; y < newHeight && y < h1 + h2; y++) {
    for (let x = 0; x < w; x++) {
      const destIdx = (y * w + x) * 4
      const srcY = y - startY + srcStartY
      const srcIdx = (srcY * w + x) * 4

      if (srcY < h2 && destIdx < result.data.length && srcIdx < img2.data.length) {
        result.data[destIdx] = img2.data[srcIdx]
        result.data[destIdx + 1] = img2.data[srcIdx + 1]
        result.data[destIdx + 2] = img2.data[srcIdx + 2]
        result.data[destIdx + 3] = img2.data[srcIdx + 3]
      }
    }
  }

  return result
}

/**
 * 滚动截图管理器
 */
export class ScrollScreenshotManager {
  constructor() {
    this.canvas = null
    this.ctx = null
    this.screenshots = []
    this.isCapturing = false
  }

  /**
   * 开始滚动截图
   * @param {HTMLCanvasElement} initialCanvas - 初始截图画布
   */
  start(initialCanvas) {
    this.canvas = document.createElement('canvas')
    this.ctx = this.canvas.getContext('2d')

    // 保存第一张截图
    const imageData = initialCanvas.getContext('2d').getImageData(
      0, 0, initialCanvas.width, initialCanvas.height
    )
    this.screenshots = [imageData]

    this.canvas.width = initialCanvas.width
    this.canvas.height = initialCanvas.height
    this.ctx.putImageData(imageData, 0, 0)

    this.isCapturing = true
  }

  /**
   * 添加新的截图并自动拼接
   * @param {HTMLCanvasElement} newCanvas - 新截图画布
   * @returns {boolean} - 是否成功拼接
   */
  addScreenshot(newCanvas) {
    if (!this.isCapturing) return false

    const newImageData = newCanvas.getContext('2d').getImageData(
      0, 0, newCanvas.width, newCanvas.height
    )

    // 获取当前拼接后的图像
    const currentImageData = this.ctx.getImageData(
      0, 0, this.canvas.width, this.canvas.height
    )

    // 计算重叠
    const overlap = calculateOverlap(currentImageData, newImageData)

    if (overlap > 0) {
      // 有重叠，拼接
      const stitched = stitchImages(currentImageData, newImageData, overlap)

      this.canvas.height = stitched.height
      this.ctx.putImageData(stitched, 0, 0)

      this.screenshots.push(newImageData)
      return true
    } else {
      // 无重叠，可能已经到底了
      this.screenshots.push(newImageData)
      return false
    }
  }

  /**
   * 完成截图
   * @returns {string} - dataURL
   */
  finish() {
    this.isCapturing = false
    return this.canvas.toDataURL('image/png')
  }

  /**
   * 取消截图
   */
  cancel() {
    this.isCapturing = false
    this.screenshots = []
    this.canvas = null
    this.ctx = null
  }

  /**
   * 获取当前状态
   */
  getStatus() {
    return {
      isCapturing: this.isCapturing,
      screenshotCount: this.screenshots.length,
      currentHeight: this.canvas?.height || 0,
      currentWidth: this.canvas?.width || 0
    }
  }
}

/**
 * 简化版滚动截图 - 手动模式
 * 用户手动滚动并多次截图，系统自动拼接
 */
export function createManualScrollStitcher() {
  const images = []

  return {
    /**
     * 添加截图
     */
    addImage(dataUrl) {
      return new Promise((resolve) => {
        const img = new Image()
        img.onload = () => {
          images.push(img)
          resolve(images.length)
        }
        img.src = dataUrl
      })
    },

    /**
     * 拼接所有截图
     */
    async stitch() {
      if (images.length === 0) return null

      // 创建输出画布
      const canvas = document.createElement('canvas')
      const ctx = canvas.getContext('2d')

      canvas.width = images[0].width

      // 先放置第一张图
      let currentY = 0
      let totalHeight = images[0].height

      // 估算总高度（假设每张图有重叠）
      for (let i = 1; i < images.length; i++) {
        totalHeight += images[i].height - 100 // 假设100px重叠
      }

      canvas.height = totalHeight
      ctx.drawImage(images[0], 0, 0)
      currentY = images[0].height

      // 逐张拼接
      for (let i = 1; i < images.length; i++) {
        const prevImg = images[i - 1]
        const currImg = images[i]

        // 简单拼接：将新图放在上一张图下方，留一些重叠
        const overlap = Math.min(100, prevImg.height / 2)
        currentY = currentY - overlap

        ctx.drawImage(currImg, 0, currentY)
        currentY += currImg.height
      }

      return canvas.toDataURL('image/png')
    },

    /**
     * 重置
     */
    reset() {
      images.length = 0
    },

    /**
     * 获取图片数量
     */
    get count() {
      return images.length
    }
  }
}
