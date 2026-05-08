import fs from 'node:fs'
import path from 'node:path'

const docsRoot = path.resolve(process.cwd())
const skipDirs = new Set(['node_modules', 'dist', 'tools'])
const linkPattern = /!?\[([^\]]*)\]\(([^)]+)\)/g
const headingPattern = /^#{1,6}\s+(.+)$/gm
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

function slugify(text) {
  return text
    .trim()
    .toLowerCase()
    .replace(/[^\w\u4e00-\u9fa5\s-]/g, '')
    .replace(/\s+/g, '-')
}

function extractAnchors(content) {
  const anchors = new Set()
  const matches = content.matchAll(headingPattern)
  for (const match of matches) {
    anchors.add(slugify(match[1]))
  }
  return anchors
}

for (const filePath of walk(docsRoot)) {
  const content = fs.readFileSync(filePath, 'utf8')
  const relativeFile = path.relative(docsRoot, filePath)
  const links = [...content.matchAll(linkPattern)]

  for (const link of links) {
    const target = (link[2] || '').trim()
    if (!target || target.startsWith('http://') || target.startsWith('https://') || target.startsWith('mailto:')) {
      continue
    }

    const [rawPath, rawAnchor] = target.split('#')
    const resolvedPath = rawPath
      ? path.resolve(path.dirname(filePath), rawPath)
      : filePath

    if (!fs.existsSync(resolvedPath)) {
      errors.push(`${relativeFile}: 链接目标不存在 -> ${target}`)
      continue
    }

    if (rawAnchor) {
      const anchorContent = fs.readFileSync(resolvedPath, 'utf8')
      const anchors = extractAnchors(anchorContent)
      if (!anchors.has(slugify(rawAnchor))) {
        errors.push(`${relativeFile}: 锚点不存在 -> ${target}`)
      }
    }
  }
}

if (errors.length > 0) {
  console.error(errors.join('\n'))
  process.exit(1)
}

console.log('链接检查通过')
