<template>
  <el-drawer
    v-model="visible"
    size="320px"
    direction="rtl"
    :with-header="false"
    class="im-detail-drawer"
  >
    <div v-loading="loading" class="drawer-content">
      <!-- 1. 头部：基本信息 (对齐钉钉内联编辑) -->
      <section class="section hero-section">
        <div class="hero-main">
          <div class="avatar-edit-wrapper" :class="{ 'can-edit': isGroupOwner }" @click="handleEditAvatar">
            <DingtalkAvatar :src="session.avatar" :name="session.name" :size="48" shape="square" />
            <div v-if="isGroupOwner" class="avatar-edit-mask"><el-icon><Camera /></el-icon></div>
          </div>
          <div class="hero-info">
            <div v-if="!isEditingName" class="name-row" @click="startEditName">
              <h3 class="name">{{ session.name }}</h3>
              <el-icon v-if="isGroupOwner" class="edit-icon"><EditPen /></el-icon>
            </div>
            <el-input
              v-else
              ref="nameInputRef"
              v-model="editingName"
              size="small"
              class="name-input"
              @blur="saveName"
              @keyup.enter="saveName"
            />
            <p class="sub-text">{{ session.type === 'GROUP' ? `群号: ${session.targetId}` : '个人会话' }}</p>
          </div>
        </div>
      </section>

      <!-- 2. 群成员区域 (4列网格) -->
      <section v-if="session.type === 'GROUP'" class="section member-section">
        <div class="section-header">
          <span class="title">群成员</span>
          <span class="count">{{ allMembers.length }}人</span>
        </div>
        <div class="member-grid">
          <div v-for="m in members" :key="m.userId" class="member-item" @click="emit('show-user', m.userId)" @contextmenu="handleMemberRightClick($event, m)">
            <div class="avatar-wrapper">
              <DingtalkAvatar :src="m.avatar" :name="m.nickname" :size="34" />
              <span v-if="m.role === 'OWNER'" class="role-badge owner"><el-icon><StarFilled /></el-icon></span>
              <span v-else-if="m.role === 'ADMIN'" class="role-badge admin"><el-icon><Microphone /></el-icon></span>
            </div>
            <span class="nickname">{{ m.nickname }}</span>
          </div>
          <!-- 邀请按钮 -->
          <div class="member-item add-btn" @click="handleInvite">
            <div class="icon-box"><el-icon><Plus /></el-icon></div>
            <span class="nickname">邀请</span>
          </div>
          <!-- 更多/收起 -->
          <div v-if="hasMoreMembers" class="member-item more-btn" @click="toggleShowAllMembers">
            <div class="icon-box"><el-icon><More /></el-icon></div>
            <span class="nickname">更多</span>
          </div>
        </div>
      </section>

      <!-- 3. 功能操作区 -->
      <section class="section menu-section">
        <div class="menu-item" @click="handleShowAnnouncement">
          <div class="menu-left">
            <el-icon class="icon-orange"><Bell /></el-icon>
            <div class="menu-text">
              <span>群公告</span>
              <p v-if="latestAnnouncement" class="menu-sub">{{ latestAnnouncement }}</p>
            </div>
          </div>
          <el-icon><ArrowRight /></el-icon>
        </div>

        <div class="menu-item" @click="handleFileClick">
          <div class="menu-left">
            <el-icon class="icon-blue"><FolderOpened /></el-icon>
            <span>群文件</span>
          </div>
          <el-icon><ArrowRight /></el-icon>
        </div>

        <div v-if="session.type === 'GROUP'" class="menu-item" @click="handleShowQrCode">
          <div class="menu-left">
            <el-icon class="icon-green"><Link /></el-icon>
            <span>群二维码</span>
          </div>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </section>

      <!-- 4. 设置开关区 -->
      <section class="section settings-section">
        <div class="setting-row">
          <span>置顶会话</span>
          <el-switch :model-value="session.isPinned" @change="togglePin" />
        </div>
        <div class="setting-row">
          <span>消息免打扰</span>
          <el-switch :model-value="session.isMuted" @change="toggleMuteSession" />
        </div>
        <div v-if="isGroupOwner" class="setting-row">
          <span>全员禁言</span>
          <el-switch :model-value="isAllMuted" @change="toggleAllMuted" />
        </div>
      </section>

      <!-- 5. 危险操作区 -->
      <div class="bottom-actions">
        <el-button type="info" plain class="w-full action-btn" @click="handleClearChat">清空聊天记录</el-button>
        <el-button v-if="session.type === 'GROUP'" type="danger" plain class="w-full action-btn" @click="handleLeaveGroup">
          {{ isGroupOwner ? '解散群聊' : '退出群聊' }}
        </el-button>
      </div>
    </div>

    <!-- 附属面板 -->
    <GroupFilePanel ref="groupFilePanelRef" :group-id="session.targetId" />
    <CallDialog ref="callDialogRef" :session="session" />
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Camera, EditPen, Plus, More, Bell, FolderOpened,
  Link, ArrowRight, StarFilled, Microphone
} from '@element-plus/icons-vue'
import { getGroupMembers, getGroupAnnouncements, leaveGroup } from '@/api/im/group'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import GroupFilePanel from '@/components/GroupDetailDrawer/GroupFilePanel.vue'
import CallDialog from '@/components/Chat/CallDialog.vue'

