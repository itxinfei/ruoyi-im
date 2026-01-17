import { ElMessage } from 'element-plus'

const clickFeedback = {
  mounted(el, binding) {
    el.addEventListener('click', handleClick)
  },
  unmounted(el) {
    el.removeEventListener('click', handleClick)
  },
}

function handleClick(e) {
  const ripple = document.createElement('span')
  ripple.style.cssText = `
    position: absolute;
    background: rgba(22, 119, 255, 0.3);
    border-radius: 50%;
    transform: scale(0);
    pointer-events: none;
    width: 100px;
    height: 100px;
    left: 50%;
    top: 50%;
    margin-left: -50px;
    margin-top: -50px;
  `

  const rect = e.currentTarget.getBoundingClientRect()
  const size = Math.max(rect.width, rect.height)
  ripple.style.width = ripple.style.height = size + 'px'

  e.currentTarget.style.position = 'relative'
  e.currentTarget.style.overflow = 'hidden'
  e.currentTarget.appendChild(ripple)

  requestAnimationFrame(() => {
    ripple.style.transform = 'scale(2)'
    ripple.style.opacity = '0'
  })

  setTimeout(() => {
    if (ripple.parentNode) {
      ripple.parentNode.removeChild(ripple)
    }
  }, 600)
}

export default clickFeedback
