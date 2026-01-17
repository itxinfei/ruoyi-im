<template>
  <el-dialog
    v-model="visible"
    title="群聊设置"
    width="600px"
    @close="handleClose"
  >
    <div class="group-config">
      <el-tabs v-model="activeTab">
        <!-- 基本设置 -->
        <el-tab-pane label="基本设置" name="basic">
          <el-form :model="config" label-width="120px">
            <el-form-item label="群聊名称">
              <el-input
                v-model="config.name"
                placeholder="请输入群聊名称"
                maxlength="50"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item label="群聊描述">
              <el-input
                v-model="config.description"
                type="textarea"
                placeholder="请输入群聊描述"
                :rows="3"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item label="加群方式">
              <el-radio-group v-model="config.joinType">
                <el-radio label="free">自由加入</el-radio>
                <el-radio label="needApproval">需要审核</el-radio>
                <el-radio label="inviteOnly">仅限邀请</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="成员上限">
              <el-input-number
                v-model="config.maxMembers"
                :min="3"
                :max="500"
                controls-position="right"
              />
              <span class="form-tip">群聊成员数量上限</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- 权限设置 -->
        <el-tab-pane label="权限设置" name="permission">
          <el-form :model="config.permissions" label-width="120px">
            <el-form-item label="发言权限">
              <el-radio-group v-model="config.permissions.speak">
                <el-radio label="all">所有人可发言</el-radio>
                <el-radio label="admin">仅管理员可发言</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="上传文件">
              <el-radio-group v-model="config.permissions.uploadFile">
                <el-radio label="all">所有人可上传</el-radio>
                <el-radio label="admin">仅管理员可上传</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="邀请成员">
              <el-radio-group v-model="config.permissions.invite">
                <el-radio label="all">所有人可邀请</el-radio>
                <el-radio label="admin">仅管理员可邀请</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="@所有人">
              <el-radio-group v-model="config.permissions.atAll">
                <el-radio label="all">所有人可@所有人</el-radio>
                <el-radio label="admin">仅管理员可@所有人</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- 消息设置 -->
        <el-tab-pane label="消息设置" name="message">
          <el-form :model="config.message" label-width="120px">
            <el-form-item label="消息撤回">
              <el-switch v-model="config.message.allowRecall" />
              <span class="form-tip">允许成员撤回自己发送的消息</span>
            </el-form-item>
            
            <el-form-item label="撤回时限">
              <el-input-number
                v-model="config.message.recallTimeout"
                :min="1"
                :max="10"
                controls-position="right"
                :disabled="!config.message.allowRecall"
              />
              <span class="form-tip">消息撤回时限（分钟）</span>
            </el-form-item>
            
            <el-form-item label="消息保存">
              <el-radio-group v-model="config.message.saveType">
                <el-radio label="permanent">永久保存</el-radio>
                <el-radio label="days">按天数保存</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item v-if="config.message.saveType === 'days'" label="保存天数">
              <el-input-number
                v-model="config.message.saveDays"
                :min="7"
                :max="365"
                controls-position="right"
              />
              <span class="form-tip">超过天数的消息将被自动删除</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
    
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button 
        type="primary" 
        :loading="saving"
        @click="handleSave"
      >
        保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  groupInfo: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const store = useStore()
const activeTab = ref('basic')
const saving = ref(false)

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const config = reactive({
  name: '',
  description: '',
  joinType: 'needApproval',
  maxMembers: 200,
  permissions: {
    speak: 'all',
    uploadFile: 'all',
    invite: 'admin',
    atAll: 'admin'
  },
  message: {
    allowRecall: true,
    recallTimeout: 2,
    saveType: 'permanent',
    saveDays: 30
  }
})

// 监听群聊信息变化，更新配置
watch(() => props.groupInfo, (newInfo) => {
  if (newInfo) {
    Object.assign(config, {
      name: newInfo.name || '',
      description: newInfo.description || '',
      joinType: newInfo.joinType || 'needApproval',
      maxMembers: newInfo.maxMembers || 200,
      permissions: {
        speak: newInfo.permissions?.speak || 'all',
        uploadFile: newInfo.permissions?.uploadFile || 'all',
        invite: newInfo.permissions?.invite || 'admin',
        atAll: newInfo.permissions?.atAll || 'admin'
      },
      message: {
        allowRecall: newInfo.message?.allowRecall !== false,
        recallTimeout: newInfo.message?.recallTimeout || 2,
        saveType: newInfo.message?.saveType || 'permanent',
        saveDays: newInfo.message?.saveDays || 30
      }
    })
  }
}, { immediate: true })

async function handleSave() {
  saving.value = true
  
  try {
    // 调用保存群聊配置的API
    // await store.dispatch('im/updateGroupConfig', {
    //   groupId: props.groupInfo.id,
    //   ...config
    // })
    
    // 临时模拟
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('群聊配置已保存')
    emit('success', config)
    handleClose()
  } catch (error) {
    console.error('保存群聊配置失败:', error)
    ElMessage.error('保存群聊配置失败：' + error.message)
  } finally {
    saving.value = false
  }
}

function handleClose() {
  visible.value = false
}
</script>

<style scoped>
.group-config {
  padding: 8px 0;
}

.form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #8c8c8c;
}

:deep(.el-tabs__content) {
  padding-top: 20px;
}
</style>