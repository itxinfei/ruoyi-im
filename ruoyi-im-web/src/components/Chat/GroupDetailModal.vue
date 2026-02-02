<template>
  <el-dialog
    v-model="visible"
    :title="null"
    width="600px"
    :modal="true"
    :close-on-click-modal="true"
    :close-on-press-escape="true"
    destroy-on-close
    append-to-body
    class="group-detail-modal"
  >
    <!-- 关闭按钮 -->
    <template #header="{ close, titleId, titleClass }">
      <div class="modal-header">
        <button class="close-btn" @click="close">
          <el-icon><Close /></el-icon>
        </button>
      </div>
    </template>

    <div v-if="groupInfo" v-loading="loading" class="group-detail-content">
      <!-- 群组信息卡片 -->
      <div class="group-info-card">
        <DingtalkAvatar
          :src="groupInfo.avatar"
          :name="groupInfo.name"
          :size="80"
          shape="square"
          custom-class="group-avatar"
        />
        <h3 class="group-name">{{ groupInfo.name }}</h3>
        <div class="group-meta">
          <span class="member-count">{{ members.length }} 位成员</span>
          <span v-if="groupInfo.ownerId === currentUserId" class="role-badge owner">群主</span>
          <span v-else-if="isAdmin" class="role-badge admin">管理员</span>
        </div>
      </div>

      <!-- 快捷操作按钮 -->
      <div class="quick-actions">
        <el-button class="action-btn" @click="handleAddMembers" v-if="isOwnerOrAdmin">
          <el-icon><Plus /></el-icon>
          <span>邀请成员</span>
        </el-button>
        <el-button class="action-btn" @click="handleEditGroupName" v-if="isOwnerOrAdmin">
          <el-icon><Edit /></el-icon>
          <span>修改群名</span>
        </el-button>
        <el-button class="action-btn" @click="handleEditAnnouncement" v-if="isOwnerOrAdmin">
          <el-icon><Bell /></el-icon>
          <span>编辑公告</span>
        </el-button>
      </div>

      <!-- 群公告区块 -->
      <div class="section-block" v-if="groupInfo.announcement || isOwnerOrAdmin">
        <div class="section-header">
          <el-icon><Bell /></el-icon>
          <span class="section-title">群公告</span>
        </div>
        <div class="announcement-content" :class="{ empty: !groupInfo.announcement }">
          {{ groupInfo.announcement || '暂无群公告' }}
        </div>
      </div>

      <!-- 群成员区块 -->
      <div class="section-block members-block">
        <div class="section-header">
          <el-icon><User /></el-icon>
          <span class="section-title">群成员 ({{ members.length }})</span>
          <el-button
            v-if="members.length > 24"
            text
            type="primary"
            size="small"
            class="view-all-btn"
            @click="handleViewAllMembers"
          >
            查看全部
          </el-button>
        </div>
        <!-- 成员网格 - 每行6个 -->
        <div class="members-grid">
          <!-- 成员列表 -->
          <div
            v-for="member in displayMembers"
            :key="member.id"
            class="member-item"
            @click="handleMemberClick(member)"
          >
            <div class="member-avatar-wrap">
              <DingtalkAvatar
                :src="member.avatar"
                :name="member.name"
                :size="48"
                shape="circle"
                custom-class="member-avatar"
              />
              <div v-if="member.role === 'OWNER'" class="role-icon owner">
                <el-icon><Crown /></el-icon>
              </div>
              <div v-else-if="member.role === 'ADMIN'" class="role-icon admin">
                <el-icon><UserFilled /></el-icon>
              </div>
            </div>
            <span class="member-name">{{ member.name }}</span>
          </div>

          <!-- 邀请按钮（管理员可见） -->
          <div
            v-if="isOwnerOrAdmin"
            class="member-item add-member"
            @click="handleAddMembers"
          >
            <div class="member-avatar-wrap add-icon">
              <el-icon><Plus /></el-icon>
            </div>
            <span class="member-name">邀请</span>
          </div>
        </div>
      </div>

      <!-- 群文件区块 -->
      <div class="section-block">
        <div class="section-header">
          <el-icon><Folder /></el-icon>
          <span class="section-title">群文件</span>
          <el-button text type="primary" size="small" class="view-all-btn" @click="handleViewFiles">
            查看全部
          </el-button>
        </div>
        <div class="files-preview">
          <div v-if="recentFiles.length === 0" class="empty-state">
            <el-icon><FolderOpened /></el-icon>
            <span>暂无群文件</span>
          </div>
          <div v-else class="file-list">
            <div
              v-for="file in recentFiles"
              :key="file.id"
              class="file-item"
              @click="handleFileClick(file)"
            >
              <div class="file-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="file-info">
                <span class="file-name">{{ file.name }}</span>
                <span class="file-size">{{ formatFileSize(file.size) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 设置选项 -->
      <div class="settings-section">
        <div class="setting-item">
          <div class="setting-label">
            <el-icon><Bell /></el-icon>
            <span>消息免打扰</span>
          </div>
          <el-switch
            v-model="groupInfo.isMuted"
            @change="handleMuteChange"
            :active-color="#0089FF"
          />
        </div>
        <div class="setting-item">
          <div class="setting-label">
            <el-icon><PushPin /></el-icon>
            <span>置顶聊天</span>
          </div>
          <el-switch
            v-model="groupInfo.isPinned"
            @change="handlePinChange"
            :active-color="#0089FF"
          />
        </div>
      </div>

      <!-- 底部操作 -->
      <div class="footer-actions">
        <el-button
          v-if="isOwner"
          type="danger"
          plain
          class="footer-btn danger-btn"
          @click="handleDismiss"
        >
          解散群聊
        </el-button>
        <el-button
          v-else
          type="danger"
          plain
          class="footer-btn danger-btn"
          @click="handleLeave"
        >
          退出群聊
        </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Close, Plus, Edit, Bell, User, Crown, UserFilled,
  Folder, FolderOpened, Document, PushPin
} from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { getStoredUserInfo } from '@/utils/storage'
import {
  getGroup,
  getGroupMembers,
  dismissGroup,
  leaveGroup,
  updateGroup,
  setGroupMute,
  removeGroupMember,
  setGroupAdmin,
  addGroupMembers
} from '@/api/im/group'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  groupId: { type: [Number, String], default: null }
})

