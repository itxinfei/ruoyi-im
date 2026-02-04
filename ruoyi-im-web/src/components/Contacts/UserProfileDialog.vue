<template>
  <el-dialog
    v-model="visible"
    :width="layoutMode === 'compact' ? 640 : 500"
    :class="['user-profile-dialog', `layout-${layoutMode}`, 'dingtalk-dialog-fade']"
    :show-close="false"
    :close-on-click-modal="true"
    destroy-on-close
    append-to-body
  >
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else-if="userInfo" class="dialog-container">
      <!-- compact 模式：左右分栏简洁布局 -->
      <template v-if="layoutMode === 'compact'">
        <button class="close-btn close-btn-compact" @click="handleClose">
          <el-icon><Close /></el-icon>
        </button>
        <div class="compact-layout">
          <!-- 左侧：用户信息 -->
          <div class="compact-left">
            <div class="avatar-wrapper">
              <DingtalkAvatar
                :src="userInfo?.avatar"
                :name="userName"
                :user-id="targetUserId"
                :size="88"
                shape="circle"
              />
            </div>
            <h3 class="user-name-compact">{{ userName }}</h3>
            <div class="user-tags-compact">
              <span v-if="userInfo.position" class="tag">
                <span class="material-icons-outlined tag-icon">badge</span>
                {{ userInfo.position }}
              </span>
              <span v-if="userInfo.department" class="tag">
                <span class="material-icons-outlined tag-icon">business</span>
                {{ userInfo.department }}
              </span>
              <span v-if="genderIcon" class="tag gender-tag">
                <span class="material-icons-outlined tag-icon">{{ genderIcon }}</span>
                {{ genderText }}
              </span>
            </div>
          </div>

          <!-- 右侧：信息和操作 -->
          <div class="compact-right">
            <!-- 信息列表 -->
            <div class="info-list-compact">
              <div class="info-item">
                <span class="material-icons-outlined info-icon">person</span>
                <span class="info-label">用户名</span>
                <span class="info-value">{{ userInfo.username || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="material-icons-outlined info-icon">phone</span>
                <span class="info-label">手机</span>
                <span class="info-value">{{ userInfo.mobile || userInfo.phone || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="material-icons-outlined info-icon">email</span>
                <span class="info-label">邮箱</span>
                <span class="info-value">{{ userInfo.email || '未设置' }}</span>
              </div>
              <div v-if="userInfo.birthday" class="info-item">
                <span class="material-icons-outlined info-icon">cake</span>
                <span class="info-label">生日</span>
                <span class="info-value">{{ formatDate(userInfo.birthday) }}</span>
              </div>
              <div v-if="userInfo.lastOnlineTime" class="info-item">
                <span class="material-icons-outlined info-icon">schedule</span>
                <span class="info-label">最近在线</span>
                <span class="info-value">{{ formatDateTime(userInfo.lastOnlineTime) }}</span>
              </div>
              <div v-if="userInfo.signature" class="info-item full-width">
                <span class="material-icons-outlined info-icon">format_quote</span>
                <span class="info-value signature">{{ userInfo.signature }}</span>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="action-group-compact">
              <button class="action-btn primary" @click="handleSendMessage">
                <span class="material-icons-outlined">chat_bubble</span>
                发消息
              </button>
              <button class="action-btn" @click="handleVoiceCall">
                <span class="material-icons-outlined">phone_in_talk</span>
                语音
              </button>
              <button class="action-btn" @click="handleVideoCall">
                <span class="material-icons-outlined">videocam</span>
                视频
              </button>
            </div>
          </div>
        </div>
      </template>

      <!-- default 模式：原始垂直布局 -->
      <template v-else>
        <!-- 顶部：用户基本信息 + 关闭按钮 -->
        <div class="dialog-header">
          <button class="close-btn" @click="handleClose">
            <el-icon><Close /></el-icon>
          </button>

          <!-- 用户信息 -->
          <div class="user-info-section">
            <DingtalkAvatar
              :src="userInfo?.avatar"
              :name="userName"
              :user-id="targetUserId"
              :size="64"
              shape="circle"
              custom-class="user-avatar-large"
            />
            <div class="user-basic-info">
              <h2 class="user-name">{{ userName }}</h2>
              <p class="user-id">ID: {{ userInfo.username || targetUserId }}</p>
              <div v-if="userInfo.signature" class="user-signature">{{ userInfo.signature }}</div>
            </div>
          </div>

          <!-- 状态标签 -->
          <div class="user-status-tags">
            <el-tag v-if="userInfo.online" type="success" size="small" effect="plain">
              <span class="status-dot online"></span>在线
            </el-tag>
            <el-tag v-else type="info" size="small" effect="plain">
              <span class="status-dot offline"></span>离线
            </el-tag>
            <el-tag v-if="userInfo.department" type="info" size="small" effect="plain">
              {{ userInfo.department }}
            </el-tag>
          </div>
        </div>

        <!-- 快捷操作区 -->
        <div class="dialog-actions">
          <el-button type="primary" @click="handleSendMessage">
            <el-icon><ChatDotRound /></el-icon>发消息
          </el-button>
          <el-button @click="handleVoiceCall">
            <el-icon><Phone /></el-icon>语音
          </el-button>
          <el-button @click="handleVideoCall">
            <el-icon><VideoCamera /></el-icon>视频
          </el-button>
          <el-dropdown @command="handleMoreCommand" trigger="click">
            <el-button>
              <el-icon><MoreFilled /></el-icon>更多
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="set-remark">
                  <el-icon><Edit /></el-icon>设置备注
                </el-dropdown-item>
                <el-dropdown-item command="favorite">
                  <el-icon><Star /></el-icon>{{ isFavorite ? '取消常用' : '设为常用' }}
                </el-dropdown-item>
                <el-dropdown-item command="history">
                  <el-icon><Clock /></el-icon>聊天记录
                </el-dropdown-item>
                <el-dropdown-item command="share">
                  <el-icon><Share /></el-icon>分享名片
                </el-dropdown-item>
                <el-dropdown-item v-if="!isCurrentUser" command="block" divided class="danger">
                  <el-icon><CircleClose /></el-icon>加入黑名单
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <!-- Tab 内容区 -->
        <el-tabs v-model="activeTab" class="dialog-tabs">
        <!-- 资料 Tab -->
        <el-tab-pane label="资料" name="info">
          <div class="tab-content">
            <!-- 基本信息 -->
            <div class="info-section">
              <h4 class="section-title">基本信息</h4>
              <div class="info-row">
                <span class="label">手机</span>
                <span class="value">{{ userInfo.mobile || userInfo.phone || '-' }}</span>
                <el-button
                  v-if="userInfo.mobile || userInfo.phone"
                  link
                  size="small"
                  @click="copyToClipboard(userInfo.mobile || userInfo.phone)"
                >
                  <el-icon><CopyDocument /></el-icon>
                </el-button>
              </div>
              <div class="info-row">
                <span class="label">邮箱</span>
                <span class="value">{{ userInfo.email || '-' }}</span>
                <el-button
                  v-if="userInfo.email"
                  link
                  size="small"
                  @click="copyToClipboard(userInfo.email)"
                >
                  <el-icon><CopyDocument /></el-icon>
                </el-button>
              </div>
              <div class="info-row">
                <span class="label">部门</span>
                <span class="value">{{ userInfo.department || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="label">职位</span>
                <span class="value">{{ userInfo.position || '-' }}</span>
              </div>
            </div>

            <!-- 备注 -->
            <div class="info-section">
              <div class="section-title">
                <h4>备注</h4>
                <el-button link size="small" @click="handleSetRemark">
                  <el-icon><Edit /></el-icon>编辑
                </el-button>
              </div>
              <p class="remark-text">{{ userInfo.remark || '暂无备注' }}</p>
            </div>
          </div>
        </el-tab-pane>

        <!-- 共同群组 Tab -->
        <el-tab-pane name="groups">
          <template #label>
            共同群组
            <el-badge v-if="commonGroups.length > 0" :value="commonGroups.length" :max="99" />
          </template>
          <div class="tab-content">
            <div v-if="commonGroups.length === 0" class="empty-state">
              <el-icon :size="48"><ChatLineSquare /></el-icon>
              <p>暂无共同群组</p>
            </div>
            <div v-else class="groups-list">
              <div
                v-for="group in commonGroups"
                :key="group.id"
                class="group-item"
                @click="handleGroupClick(group)"
              >
                <DingtalkAvatar
                  :src="group.avatar"
                  :name="group.name"
                  :size="40"
                  shape="square"
                />
                <div class="group-details">
                  <div class="group-name">{{ group.name }}</div>
                  <div class="group-meta">{{ group.memberCount || 0 }} 人</div>
                </div>
                <el-icon><ArrowRight /></el-icon>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 更多 Tab -->
        <el-tab-pane label="更多" name="more">
          <div class="tab-content">
            <div class="more-list">
              <div class="more-item" @click="handleToggleFavorite">
                <div class="more-icon" :class="{ active: isFavorite }">
                  <el-icon><Star /></el-icon>
                </div>
                <div class="more-content">
                  <span class="more-name">{{ isFavorite ? '取消常用联系人' : '添加为常用联系人' }}</span>
                  <span class="more-desc">{{ isFavorite ? '从常用联系人列表移除' : '添加到常用联系人列表' }}</span>
                </div>
              </div>
              <div class="more-item" @click="handleShareCard">
                <div class="more-icon">
                  <el-icon><Share /></el-icon>
                </div>
                <div class="more-content">
                  <span class="more-name">分享名片</span>
                  <span class="more-desc">将该用户名片分享给其他人</span>
                </div>
              </div>
              <div v-if="!isCurrentUser" class="more-item danger" @click="handleBlock">
                <div class="more-icon danger">
                  <el-icon><CircleClose /></el-icon>
                </div>
                <div class="more-content">
                  <span class="more-name">加入黑名单</span>
                  <span class="more-desc">屏蔽该用户的消息</span>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
      </template>
    </div>

    <!-- 备注编辑弹窗 (仅 default 模式) -->
    <el-dialog
      v-if="layoutMode === 'default'"
      v-model="showRemarkDialog"
      title="设置备注"
      width="400px"
      append-to-body
    >
      <el-form :model="remarkForm" label-position="top">
        <el-form-item label="备注名称">
          <el-input
            v-model="remarkForm.remark"
            placeholder="请输入备注名称"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="标签">
          <el-select
            v-model="remarkForm.tags"
            multiple
            filterable
            allow-create
            placeholder="添加标签"
            style="width: 100%"
          >
            <el-option
              v-for="tag in presetTags"
              :key="tag"
              :label="tag"
              :value="tag"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRemarkDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveRemark">确定</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Close, ChatDotRound, Phone, VideoCamera, Edit, Star,
  Clock, Share, CircleClose, CopyDocument, ArrowRight,
  ChatLineSquare, MoreFilled
} from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { getUserInfo } from '@/api/im/user'
import { getCommonGroups } from '@/api/im/group'
import { formatDateISO, formatDateTimeISO, copyToClipboard } from '@/utils/format'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  session: { type: Object, default: null },
  layoutMode: { type: String, default: 'default' } // 'default' | 'compact'
})

const emit = defineEmits([
  'update:modelValue', 'send-message', 'voice-call', 'video-call',
  'history', 'share', 'group-click'
])

const store = useStore()
const currentUser = computed(() => store.getters['user/currentUser'] || {})

// 响应式数据
const visible = ref(false)
const userInfo = ref(null)
const loading = ref(false)
const activeTab = ref('info')
const isFavorite = ref(false)
const showRemarkDialog = ref(false)
const remarkForm = ref({ remark: '', tags: [] })
const presetTags = ref(['同事', '朋友', '客户', '领导', '重要'])
const commonGroups = ref([])

// 计算属性
const targetUserId = computed(() => {
  return props.session?.targetUserId || props.session?.userId || props.session?.targetId
})

const userName = computed(() => {
  if (!userInfo.value) return ''
  return userInfo.value.remark || userInfo.value.nickname || userInfo.value.username || '未知用户'
})

const isCurrentUser = computed(() => {
  return currentUser.value?.id == targetUserId.value
})

// 性别相关计算属性（用于 compact 模式）
const genderIcon = computed(() => {
  const gender = userInfo.value?.gender
  if (gender === 1) return 'male'
  if (gender === 2) return 'female'
  return null
})

const genderText = computed(() => {
  const gender = userInfo.value?.gender
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return null
})

// 加载用户信息
const loadUserInfo = async () => {
  if (!props.session) return
  loading.value = true
  try {
    const userId = targetUserId.value
    if (userId) {
      const res = await getUserInfo(userId)
      if (res.code === 200) {
        userInfo.value = {
          ...res.data,
          online: Math.random() > 0.5
        }
        await loadCommonGroups(userId)
      }
    } else {
      userInfo.value = { ...props.session, online: false }
    }
  } catch (error) {
    userInfo.value = { ...props.session, online: false }
  } finally {
    loading.value = false
  }
}

// 加载共同群组
const loadCommonGroups = async (userId) => {
  if (isCurrentUser.value) {
    commonGroups.value = []
    return
  }
  try {
    const res = await getCommonGroups(userId)
    if (res.code === 200) {
      commonGroups.value = res.data || []
    }
  } catch (e) {
    console.error('加载共同群组失败', e)
    commonGroups.value = []
  }
}

// 监听弹窗打开
watch(() => props.modelValue, (isOpen) => {
  visible.value = isOpen
  if (isOpen) {
    activeTab.value = 'info'
    loadUserInfo()
  }
})

watch(visible, (val) => {
  if (!val) emit('update:modelValue', false)
})

// 事件处理
const handleClose = () => {
  visible.value = false
}

const handleSendMessage = () => {
  emit('send-message', props.session)
  handleClose()
}

const handleVoiceCall = () => {
  emit('voice-call', props.session)
}

const handleVideoCall = () => {
  emit('video-call', props.session)
}

const handleSetRemark = () => {
  remarkForm.value = {
    remark: userInfo.value?.remark || '',
    tags: userInfo.value?.tags || []
  }
  showRemarkDialog.value = true
}

const handleSaveRemark = () => {
  userInfo.value = {
    ...userInfo.value,
    remark: remarkForm.value.remark,
    tags: remarkForm.value.tags
  }
  showRemarkDialog.value = false
  ElMessage.success('备注已保存')
}

const handleToggleFavorite = () => {
  isFavorite.value = !isFavorite.value
  ElMessage.success(isFavorite.value ? '已添加到常用联系人' : '已取消常用联系人')
}

const handleViewHistory = () => {
  emit('history', props.session)
  handleClose()
}

const handleShareCard = () => {
  emit('share', userInfo.value)
}

const handleBlock = () => {
  ElMessageBox.confirm(
    `确定要将 ${userName.value} 加入黑名单吗？加入后将不再接收该用户的消息。`,
    '加入黑名单',
    { type: 'warning', confirmButtonText: '确定加入', cancelButtonText: '取消' }
  ).then(() => {
    ElMessage.success('已加入黑名单')
  }).catch(() => {})
}

const handleGroupClick = (group) => {
  emit('group-click', group)
}

const handleMoreCommand = (command) => {
  switch (command) {
    case 'set-remark':
      handleSetRemark()
      break
    case 'favorite':
      handleToggleFavorite()
      break
    case 'history':
      handleViewHistory()
      break
    case 'share':
      handleShareCard()
      break
    case 'block':
      handleBlock()
      break
  }
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.user-profile-dialog {
  :deep(.el-dialog) {
    border-radius: var(--dt-radius-xl);
    overflow: hidden;
  }

  :deep(.el-dialog__header) {
    display: none;
  }

  :deep(.el-dialog__body) {
    padding: 0;
  }

  :deep(.el-dialog__close) {
    z-index: 1;
  }
}

.loading-state {
  padding: 40px 24px;
}

.dialog-container {
  display: flex;
  flex-direction: column;
  min-height: 400px;
}

// ====== 头部区域 ======
.dialog-header {
  padding: 20px 24px 16px;
  border-bottom: 1px solid var(--dt-border-light);
  background: var(--dt-bg-sidebar-gradient);
  position: relative;
}

.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
  border-radius: var(--dt-radius-full);
  border: none;
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--dt-transition-base);

  &:hover {
    background: rgba(255, 255, 255, 0.3);
  }

  .el-icon {
    font-size: 18px;
  }
}

.user-info-section {
  display: flex;
  gap: 16px;
  align-items: center;
}

.user-avatar-large {
  flex-shrink: 0;
  box-shadow: var(--dt-shadow-float);
}

.user-basic-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 4px;
}

.user-id {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0 0 8px;
}

.user-signature {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-status-tags {
  display: flex;
  gap: 8px;
}

.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: var(--dt-radius-full);
  margin-right: 4px;

  &.online {
    background: var(--dt-success-color);
  }

  &.offline {
    background: var(--dt-text-tertiary);
  }
}

// ====== 快捷操作区 ======
.dialog-actions {
  display: flex;
  gap: 8px;
  padding: 12px 24px;
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);

  .el-button {
    flex: 1;
    height: 36px;

    .el-icon {
      margin-right: 4px;
    }
  }
}

