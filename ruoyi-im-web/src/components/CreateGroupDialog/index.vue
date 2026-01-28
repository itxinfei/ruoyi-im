<template>
  <div class="group-dialog">
    <el-dialog
      v-model="visible"
      title="创建群组"
      width="600px"
      :close-on-click-modal="true"
      @close="handleClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="群组名称" prop="name">
          <el-input
            v-model="form.name"
            placeholder="请输入群组名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="群组头像" prop="avatar">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
          >
            <img v-if="form.avatar" :src="addTokenToUrl(form.avatar)" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="群组公告" prop="announcement">
          <el-input
            v-model="form.announcement"
            type="textarea"
            :rows="3"
            placeholder="请输入群组公告"
            maxlength="200"
            show-word-limit
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
              v-for="contact in contacts"
              :key="contact.id"
              :label="contact.name"
              :value="contact.id"
            >
              <div class="contact-option">
                <el-avatar :size="24" :src="addTokenToUrl(contact.avatar)">
                  {{ contact.name?.charAt(0) }}
                </el-avatar>
                <span>{{ contact.name }}</span>
              </div>
            </el-option>
          </el-select>
          <div class="member-count">已选择 {{ form.members.length }} 人</div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">
          创建群组
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { createGroup } from '@/api/im/group'
import { getContacts } from '@/api/im/contact'
import { addTokenToUrl } from '@/utils/file'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(false)
const formRef = ref(null)
const loading = ref(false)
const contacts = ref([])

const form = reactive({
  name: '',
  avatar: '',
  announcement: '',
  members: []
})

const rules = {
  name: [
    { required: true, message: '请输入群组名称', trigger: 'blur' },
    { min: 2, max: 50, message: '群组名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  members: [
    { required: true, message: '请选择至少一个成员', trigger: 'change' },
    { type: 'array', min: 1, message: '请选择至少一个成员', trigger: 'change' }
  ]
}

// 加载联系人列表
const loadContacts = async () => {
  try {
    const response = await getContacts()
    if (response && response.data) {
      contacts.value = response.data.map(item => ({
        id: item.friendId || item.id,
        name: item.friendName || item.name || '未知',
        avatar: item.avatar || ''
      }))
    }
  } catch (error) {
    console.error('加载联系人失败:', error)
  }
}

import { uploadAvatar } from '@/api/im/user'

// 头像上传前验证
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }

  // Upload image
  const formData = new FormData()
  formData.append('avatarfile', file)
  
  uploadAvatar(formData).then(res => {
     if(res.code === 200) {
        form.avatar = res.data
        ElMessage.success('上传成功')
     }
  }).catch(err => {
     ElMessage.error('上传失败')
  })

  return false // Stop auto upload by element
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    loading.value = true

    const response = await createGroup({
      name: form.name,
      avatar: form.avatar,
      announcement: form.announcement,
      memberIds: form.members
    })

    if (response) {
      ElMessage.success('群组创建成功')
      emit('success', response.data)
      handleClose()
    }
  } catch (error) {
    console.error('创建群组失败:', error)
    if (error !== 'cancel') {
      ElMessage.error('创建群组失败')
    }
  } finally {
    loading.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  formRef.value?.resetFields()
  form.name = ''
  form.avatar = ''
  form.announcement = ''
  form.members = []
  emit('update:modelValue', false)
}

// 监听显示状态
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    loadContacts()
  }
})

watch(visible, (val) => {
  if (!val) {
    emit('update:modelValue', false)
  }
})
</script>

<style scoped lang="scss">
.contact-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.member-count {
  margin-top: 8px;
  font-size: 12px;
  color: #8c8c8c;
}

.avatar-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: all 0.3s;

    &:hover {
      border-color: #0089ff;
    }
  }
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c8c8c;
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
}
</style>
