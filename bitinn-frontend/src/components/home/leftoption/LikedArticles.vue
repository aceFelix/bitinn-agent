<!--
  点赞的文章列表
  展示当前用户点赞的文章，支持分页和缓存
  @author aceFelix
-->
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { cachedRequest } from '@/utils/apiClient'
import { getSignUrls, normalizeCoverUrl } from '@/utils/ossSign'
import request from '@/utils/request'
import defaultAvatar from '@/assets/user-avatar1.jpg'
import ArticleCard from '@/components/common/ArticleCard.vue'
import ArticleCardSkeleton from '@/components/common/ArticleCardSkeleton.vue'

const router = useRouter()
const articles = ref([])
const loading = ref(true)

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

const fetchLikedArticles = async () => {
  loading.value = true
  try {
    const result = await cachedRequest('liked-articles', {
      url: '/interaction/liked-articles',
      method: 'GET',
    }, 3 * 60 * 1000)
    if (result.code === 200) {
      const items = result.data || []
      const newArticles = items.map(item => ({
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
        isLiked: true,
        isFavorited: false
      }))

      const coverUrls = newArticles.map(a => a.rawCover).filter(Boolean)

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
            article.isFavorited = s.favorited
          }
        }
      }

      articles.value = newArticles
    }
  } catch (error) {
    if (!error.message?.includes('cancel')) {
      console.error('获取点赞文章失败:', error)
    }
  } finally {
    loading.value = false
  }
}

const goToDetail = (id) => {
  router.push(`/article/${id}`)
}

onMounted(() => {
  fetchLikedArticles()
})
</script>

<template>
  <section class="content-feed">
    <div class="feed-header">
      <div class="section-title">
        <svg viewBox="0 0 24 24" fill="none" stroke="#EF4444" stroke-width="2" width="20" height="20">
          <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
        </svg>
        <span>点赞的文章</span>
      </div>
    </div>

    <div class="articles-list">
      <template v-if="loading && articles.length === 0">
        <ArticleCardSkeleton v-for="i in 3" :key="'skel-' + i" rgb-color="239, 68, 68" />
      </template>

      <template v-else-if="articles.length > 0">
        <ArticleCard
          v-for="article in articles"
          :key="article.id"
          :article="article"
          :default-avatar="defaultAvatar"
          @click="goToDetail"
        />
      </template>

      <div v-else class="empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5" width="48" height="48">
          <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
        </svg>
        <p>还没有点赞过文章</p>
        <span class="empty-hint">浏览文章时点击❤️即可点赞</span>
      </div>
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
    box-shadow: 0 8px 24px rgba(239, 68, 68, 0.25);
    transform: translateY(-2px);
  }

  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-track { background: transparent; }
  &::-webkit-scrollbar-thumb { background: rgba(239, 68, 68, 0.3); border-radius: 4px; &:hover { background: #EF4444; } }
}

.feed-header { margin-bottom: 16px; }

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
}

.articles-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 48px 0;
  color: #999;
  font-size: 14px;

  .empty-hint { font-size: 12px; color: #bbb; }
}
</style>
