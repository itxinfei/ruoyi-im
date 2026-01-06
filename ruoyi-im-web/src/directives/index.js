/**
 * 点击反馈指令
 * 点击时添加缩小动画效果
 */
import { ElMessage } from 'element-plus'

export default {
  mounted(el, binding) {
    const handleClick = (e) => {
      el.classList.add('clicking')
      setTimeout(() => {
        el.classList.remove('clicking')
      }, 200)

      // 如果有回调函数则执行
      if (typeof binding.value === 'function') {
        binding.value(e)
      }
    }

    el._clickFeedbackHandler = handleClick
    el.addEventListener('click', handleClick)
  },

  unmounted(el) {
    if (el._clickFeedbackHandler) {
      el.removeEventListener('click', el._clickFeedbackHandler)
    }
  },
}
