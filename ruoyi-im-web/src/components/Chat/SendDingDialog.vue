<template>
  <div class="ding-dialog">
    <el-dialog
      v-model="visible"
      title="DING强提醒"
      width="520px"
      :close-on-click-modal="false"
      @close="handleClose"
      class="ding-modal"
    >
      <div class="dialog-content">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          class="ding-form"
        >
          <!-- 提醒内容 -->
          <el-form-item label="提醒内容" prop="content">
            <div class="content-input-wrapper">
              <el-input
                v-model="form.content"
                type="textarea"
                :rows="4"
                placeholder="请输入DING提醒内容（必填）"
                maxlength="500"
                show-word-limit
                class="custom-textarea"
              />
              <div class="input-decoration"></div>
            </div>
          </el-form-item>

          <!-- 提醒类型 -->
          <el-form-item label="提醒类型" prop="dingType">
            <div class="type-selector">
              <div
                v-for="type in dingTypes"
                :key="type.value"
                class="type-option"
                :class="{
                  active: form.dingType === type.value,
                  disabled: type.disabled
                }"
                @click="!type.disabled && (form.dingType = type.value)"
              >
                <div class="type-icon">{{ type.icon }}</div>
                <div class="type-info">
                  <span class="type-label">{{ type.label }}</span>
                  <span v-if="type.disabled" class="type-disabled-tip">需要第三方服务</span>
                </div>
                <div v-if="form.dingType === type.value" class="type-glow"></div>
              </div>
            </div>
          </el-form-item>

          <!-- 优先级 -->
          <el-form-item label="优先级" prop="priority">
            <div class="priority-selector">
              <div
                v-for="level in priorityLevels"
                :key="level.value"
                class="priority-option"
                :class="{ active: form.priority === level.value }"
                @click="form.priority = level.value"
              >
                <div class="priority-icon">{{ level.icon }}</div>
                <span class="priority-label">{{ level.label }}</span>
                <div v-if="form.priority === level.value" class="priority-glow"></div>
              </div>
            </div>
          </el-form-item>

          <!-- 提醒对象 -->
          <el-form-item label="提醒对象" prop="targetUsers">
            <div class="target-users-wrapper">
              <div class="select-all-bar">
                <el-checkbox v-model="selectAll" @change="handleSelectAll" class="custom-checkbox">
                  <span class="checkbox-label">
                    <span class="checkbox-icon">👥</span>
                    <span>全选会话成员</span>
                  </span>
                </el-checkbox>
              </div>
              <el-divider class="custom-divider" />
              <div v-if="conversationMembers.length > 0" class="members-list">
                <el-checkbox-group v-model="form.targetUsers">
                  <div
                    v-for="member in conversationMembers"
                    :key="member.userId"
                    class="member-card"
                  >
                    <el-checkbox :label="member.userId">
                      <div class="member-item">
                        <div class="member-avatar-wrapper">
                          <DingtalkAvatar
                            :size="36"
                            :src="member.avatar"
                            :name="member.nickname || member.username"
                          />
                          <div class="avatar-glow"></div>
                        </div>
                        <span class="member-name">{{ member.nickname || member.username }}</span>
                      </div>
                    </el-checkbox>
                  </div>
                </el-checkbox-group>
              </div>
              <div v-else class="empty-tip">
                <span class="empty-icon">📭</span>
                <span>暂无可选成员</span>
              </div>
            </div>
          </el-form-item>

          <!-- 过期时间 -->
          <el-form-item label="过期时间" prop="expireHours">
            <div class="expire-selector">
              <div
                v-for="option in expireOptions"
                :key="option.value"
                class="expire-option"
                :class="{ active: form.expireHours === option.value }"
                @click="form.expireHours = option.value"
              >
                <span class="expire-label">{{ option.label }}</span>
                <div class="expire-decoration"></div>
              </div>
            </div>
          </el-form-item>

          <!-- 需要回执 -->
          <el-form-item label="需要回执" prop="receiptRequired">
            <div class="receipt-toggle">
              <el-switch
                v-model="form.receiptRequired"
                active-text="需要回执"
                inactive-text="不需要回执"
                class="custom-switch"
              />
              <div class="switch-glow"></div>
            </div>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleClose" class="footer-btn footer-btn--cancel">
            <span class="btn-icon">✕</span>
            <span>取消</span>
          </el-button>
          <el-button type="danger" :loading="loading" @click="handleSubmit" class="footer-btn footer-btn--submit">
            <span class="btn-icon">📨</span>
            <span>发送DING</span>
          </el-button>
        </div>
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

