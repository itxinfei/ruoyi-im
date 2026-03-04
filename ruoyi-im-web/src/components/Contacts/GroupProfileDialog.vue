<template>
  <el-dialog
    v-model="visible"
    width="560px"
    :show-close="false"
    class="group-profile-dialog"
    append-to-body
    destroy-on-close
  >
    <div v-if="loading" class="loading-wrap" v-loading="loading"></div>
    <div v-else-if="groupDetail" class="group-container">
      <!-- 头部 -->
      <header class="dialog-header">
        <h3 class="title">群聊信息</h3>
        <button class="close-btn" @click="handleClose">
          <el-icon><Close /></el-icon>
        </button>
      </header>

      <!-- 主体内容 -->
      <div class="group-body custom-scrollbar">
        <!-- 群基本信息 -->
        <section class="info-section">
          <div class="group-meta">
            <DingtalkAvatar
              :src="groupDetail.avatar"
              :name="groupDetail.name"
              :is-group="true"
              :size="56"
              shape="square"
            />
            <div class="meta-info">
              <h2 class="group-name">{{ groupDetail.name }}</h2>
              <p class="group-desc">
                <span>{{ groupDetail.memberCount || 0 }} 人</span>
                <span class="divider">·</span>
                <span>创建于 {{ formatDate(groupDetail.createTime) }}</span>
              </p>
            </div>
          </div>
        </section>

        <!-- 群成员 -->
        <section class="member-section">
          <div class="section-header">
            <span class="section-title">群成员</span>
            <button class="more-btn" @click="showAllMembers = true">
              查看全部 <el-icon><ArrowRight /></el-icon>
            </button>
          </div>
          <div class="member-list">
            <div
              v-for="m in displayMembers"
              :key="m.userId"
              class="member-item"
              @click="handleMemberClick(m)"
            >
              <DingtalkAvatar
                :src="m.avatar"
                :name="m.nickname"
                :user-id="m.userId"
                :size="36"
                shape="square"
              />
              <span class="member-name">{{ m.nickname }}</span>
              <span v-if="m.role === 'OWNER'" class="role-tag owner">群主</span>
              <span v-else-if="m.role === 'ADMIN'" class="role-tag admin">管理员</span>
            </div>
            <button class="add-member-btn" @click="handleAddMember">
              <el-icon><Plus /></el-icon>
              <span>邀请</span>
            </button>
          </div>
        </section>

        <!-- 群设置 -->
        <section class="settings-section">
          <div class="section-title">群设置</div>
          
          <div class="setting-item">
            <div class="setting-left">
              <span class="setting-label">群聊名称</span>
            </div>
            <div class="setting-right">
              <span class="setting-value">{{ groupDetail.name }}</span>
              <el-icon v-if="canEdit" class="edit-icon" @click="handleEditName"><EditPen /></el-icon>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-left">
              <span class="setting-label">群公告</span>
            </div>
            <div class="setting-right clickable" @click="handleViewAnnouncement">
              <span class="setting-value ellipsis">{{ groupDetail.announcement || '暂无公告' }}</span>
              <el-icon class="arrow-icon"><ArrowRight /></el-icon>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-left">
              <span class="setting-label">我在本群的昵称</span>
            </div>
            <div class="setting-right clickable" @click="handleEditNickname">
              <span class="setting-value">{{ groupDetail.myNickname || '未设置' }}</span>
              <el-icon class="arrow-icon"><ArrowRight /></el-icon>
            </div>
          </div>
        </section>

        <!-- 消息设置 -->
        <section class="message-settings">
          <div class="section-title">消息设置</div>
          
          <div class="switch-item">
            <span class="switch-label">置顶聊天</span>
            <el-switch v-model="groupDetail.isPinned" @change="handleTogglePin" size="default" />
          </div>

          <div class="switch-item">
            <span class="switch-label">消息免打扰</span>
            <el-switch v-model="groupDetail.isMuted" @change="handleToggleMute" size="default" />
          </div>
        </section>

        <!-- 管理员功能 -->
        <section v-if="isAdmin" class="admin-section">
          <div class="section-title">群管理</div>
          <div class="admin-actions">
            <div class="admin-item" @click="handleGroupManage">
              <el-icon><Setting /></el-icon>
              <span>群管理</span>
            </div>
            <div class="admin-item" @click="handleMemberManage">
              <el-icon><User /></el-icon>
              <span>成员管理</span>
            </div>
          </div>
        </section>
      </div>

      <!-- 底部操作 -->
      <footer class="dialog-footer">
        <button class="exit-btn" @click="handleExitGroup">
          <el-icon><SwitchButton /></el-icon>
          <span>退出群聊</span>
        </button>
      </footer>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Close, ArrowRight, Plus, EditPen, Setting, User, SwitchButton } from '@element-plus/icons-vue'
