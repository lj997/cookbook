import axios from 'axios'

const instance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

instance.interceptors.request.use(
  config => {
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

instance.interceptors.response.use(
  response => {
    const data = response.data
    if (data.success) {
      return data
    } else {
      const error = new Error(data.message || '请求失败')
      error.data = data
      return Promise.reject(error)
    }
  },
  error => {
    console.error('Response error:', error)
    if (error.response) {
      switch (error.response.status) {
        case 404:
          error.message = '请求的资源不存在'
          break
        case 500:
          error.message = '服务器内部错误'
          break
        default:
          error.message = error.response.data?.message || '请求失败'
      }
    } else if (error.request) {
      error.message = '网络连接失败，请检查网络'
    }
    return Promise.reject(error)
  }
)

export default instance
