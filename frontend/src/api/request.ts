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
    if (err.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      window.location.href = '/login'
    }
    return Promise.reject(new Error(msg))
  }
)

export default request
