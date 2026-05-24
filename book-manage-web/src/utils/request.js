import axios from 'axios'
import { ElMessage, ElNotification } from 'element-plus'

const service = axios.create({
  baseURL: '/api',
  timeout: 10000
})

service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
      return response
    }
    const res = response.data
    if (res.code !== 200) {
      if (res.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        ElNotification({
          title: '登录过期',
          message: '登录已过期，请重新登录',
          type: 'warning',
          duration: 3000
        })
        setTimeout(() => {
          window.location.href = '/login'
        }, 1500)
      } else {
        ElMessage.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    console.error('Response error:', error)
    if (error.response) {
      const { status, data } = error.response
      const message = data?.message || ''
      if (status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        ElNotification({
          title: '登录过期',
          message: '登录已过期，请重新登录',
          type: 'warning',
          duration: 3000
        })
        setTimeout(() => {
          window.location.href = '/login'
        }, 1500)
      } else if (status >= 500) {
        ElNotification({
          title: '服务器错误',
          message: message || '服务器开小差了，请稍后再试',
          type: 'error',
          duration: 5000
        })
      } else if (status === 403) {
        ElNotification({
          title: '无权限',
          message: message || '您没有权限执行此操作',
          type: 'warning',
          duration: 4000
        })
      } else if (status === 404) {
        ElNotification({
          title: '资源不存在',
          message: message || '请求的资源不存在',
          type: 'warning',
          duration: 4000
        })
      } else {
        ElNotification({
          title: '请求失败',
          message: message || `请求错误 (${status})`,
          type: 'error',
          duration: 4000
        })
      }
    } else if (error.code === 'ECONNABORTED') {
      ElNotification({
        title: '请求超时',
        message: '请求超时，请检查网络后重试',
        type: 'error',
        duration: 5000
      })
    } else {
      ElNotification({
        title: '网络错误',
        message: '网络连接异常，请检查网络设置',
        type: 'error',
        duration: 5000
      })
    }
    return Promise.reject(error)
  }
)

export default service
