<template>
  <el-dialog
    v-model="visible"
    :title="isOwnProfile ? '个人资料' : '用户资料'"
    width="480px"
    :close-on-click-modal="false"
    class="user-profile-dialog"
    @close="handleClose"
  >
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else class="profile-content">
      <!-- 头部信息 -->
      <div class="profile-header">
        <div class="avatar-section">
          <el-avatar :size="80" :src="userInfo.avatar">
            {{ (userInfo.nickname || userInfo.username || 'U')?.charAt(0) }}
          </el-avatar>
          <el-tag v-if="userInfo.gender === 1" size="small" class="gender-tag male">
            <el-icon><Male /></el-icon>
          </el-tag>
          <el-tag v-else-if="userInfo.gender === 2" size="small" class="gender-tag female">
            <el-icon><Female /></el-icon>
          </el-tag>
        </div>

        <div class="basic-info">
          <h3 class="nickname">{{ userInfo.nickname || '未设置昵称' }}</h3>
          <p class="username">@{{ userInfo.username }}</p>
          <p v-if="userInfo.signature" class="signature">{{ userInfo.signature }}</p>
          <div class="status-badge" :class="onlineStatus">
            <span class="status-dot"></span>
            <span class="status-text">{{ statusText }}</span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="header-actions">
          <template v-if="!isOwnProfile">
            <el-button v-if="!isFriend" type="primary" @click="handleAddFriend">
              <el-icon><Plus /></el-icon>
              添加好友
            </el-button>
            <el-button v-else @click="handleSendMessage">
              <el-icon><ChatDotRound /></el-icon>
              发消息
            </el-button>
          </template>
          <el-button v-else type="primary" @click="handleEditProfile">
            <el-icon><Edit /></el-icon>
            编辑资料
          </el-button>
        </div>
      </div>

      <!-- 详细信息 -->
      <div class="profile-details">
        <div class="detail-section">
          <h4 class="section-title">基本信息</h4>
          <div class="detail-list">
            <div v-if="userInfo.email" class="detail-item">
              <el-icon class="detail-icon"><Message /></el-icon>
              <span class="detail-label">邮箱</span>
              <span class="detail-value">{{ userInfo.email }}</span>
            </div>
            <div v-if="userInfo.phonenumber" class="detail-item">
              <el-icon class="detail-icon"><Phone /></el-icon>
              <span class="detail-label">手机</span>
              <span class="detail-value">{{ userInfo.phonenumber }}</span>
            </div>
            <div class="detail-item">
              <el-icon class="detail-icon"><Calendar /></el-icon>
              <span class="detail-label">注册时间</span>
              <span class="detail-value">{{ formatDate(userInfo.createTime) }}</span>
            </div>
          </div>
        </div>

        <!-- 备注名（仅好友可见） -->
        <div v-if="!isOwnProfile && isFriend" class="detail-section">
          <h4 class="section-title">备注信息</h4>
          <div class="remark-input">
            <el-input
              v-model="remark"
              placeholder="添加备注名..."
              :maxlength="20"
              @blur="handleSaveRemark"
            >
              <template #append>
                <el-button @click="handleSaveRemark">保存</el-button>
              </template>
            </el-input>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑资料弹窗 -->
    <el-dialog
      v-model="showEditDialog"
      title="编辑个人资料"
      width="500px"
      append-to-body
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="头像">
          <div class="avatar-edit">
            <el-avatar :size="60" :src="editForm.avatar">
              {{ (editForm.nickname || editForm.username || 'U')?.charAt(0) }}
            </el-avatar>
            <el-upload
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
              accept="image/*"
              name="avatarfile"
            >
              <el-button size="small" :loading="avatarUploading">更换头像</el-button>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item label="昵称">
          <el-input
            v-model="editForm.nickname"
            placeholder="请输入昵称"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="个性签名">
          <el-input
            v-model="editForm.signature"
            type="textarea"
            :rows="3"
            placeholder="介绍一下自己..."
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>

        <el-form-item label="手机号">
          <el-input v-model="editForm.phonenumber" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="性别">
          <el-radio-group v-model="editForm.gender">
            <el-radio :label="0">保密</el-radio>
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSaveProfile">保存</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Male,
  Female,
  Plus,
  ChatDotRound,
  Edit,
  Message,
  Phone,
  Calendar,
} from '@element-plus/icons-vue'
import { getCurrentUserInfo, setUserInfo } from '@/utils/im-user'
import { updateProfile, uploadAvatar as uploadAvatarApi } from '@/api/im/user'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  userId: {
    type: Number,
    default: null,
  },
})

