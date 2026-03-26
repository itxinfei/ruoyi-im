<template>
  <div class="group-dialog">
    <el-dialog
      v-model="visible"
      title="创建群组"
      width="560px"
      :close-on-click-modal="false"
      :close-on-press-escape="true"
      class="dingtalk-dialog"
      @close="handleClose"
    >
      <div class="dialog-scroll-container">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
        >
          <el-form-item label="群组名称" prop="name">
            <el-input
              v-model="form.name"
              placeholder="起个好听的名字"
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
              <img v-if="form.avatar" :src="addTokenToUrl(form.avatar)" class="avatar">
              <div v-else class="avatar-placeholder">
                <el-icon class="avatar-uploader-icon">
                  <Plus />
                </el-icon>
                <span>上传头像</span>
              </div>
            </el-upload>
          </el-form-item>

          <el-form-item label="群组公告" prop="announcement">
            <el-input
              v-model="form.announcement"
              type="textarea"
              :rows="3"
              placeholder="请输入群组公告（可选）"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="选择成员" prop="members">
            <el-select
              v-model="form.members"
              multiple
              filterable
              collapse-tags
              collapse-tags-tooltip
              placeholder="搜索并选择成员"
              style="width: 100%"
            >
              <el-option
                v-for="contact in contacts"
                :key="contact.id"
                :label="contact.name"
                :value="contact.id"
              >
                <div class="contact-option">
                  <DingtalkAvatar
                    :name="contact.name"
                    :user-id="contact.id"
                    :size="24"
                    shape="square"
                  />
                  <span>{{ contact.name }}</span>
                </div>
              </el-option>
            </el-select>
            <div class="member-count">
              当前选择: <b>{{ form.members.length }}</b> 人
            </div>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleClose">
            取消
          </el-button>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            立即创建
          </el-button>
        </div>
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
    if (res.code === 200) {
      form.avatar = res.data
      ElMessage.success('上传成功')
    }
  }).catch(() => {
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
.dialog-scroll-container {
  max-height: 450px;
  overflow-y: auto;
  padding: 0 4px; // 留出滚动条空间
  margin: -10px 0; // 补偿 Form 的内边距
}

.avatar-uploader {
  .avatar-placeholder {
    width: 100px; height: 100px; border: 1px dashed var(--dt-border-color); border-radius: 8px;
    display: flex; flex-direction: column; align-items: center; justify-content: center;
    gap: 8px; color: var(--dt-text-desc); font-size: 12px; transition: all 0.2s;
    &:hover { border-color: var(--dt-brand-color); color: var(--dt-brand-color); background: var(--dt-bg-hover); }
    .avatar-uploader-icon { font-size: 24px; margin: 0; }
  }
}

.contact-option {
  display: flex; align-items: center; gap: 10px; padding: 4px 0;
}

.member-count {
  margin-top: 10px; font-size: 13px; color: var(--dt-text-secondary); b { color: var(--dt-brand-color); }
}
</style>
