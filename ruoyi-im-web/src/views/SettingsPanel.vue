<template>
  <div class="settings-panel">
    <!-- 设置头部 -->
    <div class="settings-header">
      <h2>设置</h2>
    </div>

    <!-- 设置内容 -->
    <div class="settings-content">
      <!-- 左侧菜单 -->
      <div class="settings-menu">
        <div 
          v-for="menu in menuList" 
          :key="menu.key"
          class="menu-item"
          :class="{ active: activeMenu === menu.key }"
          @click="activeMenu = menu.key"
        >
          <span class="material-icons-outlined">{{ menu.icon }}</span>
          <span>{{ menu.label }}</span>
        </div>
      </div>

      <!-- 右侧内容 -->
      <div class="settings-detail">
        <!-- 通知设置 -->
        <div v-if="activeMenu === 'notification'" class="setting-section">
          <h3>通知设置</h3>
          <div class="setting-list">
            <div class="setting-item">
              <div class="item-info">
                <div class="item-title">消息通知</div>
                <div class="item-desc">接收新消息提醒</div>
              </div>
              <el-switch v-model="notificationSettings.messageNotify" @change="saveNotificationSettings" />
            </div>
            <div class="setting-item">
              <div class="item-info">
                <div class="item-title">群聊通知</div>
                <div class="item-desc">接收群消息提醒</div>
              </div>
              <el-switch v-model="notificationSettings.groupNotify" @change="saveNotificationSettings" />
            </div>
            <div class="setting-item">
              <div class="item-info">
                <div class="item-title">系统通知</div>
                <div class="item-desc">接收系统消息提醒</div>
              </div>
              <el-switch v-model="notificationSettings.systemNotify" @change="saveNotificationSettings" />
            </div>
            <div class="setting-item">
              <div class="item-info">
                <div class="item-title">声音提示</div>
                <div class="item-desc">收到消息时播放提示音</div>
              </div>
              <el-switch v-model="notificationSettings.soundNotify" @change="saveNotificationSettings" />
            </div>
            <div class="setting-item">
              <div class="item-info">
                <div class="item-title">震动提示</div>
                <div class="item-desc">收到消息时震动提醒</div>
              </div>
              <el-switch v-model="notificationSettings.vibrateNotify" @change="saveNotificationSettings" />
            </div>
          </div>
        </div>

        <!-- 隐私设置 -->
        <div v-if="activeMenu === 'privacy'" class="setting-section">
          <h3>隐私设置</h3>
          <div class="setting-list">
            <div class="setting-item">
              <div class="item-info">
                <div class="item-title">在线状态</div>
                <div class="item-desc">允许他人查看我的在线状态</div>
              </div>
              <el-switch v-model="privacySettings.showOnlineStatus" @change="savePrivacySettings" />
            </div>
            <div class="setting-item">
              <div class="item-info">
                <div class="item-title">最后上线时间</div>
                <div class="item-desc">允许他人查看我的最后上线时间</div>
              </div>
              <el-switch v-model="privacySettings.showLastOnline" @change="savePrivacySettings" />
            </div>
            <div class="setting-item">
              <div class="item-info">
                <div class="item-title">陌生人消息</div>
                <div class="item-desc">允许接收陌生人的消息</div>
              </div>
              <el-switch v-model="privacySettings.allowStrangerMsg" @change="savePrivacySettings" />
            </div>
            <div class="setting-item">
              <div class="item-info">
                <div class="item-title">语音通话</div>
                <div class="item-desc">允许他人发起语音通话</div>
              </div>
              <el-switch v-model="privacySettings.allowVoiceCall" @change="savePrivacySettings" />
            </div>
            <div class="setting-item">
              <div class="item-info">
                <div class="item-title">视频通话</div>
                <div class="item-desc">允许他人发起视频通话</div>
              </div>
              <el-switch v-model="privacySettings.allowVideoCall" @change="savePrivacySettings" />
            </div>
          </div>
        </div>

        <!-- 通用设置 -->
        <div v-if="activeMenu === 'general'" class="setting-section">
          <h3>通用设置</h3>
          <div class="setting-list">
            <div class="setting-item">
              <div class="item-info">
                <div class="item-title">语言</div>
                <div class="item-desc">选择界面显示语言</div>
              </div>
              <el-select v-model="generalSettings.language" size="small" style="width: 120px" @change="saveGeneralSettings">
                <el-option label="简体中文" value="zh_CN" />
                <el-option label="English" value="en_US" />
              </el-select>
            </div>
            <div class="setting-item">
              <div class="item-info">
                <div class="item-title">主题</div>
                <div class="item-desc">选择界面主题风格</div>
              </div>
              <el-select v-model="generalSettings.theme" size="small" style="width: 120px" @change="saveGeneralSettings">
                <el-option label="浅色模式" value="light" />
                <el-option label="深色模式" value="dark" />
              </el-select>
            </div>
            <div class="setting-item">
              <div class="item-info">
                <div class="item-title">字体大小</div>
                <div class="item-desc">选择界面字体大小</div>
              </div>
              <el-select v-model="generalSettings.fontSize" size="small" style="width: 120px" @change="saveGeneralSettings">
                <el-option label="小" value="small" />
                <el-option label="中" value="medium" />
                <el-option label="大" value="large" />
              </el-select>
            </div>
          </div>
        </div>

        <!-- 黑名单 -->
        <div v-if="activeMenu === 'blocked'" class="setting-section">
          <h3>黑名单</h3>
          <div v-if="blockedUsers.length" class="blocked-list">
            <div v-for="user in blockedUsers" :key="user.userId" class="blocked-item">
              <DingtalkAvatar :src="user.avatar" :name="user.nickname" :size="40" />
              <div class="user-info">
                <div class="user-name">{{ user.nickname }}</div>
                <div class="user-dept">{{ user.department || '暂无部门' }}</div>
              </div>
              <div class="action-btns">
                <el-button size="small" type="primary" link @click="handleUnblock(user.userId)">
                  解除拉黑
                </el-button>
              </div>
            </div>
          </div>
          <div v-else class="empty-blocked">
            <span class="material-icons-outlined">person_remove</span>
            <p>暂无黑名单用户</p>
          </div>
        </div>

        <!-- 关于 -->
        <div v-if="activeMenu === 'about'" class="setting-section">
          <h3>关于</h3>
          <div class="about-info">
            <div class="app-logo">IM</div>
            <div class="app-name">心流IM</div>
            <div class="app-version">版本 1.0.0</div>
            <div class="app-desc">一款现代企业级即时通讯应用</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { 
  getNotificationSettings, updateNotificationSettings,
  getPrivacySettings, updatePrivacySettings,
  getGeneralSettings, updateGeneralSettings,
  getBlockedUsers, unblockUser
} from '@/api/im/config'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { ElMessage } from 'element-plus'

