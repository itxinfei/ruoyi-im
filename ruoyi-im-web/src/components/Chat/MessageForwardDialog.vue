<!--
  消息转发对话框组件
  功能：将消息转发给其他联系人或群组
  支持：单条转发、批量转发、逐条转发、添加备注
-->
<template>
  <el-dialog
    v-model="visible"
    title="转发消息"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="forward-dialog">
      <!-- 消息预览 -->
      <div class="message-preview">
        <div class="preview-header">
          <i class="el-icon-share"></i>
          <span>将要转发 {{ messageCount }} 条消息</span>
        </div>
        <div class="preview-list">
          <div
            v-for="(msg, index) in previewMessages"
            :key="index"
            class="preview-item"
          >
            <div class="preview-type">
              <i :class="getMessageTypeIcon(msg.type || msg.messageType)"></i>
            </div>
            <div class="preview-content">
              {{ getMessagePreview(msg) }}
            </div>
          </div>
          <div v-if="messages.length > 5" class="preview-more">
            还有 {{ messages.length - 5 }} 条消息...
          </div>
        </div>
      </div>

      <!-- 转发模式选择 -->
      <div class="forward-mode">
        <el-radio-group v-model="forwardMode" size="small">
          <el-radio-button label="merge">合并转发</el-radio-button>
          <el-radio-button label="separate">逐条转发</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 选择转发目标 -->
      <div class="target-selector">
        <el-tabs v-model="activeTab" class="selector-tabs">
          <el-tab-pane label="转发给好友" name="friend">
            <div class="search-box">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索好友"
                clearable
              >
                <template #prefix>
                  <i class="el-icon-search"></i>
                </template>
              </el-input>
            </div>
            <div class="friend-list">
              <div
                v-for="friend in filteredFriends"
                :key="friend.id"
                class="friend-item"
                :class="{ selected: isTargetSelected(friend.id) }"
                @click="toggleSelectTarget(friend)"
              >
                <img :src="friend.avatar || defaultAvatar" class="friend-avatar" />
                <span class="friend-name">{{ friend.name || friend.nickname }}</span>
                <i v-if="isTargetSelected(friend.id)" class="el-icon-check selected-icon"></i>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="转发到群聊" name="group">
            <div class="search-box">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索群聊"
                clearable
              >
                <template #prefix>
                  <i class="el-icon-search"></i>
                </template>
              </el-input>
            </div>
            <div class="group-list">
              <div
                v-for="group in filteredGroups"
                :key="group.id"
                class="group-item"
                :class="{ selected: isTargetSelected(group.id) }"
                @click="toggleSelectTarget(group)"
              >
                <img :src="group.avatar || defaultGroupAvatar" class="group-avatar" />
                <span class="group-name">{{ group.name || group.groupName }}</span>
                <span v-if="group.memberCount" class="group-member-count">{{ group.memberCount }}人</span>
                <i v-if="isTargetSelected(group.id)" class="el-icon-check selected-icon"></i>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 添加备注 -->
      <div class="add-remark">
        <el-checkbox v-model="addRemark">添加转发备注</el-checkbox>
        <transition name="remark-expand">
          <el-input
            v-if="addRemark"
            v-model="remarkText"
            type="textarea"
            :rows="2"
            placeholder="输入转发备注（可选）"
            maxlength="200"
            show-word-limit
          />
        </transition>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <div class="selected-info">
          <span v-if="selectedTargetsList.length > 0">
            已选择 {{ selectedTargetsList.length }} 个目标
          </span>
        </div>
        <div class="footer-buttons">
          <el-button @click="handleClose">取消</el-button>
          <el-button
            type="primary"
            :disabled="selectedTargetsList.length === 0"
            @click="handleConfirm"
          >
            发送 ({{ selectedTargetsList.length }})
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useStore } from 'vuex'

