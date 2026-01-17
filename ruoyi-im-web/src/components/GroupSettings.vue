<template>
  <el-drawer
    v-model="visible"
    title="群聊设置"
    size="400px"
    direction="rtl"
    @close="handleClose"
  >
    <div class="group-settings">
      <!-- 群聊基本信息 -->
      <div class="settings-section">
        <div class="section-title">群聊信息</div>
        <div class="group-info">
          <div class="avatar-upload">
            <DtAvatar
              :name="groupInfo.name"
              :avatar="groupInfo.avatar"
              :size="60"
              class="group-avatar"
              clickable
              @click="showAvatarUpload"
            />
            <el-button
              type="primary"
              size="small"
              class="upload-btn"
              @click="showAvatarUpload"
            >
              更换头像
            </el-button>
          </div>
          
          <div class="group-fields">
            <el-form-item label="群聊名称">
              <el-input
                v-model="groupInfo.name"
                placeholder="请输入群聊名称"
                maxlength="50"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item label="群聊描述">
              <el-input
                v-model="groupInfo.description"
                type="textarea"
                placeholder="请输入群聊描述"
                maxlength="200"
                show-word-limit
                :rows="3"
              />
            </el-form-item>
          </div>
        </div>
      </div>

      <!-- 群聊管理 -->
      <div class="settings-section" v-if="isOwner">
        <div class="section-title">群聊管理</div>
        <div class="management-actions">
          <el-button
            type="primary"
            :icon="User"
            @click="showInviteDialog"
          >
            邀请成员
          </el-button>
          <el-button
            :icon="Notification"
            @click="showAnnounceDialog"
          >
            发布公告
          </el-button>
          <el-button
            :icon="Setting"
            @click="showGroupConfig"
          >
            群聊配置
          </el-button>
        </div>
      </div>

      <!-- 成员管理 -->
      <div class="settings-section">
        <div class="section-header">
          <div class="section-title">群成员 ({{ memberList.length }}人)</div>
          <el-input
            v-model="memberSearch"
            placeholder="搜索成员"
            :prefix-icon="Search"
            size="small"
            class="member-search"
            clearable
          />
        </div>
        
        <div class="member-list">
          <div
            v-for="member in filteredMembers"
            :key="member.id"
            class="member-item"
          >
            <DtAvatar
              :name="member.name || member.username"
              :avatar="member.avatar"
              :size="40"
              class="member-avatar"
            />
            <div class="member-info">
              <div class="member-name">{{ member.name || member.username }}</div>
              <div class="member-role">{{ getRoleLabel(member.role) }}</div>
            </div>
            
            <div class="member-actions" v-if="isOwner && member.id !== currentUserId">
              <el-dropdown trigger="click" @command="handleMemberAction">
                <el-button :icon="MoreFilled" text size="small" />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="setAdmin" v-if="member.role !== 'OWNER'">
                      设为管理员
                    </el-dropdown-item>
                    <el-dropdown-item command="removeAdmin" v-if="member.role === 'ADMIN'">
                      取消管理员
                    </el-dropdown-item>
                    <el-dropdown-item command="mute" v-if="member.role !== 'OWNER'">
                      禁言
                    </el-dropdown-item>
                    <el-dropdown-item command="remove" v-if="member.role !== 'OWNER'" divided>
                      移除出群
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </div>

      <!-- 危险操作 -->
      <div class="settings-section danger" v-if="isOwner">
        <div class="section-title">危险操作</div>
        <div class="danger-actions">
          <el-button
            type="danger"
            :icon="SwitchButton"
            @click="transferGroup"
          >
            转让群主
          </el-button>
          <el-button
            type="danger"
            :icon="Delete"
            @click="disbandGroup"
          >
            解散群聊
          </el-button>
        </div>
      </div>
      
      <!-- 普通成员操作 -->
      <div class="settings-section" v-else>
        <div class="section-title">操作</div>
        <div class="member-actions">
          <el-button
            type="danger"
            :icon="SwitchButton"
            @click="quitGroup"
          >
            退出群聊
          </el-button>
        </div>
      </div>
    </div>

    <!-- 保存按钮 -->
    <div class="settings-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="saveSettings" :loading="saving">
        保存
      </el-button>
    </div>

    <!-- 邀请成员对话框 -->
    <InviteMemberDialog
      v-model="inviteDialogVisible"
      :group-id="groupInfo.id"
      @success="handleInviteSuccess"
    />

    <!-- 发布公告对话框 -->
    <AnnounceDialog
      v-model="announceDialogVisible"
      :group-id="groupInfo.id"
      @success="handleAnnounceSuccess"
    />

    <!-- 群聊配置对话框 -->
    <GroupConfigDialog
      v-model="configDialogVisible"
      :group-info="groupInfo"
      @success="handleConfigSuccess"
    />

    <!-- 头像上传 -->
    <input
      ref="avatarInput"
      type="file"
      accept="image/*"
      style="display: none"
      @change="handleAvatarChange"
    />
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User, Notification, Setting, MoreFilled, Search,
  SwitchButton, Delete
} from '@element-plus/icons-vue'
import DtAvatar from '@/components/DtAvatar.vue'
import InviteMemberDialog from '@/components/InviteMemberDialog.vue'
import AnnounceDialog from '@/components/AnnounceDialog.vue'
import GroupConfigDialog from '@/components/GroupConfigDialog.vue'
import { useStore } from 'vuex'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  groupId: {
    type: [String, Number],
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'update'])

