<template>
  <div class="contacts-panel">
    <div class="contacts-left">
      <div class="contacts-header">
        <h2 class="contacts-title">通讯录</h2>
        <div class="header-actions">
          <el-badge :value="pendingRequestsCount" :hidden="pendingRequestsCount === 0">
            <el-button
              :icon="UserFilled"
              text
              circle
              @click="showFriendRequests = true"
            />
          </el-badge>
          <el-button :icon="Plus" text circle @click="handleAddContact" />
        </div>
      </div>

      <div class="search-box">
        <el-icon class="search-icon"><Search /></el-icon>
        <el-input
          v-model="searchQuery"
          placeholder="搜索联系人"
          class="search-input"
          clearable
        />
      </div>

      <div v-loading="loading" class="contacts-body">
        <div v-if="!searchQuery" class="departments-section">
          <div class="section-header">
            <el-icon><OfficeBuilding /></el-icon>
            <span>组织架构</span>
          </div>
          <el-tree
            :data="orgTree"
            :props="treeProps"
            node-key="id"
            default-expand-all
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <div class="tree-node">
                <div class="node-icon" :class="data.type === 'dept' ? 'dept-icon' : 'user-icon'">
                  <el-icon v-if="data.type === 'dept'"><OfficeBuilding /></el-icon>
                  <el-icon v-else><User /></el-icon>
                </div>
                <span class="node-label">{{ node.label }}</span>
                <span v-if="data.type === 'dept'" class="node-count">({{ data.count || 0 }})</span>
              </div>
            </template>
          </el-tree>
        </div>

        <div class="contacts-section">
          <div class="section-header">
            <el-icon><User /></el-icon>
            <span>全部联系人</span>
            <el-badge :value="filteredContacts.length" class="contact-badge" />
          </div>
          <div class="contacts-list">
            <div
              v-for="contact in filteredContacts"
              :key="contact.id"
              class="contact-item"
              :class="{ active: selectedContact?.id === contact.id }"
              @click="handleContactClick(contact)"
            >
              <div class="contact-avatar">{{ contact.avatar }}</div>
              <div class="contact-info">
                <div class="contact-name-row">
                  <span class="contact-name">{{ contact.name }}</span>
                  <el-icon v-if="contact.isFavorite" class="favorite-icon"><Star /></el-icon>
                </div>
                <p class="contact-position">{{ contact.position }} · {{ contact.department }}</p>
              </div>
            </div>
          </div>
        </div>

        <div v-if="!loading && contacts.length === 0" class="empty-state">
          <el-empty description="暂无联系人" />
        </div>
      </div>
    </div>

    <div class="contacts-right">
      <div v-if="selectedContact" class="contact-detail">
        <div class="detail-header">
          <div class="detail-avatar">{{ selectedContact.avatar }}</div>
          <div class="detail-info">
            <div class="detail-name-row">
              <h2 class="detail-name">{{ selectedContact.name }}</h2>
              <el-icon v-if="selectedContact.isFavorite" class="detail-favorite"><Star /></el-icon>
            </div>
            <p class="detail-position">{{ selectedContact.position }} · {{ selectedContact.department }}</p>
            <div class="detail-actions">
              <el-button type="primary" class="action-btn" @click="handleSendMessage">
                <el-icon><ChatDotRound /></el-icon>
                发消息
              </el-button>
              <el-button class="action-btn action-green">
                <el-icon><Phone /></el-icon>
                语音
              </el-button>
              <el-button class="action-btn action-blue">
                <el-icon><VideoCamera /></el-icon>
                视频
              </el-button>
              <el-button class="action-btn" @click="toggleFavorite">
                {{ selectedContact.isFavorite ? '取消收藏' : '添加收藏' }}
              </el-button>
            </div>
          </div>
        </div>

        <div class="detail-body">
          <div class="info-card">
            <h3 class="info-title">基本信息</h3>
            <div class="info-row">
              <span class="info-label">姓名</span>
              <span class="info-value">{{ selectedContact.name }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">职位</span>
              <span class="info-value">{{ selectedContact.position }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">部门</span>
              <span class="info-value">{{ selectedContact.department }}</span>
            </div>
          </div>

          <div class="info-card">
            <h3 class="info-title">联系方式</h3>
            <div class="info-row">
              <span class="info-label">手机</span>
              <span class="info-value info-link">{{ selectedContact.phone }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">邮箱</span>
              <span class="info-value info-link">{{ selectedContact.email }}</span>
            </div>
          </div>

          <div class="info-card">
            <h3 class="info-title">其他信息</h3>
            <div class="info-row">
              <span class="info-label">工号</span>
              <span class="info-value">2024{{ String(selectedContact.id).padStart(4, '0') }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">入职时间</span>
              <span class="info-value">2023-05-15</span>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <el-icon class="empty-icon"><User /></el-icon>
        <p class="empty-text">选择联系人查看详情</p>
      </div>
    </div>

    <!-- 好友申请对话框 -->
    <FriendRequestsDialog
      v-model="showFriendRequests"
      @refresh="loadContacts"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Search,
  OfficeBuilding,
  User,
  ArrowRight,
  Star,
  ChatDotRound,
  Phone,
  VideoCamera,
  UserFilled,
  Plus
} from '@element-plus/icons-vue'
import { getContacts, searchContacts, getFriendRequests } from '@/api/im/contact'
import { getOrgTree, getDepartmentMembers } from '@/api/im/organization'
import FriendRequestsDialog from '@/components/FriendRequestsDialog/index.vue'

const searchQuery = ref('')
const selectedContact = ref(null)
const contacts = ref([])
const loading = ref(false)
const showFriendRequests = ref(false)
const pendingRequestsCount = ref(0)

// 组织架构树
const orgTree = ref([])
const treeProps = {
  children: 'children',
  label: 'name'
}

// 加载组织架构
const loadOrgTree = async () => {
  try {
    const response = await getOrgTree()
    if (response && response.data) {
      orgTree.value = response.data
    }
  } catch (error) {
    console.error('加载组织架构失败:', error)
  }
}

// 加载好友申请数量
const loadPendingRequests = async () => {
  try {
    const response = await getFriendRequests()
    if (response && response.data) {
      pendingRequestsCount.value = response.data.filter(r => 
        r.direction === 'RECEIVED' && r.status === 'PENDING'
      ).length
    }
  } catch (error) {
    console.error('加载好友申请失败:', error)
  }
}

// 处理树节点点击
const handleNodeClick = async (data) => {
  if (data.type === 'dept') {
    // 加载部门成员
    try {
      const response = await getDepartmentMembers(data.id)
      if (response && response.data) {
        contacts.value = response.data.map(item => ({
          id: item.userId || item.id,
          name: item.userName || item.name || '未知',
          avatar: item.avatar?.charAt(0) || item.name?.charAt(0) || '?',
          position: item.position || '职位未知',
          department: data.name,
          phone: item.phone || '未填写',
          email: item.email || '未填写',
          isFavorite: item.isFavorite || false
        }))
      }
    } catch (error) {
      console.error('加载部门成员失败:', error)
    }
  } else if (data.type === 'user') {
    // 选中用户
    selectedContact.value = contacts.value.find(c => c.id === data.id)
  }
}

// 添加联系人
const handleAddContact = () => {
  ElMessage.info('添加联系人功能开发中...')
  // TODO: 打开添加联系人对话框
}

// 加载联系人列表
const loadContacts = async () => {
  loading.value = true
  try {
    const response = await getContacts()
    if (response && response.data) {
      contacts.value = response.data.map(item => ({
        id: item.friendId || item.id,
        name: item.friendName || item.name || '未知',
        avatar: item.avatar?.charAt(0) || item.name?.charAt(0) || '?',
        position: item.position || '职位未知',
        department: item.department || '部门未知',
        phone: item.phone || '未填写',
        email: item.email || '未填写',
        isFavorite: item.isFavorite || false
      }))
    }
  } catch (error) {
    console.error('加载联系人失败:', error)
    ElMessage.error('加载联系人失败')
  } finally {
    loading.value = false
  }
}

// 过滤联系人
const filteredContacts = computed(() => {
  if (!searchQuery.value) {
    return contacts.value
  }
  const query = searchQuery.value.toLowerCase()
  return contacts.value.filter(contact =>
    contact.name.toLowerCase().includes(query) ||
    contact.department.toLowerCase().includes(query) ||
    contact.position.toLowerCase().includes(query)
  )
})

// 处理联系人点击
const handleContactClick = (contact) => {
  selectedContact.value = contact
}

// 处理部门点击
const handleDepartmentClick = (dept) => {
  console.log('Department clicked:', dept)
  // TODO: 加载部门成员
}

// 发送消息
const handleSendMessage = () => {
  if (selectedContact.value) {
    console.log('发送消息给:', selectedContact.value.name)
    // TODO: 创建会话并跳转到聊天页面
    ElMessage.info('功能开发中...')
  }
}

// 切换收藏状态
const toggleFavorite = () => {
  if (selectedContact.value) {
    selectedContact.value.isFavorite = !selectedContact.value.isFavorite
    ElMessage.success(selectedContact.value.isFavorite ? '已添加收藏' : '已取消收藏')
    // TODO: 调用 API 更新收藏状态
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadContacts()
  loadOrgTree()
  loadPendingRequests()
})
</script>

<style scoped>
.contacts-panel {
  flex: 1;
  display: flex;
  background: #fff;
  overflow: hidden;
}

.contacts-left {
  width: 320px;
  min-width: 320px;
  flex-shrink: 0;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
}

.contacts-header {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;

  .contacts-title {
    font-size: 20px;
    font-weight: 600;
    color: #262626;
    margin: 0 0 12px 0;
  }

  .search-box {
    position: relative;

    .search-icon {
      position: absolute;
      left: 10px;
      top: 50%;
      transform: translateY(-50%);
      font-size: 16px;
      color: #8c8c8c;
    }

    .search-input {
      width: 100%;

      :deep(.el-input__wrapper) {
        background-color: #f5f5f5;
        border: none;
        padding-left: 36px;
        box-shadow: none;
      }
    }
  }
}

.contacts-body {
  flex: 1;
  overflow-y: auto;
}

.departments-section {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #595959;
  margin-bottom: 12px;

  .el-icon {
    font-size: 16px;
  }

  .contact-badge {
    margin-left: auto;
  }
}

.departments-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.department-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: #f5f5f5;
  }

  .dept-info {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .dept-icon {
    width: 36px;
    height: 36px;
    border-radius: 8px;
    background: linear-gradient(135deg, #3b82f6, #2563eb);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 500;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .dept-name {
    font-size: 14px;
    color: #262626;
  }

  .dept-meta {
    display: flex;
    align-items: center;
    gap: 8px;

    .dept-count {
      font-size: 12px;
      color: #8c8c8c;
    }

    .el-icon {
      font-size: 16px;
      color: #8c8c8c;
    }
  }
}

.contacts-section {
  padding: 16px;
}

.contacts-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: #f5f5f5;
  }

  &.active {
    background-color: #e6f7ff;
  }

  .contact-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: linear-gradient(135deg, #3b82f6, #2563eb);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: 500;
    flex-shrink: 0;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .contact-info {
    flex: 1;
    min-width: 0;
  }

  .contact-name-row {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-bottom: 2px;
  }

  .contact-name {
    font-size: 14px;
    font-weight: 500;
    color: #262626;
  }

  .favorite-icon {
    font-size: 12px;
    color: #faad14;
    flex-shrink: 0;
  }

  .contact-position {
    font-size: 12px;
    color: #8c8c8c;
    margin: 0;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.contacts-right {
  flex: 1;
  background-color: #f7f8fa;
  overflow-y: auto;
}

.contact-detail {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.detail-header {
  background: #fff;
  padding: 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  gap: 16px;
}

.detail-avatar {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 600;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.detail-info {
  flex: 1;
}

.detail-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.detail-name {
  font-size: 24px;
  font-weight: 500;
  color: #262626;
  margin: 0;
}

.detail-favorite {
  font-size: 20px;
  color: #faad14;
}

.detail-position {
  font-size: 14px;
  color: #595959;
  margin: 0 0 16px 0;
}

.detail-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;

  .action-btn {
    display: flex;
    align-items: center;
    gap: 4px;

    &.action-green {
      border-color: #d9f7be;
      color: #52c41a;

      &:hover {
        background-color: #f6ffed;
      }
    }

    &.action-blue {
      border-color: #bae7ff;
      color: #1890ff;

      &:hover {
        background-color: #e6f7ff;
      }
    }
  }
}

.detail-body {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.info-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.info-title {
  font-size: 16px;
  font-weight: 500;
  color: #262626;
  margin: 0 0 16px 0;
}

.info-row {
  display: flex;
  align-items: center;
  padding: 8px 0;

  .info-label {
    width: 96px;
    font-size: 14px;
    color: #8c8c8c;
    flex-shrink: 0;
  }

  .info-value {
    flex: 1;
    font-size: 14px;
    color: #262626;

    &.info-link {
      color: #1890ff;
      cursor: pointer;

      &:hover {
        text-decoration: underline;
      }
    }
  }
}

.empty-state {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c8c8c;
  padding: 40px 20px;

  .empty-icon {
    font-size: 64px;
    color: #d9d9d9;
    margin-bottom: 16px;
  }

  .empty-text {
    font-size: 14px;
    margin: 0;
  }
}
</style>
