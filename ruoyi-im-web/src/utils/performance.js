/**
 * 性能监控工具
 * 监控 Core Web Vitals 指标：LCP, INP, CLS
 */
import { onCLS, onINP, onLCP, onFCP, onTTFB } from 'web-vitals'

/**
 * 性能指标存储
 */
const metrics = {
  lcp: null, // Largest Contentful Paint (最大内容绘制)
  inp: null, // Interaction to Next Paint (交互到下一次绘制)
  cls: null, // Cumulative Layout Shift (累积布局偏移)
  fcp: null, // First Contentful Paint (首次内容绘制)
  ttfb: null // Time to First Byte (首字节时间)
}

/**
 * 上报性能数据到控制台
 */
function logMetric(metric) {
  const { name, value, rating } = metric
  console.log(`[Performance] ${name}:`, {
    value: `${value.toFixed(2)} ms`,
    rating: rating === 'good' ? '✅' : rating === 'needs-improvement' ? '⚠️' : '❌'
  })

  // 存储指标
  const key = name.toLowerCase().replace('-', '')
  if (key in metrics) {
    metrics[key] = metric
  }
}

/**
 * 上报性能数据到服务器（可选）
 */
function reportMetric(metric) {
  // TODO: 发送到分析平台
  // fetch('/api/analytics/performance', {
  //   method: 'POST',
  //   headers: { 'Content-Type': 'application/json' },
  //   body: JSON.stringify({
  //     metric: metric.name,
  //     value: metric.value,
  //     rating: metric.rating,
  //     url: window.location.href,
  //     userAgent: navigator.userAgent,
  //     timestamp: Date.now()
  //   })
  // }).catch(err => console.warn('[Performance] 上报失败:', err))
}

/**
 * 检查性能是否达标
 */
function checkPerformance() {
  const issues = []

  if (metrics.cls && metrics.cls.value > 0.25) {
    issues.push('CLS (累积布局偏移) 过大，页面稳定性需要优化')
  }

  if (metrics.lcp && metrics.lcp.value > 4000) {
    issues.push('LCP (最大内容绘制) 过慢，页面加载速度需要优化')
  }

  if (metrics.inp && metrics.inp.value > 300) {
    issues.push('INP (交互响应) 过长，交互响应需要优化')
  }

  if (issues.length > 0) {
    console.warn('[Performance] 发现性能问题:')
    issues.forEach(issue => console.warn(`  - ${issue}`))
  }

  return issues
}

/**
 * 获取所有性能指标
 */
export function getMetrics() {
  return { ...metrics }
}

/**
 * 初始化性能监控
 */
export function initPerformanceMonitoring() {
  // 仅在生产环境启用
  if (import.meta.env.DEV) {
    console.log('[Performance] 开发环境，性能监控已禁用')
    return
  }

  console.log('[Performance] 初始化性能监控...')

  // 监控 CLS (累积布局偏移)
  onCLS((metric) => {
    logMetric(metric)
    reportMetric(metric)
  })

  // 监控 INP (交互到下一次绘制)
  onINP((metric) => {
    logMetric(metric)
    reportMetric(metric)
  })

  // 监控 LCP (最大内容绘制)
  onLCP((metric) => {
    logMetric(metric)
    reportMetric(metric)

    // LCP 加载完成后检查性能
    setTimeout(() => {
      checkPerformance()
    }, 1000)
  })

  // 监控 FCP (首次内容绘制)
  onFCP((metric) => {
    logMetric(metric)
    reportMetric(metric)
  })

  // 监控 TTFB (首字节时间)
  onTTFB((metric) => {
    logMetric(metric)
    reportMetric(metric)
  })
}

/**
 * 手动触发性能检查（用于调试）
 */
export function checkPerformanceNow() {
  const allMetrics = getMetrics()
  console.table(
    Object.entries(allMetrics)
      .filter(([_, value]) => value !== null)
      .map(([name, value]) => ({
        指标: name.toUpperCase(),
        数值: value.value.toFixed(2) + ' ms',
        评级: value.rating === 'good' ? '优秀' : value.rating === 'needs-improvement' ? '需改进' : '差'
      }))
  )

  return checkPerformance()
}

/**
 * 导出性能数据为 JSON
 */
export function exportMetrics() {
  const data = {
    url: window.location.href,
    timestamp: Date.now(),
    userAgent: navigator.userAgent,
    metrics: getMetrics()
  }

  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `performance-${Date.now()}.json`
  a.click()
  URL.revokeObjectURL(url)

  console.log('[Performance] 性能数据已导出')
}

export default {
  initPerformanceMonitoring,
  getMetrics,
  checkPerformanceNow,
  exportMetrics
}
