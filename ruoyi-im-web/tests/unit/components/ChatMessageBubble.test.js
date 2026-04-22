import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import ChatMessageBubble from '@/components/im/ChatMessageBubble.vue'

// Mock Element Plus icons
vi.mock('@element-plus/icons-vue', () => ({
  Check: { name: 'Check', template: '<i class="el-icon-check"></i>' },
  Document: { name: 'Document', template: '<i class="el-icon-document"></i>' },
  CaretRight: { name: 'CaretRight', template: '<i class="el-icon-caret-right"></i>' },
  CircleCheck: { name: 'CircleCheck', template: '<i class="el-icon-circle-check"></i>' },
  ChatDotSquare: { name: 'ChatDotSquare', template: '<i class="el-icon-chat-dot-square"></i>' },
  Share: { name: 'Share', template: '<i class="el-icon-share"></i>' },
  MoreFilled: { name: 'MoreFilled', template: '<i class="el-icon-more-filled"></i>' },
  Location: { name: 'Location', template: '<i class="el-icon-location"></i>' },
  Smile: { name: 'Smile', template: '<i class="el-icon-smile"></i>' }
}))

// Mock ElImage
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElImage: {
      name: 'ElImage',
      props: ['src', 'previewSrcList', 'fit', 'class', 'lazy'],
      template: '<img class="el-image" />'
    },
    ElButton: {
      name: 'ElButton',
      props: ['size', 'plain'],
      template: '<button class="el-button"><slot /></button>'
    }
  }
})

