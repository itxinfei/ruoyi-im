<template>
  <el-dialog
    v-model="visible"
    :title="null"
    width="600px"
    :modal="true"
    :close-on-click-modal="true"
    :close-on-press-escape="true"
    destroy-on-close
    append-to-body
    class="contact-detail-modal"
  >
    <!-- 关闭按钮 -->
    <template #header="{ close }">
      <div class="modal-header">
        <button class="close-btn" @click="close">
          <el-icon><Close /></el-icon>
        </button>
      </div>
    </template>

    <div v-if="contactInfo" v-loading="loading" class="contact-detail-content">
      <!-- 联系人信息卡片 -->
      <div class="contact-info-card">
        <DingtalkAvatar
          :src="contactInfo.avatar"
          :name="contactInfo.name"
          :size="80"
          shape="circle"
          custom-class="contact-avatar"
        />
        <h3 class="contact-name">{{ contactInfo.name }}</h3>
        <div class="contact-meta" v-if="contactInfo.remark">
          <el-icon><Edit /></el-icon>
          <span class="remark">{{ contactInfo.remark }}</span>
        </div>
        <div class="contact-dept" v-if="contactInfo.dept">
          <span>{{ contactInfo.dept }}</span>
          <span v-if="contactInfo.position"> · {{ contactInfo.position }}</span>
        </div>
      </div>

      <!-- 快捷操作按钮 -->
      <div class="quick-actions">
        <el-button class="action-btn primary-btn" @click="handleStartChat">
          <el-icon><ChatDotRound /></el-icon>
          <span>发消息</span>
        </el-button>
        <el-button class="action-btn" @click="handleVideoCall">
          <el-icon><VideoCamera /></el-icon>
          <span>视频通话</span>
        </el-button>
        <el-button class="action-btn" @click="handleVoiceCall">
          <el-icon><Phone /></el-icon>
          <span>语音通话</span>
        </el-button>
        <el-button class="action-btn" @click="handleEditRemark">
          <el-icon><Edit /></el-icon>
          <span>设置备注</span>
        </el-button>
      </div>

      <!-- 备注区块 -->
      <div class="section-block" v-if="contactInfo.remark || canEditRemark">
        <div class="section-header">
          <el-icon><Edit /></el-icon>
          <span class="section-title">备注信息</span>
          <el-button
            v-if="canEditRemark"
            text
            type="primary"
            size="small"
            @click="handleEditRemark"
          >
            编辑
          </el-button>
        </div>
        <div class="remark-content" :class="{ empty: !contactInfo.remark }">
          {{ contactInfo.remark || '暂无备注信息，点击编辑按钮添加备注' }}
        </div>
      </div>

      <!-- 共同群组区块 -->
      <div class="section-block" v-if="commonGroups.length > 0">
        <div class="section-header">
          <el-icon><UserFilled /></el-icon>
          <span class="section-title">共同群组 ({{ commonGroups.length }})</span>
        </div>
        <div class="groups-list">
          <div
            v-for="group in commonGroups.slice(0, 5)"
            :key="group.id"
            class="group-item"
            @click="handleGroupClick(group)"
          >
            <DingtalkAvatar
              :src="group.avatar"
              :name="group.name"
              :size="40"
              shape="square"
              custom-class="group-avatar-small"
            />
            <div class="group-info">
              <span class="group-name">{{ group.name }}</span>
              <span class="group-member-count">{{ group.memberCount || 0 }} 人</span>
            </div>
          </div>
        </div>
        <el-button
          v-if="commonGroups.length > 5"
          text
          type="primary"
          size="small"
          class="view-more-btn"
          @click="handleViewAllGroups"
        >
          查看全部 {{ commonGroups.length }} 个群组
        </el-button>
      </div>

      <!-- 共享文件区块 -->
      <div class="section-block">
        <div class="section-header">
          <el-icon><Folder /></el-icon>
          <span class="section-title">共享文件</span>
          <el-button text type="primary" size="small" @click="handleViewFiles">
            查看全部
          </el-button>
        </div>
        <div class="files-preview">
          <div v-if="sharedFiles.length === 0" class="empty-state">
            <el-icon><FolderOpened /></el-icon>
            <span>暂无共享文件</span>
          </div>
          <div v-else class="file-list">
            <div
              v-for="file in sharedFiles.slice(0, 4)"
              :key="file.id"
              class="file-item"
              @click="handleFileClick(file)"
            >
              <div class="file-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="file-info">
                <span class="file-name">{{ file.name }}</span>
                <span class="file-meta">
                  {{ formatDate(file.sendTime) }} · {{ formatFileSize(file.size) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 更多操作 -->
      <div class="more-actions">
        <div class="action-group">
          <div class="action-item" @click="handleViewHistory">
            <el-icon><Clock /></el-icon>
            <span>查看历史消息</span>
          </div>
          <div class="action-item" @click="handleAddToTags">
            <el-icon><PriceTag /></el-icon>
            <span>标签管理</span>
          </div>
          <div class="action-item" @click="handleSetBackground">
            <el-icon><Picture /></el-icon>
            <span>设置聊天背景</span>
          </div>
        </div>
      </div>

      <!-- 底部危险操作 -->
      <div class="footer-actions" v-if="!isSpecialContact">
        <el-button
          type="danger"
          plain
          class="footer-btn danger-btn"
          @click="handleDeleteContact"
        >
          删除联系人
        </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Close, Edit, ChatDotRound, VideoCamera, Phone, UserFilled,
  Folder, FolderOpened, Document, Clock, PriceTag, Picture
} from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { getCommonGroups } from '@/api/im/group'
import { updateFriend, deleteFriend } from '@/api/im/contact'
import dayjs from 'dayjs'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  userId: { type: [Number, String], default: null },
  contactInfo: { type: Object, default: null }
})

