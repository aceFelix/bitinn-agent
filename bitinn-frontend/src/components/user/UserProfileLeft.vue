<!--
  用户主页左侧栏
  展示头像、昵称、简介、加入日期和社交媒体信息
  @author aceFelix
-->
<script setup>
import { computed } from 'vue'
import defaultAvatar from '@/assets/user-avatar1.jpg'

const props = defineProps({
  userInfo: { type: Object, default: () => ({}) },
  uploading: { type: Boolean, default: false }
})

const emit = defineEmits(['upload-avatar'])

const displayAvatar = computed(() => {
  return props.userInfo.userPic || defaultAvatar
})

const displayBio = computed(() => {
  return props.userInfo.bio || '这个人很懒，还没有写简介~'
})

const joinDate = computed(() => {
  if (!props.userInfo.createTime) return ''
  const date = new Date(props.userInfo.createTime)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
})
</script>

<template>
  <aside class="profile-sidebar">
    <div class="profile-card">
      <div class="user-info-section">
        <div class="avatar-section">
          <div class="avatar-wrapper" @click="$emit('upload-avatar')">
            <img :src="displayAvatar" :alt="userInfo.nickname" class="profile-avatar" />
            <div class="avatar-overlay">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
                <circle cx="12" cy="13" r="4"/>
              </svg>
              <span>更换头像</span>
            </div>
            <div v-if="uploading" class="avatar-loading">
              <div class="spinner"></div>
            </div>
          </div>
        </div>

        <div class="basic-info">
          <h2 class="user-name">{{ userInfo.nickname || userInfo.username || '用户' }}</h2>
          <p class="user-bio">{{ displayBio }}</p>
        </div>

        <div class="stats-grid">
          <div class="stat-box">
            <span class="stat-value">{{ userInfo.articleCount || 0 }}</span>
            <span class="stat-label">文章</span>
          </div>
          <div class="stat-box">
            <span class="stat-value">{{ userInfo.fanCount || 0 }}</span>
            <span class="stat-label">粉丝</span>
          </div>
          <div class="stat-box">
            <span class="stat-value">{{ userInfo.likeCount || 0 }}</span>
            <span class="stat-label">获赞</span>
          </div>
        </div>

        <div class="join-info">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <polyline points="12 6 12 12 16 14"/>
          </svg>
          <span>于 {{ joinDate }} 加入</span>
        </div>
      </div>

      <div class="quick-links-section">
        <div class="extra-links">
          <h4 class="links-title">快捷入口</h4>
        <a href="#" class="link-item">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
          </svg>
          <span>我的文章</span>
        </a>
        <a href="#" class="link-item">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/>
          </svg>
          <span>我的收藏</span>
        </a>
        <a href="#" class="link-item">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
          <span>我的关注</span>
        </a>
        <a href="#" class="link-item">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="3"/>
            <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/>
          </svg>
          <span>账号设置</span>
        </a>
        </div>
      </div>
    </div>
  </aside>
</template>

<style scoped lang="scss">
.profile-sidebar {
  min-width: 0;
  height: 100%;
  overflow-y: auto;
  border-radius: 12px;
  padding: 16px;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);

  &:hover {
    box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
    transform: translateY(-2px);
  }

  &::-webkit-scrollbar {
    display: none;
  }

  -ms-overflow-style: none;
  scrollbar-width: none;

  .profile-card {
    display: flex;
    flex-direction: column;
    gap: 16px;

    .user-info-section {
      border-radius: 12px;
      padding: 24px;
      background: white;
      transition: all 0.2s;

      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
        transform: translateY(-2px);
      }
    }

    .quick-links-section {
      border-radius: 12px;
      padding: 20px 24px;
      background: white;
      transition: all 0.2s;

      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
        transform: translateY(-2px);
      }
    }
  }
}

.avatar-section {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;

  .avatar-wrapper {
    position: relative;
    width: 120px;
    height: 120px;
    border-radius: 50%;
    overflow: hidden;
    cursor: pointer;
    border: 3px solid #F97316;
    transition: all 0.2s;

    &:hover {
      box-shadow: 0 4px 16px rgba(249, 115, 22, 0.4);
      transform: scale(1.02);

      .avatar-overlay {
        opacity: 1;
      }
    }

    .profile-avatar {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .avatar-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.2s;
      color: white;

      svg {
        width: 24px;
        height: 24px;
        margin-bottom: 4px;
      }

      span {
        font-size: 12px;
      }
    }

    .avatar-loading {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;

      .spinner {
        width: 32px;
        height: 32px;
        border: 3px solid rgba(255, 255, 255, 0.3);
        border-top-color: #F97316;
        border-radius: 50%;
        animation: spin 0.8s linear infinite;
      }
    }
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.basic-info {
  text-align: center;
  margin-bottom: 20px;

  .user-name {
    font-size: 20px;
    font-weight: 700;
    color: #1a1a1a;
    margin-bottom: 8px;
  }

  .user-bio {
    font-size: 14px;
    color: #888;
    line-height: 1.5;
    word-break: break-all;
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;

  .stat-box {
    text-align: center;

    .stat-value {
      display: block;
      font-size: 20px;
      font-weight: 700;
      color: #F97316;
      line-height: 1.2;
    }

    .stat-label {
      font-size: 12px;
      color: #888;
      margin-top: 4px;
    }
  }
}

.join-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 12px;
  color: #888;

  svg {
    width: 14px;
    height: 14px;
  }
}

.extra-links {
  .links-title {
    font-size: 13px;
    font-weight: 600;
    color: #1a1a1a;
    margin-bottom: 12px;
  }

  .link-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 12px;
    border-radius: 8px;
    text-decoration: none;
    color: #555;
    font-size: 14px;
    transition: all 0.2s;

    svg {
      width: 18px;
      height: 18px;
      stroke: #888;
      transition: stroke 0.2s;
    }

    &:hover {
      background: rgba(249, 115, 22, 0.08);
      color: #F97316;

      svg {
        stroke: #F97316;
      }
    }
  }
}
</style>
