<template>
  <el-dialog
    v-model="visible"
    title="群公告"
    width="600px"
    :close-on-click-modal="true"
    @close="handleClose"
  >
    <!-- 公告列表 -->
    <div class="announcement-list">
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else-if="announcements.length === 0" class="empty-state">
        <el-empty description="暂无群公告">
          <el-button v-if="canManage" type="primary" @click="showCreateDialog = true">
            发布公告
          </el-button>
        </el-empty>
      </div>

      <div v-else class="announcement-items">
        <div
          v-for="item in announcements"
          :key="item.id"
          class="announcement-item"
          :class="{ unread: !item.isRead, pinned: item.isPinned }"
        >
          <div class="announcement-header">
            <div class="header-left">
              <span v-if="item.isPinned" class="pinned-badge">
                <span class="material-icons-outlined">push_pin</span>
                置顶
              </span>
              <span class="announcement-title">{{ item.title }}</span>
            </div>
            <div class="header-right">
              <el-tag v-if="item.priority === 'HIGH'" type="danger" size="small">重要</el-tag>
              <el-dropdown v-if="canManage" trigger="click" @command="(cmd) => handleCommand(cmd, item)">
                <span class="more-btn">
                  <span class="material-icons-outlined">more_horiz</span>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="edit">
                      <span class="material-icons-outlined">edit</span>
                      编辑
                    </el-dropdown-item>
                    <el-dropdown-item command="pin">
                      <span class="material-icons-outlined">{{ item.isPinned ? 'push_pin' : 'push_pin' }}</span>
                      {{ item.isPinned ? '取消置顶' : '置顶' }}
                    </el-dropdown-item>
                    <el-dropdown-item command="delete" class="danger-item">
                      <span class="material-icons-outlined">delete</span>
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <div class="announcement-content" @click="handleViewDetail(item)">
            <div class="content-text">{{ item.content }}</div>
            <div class="content-footer">
              <span class="publisher">{{ item.publisherName }}</span>
              <span class="publish-time">{{ formatTime(item.publishTime) }}</span>
              <span v-if="item.readCount" class="read-count">
                {{ item.readCount }}人已读
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部按钮 -->
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button v-if="canManage" type="primary" @click="showCreateDialog = true">
          发布公告
        </el-button>
      </div>
    </template>

    <!-- 创建/编辑公告对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingAnnouncement ? '编辑公告' : '发布公告'"
      width="500px"
      append-to-body
    >
      <el-form :model="formData" label-width="80px">
        <el-form-item label="标题" required>
          <el-input
            v-model="formData.title"
            placeholder="请输入公告标题"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="内容" required>
          <el-input
            v-model="formData.content"
            type="textarea"
            :rows="6"
            placeholder="请输入公告内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="优先级">
          <el-radio-group v-model="formData.priority">
            <el-radio label="LOW">普通</el-radio>
            <el-radio label="MEDIUM">重要</el-radio>
            <el-radio label="HIGH">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ editingAnnouncement ? '更新' : '发布' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 公告详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="公告详情"
      width="500px"
      append-to-body
    >
      <div v-if="currentAnnouncement" class="announcement-detail">
        <div class="detail-header">
          <h3 class="detail-title">{{ currentAnnouncement.title }}</h3>
          <el-tag v-if="currentAnnouncement.priority === 'HIGH'" type="danger" size="small">
            重要
          </el-tag>
        </div>

        <div class="detail-content">
          {{ currentAnnouncement.content }}
        </div>

        <div class="detail-footer">
          <div class="publisher-info">
            <span>{{ currentAnnouncement.publisherName }}</span>
            <span>{{ formatTime(currentAnnouncement.publishTime) }}</span>
          </div>
          <div v-if="currentAnnouncement.readCount" class="read-info">
            {{ currentAnnouncement.readCount }}人已读
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import {
  createAnnouncement,
  updateAnnouncement,
  deleteAnnouncement,
  getAnnouncementPage,
  publishAnnouncement,
  setAnnouncementPinned,
  markAnnouncementAsRead
} from '@/api/im/announcement'

