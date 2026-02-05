/**
 * 输入框快捷命令 Composable
 *
 * 职责：
 * - 处理 / 命令面板显示
 * - 命令选择和执行
 * - 命令配置管理
 *
 * 使用方式：
 * ```js
 * const { showCommandPalette, handleInput, handleCommandSelect } = useInputCommand({
 *   textareaRef,
 *   messageContent,
 *   commands: COMMANDS,
 *   onCommandExecute
 * })
 * ```
 */
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * 默认命令配置
 */
export const DEFAULT_COMMANDS = {
  document: {
    id: 'document',
    label: '发送文件',
    icon: 'folder',
    action: 'upload-file'
  },
  schedule: {
    id: 'schedule',
    label: '创建日程',
    icon: 'calendar_today',
    action: 'create-schedule'
  },
  meeting: {
    id: 'meeting',
    label: '发起会议',
    icon: 'videocam',
    action: 'start-video'
  },
  todo: {
    id: 'todo',
    label: '创建待办',
    icon: 'check_circle',
    action: 'create-todo'
  },
  approval: {
    id: 'approval',
    label: '发起审批',
    icon: 'assignment',
    action: 'create-approval'
  },
  vote: {
    id: 'vote',
    label: '发起投票',
    icon: 'poll',
    action: 'coming-soon'
  },
  announcement: {
    id: 'announcement',
    label: '发布公告',
    icon: 'campaign',
    action: 'create-announcement'
  },
  checkin: {
    id: 'checkin',
    label: '发送位置',
    icon: 'location_on',
    action: 'send-location'
  }
}

export function useInputCommand(options = {}) {
  const {
    textareaRef = ref(null),
    messageContent = ref(''),
    commands = DEFAULT_COMMANDS,
    onCommandExecute = null
  } = options

  const showCommandPalette = ref(false)
  const commandPalettePosition = ref({ x: 0, y: 0 })

  /**
   * 在光标位置显示命令面板
   */
  const showCommandPaletteAtCursor = () => {
    const tx = textareaRef.value
    if (!tx) {return}

    const rect = tx.getBoundingClientRect()
    commandPalettePosition.value = {
      x: rect.left,
      y: rect.top - 400 // 显示在输入框上方
    }

    showCommandPalette.value = true
  }

  /**
   * 隐藏命令面板
   */
  const hideCommandPalette = () => {
    showCommandPalette.value = false
  }

  /**
   * 处理命令选择
   * @param {string} commandId - 命令ID
   */
  const handleCommandSelect = commandId => {
    const currentContent = messageContent.value
    const cursorPos = textareaRef.value?.selectionStart || 0

    // 找到 / 的位置并移除它
    let slashPos = cursorPos - 1
    while (slashPos >= 0 && currentContent[slashPos] !== '/') {
      slashPos--
    }

    // 移除命令触发符
    if (slashPos >= 0) {
      const newContent = currentContent.slice(0, slashPos) + currentContent.slice(cursorPos)
      messageContent.value = newContent
    }

    hideCommandPalette()

    // 执行命令
    const command = commands[commandId] || Object.values(commands).find(c => c.id === commandId)
    if (command) {
      executeCommand(command)
    }
  }

  /**
   * 执行命令
   * @param {Object} command - 命令对象
   */
  const executeCommand = command => {
    if (onCommandExecute) {
      onCommandExecute(command)
      return
    }

    // 默认处理
    switch (command.action) {
      case 'upload-file':
        ElMessage.info('请点击文件按钮上传文件')
        break
      case 'create-schedule':
        ElMessage.info('日程创建功能')
        break
      case 'start-video':
        ElMessage.info('视频会议功能')
        break
      case 'create-todo':
        ElMessage.info('待办创建功能')
        break
      case 'create-approval':
        ElMessage.info('审批创建功能')
        break
      case 'coming-soon':
        ElMessage.info(`${command.label}即将上线`)
        break
      case 'create-announcement':
        ElMessage.info('公告创建功能')
        break
      case 'send-location':
        ElMessage.info('位置发送功能')
        break
      default:
        ElMessage.info(`命令 ${command.label} 即将上线`)
    }
  }

  /**
   * 处理输入事件，检测命令触发
   */
  const handleInputCheck = () => {
    const value = messageContent.value
    const cursorPos = textareaRef.value?.selectionStart || 0
    const lastChar = value.charAt(cursorPos - 1)

    // 如果输入了 / 且在消息开头或前面有空格，显示命令面板
    if (lastChar === '/' && (cursorPos === 1 || value.charAt(cursorPos - 2) === ' ')) {
      showCommandPaletteAtCursor()
    } else if (showCommandPalette.value && !value.includes('/')) {
      hideCommandPalette()
    }
  }

  /**
   * 获取可用命令列表
   */
  const getAvailableCommands = () => {
    return Object.values(commands)
  }

  return {
    // 状态
    showCommandPalette,
    commandPalettePosition,

    // 方法
    showCommandPaletteAtCursor,
    hideCommandPalette,
    handleCommandSelect,
    handleInputCheck,
    executeCommand,
    getAvailableCommands
  }
}
