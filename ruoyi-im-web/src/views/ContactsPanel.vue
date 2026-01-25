<template>
  <div class="contacts-panel">
    <!-- 左侧边栏 -->
    <aside class="contacts-sidebar">
      <!-- 头部 -->
      <div class="sidebar-header">
        <h1 class="sidebar-title">
          <span class="material-icons-outlined title-icon">contacts</span>
          通讯录
        </h1>
        <el-tooltip content="添加联系人" placement="right">
          <button class="add-btn" @click="showAddDialog = true">
            <span class="material-icons-outlined">person_add</span>
          </button>
        </el-tooltip>
      </div>

      <!-- 搜索框 -->
      <div class="sidebar-search">
        <div class="search-input-wrapper">
          <span class="material-icons-outlined search-icon">search</span>
          <input
            v-model="searchQuery"
            class="search-input"
            placeholder="搜索联系人、群组..."
            type="text"
          />
          <span v-if="searchQuery" class="clear-btn" @click="searchQuery = ''">
            <span class="material-icons-outlined">close</span>
          </span>
        </div>
      </div>

      <!-- 快捷导航 -->
      <div class="sidebar-content">
        <div class="quick-nav">
          <div
            class="nav-item"
            :class="{ active: view === 'new-friends' }"
            @click="handleNav('new-friends')"
          >
            <div class="nav-indicator"></div>
            <div class="nav-icon new-friends-icon">
              <span class="material-icons-outlined">person_add</span>
            </div>
            <span class="nav-label">新的朋友</span>
            <div v-if="requestCount > 0" class="nav-badge">
              {{ requestCount > 99 ? '99+' : requestCount }}
            </div>
          </div>
          <div
            class="nav-item"
            :class="{ active: view === 'groups' }"
            @click="handleNav('groups')"
          >
            <div class="nav-indicator"></div>
            <div class="nav-icon groups-icon">
              <span class="material-icons-outlined">groups</span>
            </div>
            <span class="nav-label">我的群组</span>
          </div>
        </div>

        <!-- 组织架构 -->
        <div class="org-section">
          <div class="org-section-header">
            <span class="org-title">组织架构</span>
            <span class="material-icons-outlined org-icon">corporate_fare</span>
          </div>
          <div class="org-tree">
            <OrganizationTree
              v-model:selected-dept="selectedDept"
              @select="handleDeptSelect"
            />
          </div>
        </div>
      </div>
    </aside>

    <!-- 右侧主内容 -->
    <main class="contacts-main">
      <!-- 部门成员视图 -->
      <template v-if="view === 'department' && selectedDept">
        <header class="main-header">
          <div class="header-left">
            <el-breadcrumb separator="/" class="header-breadcrumb">
              <el-breadcrumb-item>
                <span class="material-icons-outlined breadcrumb-icon">home</span>
                通讯录
              </el-breadcrumb-item>
              <el-breadcrumb-item>{{ selectedDept.name }}</el-breadcrumb-item>
            </el-breadcrumb>
            <div class="member-count">
              <span class="material-icons-outlined count-icon">people</span>
              {{ selectedDept.memberCount || 0 }} 人
            </div>
          </div>
          <div class="header-right">
            <div class="avatar-preview">
              <div
                v-for="member in previewMembers"
                :key="member.id"
                class="preview-avatar"
                :style="{ backgroundColor: member.color }"
                :title="member.name"
              >
                {{ member.name?.charAt(0) }}
              </div>
              <div v-if="deptMembers.length > 3" class="preview-avatar more-count" title="更多成员">
                +{{ deptMembers.length - 3 }}
              </div>
            </div>
            <div class="divider"></div>
            <el-tooltip content="部门设置" placement="bottom">
              <button class="icon-btn">
                <span class="material-icons-outlined">settings</span>
              </button>
            </el-tooltip>
            <el-tooltip content="刷新" placement="bottom">
              <button class="icon-btn" @click="loadDeptMembers(selectedDept.id)">
                <span class="material-icons-outlined">refresh</span>
              </button>
            </el-tooltip>
          </div>
        </header>

        <div class="main-content">
          <div v-if="deptMembers.length === 0" class="empty-state">
            <span class="material-icons-outlined empty-icon">person_off</span>
            <h3 class="empty-title">暂无成员</h3>
            <p class="empty-text">该部门还没有成员</p>
          </div>
          <div v-else class="members-grid">
            <div
              v-for="member in deptMembers"
              :key="member.id"
              class="member-card"
              @click="handleMemberClick(member)"
            >
              <div class="member-card-header">
                <DingtalkAvatar
                  :name="member.name"
                  :user-id="member.id"
                  :size="48"
                  shape="square"
                  custom-class="member-avatar"
                />
                <div class="online-indicator" :class="{ online: member.online }"></div>
              </div>
              <div class="member-card-body">
                <h4 class="member-name">{{ member.name }}</h4>
                <p class="member-position">{{ member.position || '员工' }}</p>
                <div class="member-actions">
                  <el-tooltip content="发消息" placement="top">
                    <button class="action-btn" @click.stop="handleChat(member)">
                      <span class="material-icons-outlined">chat_bubble</span>
                    </button>
                  </el-tooltip>
                  <el-tooltip content="查看详情" placement="top">
                    <button class="action-btn">
                      <span class="material-icons-outlined">visibility</span>
                    </button>
                  </el-tooltip>
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
          <div class="empty-illustration">
            <span class="material-icons-outlined empty-icon">corporate_fare</span>
            <div class="empty-decoration"></div>
          </div>
          <h3 class="empty-title">欢迎使用通讯录</h3>
          <p class="empty-text">请在左侧选择部门查看成员</p>
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getFriendRequests } from '@/api/im/contact'
import { getDepartmentMembers } from '@/api/im/organization'
import { createConversation } from '@/api/im/conversation'
import AddContactDialog from '@/components/Contacts/AddContactDialog.vue'
import OrganizationTree from '@/components/Contacts/OrganizationTree.vue'
import NewFriendsView from '@/components/Contacts/NewFriendsView.vue'
import GroupsView from '@/components/Contacts/GroupsView.vue'
import UserProfileDialog from '@/components/Contacts/UserProfileDialog.vue'
import GroupProfileDialog from '@/components/Contacts/GroupProfileDialog.vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const router = useRouter()
const view = ref('department') // department | new-friends | groups
const selectedDept = ref(null)
const searchQuery = ref('')
const showAddDialog = ref(false)
const requestCount = ref(0)

