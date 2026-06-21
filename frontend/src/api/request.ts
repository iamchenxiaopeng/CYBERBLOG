import axios from 'axios'
import { useUserStore } from '@/stores/user'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use(config => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers['Authorization'] = `Bearer ${userStore.token}`
  }
  return config
})

request.interceptors.response.use(
  res => res.data,
  err => {
    // 详细诊断：按优先级提取错误信息
    let msg = '网络错误'
    const status = err.response?.status
    const serverMsg = err.response?.data?.message

    // 后端返回了结构化错误信息 → 直接使用
    if (serverMsg) {
      msg = serverMsg
    }
    // 有 HTTP 状态码但无消息 → 根据状态码给友好提示
    else if (status) {
      const statusMessages: Record<number, string> = {
        400: '请求参数有误',
        401: '登录已过期，请重新登录',
        403: '没有权限执行此操作',
        404: '请求的资源不存在',
        429: '请求过于频繁，请稍后再试',
        500: '服务器内部错误',
        502: '服务暂时不可用',
        503: '服务正在维护中',
      }
      msg = statusMessages[status] || `服务器错误 (${status})`
    }
    // 完全没收到响应（后端宕机/网络不通）→ 给更具体的提示
    else if (err.request) {
      msg = '无法连接到服务器，请检查网络或稍后重试'
      console.error('[API] 请求无响应:', err.message || err.code)
    }
    // 其他异常（如请求被取消等）
    else {
      console.error('[API] 请求异常:', err.message)
    }

    // 401/403 需要特殊处理（登出 + 跳转）
    if (status === 401 || status === 403) {
      const method = err.config?.method?.toLowerCase()
      if (status === 401 || (status === 403 && ['post', 'put', 'delete', 'patch'].includes(method || ''))) {
        const userStore = useUserStore()
        userStore.logout()
        if (!window.location.pathname.startsWith('/login')) {
          window.location.href = '/login?expired=1'
        }
      }
    }

    return Promise.reject(new Error(msg))
  }
)

export default request
