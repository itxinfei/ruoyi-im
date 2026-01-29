<template>
  <div class="contacts-panel" :class="{ 'mobile-view': isMobile }">
    <!-- Mobile Overlay -->
    <div 
      v-if="isMobile && showMobileSidebar" 
      class="mobile-overlay"
      @click="showMobileSidebar = false"
    ></div>

    <!-- 左侧导航栏 (Sidebar / Drawer) -->
    <aside 
      class="sidebar" 
      :class="{ 'sidebar-open': showMobileSidebar }"
    >
      <!-- 搜索框 -->
      <div class="search-section">
        <div class="search-box">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <path d="M21 21l-4.35-4.35" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索联系人、群组"
            @input="handleSearch"
          />
          <button v-if="searchQuery" class="clear-btn" @click="clearSearch">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 6L6 18M6 6l12 12" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- 导航菜单 -->
      <nav class="nav-list scrollbar-sm">
        <div
          class="nav-item"
          :class="{ active: currentNav === 'new' }"
          @click="switchNav('new')"
        >
          <div class="icon-wrapper bg-orange">
             <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                <circle cx="8.5" cy="7" r="4"></circle>
                <line x1="20" y1="8" x2="20" y2="14"></line>
                <line x1="23" y1="11" x2="17" y2="11"></line>
            </svg>
          </div>
          <span class="item-text">新的朋友</span>
          <span v-if="pendingCount > 0" class="item-badge">{{ pendingCount > 99 ? '99+' : pendingCount }}</span>
        </div>

        <div
          class="nav-item"
          :class="{ active: currentNav === 'friends' }"
          @click="switchNav('friends')"
        >
           <div class="icon-wrapper bg-blue">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                <circle cx="9" cy="7" r="4"></circle>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
            </svg>
           </div>
          <span class="item-text">我的好友</span>
        </div>

        <div
          class="nav-item"
          :class="{ active: currentNav === 'groups' }"
          @click="switchNav('groups')"
        >
          <div class="icon-wrapper bg-green">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="3" width="7" height="7"></rect>
                <rect x="14" y="3" width="7" height="7"></rect>
                <rect x="14" y="14" width="7" height="7"></rect>
                <rect x="3" y="14" width="7" height="7"></rect>
            </svg>
          </div>
          <span class="item-text">我的群组</span>
        </div>

        <div class="nav-divider"></div>

        <!-- 组织架构 -->
        <div class="org-section">
          <div
            class="nav-item org-root"
            :class="{ expanded: orgExpanded }"
            @click="toggleOrg"
          >
             <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="9 18 15 12 9 6"></polyline>
            </svg>
            <span class="item-text font-medium">组织架构</span>
          </div>

          <div v-show="orgExpanded" class="org-list">
            <div
              v-for="dept in flatDepts"
              :key="dept.id"
              class="org-item"
              :class="{
                'is-child': dept.isChild,
                active: selectedDeptId === dept.id
              }"
              @click="selectDept(dept)"
            >
              <span v-if="dept.isChild" class="org-dot"></span>
              <span class="org-name">{{ dept.name }}</span>
              <span class="org-count">({{ dept.userCount || 0 }})</span>
            </div>
          </div>
        </div>
      </nav>
    </aside>

    <!-- 中间列表栏 -->
    <main class="list-panel" :class="{ 'has-index': showIndexBar }">
      <!-- Mobile Header -->
      <div v-if="isMobile" class="mobile-header">
        <button class="menu-btn" @click="showMobileSidebar = true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="3" y1="12" x2="21" y2="12"></line>
                <line x1="3" y1="6" x2="21" y2="6"></line>
                <line x1="3" y1="18" x2="21" y2="18"></line>
            </svg>
        </button>
        <span class="mobile-title">{{ listTitle }}</span>
      </div>

      <!-- A-Z 索引栏 (仅组织架构显示) -->
      <div v-if="showIndexBar && !isMobile" class="index-bar">
        <span
          v-for="letter in indexLetters"
          :key="letter"
          class="index-item"
          :class="{ active: activeLetter === letter, disabled: !letterCounts[letter] }"
          @click="scrollToLetter(letter)"
        >
          {{ letter }}
        </span>
      </div>

      <!-- 列表头部 (Desktop Only) -->
      <header v-if="!isMobile" class="list-header">
        <h2 class="list-title">{{ listTitle }}</h2>
        <span v-if="listCount > 0" class="list-count">{{ listCount }}</span>
      </header>

      <!-- 列表内容 -->
      <div class="list-body">
        <!-- 加载中 -->
        <div v-if="loading" class="loading">
          <div class="spinner"></div>
          <span>加载中...</span>
        </div>

        <!-- 搜索结果 -->
        <div v-else-if="searchQuery && searchResults.length > 0" class="search-list">
             <VirtualList
                class="virtual-scroll-container"
                :items="searchResults"
                :item-size="60"
            >
                <template #default="{ item }">
                    <ContactItem
                        :item="item"
                        :is-active="selectedItemId === item.id"
                        :search-query="searchQuery"
                        @click="selectItem(item)"
                    />
                </template>
            </VirtualList>
        </div>

        <!-- 新的朋友 -->
        <template v-else-if="currentNav === 'new' && !searchQuery">
          <NewFriendsView ref="newFriendsRef" @update-count="updatePendingCount" />
        </template>

        <!-- 我的好友 / 组织架构 / Search Empty -->
        <template v-else>
             <!-- 使用 VirtualList 渲染扁平化的列表 (包含 Header) -->
             <div v-if="virtualListData.length === 0" class="empty">
                <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" stroke-width="1.5">
                    <circle cx="19" cy="19" r="9"/>
                    <path d="M39 39l-6-6" stroke-linecap="round"/>
                </svg>
                <p>暂无数据</p>
             </div>
             
             <VirtualList
                v-else
                ref="virtualListRef"
                class="virtual-scroll-container"
                :items="virtualListData"
                :item-size="getItemSize"
                @scroll="handleListScroll"
            >
                <template #default="{ item }">
                     <!-- Group Header -->
                    <div v-if="item.type === 'header'" class="list-group-header">
                        {{ item.title }}
                    </div>
                    <!-- Contact Item -->
                    <ContactItem
                        v-else
                        :item="item"
                        :is-active="selectedItemId === item.id"
                        @click="selectItem(item)"
                        @delete="handleDelete"
                    />
                </template>
            </VirtualList>
        </template>
      </div>
    </main>

    <!-- 右侧详情栏 -->
    <aside class="detail-panel" :class="{ 'detail-open': selectedItemId && isMobile }">
       <!-- Mobile Back Button -->
       <div v-if="isMobile && selectedItemId" class="mobile-detail-nav">
           <button class="back-btn" @click="selectedItemId = null">
               <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                   <path d="M15 18l-6-6 6-6" stroke-linecap="round" stroke-linejoin="round"/>
               </svg>
               返回
           </button>
       </div>

      <div v-if="!selectedItemId" class="empty-detail">
        <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="24" cy="24" r="18"/>
          <path d="M24 16v16M16 24h16" stroke-linecap="round"/>
        </svg>
        <p>选择联系人或群组查看详情</p>
      </div>

      <template v-else>
        <ContactDetail 
            v-if="selectedType === 'friend' || selectedType === 'member'" 
            :user="selectedItem"
            @voice-call="startVoiceCall"
            @video-call="startVideoCall"
            @message="startChat"
        />
        <GroupProfileDialog 
            v-else-if="selectedType === 'group'"
            :model-value="true" 
            :group="selectedItem"
            :inline="true"
            @close=""
        />
        <!-- Note: GroupProfileDialog typically is a dialog, we might need an extraction here or use content directly. 
             Ideally we should have a GroupDetail component. For now reusing existing or duplicating structure inside.
             The original code had inline Detail logic. Let's keep inline logic or Component if available.
             Original code had inline template. Let's extract to ensure cleanliness or restore inline.
             To be safe and consistent with 'ContactDetail' I will restore inline logic simplified or use a new component.
             Since I cannot see GroupProfileDialog content fully, I will restore inline layout for Groups to match original behavior but refined.
        -->
        <div v-else-if="selectedType === 'group'" class="detail-content">
             <div class="detail-header">
                <DingtalkAvatar :name="selectedItem?.name" :size="80" :src="selectedItem?.avatar" shape="square" />
                <div class="detail-name">{{ selectedItem?.name }}</div>
                <div class="detail-signature">{{ selectedItem?.description || '暂无描述' }}</div>
             </div>
             <div class="detail-actions">
                <el-button type="primary" size="large" @click="startChat">发消息</el-button>
             </div>
             <div class="detail-section">
                <div class="info-item">
                    <span class="info-label">群主</span>
                    <span class="info-value">{{ selectedItem?.ownerName || 'Unknown' }}</span>
                </div>
                 <div class="info-item">
                    <span class="info-label">成员数</span>
                    <span class="info-value">{{ selectedItem?.memberCount || 0 }}</span>
                </div>
             </div>
        </div>
      </template>
    </aside>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useStore } from 'vuex'
