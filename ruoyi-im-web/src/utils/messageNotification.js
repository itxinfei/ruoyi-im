/**
 * 消息提醒工具类
 * 提供浏览器通知、提示音、标题闪烁等功能
 */
import { getDoNotDisturb } from './storage'

// 提示音音频对象
let notificationAudio = null
let titleFlashTimer = null
let originalTitle = document.title
let isTitleFlashing = false

/**
 * 初始化提示音
 */
function initNotificationSound() {
    if (!notificationAudio) {
        // 创建音频对象,使用data URI内嵌简单提示音
        // 这是一个简单的"叮"声,实际项目中应替换为真实音频文件
        notificationAudio = new Audio()
        // 临时使用系统提示音,后续可替换为自定义音频文件
        notificationAudio.src = 'data:audio/wav;base64,UklGRnoGAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQoGAACBhYqFbF1fdJivrJBhNjVgodDbq2EcBj+a2/LDciUFLIHO8tiJNwgZaLvt559NEAxQp+PwtmMcBjiR1/LMeSwFJHfH8N2QQAoUXrTp66hVFApGn+DyvmwhBSuBzvLZiTYIGGS57OihUBELTKXh8bllHgU2jdXvz3ozCCF1xe/glEILElyx6OyrWBUIQ5zd8sFuJAUuhM/z24k4CBllu+zooVARDEuk4fG5ZR4FNo3V789+MwghdsXv4JRCCxJcr+jrq1gVCEOb3fLBbiQFLoTP89uJOAgZZbvs6KFQEQxLpOHxuWUeBTaN1e/PfjMIIXbF7+CUQgsRXK/o66tYFQhDm93ywW4kBS6Ez/PbiTgIGWW77OihUBEMS6Th8bllHgU2jdXvz34zCCF2xe/glEILEVyv6OurWBUIQ5vd8sFuJAUuhM/z24k4CBllu+zooVARDEuk4fG5ZR4FNo3V789+MwghdsXv4JRCCxFcr+jrq1gVCEOb3fLBbiQFLoTP89uJOAgZZbvs6KFQEQxLpOHxuWUeBTaN1e/PfjMIIXbF7+CUQgsRXK/o66tYFQhDm93ywW4kBS6Ez/PbiTgIGWW77OihUBEMS6Th8bllHgU2jdXvz34zCCF2xe/glEILEVyv6OurWBUIQ5vd8sFuJAUuhM/z24k4CBllu+zooVARDEuk4fG5ZR4FNo3V789+MwghdsXv4JRCCxFcr+jrq1gVCEOb3fLBbiQFLoTP89uJOAgZZbvs6KFQEQxLpOHxuWUeBTaN1e/PfjMIIXbF7+CUQgsRXK/o66tYFQhDm93ywW4kBS6Ez/PbiTgIGWW77OihUBEMS6Th8bllHgU2jdXvz34zCCF2xe/glEILEVyv6OurWBUIQ5vd8sFuJAUuhM/z24k4CBllu+zooVARDEuk4fG5ZR4FNo3V789+MwghdsXv4JRCCxFcr+jrq1gVCEOb3fLBbiQFLoTP89uJOAgZZbvs6KFQEQxLpOHxuWUeBTaN1e/PfjMIIXbF7+CUQgsRXK/o66tYFQhDm93ywW4kBS6Ez/PbiTgIGWW77OihUBEMS6Th8bllHgU2jdXvz34zCCF2xe/glEILEVyv6OurWBUIQ5vd8sFuJAUuhM/z24k4CBllu+zooVARDEuk4fG5ZR4FNo3V789+MwghdsXv4JRCCxFcr+jrq1gVCEOb3fLBbiQFLoTP89uJOAgZZbvs6KFQEQxLpOHxuWUeBTaN1e/PfjMIIXbF7+CUQgsRXK/o66tYFQhDm93ywW4kBS6Ez/PbiTgIGWW77OihUBEMS6Th8bllHgU2jdXvz34zCCF2xe/glEILEVyv6OurWBUIQ5vd8sFuJAUuhM/z24k4CBllu+zooVARDEuk4fG5ZR4FNo3V789+Mwgh'
        notificationAudio.volume = 0.5
    }
}

/**
 * 请求浏览器通知权限
 * @returns {Promise<string>} 权限状态: 'granted', 'denied', 'default'
 */
export async function requestNotificationPermission() {
    if (!('Notification' in window)) {
        console.warn('[消息提醒] 浏览器不支持通知API')
        return 'unsupported'
    }

    if (Notification.permission === 'granted') {
        return 'granted'
    }

    if (Notification.permission !== 'denied') {
        const permission = await Notification.requestPermission()
        return permission
    }

    return Notification.permission
}

/**
 * 显示浏览器通知
 * @param {Object} options - 通知选项
 * @param {string} options.title - 通知标题
 * @param {string} options.body - 通知内容
 * @param {string} options.icon - 通知图标URL
 * @param {string} options.tag - 通知标签(用于替换相同标签的通知)
 * @returns {Notification|null} 通知对象
 */
export function showBrowserNotification({ title, body, icon, tag }) {
    if (!('Notification' in window)) {
        console.warn('[消息提醒] 浏览器不支持通知API')
        return null
    }

    if (Notification.permission !== 'granted') {
        console.warn('[消息提醒] 未授予通知权限')
        return null
    }

    try {
        const notification = new Notification(title, {
            body,
            icon: icon || '/favicon.ico',
            tag: tag || 'im-message',
            requireInteraction: false,
            silent: false
        })

        // 点击通知时聚焦窗口
        notification.onclick = () => {
            window.focus()
            notification.close()
        }

        // 3秒后自动关闭
        setTimeout(() => {
            notification.close()
        }, 3000)

        return notification
    } catch (error) {
        console.error('[消息提醒] 显示通知失败:', error)
        return null
    }
}

