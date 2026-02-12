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
          <el-dropdown
            trigger="click"
            placement="bottom-end"
            @command="handleAddMenuCommand"
          >
            <el-button
              circle
              size="small"
              :icon="Plus"
            />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="addFriend">
                  <el-icon><User /></el-icon>
                  添加联系人
                </el-dropdown-item>
                <el-dropdown-item command="createGroup">
                  <el-icon><ChatDotSquare /></el-icon>
                  发起群聊
                </el-dropdown-item>
                <el-dropdown-item command="joinGroup">
                  <el-icon><Search /></el-icon>
                  加入群组
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
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
      <el-scrollbar class="sidebar-nav-scroll">
        <nav class="nav-list">
          <!-- 主导航：不需要分组标题，直接列出 -->
          <div class="nav-group nav-group--primary">
            <div
              class="nav-item"
              :class="{ active: currentNav === 'new' }"
              @click="switchNav('new')"
            >
              <div class="nav-icon-wrapper bg-gradient-orange">
                <el-icon>
                  <UserFilled />
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
                  <StarFilled />
                </el-icon>
              </div>
              <span class="nav-text">可能认识的人</span>
              <span
                v-if="recommendedCount > 0"
                class="nav-dot"
              />
            </div>
          </div>

          <div class="nav-divider" />

          <!-- 联系人 & 群组 -->
          <div class="nav-group">
            <div class="nav-group-title">
              联系人
            </div>

            <div
              class="nav-item"
              :class="{ active: currentNav === 'all' }"
              @click="switchNav('all')"
            >
              <div class="nav-icon-wrapper bg-gradient-blue">
                <el-icon>
                  <OfficeBuilding />
                </el-icon>
              </div>
              <span class="nav-text">全部联系人</span>
              <span class="nav-count">{{ allUsers.length }}</span>
            </div>

            <div
              class="nav-item"
              :class="{ active: currentNav === 'friends' }"
              @click="switchNav('friends')"
            >
              <div class="nav-icon-wrapper bg-gradient-green">
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
                      <el-icon><Plus /></el-icon>
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
                          <el-icon><EditPen /></el-icon>
                          重命名
                        </el-dropdown-item>
                        <el-dropdown-item
                          command="delete"
                          divided
                        >
                          <el-icon><Delete /></el-icon>
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
              <div class="nav-icon-wrapper bg-gradient-teal">
                <el-icon>
                  <ChatLineRound />
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

              <!-- 部门树 -->
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
      </el-scrollbar>
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
          <el-tooltip
            content="刷新"
            placement="bottom"
          >
            <el-button
              link
              :icon="Refresh"
              @click="refreshCurrent"
            />
          </el-tooltip>
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
        ref="listContentRef"
        v-loading="loading"
        class="list-content-wrapper"
      >
        <div
          v-if="currentNav === 'recommended'"
          class="recommend-content"
        >
          <el-scrollbar>
            <RecommendedContacts
              ref="recommendedContactsRef"
              @update="handleRecommendUpdate"
            />
          </el-scrollbar>
        </div>

        <div
          v-else-if="currentNav === 'new'"
          class="friend-requests"
        >
          <el-scrollbar>
            <NewFriendsView
              ref="newFriendsViewRef"
              @update-count="pendingCount = $event"
            />
          </el-scrollbar>
        </div>

        <!-- 通用列表 (好友/群组/部门成员) -->
        <VirtualList
          v-else-if="currentList.length > 0"
          class="virtual-list"
          :items="currentList"
          :item-size="56"
          :height="virtualListHeight"
          :class="{ 'batch-mode': batchMode }"
        >
          <template #default="{ item }">
            <div
              class="list-item"
              :class="{ active: selectedItemId === getItemId(item), 'batch-mode': batchMode }"
              @click="handleItemClick(item)"
            >
              <!-- 批量选择复选框 -->
              <el-checkbox
                v-if="batchMode"
                :model-value="selectedContacts.has(getItemId(item))"
                class="item-checkbox"
                @change="(val) => toggleContactSelection(getItemId(item), val)"
                @click.stop
              />

              <DingtalkAvatar
                :src="getItemAvatar(item)"
                :name="getItemName(item)"
                :size="36"
                :shape="currentNav === 'groups' ? 'square' : 'circle'"
              />
              <div class="item-info">
                <div class="item-header">
                  <span class="item-name">{{ getItemName(item) }}</span>
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
          >
            <el-button
              type="primary"
              size="small"
              @click="refreshCurrent"
            >
              <el-icon class="mr-1">
                <Refresh />
              </el-icon>
              刷新重试
            </el-button>
          </el-empty>
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
          v-if="isFriendNav || currentNav === 'org'"
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
                  >{{ item.type === 'group' ? '群组' : item.type === 'dept' ? '部门' : '联系人' }}</span>
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

    <!-- 创建群聊对话框 -->
    <CreateGroupDialog
      v-model="showCreateGroupDialog"
      @success="handleCreateGroupSuccess"
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
import { useDebounceFn, useResizeObserver } from '@vueuse/core'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import ContactDetail from '@/components/Contacts/ContactDetail.vue'
import VirtualList from '@/components/Common/VirtualList.vue'
import RecommendedContacts from '@/components/Contacts/RecommendedContacts.vue'
import BatchOperationBar from '@/components/Contacts/BatchOperationBar.vue'
import GroupSelectDialog from '@/components/Contacts/GroupSelectDialog.vue'
import AddFriendDialog from '@/components/Contacts/AddFriendDialog.vue'
import CreateGroupDialog from '@/components/CreateGroupDialog/index.vue'
import NewFriendsView from '@/components/Contacts/NewFriendsView.vue'
import { getFriendRequests, getGroupedFriendList, getGroupList, createGroup, renameGroup, deleteGroup, clearFriendListCache, batchMoveToGroup, batchDeleteContacts, batchAddFriends as batchAddFriendsApi } from '@/api/im/contact'
import { getGroups } from '@/api/im/group'
import { getOrgTree, getDepartmentMembers, searchOrgMembers } from '@/api/im/organization'
import { getAllUsers } from '@/api/im/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, User, Avatar, ChatDotSquare, OfficeBuilding,
  ArrowRight, Plus, Close, MoreFilled, Star, Refresh,
  UserFilled, StarFilled, ChatLineRound, EditPen, Delete
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
const showCreateGroupDialog = ref(false)
const loading = ref(false)
const searchInputRef = ref(null)
const recommendedContactsRef = ref(null)
const newFriendsViewRef = ref(null)
const listContentRef = ref(null)
const virtualListHeight = ref(400)