// 详情弹窗控制
const showUserDialog = ref(false)
const selectedUserId = ref(null)
const showGroupDialog = ref(false)
const selectedGroupId = ref(null)

const deptMembers = ref([])

const previewMembers = computed(() => {
  return deptMembers.value.slice(0, 3).map(m => ({
    id: m.id,
    name: m.name,
    color: getAvatarColor(m.name)
  }))
})

const handleNav = (target) => {
  view.value = target
  selectedDept.value = null
}

const handleDeptSelect = async (dept) => {
  selectedDept.value = dept
  view.value = 'department'
  await loadDeptMembers(dept.id)
}

const handleMemberClick = (member) => {
  selectedUserId.value = member.id || member.userId
  showUserDialog.value = true
}

const handleChat = async (member) => {
  try {
    const res = await createConversation({
      type: 'PRIVATE',
      targetId: member.id || member.userId
    })
    if (res.code === 200) {
      // 切换到聊天面板
      window.dispatchEvent(new CustomEvent('switch-to-chat', {
        detail: { conversation: res.data }
      }))
      ElMessage.success('已发起聊天')
    }
  } catch (e) {
    console.error('发起聊天失败', e)
    ElMessage.error('无法发起聊天')
  }
}

const handleGroupSelect = (group) => {
  selectedGroupId.value = group.id
  showGroupDialog.value = true
}

const handleBack = () => {
  view.value = 'department'
}

const loadDeptMembers = async (deptId) => {
  try {
    const res = await getDepartmentMembers(deptId)
    if (res.code === 200) {
      deptMembers.value = (res.data || []).map(m => ({
        ...m,
        color: getAvatarColor(m.name),
        online: Math.random() > 0.4 // 模拟在线状态
      }))
    }
  } catch (e) {
    console.error(e)
  }
}

const getAvatarColor = (name) => {
  const colors = [
    '#1677ff', '#0ea5e9', '#ec4899', '#6366f1',
    '#f97316', '#14b8a6', '#8b5cf6', '#eab308'
  ]
  let hash = 0
  for (let i = 0; i < (name || '').length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
}

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

const groupsViewRef = ref(null)
const loadGroups = () => {
  groupsViewRef.value?.loadGroups?.()
}

onMounted(() => {
  checkRequests()
})
</script>

<style scoped lang="scss">
// ============================================================================
// 容器
// ============================================================================
.contacts-panel {
  display: flex;
  height: 100%;
  flex: 1;
  min-width: 0;
  background: var(--dt-bg-body);
}

// ============================================================================
// 左侧边栏
// ============================================================================
.contacts-sidebar {
  width: 280px;
  background: var(--dt-bg-card);
  border-right: 1px solid var(--dt-border-light);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 16px 16px 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sidebar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0;

  .title-icon {
    font-size: 20px;
    color: var(--dt-brand-color);
  }
}

.add-btn {
  width: 36px;
  height: 36px;
  color: var(--dt-text-secondary);
  background: var(--dt-bg-body);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--dt-transition-base);

  &:hover {
    color: var(--dt-brand-color);
    border-color: var(--dt-brand-color);
    background: var(--dt-brand-bg);
    transform: scale(1.05);
  }

  .material-icons-outlined {
    font-size: 18px;
  }
}

