<template>
  <div class="contacts-panel" :class="{ 'mobile-view': isMobile }">
    <!-- Mobile Overlay -->
    <div
      v-if="isMobile && showMobileSidebar"
      class="mobile-overlay"
      @click="showMobileSidebar = false"
    ></div>

    <!-- 左侧导航栏 - 钉钉风格超窄侧边栏 -->
    <aside
      class="sidebar"
      :class="{ 'sidebar-open': showMobileSidebar }"
    >
      <!-- 搜索框 - 紧凑设计 -->
      <div class="search-section">
        <div class="search-trigger" :class="{ 'has-value': searchQuery }" @click="showSearchPanel = true">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <path d="M21 21l-4.35-4.35" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
      </div>

      <!-- 导航菜单 - 图标居中排列 -->
      <nav class="nav-list scrollbar-sm">
        <div
          class="nav-item"
          :class="{ active: currentNav === 'new' }"
          @click="switchNav('new')"
        >
          <div class="nav-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="8.5" cy="7" r="4"/>
              <line x1="20" y1="8" x2="20" y2="14"/>
              <line x1="23" y1="11" x2="17" y2="11"/>
            </svg>
            <span v-if="pendingCount > 0" class="nav-badge">{{ pendingCount > 99 ? '99+' : pendingCount }}</span>
          </div>
          <span class="nav-tooltip">新的朋友</span>
        </div>

        <div
          class="nav-item"
          :class="{ active: currentNav === 'friends' }"
          @click="switchNav('friends')"
        >
          <div class="nav-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
          </div>
          <span class="nav-tooltip">我的好友</span>
        </div>

        <div
          class="nav-item"
          :class="{ active: currentNav === 'groups' }"
          @click="switchNav('groups')"
        >
          <div class="nav-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="7" height="7"/>
              <rect x="14" y="3" width="7" height="7"/>
              <rect x="14" y="14" width="7" height="7"/>
              <rect x="3" y="14" width="7" height="7"/>
            </svg>
          </div>
          <span class="nav-tooltip">我的群组</span>
        </div>

        <div class="nav-divider"></div>

        <!-- 组织架构 - 树形结构 -->
        <div class="org-section">
          <div
            class="nav-item org-root"
            :class="{ expanded: orgExpanded }"
            @click="toggleOrg"
          >
            <svg class="arrow-icon" :class="{ rotated: orgExpanded }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="9 18 15 12 9 6"></polyline>
            </svg>
            <svg class="org-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
            <span class="nav-tooltip">组织架构</span>
          </div>

          <Transition name="slide-down">
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
          </Transition>
        </div>
      </nav>
    </aside>

    <!-- 搜索面板 -->
    <Transition name="slide-left">
      <div v-if="showSearchPanel" class="search-panel">
        <div class="search-panel-header">
          <div class="search-input-wrapper">
            <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <path d="M21 21l-4.35-4.35" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <input
              ref="searchInputRef"
              v-model="searchQuery"
              type="text"
              placeholder="搜索联系人、群组"
              @input="handleSearch"
              @blur="handleSearchBlur"
            />
            <button v-if="searchQuery" class="clear-btn" @click="clearSearch">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 6L6 18M6 6l12 12" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
          <button class="close-btn" @click="showSearchPanel = false">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 6L6 18M6 6l12 12" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>

        <!-- 搜索历史 -->
        <div v-if="!searchQuery && searchHistory.length > 0" class="search-history">
          <div class="history-header">
            <span>搜索历史</span>
            <button class="clear-history-btn" @click="clearSearchHistory">清除</button>
          </div>
          <div class="history-list">
            <span
              v-for="item in searchHistory"
              :key="item"
              class="history-tag"
              @mousedown.prevent="useHistorySearch(item)"
            >
              {{ item }}
            </span>
          </div>
        </div>

        <!-- 搜索结果 -->
        <div v-if="searchQuery" class="search-results">
          <template v-if="loading">
            <div class="loading-state">
              <div class="loading-spinner"></div>
              <span class="loading-text">搜索中...</span>
            </div>
          </template>
          <template v-else-if="searchResults.length > 0">
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
                  @click="selectItem(item); showSearchPanel = false"
                />
              </template>
            </VirtualList>
          </template>
          <template v-else>
            <div class="empty-state">
              <svg class="empty-icon" viewBox="0 0 48 48" fill="none" stroke="currentColor" stroke-width="1.5">
                <circle cx="19" cy="19" r="9"/>
                <path d="M39 39l-6-6" stroke-linecap="round"/>
              </svg>
              <p class="empty-text">未找到相关结果</p>
            </div>
          </template>
        </div>
      </div>
    </Transition>

    <!-- 中间列表栏 - 优化布局 -->
    <main class="list-panel" :class="{ 'has-index': showIndexBar }">
      <!-- Mobile Header -->
      <div v-if="isMobile" class="mobile-header">
        <button class="menu-btn" @click="showMobileSidebar = true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="3" y1="12" x2="21" y2="12"/>
            <line x1="3" y1="6" x2="21" y2="6"/>
            <line x1="3" y1="18" x2="21" y2="18"/>
          </svg>
        </button>
        <span class="mobile-title">{{ listTitle }}</span>
        <button class="search-btn" @click="showSearchPanel = true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <path d="M21 21l-4.35-4.35" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>

      <!-- PC Header -->
      <header v-if="!isMobile" class="list-header">
        <h2 class="list-title">{{ listTitle }}</h2>
        <div class="header-actions">
          <span v-if="listCount > 0" class="list-count">{{ listCount }}人</span>
        </div>
      </header>

      <!-- A-Z 索引栏 -->
      <div v-if="showIndexBar && !isMobile" class="index-bar">
        <div
          v-for="letter in indexLetters"
          :key="letter"
          class="index-item"
          :class="{ active: activeLetter === letter, disabled: !letterCounts[letter] }"
          @click="scrollToLetter(letter)"
        >
          {{ letter }}
        </div>
      </div>

      <!-- 列表内容 -->
      <div class="list-body">
        <!-- 加载中 -->
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner"></div>
          <span class="loading-text">加载中...</span>
        </div>

        <!-- 新的朋友 -->
        <template v-else-if="currentNav === 'new' && !searchQuery">
          <NewFriendsView ref="newFriendsRef" @update-count="updatePendingCount" />
        </template>

        <!-- 我的好友 / 群组 / 组织架构 -->
        <template v-else>
          <div v-if="virtualListData.length === 0" class="empty-state">
            <svg class="empty-icon" viewBox="0 0 48 48" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="19" cy="19" r="9"/>
              <path d="M39 39l-6-6" stroke-linecap="round"/>
            </svg>
            <p class="empty-text">暂无数据</p>
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

    <!-- 右侧详情栏 - 卡片式布局 -->
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

      <!-- 空状态 -->
      <div v-if="!selectedItemId" class="empty-detail">
        <div class="empty-detail-icon">
          <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="24" cy="24" r="18"/>
            <path d="M24 16v16M16 24h16" stroke-linecap="round"/>
          </svg>
        </div>
        <p class="empty-detail-text">选择联系人或群组查看详情</p>
      </div>

      <template v-else>
        <!-- 联系人详情 -->
        <ContactDetail
          v-if="selectedType === 'friend' || selectedType === 'member'"
          :user="selectedItem"
          @voice-call="startVoiceCall"
          @video-call="startVideoCall"
          @message="startChat"
        />

        <!-- 群组详情 - 卡片式布局 -->
        <div v-else-if="selectedType === 'group'" class="group-detail">
          <!-- 群组基本信息卡片 -->
          <div class="detail-card group-info-card">
            <div class="group-avatar-wrapper">
              <DingtalkAvatar :name="selectedItem?.name" :size="64" :src="selectedItem?.avatar" shape="square" />
            </div>
            <div class="group-basic-info">
              <h3 class="group-name">{{ selectedItem?.name }}</h3>
              <div class="group-meta">
                <span class="meta-tag">
                  <svg class="meta-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                    <circle cx="9" cy="7" r="4"/>
                  </svg>
                  {{ selectedItem?.memberCount || 0 }} 人
                </span>
                <span class="meta-tag">
                  <svg class="meta-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                    <line x1="9" y1="9" x2="15" y2="9"/>
                    <line x1="9" y1="13" x2="15" y2="13"/>
                  </svg>
                  ID: {{ selectedItem?.id }}
                </span>
              </div>
              <p class="group-description">{{ selectedItem?.description || '暂无群描述' }}</p>
            </div>
          </div>

          <!-- 操作按钮卡片 -->
          <div class="detail-card action-card">
            <div class="action-grid">
              <button class="action-item primary" @click="startChat">
                <svg class="action-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
                <span class="action-label">发消息</span>
              </button>
              <button class="action-item" @click="startVoiceCall">
                <svg class="action-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/>
                </svg>
                <span class="action-label">语音通话</span>
              </button>
              <button class="action-item" @click="startVideoCall">
                <svg class="action-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polygon points="23 7 16 12 23 17 23 7"/>
                  <rect x="1" y="5" width="15" height="14" rx="2" ry="2"/>
                </svg>
                <span class="action-label">视频通话</span>
              </button>
            </div>
          </div>

          <!-- 群详细信息卡片 -->
          <div class="detail-card info-card">
            <h4 class="card-title">群信息</h4>
            <div class="info-list">
              <div class="info-row">
                <span class="info-key">群主</span>
                <span class="info-val">{{ selectedItem?.ownerName || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-key">创建时间</span>
                <span class="info-val">{{ selectedItem?.createTime || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="info-key">群公告</span>
                <span class="info-val">{{ selectedItem?.notice || '暂无公告' }}</span>
              </div>
            </div>
          </div>
        </div>
      </template>
    </aside>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useStore } from 'vuex'
import { useWindowSize, useDebounceFn } from '@vueuse/core'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import NewFriendsView from '@/components/Contacts/NewFriendsView.vue'
import ContactItem from '@/components/Contacts/ContactItem.vue'
import ContactDetail from '@/components/Contacts/ContactDetail.vue'
import VirtualList from '@/components/Common/VirtualList.vue'
import { getFriendRequests, getGroupedFriendList, searchContacts } from '@/api/im/contact'
import { getGroups } from '@/api/im/group'
import { getOrgTree, getDepartmentMembers } from '@/api/im/organization'
import { ElMessage } from 'element-plus'

// 定义 emit，用于通知父组件切换模块
const emit = defineEmits(['switch-module'])

const store = useStore()
const { width } = useWindowSize()
const isMobile = computed(() => width.value < 768)

// State
const currentNav = ref('friends')
const searchQuery = ref('')
const showMobileSidebar = ref(false)
const showSearchPanel = ref(false)
const loading = ref(false)
const searchInputRef = ref(null)

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
    showSearchPanel.value = false

    if (nav === 'friends') loadFriends()
    else if (nav === 'groups') loadGroups()
    else if (nav === 'new') loadNewFriends()
}

const selectItem = (item) => {
    selectedItemId.value = item.id
    selectedType.value = item.type || (currentNav.value === 'groups' ? 'group' : 'friend')
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
}

// API Loaders
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
                 dept: f.department,
                 pinyin: f.pinyin || '',
                 position: f.position,
                 online: f.online
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
    emit('switch-module', 'chat')
}
const startVoiceCall = () => ElMessage.info('语音通话开发中')
const startVideoCall = () => ElMessage.info('视频通话开发中')

// 搜索历史
const searchHistory = ref([])

const loadSearchHistory = () => {
    const history = localStorage.getItem('contacts_search_history')
    if (history) {
        searchHistory.value = JSON.parse(history)
    }
}

const saveSearchHistory = (query) => {
    if (!query.trim()) return
    const history = searchHistory.value.filter(h => h !== query)
    history.unshift(query)
    if (history.length > 10) history.pop()
    searchHistory.value = history
    localStorage.setItem('contacts_search_history', JSON.stringify(history))
}

const clearSearchHistory = () => {
    searchHistory.value = []
    localStorage.removeItem('contacts_search_history')
}

const useHistorySearch = (query) => {
    searchQuery.value = query
    handleSearch()
}

const clearSearch = () => {
    searchQuery.value = ''
    searchResults.value = []
}

const handleSearchBlur = () => {
    // 延迟关闭，让点击事件先触发
    setTimeout(() => {
        if (!searchQuery.value) {
            showSearchPanel.value = false
        }
    }, 200)
}

// 防抖搜索
const handleSearch = useDebounceFn(async () => {
    if (!searchQuery.value.trim()) {
        searchResults.value = []
        return
    }

    loading.value = true

    try {
        // 优先使用后端搜索
        const res = await searchContacts({ keyword: searchQuery.value.trim() })
        if (res.code === 200 && res.data) {
            searchResults.value = [
                ...(res.data.users || []).map(u => ({ ...u, type: 'friend' })),
                ...(res.data.groups || []).map(g => ({ ...g, type: 'group' }))
            ]
        } else {
            // 降级到前端搜索
            performLocalSearch()
        }
        // 保存搜索历史
        saveSearchHistory(searchQuery.value)
    } catch (e) {
        // 后端搜索失败，使用前端搜索
        performLocalSearch()
    } finally {
        loading.value = false
    }
}, 300)

// 前端本地搜索（降级方案）
const performLocalSearch = () => {
    const q = searchQuery.value.toLowerCase().trim()
    if (!q) {
        searchResults.value = []
        return
    }

    const results = []

    // 搜索好友
    friendGroups.value.forEach(g => {
        if (g.friends) {
            g.friends.forEach(f => {
                const name = f.displayName || f.friendName || ''
                const pinyin = f.pinyin || ''
                if (name.toLowerCase().includes(q) || pinyin.toLowerCase().includes(q)) {
                    results.push({ ...f, type: 'friend' })
                }
            })
        }
    })

    // 搜索群组
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

// Watch for search panel open to focus input
watch(showSearchPanel, (val) => {
    if (val) {
        nextTick(() => {
            searchInputRef.value?.focus()
        })
    }
})

onMounted(() => {
    loadFriends()
    loadOrgTree()
    loadSearchHistory()
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// ============================================================================
// 布局容器 - 钉钉风格三栏布局
// ============================================================================

.contacts-panel {
  display: flex;
  width: 100%;
  height: 100%;
  background: var(--dt-bg-body);
  overflow: hidden;
  position: relative;
}

// ============================================================================
// 左侧导航栏 - 钉钉风格超窄侧边栏 (56px)
// ============================================================================

.sidebar {
  width: 56px;
  background: #ffffff;
  border-right: 1px solid var(--dt-border-color);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: transform var(--dt-transition-base);
  z-index: 20;

  // 搜索触发器
  .search-section {
    padding: 12px 8px;
    border-bottom: 1px solid var(--dt-border-lighter);
  }

  .search-trigger {
    width: 40px;
    height: 40px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--dt-bg-card-hover);
    border-radius: var(--dt-radius-md);
    cursor: pointer;
    transition: all var(--dt-transition-base);

    &:hover {
      background: var(--dt-brand-bg);
    }

    &.has-value {
      background: var(--dt-brand-bg);

      .search-icon {
        color: var(--dt-brand-color);
      }
    }

    .search-icon {
      width: 18px;
      height: 18px;
      color: var(--dt-text-tertiary);
      transition: color var(--dt-transition-base);
    }
  }
}

// 导航菜单 - 图标居中
.nav-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 4px;

  @extend .scrollbar-sm;
}

.nav-divider {
  height: 1px;
  background: var(--dt-border-lighter);
  margin: 4px 0;
}

.nav-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 0;
  height: 48px;
  cursor: pointer;
  transition: all var(--dt-transition-fast);
  color: var(--dt-text-secondary);
  user-select: none;

  &:hover {
    color: var(--dt-text-primary);

    .nav-icon {
      background: var(--dt-bg-card-hover);
    }

    .nav-tooltip {
      opacity: 1;
      visibility: visible;
    }
  }

  &.active {
    color: var(--dt-brand-color);

    .nav-icon {
      background: var(--dt-brand-bg);
    }
  }

  .nav-icon {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: var(--dt-radius-md);
    background: transparent;
    position: relative;
    transition: all var(--dt-transition-base);

    svg {
      width: 20px;
      height: 20px;
    }
  }

  .nav-badge {
    position: absolute;
    top: -2px;
    right: -2px;
    min-width: 16px;
    height: 16px;
    padding: 0 4px;
    background: var(--dt-error-color);
    color: #ffffff;
    font-size: 10px;
    font-weight: 500;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  // Tooltip
  .nav-tooltip {
    position: absolute;
    left: 100%;
    margin-left: 8px;
    padding: 6px 12px;
    background: var(--dt-text-primary);
    color: #ffffff;
    font-size: 12px;
    white-space: nowrap;
    border-radius: var(--dt-radius-sm);
    opacity: 0;
    visibility: hidden;
    transition: all var(--dt-transition-fast);
    pointer-events: none;
    z-index: 100;
  }
}

// 组织架构
.org-section {
  margin-top: 4px;

  .org-list {
    padding: 4px 0;
  }

  .slide-down-enter-active,
  .slide-down-leave-active {
    transition: all var(--dt-transition-base);
    overflow: hidden;
  }

  .slide-down-enter-from,
  .slide-down-leave-to {
    opacity: 0;
    max-height: 0;
  }

  .slide-down-enter-to,
  .slide-down-leave-from {
    opacity: 1;
    max-height: 300px;
  }

  .org-item {
    display: flex;
    align-items: center;
    padding: 8px 6px;
    font-size: 12px;
    color: var(--dt-text-secondary);
    cursor: pointer;
    transition: all var(--dt-transition-fast);
    margin: 0 4px;
    border-radius: var(--dt-radius-sm);

    &:hover {
      background: var(--dt-bg-card-hover);
      color: var(--dt-text-primary);
    }

    &.is-child {
      padding-left: 16px;
      position: relative;

      .org-dot {
        position: absolute;
        left: 6px;
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
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .org-count {
      font-size: 11px;
      color: var(--dt-text-tertiary);
    }
  }

  .org-root {
    height: 40px;
    flex-direction: row;
    gap: 0;

    .arrow-icon {
      width: 14px;
      height: 14px;
      transition: transform var(--dt-transition-base);
      margin-right: 2px;

      &.rotated {
        transform: rotate(90deg);
      }
    }

    .org-icon {
      width: 20px;
      height: 20px;
    }
  }
}

// ============================================================================
// 搜索面板 - 侧滑式搜索
// ============================================================================

.search-panel {
  position: absolute;
  left: 56px;
  top: 0;
  bottom: 0;
  width: 320px;
  background: #ffffff;
  border-right: 1px solid var(--dt-border-color);
  z-index: 15;
  display: flex;
  flex-direction: column;
  box-shadow: var(--dt-shadow-3);
}

.search-panel-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  border-bottom: 1px solid var(--dt-border-lighter);
}

.search-input-wrapper {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  height: 36px;
  background: var(--dt-bg-card-hover);
  border-radius: var(--dt-radius-md);
  padding: 0 12px;

  &:focus-within {
    background: #ffffff;
    box-shadow: 0 0 0 2px var(--dt-brand-light);
  }

  .search-icon {
    width: 16px;
    height: 16px;
    color: var(--dt-text-tertiary);
    margin-right: 8px;
    flex-shrink: 0;
  }

  input {
    flex: 1;
    border: none;
    background: transparent;
    font-size: 14px;
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
    width: 18px;
    height: 18px;
    border: none;
    background: rgba(0, 0, 0, 0.06);
    color: var(--dt-text-tertiary);
    cursor: pointer;
    border-radius: 50%;
    transition: all var(--dt-transition-fast);
    flex-shrink: 0;

    &:hover {
      background: rgba(0, 0, 0, 0.12);
    }

    svg {
      width: 12px;
      height: 12px;
    }
  }
}

.close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: var(--dt-text-tertiary);
  cursor: pointer;
  border-radius: var(--dt-radius-sm);
  transition: all var(--dt-transition-fast);
  flex-shrink: 0;

  &:hover {
    background: var(--dt-bg-card-hover);
    color: var(--dt-text-secondary);
  }

  svg {
    width: 18px;
    height: 18px;
  }
}

.search-history {
  padding: 12px;
  border-bottom: 1px solid var(--dt-border-lighter);
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 13px;
  color: var(--dt-text-tertiary);
}

.clear-history-btn {
  border: none;
  background: transparent;
  color: var(--dt-brand-color);
  font-size: 12px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--dt-radius-sm);
  transition: background var(--dt-transition-fast);

  &:hover {
    background: var(--dt-brand-bg);
  }
}

.history-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.history-tag {
  padding: 6px 12px;
  background: var(--dt-bg-card-hover);
  border-radius: 16px;
  font-size: 12px;
  color: var(--dt-text-secondary);
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);
  }
}

