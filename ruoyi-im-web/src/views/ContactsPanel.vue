<template>
  <div class="contacts-panel-premium">
    <!-- 1. 左侧树导航 (240px 黄金中轴) -->
    <aside class="org-sidebar">
      <div class="sidebar-header">
        <span class="title">通讯录</span>
        <button class="action-btn"><span class="material-icons-outlined">search</span></button>
      </div>
      
      <div class="sidebar-content custom-scrollbar">
        <!-- 核心入口 -->
        <div class="quick-links">
          <div class="link-item" :class="{ active: view === 'friends' }" @click="view = 'friends'">
            <span class="material-icons-outlined icon green">person</span>
            <span class="label">我的好友</span>
          </div>
          <div class="link-item" :class="{ active: view === 'frequent' }" @click="view = 'frequent'">
            <span class="material-icons-outlined icon purple">star</span>
            <span class="label">常用联系人</span>
          </div>
          <div class="link-item" :class="{ active: view === 'new' }" @click="view = 'new'">
            <span class="material-icons-outlined icon orange">person_add_alt</span>
            <span class="label">新的朋友</span>
          </div>
          <div class="link-item" :class="{ active: view === 'groups' }" @click="view = 'groups'">
            <span class="material-icons-outlined icon blue">groups</span>
            <span class="label">我的群组</span>
          </div>
        </div>

        <div class="org-tree-section">
          <div class="section-label">组织架构</div>
          <OrganizationTree @select="handleDeptSelect" />
        </div>
      </div>
    </aside>

    <!-- 2. 中间内容区域 (根据视图切换) -->
    <main class="members-main">
      <!-- 部门成员视图 -->
      <template v-if="view === 'dept'">
        <header class="main-header">
          <div class="header-left">
            <h2 class="dept-title">{{ selectedDept?.name || '所有成员' }}</h2>
            <span class="member-count">{{ deptMembers.length }} 人</span>
          </div>
          <div class="header-right">
            <el-button size="small" type="primary" plain @click="handleAddContact"><el-icon><Plus /></el-icon> 添加成员</el-button>
          </div>
        </header>

        <div class="members-scroller custom-scrollbar">
          <div v-if="deptMembers.length > 0" class="members-grid">
            <el-dropdown
              v-for="m in deptMembers" 
              :key="m.id" 
              trigger="contextmenu"
              placement="bottom-start"
              class="m-card-dropdown"
              @command="(c) => handleMenuCommand(c, m)"
            >
              <div 
                class="m-card" 
                @click="handleMemberClick(m)"
              >
                <DingtalkAvatar :src="m.avatar" :name="m.name || m.userName" :user-id="m.id" :size="48" shape="square" />
                <div class="m-info">
                  <div class="m-name">{{ m.name || m.userName }}</div>
                  <div class="m-sub">{{ m.position || '成员' }} · {{ m.deptName || m.departmentName || '组织' }}</div>
                </div>
                <div class="m-actions">
                  <el-tooltip content="发消息" placement="top">
                    <button class="circle-btn blue" @click.stop="handleChat(m)"><span class="material-icons-outlined">chat_bubble</span></button>
                  </el-tooltip>
                </div>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="contact-context-menu">
                  <el-dropdown-item command="chat"><el-icon><ChatDotRound /></el-icon> 发送消息</el-dropdown-item>
                  <el-dropdown-item command="profile"><el-icon><User /></el-icon> 查看资料</el-dropdown-item>
                  <el-dropdown-item command="pin" divided><el-icon><Top /></el-icon> 置顶联系人</el-dropdown-item>
                  <el-dropdown-item command="delete" class="text-danger"><el-icon><Delete /></el-icon> 删除好友</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <el-empty v-else description="该部门暂无成员" />
        </div>
      </template>

      <!-- 新的朋友视图 -->
      <template v-else-if="view === 'new'">
        <NewFriendsView @back="view = 'dept'" />
      </template>

      <!-- 我的群组视图 -->
      <template v-else-if="view === 'groups'">
        <GroupsView @back="view = 'dept'" @select-group="handleGroupSelect" />
      </template>