// ============================================================================
// 搜索框
// ============================================================================
.sidebar-search {
  padding: 0 12px 12px;
}

.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 10px;
  color: var(--dt-text-quaternary);
  font-size: 18px;
  pointer-events: none;
}

.search-input {
  width: 100%;
  height: 38px;
  background: var(--dt-bg-body);
  border: 1px solid var(--dt-border-light);
  border-radius: var(--dt-radius-md);
  padding: 0 36px;
  font-size: 13px;
  color: var(--dt-text-primary);
  outline: none;
  transition: all var(--dt-transition-base);

  &::placeholder {
    color: var(--dt-text-quaternary);
  }

  &:focus {
    border-color: var(--dt-brand-color);
    background: var(--dt-bg-card);
    box-shadow: 0 0 0 2px var(--dt-brand-lighter);
  }
}

.clear-btn {
  position: absolute;
  right: 8px;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 50%;
  color: var(--dt-text-quaternary);
  transition: all var(--dt-transition-base);

  &:hover {
    background: var(--dt-border-color);
    color: var(--dt-text-secondary);
  }

  .material-icons-outlined {
    font-size: 14px;
  }
}

// ============================================================================
// 侧边栏内容
// ============================================================================
.sidebar-content {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px;
}

// 快捷导航
.quick-nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-bottom: 12px;
}

.nav-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all var(--dt-transition-base);
  color: var(--dt-text-secondary);

  &:hover {
    background: var(--dt-bg-body);
    color: var(--dt-text-primary);

    .nav-icon {
      transform: scale(1.05);
    }
  }

  &.active {
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);
    font-weight: 500;

    .nav-indicator {
      opacity: 1;
    }
  }
}

.nav-indicator {
  position: absolute;
  left: 8px;
  width: 3px;
  height: 16px;
  background: var(--dt-brand-color);
  border-radius: 2px;
  opacity: 0;
  transition: opacity var(--dt-transition-base);
}