const emit = defineEmits(['update:modelValue', 'update-success'])

const visible = ref(false)
const loading = ref(false)
const saving = ref(false)
const avatarUploading = ref(false)
const showEditDialog = ref(false)
const isFriend = ref(false)
const remark = ref('')

const userInfo = reactive({
  id: null,
  username: '',
  nickname: '',
  signature: '',
  email: '',
  phonenumber: '',
  gender: 0,
  avatar: '',
  createTime: null,
  status: 1,
})

const editForm = reactive({
  id: null,
  username: '',
  nickname: '',
  signature: '',
  email: '',
  phonenumber: '',
  gender: 0,
  avatar: '',
})

// 是否是自己的资料
const isOwnProfile = computed(() => {
  if (!props.userId) return true
  const currentUserId = getCurrentUserInfo()?.userId || getCurrentUserInfo()?.id
  return props.userId === currentUserId
})

// 在线状态
const onlineStatus = computed(() => {
  if (userInfo.status === 0) return 'offline'
  if (isOwnProfile.value) return 'online'
  // TODO: 从WebSocket获取真实在线状态
  return 'online'
})

const statusText = computed(() => {
  const statusMap = {
    online: '在线',
    offline: '离线',
    busy: '忙碌',
    away: '离开',
  }
  return statusMap[onlineStatus.value] || '未知'
})

// 上传配置
const uploadUrl = computed(() => {
  return (import.meta.env.VITE_APP_BASE_API || '/api') + '/im/user/avatar'
})

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token') || localStorage.getItem('Admin-Token')
  return {
    Authorization: `Bearer ${token}`,
  }
})

// 监听显示状态
watch(
  () => props.modelValue,
  val => {
    visible.value = val
    if (val) {
      loadUserInfo()
    }
  }
)

watch(visible, val => {
  emit('update:modelValue', val)
})

