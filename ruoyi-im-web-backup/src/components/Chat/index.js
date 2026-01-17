/**
 * @file Chat组件模块导出
 * @description 聊天相关组件统一导出
 */
import MessageBubble from './MessageBubble.vue'
import EmojiPicker from './EmojiPicker.vue'
import ImagePreview from './ImagePreview.vue'
import SessionList from './SessionList.vue'
import MessageList from './MessageList.vue'
import ChatInput from './ChatInput.vue'
import MentionSelector from './MentionSelector.vue'
import LocationPicker from './LocationPicker.vue'
import VoteDialog from './VoteDialog.vue'
import CodeSnippetDialog from './CodeSnippetDialog.vue'
import VirtualMessageList from './VirtualMessageList.vue'

const components = [
  MessageBubble,
  EmojiPicker,
  ImagePreview,
  SessionList,
  MessageList,
  ChatInput,
  MentionSelector,
  LocationPicker,
  VoteDialog,
  CodeSnippetDialog,
  VirtualMessageList,
]

const install = function (Vue) {
  components.forEach(component => {
    Vue.component(component.name, component)
  })
}

export {
  install,
  MessageBubble,
  EmojiPicker,
  ImagePreview,
  SessionList,
  MessageList,
  ChatInput,
  MentionSelector,
  LocationPicker,
  VoteDialog,
  CodeSnippetDialog,
  VirtualMessageList,
}

export default {
  install,
}
