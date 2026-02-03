<template>
  <div class="contacts-panel" :class="{ 'mobile-view': isMobile }">
    <!-- Mobile Overlay -->
    <Transition name="fade">
      <div
        v-if="isMobile && showMobileSidebar"
        class="mobile-overlay"
        @click="showMobileSidebar = false"
      ></div>
    </Transition>

    <!-- 左侧导航栏 -->
    <aside
      class="sidebar"
      :class="{ 'sidebar-open': showMobileSidebar }"
    >
      <!-- 顶部标题区 -->
      <div class="sidebar-header">
        <div class="header-content">
          <h2 class="sidebar-title">通讯录</h2>
          <el-tooltip content="添加朋友/群组" placement="bottom">
            <el-button circle size="small" :icon="Plus" @click="showAddMenu = true" />
          </el-tooltip>
        </div>
        <div class="search-box" @click="showSearchPanel = true">
          <el-icon class="search-icon"><Search /></el-icon>
          <span class="placeholder">搜索联系人、群组...</span>
        </div>
      </div>

      <!-- 导航菜单 -->
      <nav class="nav-list scrollbar-custom">
        <div class="nav-group">
          <div class="nav-group-title">常用</div>
          <div
            class="nav-item"
            :class="{ active: currentNav === 'new' }"
            @click="switchNav('new')"
          >
            <div class="nav-icon-wrapper bg-gradient-orange">
              <el-icon><User /></el-icon>
            </div>
            <span class="nav-text">新的朋友</span>
            <span v-if="pendingCount > 0" class="nav-badge">{{ pendingCount > 99 ? '99+' : pendingCount }}</span>
          </div>

          <div
            class="nav-item"
            :class="{ active: currentNav === 'friends' }"
            @click="switchNav('friends')"
          >
            <div class="nav-icon-wrapper bg-gradient-blue">
              <el-icon><Avatar /></el-icon>
            </div>
            <span class="nav-text">我的好友</span>
          </div>

          <div
            class="nav-item"
            :class="{ active: currentNav === 'groups' }"
            @click="switchNav('groups')"
          >
            <div class="nav-icon-wrapper bg-gradient-green">
              <el-icon><ChatDotSquare /></el-icon>
            </div>
            <span class="nav-text">我的群组</span>
          </div>
        </div>

        <div class="nav-divider"></div>

        <!-- 组织架构 -->
        <div class="nav-group">
          <div class="nav-group-title">组织架构</div>
          <div class="org-tree-container">
             <div
              class="nav-item org-root"
              :class="{ expanded: orgExpanded }"
              @click="toggleOrg"
            >
              <el-icon class="arrow-icon" :class="{ rotated: orgExpanded }"><ArrowRight /></el-icon>
              <div class="nav-icon-wrapper bg-gradient-purple">
                <el-icon><OfficeBuilding /></el-icon>
              </div>
              <span class="nav-text">企业组织</span>
            </div>

            <Transition name="expand">
              <div v-show="orgExpanded" class="org-list">
                <div
                  v-for="dept in flatDepts"
                  :key="dept.id"
                  class="org-item"
                  :class="{
                    active: selectedDeptId === dept.id
                  }"
                  :style="{ paddingLeft: `${20 + (dept.level || 0) * 16}px` }"
                  @click="selectDept(dept)"
                >
                  <div class="org-item-content">
                    <span v-if="dept.level > 0" class="org-line"></span>
                    <span class="org-name">{{ dept.name }}</span>
                    <span class="org-count">{{ dept.userCount || 0 }}</span>
                  </div>
                </div>
              </div>
            </Transition>
          </div>
        </div>
      </nav>
    </aside>

    <!-- 中间列表区 -->
    <section class="list-panel">
      <div class="list-header">
        <div class="header-left">
          <el-button v-if="isMobile" :icon="Menu" circle text @click="showMobileSidebar = true" class="mobile-menu-btn" />
          <h3 class="list-title">{{ currentListTitle }}</h3>
        </div>
        <div class="list-actions">
           <el-button v-if="currentNav === 'friends'" type="primary" link @click="showAddFriend = true">
             <el-icon class="mr-1"><Plus /></el-icon> 添加
           </el-button>
        </div>
      </div>
      
      <div class="list-content-wrapper" v-loading="loading">
        <!-- 新的朋友列表 -->
        <div v-if="currentNav === 'new'" class="friend-requests scrollbar-custom">
          <div v-if="friendRequests.length === 0" class="empty-state">
             <el-empty description="暂无新的好友请求" :image-size="120" />
          </div>
          <div v-else class="request-list">
            <div v-for="req in friendRequests" :key="req.id" class="request-item">
               <DingtalkAvatar :src="req.avatar" :name="req.nickname" :size="48" />
               <div class="request-info">
                 <div class="request-name">{{ req.nickname }}</div>
                 <div class="request-msg">{{ req.reason || '请求添加好友' }}</div>
               </div>
               <div class="request-actions">
                 <el-button type="primary" size="small" round @click="acceptRequest(req)">接受</el-button>
                 <el-button size="small" round @click="ignoreRequest(req)">忽略</el-button>
               </div>
            </div>
          </div>
        </div>

        <!-- 通用列表 (好友/群组/部门成员) -->
        <VirtualList
          v-else-if="currentList.length > 0"
          class="virtual-list scrollbar-custom"
          :items="currentList"
          :item-size="72"
        >
          <template #default="{ item }">
            <div 
              class="list-item"
              :class="{ active: selectedItemId === item.id }"
              @click="selectItem(item)"
            >
              <DingtalkAvatar 
                :src="item.avatar || item.groupAvatar" 
                :name="item.name || item.groupName || item.nickname" 
                :size="44" 
                :shape="currentNav === 'groups' ? 'square' : 'circle'"
              />
              <div class="item-info">
                <div class="item-header">
                  <span class="item-name">{{ item.name || item.groupName || item.nickname }}</span>
                  <span v-if="item.role === 'OWNER'" class="role-tag owner">群主</span>
                  <span v-if="item.role === 'ADMIN'" class="role-tag admin">管理员</span>
                </div>
                <div class="item-desc" v-if="item.signature || item.description || item.notice">
                  {{ item.signature || item.description || item.notice || '暂无介绍' }}
                </div>
              </div>
            </div>
          </template>
        </VirtualList>
        
        <div v-else class="empty-state">
           <el-empty :description="getEmptyText" :image-size="120" />
        </div>
      </div>
    </section>

    <!-- 右侧详情区 -->
    <main class="detail-panel" :class="{ 'detail-open': selectedItem || isMobile }">
      <div v-if="isMobile && selectedItem" class="mobile-detail-header">
        <el-button :icon="ArrowLeft" text @click="selectedItem = null">返回</el-button>
        <span>详情</span>
      </div>
      
      <div v-if="selectedItem" class="detail-content-wrapper">
         <ContactDetail 
           v-if="currentNav === 'friends' || currentNav === 'org' || currentNav === 'new'" 
           :contact="selectedItem"
           @message="handleMessage"
           @voice-call="handleVoiceCall"
           @video-call="handleVideoCall"
         />
         <!-- 群组详情暂时复用ContactDetail或开发新的组件，这里假设ContactDetail能处理 -->
         <ContactDetail 
           v-else-if="currentNav === 'groups'"
           :contact="{ ...selectedItem, isGroup: true }"
           @message="handleMessage"
         />
      </div>
      <div v-else class="empty-selection">
        <div class="empty-content">
          <img src="@/assets/images/login-bg.png" class="empty-bg" alt="" />
          <h3>即时通讯</h3>
          <p>选择联系人或群组以开始聊天</p>
        </div>
      </div>
    </main>

    <!-- 搜索面板 -->
    <Transition name="fade">
      <div v-if="showSearchPanel" class="search-modal-overlay" @click.self="showSearchPanel = false">
        <div class="search-modal">
          <div class="search-header">
            <el-input
              ref="searchInputRef"
              v-model="searchQuery"
              placeholder="搜索联系人、群组、部门"
              prefix-icon="Search"
              clearable
              size="large"
              @input="handleSearch"
            />
            <el-button link class="close-search-btn" @click="showSearchPanel = false">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
          
          <div class="search-body scrollbar-custom">
             <div v-if="searchQuery && searchResults.length > 0" class="search-results-list">
                <div 
                  v-for="item in searchResults" 
                  :key="item.id" 
                  class="search-result-item"
                  @click="selectItem(item); showSearchPanel = false"
                >
                   <DingtalkAvatar :src="item.avatar" :name="item.name" :size="40" />
                   <div class="result-info">
                     <span class="result-name" v-html="highlightText(item.name, searchQuery)"></span>
                     <span class="result-type-tag" :class="item.type">{{ item.type === 'group' ? '群组' : '联系人' }}</span>
                   </div>
                   <el-icon class="arrow-right"><ArrowRight /></el-icon>
                </div>
             </div>
             <div v-else-if="searchQuery" class="empty-search">
               <el-empty description="未找到相关结果" :image-size="80" />
             </div>
             <div v-else class="search-history">
               <!-- 可以在这里添加搜索历史 -->
             </div>
          </div>
        </div>
      </div>
    </Transition>

  </div>
