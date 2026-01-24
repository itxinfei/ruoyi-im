<template>
  <div class="chat-sidebar">
    <div class="sidebar-header">
      <h3 class="title">{{ isGroup ? '群聊详情' : '个人资料' }}</h3>
      <el-button link @click="$emit('close')">
        <el-icon><Close /></el-icon>
      </el-button>
    </div>

    <div class="sidebar-content" v-loading="loading">
      <!-- 群聊详情展示 -->
      <template v-if="isGroup && detail">
        <!-- 群组信息 -->
        <div class="section group-info">
          <el-avatar :size="64" :src="addTokenToUrl(detail.avatar)" shape="square" class="avatar">
            {{ detail.name?.charAt(0) }}
          </el-avatar>
          <div class="info">
            <div class="name">{{ detail.name }}</div>
            <div class="id">群号: {{ detail.id }}</div>
          </div>
        </div>

        <el-divider />

        <!-- 成员列表 -->
        <div class="section members-section">
          <div class="section-title">
            <span>群成员 ({{ detail.memberCount || members.length }})</span>
            <el-button link @click="showAllMembers = true">查看全部</el-button>
          </div>
          <div class="members-grid">
            <div 
              v-for="m in members.slice(0, 7)" 
              :key="m.id" 
              class="member-item" 
              @click="$emit('member-click', m)"
            >
              <DingtalkAvatar 
                :name="m.name" 
                :user-id="m.id" 
                :src="addTokenToUrl(m.avatar)" 
                :size="36" 
                shape="square" 
              />
              <span class="m-name">{{ m.name }}</span>
            </div>
            <div class="member-item add-btn" @click="handleAddMember">
              <div class="add-icon"><el-icon><Plus /></el-icon></div>
              <span class="m-name">添加</span>
            </div>
          </div>
        </div>

        <el-divider />

        <!-- 群公告 -->
        <div class="section">
          <div class="section-title">群公告</div>
          <div class="announcement-card" :class="{ empty: !detail.announcement }">
            {{ detail.announcement || '暂无群公告' }}
          </div>
        </div>

        <el-divider />

        <!-- 群设置预览 (只读或简化) -->
        <div class="section settings">
          <div class="setting-item">
            <span>置顶聊天</span>
            <el-switch v-model="detail.isPinned" size="small" />
          </div>
          <div class="setting-item">
            <span>消息免打扰</span>
            <el-switch v-model="detail.isMuted" size="small" />
          </div>
        </div>
      </template>

      <!-- 单聊详情展示 -->
      <template v-else-if="detail">
        <div class="section user-info">
          <DingtalkAvatar 
            :name="detail.nickname || detail.username" 
            :user-id="detail.id" 
            :src="addTokenToUrl(detail.avatar)" 
            :size="80" 
            shape="square" 
            class="avatar-large"
          />
          <div class="user-main">
            <div class="name-row">
              <span class="nickname">{{ detail.nickname || detail.username }}</span>
              <el-icon v-if="detail.gender === 1" class="male"><Male /></el-icon>
              <el-icon v-else-if="detail.gender === 2" class="female"><Female /></el-icon>
            </div>
            <div class="sub">账号: {{ detail.username }}</div>
          </div>
        </div>

        <el-divider />

        <div class="section info-list">
          <div class="info-item">
            <span class="label">职位</span>
            <span class="value">{{ detail.position || '暂无' }}</span>
          </div>
          <div class="info-item">
            <span class="label">部门</span>
            <span class="value">{{ detail.departmentName || detail.department || '未分配' }}</span>
          </div>
          <div class="info-item">
            <span class="label">手机</span>
            <span class="value">{{ detail.mobile || '-' }}</span>
          </div>
        </div>
      </template>
    </div>

    <!-- 底部操作区域 -->
    <div class="sidebar-footer">
      <el-button v-if="isGroup" type="danger" plain class="w-full" @click="handleLeave">退出群聊</el-button>
      <el-button v-else type="primary" plain class="w-full" @click="$emit('start-chat')">发起聊天</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { Close, Plus, Male, Female } from '@element-plus/icons-vue'
import { getGroup, getGroupMembers, leaveGroup } from '@/api/im/group'
import { getUserInfo } from '@/api/im/user'
import { addTokenToUrl } from '@/utils/file'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  session: { type: Object, required: true }
})

const emit = defineEmits(['close', 'member-click', 'start-chat'])

