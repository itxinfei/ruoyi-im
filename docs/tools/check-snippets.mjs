import fs from 'node:fs'
import path from 'node:path'
import YAML from 'yaml'

const docsRoot = path.resolve(process.cwd())
const skipDirs = new Set(['node_modules', 'dist', 'tools'])
const fencePattern = /```([a-zA-Z0-9_-]+)?\n([\s\S]*?)```/g
const errors = []

function walk(dir) {
  const entries = fs.readdirSync(dir, { withFileTypes: true })
  const files = []

  for (const entry of entries) {
    const fullPath = path.join(dir, entry.name)
    if (entry.isDirectory()) {
      if (!skipDirs.has(entry.name)) {
        files.push(...walk(fullPath))
      }
      continue
    }
    if (entry.isFile() && entry.name.endsWith('.md')) {
      files.push(fullPath)
    }
  }

  return files
}

function validateFence(filePath, lang, code, index) {
  if (!lang) {
    errors.push(`${filePath}: 代码块 #${index + 1} 缺少语言标识`)
    return
  }

  const trimmed = code.trim()
  if (!trimmed) {
    errors.push(`${filePath}: 代码块 #${index + 1} 内容为空`)
    return
  }

  if (lang === 'json') {
    try {
      JSON.parse(trimmed)
    } catch (error) {
      errors.push(`${filePath}: JSON 代码块 #${index + 1} 解析失败: ${error.message}`)
    }
  }

  if (lang === 'yaml' || lang === 'yml') {
    try {
      YAML.parse(trimmed)
    } catch (error) {
      errors.push(`${filePath}: YAML 代码块 #${index + 1} 解析失败: ${error.message}`)
    }
  }

  if (lang === 'mermaid') {
    const isSupported = /^(graph|flowchart|sequenceDiagram|classDiagram|stateDiagram|erDiagram|journey|gantt|mindmap|timeline)\b/m.test(trimmed)
    if (!isSupported) {
      errors.push(`${filePath}: Mermaid 代码块 #${index + 1} 未包含有效图类型`)
    }
  }
}

for (const filePath of walk(docsRoot)) {
  const content = fs.readFileSync(filePath, 'utf8')
  const blocks = [...content.matchAll(fencePattern)]
  blocks.forEach((block, index) => {
    const lang = (block[1] || '').trim()
    const code = (block[2] || '').trim()
    if (!lang && !code) return
    validateFence(path.relative(docsRoot, filePath), lang, code, index)
  })
}

if (errors.length > 0) {
  console.error(errors.join('\n'))
  process.exit(1)
}

console.log('代码示例检查通过')
