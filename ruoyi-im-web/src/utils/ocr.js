/**
 * OCR 文字识别工具
 * 使用 Tesseract.js 进行客户端文字识别
 */

// Tesseract.js CDN URL
const TESSERACT_CDN = 'https://cdn.jsdelivr.net/npm/tesseract.js@5/dist/tesseract.min.js'

/**
 * OCR 识别器类
 */
export class OCREngine {
  constructor() {
    this.loaded = false
    this.loading = false
    this.Tesseract = null
  }

  /**
   * 加载 Tesseract.js
   */
  async load() {
    if (this.loaded) {return true}
    if (this.loading) {
      // 等待加载完成
      while (this.loading) {
        await new Promise(resolve => setTimeout(resolve, 100))
      }
      return this.loaded
    }

    this.loading = true

    return new Promise((resolve, reject) => {
      // 检查是否已加载
      if (window.Tesseract) {
        this.Tesseract = window.Tesseract
        this.loaded = true
        this.loading = false
        resolve(true)
        return
      }

      // 动态加载脚本
      const script = document.createElement('script')
      script.src = TESSERACT_CDN
      script.onload = () => {
        this.Tesseract = window.Tesseract
        this.loaded = true
        this.loading = false
        resolve(true)
      }
      script.onerror = () => {
        this.loading = false
        reject(new Error('OCR 库加载失败，请检查网络连接'))
      }
      document.head.appendChild(script)
    })
  }

  /**
   * 识别图片中的文字
   * @param {string} imageData - 图片 dataURL
   * @param {Object} options - 识别选项
   * @returns {Promise<Object>} - 识别结果
   */
  async recognize(imageData, options = {}) {
    await this.load()

    const {
      language = 'chi_sim+eng', // 中文简体+英文
      logger = () => {}
    } = options

    try {
      const result = await this.Tesseract.recognize(
        imageData,
        language,
        {
          logger: m => {
            if (m.status === 'recognizing text') {
              logger({
                status: 'processing',
                progress: Math.round(m.progress * 100)
              })
            }
          }
        }
      )

      return {
        success: true,
        text: result.data.text,
        confidence: result.data.confidence,
        words: result.data.words.map(w => ({
          text: w.text,
          confidence: w.confidence,
          bbox: w.bbox
        })),
        lines: result.data.lines.map(l => ({
          text: l.text,
          confidence: l.confidence,
          bbox: l.bbox
        }))
      }
    } catch (error) {
      return {
        success: false,
        error: error.message,
        text: ''
      }
    }
  }

  /**
   * 识别选中区域的文字
   * @param {HTMLCanvasElement} canvas - 包含图片的画布
   * @param {Object} region - 选区 {x, y, width, height}
   * @param {Object} options - 识别选项
   */
  async recognizeRegion(canvas, region, options = {}) {
    // 提取选区
    const tempCanvas = document.createElement('canvas')
    tempCanvas.width = region.width
    tempCanvas.height = region.height

    const ctx = tempCanvas.getContext('2d')
    ctx.drawImage(canvas, region.x, region.y, region.width, region.height, 0, 0, region.width, region.height)

    const imageData = tempCanvas.toDataURL('image/png')
    return this.recognize(imageData, options)
  }
}

// 创建单例实例
let ocrInstance = null

/**
 * 获取 OCR 引擎实例
 */
export function getOCREngine() {
  if (!ocrInstance) {
    ocrInstance = new OCREngine()
  }
  return ocrInstance
}

/**
 * 快速识别函数
 * @param {string} imageData - 图片 dataURL
 * @param {Function} onProgress - 进度回调
 */
export async function recognizeText(imageData, onProgress) {
  const engine = getOCREngine()
  return engine.recognize(imageData, { logger: onProgress })
}

/**
 * 预加载 OCR 库（在需要时提前加载）
 */
export function preloadOCR() {
  const engine = getOCREngine()
  return engine.load()
}
