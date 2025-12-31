<template>
  <div class="component-upload-image">
    <el-upload
      :action="uploadImgUrl"
      :name="name"
      :show-file-list="false"
      :before-upload="handleBeforeUpload"
      :on-success="handleUploadSuccess"
      :on-error="handleUploadError"
      :headers="headers"
      class="image-uploader"
    >
      <img v-if="value" :src="value" class="image" />
      <i v-else class="el-icon-plus image-uploader-icon"></i>
    </el-upload>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'

defineProps({
  value: {
    type: String,
    default: '',
  },
  name: {
    type: String,
    default: 'file',
  },
  uploadImgUrl: {
    type: String,
    default: import.meta.env.VITE_BASE_API + '/common/upload',
  },
})

const emit = defineEmits(['input'])

const headers = {
  Authorization: 'Bearer ' + getToken(),
}

const maxSize = 5
const acceptTypes = ['.jpg', '.jpeg', '.png', '.gif']

const handleBeforeUpload = file => {
  // 校验文件类型
  let extension = file.name.substring(file.name.lastIndexOf('.')).toLowerCase()
  if (!acceptTypes.includes(extension)) {
    ElMessage.error(`只能上传${acceptTypes.join(',')}格式的图片！`)
    return false
  }
  // 校验文件大小
  if (file.size / 1024 / 1024 > maxSize) {
    ElMessage.error(`图片大小不能超过${maxSize}MB！`)
    return false
  }
  return true
}

const handleUploadSuccess = res => {
  if (res.code === 200) {
    emit('input', res.data.url)
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(res.msg)
  }
}

const handleUploadError = () => {
  ElMessage.error('上传失败，请重试')
}
</script>

<style lang="scss" scoped>
.component-upload-image {
  .image-uploader {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    width: 178px;
    height: 178px;

    &:hover {
      border-color: #409eff;
    }
  }

  .image-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 178px;
    height: 178px;
    line-height: 178px;
    text-align: center;
  }

  .image {
    width: 178px;
    height: 178px;
    display: block;
  }
}
</style>
