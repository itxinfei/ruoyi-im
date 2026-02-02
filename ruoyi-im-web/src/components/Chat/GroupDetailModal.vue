<template>
  <el-dialog
    v-model="visible"
    :title="null"
    :width="isMobile ? '100%' : '600px'"
    :fullscreen="isMobile"
    class="group-detail-modal modern-modal"
    append-to-body
    destroy-on-close
    :show-close="false"
    :close-on-click-modal="!isMobile"
  >
    <div class="modal-container">
      <!-- Header -->
      <div class="modal-header">
        <div class="header-content">
          <h3 class="title">群组详情</h3>
          <div class="header-actions">
            <el-button circle text class="close-btn" @click="visible = false">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <div class="modal-body custom-scrollbar" v-loading="loading">
        <!-- Group Identity Card -->
        <div class="group-identity-card">
          <div class="group-avatar-section">
            <DingtalkAvatar
              :src="groupInfo?.avatar"
              :name="groupInfo?.name"
              :size="80"
              shape="square"
              class="group-avatar"
            />
            <div class="group-info">
              <div class="name-row">
                <h2 class="group-name">{{ groupInfo?.name }}</h2>
                <el-button 
                  v-if="isOwnerOrAdmin" 
                  circle 
                  text 
                  size="small"
                  class="edit-name-btn"
                  @click="handleEditGroupName"
                >
                  <el-icon><EditPen /></el-icon>
                </el-button>
              </div>
              <div class="meta-row">
                <span class="meta-item">{{ members.length }} 位成员</span>
                <span class="divider">·</span>
                <span class="meta-item">ID: {{ groupInfo?.id }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Tabs -->
        <div class="detail-tabs-wrapper">
          <el-tabs v-model="activeTab" class="modern-tabs" stretch>
            
            <!-- Overview Tab -->
            <el-tab-pane label="概览" name="overview">
              <div class="tab-content">
                <!-- Announcement -->
                <div class="section-card">
                  <div class="card-header">
                    <div class="title-with-icon">
                      <el-icon class="icon-primary"><Bell /></el-icon>
                      <span>群公告</span>
                    </div>
                    <el-button v-if="isOwnerOrAdmin" link type="primary" size="small" @click="handleEditAnnouncement">
                      编辑
                    </el-button>
                  </div>
                  <div class="announcement-content" :class="{ 'is-empty': !groupInfo?.announcement }">
                    {{ groupInfo?.announcement || '暂无群公告，点击编辑添加' }}
                  </div>
                </div>

                <!-- Members -->
                <div class="section-card">
                  <div class="card-header">
                    <div class="title-with-icon">
                      <el-icon class="icon-success"><UserFilled /></el-icon>
                      <span>群成员 <span class="count">({{ members.length }})</span></span>
                    </div>
                    <div class="actions">
                      <el-button v-if="isOwnerOrAdmin" link type="primary" size="small" @click="handleAddMembers">
                        <el-icon><Plus /></el-icon> 邀请
                      </el-button>
                      <el-button link type="info" size="small" @click="handleViewAllMembers">
                        全部 <el-icon><ArrowRight /></el-icon>
                      </el-button>
                    </div>
                  </div>
                  
                  <div class="members-grid">
                    <div 
                      v-for="member in displayMembers" 
                      :key="member.id" 
                      class="member-item"
                      @click="handleMemberClick(member)"
                    >
                      <div class="avatar-box">
                        <DingtalkAvatar
                          :src="member.avatar"
                          :name="member.name"
                          :size="48"
                          shape="circle"
                        />
                        <div v-if="member.role === 'OWNER'" class="role-badge owner">
                          <el-icon><Star /></el-icon>
                        </div>
                        <div v-else-if="member.role === 'ADMIN'" class="role-badge admin">
                          <el-icon><User /></el-icon>
                        </div>
                      </div>
                      <span class="member-name">{{ member.name }}</span>
                    </div>

                    <div v-if="isOwnerOrAdmin" class="member-item add-btn" @click="handleAddMembers">
                      <div class="avatar-box dashed">
                        <el-icon><Plus /></el-icon>
                      </div>
                      <span class="member-name">邀请</span>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- Files Tab -->
            <el-tab-pane label="文件" name="files">
              <div class="tab-content">
                <div class="files-header">
                  <span>最近文件</span>
                  <el-button link type="primary" @click="handleViewFiles">查看全部</el-button>
                </div>
                
                <div v-if="recentFiles.length > 0" class="file-list">
                  <div v-for="file in recentFiles" :key="file.id" class="file-item" @click="handleFileClick(file)">
                    <div class="file-icon">
                      <el-icon><Document /></el-icon>
                    </div>
                    <div class="file-info">
                      <div class="file-name">{{ file.name }}</div>
                      <div class="file-meta">{{ formatFileSize(file.size) }} · {{ file.uploaderName }}</div>
                    </div>
                  </div>
                </div>
                <div v-else class="empty-state">
                  <el-empty description="暂无群文件" :image-size="100">
                    <template #image>
                      <div class="empty-icon-bg">
                        <el-icon><Folder /></el-icon>
                      </div>
                    </template>
                  </el-empty>
                </div>
              </div>
            </el-tab-pane>

            <!-- Settings Tab -->
            <el-tab-pane label="设置" name="settings">
              <div class="tab-content">
                <div class="settings-group">
                  <div class="setting-item">
                    <div class="label-group">
                      <div class="label">消息免打扰</div>
                      <div class="desc">开启后，将不再接收此群的消息通知</div>
                    </div>
                    <el-switch v-model="groupInfo.isMuted" @change="handleMuteChange" />
                  </div>
                  
                  <div class="setting-item">
                    <div class="label-group">
                      <div class="label">置顶聊天</div>
                      <div class="desc">将此群固定在聊天列表顶部</div>
                    </div>
                    <el-switch v-model="groupInfo.isPinned" @change="handlePinChange" />
                  </div>
                </div>

                <div class="danger-zone">
                  <el-button 
                    v-if="isOwner" 
                    type="danger" 
                    plain 
                    class="danger-btn"
                    @click="handleDismiss"
                  >
                    解散群聊
                  </el-button>
                  <el-button 
                    v-else 
                    type="danger" 
                    plain 
                    class="danger-btn"
                    @click="handleLeave"
                  >
                    退出群聊
                  </el-button>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useWindowSize } from '@vueuse/core'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Close, Plus, EditPen, Bell, User, Star, UserFilled,
  Folder, Document, ArrowRight
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
  addGroupMembers
} from '@/api/im/group'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  groupId: { type: [Number, String], default: null }
})

