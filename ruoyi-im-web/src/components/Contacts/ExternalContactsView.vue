<template>
  <div class="external-contacts-view">
    <!-- 头部 -->
    <header class="view-header">
      <div class="header-left">
        <span class="view-title">外部联系人</span>
        <span class="count-badge">{{ contacts.length }}</span>
      </div>
      <el-button type="primary" size="small" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        添加联系人
      </el-button>
    </header>

    <!-- 搜索和筛选 -->
    <div class="filter-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索外部联系人"
        prefix-icon="Search"
        clearable
        size="small"
        class="search-input"
        @input="handleSearch"
      />
      <el-select
        v-model="selectedGroupId"
        placeholder="全部分组"
        clearable
        size="small"
        class="group-select"
        @change="handleGroupChange"
      >
        <el-option
          v-for="group in groups"
          :key="group.id"
          :label="group.name"
          :value="group.id"
        />
      </el-select>
      <el-button
        size="small"
        :type="showStarredOnly ? 'primary' : 'default'"
        @click="toggleStarred"
      >
        <el-icon><StarFilled /></el-icon>
        星标
      </el-button>
    </div>

    <!-- 联系人列表 -->
    <div class="view-content">
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <div v-else-if="filteredContacts.length === 0" class="empty-state">
        <el-icon class="empty-icon"><UserFilled /></el-icon>
        <p class="empty-text">{{ searchKeyword ? '未找到匹配的联系人' : '暂无外部联系人' }}</p>
        <el-button v-if="!searchKeyword" type="primary" size="small" @click="showAddDialog = true">
          添加联系人
        </el-button>
      </div>

      <div v-else class="contacts-list">
        <div
          v-for="contact in filteredContacts"
          :key="contact.id"
          class="contact-item"
          @click="handleViewDetail(contact)"
        >
          <DingtalkAvatar
            :src="contact.avatar"
            :name="contact.name"
            :size="44"
            shape="square"
          />
          <div class="contact-info">
            <div class="contact-name">
              <span class="name">{{ contact.name }}</span>
              <el-icon v-if="contact.starred" class="starred-icon"><StarFilled /></el-icon>
            </div>
            <div class="contact-meta">
              <span v-if="contact.company" class="company">{{ contact.company }}</span>
              <span v-if="contact.position" class="position">{{ contact.position }}</span>
            </div>
            <div v-if="contact.mobile" class="contact-mobile">
              {{ contact.mobile }}
            </div>
          </div>
          <div class="contact-actions">
            <el-button
              text
              circle
              size="small"
              @click.stop="handleToggleStar(contact)"
            >
              <el-icon :class="{ 'is-starred': contact.starred }">
                <StarFilled v-if="contact.starred" />
                <Star v-else />
              </el-icon>
            </el-button>
            <el-dropdown trigger="click" @command="handleCommand($event, contact)">
              <el-button text circle size="small" @click.stop>
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">编辑</el-dropdown-item>
                  <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑联系人弹窗 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingContact ? '编辑联系人' : '添加外部联系人'"
      width="480px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="公司" prop="company">
          <el-input v-model="formData.company" placeholder="请输入公司名称" />
        </el-form-item>
        <el-form-item label="职位" prop="position">
          <el-input v-model="formData.position" placeholder="请输入职位" />
        </el-form-item>
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="formData.mobile" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="分组" prop="groupId">
          <el-select v-model="formData.groupId" placeholder="选择分组" clearable style="width: 100%">
            <el-option
              v-for="group in groups"
              :key="group.id"
              :label="group.name"
              :value="group.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ editingContact ? '保存' : '添加' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 联系人详情弹窗 -->
    <el-dialog
      v-model="showDetailDialog"
      title="联系人详情"
      width="400px"
      destroy-on-close
    >
      <div v-if="currentContact" class="detail-content">
        <div class="detail-header">
          <DingtalkAvatar
            :src="currentContact.avatar"
            :name="currentContact.name"
            :size="72"
            shape="square"
          />
          <div class="detail-name">{{ currentContact.name }}</div>
          <div class="detail-company" v-if="currentContact.company">
            {{ currentContact.company }} · {{ currentContact.position || '职位未知' }}
          </div>
        </div>
        <div class="detail-info">
          <div class="info-item" v-if="currentContact.mobile">
            <span class="label">手机</span>
            <span class="value">{{ currentContact.mobile }}</span>
          </div>
          <div class="info-item" v-if="currentContact.email">
            <span class="label">邮箱</span>
            <span class="value">{{ currentContact.email }}</span>
          </div>
          <div class="info-item" v-if="currentContact.remark">
            <span class="label">备注</span>
            <span class="value">{{ currentContact.remark }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="handleEditFromDetail">编辑</el-button>
        <el-button type="primary" @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Star, StarFilled, UserFilled, MoreFilled, Loading } from '@element-plus/icons-vue'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import {
  getExternalContactList,
  getExternalContactsByGroup,
  getStarredExternalContacts,
  searchExternalContacts,
  createExternalContact,
  updateExternalContact,
  deleteExternalContact,
  toggleExternalContactStar,
  getExternalContactGroupList
} from '@/api/im/externalContact'

