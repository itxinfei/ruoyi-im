<template>
  <el-dialog
    v-model="visible"
    title="群组信息"
    width="520px"
    class="group-detail-dialog"
    destroy-on-close
    append-to-body
  >
    <div v-if="groupInfo" v-loading="loading" class="group-detail-container">
      <!-- 头部卡片 -->
      <div class="group-hero">
        <div class="hero-left">
          <DingtalkAvatar
            :src="groupInfo.avatar"
            :name="groupInfo.name"
            :size="64"
            shape="square"
            custom-class="group-hero-avatar"
          />
          <div class="hero-info">
            <h3 class="group-name-text">{{ groupInfo.name }}</h3>
            <span class="member-tag">{{ members.length }} 人</span>
          </div>
        </div>
        <div class="hero-right">
          <el-button v-if="isOwnerOrAdmin" text :icon="Edit" @click="handleEditGroupName">修改名称</el-button>
        </div>
      </div>

      <!-- 公告栏 -->
      <div class="announcement-section">
        <div class="section-label">
          <el-icon><Bell /></el-icon>
          <span>群公告</span>
          <el-button v-if="isOwnerOrAdmin" text type="primary" size="small" @click="handleEditAnnouncement">编辑</el-button>
        </div>
        <div class="announcement-card" :class="{ empty: !groupInfo.announcement }">
          {{ groupInfo.announcement || '暂无公告...' }}
        </div>
      </div>

      <!-- 成员列表（网格布局） -->
      <div class="members-section">
        <div class="section-label">
          <el-icon><User /></el-icon>
          <span>群成员</span>
          <span class="count">{{ members.length }}</span>
        </div>
        
        <div class="members-grid-new scrollbar-thin">
          <!-- 添加新成员按钮 -->
          <div v-if="isOwnerOrAdmin" class="member-cell add-btn" @click="handleAddMembers">
            <div class="cell-icon-wrap"><el-icon><Plus /></el-icon></div>
            <span>邀请</span>
          </div>

          <!-- 成员列表（带左击菜单） -->
          <el-dropdown
            v-for="member in members"
            :key="member.id"
            trigger="click"
            placement="right-start"
            @command="handleMemberCommand"
          >
            <div class="member-cell">
              <DingtalkAvatar
                :src="member.avatar"
                :name="member.name"
                :size="40"
                shape="square"
                custom-class="member-avatar-cell"
              />
              <span class="cell-name">{{ member.name }}</span>
              <div v-if="member.role === 'OWNER'" class="role-flag owner">群主</div>
              <div v-else-if="member.role === 'ADMIN'" class="role-flag admin">管理</div>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="member-menu">
                <el-dropdown-item :command="{ action: 'chat', member }">
                  <span class="material-icons-outlined menu-icon">chat_bubble</span>
                  发消息
                </el-dropdown-item>
                <el-dropdown-item :command="{ action: 'profile', member }">
                  <span class="material-icons-outlined menu-icon">person</span>
                  查看资料
                </el-dropdown-item>
                <el-dropdown-item divided :command="{ action: 'setAdmin', member }" v-if="isOwner && member.role !== 'OWNER'">
                  <span class="material-icons-outlined menu-icon">{{ member.role === 'ADMIN' ? 'remove_moderator' : 'admin_panel_settings' }}</span>
                  {{ member.role === 'ADMIN' ? '取消管理员' : '设为管理员' }}
                </el-dropdown-item>
                <el-dropdown-item divided :command="{ action: 'remove', member }" v-if="isOwnerOrAdmin && member.role !== 'OWNER'">
                  <span class="material-icons-outlined menu-icon remove">person_remove</span>
                  移除出群
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 配置项 -->
      <div class="configs-section">
        <div class="config-item">
          <span>消息免打扰</span>
          <el-switch v-model="groupInfo.isMuted" @change="handleMuteChange" />
        </div>
        <div class="config-item">
          <span>置顶聊天</span>
          <el-switch v-model="groupInfo.isPinned" @change="handlePinChange" />
        </div>
        <div v-if="isOwner" class="config-item danger-config">
          <span>全员禁言</span>
          <el-switch v-model="groupInfo.isAllMuted" @change="handleAllMuteChange" />
        </div>
      </div>

      <!-- 底部危险操作 -->
      <div class="danger-actions">
        <el-button v-if="isOwner" type="danger" plain class="wide-btn" @click="handleDismiss">解散群聊</el-button>
        <el-button v-else type="danger" plain class="wide-btn" @click="handleLeave">退出群聊</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell, User, Setting, MoreFilled, Edit, Plus } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import {
  getGroup,
  getGroupMembers,
  dismissGroup,
  leaveGroup,
  updateGroup,
  setGroupMute
} from '@/api/im/group'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  groupId: { type: [Number, String], default: null }
})