// Data
const friendGroups = ref([])
const friendGroupNames = ref([]) // 好友分组名称列表
const groupList = ref([])
const orgMembers = ref([])
const allUsers = ref([]) // 公司所有用户
const friendRequests = ref(null)
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

// 创建群聊成功处理
const handleCreateGroupSuccess = () => {
  fetchData('groups')
}

// "+"号下拉菜单命令处理
const handleAddMenuCommand = command => {
  if (command === 'addFriend') {
    showAddFriendDialog.value = true
  } else if (command === 'createGroup') {
    showCreateGroupDialog.value = true
  } else if (command === 'joinGroup') {
    ElMessageBox.prompt('请输入群号', '加入群组', {
      confirmButtonText: '加入',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '请输入群号'
    }).then(({ value }) => {
      store.dispatch('im/session/createAndSwitchSession', {
        type: 'GROUP',
        targetId: value.trim()
      }).then(() => {
        ElMessage.success('已加入群组')
        emit('switch-module', 'chat')
      }).catch(error => {
        ElMessage.error('加入群组失败：' + (error.message || '请检查群号是否正确'))
      })
    }).catch(() => {})
  }
}

// Selection
const selectedItemId = ref(null)
const selectedItem = ref(null)
const groupProfileOpen = ref(false)

// Org Tree
const orgExpanded = ref(true)
const selectedDeptId = ref(null)
const orgTreeData = ref([]) // 原始树形数据
const flatDepts = ref([]) // 扁平化后的显示数据

