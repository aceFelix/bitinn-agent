<!--
  AI 聊天页面
  三栏布局：左侧会话列表、中间聊天面板、右侧技能引导
  @author aceFelix
-->
<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import AiChatLeft from '@/components/ai-chat/AiChatLeft.vue'
import AiChatMain from '@/components/ai-chat/AiChatMain.vue'
import AiChatRight from '@/components/ai-chat/AiChatRight.vue'
import CodeThemeSelector from '@/components/ai-chat/CodeThemeSelector.vue'
import { userInfoStore } from '@/stores/userInfo'
import { useTokenStore } from '@/stores/token'
import defaultAvatar from '@/assets/user-avatar1.jpg'

const router = useRouter()
const tokenStore = useTokenStore()
const showSettings = ref(false)
const codeTheme = ref(localStorage.getItem('ai-code-theme') || 'github-dark')

const userStore = userInfoStore()
const isLoggedIn = computed(() => !!tokenStore.token)

const userAvatar = computed(() => {
  const avatar = userStore.userInfo?.userPic || ''
  return avatar || defaultAvatar
})
const userName = computed(() => userStore.userInfo?.nickname || '用户')

const goToLogin = () => {
  router.push('/login')
}

const currentConversationId = ref(null)
const mainPanelKey = ref(0)
const initialMode = ref('normal')
const initialModel = ref('qwen3.6-plus')

const leftPanelRef = ref(null)
const mainPanelRef = ref(null)

const handleSelectConversation = (conv) => {
  const id = conv?.id || conv
  currentConversationId.value = id
  initialMode.value = conv?.mode || 'normal'
  initialModel.value = conv?.model || 'qwen3.6-plus'
  mainPanelKey.value++
}

const handleNewConversation = () => {
  currentConversationId.value = null
  mainPanelKey.value++
}

const handleConversationCreated = (id) => {
  currentConversationId.value = id
  leftPanelRef.value?.refreshList()
}

const handleDeleteConversation = (id) => {
  if (currentConversationId.value === id) {
    currentConversationId.value = null
  }
  leftPanelRef.value?.refreshList()
}

const handleMessageSent = () => {
  leftPanelRef.value?.refreshList()
}

const handleTitleUpdated = ({ conversationId, title }) => {
  leftPanelRef.value?.updateConversationTitle(conversationId, title)
}

const handleSkillSelect = (skill) => {
  mainPanelRef.value?.handleSkillSelect(skill)
}

const handleQuickAction = (text) => {
  mainPanelRef.value?.handleQuickAction(text)
}

const goBack = () => {
  router.push('/')
}

const toggleSettings = () => {
  showSettings.value = !showSettings.value
}
</script>

<template>
  <div class="ai-chat-page">
    <header class="navbar">
      <div class="nav-brand" @click="goBack">
        <div class="logo">
          <span class="logo-bracket">&lt;</span>
          <span class="logo-text">BitInn</span>
          <span class="logo-bracket">/&gt;</span>
        </div>
      </div>

      <div class="page-title">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="ai-icon">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
        <span>AI 小B</span>
      </div>

      <div class="nav-actions-right">
        <button
          :class="['btn-settings', { active: showSettings }]"
          @click="toggleSettings"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="3"/>
            <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/>
          </svg>
        </button>
        <button class="btn-back" @click="goBack">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          <span>返回</span>
        </button>
        <button v-if="!isLoggedIn" class="btn-login" @click="goToLogin">登录 / 注册</button>
        <div v-else class="user-avatar-wrapper" :title="userName">
          <img :src="userAvatar" :alt="userName" class="nav-user-avatar" />
        </div>
      </div>
    </header>

    <transition name="settings-fade">
      <div v-if="showSettings" class="settings-dropdown">
        <div class="settings-header">
          <h3 class="settings-title">AI 设置</h3>
          <button class="btn-close-settings" @click="showSettings = false">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>

        <div class="settings-content">
          <div class="setting-item">
            <label>模型选择</label>
            <select class="setting-select">
              <option>通义千问 Plus</option>
              <option>通义千问 Max</option>
              <option>通义千问 Turbo</option>
            </select>
          </div>

          <CodeThemeSelector v-model="codeTheme" />
        </div>
      </div>
    </transition>

    <main class="chat-grid">
      <AiChatLeft
        ref="leftPanelRef"
        :current-conversation-id="currentConversationId"
        @select-conversation="handleSelectConversation"
        @new-conversation="handleNewConversation"
        @delete-conversation="handleDeleteConversation"
      />
      <AiChatMain
        ref="mainPanelRef"
        :key="mainPanelKey"
        :conversation-id="currentConversationId"
        :initial-mode="initialMode"
        :initial-model="initialModel"
        :code-theme="codeTheme"
        @conversation-created="handleConversationCreated"
        @message-sent="handleMessageSent"
        @title-updated="handleTitleUpdated"
      />
      <AiChatRight
        @select-skill="handleSkillSelect"
        @quick-action="handleQuickAction"
      />
    </main>
  </div>
