<template>
  <el-dialog
    :model-value="visible"
    title="编辑个人资料"
    width="500px"
    @update:model-value="$emit('update:visible', $event)"
  >
    <el-form :model="form" label-width="80px" :rules="rules" ref="formRef">
      <el-form-item label="头像">
        <div class="avatar-uploader" @click="triggerFileUpload">
          <img v-if="form.avatar" :src="addTokenToUrl(form.avatar)" class="avatar" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          <div class="upload-mask">
            <el-icon><Camera /></el-icon>
          </div>
        </div>
        <input
          type="file"
          ref="fileInput"
          style="display: none"
          accept="image/*"
          @change="handleFileChange"
        />
      </el-form-item>

      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="form.nickname" placeholder="请输入昵称" />
      </el-form-item>

      <el-form-item label="手机号" prop="mobile">
        <el-input v-model="form.mobile" placeholder="请输入手机号" />
      </el-form-item>

      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱" />
      </el-form-item>

      <el-form-item label="部门" prop="department">
        <el-input v-model="form.department" placeholder="请输入所属部门" />
      </el-form-item>

      <el-form-item label="职位" prop="position">
        <el-input v-model="form.position" placeholder="请输入职位" />
      </el-form-item>

      <el-form-item label="个性签名" prop="signature">
        <el-input
          v-model="form.signature"
          type="textarea"
          placeholder="请输入个性签名"
          :rows="3"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="$emit('update:visible', false)">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSave">保存</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Plus, Camera } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { uploadAvatar } from '@/api/im/user'
import { addTokenToUrl } from '@/utils/file'

const props = defineProps({
  visible: Boolean,
  userInfo: Object
})

const emit = defineEmits(['update:visible', 'save'])

const formRef = ref(null)
const fileInput = ref(null)
const loading = ref(false)

const form = ref({
  nickname: '',
  mobile: '',
  email: '',
  department: '',
  position: '',
  signature: '',
  avatar: ''
})

const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  mobile: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

watch(() => props.userInfo, (newVal) => {
  if (newVal) {
    form.value = { ...newVal }
  }
}, { immediate: true })

const triggerFileUpload = () => {
  fileInput.value.click()
}

const handleFileChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }

  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 2MB')
    return
  }

  const formData = new FormData()
  formData.append('avatarfile', file)

  try {
    const res = await uploadAvatar(formData)
    if (res.code === 200) {
      form.value.avatar = res.data
      ElMessage.success('头像上传成功')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('头像上传失败')
  }
}

const handleSave = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      emit('save', form.value)
      loading.value = false // Parent should handle closing, but for safety
    }
  })
}
</script>

<style scoped>
.avatar-uploader {
  width: 100px;
  height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader:hover .upload-mask {
  opacity: 1;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.upload-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.3s;
}

.upload-mask .el-icon {
  font-size: 24px;
}
</style>
