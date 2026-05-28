<!--
  文章详情左侧边栏
  展示文章目录（TOC）、互动数据（点赞/收藏/评论/分享），支持目录导航
  @author aceFelix
-->
<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { useTokenStore } from '@/stores/token'

const props = defineProps({
  articleId: { type: Number, required: true },
  likes: { type: Number, default: 0 },
  favorites: { type: Number, default: 0 },
  comments: { type: Number, default: 0 },
  shares: { type: Number, default: 0 },
  shareCount: { type: Number, default: 0 },
  tocList: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['like', 'favorite', 'comment', 'share', 'toc-click'])

const tokenStore = useTokenStore()
const isLiked = ref(false)
const isFavorited = ref(false)
const likeCount = ref(props.likes)
const favoriteCount = ref(props.favorites)
const commentCount = ref(props.comments)
const shareCount = ref(props.shareCount || props.shares || 0)
const activeTocId = ref('')

const fetchInteractionStatus = async () => {
  if (!tokenStore.token) return
  try {
    const result = await request.get(`/interaction/article-status/${props.articleId}`)
    if (result.code === 200 && result.data) {
      isLiked.value = result.data.liked
      isFavorited.value = result.data.favorited
      likeCount.value = result.data.likeCount
      favoriteCount.value = result.data.favoriteCount
      commentCount.value = result.data.commentCount
      shareCount.value = result.data.shareCount || 0
    }
  } catch (error) {
    console.error('获取交互状态失败:', error)
  }
}

const toggleLike = async () => {
  if (!tokenStore.token) return
  try {
    const result = await request.post(`/interaction/like/${props.articleId}`)
    if (result.code === 200) {
      isLiked.value = result.data
      likeCount.value += result.data ? 1 : -1
      emit('like', isLiked.value)
    }
  } catch (error) {
    console.error('点赞操作失败:', error)
  }
}

const toggleFavorite = async () => {
  if (!tokenStore.token) return
  try {
    const result = await request.post(`/interaction/favorite/${props.articleId}`)
    if (result.code === 200) {
      isFavorited.value = result.data
      favoriteCount.value += result.data ? 1 : -1
      emit('favorite', isFavorited.value)
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
  }
}

const scrollToComment = () => {
  emit('comment')
}

const handleShare = async () => {
  // 调用后端转发接口
  try {
    const result = await request.post(`/interaction/share/${props.articleId}`)
    if (result.code === 200) {
      shareCount.value++
      alert('转发成功！')
    }
  } catch (error) {
    console.error('转发失败:', error)
  }
  emit('share')
}

const handleTocClick = (item) => {
  activeTocId.value = item.id
  emit('toc-click', item)
}

onMounted(() => {
  fetchInteractionStatus()
})
</script>

<template>
  <aside class="detail-sidebar">
    <div class="action-bar">
      <button
        class="action-btn action-btn-like"
        :class="{ active: isLiked }"
        :title="isLiked ? '取消点赞' : '点赞'"
        @click="toggleLike"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="22" height="22">
          <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
        </svg>
        <span class="action-count">{{ likeCount }}</span>
      </button>

      <button
        class="action-btn action-btn-fav"
        :class="{ active: isFavorited }"
        :title="isFavorited ? '取消收藏' : '收藏'"
        @click="toggleFavorite"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="22" height="22">
          <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
        </svg>
        <span class="action-count">{{ favoriteCount }}</span>
      </button>

      <button
        class="action-btn"
        title="评论"
        @click="scrollToComment"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="22" height="22">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
        <span class="action-count">{{ commentCount }}</span>
      </button>

      <button
        class="action-btn"
        title="分享"
        @click="handleShare"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="22" height="22">
          <path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8"/>
          <polyline points="16 6 12 2 8 6"/>
          <line x1="12" y1="2" x2="12" y2="15"/>
        </svg>
        <span class="action-count">{{ shares }}</span>
      </button>
    </div>

    <div v-if="tocList.length > 0" class="toc-section">
      <h3 class="toc-title">目录</h3>
      <nav class="toc-list">
        <a
          v-for="item in tocList"
          :key="item.id"
          href="#"
          :class="['toc-item', { active: activeTocId === item.id }]"
          :style="{ paddingLeft: (item.level * 12 + 8) + 'px' }"
          @click.prevent="handleTocClick(item)"
        >
          {{ item.text }}
        </a>
      </nav>
    </div>
  </aside>
</template>

<style scoped lang="scss">
.detail-sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: 100%;
  overflow-y: auto;
  border-radius: 10px;
  background: #fff;
  padding: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
    transform: translateY(-2px);
  }

  &::-webkit-scrollbar { display: none; }
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.action-bar {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 18px 14px;
  border-radius: 10px;
  background: #fff;
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    transform: translateY(-2px);
  }
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  padding: 10px 6px;
  border: none;
  border-radius: 10px;
  background: transparent;
  cursor: pointer;
  transition: all 0.2s;
  color: #666;

  svg { transition: all 0.2s; }

  &:hover {
    background: rgba(249, 115, 22, 0.06);
    color: #F97316;
    svg { stroke: #F97316; }
  }

  &.active {
    color: #F97316;
    svg { stroke: #F97316; fill: rgba(249, 115, 22, 0.15); }
  }

  &.action-btn-like.active {
    color: #EF4444;
    svg { stroke: #EF4444; fill: #EF4444; }
  }

  &.action-btn-fav.active {
    color: #EAB308;
    svg { stroke: #EAB308; fill: #EAB308; }
  }
}

.action-count {
  font-size: 11px;
  font-weight: 600;
  line-height: 1;
}

.toc-section {
  padding: 18px 16px;
  border-radius: 10px;
  background: #fff;
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    transform: translateY(-2px);
  }
}

.toc-title {
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 14px;
  color: #1a1a1a;
}

.toc-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.toc-item {
  display: block;
  padding: 6px 8px;
  border-radius: 8px;
  font-size: 13px;
  color: #666;
  text-decoration: none;
  transition: all 0.2s;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;

  &:hover {
    background: rgba(249, 115, 22, 0.06);
    color: #F97316;
  }

  &.active {
    color: #F97316;
    font-weight: 600;
    background: rgba(249, 115, 22, 0.08);
    border-left: 3px solid #F97316;
  }
}
</style>
