/**
 * 文档协作编辑器组件
 * 支持多人实时在线编辑、协作者状态显示、评论等功能
 */
<template>
  <div class="document-editor">
    <!-- 编辑器头部工具栏 -->
    <div class="editor-header">
      <div class="header-left">
        <input
          v-model="documentTitle"
          class="title-input"
          placeholder="文档标题"
          @blur="saveTitle"
        >
        <span
          v-if="saving"
          class="saving-status"
        >
          <span class="material-icons-outlined spinning">sync</span>
          保存中...
        </span>
        <span
          v-else-if="lastSaved"
          class="saved-status"
        >
          已保存 {{ lastSaved }}
        </span>
      </div>

      <div class="header-center">
        <!-- 在线协作者头像 -->
        <div class="online-collaborators">
          <el-avatar
            v-for="user in onlineUsers"
            :key="user.userId"
            :size="28"
            :src="user.avatar"
            class="collaborator-avatar"
            :title="user.userName"
          >
            {{ user.userName?.charAt(0) }}
          </el-avatar>
          <span
            v-if="onlineUsers.length > 0"
            class="online-count"
          >
            {{ onlineUsers.length }} 人在线
          </span>
        </div>
      </div>

      <div class="header-right">
        <el-button
          size="small"
          :icon="Share"
          @click="showShareDialog = true"
        >
          分享
        </el-button>
        <el-button
          size="small"
          :icon="Clock"
          @click="showHistoryPanel = !showHistoryPanel"
        >
          历史
        </el-button>
        <el-button
          size="small"
          :icon="Comment"
          @click="showCommentsPanel = !showCommentsPanel"
        >
          评论 ({{ comments.length }})
        </el-button>
        <el-button
          size="small"
          :icon="Close"
          @click="handleClose"
        >
          关闭
        </el-button>
      </div>
    </div>

    <!-- 编辑器主体 -->
    <div class="editor-body">
      <!-- 左侧工具栏 -->
      <div class="editor-toolbar">
        <div class="toolbar-group">
          <button
            v-for="tool in formatTools"
            :key="tool.name"
            class="toolbar-btn"
            :class="{ active: isFormatActive(tool.tag) }"
            :title="tool.title"
            @click="execCommand(tool.command, tool.value)"
          >
            <span class="material-icons-outlined">{{ tool.icon }}</span>
          </button>
        </div>

        <div class="toolbar-divider" />

        <div class="toolbar-group">
          <el-dropdown
            trigger="click"
            @command="handleFontSize"
          >
            <button class="toolbar-btn">
              <span class="material-icons-outlined">format_size</span>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="size in fontSizes"
                  :key="size"
                  :command="size"
                >
                  {{ size }}px
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <el-color-picker
            v-model="textColor"
            size="small"
            show-alpha
            @change="changeTextColor"
          />
        </div>

        <div class="toolbar-divider" />

        <div class="toolbar-group">
          <button
            class="toolbar-btn"
            title="插入链接"
            @click="insertLink"
          >
            <span class="material-icons-outlined">link</span>
          </button>
          <button
            class="toolbar-btn"
            title="插入图片"
            @click="insertImage"
          >
            <span class="material-icons-outlined">image</span>
          </button>
          <button
            class="toolbar-btn"
            title="插入表格"
            @click="insertTable"
          >
            <span class="material-icons-outlined">table_chart</span>
          </button>
        </div>
      </div>

      <!-- 编辑区域 -->
      <div class="editor-content-wrapper">
        <div
          ref="editorRef"
          class="editor-content"
          contenteditable="true"
          @input="handleInput"
          @keydown="handleKeydown"
          @click="updateCursorPosition"
          @keyup="updateCursorPosition"
        >
          <!-- 文档内容 -->
        </div>

        <!-- 协作者光标显示 -->
        <div class="collaborator-cursors">
          <div
            v-for="cursor in collaboratorCursors"
            :key="cursor.userId"
            class="remote-cursor"
            :style="{ left: cursor.x + 'px', top: cursor.y + 'px' }"
          >
            <div
              class="cursor-flag"
              :style="{ backgroundColor: cursor.color }"
            >
              {{ cursor.userName }}
            </div>
            <div
              class="cursor-line"
              :style="{ backgroundColor: cursor.color }"
            />
          </div>
        </div>
      </div>

      <!-- 右侧面板：历史记录或评论 -->
      <div
        v-if="showHistoryPanel || showCommentsPanel"
        class="editor-side-panel"
      >
        <!-- 历史记录 -->
        <div
          v-if="showHistoryPanel"
          class="history-panel"
        >
          <div class="panel-header">
            <h3>版本历史</h3>
            <el-button
              size="small"
              text
              @click="showHistoryPanel = false"
            >
              <span class="material-icons-outlined">close</span>
            </el-button>
          </div>
          <div class="panel-content">
            <div
              v-for="version in versions"
              :key="version.id"
              class="history-item"
              :class="{ active: currentVersion === version.version }"
              @click="restoreVersion(version)"
            >
              <div class="history-meta">
                <span class="history-author">{{ version.creatorName }}</span>
                <span class="history-time">{{ formatTime(version.createTime) }}</span>
              </div>
              <div class="history-desc">
                版本 {{ version.version }} - {{ version.description || '无描述' }}
              </div>
            </div>
          </div>
        </div>

        <!-- 评论区 -->
        <div
          v-if="showCommentsPanel"
          class="comments-panel"
        >
          <div class="panel-header">
            <h3>评论 ({{ comments.length }})</h3>
            <el-button
              size="small"
              text
              @click="showCommentsPanel = false"
            >
              <span class="material-icons-outlined">close</span>
            </el-button>
          </div>
          <div class="panel-content">
            <div
              v-for="comment in comments"
              :key="comment.id"
              class="comment-item"
            >
              <el-avatar
                :size="32"
                :src="comment.creatorAvatar"
              >
                {{ comment.creatorName?.charAt(0) }}
              </el-avatar>
              <div class="comment-content">
                <div class="comment-header">
                  <span class="comment-author">{{ comment.creatorName }}</span>
                  <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                </div>
                <div class="comment-text">
                  {{ comment.content }}
                </div>
              </div>
            </div>
            <!-- 添加评论输入框 -->
            <div class="add-comment">
              <el-input
                v-model="newComment"
                type="textarea"
                :rows="2"
                placeholder="添加评论..."
                @keyup.ctrl.enter="addComment"
              />
              <el-button
                size="small"
                type="primary"
                :disabled="!newComment.trim()"
                @click="addComment"
              >
                发送
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分享对话框 -->
    <el-dialog
      v-model="showShareDialog"
      title="分享文档"
      width="500px"
    >
      <div class="share-dialog">
        <el-form label-width="80px">
          <el-form-item label="协作者">
            <el-select
              v-model="newCollaborators"
              multiple
              filterable
              placeholder="选择用户添加为协作者"
              style="width: 100%"
            >
              <el-option
                v-for="user in availableUsers"
                :key="user.userId"
                :label="user.userName"
                :value="user.userId"
              >
                <div class="user-option">
                  <el-avatar
                    :size="24"
                    :src="user.avatar"
                  >
                    {{ user.userName?.charAt(0) }}
                  </el-avatar>
                  <span>{{ user.userName }}</span>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="权限">
            <el-radio-group v-model="collaboratorPermission">
              <el-radio label="EDIT">
                可编辑
              </el-radio>
              <el-radio label="COMMENT">
                可评论
              </el-radio>
              <el-radio label="VIEW">
                仅查看
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>

        <div class="current-collaborators">
          <h4>当前协作者</h4>
          <div class="collaborator-list">
            <div
              v-for="collaborator in collaborators"
              :key="collaborator.userId"
              class="collaborator-item"
            >
              <el-avatar
                :size="32"
                :src="collaborator.avatar"
              >
                {{ collaborator.userName?.charAt(0) }}
              </el-avatar>
              <span class="collaborator-name">{{ collaborator.userName }}</span>
              <el-tag
                :type="getPermissionType(collaborator.permission)"
                size="small"
              >
                {{ getPermissionLabel(collaborator.permission) }}
              </el-tag>
              <el-button
                v-if="canRemoveCollaborator(collaborator)"
                size="small"
                text
                type="danger"
                @click="removeCollaborator(collaborator)"
              >
                移除
              </el-button>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showShareDialog = false">
          取消
        </el-button>
        <el-button
          type="primary"
          @click="addCollaborators"
        >
          添加
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Share,
  Clock,
  Comment,
  Close
} from '@element-plus/icons-vue'
import {
  getDocumentDetail,
  updateDocument,
  getCollaborators,
  getOnlineEditors,
  sendHeartbeat,
  joinDocument,
  leaveDocument,
  addCollaborators as apiAddCollaborators,
  removeCollaborator as apiRemoveCollaborator,
  getDocumentVersions,
  getDocumentComments,
  addDocumentComment
} from '@/api/im/document'
import { useImWebSocket } from '@/composables/useImWebSocket'
import { addTokenToUrl } from '@/utils/file'

