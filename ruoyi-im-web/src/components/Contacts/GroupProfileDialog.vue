<template>
  <!-- PC端侧边栏风格的群组详情 -->
  <transition name="slide-right">
    <div
      v-if="visible"
      class="group-profile-sidebar"
    >
      <div
        class="sidebar-overlay"
        @click="handleClose"
      />

      <div class="sidebar-panel">
        <!-- 头部 -->
        <div class="sidebar-header">
          <h3 class="header-title">
            群聊信息
          </h3>
          <button
            class="close-btn"
            @click="handleClose"
          >
            <el-icon>
              <Close />
            </el-icon>
          </button>
        </div>

        <!-- 内容区 -->
        <div
          v-if="loading"
          class="sidebar-body"
        >
          <el-skeleton
            :rows="10"
            animated
          />
        </div>

        <div
          v-else-if="groupInfo"
          class="sidebar-body"
        >
          <!-- 群组基本信息卡片 -->
          <div class="info-card">
            <div class="group-header-section">
              <el-avatar
                :size="60"
                :src="groupInfo.avatar || ''"
                shape="square"
                class="group-avatar"
              >
                {{ groupInfo.name?.charAt(0) || '群' }}
              </el-avatar>
              <div class="group-basic-info">
                <h4 class="group-name">
                  {{ groupInfo.name }}
                </h4>
                <p class="group-meta">
                  {{ groupMembers.length }}人
                </p>
              </div>
            </div>

            <!-- 快捷操作按钮 -->
            <div class="quick-actions">
              <button
                class="action-item"
                @click="handleAddMember"
              >
                <el-icon>
                  <UserFilled />
                </el-icon>
                <span>邀请</span>
              </button>
              <button
                class="action-item"
                @click="shareGroup"
              >
                <el-icon>
                  <Share />
                </el-icon>
                <span>分享</span>
              </button>
              <button
                class="action-item"
                @click="copyGroupId"
              >
                <el-icon>
                  <CopyDocument />
                </el-icon>
                <span>复制ID</span>
              </button>
            </div>
          </div>

          <!-- 群成员 -->
          <div class="section-block">
            <div class="section-header">
              <span class="section-title">群成员 ({{ groupMembers.length }})</span>
              <el-input
                v-model="memberKeyword"
                placeholder="搜索成员"
                size="small"
                clearable
                class="member-search"
              >
                <template #prefix>
                  <el-icon>
                    <Search />
                  </el-icon>
                </template>
              </el-input>
            </div>

            <div class="members-list">
              <div
                v-for="member in filteredMembers"
                :key="member.userId"
                class="member-item"
                @click="viewMemberInfo(member.userId)"
              >
                <DingtalkAvatar
                  :src="member.avatar"
                  :name="member.userName"
                  :user-id="member.userId"
                  :size="40"
                  shape="square"
                  custom-class="member-avatar"
                />
                <div class="member-info">
                  <span class="member-name">{{ member.userName }}</span>
                  <span
                    v-if="member.role === 'OWNER'"
                    class="member-role owner"
                  >群主</span>
                  <span
                    v-else-if="member.role === 'ADMIN'"
                    class="member-role admin"
                  >管理员</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 群设置 -->
          <div class="section-block">
            <div class="section-header">
              <span class="section-title">群设置</span>
            </div>

            <div class="settings-list">
              <div class="setting-item">
                <span class="setting-label">群聊名称</span>
                <span class="setting-value">{{ groupInfo.name }}</span>
              </div>

              <div class="setting-item">
                <span class="setting-label">群组ID</span>
                <span class="setting-value">{{ groupInfo.id }}</span>
              </div>

              <!-- 新增：开关设置 -->
              <div class="setting-item">
                <span class="setting-label">消息免打扰</span>
                <el-switch
                  v-model="isMuted"
                  @change="handleToggleMute"
                />
              </div>

              <div class="setting-item">
                <span class="setting-label">置顶聊天</span>
                <el-switch
                  v-model="isPinned"
                  @change="handleTogglePin"
                />
              </div>

              <div
                class="setting-item clickable"
                @click="handleFiles"
              >
                <span class="setting-label">群文件</span>
                <el-icon class="setting-arrow">
                  <ArrowRight />
                </el-icon>
              </div>

              <div
                class="setting-item clickable"
                @click="handleAnnouncement"
              >
                <span class="setting-label">群公告</span>
                <el-icon class="setting-arrow">
                  <ArrowRight />
                </el-icon>
              </div>
            </div>
          </div>

          <!-- 危险操作区 -->
          <div class="section-block danger-zone">
            <button
              class="danger-btn"
              @click="handleLeaveGroup"
            >
              {{ isOwner ? '解散群聊' : '退出群聊' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </transition>

  <!-- 邀请成员弹窗 -->
  <AddMembersDialog
    v-model="showInviteDialog"
    :group-id="groupId"
    @success="handleInviteSuccess"
  />
</template>

<script setup>
import { ref, watch, computed, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Close,
  UserFilled,
  Share,
  CopyDocument,
  Search,
  ArrowRight
} from '@element-plus/icons-vue'
import { getGroup, getGroupMembers, leaveGroup, dismissGroup } from '@/api/im/group'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import AddMembersDialog from '@/components/Chat/AddMembersDialog.vue'
import { debounce } from '@/utils/debounce'

const props = defineProps({
  visible: { type: Boolean, default: false },
  groupId: { type: [Number, String], default: null }
})

const emit = defineEmits(['update:visible', 'refresh-group', 'view-member', 'show-files', 'show-announcement'])
const store = useStore()

const loading = ref(false)
const loadError = ref(null)
const groupInfo = ref(null)
const groupMembers = ref([])
const memberKeyword = ref('')
const isUnmounted = ref(false)
const showInviteDialog = ref(false)

// 设置状态
const isMuted = ref(false)
const isPinned = ref(false)

const currentUser = computed(() => store.getters['user/currentUser'])
const isOwner = computed(() => groupInfo.value?.ownerId === currentUser.value?.id)

// 初始化设置状态
const initSettings = () => {
  const session = store.getters['im/session/sessionById'](props.groupId)
  if (session) {
    isMuted.value = session.isMuted || false
    isPinned.value = session.isPinned || false
  }
}

const handleToggleMute = async val => {
  try {
    await store.dispatch('im/session/muteSession', { 
      sessionId: props.groupId, 
      isMuted: val 
    })
    ElMessage.success(val ? '已开启免打扰' : '已关闭免打扰')
  } catch (e) {
    isMuted.value = !val // 回滚
    ElMessage.error('设置失败')
  }
}

const handleTogglePin = async val => {
  try {
    await store.dispatch('im/session/pinSession', { 
      sessionId: props.groupId, 
      isPinned: val 
    })
    ElMessage.success(val ? '已置顶' : '已取消置顶')
  } catch (e) {
    isPinned.value = !val // 回滚
    ElMessage.error('设置失败')
  }
}

watch(() => props.visible, v => {
  if (v && props.groupId) {
    loadGroupInfo()
    initSettings()
  }
})

watch(() => props.groupId, gid => {
  if (gid && props.visible) {
    loadGroupInfo()
  }
})

// 成员搜索防抖处理
const debouncedMemberKeyword = ref('')
const updateDebouncedKeyword = debounce(value => {
  debouncedMemberKeyword.value = value
}, 300)

watch(memberKeyword, newVal => {
  updateDebouncedKeyword(newVal)
})

const filteredMembers = computed(() => {
  const keyword = (debouncedMemberKeyword.value || '').trim().toLowerCase()
  if (!keyword) {
    return groupMembers.value
  }
  return (groupMembers.value || []).filter(m => {
    const name = (m?.userName || '').toString().toLowerCase()
    return name.includes(keyword)
  })
})

const loadGroupInfo = async (isRetry = false) => {
  if (!props.groupId) {
    return
  }
  loading.value = true
  loadError.value = null

  try {
    const [gRes, mRes] = await Promise.all([
      getGroup(props.groupId),
      getGroupMembers(props.groupId)
    ])

    if (gRes.code === 200) {
      groupInfo.value = gRes.data
    } else {
      throw new Error(gRes.msg || '加载群组信息失败')
    }

    if (mRes.code === 200) {
      groupMembers.value = mRes.data
    } else {
      throw new Error(mRes.msg || '加载群成员失败')
    }

    loadError.value = null
  } catch (e) {
    console.error('加载群组详情失败:', e)
    loadError.value = e.message || '加载详情失败'

    // 显示错误提示,提供重试选项
    ElMessage({
      type: 'error',
      message: loadError.value,
      duration: 3000,
      showClose: true
    })
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  emit('update:visible', false)
}

const copyGroupId = () => {
  navigator.clipboard.writeText(groupInfo.value.id.toString())
  ElMessage.success('ID 已复制')
}

const viewMemberInfo = uid => { emit('view-member', uid) }

const handleAddMember = () => {
  showInviteDialog.value = true
}

const handleInviteSuccess = () => {
  loadGroupInfo()
  emit('refresh-group')
}

const shareGroup = () => {
  // TODO: 实现分享群组功能
  ElMessage.info('分享功能开发中')
}

// 触发群文件面板显示
const handleFiles = () => {
  emit('show-files', props.groupId)
}

// 触发群公告对话框显示
const handleAnnouncement = () => {
  emit('show-announcement', props.groupId)
}

const handleLeaveGroup = async () => {
  const actionText = isOwner.value ? '解散' : '退出'
  try {
    await ElMessageBox.confirm(`确定要${actionText}该群聊吗?`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = isOwner.value 
      ? await dismissGroup(props.groupId) 
      : await leaveGroup(props.groupId)

    if (res.code === 200) {
      ElMessage.success(`已${actionText}群聊`)
      handleClose()
      emit('refresh-group')
      // 跳转回首页或清空当前会话
      store.commit('im/session/SET_CURRENT_SESSION', null)
    } else {
      throw new Error(res.msg || `${actionText}群聊失败`)
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error(`${actionText}群聊失败:`, e)
      ElMessage.error(e.message || '操作失败')
    }
  }
}

// 组件卸载时清理
onUnmounted(() => {
  isUnmounted.value = true
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// 侧边栏容器
.group-profile-sidebar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 2000;
  display: flex;
  justify-content: flex-end;
}

// 遮罩层
.sidebar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--dt-bg-overlay);
  backdrop-filter: blur(2px);
}

// 侧边栏面板
.sidebar-panel {
  position: relative;
  width: 360px;
  height: 100%;
  background: var(--dt-bg-card);
  box-shadow: -2px 0 8px var(--dt-black-08);
  display: flex;
  flex-direction: column;
  z-index: 1;
}

// 头部
.sidebar-header {
  height: 56px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--dt-border-lighter);
  flex-shrink: 0;

  .header-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0;
  }

  .close-btn {
    width: 32px;
    height: 32px;
    border-radius: var(--dt-radius-md);
    border: none;
    background: transparent;
    color: var(--dt-text-secondary);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s var(--dt-ease-out);

    &:hover {
      background: var(--dt-bg-hover);
      color: var(--dt-text-primary);
    }
  }
}

// 内容区
.sidebar-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--dt-scrollbar-thumb);
    border-radius: 3px;

    &:hover {
      background: var(--dt-scrollbar-thumb-hover);
    }
  }
}