import { useWindowSize } from '@vueuse/core'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import NewFriendsView from '@/components/Contacts/NewFriendsView.vue'
import ContactItem from '@/components/Contacts/ContactItem.vue'
import ContactDetail from '@/components/Contacts/ContactDetail.vue'
import VirtualList from '@/components/Common/VirtualList.vue'
import { getFriendRequests, getGroupedFriendList } from '@/api/im/contact'
import { getGroups } from '@/api/im/group'
import { getOrgTree, getDepartmentMembers } from '@/api/im/organization'
import { ElMessage } from 'element-plus'

const store = useStore()
const { width } = useWindowSize()
const isMobile = computed(() => width.value < 768)

// State
const currentNav = ref('friends')
const searchQuery = ref('')
const showMobileSidebar = ref(false)
const loading = ref(false)

// Data
const friendGroups = ref([])
const groupList = ref([])
const orgMembers = ref([])
const pendingCount = ref(0)
const searchResults = ref([])

// Selection
const selectedItemId = ref(null)
const selectedType = ref(null)

// Org Tree
const orgExpanded = ref(true)
const selectedDeptId = ref(null)
const flatDepts = ref([])

// A-Z Index
const indexLetters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ#'.split('')
const activeLetter = ref('')
const virtualListRef = ref(null)

