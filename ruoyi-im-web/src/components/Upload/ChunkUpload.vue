<template>
  <div class="chunk-upload">
    <div v-if="file" class="upload-info">
      <div class="file-info">
        <i :class="getFileIcon(file.name)"></i>
        <span class="file-name">{{ file.name }}</span>
        <span class="file-size">({{ formatFileSize(file.size) }})</span>
      </div>
      <el-progress v-if="uploading" :percentage="progress" :status="uploadStatus"></el-progress>
      <div class="upload-actions">
        <template v-if="!uploading">
          <el-button type="primary" size="small" @click="startUpload"> 开始上传 </el-button>
          <el-button size="small" @click="cancelUpload">取消</el-button>
        </template>
        <template v-else>
          <el-button v-if="uploadStatus !== 'success'" size="small" @click="pauseUpload">
            {{ isPaused ? '继续' : '暂停' }}
          </el-button>
          <el-button size="small" @click="cancelUpload">取消</el-button>
        </template>
      </div>
    </div>

    <div
      v-else
      class="upload-drop"
      @drop.prevent="handleDrop"
      @dragover.prevent
      @click="triggerFileInput"
    >
      <input ref="fileInput" type="file" style="display: none" @change="handleFileSelect" />
      <i class="el-icon-upload"></i>
      <div class="upload-text">将文件拖到此处，或<em>点击上传</em></div>
      <div class="upload-tip">支持大文件上传，单个文件最大{{ maxFileSize }}MB</div>
    </div>
  </div>
</template>

<script>
import SparkMD5 from 'spark-md5'

