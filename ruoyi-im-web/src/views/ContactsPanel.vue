<template>
  <div class="contacts-panel">
    <!-- 左侧边栏 -->
    <aside class="contacts-sidebar">
      <div class="sidebar-header">
        <h1 class="sidebar-title">通讯录</h1>
        <button
          @click="showAddDialog = true"
          class="add-btn"
          title="添加联系人/群组"
        >
          <span class="material-icons-outlined">add</span>
        </button>
      </div>

      <div class="sidebar-search">
        <div class="search-input-wrapper">
          <span class="material-icons-outlined search-icon">search</span>
          <input
            v-model="searchQuery"
            class="search-input"
            placeholder="搜索部门或成员"
            type="text"
          />
        </div>
      </div>

      <div class="sidebar-content">
        <!-- 快捷导航 -->
        <div class="quick-nav">
          <div
            class="nav-item"
            :class="{ active: view === 'new-friends' }"
            @click="handleNav('new-friends')"
          >
            <div class="nav-icon new-friends-icon">
              <span class="material-icons-outlined">person_add</span>
            </div>
            <span class="nav-label">新的朋友</span>
            <el-badge v-if="requestCount > 0" :value="requestCount" class="nav-badge" />
          </div>
          <div
            class="nav-item"
            :class="{ active: view === 'groups' }"
            @click="handleNav('groups')"
          >
            <div class="nav-icon groups-icon">
              <span class="material-icons-outlined">groups</span>
            </div>
            <span class="nav-label">我的群组</span>
          </div>
        </div>

        <!-- 组织架构 -->
        <div class="org-section">
          <div class="org-title">组织架构</div>
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
            <el-breadcrumb separator="/">
              <el-breadcrumb-item>通讯录</el-breadcrumb-item>
              <el-breadcrumb-item>{{ selectedDept.name }}</el-breadcrumb-item>
            </el-breadcrumb>
            <span class="member-count">{{ selectedDept.memberCount || 0 }} 人</span>
          </div>
          <div class="header-right">
            <div class="avatar-preview">
              <div
                v-for="member in previewMembers"
                :key="member.id"
                class="preview-avatar"
                :style="{ backgroundColor: member.color }"
              >
                {{ member.name?.charAt(0) }}
              </div>
              <div class="preview-avatar add-more" @click="showAddMember = true">
                <span class="material-icons-outlined">add</span>
              </div>
            </div>
            <div class="divider"></div>
            <button class="settings-btn">
              <span class="material-icons-outlined">settings</span>
              <span>设置</span>
            </button>
          </div>
        </header>

        <div class="main-content">
          <div class="members-table">
            <table class="table">
              <thead>
                <tr>
                  <th>成员信息</th>
                  <th>职务</th>
                  <th class="contact-col">联系方式</th>
                  <th class="status-col">状态</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="member in deptMembers"
                  :key="member.id"
                  class="member-row"
                  @click="handleMemberClick(member)"
                >
                  <td class="info-col">
                    <div class="member-info">
                      <DingtalkAvatar
                        :name="member.name"
                        :user-id="member.id"
                        :size="40"
                        shape="square"
                        custom-class="member-avatar-item"
                      />
                      <div>
                        <div class="member-name">{{ member.name }}</div>
                        <div class="member-position-mobile">{{ member.position }}</div>
                      </div>
                    </div>
                  </td>
                  <td>
                    <span class="position-tag">{{ member.position }}</span>
                  </td>
                  <td class="contact-col">
                    <span class="contact-info">{{ member.phone || member.mobile }}</span>
                  </td>
                  <td class="status-col">
                    <div class="status-indicator" :class="member.online ? 'online' : 'offline'">
                      <div class="status-dot"></div>
                      <span class="status-text">{{ member.online ? '在线' : '离线' }}</span>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
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
        <div class="empty-state">
          <span class="material-icons-outlined empty-icon">group</span>
          <p class="empty-text">请选择一个部门查看成员</p>
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
import { getFriendRequests } from '@/api/im/contact'
import { getDepartmentMembers } from '@/api/im/organization'
import AddContactDialog from '@/components/Contacts/AddContactDialog.vue'
import OrganizationTree from '@/components/Contacts/OrganizationTree.vue'
import NewFriendsView from '@/components/Contacts/NewFriendsView.vue'
import GroupsView from '@/components/Contacts/GroupsView.vue'
import UserProfileDialog from '@/components/Contacts/UserProfileDialog.vue'
import GroupProfileDialog from '@/components/Contacts/GroupProfileDialog.vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const view = ref('department') // department | new-friends | groups
const selectedDept = ref(null)
const searchQuery = ref('')
const showAddDialog = ref(false)
const showAddMember = ref(false)
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
    '#2563eb', '#0ea5e9', '#ec4899', '#6366f1',
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
  // 默认选中第一个部门
  // 可以在这里初始化 selectedDept
})
</script>

<style scoped>
.contacts-panel {
  display: flex;
  height: 100%;
  flex: 1;
  min-width: 0;
  background: #f4f7f9;
}

/* 左侧边栏 */
.contacts-sidebar {
  width: 288px;
  background: #fff;
  border-right: 1px solid #e6e6e6;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sidebar-title {
  font-size: 20px;
  font-weight: 700;
  color: #262626;
  margin: 0;
}

.add-btn {
  color: #8c8c8c;
  background: #f5f5f5;
  padding: 6px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  border: none;
  cursor: pointer;
}

.add-btn:hover {
  color: #1677ff;
  background: #e6f4ff;
}

