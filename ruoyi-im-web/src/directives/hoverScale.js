const hoverScale = {
  mounted(el, binding) {
    el.style.transition = 'transform 0.2s cubic-bezier(0.4, 0, 0.2, 1)'
    el.addEventListener('mouseenter', handleMouseEnter)
    el.addEventListener('mouseleave', handleMouseLeave)
  },
  unmounted(el) {
    el.removeEventListener('mouseenter', handleMouseEnter)
    el.removeEventListener('mouseleave', handleMouseLeave)
    el.style.transform = ''
  },
}

function handleMouseEnter(e) {
  e.currentTarget.style.transform = 'scale(1.05)'
}

function handleMouseLeave(e) {
  e.currentTarget.style.transform = 'scale(1)'
}

export default hoverScale
