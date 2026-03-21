<template>
  <div class="contacts-panel-v2">
    <!-- 1. 中栏：分类导航与组织树 (280px) -->
    <aside class="contacts-sidebar">
      <div class="sidebar-header">
        <div class="search-wrap">
          <el-icon class="search-icon">
            <Search />
          </el-icon>
          <input v-model="sidebarSearch" placeholder="搜索联系人/部门" class="inner-input">
        </div>
      </div>

      <div class="sidebar-scroll custom-scrollbar">
        <!-- 固定分类 -->
        <div class="nav-list">
          <div class="nav-item" :class="{ active: view === 'new' }" @click="switchView('new')">
            <div class="icon-box orange">
              <el-icon><UserFilled /></el-icon>
            </div>
            <span>新的联系人</span>
          </div>
          <div class="nav-item" :class="{ active: view === 'groups' }" @click="switchView('groups')">
            <div class="icon-box blue">
              <el-icon><Menu /></el-icon>
            </div>
            <span>我的群组</span>
          </div>
          <div class="nav-item" :class="{ active: view === 'friends' }" @click="switchView('friends')">
            <div class="icon-box green">
              <el-icon><StarFilled /></el-icon>
            </div>
            <span>我的好友</span>
          </div>
          <div class="nav-item" :class="{ active: view === 'external' }" @click="switchView('external')">
            <div class="icon-box purple">
              <el-icon><Link /></el-icon>
            </div>
            <span>外部联系人</span>
          </div>
          <div class="nav-item" :class="{ active: view === 'frequent' }" @click="switchView('frequent')">
            <div class="icon-box cyan">
              <el-icon><Clock /></el-icon>
            </div>
            <span>常用联系人</span>
          </div>
        </div>

        <div class="tree-divider">
          组织架构
        </div>

        <!-- 组织树组件 -->
        <div class="org-tree-wrap">
          <OrganizationTree @select="handleDeptSelect" />
        </div>
      </div>
    </aside>

    <!-- 2. 主栏：动态内容区 (面包屑 + 混合列表) -->
    <main class="contacts-main">
      <!-- 只有在组织架构视图下才显示面包屑 -->
      <header v-if="view === 'dept'" class="main-header">
        <div class="breadcrumb-container">
          <el-breadcrumb separator-icon="ArrowRight">
            <el-breadcrumb-item @click="handleBreadcrumbClick(null)">
              公司通讯录
            </el-breadcrumb-item>
            <el-breadcrumb-item
              v-for="node in breadcrumbs"
              :key="node.id"
              @click="handleBreadcrumbClick(node)"
            >
              {{ node.name }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-actions">
          <el-button
            :icon="Plus"
            type="primary"
            plain
            size="small"
          >
            邀请成员
          </el-button>
        </div>
      </header>

      <div class="main-content-scroller custom-scrollbar">
        <!-- A. 组织架构混合视图 -->
        <template v-if="view === 'dept'">
          <!-- 下级部门列表 (文件夹样式) -->
          <div v-if="subDepartments.length > 0" class="sub-dept-list">
            <div
              v-for="dept in subDepartments"
              :key="dept.id"
              class="dept-folder-card"
              @click="handleDeptSelect(dept)"
            >
              <div class="folder-icon">
                <el-icon><FolderOpened /></el-icon>
              </div>
              <div class="folder-info">
                <span class="folder-name">{{ dept.name }}</span>
                <span class="folder-count">{{ dept.memberCount || 0 }}人</span>
              </div>
              <el-icon class="arrow">
                <ArrowRight />
              </el-icon>
            </div>
          </div>

          <!-- 成员列表 (网格卡片样式) -->
          <div v-if="subDepartments.length > 0 && deptMembers.length > 0" class="section-divider">
            <span>直属成员</span>
          </div>

          <div v-if="deptMembers.length > 0" class="members-grid">
            <div
              v-for="m in deptMembers"
              :key="m.id"
              class="member-card-ding"
              @click="handleMemberClick(m)"
            >
              <DingtalkAvatar
                :src="m.avatar"
                :name="m.name || m.userName"
                :user-id="m.id"
                :size="44"
                shape="square"
              />
              <div class="m-body">
                <div class="m-name">
                  {{ m.name || m.userName }}
                </div>
                <div class="m-position">
                  {{ m.position || '成员' }}
                </div>
              </div>
              <button class="chat-shortcut" @click.stop="handleChat(m)">
                <el-icon><ChatDotRound /></el-icon>
              </button>
            </div>
          </div>

          <el-empty v-if="subDepartments.length === 0 && deptMembers.length === 0" description="该目录下暂无内容" />
        </template>

        <!-- B. 其他视图 (复用统一逻辑) -->
        <NewFriendsView v-else-if="view === 'new'" />
        <GroupsView v-else-if="view === 'groups'" @select-group="handleGroupSelect" />
        <ExternalContactsView v-else-if="view === 'external'" />
        <FrequentContactsView v-else-if="view === 'frequent'" @select-member="handleMemberClick" />
        <div v-else-if="view === 'friends'" class="friends-panel">
          <div class="view-title">
            我的好友 ({{ friends.length }})
          </div>
          <div class="members-grid">
            <div
              v-for="f in friends"
              :key="f.id"
              class="member-card-ding"
              @click="handleMemberClick(f)"
            >
              <DingtalkAvatar
                :src="f.friendAvatar"
                :name="f.friendName"
                :user-id="f.friendId"
                :size="44"
                shape="square"
              />
              <div class="m-body">
                <div class="m-name">
                  {{ f.friendName }}
                </div>
                <div class="m-position">
                  在线好友
                </div>
              </div>
              <button class="chat-shortcut" @click.stop="handleChat(f)">
                <el-icon><ChatDotRound /></el-icon>
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 统一详情弹窗 -->
    <UserProfileDialog v-model="showProfile" :user-id="activeUserId" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useStore } from 'vuex'
