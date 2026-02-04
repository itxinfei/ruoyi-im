/**
 * 收藏消息面板
 * 显示用户收藏的所有消息，支持分组、搜索和管理
 */
<template>
  <div class="favorite-messages-panel">
    <!-- 面板头部 -->
    <div class="panel-header">
      <div class="header-title">
        <span class="material-icons-outlined">star</span>
        <span>收藏消息</span>
      </div>
      <el-button size="small" text @click="handleClose">
        <el-icon><Close /></el-icon>
      </el-button>
    </div>

    <!-- 筛选和搜索 -->
    <div class="panel-filter">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索收藏内容..."
        :prefix-icon="Search"
        clearable
        size="small"
      />
      <el-select
        v-model="filterType"
        size="small"
        style="width: 120px"
      >
        <el-option label="全部" value="all" />
        <el-option label="按会话" value="conversation" />
        <el-option label="按标签" value="tag" />
      </el-select>
    </div>

    <!-- 收藏消息列表 -->
    <div class="favorite-list">
      <div
        v-for="item in filteredFavorites"
        :key="item.id"
        class="favorite-item"
      >
        <!-- 消息头部：会话信息和时间 -->
        <div class="favorite-header">
          <div class="conversation-info" @click="handleJumpToConversation(item)">
            <DingtalkAvatar
              :src="item.conversationAvatar"
              :size="32"
              :username="item.conversationName"
            />
            <span class="conversation-name">{{ item.conversationName }}</span>
          </div>
          <div class="favorite-meta">
            <span class="favorite-time">{{ formatTime(item.favoriteTime) }}</span>
            <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, item)">
              <el-button size="small" text>
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="jump">跳转到消息</el-dropdown-item>
                  <el-dropdown-item command="edit">编辑备注</el-dropdown-item>
                  <el-dropdown-item command="unfavorite" class="danger">
                    取消收藏
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>

        <!-- 消息内容 -->
        <div class="favorite-content" @click="handleJumpToMessage(item)">
          <div class="message-sender">{{ item.senderName }}</div>

          <!-- 文本消息 -->
          <div v-if="item.messageType === 'TEXT'" class="message-text">
            {{ item.content }}
          </div>

          <!-- 图片消息 -->
          <div v-else-if="item.messageType === 'IMAGE'" class="message-image">
            <img :src="item.content?.imageUrl" alt="图片" />
          </div>

          <!-- 文件消息 -->
          <div v-else-if="item.messageType === 'FILE'" class="message-file">
            <el-icon><Document /></el-icon>
            <span>{{ item.content?.fileName || '文件' }}</span>
          </div>

          <!-- 其他类型 -->
          <div v-else class="message-other">
            {{ getMessageTypeText(item.messageType) }}
          </div>

          <!-- 备注信息 -->
          <div v-if="item.remark" class="favorite-remark">
            <span class="material-icons-outlined">edit_note</span>
            <span>{{ item.remark }}</span>
          </div>

          <!-- 标签 -->
          <div v-if="item.tags && item.tags.length > 0" class="favorite-tags">
            <el-tag
              v-for="tag in item.tags"
              :key="tag"
              size="small"
              type="info"
            >
              {{ tag }}
            </el-tag>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredFavorites.length === 0" class="empty-state">
        <el-empty
          :description="searchKeyword ? '没有找到匹配的收藏' : '还没有收藏的消息'"
        >
          <template #image>
            <span class="material-icons-outlined empty-icon">star_border</span>
          </template>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Close, MoreFilled, Document } from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import { getFavoriteList, removeFavorite, updateFavorite } from '@/api/im/favorite'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { formatRelativeTime } from '@/utils/message'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'jump-to-message'])

const store = useStore()
const searchKeyword = ref('')
const filterType = ref('all')
const favorites = ref([])

// 过滤后的收藏列表
const filteredFavorites = computed(() => {
  let result = favorites.value

  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(item => {
      const content = typeof item.content === 'string'
        ? item.content
        : JSON.stringify(item.content)
      return (
        item.conversationName?.toLowerCase().includes(keyword) ||
        item.senderName?.toLowerCase().includes(keyword) ||
        content?.toLowerCase().includes(keyword) ||
        item.remark?.toLowerCase().includes(keyword)
      )
    })
  }

  // 排序：最新收藏在前
  result = [...result].sort((a, b) => {
    return new Date(b.favoriteTime) - new Date(a.favoriteTime)
  })

  return result
})

/**
 * 格式化时间显示
 */
const formatTime = (time) => {
  return formatRelativeTime(time)
}

/**
 * 获取消息类型文本
 */
const getMessageTypeText = (type) => {
  const typeMap = {
    IMAGE: '[图片]',
    VIDEO: '[视频]',
    FILE: '[文件]',
    AUDIO: '[语音]',
    LOCATION: '[位置]',
    SYSTEM: '[系统消息]'
  }
  return typeMap[type] || '[消息]'
}

/**
 * 加载收藏列表
 */
