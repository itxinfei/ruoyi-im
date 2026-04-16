<template>
  <div class="session-panel" @click="handleGlobalClick">
    <!-- 头部搜索与筛选 (对齐钉钉 8px 步进) -->
    <div class="panel-header">
      <div class="search-bar" :class="{ 'is-focused': isSearchFocused }">
        <el-icon class="search-icon">
          <Search />
        </el-icon>
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="搜索"
          @focus="isSearchFocused = true"
          @blur="isSearchFocused = false"
        >
      </div>
      <!-- 会话类型过滤 tabs -->
      <div class="session-tabs">
        <button
          v-for="tab in sessionTabs"
          :key="tab.value"
          class="session-tab"
          :class="{ active: filterType === tab.value }"
          @click="filterType = tab.value"
        >
          {{ tab.label }}
        </button>
      </div>
      <!-- 分组管理按钮 -->
      <div class="session-actions">
        <el-tooltip content="分组管理">
          <el-button text size="small" @click="openGroupDialog()">
            <el-icon><FolderOpened /></el-icon>
          </el-button>
        </el-tooltip>
      </div>
    </div>

    <!-- 会话列表区 -->
    <div v-loading="loading" class="session-list">
      <!-- 分类分组列表 -->
      <template v-for="group in groupedSessions" :key="group.type">
        <!-- 分类头部（可折叠） -->
        <div
          v-if="group.sessions.length > 0 || group.type === 'PINNED'"
          class="session-group"
          :class="{ 'is-collapsed': collapsedGroups[group.type] }"
        >
          <div class="group-header" @click="toggleGroup(group.type)">
            <el-icon class="group-arrow">
              <ArrowDown v-if="!collapsedGroups[group.type]" />
              <ArrowRight v-else />
            </el-icon>
            <span class="group-name">{{ group.label }}</span>
            <span v-if="group.unreadCount > 0" class="group-unread-badge">
              {{ group.unreadCount > 99 ? '99+' : group.unreadCount }}
            </span>
          </div>

          <!-- 分组内的会话项 -->
          <div v-show="!collapsedGroups[group.type]" class="group-sessions">
            <div
              v-for="session in group.sessions"
              :key="session.id"
              class="session-item"
              :class="{ 'is-active': currentSession && currentSession.id === session.id, 'is-pinned': session.isPinned }"
              @click="selectSession(session)"
              @contextmenu.prevent="handleContextMenu($event, session)"
            >
              <!-- 头像区 (对齐钉钉 4px 圆角) -->
              <div class="avatar-wrapper" @click.stop="handleAvatarClick(session)">
                <img :src="session.avatar || '/avatars/default.png'" class="avatar" alt="avatar">
                <div v-if="session.unreadCount > 0" class="unread-badge" :class="getUnreadBadgeClass(session.unreadCount)">
                  {{ session.unreadCount > 99 ? '99+' : session.unreadCount }}
                </div>
              </div>

              <!-- 内容区 -->
              <div class="content-wrapper">
                <div class="content-top">
                  <span class="session-name">{{ session.name }}</span>
                  <span class="session-time">{{ formatTime(session.lastMessageTime) }}</span>
                </div>
                <div class="content-bottom">
                  <span v-if="session.draftContent" class="draft-tag">[草稿]</span>
                  <span class="last-message">{{ session.lastMessage }}</span>
                  <el-icon v-if="session.isMuted" class="mute-icon" :size="14">
                    <BellFilled />
                  </el-icon>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- 空状态 -->
      <div v-if="!loading && filteredSessions.length === 0" class="empty-list">
        <el-icon :size="48" color="var(--dt-border-lighter)">
          <ChatLineRound />
        </el-icon>
        <p>暂无消息会话</p>
      </div>
    </div>

    <!-- 右键会话上下文菜单 -->
    <Teleport to="body">
      <div
        v-if="contextMenuVisible"
        class="session-context-menu"
        :style="contextMenuStyle"
        @click.stop
      >
        <div class="context-menu-header">
          移动到分组
        </div>
        <div
          class="context-menu-item"
          @click="moveSessionToGroup(contextMenuSession, null)"
        >
          <el-icon><FolderAdd /></el-icon>
          未分组
        </div>
        <div
          v-for="group in userSessionGroups"
          :key="group.id"
          class="context-menu-item"
          :class="{ 'is-active': contextMenuSession?.groupId === group.id }"
          @click="moveSessionToGroup(contextMenuSession, group.id)"
        >
          <el-icon><FolderOpened /></el-icon>
          {{ group.name }}
        </div>
        <div class="context-menu-divider" />
        <div class="context-menu-item" @click="openGroupDialog()">
          <el-icon><Plus /></el-icon>
          新建分组
        </div>
      </div>
    </Teleport>

    <!-- 分组管理对话框 -->
    <el-dialog
      v-model="showGroupDialog"
      :title="editingGroup ? '编辑分组' : '会话分组'"
      width="400px"
      destroy-on-close
    >
      <div class="group-dialog-content">
        <!-- 新建分组 -->
        <div class="group-create-row">
          <el-input
            v-model="newGroupName"
            placeholder="输入新分组名称"
            @keyup.enter="handleSaveGroup"
          />
          <el-button type="primary" size="small" @click="handleSaveGroup">
            新建
          </el-button>
        </div>

        <!-- 分组列表 -->
        <div class="group-list">
          <div
            v-for="group in userSessionGroups"
            :key="group.id"
            class="group-list-item"
          >
            <span class="group-item-name">{{ group.name }}</span>
            <span class="group-item-count">
              {{ sessionsByGroup(group.id)?.length || 0 }} 个会话
            </span>
            <div class="group-item-actions">
              <el-button text size="small" @click="openGroupDialog(group)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button
                text
                size="small"
                type="danger"
                @click="handleDeleteGroup(group)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
          <el-empty v-if="userSessionGroups.length === 0" description="暂无分组" :image-size="60" />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="js">
