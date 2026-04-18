<template>
  <el-dialog
    v-model="visible"
    :width="720"
    :show-close="false"
    class="premium-forward-dialog"
    destroy-on-close
    append-to-body
  >
    <div class="forward-v4-body">
      <!-- 1. 左侧：消息预览区 (Premium 材质) -->
      <aside class="preview-aside">
        <div class="aside-label">转发内容</div>
        <div class="preview-card custom-scrollbar">
          <div v-if="isMultiple" class="multi-preview">
            <div v-for="m in messages.slice(0, 8)" :key="m.id" class="m-line">
              <span class="m-sender">{{ m.senderName }}:</span>
              <span class="m-text">{{ getMessagePreview(m) }}</span>
            </div>
            <div v-if="messages.length > 8" class="m-more">... 还有 {{ messages.length - 8 }} 条</div>
          </div>
          <div v-else-if="singleMessage" class="single-preview">
             <!-- 具体渲染逻辑同上，但增加边距校准 -->
             <div class="s-sender">{{ singleMessage.senderName }}</div>
             <p class="s-content">{{ singleMessage.content }}</p>
          </div>
        </div>
        
        <div class="mode-switch">
          <el-radio-group v-model="forwardMode" size="small">
            <el-radio-button label="oneByOne">逐条转发</el-radio-button>
            <el-radio-button label="combine">合并转发</el-radio-button>
          </el-radio-group>
        </div>
      </aside>

      <!-- 2. 右侧：多维会话选择 (双栏结构) -->
      <main class="select-main">
        <header class="select-header">
          <div class="search-bar-v4">
            <el-icon><Search /></el-icon>
            <input v-model="searchKeyword" placeholder="搜索联系人或群组" />
          </div>
        </header>

        <div class="dual-column">
          <!-- 选择源 -->
          <div class="column-left custom-scrollbar">
            <div class="col-title">最近会话</div>
            <div 
              v-for="s in filteredSessions" 
              :key="s.id" 
              class="sel-item"
              :class="{ selected: selectedSessionIds.includes(s.id) }"
              @click="toggleSession(s)"
            >
              <DingtalkAvatar :src="s.avatar" :name="s.name" :size="32" shape="square" />
              <span class="name">{{ s.name }}</span>
              <div class="check-box"><el-icon v-if="selectedSessionIds.includes(s.id)"><Check /></el-icon></div>
            </div>
          </div>

          <!-- 已选预览 -->
          <div class="column-right custom-scrollbar">
            <div class="col-title">已选择 ({{ selectedSessionIds.length }})</div>
            <div v-for="id in selectedSessionIds" :key="id" class="selected-pill">
              <span class="p-name">{{ getSessionName(id) }}</span>
              <el-icon class="p-close" @click="removeSession(id)"><Close /></el-icon>
            </div>
          </div>
        </div>
      </main>
    </div>

    <template #footer>
      <div class="v4-dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :disabled="!selectedSessionIds.length" @click="handleForward">
          完成转发
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="js">
import { ref, computed } from 'vue'
import { Search, Check, Close } from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import DingtalkAvatar from '@/components/Common/DingtalkAvatar.vue'

const store = useStore()
const visible = ref(false)
const messages = ref([])
const searchKeyword = ref('')
const selectedSessionIds = ref([])
const forwardMode = ref('oneByOne')

const isMultiple = computed(() => messages.value.length > 1)
const singleMessage = computed(() => messages.value[0])
const sessions = computed(() => store.state.im.session.sessions)
const filteredSessions = computed(() => sessions.value.filter(s => s.name.includes(searchKeyword.value)))

const getSessionName = (id) => sessions.value.find(s => s.id === id)?.name || ''
const toggleSession = (s) => {
  const idx = selectedSessionIds.value.indexOf(s.id)
  if (idx === -1) selectedSessionIds.value.push(s.id)
  else selectedSessionIds.value.splice(idx, 1)
}
const removeSession = (id) => selectedSessionIds.value = selectedSessionIds.value.filter(i => i !== id)

const open = (msgs) => { messages.value = Array.isArray(msgs) ? msgs : [msgs]; visible.value = true }
defineExpose({ open })
</script>

<style scoped lang="scss">
.premium-forward-dialog {
  :deep(.el-dialog) { border-radius: 12px; overflow: hidden; padding: 0; box-shadow: var(--dt-shadow-3); }
  :deep(.el-dialog__body) { padding: 0; }
  :deep(.el-dialog__header) { display: none; }
}

.forward-v4-body {
  display: flex; height: 480px;
}

// 1. 左侧预览
.preview-aside {
  width: 240px; background: #f8fbff; border-right: 1px solid rgba(0,0,0,0.05);
  display: flex; flex-direction: column; padding: 20px;
  .aside-label { font-size: 12px; color: #999; margin-bottom: 12px; }
  .preview-card {
    flex: 1; background: #fff; border: 1px solid rgba(0,0,0,0.06); border-radius: 8px;
    padding: 12px; font-size: 13px; line-height: 1.6;
    .m-sender { color: var(--dt-brand-color); font-weight: 600; margin-right: 4px; }
    .m-text { color: #555; }
    .m-line { margin-bottom: 8px; @include text-ellipsis; }
  }
  .mode-switch { margin-top: 16px; width: 100%; :deep(.el-radio-group) { width: 100%; display: flex; .el-radio-button { flex: 1; } } }
}

// 2. 右侧选择
.select-main {
  flex: 1; display: flex; flex-direction: column;
  .select-header { padding: 16px 20px; border-bottom: 1px solid rgba(0,0,0,0.05); }
  .search-bar-v4 {
    height: 32px; background: #f2f2f2; border-radius: 16px; display: flex; align-items: center; padding: 0 12px; gap: 8px;
    input { border: none; background: transparent; outline: none; flex: 1; font-size: 13px; }
    .el-icon { color: #888; }
  }
}

.dual-column {
  flex: 1; display: flex; overflow: hidden;
  .column-left, .column-right { flex: 1; padding: 12px; overflow-y: auto; }
  .column-right { background: #fdfdfe; border-left: 1px solid rgba(0,0,0,0.03); }
  .col-title { font-size: 11px; font-weight: 700; color: #aaa; margin-bottom: 12px; padding-left: 8px; }
}

.sel-item {
  height: 44px; display: flex; align-items: center; gap: 10px; padding: 0 10px;
  border-radius: 6px; cursor: pointer; transition: 0.1s;
  &:hover { background: #f5f5f5; }
  &.selected { .check-box { background: var(--dt-brand-color); border-color: var(--dt-brand-color); color: #fff; } }
  .name { font-size: 14px; flex: 1; @include text-ellipsis; }
  .check-box { width: 18px; height: 18px; border: 1.5px solid #dcdfe6; border-radius: 50%; @include flex-center; font-size: 11px; color: transparent; }
}

.selected-pill {
  height: 32px; background: #eff4fc; border-radius: 16px; padding: 0 12px;
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px;
  .p-name { font-size: 12px; color: var(--dt-brand-color); font-weight: 500; }
  .p-close { font-size: 14px; color: #888; cursor: pointer; &:hover { color: #ff4d4f; } }
}

.v4-dialog-footer { padding: 12px 20px; border-top: 1px solid rgba(0,0,0,0.05); display: flex; justify-content: flex-end; gap: 12px; }
</style>
