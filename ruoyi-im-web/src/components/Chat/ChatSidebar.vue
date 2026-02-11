<template>
  <div
    class="chat-sidebar"
    :class="{ 'with-tabs': isGroup }"
  >
    <!-- Tab 导航（仅群聊显示） -->
    <div
      v-if="isGroup"
      class="sidebar-tabs"
    >
      <div
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-item"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        <span class="material-icons-outlined tab-icon">{{ tab.icon }}</span>
        <span class="tab-label">{{ tab.label }}</span>
        <span
          v-if="tab.badge"
          class="tab-badge"
        >{{ tab.badge }}</span>
      </div>
    </div>

    <!-- 简单头部（单聊显示） -->
    <div
      v-else
      class="sidebar-header"
    >
      <h3 class="title">
        个人资料
      </h3>
      <el-button
        link
        @click="$emit('close')"
      >
        <el-icon>
          <Close />
        </el-icon>
      </el-button>
    </div>

    <!-- Tab 内容区域 -->
    <div
      v-loading="loading"
      class="sidebar-content"
    >
      <!-- ========== 成员 Tab ========== -->
      <template v-if="activeTab === 'members'">
        <div
          v-if="isGroup && detail"
          class="tab-content"
        >
          <!-- 群组信息 -->
          <div class="section group-info">
            <el-avatar
              :size="64"
              :src="addTokenToUrl(detail.avatar)"
              shape="square"
              class="avatar"
            >
              {{ detail.name?.charAt(0) }}
            </el-avatar>
            <div class="info">
              <div class="name">
                {{ detail.name }}
              </div>
              <div class="id">
                群号: {{ detail.id }}
              </div>
            </div>
          </div>

          <el-divider />

          <!-- 成员列表 -->
          <div class="section members-section">
            <div class="section-title">
              <span>群成员 ({{ detail.memberCount || members.length }})</span>
              <el-button
                link
                type="primary"
                @click="handleShowAllMembers"
              >
                查看全部
                <el-icon class="el-icon--right">
                  <ArrowRight />
                </el-icon>
              </el-button>
            </div>
            <div class="members-grid">
              <el-dropdown
                v-for="m in displayMembers"
                :key="m.id"
                trigger="click"
                placement="right-start"
                @command="handleMemberCommand"
              >
                <div
                  class="member-item"
                  :class="{ 'is-admin': m.role === 'ADMIN' || m.role === 'OWNER' }"
                >
                  <DingtalkAvatar
                    :name="m.name"
                    :user-id="m.id"
                    :src="addTokenToUrl(m.avatar)"
                    :size="40"
                    shape="square"
                  />
                  <span class="m-name">{{ m.name }}</span>
                  <span
                    v-if="m.role === 'OWNER'"
                    class="role-badge owner"
                  >群主</span>
                  <span
                    v-else-if="m.role === 'ADMIN'"
                    class="role-badge admin"
                  >管理员</span>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :command="{ action: 'chat', member: m }">
                      <span class="material-icons-outlined menu-icon">chat_bubble</span>
                      发消息
                    </el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'mention', member: m }">
                      <span class="material-icons-outlined menu-icon">alternate_email</span>
                      @提及
                    </el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'profile', member: m }">
                      <span class="material-icons-outlined menu-icon">person</span>
                      查看资料
                    </el-dropdown-item>
                    <el-dropdown-item
                      v-if="canManageMember && m.role !== 'OWNER'"
                      divided
                      :command="{ action: 'setAdmin', member: m }"
                    >
                      <span class="material-icons-outlined menu-icon">{{ m.role === 'ADMIN' ? 'remove_moderator' :
                        'admin_panel_settings' }}</span>
                      {{ m.role === 'ADMIN' ? '取消管理员' : '设为管理员' }}
                    </el-dropdown-item>
                    <el-dropdown-item
                      v-if="canManageMember && m.role !== 'OWNER'"
                      divided
                      :command="{ action: 'remove', member: m }"
                    >
                      <span class="material-icons-outlined menu-icon remove">person_remove</span>
                      移除出群
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <div
                v-if="canManageMember"
                class="member-item add-btn"
                @click="handleAddMember"
              >
                <div class="add-icon">
                  <el-icon>
                    <Plus />
                  </el-icon>
                </div>
                <span class="m-name">添加</span>
              </div>
            </div>
          </div>

          <el-divider />

          <!-- 群公告 -->
          <div class="section">
            <div class="section-title">
              群公告
            </div>
            <div
              class="announcement-card"
              :class="{ empty: !detail.announcement }"
            >
              {{ detail.announcement || '暂无群公告' }}
            </div>
          </div>

          <el-divider />

          <!-- 群设置预览 -->
          <div class="section settings">
            <div class="setting-item">
              <span>置顶聊天</span>
              <el-switch
                v-model="detail.isPinned"
                size="small"
                @change="handleSettingChange('pin', $event)"
              />
            </div>
            <div class="setting-item">
              <span>消息免打扰</span>
              <el-switch
                v-model="detail.isMuted"
                size="small"
                @change="handleSettingChange('mute', $event)"
              />
            </div>
          </div>
        </div>

        <!-- 单聊详情 -->
        <div
          v-else-if="detail"
          class="tab-content"
        >
          <div class="section user-info">
            <DingtalkAvatar
              :name="detail.nickname || detail.username"
              :user-id="detail.id"
              :src="addTokenToUrl(detail.avatar)"
              :size="80"
              shape="square"
              class="avatar-large"
            />
            <div class="user-main">
              <div class="name-row">
                <span class="nickname">{{ detail.nickname || detail.username }}</span>
                <span
                  v-if="detail.gender === 1"
                  class="material-icons-outlined male"
                >male</span>
                <span
                  v-else-if="detail.gender === 2"
                  class="material-icons-outlined female"
                >female</span>
              </div>
              <div class="sub">
                账号: {{ detail.username }}
              </div>
              <div
                class="status"
                :class="{ online: isOnline }"
              >
                <span class="dot" />
                {{ isOnline ? '在线' : '离线' }}
              </div>
            </div>
          </div>

          <el-divider />

          <div class="section info-list">
            <div class="info-item">
              <span class="label">职位</span>
              <span class="value">{{ detail.position || '暂无' }}</span>
            </div>
            <div class="info-item">
              <span class="label">部门</span>
              <span class="value">{{ detail.departmentName || detail.department || '未分配' }}</span>
            </div>
            <div class="info-item">
              <span class="label">手机</span>
              <span class="value">{{ detail.mobile || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">邮箱</span>
              <span class="value">{{ detail.email || '-' }}</span>
            </div>
          </div>
        </div>
      </template>

      <!-- ========== 文件 Tab ========== -->
      <template v-else-if="activeTab === 'files'">
        <div class="tab-content files-content">
          <div class="section">
            <div class="section-title">
              <span>聊天文件</span>
              <el-input
                v-model="fileSearchKeyword"
                placeholder="搜索文件"
                prefix-icon="Search"
                size="small"
                style="width: 160px"
                clearable
              />
            </div>

            <!-- 文件分类 -->
            <div class="file-categories">
              <div
                v-for="cat in fileCategories"
                :key="cat.key"
                class="file-category"
                :class="{ active: fileCategory === cat.key }"
                @click="fileCategory = cat.key"
              >
                <span class="material-icons-outlined">{{ cat.icon }}</span>
                <span>{{ cat.label }}</span>
              </div>
            </div>

            <!-- 文件列表 -->
            <div
              v-loading="loadingFiles"
              class="file-list"
            >
              <div
                v-if="filteredFiles.length === 0"
                class="empty-state"
              >
                <span class="material-icons-outlined empty-icon">folder_open</span>
                <p>暂无文件</p>
              </div>
              <div
                v-for="file in filteredFiles"
                :key="file.id"
                class="file-item"
                @click="handleFileClick(file)"
              >
                <div
                  class="file-icon"
                  :class="`type-${file.type}`"
                >
                  <span class="material-icons-outlined">{{ getFileIcon(file.type) }}</span>
                </div>
                <div class="file-info">
                  <div class="file-name">
                    {{ file.name }}
                  </div>
                  <div class="file-meta">
                    {{ utilsFormatFileSize(file.size) }} · {{ formatDate(file.sendTime) }}
                  </div>
                </div>
                <el-dropdown
                  trigger="click"
                  @command="(cmd) => handleFileCommand(cmd, file)"
                >
                  <el-button
                    link
                    class="file-more"
                  >
                    <el-icon>
                      <MoreFilled />
                    </el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="download">
                        <span class="material-icons-outlined menu-icon">download</span>
                        下载
                      </el-dropdown-item>
                      <el-dropdown-item command="open">
                        <span class="material-icons-outlined menu-icon">open_in_new</span>
                        打开
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- ========== 搜索 Tab ========== -->
      <template v-else-if="activeTab === 'search'">
        <div class="tab-content search-content">
          <div class="section">
            <!-- 搜索框 -->
            <div class="search-box">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索聊天记录"
                prefix-icon="Search"
                size="large"
                clearable
                @keyup.enter="handleSearch"
              >
                <template #append>
                  <el-button
                    :icon="Search"
                    @click="handleSearch"
                  />
                </template>
              </el-input>
            </div>

            <!-- 搜索结果 -->
            <div
              v-loading="searching"
              class="search-results"
            >
              <div
                v-if="!searchKeyword"
                class="search-hints"
              >
                <div class="hint-title">
                  搜索提示
                </div>
                <div class="hint-list">
                  <div class="hint-item">
                    <span class="material-icons-outlined">text_fields</span>
                    <span>输入关键词搜索消息内容</span>
                  </div>
                  <div class="hint-item">
                    <span class="material-icons-outlined">person</span>
                    <span>搜索 @好友 查找相关消息</span>
                  </div>
                  <div class="hint-item">
                    <span class="material-icons-outlined">schedule</span>
                    <span>使用日期筛选查找历史消息</span>
                  </div>
                </div>
              </div>

              <div
                v-else-if="searchResults.length === 0 && !searching"
                class="empty-state"
              >
                <span class="material-icons-outlined empty-icon">search_off</span>
                <p>未找到相关结果</p>
              </div>

              <div
                v-else
                class="result-list"
              >
                <div
                  v-for="(result, index) in searchResults"
                  :key="result.id"
                  class="result-item"
                  @click="handleResultClick(result)"
                >
                  <div class="result-sender">
                    <DingtalkAvatar
                      :name="result.senderName"
                      :src="result.senderAvatar"
                      :size="28"
                    />
                    <span class="sender-name">{{ result.senderName }}</span>
                    <span class="result-time">{{ formatTime(result.sendTime) }}</span>
                  </div>
                  <div
                    class="result-content"
                    v-html="highlightKeyword(result.content)"
                  />
                </div>
                <div
                  v-if="hasMoreResults"
                  class="load-more"
                >
                  <el-button
                    text
                    @click="loadMoreResults"
                  >
                    加载更多
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- 底部操作区域 -->
    <div class="sidebar-footer">
      <el-button
        v-if="isGroup && activeTab === 'members'"
        type="danger"
        plain
        class="w-full"
        @click="handleLeave"
      >
        退出群聊
      </el-button>
      <el-button
        v-else-if="!isGroup"
        type="primary"
        plain
        class="w-full"
        @click="$emit('start-chat')"
      >
        发起聊天
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Close, Plus, ArrowRight, Search, MoreFilled } from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import { getGroup, getGroupMembers, leaveGroup, removeGroupMember, addGroupMembers, setGroupAdmin } from '@/api/im/group'
import { getUserInfo } from '@/api/im/user'
import { searchMessages } from '@/api/im/message'
import { addTokenToUrl } from '@/utils/file'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { formatFileSize as utilsFormatFileSize, formatDateTimeISO } from '@/utils/format'
import { parseMessageContent } from '@/utils/message'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const store = useStore()
const currentUser = computed(() => store.getters['user/currentUser'])

const props = defineProps({
  session: { type: Object, required: true },
  messages: { type: Array, default: () => [] }
})

const emit = defineEmits(['close', 'member-click', 'start-chat', 'mention-member', 'show-profile', 'show-all-members', 'scroll-to-message', 'setting-change'])

// 加载状态
const loading = ref(false)
const loadingFiles = ref(false)
const searching = ref(false)

// 数据
const detail = ref(null)
const members = ref([])

// Tab 状态
const isGroup = computed(() => props.session?.type === 'GROUP')
const activeTab = ref('members')

// Tab 配置
const tabs = computed(() => {
  const baseTabs = [
    { key: 'members', label: '成员', icon: 'people' },
    { key: 'files', label: '文件', icon: 'folder' },
    { key: 'search', label: '搜索', icon: 'search' }
  ]
  return baseTabs
})

// 显示成员（取前12个）
const displayMembers = computed(() => {
  return members.value.slice(0, 12)
})

// 在线状态
const isOnline = computed(() => {
  if (isGroup.value) { return false }
  const userId = props.session?.targetId
  if (userId && store.state.im.contact?.userStatus?.[userId]) {
    return store.state.im.contact.userStatus[userId] === 'online'
  }
  return props.session?.peerOnline ?? false
})

// 判断是否有管理成员的权限
const canManageMember = computed(() => {
  if (!detail.value || !currentUser.value) { return false }
  const memberRole = props.session?.memberRole
  return memberRole === 'OWNER' || memberRole === 'ADMIN'
})

// ========== 文件相关 ==========
const fileSearchKeyword = ref('')
const fileCategory = ref('all')
const files = ref([])

const fileCategories = [
  { key: 'all', label: '全部', icon: 'folder_open' },
  { key: 'image', label: '图片', icon: 'image' },
  { key: 'document', label: '文档', icon: 'description' },
  { key: 'video', label: '视频', icon: 'videocam' },
  { key: 'audio', label: '音频', icon: 'mic' },
  { key: 'other', label: '其他', icon: 'insert_drive_file' }
]

const filteredFiles = computed(() => {
  let result = files.value

  // 分类筛选
  if (fileCategory.value !== 'all') {
    result = result.filter(f => f.category === fileCategory.value)
  }

  // 关键词搜索
  if (fileSearchKeyword.value) {
    const keyword = fileSearchKeyword.value.toLowerCase()
    result = result.filter(f => f.name.toLowerCase().includes(keyword))
  }

  return result
})

// ========== 搜索相关 ==========
const searchKeyword = ref('')
const searchResults = ref([])
const hasMoreResults = ref(false)
const searchPage = ref(1)

// ========== 加载详情 ==========
const loadDetail = async () => {
  if (!props.session?.id) { return }
  const targetSessionId = props.session.id
  loading.value = true
  try {
    if (isGroup.value) {
      const gId = props.session.targetId || props.session.id
      const [gRes, mRes] = await Promise.all([
        getGroup(gId),
        getGroupMembers(gId)
      ])
      // 竞态守卫：异步期间会话已切换，丢弃过期结果
      if (props.session?.id !== targetSessionId) { return }
      if (gRes.code === 200) { detail.value = gRes.data }
      if (mRes.code === 200) {
        members.value = mRes.data.map(m => ({
          id: m.userId || m.id,
          name: m.userName || m.name || '未知',
          avatar: m.avatar || '',
          role: m.role || 'MEMBER'
        }))
      }
      // 加载群文件
      loadFiles()
    } else {
      const res = await getUserInfo(props.session.targetId)
      // 竞态守卫
      if (props.session?.id !== targetSessionId) { return }
      if (res.code === 200) { detail.value = res.data }
    }
  } catch (e) {
    if (props.session?.id !== targetSessionId) { return }
    console.error('加载详情失败', e)
    ElMessage.error('加载详情失败')
  } finally {
    if (props.session?.id === targetSessionId) {
      loading.value = false
    }
  }
}

// ========== 加载文件 ==========
const loadFiles = () => {
  // 从 messages 中提取文件
  const fileList = props.messages
    .filter(m => m.messageType === 'IMAGE' || m.messageType === 'FILE')
    .map(m => {
      const content = parseMessageContent(m) || {}
      return {
        id: m.id,
        name: content.fileName || content.name || '未命名文件',
        size: content.fileSize || 0,
        type: content.fileType || getFileType(m.messageType),
        category: getFileCategory(m.messageType),
        url: content.url || content.fileUrl,
        sendTime: m.sendTime || m.createTime,
        senderId: m.senderId,
        senderName: m.senderName
      }
    })
  files.value = fileList
}

const getFileType = messageType => {
  const typeMap = { IMAGE: 'image', FILE: 'file' }
  return typeMap[messageType] || 'file'
}

const getFileCategory = messageType => {
  const catMap = { IMAGE: 'image', FILE: 'document' }
  return catMap[messageType] || 'other'
}

const getFileIcon = type => {
  const iconMap = {
    image: 'image',
    pdf: 'picture_as_pdf',
    doc: 'description',
    docx: 'description',
    xls: 'table_chart',
    xlsx: 'table_chart',
    ppt: 'slideshow',
    pptx: 'slideshow',
    video: 'videocam',
    audio: 'audiotrack',
    zip: 'archive',
    default: 'insert_drive_file'
  }
  return iconMap[type] || iconMap.default
}

const formatDate = formatDateTimeISO

const formatTime = date => {
  return dayjs(date).fromNow()
}

// ========== 搜索功能 ==========
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    searchResults.value = []
    return
  }

  searching.value = true
  searchPage.value = 1

  try {
    const res = await searchMessages({
      conversationId: props.session.id,
      keyword: searchKeyword.value,
      page: 1,
      size: 20
    })

    if (res.code === 200) {
      searchResults.value = res.data?.list || []
      hasMoreResults.value = res.data?.hasMore || false
    }
  } catch (e) {
    console.error('搜索失败', e)
    ElMessage.error('搜索失败')
  } finally {
    searching.value = false
  }
}