.sidebar-search {
  padding: 0 16px 16px;
}

.search-input-wrapper {
  position: relative;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #bfbfbf;
  font-size: 16px;
}

.search-input {
  width: 100%;
  background: #f5f5f5;
  border: none;
  border-radius: 6px;
  padding: 10px 12px 10px 36px;
  font-size: 14px;
  color: #262626;
  outline: none;
  transition: all 0.2s;
}

.search-input:focus {
  background: #fff;
  box-shadow: 0 0 0 1px #1677ff inset;
}

.search-input::placeholder {
  color: #bfbfbf;
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
}

/* 快捷导航 */
.quick-nav {
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-bottom: 16px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  color: #595959;
  transition: all 0.2s;
  position: relative;
}

.nav-item:hover {
  background: #f5f5f5;
}

.nav-item.active {
  background: #e6f7ff;
  color: #1677ff;
}

.nav-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.new-friends-icon {
  background: #fff7e6;
  color: #fa8c16;
}

.groups-icon {
  background: #f6ffed;
  color: #52c41a;
}

.nav-label {
  font-size: 14px;
  font-weight: 500;
}

.nav-badge {
  margin-left: auto;
}

/* 组织架构 */
.org-section {
  padding: 0 8px;
}

.org-title {
  padding: 8px 12px 4px;
  font-size: 12px;
  font-weight: 600;
  color: #8c8c8c;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.org-tree {
  padding: 0 4px;
}

/* 右侧主内容 */
.contacts-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.main-header {
  height: 64px;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  color: #262626;
  margin: 0;
}

.member-count {
  padding: 4px 10px;
  background: #f5f5f5;
  color: #8c8c8c;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #8c8c8c;
}

.avatar-preview {
  display: flex;
  align-items: center;
  margin-right: 8px;
}

.preview-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 2px solid #fff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 500;
  margin-left: -8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.preview-avatar:first-child {
  margin-left: 0;
}

.preview-avatar.add-more {
  background: #f5f5f5;
  color: #bfbfbf;
  cursor: pointer;
}

.preview-avatar.add-more:hover {
  background: #e6e6e6;
}

.divider {
  width: 1px;
  height: 16px;
  background: #d9d9d9;
  margin: 0 8px;
}

.settings-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  color: #595959;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s;
}

.settings-btn:hover {
  color: #1677ff;
}

.main-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.members-table {
  background: #fff;
  border-radius: 8px;
  /* box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05); */
  /* border: 1px solid #e6e6e6; */
  overflow: hidden;
}

.table {
  width: 100%;
  border-collapse: collapse;
}

.table thead {
  background: #f7f9fb;
  border-bottom: 1px solid #f0f0f0;
}

.table th {
  padding: 12px 24px;
  text-align: left;
  font-size: 13px;
  font-weight: 500;
  color: #646a73;
  /* text-transform: uppercase; */
  /* letter-spacing: 0.5px; */
}

/* ... existing code ... */

.table tbody tr {
  border-bottom: 1px solid #f5f6f7;
  cursor: pointer;
  transition: background 0.2s;
}

.table tbody tr:hover {
  background: #f5f6f7;
}

/* ... existing code ... */

.member-row {
  transition: all 0.2s;
}

.info-col {
  width: 40%;
}

.member-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

:deep(.member-avatar-item) {
  border-radius: 12px !important;
}

.member-name {
  font-size: 14px;
  font-weight: 500;
  color: #262626;
}

.member-position-mobile {
  font-size: 12px;
  color: #8c8c8c;
}

.position-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  background: #e6f7ff;
  color: #1677ff;
}

.contact-col {
  display: none;
}

.contact-info {
  font-size: 14px;
  color: #8c8c8c;
}

.status-indicator {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-indicator.online .status-dot {
  background: #22c55e;
  box-shadow: 0 0 8px rgba(34, 197, 94, 0.4);
}

.status-indicator.offline .status-dot {
  background: #d9d9d9;
}

.status-indicator.online .status-text {
  color: #595959;
}

.status-indicator.offline .status-text {
  color: #8c8c8c;
}

.status-text {
  font-size: 14px;
}

/* 空状态 */
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #bfbfbf;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 14px;
  margin: 0;
}

/* 响应式 */
@media (min-width: 768px) {
  .table th.contact-col,
  .table td.contact-col {
    display: table-cell;
  }
}

/* 暗色模式 */
:deep(.dark) .contacts-sidebar,
:deep(.dark) .main-header {
  background: #1e293b;
  border-color: #334155;
}

:deep(.dark) .members-table {
  background: #1e293b;
  border-color: #334155;
  box-shadow: none;
}

:deep(.dark) .sidebar-title,
:deep(.dark) .member-name {
  color: #f1f5f9;
}

:deep(.dark) .nav-item {
  color: #cbd5e1;
}

:deep(.dark) .nav-item:hover {
  background: rgba(51, 65, 85, 0.5);
}

:deep(.dark) .search-input {
  background: #0f172a;
  color: #cbd5e1;
}

:deep(.dark) .table thead {
  background: rgba(15, 23, 42, 0.5);
  border-bottom: 1px solid #334155;
}

:deep(.dark) .table th {
  color: #64748b;
}

:deep(.dark) .table tbody tr:hover {
  background: rgba(51, 65, 85, 0.3);
}

:deep(.dark) .status-indicator.offline .status-dot {
  background: #475569;
}

:deep(.dark) .status-indicator.online .status-text {
  color: #cbd5e1;
}

:deep(.dark) .status-indicator.offline .status-text {
  color: #64748b;
}
</style>