.nav-icon {
  width: 32px;
  height: 32px;
  border-radius: var(--dt-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: transform var(--dt-transition-base);

  .material-icons-outlined {
    font-size: 18px;
  }

  &.new-friends-icon {
    background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
    color: #fff;
  }

  &.groups-icon {
    background: linear-gradient(135deg, #1677ff 0%, #0e5fd9 100%);
    color: #fff;
  }
}

.nav-label {
  flex: 1;
  font-size: 14px;
}

.nav-badge {
  padding: 2px 7px;
  background: var(--dt-error-color);
  color: #fff;
  border-radius: var(--dt-radius-full);
  font-size: 11px;
  font-weight: 600;
  min-width: 18px;
  text-align: center;
}

// 组织架构
.org-section {
  margin-top: 8px;
}

.org-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
}

.org-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--dt-text-quaternary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.org-icon {
  font-size: 16px;
  color: var(--dt-text-quaternary);
}

.org-tree {
  padding: 0 4px;
}

// ============================================================================
// 右侧主内容
// ============================================================================
.contacts-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.main-header {
  height: 64px;
  padding: 0 24px;
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-breadcrumb {
  :deep(.el-breadcrumb__item) {
    .el-breadcrumb__inner {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 14px;
      color: var(--dt-text-secondary);
      font-weight: 500;
    }

    &:last-child .el-breadcrumb__inner {
      color: var(--dt-text-primary);
    }
  }
}

.breadcrumb-icon {
  font-size: 16px;
}

.member-count {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);
  border-radius: var(--dt-radius-full);
  font-size: 12px;
  font-weight: 600;

  .count-icon {
    font-size: 14px;
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-preview {
  display: flex;
  align-items: center;
}

.preview-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 2px solid var(--dt-bg-card);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
  margin-left: -8px;
  box-shadow: var(--dt-shadow-2);
  transition: transform var(--dt-transition-base);

  &:hover {
    transform: translateY(-2px);
    z-index: 10;
  }

  &:first-child {
    margin-left: 0;
  }

  &.more-count {
    background: var(--dt-bg-body);
    color: var(--dt-text-secondary);
    font-size: 10px;
  }
}

.divider {
  width: 1px;
  height: 20px;
  background: var(--dt-border-color);
}

.icon-btn {
  width: 36px;
  height: 36px;
  background: transparent;
  border: none;
  border-radius: var(--dt-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--dt-text-secondary);
  transition: all var(--dt-transition-base);

  &:hover {
    background: var(--dt-bg-body);
    color: var(--dt-brand-color);
  }

  .material-icons-outlined {
    font-size: 18px;
  }
}

.main-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

// ============================================================================
// 成员网格
// ============================================================================
.members-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.member-card {
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg);
  padding: 16px;
  box-shadow: var(--dt-shadow-card);
  transition: all var(--dt-transition-base);
  cursor: pointer;
  border: 1px solid transparent;

  &:hover {
    box-shadow: var(--dt-shadow-card-hover);
    transform: translateY(-2px);
    border-color: var(--dt-brand-color);

    .member-actions {
      opacity: 1;
    }
  }
}

.member-card-header {
  position: relative;
  display: flex;
  justify-content: center;
  margin-bottom: 12px;

  :deep(.member-avatar) {
    border-radius: var(--dt-radius-md) !important;
  }
}

.online-indicator {
  position: absolute;
  bottom: 2px;
  right: calc(50% - 20px);
  width: 12px;
  height: 12px;
  background: var(--dt-text-quaternary);
  border: 2px solid var(--dt-bg-card);
  border-radius: 50%;
  transition: all var(--dt-transition-base);

  &.online {
    background: var(--dt-success-color);
    box-shadow: 0 0 0 2px rgba(82, 196, 26, 0.2);
  }
}

.member-card-body {
  text-align: center;
}

.member-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0 0 4px 0;
}

.member-position {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  margin: 0 0 12px 0;
}

.member-actions {
  display: flex;
  justify-content: center;
  gap: 8px;
  opacity: 0;
  transition: opacity var(--dt-transition-base);
}

.action-btn {
  width: 32px;
  height: 32px;
  border-radius: var(--dt-radius-md);
  background: var(--dt-bg-body);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--dt-text-secondary);
  transition: all var(--dt-transition-base);

  &:hover {
    background: var(--dt-brand-color);
    color: #fff;
  }

  .material-icons-outlined {
    font-size: 16px;
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
  text-align: center;
}

.empty-state-large {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
}

.empty-illustration {
  position: relative;
  margin-bottom: 24px;
}

.empty-icon {
  font-size: 80px;
  color: var(--dt-border-color);
}

.empty-decoration {
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 6px;
  background: var(--dt-border-color);
  border-radius: var(--dt-radius-full);
  opacity: 0.5;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0 0 8px 0;
}

.empty-text {
  font-size: 14px;
  color: var(--dt-text-tertiary);
  margin: 0;
}

// ============================================================================
// 暗色模式
// ============================================================================
.dark .contacts-panel {
  background: var(--dt-bg-body-dark);
}

.dark .contacts-sidebar,
.dark .main-header {
  background: var(--dt-bg-card-dark);
  border-color: var(--dt-border-dark);
}

.dark .sidebar-title,
.dark .member-name {
  color: var(--dt-text-primary-dark);
}

.dark .nav-item {
  color: var(--dt-text-secondary-dark);

  &:hover {
    background: var(--dt-bg-hover-dark);
  }

  &.active {
    background: rgba(22, 119, 255, 0.15);
  }
}

.dark .search-input {
  background: var(--dt-bg-input-dark);
  border-color: var(--dt-border-dark);
  color: var(--dt-text-primary-dark);
}

.dark .member-card {
  background: var(--dt-bg-card-dark);
}

.dark .icon-btn:hover {
  background: var(--dt-bg-hover-dark);
}

.dark .empty-icon {
  color: var(--dt-border-dark);
}

.dark .empty-decoration {
  background: var(--dt-border-dark);
}

.dark .online-indicator {
  border-color: var(--dt-bg-card-dark);
}

.dark .online-indicator:not(.online) {
  background: var(--dt-border-dark);
}

// ============================================================================
// 响应式
// ============================================================================
@media (max-width: 768px) {
  .contacts-sidebar {
    width: 240px;
  }

  .members-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }

  .header-breadcrumb {
    display: none;
  }
}
</style>
