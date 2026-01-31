<template>
  <el-dialog
    v-model="visible"
    :width="720"
    class="user-profile-dialog"
    :show-close="false"
    :close-on-click-modal="true"
    destroy-on-close
    append-to-body
  >
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else-if="userInfo" class="profile-container">
      <!-- 左侧：用户基本信息和快捷操作 -->
      <div class="profile-sidebar">
        <!-- 关闭按钮 -->
        <el-button class="close-btn" circle @click="handleClose">
          <el-icon><Close /></el-icon>
        </el-button>

        <div class="sidebar-content">
          <div class="avatar-wrapper">
            <DingtalkAvatar
              :src="userInfo?.avatar"
              :name="userName"
              :user-id="targetUserId"
              :size="100"
              shape="circle"
            />
            <div v-if="userInfo.online" class="online-indicator" title="在线"></div>
          </div>

          <h2 class="user-name">{{ userName }}</h2>
          <p class="user-account">@{{ userInfo.username }}</p>

          <div class="user-tags">
            <el-tag v-if="userInfo.position" size="small" type="primary" effect="light">
              {{ userInfo.position }}
            </el-tag>
            <el-tag v-if="userInfo.department" size="small" type="info" effect="light">
              {{ userInfo.department }}
            </el-tag>
          </div>

          <div class="quick-actions">
            <el-button type="primary" size="large" @click="handleSendMessage">
              <el-icon><ChatDotRound /></el-icon>发消息
            </el-button>
            <div class="secondary-actions">
              <el-button size="large" @click="handleVoiceCall">
                <el-icon><Phone /></el-icon>语音
              </el-button>
              <el-button size="large" @click="handleVideoCall">
                <el-icon><VideoCamera /></el-icon>视频
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：详细信息和功能 -->
      <div class="profile-main">
        <el-tabs v-model="activeTab" class="profile-tabs">
          <!-- 资料 Tab -->
          <el-tab-pane label="详细资料" name="info">
            <div class="tab-content">
              <!-- 联系信息 -->
              <div class="info-section">
                <div class="section-title">联系信息</div>
                <div class="info-grid">
                  <div v-if="userInfo.mobile || userInfo.phone" class="info-item">
                    <span class="info-label">
                      <el-icon><Iphone /></el-icon>手机
                    </span>
                    <span class="info-value">{{ userInfo.mobile || userInfo.phone }}</span>
                    <el-button
                      link
                      type="primary"
                      class="copy-btn"
                      @click="copyToClipboard(userInfo.mobile || userInfo.phone)"
                    >
                      <el-icon><CopyDocument /></el-icon>复制
                    </el-button>
                  </div>
                  <div v-if="userInfo.email" class="info-item">
                    <span class="info-label">
                      <el-icon><Message /></el-icon>邮箱
                    </span>
                    <span class="info-value">{{ userInfo.email }}</span>
                    <el-button
                      link
                      type="primary"
                      class="copy-btn"
                      @click="copyToClipboard(userInfo.email)"
                    >
                      <el-icon><CopyDocument /></el-icon>复制
                    </el-button>
                  </div>
                  <div class="info-item">
                    <span class="info-label">
                      <el-icon><User /></el-icon>账号
                    </span>
                    <span class="info-value">{{ userInfo.username }}</span>
                  </div>
                </div>
              </div>

              <!-- 工作信息 -->
              <div class="info-section">
                <div class="section-title">工作信息</div>
                <div class="info-grid">
                  <div v-if="userInfo.department" class="info-item">
                    <span class="info-label">
                      <el-icon><OfficeBuilding /></el-icon>部门
                    </span>
                    <span class="info-value">{{ userInfo.department }}</span>
                  </div>
                  <div v-if="userInfo.position" class="info-item">
                    <span class="info-label">
                      <el-icon><Postcard /></el-icon>职位
                    </span>
                    <span class="info-value">{{ userInfo.position }}</span>
                  </div>
                </div>
              </div>

              <!-- 个人简介 -->
              <div class="info-section">
                <div class="section-title">个人简介</div>
                <div class="signature-box">
                  {{ userInfo.signature || '这个人很懒，什么都没留下～' }}
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 共同群组 Tab -->
          <el-tab-pane name="groups">
            <template #label>
              <span>共同群组</span>
              <el-badge v-if="commonGroups.length > 0" :value="commonGroups.length" :max="99" class="tab-badge" />
            </template>
            <div class="tab-content">
              <div v-if="commonGroups.length === 0" class="empty-state">
                <el-icon :size="48" color="#dcdfe6"><ChatLineSquare /></el-icon>
                <p>暂无共同群组</p>
              </div>
              <div v-else class="groups-grid">
                <div
                  v-for="group in commonGroups"
                  :key="group.id"
                  class="group-card"
                  @click="handleGroupClick(group)"
                >
                  <DingtalkAvatar
                    :src="group.avatar"
                    :name="group.name"
                    :size="48"
                    shape="square"
                  />
                  <div class="group-info">
                    <div class="group-name">{{ group.name }}</div>
                    <div class="group-meta">{{ group.memberCount || 0 }} 人</div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 操作 Tab -->
          <el-tab-pane label="更多操作" name="actions">
            <div class="tab-content">
              <div class="action-list">
                <div class="action-item" @click="handleSetRemark">
                  <div class="action-icon">
                    <el-icon><Edit /></el-icon>
                  </div>
                  <div class="action-content">
                    <span class="action-name">设置备注</span>
                    <span class="action-desc">添加备注名和标签</span>
                  </div>
                  <el-icon class="action-arrow"><ArrowRight /></el-icon>
                </div>
                <div class="action-item" @click="handleToggleFavorite">
                  <div class="action-icon" :class="{ active: isFavorite }">
                    <el-icon><Star /></el-icon>
                  </div>
                  <div class="action-content">
                    <span class="action-name">{{ isFavorite ? '取消常用联系人' : '添加为常用联系人' }}</span>
                    <span class="action-desc">{{ isFavorite ? '从常用联系人中移除' : '添加到常用联系人列表' }}</span>
                  </div>
                  <el-icon class="action-arrow"><ArrowRight /></el-icon>
                </div>
                <div class="action-item" @click="handleViewHistory">
                  <div class="action-icon">
                    <el-icon><Clock /></el-icon>
                  </div>
                  <div class="action-content">
                    <span class="action-name">查看聊天记录</span>
                    <span class="action-desc">查看与该用户的所有聊天记录</span>
                  </div>
                  <el-icon class="action-arrow"><ArrowRight /></el-icon>
                </div>
                <div class="action-item" @click="handleShareCard">
                  <div class="action-icon">
                    <el-icon><Share /></el-icon>
                  </div>
                  <div class="action-content">
                    <span class="action-name">分享名片</span>
                    <span class="action-desc">将该用户名片分享给其他人</span>
                  </div>
                  <el-icon class="action-arrow"><ArrowRight /></el-icon>
                </div>
                <div v-if="!isCurrentUser" class="action-item danger" @click="handleBlock">
                  <div class="action-icon">
                    <el-icon><CircleClose /></el-icon>
                  </div>
                  <div class="action-content">
                    <span class="action-name">加入黑名单</span>
                    <span class="action-desc">屏蔽该用户的消息</span>
                  </div>
                  <el-icon class="action-arrow"><ArrowRight /></el-icon>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </el-dialog>

  <!-- 备注编辑弹窗 -->
  <el-dialog
    v-model="showRemarkDialog"
    title="设置备注"
    width="400px"
    class="remark-dialog"
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
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Close, ChatDotRound, Phone, VideoCamera, Edit, Star,
  Clock, Share, CircleClose, Iphone, Message, User,
  OfficeBuilding, Postcard, CopyDocument, ArrowRight,
  ChatLineSquare
} from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { getUserInfo } from '@/api/im/user'
import { getCommonGroups } from '@/api/im/group'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  session: { type: Object, default: null }
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
        // 加载共同群组
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
    loadUserInfo()
    activeTab.value = 'info'
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
    { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' }
  ).then(() => {
    ElMessage.success('已加入黑名单')
  }).catch(() => {})
}

