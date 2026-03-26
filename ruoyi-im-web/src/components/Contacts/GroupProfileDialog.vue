<template>
  <el-dialog
    v-model="visible"
    width="560px"
    :show-close="false"
    class="group-profile-dialog"
    append-to-body
    destroy-on-close
  >
    <div v-if="loading" v-loading="loading" class="loading-wrap" />
    <div v-else-if="groupDetail" class="group-container">
      <!-- 头部 -->
      <header class="dialog-header">
        <div class="header-left">
          <DingtalkAvatar
            :src="groupDetail.avatar"
            :name="groupDetail.name"
            :is-group="true"
            :size="40"
            shape="square"
          />
          <div class="header-info">
            <div class="group-name">{{ groupDetail.name }}</div>
            <div class="group-sub">
              <span>{{ groupDetail.memberCount || 0 }} 人</span>
              <span class="divider">·</span>
              <span>创建于 {{ formatDate(groupDetail.createTime) }}</span>
            </div>
          </div>
        </div>
        <div class="header-actions">
          <el-button size="small" @click="handleTogglePin(!groupDetail.isPinned)">
            {{ groupDetail.isPinned ? '取消置顶' : '置顶' }}
          </el-button>
          <el-button size="small" @click="handleToggleMute(!groupDetail.isMuted)">
            {{ groupDetail.isMuted ? '取消静音' : '静音' }}
          </el-button>
          <el-dropdown trigger="click" @command="handleMoreCommand">
            <el-button size="small">更多</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="qrcode">群二维码</el-dropdown-item>
                <el-dropdown-item command="clear">清空聊天记录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button class="close-btn" @click="handleClose">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </header>

      <!-- 主体内容 -->
      <div class="group-body custom-scrollbar">
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
                :size="32"
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
          <div class="section-title">
            群设置
          </div>

          <div class="setting-item">
            <div class="setting-left">
              <span class="setting-label">群聊名称</span>
            </div>
            <div class="setting-right">
              <span class="setting-value">{{ groupDetail.name }}</span>
              <el-icon v-if="canEdit" class="edit-icon" @click="handleEditName">
                <EditPen />
              </el-icon>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-left">
              <span class="setting-label">群公告</span>
            </div>
            <div class="setting-right clickable" @click="handleViewAnnouncement">
              <span class="setting-value ellipsis">{{ groupDetail.announcement || '暂无公告' }}</span>
              <el-icon class="arrow-icon">
                <ArrowRight />
              </el-icon>
              <el-icon v-if="canEdit" class="edit-icon" @click.stop="handleEditAnnouncement">
                <EditPen />
              </el-icon>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-left">
              <span class="setting-label">我在本群的昵称</span>
            </div>
            <div class="setting-right clickable" @click="handleEditNickname">
              <span class="setting-value">{{ groupDetail.myNickname || '未设置' }}</span>
              <el-icon class="arrow-icon">
                <ArrowRight />
              </el-icon>
            </div>
          </div>
        </section>

        <!-- 消息设置 -->
        <section class="message-settings">
          <div class="section-title">
            消息设置
          </div>

          <div class="switch-item">
            <span class="switch-label">置顶聊天</span>
            <el-switch v-model="groupDetail.isPinned" size="small" @change="handleTogglePin" />
          </div>

          <div class="switch-item">
            <span class="switch-label">消息免打扰</span>
            <el-switch v-model="groupDetail.isMuted" size="small" @change="handleToggleMute" />
          </div>
        </section>

        <!-- 管理员功能 -->
        <section v-if="isAdmin" class="admin-section">
          <div class="section-title">
            群管理
          </div>
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

      <!-- 群二维码弹窗 -->
      <el-dialog v-model="showQr" width="360px" append-to-body>
        <div class="qr-wrap">
          <img v-if="groupDetail?.qrcodeUrl" :src="groupDetail.qrcodeUrl" class="qr-img">
          <div v-else class="qr-empty">暂无二维码</div>
          <div class="qr-text">使用钉钉扫码加入群聊</div>
          <div class="qr-actions">
            <button class="qr-btn" @click="copyQrLink">复制链接</button>
            <button class="qr-btn" @click="downloadQr">下载二维码</button>
          </div>
        </div>
      </el-dialog>

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
import { getGroup, leaveGroup, updateGroup } from '@/api/im/group'
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
const showQr = ref(false)

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

const handleEditAnnouncement = () => {
  ElMessageBox.prompt('编辑群公告', '群公告', {
    inputValue: groupDetail.value?.announcement || '',
    confirmButtonText: '保存',
    cancelButtonText: '取消'
  }).then(async ({ value }) => {
    await updateGroup(props.groupId, { notice: value })
    groupDetail.value.announcement = value
    ElMessage.success('群公告已更新')
  }).catch(() => {})
}

const handleMoreCommand = (cmd) => {
  if (cmd === 'qrcode') {
    showQr.value = true
  } else if (cmd === 'clear') {
    ElMessage.info('清空聊天记录功能待接入')
  }
}