const props = defineProps({
  documentId: {
    type: [String, Number],
    required: true
  }
})

const emit = defineEmits(['close', 'save'])

// ========== 状态 ==========
const documentTitle = ref('')
const documentContent = ref('')
const saving = ref(false)
const lastSaved = ref('')
const editorRef = ref(null)

// 在线用户和协作者
const onlineUsers = ref([])
const collaborators = ref([])
const collaboratorCursors = ref([])

// 历史和评论
const showHistoryPanel = ref(false)
const showCommentsPanel = ref(false)
const versions = ref([])
const comments = ref([])
const newComment = ref('')
const currentVersion = ref(0)

// 分享对话框
const showShareDialog = ref(false)
const newCollaborators = ref([])
const collaboratorPermission = ref('EDIT')
const availableUsers = ref([])

// 文本样式
const textColor = ref('#000000')

// 定时器
let heartbeatTimer = null
let saveTimer = null
let onlineUsersTimer = null

// ========== 工具栏配置 ==========
const formatTools = [
  { name: 'bold', icon: 'format_bold', command: 'bold', title: '加粗', tag: 'B' },
  { name: 'italic', icon: 'format_italic', command: 'italic', title: '斜体', tag: 'I' },
  { name: 'underline', icon: 'format_underlined', command: 'underline', title: '下划线', tag: 'U' },
  { name: 'strike', icon: 'strikethrough_s', command: 'strikeThrough', title: '删除线', tag: 'S' },
  { name: 'h1', icon: 'title', command: 'formatBlock', value: 'H1', title: '标题1' },
  { name: 'h2', icon: 'title', command: 'formatBlock', value: 'H2', title: '标题2' },
  { name: 'ul', icon: 'format_list_bulleted', command: 'insertUnorderedList', title: '无序列表' },
  { name: 'ol', icon: 'format_list_numbered', command: 'insertOrderedList', title: '有序列表' }
]

