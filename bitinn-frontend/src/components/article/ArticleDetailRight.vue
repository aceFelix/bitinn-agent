<!--
  文章详情右侧栏
  展示作者信息、相关推荐和操作按钮（关注/取消关注）
  @author aceFelix
-->
<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { useTokenStore } from '@/stores/token'

const router = useRouter()
const tokenStore = useTokenStore()

const props = defineProps({
  author: {
    type: Object,
    default: () => ({})
  },
  authorId: {
    type: Number,
    default: null
  },
  relatedArticles: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['follow', 'message', 'article-click'])

const isFollowing = ref(false)
const followerCount = ref(props.author.followerCount || 0)

const isLoggedIn = () => !!tokenStore.token

const fetchFollowStatus = async () => {
  if (!isLoggedIn() || !props.authorId) return
  try {
    const result = await request.get(`/interaction/follow/status/${props.authorId}`)
    if (result.code === 200) {
      isFollowing.value = result.data
    }
  } catch (error) {
    console.error('获取关注状态失败:', error)
  }
}

const handleFollow = async () => {
  if (!isLoggedIn() || !props.authorId) return
  try {
    const result = await request.post(`/interaction/follow/${props.authorId}`)
    if (result.code === 200) {
      isFollowing.value = result.data
      followerCount.value += result.data ? 1 : -1
      emit('follow', isFollowing.value)
    }
  } catch (error) {
    console.error('关注操作失败:', error)
  }
}

const handleMessage = () => {
  emit('message')
}

const handleArticleClick = (article) => {
  if (article.id) {
    router.push(`/article/${article.id}`)
  }
}

onMounted(() => {
  fetchFollowStatus()
})

watch(() => props.authorId, () => {
  fetchFollowStatus()
})
</script>

<template>
  <aside class="detail-right">
    <div v-if="author.name" class="author-card">
      <div class="author-info">
        <img :src="author.avatar" :alt="author.name" class="author-avatar">
        <div class="author-text">
          <h4 class="author-name">{{ author.name }}</h4>
          <p class="author-bio">{{ author.bio || '' }}</p>
        </div>
      </div>

      <div class="author-stats">
        <div class="stat-item">
          <span class="stat-value">{{ author.articleCount || 0 }}</span>
          <span class="stat-label">文章</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">{{ followerCount }}</span>
          <span class="stat-label">粉丝</span>
        </div>
      </div>

      <div class="author-actions">
        <button
          v-if="isLoggedIn()"
          class="btn-follow"
          :class="{ followed: isFollowing }"
          @click="handleFollow"
        >
          {{ isFollowing ? '已关注' : '+ 关注' }}
        </button>
        <button
          v-else
          class="btn-follow"
          @click="router.push('/login')"
        >
          + 关注
        </button>
        <button class="btn-message" @click="handleMessage">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15">
            <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
            <polyline points="22,6 12,13 2,6"/>
          </svg>
          私信
        </button>
      </div>
    </div>

    <div v-if="relatedArticles.length > 0" class="related-section">
      <h3 class="section-title">相关推荐</h3>
      <div class="related-list">
        <a
          v-for="item in relatedArticles"
          :key="item.id"
          href="#"
          class="related-item"
          @click.prevent="handleArticleClick(item)"
        >
          <img v-if="item.cover" :src="item.cover" :alt="item.title" class="related-cover">
          <div class="related-info">
            <h5 class="related-title">{{ item.title }}</h5>
            <div class="related-meta">
              <span class="related-author">{{ item.authorName }}</span>
              <span class="related-likes">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12">
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                </svg>
                {{ item.likes }}
              </span>
            </div>
          </div>
        </a>
      </div>
    </div>
  </aside>
</template>

<style scoped lang="scss">
.detail-right {
  display: flex;
  flex-direction: column;
  gap: 18px;
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

  &::-webkit-scrollbar {
    display: none;
  }

  -ms-overflow-style: none;
  scrollbar-width: none;
}

.author-card {
  padding: 20px;
  border-radius: 10px;
  background: #fff;
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    transform: translateY(-2px);
  }
}

.author-info {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 16px;

  .author-avatar {
    width: 52px;
    height: 52px;
    border-radius: 50%;
    object-fit: cover;
    border: 2px solid #f3f3f3;
  }

  .author-text {
    .author-name {
      font-size: 16px;
      font-weight: 700;
      color: #1a1a1a;
      margin-bottom: 3px;
    }

    .author-bio {
      font-size: 12.5px;
      color: #888;
      line-height: 1.4;
    }
  }
}

.author-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 28px;
  padding: 14px 0;
  margin-bottom: 16px;

  .stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;

    .stat-value {
      font-size: 19px;
      font-weight: 700;
      color: #1a1a1a;
    }

    .stat-label {
      font-size: 12px;
      color: #999;
    }
  }

  .stat-divider {
    width: 1px;
    height: 32px;
    background: #eee;
  }
}

.author-actions {
  display: flex;
  gap: 10px;
}

.btn-follow {
  flex: 1;
  padding: 8px 0;
  border: none;
  border-radius: 8px;
  font-size: 13.5px;
  font-weight: 600;
  cursor: pointer;
  background: #F97316;
  color: white;
  transition: all 0.2s;

  &:hover:not(.followed) {
    box-shadow: 0 6px 20px rgba(249, 115, 22, 0.35);
    transform: translateY(-1px);
  }

  &.followed {
    background: #f5f5f5;
    color: #666;
    cursor: pointer;

    &:hover {
      background: #fee2e2;
      color: #EF4444;
    }
  }
}

.btn-message {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 8px 14px;
  border: 1.5px solid #e5e7eb;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  background: transparent;
  color: #666;
  transition: all 0.2s;

  svg {
    stroke: currentColor;
  }

  &:hover {
    border-color: #F97316;
    color: #F97316;

    svg {
      stroke: #F97316;
    }
  }
}

.related-section {
  padding: 20px;
  border-radius: 10px;
  background: #fff;
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    transform: translateY(-2px);
  }
}

.section-title {
  font-size: 15px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 16px;
}

.related-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.related-item {
  display: flex;
  gap: 12px;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    .related-title {
      color: #F97316;
    }
  }
}

.related-cover {
  width: 80px;
  height: 54px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.related-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.related-title {
  font-size: 13.5px;
  font-weight: 600;
  color: #333;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.2s;
}

.related-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 11.5px;
  color: #aaa;

  .related-likes {
    display: inline-flex;
    align-items: center;
    gap: 3px;
  }
}
</style>
