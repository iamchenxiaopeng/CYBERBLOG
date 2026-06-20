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
    const msg = err.response?.data?.message || '网络错误'
    const status = err.response?.status
    if (status === 401 || status === 403) {
      // 401 = 未认证；403 = 权限不足（可能是 token 过期）
      // 如果是写操作（POST/PUT/DELETE）遇到 403，多半是 token 过期
      const method = err.config?.method?.toLowerCase()
      if (status === 401 || (status === 403 && ['post', 'put', 'delete', 'patch'].includes(method || ''))) {
        const userStore = useUserStore()
        userStore.logout()
        // 避免在登录页重复跳转
        if (!window.location.pathname.startsWith('/login')) {
          window.location.href = '/login?expired=1'
        }
      }
    }
    return Promise.reject(new Error(msg))
  }
)

export default request