// Computed for Display Titles
const listTitle = computed(() => {
    if (searchQuery.value) return '搜索结果'
    const map = { new: '新的朋友', friends: '我的好友', groups: '我的群组', org: '组织架构' }
    return map[currentNav.value] || ''
})

const listCount = computed(() => {
    if (currentNav.value === 'friends') {
        return friendGroups.value.reduce((acc, g) => acc + g.friends.length, 0)
    }
    if (currentNav.value === 'groups') return groupList.value.length
    if (currentNav.value === 'org') return orgMembers.value.length
    return 0
})

const selectedItem = computed(() => {
    if (!selectedItemId.value) return null
    let list = []
    if (selectedType.value === 'friend') {
         friendGroups.value.forEach(g => list.push(...g.friends))
    } else if (selectedType.value === 'group') {
        list = groupList.value
    } else if (selectedType.value === 'member') {
        list = orgMembers.value
    }
    const item = list.find(i => i.id === selectedItemId.value)
    return item ? { ...item, type: selectedType.value } : null
})

// Flatten Data for VirtualList
const virtualListData = computed(() => {
    if (currentNav.value === 'groups') {
        return groupList.value.map(g => ({ ...g, type: 'group' }))
    }
    
    if (currentNav.value === 'friends') {
        // Flatten Grouped Friends (e.g. A -> [User1, User2])
        const res = []
        friendGroups.value.forEach(g => {
            if (g.friends && g.friends.length > 0) {
                res.push({ type: 'header', title: g.name, id: `header-${g.name}` })
                res.push(...g.friends.map(f => ({ ...f, type: 'friend' })))
            }
        })
        return res
    }
    
    if (currentNav.value === 'org') {
        // Group Org Members by A-Z
        // Note: Similar logic to `groupedOrgMembers` but flattened
        const groups = groupMembersByLetter(orgMembers.value)
        const res = []
        Object.keys(groups).sort().forEach(letter => {
            const members = groups[letter]
            if (members.length > 0) {
                if (letter !== '#') res.push({ type: 'header', title: letter, id: `header-${letter}` })
                res.push(...members.map(m => ({ ...m, type: 'member' })))
            }
        })
        // Handle # last
        if (groups['#'] && groups['#'].length > 0) {
             res.push({ type: 'header', title: '#', id: 'header-#' })
             res.push(...groups['#'].map(m => ({ ...m, type: 'member' })))
        }
        return res
    }
    
    return []
})

