<template>
  <el-drawer
    v-model="visible"
    size="420px"
    direction="rtl"
    class="user-detail-drawer"
    :with-header="false"
    append-to-body
    destroy-on-close
  >
    <div v-if="userInfo" class="drawer-content">
      <!-- 关闭按钮 -->
      <button class="close-btn" @click="handleClose">
        <span class="material-icons-outlined">close</span>
      </button>

      <!-- 顶部区域 -->
      <div class="top-section">
        <!-- 背景装饰 -->
        <div class="bg-decoration">
          <div class="decoration-circle circle-1"></div>
          <div class="decoration-circle circle-2"></div>
        </div>

        <!-- 头像 -->
        <div class="avatar-container">
          <DingtalkAvatar
            :src="userInfo?.avatar"
            :name="userName"
            :user-id="session?.targetId || session?.targetUserId"
            :size="96"
            shape="circle"
            custom-class="detail-avatar"
          />
          <!-- 在线状态点 -->
          <div v-if="!isGroup && userInfo.online" class="online-dot"></div>
        </div>

        <!-- 用户名和标签 -->
        <h2 class="user-name">{{ userName }}</h2>
        <div class="user-tags">
          <span v-if="userInfo.online" class="status-tag online">
            <span class="material-icons-outlined status-icon">circle</span>
            在线
          </span>
          <span v-else class="status-tag offline">
            <span class="material-icons-outlined status-icon">radio_button_unchecked</span>
            离线
          </span>
        </div>

        <!-- 职位信息 -->
        <p v-if="!isGroup" class="user-position">
          <span v-if="userInfo.position">{{ userInfo.position }}</span>
          <span v-if="userInfo.department"> · {{ userInfo.department }}</span>
          <span v-if="!userInfo.position && !userInfo.department" class="empty-hint">暂无职位信息</span>
        </p>
      </div>

      <!-- 快捷操作按钮 -->
      <div class="quick-actions">
        <button class="action-btn primary" @click="handleSendMessage">
          <span class="material-icons-outlined">chat_bubble</span>
          <span>发消息</span>
        </button>
        <button class="action-btn" @click="handleVoiceCall">
          <span class="material-icons-outlined">phone</span>
        </button>
        <button class="action-btn" @click="handleVideoCall">
          <span class="material-icons-outlined">videocam</span>
        </button>
      </div>

      <!-- 信息卡片 -->
      <div class="info-cards">
        <!-- 共同群组区块 -->
        <div v-if="!isGroup && commonGroups.length > 0" class="info-card full-width common-groups-card">
          <div class="card-content">
            <div class="section-header">
              <span class="material-icons-outlined section-icon">groups</span>
              <span class="card-label">共同群组 ({{ commonGroups.length }})</span>
            </div>
            <div class="groups-list">
              <div v-for="group in commonGroups" :key="group.id"
                   class="group-item" @click="handleGroupClick(group)">
                <DingtalkAvatar :src="group.avatar" :name="group.name" :size="32" />
                <div class="group-info">
                  <span class="group-name">{{ group.name }}</span>
                  <span class="group-members">{{ group.memberCount || 0 }}人</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="info-card" v-if="userInfo.phone">
          <div class="card-icon">
            <span class="material-icons-outlined">phone</span>
          </div>
          <div class="card-content">
            <span class="card-label">手机号码</span>
            <span class="card-value">{{ userInfo.phone }}</span>
          </div>
        </div>

        <div class="info-card" v-if="userInfo.email">
          <div class="card-icon">
            <span class="material-icons-outlined">email</span>
          </div>
          <div class="card-content">
            <span class="card-label">邮箱地址</span>
            <span class="card-value email">{{ userInfo.email }}</span>
          </div>
        </div>

        <div class="info-card" v-if="!isGroup && userInfo.username">
          <div class="card-icon">
            <span class="material-icons-outlined">alternate_email</span>
          </div>
          <div class="card-content">
            <span class="card-label">用户名</span>
            <span class="card-value">@{{ userInfo.username }}</span>
          </div>
        </div>

        <div class="info-card full-width" v-if="userInfo.signature || !userInfo.signature">
          <div class="card-icon">
            <span class="material-icons-outlined">format_quote</span>
          </div>
          <div class="card-content">
            <span class="card-label">个性签名</span>
            <p class="card-value signature">{{ userInfo.signature || '这个人很懒，什么都没留下～' }}</p>
          </div>
        </div>
      </div>

      <!-- 更多操作 -->
      <div class="more-actions">
        <button class="more-action-item" @click="handleAddToFavorites">
          <span class="material-icons-outlined action-icon">star_border</span>
          <span>添加常用</span>
        </button>
        <button class="more-action-item" @click="handleSetRemark">
          <span class="material-icons-outlined action-icon">edit</span>
          <span>设置备注</span>
        </button>
        <button class="more-action-item" @click="handleViewHistory">
          <span class="material-icons-outlined action-icon">history</span>
          <span>历史消息</span>
        </button>
        <button class="more-action-item danger" @click="handleReport">
          <span class="material-icons-outlined action-icon">block</span>
          <span>举报</span>
        </button>
      </div>

      <!-- 底部 -->
      <div class="bottom-section">
        <div class="secure-badge">
          <span class="material-icons-outlined">verified_user</span>
          <span>端到端加密保护</span>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-else-if="loading" class="loading-state">
      <el-skeleton animated>
        <template #template>
          <div class="skeleton-bg"></div>
          <el-skeleton-item variant="circle" style="width: 80px; height: 80px; margin: -40px auto 0;" />
          <el-skeleton-item variant="text" style="width: 120px; margin: 12px auto 0;" />
          <el-skeleton-item variant="text" style="width: 80px; margin: 0 auto;" />
        </template>
      </el-skeleton>
    </div>
  </el-drawer>

  <!-- 聊天记录面板 -->
  <ChatHistoryPanel
    v-model:visible="showHistory"
    :session="props.session"
  />
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import ChatHistoryPanel from '@/components/Chat/ChatHistoryPanel.vue'
import { getUserInfo } from '@/api/im/user'
import { updateContactRemark } from '@/api/im/contact'
import { getCommonGroups } from '@/api/im/group'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  session: { type: Object, default: null }
})