const store = useStore()
const avatarInput = ref(null)
const saving = ref(false)
const memberSearch = ref('')
const inviteDialogVisible = ref(false)
const announceDialogVisible = ref(false)
const configDialogVisible = ref(false)

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const currentUserId = computed(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return userInfo.userId || userInfo.id
})

const groupInfo = ref({
  id: null,
  name: '',
  description: '',
  avatar: ''
})

const memberList = ref([])

const isOwner = computed(() => {
  const currentUser = memberList.value.find(m => m.id === currentUserId.value)
  return currentUser?.role === 'OWNER'
})

const filteredMembers = computed(() => {
  if (!memberSearch.value) return memberList.value
  return memberList.value.filter(member => 
    (member.name && member.name.includes(memberSearch.value)) ||
    (member.username && member.username.includes(memberSearch.value))
  )
})

function getRoleLabel(role) {
  const roleMap = {
    'OWNER': '群主',
    'ADMIN': '管理员',
    'MEMBER': '成员'
  }
  return roleMap[role] || '成员'
}

async function loadGroupInfo() {
  if (!props.groupId) return
  
  try {
    const response = await store.dispatch('im/getGroupInfo', props.groupId)
    groupInfo.value = response.data || {}
  } catch (error) {
    console.error('加载群聊信息失败:', error)
    ElMessage.error('加载群聊信息失败')
  }
}

async function loadMemberList() {
  if (!props.groupId) return
  
  try {
    const response = await store.dispatch('im/getGroupMembers', props.groupId)
    memberList.value = response.data || []
  } catch (error) {
    console.error('加载成员列表失败:', error)
    ElMessage.error('加载成员列表失败')
  }
}

function showAvatarUpload() {
  avatarInput.value?.click()
}

async function handleAvatarChange(event) {
  const file = event.target.files[0]
  if (!file) return
  
  try {
    // 这里应该上传图片到服务器
    ElMessage.info('头像上传功能开发中...')
  } catch (error) {
    ElMessage.error('头像上传失败')
  }
}

function showInviteDialog() {
  inviteDialogVisible.value = true
}

function showAnnounceDialog() {
  announceDialogVisible.value = true
}

function showGroupConfig() {
  configDialogVisible.value = true
}

async function handleMemberAction(command) {
  // 这里需要实现成员管理逻辑
  switch (command) {
    case 'setAdmin':
      ElMessage.info('设置管理员功能开发中...')
      break
    case 'removeAdmin':
      ElMessage.info('取消管理员功能开发中...')
      break
    case 'mute':
      ElMessage.info('禁言功能开发中...')
      break
    case 'remove':
      try {
        await ElMessageBox.confirm('确定要将该成员移出群聊吗？', '确认移除', {
          type: 'warning'
        })
        ElMessage.info('移除功能开发中...')
      } catch {
        // 用户取消
      }
      break
  }
}

