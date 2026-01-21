<template>
  <el-drawer
    v-model="visible"
    title="群组详情"
    size="400px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div v-if="groupInfo" v-loading="loading" class="group-detail">
      <!-- 群组基本信息 -->
      <div class="group-header">
        <el-avatar :size="80" :src="groupInfo.avatar">
          {{ groupInfo.name?.charAt(0) }}
        </el-avatar>
        <h2 class="group-name">{{ groupInfo.name }}</h2>
        <p class="group-id">群号: {{ groupInfo.id }}</p>
      </div>

      <!-- 群公告 -->
      <div class="info-section">
        <div class="section-title">
          <el-icon><Bell /></el-icon>
          <span>群公告</span>
          <el-button
            v-if="isOwnerOrAdmin"
            text
            size="small"
            @click="handleEditAnnouncement"
          >
            编辑
          </el-button>
        </div>
        <div class="announcement-content">
          {{ groupInfo.announcement || '暂无公告' }}
        </div>
      </div>

      <!-- 群成员 -->
      <div class="info-section">
        <div class="section-title">
          <el-icon><User /></el-icon>
          <span>群成员 ({{ members.length }})</span>
          <el-button
            v-if="isOwnerOrAdmin"
            text
            size="small"
            @click="handleAddMembers"
          >
            添加
          </el-button>
        </div>
        <div class="members-grid">
          <div
            v-for="member in displayMembers"
            :key="member.id"
            class="member-item"
          >
            <el-avatar :size="48" :src="member.avatar">
              {{ member.name?.charAt(0) }}
            </el-avatar>
            <div class="member-name">{{ member.name }}</div>
            <el-tag v-if="member.role === 'OWNER'" size="small" type="danger">
              群主
            </el-tag>
            <el-tag v-else-if="member.role === 'ADMIN'" size="small" type="warning">
              管理员
            </el-tag>
          </div>
          <div v-if="members.length > 9" class="member-item more" @click="showAllMembers = true">
            <el-icon class="more-icon"><MoreFilled /></el-icon>
            <div class="member-name">更多</div>
          </div>
        </div>
      </div>

      <!-- 群设置 -->
      <div class="info-section">
        <div class="section-title">
          <el-icon><Setting /></el-icon>
          <span>群设置</span>
        </div>
        <div class="settings-list">
          <div class="setting-item">
            <span>消息免打扰</span>
            <el-switch v-model="groupInfo.isMuted" @change="handleMuteChange" />
          </div>
          <div class="setting-item">
            <span>置顶聊天</span>
            <el-switch v-model="groupInfo.isPinned" @change="handlePinChange" />
          </div>
          <div v-if="isOwner" class="setting-item">
            <span>全员禁言</span>
            <el-switch v-model="groupInfo.isAllMuted" @change="handleAllMuteChange" />
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button v-if="isOwner" type="danger" plain @click="handleDismiss">
          解散群组
        </el-button>
        <el-button v-else type="warning" plain @click="handleLeave">
          退出群组
        </el-button>
      </div>
    </div>

    <!-- 全部成员对话框 -->
    <el-dialog
      v-model="showAllMembers"
      title="全部成员"
      width="500px"
      append-to-body
    >
      <div class="all-members-list">
        <div
          v-for="member in members"
          :key="member.id"
          class="member-list-item"
        >
          <el-avatar :size="40" :src="member.avatar">
            {{ member.name?.charAt(0) }}
          </el-avatar>
          <div class="member-info">
            <div class="member-name">{{ member.name }}</div>
            <div class="member-role">
              <el-tag v-if="member.role === 'OWNER'" size="small" type="danger">
                群主
              </el-tag>
              <el-tag v-else-if="member.role === 'ADMIN'" size="small" type="warning">
                管理员
              </el-tag>
              <span v-else class="role-text">成员</span>
            </div>
          </div>
          <el-dropdown v-if="isOwnerOrAdmin && member.role !== 'OWNER'" trigger="click">
            <el-button text circle>
              <el-icon><MoreFilled /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="isOwner" @click="handleSetAdmin(member)">
                  {{ member.role === 'ADMIN' ? '取消管理员' : '设为管理员' }}
                </el-dropdown-item>
                <el-dropdown-item @click="handleRemoveMember(member)">
                  移出群组
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-dialog>
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Bell,
  User,
  Setting,
  MoreFilled
} from '@element-plus/icons-vue'
import {
  getGroup,
  getGroupMembers,
  dismissGroup,
  leaveGroup,
  removeGroupMember,
  setGroupAdmin,
  updateGroup,
  setGroupMute
} from '@/api/im/group'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  groupId: {
    type: [Number, String],
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'refresh'])

