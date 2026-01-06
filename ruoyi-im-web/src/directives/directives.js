/**
 * 自定义指令集合
 */
import clickFeedback from './clickFeedback.js'
import hoverScale from './hoverScale.js'

const directives = {
  clickFeedback,
  hoverScale,
}

export default {
  install(app) {
    Object.keys(directives).forEach(key => {
      app.directive(key, directives[key])
    })
  },
}

export { clickFeedback, hoverScale }
