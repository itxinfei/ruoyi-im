<template>
  <el-dialog
    v-model="visible"
    title="群设置"
    width="520px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      class="group-settings-form"
    >
      <!-- 群名称 -->
      <el-form-item label="群名称" prop="name">
        <el-input
          v-model="formData.name"
          placeholder="请输入群名称"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <!-- 群头像 -->
      <el-form-item label="群头像">
        <div class="avatar-upload">
          <div class="avatar-preview" @click="handleAvatarClick">
            <img v-if="formData.avatar" :src="formData.avatar" alt="群头像" />
            <div v-else class="avatar-placeholder">
              <span class="material-icons-outlined">groups</span>
            </div>
            <div class="avatar-mask">
              <span class="material-icons-outlined">edit</span>
            </div>
          </div>
          <el-button size="small" @click="handleAvatarClick">更换头像</el-button>
          <input
            ref="avatarInputRef"
            type="file"
            accept="image/*"
            style="display: none"
            @change="handleAvatarChange"
          />
        </div>
      </el-form-item>

      <!-- 群简介 -->
      <el-form-item label="群简介">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入群简介"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <!-- 群公告 -->
      <el-form-item label="群公告">
        <el-input
          v-model="formData.notice"
          type="textarea"
          :rows="3"
          placeholder="请输入群公告"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-divider content-position="left">群管理设置</el-divider>

      <!-- 入群验证 -->
      <el-form-item label="入群验证">
        <div class="setting-item">
          <el-switch v-model="formData.needApproval" />
          <span class="setting-tip">开启后需要管理员审核才能加入</span>
        </div>
      </el-form-item>

      <!-- 邀请确认 -->
      <el-form-item label="邀请确认">
        <div class="setting-item">
          <el-switch v-model="formData.inviteConfirm" />
          <span class="setting-tip">邀请成员入群时需要确认</span>
        </div>
      </el-form-item>

      <!-- 允许成员上传文件 -->
      <el-form-item label="上传文件">
        <div class="setting-item">
          <el-switch v-model="formData.allowUpload" />
          <span class="setting-tip">允许普通成员上传群文件</span>
        </div>
      </el-form-item>

      <!-- 显示成员列表 -->
      <el-form-item label="成员列表">
        <div class="setting-item">
          <el-switch v-model="formData.showMemberList" />
          <span class="setting-tip">非群成员可以查看群成员列表</span>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { updateGroup } from '@/api/im/group'
import { uploadGroupAvatar } from '@/api/im/file'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  group: { type: Object, default: null }
})

const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const formRef = ref(null)
const avatarInputRef = ref(null)
const saving = ref(false)

const formData = reactive({
  name: '',
  avatar: '',
  description: '',
  notice: '',
  needApproval: false,
  inviteConfirm: false,
  allowUpload: true,
  showMemberList: true
})

const formRules = {
  name: [
    { required: true, message: '请输入群名称', trigger: 'blur' },
    { min: 2, max: 50, message: '群名称长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

// 监听群组变化，初始化表单数据
watch(() => props.group, (group) => {
  if (group) {
    formData.name = group.name || ''
    formData.avatar = group.avatar || ''
    formData.description = group.description || ''
    formData.notice = group.notice || ''
    formData.needApproval = group.needApproval || false
    formData.inviteConfirm = group.inviteConfirm || false
    formData.allowUpload = group.allowUpload !== false
    formData.showMemberList = group.showMemberList !== false
  }
}, { immediate: true })

// 点击头像选择
const handleAvatarClick = () => {
  avatarInputRef.value?.click()
}

// 头像文件选择
const handleAvatarChange = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }

  // 验证文件大小（5MB）
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return
  }

  // 本地预览
  const reader = new FileReader()
  reader.onload = (e) => {
    formData.avatar = e.target.result
  }
  reader.readAsDataURL(file)

  // 上传头像
  try {
    const formDataUpload = new FormData()
    formDataUpload.append('file', file)
    const res = await uploadGroupAvatar(props.group.id, formDataUpload)
    if (res.code === 200) {
      formData.avatar = res.data.url || res.data
      ElMessage.success('头像上传成功')
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    // 上传失败但不影响预览，用户保存时会重试
  }

  // 清空input以允许重复选择同一文件
  event.target.value = ''
}

// 保存设置
const handleSave = async () => {
  try {
    await formRef.value?.validate()

    saving.value = true
    const updateData = {
      name: formData.name,
      avatar: formData.avatar,
      description: formData.description,
      notice: formData.notice,
      needApproval: formData.needApproval,
      inviteConfirm: formData.inviteConfirm,
      allowUpload: formData.allowUpload,
      showMemberList: formData.showMemberList
    }

    await updateGroup(props.group.id, updateData)
    ElMessage.success('群设置保存成功')
    emit('saved', updateData)
    handleClose()
  } catch (error) {
    if (error !== false) { // 表单验证失败时error为false
      console.error('保存群设置失败:', error)
      ElMessage.error('保存失败，请重试')
    }
  } finally {
    saving.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  formRef.value?.resetFields()
  visible.value = false
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.group-settings-form {
  .avatar-upload {
    display: flex;
    align-items: center;
    gap: 16px;

    .avatar-preview {
      position: relative;
      width: 64px;
      height: 64px;
      border-radius: 12px;
      overflow: hidden;
      cursor: pointer;
      background: var(--dt-bg-body);
      border: 1px solid var(--dt-border-light);

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .avatar-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: var(--dt-text-tertiary);

        .material-icons-outlined {
          font-size: 32px;
        }
      }

      .avatar-mask {
        position: absolute;
        inset: 0;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        align-items: center;
        justify-content: center;
        opacity: 0;
        transition: opacity 0.2s;

        .material-icons-outlined {
          color: #fff;
          font-size: 20px;
        }
      }

      &:hover .avatar-mask {
        opacity: 1;
      }
    }
  }

  .setting-item {
    display: flex;
    align-items: center;
    gap: 12px;

    .setting-tip {
      font-size: 13px;
      color: var(--dt-text-secondary);
    }
  }

  :deep(.el-divider) {
    margin: 24px 0 20px;
  }

  :deep(.el-form-item) {
    margin-bottom: 20px;
  }

  :deep(.el-textarea__inner) {
    border-radius: 8px;
  }

  :deep(.el-input__inner) {
    border-radius: 8px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
