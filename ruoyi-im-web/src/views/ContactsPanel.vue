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
        <div class="search-box" :class="{ 'is-focused': showSearchHistory }">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <path d="M21 21l-4.35-4.35" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索联系人、群组"
            @input="handleSearch"
            @focus="onSearchFocus"
            @blur="showSearchHistory = false"
          />
          <button v-if="searchQuery" class="clear-btn" @click="clearSearch">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 6L6 18M6 6l12 12" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
        
        <!-- 搜索历史 -->
        <Transition name="fade">
          <div v-if="showSearchHistory" class="search-history">
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
        </Transition>
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
             <svg class="arrow-icon" :class="{ rotated: orgExpanded }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="9 18 15 12 9 6"></polyline>
            </svg>
            <span class="item-text font-medium">组织架构</span>
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
        <!-- 联系人详情 -->
        <ContactDetail 
            v-if="selectedType === 'friend' || selectedType === 'member'" 
            :user="selectedItem"
            @voice-call="startVoiceCall"
            @video-call="startVideoCall"
            @message="startChat"
        />
        <!-- 群组详情 - 统一风格 -->
        <div v-else-if="selectedType === 'group'" class="detail-content">
          <div class="detail-header">
            <DingtalkAvatar :name="selectedItem?.name" :size="80" :src="selectedItem?.avatar" shape="square" />
            <div class="detail-name">{{ selectedItem?.name }}</div>
            <div class="detail-meta">
              <span class="meta-item">{{ selectedItem?.memberCount || 0 }} 人</span>
              <span class="meta-divider">·</span>
              <span class="meta-item">群号: {{ selectedItem?.id }}</span>
            </div>
            <div class="detail-signature">{{ selectedItem?.description || '暂无群描述' }}</div>
          </div>
          
          <div class="detail-actions">
            <button class="action-btn primary" @click="startChat">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
              </svg>
              发消息
            </button>
            <button class="action-btn" @click="startVoiceCall">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path>
              </svg>
              语音通话
            </button>
            <button class="action-btn" @click="startVideoCall">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polygon points="23 7 16 12 23 17 23 7"></polygon>
                <rect x="1" y="5" width="15" height="14" rx="2" ry="2"></rect>
              </svg>
              视频通话
            </button>
          </div>
          
          <div class="detail-section">
            <div class="section-title">群信息</div>
            <div class="info-list">
              <div class="info-item">
                <span class="info-label">群主</span>
                <span class="info-value">{{ selectedItem?.ownerName || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">创建时间</span>
                <span class="info-value">{{ selectedItem?.createTime || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">群公告</span>
                <span class="info-value">{{ selectedItem?.notice || '暂无公告' }}</span>
              </div>
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
    // 通知父组件切换到聊天模块
    emit('switch-module', 'chat')
}
const startVoiceCall = () => ElMessage.info('语音通话开发中')
const startVideoCall = () => ElMessage.info('视频通话开发中')
// 搜索历史
const searchHistory = ref([])
const showSearchHistory = ref(false)

// 加载搜索历史
const loadSearchHistory = () => {
    const history = localStorage.getItem('contacts_search_history')
    if (history) {
        searchHistory.value = JSON.parse(history)
    }
}

// 保存搜索历史
const saveSearchHistory = (query) => {
    if (!query.trim()) return
    const history = searchHistory.value.filter(h => h !== query)
    history.unshift(query)
    if (history.length > 10) history.pop()
    searchHistory.value = history
    localStorage.setItem('contacts_search_history', JSON.stringify(history))
}

// 清除搜索历史
const clearSearchHistory = () => {
    searchHistory.value = []
    localStorage.removeItem('contacts_search_history')
}

// 使用历史搜索
const useHistorySearch = (query) => {
    searchQuery.value = query
    handleSearch()
}

const clearSearch = () => {
    searchQuery.value = ''
    searchResults.value = []
    showSearchHistory.value = false
}

// 防抖搜索
const handleSearch = useDebounceFn(async () => {
    if (!searchQuery.value.trim()) {
        searchResults.value = []
        showSearchHistory.value = false
        return
    }
    
    loading.value = true
    showSearchHistory.value = false
    
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

// 聚焦搜索框时显示历史
const onSearchFocus = () => {
    if (!searchQuery.value && searchHistory.value.length > 0) {
        showSearchHistory.value = true
    }
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
    loadSearchHistory()
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
// 钉钉风格：侧边栏宽度 200px
// ============================================================================

.sidebar {
  width: 200px; /* 钉钉标准宽度 */
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
  padding: 12px 8px; /* 左右与列表内边距对齐 */
  border-bottom: 1px solid var(--dt-border-lighter);
  position: relative;

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }
}

// 搜索历史下拉
.search-history {
  position: absolute;
  top: 100%;
  left: 8px;
  right: 8px;
  background: #ffffff;
  border-radius: var(--dt-radius-md);
  box-shadow: var(--dt-shadow-float);
  padding: 12px;
  z-index: 100;
  margin-top: 4px;

  .dark & {
    background: var(--dt-bg-card-dark);
  }
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-secondary);
}

.clear-history-btn {
  border: none;
  background: transparent;
  color: var(--dt-brand-color);
  font-size: var(--dt-font-size-sm);
  cursor: pointer;
  padding: 2px 6px;
  border-radius: var(--dt-radius-sm);

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
  background: var(--dt-bg-body);
  border-radius: var(--dt-radius-md);
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-primary);
  cursor: pointer;
  transition: all var(--dt-transition-fast);

  &:hover {
    background: var(--dt-bg-session-hover);
    color: var(--dt-brand-color);
  }

  .dark & {
    background: var(--dt-bg-body-dark);
  }
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
  height: 32px;
  background: var(--dt-bg-card-hover);
  border-radius: var(--dt-radius-md);
  padding: 0 8px;
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
    width: 16px;
    height: 16px;
    color: var(--dt-text-tertiary);
    flex-shrink: 0;
    margin-right: 6px;
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
  padding: 8px 8px; /* 减少左右内边距，为内容腾出空间 */

  @extend .scrollbar-sm;
}

.nav-divider {
  height: 1px;
  background: var(--dt-border-lighter);
  margin: 8px 8px 12px; /* 与列表内边距对齐 */

  .dark & {
    background: var(--dt-border-dark);
  }
}

.nav-item {
  display: flex;
  flex-direction: row; /* 显式声明水平排列，防止变形 */
  align-items: center;
  gap: 8px; /* 减少图标与文字间距 */
  height: 36px;
  padding: 0 8px; /* 减少内边距 */
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: background-color 0.15s ease, color 0.15s ease;
  color: var(--dt-text-primary);
  user-select: none;
  position: relative;

  &:hover {
    background: var(--dt-bg-session-hover);
  }

  &.active {
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);

    /* 钉钉风格：左侧蓝色指示条 */
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 16px;
      background: var(--dt-brand-color);
      border-radius: 0 2px 2px 0;
    }

    .icon-wrapper {
      background: var(--dt-brand-color);
    }
  }

  .item-text {
    flex: 1;
    font-size: var(--dt-font-size-base);
    font-weight: var(--dt-font-weight-normal);
    white-space: nowrap; /* 防止文字换行 */
    overflow: hidden;
    text-overflow: ellipsis; /* 超长显示省略号 */
  }

  .item-badge {
    min-width: 18px; /* 稍微缩小徽章 */
    height: 18px;
    padding: 0 5px;
    background: var(--dt-error-color);
    color: #ffffff;
    font-size: var(--dt-font-size-xs);
    font-weight: var(--dt-font-weight-medium);
    border-radius: 9px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }
}

.icon-wrapper {
  width: 30px; /* 稍微缩小图标容器 */
  height: 30px;
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
    width: 16px;
    height: 16px;
  }
}