const activeMenu = ref('notification')

const menuList = [
  { key: 'notification', label: '通知设置', icon: 'notifications' },
  { key: 'privacy', label: '隐私设置', icon: 'shield' },
  { key: 'general', label: '通用设置', icon: 'settings' },
  { key: 'blocked', label: '黑名单', icon: 'block' },
  { key: 'about', label: '关于', icon: 'info' }
]

const notificationSettings = ref({
  messageNotify: true,
  groupNotify: true,
  systemNotify: true,
  soundNotify: true,
  vibrateNotify: false
})

const privacySettings = ref({
  showOnlineStatus: true,
  showLastOnline: true,
  allowStrangerMsg: false,
  allowVoiceCall: true,
  allowVideoCall: true
})

const generalSettings = ref({
  language: 'zh_CN',
  theme: 'light',
  fontSize: 'medium'
})

const blockedUsers = ref([])

// 加载通知设置
const loadNotificationSettings = async () => {
  try {
    const res = await getNotificationSettings()
    if (res.code === 200 && res.data) {
      notificationSettings.value = { ...notificationSettings.value, ...res.data }
    }
  } catch (e) {
    console.error('获取通知设置失败', e)
  }
}

// 保存通知设置
const saveNotificationSettings = async () => {
  try {
    const res = await updateNotificationSettings(notificationSettings.value)
    if (res.code === 200) {
      ElMessage.success('保存成功')
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

// 加载隐私设置
const loadPrivacySettings = async () => {
  try {
    const res = await getPrivacySettings()
    if (res.code === 200 && res.data) {
      privacySettings.value = { ...privacySettings.value, ...res.data }
    }
  } catch (e) {
    console.error('获取隐私设置失败', e)
  }
}

// 保存隐私设置
const savePrivacySettings = async () => {
  try {
    const res = await updatePrivacySettings(privacySettings.value)
    if (res.code === 200) {
      ElMessage.success('保存成功')
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

// 加载通用设置
const loadGeneralSettings = async () => {
  try {
    const res = await getGeneralSettings()
    if (res.code === 200 && res.data) {
      generalSettings.value = { ...generalSettings.value, ...res.data }
    }
  } catch (e) {
    console.error('获取通用设置失败', e)
  }
}

// 保存通用设置
const saveGeneralSettings = async () => {
  try {
    const res = await updateGeneralSettings(generalSettings.value)
    if (res.code === 200) {
      ElMessage.success('保存成功')
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

// 加载黑名单
const loadBlockedUsers = async () => {
  try {
    const res = await getBlockedUsers()
    if (res.code === 200) {
      blockedUsers.value = res.data || []
    }
  } catch (e) {
    console.error('获取黑名单失败', e)
  }
}

// 解除拉黑
const handleUnblock = async (userId) => {
  try {
    const res = await unblockUser(userId)
    if (res.code === 200) {
      ElMessage.success('已解除拉黑')
      loadBlockedUsers()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadNotificationSettings()
  loadPrivacySettings()
  loadGeneralSettings()
  loadBlockedUsers()
})
</script>

<style scoped lang="scss">
.settings-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.settings-header {
  padding: 16px 24px;
  border-bottom: 1px solid #e8e8e8;
  
  h2 {
    margin: 0;
    font-size: 18px;
    font-weight: 500;
  }
}

.settings-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.settings-menu {
  width: 180px;
  border-right: 1px solid #e8e8e8;
  padding: 12px;
  
  .menu-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 12px;
    border-radius: 8px;
    cursor: pointer;
    color: #666;
    transition: all 0.2s;
    
    .material-icons-outlined {
      font-size: 20px;
    }
    
    &:hover {
      background: #f5f5f5;
    }
    
    &.active {
      background: #e6f7ff;
      color: #1677ff;
    }
  }
}

.settings-detail {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.setting-section {
  h3 {
    margin: 0 0 20px;
    font-size: 16px;
    font-weight: 500;
  }
}

.setting-list {
  .setting-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 14px 0;
    border-bottom: 1px solid #f0f0f0;
    
    .item-info {
      .item-title {
        font-size: 14px;
        color: #333;
      }
      
      .item-desc {
        font-size: 12px;
        color: #999;
        margin-top: 4px;
      }
    }
  }
}

.blocked-list {
  .blocked-item {
    display: flex;
    align-items: center;
    padding: 12px;
    border-radius: 8px;
    transition: background 0.2s;
    
    &:hover {
      background: #f5f5f5;
    }
    
    .user-info {
      flex: 1;
      margin-left: 12px;
      
      .user-name {
        font-size: 14px;
        font-weight: 500;
        color: #333;
      }
      
      .user-dept {
        font-size: 12px;
        color: #999;
        margin-top: 4px;
      }
    }
  }
}

.empty-blocked {
  text-align: center;
  padding: 60px 0;
  color: #999;
  
  .material-icons-outlined {
    font-size: 48px;
    opacity: 0.5;
  }
  
  p {
    margin-top: 12px;
    font-size: 14px;
  }
}

.about-info {
  text-align: center;
  padding: 40px 0;
  
  .app-logo {
    width: 80px;
    height: 80px;
    margin: 0 auto 16px;
    background: linear-gradient(135deg, #1677ff 0%, #0d5bbd 100%);
    border-radius: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 28px;
    font-weight: 700;
  }
  
  .app-name {
    font-size: 20px;
    font-weight: 500;
    color: #333;
  }
  
  .app-version {
    font-size: 14px;
    color: #999;
    margin-top: 8px;
  }
  
  .app-desc {
    font-size: 14px;
    color: #666;
    margin-top: 16px;
  }
}
</style>
