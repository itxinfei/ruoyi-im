<template>
  <div class="document-list">
    <!-- 文档头部 -->
    <div class="document-header">
      <div class="header-title">
        <h2>文档</h2>
        <span class="document-count">{{ documentList.length }} 个文档</span>
      </div>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索文档..."
          :prefix-icon="Search"
          class="search-input"
          @input="handleSearch"
        />
        <el-button type="primary" :icon="Plus" @click="handleCreateDocument"> 新建文档 </el-button>
        <el-button :icon="Upload" @click="handleImport"> 导入 </el-button>
      </div>
    </div>

    <!-- 文档分类标签 -->
    <div class="document-tabs">
      <div
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-item"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        <el-icon><component :is="tab.icon" /></el-icon>
        <span>{{ tab.label }}</span>
        <span v-if="tab.count > 0" class="tab-count">{{ tab.count }}</span>
      </div>
    </div>

    <!-- 文档列表 -->
    <div class="document-container">
      <!-- 我的文档 -->
      <template v-if="activeTab === 'my' || activeTab === 'all'">
        <div v-if="myDocuments.length === 0" class="empty-state">
          <el-icon :size="64"><Document /></el-icon>
          <p>暂无文档，创建第一个文档吧</p>
          <el-button type="primary" @click="handleCreateDocument">创建文档</el-button>
        </div>
        <div v-else class="document-grid">
          <div
            v-for="doc in myDocuments"
            :key="doc.id"
            class="document-card"
            @click="handleOpenDocument(doc)"
          >
            <div class="card-icon" :class="doc.iconType">
              <el-icon :size="32"><component :is="getDocIcon(doc.type)" /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-title">{{ doc.title }}</div>
              <div class="card-meta">
                <span class="meta-time">{{ formatTime(doc.updateTime) }}</span>
              </div>
              <div class="card-preview">{{ doc.preview }}</div>
            </div>
            <div class="card-footer">
              <div class="collaborators">
                <el-avatar
                  v-for="user in doc.collaborators.slice(0, 3)"
                  :key="user.id"
                  :size="24"
                  :src="user.avatar"
                >
                  {{ user.name?.charAt(0) }}
                </el-avatar>
                <span v-if="doc.collaborators.length > 3" class="more-count">
                  +{{ doc.collaborators.length - 3 }}
                </span>
              </div>
              <div class="card-actions" @click.stop>
                <el-dropdown trigger="click" @command="handleCardAction($event, doc)">
                  <el-icon :size="20"><MoreFilled /></el-icon>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="rename">重命名</el-dropdown-item>
                      <el-dropdown-item command="share">分享</el-dropdown-item>
                      <el-dropdown-item command="move">移动到</el-dropdown-item>
                      <el-dropdown-item command="star" :divided="true">
                        {{ doc.starred ? '取消收藏' : '收藏' }}
                      </el-dropdown-item>
                      <el-dropdown-item command="delete" divided>
                        <span style="color: var(--el-color-danger)">删除</span>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
            <div v-if="doc.starred" class="card-star">
              <el-icon color="#fadb14"><Star /></el-icon>
            </div>
          </div>
        </div>
      </template>

      <!-- 与我共享 -->
      <template v-if="activeTab === 'shared'">
        <div v-if="sharedDocuments.length === 0" class="empty-state">
          <el-icon :size="64"><Share /></el-icon>
          <p>暂无共享文档</p>
        </div>
        <div v-else class="document-grid">
          <div
            v-for="doc in sharedDocuments"
            :key="doc.id"
            class="document-card"
            @click="handleOpenDocument(doc)"
          >
            <div class="card-icon shared">
              <el-icon :size="32"><Share /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-title">{{ doc.title }}</div>
              <div class="card-meta">
                <span class="meta-owner">来自 {{ doc.owner }}</span>
                <span class="meta-time">{{ formatTime(doc.updateTime) }}</span>
              </div>
              <div class="card-preview">{{ doc.preview }}</div>
            </div>
            <div class="card-footer">
              <div class="card-permission">
                <el-tag :type="getPermissionType(doc.permission)" size="small">
                  {{ getPermissionText(doc.permission) }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- 收藏夹 -->
      <template v-if="activeTab === 'starred'">
        <div v-if="starredDocuments.length === 0" class="empty-state">
          <el-icon :size="64"><Star /></el-icon>
          <p>暂无收藏文档</p>
        </div>
        <div v-else class="document-grid">
          <div
            v-for="doc in starredDocuments"
            :key="doc.id"
            class="document-card"
            @click="handleOpenDocument(doc)"
          >
            <div class="card-icon starred">
              <el-icon :size="32"><Star /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-title">{{ doc.title }}</div>
              <div class="card-meta">
                <span class="meta-time">{{ formatTime(doc.updateTime) }}</span>
              </div>
              <div class="card-preview">{{ doc.preview }}</div>
            </div>
          </div>
        </div>
      </template>

      <!-- 回收站 -->
      <template v-if="activeTab === 'trash'">
        <div v-if="trashedDocuments.length === 0" class="empty-state">
          <el-icon :size="64"><Delete /></el-icon>
          <p>回收站为空</p>
        </div>
        <div v-else class="document-list-view">
          <div v-for="doc in trashedDocuments" :key="doc.id" class="document-list-item">
            <div class="item-icon">
              <el-icon :size="24"><Document /></el-icon>
            </div>
            <div class="item-content">
              <div class="item-title">{{ doc.title }}</div>
              <div class="item-meta">
                <span>删除于 {{ formatTime(doc.deleteTime) }}</span>
                <span>{{ doc.autoDeleteDays }}天后自动删除</span>
              </div>
            </div>
            <div class="item-actions">
              <el-button size="small" @click="handleRestore(doc)">恢复</el-button>
              <el-button size="small" type="danger" @click="handleDeleteForever(doc)">
                永久删除
              </el-button>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- 新建文档对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="新建文档"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="createFormRef" :model="createForm" label-width="80px">
        <el-form-item label="文档标题">
          <el-input
            v-model="createForm.title"
            placeholder="请输入文档标题"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="文档类型">
          <el-radio-group v-model="createForm.type">
            <el-radio value="doc">空白文档</el-radio>
            <el-radio value="sheet">表格</el-radio>
            <el-radio value="mind">思维导图</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="文件夹">
          <el-cascader
            v-model="createForm.folderId"
            :options="folderOptions"
            :props="{ checkStrictly: true }"
            placeholder="选择文件夹（可选）"
            clearable
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleSubmitCreate"> 创建 </el-button>
      </template>
    </el-dialog>

    <!-- 分享对话框 -->
    <el-dialog v-model="shareDialogVisible" title="分享文档" width="500px">
      <div v-if="currentShareDoc" class="share-content">
        <el-tabs v-model="shareTab">
          <el-tab-pane label="分享链接" name="link">
            <div class="share-link">
              <el-input
                v-model="shareLink"
                readonly
                :suffix-icon="CopyDocument"
                @click="handleCopyLink"
              >
                <template #append>
                  <el-button @click="handleCopyLink">复制</el-button>
                </template>
              </el-input>
            </div>
            <div class="share-settings">
              <div class="setting-item">
                <span>获取链接的成员可</span>
                <el-radio-group v-model="sharePermission">
                  <el-radio value="view">仅查看</el-radio>
                  <el-radio value="edit">编辑</el-radio>
                </el-radio-group>
              </div>
              <div class="setting-item">
                <el-checkbox v-model="shareAllowCopy">允许复制</el-checkbox>
              </div>
              <div class="setting-item">
                <el-checkbox v-model="shareAllowDownload">允许下载</el-checkbox>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="邀请协作" name="invite">
            <div class="invite-content">
              <el-select
                v-model="inviteUsers"
                multiple
                filterable
                placeholder="选择成员"
                style="width: 100%"
              >
                <el-option
                  v-for="user in userList"
                  :key="user.id"
                  :label="user.name"
                  :value="user.id"
                >
                  <div class="user-option">
                    <el-avatar :size="24" :src="user.avatar">
                      {{ user.name?.charAt(0) }}
                    </el-avatar>
                    <span>{{ user.name }}</span>
                  </div>
                </el-option>
              </el-select>
              <div class="invite-permission">
                <span>权限</span>
                <el-radio-group v-model="invitePermission">
                  <el-radio value="edit">可编辑</el-radio>
                  <el-radio value="view">仅查看</el-radio>
                </el-radio-group>
              </div>
              <el-button type="primary" style="width: 100%; margin-top: 16px" @click="handleInvite">
                邀请
              </el-button>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>

    <!-- 重命名对话框 -->
    <el-dialog v-model="renameDialogVisible" title="重命名" width="400px">
      <el-input
        v-model="newTitle"
        placeholder="请输入新标题"
        maxlength="50"
        show-word-limit
        @keyup.enter="handleSubmitRename"
      />
      <template #footer>
        <el-button @click="renameDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitRename">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Plus,
  Upload,
  Document,
  Share,
  Star,
  Delete,
  MoreFilled,
  CopyDocument,
  FolderOpened,
  Clock,
} from '@element-plus/icons-vue'
import { formatRelativeTime } from '@/utils/format/time.js'
import * as documentApi from '@/api/im/document'

