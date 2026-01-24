<template>
  <el-drawer
    v-model="visible"
    :title="drawerTitle"
    direction="rtl"
    size="400px"
    :before-close="handleClose"
  >
    <div v-if="userInfo" class="user-detail-content">
      <!-- 用户头像和基本信息 -->
      <div class="user-header">
        <div class="avatar-container">
          <div class="avatar-large" :class="getAvatarBgClass()">
            {{ getAvatarText }}
          </div>
          <span v-if="!isGroup && userInfo.online" class="online-badge">在线</span>
        </div>
        
        <div class="user-basic-info">
          <h2 class="user-name">{{ userName }}</h2>
          <p v-if="!isGroup" class="user-position">
            {{ userInfo.position || '职位未知' }} · {{ userInfo.department || '部门未知' }}
          </p>
          <p v-else class="member-count">
            {{ userInfo.memberCount || 0 }} 名成员
          </p>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button type="primary" :icon="ChatDotRound" @click="handleSendMessage">
          发消息
        </el-button>
        <el-button :icon="Phone" @click="handleVoiceCall">
          语音通话
        </el-button>
        <el-button :icon="Videocam" @click="handleVideoCall">
          视频通话
        </el-button>
      </div>

      <el-divider />

      <!-- 详细信息 -->
      <div class="detail-info">
        <template v-if="isGroup">
          <!-- 群组信息 -->
          <div class="info-section">
            <h3 class="section-title">群信息</h3>
            <div class="info-item">
              <span class="info-label">群公告</span>
              <span class="info-value">{{ userInfo.notice || '暂无公告' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">群描述</span>
              <span class="info-value">{{ userInfo.description || '暂无描述' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">群主</span>
              <span class="info-value">{{ userInfo.ownerName || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">创建时间</span>
              <span class="info-value">{{ formatDate(userInfo.createTime) }}</span>
            </div>
          </div>
        </template>
        <template v-else>
          <!-- 个人信息 -->
          <div class="info-section">
            <h3 class="section-title">个人信息</h3>
            <div class="info-item">
              <span class="info-label">
                <span class="material-icons-outlined">phone</span>
                手机号
              </span>
              <span class="info-value">{{ userInfo.phone || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">
                <span class="material-icons-outlined">email</span>
                邮箱
              </span>
              <span class="info-value">{{ userInfo.email || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">
                <span class="material-icons-outlined">work</span>
                部门
              </span>
              <span class="info-value">{{ userInfo.department || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">
                <span class="material-icons-outlined">badge</span>
                工号
              </span>
              <span class="info-value">{{ userInfo.employeeId || '-' }}</span>
            </div>
          </div>

          <el-divider />

          <div class="info-section">
            <h3 class="section-title">个性签名</h3>
            <p class="signature">{{ userInfo.signature || '这个人很懒,什么都没留下' }}</p>
          </div>
        </template>
      </div>

      <!-- 更多操作 -->
      <div class="more-actions">
        <el-button text @click="handleViewProfile">
          <span class="material-icons-outlined">person</span>
          查看完整资料
        </el-button>
      </div>
    </div>
    
    <div v-else class="loading-state">
      <el-skeleton :rows="8" animated />
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Phone, Videocam } from '@element-plus/icons-vue'
import { getUserInfo } from '@/api/im/user'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  session: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'send-message', 'voice-call', 'video-call'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const userInfo = ref(null)
const loading = ref(false)

// 判断是否为群组
const isGroup = computed(() => props.session?.type === 'GROUP')

// 抽屉标题
const drawerTitle = computed(() => isGroup.value ? '群详情' : '用户详情')

// 用户名称
const userName = computed(() => {
  if (!userInfo.value) return ''
  return isGroup.value ? userInfo.value.name : userInfo.value.nickname || userInfo.value.username
})

// 头像文字
const getAvatarText = computed(() => {
  return userName.value ? userName.value.charAt(0).toUpperCase() : '?'
})

// 头像背景色
const getAvatarBgClass = () => {
  if (isGroup.value) return 'bg-primary'
  const colors = ['bg-blue', 'bg-orange', 'bg-emerald', 'bg-purple']
  const index = (props.session?.id || 0) % colors.length
  return colors[index]
}

// 格式化日期
const formatDate = (timestamp) => {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 加载用户信息
const loadUserInfo = async () => {
  if (!props.session) return
  
  loading.value = true
  try {
    // 根据会话类型获取不同的信息
    if (isGroup.value) {
      // 群组信息(这里需要调用群组API)
      // const res = await getGroupInfo(props.session.id)
      // 临时使用会话信息
      userInfo.value = {
        ...props.session,
        online: false
      }
    } else {
      // 获取用户信息
      const targetUserId = props.session.targetUserId || props.session.userId
      if (targetUserId) {
        const res = await getUserInfo(targetUserId)
        if (res.code === 200) {
          userInfo.value = {
            ...res.data,
            online: Math.random() > 0.3 // 临时模拟在线状态
          }
        }
      } else {
        // 使用会话信息
        userInfo.value = {
          ...props.session,
          online: false
        }
      }
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('加载用户信息失败')
  } finally {
    loading.value = false
  }
}

// 监听会话变化
watch(() => props.session, (newSession) => {
  if (newSession && props.modelValue) {
    loadUserInfo()
  }
}, { immediate: true })

// 监听抽屉打开
watch(() => props.modelValue, (isOpen) => {
  if (isOpen && props.session) {
    loadUserInfo()
  }
})

// 关闭抽屉
const handleClose = () => {
  visible.value = false
  userInfo.value = null
}

// 发送消息
const handleSendMessage = () => {
  emit('send-message', props.session)
  handleClose()
}

// 语音通话
const handleVoiceCall = () => {
  emit('voice-call', props.session)
  ElMessage.info('语音通话功能开发中')
}

// 视频通话
const handleVideoCall = () => {
  emit('video-call', props.session)
  ElMessage.info('视频通话功能开发中')
}

// 查看完整资料
const handleViewProfile = () => {
  ElMessage.info('查看完整资料功能开发中')
}
</script>

<style scoped lang="scss">
.user-detail-content {
  padding: 0 4px;
}

.user-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 0;
  text-align: center;
}

.avatar-container {
  position: relative;
  margin-bottom: 16px;
}

.avatar-large {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 600;
  color: #fff;
  
  &.bg-primary { background: #1677ff; }
  &.bg-blue { background: #3b82f6; }
  &.bg-orange { background: #f97316; }
  &.bg-emerald { background: #10b981; }
  &.bg-purple { background: #a855f7; }
}

.online-badge {
  position: absolute;
  bottom: -4px;
  right: -4px;
  background: #52c41a;
  color: #fff;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  border: 2px solid #fff;
}

.user-basic-info {
  width: 100%;
}

.user-name {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 8px 0;
}

.user-position,
.member-count {
  font-size: 13px;
  color: #8c8c8c;
  margin: 0;
}

.action-buttons {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  
  .el-button {
    flex: 1;
  }
}

.detail-info {
  margin-top: 16px;
}

.info-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 12px 0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  
  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #8c8c8c;
  
  .material-icons-outlined {
    font-size: 18px;
  }
}

.info-value {
  font-size: 13px;
  color: #262626;
  text-align: right;
  max-width: 60%;
  word-break: break-all;
}

.signature {
  font-size: 13px;
  color: #595959;
  line-height: 1.6;
  margin: 0;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.more-actions {
  margin-top: 16px;
  
  .el-button {
    width: 100%;
    justify-content: flex-start;
    color: #1677ff;
    
    .material-icons-outlined {
      margin-right: 8px;
      font-size: 20px;
    }
  }
}

.loading-state {
  padding: 24px;
}

/* 深色模式 */
:deep(.dark) {
  .user-name {
    color: #f1f5f9;
  }
  
  .user-position,
  .member-count {
    color: #94a3b8;
  }
  
  .section-title {
    color: #f1f5f9;
  }
  
  .info-label {
    color: #94a3b8;
  }
  
  .info-value {
    color: #e2e8f0;
  }
  
  .info-item {
    border-color: #334155;
  }
  
  .signature {
    background: rgba(51, 65, 85, 0.4);
    color: #cbd5e1;
  }
}
</style>