export default {
  name: 'ChunkUpload',
  props: {
    // 文件大小限制（MB）
    maxFileSize: {
      type: Number,
      default: 1024,
    },
    // 分片大小（MB）
    chunkSize: {
      type: Number,
      default: 5,
    },
  },
  emits: ['success', 'cancel'],
  data() {
    return {
      file: null,
      fileHash: '',
      chunks: [],
      uploadedChunks: [],
      uploading: false,
      isPaused: false,
      progress: 0,
      uploadStatus: '',
      worker: null,
    }
  },
  methods: {
    // 触发文件选择
    triggerFileInput() {
      this.$refs.fileInput.click()
    },

    // 处理文件选择
    handleFileSelect(e) {
      const file = e.target.files[0]
      if (file) {
        this.handleFile(file)
      }
      e.target.value = ''
    },

    // 处理拖放文件
    handleDrop(e) {
      const file = e.dataTransfer.files[0]
      if (file) {
        this.handleFile(file)
      }
    },

    // 处理文件
    async handleFile(file) {
      // 检查文件大小
      if (file.size > this.maxFileSize * 1024 * 1024) {
        this.$message.error(`文件大小不能超过${this.maxFileSize}MB`)
        return
      }

      this.file = file
      // 计算文件hash
      await this.calculateHash()
      // 检查是否存在已上传的分片
      await this.checkUploadedChunks()
      // 创建分片
      this.createChunks()
    },

    // 计算文件hash
    calculateHash() {
      return new Promise(resolve => {
        const spark = new SparkMD5.ArrayBuffer()
        const reader = new FileReader()
        const file = this.file
        const size = file.size
        const offset = 2 * 1024 * 1024 // 取前2MB计算hash

        // 读取完成后计算hash
        reader.onload = e => {
          spark.append(e.target.result)
          this.fileHash = spark.end()
          resolve()
        }

        // 读取文件片段
        const slice = file.slice(0, Math.min(offset, size))
        reader.readAsArrayBuffer(slice)
      })
    },

    // 检查已上传的分片
    async checkUploadedChunks() {
      try {
        const res = await this.$http.get(`/im/file/upload/chunks/${this.fileHash}`)
        this.uploadedChunks = res.data || []
      } catch (error) {
        console.error('Failed to check uploaded chunks:', error)
        this.uploadedChunks = []
      }
    },

    // 创建分片
    createChunks() {
      const file = this.file
      const size = file.size
      const chunkSize = this.chunkSize * 1024 * 1024
      const chunks = []
      let start = 0

      while (start < size) {
        const end = Math.min(start + chunkSize, size)
        const chunk = file.slice(start, end)
        chunks.push({
          index: chunks.length,
          file: chunk,
          size: chunk.size,
          uploaded: this.uploadedChunks.includes(chunks.length),
        })
        start = end
      }

      this.chunks = chunks
    },

    // 开始上传
    async startUpload() {
      if (this.uploading) return

      this.uploading = true
      this.uploadStatus = 'active'

      try {
        // 上传所有分片
        await this.uploadChunks()

        // 合并分片
        await this.mergeChunks()

        this.uploadStatus = 'success'
        this.progress = 100

        // 触发上传完成事件
        this.$emit('success', {
          hash: this.fileHash,
          name: this.file.name,
          size: this.file.size,
        })
      } catch (error) {
        console.error('Upload failed:', error)
        this.uploadStatus = 'exception'
        this.$message.error('上传失败')
      }

      this.uploading = false
    },

    // 上传分片
    async uploadChunks() {
      const requests = this.chunks
        .filter(chunk => !chunk.uploaded)
        .map(chunk => {
          const formData = new FormData()
          formData.append('file', chunk.file)
          formData.append('hash', this.fileHash)
          formData.append('index', chunk.index)
          formData.append('size', chunk.size)

          return this.$http.post('/im/file/upload/chunk', formData, {
            onUploadProgress: e => {
              chunk.progress = Math.round((e.loaded / e.total) * 100)
              this.updateTotalProgress()
            },
          })
        })

      await Promise.all(requests)
    },

    // 合并分片
    async mergeChunks() {
      await this.$http.post('/im/file/upload/merge', {
        hash: this.fileHash,
        filename: this.file.name,
        size: this.file.size,
        chunks: this.chunks.length,
      })
    },

    // 更新总进度
    updateTotalProgress() {
      const uploaded = this.chunks.reduce((acc, chunk) => {
        return acc + (chunk.uploaded ? chunk.size : ((chunk.progress || 0) * chunk.size) / 100)
      }, 0)

      this.progress = Math.round((uploaded / this.file.size) * 100)
    },

    // 暂停上传
    pauseUpload() {
      if (this.isPaused) {
        this.startUpload()
      } else {
        this.isPaused = true
        // TODO: 取消正在进行的请求
      }
    },

    // 取消上传
    cancelUpload() {
      this.file = null
      this.fileHash = ''
      this.chunks = []
      this.uploadedChunks = []
      this.uploading = false
      this.isPaused = false
      this.progress = 0
      this.uploadStatus = ''

      this.$emit('cancel')
    },

    // 获取文件图标
    getFileIcon(fileName) {
      const extension = fileName.split('.').pop().toLowerCase()
      const iconMap = {
        pdf: 'el-icon-document',
        doc: 'el-icon-document-checked',
        docx: 'el-icon-document-checked',
        xls: 'el-icon-document',
        xlsx: 'el-icon-document',
        txt: 'el-icon-document',
        // 可以根据需要添加更多文件类型
      }
      return iconMap[extension] || 'el-icon-document'
    },

    // 格式化文件大小
    formatFileSize(size) {
      if (size < 1024) {
        return size + 'B'
      } else if (size < 1024 * 1024) {
        return (size / 1024).toFixed(1) + 'KB'
      } else if (size < 1024 * 1024 * 1024) {
        return (size / (1024 * 1024)).toFixed(1) + 'MB'
      } else {
        return (size / (1024 * 1024 * 1024)).toFixed(1) + 'GB'
      }
    },
  },
}
</script>

<style lang="scss" scoped>
.chunk-upload {
  .upload-drop {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 180px;
    border: 2px dashed #dcdfe6;
    border-radius: 6px;
    cursor: pointer;
    transition: border-color 0.3s;

    &:hover {
      border-color: #409eff;
    }

    i {
      font-size: 48px;
      color: #c0c4cc;
      margin-bottom: 16px;
    }

    .upload-text {
      color: #606266;
      margin-bottom: 8px;

      em {
        color: #409eff;
        font-style: normal;
      }
    }

    .upload-tip {
      font-size: 12px;
      color: #909399;
    }
  }

  .upload-info {
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    padding: 12px;

    .file-info {
      display: flex;
      align-items: center;
      margin-bottom: 12px;

      i {
        font-size: 24px;
        margin-right: 8px;
        color: #909399;
      }

      .file-name {
        flex: 1;
        margin-right: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .file-size {
        color: #909399;
      }
    }

    .upload-actions {
      display: flex;
      justify-content: flex-end;
      margin-top: 12px;

      .el-button {
        margin-left: 8px;
      }
    }
  }
}
</style>
