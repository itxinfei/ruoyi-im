<template>
  <div class="contacts-panel">
    <!-- 左侧导航栏 -->
    <aside class="sidebar">
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
            placeholder="搜索"
            @input="handleSearch"
          />
          <button
            v-if="searchQuery"
            class="clear-btn"
            @click="clearSearch"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 6L6 18M6 6l12 12" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- 导航菜单 -->
      <nav class="nav-list">
        <div
          class="nav-item"
          :class="{ active: currentNav === 'new' }"
          @click="switchNav('new')"
        >
          <svg class="item-icon" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.5"/>
            <path d="M12 8v8M8 12h8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <span class="item-text">新的朋友</span>
          <span v-if="pendingCount > 0" class="item-badge">{{ pendingCount > 99 ? '99+' : pendingCount }}</span>
        </div>

        <div
          class="nav-item"
          :class="{ active: currentNav === 'friends' }"
          @click="switchNav('friends')"
        >
          <svg class="item-icon" viewBox="0 0 24 24" fill="none">
            <circle cx="8" cy="8" r="3.5" stroke="currentColor" stroke-width="1.5"/>
            <path d="M17.5 16.5c0-3-3.5-4.5-6.5-4.5h-5c-3 0-6.5 1.5-6.5 4.5v1" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <path d="M22 19.5c0-2.5-2.5-3.5-5-3.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <span class="item-text">我的好友</span>
          <span v-if="friendTotal > 0" class="item-count">{{ friendTotal }}</span>
        </div>

        <div
          class="nav-item"
          :class="{ active: currentNav === 'groups' }"
          @click="switchNav('groups')"
        >
          <svg class="item-icon" viewBox="0 0 24 24" fill="none">
            <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <circle cx="9" cy="7" r="3.5" stroke="currentColor" stroke-width="1.5"/>
            <path d="M23 21v-2a4 4 0 00-3-3.87" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            <path d="M16 3.13a4 4 0 010 7.75" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <span class="item-text">我的群组</span>
          <span v-if="groupTotal > 0" class="item-count">{{ groupTotal }}</span>
        </div>

        <div class="nav-divider"></div>

        <!-- 组织架构 -->
        <div class="org-section">
          <div
            class="nav-item org-root"
            :class="{ expanded: orgExpanded }"
            @click="toggleOrg"
          >
            <svg class="arrow-icon" viewBox="0 0 24 24" fill="none">
              <path d="M10 17l5-5-5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span class="item-text">组织架构</span>
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
      <!-- A-Z 索引栏 (仅组织架构显示) -->
      <div v-if="showIndexBar" class="index-bar">
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

      <!-- 列表头部 -->
      <header class="list-header">
        <h2 class="list-title">{{ listTitle }}</h2>
        <span v-if="listCount > 0" class="list-count">{{ listCount }}</span>
      </header>

      <!-- 列表内容 -->
      <div class="list-body" ref="listBodyRef" @scroll="handleListScroll">
        <!-- 加载中 -->
        <div v-if="loading" class="loading">
          <div class="spinner"></div>
          <span>加载中...</span>
        </div>

        <!-- 搜索结果 -->
        <div v-else-if="searchQuery && searchResults.length > 0" class="search-list">
          <div
            v-for="item in searchResults"
            :key="item.id"
            class="list-item"
            :class="{ active: selectedItemId === item.id && selectedType === item.type }"
            @click="selectItem(item)"
          >
            <DingtalkAvatar
              :name="item.name"
              :size="40"
              :src="item.avatar"
              :shape="item.type === 'group' ? 'square' : 'circle'"
            />
            <div class="item-info">
              <div class="item-name">{{ item.name }}</div>
              <div class="item-desc">{{ item.dept || item.description }}</div>
            </div>
          </div>
        </div>

        <!-- 新的朋友 -->
        <template v-else-if="currentNav === 'new' && !searchQuery">
          <NewFriendsView ref="newFriendsRef" @update-count="updatePendingCount" />
        </template>

        <!-- 我的好友 -->
        <template v-else-if="currentNav === 'friends' && !searchQuery">
          <div v-if="friendGroups.length === 0" class="empty">
            <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="19" cy="19" r="9"/>
              <path d="M34 10l-6 6m0 0l-6-6m6 6l-6 6m6-6h.01" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <p>暂无好友</p>
          </div>
          <div v-else class="friend-list">
            <div
              v-for="group in friendGroups"
              :key="group.name || 'default'"
              class="friend-group"
            >
              <div class="group-header" @click="toggleGroup(group.name || 'default')">
                <svg
                  class="group-arrow"
                  :class="{ collapsed: collapsedGroups.has(group.name || 'default') }"
                  viewBox="0 0 24 24"
                  fill="none"
                >
                  <path d="M15 19l-7-7 7-7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span class="group-name">{{ group.name || '未分组' }}</span>
                <span class="group-count">{{ group.friends.length }}</span>
              </div>
              <div v-show="!collapsedGroups.has(group.name || 'default')" class="group-members">
                <div
                  v-for="friend in group.friends"
                  :key="friend.id"
                  class="list-item"
                  :class="{ active: selectedItemId === friend.id && selectedType === 'friend' }"
                  @click="selectItem({ ...friend, type: 'friend' })"
                >
                  <DingtalkAvatar :name="friend.displayName" :size="40" :src="friend.avatar" />
                  <div class="item-info">
                    <div class="item-name">{{ friend.displayName }}</div>
                    <div class="item-desc">{{ friend.dept }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- 我的群组 -->
        <template v-else-if="currentNav === 'groups' && !searchQuery">
          <div v-if="groupList.length === 0" class="empty">
            <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2" stroke-linecap="round"/>
              <circle cx="9" cy="7" r="3.5"/>
              <path d="M23 21v-2a4 4 0 00-3-3.87" stroke-linecap="round"/>
              <path d="M16 3.13a4 4 0 010 7.75" stroke-linecap="round"/>
            </svg>
            <p>暂无群组</p>
          </div>
          <div v-else class="group-list">
            <div
              v-for="group in groupList"
              :key="group.id"
              class="list-item"
              :class="{ active: selectedItemId === group.id && selectedType === 'group' }"
              @click="selectItem({ ...group, type: 'group' })"
            >
              <DingtalkAvatar :name="group.name" :size="40" :src="group.avatar" shape="square" />
              <div class="item-info">
                <div class="item-name">{{ group.name }}</div>
                <div class="item-desc">{{ group.memberCount || 0 }}人</div>
              </div>
            </div>
          </div>
        </template>

        <!-- 组织架构成员 -->
        <template v-else-if="currentNav === 'org' && !searchQuery">
          <div v-if="orgMembers.length === 0" class="empty">
            <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M24 4L4 14v20l20 10 20-10V14L24 4z"/>
            </svg>
            <p>暂无成员</p>
          </div>
          <div v-else class="org-members">
            <!-- 按首字母分组成员 -->
            <div
              v-for="group in groupedOrgMembers"
              :key="group.letter"
              class="member-group"
              :ref="el => { if (el && group.letter) letterRefs[group.letter] = el }"
            >
              <div class="letter-header">{{ group.letter }}</div>
              <div
                v-for="member in group.members"
                :key="member.id"
                class="list-item"
                :class="{ active: selectedItemId === member.id && selectedType === 'member' }"
                @click="selectItem({ ...member, type: 'member' })"
              >
                <DingtalkAvatar :name="member.name" :size="40" :src="member.avatar" />
                <div class="item-info">
                  <div class="item-name">{{ member.name }}</div>
                  <div class="item-desc">{{ member.position || member.dept }}</div>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- 空状态 -->
        <div v-else class="empty">
          <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="19" cy="19" r="9"/>
            <path d="M39 39l-6-6" stroke-linecap="round"/>
          </svg>
          <p>未找到相关结果</p>
        </div>
      </div>
    </main>

    <!-- 右侧详情栏 -->
    <aside class="detail-panel">
      <div v-if="!selectedItemId" class="empty-detail">
        <svg viewBox="0 0 48 48" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="24" cy="24" r="18"/>
          <path d="M24 16v16M16 24h16" stroke-linecap="round"/>
        </svg>
        <p>选择联系人或群组查看详情</p>
      </div>

      <template v-else>
        <!-- 好友/成员详情 -->
        <div v-if="selectedType === 'friend' || selectedType === 'member'" class="detail-content">
          <div class="detail-header">
            <div class="avatar-container">
              <DingtalkAvatar
                :name="selectedItem?.displayName || selectedItem?.name"
                :size="80"
                :src="selectedItem?.avatar"
              />
              <div v-if="selectedItem?.isOnline" class="online-tag">在线</div>
            </div>
            <div class="detail-name">{{ selectedItem?.displayName || selectedItem?.name }}</div>
            <div class="detail-signature">{{ selectedItem?.signature || '这个人很懒，什么都没写' }}</div>
          </div>

          <div class="detail-section">
            <h3 class="section-title">基本信息</h3>
            <div class="info-list">
              <div class="info-item">
                <span class="info-label">部门</span>
                <span class="info-value">{{ selectedItem?.dept || '无' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">职位</span>
                <span class="info-value">{{ selectedItem?.position || '无' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">手机</span>
                <span class="info-value">{{ selectedItem?.phone || '无' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">邮箱</span>
                <span class="info-value">{{ selectedItem?.email || '无' }}</span>
              </div>
            </div>
          </div>

          <div class="detail-actions">
            <button class="btn btn-primary" @click="startChat">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              发消息
            </button>
            <button class="btn btn-secondary" @click="startVoiceCall">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07 19.5 19.5 0 01-6-6 19.79 19.79 0 01-3.07-8.67A2 2 0 014.11 2h3a2 2 0 012 1.72 12.84 12.84 0 00.7 2.81 2 2 0 01-.45 2.11L8.09 9.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45 12.84 12.84 0 002.81.7A2 2 0 0122 16.92z" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              语音通话
            </button>
            <button class="btn btn-secondary" @click="startVideoCall">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              视频通话
            </button>
          </div>
        </div>

        <!-- 群组详情 -->
        <div v-else-if="selectedType === 'group'" class="detail-content">
          <div class="detail-header">
            <div class="avatar-container">
              <DingtalkAvatar
                :name="selectedItem?.name"
                :size="80"
                :src="selectedItem?.avatar"
                shape="square"
              />
            </div>
            <div class="detail-name">{{ selectedItem?.name }}</div>
            <div class="detail-signature">{{ selectedItem?.description || '暂无群描述' }}</div>
          </div>

          <div class="detail-section">
            <h3 class="section-title">群信息</h3>
            <div class="info-list">
              <div class="info-item">
                <span class="info-label">群主</span>
                <span class="info-value">{{ selectedItem?.ownerName || '无' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">群成员</span>
                <span class="info-value">{{ selectedItem?.memberCount || 0 }}人</span>
              </div>
              <div class="info-item">
                <span class="info-label">创建时间</span>
                <span class="info-value">{{ formatDate(selectedItem?.createTime) }}</span>
              </div>
            </div>
          </div>

          <div class="detail-actions">
            <button class="btn btn-primary" @click="startChat">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              发消息
            </button>
            <button class="btn btn-secondary" @click="showGroupMembers">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2" stroke-linecap="round"/>
                <circle cx="9" cy="7" r="4"/>
                <path d="M23 21v-2a4 4 0 00-3-3.87" stroke-linecap="round"/>
                <path d="M16 3.13a4 4 0 010 7.75" stroke-linecap="round"/>
              </svg>
              查看成员
            </button>
          </div>
        </div>
      </template>
    </aside>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import NewFriendsView from '@/components/Contacts/NewFriendsView.vue'
import { getFriendRequests, getGroupedFriendList } from '@/api/im/contact'
import { getGroups } from '@/api/im/group'
import { ElMessage } from 'element-plus'

const store = useStore()

const currentNav = ref('friends')
const searchQuery = ref('')
const pendingCount = ref(0)
const friendTotal = ref(0)
const groupTotal = ref(0)
const friendGroups = ref([])
const groupList = ref([])
const orgMembers = ref([])
const searchResults = ref([])
const loading = ref(false)

const orgExpanded = ref(false)
const selectedDeptId = ref(null)
const selectedItemId = ref(null)
const selectedType = ref(null)
const collapsedGroups = ref(new Set())

const flatDepts = ref([])

// ========== A-Z 索引相关状态 ==========
const indexLetters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ#'.split('')
const activeLetter = ref('')
const letterCounts = ref({})
const letterRefs = ref({})
const listBodyRef = ref(null)

// 是否显示索引栏（仅组织架构且有成员时显示）
const showIndexBar = computed(() => {
  return currentNav.value === 'org' && orgMembers.value.length > 0
})

// 按首字母分组的组织成员
const groupedOrgMembers = computed(() => {
  const groups = {}
  const counts = {}

  // 初始化所有字母的计数
  indexLetters.forEach(letter => {
    groups[letter] = []
    counts[letter] = 0
  })

  orgMembers.value.forEach(member => {
    // 获取拼音首字母（简化处理：取第一个字符的大写）
    let letter = '#'
    if (member.name && member.name.length > 0) {
      const firstChar = member.name.charAt(0).toUpperCase()
      // 判断是否为 A-Z
      if (/[A-Z]/.test(firstChar)) {
        letter = firstChar
      }
    }

    if (!groups[letter]) {
      groups[letter] = []
    }
    groups[letter].push(member)
    counts[letter] = (counts[letter] || 0) + 1
  })

  letterCounts.value = counts

  // 转换为数组并排序
  return Object.entries(groups)
    .filter(([_, members]) => members.length > 0)
    .map(([letter, members]) => ({ letter, members }))
    .sort((a, b) => {
      // # 排在最后，其他按字母顺序
      if (a.letter === '#') return 1
      if (b.letter === '#') return -1
      return a.letter.localeCompare(b.letter)
    })
})

// 列表标题和数量
const listTitle = computed(() => {
  if (searchQuery.value) return '搜索结果'
  if (currentNav.value === 'new') return '新的朋友'
  if (currentNav.value === 'friends') return '我的好友'
  if (currentNav.value === 'groups') return '我的群组'
  if (currentNav.value === 'org') return '组织架构'
  return ''
})

const listCount = computed(() => {
  if (searchQuery.value) return searchResults.value.length
  if (currentNav.value === 'friends') return friendTotal.value
  if (currentNav.value === 'groups') return groupTotal.value
  return 0
})

const selectedItem = computed(() => {
  return null
})

// 切换导航
const switchNav = (nav) => {
  currentNav.value = nav
  selectedItemId.value = null
  selectedType.value = null
  searchQuery.value = ''

  if (nav === 'new') loadNewFriends()
  else if (nav === 'friends') loadFriends()
  else if (nav === 'groups') loadGroups()
}

// 加载新的朋友
const loadNewFriends = async () => {
  try {
    const res = await getFriendRequests()
    pendingCount.value = res.data?.filter(r => r.status === 'PENDING').length || 0
  } catch (error) {
    console.error('加载新朋友失败', error)
  }
}

// 加载好友列表
const loadFriends = async () => {
  loading.value = true
  try {
    const res = await getGroupedFriendList()
    friendGroups.value = res.data.map(g => ({
      name: g.groupName,
      friends: (g.friends || []).map(f => ({
        id: f.friendId,
        displayName: f.remark || f.friendName,
        avatar: f.friendAvatar,
        dept: f.dept
      }))
    }))
    friendTotal.value = friendGroups.value.reduce((sum, g) => sum + g.friends.length, 0)
  } catch (error) {
    console.error('加载好友失败', error)
  } finally {
    loading.value = false
  }
}

// 加载群组列表
const loadGroups = async () => {
  loading.value = true
  try {
    const res = await getGroups()
    groupList.value = res.data || []
    groupTotal.value = groupList.value.length
  } catch (error) {
    console.error('加载群组失败', error)
  } finally {
    loading.value = false
  }
}

// 切换分组展开/收起
const toggleGroup = (name) => {
  const key = name || 'default'
  if (collapsedGroups.value.has(key)) {
    collapsedGroups.value.delete(key)
  } else {
    collapsedGroups.value.add(key)
  }
}

// 切换组织架构展开/收起
const toggleOrg = () => {
  orgExpanded.value = !orgExpanded.value
}

// 选择部门
const selectDept = (dept) => {
  selectedDeptId.value = dept.id
  currentNav.value = 'org'
  loadOrgMembers(dept.id)
}

// 加载组织架构成员
const loadOrgMembers = async (deptId) => {
  loading.value = true
  try {
    orgMembers.value = []
  } catch (error) {
    console.error('加载组织架构成员失败', error)
  } finally {
    loading.value = false
  }
}

// 选择项目
const selectItem = (item) => {
  selectedItemId.value = item.id
  selectedType.value = item.type
}

// 搜索
const handleSearch = async () => {
  if (!searchQuery.value.trim()) {
    searchResults.value = []
    return
  }
  searchResults.value = []
}

// 清空搜索
const clearSearch = () => {
  searchQuery.value = ''
  searchResults.value = []
}

// ========== A-Z 索引相关方法 ==========

// 滚动到指定字母
const scrollToLetter = (letter) => {
  if (!letter || !letterCounts.value[letter]) return

  const ref = letterRefs.value[letter]
  if (ref) {
    ref.scrollIntoView({ behavior: 'smooth', block: 'start' })
    activeLetter.value = letter

    // 1秒后清除高亮
    setTimeout(() => {
      activeLetter.value = ''
    }, 1000)
  }
}

// 处理列表滚动，更新当前可见字母
const handleListScroll = () => {
  if (!listBodyRef.value || !showIndexBar.value) return

  const container = listBodyRef.value
  const scrollTop = container.scrollTop
  const containerHeight = container.clientHeight

  // 遍历所有字母分组，找到当前可见的字母
  for (const group of groupedOrgMembers.value) {
    const ref = letterRefs.value[group.letter]
    if (ref) {
      const rect = ref.getBoundingClientRect()
      const containerRect = container.getBoundingClientRect()

      // 如果字母标题在可见区域中间位置
      if (rect.top >= containerRect.top + 40 && rect.top <= containerRect.top + containerHeight / 2) {
        activeLetter.value = group.letter
        break
      }
    }
  }
}

// 更新新朋友数量
const updatePendingCount = (count) => {
  pendingCount.value = count
}

// 开始聊天
const startChat = () => {
  if (!selectedItemId.value) return
  const isGroup = selectedType.value === 'group'
  store.dispatch('im/session/createAndSwitchSession', {
    targetId: selectedItemId.value,
    type: isGroup ? 'GROUP' : 'PRIVATE'
  })
}

// 语音通话
const startVoiceCall = () => {
  ElMessage.info('语音通话功能开发中')
}

// 视频通话
const startVideoCall = () => {
  ElMessage.info('视频通话功能开发中')
}

// 显示群成员
const showGroupMembers = () => {
  ElMessage.info('查看群成员功能开发中')
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '无'
  return new Date(date).toLocaleDateString('zh-CN')
}

// 初始化
loadFriends()
loadGroups()
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// ============================================================================
// 根容器
// ============================================================================
.contacts-panel {
  display: flex;
  width: 100%;
  height: 100%;
  background: var(--dt-bg-body);
  overflow: hidden;
}

.dark .contacts-panel {
  background: var(--dt-bg-body-dark);
}

// ============================================================================
// 左侧导航栏 (200px)
// ============================================================================
.sidebar {
  width: 200px;
  flex-shrink: 0;
  min-width: 0;
  background: var(--dt-bg-card);
  border-right: 1px solid var(--dt-border-color);
  display: flex;
  flex-direction: column;
}

.dark .sidebar {
  background: var(--dt-bg-card-dark);
  border-right-color: var(--dt-border-dark);
}

.search-section {
  padding: 16px 12px;
  border-bottom: 1px solid var(--dt-border-color);
  flex-shrink: 0;
}

.dark .search-section {
  border-bottom-color: var(--dt-border-dark);
}

.search-box {
  display: flex;
  align-items: center;
  height: 36px;
  padding: 0 12px;
  background: var(--dt-bg-body);
  border-radius: 6px;

  &:focus-within {
    background: var(--dt-bg-card);
    box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.1);
  }
}

.dark .search-box {
  background: var(--dt-bg-hover-dark);
}

.dark .search-box:focus-within {
  background: var(--dt-bg-card-dark);
  box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.2);
}

.search-icon {
  width: 14px;
  height: 14px;
  color: var(--dt-text-quaternary);
  margin-right: 8px;
  flex-shrink: 0;
}

.search-box input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 13px;
  color: var(--dt-text-primary);
}

.search-box input::placeholder {
  color: var(--dt-text-quaternary);
}

.dark .search-box input {
  color: var(--dt-text-primary-dark);
}

.dark .search-box input::placeholder {
  color: var(--dt-text-quaternary-dark);
}

.clear-btn {
  width: 14px;
  height: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: var(--dt-text-quaternary);
  cursor: pointer;
  flex-shrink: 0;
}

.clear-btn svg {
  width: 10px;
  height: 10px;
}

.clear-btn:hover {
  color: var(--dt-text-tertiary);
}

.nav-list {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 8px 0;
}

.nav-item {
  display: flex;
  align-items: center;
  height: 40px;
  padding: 0 16px;
  margin: 1px 8px;
  border-radius: 6px;
  cursor: pointer;
  color: var(--dt-text-secondary);
  font-size: 14px;
  transition: all 0.2s;
  white-space: nowrap;
  overflow: hidden;
}

.nav-item:hover {
  background: var(--dt-bg-body);
  color: var(--dt-text-primary);
}

.nav-item.active {
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);
}

.dark .nav-item {
  color: var(--dt-text-secondary-dark);
}

.dark .nav-item:hover {
  background: var(--dt-bg-hover-dark);
  color: var(--dt-text-primary-dark);
}

.dark .nav-item.active {
  background: rgba(22, 119, 255, 0.15);
  color: var(--dt-brand-color);
}

.item-icon {
  width: 18px;
  height: 18px;
  color: currentColor;
  margin-right: 10px;
  flex-shrink: 0;
}

.item-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-badge {
  min-width: 18px;
  height: 18px;
  padding: 0 6px;
  background: var(--dt-error-color);
  color: #fff;
  border-radius: 9px;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  flex-shrink: 0;
}

.item-count {
  font-size: 12px;
  color: var(--dt-text-quaternary);
  flex-shrink: 0;
}

.nav-divider {
  height: 1px;
  margin: 8px 12px;
  background: var(--dt-border-color);
}

.dark .nav-divider {
  background: var(--dt-border-dark);
}

.org-section {
  margin-top: 4px;
}

.org-root .arrow-icon {
  width: 14px;
  height: 14px;
  color: var(--dt-text-quaternary);
  margin-right: 8px;
  transition: transform 0.2s;
  transform: rotate(-90deg);
  flex-shrink: 0;
}

.dark .org-root .arrow-icon {
  color: var(--dt-text-quaternary-dark);
}

.org-root.expanded .arrow-icon {
  transform: rotate(0deg);
}

.org-list {
  padding: 4px 0;
}

.org-item {
  display: flex;
  align-items: center;
  height: 36px;
  padding: 0 16px 0 44px;
  margin: 1px 8px;
  border-radius: 6px;
  cursor: pointer;
  color: var(--dt-text-secondary);
  font-size: 13px;
  transition: all 0.2s;
  white-space: nowrap;
  overflow: hidden;
}

.org-item:hover {
  background: var(--dt-bg-body);
  color: var(--dt-text-primary);
}

.org-item.active {
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);
}

.dark .org-item {
  color: var(--dt-text-secondary-dark);
}

.dark .org-item:hover {
  background: var(--dt-bg-hover-dark);
  color: var(--dt-text-primary-dark);
}

.dark .org-item.active {
  background: rgba(22, 119, 255, 0.15);
  color: var(--dt-brand-color);
}

.org-dot {
  width: 4px;
  height: 4px;
  background: var(--dt-border-color);
  border-radius: 50%;
  margin-right: 8px;
  flex-shrink: 0;
}

.org-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
}

.org-count {
  font-size: 12px;
  color: var(--dt-text-quaternary);
  flex-shrink: 0;
}

// ============================================================================
// 中间列表栏 (300px)
// ============================================================================
.list-panel {
  width: 300px;
  flex-shrink: 0;
  min-width: 0;
  background: var(--dt-bg-card);
  border-right: 1px solid var(--dt-border-color);
  display: flex;
  flex-direction: column;
}

.dark .list-panel {
  background: var(--dt-bg-card-dark);
  border-right-color: var(--dt-border-dark);
}

.list-header {
  display: flex;
  align-items: center;
  height: 48px;
  padding: 0 16px;
  border-bottom: 1px solid var(--dt-border-color);
  flex-shrink: 0;
}

.dark .list-header {
  border-bottom-color: var(--dt-border-dark);
}

.list-title {
  flex: 1;
  font-size: 15px;
  font-weight: 500;
  color: var(--dt-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dark .list-title {
  color: var(--dt-text-primary-dark);
}

.list-count {
  font-size: 13px;
  color: var(--dt-text-quaternary);
  flex-shrink: 0;
}

.list-body {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

.list-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.list-item:hover {
  background: var(--dt-bg-body);
}

.list-item.active {
  background: var(--dt-brand-bg);
}

.dark .list-item:hover {
  background: var(--dt-bg-hover-dark);
}

.dark .list-item.active {
  background: rgba(22, 119, 255, 0.15);
}

.item-info {
  flex: 1;
  margin-left: 12px;
  min-width: 0;
  overflow: hidden;
}

.item-name {
  font-size: 14px;
  color: var(--dt-text-primary);
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dark .item-name {
  color: var(--dt-text-primary-dark);
}

.item-desc {
  font-size: 12px;
  color: var(--dt-text-quaternary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.friend-list,
.group-list,
.org-members {
  padding: 4px 0;
}

.friend-group {
  margin-bottom: 4px;
}

.group-header {
  display: flex;
  align-items: center;
  height: 36px;
  padding: 0 16px;
  cursor: pointer;
  user-select: none;
  white-space: nowrap;
  overflow: hidden;
}

.group-header:hover {
  background: var(--dt-bg-body);
}

.dark .group-header:hover {
  background: var(--dt-bg-hover-dark);
}

.group-arrow {
  width: 14px;
  height: 14px;
  color: var(--dt-text-quaternary);
  margin-right: 8px;
  transition: transform 0.2s;
  flex-shrink: 0;
}

.group-arrow.collapsed {
  transform: rotate(-90deg);
}

.dark .group-arrow {
  color: var(--dt-text-quaternary-dark);
}

.group-name {
  flex: 1;
  font-size: 13px;
  color: var(--dt-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
}

.dark .group-name {
  color: var(--dt-text-secondary-dark);
}

.group-count {
  font-size: 12px;
  color: var(--dt-text-quaternary);
  flex-shrink: 0;
}

.group-members {
  padding: 0 4px;
}

.loading,
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--dt-text-quaternary);
}

.loading svg,
.empty svg {
  width: 48px;
  height: 48px;
  margin-bottom: 12px;
  opacity: 0.3;
}

.loading p,
.empty p {
  font-size: 13px;
  margin: 0;
}

.spinner {
  width: 24px;
  height: 24px;
  border: 2px solid var(--dt-border-color);
  border-top-color: var(--dt-brand-color);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.dark .spinner {
  border-color: var(--dt-border-dark);
  border-top-color: var(--dt-brand-color);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

// ============================================================================
// 右侧详情栏 (flex: 1)
// ============================================================================
.detail-panel {
  flex: 1;
  background: var(--dt-bg-card);
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.dark .detail-panel {
  background: var(--dt-bg-card-dark);
}

.empty-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--dt-text-quaternary);
}

.empty-detail svg {
  width: 64px;
  height: 64px;
  margin-bottom: 16px;
  opacity: 0.2;
}

.empty-detail p {
  font-size: 14px;
  margin: 0;
}

.detail-content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
}

.detail-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48px 24px 32px;
  border-bottom: 1px solid var(--dt-border-color);
  flex-shrink: 0;
}

.dark .detail-header {
  border-bottom-color: var(--dt-border-dark);
}

.avatar-container {
  position: relative;
  margin-bottom: 16px;
  flex-shrink: 0;
}

.online-tag {
  position: absolute;
  bottom: 4px;
  right: 4px;
  padding: 2px 8px;
  background: var(--dt-success-color);
  color: #fff;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 500;
}

.detail-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin-bottom: 8px;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}

.dark .detail-name {
  color: var(--dt-text-primary-dark);
}

.detail-signature {
  font-size: 13px;
  color: var(--dt-text-quaternary);
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}

.detail-section {
  padding: 24px;
  border-bottom: 1px solid var(--dt-border-color);
  flex-shrink: 0;
}

.dark .detail-section {
  border-bottom-color: var(--dt-border-dark);
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-primary);
  margin-bottom: 16px;
}

.dark .section-title {
  color: var(--dt-text-primary-dark);
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-label {
  width: 60px;
  font-size: 13px;
  color: var(--dt-text-secondary);
  flex-shrink: 0;
}

.dark .info-label {
  color: var(--dt-text-secondary-dark);
}

.info-value {
  flex: 1;
  font-size: 13px;
  color: var(--dt-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dark .info-value {
  color: var(--dt-text-primary-dark);
}

.detail-actions {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex-shrink: 0;
}

.btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 40px;
  padding: 0 20px;
  border-radius: 6px;
  border: none;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.btn svg {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.btn-primary {
  background: var(--dt-brand-color);
  color: #fff;
}

.btn-primary:hover {
  background: var(--dt-brand-hover);
}

.btn-secondary {
  background: var(--dt-bg-body);
  color: var(--dt-text-primary);
}

.btn-secondary:hover {
  background: var(--dt-bg-hover);
}

.dark .btn-secondary {
  background: var(--dt-bg-hover-dark);
  color: var(--dt-text-primary-dark);
}

.dark .btn-secondary:hover {
  background: var(--dt-bg-hover-dark);
}

// ============================================================================
// A-Z 索引栏样式
// ============================================================================
.list-panel.has-index {
  padding-right: 40px; // 为索引栏留出空间
}

.index-bar {
  position: absolute;
  right: 8px;
  top: 50px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  z-index: 10;
}

.index-item {
  width: 24px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 500;
  color: var(--dt-text-tertiary);
  cursor: pointer;
  border-radius: 4px;
  transition: all var(--dt-transition-fast);
  user-select: none;

  &:hover {
    background: var(--dt-bg-hover);
    color: var(--dt-brand-color);
  }

  &.active {
    background: var(--dt-brand-color);
    color: #fff;
    transform: scale(1.1);
  }

  &.disabled {
    opacity: 0.3;
    cursor: not-allowed;

    &:hover {
      background: transparent;
      color: var(--dt-text-quaternary);
    }
  }
}

// 字母分组样式
.member-group {
  margin-bottom: 8px;
}

.letter-header {
  padding: 8px 16px;
  background: var(--dt-bg-body);
  font-size: 12px;
  font-weight: 600;
  color: var(--dt-text-secondary);
  position: sticky;
  top: 0;
  z-index: 1;
}
</style>