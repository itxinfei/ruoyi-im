<template>
  <div class="group-container">
    <!-- 左侧群组列表面板 -->
    <div class="group-list-panel">
      <div class="panel-header">
        <div class="search-container">
          <el-input
            v-model="searchText"
            placeholder="搜索群组"
            :prefix-icon="Search"
            clearable
            class="search-input"
            @input="handleSearch"
          />
        </div>
        <el-button type="primary" :icon="Plus" @click="handleAdd" size="default">创建群组</el-button>
      </div>

      <div class="group-list">
        <div
          v-for="group in filteredGroups"
          :key="group.groupId"
          class="group-item"
          :class="{ active: selectedGroup?.groupId === group.groupId }"
          @click="selectGroup(group)"
        >
          <div class="group-avatar">
            <el-avatar :size="40" :src="group.avatar || '/profile/group.png'" :alt="group.groupName">
              {{ group.groupName?.charAt(0) || '群' }}
            </el-avatar>
          </div>
          <div class="group-info">
            <div class="group-name">
              {{ group.groupName }}
            </div>
            <div class="group-meta">
              <span class="member-count">{{ group.memberCount || 0 }}人</span>
              <span class="separator">•</span>
              <span class="owner-name">{{ group.ownerName || '未知' }}</span>
            </div>
          </div>
        </div>

        <el-empty v-if="filteredGroups.length === 0" :description="searchText ? '没有找到相关群组' : '暂无群组'" />
      </div>
    </div>

    <!-- 右侧详情区域 -->
    <div class="detail-panel">
      <template v-if="selectedGroup">
        <!-- 详情头部 -->
        <div class="detail-header">
          <div class="header-avatar">
            <el-avatar :size="64" :src="selectedGroup.avatar || '/profile/group.png'" :alt="selectedGroup.groupName">
              {{ selectedGroup.groupName?.charAt(0) || '群' }}
            </el-avatar>
          </div>
          <div class="header-info">
            <h2 class="group-name">{{ selectedGroup.groupName }}</h2>
            <p class="group-meta">
              <span class="member-count">{{ selectedGroup.memberCount || 0 }}人</span>
              <span class="separator">•</span>
              <span class="owner">群主 {{ selectedGroup.ownerName || '未知' }}</span>
            </p>
          </div>
          <div class="header-actions">
            <el-button type="primary" :icon="Comment" @click="enterGroupChat" size="default">发送消息</el-button>
            <el-dropdown trigger="click" @command="handleAction">
              <el-button :icon="More" circle size="default" />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="members" :icon="User">
                    群成员({{ selectedGroup.memberCount || 0 }})
                  </el-dropdown-item>
                  <el-dropdown-item command="notice" :icon="Bell">
                    群公告
                  </el-dropdown-item>
                  <el-dropdown-item command="settings" :icon="Setting">
                    群设置
                  </el-dropdown-item>
                  <el-dropdown-item divided command="exit" :icon="SwitchButton">
                    退出群组
                  </el-dropdown-item>
                  <el-dropdown-item 
                    command="dismiss" 
                    :icon="Delete"
                    v-if="selectedGroup.ownerId === currentUserId"
                  >
                    解散群组
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>

        <!-- 详情内容 -->
        <div class="detail-content">
          <div class="info-section">
            <h4>群组信息</h4>
            <div class="info-grid">
              <div class="info-item">
                <span class="label">群主</span>
                <span class="value">{{ selectedGroup.ownerName || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">成员数</span>
                <span class="value">{{ selectedGroup.memberCount || 0 }}人</span>
              </div>
              <div class="info-item">
                <span class="label">创建时间</span>
                <span class="value">{{ formatTime(selectedGroup.createTime) || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">群状态</span>
                <span class="value">
                  <el-tag 
                    :type="selectedGroup.status === '0' || selectedGroup.status === 0 ? 'success' : 'danger'"
                    size="small"
                  >
                    {{ selectedGroup.status === '0' || selectedGroup.status === 0 ? '正常' : '禁用' }}
                  </el-tag>
                </span>
              </div>
            </div>
          </div>

          <div class="info-section">
            <h4>群简介</h4>
            <p class="notice">{{ selectedGroup.description || '暂无群简介' }}</p>
          </div>
        </div>
      </template>

      <!-- 空状态 -->
      <div v-else class="empty-detail">
        <div class="empty-content">
          <el-icon :size="64" class="empty-icon"><ChatLineRound /></el-icon>
          <h3 class="empty-title">选择一个群组查看详细信息</h3>
          <p class="empty-description">点击左侧群组列表中的群组，查看详细信息并进行管理</p>
        </div>
      </div>
    </div>

    <!-- 成员管理对话框 -->
    <el-dialog v-model="memberDialogVisible" :title="`群成员(${memberList.length})`" width="600px" destroy-on-close>
      <div class="member-header">
        <el-input
          v-model="memberSearchText"
          placeholder="搜索成员"
          :prefix-icon="Search"
          clearable
          style="width: 200px; margin-right: 12px;"
        />
        <el-button type="primary" :icon="Plus" @click="handleAddMember">添加成员</el-button>
      </div>
      <el-table :data="filteredMembers" max-height="400" style="margin-top: 16px;">
        <el-table-column label="成员" min-width="150">
          <template #default="scope">
            <div class="member-info">
              <el-avatar :size="32" :src="scope.row.avatar">
                {{ scope.row.nickname?.charAt(0) || scope.row.username?.charAt(0) || '用' }}
              </el-avatar>
              <div class="member-text">
                <span class="member-name">{{ scope.row.nickname || scope.row.username }}</span>
                <span class="member-role" v-if="scope.row.role !== 'member'">{{ getMemberRoleLabel(scope.row.role) }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getMemberRoleType(scope.row.role)" size="small">
              {{ getMemberRoleLabel(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="加入时间" width="160" align="center" prop="joinTime" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="scope">
            <el-button
              v-if="canRemoveMember(scope.row)"
              link
              type="danger"
              size="small"
              @click="handleRemoveMember(scope.row)"
            >移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 群组设置对话框 -->
    <el-dialog v-model="settingsDialogVisible" title="群设置" width="500px" destroy-on-close>
      <el-form :model="settingsForm" label-width="120px">
        <el-form-item label="入群验证">
          <el-switch v-model="settingsForm.joinApproval" />
          <span class="form-tip">开启后，新成员需要管理员审批</span>
        </el-form-item>
        <el-form-item label="全员禁言">
          <el-switch v-model="settingsForm.muteAll" />
          <span class="form-tip">开启后，只有管理员可以发言</span>
        </el-form-item>
        <el-form-item label="允许成员邀请">
          <el-switch v-model="settingsForm.allowMemberInvite" />
          <span class="form-tip">开启后，普通成员也可以邀请新成员</span>
        </el-form-item>
        <el-form-item label="群公告">
          <el-input
            v-model="settingsForm.announcement"
            type="textarea"
            :rows="4"
            placeholder="请输入群公告"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="settingsDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="settingsLoading" @click="handleSaveSettings">保存</el-button>
      </template>
    </el-dialog>

    <!-- 创建/编辑群组对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="群组名称" prop="groupName">
          <el-input v-model="formData.groupName" placeholder="请输入群组名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="群组头像" prop="avatar">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <el-avatar v-if="formData.avatar" :size="80" :src="formData.avatar" />
            <el-icon v-else class="avatar-uploader-icon" :size="24"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="群组简介" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入群组简介"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Plus,
  More,
  Comment,
  User,
  Bell,
  Setting,
  SwitchButton,
  Delete,
  ChatLineRound,
} from '@element-plus/icons-vue'
import { listGroup, getGroup, addGroup, updateGroup, delGroup } from '@/api/im/group'
import { getToken } from '@/utils/auth'

const router = useRouter()

// ==================== 响应式状态 ====================

/** 搜索文本 */
const searchText = ref('')

/** 选中的群组 */
const selectedGroup = ref(null)

/** 加载状态 */
const loading = ref(false)
const submitLoading = ref(false)
const settingsLoading = ref(false)

/** 表单引用 */
const formRef = ref(null)

/** 群组列表 */
const groupList = ref([])

/** 对话框控制 */
const dialogVisible = ref(false)
const dialogTitle = ref('')
const memberDialogVisible = ref(false)
const settingsDialogVisible = ref(false)

/** 表单数据 */
const formData = reactive({
  groupId: null,
  groupName: '',
  avatar: '',
  description: '',
  status: '0',
})

/** 表单校验规则 */
const formRules = {
  groupName: [
    { required: true, message: '请输入群组名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' },
  ],
}

/** 成员相关 */
const memberSearchText = ref('')
const memberList = ref([])

/** 设置表单 */
const settingsForm = reactive({
  groupId: null,
  joinApproval: false,
  muteAll: false,
  allowMemberInvite: true,
  announcement: '',
})

/** 当前用户ID（模拟） */
const currentUserId = ref(1)

// ==================== 计算属性 ====================

/** 上传地址 */
const uploadUrl = computed(() => {
  const baseUrl = import.meta.env.VITE_BASE_API || ''
  return `${baseUrl}/api/im/file/upload`
})

/** 上传请求头 */
const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + getToken(),
}))

/** 过滤后的群组列表 */
const filteredGroups = computed(() => {
  if (!searchText.value) return groupList.value
  const keyword = searchText.value.toLowerCase()
  return groupList.value.filter(g => {
    const name = g.groupName || ''
    return name.toLowerCase().includes(keyword)
  })
})

/** 过滤后的成员列表 */
const filteredMembers = computed(() => {
  if (!memberSearchText.value) return memberList.value
  const keyword = memberSearchText.value.toLowerCase()
  return memberList.value.filter(m => {
    const nickname = m.nickname || ''
    const username = m.username || ''
    return nickname.toLowerCase().includes(keyword) || username.toLowerCase().includes(keyword)
  })
})

// ==================== 方法定义 ====================

/**
 * 获取群组列表
 */
const getList = async () => {
  loading.value = true
  try {
    // 这拟数据，因为实际API可能尚未完全实现
    groupList.value = [
      { groupId: 1, groupName: '技术交流群', ownerName: '管理员', ownerId: 1, ownerAvatar: '', memberCount: 128, status: '0', createTime: '2024-01-15 10:30:00', avatar: '', description: '技术交流与讨论' },
      { groupId: 2, groupName: '产品讨论组', ownerName: '张三', ownerId: 2, ownerAvatar: '', memberCount: 45, status: '0', createTime: '2024-01-14 15:20:00', avatar: '', description: '产品设计与讨论' },
      { groupId: 3, groupName: '项目协作群', ownerName: '李四', ownerId: 3, ownerAvatar: '', memberCount: 32, status: '1', createTime: '2024-01-13 09:00:00', avatar: '', description: '项目进度同步' },
      { groupId: 4, groupName: '前端开发群', ownerName: '王五', ownerId: 4, ownerAvatar: '', memberCount: 88, status: '0', createTime: '2024-01-12 14:30:00', avatar: '', description: '前端技术分享' },
      { groupId: 5, groupName: '后端技术群', ownerName: '赵六', ownerId: 5, ownerAvatar: '', memberCount: 65, status: '0', createTime: '2024-01-11 11:15:00', avatar: '', description: '后端技术讨论' },
    ]
  } catch (error) {
    console.error('获取群组列表失败:', error)
    groupList.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 搜索群组
 */
const handleSearch = () => {
  // 搜索已在computed中处理
}

/**
 * 选择群组
 */
const selectGroup = (group) => {
  selectedGroup.value = group
}

/**
 * 进入群聊
 */
const enterGroupChat = () => {
  if (selectedGroup.value) {
    router.push(`/im/chat?groupId=${selectedGroup.value.groupId}`)
  }
}

/**
 * 处理操作
 */
const handleAction = async (command) => {
  if (!selectedGroup.value) return

  switch (command) {
    case 'members':
      memberList.value = [
        { id: 1, nickname: '管理员', username: 'admin', role: 'owner', joinTime: '2024-01-01 10:00:00', avatar: '' },
        { id: 2, nickname: '张三', username: 'zhangsan', role: 'admin', joinTime: '2024-01-02 14:30:00', avatar: '' },
        { id: 3, nickname: '李四', username: 'lisi', role: 'member', joinTime: '2024-01-03 09:15:00', avatar: '' },
        { id: 4, nickname: '王五', username: 'wangwu', role: 'member', joinTime: '2024-01-04 16:45:00', avatar: '' },
      ]
      memberDialogVisible.value = true
      break
    case 'notice':
      ElMessage.info('查看群公告功能开发中...')
      break
    case 'settings':
      settingsForm.groupId = selectedGroup.value.groupId
      settingsForm.joinApproval = selectedGroup.value.joinApproval || false
      settingsForm.muteAll = selectedGroup.value.muteAll || false
      settingsForm.allowMemberInvite = selectedGroup.value.allowMemberInvite !== false
      settingsForm.announcement = selectedGroup.value.description || ''
      settingsDialogVisible.value = true
      break
    case 'exit':
      try {
        await ElMessageBox.confirm('确定要退出当前群组吗？', '确认退出', {
          type: 'warning',
        })
        // 退出群组逻辑
        ElMessage.success('已退出群组')
        selectedGroup.value = null
      } catch {
        // 取消操作
      }
      break
    case 'dismiss':
      try {
        await ElMessageBox.confirm('确定要解散当前群组吗？此操作不可逆！', '确认解散', {
          type: 'warning',
        })
        await delGroup(selectedGroup.value.groupId)
        ElMessage.success('群组已解散')
        const index = groupList.value.findIndex(g => g.groupId === selectedGroup.value.groupId)
        if (index > -1) {
          groupList.value.splice(index, 1)
        }
        selectedGroup.value = null
      } catch {
        // 取消操作
      }
      break
  }
}

/**
 * 新增群组
 */
const handleAdd = () => {
  dialogTitle.value = '创建群组'
  formData.groupId = null
  formData.groupName = ''
  formData.avatar = ''
  formData.description = ''
  formData.status = '0'
  dialogVisible.value = true
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate()
  submitLoading.value = true

  try {
    if (formData.groupId) {
      await updateGroup(formData)
      ElMessage.success('修改成功')
    } else {
      await addGroup(formData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    getList()
  } catch (error) {
    ElMessage.error('操作失败：' + (error.message || '未知错误'))
  } finally {
    submitLoading.value = false
  }
}

/**
 * 添加成员
 */
const handleAddMember = () => {
  ElMessage.info('添加成员功能开发中...')
}

/**
 * 移除成员
 */
const handleRemoveMember = async (member) => {
  try {
    await ElMessageBox.confirm(`确定要移除成员"${member.nickname || member.username}"吗？`, '确认', {
      type: 'warning',
    })
    const index = memberList.value.findIndex(m => m.id === member.id)
    if (index > -1) {
      memberList.value.splice(index, 1)
      ElMessage.success('移除成功')
    }
  } catch {
    // 取消操作
  }
}

/**
 * 判断是否可以移除成员
 */
const canRemoveMember = (member) => {
  return member.role !== 'owner' && member.id !== currentUserId
}

/**
 * 获取成员角色类型
 */
const getMemberRoleType = (role) => {
  const types = {
    owner: 'danger',
    admin: 'warning',
    member: 'info',
  }
  return types[role] || 'info'
}

/**
 * 获取成员角色标签
 */
const getMemberRoleLabel = (role) => {
  const labels = {
    owner: '群主',
    admin: '管理员',
    member: '成员',
  }
  return labels[role] || '成员'
}

/**
 * 保存设置
 */
const handleSaveSettings = async () => {
  settingsLoading.value = true
  try {
    // 模拟保存设置
    ElMessage.success('设置保存成功')
    settingsDialogVisible.value = false
  } catch (error) {
    ElMessage.error('保存失败：' + (error.message || '网络错误'))
  } finally {
    settingsLoading.value = false
  }
}

/**
 * 头像上传成功
 */
const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    formData.avatar = response.data?.url || response.url
  } else {
    ElMessage.error('头像上传失败')
  }
}

/**
 * 头像上传前校验
 */
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB')
    return false
  }
  return true
}

/**
 * 格式化时间
 */
const formatTime = (time) => {
  if (!time) return ''
  return time
}

// ==================== 生命周期 ====================

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
@use '@/assets/styles/variables.scss' as *;

.group-container {
  height: 100%;
  display: flex;
  background-color: #f5f5f5;
}

// 左侧群组列表面板
.group-list-panel {
  width: $list-panel-width;
  min-width: $list-panel-width;
  height: 100%;
  background-color: #fff;
  border-right: 1px solid #e6e6e6;
  display: flex;
  flex-direction: column;

  .panel-header {
    padding: 16px;
    border-bottom: 1px solid #f0f0f0;
    display: flex;
    gap: 8px;

    .search-container {
      flex: 1;
      position: relative;

      .search-input {
        :deep(.el-input__wrapper) {
          border-radius: 18px;
          background-color: #f5f5f5;
          padding-left: 36px;
        }

        :deep(.el-input__prefix) {
          left: 12px;
        }
      }
    }
  }

  .group-list {
    flex: 1;
    overflow-y: auto;

    .group-item {
      display: flex;
      align-items: center;
      padding: 12px 16px;
      cursor: pointer;
      transition: background-color 0.2s;
      border-left: 3px solid transparent;

      &:hover {
        background-color: #f5f7fa;
      }

      &.active {
        background-color: #e6f7ff;
        border-left-color: $primary-color;
      }

      .group-avatar {
        margin-right: 12px;
      }

      .group-info {
        flex: 1;
        min-width: 0;

        .group-name {
          font-size: 14px;
          font-weight: 500;
          color: #333;
          margin-bottom: 4px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .group-meta {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          color: #999;

          .separator {
            color: #d9d9d9;
          }
        }
      }
    }
  }
}

// 右侧详情区域
.detail-panel {
  flex: 1;
  height: 100%;
  background-color: #fff;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .detail-header {
    padding: 24px;
    display: flex;
    align-items: center;
    gap: 16px;
    border-bottom: 1px solid #f0f0f0;
    background-color: #fafafa;

    .header-avatar {
      flex-shrink: 0;
    }

    .header-info {
      flex: 1;

      .group-name {
        margin: 0 0 4px;
        font-size: 18px;
        font-weight: 600;
        color: #333;
      }

      .group-meta {
        margin: 0;
        font-size: 14px;
        color: #666;
        display: flex;
        align-items: center;
        gap: 4px;

        .separator {
          color: #d9d9d9;
        }
      }
    }

    .header-actions {
      display: flex;
      gap: 8px;
      flex-shrink: 0;
    }
  }

  .detail-content {
    flex: 1;
    padding: 24px;
    overflow-y: auto;

    .info-section {
      margin-bottom: 24px;

      h4 {
        margin: 0 0 16px;
        font-size: 14px;
        font-weight: 600;
        color: #333;
      }

      .info-grid {
        display: grid;
        grid-template-columns: 1fr;
        gap: 12px;

        .info-item {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 8px 0;
          border-bottom: 1px solid #f5f5f5;

          &:last-child {
            border-bottom: none;
          }

          .label {
            font-size: 14px;
            color: #666;
          }

          .value {
            font-size: 14px;
            color: #333;
            text-align: right;
          }
        }
      }

      .notice {
        margin: 0;
        padding: 12px 16px;
        background-color: #f5f7fa;
        border-radius: 8px;
        font-size: 14px;
        color: #333;
        line-height: 1.6;
      }
    }
  }

  .empty-detail {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;

    .empty-content {
      text-align: center;
      color: #999;

      .empty-icon {
        color: #d9d9d9;
        margin-bottom: 16px;
      }

      .empty-title {
        margin: 0 0 8px;
        font-size: 16px;
        font-weight: 500;
        color: #666;
      }

      .empty-description {
        margin: 0;
        font-size: 14px;
        color: #999;
      }
    }
  }
}

.avatar-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: border-color 0.3s;
    width: 80px;
    height: 80px;
    display: flex;
    align-items: center;
    justify-content: center;

    &:hover {
      border-color: #409eff;
    }
  }

  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
  }
}

.member-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}

.member-info {
  display: flex;
  align-items: center;
  gap: 8px;

  .member-text {
    display: flex;
    flex-direction: column;

    .member-name {
      font-size: 14px;
      color: #333;
    }

    .member-role {
      font-size: 12px;
      color: #999;
    }
  }
}

.form-tip {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}

// 响应式
@media screen and (max-width: 768px) {
  .group-list-panel {
    width: 100%;
  }

  .detail-panel {
    display: none;
  }
}
</style>