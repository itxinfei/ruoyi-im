<template>
  <div class="favorites-panel">
    <!-- 顶部统计与操作栏 -->
    <div class="favorites-header">
      <div class="stats-bar">
        <div class="stat-item">
          <span class="stat-value">{{ favorites.length }}</span>
          <span class="stat-label">个收藏</span>
        </div>
      </div>
    </div>

    <!-- 搜索与筛选 -->
    <div class="filter-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索收藏内容"
        clearable
        style="width: 100%"
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 收藏列表 -->
    <div v-loading="loading" class="favorites-list scrollbar-thin">
      <el-empty v-if="filteredFavorites.length === 0 && !loading" description="暂无收藏" />

      <div
        v-for="item in filteredFavorites"
        :key="item.id"
        class="favorite-item"
        @click="handleJumpToMessage(item)"
      >
        <div class="favorite-content">
          <div class="favorite-header-row">
            <div class="favorite-meta">
              <span class="favorite-sender">{{ item.senderName || '未知' }}</span>
              <span class="favorite-time">{{ formatDate(item.favoriteTime) }}</span>
            </div>
            <div class="favorite-actions">
              <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, item)">
                <el-button text>
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="editRemark">
                      编辑备注
                    </el-dropdown-item>
                    <el-dropdown-item command="editTags">
                      编辑标签
                    </el-dropdown-item>
                    <el-dropdown-item command="jump">
                      跳转到消息
                    </el-dropdown-item>
                    <el-dropdown-item command="remove" divided>
                      取消收藏
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <div class="favorite-message-preview">
            <div class="message-bubble" :class="getMessageTypeClass(item.messageType)">
              <span v-if="item.messageType === 'TEXT'">{{ truncateText(item.messageContent, 100) }}</span>
              <span v-else-if="item.messageType === 'IMAGE'">[图片]</span>
              <span v-else-if="item.messageType === 'FILE'">[文件] {{ item.fileName || '' }}</span>
              <span v-else-if="item.messageType === 'VOICE'">[语音]</span>
              <span v-else-if="item.messageType === 'VIDEO'">[视频]</span>
              <span v-else>{{ item.messageContent || item.messageType }}</span>
            </div>
          </div>

          <div v-if="item.remark || item.tags" class="favorite-remark-tags">
            <el-tag
              v-if="item.remark"
              size="small"
              type="info"
              class="remark-tag"
            >
              <el-icon><Star /></el-icon> {{ item.remark }}
            </el-tag>
            <el-tag
              v-for="tag in parseTags(item.tags)"
              :key="tag"
              size="small"
              class="tag-item"
            >
              #{{ tag }}
            </el-tag>
          </div>

          <div class="favorite-source">
            <el-icon><ChatLineSquare /></el-icon>
            <span>{{ item.conversationName || '未知会话' }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑备注对话框 -->
    <el-dialog
      v-model="remarkDialogVisible"
      title="编辑备注"
      width="400px"
      append-to-body
    >
      <el-input
        v-model="editRemarkValue"
        placeholder="请输入备注（可选）"
        maxlength="50"
        show-word-limit
      />
      <template #footer>
        <el-button @click="remarkDialogVisible = false">
          取消
        </el-button>
        <el-button type="primary" @click="confirmEditRemark">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 编辑标签对话框 -->
    <el-dialog
      v-model="tagsDialogVisible"
      title="编辑标签"
      width="400px"
      append-to-body
    >
      <el-select
        v-model="editTagsValue"
        placeholder="输入标签后按回车添加"
        allow-create
        filterable
        multiple
        style="width: 100%"
      >
        <el-option
          v-for="tag in commonTags"
          :key="tag"
          :label="tag"
          :value="tag"
        />
      </el-select>
      <template #footer>
        <el-button @click="tagsDialogVisible = false">
          取消
        </el-button>
        <el-button type="primary" @click="confirmEditTags">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, MoreFilled, Star, ChatLineSquare } from '@element-plus/icons-vue'
import {
  getUserFavorites,
  removeMessageFavorite,
  addMessageFavorite
} from '@/api/im/message'

//  emits 和 props
const emit = defineEmits(['jump-to-message'])

const loading = ref(false)
const favorites = ref([])
const searchKeyword = ref('')

// 编辑相关
const remarkDialogVisible = ref(false)
const tagsDialogVisible = ref(false)
const editRemarkValue = ref('')
const editTagsValue = ref([])
const currentFavorite = ref(null)

// 常用标签
const commonTags = ['重要', '工作', '生活', '待办', '参考', '财务', '合同']

// 过滤后的收藏列表
const filteredFavorites = computed(() => {
  if (!searchKeyword.value) return favorites.value
  const keyword = searchKeyword.value.toLowerCase()
  return favorites.value.filter(item => {
    return (
      item.senderName?.toLowerCase().includes(keyword) ||
      item.messageContent?.toLowerCase().includes(keyword) ||
      item.conversationName?.toLowerCase().includes(keyword) ||
      item.remark?.toLowerCase().includes(keyword) ||
      item.tags?.toLowerCase().includes(keyword)
    )
  })
})

// 加载收藏列表
const loadFavorites = async () => {
  loading.value = true
  try {
    const res = await getUserFavorites()
    if (res.code === 200) {
      favorites.value = res.data || []
    }
  } catch (e) {
    ElMessage.error('加载收藏列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
let searchTimer = null
const handleSearch = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    // 实时过滤，无需重新加载
  }, 300)
}