const letterCounts = computed(() => {
    if (currentNav.value !== 'org') return {}
    const counts = {}
    virtualListData.value.forEach(item => {
        if (item.type === 'header') {
            counts[item.title] = (counts[item.title] || 0) + 1
        }
    })
    return counts
})

// Helper: Group By Letter
function groupMembersByLetter(members) {
    const groups = {}
    indexLetters.forEach(l => groups[l] = [])
    
    members.forEach(m => {
        let char = (m.name?.[0] || '#').toUpperCase()
        if (!/[A-Z]/.test(char)) char = '#'
        if (!groups[char]) groups[char] = []
        groups[char].push(m)
    })
    return groups
}

// Variable Item Size for VirtualList
const getItemSize = (item) => {
    return item.type === 'header' ? 32 : 60
}

// Actions
const switchNav = (nav) => {
    currentNav.value = nav
    if (isMobile.value) showMobileSidebar.value = false
    selectedItemId.value = null
    searchQuery.value = ''
    
    if (nav === 'friends') loadFriends()
    else if (nav === 'groups') loadGroups()
    else if (nav === 'new') loadNewFriends()
}

const selectItem = (item) => {
    selectedItemId.value = item.id
    selectedType.value = item.type || (currentNav.value === 'groups' ? 'group' : 'friend')
    // Mobile: Stay on page but Detail panel slides in
}

const selectDept = (dept) => {
    selectedDeptId.value = dept.id
    currentNav.value = 'org'
    if (isMobile.value) showMobileSidebar.value = false
    loadOrgMembers(dept.id)
}

// Scroll Handling (A-Z)
const scrollToLetter = (letter) => {
    // Find index of header
    const index = virtualListData.value.findIndex(i => i.type === 'header' && i.title === letter)
    if (index !== -1 && virtualListRef.value) {
        virtualListRef.value.scrollToIndex(index)
        activeLetter.value = letter
        setTimeout(() => activeLetter.value = '', 1000)
    }
}

const handleListScroll = (e) => {
   // Logic to update activeLetter based on scroll position could be complex with VirtualList
   // Simpler: Use visible-range event from VirtualList if available, or just ignore for now
}

// API Loaders (Mirrored from original)
const loadFriends = async () => {
    loading.value = true
    try {
        const res = await getGroupedFriendList()
        friendGroups.value = res.data.map(g => ({
             name: g.groupName,
             friends: (g.friends||[]).map(f => ({
                 id: f.friendId,
                 displayName: f.remark || f.friendName,
                 avatar: f.friendAvatar,
                 dept: f.dept
             }))
        }))
    } finally { loading.value = false }
}

const loadGroups = async () => {
    loading.value = true
    try {
        const res = await getGroups()
        groupList.value = res.data || []
    } finally { loading.value = false }
}

const loadOrgTree = async () => {
    try {
        const res = await getOrgTree()
        if (res.data) {
             flatDepts.value = flattenOrgTree(res.data)
        }
    } catch(e) {}
}

const loadOrgMembers = async (deptId) => {
    loading.value = true
    try {
        const res = await getDepartmentMembers(deptId)
        orgMembers.value = res.data || []
    } finally { loading.value = false }
}

const loadNewFriends = async () => {
    const res = await getFriendRequests()
    pendingCount.value = res.data?.filter(r => r.status === 'PENDING').length || 0
}

// Handlers
const toggleOrg = () => orgExpanded.value = !orgExpanded.value
const updatePendingCount = (c) => pendingCount.value = c
const startChat = () => {
    if (!selectedItemId.value) return
    const type = selectedType.value === 'group' ? 'GROUP' : 'PRIVATE'
    store.dispatch('im/session/createAndSwitchSession', { targetId: selectedItemId.value, type })
}
const startVoiceCall = () => ElMessage.info('语音通话开发中')
const startVideoCall = () => ElMessage.info('视频通话开发中')
const clearSearch = () => searchQuery.value = ''
const handleSearch = () => {
    if (!searchQuery.value.trim()) {
        searchResults.value = []
        return
    }
    const q = searchQuery.value.toLowerCase()
    const results = []
    
    // Search Friends
    friendGroups.value.forEach(g => {
        if (g.friends) {
            g.friends.forEach(f => {
                const name = f.displayName || f.friendName || ''
                if (name.toLowerCase().includes(q)) {
                    results.push({ ...f, type: 'friend' })
                }
            })
        }
    })
    
    // Search Groups
    groupList.value.forEach(g => {
        if (g.name && g.name.toLowerCase().includes(q)) {
            results.push({ ...g, type: 'group' })
        }
    })
    
    searchResults.value = results
}
const handleDelete = () => ElMessage.warning('删除功能开发中')

