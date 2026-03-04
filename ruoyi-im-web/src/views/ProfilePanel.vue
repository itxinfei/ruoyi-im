<template>
  <div class="profile-panel">
    <!-- 资料头部 -->
    <div class="profile-header">
      <h2>个人资料</h2>
    </div>

    <!-- 资料内容 -->
    <div class="profile-content">
      <!-- 头像区域 -->
      <div class="avatar-section">
        <div class="avatar-wrapper" @click="handleChangeAvatar">
          <DingtalkAvatar :src="userInfo.avatar" :name="userInfo.nickname" :size="100" shape="square" />
          <div class="avatar-edit-mask">
            <span class="material-icons-outlined">camera_alt</span>
          </div>
        </div>
        <div class="avatar-tip">点击更换头像</div>
        <input ref="avatarInput" type="file" accept="image/*" style="display: none" @change="handleAvatarChange" />
      </div>

      <!-- 基本信息 -->
      <div class="info-section">
        <h3>基本信息</h3>
        <div class="info-list">
          <div class="info-item">
            <span class="label">昵称</span>
            <div class="value">
              <template v-if="!editing.nickname">
                {{ userInfo.nickname || '-' }}
                <span class="material-icons-outlined edit-icon" @click="startEdit('nickname')">edit</span>
              </template>
              <template v-else>
                <el-input v-model="editForm.nickname" size="small" style="width: 200px" />
                <el-button size="small" type="primary" @click="saveEdit('nickname')">保存</el-button>
                <el-button size="small" @click="cancelEdit('nickname')">取消</el-button>
              </template>
            </div>
          </div>
          <div class="info-item">
            <span class="label">用户名</span>
            <div class="value">
              {{ userInfo.username || '-' }}
            </div>
          </div>
          <div class="info-item">
            <span class="label">性别</span>
            <div class="value">
              <template v-if="!editing.gender">
                {{ genderText }}
                <span class="material-icons-outlined edit-icon" @click="startEdit('gender')">edit</span>
              </template>
              <template v-else>
                <el-radio-group v-model="editForm.gender">
                  <el-radio label="male">男</el-radio>
                  <el-radio label="female">女</el-radio>
                  <el-radio label="unknown">未知</el-radio>
                </el-radio-group>
                <el-button size="small" type="primary" @click="saveEdit('gender')">保存</el-button>
                <el-button size="small" @click="cancelEdit('gender')">取消</el-button>
              </template>
            </div>
          </div>
          <div class="info-item">
            <span class="label">生日</span>
            <div class="value">
              <template v-if="!editing.birthday">
                {{ userInfo.birthday || '-' }}
                <span class="material-icons-outlined edit-icon" @click="startEdit('birthday')">edit</span>
              </template>
              <template v-else>
                <el-date-picker v-model="editForm.birthday" type="date" placeholder="选择生日" size="small" />
                <el-button size="small" type="primary" @click="saveEdit('birthday')">保存</el-button>
                <el-button size="small" @click="cancelEdit('birthday')">取消</el-button>
              </template>
            </div>
          </div>
          <div class="info-item">
            <span class="label">邮箱</span>
            <div class="value">
              <template v-if="!editing.email">
                {{ userInfo.email || '-' }}
                <span class="material-icons-outlined edit-icon" @click="startEdit('email')">edit</span>
              </template>
              <template v-else>
                <el-input v-model="editForm.email" size="small" style="width: 200px" />
                <el-button size="small" type="primary" @click="saveEdit('email')">保存</el-button>
                <el-button size="small" @click="cancelEdit('email')">取消</el-button>
              </template>
            </div>
          </div>
          <div class="info-item">
            <span class="label">手机号</span>
            <div class="value">
              {{ userInfo.phone || '-' }}
            </div>
          </div>
        </div>
      </div>

      <!-- 账号信息 -->
      <div class="info-section">
        <h3>账号信息</h3>
        <div class="info-list">
          <div class="info-item">
            <span class="label">用户ID</span>
            <div class="value">
              {{ userInfo.id || '-' }}
            </div>
          </div>
          <div class="info-item">
            <span class="label">部门</span>
            <div class="value">
              {{ userInfo.department || '-' }}
            </div>
          </div>
          <div class="info-item">
            <span class="label">职位</span>
            <div class="value">
              <template v-if="!editing.position">
                {{ userInfo.position || '-' }}
                <span class="material-icons-outlined edit-icon" @click="startEdit('position')">edit</span>
              </template>
              <template v-else>
                <el-input v-model="editForm.position" size="small" style="width: 200px" />
                <el-button size="small" type="primary" @click="saveEdit('position')">保存</el-button>
                <el-button size="small" @click="cancelEdit('position')">取消</el-button>
              </template>
            </div>
          </div>
          <div class="info-item">
            <span class="label">个人签名</span>
            <div class="value">
              <template v-if="!editing.signature">
                {{ userInfo.signature || '-' }}
                <span class="material-icons-outlined edit-icon" @click="startEdit('signature')">edit</span>
              </template>
              <template v-else>
                <el-input v-model="editForm.signature" size="small" style="width: 300px" placeholder="个性签名" />
                <el-button size="small" type="primary" @click="saveEdit('signature')">保存</el-button>
                <el-button size="small" @click="cancelEdit('signature')">取消</el-button>
              </template>
            </div>
          </div>
          <div class="info-item">
            <span class="label">注册时间</span>
            <div class="value">
              {{ formatTime(userInfo.createTime) }}
            </div>
          </div>
        </div>
      </div>

      <!-- 修改密码 -->
      <div class="info-section">
        <h3>安全设置</h3>
        <div class="info-list">
          <div class="info-item">
            <span class="label">登录密码</span>
            <div class="value">
              <span class="password-dots">••••••••</span>
              <el-button size="small" @click="showPasswordDialog = true">修改密码</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="400px">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="80px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { getUserInfo, updateUser, uploadAvatar } from '@/api/im/user'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { ElMessage } from 'element-plus'