// 组件卸载时清理定时器
onUnmounted(() => {
  if (searchTimer) {
    clearTimeout(searchTimer)
    searchTimer = null
  }
})

// 跳转到消息
const handleJumpToMessage = (item) => {
  emit('jump-to-message', item)
}

// 命令处理
const handleCommand = (cmd, item) => {
  currentFavorite.value = item
  switch (cmd) {
    case 'editRemark':
      editRemarkValue.value = item.remark || ''
      remarkDialogVisible.value = true
      break
    case 'editTags':
      editTagsValue.value = parseTags(item.tags)
      tagsDialogVisible.value = true
      break
    case 'jump':
      handleJumpToMessage(item)
      break
    case 'remove':
      handleRemoveFavorite(item)
      break
  }
}

// 取消收藏
const handleRemoveFavorite = async (item) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏吗？', '提示', {
      type: 'warning'
    })

    const res = await removeMessageFavorite(item.messageId)
    if (res.code === 200) {
      ElMessage.success('取消收藏成功')
      loadFavorites()
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('取消收藏失败')
    }
  }
}

// 确认编辑备注
const confirmEditRemark = async () => {
  try {
    const res = await addMessageFavorite({
      messageId: currentFavorite.value.messageId,
      conversationId: currentFavorite.value.conversationId,
      remark: editRemarkValue.value,
      tags: currentFavorite.value.tags
    })
    if (res.code === 200) {
      ElMessage.success('备注更新成功')
      remarkDialogVisible.value = false
      loadFavorites()
    }
  } catch (e) {
    ElMessage.error('备注更新失败')
  }
}

// 确认编辑标签
const confirmEditTags = async () => {
  try {
    const res = await addMessageFavorite({
      messageId: currentFavorite.value.messageId,
      conversationId: currentFavorite.value.conversationId,
      remark: currentFavorite.value.remark,
      tags: editTagsValue.value.join(',')
    })
    if (res.code === 200) {
      ElMessage.success('标签更新成功')
      tagsDialogVisible.value = false
      loadFavorites()
    }
  } catch (e) {
    ElMessage.error('标签更新失败')
  }
}

// 辅助方法
const truncateText = (text, length) => {
  if (!text) return ''
  if (text.length <= length) return text
  return text.substring(0, length) + '...'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const day = 24 * 60 * 60 * 1000

  if (diff < day) {
    return `今天 ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
  } else if (diff < 2 * day) {
    return `昨天 ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
  } else {
    return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
  }
}

const parseTags = (tagsStr) => {
  if (!tagsStr) return []
  return tagsStr.split(',').filter(t => t.trim())
}

const getMessageTypeClass = (type) => {
  const classMap = {
    'TEXT': 'type-text',
    'IMAGE': 'type-image',
    'FILE': 'type-file',
    'VOICE': 'type-voice',
    'VIDEO': 'type-video'
  }
  return classMap[type] || 'type-other'
}

onMounted(loadFavorites)

// 暴露刷新方法
defineExpose({ refresh: loadFavorites })
</script>

<style scoped lang="scss">
.favorites-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--dt-bg-body);
}

.favorites-header {
  padding: 16px;
  border-bottom: 1px solid var(--dt-border-light);

  .stats-bar {
    display: flex;
    gap: 24px;

    .stat-item {
      display: flex;
      align-items: baseline;
      gap: 4px;

      .stat-value {
        font-size: 20px;
        font-weight: 600;
        color: var(--dt-text-primary);
      }

      .stat-label {
        font-size: 13px;
        color: var(--dt-text-tertiary);
      }
    }
  }
}

.filter-bar {
  padding: 12px 16px;
  border-bottom: 1px solid var(--dt-border-light);
}

.favorites-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;

  .favorite-item {
    padding: 12px;
    border-radius: 8px;
    margin-bottom: 8px;
    cursor: pointer;
    transition: all 0.2s;
    background: var(--dt-bg-base);

    &:hover {
      background: var(--dt-bg-body-hover);
    }

    .favorite-content {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .favorite-header-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .favorite-meta {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 12px;
      color: var(--dt-text-tertiary);

      .favorite-sender {
        font-weight: 500;
        color: var(--dt-text-primary);
      }
    }

    .favorite-message-preview {
      .message-bubble {
        display: inline-block;
        padding: 8px 12px;
        border-radius: 8px;
        font-size: 14px;
        max-width: 100%;
        word-break: break-word;

        &.type-text {
          background: var(--dt-bg-body);
          color: var(--dt-text-primary);
        }

        &.type-image {
          background: #f0f9eb;
          color: #67c23a;
        }

        &.type-file {
          background: #e6f7ff;
          color: #1890ff;
        }

        &.type-voice {
          background: #fff7e6;
          color: #fa8c16;
        }

        &.type-video {
          background: #fff1f0;
          color: #f5222d;
        }
      }
    }

    .favorite-remark-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;

      .remark-tag {
        background: #f6f8fa;
        border-color: #e1e4e8;
      }

      .tag-item {
        background: #f0f9ff;
        border-color: #bae7ff;
        color: #096dd9;
      }
    }

    .favorite-source {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      color: var(--dt-text-tertiary);
    }
  }
}
</style>
