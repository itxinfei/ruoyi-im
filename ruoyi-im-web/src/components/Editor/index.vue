<template>
  <div class="editor">
    <el-upload
      v-if="showUpload"
      class="editor-upload"
      :action="uploadUrl"
      :headers="headers"
      :before-upload="handleBeforeUpload"
      :on-success="handleUploadSuccess"
      :on-error="handleUploadError"
      :show-file-list="false"
      accept="image/*"
    >
      <el-button size="small" type="primary">
        <i class="el-icon-upload2"></i>
        插入图片
      </el-button>
    </el-upload>

    <div ref="editor" class="editor-content"></div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import 'quill/dist/quill.core.css'
import 'quill/dist/quill.snow.css'
import { getToken } from '@/utils/auth'

const Quill = require('quill')

const props = defineProps({
  /* 编辑器的内容 */
  value: {
    type: String,
    default: '',
  },
  /* 高度 */
  height: {
    type: [String, Number],
    default: '300px',
  },
  /* 最小高度 */
  minHeight: {
    type: [String, Number],
    default: '300px',
  },
  /* 是否显示上传图片按钮 */
  showUpload: {
    type: Boolean,
    default: true,
  },
  /* 上传地址 */
  uploadUrl: {
    type: String,
    default: import.meta.env.VITE_BASE_API + '/common/upload',
  },
})

const emit = defineEmits(['input', 'change', 'selectionChange'])

const editor = ref(null)
const quillInstance = ref(null)
const content = ref('')

const headers = {
  Authorization: 'Bearer ' + getToken(),
}

const options = {
  theme: 'snow',
  bounds: document.body,
  debug: 'warn',
  modules: {
    toolbar: [
      ['bold', 'italic', 'underline', 'strike'], // 加粗，斜体，下划线，删除线
      ['blockquote', 'code-block'], // 引用，代码块
      [{ header: 1 }, { header: 2 }], // 标题，键值对的形式；1、2表示字体大小
      [{ list: 'ordered' }, { list: 'bullet' }], // 列表
      [{ script: 'sub' }, { script: 'super' }], // 上下标
      [{ indent: '-1' }, { indent: '+1' }], // 缩进
      [{ direction: 'rtl' }], // 文本方向
      [{ size: ['small', false, 'large', 'huge'] }], // 字体大小
      [{ header: [1, 2, 3, 4, 5, 6, false] }], // 标题
      [{ color: [] }, { background: [] }], // 字体颜色，字体背景颜色
      [{ font: [] }], // 字体
      [{ align: [] }], // 对齐方式
      ['clean'], // 清除字体样式
      ['link', 'image'], // 链接、图片
    ],
  },
  placeholder: '请输入内容',
  readOnly: false,
}

// 监听 value 变化
watch(
  () => props.value,
  val => {
    if (val !== content.value) {
      content.value = val
      if (quillInstance.value) {
        quillInstance.value.pasteHTML(content.value)
      }
    }
  },
  { immediate: true }
)

const init = () => {
  if (!editor.value) return

  quillInstance.value = new Quill(editor.value, options)

  // 设置编辑器内容
  if (content.value) {
    quillInstance.value.pasteHTML(content.value)
  }

  quillInstance.value.on('text-change', () => {
    const html = editor.value.children[0].innerHTML
    const text = quillInstance.value.getText()
    const quill = quillInstance.value
    content.value = html
    emit('input', html)
    emit('change', { html, text, quill })
  })

  quillInstance.value.on('selection-change', range => {
    emit('selectionChange', range)
  })

  // 设置编辑器高度
  editor.value.style.height = props.height
  editor.value.style.minHeight = props.minHeight
}

// 上传前校检格式和大小
const handleBeforeUpload = file => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('上传图片只能是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 上传成功回调
const handleUploadSuccess = res => {
  if (res.code === 200) {
    // 获取光标所在位置
    let length = quillInstance.value.getSelection()?.index || 0
    quillInstance.value.insertEmbed(length, 'image', res.data.url)
    // 光标向后移动一位
    quillInstance.value.setSelection(length + 1)
  } else {
    ElMessage.error('图片插入失败')
  }
}

// 上传失败回调
const handleUploadError = () => {
  ElMessage.error('图片插入失败')
}

onMounted(() => {
  nextTick(() => {
    init()
  })
})

onBeforeUnmount(() => {
  quillInstance.value = null
})
</script>

<style lang="scss" scoped>
.editor {
  position: relative;

  .editor-upload {
    position: absolute;
    right: 10px;
    top: 10px;
    z-index: 10;
  }

  .editor-content {
    line-height: normal !important;
    height: 300px;
  }

  :deep(.ql-container) {
    height: auto;
    min-height: 300px;
    max-height: 500px;
    overflow-y: auto;
  }
}
</style>
