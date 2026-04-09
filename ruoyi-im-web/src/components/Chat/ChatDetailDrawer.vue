<script setup>
import { ref, watch, computed } from 'vue'
import { useStore } from 'vuex'
import { getGroup, leaveGroup, getGroupQrCode, setGroupAdmin, muteGroupMember, transferGroupOwner, getGroupAnnouncements, getGroupMembers } from '@/api/im/group'
import { getUserInfo } from '@/api/im/user'
import { getGroupFileStatistics } from '@/api/im/groupFile'
import { initiateCall } from '@/api/im/videoCall'
import { clearConversationMessages } from '@/api/im/message'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Phone, VideoCamera, Link, Bell, Microphone, StarFilled, Mute, More, ArrowUp } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import GroupFilePanel from '@/components/GroupDetailDrawer/GroupFilePanel.vue'
import CallDialog from '@/components/Chat/CallDialog.vue'
import GroupCallPanel from '@/components/Chat/GroupCallPanel.vue'

const props = defineProps({
  modelValue: Boolean,
  session: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'show-user', 'switch-module'])
const store = useStore()

const loading = ref(false)
const members = ref([])
const allMembers = ref([])  // 保存完整成员列表
const fileStats = ref(null)
const userInfo = ref(null)
const groupFilePanelRef = ref(null)
const groupCallPanelRef = ref(null)
const callDialogRef = ref(null)
const qrCodeDialogVisible = ref(false)
const qrCodeUrl = ref('')
const qrCodeLoading = ref(false)
const announcementDialogVisible = ref(false)
const announcementContent = ref('')
const announcements = ref([])
const showAllMembers = ref(false)  // 是否显示全部成员
const currentUserId = computed(() => store.getters['user/currentUser']?.id)
const isGroupOwner = computed(() => members.value.some(m => m.role === 'OWNER' && m.userId === currentUserId.value))
const isGroupAdmin = computed(() => members.value.some(m => m.role === 'ADMIN' && m.userId === currentUserId.value))
// 是否有更多成员可显示
const hasMoreMembers = computed(() => {
  return allMembers.value.length > 12 && !showAllMembers.value
})

// 成员右键菜单
const memberContextMenu = ref({
  show: false,
  x: 0,
  y: 0,
  member: null
})

const loadData = async () => {
  if (!props.session?.id) return
  loading.value = true
  try {
    if (props.session.type === 'GROUP') {
      const groupRes = await getGroup(props.session.targetId)
      if (groupRes.code === 200) {
        // 群组成员单独获取
        const membersRes = await getGroupMembers(props.session.targetId)
        if (membersRes.code === 200) {
          // 保存全部成员
          allMembers.value = (membersRes.data || []).map(m => ({
            userId: m.userId,
            nickname: m.groupNickname || m.userName,
            avatar: m.userAvatar,
            role: m.role,
            isMuted: m.isMuted
          }))
          // 默认显示前12个
          members.value = allMembers.value.slice(0, 12)
          // 重置查看更多状态
          showAllMembers.value = false
        }
      }
      const statsRes = await getGroupFileStatistics(props.session.targetId)
      if (statsRes.code === 200) fileStats.value = statsRes.data
    } else {
      const userId = props.session.peerUserId || props.session.targetId
      const userRes = await getUserInfo(userId)
      if (userRes.code === 200) userInfo.value = userRes.data
    }
  } catch (e) {
    console.error('加载详情失败', e)
    ElMessage.error('加载详情失败')
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) loadData()
})

const handleClose = () => emit('update:modelValue', false)

const handleFileClick = () => {
  if (groupFilePanelRef.value) {
    groupFilePanelRef.value.open()
  }
}

