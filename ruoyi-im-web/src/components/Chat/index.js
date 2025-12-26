import MessageBubble from './MessageBubble.vue'
import EmojiPicker from './EmojiPicker.vue'
import FileUploader from './FileUploader.vue'
import ImagePreview from './ImagePreview.vue'
import SessionList from './SessionList.vue'
import MessageList from './MessageList.vue'
import ChatInput from './ChatInput.vue'

const components = [MessageBubble, EmojiPicker, FileUploader, ImagePreview, SessionList, MessageList, ChatInput]

const install = function (Vue) {
  components.forEach(component => {
    Vue.component(component.name, component)
  })
}

if (typeof window !== 'undefined' && window.Vue) {
  install(window.Vue)
}

export { install, MessageBubble, EmojiPicker, FileUploader, ImagePreview, SessionList, MessageList, ChatInput }

export default {
  install,
}
