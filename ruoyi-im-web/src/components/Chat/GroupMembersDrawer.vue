<template>
  <el-drawer
    v-model="visible"
    size="480px"
    direction="rtl"
    class="group-members-drawer"
    :with-header="false"
    append-to-body
    destroy-on-close
  >
    <div class="drawer-content">
      <!-- 头部 -->
      <div class="drawer-header">
        <div class="header-title">
          <button class="back-btn" @click="handleClose">
            <span class="material-icons-outlined">arrow_back</span>
          </button>
          <span class="title-text">群成员 ({{ members.length }})</span>
        </div>
      </div>

      <!-- 搜索框 -->
      <div class="search-section">
        <el-input
          v-model="searchText"
          placeholder="搜索成员"
          clearable
          prefix-icon="Search"
        />
      </div>

      <!-- 成员列表 -->
      <div class="members-list" ref="listRef">
        <!-- 群主 -->
        <template v-if="owner">
          <div class="section-title">群主</div>
          <div class="member-item" @click="handleMemberClick(owner)">
            <DingtalkAvatar :src="owner.avatar" :name="owner.name" :size="44" />
            <div class="member-info">
              <div class="member-name">{{ owner.name }}</div>
              <div class="member-role role-owner">群主</div>
            </div>
            <div v-if="canManage && owner.id !== currentUserId" class="member-actions">
              <el-button size="small" @click.stop="handleAtMember(owner)">
                <span class="material-icons-outlined">alternate_email</span>
              </el-button>
            </div>
          </div>
        </template>

        <!-- 管理员 -->
        <template v-if="admins.length > 0">
          <div class="section-title">管理员 ({{ admins.length }})</div>
          <div
            v-for="admin in admins"
            :key="admin.id"
            class="member-item"
            @click="handleMemberClick(admin)"
          >
            <DingtalkAvatar :src="admin.avatar" :name="admin.name" :size="44" />
            <div class="member-info">
              <div class="member-name">{{ admin.name }}</div>
              <div class="member-role role-admin">管理员</div>
            </div>
            <div v-if="canManage" class="member-actions">
              <el-button size="small" @click.stop="handleAtMember(admin)">
                <span class="material-icons-outlined">alternate_email</span>
              </el-button>
              <el-dropdown v-if="currentRole === 'OWNER'" @click.stop>
                <el-button size="small">
                  <span class="material-icons-outlined">more_horiz</span>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleSetAdmin(admin, false)">
                      取消管理员
                    </el-dropdown-item>
                    <el-dropdown-item @click="handleMuteMember(admin)" :divided="true">
                      {{ admin.isMuted ? '取消禁言' : '禁言' }}
                    </el-dropdown-item>
                    <el-dropdown-item @click="handleRemoveMember(admin)" class="danger">
                      移出群聊
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </template>

        <!-- 普通成员 -->
        <template v-if="normalMembers.length > 0">
          <div class="section-title">成员 ({{ normalMembers.length }})</div>
          <div
            v-for="member in normalMembers"
            :key="member.id"
            class="member-item"
            @click="handleMemberClick(member)"
          >
            <DingtalkAvatar :src="member.avatar" :name="member.name" :size="44" />
            <div class="member-info">
              <div class="member-name">{{ member.name }}</div>
              <div class="member-role role-member">{{ member.department || '成员' }}</div>
            </div>
            <div v-if="canManage || member.id !== currentUserId" class="member-actions">
              <el-button size="small" @click.stop="handleAtMember(member)">
                <span class="material-icons-outlined">alternate_email</span>
              </el-button>
              <el-dropdown v-if="canManage && member.id !== currentUserId" @click.stop>
                <el-button size="small">
                  <span class="material-icons-outlined">more_horiz</span>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleSetAdmin(member, true)">
                      设为管理员
                    </el-dropdown-item>
                    <el-dropdown-item @click="handleMuteMember(member)" :divided="true">
                      {{ member.isMuted ? '取消禁言' : '禁言' }}
                    </el-dropdown-item>
                    <el-dropdown-item @click="handleRemoveMember(member)" class="danger">
                      移出群聊
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </template>

        <!-- 空状态 -->
        <div v-if="filteredMembers.length === 0" class="empty-state">
          <span class="material-icons-outlined">search_off</span>
          <p>未找到成员</p>
        </div>
      </div>

      <!-- 底部添加按钮 -->
      <div v-if="canManage" class="drawer-footer">
        <el-button type="primary" @click="handleAddMembers" class="add-btn">
          <span class="material-icons-outlined">add</span>
          添加成员
        </el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { getGroupMembers, setGroupAdmin, muteGroupMember, removeGroupMember } from '@/api/im/group'
import { useStore } from 'vuex'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  groupId: { type: [String, Number], default: null },
  currentUserRole: { type: String, default: 'MEMBER' }
})

const emit = defineEmits(['update:modelValue', 'add-members', '@-member', 'member-click'])

const store = useStore()
const currentUserId = computed(() => store.state.im.user?.id)

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const members = ref([])
const loading = ref(false)
const searchText = ref('')

const currentRole = computed(() => props.currentUserRole || 'MEMBER')

const canManage = computed(() => {
  return currentRole.value === 'OWNER' || currentRole.value === 'ADMIN'
})

