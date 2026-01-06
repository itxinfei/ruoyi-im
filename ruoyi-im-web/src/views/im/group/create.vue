<template>
  <div class="group-create-container">
    <div class="create-header">
      <h2>创建群组</h2>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="create-form">
      <el-form-item label="群组名称" prop="name">
        <el-input
          v-model="form.name"
          placeholder="请输入群组名称（2-50个字符）"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="群组头像" prop="avatar">
        <div class="avatar-upload-wrapper">
          <el-upload
            class="avatar-uploader"
            action="/api/im/file/upload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
            :headers="uploadHeaders"
          >
            <img v-if="form.avatar" :src="form.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div v-if="form.avatar" class="avatar-remove" @click="removeAvatar">
            <el-icon><Delete /></el-icon>
          </div>
        </div>
      </el-form-item>

      <el-form-item label="群组类型" prop="type">
        <el-radio-group v-model="form.type">
          <el-radio label="PUBLIC">公开群</el-radio>
          <el-radio label="PRIVATE">私密群</el-radio>
        </el-radio-group>
        <div class="form-tip">
          {{
            form.type === 'PUBLIC'
              ? '公开群：任何人都可以通过群号搜索到'
              : '私密群：只有受邀成员可以加入'
          }}
        </div>
      </el-form-item>

      <el-form-item label="群组描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="4"
          placeholder="请输入群组描述（选填）"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="群组成员" prop="memberIds">
        <div class="member-selector">
          <div class="selected-members">
            <el-tag
              v-for="member in selectedMembers"
              :key="member.id"
              :closable="true"
              class="member-tag"
              @close="removeMember(member.id)"
            >
              <el-avatar :size="20" :src="member.avatar" class="member-avatar">
                {{ (member.name || member.username)?.charAt(0) }}
              </el-avatar>
              {{ member.name || member.username }}
            </el-tag>
            <el-button
              v-if="selectedMembers.length > 0"
              text
              type="danger"
              @click="clearAllMembers"
            >
              清空
            </el-button>
          </div>
          <el-button :icon="Plus" @click="showMemberSelector = true"> 添加成员 </el-button>
        </div>
        <div class="form-tip">已选择 {{ selectedMembers.length }} 位成员</div>
      </el-form-item>

      <el-form-item label="群主管理" prop="ownerId">
        <el-select v-model="form.ownerId" placeholder="选择群主" style="width: 100%">
          <el-option
            v-for="member in [{ id: currentUserId, name: '我自己' }, ...selectedMembers]"
            :key="member.id"
            :label="member.name"
            :value="member.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="入群验证" prop="needApproval">
        <el-switch v-model="form.needApproval" active-text="需要验证" inactive-text="无需验证" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="submitForm"> 创建群组 </el-button>
        <el-button @click="goBack">取消</el-button>
      </el-form-item>
    </el-form>

    <!-- 成员选择器对话框 -->
    <el-dialog v-model="showMemberSelector" title="选择群组成员" width="600px">
      <div class="member-selector-dialog">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索联系人..."
          :prefix-icon="Search"
          clearable
          class="search-input"
        />
        <div class="contact-list">
          <div
            v-for="contact in filteredContacts"
            :key="contact.id"
            class="contact-item"
            :class="{ selected: isMemberSelected(contact.id) }"
            @click="toggleMember(contact)"
          >
            <el-avatar :size="40" :src="contact.avatar">
              {{ (contact.name || contact.username)?.charAt(0) }}
            </el-avatar>
            <div class="contact-info">
              <div class="contact-name">{{ contact.name || contact.username }}</div>
              <div class="contact-status">{{ contact.online ? '在线' : '离线' }}</div>
            </div>
            <el-icon v-if="isMemberSelected(contact.id)" class="check-icon">
              <Check />
            </el-icon>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showMemberSelector = false">取消</el-button>
        <el-button type="primary" @click="confirmMembers"
          >确定 ({{ selectedMembers.length }})</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Delete, Search, Check } from '@element-plus/icons-vue'
import { addGroup } from '@/api/im/group'
import { listContact } from '@/api/im/contact'
import { getToken } from '@/utils/auth'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const showMemberSelector = ref(false)
const searchKeyword = ref('')
const contacts = ref([])
const selectedMembers = ref([])

const currentUserId = computed(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return userInfo.userId || null
})

const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + getToken(),
}))

const form = reactive({
  name: '',
  avatar: '',
  description: '',
  type: 'PUBLIC',
  memberIds: [],
  ownerId: currentUserId.value,
  needApproval: false,
})

const rules = {
  name: [
    { required: true, message: '请输入群组名称', trigger: 'blur' },
    { min: 2, max: 50, message: '群组名称长度应为2-50个字符', trigger: 'blur' },
  ],
  memberIds: [
    { required: true, message: '请选择群组成员', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (selectedMembers.value.length === 0) {
          callback(new Error('至少需要选择一位成员'))
        } else {
          callback()
        }
      },
      trigger: 'change',
    },
  ],
}

// 过滤联系人
const filteredContacts = computed(() => {
  if (!searchKeyword.value) return contacts.value
  const keyword = searchKeyword.value.toLowerCase()
  return contacts.value.filter(c => {
    const name = (c.name || c.username || '').toLowerCase()
    return name.includes(keyword)
  })
})

