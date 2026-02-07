<template>
  <div
    class="contacts-panel"
    :class="{ 'group-profile-open': groupProfileOpen }"
  >
    <!-- 左侧导航栏 -->
    <aside class="sidebar">
      <!-- 顶部标题区 -->
      <div class="sidebar-header">
        <div class="header-content">
          <h2 class="sidebar-title">
            通讯录
          </h2>
          <el-tooltip
            content="添加朋友/群组"
            placement="bottom"
          >
            <el-button
              circle
              size="small"
              :icon="Plus"
              @click="showAddMenu = true"
            />
          </el-tooltip>
        </div>
        <div
          class="search-box"
          @click="showSearchPanel = true"
        >
          <el-icon class="search-icon">
            <Search />
          </el-icon>
          <span class="placeholder">搜索联系人、群组...</span>
        </div>
      </div>

      <!-- 导航菜单 -->
      <nav class="nav-list scrollbar-custom">
        <div class="nav-group">
          <div class="nav-group-title">
            常用
          </div>
          <div
            class="nav-item"
            :class="{ active: currentNav === 'new' }"
            @click="switchNav('new')"
          >
            <div class="nav-icon-wrapper bg-gradient-orange">
              <el-icon>
                <User />
              </el-icon>
            </div>
            <span class="nav-text">新的朋友</span>
            <span
              v-if="pendingCount > 0"
              class="nav-badge"
            >{{ pendingCount > 99 ? '99+' : pendingCount }}</span>
          </div>

          <div
            class="nav-item"
            :class="{ active: currentNav === 'recommended' }"
            @click="switchNav('recommended')"
          >
            <div class="nav-icon-wrapper bg-gradient-pink">
              <el-icon>
                <Star />
              </el-icon>
            </div>
            <span class="nav-text">可能认识的人</span>
            <span
              v-if="recommendedCount > 0"
              class="nav-dot"
            />
          </div>

          <div
            class="nav-item"
            :class="{ active: currentNav === 'all' }"
            @click="switchNav('all')"
          >
            <div class="nav-icon-wrapper bg-gradient-primary">
              <el-icon>
                <User />
              </el-icon>
            </div>
            <span class="nav-text">公司同事</span>
            <span class="nav-count">{{ allUsers.length }}</span>
          </div>

          <div
            class="nav-item"
            :class="{ active: currentNav === 'friends' }"
            @click="switchNav('friends')"
          >
            <div class="nav-icon-wrapper bg-gradient-blue">
              <el-icon>
                <Avatar />
              </el-icon>
            </div>
            <span class="nav-text">我的好友</span>
            <el-dropdown
              trigger="click"
              @command="handleGroupCommand"
              @click.stop
            >
              <el-icon class="group-menu-icon">
                <MoreFilled />
              </el-icon>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="add">
                    <span class="material-icons-outlined">add</span>
                    添加分组
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <!-- 好友分组列表 -->
          <Transition name="expand">
            <div
              v-if="friendGroupNames.length > 0"
              class="friend-groups-list"
            >
              <div
                v-for="groupName in friendGroupNames"
                :key="groupName"
                class="nav-item nav-item-sub"
                :class="{ active: currentNav === groupName }"
                @click="switchNav(groupName)"
              >
                <div class="nav-icon-wrapper bg-gradient-light">
                  <span class="group-initial">{{ groupName.charAt(0) }}</span>
                </div>
                <span class="nav-text">{{ groupName }}</span>
                <el-dropdown
                  trigger="click"
                  @command="(cmd) => handleGroupItemCommand(cmd, groupName)"
                  @click.stop
                >
                  <el-icon class="group-item-menu-icon">
                    <MoreFilled />
                  </el-icon>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="rename">
                        <span class="material-icons-outlined">edit</span>
                        重命名
                      </el-dropdown-item>
                      <el-dropdown-item
                        command="delete"
                        divided
                      >
                        <span class="material-icons-outlined">delete</span>
                        删除分组
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </Transition>

          <div
            class="nav-item"
            :class="{ active: currentNav === 'groups' }"
            @click="switchNav('groups')"
          >
            <div class="nav-icon-wrapper bg-gradient-green">
              <el-icon>
                <ChatDotSquare />
              </el-icon>
            </div>
            <span class="nav-text">我的群组</span>
          </div>
        </div>

        <div class="nav-divider" />

        <!-- 组织架构 -->
        <div class="nav-group">
          <div class="nav-group-title">
            组织架构
          </div>
          <!-- 组织架构搜索 -->
          <div class="org-search-box">
            <el-input
              v-model="orgSearchQuery"
              placeholder="搜索部门/人员..."
              :prefix-icon="Search"
              size="small"
              clearable
              @input="handleOrgSearch"
            />
          </div>
          <div class="org-tree-container">
            <div
              class="nav-item org-root"
              :class="{ expanded: orgExpanded }"
              @click="toggleOrg"
            >
              <el-icon
                class="arrow-icon"
                :class="{ rotated: orgExpanded }"
              >
                <ArrowRight />
              </el-icon>
              <div class="nav-icon-wrapper bg-gradient-purple">
                <el-icon>
                  <OfficeBuilding />
                </el-icon>
              </div>
              <span class="nav-text">企业组织</span>
            </div>

            <Transition name="expand">
              <div
                v-show="orgExpanded"
                class="org-list"
              >
                <div
                  v-for="dept in flatDepts"
                  :key="dept.id"
                  class="org-item"
                  :class="{
                    active: selectedDeptId === dept.id,
                    'has-children': dept.hasChildren
                  }"
                  :style="{ paddingLeft: `${20 + (dept.level || 0) * 16}px` }"
                  @click="selectDept(dept)"
                >
                  <div class="org-item-content">
                    <el-icon
                      v-if="dept.hasChildren"
                      class="org-expand-icon"
                      :class="{ expanded: dept.expanded }"
                    >
                      <ArrowRight />
                    </el-icon>
                    <span
                      v-if="dept.level > 0 && !dept.hasChildren"
                      class="org-line"
                    />
                    <span class="org-name">{{ dept.name }}</span>
                    <span class="org-stats">
                      <span class="org-count">{{ dept.userCount || 0 }}人</span>
                      <span
                        v-if="dept.onlineCount > 0"
                        class="org-online"
                      >
                        <span class="online-dot" />
                        {{ dept.onlineCount }}
                      </span>
                    </span>
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
          <h3 class="list-title">
            {{ currentListTitle }}
          </h3>
        </div>
        <div class="list-actions">
          <el-button
            v-if="currentNav === 'friends'"
            type="primary"
            link
            @click="showAddFriendDialog = true"
          >
            <el-icon class="mr-1">
              <Plus />
            </el-icon> 添加
          </el-button>
        </div>
      </div>

      <div
        v-loading="loading"
        class="list-content-wrapper"
      >
        <!-- 推荐联系人 -->
        <div
          v-if="currentNav === 'recommended'"
          class="recommend-content scrollbar-custom"
        >
          <RecommendedContacts
            ref="recommendedContactsRef"
            @update="handleRecommendUpdate"
          />
        </div>

        <!-- 新的朋友列表 -->
        <div
          v-else-if="currentNav === 'new'"
          class="friend-requests scrollbar-custom"
        >
          <NewFriendsView
            ref="newFriendsViewRef"
            @update-count="pendingCount = $event"
          />
        </div>

        <!-- 通用列表 (好友/群组/部门成员) -->
        <VirtualList
          v-else-if="currentList.length > 0"
          class="virtual-list scrollbar-custom"
          :items="currentList"
          :item-size="72"
          :class="{ 'batch-mode': batchMode }"
        >
          <template #default="{ item }">
            <div
              class="list-item"
              :class="{ active: selectedItemId === item.id, 'batch-mode': batchMode }"
              @click="handleItemClick(item)"
            >
              <!-- 批量选择复选框 -->
              <el-checkbox
                v-if="batchMode"
                :model-value="selectedContacts.has(item.id)"
                class="item-checkbox"
                @change="(val) => toggleContactSelection(item.id, val)"
                @click.stop
              />

              <DingtalkAvatar
                :src="item.avatar || item.groupAvatar"
                :name="item.name || item.groupName || item.nickname"
                :size="44"
                :shape="currentNav === 'groups' ? 'square' : 'circle'"
              />
              <div class="item-info">
                <div class="item-header">
                  <span class="item-name">{{ item.name || item.groupName || item.nickname }}</span>
                  <span
                    v-if="item.role === 'OWNER'"
                    class="role-tag owner"
                  >群主</span>
                  <span
                    v-if="item.role === 'ADMIN'"
                    class="role-tag admin"
                  >管理员</span>
                </div>
                <div
                  v-if="item.signature || item.description || item.notice"
                  class="item-desc"
                >
                  {{ item.signature || item.description || item.notice || '暂无介绍' }}
                </div>
              </div>
            </div>
          </template>
        </VirtualList>

        <div
          v-else
          class="empty-state"
        >
          <el-empty
            :description="getEmptyText"
            :image-size="120"
          />
        </div>
      </div>
    </section>

    <!-- 右侧详情区 -->
    <main
      class="detail-panel"
      :class="{ 'detail-open': selectedItem }"
    >
      <div
        v-if="selectedItem"
        class="detail-content-wrapper"
      >
        <ContactDetail
          v-if="currentNav === 'friends' || currentNav === 'org' || currentNav === 'new'"
          :contact="selectedItem"
          @message="handleMessage"
          @voice-call="handleVoiceCall"
          @video-call="handleVideoCall"
          @toggle-group-profile="handleGroupProfileToggle"
        />
        <!-- 群组详情暂时复用ContactDetail或开发新的组件，这里假设ContactDetail能处理 -->
        <ContactDetail
          v-else-if="currentNav === 'groups'"
          :contact="{ ...selectedItem, isGroup: true }"
          @message="handleMessage"
          @toggle-group-profile="handleGroupProfileToggle"
        />
      </div>
      <div
        v-else
        class="empty-selection"
      >
        <div class="empty-content">
          <img
            src="@/assets/images/login-bg.png"
            class="empty-bg"
            alt=""
          >
          <h3>即时通讯</h3>
          <p>选择联系人或群组以开始聊天</p>
        </div>
      </div>
    </main>

    <!-- 搜索面板 -->
    <Transition name="fade">
      <div
        v-if="showSearchPanel"
        class="search-modal-overlay"
        @click.self="showSearchPanel = false"
      >
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
            <el-button
              link
              class="close-search-btn"
              @click="showSearchPanel = false"
            >
              <el-icon>
                <Close />
              </el-icon>
            </el-button>
          </div>

          <div class="search-body scrollbar-custom">
            <div
              v-if="searchQuery && searchResults.length > 0"
              class="search-results-list"
            >
              <div
                v-for="item in searchResults"
                :key="item.id"
                class="search-result-item"
                @click="selectItem(item); showSearchPanel = false"
              >
                <DingtalkAvatar
                  :src="item.avatar"
                  :name="item.name"
                  :size="40"
                />
                <div class="result-info">
                  <!-- eslint-disable-next-line vue/no-v-html -->
                  <span
                    class="result-name"
                    v-html="highlightText(item.name, searchQuery)"
                  />
                  <span
                    class="result-type-tag"
                    :class="item.type"
                  >{{ item.type === 'group' ? '群组' : '联系人' }}</span>
                </div>
                <el-icon class="arrow-right">
                  <ArrowRight />
                </el-icon>
              </div>
            </div>
            <div
              v-else-if="searchQuery"
              class="empty-search"
            >
              <el-empty
                description="未找到相关结果"
                :image-size="80"
              />
            </div>
            <div
              v-else
              class="search-history"
            >
              <!-- 可以在这里添加搜索历史 -->
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <!-- 添加分组对话框 -->
    <el-dialog
      v-model="showAddGroupDialog"
      title="添加好友分组"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form @submit.prevent="handleAddGroup">
        <el-form-item label="分组名称">
          <el-input
            v-model="newGroupName"
            placeholder="请输入分组名称"
            maxlength="20"
            show-word-limit
            @keyup.enter="handleAddGroup"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddGroupDialog = false">
          取消
        </el-button>
        <el-button
          type="primary"
          @click="handleAddGroup"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 重命名分组对话框 -->
    <el-dialog
      v-model="showRenameGroupDialog"
      title="重命名分组"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form @submit.prevent="handleRenameGroup">
        <el-form-item label="分组名称">
          <el-input
            v-model="renameGroupName"
            placeholder="请输入新的分组名称"
            maxlength="20"
            show-word-limit
            @keyup.enter="handleRenameGroup"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRenameGroupDialog = false">
          取消
        </el-button>
        <el-button
          type="primary"
          @click="handleRenameGroup"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 分组选择对话框 -->
    <GroupSelectDialog
      v-model="showGroupSelectDialog"
      :selected-count="selectedContacts.size"
      @confirm="handleGroupSelectConfirm"
    />

    <!-- 添加好友对话框 -->
    <AddFriendDialog
      v-model="showAddFriendDialog"
      @success="handleAddFriendSuccess"
    />

    <!-- 批量操作栏 -->
    <BatchOperationBar
      :visible="batchMode"
      :selected-count="selectedCount"
      :total-count="currentList.length"
      :type="currentNav === 'groups' ? 'group' : currentNav === 'all' || currentNav.startsWith('group-') ? 'other' : 'friend'"
      @select-all="handleSelectAll"
      @cancel="exitBatchMode"
      @move-group="handleBatchMoveGroup"
      @send-message="handleBatchSendMessage"
      @delete="handleBatchDelete"
      @add-friends="handleBatchAddFriends"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useStore } from 'vuex'
