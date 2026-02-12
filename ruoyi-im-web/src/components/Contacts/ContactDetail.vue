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
        <!-- 钉钉风格：紧凑的头部信息卡片 -->
        <div class="profile-header">
          <div class="header-content">
            <div class="avatar-wrapper">
              <el-avatar
                :size="64"
                :src="getAvatar"
                :shape="isGroup ? 'square' : 'circle'"
                class="main-avatar"
              >
                {{ getName.charAt(0) }}
              </el-avatar>
              <!-- 在线状态指示 -->
              <div
                v-if="!isGroup"
                class="status-indicator"
                :class="{ online: contact.online }"
              >
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
                <el-button
                  v-if="getEmail !== '-'"
                  link
                  type="primary"
                  size="small"
                  class="copy-btn"
                  @click="copyText(getEmail)"
                >
                  复制
                </el-button>
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

              <div
                v-if="isGroup"
                class="info-item"
              >
                <div class="label">
                  <el-icon><Avatar /></el-icon>
                  群成员
                </div>
                <div class="value">
                  {{ contact.userCount || contact.memberCount || 0 }}人
                  <span
                    v-if="contact.onlineCount > 0"
                    class="online-count"
                  >
                    ({{ contact.onlineCount }}人在线)
                  </span>
                </div>
              </div>

              <div
                v-if="isGroup"
                class="info-item"
              >
                <div class="label">
                  <el-icon><InfoFilled /></el-icon>
                  群号
                </div>
                <div class="value">
                  {{ contact.id || contact.groupId || '-' }}
                  <el-button
                    v-if="contact.id || contact.groupId"
                    link
                    type="primary"
                    size="small"
                    class="copy-btn"
                    @click="copyText(String(contact.id || contact.groupId))"
                  >
                    复制
                  </el-button>
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
                class="list-action-item"
                :class="{ danger: !isBlocked }"
                @click="handleBlockToggle"
              >
                <div class="left">
                  <el-icon><component :is="isBlocked ? 'CircleCheck' : 'CircleClose'" /></el-icon>
                  <span>{{ isBlocked ? '解除拉黑' : '拉黑好友' }}</span>
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
  Iphone, Message, Suitcase, Edit, Delete, InfoFilled, User, Avatar,
  ArrowRight, CircleCheck, CircleClose
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { updateContactRemark, deleteContact, blockFriend } from '@/api/im/contact'
import { addFavorite, removeFavorite, isFavorited } from '@/api/im/favorite'
import { addTokenToUrl } from '@/utils/file'
import { copyToClipboard } from '@/utils/format'
import GroupProfileDialog from '@/components/Contacts/GroupProfileDialog.vue'

const props = defineProps({
  contact: Object
})

const emit = defineEmits(['update', 'voice-call', 'video-call', 'message', 'toggle-group-profile'])

const isFavorite = ref(false)
const isBlocked = ref(false)
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

const handleBlockToggle = async () => {
  if (!isFriend.value || !props.contact?.id) {return}

  const actionText = isBlocked.value ? '解除拉黑' : '拉黑'
  const confirmText = isBlocked.value
    ? '确定要解除对该好友的拉黑吗？'
    : '确定要拉黑该好友吗？拉黑后将不再接收其消息。'

  try {
    await ElMessageBox.confirm(confirmText, `${actionText}确认`, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: isBlocked.value ? 'info' : 'warning'
    })

    await blockFriend(props.contact.id, !isBlocked.value)
    isBlocked.value = !isBlocked.value
    ElMessage.success(isBlocked.value ? '已拉黑好友' : '已解除拉黑')
    emit('update')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
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
  padding-bottom: 40px;
}