// ====== Tab 内容区 ======
.dialog-tabs {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  :deep(.el-tabs__header) {
    margin: 0;
    padding: 0 24px;
    background: var(--dt-bg-card);
    border-bottom: 1px solid var(--dt-border-light);
  }

  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }

  :deep(.el-tabs__item) {
    height: 48px;
    line-height: 48px;
    font-size: 14px;
    color: var(--dt-text-secondary);
    padding: 0 16px;
    margin: 0 4px;

    &.is-active {
      color: var(--dt-brand-color);
      font-weight: 500;
    }
  }

  :deep(.el-tabs__active-bar) {
    height: 2px;
    background: var(--dt-brand-color);
  }

  :deep(.el-tabs__content) {
    flex: 1;
    overflow-y: auto;
    padding: 16px 24px;
  }
}

.tab-content {
  min-height: 200px;
}

// ====== 资料Tab ======
.info-section {
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0 0 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;

  h4 {
    margin: 0;
  }

  .el-button {
    font-size: 13px;
    padding: 0;
  }
}

.info-row {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid var(--dt-border-lighter);

  &:last-child {
    border-bottom: none;
  }

  .label {
    width: 60px;
    font-size: 13px;
    color: var(--dt-text-secondary);
    flex-shrink: 0;
  }

  .value {
    flex: 1;
    font-size: 14px;
    color: var(--dt-text-primary);
  }

  .el-button {
    margin-left: 8px;
  }
}

