<template>
  <div class="avatar-upload-container">
    <div class="avatar-preview" @click="handleClick">
      <el-avatar :size="size" :src="currentAvatar">
        {{ defaultText }}
      </el-avatar>
      <div v-if="!disabled" class="upload-overlay">
        <el-icon class="upload-icon"><Camera /></el-icon>
        <span class="upload-text">{{ uploadText }}</span>
      </div>
    </div>

    <input
      ref="fileInput"
      type="file"
      accept="image/*"
      style="display: none"
      @change="handleFileChange"
    />

    <el-dialog
      v-model="cropperVisible"
      title="裁剪头像"
      width="600px"
      :close-on-click-modal="false"
      @close="handleCropperClose"
    >
      <div class="cropper-container">
        <div class="cropper-wrapper">
          <vue-cropper
            ref="cropperRef"
            :aspect-ratio="1"
            :view-mode="1"
            :drag-mode="'move'"
            :auto-crop="true"
            :auto-crop-width="200"
            :auto-crop-height="200"
            :background="true"
            :rotatable="true"
            :scalable="true"
            :zoomable="true"
            :min-container-width="300"
            :min-container-height="300"
            src=""
          />
        </div>
        <div class="cropper-preview">
          <div class="preview-title">预览</div>
          <div class="preview-box">
            <img :src="previewImage" class="preview-image" />
          </div>
          <div class="preview-title" style="margin-top: 20px">圆形预览</div>
          <div class="preview-box preview-circle">
            <img :src="previewImage" class="preview-image" />
          </div>
        </div>
      </div>

      <div class="cropper-actions">
        <el-button @click="handleRotateLeft">
          <el-icon><RefreshLeft /></el-icon>
          左旋转
        </el-button>
        <el-button @click="handleRotateRight">
          <el-icon><RefreshRight /></el-icon>
          右旋转
        </el-button>
        <el-button @click="handleScaleX">
          <el-icon><Sort /></el-icon>
          水平翻转
        </el-button>
        <el-button @click="handleScaleY">
          <el-icon><Sort style="transform: rotate(90deg)" /></el-icon>
          垂直翻转
        </el-button>
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </div>

      <template #footer>
        <el-button @click="cropperVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleConfirmCrop">
          确认上传
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera, RefreshLeft, RefreshRight, Sort, Refresh } from '@element-plus/icons-vue'
import VueCropper from 'vue-cropperjs'
import { uploadAvatar } from '@/api/system/user'
import { useStore } from 'vuex'

const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
  size: {
    type: Number,
    default: 100,
  },
  defaultText: {
    type: String,
    default: 'U',
  },
  uploadText: {
    type: String,
    default: '更换头像',
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  maxSize: {
    type: Number,
    default: 2,
  },
  acceptTypes: {
    type: Array,
    default: () => ['image/jpeg', 'image/png', 'image/gif', 'image/jpg'],
  },
})

const emit = defineEmits(['update:modelValue', 'upload-success', 'upload-error'])

const store = useStore()

const fileInput = ref(null)
const cropperRef = ref(null)
const cropperVisible = ref(false)
const uploading = ref(false)
const previewImage = ref('')

const currentAvatar = computed(() => props.modelValue)

watch(
  () => cropperRef.value,
  (cropper) => {
    if (cropper) {
      cropper.getCroppedCanvas().toBlob((blob) => {
        const reader = new FileReader()
        reader.onload = (e) => {
          previewImage.value = e.target.result
        }
        reader.readAsDataURL(blob)
      })
    }
  }
)

const handleClick = () => {
  if (props.disabled) return
  fileInput.value.click()
}

const handleFileChange = (event) => {
  const file = event.target.files[0]
  if (!file) return

  if (!validateFile(file)) {
    return
  }

  const reader = new FileReader()
  reader.onload = (e) => {
    if (cropperRef.value) {
      cropperRef.value.replace(e.target.result)
    }
    cropperVisible.value = true
  }
  reader.readAsDataURL(file)

  event.target.value = ''
}

const validateFile = (file) => {
  if (!props.acceptTypes.includes(file.type)) {
    ElMessage.error(`只支持 ${props.acceptTypes.join(', ')} 格式的图片`)
    return false
  }

  const maxSizeMB = props.maxSize
  const maxSizeBytes = maxSizeMB * 1024 * 1024
  if (file.size > maxSizeBytes) {
    ElMessage.error(`图片大小不能超过 ${maxSizeMB}MB`)
    return false
  }

  return true
}

const handleRotateLeft = () => {
  if (cropperRef.value) {
    cropperRef.value.rotate(-90)
    updatePreview()
  }
}

const handleRotateRight = () => {
  if (cropperRef.value) {
    cropperRef.value.rotate(90)
    updatePreview()
  }
}