const copyQrLink = () => {
  const url = groupDetail.value?.qrcodeUrl
  if (!url) return ElMessage.info('暂无二维码')
  navigator.clipboard?.writeText(url).then(() => {
    ElMessage.success('已复制')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

const downloadQr = () => {
  const url = groupDetail.value?.qrcodeUrl
  if (!url) return ElMessage.info('暂无二维码')
  const a = document.createElement('a')
  a.href = url
  a.download = `group-${props.groupId}-qrcode.png`
  a.click()
}

const handleMemberClick = (m) => { emit('show-user', m.userId) }
const handleAddMember = () => { emit('add-member', props.groupId) }

const handleEditName = () => {
  ElMessage.info('请在群聊中发送消息呼唤AI助手修改群名称')
}

const handleViewAnnouncement = () => {
  if (groupDetail.value?.announcement) {
    ElMessageBox.alert(groupDetail.value.announcement, '群公告', {
      confirmButtonText: '确定'
    })
  } else {
    ElMessage.info('暂无群公告')
  }
}

const handleEditNickname = () => {
  ElMessage.info('请在群聊中发送消息呼唤AI助手修改群昵称')
}

const handleGroupManage = () => {
  ElMessage.info('群管理功能请使用通讯录-群管理')
}

const handleMemberManage = () => {
  ElMessage.info('成员管理：点击上方成员列表进行管理')
}

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
    border-radius: var(--dt-radius-lg);
    overflow: hidden;
    padding: 0;
    box-shadow: var(--dt-shadow-3);
  }
  :deep(.el-dialog__header) { display: none; }
  :deep(.el-dialog__body) { padding: 0; }
}

.group-container {
  background: var(--dt-bg-card);
  display: flex;
  flex-direction: column;
  max-height: 70vh;
}

.dialog-header {
  padding: var(--dt-spacing-lg);
  border-bottom: 1px solid var(--dt-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-md);
  min-width: 0;
}

.header-info {
  min-width: 0;
}

.group-name {
  font-size: var(--dt-font-size-lg);
  font-weight: 600;
  color: var(--dt-text-primary);
}

.group-sub {
  margin-top: 4px;
  font-size: var(--dt-font-size-xs);
  color: var(--dt-text-tertiary);
  display: flex;
  align-items: center;
  gap: 6px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-actions .el-button {
  padding: 4px 10px;
  font-size: 12px;
  border-radius: 4px;
}

.header-actions .close-btn {
  padding: 4px 8px;
  background: transparent;
  border: 1px solid var(--dt-border-light);
}

.group-body {
  flex: 1;
  overflow-y: auto;
  padding: var(--dt-spacing-lg);
}

.member-section {
  margin-bottom: 12px;

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 10px;

    .section-title {
      font-size: 13px;
      font-weight: 600;
      color: var(--dt-text-secondary);
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
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 10px;

    .member-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 6px;
      padding: 6px 0;
      background: transparent;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        background: var(--dt-bg-session-hover);
      }

      .member-name {
        font-size: 12px;
        color: var(--dt-text-primary);
        max-width: 72px;
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
      flex-direction: column;
      align-items: center;
      gap: 6px;
      padding: 6px 0;
      background: transparent;
      border: 1px dashed var(--dt-border-light);
      border-radius: 8px;
      color: var(--dt-text-tertiary);
      font-size: 12px;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        background: var(--dt-bg-session-hover);
      }
    }
  }
}

.settings-section,
.message-settings,
.admin-section {
  margin-bottom: 12px;
  border: 1px solid var(--dt-border-light);
  border-radius: 8px;
  padding: 12px 16px;
  background: var(--dt-bg-card);

  .section-title {
    font-size: 13px;
    font-weight: 600;
    color: var(--dt-text-secondary);
    margin-bottom: 10px;
  }
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid var(--dt-border-lighter);

  &:last-child {
    border-bottom: none;
  }

  .setting-label {
    font-size: 13px;
    color: var(--dt-text-secondary);
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
      font-size: 13px;
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
  padding: 8px 0;
  border-bottom: 1px solid var(--dt-border-lighter);

  &:last-child {
    border-bottom: none;
  }

  .switch-label {
    font-size: 13px;
    color: var(--dt-text-secondary);
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
  padding: 12px var(--dt-spacing-lg);
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
      background: var(--dt-error-bg);
    }
  }
}

.qr-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 16px 0;
}

.qr-img {
  width: 220px;
  height: 220px;
  object-fit: cover;
  border: 1px solid var(--dt-border-light);
  border-radius: 8px;
}

.qr-empty {
  width: 220px;
  height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--dt-text-tertiary);
  border: 1px dashed var(--dt-border-light);
  border-radius: 8px;
}

.qr-text {
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.qr-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
}

.qr-btn {
  padding: 6px 12px;
  font-size: 12px;
  border-radius: 6px;
  border: 1px solid var(--dt-border-light);
  background: var(--dt-bg-body);
  color: var(--dt-text-secondary);
  cursor: pointer;
  transition: all 0.2s;
}

.qr-btn:hover {
  background: var(--dt-brand-lighter);
  color: var(--dt-brand-color);
  border-color: var(--dt-brand-light);
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

  .qr-btn {
    background: var(--dt-bg-body-dark);
    color: var(--dt-text-primary-dark);
    border-color: var(--dt-border-dark);
  }
}
</style>