// 语音通话
const handleVoiceCall = async () => {
  const peerId = props.session.peerUserId || props.session.targetId
  if (!peerId) {
    ElMessage.warning('无法获取对方ID')
    return
  }
  try {
    const res = await initiateCall({
      calleeId: peerId,
      conversationId: props.session.id,
      callType: 'VOICE'
    })
    if (res.code === 200) {
      callDialogRef.value?.open('voice', {
        status: 'calling',
        callId: res.data.callId,
        peerId,
        peerName: props.session.name,
        peerAvatar: props.session.avatar
      })
    } else {
      throw new Error(res.message || '发起通话失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '发起语音通话失败')
  }
}

// 视频通话
const handleVideoCall = async () => {
  const peerId = props.session.peerUserId || props.session.targetId
  if (!peerId) {
    ElMessage.warning('无法获取对方ID')
    return
  }
  try {
    const res = await initiateCall({
      calleeId: peerId,
      conversationId: props.session.id,
      callType: 'VIDEO'
    })
    if (res.code === 200) {
      callDialogRef.value?.open('video', {
        status: 'calling',
        callId: res.data.callId,
        peerId,
        peerName: props.session.name,
        peerAvatar: props.session.avatar
      })
    } else {
      throw new Error(res.message || '发起通话失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '发起视频通话失败')
  }
}

// 群组通话
const handleGroupCall = async (callType = 'VIDEO') => {
  if (!props.session.targetId) {
    ElMessage.warning('无法获取群ID')
    return
  }
  // 加载群成员用于选择邀请
  try {
    const memberIds = members.value.map(m => m.userId)
    groupCallPanelRef.value?.loadGroupMembers(memberIds)
    groupCallPanelRef.value?.openAsInitiator(callType, memberIds)
  } catch (error) {
    ElMessage.error('发起群组通话失败')
  }
}

// 清空聊天记录
const handleClearChat = async () => {
  try {
    await ElMessageBox.confirm('确定要清空此会话的所有消息吗？此操作不可恢复。', '清空聊天记录', {
      confirmButtonText: '确定清空',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await clearConversationMessages(props.session.id)
    if (res.code === 200) {
      // 清空前端消息缓存
      store.dispatch('im/message/clearMessages', props.session.id)
      ElMessage.success('聊天记录已清空')
    } else {
      throw new Error(res.message || '清空失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '清空聊天记录失败')
    }
  }
}

// 退出群聊
const handleLeaveGroup = async () => {
  try {
    await ElMessageBox.confirm('确定要退出该群聊吗？退出后将不再接收此群的消息。', '退出群聊', {
      confirmButtonText: '确定退出',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await leaveGroup(props.session.targetId)
    if (res.code === 200) {
      ElMessage.success('已退出群聊')
      handleClose()
      emit('switch-module', 'chat')
    } else {
      throw new Error(res.msg || '退出失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '退出群聊失败')
    }
  }
}

// 显示群二维码
const handleShowQrCode = async () => {
  qrCodeDialogVisible.value = true
  qrCodeLoading.value = true
  qrCodeUrl.value = ''
  try {
    const res = await getGroupQrCode(props.session.targetId)
    if (res.code === 200) {
      qrCodeUrl.value = res.data
    } else {
      throw new Error(res.message || '获取二维码失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '获取群二维码失败')
    qrCodeDialogVisible.value = false
  } finally {
    qrCodeLoading.value = false
  }
}

// 切换显示全部成员
const toggleShowAllMembers = () => {
  if (showAllMembers.value) {
    members.value = allMembers.value.slice(0, 12)
    showAllMembers.value = false
  } else {
    members.value = allMembers.value
    showAllMembers.value = true
  }
}

// 成员右键菜单
const handleMemberRightClick = (event, member) => {
  event.preventDefault()
  event.stopPropagation()
  memberContextMenu.value = {
    show: true,
    x: event.clientX,
    y: event.clientY,
    member
  }
}

const closeMemberMenu = () => {
  memberContextMenu.value.show = false
}

// 设置/取消管理员
const handleToggleAdmin = async (member) => {
  closeMemberMenu()
  if (member.role === 'OWNER') {
    ElMessage.warning('群主不能被设置为管理员')
    return
  }
  try {
    const isAdmin = member.role === 'ADMIN'
    const res = await setGroupAdmin(props.session.targetId, member.userId, !isAdmin)
    if (res.code === 200) {
      ElMessage.success(isAdmin ? '已取消管理员' : '已设置为管理员')
      loadData()
    } else {
      throw new Error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 禁言/取消禁言成员
const handleToggleMute = async (member) => {
  closeMemberMenu()
  if (member.userId === currentUserId.value) {
    ElMessage.warning('不能禁言自己')
    return
  }
  try {
    const isMuted = member.isMuted
    const res = await muteGroupMember(props.session.targetId, member.userId, isMuted ? null : 3600)
    if (res.code === 200) {
      ElMessage.success(isMuted ? '已取消禁言' : '已禁言该成员')
      loadData()
    } else {
      throw new Error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 转让群主
const handleTransferOwner = async (member) => {
  closeMemberMenu()
  if (member.role === 'OWNER') {
    ElMessage.warning('该成员已经是群主')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要将群主转让给 ${member.nickname} 吗？转让后你将不再是群主。`, '转让群主', {
      confirmButtonText: '确定转让',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await transferGroupOwner(props.session.targetId, member.userId)
    if (res.code === 200) {
      ElMessage.success('群主已转让')
      loadData()
    } else {
      throw new Error(res.message || '转让失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '转让群主失败')
    }
  }
}

// 查看群公告
const handleShowAnnouncement = async () => {
  announcementDialogVisible.value = true
  try {
    const res = await getGroupAnnouncements(props.session.targetId)
    if (res.code === 200) {
      announcements.value = res.data || []
    }
  } catch (error) {
    console.error('获取公告失败', error)
  }
}
</script>

<template>
  <el-drawer
    :model-value="modelValue"
    @update:model-value="val => emit('update:modelValue', val)"
    title="会话详情"
    direction="rtl"
    size="320px"
    class="im-detail-drawer"
    :with-header="false"
  >
    <div class="drawer-content" v-loading="loading">
      <!-- 头部：基本信息 -->
      <div class="section hero-section">
        <div class="hero-main">
          <DingtalkAvatar 
            :src="session.avatar" 
            :name="session.name" 
            :size="48" 
            shape="square" 
          />
          <div class="hero-info">
            <h3 class="name">{{ session.name }}</h3>
            <p class="sub">{{ session.type === 'GROUP' ? '群聊' : '单聊' }}</p>
          </div>
        </div>
      </div>

      <!-- 群成员区域 -->
      <div v-if="session.type === 'GROUP'" class="section">
        <div class="section-header">
          <span class="title">群成员</span>
          <span class="count">{{ session.memberCount }}人</span>
        </div>
        <div class="member-grid">
          <div v-for="m in members" :key="m.userId" class="member-item" @click="emit('show-user', m.userId)" @contextmenu="handleMemberRightClick($event, m)">
            <div class="avatar-wrapper">
              <DingtalkAvatar :src="m.avatar" :name="m.nickname" :size="32" shape="square" />
              <span v-if="m.role === 'OWNER'" class="role-badge owner"><el-icon><StarFilled /></el-icon></span>
              <span v-else-if="m.role === 'ADMIN'" class="role-badge admin"><el-icon><Microphone /></el-icon></span>
            </div>
            <span class="nickname">{{ m.nickname }}</span>
          </div>
          <!-- 查看更多 -->
          <div v-if="hasMoreMembers" class="member-item more-btn" @click="toggleShowAllMembers">
            <div class="icon-box more-icon"><el-icon><More /></el-icon></div>
            <span class="nickname">查看更多</span>
          </div>
          <!-- 收起 -->
          <div v-else-if="showAllMembers && allMembers.length > 12" class="member-item collapse-btn" @click="showAllMembers = false">
            <div class="icon-box"><el-icon><ArrowUp /></el-icon></div>
            <span class="nickname">收起</span>
          </div>
          <div class="member-item add-btn">
            <div class="icon-box"><el-icon><Plus /></el-icon></div>
            <span class="nickname">邀请</span>
          </div>
        </div>
      </div>

      <!-- 个人信息展示 -->
      <div v-if="session.type === 'PRIVATE' && userInfo" class="section info-list">
        <div class="info-item">
          <label>部门</label>
          <span>{{ userInfo.departmentName || '—' }}</span>
        </div>
        <div class="info-item">
          <label>职位</label>
          <span>{{ userInfo.position || '—' }}</span>
        </div>
        <!-- 快捷操作 -->
        <div class="info-actions">
          <el-button type="primary" plain size="small" @click="handleVoiceCall">
            <el-icon><Phone /></el-icon>
            语音通话
          </el-button>
          <el-button type="primary" plain size="small" @click="handleVideoCall">
            <el-icon><VideoCamera /></el-icon>
            视频通话
          </el-button>
        </div>
      </div>

      <!-- 群组通话入口 -->
      <div v-if="session.type === 'GROUP'" class="section info-actions">
        <el-button type="primary" plain size="small" @click="handleGroupCall('VOICE')">
          <el-icon><Phone /></el-icon>
          语音通话
        </el-button>
        <el-button type="primary" plain size="small" @click="handleGroupCall('VIDEO')">
          <el-icon><VideoCamera /></el-icon>
          视频通话
        </el-button>
      </div>

      <!-- 设置开关区 -->
      <div class="section settings-list">
        <div class="setting-row">
          <span>置顶会话</span>
          <el-switch :model-value="session.isPinned" />
        </div>
        <div class="setting-row">
          <span>消息免打扰</span>
          <el-switch :model-value="session.isMuted" />
        </div>
      </div>

      <!-- 文件入口 -->
      <div class="section menu-item" @click="handleFileClick">
        <div class="menu-left">
          <el-icon><FolderOpened /></el-icon>
          <span>{{ session.type === 'GROUP' ? '群文件' : '聊天文件' }}</span>
        </div>
        <el-icon><ArrowRight /></el-icon>
      </div>

      <!-- 群二维码入口 -->
      <div v-if="session.type === 'GROUP'" class="section menu-item" @click="handleShowQrCode">
        <div class="menu-left">
          <el-icon><Link /></el-icon>
          <span>群二维码</span>
        </div>
        <el-icon><ArrowRight /></el-icon>
      </div>

      <!-- 群公告入口 -->
      <div v-if="session.type === 'GROUP'" class="section menu-item" @click="handleShowAnnouncement">
        <div class="menu-left">
          <el-icon><Bell /></el-icon>
          <span>群公告</span>
        </div>
        <el-icon><ArrowRight /></el-icon>
      </div>

      <!-- 文件面板 -->
      <GroupFilePanel ref="groupFilePanelRef" :group-id="session.type === 'GROUP' ? session.targetId : null" />

      <div class="bottom-actions">
        <el-button type="danger" plain class="w-full" @click="handleClearChat">清空聊天记录</el-button>
        <el-button v-if="session.type === 'GROUP'" type="danger" plain class="w-full" @click="handleLeaveGroup">退出群聊</el-button>
      </div>
    </div>

    <!-- 通话弹窗 -->
    <CallDialog ref="callDialogRef" :session="session" />

    <!-- 群组通话面板 -->
    <GroupCallPanel ref="groupCallPanelRef" :session="session" :conversation-id="session.type === 'GROUP' ? session.id : null" />

    <!-- 群二维码弹窗 -->
    <el-dialog
      v-model="qrCodeDialogVisible"
      title="群二维码"
      width="320px"
      append-to-body
      class="qr-code-dialog"
    >
      <div class="qr-code-content" v-loading="qrCodeLoading">
        <img v-if="qrCodeUrl" :src="qrCodeUrl" alt="群二维码" class="qr-code-image" />
        <div v-else class="qr-code-empty">加载中...</div>
        <p class="qr-code-tip">扫码加入群聊</p>
      </div>
    </el-dialog>

    <!-- 成员右键菜单 -->
    <teleport to="body">
      <div
        v-if="memberContextMenu.show"
        class="member-context-menu"
        :style="{ left: memberContextMenu.x + 'px', top: memberContextMenu.y + 'px' }"
        @click.stop
      >
        <div class="menu-item" @click="handleToggleAdmin(memberContextMenu.member)">
          <el-icon><Microphone /></el-icon>
          <span>{{ memberContextMenu.member?.role === 'ADMIN' ? '取消管理员' : '设为管理员' }}</span>
        </div>
        <div class="menu-item" @click="handleToggleMute(memberContextMenu.member)">
          <el-icon><Mute /></el-icon>
          <span>{{ memberContextMenu.member?.isMuted ? '取消禁言' : '禁言该成员' }}</span>
        </div>
        <div v-if="isGroupOwner" class="menu-item" @click="handleTransferOwner(memberContextMenu.member)">
          <el-icon><StarFilled /></el-icon>
          <span>转让群主</span>
        </div>
      </div>
      <div v-if="memberContextMenu.show" class="menu-overlay" @click="closeMemberMenu" />
    </teleport>

    <!-- 群公告弹窗 -->
    <el-dialog
      v-model="announcementDialogVisible"
      title="群公告"
      width="400px"
      append-to-body
      class="announcement-dialog"
    >
      <div class="announcement-list" v-if="announcements.length > 0">
        <div v-for="item in announcements" :key="item.id" class="announcement-item">
          <div class="announcement-content">{{ item.content }}</div>
          <div class="announcement-meta">
            <span>{{ item.createBy }}</span>
            <span>{{ item.createTime }}</span>
          </div>
        </div>
      </div>
      <div v-else class="announcement-empty">暂无公告</div>
    </el-dialog>
  </el-drawer>
</template>

<style lang="scss" scoped>
.im-detail-drawer {
  :deep(.el-drawer) {
    background: var(--dt-bg-body);
    box-shadow: -4px 0 20px rgba(0, 0, 0, 0.1);  /* 抽屉阴影 */
    transition: transform var(--dt-transition-base);
  }

  :deep(.el-drawer__overlay) {
    background: var(--dt-overlay-bg);
  }

  // 钉钉规范：左侧8px圆角，右侧无
  :deep(.el-drawer__body) {
    padding: 0;
    background-color: var(--dt-bg-body);
    border-radius: 8px 0 0 8px;
  }
}

.drawer-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  .section {
    background: var(--dt-bg-card);
    margin-bottom: 8px;
    padding: 20px;  /* 钉钉规范：内边距20px */

    &.hero-section {
      padding: 24px 20px;
      .hero-main {
        display: flex;
        align-items: center;
        gap: 12px;
        .name {
          margin: 0;
          font-size: var(--dt-font-size-lg);
          color: var(--dt-text-main);
        }
        .sub {
          margin: 4px 0 0;
          font-size: var(--dt-font-size-sm);
          color: var(--dt-text-desc);
        }
      }
    }
  }

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    .title { font-weight: 500; color: var(--dt-text-main); }
    .count { font-size: var(--dt-font-size-sm); color: var(--dt-text-desc); }
  }

  .member-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
    .member-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;
      cursor: pointer;
      .avatar-wrapper {
        position: relative;
        .role-badge {
          position: absolute;
          bottom: -2px;
          right: -2px;
          width: 14px;
          height: 14px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 8px;  /* 角色角标图标8px，保持非标准值 */
          &.owner {
            background: var(--dt-warning-color);
            color: var(--dt-text-white);
          }
          &.admin {
            background: var(--dt-brand-color);
            color: var(--dt-text-white);
          }
        }
      }
      .nickname {
        font-size: var(--dt-font-size-xs);
        color: var(--dt-text-desc);
        max-width: 100%;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      &.add-btn {
        .icon-box {
          width: 32px;
          height: 32px;
          border: 1px dashed var(--dt-border-light);
          border-radius: 4px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: var(--dt-text-desc);
        }
      }

      // 查看更多样式
      &.more-btn {
        cursor: pointer;
        .more-icon {
          width: 32px;
          height: 32px;
          background: var(--dt-brand-bg);
          border: none;
          border-radius: 4px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: var(--dt-brand-color);
          font-size: var(--dt-font-size-xl);
          transition: background-color var(--dt-transition-fast), color var(--dt-transition-fast);
        }
        &:hover .more-icon {
          background: var(--dt-brand-color);
          color: var(--dt-text-white);
        }
      }

      // 收起样式
      &.collapse-btn {
        cursor: pointer;
        .icon-box {
          width: 32px;
          height: 32px;
          background: var(--dt-bg-hover);
          border: none;
          border-radius: 4px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: var(--dt-text-secondary);
          transition: background-color var(--dt-transition-fast);
        }
        &:hover .icon-box {
          background: var(--dt-border-light);
        }
      }
    }
  }

  .info-list {
    .info-item {
      display: flex;
      justify-content: space-between;
      margin-bottom: 12px;
      font-size: var(--dt-font-size-base);
      &:last-child { margin-bottom: 0; }
      label { color: var(--dt-text-desc); }
      span { color: var(--dt-text-main); }
    }
    .info-actions {
      display: flex;
      gap: 8px;
      margin-top: 12px;
      .el-button {
        flex: 1;
      }
    }
  }

  .settings-list {
    .setting-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;
      font-size: var(--dt-font-size-base);
      color: var(--dt-text-main);
    }
  }

  .menu-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    cursor: pointer;
    &:hover { background: var(--dt-bg-hover); }
    .menu-left {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: var(--dt-font-size-base);
      color: var(--dt-text-main);
    }
  }

  .bottom-actions {
    margin-top: auto;
    padding: 16px;
    background: var(--dt-bg-card);
  }
}

.qr-code-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 0;
  .qr-code-image {
    width: 240px;
    height: 240px;
    border-radius: var(--dt-radius-lg);
  }
  .qr-code-empty {
    width: 240px;
    height: 240px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--dt-text-desc);
    background: var(--dt-bg-hover);
    border-radius: var(--dt-radius-lg);
  }
  .qr-code-tip {
    margin: 12px 0 0;
    font-size: var(--dt-font-size-base);
    color: var(--dt-text-desc);
  }
}

.member-context-menu {
  position: fixed;
  z-index: 9999;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  box-shadow: var(--dt-shadow-2);
  padding: 4px 0;
  min-width: 140px;
  .menu-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-main);
    cursor: pointer;
    transition: background var(--dt-transition-fast);
    &:hover {
      background: var(--dt-bg-hover);
    }
  }
}

.menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9998;
}

.announcement-list {
  max-height: 400px;
  overflow-y: auto;
  .announcement-item {
    padding: 12px;
    border-bottom: 1px solid var(--dt-border-light);
    &:last-child { border-bottom: none; }
    .announcement-content {
      font-size: var(--dt-font-size-base);
      color: var(--dt-text-main);
      line-height: 1.6;
      white-space: pre-wrap;
    }
    .announcement-meta {
      display: flex;
      justify-content: space-between;
      margin-top: 8px;
      font-size: var(--dt-font-size-sm);
      color: var(--dt-text-desc);
    }
  }
}

.announcement-empty {
  text-align: center;
  padding: 32px;
  color: var(--dt-text-desc);
  font-size: var(--dt-font-size-base);
}
</style>