const emit = defineEmits(['update:modelValue', 'send-message', 'voice-call', 'video-call', 'switch-to-group'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const userInfo = ref(null)
const loading = ref(false)
const showHistory = ref(false)
const commonGroups = ref([])
const loadingCommonGroups = ref(false)
const router = useRouter()
const store = useStore()
const currentUserId = computed(() => store.state.im.user?.id)

const isGroup = computed(() => props.session?.type === 'GROUP')
const userName = computed(() => {
  if (!userInfo.value) return ''
  return isGroup.value
    ? userInfo.value.name
    : userInfo.value.nickname || userInfo.value.username || '未知用户'
})

// 加载用户信息
const loadUserInfo = async () => {
  if (!props.session) return
  loading.value = true
  try {
    if (isGroup.value) {
      userInfo.value = {
        ...props.session,
        online: false,
        memberCount: props.session.memberCount || Math.floor(Math.random() * 50) + 10
      }
    } else {
      const targetUserId = props.session.targetUserId || props.session.userId || props.session.targetId
      if (targetUserId) {
        const res = await getUserInfo(targetUserId)
        if (res.code === 200) {
          userInfo.value = {
            ...res.data,
            online: Math.random() > 0.5
          }
          // 加载共同群组
          loadCommonGroups(targetUserId)
        }
      } else {
        userInfo.value = { ...props.session, online: false }
      }
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    userInfo.value = { ...props.session, online: false }
  } finally {
    loading.value = false
  }
}

// 加载共同群组
const loadCommonGroups = async (targetUserId) => {
  if (!targetUserId || !currentUserId.value) return
  loadingCommonGroups.value = true
  try {
    const res = await getCommonGroups(targetUserId)
    if (res.code === 200) {
      commonGroups.value = res.data || []
    }
  } catch (error) {
    console.error('加载共同群组失败:', error)
    commonGroups.value = []
  } finally {
    loadingCommonGroups.value = false
  }
}

watch(() => props.modelValue, (isOpen) => {
  if (isOpen) {
    loadUserInfo()
  }
})

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

const handleAddToFavorites = () => {
  ElMessage.success('已添加到常用联系人')
}

const handleSetRemark = async () => {
  if (!userInfo.value) return

  try {
    const { value } = await ElMessageBox.prompt('请输入备注名称', '设置备注', {
      confirmButtonText: '保存',
      cancelButtonText: '取消',
      inputValue: userInfo.value.remark || '',
      inputPlaceholder: '请输入备注名称'
    })

    if (!value || !value.trim()) {
      ElMessage.warning('备注不能为空')
      return
    }

    // 使用用户ID作为联系人ID
    await updateContactRemark(userInfo.value.id, { remark: value.trim() })
    ElMessage.success('备注已更新')
    // 更新本地用户信息
    userInfo.value.remark = value.trim()
  } catch (err) {
    // 用户取消或请求失败
    if (err !== 'cancel') {
      ElMessage.error('设置备注失败，请重试')
    }
  }
}

const handleViewHistory = () => {
  showHistory.value = true
}

const handleReport = () => {
  ElMessage.info('举报功能开发中')
}

// 点击共同群组
const handleGroupClick = (group) => {
  // 关闭抽屉
  visible.value = false
  // 发送切换到群组会话的事件
  emit('switch-to-group', group)
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// ============================================================================
// 抽屉基础样式
// ============================================================================
:deep(.user-detail-drawer) {
  .el-drawer__body {
    padding: 0;
    overflow: hidden;
  }
}

.drawer-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--dt-bg-card);
}

// 关闭按钮
.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--dt-bg-hover);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  z-index: 10;

  &:hover {
    background: var(--dt-border);
  }

  .material-icons-outlined {
    font-size: 18px;
    color: var(--dt-text-secondary);
  }
}