// 组织架构搜索
const orgSearchQuery = ref('')
const orgSearchResults = ref([])
const orgSearching = ref(false)
const fetchSeq = ref(0)
const cache = ref({
  friends: { ts: 0 },
  groups: { ts: 0 },
  new: { ts: 0 },
  all: { ts: 0 }
})

const getItemId = item => {
  // 优先使用friendId（好友），其次是id，然后是groupId
  // 确保与去重逻辑使用相同的ID字段
  return item?.friendId ?? item?.id ?? item?.userId ?? item?.groupId ?? null
}

const getItemName = item => {
  return item?.remark || item?.friendName || item?.name || item?.groupName || item?.nickname || item?.username || ''
}

const getItemAvatar = item => {
  return item?.avatar || item?.friendAvatar || item?.headImg || item?.groupAvatar || ''
}

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

const isFriendNav = computed(() => {
  if (currentNav.value === 'friends') { return true }
  return friendGroups.value.some(g => g.groupName === currentNav.value)
})

const currentList = computed(() => {
  if (currentNav.value === 'all') {
    return allUsers.value
  }
  if (currentNav.value === 'friends') {
    // 全部好友 - 重点加固：处理后端返回的多种可能的结构
    const rawGroups = friendGroups.value || []
    
    // 如果返回的不是列表而是对象，尝试提取数据
    const normalizedGroups = Array.isArray(rawGroups) ? rawGroups : (rawGroups.data || [])
    
    const allFriends = normalizedGroups.flatMap(g => {
      // 兼顾后端可能返回的不同字段名
      return g.friends || g.contacts || (Array.isArray(g) ? g : [])
    })
    
    // 如果全部分组展平后为空，且 rawGroups 本身就是好友列表
    if (allFriends.length === 0 && normalizedGroups.length > 0 && !normalizedGroups[0].groupName) {
      return normalizedGroups
    }

    const uniqueMap = new Map()
    allFriends.forEach(friend => {
      if (!friend) {return}
      const id = friend.friendId || friend.id || friend.userId
      if (id) {
        uniqueMap.set(id, friend)
      }
    })
    return Array.from(uniqueMap.values())
  }
  // 检查是否是分组导航
  const group = friendGroups.value.find(g => g.groupName === currentNav.value)
  if (group) {
    return group.friends || group.contacts || []
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
  if (nav === 'recommended') { recommendedCount.value = 0 }
  fetchData(nav)
}

const toggleOrg = () => {
  orgExpanded.value = !orgExpanded.value
}

// 递归构建扁平化的部门树（只包含展开的节点）
const buildFlatDepts = (depts, level = 0, result = []) => {
  if (!depts || !Array.isArray(depts)) {return result}

  depts.forEach(dept => {
    // 添加当前部门
    result.push({
      ...dept,
      level,
      hasChildren: dept.children && dept.children.length > 0
    })

    // 如果有子部门且已展开，递归添加
    if (dept.children && dept.children.length > 0 && dept.expanded) {
      buildFlatDepts(dept.children, level + 1, result)
    }
  })

  return result
}

// 递归查找并更新部门展开状态
const toggleDeptExpanded = (depts, deptId) => {
  if (!depts || !Array.isArray(depts)) {return false}

  for (const dept of depts) {
    if (dept.id === deptId) {
      // 找到目标部门，切换展开状态
      dept.expanded = !dept.expanded
      return true
    }
    // 递归查找子部门
    if (dept.children && dept.children.length > 0) {
      if (toggleDeptExpanded(dept.children, deptId)) {
        return true
      }
    }
  }
  return false
}

// 从原始树数据重新计算扁平列表
const refreshFlatDepts = () => {
  flatDepts.value = buildFlatDepts(orgTreeData.value)
}

const selectDept = async dept => {
  // 如果点击的是有子节点的部门，切换展开/折叠状态
  if (dept.hasChildren) {
    toggleDeptExpanded(orgTreeData.value, dept.id)
    // 重新计算扁平化的部门树
    refreshFlatDepts()
  }

  // 加载部门成员
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
  selectedItemId.value = getItemId(item)
  selectedItem.value = item
}

const handleSearch = useDebounceFn(() => {
  const q = searchQuery.value?.trim()
  if (!q) {
    searchResults.value = []
    return
  }

  const keyword = q.toLowerCase()
  const results = []

  const pushUnique = item => {
    const id = getItemId(item)
    if (!id) { return }
    if (results.some(r => getItemId(r) === id && r.type === item.type)) { return }
    results.push(item)
  }

  const friendIdSet = new Set()
  friendGroups.value.forEach(g => {
    const friends = g.friends || g.contacts || []
    friends.forEach(friend => {
      if (friend.friendId) { friendIdSet.add(friend.friendId) }
      const hay = [
        friend.remark,
        friend.friendName,
        friend.nickname,
        friend.name,
        friend.username,
        friend.pinyin
      ].filter(Boolean).join(' ').toLowerCase()
      if (hay.includes(keyword)) {
        pushUnique({
          ...friend,
          type: 'friend',
          name: getItemName(friend),
          avatar: getItemAvatar(friend)
        })
      }
    })
  })

  groupList.value.forEach(group => {
    const hay = [group.name, group.groupName, group.notice, group.description].filter(Boolean).join(' ').toLowerCase()
    if (hay.includes(keyword)) {
      pushUnique({
        ...group,
        type: 'group',
        isGroup: true,
        name: group.name || group.groupName,
        avatar: group.avatar || group.groupAvatar
      })
    }
  })

  if (results.length < 30 && allUsers.value.length > 0) {
    for (const user of allUsers.value) {
      if (results.length >= 50) { break }
      if (friendIdSet.has(user.id)) { continue }
      const hay = [user.nickname, user.name, user.username, user.phone, user.email].filter(Boolean).join(' ').toLowerCase()
      if (hay.includes(keyword)) {
        pushUnique({
          ...user,
          type: 'user',
          name: user.nickname || user.name || user.username,
          avatar: user.avatar
        })
      }
    }
  }

  searchResults.value = results.slice(0, 50)
}, 300)

const shouldUseCache = nav => {
  const entry = cache.value[nav]
  if (!entry?.ts) { return false }
  if (Date.now() - entry.ts > 30 * 1000) { return false }
  if (nav === 'friends') { return friendGroups.value.length > 0 }
  if (nav === 'groups') { return groupList.value.length > 0 }
  if (nav === 'new') { return friendRequests.value !== null }
  if (nav === 'all') { return allUsers.value.length > 0 }
  return false
}

const fetchData = async (nav, options = {}) => {
  const force = Boolean(options.force)
  // 如果是强制加载，则跳过缓存检查
  if (!force && shouldUseCache(nav)) { return }

  const seq = ++fetchSeq.value
  loading.value = true
  try {
    if (nav === 'friends') {
      if (force) {
        await clearFriendListCache().catch(() => null)
      }
      const res = await getGroupedFriendList()
      friendGroups.value = (res.data || []).map(g => {
        if (!g) { return g }
        const contacts = g.contacts || g.friends || []
        return { ...g, contacts, friends: contacts }
      })
      cache.value.friends.ts = Date.now()
    } else if (nav === 'groups') {
      const res = await getGroups()
      groupList.value = res.data || []
      cache.value.groups.ts = Date.now()
    } else if (nav === 'new') {
      const res = await getFriendRequests()
      friendRequests.value = res.data || []
      pendingCount.value = (res.data || []).filter(r => r.status === 'PENDING').length
      cache.value.new.ts = Date.now()
    } else if (nav === 'all') {
      const res = await getAllUsers()
      allUsers.value = res.data || []
      cache.value.all.ts = Date.now()
    }
  } catch (error) {
    console.error(error)
  } finally {
    if (seq === fetchSeq.value) {
      loading.value = false
    }
  }
}

const handleMessage = contact => {
  // 切换到聊天模块并打开与该联系人的会话
  emit('switch-module', 'chat')

  // 需要创建或切换到与该联系人的会话
  const isGroup = Boolean(contact?.isGroup || contact?.type === 'group')
  const payload = isGroup
    ? { type: 'GROUP', targetId: contact?.id ?? contact?.groupId }
    : { type: 'PRIVATE', targetId: contact?.friendId ?? contact?.id }

  if (!payload.targetId) { return }

  store.dispatch('im/session/createAndSwitchSession', payload).catch(error => {
    console.error('创建会话失败:', error)
  })
}

const handleVoiceCall = contact => {
  // 触发语音通话
  const targetUserId = contact?.friendId ?? contact?.id
  if (!targetUserId) { return }
  emit('voice-call', {
    userId: targetUserId,
    userName: getItemName(contact),
    userAvatar: getItemAvatar(contact)
  })
}

const handleVideoCall = contact => {
  // 触发视频通话
  const targetUserId = contact?.friendId ?? contact?.id
  if (!targetUserId) { return }
  emit('video-call', {
    userId: targetUserId,
    userName: getItemName(contact),
    userAvatar: getItemAvatar(contact)
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
    const id = getItemId(item)
    if (!id) { return }
    toggleContactSelection(id, !selectedContacts.value.has(id))
  } else {
    selectItem(item)
  }
}

const handleGroupProfileToggle = value => {
  groupProfileOpen.value = value
}

// 全选/取消全选 - 包装composable方法
const handleSelectAll = selected => {
  batchSelectAll(currentList.value, selected, getItemId)
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
    await batchMoveToGroup({ contactIds: ids, groupName })
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
    await batchDeleteContacts(ids)
    fetchData(currentNav.value)
  })
}

// 批量添加好友
const handleBatchAddFriends = async () => {
  await batchAddFriends(async (ids, reason) => {
    // 调用批量添加API
    await batchAddFriendsApi({ userIds: ids, remark: reason })
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
  fetchData('friends', { force: true })
  loadFriendGroupNames()
  // 预加载所有用户，用于显示数量
  getAllUsers().then(res => {
    allUsers.value = res.data || []
    cache.value.all.ts = Date.now()
  })
  // Load org tree
  getOrgTree().then(res => {
    orgTreeData.value = res.data || []
    flatDepts.value = buildFlatDepts(orgTreeData.value)
  })
})

const updateVirtualListHeight = () => {
  if (!listContentRef.value) { return }
  const h = listContentRef.value.clientHeight
  if (h > 0) { virtualListHeight.value = h }
}

useResizeObserver(listContentRef, () => {
  updateVirtualListHeight()
})

watch([currentNav, loading], () => {
  nextTick(() => updateVirtualListHeight())
})

const refreshCurrent = async () => {
  if (currentNav.value === 'recommended') {
    recommendedContactsRef.value?.refresh?.()
    return
  }
  if (currentNav.value === 'new') {
    newFriendsViewRef.value?.refresh?.()
    await fetchData('new', { force: true })
    return
  }
  if (currentNav.value === 'org') {
    if (selectedDeptId.value) {
      loading.value = true
      try {
        const res = await getDepartmentMembers(selectedDeptId.value)
        orgMembers.value = res.data || []
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
      return
    }
    return
  }
  if (currentNav.value === 'friends' || isFriendNav.value) {
    await fetchData('friends', { force: true })
    return
  }
  await fetchData(currentNav.value, { force: true })
}

watch(showSearchPanel, val => {
  if (val) {
    nextTick(() => searchInputRef.value?.focus())
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// ==================== 布局变量 ====================
$sidebar-width: 220px;
$list-width: 260px;
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
      background-color: var(--dt-scrollbar-thumb);
      border-radius: 3px;

      &:hover {
        background-color: var(--dt-scrollbar-thumb-hover);
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
      box-shadow: var(--dt-shadow-lg);

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
      background: var(--dt-bg-card);

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
  background: var(--dt-overlay);
  z-index: 90;
  backdrop-filter: blur(2px);
}

/* ==================== 侧边栏 ==================== */
.sidebar {
  width: $sidebar-width;
  background: var(--dt-bg-session-list);
  border-right: 1px solid var(--dt-border-color);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 12px 12px 8px;
  border-bottom: 1px solid transparent;

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    .sidebar-title {
      font-size: 16px;
      font-weight: 600;
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
    border-radius: 6px;
    padding: 8px 12px;
    display: flex;
    align-items: center;
    cursor: pointer;
    transition: all var(--dt-transition-base);

    &:hover {
      background: var(--dt-fill-color-dark);
    }

    .search-icon {
      color: var(--dt-text-tertiary);
      margin-right: 8px;
      font-size: 14px;
    }

    .placeholder {
      font-size: 13px;
      color: var(--dt-text-tertiary);
    }
  }
}

/* ==================== 导航列表 ==================== */
.nav-list {
  flex: 1;
  overflow-y: auto;
  padding: 4px 8px;
}

.nav-group {
  margin-bottom: 4px;

  // 无标题的主导航组，紧凑排列
  &--primary {
    margin-bottom: 0;
  }
}

.nav-group-title {
  padding: 6px 10px 4px;
  font-size: 11px;
  font-weight: 500;
  color: var(--dt-text-quaternary);
  letter-spacing: 0.3px;
}

.nav-item {
  display: flex;
  align-items: center;
  height: 36px;
  padding: 0 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
  color: var(--dt-text-primary);
  margin-bottom: 1px;
  position: relative;

  &:hover {
    background-color: var(--dt-bg-subtle-hover);
  }

  &.active {
    background-color: var(--dt-brand-bg);
    color: var(--dt-brand-color);

    .nav-text {
      font-weight: 600;
    }
  }

  // 钉钉风格：浅色底 + 彩色图标（非饱和渐变）
  .nav-icon-wrapper {
    width: 28px;
    height: 28px;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 10px;
    font-size: 15px;
    flex-shrink: 0;
    transition: all 0.2s;

    &.bg-gradient-orange { background: #FFF3E0; color: #F57C00; }
    &.bg-gradient-pink { background: #FFEBEE; color: #E53935; }
    &.bg-gradient-blue { background: var(--dt-brand-bg); color: var(--dt-brand-color); }
    &.bg-gradient-primary { background: var(--dt-brand-bg); color: var(--dt-brand-color); }
    &.bg-gradient-green { background: #E8F5E9; color: #2E7D32; }
    &.bg-gradient-teal { background: #E0F7FA; color: #00838F; }
    &.bg-gradient-purple { background: #F3E5F5; color: #7B1FA2; }
    &.bg-gradient-light {
      background: var(--dt-fill-color);
      color: var(--dt-text-secondary);
      .group-initial { font-size: 12px; font-weight: 600; }
    }
  }

  .nav-text {
    flex: 1;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    font-size: 13px;
  }

  .nav-badge {
    background: var(--dt-error-color);
    color: #fff;
    font-size: 10px;
    min-width: 16px;
    height: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    font-weight: 600;
    padding: 0 4px;
  }

  .nav-count {
    font-size: 11px;
    color: var(--dt-text-quaternary);
    margin-left: 6px;
  }

  .nav-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background-color: var(--dt-error-color);
    margin-left: 6px;
  }

  .group-menu-icon,
  .group-item-menu-icon {
    opacity: 0;
    transition: all 0.2s;
    color: var(--dt-text-quaternary);
    margin-left: 4px;

    &:hover {
      color: var(--dt-brand-color);
    }
  }

  &:hover .group-menu-icon,
  &:hover .group-item-menu-icon {
    opacity: 1;
  }
}

.friend-groups-list {
  padding-left: 4px;
  margin-bottom: 4px;

  .nav-item-sub {
    padding-left: 38px;
    font-size: 13px;
    margin-bottom: 0;

    &::before {
      content: '';
      position: absolute;
      left: 18px;
      top: 50%;
      width: 10px;
      height: 1px;
      background: var(--dt-border-light);
    }
  }

  .bg-gradient-light {
    background: var(--dt-brand-bg);
    color: var(--dt-brand-color);
    .group-initial { color: var(--dt-brand-color); font-weight: 600; }
  }
}

.nav-divider {
  height: 1px;
  background: var(--dt-border-light);
  margin: 4px 10px;
}

/* ==================== 组织架构 ==================== */
.org-search-box {
  margin-bottom: 4px;
  padding: 0 4px;

  :deep(.el-input__wrapper) {
    background: var(--dt-bg-card);
    border-radius: 6px;
  }
}

.org-tree-container {
  .arrow-icon {
    font-size: 12px;
    color: var(--dt-text-quaternary);
    margin-right: 4px;
    transition: transform 0.2s;

    &.rotated {
      transform: rotate(90deg);
    }
  }
}

.org-item {
  padding: 6px 12px;
  margin: 1px 4px;
  cursor: pointer;
  transition: all 0.2s;
  border-radius: 6px;

  &:hover {
    background-color: var(--dt-bg-subtle-hover);
  }

  &.active {
    background-color: var(--dt-brand-bg);
    .org-name { color: var(--dt-brand-color); font-weight: 600; }
  }

  .org-expand-icon {
    font-size: 12px;
    color: var(--dt-text-quaternary);
    margin-right: 6px;
    transition: transform 0.2s;

    &.expanded { transform: rotate(90deg); }
  }

  .org-item-content {
    display: flex;
    align-items: center;
    position: relative;

    .org-name {
      font-size: 13px;
      color: var(--dt-text-primary);
      flex: 1;
    }

    .org-stats {
      font-size: 11px;
      color: var(--dt-text-quaternary);
      margin-left: 8px;
    }

    .org-online {
      display: flex;
      align-items: center;
      gap: 3px;
      color: var(--dt-success-color);
      font-weight: 500;

      .online-dot {
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background-color: var(--dt-success-color);
      }
    }
  }
}

/* ==================== 列表面板 ==================== */
.list-panel {
  width: $list-width;
  border-right: 1px solid var(--dt-border-light);
  display: flex;
  flex-direction: column;
  background: var(--dt-bg-card);
  flex-shrink: 0;
}

.list-header {
  height: 48px;
  padding: 0 14px;
  border-bottom: 1px solid var(--dt-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;

  .list-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0;
  }
}

.list-content-wrapper {
  flex: 1;
  overflow: hidden;
  background: var(--dt-bg-body);
}

.list-item {
  display: flex;
  align-items: center;
  padding: 0 14px;
  height: 56px;
  cursor: pointer;
  transition: all 0.15s;
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-lighter);
  position: relative;

  &:hover {
    background-color: var(--dt-bg-subtle-hover);
  }

  &.active {
    background-color: var(--dt-bg-session-active);

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 20%;
      height: 60%;
      width: 3px;
      background: var(--dt-brand-color);
      border-radius: 0 3px 3px 0;
    }

    .item-name {
      color: var(--dt-brand-color);
      font-weight: 600;
    }
  }

  .item-info {
    margin-left: 10px;
    flex: 1;
    min-width: 0;

    .item-header {
      display: flex;
      align-items: center;
      margin-bottom: 2px;

      .item-name {
        font-size: 13px;
        font-weight: 500;
        color: var(--dt-text-primary);
        margin-right: 6px;
      }

      .role-tag {
        font-size: 10px;
        padding: 1px 5px;
        border-radius: 3px;
        font-weight: 500;

        &.owner { background: #FEF3C7; color: #D97706; }
        &.admin { background: #E0F2FE; color: #0369A1; }
      }
    }

    .item-desc {
      font-size: 12px;
      color: var(--dt-text-quaternary);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}

/* ==================== 详情面板 ==================== */
.detail-panel {
  flex: 1;
  background: var(--dt-bg-card);
  display: flex;
  flex-direction: column;
  position: relative;
  min-width: $panel-min-width;
}

.detail-content-wrapper {
  flex: 1;
  height: 100%;
  overflow-y: auto;
}

.empty-selection {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  background: var(--dt-bg-page);

  .empty-content {
    text-align: center;
    opacity: 0.8;

    .empty-bg {
      width: 180px;
      margin-bottom: 24px;
    }

    h3 { font-size: 18px; color: var(--dt-text-primary); margin-bottom: 8px; }
    p { color: var(--dt-text-quaternary); font-size: 14px; }
  }
}

/* ==================== 搜索模态框 ==================== */
.search-modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.2);
  z-index: 200;
  backdrop-filter: blur(8px);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 10vh;
}

.search-modal {
  width: 600px;
  background: var(--dt-bg-card);
  border-radius: 12px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  max-height: 75vh;
  animation: modal-in 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  overflow: hidden;

  .search-header {
    padding: 16px 20px;
    border-bottom: 1px solid var(--dt-border-light);
    display: flex;
    align-items: center;
    gap: 12px;

    :deep(.el-input__wrapper) {
      box-shadow: none;
      font-size: 15px;
    }

    .close-search-btn {
      color: var(--dt-text-quaternary);
      &:hover { color: var(--dt-error-color); }
    }
  }
}

.search-result-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  cursor: pointer;
  transition: all 0.2s;
  border-radius: 8px;
  margin: 4px 12px;

  &:hover {
    background: var(--dt-bg-subtle-hover);
    .arrow-right { transform: translateX(0); opacity: 1; }
  }
  
  .result-info {
    margin-left: 12px;
    flex: 1;
    .result-name { font-weight: 600; color: var(--dt-text-primary); }
    .result-type-tag { font-size: 11px; padding: 2px 8px; border-radius: 4px; margin-top: 2px; display: inline-block; }
  }
}

@keyframes modal-in {
  from { opacity: 0; transform: translateY(-30px) scale(0.96); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.dark {
  .sidebar, .list-panel, .detail-panel { background: var(--dt-bg-card-dark); border-color: var(--dt-border-dark); }
  .nav-item:hover, .org-item:hover, .list-item:hover { background: var(--dt-bg-hover-dark); }
  .nav-text, .org-name, .item-name, .sidebar-title, h3 { color: var(--dt-text-primary-dark); }
  .search-modal { background: var(--dt-bg-card-dark); }
  .search-header { border-color: var(--dt-border-dark); }

  // 暗色模式下导航图标使用降低透明度的背景
  .nav-icon-wrapper {
    &.bg-gradient-orange { background: rgba(245, 124, 0, 0.15); color: #FFB74D; }
    &.bg-gradient-pink { background: rgba(229, 57, 53, 0.15); color: #EF9A9A; }
    &.bg-gradient-blue { background: rgba(var(--dt-brand-color-rgb, 64, 158, 255), 0.15); }
    &.bg-gradient-primary { background: rgba(var(--dt-brand-color-rgb, 64, 158, 255), 0.15); }
    &.bg-gradient-green { background: rgba(46, 125, 50, 0.15); color: #81C784; }
    &.bg-gradient-teal { background: rgba(0, 131, 143, 0.15); color: #80DEEA; }
    &.bg-gradient-purple { background: rgba(123, 31, 162, 0.15); color: #CE93D8; }
  }

  .role-tag {
    &.owner { background: rgba(217, 119, 6, 0.15) !important; }
    &.admin { background: rgba(3, 105, 161, 0.15) !important; }
  }
}
</style>