const emit = defineEmits(['update:modelValue', 'refresh', 'show-member-profile', 'view-files', 'view-all-members'])

const { width } = useWindowSize()
const isMobile = computed(() => width.value < 768)

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const activeTab = ref('overview')
const loading = ref(false)
const groupInfo = ref({})
const members = ref([])
const currentUserId = ref(null)
const recentFiles = ref([])

// Computed
const isOwner = computed(() => groupInfo.value?.ownerId === currentUserId.value)
const isAdmin = computed(() => {
  if (isOwner.value) return true
  const m = members.value.find(x => x.id === currentUserId.value)
  return m?.role === 'ADMIN'
})
const isOwnerOrAdmin = computed(() => isOwner.value || isAdmin.value)
const displayMembers = computed(() => {
  const maxCount = isOwnerOrAdmin.value ? 19 : 20 
  return members.value.slice(0, maxCount)
})

// Methods
const loadCurrentUser = () => {
  const u = getStoredUserInfo()
  if (u) currentUserId.value = u.userId || u.id
}

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
    recentFiles.value = [] 
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
    activeTab.value = 'overview'
  }
})

const handleMemberClick = (member) => emit('show-member-profile', member)

const handleEditAnnouncement = async () => {
  const { value } = await ElMessageBox.prompt('编辑群公告', {
    confirmButtonText: '保存',
    cancelButtonText: '取消',
    inputValue: groupInfo.value.announcement || '',
    inputType: 'textarea',
    inputPlaceholder: '请输入群公告内容...'
  })
  if (value === null) return
  try {
    await updateGroup({ groupId: props.groupId, announcement: value })
    groupInfo.value.announcement = value
    ElMessage.success('群公告已更新')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('更新失败')
  }
}

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
    if (e !== 'cancel') ElMessage.error('修改失败')
  }
}

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
        inputErrorMessage: '请输入有效的用户ID'
      }
    )
    if (!value) return
    const userIds = value.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id))
    if (userIds.length === 0) return
    
    await addGroupMembers(props.groupId, userIds)
    ElMessage.success(`已邀请 ${userIds.length} 位成员`)
    loadGroupDetail()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('邀请失败')
  }
}