// Utils
const flattenOrgTree = (tree) => {
    const res = []
    tree.forEach(d => {
        res.push({ id: d.id, name: d.name, isChild: false, userCount: d.userCount })
        if (d.children) {
            d.children.forEach(c => res.push({ id: c.id, name: c.name, isChild: true, parentId: d.id, userCount: c.userCount }))
        }
    })
    return res
}

onMounted(() => {
    loadFriends()
    loadOrgTree()
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// ============================================================================
// 布局容器
// ============================================================================

.contacts-panel {
  display: flex;
  width: 100%;
  height: 100%;
  background: var(--dt-bg-body);
  overflow: hidden;
  position: relative;

  // 暗色模式适配
  .dark & {
    background: var(--dt-bg-body-dark);
  }
}

// ============================================================================
// 侧边栏 Sidebar - 使用浅色背景，改善可读性
// ============================================================================

.sidebar {
  width: 260px;
  background: #ffffff;
  border-right: 1px solid var(--dt-border-divider);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: transform var(--dt-transition-base);
  z-index: 20;

  .dark & {
    background: var(--dt-bg-sidebar-dark);
    border-right-color: var(--dt-border-dark);
  }

  &.sidebar-open {
    transform: translateX(0);
  }
}

// Mobile 侧边栏
.mobile-view .sidebar {
  position: fixed;
  top: 0;
  left: 0;
  height: 100%;
  transform: translateX(-100%);
  width: 280px;
  max-width: 85vw;
  box-shadow: var(--dt-shadow-float);
}

.mobile-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 15;
  animation: fadeIn var(--dt-transition-base);
}

// ============================================================================
// 搜索区域
// ============================================================================

.search-section {
  padding: 16px;
  border-bottom: 1px solid var(--dt-border-lighter);

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
  height: 40px;
  background: var(--dt-bg-card-hover);
  border-radius: var(--dt-radius-md);
  padding: 0 12px;
  transition: all var(--dt-transition-base);

  &:focus-within {
    background: #ffffff;
    box-shadow: 0 0 0 2px var(--dt-brand-color);

    .dark & {
      background: var(--dt-bg-input-dark);
      box-shadow: 0 0 0 2px var(--dt-brand-color);
    }
  }

  .search-icon {
    width: 18px;
    height: 18px;
    color: var(--dt-text-tertiary);
    flex-shrink: 0;
    margin-right: 8px;
  }

  input {
    flex: 1;
    border: none;
    background: transparent;
    font-size: var(--dt-font-size-base);
    color: var(--dt-text-primary);
    outline: none;

    &::placeholder {
      color: var(--dt-text-quaternary);
    }
  }

  .clear-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 20px;
    height: 20px;
    border: none;
    background: transparent;
    color: var(--dt-text-tertiary);
    cursor: pointer;
    border-radius: 50%;
    transition: all var(--dt-transition-fast);

    &:hover {
      background: var(--dt-bg-overlay);
      color: var(--dt-text-secondary);
    }

    svg {
      width: 14px;
      height: 14px;
    }
  }
}

// ============================================================================
// 导航菜单
// ============================================================================

.nav-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 12px;

  @extend .scrollbar-sm;
}

