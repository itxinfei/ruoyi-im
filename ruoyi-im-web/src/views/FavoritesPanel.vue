<template>
  <div class="favorites-panel">
    <div class="panel-header">
      <h2 class="panel-title">
        我的收藏
      </h2>
      <div class="header-actions">
        <div class="category-tabs">
          <span
            v-for="cat in categories"
            :key="cat.value"
            class="category-tab"
            :class="{ active: activeCategory === cat.value }"
            @click="activeCategory = cat.value"
          >
            {{ cat.label }}
          </span>
        </div>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索收藏"
          prefix-icon="Search"
          clearable
          class="search-input"
        />
      </div>
    </div>

    <div class="panel-content">
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading">
          <Loading />
        </el-icon>
        <span>加载中...</span>
      </div>

      <div v-else-if="filteredFavorites.length === 0" class="empty-state">
        <span class="material-icons-outlined empty-icon">star_outline</span>
        <p class="empty-text">
          {{ searchKeyword ? '没有找到匹配的收藏' : '暂无收藏消息' }}
        </p>
        <p class="empty-hint">
          在聊天中右键消息选择"收藏"即可添加
        </p>
      </div>

      <div v-else class="favorites-list">
        <div
          v-for="item in filteredFavorites"
          :key="item.id"
          class="favorite-item"
          @click="handleJumpToMessage(item)"
        >
          <div class="favorite-icon" :class="getTypeClass(item.messageType)">
            <span class="material-icons-outlined">{{ getTypeIcon(item.messageType) }}</span>
          </div>
          <div class="favorite-content">
            <div class="favorite-title">
              {{ item.contentPreview || getMessagePreview(item) }}
            </div>
            <div class="favorite-meta">
              <span class="meta-source">{{ item.conversationName || '未知会话' }}</span>
              <span class="meta-divider">·</span>
              <span class="meta-time">{{ formatTime(item.createTime) }}</span>
            </div>
          </div>
          <div class="favorite-actions">
            <el-tooltip content="取消收藏">
              <button class="action-btn" @click.stop="handleRemoveFavorite(item)">
                <span class="material-icons-outlined">star</span>
              </button>
            </el-tooltip>
            <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, item)">
              <button class="action-btn">
                <span class="material-icons-outlined">more_horiz</span>
              </button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="copy">
                    复制内容
                  </el-dropdown-item>
                  <el-dropdown-item command="forward">
                    转发
                  </el-dropdown-item>
                  <el-dropdown-item command="remove" divided>
                    取消收藏
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>
    <ForwardDialog ref="forwardDialogRef" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getUserFavorites, removeMessageFavorite } from '@/api/im/message'
import ForwardDialog from '@/components/ForwardDialog/index.vue'

const store = useStore()
const router = useRouter()
const emit = defineEmits(['switch-module'])

const loading = ref(false)
const favorites = ref([])
const searchKeyword = ref('')
const activeCategory = ref('all')
const forwardDialogRef = ref(null)

const categories = [
  { label: '全部', value: 'all' },
  { label: '文本', value: 'TEXT' },
  { label: '图片', value: 'IMAGE' },
  { label: '文件', value: 'FILE' },
  { label: '语音', value: 'VOICE' },
  { label: '视频', value: 'VIDEO' }
]

const filteredFavorites = computed(() => {
  let result = favorites.value

  // 按分类过滤
  if (activeCategory.value !== 'all') {
    result = result.filter(item => item.messageType === activeCategory.value)
  }

  // 按关键词过滤
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(item => {
      const preview = item.contentPreview || getMessagePreview(item)
      const source = item.conversationName || ''
      return preview.toLowerCase().includes(keyword) || source.toLowerCase().includes(keyword)
    })
  }
  return result
})

const getTypeIcon = (type) => {
  const icons = {
    'TEXT': 'chat_bubble',
    'IMAGE': 'image',
    'FILE': 'description',
    'VOICE': 'mic',
    'VIDEO': 'videocam',
    'LINK': 'link'
  }
  return icons[type] || 'chat_bubble'
}

