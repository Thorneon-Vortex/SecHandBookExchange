import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAdminStore } from '@/stores/admin'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const adminStore = useAdminStore()
    if (adminStore.token) {
      config.headers.Authorization = `Bearer ${adminStore.token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 1) {
      return res
    } else {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
  },
  error => {
    if (error.response?.status === 401) {
      ElMessage.error('请先登录')
      const adminStore = useAdminStore()
      adminStore.logout()
      router.push('/login')
    } else if (error.response?.status === 403) {
      ElMessage.error('权限不足')
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request