// Props
const props = defineProps({
  userId: {
    type: [String, Number],
    default: null,
  },
})

// Emits
const emit = defineEmits(['open-document', 'document-created'])

// State
const loading = ref(false)
const activeTab = ref('all')
const searchKeyword = ref('')
const createDialogVisible = ref(false)
const shareDialogVisible = ref(false)
const renameDialogVisible = ref(false)
const creating = ref(false)
const shareTab = ref('link')
const sharePermission = ref('view')
const shareAllowCopy = ref(true)
const shareAllowDownload = ref(true)
const inviteUsers = ref([])
const invitePermission = ref('edit')
const newTitle = ref('')
const currentShareDoc = ref(null)
const currentRenameDoc = ref(null)

const shareLink = ref('')

// 标签页
const tabs = ref([
  { key: 'all', label: '全部', icon: Document, count: 0 },
  { key: 'my', label: '我的文档', icon: Document, count: 0 },
  { key: 'shared', label: '与我共享', icon: Share, count: 3 },
  { key: 'starred', label: '收藏夹', icon: Star, count: 0 },
  { key: 'trash', label: '回收站', icon: Delete, count: 0 },
])

// 新建文档表单
const createForm = ref({
  title: '',
  type: 'doc',
  folderId: null,
})

// 文件夹选项
const folderOptions = ref([
  {
    value: 'work',
    label: '工作',
    children: [
      { value: 'project', label: '项目文档' },
      { value: 'meeting', label: '会议记录' },
    ],
  },
  {
    value: 'personal',
    label: '个人',
    children: [
      { value: 'notes', label: '笔记' },
      { value: 'draft', label: '草稿' },
    ],
  },
])