const props = defineProps({
  modelValue: Boolean,
  session: Object
})
const emit = defineEmits(['update:modelValue', 'show-user', 'switch-module'])
const store = useStore()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const allMembers = ref([])
const members = ref([])
const showAllMembers = ref(false)
const announcements = ref([])
const isEditingName = ref(false)
const editingName = ref('')
const nameInputRef = ref(null)

const currentUserId = computed(() => store.getters['user/currentUser']?.id)
const isGroupOwner = computed(() => allMembers.value.some(m => m.role === 'OWNER' && m.userId === currentUserId.value))
const hasMoreMembers = computed(() => allMembers.value.length > 11 && !showAllMembers.value)
const latestAnnouncement = computed(() => announcements.value[0]?.content || '')

const loadData = async () => {
  if (!props.session?.targetId || props.session.type !== 'GROUP') return
  loading.value = true
  try {
    const [mRes, aRes] = await Promise.all([
      getGroupMembers(props.session.targetId),
      getGroupAnnouncements(props.session.targetId)
    ])
    if (mRes.code === 200) {
      allMembers.value = mRes.data || []
      members.value = allMembers.value.slice(0, 11)
    }
    if (aRes.code === 200) announcements.value = aRes.data || []
  } finally {
    loading.value = false
  }
}

watch(visible, (val) => { if (val) loadData() })

const startEditName = () => {
  if (!isGroupOwner.value) return
  editingName.value = props.session.name
  isEditingName.value = true
  nextTick(() => nameInputRef.value?.focus())
}

const saveName = async () => {
  if (!isEditingName.value) return
  if (editingName.value === props.session.name || !editingName.value.trim()) {
    isEditingName.value = false; return
  }
  try {
    const res = await updateGroup(props.session.targetId, { groupName: editingName.value })
    if (res.code === 200) {
      ElMessage.success('修改成功')
      // eslint-disable-next-line vue/no-mutating-props
      props.session.name = editingName.value
    }
  } finally {
    isEditingName.value = false
  }
}

const toggleShowAllMembers = () => {
  members.value = showAllMembers.value ? allMembers.value.slice(0, 11) : allMembers.value
  showAllMembers.value = !showAllMembers.value
}

const handleInvite = () => ElMessage.info('邀请功能联调中')
const handleEditAvatar = () => isGroupOwner.value && ElMessage.info('头像修改功能联调中')
const handleShowAnnouncement = () => ElMessage.info('公告详情建设中')
const handleFileClick = () => ElMessage.info('文件面板联调中')
const handleShowQrCode = () => ElMessage.info('二维码生成中')

const togglePin = (val) => store.dispatch('im/session/pinSession', { sessionId: props.session.id, pinned: val })
const toggleMuteSession = (val) => store.dispatch('im/session/muteSession', { sessionId: props.session.id, muted: val })

const handleClearChat = () => {
  ElMessageBox.confirm('确定清空聊天记录吗？', '提示', { type: 'warning' }).then(() => {
    store.dispatch('im/message/clearMessages', props.session.id)
    ElMessage.success('已清空')
  })
}

