<template>
  <div class="contacts-panel" :class="{ 'mobile-view': isMobile }">
    <!-- 左侧导航栏 (钉钉风格：一级模块) -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <h2 class="sidebar-title">通讯录</h2>
        <div class="search-box-wrapper">
          <el-input
            v-model="searchQuery"
            placeholder="搜索"
            :prefix-icon="Search"
            clearable
            class="dingtalk-search"
          />
        </div>
      </div>

      <el-scrollbar class="sidebar-scroll">
        <div class="nav-section">
          <div class="nav-item" :class="{ active: currentModule === 'new' }" @click="switchModule('new')">
            <div class="nav-icon bg-orange"><el-icon><User /></el-icon></div>
            <span class="nav-label">新的朋友</span>
            <span v-if="pendingCount > 0" class="nav-badge">{{ pendingCount }}</span>
          </div>
          <div class="nav-item" :class="{ active: currentModule === 'group' }" @click="switchModule('group')">
            <div class="nav-icon bg-blue"><el-icon><ChatDotRound /></el-icon></div>
            <span class="nav-label">群聊</span>
          </div>
          <div class="nav-item" :class="{ active: currentModule === 'phone' }" @click="switchModule('phone')">
            <div class="nav-icon bg-green"><el-icon><Cellphone /></el-icon></div>
            <span class="nav-label">手机联系人</span>
          </div>
        </div>

        <div class="nav-divider"></div>

        <div class="nav-section">
          <div class="section-header">企业通讯录</div>
          <div
            v-for="corp in corporations"
            :key="corp.id"
            class="corp-item"
            :class="{ active: currentModule === 'corp' && activeCorpId === corp.id }"
            @click="selectCorp(corp)"
          >
            <div class="corp-icon"><el-icon><OfficeBuilding /></el-icon></div>
            <span class="corp-name">{{ corp.name }}</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
      </el-scrollbar>
    </aside>

    <!-- 中间列表面板 (钉钉风格：成员/子部门列表) -->
    <div class="list-panel">
      <div class="list-header">
        <div class="breadcrumb">
          <span class="breadcrumb-item">{{ currentPath }}</span>
        </div>
        <div class="list-actions">
          <el-button link :icon="Plus" @click="handleAddMember">添加</el-button>
        </div>
      </div>
      
      <el-scrollbar class="list-scroll">
        <!-- 部门列表 -->
        <div v-if="subDepartments.length > 0" class="dept-group">
          <div
            v-for="dept in subDepartments"
            :key="dept.id"
            class="dept-item"
            @click="selectDept(dept)"
          >
            <el-icon class="dept-icon"><Folder /></el-icon>
            <span class="dept-name">{{ dept.name }}</span>
            <span class="dept-count">({{ dept.memberCount }})</span>
          </div>
        </div>

        <!-- 成员列表 -->
        <div class="member-group">
          <div
            v-for="member in currentMembers"
            :key="member.id"
            class="member-item"
            :class="{ active: selectedMemberId === member.id }"
            @click="selectMember(member)"
          >
            <DingtalkAvatar :src="member.avatar" :name="member.name" :size="40" shape="square" />
            <div class="member-info">
              <div class="name-row">
                <span class="name">{{ member.name }}</span>
                <span v-if="member.isLeader" class="leader-tag">负责人</span>
              </div>
              <div class="position">{{ member.position || '暂无职位' }}</div>
            </div>
          </div>
        </div>
      </el-scrollbar>
    </div>

    <!-- 右侧详情面板 (钉钉风格：个人名片) -->
    <div class="detail-panel">
      <div v-if="selectedMember" class="profile-card">
        <div class="profile-header">
          <DingtalkAvatar :src="selectedMember.avatar" :name="selectedMember.name" :size="80" shape="square" />
          <h2 class="profile-name">{{ selectedMember.name }}</h2>
          <p class="profile-id">工号：{{ selectedMember.workId || '-' }}</p>
        </div>
        
        <div class="info-sections">
          <div class="info-item">
            <span class="label">部门</span>
            <span class="value">{{ selectedMember.deptName }}</span>
          </div>
          <div class="info-item">
            <span class="label">职位</span>
            <span class="value">{{ selectedMember.position || '员工' }}</span>
          </div>
          <div class="info-item">
            <span class="label">手机</span>
            <span class="value">{{ selectedMember.phonenumber || '未公开' }}</span>
          </div>
        </div>

        <div class="profile-actions">
          <el-button type="primary" class="main-btn" @click="startChat(selectedMember)">发消息</el-button>
          <div class="sub-actions">
            <el-button circle :icon="Phone" />
            <el-button circle :icon="VideoCamera" />
          </div>
        </div>
      </div>
      <div v-else class="empty-detail">
        <el-empty description="选择联系人查看详情" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import {
  Search, User, ChatDotRound, Cellphone, OfficeBuilding,
  ArrowRight, Folder, UserFilled, Phone, VideoCamera, Plus
} from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { getOrgTree, getDepartmentMembers } from '@/api/im/organization'
import { ElMessage } from 'element-plus'