const props = defineProps({
  modelValue: Boolean,
  message: Object,
  messages: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['update:modelValue', 'confirm'])

const store = useStore()
const visible = ref(false)
const activeTab = ref('friend')
const searchKeyword = ref('')
const selectedTargets = ref([])
const forwardMode = ref('merge')
const addRemark = ref(false)
const remarkText = ref('')

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
const defaultGroupAvatar = 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'

// 获取消息列表
const messageList = computed(() => {
  if (props.messages && props.messages.length > 0) {
    return props.messages
  }
  if (props.message) {
    return [props.message]
  }
  return []
})

const messageCount = computed(() => messageList.value.length)
const previewMessages = computed(() => messageList.value.slice(0, 5))

// 从 store 获取联系人和群组
const contacts = computed(() => {
  return store.state.im?.contacts || []
})

const groups = computed(() => {
  return store.state.im?.groups || []
})

// 过滤后的列表
const filteredFriends = computed(() => {
  if (!searchKeyword.value) return contacts.value
  const keyword = searchKeyword.value.toLowerCase()
  return contacts.value.filter(c =>
    (c.name || c.nickname || '').toLowerCase().includes(keyword)
  )
})

const filteredGroups = computed(() => {
  if (!searchKeyword.value) return groups.value
  const keyword = searchKeyword.value.toLowerCase()
  return groups.value.filter(g =>
    (g.name || g.groupName || '').toLowerCase().includes(keyword)
  )
})

// 已选择的目标列表
const selectedTargetsList = computed(() => {
  return selectedTargets.value
})

// 方法
const getMessageTypeIcon = (type) => {
  const icons = {
    text: 'el-icon-chat-dot-round',
    image: 'el-icon-picture',
    file: 'el-icon-document',
    voice: 'el-icon-microphone',
    video: 'el-icon-video-camera',
    location: 'el-icon-location',
  }
  return icons[type] || 'el-icon-chat-dot-round'
}

const getMessagePreview = (msg) => {
  const type = msg.type || msg.messageType
  const content = msg.content || msg.text || ''

  if (type === 'text') {
    return content.substring(0, 30)
  } else if (type === 'image') {
    return '[图片]'
  } else if (type === 'file') {
    return `[文件] ${content.name || ''}`
  } else if (type === 'voice') {
    return '[语音]'
  } else if (type === 'video') {
    return '[视频]'
  } else if (type === 'location') {
    return `[位置] ${content.name || ''}`
  }
  return '[消息]'
}

const isTargetSelected = (id) => {
  return selectedTargets.value.some(t => t.id === id)
}

const toggleSelectTarget = (target) => {
  const index = selectedTargets.value.findIndex(t => t.id === target.id)
  if (index > -1) {
    selectedTargets.value.splice(index, 1)
  } else {
    selectedTargets.value.push(target)
  }
}

const handleConfirm = () => {
  const data = {
    messages: messageList.value,
    targets: selectedTargets.value,
    mode: forwardMode.value,
    remark: addRemark.value ? remarkText.value : undefined,
  }
  emit('confirm', data)
  handleClose()
}

const handleClose = () => {
  visible.value = false
  // 延迟重置，避免动画闪烁
  setTimeout(() => {
    selectedTargets.value = []
    searchKeyword.value = ''
    forwardMode.value = 'merge'
    addRemark.value = false
    remarkText.value = ''
    activeTab.value = 'friend'
  }, 300)
}

// 监听 props 变化
watch(() => props.modelValue, val => {
  visible.value = val
})

watch(visible, val => {
  emit('update:modelValue', val)
})
</script>

<style lang="scss" scoped>
@use '@/styles/dingtalk-theme.scss' as *;

.forward-dialog {
  .message-preview {
    margin-bottom: 16px;
    padding: 12px;
    background: #f5f7fa;
    border-radius: 8px;

    .preview-header {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      color: #666;
      margin-bottom: 8px;

      i {
        color: $primary-color;
      }
    }

    .preview-list {
      max-height: 120px;
      overflow-y: auto;

      .preview-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 6px 8px;
        background: #fff;
        border-radius: 4px;
        margin-bottom: 4px;

        &:last-child {
          margin-bottom: 0;
        }

        .preview-type {
          i {
            font-size: 14px;
            color: #999;
          }
        }

        .preview-content {
          flex: 1;
          font-size: 12px;
          color: #666;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .preview-more {
        text-align: center;
        font-size: 12px;
        color: #999;
        padding: 4px;
      }
    }
  }

  .forward-mode {
    margin-bottom: 16px;
  }

  .target-selector {
    .search-box {
      margin-bottom: 12px;
    }

    .friend-list,
    .group-list {
      max-height: 250px;
      overflow-y: auto;
      border: 1px solid #e8e8e8;
      border-radius: 8px;
      padding: 4px;

      .friend-item,
      .group-item {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 10px 12px;
        border-radius: 6px;
        cursor: pointer;
        transition: all 0.2s;
        position: relative;

        &:hover {
          background: #f5f7fa;
        }

        &.selected {
          background: #e6f7ff;

          .friend-name,
          .group-name {
            color: $primary-color;
          }
        }

        .friend-avatar,
        .group-avatar {
          width: 40px;
          height: 40px;
          border-radius: 50%;
          object-fit: cover;
        }

        .friend-name,
        .group-name {
          flex: 1;
          font-size: 14px;
          color: #333;
        }

        .group-member-count {
          font-size: 12px;
          color: #999;
        }

        .selected-icon {
          color: $primary-color;
          font-size: 16px;
        }
      }
    }
  }

  .add-remark {
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #e8e8e8;

    .el-textarea {
      margin-top: 8px;
    }
  }
}

.dialog-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;

  .selected-info {
    font-size: 13px;
    color: #666;
  }

  .footer-buttons {
    display: flex;
    gap: 8px;
  }
}

// 备注输入框展开动画
.remark-expand-enter-active,
.remark-expand-leave-active {
  transition: all 0.3s ease;
}

.remark-expand-enter-from,
.remark-expand-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

// 响应式
@media (max-width: 600px) {
  .forward-dialog {
    .friend-list,
    .group-list {
      max-height: 200px;
    }
  }
}
</style>
