<template>
  <div class="document-editor">
    <!-- 编辑器头部 -->
    <div class="editor-header">
      <div class="header-left">
        <el-button :icon="ArrowLeft" @click="handleBack" text>返回</el-button>
        <div class="doc-title-wrapper">
          <el-input
            v-if="editingTitle"
            ref="titleInputRef"
            v-model="document.title"
            class="doc-title-input"
            @blur="handleSaveTitle"
            @keyup.enter="handleSaveTitle"
          />
          <h2 v-else class="doc-title" @click="startEditTitle">
            {{ document.title || '未命名文档' }}
            <el-icon :size="16"><Edit /></el-icon>
          </h2>
          <span v-if="document.autoSaving" class="saving-indicator">
            保存中...
          </span>
          <span v-else-if="document.saved" class="saved-indicator">
            已保存
          </span>
        </div>
      </div>
      <div class="header-center">
        <div class="collaborator-avatars">
          <el-tooltip
            v-for="user in activeUsers"
            :key="user.id"
            :content="user.name + (user.cursor ? ' 正在编辑' : '')"
            placement="top"
          >
            <el-avatar
              :size="32"
              :src="user.avatar"
              :class="{ 'is-editing': user.isEditing }"
            >
              {{ user.name?.charAt(0) }}
            </el-avatar>
          </el-tooltip>
        </div>
      </div>
      <div class="header-right">
        <el-button :icon="Comment" @click="showComments = !showComments">
          评论 ({{ comments.length }})
        </el-button>
        <el-button :icon="Clock" @click="showHistory = true">
          历史版本
        </el-button>
        <el-dropdown trigger="click" @command="handleMoreAction">
          <el-button :icon="MoreFilled" circle />
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="share">分享</el-dropdown-item>
              <el-dropdown-item command="export">导出</el-dropdown-item>
              <el-dropdown-item command="print">打印</el-dropdown-item>
              <el-dropdown-item command="settings" divided>文档设置</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 编辑器主体 -->
    <div class="editor-body">
      <!-- 侧边栏：目录/评论 -->
      <div v-if="showComments || showOutline" class="editor-sidebar">
        <el-tabs v-model="sidebarTab" type="border-card">
          <el-tab-pane v-if="showOutline" label="目录" name="outline">
            <div class="outline-list">
              <div
                v-for="(item, index) in outline"
                :key="index"
                class="outline-item"
                :class="`level-${item.level}`"
                @click="scrollToHeading(item.id)"
              >
                {{ item.text }}
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane v-if="showComments" label="评论" name="comments">
            <div class="comments-list">
              <div
                v-for="comment in comments"
                :key="comment.id"
                class="comment-item"
              >
                <div class="comment-header">
                  <el-avatar :size="24" :src="comment.user.avatar">
                    {{ comment.user.name?.charAt(0) }}
                  </el-avatar>
                  <span class="comment-user">{{ comment.user.name }}</span>
                  <span class="comment-time">{{ formatTime(comment.time) }}</span>
                </div>
                <div class="comment-content">{{ comment.content }}</div>
                <div v-if="comment.replies?.length" class="comment-replies">
                  <div
                    v-for="reply in comment.replies"
                    :key="reply.id"
                    class="reply-item"
                  >
                    <span class="reply-user">{{ reply.user.name }}:</span>
                    <span class="reply-content">{{ reply.content }}</span>
                  </div>
                </div>
                <div class="comment-actions">
                  <el-button size="small" text @click="handleReplyComment(comment)">
                    回复
                  </el-button>
                  <el-button
                    v-if="comment.canDelete"
                    size="small"
                    text
                    type="danger"
                    @click="handleDeleteComment(comment)"
                  >
                    删除
                  </el-button>
                </div>
              </div>
              <div v-if="comments.length === 0" class="empty-comments">
                <p>暂无评论</p>
                <p class="tip">选中文字后点击评论按钮添加评论</p>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 编辑器区域 -->
      <div class="editor-container">
        <!-- 工具栏 -->
        <div class="editor-toolbar">
          <div class="toolbar-group">
            <el-tooltip content="撤销 (Ctrl+Z)" placement="top">
              <el-button :icon="RefreshLeft" text size="small" @click="handleUndo" />
            </el-tooltip>
            <el-tooltip content="重做 (Ctrl+Shift+Z)" placement="top">
              <el-button :icon="RefreshRight" text size="small" @click="handleRedo" />
            </el-tooltip>
          </div>
          <div class="toolbar-divider"></div>
          <div class="toolbar-group">
            <el-select v-model="formatType" size="small" style="width: 100px" @change="handleFormatChange">
              <el-option label="正文" value="p" />
              <el-option label="标题1" value="h1" />
              <el-option label="标题2" value="h2" />
              <el-option label="标题3" value="h3" />
              <el-option label="标题4" value="h4" />
            </el-select>
          </div>
          <div class="toolbar-divider"></div>
          <div class="toolbar-group">
            <el-tooltip content="加粗 (Ctrl+B)" placement="top">
              <el-button
                :icon="Bold"
                text
                size="small"
                :color="isBold ? 'var(--el-color-primary)' : ''"
                @click="handleBold"
              />
            </el-tooltip>
            <el-tooltip content="斜体 (Ctrl+I)" placement="top">
              <el-button
                :icon="Italic"
                text
                size="small"
                :color="isItalic ? 'var(--el-color-primary)' : ''"
                @click="handleItalic"
              />
            </el-tooltip>
            <el-tooltip content="下划线 (Ctrl+U)" placement="top">
              <el-button
                :icon="Underline"
                text
                size="small"
                :color="isUnderline ? 'var(--el-color-primary)' : ''"
                @click="handleUnderline"
              />
            </el-tooltip>
            <el-tooltip content="删除线" placement="top">
              <el-button
                :icon="Delete"
                text
                size="small"
                @click="handleStrikeThrough"
              />
            </el-tooltip>
          </div>
          <div class="toolbar-divider"></div>
          <div class="toolbar-group">
            <el-tooltip content="无序列表" placement="top">
              <el-button :icon="List" text size="small" @click="handleUnorderedList" />
            </el-tooltip>
            <el-tooltip content="有序列表" placement="top">
              <el-button :icon="OrderList" text size="small" @click="handleOrderedList" />
            </el-tooltip>
            <el-tooltip content="任务列表" placement="top">
              <el-button :icon="Select" text size="small" @click="handleTaskList" />
            </el-tooltip>
          </div>
          <div class="toolbar-divider"></div>
          <div class="toolbar-group">
            <el-tooltip content="插入链接" placement="top">
              <el-button :icon="Link" text size="small" @click="handleInsertLink" />
            </el-tooltip>
            <el-tooltip content="插入图片" placement="top">
              <el-button :icon="Picture" text size="small" @click="handleInsertImage" />
            </el-tooltip>
            <el-tooltip content="插入表格" placement="top">
              <el-button :icon="Grid" text size="small" @click="handleInsertTable" />
            </el-tooltip>
            <el-tooltip content="插入代码块" placement="top">
              <el-button :icon="Document" text size="small" @click="handleInsertCode" />
            </el-tooltip>
            <el-tooltip content="插入公式" placement="top">
              <el-button :icon="Operation" text size="small" @click="handleInsertFormula" />
            </el-tooltip>
          </div>
          <div class="toolbar-divider"></div>
          <div class="toolbar-group">
            <el-tooltip content="插入日期" placement="top">
              <el-button :icon="Calendar" text size="small" @click="handleInsertDate" />
            </el-tooltip>
            <el-tooltip content="插入分割线" placement="top">
              <el-button text size="small" @click="handleInsertDivider">—</el-button>
            </el-tooltip>
          </div>
          <div class="toolbar-divider"></div>
          <div class="toolbar-group">
            <el-color-picker v-model="textColor" size="small" @change="handleTextColor" />
            <el-color-picker v-model="bgColor" size="small" @change="handleBgColor" />
          </div>
          <div class="toolbar-spacer"></div>
          <div class="toolbar-group">
            <el-button :icon="Menu" text size="small" @click="toggleOutline">
              目录
            </el-button>
            <el-button :icon="FullScreen" text size="small" @click="toggleFullScreen">
              全屏
            </el-button>
          </div>
        </div>

        <!-- 编辑区域 -->
        <div
          ref="editorRef"
          class="editor-content"
          contenteditable="true"
          spellcheck="false"
          @input="handleInput"
          @keydown="handleKeydown"
          @mouseup="handleSelectionChange"
          @keyup="handleSelectionChange"
        >
          <p>欢迎使用在线文档编辑器</p>
          <p></p>
          <h2>功能特点</h2>
          <ul>
            <li>实时协作编辑 - 多人同时编辑文档</li>
            <li>富文本格式 - 支持多种文本样式</li>
            <li>插入元素 - 图片、表格、代码、公式</li>
            <li>评论系统 - 选中文字添加评论讨论</li>
            <li>历史版本 - 查看和恢复历史版本</li>
            <li>自动保存 - 编辑内容自动保存</li>
          </ul>
          <p></p>
          <h2>快捷键</h2>
          <ul>
            <li><b>Ctrl+B</b> - 加粗</li>
            <li><b>Ctrl+I</b> - 斜体</li>
            <li><b>Ctrl+U</b> - 下划线</li>
            <li><b>Ctrl+Z</b> - 撤销</li>
            <li><b>Ctrl+Shift+Z</b> - 重做</li>
            <li><b>Ctrl+/</b> - 显示快捷键</li>
          </ul>
        </div>

        <!-- 选中文字时的浮动工具栏 -->
        <transition name="fade">
          <div v-if="showFloatToolbar && selectionText" class="float-toolbar" :style="floatToolbarStyle">
            <el-button-group>
              <el-button :icon="Comment" size="small" @click="handleAddComment">
                评论
              </el-button>
              <el-button size="small" @click="handleHighlight">
                高亮
              </el-button>
            </el-button-group>
          </div>
        </transition>

        <!-- 协作者光标 -->
        <div
          v-for="cursor in collaboratorCursors"
          :key="cursor.userId"
          class="collaborator-cursor"
          :style="{
            left: cursor.x + 'px',
            top: cursor.y + 'px',
            borderColor: cursor.color
          }"
        >
          <span class="cursor-label" :style="{ backgroundColor: cursor.color }">
            {{ cursor.userName }}
          </span>
        </div>
      </div>
    </div>

    <!-- 历史版本对话框 -->
    <el-dialog
      v-model="showHistory"
      title="历史版本"
      width="800px"
    >
      <div class="history-list">
        <div
          v-for="(version, index) in historyVersions"
          :key="version.id"
          class="history-item"
          :class="{ active: selectedVersionId === version.id }"
          @click="handleSelectVersion(version)"
        >
          <div class="version-info">
            <div class="version-user">{{ version.user }}</div>
            <div class="version-time">{{ formatTime(version.time) }}</div>
          </div>
          <div class="version-actions">
            <el-button size="small" @click="handlePreviewVersion(version)">
              预览
            </el-button>
            <el-button
              v-if="index > 0"
              size="small"
              type="primary"
              @click="handleRestoreVersion(version)"
            >
              恢复此版本
            </el-button>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showHistory = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 评论对话框 -->
    <el-dialog
      v-model="commentDialogVisible"
      title="添加评论"
      width="500px"
    >
      <div class="comment-dialog">
        <div class="selected-text">
          <el-icon><Quote /></el-icon>
          "{{ selectedTextForComment }}"
        </div>
        <el-input
          v-model="newComment"
          type="textarea"
          :rows="4"
          placeholder="写下你的评论..."
        />
      </div>
      <template #footer>
        <el-button @click="commentDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitComment">发布</el-button>
      </template>
    </el-dialog>

    <!-- 插入链接对话框 -->
    <el-dialog
      v-model="linkDialogVisible"
      title="插入链接"
      width="500px"
    >
      <el-form label-width="80px">
        <el-form-item label="链接地址">
          <el-input v-model="linkUrl" placeholder="https://" />
        </el-form-item>
        <el-form-item label="链接文字">
          <el-input v-model="linkText" placeholder="输入链接文字" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="linkDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmLink">插入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  Edit,
  Comment,
  Clock,
  MoreFilled,
  RefreshLeft,
  RefreshRight,
  Bold,
  Italic,
  Underline,
  Delete,
  List,
  OrderList,
  Select,
  Link,
  Picture,
  Grid,
  Document,
  Operation,
  Calendar,
  Menu,
  FullScreen,
  Quote
} from '@element-plus/icons-vue'
import { formatRelativeTime } from '@/utils/format/time.js'