import { useDebounceFn } from '@vueuse/core'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import ContactDetail from '@/components/Contacts/ContactDetail.vue'
import VirtualList from '@/components/Common/VirtualList.vue'
import RecommendedContacts from '@/components/Contacts/RecommendedContacts.vue'
import BatchOperationBar from '@/components/Contacts/BatchOperationBar.vue'
import GroupSelectDialog from '@/components/Contacts/GroupSelectDialog.vue'
import AddFriendDialog from '@/components/Contacts/AddFriendDialog.vue'
import NewFriendsView from '@/components/Contacts/NewFriendsView.vue'
import { getFriendRequests, getGroupedFriendList, searchContacts, handleFriendRequest, getGroupList, createGroup, renameGroup, deleteGroup } from '@/api/im/contact'
import { getGroups } from '@/api/im/group'
import { getOrgTree, getDepartmentMembers, searchOrgMembers } from '@/api/im/organization'
import { getAllUsers } from '@/api/im/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, User, Avatar, ChatDotSquare, OfficeBuilding,
  ArrowRight, Plus, Close, Menu, ArrowLeft, MoreFilled, Star
} from '@element-plus/icons-vue'
import { useHighlightText } from '@/composables/useHighlightText'
import { useContactBatch } from '@/composables/useContactBatch'

