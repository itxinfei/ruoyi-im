<template>
  <div class="user-profile-panel">
    <div class="profile-content">
      <div class="cover-image"></div>

      <div class="profile-card">
        <div class="profile-header">
          <div class="avatar-section">
            <div class="avatar" v-if="currentUser.avatar">
              <img :src="currentUser.avatar" alt="Avatar" />
            </div>
            <div class="avatar placeholder" v-else>
              <span class="avatar-text">{{ (currentUser.nickname || currentUser.username || '?').charAt(0) }}</span>
            </div>
            <div v-if="isOnline" class="online-indicator"></div>
          </div>

          <div class="profile-info">
            <div class="info-top">
              <div class="name-section">
                <h1 class="user-name">{{ currentUser.nickname || currentUser.username }}</h1>
                <el-badge class="status-badge" type="success" v-if="isOnline">在线</el-badge>
                <el-badge class="status-badge" type="info" v-else>离线</el-badge>
              </div>
              <p class="user-position">
                {{ currentUser.position || '暂无职位' }} · {{ currentUser.department || '暂无部门' }}
              </p>
            </div>

            <div class="action-buttons">
              <el-button class="edit-btn" @click="showEditDialog = true">
                <el-icon><Edit /></el-icon>
                编辑资料
              </el-button>
            </div>
          </div>
        </div>

        <el-divider class="profile-divider" />

        <div class="section-container">
          <!-- 联系方式 -->
          <div class="info-group">
            <h2 class="section-title">联系方式</h2>
            <div class="info-grid">
              <div class="info-item">
                <div class="icon-box icon-blue"><el-icon><Phone /></el-icon></div>
                <div>
                  <div class="label">手机号码</div>
                  <div class="value">{{ currentUser.mobile || '未填写' }}</div>
                </div>
              </div>
              <div class="info-item">
                <div class="icon-box icon-purple"><el-icon><Message /></el-icon></div>
                <div>
                  <div class="label">邮箱地址</div>
                  <div class="value">{{ currentUser.email || '未填写' }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 工作信息 -->
          <div class="info-group">
            <h2 class="section-title">工作信息</h2>
            <div class="info-grid">
              <div class="info-item">
                <div class="icon-box icon-green"><el-icon><OfficeBuilding /></el-icon></div>
                <div>
                  <div class="label">所属部门</div>
                  <div class="value">{{ currentUser.department || '未填写' }}</div>
                </div>
              </div>
              <div class="info-item">
                <div class="icon-box icon-orange"><el-icon><Briefcase /></el-icon></div>
                <div>
                  <div class="label">职位</div>
                  <div class="value">{{ currentUser.position || '未填写' }}</div>
                </div>
              </div>
              <div class="info-item">
                 <div class="icon-box icon-cyan"><el-icon><Calendar /></el-icon></div>
                 <div>
                   <div class="label">注册时间</div>
                   <div class="value">{{ formatDate(currentUser.createTime) }}</div>
                 </div>
              </div>
            </div>
          </div>

          <!-- 个性签名 -->
          <div class="info-group" v-if="currentUser.signature">
            <h2 class="section-title">个性签名</h2>
             <p class="signature-text">{{ currentUser.signature }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Edit Profile Dialog -->
    <EditProfileDialog 
      v-model:visible="showEditDialog"
      :user-info="currentUser"
      @save="handleSaveProfile"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import {
  Edit,
  Phone,
  Message,
  OfficeBuilding,
  Briefcase,
  Calendar
} from '@element-plus/icons-vue'
import EditProfileDialog from '@/components/EditProfileDialog/index.vue'

const store = useStore()
const showEditDialog = ref(false)

const currentUser = computed(() => store.getters['user/currentUser'] || {})
const isOnline = computed(() => store.state.user.online)

onMounted(async () => {
  await store.dispatch('user/getUserInfo')
})

const formatDate = (dateStr) => {
  if (!dateStr) return '未知'
  try {
    return new Date(dateStr).toLocaleDateString()
  } catch (e) {
    return dateStr
  }
}

const handleSaveProfile = async (formData) => {
  try {
    await store.dispatch('user/updateProfile', formData)
    ElMessage.success('更新成功')
    showEditDialog.value = false
  } catch (error) {
    console.error(error)
    ElMessage.error(error.message || '更新失败')
  }
}
</script>

<style scoped>
.user-profile-panel {
  flex: 1;
  background-color: #f7f8fa;
  overflow-y: auto;
  padding-bottom: 40px;
}

.profile-content {
  max-width: 800px;
  margin: 0 auto;
}

.cover-image {
  height: 160px;
  background: linear-gradient(135deg, #60a5fa, #3b82f6, #2563eb);
}

.profile-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  margin: -40px 20px 0;
  position: relative;
  padding: 24px;
}

.profile-header {
  display: flex;
  gap: 24px;
}

.avatar-section {
  position: relative;
  margin-top: -48px; 
}

.avatar {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  border: 4px solid #fff;
  overflow: hidden;
  background-color: #eee;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar.placeholder {
  background: linear-gradient(135deg, #60a5fa, #2563eb);
  color: #fff;
}

.avatar-text {
  font-size: 32px;
  font-weight: 600;
}

.online-indicator {
  position: absolute;
  bottom: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background-color: #52c41a;
  border: 3px solid #fff;
}

.profile-info {
  flex: 1;
  padding-top: 8px;
  display: flex;
  justify-content: space-between;
}

.info-top .user-name {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  display: inline-block;
  margin-right: 8px;
}

.user-position {
  color: #666;
  margin-top: 4px;
}

.section-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #333;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.icon-box {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-blue { background: #e6f7ff; color: #1890ff; }
.icon-purple { background: #f9f0ff; color: #722ed1; }
.icon-green { background: #f6ffed; color: #52c41a; }
.icon-orange { background: #fff7e6; color: #fa8c16; }
.icon-cyan { background: #e6fffb; color: #13c2c2; }

.label {
  font-size: 12px;
  color: #888;
}

.value {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.signature-text {
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
  color: #555;
  font-style: italic;
}
</style>
