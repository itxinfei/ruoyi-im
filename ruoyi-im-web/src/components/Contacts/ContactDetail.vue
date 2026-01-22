<template>
  <div v-if="contact" class="contact-detail">
    <div class="header">
      <div class="avatar-large">
        <img v-if="getAvatar" :src="getAvatar" />
        <div v-else class="avatar-text">
          {{ getName.charAt(0) }}
        </div>
      </div>
      <div class="main-info">
        <div class="name-row">
          <span class="name">{{ getName }}</span>
          <template v-if="!contact.isGroup">
            <el-tag size="small" type="success" v-if="contact.online">在线</el-tag>
            <el-tag size="small" type="info" v-else>离线</el-tag>
          </template>
        </div>
        <div class="meta">
          <template v-if="contact.isGroup">
            群号: {{ contact.id }} · {{ contact.memberCount || 0 }} 成员
          </template>
          <template v-else>
            {{ contact.position || '职位未知' }} · {{ contact.department || '部门未知' }}
          </template>
        </div>
      </div>
    </div>

    <div class="actions">
      <el-button type="primary" :icon="ChatDotRound" @click="startChat">发消息</el-button>
      
      <template v-if="!contact.isGroup">
        <el-button :icon="contact.isFavorite ? StarFilled : Star" @click="toggleFavorite">
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
      </template>
      <template v-else>
        <!-- 群组操作 -->
        <el-button :icon="Setting" @click="handleGroupConfig">群设置</el-button>
      </template>
    </div>

    <el-divider />

    <div class="info-list">
      <template v-if="contact.isGroup">
        <div class="info-item">
          <span class="label">群公告</span>
          <span class="value">{{ contact.notice || '暂无公告' }}</span>
        </div>
        <div class="info-item">
          <span class="label">群描述</span>
          <span class="value">{{ contact.description || '暂无描述' }}</span>
        </div>
        <div class="info-item">
          <span class="label">群主</span>
          <span class="value">{{ contact.ownerName || '-' }}</span>
        </div>
      </template>
      <template v-else>
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
      </template>
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
  MoreFilled,
  Setting
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { updateContactRemark, deleteContact } from '@/api/im/contact'
import { createConversation } from '@/api/im/conversation'

const props = defineProps({
  contact: Object
})

const emit = defineEmits(['update'])
const store = useStore()

const getName = computed(() => {
  if (!props.contact) return ''
  return props.contact.isGroup ? props.contact.name : props.contact.friendName
})

const getAvatar = computed(() => {
  if (!props.contact) return ''
  return props.contact.isGroup ? props.contact.avatar : props.contact.friendAvatar
})

const startChat = async () => {
  try {
    const isGroup = props.contact.isGroup
    // For friends, targetId is friendId. For groups, it's id.
    const targetId = isGroup ? props.contact.id : props.contact.friendId
    const type = isGroup ? 'GROUP' : 'PRIVATE'

    const res = await createConversation({ type, targetId })
    if (res.code === 200) {
      const conv = res.data
      store.commit('im/SET_CURRENT_SESSION', conv)
      // Signal parent or use router to switch to Chat tab
      ElMessage.success('已发起聊天')
      // NOTE: We usually emit an event or use a global event bus to switch tabs
      window.dispatchEvent(new CustomEvent('switch-tab', { detail: 'chat' }))
    }
  } catch (e) {
    console.error('发起聊天失败', e)
    ElMessage.error('无法发起聊天')
  }
}

const handleGroupConfig = () => {
  ElMessage.info('该功能正在迭代中...')
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
