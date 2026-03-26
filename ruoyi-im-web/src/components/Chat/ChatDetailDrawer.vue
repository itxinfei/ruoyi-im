<script setup>
import { ref, watch } from 'vue'
import { getGroup } from '@/api/im/group'
import { getUserInfo } from '@/api/im/user'
import { getGroupFileStatistics } from '@/api/im/groupFile'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import GroupFilePanel from '@/components/GroupDetailDrawer/GroupFilePanel.vue'

const props = defineProps({
  modelValue: Boolean,
  session: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'show-user', 'switch-module'])

const loading = ref(false)
const members = ref([])
const fileStats = ref(null)
const userInfo = ref(null)
const groupFilePanelRef = ref(null)

const loadData = async () => {
  if (!props.session?.id) return
  loading.value = true
  try {
    if (props.session.type === 'GROUP') {
      const groupRes = await getGroup(props.session.targetId)
      if (groupRes.code === 200) {
        members.value = (groupRes.data?.members || []).slice(0, 12)
      }
      const statsRes = await getGroupFileStatistics(props.session.targetId)
      if (statsRes.code === 200) fileStats.value = statsRes.data
    } else {
      const userId = props.session.peerUserId || props.session.targetId
      const userRes = await getUserInfo(userId)
      if (userRes.code === 200) userInfo.value = userRes.data
    }
  } catch (e) {
    console.error('加载详情失败', e)
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) loadData()
})

const handleClose = () => emit('update:modelValue', false)

const handleFileClick = () => {
  if (groupFilePanelRef.value) {
    groupFilePanelRef.value.open()
  }
}
</script>

<template>
  <el-drawer
    :model-value="modelValue"
    @update:model-value="val => emit('update:modelValue', val)"
    title="会话详情"
    direction="rtl"
    size="320px"
    class="im-detail-drawer"
    :with-header="false"
  >
    <div class="drawer-content" v-loading="loading">
      <!-- 头部：基本信息 -->
      <div class="section hero-section">
        <div class="hero-main">
          <DingtalkAvatar 
            :src="session.avatar" 
            :name="session.name" 
            :size="48" 
            shape="square" 
          />
          <div class="hero-info">
            <h3 class="name">{{ session.name }}</h3>
            <p class="sub">{{ session.type === 'GROUP' ? '群聊' : '单聊' }}</p>
          </div>
        </div>
      </div>

      <!-- 群成员区域 -->
      <div v-if="session.type === 'GROUP'" class="section">
        <div class="section-header">
          <span class="title">群成员</span>
          <span class="count">{{ session.memberCount }}人</span>
        </div>
        <div class="member-grid">
          <div v-for="m in members" :key="m.userId" class="member-item" @click="emit('show-user', m.userId)">
            <DingtalkAvatar :src="m.avatar" :name="m.nickname" :size="32" shape="square" />
            <span class="nickname">{{ m.nickname }}</span>
          </div>
          <div class="member-item add-btn">
            <div class="icon-box"><el-icon><Plus /></el-icon></div>
            <span class="nickname">邀请</span>
          </div>
        </div>
      </div>

      <!-- 个人信息展示 -->
      <div v-if="session.type === 'PRIVATE' && userInfo" class="section info-list">
        <div class="info-item">
          <label>部门</label>
          <span>{{ userInfo.departmentName || '—' }}</span>
        </div>
        <div class="info-item">
          <label>职位</label>
          <span>{{ userInfo.position || '—' }}</span>
        </div>
      </div>

      <!-- 设置开关区 -->
      <div class="section settings-list">
        <div class="setting-row">
          <span>置顶会话</span>
          <el-switch :model-value="session.isPinned" />
        </div>
        <div class="setting-row">
          <span>消息免打扰</span>
          <el-switch :model-value="session.isMuted" />
        </div>
      </div>

      <!-- 文件入口 -->
      <div class="section menu-item" @click="handleFileClick">
        <div class="menu-left">
          <el-icon><FolderOpened /></el-icon>
          <span>{{ session.type === 'GROUP' ? '群文件' : '聊天文件' }}</span>
        </div>
        <el-icon><ArrowRight /></el-icon>
      </div>

      <!-- 文件面板 -->
      <GroupFilePanel ref="groupFilePanelRef" :group-id="session.type === 'GROUP' ? session.targetId : null" />

      <div class="bottom-actions">
        <el-button v-if="session.type === 'GROUP'" type="danger" plain class="w-full">退出群聊</el-button>
      </div>
    </div>
  </el-drawer>
</template>

<style lang="scss" scoped>
.im-detail-drawer {
  :deep(.el-drawer__body) {
    padding: 0;
    background-color: var(--dt-bg-body);
  }
}

.drawer-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  .section {
    background: var(--dt-bg-card);
    margin-bottom: 8px;
    padding: 16px;
    
    &.hero-section {
      padding: 24px 16px;
      .hero-main {
        display: flex;
        align-items: center;
        gap: 12px;
        .name {
          margin: 0;
          font-size: 16px;
          color: var(--dt-text-main);
        }
        .sub {
          margin: 4px 0 0;
          font-size: 12px;
          color: var(--dt-text-desc);
        }
      }
    }
  }

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    .title { font-weight: 500; color: var(--dt-text-main); }
    .count { font-size: 12px; color: var(--dt-text-desc); }
  }

  .member-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
    .member-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;
      cursor: pointer;
      .nickname {
        font-size: 11px;
        color: var(--dt-text-desc);
        max-width: 100%;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      &.add-btn {
        .icon-box {
          width: 32px;
          height: 32px;
          border: 1px dashed var(--dt-border-light);
          border-radius: 4px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: var(--dt-text-desc);
        }
      }
    }
  }

  .info-list {
    .info-item {
      display: flex;
      justify-content: space-between;
      margin-bottom: 12px;
      font-size: 14px;
      &:last-child { margin-bottom: 0; }
      label { color: var(--dt-text-desc); }
      span { color: var(--dt-text-main); }
    }
  }

  .settings-list {
    .setting-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;
      font-size: 14px;
      color: var(--dt-text-main);
    }
  }

  .menu-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    cursor: pointer;
    &:hover { background: var(--dt-bg-hover); }
    .menu-left {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 14px;
      color: var(--dt-text-main);
    }
  }

  .bottom-actions {
    margin-top: auto;
    padding: 16px;
    background: var(--dt-bg-card);
  }
}
</style>