const handleScaleX = () => {
  if (cropperRef.value) {
    const data = cropperRef.value.getData()
    cropperRef.value.scaleX(data.scaleX === 1 ? -1 : 1)
    updatePreview()
  }
}

const handleScaleY = () => {
  if (cropperRef.value) {
    const data = cropperRef.value.getData()
    cropperRef.value.scaleY(data.scaleY === 1 ? -1 : 1)
    updatePreview()
  }
}

const handleReset = () => {
  if (cropperRef.value) {
    cropperRef.value.reset()
    updatePreview()
  }
}

const updatePreview = () => {
  if (cropperRef.value) {
    cropperRef.value.getCroppedCanvas().toBlob((blob) => {
      const reader = new FileReader()
      reader.onload = (e) => {
        previewImage.value = e.target.result
      }
      reader.readAsDataURL(blob)
    })
  }
}

const handleConfirmCrop = async () => {
  if (!cropperRef.value) return

  uploading.value = true

  try {
    const canvas = cropperRef.value.getCroppedCanvas({
      width: 200,
      height: 200,
    })

    canvas.toBlob(async (blob) => {
      if (!blob) {
        ElMessage.error('图片处理失败')
        uploading.value = false
        return
      }

      const formData = new FormData()
      formData.append('avatarfile', blob, 'avatar.png')

      try {
        const response = await uploadAvatar(formData)

        if (response.code === 200 || response.code === 0) {
          const avatarUrl = response.data?.url || response.imgUrl || response.url
          emit('update:modelValue', avatarUrl)
          emit('upload-success', avatarUrl)
          
          store.commit('user/SET_AVATAR', avatarUrl)
          
          ElMessage.success('头像上传成功')
          cropperVisible.value = false
        } else {
          throw new Error(response.msg || '上传失败')
        }
      } catch (error) {
        console.error('Avatar upload error:', error)
        ElMessage.error(error.message || '头像上传失败')
        emit('upload-error', error)
      } finally {
        uploading.value = false
      }
    }, 'image/png')
  } catch (error) {
    console.error('Crop error:', error)
    ElMessage.error('图片裁剪失败')
    uploading.value = false
  }
}

const handleCropperClose = () => {
  if (cropperRef.value) {
    cropperRef.value.reset()
  }
}
</script>

<style lang="scss" scoped>
.avatar-upload-container {
  display: inline-block;

  .avatar-preview {
    position: relative;
    cursor: pointer;
    display: inline-block;

    &:hover .upload-overlay {
      opacity: 1;
    }

    .upload-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background-color: rgba(0, 0, 0, 0.5);
      border-radius: 50%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.3s;

      .upload-icon {
        color: #fff;
        font-size: 24px;
        margin-bottom: 4px;
      }

      .upload-text {
        color: #fff;
        font-size: 12px;
      }
    }
  }

  .cropper-container {
    display: flex;
    gap: 20px;

    .cropper-wrapper {
      flex: 1;
      height: 300px;
      background-color: #f5f5f5;
      border-radius: 4px;
      overflow: hidden;

      :deep(.vue-cropper) {
        height: 100%;
      }
    }

    .cropper-preview {
      width: 200px;
      display: flex;
      flex-direction: column;
      align-items: center;

      .preview-title {
        font-size: 14px;
        color: #606266;
        margin-bottom: 10px;
      }

      .preview-box {
        width: 150px;
        height: 150px;
        border: 1px solid #dcdfe6;
        border-radius: 4px;
        overflow: hidden;
        background-color: #f5f5f5;

        &.preview-circle {
          border-radius: 50%;
        }

        .preview-image {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }
    }
  }

  .cropper-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;

    .el-button {
      flex: 1;
      min-width: 100px;
    }
  }
}

@media screen and (max-width: 768px) {
  .avatar-upload-container {
    .cropper-container {
      flex-direction: column;

      .cropper-wrapper {
        height: 250px;
      }

      .cropper-preview {
        width: 100%;
        flex-direction: row;
        justify-content: center;
        gap: 20px;

        .preview-box {
          width: 100px;
          height: 100px;
        }
      }
    }

    .cropper-actions {
      .el-button {
        min-width: 80px;
        font-size: 12px;
      }
    }
  }
}

@media screen and (max-width: 480px) {
  .avatar-upload-container {
    .cropper-wrapper {
      height: 200px;
    }

    .cropper-preview {
      flex-direction: column;

      .preview-box {
        width: 80px;
        height: 80px;
      }
    }

    .cropper-actions {
      gap: 8px;

      .el-button {
        min-width: 60px;
        font-size: 11px;
        padding: 8px 12px;
      }
    }
  }
}
</style>
