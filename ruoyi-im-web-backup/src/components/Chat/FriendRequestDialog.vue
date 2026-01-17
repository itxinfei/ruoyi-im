<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="400"
    :close-on-click-modal="false"
    class="friend-request-dialog"
    @close="handleClose"
  >
    <!-- 添加好友表单 -->
    <div v-if="mode === 'add'" class="add-friend-form">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px">
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="formData.userId" placeholder="请输入对方用户ID" clearable />
        </el-form-item>

        <el-form-item label="验证信息" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入验证信息（选填）"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="分组" prop="groupId">
          <el-select v-model="formData.groupId" placeholder="选择分组" style="width: 100%">
            <el-option label="默认分组" value="" />
            <el-option
              v-for="group in friendGroups"
              :key="group.id"
              :label="group.name"
              :value="group.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
    </div>

    <!-- 好友请求列表 -->
    <div v-else-if="mode === 'list'" class="request-list">
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else-if="requests.length === 0" class="empty-state">
        <el-icon><Box /></el-icon>
        <p>暂无好友申请</p>
      </div>

      <div v-else class="request-items">
        <div v-for="request in requests" :key="request.id" class="request-item">
          <el-avatar :size="50" :src="request.fromAvatar">
            {{ (request.fromName || request.fromNickName)?.charAt(0) }}
          </el-avatar>
          <div class="request-info">
            <div class="request-name">{{ request.fromName || request.fromNickName }}</div>
            <div class="request-remark">{{ request.remark || '请求添加你为好友' }}</div>
            <div class="request-time">{{ formatTime(request.createTime) }}</div>
          </div>
          <div v-if="request.status === 'PENDING'" class="request-actions">
            <el-button type="primary" size="small" @click="handleRequest(request, 'accept')">
              同意
            </el-button>
            <el-button size="small" @click="handleRequest(request, 'reject')"> 拒绝 </el-button>
          </div>
          <div v-else class="request-status">
            <el-tag :type="request.status === 'ACCEPTED' ? 'success' : 'info'" size="small">
              {{ request.status === 'ACCEPTED' ? '已同意' : '已拒绝' }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- 处理结果 -->
    <div v-else-if="mode === 'result'" class="result-state">
      <el-icon class="success-icon"><SuccessFilled /></el-icon>
      <p class="result-text">好友申请已发送</p>
      <p class="result-hint">等待对方确认</p>
    </div>

    <template v-if="mode !== 'result'" #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">{{ mode === 'list' ? '关闭' : '取消' }}</el-button>
        <el-button v-if="mode === 'add'" type="primary" :loading="submitting" @click="handleSubmit">
          发送申请
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Loading, Box, SuccessFilled } from '@element-plus/icons-vue'
import {
  addContact,
  getFriendRequests,
  handleFriendRequest,
  getFriendGroups,
} from '@/api/im/contact'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  mode: {
    type: String,
    default: 'add', // add | list | result
  },
  userId: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['update:modelValue', 'success'])

const formRef = ref(null)
const submitting = ref(false)
const loading = ref(false)
const requests = ref([])
const friendGroups = ref([])

const formData = ref({
  userId: props.userId || '',
  remark: '',
  groupId: '',
})

const rules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
}

const visible = computed({
  get: () => props.modelValue,
  set: val => emit('update:modelValue', val),
})

const title = computed(() => {
  switch (props.mode) {
    case 'add':
      return '添加好友'
    case 'list':
      return '好友申请'
    case 'result':
      return '发送成功'
    default:
      return '添加好友'
  }
})

// 监听对话框打开
watch(
  () => props.modelValue,
  val => {
    if (val) {
      if (props.mode === 'add') {
        formData.value.userId = props.userId || ''
        loadFriendGroups()
      } else if (props.mode === 'list') {
        loadRequests()
      }
    }
  }
)

// 加载好友分组
const loadFriendGroups = async () => {
  try {
    const res = await getFriendGroups()
    if (res.code === 200) {
      friendGroups.value = res.data || []
    }
  } catch (error) {
    console.error('加载分组失败:', error)
  }
}

// 加载好友申请列表
const loadRequests = async () => {
  loading.value = true
  try {
    const res = await getFriendRequests()
    if (res.code === 200) {
      requests.value = (res.data || []).filter(r => r.status === 'PENDING')
    }
  } catch (error) {
    console.error('加载好友申请失败:', error)
  } finally {
    loading.value = false
  }
}

// 提交添加好友申请
const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await addContact({
      friendId: formData.value.userId,
      remark: formData.value.remark,
      groupId: formData.value.groupId,
    })
    emit('success')
    formData.value = { userId: '', remark: '', groupId: '' }
  } catch (error) {
    console.error('添加好友失败:', error)
  } finally {
    submitting.value = false
  }
}

// 处理好友申请
const handleRequest = async (request, action) => {
  try {
    await handleFriendRequest({
      requestId: request.id,
      action: action, // accept | reject
    })
    ElMessage.success(action === 'accept' ? '已同意好友申请' : '已拒绝好友申请')
    // 从列表中移除
    const index = requests.value.findIndex(r => r.id === request.id)
    if (index > -1) {
      requests.value.splice(index, 1)
    }
    emit('success')
  } catch (error) {
    console.error('处理好友申请失败:', error)
    ElMessage.error('操作失败')
  }
}

// 格式化时间
const formatTime = time => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'

  return date.toLocaleDateString('zh-CN')
}

// 关闭对话框
const handleClose = () => {
  emit('update:modelValue', false)
}
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.friend-request-dialog {
  .add-friend-form {
    padding: 10px 0;
  }

  .request-list {
    min-height: 200px;
    max-height: 400px;
    overflow-y: auto;
  }

  .loading-state,
  .empty-state,
  .result-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px 20px;
    color: $text-secondary;
    gap: 12px;

    .el-icon {
      font-size: 48px;
      color: $text-tertiary;
    }

    &.result-state {
      .success-icon {
        font-size: 64px;
        color: $success-color;
      }

      .result-text {
        font-size: 16px;
        font-weight: 500;
        color: $text-primary;
      }

      .result-hint {
        font-size: 13px;
        color: $text-secondary;
      }
    }
  }

  .request-items {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .request-item {
    display: flex;
    align-items: center;
    padding: 12px;
    background: #f5f7fa;
    border-radius: 8px;
    gap: 12px;

    .request-info {
      flex: 1;
      min-width: 0;

      .request-name {
        font-size: 14px;
        font-weight: 500;
        color: $text-primary;
        margin-bottom: 4px;
      }

      .request-remark {
        font-size: 13px;
        color: $text-secondary;
        margin-bottom: 4px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .request-time {
        font-size: 12px;
        color: $text-tertiary;
      }
    }

    .request-actions {
      display: flex;
      gap: 8px;
      flex-shrink: 0;
    }

    .request-status {
      flex-shrink: 0;
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }
}
</style>