.remark-text {
  font-size: 14px;
  color: var(--dt-text-secondary);
  line-height: 1.6;
  margin: 0;
  min-height: 40px;
  white-space: pre-wrap;
}

// ====== 空状态 ======
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: var(--dt-text-tertiary);

  .el-icon {
    font-size: 48px;
    color: var(--dt-border-color);
    margin-bottom: 12px;
  }

  p {
    margin: 0;
    font-size: 14px;
  }
}

// ====== 群组列表 ======
.groups-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.group-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  background: var(--dt-bg-tertiary);
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all var(--dt-transition-base);

  &:hover {
    background: var(--dt-brand-bg);
  }

  .group-details {
    flex: 1;
    min-width: 0;
  }

  .group-name {
    font-size: 14px;
    font-weight: 500;
    color: var(--dt-text-primary);
  }

  .group-meta {
    font-size: 12px;
    color: var(--dt-text-tertiary);
  }

  .el-icon {
    color: var(--dt-text-tertiary);
  }
}

// ====== 更多Tab ======
.more-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.more-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--dt-bg-tertiary);
  border-radius: var(--dt-radius-md);
  cursor: pointer;
  transition: all var(--dt-transition-base);

  &:hover {
    background: var(--dt-bg-hover);
  }

  &.danger {
    &:hover {
      background: var(--dt-error-bg);
    }
  }

  .more-icon {
    width: 36px;
    height: 36px;
    border-radius: var(--dt-radius-md);
    background: var(--dt-bg-card);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    .el-icon {
      font-size: 18px;
      color: var(--dt-text-secondary);
    }

    &.active {
      background: var(--dt-warning-bg);

      .el-icon {
        color: var(--dt-warning-color);
      }
    }

    &.danger {
      background: var(--dt-error-bg);

      .el-icon {
        color: var(--dt-error-color);
      }
    }
  }

  .more-content {
    flex: 1;
    display: flex;
    flex-direction: column;

    .more-name {
      font-size: 14px;
      color: var(--dt-text-primary);
      font-weight: 500;
    }

    .more-desc {
      font-size: 12px;
      color: var(--dt-text-tertiary);
    }
  }
}