// 信息卡片
.info-card {
  background: var(--dt-bg-hover);
  border-radius: var(--dt-radius-lg);
  padding: 20px;
  margin-bottom: 16px;
}

.group-header-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;

  .group-avatar {
    border-radius: var(--dt-radius-md);
    flex-shrink: 0;
  }

  .group-basic-info {
    flex: 1;
    min-width: 0;

    .group-name {
      font-size: 16px;
      font-weight: 600;
      color: var(--dt-text-primary);
      margin: 0 0 4px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .group-meta {
      font-size: 13px;
      color: var(--dt-text-tertiary);
      margin: 0;
    }
  }
}

// 快捷操作
.quick-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;

  .action-item {
    height: 64px;
    background: var(--dt-bg-card);
    border: 1px solid var(--dt-border-lighter);
    border-radius: var(--dt-radius-md);
    cursor: pointer;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 6px;
    font-size: 13px;
    color: var(--dt-text-primary);
    transition: all 0.2s var(--dt-ease-out);

    &:hover {
      background: var(--dt-bg-hover);
      border-color: var(--dt-border);
    }

    .el-icon {
      font-size: 20px;
    }
  }
}

// 区块
.section-block {
  margin-bottom: 16px;

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;

    .section-title {
      font-size: 14px;
      font-weight: 600;
      color: var(--dt-text-primary);
    }

    .member-search {
      width: 160px;
    }
  }
}

