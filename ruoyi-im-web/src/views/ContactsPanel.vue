<template>
  <div class="contacts-panel-v3">
    <!-- 1. 中栏：导航与组织树 (对齐钉钉 280px) -->
    <aside class="contacts-sidebar">
      <header class="sidebar-header">
        <div class="search-box">
          <el-icon><Search /></el-icon>
          <input v-model="sidebarSearch" placeholder="搜索联系人、部门" />
        </div>
      </header>

      <div class="sidebar-body custom-scrollbar">
        <!-- 核心导航 (极简彩色线性风格) -->
        <nav class="nav-group">
          <div
            v-for="item in navItems"
            :key="item.id"
            class="nav-item"
            :class="{ active: view === item.id }"
            @click="switchView(item.id)"
          >
            <div class="icon-wrapper" :style="{ color: item.color }">
              <el-icon><component :is="item.icon" /></el-icon>
            </div>
            <span class="label">{{ item.label }}</span>
          </div>
        </nav>

        <div class="section-divider">组织架构</div>

        <!-- 组织树区 -->
        <div class="org-tree-section">
          <OrganizationTree @select="handleDeptSelect" />
        </div>
      </div>
    </aside>

    <!-- 2. 右侧主视图 (对齐钉钉：工业灰背景 + 纯白卡片) -->
    <main class="contacts-main">
      <!-- 统一头部面包屑 (56px) -->
      <header v-if="view === 'dept' || breadcrumbs.length" class="main-header">
        <div class="breadcrumb-bar">
          <span class="root-node" @click="handleBreadcrumbClick(null)">公司通讯录</span>
          <template v-for="node in breadcrumbs" :key="node.id">
            <el-icon class="sep"><ArrowRight /></el-icon>
            <span class="node" @click="handleBreadcrumbClick(node)">{{ node.name }}</span>
          </template>
        </div>
        <el-button type="primary" size="small" plain :icon="Plus">邀请成员</el-button>
      </header>

      <div class="content-scroller custom-scrollbar">
        <template v-if="view === 'dept'">
          <!-- 部门网格 (纯白文件夹卡片) -->
          <div v-if="subDepartments.length" class="dept-grid">
            <div v-for="dept in subDepartments" :key="dept.id" class="dept-card" @click="handleDeptSelect(dept)">
              <div class="folder-icon-box"><el-icon><FolderOpened /></el-icon></div>
              <div class="folder-info">
                <div class="name">{{ dept.name }}</div>
                <div class="count">{{ dept.memberCount || 0 }} 人</div>
              </div>
              <el-icon class="arrow-icon"><ArrowRight /></el-icon>
            </div>
          </div>

          <!-- 成员网格 (纯白成员卡片) -->
          <template v-if="deptMembers.length">
            <div class="member-grid-header">直属成员 ({{ deptMembers.length }})</div>
            <div class="members-grid">
              <div v-for="m in deptMembers" :key="m.id" class="member-card-v3" @click="handleMemberClick(m)">
                <DingtalkAvatar :src="m.avatar" :name="m.name" :size="40" shape="square" />
                <div class="member-info">
                  <div class="m-name">{{ m.name || m.userName }}</div>
                  <div class="m-post">{{ m.position || '成员' }}</div>
                </div>
                <button class="chat-btn" @click.stop="handleChat(m)"><el-icon><ChatDotRound /></el-icon></button>
              </div>
            </div>
          </template>
          <div v-else-if="!subDepartments.length" class="empty-state">
            <el-icon :size="64" color="var(--dt-text-quaternary)"><OfficeBuilding /></el-icon>
            <p>请选择部门查看成员</p>
          </div>
        </template>

        <!-- 其他子模块视图 -->
        <component :is="activeSubView" v-else @select-member="handleMemberClick" />
      </div>
    </main>
  </div>
</template>

<script setup lang="js">
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { Search, Plus, UserFilled, Menu, StarFilled, Link, Clock, FolderOpened, ArrowRight, ChatDotRound, OfficeBuilding } from '@element-plus/icons-vue'
import OrganizationTree from '@/components/Contacts/OrganizationTree.vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { getDepartmentMembers } from '@/api/im/organization'
import { createConversation } from '@/api/im/conversation'

const store = useStore()

const view = ref('dept')
const sidebarSearch = ref('')
const breadcrumbs = ref([])
const subDepartments = ref([])
const deptMembers = ref([])
const membersLoading = ref(false)

