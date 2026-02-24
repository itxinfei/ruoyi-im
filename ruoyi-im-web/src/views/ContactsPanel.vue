<template>
  <div class="contacts-panel" :class="{ 'mobile-view': isMobile }">
    <!-- 左侧导航：钉钉 7.0 极简风格 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <h2 class="sidebar-title">通讯录</h2>
        <div class="search-input-box">
          <el-input
            v-model="searchQuery"
            placeholder="搜索"
            :prefix-icon="Search"
            clearable
          />
        </div>
      </div>

      <el-scrollbar class="sidebar-scroll">
        <!-- 核心功能入口 -->
        <div class="nav-group">
          <div class="nav-item" :class="{ active: currentModule === 'new' }" @click="switchModule('new')">
            <el-icon class="icon bg-orange"><User /></el-icon>
            <span class="label">新的朋友</span>
            <span v-if="pendingCount > 0" class="badge">{{ pendingCount }}</span>
          </div>
          <div class="nav-item" :class="{ active: currentModule === 'group' }" @click="switchModule('group')">
            <el-icon class="icon bg-blue"><ChatDotRound /></el-icon>
            <span class="label">群聊</span>
          </div>
          <div class="nav-item" :class="{ active: currentModule === 'phone' }" @click="switchModule('phone')">
            <el-icon class="icon bg-green"><Cellphone /></el-icon>
            <span class="label">手机联系人</span>
          </div>
        </div>

        <div class="divider"></div>

        <!-- 组织架构入口 -->
        <div class="nav-group">
          <div class="group-title">企业通讯录</div>
          <div
            v-for="corp in corporations"
            :key="corp.id"
            class="nav-item corp-item"
            :class="{ active: currentModule === 'corp' && activeCorpId === corp.id }"
            @click="selectCorp(corp)"
          >
            <el-icon class="icon corp-icon"><OfficeBuilding /></el-icon>
            <span class="label">{{ corp.name }}</span>
            <el-icon class="arrow"><ArrowRight /></el-icon>
          </div>
        </div>
      </el-scrollbar>
    </aside>

    <!-- 中间列表：部门与成员 -->
    <div class="list-panel">
      <div class="list-header">
        <div class="breadcrumb">{{ currentPath }}</div>
        <el-button link :icon="Plus" @click="handleAddMember">添加</el-button>
      </div>
      
      <el-scrollbar class="list-scroll">
        <!-- 部门级联 -->
        <div v-if="subDepartments.length > 0" class="section">
          <div v-for="dept in subDepartments" :key="dept.id" class="item dept-item" @click="selectDept(dept)">
            <el-icon class="folder-icon"><Folder /></el-icon>
            <span class="name">{{ dept.name }}</span>
            <span class="count">{{ dept.memberCount }}</span>
          </div>
        </div>

        <!-- 成员展示 -->
        <div class="section">
          <div
            v-for="member in currentMembers"
            :key="member.id"
            class="item member-item"
            :class="{ active: selectedMemberId === member.id }"
            @click="selectMember(member)"
          >
            <DingtalkAvatar :src="member.avatar" :name="member.name" :size="36" shape="square" />
            <div class="info">
              <div class="name-row">
                <span class="name">{{ member.name }}</span>
                <span v-if="member.isLeader" class="leader-tag">负责人</span>
              </div>
              <div class="sub">{{ member.position || '员工' }}</div>
            </div>
          </div>
        </div>
      </el-scrollbar>
    </div>

    <!-- 右侧详情：钉钉名片 -->
    <div class="detail-panel">
      <div v-if="selectedMember" class="profile-card">
        <div class="card-header">
          <DingtalkAvatar :src="selectedMember.avatar" :name="selectedMember.name" :size="80" shape="square" />
          <h2 class="name">{{ selectedMember.name }}</h2>
          <div class="id">工号：{{ selectedMember.workId || '-' }}</div>
        </div>
        
        <div class="card-body">
          <div class="row"><span class="lbl">部门</span><span class="val">{{ selectedMember.deptName }}</span></div>
          <div class="row"><span class="lbl">职位</span><span class="val">{{ selectedMember.position || '员工' }}</span></div>
          <div class="row"><span class="lbl">手机</span><span class="val">{{ selectedMember.phonenumber || '未公开' }}</span></div>
        </div>

        <div class="card-footer">
          <el-button type="primary" class="send-msg-btn" @click="startChat(selectedMember)">发消息</el-button>
          <div class="extra-actions">
            <el-button circle :icon="Phone" />
            <el-button circle :icon="VideoCamera" />
          </div>
        </div>
      </div>
      <div v-else class="empty-view">
        <el-empty description="选择联系人查看详情" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useStore } from 'vuex'
import {
  Search, User, ChatDotRound, Cellphone, OfficeBuilding,
  ArrowRight, Folder, Phone, VideoCamera, Plus
} from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { getOrgTree, getDepartmentMembers } from '@/api/im/organization'
import { ElMessage } from 'element-plus'

const store = useStore()
const isMobile = ref(false)
const searchQuery = ref('')
const currentModule = ref('corp')
const activeCorpId = ref(null)
const currentPath = ref('通讯录')
const pendingCount = ref(0)

const corporations = ref([{ id: 1, name: '即时通讯企业中心' }])
const subDepartments = ref([])
const currentMembers = ref([])
const selectedMemberId = ref(null)
const selectedMember = ref(null)