// 判断是否已选择成员
const isMemberSelected = id => {
  return selectedMembers.value.some(m => m.id === id)
}

// 切换成员选择状态
const toggleMember = contact => {
  const index = selectedMembers.value.findIndex(m => m.id === contact.id)
  if (index > -1) {
    selectedMembers.value.splice(index, 1)
  } else {
    selectedMembers.value.push(contact)
  }
}

// 移除成员
const removeMember = id => {
  const index = selectedMembers.value.findIndex(m => m.id === id)
  if (index > -1) {
    selectedMembers.value.splice(index, 1)
  }
}

// 清空所有成员
const clearAllMembers = () => {
  selectedMembers.value = []
}

// 确认成员选择
const confirmMembers = () => {
  form.memberIds = selectedMembers.value.map(m => m.id)
  // 如果群主不在成员列表中，设置当前用户为群主
  if (!form.memberIds.includes(form.ownerId)) {
    form.ownerId = currentUserId.value
  }
  showMemberSelector.value = false
}

// 头像上传成功
const handleAvatarSuccess = response => {
  if (response.code === 200) {
    form.avatar = response.data.url
    ElMessage.success('头像上传成功')
  }
}

// 头像上传前验证
const beforeAvatarUpload = file => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
  }
  return isImage && isLt2M
}

// 移除头像
const removeAvatar = () => {
  form.avatar = ''
}

// 加载联系人列表
const loadContacts = async () => {
  try {
    const res = await listContact()
    if (res.code === 200) {
      const dataRows = res.data?.rows || res.rows || []
      contacts.value = Array.isArray(dataRows) ? dataRows : []
    }
  } catch (error) {
    console.error('加载联系人失败:', error)
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    const submitData = {
      ...form,
      memberIds: selectedMembers.value.map(m => m.id),
    }

    const res = await addGroup(submitData)
    if (res.code === 200) {
      ElMessage.success('群组创建成功')
      router.push('/im/group')
    }
  } catch (error) {
    console.error('创建群组失败:', error)
  } finally {
    submitting.value = false
  }
}

// 返回
const goBack = () => {
  router.back()
}

onMounted(() => {
  loadContacts()
})
</script>

<style lang="scss" scoped>
@use '@/styles/dingtalk-theme.scss' as *;

.group-create-container {
  height: 100%;
  overflow-y: auto;
  background-color: $bg-white;

  .create-header {
    padding: $spacing-lg;
    border-bottom: 1px solid $border-light;

    h2 {
      margin: 0;
      font-size: $font-size-xl;
      font-weight: $font-weight-semibold;
    }
  }

  .create-form {
    max-width: 600px;
    margin: $spacing-xl auto;
    padding: 0 $spacing-lg;

    .avatar-upload-wrapper {
      position: relative;
      display: inline-block;

      .avatar-remove {
        position: absolute;
        top: -8px;
        right: -8px;
        width: 24px;
        height: 24px;
        background: $error-color;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        cursor: pointer;
        font-size: 12px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);

        &:hover {
          background: #ff4d4f;
        }
      }
    }

    .avatar-uploader {
      .avatar {
        width: 100px;
        height: 100px;
        display: block;
        border-radius: 8px;
        object-fit: cover;
      }

      .avatar-uploader-icon {
        font-size: 28px;
        color: $text-tertiary;
        width: 100px;
        height: 100px;
        line-height: 100px;
        text-align: center;
        border: 1px dashed $border-base;
        border-radius: $border-radius-lg;
        cursor: pointer;
        transition: all 0.2s;

        &:hover {
          border-color: $primary-color;
          color: $primary-color;
        }
      }
    }

    .form-tip {
      font-size: 12px;
      color: $text-tertiary;
      margin-top: 4px;
    }

    .member-selector {
      width: 100%;

      .selected-members {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        margin-bottom: 8px;
        min-height: 32px;
        padding: 8px;
        border: 1px solid $border-base;
        border-radius: 4px;
        background: $bg-light;

        .member-tag {
          display: inline-flex;
          align-items: center;
          gap: 4px;
          padding: 4px 8px;
          background: white;
          border: 1px solid $border-light;

          .member-avatar {
            font-size: 10px;
          }
        }
      }
    }
  }
}

.member-selector-dialog {
  .search-input {
    margin-bottom: 16px;
  }

  .contact-list {
    max-height: 400px;
    overflow-y: auto;
    border: 1px solid $border-light;
    border-radius: 8px;
  }

  .contact-item {
    display: flex;
    align-items: center;
    padding: 12px;
    cursor: pointer;
    transition: background-color 0.2s;
    border-bottom: 1px solid $border-lighter;

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background-color: #f5f7fa;
    }

    &.selected {
      background-color: #e6f7ff;

      .check-icon {
        display: flex;
      }
    }

    .contact-info {
      flex: 1;
      margin-left: 12px;

      .contact-name {
        font-size: 14px;
        color: $text-primary;
        margin-bottom: 4px;
      }

      .contact-status {
        font-size: 12px;
        color: $text-tertiary;
      }
    }

    .check-icon {
      display: none;
      width: 20px;
      height: 20px;
      background: $primary-color;
      border-radius: 50%;
      color: white;
      font-size: 12px;

      .el-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 100%;
        height: 100%;
      }
    }
  }
}
</style>
