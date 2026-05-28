<!--
  草稿箱组件
  加载并展示用户的草稿列表，支持选择草稿继续编辑
  @author aceFelix
-->
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()

const drafts = ref([])
const loading = ref(true)
const selectedDraftId = ref(null)

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

const fetchDrafts = async () => {
  loading.value = true
  try {
    const result = await request.get('/article/my', {
      params: { state: '草稿', pageNum: 1, pageSize: 50 }
    })
    if (result.code === 200 && result.data) {
      const items = result.data.items || []
      drafts.value = items.map(item => ({
        id: item.id,
        title: item.title || '无标题草稿',
        updatedAt: formatTime(item.updateTime || item.createTime),
        wordCount: item.content ? item.content.replace(/<[^>]+>/g, '').length : 0
      }))
    }
  } catch (error) {
    console.error('获取草稿列表失败:', error)
  } finally {
    loading.value = false
  }
}

const selectDraft = (draft) => {
  selectedDraftId.value = selectedDraftId.value === draft.id ? null : draft.id
}

const continueDraft = () => {
  const draft = drafts.value.find(d => d.id === selectedDraftId.value)
  if (!draft) return
  router.push({ path: '/article/edit', query: { draftId: draft.id } })
}

const refreshDrafts = () => {
  selectedDraftId.value = null
  fetchDrafts()
}

defineExpose({
  getDrafts: () => drafts.value,
  drafts,
  selectedDraftId,
  selectDraft,
  continueDraft,
  refreshDrafts
})

onMounted(() => {
  fetchDrafts()
})
</script>

<template>
  <section class="drafts-section">
    <h2 class="section-title">草稿箱</h2>
    <p class="section-desc">继续你未完成的创作</p>
    <div class="drafts-list">
      <!-- 加载中 -->
      <div v-if="loading" class="drafts-loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- 草稿列表 -->
      <template v-else>
        <div
          v-for="draft in drafts"
          :key="draft.id"
          class="draft-card"
          :class="{ selected: selectedDraftId === draft.id }"
          @click="selectDraft(draft)"
        >
          <h4 class="draft-title">{{ draft.title }}</h4>
          <div class="draft-meta">
            <span class="draft-time">{{ draft.updatedAt }}</span>
            <span class="draft-count">{{ draft.wordCount }} 字</span>
          </div>
        </div>
        <div v-if="drafts.length === 0" class="drafts-empty">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
          </svg>
          <p>暂无草稿</p>
        </div>
      </template>
    </div>

    <!-- 继续编辑按钮 -->
    <div class="edit-action">
      <button
        class="btn-continue-edit"
        :class="{ active: selectedDraftId !== null }"
        :disabled="selectedDraftId === null"
        @click="continueDraft"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
          <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
        </svg>
        继续编辑
      </button>
    </div>
  </section>
</template>

<style scoped lang="scss">
.drafts-section {
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  transition: all 0.2s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(249, 115, 22, 0.25);
  }

  > .section-title {
    flex-shrink: 0;
    padding: 24px 24px 0;
    margin-bottom: 8px;
  }

  > .section-desc {
    flex-shrink: 0;
    padding: 0 24px;
    margin-bottom: 16px;
  }
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
}

.section-desc {
  font-size: 14px;
  margin-bottom: 20px;
}

.drafts-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  overflow-y: auto;
  padding: 0 24px 24px;
  scrollbar-width: none;
  -ms-overflow-style: none;

  &::-webkit-scrollbar { display: none; }
}

.drafts-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
  gap: 12px;

  p {
    font-size: 14px;
    color: #888;
  }
}

.loading-spinner {
  width: 24px;
  height: 24px;
  border: 3px solid rgba(249, 115, 22, 0.2);
  border-top-color: #F97316;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.draft-card {
  border: 2px solid transparent;
  border-radius: 10px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(249, 115, 22, 0.25);
  }

  &.selected {
    border-color: #F97316;
    background: rgba(249, 115, 22, 0.06);
    box-shadow: 0 4px 16px rgba(249, 115, 22, 0.2);

    .draft-title {
      color: #F97316;
    }
  }
}

.draft-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.2s;
}

.draft-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
}

.drafts-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;

  svg {
    width: 48px;
    height: 48px;
    margin-bottom: 12px;
    opacity: 0.5;
  }

  p {
    font-size: 14px;
  }
}

// 继续编辑按钮区域
.edit-action {
  padding: 16px 24px 24px;
  display: flex;
  justify-content: center;
  flex-shrink: 0;
}

.btn-continue-edit {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: transparent;
  color: #999;
  border: 1px solid #ddd;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: not-allowed;
  transition: all 0.2s;

  svg {
    width: 18px;
    height: 18px;
  }

  &.active {
    background: #F97316;
    color: white;
    border-color: #F97316;
    cursor: pointer;

    &:hover {
      box-shadow: 0 8px 24px rgba(249, 115, 22, 0.4);
      transform: translateY(-2px);
    }
  }
}
</style>