const getTypeClass = (type) => {
  const classes = {
    'TEXT': 'type-text',
    'IMAGE': 'type-image',
    'FILE': 'type-file',
    'VOICE': 'type-voice',
    'VIDEO': 'type-video',
    'LINK': 'type-link'
  }
  return classes[type] || 'type-text'
}

const getMessagePreview = (item) => {
  if (item.content) {
    // 截取前50个字符作为预览
    return item.content.length > 50 ? item.content.substring(0, 50) + '...' : item.content
  }
  if (item.messageType === 'IMAGE') return '[图片]'
  if (item.messageType === 'FILE') return `[文件] ${item.fileName || ''}`
  if (item.messageType === 'VOICE') return '[语音]'
  if (item.messageType === 'VIDEO') return '[视频]'
  return '[消息]'
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  // 一天内显示时间
  if (diff < 24 * 60 * 60 * 1000) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  // 一周内显示星期
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return days[date.getDay()]
  }
  // 其他显示日期
  return date.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' })
}

const loadFavorites = async () => {
  loading.value = true
  try {
    const res = await getUserFavorites()
    if (res.code === 200) {
      favorites.value = res.data || []
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  } catch (error) {
    console.error('加载收藏列表失败', error)
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleJumpToMessage = (item) => {
  // 跳转到对应会话并定位消息
  if (item.conversationId) {
    store.commit('im/session/SET_CURRENT_SESSION', {
      id: item.conversationId,
      name: item.conversationName
    })
    // 切换到消息模块
    store.dispatch('im/session/selectSession', { id: item.conversationId })
    emit('switch-module', 'chat')
  }
}

const handleForward = (item) => {
  // 将收藏消息转换为普通消息格式（适配 ForwardDialog）
  const message = {
    id: item.messageId,
    messageId: item.messageId,
    conversationId: item.conversationId,
    type: item.messageType,
    content: item.content || item.contentPreview || '',
    senderId: item.senderId,
    senderName: item.senderName,
    fileName: item.fileName,
    fileSize: item.fileSize,
    fileUrl: item.fileUrl,
    thumbnailUrl: item.thumbnailUrl,
    duration: item.duration,
    createTime: item.createTime
  }
  forwardDialogRef.value?.open(message)
}

const handleRemoveFavorite = async (item) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏这条消息吗？', '取消收藏', {
      type: 'warning'
    })
    const res = await removeMessageFavorite(item.messageId)
    if (res.code === 200) {
      favorites.value = favorites.value.filter(f => f.id !== item.id)
      ElMessage.success('已取消收藏')
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch {
    // 用户取消
  }
}

const handleCommand = (command, item) => {
  switch (command) {
    case 'copy':
      navigator.clipboard.writeText(item.content || '')
      ElMessage.success('已复制到剪贴板')
      break
    case 'forward':
      handleForward(item)
      break
    case 'remove':
      handleRemoveFavorite(item)
      break
  }
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped lang="scss">
.favorites-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  flex: 1;
  min-width: 0;
  background: var(--dt-bg-body);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--dt-spacing-md, 16px) var(--dt-spacing-lg, 24px);
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);
  flex-shrink: 0;
}

.panel-title {
  font-size: var(--dt-font-size-lg, 18px);
  font-weight: var(--dt-font-weight-semibold, 600);
  color: var(--dt-text-primary);
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-md, 16px);

  .search-input {
    width: var(--dt-search-width-md, 200px);
  }
}

.category-tabs {
  display: flex;
  gap: var(--dt-spacing-xs, 4px);
}

