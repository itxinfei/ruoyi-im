<template>
  <div class="chat-sidebar">
    <div class="sidebar-header">
      <h3 class="title">{{ isGroup ? '群聊详情' : '个人资料' }}</h3>
      <el-button link @click="$emit('close')">
        <el-icon><Close /></el-icon>
      </el-button>
    </div>

    <div class="sidebar-content" v-loading="loading">
      <el-tabs v-model="activeTab" class="sidebar-tabs" stretch>
        <!-- 成员标签页 -->
        <el-tab-pane label="成员" name="members">
          <template v-if="isGroup && detail">
            <!-- 搜索成员 -->
            <div class="search-box">
              <el-input v-model="memberSearch" placeholder="搜索成员" :prefix-icon="Search" size="small" clearable />
            </div>
            <!-- 成员网格 -->
            <div class="section members-section">
              <div class="members-grid">
                <div v-for="m in filteredMembers" :key="m.id" class="member-item" @click="$emit('member-click', m)">
                  <DingtalkAvatar :name="m.name" :user-id="m.id" :src="addTokenToUrl(m.avatar)" :size="36" shape="square" />
                  <span class="m-name">{{ m.name }}</span>
                </div>
                <div class="member-item add-btn" @click="handleAddMember">
                  <div class="add-icon"><el-icon><Plus /></el-icon></div>
                  <span class="m-name">添加</span>
                </div>
              </div>
            </div>
          </template>
          
          <!-- 单聊个人资料 -->
          <template v-else-if="detail">
            <div class="section user-info-card">
              <DingtalkAvatar :name="detail.nickname || detail.username" :user-id="detail.id" :src="addTokenToUrl(detail.avatar)" :size="80" shape="square" />
              <h3 class="card-name">{{ detail.nickname || detail.username }}</h3>
              <p class="card-sub">职位: {{ detail.position || '暂无' }}</p>
            </div>
            <div class="section info-list">
              <div class="info-item"><span class="label">部门</span><span class="value">{{ detail.departmentName || '未分配' }}</span></div>
              <div class="info-item"><span class="label">手机</span><span class="value">{{ detail.mobile || '-' }}</span></div>
            </div>
          </template>
        </el-tab-pane>

        <!-- 文件标签页 -->
        <el-tab-pane label="文件" name="files">
          <div class="section files-section">
            <div v-if="files.length === 0" class="empty-files">
              <span class="material-icons-outlined">folder_open</span>
              <p>暂无群文件</p>
            </div>
            <div v-else class="file-list">
              <div v-for="file in files" :key="file.id" class="file-row">
                <el-icon><Document /></el-icon>
                <div class="file-info">
                  <div class="f-name">{{ file.name }}</div>
                  <div class="f-meta">{{ file.size }} · {{ file.time }}</div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 设置标签页 -->
        <el-tab-pane label="设置" name="settings">
          <div v-if="detail" class="section settings-list">
            <div class="setting-item"><span>置顶聊天</span><el-switch v-model="detail.isPinned" size="small" /></div>
            <div class="setting-item"><span>消息免打扰</span><el-switch v-model="detail.isMuted" size="small" /></div>
            <div class="setting-item" v-if="isGroup"><span>全员禁言</span><el-switch v-model="detail.allMuted" size="small" /></div>
            <div class="setting-item" v-if="isGroup"><span>保存到通讯录</span><el-switch v-model="detail.isSaved" size="small" /></div>
          </div>
          <div v-else class="p-4 text-center text-gray-400">正在加载...</div>
        </el-tab-pane>
      </el-tabs>
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
import { Close, Plus, Male, Female, Search, Document } from '@element-plus/icons-vue'
import { getGroup, getGroupMembers, leaveGroup } from '@/api/im/group'
// ...

const activeTab = ref('members')
const memberSearch = ref('')
const files = ref([]) // 模拟文件列表

const filteredMembers = computed(() => {
  if (!memberSearch.value) return members.value
  return members.value.filter(m => m.name.toLowerCase().includes(memberSearch.value.toLowerCase()))
})
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