import { getGroup, leaveGroup } from '@/api/im/group'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useStore } from 'vuex'

const props = defineProps({ modelValue: Boolean, groupId: [String, Number] })
const emit = defineEmits(['update:modelValue', 'show-user', 'add-member'])

const store = useStore()
const visible = ref(false)
const loading = ref(false)
const groupDetail = ref(null)
const showAllMembers = ref(false)

const isAdmin = computed(() => groupDetail.value?.role === 'ADMIN' || groupDetail.value?.role === 'OWNER')
const canEdit = computed(() => isAdmin.value)
const displayMembers = computed(() => groupDetail.value?.members?.slice(0, 8) || [])

const formatDate = (date) => {
  if (!date) return '未知'
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const handleClose = () => emit('update:modelValue', false)

const loadGroupDetail = async () => {
  if (!props.groupId) return
  loading.value = true
  try {
    const res = await getGroup(props.groupId)
    if (res.code === 200) groupDetail.value = res.data
  } finally { loading.value = false }
}

const handleTogglePin = async (val) => {
  try {
    await store.dispatch('im/session/pinSession', { sessionId: props.groupId, pinned: val })
    ElMessage.success(val ? '已置顶' : '已取消置顶')
  } catch (e) {
    ElMessage.error('设置失败')
    groupDetail.value.isPinned = !val
  }
}

const handleToggleMute = async (val) => {
  try {
    await store.dispatch('im/session/muteSession', { sessionId: props.groupId, muted: val })
    ElMessage.success(val ? '已开启免打扰' : '已关闭免打扰')
  } catch (e) {
    ElMessage.error('设置失败')
    groupDetail.value.isMuted = !val
  }
}

const handleMemberClick = (m) => { emit('show-user', m.userId) }
const handleAddMember = () => { emit('add-member', props.groupId) }
const handleEditName = () => { ElMessage.info('功能开发中') }
const handleViewAnnouncement = () => { ElMessage.info('功能开发中') }
const handleEditNickname = () => { ElMessage.info('功能开发中') }
const handleGroupManage = () => { ElMessage.info('功能开发中') }
const handleMemberManage = () => { ElMessage.info('功能开发中') }

const handleExitGroup = async () => {
  try {
    await ElMessageBox.confirm('退出后将不再接收此群消息，确定退出吗？', '退出群聊', { 
      type: 'warning',
      confirmButtonText: '确定退出',
      cancelButtonText: '取消'
    })
    const res = await leaveGroup(props.groupId)
    if (res.code === 200) {
      ElMessage.success('已退出群聊')
      handleClose()
      store.dispatch('im/session/deleteSession', props.groupId)
    } else {
      ElMessage.error(res.msg || '退出失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('退出群聊失败')
    }
  }
}

watch(() => props.modelValue, (val) => { 
  visible.value = val
  if (val && props.groupId) loadGroupDetail() 
})
watch(visible, (val) => { if (!val) emit('update:modelValue', false) })
</script>

<style scoped lang="scss">
.group-profile-dialog {
  :deep(.el-dialog) {
    border-radius: 12px;
    overflow: hidden;
    padding: 0;
  }
  :deep(.el-dialog__header) { display: none; }
  :deep(.el-dialog__body) { padding: 0; }
}

.group-container {
  background: #fff;
  display: flex;
  flex-direction: column;
  max-height: 70vh;
}

.dialog-header {
  height: 52px;
  padding: 0 20px;
  border-bottom: 1px solid var(--dt-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;

  .title {
    font-size: 16px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0;
  }

  .close-btn {
    width: 32px;
    height: 32px;
    border: none;
    background: transparent;
    border-radius: 6px;
    cursor: pointer;
    color: var(--dt-text-tertiary);
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;

    &:hover {
      background: var(--dt-bg-session-hover);
      color: var(--dt-brand-color);
    }
  }
}

.group-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.info-section {
  margin-bottom: 20px;

  .group-meta {
    display: flex;
    align-items: center;
    gap: 16px;

    .meta-info {
      flex: 1;
      min-width: 0;

      .group-name {
        font-size: 18px;
        font-weight: 600;
        color: var(--dt-text-primary);
        margin: 0 0 6px;
      }

      .group-desc {
        font-size: 13px;
        color: var(--dt-text-tertiary);
        margin: 0;

        .divider {
          margin: 0 6px;
        }
      }
    }
  }
}

.member-section {
  margin-bottom: 20px;

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;

    .section-title {
      font-size: 14px;
      font-weight: 600;
      color: var(--dt-text-primary);
    }

    .more-btn {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: var(--dt-text-tertiary);
      background: none;
      border: none;
      cursor: pointer;
      padding: 4px 8px;
      border-radius: 4px;
      transition: all 0.2s;

      &:hover {
        color: var(--dt-brand-color);
        background: var(--dt-brand-lighter);
      }
    }
  }

  .member-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    .member-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 12px;
      background: var(--dt-bg-body);
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        background: var(--dt-bg-session-hover);
      }

      .member-name {
        font-size: 13px;
        color: var(--dt-text-primary);
        max-width: 80px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .role-tag {
        font-size: 10px;
        padding: 2px 6px;
        border-radius: 4px;
        font-weight: 500;

        &.owner {
          background: #fff7e6;
          color: #fa8c16;
        }

        &.admin {
          background: var(--dt-brand-lighter);
          color: var(--dt-brand-color);
        }
      }
    }

    .add-member-btn {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 16px;
      background: var(--dt-brand-lighter);
      border: 1px dashed var(--dt-brand-color);
      border-radius: 8px;
      color: var(--dt-brand-color);
      font-size: 13px;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        background: var(--dt-brand-bg);
      }
    }
  }
}