<!-- 我的好友/常用联系人视图 -->
<template v-else>
  <header class="main-header">
    <div class="header-left">
      <h2 class="dept-title">{{ view === 'friends' ? '我的好友' : '常用联系人' }}</h2>
      <span class="member-count">{{ view === 'friends' ? friends.length : 0 }} 人</span>
    </div>
  </header>

  <div class="members-scroller custom-scrollbar">
    <div v-if="view === 'friends' && friends.length > 0" class="members-grid">
      <el-dropdown
        v-for="f in friends" 
        :key="f.id" 
        trigger="contextmenu"
        placement="bottom-start"
        class="m-card-dropdown"
        @command="(c) => handleMenuCommand(c, f)"
      >
        <div class="m-card" @click="handleMemberClick(f)">
          <DingtalkAvatar :src="f.friendAvatar" :name="f.friendName" :user-id="f.friendId" :size="48" shape="square" />
          <div class="m-info">
            <div class="m-name">{{ f.friendName }}</div>
            <div class="m-sub">{{ f.position || '好友' }}</div>
          </div>
          <div class="m-actions">
            <button class="circle-btn blue" @click.stop="handleChat(f)"><span class="material-icons-outlined">chat_bubble</span></button>
          </div>
        </div>
        <template #dropdown>
          <el-dropdown-menu class="contact-context-menu">
            <el-dropdown-item command="chat"><el-icon><ChatDotRound /></el-icon> 发送消息</el-dropdown-item>
            <el-dropdown-item command="profile"><el-icon><User /></el-icon> 查看资料</el-dropdown-item>
            <el-dropdown-item command="delete" class="text-danger"><el-icon><Delete /></el-icon> 删除好友</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    <el-empty v-else :description="view === 'friends' ? '暂无好友' : '暂无数据'" />
  </div>
</template>
</main>

<!-- 成员详情弹窗 -->
<UserProfileDialog v-model="showProfile" :user="activeUser" @chat="handleChat" />
</div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useStore } from 'vuex'
import { getDepartmentMembers } from '@/api/im/organization'
import { getContacts } from '@/api/im/contact'
import OrganizationTree from '@/components/Contacts/OrganizationTree.vue'
import NewFriendsView from '@/components/Contacts/NewFriendsView.vue'
import GroupsView from '@/components/Contacts/GroupsView.vue'
import UserProfileDialog from '@/components/Contacts/UserProfileDialog.vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { Plus, ChatDotRound, User, Top, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const store = useStore()
const view = ref('dept') 
const selectedDept = ref(null)
const deptMembers = ref([])
const friends = ref([])
const showProfile = ref(false)
const activeUser = ref(null)

const loadFriends = async () => {
try {
const res = await getContacts()
if (res.code === 200) friends.value = res.data || []
} catch (e) { console.error('加载好友失败', e) }
}

watch(view, (newView) => {
if (newView === 'friends') loadFriends()
})

const handleDeptSelect = async (dept) => {
  view.value = 'dept'
  selectedDept.value = dept
  try {
    const res = await getDepartmentMembers(dept.id)
    if (res.code === 200) deptMembers.value = res.data
  } catch (e) { ElMessage.error('加载失败') }
}

const handleMemberClick = (m) => {
  activeUser.value = m
  showProfile.value = true
}

const handleChat = (m) => {
  const session = {
    id: m.id || m.userId,
    targetId: m.id || m.userId,
    name: m.name || m.userName || m.nickname,
    avatar: m.avatar,
    type: 'PRIVATE'
  }
  store.commit('im/session/SET_CURRENT_SESSION', session)
  ElMessage.success(`正在进入对话: ${session.name}`)
}