const emit = defineEmits(['update:modelValue', 'refresh'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const groupInfo = ref(null)
const members = ref([])
const currentUserId = ref(null)

const loadCurrentUser = () => {
  const info = localStorage.getItem('user_info')
  if (info) {
    try {
      const u = JSON.parse(info)
      currentUserId.value = u.userId || u.id
    } catch (e) {}
  }
}

const isOwner = computed(() => groupInfo.value?.ownerId === currentUserId.value)
const isOwnerOrAdmin = computed(() => {
  if (isOwner.value) return true
  const m = members.value.find(x => x.id === currentUserId.value)
  return m?.role === 'ADMIN'
})

const loadGroupDetail = async () => {
  if (!props.groupId) return
  loading.value = true
  try {
    const [res1, res2] = await Promise.all([getGroup(props.groupId), getGroupMembers(props.groupId)])
    if (res1.data) groupInfo.value = res1.data
    if (res2.data) {
      members.value = res2.data.map(m => ({
        id: m.userId || m.id,
        name: m.userName || m.name || '未知',
        avatar: m.avatar || '',
        role: m.role || 'MEMBER'
      }))
    }
  } catch (e) {
    ElMessage.error('加载群组信息失败')
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) {
    loadCurrentUser()
    loadGroupDetail()
  }
})

const handleEditAnnouncement = async () => {
  const { value } = await ElMessageBox.prompt('编辑群公告', {
    confirmButtonText: '保存',
    cancelButtonText: '取消',
    inputValue: groupInfo.value.announcement,
    inputType: 'textarea'
  })
  try {
    await updateGroup({ groupId: props.groupId, announcement: value })
    groupInfo.value.announcement = value
    ElMessage.success('已更新公告')
  } catch (e) {}
}

const handleEditGroupName = async () => {
  const { value } = await ElMessageBox.prompt('修改群聊名称', {
    confirmButtonText: '保存',
    cancelButtonText: '取消',
    inputValue: groupInfo.value.name
  })
  try {
    await updateGroup({ groupId: props.groupId, name: value })
    groupInfo.value.name = value
    ElMessage.success('已重命名')
    emit('refresh')
  } catch (e) {}
}

const handleMuteChange = async (v) => {
  try {
    await setGroupMute({ groupId: props.groupId, isMuted: v })
    ElMessage.success(v ? '已开启免打扰' : '已关闭免打扰')
  } catch (e) { groupInfo.value.isMuted = !v }
}

const handlePinChange = (v) => ElMessage.success(v ? '已置顶' : '已取消置顶')

const handleAllMuteChange = async (v) => {
  try {
    await setGroupMute({ groupId: props.groupId, isAllMuted: v })
    ElMessage.success(v ? '禁言成功' : '已取消全员禁言')
  } catch (e) { groupInfo.value.isAllMuted = !v }
}

const handleDismiss = () => {
   ElMessageBox.confirm('确定要解散群组吗？此操作不可逆。', '警告', { type: 'error' }).then(async () => {
    await dismissGroup(props.groupId)
    ElMessage.success('群聊已解散')
    emit('refresh'); visible.value = false
  }).catch(() => {})
}

const handleLeave = () => {
  ElMessageBox.confirm('确定退出群聊吗？', '提示', { type: 'warning' }).then(async () => {
    await leaveGroup(props.groupId)
    ElMessage.success('已退出')
    emit('refresh'); visible.value = false
  }).catch(() => {})
}

const handleAddMembers = () => ElMessage.info('邀请功能正在开发中...')

// 处理成员菜单命令
const handleMemberCommand = (command) => {
  const { action, member } = command
  switch (action) {
    case 'chat':
      // 发起私聊
      ElMessage.info(`正在与 ${member.name} 聊天...`)
      // TODO: 触发切换到私聊会话
      break
    case 'profile':
      // 查看成员资料
      ElMessage.info(`查看 ${member.name} 的资料...`)
      // TODO: 打开成员资料弹窗
      break
    case 'setAdmin':
      // 设置/取消管理员
      handleToggleAdmin(member)
      break
    case 'remove':
      // 移除出群
      handleRemoveMember(member)
      break
  }
}

// 切换管理员状态
const handleToggleAdmin = async (member) => {
  const isSetAdmin = member.role !== 'ADMIN'
  try {
    await ElMessageBox.confirm(
      `确定${isSetAdmin ? '设置' : '取消'} ${member.name} 为管理员吗？`,
      isSetAdmin ? '设置管理员' : '取消管理员',
      { type: 'warning' }
    )
    // TODO: 调用设置管理员 API
    ElMessage.success(isSetAdmin ? '已设置为管理员' : '已取消管理员')
    // 刷新成员列表
    loadGroupDetail()
  } catch {
    // 用户取消
  }
}

// 移除群成员
const handleRemoveMember = async (member) => {
  try {
    await ElMessageBox.confirm(`确定将 ${member.name} 移除出群吗？`, '移除成员', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    // TODO: 调用移除成员 API
    ElMessage.info('移除功能开发中...')
  } catch {
    // 用户取消
  }
}
</script>

<style scoped lang="scss">
:deep(.group-detail-dialog) {
  border-radius: 8px;
  .el-dialog__header { border-bottom: 1px solid #f2f3f5; padding: 16px 20px; }
  .el-dialog__body { padding: 0; }
}

.group-detail-container {
  background: #fff;
  .dark & { background: #1e293b; }
}

.group-hero {
  display: flex; justify-content: space-between; align-items: center;
  padding: 24px 20px; border-bottom: 1px solid #f2f3f5;
  .dark & { border-color: #334155; }

  .hero-left {
    display: flex; align-items: center; gap: 16px;
    :deep(.group-hero-avatar) { border-radius: 6px; }
    .hero-info {
      .group-name-text { font-size: 18px; font-weight: 600; margin: 0; color: #1f2329; .dark & { color: #f1f5f9; } }
      .member-tag { font-size: 12px; color: #8f959e; margin-top: 4px; display: inline-block; }
    }
  }
}

.section-label {
  display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600;
  color: #1f2329; margin-bottom: 12px; .dark & { color: #e2e8f0; }
  .el-icon { color: #8f959e; }
  .count { color: #8f959e; font-weight: normal; font-size: 12px; }
  span { flex: 1; }
}

.announcement-section {
  padding: 20px; border-bottom: 1px solid #f2f3f5; .dark & { border-color: #334155; }
  .announcement-card {
    padding: 12px; background: #f8fafc; border-radius: 4px; font-size: 13px; color: #64748b; line-height: 1.6;
    .dark & { background: rgba(30, 41, 59, 0.5); }
    &.empty { color: #bbbfc4; font-style: italic; }
  }
}

.members-section {
  padding: 20px; border-bottom: 1px solid #f2f3f5; .dark & { border-color: #334155; }
  .members-grid-new {
    display: grid; grid-template-columns: repeat(5, 1fr); gap: 16px;
    max-height: 240px; overflow-y: auto; padding-right: 8px;

    .member-cell {
      display: flex; flex-direction: column; align-items: center; gap: 6px; cursor: pointer;
      position: relative;
      padding: 4px; border-radius: 6px; transition: background-color 0.2s;
      &:hover { background: var(--dt-bg-hover, #f5f5f5); }
      :deep(.member-avatar-cell) { border-radius: 6px; transition: opacity 0.2s; }
      &:hover :deep(.member-avatar-cell) { opacity: 0.8; }
      .cell-name { font-size: 12px; color: #1f2329; text-align: center; max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; .dark & { color: #94a3b8; } }

      &.add-btn {
        .cell-icon-wrap {
          width: 40px; height: 40px; border: 1px dashed #dcdfe6; border-radius: 6px;
          display: flex; align-items: center; justify-content: center; color: #8f959e;
          &:hover { border-color: #1677ff; color: #1677ff; }
        }
        span { font-size: 12px; color: #8f959e; }
      }

      .role-flag {
        position: absolute; top: -4px; right: -4px; font-size: 9px; padding: 1px 4px; border-radius: 4px; color: #fff;
        &.owner { background: #ff4d4f; }
        &.admin { background: #faad14; }
      }
    }

    // 成员菜单样式
    :deep(.member-menu) {
      .el-dropdown-menu__item {
        display: flex; align-items: center; gap: 8px; padding: 8px 16px;
        .menu-icon {
          font-size: 18px; color: #646a73;
          &.remove { color: #ff4d4f; }
        }
        &:hover .menu-icon { color: #1677ff; }
      }
    }
  }
}

.configs-section {
  padding: 8px 20px; border-bottom: 1px solid #f2f3f5; .dark & { border-color: #334155; }
  .config-item {
    display: flex; justify-content: space-between; align-items: center; padding: 12px 0;
    span { font-size: 14px; color: #1f2329; .dark & { color: #e2e8f0; } }
  }
}

.danger-actions {
  padding: 24px 20px; text-align: center;
  .wide-btn { width: 100%; }
}
</style>