.category-tab {
  padding: var(--dt-spacing-xs, 6px) var(--dt-spacing-sm, 10px);
  font-size: var(--dt-font-size-xs, 12px);
  color: var(--dt-text-secondary);
  cursor: pointer;
  border-radius: var(--dt-radius-sm, 4px);
  transition: all var(--dt-transition-fast);
  white-space: nowrap;

  &:hover {
    color: var(--dt-brand-color);
    background: var(--dt-bg-hover);
  }

  &.active {
    color: var(--dt-brand-color);
    background: var(--dt-brand-lighter);
    font-weight: var(--dt-font-weight-medium, 500);
  }
}

.panel-content {
  flex: 1;
  overflow-y: auto;
  padding: var(--dt-spacing-md, 16px);
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: var(--dt-empty-state-height, 300px);
  color: var(--dt-text-tertiary);
}

.empty-icon {
  font-size: var(--dt-icon-size-2xl, 64px);
  margin-bottom: var(--dt-spacing-md, 16px);
  color: var(--dt-border-color);
}

.empty-text {
  font-size: var(--dt-font-size-base, 16px);
  color: var(--dt-text-secondary);
  margin: 0;
}

.empty-hint {
  font-size: var(--dt-font-size-sm, 14px);
  color: var(--dt-text-tertiary);
  margin: var(--dt-spacing-sm, 8px) 0 0 0;
}

.favorites-list {
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-sm, 8px);
}

.favorite-item {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-md, 12px);
  padding: var(--dt-spacing-md, 14px) var(--dt-spacing-lg, 16px);
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg, 12px);
  cursor: pointer;
  transition: all var(--dt-transition-fast);
  border: 1px solid var(--dt-border-light);
}

.favorite-item:hover {
  box-shadow: var(--dt-shadow-card);
  transform: translateY(var(--dt-transform-y, -1px));
  border-color: var(--dt-brand-color);
}

.favorite-icon {
  width: var(--dt-avatar-size-lg, 44px);
  height: var(--dt-avatar-size-lg, 44px);
  border-radius: var(--dt-radius-lg, 10px);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .material-icons-outlined {
    font-size: var(--dt-icon-size-lg, 22px);
  }

  &.type-text {
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);
  }

  &.type-image {
    background: var(--dt-success-bg);
    color: var(--dt-success-color);
  }

  &.type-file {
    background: var(--dt-warning-bg);
    color: var(--dt-warning-color);
  }

  &.type-voice {
    background: var(--dt-info-bg);
    color: var(--dt-info-color);
  }

  &.type-video {
    background: var(--dt-error-bg);
    color: var(--dt-error-color);
  }

  &.type-link {
    background: var(--dt-brand-lighter);
    color: var(--dt-brand-color);
  }
}

.favorite-content {
  flex: 1;
  min-width: 0;
}

.favorite-title {
  font-size: var(--dt-font-size-sm, 14px);
  font-weight: var(--dt-font-weight-medium, 500);
  color: var(--dt-text-primary);
  margin-bottom: var(--dt-spacing-xs, 4px);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.favorite-meta {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-xs, 6px);
  font-size: var(--dt-font-size-xs, 12px);
  color: var(--dt-text-tertiary);
}

.meta-source {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta-divider {
  opacity: 0.5;
}

.favorite-actions {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-xs, 4px);
}

.action-btn {
  width: var(--dt-btn-size-sm, 32px);
  height: var(--dt-btn-size-sm, 32px);
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: var(--dt-text-tertiary);
  border-radius: var(--dt-radius-md, 8px);
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  .material-icons-outlined {
    font-size: var(--dt-icon-size-md, 18px);
  }

  &:hover {
    background: var(--dt-bg-hover);
    color: var(--dt-brand-color);
  }
}

/* 暗色模式 */
.dark {
  .favorites-panel {
    background: var(--dt-bg-body-dark);
  }

  .panel-header {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  .panel-title,
  .favorite-title {
    color: var(--dt-text-primary-dark);
  }

  .favorite-item {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  .favorite-item:hover {
    border-color: var(--dt-brand-color);
  }

  .empty-icon {
    color: var(--dt-border-dark);
  }
}
</style>
