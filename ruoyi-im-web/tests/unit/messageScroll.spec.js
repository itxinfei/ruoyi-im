import { describe, it, expect, vi, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'
import { useMessageScroll } from '@/components/Chat/composables/useMessageScroll'

const createWrapper = () => {
  let emitMock
  const Comp = {
    template: '<div ref="listRef"></div>',
    setup() {
      const listRef = ref(null)
      const isUnmounted = ref(false)
      emitMock = vi.fn()
      const api = useMessageScroll(listRef, emitMock, isUnmounted)
      return { listRef, emitMock, ...api }
    }
  }
  const wrapper = mount(Comp, { attachTo: document.body })
  return { wrapper, emitMock }
}

const setScrollMetrics = (el, { scrollTop, scrollHeight, clientHeight }) => {
  Object.defineProperty(el, 'scrollHeight', { value: scrollHeight, configurable: true })
  Object.defineProperty(el, 'clientHeight', { value: clientHeight, configurable: true })
  el.scrollTop = scrollTop
}

afterEach(() => {
  vi.useRealTimers()
})

describe('useMessageScroll', () => {
  it('在向上滚动到顶部时触发加载更多', async () => {
    vi.useFakeTimers()
    const { wrapper, emitMock } = createWrapper()
    const el = wrapper.vm.listRef
    setScrollMetrics(el, { scrollTop: 200, scrollHeight: 1000, clientHeight: 400 })
    wrapper.vm.handleScroll({ target: el })
    vi.advanceTimersByTime(120)

    setScrollMetrics(el, { scrollTop: 40, scrollHeight: 1000, clientHeight: 400 })
    wrapper.vm.handleScroll({ target: el })
    vi.advanceTimersByTime(120)

    expect(emitMock).toHaveBeenCalledWith('load-more', 'top')
    wrapper.unmount()
  })

  it('在滚动到底部时触发加载更多', () => {
    vi.useFakeTimers()
    const { wrapper, emitMock } = createWrapper()
    const el = wrapper.vm.listRef
    setScrollMetrics(el, { scrollTop: 620, scrollHeight: 1000, clientHeight: 400 })
    wrapper.vm.handleScroll({ target: el })
    vi.advanceTimersByTime(120)

    expect(emitMock).toHaveBeenCalledWith('load-more', 'bottom')
    wrapper.unmount()
  })

  it('距离底部较远时显示回到底部按钮', () => {
    vi.useFakeTimers()
    const { wrapper } = createWrapper()
    const el = wrapper.vm.listRef
    setScrollMetrics(el, { scrollTop: 100, scrollHeight: 1000, clientHeight: 400 })
    wrapper.vm.handleScroll({ target: el })
    vi.advanceTimersByTime(120)

    expect(wrapper.vm.showScrollToBottom).toBe(true)
    wrapper.unmount()
  })
})
