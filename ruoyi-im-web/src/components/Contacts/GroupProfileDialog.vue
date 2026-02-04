<template>
  <el-dialog
    v-model="visible"
    :width="640"
    class="group-profile-dialog"
    :show-close="false"
    :close-on-click-modal="true"
    destroy-on-close
    append-to-body
  >
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="6" animated />
    </div>

    <div v-else-if="groupInfo" class="profile-container">
      <!-- 左侧：群组基本信息和快捷操作 -->
      <GroupProfileHeader
        :group-info="groupInfo"
        :member-count="members.length"
        :is-owner="isOwner"
        :is-admin="isAdmin"
        @close="handleClose"
      />

      <GroupActions
        :is-owner-or-admin="isOwnerOrAdmin"
        @send-message="handleSendMessage"
        @invite="handleInviteMember"
        @manage="handleManage"
      />

      <!-- 右侧：详细信息和功能 -->
      <div class="profile-main">
        <el-tabs v-model="activeTab" class="profile-tabs">
          <!-- 成员 Tab -->
          <el-tab-pane name="members">
            <template #label>
              <span>成员</span>
              <el-badge :value="members.length" :max="99" class="tab-badge" />
            </template>
            <GroupMemberList
              :members="members"
              :current-user="currentUser"
              :is-owner="isOwner"
              :is-admin="isAdmin"
              @invite="handleInviteMember"
              @mute-all="handleMuteAll"
              @member-click="handleMemberClick"
              @set-admin="handleSetAdmin"
              @cancel-admin="handleCancelAdmin"
              @mute-member="handleMuteMember"
              @remove-member="handleRemoveMember"
            />
          </el-tab-pane>

          <!-- 公告 Tab -->
          <el-tab-pane name="announcements">
            <template #label>
              <span>公告</span>
              <el-badge v-if="announcements.length > 0" :value="announcements.length" :max="99" class="tab-badge" />
            </template>
            <AnnouncementList
              :announcements="announcements"
              :can-manage="isOwnerOrAdmin"
              @create="handleCreateAnnouncement"
              @edit="handleEditAnnouncement"
              @delete="handleDeleteAnnouncement"
            />
          </el-tab-pane>

          <!-- 二维码 Tab -->
          <el-tab-pane name="qrcode">
            <template #label>
              <span>二维码</span>
              <el-icon><Grid /></el-icon>
            </template>
            <GroupQRCode :group-info="groupInfo" />
          </el-tab-pane>

          <!-- 文件 Tab -->
          <el-tab-pane name="files">
            <template #label>
              <span>文件</span>
              <el-icon><Folder /></el-icon>
            </template>
            <GroupFiles
              :group-id="groupInfo?.id"
              @open-in-chat="handleOpenFileInChat"
              @preview-image="handlePreviewImage"
            />
          </el-tab-pane>

          <!-- 设置 Tab -->
          <el-tab-pane label="设置" name="settings">
            <GroupSettingsPanel
              :group-info="groupInfo"
              :settings="groupSettings"
              :is-muted="isMuted"
              :is-top="isTop"
              :is-owner="isOwner"
              :is-admin="isAdmin"
              @edit-name="handleEditGroupName"
              @edit-desc="handleEditGroupDesc"
              @setting-change="handleSettingChange"
              @mute-change="handleMuteChange"
              @top-change="handleTopChange"
              @transfer="handleTransfer"
              @dismiss="handleDismiss"
              @exit="handleExit"
            />
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </el-dialog>

  <!-- 发布公告弹窗 -->
  <el-dialog
    v-model="showAnnouncementDialog"
    :title="editingAnnouncement ? '编辑公告' : '发布公告'"
    width="480px"
    class="announcement-dialog"
    append-to-body
  >
    <el-form :model="announcementForm" label-position="top">
      <el-form-item label="标题" required>
        <el-input
          v-model="announcementForm.title"
          placeholder="请输入公告标题"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="内容" required>
        <el-input
          v-model="announcementForm.content"
          type="textarea"
          :rows="4"
          placeholder="请输入公告内容"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="announcementForm.isPinned">置顶公告</el-checkbox>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showAnnouncementDialog = false">取消</el-button>
      <el-button type="primary" @click="handleSaveAnnouncement">确定</el-button>
    </template>
  </el-dialog>

  <!-- 转让群主弹窗 -->
  <el-dialog
    v-model="showTransferDialog"
    title="转让群主"
    width="400px"
    class="transfer-dialog"
    append-to-body
  >
    <p class="transfer-hint">请选择新的群主：</p>
    <div class="transfer-member-list">
      <div
        v-for="member in transferableMembers"
        :key="member.id || member.userId"
        class="transfer-member-item"
        :class="{ selected: selectedNewOwner?.id === member.id }"
        @click="selectedNewOwner = member"
      >
        <DingtalkAvatar
          :src="member.avatar"
          :name="member.nickname || member.username"
          :size="40"
          shape="circle"
        />
        <span class="member-name">{{ member.nickname || member.username }}</span>
        <el-icon v-if="selectedNewOwner?.id === member.id" class="check-icon"><Check /></el-icon>
      </div>
    </div>
    <template #footer>
      <el-button @click="showTransferDialog = false">取消</el-button>
      <el-button type="primary" :disabled="!selectedNewOwner" @click="handleConfirmTransfer">确定转让</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Grid, Folder } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
