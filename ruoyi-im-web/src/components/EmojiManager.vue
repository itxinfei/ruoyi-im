<template>
  <el-dialog
    v-model="visible"
    title="表情管理"
    width="600px"
    class="emoji-manager-dialog"
    append-to-body
    @open="loadEmojis"
  >
    <div class="emoji-manager-content">
      <!-- 上传区域 -->
      <div class="upload-section">
        <el-upload
          ref="uploadRef"
          :action="uploadUrl"
          :headers="uploadHeaders"
          :show-file-list="false"
          :before-upload="beforeUpload"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          accept="image/png,image/jpeg,image/gif,image/webp"
          drag
        >
          <div class="upload-area">
            <el-icon class="upload-icon"><Plus /></el-icon>
            <div class="upload-text">点击或拖拽图片到此处上传</div>
            <div class="upload-hint">支持 PNG、JPG、GIF、WebP 格式，最大 2MB</div>
          </div>
        </el-upload>
      </div>

      <!-- 表情列表 -->
      <div v-loading="loading" class="emoji-list-section">
        <div class="section-header">
          <span class="section-title">我的表情 ({{ myEmojis.length }})</span>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索表情名称"
            size="small"
            clearable
            style="width: 200px"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <div v-if="!loading && myEmojis.length === 0" class="empty-state">
          <el-icon><FolderOpened /></el-icon>
          <span>还没有自定义表情，快来添加吧~</span>
        </div>

        <div v-else class="emoji-grid">
          <div
            v-for="emoji in filteredEmojis"
            :key="emoji.id"
            class="emoji-item"
          >
            <div class="emoji-image-wrapper">
              <img :src="emoji.emojiUrl" :alt="emoji.emojiName" class="emoji-image" />
            </div>
            <div class="emoji-info">
              <span class="emoji-name">{{ emoji.emojiName }}</span>
              <span class="emoji-use-count">使用 {{ emoji.useCount || 0 }} 次</span>
            </div>
            <div class="emoji-actions">
              <el-button
                size="small"
                type="danger"
                text
                :icon="Delete"
                @click="handleDelete(emoji)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 系统表情（只读展示） -->
      <div v-if="systemEmojis.length > 0" class="system-emoji-section">
        <div class="section-header">
          <span class="section-title">系统表情 ({{ systemEmojis.length }})</span>
        </div>
        <div class="emoji-grid compact">
          <div
            v-for="emoji in systemEmojis"
            :key="emoji.id"
            class="emoji-item system"
          >
            <div class="emoji-image-wrapper">
              <img :src="emoji.emojiUrl" :alt="emoji.emojiName" class="emoji-image" />
            </div>
            <div class="emoji-info">
              <span class="emoji-name">{{ emoji.emojiName }}</span>
            </div>
            <div class="emoji-actions">
              <el-button
                size="small"
                type="primary"
                text
                @click="handleAddToMine(emoji)"
              >
                添加到我的表情
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Plus, Search, FolderOpened, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEmojiList, deleteEmoji, uploadEmoji, shareEmoji } from '@/api/im/emoji'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'refresh'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const searchKeyword = ref('')
const allEmojis = ref([])

// 上传配置
const uploadUrl = computed(() => {
  return '/api/im/emoji/upload'
})

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('im_token')
  const userInfo = localStorage.getItem('im_user_info')
  const headers = {}
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      if (user.id) {
        headers['userId'] = String(user.id)
      }
    } catch (e) {}
  }
  return headers
})

// 我的表情
const myEmojis = computed(() => {
  return allEmojis.value.filter(e => e.isOwn)
})

// 系统表情
const systemEmojis = computed(() => {
  return allEmojis.value.filter(e => !e.isOwn)
})

// 过滤后的表情
const filteredEmojis = computed(() => {
  const keyword = searchKeyword.value.toLowerCase().trim()
  if (!keyword) return myEmojis.value
  return myEmojis.value.filter(e =>
    (e.emojiName && e.emojiName.toLowerCase().includes(keyword)) ||
    (e.emojiCode && e.emojiCode.toLowerCase().includes(keyword))
  )
})

