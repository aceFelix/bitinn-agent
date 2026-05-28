<!--
  文章详情内容区
  展示文章正文、评论列表，支持发表评论和点赞/收藏/分享操作
  @author aceFelix
-->
<script setup>
import { ref, onMounted, watch } from 'vue'
import request from '@/utils/request'
import { useTokenStore } from '@/stores/token'
import { userInfoStore } from '@/stores/userInfo'
import defaultAvatar from '@/assets/user-avatar1.jpg'

const props = defineProps({
  article: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['comment-click', 'comment-count-change'])

const tokenStore = useTokenStore()
const userStore = userInfoStore()

const commentText = ref('')
const comments = ref([])
const submitting = ref(false)
const currentUserId = ref(null)

const isLoggedIn = () => !!tokenStore.token

function formatTime(timeStr) {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

const fetchComments = async () => {
  if (!props.article.id) return
  try {
    const result = await request.get(`/interaction/comment/${props.article.id}`)
    if (result.code === 200) {
      comments.value = (result.data || []).map(c => ({
        id: c.id,
        articleId: c.articleId,
        userId: c.userId,
        author: c.nickname || c.username || '匿名用户',
        avatar: c.userPic || defaultAvatar,
        content: c.content,
        time: formatTime(c.createTime),
        isOwner: currentUserId.value != null && c.userId === currentUserId.value
      }))
      emit('comment-count-change', comments.value.length)
    }
  } catch (error) {
    console.error('获取评论失败:', error)
  }
}

const submitComment = async () => {
  const text = commentText.value.trim()
  if (!text) return
  if (!isLoggedIn()) return
  if (submitting.value) return

  submitting.value = true
  try {
    const result = await request.post('/interaction/comment', {
      articleId: props.article.id,
      content: text
    })
    if (result.code === 200) {
      commentText.value = ''
      await fetchComments()
    }
  } catch (error) {
    console.error('提交评论失败:', error)
  } finally {
    submitting.value = false
  }
}

const deleteComment = async (commentId) => {
  try {
    const result = await request.delete(`/interaction/comment/${commentId}`)
    if (result.code === 200) {
      await fetchComments()
    }
  } catch (error) {
    console.error('删除评论失败:', error)
  }
}

const scrollToComments = () => {
  emit('comment-click')
}

onMounted(async () => {
  if (isLoggedIn()) {
    try {
      const info = userStore.userInfo
      if (info && info.id) {
        currentUserId.value = info.id
      }
    } catch (e) {
      // ignore
    }
  }
  await fetchComments()
})

watch(() => props.article.id, () => {
  fetchComments()
})
</script>

<template>
  <article class="article-detail-content">
    <div class="article-header">
      <h1 class="article-title">{{ article.title || '' }}</h1>
    </div>

    <div ref="contentRef" class="article-body" v-html="article.content || ''"></div>

    <div ref="commentSectionRef" class="comment-section">
      <div class="section-header">
        <h3 class="section-title">评论 ({{ comments.length }})</h3>
      </div>

      <div class="comment-input-area">
        <textarea
          v-model="commentText"
          class="comment-textarea"
          :placeholder="isLoggedIn() ? '写下你的想法...' : '请先登录后评论'"
          rows="3"
          :disabled="!isLoggedIn()"
        ></textarea>
        <button
          class="btn-submit-comment"
          :disabled="!isLoggedIn() || submitting || !commentText.trim()"
          @click="submitComment"
        >
          {{ submitting ? '发布中...' : '发布评论' }}
        </button>
      </div>

      <div class="comments-list">
        <div v-if="comments.length === 0" class="no-comments">
          <p>暂无评论，来说说你的想法吧~</p>
        </div>
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <img :src="comment.avatar" :alt="comment.author" class="comment-avatar">
          <div class="comment-main">
            <div class="comment-head">
              <span class="comment-author">{{ comment.author }}</span>
              <span class="comment-time">{{ comment.time }}</span>
              <button
                v-if="comment.isOwner"
                class="comment-delete-btn"
                title="删除评论"
                @click="deleteComment(comment.id)"
              >
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
                  <polyline points="3 6 5 6 21 6"/>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                </svg>
              </button>
            </div>
            <p class="comment-text">{{ comment.content }}</p>
          </div>
        </div>
      </div>
    </div>
  </article>
</template>

<style scoped lang="scss">
.article-detail-content {
  border-radius: 10px;
  background: #fff;
  padding: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  height: 100%;
  overflow-y: auto;
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
    transform: translateY(-2px);
  }

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(249, 115, 22, 0.25);
    border-radius: 4px;
  }
}