.search-results {
  flex: 1;
  overflow: hidden;
}

// ============================================================================
// 中间列表栏 - 优化布局
// ============================================================================

.list-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  position: relative;
  border-right: 1px solid var(--dt-border-color);
  margin-left: 0;
  transition: margin-left var(--dt-transition-base);

  &.has-index {
    padding-right: 40px;
  }

  // 当搜索面板打开时，添加左边距
  &:has(+ .search-panel) {
    margin-left: 320px;
  }
}

// Mobile Header
.mobile-header {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 12px;
  background: #ffffff;
  border-bottom: 1px solid var(--dt-border-lighter);
  flex-shrink: 0;

  .menu-btn,
  .search-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    border: none;
    padding: 0;
    background: transparent;
    color: var(--dt-text-secondary);
    cursor: pointer;
    border-radius: var(--dt-radius-sm);
    transition: background var(--dt-transition-fast);

    &:hover {
      background: var(--dt-bg-card-hover);
    }

    svg {
      width: 20px;
      height: 20px;
    }
  }

  .mobile-title {
    flex: 1;
    font-size: 16px;
    font-weight: 500;
    color: var(--dt-text-primary);
    text-align: center;
  }
}

// PC Header
.list-header {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  border-bottom: 1px solid var(--dt-border-lighter);
  flex-shrink: 0;

  .list-title {
    font-size: 18px;
    font-weight: 500;
    color: var(--dt-text-primary);
    margin: 0;
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .list-count {
    font-size: 14px;
    color: var(--dt-text-tertiary);
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
  gap: 1px;
  z-index: 10;
  padding: 6px 3px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: var(--dt-shadow-sm);
}

.index-item {
  width: 14px;
  height: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 500;
  color: var(--dt-text-tertiary);
  border-radius: 50%;
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:hover:not(.disabled) {
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);
  }

  &.active {
    background: var(--dt-brand-color);
    color: #ffffff;
  }

  &.disabled {
    opacity: 0.2;
    cursor: default;
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
  color: var(--dt-text-tertiary);
  font-size: 12px;
  font-weight: 500;
  position: sticky;
  top: 0;
  z-index: 5;
}

.virtual-scroll-container {
  height: 100%;
}

// 加载状态
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: var(--dt-text-tertiary);

  .loading-spinner {
    width: 28px;
    height: 28px;
    border: 2px solid var(--dt-border-lighter);
    border-top-color: var(--dt-brand-color);
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
    margin-bottom: 12px;
  }

  .loading-text {
    font-size: 13px;
  }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: var(--dt-text-tertiary);

  .empty-icon {
    width: 56px;
    height: 56px;
    margin-bottom: 16px;
    opacity: 0.3;
  }

  .empty-text {
    margin: 0;
    font-size: 13px;
  }
}

// ============================================================================
// 详情面板 - 卡片式布局
// ============================================================================

.detail-panel {
  flex: 1;
  max-width: 360px;
  background: var(--dt-bg-body);
  display: flex;
  flex-direction: column;
  border-left: 1px solid var(--dt-border-color);
}

.mobile-detail-nav {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  border-bottom: 1px solid var(--dt-border-lighter);
  flex-shrink: 0;
  background: #ffffff;

  .back-btn {
    display: flex;
    align-items: center;
    gap: 4px;
    border: none;
    padding: 8px 12px;
    background: transparent;
    font-size: 14px;
    font-weight: 500;
    color: var(--dt-brand-color);
    cursor: pointer;
    border-radius: var(--dt-radius-sm);
    transition: background var(--dt-transition-fast);

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
  padding: 40px;

  .empty-detail-icon {
    width: 72px;
    height: 72px;
    margin-bottom: 16px;
    opacity: 0.15;
    color: var(--dt-text-tertiary);
  }

  .empty-detail-text {
    margin: 0;
    font-size: 13px;
    color: var(--dt-text-tertiary);
  }
}

// 群组详情
.group-detail {
  flex: 1;
  overflow-y: auto;
  padding: 16px;

  @extend .scrollbar-sm;
}

.detail-card {
  background: #ffffff;
  border-radius: var(--dt-radius-lg);
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: var(--dt-shadow-sm);
}

// 群组信息卡片
.group-info-card {
  display: flex;
  gap: 12px;
  align-items: flex-start;

  .group-avatar-wrapper {
    flex-shrink: 0;
  }

  .group-basic-info {
    flex: 1;
    min-width: 0;
  }

  .group-name {
    font-size: 16px;
    font-weight: 500;
    color: var(--dt-text-primary);
    margin: 0 0 8px 0;
    word-break: break-word;
  }

  .group-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-bottom: 10px;
  }

  .meta-tag {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 3px 8px;
    background: var(--dt-bg-body);
    border-radius: 12px;
    font-size: 11px;
    color: var(--dt-text-secondary);

    .meta-icon {
      width: 12px;
      height: 12px;
    }
  }

  .group-description {
    font-size: 13px;
    color: var(--dt-text-secondary);
    line-height: 1.5;
    margin: 0;
    word-break: break-word;
  }
}

// 操作按钮卡片
.action-card {
  .action-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }

  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    padding: 14px 8px;
    border: none;
    background: var(--dt-bg-body);
    border-radius: var(--dt-radius-md);
    cursor: pointer;
    transition: all var(--dt-transition-fast);

    &:hover {
      background: var(--dt-brand-bg);

      .action-icon,
      .action-label {
        color: var(--dt-brand-color);
      }
    }

    &.primary {
      background: var(--dt-brand-color);

      .action-icon,
      .action-label {
        color: #ffffff;
      }

      &:hover {
        background: var(--dt-brand-hover);
      }
    }

    .action-icon {
      width: 22px;
      height: 22px;
      color: var(--dt-text-secondary);
      transition: color var(--dt-transition-fast);
    }

    .action-label {
      font-size: 11px;
      color: var(--dt-text-secondary);
      font-weight: 400;
      transition: color var(--dt-transition-fast);
    }
  }
}