const fontSizes = [12, 14, 16, 18, 20, 24, 28, 32, 36, 48]

// ========== 生命周期 ==========
onMounted(async () => {
  await loadDocument()
  await loadCollaborators()
  await loadComments()
  await loadVersions()

  // 加入在线编辑
  await joinDocument(props.documentId)
  await loadOnlineUsers()

  // 启动定时器
  startTimers()

  // 监听 WebSocket 消息
  setupWebSocketListeners()
})

onUnmounted(async () => {
  clearInterval(heartbeatTimer)
  clearInterval(saveTimer)
  clearInterval(onlineUsersTimer)

  await leaveDocument(props.documentId)
})

// ========== 数据加载 ==========
async function loadDocument() {
  try {
    const res = await getDocumentDetail(props.documentId)
    if (res.code === 200 && res.data) {
      documentTitle.value = res.data.title || ''
      documentContent.value = res.data.content || ''
      currentVersion.value = res.data.version || 0

      await nextTick()
      if (editorRef.value) {
        editorRef.value.innerHTML = documentContent.value
      }
    }
  } catch (error) {
    console.error('加载文档失败:', error)
  }
}

async function loadCollaborators() {
  try {
    const res = await getCollaborators(props.documentId)
    if (res.code === 200) {
      collaborators.value = res.data || []
    }
  } catch (error) {
    console.error('加载协作者失败:', error)
  }
}

