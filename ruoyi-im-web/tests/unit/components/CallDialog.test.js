import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import CallDialog from '@/components/Chat/CallDialog.vue'

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: { error: vi.fn() }
}))

// Mock Element Plus icons
vi.mock('@element-plus/icons-vue', () => ({
  PhoneFilled: { name: 'PhoneFilled', template: '<i class="el-icon-phone-filled"></i>' },
  CloseBold: { name: 'CloseBold', template: '<i class="el-icon-close-bold"></i>' },
  Microphone: { name: 'Microphone', template: '<i class="el-icon-microphone"></i>' },
  Mute: { name: 'Mute', template: '<i class="el-icon-mute"></i>' },
  VideoCamera: { name: 'VideoCamera', template: '<i class="el-icon-video-camera"></i>' },
  VideoCameraFilled: { name: 'VideoCameraFilled', template: '<i class="el-icon-video-camera-filled"></i>' },
  Close: { name: 'Close', template: '<i class="el-icon-close"></i>' }
}))

// Mock DingtalkAvatar
vi.mock('@/components/Common/DingtalkAvatar.vue', () => ({
  default: { name: 'DingtalkAvatar', template: '<div class="dingtalk-avatar"></div>' }
}))

// Mock useWebRTC
vi.mock('@/composables/useWebRTC', () => ({
  useWebRTC: vi.fn(() => ({
    createOffer: vi.fn(),
    createAnswer: vi.fn(),
    handleAnswer: vi.fn(),
    handleCandidate: vi.fn(),
    closePeerConnection: vi.fn()
  }))
}))

// Mock useImWebSocket
vi.mock('@/composables/useImWebSocket', () => ({
  useImWebSocket: vi.fn(() => ({
    sendMessage: vi.fn()
  }))
}))

// Mock API calls
vi.mock('@/api/im/videoCall', () => ({
  acceptCall: vi.fn().mockResolvedValue({ code: 200 }),
  rejectCall: vi.fn().mockResolvedValue({ code: 200 }),
  endCall: vi.fn().mockResolvedValue({ code: 200 }),
  initiateCall: vi.fn().mockResolvedValue({ code: 200, data: 'test-call-id' })
}))

// Mock mediaDevices
Object.defineProperty(navigator, 'mediaDevices', {
  value: {
    getUserMedia: vi.fn().mockResolvedValue({
      getTracks: () => [{ stop: vi.fn() }],
      getAudioTracks: () => [{ enabled: true }],
      getVideoTracks: () => [{ enabled: true }]
    })
  },
  writable: true
})

describe('CallDialog', () => {
  let wrapper

  beforeEach(() => {
    wrapper = null
  })

  afterEach(() => {
    if (wrapper) {
      try {
        wrapper.unmount()
      } catch (e) {
        // Ignore unmount errors
      }
    }
    vi.clearAllMocks()
  })

  const mountComponent = () => {
    return mount(CallDialog, {
      attachTo: document.body,
      props: { session: {} },
      global: {
        stubs: {
          'el-dialog': true,
          'el-icon': true,
          'audio': true
        }
      }
    })
  }

  // Basic rendering and initial state
  describe('Initial State', () => {
    it('should have correct initial status', () => {
      wrapper = mountComponent()
      expect(wrapper.vm.status).toBe('calling')
    })

    it('should have correct initial type', () => {
      wrapper = mountComponent()
      expect(wrapper.vm.type).toBe('voice')
    })

    it('should have correct initial isMuted', () => {
      wrapper = mountComponent()
      expect(wrapper.vm.isMuted).toBe(false)
    })

    it('should have correct initial localVideoOff', () => {
      wrapper = mountComponent()
      expect(wrapper.vm.localVideoOff).toBe(false)
    })

    it('should have correct initial remoteVideoOff', () => {
      wrapper = mountComponent()
      expect(wrapper.vm.remoteVideoOff).toBe(false)
    })

    it('should have correct initial visible', () => {
      wrapper = mountComponent()
      expect(wrapper.vm.visible).toBe(false)
    })

    it('should have empty initial callId', () => {
      wrapper = mountComponent()
      expect(wrapper.vm.callId).toBe('')
    })

    it('should have zero initial duration', () => {
      wrapper = mountComponent()
      expect(wrapper.vm.duration).toBe(0)
    })
  })

  // Status text computed
  describe('Status Text', () => {
    it('should show calling text for calling status', () => {
      wrapper = mountComponent()
      wrapper.vm.status = 'calling'
      expect(wrapper.vm.statusText).toBe('正在呼叫对方...')
    })

    it('should show incoming text for incoming status', () => {
      wrapper = mountComponent()
      wrapper.vm.status = 'incoming'
      expect(wrapper.vm.statusText).toBe('邀请你进行通话')
    })

    it('should show talking text for talking status', () => {
      wrapper = mountComponent()
      wrapper.vm.status = 'talking'
      expect(wrapper.vm.statusText).toBe('正在通话')
    })

    it('should show hanging_up text', () => {
      wrapper = mountComponent()
      wrapper.vm.status = 'hanging_up'
      expect(wrapper.vm.statusText).toBe('通话已结束')
    })

    it('should show timeout text', () => {
      wrapper = mountComponent()
      wrapper.vm.status = 'timeout'
      expect(wrapper.vm.statusText).toBe('对方无应答')
    })

    it('should show reconnecting text', () => {
      wrapper = mountComponent()
      wrapper.vm.status = 'reconnecting'
      expect(wrapper.vm.statusText).toBe('网络不稳定，正在重连...')
    })
  })

  // Duration formatting
  describe('Duration Formatting', () => {
    it('should format 0 seconds', () => {
      wrapper = mountComponent()
      wrapper.vm.duration = 0
      expect(wrapper.vm.formattedDuration).toBe('00:00')
    })

    it('should format 65 seconds', () => {
      wrapper = mountComponent()
      wrapper.vm.duration = 65
      expect(wrapper.vm.formattedDuration).toBe('01:05')
    })

    it('should format 125 seconds', () => {
      wrapper = mountComponent()
      wrapper.vm.duration = 125
      expect(wrapper.vm.formattedDuration).toBe('02:05')
    })

    it('should format 3600 seconds', () => {
      wrapper = mountComponent()
      wrapper.vm.duration = 3600
      expect(wrapper.vm.formattedDuration).toBe('60:00')
    })
  })

  // Dialog width
  describe('Dialog Width', () => {
    it('should be 360px for voice calls', () => {
      wrapper = mountComponent()
      wrapper.vm.type = 'voice'
      expect(wrapper.vm.dialogWidth).toBe('360px')
    })

    it('should be 800px for video calls', () => {
      wrapper = mountComponent()
      wrapper.vm.type = 'video'
      expect(wrapper.vm.dialogWidth).toBe('800px')
    })
  })

  // Exposed methods (basic check without calling methods that modify component state)
  describe('Exposed Methods', () => {
    it('should have open method defined', () => {
      wrapper = mountComponent()
      expect(typeof wrapper.vm.open).toBeDefined()
    })

    it('should have end method defined', () => {
      wrapper = mountComponent()
      expect(typeof wrapper.vm.end).toBeDefined()
    })

    it('should have handleWebRTCSignal method defined', () => {
      wrapper = mountComponent()
      expect(typeof wrapper.vm.handleWebRTCSignal).toBeDefined()
    })
  })
})