const loadMoreResults = async () => {
  if (!hasMoreResults.value) { return }

  try {
    const res = await searchMessages({
      conversationId: props.session.id,
      keyword: searchKeyword.value,
      page: ++searchPage.value,
      size: 20
    })

    if (res.code === 200) {
      searchResults.value.push(...(res.data?.list || []))
      hasMoreResults.value = res.data?.hasMore || false
    }
  } catch (e) {
    console.error('加载更多失败', e)
  }
}

/**
 * 高亮搜索关键词
 * 先转义 HTML 防止 XSS，再进行关键词高亮
 */
const highlightKeyword = content => {
  if (!searchKeyword.value) { return escapeHtml(content) }
  const keyword = searchKeyword.value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const regex = new RegExp(`(${keyword})`, 'gi')
  return escapeHtml(content).replace(regex, '<mark>$1</mark>')
}

/**
 * 转义 HTML 特殊字符，防止 XSS 攻击
 */
const escapeHtml = text => {
  if (!text) { return '' }
  const div = document.createElement('div')
  div.textContent = text
  return div.innerHTML
}

const handleResultClick = result => {
  emit('scroll-to-message', result.id)
}

// ========== 成员操作 ==========
const handleShowAllMembers = () => {
  emit('show-all-members')
}

const handleAddMember = async () => {
  try {
    const { value } = await ElMessageBox.prompt(
      '请输入要邀请的用户ID（多个用户用逗号分隔）',
      '邀请成员',
      {
        confirmButtonText: '邀请',
        cancelButtonText: '取消',
        inputPattern: /^(\d+)(,\s*\d+)*$/,
        inputErrorMessage: '请输入有效的用户ID，多个用逗号分隔'
      }
    )

    if (!value) { return }

    const userIds = value.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id))
    if (userIds.length === 0) {
      ElMessage.warning('请输入有效的用户ID')
      return
    }

    const gId = props.session.targetId || props.session.id
    await addGroupMembers(gId, userIds)
    ElMessage.success(`已邀请 ${userIds.length} 位成员`)
    loadDetail()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('邀请失败，请重试')
    }
  }
}

