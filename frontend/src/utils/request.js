import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    
    // 根据后端返回的code判断
    if (res.code === 1) {
      return res
    } else {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
  },
  error => {
    console.error('响应错误:', error)
    
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        ElMessage.error('请先登录')
        const userStore = useUserStore()
        userStore.logout()
        window.location.href = '/login'
      } else if (status === 403) {
        ElMessage.error('没有权限')
      } else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      } else {
        ElMessage.error(error.response.data?.msg || '服务器错误')
      }
    } else {
      ElMessage.error('网络连接失败')
    }
    
    return Promise.reject(error)
  }
)

export default request

