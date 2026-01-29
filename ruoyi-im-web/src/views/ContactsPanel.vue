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

.contacts-panel {
  display: flex;
  width: 100%;
  height: 100%;
  background: var(--dt-bg-body);
  overflow: hidden;
  position: relative;
}

// Sidebar
.sidebar {
  width: 240px;
  background: var(--dt-bg-sidebar);
  border-right: 1px solid var(--dt-border-divider);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: transform 0.3s ease;
  z-index: 20;

  &.sidebar-open {
    transform: translateX(0);
  }
}

// Mobile specific Sidebar
.mobile-view .sidebar {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  transform: translateX(-100%);
  width: 80%;
  max-width: 300px;
  box-shadow: 4px 0 12px rgba(0,0,0,0.1);
}

.mobile-overlay {
    position: absolute;
    inset: 0;
    background: rgba(0,0,0,0.5);
    z-index: 15;
}

.mobile-header {
    height: 50px;
    display: flex;
    align-items: center;
    padding: 0 16px;
    background: var(--dt-bg-body);
    border-bottom: 1px solid var(--dt-border-divider);
    
    .menu-btn {
        background: none;
        border: none;
        padding: 8px;
        margin-left: -8px;
        color: var(--dt-text-primary);
    }
    
    .mobile-title {
        font-size: 16px;
        font-weight: 600;
        margin-left: 8px;
    }
}

// Nav Items
.nav-list {
    flex: 1;
    overflow-y: auto;
    padding: 12px 8px;
}

.nav-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
    color: var(--dt-text-primary);
    
    &:hover {
        background: var(--dt-bg-sidebar-item-hover);
    }
    
    &.active {
        background: var(--dt-bg-active-dark); // Use brand active color or similar
        color: #fff;
    }
}

.icon-wrapper {
    width: 32px;
    height: 32px;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    
    &.bg-orange { background: #FF9800; }
    &.bg-blue { background: #2196F3; }
    &.bg-green { background: #4CAF50; }
    
    svg { width: 18px; height: 18px; }
}

// List Panel
.list-panel {
    flex: 1;
    display: flex;
    flex-direction: column;
    background: var(--dt-bg-body);
    position: relative;
    border-right: 1px solid var(--dt-border-divider);
}

.list-header {
    height: 60px;
    display: flex;
    align-items: center;
    padding: 0 20px;
    border-bottom: 1px solid var(--dt-border-divider);
    
    h2 { font-size: 18px; font-weight: 600; }
}

.list-group-header {
    padding: 8px 16px;
    background: var(--dt-bg-body);
    color: var(--dt-text-secondary);
    font-size: 12px;
    font-weight: 600;
    position: sticky;
    top: 0;
    z-index: 10;
}

// Detail Panel
.detail-panel {
    flex: 1; // On desktop take remaining space
    max-width: 400px; // Or flexible
    background: var(--dt-bg-card);
    display: flex;
    flex-direction: column;
    border-left: 1px solid var(--dt-border-divider);
}

.mobile-view .detail-panel {
    position: absolute;
    inset: 0;
    transform: translateX(100%);
    transition: transform 0.3s ease;
    z-index: 30;
    max-width: none;
    
    &.detail-open {
        transform: translateX(0);
    }
}

.mobile-detail-nav {
    height: 50px;
    display: flex;
    align-items: center;
    padding: 0 16px;
    border-bottom: 1px solid var(--dt-border-divider);
    
    .back-btn {
        display: flex;
        align-items: center;
        gap: 4px;
        background: none;
        border: none;
        font-size: 14px;
        color: var(--dt-brand-color);
    }
}

// Detail Content
.detail-content {
    flex: 1;
    overflow-y: auto;
    padding: 0;
}

.detail-header {
    padding: 40px 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
    border-bottom: 1px solid var(--dt-border-divider);
    
    .detail-name { font-size: 20px; font-weight: 600; margin-top: 16px; }
    .detail-signature { color: var(--dt-text-secondary); margin-top: 8px; }
}

.detail-actions {
    padding: 24px;
    display: flex;
    justify-content: center;
    border-bottom: 1px solid var(--dt-border-divider);
    
    button { width: 100%; }
}

.detail-section {
    padding: 24px;
}

.info-item {
    display: flex;
    margin-bottom: 16px;
    font-size: 14px;
    
    .info-label { width: 80px; color: var(--dt-text-secondary); }
    .info-value { flex: 1; color: var(--dt-text-primary); }
}

.empty-detail {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: var(--dt-text-quaternary);
    
    svg { width: 64px; height: 64px; margin-bottom: 16px; opacity: 0.5; }
}
</style>