const visible = ref(false)
const loading = ref(false)
const groupInfo = ref(null)
const members = ref([])
const showAllMembers = ref(false)

const currentUserId = ref(null)

// 加载当前用户ID
const loadCurrentUser = () => {
  const userInfoStr = localStorage.getItem('user_info')
  if (userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr)
      currentUserId.value = userInfo.userId || userInfo.id
    } catch (error) {
      console.error('解析用户信息失败:', error)
    }
  }
}

// 是否为群主
const isOwner = computed(() => {
  return groupInfo.value?.ownerId === currentUserId.value
})

// 是否为管理员或群主
const isOwnerOrAdmin = computed(() => {
  if (isOwner.value) return true
  const member = members.value.find(m => m.id === currentUserId.value)
  return member?.role === 'ADMIN'
})

// 显示的成员（最多9个）
const displayMembers = computed(() => {
  return members.value.slice(0, 9)
})

// 加载群组详情
const loadGroupDetail = async () => {
  if (!props.groupId) return

  loading.value = true
  try {
    const [groupRes, membersRes] = await Promise.all([
      getGroup(props.groupId),
      getGroupMembers(props.groupId)
    ])

    if (groupRes && groupRes.data) {
      groupInfo.value = groupRes.data
    }

    if (membersRes && membersRes.data) {
      members.value = membersRes.data.map(item => ({
        id: item.userId || item.id,
        name: item.userName || item.name || '未知',
        avatar: item.avatar || '',
        role: item.role || 'MEMBER'
      }))
    }
  } catch (error) {
    console.error('加载群组详情失败:', error)
    ElMessage.error('加载群组详情失败')
  } finally {
    loading.value = false
  }
}

// 编辑公告
const handleEditAnnouncement = async () => {
  const { value } = await ElMessageBox.prompt('请输入群公告', '编辑公告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: groupInfo.value.announcement,
    inputType: 'textarea'
  })

  try {
    await updateGroup({
      groupId: props.groupId,
      announcement: value
    })
    groupInfo.value.announcement = value
    ElMessage.success('公告更新成功')
  } catch (error) {
    console.error('更新公告失败:', error)
  }
}

// 添加成员
const handleAddMembers = () => {
  ElMessage.info('添加成员功能开发中...')
  // TODO: 打开添加成员对话框
}

// 设置管理员
const handleSetAdmin = async (member) => {
  const isAdmin = member.role === 'ADMIN'
  try {
    await setGroupAdmin({
      groupId: props.groupId,
      userId: member.id,
      isAdmin: !isAdmin
    })
    member.role = isAdmin ? 'MEMBER' : 'ADMIN'
    ElMessage.success(isAdmin ? '已取消管理员' : '已设为管理员')
  } catch (error) {
    console.error('设置管理员失败:', error)
    ElMessage.error('操作失败')
  }
}

