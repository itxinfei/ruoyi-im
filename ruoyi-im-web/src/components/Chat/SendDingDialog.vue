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
                <div class="type-icon">
                  <svg v-if="type.value === 'APP'" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M17 1.01L7 1C5.9 1 5 1.9 5 3V21C5 22.1 5.9 23 7 23H17C18.1 23 19 22.1 19 21V3C19 1.9 18.1 1.01 17 1.01ZM17 19H7V5H17V19Z" fill="currentColor"/>
                  </svg>
                  <svg v-else-if="type.value === 'SMS'" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M20 2H4C2.9 2 2 2.9 2 4V22L6 18H20C21.1 18 22 17.1 22 16V4C22 2.9 21.1 2 20 2ZM18 14H6V12H18V14ZM18 11H6V9H18V11ZM18 8H6V6H18V8Z" fill="currentColor"/>
                  </svg>
                  <svg v-else viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M6.62 10.79C15.02 13.79 19.4 7.75 20.26 5.89C20.46 6.19 20.65 6.5 20.82 6.82C19.7 10.45 15.29 14.74 9.2 15.52C7.25 15.77 5.31 15.57 3.56 14.95C4.5 13.54 5.55 12.13 6.62 10.79ZM19.63 5.31C19.35 4.95 19.05 4.6 18.74 4.26C18.5 4.61 18.26 4.94 18.02 5.27C16.85 6.82 15.47 8.16 13.93 9.26C10.92 11.25 7.32 12.22 3.8 11.79C3.36 11.74 2.92 11.67 2.49 11.57C3.78 9.86 5.21 8.28 6.76 6.89C8.37 5.45 10.15 4.26 12.04 3.39C14.47 2.28 16.99 2.27 19.4 3.37C19.5 4.01 19.58 4.66 19.63 5.31ZM12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM12 20C7.58 20 4 16.42 4 12C4 7.58 7.58 4 12 4C16.42 4 20 7.58 20 12C20 16.42 16.42 20 12 20Z" fill="currentColor"/>
                  </svg>
                </div>
                <div class="type-info">
                  <span class="type-label">{{ type.label }}</span>
                  <span v-if="type.disabled" class="type-disabled-tip">需要第三方服务</span>
                </div>
                <div v-if="form.dingType === type.value" class="type-glow"></div>
              </div>
            </div>
          </el-form-item>

          <!-- 优先级 -->
          <el-form-item label="优先级" prop="isUrgent">
            <div class="priority-selector">
              <div
                v-for="level in urgencyLevels"
                :key="level.value"
                class="priority-option"
                :class="{ active: form.isUrgent === level.value }"
                @click="form.isUrgent = level.value"
              >
                <div class="priority-icon">
                  <svg v-if="level.value === 0" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 8V12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    <path d="M12 16H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  </svg>
                  <svg v-else viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M13 2L3 14H12L11 22L21 10H12L13 2Z" fill="currentColor"/>
                  </svg>
                </div>
                <span class="priority-label">{{ level.label }}</span>
                <div v-if="form.isUrgent === level.value" class="priority-glow"></div>
              </div>
            </div>
          </el-form-item>

          <!-- 提醒对象 -->
          <el-form-item label="提醒对象" prop="targetUsers">
            <div class="target-users-wrapper">
              <div class="select-all-bar">
                <el-checkbox v-model="selectAll" @change="handleSelectAll" class="custom-checkbox">
                  <span class="checkbox-label">
                    <svg class="checkbox-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M16 11C17.66 11 18.99 9.66 18.99 8C18.99 6.34 17.66 5 16 5C14.34 5 13 6.34 13 8C13 9.66 14.34 11 16 11ZM8 11C9.66 11 10.99 9.66 10.99 8C10.99 6.34 9.66 5 8 5C6.34 5 5 6.34 5 8C5 9.66 6.34 11 8 11ZM8 13C5.67 13 1 14.17 1 16.5V19H15V16.5C15 14.17 10.33 13 8 13ZM16 13C15.71 13 15.38 13.02 15.03 13.05C16.19 13.89 17 15.02 17 16.5V19H23V16.5C23 14.17 18.33 13 16 13Z" fill="currentColor"/>
                    </svg>
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
                <svg class="empty-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M20 4H4C2.9 4 2 4.9 2 6V18C2 19.1 2.9 20 4 20H20C21.1 20 22 19.1 22 18V6C22 4.9 21.1 4 20 4ZM20 18H4V8L12 13L20 8V18ZM12 11L4 6H20L12 11Z" fill="currentColor"/>
                </svg>
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
            <svg class="btn-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M19 6.41L17.59 5L12 10.59L6.41 5L5 6.41L10.59 12L5 17.59L6.41 19L12 13.41L17.59 19L19 17.59L13.41 12L19 6.41Z" fill="currentColor"/>
            </svg>
            <span>取消</span>
          </el-button>
          <el-button type="danger" :loading="loading" @click="handleSubmit" class="footer-btn footer-btn--submit">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M2.01 21L23 12L2.01 3L2 10L17 12L2 14L2.01 21Z" fill="currentColor"/>
            </svg>
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
    disabled: false
  },
  {
    value: 'SMS',
    label: '短信提醒',
    disabled: true
  },
  {
    value: 'CALL',
    label: '电话提醒',
    disabled: true
  }
]