// 定义 emit，用于通知父组件切换模块或触发通话
const emit = defineEmits(['switch-module', 'voice-call', 'video-call'])

const store = useStore()

// State
const currentNav = ref('new') // 当前导航项
const searchQuery = ref('')
const showSearchPanel = ref(false)
const showAddMenu = ref(false)
const loading = ref(false)
const searchInputRef = ref(null)
const recommendedContactsRef = ref(null)

// Data
const friendGroups = ref([])
const friendGroupNames = ref([]) // 好友分组名称列表
const groupList = ref([])
const orgMembers = ref([])
const allUsers = ref([]) // 公司所有用户
const friendRequests = ref([])
const pendingCount = ref(0)
const recommendedCount = ref(0)
const searchResults = ref([])

// 分组管理状态
const showAddGroupDialog = ref(false)
const showRenameGroupDialog = ref(false)
const newGroupName = ref('')
const renameGroupName = ref('')
const selectedGroupName = ref('')

// 分组选择对话框
const showGroupSelectDialog = ref(false)

// 添加好友对话框
const showAddFriendDialog = ref(false)

// 添加好友成功处理
const handleAddFriendSuccess = () => {
  ElMessage.success('好友请求已发送，请等待对方确认')
  // 可以在这里刷新好友请求列表
  fetchData('new')
}