// 移除成员
const handleRemoveMember = async (member) => {
  try {
    await ElMessageBox.confirm(
      `确定要将 ${member.name} 移出群组吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await removeGroupMember({
      groupId: props.groupId,
      userId: member.id
    })

    members.value = members.value.filter(m => m.id !== member.id)
    ElMessage.success('已移出群组')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除成员失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 免打扰设置
const handleMuteChange = async (value) => {
  try {
    await setGroupMute({
      groupId: props.groupId,
      isMuted: value
    })
    ElMessage.success(value ? '已开启免打扰' : '已关闭免打扰')
  } catch (error) {
    console.error('设置免打扰失败:', error)
    groupInfo.value.isMuted = !value
  }
}

// 置顶设置
const handlePinChange = (value) => {
  ElMessage.success(value ? '已置顶' : '已取消置顶')
  // TODO: 调用置顶API
}

// 全员禁言设置
const handleAllMuteChange = async (value) => {
  try {
    await setGroupMute({
      groupId: props.groupId,
      isAllMuted: value
    })
    ElMessage.success(value ? '已开启全员禁言' : '已关闭全员禁言')
  } catch (error) {
    console.error('设置全员禁言失败:', error)
    groupInfo.value.isAllMuted = !value
  }
}

// 解散群组
const handleDismiss = async () => {
  try {
    await ElMessageBox.confirm(
      '解散后，群组将被永久删除，所有成员将被移出。确定要解散群组吗？',
      '解散群组',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    await dismissGroup(props.groupId)
    ElMessage.success('群组已解散')
    emit('refresh')
    handleClose()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('解散群组失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 退出群组
const handleLeave = async () => {
  try {
    await ElMessageBox.confirm(
      '退出后，将不再接收此群组的消息。确定要退出群组吗？',
      '退出群组',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await leaveGroup(props.groupId)
    ElMessage.success('已退出群组')
    emit('refresh')
    handleClose()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出群组失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 关闭抽屉
const handleClose = () => {
  emit('update:modelValue', false)
}

// 监听显示状态
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    loadCurrentUser()
    loadGroupDetail()
  }
})

watch(visible, (val) => {
  if (!val) {
    emit('update:modelValue', false)
  }
})
</script>

<style scoped lang="scss">
.group-detail {
  padding: 0 0 20px 0;
}

.group-header {
  text-align: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;

  .group-name {
    margin: 12px 0 4px 0;
    font-size: 20px;
    font-weight: 500;
    color: #262626;
  }

  .group-id {
    margin: 0;
    font-size: 12px;
    color: #8c8c8c;
  }
}

.info-section {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;

  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 16px;
    font-size: 14px;
    font-weight: 500;
    color: #262626;

    .el-icon {
      font-size: 16px;
    }

    span {
      flex: 1;
    }
  }

  .announcement-content {
    padding: 12px;
    background: #f5f5f5;
    border-radius: 8px;
    font-size: 14px;
    color: #595959;
    line-height: 1.6;
    white-space: pre-wrap;
  }
}

.members-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;

  .member-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    cursor: pointer;
    padding: 8px;
    border-radius: 8px;
    transition: background-color 0.2s;

    &:hover {
      background-color: #f5f5f5;
    }

    &.more {
      .more-icon {
        font-size: 24px;
        color: #8c8c8c;
        width: 48px;
        height: 48px;
        display: flex;
        align-items: center;
        justify-content: center;
        border: 1px dashed #d9d9d9;
        border-radius: 50%;
      }
    }

    .member-name {
      font-size: 12px;
      color: #595959;
      text-align: center;
      max-width: 100%;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .el-tag {
      margin-top: 2px;
    }
  }
}

.settings-list {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .setting-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px;
    background: #f5f5f5;
    border-radius: 8px;
    font-size: 14px;
    color: #262626;
  }
}

.action-buttons {
  padding: 20px;
  display: flex;
  justify-content: center;

  .el-button {
    width: 100%;
  }
}

.all-members-list {
  max-height: 400px;
  overflow-y: auto;

  .member-list-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    border-radius: 8px;
    transition: background-color 0.2s;

    &:hover {
      background-color: #f5f5f5;
    }

    .member-info {
      flex: 1;

      .member-name {
        font-size: 14px;
        font-weight: 500;
        color: #262626;
        margin-bottom: 4px;
      }

      .member-role {
        .role-text {
          font-size: 12px;
          color: #8c8c8c;
        }
      }
    }
  }
}
</style>