// 优先级选项
const urgencyLevels = [
  {
    value: 0,
    label: '普通'
  },
  {
    value: 1,
    label: '紧急'
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
  isUrgent: 0,
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
      isUrgent: form.isUrgent,
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
  form.isUrgent = 0
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
        border-radius: var(--dt-radius-lg);
        border: 2px solid rgba(0, 0, 0, 0.08);
        background: #fafafa;
        transition: all 0.3s;
        padding: 12px 16px;
        font-size: 14px;
        line-height: 1.6;

        &:focus {
          border-color: var(--dt-brand-color);
          background: #fff;
          box-shadow: 0 4px 12px rgba(0, 137, 255, 0.1);
        }
      }

      :deep(.el-input__count) {
        background: rgba(0, 0, 0, 0.03);
        padding: 2px 8px;
        border-radius: var(--dt-radius-md);
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
      border-radius: var(--dt-radius-sm);
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
    border-radius: var(--dt-radius-lg);
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
      border-color: rgba(0, 137, 255, 0.3);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);

      &::before {
        opacity: 1;
      }
    }

    &.active {
      background: linear-gradient(135deg, #e8f4ff 0%, #f0f7ff 100%);
      border-color: var(--dt-brand-color);
      box-shadow: 0 4px 12px rgba(0, 137, 255, 0.2);
    }

    &.disabled {
      opacity: 0.5;
      cursor: not-allowed;
      background: #f5f5f5;
    }

    .type-icon {
      width: 24px;
      height: 24px;
      flex-shrink: 0;
      color: #666;
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
      border-radius: var(--dt-radius-xl);
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
    border-radius: var(--dt-radius-lg);
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;

    &:hover {
      background: #fff;
      border-color: rgba(0, 137, 255, 0.3);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }

    &.active {
      border-color: var(--dt-brand-color);
      box-shadow: 0 4px 12px rgba(0, 137, 255, 0.2);

      &:nth-child(2) {
        background: linear-gradient(135deg, #fff1f0 0%, #fff5f3 100%);
        border-color: #f54a45;
      }
    }

    .priority-icon {
      width: 20px;
      height: 20px;
      flex-shrink: 0;
      color: #666;
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
      border-radius: var(--dt-radius-xl);
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
    border-radius: var(--dt-radius-lg);
    padding: 16px;
    transition: all 0.3s;

    &:hover {
      border-color: rgba(0, 137, 255, 0.2);
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
          width: 16px;
          height: 16px;
          color: #999;
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
        border-radius: var(--dt-radius-sm);
      }

      &::-webkit-scrollbar-thumb {
        background: rgba(0, 0, 0, 0.15);
        border-radius: var(--dt-radius-sm);

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
              background: linear-gradient(135deg, #0089FF 0%, #0e5fd9 100%);
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
        width: 24px;
        height: 24px;
        color: #ccc;
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
    border-radius: var(--dt-radius-lg);
    text-align: center;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;

    &:hover {
      background: #fff;
      border-color: rgba(0, 137, 255, 0.3);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }

    &.active {
      background: linear-gradient(135deg, #e8f4ff 0%, #f0f7ff 100%);
      border-color: var(--dt-brand-color);
      box-shadow: 0 4px 12px rgba(0, 137, 255, 0.2);
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
      border-radius: var(--dt-radius-sm);
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
      border-radius: var(--dt-radius-2xl);
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
    border-radius: var(--dt-radius-3xl);
    border: none;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;

    .btn-icon {
      width: 16px;
      height: 16px;
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
    border-color: rgba(0, 137, 255, 0.3);
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