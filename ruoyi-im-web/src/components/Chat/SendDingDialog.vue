<template>
  <div class="ding-dialog">
    <el-dialog
      v-model="visible"
      title="DING强提醒"
      width="500px"
      :close-on-click-modal="false"
      @close="handleClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="提醒内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="请输入DING提醒内容（必填）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="提醒类型" prop="dingType">
          <el-radio-group v-model="form.dingType">
            <el-radio label="APP">应用内提醒</el-radio>
            <!-- 短信和电话提醒需要第三方服务支持，暂时禁用 -->
            <el-radio label="SMS" disabled>短信提醒</el-radio>
            <el-radio label="CALL" disabled>电话提醒</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="优先级" prop="priority">
          <el-radio-group v-model="form.priority">
            <el-radio label="NORMAL">普通</el-radio>
            <el-radio label="URGENT">紧急</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="提醒对象" prop="targetUsers">
          <div class="target-users-selector">
            <el-checkbox v-model="selectAll" @change="handleSelectAll">
              全选会话成员
            </el-checkbox>
            <el-divider />
            <div v-if="conversationMembers.length > 0" class="members-list">
              <el-checkbox-group v-model="form.targetUsers">
                <el-checkbox
                  v-for="member in conversationMembers"
                  :key="member.userId"
                  :label="member.userId"
                >
                  <div class="member-item">
                    <DingtalkAvatar
                      :size="32"
                      :src="member.avatar"
                      :name="member.nickname || member.username"
                    />
                    <span>{{ member.nickname || member.username }}</span>
                  </div>
                </el-checkbox>
              </el-checkbox-group>
            </div>
            <div v-else class="empty-tip">暂无可选成员</div>
          </div>
        </el-form-item>

        <el-form-item label="过期时间" prop="expireHours">
          <el-select v-model="form.expireHours" placeholder="请选择" style="width: 100%">
            <el-option label="1小时" :value="1" />
            <el-option label="6小时" :value="6" />
            <el-option label="12小时" :value="12" />
            <el-option label="24小时" :value="24" />
            <el-option label="48小时" :value="48" />
            <el-option label="72小时" :value="72" />
          </el-select>
        </el-form-item>

        <el-form-item label="需要回执" prop="receiptRequired">
          <el-switch v-model="form.receiptRequired" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="handleClose">取消</el-button>
        <el-button type="danger" :loading="loading" @click="handleSubmit">
          发送DING
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { sendDing } from '@/api/im/ding'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'
import { getGroupMembers } from '@/api/im/group'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  conversationId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(false)
const formRef = ref(null)
const loading = ref(false)
const conversationMembers = ref([])

const form = reactive({
  content: '',
  dingType: 'APP',
  priority: 'NORMAL',
  targetUsers: [],
  expireHours: 24,
  receiptRequired: true
})

const rules = {
  content: [
    { required: true, message: '请输入DING提醒内容', trigger: 'blur' },
    { min: 1, max: 500, message: '内容长度在 1 到 500 个字符', trigger: 'blur' }
  ],
  targetUsers: [
    { required: true, message: '请选择至少一个提醒对象', trigger: 'change' },
    { type: 'array', min: 1, message: '请选择至少一个提醒对象', trigger: 'change' }
  ]
}

// 全选功能
const selectAll = computed({
  get: () => {
    return conversationMembers.value.length > 0 &&
           form.targetUsers.length === conversationMembers.value.length
  },
  set: (val) => {
    if (val) {
      form.targetUsers = conversationMembers.value.map(m => m.userId)
    } else {
      form.targetUsers = []
    }
  }
})

const handleSelectAll = (val) => {
  selectAll.value = val
}

// 加载会话成员
const loadConversationMembers = async () => {
  if (!props.conversationId) {
    return
  }
  try {
    const response = await getGroupMembers(props.conversationId)
    if (response && response.data) {
      conversationMembers.value = response.data.filter(m => m.groupRole !== 'OWNER')
      // 默认全选
      form.targetUsers = conversationMembers.value.map(m => m.userId)
    }
  } catch (error) {
    console.error('加载会话成员失败:', error)
  }
}

// 提交发送
const handleSubmit = async () => {
  await formRef.value.validate()

  loading.value = true
  try {
    const response = await sendDing({
      conversationId: props.conversationId,
      content: form.content,
      dingType: form.dingType,
      priority: form.priority,
      targetUsers: form.targetUsers,
      expireHours: form.expireHours,
      receiptRequired: form.receiptRequired
    })

    if (response) {
      ElMessage.success('DING消息发送成功')
      emit('success')
      handleClose()
    }
  } catch (error) {
    console.error('发送DING失败:', error)
    ElMessage.error(error.message || '发送DING失败')
  } finally {
    loading.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  formRef.value?.resetFields()
  form.content = ''
  form.dingType = 'APP'
  form.priority = 'NORMAL'
  form.targetUsers = []
  form.expireHours = 24
  form.receiptRequired = true
  conversationMembers.value = []
  visible.value = false
  emit('update:modelValue', false)
}

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    loadConversationMembers()
  }
})

watch(visible, (val) => {
  if (!val) {
    emit('update:modelValue', false)
  }
})
</script>

<style lang="scss" scoped>
.ding-dialog {
  :deep(.el-dialog__body) {
    padding-top: 20px;
  }

  .target-users-selector {
    width: 100%;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    padding: 10px;

    .el-divider {
      margin: 10px 0;
    }

    .members-list {
      max-height: 200px;
      overflow-y: auto;

      .el-checkbox-group {
        display: flex;
        flex-direction: column;
        gap: 8px;
      }

      .el-checkbox {
        margin-right: 0;

        .member-item {
          display: flex;
          align-items: center;
          gap: 8px;
        }
      }
    }

    .empty-tip {
      color: #999;
      text-align: center;
      padding: 20px 0;
    }
  }
}
</style>
