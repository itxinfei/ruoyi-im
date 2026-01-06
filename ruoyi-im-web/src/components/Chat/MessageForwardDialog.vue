<template>
  <el-dialog
    v-model="visible"
    title="转发消息"
    :width="500"
    :close-on-click-modal="false"
    class="message-forward-dialog"
    @close="handleClose"
  >
    <!-- 消息预览 -->
    <div class="message-preview">
      <div class="preview-label">转发内容</div>
      <div class="preview-content">
        <!-- 文本消息 -->
        <div v-if="message.type === 'text'" class="content-text">
          {{ message.content }}
        </div>
        <!-- 图片消息 -->
        <div v-else-if="message.type === 'image'" class="content-image">
          <img :src="message.content" alt="图片" />
          <span>[图片]</span>
        </div>
        <!-- 文件消息 -->
        <div v-else-if="message.type === 'file'" class="content-file">
          <el-icon><Document /></el-icon>
          <span>{{ message.content?.name || '文件' }}</span>
        </div>
        <!-- 语音消息 -->
        <div v-else-if="message.type === 'voice'" class="content-voice">
          <el-icon><Microphone /></el-icon>
          <span>{{ message.content?.duration || 0 }}秒</span>
        </div>
        <!-- 视频消息 -->
        <div v-else-if="message.type === 'video'" class="content-video">
          <el-icon><VideoCamera /></el-icon>
          <span>[视频]</span>
        </div>
        <!-- 其他 -->
        <div v-else class="content-other">
          {{ message.content }}
        </div>
      </div>
    </div>

    <!-- 选择转发目标 -->
    <div class="forward-target">
      <div class="target-label">选择转发给</div>
      <el-tabs v-model="activeTab" class="target-tabs">
        <el-tab-pane label="最近联系人" name="contacts">
          <div class="contact-list">
            <div
              v-for="contact in recentContacts"
              :key="contact.id"
              class="contact-item"
              :class="{ selected: selectedTargets.includes(contact.id) }"
              @click="toggleTarget(contact.id)"
            >
              <el-avatar :size="40" :src="contact.avatar">
                {{ (contact.name || contact.nickname)?.charAt(0) }}
              </el-avatar>
              <div class="contact-info">
                <div class="contact-name">{{ contact.name || contact.nickname }}</div>
                <div class="contact-status">{{ contact.online ? '在线' : '离线' }}</div>
              </div>
              <div v-if="selectedTargets.includes(contact.id)" class="check-icon">
                <el-icon><Check /></el-icon>
              </div>
            </div>
            <el-empty
              v-if="recentContacts.length === 0"
              description="暂无最近联系人"
              :image-size="80"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="群组" name="groups">
          <div class="group-list">
            <div
              v-for="group in groups"
              :key="group.id"
              class="group-item"
              :class="{ selected: selectedTargets.includes(group.id) }"
              @click="toggleTarget(group.id)"
            >
              <el-avatar :size="40" :src="group.avatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <div class="group-info">
                <div class="group-name">{{ group.name }}</div>
                <div class="group-members">{{ group.memberCount || 0 }}人</div>
              </div>
              <div v-if="selectedTargets.includes(group.id)" class="check-icon">
                <el-icon><Check /></el-icon>
              </div>
            </div>
            <el-empty v-if="groups.length === 0" description="暂无群组" :image-size="80" />
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- 搜索框 -->
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索联系人或群组..."
          :prefix-icon="Search"
          clearable
          size="small"
        />
      </div>
    </div>

    <!-- 底部操作按钮 -->
    <template #footer>
      <div class="dialog-footer">
        <div class="selected-count">已选择 {{ selectedTargets.length }} 个对象</div>
        <div class="footer-buttons">
          <el-button @click="handleClose">取消</el-button>
          <el-button type="primary" :disabled="selectedTargets.length === 0" @click="handleForward">
            发送
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import {
  Document,
  Microphone,
  VideoCamera,
  Check,
  UserFilled,
  Search,
} from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  message: {
    type: Object,
    required: true,
  },
})

const emit = defineEmits(['update:modelValue', 'success'])

const store = useStore()
const activeTab = ref('contacts')
const searchKeyword = ref('')
const selectedTargets = ref([])

// 从 store 获取会话列表作为最近联系人
const recentContacts = computed(() => {
  const sessions = store.state.im.sessions || []
  const contacts = sessions
    .filter(s => s.type === 'private' && s.peerId)
    .map(s => ({
      id: s.peerId,
      name: s.name,
      nickname: s.name,
      avatar: s.avatar,
      online: s.online || false,
    }))
  return filterByKeyword(contacts)
})

