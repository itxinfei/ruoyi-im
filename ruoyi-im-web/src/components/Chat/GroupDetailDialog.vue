<template>
  <el-dialog
    v-model="visible"
    :width="680"
    class="group-detail-dialog"
    :close-on-click-modal="true"
    append-to-body
    destroy-on-close
    align-center
  >
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 群组信息内容 -->
    <div v-else-if="groupInfo" class="dialog-content">
      <!-- 左侧：群组信息 -->
      <div class="left-section">
        <div class="group-avatar">
          <span v-if="groupInfo.avatar" class="avatar-img" :style="{ backgroundImage: `url(${groupInfo.avatar})` }"></span>
          <span v-else class="material-icons-outlined">groups</span>
        </div>
        <h3 class="group-name">{{ groupInfo.name }}</h3>
        <div class="group-tags">
          <span v-if="groupInfo.type" class="tag">
            <span class="material-icons-outlined tag-icon">{{ groupInfo.type === 'PUBLIC' ? 'public' : 'lock' }}</span>
            {{ groupTypeText }}
          </span>
          <span v-if="groupInfo.myRole" class="tag role-tag">
            <span class="material-icons-outlined tag-icon">shield</span>
            {{ roleText }}
          </span>
        </div>
      </div>

      <!-- 右侧：信息和操作 -->
      <div class="right-section">
        <!-- 信息列表 -->
        <div class="info-list">
          <div class="info-item">
            <span class="material-icons-outlined info-icon">people</span>
            <span class="info-label">成员数</span>
            <span class="info-value">{{ groupInfo.memberCount || 0 }} / {{ groupInfo.memberLimit || groupInfo.maxMembers || '∞' }}</span>
          </div>
          <div v-if="groupInfo.ownerName" class="info-item">
            <span class="material-icons-outlined info-icon">person</span>
            <span class="info-label">群主</span>
            <span class="info-value">{{ groupInfo.ownerName }}</span>
          </div>
          <div v-if="groupInfo.createTime" class="info-item">
            <span class="material-icons-outlined info-icon">event</span>
            <span class="info-label">创建时间</span>
            <span class="info-value">{{ formatDate(groupInfo.createTime) }}</span>
          </div>
          <div v-if="groupInfo.description" class="info-item full-width">
            <span class="material-icons-outlined info-icon">description</span>
            <span class="info-value">{{ groupInfo.description }}</span>
          </div>
          <div v-if="groupInfo.notice || groupInfo.announcement" class="info-item full-width">
            <span class="material-icons-outlined info-icon">campaign</span>
            <span class="info-value announcement">{{ groupInfo.notice || groupInfo.announcement }}</span>
          </div>
        </div>

        <!-- 群成员预览 -->
        <div class="members-preview">
          <div class="members-header">
            <span class="members-title">群成员</span>
            <span class="members-count">{{ groupInfo.memberCount || 0 }}人</span>
          </div>
          <div class="members-grid">
            <div
              v-for="member in displayMembers.slice(0, 12)"
              :key="member.id"
              class="member-item"
              :title="member.name"
            >
              <DingtalkAvatar
                :src="member.avatar"
                :name="member.name"
                :user-id="member.id"
                :size="32"
                shape="circle"
              />
            </div>
            <div
              v-if="groupInfo.memberCount > 12"
              class="member-item more"
              @click="handleMembers"
            >
              <span>+{{ groupInfo.memberCount - 12 }}</span>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-group">
          <button class="action-btn primary" @click="handleSendMessage">
            <span class="material-icons-outlined">chat_bubble</span>
            发消息
          </button>
          <button class="action-btn" @click="handleToggleMute" :title="groupInfo.isMuted ? '取消免打扰' : '消息免打扰'">
            <span class="material-icons-outlined">
              {{ groupInfo.isMuted ? 'notifications_off' : 'notifications' }}
            </span>
          </button>
          <button class="action-btn" @click="handleTogglePin" :title="groupInfo.isPinned ? '取消置顶' : '置顶会话'">
            <span class="material-icons-outlined">push_pin</span>
          </button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { getGroup, getGroupMembers } from '@/api/im/group'
import dayjs from 'dayjs'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  groupId: { type: [String, Number], default: null }
})

const emit = defineEmits(['update:modelValue', 'send-message', 'members', 'update-group'])

const visible = ref(false)
const groupInfo = ref(null)
const loading = ref(false)
const displayMembers = ref([])

// 群组类型文本
const groupTypeText = computed(() => {
  const type = groupInfo.value?.type
  if (type === 'PUBLIC') return '公开群'
  if (type === 'PRIVATE') return '私密群'
  return ''
})

// 角色文本
const roleText = computed(() => {
  const role = groupInfo.value?.myRole
  if (role === 'OWNER') return '群主'
  if (role === 'ADMIN') return '管理员'
  if (role === 'MEMBER') return '成员'
  return ''
})

// 日期格式化
const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD')
}

