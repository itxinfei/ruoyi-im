<template>
  <el-dialog
    v-model="visible"
    title="邀请成员"
    width="600px"
    class="invite-member-dialog"
    destroy-on-close
    append-to-body
  >
    <div class="invite-container">
      <!-- 搜索区域 -->
      <div class="search-section">
        <el-input
          v-model="searchQuery"
          placeholder="搜索用户姓名、手机号或邮箱"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <!-- 已选成员 -->
      <div v-if="selectedUsers.length > 0" class="selected-section">
        <div class="section-title">
          已选择 {{ selectedUsers.length }} 人
        </div>
        <div class="selected-list">
          <el-tag
            v-for="user in selectedUsers"
            :key="user.id"
            closable
            @close="removeUser(user)"
          >
            {{ user.name }}
          </el-tag>
        </div>
      </div>

      <!-- 用户列表 -->
      <div class="users-section">
        <div class="section-title">
          用户列表
        </div>
        <div v-if="loading" class="loading-state">
          <el-icon class="is-loading">
            <Loading />
          </el-icon>
          <span>加载中...</span>
        </div>
        <div v-else-if="userList.length === 0" class="empty-state">
          <span class="material-icons-outlined">person_off</span>
          <p>{{ searchQuery ? '未找到匹配的用户' : '暂无可用用户' }}</p>
        </div>
        <div v-else class="user-list">
          <div
            v-for="user in userList"
            :key="user.id"
            class="user-item"
            :class="{ selected: isSelected(user), disabled: isMember(user) }"
            @click="toggleUser(user)"
          >
            <el-checkbox :model-value="isSelected(user)" :disabled="isMember(user)" />
            <DingtalkAvatar
              :src="user.avatar"
              :name="user.name"
              :size="40"
              shape="circle"
            />
            <div class="user-info">
              <div class="user-name">
                {{ user.name }}
              </div>
              <div class="user-dept">
                {{ user.department || '未知部门' }}
              </div>
            </div>
            <el-tag v-if="isMember(user)" size="small" type="info">
              已在群
            </el-tag>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :disabled="selectedUsers.length === 0"
          :loading="inviting"
          @click="handleInvite"
        >
          邀请 ({{ selectedUsers.length }})
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Loading } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { searchUsers } from '@/api/im/user'
import { addGroupMembers } from '@/api/im/group'

const props = defineProps({
  modelValue: Boolean,
  groupId: { type: [Number, String], required: true },
  existingMembers: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const searchQuery = ref('')
const loading = ref(false)
const inviting = ref(false)
const userList = ref([])
const selectedUsers = ref([])
let searchTimer = null

const isSelected = (user) => selectedUsers.value.some(u => u.id === user.id)
const isMember = (user) => props.existingMembers.some(m => m.id === user.id || m.userId === user.id)

const handleSearch = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    loadUsers()
  }, 300)
}

// 组件卸载时清理定时器
onUnmounted(() => {
  if (searchTimer) {
    clearTimeout(searchTimer)
    searchTimer = null
  }
})

const loadUsers = async () => {
  if (!searchQuery.value.trim()) {
    userList.value = []
    return
  }
  loading.value = true
  try {
    const res = await searchUsers(searchQuery.value.trim())
    if (res.code === 200 && res.data) {
      userList.value = res.data.map(u => ({
        id: u.id,
        name: u.nickname || u.username,
        avatar: u.avatar,
        department: u.departmentName || u.department
      }))
    }
  } catch (error) {
    console.error('搜索用户失败:', error)
    ElMessage.error('搜索用户失败')
  } finally {
    loading.value = false
  }
}

const toggleUser = (user) => {
  if (isMember(user)) return
  if (isSelected(user)) {
    removeUser(user)
  } else {
    selectedUsers.value.push(user)
  }
}

const removeUser = (user) => {
  selectedUsers.value = selectedUsers.value.filter(u => u.id !== user.id)
}

const handleInvite = async () => {
  if (selectedUsers.value.length === 0) return
  inviting.value = true
  try {
    const userIds = selectedUsers.value.map(u => u.id)
    await addGroupMembers(props.groupId, userIds)
    ElMessage.success(`成功邀请 ${selectedUsers.value.length} 位成员`)
    selectedUsers.value = []
    emit('success')
    visible.value = false
  } catch (error) {
    console.error('邀请成员失败:', error)
    ElMessage.error('邀请失败')
  } finally {
    inviting.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) {
    searchQuery.value = ''
    selectedUsers.value = []
    userList.value = []
  }
})
</script>

<style scoped lang="scss">
.invite-member-dialog {
  :deep(.el-dialog__body) {
    padding: 20px 24px;
    max-height: 60vh;
    overflow: hidden;
  }

  :deep(.el-dialog__footer) {
    padding: 16px 24px;
    border-top: 1px solid var(--dt-border-light);
  }
}

.invite-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-section {
  .el-input {
    width: 100%;
  }
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--dt-text-secondary);
  margin-bottom: 12px;
}

.selected-section {
  .selected-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    padding: 12px;
    background: var(--dt-bg-body);
    border-radius: 8px;
    min-height: 44px;
  }
}

.users-section {
  flex: 1;
  overflow: hidden;

  .loading-state,
  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px;
    color: var(--dt-text-tertiary);

    .material-icons-outlined {
      font-size: 48px;
      margin-bottom: 12px;
      opacity: 0.5;
    }

    p {
      font-size: 14px;
    }
  }

  .user-list {
    max-height: 300px;
    overflow-y: auto;
    padding-right: 8px;

    .user-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s;

      &:hover:not(.disabled) {
        background: var(--dt-bg-body);
      }

      &.selected {
        background: var(--dt-brand-lighter);
      }

      &.disabled {
        opacity: 0.6;
        cursor: not-allowed;
      }

      .user-info {
        flex: 1;

        .user-name {
          font-size: 14px;
          font-weight: 500;
          color: var(--dt-text-primary);
        }

        .user-dept {
          font-size: 12px;
          color: var(--dt-text-tertiary);
          margin-top: 2px;
        }
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dark {
  .invite-member-dialog {
    :deep(.el-dialog__footer) {
      border-top-color: var(--dt-border-dark);
    }
  }

  .selected-section .selected-list {
    background: var(--dt-bg-hover-dark);
  }

  .users-section {
    .user-list .user-item {
      &:hover:not(.disabled) {
        background: var(--dt-bg-hover-dark);
      }

      &.selected {
        background: rgba(22, 119, 255, 0.2);
      }

      .user-info {
        .user-name {
          color: var(--dt-text-primary-dark);
        }

        .user-dept {
          color: var(--dt-text-tertiary-dark);
        }
      }
    }
  }
}
</style>
