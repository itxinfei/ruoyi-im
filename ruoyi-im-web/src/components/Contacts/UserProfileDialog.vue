<template>
  <el-dialog v-model="visible" :width="layoutMode === 'compact' ? 640 : 480"
    :class="['user-profile-dialog', `layout-${layoutMode}`, 'dingtalk-dialog-fade']" :show-close="false"
    :close-on-click-modal="true" destroy-on-close append-to-body>
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else-if="userInfo" class="dialog-container">
      <!-- 关闭按钮 -->
      <button class="close-btn" @click="handleClose">
        <el-icon>
          <Close />
        </el-icon>
      </button>

      <!-- 头部区域：头像与基本信息 -->
      <div class="dialog-header">
        <div class="user-info-section">
          <DingtalkAvatar :src="userInfo?.avatar" :name="userName" :user-id="targetUserId" :size="64" shape="circle"
            custom-class="user-avatar-large" />
          <div class="user-basic-info">
            <h2 class="user-name">{{ userName }}</h2>
            <p class="user-id">ID: {{ userInfo.username || targetUserId }}</p>
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
        </div>

        <!-- 签名/简介 -->
        <div v-if="userInfo.signature" class="user-signature-box">
          {{ userInfo.signature }}
        </div>
      </div>

      <!-- 快捷操作区：统一按钮容器 -->
      <div class="dialog-actions-refined">
        <button class="refined-action-btn primary" @click="handleSendMessage">
          <el-icon>
            <ChatDotRound />
          </el-icon>
          <span>发消息</span>
        </button>
        <button class="refined-action-btn" @click="handleVoiceCall">
          <el-icon>
            <Phone />
          </el-icon>
          <span>语音</span>
        </button>
        <button class="refined-action-btn" @click="handleVideoCall">
          <el-icon>
            <VideoCamera />
          </el-icon>
          <span>视频</span>
        </button>
        <el-dropdown trigger="click" @command="handleMoreCommand">
          <button class="refined-action-btn more">
            <el-icon>
              <MoreFilled />
            </el-icon>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="set-remark">
                <el-icon>
                  <Edit />
                </el-icon>设置备注
              </el-dropdown-item>
              <el-dropdown-item command="favorite">
                <el-icon>
                  <Star />
                </el-icon>{{ isFavorite ? '取消常用' : '设为常用' }}
              </el-dropdown-item>
              <el-dropdown-item command="history">
                <el-icon>
                  <Clock />
                </el-icon>聊天记录
              </el-dropdown-item>
              <el-dropdown-item command="share">
                <el-icon>
                  <Share />
                </el-icon>分享名片
              </el-dropdown-item>
              <el-dropdown-item v-if="!isCurrentUser" command="block" divided class="danger">
                <el-icon>
                  <CircleClose />
                </el-icon>加入黑名单
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <!-- 内容区：Tabs -->
      <el-tabs v-model="activeTab" class="dialog-tabs">
        <el-tab-pane label="资料" name="info">
          <div class="tab-content">
            <div class="info-section">
              <h4 class="section-title">基本信息</h4>
              <div class="info-row">
                <span class="label">手机</span>
                <span class="value">{{ userInfo.mobile || userInfo.phone || '-' }}</span>
                <el-button v-if="userInfo.mobile || userInfo.phone" link size="small"
                  @click="copyToClipboard(userInfo.mobile || userInfo.phone)">
                  <el-icon>
                    <CopyDocument />
                  </el-icon>
                </el-button>
              </div>
              <div class="info-row">
                <span class="label">邮箱</span>
                <span class="value">{{ userInfo.email || '-' }}</span>
                <el-button v-if="userInfo.email" link size="small" @click="copyToClipboard(userInfo.email)">
                  <el-icon>
                    <CopyDocument />
                  </el-icon>
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

            <div class="info-section">
              <div class="section-title">
                <h4>备注</h4>
                <el-button link size="small" @click="handleSetRemark">
                  <el-icon>
                    <Edit />
                  </el-icon>编辑
                </el-button>
              </div>
              <p class="remark-text">{{ userInfo.remark || '暂无备注' }}</p>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane name="groups">
          <template #label>
            共同群组
            <el-badge v-if="commonGroups.length > 0" :value="commonGroups.length" :max="99" />
          </template>
          <div class="tab-content">
            <div v-if="commonGroups.length === 0" class="empty-state">
              <el-icon :size="48">
                <ChatLineSquare />
              </el-icon>
              <p>暂无共同群组</p>
            </div>
            <div v-else class="groups-list">
              <div v-for="group in commonGroups" :key="group.id" class="group-item" @click="handleGroupClick(group)">
                <DingtalkAvatar :src="group.avatar" :name="group.name" :size="40" shape="square" />
                <div class="group-details">
                  <div class="group-name">{{ group.name }}</div>
                  <div class="group-meta">{{ group.memberCount || 0 }} 人</div>
                </div>
                <el-icon>
                  <ArrowRight />
                </el-icon>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 备注编辑弹窗 -->
    <el-dialog v-model="showRemarkDialog" title="设置备注" width="400px" append-to-body>
      <el-form :model="remarkForm" label-position="top">
        <el-form-item label="备注名称">
          <el-input v-model="remarkForm.remark" placeholder="请输入备注名称" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="remarkForm.tags" multiple filterable allow-create placeholder="添加标签" style="width: 100%">
            <el-option v-for="tag in presetTags" :key="tag" :label="tag" :value="tag" />
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
  layoutMode: { type: String, default: 'default' }
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
        await loadCommonGroups(userId)
      }
    }
  } catch (error) {
    console.error('加载用户信息失败', error)
  } finally {
    loading.value = false
  }
}