// Props
const props = defineProps({
  documentId: {
    type: [String, Number],
    required: true,
  },
  document: {
    type: Object,
    default: () => ({}),
  },
  userId: {
    type: [String, Number],
    default: null,
  },
})

// Emits
const emit = defineEmits(['back', 'save'])

// State
const editingTitle = ref(false)
const titleInputRef = ref(null)
const editorRef = ref(null)
const showComments = ref(false)
const showOutline = ref(false)
const sidebarTab = ref('comments')
const showHistory = ref(false)
const commentDialogVisible = ref(false)
const linkDialogVisible = ref(false)
const newComment = ref('')
const selectedTextForComment = ref('')
const currentReplyComment = ref(null)
const selectedVersionId = ref(null)

// 格式状态
const formatType = ref('p')
const isBold = ref(false)
const isItalic = ref(false)
const isUnderline = ref(false)
const textColor = ref('#000000')
const bgColor = ref('#ffffff')

// 选区状态
const selectionText = ref('')
const showFloatToolbar = ref(false)
const floatToolbarStyle = ref({ left: '0px', top: '0px' })

// 链接输入
const linkUrl = ref('')
const linkText = ref('')

// 文档状态
const document = ref({
  id: props.documentId,
  title: props.document.title || '未命名文档',
  content: props.document.content || '',
  autoSaving: false,
  saved: true,
})