// Selection
const selectedItemId = ref(null)
const selectedItem = ref(null)
const groupProfileOpen = ref(false)

// Org Tree
const orgExpanded = ref(true)
const selectedDeptId = ref(null)
const flatDepts = ref([])

// 组织架构搜索
const orgSearchQuery = ref('')
const orgSearchResults = ref([])
const orgSearching = ref(false)

// ==================== Composables ====================
// 搜索高亮
const { highlightText } = useHighlightText(searchQuery)

// 批量操作
const {
  batchMode,
  selectedContacts,
  selectedCount,
  hasSelection,
  exitBatchMode,
  toggleContactSelection,
  handleSelectAll: batchSelectAll,
  handleBatchMoveGroup: batchMoveGroup,
  handleBatchSendMessage: batchSendMessage,
  handleBatchDelete: batchDelete,
  handleBatchAddFriends: batchAddFriends
} = useContactBatch()

// Computed for Display Titles
const listTitle = computed(() => {
  if (searchQuery.value) { return '搜索结果' }
  const map = {
    new: '新的朋友',
    recommended: '可能认识的人',
    all: '公司同事',
    friends: '我的好友',
    groups: '我的群组',
    org: '组织架构'
  }
  if (map[currentNav.value]) { return map[currentNav.value] }
  // 如果是分组导航，返回分组名称
  const group = friendGroups.value.find(g => g.groupName === currentNav.value)
  if (group) { return group.groupName }
  return ''
})

const currentListTitle = computed(() => listTitle.value)

const currentList = computed(() => {
  if (currentNav.value === 'all') {
    return allUsers.value
  }
  if (currentNav.value === 'friends') {
    // 全部好友 - 展平所有分组
    return friendGroups.value.flatMap(g => g.friends)
  }
  // 检查是否是分组导航
  const group = friendGroups.value.find(g => g.groupName === currentNav.value)
  if (group) {
    return group.friends || []
  }
  if (currentNav.value === 'groups') { return groupList.value }
  if (currentNav.value === 'org') { return orgMembers.value }
  return []
})

const getEmptyText = computed(() => `暂无${currentListTitle.value}`)

// Methods
const switchNav = nav => {
  currentNav.value = nav
  selectedItemId.value = null
  selectedItem.value = null
  fetchData(nav)
}

const toggleOrg = () => {
  orgExpanded.value = !orgExpanded.value
}

