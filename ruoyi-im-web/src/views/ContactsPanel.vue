<template>
  <div class="contacts-panel">
    <!-- 左侧导航 - 钉钉风格简洁设计 -->
    <aside class="contacts-sidebar">
      <div class="sidebar-title">通讯录</div>
      <nav class="sidebar-nav">
        <div
          v-for="item in navItems"
          :key="item.key"
          class="nav-item"
          :class="{ active: view === item.key }"
          @click="handleNav(item.key)"
        >
          <span class="material-icons-outlined nav-icon">{{ item.icon }}</span>
          <span class="nav-label">{{ item.label }}</span>
          <span v-if="item.badge" class="nav-badge">{{ item.badge > 99 ? '99+' : item.badge }}</span>
        </div>
      </nav>
      <div class="sidebar-org">
        <div class="org-header">组织架构</div>
        <div class="org-tree">
          <OrganizationTree
            v-model:selected-dept="selectedDept"
            @select="handleDeptSelect"
          />
        </div>
      </div>
    </aside>

    <!-- 右侧主内容 -->
    <main class="contacts-main">
      <!-- 部门成员视图 -->
      <template v-if="view === 'department' && selectedDept">
        <header class="main-header">
          <div class="header-title">
            <span class="material-icons-outlined">apartment</span>
            {{ selectedDept.name }}
          </div>
          <div class="header-info">{{ deptMembers.length }} 人</div>
        </header>
        <div class="main-content">
          <div v-if="deptMembers.length === 0" class="empty-state">
            <span class="material-icons-outlined">person_off</span>
            <p>暂无成员</p>
          </div>
          <div v-else class="member-list">
            <div
              v-for="member in deptMembers"
              :key="member.id"
              class="member-item"
              @click="handleMemberClick(member)"
            >
              <DingtalkAvatar
                :name="member.name"
                :user-id="member.id"
                :size="48"
              />
              <div class="member-info">
                <div class="member-name">{{ member.name }}</div>
                <div class="member-position">{{ member.position || '员工' }}</div>
              </div>
              <div class="member-status" :class="{ online: member.online }"></div>
            </div>
          </div>
        </div>
      </template>

      <!-- 我的好友视图 -->
      <template v-else-if="view === 'friends'">
        <header class="main-header">
          <div class="header-title">
            <span class="material-icons-outlined">people</span>
            我的好友
          </div>
          <div class="header-info">{{ friendCount }} 人</div>
        </header>
        <div class="main-content">
          <div v-if="loadingFriends" v-loading="true" class="loading-state"></div>
          <div v-else-if="groupedFriends.length === 0" class="empty-state">
            <span class="material-icons-outlined">person_off</span>
            <p>暂无好友</p>
          </div>
          <div v-else class="friends-list">
            <div v-for="group in groupedFriends" :key="group.groupName || 'default'" class="friend-group">
              <div class="friend-group-header" @click="toggleGroupCollapse(group.groupName)">
                <span class="collapse-icon" :class="{ collapsed: isGroupCollapsed(group.groupName) }">
                  <span class="material-icons-outlined">expand_more</span>
                </span>
                <span class="group-name">{{ group.groupName || '未分组' }}</span>
                <span class="group-count">{{ group.contacts.length }}</span>
              </div>
              <div v-show="!isGroupCollapsed(group.groupName)" class="friend-group-members">
                <div
                  v-for="friend in group.contacts"
                  :key="friend.id"
                  class="friend-item"
                  @click="handleFriendClick(friend)"
                >
                  <DingtalkAvatar
                    :name="friend.remark || friend.friendName"
                    :user-id="friend.friendId"
                    :size="44"
                    :src="friend.friendAvatar"
                  />
                  <div class="friend-info">
                    <div class="friend-name">{{ friend.remark || friend.friendName }}</div>
                    <div class="friend-desc">{{ friend.position || friend.department || '联系人' }}</div>
                  </div>
                  <div class="friend-status" :class="{ online: friend.online }"></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- 新朋友视图 -->
      <template v-else-if="view === 'new-friends'">
        <NewFriendsView @back="handleBack" />
      </template>

      <!-- 我的群组视图 -->
      <template v-else-if="view === 'groups'">
        <GroupsView ref="groupsViewRef" @select-group="handleGroupSelect" />
      </template>

      <!-- 默认空状态 -->
      <template v-else>
        <div class="empty-state-large">
          <span class="material-icons-outlined">contacts</span>
          <p>选择左侧导航查看内容</p>
        </div>
      </template>
    </main>

    <!-- Dialogs -->
    <AddContactDialog v-model:visible="showAddDialog" />

    <!-- 好友详情弹窗 -->
    <UserProfileDialog
      v-model="showUserDialog"
      :user-id="selectedUserId"
    />

    <!-- 群组详情弹窗 -->
    <GroupProfileDialog
      v-model="showGroupDialog"
      :group-id="selectedGroupId"
      @refresh="loadGroups"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { getFriendRequests, getGroupedFriendList } from '@/api/im/contact'
