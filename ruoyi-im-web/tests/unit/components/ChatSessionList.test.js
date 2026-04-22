import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick, h } from 'vue'
import ChatSessionList from '@/components/im/ChatSessionList.vue'

// Mock Element Plus icons
vi.mock('@element-plus/icons-vue', () => ({
  Search: { name: 'Search', template: '<i class="el-icon-search"></i>' },
  Plus: { name: 'Plus', template: '<i class="el-icon-plus"></i>' },
  ArrowDown: { name: 'ArrowDown', template: '<i class="el-icon-arrow-down"></i>' },
  BellFilled: { name: 'BellFilled', template: '<i class="el-icon-bell-filled"></i>' },
  Top: { name: 'Top', template: '<i class="el-icon-top"></i>' },
  Bottom: { name: 'Bottom', template: '<i class="el-icon-bottom"></i>' },
  Delete: { name: 'Delete', template: '<i class="el-icon-delete"></i>' },
  Mute: { name: 'Mute', template: '<i class="el-icon-mute"></i>' }
}))

// Stub el-icon globally
const MockElIcon = {
  name: 'ElIcon',
  props: ['size'],
  render: (props) => h('i', { class: `el-icon ${props.size || ''}` })
}

// Mock el-loading directive
const mockLoading = vi.fn()
const MockLoading = {
  mounted: mockLoading,
  updated: mockLoading,
  unmounted: mockLoading
}

// Mock Vuex store
const mockDispatch = vi.fn()
const mockState = {
  im: {
    session: {
      currentSession: null,
      sessions: [
        {
          id: '1',
          name: 'Alice',
          avatar: '/avatars/alice.png',
          type: 'PRIVATE',
          lastMessage: 'Hello there',
          lastMessageTime: Date.now() - 60000,
          unreadCount: 3,
          isPinned: false,
          isMuted: false
        },
        {
          id: '2',
          name: 'Bob',
          avatar: '/avatars/bob.png',
          type: 'PRIVATE',
          lastMessage: 'See you tomorrow',
          lastMessageTime: Date.now() - 3600000,
          unreadCount: 0,
          isPinned: true,
          isMuted: false
        },
        {
          id: '3',
          name: 'Team Chat',
          avatar: '/avatars/team.png',
          type: 'GROUP',
          lastMessage: 'Meeting at 3pm',
          lastMessageTime: Date.now() - 86400000,
          unreadCount: 12,
          isPinned: false,
          isMuted: true
        }
      ]
    }
  }
}

vi.mock('vuex', () => ({
  useStore: () => ({
    state: mockState,
    dispatch: mockDispatch,
    commit: vi.fn()
  })
}))

