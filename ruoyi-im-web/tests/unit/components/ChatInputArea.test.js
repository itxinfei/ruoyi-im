import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import ChatInputArea from '@/components/im/ChatInputArea.vue'

// Mock Element Plus icons
vi.mock('@element-plus/icons-vue', () => ({
  Plus: { name: 'Plus', template: '<i class="el-icon-plus"></i>' },
  CircleCheck: { name: 'CircleCheck', template: '<i class="el-icon-circle-check"></i>' },
  User: { name: 'User', template: '<i class="el-icon-user"></i>' },
  Link: { name: 'Link', template: '<i class="el-icon-link"></i>' },
  Document: { name: 'Document', template: '<i class="el-icon-document"></i>' },
  Promotion: { name: 'Promotion', template: '<i class="el-icon-promotion"></i>' },
  UploadFilled: { name: 'UploadFilled', template: '<i class="el-icon-upload-filled"></i>' }
}))

describe('ChatInputArea', () => {
  let wrapper

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  describe('Basic Rendering', () => {
    it('should render the editor component', () => {
      wrapper = mount(ChatInputArea, {
        attachTo: document.body
      })
      expect(wrapper.find('.slack-rich-editor').exists()).toBe(true)
      expect(wrapper.find('.slack-send-btn').exists()).toBe(true)
    })

    it('should display placeholder text', () => {
      wrapper = mount(ChatInputArea)
      const editor = wrapper.find('.slack-rich-editor')
      expect(editor.attributes('placeholder')).toBe('给同事发送消息...')
    })
  })

  describe('Send Message', () => {
    it('should not emit send event when editor is empty', async () => {
      wrapper = mount(ChatInputArea)
      await wrapper.find('.slack-send-btn').trigger('click')
      expect(wrapper.emitted('send')).toBeFalsy()
    })

    it('should emit send event with content when button is clicked', async () => {
      wrapper = mount(ChatInputArea)
      const editor = wrapper.find('.slack-rich-editor')
      editor.element.innerText = 'Hello World'
      editor.trigger('input')
      await wrapper.find('.slack-send-btn').trigger('click')
      expect(wrapper.emitted('send')).toBeTruthy()
      expect(wrapper.emitted('send')[0]).toEqual(['Hello World'])
    })

    it('should clear editor after sending message', async () => {
      wrapper = mount(ChatInputArea)
      const editor = wrapper.find('.slack-rich-editor')
      editor.element.innerText = 'Hello World'
      editor.trigger('input')
      await wrapper.find('.slack-send-btn').trigger('click')
      const newEditor = wrapper.find('.slack-rich-editor')
      expect(newEditor.text()).toBe('')
    })

    it('should emit update:modelValue with empty string after sending', async () => {
      wrapper = mount(ChatInputArea)
      const editor = wrapper.find('.slack-rich-editor')
      editor.element.innerText = 'Test message'
      editor.trigger('input')
      await wrapper.find('.slack-send-btn').trigger('click')
      expect(wrapper.emitted('update:modelValue')).toBeTruthy()
      const lastEmit = wrapper.emitted('update:modelValue').pop()
      expect(lastEmit).toEqual([''])
    })
  })

  describe('Content Detection', () => {
    it('should detect content presence', async () => {
      wrapper = mount(ChatInputArea)
      expect(wrapper.vm.hasContent).toBe(false)

      const editor = wrapper.find('.slack-rich-editor')
      editor.element.innerText = 'Some text'
      editor.trigger('input')
      expect(wrapper.vm.hasContent).toBe(true)
    })

    it('should enable send button when content exists', async () => {
      wrapper = mount(ChatInputArea)
      const editor = wrapper.find('.slack-rich-editor')
      editor.element.innerText = 'Hello'
      editor.trigger('input')

      // canSend computed should be true when hasContent is true
      expect(wrapper.vm.canSend).toBe(true)
    })
  })

  describe('Toolbar Actions', () => {
    it('should emit attach-click when attach button is clicked', async () => {
      wrapper = mount(ChatInputArea)
      await wrapper.find('button[title="添加附件"]').trigger('click')
      expect(wrapper.emitted('attach-click')).toBeTruthy()
    })

    it('should emit emoji-click when emoji button is clicked', async () => {
      wrapper = mount(ChatInputArea)
      await wrapper.find('button[title="表情"]').trigger('click')
      expect(wrapper.emitted('emoji-click')).toBeTruthy()
    })

    it('should emit mention-click when mention button is clicked', async () => {
      wrapper = mount(ChatInputArea)
      await wrapper.find('button[title="提及"]').trigger('click')
      expect(wrapper.emitted('mention-click')).toBeTruthy()
    })

    it('should emit link-click when link button is clicked', async () => {
      wrapper = mount(ChatInputArea)
      await wrapper.find('button[title="链接"]').trigger('click')
      expect(wrapper.emitted('link-click')).toBeTruthy()
    })
  })

  describe('Drag and Drop', () => {
    it('should set isDragover to true on dragover', async () => {
      wrapper = mount(ChatInputArea)
      expect(wrapper.vm.isDragover).toBe(false)

      await wrapper.trigger('dragover')
      expect(wrapper.vm.isDragover).toBe(true)
    })

    it('should set isDragover to false on dragleave', async () => {
      wrapper = mount(ChatInputArea)
      await wrapper.trigger('dragover')
      expect(wrapper.vm.isDragover).toBe(true)

      await wrapper.trigger('dragleave')
      expect(wrapper.vm.isDragover).toBe(false)
    })

    it('should emit attach event when files are dropped', async () => {
      wrapper = mount(ChatInputArea)
      const file = new File(['test'], 'test.txt', { type: 'text/plain' })
      const dataTransfer = { files: [file] }

      await wrapper.trigger('drop', { dataTransfer })
      expect(wrapper.emitted('attach')).toBeTruthy()
    })
  })

  describe('Focus State', () => {
    it('should update isFocused on editor focus', async () => {
      wrapper = mount(ChatInputArea)
      expect(wrapper.vm.isFocused).toBe(false)

      await wrapper.find('.slack-rich-editor').trigger('focus')
      expect(wrapper.vm.isFocused).toBe(true)
    })

    it('should update isFocused on editor blur', async () => {
      wrapper = mount(ChatInputArea)
      await wrapper.find('.slack-rich-editor').trigger('focus')
      expect(wrapper.vm.isFocused).toBe(true)

      await wrapper.find('.slack-rich-editor').trigger('blur')
      expect(wrapper.vm.isFocused).toBe(false)
    })
  })

  describe('Insert Text', () => {
    it('should expose insertText method', () => {
      wrapper = mount(ChatInputArea)
      expect(typeof wrapper.vm.insertText).toBe('function')
    })
  })
})