const handleMenuCommand = (command, user) => {
  switch(command) {
    case 'chat': handleChat(user); break;
    case 'profile': handleMemberClick(user); break;
    case 'delete':
      ElMessageBox.confirm(`确定要删除好友 ${user.name || user.userName} 吗？`, '提示', { type: 'warning' })
        .then(() => { ElMessage.success('已模拟删除') });
      break;
  }
}

const handleGroupSelect = (group) => {
  const session = { ...group, type: 'GROUP' }
  store.commit('im/session/SET_CURRENT_SESSION', session)
}

const handleAddContact = () => { /* 弹窗逻辑 */ }
</script>

<style scoped lang="scss">
.contacts-panel-premium { display: flex; height: 100%; flex: 1; background: #fff; overflow: hidden; }
.org-sidebar { width: 240px; border-right: 1px solid #f2f3f5; display: flex; flex-direction: column; flex-shrink: 0; min-width: 0;
  .title { font-size: 16px; font-weight: 600; color: #1f2329; }
  .action-btn { border: none; background: transparent; cursor: pointer; color: #8f959e; &:hover { color: #1677ff; } }
}
.sidebar-content { flex: 1; padding: 12px 8px; overflow-y: auto;
  .link-item { display: flex; align-items: center; gap: 12px; padding: 10px 12px; border-radius: 8px; cursor: pointer; transition: all 0.2s;
    &:hover { background: #f5f6f7; }
    &.active { background: #e8f4ff; color: #1677ff; .label { font-weight: 600; } }
    .icon { font-size: 20px; &.green { color: #52c41a; } &.purple { color: #722ed1; } &.orange { color: #fa8c16; } &.blue { color: #1677ff; } }
    .label { font-size: 14px; color: #1f2329; }
  }
  .org-tree-section { margin-top: 24px; .section-label { padding: 0 12px 8px; font-size: 11px; color: #8f959e; text-transform: uppercase; letter-spacing: 1px; } }
}
.members-main { flex: 1; display: flex; flex-direction: column; background: #f5f5f5;
  .main-header { height: 60px; padding: 0 24px; background: #fff; border-bottom: 1px solid #f2f3f5; display: flex; align-items: center; justify-content: space-between;
    .dept-title { font-size: 18px; font-weight: 600; color: #1f2329; margin: 0; }
    .member-count { font-size: 12px; color: #8f959e; margin-left: 12px; }
  }
}
.members-scroller { flex: 1; padding: 24px; overflow-y: auto; }
.members-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }
.member-card { background: #fff; padding: 16px; border-radius: 10px; border: 1px solid rgba(31,35,41,0.08); display: flex; align-items: center; gap: 16px; cursor: pointer; transition: all 0.2s;
  &:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.06); .member-actions { opacity: 1; } }
  .member-info { flex: 1; min-width: 0; .member-name { font-size: 15px; font-weight: 600; color: #1f2329; } .member-sub { font-size: 12px; color: #8f959e; margin-top: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; } }
  .member-actions { opacity: 0; transition: opacity 0.2s; .circle-btn { width: 32px; height: 32px; border-radius: 50%; border: none; background: #e8f4ff; color: #1677ff; cursor: pointer; display: flex; align-items: center; justify-content: center; &:hover { background: #1677ff; color: #fff; } .material-icons-outlined { font-size: 18px; } } }
}
.contact-context-menu {
  width: 180px;
  border-radius: 8px;
  padding: 4px;
  :deep(.el-dropdown-menu__item) { 
    display: flex; 
    align-items: center; 
    gap: 10px; 
    padding: 10px 16px; 
    font-size: 14px;
    border-radius: 4px;
    margin: 2px 0;
    .el-icon { font-size: 16px; }
    &:hover { background-color: #f5f7fa; color: #1677ff; }
    &.text-danger { color: #ff4d4f; &:hover { background-color: #fff1f0; } }
  }
}
</style>