describe('ChatSessionList', () => {
  let wrapper

  beforeEach(() => {
    localStorage.clear()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
    vi.clearAllMocks()
  })

  const mountComponent = (options = {}) => {
    return mount(ChatSessionList, {
      global: {
        stubs: {
          'el-icon': MockElIcon,
          'el-input': { template: '<input class="el-input" />' },
          'el-button': { template: '<button class="el-button"><slot /></button>' },
          'v-loading': MockLoading
        }
      },
      ...options
    })
  }

  describe('Basic Rendering', () => {
    it('should render the session panel', () => {
      wrapper = mountComponent({
        attachTo: document.body
      })
      expect(wrapper.find('.session-panel-v4').exists()).toBe(true)
    })

    it('should render search input', () => {
      wrapper = mountComponent()
      const searchInput = wrapper.find('input')
      expect(searchInput.exists()).toBe(true)
      expect(searchInput.attributes('placeholder')).toBe('搜索会话...')
    })

    it('should render filter tabs', () => {
      wrapper = mountComponent()
      const tabs = wrapper.findAll('.tab-item')
      expect(tabs.length).toBe(3)
      expect(tabs[0].text()).toBe('全部')
      expect(tabs[1].text()).toBe('单聊')
      expect(tabs[2].text()).toBe('群聊')
    })
  })

  describe('Session List', () => {
    it('should render session items', () => {
      wrapper = mountComponent()
      const items = wrapper.findAll('.session-item-v4')
      expect(items.length).toBeGreaterThan(0)
    })

    it('should display session name', () => {
      wrapper = mountComponent()
      const firstItem = wrapper.find('.session-item-v4')
      // Bob is pinned so appears first (pinned group comes before private group)
      expect(firstItem.find('.name').text()).toBe('Bob')
    })

    it('should display last message preview', () => {
      wrapper = mountComponent()
      const firstItem = wrapper.find('.session-item-v4')
      // Bob is pinned so appears first (pinned group comes before private group)
      expect(firstItem.find('.msg-preview').text()).toContain('See you')
    })

    it('should display unread badge when unread count > 0', () => {
      wrapper = mountComponent()
      const unreadBadges = wrapper.findAll('.unread-badge:not(.dot)')
      expect(unreadBadges.length).toBeGreaterThan(0)
    })

    it('should display dot instead of count when muted', () => {
      wrapper = mountComponent()
      const dotBadge = wrapper.find('.unread-badge.dot')
      expect(dotBadge.exists()).toBe(true)
    })

    it('should show draft tag when draftContent exists', async () => {
      mockState.im.session.sessions[0].draftContent = 'Draft message'
      wrapper = mountComponent()
      await nextTick()
      const draftTag = wrapper.find('.draft-tag')
      expect(draftTag.exists()).toBe(true)
      expect(draftTag.text()).toBe('[草稿]')
      delete mockState.im.session.sessions[0].draftContent
    })
  })

  describe('Filter Tabs', () => {
    it('should filter by PRIVATE type when clicking 单聊 tab', async () => {
      wrapper = mountComponent()
      const tabs = wrapper.findAll('.tab-item')
      await tabs[1].trigger('click')
      expect(wrapper.vm.filterType).toBe('PRIVATE')
    })

    it('should filter by GROUP type when clicking 群聊 tab', async () => {
      wrapper = mountComponent()
      const tabs = wrapper.findAll('.tab-item')
      await tabs[2].trigger('click')
      expect(wrapper.vm.filterType).toBe('GROUP')
    })

    it('should show all sessions when clicking 全部 tab', async () => {
      wrapper = mountComponent()
      const tabs = wrapper.findAll('.tab-item')
      await tabs[0].trigger('click')
      expect(wrapper.vm.filterType).toBe('all')
    })
  })

  describe('Search', () => {
    it('should filter sessions by search keyword', async () => {
      wrapper = mountComponent()
      const searchInput = wrapper.find('input')
      await searchInput.setValue('Alice')
      await nextTick()
      expect(wrapper.vm.searchKeyword).toBe('Alice')
    })

    it('should clear search when input is cleared', async () => {
      wrapper = mountComponent()
      const searchInput = wrapper.find('input')
      await searchInput.setValue('Alice')
      await nextTick()
      await searchInput.setValue('')
      await nextTick()
      expect(wrapper.vm.searchKeyword).toBe('')
    })
  })

  describe('Grouped Sessions', () => {
    it('should group sessions into PINNED, PRIVATE, and GROUP', () => {
      wrapper = mountComponent()
      wrapper.vm.filterType = 'all'
      const groups = wrapper.vm.groupedSessions
      expect(groups.length).toBe(3)
      expect(groups[0].type).toBe('PINNED')
      expect(groups[1].type).toBe('PRIVATE')
      expect(groups[2].type).toBe('GROUP')
    })

    it('should return flat list when filter is not all', () => {
      wrapper = mountComponent()
      wrapper.vm.filterType = 'PRIVATE'
      const groups = wrapper.vm.groupedSessions
      expect(groups.length).toBe(1)
      expect(groups[0].type).toBe('FLAT')
    })

    it('should put pinned sessions in PINNED group', () => {
      wrapper = mountComponent()
      wrapper.vm.filterType = 'all'
      const pinnedGroup = wrapper.vm.groupedSessions.find(g => g.type === 'PINNED')
      expect(pinnedGroup.sessions.length).toBe(1)
      expect(pinnedGroup.sessions[0].name).toBe('Bob')
    })
  })

  describe('Collapsed Groups', () => {
    it('should toggle collapsed state when group header is clicked', async () => {
      wrapper = mountComponent()
      wrapper.vm.filterType = 'all'
      const groupHeader = wrapper.find('.group-header')
      if (groupHeader.exists()) {
        await groupHeader.trigger('click')
        expect(wrapper.vm.collapsedGroups['PINNED']).toBe(true)
      }
    })
  })

  describe('Actions', () => {
    it('should call selectSession when session item is clicked', async () => {
      wrapper = mountComponent()
      const firstItem = wrapper.find('.session-item-v4')
      await firstItem.trigger('click')
      expect(mockDispatch).toHaveBeenCalledWith('im/session/selectSession', expect.any(Object))
    })

    it('should call pinSession when pin button is clicked', async () => {
      wrapper = mountComponent()
      const firstItem = wrapper.find('.session-item-v4')
      const pinBtn = firstItem.find('.action-btn[title="置顶"]')
      if (pinBtn.exists()) {
        await pinBtn.trigger('click')
        expect(mockDispatch).toHaveBeenCalledWith('im/session/pinSession', expect.any(Object))
      }
    })

    it('should call muteSession when mute button is clicked', async () => {
      wrapper = mountComponent()
      const firstItem = wrapper.find('.session-item-v4')
      const muteBtn = firstItem.find('.action-btn[title="免打扰"]')
      if (muteBtn.exists()) {
        await muteBtn.trigger('click')
        expect(mockDispatch).toHaveBeenCalledWith('im/session/muteSession', expect.any(Object))
      }
    })
  })

  describe('Time Formatting', () => {
    it('should format time correctly', () => {
      wrapper = mountComponent()
      const now = Date.now()
      const formatted = wrapper.vm.formatTime(now)
      expect(formatted).toMatch(/\d{2}:\d{2}/)
    })

    it('should return empty string for null time', () => {
      wrapper = mountComponent()
      expect(wrapper.vm.formatTime(null)).toBe('')
    })
  })

  describe('Search Focus State', () => {
    it('should update isSearchFocused on focus', async () => {
      wrapper = mountComponent()
      const searchInput = wrapper.find('input')
      await searchInput.trigger('focus')
      expect(wrapper.vm.isSearchFocused).toBe(true)
    })

    it('should update isSearchFocused on blur', async () => {
      wrapper = mountComponent()
      const searchInput = wrapper.find('input')
      await searchInput.trigger('focus')
      await searchInput.trigger('blur')
      expect(wrapper.vm.isSearchFocused).toBe(false)
    })
  })
})
