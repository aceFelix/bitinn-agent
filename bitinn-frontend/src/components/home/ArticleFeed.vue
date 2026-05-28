<!--
  文章 Feed 流
  首页核心内容区，支持推荐/最新排序、无限滚动加载、OSS 签名 URL 转换
  @author aceFelix
-->
<script setup>
import { ref, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { cancellableRequest } from '@/utils/apiClient'
import { getSignUrls, normalizeCoverUrl, isOssUrl } from '@/utils/ossSign'
import request from '@/utils/request'
import defaultAvatar from '@/assets/user-avatar1.jpg'
import ArticleCard from '@/components/common/ArticleCard.vue'
import ArticleCardSkeleton from '@/components/common/ArticleCardSkeleton.vue'

const router = useRouter()

const articles = ref([])
const loading = ref(true)
const currentSort = ref('recommend')
const pageNum = ref(1)
const pageSize = 10
const hasMore = ref(true)
const sentinelRef = ref(null)
let observer = null

let debounceTimer = null

const typeColorMap = {
  '技术文章': '#F97316',
  '学习笔记': '#10B981',
  '项目分享': '#8B5CF6',
  '问题讨论': '#EF4444',
  '经验分享': '#3B82F6',
  '教程指南': '#EC4899'
}

const sortOptions = [
  { value: 'recommend', label: '推荐' },
  { value: 'latest', label: '最新' },
  { value: 'hot', label: '热门' }
]

function formatTime(timeStr) {
  if (!timeStr) return ''
  let date
  if (typeof timeStr === 'string') {
    const cleaned = timeStr.replace(/\.\d+$/, '').replace(/ /, 'T')
    date = new Date(cleaned + (cleaned.includes('T') && !cleaned.includes('Z') ? '' : ''))
  } else {
    date = new Date(timeStr)
  }
  if (isNaN(date.getTime())) return ''
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  if (diff < 0) return '刚刚'
  const seconds = Math.floor(diff / 1000)
  if (seconds < 60) return `${seconds}秒前`
  const minutes = Math.floor(seconds / 60)
  if (minutes < 60) return `${minutes}分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

const fetchArticles = async (reset = false) => {
  const prevArticles = reset ? [...articles.value] : []
  if (reset) {
    pageNum.value = 1
    hasMore.value = true
  }
  loading.value = true
  try {
    const result = await cancellableRequest('feed-' + currentSort.value, {
      url: '/article/feed',
      method: 'GET',
      params: { sortType: currentSort.value, pageNum: pageNum.value, pageSize },
    })

    if (result.code === 200) {
      const pageData = result.data || {}
      const newArticles = (pageData.items || []).map(item => ({
        id: item.id,
        title: item.title,
        excerpt: item.excerpt || (item.content ? item.content.replace(/<[^>]+>/g, '').substring(0, 100) + '...' : ''),
        author: item.authorName || '匿名用户',
        avatar: item.authorAvatar || defaultAvatar,
        type: item.categoryName || '',
        typeColor: typeColorMap[item.categoryName] || '#F97316',
        tags: (item.tags || []).map(t => typeof t === 'string' ? t : t.tagName).filter(Boolean),
        likes: item.likeCount || 0,
        comments: item.commentCount || 0,
        favorites: item.favoriteCount || 0,
        shares: item.shareCount || 0,
        readTime: `${Math.max(1, Math.ceil((item.content?.length || 0) / 500))} min`,
        publishedAt: formatTime(item.createTime),
        cover: normalizeCoverUrl(item.coverImg),
        rawCover: item.coverImg,
        isLiked: false,
        isFavorited: false
      }))

      const coverUrls = newArticles.map(a => a.rawCover).filter(Boolean)

      // 并行获取封面签名和交互状态，减少串行等待
      const [signResult, statusResult] = await Promise.all([
        coverUrls.length > 0 ? getSignUrls(coverUrls).catch(() => ({})) : Promise.resolve({}),
        request.post('/interaction/batch-status', newArticles.map(a => a.id)).catch(() => ({ data: {} }))
      ])

      if (coverUrls.length > 0) {
        for (const article of newArticles) {
          if (article.rawCover && signResult[article.rawCover]) {
            article.cover = signResult[article.rawCover]
          }
        }
      }

      if (statusResult.code === 200 && statusResult.data) {
        for (const article of newArticles) {
          const s = statusResult.data[String(article.id)]
          if (s) {
            article.isLiked = s.liked
            article.isFavorited = s.favorited
          }
        }
      }

      if (reset && pageNum.value === 1) {
        articles.value = newArticles
      } else {
        articles.value.push(...newArticles)
      }
      hasMore.value = newArticles.length >= pageSize
      await nextTick()
      setupObserver()
    }
  } catch (error) {
    if (axios.isCancel(error)) {
      return
    }
    console.error('获取文章列表失败:', error)
    if (reset && prevArticles.length > 0) {
      articles.value = prevArticles
    }
  } finally {
    loading.value = false
  }
}

const switchSort = (sortValue) => {
  if (currentSort.value === sortValue) return
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    currentSort.value = sortValue
  }, 300)
}

const loadMore = () => {
  if (hasMore.value && !loading.value) {
    pageNum.value++
    fetchArticles()
  }
}

const goToDetail = (id) => {
  router.push(`/article/${id}`)
}

const setupObserver = () => {
  if (observer) observer.disconnect()
  if (!hasMore.value || !sentinelRef.value) return
  
  observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting && hasMore.value && !loading.value) {
      loadMore()
    }
  }, { rootMargin: '200px' })
  
  observer.observe(sentinelRef.value)
}

watch(currentSort, () => {
  fetchArticles(true)
})

onMounted(() => {
  fetchArticles()
})

onUnmounted(() => {
  if (observer) observer.disconnect()
  clearTimeout(debounceTimer)
})
</script>

<template>
  <section class="content-feed">
    <div class="feed-header">
      <div class="sort-tabs">
        <button 
          v-for="option in sortOptions"
          :key="option.value"
          :class="['sort-tab', { active: currentSort === option.value }]"
          @click="switchSort(option.value)"
        >
          {{ option.label }}
        </button>
      </div>
    </div>

    <div class="articles-list">
      <template v-if="loading && articles.length === 0">
        <ArticleCardSkeleton v-for="i in 3" :key="'skel-' + i" rgb-color="249, 115, 22" />
      </template>
      
      <template v-else-if="articles.length > 0">
        <ArticleCard
          v-for="article in articles"
          :key="article.id"
          :article="article"
          :default-avatar="defaultAvatar"
          @click="goToDetail"
        />

        <div ref="sentinelRef" class="sentinel"></div>
      </template>

      <div v-else-if="!loading && articles.length === 0" class="empty-state">
        <p>暂无文章</p>
        <button class="btn-create" @click="router.push('/edit')">发布第一篇文章</button>
      </div>
    </div>

    <div v-if="loading && articles.length > 0" class="load-more">
      <div class="loading-spinner small"></div>
      <span>加载中...</span>
    </div>

    <div v-if="!hasMore && articles.length > 0" class="no-more">
      <span>— 已经到底了 —</span>
    </div>
  </section>
</template>

<style lang="scss">
.content-feed {
  min-width: 0;
  height: 100%;
  overflow-y: auto;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
    transform: translateY(-2px);
  }

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(249, 115, 22, 0.3);
    border-radius: 4px;

    &:hover {
      background: #F97316;
    }
  }
}

.feed-header {
  border-radius: 12px;
  padding: 4px;
  margin-bottom: 16px;
}
  
.sort-tabs {
  display: flex;
  gap: 4px;
}

.sort-tab {
  flex: 1;
  padding: 10px 16px;
  background: transparent;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
    transform: translateY(-2px);
  }

  &.active {
    font-weight: 600;
    box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
    transform: translateY(-2px);
    color: #F97316;
  }
}

.articles-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sentinel {
  height: 1px;
  width: 100%;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 48px 0;
  color: #999;
  font-size: 14px;

  .btn-create {
    padding: 10px 24px;
    border: none;
    border-radius: 8px;
    background: #F97316;
    color: white;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      box-shadow: 0 6px 20px rgba(249, 115, 22, 0.35);
      transform: translateY(-1px);
    }
  }
}

.load-more {
  margin-top: 24px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #888;
  font-size: 13px;
}

.no-more {
  margin-top: 24px;
  text-align: center;
  color: #555;
  font-size: 13px;
  letter-spacing: 1px;
}

.loading-spinner {
  width: 28px;
  height: 28px;
  border: 3px solid rgba(249, 115, 22, 0.15);
  border-top-color: #F97316;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;

  &.small {
    width: 18px;
    height: 18px;
    border-width: 2px;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>