const navItems = [
  { id: 'new', label: '新的联系人', icon: UserFilled, color: 'var(--dt-warning-color)' },
  { id: 'groups', label: '我的群组', icon: Menu, color: 'var(--dt-brand-color)' },
  { id: 'friends', label: '我的好友', icon: StarFilled, color: 'var(--dt-success-color)' },
  { id: 'external', label: '外部联系人', icon: Link, color: 'var(--dt-purple-color)' },
  { id: 'frequent', label: '常用联系人', icon: Clock, color: 'var(--dt-cyan-color)' }
]

const switchView = (v) => {
  view.value = v
  if (v !== 'dept') {
    breadcrumbs.value = []
    subDepartments.value = []
    deptMembers.value = []
  }
}

/**
 * 加载部门数据（子部门列表 + 成员列表）
 * @param {Object} dept - 部门对象，需包含 id、name，可选 children
 */
const loadDeptData = async (dept) => {
  subDepartments.value = dept.children || []
  membersLoading.value = true
  try {
    const res = await getDepartmentMembers(dept.id)
    if (res.code === 200) {
      deptMembers.value = res.data || []
    } else {
      deptMembers.value = []
    }
  } catch (e) {
    console.error('加载部门成员失败', e)
    deptMembers.value = []
  } finally {
    membersLoading.value = false
  }
}

/**
 * 选中部门（来自组织树或部门卡片点击）
 * - 来自组织树：重置面包屑为 [dept]
 * - 来自部门卡片（下钻）：追加到面包屑末尾
 */
const handleDeptSelect = (dept) => {
  if (!dept) return
  view.value = 'dept'

  // 判断是否为下钻操作（当前子部门列表中包含该部门）
  const isDrillDown = subDepartments.value.some(d => d.id === dept.id)

  if (isDrillDown) {
    breadcrumbs.value = [...breadcrumbs.value, { id: dept.id, name: dept.name }]
  } else {
    // 来自组织树 - 根节点重置，否则设为单级面包屑
    breadcrumbs.value = dept.id === 'root' ? [] : [{ id: dept.id, name: dept.name }]
  }

  loadDeptData(dept)
}

/**
 * 面包屑点击 - 导航到对应层级
 * @param {Object|null} node - null 表示点击根节点"公司通讯录"
 */
const handleBreadcrumbClick = (node) => {
  if (node === null) {
    // 回到根级：清空面包屑和数据
    breadcrumbs.value = []
    subDepartments.value = []
    deptMembers.value = []
    return
  }

  const idx = breadcrumbs.value.findIndex(b => b.id === node.id)
  if (idx !== -1) {
    breadcrumbs.value = breadcrumbs.value.slice(0, idx + 1)
    // 重新加载该部门的数据（子部门信息已丢失，仅传 id/name）
    loadDeptData({ id: node.id, name: node.name, children: [] })
  }
}

/**
 * 成员点击 - 打开与该成员的私聊会话
 */
const handleMemberClick = (m) => {
  handleChat(m)
}

/**
 * 发起私聊会话
 */
const handleChat = async (m) => {
  try {
    const userId = m.userId || m.id
    if (!userId) {
      ElMessage.warning('无法获取用户信息')
      return
    }
    const res = await createConversation({ type: 'PRIVATE', targetId: userId })
    if (res.code === 200) {
      store.commit('im/session/SET_CURRENT_SESSION', res.data)
      ElMessage.success(`已打开与 ${m.name || m.userName} 的会话`)
    } else {
      ElMessage.error(res.msg || '无法发起对话')
    }
  } catch (e) {
    console.error('创建会话失败', e)
    ElMessage.error('无法发起对话')
  }
}
</script>

<style scoped lang="scss">
// ============================================================================
// 通讯录面板 - 钉钉风格
// ============================================================================

.contacts-panel-v3 {
  display: flex;
  height: 100%;
  background: var(--dt-bg-card);
  overflow: hidden;
}

// ============================================================================
// 侧栏 - 钉钉规范
// ============================================================================

.contacts-sidebar {
  width: var(--dt-contact-panel-width);
  background: var(--dt-bg-card);
  border-right: 1px solid var(--dt-border-light);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  height: var(--dt-header-height);
  padding: 0 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid var(--dt-border-light);
}

