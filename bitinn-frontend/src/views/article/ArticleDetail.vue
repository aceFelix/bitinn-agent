<!--
  文章详情页面
  三栏布局：左侧文章目录、中间文章内容、右侧作者信息和操作
  支持 OSS 签名 URL 自动转换
  @author aceFelix
-->
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ArticleDetailSidebar from '@/components/article/ArticleDetailSidebar.vue'
import ArticleDetailContent from '@/components/article/ArticleDetailContent.vue'
import ArticleDetailRight from '@/components/article/ArticleDetailRight.vue'
import UserCard from '@/components/common/UserCard.vue'
import defaultAvatar from '@/assets/user-avatar1.jpg'
import request from '@/utils/request'
import { useTokenStore } from '@/stores/token'
import { userInfoStore } from '@/stores/userInfo'
import { getSignUrl, getSignUrls, normalizeCoverUrl } from '@/utils/ossSign'

const route = useRoute()
const router = useRouter()
const tokenStore = useTokenStore()
const userStore = userInfoStore()

const showUserCard = ref(false)
const loading = ref(true)

const articleId = computed(() => route.params.id)

const isLoggedIn = computed(() => !!tokenStore.token)

const userAvatar = computed(() => {
  if (!isLoggedIn.value) return ''
  const avatar = userStore.userInfo?.userPic || ''
  return avatar || defaultAvatar
})

