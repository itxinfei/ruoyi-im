<template>
  <div class="group-create-container">
    <div class="create-header">
      <h2>创建群组</h2>
    </div>

    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" class="create-form">
      <el-form-item label="群组名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入群组名称" />
      </el-form-item>

      <el-form-item label="群组头像" prop="avatar">
        <el-upload
          class="avatar-uploader"
          :show-file-list="false"
          :on-success="handleAvatarSuccess"
          :before-upload="beforeAvatarUpload"
        >
          <img v-if="form.avatar" :src="form.avatar" class="avatar" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
      </el-form-item>

      <el-form-item label="群组描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="4"
          placeholder="请输入群组描述"
        />
      </el-form-item>

      <el-form-item label="选择成员" prop="members">
        <el-select
          v-model="form.members"
          multiple
          filterable
          placeholder="请选择群组成员"
          style="width: 100%"
        >
          <el-option
            v-for="user in users"
            :key="user.id"
            :label="user.name || user.username"
            :value="user.id"
          >
            <div class="user-option">
              <el-avatar :size="24" :src="user.avatar">
                {{ (user.name || user.username)?.charAt(0) }}
              </el-avatar>
              <span>{{ user.name || user.username }}</span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitForm">创建群组</el-button>
        <el-button @click="goBack">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref(null)

const form = reactive({
  name: '',
  avatar: '',
  description: '',
  members: [],
})

const rules = {
  name: [{ required: true, message: '请输入群组名称', trigger: 'blur' }],
  members: [{ required: true, message: '请选择群组成员', trigger: 'change' }],
}

const users = ref([
  { id: 1, name: '张三', username: 'zhangsan', avatar: '' },
  { id: 2, name: '李四', username: 'lisi', avatar: '' },
  { id: 3, name: '王五', username: 'wangwu', avatar: '' },
])

const handleAvatarSuccess = (response) => {
  form.avatar = response.url
  ElMessage.success('头像上传成功')
}

const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('头像图片只能是 JPG/PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('头像图片大小不能超过 2MB!')
  }
  return isJPG && isLt2M
}

const submitForm = async () => {
  if (!formRef.value) return

  await formRef.value.validate((valid) => {
    if (valid) {
      ElMessage.success('群组创建成功')
      router.push('/im/group')
    }
  })
}

const goBack = () => {
  router.back()
}
</script>

<style lang="scss" scoped>
@use '@/styles/dingtalk-theme.scss' as *;

.group-create-container {
  height: 100%;
  overflow-y: auto;
  background-color: $bg-white;

  .create-header {
    padding: $spacing-lg;
    border-bottom: 1px solid $border-light;

    h2 {
      margin: 0;
      font-size: $font-size-xl;
      font-weight: $font-weight-semibold;
    }
  }

  .create-form {
    max-width: 600px;
    margin: $spacing-xl auto;
    padding: 0 $spacing-lg;

    .avatar-uploader {
      .avatar {
        width: 100px;
        height: 100px;
        display: block;
      }

      .avatar-uploader-icon {
        font-size: 28px;
        color: $text-tertiary;
        width: 100px;
        height: 100px;
        line-height: 100px;
        text-align: center;
        border: 1px dashed $border-base;
        border-radius: $border-radius-lg;
        cursor: pointer;

        &:hover {
          border-color: $primary-color;
        }
      }
    }

    .user-option {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
    }
  }
}
</style>
