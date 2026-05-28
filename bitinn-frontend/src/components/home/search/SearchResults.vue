<!--
  搜索结果页
  展示文章搜索结果，支持关键词搜索、分页和 OSS 签名 URL 转换
  @author aceFelix
-->
<script setup>
import { ref, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { cancellableRequest } from '@/utils/apiClient'
import { getSignUrls, normalizeCoverUrl } from '@/utils/ossSign'
import request from '@/utils/request'
import defaultAvatar from '@/assets/user-avatar1.jpg'
import ArticleCard from '@/components/common/ArticleCard.vue'
import ArticleCardSkeleton from '@/components/common/ArticleCardSkeleton.vue'

const router = useRouter()

const props = defineProps({
  keyword: { type: String, default: '' }
})

const emit = defineEmits(['back'])

const searchKeyword = ref(props.keyword || '')
const articles = ref([])
const loading = ref(false)
const currentSort = ref('relevant')
const pageNum = ref(1)
const pageSize = 10
const hasMore = ref(true)
const total = ref(0)
const sentinelRef = ref(null)
let observer = null
let debounceTimer = null

const sortOptions = [
  { value: 'relevant', label: '相关度' },
  { value: 'latest', label: '最新' },
  { value: 'hot', label: '热门' }
]

const typeColorMap = {
  '技术文章': '#F97316',
  '学习笔记': '#10B981',
  '项目分享': '#8B5CF6',
  '问题讨论': '#EF4444',
  '经验分享': '#3B82F6',
  '教程指南': '#EC4899'
}

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

const fetchResults = async (reset = false) => {
  const q = searchKeyword.value.trim()
  if (!q) return
  if (reset) {
    pageNum.value = 1
    articles.value = []
    hasMore.value = true
  }
  loading.value = true
  try {
    const result = await cancellableRequest('search-results', {
      url: '/search',
      method: 'GET',
      params: {
        keyword: q,
        pageNum: pageNum.value,
        pageSize,
        sortType: currentSort.value
      }
    })
    if (result.code === 200) {
      const data = result.data || {}
      total.value = data.total || 0
      const newArticles = (data.items || []).map(item => ({
        id: item.id,
        title: item.title || '',
        excerpt: item.excerpt || '',
        contentHighlights: item.contentHighlights || [],
        author: item.authorName || '匿名用户',
        avatar: item.authorAvatar || defaultAvatar,
        type: item.categoryName || '',
        typeColor: typeColorMap[item.categoryName] || '#F97316',
        tags: item.tags || [],
        likes: item.likeCount || 0,
        comments: item.commentCount || 0,
        favorites: item.favoriteCount || 0,
        shares: item.shareCount || 0,
        readTime: `${Math.max(1, Math.ceil(((item.contentHighlights || []).join('').length || 100) / 500))} min`,
        publishedAt: formatTime(item.createTime),
        cover: normalizeCoverUrl(item.coverImg),
        rawCover: item.coverImg,
        isLiked: false,
        isFavorited: false,
        score: item.score
      }))
      const coverUrls = newArticles.map(a => a.rawCover).filter(Boolean)
      if (coverUrls.length > 0) {
        const signMap = await getSignUrls(coverUrls)
        for (const article of newArticles) {
          if (article.rawCover && signMap[article.rawCover]) {
            article.cover = signMap[article.rawCover]
          }
        }
      }
      try {
        const tokenStore = (await import('@/stores/token')).useTokenStore()
        if (tokenStore.token) {
          const statusRes = await request.post('/interaction/batch-status', newArticles.map(a => a.id))
          if (statusRes.code === 200 && statusRes.data) {
            for (const article of newArticles) {
              const s = statusRes.data[String(article.id)]
              if (s) {
                article.isLiked = s.liked
                article.isFavorited = s.favorited
              }
            }
          }
        }
      } catch {}
      if (reset) {
        articles.value = newArticles
      } else {
        articles.value.push(...newArticles)
      }
      hasMore.value = newArticles.length >= pageSize
      await nextTick()
      setupObserver()
    }
  } catch (error) {
    if (!error.message?.includes('cancel')) {
      console.error('搜索失败:', error)
    }
  } finally {
    loading.value = false
  }
}

const switchSort = (sortValue) => {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    currentSort.value = sortValue
    fetchResults(true)
  }, 300)
}

const loadMore = () => {
  if (hasMore.value && !loading.value) {
    pageNum.value++
    fetchResults()
  }
}

const goToDetail = (id) => {
  router.push(`/article/${id}`)
}

const goBack = () => {
  emit('back')
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

watch(() => props.keyword, (val) => {
  if (val && val !== searchKeyword.value) {
    searchKeyword.value = val
    fetchResults(true)
  }
})

onMounted(() => {
  searchKeyword.value = props.keyword || ''
  if (searchKeyword.value) {
    fetchResults(true)
  }
})

onUnmounted(() => {
  if (observer) observer.disconnect()
  clearTimeout(debounceTimer)
})
</script>

<template>
  <section class="content-feed">
    <div class="feed-header">
      <div class="result-bar">
        <button class="back-btn" @click="goBack">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="15 18 9 12 15 6"/>
          </svg>
          <span>返回</span>
        </button>
        <div class="result-info">
          搜索 "<span class="highlight-keyword">{{ searchKeyword }}</span>"
          共找到 <span class="result-count">{{ total }}</span> 篇文章
        </div>
      </div>
      <div class="sort-tabs">
        <button
          v-for="opt in sortOptions"
          :key="opt.value"
          :class="['sort-tab', { active: currentSort === opt.value }]"
          @click="switchSort(opt.value)"
        >{{ opt.label }}</button>
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
          :use-html-content="true"
          @click="goToDetail"
        >
          <template #highlights>
            <div v-if="article.contentHighlights.length > 0" class="content-highlights">
              <span v-for="(hl, i) in article.contentHighlights.slice(0, 2)" :key="i" class="highlight-fragment" v-html="'...' + hl + '...'"></span>
            </div>
          </template>
        </ArticleCard>

        <div ref="sentinelRef" class="sentinel"></div>
      </template>

      <div v-else-if="!loading && articles.length === 0" class="empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="empty-icon">
          <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/><line x1="8" y1="11" x2="14" y2="11"/>
        </svg>
        <p>未找到相关文章</p>
        <span>试试其他关键词？</span>
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

<style scoped lang="scss">
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

.result-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: 1px solid transparent;
  border-radius: 6px;
  background: transparent;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;

  svg { width: 16px; height: 16px; }

  &:hover {
    border-color: #F97316;
    color: #F97316;
  }
}

.result-info {
  font-size: 14px;
  color: #666;

  .highlight-keyword {
    color: #F97316;
    font-weight: 600;
  }

  .result-count {
    color: #F97316;
    font-weight: 700;
  }
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

.content-highlights {
  margin-bottom: 16px;

  .highlight-fragment {
    font-size: 13px;
    line-height: 1.5;
    color: #666;

    :deep(.highlight) {
      color: #F97316;
      background: rgba(249, 115, 22, 0.1);
      padding: 0 2px;
      border-radius: 2px;
    }
  }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;

  .empty-icon { width: 64px; height: 64px; margin-bottom: 16px; stroke: #ccc; }
  p { font-size: 18px; margin-bottom: 4px; }
  span { font-size: 14px; }
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