/**
 * ChatSessionList.vue (对齐钉钉 250px 规范)
 * 修复：数据初始化加载、色彩对齐、图标修正
 * 功能增强：会话分组管理（用户创建分组）
 */
import { ref, computed, onMounted, reactive } from 'vue'
import { useStore } from 'vuex'
import { Search, BellFilled, ChatLineRound, ArrowDown, ArrowRight, FolderOpened, FolderAdd, Delete, Edit, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const store = useStore()

// 内部状态
const isSearchFocused = ref(false)
const searchKeyword = ref('')
const filterType = ref('all')

// 分类折叠状态
const collapsedGroups = reactive({
  PINNED: false,
  PRIVATE: false,
  GROUP: false,
  USER_GROUP: false
})

// 会话分组相关状态
const showGroupDialog = ref(false)
const editingGroup = ref(null)
const newGroupName = ref('')
const contextMenuSession = ref(null)
const contextMenuVisible = ref(false)
const contextMenuStyle = ref({})

// 分类定义
const sessionGroups = [
  { type: 'PINNED', label: '置顶' },
  { type: 'PRIVATE', label: '单聊' },
  { type: 'GROUP', label: '群聊' }
]

// 切换分组折叠状态
const toggleGroup = (type) => {
  collapsedGroups[type] = !collapsedGroups[type]
}

// 会话类型过滤 tabs
const sessionTabs = [
  { label: '全部', value: 'all' },
  { label: '单聊', value: 'PRIVATE' },
  { label: '群聊', value: 'GROUP' }
]

// 数据绑定 (对齐 Store 路径 im/session)
const loading = computed(() => store.state.im?.session?.loading)
const currentSession = computed(() => store.state.im?.session?.currentSession)
const sessions = computed(() => store.getters['im/session/sortedSessions'])

// 会话分组相关
const userSessionGroups = computed(() => store.getters['im/session/sessionGroups'] || [])
const sessionsByGroup = (groupId) => store.getters['im/session/sessionsByGroup'](groupId)

const filteredSessions = computed(() => {
  let list = sessions.value || []
  // 按类型过滤
  if (filterType.value !== 'all') {
    list = list.filter(s => s.type === filterType.value)
  }
  // 按关键词过滤
  if (searchKeyword.value) {
    list = list.filter(s => s.name?.toLowerCase().includes(searchKeyword.value.toLowerCase()))
  }
  return list
})

// 分组会话列表（钉钉分类设计：置顶 > 单聊 > 群聊 + 用户分组）
const groupedSessions = computed(() => {
  const list = filteredSessions.value

  const typeGroups = sessionGroups.map(group => {
    let groupSessions

    if (filterType.value !== 'all') {
      // 单一类型过滤时，只显示该类型的非置顶会话
      groupSessions = list.filter(s => !s.isPinned && s.type === filterType.value)
    } else {
      // 全部类型时，按分组显示
      if (group.type === 'PINNED') {
        groupSessions = list.filter(s => s.isPinned)
      } else {
        groupSessions = list.filter(s => !s.isPinned && s.type === group.type)
      }
    }

    const unreadCount = groupSessions.reduce((sum, s) => sum + (s.unreadCount || 0), 0)

    return {
      type: group.type,
      label: group.label,
      sessions: groupSessions,
      unreadCount
    }
  })

  // 用户创建的分组（只在全部筛选时显示）
  if (filterType.value === 'all' && userSessionGroups.value.length > 0) {
    const userGroups = userSessionGroups.value.map(g => {
      // 该分组下的会话（排除已置顶的，置顶会话在置顶分组中显示）
      const groupSessions = list.filter(s => !s.isPinned && s.groupId === g.id)
      const unreadCount = groupSessions.reduce((sum, s) => sum + (s.unreadCount || 0), 0)
      return {
        type: 'USER_GROUP_' + g.id,
        label: g.name,
        sessions: groupSessions,
        unreadCount,
        groupId: g.id,
        isUserGroup: true
      }
    }).filter(g => g.sessions.length > 0)

    typeGroups.push(...userGroups)
  }

  return typeGroups
})

// 初始化加载
onMounted(() => {
  store.dispatch('im/session/loadSessions')
  store.dispatch('im/session/loadSessionGroups')
})

const selectSession = (session) => {
  store.dispatch('im/session/selectSession', session)
}

// 头像点击事件
const emit = defineEmits(['avatar-click', 'mention-user'])

const handleAvatarClick = (session) => {
  if (session.type === 'PRIVATE') {
    // 单聊：打开用户详情
    emit('avatar-click', session)
  } else {
    // 群聊：@该成员或显示成员信息
    emit('mention-user', session)
  }
}

// 未读角标圆角：1-9 圆形，10+ 胶囊
const getUnreadBadgeClass = (count) => {
  if (count >= 10) {
    return 'unread-badge-capsule'
  }
  return 'unread-badge-circle'
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()

  // 如果是今天，显示时间；否则显示日期
  if (date.toDateString() === now.toDateString()) {
    return date.getHours().toString().padStart(2, '0') + ':' +
           date.getMinutes().toString().padStart(2, '0')
  }
  return (date.getMonth() + 1) + '/' + date.getDate()
}

// ============ 会话分组管理 ============

// 打开分组管理对话框
const openGroupDialog = (group = null) => {
  editingGroup.value = group
  newGroupName.value = group ? group.name : ''
  showGroupDialog.value = true
}

// 创建或更新分组
const handleSaveGroup = async () => {
  if (!newGroupName.value.trim()) {
    ElMessage.warning('分组名称不能为空')
    return
  }
  try {
    if (editingGroup.value) {
      await store.dispatch('im/session/editSessionGroup', {
        id: editingGroup.value.id,
        name: newGroupName.value.trim()
      })
      ElMessage.success('分组名称已更新')
    } else {
      await store.dispatch('im/session/addSessionGroup', {
        name: newGroupName.value.trim()
      })
      ElMessage.success('分组已创建')
    }
    showGroupDialog.value = false
    newGroupName.value = ''
    editingGroup.value = null
  } catch (e) {
    console.error('保存分组失败', e)
  }
}

// 删除分组
const handleDeleteGroup = async (group) => {
  try {
    await ElMessageBox.confirm(
      `确定删除分组"${group.name}"？会话将移至未分组。`,
      '删除分组',
      { type: 'warning' }
    )
    await store.dispatch('im/session/removeSessionGroup', group.id)
    ElMessage.success('分组已删除')
  } catch (e) {
    if (e !== 'cancel') {
      console.error('删除分组失败', e)
    }
  }
}

// 将会话添加到分组
const moveSessionToGroup = async (session, groupId) => {
  try {
    if (session.groupId === groupId) {
      // 已在该分组，转为未分组
      await store.dispatch('im/session/removeSessionFromGroup', {
        sessionId: session.id,
        groupId: session.groupId
      })
    } else {
      await store.dispatch('im/session/addSessionToGroup', {
        sessionId: session.id,
        groupId
      })
    }
    contextMenuVisible.value = false
  } catch (e) {
    console.error('移动会话失败', e)
  }
}

// 右键菜单
const handleContextMenu = (event, session) => {
  event.preventDefault()
  contextMenuSession.value = session
  contextMenuVisible.value = true
  contextMenuStyle.value = {
    left: event.clientX + 'px',
    top: event.clientY + 'px'
  }
}

// 关闭右键菜单
const closeContextMenu = () => {
  contextMenuVisible.value = false
  contextMenuSession.value = null
}

// 点击空白处关闭菜单
const handleGlobalClick = () => {
  if (contextMenuVisible.value) {
    closeContextMenu()
  }
}
</script>

<style scoped>
.session-panel {
  width: var(--dt-session-panel-width);
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: var(--dt-bg-session-list);
  border-right: 1px solid var(--dt-border-light);
  flex-shrink: 0;
}

.panel-header {
  padding: var(--dt-spacing-lg) var(--dt-spacing-lg) var(--dt-spacing-sm);
  background-color: var(--dt-bg-session-list);
}

.session-tabs {
  display: flex;
  gap: 4px;
  margin-top: 10px;
  background: var(--dt-bg-hover);
  border-radius: var(--dt-radius-lg);
  padding: 4px;
}

.session-tab {
  flex: 1;
  height: 28px;
  border: none;
  background: transparent;
  border-radius: var(--dt-radius-md);
  font-size: var(--dt-font-size-sm);  /* 13px对齐到标准12px */
  font-weight: 500;
  color: var(--dt-text-secondary);
  cursor: pointer;
  transition: color var(--dt-transition-fast), background-color var(--dt-transition-fast);
}

.session-tab:hover {
  color: var(--dt-text-primary);
  background: var(--dt-bg-card);
}

.session-tab.active {
  background-color: var(--dt-bg-card);
  color: var(--dt-brand-color);
  font-weight: 600;
  box-shadow: var(--dt-shadow-1);
}

.session-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
  padding: 0 4px;
}