const handleLeaveGroup = () => {
  const text = isGroupOwner.value ? '确定解散该群聊吗？' : '确定退出该群聊吗？'
  ElMessageBox.confirm(text, '警告', { type: 'error' }).then(async () => {
    await leaveGroup(props.session.targetId)
    visible.value = false
    emit('switch-module', 'chat')
  })
}
</script>

<style lang="scss" scoped>
.im-detail-drawer {
  :deep(.el-drawer__body) { padding: 0; background: var(--dt-bg-body); border-radius: 8px 0 0 8px; }
}

.drawer-content {
  height: 100%; display: flex; flex-direction: column; overflow-y: auto;
  &::-webkit-scrollbar { width: 4px; }
}

.section { background: var(--dt-bg-card); margin-bottom: 8px; padding: 20px; }

.hero-section {
  .hero-main { display: flex; align-items: center; gap: 16px; }
  .avatar-edit-wrapper {
    position: relative; cursor: default;
    &.can-edit { cursor: pointer; &:hover .avatar-edit-mask { opacity: 1; } }
    .avatar-edit-mask {
      position: absolute; inset: 0; background: rgba(0,0,0,0.5); color: var(--dt-text-white);
      @include flex-center; border-radius: 4px; opacity: 0; transition: var(--dt-transition-fast);
    }
  }
  .hero-info {
    flex: 1; min-width: 0;
    .name-row { display: flex; align-items: center; gap: 8px; cursor: pointer; &:hover .edit-icon { opacity: 1; } }
    .name { font-size: 16px; font-weight: 600; margin: 0; @include text-ellipsis; }
    .edit-icon { font-size: 14px; color: var(--dt-brand-color); opacity: 0; transition: var(--dt-transition-fast); }
    .sub-text { font-size: 12px; color: var(--dt-text-tertiary); margin-top: 4px; }
  }
}

.member-section {
  .section-header { display: flex; justify-content: space-between; margin-bottom: 16px; .title { font-weight: 600; } .count { font-size: 12px; color: var(--dt-text-tertiary); } }
  .member-grid {
    display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px;
    .member-item {
      display: flex; flex-direction: column; align-items: center; gap: 6px; cursor: pointer;
      .avatar-wrapper { position: relative; .role-badge { position: absolute; bottom: -2px; right: -2px; width: 14px; height: 14px; border-radius: 50%; @include flex-center; font-size: 10px; color: var(--dt-text-white); &.owner { background: var(--dt-warning-color); } &.admin { background: var(--dt-brand-color); } } }
      .nickname { font-size: 11px; color: var(--dt-text-secondary); width: 100%; text-align: center; @include text-ellipsis; }
      .icon-box { width: 34px; height: 34px; border-radius: 4px; border: 1px dashed var(--dt-border-light); @include flex-center; color: var(--dt-text-tertiary); }
      &.add-btn:hover .icon-box { border-color: var(--dt-brand-color); color: var(--dt-brand-color); }
      &.more-btn:hover .icon-box { background: var(--dt-bg-hover); }
    }
  }
}

.menu-section {
  padding: 8px 0;
  .menu-item {
    height: 54px; padding: 0 20px; display: flex; align-items: center; justify-content: space-between; cursor: pointer;
    &:hover { background: var(--dt-bg-hover); }
    .menu-left { display: flex; align-items: center; gap: 12px; font-size: 14px; color: var(--dt-text-primary); .el-icon { font-size: 18px; } }
    .menu-text { display: flex; flex-direction: column; .menu-sub { font-size: 12px; color: var(--dt-text-tertiary); margin-top: 2px; max-width: 200px; @include text-ellipsis; } }
    .icon-orange { color: var(--dt-warning-color); } .icon-blue { color: var(--dt-brand-color); } .icon-green { color: var(--dt-success-color); }
  }
}

.settings-section {
  .setting-row { height: 44px; display: flex; align-items: center; justify-content: space-between; font-size: 14px; color: var(--dt-text-primary); }
}

.bottom-actions {
  margin-top: auto; padding: 20px; display: flex; flex-direction: column; gap: 12px;
  .action-btn { width: 100%; margin: 0; }
}
</style>