.search-box {
  width: 100%;
  height: 36px;
  background: var(--dt-bg-input);
  border-radius: var(--dt-radius-sm);
  display: flex;
  align-items: center;
  padding: 0 12px;
  gap: 8px;
  transition: all 0.2s;
  border: 1px solid transparent;

  &:focus-within {
    background: var(--dt-bg-card);
    border-color: var(--dt-brand-color);
    box-shadow: 0 0 0 3px var(--dt-brand-lighter);
  }

  input {
    border: none;
    background: transparent;
    outline: none;
    font-size: 14px;
    flex: 1;
    color: var(--dt-text-primary);
    &::placeholder { color: var(--dt-text-tertiary); }
  }

  .el-icon {
    color: var(--dt-text-tertiary);
    font-size: 16px;
  }
}

.sidebar-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px 8px;
}

// ============================================================================
// 导航项
// ============================================================================

.nav-item {
  height: 44px;
  display: flex;
  align-items: center;
  padding: 0 14px;
  gap: 12px;
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: var(--dt-transition-fast);
  margin-bottom: 2px;

  &:hover {
    background: var(--dt-bg-hover);
  }

  &.active {
    background: var(--dt-brand-bg);

    .label {
      color: var(--dt-brand-color);
      font-weight: var(--dt-font-weight-semibold);
    }
  }
}

.icon-wrapper {
  font-size: var(--dt-font-size-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
}

.label {
  font-size: 14px;
  color: var(--dt-text-primary);
}

.section-divider {
  font-size: 12px;
  color: var(--dt-text-tertiary);
  font-weight: var(--dt-font-weight-semibold);
  padding: 24px 12px 12px;
  letter-spacing: 0.3px;
}

// ============================================================================
// 右侧主区 (工业灰背景)
// ============================================================================

.contacts-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--dt-bg-body);
  min-width: 0;
}

.main-header {
  height: var(--dt-header-height);
  padding: 0 24px;
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.breadcrumb-bar {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: var(--dt-text-secondary);

  .root-node,
  .node {
    cursor: pointer;
    transition: color 0.15s;
    &:hover { color: var(--dt-brand-color); }
  }

  .root-node { color: var(--dt-text-tertiary); }

  .sep {
    margin: 0 8px;
    font-size: 12px;
    color: var(--dt-text-quaternary);
  }
}

.content-scroller {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

// ============================================================================
// 部门网格 (纯白文件夹卡片)
// ============================================================================

.dept-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}

.dept-card {
  background: var(--dt-bg-card);
  padding: 16px 24px;
  border-radius: var(--dt-radius-lg);
  border: 1px solid var(--dt-border-light);
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: var(--dt-transition-fast);

  &:hover {
    border-color: var(--dt-brand-light);
    box-shadow: var(--dt-shadow-1);

    .arrow-icon { opacity: 1; }
  }
}

.folder-icon-box {
  width: 48px;
  height: 48px;
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);
  border-radius: var(--dt-radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.folder-info {
  flex: 1;
  .name {
    font-size: 16px;
    font-weight: var(--dt-font-weight-semibold);
    color: var(--dt-text-primary);
  }
  .count {
    font-size: 12px;
    color: var(--dt-text-tertiary);
    margin-top: 4px;
  }
}

.arrow-icon {
  font-size: 14px;
  color: var(--dt-text-quaternary);
  opacity: 0;
  transition: opacity 0.2s;
}

// ============================================================================
// 成员网格 (纯白成员卡片)
// ============================================================================

.member-grid-header {
  font-size: 14px;
  font-weight: var(--dt-font-weight-bold);
  color: var(--dt-text-secondary);
  margin-bottom: 16px;
}

.members-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.member-card-v3 {
  background: var(--dt-bg-card);
  padding: 16px 18px;
  border-radius: var(--dt-radius-lg);
  border: 1px solid var(--dt-border-light);
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: var(--dt-transition-fast);

  &:hover {
    border-color: var(--dt-brand-color);
    box-shadow: var(--dt-shadow-1);

    .chat-btn { opacity: 1; }
  }
}

.member-info {
  flex: 1;
  min-width: 0;

  .m-name {
    font-size: 14px;
    font-weight: var(--dt-font-weight-semibold);
    color: var(--dt-text-primary);
  }

  .m-post {
    font-size: 12px;
    color: var(--dt-text-tertiary);
    margin-top: 4px;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: var(--dt-spacing-lg);
  p { color: var(--dt-text-tertiary); font-size: var(--dt-font-size-base); }
}

.chat-btn {
  width: 34px;
  height: 34px;
  background: var(--dt-brand-bg);
  color: var(--dt-brand-color);
  border: none;
  border-radius: 50%;
  opacity: 0;
  transition: var(--dt-transition-fast);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover {
    background: var(--dt-brand-color);
    color: var(--dt-text-white);
  }
}
</style>