// 成员列表
.members-list {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-lighter);
  border-radius: var(--dt-radius-md);
  overflow: hidden;

  .member-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    cursor: pointer;
    transition: background 0.2s var(--dt-ease-out);
    border-bottom: 1px solid var(--dt-border-lighter);

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background: var(--dt-bg-hover);
    }

    .member-avatar {
      border-radius: var(--dt-radius-sm);
      flex-shrink: 0;
    }

    .member-info {
      flex: 1;
      min-width: 0;
      display: flex;
      align-items: center;
      gap: 8px;

      .member-name {
        font-size: 14px;
        color: var(--dt-text-primary);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .member-role {
        font-size: 11px;
        padding: 2px 6px;
        border-radius: 3px;
        flex-shrink: 0;

        &.owner {
          background: #fff3e0;
          color: #f57c00;
        }

        &.admin {
          background: #e3f2fd;
          color: #1976d2;
        }
      }
    }
  }
}

// 设置列表
.settings-list {
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-lighter);
  border-radius: var(--dt-radius-md);
  overflow: hidden;

  .setting-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 14px 16px;
    border-bottom: 1px solid var(--dt-border-lighter);

    &:last-child {
      border-bottom: none;
    }

    &.vertical {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
    }

    &.clickable {
      cursor: pointer;
      transition: background 0.2s var(--dt-ease-out);

      &:hover {
        background: var(--dt-bg-hover);
      }
    }

    .setting-label {
      font-size: 14px;
      color: var(--dt-text-secondary);
    }

    .setting-value {
      font-size: 14px;
      color: var(--dt-text-primary);
      text-align: right;
    }

    .setting-desc {
      font-size: 13px;
      color: var(--dt-text-secondary);
      line-height: 1.6;
      margin: 0;
    }

    .setting-arrow {
      color: var(--dt-text-tertiary);
      font-size: 14px;
    }
  }
}

// 危险操作区
.danger-zone {
  .danger-btn {
    width: 100%;
    height: 40px;
    background: var(--dt-bg-card);
    border: 1px solid #f44336;
    border-radius: var(--dt-radius-md);
    color: #f44336;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s var(--dt-ease-out);

    &:hover {
      background: #f44336;
      color: #fff;
    }
  }
}

// 滑入动画
.slide-right-enter-active,
.slide-right-leave-active {
  transition: all 0.3s var(--dt-ease-out);

  .sidebar-overlay {
    transition: opacity 0.3s var(--dt-ease-out);
  }

  .sidebar-panel {
    transition: transform 0.3s var(--dt-ease-out);
  }
}

.slide-right-enter-from,
.slide-right-leave-to {
  .sidebar-overlay {
    opacity: 0;
  }

  .sidebar-panel {
    transform: translateX(100%);
  }
}

// 暗色模式 - 仅保留角色标签的特殊适配
:global(.dark) {
  .member-role {
    &.owner {
      background: rgba(245, 124, 0, 0.15);
      color: #ffb74d;
    }

    &.admin {
      background: rgba(25, 118, 210, 0.15);
      color: #64b5f6;
    }
  }
}
</style>
