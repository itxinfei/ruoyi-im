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
            :class="{ active: view === 'friends' }"
            @click="handleNav('friends')"
          >
            <div class="nav-indicator"></div>
            <div class="nav-icon friends-icon">
              <span class="material-icons-outlined">people</span>
            </div>
            <span class="nav-label">我的好友</span>
            <div v-if="friendCount > 0" class="nav-count">{{ friendCount }}</div>
          </div>
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

      <!-- 我的好友视图 -->
      <template v-else-if="view === 'friends'">
        <header class="main-header">
          <div class="header-left">
            <el-breadcrumb separator="/" class="header-breadcrumb">
              <el-breadcrumb-item>
                <span class="material-icons-outlined breadcrumb-icon">home</span>
                通讯录
              </el-breadcrumb-item>
              <el-breadcrumb-item>我的好友</el-breadcrumb-item>
            </el-breadcrumb>
            <div class="member-count">
              <span class="material-icons-outlined count-icon">people</span>
              {{ friendCount }} 人
            </div>
          </div>
          <div class="header-right">
            <el-tooltip content="刷新" placement="bottom">
              <button class="icon-btn" @click="loadFriends">
                <span class="material-icons-outlined">refresh</span>
              </button>
            </el-tooltip>
          </div>
        </header>

        <div class="main-content friends-content">
          <div v-if="loadingFriends" v-loading="true" class="loading-wrapper"></div>
          <div v-else-if="groupedFriends.length === 0" class="empty-state">
            <span class="material-icons-outlined empty-icon">person_off</span>
            <h3 class="empty-title">暂无好友</h3>
            <p class="empty-text">点击上方添加按钮开始添加好友</p>
          </div>
          <div v-else class="friends-list">
            <div v-for="group in groupedFriends" :key="group.groupName || 'default'" class="friend-group">
              <div class="friend-group-header" @click="toggleGroupCollapse(group.groupName)">
                <span class="collapse-icon" :class="{ collapsed: isGroupCollapsed(group.groupName) }">
                  <span class="material-icons-outlined">expand_more</span>
                </span>
                <h4 class="friend-group-name">{{ group.groupName || '未分组' }}</h4>
                <span class="friend-group-count">{{ group.contacts.length }} 人</span>
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
                    <h5 class="friend-name">{{ friend.remark || friend.friendName }}</h5>
                    <p class="friend-desc">{{ friend.position || friend.department || '联系人' }}</p>
                  </div>
                  <div class="friend-online" :class="{ online: friend.online }"></div>
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

      <!-- 搜索结果视图 -->
      <template v-else-if="view === 'search'">
        <header class="main-header">
          <div class="header-left">
            <el-breadcrumb separator="/" class="header-breadcrumb">
              <el-breadcrumb-item>
                <span class="material-icons-outlined breadcrumb-icon">home</span>
                通讯录
              </el-breadcrumb-item>
              <el-breadcrumb-item>搜索结果</el-breadcrumb-item>
            </el-breadcrumb>
            <div class="member-count">
              <span class="material-icons-outlined count-icon">search</span>
              {{ searchResults.length }} 条结果
            </div>
          </div>
          <div class="header-right">
            <el-tag type="info" size="small">"{{ searchQuery }}"</el-tag>
          </div>
        </header>

        <div class="main-content search-content">
          <div v-if="searching" v-loading="true" class="loading-wrapper"></div>
          <div v-else-if="searchResults.length === 0" class="empty-state">
            <span class="material-icons-outlined empty-icon">search_off</span>
            <h3 class="empty-title">未找到结果</h3>
            <p class="empty-text">试试其他关键词</p>
          </div>
          <div v-else class="search-results-list">
            <div
              v-for="result in searchResults"
              :key="result.id"
              class="search-result-item"
              @click="handleMemberClick(result)"
            >
              <DingtalkAvatar
                :name="result.name"
                :user-id="result.id"
                :size="44"
                :src="result.avatar"
              />
              <div class="result-info">
                <h5 class="result-name">
                  {{ result.name }}
                  <span v-if="result.department" class="result-dept">{{ result.department }}</span>
                </h5>
                <p class="result-desc">{{ result.position || '员工' }}</p>
              </div>
              <div class="result-online" :class="{ online: result.online }"></div>
              <el-tooltip content="发消息" placement="top">
                <button class="result-action-btn" @click.stop="handleChat(result)">
                  <span class="material-icons-outlined">chat_bubble</span>
                </button>
              </el-tooltip>
            </div>
          </div>
        </div>
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
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { getFriendRequests, getGroupedFriendList } from '@/api/im/contact'
import { getDepartmentMembers, searchOrgMembers } from '@/api/im/organization'
import { createConversation } from '@/api/im/conversation'
import AddContactDialog from '@/components/Contacts/AddContactDialog.vue'
import OrganizationTree from '@/components/Contacts/OrganizationTree.vue'
import NewFriendsView from '@/components/Contacts/NewFriendsView.vue'
import GroupsView from '@/components/Contacts/GroupsView.vue'
import UserProfileDialog from '@/components/Contacts/UserProfileDialog.vue'
import GroupProfileDialog from '@/components/Contacts/GroupProfileDialog.vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const router = useRouter()
const store = useStore()
const view = ref('friends') // friends | department | new-friends | groups | search
const selectedDept = ref(null)
const searchQuery = ref('')
const showAddDialog = ref(false)
const requestCount = ref(0)
const friendCount = ref(0)
const loadingFriends = ref(false)
const groupedFriends = ref([])