import {
  Search, Plus, UserFilled, Menu, StarFilled,
  FolderOpened, ArrowRight, ChatDotRound
} from '@element-plus/icons-vue'
import { getDepartment, getDepartmentMembers } from '@/api/im/organization'
import { getContacts } from '@/api/im/contact'
import OrganizationTree from '@/components/Contacts/OrganizationTree.vue'
import NewFriendsView from '@/components/Contacts/NewFriendsView.vue'
import GroupsView from '@/components/Contacts/GroupsView.vue'
import UserProfileDialog from '@/components/Contacts/UserProfileDialog.vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { ElMessage } from 'element-plus'

const store = useStore()
const sidebarSearch = ref('')
const view = ref('dept')
const currentDeptId = ref(null)
const selectedDept = ref(null)
const subDepartments = ref([])
const deptMembers = ref([])
const breadcrumbs = ref([])
const friends = ref([])
const showProfile = ref(false)
const activeUserId = ref(null)

// 视图切换逻辑
const switchView = (v) => { view.value = v; if (v === 'friends') loadFriends() }

// 处理部门选择
const handleDeptSelect = async (dept) => {
  view.value = 'dept'
  currentDeptId.value = dept.id
  selectedDept.value = dept

  // 更新面包屑逻辑
  updateBreadcrumbs(dept)

  try {
    const [deptRes, memberRes] = await Promise.all([
      getDepartment(dept.id),
      getDepartmentMembers(dept.id)
    ])
    if (deptRes.code === 200) subDepartments.value = deptRes.data.children || []
    if (memberRes.code === 200) deptMembers.value = memberRes.data || []
  } catch (e) { ElMessage.error('加载部门数据失败') }
}

const updateBreadcrumbs = (node) => {
  // 这里简化处理，实际应根据 tree 节点的 path 获取全路径
  // 仅作为演示，目前采用“点击追加”或“点击回退”模拟
  const idx = breadcrumbs.value.findIndex(b => b.id === node.id)
  if (idx !== -1) {
    breadcrumbs.value = breadcrumbs.value.slice(0, idx + 1)
  } else {
    // 假设 node 携带了 parent 信息或当前是层级进入
    breadcrumbs.value.push(node)
  }
}

