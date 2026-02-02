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
      <div class="profile-sidebar">
        <!-- 关闭按钮 -->
        <el-button class="close-btn" circle @click="handleClose">
          <el-icon><Close /></el-icon>
        </el-button>

        <div class="sidebar-content">
          <div class="avatar-wrapper">
            <DingtalkAvatar
              :src="groupInfo.avatar"
              :name="groupInfo.name"
              :size="100"
              shape="square"
            />
          </div>

          <h2 class="group-name">{{ groupInfo.name }}</h2>
          <p class="group-meta">
            <span class="group-id">群号: {{ groupInfo.groupCode || groupInfo.id }}</span>
            <span class="member-count">{{ members.length }} 人</span>
          </p>
          <p class="group-intro">{{ groupInfo.description || '暂无群简介' }}</p>

          <div class="group-tags">
            <el-tag v-if="groupInfo.groupType" size="small" type="primary" effect="light">
              {{ groupInfo.groupType === 'INTERNAL' ? '内部群' : '外部群' }}
            </el-tag>
            <el-tag v-if="isOwner" size="small" type="danger" effect="light">群主</el-tag>
            <el-tag v-else-if="isAdmin" size="small" type="warning" effect="light">管理员</el-tag>
          </div>

          <div class="quick-actions">
            <el-button type="primary" size="large" @click="handleSendMessage">
              <el-icon><ChatDotRound /></el-icon>发消息
            </el-button>
            <div class="secondary-actions">
              <el-button size="large" @click="handleInviteMember">
                <el-icon><Plus /></el-icon>邀请
              </el-button>
              <el-button v-if="isOwnerOrAdmin" size="large" @click="handleManage">
                <el-icon><Setting /></el-icon>管理
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：详细信息和功能 -->
      <div class="profile-main">
        <el-tabs v-model="activeTab" class="profile-tabs">
          <!-- 成员 Tab -->
          <el-tab-pane name="members">
            <template #label>
              <span>成员</span>
              <el-badge :value="members.length" :max="99" class="tab-badge" />
            </template>
            <div class="tab-content">
              <!-- 成员搜索 -->
              <div class="members-header">
                <el-input
                  v-model="memberSearch"
                  placeholder="搜索成员"
                  clearable
                  class="member-search"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
                <el-dropdown v-if="isOwnerOrAdmin" @command="handleBatchCommand">
                  <el-button>
                    批量管理<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="invite">邀请成员</el-dropdown-item>
                      <el-dropdown-item command="mute" divided>全员禁言</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>

              <!-- 成员列表 -->
              <div class="members-list">
                <div
                  v-for="member in displayedMembers"
                  :key="member.id || member.userId"
                  class="member-item"
                  @click="handleMemberClick(member)"
                >
                  <div class="member-avatar-wrapper">
                    <DingtalkAvatar
                      :src="member.avatar"
                      :name="member.nickname || member.username"
                      :size="44"
                      shape="circle"
                    />
                    <div v-if="isOnline(member)" class="online-indicator" title="在线"></div>
                  </div>
                  <div class="member-info">
                    <div class="member-name">
                      {{ member.nickname || member.username }}
                      <span v-if="member.remark" class="member-remark">({{ member.remark }})</span>
                    </div>
                    <div class="member-role">
                      <el-tag
                        v-if="member.role === 'OWNER'"
                        size="small"
                        type="danger"
                        effect="light"
                      >群主</el-tag>
                      <el-tag
                        v-else-if="member.role === 'ADMIN'"
                        size="small"
                        type="warning"
                        effect="light"
                      >管理员</el-tag>
                      <span v-else class="role-text">成员</span>
                    </div>
                  </div>
                  <div v-if="isOwnerOrAdmin && !isSelf(member)" class="member-actions">
                    <el-dropdown trigger="click" @command="handleMemberCommand($event, member)">
                      <el-button link type="primary" @click.stop>
                        <el-icon><MoreFilled /></el-icon>
                      </el-button>
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item v-if="isOwner && member.role !== 'ADMIN'" command="setAdmin">
                            设为管理员
                          </el-dropdown-item>
                          <el-dropdown-item v-if="isOwner && member.role === 'ADMIN'" command="cancelAdmin">
                            取消管理员
                          </el-dropdown-item>
                          <el-dropdown-item command="mute">禁言</el-dropdown-item>
                          <el-dropdown-item command="remove" divided>
                            <span style="color: #ff4d4f">移出群聊</span>
                          </el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 公告 Tab -->
          <el-tab-pane name="announcements">
            <template #label>
              <span>公告</span>
              <el-badge v-if="announcements.length > 0" :value="announcements.length" :max="99" class="tab-badge" />
            </template>
            <div class="tab-content">
              <div class="announcement-header">
                <el-button
                  v-if="isOwnerOrAdmin"
                  type="primary"
                  @click="handleCreateAnnouncement"
                >
                  <el-icon><Plus /></el-icon>发布公告
                </el-button>
              </div>

              <div v-if="announcements.length === 0" class="empty-state">
                <el-icon :size="48" color="#dcdfe6"><Bell /></el-icon>
                <p>暂无群公告</p>
                <p v-if="isOwnerOrAdmin" class="empty-hint">点击上方按钮发布公告</p>
              </div>

              <div v-else class="announcement-list">
                <div
                  v-for="announcement in announcements"
                  :key="announcement.id"
                  class="announcement-item"
                  :class="{ pinned: announcement.isPinned }"
                >
                  <div class="announcement-header-row">
                    <div class="announcement-title">
                      <el-icon v-if="announcement.isPinned" class="pin-icon"><Top /></el-icon>
                      {{ announcement.title }}
                    </div>
                    <span class="announcement-time">{{ formatTime(announcement.createTime) }}</span>
                  </div>
                  <div class="announcement-content">{{ announcement.content }}</div>
                  <div class="announcement-footer">
                    <span class="publisher">{{ announcement.publisherName || '群主' }}</span>
                    <div class="announcement-actions">
                      <el-button v-if="isOwnerOrAdmin" link type="primary" size="small" @click="handleEditAnnouncement(announcement)">
                        编辑
                      </el-button>
                      <el-button v-if="isOwnerOrAdmin" link type="danger" size="small" @click="handleDeleteAnnouncement(announcement)">
                        删除
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 设置 Tab -->
          <el-tab-pane label="设置" name="settings">
            <div class="tab-content">
              <!-- 群信息设置 -->
              <div class="settings-section">
                <div class="section-title">群信息</div>
                <div class="settings-list">
                  <div class="setting-item">
                    <div class="setting-info">
                      <span class="setting-name">群名称</span>
                      <span class="setting-value">{{ groupInfo.name }}</span>
                    </div>
                    <el-button v-if="isOwnerOrAdmin" link type="primary" @click="handleEditGroupName">
                      <el-icon><Edit /></el-icon>修改
                    </el-button>
                  </div>
                  <div class="setting-item">
                    <div class="setting-info">
                      <span class="setting-name">群简介</span>
                      <span class="setting-value">{{ groupInfo.description || '暂无简介' }}</span>
                    </div>
                    <el-button v-if="isOwnerOrAdmin" link type="primary" @click="handleEditGroupDesc">
                      <el-icon><Edit /></el-icon>修改
                    </el-button>
                  </div>
                </div>
              </div>

              <!-- 群权限设置 -->
              <div v-if="isOwnerOrAdmin" class="settings-section">
                <div class="section-title">群权限</div>
                <div class="settings-list">
                  <div class="setting-item">
                    <div class="setting-info">
                      <span class="setting-name">入群验证</span>
                      <span class="setting-desc">新成员加入需要管理员审核</span>
                    </div>
                    <el-switch v-model="groupSettings.needVerify" @change="handleSettingChange('needVerify', $event)" />
                  </div>
                  <div class="setting-item">
                    <div class="setting-info">
                      <span class="setting-name">允许成员邀请</span>
                      <span class="setting-desc">群成员可以邀请其他人加入</span>
                    </div>
                    <el-switch v-model="groupSettings.allowInvite" @change="handleSettingChange('allowInvite', $event)" />
                  </div>
                  <div class="setting-item">
                    <div class="setting-info">
                      <span class="setting-name">全员禁言</span>
                      <span class="setting-desc">只有群主和管理员可以发言</span>
                    </div>
                    <el-switch v-model="groupSettings.allMuted" @change="handleSettingChange('allMuted', $event)" />
                  </div>
                </div>
              </div>

              <!-- 我的设置 -->
              <div class="settings-section">
                <div class="section-title">我的设置</div>
                <div class="settings-list">
                  <div class="setting-item">
                    <div class="setting-info">
                      <span class="setting-name">消息免打扰</span>
                      <span class="setting-desc">不再接收该群的消息通知</span>
                    </div>
                    <el-switch v-model="isMuted" @change="handleMuteChange" />
                  </div>
                  <div class="setting-item">
                    <div class="setting-info">
                      <span class="setting-name">置顶会话</span>
                      <span class="setting-desc">将该群置顶在会话列表</span>
                    </div>
                    <el-switch v-model="isTop" @change="handleTopChange" />
                  </div>
                </div>
              </div>

              <!-- 危险操作 -->
              <div class="settings-section danger-section">
                <div class="section-title">危险操作</div>
                <div class="action-list">
                  <div v-if="isOwner" class="action-item" @click="handleTransfer">
                    <div class="action-icon">
                      <el-icon><Switch /></el-icon>
                    </div>
                    <div class="action-content">
                      <span class="action-name">转让群主</span>
                      <span class="action-desc">将群主身份转让给其他成员</span>
                    </div>
                    <el-icon class="action-arrow"><ArrowRight /></el-icon>
                  </div>
                  <div v-if="isOwner" class="action-item danger" @click="handleDismiss">
                    <div class="action-icon">
                      <el-icon><Delete /></el-icon>
                    </div>
                    <div class="action-content">
                      <span class="action-name">解散群聊</span>
                      <span class="action-desc">解散后将删除所有聊天记录</span>
                    </div>
                    <el-icon class="action-arrow"><ArrowRight /></el-icon>
                  </div>
                  <div v-else class="action-item danger" @click="handleExit">
                    <div class="action-icon">
                      <el-icon><CircleClose /></el-icon>
                    </div>
                    <div class="action-content">
                      <span class="action-name">退出群聊</span>
                      <span class="action-desc">退出后将不再接收群消息</span>
                    </div>
                    <el-icon class="action-arrow"><ArrowRight /></el-icon>
                  </div>
                </div>
              </div>
            </div>
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
import {
  Close, ChatDotRound, Plus, Setting, Search, MoreFilled,
  Bell, Top, Edit, Mute, Switch, Delete, CircleClose,
  ArrowRight, Check, ArrowDown
} from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
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
const memberSearch = ref('')
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

