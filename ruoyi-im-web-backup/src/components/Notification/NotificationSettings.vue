<template>
  <el-dialog
    v-model="dialogVisible"
    title="通知设置"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form :model="settingsForm" label-width="120px">
      <el-divider content-position="left">通知类型开关</el-divider>

      <el-form-item label="系统通知">
        <el-switch v-model="settingsForm.systemEnabled" />
        <span class="setting-desc">接收系统相关通知</span>
      </el-form-item>

      <el-form-item label="审批通知">
        <el-switch v-model="settingsForm.approvalEnabled" />
        <span class="setting-desc">接收审批流程相关通知</span>
      </el-form-item>

      <el-form-item label="消息通知">
        <el-switch v-model="settingsForm.messageEnabled" />
        <span class="setting-desc">接收聊天消息通知</span>
      </el-form-item>

      <el-form-item label="DING通知">
        <el-switch v-model="settingsForm.dingEnabled" />
        <span class="setting-desc">接收DING重要通知</span>
      </el-form-item>

      <el-divider content-position="left">推送渠道配置</el-divider>

      <el-form-item label="桌面通知">
        <el-switch v-model="settingsForm.desktopEnabled" />
        <span class="setting-desc">在桌面显示通知弹窗</span>
      </el-form-item>

      <el-form-item label="声音提醒">
        <el-switch v-model="settingsForm.soundEnabled" />
        <span class="setting-desc">接收通知时播放提示音</span>
      </el-form-item>

      <el-form-item label="免打扰时段">
        <el-time-picker
          v-model="settingsForm.doNotDisturbStart"
          placeholder="开始时间"
          format="HH:mm"
          value-format="HH:mm"
          style="width: 140px"
        />
        <span style="margin: 0 8px">-</span>
        <el-time-picker
          v-model="settingsForm.doNotDisturbEnd"
          placeholder="结束时间"
          format="HH:mm"
          value-format="HH:mm"
          style="width: 140px"
        />
        <span class="setting-desc">在此时间段内不接收通知</span>
      </el-form-item>

      <el-form-item label="免打扰星期">
        <el-checkbox-group v-model="settingsForm.doNotDisturbDays">
          <el-checkbox :label="0">周日</el-checkbox>
          <el-checkbox :label="1">周一</el-checkbox>
          <el-checkbox :label="2">周二</el-checkbox>
          <el-checkbox :label="3">周三</el-checkbox>
          <el-checkbox :label="4">周四</el-checkbox>
          <el-checkbox :label="5">周五</el-checkbox>
          <el-checkbox :label="6">周六</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getNotificationSettings, updateNotificationSettings } from '@/api/im/notification'

const props = defineProps({
  visible: Boolean,
})

const emit = defineEmits(['update:visible', 'saved'])

const dialogVisible = ref(false)
const saving = ref(false)

const settingsForm = ref({
  systemEnabled: true,
  approvalEnabled: true,
  messageEnabled: true,
  dingEnabled: true,
  desktopEnabled: true,
  soundEnabled: true,
  doNotDisturbStart: '22:00',
  doNotDisturbEnd: '08:00',
  doNotDisturbDays: [],
})

const loadSettings = async () => {
  try {
    const response = await getNotificationSettings()
    if (response.code === 200 && response.data) {
      Object.assign(settingsForm.value, response.data)
    }
  } catch (error) {
    console.error('加载通知设置失败:', error)
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    const response = await updateNotificationSettings(settingsForm.value)
    if (response.code === 200) {
      ElMessage.success('保存成功')
      emit('saved')
      handleClose()
    } else {
      ElMessage.error(response.msg || '保存失败')
    }
  } catch (error) {
    console.error('保存通知设置失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleClose = () => {
  dialogVisible.value = false
  emit('update:visible', false)
}

watch(
  () => props.visible,
  visible => {
    dialogVisible.value = visible
    if (visible) {
      loadSettings()
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
