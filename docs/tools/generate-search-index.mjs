import fs from 'node:fs'
import path from 'node:path'

const docsRoot = path.resolve(process.cwd())
const outputFile = path.join(docsRoot, 'search-index.json')
const skipDirs = new Set(['node_modules', 'dist', 'tools'])

function walk(dir) {
  const entries = fs.readdirSync(dir, { withFileTypes: true })
  const files = []

  for (const entry of entries) {
    if (entry.name.startsWith('.')) {
      if (entry.name !== '.markdownlint-cli2.jsonc') continue
    }

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

function parseHeadings(content) {
  return content
    .split(/\r?\n/)
    .filter(line => /^#{1,6}\s+/.test(line))
    .map(line => line.replace(/^#{1,6}\s+/, '').trim())
}

function parseSummary(content) {
  const lines = content
    .split(/\r?\n/)
    .map(line => line.trim())
    .filter(Boolean)
    .filter(line => !line.startsWith('#') && !line.startsWith('>') && !line.startsWith('|') && !line.startsWith('```'))

  return lines.slice(0, 3).join(' ').slice(0, 180)
}

const searchIndex = walk(docsRoot).map((filePath) => {
  const content = fs.readFileSync(filePath, 'utf8')
  const headings = parseHeadings(content)
  const title = headings[0] || path.basename(filePath, '.md')
  const relativePath = path.relative(docsRoot, filePath).replace(/\\/g, '/')

  return {
    title,
    path: relativePath,
    category: relativePath.includes('/') ? relativePath.split('/')[0] : 'root',
    headings: headings.slice(1, 12),
    summary: parseSummary(content),
    updatedAt: new Date(fs.statSync(filePath).mtimeMs).toISOString()
  }
})

fs.writeFileSync(outputFile, JSON.stringify(searchIndex, null, 2) + '\n', 'utf8')
console.log(`已生成搜索索引: ${outputFile}`)