// 用户列表（模拟数据 - 后续对接用户API）
const userList = ref([
  { id: 1, name: '张三', avatar: '' },
  { id: 2, name: '李四', avatar: '' },
  { id: 3, name: '王五', avatar: '' },
  { id: 4, name: '赵六', avatar: '' },
])

// 文档数据
const documentList = ref([])
const sharedDocuments = ref([])
const trashedDocuments = ref([])
const starredDocuments = ref([])

// 加载文档列表
const loadDocuments = async () => {
  loading.value = true
  try {
    const response = await documentApi.getDocumentList()
    if (response.code === 200) {
      const data = response.data || []
      documentList.value = data.map(doc => ({
        id: doc.id,
        title: doc.title,
        type: doc.type || 'doc',
        iconType: getIconType(doc.type),
        content: doc.content || '',
        preview: getPreview(doc.content),
        updateTime: doc.updateTime ? new Date(doc.updateTime) : new Date(),
        createTime: doc.createTime ? new Date(doc.createTime) : new Date(),
        ownerId: doc.creatorId,
        owner: doc.creatorName || '我',
        starred: doc.starred || false,
        collaborators: doc.collaborators || [],
        folderId: doc.folderId,
      }))
      updateTabCounts()
    }
  } catch (error) {
    console.error('加载文档列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载收藏文档
const loadStarredDocuments = async () => {
  try {
    const response = await documentApi.getStarredDocuments()
    if (response.code === 200) {
      starredDocuments.value = (response.data || []).map(doc => ({
        id: doc.id,
        title: doc.title,
        type: doc.type || 'doc',
        iconType: getIconType(doc.type),
        preview: getPreview(doc.content),
        updateTime: doc.updateTime ? new Date(doc.updateTime) : new Date(),
      }))
    }
  } catch (error) {
    console.error('加载收藏文档失败:', error)
  }
}

// 更新标签计数
const updateTabCounts = () => {
  tabs.value[0].count = documentList.value.length
  tabs.value[1].count = documentList.value.filter(d => d.ownerId === props.userId).length
  tabs.value[2].count = sharedDocuments.value.length
  tabs.value[3].count = starredDocuments.value.length
  tabs.value[4].count = trashedDocuments.value.length
}

// 获取图标类型
const getIconType = (type) => {
  const map = {
    doc: 'doc',
    DOC: 'doc',
    sheet: 'sheet',
    SHEET: 'sheet',
    mind: 'mind',
    MIND: 'mind',
  }
  return map[type] || 'doc'
}

// 获取预览文本
const getPreview = (content) => {
  if (!content) return ''
  const plainText = content.replace(/<[^>]*>/g, '').trim()
  return plainText.substring(0, 50) + (plainText.length > 50 ? '...' : '')
}

// 计算属性
const myDocuments = computed(() => {
  let result = documentList.value.filter(d => d.ownerId === props.userId)
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(
      d => d.title.toLowerCase().includes(keyword) || d.preview.toLowerCase().includes(keyword)
    )
  }
  return result
})

const starredDocuments = computed(() => {
  return documentList.value.filter(d => d.starred)
})

// 方法
const formatTime = time => {
  return formatRelativeTime(time)
}

const getDocIcon = type => {
  return Document
}

const getPermissionType = permission => {
  return permission === 'edit' ? 'success' : 'info'
}

const getPermissionText = permission => {
  return permission === 'edit' ? '可编辑' : '仅查看'
}

const handleSearch = () => {
  // 搜索已通过 computed 实现
}

const handleCreateDocument = () => {
  createForm.value = {
    title: '未命名文档',
    type: 'doc',
    folderId: null,
  }
  createDialogVisible.value = true
}

const handleSubmitCreate = async () => {
  if (!createForm.value.title.trim()) {
    ElMessage.warning('请输入文档标题')
    return
  }

  creating.value = true

  try {
    const response = await documentApi.createDocument({
      title: createForm.value.title,
      type: createForm.value.type.toUpperCase(),
      folderId: createForm.value.folderId,
      content: '',
    })

    if (response.code === 200) {
      ElMessage.success('文档创建成功')
      createDialogVisible.value = false
      await loadDocuments()
      emit('document-created', response.data)
    } else {
      ElMessage.error(response.msg || '创建失败')
    }
  } catch (error) {
    console.error('创建文档失败:', error)
    ElMessage.error('创建失败，请重试')
  } finally {
    creating.value = false
  }
}

const handleOpenDocument = doc => {
  emit('open-document', doc)
}

const handleImport = () => {
  ElMessage.info('导入功能开发中')
}

const handleCardAction = async (command, doc) => {
  switch (command) {
    case 'rename':
      currentRenameDoc.value = doc
      newTitle.value = doc.title
      renameDialogVisible.value = true
      break
    case 'share':
      currentShareDoc.value = doc
      shareDialogVisible.value = true
      break
    case 'move':
      ElMessage.info('移动功能开发中')
      break
    case 'star':
      try {
        const api = doc.starred ? documentApi.unstarDocument : documentApi.starDocument
        const response = await api(doc.id)
        if (response.code === 200) {
          doc.starred = !doc.starred
          updateTabCounts()
          ElMessage.success(doc.starred ? '已收藏' : '已取消收藏')
        }
      } catch (error) {
        ElMessage.error('操作失败')
      }
      break
    case 'delete':
      handleDelete(doc)
      break
  }
}

const handleDelete = async doc => {
  try {
    await ElMessageBox.confirm(`确定要将"${doc.title}"移到回收站吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const response = await documentApi.deleteDocument(doc.id)
    if (response.code === 200) {
      const index = documentList.value.findIndex(d => d.id === doc.id)
      if (index > -1) {
        documentList.value.splice(index, 1)
      }
      updateTabCounts()
      ElMessage.success('已移到回收站')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleRestore = async doc => {
  try {
    const response = await documentApi.restoreDocument(doc.id)
    if (response.code === 200) {
      const index = trashedDocuments.value.findIndex(d => d.id === doc.id)
      if (index > -1) {
        trashedDocuments.value.splice(index, 1)
      }
      await loadDocuments()
      updateTabCounts()
      ElMessage.success('已恢复文档')
    }
  } catch (error) {
    console.error('恢复失败:', error)
    ElMessage.error('恢复失败')
  }
}

const handleDeleteForever = async doc => {
  try {
    await ElMessageBox.confirm(`确定要永久删除"${doc.title}"吗？此操作不可恢复！`, '永久删除', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error',
    })

    const response = await documentApi.permanentlyDeleteDocument(doc.id)
    if (response.code === 200) {
      const index = trashedDocuments.value.findIndex(d => d.id === doc.id)
      if (index > -1) {
        trashedDocuments.value.splice(index, 1)
      }
      updateTabCounts()
      ElMessage.success('已永久删除')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleCopyLink = () => {
  navigator.clipboard.writeText(shareLink.value).then(() => {
    ElMessage.success('链接已复制')
  })
}

const handleInvite = () => {
  if (inviteUsers.value.length === 0) {
    ElMessage.warning('请选择要邀请的成员')
    return
  }
  ElMessage.success(`已邀请 ${inviteUsers.value.length} 位成员`)
  inviteUsers.value = []
  shareDialogVisible.value = false
}

const handleSubmitRename = () => {
  if (!newTitle.value.trim()) {
    ElMessage.warning('请输入新标题')
    return
  }

  if (currentRenameDoc.value) {
    currentRenameDoc.value.title = newTitle.value
    ElMessage.success('重命名成功')
    renameDialogVisible.value = false
  }
}

// 生命周期
onMounted(() => {
  loadDocuments()
  loadStarredDocuments()
})
</script>

<style lang="scss" scoped>
.document-list {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f5f7fa;
}

// 文档头部
.document-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: white;
  border-bottom: 1px solid var(--el-border-color-light);

  .header-title {
    display: flex;
    align-items: baseline;
    gap: 12px;

    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }

    .document-count {
      font-size: 13px;
      color: var(--el-text-color-secondary);
    }
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 12px;

    .search-input {
      width: 240px;
    }
  }
}

// 文档标签
.document-tabs {
  display: flex;
  gap: 4px;
  padding: 12px 20px;
  background: white;
  border-bottom: 1px solid var(--el-border-color-light);
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--el-text-color-secondary);
  font-size: 14px;

  &:hover {
    background: var(--el-fill-color-light);
  }

  &.active {
    background: var(--el-color-primary-light-9);
    color: var(--el-color-primary);
    font-weight: 500;
  }
}

.tab-count {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

// 文档容器
.document-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

// 文档网格
.document-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.document-card {
  position: relative;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 12px;
  border: 1px solid var(--el-border-color-light);
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: var(--el-color-primary);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  }
}

.card-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  margin-bottom: 12px;

  &.doc {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
  }

  &.meeting {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    color: white;
  }

  &.tech {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    color: white;
  }

  &.shared {
    background: var(--el-color-success-light-9);
    color: var(--el-color-success);
  }

  &.starred {
    background: var(--el-color-warning-light-9);
    color: var(--el-color-warning);
  }
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-size: 15px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  margin-bottom: 8px;

  .meta-owner {
    color: var(--el-text-color-secondary);
  }
}

.card-preview {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.collaborators {
  display: flex;
  align-items: center;
  gap: -8px;

  .el-avatar {
    border: 2px solid white;
    margin-left: -8px;

    &:first-child {
      margin-left: 0;
    }
  }

  .more-count {
    margin-left: 4px;
    font-size: 12px;
    color: var(--el-text-color-placeholder);
  }
}

.card-actions {
  color: var(--el-text-color-placeholder);

  .el-icon {
    cursor: pointer;

    &:hover {
      color: var(--el-text-color-secondary);
    }
  }
}

.card-star {
  position: absolute;
  top: 12px;
  right: 12px;
}

.card-permission {
  display: flex;
  gap: 8px;
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--el-text-color-placeholder);

  p {
    margin: 16px 0 24px 0;
    font-size: 14px;
  }
}

// 列表视图（回收站）
.document-list-view {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.document-list-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  border: 1px solid var(--el-border-color-light);

  .item-icon {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--el-fill-color-light);
    border-radius: 8px;
    color: var(--el-text-color-secondary);
  }

  .item-content {
    flex: 1;
  }

  .item-title {
    font-size: 15px;
    font-weight: 500;
    color: var(--el-text-color-primary);
  }

  .item-meta {
    display: flex;
    gap: 16px;
    margin-top: 4px;
    font-size: 13px;
    color: var(--el-text-color-placeholder);
  }
}

// 分享设置
.share-content {
  .share-link {
    margin-bottom: 20px;
  }

  .share-settings {
    .setting-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 0;
      border-bottom: 1px solid var(--el-border-color-lighter);

      &:last-child {
        border-bottom: none;
      }
    }
  }

  .invite-content {
    .invite-permission {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-top: 16px;
      padding: 12px;
      background: var(--el-fill-color-lighter);
      border-radius: 8px;
    }
  }
}

.user-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

@media (max-width: 768px) {
  .document-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;

    .header-actions {
      flex-direction: column;

      .search-input {
        width: 100%;
      }
    }
  }

  .document-grid {
    grid-template-columns: 1fr;
  }

  .document-tabs {
    overflow-x: auto;
    flex-wrap: nowrap;
  }
}
</style>