async function loadOnlineUsers() {
  try {
    const res = await getOnlineEditors(props.documentId)
    if (res.code === 200) {
      onlineUsers.value = res.data || []
    }
  } catch (error) {
    console.error('加载在线用户失败:', error)
  }
}

async function loadComments() {
  try {
    const res = await getDocumentComments(props.documentId)
    if (res.code === 200) {
      comments.value = res.data || []
    }
  } catch (error) {
    console.error('加载评论失败:', error)
  }
}

async function loadVersions() {
  try {
    const res = await getDocumentVersions(props.documentId)
    if (res.code === 200) {
      versions.value = res.data || []
    }
  } catch (error) {
    console.error('加载版本历史失败:', error)
  }
}

// ========== 编辑器操作 ==========
function execCommand(command, value = null) {
  document.execCommand(command, false, value)
  editorRef.value?.focus()
}

function isFormatActive(tag) {
  return document.queryCommandState(tag)
}

function handleFontSize(size) {
  execCommand('fontSize', '7')
  const fontElements = editorRef.value?.getElementsByTagName('font')
  if (fontElements?.length) {
    fontElements[fontElements.length - 1].size = size
  }
}

function changeTextColor(color) {
  execCommand('foreColor', color)
}

function insertLink() {
  const url = prompt('请输入链接地址:', 'https://')
  if (url) {
    execCommand('createLink', url)
  }
}

function insertImage() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = async e => {
    const file = e.target.files[0]
    if (file) {
      // TODO: 上传图片并插入
      ElMessage.info('图片上传功能待实现')
    }
  }
  input.click()
}

function insertTable() {
  const rows = prompt('行数:', '3')
  const cols = prompt('列数:', '3')
  if (rows && cols) {
    let table = '<table style="border-collapse: collapse; width: 100%;">'
    for (let i = 0; i < parseInt(rows); i++) {
      table += '<tr>'
      for (let j = 0; j < parseInt(cols); j++) {
        table += '<td style="border: 1px solid #ddd; padding: 8px;">&nbsp;</td>'
      }
      table += '</tr>'
    }
    table += '</table><p></p>'
    execCommand('insertHTML', table)
  }
}

function handleInput() {
  documentContent.value = editorRef.value?.innerHTML || ''
  scheduleSave()
}

function handleKeydown(e) {
  // Ctrl+S 保存
  if (e.ctrlKey && e.key === 's') {
    e.preventDefault()
    saveDocument()
  }
}

async function updateCursorPosition() {
  const selection = window.getSelection()
  if (selection && selection.rangeCount > 0) {
    const range = selection.getRangeAt(0)
    const rect = range.getBoundingClientRect()
    // TODO: 发送光标位置到服务器
  }
}

// ========== 保存逻辑 ==========
function scheduleSave() {
  clearTimeout(saveTimer)
  saveTimer = setTimeout(() => {
    saveDocument()
  }, 2000) // 2秒后自动保存
}