const switchModule = (module) => {
  currentModule.value = module
  currentPath.value = module === 'new' ? '新的朋友' : module === 'group' ? '群聊' : '手机联系人'
  subDepartments.value = []
  currentMembers.value = []
  selectedMember.value = null
}

const selectCorp = async (corp) => {
  currentModule.value = 'corp'; activeCorpId.value = corp.id; currentPath.value = corp.name
  try {
    const res = await getOrgTree()
    subDepartments.value = res.data || []
    currentMembers.value = []
  } catch (e) { ElMessage.error('加载失败') }
}

const selectDept = async (dept) => {
  currentPath.value = dept.name
  try {
    const res = await getDepartmentMembers(dept.id)
    currentMembers.value = res.data || []
    subDepartments.value = dept.children || []
  } catch (e) { ElMessage.error('加载失败') }
}

const selectMember = (m) => { selectedMemberId.value = m.id; selectedMember.value = m }

const startChat = (m) => {
  store.dispatch('im/session/createAndSwitchSession', {
    type: 'PRIVATE', targetId: m.id || m.userId
  }).then(() => {
    window.dispatchEvent(new CustomEvent('im-switch-module', { detail: 'chat' }))
  })
}

const handleAddMember = () => ElMessage.info('功能开发中')

onMounted(() => selectCorp(corporations.value[0]))
</script>

<style scoped lang="scss">
.contacts-panel {
  display: flex; height: 100%; background: #fff; overflow: hidden;
}

// 侧边栏
.sidebar {
  width: 250px; background: #F7F8F9; border-right: 1px solid #E5E6EB; display: flex; flex-direction: column;
  .sidebar-header {
    padding: 24px 16px 12px;
    .sidebar-title { font-size: 18px; font-weight: 600; color: #1D2129; margin-bottom: 12px; }
    .search-input-box :deep(.el-input__wrapper) { background: #E5E6EB; border-radius: 6px; box-shadow: none; }
  }
}

.nav-group {
  padding: 12px 8px; // 增加分组间距
  .group-title { padding: 12px 12px 8px; font-size: 12px; color: #86909C; text-transform: uppercase; letter-spacing: 0.5px; }
  .nav-item {
    height: 48px; // 提升至 48px
    display: flex; align-items: center; padding: 0 16px; border-radius: 8px;
    cursor: pointer; margin-bottom: 4px; color: #4E5969; position: relative;
    transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
    .icon {
      width: 24px; height: 24px; border-radius: 4px; display: flex; align-items: center; justify-content: center;
      margin-right: 10px; color: #fff; font-size: 14px;
      &.bg-orange { background: #FF7D00; }
      &.bg-blue { background: #165DFF; }
      &.bg-green { background: #00B42A; }
    }
    .corp-icon { color: #86909C; font-size: 18px; }
    .label { flex: 1; font-size: 14px; }
    .arrow { font-size: 12px; color: #C9CDD4; }
  }
}

// 列表
.list-panel {
  width: 320px; border-right: 1px solid #E5E6EB; display: flex; flex-direction: column;
  .list-header {
    height: 56px; padding: 0 16px; border-bottom: 1px solid #F2F3F5; display: flex; align-items: center; justify-content: space-between;
    .breadcrumb { font-size: 14px; font-weight: 600; color: #1D2129; }
  }
}

.section { padding: 8px 0; }
.item {
  display: flex; align-items: center; padding: 0 16px; cursor: pointer; transition: background 0.2s;
  &:hover { background: #F2F3F5; }
  &.active { background: #E8F3FF; }
}

.dept-item {
  height: 48px;
  .folder-icon { color: #FFCF8B; font-size: 20px; margin-right: 12px; }
  .name { flex: 1; font-size: 14px; }
  .count { color: #86909C; font-size: 12px; }
}

.member-item {
  height: 64px;
  .info {
    margin-left: 12px; flex: 1;
    .name { font-size: 14px; font-weight: 500; color: #1D2129; }
    .leader-tag { margin-left: 8px; padding: 0 4px; background: #E8F3FF; color: #165DFF; font-size: 10px; border-radius: 2px; }
    .sub { font-size: 12px; color: #86909C; margin-top: 2px; }
  }
}

// 详情
.detail-panel { flex: 1; background: #F7F8F9; display: flex; align-items: center; justify-content: center; }
.profile-card {
  width: 400px; background: #fff; border-radius: 12px; box-shadow: 0 12px 32px rgba(0,0,0,0.06); padding: 40px; text-align: center;
  .card-header { margin-bottom: 32px; .name { font-size: 24px; margin: 16px 0 4px; } .id { color: #86909C; font-size: 13px; } }
  .card-body {
    border-top: 1px solid #F2F3F5; padding-top: 24px; text-align: left;
    .row { display: flex; margin-bottom: 16px; font-size: 14px; .lbl { width: 80px; color: #86909C; } .val { color: #1D2129; font-weight: 500; } }
  }
  .card-footer {
    margin-top: 40px; display: flex; flex-direction: column; gap: 16px;
    .send-msg-btn { height: 44px; border-radius: 22px; font-weight: 600; background: #165DFF; }
    .extra-actions { display: flex; justify-content: center; gap: 16px; .el-button { background: #F2F3F5; border: none; color: #4E5969; } }
  }
}
</style>