// 加载表情列表
const loadEmojis = async () => {
  loading.value = true
  try {
    const res = await getEmojiList()
    if (res.code === 200) {
      allEmojis.value = res.data || []
    }
  } catch (error) {
    console.error('加载表情失败', error)
  } finally {
    loading.value = false
  }
}

// 上传前校验
const beforeUpload = (file) => {
  const validTypes = ['image/png', 'image/jpeg', 'image/gif', 'image/webp']
  const isValidType = validTypes.includes(file.type)
  const isValidSize = file.size <= 2 * 1024 * 1024

  if (!isValidType) {
    ElMessage.error('只支持 PNG、JPG、GIF、WebP 格式的图片')
    return false
  }

  if (!isValidSize) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }

  return true
}

// 上传成功
const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    ElMessage.success('表情添加成功')
    loadEmojis()
    emit('refresh')
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

// 上传失败
const handleUploadError = () => {
  ElMessage.error('上传失败，请重试')
}

// 删除表情
const handleDelete = async (emoji) => {
  try {
    await ElMessageBox.confirm(`确定要删除表情"${emoji.emojiName}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await deleteEmoji(emoji.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadEmojis()
      emit('refresh')
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除表情失败', error)
      ElMessage.error('删除失败')
    }
  }
}

// 添加系统表情到我的表情
const handleAddToMine = async (emoji) => {
  try {
    const res = await shareEmoji(emoji.id)
    if (res.code === 200) {
      ElMessage.success('已添加到我的表情')
      loadEmojis()
      emit('refresh')
    } else {
      ElMessage.error(res.msg || '添加失败')
    }
  } catch (error) {
    console.error('添加表情失败', error)
    ElMessage.error('添加失败')
  }
}
</script>

<style scoped lang="scss">
.emoji-manager-dialog {
  :deep(.el-dialog__body) {
    padding: 16px;
    max-height: 600px;
    overflow-y: auto;
  }
}

.emoji-manager-content {
  .upload-section {
    margin-bottom: 20px;

    .upload-area {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 24px;
      gap: 8px;

      .upload-icon {
        font-size: 48px;
        color: var(--el-color-primary);
      }

      .upload-text {
        font-size: 14px;
        color: var(--el-text-color-primary);
      }

      .upload-hint {
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }
  }

  .emoji-list-section,
  .system-emoji-section {
    margin-bottom: 20px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .section-title {
      font-size: 14px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px;
    gap: 12px;
    color: var(--el-text-color-secondary);

    .el-icon {
      font-size: 48px;
      opacity: 0.5;
    }
  }

  .emoji-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;

    &.compact {
      grid-template-columns: repeat(6, 1fr);
      gap: 8px;
    }

    .emoji-item {
      background: var(--el-fill-color-light);
      border-radius: 8px;
      padding: 12px;
      transition: all 0.2s;

      &:hover {
        background: var(--el-fill-color);
      }

      &.system {
        .emoji-image-wrapper {
          opacity: 0.8;
        }
      }

      .emoji-image-wrapper {
        width: 100%;
        aspect-ratio: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 8px;
        background: #fff;
        border-radius: 6px;
        overflow: hidden;

        .emoji-image {
          max-width: 100%;
          max-height: 100%;
          object-fit: contain;
        }
      }

      .emoji-info {
        text-align: center;
        margin-bottom: 8px;

        .emoji-name {
          display: block;
          font-size: 12px;
          color: var(--el-text-color-primary);
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .emoji-use-count {
          display: block;
          font-size: 11px;
          color: var(--el-text-color-secondary);
          margin-top: 2px;
        }
      }

      .emoji-actions {
        display: flex;
        justify-content: center;

        .el-button {
          width: 100%;
        }
      }
    }
  }
}

// 深色模式适配
.dark {
  .emoji-grid .emoji-item .emoji-image-wrapper {
    background: var(--el-bg-color);
  }
}
</style>
