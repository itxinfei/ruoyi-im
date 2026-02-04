<template>
  <el-dialog
    v-model="visible"
    :width="dialogWidth"
    :show-close="false"
    :close-on-click-modal="true"
    class="dingtalk-desktop-profile"
    destroy-on-close
    append-to-body
  >
    <div v-if="loading" v-loading="loading" class="h-64 flex items-center justify-center"></div>
    <div v-else class="bg-white dark:bg-slate-900">
      
      <!-- 顶部：头像与核心信息 -->
      <div class="p-6 pb-4 flex items-start gap-4 relative">
        <div class="relative flex-shrink-0">
          <DingtalkAvatar
            :src="currentUser.avatar"
            :name="currentUser.nickname || currentUser.username"
            :user-id="currentUser.id"
            :size="64"
            shape="square"
            class="rounded-lg shadow-sm border border-slate-100 dark:border-slate-800"
          />
        </div>
        
        <div class="flex-1 min-w-0 pt-1">
          <div class="flex items-center gap-2">
            <h3 class="text-lg font-bold text-slate-800 dark:text-slate-100 truncate">
              {{ currentUser.nickname || currentUser.username }}
            </h3>
            <span v-if="currentUser.sex === 1" class="material-icons-outlined text-blue-500 text-sm">male</span>
            <span v-else-if="currentUser.sex === 2" class="material-icons-outlined text-pink-500 text-sm">female</span>
          </div>
          <p class="text-xs text-slate-400 mt-1 truncate">{{ currentUser.position || currentUser.dept?.deptName || '暂无职位' }}</p>

        </div>

        <!-- 关闭按钮 -->
        <el-button 
          class="absolute top-4 right-4 !p-1 !h-auto !text-slate-300 hover:!text-slate-500 !bg-transparent !border-none"
          @click="handleClose"
        >
          <el-icon><Close /></el-icon>
        </el-button>
      </div>

      <!-- 分割线 -->
      <div class="mx-6 border-b border-slate-50 dark:border-slate-800"></div>

      <!-- 中部：详细属性列表 -->
      <div class="p-6 space-y-4">
        <div class="profile-info-row">
          <span class="label">部门</span>
          <span class="value">{{ currentUser.dept?.deptName || currentUser.departmentName || '未分配部门' }}</span>
        </div>
        <div class="profile-info-row">
          <span class="label">工号</span>
          <span class="value">{{ currentUser.id }}</span>
        </div>
        <div 
          class="profile-info-row group"
          :class="{ 'cursor-pointer': currentUser.mobile, 'cursor-default': !currentUser.mobile }" 
          @click="copyToClipboard(currentUser.mobile)"
        >
          <span class="label">手机</span>
          <div class="value-container">
            <span class="value text-primary font-medium">{{ currentUser.mobile || '-' }}</span>
            <el-icon 
              v-if="currentUser.mobile"
              class="copy-icon text-slate-300 opacity-0 group-hover:opacity-100 transition-opacity" 
              @click.stop="copyToClipboard(currentUser.mobile)"
            >
              <CopyDocument />
            </el-icon>
          </div>
        </div>
        <div 
          class="profile-info-row group"
          :class="{ 'cursor-pointer': currentUser.email, 'cursor-default': !currentUser.email }"
          @click="copyToClipboard(currentUser.email)"
        >
          <span class="label">邮箱</span>
          <div class="value-container">
            <span class="value truncate">{{ currentUser.email || '-' }}</span>
            <el-icon 
              v-if="currentUser.email"
              class="copy-icon text-slate-300 opacity-0 group-hover:opacity-100 transition-opacity"
              @click.stop="copyToClipboard(currentUser.email)"
            >
              <CopyDocument />
            </el-icon>
          </div>
        </div>
      </div>

      <!-- 底部：操作按钮栏 -->
      <div class="action-bar">
        <div class="flex gap-2">
          <el-button type="primary" size="small" round @click="showEditDialog = true">
            <el-icon class="mr-1"><Edit /></el-icon>编辑资料
          </el-button>
          <el-button size="small" round plain @click="handleChangePassword">
            <span class="material-icons-outlined mr-1">lock</span>安全
          </el-button>
        </div>
        <el-button link type="danger" size="small" @click="handleLogout">
          <el-icon class="mr-1"><SwitchButton /></el-icon>退出登录
        </el-button>
      </div>

    </div>

    <!-- 弹窗组件 -->
    <EditProfileDialog
      v-model:visible="showEditDialog"
      :user-info="currentUser"
      @save="handleSaveProfile"
    />
    <ChangePasswordDialog v-model="showChangePassword" />
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import {
  Close, Edit, Phone, Message,
  SwitchButton, CopyDocument, User
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import EditProfileDialog from '@/components/Common/EditProfileDialog.vue'
import ChangePasswordDialog from '@/components/Common/ChangePasswordDialog.vue'
import { copyToClipboard } from '@/utils/format'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue'])

const store = useStore()
const router = useRouter()

const visible = ref(false)
const loading = ref(false)
const showEditDialog = ref(false)
const showChangePassword = ref(false)

// 响应式宽度
const windowWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 1200)