// 目录
const outline = ref([])

// 评论
const comments = ref([
  {
    id: 1,
    user: { id: 2, name: '张三', avatar: '' },
    content: '这里需要补充更多细节',
    time: new Date(Date.now() - 1000 * 60 * 30),
    replies: [
      { id: 11, user: { name: '李四' }, content: '好的，我会补充' }
    ],
    canDelete: false,
  },
])

// 协作者
const activeUsers = ref([
  { id: 1, name: '张三', avatar: '', isEditing: true },
  { id: 2, name: '李四', avatar: '', isEditing: false },
])

// 协作者光标
const collaboratorCursors = ref([
  { userId: 1, userName: '张三', x: 300, y: 150, color: '#ff6b6b' },
  { userId: 2, userName: '李四', x: 450, y: 280, color: '#4ecdc4' },
])

// 历史版本
const historyVersions = ref([
  {
    id: 1,
    user: '我',
    time: new Date(),
    content: '',
  },
  {
    id: 2,
    user: '张三',
    time: new Date(Date.now() - 1000 * 60 * 60 * 2),
    content: '',
  },
  {
    id: 3,
    user: '李四',
    time: new Date(Date.now() - 1000 * 60 * 60 * 24),
    content: '',
  },
])

let autoSaveTimer = null
let updateOutlineTimer = null

