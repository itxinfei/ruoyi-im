<template>
  <el-dialog
    title="添加联系人"
    :model-value="visible"
    width="500px"
    @update:model-value="$emit('update:visible', $event)"
  >
    <div class="search-section">
      <el-input
        v-model="keyword"
        placeholder="输入用户名或昵称搜索"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button :icon="Search" @click="handleSearch" :loading="searching" />
        </template>
      </el-input>
    </div>

    <div class="result-list" v-loading="searching">
      <div v-if="hasSearched && results.length === 0" class="empty-result">
        未找到相关用户
      </div>
      <div
        v-for="user in results"
        :key="user.id"
        class="user-item"
      >
        <div class="user-avatar">
          <img v-if="user.avatar" :src="user.avatar" />
          <div v-else class="avatar-placeholder">
            {{ (user.nickname || user.username || '?').charAt(0) }}
          </div>
        </div>
        <div class="user-info">
          <div class="user-name">{{ user.nickname || user.username }}</div>
          <div class="user-meta">
            {{ user.department || '未知部门' }} · {{ user.position || '未知职位' }}
          </div>
        </div>
        <el-button
          v-if="!user.isFriend"
          type="primary"
          size="small"
          @click="handleAdd(user)"
        >
          添加
        </el-button>
        <span v-else class="is-friend">已添加</span>
      </div>
    </div>

    <el-dialog
      v-model="showRequestDialog"
      title="发送好友申请"
      width="400px"
      append-to-body
    >
      <el-form>
        <el-form-item label="验证信息">
          <el-input
            v-model="requestMessage"
            type="textarea"
            placeholder="我是..."
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="分组">
           <el-select v-model="selectedGroup" placeholder="选择分组" style="width: 100%">
             <el-option label="默认分组" value="默认分组" />
             <el-option label="同事" value="同事" />
             <el-option label="朋友" value="朋友" />
           </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRequestDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAdd" :loading="sending">发送</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { searchUsers } from '@/api/im/user'
import { sendFriendRequest } from '@/api/im/contact'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: Boolean
})

const emit = defineEmits(['update:visible'])

const keyword = ref('')
const results = ref([])
const searching = ref(false)
const hasSearched = ref(false)

const showRequestDialog = ref(false)
const requestMessage = ref('')
const selectedGroup = ref('默认分组')
const targetUser = ref(null)
const sending = ref(false)

const handleSearch = async () => {
  if (!keyword.value.trim()) return
  searching.value = true
  hasSearched.value = true
  try {
    const res = await searchUsers(keyword.value)
    if (res.code === 200) {
      results.value = res.data
    }
  } catch (error) {
    console.error(error)
  } finally {
    searching.value = false
  }
}

const handleAdd = (user) => {
  targetUser.value = user
  requestMessage.value = `我是${user.nickname || ''}`
  showRequestDialog.value = true
}

const confirmAdd = async () => {
  if (!targetUser.value) return
  sending.value = true
  try {
    await sendFriendRequest({
      targetUserId: targetUser.value.id,
      message: requestMessage.value,
      groupName: selectedGroup.value
    })
    ElMessage.success('申请已发送')
    showRequestDialog.value = false
    // Mark as pending or refresh list?
    // For simplicity, just close dialog or keep searching
  } catch (error) {
    ElMessage.error(error.message || '发送失败')
  } finally {
    sending.value = false
  }
}
</script>

<style scoped>
.search-section {
  margin-bottom: 20px;
}

.result-list {
  max-height: 300px;
  overflow-y: auto;
}

.user-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.user-avatar {
  width: 40px;
  height: 40px;
  margin-right: 12px;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

.user-info {
  flex: 1;
}

.user-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.user-meta {
  font-size: 12px;
  color: #999;
}

.empty-result {
  text-align: center;
  color: #999;
  padding: 20px;
}

.is-friend {
  font-size: 12px;
  color: #999;
}
</style>