const loadCommonGroups = async userId => {
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
    commonGroups.value = []
  }
}

watch(() => props.modelValue, isOpen => {
  visible.value = isOpen
  if (isOpen) {
    activeTab.value = 'info'
    loadUserInfo()
  }
})

watch(visible, val => {
  if (!val) emit('update:modelValue', false)
})

const handleClose = () => { visible.value = false }
const handleSendMessage = () => { emit('send-message', props.session); handleClose() }
const handleVoiceCall = () => { emit('voice-call', props.session) }
const handleVideoCall = () => { emit('video-call', props.session) }
const handleSetRemark = () => {
  remarkForm.value = {
    remark: userInfo.value?.remark || '',
    tags: userInfo.value?.tags || []
  }
  showRemarkDialog.value = true
}
const handleSaveRemark = () => {
  userInfo.value = { ...userInfo.value, remark: remarkForm.value.remark, tags: remarkForm.value.tags }
  showRemarkDialog.value = false
  ElMessage.success('备注已保存')
}
const handleToggleFavorite = () => {
  isFavorite.value = !isFavorite.value
  ElMessage.success(isFavorite.value ? '已添加到常用联系人' : '已取消常用联系人')
}
const handleViewHistory = () => { emit('history', props.session); handleClose() }
const handleShareCard = () => { emit('share', userInfo.value) }
const handleBlock = () => {
  ElMessageBox.confirm(`确定要将 ${userName.value} 加入黑名单吗？`, '提示', { type: 'warning' })
    .then(() => ElMessage.success('已加入黑名单'))
}
const handleGroupClick = group => { emit('group-click', group) }
const handleMoreCommand = command => {
  switch (command) {
    case 'set-remark': handleSetRemark(); break
    case 'favorite': handleToggleFavorite(); break
    case 'history': handleViewHistory(); break
    case 'share': handleShareCard(); break
    case 'block': handleBlock(); break
  }
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.user-profile-dialog {
  :deep(.el-dialog) {
    border-radius: var(--dt-radius-xl);
    overflow: hidden;
    padding: 0;
  }

  :deep(.el-dialog__header) {
    display: none;
  }

  :deep(.el-dialog__body) {
    padding: 0;
  }
}

.dialog-container {
  display: flex;
  flex-direction: column;
  min-height: 480px;
  background: var(--dt-bg-card);
}

.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 28px;
  height: 28px;
  border-radius: var(--dt-radius-sm);
  border: none;
  background: transparent;
  color: var(--dt-text-tertiary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 20;

  &:hover {
    background: var(--dt-bg-hover);
    color: var(--dt-text-primary);
  }
}

.dialog-header {
  padding: 40px 24px 20px;
  background: #fff;
}

.user-info-section {
  display: flex;
  gap: 20px;
  align-items: center;
}

.user-basic-info {
  flex: 1;
}

.user-name {
  font-size: 20px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0 0 6px;
}

.user-id {
  font-size: 13px;
  color: var(--dt-text-tertiary);
  margin: 0 0 10px;
}

.user-status-tags {
  display: flex;
  gap: 8px;
}

.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 4px;

  &.online {
    background: #52c41a;
  }

  &.offline {
    background: #bfbfbf;
  }
}

.user-signature-box {
  margin-top: 16px;
  padding: 12px;
  background: var(--dt-bg-tertiary);
  border-radius: var(--dt-radius-md);
  font-size: 13px;
  color: var(--dt-text-secondary);
  line-height: 1.5;
}

// 按钮区域
.dialog-actions-refined {
  display: flex;
  gap: 12px;
  padding: 0 24px 24px;
  background: #fff;

  .refined-action-btn {
    flex: 1;
    height: 36px;
    border: 1px solid var(--dt-border-light);
    background: #fff;
    border-radius: var(--dt-radius-md);
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    font-size: 14px;
    color: var(--dt-text-primary);
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: var(--dt-bg-hover);
      border-color: var(--dt-border);
    }

    &.primary {
      background: var(--dt-brand-color);
      border-color: var(--dt-brand-color);
      color: #fff;

      &:hover {
        filter: brightness(1.05);
      }
    }

    &.more {
      flex: 0 0 36px;
      padding: 0;
    }
  }
}

.dialog-tabs {
  flex: 1;
  background: #fff;

  :deep(.el-tabs__header) {
    margin: 0;
    padding: 0 24px;
    border-bottom: 1px solid var(--dt-border-lighter);
  }

  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }

  :deep(.el-tabs__content) {
    padding: 20px 24px;
  }
}

.info-section {
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;

  h4 {
    margin: 0;
    font-size: 14px;
    color: var(--dt-text-tertiary);
    font-weight: normal;
  }
}

.info-row {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid var(--dt-border-lighter);

  .label {
    width: 80px;
    font-size: 14px;
    color: var(--dt-text-secondary);
  }

  .value {
    flex: 1;
    font-size: 14px;
    color: var(--dt-text-primary);
  }
}

.remark-text {
  font-size: 14px;
  color: var(--dt-text-primary);
  background: var(--dt-bg-tertiary);
  padding: 12px;
  border-radius: var(--dt-radius-md);
}

.groups-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.group-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  border-radius: var(--dt-radius-md);
  cursor: pointer;

  &:hover {
    background: var(--dt-bg-hover);
  }

  .group-details {
    flex: 1;
  }

  .group-name {
    font-size: 14px;
    font-weight: 500;
  }

  .group-meta {
    font-size: 12px;
    color: var(--dt-text-tertiary);
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0;
  color: var(--dt-text-tertiary);
}
</style>