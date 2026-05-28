/**
 * OSS 签名 URL 工具
 * 将私有 OSS 图片 URL 转换为带签名的临时访问 URL，支持单张和批量转换
 * 内置内存缓存（55 分钟），避免重复请求签名接口
 * @author aceFelix
 */
import request from '@/utils/request'

const OSS_DOMAIN = import.meta.env.VITE_OSS_DOMAIN || 'your-bucket.oss-cn-hangzhou.aliyuncs.com'
const CACHE_PREFIX = 'oss_sign_'
const CACHE_DURATION = 55 * 60 * 1000

/** 签名 URL 内存缓存（key: 原始 URL 去参数, value: { url, time }） */
const signCache = new Map()

/**
 * 判断是否为 OSS URL
 * @param {string} url
 * @returns {boolean}
 */
function isOssUrl(url) {
  if (!url || typeof url !== 'string') return false
  return url.includes('aliyuncs.com') || url.includes('oss-cn-')
}

/** 取 URL 去掉查询参数后的部分作为缓存键 */
function getCacheKey(url) {
  return url.split('?')[0]
}

/** 尝试从缓存获取签名 URL */
function getCachedSignUrl(url) {
  const key = getCacheKey(url)
  const cached = signCache.get(key)
  if (cached && Date.now() - cached.time < CACHE_DURATION) {
    return cached.url
  }
  return null
}

/** 将签名结果存入缓存 */
function setCachedSignUrl(originalUrl, signedUrl) {
  const key = getCacheKey(originalUrl)
  signCache.set(key, { url: signedUrl, time: Date.now() })
}

/**
 * 获取单个 OSS URL 的签名地址
 * @param {string} url - 原始 OSS URL
 * @returns {string} 签名后的 URL，非 OSS URL 则原样返回
 */
export async function getSignUrl(url) {
  if (!url || typeof url !== 'string') return url
  if (!isOssUrl(url)) return url

  const cached = getCachedSignUrl(url)
  if (cached) return cached

  try {
    const result = await request.get('/oss/sign-url', { params: { url } })
    if (result.code === 200 && result.data) {
      setCachedSignUrl(url, result.data)
      return result.data
    }
  } catch (e) {
    console.warn('[OSS签名] 获取签名URL失败:', url, e)
  }
  return url
}

/**
 * 批量获取 OSS URL 的签名地址
 * @param {string[]} urls - 原始 OSS URL 数组
 * @returns {Object} 原始 URL -> 签名 URL 的映射
 */
export async function getSignUrls(urls) {
  if (!urls || !Array.isArray(urls)) return {}

  const ossUrls = urls.filter(u => u && isOssUrl(u))
  if (ossUrls.length === 0) return {}

  const result = {}
  const uncached = []

  for (const url of ossUrls) {
    const cached = getCachedSignUrl(url)
    if (cached) {
      result[url] = cached
    } else {
      uncached.push(url)
    }
  }

  if (uncached.length > 0) {
    try {
      const res = await request.post('/oss/sign-urls', uncached)
      if (res.code === 200 && res.data) {
        for (const [orig, signed] of Object.entries(res.data)) {
          result[orig] = signed
          setCachedSignUrl(orig, signed)
        }
      }
    } catch (e) {
      console.warn('[OSS签名] 批量获取签名URL失败:', e)
    }
  }

  return result
}

/**
 * 规范化封面 URL，仅返回合法图片地址
 * @param {string} url
 * @returns {string|null}
 */
export function normalizeCoverUrl(url) {
  if (!url || typeof url !== 'string') return null
  url = url.trim()
  if (url.startsWith('http://') || url.startsWith('https://')) {
    if (/\.(jpg|jpeg|png|gif|webp|svg|bmp)(\?|$)/i.test(url)) return url
    if (url.includes('aliyuncs.com') || url.includes('oss-')) return url
    return null
  }
  return null
}

export { isOssUrl }