const emit = defineEmits(['update:modelValue', 'refresh', 'start-chat', 'show-member-profile', 'view-files', 'view-all-members'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const groupInfo = ref(null)
const members = ref([])
const currentUserId = ref(null)
const recentFiles = ref([])

// 是否是群主
const isOwner = computed(() => groupInfo.value?.ownerId === currentUserId.value)

// 是否是管理员
const isAdmin = computed(() => {
  if (isOwner.value) return true
  const m = members.value.find(x => x.id === currentUserId.value)
  return m?.role === 'ADMIN'
})

// 是否是群主或管理员
const isOwnerOrAdmin = computed(() => isOwner.value || isAdmin.value)

// 显示的成员（最多显示24个，加上邀请按钮）
const displayMembers = computed(() => {
  const maxCount = isOwnerOrAdmin.value ? 23 : 24
  return members.value.slice(0, maxCount)
})

// 加载当前用户信息
const loadCurrentUser = () => {
  const u = getStoredUserInfo()
  if (u) {
    currentUserId.value = u.userId || u.id
  }
}

// 加载群组详情
const loadGroupDetail = async () => {
  if (!props.groupId) return
  loading.value = true
  try {
    const [res1, res2] = await Promise.all([
      getGroup(props.groupId),
      getGroupMembers(props.groupId)
    ])
    if (res1.data) {
      groupInfo.value = {
        ...res1.data,
        isMuted: res1.data.isMuted || false,
        isPinned: res1.data.isPinned || false
      }
    }
    if (res2.data) {
      members.value = res2.data.map(m => ({
        id: m.userId || m.id,
        userId: m.userId || m.id,
        name: m.userName || m.name || '未知',
        avatar: m.avatar || '',
        role: m.role || 'MEMBER'
      }))
    }
    // TODO: 加载最近文件
    recentFiles.value = []
  } catch (e) {
    ElMessage.error('加载群组信息失败')
  } finally {
    loading.value = false
  }
}

// 监听弹窗打开
watch(() => props.modelValue, (val) => {
  if (val) {
    loadCurrentUser()
    loadGroupDetail()
  }
})

// 处理成员点击
const handleMemberClick = (member) => {
  emit('show-member-profile', member)
}

// 编辑群公告
const handleEditAnnouncement = async () => {
  const { value } = await ElMessageBox.prompt('编辑群公告', {
    confirmButtonText: '保存',
    cancelButtonText: '取消',
    inputValue: groupInfo.value.announcement || '',
    inputType: 'textarea',
    inputPlaceholder: '请输入群公告内容...'
  })
  if (!value) return
  try {
    await updateGroup({ groupId: props.groupId, announcement: value })
    groupInfo.value.announcement = value
    ElMessage.success('群公告已更新')
  } catch (e) {
    // 用户取消
  }
}

// 编辑群名称
const handleEditGroupName = async () => {
  const { value } = await ElMessageBox.prompt('修改群聊名称', {
    confirmButtonText: '保存',
    cancelButtonText: '取消',
    inputValue: groupInfo.value.name,
    inputPlaceholder: '请输入群聊名称'
  })
  if (!value) return
  try {
    await updateGroup({ groupId: props.groupId, name: value })
    groupInfo.value.name = value
    ElMessage.success('群名称已修改')
    emit('refresh')
  } catch (e) {
    // 用户取消
  }
}

// 添加成员
const handleAddMembers = async () => {
  try {
    const { value } = await ElMessageBox.prompt(
      '请输入要邀请的用户ID（多个用户用逗号分隔）',
      '邀请成员',
      {
        confirmButtonText: '邀请',
        cancelButtonText: '取消',
        inputPlaceholder: '例如: 1, 2, 3',
        inputPattern: /^(\d+)(,\s*\d+)*$/,
        inputErrorMessage: '请输入有效的用户ID，多个用逗号分隔'
      }
    )
    if (!value) return
    const userIds = value.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id))
    if (userIds.length === 0) {
      ElMessage.warning('请输入有效的用户ID')
      return
    }
    await addGroupMembers(props.groupId, userIds)
    ElMessage.success(`已邀请 ${userIds.length} 位成员`)
    loadGroupDetail()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('邀请失败，请重试')
    }
  }
}

