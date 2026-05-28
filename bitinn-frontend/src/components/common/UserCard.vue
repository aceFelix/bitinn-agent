<!--
  用户卡片弹出层
  展示当前登录用户头像、昵称、角色及快捷操作入口
  @author aceFelix
-->
<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import defaultAvatar from '@/assets/user-avatar1.jpg'
import { useTokenStore } from '@/stores/token'
import { userInfoStore } from '@/stores/userInfo'

const props = defineProps({
  visible: Boolean
})

const emit = defineEmits(['close'])

const router = useRouter()
const tokenStore = useTokenStore()
const userStore = userInfoStore()

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

const userBio = computed(() => {
  return userStore.userInfo?.bio || '热爱技术，分享知识'
})

const articleCount = computed(() => {
  return userStore.userInfo?.articleCount || 0
})

const fanCount = computed(() => {
  return userStore.userInfo?.fanCount || 0
})

const closeCard = () => {
  emit('close')
}

const goToProfile = () => {
  router.push('/profile')
  closeCard()
}

const handleLogout = () => {
  tokenStore.removeToken()
  userStore.removeUserInfo()
  router.push('/')
  closeCard()
}
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="user-card-overlay" @click="closeCard">
      <div class="user-card" @click.stop>
        <div class="card-header">
          <img :src="userAvatar" :alt="userName" class="card-avatar" />
          <div class="card-info">
            <h3 class="card-name">{{ userName }}</h3>
            <p class="card-bio">{{ userBio }}</p>
          </div>
        </div>

        <div class="card-stats">
          <div class="stat-item">
            <span class="stat-num">{{ articleCount }}</span>
            <span class="stat-label">文章</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-num">{{ fanCount }}</span>
            <span class="stat-label">粉丝</span>
          </div>
        </div>

        <div class="card-actions">
          <button class="btn-profile" @click="goToProfile">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            <span>个人主页</span>
          </button>
          <button class="btn-logout" @click="handleLogout">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
              <polyline points="16 17 21 12 16 7"/>
              <line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
            <span>退出登录</span>
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped lang="scss">
.user-card-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.15);
  z-index: 9999;
}

.user-card {
  position: absolute;
  top: 70px;
  right: calc((100vw - 1400px) / 2 + 46px);
  width: 260px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  padding: 12px 14px;
  animation: cardSlideIn 0.15s ease-out;

  @keyframes cardSlideIn {
    from {
      opacity: 0;
      transform: translateY(-6px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.card-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #F97316;
  flex-shrink: 0;
}

.card-info {
  min-width: 0;
  overflow: hidden;
}

.card-name {
  font-size: 14px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-bio {
  font-size: 11px;
  color: #888;
  line-height: 1.3;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px 0;
  gap: 16px;
}

.stat-item {
  text-align: center;
}

.stat-num {
  display: block;
  font-size: 16px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.2;
}

.stat-label {
  font-size: 10px;
  color: #888;
  margin-top: 1px;
}

.stat-divider {
  width: 1px;
  height: 22px;
  background: #e5e7eb;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-top: 10px;
}

.btn-profile,
.btn-logout {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  padding: 7px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #F97316;
  background: transparent;
  color: #F97316;

  svg {
    width: 13px;
    height: 13px;
  }

  &:hover {
    background: #F97316;
    color: white;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(249, 115, 22, 0.25);

    svg {
      stroke: white;
    }
  }
}

.btn-logout {
  border-color: #ef4444;
  color: #ef4444;

  &:hover {
    background: #ef4444;
    box-shadow: 0 4px 12px rgba(239, 68, 68, 0.25);
  }
}
</style>