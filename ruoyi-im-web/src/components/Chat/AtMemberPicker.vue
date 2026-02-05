<template>
  <el-dialog
    v-model="visible"
    title="选择提醒的人"
    width="400px"
    class="at-member-picker"
    append-to-body
    @open="loadMembers"
  >
    <div class="search-box">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索成员"
        :prefix-icon="Search"
        clearable
        size="small"
      />
    </div>
    
    <div
      v-loading="loading"
      class="member-list"
    >
      <div
        class="member-item all"
        @click="handleSelect({ id: 'all', name: '所有人' })"
      >
        <div class="avatar-all">
          @
        </div>
        <span class="name">所有人</span>
      </div>
      
      <div
        v-for="member in filteredMembers"
        :key="member.id"
        class="member-item"
        @click="handleSelect(member)"
      >
        <DingtalkAvatar
          :src="member.avatar"
          :name="member.nickname || member.username"
          :user-id="member.userId || member.id"
          :size="32"
          shape="circle"
        />
        <span class="name">{{ member.nickname || member.username }}</span>
      </div>
      
      <el-empty
        v-if="!loading && filteredMembers.length === 0"
        description="未找到成员"
      />
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getGroupMembers } from '@/api/im/group'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  sessionId: [String, Number]
})

const emit = defineEmits(['select'])

const visible = ref(false)
const loading = ref(false)
const searchKeyword = ref('')
const members = ref([])

const loadMembers = async () => {
  if (!props.sessionId) {return}
  loading.value = true
  try {
    const res = await getGroupMembers(props.sessionId)
    if (res.code === 200) {
      members.value = res.data
    }
  } catch (error) {
    console.error('加载成员失败', error)
  } finally {
    loading.value = false
  }
}

const filteredMembers = computed(() => {
  const keyword = searchKeyword.value.toLowerCase()
  if (!keyword) {return members.value}
  return members.value.filter(m => 
    (m.nickname && m.nickname.toLowerCase().includes(keyword)) ||
    (m.username && m.username.toLowerCase().includes(keyword))
  )
})

const handleSelect = member => {
  emit('select', member)
  visible.value = false
}

const open = () => {
  visible.value = true
}

defineExpose({ open })
</script>

<style scoped lang="scss">
.at-member-picker {
  :deep(.el-dialog__body) {
    padding: 10px 0;
  }
}

.search-box {
  padding: 0 16px 10px;
}

.member-list {
  max-height: 400px;
  overflow-y: auto;
  
  .member-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 16px;
    cursor: pointer;
    transition: background 0.2s;
    
    &:hover {
      background-color: #f5f5f5;
    }
    
    .avatar-all {
      width: 32px;
      height: 32px;
      background-color: #0089ff;
      color: #fff;
      border-radius: var(--dt-radius-full);
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: bold;
    }
    
    .name {
      font-size: 14px;
      color: #262626;
    }
  }
}
</style>