.search-bar {
  display: flex;
  align-items: center;
  height: 34px;
  background-color: var(--dt-bg-hover);
  border-radius: var(--dt-radius-lg);
  padding: 0 12px;
  border: 1.5px solid transparent;
  transition: background-color var(--dt-transition-fast);
}

.search-bar:hover {
  background-color: var(--dt-bg-card);
}

.search-bar.is-focused {
  background-color: var(--dt-bg-card);
  border-color: var(--dt-brand-color);
  box-shadow: 0 0 0 3px var(--dt-brand-bg);
}

.search-icon {
  color: var(--dt-text-tertiary);
  font-size: var(--dt-font-size-lg);
}

.search-bar input {
  border: none;
  background: transparent;
  outline: none;
  margin-left: 10px;
  width: 100%;
  font-size: var(--dt-font-size-base);
  color: var(--dt-text-primary);
}

.search-bar input::placeholder {
  color: var(--dt-text-tertiary);
}

.session-list {
  flex: 1;
  overflow-y: auto;
}

/* 滚动条美化 */
.session-list::-webkit-scrollbar { width: 4px; }
.session-list::-webkit-scrollbar-thumb { background: var(--dt-scrollbar-thumb-bg); border-radius: var(--dt-radius-sm); }
.session-list::-webkit-scrollbar-thumb:hover { background: var(--dt-scrollbar-thumb-bg-hover); }
.session-list::-webkit-scrollbar-track { background: transparent; }

