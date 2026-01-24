<template>
  <el-dialog
    v-model="visible"
    width="420px"
    :show-close="false"
    class="personal-profile-dialog"
    destroy-on-close
    append-to-body
  >
    <div v-if="loading" v-loading="loading" class="loading-state"></div>
    <div v-else class="profile-container">
      <!-- 顶部封面与关闭按钮 -->
      <div class="profile-cover">
        <el-button class="circle-btn close-btn" circle @click="handleClose">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>

      <!-- 用户核心信息 -->
      <div class="profile-header">
        <div class="avatar-wrapper">
          <DingtalkAvatar
            :src="currentUser.avatar"
            :name="currentUser.nickname || currentUser.username"
            :user-id="currentUser.id"
            :size="88"
            shape="square"
            custom-class="user-avatar"
          />
        </div>
        
        <div class="user-main">
          <div class="name-row">
            <h2 class="nickname">{{ currentUser.nickname || currentUser.username }}</h2>
            <el-icon v-if="currentUser.gender === 1" class="gender-icon male"><Male /></el-icon>
            <el-icon v-else-if="currentUser.gender === 2" class="gender-icon female"><Female /></el-icon>
            <el-tag size="small" type="success" class="status-tag">在线</el-tag>
          </div>
          <p class="account">账号：{{ currentUser.username }}</p>
        </div>
      </div>

      <!-- 详细资料项 -->
      <div class="profile-details">
        <div class="detail-item">
          <span class="label">职位</span>
          <span class="value">{{ currentUser.position || '成员' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">部门</span>
          <span class="value">{{ currentUser.departmentName || currentUser.department || '未分配' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">手机</span>
          <span class="value">{{ currentUser.mobile || '未填写' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">邮箱</span>
          <span class="value">{{ currentUser.email || '未填写' }}</span>
        </div>
        <div class="detail-item signature">
          <span class="label">签名</span>
          <span class="value">{{ currentUser.signature || '这个人很懒，什么都没写~' }}</span>
        </div>
      </div>

      <!-- 底部操作栏 -->
      <div class="profile-actions">
        <el-button type="primary" class="action-btn" @click="showEditDialog = true">
          <el-icon><Edit /></el-icon>
          <span>编辑资料</span>
        </el-button>
        <el-button type="danger" plain class="logout-btn" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出登录</span>
        </el-button>
      </div>
    </div>

    <!-- 内嵌编辑弹窗 -->
    <EditProfileDialog 
      v-model:visible="showEditDialog"
      :user-info="currentUser"
      @save="handleSaveProfile"
    />
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { Close, Male, Female, Edit, SwitchButton } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import EditProfileDialog from '@/components/EditProfileDialog/index.vue'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue'])

const store = useStore()
const router = useRouter()
const visible = ref(false)
const loading = ref(false)
const showEditDialog = ref(false)

const currentUser = computed(() => store.getters['user/currentUser'] || {})

const handleClose = () => {
  emit('update:modelValue', false)
}

const handleSaveProfile = async (formData) => {
  try {
    await store.dispatch('user/updateProfile', formData)
    ElMessage.success('更新成功')
    showEditDialog.value = false
  } catch (error) {
    ElMessage.error(error.message || '更新失败')
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await store.dispatch('user/logout')
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {})
}

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  if (!val) {
    emit('update:modelValue', false)
  }
})
</script>

<style scoped lang="scss">
.personal-profile-dialog :deep(.el-dialog__header) {
  display: none;
}

.personal-profile-dialog :deep(.el-dialog__body) {
  padding: 0;
  overflow: hidden;
  border-radius: 12px;
}

.loading-state {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-container {
  background: #fff;
  .dark & { background: #1e293b; }
}

.profile-cover {
  height: 100px;
  background: linear-gradient(135deg, #0089ff 0%, #00d2ff 100%);
  position: relative;

  .close-btn {
    position: absolute;
    top: 12px;
    right: 12px;
    background: rgba(0, 0, 0, 0.1);
    border: none;
    color: #fff;
    &:hover {
      background: rgba(0, 0, 0, 0.3);
    }
  }
}

.profile-header {
  padding: 0 24px;
  margin-top: -44px;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24px;

  .avatar-wrapper {
    position: relative;
    margin-bottom: 12px;
    :deep(.user-avatar) {
      border: 4px solid #fff;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      border-radius: 16px;
      .dark & { border-color: #1e293b; }
    }
  }

  .user-main {
    text-align: center;
    .name-row {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      .nickname {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
        color: #1f2329;
        .dark & { color: #f1f5f9; }
      }
      .gender-icon {
        font-size: 16px;
        &.male { color: #0089ff; }
        &.female { color: #ff4d4f; }
      }
      .status-tag {
        border-radius: 4px;
        padding: 0 4px;
      }
    }
    .account {
      margin: 4px 0 0;
      font-size: 13px;
      color: #8f959e;
    }
  }
}

.profile-details {
  padding: 0 24px;
  margin-bottom: 24px;
  
  .detail-item {
    display: flex;
    justify-content: space-between;
    padding: 12px 0;
    border-bottom: 1px solid #f2f3f5;
    font-size: 14px;
    
    .dark & { border-color: #334155; }

    &:last-child {
      border-bottom: none;
    }

    .label {
      color: #8f959e;
      flex-shrink: 0;
    }
    
    .value {
      color: #1f2329;
      text-align: right;
      word-break: break-all;
      .dark & { color: #f1f5f9; }
    }
    
    &.signature {
      flex-direction: column;
      .value {
        text-align: left;
        margin-top: 6px;
        color: #646a73;
        font-style: italic;
        .dark & { color: #94a3b8; }
      }
    }
  }
}

.profile-actions {
  padding: 0 24px 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  
  .action-btn {
    height: 40px;
    width: 100%;
    font-weight: 500;
  }
  
  .logout-btn {
    height: 40px;
    width: 100%;
  }
}
</style>
