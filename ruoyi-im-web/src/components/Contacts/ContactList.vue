<template>
  <div class="contact-list-container">
    <div class="search-box">
      <el-input
        v-model="searchQuery"
        placeholder="搜索联系人"
        clearable
        :prefix-icon="Search"
      />
    </div>

    <div
      v-loading="loading"
      class="list-content"
    >
      <!-- 导航列表 -->
      <div
        v-if="!searchQuery"
        class="nav-list"
      >
        <div
          class="nav-item"
          @click="$emit('nav', 'new-friends')"
        >
          <el-badge
            :value="unreadRequestCount"
            :hidden="unreadRequestCount === 0"
            class="nav-badge"
          >
            <div class="nav-icon new-friends">
              <el-icon><User /></el-icon>
            </div>
          </el-badge>
          <span class="nav-label">新朋友</span>
        </div>
      </div>

      <el-collapse v-model="activeNames">
        <!-- 组织架构 -->
        <el-collapse-item
          v-if="!searchQuery"
          title="组织架构"
          name="org"
        >
          <el-tree
            :data="orgTree"
            :props="{ children: 'children', label: 'name' }"
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <span class="custom-tree-node">
                <el-icon v-if="data.type === 'dept'"><OfficeBuilding /></el-icon>
                <el-icon v-else><User /></el-icon>
                <span class="node-label">{{ node.label }}</span>
              </span>
            </template>
          </el-tree>
        </el-collapse-item>

        <!-- 我的群组 -->
        <el-collapse-item
          title="我的群组"
          name="groups"
        >
          <div
            v-for="group in filteredGroups"
            :key="group.id"
            class="friend-item"
            :class="{ active: currentContact?.id === group.id && currentContact?.isGroup }"
            @click="handleGroupClick(group)"
          >
            <el-avatar
              :size="36"
              :src="addTokenToUrl(group.avatar)"
              class="avatar"
            >
              {{ (group.name?.charAt(0) || '?').toUpperCase() }}
            </el-avatar>
            <div class="info">
              <div class="name">
                {{ group.name }}
              </div>
              <div class="desc">
                {{ group.memberCount || 0 }} 人
              </div>
            </div>
          </div>
          <div
            v-if="filteredGroups.length === 0"
            class="empty-small"
          >
            暂无群组
          </div>
        </el-collapse-item>

        <!-- 我的联系人 - 分组显示 -->
        <template v-if="!searchQuery">
          <el-collapse-item
            v-for="groupData in groupedContacts"
            :key="groupData.groupName"
            :name="`friends-${groupData.groupName}`"
          >
            <template #title>
              <div class="group-header">
                <span class="group-name">{{ groupData.groupName || '未分组' }}</span>
                <span class="group-count">({{ groupData.contacts.length }})</span>
                <el-dropdown
                  trigger="click"
                  @command="(cmd) => handleGroupCommand(cmd, groupData.groupName)"
                  @click.stop
                >
                  <el-icon
                    class="group-more"
                    @click.stop
                  >
                    <MoreFilled />
                  </el-icon>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="rename">
                        重命名分组
                      </el-dropdown-item>
                      <el-dropdown-item
                        v-if="groupData.groupName"
                        command="delete"
                      >
                        删除分组
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </template>
            <div
              v-for="contact in groupData.contacts"
              :key="contact.id"
              class="friend-item"
              :class="{ active: currentContact?.id === contact.id && !currentContact?.isGroup }"
              @click="handleContactClick(contact)"
              @contextmenu.prevent="showContactMenu($event, contact)"
            >
              <div class="avatar">
                <img
                  v-if="contact.friendAvatar"
                  :src="contact.friendAvatar"
                >
                <div
                  v-else
                  class="avatar-text"
                >
                  {{ (contact.friendName || '?').charAt(0).toUpperCase() }}
                </div>
              </div>
              <div class="info">
                <div class="name">
                  {{ contact.remark || contact.friendName }}
                </div>
                <div class="desc">
                  {{ contact.position || '联系人' }}
                </div>
              </div>
            </div>
            <div
              v-if="groupData.contacts.length === 0"
              class="empty-small"
            >
              暂无联系人
            </div>
          </el-collapse-item>
        </template>

        <!-- 搜索结果 -->
        <el-collapse-item
          v-else
          title="搜索结果"
          name="search"
        >
          <div
            v-for="contact in searchResults"
            :key="contact.id"
            class="friend-item"
            @click="handleContactClick(contact)"
          >
            <div class="avatar">
              <img
                v-if="contact.friendAvatar"
                :src="contact.friendAvatar"
              >
              <div
                v-else
                class="avatar-text"
              >
                {{ (contact.friendName || '?').charAt(0).toUpperCase() }}
              </div>
            </div>
            <div class="info">
              <div class="name">
                {{ contact.remark || contact.friendName }}
              </div>
              <div class="desc">
                {{ contact.groupName || '未分组' }}
              </div>
            </div>
          </div>
          <div
            v-if="searchResults.length === 0"
            class="empty-small"
          >
            未找到联系人
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>

    <!-- 分组重命名对话框 -->
    <el-dialog
      v-model="renameDialogVisible"
      title="重命名分组"
      width="400px"
      :append-to-body="true"
    >
      <el-form @submit.prevent="confirmRename">
        <el-form-item label="分组名称">
          <el-input
            v-model="newGroupName"
            placeholder="请输入新的分组名称"
            maxlength="20"
            show-word-limit
            @keyup.enter="confirmRename"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="renameDialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="renameLoading"
          @click="confirmRename"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 联系人右键菜单 -->
    <ContextMenu
      :show="contextMenuVisible"
      :x="contextMenuX"
      :y="contextMenuY"
      :items="contextMenuItems"
      @select="handleContextMenuSelect"
      @update:show="contextMenuVisible = $event"
    />

    <!-- 移动到分组对话框 -->
    <el-dialog
      v-model="moveDialogVisible"
      title="移动到分组"
      width="400px"
      :append-to-body="true"
    >
      <el-select
        v-model="targetGroupName"
        placeholder="选择目标分组"
        style="width: 100%"
      >
        <el-option
          label="未分组"
          value=""
        />
        <el-option
          v-for="group in groupList"
          :key="group"
          :label="group || '未分组'"
          :value="group"
        />
      </el-select>
      <template #footer>
        <el-button @click="moveDialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="moveLoading"
          @click="confirmMove"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 修改备注对话框 -->
    <el-dialog
      v-model="remarkDialogVisible"
      title="修改备注"
      width="400px"
      :append-to-body="true"
    >
      <el-input
        v-model="newRemark"
        placeholder="请输入备注名称"
        maxlength="50"
        show-word-limit
        @keyup.enter="confirmRemark"
      />
      <template #footer>
        <el-button @click="remarkDialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="remarkLoading"
          @click="confirmRemark"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { Search, OfficeBuilding, User, MoreFilled, FolderOpened, Edit, Delete } from '@element-plus/icons-vue'
