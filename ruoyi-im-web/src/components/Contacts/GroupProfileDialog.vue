<template>
  <el-dialog
    v-model="visible"
    :width="680"
    class="group-profile-dialog"
    :show-close="true"
    :close-on-click-modal="true"
    destroy-on-close
    append-to-body
  >
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="3" animated />
    </div>

    <div v-else-if="groupInfo" class="dialog-content">
      <!-- 左侧：群组基本信息 -->
      <div class="left-section">
        <div class="group-avatar-wrapper">
          <img v-if="groupInfo.avatar" :src="groupInfo.avatar" class="group-avatar-img" />
          <div v-else class="group-avatar-icon">
            <span class="material-icons-outlined">groups</span>
          </div>
        </div>

        <h3 class="group-name">{{ groupInfo.name }}</h3>

        <div class="group-meta">
          <span class="meta-item">
            <span class="material-icons-outlined">people</span>
            {{ groupInfo.memberCount || 0 }} 人
          </span>
        </div>

        <div class="group-desc">{{ groupInfo.description || '暂无群简介' }}</div>
      </div>

      <!-- 右侧：详细信息和操作 -->
      <div class="right-section">
        <!-- 群成员预览 -->
        <div class="members-preview">
          <div class="section-title">
            <span>群成员</span>
            <span class="count">{{ groupInfo.memberCount || 0 }}</span>
          </div>
          <div class="members-list">
            <div v-for="member in displayMembers" :key="member.id" class="member-item" :title="member.name">
              <DingtalkAvatar
                :src="member.avatar"
                :name="member.name"
                :user-id="member.id"
                :size="36"
                shape="circle"
              />
            </div>
            <div v-if="groupInfo.memberCount > displayMembers.length" class="member-item more">
              +{{ groupInfo.memberCount - displayMembers.length }}
            </div>
          </div>
        </div>

        <!-- 快捷操作 -->
        <div class="quick-actions">
          <button class="quick-btn primary" @click="handleSendMessage">
            <span class="material-icons-outlined">chat_bubble</span>
            发消息
          </button>
          <button class="quick-btn" @click="handleAnnouncement">
            <span class="material-icons-outlined">campaign</span>
            公告
          </button>
          <button class="quick-btn" @click="handleMembers">
            <span class="material-icons-outlined">people</span>
            成员
          </button>
        </div>

        <!-- 更多操作 -->
        <div class="more-actions">
          <button class="more-btn" @click="handleToggleMute">
            <span class="material-icons-outlined">
              {{ groupInfo.isMuted ? 'notifications' : 'notifications_off' }}
            </span>
            {{ groupInfo.isMuted ? '取消免打扰' : '消息免打扰' }}
          </button>
          <button class="more-btn" @click="handlePin">
            <span class="material-icons-outlined">push_pin</span>
            {{ groupInfo.isPinned ? '取消置顶' : '置顶会话' }}
          </button>
          <button v-if="isOwnerOrAdmin" class="more-btn" @click="handleSettings">
            <span class="material-icons-outlined">settings</span>
            群设置
          </button>
          <button class="more-btn danger" @click="handleExitGroup">
            <span class="material-icons-outlined">exit_to_app</span>
            退出群聊
          </button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { getGroup, getGroupMembers } from '@/api/im/group'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  groupId: { type: [String, Number], default: null }
})

const emit = defineEmits(['update:modelValue', 'send-message', 'announcement', 'members', 'settings', 'exit', 'update-group'])

const visible = ref(false)
const groupInfo = ref(null)
const loading = ref(false)
const displayMembers = ref([])

const isOwnerOrAdmin = computed(() => {
  const role = groupInfo.value?.myRole || groupInfo.value?.role
  return role === 'OWNER' || role === 'ADMIN'
})

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
      displayMembers.value = membersRes.data.slice(0, 10).map(item => ({
        id: item.userId || item.id,
        name: item.userName || item.name || '未知',
        avatar: item.avatar || '',
        role: item.role || 'MEMBER'
      }))
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
  visible.value = isOpen
  if (isOpen) {
    loadGroupInfo()
  }
})

watch(visible, (val) => {
  if (!val) emit('update:modelValue', false)
})

const handleClose = () => {
  visible.value = false
}

const handleSendMessage = () => {
  emit('send-message', groupInfo.value)
  handleClose()
}

