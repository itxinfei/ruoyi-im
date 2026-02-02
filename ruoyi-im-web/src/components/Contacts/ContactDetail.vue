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
          <template v-if="!isGroup">
            <el-tag size="small" type="success" v-if="contact.online">在线</el-tag>
            <el-tag size="small" type="info" v-else>离线</el-tag>
          </template>
        </div>
        <div class="meta">
          <template v-if="isGroup">
            群号: {{ contact.id }} · {{ contact.memberCount || contact.memberCount || 0 }} 成员
          </template>
          <template v-else>
            {{ getPosition }} · {{ getDepartment }}
          </template>
        </div>
      </div>
    </div>

    <div class="actions">
      <el-button type="primary" :icon="ChatDotRound" @click="startChat">发消息</el-button>
      <el-button :icon="Phone" @click="$emit('voice-call', contact)">语音通话</el-button>
      <el-button :icon="VideoCamera" @click="$emit('video-call', contact)">视频通话</el-button>
      
      <template v-if="!isGroup">
        <el-button :icon="isFavorite ? StarFilled : Star" @click="toggleFavorite">
          {{ isFavorite ? '已收藏' : '收藏' }}
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
      <template v-if="isGroup">
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
          <span class="value">{{ getPhone }}</span>
        </div>
        <div class="info-item">
          <span class="label">邮箱</span>
          <span class="value">{{ getEmail }}</span>
        </div>
        <div class="info-item">
          <span class="label">签名</span>
          <span class="value">{{ getSignature }}</span>
        </div>
      </template>
    </div>
  </div>
  <div v-else class="empty-state">
    <el-empty description="选择联系人查看详情" />
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useStore } from 'vuex'
import {
  ChatDotRound,
  Star,
  StarFilled,
  MoreFilled,
  Setting,
  Phone,
  VideoCamera
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { updateContactRemark, deleteContact } from '@/api/im/contact'
import { addFavorite, removeFavorite, isFavorited } from '@/api/im/favorite'
import { addTokenToUrl } from '@/utils/file'

const props = defineProps({
  contact: Object
})

const emit = defineEmits(['update', 'voice-call', 'video-call', 'message'])
const store = useStore()

// 收藏状态
const isFavorite = ref(false)

// 统一类型判断逻辑（兼容多种写法）
const isGroup = computed(() => {
  return props.contact?.isGroup || props.contact?.type === 'group'
})

// 获取显示名称（兼容多种字段）
const getName = computed(() => {
  if (!props.contact) return '未知用户'
  if (isGroup.value) {
    return props.contact.name || props.contact.groupName || '未知群组'
  }
  return props.contact.name || props.contact.nickname || props.contact.friendName || props.contact.displayName || '未知用户'
})

// 获取头像（兼容多种字段）
const getAvatar = computed(() => {
  if (!props.contact) return ''
  if (isGroup.value) {
    const avatar = props.contact.avatar || props.contact.groupAvatar
    return addTokenToUrl(avatar)
  }
  const avatar = props.contact.avatar || props.contact.friendAvatar || props.contact.headImg
  return addTokenToUrl(avatar)
})

// 获取职位（兼容 dept/department 字段）
const getPosition = computed(() => {
  if (!props.contact) return ''
  return props.contact.position || props.contact.job || '职位未知'
})

// 获取部门（兼容 dept/department 字段）
const getDepartment = computed(() => {
  if (!props.contact) return ''
  return props.contact.dept || props.contact.department || '部门未知'
})

// 获取手机号（兼容 phone/mobile 字段）
const getPhone = computed(() => {
  if (!props.contact) return ''
  return props.contact.phone || props.contact.mobile || '-'
})

// 获取邮箱
const getEmail = computed(() => {
  if (!props.contact) return ''
  return props.contact.email || '-'
})

// 获取签名
const getSignature = computed(() => {
  if (!props.contact) return ''
  return props.contact.signature || props.contact.sign || '这个人很懒，什么都没留下～'
})

// 检查收藏状态
const checkFavoriteStatus = async () => {
  if (!props.contact?.id) return
  try {
    const res = await isFavorited(props.contact.id)
    isFavorite.value = res.code === 200 && res.data === true
  } catch (err) {
    isFavorite.value = false
  }
}

// 监听联系人变化，检查收藏状态
watch(() => props.contact?.id, (newId) => {
  if (newId && !props.contact?.isGroup) {
    checkFavoriteStatus()
  }
}, { immediate: true })

const startChat = () => {
  // 触发 message 事件，让父组件处理创建会话和切换
  emit('message', props.contact)
}

const handleGroupConfig = () => {
  ElMessage.info('该功能正在迭代中...')
}

const toggleFavorite = async () => {
  if (!props.contact?.id) return
  try {
    if (isFavorite.value) {
      // 取消收藏
      await removeFavorite(props.contact.id)
      isFavorite.value = false
      ElMessage.success('已取消收藏')
    } else {
      // 添加收藏
      await addFavorite({
        messageId: props.contact.id,
        remark: '联系人收藏'
      })
      isFavorite.value = true
      ElMessage.success('已添加到收藏')
    }
  } catch (err) {
    console.error('操作失败:', err)
    ElMessage.error('操作失败，请重试')
  }
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

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.contact-detail {
  padding: 24px;
  background: #ffffff;
  height: 100%;
  overflow-y: auto;

  @extend .scrollbar-sm;

  .dark & {
    background: var(--dt-bg-card-dark);
  }
}

.header {
  display: flex;
  margin-bottom: 24px;
  align-items: center;
}

.avatar-large {
  width: 80px;
  height: 80px;
  border-radius: var(--dt-radius-lg);
  background: var(--dt-brand-color);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  margin-right: 20px;
  flex-shrink: 0;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.main-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  flex: 1;
  min-width: 0;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.name {
  font-size: var(--dt-font-size-2xl);
  font-weight: var(--dt-font-weight-semibold);
  color: var(--dt-text-primary);

  .dark & {
    color: var(--dt-text-primary-dark);
  }
}

.meta {
  color: var(--dt-text-secondary);
  font-size: var(--dt-font-size-sm);

  .dark & {
    color: var(--dt-text-secondary-dark);
  }
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 24px;
}

.info-list {
  display: flex;
  flex-direction: column;
}

.info-item {
  display: flex;
  padding: 16px 0;
  border-bottom: 1px solid var(--dt-border-lighter);

  &:last-child {
    border-bottom: none;
  }

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }
}

.label {
  width: 80px;
  color: var(--dt-text-secondary);
  flex-shrink: 0;
  font-size: var(--dt-font-size-sm);

  .dark & {
    color: var(--dt-text-secondary-dark);
  }
}

.value {
  flex: 1;
  color: var(--dt-text-primary);
  word-break: break-word;

  .dark & {
    color: var(--dt-text-primary-dark);
  }
}

.empty-state {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--dt-bg-body);

  .dark & {
    background: var(--dt-bg-body-dark);
  }
}

.danger-text {
  color: var(--dt-error-color);
}

// ============================================================================
// 响应式适配
// ============================================================================

@media (max-width: 768px) {
  .contact-detail {
    padding: 16px;
  }

  .header {
    flex-direction: column;
    text-align: center;
  }

  .avatar-large {
    margin-right: 0;
    margin-bottom: 16px;
  }

  .name-row {
    justify-content: center;
  }

  .actions {
    justify-content: center;
  }
}
</style>