const emit = defineEmits(['update:modelValue', 'start-chat', 'video-call', 'voice-call', 'refresh', 'delete-contact'])

const router = useRouter()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const commonGroups = ref([])
const sharedFiles = ref([])

// 联系人信息（从 props 或默认值）
const contactInfo = computed(() => {
  return props.contactInfo || {
    id: props.userId,
    name: '未知用户',
    avatar: '',
    remark: '',
    dept: '',
    position: ''
  }
})

// 是否可以编辑备注
const canEditRemark = computed(() => props.userId && props.userId !== 'special')

// 是否是特殊联系人（系统通知等）
const isSpecialContact = computed(() => !props.userId || props.userId === 'special')

// 加载共同群组
const loadCommonGroups = async () => {
  if (!props.userId || props.userId === 'special') return
  try {
    const res = await getCommonGroups(props.userId)
    if (res.data) {
      commonGroups.value = res.data.map(g => ({
        id: g.id,
        name: g.name,
        avatar: g.avatar,
        memberCount: g.memberCount || 0
      }))
    }
  } catch (e) {
    // 静默失败
  }
}

// 监听弹窗打开
watch(() => props.modelValue, (val) => {
  if (val) {
    loadCommonGroups()
    // TODO: 加载共享文件
    sharedFiles.value = []
  }
})

// 发起聊天
const handleStartChat = () => {
  emit('start-chat', {
    type: 'PRIVATE',
    targetId: props.userId,
    peerName: contactInfo.value.name,
    peerAvatar: contactInfo.value.avatar
  })
  visible.value = false
}

// 视频通话
const handleVideoCall = () => {
  emit('video-call', {
    userId: props.userId,
    userName: contactInfo.value.name
  })
}

// 语音通话
const handleVoiceCall = () => {
  emit('voice-call', {
    userId: props.userId,
    userName: contactInfo.value.name
  })
}

// 编辑备注
const handleEditRemark = async () => {
  const { value } = await ElMessageBox.prompt('设置备注名', '编辑备注', {
    confirmButtonText: '保存',
    cancelButtonText: '取消',
    inputValue: contactInfo.value.remark || '',
    inputPlaceholder: '请输入备注名称'
  })
  if (value !== null) {
    try {
      await updateFriend(props.userId, { remark: value })
      if (contactInfo.value) {
        contactInfo.value.remark = value
      }
      ElMessage.success('备注已更新')
      emit('refresh')
    } catch (e) {
      ElMessage.error('更新失败，请重试')
    }
  }
}

