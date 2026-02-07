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
        <span
          class="name"
          v-html="highlightText(member.nickname || member.username)"
        />
      </div>

      <el-empty
        v-if="!loading && filteredMembers.length === 0"
        description="未找到成员"
      />
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
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
const debouncedKeyword = ref('') // 防抖后的搜索关键词

// 防抖定时器
let debounceTimer = null

// 监听搜索关键词变化,应用防抖
watch(searchKeyword, newVal => {
  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }

  debounceTimer = setTimeout(() => {
    debouncedKeyword.value = newVal
  }, 150) // 150ms 防抖延迟
})

const loadMembers = async () => {
  if (!props.sessionId) { return }
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
  const keyword = debouncedKeyword.value.toLowerCase() // 使用防抖后的关键词
  if (!keyword) { return members.value }
  return members.value.filter(m =>
    (m.nickname && m.nickname.toLowerCase().includes(keyword)) ||
    (m.username && m.username.toLowerCase().includes(keyword))
  )
})

// 高亮搜索关键词
const highlightText = text => {
  if (!text || !debouncedKeyword.value) {return text}

  const keyword = debouncedKeyword.value
  const regex = new RegExp(`(${keyword})`, 'gi')
  return text.replace(regex, '<span class="highlight">$1</span>')
}

const handleSelect = member => {
  emit('select', member)
  visible.value = false
  searchKeyword.value = '' // 清空搜索
  debouncedKeyword.value = ''
}

const open = () => {
  visible.value = true
}

defineExpose({ open })
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

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
    transition: background var(--dt-transition-fast);

    &:hover {
      background-color: #f5f5f5; // 野火IM: 浅灰Hover
    }

    .avatar-all {
      width: 32px;
      height: 32px;
      background-color: #4168e0; // 野火IM: 品牌蓝
      color: #fff;
      border-radius: var(--dt-radius-full);
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: bold;
    }

    .name {
      font-size: 14px;
      color: var(--dt-text-primary);

      // 搜索高亮样式
      :deep(.highlight) {
        background-color: rgba(255, 235, 59, 0.4); // 黄色高亮
        color: var(--dt-text-primary);
        font-weight: 600;
        padding: 0 2px;
        border-radius: 2px;
      }
    }
  }
}

// 暗色模式
:global(.dark) {
  .member-list {
    .member-item {
      &:hover {
        background-color: var(--dt-bg-hover-dark);
      }

      .name {
        color: var(--dt-text-primary-dark);

        :deep(.highlight) {
          background-color: rgba(255, 235, 59, 0.3);
          color: var(--dt-text-primary-dark);
        }
      }
    }
  }
}
</style>