.nav-divider {
  height: 1px;
  background: var(--dt-border-lighter);
  margin: 8px 0 12px;

  .dark & {
    background: var(--dt-border-dark);
  }
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all var(--dt-transition-fast);
  color: var(--dt-text-primary);
  user-select: none;

  &:hover {
    background: var(--dt-bg-session-hover);
  }

  &.active {
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);

    .icon-wrapper {
      background: var(--dt-brand-color);
    }
  }

  .item-text {
    flex: 1;
    font-size: var(--dt-font-size-base);
    font-weight: var(--dt-font-weight-normal);
  }

  .item-badge {
    min-width: 20px;
    height: 20px;
    padding: 0 6px;
    background: var(--dt-error-color);
    color: #ffffff;
    font-size: var(--dt-font-size-xs);
    font-weight: var(--dt-font-weight-medium);
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.icon-wrapper {
  width: 36px;
  height: 36px;
  border-radius: var(--dt-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  flex-shrink: 0;

  &.bg-orange { background: #FF9800; }
  &.bg-blue { background: #2196F3; }
  &.bg-green { background: #4CAF50; }

  svg {
    width: 20px;
    height: 20px;
  }
}

// 组织架构样式
.org-section {
  margin-top: 4px;

  .nav-item.org-root {
    padding: 8px 12px;
    color: var(--dt-text-secondary);
    font-weight: var(--dt-font-weight-medium);

    .arrow-icon {
      width: 16px;
      height: 16px;
      transition: transform var(--dt-transition-fast);

      .org-root.expanded & {
        transform: rotate(90deg);
      }
    }

    &.expanded {
      color: var(--dt-text-primary);
    }
  }

  .org-list {
    margin-left: 16px;
    padding: 4px 0;
  }

  .org-item {
    display: flex;
    align-items: center;
    padding: 6px 12px;
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-secondary);
    border-radius: var(--dt-radius-sm);
    cursor: pointer;
    transition: all var(--dt-transition-fast);

    &:hover {
      background: var(--dt-bg-session-hover);
      color: var(--dt-text-primary);
    }

    &.is-child {
      padding-left: 24px;
      position: relative;

      &::before {
        content: '';
        position: absolute;
        left: 12px;
        top: 50%;
        transform: translateY(-50%);
        width: 4px;
        height: 4px;
        background: var(--dt-border-color);
        border-radius: 50%;
      }
    }

    &.active {
      background: var(--dt-brand-bg);
      color: var(--dt-brand-color);
    }

    .org-name {
      flex: 1;
    }

    .org-count {
      font-size: var(--dt-font-size-xs);
      color: var(--dt-text-quaternary);
    }
  }
}

// ============================================================================
// 中间列表栏
// ============================================================================

.list-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--dt-bg-card);
  position: relative;
  border-right: 1px solid var(--dt-border-divider);

  .dark & {
    background: var(--dt-bg-card-dark);
    border-right-color: var(--dt-border-dark);
  }

  &.has-index {
    padding-right: 48px;
  }
}

.mobile-header {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-divider);
  flex-shrink: 0;

  .dark & {
    background: var(--dt-bg-card-dark);
    border-bottom-color: var(--dt-border-dark);
  }

  .menu-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    border: none;
    padding: 0;
    background: transparent;
    color: var(--dt-text-primary);
    cursor: pointer;
    border-radius: var(--dt-radius-sm);

    &:hover {
      background: var(--dt-bg-session-hover);
    }

    svg {
      width: 20px;
      height: 20px;
    }
  }

  .mobile-title {
    font-size: var(--dt-font-size-lg);
    font-weight: var(--dt-font-weight-semibold);
    margin-left: 8px;
    color: var(--dt-text-primary);
  }
}

// A-Z 索引栏
.index-bar {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  z-index: 10;
  padding: 8px 4px;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg);
  box-shadow: var(--dt-shadow-1);

  .dark & {
    background: var(--dt-bg-card-dark);
  }
}

.index-item {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-tertiary);
  border-radius: 50%;
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:hover:not(.disabled) {
    background: var(--dt-bg-session-hover);
    color: var(--dt-brand-color);
  }

  &.active {
    background: var(--dt-brand-color);
    color: #ffffff;
  }

  &.disabled {
    opacity: 0.3;
    cursor: default;
  }
}

.list-header {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid var(--dt-border-lighter);
  flex-shrink: 0;

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }

  h2 {
    font-size: var(--dt-font-size-xl);
    font-weight: var(--dt-font-weight-semibold);
    color: var(--dt-text-primary);
    margin: 0;
  }

  .list-count {
    margin-left: 8px;
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-tertiary);
  }
}

