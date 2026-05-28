<!--
  AI 聊天左侧面板
  管理会话列表：加载、创建、删除会话
  @author aceFelix
-->
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listConversations, createConversation, deleteConversation } from '@/api/chat'

const props = defineProps({
  currentConversationId: { type: String, default: null }
})

const emit = defineEmits(['select-conversation', 'new-conversation', 'delete-conversation'])

const conversations = ref([])
const showDeleteConfirm = ref(null)

const loadConversations = async () => {
  try {
    const res = await listConversations()
    conversations.value = res.data || []
  } catch (e) {
    // 加载失败静默处理
  }
}

const selectConversation = (conv) => {
  emit('select-conversation', conv)
}

const newChat = async () => {
  emit('new-conversation')
}

const confirmDelete = (id) => {
  showDeleteConfirm.value = id
}

const cancelDelete = () => {
  showDeleteConfirm.value = null
}

const executeDelete = async (id) => {
  try {
    await deleteConversation(id)
    emit('delete-conversation', id)
    conversations.value = conversations.value.filter(c => c.id !== id)
    ElMessage.success('已删除')
  } catch (e) {
    ElMessage.error('删除失败')
  }
  showDeleteConfirm.value = null
}

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  if (days < 30) return `${Math.floor(days / 7)}周前`
  return date.toLocaleDateString('zh-CN')
}

const refreshList = () => {
  loadConversations()
}

const updateConversationTitle = (conversationId, title) => {
  const conv = conversations.value.find(c => c.id === conversationId)
  if (conv) {
    conv.title = title
  }
}

onMounted(() => {
  loadConversations()
})

defineExpose({ refreshList, updateConversationTitle })
</script>

<template>
  <aside class="chat-sidebar">
    <div class="sidebar-header">
      <button class="btn-new-chat" @click="newChat">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 5v14M5 12h14"/>
        </svg>
        <span>新建对话</span>
      </button>
    </div>

    <div class="conversation-list">
      <div
        v-for="conv in conversations"
        :key="conv.id"
        :class="['conversation-item', { active: conv.id === currentConversationId }]"
        @click="selectConversation(conv)"
      >
        <div class="conv-content">
          <svg v-if="conv.mode === 'vision'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="conv-icon">
            <rect x="3" y="3" width="18" height="18" rx="2"/>
            <circle cx="8.5" cy="8.5" r="1.5"/>
            <path d="M21 15l-5-5L5 21"/>
          </svg>
          <svg v-else-if="conv.mode === 'professional'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="conv-icon">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/>
            <path d="M2 17l10 5 10-5"/>
            <path d="M2 12l10 5 10-5"/>
          </svg>
          <svg v-else-if="conv.mode === 'image-gen'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="conv-icon">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
            <circle cx="8.5" cy="8.5" r="1.5"/>
            <polyline points="21 15 16 10 5 21"/>
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="conv-icon">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
          <div class="conv-info">
            <span class="conv-title">{{ conv.title }}</span>
            <div class="conv-meta">
              <span v-if="conv.mode && conv.mode !== 'normal'" class="conv-mode-badge">{{ { vision: '识图', professional: '专业', 'image-gen': '生图' }[conv.mode] }}</span>
              <span class="conv-time">{{ formatTime(conv.lastMessageAt || conv.updatedAt) }}</span>
            </div>
          </div>
        </div>

        <template v-if="showDeleteConfirm === conv.id">
          <button class="btn-delete-confirm" @click.stop="executeDelete(conv.id)">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
          </button>
          <button class="btn-delete-cancel" @click.stop="cancelDelete">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </template>
        <button v-else class="btn-delete" @click.stop="confirmDelete(conv.id)">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="3 6 5 6 21 6"/>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
          </svg>
        </button>
      </div>

      <div v-if="conversations.length === 0" class="empty-conversations">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="empty-icon">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
        <span>暂无对话，点击上方按钮开始</span>
      </div>
    </div>
  </aside>
</template>

<style scoped lang="scss">
.chat-sidebar {
  height: 100%;
  overflow-y: auto;
  border-radius: 12px;
  padding: 16px;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  background: white;
  display: flex;
  flex-direction: column;
  gap: 16px;

  &:hover {
    box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
    transform: translateY(-2px);
  }

  &::-webkit-scrollbar { display: none; }
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.sidebar-header {
  display: flex;
  gap: 8px;

  .btn-new-chat {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 12px 16px;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    border: none;
    background: linear-gradient(135deg, #F97316, #ea580c);
    color: white;

    svg {
      width: 18px;
      height: 18px;
      stroke: white;
    }

    &:hover {
      box-shadow: 0 8px 24px rgba(249, 115, 22, 0.4);
      transform: translateY(-2px);
    }
  }
}

.conversation-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.conversation-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    transform: translateY(-1px);

    .btn-delete {
      opacity: 1;
    }
  }

  &.active {
    box-shadow: 0 6px 16px rgba(249, 115, 22, 0.3);
    border-left: 3px solid #F97316;

    .conv-icon {
      stroke: #F97316 !important;
    }

    .conv-title {
      color: #F97316 !important;
      font-weight: 600;
    }
  }

  .conv-content {
    display: flex;
    align-items: center;
    gap: 12px;
    flex: 1;
    min-width: 0;

    .conv-icon {
      width: 20px;
      height: 20px;
      stroke: #888;
      flex-shrink: 0;
    }

    .conv-info {
      display: flex;
      flex-direction: column;
      gap: 2px;
      min-width: 0;

      .conv-title {
        font-size: 13px;
        color: #333;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .conv-meta {
        display: flex;
        align-items: center;
        gap: 6px;
      }

      .conv-mode-badge {
        padding: 0 5px;
        border-radius: 4px;
        background: rgba(249, 115, 22, 0.1);
        color: #F97316;
        font-size: 10px;
        font-weight: 500;
        line-height: 16px;
      }

      .conv-time {
        font-size: 11px;
        color: #999;
      }
    }
  }

  .btn-delete {
    width: 28px;
    height: 28px;
    display: flex;
    align-items: center;
    justify-content: center;
    border: none;
    background: transparent;
    border-radius: 6px;
    cursor: pointer;
    opacity: 0;
    transition: all 0.2s;

    svg {
      width: 16px;
      height: 16px;
      stroke: #999;
    }

    &:hover {
      background: rgba(239, 68, 68, 0.1);

      svg {
        stroke: #ef4444;
      }
    }
  }

  .btn-delete-confirm,
  .btn-delete-cancel {
    width: 28px;
    height: 28px;
    display: flex;
    align-items: center;
    justify-content: center;
    border: none;
    border-radius: 6px;
    cursor: pointer;

    svg {
      width: 16px;
      height: 16px;
    }
  }

  .btn-delete-confirm {
    background: rgba(239, 68, 68, 0.1);

    svg { stroke: #ef4444; }

    &:hover {
      background: #ef4444;

      svg { stroke: white; }
    }
  }

  .btn-delete-cancel {
    background: transparent;

    svg { stroke: #999; }

    &:hover {
      background: #f0f0f0;
    }
  }
}

.empty-conversations {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 40px 16px;
  color: #999;
  font-size: 13px;

  .empty-icon {
    width: 40px;
    height: 40px;
    stroke: #ddd;
  }
}
</style>
