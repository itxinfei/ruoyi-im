<template>
  <div v-if="contact" class="contact-detail">
    <div class="header">
      <div class="avatar-large">
        <img v-if="contact.friendAvatar" :src="contact.friendAvatar" />
        <div v-else class="avatar-text">
          {{ (contact.friendName || '?').charAt(0) }}
        </div>
      </div>
      <div class="main-info">
        <div class="name-row">
          <span class="name">{{ contact.friendName }}</span>
          <el-tag size="small" type="success" v-if="contact.online">在线</el-tag>
          <el-tag size="small" type="info" v-else>离线</el-tag>
        </div>
        <div class="meta">{{ contact.position || '职位未知' }} · {{ contact.department || '部门未知' }}</div>
      </div>
    </div>

    <div class="actions">
      <el-button type="primary" :icon="ChatDotRound" @click="startChat">发消息</el-button>
      <el-button :icon="hide ? StarFilled : Star" @click="toggleFavorite">
        {{ contact.isFavorite ? '已收藏' : '收藏' }}
      </el-button>
      <el-dropdown @command="handleMoreCommand">
        <el-button :icon="MoreFilled" circle />
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="editRemark">修改备注</el-dropdown-item>
            <el-dropdown-item command="delete" divided class="danger-text">删除好友</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <el-divider />

    <div class="info-list">
      <div class="info-item">
        <span class="label">手机</span>
        <span class="value">{{ contact.phone || '-' }}</span>
      </div>
      <div class="info-item">
        <span class="label">邮箱</span>
        <span class="value">{{ contact.email || '-' }}</span>
      </div>
      <div class="info-item">
        <span class="label">签名</span>
        <span class="value">{{ contact.signature || '-' }}</span>
      </div>
      <div class="info-item">
        <span class="label">分组</span>
        <span class="value">{{ contact.groupName || '默认分组' }}</span>
      </div>
    </div>
  </div>
  <div v-else class="empty-state">
    <el-empty description="选择联系人查看详情" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useStore } from 'vuex'
import { 
  ChatDotRound, 
  Star, 
  StarFilled, 
  MoreFilled 
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { updateContactRemark, deleteContact } from '@/api/im/contact'

const props = defineProps({
  contact: Object
})

const emit = defineEmits(['update'])
const startChat = () => {
  // Navigate to chat with this user
  store.dispatch('im/selectSession', {
    id: props.contact.friendId, // Assuming session ID matches user ID for private chat or need creation
    type: 'private',
    name: props.contact.friendName
  })
}

const toggleFavorite = async () => {
  ElMessage.info('收藏功能开发中')
}

const handleMoreCommand = (cmd) => {
  if (cmd === 'delete') {
    ElMessageBox.confirm('确定要删除该好友吗？', '提示', {
      type: 'warning'
    }).then(async () => {
      await deleteContact(props.contact.id)
      ElMessage.success('删除成功')
      emit('update')
    })
  } else if (cmd === 'editRemark') {
    ElMessageBox.prompt('请输入备注', '修改备注', {
      inputValue: props.contact.friendName
    }).then(async ({ value }) => {
      await updateContactRemark(props.contact.id, { remark: value })
      ElMessage.success('修改成功')
      emit('update')
    })
  }
}
</script>

<style scoped>
.contact-detail {
  padding: 40px;
  background: white;
  height: 100%;
}

.header {
  display: flex;
  margin-bottom: 24px;
}

.avatar-large {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  margin-right: 20px;
}

.main-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.name {
  font-size: 24px;
  font-weight: bold;
}

.meta {
  color: #666;
}

.actions {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  border-bottom: 1px solid #f9f9f9;
  padding-bottom: 12px;
}

.label {
  width: 80px;
  color: #999;
}

.value {
  color: #333;
  flex: 1;
}

.empty-state {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.danger-text {
  color: #f56c6c;
}
</style>
