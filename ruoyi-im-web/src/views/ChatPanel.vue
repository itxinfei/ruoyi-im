<template>
  <div class="chat-panel">
    <div v-if="!session" class="empty-placeholder">
      <el-empty description="选择一个会话开始聊天" />
    </div>
    <template v-else>
      <ChatHeader :session="session" />
      <MessageList 
        ref="msgListRef"
        :messages="messages" 
        :loading="loading" 
        :current-user="currentUser" 
      />
      <MessageInput 
        :sending="sending" 
        @send="handleSend" 
        @upload-file="triggerFile" 
        @upload-image="triggerImage" 
      />
      
      <!-- Hidden Uploads -->
      <FileUpload ref="fileRef" type="file" @success="handleFileSuccess" />
      <FileUpload ref="imgRef" type="image" @success="handleImgSuccess" />
    </template>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useStore } from 'vuex'
import ChatHeader from '@/components/Chat/ChatHeader.vue'
import MessageList from '@/components/Chat/MessageList.vue'
import MessageInput from '@/components/Chat/MessageInput.vue'
import FileUpload from '@/components/FileUpload/index.vue'
import { getMessages, sendMessage } from '@/api/im/message'
import { useImWebSocket } from '@/composables/useImWebSocket'

const props = defineProps({
  session: Object
})

const store = useStore()
const currentUser = computed(() => store.getters['user/currentUser'])
const messages = ref([])
const loading = ref(false)
const sending = ref(false)
const msgListRef = ref(null)
const fileRef = ref(null)
const imgRef = ref(null)

const { onMessage } = useImWebSocket()

const loadHistory = async () => {
  if (!props.session?.id) return
  loading.value = true
  try {
    const res = await getMessages({ conversationId: props.session.id, pageSize: 50 })
    if (res.code === 200) {
      messages.value = res.data.map(transformMsg).reverse() // Backend often returns new->old
    }
  } finally {
    loading.value = false
    msgListRef.value?.scrollToBottom()
  }
}

const transformMsg = (m) => ({
  ...m,
  isOwn: m.senderId === currentUser.value.id,
  timestamp: m.createTime || m.timestamp
})

const handleSend = async (content) => {
  sending.value = true
  try {
    const res = await sendMessage({
      conversationId: props.session.id,
      messageType: 'TEXT',
      content
    })
    if (res.code === 200) {
      messages.value.push(transformMsg(res.data))
    }
  } finally {
    sending.value = false
    msgListRef.value?.scrollToBottom()
  }
}

// Websocket handling
onMessage((msg) => {
  if (msg.conversationId === props.session?.id) {
    messages.value.push(transformMsg(msg))
    msgListRef.value?.scrollToBottom()
  }
})

watch(() => props.session, () => {
  messages.value = []
  loadHistory()
})

const triggerFile = () => fileRef.value?.triggerUpload()
const triggerImage = () => imgRef.value?.triggerUpload()

// Specific handlers for file/img success would go here calling sendMessage with type FILE/IMAGE
const handleFileSuccess = ({ data }) => {
   // Logic...
   // Reuse sendMessage with type FILE
}
const handleImgSuccess = ({ data }) => {
   // Logic...
}
</script>

<style scoped>
.chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.empty-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}
</style>