const filteredMembers = computed(() => {
  if (!memberSearch.value) return members.value
  const keyword = memberSearch.value.toLowerCase()
  return members.value.filter(m =>
    (m.nickname || m.username || '').toLowerCase().includes(keyword)
  )
})

const displayedMembers = computed(() => filteredMembers.value)

const transferableMembers = computed(() => {
  return members.value.filter(m => (m.userId || m.id) != currentUser.value?.id)
})

// 加载群组信息
const loadGroupInfo = async () => {
  if (!props.groupId) return
  loading.value = true
  try {
    const [groupRes, membersRes] = await Promise.all([
      getGroup(props.groupId),
      getGroupMembers(props.groupId)
    ])

    if (groupRes.code === 200) {
      groupInfo.value = groupRes.data
      groupSettings.value = {
        needVerify: groupRes.data.needVerify || false,
        allowInvite: groupRes.data.allowInvite !== false,
        allMuted: groupRes.data.allMuted || false
      }
    }

    if (membersRes.code === 200) {
      members.value = membersRes.data || []
    }

    loadAnnouncements()
  } catch (error) {
    console.error('加载群组信息失败', error)
    ElMessage.error('加载群组信息失败')
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

const isSelf = (member) => {
  return (member.userId || member.id) == currentUser.value?.id
}

const isOnline = (member) => {
  return Math.random() > 0.5
}

const handleBatchCommand = (command) => {
  switch (command) {
    case 'invite':
      handleInviteMember()
      break
    case 'mute':
      groupSettings.value.allMuted = !groupSettings.value.allMuted
      handleSettingChange('allMuted', groupSettings.value.allMuted)
      break
  }
}

const handleMemberCommand = async (command, member) => {
  const memberName = member.nickname || member.username
  const memberId = member.userId || member.id

  switch (command) {
    case 'setAdmin':
      try {
        await setGroupAdmin(props.groupId, memberId, true)
        ElMessage.success(`已将 ${memberName} 设为管理员`)
        loadGroupInfo()
      } catch (e) {
        ElMessage.error('设置失败')
      }
      break
    case 'cancelAdmin':
      try {
        await setGroupAdmin(props.groupId, memberId, false)
        ElMessage.success(`已取消 ${memberName} 的管理员权限`)
        loadGroupInfo()
      } catch (e) {
        ElMessage.error('取消失败')
      }
      break
    case 'mute':
      ElMessage.success(`已禁言 ${memberName}`)
      break
    case 'remove':
      ElMessageBox.confirm(
        `确定要将 ${memberName} 移出群聊吗？`,
        '移出群聊',
        { type: 'warning' }
      ).then(async () => {
        try {
          const { removeGroupMember } = await import('@/api/im/group')
          await removeGroupMember(props.groupId, [memberId])
          ElMessage.success('已移出群聊')
          loadGroupInfo()
        } catch (e) {
          ElMessage.error('操作失败')
        }
      }).catch(() => {})
      break
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

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 86400000) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (diff < 604800000) {
    const days = Math.floor(diff / 86400000)
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
  }
}
</script>

<style scoped lang="scss">
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

// 左侧边栏
.profile-sidebar {
  width: 240px;
  background: linear-gradient(180deg, var(--dt-brand-color) 0%, #0e5fd9 100%);
  color: #fff;
  padding: 28px 20px;
  position: relative;
  display: flex;
  flex-direction: column;

  .close-btn {
    position: absolute;
    top: 16px;
    right: 16px;
    background: rgba(255, 255, 255, 0.2);
    border: none;
    color: #fff;

    &:hover {
      background: rgba(255, 255, 255, 0.3);
    }
  }

  .sidebar-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
  }

  .avatar-wrapper {
    margin-bottom: 20px;

    :deep(.dingtalk-avatar) {
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
    }
  }

  .group-name {
    font-size: 22px;
    font-weight: 600;
    margin: 0 0 8px;
    color: #fff;
    text-align: center;
  }

  .group-meta {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.85);
    margin: 0 0 8px;
    display: flex;
    align-items: center;
    gap: 12px;

    .group-id,
    .member-count {
      background: rgba(255, 255, 255, 0.15);
      padding: 2px 10px;
      border-radius: 12px;
    }
  }

  .group-intro {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.7);
    margin: 0 0 16px;
    text-align: center;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .group-tags {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 8px;
    margin-bottom: 32px;

    :deep(.el-tag) {
      background: rgba(255, 255, 255, 0.2);
      border-color: rgba(255, 255, 255, 0.3);
      color: #fff;
    }
  }

  .quick-actions {
    width: 100%;

    .el-button {
      width: 100%;
      margin-bottom: 12px;
      background: #fff;
      border-color: #fff;
      color: var(--dt-brand-color);
      font-size: 15px;
      height: 44px;

      &:hover {
        background: rgba(255, 255, 255, 0.9);
      }

      .el-icon {
        margin-right: 6px;
      }
    }

    .secondary-actions {
      display: flex;
      gap: 12px;

      .el-button {
        flex: 1;
        background: rgba(255, 255, 255, 0.15);
        border: 1px solid rgba(255, 255, 255, 0.3);
        color: #fff;
        margin-bottom: 0;

        &:hover {
          background: rgba(255, 255, 255, 0.25);
        }
      }
    }
  }
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

// 成员区域
.members-header {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;

  .member-search {
    flex: 1;
  }
}

.members-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
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