const handleGroupClick = (group) => {
  emit('group-click', group)
}

// 工具函数
const copyToClipboard = async (text) => {
  if (!text) return
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    // 降级处理
    const textArea = document.createElement('textarea')
    textArea.value = text
    textArea.style.position = 'fixed'
    textArea.style.left = '-999999px'
    document.body.appendChild(textArea)
    textArea.focus()
    textArea.select()
    document.execCommand('copy')
    document.body.removeChild(textArea)
    ElMessage.success('已复制到剪贴板')
  }
}
</script>

<style scoped lang="scss">
.user-profile-dialog {
  :deep(.el-dialog) {
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  }

  :deep(.el-dialog__header) {
    display: none;
  }

  :deep(.el-dialog__body) {
    padding: 0;
  }
}

.loading-state {
  padding: 60px 40px;
}

.profile-container {
  display: flex;
  min-height: 500px;
  max-height: 700px;
}

// 左侧边栏
.profile-sidebar {
  width: 260px;
  background: linear-gradient(180deg, var(--dt-brand-color) 0%, #0e5fd9 100%);
  color: #fff;
  padding: 32px 24px;
  position: relative;
  display: flex;
  flex-direction: column;

  .close-btn {
    position: absolute;
    top: 16px;
    right: 16px;
    background: rgba(255, 255, 255, 0.2);
    border: none;
    color: #fff;

    &:hover {
      background: rgba(255, 255, 255, 0.3);
    }
  }

  .sidebar-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
  }

  .avatar-wrapper {
    position: relative;
    margin-bottom: 20px;

    :deep(.dingtalk-avatar) {
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
    }

    .online-indicator {
      position: absolute;
      bottom: 6px;
      right: 6px;
      width: 20px;
      height: 20px;
      background: #52c41a;
      border: 3px solid #fff;
      border-radius: 50%;
    }
  }

  .user-name {
    font-size: 24px;
    font-weight: 600;
    margin: 0 0 8px;
    color: #fff;
    text-align: center;
  }

  .user-account {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.8);
    margin: 0 0 16px;
  }

  .user-tags {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 8px;
    margin-bottom: 32px;

    :deep(.el-tag) {
      background: rgba(255, 255, 255, 0.2);
      border-color: rgba(255, 255, 255, 0.3);
      color: #fff;
    }
  }

  .quick-actions {
    width: 100%;

    .el-button {
      width: 100%;
      margin-bottom: 12px;
      background: #fff;
      border-color: #fff;
      color: var(--dt-brand-color);
      font-size: 15px;
      height: 44px;

      &:hover {
        background: rgba(255, 255, 255, 0.9);
      }

      .el-icon {
        margin-right: 6px;
      }
    }

    .secondary-actions {
      display: flex;
      gap: 12px;

      .el-button {
        flex: 1;
        background: rgba(255, 255, 255, 0.15);
        border: 1px solid rgba(255, 255, 255, 0.3);
        color: #fff;
        margin-bottom: 0;

        &:hover {
          background: rgba(255, 255, 255, 0.25);
        }
      }
    }
  }
}