// 子组件
import GroupProfileHeader from './GroupProfile/GroupProfileHeader.vue'
import GroupActions from './GroupProfile/GroupActions.vue'
import GroupMemberList from './GroupProfile/GroupMemberList.vue'
import GroupSettingsPanel from './GroupProfile/GroupSettingsPanel.vue'
import AnnouncementList from './GroupProfile/AnnouncementList.vue'
import GroupQRCode from './GroupProfile/GroupQRCode.vue'
import GroupFiles from './GroupProfile/GroupFiles.vue'
// API
import { getGroup, getGroupMembers, setGroupAdmin, transferGroupOwner, dismissGroup, leaveGroup } from '@/api/im/group'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  groupId: { type: [String, Number], default: null }
})

const emit = defineEmits([
  'update:modelValue', 'send-message', 'invite', 'manage',
  'exit', 'dismiss', 'update-group', 'member-click'
])

const store = useStore()
const currentUser = computed(() => store.getters['user/currentUser'] || {})

// 响应式数据
const visible = ref(false)
const groupInfo = ref(null)
const loading = ref(false)
const activeTab = ref('members')
const members = ref([])
const isMuted = ref(false)
const isTop = ref(false)
const showAnnouncementDialog = ref(false)
const showTransferDialog = ref(false)
const editingAnnouncement = ref(null)
const selectedNewOwner = ref(null)
const announcementForm = ref({ title: '', content: '', isPinned: false })
const groupSettings = ref({ needVerify: false, allowInvite: true, allMuted: false })

// 模拟公告数据
const announcements = ref([])

// 计算属性
const isOwner = computed(() => {
  const myMember = members.value.find(m => (m.userId || m.id) == currentUser.value?.id)
  return myMember?.role === 'OWNER'
})

const isAdmin = computed(() => {
  const myMember = members.value.find(m => (m.userId || m.id) == currentUser.value?.id)
  return myMember?.role === 'ADMIN'
})

const isOwnerOrAdmin = computed(() => isOwner.value || isAdmin.value)

const transferableMembers = computed(() => {
  return members.value.filter(m => (m.userId || m.id) != currentUser.value?.id)
})

// 加载群组信息
const loadGroupInfo = async () => {
  if (!props.groupId) return
  loading.value = true
  try {
    const [groupRes, membersRes] = await Promise.all([
      getGroup(props.groupId).catch(() => ({ code: 500, data: null })),
      getGroupMembers(props.groupId).catch(() => ({ code: 500, data: [] }))
    ])

    if (groupRes.code === 200 && groupRes.data) {
      groupInfo.value = groupRes.data
      groupSettings.value = {
        needVerify: groupRes.data.needVerify || false,
        allowInvite: groupRes.data.allowInvite !== false,
        allMuted: groupRes.data.allMuted || false
      }
    } else {
      // 确保即使失败也有默认值
      groupInfo.value = { name: '群聊' }
    }

    // 确保 members 始终是数组
    if (membersRes.code === 200 && Array.isArray(membersRes.data)) {
      members.value = membersRes.data
    } else {
      members.value = []
    }

    loadAnnouncements()
  } catch (error) {
    console.error('加载群组信息失败', error)
    // 确保错误情况下也有默认值
    groupInfo.value = { name: '群聊' }
    members.value = []
  } finally {
    loading.value = false
  }
}

const loadAnnouncements = () => {
  announcements.value = []
}

// 监听弹窗打开
watch(() => props.modelValue, (isOpen) => {
  visible.value = isOpen
  if (isOpen) {
    loadGroupInfo()
    activeTab.value = 'members'
  }
})

watch(visible, (val) => {
  if (!val) emit('update:modelValue', false)
})

// 事件处理
const handleClose = () => {
  visible.value = false
}

const handleSendMessage = () => {
  emit('send-message', groupInfo.value)
  handleClose()
}