// 方法
const handleBack = () => {
  emit('back')
}

const startEditTitle = () => {
  editingTitle.value = true
  nextTick(() => {
    titleInputRef.value?.focus()
  })
}

const handleSaveTitle = () => {
  editingTitle.value = false
  handleAutoSave()
}

const handleInput = () => {
  document.value.saved = false
  handleAutoSave()
  debouncedUpdateOutline()
}

const handleAutoSave = () => {
  clearTimeout(autoSaveTimer)
  document.value.autoSaving = true

  autoSaveTimer = setTimeout(() => {
    saveDocument()
  }, 2000)
}

const saveDocument = async () => {
  try {
    const content = editorRef.value?.innerHTML || ''

    // TODO: 调用API保存文档
    // await updateDocument(props.documentId, { content })

    document.value.autoSaving = false
    document.value.saved = true
    document.value.content = content

    emit('save', {
      id: document.value.id,
      title: document.value.title,
      content: content,
    })
  } catch (error) {
    console.error('保存失败:', error)
    document.value.autoSaving = false
  }
}

const updateOutline = () => {
  const editor = editorRef.value
  if (!editor) return

  const headings = editor.querySelectorAll('h1, h2, h3, h4')
  outline.value = Array.from(headings).map((h, index) => ({
    id: `heading-${index}`,
    text: h.textContent,
    level: parseInt(h.tagName.charAt(1)),
  }))
}

const debouncedUpdateOutline = () => {
  clearTimeout(updateOutlineTimer)
  updateOutlineTimer = setTimeout(updateOutline, 500)
}

const scrollToHeading = (id) => {
  // 滚动到指定标题
  ElMessage.info('滚动功能开发中')
}

