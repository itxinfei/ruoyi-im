<template>
  <div class="settings-panel">
    <!-- 设置页面头部 -->
    <div class="settings-header">
      <h2>设置</h2>
    </div>

    <!-- 设置内容区 -->
    <div class="settings-content custom-scrollbar" v-loading="loading">
      <!-- 通用设置 -->
      <div class="settings-section">
        <div class="section-title">通用设置</div>
        <div class="settings-card">
          <div class="setting-row">
            <div class="setting-info">
              <span class="setting-label">语言</span>
              <span class="setting-desc">选择界面显示语言</span>
            </div>
            <el-select v-model="generalSettings.language" @change="saveGeneralSettings">
              <el-option label="简体中文" value="zh-CN" />
              <el-option label="English" value="en-US" />
            </el-select>
          </div>
          <div class="setting-row">
            <div class="setting-info">
              <span class="setting-label">字体大小</span>
              <span class="setting-desc">调整界面字体大小</span>
            </div>
            <el-select v-model="generalSettings.fontSize" @change="saveGeneralSettings">
              <el-option label="小" value="small" />
              <el-option label="中" value="medium" />
              <el-option label="大" value="large" />
            </el-select>
          </div>
          <div class="setting-row">
            <div class="setting-info">
              <span class="setting-label">回车发送</span>
              <span class="setting-desc">按 Enter 发送消息，Ctrl+Enter 换行</span>
            </div>
            <el-switch v-model="generalSettings.enterToSend" @change="saveGeneralSettings" />
          </div>
        </div>
      </div>

      <!-- 通知设置 -->
      <div class="settings-section">
        <div class="section-title">通知设置</div>
        <div class="settings-card">
          <div class="setting-row">
            <div class="setting-info">
              <span class="setting-label">消息通知</span>
              <span class="setting-desc">收到新消息时提醒</span>
            </div>
            <el-switch v-model="notificationSettings.messageNotification" @change="saveNotificationSettings" />
          </div>
          <div class="setting-row">
            <div class="setting-info">
              <span class="setting-label">群组通知</span>
              <span class="setting-desc">群聊消息通知</span>
            </div>
            <el-switch v-model="notificationSettings.groupNotification" @change="saveNotificationSettings" />
          </div>
          <div class="setting-row">
            <div class="setting-info">
              <span class="setting-label">系统通知</span>
              <span class="setting-desc">系统消息通知</span>
            </div>
            <el-switch v-model="notificationSettings.systemNotification" @change="saveNotificationSettings" />
          </div>
          <div class="setting-row">
            <div class="setting-info">
              <span class="setting-label">声音提示</span>
              <span class="setting-desc">收到消息时播放提示音</span>
            </div>
            <el-switch v-model="notificationSettings.soundEnabled" @change="saveNotificationSettings" />
          </div>
          <div class="setting-row">
            <div class="setting-info">
              <span class="setting-label">桌面通知</span>
              <span class="setting-desc">在桌面显示通知横幅</span>
            </div>
            <el-switch v-model="notificationSettings.desktopNotification" @change="saveNotificationSettings" />
          </div>
          <div class="setting-row">
            <div class="setting-info">
              <span class="setting-label">全部静音</span>
              <span class="setting-desc">关闭所有消息提醒</span>
            </div>
            <el-switch v-model="notificationSettings.muteAll" @change="saveNotificationSettings" />
          </div>
        </div>
      </div>

      <!-- 隐私设置 -->
      <div class="settings-section">
        <div class="section-title">隐私设置</div>
        <div class="settings-card">
          <div class="setting-row">
            <div class="setting-info">
              <span class="setting-label">显示在线状态</span>
              <span class="setting-desc">其他人可以看到你是否在线</span>
            </div>
            <el-switch v-model="privacySettings.showOnlineStatus" @change="savePrivacySettings" />
          </div>
          <div class="setting-row">
            <div class="setting-info">
              <span class="setting-label">允许陌生人消息</span>
              <span class="setting-desc">允许接收陌生人的会话请求</span>
            </div>
            <el-switch v-model="privacySettings.allowStrangerMessage" @change="savePrivacySettings" />
          </div>
          <div class="setting-row">
            <div class="setting-info">
              <span class="setting-label">允许语音通话</span>
              <span class="setting-desc">允许其他人向你发起语音通话</span>
            </div>
            <el-switch v-model="privacySettings.allowVoiceCall" @change="savePrivacySettings" />
          </div>
          <div class="setting-row">
            <div class="setting-info">
              <span class="setting-label">允许视频通话</span>
              <span class="setting-desc">允许其他人向你发起视频通话</span>
            </div>
            <el-switch v-model="privacySettings.allowVideoCall" @change="savePrivacySettings" />
          </div>
          <div class="setting-row">
            <div class="setting-info">
              <span class="setting-label">显示最后上线时间</span>
              <span class="setting-desc">显示你的最后上线时间</span>
            </div>
            <el-switch v-model="privacySettings.showLastSeen" @change="savePrivacySettings" />
          </div>
        </div>
      </div>

      <!-- 快捷键设置 -->
      <div class="settings-section">
        <div class="section-title">快捷键设置</div>
        <div class="settings-card">
          <div class="setting-row">
            <div class="setting-info">
              <span class="setting-label">发送消息</span>
              <span class="setting-desc">Enter 发送，Ctrl+Enter 换行</span>
            </div>
            <el-switch v-model="shortcutSettings.enterToSend" @change="saveShortcutSettings" />
          </div>
          <div class="setting-row">
            <div class="setting-info">
              <span class="setting-label">全局搜索</span>
              <span class="setting-desc">快速打开搜索面板</span>
            </div>
            <el-select v-model="shortcutSettings.globalSearch" style="width: 120px" @change="saveShortcutSettings">
              <el-option label="Ctrl+K" value="Ctrl+K" />
              <el-option label="Ctrl+F" value="Ctrl+F" />
              <el-option label="Ctrl+S" value="Ctrl+S" />
              <el-option label="无" value="" />
            </el-select>
          </div>
          <div class="setting-row">
            <div class="setting-info">
              <span class="setting-label">新建会话</span>
              <span class="setting-desc">快速发起新会话</span>
            </div>
            <el-select v-model="shortcutSettings.newChat" style="width: 120px" @change="saveShortcutSettings">
              <el-option label="Ctrl+N" value="Ctrl+N" />
              <el-option label="Ctrl+T" value="Ctrl+T" />
              <el-option label="无" value="" />
            </el-select>
          </div>
          <div class="setting-row">
            <div class="setting-info">
              <span class="setting-label">截屏快捷键</span>
              <span class="setting-desc">快速截屏并发送</span>
            </div>
            <el-select v-model="shortcutSettings.screenshot" style="width: 120px" @change="saveShortcutSettings">
              <el-option label="Ctrl+Alt+S" value="Ctrl+Alt+S" />
              <el-option label="Ctrl+Shift+S" value="Ctrl+Shift+S" />
              <el-option label="无" value="" />
            </el-select>
          </div>
          <div class="setting-row">
            <div class="setting-info">
              <span class="setting-label">一键回到顶部</span>
              <span class="setting-desc">消息列表快速回到顶部</span>
            </div>
            <el-select v-model="shortcutSettings.scrollToTop" style="width: 120px" @change="saveShortcutSettings">
              <el-option label="Ctrl+Home" value="Ctrl+Home" />
              <el-option label="Home" value="Home" />
              <el-option label="无" value="" />
            </el-select>
          </div>
        </div>
      </div>

      <!-- 黑名单管理 -->
      <div class="settings-section">
        <div class="section-title">黑名单</div>
        <div class="settings-card">
          <div v-if="blockedUsers.length === 0" class="empty-blocked">
            <span>暂无黑名单用户</span>
          </div>
          <div v-for="user in blockedUsers" :key="user.userId" class="blocked-user-row">
            <div class="user-info">
              <DingtalkAvatar :src="user.avatar" :name="user.nickname || '用户'" :size="32" shape="square" />
              <span class="username">{{ user.nickname || '用户' + user.userId }}</span>
            </div>
            <el-button type="danger" plain size="small" @click="handleUnblock(user.userId)">
              解除拉黑
            </el-button>
          </div>
        </div>
      </div>

      <!-- 通话记录 -->
      <div class="settings-section">
        <div class="section-title">通话记录</div>
        <div class="settings-card">
          <div v-if="callHistoryLoading" class="call-history-loading">
            <span>加载中...</span>
          </div>
          <div v-else-if="callHistory.length === 0" class="call-history-empty">
            <el-icon><Phone /></el-icon>
            <span>暂无通话记录</span>
          </div>
          <div v-else class="call-history-list">
            <div
              v-for="call in callHistory"
              :key="call.callId"
              class="call-history-item"
            >
              <div class="call-avatar">
                <DingtalkAvatar
                  :src="call.callerAvatar"
                  :name="call.callerName"
                  :size="40"
                  shape="circle"
                />
                <div class="call-type-icon">
                  <el-icon v-if="call.callType === 'VOICE'" class="call-icon voice">
                    <PhoneFilled />
                  </el-icon>
                  <el-icon v-else class="call-icon video">
                    <VideoCamera />
                  </el-icon>
                </div>
              </div>
              <div class="call-info">
                <div class="call-peer">
                  与 <span class="peer-name">{{ call.callerName }}</span> 的通话
                </div>
                <div class="call-meta">
                  <el-tag v-if="call.status === 'COMPLETED'" type="success" size="small">
                    已接通
                  </el-tag>
                  <el-tag v-else-if="call.status === 'MISSED'" type="danger" size="small">
                    未接
                  </el-tag>
                  <el-tag v-else-if="call.status === 'REJECTED'" type="warning" size="small">
                    已拒绝
                  </el-tag>
                  <el-tag v-else type="info" size="small">
                    {{ call.status }}
                  </el-tag>
                  <span class="call-time">{{ formatTime(call.startTime) }}</span>
                  <span v-if="call.duration" class="call-duration">
                    {{ formatDuration(call.duration) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="js">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getGeneralSettings,
  updateGeneralSettings,
  getNotificationSettings,
  updateNotificationSettings,
  getPrivacySettings,
  updatePrivacySettings,
  getBlockedUsers,
  unblockUser,
  getShortcutSettings,
  updateShortcutSettings
} from '@/api/im/config'
import { getCallHistory } from '@/api/im/videoCall'
import { getUsersBatch } from '@/api/im/user'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { Phone, VideoCamera, PhoneFilled } from '@element-plus/icons-vue'

const loading = ref(false)

// 设置数据
const generalSettings = ref({
  language: 'zh-CN',
  theme: 'light',
  fontSize: 'medium',
  enterToSend: true
})

const notificationSettings = ref({
  messageNotification: true,
  groupNotification: true,
  systemNotification: true,
  soundEnabled: true,
  desktopNotification: true,
  muteAll: false
})

const privacySettings = ref({
  showOnlineStatus: true,
  allowStrangerMessage: false,
  allowVoiceCall: true,
  allowVideoCall: true,
  showLastSeen: false
})

const blockedUsers = ref([])
const callHistory = ref([])
const callHistoryLoading = ref(false)

const shortcutSettings = ref({
  enterToSend: true,
  globalSearch: 'Ctrl+K',
  newChat: 'Ctrl+N',
  screenshot: '',
  scrollToTop: 'Home'
})

// 加载所有设置
const loadSettings = async () => {
  loading.value = true
  try {
    const [generalRes, notificationRes, privacyRes, blockedRes, shortcutRes] = await Promise.all([
      getGeneralSettings(),
      getNotificationSettings(),
      getPrivacySettings(),
      getBlockedUsers(),
      getShortcutSettings()
    ])

    if (generalRes.code === 200) {
      generalSettings.value = { ...generalSettings.value, ...generalRes.data }
    }
    if (notificationRes.code === 200) {
      notificationSettings.value = { ...notificationSettings.value, ...notificationRes.data }
    }
    if (privacyRes.code === 200) {
      privacySettings.value = { ...privacySettings.value, ...privacyRes.data }
    }
    if (blockedRes.code === 200) {
      blockedUsers.value = blockedRes.data || []
    }
    if (shortcutRes.code === 200) {
      shortcutSettings.value = { ...shortcutSettings.value, ...shortcutRes.data }
    }
  } catch (e) {
    console.error('加载设置失败', e)
    ElMessage.error('加载设置失败')
  } finally {
    loading.value = false
  }
}

// 加载通话记录
const loadCallHistory = async () => {
  callHistoryLoading.value = true
  try {
    const res = await getCallHistory(20)
    if (res.code === 200) {
      const history = res.data || []
      // 获取涉及的用户信息
      const userIds = [...new Set(history.flatMap(c => [c.callerId, c.calleeId]))]
      if (userIds.length > 0) {
        const userRes = await getUsersBatch(userIds)
        const userMap = new Map()
        if (userRes.code === 200) {
          (userRes.data || []).forEach(u => userMap.set(u.id, u))
        }
        callHistory.value = history.map(c => ({
          ...c,
          callerName: userMap.get(c.callerId)?.nickname || '用户' + c.callerId,
          callerAvatar: userMap.get(c.callerId)?.avatar || '',
          calleeName: userMap.get(c.calleeId)?.nickname || '用户' + c.calleeId,
          calleeAvatar: userMap.get(c.calleeId)?.avatar || ''
        }))
      } else {
        callHistory.value = history
      }
    }
  } catch (e) {
    console.error('加载通话记录失败', e)
  } finally {
    callHistoryLoading.value = false
  }
}

// 保存通用设置
const saveGeneralSettings = async () => {
  try {
    const res = await updateGeneralSettings(generalSettings.value)
    if (res.code === 200) {
      ElMessage.success('设置已保存')
    }
  } catch (e) {
    console.error('保存通用设置失败', e)
    ElMessage.error('保存通用设置失败')
  }
}

// 保存通知设置
const saveNotificationSettings = async () => {
  try {
    const res = await updateNotificationSettings(notificationSettings.value)
    if (res.code === 200) {
      ElMessage.success('设置已保存')
    }
  } catch (e) {
    console.error('保存通知设置失败', e)
    ElMessage.error('保存通知设置失败')
  }
}

// 保存隐私设置
const savePrivacySettings = async () => {
  try {
    const res = await updatePrivacySettings(privacySettings.value)
    if (res.code === 200) {
      ElMessage.success('设置已保存')
    }
  } catch (e) {
    console.error('保存隐私设置失败', e)
    ElMessage.error('保存隐私设置失败')
  }
}

// 保存快捷键设置
const saveShortcutSettings = async () => {
  try {
    const res = await updateShortcutSettings(shortcutSettings.value)
    if (res.code === 200) {
      ElMessage.success('设置已保存')
    }
  } catch (e) {
    console.error('保存快捷键设置失败', e)
    ElMessage.error('保存快捷键设置失败')
  }
}

// 解除拉黑
const handleUnblock = async (userId) => {
  try {
    const res = await unblockUser(userId)
    if (res.code === 200) {
      blockedUsers.value = blockedUsers.value.filter(u => u.userId !== userId)
      ElMessage.success('已解除拉黑')
    }
  } catch (e) {
    console.error('解除拉黑失败', e)
    ElMessage.error('解除拉黑失败')
  }
}

onMounted(() => {
  loadSettings()
  loadCallHistory()
  // 注册全局键盘快捷键
  document.addEventListener('keydown', handleGlobalKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleGlobalKeydown)
})

// 全局键盘快捷键处理
const handleGlobalKeydown = (e) => {
  const ctrl = e.ctrlKey || e.metaKey
  const key = e.key

  // 全局搜索 Ctrl+K / Ctrl+F / Ctrl+S
  if (ctrl && shortcutSettings.value.globalSearch) {
    const searchMap = { 'K': 'Ctrl+K', 'F': 'Ctrl+F', 'S': 'Ctrl+S' }
    if (searchMap[key] === shortcutSettings.value.globalSearch) {
      e.preventDefault()
      // 触发全局搜索事件
      window.dispatchEvent(new CustomEvent('shortcut:global-search'))
    }
  }

  // 新建会话 Ctrl+N / Ctrl+T
  if (ctrl && shortcutSettings.value.newChat) {
    const chatMap = { 'N': 'Ctrl+N', 'T': 'Ctrl+T' }
    if (chatMap[key] === shortcutSettings.value.newChat) {
      e.preventDefault()
      window.dispatchEvent(new CustomEvent('shortcut:new-chat'))
    }
  }

  // 截屏快捷键
  if (ctrl && e.altKey && shortcutSettings.value.screenshot === 'Ctrl+Alt+S') {
    e.preventDefault()
    window.dispatchEvent(new CustomEvent('shortcut:screenshot'))
  }
  if (ctrl && e.shiftKey && shortcutSettings.value.screenshot === 'Ctrl+Shift+S') {
    e.preventDefault()
    window.dispatchEvent(new CustomEvent('shortcut:screenshot'))
  }

  // 一键回到顶部
  if (shortcutSettings.value.scrollToTop === 'Ctrl+Home' && ctrl && key === 'Home') {
    e.preventDefault()
    window.dispatchEvent(new CustomEvent('shortcut:scroll-to-top'))
  }
  if (shortcutSettings.value.scrollToTop === 'Home' && key === 'Home') {
    e.preventDefault()
    window.dispatchEvent(new CustomEvent('shortcut:scroll-to-top'))
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today.getTime() - 86400000)
  const callDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())

  if (callDate.getTime() === today.getTime()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (callDate.getTime() === yesterday.getTime()) {
    return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else {
    return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' }) + ' ' +
           date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
}

// 格式化通话时长
const formatDuration = (seconds) => {
  if (!seconds) return ''
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}:${s.toString().padStart(2, '0')}`
}
</script>

<style lang="scss" scoped>
.settings-panel {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: var(--dt-bg-body);
}

.settings-header {
  flex-shrink: 0;
  padding: 16px 24px;
  background-color: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);

  h2 {
    margin: 0;
    font-size: 16px;
    font-weight: 500;
    color: var(--dt-text-primary);
  }
}

.settings-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px 24px;
}

.settings-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--dt-text-secondary);
  margin-bottom: 12px;
  padding-left: 4px;
}

.settings-card {
  background-color: var(--dt-bg-card);
  border-radius: var(--dt-radius-md);
  padding: 8px 0;
}

.setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  transition: background-color var(--dt-transition-fast);

  &:hover {
    background-color: var(--dt-bg-hover);
  }
}

.setting-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.setting-label {
  font-size: 14px;
  color: var(--dt-text-primary);
}

.setting-desc {
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.empty-blocked {
  padding: 24px 16px;
  text-align: center;
  color: var(--dt-text-tertiary);
  font-size: 14px;
}

.blocked-user-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  transition: background-color var(--dt-transition-fast);

  &:hover {
    background-color: var(--dt-bg-hover);
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 12px;

    .username {
      font-size: 14px;
      color: var(--dt-text-primary);
    }
  }
}

.call-history-loading,
.call-history-empty {
  padding: 24px 16px;
  text-align: center;
  color: var(--dt-text-tertiary);
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.call-history-list {
  max-height: 400px;
  overflow-y: auto;
}

.call-history-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  transition: background-color var(--dt-transition-fast);
  cursor: pointer;

  &:hover {
    background-color: var(--dt-bg-hover);
  }
}

.call-avatar {
  position: relative;

  .call-type-icon {
    position: absolute;
    bottom: -2px;
    right: -2px;
    width: 18px;
    height: 18px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2px solid var(--dt-bg-card);

    .call-icon {
      font-size: 10px;
      color: var(--dt-text-white);

      &.voice {
        background: var(--dt-success-color);
      }

      &.video {
        background: var(--dt-brand-color);
      }
    }
  }
}

.call-info {
  flex: 1;
  min-width: 0;

  .call-peer {
    font-size: 14px;
    color: var(--dt-text-primary);
    margin-bottom: 4px;

    .peer-name {
      font-weight: 500;
    }
  }

  .call-meta {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 12px;
    color: var(--dt-text-tertiary);

    .call-time {
      color: var(--dt-text-tertiary);
    }

    .call-duration {
      color: var(--dt-text-secondary);
    }
  }
}
</style>