const handleInviteMember = () => {
  emit('invite', groupInfo.value)
}

const handleManage = () => {
  emit('manage', groupInfo.value)
}

const handleMemberClick = (member) => {
  emit('member-click', member)
}

// 新的子组件事件处理方法
const handleMuteAll = () => {
  groupSettings.value.allMuted = !groupSettings.value.allMuted
  handleSettingChange('allMuted', groupSettings.value.allMuted)
}

const handleSetAdmin = async ({ member, memberId }) => {
  try {
    await setGroupAdmin(props.groupId, memberId, true)
    loadGroupInfo()
  } catch (e) {
    ElMessage.error('设置失败')
  }
}

const handleCancelAdmin = async ({ member, memberId }) => {
  try {
    await setGroupAdmin(props.groupId, memberId, false)
    loadGroupInfo()
  } catch (e) {
    ElMessage.error('取消失败')
  }
}

const handleMuteMember = (member) => {
  // 实现禁言逻辑
}

const handleRemoveMember = async ({ member, memberId }) => {
  try {
    const { removeGroupMember } = await import('@/api/im/group')
    await removeGroupMember(props.groupId, [memberId])
    ElMessage.success('已移出群聊')
    loadGroupInfo()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleCreateAnnouncement = () => {
  editingAnnouncement.value = null
  announcementForm.value = { title: '', content: '', isPinned: false }
  showAnnouncementDialog.value = true
}

const handleEditAnnouncement = (announcement) => {
  editingAnnouncement.value = announcement
  announcementForm.value = {
    title: announcement.title,
    content: announcement.content,
    isPinned: announcement.isPinned
  }
  showAnnouncementDialog.value = true
}

const handleSaveAnnouncement = () => {
  if (!announcementForm.value.title.trim()) {
    ElMessage.warning('请输入公告标题')
    return
  }
  if (!announcementForm.value.content.trim()) {
    ElMessage.warning('请输入公告内容')
    return
  }

  if (editingAnnouncement.value) {
    const index = announcements.value.findIndex(a => a.id === editingAnnouncement.value.id)
    if (index !== -1) {
      announcements.value[index] = {
        ...editingAnnouncement.value,
        ...announcementForm.value
      }
    }
    ElMessage.success('公告已更新')
  } else {
    announcements.value.unshift({
      id: Date.now(),
      ...announcementForm.value,
      createTime: new Date().toISOString(),
      publisherName: currentUser.value?.nickname || currentUser.value?.username
    })
    ElMessage.success('公告已发布')
  }
  showAnnouncementDialog.value = false
}

const handleDeleteAnnouncement = (announcement) => {
  ElMessageBox.confirm('确定要删除这条公告吗？', '删除公告', { type: 'warning' })
    .then(() => {
      const index = announcements.value.findIndex(a => a.id === announcement.id)
      if (index !== -1) {
        announcements.value.splice(index, 1)
      }
      ElMessage.success('公告已删除')
    }).catch(() => {})
}

const handleEditGroupName = () => {
  ElMessageBox.prompt('请输入新的群名称', '修改群名称', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: groupInfo.value.name,
    inputValidator: (value) => {
      if (!value || value.trim() === '') return '群名称不能为空'
      if (value.length > 50) return '群名称不能超过50个字符'
      return true
    }
  }).then(async ({ value }) => {
    try {
      const { updateGroup } = await import('@/api/im/group')
      await updateGroup(props.groupId, { name: value.trim() })
      groupInfo.value.name = value.trim()
      ElMessage.success('群名称已修改')
      emit('update-group', groupInfo.value)
    } catch (e) {
      ElMessage.error('修改失败')
    }
  }).catch(() => {})
}

const handleEditGroupDesc = () => {
  ElMessageBox.prompt('请输入新的群简介', '修改群简介', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: groupInfo.value.description || '',
    inputType: 'textarea',
    inputValidator: (value) => {
      if (value && value.length > 200) return '群简介不能超过200个字符'
      return true
    }
  }).then(async ({ value }) => {
    try {
      const { updateGroup } = await import('@/api/im/group')
      await updateGroup(props.groupId, { description: value.trim() })
      groupInfo.value.description = value.trim()
      ElMessage.success('群简介已修改')
      emit('update-group', groupInfo.value)
    } catch (e) {
      ElMessage.error('修改失败')
    }
  }).catch(() => {})
}

const handleSettingChange = async (key, value) => {
  try {
    const { updateGroup } = await import('@/api/im/group')
    const updateData = { [key]: value }
    await updateGroup(props.groupId, updateData)
    ElMessage.success('设置已保存')
    emit('update-group', { ...groupInfo.value, ...updateData })
  } catch (e) {
    groupSettings.value[key] = !value
    ElMessage.error('设置保存失败')
  }
}