// ====== 暗色模式 ======
:global(.dark) {
  .user-profile-dialog {
    :deep(.el-dialog) {
      background: var(--dt-bg-card-dark);
    }
  }

  .dialog-tabs {
    :deep(.el-tabs__header) {
      background: var(--dt-bg-card-dark);
      border-color: var(--dt-border-dark);
    }

    :deep(.el-tabs__item) {
      color: var(--dt-text-secondary-dark);

      &.is-active {
        color: var(--dt-brand-color);
      }
    }
  }

  .dialog-actions {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  .info-row {
    border-color: var(--dt-border-dark);
  }

  .group-item,
  .more-item {
    background: var(--dt-bg-tertiary-dark);
  }

  .group-name,
  .more-name {
    color: var(--dt-text-primary-dark);
  }

  .group-meta,
  .more-desc {
    color: var(--dt-text-tertiary-dark);
  }

  // ====== Compact 模式暗色适配 ======
  .info-list-compact .info-item {
    &:hover {
      background: var(--dt-bg-hover-dark);
    }
  }

  .action-group-compact .action-btn:not(.primary) {
    background: var(--dt-bg-hover-dark);
  }
}

// ====== Compact 模式样式 ======
.layout-compact {
  :deep(.el-dialog__body) {
    padding: 32px;
  }
}

.close-btn-compact {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
  border-radius: var(--dt-radius-full);
  border: none;
  background: var(--dt-bg-hover);
  color: var(--dt-text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  z-index: 10;

  &:hover {
    background: var(--dt-border);
  }
}

.compact-layout {
  display: flex;
  gap: 32px;

  .compact-left {
    display: flex;
    flex-direction: column;
    align-items: center;
    min-width: 120px;
    text-align: center;

    .avatar-wrapper {
      position: relative;
      display: inline-block;
    }

    .user-name-compact {
      font-size: 18px;
      font-weight: 500;
      color: var(--dt-text-primary);
      margin: 14px 0 12px;
      max-width: 120px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .user-tags-compact {
      display: flex;
      flex-direction: column;
      gap: 6px;
      width: 100%;

      .tag {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 4px;
        font-size: 12px;
        color: var(--dt-text-secondary);
        background: var(--dt-bg-secondary);
        padding: 5px 10px;
        border-radius: var(--dt-radius-sm);
        white-space: nowrap;

        .tag-icon {
          font-size: 13px;
        }

        &.gender-tag {
          .tag-icon {
            font-size: 15px;
          }
        }
      }
    }
  }

  .compact-right {
    flex: 1;
    display: flex;
    flex-direction: column;
  }
}

.info-list-compact {
  display: flex;
  flex-direction: column;
  gap: 3px;
  margin-bottom: 24px;

  .info-item {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 12px 16px;
    border-radius: var(--dt-radius-md);
    transition: background 0.2s;

    &:hover {
      background: var(--dt-bg-secondary);
    }

    &.full-width {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
    }

    .info-icon {
      font-size: 20px;
      color: var(--dt-text-tertiary);
      width: 22px;
      text-align: center;
    }

    .info-label {
      font-size: 13px;
      color: var(--dt-text-tertiary);
      min-width: 60px;
    }

    .info-value {
      flex: 1;
      font-size: 15px;
      color: var(--dt-text-primary);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;

      &.signature {
        white-space: normal;
        color: var(--dt-text-secondary);
        line-height: 1.5;
      }
    }
  }
}

.action-group-compact {
  display: flex;
  gap: 10px;
  margin-top: auto;

  .action-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    height: 42px;
    padding: 0 18px;
    border-radius: var(--dt-radius-md);
    font-size: 15px;
    cursor: pointer;
    transition: all 0.2s;
    border: 1px solid var(--dt-border-light);
    background: var(--dt-bg-card);

    .material-icons-outlined {
      font-size: 19px;
      color: var(--dt-text-secondary);
    }

    &.primary {
      flex: 1;
      background: var(--dt-brand-color);
      border-color: var(--dt-brand-color);
      color: #fff;

      .material-icons-outlined {
        color: #fff;
      }
    }

    &:not(.primary) {
      background: var(--dt-bg-secondary);
      color: var(--dt-text-secondary);
    }

    &:hover {
      opacity: 0.85;
    }
  }
}

// ====== 以下为原有的暗色模式样式（保持兼容）=====
:global(.dark) {
  .user-profile-dialog {
    :deep(.el-dialog) {
      background: var(--dt-bg-card-dark);
    }
  }

  .dialog-tabs {
    :deep(.el-tabs__header) {
      background: var(--dt-bg-card-dark);
      border-color: var(--dt-border-dark);
    }

    :deep(.el-tabs__item) {
      color: var(--dt-text-secondary-dark);

      &.is-active {
        color: var(--dt-brand-color);
      }
    }
  }

  .dialog-actions {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  .info-row {
    border-color: var(--dt-border-dark);
  }

  .group-item,
  .more-item {
    background: var(--dt-bg-tertiary-dark);
  }

  .group-name,
  .more-name {
    color: var(--dt-text-primary-dark);
  }

  .group-meta,
  .more-desc {
    color: var(--dt-text-tertiary-dark);
  }
}
</style>

<!-- 钉钉风格弹窗淡入动画 -->
<style lang="scss">
// 弹窗遮罩淡入（使用全局 dialogFadeIn 动画）
.dingtalk-dialog-fade .el-overlay {
  animation: dialogFadeIn 0.2s ease-out;
}

// 弹窗内容淡入缩放（使用全局 dialogZoomIn 动画）
.dingtalk-dialog-fade .el-dialog {
  animation: dialogZoomIn 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

// 使用全局对话框动画 (@/styles/animations.scss):
// - dialogFadeIn: 遮罩淡入
// - dialogZoomIn: 内容缩放淡入
</style>
