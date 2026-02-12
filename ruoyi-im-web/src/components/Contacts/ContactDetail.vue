<template>
  <div
    class="contact-detail-container"
    :class="{ 'is-empty': !contact }"
  >
    <Transition
      name="slide-fade"
      mode="out-in"
    >
      <div
        v-if="contact"
        :key="contact.id"
        class="detail-content custom-scrollbar"
      >
        <!-- Header with Blur Background -->
        <div class="profile-header">
          <div
            class="header-bg"
            :style="{ backgroundImage: `url(${getAvatar})` }"
          >
            <div class="header-overlay" />
          </div>
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
              <!-- 在线状态指示 - 增强版带呼吸灯效果 -->
              <div
                v-if="!isGroup"
                class="status-indicator"
                :class="{ online: contact.online }"
              >
                <div class="status-pulse" />
                <div class="status-dot" />
              </div>
            </div>
            
            <div class="user-identity">
              <h2 class="display-name">
                {{ getName }}
              </h2>
              <p class="signature">
                {{ getSignature }}
              </p>
              
              <div
                v-if="!isGroup"
                class="tags-container"
              >
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
          <div
            class="action-item"
            @click="startChat"
          >
            <div class="icon-box primary">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <span>发消息</span>
          </div>

          <template v-if="!isGroup">
            <div
              class="action-item"
              @click="$emit('voice-call', contact)"
            >
              <div class="icon-box success">
                <el-icon><Phone /></el-icon>
              </div>
              <span>语音通话</span>
            </div>
            
            <div
              class="action-item"
              @click="$emit('video-call', contact)"
            >
              <div class="icon-box warning">
                <el-icon><VideoCamera /></el-icon>
              </div>
              <span>视频通话</span>
            </div>

            <div
              v-if="isFriend"
              class="action-item"
              @click="toggleFavorite"
            >
              <div
                class="icon-box star"
                :class="{ active: isFavorite }"
              >
                <el-icon><component :is="isFavorite ? StarFilled : Star" /></el-icon>
              </div>
              <span>{{ isFavorite ? '已收藏' : '收藏' }}</span>
            </div>
          </template>

          <template v-else>
            <div
              class="action-item"
              @click="handleGroupConfig"
            >
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
            <div class="card-title">
              基本信息
            </div>
            
            <div class="info-list">
              <div
                v-if="!isGroup"
                class="info-item"
              >
                <div class="label">
                  <el-icon><Iphone /></el-icon>
                  手机号码
                </div>
                <div class="value">
                  {{ getPhone }}
                </div>
                <el-button
                  v-if="getPhone !== '-'"
                  link
                  type="primary"
                  size="small"
                  class="copy-btn"
                  @click="copyText(getPhone)"
                >
                  复制
                </el-button>
              </div>

              <div
                v-if="!isGroup"
                class="info-item"
              >
                <div class="label">
                  <el-icon><Message /></el-icon>
                  电子邮箱
                </div>
                <div class="value">
                  {{ getEmail }}
                </div>
              </div>

              <div
                v-if="!isGroup"
                class="info-item"
              >
                <div class="label">
                  <el-icon><Suitcase /></el-icon>
                  职位信息
                </div>
                <div class="value">
                  {{ getPosition }}
                </div>
              </div>

              <div
                v-if="isGroup"
                class="info-item"
              >
                <div class="label">
                  <el-icon><InfoFilled /></el-icon>
                  群公告
                </div>
                <div class="value announcement">
                  {{ contact.notice || '暂无公告' }}
                </div>
              </div>
              
              <div
                v-if="isGroup"
                class="info-item"
              >
                <div class="label">
                  <el-icon><User /></el-icon>
                  群主
                </div>
                <div class="value">
                  {{ contact.ownerName || '-' }}
                </div>
              </div>
            </div>
          </div>

          <div
            v-if="isFriend"
            class="section-card"
          >
            <div class="card-title">
              更多操作
            </div>
            <div class="action-list">
              <div
                class="list-action-item"
                @click="handleEditRemark"
              >
                <div class="left">
                  <el-icon><Edit /></el-icon>
                  <span>修改备注</span>
                </div>
                <el-icon class="arrow">
                  <ArrowRight />
                </el-icon>
              </div>
              
              <div
                class="list-action-item danger"
                @click="handleDeleteContact"
              >
                <div class="left">
                  <el-icon><Delete /></el-icon>
                  <span>删除好友</span>
                </div>
                <el-icon class="arrow">
                  <ArrowRight />
                </el-icon>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div
        v-else
        class="empty-placeholder"
      >
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