// 信息卡片
.info-card {
  .card-title {
    font-size: 14px;
    font-weight: 500;
    color: var(--dt-text-primary);
    margin: 0 0 12px 0;
    padding-bottom: 10px;
    border-bottom: 1px solid var(--dt-border-lighter);
  }

  .info-list {
    display: flex;
    flex-direction: column;
  }

  .info-row {
    display: flex;
    padding: 10px 0;
    font-size: 13px;
    border-bottom: 1px solid var(--dt-border-lighter);

    &:last-child {
      border-bottom: none;
    }

    .info-key {
      width: 70px;
      color: var(--dt-text-secondary);
      flex-shrink: 0;
    }

    .info-val {
      flex: 1;
      color: var(--dt-text-primary);
      word-break: break-word;
    }
  }
}

// ============================================================================
// 动画效果
// ============================================================================

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--dt-transition-fast);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-left-enter-active,
.slide-left-leave-active {
  transition: transform var(--dt-transition-base), opacity var(--dt-transition-base);
}

.slide-left-enter-from,
.slide-left-leave-to {
  transform: translateX(-20px);
  opacity: 0;
}

// ============================================================================
// 响应式适配
// ============================================================================

@media (max-width: 1024px) {
  .detail-panel {
    max-width: 320px;
  }

  .group-info-card {
    flex-direction: column;
    align-items: center;
    text-align: center;

    .group-meta {
      justify-content: center;
    }
  }
}