// 加载用户信息
const loadUserInfo = async () => {
  loading.value = true
  try {
    const currentInfo = getCurrentUserInfo()
    const targetUserId = props.userId || currentInfo?.userId || currentInfo?.id

    if (targetUserId) {
      // TODO: 从API获取指定用户信息
      if (isOwnProfile.value || !props.userId) {
        // 加载自己的信息
        Object.assign(userInfo, {
          id: currentInfo.userId || currentInfo.id,
          username: currentInfo.username || currentInfo.userName || '',
          nickname: currentInfo.nickname || currentInfo.nickName || '',
          signature: currentInfo.signature || '',
          email: currentInfo.email || '',
          phonenumber: currentInfo.phonenumber || currentInfo.phone || '',
          gender: currentInfo.gender || 0,
          avatar: currentInfo.avatar || '',
          createTime: currentInfo.createTime || new Date(),
          status: currentInfo.status || 1,
        })

        // 同时初始化编辑表单
        Object.assign(editForm, {
          id: userInfo.id,
          username: userInfo.username,
          nickname: userInfo.nickname,
          signature: userInfo.signature,
          email: userInfo.email,
          phonenumber: userInfo.phonenumber,
          gender: userInfo.gender,
          avatar: userInfo.avatar,
        })
      } else {
        // TODO: 加载其他用户信息
        // 这里需要调用API获取指定用户的信息
      }
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = date => {
  if (!date) return '未知'
  const d = new Date(date)
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
}

// 编辑资料
const handleEditProfile = () => {
  showEditDialog.value = true
}

// 保存资料
const handleSaveProfile = async () => {
  saving.value = true
  try {
    await updateProfile(editForm.id, {
      nickname: editForm.nickname,
      signature: editForm.signature,
      email: editForm.email,
      phonenumber: editForm.phonenumber,
      gender: editForm.gender,
    })

    // 更新本地信息
    const currentInfo = getCurrentUserInfo()
    if (currentInfo) {
      Object.assign(currentInfo, {
        nickname: editForm.nickname,
        nickName: editForm.nickname,
        name: editForm.nickname,
        signature: editForm.signature,
        email: editForm.email,
        phonenumber: editForm.phonenumber,
        gender: editForm.gender,
        avatar: editForm.avatar,
      })
      setUserInfo(currentInfo)
    }

    // 更新显示信息
    Object.assign(userInfo, {
      nickname: editForm.nickname,
      signature: editForm.signature,
      email: editForm.email,
      phonenumber: editForm.phonenumber,
      gender: editForm.gender,
      avatar: editForm.avatar,
    })

    showEditDialog.value = false
    ElMessage.success('保存成功')
    emit('update-success')
  } catch (error) {
    ElMessage.error(error.message || '保存失败，请重试')
  } finally {
    saving.value = false
  }
}

// 上传头像前的校验
const beforeAvatarUpload = file => {
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
  avatarUploading.value = true
  return true
}

// 头像上传成功
const handleAvatarSuccess = response => {
  avatarUploading.value = false
  if (response.code === 200) {
    const avatarUrl = response.data
    editForm.avatar = avatarUrl
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response.msg || '头像上传失败')
  }
}

// 添加好友
const handleAddFriend = () => {
  ElMessage.info('添加好友功能开发中...')
  // TODO: 实现添加好友功能
}

// 发送消息
const handleSendMessage = () => {
  emit('send-message', userInfo.id)
  visible.value = false
}

// 保存备注
const handleSaveRemark = () => {
  if (remark.value) {
    ElMessage.success('备注已保存')
    // TODO: 调用API保存备注
  }
}

const handleClose = () => {
  visible.value = false
}
</script>

<style lang="scss" scoped>
.user-profile-dialog {
  :deep(.el-dialog__body) {
    padding: 0 20px 20px;
  }
}

.loading-container {
  padding: 40px 20px;
}

.profile-content {
  .profile-header {
    display: flex;
    gap: 20px;
    padding-bottom: 20px;
    border-bottom: 1px solid #f0f0f0;

    .avatar-section {
      position: relative;
      flex-shrink: 0;

      .gender-tag {
        position: absolute;
        bottom: 0;
        right: -8px;

        &.male {
          background: #e6f7ff;
          color: #1890ff;
          border-color: #1890ff;
        }

        &.female {
          background: #fff0f6;
          color: #eb2f96;
          border-color: #eb2f96;
        }
      }
    }

    .basic-info {
      flex: 1;

      .nickname {
        margin: 0 0 4px 0;
        font-size: 20px;
        font-weight: 500;
        color: #333;
      }

      .username {
        margin: 0 0 8px 0;
        font-size: 14px;
        color: #999;
      }

      .signature {
        margin: 0 0 12px 0;
        font-size: 13px;
        color: #666;
        line-height: 1.5;
      }

      .status-badge {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 4px 10px;
        border-radius: 12px;
        font-size: 12px;

        &.online {
          background: #f0f9ff;
          color: #52c41a;
        }

        &.offline {
          background: #f5f5f5;
          color: #999;
        }

        &.busy {
          background: #fff1f0;
          color: #ff4d4f;
        }

        &.away {
          background: #fffbe6;
          color: #faad14;
        }

        .status-dot {
          width: 8px;
          height: 8px;
          border-radius: 50%;
          background: currentColor;
        }
      }
    }

    .header-actions {
      display: flex;
      flex-direction: column;
      gap: 8px;
      justify-content: center;
    }
  }

  .profile-details {
    padding-top: 20px;

    .detail-section {
      margin-bottom: 24px;

      &:last-child {
        margin-bottom: 0;
      }

      .section-title {
        margin: 0 0 12px 0;
        font-size: 14px;
        font-weight: 500;
        color: #999;
      }

      .detail-list {
        .detail-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 12px 0;
          border-bottom: 1px solid #f5f5f5;

          &:last-child {
            border-bottom: none;
          }

          .detail-icon {
            font-size: 16px;
            color: #999;
          }

          .detail-label {
            width: 70px;
            font-size: 14px;
            color: #666;
          }

          .detail-value {
            flex: 1;
            font-size: 14px;
            color: #333;
          }
        }
      }

      .remark-input {
        :deep(.el-input-group__append) {
          background: #fff;
        }
      }
    }
  }
}

.avatar-edit {
  display: flex;
  align-items: center;
  gap: 16px;
}

// 暗色主题适配
:deep(.dark) {
  .profile-content {
    .profile-header {
      border-bottom-color: #333;

      .basic-info {
        .nickname {
          color: #e5e5e5;
        }

        .username {
          color: #666;
        }

        .signature {
          color: #999;
        }
      }
    }

    .profile-details {
      .detail-section {
        .detail-list {
          .detail-item {
            border-bottom-color: #333;

            .detail-label {
              color: #999;
            }

            .detail-value {
              color: #e5e5e5;
            }
          }
        }
      }
    }
  }
}
</style>