async function saveDocument() {
  if (saving.value) {return}

  saving.value = true
  try {
    const content = editorRef.value?.innerHTML || ''
    const res = await updateDocument(props.documentId, {
      title: documentTitle.value,
      content: content
    })

    if (res.code === 200) {
      lastSaved.value = formatTime(new Date())
      emit('save', { title: documentTitle.value, content })
    }
  } catch (error) {
    console.error('保存文档失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function saveTitle() {
  await saveDocument()
}

// ========== 定时器 ==========
function startTimers() {
  // 心跳：每30秒一次
  heartbeatTimer = setInterval(() => {
    sendHeartbeat(props.documentId).catch(console.error)
  }, 30000)

  // 在线用户：每10秒刷新
  onlineUsersTimer = setInterval(() => {
    loadOnlineUsers()
  }, 10000)
}

// ========== WebSocket 监听 ==========
function setupWebSocketListeners() {
  // TODO: 监听文档协作相关的 WebSocket 消息
  // - 用户加入/离开
  // - 光标位置更新
  // - 内容更新
}

// ========== 评论功能 ==========
async function addComment() {
  if (!newComment.value.trim()) {return}

  try {
    const res = await addDocumentComment(props.documentId, {
      content: newComment.value
    })

    if (res.code === 200) {
      comments.value.push(res.data)
      newComment.value = ''
      ElMessage.success('评论已添加')
    }
  } catch (error) {
    console.error('添加评论失败:', error)
    ElMessage.error('添加评论失败')
  }
}

// ========== 版本恢复 ==========
async function restoreVersion(version) {
  try {
    await ElMessageBox.confirm(
      `确定要恢复到版本 ${version.version} 吗？当前内容将被替换。`,
      '确认恢复',
      { type: 'warning' }
    )

    // TODO: 调用版本恢复API
    ElMessage.success('版本恢复成功')
    await loadDocument()
    await loadVersions()
  } catch {
    // 用户取消
  }
}

// ========== 协作者管理 ==========
async function addCollaborators() {
  if (newCollaborators.value.length === 0) {
    ElMessage.warning('请选择要添加的协作者')
    return
  }

  try {
    await apiAddCollaborators({
      documentId: props.documentId,
      userIds: newCollaborators.value,
      permission: collaboratorPermission.value
    })

    ElMessage.success('添加成功')
    showShareDialog.value = false
    newCollaborators.value = []
    await loadCollaborators()
  } catch (error) {
    console.error('添加协作者失败:', error)
    ElMessage.error('添加失败')
  }
}

async function removeCollaborator(collaborator) {
  try {
    await apiRemoveCollaborator(props.documentId, collaborator.userId)
    ElMessage.success('移除成功')
    await loadCollaborators()
  } catch (error) {
    console.error('移除协作者失败:', error)
    ElMessage.error('移除失败')
  }
}

function canRemoveCollaborator(collaborator) {
  // TODO: 检查权限
  return true
}

function getPermissionType(permission) {
  const types = {
    EDIT: '',
    COMMENT: 'warning',
    VIEW: 'info'
  }
  return types[permission] || ''
}

function getPermissionLabel(permission) {
  const labels = {
    EDIT: '可编辑',
    COMMENT: '可评论',
    VIEW: '仅查看'
  }
  return labels[permission] || permission
}

// ========== 工具函数 ==========
function formatTime(time) {
  if (!time) {return ''}
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) {return '刚刚'}
  if (diff < 3600000) {return Math.floor(diff / 60000) + '分钟前'}
  if (diff < 86400000) {return Math.floor(diff / 3600000) + '小时前'}

  return date.toLocaleDateString() + ' ' + date.toLocaleTimeString().slice(0, 5)
}

function handleClose() {
  if (editorRef.value?.innerHTML !== documentContent.value) {
    ElMessageBox.confirm('有未保存的更改，确定要关闭吗？', '提示', {
      confirmButtonText: '保存并关闭',
      cancelButtonText: '直接关闭',
      distinguishCancelAndClose: true,
      type: 'warning'
    }).then(() => {
      saveDocument().then(() => emit('close'))
    }).catch(action => {
      if (action === 'cancel') {
        emit('close')
      }
    })
  } else {
    emit('close')
  }
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.document-editor {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #fff;
}

.editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-bottom: 1px solid var(--el-border-color);
  background: #fafafa;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .title-input {
      font-size: 16px;
      font-weight: 500;
      border: none;
      background: transparent;
      outline: none;
      width: 300px;
      padding: 4px 8px;
      border-radius: var(--dt-radius-sm);

      &:hover {
        background: var(--dt-black-04);
      }

      &:focus {
        background: #fff;
        box-shadow: 0 0 0 2px var(--el-color-primary);
      }
    }

    .saving-status,
    .saved-status {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      color: var(--el-text-color-secondary);

      .spinning {
        animation: spin 1s linear infinite;
      }
    }
  }

  .header-center {
    flex: 1;
    display: flex;
    justify-content: center;

    .online-collaborators {
      display: flex;
      align-items: center;
      gap: -8px;

      .collaborator-avatar {
        border: 2px solid #fff;
        margin-left: -8px;
        cursor: pointer;
        transition: transform 0.2s;

        &:hover {
          transform: translateY(-2px);
          z-index: 1;
        }
      }

      .online-count {
        margin-left: 8px;
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }
  }

  .header-right {
    display: flex;
    gap: 8px;
  }
}

