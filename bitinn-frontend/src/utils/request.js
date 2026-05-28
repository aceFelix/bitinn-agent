/**
 * Axios 请求封装
 * 统一的 HTTP 客户端，包含请求/响应拦截、Token 注入、401 处理、超时提示等
 * @author aceFelix
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useTokenStore } from '@/stores/token'

const instance = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// ========================
// 请求拦截器
// ========================
instance.interceptors.request.use(
  (config) => {
    const tokenStore = useTokenStore()
    if (tokenStore.token) {
      config.headers.Authorization = tokenStore.token
    }
    return config
  },
  (error) => Promise.reject(error)
)

// ========================
// 响应拦截器
// ========================
instance.interceptors.response.use(
  (response) => {
    const result = response.data
    if (result.code === 200) {
      return result
    }
    ElMessage.error(result.message || '服务异常')
    return Promise.reject(result)
  },
  (error) => {
    const tokenStore = useTokenStore()
    if (axios.isCancel(error)) {
      // 请求被主动取消，不提示
      return Promise.reject(error)
    }
    if (error.response?.status === 401) {
      tokenStore.removeToken()
      ElMessage.error('请先登录')
      router.push('/login')
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请检查网络连接')
    } else {
      ElMessage.error(error.response?.data?.message || '服务异常')
    }
    return Promise.reject(error)
  }
)

export default instance
