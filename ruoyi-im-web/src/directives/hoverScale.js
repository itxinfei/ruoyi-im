/**
 * 悬停缩放指令
 * 鼠标悬停时轻微放大元素
 */
export default {
  mounted(el, binding) {
    const scale = binding.value || 1.05
    el.addEventListener('mouseenter', () => {
      el.style.transform = `scale(${scale})`
      el.style.transition = 'transform 0.2s cubic-bezier(0.4, 0, 0.2, 1)'
    })
    el.addEventListener('mouseleave', () => {
      el.style.transform = 'scale(1)'
    })
  },
}
