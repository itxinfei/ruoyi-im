<template>
  <div class="file-preview">
    <!-- 图片预览 -->
    <div v-if="isImage" class="image-preview">
      <el-image
        :src="fileUrl"
        :preview-src-list="[fileUrl]"
        fit="contain"
        :style="{ width: width, height: height }"
      >
        <template #error>
          <div class="image-slot">
            <i class="el-icon-picture-outline"></i>
          </div>
        </template>
      </el-image>
    </div>

    <!-- PDF预览 -->
    <div v-else-if="isPdf" class="pdf-preview">
      <iframe :src="fileUrl" :style="{ width: width, height: height }" frameborder="0"></iframe>
    </div>

    <!-- 视频预览 -->
    <div v-else-if="isVideo" class="video-preview">
      <video
        :src="fileUrl"
        :style="{ width: width, height: height }"
        controls
        controlsList="nodownload"
      >
        您的浏览器不支持视频播放
      </video>
    </div>

    <!-- 音频预览 -->
    <div v-else-if="isAudio" class="audio-preview">
      <audio :src="fileUrl" controls controlsList="nodownload">您的浏览器不支持音频播放</audio>
    </div>

    <!-- 文本预览 -->
    <div v-else-if="isText" class="text-preview">
      <pre>{{ fileContent }}</pre>
    </div>

    <!-- 其他文件类型 -->
    <div v-else class="other-preview">
      <div class="file-info">
        <i :class="fileIcon"></i>
        <span class="file-name">{{ fileName }}</span>
      </div>
      <el-button type="primary" size="small" @click="downloadFile"> 下载文件 </el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FilePreview',
  props: {
    // 文件URL
    fileUrl: {
      type: String,
      required: true,
    },
    // 文件名
    fileName: {
      type: String,
      default: '',
    },
    // 文件类型
    fileType: {
      type: String,
      default: '',
    },
    // 预览区域宽度
    width: {
      type: String,
      default: '100%',
    },
    // 预览区域高度
    height: {
      type: String,
      default: '400px',
    },
  },
  data() {
    return {
      fileContent: '', // 文本文件内容
    }
  },
  computed: {
    // 是否为图片
    isImage() {
      return /\.(jpg|jpeg|png|gif|bmp|webp)$/i.test(this.fileName)
    },
    // 是否为PDF
    isPdf() {
      return /\.pdf$/i.test(this.fileName)
    },
    // 是否为视频
    isVideo() {
      return /\.(mp4|webm|ogg)$/i.test(this.fileName)
    },
    // 是否为音频
    isAudio() {
      return /\.(mp3|wav|ogg)$/i.test(this.fileName)
    },
    // 是否为文本
    isText() {
      return /\.(txt|json|xml|md|js|css|html|htm)$/i.test(this.fileName)
    },
    // 文件图标
    fileIcon() {
      const iconMap = {
        doc: 'el-icon-document-word',
        docx: 'el-icon-document-word',
        xls: 'el-icon-document-excel',
        xlsx: 'el-icon-document-excel',
        ppt: 'el-icon-document-ppt',
        pptx: 'el-icon-document-ppt',
        pdf: 'el-icon-document-pdf',
        zip: 'el-icon-document-zip',
        rar: 'el-icon-document-zip',
        '7z': 'el-icon-document-zip',
        txt: 'el-icon-document-text',
        default: 'el-icon-document',
      }
      const ext = this.fileName.split('.').pop().toLowerCase()
      return iconMap[ext] || iconMap.default
    },
  },
  watch: {
    fileUrl: {
      immediate: true,
      handler(val) {
        if (this.isText && val) {
          this.loadTextContent()
        }
      },
    },
  },
  methods: {
    // 加载文本内容
    async loadTextContent() {
      try {
        const response = await fetch(this.fileUrl)
        this.fileContent = await response.text()
      } catch (error) {
        console.error('加载文本内容失败:', error)
        this.fileContent = '加载文件内容失败'
      }
    },
    // 下载文件
    downloadFile() {
      const link = document.createElement('a')
      link.href = this.fileUrl
      link.download = this.fileName
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    },
  },
}
</script>

<style lang="scss" scoped>
.file-preview {
  width: 100%;
  height: 100%;

  .image-preview {
    display: flex;
    justify-content: center;
    align-items: center;

    .image-slot {
      display: flex;
      justify-content: center;
      align-items: center;
      width: 100%;
      height: 100%;
      background: #f5f7fa;
      color: #909399;
      font-size: 30px;
    }
  }

  .pdf-preview {
    width: 100%;
    height: 100%;

    iframe {
      border: none;
    }
  }

  .video-preview,
  .audio-preview {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
  }

  .text-preview {
    width: 100%;
    height: 100%;
    overflow: auto;
    background: #f5f7fa;
    padding: 15px;

    pre {
      margin: 0;
      white-space: pre-wrap;
      word-wrap: break-word;
      font-family: Monaco, Menlo, Consolas, 'Courier New', monospace;
    }
  }

  .other-preview {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 20px;

    .file-info {
      display: flex;
      align-items: center;
      margin-bottom: 15px;

      i {
        font-size: 48px;
        margin-right: 10px;
        color: #909399;
      }

      .file-name {
        font-size: 16px;
        color: #606266;
      }
    }
  }
}
</style>