/* 分类分组样式 */
.session-group {
  margin-bottom: 4px;
}

.group-header {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  cursor: pointer;
  user-select: none;
  transition: background-color var(--dt-transition-fast);
}

.group-header:hover {
  background-color: var(--dt-bg-session-hover);
}

.group-arrow {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  margin-right: 8px;
  transition: transform var(--dt-transition-fast);
}

.session-group.is-collapsed .group-arrow {
  transform: rotate(0deg);
}

.group-name {
  font-size: var(--dt-font-size-sm);
  font-weight: 500;
  color: var(--dt-text-secondary);
  flex: 1;
}

.group-unread-badge {
  background-color: var(--dt-error-color);
  color: var(--dt-text-white);
  font-size: 10px;
  font-weight: 600;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: var(--dt-radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
}

.group-sessions {
  /* 展开状态 */
}

.session-item {
  display: flex;
  align-items: center;
  height: var(--dt-session-item-height);  /* 钉钉标准 56px */
  padding: 0 var(--dt-spacing-lg);
  cursor: pointer;
  position: relative;
  transition: background-color var(--dt-transition-fast);
}

.session-item:hover {
  background-color: var(--dt-bg-session-hover);
}

.session-item.is-active {
  background-color: var(--dt-bg-session-active);
}

/* 钉钉 3px 激活蓝条 */
.session-item.is-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 12px;
  bottom: 12px;
  width: 3px;
  background-color: var(--dt-brand-color);
  border-radius: 0 2px 2px 0;
}