const store = useStore()

// 响应式状态
const isMobile = ref(false)
const searchQuery = ref('')
const currentModule = ref('new')
const activeCorpId = ref(null)
const currentPath = ref('通讯录')
const pendingCount = ref(0)

// 数据源
const corporations = ref([
  { id: 1, name: '即时通讯企业中心' }
])
const subDepartments = ref([])
const currentMembers = ref([])
const selectedMemberId = ref(null)
const selectedMember = ref(null)

// 切换模块
const switchModule = (module) => {
  currentModule.value = module
  currentPath.value = module === 'new' ? '新的朋友' : module === 'group' ? '群聊' : '手机联系人'
  subDepartments.value = []
  currentMembers.value = []
  selectedMember.value = null
}

// 选择企业/架构
const selectCorp = async (corp) => {
  currentModule.value = 'corp'
  activeCorpId.value = corp.id
  currentPath.value = corp.name
  loading.value = true
  try {
    const res = await getOrgTree()
    subDepartments.value = res.data || []
    currentMembers.value = []
  } catch (error) {
    ElMessage.error('加载组织架构失败')
  } finally {
    loading.value = false
  }
}

// 选择部门
const selectDept = async (dept) => {
  currentPath.value = dept.name
  loading.value = true
  try {
    const res = await getDepartmentMembers(dept.id)
    currentMembers.value = res.data || []
    if (dept.children) {
      subDepartments.value = dept.children
    } else {
      subDepartments.value = []
    }
  } catch (error) {
    ElMessage.error('加载部门成员失败')
  } finally {
    loading.value = false
  }
}

// 选择成员
const selectMember = (member) => {
  selectedMemberId.value = member.id
  selectedMember.value = member
}

// 发起聊天
const startChat = (member) => {
  store.dispatch('im/session/createAndSwitchSession', {
    type: 'PRIVATE',
    targetId: member.id || member.userId
  }).then(() => {
    // 触发主页面切换到消息模块
    window.dispatchEvent(new CustomEvent('im-switch-module', { detail: 'chat' }))
  })
}

const handleAddMember = () => {
  ElMessage.info('功能开发中')
}

const loading = ref(false)

onMounted(() => {
  // 初始化加载
  selectCorp(corporations.value[0])
})
</script>

<style scoped lang="scss">
.contacts-panel {
  display: flex;
  height: 100%;
  background: #fff;
  overflow: hidden;
}

