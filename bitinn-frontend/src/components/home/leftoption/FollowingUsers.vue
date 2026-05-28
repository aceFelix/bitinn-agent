<!--
  关注的用户列表
  展示当前用户关注的用户，支持取消关注
  @author aceFelix
-->
<script setup>
import { ref, onMounted } from 'vue'
import { cancellableRequest, cachedRequest } from '@/utils/apiClient'
import defaultAvatar from '@/assets/user-avatar1.jpg'

const followingUsers = ref([])
const loading = ref(true)

const fetchFollowingUsers = async () => {
  loading.value = true
  try {
    const result = await cachedRequest('following-users', {
      url: '/interaction/following-users',
      method: 'GET',
    }, 3 * 60 * 1000)
    if (result.code === 200 && result.data) {
      followingUsers.value = (result.data || []).map(user => ({
        id: user.id,
        nickname: user.nickname || user.username || '匿名用户',
        avatar: user.avatar || defaultAvatar,
        followerCount: user.followerCount || 0
      }))
    }
  } catch (error) {
    if (!error.message?.includes('cancel')) {
      console.error('获取关注列表失败:', error)
    }
  } finally {
    loading.value = false
  }
}

const toggleFollow = async (userId) => {
  try {
    const result = await cancellableRequest('follow-' + userId, {
      url: `/interaction/follow/${userId}`,
      method: 'POST',
    })
    if (result.code === 200) {
      const followed = result.data
      if (!followed) {
        followingUsers.value = followingUsers.value.filter(u => u.id !== userId)
      }
    }
  } catch (error) {
    if (!error.message?.includes('cancel')) {
      console.error('操作失败:', error)
    }
  }
}

onMounted(() => {
  fetchFollowingUsers()
})
</script>

<template>
  <section class="content-feed">
    <div class="feed-header">
      <div class="section-title">
        <svg viewBox="0 0 24 24" fill="none" stroke="#F97316" stroke-width="2" width="20" height="20">
          <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
          <circle cx="8.5" cy="7" r="4"/>
          <line x1="20" y1="8" x2="20" y2="14"/>
          <line x1="23" y1="11" x2="17" y2="11"/>
        </svg>
        <span>关注的创作者</span>
      </div>
    </div>

    <div class="users-list">
      <template v-if="loading">
        <div v-for="i in 3" :key="'skel-' + i" class="skeleton-card">
          <div class="skeleton-row">
            <div class="skeleton-avatar"></div>
            <div class="skeleton-info">
              <div class="skeleton-line short"></div>
            </div>
          </div>
        </div>
      </template>

      <template v-else-if="followingUsers.length > 0">
        <div
          v-for="user in followingUsers"
          :key="user.id"
          class="user-card"
        >
          <img :src="user.avatar" :alt="user.nickname" class="user-avatar">
          <div class="user-info">
            <span class="user-name">{{ user.nickname }}</span>
            <span class="user-followers">{{ user.followerCount }} 粉丝</span>
          </div>
          <button class="btn-unfollow" @click="toggleFollow(user.id)">已关注</button>
        </div>
      </template>

      <div v-else class="empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5" width="48" height="48">
          <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
          <circle cx="8.5" cy="7" r="4"/>
          <line x1="20" y1="8" x2="20" y2="14"/>
          <line x1="23" y1="11" x2="17" y2="11"/>
        </svg>
        <p>还没有关注任何创作者</p>
        <span class="empty-hint">浏览文章时关注作者即可</span>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.content-feed {
  min-width: 0;
  height: 100%;
  overflow-y: auto;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
    transform: translateY(-2px);
  }

  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-track { background: transparent; }
  &::-webkit-scrollbar-thumb { background: rgba(249, 115, 22, 0.3); border-radius: 4px; }
}

.feed-header { margin-bottom: 16px; }

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
}

.users-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 10px;
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    transform: translateY(-1px);
  }

  .user-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    object-fit: cover;
  }

  .user-info {
    flex: 1;
    min-width: 0;

    .user-name {
      font-size: 14px;
      font-weight: 600;
      display: block;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .user-followers {
      font-size: 11px;
      color: #999;
    }
  }

  .btn-unfollow {
    padding: 6px 14px;
    border-radius: 6px;
    font-size: 12px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    border: 1px solid #F97316;
    background: transparent;
    color: #F97316;

    &:hover {
      background: #F97316;
      color: white;
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 48px 0;
  color: #999;
  font-size: 14px;
  .empty-hint { font-size: 12px; color: #bbb; }
}

.skeleton-card {
  border-radius: 10px;
  padding: 12px 14px;

  .skeleton-row {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .skeleton-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: linear-gradient(90deg, rgba(249,115,22,0.06) 0%, rgba(249,115,22,0.14) 25%, rgba(249,115,22,0.06) 50%);
    background-size: 200% 100%;
    animation: shimmer 1.8s ease-in-out infinite;
  }

  .skeleton-info { flex: 1; }

  .skeleton-line {
    height: 14px;
    border-radius: 6px;
    background: linear-gradient(90deg, rgba(249,115,22,0.06) 0%, rgba(249,115,22,0.14) 25%, rgba(249,115,22,0.06) 50%);
    background-size: 200% 100%;
    animation: shimmer 1.8s ease-in-out infinite;
    &.short { width: 80px; }
  }
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}
</style>
