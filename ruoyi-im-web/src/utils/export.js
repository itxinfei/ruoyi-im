/**
 * 导出工具函数
 * 提供数据导出相关的通用功能
 */

/**
 * CSV 字段安全转义
 * 处理双引号、换行符等 CSV 特殊字符，防止格式错误和注入风险
 *
 * @param {*} value - 需要转义的值
 * @returns {String} 转义后的 CSV 字段值
 */
export function escapeCsvField(value) {
  if (value === null || value === undefined) {return '""'}
  const str = String(value)
  // 转义双引号为两个双引号，并移除换行符
  const escaped = str.replace(/"/g, '""').replace(/[\r\n]+/g, ' ')
  return `"${escaped}"`
}

/**
 * 导出数据为 CSV 文件
 *
 * @param {Array} headers - 表头数组
 * @param {Array} rows - 数据行数组，每行也是一个数组
 * @param {String} filename - 导出文件名（不含扩展名）
 * @param {Object} options - 配置选项
 * @param {Boolean} options.addBOM - 是否添加 BOM（支持中文），默认 true
 * @returns {void}
 *
 * @example
 * exportToCSV(
 *   ['姓名', '年龄', '邮箱'],
 *   [['张三', 25, 'zhang@example.com'], ['李四', 30, 'li@example.com']],
 *   '用户列表'
 * )
 */
export function exportToCSV(headers, rows, filename, options = {}) {
  const { addBOM = true } = options

  // 使用安全的字段转义
  const safeRows = rows.map(row => row.map(cell => escapeCsvField(cell)))
  const csvContent = (addBOM ? '\uFEFF' : '') +
    headers.map(h => escapeCsvField(h)).join(',') + '\n' +
    safeRows.map(row => row.join(',')).join('\n')

  // 创建 Blob 并下载
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${filename}_${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(url)
}

/**
 * 将对象数组转换为 CSV 格式
 *
 * @param {Array} data - 对象数组
 * @param {Array} columns - 列配置 [{ key: 'name', label: '姓名' }, ...]
 * @param {String} filename - 导出文件名（不含扩展名）
 * @returns {void}
 *
 * @example
 * exportObjectToCSV(
 *   [{ name: '张三', age: 25 }, { name: '李四', age: 30 }],
 *   [{ key: 'name', label: '姓名' }, { key: 'age', label: '年龄' }],
 *   '用户列表'
 * )
 */
export function exportObjectToCSV(data, columns, filename) {
  const headers = columns.map(col => col.label)
  const rows = data.map(item => columns.map(col => item[col.key]))
  exportToCSV(headers, rows, filename)
}

/**
 * 导出数据为 JSON 文件
 *
 * @param {Object|Array} data - 需要导出的数据
 * @param {String} filename - 导出文件名（不含扩展名）
 * @returns {void}
 */
export function exportToJSON(data, filename) {
  const jsonContent = JSON.stringify(data, null, 2)
  const blob = new Blob([jsonContent], { type: 'application/json;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${filename}_${new Date().toISOString().slice(0, 10)}.json`
  link.click()
  URL.revokeObjectURL(url)
}