const handleViewAllMembers = () => emit('view-all-members', members.value)
const handleViewFiles = () => emit('view-files', props.groupId)
const handleFileClick = (file) => ElMessage.info('文件预览功能开发中')

const handleMuteChange = async (v) => {
  try {
    await setGroupMute({ groupId: props.groupId, isMuted: v })
    ElMessage.success(v ? '已开启免打扰' : '已关闭免打扰')
  } catch (e) {
    groupInfo.value.isMuted = !v
    ElMessage.error('设置失败')
  }
}

const handlePinChange = (v) => {
  ElMessage.success(v ? '已置顶' : '已取消置顶')
}

const handleDismiss = () => {
  ElMessageBox.confirm('确定要解散群组吗？此操作不可逆。', '警告', {
    type: 'error',
    confirmButtonText: '确定解散',
    cancelButtonText: '取消',
    confirmButtonClass: 'el-button--danger'
  }).then(async () => {
    await dismissGroup(props.groupId)
    ElMessage.success('群聊已解散')
    emit('refresh')
    visible.value = false
  }).catch(() => {})
}

const handleLeave = () => {
  ElMessageBox.confirm('确定退出群聊吗？', '提示', {
    type: 'warning',
    confirmButtonText: '确定退出',
    cancelButtonText: '取消',
    confirmButtonClass: 'el-button--danger'
  }).then(async () => {
    await leaveGroup(props.groupId)
    ElMessage.success('已退出群聊')
    emit('refresh')
    visible.value = false
  }).catch(() => {})
}

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}
</script>

<style scoped lang="scss">
.modern-modal {
  :deep(.el-dialog__header) {
    display: none;
  }
  :deep(.el-dialog__body) {
    padding: 0;
    height: 100%;
  }
  border-radius: 16px;
  overflow: hidden;
}

.modal-container {
  display: flex;
  flex-direction: column;
  height: 65vh;
  min-height: 550px;
  background-color: var(--el-bg-color-page);

  @media (max-width: 768px) {
    height: 100vh;
  }
}

.modal-header {
  background-color: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-lighter);
  padding: 16px 24px;
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .title {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }
    
    .close-btn {
      font-size: 20px;
      color: var(--el-text-color-regular);
      &:hover { color: var(--el-text-color-primary); }
    }
  }
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  
  @media (max-width: 768px) {
    padding: 16px;
  }
}

.group-identity-card {
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  
  .group-avatar-section {
    display: flex;
    align-items: center;
    gap: 20px;
    
    .group-avatar {
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
      border-radius: 12px;
    }
    
    .group-info {
      flex: 1;
      
      .name-row {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;
        
        .group-name {
          margin: 0;
          font-size: 20px;
          font-weight: 600;
          color: var(--el-text-color-primary);
        }
        
        .edit-name-btn {
          color: var(--el-text-color-secondary);
          &:hover { color: var(--el-color-primary); }
        }
      }
      
      .meta-row {
        display: flex;
        align-items: center;
        color: var(--el-text-color-secondary);
        font-size: 13px;
        
        .divider {
          margin: 0 8px;
        }
      }
    }
  }
}