import ContextMenu from '@/components/Common/ContextMenu.vue'
import { getContacts, getGroupedFriendList, getGroupList, renameGroup, deleteGroup, moveContactToGroup, updateContactRemark, deleteContact, getFriendRequests, clearFriendListCache } from '@/api/im/contact'
import { getOrgTree } from '@/api/im/organization'
import { getGroups } from '@/api/im/group'
import { addTokenToUrl } from '@/utils/file'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  currentContact: Object
})

const emit = defineEmits(['select', 'nav', 'refresh'])

const searchQuery = ref('')
const contacts = ref([])
const groupedContacts = ref([])
const groups = ref([])
const groupList = ref([])
const orgTree = ref([])
const loading = ref(false)
const activeNames = ref(['org', 'groups'])
const unreadRequestCount = ref(0)
const hasRetriedGrouped = ref(false)

// 分组重命名
const renameDialogVisible = ref(false)
const renameLoading = ref(false)
const currentRenameGroup = ref('')
const newGroupName = ref('')

// 移动到分组
const moveDialogVisible = ref(false)
const moveLoading = ref(false)
const targetGroupName = ref('')
const currentContactForMove = ref(null)

// 修改备注
const remarkDialogVisible = ref(false)
const remarkLoading = ref(false)
const newRemark = ref('')
const currentContactForRemark = ref(null)

