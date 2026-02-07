<template>
  <!-- PC端侧边栏风格的用户详情 -->
  <transition name="slide-right">
    <div
      v-if="visible"
      class="user-profile-sidebar"
    >
      <div
        class="sidebar-overlay"
        @click="handleClose"
      />

      <div class="sidebar-panel">
        <!-- 头部 -->
        <div class="sidebar-header">
          <h3 class="header-title">
            个人信息
          </h3>
          <button
            class="close-btn"
            @click="handleClose"
          >
            <el-icon>
              <Close />
            </el-icon>
          </button>
        </div>

        <!-- 内容区 -->
        <div
          v-if="loading"
          class="sidebar-body"
        >
          <el-skeleton
            :rows="10"
            animated
          />
        </div>

        <div
          v-else-if="userInfo"
          class="sidebar-body"
        >
          <!-- 用户基本信息卡片 -->
          <div class="info-card">
            <div class="user-header-section">
              <DingtalkAvatar
                :src="userInfo.avatar"
                :name="userName"
                :user-id="targetUserId"
                :size="64"
                shape="square"
                class="user-avatar"
              />
              <div class="user-basic-info">
                <h4 class="user-name">
                  {{ userName }}
                </h4>
                <p class="user-id">
                  ID: {{ userInfo.username || targetUserId }}
                </p>
                <div class="user-status">
                  <span
                    v-if="userInfo.online"
                    class="status-badge online"
                  >
                    <span class="status-dot" />在线
                  </span>
                  <span
                    v-else
                    class="status-badge offline"
                  >
                    <span class="status-dot" />离线
                  </span>
                </div>
              </div>
            </div>

            <!-- 签名 -->
            <div
              v-if="userInfo.signature"
              class="user-signature"
            >
              {{ userInfo.signature }}
            </div>

            <!-- 快捷操作按钮 -->
            <div class="quick-actions">
              <button
                class="action-item primary"
                @click="handleSendMessage"
              >
                <el-icon>
                  <ChatDotRound />
                </el-icon>
                <span>发消息</span>
              </button>
              <button
                class="action-item"
                @click="handleVoiceCall"
              >
                <el-icon>
                  <Phone />
                </el-icon>
                <span>语音</span>
              </button>
              <button
                class="action-item"
                @click="handleVideoCall"
              >
                <el-icon>
                  <VideoCamera />
                </el-icon>
                <span>视频</span>
              </button>
            </div>
          </div>

          <!-- 详细信息 -->
          <div class="section-block">
            <div class="section-header">
              <span class="section-title">详细信息</span>
            </div>

            <div class="info-list">
              <div
                v-if="userInfo.department"
                class="info-item"
              >
                <span class="info-label">部门</span>
                <span class="info-value">{{ userInfo.department }}</span>
              </div>

              <div
                v-if="userInfo.position"
                class="info-item"
              >
                <span class="info-label">职位</span>
                <span class="info-value">{{ userInfo.position }}</span>
              </div>

              <div
                v-if="userInfo.email"
                class="info-item"
              >
                <span class="info-label">邮箱</span>
                <span class="info-value">{{ userInfo.email }}</span>
              </div>

              <div
                v-if="userInfo.phone"
                class="info-item"
              >
                <span class="info-label">手机</span>
                <span class="info-value">{{ userInfo.phone }}</span>
              </div>

              <div
                v-if="remark"
                class="info-item"
              >
                <span class="info-label">备注</span>
                <span class="info-value">{{ remark }}</span>
              </div>
            </div>
          </div>

          <!-- 更多操作 -->
          <div class="section-block">
            <div class="section-header">
              <span class="section-title">更多操作</span>
            </div>

            <div class="action-list">
              <div
                class="action-list-item"
                @click="handleSetRemark"
              >
                <el-icon>
                  <Edit />
                </el-icon>
                <span>设置备注</span>
                <el-icon class="arrow">
                  <ArrowRight />
                </el-icon>
              </div>

              <div
                class="action-list-item"
                @click="handleToggleFavorite"
              >
                <el-icon>
                  <Star />
                </el-icon>
                <span>{{ isFavorite ? '取消常用联系人' : '设为常用联系人' }}</span>
                <el-icon class="arrow">
                  <ArrowRight />
                </el-icon>
              </div>

              <div
                class="action-list-item"
                @click="handleViewHistory"
              >
                <el-icon>
                  <Clock />
                </el-icon>
                <span>聊天记录</span>
                <el-icon class="arrow">
                  <ArrowRight />
                </el-icon>
              </div>

              <div
                class="action-list-item"
                @click="handleShareCard"
              >
                <el-icon>
                  <Share />
                </el-icon>
                <span>分享名片</span>
                <el-icon class="arrow">
                  <ArrowRight />
                </el-icon>
              </div>
            </div>
          </div>

          <!-- 危险操作区 -->
          <div
            v-if="!isCurrentUser"
            class="section-block danger-zone"
          >
            <button
              class="danger-btn"
              @click="handleBlock"
            >
              加入黑名单
            </button>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Close,
  ChatDotRound,
  Phone,
  VideoCamera,
  Edit,
  Star,
  Clock,
  Share,
  ArrowRight
} from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { getUserInfo } from '@/api/im/user'