const emit = defineEmits(['update', 'voice-call', 'video-call', 'message', 'toggle-group-profile'])

const isFavorite = ref(false)
const showGroupSettings = ref(false)

// Computed Properties
const isGroup = computed(() => props.contact?.isGroup || props.contact?.type === 'group')
const isFriend = computed(() => {
  if (isGroup.value) { return false }
  return props.contact?.friendId !== undefined && props.contact?.friendId !== null
})

const getName = computed(() => {
  if (!props.contact) {return 'Unknown'}
  if (isGroup.value) {return props.contact.name || props.contact.groupName || '未知群组'}
  return props.contact.friendName || props.contact.nickname || props.contact.name || '未知用户'
})

const getAvatar = computed(() => {
  if (!props.contact) {return ''}
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
  if (!isFriend.value || !props.contact?.id) {return}
  try {
    const res = await isFavorited(props.contact.id)
    isFavorite.value = res.code === 200 && res.data === true
  } catch (err) {
    isFavorite.value = false
  }
}

watch([() => props.contact?.id, isFriend], checkFavoriteStatus, { immediate: true })
watch(showGroupSettings, value => {
  emit('toggle-group-profile', value)
})

const startChat = () => emit('message', props.contact)
const handleGroupConfig = () => {
  if (isGroup.value && props.contact?.id) {
    showGroupSettings.value = true
  }
}

const toggleFavorite = async () => {
  if (!isFriend.value || !props.contact?.id) {return}
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

const copyText = async text => {
  copyToClipboard(text, { successMsg: '复制成功', emptyValues: ['-'] })
}

const handleEditRemark = () => {
  if (!isFriend.value || !props.contact?.id) {return}
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
  if (!isFriend.value || !props.contact?.id) {return}
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
@use '@/styles/design-tokens.scss' as *;

.contact-detail-container {
  height: 100%;
  background-color: var(--dt-bg-page);
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
  padding-bottom: var(--dt-spacing-5xl);
}

.profile-header {
  position: relative;
  padding-top: var(--dt-spacing-5xl);
  margin-bottom: var(--dt-spacing-xl);
  background-color: var(--dt-bg-card);
  overflow: hidden;

  .header-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 240px;
    background-size: cover;
    background-position: center;
    filter: blur(40px) saturate(1.4);
    opacity: 0.15; /* Subtler blur background */
    z-index: 0;

    .header-overlay {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: linear-gradient(
        to bottom,
        rgba(255, 255, 255, 0) 0%,
        var(--dt-bg-card) 100%
      );
    }
  }

  .header-content {
    position: relative;
    z-index: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 0 var(--dt-spacing-2xl);

    .avatar-wrapper {
      position: relative;
      margin-bottom: var(--dt-spacing-xl);

      .main-avatar {
        border: 4px solid #fff;
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
        background: var(--dt-bg-body);
        transition: transform 0.3s ease;
        
        &:hover {
          transform: scale(1.02);
        }
      }

      .status-indicator {
        position: absolute;
        bottom: 8px;
        right: 8px;
        width: 24px;
        height: 24px;
        z-index: 2;

        .status-dot {
          position: absolute;
          bottom: 2px;
          right: 2px;
          width: 18px;
          height: 18px;
          border-radius: 50%;
          background-color: var(--dt-text-quaternary);
          border: 3px solid #fff;
          transition: all 0.3s;
        }

        &.online .status-dot {
          background-color: var(--dt-success-color);
          box-shadow: 0 0 10px rgba(0, 200, 83, 0.4);
        }
      }
    }

    .user-identity {
      .display-name {
        margin: 0 0 8px;
        font-size: 26px;
        font-weight: 600;
        color: var(--dt-text-primary);
        letter-spacing: -0.5px;
      }

      .signature {
        margin: 0 0 16px;
        color: var(--dt-text-tertiary);
        font-size: 14px;
        max-width: 500px;
        line-height: 1.6;
      }

      .tags-container {
        display: flex;
        gap: 8px;
        justify-content: center;
        margin-bottom: 8px;
      }
    }
  }
}

.quick-actions {
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-bottom: 40px;
  padding: 0 24px;

  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    transition: all 0.2s;

    .icon-box {
      width: 52px;
      height: 52px;
      border-radius: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 26px;
      background-color: #f5f6f7;
      color: #646a73;
      transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);

      &.primary:hover { background-color: var(--dt-brand-color); color: #fff; box-shadow: 0 6px 16px rgba(50, 150, 250, 0.3); }
      &.success:hover { background-color: #00CC70; color: #fff; box-shadow: 0 6px 16px rgba(0, 204, 112, 0.3); }
      &.warning:hover { background-color: #FF943F; color: #fff; box-shadow: 0 6px 16px rgba(255, 148, 63, 0.3); }
      &.info:hover { background-color: #722ED1; color: #fff; box-shadow: 0 6px 16px rgba(114, 46, 209, 0.3); }

      &.star.active {
        background-color: #FFF7E6;
        color: #FA8C16;
        border: 1px solid #FFD591;
      }
    }

    span {
      font-size: 13px;
      color: var(--dt-text-secondary);
      font-weight: 500;
    }

    &:hover span {
      color: var(--dt-text-primary);
    }
  }
}

.info-sections {
  max-width: 720px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section-card {
  background-color: #fff;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid var(--dt-border-lighter);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.02);

  .card-title {
    font-size: 15px;
    font-weight: 600;
    margin-bottom: 20px;
    color: var(--dt-text-primary);
    display: flex;
    align-items: center;
    opacity: 0.85;

    &::before {
      content: '';
      display: block;
      width: 3px;
      height: 14px;
      background-color: var(--dt-brand-color);
      border-radius: 2px;
      margin-right: 10px;
    }
  }
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 16px;

  .info-item {
    display: flex;
    align-items: center;
    font-size: 14px;

    .label {
      width: 120px;
      color: var(--dt-text-tertiary);
      display: flex;
      align-items: center;
      gap: 8px;
      flex-shrink: 0;
      
      .el-icon { font-size: 16px; }
    }

    .value {
      flex: 1;
      color: var(--dt-text-primary);
      font-weight: 500;
      
      &.announcement {
        white-space: pre-wrap;
        background-color: #f7f8f9;
        padding: 12px 16px;
        border-radius: 8px;
        font-weight: normal;
        color: var(--dt-text-secondary);
      }
    }
  }
}

.action-list {
  .list-action-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 0;
    cursor: pointer;
    border-bottom: 1px solid var(--dt-border-lighter);
    transition: all 0.2s;

    &:last-child {
      border-bottom: none;
    }

    .left {
      display: flex;
      align-items: center;
      gap: 12px;
      font-size: 14px;
      color: var(--dt-text-primary);
      font-weight: 500;
    }

    .arrow {
      color: var(--dt-text-quaternary);
      font-size: 14px;
      transition: transform 0.2s;
    }

    &.danger {
      .left { color: var(--dt-error-color); }
    }

    &:hover {
      background: #fcfdfe;
      .arrow { transform: translateX(2px); }
    }
  }
}

.empty-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  
  .empty-illustration {
    width: 140px;
    height: 140px;
    border-radius: 50%;
    background-color: #f5f6f7;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 56px;
    color: var(--dt-text-quaternary);
    margin: 0 auto 20px;
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
    background-color: var(--dt-scrollbar-thumb);
    border-radius: var(--dt-radius-sm);

    &:hover {
      background-color: var(--dt-scrollbar-thumb-hover);
    }
  }
}

// Breathing pulse animation
@keyframes pulse-ring {
  0% {
    transform: scale(1);
    opacity: 0.6;
  }
  100% {
    transform: scale(2);
    opacity: 0;
  }
}
</style>