@media (max-width: 768px) {
  .contacts-panel {
    flex-direction: column;
  }

  .sidebar {
    position: fixed;
    top: 0;
    left: 0;
    height: 100%;
    transform: translateX(-100%);
    width: 200px;
    max-width: 80vw;
    box-shadow: var(--dt-shadow-lg);
    z-index: 30;

    &.sidebar-open {
      transform: translateX(0);
    }
  }

  .search-panel {
    left: 0;
    width: 100%;
    z-index: 25;
  }

  .list-panel {
    border-right: none;
    margin-left: 0 !important;
  }

  .detail-panel {
    position: fixed;
    inset: 0;
    transform: translateX(100%);
    transition: transform var(--dt-transition-base);
    z-index: 35;
    max-width: none;
    border-left: none;

    &.detail-open {
      transform: translateX(0);
    }
  }

  .mobile-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.5);
    z-index: 25;
    animation: fadeIn var(--dt-transition-base);
  }

  .action-card .action-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .group-detail {
    padding: 12px;
  }

  .detail-card {
    padding: 14px;
  }
}

@media (max-width: 480px) {
  .action-card .action-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 6px;
  }

  .action-item {
    padding: 12px 6px;

    .action-icon {
      width: 20px;
      height: 20px;
    }

    .action-label {
      font-size: 10px;
    }
  }

  .group-info-card {
    padding: 14px;
    gap: 10px;
  }

  .group-name {
    font-size: 15px;
  }
}
</style>