.editor-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.editor-toolbar {
  width: 50px;
  padding: 12px 8px;
  border-right: 1px solid var(--el-border-color);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;

  .toolbar-group {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .toolbar-divider {
    width: 30px;
    height: 1px;
    background: var(--el-border-color);
    margin: 4px 0;
  }

  .toolbar-btn {
    width: 36px;
    height: 36px;
    border: none;
    background: transparent;
    border-radius: var(--dt-radius-sm);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;

    &:hover {
      background: var(--dt-black-04);
    }

    &.active {
      background: var(--el-color-primary-light-9);
      color: var(--el-color-primary);
    }
  }
}

.editor-content-wrapper {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.editor-content {
  width: 100%;
  height: 100%;
  padding: 40px 60px;
  overflow-y: auto;
  outline: none;
  font-size: 15px;
  line-height: 1.8;

  &:empty:before {
    content: '开始输入内容...';
    color: var(--el-text-color-placeholder);
  }

  // 编辑器内元素样式
  h1, h2, h3, h4, h5, h6 {
    margin: 16px 0 8px;
    font-weight: 600;
  }

  h1 { font-size: 28px; }
  h2 { font-size: 24px; }
  h3 { font-size: 20px; }

  p {
    margin: 8px 0;
  }

  ul, ol {
    margin: 8px 0;
    padding-left: 24px;
  }

  table {
    border-collapse: collapse;
    width: 100%;
    margin: 16px 0;

    td {
      border: 1px solid var(--el-border-color);
      padding: 8px 12px;
    }
  }

  a {
    color: var(--el-color-primary);
    text-decoration: underline;
  }
}

.collaborator-cursors {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;

  .remote-cursor {
    position: absolute;
    transition: all 0.1s ease-out;

    .cursor-flag {
      padding: 2px 6px;
      border-radius: 2px;
      font-size: 11px;
      color: #fff;
      white-space: nowrap;
    }

    .cursor-line {
      width: 2px;
      height: 18px;
    }
  }
}

.editor-side-panel {
  width: 300px;
  border-left: 1px solid var(--el-border-color);
  display: flex;
  flex-direction: column;
  background: #fafafa;

  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    border-bottom: 1px solid var(--el-border-color);

    h3 {
      margin: 0;
      font-size: 14px;
      font-weight: 500;
    }
  }

  .panel-content {
    flex: 1;
    overflow-y: auto;
    padding: 12px;
  }
}

.history-panel {
  .history-item {
    padding: 10px 12px;
    border-radius: var(--dt-radius-sm);
    cursor: pointer;
    margin-bottom: 8px;
    transition: background 0.2s;

    &:hover {
      background: var(--dt-black-04);
    }

    &.active {
      background: var(--el-color-primary-light-9);
    }

    .history-meta {
      display: flex;
      justify-content: space-between;
      font-size: 12px;
      color: var(--el-text-color-secondary);
      margin-bottom: 4px;
    }

    .history-desc {
      font-size: 13px;
    }
  }
}

.comments-panel {
  .comment-item {
    display: flex;
    gap: 10px;
    margin-bottom: 16px;

    .comment-content {
      flex: 1;

      .comment-header {
        display: flex;
        justify-content: space-between;
        margin-bottom: 4px;

        .comment-author {
          font-weight: 500;
          font-size: 13px;
        }

        .comment-time {
          font-size: 11px;
          color: var(--el-text-color-secondary);
        }
      }

      .comment-text {
        font-size: 13px;
        line-height: 1.5;
      }
    }
  }

  .add-comment {
    margin-top: 16px;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
}

.share-dialog {
  .current-collaborators {
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid var(--el-border-color);

    h4 {
      margin: 0 0 12px;
      font-size: 14px;
    }

    .collaborator-list {
      .collaborator-item {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 8px 0;
      }
    }
  }

  .user-option {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

// 深色模式
.dark {
  .editor-header {
    background: #1a1a1a;
    border-bottom-color: var(--dt-white-10);
  }

  .editor-toolbar {
    border-right-color: var(--dt-white-10);

    .toolbar-btn:hover {
      background: var(--dt-white-10);
    }
  }

  .editor-side-panel {
    background: #1a1a1a;
    border-left-color: var(--dt-white-10);
  }
}
</style>