/**
 * 播放提示音
 * @param {number} volume - 音量 (0-1)
 */
export function playNotificationSound(volume = 0.5) {
    try {
        initNotificationSound()
        if (notificationAudio) {
            notificationAudio.volume = Math.max(0, Math.min(1, volume))
            notificationAudio.currentTime = 0
            notificationAudio.play().catch(err => {
                console.warn('[消息提醒] 播放提示音失败:', err)
            })
        }
    } catch (error) {
        console.error('[消息提醒] 播放提示音异常:', error)
    }
}

/**
 * 闪烁标题栏
 * @param {string} message - 要显示的消息
 * @param {number} count - 闪烁次数,默认5次
 * @param {number} interval - 闪烁间隔(毫秒),默认1000ms
 */
export function flashTitle(message = '新消息', count = 5, interval = 1000) {
    if (isTitleFlashing) {
        stopTitleFlash()
    }

    isTitleFlashing = true
    originalTitle = document.title
    let currentCount = 0
    let isOriginal = true

    titleFlashTimer = setInterval(() => {
        if (currentCount >= count) {
            stopTitleFlash()
            return
        }

        document.title = isOriginal ? `【${message}】${originalTitle}` : originalTitle
        isOriginal = !isOriginal

        if (!isOriginal) {
            currentCount++
        }
    }, interval)
}

/**
 * 停止标题栏闪烁
 */
export function stopTitleFlash() {
    if (titleFlashTimer) {
        clearInterval(titleFlashTimer)
        titleFlashTimer = null
    }
    if (isTitleFlashing) {
        document.title = originalTitle
        isTitleFlashing = false
    }
}

/**
 * 闪烁favicon(网站图标)
 * @param {string} color - 闪烁颜色,默认红色
 */
export function flashFavicon(color = '#ff4d4f') {
    try {
        const canvas = document.createElement('canvas')
        canvas.width = 32
        canvas.height = 32
        const ctx = canvas.getContext('2d')

        // 绘制红色圆点
        ctx.fillStyle = color
        ctx.beginPath()
        ctx.arc(16, 16, 8, 0, 2 * Math.PI)
        ctx.fill()

        // 更新favicon
        const link = document.querySelector("link[rel*='icon']") || document.createElement('link')
        link.type = 'image/x-icon'
        link.rel = 'shortcut icon'
        link.href = canvas.toDataURL()
        document.getElementsByTagName('head')[0].appendChild(link)
    } catch (error) {
        console.error('[消息提醒] 闪烁favicon失败:', error)
    }
}

/**
 * 恢复原始favicon
 */
export function restoreFavicon() {
    try {
        const link = document.querySelector("link[rel*='icon']")
        if (link) {
            link.href = '/favicon.ico'
        }
    } catch (error) {
        console.error('[消息提醒] 恢复favicon失败:', error)
    }
}
import {  } from '../utils/storage'

import store from '@/store'

/**
 * 综合消息提醒
 * @param {Object} options - 提醒选项
 */
export function showMessageNotification(options = {}) {
    const settings = store.state.im.settings.notifications

    // 如果全局通知已关，则直接返回
    if (!settings.enabled) {return}

    const {
        title = '新消息',
        body = '',
        icon = '',
        sound = settings.sound, // 优先使用 settings 中的音效设置
        notification = true,
        titleFlash = true,
        faviconFlash = false
    } = options

    // 检查是否在当前窗口(如果窗口已聚焦,可能不需要提醒)
    const isWindowFocused = document.hasFocus()

    // 播放提示音
    if (sound) {
        console.log('[消息提醒] 尝试播放提示音')
        playNotificationSound()
    }

    // 显示浏览器通知(仅在窗口未聚焦时)
    if (notification && !isWindowFocused) {
        console.log('[消息提醒] 显示浏览器通知:', title)
        showBrowserNotification({ title, body, icon })
    }

    // 闪烁标题栏(仅在窗口未聚焦时)
    if (titleFlash && !isWindowFocused) {
        flashTitle(title)
    }

    // 闪烁favicon(可选)
    if (faviconFlash && !isWindowFocused) {
        flashFavicon()
    }
}

/**
 * 判断是否应该提醒
 * @param {Object} message - 消息对象
 * @param {Object} currentUser - 当前用户
 * @param {Object} session - 当前会话
 * @returns {boolean} 是否应该提醒
 */
export function shouldNotify(message, currentUser, session) {
    // 自己发送的消息不提醒
    if (message.senderId === currentUser?.id) {
        return false
    }

    // 会话设置了免打扰
    if (session?.isMuted) {
        return false
    }

    // 检查免打扰时段(可从设置中读取)
    const now = new Date()
    const hour = now.getHours()
    const { start: doNotDisturbStart, end: doNotDisturbEnd } = getDoNotDisturb()

    if (doNotDisturbStart < doNotDisturbEnd) {
        // 正常时段,如22:00-8:00
        if (hour >= doNotDisturbStart || hour < doNotDisturbEnd) {
            return false
        }
    } else {
        // 跨天时段,如8:00-22:00(实际是22:00-8:00免打扰)
        if (hour >= doNotDisturbStart && hour < doNotDisturbEnd) {
            return false
        }
    }

    return true
}

// 监听窗口聚焦,停止提醒
window.addEventListener('focus', () => {
    stopTitleFlash()
    restoreFavicon()
})

export default {
    requestNotificationPermission,
    showBrowserNotification,
    playNotificationSound,
    flashTitle,
    stopTitleFlash,
    flashFavicon,
    restoreFavicon,
    showMessageNotification,
    shouldNotify
}