// 群组点击
const handleGroupClick = (group) => {
  // 跳转到群聊
  emit('start-chat', {
    type: 'GROUP',
    targetId: group.id,
    peerName: group.name,
    peerAvatar: group.avatar
  })
  visible.value = false
}

// 查看全部群组
const handleViewAllGroups = () => {
  // TODO: 打开共同群组列表
  ElMessage.info('共同群组列表功能开发中')
}

// 查看文件
const handleViewFiles = () => {
  // TODO: 打开共享文件列表
  ElMessage.info('共享文件列表功能开发中')
}

// 文件点击
const handleFileClick = (file) => {
  // TODO: 文件预览
  ElMessage.info('文件预览功能开发中')
}

// 查看历史消息
const handleViewHistory = () => {
  visible.value = false
  // 跳转到聊天历史面板
  router.push({ name: 'ChatHistory', query: { userId: props.userId } })
}

// 标签管理
const handleAddToTags = () => {
  // TODO: 打开标签管理
  ElMessage.info('标签管理功能开发中')
}

// 设置聊天背景
const handleSetBackground = () => {
  // TODO: 打开背景设置
  ElMessage.info('聊天背景设置功能开发中')
}

// 删除联系人
const handleDeleteContact = () => {
  ElMessageBox.confirm(`确定要删除联系人「${contactInfo.value.name}」吗？`, '删除联系人', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await deleteFriend(props.userId)
      ElMessage.success('已删除联系人')
      emit('delete-contact', props.userId)
      emit('refresh')
      visible.value = false
    } catch (e) {
      ElMessage.error('删除失败，请重试')
    }
  }).catch(() => {})
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD')
}
</script>

<style scoped lang="scss">
// 钉钉颜色变量
$dt-blue: #0089FF;
$dt-text-primary: #1F2329;
$dt-text-secondary: #646A73;
$dt-text-tertiary: #8F959E;
$dt-bg-hover: #F5F5F5;
$dt-border-color: #E5E6EB;
$dt-danger: #FF4D4F;

.contact-detail-modal {
  :deep(.el-dialog) {
    border-radius: 12px;
    overflow: hidden;
  }

  :deep(.el-dialog__header) {
    padding: 0;
  }

  :deep(.el-dialog__body) {
    padding: 0;
    max-height: 80vh;
    overflow-y: auto;
  }

  :deep(.el-dialog__footer) {
    display: none;
  }

  // 自定义滚动条
  :deep(.el-dialog__body)::-webkit-scrollbar {
    width: 6px;
  }

  :deep(.el-dialog__body)::-webkit-scrollbar-thumb {
    background: #C0C0C0;
    border-radius: 3px;

    &:hover {
      background: #A0A0A0;
    }
  }
}

.modal-header {
  padding: 16px 16px 0;
  text-align: right;

  .close-btn {
    width: 32px;
    height: 32px;
    border: none;
    background: transparent;
    border-radius: 6px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    color: $dt-text-secondary;
    transition: all 0.15s;

    &:hover {
      background: $dt-bg-hover;
      color: $dt-text-primary;
    }

    .el-icon {
      font-size: 20px;
    }
  }
}

.contact-detail-content {
  padding: 0 24px 24px;
}

// 联系人信息卡片
.contact-info-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 0;
  text-align: center;
  border-bottom: 1px solid $dt-border-color;

  :deep(.contact-avatar) {
    border-radius: 50%;
  }

  .contact-name {
    margin: 16px 0 8px;
    font-size: 20px;
    font-weight: 600;
    color: $dt-text-primary;
  }

  .contact-meta {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: $dt-text-tertiary;
    margin-bottom: 4px;

    .remark {
      color: $dt-text-secondary;
    }
  }

  .contact-dept {
    font-size: 13px;
    color: $dt-text-tertiary;
  }
}