/* 置顶背景色 */
.session-item.is-pinned:not(.is-active) {
  background-color: var(--dt-bg-body);
}

.avatar-wrapper {
  position: relative;
  width: var(--dt-avatar-session);  /* 钉钉标准 40px */
  height: var(--dt-avatar-session);  /* 钉钉标准 40px */
  margin-right: var(--dt-spacing-md);
  flex-shrink: 0;
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: var(--dt-radius-sm); /* 钉钉规范：4px 圆角 */
  object-fit: cover;
  border: 1px solid var(--dt-border-lighter);
}

.unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background-color: var(--dt-unread-color);
  color: var(--dt-text-white);
  font-size: var(--dt-font-size-xs);
  font-weight: var(--dt-font-weight-semibold);
  line-height: 16px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  text-align: center;
}

/* 钉钉规范：1-9 数字圆形，10+ 数字胶囊 */
.unread-badge-circle {
  border-radius: var(--dt-radius-full); /* 50% 当 width=height=16px 时为正圆 */
}

.unread-badge-capsule {
  border-radius: var(--dt-radius-lg);
}

.content-wrapper {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 4px 0;
}

.content-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.session-name {
  font-size: var(--dt-font-size-md);
  font-weight: 500;
  color: var(--dt-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-time {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-tertiary);
  margin-left: 8px;
  flex-shrink: 0;
}

.content-bottom {
  display: flex;
  align-items: center;
  gap: 6px;
}

.last-message {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  line-height: 1.4;
}

.draft-tag {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-error-color);
  flex-shrink: 0;
}

.mute-icon {
  color: var(--dt-text-quaternary);
}

.empty-list {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: var(--dt-text-tertiary);
  font-size: var(--dt-font-size-sm);
  gap: 12px;
}

.empty-list .el-icon {
  opacity: 0.4;
}

.empty-list p {
  color: var(--dt-text-secondary);
  font-size: var(--dt-font-size-base);
  margin-top: 4px;
}

/* 右键上下文菜单 */
.session-context-menu {
  position: fixed;
  z-index: 9999;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-lg);
  box-shadow: var(--dt-shadow-2);
  min-width: 160px;
  padding: 4px 0;
}

.context-menu-header {
  padding: 8px 12px;
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
  font-weight: 500;
  border-bottom: 1px solid var(--dt-border-light);
  margin-bottom: 4px;
}

.context-menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-primary);
  cursor: pointer;
  transition: background-color var(--dt-transition-fast);
}

.context-menu-item:hover {
  background-color: var(--dt-bg-hover);
}

.context-menu-item.is-active {
  color: var(--dt-brand-color);
}

.context-menu-divider {
  height: 1px;
  background: var(--dt-border-light);
  margin: 4px 0;
}

/* 分组管理对话框 */
.group-dialog-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.group-create-row {
  display: flex;
  gap: 8px;
}

.group-create-row .el-input {
  flex: 1;
}

.group-list {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-lg);
  padding: 8px;
}

.group-list-item {
  display: flex;
  align-items: center;
  padding: 8px;
  border-radius: var(--dt-radius-md);
  transition: background-color var(--dt-transition-fast);
}

.group-list-item:hover {
  background-color: var(--dt-bg-hover);
}

.group-item-name {
  flex: 1;
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-primary);
  font-weight: 500;
}

.group-item-count {
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-tertiary);
  margin: 0 8px;
}

.group-item-actions {
  display: flex;
  gap: 2px;
  opacity: 0;
  transition: opacity var(--dt-transition-fast);
}

.group-list-item:hover .group-item-actions {
  opacity: 1;
}
</style>
