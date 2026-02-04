<template>
  <div class="contact-detail-container" :class="{ 'is-empty': !contact }">
    <Transition name="slide-fade" mode="out-in">
      <div v-if="contact" :key="contact.id" class="detail-content custom-scrollbar">
        <!-- Header with Blur Background -->
        <div class="profile-header">
          <div class="header-bg" :style="{ backgroundImage: `url(${getAvatar})` }"></div>
          <div class="header-content">
            <div class="avatar-wrapper">
              <el-avatar 
                :size="120" 
                :src="getAvatar" 
                :shape="isGroup ? 'square' : 'circle'"
                class="main-avatar"
              >
                {{ getName.charAt(0) }}
              </el-avatar>
              <div v-if="!isGroup" class="status-dot" :class="{ online: contact.online }"></div>
            </div>
            
            <div class="user-identity">
              <h2 class="display-name">{{ getName }}</h2>
              <p class="signature">{{ getSignature }}</p>
              
              <div class="tags-container" v-if="!isGroup">
                <el-tag 
                  :type="contact.online ? 'success' : 'info'" 
                  effect="dark" 
                  size="small" 
                  round
                  class="status-tag"
                >
                  {{ contact.online ? 'Online' : 'Offline' }}
                </el-tag>
                <el-tag 
                  v-if="getDepartment"
                  type="primary" 
                  effect="light" 
                  size="small" 
                  round
                >
                  {{ getDepartment }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- Quick Actions -->
        <div class="quick-actions">
          <div class="action-item" @click="startChat">
            <div class="icon-box primary">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <span>发消息</span>
          </div>

          <template v-if="!isGroup">
            <div class="action-item" @click="$emit('voice-call', contact)">
              <div class="icon-box success">
                <el-icon><Phone /></el-icon>
              </div>
              <span>语音通话</span>
            </div>
            
            <div class="action-item" @click="$emit('video-call', contact)">
              <div class="icon-box warning">
                <el-icon><VideoCamera /></el-icon>
              </div>
              <span>视频通话</span>
            </div>

            <div class="action-item" @click="toggleFavorite">
              <div class="icon-box star" :class="{ active: isFavorite }">
                <el-icon><component :is="isFavorite ? StarFilled : Star" /></el-icon>
              </div>
              <span>{{ isFavorite ? '已收藏' : '收藏' }}</span>
            </div>
          </template>

          <template v-else>
            <div class="action-item" @click="handleGroupConfig">
              <div class="icon-box info">
                <el-icon><Setting /></el-icon>
              </div>
              <span>群设置</span>
            </div>
          </template>
        </div>

        <!-- Information Cards -->
        <div class="info-sections">
          <!-- Basic Info -->
          <div class="section-card">
            <div class="card-title">基本信息</div>
            
            <div class="info-list">
              <div class="info-item" v-if="!isGroup">
                <div class="label">
                  <el-icon><Iphone /></el-icon>
                  手机号码
                </div>
                <div class="value">{{ getPhone }}</div>
                <el-button v-if="getPhone !== '-'" link type="primary" size="small" class="copy-btn" @click="copyText(getPhone)">
                  复制
                </el-button>
              </div>

              <div class="info-item" v-if="!isGroup">
                <div class="label">
                  <el-icon><Message /></el-icon>
                  电子邮箱
                </div>
                <div class="value">{{ getEmail }}</div>
              </div>

              <div class="info-item" v-if="!isGroup">
                <div class="label">
                  <el-icon><Suitcase /></el-icon>
                  职位信息
                </div>
                <div class="value">{{ getPosition }}</div>
              </div>

              <div class="info-item" v-if="isGroup">
                <div class="label">
                  <el-icon><InfoFilled /></el-icon>
                  群公告
                </div>
                <div class="value announcement">{{ contact.notice || '暂无公告' }}</div>
              </div>
              
              <div class="info-item" v-if="isGroup">
                <div class="label">
                  <el-icon><User /></el-icon>
                  群主
                </div>
                <div class="value">{{ contact.ownerName || '-' }}</div>
              </div>
            </div>
          </div>

          <!-- Settings / Danger Zone -->
          <div class="section-card">
            <div class="card-title">更多操作</div>
            <div class="action-list">
              <div class="list-action-item" @click="handleEditRemark">
                <div class="left">
                  <el-icon><Edit /></el-icon>
                  <span>修改备注</span>
                </div>
                <el-icon class="arrow"><ArrowRight /></el-icon>
              </div>
              
              <div class="list-action-item danger" @click="handleDeleteContact">
                <div class="left">
                  <el-icon><Delete /></el-icon>
                  <span>删除好友</span>
                </div>
                <el-icon class="arrow"><ArrowRight /></el-icon>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-placeholder">
        <el-empty 
          description="选择联系人查看详情" 
          :image-size="200"
        >
          <template #image>
            <div class="empty-illustration">
              <el-icon><User /></el-icon>
            </div>
          </template>
        </el-empty>
      </div>
    </Transition>

    <GroupProfileDialog
      v-if="isGroup"
      v-model="showGroupSettings"
      :group-id="contact?.id"
      @update-group="emit('update')"
    />
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import {
  ChatDotRound, Star, StarFilled, Setting, Phone, VideoCamera,
  Iphone, Message, Suitcase, Edit, Delete, InfoFilled, User,
  ArrowRight
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { updateContactRemark, deleteContact } from '@/api/im/contact'
import { addFavorite, removeFavorite, isFavorited } from '@/api/im/favorite'
import { addTokenToUrl } from '@/utils/file'
import { copyToClipboard } from '@/utils/format'
import GroupProfileDialog from '@/components/Contacts/GroupProfileDialog.vue'

const props = defineProps({
  contact: Object
})

const emit = defineEmits(['update', 'voice-call', 'video-call', 'message'])

const isFavorite = ref(false)
const showGroupSettings = ref(false)

// Computed Properties
const isGroup = computed(() => props.contact?.isGroup || props.contact?.type === 'group')

const getName = computed(() => {
  if (!props.contact) return 'Unknown'
  if (isGroup.value) return props.contact.name || props.contact.groupName || '未知群组'
  return props.contact.friendName || props.contact.nickname || props.contact.name || '未知用户'
})

const getAvatar = computed(() => {
  if (!props.contact) return ''
  const src = isGroup.value 
    ? (props.contact.avatar || props.contact.groupAvatar)
    : (props.contact.headImg || props.contact.avatar)
  return addTokenToUrl(src)
})

const getPosition = computed(() => props.contact?.position || props.contact?.job || '职位未知')
const getDepartment = computed(() => props.contact?.dept || props.contact?.department || '')
const getPhone = computed(() => props.contact?.phone || props.contact?.mobile || '-')
const getEmail = computed(() => props.contact?.email || '-')
const getSignature = computed(() => props.contact?.signature || props.contact?.sign || '这个人很懒，什么都没留下～')

// Methods
const checkFavoriteStatus = async () => {
  if (!props.contact?.id || isGroup.value) return
  try {
    const res = await isFavorited(props.contact.id)
    isFavorite.value = res.code === 200 && res.data === true
  } catch (err) {
    isFavorite.value = false
  }
}

watch(() => props.contact?.id, checkFavoriteStatus, { immediate: true })

const startChat = () => emit('message', props.contact)
const handleGroupConfig = () => {
  if (isGroup.value && props.contact?.id) {
    showGroupSettings.value = true
  }
}

const toggleFavorite = async () => {
  if (!props.contact?.id) return
  try {
    if (isFavorite.value) {
      await removeFavorite(props.contact.id)
      isFavorite.value = false
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite({ messageId: props.contact.id, remark: '联系人收藏' })
      isFavorite.value = true
      ElMessage.success('已添加到收藏')
    }
  } catch (err) {
    ElMessage.error('操作失败')
  }
}

const copyText = async (text) => {
  copyToClipboard(text, { successMsg: '复制成功', emptyValues: ['-'] })
}

const handleEditRemark = () => {
  ElMessageBox.prompt('请输入新的备注名', '修改备注', {
    inputValue: props.contact.friendName,
    inputPlaceholder: '请输入备注',
    confirmButtonText: '保存',
    cancelButtonText: '取消'
  }).then(async ({ value }) => {
    await updateContactRemark(props.contact.id, { remark: value })
    ElMessage.success('修改成功')
    emit('update')
  }).catch(() => {})
}

const handleDeleteContact = () => {
  ElMessageBox.confirm('确定要删除该好友吗？此操作无法撤销。', '删除警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonClass: 'el-button--danger'
  }).then(async () => {
    await deleteContact(props.contact.id)
    ElMessage.success('删除成功')
    emit('update')
  }).catch(() => {})
}
</script>

<style scoped lang="scss">
.contact-detail-container {
  height: 100%;
  background-color: var(--el-bg-color-page);
  position: relative;
  overflow: hidden;

  &.is-empty {
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.detail-content {
  height: 100%;
  overflow-y: auto;
  padding-bottom: 40px;
}

.profile-header {
  position: relative;
  padding-top: 60px;
  margin-bottom: 24px;
  background-color: var(--el-bg-color);
  
  .header-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 160px;
    background-size: cover;
    background-position: center;
    filter: blur(20px);
    opacity: 0.3;
    z-index: 0;
    
    &::after {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: linear-gradient(to bottom, transparent, var(--el-bg-color));
    }
  }

  .header-content {
    position: relative;
    z-index: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 0 24px;
    
    .avatar-wrapper {
      position: relative;
      margin-bottom: 16px;
      
      .main-avatar {
        border: 4px solid var(--el-bg-color);
        box-shadow: var(--dt-shadow-dialog);
        background: var(--el-color-primary-light-9);
        color: var(--el-color-primary);
        font-size: 48px;
        font-weight: 600;
      }
      
      .status-dot {
        position: absolute;
        bottom: 8px;
        right: 8px;
        width: 20px;
        height: 20px;
        border-radius: 50%;
        background-color: var(--el-color-info);
        border: 3px solid var(--el-bg-color);
        
        &.online {
          background-color: var(--el-color-success);
          box-shadow: 0 0 0 2px rgba(var(--el-color-success-rgb), 0.2);
        }
      }
    }
    
    .user-identity {
      .display-name {
        margin: 0 0 8px;
        font-size: 24px;
        font-weight: 700;
        color: var(--el-text-color-primary);
      }
      
      .signature {
        margin: 0 0 16px;
        color: var(--el-text-color-secondary);
        font-size: 14px;
        max-width: 480px;
        line-height: 1.5;
      }
      
      .tags-container {
        display: flex;
        gap: 8px;
        justify-content: center;
      }
    }
  }
}

.quick-actions {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-bottom: 32px;
  padding: 0 20px;
  flex-wrap: wrap;
  
  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    group: true;
    
    .icon-box {
      width: 48px;
      height: 48px;
      border-radius: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      background-color: var(--el-fill-color);
      color: var(--el-text-color-regular);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      
      &.primary { &:hover { background-color: var(--el-color-primary); color: #fff; } }
      &.success { &:hover { background-color: var(--el-color-success); color: #fff; } }
      &.warning { &:hover { background-color: var(--el-color-warning); color: #fff; } }
      &.info { &:hover { background-color: var(--el-color-info); color: #fff; } }
      
      &.star.active {
        background-color: var(--el-color-warning-light-9);
        color: var(--el-color-warning);
      }
    }
    
    span {
      font-size: 12px;
      color: var(--el-text-color-secondary);
      transition: color 0.2s;
    }
    
    &:hover span {
      color: var(--el-text-color-primary);
    }
  }
}

.info-sections {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-card {
  background-color: var(--el-bg-color);
  border-radius: 16px;
  padding: 20px;
  box-shadow: var(--dt-shadow-card);
  
  .card-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
    color: var(--el-text-color-primary);
    display: flex;
    align-items: center;
    
    &::before {
      content: '';
      display: block;
      width: 4px;
      height: 16px;
      background-color: var(--el-color-primary);
      border-radius: 2px;
      margin-right: 8px;
    }
  }
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  
  .info-item {
    display: flex;
    align-items: flex-start;
    font-size: 14px;
    
    .label {
      width: 100px;
      color: var(--el-text-color-secondary);
      display: flex;
      align-items: center;
      gap: 6px;
      flex-shrink: 0;
    }
    
    .value {
      flex: 1;
      color: var(--el-text-color-regular);
      line-height: 1.5;
      word-break: break-all;
      
      &.announcement {
        white-space: pre-wrap;
        background-color: var(--el-fill-color-light);
        padding: 8px 12px;
        border-radius: 8px;
      }
    }
    
    .copy-btn {
      margin-left: 8px;
      padding: 0;
      height: auto;
    }
  }
}

.action-list {
  .list-action-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    cursor: pointer;
    border-bottom: 1px solid var(--el-border-color-lighter);
    transition: background-color 0.2s;
    
    &:last-child {
      border-bottom: none;
    }
    
    .left {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 14px;
      color: var(--el-text-color-regular);
    }
    
    .arrow {
      color: var(--el-text-color-placeholder);
      font-size: 16px;
    }
    
    &.danger {
      .left { color: var(--el-color-danger); }
    }
    
    &:hover {
      .left { opacity: 0.8; }
    }
  }
}

.empty-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--el-text-color-placeholder);
  
  .empty-illustration {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    background-color: var(--el-fill-color);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 48px;
    color: var(--el-text-color-placeholder);
    margin: 0 auto;
  }
}

.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

// Custom Scrollbar
.custom-scrollbar {
  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  &::-webkit-scrollbar-thumb {
    background-color: rgba(0, 0, 0, 0.1);
    border-radius: 3px;
    
    &:hover {
      background-color: rgba(0, 0, 0, 0.2);
    }
  }
}
</style>
