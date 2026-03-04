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

    <!-- 2. 中间成员列表 (灰底卡片流) -->
    <main class="members-main">
      <header class="main-header">
        <div class="header-left">
          <h2 class="dept-title">{{ selectedDept?.name || '所有成员' }}</h2>
          <span class="member-count">{{ deptMembers.length }} 人</span>
        </div>
        <div class="header-right">
          <el-button-group>
            <el-button size="small"><span class="material-icons-outlined">add</span> 添加</el-button>
            <el-button size="small"><span class="material-icons-outlined">sort</span> 排序</el-button>
          </el-button-group>
        </div>
      </header>

      <div class="members-scroller custom-scrollbar">
        <div class="members-grid">
          <div 
            v-for="m in deptMembers" 
            :key="m.id" 
            class="m-card" 
            @click="handleMemberClick(m)"
          >
            <DingtalkAvatar :src="m.avatar" :name="m.name" :user-id="m.id" :size="48" shape="square" />
            <div class="m-info">
              <div class="m-name">{{ m.name }}</div>
              <div class="m-sub">{{ m.position || '员工' }} · {{ m.departmentName }}</div>
            </div>
            <div class="m-actions">
              <button class="circle-btn blue" @click.stop="handleChat(m)"><span class="material-icons-outlined">chat_bubble</span></button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDepartmentMembers } from '@/api/im/organization'
import OrganizationTree from '@/components/Contacts/OrganizationTree.vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { ElMessage } from 'element-plus'

const view = ref('dept')
const selectedDept = ref(null)
const deptMembers = ref([])

const handleDeptSelect = async (dept) => {
  selectedDept.value = dept
  try {
    const res = await getDepartmentMembers(dept.id)
    if (res.code === 200) deptMembers.value = res.data
  } catch (e) { ElMessage.error('加载失败') }
}

const handleMemberClick = (m) => { /* show profile */ }
const handleChat = (m) => { /* switch to chat */ }
</script>

<style scoped lang="scss">
.contacts-panel-premium { display: flex; height: 100%; flex: 1; background: #fff; overflow: hidden; }
.org-sidebar { width: 240px; border-right: 1px solid #f2f3f5; display: flex; flex-direction: column; 
  .sidebar-header { height: 60px; padding: 0 16px; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #f2f3f5;
    .title { font-size: 16px; font-weight: 600; color: #1f2329; }
    .action-btn { border: none; background: transparent; cursor: pointer; color: #8f959e; &:hover { color: #1677ff; } }
  }
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
.m-card { background: #fff; padding: 16px; border-radius: 10px; border: 1px solid rgba(31,35,41,0.08); display: flex; align-items: center; gap: 16px; cursor: pointer; transition: all 0.2s;
  &:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.06); .m-actions { opacity: 1; } }
  .m-info { flex: 1; .m-name { font-size: 15px; font-weight: 600; color: #1f2329; } .m-sub { font-size: 12px; color: #8f959e; margin-top: 4px; } }
  .m-actions { opacity: 0; transition: opacity 0.2s; .circle-btn { width: 32px; height: 32px; border-radius: 50%; border: none; background: #e8f4ff; color: #1677ff; cursor: pointer; display: flex; align-items: center; justify-content: center; &:hover { background: #1677ff; color: #fff; } .material-icons-outlined { font-size: 18px; } } }
}
</style>