const handleKeydown = (e) => {
  // 快捷键处理
  if (e.ctrlKey || e.metaKey) {
    switch (e.key.toLowerCase()) {
      case 'b':
        e.preventDefault()
        handleBold()
        break
      case 'i':
        e.preventDefault()
        handleItalic()
        break
      case 'u':
        e.preventDefault()
        handleUnderline()
        break
      case 's':
        e.preventDefault()
        saveDocument()
        break
    }
  }

  // Tab键处理
  if (e.key === 'Tab') {
    e.preventDefault()
    document.execCommand('insertHTML', false, '&nbsp;&nbsp;&nbsp;&nbsp;')
  }
}

const handleSelectionChange = () => {
  const selection = window.getSelection()
  selectionText.value = selection.toString().trim()

  if (selectionText.value) {
    const range = selection.getRangeAt(0)
    const rect = range.getBoundingClientRect()
    floatToolbarStyle.value = {
      left: rect.left + rect.width / 2 - 80 + 'px',
      top: rect.top - 50 + window.scrollY + 'px',
    }
    showFloatToolbar.value = true
  } else {
    showFloatToolbar.value = false
  }

  // 更新格式状态
  updateFormatState()
}

const updateFormatState = () => {
  isBold.value = document.queryCommandState('bold')
  isItalic.value = document.queryCommandState('italic')
  isUnderline.value = document.queryCommandState('underline')
}

// 工具栏操作
const handleUndo = () => document.execCommand('undo')
const handleRedo = () => document.execCommand('redo')

const handleFormatChange = () => {
  document.execCommand('formatBlock', false, formatType.value)
}

const handleBold = () => document.execCommand('bold')
const handleItalic = () => document.execCommand('italic')
const handleUnderline = () => document.execCommand('underline')
const handleStrikeThrough = () => document.execCommand('strikeThrough')

const handleUnorderedList = () => document.execCommand('insertUnorderedList')
const handleOrderedList = () => document.execCommand('insertOrderedList')
const handleTaskList = () => {
  document.execCommand('insertHTML', false,
    '<div class="task-list"><input type="checkbox"> <span>待办事项</span></div>'
  )
}

const handleInsertLink = () => {
  selectionText.value = window.getSelection().toString()
  linkText.value = selectionText.value || '链接文字'
  linkUrl.value = ''
  linkDialogVisible.value = true
}

const handleConfirmLink = () => {
  if (!linkUrl.value) {
    ElMessage.warning('请输入链接地址')
    return
  }

  const html = `<a href="${linkUrl.value}" target="_blank">${linkText.value}</a>`
  document.execCommand('insertHTML', false, html)
  linkDialogVisible.value = false
}

const handleInsertImage = () => {
  // 触发图片上传
  ElMessage.info('图片上传功能开发中')
}

const handleInsertTable = () => {
  const html = `
    <table border="1" style="border-collapse: collapse; width: 100%;">
      <tr><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr>
      <tr><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr>
      <tr><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr>
    </table>
  `
  document.execCommand('insertHTML', false, html)
}

const handleInsertCode = () => {
  const html = `<pre style="background: #f5f5f5; padding: 12px; border-radius: 4px;"><code>代码内容</code></pre>`
  document.execCommand('insertHTML', false, html)
}

const handleInsertFormula = () => {
  ElMessage.info('公式编辑器开发中')
}

const handleInsertDate = () => {
  const now = new Date()
  const dateStr = `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`
  document.execCommand('insertText', false, dateStr)
}

const handleInsertDivider = () => {
  document.execCommand('insertHTML', false, '<hr style="margin: 16px 0;">')
}

const handleTextColor = () => {
  document.execCommand('foreColor', false, textColor.value)
}

const handleBgColor = () => {
  document.execCommand('hiliteColor', false, bgColor.value)
}

const toggleOutline = () => {
  showOutline.value = !showOutline.value
  if (showOutline.value) {
    sidebarTab.value = 'outline'
  }
}

const toggleFullScreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

// 评论相关
const handleAddComment = () => {
  selectedTextForComment.value = selectionText.value
  newComment.value = ''
  commentDialogVisible.value = true
}

const handleSubmitComment = () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  comments.value.unshift({
    id: Date.now(),
    user: { id: props.userId, name: '我', avatar: '' },
    content: newComment.value,
    time: new Date(),
    replies: [],
    canDelete: true,
  })

  ElMessage.success('评论已发布')
  commentDialogVisible.value = false
}

const handleReplyComment = (comment) => {
  currentReplyComment.value = comment
  newComment.value = ''
  commentDialogVisible.value = true
}

