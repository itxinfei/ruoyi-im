<template>
  <div class="message-list-viewport custom-scrollbar" ref="listRef" @scroll="handleScroll">
    <div v-if="loading" class="loading-overlay">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>正在同步消息...</span>
    </div>
    
    <div class="message-content-flow">
      <MessageItem
        v-for="(msg, index) in messagesWithDividers"
        :key="msg.id || index"
        :message="msg"
        @reply="(m) => $emit('reply', m)"
        @command="(c, m) => $emit('command', c, m)"
        @show-user="(uid) => $emit('show-user', uid)"
      >
        <template #bubble>
          <MessageBubble 
            :message="msg" 
            :session-type="sessionType"
            @preview="(url) => openImagePreview(url)"
          />
        </template>
      </MessageItem>
    </div>

    <!-- 底部悬浮按钮 -->
    <div v-if="showScrollToBottom" class="scroll-bottom-btn" @click="scrollToBottom">
      <el-badge :value="newMessagesCount" :hidden="newMessagesCount === 0" class="badge-item">
        <el-icon><ArrowDown /></el-icon>
      </el-badge>
      <span>有新消息</span>
    </div>

    <ImagePreviewDialog v-model:visible="showImagePreview" :image-list="imagePreviewList" :initial-index="imagePreviewIndex" />

    <!-- 消息已读详情对话框 -->
    <MessageReadDetailDialog
      v-model:visible="showReadDetail"
      :message-id="currentMessageId"
      :conversation-id="conversationId"
      :sender-id="currentSenderId"
    />
  </div>
</template>

<script setup>
import { computed, ref, nextTick, onMounted, watch } from 'vue'
import { Loading, ArrowDown } from '@element-plus/icons-vue'
import MessageItem from './MessageItem.vue'
import MessageBubble from './MessageBubble.vue'
import ImagePreviewDialog from '@/components/Common/ImagePreviewDialog.vue'
import MessageReadDetailDialog from './MessageReadDetailDialog.vue'

const props = defineProps({ messages: Array, loading: Boolean, sessionType: String, conversationId: Number })
const emit = defineEmits(['command', 'reply', 'load-more', 'show-user'])

const listRef = ref(null)
const showScrollToBottom = ref(false)
const newMessagesCount = ref(0)
const showImagePreview = ref(false)
const imagePreviewList = ref([])
const imagePreviewIndex = ref(0)

// 已读详情相关
const showReadDetail = ref(false)
const currentMessageId = ref(null)
const currentSenderId = ref(null)

// 处理命令
const handleCommand = (command, message) => {
  if (command === 'read-detail') {
    currentMessageId.value = message.id
    currentSenderId.value = message.senderId
    showReadDetail.value = true
  } else {
    emit('command', command, message)
  }
}

const messagesWithDividers = computed(() => {
  const result = []
  if (!props.messages || props.messages.length === 0) return result
  
  props.messages.forEach((m, i) => {
    // 自动插入时间分隔线 (间隔超过10分钟)
    if (i === 0 || m.timestamp - props.messages[i-1].timestamp > 10 * 60 * 1000) {
      const date = new Date(m.timestamp)
      const timeStr = date.getHours().toString().padStart(2, '0') + ':' + date.getMinutes().toString().padStart(2, '0')
      result.push({ 
        id: 'time-' + m.timestamp,
        isTimeDivider: true, 
        timeText: timeStr 
      })
    }
    result.push(m)
  })
  return result
})

// 滚动到底部
const scrollToBottom = () => {
  newMessagesCount.value = 0
  nextTick(() => {
    if (listRef.value) {
      listRef.value.scrollTop = listRef.value.scrollHeight
    }
  })
}

const handleScroll = (e) => {
  const { scrollTop, scrollHeight, clientHeight } = e.target
  // 距离底部超过200px显示回到最新按钮
  showScrollToBottom.value = scrollHeight - scrollTop - clientHeight > 200
  // 到达顶部加载更多
  if (scrollTop === 0 && !props.loading) {
    emit('load-more')
  }
}

const openImagePreview = (url) => {
  imagePreviewList.value = [url]
  imagePreviewIndex.value = 0
  showImagePreview.value = true
}

watch(() => props.messages.length, (n, o) => {
  // 如果是新消息且用户在底部，自动滚动
  if (n > o && !showScrollToBottom.value) {
    scrollToBottom()
  } else if (n > o) {
    newMessagesCount.value++
  }
})

onMounted(() => {
  scrollToBottom()
})

defineExpose({ scrollToBottom })
</script>

<style scoped lang="scss">
.message-list-viewport {
  flex: 1;
  overflow-y: auto;
  background: #f4f6f8;
  position: relative;
  scroll-behavior: smooth;
  padding: 10px 0;
}

.message-content-flow {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.scroll-bottom-btn {
  position: absolute;
  right: 24px;
  bottom: 24px;
  background: #ffffff;
  padding: 8px 16px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  color: #1677ff;
  font-size: 13px;
  font-weight: 500;
  z-index: 100;
  transition: all 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(22, 119, 255, 0.2);
  }
}

.loading-overlay {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  color: #8f959e;
  font-size: 12px;
  background: rgba(255, 255, 255, 0.8);
  position: sticky;
  top: 0;
  z-index: 10;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: transparent;
  border-radius: 3px;
}

.message-list-viewport:hover.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.1);
}
</style>