const handleBreadcrumbClick = (node) => {
  if (!node) { breadcrumbs.value = []; view.value = 'dept'; subDepartments.value = []; deptMembers.value = []; return }
  handleDeptSelect(node)
}

const loadFriends = async () => {
  try {
    const res = await getContacts()
    if (res.code === 200) friends.value = res.data || []
  } catch (e) { console.error(e) }
}

const handleMemberClick = (m) => { activeUserId.value = m.userId || m.friendId || m.id; showProfile.value = true }
const handleChat = (m) => {
  store.commit('im/session/SET_CURRENT_SESSION', { id: m.userId || m.friendId || m.id, name: m.name || m.friendName, type: 'PRIVATE' })
  ElMessage.success(`发起会话: ${m.name || m.friendName}`)
}
const handleGroupSelect = (g) => store.commit('im/session/SET_CURRENT_SESSION', { ...g, type: 'GROUP' })

onMounted(() => { if (store.state.user?.token) loadFriends() })
</script>

<style scoped lang="scss">
.contacts-panel-v2 {
  display: flex; height: 100%; width: 100%; background: var(--dt-bg-body); overflow: hidden;
}

// ============================================================================
// 中栏：侧边导航 (280px)
// ============================================================================
.contacts-sidebar {
  width: var(--dt-session-panel-width);
  height: 100%; background: var(--dt-bg-card);
  border-right: 1px solid var(--dt-border-light);
  display: flex; flex-direction: column; flex-shrink: 0;

  .sidebar-header {
    height: var(--dt-chat-header-height); padding: 0 var(--dt-spacing-md);
    @include flex-center; border-bottom: 1px solid var(--dt-border-lighter);
    .search-wrap {
      width: 100%; height: var(--dt-btn-height-sm, 32px); background: var(--dt-bg-body); border-radius: var(--dt-radius-sm, 6px);
      display: flex; align-items: center; padding: 0 var(--dt-spacing-sm, 10px); gap: var(--dt-spacing-sm, 8px);
      .search-icon { color: var(--dt-text-tertiary); font-size: var(--dt-icon-size-md, 14px); }
      .inner-input { border: none; background: transparent; outline: none; font-size: var(--dt-font-size-sm, 13px); width: 100%; }
    }
  }
}

.sidebar-scroll { flex: 1; overflow-y: auto; padding: var(--dt-spacing-md) var(--dt-spacing-sm); }

.nav-list {
  display: flex; flex-direction: column; gap: var(--dt-spacing-xs, 2px); margin-bottom: var(--dt-spacing-lg);
  .nav-item {
    display: flex; align-items: center; gap: var(--dt-spacing-sm, 12px); padding: var(--dt-spacing-sm, 10px) var(--dt-spacing-sm, 12px);
    border-radius: var(--dt-radius-md); cursor: pointer; transition: all var(--dt-transition-base);
    &:hover { background: var(--dt-bg-session-hover); }
    &.active { background: var(--dt-bg-session-active); color: var(--dt-brand-color); font-weight: var(--dt-font-weight-semibold, 600); }

    .icon-box {
      width: var(--dt-avatar-size-md, 32px); height: var(--dt-avatar-size-md, 32px); border-radius: var(--dt-radius-md, 8px); @include flex-center; color: var(--dt-text-primary); font-size: var(--dt-icon-size-lg, 18px);
      &.orange { background: var(--dt-warning-bg); color: var(--dt-warning-color); } &.blue { background: var(--dt-brand-bg); color: var(--dt-brand-color); } &.green { background: var(--dt-success-bg); color: var(--dt-success-color); }
    }
    span { font-size: var(--dt-font-size-sm, 14px); color: var(--dt-text-primary); }
  }
}

.tree-divider {
  padding: 0 var(--dt-spacing-md) var(--dt-spacing-sm); font-size: var(--dt-font-size-xs, 12px); color: var(--dt-text-tertiary);
  text-transform: uppercase; letter-spacing: var(--dt-letter-spacing-wide, 1px);
}