const handleDeleteComment = (comment) => {
  const index = comments.value.findIndex(c => c.id === comment.id)
  if (index > -1) {
    comments.value.splice(index, 1)
    ElMessage.success('评论已删除')
  }
}

const handleHighlight = () => {
  document.execCommand('hiliteColor', false, '#fff4e6')
}

const handleMoreAction = (command) => {
  switch (command) {
    case 'share':
      ElMessage.info('分享功能开发中')
      break
    case 'export':
      handleExport()
      break
    case 'print':
      window.print()
      break
    case 'settings':
      ElMessage.info('文档设置功能开发中')
      break
  }
}

const handleExport = () => {
  // 导出文档
  const content = editorRef.value?.innerHTML || ''
  const blob = new Blob([content], { type: 'text/html' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${document.value.title}.html`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('文档已导出')
}

const handleSelectVersion = (version) => {
  selectedVersionId.value = version.id
}

const handlePreviewVersion = (version) => {
  ElMessage.info('预览功能开发中')
}

const handleRestoreVersion = (version) => {
  ElMessageBox.confirm(
    `确定要恢复到 ${formatTime(version.time)} 的版本吗？`,
    '恢复版本',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    ElMessage.success('版本已恢复')
    showHistory.value = false
  }).catch(() => {})
}

const formatTime = (time) => {
  return formatRelativeTime(time)
}

// 生命周期
onMounted(() => {
  // 初始化编辑器内容
  if (document.value.content) {
    editorRef.value.innerHTML = document.value.content
  }

  // 连接WebSocket实现协作编辑
  // connectDocumentWebSocket()
})

onUnmounted(() => {
  clearTimeout(autoSaveTimer)
  clearTimeout(updateOutlineTimer)

  // 断开WebSocket
  // disconnectDocumentWebSocket()
})
</script>

<style lang="scss" scoped>
.document-editor {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f5f7fa;
}

// 编辑器头部
.editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  background: white;
  border-bottom: 1px solid var(--el-border-color-light);
  gap: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
}

.doc-title-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.doc-title {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  .el-icon {
    opacity: 0;
    transition: opacity 0.2s;
  }

  &:hover .el-icon {
    opacity: 1;
  }
}

.doc-title-input {
  :deep(.el-input__wrapper) {
    padding: 0;
    box-shadow: none;
    font-size: 18px;
    font-weight: 500;
  }
}

.saving-indicator,
.saved-indicator {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  white-space: nowrap;
}

.header-center {
  display: flex;
  align-items: center;
}

.collaborator-avatars {
  display: flex;
  align-items: center;

  .el-avatar {
    margin-left: -8px;
    border: 2px solid white;
    transition: transform 0.2s;

    &:first-child {
      margin-left: 0;
    }

    &.is-editing {
      animation: pulse 1.5s infinite;
    }
  }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

// 编辑器主体
.editor-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

// 侧边栏
.editor-sidebar {
  width: 300px;
  background: white;
  border-left: 1px solid var(--el-border-color-light);
  display: flex;
  flex-direction: column;

  :deep(.el-tabs--border-card) {
    border: none;
    box-shadow: none;
  }

  :deep(.el-tabs__content) {
    padding: 0;
    height: calc(100% - 40px);
    overflow-y: auto;
  }
}

.outline-list {
  padding: 12px;
}

.outline-item {
  padding: 6px 12px;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.2s;
  font-size: 14px;
  color: var(--el-text-color-secondary);

  &:hover {
    background: var(--el-fill-color-light);
  }

  &.level-1 {
    padding-left: 12px;
    font-weight: 500;
  }

  &.level-2 {
    padding-left: 24px;
  }

  &.level-3 {
    padding-left: 36px;
    font-size: 13px;
  }

  &.level-4 {
    padding-left: 48px;
    font-size: 13px;
  }
}

.comments-list {
  padding: 12px;
}

.comment-item {
  padding: 12px;
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
  margin-bottom: 12px;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.comment-user {
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.comment-time {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

.comment-content {
  font-size: 14px;
  color: var(--el-text-color-primary);
  line-height: 1.6;
  margin-bottom: 8px;
}

.comment-replies {
  padding-left: 12px;
  border-left: 2px solid var(--el-border-color-light);
  margin-bottom: 8px;
}

.reply-item {
  padding: 4px 0;
  font-size: 13px;
}

.reply-user {
  font-weight: 500;
  color: var(--el-text-color-secondary);
}

.reply-content {
  color: var(--el-text-color-primary);
}

.comment-actions {
  display: flex;
  gap: 8px;
}

.empty-comments {
  text-align: center;
  padding: 40px 20px;
  color: var(--el-text-color-placeholder);

  p {
    margin: 8px 0;
    font-size: 14px;
  }

  .tip {
    font-size: 12px;
  }
}

// 编辑器容器
.editor-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
  margin: 0 20px 20px 20px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
}

// 工具栏
.editor-toolbar {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  border-bottom: 1px solid var(--el-border-color-light);
  flex-wrap: wrap;
  gap: 4px;
}

.toolbar-group {
  display: flex;
  align-items: center;
  gap: 2px;
}

.toolbar-divider {
  width: 1px;
  height: 20px;
  background: var(--el-border-color-light);
  margin: 0 4px;
}

.toolbar-spacer {
  flex: 1;
}

// 编辑区域
.editor-content {
  flex: 1;
  padding: 40px 60px;
  overflow-y: auto;
  font-size: 15px;
  line-height: 1.8;
  color: var(--el-text-color-primary);

  &:focus {
    outline: none;
  }

  // 编辑器内样式
  h1 {
    font-size: 28px;
    font-weight: 600;
    margin: 24px 0 16px 0;
  }

  h2 {
    font-size: 24px;
    font-weight: 600;
    margin: 20px 0 14px 0;
  }

  h3 {
    font-size: 20px;
    font-weight: 600;
    margin: 16px 0 12px 0;
  }

  h4 {
    font-size: 18px;
    font-weight: 600;
    margin: 14px 0 10px 0;
  }

  p {
    margin: 8px 0;
  }

  ul, ol {
    padding-left: 24px;
    margin: 8px 0;
  }

  blockquote {
    border-left: 4px solid var(--el-border-color);
    padding-left: 16px;
    margin: 16px 0;
    color: var(--el-text-color-secondary);
  }

  code {
    background: var(--el-fill-color-light);
    padding: 2px 6px;
    border-radius: 4px;
    font-family: 'Monaco', 'Courier New', monospace;
    font-size: 14px;
  }

  pre {
    background: #f5f5f5;
    padding: 12px;
    border-radius: 4px;
    overflow-x: auto;
    margin: 16px 0;
  }

  table {
    border-collapse: collapse;
    width: 100%;
    margin: 16px 0;

    td, th {
      border: 1px solid var(--el-border-color-light);
      padding: 8px 12px;
      min-width: 60px;
    }
  }

  a {
    color: var(--el-color-primary);
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}

.task-list {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 4px 0;

  input[type="checkbox"] {
    cursor: pointer;
  }
}

// 浮动工具栏
.float-toolbar {
  position: fixed;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  padding: 4px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

// 协作者光标
.collaborator-cursor {
  position: absolute;
  width: 2px;
  height: 20px;
  border-left: 2px solid;
  pointer-events: none;
  z-index: 100;

  .cursor-label {
    position: absolute;
    top: -20px;
    left: 0;
    padding: 2px 6px;
    border-radius: 4px;
    color: white;
    font-size: 11px;
    white-space: nowrap;
  }
}

// 历史版本
.history-list {
  max-height: 400px;
  overflow-y: auto;
}

.history-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: var(--el-fill-color-light);
  }

  &.active {
    background: var(--el-color-primary-light-9);
  }
}

.version-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.version-user {
  font-size: 14px;
  font-weight: 500;
}

.version-time {
  font-size: 13px;
  color: var(--el-text-color-placeholder);
}

.version-actions {
  display: flex;
  gap: 8px;
}

// 评论对话框
.comment-dialog {
  .selected-text {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px;
    background: var(--el-fill-color-lighter);
    border-radius: 8px;
    margin-bottom: 16px;
    font-size: 14px;
    color: var(--el-text-color-secondary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

@media (max-width: 768px) {
  .editor-header {
    flex-wrap: wrap;

    .header-left {
      width: 100%;
    }
  }

  .editor-content {
    padding: 20px;
  }

  .editor-sidebar {
    position: fixed;
    right: 0;
    top: 0;
    height: 100%;
    z-index: 1000;
    box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
  }
}
</style>