// 过滤后的成员列表
const filteredMembers = computed(() => {
  if (!searchText.value) {
    return members.value
  }
  const keyword = searchText.value.toLowerCase()
  return members.value.filter(m =>
    m.name?.toLowerCase().includes(keyword) ||
    m.nickname?.toLowerCase().includes(keyword) ||
    m.username?.toLowerCase().includes(keyword)
  )
})

// 分离群主、管理员和普通成员
const owner = computed(() => filteredMembers.value.find(m => m.role === 'OWNER'))
const admins = computed(() => filteredMembers.value.filter(m => m.role === 'ADMIN'))
const normalMembers = computed(() => filteredMembers.value.filter(m => m.role === 'MEMBER'))

// 加载群成员列表
const loadMembers = async () => {
  if (!props.groupId) return

  loading.value = true
  try {
    const res = await getGroupMembers(props.groupId)
    if (res.code === 200) {
      members.value = res.data || []
    }
  } catch (error) {
    console.error('加载成员列表失败:', error)
    ElMessage.error('加载成员列表失败')
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) {
    searchText.value = ''
    loadMembers()
  }
})

const handleClose = () => {
  visible.value = false
}

const handleMemberClick = (member) => {
  emit('member-click', member)
}

const handleAtMember = (member) => {
  emit('@-member', member)
  handleClose()
}

const handleSetAdmin = async (member, isAdmin) => {
  try {
    await ElMessageBox.confirm(
      `确定要${isAdmin ? '设为管理员' : '取消管理员'} ${member.name} 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await setGroupAdmin(props.groupId, member.id, isAdmin)
    ElMessage.success(isAdmin ? '已设为管理员' : '已取消管理员')
    loadMembers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleMuteMember = async (member) => {
  try {
    const action = member.isMuted ? '取消禁言' : '禁言'
    const { value } = await ElMessageBox.prompt(
      `请输入${action}时长（分钟），0表示取消禁言`,
      `${action} ${member.name}`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValue: '60',
        inputPattern: /^\d+$/,
        inputErrorMessage: '请输入有效的分钟数'
      }
    )

    const duration = parseInt(value) || 0
    await muteGroupMember(props.groupId, member.id, duration)
    ElMessage.success(duration > 0 ? `已禁言 ${duration} 分钟` : '已取消禁言')
    loadMembers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleRemoveMember = async (member) => {
  try {
    await ElMessageBox.confirm(
      `确定要将 ${member.name} 移出群聊吗？`,
      '移出群聊',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await removeGroupMember(props.groupId, [member.id])
    ElMessage.success('已移出群聊')
    loadMembers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleAddMembers = () => {
  emit('add-members')
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

:deep(.group-members-drawer) {
  .el-drawer__body {
    padding: 0;
    overflow: hidden;
  }
}

.drawer-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--dt-bg-card);
}

.drawer-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--dt-border-light);

  .header-title {
    display: flex;
    align-items: center;
    gap: 8px;

    .back-btn {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: transparent;
      border: none;
      border-radius: 50%;
      cursor: pointer;
      color: var(--dt-text-secondary);

      &:hover {
        background: var(--dt-bg-hover);
      }

      .material-icons-outlined {
        font-size: 20px;
      }
    }

    .title-text {
      font-size: 16px;
      font-weight: 600;
      color: var(--dt-text-primary);
    }
  }
}

.search-section {
  padding: 12px 20px;
  border-bottom: 1px solid var(--dt-border-light);

  :deep(.el-input) {
    .el-input__wrapper {
      border-radius: 20px;
      padding: 4px 12px;
    }
  }
}

.members-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;

  .section-title {
    padding: 8px 4px;
    font-size: 12px;
    font-weight: 500;
    color: var(--dt-text-tertiary);
  }

  .member-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 12px;
    border-radius: 10px;
    cursor: pointer;
    transition: background 0.2s;

    &:hover {
      background: var(--dt-bg-hover);
    }

    .member-info {
      flex: 1;
      min-width: 0;

      .member-name {
        font-size: 14px;
        font-weight: 500;
        color: var(--dt-text-primary);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .member-role {
        font-size: 12px;
        margin-top: 2px;

        &.role-owner {
          color: #f59e0b;
        }

        &.role-admin {
          color: #8b5cf6;
        }

        &.role-member {
          color: var(--dt-text-tertiary);
        }
      }
    }

    .member-actions {
      display: flex;
      gap: 4px;
      opacity: 0;
      transition: opacity 0.2s;

      .el-button {
        .material-icons-outlined {
          font-size: 16px;
        }
      }

      :deep(.el-dropdown) {
        .el-button {
          padding: 4px 8px;
        }
      }
    }

    &:hover .member-actions {
      opacity: 1;
    }
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60px 20px;
    color: var(--dt-text-tertiary);

    .material-icons-outlined {
      font-size: 48px;
      opacity: 0.4;
      margin-bottom: 8px;
    }

    p {
      margin: 0;
      font-size: 14px;
    }
  }
}

.drawer-footer {
  padding: 16px 20px;
  border-top: 1px solid var(--dt-border-light);
  background: var(--dt-bg-body);

  .add-btn {
    width: 100%;
    height: 44px;
    border-radius: 12px;
    font-size: 15px;

    .material-icons-outlined {
      font-size: 18px;
      margin-right: 4px;
    }
  }
}

:deep(.el-dropdown-menu__item.danger) {
  color: var(--dt-error-color);

  &:hover {
    background: var(--dt-error-bg);
    color: var(--dt-error-color);
  }
}
</style>