const dialogWidth = computed(() => {
  if (windowWidth.value < 576) return '95%'
  if (windowWidth.value < 768) return '380px'
  return '420px'
})

// 监听窗口大小变化
onMounted(() => {
  window.addEventListener('resize', () => {
    windowWidth.value = window.innerWidth
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', () => {
    windowWidth.value = window.innerWidth
  })
})

const currentUser = computed(() => store.getters['user/currentUser'] || {})

const handleChangePassword = () => showChangePassword.value = true
const handleClose = () => emit('update:modelValue', false)

const handleSaveProfile = async (formData) => {
  try {
    await store.dispatch('user/updateProfile', formData)
    ElMessage.success('资料已更新')
    showEditDialog.value = false
  } catch (error) {
    ElMessage.error(error.message || '更新失败')
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出当前账号并返回登录界面吗？', '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning', roundButton: true
  }).then(async () => {
    await store.dispatch('user/logout')
    router.push('/login')
  }).catch(() => {})
}

watch(() => props.modelValue, (val) => visible.value = val)
watch(visible, (val) => { if (!val) emit('update:modelValue', false) })
</script>

<style scoped lang="scss">
.dingtalk-desktop-profile {
  :deep(.el-dialog) {
    border-radius: 12px;
    overflow: hidden;
    padding: 0;
    border: 1px solid rgba(0,0,0,0.05);
    box-shadow: var(--dt-shadow-dialog);
    // 确保在小屏幕上不会超出视口
    max-height: 90vh;
    
    // 移动端适配
    @media (max-width: 480px) {
      margin: 5vh auto !important;
      max-height: 85vh;
      border-radius: 16px;
    }
  }
  :deep(.el-dialog__header) { display: none; }
  :deep(.el-dialog__body) { 
    padding: 0; 
    // 确保内容可以滚动
    max-height: 85vh;
    overflow-y: auto;
  }
}

.profile-info-row {
  display: flex;
  align-items: flex-start;
  font-size: 14px;
  line-height: 1.6;
  padding: 12px 0;
  border-bottom: 1px solid transparent;
  transition: all 0.2s ease;

  &:last-child {
    border-bottom: none;
  }

  .label {
    min-width: 60px;
    width: 60px;
    color: #64748b;
    flex-shrink: 0;
    font-weight: 500;
    .dark & { color: #94a3b8; }
  }

  .value-container {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-width: 0;
    gap: 8px;
  }

  .value {
    color: #1e293b;
    flex: 1;
    min-width: 0;
    word-break: break-all;
    .dark & { color: #e2e8f0; }
  }
  
  .copy-icon {
    flex-shrink: 0;
    font-size: 16px;
    cursor: pointer;
    padding: 4px;
    border-radius: 4px;
    transition: all 0.2s ease;
    
    &:hover {
      background-color: var(--el-bg-color-page);
      color: var(--el-color-primary);
    }
  
  // 移动端始终显示
  @media (max-width: 768px) {
    opacity: 1 !important;
  }
}

.action-bar {
  background: rgba(248, 250, 252, 0.8);
  backdrop-filter: blur(8px);
  padding: 16px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  
  .dark & {
    background: rgba(30, 41, 59, 0.8);
    border-top-color: rgba(255, 255, 255, 0.05);
  }
  
  // 移动端优化
  @media (max-width: 480px) {
    padding: 12px 16px;
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
    
    .flex {
      justify-content: center;
    }
  }
}

// 移动端整体优化
@media (max-width: 480px) {
  .p-6 {
    padding: 16px !important;
  }
  
  .pb-4 {
    padding-bottom: 12px !important;
  }
  
  .space-y-4 > * + * {
    margin-top: 12px !important;
  }
}
  
  // 点击反馈
  &:active {
    background-color: var(--el-bg-color-page);
  }
}
</style>
