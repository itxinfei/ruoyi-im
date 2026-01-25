<template>
  <div class="bot-config-panel">
    <div class="panel-header">
      <div class="header-left">
        <div class="bot-icon">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
            <circle cx="9" cy="10" r="1.5" fill="currentColor"/>
            <circle cx="15" cy="10" r="1.5" fill="currentColor"/>
            <path d="M9 15C9 15 10.5 16 12 16C13.5 16 15 15 15 15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <span class="panel-title">群机器人</span>
        <span class="bot-count">{{ bots.length }}/{{ maxBots }}</span>
      </div>
      <el-button
        type="primary"
        size="small"
        :disabled="bots.length >= maxBots"
        @click="handleCreateBot"
      >
        添加机器人
      </el-button>
    </div>

    <div class="panel-content">
      <div v-if="bots.length === 0" class="empty-state">
        <div class="empty-icon">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2L2 7L12 12L22 7L12 2Z" fill="#e5e7eb"/>
            <circle cx="12" cy="16" r="6" stroke="#e5e7eb" stroke-width="2"/>
          </svg>
        </div>
        <p>暂无机器人，点击"添加机器人"创建</p>
      </div>

      <div v-else class="bot-list">
        <div
          v-for="bot in bots"
          :key="bot.id"
          class="bot-item"
          :class="{ 'bot-disabled': !bot.isEnabled }"
        >
          <div class="bot-info">
            <DingtalkAvatar
              :size="48"
              :src="bot.avatar"
              :name="bot.botName"
            />
            <div class="bot-details">
              <div class="bot-name-row">
                <span class="bot-name">{{ bot.botName }}</span>
                <el-tag
                  :type="bot.botType === 'SERVICE' ? 'success' : bot.botType === 'NOTIFY' ? 'warning' : 'info'"
                  size="small"
                >
                  {{ botTypeLabel(bot.botType) }}
                </el-tag>
                <el-switch
                  :model-value="bot.isEnabled"
                  size="small"
                  @change="handleToggleBot(bot)"
                />
              </div>
              <p v-if="bot.description" class="bot-description">{{ bot.description }}</p>
              <div class="bot-stats">
                <span>{{ bot.ruleCount || 0 }} 条规则</span>
              </div>
            </div>
          </div>
          <div class="bot-actions">
            <el-button-group>
              <el-button size="small" @click="handleEditBot(bot)">
                编辑
              </el-button>
              <el-button size="small" @click="handleManageRules(bot)">
                规则
              </el-button>
              <el-button
                size="small"
                type="danger"
                @click="handleDeleteBot(bot)"
              >
                删除
              </el-button>
            </el-button-group>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建/编辑机器人对话框 -->
    <el-dialog
      v-model="showBotDialog"
      :title="editingBot ? '编辑机器人' : '创建机器人'"
      width="600px"
      @close="handleCloseBotDialog"
    >
      <el-form
        ref="botFormRef"
        :model="botForm"
        :rules="botRules"
        label-width="100px"
      >
        <el-form-item label="机器人名称" prop="botName">
          <el-input
            v-model="botForm.botName"
            placeholder="请输入机器人名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="机器人类型" prop="botType">
          <el-radio-group v-model="botForm.botType">
            <el-radio label="SERVICE">客服机器人</el-radio>
            <el-radio label="NOTIFY">通知机器人</el-radio>
            <el-radio label="MANAGE">管理机器人</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="机器人头像">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
          >
            <img v-if="botForm.avatar" :src="botForm.avatar" class="avatar-preview" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="描述">
          <el-input
            v-model="botForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入机器人描述"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <!-- 规则列表 -->
        <el-divider content-position="left">触发规则</el-divider>

        <div class="rules-list">
          <div
            v-for="(rule, index) in botForm.rules"
            :key="index"
            class="rule-item"
          >
            <div class="rule-header">
              <span class="rule-index">#{{ index + 1 }}</span>
              <el-button
                type="danger"
                size="small"
                text
                @click="removeRule(index)"
              >
                删除
              </el-button>
            </div>
            <el-form-item label="规则名称">
              <el-input
                v-model="rule.ruleName"
                placeholder="请输入规则名称"
                size="small"
              />
            </el-form-item>
            <el-form-item label="触发类型">
              <el-select v-model="rule.triggerType" size="small" style="width: 100%">
                <el-option label="关键词触发" value="KEYWORD" />
                <el-option label="命令触发" value="COMMAND" />
              </el-select>
            </el-form-item>
            <el-form-item label="触发内容">
              <el-input
                v-model="rule.triggerContent"
                placeholder="请输入触发关键词或命令"
                size="small"
              />
            </el-form-item>
            <el-form-item label="匹配模式">
              <el-radio-group v-model="rule.matchMode" size="small">
                <el-radio label="EXACT">精确匹配</el-radio>
                <el-radio label="CONTAINS">包含匹配</el-radio>
                <el-radio label="REGEX">正则表达式</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="回复内容">
              <el-input
                v-model="rule.replyContent"
                type="textarea"
                :rows="2"
                placeholder="请输入回复内容"
                size="small"
              />
            </el-form-item>
            <el-form-item label="优先级">
              <el-input-number
                v-model="rule.priority"
                :min="0"
                :max="100"
                size="small"
                controls-position="right"
              />
            </el-form-item>
          </div>
        </div>

        <el-button @click="addRule" style="width: 100%">
          <el-icon><Plus /></el-icon>
          添加规则
        </el-button>
      </el-form>

      <template #footer>
        <el-button @click="handleCloseBotDialog">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitBot">
          {{ editingBot ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 规则管理对话框 -->
    <el-dialog
      v-model="showRulesDialog"
      title="机器人规则管理"
      width="700px"
    >
      <div class="rules-manage">
        <div class="rules-manage-header">
          <el-button type="primary" size="small" @click="handleAddRule">
            添加规则
          </el-button>
        </div>
        <el-table :data="currentBotRules" style="width: 100%">
          <el-table-column prop="ruleName" label="规则名称" width="120" />
          <el-table-column prop="triggerType" label="触发类型" width="100">
            <template #default="{ row }">
              <el-tag size="small">
                {{ row.triggerType === 'KEYWORD' ? '关键词' : '命令' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="triggerContent" label="触发内容" />
          <el-table-column prop="matchMode" label="匹配模式" width="100">
            <template #default="{ row }">
              <span v-if="row.matchMode === 'EXACT'">精确</span>
              <span v-else-if="row.matchMode === 'CONTAINS'">包含</span>
              <span v-else>正则</span>
            </template>
          </el-table-column>
          <el-table-column prop="replyContent" label="回复内容" show-overflow-tooltip />
          <el-table-column prop="priority" label="优先级" width="80" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                text
                @click="handleEditRule(row)"
              >
                编辑
              </el-button>
              <el-button
                type="danger"
                size="small"
                text
                @click="handleDeleteRule(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getGroupBots,
  createBot,
  updateBot,
  deleteBot,
  setBotEnabled,
  addBotRule,
  updateBotRule,
  deleteBotRule
} from '@/api/im/bot'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const props = defineProps({
  groupId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['refresh'])

const maxBots = 10
const bots = ref([])
const showBotDialog = ref(false)
const showRulesDialog = ref(false)
const editingBot = ref(null)
const submitting = ref(false)
const currentBot = ref(null)
const currentBotRules = ref([])

const botFormRef = ref(null)

const botForm = reactive({
  botName: '',
  botType: 'SERVICE',
  avatar: '',
  description: '',
  rules: []
})

const botRules = {
  botName: [
    { required: true, message: '请输入机器人名称', trigger: 'blur' }
  ]
}

// 加载机器人列表
const loadBots = async () => {
  try {
    const response = await getGroupBots(props.groupId)
    if (response && response.data) {
      bots.value = response.data.map(bot => ({
        ...bot,
        ruleCount: bot.rules?.length || 0
      }))
    }
  } catch (error) {
    console.error('加载机器人列表失败:', error)
  }
}

const botTypeLabel = (type) => {
  const map = {
    'SERVICE': '客服',
    'NOTIFY': '通知',
    'MANAGE': '管理'
  }
  return map[type] || '客服'
}

// 创建机器人
const handleCreateBot = () => {
  editingBot.value = null
  botForm.botName = ''
  botForm.botType = 'SERVICE'
  botForm.avatar = ''
  botForm.description = ''
  botForm.rules = []
  showBotDialog.value = true
}

// 编辑机器人
const handleEditBot = (bot) => {
  editingBot.value = bot
  botForm.botName = bot.botName
  botForm.botType = bot.botType
  botForm.avatar = bot.avatar || ''
  botForm.description = bot.description || ''
  botForm.rules = bot.rules ? [...bot.rules] : []
  showBotDialog.value = true
}

// 提交机器人
const handleSubmitBot = async () => {
  await botFormRef.value.validate()

  submitting.value = true
  try {
    if (editingBot.value) {
      await updateBot({
        botId: editingBot.value.id,
        botName: botForm.botName,
        botType: botForm.botType,
        avatar: botForm.avatar,
        description: botForm.description
      })
      ElMessage.success('机器人更新成功')
    } else {
      const botId = await createBot({
        groupId: props.groupId,
        botName: botForm.botName,
        botType: botForm.botType,
        avatar: botForm.avatar,
        description: botForm.description,
        rules: botForm.rules
      })
      ElMessage.success('机器人创建成功')
    }
    showBotDialog.value = false
    loadBots()
    emit('refresh')
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 关闭机器人对话框
const handleCloseBotDialog = () => {
  showBotDialog.value = false
  editingBot.value = null
  botFormRef.value?.resetFields()
}

// 切换机器人状态
const handleToggleBot = async (bot) => {
  try {
    await setBotEnabled(bot.id, !bot.isEnabled)
    ElMessage.success(bot.isEnabled ? '机器人已禁用' : '机器人已启用')
    loadBots()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 删除机器人
const handleDeleteBot = async (bot) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除机器人"${bot.botName}"吗？`,
      '确认删除',
      { type: 'warning' }
    )
    await deleteBot(bot.id)
    ElMessage.success('机器人已删除')
    loadBots()
    emit('refresh')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 管理规则
const handleManageRules = async (bot) => {
  currentBot.value = bot
  // 加载机器人详情以获取规则
  try {
    const response = await getGroupBots(props.groupId)
    if (response && response.data) {
      const botData = response.data.find(b => b.id === bot.id)
      if (botData) {
        currentBotRules.value = botData.rules || []
      }
    }
  } catch (error) {
    console.error('加载规则失败:', error)
  }
  showRulesDialog.value = true
}

// 添加规则
const addRule = () => {
  botForm.rules.push({
    ruleName: `规则${botForm.rules.length + 1}`,
    triggerType: 'KEYWORD',
    triggerContent: '',
    matchMode: 'CONTAINS',
    replyType: 'TEXT',
    replyContent: '',
    priority: 0
  })
}

// 删除规则
const removeRule = (index) => {
  botForm.rules.splice(index, 1)
}

// 头像上传
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }

  // 这里应该调用上传API，暂时使用本地预览
  const reader = new FileReader()
  reader.onload = (e) => {
    botForm.avatar = e.target.result
  }
  reader.readAsDataURL(file)
  return false
}

onMounted(() => {
  loadBots()
})
</script>

<style lang="scss" scoped>
.bot-config-panel {
  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px;

    .header-left {
      display: flex;
      align-items: center;
      gap: 10px;

      .bot-icon {
        width: 32px;
        height: 32px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 6px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;

        svg {
          width: 18px;
          height: 18px;
        }
      }

      .panel-title {
        font-size: 16px;
        font-weight: 500;
      }

      .bot-count {
        font-size: 12px;
        color: #909399;
        background: #f5f7fa;
        padding: 2px 8px;
        border-radius: 10px;
      }
    }
  }

  .panel-content {
    padding: 0 16px 16px;

    .empty-state {
      text-align: center;
      padding: 40px 20px;

      .empty-icon {
        margin-bottom: 16px;

        svg {
          width: 80px;
          height: 80px;
        }
      }

      p {
        color: #909399;
        font-size: 14px;
      }
    }

    .bot-list {
      display: flex;
      flex-direction: column;
      gap: 12px;

      .bot-item {
        border: 1px solid #e4e7ed;
        border-radius: 8px;
        padding: 16px;
        transition: all 0.3s;

        &:hover {
          box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
        }

        &.bot-disabled {
          opacity: 0.6;
        }

        .bot-info {
          display: flex;
          gap: 12px;
          margin-bottom: 12px;

          .bot-details {
            flex: 1;

            .bot-name-row {
              display: flex;
              align-items: center;
              gap: 8px;
              margin-bottom: 4px;

              .bot-name {
                font-weight: 500;
                font-size: 15px;
              }
            }

            .bot-description {
              font-size: 13px;
              color: #606266;
              margin: 4px 0;
            }

            .bot-stats {
              font-size: 12px;
              color: #909399;
            }
          }
        }

        .bot-actions {
          display: flex;
          justify-content: flex-end;
        }
      }
    }
  }

  .rules-list {
    max-height: 400px;
    overflow-y: auto;

    .rule-item {
      border: 1px solid #e4e7ed;
      border-radius: 6px;
      padding: 12px;
      margin-bottom: 12px;

      .rule-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .rule-index {
          font-weight: 500;
          color: #409eff;
        }
      }
    }
  }

  .avatar-uploader {
    :deep(.el-upload) {
      border: 1px dashed #d9d9d9;
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: all 0.3s;

      &:hover {
        border-color: #409eff;
      }
    }

    .avatar-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 80px;
      height: 80px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .avatar-preview {
      width: 80px;
      height: 80px;
      display: block;
      border-radius: 6px;
    }
  }
}
</style>
