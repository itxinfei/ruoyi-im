<template>
  <el-dialog
    v-model="visible"
    title="消息接收详情"
    width="440px"
    class="message-read-dialog"
    append-to-body
    destroy-on-close
  >
    <div class="read-detail-container">
      <el-tabs v-model="activeTab" class="read-tabs">
        <el-tab-pane :label="`未读 (${unreadList.length})`" name="unread">
          <div class="user-list custom-scrollbar">
            <div v-for="u in unreadList" :key="u.userId" class="user-row">
              <DingtalkAvatar
                :src="u.avatar"
                :name="u.nickname"
                :user-id="u.userId"
                :size="32"
                shape="square"
              />
              <span class="user-name">{{ u.nickname || u.username }}</span>
              <el-button
                v-if="isAdmin"
                size="small"
                link
                type="primary"
                @click="handleDing(u)"
              >
                DING一下
              </el-button>
            </div>
            <ImEmpty
              v-if="unreadList.length === 0"
              title="全部已读"
              :full="false"
              description="看来大家都收到了消息"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane :label="`已读 (${readList.length})`" name="read">
          <div class="user-list custom-scrollbar">
            <div v-for="u in readList" :key="u.userId" class="user-row">
              <DingtalkAvatar
                :src="u.avatar"
                :name="u.nickname"
                :user-id="u.userId"
                :size="32"
                shape="square"
              />
              <div class="user-info-col">
                <span class="user-name">{{ u.nickname || u.username }}</span>
                <span class="read-time">{{ formatReadTime(u.readTime) }}</span>
              </div>
            </div>
            <ImEmpty v-if="readList.length === 0" title="没人阅读" :full="false" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { getMessageReadDetail } from '@/api/im/message'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import ImEmpty from '@/components/Common/ImEmpty.vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: Boolean,
  messageId: [String, Number],
  conversationId: [String, Number]
})

const emit = defineEmits(['update:modelValue'])

const visible = ref(false)
const activeTab = ref('unread')
const readList = ref([])
const unreadList = ref([])
const loading = ref(false)
const isAdmin = ref(false) // 模拟管理权限

const loadData = async () => {
  if (!props.messageId || !props.conversationId) return
  loading.value = true
  try {
    const res = await getMessageReadDetail(props.conversationId, props.messageId)
    if (res.code === 200) {
      readList.value = res.data.readUsers || []
      unreadList.value = res.data.unreadUsers || []
    }
  } catch (e) {
    ElMessage.error('加载阅读详情失败')
  } finally {
    loading.value = false
  }
}

const formatReadTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')} 已读`
}

const handleDing = (user) => {
  ElMessage.success(`已向 ${user.nickname} 发起 DING 提醒`)
}

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) loadData()
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})
</script>

<style scoped lang="scss">
.message-read-dialog {
  :deep(.el-dialog__body) { padding: 0; }
}

.read-detail-container {
  height: 480px; display: flex; flex-direction: column;

  .read-tabs {
    :deep(.el-tabs__header) { margin: 0; padding: 0 20px; border-bottom: 1px solid var(--dt-border-light); }
    :deep(.el-tabs__nav-wrap::after) { display: none; }
    :deep(.el-tabs__item) { height: 50px; line-height: 50px; font-size: 14px; }
  }
}

.user-list {
  flex: 1; overflow-y: auto; padding: 12px 0;
  height: 400px;
}

.user-row {
  display: flex; align-items: center; gap: 12px; padding: 10px 20px;
  transition: background 0.2s; cursor: default;
  &:hover { background: var(--dt-bg-session-hover); }

  .user-name { font-size: 14px; color: var(--dt-text-primary); flex: 1; @include text-ellipsis; }

  .user-info-col {
    flex: 1; display: flex; flex-direction: column; gap: 2px;
    .read-time { font-size: 11px; color: var(--dt-text-tertiary); }
  }
}
</style>