</template>

<style scoped lang="scss">
.ai-chat-page {
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background: #f8f9fa;
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
  background: white;

  &:hover {
    box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
  }

  .nav-brand {
    cursor: pointer;

    .logo {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 24px;
      font-weight: 700;

      .logo-bracket {
        font-family: 'JetBrains Mono', 'Fira Code', monospace;
      }

      .logo-text {
        text-shadow: 0 0 10px rgba(249, 115, 22, 0.5);
        color: #F97316;
      }
    }
  }

  .page-title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 18px;
    font-weight: 600;
    color: #1a1a1a;

    .ai-icon {
      width: 28px;
      height: 28px;
      stroke: #F97316;
    }
  }

  .btn-back {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    border: 1px solid #F97316;
    background: transparent;
    color: #F97316;

    svg {
      width: 16px;
      height: 16px;
    }

    &:hover {
      box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
      transform: translateY(-2px);
      background: #F97316;
      color: white;

      svg { stroke: white; }
    }
  }

  .nav-actions-right {
    display: flex;
    align-items: center;
    gap: 12px;

    .btn-login {
      padding: 8px 18px;
      border-radius: 6px;
      font-size: 14px;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.2s;
      border: 1px solid #F97316;
      background: transparent;
      color: #F97316;

      &:hover {
        box-shadow: 0 4px 12px rgba(249, 115, 22, 0.3);
        background: #F97316;
        color: white;
      }
    }

    .user-avatar-wrapper {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      overflow: hidden;
      border: 2px solid #f0f0f0;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        border-color: #F97316;
        transform: scale(1.05);
        box-shadow: 0 4px 12px rgba(249, 115, 22, 0.3);
      }

      .nav-user-avatar {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
      }
    }

    .btn-settings {
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 8px;
      border: 1px solid #e5e7eb;
      background: transparent;
      cursor: pointer;
      transition: all 0.2s;

      svg {
        width: 20px;
        height: 20px;
        stroke: #666;
        transition: stroke 0.2s;
      }

      &:hover {
        border-color: #F97316;
        background: rgba(249, 115, 22, 0.05);

        svg {
          stroke: #F97316;
        }
      }

      &.active {
        border-color: #F97316;
        background: rgba(249, 115, 22, 0.1);

        svg {
          stroke: #F97316;
        }
      }
    }
  }
}

.chat-grid {
  display: grid;
  grid-template-columns: 260px 1fr 300px;
  gap: 24px;
  max-width: 1400px;
  margin: 0 auto;
  padding: 84px 24px 24px;
  height: 100vh;
  overflow: hidden;
  box-sizing: border-box;
}

@media (max-width: 1200px) {
  .chat-grid {
    grid-template-columns: 240px 1fr 280px;
    gap: 16px;
    padding: 84px 16px 16px;
  }
}

@media (max-width: 992px) {
  .chat-grid {
    grid-template-columns: 1fr;
    height: auto;
    overflow: visible;
  }

  .page-title {
    font-size: 16px;
  }
}

.settings-dropdown {
  position: fixed;
  top: 70px;
  right: 24px;
  width: 420px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  z-index: 999;
  overflow: hidden;

  .settings-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    border-bottom: 1px solid #e5e7eb;
    background: linear-gradient(135deg, #FFF5EB, white);

    .settings-title {
      font-size: 16px;
      font-weight: 700;
      color: #1a1a1a;
      margin: 0;
    }

    .btn-close-settings {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 6px;
      border: none;
      background: transparent;
      cursor: pointer;
      transition: all 0.2s;

      svg {
        width: 18px;
        height: 18px;
        stroke: #666;
      }

      &:hover {
        background: rgba(239, 68, 68, 0.1);

        svg {
          stroke: #ef4444;
        }
      }
    }
  }

  .settings-content {
    padding: 20px;
    display: flex;
    flex-direction: column;
    gap: 16px;

    .setting-item {
      display: flex;
      flex-direction: column;
      gap: 8px;

      label {
        font-size: 13px;
        font-weight: 600;
        color: #333;
      }

      .setting-select {
        width: 100%;
        padding: 10px 12px;
        border-radius: 8px;
        border: 1px solid #e5e7eb;
        font-size: 13px;
        background: white;
        cursor: pointer;
        transition: all 0.2s;

        &:focus {
          outline: none;
          border-color: #F97316;
          box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.1);
        }
      }
    }

    :deep(.code-theme-selector) {
      .theme-label { color: #333; }
    }
  }
}

.settings-fade-enter-active,
.settings-fade-leave-active {
  transition: all 0.25s ease-out;
}

.settings-fade-enter-from,
.settings-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
