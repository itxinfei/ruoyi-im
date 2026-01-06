/**
 * 点击反馈指令
 * 点击时添加缩小动画效果
 */
export default {
  mounted(el) {
    el.addEventListener('mousedown', () => {
      el.style.transform = 'scale(0.95)'
      el.style.transition = 'transform 0.1s'
    })
    el.addEventListener('mouseup', () => {
      el.style.transform = 'scale(1)'
    })
    el.addEventListener('mouseleave', () => {
      el.style.transform = 'scale(1)'
    })
  },
}