const props = defineProps({
  modelValue: Boolean,
  groupId: [String, Number],
  canManage: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const announcements = ref([])
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const submitting = ref(false)
const editingAnnouncement = ref(null)
const currentAnnouncement = ref(null)

const formData = ref({
  title: '',
  content: '',
  priority: 'MEDIUM'
})

// 加载公告列表
const loadAnnouncements = async () => {
  if (!props.groupId) return

  loading.value = true
  try {
    const res = await getAnnouncementPage({
      targetType: 'GROUP',
      targetIds: [props.groupId],
      status: 'PUBLISHED',
      pageNum: 1,
      pageSize: 20
    })
    if (res.code === 200) {
      announcements.value = res.data.list || []
    }
  } catch (error) {
    console.error('加载公告失败', error)
  } finally {
    loading.value = false
  }
}

// 查看详情
const handleViewDetail = async (item) => {
  currentAnnouncement.value = item
  showDetailDialog.value = true

  // 标记为已读
  if (!item.isRead) {
    try {
      await markAnnouncementAsRead(item.id)
      item.isRead = true
    } catch (e) {
      console.error('标记已读失败', e)
    }
  }
}

// 菜单命令处理
const handleCommand = async (cmd, item) => {
  switch (cmd) {
    case 'edit':
      editingAnnouncement.value = item
      formData.value = {
        title: item.title,
        content: item.content,
        priority: item.priority || 'MEDIUM'
      }
      showCreateDialog.value = true
      break
    case 'pin':
      try {
        await setAnnouncementPinned(item.id, !item.isPinned)
        item.isPinned = !item.isPinned
        ElMessage.success(item.isPinned ? '已置顶' : '已取消置顶')
      } catch (e) {
        ElMessage.error('操作失败')
      }
      break
    case 'delete':
      try {
        await ElMessageBox.confirm('确定要删除这条公告吗？', '删除公告', {
          type: 'warning'
        })
        await deleteAnnouncement(item.id)
        ElMessage.success('删除成功')
        loadAnnouncements()
      } catch (e) {
        if (e !== 'cancel') {
          ElMessage.error('删除失败')
        }
      }
      break
  }
}

// 提交公告
const handleSubmit = async () => {
  if (!formData.value.title.trim()) {
    ElMessage.warning('请输入公告标题')
    return
  }
  if (!formData.value.content.trim()) {
    ElMessage.warning('请输入公告内容')
    return
  }

  submitting.value = true
  try {
    const data = {
      ...formData.value,
      announcementType: 'NOTICE',
      targetType: 'GROUP',
      targetIds: [props.groupId]
    }

    if (editingAnnouncement.value) {
      // 更新公告
      await updateAnnouncement({
        id: editingAnnouncement.value.id,
        ...data
      })
      ElMessage.success('更新成功')
    } else {
      // 创建公告
      const res = await createAnnouncement(data)
      if (res.code === 200) {
        // 自动发布
        await publishAnnouncement(res.data)
        ElMessage.success('发布成功')
      }
    }

    showCreateDialog.value = false
    editingAnnouncement.value = null
    resetForm()
    loadAnnouncements()
  } catch (error) {
    console.error('提交失败', error)
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  formData.value = {
    title: '',
    content: '',
    priority: 'MEDIUM'
  }
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`

  return date.toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit'
  })
}

// 监听对话框打开
watch(() => props.modelValue, (val) => {
  if (val) {
    loadAnnouncements()
  }
})

// 监听创建对话框关闭
watch(showCreateDialog, (val) => {
  if (!val) {
    editingAnnouncement.value = null
    resetForm()
  }
})
</script>

<style scoped lang="scss">
.announcement-list {
  max-height: 500px;
  overflow-y: auto;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: var(--dt-text-tertiary);
}

.loading-state {
  gap: 12px;
}

.announcement-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.announcement-item {
  padding: 16px;
  border-radius: 8px;
  background: var(--dt-bg-body);
  border: 1px solid var(--dt-border-light);
  transition: all 0.2s;

  &.unread {
    border-left: 3px solid var(--dt-brand-color);
    background: #f0f7ff;
  }

  &.pinned {
    background: #fffbf0;
    border-color: #ffd666;

    .pinned-badge {
      color: #faad14;
    }
  }

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }
}

.announcement-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.pinned-badge {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  font-weight: 500;

  .material-icons-outlined {
    font-size: 14px;
  }
}

.announcement-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--dt-text-primary);
}

.more-btn {
  cursor: pointer;
  color: var(--dt-text-secondary);
  padding: 4px;

  .material-icons-outlined {
    font-size: 18px;
  }

  &:hover {
    color: var(--dt-brand-color);
  }
}

.announcement-content {
  cursor: pointer;
}

.content-text {
  font-size: 14px;
  color: var(--dt-text-secondary);
  line-height: 1.6;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.content-footer {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: var(--dt-text-tertiary);
}

.danger-item {
  color: var(--dt-error-color);

  &:hover {
    background: var(--dt-error-bg) !important;
  }
}

// 公告详情
.announcement-detail {
  .detail-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 16px;
    padding-bottom: 16px;
    border-bottom: 1px solid var(--dt-border-light);
  }

  .detail-title {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: var(--dt-text-primary);
  }

  .detail-content {
    font-size: 14px;
    line-height: 1.8;
    color: var(--dt-text-secondary);
    white-space: pre-wrap;
    margin-bottom: 16px;
  }

  .detail-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 12px;
    color: var(--dt-text-tertiary);
  }

  .publisher-info {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  width: 100%;
}

// 暗色模式
.dark {
  .announcement-item {
    &.unread {
      background: rgba(22, 119, 255, 0.1);
    }

    &.pinned {
      background: rgba(250, 173, 20, 0.1);
    }
  }
}
</style>
