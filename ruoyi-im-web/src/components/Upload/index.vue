<template>
  <div class="upload-container">
    <el-upload
      ref="upload"
      :action="uploadUrl"
      :headers="headers"
      :multiple="multiple"
      :file-list="fileList"
      :show-file-list="true"
      :before-upload="handleBeforeUpload"
      :on-progress="handleProgress"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-exceed="handleExceed"
      :on-remove="handleRemove"
      :drag="drag"
      :accept="accept"
      :limit="limit"
      :disabled="disabled"
    >
      <i v-if="drag" class="el-icon-upload"></i>
      <div v-if="drag" class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
      <el-button v-else size="small" type="primary">
        <i class="el-icon-upload"></i> 选择文件
      </el-button>
      <template #tip>
        <div v-if="showTip" class="el-upload__tip">
          {{ tip }}
        </div>
      </template>
    </el-upload>

    <!-- 上传进度 -->
    <el-dialog
      v-model="dialogVisible"
      title="上传进度"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div v-for="(item, index) in uploadList" :key="index" class="upload-progress">
        <div class="file-info">
          <span class="filename">{{ item.name }}</span>
          <span class="progress-text">{{ item.percentage }}%</span>
        </div>
        <el-progress :percentage="item.percentage"></el-progress>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'

const props = defineProps({
  // 值
  value: {
    type: [String, Array],
    default: '',
  },
  // 上传地址
  uploadUrl: {
    type: String,
    default: process.env.VUE_APP_BASE_API + '/common/upload',
  },
  // 是否多选
  multiple: {
    type: Boolean,
    default: false,
  },
  // 是否拖拽上传
  drag: {
    type: Boolean,
    default: false,
  },
  // 接受上传的文件类型
  accept: {
    type: String,
    default: '',
  },
  // 最大上传数量
  limit: {
    type: Number,
    default: 5,
  },
  // 文件大小限制(MB)
  fileSize: {
    type: Number,
    default: 5,
  },
  // 是否显示提示
  showTip: {
    type: Boolean,
    default: true,
  },
  // 提示文字
  tip: {
    type: String,
    default: '只能上传jpg/png/gif/doc/docx/xls/xlsx/pdf文件，且不超过5MB',
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['input'])

const upload = ref(null)
const fileList = ref([])
const uploadList = ref([])
const dialogVisible = ref(false)

const headers = {
  Authorization: 'Bearer ' + getToken(),
}

// 监听 value 变化
watch(
  () => props.value,
  val => {
    if (val) {
      let list = []
      // 如果是字符串，转换为数组
      const values = Array.isArray(val) ? val : [val]
      for (let i = 0; i < values.length; i++) {
        if (values[i]) {
          let url = values[i]
          let name = url.substring(url.lastIndexOf('/') + 1)
          list.push({ name: name, url: url })
        }
      }
      fileList.value = list
    } else {
      fileList.value = []
    }
  },
  { immediate: true }
)

// 上传前验证
const handleBeforeUpload = file => {
  // 验证文件大小
  const isLt = file.size / 1024 / 1024 < props.fileSize
  if (!isLt) {
    ElMessage.error(`文件大小不能超过 ${props.fileSize}MB!`)
    return false
  }
  // 显示上传进度对话框
  if (!dialogVisible.value) {
    dialogVisible.value = true
    uploadList.value = []
  }
  uploadList.value.push({
    name: file.name,
    percentage: 0,
  })
  return true
}

// 上传进度
const handleProgress = (event, file) => {
  const item = uploadList.value.find(item => item.name === file.name)
  if (item) {
    item.percentage = Math.floor(event.percent)
  }
}

// 上传成功
const handleSuccess = (response, file) => {
  if (response.code === 200) {
    ElMessage.success('上传成功')
    // 更新文件列表
    updateFileList(newFileList)
  } else {
    ElMessage.error(response.msg)
    // 从文件列表中移除
    upload.value.handleRemove(file)
  }
  // 检查是否所有文件都上传完成
  checkUploadComplete()
}

// 上传失败
const handleError = (err, file) => {
  ElMessage.error('上传失败')
  // 从文件列表中移除
  upload.value.handleRemove(file)
  // 检查是否所有文件都上传完成
  checkUploadComplete()
}

// 文件超出个数限制时的钩子
const handleExceed = () => {
  ElMessage.warning(`最多只能上传 ${props.limit} 个文件`)
}

// 删除文件
const handleRemove = () => {
  // 文件删除逻辑
}

// 更新文件列表
const updateFileList = newFileList => {
  fileList.value = newFileList
  let urls = newFileList.map(item => (item.response ? item.response.data.url : item.url))
  emit('input', props.multiple ? urls : urls[0])
}

// 检查是否所有文件都上传完成
const checkUploadComplete = () => {
  const isComplete = uploadList.value.every(item => item.percentage === 100)
  if (isComplete) {
    setTimeout(() => {
      dialogVisible.value = false
      uploadList.value = []
    }, 1000)
  }
}
</script>

<style lang="scss" scoped>
.upload-container {
  .el-upload-dragger {
    width: 100%;
    height: 200px;
  }

  .upload-progress {
    margin-bottom: 15px;

    .file-info {
      display: flex;
      justify-content: space-between;
      margin-bottom: 5px;

      .filename {
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        margin-right: 10px;
      }

      .progress-text {
        width: 45px;
        text-align: right;
      }
    }
  }

  .el-upload__tip {
    line-height: 1.2;
    padding-top: 5px;
    color: #909399;
  }
}
</style>
