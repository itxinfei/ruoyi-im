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
  height: 120px;
  background: linear-gradient(135deg, #3b82f6 0%, #06b6d4 100%);
  position: relative;
  
  .dark & { background: linear-gradient(135deg, #1e40af 0%, #0e7490 100%); }

  .close-btn {
    position: absolute;
    top: 12px;
    right: 12px;
    background: rgba(0, 0, 0, 0.2);
    border: none;
    color: #fff;
    width: 32px;
    height: 32px;
    transition: all 0.2s;
    backdrop-filter: blur(4px);
    
    &:hover {
      background: rgba(0, 0, 0, 0.4);
      transform: rotate(90deg);
    }
  }
}

.profile-header {
  padding: 0 24px;
  margin-top: -44px;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 28px;

  .avatar-wrapper {
    position: relative;
    margin-bottom: 12px;
    filter: drop-shadow(0 4px 6px rgba(0,0,0,0.1));
    
    :deep(.user-avatar) {
      border: 4px solid #fff;
      border-radius: 20px;
      .dark & { border-color: #1e293b; }
    }
  }
  /* ... rest ... */
}

/* ... existing code ... */

.profile-actions {
  padding: 0 24px 28px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  
  .action-btn {
    height: 42px;
    width: 100%;
    font-weight: 500;
    border-radius: 8px;
    font-size: 15px;
    transition: all 0.2s;
    &:hover { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2); }
  }
  
  .logout-btn {
    height: 42px;
    width: 100%;
    border-radius: 8px;
    font-size: 15px;
    &:hover { background: #fee2e2; border-color: #ef4444; color: #ef4444; .dark & { background: rgba(239, 68, 68, 0.1); } }
  }
}
</style>
