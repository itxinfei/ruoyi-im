<template>
  <el-dialog
    v-model="visible"
    width="360px"
    :show-close="false"
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
          <!-- 状态指示灯 -->
          <div 
            class="absolute -bottom-1 -right-1 w-3.5 h-3.5 border-2 border-white dark:border-slate-900 rounded-full"
            :style="{ backgroundColor: statusColor }"
          ></div>
        </div>
        
        <div class="flex-1 min-w-0 pt-1">
          <div class="flex items-center gap-2">
            <h3 class="text-lg font-bold text-slate-800 dark:text-slate-100 truncate">
              {{ currentUser.nickname || currentUser.username }}
            </h3>
            <span v-if="currentUser.sex === 1" class="material-icons-outlined text-blue-500 text-sm">male</span>
            <span v-else-if="currentUser.sex === 2" class="material-icons-outlined text-pink-500 text-sm">female</span>
          </div>
          <p class="text-xs text-slate-400 mt-1 truncate">{{ currentUser.position || '暂无职位' }}</p>
          <div class="mt-2">
            <el-tag size="small" effect="plain" class="!border-slate-200 !text-slate-500 !bg-transparent cursor-pointer hover:!border-primary hover:!text-primary transition-colors" @click="handleStatusToggle">
              {{ statusLabel }}
            </el-tag>
          </div>
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
          <span class="value">{{ currentUser.departmentName || '未分配部门' }}</span>
        </div>
        <div class="profile-info-row">
          <span class="label">工号</span>
          <span class="value">{{ currentUser.id }}</span>
        </div>
        <div class="profile-info-row group cursor-pointer" @click="copyToClipboard(currentUser.mobile)">
          <span class="label">手机</span>
          <span class="value text-primary font-medium">{{ currentUser.mobile || '-' }}</span>
          <el-icon class="ml-1 text-slate-300 opacity-0 group-hover:opacity-100 transition-opacity"><CopyDocument /></el-icon>
        </div>
        <div class="profile-info-row group cursor-pointer" @click="copyToClipboard(currentUser.email)">
          <span class="label">邮箱</span>
          <span class="value truncate">{{ currentUser.email || '-' }}</span>
          <el-icon class="ml-1 text-slate-300 opacity-0 group-hover:opacity-100 transition-opacity"><CopyDocument /></el-icon>
        </div>
      </div>

      <!-- 底部：操作按钮栏 -->
      <div class="bg-slate-50/50 dark:bg-slate-800/50 p-4 px-6 flex items-center justify-between border-t border-slate-100 dark:border-slate-800">
        <div class="flex gap-2">
          <el-button type="primary" size="small" round @click="showEditDialog = true">
            <el-icon class="mr-1"><Edit /></el-icon>编辑资料
          </el-button>
          <el-button size="small" round plain @click="handleChangePassword">
            <el-icon class="mr-1"><Lock /></el-icon>安全
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
import { ref, watch, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import {
  Close, Edit, Lock, Briefcase, Phone, Message,
  SwitchButton, CopyDocument, User
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import EditProfileDialog from '@/components/EditProfileDialog/index.vue'
import ChangePasswordDialog from '@/components/Common/ChangePasswordDialog.vue'

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
const userStatus = ref('online')

const currentUser = computed(() => store.getters['user/currentUser'] || {})

const statusLabel = computed(() => {
  const map = { online: '在线', busy: '忙碌', away: '离开', meeting: '会议中' }
  return map[userStatus.value] || '在线'
})

const statusColor = computed(() => {
  const map = {
    online: '#22c55e',
    busy: '#ef4444',
    away: '#f59e0b',
    meeting: '#3b82f6'
  }
  return map[userStatus.value] || '#22c55e'
})

const copyToClipboard = (text) => {
  if (!text || text === '-' || text === '未填写') {
    ElMessage.warning('暂无内容可复制')
    return
  }
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('已复制')
  })
}

const handleStatusToggle = () => {
  const statusOptions = [
    { label: '在线', value: 'online' },
    { label: '忙碌', value: 'busy' },
    { label: '离开', value: 'away' },
    { label: '会议中', value: 'meeting' }
  ]
  const currentIndex = statusOptions.findIndex(s => s.value === userStatus.value)
  const nextIndex = (currentIndex + 1) % statusOptions.length
  userStatus.value = statusOptions[nextIndex].value
  ElMessage.success(`状态已切换: ${statusOptions[nextIndex].label}`)
}

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
    border-radius: 8px;
    overflow: hidden;
    padding: 0;
    border: 1px solid rgba(0,0,0,0.05);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
  }
  :deep(.el-dialog__header) { display: none; }
  :deep(.el-dialog__body) { padding: 0; }
}

.profile-info-row {
  display: flex;
  align-items: flex-start;
  font-size: 13px;
  line-height: 1.5;

  .label {
    width: 60px;
    color: #888;
    flex-shrink: 0;
    .dark & { color: #64748b; }
  }

  .value {
    color: #333;
    flex: 1;
    min-width: 0;
    word-break: break-all;
    .dark & { color: #cbd5e1; }
  }
}
</style>
