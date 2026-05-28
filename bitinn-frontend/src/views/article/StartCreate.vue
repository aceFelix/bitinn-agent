<!--
  创作入口页面
  选择文章类型、标签，管理草稿箱，开始创作
  @author aceFelix
-->
<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import ArticleTypeSelector from '@/components/create/ArticleTypeSelector.vue'
import TagSelector from '@/components/create/TagSelector.vue'
import DraftBox from '@/components/create/DraftBox.vue'
import UserCard from '@/components/common/UserCard.vue'
import defaultAvatar from '@/assets/user-avatar1.jpg'
import { useTokenStore } from '@/stores/token'
import { userInfoStore } from '@/stores/userInfo'

const router = useRouter()
const tokenStore = useTokenStore()
const userStore = userInfoStore()

const isLoggedIn = computed(() => !!tokenStore.token)
const showUserCard = ref(false)

const userAvatar = computed(() => {
  if (!isLoggedIn.value) return ''
  const avatar = userStore.userInfo?.userPic || ''
  return avatar || defaultAvatar
})

const userName = computed(() => {
  if (!isLoggedIn.value) return ''
  return userStore.userInfo?.nickname || userStore.userInfo?.username || '用户'
})

const goToLogin = () => {
  router.push('/login')
}

const toggleUserCard = () => {
  showUserCard.value = !showUserCard.value
}

const closeUserCard = () => {
  showUserCard.value = false
}

// 文章类型选择器引用
const typeSelectorRef = ref(null)
// 标签选择器引用
const tagSelectorRef = ref(null)
// 草稿箱引用
const draftBoxRef = ref(null)

// 开始创作
const startWriting = () => {
  // 可以携带选中的类型和标签
  const query = {}
  const selectedType = typeSelectorRef.value?.getSelectedType()
  const selectedTags = tagSelectorRef.value?.getSelectedTags()
  if (selectedType) {
    query.type = selectedType.id
  }
  if (selectedTags && selectedTags.length > 0) {
    query.tags = selectedTags.join(',')
  }
  router.push({ path: '/article/edit', query })
}

// 返回首页
const goHome = () => {
  router.push('/')
}
</script>

<template>
  <div class="create-container">
    <!-- 顶部导航 -->
    <header class="create-header">
      <div class="header-brand" @click="goHome">
        <span class="bracket">&lt;</span>
        <span class="brand-name">BitInn</span>
        <span class="bracket">/&gt;</span>
      </div>
      <h1 class="header-title">开始创作</h1>
      <div class="header-right">
        <button class="btn-back" @click="goHome">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          <span>返回</span>
        </button>
        <button v-if="!isLoggedIn" class="btn-login" @click="goToLogin">登录 / 注册</button>
        <div v-else class="user-avatar-wrapper" :title="userName" @click="toggleUserCard">
          <img :src="userAvatar" :alt="userName" class="user-avatar" />
        </div>
      </div>
    </header>

    <UserCard :visible="showUserCard" @close="closeUserCard" />

    <!-- 主内容区 -->
    <main class="create-content">
      <!-- 左侧：文章类型选择 -->
      <ArticleTypeSelector ref="typeSelectorRef" />

      <!-- 中间：推荐标签 -->
      <TagSelector ref="tagSelectorRef" :has-selected-type="!!typeSelectorRef?.selectedType" @start-edit="startWriting" />

      <!-- 右侧：草稿箱 -->
      <DraftBox ref="draftBoxRef" />
    </main>
  </div>
</template>

<style scoped lang="scss">
.create-container {
  height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

// 顶部导航
.create-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  box-sizing: border-box;
  z-index: 1000;
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
  }
}

.header-brand {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 24px;
  font-weight: 700;
  cursor: pointer;
  transition: opacity 0.2s;

  &:hover {
    opacity: 0.8;
  }

  .bracket {
    font-family: 'JetBrains Mono', 'Fira Code', monospace;
  }

  .brand-name {
    text-shadow: 0 0 10px rgba(249, 115, 22, 0.5);
    color: #F97316;
  }
}

.header-title {
  font-size: 20px;
  font-weight: 600;
}

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

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
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

// 主内容区
.create-content {
  flex: 1;
  display: grid;
  grid-template-columns: 1.2fr 1fr 1fr;
  gap: 24px;
  padding: 84px 24px 24px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  min-height: 0;
  overflow: hidden;
  box-sizing: border-box;

  > * {
    min-height: 0;
    overflow: hidden;
  }
}

// 通用区块样式
section {
  height: 100%;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  transition: all 0.2s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(249, 115, 22, 0.25);
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

// 响应式
@media (max-width: 1200px) {
  .create-content {
    grid-template-columns: 1fr 1fr;
    gap: 20px;
    padding: 80px 20px 20px;
  }

  .drafts-section {
    grid-column: span 2;
  }
}

@media (max-width: 900px) {
  .create-content {
    grid-template-columns: 1fr;
    gap: 16px;
    padding: 76px 16px 16px;
  }

  .drafts-section {
    grid-column: span 1;
  }

  .type-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .create-content {
    grid-template-columns: 1fr;
    padding: 76px 16px 16px;
  }

  .drafts-section {
    grid-column: span 1;
  }

  .type-grid {
    grid-template-columns: 1fr;
  }

  .create-footer {
    flex-direction: column;
    gap: 16px;
  }
}
</style>