defineEmits(['back'])

const loading = ref(false)
const contacts = ref([])
const groups = ref([])
const searchKeyword = ref('')
const selectedGroupId = ref(null)
const showStarredOnly = ref(false)
const showAddDialog = ref(false)
const showDetailDialog = ref(false)
const editingContact = ref(null)
const currentContact = ref(null)
const submitting = ref(false)
const formRef = ref(null)

const formData = ref({
  name: '',
  company: '',
  position: '',
  mobile: '',
  email: '',
  groupId: null,
  remark: ''
})

const formRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const filteredContacts = computed(() => {
  if (showStarredOnly.value) {
    return contacts.value.filter(c => c.starred)
  }
  return contacts.value
})

const loadGroups = async () => {
  try {
    const res = await getExternalContactGroupList()
    if (res.code === 200) {
      groups.value = res.data || []
    }
  } catch (e) {
    console.error('加载分组失败', e)
  }
}

const loadContacts = async () => {
  loading.value = true
  try {
    let res
    if (selectedGroupId.value) {
      res = await getExternalContactsByGroup(selectedGroupId.value)
    } else if (showStarredOnly.value) {
      res = await getStarredExternalContacts()
    } else {
      res = await getExternalContactList()
    }
    if (res.code === 200) {
      contacts.value = res.data || []
    }
  } catch (e) {
    console.error('加载联系人失败', e)
    ElMessage.error('加载联系人失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    loadContacts()
    return
  }
  loading.value = true
  try {
    const res = await searchExternalContacts(searchKeyword.value)
    if (res.code === 200) {
      contacts.value = res.data || []
    }
  } catch (e) {
    console.error('搜索失败', e)
  } finally {
    loading.value = false
  }
}

const handleGroupChange = () => {
  showStarredOnly.value = false
  loadContacts()
}

const toggleStarred = () => {
  showStarredOnly.value = !showStarredOnly.value
  selectedGroupId.value = null
  loadContacts()
}

const handleToggleStar = async (contact) => {
  try {
    const res = await toggleExternalContactStar(contact.id)
    if (res.code === 200) {
      contact.starred = !contact.starred
      ElMessage.success(contact.starred ? '已设为星标' : '已取消星标')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleViewDetail = (contact) => {
  currentContact.value = contact
  showDetailDialog.value = true
}

const handleEditFromDetail = () => {
  showDetailDialog.value = false
  openEditDialog(currentContact.value)
}

const handleCommand = (command, contact) => {
  if (command === 'edit') {
    openEditDialog(contact)
  } else if (command === 'delete') {
    handleDelete(contact)
  }
}

const openEditDialog = (contact) => {
  editingContact.value = contact
  formData.value = {
    name: contact.name || '',
    company: contact.company || '',
    position: contact.position || '',
    mobile: contact.mobile || '',
    email: contact.email || '',
    groupId: contact.groupId || null,
    remark: contact.remark || ''
  }
  showAddDialog.value = true
}

const handleDelete = async (contact) => {
  try {
    await ElMessageBox.confirm(`确定删除联系人"${contact.name}"吗？`, '删除确认', {
      type: 'warning'
    })
    const res = await deleteExternalContact(contact.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadContacts()
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    let res
    if (editingContact.value) {
      res = await updateExternalContact(editingContact.value.id, formData.value)
    } else {
      res = await createExternalContact(formData.value)
    }
    if (res.code === 200) {
      ElMessage.success(editingContact.value ? '更新成功' : '添加成功')
      showAddDialog.value = false
      resetForm()
      loadContacts()
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  editingContact.value = null
  formData.value = {
    name: '',
    company: '',
    position: '',
    mobile: '',
    email: '',
    groupId: null,
    remark: ''
  }
}

onMounted(() => {
  loadGroups()
  loadContacts()
})
</script>

<style scoped lang="scss">
.external-contacts-view {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--dt-bg-body);
}

.view-header {
  height: 56px;
  padding: 0 var(--dt-spacing-lg);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);
  flex-shrink: 0;

  .header-left {
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-sm);
  }

  .view-title {
    font-size: 18px;
    font-weight: 600;
    color: var(--dt-text-primary);
  }

  .count-badge {
    font-size: 12px;
    color: var(--dt-text-tertiary);
    background: var(--dt-bg-body);
    padding: 2px 8px;
    border-radius: var(--dt-radius-lg);
  }
}

.filter-bar {
  padding: var(--dt-spacing-md) var(--dt-spacing-lg);
  display: flex;
  gap: var(--dt-spacing-sm);
  background: var(--dt-bg-card);
  border-bottom: 1px solid var(--dt-border-light);
  flex-shrink: 0;

  .search-input {
    flex: 1;
    max-width: 240px;
  }

  .group-select {
    width: 120px;
  }
}

.view-content {
  flex: 1;
  padding: var(--dt-spacing-lg);
  overflow-y: auto;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: var(--dt-text-tertiary);
  gap: var(--dt-spacing-sm);

  .empty-icon {
    font-size: 48px;
  }

  .empty-text {
    font-size: 14px;
    margin: 0;
  }
}

.contacts-list {
  display: flex;
  flex-direction: column;
  gap: var(--dt-spacing-sm);
}

.contact-item {
  background: var(--dt-bg-card);
  border-radius: var(--dt-radius-lg);
  padding: var(--dt-spacing-md);
  display: flex;
  align-items: center;
  gap: var(--dt-spacing-md);
  cursor: pointer;
  transition: all var(--dt-transition-fast);
  border: 1px solid var(--dt-border-lighter);

  &:hover {
    box-shadow: var(--dt-shadow-card-hover);
    border-color: var(--dt-brand-light);

    .contact-actions {
      opacity: 1;
    }
  }
}

.contact-info {
  flex: 1;
  min-width: 0;

  .contact-name {
    display: flex;
    align-items: center;
    gap: var(--dt-spacing-xs);

    .name {
      font-size: 15px;
      font-weight: 500;
      color: var(--dt-text-primary);
    }

    .starred-icon {
      color: var(--dt-warning-color);
      font-size: 14px;
    }
  }

  .contact-meta {
    font-size: 12px;
    color: var(--dt-text-tertiary);
    margin-top: 2px;

    .company + .position::before {
      content: ' · ';
    }
  }

  .contact-mobile {
    font-size: 12px;
    color: var(--dt-text-secondary);
    margin-top: 4px;
  }
}

.contact-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity var(--dt-transition-fast);

  .is-starred {
    color: var(--dt-warning-color);
  }
}

// 详情弹窗样式
.detail-content {
  .detail-header {
    text-align: center;
    padding: var(--dt-spacing-lg) 0;

    .detail-name {
      font-size: 18px;
      font-weight: 600;
      color: var(--dt-text-primary);
      margin-top: var(--dt-spacing-md);
    }

    .detail-company {
      font-size: 13px;
      color: var(--dt-text-tertiary);
      margin-top: 4px;
    }
  }

  .detail-info {
    .info-item {
      display: flex;
      padding: var(--dt-spacing-sm) 0;
      border-bottom: 1px solid var(--dt-border-lighter);

      &:last-child {
        border-bottom: none;
      }

      .label {
        width: 60px;
        color: var(--dt-text-tertiary);
        flex-shrink: 0;
      }

      .value {
        flex: 1;
        color: var(--dt-text-primary);
      }
    }
  }
}
</style>