// 右侧主内容区
.profile-main {
  flex: 1;
  background: var(--dt-bg-body);
  display: flex;
  flex-direction: column;
  overflow: hidden;

  :deep(.profile-tabs) {
    flex: 1;
    display: flex;
    flex-direction: column;

    .el-tabs__header {
      margin: 0;
      padding: 0 20px;
      background: #fff;
      border-bottom: 1px solid var(--dt-border-lighter);
    }

    .el-tabs__nav-wrap::after {
      display: none;
    }

    .el-tabs__item {
      height: 56px;
      line-height: 56px;
      font-size: 14px;
      color: var(--dt-text-secondary);

      &.is-active {
        color: var(--dt-brand-color);
        font-weight: 500;
      }
    }

    .el-tabs__active-bar {
      height: 3px;
      border-radius: 2px;
    }

    .el-tabs__content {
      flex: 1;
      overflow-y: auto;
      padding: 20px;
    }
  }
}

.tab-content {
  min-height: 100%;
}

// Tab 徽标
.tab-badge {
  :deep(.el-badge__content) {
    margin-left: 6px;
    margin-top: -2px;
  }
}

// 区域标题
.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin-bottom: 12px;
  padding-left: 12px;
  border-left: 3px solid var(--dt-brand-color);
}

// 信息区域
.info-section {
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }
}