// 右键菜单
const contextMenuVisible = ref(false)
const contextMenuX = ref(0)
const contextMenuY = ref(0)
const currentContactForMenu = ref(null)

const loadData = async () => {
  loading.value = true
  try {
    const [gcRes, oRes, gRes, glRes, frRes] = await Promise.all([
      getGroupedFriendList(),
      getOrgTree(),
      getGroups(),
      getGroupList(),
      getFriendRequests()
    ])
    if (gcRes.code === 200 && gcRes.data) {
      groupedContacts.value = gcRes.data
    }
    if (groupedContacts.value.length === 0 && !hasRetriedGrouped.value) {
      hasRetriedGrouped.value = true
      await clearFriendListCache()
      const retryRes = await getGroupedFriendList()
      if (retryRes.code === 200 && retryRes.data) {
        groupedContacts.value = retryRes.data
      }
    }
    if (oRes && oRes.code === 200) {orgTree.value = oRes.data}
    if (gRes && gRes.code === 200) {groups.value = gRes.data}
    if (glRes && glRes.code === 200) {groupList.value = glRes.data}
    if (frRes && frRes.code === 200) {
      unreadRequestCount.value = frRes.data.filter(r => r.status === 'PENDING').length
    }
    // 默认展开联系人分组
    if (groupedContacts.value.length > 0) {
      activeNames.value.push(...groupedContacts.value.map(g => `friends-${g.groupName}`))
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const searchResults = computed(() => {
  if (!searchQuery.value) {return []}
  const q = searchQuery.value.toLowerCase()
  const results = []
  groupedContacts.value.forEach(group => {
    group.contacts.forEach(contact => {
      const name = (contact.remark || contact.friendName || '').toLowerCase()
      if (name.includes(q)) {
        results.push({ ...contact, groupName: group.groupName })
      }
    })
  })
  return results
})

const filteredGroups = computed(() => {
  if (!searchQuery.value) {return groups.value}
  const q = searchQuery.value.toLowerCase()
  return groups.value.filter(g => g.name && g.name.toLowerCase().includes(q))
})

const handleNodeClick = data => {
  if (data.type === 'user') {
    emit('select', {
      id: data.id,
      friendId: data.id,
      friendName: data.name,
      friendAvatar: addTokenToUrl(data.avatar),
      isOrgNode: true
    })
  }
}

const handleGroupClick = group => {
  emit('select', { ...group, isGroup: true })
}

const handleContactClick = contact => {
  contextMenuVisible.value = false
  emit('select', { ...contact, isGroup: false })
}

// 分组管理
const handleGroupCommand = (command, groupName) => {
  if (command === 'rename') {
    currentRenameGroup.value = groupName
    newGroupName.value = groupName || ''
    renameDialogVisible.value = true
  } else if (command === 'delete') {
    ElMessageBox.confirm(
      `删除分组 "${groupName}" 后，该分组下的联系人将移动到"未分组"，确定删除吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      doDeleteGroup(groupName)
    }).catch(() => {})
  }
}

const confirmRename = async () => {
  if (!newGroupName.value.trim()) {
    ElMessage.warning('请输入分组名称')
    return
  }
  renameLoading.value = true
  try {
    const res = await renameGroup(currentRenameGroup.value || '', newGroupName.value.trim())
    if (res.code === 200) {
      ElMessage.success('重命名成功')
      renameDialogVisible.value = false
      await loadData()
      emit('refresh')
    } else {
      ElMessage.error(res.msg || '重命名失败')
    }
  } catch (e) {
    ElMessage.error('重命名失败')
  } finally {
    renameLoading.value = false
  }
}

const doDeleteGroup = async groupName => {
  try {
    const res = await deleteGroup(groupName)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadData()
      emit('refresh')
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

// 右键菜单
const showContactMenu = (event, contact) => {
  currentContactForMenu.value = contact
  contextMenuX.value = event.clientX
  contextMenuY.value = event.clientY
  contextMenuVisible.value = true
}

// 右键菜单项配置
const contextMenuItems = computed(() => {
  return [
    {
      label: '移动到分组',
      icon: 'folder_open',
      value: 'moveToGroup'
    },
    {
      label: '修改备注',
      icon: 'edit',
      value: 'editRemark'
    },
    {
      label: '删除联系人',
      icon: 'delete',
      value: 'delete',
      danger: true
    }
  ]
})

// 右键菜单选择处理
const handleContextMenuSelect = async item => {
  const contact = currentContactForMenu.value
  if (!contact) {return}

  switch (item.value) {
    case 'moveToGroup':
      currentContactForMove.value = contact
      targetGroupName.value = contact.groupName || ''
      moveDialogVisible.value = true
      break
    case 'editRemark':
      currentContactForRemark.value = contact
      newRemark.value = contact.remark || contact.friendName || ''
      remarkDialogVisible.value = true
      break
    case 'delete':
      ElMessageBox.confirm(
        `确定删除联系人 "${contact.remark || contact.friendName}" 吗？`,
        '确认删除',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(async () => {
        try {
          const res = await deleteContact(contact.id)
          if (res.code === 200) {
            ElMessage.success('删除成功')
            await loadData()
            emit('refresh')
          } else {
            ElMessage.error(res.msg || '删除失败')
          }
        } catch (e) {
          ElMessage.error('删除失败')
        }
      }).catch(() => {})
      break
  }

  contextMenuVisible.value = false
}

const handleMoveToGroup = () => {
  currentContactForMove.value = currentContactForMenu.value
  targetGroupName.value = currentContactForMenu.value.groupName || ''
  moveDialogVisible.value = true
}

const handleEditRemark = () => {
  currentContactForRemark.value = currentContactForMenu.value
  newRemark.value = currentContactForMenu.value.remark || currentContactForMenu.value.friendName || ''
  remarkDialogVisible.value = true
}

const handleDeleteContact = () => {
  const contact = currentContactForMenu.value
  ElMessageBox.confirm(
    `确定删除联系人 "${contact.remark || contact.friendName}" 吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await deleteContact(contact.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        await loadData()
        emit('refresh')
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    } catch (e) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const confirmMove = async () => {
  moveLoading.value = true
  try {
    const res = await moveContactToGroup({
      contactId: currentContactForMove.value.id,
      groupName: targetGroupName.value
    })
    if (res.code === 200) {
      ElMessage.success('移动成功')
      moveDialogVisible.value = false
      await loadData()
      emit('refresh')
    } else {
      ElMessage.error(res.msg || '移动失败')
    }
  } catch (e) {
    ElMessage.error('移动失败')
  } finally {
    moveLoading.value = false
  }
}

const confirmRemark = async () => {
  if (!newRemark.value.trim()) {
    ElMessage.warning('请输入备注名称')
    return
  }
  remarkLoading.value = true
  try {
    const res = await updateContactRemark(currentContactForRemark.value.id, {
      remark: newRemark.value.trim()
    })
    if (res.code === 200) {
      ElMessage.success('修改成功')
      remarkDialogVisible.value = false
      await loadData()
      emit('refresh')
    } else {
      ElMessage.error(res.msg || '修改失败')
    }
  } catch (e) {
    ElMessage.error('修改失败')
  } finally {
    remarkLoading.value = false
  }
}

onMounted(() => {
  loadData()
})

onBeforeUnmount(() => {
  // 清理工作
})

defineExpose({ reload: loadData })
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.contact-list-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border-right: 1px solid var(--dt-border-divider);
  position: relative;

  .dark & {
    background: var(--dt-bg-card-dark);
    border-right-color: var(--dt-border-dark);
  }
}

.search-box {
  padding: 12px;
  border-bottom: 1px solid var(--dt-border-lighter);

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }
}

.list-content {
  flex: 1;
  overflow-y: auto;

  @extend .scrollbar-sm;
}

.nav-list {
  padding: 8px 0;
  border-bottom: 1px solid var(--dt-border-lighter);

  .dark & {
    border-bottom-color: var(--dt-border-dark);
  }

  .nav-item {
    display: flex;
    align-items: center;
    padding: 10px 12px;
    cursor: pointer;
    gap: 12px;
    transition: background var(--dt-transition-fast);

    &:hover {
      background: var(--dt-bg-session-hover);
    }

    .nav-badge {
      display: flex;
      align-items: center;
    }

    .nav-icon {
      width: 36px;
      height: 36px;
      border-radius: var(--dt-radius-md);
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 20px;

      &.new-friends { background: #FF9800; }
    }

    .nav-label {
      font-size: var(--dt-font-size-base);
      color: var(--dt-text-primary);
    }
  }
}

.group-header {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding-right: 8px;

  .group-name {
    font-size: var(--dt-font-size-base);
    color: var(--dt-text-primary);
  }

  .group-count {
    font-size: var(--dt-font-size-sm);
    color: var(--dt-text-tertiary);
  }

  .group-more {
    margin-left: auto;
    cursor: pointer;
    color: var(--dt-text-tertiary);
    font-size: 16px;
    padding: 4px;
    border-radius: var(--dt-radius-sm);
    transition: all var(--dt-transition-fast);

    &:hover {
      color: var(--dt-brand-color);
      background: var(--dt-bg-session-hover);
    }
  }
}

.friend-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  cursor: pointer;
  transition: background var(--dt-transition-fast);

  &:hover,
  &.active {
    background-color: var(--dt-bg-session-hover);
  }

  &.active {
    background-color: var(--dt-brand-bg);
  }
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: var(--dt-radius-md);
  margin-right: 10px;
  background: var(--dt-brand-color);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: var(--dt-font-size-base);
  overflow: hidden;
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.info {
  flex: 1;
  overflow: hidden;
}

.name {
  font-size: var(--dt-font-size-base);
  color: var(--dt-text-primary);
  margin-bottom: 2px;
}

.desc {
  font-size: var(--dt-font-size-sm);
  color: var(--dt-text-tertiary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.custom-tree-node {
  display: flex;
  align-items: center;
  gap: 6px;

  .el-icon {
    color: var(--dt-text-tertiary);
  }

  .node-label {
    font-size: var(--dt-font-size-base);
  }
}

.empty-small {
  text-align: center;
  color: var(--dt-text-quaternary);
  font-size: var(--dt-font-size-sm);
  padding: 40px 20px;
}

/* 折叠面板标题样式优化 */
:deep(.el-collapse-item__header) {
  padding: 0 12px;
  height: 44px;
  line-height: 44px;
  font-size: var(--dt-font-size-base);
  font-weight: var(--dt-font-weight-medium);
  color: var(--dt-text-primary);
}

:deep(.el-collapse-item__wrap) {
  border-bottom: none;
}

:deep(.el-collapse-item__content) {
  padding-bottom: 0;
}

/* Element Plus 暗色模式适配 */
:deep(.el-input__wrapper) {
  .dark & {
    background-color: var(--dt-bg-input-dark);
    box-shadow: 0 0 0 1px var(--dt-border-dark) inset;
  }
}

:deep(.el-collapse) {
  border: none;
}

:deep(.el-tree) {
  background: transparent;

  .el-tree-node__content {
    height: 36px;
    border-radius: var(--dt-radius-sm);

    &:hover {
      background: var(--dt-bg-session-hover);
    }
  }

  .dark & {
    color: var(--dt-text-primary-dark);
  }
}

/* 对话框样式 */
:deep(.el-dialog) {
  .dark & {
    background: var(--dt-bg-card-dark);
    border: 1px solid var(--dt-border-dark);
  }

  .el-dialog__header {
    border-bottom: 1px solid var(--dt-border-lighter);

    .dark & {
      border-bottom-color: var(--dt-border-dark);
    }
  }

  .el-dialog__title {
    font-weight: var(--dt-font-weight-semibold);
  }
}

/* 徽标样式 */
:deep(.el-badge__content) {
  font-size: var(--dt-font-size-xs);
  font-weight: var(--dt-font-weight-medium);
}

/* 下拉菜单样式 */
:deep(.el-dropdown-menu) {
  .dark & {
    background: var(--dt-bg-card-dark);
    border-color: var(--dt-border-dark);
  }

  .el-dropdown-menu__item {
    &:hover {
      background: var(--dt-bg-session-hover);
    }
  }
}
</style>
