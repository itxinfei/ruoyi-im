<template>
  <el-dialog
    v-model="visible"
    width="480px"
    :show-close="false"
    :close-on-click-modal="true"
    class="group-profile-dialog"
    destroy-on-close
    append-to-body
  >
    <div v-if="loading" v-loading="loading" class="loading-state"></div>
    <div v-else-if="groupDetail" class="group-container">
      <!-- 顶部 Header -->
      <div class="group-header">
        <div class="header-content">
          <el-avatar :size="64" :src="addTokenToUrl(groupDetail.avatar)" shape="square" class="group-avatar">
            {{ groupDetail.name?.charAt(0) }}
          </el-avatar>
          <div class="group-meta">
            <h2 class="group-name">{{ groupDetail.name }}</h2>
            <p class="group-id">群 ID: {{ groupDetail.id }}</p>
          </div>
        </div>
        <button class="close-btn" @click="handleClose">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 6L6 18M6 6l12 12" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>

      <div class="group-content">
        <!-- 群成员预览 -->
        <div class="section">
          <div class="section-title">
            <span>群成员 ({{ groupDetail.memberCount || members.length }})</span>
            <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 18l6-6-6-6" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <div class="members-preview">
            <div v-for="m in members.slice(0, 7)" :key="m.id" class="member-item">
              <el-avatar :size="36" :src="addTokenToUrl(m.avatar)" class="m-avatar">
                {{ m.name?.charAt(0) }}
              </el-avatar>
              <span class="m-name">{{ m.name }}</span>
            </div>
            <div class="member-item add-btn" @click="handleAddMember">
              <div class="add-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 5v14M5 12h14" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
              <span class="m-name">添加</span>
            </div>
          </div>
        </div>

        <el-divider />

        <!-- 群公告 -->
        <div class="section">
          <div class="section-title">群公告</div>
          <div class="announcement-box" :class="{ empty: !groupDetail.announcement }">
            {{ groupDetail.announcement || '暂无公告' }}
          </div>
        </div>

        <el-divider />

        <!-- 功能选项 -->
        <div class="section info-list">
          <div class="info-item">
            <span class="label">群组描述</span>
            <span class="value">{{ groupDetail.description || '暂无描述' }}</span>
          </div>
          <div class="info-item">
            <span class="label">我的群昵称</span>
            <span class="value">{{ myMemberInfo?.nickname || '-' }}</span>
          </div>
        </div>
        
        <el-divider />

        <!-- 设置开关 -->
        <div class="section settings">
          <div class="setting-item">
            <span>置顶聊天</span>
            <el-switch v-model="groupDetail.isPinned" size="small" />
          </div>
          <div class="setting-item">
            <span>消息免打扰</span>
            <el-switch v-model="groupDetail.isMuted" size="small" />
          </div>
        </div>
      </div>

      <!-- 底部操作 -->
      <div class="group-actions">
        <el-button type="primary" class="main-action" @click="handleStartChat">进入群聊</el-button>
        <el-button v-if="isOwner" type="danger" plain class="sub-action" @click="handleDismiss">解散群组</el-button>
        <el-button v-else type="danger" plain class="sub-action" @click="handleLeave">退出群组</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { getGroup, getGroupMembers, dismissGroup, leaveGroup } from '@/api/im/group'
import { createConversation } from '@/api/im/conversation'
import { addTokenToUrl } from '@/utils/file'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  modelValue: Boolean,
  groupId: [String, Number]
})

const emit = defineEmits(['update:modelValue', 'refresh'])

const store = ref(useStore())
const visible = ref(false)
const loading = ref(false)
const groupDetail = ref(null)
const members = ref([])
const currentUserId = computed(() => store.value.state.user.id || store.value.state.user.userId)

const isOwner = computed(() => groupDetail.value?.ownerId === currentUserId.value)
const myMemberInfo = computed(() => members.value.find(m => m.id === currentUserId.value))

const handleClose = () => {
  emit('update:modelValue', false)
}

const loadData = async () => {
  if (!props.groupId) return
  loading.value = true
  try {
    const [gRes, mRes] = await Promise.all([
      getGroup(props.groupId),
      getGroupMembers(props.groupId)
    ])
    if (gRes.code === 200) groupDetail.value = gRes.data
    if (mRes.code === 200) {
      members.value = mRes.data.map(item => ({
        id: item.userId || item.id,
        name: item.userName || item.name || '未知',
        avatar: item.avatar || '',
        role: item.role || 'MEMBER',
        nickname: item.nickname
      }))
    }
  } catch (error) {
    console.error('加载群详情失败:', error)
  } finally {
    loading.value = false
  }
}