// 信息网格
.info-grid {
  background: #fff;
  border-radius: 12px;
  border: 1px solid var(--dt-border-lighter);
  overflow: hidden;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid var(--dt-border-lighter);

  &:last-child {
    border-bottom: none;
  }

  .info-label {
    display: flex;
    align-items: center;
    gap: 8px;
    width: 80px;
    font-size: 14px;
    color: var(--dt-text-secondary);
    flex-shrink: 0;

    .el-icon {
      font-size: 16px;
    }
  }

  .info-value {
    flex: 1;
    font-size: 14px;
    color: var(--dt-text-primary);
    word-break: break-all;
  }

  .copy-btn {
    margin-left: 8px;
    font-size: 13px;

    .el-icon {
      margin-right: 4px;
    }
  }
}

// 个人简介
.signature-box {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid var(--dt-border-lighter);
  font-size: 14px;
  color: var(--dt-text-secondary);
  line-height: 1.6;
  min-height: 80px;
}

// 共同群组网格
.groups-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.group-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid var(--dt-border-lighter);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: var(--dt-brand-color);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }

  .group-info {
    flex: 1;
    min-width: 0;

    .group-name {
      font-size: 14px;
      color: var(--dt-text-primary);
      font-weight: 500;
      margin-bottom: 4px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .group-meta {
      font-size: 12px;
      color: var(--dt-text-tertiary);
    }
  }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--dt-text-secondary);

  p {
    margin-top: 16px;
    font-size: 14px;
  }
}

// 操作列表
.action-list {
  background: #fff;
  border-radius: 12px;
  border: 1px solid var(--dt-border-lighter);
  overflow: hidden;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 1px solid var(--dt-border-lighter);

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: var(--dt-bg-session-hover);
  }

  .action-icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    background: var(--dt-bg-body);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    .el-icon {
      font-size: 20px;
      color: var(--dt-text-secondary);
    }

    &.active {
      background: #fff7e6;

      .el-icon {
        color: #faad14;
      }
    }
  }

  .action-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 2px;

    .action-name {
      font-size: 14px;
      color: var(--dt-text-primary);
      font-weight: 500;
    }

    .action-desc {
      font-size: 12px;
      color: var(--dt-text-tertiary);
    }
  }

  .action-arrow {
    color: var(--dt-text-tertiary);
  }

  &.danger {
    .action-icon {
      background: #fff2f0;

      .el-icon {
        color: #ff4d4f;
      }
    }

    .action-name {
      color: #ff4d4f;
    }
  }
}

// 弹窗样式
.remark-dialog {
  :deep(.el-dialog__body) {
    padding-top: 20px;
  }
}

// 响应式适配
@media (max-width: 768px) {
  .user-profile-dialog {
    :deep(.el-dialog) {
      width: 95% !important;
      margin: 20px auto;
    }
  }

  .profile-container {
    flex-direction: column;
    min-height: auto;
    max-height: 85vh;
  }

  .profile-sidebar {
    width: 100%;
    padding: 24px;

    .sidebar-content {
      flex-direction: row;
      align-items: flex-start;
      gap: 20px;
    }

    .avatar-wrapper {
      margin-bottom: 0;
    }

    .user-info {
      flex: 1;
      text-align: left;
    }

    .user-tags {
      justify-content: flex-start;
      margin-bottom: 16px;
    }

    .quick-actions {
      .el-button {
        margin-bottom: 8px;
      }
    }
  }

  .groups-grid {
    grid-template-columns: 1fr;
  }
}
</style>