const userName = computed(() => {
  if (!isLoggedIn.value) return ''
  return userStore.userInfo?.nickname || userStore.userInfo?.username || '用户'
})

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
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  if (minutes < 60) return `${minutes}分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

function generateToc(htmlContent) {
  if (!htmlContent) return []
  const div = document.createElement('div')
  div.innerHTML = htmlContent
  const headings = div.querySelectorAll('h2, h3, h4')
  const toc = []
  headings.forEach((el, index) => {
    const id = el.id || `toc-${index}`
    el.id = id
    toc.push({
      id,
      text: el.textContent.trim(),
      level: parseInt(el.tagName.charAt(1))
    })
  })
  return toc
}

const article = ref({
  id: null,
  title: '',
  content: '',
  excerpt: '',
  coverImg: '',
  authorName: '',
  authorAvatar: '',
  categoryName: '',
  tags: [],
  createTime: '',
  likeCount: 0,
  commentCount: 0,
  favoriteCount: 0,
  shareCount: 0,
  viewCount: 0
})

const author = ref({
  name: '',
  avatar: '',
  bio: '',
  articleCount: 0,
  followerCount: 0
})

const relatedArticles = ref([])
const tocList = ref([])

onMounted(async () => {
  try {
    const [detailRes, feedRes] = await Promise.all([
      request.get(`/article/${articleId.value}`),
      request.get('/article/feed', { params: { sortType: 'recommend', pageNum: 1, pageSize: 5 } })
    ])

    if (detailRes.code === 200 && detailRes.data) {
      const data = detailRes.data
      const rawCoverImg = data.coverImg

      let signedCoverUrl = normalizeCoverUrl(rawCoverImg)
      if (rawCoverImg) {
        signedCoverUrl = await getSignUrl(rawCoverImg)
      }

      article.value = {
        id: data.id || null,
        title: data.title || '',
        content: data.content || '',
        excerpt: data.excerpt || (data.content ? data.content.replace(/<[^>]+>/g, '').substring(0, 120) + '...' : ''),
        coverImg: signedCoverUrl,
        authorName: data.authorName || '匿名用户',
        authorAvatar: data.authorAvatar || defaultAvatar,
        categoryName: data.categoryName || '',
        type: data.categoryName || '',
        typeColor: typeColorMap[data.categoryName] || '#F97316',
        tags: (data.tags || []).map(t => typeof t === 'string' ? t : t.tagName).filter(Boolean),
        createTime: data.createTime || '',
        likeCount: data.likeCount || 0,
        commentCount: data.commentCount || 0,
        favoriteCount: data.favoriteCount || 0,
        shareCount: data.shareCount || 0,
        viewCount: data.viewCount || 0,
        readTime: `${Math.max(1, Math.ceil((data.content?.length || 0) / 500))} min`,
        publishedAt: formatTime(data.createTime)
      }

      author.value = {
        id: data.createUser || null,
        name: data.authorName || '匿名用户',
        avatar: data.authorAvatar || defaultAvatar,
        bio: '',
        articleCount: 0,
        followerCount: 0
      }

      tocList.value = generateToc(article.value.content)
    }

    if (feedRes.code === 200 && feedRes.data) {
      const items = (feedRes.data.items || [])
        .filter(item => item.id !== Number(articleId.value))
        .slice(0, 4)

      const coverUrls = items.map(i => i.coverImg).filter(Boolean)
      const signMap = coverUrls.length > 0 ? await getSignUrls(coverUrls) : {}

      relatedArticles.value = items.map(item => ({
        id: item.id,
        title: item.title,
        authorName: item.authorName || '匿名用户',
        likes: item.likeCount || 0,
        cover: signMap[item.coverImg] || normalizeCoverUrl(item.coverImg)
      }))
    }
  } catch (error) {
    console.error('获取文章详情失败:', error)
  } finally {
    loading.value = false
  }
})

const goBack = () => {
  router.back()
}

const goHome = () => {
  router.push('/')
}

const toggleUserCard = () => {
  showUserCard.value = !showUserCard.value
}

const handleTocClick = (item) => {
  const el = document.getElementById(item.id)
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}
</script>

<template>
  <div class="detail-container">
    <header class="navbar">
      <div class="nav-brand">
        <div class="logo" @click="goHome">
          <span class="bracket">&lt;</span>
          <span class="brand-name">BitInn</span>
          <span class="bracket">/&gt;</span>
        </div>
      </div>

      <div class="nav-actions">
        <button class="btn-back" @click="goBack" title="返回">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          <span>返回</span>
        </button>
        <button class="btn-write" @click="router.push('/article/edit')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 5v14M5 12h14"/>
          </svg>
          <span>写文章</span>
        </button>
        <button v-if="!isLoggedIn" class="btn-login" @click="router.push('/login')">登录 / 注册</button>
        <div v-else class="user-avatar-wrapper" :title="userName" @click="toggleUserCard">
          <img :src="userAvatar" :alt="userName" class="user-avatar" />
        </div>
      </div>
    </header>

    <UserCard :visible="showUserCard" @close="showUserCard = false" />

    <main v-if="!loading" class="main-content">
      <ArticleDetailSidebar
        :article-id="Number(articleId)"
        :likes="article.likeCount"
        :favorites="article.favoriteCount"
        :comments="article.commentCount"
        :shares="article.shareCount || 0"
        :toc-list="tocList"
        @toc-click="handleTocClick"
      />

      <ArticleDetailContent :article="article" />

      <ArticleDetailRight
        :author="author"
        :author-id="author.id"
        :related-articles="relatedArticles"
      />
    </main>

    <div v-else class="main-content">
      <div class="loading-wrapper">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.detail-container {
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  z-index: 1000;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
  }
}

.nav-brand {
  .logo {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 24px;
    font-weight: 700;
    cursor: pointer;

    .bracket {
      font-family: 'JetBrains Mono', 'Fira Code', monospace;
      color: #333;
    }

    .brand-name {
      color: #F97316;
      text-shadow: 0 0 10px rgba(249, 115, 22, 0.5);
    }
  }
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 12px;

  .btn-back {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    border: 1px solid #F97316;
    border-radius: 6px;
    background: transparent;

    svg {
      width: 16px;
      height: 16px;
    }

    &:hover {
      box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
      transform: translateY(-2px);
      background: #F97316;
      color: white;

      svg {
        stroke: white;
      }
    }
  }

  .btn-write {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    border: 1px solid #F97316;
    background: transparent;
    color: #1a1a1a;

    svg {
      width: 16px;
      height: 16px;
      stroke: currentColor;
    }

    &:hover {
      box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
      transform: translateY(-2px);
      background: #F97316;
      color: white;

      svg {
        stroke: white;
      }
    }
  }

  .btn-login {
    padding: 8px 16px;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    border: 1px solid #F97316;
    background: transparent;
    color: #F97316;

    &:hover {
      box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
      transform: translateY(-2px);
      background: #F97316;
      color: white;
    }
  }

  .user-avatar-wrapper {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    border-radius: 50%;
    overflow: hidden;
    border: 2px solid transparent;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      box-shadow: 0 4px 12px rgba(249, 115, 22, 0.4);
      transform: translateY(-2px);
      border-color: #F97316;
    }

    .user-avatar {
      width: 100%;
      height: 100%;
      object-fit: cover;
      border-radius: 50%;
    }
  }
}

.main-content {
  display: grid;
  grid-template-columns: 200px 1fr 300px;
  gap: 20px;
  max-width: 1400px;
  margin: 0 auto;
  padding: 84px 24px 24px;
  height: 100vh;
  overflow: hidden;
  box-sizing: border-box;
}

.loading-wrapper {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: #999;
  font-size: 14px;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #f0f0f0;
  border-top-color: #F97316;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 180px 1fr 270px;
    gap: 16px;
  }
}

@media (max-width: 992px) {
  .main-content {
    grid-template-columns: 1fr;
    height: auto;
    overflow: visible;
  }

  .nav-actions .btn-write span {
    display: none;
  }
}

@media (max-width: 768px) {
  .navbar {
    padding: 0 16px;
  }

  .nav-actions .btn-login {
    padding: 7px 10px;
    font-size: 11.5px;
  }
}
</style>