// ============================================================================
// 主栏：内容区域
// ============================================================================
.contacts-main {
  flex: 1; display: flex; flex-direction: column; background: var(--dt-bg-card); min-width: 0;

  .main-header {
    height: var(--dt-chat-header-height); padding: 0 var(--dt-spacing-xl);
    border-bottom: 1px solid var(--dt-border-light); @include flex-between;
    .breadcrumb-container { :deep(.el-breadcrumb__item) { cursor: pointer; &:hover { color: var(--dt-brand-color); } } }
  }
}

.main-content-scroller { flex: 1; padding: var(--dt-spacing-xl); overflow-y: auto; }

// 下级部门文件夹样式
.sub-dept-list {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: var(--dt-spacing-md);
  margin-bottom: var(--dt-spacing-xl);
}

.dept-folder-card {
  background: var(--dt-bg-body); padding: var(--dt-spacing-sm, 12px) var(--dt-spacing-md, 16px); border-radius: var(--dt-radius-md);
  display: flex; align-items: center; gap: var(--dt-spacing-sm, 12px); cursor: pointer; transition: all var(--dt-transition-fast);
  &:hover { background: var(--dt-bg-session-hover); transform: translateY(var(--dt-transform-y, -1px)); box-shadow: var(--dt-shadow-1); }

  .folder-icon { font-size: var(--dt-icon-size-lg, 24px); color: var(--dt-brand-color); }
  .folder-info { flex: 1; display: flex; flex-direction: column;
    .folder-name { font-size: var(--dt-font-size-sm, 14px); font-weight: var(--dt-font-weight-medium, 500); color: var(--dt-text-primary); }
    .folder-count { font-size: var(--dt-font-size-xs, 12px); color: var(--dt-text-tertiary); }
  }
  .arrow { font-size: var(--dt-icon-size-md, 14px); color: var(--dt-text-quaternary); }
}

.section-divider {
  padding: var(--dt-spacing-md) 0; border-bottom: 1px solid var(--dt-border-lighter);
  margin-bottom: var(--dt-spacing-lg); span { font-size: var(--dt-font-size-sm, 13px); color: var(--dt-text-tertiary); font-weight: var(--dt-font-weight-semibold, 600); }
}

// 成员卡片对标
.members-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: var(--dt-spacing-lg);
}

.member-card-ding {
  background: var(--dt-bg-card); padding: var(--dt-spacing-md, 16px); border-radius: var(--dt-radius-lg); border: 1px solid var(--dt-border-light);
  display: flex; align-items: center; gap: var(--dt-spacing-md, 14px); cursor: pointer; transition: all var(--dt-transition-fast);
  &:hover {
    box-shadow: var(--dt-shadow-card-hover); border-color: var(--dt-brand-light);
    .chat-shortcut { opacity: 1; transform: scale(1); }
  }

  .m-body { flex: 1; min-width: 0;
    .m-name { font-size: var(--dt-font-size-md, 15px); font-weight: var(--dt-font-weight-semibold, 600); color: var(--dt-text-primary); }
    .m-position { font-size: var(--dt-font-size-xs, 12px); color: var(--dt-text-tertiary); margin-top: var(--dt-spacing-xs, 2px); }
  }

  .chat-shortcut {
    @include button-reset; width: var(--dt-btn-height-sm, 32px); height: var(--dt-btn-height-sm, 32px); border-radius: 50%; background: var(--dt-brand-bg);
    color: var(--dt-brand-color); @include flex-center; opacity: 0; transform: scale(0.8);
    transition: all var(--dt-transition-fast); &:hover { background: var(--dt-brand-color); color: var(--dt-text-primary); }
  }
}

.view-title { font-size: var(--dt-font-size-lg, 18px); font-weight: var(--dt-font-weight-bold, 700); color: var(--dt-text-primary); margin-bottom: var(--dt-spacing-xl); }
</style>