.modern-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 24px;
  }
  :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
    background-color: var(--el-border-color-lighter);
  }
  :deep(.el-tabs__item) {
    font-size: 15px;
    font-weight: 500;
    &.is-active {
      font-weight: 600;
    }
  }
}

.section-card {
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    .title-with-icon {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 600;
      font-size: 15px;
      color: var(--el-text-color-primary);
      
      .el-icon { font-size: 18px; }
      .icon-primary { color: var(--el-color-primary); }
      .icon-success { color: var(--el-color-success); }
      
      .count {
        color: var(--el-text-color-secondary);
        font-weight: normal;
        font-size: 13px;
        margin-left: 4px;
      }
    }
  }
}

.announcement-content {
  font-size: 14px;
  line-height: 1.6;
  color: var(--el-text-color-regular);
  white-space: pre-wrap;
  padding: 12px 16px;
  background-color: var(--el-fill-color-light);
  border-radius: 8px;
  
  &.is-empty {
    color: var(--el-text-color-placeholder);
    font-style: italic;
  }
}

.members-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(70px, 1fr));
  gap: 16px;
  
  .member-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    cursor: pointer;
    
    .avatar-box {
      position: relative;
      width: 48px;
      height: 48px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: transform 0.2s;
      
      &.dashed {
        border: 1px dashed var(--el-border-color);
        color: var(--el-text-color-secondary);
        &:hover {
          border-color: var(--el-color-primary);
          color: var(--el-color-primary);
        }
      }
      
      &:hover:not(.dashed) {
        transform: scale(1.05);
      }
      
      .role-badge {
        position: absolute;
        bottom: -2px;
        right: -2px;
        width: 18px;
        height: 18px;
        border-radius: 50%;
        color: #fff;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 10px;
        border: 2px solid var(--el-bg-color);
        
        &.owner { background-color: var(--el-color-warning); }
        &.admin { background-color: var(--el-color-primary); }
      }
    }
    
    .member-name {
      margin-top: 8px;
      font-size: 12px;
      color: var(--el-text-color-regular);
      text-align: center;
      width: 100%;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.files-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.file-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  
  .file-item {
    display: flex;
    align-items: center;
    padding: 12px;
    background: var(--el-bg-color);
    border-radius: 12px;
    border: 1px solid var(--el-border-color-lighter);
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      border-color: var(--el-color-primary-light-5);
      background-color: var(--el-color-primary-light-9);
    }
    
    .file-icon {
      width: 40px;
      height: 40px;
      border-radius: 8px;
      background-color: var(--el-fill-color);
      color: var(--el-color-primary);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      margin-right: 12px;
    }
    
    .file-info {
      flex: 1;
      min-width: 0;
      
      .file-name {
        font-size: 14px;
        font-weight: 500;
        margin-bottom: 4px;
        color: var(--el-text-color-primary);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .file-meta {
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }
  }
}

.settings-group {
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: 0 20px;
  margin-bottom: 32px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  
  .setting-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 0;
    border-bottom: 1px solid var(--el-border-color-lighter);
    
    &:last-child {
      border-bottom: none;
    }
    
    .label-group {
      .label {
        font-size: 15px;
        font-weight: 500;
        color: var(--el-text-color-primary);
        margin-bottom: 4px;
      }
      .desc {
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }
  }
}

.danger-zone {
  padding: 0 20px;
  
  .danger-btn {
    width: 100%;
    height: 44px;
    font-size: 15px;
  }
}

.empty-state {
  padding: 40px 0;
  .empty-icon-bg {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background-color: var(--el-fill-color);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32px;
    color: var(--el-text-color-placeholder);
    margin: 0 auto;
  }
}

// Custom Scrollbar
.custom-scrollbar {
  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  &::-webkit-scrollbar-thumb {
    background-color: rgba(0, 0, 0, 0.1);
    border-radius: 3px;
    &:hover { background-color: rgba(0, 0, 0, 0.2); }
  }
}
</style>