</template>

<script setup>
import { getItem, removeItem, setJSON } from '@/utils/storage'
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useStore } from 'vuex'
import { useWindowSize, useDebounceFn } from '@vueuse/core'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import ContactDetail from '@/components/Contacts/ContactDetail.vue'
import VirtualList from '@/components/Common/VirtualList.vue'
import { getFriendRequests, getGroupedFriendList, searchContacts } from '@/api/im/contact'
import { getGroups } from '@/api/im/group'
import { getOrgTree, getDepartmentMembers } from '@/api/im/organization'
import { ElMessage } from 'element-plus'
import { 
  Search, User, Avatar, ChatDotSquare, OfficeBuilding, 
  ArrowRight, Plus, Close, Menu, ArrowLeft
} from '@element-plus/icons-vue'

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
const showAddMenu = ref(false)
const showAddFriend = ref(false)
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
const selectedItem = ref(null)

// Org Tree
const orgExpanded = ref(true)
const selectedDeptId = ref(null)
const flatDepts = ref([])

// Computed for Display Titles
const listTitle = computed(() => {
    if (searchQuery.value) return '搜索结果'
    const map = { new: '新的朋友', friends: '我的好友', groups: '我的群组', org: '组织架构' }
    return map[currentNav.value] || ''
})

const currentListTitle = computed(() => listTitle.value)