import { getDepartmentMembers } from '@/api/im/organization'
import { createConversation } from '@/api/im/conversation'
import AddContactDialog from '@/components/Contacts/AddContactDialog.vue'
import OrganizationTree from '@/components/Contacts/OrganizationTree.vue'
import NewFriendsView from '@/components/Contacts/NewFriendsView.vue'
import GroupsView from '@/components/Contacts/GroupsView.vue'
import UserProfileDialog from '@/components/Contacts/UserProfileDialog.vue'
import GroupProfileDialog from '@/components/Contacts/GroupProfileDialog.vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const store = useStore()

// 视图状态
const view = ref('friends')
const selectedDept = ref(null)
const showAddDialog = ref(false)

// 数据状态
const requestCount = ref(0)
const friendCount = ref(0)
const loadingFriends = ref(false)
const groupedFriends = ref([])
const deptMembers = ref([])

// 折叠状态
const collapsedGroups = ref(new Set())

// 弹窗状态
const showUserDialog = ref(false)
const selectedUserId = ref(null)
const showGroupDialog = ref(false)
const selectedGroupId = ref(null)

// 导航配置
const navItems = computed(() => [
  { key: 'friends', label: '我的好友', icon: 'people', badge: null },
  { key: 'new-friends', label: '新的朋友', icon: 'person_add', badge: requestCount.value },
  { key: 'groups', label: '我的群组', icon: 'groups', badge: null }
])

// 分组折叠
const toggleGroupCollapse = (groupName) => {
  const key = groupName || 'default'
  if (collapsedGroups.value.has(key)) {
    collapsedGroups.value.delete(key)
  } else {
    collapsedGroups.value.add(key)
  }
}

const isGroupCollapsed = (groupName) => {
  return collapsedGroups.value.has(groupName || 'default')
}

// 导航切换
const handleNav = (target) => {
  view.value = target
  selectedDept.value = null
}

// 部门选择
const handleDeptSelect = async (dept) => {
  selectedDept.value = dept
  view.value = 'department'
  await loadDeptMembers(dept.id)
}

// 成员点击
const handleMemberClick = (member) => {
  selectedUserId.value = member.id || member.userId
  showUserDialog.value = true
}

// 好友点击
const handleFriendClick = (friend) => {
  selectedUserId.value = friend.friendId || friend.id
  showUserDialog.value = true
}

// 发起聊天
const handleChat = async (member) => {
  try {
    const res = await createConversation({
      type: 'PRIVATE',
      targetId: member.id || member.userId
    })
    if (res.code === 200) {
      await store.dispatch('im/session/selectSessionById', res.data)
      window.dispatchEvent(new CustomEvent('switch-to-chat', {
        detail: { conversationId: res.data }
      }))
      ElMessage.success('已发起聊天')
    }
  } catch (e) {
    console.error('发起聊天失败', e)
    ElMessage.error('无法发起聊天')
  }
}

