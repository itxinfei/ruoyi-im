<template>
  <el-dialog
    v-model="visible"
    title="分享日历"
    width="480px"
    destroy-on-close
    append-to-body
  >
    <div class="share-form">
      <!-- 分享给用户 -->
      <div class="form-section">
        <div class="section-title">分享给</div>
        <el-form ref="formRef" :model="formData" :rules="formRules" label-position="top">
          <el-form-item label="用户ID或姓名" prop="targetUserId">
            <el-select
              v-model="formData.targetUserId"
              filterable
              remote
              reserve-keyword
              placeholder="输入用户ID或搜索"
              :remote-method="searchUsers"
              :loading="searchLoading"
              style="width: 100%"
            >
              <el-option
                v-for="user in searchResults"
                :key="user.id"
                :label="user.name"
                :value="user.id"
              >
                <div class="user-option">
                  <el-avatar :size="24" :src="user.avatar" class="user-avatar">
                    {{ user.name?.charAt(0) }}
                  </el-avatar>
                  <span class="user-name">{{ user.name }}</span>
                  <span class="user-id">ID: {{ user.id }}</span>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="权限" prop="permission">
            <el-radio-group v-model="formData.permission">
              <el-radio value="VIEW">只读</el-radio>
              <el-radio value="EDIT">可编辑</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>

      <!-- 已分享列表 -->
      <div v-if="shares.length > 0" class="form-section">
        <div class="section-title">已分享 ({{ shares.length }})</div>
        <div class="share-list">
          <div v-for="share in shares" :key="share.id" class="share-item">
            <div class="share-user">
              <el-avatar :size="32" :src="share.targetUserAvatar" class="user-avatar">
                {{ share.targetUserName?.charAt(0) }}
              </el-avatar>
              <div class="user-info">
                <div class="user-name">{{ share.targetUserName }}</div>
                <div class="user-id">ID: {{ share.targetUserId }}</div>
              </div>
            </div>
            <div class="share-actions">
              <el-select
                :model-value="share.permission"
                size="small"
                style="width: 100px"
                @change="(val) => handlePermissionChange(share.id, val)"
              >
                <el-option value="VIEW" label="只读" />
                <el-option value="EDIT" label="可编辑" />
              </el-select>
              <el-button
                type="danger"
                text
                size="small"
                @click="handleDeleteShare(share.id)"
              >
                取消分享
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        分享
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  shareCalendar,
  getCalendarShares,
  updateCalendarShare,
  deleteCalendarShare
} from '@/api/im/schedule'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  calendarId: {
    type: [Number, String],
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const formRef = ref(null)
const submitting = ref(false)
const searchLoading = ref(false)
const searchResults = ref([])
const shares = ref([])

const formData = ref({
  targetUserId: '',
  permission: 'VIEW'
})

const formRules = {
  targetUserId: [{ required: true, message: '请选择用户', trigger: 'change' }]
}

// 搜索用户（模拟，实际应对接用户搜索API）
const searchUsers = async (query) => {
  if (!query) {
    searchResults.value = []
    return
  }
  searchLoading.value = true
  try {
    // 模拟搜索结果，实际应调用 /api/im/user/search 或类似接口
    const mockUsers = [
      { id: 1, name: '张三', avatar: '' },
      { id: 2, name: '李四', avatar: '' },
      { id: 3, name: '王五', avatar: '' }
    ].filter(u => u.name.includes(query) || String(u.id).includes(query))
    searchResults.value = mockUsers
  } finally {
    searchLoading.value = false
  }
}

// 加载已分享列表
const loadShares = async () => {
  if (!props.calendarId) return
  try {
    const res = await getCalendarShares(props.calendarId)
    if (res.code === 200) {
      shares.value = res.data || []
    }
  } catch (e) {
    console.error('加载分享列表失败', e)
  }
}

// 提交分享
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const res = await shareCalendar({
      calendarId: props.calendarId,
      targetUserId: formData.value.targetUserId,
      permission: formData.value.permission
    })

    if (res.code === 200) {
      ElMessage.success('分享成功')
      formData.value.targetUserId = ''
      formData.value.permission = 'VIEW'
      loadShares()
      emit('success')
    } else {
      ElMessage.error(res.msg || '分享失败')
    }
  } catch (e) {
    ElMessage.error('分享失败')
  } finally {
    submitting.value = false
  }
}

// 修改权限
const handlePermissionChange = async (shareId, permission) => {
  try {
    const res = await updateCalendarShare(shareId, { permission })
    if (res.code === 200) {
      ElMessage.success('权限已更新')
      loadShares()
    }
  } catch (e) {
    ElMessage.error('更新权限失败')
  }
}

// 删除分享
const handleDeleteShare = async (shareId) => {
  try {
    const res = await deleteCalendarShare(shareId)
    if (res.code === 200) {
      ElMessage.success('已取消分享')
      loadShares()
    }
  } catch (e) {
    ElMessage.error('取消分享失败')
  }
}

// 监听弹窗打开
watch(visible, (val) => {
  if (val) {
    loadShares()
  }
})
</script>

<style scoped lang="scss">
.share-form {
  max-height: 400px;
  overflow-y: auto;
}

.form-section {
  margin-bottom: var(--dt-spacing-lg);

  &:last-child {
    margin-bottom: 0;
  }
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
  margin-bottom: var(--dt-spacing-md);
}

.share-list {
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-sm);
}

.share-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--dt-spacing-sm);
  background: var(--dt-bg-body);
  border-radius: var(--dt-radius-md);
}

.share-user {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-sm);

  .user-info {
    .user-name {
      font-size: 14px;
      font-weight: 500;
      color: var(--dt-text-primary);
    }

    .user-id {
      font-size: 12px;
      color: var(--dt-text-tertiary);
    }
  }
}

.share-actions {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-sm);
}

.user-option {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-sm);
  padding: var(--dt-spacing-xs) 0;

  .user-avatar {
    flex-shrink: 0;
  }

  .user-name {
    flex: 1;
  }

  .user-id {
    font-size: 12px;
    color: var(--dt-text-tertiary);
  }
}
</style>