const props = defineProps({
  visible: { type: Boolean, default: false },
  userId: { type: [Number, String], default: null }
})

const emit = defineEmits(['update:visible', 'send-message', 'voice-call', 'video-call'])

const store = useStore()
const loading = ref(false)
const userInfo = ref(null)
const remark = ref('')
const isFavorite = ref(false)

const targetUserId = computed(() => props.userId)
const isCurrentUser = computed(() => {
  const currentUserId = store.getters['user/currentUserId'] || store.state.user.userInfo?.userId
  return targetUserId.value === currentUserId
})

const userName = computed(() => {
  if (remark.value) {return remark.value}
  return userInfo.value?.nickname || userInfo.value?.username || '未知用户'
})

watch(() => props.visible, v => {
  if (v && props.userId) {
    loadUserInfo()
  }
})

watch(() => props.userId, uid => {
  if (uid && props.visible) {
    loadUserInfo()
  }
})

const loadUserInfo = async () => {
  if (!props.userId) {return}
  loading.value = true
  try {
    const res = await getUserInfo(props.userId)
    if (res.code === 200) {
      userInfo.value = res.data
      // 加载备注和常用状态
      loadRemarkAndFavorite()
    }
  } catch (e) {
    ElMessage.error('加载用户信息失败')
  } finally {
    loading.value = false
  }
}

const loadRemarkAndFavorite = () => {
  // TODO: 从store或API加载备注和常用状态
  remark.value = ''
  isFavorite.value = false
}

const handleClose = () => {
  emit('update:visible', false)
}

const handleSendMessage = () => {
  emit('send-message', props.userId)
  handleClose()
}

const handleVoiceCall = () => {
  emit('voice-call', props.userId)
}

const handleVideoCall = () => {
  emit('video-call', props.userId)
}

const handleSetRemark = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入备注名', '设置备注', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: remark.value
    })
    if (value !== null) {
      remark.value = value
      ElMessage.success('备注设置成功')
    }
  } catch (e) {
    // 用户取消
  }
}

const handleToggleFavorite = () => {
  isFavorite.value = !isFavorite.value
  ElMessage.success(isFavorite.value ? '已设为常用联系人' : '已取消常用联系人')
}

const handleViewHistory = () => {
  ElMessage.info('聊天记录功能开发中')
}

const handleShareCard = () => {
  ElMessage.info('分享名片功能开发中')
}

const handleBlock = async () => {
  try {
    await ElMessageBox.confirm('确定要将该用户加入黑名单吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.success('已加入黑名单')
    handleClose()
  } catch (e) {
    // 用户取消
  }
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

// 侧边栏容器
.user-profile-sidebar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 2000;
  display: flex;
  justify-content: flex-end;
}

// 遮罩层
.sidebar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(2px);
}

// 侧边栏面板
.sidebar-panel {
  position: relative;
  width: 360px;
  height: 100%;
  background: #fff;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  z-index: 1;
}

// 头部
.sidebar-header {
  height: 56px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--dt-border-lighter);
  flex-shrink: 0;

  .header-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0;
  }

  .close-btn {
    width: 32px;
    height: 32px;
    border-radius: var(--dt-radius-md);
    border: none;
    background: transparent;
    color: var(--dt-text-secondary);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s var(--dt-ease-out);

    &:hover {
      background: var(--dt-bg-hover);
      color: var(--dt-text-primary);
    }
  }
}

// 内容区
.sidebar-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.1);
    border-radius: 3px;

    &:hover {
      background: rgba(0, 0, 0, 0.2);
    }
  }
}

// 信息卡片
.info-card {
  background: #f8f9fb;
  border-radius: var(--dt-radius-lg);
  padding: 20px;
  margin-bottom: 16px;
}

