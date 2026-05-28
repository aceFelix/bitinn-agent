<!--
  我的文章列表
  展示当前用户发布的文章，支持分页
  @author aceFelix
-->
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { getSignUrls, normalizeCoverUrl } from '@/utils/ossSign'
import defaultAvatar from '@/assets/user-avatar1.jpg'
import ArticleCard from '@/components/common/ArticleCard.vue'
import ArticleCardSkeleton from '@/components/common/ArticleCardSkeleton.vue'

const router = useRouter()

const articles = ref([])
const loading = ref(true)
const currentSort = ref('latest')
const sortOptions = [
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

const fetchMyArticles = async () => {
  loading.value = true
  try {
    const result = await request.get('/article/my')
    if (result.code === 200) {
      const pageData = result.data || {}
      const newArticles = (pageData.items || []).map(item => ({
        id: item.id,
        title: item.title,
        excerpt: item.excerpt || (item.content ? item.content.replace(/<[^>]+>/g, '').substring(0, 100) + '...' : ''),
        author: item.authorName || '我',
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

      articles.value = newArticles
    }
  } catch (error) {
    console.error('获取我的文章失败:', error)
  } finally {
    loading.value = false
  }
}

const goToDetail = (id) => {
  router.push(`/article/${id}`)
}

onMounted(() => {
  fetchMyArticles()
})

defineExpose({ refresh: fetchMyArticles })
</script>

<template>
  <section class="content-feed">
    <div class="feed-header">
      <div class="sort-tabs">
        <button 
          v-for="option in sortOptions"
          :key="option.value"
          :class="['sort-tab', { active: currentSort === option.value }]"
          @click="currentSort = option.value"
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
      </template>

      <div v-else class="empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          <polyline points="14 2 14 8 20 8"/>
          <line x1="12" y1="18" x2="12" y2="12"/>
          <line x1="9" y1="15" x2="15" y2="15"/>
        </svg>
        <p>还没有发布过文章</p>
        <router-link to="/create" class="btn-write-first">写第一篇文章</router-link>
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
    box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
    transform: translateY(-2px);
  }

  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-track { background: transparent; }
  &::-webkit-scrollbar-thumb { background: rgba(249, 115, 22, 0.3); border-radius: 4px; &:hover { background: #F97316; } }
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

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 60px 20px;
  color: #888;

  svg {
    width: 64px;
    height: 64px;
    opacity: 0.4;
  }

  p { font-size: 15px; }

  .btn-write-first {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 10px 24px;
    background: #F97316;
    color: white;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 500;
    text-decoration: none;
    transition: all 0.2s;

    &:hover {
      box-shadow: 0 8px 24px rgba(249, 115, 22, 0.35);
      transform: translateY(-2px);
    }
  }
}
</style>
