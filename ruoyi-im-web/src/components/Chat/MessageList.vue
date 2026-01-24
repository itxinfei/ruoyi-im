<template>
  <div class="message-list" ref="listRef" @scroll="handleScroll">
    <!-- 加载中状态 -->
    <div v-if="loading" class="loading-wrapper">
      <el-icon class="is-loading"><Loading /></el-icon>数据加载中...
    </div>
    
    <!-- 空状态 -->
    <div v-else-if="messages.length === 0" class="empty">暂无消息</div>
    
    <!-- 消息内容 -->
    <template v-else>
      <div v-for="msg in messagesWithDividers" :key="msg.id || msg.timeText" :data-id="msg.id" class="message-wrapper">
        <!-- 时间分隔符 -->
        <div v-if="msg.isTimeDivider" class="time-divider">
          <span class="time-text">{{ msg.timeText }}</span>
        </div>

        <!-- 消息项组件 -->
        <MessageItem 
          v-else 
          :message="msg"
          @reply="$emit('reply', $event)"
          @reaction="handleReaction"
          @command="handleCommand"
          @scroll-to="scrollToMsg"
        >
          <!-- 消息气泡内容插槽 -->
          <template #bubble>
            <MessageBubble 
              :message="msg" 
              @command="handleCommand($event, msg)"
              @preview="previewImage"
              @download="downloadFile"
            />
          </template>

          <!-- 已读状态插槽 -->
          <template #read-status>
            <span v-if="msg.isRead" class="read">已读</span>
            <el-popover
              v-else-if="msg.readCount > 0"
              placement="top"
              :width="200"
              trigger="hover"
              @before-enter="fetchReadUsers(msg)"
            >
              <template #reference>
                <span class="read-count">{{ msg.readCount }}人已读</span>
              </template>
              <div v-loading="loadingReadUsers[msg.id]" class="read-users-list">
                <div v-for="user in readUsersMap[msg.id]" :key="user.id" class="read-user-item">
                  <el-avatar :size="24" :src="addTokenToUrl(user.avatar)">{{ (user.name?.charAt(0) || '?').toUpperCase() }}</el-avatar>
                  <span>{{ user.name }}</span>
                </div>
                <div v-if="!loadingReadUsers[msg.id] && (!readUsersMap[msg.id] || readUsersMap[msg.id].length === 0)" class="empty">
                  加载中...
                </div>
              </div>
            </el-popover>
            <span v-else class="unread">未读</span>
          </template>
        </MessageItem>
      </div>
    </template>

    <!-- 回到底部按钮 -->
    <Transition name="fade">
      <div v-if="showScrollToBottom" class="scroll-to-bottom-btn" @click="scrollToBottom()">
        <el-icon><ArrowDown /></el-icon>
        <span>回到最新</span>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { computed, ref, nextTick, watch } from 'vue'
import { Loading, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMessageReadUsers } from '@/api/im/message'
import MessageItem from './MessageItem.vue'
import MessageBubble from './MessageBubble.vue'
import { addTokenToUrl } from '@/utils/file'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  currentUser: Object,
  loading: Boolean,
  sessionId: [String, Number]
})

const emit = defineEmits(['delete', 'recall', 'reply', 'load-more', 'edit'])

const listRef = ref(null)
const readUsersMap = ref({})
const loadingReadUsers = ref({})
const showScrollToBottom = ref(false)

// 获取已读用户列表
const fetchReadUsers = async (msg) => {
  if (readUsersMap.value[msg.id] || loadingReadUsers.value[msg.id]) return
  
  loadingReadUsers.value[msg.id] = true
  try {
    const res = await getMessageReadUsers(props.sessionId, msg.id)
    if (res.code === 200) {
      readUsersMap.value[msg.id] = res.data
    }
  } catch (error) {
    console.error('获取已读用户失败', error)
  } finally {
    loadingReadUsers.value[msg.id] = false
  }
}

// 图片预览
const previewImage = (url) => {
  // 实际项目中应调用全局预览组件
  window.open(url, '_blank')
}

// 下载文件
const downloadFile = (fileInfo) => {
  if (!fileInfo.fileUrl) return
  window.open(fileInfo.fileUrl, '_blank')
}

// 格式化时间分隔符文案
const formatTimeDivider = (timestamp) => {
  const date = new Date(timestamp)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const msgDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())

  const diffDays = Math.floor((today - msgDate) / (1000 * 60 * 60 * 24))

  const timeStr = date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })

  if (diffDays === 0) return `今天 ${timeStr}`
  if (diffDays === 1) return `昨天 ${timeStr}`
  if (diffDays < 7) {
    const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
    return `${weekDays[date.getDay()]} ${timeStr}`
  }

  return `${date.getMonth() + 1}月${date.getDate()}日 ${timeStr}`
}