// 组织架构样式 - 钉钉风格优化
.org-section {
  margin-top: 4px;

  .nav-item.org-root {
    height: 36px;
    padding: 0 8px; /* 与导航项保持一致 */
    color: var(--dt-text-secondary);
    font-weight: var(--dt-font-weight-medium);

    .arrow-icon {
      width: 14px;
      height: 14px;
      transition: transform 0.2s ease;
      margin-right: 4px;
      flex-shrink: 0;

      &.rotated {
        transform: rotate(90deg);
      }
    }

    &.expanded {
      color: var(--dt-text-primary);
    }
  }

  .org-list {
    margin-left: 12px; /* 减少左侧缩进 */
    padding: 4px 0;
    transform-origin: top;
  }

  // 展开/收起动画
  .slide-down-enter-active,
  .slide-down-leave-active {
    transition: all 0.2s ease;
    overflow: hidden;
  }

  .slide-down-enter-from,
  .slide-down-leave-to {
    opacity: 0;
    transform: translateY(-10px);
    max-height: 0;
  }

  .slide-down-enter-to,
  .slide-down-leave-from {
    opacity: 1;
    transform: translateY(0);
    max-height: 500px;
  }

  .org-item {
    display: flex;
    align-items: center;
    padding: 6px 8px; /* 减少内边距 */
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-secondary);
    border-radius: var(--dt-radius-sm);
    cursor: pointer;
    transition: background-color 0.15s ease, color 0.15s ease;

    &:hover {
      background: var(--dt-bg-session-hover);
      color: var(--dt-text-primary);
    }

    &.is-child {
      padding-left: 16px; /* 减少子部门缩进 */
      position: relative;

      &::before {
        content: '';
        position: absolute;
        left: 8px; /* 调整圆点位置 */
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
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .org-count {
      font-size: var(--dt-font-size-xs);
      color: var(--dt-text-quaternary);
      flex-shrink: 0;
      margin-left: 4px;
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

// A-Z 索引栏 - 钉钉风格优化
.index-bar {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1px; /* 钉钉标准间距 */
  z-index: 10;
  padding: 4px 2px; /* 钉钉标准内边距 */
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg);
  box-shadow: var(--dt-shadow-1);

  .dark & {
    background: var(--dt-bg-card-dark);
  }
}

.index-item {
  width: 18px; /* 钉钉标准索引项尺寸 */
  height: 18px;
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
// 详情面板 - 钉钉风格优化
// ============================================================================

.detail-panel {
  flex: 1;
  max-width: 360px; /* 钉钉标准详情面板宽度 */
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
  background: var(--dt-bg-body);

  @extend .scrollbar-sm;

  .dark & {
    background: var(--dt-bg-body-dark);
  }
}

.detail-header {
  padding: 40px 24px 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-lighter);

  .dark & {
    background: var(--dt-bg-card-dark);
    border-bottom-color: var(--dt-border-dark);
  }

  .detail-name {
    font-size: var(--dt-font-size-2xl);
    font-weight: var(--dt-font-weight-semibold);
    color: var(--dt-text-primary);
    margin-top: 16px;
    text-align: center;
  }

  .detail-meta {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 8px;
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-secondary);

    .meta-divider {
      color: var(--dt-text-quaternary);
    }
  }

  .detail-signature {
    color: var(--dt-text-secondary);
    margin-top: 12px;
    font-size: var(--dt-font-size-sm);
    text-align: center;
    max-width: 280px;
    line-height: 1.5;
  }
}

.detail-actions {
  padding: 20px 24px;
  display: flex;
  flex-direction: row;
  justify-content: center;
  gap: 12px;
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-lighter);

  .dark & {
    background: var(--dt-bg-card-dark);
    border-bottom-color: var(--dt-border-dark);
  }

  .action-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    padding: 12px 20px;
    border: none;
    background: var(--dt-bg-body);
    border-radius: var(--dt-radius-md);
    cursor: pointer;
    transition: all var(--dt-transition-fast);
    min-width: 72px;

    .dark & {
      background: var(--dt-bg-body-dark);
    }

    &:hover {
      background: var(--dt-brand-bg);
      color: var(--dt-brand-color);
    }

    &.primary {
      background: var(--dt-brand-color);
      color: #ffffff;

      &:hover {
        background: var(--dt-brand-color-hover);
      }
    }

    svg {
      width: 24px;
      height: 24px;
    }

    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-primary);
  }
}

.detail-section {
  padding: 20px 24px;
  background: var(--dt-bg-card);
  margin-top: 12px;

  .dark & {
    background: var(--dt-bg-card-dark);
  }

  .section-title {
    font-size: var(--dt-font-size-base);
    font-weight: var(--dt-font-weight-semibold);
    color: var(--dt-text-primary);
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--dt-border-lighter);

    .dark & {
      border-bottom-color: var(--dt-border-dark);
    }
  }
}

.info-list {
  display: flex;
  flex-direction: column;
}

.info-item {
  display: flex;
  padding: 12px 0;
  font-size: var(--dt-font-size-base);

  &:not(:last-child) {
    border-bottom: 1px solid var(--dt-border-lighter);

    .dark & {
      border-bottom-color: var(--dt-border-dark);
    }
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
// 动画效果
// ============================================================================

// 淡入淡出动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

// ============================================================================
// 响应式适配
// ============================================================================

@media (max-width: 1024px) {
  .sidebar {
    width: 180px; /* 响应式适配 */
  }

  .detail-panel {
    max-width: 320px;
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

  .detail-actions {
    flex-wrap: wrap;

    .action-btn {
      flex: 1;
      min-width: 60px;
      padding: 10px 12px;
    }
  }
}
</style>