const handleMuteChange = (val) => {
  ElMessage.success(val ? '已开启消息免打扰' : '已取消消息免打扰')
}

const handleTopChange = (val) => {
  ElMessage.success(val ? '已置顶会话' : '已取消置顶')
}

const handleTransfer = () => {
  selectedNewOwner.value = null
  showTransferDialog.value = true
}

const handleConfirmTransfer = async () => {
  if (!selectedNewOwner.value) return
  const memberName = selectedNewOwner.value.nickname || selectedNewOwner.value.username

  try {
    await ElMessageBox.confirm(
      `确定要将群主转让给 ${memberName} 吗？转让后您将变为普通成员。`,
      '转让群主',
      { type: 'warning' }
    )
    await transferGroupOwner(props.groupId, selectedNewOwner.value.userId || selectedNewOwner.value.id)
    ElMessage.success('群主转让成功')
    showTransferDialog.value = false
    loadGroupInfo()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('转让失败')
    }
  }
}

const handleDismiss = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要解散群聊 "${groupInfo.value.name}" 吗？此操作不可恢复！`,
      '解散群聊',
      { type: 'danger', confirmButtonText: '确定解散' }
    )
    await dismissGroup(props.groupId)
    ElMessage.success('群聊已解散')
    emit('dismiss', props.groupId)
    handleClose()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('解散失败')
    }
  }
}

const handleExit = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要退出群聊 "${groupInfo.value.name}" 吗？`,
      '退出群聊',
      { type: 'warning' }
    )
    await leaveGroup(props.groupId)
    ElMessage.success('已退出群聊')
    emit('exit', props.groupId)
    handleClose()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('退出失败')
    }
  }
}

// 文件相关处理
const handleOpenFileInChat = (file) => {
  // 切换到对应会话并定位到消息
  emit('send-message', groupInfo.value)
  handleClose()
  ElMessage.info('正在跳转到消息...')
}

const handlePreviewImage = (file) => {
  // 预览图片
  ElMessage.info('预览: ' + file.name)
}</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.group-profile-dialog {
  :deep(.el-dialog) {
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  }

  :deep(.el-dialog__header) {
    display: none;
  }

  :deep(.el-dialog__body) {
    padding: 0;
  }
}

.loading-state {
  padding: 60px 40px;
}

.profile-container {
  display: flex;
  min-height: 550px;
  max-height: 750px;
}

// 右侧主内容区
.profile-main {
  flex: 1;
  background: var(--dt-bg-body);
  display: flex;
  flex-direction: column;
  overflow: hidden;

  :deep(.profile-tabs) {
    flex: 1;
    display: flex;
    flex-direction: column;

    .el-tabs__header {
      margin: 0;
      padding: 0 20px;
      background: #fff;
      border-bottom: 1px solid var(--dt-border-lighter);
    }

    .el-tabs__nav-wrap::after {
      display: none;
    }

    .el-tabs__item {
      height: 56px;
      line-height: 56px;
      font-size: 14px;
      color: var(--dt-text-secondary);

      &.is-active {
        color: var(--dt-brand-color);
        font-weight: 500;
      }
    }

    .el-tabs__active-bar {
      height: 3px;
      border-radius: 2px;
    }

    .el-tabs__content {
      flex: 1;
      overflow-y: auto;
      padding: 20px;
    }
  }
}

.tab-content {
  min-height: 100%;
}

// Tab 徽标
.tab-badge {
  :deep(.el-badge__content) {
    margin-left: 6px;
    margin-top: -2px;
  }
}

.member-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid var(--dt-border-lighter);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: var(--dt-brand-color);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }

  .member-avatar-wrapper {
    position: relative;

    .online-indicator {
      position: absolute;
      bottom: 2px;
      right: 2px;
      width: 12px;
      height: 12px;
      background: #52c41a;
      border: 2px solid #fff;
      border-radius: 50%;
    }
  }

  .member-info {
    flex: 1;
    min-width: 0;

    .member-name {
      font-size: 14px;
      color: var(--dt-text-primary);
      margin-bottom: 2px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;

      .member-remark {
        color: var(--dt-text-secondary);
        font-size: 12px;
      }
    }

    .member-role {
      :deep(.el-tag) {
        height: 20px;
        padding: 0 6px;
      }

      .role-text {
        font-size: 12px;
        color: var(--dt-text-tertiary);
      }
    }
  }

  .member-actions {
    opacity: 0;
    transition: opacity 0.2s;
  }

  &:hover .member-actions {
    opacity: 1;
  }
}