const loadGroupInfo = async () => {
  if (!props.groupId) return
  loading.value = true
  try {
    const [infoRes, membersRes] = await Promise.all([
      getGroup(props.groupId),
      getGroupMembers(props.groupId)
    ])

    if (infoRes.code === 200) {
      groupInfo.value = {
        ...infoRes.data,
        isMuted: infoRes.data.isMuted || false,
        isPinned: infoRes.data.isPinned || false
      }
    }

    if (membersRes.code === 200) {
      displayMembers.value = membersRes.data
      if (groupInfo.value) {
        groupInfo.value.memberCount = membersRes.data.length
      }
    }
  } catch (error) {
    console.error('加载群组信息失败:', error)
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (isOpen) => {
  if (isOpen) {
    loadGroupInfo()
  }
})

watch(visible, (val) => {
  if (!val) {
    emit('update:modelValue', false)
  }
})

const handleSendMessage = () => {
  emit('send-message', groupInfo.value)
  visible.value = false
}

const handleMembers = () => {
  emit('members', groupInfo.value)
}

const handleToggleMute = async () => {
  try {
    const newValue = !groupInfo.value.isMuted
    groupInfo.value.isMuted = newValue
    emit('update-group', { groupId: props.groupId, isMuted: newValue })
    ElMessage.success(newValue ? '已开启消息免打扰' : '已取消消息免打扰')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleTogglePin = async () => {
  try {
    const newValue = !groupInfo.value.isPinned
    groupInfo.value.isPinned = newValue
    emit('update-group', { groupId: props.groupId, isPinned: newValue })
    ElMessage.success(newValue ? '已置顶会话' : '已取消置顶')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

:deep(.group-detail-dialog) {
  .el-dialog__header {
    display: none;
  }

  .el-dialog__body {
    padding: 32px 32px;
  }
}

.loading-state {
  padding: 44px 32px;
}

.dialog-content {
  display: flex;
  gap: 32px;

  .left-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    min-width: 120px;
    text-align: center;

    .group-avatar {
      width: 88px;
      height: 88px;
      border-radius: 50%;
      background: var(--dt-bg-secondary);
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 14px;
      overflow: hidden;

      .avatar-img {
        width: 100%;
        height: 100%;
        background-size: cover;
        background-position: center;
      }

      .material-icons-outlined {
        font-size: 40px;
        color: var(--dt-text-secondary);
      }
    }

    .group-name {
      font-size: 18px;
      font-weight: 500;
      color: var(--dt-text-primary);
      margin: 0 0 12px;
      max-width: 120px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .group-tags {
      display: flex;
      flex-direction: column;
      gap: 6px;
      width: 100%;

      .tag {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 4px;
        font-size: 12px;
        color: var(--dt-text-secondary);
        background: var(--dt-bg-secondary);
        padding: 5px 10px;
        border-radius: 4px;
        white-space: nowrap;

        .tag-icon {
          font-size: 13px;
        }

        &.role-tag {
          .tag-icon {
            font-size: 15px;
          }
        }
      }
    }
  }

  .right-section {
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  .info-list {
    display: flex;
    flex-direction: column;
    gap: 3px;
    margin-bottom: 18px;

    .info-item {
      display: flex;
      align-items: center;
      gap: 14px;
      padding: 12px 16px;
      border-radius: 6px;
      transition: background 0.2s;

      &:hover {
        background: var(--dt-bg-secondary);
      }

      &.full-width {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;
      }

      .info-icon {
        font-size: 20px;
        color: var(--dt-text-tertiary);
        width: 22px;
        text-align: center;
      }

      .info-label {
        font-size: 13px;
        color: var(--dt-text-tertiary);
        min-width: 60px;
      }

      .info-value {
        flex: 1;
        font-size: 15px;
        color: var(--dt-text-primary);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;

        &.announcement {
          white-space: normal;
          color: var(--dt-text-secondary);
          line-height: 1.5;
        }
      }
    }
  }

  .members-preview {
    margin-bottom: 24px;

    .members-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 6px 0;
      margin-bottom: 10px;

      .members-title {
        font-size: 14px;
        font-weight: 500;
        color: var(--dt-text-primary);
      }

      .members-count {
        font-size: 13px;
        color: var(--dt-text-tertiary);
      }
    }

    .members-grid {
      display: grid;
      grid-template-columns: repeat(12, 1fr);
      gap: 8px;

      .member-item {
        width: 32px;
        height: 32px;
        transition: transform 0.2s;

        &:hover {
          transform: scale(1.1);
        }

        &.more {
          background: var(--dt-bg-secondary);
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 11px;
          font-weight: 500;
          color: var(--dt-text-secondary);
          cursor: pointer;
        }
      }
    }
  }

  .action-group {
    display: flex;
    gap: 10px;
    margin-top: auto;

    .action-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      height: 42px;
      padding: 0 18px;
      border-radius: 6px;
      font-size: 15px;
      cursor: pointer;
      transition: all 0.2s;
      border: 1px solid var(--dt-border-light);

      .material-icons-outlined {
        font-size: 19px;
        color: var(--dt-text-secondary);
      }

      &.primary {
        flex: 1;
        background: var(--dt-brand-color);
        border-color: var(--dt-brand-color);
        color: #fff;

        .material-icons-outlined {
          color: #fff;
        }
      }

      &:not(.primary) {
        background: var(--dt-bg-secondary);
        color: var(--dt-text-secondary);
      }

      &:hover {
        opacity: 0.85;
      }
    }
  }
}

.dark {
  .action-group .action-btn:not(.primary) {
    background: var(--dt-bg-hover-dark);
  }
}
</style>