const handleStartChat = async () => {
  try {
    const res = await createConversation({ type: 'GROUP', targetId: props.groupId })
    if (res.code === 200) {
      store.value.commit('im/SET_CURRENT_SESSION', res.data)
      window.dispatchEvent(new CustomEvent('switch-tab', { detail: 'chat' }))
      handleClose()
    }
  } catch (e) { ElMessage.error('进入群聊失败') }
}

const handleAddMember = () => { ElMessage.info('邀请功能开发中...') }

const handleDismiss = async () => {
  try {
    await ElMessageBox.confirm('确定要解散该群组吗？此操作不可撤销。', '解散群组', { type: 'error' })
    await dismissGroup(props.groupId)
    ElMessage.success('已解散群组')
    emit('refresh')
    handleClose()
  } catch (e) {}
}

const handleLeave = async () => {
  try {
    await ElMessageBox.confirm('确定要退出该群组吗？', '退出群组', { type: 'warning' })
    await leaveGroup(props.groupId)
    ElMessage.success('已退出群组')
    emit('refresh')
    handleClose()
  } catch (e) {}
}

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.groupId) loadData()
})

watch(visible, (val) => {
  if (!val) emit('update:modelValue', false)
})
</script>

<style scoped lang="scss">
.group-profile-dialog {
  :deep(.el-dialog) {
    border-radius: 16px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15), 0 0 0 1px rgba(0, 0, 0, 0.05);
    animation: dialogFadeIn 0.25s cubic-bezier(0.4, 0, 0.2, 1);
    overflow: hidden;
  }
  
  :deep(.el-dialog__header) { display: none; }
  :deep(.el-dialog__body) { padding: 0; border-radius: 16px; overflow: hidden; }
}

@keyframes dialogFadeIn {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(-10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.loading-state { height: 400px; display: flex; align-items: center; justify-content: center; }

.group-container { background: #fff; }

.group-header {
  padding: 24px 24px 20px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  
  .header-content {
    display: flex;
    gap: 16px;
    align-items: center;
    .group-avatar { border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
    .group-meta {
      .group-name { margin: 0; font-size: 18px; font-weight: 600; color: #1f2329; letter-spacing: -0.01em; }
      .group-id { margin: 4px 0 0; font-size: 12px; color: #64748b; }
    }
  }
  
  .close-btn {
    width: 36px; height: 36px;
    display: flex; align-items: center; justify-content: center;
    color: #64748b; border: none; background: rgba(0,0,0,0.03); border-radius: 10px; cursor: pointer;
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
    svg { width: 18px; height: 18px; transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1); }
    &:hover { 
      background: rgba(0,0,0,0.08); 
      color: #1f2329; 
      transform: scale(1.05);
      svg { transform: rotate(90deg); }
    }
    &:active { transform: scale(0.95); }
  }
}

.group-content {
  padding: 0 24px;
  max-height: 480px;
  overflow-y: auto;

  .section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 14px;
    font-weight: 600;
    color: #1f2329;
    margin: 16px 0 12px;

    .arrow-icon {
      width: 14px; height: 14px;
      color: #8f959e; cursor: pointer;
    }
  }

  .members-preview {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
    .member-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      width: 48px;
      gap: 4px;
      cursor: pointer;
      .m-avatar { border: 1px solid #f0f0f0; }
      .m-name { font-size: 11px; color: #646a73; width: 100%; text-align: center; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

      &.add-btn {
        .add-icon {
          width: 36px; height: 36px; border-radius: 50%; border: 1px dashed #d9d9d9;
          display: flex; align-items: center; justify-content: center; color: #8f959e;

          svg { width: 16px; height: 16px; }
          &:hover { border-color: #1677ff; color: #1677ff; }
        }
      }
    }
  }

  .announcement-box {
    padding: 12px; background: #f5f6f7; border-radius: 8px; font-size: 13px; color: #646a73; line-height: 1.6;
    &.empty { color: #8f959e; font-style: italic; }
  }

  .info-list {
    .info-item {
      display: flex; justify-content: space-between; padding: 12px 0; font-size: 14px;
      .label { color: #8f959e; }
      .value { color: #1f2329; }
    }
  }

  .settings {
    .setting-item {
      display: flex; justify-content: space-between; align-items: center; padding: 12px 0; font-size: 14px; color: #1f2329;
    }
  }
}

.group-actions {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  border-top: 1px solid #f0f0f0;
  
  .main-action { width: 100%; height: 40px; }
  .sub-action { width: 100%; height: 40px; }
}

:global(.dark) {
  .group-container {
    background: #1e293b;
    .group-header { background: #0f172a; .group-name { color: #f1f5f9; } }
    .section-title { color: #f1f5f9; }
    .announcement-box { background: #334155; color: #cbd5e1; }
    .info-item .value, .setting-item { color: #f1f5f9; }
  }
}
</style>
