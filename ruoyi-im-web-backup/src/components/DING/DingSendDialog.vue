<template>
  <el-dialog
    v-model="dialogVisible"
    title="发送DING"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="dingForm" :rules="formRules" label-width="100px">
      <el-form-item label="接收对象" prop="receivers">
        <el-select
          v-model="dingForm.receivers"
          multiple
          filterable
          placeholder="选择接收人"
          style="width: 100%"
        >
          <el-option-group label="联系人">
            <el-option
              v-for="contact in contacts"
              :key="contact.id"
              :label="contact.name"
              :value="contact.id"
            />
          </el-option-group>
          <el-option-group label="群组">
            <el-option
              v-for="group in groups"
              :key="group.id"
              :label="group.name"
              :value="group.id"
            />
          </el-option-group>
        </el-select>
      </el-form-item>

      <el-form-item label="DING内容" prop="content">
        <el-input
          v-model="dingForm.content"
          type="textarea"
          :rows="4"
          placeholder="请输入DING内容"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="附件">
        <el-upload
          v-model:file-list="fileList"
          :action="uploadUrl"
          :headers="uploadHeaders"
          :on-success="handleUploadSuccess"
          :on-remove="handleRemoveFile"
          :before-upload="beforeUpload"
          :limit="5"
        >
          <el-button size="small" :icon="Upload">上传附件</el-button>
          <template #tip>
            <div class="el-upload__tip">支持图片、文档等文件，最多5个</div>
          </template>
        </el-upload>
      </el-form-item>

      <el-divider content-position="left">提醒方式</el-divider>

      <el-form-item label="应用内通知">
        <el-switch v-model="dingForm.appNotification" />
        <span class="setting-desc">在应用内推送通知</span>
      </el-form-item>

      <el-form-item label="短信提醒">
        <el-switch v-model="dingForm.smsNotification" />
        <span class="setting-desc">通过短信发送提醒（消耗短信额度）</span>
      </el-form-item>

      <el-form-item label="电话提醒">
        <el-switch v-model="dingForm.phoneNotification" />
        <span class="setting-desc">通过电话语音提醒（消耗电话额度）</span>
      </el-form-item>

      <el-divider content-position="left">发送设置</el-divider>

      <el-form-item label="立即发送">
        <el-switch v-model="dingForm.immediate" @change="handleImmediateChange" />
      </el-form-item>

      <el-form-item v-if="!dingForm.immediate" label="定时发送" prop="scheduledTime">
        <el-date-picker
          v-model="dingForm.scheduledTime"
          type="datetime"
          placeholder="选择发送时间"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DD HH:mm:ss"
          :disabled-date="disabledDate"
          :disabled-hours="disabledHours"
          :disabled-minutes="disabledMinutes"
        />
      </el-form-item>

      <el-form-item label="已读回执">
        <el-switch v-model="dingForm.readReceipt" />
        <span class="setting-desc">查看谁已读、谁未读</span>
      </el-form-item>

      <el-form-item label="DING模板">
        <el-select
          v-model="dingForm.templateId"
          placeholder="选择模板（可选）"
          clearable
          style="width: 100%"
          @change="handleTemplateChange"
        >
          <el-option
            v-for="template in templates"
            :key="template.id"
            :label="template.name"
            :value="template.id"
          />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="sending" @click="handleSend">
        {{ dingForm.immediate ? '立即发送' : '定时发送' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { getToken } from '@/utils/auth'

const props = defineProps({
  visible: Boolean,
})

const emit = defineEmits(['update:visible', 'sent'])

const dialogVisible = ref(false)
const sending = ref(false)
const formRef = ref(null)
const fileList = ref([])
const contacts = ref([])
const groups = ref([])
const templates = ref([])

const uploadUrl = computed(() => {
  return process.env.VUE_APP_BASE_API + '/api/im/file/upload'
})

const uploadHeaders = computed(() => {
  return {
    Authorization: 'Bearer ' + getToken(),
  }
})

const dingForm = reactive({
  receivers: [],
  content: '',
  attachments: [],
  appNotification: true,
  smsNotification: false,
  phoneNotification: false,
  immediate: true,
  scheduledTime: '',
  readReceipt: true,
  templateId: '',
})

const formRules = {
  receivers: [{ required: true, message: '请选择接收对象', trigger: 'change' }],
  content: [{ required: true, message: '请输入DING内容', trigger: 'blur' }],
  scheduledTime: [
    {
      validator: (rule, value, callback) => {
        if (!dingForm.immediate && !value) {
          callback(new Error('请选择发送时间'))
        } else {
          callback()
        }
      },
      trigger: 'change',
    },
  ],
}

const loadContacts = async () => {
  try {
    const response = await fetch('/api/im/contact/list', {
      headers: {
        Authorization: 'Bearer ' + getToken(),
      },
    })
    const data = await response.json()
    if (data.code === 200) {
      contacts.value = data.data || []
    }
  } catch (error) {
    console.error('加载联系人失败:', error)
  }
}

const loadGroups = async () => {
  try {
    const response = await fetch('/api/im/group/list', {
      headers: {
        Authorization: 'Bearer ' + getToken(),
      },
    })
    const data = await response.json()
    if (data.code === 200) {
      groups.value = data.data || []
    }
  } catch (error) {
    console.error('加载群组失败:', error)
  }
}

const loadTemplates = async () => {
  try {
    const response = await fetch('/api/im/ding/template/list', {
      headers: {
        Authorization: 'Bearer ' + getToken(),
      },
    })
    const data = await response.json()
    if (data.code === 200) {
      templates.value = data.data || []
    }
  } catch (error) {
    console.error('加载模板失败:', error)
  }
}

const handleImmediateChange = () => {
  if (dingForm.immediate) {
    dingForm.scheduledTime = ''
  }
}

const disabledDate = time => {
  return time.getTime() < Date.now() - 8.64e7
}

const disabledHours = () => {
  const hours = []
  const now = new Date()
  if (dingForm.scheduledTime) {
    const scheduledDate = new Date(dingForm.scheduledTime)
    if (scheduledDate.toDateString() === now.toDateString()) {
      for (let i = 0; i < now.getHours(); i++) {
        hours.push(i)
      }
    }
  }
  return hours
}

const disabledMinutes = hour => {
  const minutes = []
  const now = new Date()
  if (dingForm.scheduledTime) {
    const scheduledDate = new Date(dingForm.scheduledTime)
    if (scheduledDate.toDateString() === now.toDateString() && hour === now.getHours()) {
      for (let i = 0; i < now.getMinutes(); i++) {
        minutes.push(i)
      }
    }
  }
  return minutes
}

const beforeUpload = file => {
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过10MB')
    return false
  }
  return true
}

const handleUploadSuccess = response => {
  if (response.code === 200) {
    dingForm.attachments.push({
      name: response.data.fileName,
      url: response.data.url,
      size: response.data.fileSize,
    })
    ElMessage.success('上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

const handleRemoveFile = file => {
  const index = dingForm.attachments.findIndex(item => item.url === file.url)
  if (index > -1) {
    dingForm.attachments.splice(index, 1)
  }
}

const handleTemplateChange = templateId => {
  const template = templates.value.find(t => t.id === templateId)
  if (template) {
    dingForm.content = template.content
  }
}

const handleSend = async () => {
  try {
    await formRef.value.validate()
  } catch (error) {
    return
  }

  sending.value = true
  try {
    const response = await fetch('/api/im/ding/send', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + getToken(),
      },
      body: JSON.stringify(dingForm),
    })
    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success('DING发送成功')
      emit('sent')
      handleClose()
    } else {
      ElMessage.error(data.msg || '发送失败')
    }
  } catch (error) {
    console.error('发送DING失败:', error)
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

const handleClose = () => {
  dialogVisible.value = false
  emit('update:visible', false)
  resetForm()
}

const resetForm = () => {
  dingForm.receivers = []
  dingForm.content = ''
  dingForm.attachments = []
  dingForm.appNotification = true
  dingForm.smsNotification = false
  dingForm.phoneNotification = false
  dingForm.immediate = true
  dingForm.scheduledTime = ''
  dingForm.readReceipt = true
  dingForm.templateId = ''
  fileList.value = []
  formRef.value?.resetFields()
}

watch(
  () => props.visible,
  visible => {
    dialogVisible.value = visible
    if (visible) {
      loadContacts()
      loadGroups()
      loadTemplates()
    }
  }
)

watch(dialogVisible, val => {
  if (!val) {
    emit('update:visible', false)
  }
})
</script>

<style lang="scss" scoped>
.setting-desc {
  margin-left: 12px;
  font-size: 12px;
  color: #999;
}

:deep(.el-form-item__content) {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}
</style>