// DING类型选项
const dingTypes = [
  {
    value: 'APP',
    label: '应用内提醒',
    icon: '📱',
    disabled: false
  },
  {
    value: 'SMS',
    label: '短信提醒',
    icon: '💬',
    disabled: true
  },
  {
    value: 'CALL',
    label: '电话提醒',
    icon: '📞',
    disabled: true
  }
]

// 优先级选项
const priorityLevels = [
  {
    value: 'NORMAL',
    label: '普通',
    icon: '📢'
  },
  {
    value: 'URGENT',
    label: '紧急',
    icon: '⚡'
  }
]

// 过期时间选项
const expireOptions = [
  { value: 1, label: '1小时' },
  { value: 6, label: '6小时' },
  { value: 12, label: '12小时' },
  { value: 24, label: '24小时' },
  { value: 48, label: '48小时' },
  { value: 72, label: '72小时' }
]

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
  // 对话框内容
  .dialog-content {
    padding: 20px 0;
  }

  .ding-form {
    :deep(.el-form-item__label) {
      font-weight: 600;
      color: #1a1a1a;
      font-size: 14px;
    }
  }

  // 内容输入框
  .content-input-wrapper {
    position: relative;
    width: 100%;

    .custom-textarea {
      :deep(.el-textarea__inner) {
        border-radius: 12px;
        border: 2px solid rgba(0, 0, 0, 0.08);
        background: #fafafa;
        transition: all 0.3s;
        padding: 12px 16px;
        font-size: 14px;
        line-height: 1.6;

        &:focus {
          border-color: var(--dt-brand-color);
          background: #fff;
          box-shadow: 0 4px 12px rgba(22, 119, 255, 0.1);
        }
      }

      :deep(.el-input__count) {
        background: rgba(0, 0, 0, 0.03);
        padding: 2px 8px;
        border-radius: 6px;
        margin: 8px;
        color: #999;
        font-size: 12px;
      }
    }

    .input-decoration {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 2px;
      background: linear-gradient(90deg, var(--dt-brand-color) 0%, transparent 100%);
      border-radius: 2px;
      opacity: 0;
      transition: opacity 0.3s;
    }

    &:focus-within .input-decoration {
      opacity: 1;
    }
  }

  // 类型选择器
  .type-selector {
    display: flex;
    gap: 12px;
    width: 100%;
  }

  .type-option {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 14px 16px;
    background: #fafafa;
    border: 2px solid rgba(0, 0, 0, 0.06);
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      inset: 0;
      background: linear-gradient(135deg, rgba(255, 255, 255, 0) 0%, rgba(0, 0, 0, 0.02) 100%);
      transition: opacity 0.3s;
      opacity: 0;
    }

    &:hover:not(.disabled) {
      background: #fff;
      border-color: rgba(22, 119, 255, 0.3);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);

      &::before {
        opacity: 1;
      }
    }

    &.active {
      background: linear-gradient(135deg, #e8f4ff 0%, #f0f7ff 100%);
      border-color: var(--dt-brand-color);
      box-shadow: 0 4px 12px rgba(22, 119, 255, 0.2);
    }

    &.disabled {
      opacity: 0.5;
      cursor: not-allowed;
      background: #f5f5f5;
    }

    .type-icon {
      font-size: 24px;
      flex-shrink: 0;
    }

    .type-info {
      display: flex;
      flex-direction: column;
      gap: 2px;
      flex: 1;
    }

    .type-label {
      font-size: 14px;
      font-weight: 600;
      color: #1a1a1a;
    }

    .type-disabled-tip {
      font-size: 11px;
      color: #999;
    }

    .type-glow {
      position: absolute;
      inset: -4px;
      background: var(--dt-brand-color);
      opacity: 0.1;
      border-radius: 16px;
      animation: typeGlow 2s ease-in-out infinite;
    }

    @keyframes typeGlow {
      0%, 100% {
        opacity: 0.1;
      }
      50% {
        opacity: 0.2;
      }
    }
  }

  // 优先级选择器
  .priority-selector {
    display: flex;
    gap: 12px;
    width: 100%;
  }

  .priority-option {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 20px;
    background: #fafafa;
    border: 2px solid rgba(0, 0, 0, 0.06);
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;

    &:hover {
      background: #fff;
      border-color: rgba(22, 119, 255, 0.3);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }

    &.active {
      border-color: var(--dt-brand-color);
      box-shadow: 0 4px 12px rgba(22, 119, 255, 0.2);

      &:nth-child(2) {
        background: linear-gradient(135deg, #fff1f0 0%, #fff5f3 100%);
        border-color: #f54a45;
      }
    }

    .priority-icon {
      font-size: 20px;
      flex-shrink: 0;
    }

    .priority-label {
      font-size: 14px;
      font-weight: 600;
      color: #1a1a1a;
    }

    .priority-glow {
      position: absolute;
      inset: -4px;
      background: currentColor;
      opacity: 0.1;
      border-radius: 16px;
      animation: priorityGlow 2s ease-in-out infinite;
    }

    @keyframes priorityGlow {
      0%, 100% {
        opacity: 0.1;
      }
      50% {
        opacity: 0.2;
      }
    }
  }

  // 提醒对象
  .target-users-wrapper {
    width: 100%;
    background: #fafafa;
    border: 2px solid rgba(0, 0, 0, 0.06);
    border-radius: 12px;
    padding: 16px;
    transition: all 0.3s;

    &:hover {
      border-color: rgba(22, 119, 255, 0.2);
    }

    .select-all-bar {
      margin-bottom: 12px;

      .custom-checkbox {
        :deep(.el-checkbox__label) {
          font-size: 14px;
          font-weight: 500;
          color: #1a1a1a;
        }

        :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
          background-color: var(--dt-brand-color);
          border-color: var(--dt-brand-color);
        }
      }

      .checkbox-label {
        display: flex;
        align-items: center;
        gap: 6px;

        .checkbox-icon {
          font-size: 16px;
        }
      }
    }

    .custom-divider {
      margin: 12px 0;
      border-color: rgba(0, 0, 0, 0.08);
    }

    .members-list {
      max-height: 220px;
      overflow-y: auto;

      &::-webkit-scrollbar {
        width: 6px;
      }

      &::-webkit-scrollbar-track {
        background: rgba(0, 0, 0, 0.04);
        border-radius: 3px;
      }

      &::-webkit-scrollbar-thumb {
        background: rgba(0, 0, 0, 0.15);
        border-radius: 3px;

        &:hover {
          background: rgba(0, 0, 0, 0.25);
        }
      }

      .el-checkbox-group {
        display: flex;
        flex-direction: column;
        gap: 10px;
      }

      .member-card {
        transition: all 0.3s;

        &:hover {
          transform: translateX(4px);
        }

        .el-checkbox {
          margin-right: 0;
          width: 100%;

          :deep(.el-checkbox__label) {
            width: 100%;
          }

          :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
            background-color: var(--dt-brand-color);
            border-color: var(--dt-brand-color);
          }
        }

        .member-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 6px 0;

          .member-avatar-wrapper {
            position: relative;
            flex-shrink: 0;

            .avatar-glow {
              position: absolute;
              inset: -4px;
              border-radius: 50%;
              background: linear-gradient(135deg, #1677ff 0%, #0e5fd9 100%);
              opacity: 0.3;
              animation: avatarGlow 3s ease-in-out infinite;
            }

            @keyframes avatarGlow {
              0%, 100% {
                opacity: 0.3;
                transform: scale(1);
              }
              50% {
                opacity: 0.5;
                transform: scale(1.1);
              }
            }
          }

          .member-name {
            font-size: 14px;
            font-weight: 500;
            color: #1a1a1a;
          }
        }
      }
    }

    .empty-tip {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      color: #999;
      padding: 30px 0;
      font-size: 14px;

      .empty-icon {
        font-size: 24px;
      }
    }
  }

  // 过期时间选择器
  .expire-selector {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
    width: 100%;
  }

  .expire-option {
    flex: 1;
    min-width: 80px;
    padding: 10px 16px;
    background: #fafafa;
    border: 2px solid rgba(0, 0, 0, 0.06);
    border-radius: 10px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;

    &:hover {
      background: #fff;
      border-color: rgba(22, 119, 255, 0.3);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }

    &.active {
      background: linear-gradient(135deg, #e8f4ff 0%, #f0f7ff 100%);
      border-color: var(--dt-brand-color);
      box-shadow: 0 4px 12px rgba(22, 119, 255, 0.2);
    }

    .expire-label {
      font-size: 13px;
      font-weight: 600;
      color: #1a1a1a;
      position: relative;
      z-index: 1;
    }

    .expire-decoration {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 3px;
      background: linear-gradient(90deg, var(--dt-brand-color) 0%, transparent 100%);
      border-radius: 2px;
      opacity: 0;
      transition: opacity 0.3s;
    }

    &.active .expire-decoration {
      opacity: 1;
    }
  }

  // 回执开关
  .receipt-toggle {
    position: relative;

    .custom-switch {
      :deep(.el-switch__label) {
        font-size: 13px;
        font-weight: 500;
        color: #666;
      }

      :deep(.el-switch.is-checked .el-switch__core) {
        background-color: var(--dt-brand-color);
      }
    }

    .switch-glow {
      position: absolute;
      inset: -8px;
      background: var(--dt-brand-color);
      opacity: 0.1;
      border-radius: 20px;
      animation: switchGlow 3s ease-in-out infinite;
    }

    @keyframes switchGlow {
      0%, 100% {
        opacity: 0.1;
      }
      50% {
        opacity: 0.2;
      }
    }
  }

  // 底部按钮
  .dialog-footer {
    display: flex;
    gap: 12px;
    justify-content: flex-end;
  }

  .footer-btn {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 10px 24px;
    border-radius: 24px;
    border: none;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;

    .btn-icon {
      font-size: 16px;
    }

    &::before {
      content: '';
      position: absolute;
      inset: 0;
      background: linear-gradient(45deg, transparent 30%, rgba(255, 255, 255, 0.2) 50%, transparent 50%);
      opacity: 0;
      transition: opacity 0.3s;
    }

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);

      &::before {
        opacity: 1;
      }
    }

    &:active {
      transform: translateY(0);
    }

    &--cancel {
      background: rgba(0, 0, 0, 0.04);
      color: #666;

      &:hover {
        background: rgba(0, 0, 0, 0.08);
      }
    }

    &--submit {
      background: linear-gradient(135deg, #f54a45 0%, #d32f2f 100%);
      color: #fff;
      box-shadow: 0 4px 12px rgba(245, 74, 69, 0.3);

      &:hover {
        background: linear-gradient(135deg, #ff7875 0%, #f54a45 100%);
        box-shadow: 0 6px 16px rgba(245, 74, 69, 0.4);
      }
    }
  }
}