.sidebar {
  width: 250px;
  background: #F7F8F9;
  border-right: 1px solid #E5E6EB;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;

  .sidebar-header {
    padding: 24px 16px 12px;
    .sidebar-title { font-size: 18px; font-weight: 600; color: #1D2129; margin: 0 0 16px; }
    .dingtalk-search {
      :deep(.el-input__wrapper) {
        background: #E5E6EB; box-shadow: none; border-radius: 6px;
        &.is-focus { background: #fff; box-shadow: 0 0 0 1px #165DFF; }
      }
    }
  }
}

.nav-section {
  padding: 8px;
  .section-header { padding: 12px 12px 8px; font-size: 12px; color: #86909C; font-weight: 500; }
}

.nav-item, .corp-item {
  height: 40px; display: flex; align-items: center; padding: 0 12px; border-radius: 6px;
  cursor: pointer; margin-bottom: 2px; color: #4E5969; transition: all 0.2s;
  &:hover { background: #E5E6EB; }
  &.active { background: #E8F3FF; color: #165DFF; .nav-label, .corp-name { font-weight: 600; } }
  .nav-icon {
    width: 24px; height: 24px; border-radius: 4px; display: flex; align-items: center;
    justify-content: center; margin-right: 10px; color: #fff; font-size: 14px;
    &.bg-orange { background: #FF7D00; }
    &.bg-blue { background: #165DFF; }
    &.bg-green { background: #00B42A; }
  }
  .corp-icon { margin-right: 10px; font-size: 18px; color: #86909C; }
  .nav-label, .corp-name { flex: 1; font-size: 14px; }
  .arrow-icon { font-size: 12px; color: #C9CDD4; }
}

.nav-divider { height: 1px; background: #E5E6EB; margin: 8px 16px; }

.list-panel {
  width: 320px; border-right: 1px solid #E5E6EB; display: flex; flex-direction: column; background: #fff;
  .list-header {
    height: 56px; padding: 0 16px; display: flex; align-items: center;
    justify-content: space-between; border-bottom: 1px solid #F2F3F5;
    .breadcrumb-item { font-size: 14px; font-weight: 600; color: #1D2129; }
  }
}

.dept-item {
  height: 48px; display: flex; align-items: center; padding: 0 16px; cursor: pointer;
  &:hover { background: #F2F3F5; }
  .dept-icon { margin-right: 12px; color: #FFCF8B; font-size: 20px; }
  .dept-name { font-size: 14px; color: #1D2129; flex: 1; }
  .dept-count { font-size: 12px; color: #86909C; }
}

.member-item {
  height: 64px; display: flex; align-items: center; padding: 0 16px; cursor: pointer; position: relative;
  &:hover { background: #F2F3F5; }
  &.active {
    background: #E8F3FF;
    &::after { content: ''; position: absolute; right: 0; width: 3px; height: 24px; top: 20px; background: #165DFF; border-radius: 3px 0 0 3px; }
  }
  .member-info {
    margin-left: 12px; flex: 1; min-width: 0;
    .name { font-size: 14px; font-weight: 500; color: #1D2129; }
    .leader-tag { margin-left: 8px; padding: 0 4px; background: #E8F3FF; color: #165DFF; font-size: 10px; border-radius: 2px; }
    .position { font-size: 12px; color: #86909C; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  }
}

.detail-panel { flex: 1; background: #F7F8F9; display: flex; align-items: center; justify-content: center; }
.profile-card {
  width: 400px; background: #fff; border-radius: 12px; box-shadow: 0 12px 32px rgba(0,0,0,0.06);
  padding: 40px; text-align: center;
  .profile-name { font-size: 24px; font-weight: 600; margin: 16px 0 4px; }
  .info-sections { border-top: 1px solid #F2F3F5; padding-top: 24px; text-align: left; }
  .info-item { display: flex; margin-bottom: 16px; .label { width: 80px; color: #86909C; } .value { color: #1D2129; font-weight: 500; } }
  .profile-actions { margin-top: 40px; display: flex; flex-direction: column; gap: 12px; .main-btn { height: 44px; border-radius: 22px; } }
}
</style>