const loading = ref(false)
const detail = ref(null)
const members = ref([])
const showAllMembers = ref(false)

const isGroup = computed(() => props.session?.type === 'GROUP')

const loadDetail = async () => {
  if (!props.session?.id) return
  loading.value = true
  try {
    if (isGroup.value) {
      const gId = props.session.targetId || props.session.id
      const [gRes, mRes] = await Promise.all([
        getGroup(gId),
        getGroupMembers(gId)
      ])
      if (gRes.code === 200) detail.value = gRes.data
      if (mRes.code === 200) {
        members.value = mRes.data.map(m => ({
          id: m.userId || m.id,
          name: m.userName || m.name || '未知',
          avatar: m.avatar || ''
        }))
      }
    } else {
      const res = await getUserInfo(props.session.targetId)
      if (res.code === 200) detail.value = res.data
    }
  } catch (e) {
    console.error('加载详情失败', e)
    ElMessage.error('加载详情失败')
  } finally {
    loading.value = false
  }
}

const handleAddMember = () => ElMessage.info('邀请功能开发中...')
const handleLeave = async () => {
  try {
    await ElMessageBox.confirm('确定退出群组吗？', '提示', { type: 'warning' })
    await leaveGroup(props.session.targetId || props.session.id)
    ElMessage.success('已退出')
    emit('close')
  } catch (e) {}
}

watch(() => props.session?.id, () => {
  detail.value = null
  loadDetail()
}, { immediate: true })
</script>

<style scoped lang="scss">
.chat-sidebar {
  width: 280px; height: 100%; border-left: 1px solid #f0f0f0; background: #fff;
  display: flex; flex-direction: column; flex-shrink: 0;
}

.sidebar-header {
  height: 57px; padding: 0 16px; border-bottom: 1px solid #f2f3f5;
  display: flex; align-items: center; justify-content: space-between;
  .title { font-size: 15px; font-weight: 600; color: #1f2329; margin: 0; }
}

.sidebar-content {
  flex: 1; overflow-y: auto; padding: 20px 16px;
  .section { margin-bottom: 24px; }
  .section-title {
    display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;
    font-size: 13px; font-weight: 600; color: #1f2329;
  }
}

.group-info {
  display: flex; gap: 12px; align-items: center;
  .avatar { border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
  .name { font-size: 16px; font-weight: 600; color: #1f2329; margin-bottom: 4px; }
  .id { font-size: 12px; color: #8f959e; }
}

.members-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px;
  .member-item {
    display: flex; flex-direction: column; align-items: center; gap: 4px; cursor: pointer;
    .m-name { font-size: 11px; color: #646a73; max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    &.add-btn {
      .add-icon { 
        width: 36px; height: 36px; border: 1px dashed #d9d9d9; border-radius: 4px;
        display: flex; align-items: center; justify-content: center; color: #8f959e;
        &:hover { border-color: #1677ff; color: #1677ff; }
      }
    }
  }
}

.announcement-card {
  padding: 12px; background: #f8fafc; border-radius: 8px; font-size: 13px; color: #1f2329; line-height: 1.6;
  &.empty { color: #8f959e; font-style: italic; }
}

.settings {
  .setting-item {
    display: flex; justify-content: space-between; align-items: center; padding: 8px 0; font-size: 14px;
  }
}

.user-info {
  display: flex; flex-direction: column; align-items: center; gap: 16px;
  .avatar-large { border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
  .user-main {
    text-align: center;
    .name-row {
      display: flex; align-items: center; justify-content: center; gap: 6px;
      .nickname { font-size: 18px; font-weight: 600; color: #1f2329; }
      .male { color: #1677ff; } .female { color: #ff4d4f; }
    }
    .sub { font-size: 12px; color: #8f959e; margin-top: 4px; }
  }
}

.info-list {
  .info-item {
    display: flex; justify-content: space-between; padding: 10px 0; font-size: 14px;
    .label { color: #8f959e; } .value { color: #1f2329; }
  }
}

.sidebar-footer { padding: 16px; border-top: 1px solid #f2f3f5; }

:global(.dark) {
  .chat-sidebar { background: #1e293b; border-color: #334155; }
  .sidebar-header { border-bottom-color: #334155; .title { color: #f1f5f9; } }
  .announcement-card { background: #0f172a; color: #cbd5e1; }
  .section-title, .name, .nickname, .value, .setting-item { color: #f1f5f9; }
}
</style>