// 计算带时间分割线的消息列表
const messagesWithDividers = computed(() => {
  const res = []
  props.messages.forEach((msg, index) => {
    let showDivider = false
    if (index === 0) {
      showDivider = true
    } else {
      const prevMsg = props.messages[index - 1]
      const timeDiff = msg.timestamp - prevMsg.timestamp
      // 30分钟间隔或每10条消息显示一次时间
      if (timeDiff > 30 * 60 * 1000 || index % 10 === 0) {
        showDivider = true
      }
    }

    if (showDivider) {
      res.push({ 
        isTimeDivider: true, 
        timeText: formatTimeDivider(msg.timestamp), 
        id: 'divider-' + msg.timestamp + '-' + index 
      })
    }
    res.push(msg)
  })
  return res
})

// 滚动到底部
const scrollToBottom = (smooth = true) => {
  nextTick(() => {
    if (listRef.value) {
      listRef.value.scrollTo({
        top: listRef.value.scrollHeight,
        behavior: smooth ? 'smooth' : 'auto'
      })
    }
  })
}

// 监听消息变化自动滚动
watch(() => props.messages.length, () => scrollToBottom())

// 处理菜单命令
const handleCommand = (cmd, msg) => {
  if (cmd === 'copy') {
    navigator.clipboard.writeText(msg.content)
    ElMessage.success('已复制')
  } else if (cmd === 'reply') {
    emit('reply', msg)
  } else if (cmd === 'recall') {
    emit('recall', msg.id)
  } else if (cmd === 'delete') {
    ElMessageBox.confirm('确定删除这条消息吗？', '提示', {
      type: 'warning'
    }).then(() => {
      emit('delete', msg.id)
    })
  } else if (cmd === 'edit') {
    emit('edit', msg)
  }
}

// 处理表情反应
const handleReaction = (msg, reaction) => {
  ElMessage.success(`你对消息做出了 ${reaction} 反应`)
}

// 滚动到指定消息
const scrollToMsg = (id) => {
  const el = listRef.value?.querySelector(`[data-id="${id}"]`)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth' })
  }
}

// 监听滚动事件
const handleScroll = () => {
  if (!listRef.value || props.loading) return
  
  const { scrollTop, clientHeight, scrollHeight } = listRef.value
  
  // 滚动到顶部加载更多
  if (scrollTop === 0) {
    emit('load-more')
  }
  
  // 检测是否接近底部
  const distanceFromBottom = scrollHeight - scrollTop - clientHeight
  showScrollToBottom.value = distanceFromBottom > 300
}

// 保持滚动偏移（用于加载更多）
const maintainScroll = (oldHeight) => {
  nextTick(() => {
    if (listRef.value) {
      listRef.value.scrollTop = listRef.value.scrollHeight - oldHeight
    }
  })
}

defineExpose({ scrollToBottom, maintainScroll })
</script>

<style scoped lang="scss">
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f7f8fa;
  position: relative;
}

.loading-wrapper {
  text-align: center;
  padding: 20px;
  color: #94a3b8;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.empty {
  text-align: center;
  padding: 40px;
  color: #bfbfbf;
  font-size: 13px;
}

.time-divider {
  text-align: center;
  margin: 20px 0;
  color: #b3b3b3;
  font-size: 12px;
  line-height: 1;
  
  .time-text {
    background: rgba(0, 0, 0, 0.05);
    padding: 4px 12px;
    border-radius: 10px;
    display: inline-block;
  }
}

.read-status {
  font-size: 11px;
  cursor: default;

  .read { color: #bfbfbf; }
  .unread { color: #1677ff; }
  .read-count {
    color: #1677ff;
    cursor: pointer;
    &:hover { text-decoration: underline; }
  }
}

.read-users-list {
  max-height: 200px;
  overflow-y: auto;
  
  .read-user-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    font-size: 13px;
    
    &:hover { background-color: #f8fafc; }
    &:not(:last-child) { border-bottom: 1px solid #f1f5f9; }
  }
}

.scroll-to-bottom-btn {
  position: absolute;
  right: 20px;
  bottom: 20px;
  background: #fff;
  border: 1px solid #e6e6e6;
  border-radius: 20px;
  padding: 8px 16px;
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  font-size: 13px;
  color: #595959;
  z-index: 10;
  transition: all 0.2s;
  
  &:hover {
    background: #f8fafc;
    border-color: #1677ff;
    color: #1677ff;
  }
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>

<style lang="scss">
/* 全局样式用于气泡右键菜单 */
.message-context-menu {
  padding: 4px 0 !important;
  border-radius: 8px !important;
  box-shadow: var(--dt-shadow-lg) !important;
  border: 1px solid var(--dt-border-light) !important;

  .el-dropdown-menu__item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px !important;
    font-size: 13px !important;
    color: var(--dt-text-secondary) !important;

    .el-icon { font-size: 16px; color: var(--dt-text-tertiary); }

    &:hover {
      background-color: var(--dt-brand-bg) !important;
      color: var(--dt-brand-color) !important;
      .el-icon { color: var(--dt-brand-color); }
    }

    &.danger {
      color: var(--dt-error-color) !important;
      &:hover { background-color: #fff1f0 !important; }
      .el-icon { color: var(--dt-error-color); }
    }
  }
}
</style>