// 从 store 获取群组列表
const groups = computed(() => {
  const sessions = store.state.im.sessions || []
  const groupList = sessions
    .filter(s => s.type === 'group')
    .map(s => ({
      id: s.id,
      name: s.name,
      avatar: s.avatar,
      memberCount: s.memberCount || 0,
    }))
  return filterByKeyword(groupList)
})

// 根据关键词过滤
const filterByKeyword = list => {
  if (!searchKeyword.value) return list
  const keyword = searchKeyword.value.toLowerCase()
  return list.filter(item => {
    const name = (item.name || '').toLowerCase()
    return name.includes(keyword)
  })
}

// 切换选择目标
const toggleTarget = id => {
  const index = selectedTargets.value.indexOf(id)
  if (index > -1) {
    selectedTargets.value.splice(index, 1)
  } else {
    selectedTargets.value.push(id)
  }
}

// 执行转发
const handleForward = async () => {
  if (selectedTargets.value.length === 0) {
    ElMessage.warning('请选择转发目标')
    return
  }

  try {
    await store.dispatch('im/forwardMessage', {
      messageId: props.message.id,
      targetSessionIds: selectedTargets.value,
    })
    ElMessage.success(`消息已转发到 ${selectedTargets.value.length} 个会话`)
    emit('success')
    handleClose()
  } catch (error) {
    console.error('转发失败:', error)
  }
}

// 关闭对话框
const handleClose = () => {
  selectedTargets.value = []
  searchKeyword.value = ''
  emit('update:modelValue', false)
}

// 监听对话框关闭，清空选择
watch(
  () => props.modelValue,
  val => {
    if (!val) {
      selectedTargets.value = []
      searchKeyword.value = ''
    }
  }
)
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.message-forward-dialog {
  :deep(.el-dialog__body) {
    padding: 16px;
    max-height: 500px;
    overflow-y: auto;
  }
}

.message-preview {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;

  .preview-label {
    font-size: 12px;
    color: $text-secondary;
    margin-bottom: 8px;
  }

  .preview-content {
    background: white;
    padding: 12px;
    border-radius: 8px;
    border-left: 3px solid $primary-color;

    .content-text {
      color: $text-primary;
      font-size: 14px;
      line-height: 1.5;
      word-break: break-all;
      max-height: 60px;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }

    .content-image {
      display: flex;
      align-items: center;
      gap: 8px;

      img {
        width: 50px;
        height: 50px;
        object-fit: cover;
        border-radius: 4px;
      }

      span {
        color: $text-secondary;
        font-size: 13px;
      }
    }

    .content-file,
    .content-voice,
    .content-video {
      display: flex;
      align-items: center;
      gap: 8px;
      color: $text-secondary;
      font-size: 13px;

      .el-icon {
        font-size: 20px;
        color: $primary-color;
      }
    }

    .content-other {
      color: $text-secondary;
      font-size: 13px;
    }
  }
}

.forward-target {
  .target-label {
    font-size: 14px;
    font-weight: 500;
    color: $text-primary;
    margin-bottom: 12px;
  }

  .target-tabs {
    :deep(.el-tabs__content) {
      max-height: 250px;
      overflow-y: auto;
    }

    .contact-list,
    .group-list {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .contact-item,
    .group-item {
      display: flex;
      align-items: center;
      padding: 10px 12px;
      border-radius: 8px;
      cursor: pointer;
      transition: background-color 0.2s;
      position: relative;

      &:hover {
        background-color: #f5f7fa;
      }

      &.selected {
        background-color: #e6f7ff;

        .check-icon {
          display: flex;
        }
      }

      .contact-info,
      .group-info {
        flex: 1;
        margin-left: 12px;

        .contact-name,
        .group-name {
          font-size: 14px;
          color: $text-primary;
          margin-bottom: 2px;
        }

        .contact-status,
        .group-members {
          font-size: 12px;
          color: $text-tertiary;
        }
      }

      .check-icon {
        display: none;
        align-items: center;
        justify-content: center;
        width: 20px;
        height: 20px;
        background: $primary-color;
        border-radius: 50%;
        color: white;
        font-size: 12px;

        .el-icon {
          font-size: 12px;
        }
      }
    }
  }

  .search-box {
    margin-top: 12px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .selected-count {
    font-size: 13px;
    color: $text-secondary;
  }

  .footer-buttons {
    display: flex;
    gap: 8px;
  }
}
</style>
