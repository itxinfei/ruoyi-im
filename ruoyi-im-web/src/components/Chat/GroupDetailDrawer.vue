<template>
  <el-drawer
    v-model="visible"
    size="280px"  // 钉钉标准：抽屉宽度 280px
    direction="rtl"
    class="group-detail-drawer"
    :with-header="false"
    append-to-body
    destroy-on-close
  >
    <div v-if="groupInfo" class="group-profile">
      <!-- 封面区域 -->
      <div class="profile-cover">
        <div class="cover-pattern"></div>
        <div class="cover-overlay"></div>
        <button class="close-btn" @click="handleClose">
          <span class="material-icons-outlined">close</span>
        </button>

        <!-- 群组图标 -->
        <div class="group-icon">
          <span class="material-icons-outlined">groups</span>
        </div>
      </div>

      <!-- 群组信息区域 -->
      <div class="profile-info">
        <h2 class="profile-name">{{ groupInfo.name }}</h2>
        <p class="profile-desc">{{ groupInfo.description || '暂无群简介' }}</p>

        <!-- 群组标签 -->
        <div class="profile-tags">
          <span class="tag">
            <span class="material-icons-outlined tag-icon">people</span>
            {{ groupInfo.memberCount || 0 }} 人
          </span>
          <span v-if="groupInfo.isMuted" class="tag muted-tag">
            <span class="material-icons-outlined tag-icon">notifications_off</span>
            已免打扰
          </span>
        </div>
      </div>

      <!-- 快捷操作网格 -->
      <div class="action-grid">
        <button class="action-card primary" @click="handleSendMessage">
          <div class="action-icon">
            <span class="material-icons-outlined">chat_bubble</span>
          </div>
          <span class="action-label">发消息</span>
        </button>

        <button class="action-card" @click="handleAnnouncement">
          <div class="action-icon announcement">
            <span class="material-icons-outlined">campaign</span>
          </div>
          <span class="action-label">公告</span>
        </button>

        <button class="action-card" @click="handleMembers">
          <div class="action-icon members">
            <span class="material-icons-outlined">people</span>
          </div>
          <span class="action-label">成员</span>
        </button>

        <button class="action-card" @click="handleFiles">
          <div class="action-icon files">
            <span class="material-icons-outlined">folder_open</span>
          </div>
          <span class="action-label">文件</span>
        </button>
      </div>

      <!-- 群公告区域 -->
      <div v-if="groupInfo.announcement" class="announcement-section">
        <div class="section-header">
          <span class="material-icons-outlined section-icon">campaign</span>
          <h3 class="section-title">群公告</h3>
          <span v-if="groupInfo.hasNewAnnouncement" class="new-badge">新</span>
        </div>
        <div class="announcement-content">
          <p>{{ groupInfo.announcement }}</p>
          <span v-if="groupInfo.announcementTime" class="announcement-time">
            {{ formatTime(groupInfo.announcementTime) }}
          </span>
        </div>
      </div>

      <!-- 群成员区域 -->
      <div class="members-section">
        <div class="section-header" @click="handleMembers">
          <span class="material-icons-outlined section-icon">people</span>
          <h3 class="section-title">群成员</h3>
          <span class="member-count">{{ groupInfo.memberCount || 0 }}</span>
          <span class="material-icons-outlined arrow-icon">chevron_right</span>
        </div>

        <!-- 成员头像列表 -->
        <div class="members-avatars">
          <div v-for="member in displayMembers" :key="member.id"
               class="member-avatar"
               :title="member.name"
               @click="handleMemberClick(member)">
            <DingtalkAvatar
              :src="member.avatar"
              :name="member.name"
              :user-id="member.id"
              :size="40"
              shape="circle"
            />
            <span v-if="member.role === 'OWNER'" class="role-badge owner">群主</span>
            <span v-else-if="member.role === 'ADMIN'" class="role-badge admin">管理员</span>
          </div>
          <div v-if="groupInfo.memberCount > displayMembers.length"
               class="member-avatar more"
               @click="handleMembers">
            <span>+{{ groupInfo.memberCount - displayMembers.length }}</span>
          </div>
        </div>
      </div>

      <!-- 更多操作 -->
      <div class="more-actions">
        <button class="more-action-item" @click="handleToggleMute">
          <span class="material-icons-outlined action-icon">
            {{ groupInfo.isMuted ? 'notifications' : 'notifications_off' }}
          </span>
          <span>{{ groupInfo.isMuted ? '取消免打扰' : '消息免打扰' }}</span>
        </button>
        <button class="more-action-item" @click="handlePin">
          <span class="material-icons-outlined action-icon">push_pin</span>
          <span>{{ groupInfo.isPinned ? '取消置顶' : '置顶会话' }}</span>
        </button>
        <button v-if="isOwnerOrAdmin" class="more-action-item" @click="handleSettings">
          <span class="material-icons-outlined action-icon">settings</span>
          <span>群设置</span>
        </button>
        <button class="more-action-item danger" @click="handleExitGroup">
          <span class="material-icons-outlined action-icon">exit_to_app</span>
          <span>退出群聊</span>
        </button>
      </div>

      <!-- 安全提示 -->
      <div class="security-footer">
        <span class="material-icons-outlined security-icon">verified_user</span>
        <span>群聊内容已加密保护</span>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-else-if="loading" class="profile-loading">
      <el-skeleton :rows="3" animated />
    </div>
  </el-drawer>
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