async function saveSettings() {
  if (!groupInfo.value.name.trim()) {
    ElMessage.error('群聊名称不能为空')
    return
  }
  
  saving.value = true
  try {
    await store.dispatch('im/updateGroup', {
      groupId: groupInfo.value.id,
      name: groupInfo.value.name,
      description: groupInfo.value.description
    })
    ElMessage.success('群聊信息已更新')
    emit('update', groupInfo.value)
  } catch (error) {
    console.error('保存群聊设置失败:', error)
    ElMessage.error('保存群聊设置失败')
  } finally {
    saving.value = false
  }
}

async function transferGroup() {
  try {
    await ElMessageBox.confirm('确定要转让群主权限吗？', '确认转让', {
      type: 'warning'
    })
    ElMessage.info('转让群主功能开发中...')
  } catch {
    // 用户取消
  }
}

async function disbandGroup() {
  try {
    await ElMessageBox.confirm(
      '确定要解散群聊吗？此操作不可恢复！',
      '确认解散',
      {
        type: 'error',
        confirmButtonText: '确定解散',
        confirmButtonClass: 'el-button--danger'
      }
    )
    ElMessage.info('解散群聊功能开发中...')
  } catch {
    // 用户取消
  }
}

async function quitGroup() {
  try {
    await ElMessageBox.confirm(
      '确定要退出群聊吗？退出后将无法收到群聊消息。',
      '确认退出',
      { type: 'warning' }
    )
    ElMessage.info('退出群聊功能开发中...')
  } catch {
    // 用户取消
  }
}

function handleInviteSuccess() {
  loadMemberList()
  ElMessage.success('邀请成功')
}

function handleAnnounceSuccess() {
  ElMessage.success('公告发布成功')
}

function handleConfigSuccess() {
  loadGroupInfo()
  ElMessage.success('群聊配置已更新')
}

function handleClose() {
  visible.value = false
}

// 监听群聊ID变化
watch(() => props.groupId, (newId) => {
  if (newId && visible.value) {
    loadGroupInfo()
    loadMemberList()
  }
}, { immediate: true })

// 监听显示状态
watch(visible, (newVisible) => {
  if (newVisible && props.groupId) {
    loadGroupInfo()
    loadMemberList()
  }
})
</script>

<style scoped>
.group-settings {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.settings-section {
  margin-bottom: 24px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.settings-section.danger {
  background: #fff2f0;
  border: 1px solid #ffccc7;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 16px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.group-info {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.group-avatar {
  cursor: pointer;
  transition: transform 0.2s;
}

.group-avatar:hover {
  transform: scale(1.05);
}

.upload-btn {
  font-size: 12px;
  padding: 4px 8px;
}

.group-fields {
  flex: 1;
}

.management-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.member-search {
  width: 200px;
}

.member-list {
  max-height: 400px;
  overflow-y: auto;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.member-item:last-child {
  border-bottom: none;
}

.member-avatar {
  flex-shrink: 0;
}

.member-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.member-name {
  font-size: 14px;
  font-weight: 500;
  color: #262626;
}

.member-role {
  font-size: 12px;
  color: #8c8c8c;
}

.member-actions {
  flex-shrink: 0;
}

.danger-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.settings-footer {
  margin-top: auto;
  padding: 16px 0;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

/* 滚动条样式 */
.member-list::-webkit-scrollbar {
  width: 4px;
}

.member-list::-webkit-scrollbar-track {
  background: transparent;
}

.member-list::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 2px;
}

.member-list::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}

/* 深色主题适配 */
@media (prefers-color-scheme: dark) {
  .settings-section {
    background: #2a2a2a;
  }
  
  .settings-section.danger {
    background: #2a1a1a;
    border-color: #8b0000;
  }
  
  .section-title {
    color: #e8e8e8;
  }
  
  .member-item {
    border-bottom-color: #404040;
  }
  
  .member-name {
    color: #e8e8e8;
  }
  
  .member-role {
    color: #ccc;
  }
  
  .settings-footer {
    border-top-color: #404040;
  }
}
</style>