// 查看全部成员
const handleViewAllMembers = () => {
  emit('view-all-members', members.value)
}

// 查看文件
const handleViewFiles = () => {
  emit('view-files', props.groupId)
}

// 文件点击
const handleFileClick = (file) => {
  // TODO: 实现文件预览或下载
  ElMessage.info('文件预览功能开发中')
}

// 免打扰设置
const handleMuteChange = async (v) => {
  try {
    await setGroupMute({ groupId: props.groupId, isMuted: v })
    ElMessage.success(v ? '已开启免打扰' : '已关闭免打扰')
  } catch (e) {
    groupInfo.value.isMuted = !v
  }
}

// 置顶设置
const handlePinChange = (v) => {
  ElMessage.success(v ? '已置顶' : '已取消置顶')
  // TODO: 调用置顶 API
}

// 解散群聊
const handleDismiss = () => {
  ElMessageBox.confirm('确定要解散群组吗？此操作不可逆。', '警告', {
    type: 'error',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    await dismissGroup(props.groupId)
    ElMessage.success('群聊已解散')
    emit('refresh')
    visible.value = false
  }).catch(() => {})
}

// 退出群聊
const handleLeave = () => {
  ElMessageBox.confirm('确定退出群聊吗？', '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    await leaveGroup(props.groupId)
    ElMessage.success('已退出群聊')
    emit('refresh')
    visible.value = false
  }).catch(() => {})
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}
</script>

<style scoped lang="scss">
// 钉钉颜色变量
$dt-blue: #0089FF;
$dt-text-primary: #1F2329;
$dt-text-secondary: #646A73;
$dt-text-tertiary: #8F959E;
$dt-bg-hover: #F5F5F5;
$dt-border-color: #E5E6EB;
$dt-danger: #FF4D4F;

.group-detail-modal {
  :deep(.el-dialog) {
    border-radius: 12px;
    overflow: hidden;
  }

  :deep(.el-dialog__header) {
    padding: 0;
  }

  :deep(.el-dialog__body) {
    padding: 0;
    max-height: 80vh;
    overflow-y: auto;
  }

  :deep(.el-dialog__footer) {
    display: none;
  }

  // 自定义滚动条
  :deep(.el-dialog__body)::-webkit-scrollbar {
    width: 6px;
  }

  :deep(.el-dialog__body)::-webkit-scrollbar-thumb {
    background: #C0C0C0;
    border-radius: 3px;

    &:hover {
      background: #A0A0A0;
    }
  }
}

.modal-header {
  padding: 16px 16px 0;
  text-align: right;

  .close-btn {
    width: 32px;
    height: 32px;
    border: none;
    background: transparent;
    border-radius: 6px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    color: $dt-text-secondary;
    transition: all 0.15s;

    &:hover {
      background: $dt-bg-hover;
      color: $dt-text-primary;
    }

    .el-icon {
      font-size: 20px;
    }
  }
}

.group-detail-content {
  padding: 0 24px 24px;
}

// 群组信息卡片
.group-info-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 0;
  text-align: center;
  border-bottom: 1px solid $dt-border-color;

  :deep(.group-avatar) {
    border-radius: 12px;
  }

  .group-name {
    margin: 16px 0 8px;
    font-size: 20px;
    font-weight: 600;
    color: $dt-text-primary;
  }

  .group-meta {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: $dt-text-tertiary;

    .role-badge {
      padding: 2px 8px;
      border-radius: 4px;
      font-size: 12px;

      &.owner {
        background: #FFF1F0;
        color: $dt-danger;
      }

      &.admin {
        background: #FFF7E6;
        color: #FAAD14;
      }
    }
  }
}

