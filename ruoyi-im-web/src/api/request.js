/**
 * Axios 基础配置
 * 用于统一管理 HTTP 请求
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '', // 使用环境变量或空字符串
  timeout: 15000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从 localStorage 获取 token
    const token = localStorage.getItem('im_token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }

    // 从 localStorage 获取用户信息并发送 userId
    const userInfo = localStorage.getItem('im_user_info')
    if (userInfo) {
      try {
        const user = JSON.parse(userInfo)
        // 调试日志
        console.log('请求拦截器 - 用户信息:', user)
        if (user.id) {
          config.headers['userId'] = String(user.id) // 确保是字符串类型
          console.log('请求拦截器 - 设置 userId header:', user.id, 'URL:', config.url)
        } else {
          console.warn('请求拦截器 - 用户信息中没有 id 字段:', user)
        }
      } catch (e) {
        console.warn('解析用户信息失败:', e)
      }
    } else {
      console.warn('请求拦截器 - localStorage 中没有 im_user_info')
    }

    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data

    // 如果返回的状态码不是 200，则判断为错误
    if (res.code !== undefined && res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')

      // 401: 未授权
      if (res.code === 401) {
        // 清除 token 并跳转登录页
        localStorage.removeItem('im_token')
        window.location.href = '/login'
      }

      return Promise.reject(new Error(res.msg || '请求失败'))
    }

    return res
  },
  error => {
    console.error('响应错误:', error)

    if (error.response) {
      const { status, data } = error.response

      // 打印更详细的错误信息
      console.log('错误详情:', data)

      switch (status) {
        case 400:
          ElMessage.error(data?.msg || '请求参数错误')
          break
        case 401:
          ElMessage.error('未授权，请重新登录')
          localStorage.removeItem('im_token')
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error('拒绝访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(`请求失败: ${status}`)
      }
    } else if (error.request) {
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      ElMessage.error(error.message || '请求失败')
    }

    return Promise.reject(error)
  }
)

export default service
