<template>
  <el-dialog
    v-model="visible"
    title="邀请成员"
    width="600px"
    @close="handleClose"
  >
    <div class="invite-member">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索用户"
        :prefix-icon="Search"
        class="search-input"
        clearable
      />
      
      <div class="user-list">
        <div
          v-for="user in filteredUsers"
          :key="user.id"
          class="user-item"
          :class="{ selected: selectedUsers.some(u => u.id === user.id) }"
          @click="toggleUser(user)"
        >
          <DtAvatar
            :name="user.name || user.username"
            :avatar="user.avatar"
            :size="40"
            class="user-avatar"
          />
          <div class="user-info">
            <div class="user-name">{{ user.name || user.username }}</div>
            <div class="user-desc">{{ user.department || '暂无部门' }}</div>
          </div>
          <el-checkbox
            :model-value="selectedUsers.some(u => u.id === user.id)"
            @change="checked => handleUserSelect(user, checked)"
          />
        </div>
      </div>
      
      <div v-if="filteredUsers.length === 0" class="empty-state">
        <el-empty description="暂无用户" />
      </div>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <span class="selected-count">已选择 {{ selectedUsers.length }} 人</span>
        <div>
          <el-button @click="handleClose">取消</el-button>
          <el-button 
            type="primary" 
            :disabled="selectedUsers.length === 0"
            :loading="inviting"
            @click="handleInvite"
          >
            邀请
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import DtAvatar from '@/components/DtAvatar.vue'
import { useStore } from 'vuex'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  groupId: {
    type: [String, Number],
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const store = useStore()
const searchKeyword = ref('')
const selectedUsers = ref([])
const inviting = ref(false)
const allUsers = ref([])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const filteredUsers = computed(() => {
  if (!searchKeyword.value) return allUsers.value
  
  const keyword = searchKeyword.value.toLowerCase()
  return allUsers.value.filter(user => 
    (user.name && user.name.toLowerCase().includes(keyword)) ||
    (user.username && user.username.toLowerCase().includes(keyword)) ||
    (user.department && user.department.toLowerCase().includes(keyword))
  )
})

function toggleUser(user) {
  const index = selectedUsers.value.findIndex(u => u.id === user.id)
  if (index > -1) {
    selectedUsers.value.splice(index, 1)
  } else {
    selectedUsers.value.push(user)
  }
}

function handleUserSelect(user, checked) {
  if (checked) {
    if (!selectedUsers.value.some(u => u.id === user.id)) {
      selectedUsers.value.push(user)
    }
  } else {
    const index = selectedUsers.value.findIndex(u => u.id === user.id)
    if (index > -1) {
      selectedUsers.value.splice(index, 1)
    }
  }
}

async function loadUsers() {
  try {
    // 这里应该调用获取用户列表的API
    // const response = await store.dispatch('im/getUserList')
    // allUsers.value = response.data || []
    
    // 临时模拟数据
    allUsers.value = [
      { id: 1, name: '张三', username: 'zhangsan', department: '技术部', avatar: '' },
      { id: 2, name: '李四', username: 'lisi', department: '产品部', avatar: '' },
      { id: 3, name: '王五', username: 'wangwu', department: '设计部', avatar: '' },
      { id: 4, name: '赵六', username: 'zhaoliu', department: '运营部', avatar: '' },
    ]
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  }
}

async function handleInvite() {
  if (selectedUsers.value.length === 0) {
    ElMessage.warning('请选择要邀请的用户')
    return
  }
  
  inviting.value = true
  
  try {
    // 调用邀请成员的API
    const userIds = selectedUsers.value.map(u => u.id)
    
    // await store.dispatch('im/inviteGroupMembers', {
    //   groupId: props.groupId,
    //   userIds
    // })
    
    // 临时模拟
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success(`已邀请 ${selectedUsers.value.length} 位成员`)
    emit('success')
    handleClose()
  } catch (error) {
    console.error('邀请失败:', error)
    ElMessage.error('邀请失败：' + error.message)
  } finally {
    inviting.value = false
  }
}

function handleClose() {
  selectedUsers.value = []
  searchKeyword.value = ''
  visible.value = false
}

onMounted(() => {
  if (visible.value) {
    loadUsers()
  }
})
</script>

<style scoped>
.invite-member {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-input {
  margin-bottom: 8px;
}

.user-list {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 8px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.user-item:hover {
  background: #f5f5f5;
}

.user-item.selected {
  background: #e6f7ff;
  border: 1px solid #91d5ff;
}

.user-avatar {
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #262626;
}

.user-desc {
  font-size: 12px;
  color: #8c8c8c;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.dialog-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.selected-count {
  font-size: 14px;
  color: #595959;
}

/* 滚动条样式 */
.user-list::-webkit-scrollbar {
  width: 6px;
}

.user-list::-webkit-scrollbar-track {
  background: transparent;
}

.user-list::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.user-list::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}
</style>