// 快捷操作按钮
.quick-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid $dt-border-color;

  .action-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    border: 1px solid $dt-border-color;
    border-radius: 6px;
    background: #fff;
    color: $dt-text-primary;
    font-size: 14px;
    transition: all 0.15s;

    &:hover {
      border-color: $dt-blue;
      color: $dt-blue;
      background: rgba(0, 137, 255, 0.05);
    }

    .el-icon {
      font-size: 16px;
    }
  }
}

// 区块样式
.section-block {
  padding: 20px 0;
  border-bottom: 1px solid $dt-border-color;

  &:last-of-type {
    border-bottom: none;
  }

  .section-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 16px;

    .el-icon {
      font-size: 18px;
      color: $dt-text-tertiary;
    }

    .section-title {
      flex: 1;
      font-size: 16px;
      font-weight: 600;
      color: $dt-text-primary;
    }

    .view-all-btn {
      font-size: 13px;
      padding: 4px 8px;
      height: auto;
    }
  }
}

// 公告内容
.announcement-content {
  padding: 12px 16px;
  background: #F8FAFC;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.6;
  color: $dt-text-secondary;
  white-space: pre-wrap;

  &.empty {
    color: $dt-text-tertiary;
    font-style: italic;
  }
}

// 成员网格 - 每行6个
.members-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
}

.member-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;

  &:hover {
    background: $dt-bg-hover;
  }

  .member-avatar-wrap {
    position: relative;

    :deep(.member-avatar) {
      border-radius: 50%;
    }

    .role-icon {
      position: absolute;
      bottom: -2px;
      right: -2px;
      width: 18px;
      height: 18px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;

      &.owner {
        background: $dt-danger;
      }

      &.admin {
        background: #FAAD14;
      }

      .el-icon {
        font-size: 12px;
      }
    }

    &.add-icon {
      width: 48px;
      height: 48px;
      border: 1px dashed $dt-border-color;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: $dt-text-tertiary;
      transition: all 0.15s;

      .el-icon {
        font-size: 20px;
      }
    }
  }

  &.add-member:hover .add-icon {
    border-color: $dt-blue;
    color: $dt-blue;
  }

  .member-name {
    font-size: 12px;
    color: $dt-text-primary;
    text-align: center;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

// 文件预览
.files-preview {
  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 24px;
    color: $dt-text-tertiary;

    .el-icon {
      font-size: 40px;
    }
  }

  .file-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .file-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 12px;
    border-radius: 6px;
    cursor: pointer;
    transition: background 0.15s;

    &:hover {
      background: $dt-bg-hover;
    }

    .file-icon {
      width: 36px;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #F0F2F5;
      border-radius: 6px;
      color: $dt-text-secondary;

      .el-icon {
        font-size: 18px;
      }
    }

    .file-info {
      display: flex;
      flex-direction: column;
      gap: 2px;
      flex: 1;

      .file-name {
        font-size: 14px;
        color: $dt-text-primary;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .file-size {
        font-size: 12px;
        color: $dt-text-tertiary;
      }
    }
  }
}

// 设置区域
.settings-section {
  padding: 16px 0;
  border-bottom: 1px solid $dt-border-color;

  .setting-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;

    &:not(:last-child) {
      border-bottom: 1px solid #F0F2F5;
    }

    .setting-label {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 14px;
      color: $dt-text-primary;

      .el-icon {
        font-size: 18px;
        color: $dt-text-tertiary;
      }
    }
  }
}

// 底部操作
.footer-actions {
  padding: 16px 0 0;
  text-align: center;

  .footer-btn {
    width: 100%;
    height: 40px;
    font-size: 14px;

    &.danger-btn {
      border-color: $dt-danger;
      color: $dt-danger;

      &:hover {
        background: rgba(255, 77, 79, 0.1);
      }
    }
  }
}

// 暗色模式适配
:global(.dark) {
  .group-detail-modal {
    :deep(.el-dialog) {
      background: #1e293b;
    }
  }

  .group-name {
    color: #f1f5f9;
  }

  .group-meta {
    color: #94a3b8;
  }

  .section-title {
    color: #e2e8f0;
  }

  .member-name {
    color: #94a3b8;
  }

  .setting-label {
    span {
      color: #e2e8f0;
    }
  }

  .action-btn {
    background: #334155;
    border-color: #475569;
    color: #e2e8f0;

    &:hover {
      background: rgba(0, 137, 255, 0.1);
      border-color: $dt-blue;
    }
  }
}
</style>