const handleMemberCommand = command => {
  const { action, member } = command
  switch (action) {
    case 'chat':
      emit('member-click', member)
      break
    case 'mention':
      emit('mention-member', member)
      break
    case 'profile':
      emit('show-profile', member)
      break
    case 'setAdmin':
      handleSetAdmin(member)
      break
    case 'remove':
      handleRemoveMember(member)
      break
  }
}

const handleSetAdmin = async member => {
  const newRole = member.role === 'ADMIN' ? 'MEMBER' : 'ADMIN'
  const action = newRole === 'ADMIN' ? '设为管理员' : '取消管理员'
  try {
    await ElMessageBox.confirm(`确定要${action} ${member.name} 吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await setGroupAdmin(detail.value.id, member.userId, newRole === 'ADMIN')
    ElMessage.success(`${action}成功`)
    loadDetail()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleRemoveMember = async member => {
  try {
    await ElMessageBox.confirm(`确定将 ${member.name} 移除出群吗？`, '移除成员', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    const gId = props.session.targetId || props.session.id
    await removeGroupMember(gId, [member.id])
    ElMessage.success('已移除成员')
    loadDetail()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('移除失败，请重试')
    }
  }
}

// ========== 文件操作 ==========
const handleFileClick = file => {
  // 打开或预览文件
  if (file.url) {
    window.open(file.url, '_blank')
  }
}

const handleFileCommand = (command, file) => {
  switch (command) {
    case 'download':
      if (file.url) {
        const link = document.createElement('a')
        link.href = file.url
        link.download = file.name
        link.click()
      }
      break
    case 'open':
      handleFileClick(file)
      break
  }
}

// ========== 设置变更 ==========
const handleSettingChange = (type, value) => {
  emit('setting-change', { type, value, session: props.session })
}

// ========== 退出群聊 ==========
const handleLeave = async () => {
  try {
    await ElMessageBox.confirm('确定退出群组吗？', '提示', { type: 'warning' })
    await leaveGroup(props.session.targetId || props.session.id)
    ElMessage.success('已退出')
    emit('close')
  } catch (e) {
    // 用户取消操作，无需处理
  }
}

// ========== 监听会话变化 ==========
watch(() => props.session?.id, () => {
  detail.value = null
  members.value = []
  files.value = []
  searchResults.value = []
  searchKeyword.value = ''
  activeTab.value = 'members'
  loadDetail()
}, { immediate: true })

// 监听消息变化更新文件列表
watch(() => props.messages, () => {
  if (activeTab.value === 'files') {
    loadFiles()
  }
}, { deep: true })
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.chat-sidebar {
  width: var(--dt-session-panel-width);
  height: 100%;
  border-left: 1px solid var(--dt-border-light);
  background: var(--dt-bg-card);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: all var(--dt-transition-base);
}

// Tab 导航
.sidebar-tabs {
  display: flex;
  padding: 0 8px;
  border-bottom: 1px solid var(--dt-border-light);
  background: var(--dt-bg-tertiary);

  .tab-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 4px;
    padding: 12px 8px;
    cursor: pointer;
    position: relative;
    transition: all var(--dt-transition-base);
    color: var(--dt-text-tertiary);

    .tab-icon {
      font-size: 20px;
      transition: transform var(--dt-transition-base);
    }

    .tab-label {
      font-size: 12px;
      font-weight: 500;
    }

    .tab-badge {
      position: absolute;
      top: 8px;
      right: 12px;
      min-width: 16px;
      height: 16px;
      padding: 0 4px;
      background: var(--dt-error-color);
      color: #fff;
      font-size: 10px;
      font-weight: 600;
      border-radius: var(--dt-radius-md);
      display: flex;
      align-items: center;
      justify-content: center;
    }

    &:hover {
      color: var(--dt-brand-color);
      background: var(--dt-bg-hover);

      .tab-icon {
        transform: scale(1.1);
      }
    }

    &.active {
      color: var(--dt-brand-color);

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 24px;
        height: 3px;
        background: var(--dt-brand-color);
        border-radius: var(--dt-radius-sm) var(--dt-radius-sm) 0 0;
      }
    }
  }
}

// 简单头部
.sidebar-header {
  height: 57px;
  padding: 0 16px;
  border-bottom: 1px solid var(--dt-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;

  .title {
    font-size: 15px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0;
  }
}

// 内容区域
.sidebar-content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 16px;

  .tab-content {
    animation: tabFadeIn 0.25s ease-out;
  }

  .section {
    margin-bottom: 24px;
  }

  .section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    font-size: 13px;
    font-weight: 600;
    color: var(--dt-text-primary);
  }
}

@keyframes tabFadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 群组信息
.group-info {
  display: flex;
  gap: 12px;
  align-items: center;

  .avatar {
    border-radius: var(--dt-radius-md);
  }

  .name {
    font-size: 16px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin-bottom: 4px;
  }

  .id {
    font-size: 12px;
    color: var(--dt-text-tertiary);
  }
}

// 成员网格
.members-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;

  .member-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    padding: 8px 4px;
    border-radius: var(--dt-radius-md);
    cursor: pointer;
    transition: all var(--dt-transition-base);
    position: relative;

    &:hover {
      background: var(--dt-bg-hover);
    }

    &.is-admin {
      .role-badge {
        display: flex;
      }
    }

    .m-name {
      font-size: 11px;
      color: var(--dt-text-secondary);
      max-width: 100%;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .role-badge {
      display: none;
      position: absolute;
      top: 4px;
      right: 4px;
      padding: 2px 4px;
      font-size: 9px;
      font-weight: 600;
      border-radius: var(--dt-radius-sm);

      &.owner {
        background: #ff6b6b;
        color: #fff;
      }

      &.admin {
        background: #3296FA;
        color: #fff;
      }
    }

    &.add-btn {
      .add-icon {
        width: 40px;
        height: 40px;
        border: 1px dashed var(--dt-border-color);
        border-radius: var(--dt-radius-sm);
        display: flex;
        align-items: center;
        justify-content: center;
        color: var(--dt-text-tertiary);
        transition: all var(--dt-transition-base);

        &:hover {
          border-color: var(--dt-brand-color);
          color: var(--dt-brand-color);
        }
      }
    }
  }
}

// 菜单图标样式
:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;

  .menu-icon {
    font-size: 18px;
    color: var(--dt-text-secondary);

    &.remove {
      color: var(--dt-error-color);
    }
  }

  &:hover .menu-icon {
    color: var(--dt-brand-color);
  }
}

// 群公告
.announcement-card {
  padding: 12px;
  background: var(--dt-bg-tertiary);
  border-radius: var(--dt-radius-md);
  font-size: 13px;
  color: var(--dt-text-primary);
  line-height: 1.6;

  &.empty {
    color: var(--dt-text-tertiary);
    font-style: italic;
  }
}

// 设置项
.settings {
  .setting-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 0;
    font-size: 14px;
    color: var(--dt-text-primary);
  }
}

// 用户信息
.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;

  .avatar-large {
    border-radius: var(--dt-radius-md);
  }

  .user-main {
    text-align: center;

    .name-row {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;

      .nickname {
        font-size: 18px;
        font-weight: 600;
        color: var(--dt-text-primary);
      }

      .male {
        color: var(--dt-brand-color);
      }

      .female {
        color: var(--dt-error-color);
      }
    }

    .sub {
      font-size: 12px;
      color: var(--dt-text-tertiary);
      margin-top: 4px;
    }

    .status {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      margin-top: 8px;
      font-size: 12px;
      color: var(--dt-text-tertiary);
      padding: 4px 12px;
      background: var(--dt-bg-tertiary);
      border-radius: var(--dt-radius-lg);

      .dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: var(--dt-text-quaternary);
      }

      &.online {
        color: var(--dt-success-color);
        background: var(--dt-success-dark-bg);

        .dot {
          background: var(--dt-success-color);
        }
      }
    }
  }
}

// 信息列表
.info-list {
  .info-item {
    display: flex;
    justify-content: space-between;
    padding: 12px 0;
    font-size: 14px;
    border-bottom: 1px solid var(--dt-border-lighter);

    &:last-child {
      border-bottom: none;
    }

    .label {
      color: var(--dt-text-tertiary);
    }

    .value {
      color: var(--dt-text-primary);
      text-align: right;
    }
  }
}

// ========== 文件 Tab 样式 ==========
.file-categories {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  overflow-x: auto;
  padding-bottom: 4px;

  .file-category {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    min-width: 56px;
    padding: 8px;
    border-radius: var(--dt-radius-md);
    cursor: pointer;
    transition: all var(--dt-transition-base);
    color: var(--dt-text-tertiary);

    .material-icons-outlined {
      font-size: 20px;
    }

    span {
      font-size: 11px;
    }

    &:hover {
      background: var(--dt-bg-hover);
      color: var(--dt-text-secondary);
    }

    &.active {
      background: var(--dt-brand-bg);
      color: var(--dt-brand-color);
    }
  }
}

.file-list {
  .empty-state {
    text-align: center;
    padding: 40px 20px;
    color: var(--dt-text-tertiary);

    .empty-icon {
      font-size: 48px;
      opacity: 0.5;
      margin-bottom: 12px;
    }
  }

  .file-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    border-radius: var(--dt-radius-md);
    cursor: pointer;
    transition: all var(--dt-transition-base);

    &:hover {
      background: var(--dt-bg-hover);
    }

    .file-icon {
      width: 40px;
      height: 40px;
      border-radius: var(--dt-radius-sm);
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--dt-bg-tertiary);
      color: var(--dt-brand-color);

      .material-icons-outlined {
        font-size: 24px;
      }

      &.type-image {
        background: #e6f7ff;
        color: var(--dt-brand-color);
      }

      &.type-pdf {
        background: #fff1f0;
        color: #ff4d4f;
      }

      &.type-doc {
        background: #f6ffed;
        color: #52c41a;
      }
    }

    .file-info {
      flex: 1;
      min-width: 0;

      .file-name {
        font-size: 13px;
        color: var(--dt-text-primary);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        margin-bottom: 2px;
      }

      .file-meta {
        font-size: 11px;
        color: var(--dt-text-tertiary);
      }
    }

    .file-more {
      color: var(--dt-text-tertiary);
      opacity: 0;
      transition: opacity var(--dt-transition-base);
    }

    &:hover .file-more {
      opacity: 1;
    }
  }
}

// ========== 搜索 Tab 样式 ==========
.search-box {
  margin-bottom: 20px;
}

.search-results {
  min-height: 200px;

  .search-hints {
    padding: 20px;

    .hint-title {
      font-size: 14px;
      font-weight: 600;
      color: var(--dt-text-primary);
      margin-bottom: 16px;
    }

    .hint-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .hint-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px;
      background: var(--dt-bg-tertiary);
      border-radius: var(--dt-radius-md);
      font-size: 13px;
      color: var(--dt-text-secondary);

      .material-icons-outlined {
        font-size: 20px;
        color: var(--dt-brand-color);
      }
    }
  }

  .empty-state {
    text-align: center;
    padding: 40px 20px;
    color: var(--dt-text-tertiary);

    .empty-icon {
      font-size: 48px;
      opacity: 0.5;
      margin-bottom: 12px;
    }
  }

  .result-list {
    .result-item {
      padding: 12px;
      border-radius: var(--dt-radius-md);
      cursor: pointer;
      transition: all var(--dt-transition-base);
      margin-bottom: 8px;

      &:hover {
        background: var(--dt-bg-hover);
      }

      .result-sender {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 6px;

        .sender-name {
          font-size: 12px;
          font-weight: 600;
          color: var(--dt-text-primary);
        }

        .result-time {
          margin-left: auto;
          font-size: 11px;
          color: var(--dt-text-tertiary);
        }
      }

      .result-content {
        font-size: 13px;
        color: var(--dt-text-secondary);
        line-height: 1.5;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;

        :deep(mark) {
          background: var(--dt-search-highlight-bg);
          color: var(--dt-search-highlight-text);
          padding: 0 2px;
          border-radius: var(--dt-radius-sm);
        }
      }
    }
  }

  .load-more {
    text-align: center;
    padding: 12px;
  }
}

// 底部操作区域
.sidebar-footer {
  padding: 16px;
  border-top: 1px solid var(--dt-border-light);
}

// ========== 暗色模式 ==========
.dark .chat-sidebar {
  background: var(--dt-bg-card-dark);
  border-color: var(--dt-border-dark);

  .sidebar-tabs {
    background: var(--dt-bg-tertiary-dark);
    border-bottom-color: var(--dt-border-dark);
  }

  .sidebar-header {
    border-bottom-color: var(--dt-border-dark);

    .title {
      color: var(--dt-text-primary-dark);
    }
  }

  .sidebar-content {
    .section-title {
      color: var(--dt-text-primary-dark);
    }
  }

  .group-info {
    .name {
      color: var(--dt-text-primary-dark);
    }

    .id {
      color: var(--dt-text-tertiary-dark);
    }
  }

  .announcement-card {
    background: var(--dt-bg-tertiary-dark);
    color: var(--dt-text-primary-dark);
  }

  .settings .setting-item {
    color: var(--dt-text-primary-dark);
  }

  .user-info {
    .user-main .name-row .nickname {
      color: var(--dt-text-primary-dark);
    }

    .user-main .sub {
      color: var(--dt-text-tertiary-dark);
    }
  }

  .info-list .info-item {
    border-bottom-color: var(--dt-border-dark);

    .label {
      color: var(--dt-text-tertiary-dark);
    }

    .value {
      color: var(--dt-text-primary-dark);
    }
  }

  .sidebar-footer {
    border-top-color: var(--dt-border-dark);
  }
}
</style>