const handleAddMember = () => {
  // TODO: 打开邀请成员弹窗
  ElMessage.info('请在群聊设置中邀请成员')
}
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
  width: 280px; height: 100%; border-left: 1px solid var(--dt-border-light); background: var(--dt-bg-card);
  display: flex; flex-direction: column; flex-shrink: 0; min-width: 0;
}

.sidebar-header {
  height: var(--dt-header-height); padding: 0 var(--dt-spacing-lg); border-bottom: 1px solid var(--dt-border-lighter);
  display: flex; align-items: center; justify-content: space-between;
  .title { font-size: var(--dt-font-size-md); font-weight: var(--dt-font-weight-semibold); color: var(--dt-text-primary); margin: 0; }
}

.sidebar-content {
  flex: 1; overflow-y: auto; padding: var(--dt-spacing-xl) var(--dt-spacing-lg);
  .section { margin-bottom: var(--dt-spacing-2xl); }
  .section-title {
    display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--dt-spacing-md);
    font-size: var(--dt-font-size-sm); font-weight: var(--dt-font-weight-semibold); color: var(--dt-text-primary);
  }
}

.sidebar-tabs {
  :deep(.el-tabs__header) { margin-bottom: 0; padding: 0 var(--dt-spacing-sm); }
  :deep(.el-tabs__nav-wrap::after) { height: 1px; background-color: var(--dt-border-lighter); }
}

.search-box { padding: var(--dt-spacing-md) 0; }

.user-info-card {
  display: flex; flex-direction: column; align-items: center; text-align: center; gap: var(--dt-spacing-md);
  .card-name { font-size: var(--dt-font-size-xl); font-weight: var(--dt-font-weight-semibold); color: var(--dt-text-primary); margin: 0; }
  .card-sub { font-size: var(--dt-font-size-sm); color: var(--dt-text-tertiary); margin: 0; }
}

.files-section {
  .empty-files {
    display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 60px 0; color: var(--dt-text-tertiary);
    .material-icons-outlined { font-size: 48px; margin-bottom: var(--dt-spacing-md); opacity: 0.5; }
    p { font-size: var(--dt-font-size-sm); }
  }
  .file-list {
    display: flex; flex-direction: column; gap: var(--dt-spacing-xs);
    .file-row {
      display: flex; align-items: center; gap: var(--dt-spacing-md); padding: var(--dt-spacing-md); border-radius: var(--dt-radius-md); cursor: pointer;
      &:hover { background: var(--dt-bg-session-hover); }
      .el-icon { font-size: 32px; color: var(--dt-brand-color); }
      .f-name { font-size: var(--dt-font-size-sm); color: var(--dt-text-primary); font-weight: var(--dt-font-weight-medium); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
      .f-meta { font-size: var(--dt-font-size-xs); color: var(--dt-text-tertiary); }
    }
  }
}

.settings-list {
  display: flex; flex-direction: column; gap: var(--dt-spacing-xs);
  .setting-item {
    display: flex; justify-content: space-between; align-items: center; padding: var(--dt-spacing-md) var(--dt-spacing-sm); border-radius: var(--dt-radius-md);
    &:hover { background: var(--dt-bg-session-hover); }
    span { font-size: var(--dt-font-size-base); color: var(--dt-text-primary); }
  }
}

.info-list {
  .info-item {
    display: flex; justify-content: space-between; padding: var(--dt-spacing-md) 0; font-size: var(--dt-font-size-base);
    .label { color: var(--dt-text-tertiary); } .value { color: var(--dt-text-primary); }
  }
}

.sidebar-footer { padding: var(--dt-spacing-lg); border-top: 1px solid var(--dt-border-lighter); }

:global(.dark) {
  .chat-sidebar { background: var(--dt-bg-card-dark); border-color: var(--dt-border-dark); }
  .sidebar-header { border-bottom-color: var(--dt-border-dark); .title { color: var(--dt-text-primary); } }
  .section-title, .name, .nickname, .value, .setting-item { color: var(--dt-text-primary); }
}
</style>