const loadFavorites = async () => {
  try {
    const response = await getFavoriteList()
    favorites.value = response.data || []
  } catch (error) {
    console.error('加载收藏失败:', error)
    ElMessage.error('加载收藏失败')
  }
}

/**
 * 跳转到会话
 */
const handleJumpToConversation = (item) => {
  emit('jump-to-conversation', item.conversationId)
}

/**
 * 跳转到消息
 */
const handleJumpToMessage = (item) => {
  emit('jump-to-message', {
    conversationId: item.conversationId,
    messageId: item.messageId
  })
}

/**
 * 处理命令操作
 */
const handleCommand = async (cmd, item) => {
  switch (cmd) {
    case 'jump':
      handleJumpToMessage(item)
      break

    case 'edit':
      await handleEditRemark(item)
      break

    case 'unfavorite':
      await handleUnfavorite(item)
      break
  }
}

/**
 * 编辑备注
 */
const handleEditRemark = async (item) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入备注', '编辑备注', {
      inputValue: item.remark || '',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })

    await updateFavorite({
      messageId: item.messageId,
      remark: value
    })

    item.remark = value
    ElMessage.success('备注已更新')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新备注失败:', error)
      ElMessage.error('更新备注失败')
    }
  }
}

/**
 * 取消收藏
 */
const handleUnfavorite = async (item) => {
  try {
    await ElMessageBox.confirm(
      '确定要取消收藏这条消息吗？',
      '取消收藏',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await removeFavorite(item.messageId)

    // 从列表中移除
    const index = favorites.value.findIndex(f => f.id === item.id)
    if (index !== -1) {
      favorites.value.splice(index, 1)
    }

    ElMessage.success('已取消收藏')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消收藏失败:', error)
      ElMessage.error('取消收藏失败')
    }
  }
}

/**
 * 关闭面板
 */
const handleClose = () => {
  emit('close')
}

// 组件挂载时加载数据
onMounted(() => {
  if (props.visible) {
    loadFavorites()
  }
})

// 监听显示状态
watch(() => props.visible, (newVal) => {
  if (newVal) {
    loadFavorites()
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.favorite-messages-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--dt-bg-card);
}

// 面板头部
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--dt-border-color);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--dt-text-primary);

  .material-icons-outlined {
    color: #FFD700;
    font-size: 20px;
  }
}

// 筛选区域
.panel-filter {
  display: flex;
  gap: 12px;
  padding: 12px 20px;
  border-bottom: 1px solid var(--dt-border-light);
}

// 收藏列表
.favorite-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-border-color);
    border-radius: var(--dt-radius-sm);
  }
}

.favorite-item {
  padding: 12px;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  margin-bottom: 12px;
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:hover {
    border-color: var(--dt-border-color);
    box-shadow: var(--dt-shadow-sm);
  }
}

.favorite-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.conversation-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;

  &:hover .conversation-name {
    color: var(--dt-brand-color);
  }
}

.conversation-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--dt-text-secondary);
  transition: color var(--dt-transition-fast);
}

.favorite-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.favorite-time {
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.favorite-content {
  padding: 8px;
  background: var(--dt-bg-hover);
  border-radius: var(--dt-radius-sm);
}

.message-sender {
  font-size: 12px;
  font-weight: 500;
  color: var(--dt-brand-color);
  margin-bottom: 4px;
}

.message-text {
  font-size: 14px;
  color: var(--dt-text-primary);
  line-height: 1.5;
  word-break: break-word;
  white-space: pre-wrap;
}

.message-image {
  img {
    max-width: 200px;
    max-height: 150px;
    border-radius: var(--dt-radius-sm);
    object-fit: cover;
  }
}

.message-file {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: var(--dt-brand-bg);
  border-radius: var(--dt-radius-sm);
  color: var(--dt-brand-color);
  font-size: 13px;

  .el-icon {
    font-size: 20px;
  }
}

.message-other {
  font-size: 13px;
  color: var(--dt-text-secondary);
  font-style: italic;
}

.favorite-remark {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  padding: 6px 8px;
  background: rgba(255, 215, 0, 0.1);
  border-radius: var(--dt-radius-sm);
  font-size: 12px;
  color: var(--dt-text-secondary);

  .material-icons-outlined {
    font-size: 14px;
    color: #FFD700;
  }
}

.favorite-tags {
  display: flex;
  gap: 6px;
  margin-top: 8px;
  flex-wrap: wrap;
}

// 空状态
.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 300px;

  .empty-icon {
    font-size: 64px;
    color: var(--dt-text-quaternary);
  }
}

// 危险操作样式
:deep(.el-dropdown-menu__item.danger) {
  color: var(--dt-error-color);

  &:hover {
    background: var(--dt-error-bg);
    color: var(--dt-error-color);
  }
}

// 暗色模式
:global(.dark) {
  .favorite-item {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);

    &:hover {
      border-color: var(--dt-border-dark);
      background: var(--dt-bg-hover-dark);
    }
  }

  .favorite-content {
    background: var(--dt-bg-hover-dark);
  }

  .favorite-remark {
    background: rgba(255, 215, 0, 0.15);
  }
}
</style>