const store = useStore()

const userInfo = ref({})
const editing = ref({})
const editForm = ref({})
const showPasswordDialog = ref(false)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const avatarInput = ref(null)

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const genderText = computed(() => {
  const map = { male: '男', female: '女', unknown: '未知' }
  return map[userInfo.value.gender] || '未知'
})

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const res = await getUserInfo()
    if (res.code === 200) {
      userInfo.value = res.data || {}
    }
  } catch (e) {
    console.error('获取用户信息失败', e)
  }
}

const startEdit = (field) => {
  editing.value[field] = true
  editForm.value[field] = userInfo.value[field]
}

const cancelEdit = (field) => {
  editing.value[field] = false
  delete editForm.value[field]
}

const saveEdit = async (field) => {
  try {
    const data = { [field]: editForm.value[field] }
    const res = await updateUser(userInfo.value.id, data)
    if (res.code === 200) {
      userInfo.value[field] = editForm.value[field]
      editing.value[field] = false
      ElMessage.success('保存成功')
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const handleChangeAvatar = () => {
  avatarInput.value?.click()
}

const handleAvatarChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  
  const formData = new FormData()
  formData.append('avatar', file)
  
  try {
    const res = await uploadAvatar(formData)
    if (res.code === 200) {
      userInfo.value.avatar = res.data
      ElMessage.success('头像更新成功')
    } else {
      ElMessage.error(res.msg || '上传失败')
    }
  } catch (e) {
    ElMessage.error('上传失败')
  }
  
  e.target.value = ''
}

const handleChangePassword = async () => {
  // 这里调用修改密码API
  ElMessage.info('修改密码功能开发中')
  showPasswordDialog.value = false
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString()
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped lang="scss">
.profile-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.profile-header {
  padding: 16px 24px;
  border-bottom: 1px solid #e8e8e8;
  
  h2 {
    margin: 0;
    font-size: 18px;
    font-weight: 500;
  }
}

.profile-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.avatar-section {
  text-align: center;
  padding: 20px 0 40px;
  
  .avatar-wrapper {
    position: relative;
    display: inline-block;
    cursor: pointer;
    border-radius: 12px;
    overflow: hidden;
    
    .avatar-edit-mask {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.2s;
      
      .material-icons-outlined {
        color: #fff;
        font-size: 32px;
      }
    }
    
    &:hover .avatar-edit-mask {
      opacity: 1;
    }
  }
  
  .avatar-tip {
    margin-top: 12px;
    font-size: 13px;
    color: #999;
  }
}

.info-section {
  margin-bottom: 32px;
  
  h3 {
    margin: 0 0 16px;
    font-size: 15px;
    font-weight: 500;
    color: #333;
    padding-left: 12px;
    border-left: 3px solid #1677ff;
  }
}

.info-list {
  .info-item {
    display: flex;
    align-items: center;
    padding: 14px 0;
    border-bottom: 1px solid #f0f0f0;
    
    .label {
      width: 100px;
      font-size: 14px;
      color: #999;
    }
    
    .value {
      flex: 1;
      font-size: 14px;
      color: #333;
      display: flex;
      align-items: center;
      gap: 8px;
      
      .edit-icon {
        font-size: 16px;
        color: #999;
        cursor: pointer;
        
        &:hover {
          color: #1677ff;
        }
      }
      
      .password-dots {
        letter-spacing: 2px;
      }
    }
  }
}
</style>
