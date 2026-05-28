<!--
  文章卡片组件
  在 Feed 流中展示文章摘要，支持标准摘要和 HTML 内容渲染
  @author aceFelix
-->
<script setup>
defineProps({
  article: { type: Object, required: true },
  useHtmlContent: { type: Boolean, default: false },
  defaultAvatar: { type: String, required: true }
})

const emit = defineEmits(['click'])

const handleClick = (id) => {
  emit('click', id)
}

const onCoverError = (e) => { e.target.style.display = 'none' }
</script>

<template>
  <article
    class="article-card"
    @click="handleClick(article.id)"
  >
    <div class="article-header">
      <img
        :src="article.avatar"
        :alt="article.author"
        class="author-avatar"
        @error="e => e.target.src = defaultAvatar"
      >
      <div class="article-meta">
        <span class="author-name">{{ article.author }}</span>
        <span class="article-date">{{ article.publishedAt }}</span>
      </div>
    </div>

    <div class="article-content">
      <div class="article-text">
        <h2 v-if="useHtmlContent" class="article-title" v-html="article.title"></h2>
        <h2 v-else class="article-title">{{ article.title }}</h2>

        <p v-if="useHtmlContent" class="article-excerpt" v-html="article.excerpt"></p>
        <p v-else class="article-excerpt">{{ article.excerpt }}</p>

        <slot name="highlights"></slot>

        <div class="article-footer">
          <div class="article-tags">
            <span
              v-if="article.type"
              class="type-badge"
              :style="{ color: article.typeColor, borderColor: article.typeColor }"
            >
              {{ article.type }}
            </span>
            <span
              v-for="tag in article.tags"
              :key="tag"
              class="tag-badge"
            >
              #{{ tag }}
            </span>
          </div>

          <div class="article-stats">
            <span class="stat-item" :class="{ 'stat-active': article.isLiked }">
              <svg viewBox="0 0 24 24" :fill="article.isLiked ? '#FF4757' : 'none'" :stroke="article.isLiked ? '#FF4757' : 'currentColor'" stroke-width="2">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
              </svg>
              {{ article.likes }}
            </span>
            <span class="stat-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              </svg>
              {{ article.comments }}
            </span>
            <span class="stat-item" :class="{ 'stat-active': article.isFavorited }">
              <svg viewBox="0 0 24 24" :fill="article.isFavorited ? '#FFB800' : 'none'" :stroke="article.isFavorited ? '#FFB800' : 'currentColor'" stroke-width="2">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
              </svg>
              {{ article.favorites }}
            </span>
            <span class="stat-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8"/>
                <polyline points="16 6 12 2 8 6"/>
                <line x1="12" y1="2" x2="12" y2="15"/>
              </svg>
              {{ article.shares }}
            </span>
            <span class="stat-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12 6 12 12 16 14"/>
              </svg>
              {{ article.readTime }}
            </span>
          </div>
        </div>
      </div>

      <img
        v-if="article.cover"
        :src="article.cover"
        :alt="article.title"
        class="article-cover"
        @error="onCoverError"
      >
    </div>
  </article>
</template>

<style scoped lang="scss">
.article-card {
  border-radius: 12px;
  padding: 20px;
  transition: all 0.2s;
  cursor: pointer;

  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
    transform: translateY(-2px);
  }
}

.article-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;

  .author-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
  }

  .article-meta {
    display: flex;
    flex-direction: column;
    gap: 2px;

    .author-name { font-size: 14px; font-weight: 600; }
    .article-date { font-size: 12px; color: #999; }
  }
}

.article-content {
  display: flex;
  gap: 20px;

  .article-text {
    flex: 1;
    min-width: 0;
  }

  .article-title {
    font-size: 20px;
    font-weight: 600;
    margin-bottom: 8px;
    line-height: 1.4;

    :deep(.highlight) {
      color: #F97316;
      background: rgba(249, 115, 22, 0.1);
      padding: 0 2px;
      border-radius: 2px;
    }
  }

  .article-excerpt {
    font-size: 14px;
    line-height: 1.6;
    margin-bottom: 16px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;

    :deep(.highlight) {
      color: #F97316;
      background: rgba(249, 115, 22, 0.1);
      padding: 0 2px;
      border-radius: 2px;
    }
  }

  .article-cover {
    width: 200px;
    height: 120px;
    object-fit: cover;
    border-radius: 8px;
    flex-shrink: 0;
  }
}

.article-footer {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;

  .type-badge {
    padding: 4px 12px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 600;
    border: 1.5px solid;
    transition: all 0.2s;

    &:hover { transform: translateY(-1px); }
  }

  .tag-badge {
    padding: 4px 10px;
    border-radius: 12px;
    font-size: 12px;
    transition: all 0.2s;
  }
}

.article-stats {
  display: flex;
  gap: 16px;

  .stat-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;

    svg { width: 16px; height: 16px; }

    &.stat-active { font-weight: 600; }
  }
}

@media (max-width: 768px) {
  .article-content {
    flex-direction: column;

    .article-cover {
      width: 100%;
      height: 180px;
    }
  }
}
</style>