// 群组选择
const handleGroupSelect = (group) => {
  selectedGroupId.value = group.id
  showGroupDialog.value = true
}

const handleBack = () => {
  view.value = 'department'
}

// 加载部门成员
const loadDeptMembers = async (deptId) => {
  try {
    const res = await getDepartmentMembers(deptId)
    if (res.code === 200) {
      deptMembers.value = (res.data || []).map(m => ({
        ...m,
        online: Math.random() > 0.4
      }))
    }
  } catch (e) {
    console.error(e)
  }
}

// 检查好友请求
const checkRequests = async () => {
  try {
    const res = await getFriendRequests()
    if (res.code === 200) {
      requestCount.value = res.data.filter(
        r => r.status === 'PENDING' && r.direction === 'RECEIVED'
      ).length
    }
  } catch (e) {
    console.error(e)
  }
}

// 加载好友列表
const loadFriends = async () => {
  loadingFriends.value = true
  try {
    const res = await getGroupedFriendList()
    if (res.code === 200 && res.data) {
      groupedFriends.value = res.data.map(group => ({
        groupName: group.groupName,
        contacts: (group.friends || []).map(c => ({
          ...c,
          online: c.online || false
        }))
      }))
      friendCount.value = groupedFriends.value.reduce(
        (sum, g) => sum + (g.contacts?.length || 0), 0
      )
    }
  } catch (e) {
    console.error('加载好友失败', e)
    ElMessage.error('加载好友失败')
  } finally {
    loadingFriends.value = false
  }
}

const groupsViewRef = ref(null)
const loadGroups = () => {
  groupsViewRef.value?.loadGroups?.()
}

// 键盘快捷键
const handleKeydown = (e) => {
  if (e.key === 'Escape') {
    if (showAddDialog.value) showAddDialog.value = false
    else if (showUserDialog.value) showUserDialog.value = false
    else if (showGroupDialog.value) showGroupDialog.value = false
  }
}