.article-header {
  padding: 28px 28px 20px;
}

.article-title {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.45;
  margin-bottom: 18px;
  color: #1a1a1a;
}

.article-body {
  padding: 0 28px 28px;
  font-size: 15px;
  line-height: 1.85;
  color: #333;

  :deep(h2) {
    font-size: 21px;
    font-weight: 700;
    margin: 32px 0 14px;
    padding-top: 10px;
    color: #1a1a1a;
  }

  :deep(h3) {
    font-size: 17px;
    font-weight: 600;
    margin: 24px 0 10px;
    color: #222;
  }

  :deep(p) {
    margin: 12px 0;
  }

  :deep(pre) {
    background: #1e1e2e;
    color: #cdd6f4;
    border-radius: 10px;
    padding: 18px 20px;
    overflow-x: auto;
    margin: 18px 0;
    font-size: 13.5px;
    line-height: 1.65;
  }

  :deep(code) {
    font-family: 'JetBrains Mono', 'Fira Code', monospace;
    font-size: 13px;
  }

  :deep(blockquote) {
    border-left: 4px solid #F97316;
    margin: 18px 0;
    padding: 12px 18px;
    background: rgba(249, 115, 22, 0.05);
    border-radius: 0 10px 10px 0;
    color: #555;
    font-style: italic;
  }

  :deep(img) {
    max-width: 100%;
    border-radius: 10px;
    margin: 16px 0;
  }

  :deep(ul),
  :deep(ol) {
    padding-left: 22px;
    margin: 12px 0;

    li {
      margin: 6px 0;
    }
  }

  :deep(table) {
    width: 100%;
    border-collapse: collapse;
    margin: 18px 0;

    th, td {
      border: 1px solid #e5e7eb;
      padding: 10px 14px;
      text-align: left;
      font-size: 13.5px;
    }

    th {
      background: #f9fafb;
      font-weight: 600;
    }
  }
}

.comment-section {
  padding: 24px 28px 28px;
  border-top: 1px solid #eee;
}

.section-header {
  margin-bottom: 18px;

  .section-title {
    font-size: 17px;
    font-weight: 700;
    color: #1a1a1a;
  }
}

.comment-input-area {
  margin-bottom: 24px;

  .comment-textarea {
    width: 100%;
    padding: 12px 16px;
    border: 1.5px solid #e5e7eb;
    border-radius: 10px;
    font-size: 14px;
    line-height: 1.6;
    resize: none;
    outline: none;
    transition: all 0.2s;
    font-family: inherit;
    box-sizing: border-box;

    &::placeholder {
      color: #bbb;
    }

    &:focus {
      border-color: #F97316;
      box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.08);
    }

    &:disabled {
      background: #f9f9f9;
      cursor: not-allowed;
    }
  }

  .btn-submit-comment {
    margin-top: 10px;
    padding: 8px 22px;
    border: none;
    border-radius: 9px;
    font-size: 13.5px;
    font-weight: 600;
    cursor: pointer;
    background: #F97316;
    color: white;
    transition: all 0.2s;

    &:hover:not(:disabled) {
      box-shadow: 0 6px 20px rgba(249, 115, 22, 0.35);
      transform: translateY(-1px);
    }

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.no-comments {
  text-align: center;
  padding: 32px 0;
  color: #bbb;
  font-size: 14px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding-bottom: 18px;
  border-bottom: 1px solid #f3f3f3;

  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.comment-main {
  flex: 1;
  min-width: 0;
}

.comment-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;

  .comment-author {
    font-size: 13.5px;
    font-weight: 600;
    color: #333;
  }

  .comment-time {
    font-size: 12px;
    color: #aaa;
  }

  .comment-delete-btn {
    display: inline-flex;
    align-items: center;
    margin-left: auto;
    padding: 2px 6px;
    border: none;
    border-radius: 4px;
    background: transparent;
    color: #ccc;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      color: #EF4444;
      background: rgba(239, 68, 68, 0.06);
    }
  }
}

.comment-text {
  font-size: 14px;
  line-height: 1.65;
  color: #444;
  margin-bottom: 8px;
}
</style>
