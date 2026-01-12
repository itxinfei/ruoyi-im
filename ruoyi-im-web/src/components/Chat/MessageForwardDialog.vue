<!--
  消息转发对话框组件
  功能：将消息转发给其他联系人或群组
  用途：支持消息转发功能（需求文档4.2.1中提到的消息操作之一）
-->
<template>
  <el-dialog
    v-model="visible"
    title="转发消息"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="forward-dialog">
      <!-- 消息预览 -->
      <div v-if="message" class="message-preview">
        <div class="preview-label">转发内容</div>
        <div class="preview-content">
          <template v-if="message.type === 'text'">
            {{ message.content }}
          </template>
          <template v-else-if="message.type === 'image'">
            <el-image :src="message.content" fit="cover" style="width: 100px; height: 100px" />
          </template>
          <template v-else>
            [{{ message.type }}消息]
          </template>
        </div>
      </div>

      <!-- 选择转发目标 -->
      <div class="forward-target">
        <div class="target-label">转发给</div>
        <el-tabs v-model="activeTab">
          <el-tab-pane label="联系人" name="contact">
            <div class="contact-list">
              <div
                v-for="contact in filteredContacts"
                :key="contact.id"
                class="contact-item"
                :class="{ selected: selectedTargets.includes(contact.id) }"
                @click="toggleSelect(contact.id)"
              >
                <el-avatar :size="36" :src="contact.avatar">{{ contact.name?.charAt(0) }}</el-avatar>
                <span class="contact-name">{{ contact.name }}</span>
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="群组" name="group">
            <div class="group-list">
              <div
                v-for="group in filteredGroups"
                :key="group.id"
                class="group-item"
                :class="{ selected: selectedTargets.includes(group.id) }"
                @click="toggleSelect(group.id)"
              >
                <el-avatar :size="36" :src="group.avatar">{{ group.name?.charAt(0) }}</el-avatar>
                <span class="group-name">{{ group.name }}</span>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :disabled="selectedTargets.length === 0" @click="handleConfirm">
        转发
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useStore } from 'vuex'

const props = defineProps({
  modelValue: Boolean,
  message: Object,
})

const emit = defineEmits(['update:modelValue', 'confirm'])

const store = useStore()
const visible = ref(false)
const activeTab = ref('contact')
const selectedTargets = ref([])

const contacts = computed(() => store.state.im?.contacts || [])
const groups = computed(() => store.state.im?.groups || [])

const filteredContacts = computed(() => contacts.value)
const filteredGroups = computed(() => groups.value)

watch(() => props.modelValue, val => { visible.value = val })
watch(visible, val => { emit('update:modelValue', val) })

function toggleSelect(id) {
  const index = selectedTargets.value.indexOf(id)
  if (index > -1) selectedTargets.value.splice(index, 1)
  else selectedTargets.value.push(id)
}

function handleConfirm() {
  emit('confirm', { message: props.message, targets: selectedTargets.value })
  handleClose()
}

function handleClose() {
  visible.value = false
  selectedTargets.value = []
}
</script>

<style lang="scss" scoped>
.forward-dialog {
  .message-preview {
    margin-bottom: 16px;
    padding: 12px;
    background: #f5f7fa;
    border-radius: 8px;
  }
  .contact-item, .group-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 8px;
    cursor: pointer;
    border-radius: 4px;
    &:hover, &.selected { background: #e6f7ff; }
  }
}
</style>
