class Logger {
  constructor() {
    this.level = process.env.NODE_ENV === 'production' ? 'error' : 'debug'
    this.levels = {
      debug: 0,
      info: 1,
      warn: 2,
      error: 3,
    }
    this.prefix = '[IM]'
  }

  setLevel(level) {
    if (this.levels[level] !== undefined) {
      this.level = level
    }
  }

  setPrefix(prefix) {
    this.prefix = prefix
  }

  formatMessage(level, args) {
    const timestamp = new Date().toISOString()
    const message = args.map(arg => {
      if (typeof arg === 'object') {
        return JSON.stringify(arg, null, 2)
      }
      return String(arg)
    }).join(' ')
    return `${timestamp} ${this.prefix} [${level.toUpperCase()}] ${message}`
  }

  shouldLog(level) {
    return this.levels[level] >= this.levels[this.level]
  }

  debug(...args) {
    if (this.shouldLog('debug')) {
      console.log(this.formatMessage('debug', args), ...args)
    }
  }

  info(...args) {
    if (this.shouldLog('info')) {
      console.info(this.formatMessage('info', args), ...args)
    }
  }

  warn(...args) {
    if (this.shouldLog('warn')) {
      console.warn(this.formatMessage('warn', args), ...args)
    }
  }

  error(...args) {
    if (this.shouldLog('error')) {
      console.error(this.formatMessage('error', args), ...args)
    }
  }

  group(label) {
    console.group(`${this.prefix} ${label}`)
  }

  groupEnd() {
    console.groupEnd()
  }

  table(data) {
    if (this.shouldLog('debug')) {
      console.table(data)
    }
  }

  time(label) {
    if (this.shouldLog('debug')) {
      console.time(`${this.prefix} ${label}`)
    }
  }

  timeEnd(label) {
    if (this.shouldLog('debug')) {
      console.timeEnd(`${this.prefix} ${label}`)
    }
  }

  trace(...args) {
    if (this.shouldLog('debug')) {
      console.trace(this.formatMessage('trace', args), ...args)
    }
  }
}

export default new Logger()