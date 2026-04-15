import fs from 'node:fs'
import path from 'node:path'
import PDFDocument from 'pdfkit'

const docsRoot = path.resolve(process.cwd())
const distDir = path.join(docsRoot, 'dist')
const pdfFile = path.join(distDir, 'llm-docs-offline-bundle.pdf')

const bundleFiles = [
  'README.md',
  '56-大模型文档体系盘点与差距分析.md',
  '57-大模型生命周期开发手册.md',
  '58-API_SDK_CLI双语参考手册.md',
  '59-架构图时序图与数据流图.md',
  '60-版本兼容矩阵与升级指南.md',
  '61-端到端实战教程.md',
  '62-文档质量门禁与CI规范.md',
  '63-新成员快速上手手册.md'
]

fs.mkdirSync(distDir, { recursive: true })

const doc = new PDFDocument({
  margin: 40,
  size: 'A4',
  bufferPages: true
})

doc.pipe(fs.createWriteStream(pdfFile))
doc.font('Courier')

bundleFiles.forEach((fileName, index) => {
  const fullPath = path.join(docsRoot, fileName)
  if (!fs.existsSync(fullPath)) {
    return
  }

  const content = fs.readFileSync(fullPath, 'utf8')
  if (index > 0) {
    doc.addPage()
  }

  doc.fontSize(16).text(fileName, { underline: true })
  doc.moveDown(0.5)
  doc.fontSize(9).text(content.replace(/\t/g, '  '), {
    width: 515,
    lineGap: 2
  })
})

doc.end()
console.log(`已生成离线 PDF: ${pdfFile}`)