describe('ChatMessageBubble', () => {
  let wrapper

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
    vi.clearAllMocks()
  })

  const defaultMessage = {
    id: 'msg-1',
    messageType: 'TEXT',
    content: 'Hello World',
    senderId: 'user-1',
    senderName: 'Alice',
    senderAvatar: '/avatars/alice.png',
    sendTime: new Date().toISOString()
  }

  const mountComponent = (props = {}) => {
    return mount(ChatMessageBubble, {
      props: {
        message: defaultMessage,
        isMe: false,
        isGrouped: false,
        showTime: true,
        isSelectionMode: false,
        isSelected: false,
        ...props
      }
    })
  }

  describe('Basic Rendering', () => {
    it('should render the message row', () => {
      wrapper = mountComponent()
      expect(wrapper.find('.slack-message-row').exists()).toBe(true)
    })

    it('should render text content', () => {
      wrapper = mountComponent()
      expect(wrapper.find('.rich-text-content').exists()).toBe(true)
      expect(wrapper.find('.rich-text-content').text()).toBe('Hello World')
    })

    it('should not show checkbox zone when not in selection mode', () => {
      wrapper = mountComponent({ isSelectionMode: false, isSelected: false })
      expect(wrapper.find('.slack-checkbox-zone').exists()).toBe(false)
    })

    it('should show checkbox zone when in selection mode', () => {
      wrapper = mountComponent({ isSelectionMode: true, isSelected: false })
      expect(wrapper.find('.slack-checkbox-zone').exists()).toBe(true)
    })
  })

  describe('Message Types', () => {
    it('should render TEXT message type', () => {
      wrapper = mountComponent({ message: { ...defaultMessage, messageType: 'TEXT', content: 'Test text' } })
      expect(wrapper.find('.rich-text-content').exists()).toBe(true)
      expect(wrapper.find('.rich-text-content').text()).toBe('Test text')
    })

    it('should render IMAGE message type', () => {
      wrapper = mountComponent({ message: { ...defaultMessage, messageType: 'IMAGE', content: 'https://example.com/image.jpg' } })
      expect(wrapper.find('.media-content').exists()).toBe(true)
    })

    it('should render VIDEO message type', () => {
      wrapper = mountComponent({ message: { ...defaultMessage, messageType: 'VIDEO', content: 'https://example.com/video.mp4' } })
      expect(wrapper.find('.media-content.video').exists()).toBe(true)
    })

    it('should render FILE message type', () => {
      wrapper = mountComponent({ message: { ...defaultMessage, messageType: 'FILE', fileName: 'test.pdf', fileSize: 1024 * 1024 } })
      expect(wrapper.find('.file-card').exists()).toBe(true)
    })

    it('should render CARD (contact) message type', () => {
      wrapper = mountComponent({
        message: {
          ...defaultMessage,
          messageType: 'CARD',
          payload: { userId: '123', userName: 'John', department: 'Engineering' }
        }
      })
      expect(wrapper.find('.contact-card').exists()).toBe(true)
    })

    it('should render LOCATION message type', () => {
      wrapper = mountComponent({
        message: {
          ...defaultMessage,
          messageType: 'LOCATION',
          payload: { title: 'Office', address: '123 Main St', latitude: 40.7, longitude: -74.0 }
        }
      })
      expect(wrapper.find('.location-card').exists()).toBe(true)
    })
  })

  describe('Avatar Display', () => {
    it('should show avatar when not grouped', () => {
      wrapper = mountComponent({ isGrouped: false })
      expect(wrapper.find('.avatar').exists()).toBe(true)
    })

    it('should hide avatar and show time when grouped', () => {
      wrapper = mountComponent({ isGrouped: true })
      expect(wrapper.find('.avatar').exists()).toBe(false)
      expect(wrapper.find('.time-hover').exists()).toBe(true)
    })

    it('should use default avatar when senderAvatar is not provided', () => {
      wrapper = mountComponent({ message: { ...defaultMessage, senderAvatar: '' } })
      const avatar = wrapper.find('.avatar')
      expect(avatar.attributes('src')).toBe('/avatars/default.png')
    })
  })

  describe('Sender Info', () => {
    it('should display sender name', () => {
      wrapper = mountComponent({ isGrouped: false })
      expect(wrapper.find('.sender-name').text()).toBe('Alice')
    })

    it('should show sender name only when not grouped', () => {
      wrapper = mountComponent({ isGrouped: true })
      expect(wrapper.find('.sender-name').exists()).toBe(false)
    })

    it('should use "成员" when senderName is not provided', () => {
      wrapper = mountComponent({ message: { ...defaultMessage, senderName: '' }, isGrouped: false })
      expect(wrapper.find('.sender-name').text()).toBe('成员')
    })
  })

  describe('Time Display', () => {
    it('should format time correctly', () => {
      const now = new Date()
      const messageWithTime = {
        ...defaultMessage,
        sendTime: now.toISOString()
      }
      wrapper = mountComponent({ message: messageWithTime })
      const expected = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      expect(wrapper.find('.timestamp').text()).toBe(expected)
    })

    it('should show hover time when grouped', () => {
      wrapper = mountComponent({ isGrouped: true })
      expect(wrapper.find('.time-hover').exists()).toBe(true)
    })
  })

  describe('Selection Mode', () => {
    it('should add is-selected class when selected', () => {
      wrapper = mountComponent({ isSelected: true })
      expect(wrapper.find('.slack-message-row.is-selected').exists()).toBe(true)
    })

    it('should emit toggle-selection when checkbox is clicked', async () => {
      wrapper = mountComponent({ isSelectionMode: true, message: defaultMessage })
      const checkboxZone = wrapper.find('.slack-checkbox-zone')
      await checkboxZone.trigger('click')
      expect(wrapper.emitted('toggle-selection')).toBeTruthy()
    })
  })

  describe('Hover State', () => {
    it('should update isHovered on mouseenter', async () => {
      wrapper = mountComponent()
      const row = wrapper.find('.slack-message-row')
      await row.trigger('mouseenter')
      await nextTick()
      expect(wrapper.vm.isHovered).toBe(true)
    })

    it('should update isHovered on mouseleave', async () => {
      wrapper = mountComponent()
      const row = wrapper.find('.slack-message-row')
      await row.trigger('mouseenter')
      await row.trigger('mouseleave')
      await nextTick()
      expect(wrapper.vm.isHovered).toBe(false)
    })
  })

  describe('File Card', () => {
    it('should display file name', () => {
      wrapper = mountComponent({
        message: { ...defaultMessage, messageType: 'FILE', fileName: 'document.pdf' }
      })
      expect(wrapper.find('.f-name').text()).toBe('document.pdf')
    })

    it('should format file size correctly', () => {
      wrapper = mountComponent({
        message: { ...defaultMessage, messageType: 'FILE', fileName: 'doc.pdf', fileSize: 2 * 1024 * 1024 }
      })
      expect(wrapper.find('.f-meta').text()).toContain('2.0 MB')
    })

    it('should use "未知文件" when fileName is not provided', () => {
      wrapper = mountComponent({
        message: { ...defaultMessage, messageType: 'FILE', fileName: '' }
      })
      expect(wrapper.find('.f-name').text()).toBe('未知文件')
    })
  })

  describe('Contact Card', () => {
    it('should display user name', () => {
      wrapper = mountComponent({
        message: {
          ...defaultMessage,
          messageType: 'CARD',
          payload: { userName: 'John Doe' }
        }
      })
      expect(wrapper.find('.c-name').text()).toBe('John Doe')
    })

    it('should display department or default text', () => {
      wrapper = mountComponent({
        message: {
          ...defaultMessage,
          messageType: 'CARD',
          payload: { userName: 'John', department: 'Engineering' }
        }
      })
      expect(wrapper.find('.c-desc').text()).toBe('Engineering')
    })

    it('should use "名片" as default description', () => {
      wrapper = mountComponent({
        message: {
          ...defaultMessage,
          messageType: 'CARD',
          payload: { userName: 'John' }
        }
      })
      expect(wrapper.find('.c-desc').text()).toBe('名片')
    })
  })

  describe('Location Card', () => {
    it('should display location title', () => {
      wrapper = mountComponent({
        message: {
          ...defaultMessage,
          messageType: 'LOCATION',
          payload: { title: 'Home', address: '123 Main St' }
        }
      })
      expect(wrapper.find('.l-title').text()).toBe('Home')
    })

    it('should display address', () => {
      wrapper = mountComponent({
        message: {
          ...defaultMessage,
          messageType: 'LOCATION',
          payload: { title: 'Office', address: '456 Broadway' }
        }
      })
      expect(wrapper.find('.l-address').text()).toBe('456 Broadway')
    })

    it('should use "位置" as default title', () => {
      wrapper = mountComponent({
        message: {
          ...defaultMessage,
          messageType: 'LOCATION',
          payload: {}
        }
      })
      expect(wrapper.find('.l-title').text()).toBe('位置')
    })
  })

  describe('Emits', () => {
    it('should emit show-user when avatar is clicked', async () => {
      wrapper = mountComponent({ isGrouped: false })
      const avatar = wrapper.find('.avatar')
      await avatar.trigger('click')
      expect(wrapper.emitted('show-user')).toBeTruthy()
    })
  })
})