const currentList = computed(() => {
  if (currentNav.value === 'friends') return friendGroups.value.flatMap(g => g.friends) // Flatten for virtual list if needed, or handle groups
  if (currentNav.value === 'groups') return groupList.value
  if (currentNav.value === 'org') return orgMembers.value
  return []
})

const getEmptyText = computed(() => `暂无${currentListTitle.value}`)

// Methods
const switchNav = (nav) => {
  currentNav.value = nav
  selectedItemId.value = null
  selectedItem.value = null
  if (isMobile.value) showMobileSidebar.value = false
  fetchData(nav)
}

const toggleOrg = () => {
  orgExpanded.value = !orgExpanded.value
}

const selectDept = async (dept) => {
  selectedDeptId.value = dept.id
  currentNav.value = 'org'
  loading.value = true
  try {
    const res = await getDepartmentMembers(dept.id)
    orgMembers.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const selectItem = (item) => {
  selectedItemId.value = item.id
  selectedItem.value = item
}

const handleSearch = useDebounceFn(async () => {
  if (!searchQuery.value) {
    searchResults.value = []
    return
  }
  try {
    const res = await searchContacts(searchQuery.value)
    searchResults.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}, 300)

const highlightText = (text, query) => {
  if (!query) return text
  const reg = new RegExp(`(${query})`, 'gi')
  return text.replace(reg, '<span class="highlight">$1</span>')
}

const fetchData = async (nav) => {
  loading.value = true
  try {
    if (nav === 'friends') {
      const res = await getGroupedFriendList()
      friendGroups.value = res.data || []
    } else if (nav === 'groups') {
      const res = await getGroups()
      groupList.value = res.data || []
    } else if (nav === 'new') {
      const res = await getFriendRequests()
      // pendingCount.value = res.data.length
      // Assuming friendRequests is stored in store as well or updated here
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleMessage = (contact) => {
  // Logic to switch to chat panel
  console.log('Message', contact)
}

const handleVoiceCall = (contact) => {
  console.log('Voice Call', contact)
}

const handleVideoCall = (contact) => {
  console.log('Video Call', contact)
}

const acceptRequest = (req) => {
  console.log('Accept', req)
}

const ignoreRequest = (req) => {
  console.log('Ignore', req)
}

// Lifecycle
onMounted(() => {
  fetchData('friends')
  // Load org tree
  getOrgTree().then(res => {
    // Flatten logic if needed, or just assign
    flatDepts.value = res.data || [] 
  })
})

watch(showSearchPanel, (val) => {
  if (val) {
    nextTick(() => searchInputRef.value?.focus())
  }
})
</script>

<style scoped lang="scss">
@use "sass:color";
/* Variables */
$sidebar-width: 260px;
$list-width: 320px;
$bg-color: #f5f7fa;
$border-color: #e4e7ed;
$primary-color: var(--el-color-primary);
$text-primary: #303133;
$text-secondary: #606266;
$text-tertiary: #909399;
$hover-bg: #f0f2f5;
$active-bg: #e6f1fc; // Light blue for active states

.contacts-panel {
  display: flex;
  height: 100%;
  width: 100%;
  background-color: #fff;
  overflow: hidden;
  position: relative;

  &.mobile-view {
    flex-direction: column;
    
    .sidebar {
      position: absolute;
      z-index: 100;
      height: 100%;
      transform: translateX(-100%);
      transition: transform 0.3s ease;
      box-shadow: 2px 0 8px rgba(0,0,0,0.1);
      
      &.sidebar-open {
        transform: translateX(0);
      }
    }
    
    .list-panel {
      width: 100%;
      border-right: none;
    }
    
    .detail-panel {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      z-index: 50;
      transform: translateX(100%);
      transition: transform 0.3s ease;
      background: #fff;
      
      &.detail-open {
        transform: translateX(0);
      }
    }
  }
}

.mobile-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.4);
  z-index: 90;
  backdrop-filter: blur(2px);
}

/* Sidebar */
.sidebar {
  width: $sidebar-width;
  background: #fcfcfc;
  border-right: 1px solid $border-color;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid transparent;
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    
    .sidebar-title {
      font-size: 20px;
      font-weight: 600;
      color: $text-primary;
      margin: 0;
    }
  }
  
  .search-box {
    background: $hover-bg;
    border-radius: 6px;
    padding: 8px 12px;
    display: flex;
    align-items: center;
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      background: color.adjust($hover-bg, $lightness: -2%);
    }
    
    .search-icon {
      color: $text-tertiary;
      margin-right: 8px;
    }
    
    .placeholder {
      font-size: 13px;
      color: $text-tertiary;
    }
  }
}

.nav-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.nav-group {
  margin-bottom: 24px;
}

.nav-group-title {
  padding: 0 12px;
  font-size: 12px;
  font-weight: 600;
  color: $text-tertiary;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  color: $text-primary;
  margin-bottom: 4px;
  
  &:hover {
    background-color: $hover-bg;
  }
  
  &.active {
    background-color: $active-bg;
    color: $primary-color;
    font-weight: 500;
  }
  
  .nav-icon-wrapper {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12px;
    color: #fff;
    font-size: 16px;
    flex-shrink: 0;
    
    &.bg-gradient-orange { background: linear-gradient(135deg, #FF9A9E 0%, #FECFEF 100%); color: #fff; }
    &.bg-gradient-blue { background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%); color: #fff; }
    &.bg-gradient-green { background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%); color: #fff; }
    &.bg-gradient-purple { background: linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%); color: #fff; }
  }
  
  .nav-text {
    flex: 1;
    font-size: 14px;
  }
  
  .nav-badge {
    background: #ff4d4f;
    color: #fff;
    font-size: 11px;
    padding: 2px 6px;
    border-radius: 10px;
    line-height: 1;
  }
}

.nav-divider {
  height: 1px;
  background: $border-color;
  margin: 12px 16px 24px;
}

.org-root {
  .arrow-icon {
    font-size: 12px;
    color: $text-tertiary;
    margin-right: 8px;
    transition: transform 0.2s;
    
    &.rotated {
      transform: rotate(90deg);
    }
  }
}

.org-list {
  margin-top: 4px;
}

.org-item {
  padding: 8px 12px;
  cursor: pointer;
  transition: background-color 0.2s;
  border-radius: 6px;
  
  &:hover {
    background-color: $hover-bg;
  }
  
  &.active {
    background-color: $active-bg;
    
    .org-name {
      color: $primary-color;
      font-weight: 500;
    }
  }
  
  .org-item-content {
    display: flex;
    align-items: center;
    position: relative;
    
    .org-line {
      position: absolute;
      left: -12px;
      top: 50%;
      width: 8px;
      height: 1px;
      background: $border-color;
    }
    
    .org-name {
      font-size: 14px;
      color: $text-primary;
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .org-count {
      font-size: 12px;
      color: $text-tertiary;
      margin-left: 8px;
    }
  }
}

/* List Panel */
.list-panel {
  width: $list-width;
  border-right: 1px solid $border-color;
  display: flex;
  flex-direction: column;
  background: #fff;
  flex-shrink: 0;
}

.list-header {
  height: 60px;
  padding: 0 16px;
  border-bottom: 1px solid $border-color;
  display: flex;
  align-items: center;
  justify-content: space-between;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .list-title {
      font-size: 16px;
      font-weight: 600;
      color: $text-primary;
      margin: 0;
    }
  }
}

.list-content-wrapper {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.virtual-list {
  height: 100%;
}

.list-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
  border-bottom: 1px solid transparent; // prevent jump
  
  &:hover {
    background-color: $hover-bg;
  }
  
  &.active {
    background-color: $active-bg;
  }
  
  .item-info {
    margin-left: 12px;
    flex: 1;
    overflow: hidden;
    
    .item-header {
      display: flex;
      align-items: center;
      margin-bottom: 4px;
      
      .item-name {
        font-size: 15px;
        font-weight: 500;
        color: $text-primary;
        margin-right: 8px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      
      .role-tag {
        font-size: 10px;
        padding: 1px 4px;
        border-radius: 4px;
        white-space: nowrap;
        
        &.owner { background: #fff7e6; color: #fa8c16; border: 1px solid #ffd591; }
        &.admin { background: #e6f7ff; color: #1890ff; border: 1px solid #91d5ff; }
      }
    }
    
    .item-desc {
      font-size: 13px;
      color: $text-tertiary;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}

/* Detail Panel */
.detail-panel {
  flex: 1;
  background: $bg-color;
  display: flex;
  flex-direction: column;
  position: relative;
  
  .mobile-detail-header {
    height: 50px;
    background: #fff;
    border-bottom: 1px solid $border-color;
    display: flex;
    align-items: center;
    padding: 0 8px;
    font-weight: 600;
  }
}

.detail-content-wrapper {
  flex: 1;
  height: 100%;
  overflow: hidden;
}

.empty-selection {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .empty-content {
    text-align: center;
    
    .empty-bg {
      width: 240px;
      margin-bottom: 24px;
      opacity: 0.8;
    }
    
    h3 {
      font-size: 18px;
      color: $text-primary;
      margin-bottom: 8px;
    }
    
    p {
      color: $text-tertiary;
      font-size: 14px;
    }
  }
}

/* Friend Requests */
.request-list {
  padding: 16px;
}

.request-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid $border-color;
  margin-bottom: 12px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.02);
  
  .request-info {
    flex: 1;
    margin-left: 12px;
    
    .request-name {
      font-weight: 500;
      color: $text-primary;
      margin-bottom: 4px;
    }
    
    .request-msg {
      font-size: 13px;
      color: $text-tertiary;
    }
  }
  
  .request-actions {
    display: flex;
    gap: 8px;
  }
}

/* Search Modal */
.search-modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.4);
  z-index: 200;
  backdrop-filter: blur(2px);
  display: flex;
  justify-content: center;
  padding-top: 80px;
}

.search-modal {
  width: 600px;
  max-width: 90%;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
  display: flex;
  flex-direction: column;
  max-height: 70vh;
  animation: modal-in 0.3s ease;
  
  .search-header {
    padding: 16px;
    border-bottom: 1px solid $border-color;
    display: flex;
    align-items: center;
    gap: 12px;
    
    .close-search-btn {
      font-size: 20px;
      color: $text-tertiary;
      
      &:hover { color: $text-primary; }
    }
  }
  
  .search-body {
    flex: 1;
    overflow-y: auto;
    padding: 16px 0;
    min-height: 200px;
  }
}

.search-result-item {
  display: flex;
  align-items: center;
  padding: 12px 24px;
  cursor: pointer;
  transition: background 0.2s;
  
  &:hover {
    background: $hover-bg;
    
    .arrow-right { opacity: 1; transform: translateX(0); }
  }
  
  .result-info {
    flex: 1;
    margin-left: 12px;
    display: flex;
    flex-direction: column;
    
    .result-name {
      font-weight: 500;
      color: $text-primary;
      margin-bottom: 2px;
    }
    
    .result-type-tag {
      font-size: 11px;
      color: $text-tertiary;
      background: #f0f2f5;
      padding: 1px 6px;
      border-radius: 4px;
      align-self: flex-start;
      
      &.group { color: #1890ff; background: #e6f7ff; }
      &.contact { color: #52c41a; background: #f6ffed; }
    }
  }
  
  .arrow-right {
    color: $text-tertiary;
    opacity: 0;
    transform: translateX(-5px);
    transition: all 0.2s;
  }
}

/* Animations */
@keyframes modal-in {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  max-height: 500px;
  opacity: 1;
}
.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  opacity: 0;
}

.mr-1 { margin-right: 4px; }
</style>
