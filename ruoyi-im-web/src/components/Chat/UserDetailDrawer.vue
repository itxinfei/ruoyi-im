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
          <!-- 群组使用图标，单聊使用钉钉风格头像 -->
          <div v-if="isGroup" class="avatar-large group-avatar">
            <span class="material-icons-outlined">groups</span>
          </div>
          <DingtalkAvatar
            v-else
            :src="userInfo?.avatar"
            :name="userName"
            :user-id="session?.targetId || session?.targetUserId"
            :size="72"
            shape="square"
            custom-class="avatar-large"
          />
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
        <el-button :icon="VideoCamera" @click="handleVideoCall">
          视频通话
        </el-button>
      </div>

      <el-divider />

      <!-- Tab 页签内容 -->
      <el-tabs v-model="activeTab" class="detail-tabs">
        <el-tab-pane label="详情" name="info">
          <div class="detail-info">
            <template v-if="isGroup">
              <div class="info-section">
                <h3 class="section-title">群公告</h3>
                <p class="notice-box">{{ userInfo.notice || '暂无公告' }}</p>
              </div>
              <div class="info-section">
                <h3 class="section-title">群设置</h3>
                <div class="settings-list">
                  <div class="setting-row">
                    <span>允许成员邀请好友</span>
                    <el-switch size="small" :model-value="true" disabled />
                  </div>
                </div>
              </div>
            </template>
            <template v-else>
              <div class="info-section">
                <h3 class="section-title">个人信息</h3>
                <div class="info-item">
                  <span class="info-label"><el-icon><Phone /></el-icon> 手机号</span>
                  <span class="info-value">{{ userInfo.phone || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label"><el-icon><Message /></el-icon> 邮箱</span>
                  <span class="info-value">{{ userInfo.email || '-' }}</span>
                </div>
              </div>
              <div class="info-section">
                <h3 class="section-title">个性签名</h3>
                <p class="signature">{{ userInfo.signature || '这个人很懒，什么都没留下' }}</p>
              </div>
            </template>
          </div>
        </el-tab-pane>

        <el-tab-pane v-if="isGroup" label="成员" name="members">
          <div class="member-list">
            <div v-for="i in 5" :key="i" class="member-item">
              <el-avatar :size="32" shape="square">{{ '成员'.charAt(0) }}</el-avatar>
              <div class="member-info">
                <span class="member-name">群成员 {{ i }}</span>
                <span v-if="i === 1" class="owner-tag">群主</span>
              </div>
            </div>
            <el-button class="view-all-btn" text>查看全部成员</el-button>
          </div>
        </el-tab-pane>

        <el-tab-pane label="文件" name="files">
          <div class="file-list">
            <div v-for="i in 3" :key="i" class="file-item">
              <el-icon class="file-icon"><Document /></el-icon>
              <div class="file-info">
                <span class="file-name">项目文档_v{{ i }}.pdf</span>
                <span class="file-meta">2.4 MB · 2026-01-24</span>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

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
import { ChatDotRound, Phone, VideoCamera, Suitcase, Message, Document } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
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
const activeTab = ref('info')

const isGroup = computed(() => props.session?.type === 'GROUP')
const drawerTitle = computed(() => isGroup.value ? '群详情' : '用户详情')

const userName = computed(() => {
  if (!userInfo.value) return ''
  return isGroup.value ? userInfo.value.name : userInfo.value.nickname || userInfo.value.username
})

const loadUserInfo = async () => {
  if (!props.session) return
  loading.value = true
  try {
    if (isGroup.value) {
      userInfo.value = { ...props.session, online: false }
    } else {
      const targetUserId = props.session.targetUserId || props.session.userId
      if (targetUserId) {
        const res = await getUserInfo(targetUserId)
        if (res.code === 200) {
          userInfo.value = { ...res.data, online: Math.random() > 0.3 }
        }
      } else {
        userInfo.value = { ...props.session, online: false }
      }
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('加载用户信息失败')
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (isOpen) => {
  if (isOpen && props.session) {
    loadUserInfo()
    activeTab.value = 'info'
  }
})

const handleClose = () => {
  visible.value = false
  userInfo.value = null
}

const handleSendMessage = () => {
  emit('send-message', props.session)
  handleClose()
}

const handleVoiceCall = () => {
  emit('voice-call', props.session)
  ElMessage.info('语音通话功能开发中')
}

const handleVideoCall = () => {
  emit('video-call', props.session)
  ElMessage.info('视频通话功能开发中')
}

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
  padding: 20px 0;
  text-align: center;
}

.avatar-container {
  position: relative;
  margin-bottom: 16px;
}

.avatar-large {
  border-radius: 12px;
}

.group-avatar {
  width: 72px;
  height: 72px;
  border-radius: 12px;
  background: #1677ff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
}

.online-badge {
  position: absolute;
  bottom: -4px;
  right: -4px;
  background: #52c41a;
  color: #fff;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 10px;
  border: 2px solid #fff;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 4px 0;
}

.user-position, .member-count {
  font-size: 13px;
  color: #64748b;
  margin: 0;
}

.action-buttons {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  .el-button { flex: 1; height: 36px; }
}

.detail-tabs {
  :deep(.el-tabs__header) { margin-bottom: 16px; }
  :deep(.el-tabs__nav-wrap::after) { height: 1px; }
}

.info-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  color: #94a3b8;
  margin: 0 0 12px 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
  &:last-child { border-bottom: none; }
}

.info-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #64748b;
  .el-icon { font-size: 16px; }
}

.info-value { font-size: 13px; color: #1e293b; font-weight: 500; }

.notice-box {
  background: #f8fafc;
  padding: 12px;
  border-radius: 8px;
  font-size: 13px;
  color: #475569;
  line-height: 1.6;
  margin: 0;
}

.signature {
  font-size: 13px;
  color: #64748b;
  font-style: italic;
  padding-left: 8px;
  border-left: 2px solid #e2e8f0;
}

.member-list {
  .member-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 0;
    border-bottom: 1px solid #f1f5f9;
  }
  .member-info { flex: 1; display: flex; justify-content: space-between; align-items: center; }
  .member-name { font-size: 14px; color: #1e293b; }
  .owner-tag { font-size: 10px; background: #e0f2fe; color: #0369a1; padding: 2px 6px; border-radius: 4px; }
  .view-all-btn { width: 100%; margin-top: 8px; }
}

.file-list {
  .file-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 0;
    border-bottom: 1px solid #f1f5f9;
  }
  .file-icon { font-size: 24px; color: #3b82f6; }
  .file-info { display: flex; flex-direction: column; }
  .file-name { font-size: 13px; color: #1e293b; font-weight: 500; margin-bottom: 2px; }
  .file-meta { font-size: 11px; color: #94a3b8; }
}

.more-actions {
  margin-top: 24px;
  .el-button { width: 100%; justify-content: flex-start; color: #1677ff; }
}

.loading-state { padding: 40px; text-align: center; }

/* 暗色模式适配 */
:deep(.dark) {
  .user-name, .info-value, .member-name, .file-name { color: #f1f5f9; }
  .notice-box { background: rgba(30, 41, 59, 0.5); color: #cbd5e1; }
  .info-item, .member-item, .file-item { border-color: #334155; }
  .signature { border-color: #334155; color: #94a3b8; }
}
</style>