// 钉钉风格：简洁扁平的头部
.profile-header {
  background-color: var(--dt-bg-card);
  padding: 28px 24px 20px;
  margin-bottom: 12px;

  .header-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;

    .avatar-wrapper {
      position: relative;
      margin-bottom: 12px;

      .main-avatar {
        border: 3px solid var(--dt-bg-card);
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
        background: var(--dt-bg-body);
      }

      .status-indicator {
        position: absolute;
        bottom: 2px;
        right: 2px;
        z-index: 2;

        .status-dot {
          width: 12px;
          height: 12px;
          border-radius: 50%;
          background-color: var(--dt-text-quaternary);
          border: 2px solid var(--dt-bg-card);
          transition: all 0.3s;
        }

        &.online .status-dot {
          background-color: var(--dt-success-color);
        }
      }
    }

    .user-identity {
      .display-name {
        margin: 0 0 4px;
        font-size: 20px;
        font-weight: 600;
        color: var(--dt-text-primary);
      }

      .signature {
        margin: 0 0 10px;
        color: var(--dt-text-tertiary);
        font-size: 13px;
        max-width: 400px;
        line-height: 1.5;
      }

      .tags-container {
        display: flex;
        gap: 6px;
        justify-content: center;
        margin-bottom: 4px;
      }
    }
  }
}

// 钉钉风格：紧凑的操作按钮行
.quick-actions {
  display: flex;
  justify-content: center;
  gap: 28px;
  margin-bottom: 20px;
  padding: 0 20px;

  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    cursor: pointer;
    transition: all 0.15s;

    .icon-box {
      width: 44px;
      height: 44px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      background-color: var(--dt-bg-hover);
      color: var(--dt-text-secondary);
      transition: all 0.2s ease;

      &.primary:hover { background-color: var(--dt-brand-color); color: #fff; }
      &.success:hover { background-color: #2E7D32; color: #fff; }
      &.warning:hover { background-color: #E65100; color: #fff; }
      &.info:hover { background-color: #7B1FA2; color: #fff; }

      &.star.active {
        background-color: #FFF8E1;
        color: #F57F17;
      }
    }

    span {
      font-size: 12px;
      color: var(--dt-text-tertiary);
    }

    &:hover span {
      color: var(--dt-text-primary);
    }
  }
}

// 信息区
.info-sections {
  max-width: 640px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section-card {
  background-color: var(--dt-bg-card);
  border-radius: 8px;
  padding: 16px 20px;
  border: 1px solid var(--dt-border-lighter);

  .card-title {
    font-size: 13px;
    font-weight: 600;
    margin-bottom: 14px;
    color: var(--dt-text-primary);
    display: flex;
    align-items: center;

    &::before {
      content: '';
      display: block;
      width: 3px;
      height: 12px;
      background-color: var(--dt-brand-color);
      border-radius: 2px;
      margin-right: 8px;
    }
  }
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .info-item {
    display: flex;
    align-items: center;
    font-size: 13px;

    .label {
      width: 100px;
      color: var(--dt-text-tertiary);
      display: flex;
      align-items: center;
      gap: 6px;
      flex-shrink: 0;

      .el-icon { font-size: 14px; }
    }

    .value {
      flex: 1;
      color: var(--dt-text-primary);
      font-weight: 500;

      &.announcement {
        white-space: pre-wrap;
        background-color: var(--dt-bg-hover);
        padding: 10px 14px;
        border-radius: 6px;
        font-weight: normal;
        font-size: 13px;
        color: var(--dt-text-secondary);
      }

      .online-count {
        color: var(--dt-success-color);
        font-size: 12px;
        margin-left: 8px;
      }
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
    border-bottom: 1px solid var(--dt-border-lighter);
    transition: all 0.15s;

    &:last-child {
      border-bottom: none;
    }

    .left {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 13px;
      color: var(--dt-text-primary);
      font-weight: 500;
    }

    .arrow {
      color: var(--dt-text-quaternary);
      font-size: 12px;
      transition: transform 0.15s;
    }

    &.danger {
      .left { color: var(--dt-error-color); }
    }

    &:hover {
      background: var(--dt-bg-hover);
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
    width: 100px;
    height: 100px;
    border-radius: 50%;
    background-color: var(--dt-bg-hover);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 40px;
    color: var(--dt-text-quaternary);
    margin: 0 auto 16px;
  }
}

// Transitions
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.25s ease;
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

// Scrollbar
.custom-scrollbar {
  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  &::-webkit-scrollbar-thumb {
    background-color: var(--dt-scrollbar-thumb);
    border-radius: 3px;

    &:hover {
      background-color: var(--dt-scrollbar-thumb-hover);
    }
  }
}
</style>