const selectDept = async dept => {
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

const selectItem = item => {
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

const fetchData = async nav => {
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
      friendRequests.value = res.data || []
      pendingCount.value = (res.data || []).length
    } else if (nav === 'all') {
      const res = await getAllUsers()
      allUsers.value = res.data || []
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleMessage = contact => {
  // 切换到聊天模块并打开与该联系人的会话
  emit('switch-module', 'chat')

  // 需要创建或切换到与该联系人的会话
  if (contact.id) {
    store.dispatch('im/session/createAndSwitchSession', {
      type: 'PRIVATE',
      targetId: contact.id
    }).catch(error => {
      console.error('创建会话失败:', error)
    })
  }
}

const handleVoiceCall = contact => {
  // 触发语音通话
  emit('voice-call', {
    userId: contact.id,
    userName: contact.name || contact.nickname,
    userAvatar: contact.avatar
  })
}

const handleVideoCall = contact => {
  // 触发视频通话
  emit('video-call', {
    userId: contact.id,
    userName: contact.name || contact.nickname,
    userAvatar: contact.avatar
  })
}

// ==================== 好友分组管理 ====================

// 加载好友分组列表
const loadFriendGroupNames = async () => {
  try {
    const res = await getGroupList()
    friendGroupNames.value = res.data || []
  } catch (error) {
    console.error('加载好友分组失败:', error)
  }
}

// 打开添加分组对话框
const openAddGroupDialog = () => {
  newGroupName.value = ''
  showAddGroupDialog.value = true
}

// 创建好友分组
const handleAddGroup = async () => {
  if (!newGroupName.value.trim()) {
    ElMessage.warning('请输入分组名称')
    return
  }

  // 检查是否已存在
  if (friendGroupNames.value.includes(newGroupName.value)) {
    ElMessage.warning('该分组已存在')
    return
  }

  try {
    const res = await createGroup({ groupName: newGroupName.value })
    if (res.code === 200) {
      ElMessage.success('分组创建成功')
      showAddGroupDialog.value = false
      await loadFriendGroupNames()
      await fetchData('friends')
    } else {
      ElMessage.error(res.msg || '创建失败')
    }
  } catch (error) {
    console.error('创建分组失败:', error)
    ElMessage.error('创建失败，请稍后重试')
  }
}

// 打开重命名分组对话框
const openRenameGroupDialog = groupName => {
  selectedGroupName.value = groupName
  renameGroupName.value = groupName
  showRenameGroupDialog.value = true
}

// 重命名好友分组
const handleRenameGroup = async () => {
  if (!renameGroupName.value.trim()) {
    ElMessage.warning('请输入分组名称')
    return
  }

  // 检查是否与其他分组重名
  if (renameGroupName.value !== selectedGroupName.value && friendGroupNames.value.includes(renameGroupName.value)) {
    ElMessage.warning('该分组名称已存在')
    return
  }

  try {
    const res = await renameGroup(selectedGroupName.value, renameGroupName.value)
    if (res.code === 200) {
      ElMessage.success('分组重命名成功')
      showRenameGroupDialog.value = false
      await loadFriendGroupNames()
      await fetchData('friends')
    } else {
      ElMessage.error(res.msg || '重命名失败')
    }
  } catch (error) {
    console.error('重命名分组失败:', error)
    ElMessage.error('重命名失败，请稍后重试')
  }
}

// 删除好友分组
const handleDeleteGroup = async groupName => {
  try {
    await ElMessageBox.confirm(
      `删除分组"${groupName}"后，该分组下的好友将移至"未分组"，确定要删除吗？`,
      '删除分组',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await deleteGroup(groupName)
    if (res.code === 200) {
      ElMessage.success('分组删除成功')
      await loadFriendGroupNames()
      await fetchData('friends')
      // 如果当前正在查看该分组，切换到全部好友
      if (currentNav.value === groupName) {
        switchNav('friends')
      }
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分组失败:', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

// Unused function removed

// 分组菜单命令处理
const handleGroupCommand = command => {
  if (command === 'add') {
    openAddGroupDialog()
  }
}

// 分组项菜单命令处理
const handleGroupItemCommand = (command, groupName) => {
  if (command === 'rename') {
    openRenameGroupDialog(groupName)
  } else if (command === 'delete') {
    handleDeleteGroup(groupName)
  }
}

// 推荐更新处理
const handleRecommendUpdate = () => {
  // 可以在这里更新推荐计数或其他状态
}

// ==================== 批量操作 ====================

// 处理列表项点击（批量模式下切换选择）
const handleItemClick = item => {
  if (batchMode.value) {
    toggleContactSelection(item.id, !selectedContacts.value.has(item.id))
  } else {
    selectItem(item)
  }
}

const handleGroupProfileToggle = value => {
  groupProfileOpen.value = value
}

// 全选/取消全选 - 包装composable方法
const handleSelectAll = selected => {
  batchSelectAll(currentList.value, selected)
}

// 批量移动到分组
const handleBatchMoveGroup = async () => {
  if (!hasSelection.value) {
    ElMessage.warning('请先选择联系人')
    return
  }
  showGroupSelectDialog.value = true
}

// 处理分组选择确认
const handleGroupSelectConfirm = async groupName => {
  const contactIds = Array.from(selectedContacts.value)

  await batchMoveGroup(async ids => {
    // 调用批量移动API
    // await batchMoveToGroup({ contactIds: ids, groupName })
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success(`已将 ${ids.length} 个联系人移动到"${groupName}"`)
    fetchData(currentNav.value)
  })
}

// 批量发送消息
const handleBatchSendMessage = async () => {
  await batchSendMessage(async ids => {
    ElMessage.info(`将向 ${ids.length} 个联系人发送消息`)
    // TODO: 实现批量发送消息
  })
}

// 批量删除
const handleBatchDelete = async () => {
  await batchDelete(async ids => {
    // 调用批量删除API
    // await batchDeleteContacts(ids)
    await new Promise(resolve => setTimeout(resolve, 500))
    fetchData(currentNav.value)
  })
}

// 批量添加好友
const handleBatchAddFriends = async () => {
  await batchAddFriends(async (ids, reason) => {
    // 调用批量添加API
    // await batchAddFriends({ userIds: ids, remark: reason })
    await new Promise(resolve => setTimeout(resolve, 500))
  })
}

// ==================== 组织架构搜索 ====================

const handleOrgSearch = useDebounceFn(async () => {
  const query = orgSearchQuery.value.trim()
  if (!query) {
    orgSearchResults.value = []
    return
  }

  orgSearching.value = true
  try {
    // 同时搜索部门和成员
    const [memberRes] = await Promise.all([
      searchOrgMembers({ keyword: query })
    ])

    const results = []

    // 添加成员结果
    if (memberRes.code === 200 && memberRes.data) {
      memberRes.data.forEach(user => {
        results.push({
          id: user.id,
          type: 'user',
          name: user.name || user.nickname,
          avatar: user.avatar,
          dept: user.dept,
          position: user.position
        })
      })
    }

    // 搜索部门
    const searchDepts = (depts, prefix = '') => {
      depts.forEach(dept => {
        if (dept.name && dept.name.toLowerCase().includes(query.toLowerCase())) {
          results.push({
            id: `dept-${dept.id}`,
            type: 'dept',
            name: prefix + dept.name,
            userCount: dept.userCount,
            originalDept: dept
          })
        }
        if (dept.children && dept.children.length > 0) {
          searchDepts(dept.children, prefix + dept.name + '/')
        }
      })
    }
    // 这里需要完整的部门树数据，暂时简化处理
    if (flatDepts.value && flatDepts.value.length > 0) {
      searchDepts(flatDepts.value)
    }

    orgSearchResults.value = results
  } catch (error) {
    console.error('组织架构搜索失败:', error)
  } finally {
    orgSearching.value = false
  }
}, 300)

// Lifecycle
onMounted(() => {
  fetchData('friends')
  loadFriendGroupNames()
  // 预加载所有用户，用于显示数量
  getAllUsers().then(res => {
    allUsers.value = res.data || []
  })
  // Load org tree
  getOrgTree().then(res => {
    // Flatten logic if needed, or just assign
    flatDepts.value = res.data || []
  })
})

watch(showSearchPanel, val => {
  if (val) {
    nextTick(() => searchInputRef.value?.focus())
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// ==================== 布局变量 ====================
$sidebar-width: 240px;
$list-width: 280px;
$list-width-collapsed: 200px;
$panel-min-width: 320px;

.contacts-panel {
  display: flex;
  height: 100%;
  width: 100%;
  background-color: var(--dt-bg-page);
  overflow: hidden;
  position: relative;

  // 滚动条统一样式
  * {
    &::-webkit-scrollbar {
      width: 6px;
      height: 6px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }

    &::-webkit-scrollbar-thumb {
      background-color: rgba(0, 0, 0, 0.1);
      border-radius: 3px;

      &:hover {
        background-color: rgba(0, 0, 0, 0.15);
      }
    }
  }

  &.mobile-view {
    flex-direction: column;

    .sidebar {
      position: absolute;
      z-index: 100;
      height: 100%;
      transform: translateX(-100%);
      transition: transform 0.3s ease;
      box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);

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
  background: rgba(0, 0, 0, 0.4);
  z-index: 90;
  backdrop-filter: blur(2px);
}

/* ==================== 侧边栏 ==================== */
.sidebar {
  width: $sidebar-width;
  background: var(--dt-bg-sidebar);
  border-right: 1px solid var(--dt-border-color);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  padding: var(--dt-spacing-lg);
  border-bottom: 1px solid transparent;

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--dt-spacing-md);

    .sidebar-title {
      font-size: var(--dt-font-size-xl);
      font-weight: var(--dt-font-weight-semibold);
      color: var(--dt-text-primary);
      margin: 0;
    }

    :deep(.el-button) {
      --el-button-bg-color: var(--dt-fill-color);
      --el-button-border-color: transparent;
      --el-button-text-color: var(--dt-text-secondary);
      --el-button-hover-bg-color: var(--dt-fill-color-dark);
      --el-button-hover-border-color: transparent;

      &:hover {
        color: var(--dt-text-primary);
      }
    }
  }

  .search-box {
    background: var(--dt-fill-color);
    border-radius: var(--dt-radius-md);
    padding: 10px 14px;
    display: flex;
    align-items: center;
    cursor: pointer;
    transition: all var(--dt-transition-base);

    &:hover {
      background: var(--dt-fill-color-dark);
    }

    .search-icon {
      color: var(--dt-text-tertiary);
      margin-right: 10px;
      font-size: 16px;
    }

    .placeholder {
      font-size: var(--dt-font-size-sm);
      color: var(--dt-text-tertiary);
    }
  }
}

/* ==================== 导航列表 ==================== */
.nav-list {
  flex: 1;
  overflow-y: auto;
  padding: var(--dt-spacing-md);
}

.nav-group {
  margin-bottom: var(--dt-spacing-xl);
}

.nav-group-title {
  padding: 0 var(--dt-spacing-md);
  font-size: var(--dt-font-size-xs);
  font-weight: var(--dt-font-weight-semibold);
  color: var(--dt-text-tertiary);
  margin-bottom: var(--dt-spacing-sm);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 10px var(--dt-spacing-md);
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all var(--dt-transition-base);
  color: var(--dt-text-primary);
  margin-bottom: 2px;

  &:hover {
    background-color: var(--dt-bg-hover);
  }

  &.active {
    background-color: var(--dt-brand-bg);
    color: var(--dt-brand-color);
    font-weight: var(--dt-font-weight-medium);

    .nav-text {
      font-weight: var(--dt-font-weight-medium);
    }
  }

  .nav-icon-wrapper {
    width: 32px;
    height: 32px;
    border-radius: var(--dt-radius-md);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: var(--dt-spacing-md);
    color: #fff;
    font-size: 16px;
    flex-shrink: 0;

    &.bg-gradient-orange {
      background: linear-gradient(135deg, #FF9A9E 0%, #FECFEF 100%);
    }

    &.bg-gradient-blue {
      background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%);
    }

    &.bg-gradient-green {
      background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%);
    }

    &.bg-gradient-purple {
      background: linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%);
    }

    &.bg-gradient-primary {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    &.bg-gradient-pink {
      background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
    }
  }

  .nav-text {
    flex: 1;
    font-size: var(--dt-font-size-base);
  }

  .nav-badge {
    background: var(--dt-danger-color);
    color: #fff;
    font-size: 10px;
    padding: 2px 6px;
    border-radius: var(--dt-radius-full);
    line-height: 1;
    font-weight: var(--dt-font-weight-medium);
  }

  .nav-count {
    font-size: var(--dt-font-size-xs);
    color: var(--dt-text-tertiary);
    margin-left: auto;
  }

  .nav-dot {
    width: 8px;
    height: 8px;
    border-radius: var(--dt-radius-full);
    background-color: var(--dt-danger-color);
    margin-left: auto;
  }

  // 分组菜单图标
  .group-menu-icon,
  .group-item-menu-icon {
    margin-left: auto;
    padding: 4px;
    border-radius: var(--dt-radius-sm);
    opacity: 0;
    transition: all var(--dt-transition-base);
    color: var(--dt-text-tertiary);

    &:hover {
      background: var(--dt-fill-color);
      color: var(--dt-text-primary);
    }
  }

  &:hover .group-menu-icon,
  &:hover .group-item-menu-icon {
    opacity: 1;
  }
}

// 好友分组列表
.friend-groups-list {
  padding-left: 16px;
  margin-bottom: 8px;

  .nav-item-sub {
    padding-left: 36px;
    font-size: 13px;
  }

  .group-initial {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: 600;
    color: #909399;
  }
}

// ==================== 渐变背景色 ====================
.bg-gradient-light {
  background: linear-gradient(135deg, #e0e7ff 0%, #f3e8ff 100%);
}

// ==================== 推荐内容 ==================== */
.recommend-content {
  padding: var(--dt-spacing-lg);
}

/* ==================== 滚动条优化 ==================== */
.scrollbar-custom {
  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background-color: var(--dt-border-color);
    border-radius: 2px;

    &:hover {
      background-color: var(--dt-text-tertiary);
    }
  }
}

// 分组展开动画
.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  max-height: 300px;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  opacity: 0;
}

.nav-divider {
  height: 1px;
  background: var(--dt-border-color);
  margin: 12px 16px 24px;
}

.org-search-box {
  margin-bottom: 8px;
  padding: 0 12px;

  :deep(.el-input__wrapper) {
    border-radius: var(--dt-radius-sm);
    box-shadow: none;
    background: var(--el-fill-color-light);

    &.is-focus {
      background: #fff;
    }
  }
}

.org-root {
  .arrow-icon {
    font-size: 12px;
    color: var(--dt-text-tertiary);
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
  border-radius: var(--dt-radius-md);

  &:hover {
    background-color: var(--dt-bg-hover);
  }

  &.active {
    background-color: var(--dt-brand-bg);

    .org-name {
      color: var(--dt-brand-color);
      font-weight: 500;
    }
  }

  &.has-children {
    .org-item-content {
      padding-left: 0;
    }
  }

  .org-expand-icon {
    font-size: 12px;
    color: var(--dt-text-tertiary);
    margin-right: 6px;
    transition: transform 0.2s;
    flex-shrink: 0;

    &.expanded {
      transform: rotate(90deg);
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
      background: var(--dt-border-color);
    }

    .org-name {
      font-size: 14px;
      color: var(--dt-text-primary);
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .org-stats {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-left: 8px;
    }

    .org-count {
      font-size: 12px;
      color: var(--dt-text-tertiary);
    }

    .org-online {
      display: flex;
      align-items: center;
      gap: 3px;
      font-size: 11px;
      color: var(--el-color-success);

      .online-dot {
        width: 6px;
        height: 6px;
        border-radius: var(--dt-radius-full);
        background-color: var(--el-color-success);
        animation: pulse 2s infinite;
      }
    }
  }
}

/* ==================== 列表面板 ==================== */
.list-panel {
  width: $list-width;
  border-right: 1px solid var(--dt-border-color);
  display: flex;
  flex-direction: column;
  background: var(--dt-bg-card);
  flex-shrink: 0;
}

.list-header {
  height: 56px;
  padding: 0 var(--dt-spacing-lg);
  border-bottom: 1px solid var(--dt-border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--dt-bg-card);

  .header-left {
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-sm);

    .list-title {
      font-size: var(--dt-font-size-lg);
      font-weight: var(--dt-font-weight-semibold);
      color: var(--dt-text-primary);
      margin: 0;
    }

    :deep(.el-button) {
      color: var(--dt-brand-color);
    }
  }
}

.list-content-wrapper {
  flex: 1;
  position: relative;
  overflow: hidden;
  background: var(--dt-bg-page);
}

.virtual-list {
  height: 100%;
}

.list-item {
  display: flex;
  align-items: center;
  padding: var(--dt-spacing-md) var(--dt-spacing-lg);
  cursor: pointer;
  transition: background-color var(--dt-transition-base);
  border-bottom: 1px solid var(--dt-border-color-lighter);
  background: var(--dt-bg-card);

  &:hover {
    background-color: var(--dt-bg-hover);
  }

  &.active {
    background-color: var(--dt-brand-bg);

    .item-name {
      color: var(--dt-brand-color);
    }
  }

  &.batch-mode {
    padding-left: var(--dt-spacing-md);
  }

  .item-checkbox {
    margin-right: var(--dt-spacing-md);
    flex-shrink: 0;
  }

  .item-info {
    margin-left: var(--dt-spacing-md);
    flex: 1;
    overflow: hidden;

    .item-header {
      display: flex;
      align-items: center;
      margin-bottom: 2px;

      .item-name {
        font-size: var(--dt-font-size-base);
        font-weight: var(--dt-font-weight-medium);
        color: var(--dt-text-primary);
        margin-right: var(--dt-spacing-sm);
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .role-tag {
        font-size: 10px;
        padding: 2px 6px;
        border-radius: var(--dt-radius-sm);
        white-space: nowrap;
        font-weight: var(--dt-font-weight-medium);

        &.owner {
          background: var(--dt-warning-bg);
          color: var(--dt-warning-color);
        }

        &.admin {
          background: var(--dt-brand-bg);
          color: var(--dt-brand-color);
        }
      }
    }

    .item-desc {
      font-size: var(--dt-font-size-sm);
      color: var(--dt-text-secondary);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}

.group-profile-open {
  .nav-item.active {
    background-color: transparent;
    color: var(--dt-text-primary);
    font-weight: normal;

    .nav-text {
      font-weight: normal;
    }
  }

  .org-item.active {
    background-color: transparent;

    .org-name {
      color: var(--dt-text-primary);
      font-weight: normal;
    }
  }

  .list-item.active {
    background-color: var(--dt-bg-card);

    .item-name {
      color: var(--dt-text-primary);
    }
  }
}

/* ==================== 详情面板 ==================== */
.detail-panel {
  flex: 1;
  background: var(--dt-bg-page);
  display: flex;
  flex-direction: column;
  position: relative;
  min-width: $panel-min-width;

  .mobile-detail-header {
    height: 50px;
    background: var(--dt-bg-card);
    border-bottom: 1px solid var(--dt-border-color);
    display: flex;
    align-items: center;
    padding: 0 var(--dt-spacing-md);
    font-weight: var(--dt-font-weight-semibold);
    color: var(--dt-text-primary);
  }
}

.detail-content-wrapper {
  flex: 1;
  height: 100%;
  overflow-y: auto;
  padding: var(--dt-spacing-xl);
}

.empty-selection {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;

  .empty-content {
    text-align: center;
    padding: var(--dt-spacing-xl);

    .empty-bg {
      width: 200px;
      height: 150px;
      margin: 0 auto var(--dt-spacing-xl);
      opacity: 0.6;
      object-fit: contain;
    }

    h3 {
      font-size: var(--dt-font-size-lg);
      font-weight: var(--dt-font-weight-semibold);
      color: var(--dt-text-primary);
      margin-bottom: var(--dt-spacing-sm);
    }

    p {
      color: var(--dt-text-secondary);
      font-size: var(--dt-font-size-base);
      margin-bottom: var(--dt-spacing-lg);
    }

    :deep(.el-button) {
      min-width: 100px;
    }
  }
}

/* ==================== 好友请求 ==================== */
.friend-requests {
  height: 100%;
}

/* ==================== 搜索模态框 ==================== */
.search-modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  z-index: 200;
  backdrop-filter: blur(4px);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 15vh;
}

.search-modal {
  width: 560px;
  max-width: 90vw;
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-xl);
  box-shadow: var(--dt-shadow-dialog);
  display: flex;
  flex-direction: column;
  max-height: 70vh;
  animation: modal-in 0.3s ease;
  overflow: hidden;

  .search-header {
    padding: var(--dt-spacing-lg);
    border-bottom: 1px solid var(--dt-border-color);
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-md);

    :deep(.el-input__wrapper) {
      box-shadow: none;
    }

    .close-search-btn {
      font-size: 20px;
      color: var(--dt-text-tertiary);

      &:hover {
        color: var(--dt-text-primary);
      }
    }
  }

  .search-body {
    flex: 1;
    overflow-y: auto;
    padding: var(--dt-spacing-md) 0;
    min-height: 200px;
  }
}

.search-results-list {
  padding: 0 var(--dt-spacing-md);
}

.search-result-item {
  display: flex;
  align-items: center;
  padding: var(--dt-spacing-md);
  cursor: pointer;
  transition: all var(--dt-transition-base);
  border-radius: var(--dt-radius-md);
  margin-bottom: var(--dt-spacing-xs);

  &:hover {
    background: var(--dt-bg-hover);

    .arrow-right {
      opacity: 1;
      transform: translateX(0);
    }
  }

  .result-info {
    flex: 1;
    margin-left: var(--dt-spacing-md);
    display: flex;
    flex-direction: column;
    gap: 2px;

    .result-name {
      font-weight: var(--dt-font-weight-medium);
      color: var(--dt-text-primary);
    }

    .result-type-tag {
      font-size: var(--dt-font-size-xs);
      color: var(--dt-text-secondary);
      background: var(--dt-fill-color);
      padding: 2px 8px;
      border-radius: var(--dt-radius-sm);
      align-self: flex-start;

      &.group {
        color: var(--dt-brand-color);
        background: var(--dt-brand-bg);
      }

      &.contact {
        color: var(--dt-success-color);
        background: var(--dt-success-bg);
      }
    }
  }

  .arrow-right {
    color: var(--dt-text-tertiary);
    opacity: 0;
    transform: translateX(-4px);
    transition: all var(--dt-transition-base);
  }
}

.empty-search {
  padding: var(--dt-spacing-xl) 0;
}

/* ==================== 动画效果 ==================== */
@keyframes modal-in {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes pulse {

  0%,
  100% {
    opacity: 1;
  }

  50% {
    opacity: 0.4;
  }
}

@keyframes slide-in-right {
  from {
    opacity: 0;
    transform: translateX(20px);
  }

  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--dt-transition-base);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.expand-enter-active,
.expand-leave-active {
  transition: all var(--dt-transition-base);
  max-height: 500px;
  opacity: 1;
}

.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  opacity: 0;
}

/* ==================== 响应式布局 ==================== */
@media (max-width: 1400px) {
  .sidebar {
    width: 200px;
  }

  .list-panel {
    width: 240px;
  }
}
</style>