const emit = defineEmits(['update:modelValue', 'send-message', 'announcement', 'members', 'files', 'settings', 'exit', 'update-group'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const groupInfo = ref(null)
const loading = ref(false)
const displayMembers = ref([])

// 是否是群主或管理员
const isOwnerOrAdmin = computed(() => {
  return groupInfo.value?.role === 'OWNER' || groupInfo.value?.role === 'ADMIN'
})

// 加载群组信息
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
      displayMembers.value = membersRes.data.slice(0, 8)
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

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`

  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

watch(() => props.modelValue, (isOpen) => {
  if (isOpen) {
    loadGroupInfo()
  }
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

const handleFiles = () => {
  emit('files', groupInfo.value)
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

const handleMemberClick = (member) => {
  ElMessage.info(`查看 ${member.name} 的资料`)
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// ============================================================================
// 抽屉基础样式
// ============================================================================
:deep(.group-detail-drawer) {
  .el-drawer__body {
    padding: 0;
    overflow: hidden;
  }
}

.group-profile {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--dt-bg-card);
}

// 封面区域
.profile-cover {
  position: relative;
  height: 160px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;

  .cover-pattern {
    position: absolute;
    inset: 0;
    opacity: 0.15;
    background-image:
      radial-gradient(circle at 20% 30%, rgba(255, 255, 255, 0.3) 0%, transparent 40%),
      radial-gradient(circle at 70% 70%, rgba(255, 255, 255, 0.2) 0%, transparent 40%);
    animation: patternMove 15s ease-in-out infinite;
  }

  .cover-overlay {
    position: absolute;
    inset: 0;
    background: linear-gradient(180deg, transparent 0%, rgba(0, 0, 0, 0.3) 100%);
  }

  @keyframes patternMove {
    0%, 100% { transform: scale(1) rotate(0deg); }
    50% { transform: scale(1.1) rotate(5deg); }
  }

  .close-btn {
    position: absolute;
    top: 16px;
    right: 16px;
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    border: none;
    color: #fff;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s;
    backdrop-filter: blur(10px);
    z-index: 10;

    &:hover {
      background: rgba(255, 255, 255, 0.3);
      transform: rotate(90deg);
    }

    .material-icons-outlined {
      font-size: 20px;
    }
  }

  .group-icon {
    position: absolute;
    bottom: -40px;
    left: 50%;
    transform: translateX(-50%);
    width: 88px;
    height: 88px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.95);
    display: flex;
    align-items: center;
    justify-content: center;
    border: 4px solid var(--dt-bg-card);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
    color: #667eea;
    z-index: 5;

    .material-icons-outlined {
      font-size: 40px;
    }
  }
}

// 信息区域
.profile-info {
  padding: 52px 24px 20px;
  text-align: center;

  .profile-name {
    font-size: 22px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0 0 8px;
    word-break: break-word;
  }

  .profile-desc {
    font-size: 13px;
    color: var(--dt-text-secondary);
    margin: 0 0 12px;
    line-height: 1.5;
  }

  .profile-tags {
    display: flex;
    justify-content: center;
    gap: 8px;
    flex-wrap: wrap;

    .tag {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      padding: 4px 10px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 500;
      background: rgba(102, 126, 234, 0.1);
      color: #667eea;

      &.muted-tag {
        background: rgba(156, 163, 175, 0.1);
        color: #9ca3af;
      }

      .tag-icon {
        font-size: 14px;
      }
    }
  }
}

// 操作网格
.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  padding: 0 24px 20px;

  .action-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 16px 8px;
    background: var(--dt-bg-body);
    border: 1px solid var(--dt-border-light);
    border-radius: 16px;
    cursor: pointer;
    transition: all 0.3s;

    .action-icon {
      width: 44px;
      height: 44px;
      border-radius: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;

      &.primary {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
      }

      &.announcement {
        background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
        box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
      }

      &.members {
        background: linear-gradient(135deg, #10b981 0%, #059669 100%);
        box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
      }

      &.files {
        background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
        box-shadow: 0 4px 12px rgba(139, 92, 246, 0.3);
      }
    }

    .action-label {
      font-size: 12px;
      font-weight: 500;
      color: var(--dt-text-secondary);
    }

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
    }
  }
}

// 内容区域滚动
.announcement-section,
.members-section {
  padding: 16px 24px;
  border-top: 1px solid var(--dt-border-light);

  .section-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    cursor: pointer;

    .section-icon {
      font-size: 18px;
      color: var(--dt-text-secondary);
    }

    .section-title {
      flex: 1;
      font-size: 14px;
      font-weight: 600;
      color: var(--dt-text-primary);
      margin: 0;
    }

    .new-badge {
      padding: 2px 6px;
      background: var(--dt-error-color);
      color: #fff;
      font-size: 10px;
      border-radius: 10px;
    }

    .member-count {
      font-size: 12px;
      color: var(--dt-text-tertiary);
    }

    .arrow-icon {
      font-size: 18px;
      color: var(--dt-text-tertiary);
    }
  }
}

.announcement-section {
  .announcement-content {
    padding: 12px;
    background: var(--dt-bg-body);
    border-radius: 12px;
    border-left: 3px solid #f59e0b;

    p {
      margin: 0 0 4px;
      font-size: 13px;
      color: var(--dt-text-primary);
      line-height: 1.5;
    }

    .announcement-time {
      font-size: 11px;
      color: var(--dt-text-tertiary);
    }
  }
}

.members-section {
  flex: 1;
  overflow-y: auto;

  .members-avatars {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    .member-avatar {
      position: relative;
      width: 48px;
      height: 48px;
      cursor: pointer;
      transition: transform 0.2s;

      &:hover {
        transform: scale(1.1);
      }

      &.more {
        background: var(--dt-bg-body);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        color: var(--dt-text-secondary);
        border: 1px dashed var(--dt-border);
      }

      .role-badge {
        position: absolute;
        bottom: -2px;
        left: 50%;
        transform: translateX(-50%);
        padding: 2px 4px;
        font-size: 9px;
        border-radius: 8px;
        white-space: nowrap;

        &.owner {
          background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
          color: #fff;
        }

        &.admin {
          background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
          color: #fff;
        }
      }
    }
  }
}

// 更多操作
.more-actions {
  padding: 16px 24px;
  border-top: 1px solid var(--dt-border-light);

  .more-action-item {
    display: flex;
    align-items: center;
    gap: 12px;
    width: 100%;
    padding: 12px;
    background: transparent;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
    text-align: left;
    font-size: 14px;
    color: var(--dt-text-primary);
    margin-bottom: 4px;

    .action-icon {
      font-size: 20px;
      color: var(--dt-text-secondary);
    }

    &:hover {
      background: var(--dt-bg-hover);

      .action-icon {
        color: var(--dt-brand-color);
      }
    }

    &.danger:hover {
      background: var(--dt-error-bg);
      color: var(--dt-error-color);

      .action-icon {
        color: var(--dt-error-color);
      }
    }
  }
}

// 底部安全提示
.security-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 16px 24px;
  background: var(--dt-bg-body);
  border-top: 1px solid var(--dt-border-light);

  .security-icon {
    font-size: 14px;
    color: #10b981;
  }

  font-size: 11px;
  color: var(--dt-text-tertiary);
}

// 加载状态
.profile-loading {
  padding: 24px;
}
</style>
