<template>
  <el-dialog
    v-model="dialogVisible"
    title="发起审批"
    width="700px"
    :close-on-click-modal="false"
    @closed="handleClosed"
  >
    <div class="create-approval-dialog">
      <!-- 审批类型选择 -->
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        label-position="left"
      >
        <!-- 审批类型 -->
        <el-form-item
          label="审批类型"
          prop="type"
        >
          <el-select
            v-model="formData.type"
            placeholder="请选择审批类型"
            style="width: 100%"
            @change="handleTypeChange"
          >
            <el-option
              v-for="item in approvalTypes"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
              <span
                class="material-icons-outlined"
                style="margin-right: 8px"
              >{{ item.icon }}</span>
              <span>{{ item.label }}</span>
            </el-option>
          </el-select>
        </el-form-item>

        <!-- 审批事由 -->
        <el-form-item
          label="审批事由"
          prop="reason"
        >
          <el-input
            v-model="formData.reason"
            type="textarea"
            :rows="3"
            placeholder="请说明审批事由..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <!-- 开始时间 -->
        <el-form-item
          label="开始时间"
          prop="startTime"
        >
          <el-date-picker
            v-model="formData.startTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>

        <!-- 结束时间 -->
        <el-form-item
          label="结束时间"
          prop="endTime"
        >
          <el-date-picker
            v-model="formData.endTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>

        <!-- 审批人 -->
        <el-form-item
          label="审批人"
          prop="approverId"
        >
          <el-select
            v-model="formData.approverId"
            placeholder="请选择审批人"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="user in approverList"
              :key="user.id"
              :label="user.name"
              :value="user.id"
            >
              <div class="user-option">
                <DingtalkAvatar
                  :name="user.name"
                  :size="24"
                />
                <span style="margin-left: 8px">{{ user.name }}</span>
                <span
                  v-if="user.department"
                  class="user-dept"
                >{{ user.department }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <!-- 附件 -->
        <el-form-item label="附件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="5"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
          >
            <el-button
              type="primary"
              plain
            >
              <span class="material-icons-outlined">upload_file</span>
              选择文件
            </el-button>
            <template #tip>
              <div class="upload-tip">
                最多上传 5 个文件，单个不超过 10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <!-- 备注 -->
        <el-form-item label="备注">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="2"
            placeholder="选填"
            maxlength="200"
          />
        </el-form-item>
      </el-form>

      <!-- 预览提示 -->
      <div class="preview-hint">
        <span class="material-icons-outlined">info</span>
        <span>提交后审批将发送给审批人，你可以在"工作台 - 审批"中查看进度</span>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="loading"
          @click="handleSubmit"
        >
          提交审批
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  modelValue: Boolean,
  sessionId: {
    type: [String, Number],
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'success', 'closed'])

const dialogVisible = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})

const formRef = ref(null)
const uploadRef = ref(null)
const loading = ref(false)
const fileList = ref([])

const formData = ref({
  type: '',
  reason: '',
  startTime: '',
  endTime: '',
  approverId: '',
  remark: ''
})

const formRules = {
  type: [
    { required: true, message: '请选择审批类型', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入审批事由', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ],
  approverId: [
    { required: true, message: '请选择审批人', trigger: 'change' }
  ]
}

const approvalTypes = [
  { value: 'leave', label: '请假审批', icon: 'flight_land' },
  { value: 'overtime', label: '加班申请', icon: 'schedule' },
  { value: 'expense', label: '报销申请', icon: 'receipt' },
  { value: 'purchase', label: '采购申请', icon: 'shopping_cart' },
  { value: 'contract', label: '合同审批', icon: 'description' },
  { value: 'other', label: '其他审批', icon: 'more_horiz' }
]

const approverList = ref([
  { id: 1, name: '张经理', department: '技术部' },
  { id: 2, name: '李总监', department: '产品部' }
  // TODO: 从后端获取审批人列表
])

watch(() => props.modelValue, val => {
  if (val) {
    // 重置表单
    formData.value = {
      type: '',
      reason: '',
      startTime: '',
      endTime: '',
      approverId: '',
      remark: ''
    }
    fileList.value = []
  }
})

const handleTypeChange = () => {
  // 根据类型调整表单
}

const handleFileChange = (file, files) => {
  fileList.value = files
}

const handleFileRemove = (file, files) => {
  fileList.value = files
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  // 验证时间
  if (formData.value.startTime && formData.value.endTime) {
    if (new Date(formData.value.startTime) >= new Date(formData.value.endTime)) {
      ElMessage.warning('结束时间必须晚于开始时间')
      return
    }
  }

  loading.value = true
  try {
    // TODO: 调用后端 API
    await new Promise(resolve => setTimeout(resolve, 1000))

    ElMessage.success('审批提交成功')
    emit('success', formData.value)
    dialogVisible.value = false
  } catch (error) {
    ElMessage.error('提交失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const handleClosed = () => {
  emit('closed')
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.create-approval-dialog {
  padding: 8px 0;
}

.user-option {
  display: flex;
  align-items: center;
}

.user-dept {
  margin-left: 8px;
  font-size: 12px;
  color: var(--dt-text-tertiary, #858e9e);
}

.upload-tip {
  font-size: 12px;
  color: var(--dt-text-tertiary, #858e9e);
  margin-top: 4px;
}

.preview-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: var(--dt-info-bg, #e6f4ff);
  border-radius: 8px;
  font-size: 13px;
  color: var(--dt-info-color, #3296fa);

  .material-icons-outlined {
    font-size: 18px;
    flex-shrink: 0;
  }

  .dark & {
    background: rgba(50, 150, 250, 0.15);
    color: var(--dt-brand-color, #3296fa);
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