// ============================================================================
// 对话框样式覆盖
// ============================================================================
:deep(.ding-modal) {
  .el-dialog__header {
    padding: 24px 24px 16px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.06);

    .el-dialog__title {
      font-size: 18px;
      font-weight: 700;
      color: #1a1a1a;
      letter-spacing: -0.5px;
    }

    .el-dialog__close {
      font-size: 20px;
      color: #999;
      transition: all 0.3s;

      &:hover {
        color: #666;
        transform: rotate(90deg);
      }
    }
  }

  .el-dialog__body {
    padding: 20px 24px;
    background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  }

  .el-dialog__footer {
    padding: 16px 24px 24px;
    border-top: 1px solid rgba(0, 0, 0, 0.06);
    background: #fff;
  }
}

// ============================================================================
// 暗色模式
// ============================================================================
.dark :deep(.ding-modal) {
  .el-dialog__header {
    border-color: rgba(255, 255, 255, 0.1);
    background: #1a1f2e;
  }

  .el-dialog__title {
    color: #e8e8e8;
  }

  .el-dialog__body {
    background: linear-gradient(135deg, #1a1f2e 0%, #141925 100%);
  }

  .el-dialog__footer {
    border-color: rgba(255, 255, 255, 0.1);
    background: #1a1f2e;
  }
}

.dark .ding-form {
  :deep(.el-form-item__label) {
    color: #e8e8e8;
  }
}

.dark .content-input-wrapper {
  .custom-textarea {
    :deep(.el-textarea__inner) {
      background: #0d1d2d;
      border-color: rgba(255, 255, 255, 0.1);
      color: #e8e8e8;

      &:focus {
        background: #1a1f2e;
        border-color: var(--dt-brand-color);
      }
    }

    :deep(.el-input__count) {
      background: rgba(255, 255, 255, 0.06);
      color: #888;
    }
  }
}

.dark .type-option,
.dark .priority-option,
.dark .expire-option {
  background: #0d1d2d;
  border-color: rgba(255, 255, 255, 0.1);

  &:hover {
    background: #1a1f2e;
    border-color: rgba(22, 119, 255, 0.3);
  }

  &.active {
    background: linear-gradient(135deg, #0d1d2d 0%, #0a1929 100%);
  }

  .type-label,
  .priority-label,
  .expire-label {
    color: #e8e8e8;
  }
}

.dark .target-users-wrapper {
  background: #0d1d2d;
  border-color: rgba(255, 255, 255, 0.1);

  .custom-checkbox {
    :deep(.el-checkbox__label) {
      color: #e8e8e8;
    }
  }

  .member-name {
    color: #e8e8e8;
  }
}

.dark .footer-btn--cancel {
  background: rgba(255, 255, 255, 0.06);
  color: #999;

  &:hover {
    background: rgba(255, 255, 255, 0.1);
  }
}
</style>