// 公告区域
.announcement-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--dt-text-secondary);

  p {
    margin-top: 16px;
    font-size: 14px;
  }

  .empty-hint {
    font-size: 12px;
    color: var(--dt-text-tertiary);
    margin-top: 8px;
  }
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.announcement-item {
  padding: 16px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid var(--dt-border-lighter);

  &.pinned {
    background: rgba(var(--dt-brand-color-rgb), 0.05);
    border-color: rgba(var(--dt-brand-color-rgb), 0.2);
  }

  .announcement-header-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;

    .announcement-title {
      font-size: 15px;
      font-weight: 500;
      color: var(--dt-text-primary);
      display: flex;
      align-items: center;
      gap: 6px;

      .pin-icon {
        color: var(--dt-brand-color);
      }
    }

    .announcement-time {
      font-size: 12px;
      color: var(--dt-text-tertiary);
    }
  }

  .announcement-content {
    font-size: 14px;
    color: var(--dt-text-secondary);
    line-height: 1.6;
    margin-bottom: 12px;
  }

  .announcement-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .publisher {
      font-size: 12px;
      color: var(--dt-text-tertiary);
    }

    .announcement-actions {
      display: flex;
      gap: 8px;
    }
  }
}

// 设置区域
.settings-section {
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin-bottom: 12px;
  padding-left: 12px;
  border-left: 3px solid var(--dt-brand-color);
}

.settings-list {
  background: #fff;
  border-radius: 12px;
  border: 1px solid var(--dt-border-lighter);
  overflow: hidden;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid var(--dt-border-lighter);

  &:last-child {
    border-bottom: none;
  }

  .setting-info {
    display: flex;
    flex-direction: column;
    gap: 2px;

    .setting-name {
      font-size: 14px;
      color: var(--dt-text-primary);
    }

    .setting-value {
      font-size: 13px;
      color: var(--dt-text-secondary);
      max-width: 200px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .setting-desc {
      font-size: 12px;
      color: var(--dt-text-tertiary);
    }
  }
}

// 危险操作区域
.danger-section {
  .section-title {
    border-left-color: #ff4d4f;
  }
}

.action-list {
  background: #fff;
  border-radius: 12px;
  border: 1px solid var(--dt-border-lighter);
  overflow: hidden;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 1px solid var(--dt-border-lighter);

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: var(--dt-bg-session-hover);
  }

  .action-icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    background: var(--dt-bg-body);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    .el-icon {
      font-size: 20px;
      color: var(--dt-text-secondary);
    }
  }

  .action-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 2px;

    .action-name {
      font-size: 14px;
      color: var(--dt-text-primary);
      font-weight: 500;
    }

    .action-desc {
      font-size: 12px;
      color: var(--dt-text-tertiary);
    }
  }

  .action-arrow {
    color: var(--dt-text-tertiary);
  }

  &.danger {
    .action-icon {
      background: #fff2f0;

      .el-icon {
        color: #ff4d4f;
      }
    }

    .action-name {
      color: #ff4d4f;
    }
  }
}

// 弹窗样式
.announcement-dialog,
.transfer-dialog {
  :deep(.el-dialog__body) {
    padding-top: 20px;
  }
}

.transfer-hint {
  margin: 0 0 16px;
  color: var(--dt-text-secondary);
  font-size: 14px;
}

.transfer-member-list {
  max-height: 300px;
  overflow-y: auto;

  .transfer-member-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
    margin-bottom: 4px;

    &:hover,
    &.selected {
      background: var(--dt-bg-session-hover);
    }

    .member-name {
      flex: 1;
      font-size: 14px;
      color: var(--dt-text-primary);
    }

    .check-icon {
      color: var(--dt-brand-color);
    }
  }
}

// 响应式适配
@media (max-width: 768px) {
  .group-profile-dialog {
    :deep(.el-dialog) {
      width: 95% !important;
      margin: 20px auto;
    }
  }

  .profile-container {
    flex-direction: column;
    min-height: auto;
    max-height: 85vh;
  }

  .profile-sidebar {
    width: 100%;
    padding: 24px;

    .sidebar-content {
      flex-direction: row;
      align-items: flex-start;
      gap: 20px;
    }

    .avatar-wrapper {
      margin-bottom: 0;
    }

    .group-info {
      flex: 1;
      text-align: left;
    }

    .group-tags {
      justify-content: flex-start;
    }

    .quick-actions {
      width: auto;
      margin-left: auto;
    }
  }

  .members-list {
    grid-template-columns: 1fr;
  }
}
</style>