onMounted(() => {
  checkRequests()
  loadFriends()
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// ============================================================================
// 容器
// ============================================================================
.contacts-panel {
  display: flex;
  height: 100%;
  background: var(--dt-bg-body);
}

// ============================================================================
// 左侧导航 - 钉钉风格
// ============================================================================
.contacts-sidebar {
  width: 200px;
  background: #f5f5f5;
  border-right: 1px solid var(--dt-border-light);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;

  .dark & {
    background: #1e1e1e;
    border-right-color: #333;
  }
}

.sidebar-title {
  padding: 20px 16px 12px;
  font-size: 16px;
  font-weight: 600;
  color: var(--dt-text-primary);
}

.sidebar-nav {
  padding: 0 8px;
  margin-bottom: 16px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 40px;
  padding: 0 12px;
  margin: 2px 0;
  border-radius: 6px;
  cursor: pointer;
  color: var(--dt-text-secondary);
  font-size: 14px;
  transition: all 0.2s;

  .nav-icon {
    font-size: 18px;
    color: var(--dt-text-tertiary);
  }

  .nav-label {
    flex: 1;
  }

  .nav-badge {
    padding: 2px 6px;
    min-width: 18px;
    height: 18px;
    background: #ff4d4f;
    color: #fff;
    border-radius: 9px;
    font-size: 11px;
    font-weight: 500;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &:hover {
    background: rgba(0, 0, 0, 0.04);

    .dark & {
      background: rgba(255, 255, 255, 0.06);
    }
  }

  &.active {
    background: #e6f4ff;
    color: var(--dt-brand-color);
    font-weight: 500;

    .dark & {
      background: rgba(22, 119, 255, 0.15);
    }

    .nav-icon {
      color: var(--dt-brand-color);
    }
  }
}

.sidebar-org {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.org-header {
  padding: 12px 16px 8px;
  font-size: 12px;
  font-weight: 600;
  color: var(--dt-text-quaternary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.org-tree {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px;
}

// ============================================================================
// 右侧内容区
// ============================================================================
.contacts-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #fff;

  .dark & {
    background: var(--dt-bg-body-dark);
  }
}

.main-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid var(--dt-border-light);
  background: #fff;

  .dark & {
    background: var(--dt-bg-card-dark);
    border-bottom-color: var(--dt-border-dark);
  }
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--dt-text-primary);

  .material-icons-outlined {
    font-size: 20px;
    color: var(--dt-text-tertiary);
  }
}

.header-info {
  font-size: 13px;
  color: var(--dt-text-tertiary);
}

.main-content {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

// ============================================================================
// 成员列表 - 简洁列表样式
// ============================================================================
.member-list {
  padding: 0 16px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: var(--dt-bg-hover);
  }

  .member-info {
    flex: 1;
    min-width: 0;
  }

  .member-name {
    font-size: 14px;
    font-weight: 500;
    color: var(--dt-text-primary);
    margin-bottom: 2px;
  }

  .member-position {
    font-size: 12px;
    color: var(--dt-text-tertiary);
  }

  .member-status {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #d9d9d9;
    flex-shrink: 0;

    &.online {
      background: #52c41a;
    }
  }
}

// ============================================================================
// 好友列表
// ============================================================================
.friends-list {
  padding: 0 16px;
}

.friend-group {
  margin-bottom: 8px;
}

.friend-group-header {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 4px;
  cursor: pointer;
  user-select: none;

  .collapse-icon {
    transition: transform 0.2s;

    &.collapsed {
      transform: rotate(-90deg);
    }

    .material-icons-outlined {
      font-size: 18px;
      color: var(--dt-text-tertiary);
    }
  }

  .group-name {
    font-size: 13px;
    font-weight: 500;
    color: var(--dt-text-secondary);
  }

  .group-count {
    margin-left: auto;
    font-size: 12px;
    color: var(--dt-text-quaternary);
  }

  &:hover .group-name {
    color: var(--dt-text-primary);
  }
}

.friend-group-members {
  display: flex;
  flex-direction: column;
}

.friend-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: var(--dt-bg-hover);
  }

  .friend-info {
    flex: 1;
    min-width: 0;
  }

  .friend-name {
    font-size: 14px;
    font-weight: 500;
    color: var(--dt-text-primary);
    margin-bottom: 2px;
  }

  .friend-desc {
    font-size: 12px;
    color: var(--dt-text-tertiary);
  }

  .friend-status {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #d9d9d9;
    flex-shrink: 0;

    &.online {
      background: #52c41a;
    }
  }
}

// ============================================================================
// 空状态
// ============================================================================
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--dt-text-tertiary);

  .material-icons-outlined {
    font-size: 48px;
    margin-bottom: 12px;
    opacity: 0.5;
  }

  p {
    font-size: 14px;
  }
}

.empty-state-large {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--dt-text-tertiary);

  .material-icons-outlined {
    font-size: 64px;
    margin-bottom: 16px;
    opacity: 0.4;
  }

  p {
    font-size: 14px;
  }
}

.loading-state {
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

// ============================================================================
// 组织架构树
// ============================================================================
:deep(.org-tree) {
  .org-tree-node {
    padding: 6px 8px;
    margin: 1px 0;
    border-radius: 4px;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    color: var(--dt-text-secondary);

    &:hover {
      background: rgba(0, 0, 0, 0.04);

      .dark & {
        background: rgba(255, 255, 255, 0.06);
      }
    }

    &.active {
      background: #e6f4ff;
      color: var(--dt-brand-color);

      .dark & {
        background: rgba(22, 119, 255, 0.15);
      }
    }

    .tree-node-children {
      margin-left: 12px;
      padding-left: 12px;
      border-left: 1px solid var(--dt-border-light);
    }
  }
}

// ============================================================================
// 滚动条美化
// ============================================================================
.org-tree,
.main-content {
  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: transparent;
    border-radius: 3px;
  }

  &:hover::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.1);

    .dark & {
      background: rgba(255, 255, 255, 0.1);
    }
  }
}
</style>