// 分组折叠状态
const collapsedGroups = ref(new Set())

// 搜索相关状态
const searchResults = ref([])
const searching = ref(false)
const searchView = ref('') // 'all' | 'members' | 'friends' | 'groups'

// 详情弹窗控制
const showUserDialog = ref(false)
const selectedUserId = ref(null)
const showGroupDialog = ref(false)
const selectedGroupId = ref(null)

const deptMembers = ref([])

// 切换分组折叠状态
const toggleGroupCollapse = (groupName) => {
  const key = groupName || 'default'
  if (collapsedGroups.value.has(key)) {
    collapsedGroups.value.delete(key)
  } else {
    collapsedGroups.value.add(key)
  }
}

// 判断分组是否折叠
const isGroupCollapsed = (groupName) => {
  return collapsedGroups.value.has(groupName || 'default')
}

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
      // 使用 Vuex 选择会话
      const conversationId = res.data
      await store.dispatch('im/session/selectSessionById', conversationId)

      // 通知 MainPage 切换到聊天模块
      window.dispatchEvent(new CustomEvent('switch-to-chat', {
        detail: { conversationId }
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
      // 计算好友总数
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

const handleFriendClick = (friend) => {
  selectedUserId.value = friend.friendId || friend.id
  showUserDialog.value = true
}

const groupsViewRef = ref(null)
const loadGroups = () => {
  groupsViewRef.value?.loadGroups?.()
}

onMounted(() => {
  checkRequests()
  loadFriends()

  // 添加键盘快捷键监听
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  // 移除键盘快捷键监听
  window.removeEventListener('keydown', handleKeydown)
})

// 键盘快捷键处理
const handleKeydown = (e) => {
  // ESC 关闭弹窗
  if (e.key === 'Escape') {
    if (showAddDialog.value) {
      showAddDialog.value = false
    } else if (showUserDialog.value) {
      showUserDialog.value = false
    } else if (showGroupDialog.value) {
      showGroupDialog.value = false
    }
  }

  // Ctrl/Cmd + F 聚焦搜索框
  if ((e.ctrlKey || e.metaKey) && e.key === 'f') {
    e.preventDefault()
    document.querySelector('.search-input')?.focus()
  }
}

// 搜索功能 - 防抖处理
let searchTimer = null
const SEARCH_DELAY = 300

watch(searchQuery, (newVal) => {
  clearTimeout(searchTimer)

  if (!newVal || newVal.trim().length === 0) {
    // 清空搜索，返回之前的视图
    view.value = 'friends'
    searchResults.value = []
    return
  }

  searchTimer = setTimeout(() => {
    performSearch(newVal.trim())
  }, SEARCH_DELAY)
})

const performSearch = async (keyword) => {
  if (!keyword) return

  searching.value = true
  view.value = 'search'
  searchView.value = 'all'

  try {
    // 搜索组织成员
    const res = await searchOrgMembers({ keyword })
    if (res.code === 200) {
      searchResults.value = (res.data || []).map(member => ({
        ...member,
        online: Math.random() > 0.5 // 模拟在线状态
      }))
    }
  } catch (e) {
    console.error('搜索失败', e)
    ElMessage.error('搜索失败')
  } finally {
    searching.value = false
  }
}

// 计算搜索结果分类
const searchResultMembers = computed(() => {
  return searchResults.value.filter(r => r.type === 'member')
})

const searchResultFriends = computed(() => {
  return searchResults.value.filter(r => r.type === 'friend')
})

const searchResultGroups = computed(() => {
  return searchResults.value.filter(r => r.type === 'group')
})
</script>

<style scoped lang="scss">
@import '@/styles/design-tokens.scss';

// ============================================================================
// 容器 - 极简主义布局
// ============================================================================
.contacts-panel {
  display: flex;
  height: 100%;
  flex: 1;
  min-width: 0;
  background: var(--dt-bg-body);
  animation: fadeIn 0.4s ease-out;
}

// ============================================================================
// 左侧边栏 - 精致导航
// ============================================================================
.contacts-sidebar {
  width: 240px;
  background: var(--dt-bg-card);
  border-right: 1px solid var(--dt-border-light);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: all 0.3s var(--dt-ease-out);

  .sidebar-header {
    padding: 24px 20px 16px;
    display: flex;
    align-items: center;
    justify-content: space-between;

    .sidebar-title {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 20px;
      font-weight: 800;
      color: var(--dt-text-primary);
      letter-spacing: -0.5px;

      .title-icon {
        font-size: 24px;
        background: linear-gradient(135deg, var(--dt-brand-color) 0%, var(--dt-brand-active) 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
      }
    }
  }
}

.add-btn {
  width: 32px;
  height: 32px;
  color: var(--dt-brand-color);
  background: var(--dt-brand-lighter);
  border: none;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s var(--dt-ease-bounce);

  &:hover {
    background: var(--dt-brand-color);
    color: #fff;
    transform: rotate(90deg) scale(1.1);
  }
}

// 搜索框美化
.sidebar-search {
  padding: 0 16px 20px;

  .search-input-wrapper {
    position: relative;
    
    .search-icon {
      position: absolute;
      left: 12px;
      color: #8f959e;
      font-size: 18px;
    }

    .search-input {
      width: 100%;
      height: 36px;
      background: var(--dt-bg-body);
      border: 1px solid transparent;
      border-radius: 6px;
      padding: 0 36px;
      font-size: 13px;
      color: var(--dt-text-primary);
      transition: all 0.3s var(--dt-ease-out);

      &:focus {
        background: #fff;
        border-color: var(--dt-brand-color);
        box-shadow: 0 0 0 4px var(--dt-brand-lighter);
        .dark & { background: var(--dt-bg-card-dark); }
      }
    }
  }
}

// 侧边栏菜单 - 美化样式
.sidebar-content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 0 12px;
  scrollbar-width: thin;
  scrollbar-color: transparent transparent;

  &:hover {
    scrollbar-color: var(--dt-border-light) transparent;
  }

  &::-webkit-scrollbar {
    width: 4px;
  }
  &::-webkit-scrollbar-thumb {
    background: transparent;
    border-radius: 2px;
  }
  &:hover::-webkit-scrollbar-thumb {
    background: var(--dt-border-light);
  }

  .quick-nav {
    display: flex;
    flex-direction: column;
    gap: 4px;
    margin-bottom: 16px;
    padding: 4px 0;

    .nav-item {
      position: relative;
      display: flex;
      align-items: center;
      gap: 10px;
      height: 44px;
      padding: 0 14px;
      border-radius: 10px;
      cursor: pointer;
      transition: all 0.2s var(--dt-ease-out);
      color: var(--dt-text-secondary);
      font-size: 14px;
      font-weight: 500;

      .nav-indicator {
        position: absolute;
        left: 0;
        width: 3px;
        height: 20px;
        background: var(--dt-brand-color);
        border-radius: 0 3px 3px 0;
        opacity: 0;
        transform: scaleY(0.7);
        transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
      }

      .nav-icon {
        width: 36px;
        height: 36px;
        border-radius: 11px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        box-shadow: 0 4px 12px rgba(0,0,0,0.08);
        flex-shrink: 0;
        transition: all 0.25s var(--dt-ease-out);

        .material-icons-outlined {
          font-size: 18px;
        }

        &.friends-icon {
          background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
        }
        &.new-friends-icon {
          background: linear-gradient(135deg, #10b981 0%, #059669 100%);
        }
        &.groups-icon {
          background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
        }
      }

      .nav-label {
        font-size: 14px;
        font-weight: 500;
        flex: 1;
      }

      .nav-badge, .nav-count {
        padding: 3px 8px;
        background: var(--dt-error-color);
        color: #fff;
        border-radius: 11px;
        font-size: 11px;
        font-weight: 700;
        min-width: 20px;
        text-align: center;
        box-shadow: 0 2px 6px rgba(239, 68, 68, 0.25);
      }

      .nav-count {
        background: var(--dt-brand-color);
        box-shadow: 0 2px 6px rgba(22, 119, 255, 0.25);
      }

      &:hover {
        background: var(--dt-bg-hover);
        color: var(--dt-text-primary);
        transform: translateX(5px);

        .nav-icon {
          transform: scale(1.05);
          box-shadow: 0 6px 16px rgba(0,0,0,0.12);
        }
      }

      &.active {
        background: var(--dt-brand-lighter);
        color: var(--dt-brand-color);
        transform: translateX(3px);

        .nav-indicator {
          opacity: 1;
          transform: scaleY(1);
        }
        .nav-icon {
          box-shadow: 0 6px 16px rgba(22, 119, 255, 0.25);
        }
      }
    }
  }
}

// 组织架构
.org-section {
  margin-top: 16px;

  .org-section-header {
    padding: 0 14px 8px;
    display: flex;
    align-items: center;
    justify-content: space-between;

    .org-title {
      font-size: 11px;
      font-weight: 700;
      color: var(--dt-text-quaternary);
      text-transform: uppercase;
      letter-spacing: 1px;
    }
    .org-icon { font-size: 16px; color: var(--dt-text-quaternary); }
  }

  .org-tree {
    padding: 0 8px;
  }
}

// ============================================================================
// 组织架构树样式优化
// ============================================================================
:deep(.org-tree) {
  .org-tree-node {
    padding: 6px 8px;
    margin: 2px 0;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    color: var(--dt-text-secondary);

    &:hover {
      background: var(--dt-bg-hover);
      color: var(--dt-brand-color);
    }

    &.active {
      background: var(--dt-brand-lighter);
      color: var(--dt-brand-color);
      font-weight: 600;
    }

    .tree-node-icon {
      color: var(--dt-text-quaternary);
      transition: color 0.2s;
    }

    &:hover .tree-node-icon {
      color: var(--dt-brand-color);
    }

    .tree-node-children {
      margin-left: 16px;
      padding-left: 12px;
      border-left: 1px dashed var(--dt-border-light);
    }
  }
}

// ============================================================================
// 右侧主内容 - 现代画布
// ============================================================================
.contacts-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #fff;
  .dark & { background: var(--dt-bg-body-dark); }
}

.main-header {
  height: 72px;
  padding: 0 32px;
  border-bottom: 1px solid var(--dt-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  backdrop-filter: blur(20px);
  background: rgba(255,255,255,0.8);
  .dark & { background: rgba(15, 23, 42, 0.8); border-color: var(--dt-border-dark); }

  .header-left {
    display: flex;
    align-items: center;
    gap: 20px;

    .member-count {
      padding: 4px 12px;
      background: var(--dt-brand-lighter);
      color: var(--dt-brand-color);
      border-radius: 8px;
      font-size: 12px;
      font-weight: 700;
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
}

.avatar-preview {
  display: flex;
  .preview-avatar {
    width: 32px;
    height: 32px;
    border-radius: 6px;
    border: 2px solid #fff;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 11px;
    font-weight: 700;
    margin-left: -10px;
    box-shadow: 0 4px 8px rgba(0,0,0,0.1);
    &.more-count { background: var(--dt-bg-body); color: var(--dt-text-secondary); }
    .dark & { border-color: var(--dt-bg-card-dark); }
  }
}

.main-content {
  flex: 1;
  padding: 32px;
  overflow-y: auto;
  scroll-behavior: smooth;
}

// 成员网格美化
.members-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.member-card {
  background: var(--dt-bg-card);
  border-radius: 6px;
  padding: 24px 20px;
  border: 1px solid var(--dt-border-light);
  transition: all 0.3s var(--dt-ease-bounce);
  text-align: center;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0; left: 0; right: 0; height: 4px;
    background: linear-gradient(90deg, var(--dt-brand-color), var(--dt-brand-active));
    opacity: 0;
    transition: opacity 0.3s;
  }

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 20px 40px rgba(0,0,0,0.06);
    border-color: var(--dt-brand-lighter);
    &::before { opacity: 1; }
    .member-actions { transform: translateY(0); opacity: 1; }
  }

  .member-avatar {
    margin: 0 auto 16px;
    border-radius: 6px !important;
    box-shadow: 0 8px 16px rgba(0,0,0,0.08);
  }

  .member-name { font-size: 16px; font-weight: 700; color: var(--dt-text-primary); margin-bottom: 4px; }
  .member-position { font-size: 12px; color: var(--dt-text-tertiary); margin-bottom: 20px; }
}

.member-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  transform: translateY(10px);
  opacity: 0;
  transition: all 0.3s;

  .action-btn {
    width: 36px; height: 36px;
    border-radius: 6px;
    background: var(--dt-brand-lighter);
    color: var(--dt-brand-color);
    border: none;
    display: flex; align-items: center; justify-content: center;
    cursor: pointer;
    &:hover { background: var(--dt-brand-color); color: #fff; transform: scale(1.1); }
  }
}

// 好友分组列表美化
.friend-group {
  margin-bottom: 32px;
  .friend-group-header {
    display: flex; align-items: center; gap: 12px; margin-bottom: 16px;
    &::after { content: ''; flex: 1; height: 1px; background: var(--dt-border-light); }
    .friend-group-name { font-size: 13px; font-weight: 800; color: var(--dt-text-quaternary); }
  }
}

.friend-item {
  display: flex; align-items: center; gap: 16px;
  padding: 12px 20px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  &:hover { background: var(--dt-bg-body); transform: translateX(4px); }

  .friend-info {
    flex: 1;
    .friend-name { font-size: 15px; font-weight: 600; color: var(--dt-text-primary); }
    .friend-desc { font-size: 12px; color: var(--dt-text-tertiary); }
  }

  .friend-online {
    width: 8px; height: 8px; border-radius: 50%;
    background: #cbd5e1;
    &.online { background: #22c55e; box-shadow: 0 0 12px #22c55e; animation: pulse 2s infinite; }
  }
}

// 动画
@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(34, 197, 94, 0.4); }
  70% { box-shadow: 0 0 0 10px rgba(34, 197, 94, 0); }
  100% { box-shadow: 0 0 0 0 rgba(34, 197, 94, 0); }
}

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

// 暗色模式微调
.dark {
  .contacts-sidebar, .main-header { border-color: #1e293b; }
  .member-card { background: #1e293b; border-color: #334155; }
  .friend-item:hover { background: #1e293b; }
  .nav-item:hover { background: #1e293b; }
}

// ============================================================================
// 搜索结果样式
// ============================================================================
.search-content {
  padding: 24px;
}

.search-results-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.search-result-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 20px;
  border-radius: 12px;
  background: var(--dt-bg-card);
  border: 1px solid var(--dt-border-light);
  cursor: pointer;
  transition: all 0.2s var(--dt-ease-out);

  &:hover {
    background: var(--dt-bg-hover);
    border-color: var(--dt-brand-lighter);
    transform: translateX(4px);

    .result-action-btn {
      opacity: 1;
      transform: scale(1);
    }
  }

  .result-info {
    flex: 1;
    min-width: 0;

    .result-name {
      font-size: 15px;
      font-weight: 600;
      color: var(--dt-text-primary);
      margin-bottom: 4px;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .result-dept {
      font-size: 12px;
      font-weight: 400;
      color: var(--dt-text-tertiary);
      background: var(--dt-bg-body);
      padding: 2px 8px;
      border-radius: 6px;
    }

    .result-desc {
      font-size: 13px;
      color: var(--dt-text-tertiary);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  .result-online {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #cbd5e1;
    flex-shrink: 0;

    &.online {
      background: #22c55e;
      box-shadow: 0 0 12px #22c55e;
      animation: pulse 2s infinite;
    }
  }

  .result-action-btn {
    width: 36px;
    height: 36px;
    border-radius: 10px;
    background: var(--dt-brand-lighter);
    color: var(--dt-brand-color);
    border: none;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    opacity: 0;
    transform: scale(0.9);
    transition: all 0.2s;
    flex-shrink: 0;

    &:hover {
      background: var(--dt-brand-color);
      color: #fff;
      transform: scale(1.1);
    }
  }
}

.loading-wrapper {
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

// ============================================================================
// 在线状态指示器
// ============================================================================
.online-indicator {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #cbd5e1;
  border: 2px solid #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;

  &.online {
    background: #22c55e;
    box-shadow: 0 0 0 2px rgba(34, 197, 94, 0.2);
  }

  .dark & {
    border-color: var(--dt-bg-card);
  }
}

// ============================================================================
// 面包屑导航
// ============================================================================
:deep(.header-breadcrumb) {
  .el-breadcrumb__item {
    .el-breadcrumb__inner {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 14px;
      color: var(--dt-text-secondary);
      font-weight: 500;

      .breadcrumb-icon {
        font-size: 16px;
      }
    }

    &:last-child .el-breadcrumb__inner {
      color: var(--dt-text-primary);
      font-weight: 600;
    }
  }

  .el-breadcrumb__separator {
    color: var(--dt-border-color);
    margin: 0 8px;
  }
}

// ============================================================================
// 图标按钮
// ============================================================================
.icon-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: transparent;
  border: none;
  color: var(--dt-text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s var(--dt-ease-out);

  &:hover {
    background: var(--dt-bg-hover);
    color: var(--dt-brand-color);
  }

  &:active {
    transform: scale(0.95);
  }
}

.divider {
  width: 1px;
  height: 24px;
  background: var(--dt-border-light);
  margin: 0 12px;
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

  .empty-icon {
    font-size: 64px;
    color: var(--dt-text-quaternary);
    margin-bottom: 16px;
  }

  .empty-title {
    font-size: 18px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin-bottom: 8px;
  }

  .empty-text {
    font-size: 14px;
    color: var(--dt-text-tertiary);
  }
}

.empty-state-large {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;

  .empty-illustration {
    position: relative;
    margin-bottom: 24px;

    .empty-icon {
      font-size: 80px;
      color: var(--dt-brand-color);
      opacity: 0.5;
    }

    .empty-decoration {
      position: absolute;
      bottom: -10px;
      left: 50%;
      transform: translateX(-50%);
      width: 40px;
      height: 4px;
      background: var(--dt-brand-color);
      border-radius: 2px;
      opacity: 0.3;
    }
  }

  .empty-title {
    font-size: 20px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin-bottom: 12px;
  }

  .empty-text {
    font-size: 14px;
    color: var(--dt-text-tertiary);
  }
}

// ============================================================================
// 清除按钮
// ============================================================================
.clear-btn {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--dt-text-quaternary);
  border-radius: 4px;
  transition: all 0.2s;

  &:hover {
    background: var(--dt-bg-hover);
    color: var(--dt-text-secondary);
  }
}

// ============================================================================
// 好友列表滚动优化
// ============================================================================
.friends-content {
  .friends-list {
    max-width: 800px;
  }
}

.friend-group-members {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

// ============================================================================
// 分组折叠功能
// ============================================================================
.friend-group-header {
  cursor: pointer;
  user-select: none;
  transition: all 0.2s;

  .collapse-icon {
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--dt-text-quaternary);
    transition: transform 0.3s var(--dt-ease-out);

    &.collapsed {
      transform: rotate(-90deg);
    }

    .material-icons-outlined {
      font-size: 18px;
    }
  }

  &:hover {
    .friend-group-name {
      color: var(--dt-brand-color);
    }

    .collapse-icon {
      color: var(--dt-brand-color);
    }
  }
}

// ============================================================================
// 响应式优化
// ============================================================================
@media (max-width: 1024px) {
  .contacts-sidebar {
    width: 200px;
  }

  .members-grid {
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
    gap: 16px;
  }

  .main-content {
    padding: 20px;
  }
}

@media (max-width: 768px) {
  .contacts-panel {
    flex-direction: column;
  }

  .contacts-sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid var(--dt-border-light);
  }

  .sidebar-content {
    max-height: 200px;
  }

  .members-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  }
}
</style>