.user-header-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;

  .user-avatar {
    border-radius: 4px;
    flex-shrink: 0;
  }

  .user-basic-info {
    flex: 1;
    min-width: 0;

    .user-name {
      font-size: 16px;
      font-weight: 600;
      color: var(--dt-text-primary);
      margin: 0 0 4px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .user-id {
      font-size: 12px;
      color: var(--dt-text-tertiary);
      margin: 0 0 6px;
    }

    .user-status {
      .status-badge {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        padding: 2px 8px;
        border-radius: 12px;

        &.online {
          background: #e6f7ed;
          color: #22c55e;

          .status-dot {
            width: 6px;
            height: 6px;
            border-radius: 50%;
            background: #22c55e;
          }
        }

        &.offline {
          background: #f5f5f5;
          color: #999;

          .status-dot {
            width: 6px;
            height: 6px;
            border-radius: 50%;
            background: #999;
          }
        }
      }
    }
  }
}

.user-signature {
  font-size: 13px;
  color: var(--dt-text-secondary);
  line-height: 1.6;
  margin-bottom: 16px;
  padding: 12px;
  background: #fff;
  border-radius: var(--dt-radius-md);
}

// 快捷操作
.quick-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;

  .action-item {
    height: 64px;
    background: #fff;
    border: 1px solid var(--dt-border-lighter);
    border-radius: var(--dt-radius-md);
    cursor: pointer;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 6px;
    font-size: 13px;
    color: var(--dt-text-primary);
    transition: all 0.2s var(--dt-ease-out);

    &:hover {
      background: var(--dt-bg-hover);
      border-color: var(--dt-border);
    }

    &.primary {
      background: #4168e0;
      color: #fff;
      border-color: #4168e0;

      &:hover {
        background: #3558d0;
      }
    }

    .el-icon {
      font-size: 20px;
    }
  }
}

// 区块
.section-block {
  margin-bottom: 16px;

  .section-header {
    margin-bottom: 12px;

    .section-title {
      font-size: 14px;
      font-weight: 600;
      color: var(--dt-text-primary);
    }
  }
}

// 信息列表
.info-list {
  background: #fff;
  border: 1px solid var(--dt-border-lighter);
  border-radius: var(--dt-radius-md);
  overflow: hidden;

  .info-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 14px 16px;
    border-bottom: 1px solid var(--dt-border-lighter);

    &:last-child {
      border-bottom: none;
    }

    .info-label {
      font-size: 14px;
      color: var(--dt-text-secondary);
    }

    .info-value {
      font-size: 14px;
      color: var(--dt-text-primary);
      text-align: right;
      flex: 1;
      margin-left: 16px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

// 操作列表
.action-list {
  background: #fff;
  border: 1px solid var(--dt-border-lighter);
  border-radius: var(--dt-radius-md);
  overflow: hidden;

  .action-list-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 16px;
    border-bottom: 1px solid var(--dt-border-lighter);
    cursor: pointer;
    transition: background 0.2s var(--dt-ease-out);

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background: var(--dt-bg-hover);
    }

    .el-icon:first-child {
      font-size: 18px;
      color: var(--dt-text-secondary);
    }

    span {
      flex: 1;
      font-size: 14px;
      color: var(--dt-text-primary);
    }

    .arrow {
      font-size: 14px;
      color: var(--dt-text-tertiary);
    }
  }
}

// 危险操作区
.danger-zone {
  .danger-btn {
    width: 100%;
    height: 40px;
    background: #fff;
    border: 1px solid #f44336;
    border-radius: var(--dt-radius-md);
    color: #f44336;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s var(--dt-ease-out);

    &:hover {
      background: #f44336;
      color: #fff;
    }
  }
}

// 滑入动画
.slide-right-enter-active,
.slide-right-leave-active {
  transition: all 0.3s var(--dt-ease-out);

  .sidebar-overlay {
    transition: opacity 0.3s var(--dt-ease-out);
  }

  .sidebar-panel {
    transition: transform 0.3s var(--dt-ease-out);
  }
}

.slide-right-enter-from,
.slide-right-leave-to {
  .sidebar-overlay {
    opacity: 0;
  }

  .sidebar-panel {
    transform: translateX(100%);
  }
}

// 暗色模式
:global(.dark) {
  .sidebar-panel {
    background: var(--dt-bg-card-dark);
    border-left: 1px solid var(--dt-border-dark);
  }

  .info-card {
    background: var(--dt-bg-dark);
  }

  .user-signature {
    background: var(--dt-bg-card-dark);
  }

  .quick-actions .action-item {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);

    &:hover {
      background: rgba(255, 255, 255, 0.08);
    }

    &.primary {
      background: #4168e0;
      border-color: #4168e0;

      &:hover {
        background: #3558d0;
      }
    }
  }

  .info-list,
  .action-list {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);

    .info-item,
    .action-list-item {
      border-color: var(--dt-border-dark);

      &:hover {
        background: rgba(255, 255, 255, 0.05);
      }
    }
  }

  .danger-btn {
    background: var(--dt-bg-card-dark);
  }
}
</style>