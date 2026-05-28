/**
 * 增强版 API 客户端
 * - 自动请求去重（防止用户快速点击重复发送）
 * - 请求缓存（适合 Feed、分类等高频读接口）
 * - 请求取消（切换 Tab/路由时自动取消上一个未完成请求）
 * - 自动重试（可选）
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useTokenStore } from '@/stores/token'

const BASE_URL = '/api'
const TIMEOUT = 15000

// ==============================
// 缓存管理（内存缓存，适合小数据）
// ==============================
const cacheStore = new Map()

const CACHE_TTL = {
  feed: 2 * 60 * 1000,       // Feed 缓存 2 分钟
  category: 10 * 60 * 1000,  // 分类缓存 10 分钟
  default: 5 * 60 * 1000,    // 默认 5 分钟
}

function getCacheKey(url, params) {
  return `${url}::${JSON.stringify(params || {})}`
}

function getCache(key, ttl = CACHE_TTL.default) {
  const entry = cacheStore.get(key)
  if (!entry) return null
  if (Date.now() - entry.timestamp > ttl) {
    cacheStore.delete(key)
    return null
  }
  return entry.data
}

function setCache(key, data) {
  cacheStore.set(key, { data, timestamp: Date.now() })
}

// ==============================
// 请求去重（防止短时间重复请求）
// ==============================
const pendingRequests = new Map()
const PENDING_TTL = 5000 // 5 秒内相同请求视为重复

function getRequestKey(config) {
  return `${config.method}:${config.url}:${JSON.stringify(config.params || config.data || {})}`
}

// ===============================
// 请求取消控制器（跨请求共享）
// ==============================
export const abortControllerStore = {
  controllers: new Map(),

  /** 为某个 key 创建/获取取消控制器 */
  get(key) {
    const existing = this.controllers.get(key)
    if (existing) {
      existing.abort() // 取消上一个
    }
    const controller = new AbortController()
    this.controllers.set(key, controller)
    return controller
  },

  /** 取消指定 key 的请求 */
  cancel(key) {
    const controller = this.controllers.get(key)
    if (controller) {
      controller.abort()
      this.controllers.delete(key)
    }
  },

  /** 取消所有请求 */
  cancelAll() {
    this.controllers.forEach((c) => c.abort())
    this.controllers.clear()
  },
}

// ==============================
// Axios 实例
// ==============================
const apiClient = axios.create({
  baseURL: BASE_URL,
  timeout: TIMEOUT,
})

// 请求拦截
apiClient.interceptors.request.use(
  (config) => {
    const tokenStore = useTokenStore()
    if (tokenStore.token) {
      config.headers.Authorization = tokenStore.token
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截
apiClient.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const tokenStore = useTokenStore()
    if (axios.isCancel(error)) {
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

// ==============================
// 导出增强请求方法
// ==============================

/**
 * 通用请求（带去重）
 */
export function request(config) {
  const key = getRequestKey(config)
  const now = Date.now()
  const pending = pendingRequests.get(key)

  if (pending && now - pending.time < PENDING_TTL) {
    if (axios.isCancel(pending.error)) {
      pendingRequests.delete(key)
    } else if (!pending.settled) {
      return pending.promise
    }
  }

  let settled = false
  const promise = apiClient(config)
    .then((data) => {
      settled = true
      return data
    })
    .catch((err) => {
      pendingRequests.delete(key)
      throw err
    })
    .finally(() => {
      pending.settled = true
    })

  pendingRequests.set(key, { promise, time: now, settled, error: null })
  return promise
}

/**
 * 列表/Feed 请求（带缓存 + 取消）
 * @param {string} cacheKey - 缓存 key
 * @param {object} config - axios config
 * @param {number} cacheTtl - 缓存 TTL(ms)
 */
export function cachedRequest(cacheKey, config, cacheTtl = CACHE_TTL.default) {
  const key = getCacheKey(config.url, config.params)
  const cached = getCache(key, cacheTtl)
  if (cached) {
    return Promise.resolve(cached)
  }
  return request(config).then((data) => {
    setCache(key, data)
    return data
  })
}

/**
 * 带取消的请求（适用于 Tab 切换、搜索等场景）
 * @param {string} reqKey - 请求唯一标识
 * @param {object} config - axios config
 */
export function cancellableRequest(reqKey, config) {
  const controller = abortControllerStore.get(reqKey)
  config.signal = controller.signal
  return request(config)
}

export default apiClient