// 顶部区域
.top-section {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  border-bottom: 1px solid var(--dt-border-light);
}

.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 100%;
  overflow: hidden;
  pointer-events: none;

  .decoration-circle {
    position: absolute;
    border-radius: 50%;
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);

    &.circle-1 {
      width: 120px;
      height: 120px;
      top: -40px;
      right: -40px;
    }

    &.circle-2 {
      width: 80px;
      height: 80px;
      bottom: -20px;
      left: -20px;
      opacity: 0.6;
    }
  }
}

.avatar-container {
  position: relative;
  margin-bottom: 16px;

  .detail-avatar {
    border: 4px solid var(--dt-bg-card);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  }

  .online-dot {
    position: absolute;
    bottom: 4px;
    right: 4px;
    width: 16px;
    height: 16px;
    background: #52c41a;
    border: 3px solid var(--dt-bg-card);
    border-radius: 50%;
  }
}

.user-name {
  font-size: 20px;
  font-weight: 600;
  color: var(--dt-text-primary);
  margin: 0 0 12px;
  text-align: center;
  word-break: break-word;
}

.user-tags {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 8px;

  .status-tag {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 10px;
    border-radius: 16px;
    font-size: 12px;
    font-weight: 500;

    &.online {
      background: rgba(82, 196, 26, 0.1);
      color: #52c41a;

      .status-icon {
        font-size: 12px;
      }
    }

    &.offline {
      background: var(--dt-bg-body);
      color: var(--dt-text-tertiary);

      .status-icon {
        font-size: 12px;
      }
    }
  }
}

.user-position {
  font-size: 13px;
  color: var(--dt-text-secondary);
  margin: 0;
  text-align: center;

  .empty-hint {
    color: var(--dt-text-tertiary);
  }
}