.list-body {
  flex: 1;
  overflow: hidden;
  position: relative;
}

.list-group-header {
  padding: 8px 16px;
  background: var(--dt-bg-body);
  color: var(--dt-text-secondary);
  font-size: var(--dt-font-size-sm);
  font-weight: var(--dt-font-weight-semibold);
  position: sticky;
  top: 0;
  z-index: 5;

  .dark & {
    background: var(--dt-bg-body-dark);
  }
}

.virtual-scroll-container {
  height: 100%;
}

// 加载状态
.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: var(--dt-text-tertiary);

  .spinner {
    width: 32px;
    height: 32px;
    border: 3px solid var(--dt-border-light);
    border-top-color: var(--dt-brand-color);
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
    margin-bottom: 12px;
  }
}

// 空状态
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: var(--dt-text-quaternary);

  svg {
    width: 64px;
    height: 64px;
    margin-bottom: 16px;
    opacity: 0.3;
  }

  p {
    margin: 0;
    font-size: var(--dt-font-size-base);
  }
}

// ============================================================================
// 详情面板
// ============================================================================

.detail-panel {
  flex: 1;
  max-width: 400px;
  background: var(--dt-bg-card);
  display: flex;
  flex-direction: column;
  border-left: 1px solid var(--dt-border-divider);

  .dark & {
    background: var(--dt-bg-card-dark);
    border-left-color: var(--dt-border-dark);
  }
}

.mobile-view .detail-panel {
  position: fixed;
  inset: 0;
  transform: translateX(100%);
  transition: transform var(--dt-transition-base);
  z-index: 30;
  max-width: none;
  border-left: none;

  &.detail-open {
    transform: translateX(0);
  }
}

.mobile-detail-nav {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  border-bottom: 1px solid var(--dt-border-lighter);
  flex-shrink: 0;

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }

  .back-btn {
    display: flex;
    align-items: center;
    gap: 4px;
    border: none;
    padding: 8px 12px;
    background: transparent;
    font-size: var(--dt-font-size-base);
    font-weight: var(--dt-font-weight-medium);
    color: var(--dt-brand-color);
    cursor: pointer;
    border-radius: var(--dt-radius-sm);

    &:hover {
      background: var(--dt-brand-bg);
    }

    svg {
      width: 20px;
      height: 20px;
    }
  }
}

.empty-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--dt-text-quaternary);
  padding: 40px;

  svg {
    width: 80px;
    height: 80px;
    margin-bottom: 16px;
    opacity: 0.2;
  }

  p {
    margin: 0;
    font-size: var(--dt-font-size-base);
    color: var(--dt-text-tertiary);
  }
}

// ============================================================================
// 详情内容
// ============================================================================

.detail-content {
  flex: 1;
  overflow-y: auto;

  @extend .scrollbar-sm;
}

.detail-header {
  padding: 32px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  border-bottom: 1px solid var(--dt-border-lighter);

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }

  .detail-name {
    font-size: var(--dt-font-size-2xl);
    font-weight: var(--dt-font-weight-semibold);
    color: var(--dt-text-primary);
    margin-top: 16px;
    text-align: center;
  }

  .detail-signature {
    color: var(--dt-text-secondary);
    margin-top: 8px;
    font-size: var(--dt-font-size-sm);
    text-align: center;
  }
}

.detail-actions {
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  border-bottom: 1px solid var(--dt-border-lighter);

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }
}

.detail-section {
  padding: 20px 24px;
}

.info-item {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid var(--dt-border-lighter);
  font-size: var(--dt-font-size-base);

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }

  &:last-child {
    border-bottom: none;
  }

  .info-label {
    width: 80px;
    color: var(--dt-text-secondary);
    flex-shrink: 0;
  }

  .info-value {
    flex: 1;
    color: var(--dt-text-primary);
    word-break: break-word;
  }
}

// ============================================================================
// 响应式适配
// ============================================================================

@media (max-width: 1024px) {
  .sidebar {
    width: 220px;
  }

  .detail-panel {
    max-width: 350px;
  }
}

@media (max-width: 768px) {
  .contacts-panel {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    max-width: none;
  }

  .list-panel {
    border-right: none;
  }
}
</style>