// 快捷操作按钮
.quick-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid $dt-border-color;
  flex-wrap: wrap;

  .action-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    border: 1px solid $dt-border-color;
    border-radius: 6px;
    background: #fff;
    color: $dt-text-primary;
    font-size: 14px;
    transition: all 0.15s;

    &:hover {
      border-color: $dt-blue;
      color: $dt-blue;
      background: rgba(0, 137, 255, 0.05);
    }

    &.primary-btn {
      background: $dt-blue;
      border-color: $dt-blue;
      color: #fff;

      &:hover {
        background: #0070D2;
        border-color: #0070D2;
        color: #fff;
      }
    }

    .el-icon {
      font-size: 16px;
    }
  }
}

// 区块样式
.section-block {
  padding: 20px 0;
  border-bottom: 1px solid $dt-border-color;

  &:last-of-type {
    border-bottom: none;
  }

  .section-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 16px;

    .el-icon {
      font-size: 18px;
      color: $dt-text-tertiary;
    }

    .section-title {
      flex: 1;
      font-size: 16px;
      font-weight: 600;
      color: $dt-text-primary;
    }
  }

  .view-more-btn {
    margin-top: 8px;
    width: 100%;
  }
}

// 备注内容
.remark-content {
  padding: 12px 16px;
  background: #F8FAFC;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.6;
  color: $dt-text-secondary;
  white-space: pre-wrap;

  &.empty {
    color: $dt-text-tertiary;
    font-style: italic;
  }
}

// 共同群组列表
.groups-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.group-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.15s;

  &:hover {
    background: $dt-bg-hover;
  }

  :deep(.group-avatar-small) {
    border-radius: 6px;
  }

  .group-info {
    display: flex;
    flex-direction: column;
    gap: 2px;

    .group-name {
      font-size: 14px;
      color: $dt-text-primary;
    }

    .group-member-count {
      font-size: 12px;
      color: $dt-text-tertiary;
    }
  }
}

// 文件预览
.files-preview {
  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 24px;
    color: $dt-text-tertiary;

    .el-icon {
      font-size: 40px;
    }
  }

  .file-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .file-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 12px;
    border-radius: 6px;
    cursor: pointer;
    transition: background 0.15s;

    &:hover {
      background: $dt-bg-hover;
    }

    .file-icon {
      width: 36px;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #F0F2F5;
      border-radius: 6px;
      color: $dt-text-secondary;

      .el-icon {
        font-size: 18px;
      }
    }

    .file-info {
      display: flex;
      flex-direction: column;
      gap: 2px;
      flex: 1;

      .file-name {
        font-size: 14px;
        color: $dt-text-primary;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .file-meta {
        font-size: 12px;
        color: $dt-text-tertiary;
      }
    }
  }
}

// 更多操作
.more-actions {
  padding: 8px 0;

  .action-group {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }

  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    padding: 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: background 0.15s;

    &:hover {
      background: $dt-bg-hover;
    }

    .el-icon {
      font-size: 20px;
      color: $dt-text-secondary;
    }

    span {
      font-size: 12px;
      color: $dt-text-primary;
    }
  }
}

// 底部操作
.footer-actions {
  padding: 16px 0 0;
  text-align: center;

  .footer-btn {
    width: 100%;
    height: 40px;
    font-size: 14px;

    &.danger-btn {
      border-color: $dt-danger;
      color: $dt-danger;

      &:hover {
        background: rgba(255, 77, 79, 0.1);
      }
    }
  }
}

// 暗色模式适配
:global(.dark) {
  .contact-detail-modal {
    :deep(.el-dialog) {
      background: #1e293b;
    }
  }

  .contact-name {
    color: #f1f5f9;
  }

  .contact-meta,
  .contact-dept {
    color: #94a3b8;
  }

  .section-title {
    color: #e2e8f0;
  }

  .action-item span {
    color: #e2e8f0;
  }

  .action-btn {
    background: #334155;
    border-color: #475569;
    color: #e2e8f0;

    &:hover {
      background: rgba(0, 137, 255, 0.1);
      border-color: $dt-blue;
    }
  }

  .remark-content {
    background: rgba(30, 41, 59, 0.5);
  }
}
</style>