const handleAnnouncement = () => {
  emit('announcement', groupInfo.value)
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

const handlePin = async () => {
  try {
    const newValue = !groupInfo.value.isPinned
    groupInfo.value.isPinned = newValue
    emit('update-group', { groupId: props.groupId, isPinned: newValue })
    ElMessage.success(newValue ? '已置顶会话' : '已取消置顶')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleSettings = () => {
  emit('settings', groupInfo.value)
}

const handleExitGroup = async () => {
  try {
    await ElMessageBox.confirm('确定要退出该群聊吗？', '退出群聊', {
      type: 'warning',
      confirmButtonText: '确定退出',
      cancelButtonText: '取消'
    })
    emit('exit', groupInfo.value)
    handleClose()
  } catch {
    // 用户取消
  }
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

:deep(.group-profile-dialog) {
  .el-dialog {
    border-radius: 12px;
    overflow: hidden;
  }

  .el-dialog__header {
    padding: 16px 20px;
    border-bottom: 1px solid var(--dt-border-light);
    margin: 0;

    .el-dialog__title {
      font-size: 16px;
      font-weight: 600;
      color: var(--dt-text-primary);
    }

    .el-dialog__headerbtn {
      top: 16px;
      right: 16px;
      width: 32px;
      height: 32px;

      .el-dialog__close {
        font-size: 18px;
        color: var(--dt-text-secondary);
      }
    }
  }

  .el-dialog__body {
    padding: 24px;
  }
}

.loading-state {
  padding: 60px 40px;
}

.dialog-content {
  display: flex;
  gap: 32px;
}

// 左侧区域
.left-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 180px;
  flex-shrink: 0;
  padding-right: 24px;
  border-right: 1px solid var(--dt-border-light);

  .group-avatar-wrapper {
    width: 100px;
    height: 100px;
    margin-bottom: 16px;

    .group-avatar-img {
      width: 100%;
      height: 100%;
      border-radius: 20px;
      object-fit: cover;
    }

    .group-avatar-icon {
      width: 100%;
      height: 100%;
      border-radius: 20px;
      background: linear-gradient(135deg, #1677ff 0%, #0e5fd9 100%);
      color: #fff;
      display: flex;
      align-items: center;
      justify-content: center;

      .material-icons-outlined {
        font-size: 48px;
      }
    }
  }

  .group-name {
    font-size: 18px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0 0 12px;
    text-align: center;
    word-break: break-word;
    max-width: 100%;
  }

  .group-meta {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-bottom: 12px;

    .meta-item {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      color: var(--dt-text-secondary);

      .material-icons-outlined {
        font-size: 14px;
      }
    }
  }

  .group-desc {
    font-size: 12px;
    color: var(--dt-text-tertiary);
    text-align: center;
    line-height: 1.5;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
  }
}

// 右侧区域
.right-section {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

// 成员预览
.members-preview {
  .section-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
    font-size: 14px;
    font-weight: 600;
    color: var(--dt-text-primary);

    .count {
      font-size: 12px;
      color: var(--dt-text-tertiary);
      font-weight: 400;
    }
  }

  .members-list {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;

    .member-item {
      width: 36px;
      height: 36px;
      flex-shrink: 0;
      cursor: pointer;
      transition: transform 0.2s;

      &:hover {
        transform: scale(1.1);
      }

      &.more {
        display: flex;
        align-items: center;
        justify-content: center;
        background: var(--dt-bg-body);
        border-radius: 50%;
        font-size: 11px;
        color: var(--dt-text-secondary);
        border: 1px dashed var(--dt-border);
      }
    }
  }
}

// 快捷操作
.quick-actions {
  display: flex;
  gap: 8px;

  .quick-btn {
    flex: 1;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    background: var(--dt-bg-body);
    border: 1px solid var(--dt-border-light);
    border-radius: 8px;
    cursor: pointer;
    font-size: 14px;
    color: var(--dt-text-primary);
    transition: all 0.2s;

    .material-icons-outlined {
      font-size: 18px;
    }

    &.primary {
      flex: 2;
      background: var(--dt-brand-color);
      border-color: var(--dt-brand-color);
      color: #fff;

      &:hover {
        opacity: 0.9;
      }
    }

    &:hover:not(.primary) {
      background: var(--dt-bg-hover);
      border-color: var(--dt-brand-color);
      color: var(--dt-brand-color);
    }
  }
}

// 更多操作
.more-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;

  .more-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 14px;
    background: transparent;
    border: 1px solid var(--dt-border-light);
    border-radius: 8px;
    cursor: pointer;
    font-size: 13px;
    color: var(--dt-text-secondary);
    transition: all 0.2s;

    .material-icons-outlined {
      font-size: 16px;
    }

    &:hover {
      background: var(--dt-bg-hover);
      border-color: var(--dt-brand-color);
      color: var(--dt-brand-color);
    }

    &.danger:hover {
      background: var(--dt-error-bg);
      border-color: var(--dt-error-color);
      color: var(--dt-error-color);
    }
  }
}
</style>
