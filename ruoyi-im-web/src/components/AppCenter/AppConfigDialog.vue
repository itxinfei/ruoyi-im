<template>
  <el-dialog
    v-model="visible"
    title="应用设置"
    width="500px"
    :close-on-click-modal="false"
    class="app-config-dialog"
    @close="handleClose"
  >
    <div v-if="app" class="config-content">
      <!-- 应用信息 -->
      <div class="app-info-section">
        <div class="app-icon" :style="{ background: app.iconColor }">
          <img v-if="app.iconUrl" :src="app.iconUrl" :alt="app.name" />
          <span v-else>{{ app.name.charAt(0) }}</span>
        </div>
        <div class="app-details">
          <h3 class="app-name">{{ app.name }}</h3>
          <p class="app-description">{{ app.description }}</p>
        </div>
      </div>

      <!-- 配置表单 -->
      <el-form
        ref="formRef"
        :model="configForm"
        label-width="100px"
        class="config-form"
      >
        <el-form-item
          v-for="item in configItems"
          :key="item.key"
          :label="item.label"
          :required="item.required"
        >
          <!-- 文本输入 -->
          <el-input
            v-if="item.type === 'text'"
            v-model="configForm[item.key]"
            :placeholder="item.placeholder"
            clearable
          />

          <!-- 数字输入 -->
          <el-input-number
            v-else-if="item.type === 'number'"
            v-model="configForm[item.key]"
            :min="item.min"
            :max="item.max"
            :step="item.step || 1"
            style="width: 100%"
          />

          <!-- 开关 -->
          <el-switch
            v-else-if="item.type === 'boolean'"
            v-model="configForm[item.key]"
            :active-text="item.activeText || '启用'"
            :inactive-text="item.inactiveText || '禁用'"
          />

          <!-- 选择器 -->
          <el-select
            v-else-if="item.type === 'select'"
            v-model="configForm[item.key]"
            :placeholder="item.placeholder"
            style="width: 100%"
          >
            <el-option
              v-for="option in item.options"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>

          <!-- 文本域 -->
          <el-input
            v-else-if="item.type === 'textarea'"
            v-model="configForm[item.key]"
            type="textarea"
            :rows="3"
            :placeholder="item.placeholder"
          />

          <template v-if="item.description">
            <div class="item-description">{{ item.description }}</div>
          </template>
        </el-form-item>

        <!-- 额外设置 -->
        <el-form-item label="其他设置">
          <div class="extra-settings">
            <el-checkbox v-model="pinned">
              应用首页置顶
            </el-checkbox>
          </div>
        </el-form-item>
      </el-form>

      <!-- 配置说明 -->
      <div v-if="app.configDescription" class="config-description">
        <h4>配置说明</h4>
        <p>{{ app.configDescription }}</p>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">
          保存设置
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { updateAppConfig, pinApp } from '@/api/im/app'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  app: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(false)
const saving = ref(false)
const pinned = ref(false)
const formRef = ref(null)
const configForm = reactive({})

// 获取配置项
const configItems = computed(() => {
  if (!props.app?.configSchema) {
    return []
  }

  try {
    const schema = typeof props.app.configSchema === 'string'
      ? JSON.parse(props.app.configSchema)
      : props.app.configSchema

    return schema.items || []
  } catch (error) {
    console.error('解析配置架构失败:', error)
    return []
  }
})

// 初始化配置
const initConfig = () => {
  if (!props.app) return

  // 设置置顶状态
  pinned.value = props.app.pinned || false

  // 初始化配置表单
  if (props.app.config) {
    const config = typeof props.app.config === 'string'
      ? JSON.parse(props.app.config)
      : props.app.config

    Object.keys(configForm).forEach(key => delete configForm[key])
    Object.assign(configForm, config)
  }
}

// 保存配置
const handleSave = async () => {
  if (!props.app) return

  try {
    saving.value = true

    // 保存配置
    const res = await updateAppConfig(props.app.id, configForm)

    if (res.code === 200) {
      // 保存置顶状态
      if (pinned.value !== props.app.pinned) {
        await pinApp(props.app.id, pinned.value)
      }

      ElMessage.success('配置已保存')
      emit('success')
      handleClose()
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (error) {
    console.error('保存配置失败:', error)
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

const handleClose = () => {
  visible.value = false
  emit('update:modelValue', false)
}

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    initConfig()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.config-content {
  .app-info-section {
    display: flex;
    gap: 16px;
    padding: 16px;
    background: var(--dt-bg-hover);
    border-radius: var(--dt-radius-lg);
    margin-bottom: 20px;

    .app-icon {
      width: 48px;
      height: 48px;
      border-radius: var(--dt-radius-lg);
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;

      img {
        width: 28px;
        height: 28px;
        object-fit: contain;
      }

      span {
        font-size: 18px;
        font-weight: 700;
        color: #fff;
      }
    }

    .app-details {
      flex: 1;

      .app-name {
        font-size: 16px;
        font-weight: 600;
        color: var(--dt-text-primary);
        margin: 0 0 4px 0;
      }

      .app-description {
        font-size: 13px;
        color: var(--dt-text-secondary);
        margin: 0;
      }
    }
  }
}

.config-form {
  .item-description {
    font-size: 12px;
    color: var(--dt-text-tertiary);
    margin-top: 4px;
  }
}

.extra-settings {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.config-description {
  padding: 12px 16px;
  background: var(--dt-bg-hover);
  border-radius: var(--dt-radius-md);
  margin-top: 16px;

  h4 {
    font-size: 13px;
    font-weight: 600;
    color: var(--dt-text-primary);
    margin: 0 0 8px 0;
  }

  p {
    font-size: 13px;
    color: var(--dt-text-secondary);
    line-height: 1.5;
    margin: 0;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