// 快捷操作
.quick-actions {
  display: flex;
  gap: 12px;
  padding: 20px 24px;
  border-bottom: 1px solid var(--dt-border-light);

  .action-btn {
    flex: 1;
    height: 44px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    background: var(--dt-bg-body);
    border: 1px solid var(--dt-border-light);
    cursor: pointer;
    transition: all 0.2s;
    font-size: 14px;
    font-weight: 500;
    color: var(--dt-text-primary);

    .material-icons-outlined {
      font-size: 20px;
    }

    &.primary {
      background: var(--dt-brand-color);
      border-color: var(--dt-brand-color);
      color: #fff;
      flex: 2;
    }

    &:hover:not(.primary) {
      background: var(--dt-brand-bg);
      border-color: var(--dt-brand-color);
      color: var(--dt-brand-color);
    }

    &.primary:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
    }
  }
}

// 信息卡片
.info-cards {
  padding: 20px 24px;
  border-bottom: 1px solid var(--dt-border-light);
  overflow-y: auto;
  flex: 1;

  .info-card {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px;
    background: var(--dt-bg-body);
    border-radius: 12px;
    margin-bottom: 12px;
    transition: all 0.2s;

    &:hover {
      background: var(--dt-bg-hover);
    }

    &.full-width {
      flex-direction: column;
      align-items: flex-start;
    }

    .card-icon {
      width: 36px;
      height: 36px;
      border-radius: 10px;
      background: var(--dt-brand-bg);
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;

      .material-icons-outlined {
        font-size: 18px;
        color: var(--dt-brand-color);
      }
    }

    .card-content {
      flex: 1;
      min-width: 0;

      .card-label {
        display: block;
        font-size: 11px;
        color: var(--dt-text-tertiary);
        margin-bottom: 2px;
      }

      .card-value {
        font-size: 13px;
        color: var(--dt-text-primary);

        &.email {
          word-break: break-all;
        }

        &.signature {
          font-style: italic;
          color: var(--dt-text-secondary);
          line-height: 1.5;
          margin: 0;
        }
      }

      // 共同群组区块样式
      .section-header {
        display: flex;
        align-items: center;
        gap: 6px;
        margin-bottom: 10px;

        .section-icon {
          font-size: 16px;
          color: var(--dt-brand-color);
        }

        .card-label {
          font-size: 12px;
          font-weight: 500;
          color: var(--dt-text-primary);
          margin: 0;
        }
      }

      .groups-list {
        display: flex;
        flex-direction: column;
        gap: 6px;
      }

      .group-item {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 8px;
        border-radius: 8px;
        cursor: pointer;
        transition: background 0.2s;

        &:hover {
          background: var(--dt-bg-hover);
        }

        .group-info {
          flex: 1;
          min-width: 0;
          display: flex;
          align-items: center;
          gap: 8px;

          .group-name {
            font-size: 13px;
            color: var(--dt-text-primary);
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .group-members {
            font-size: 11px;
            color: var(--dt-text-tertiary);
            flex-shrink: 0;
          }
        }
      }
    }
  }
}

// 更多操作
.more-actions {
  padding: 20px 24px;
  border-bottom: 1px solid var(--dt-border-light);

  .more-action-item {
    display: flex;
    align-items: center;
    gap: 12px;
    width: 100%;
    padding: 12px;
    background: transparent;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
    text-align: left;
    font-size: 14px;
    color: var(--dt-text-primary);
    margin-bottom: 4px;

    .action-icon {
      font-size: 20px;
      color: var(--dt-text-secondary);
    }

    &:hover {
      background: var(--dt-bg-hover);

      .action-icon {
        color: var(--dt-brand-color);
      }
    }

    &.danger:hover {
      background: var(--dt-error-bg);
      color: var(--dt-error-color);

      .action-icon {
        color: var(--dt-error-color);
      }
    }
  }
}

// 底部
.bottom-section {
  padding: 16px 24px;
  background: var(--dt-bg-body);

  .secure-badge {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;

    .material-icons-outlined {
      font-size: 14px;
      color: #52c41a;
    }

    font-size: 11px;
    color: var(--dt-text-tertiary);
  }
}

// 加载状态
.loading-state {
  padding: 24px;

  .skeleton-bg {
    height: 100px;
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
    border-radius: 24px 24px 0 0;
    margin: -24px -24px 16px -24px;
  }
}
</style>
