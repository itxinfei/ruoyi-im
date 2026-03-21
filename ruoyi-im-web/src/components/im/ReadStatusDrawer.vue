<template>
  <el-drawer
    v-model="visible"
    title="消息接收详情"
    size="320px"
    direction="rtl"
    :with-header="false"
    class="read-status-drawer"
  >
    <header class="drawer-header">
      <span class="title">消息接收详情</span>
      <el-icon class="close-icon" @click="visible = false"><Close /></el-icon>
    </header>

    <div class="drawer-content">
      <el-tabs v-model="activeTab" class="status-tabs">
        <el-tab-pane :label="`未读 (${unreadUsers.length})`" name="unread">
          <div class="user-list">
            <div v-for="user in unreadUsers" :key="user.userId" class="user-item">
              <DingtalkAvatar :src="user.avatar" :name="user.nickname" :size="32" shape="square" />
              <span class="user-name">{{ user.nickname }}</span>
            </div>
            <div v-if="unreadUsers.length === 0" class="empty-text">全部已读</div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane :label="`已读 (${readUsers.length})`" name="read">
          <div class="user-list">
            <div v-for="user in readUsers" :key="user.userId" class="user-item">
              <DingtalkAvatar :src="user.avatar" :name="user.nickname" :size="32" shape="square" />
              <div class="user-info">
                <span class="user-name">{{ user.nickname }}</span>
                <span class="read-time">{{ formatTime(user.readTime) }}</span>
              </div>
            </div>
            <div v-if="readUsers.length === 0" class="empty-text">暂无已读记录</div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Close } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { getMessageReadDetail } from '@/api/im/message'

const props = defineProps({
  modelValue: Boolean,
  messageId: [String, Number]
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const activeTab = ref('unread')
const readUsers = ref([])
const unreadUsers = ref([])
const loading = ref(false)

const fetchData = async () => {
  if (!props.messageId) return
  loading.value = true
  try {
    const res = await getMessageReadDetail(props.messageId)
    if (res.code === 200 && res.data) {
      readUsers.value = res.data.readUsers || []
      unreadUsers.value = res.data.unreadUsers || []
    }
  } catch (e) {
    console.error('获取已读详情失败', e)
  } finally {
    loading.value = false
  }
}

watch(() => props.messageId, fetchData, { immediate: true })

const formatTime = (ts) => {
  if (!ts) return ''
  const d = new Date(ts)
  return `${d.getHours()}:${d.getMinutes().toString().padStart(2, '0')} 已读`
}
</script>

<style scoped>
.drawer-header {
  height: 60px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--dt-border-light);
}
.title { font-size: 16px; font-weight: 600; color: var(--dt-text-primary); }
.close-icon { cursor: pointer; color: var(--dt-text-tertiary); font-size: 16px; }
.close-icon:hover { color: var(--dt-text-primary); }
.drawer-content { height: calc(100% - 60px); }

.status-tabs :deep(.el-tabs__nav-wrap) { padding: 0 20px; }

.user-list { padding: 10px 20px; }
.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 48px;
}
.user-name { font-size: 14px; color: var(--dt-text-primary); }
.user-info { display: flex; flex-direction: column; gap: 2px; }
.read-time { font-size: 11px; color: var(--dt-text-tertiary); }
.empty-text { text-align: center; color: var(--dt-text-tertiary); margin-top: 40px; font-size: 13px; }
</style>