.settings-section,
.message-settings,
.admin-section {
  margin-bottom: 20px;

  .section-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin-bottom: 12px;
  }
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid var(--dt-border-lighter);

  &:last-child {
    border-bottom: none;
  }

  .setting-label {
    font-size: 14px;
    color: var(--dt-text-primary);
  }

  .setting-right {
    display: flex;
    align-items: center;
    gap: 8px;

    &.clickable {
      cursor: pointer;
      padding: 4px 8px;
      margin: -4px -8px;
      border-radius: 6px;
      transition: background 0.2s;

      &:hover {
        background: var(--dt-bg-session-hover);
      }
    }

    .setting-value {
      font-size: 14px;
      color: var(--dt-text-tertiary);

      &.ellipsis {
        max-width: 200px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    .edit-icon,
    .arrow-icon {
      color: var(--dt-text-quaternary);
      cursor: pointer;
      transition: color 0.2s;

      &:hover {
        color: var(--dt-brand-color);
      }
    }
  }
}

.switch-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid var(--dt-border-lighter);

  &:last-child {
    border-bottom: none;
  }

  .switch-label {
    font-size: 14px;
    color: var(--dt-text-primary);
  }
}

.admin-actions {
  display: flex;
  gap: 12px;

  .admin-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 16px 24px;
    background: var(--dt-bg-body);
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: var(--dt-bg-session-hover);
      color: var(--dt-brand-color);
    }

    .el-icon {
      font-size: 24px;
    }

    span {
      font-size: 13px;
      color: var(--dt-text-secondary);
    }
  }
}

.dialog-footer {
  padding: 16px 20px;
  border-top: 1px solid var(--dt-border-light);
  flex-shrink: 0;

  .exit-btn {
    width: 100%;
    height: 40px;
    border: none;
    background: var(--dt-error-bg);
    color: var(--dt-error-color);
    border-radius: 8px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    transition: all 0.2s;

    &:hover {
      background: #ffccc7;
    }
  }
}

// 暗色模式
.dark {
  .group-container {
    background: var(--dt-bg-card-dark);
  }

  .dialog-header {
    border-bottom-color: var(--dt-border-dark);
  }

  .setting-item,
  .switch-item {
    border-bottom-color: var(--dt-border-dark);
  }

  .dialog-footer {
    border-top-color: var(--dt-border-dark);
  }
}
</style>