<!--
  消息 Feed 面板
  展示点赞、收藏、评论、私信、粉丝、系统通知等分类消息
  @author aceFelix
-->
<script setup>
import { ref, computed } from 'vue'
import defaultAvatar from '@/assets/user-avatar1.jpg'

const emit = defineEmits(['back'])

const props = defineProps({
  activeTab: { type: String, default: 'like' }
})

const tabs = [
  { id: 'like', label: '点赞' },
  { id: 'favorite', label: '收藏' },
  { id: 'comment', label: '评论' },
  { id: 'dm', label: '私信' },
  { id: 'follower', label: '新增粉丝' },
  { id: 'system', label: '系统通知' }
]

const currentTab = ref(props.activeTab)

function formatTime(timeStr) {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  if (diff < 0) return '刚刚'
  const minutes = Math.floor(diff / 60000)
  if (minutes < 60) return `${minutes}分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 3) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

const switchTab = (tabId) => {
  currentTab.value = tabId
}

// ---- Mock Data ----
const likeNotifications = ref([
  { id: 1, user: { name: '前端架构师', avatar: '' }, target: 'Vue 3 Composition API 实战指南', time: '2026-04-30T10:23:00', unread: true },
  { id: 2, user: { name: '全栈阿杰', avatar: '' }, target: 'Spring Boot 3.x 新特性解析', time: '2026-04-30T08:15:00', unread: true },
  { id: 3, user: { name: 'Pythonista', avatar: '' }, target: 'FastAPI 异步编程模式', time: '2026-04-29T22:40:00', unread: false },
  { id: 4, user: { name: '代码诗人', avatar: '' }, target: '微服务架构设计原则', time: '2026-04-29T18:12:00', unread: false },
  { id: 5, user: { name: 'Rustacean', avatar: '' }, target: 'Rust 所有权系统深入理解', time: '2026-04-29T14:30:00', unread: false }
])

const favoriteNotifications = ref([
  { id: 6, user: { name: '云原生小白', avatar: '' }, target: 'Docker Compose 编排实战', time: '2026-04-30T09:45:00', unread: true },
  { id: 7, user: { name: 'Java练习生', avatar: '' }, target: 'JVM 调优实战案例', time: '2026-04-29T16:20:00', unread: false },
  { id: 8, user: { name: 'DevOps老王', avatar: '' }, target: 'CI/CD 流水线设计', time: '2026-04-28T11:00:00', unread: false }
])

const commentNotifications = ref([
  { id: 9, user: { name: '测试小能手', avatar: '' }, target: '单元测试编写指南', comment: '写得很好，实际项目中很受用！', time: '2026-04-30T11:05:00', unread: true },
  { id: 10, user: { name: '后端老司机', avatar: '' }, target: '数据库索引优化', comment: 'B+树的部分可以再深入讲讲吗？', time: '2026-04-30T07:30:00', unread: true },
  { id: 11, user: { name: '前端小学生', avatar: '' }, target: 'CSS Grid 布局完全指南', comment: '收藏了，太实用了', time: '2026-04-29T20:15:00', unread: false }
])

const dmList = ref([
  { id: 12, user: { name: '技术大咖', avatar: '' }, message: '你好，看到你写的关于微服务的文章...', time: '2026-04-30T12:10:00', unread: true },
  { id: 13, user: { name: '代码诗人', avatar: '' }, message: '你的那个开源项目我很感兴趣...', time: '2026-04-30T09:30:00', unread: true },
  { id: 14, user: { name: '架构师李', avatar: '' }, message: '有没有兴趣一起做个 side project？', time: '2026-04-29T15:00:00', unread: false }
])

const followerNotifications = ref([
  { id: 15, user: { name: 'AI探索者', avatar: '', bio: '专注 AI / ML 领域' }, time: '2026-04-30T13:05:00', unread: true, followed: false },
  { id: 16, user: { name: '前端新秀', avatar: '', bio: 'Vue & React 开发者' }, time: '2026-04-30T10:50:00', unread: true, followed: false },
  { id: 17, user: { name: '数据科学家', avatar: '', bio: 'Python & 数据挖掘' }, time: '2026-04-29T19:20:00', unread: false, followed: true }
])

const systemNotifications = ref([
  { id: 18, type: 'info', title: '社区公约更新', content: 'BitInn 社区公约已于 2026年4月28日更新，请仔细阅读最新版公约内容。', time: '2026-04-29T10:00:00', unread: true },
  { id: 19, type: 'success', title: '创作等级提升', content: '恭喜！你的创作等级已升至 Lv.3，解锁了自定义封面功能。', time: '2026-04-28T08:30:00', unread: false },
  { id: 20, type: 'warning', title: '账号安全提醒', content: '检测到你的账号在新设备上登录，如非本人操作请及时修改密码。', time: '2026-04-26T14:22:00', unread: false }
])

const currentList = computed(() => {
  const map = {
    like: likeNotifications,
    favorite: favoriteNotifications,
    comment: commentNotifications,
    dm: dmList,
    follower: followerNotifications,
    system: systemNotifications
  }
  return map[currentTab.value]?.value || []
})

const unreadCounts = computed(() => {
  return {
    like: likeNotifications.value.filter(n => n.unread).length,
    favorite: favoriteNotifications.value.filter(n => n.unread).length,
    comment: commentNotifications.value.filter(n => n.unread).length,
    dm: dmList.value.filter(n => n.unread).length,
    follower: followerNotifications.value.filter(n => n.unread).length,
    system: systemNotifications.value.filter(n => n.unread).length
  }
})

const toggleFollow = (item) => {
  item.followed = !item.followed
}

const goBack = () => {
  emit('back')
}

const viewArticle = (id) => {
  console.log('查看文章:', id)
}

const viewDM = (id) => {
  console.log('打开私信:', id)
}
</script>

<template>
  <section class="message-feed">
    <!-- 头部 -->
    <div class="feed-header">
      <button class="back-btn" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
        <span>返回</span>
      </button>
      <div class="header-title">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
          <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
        </svg>
        <h2>消息中心</h2>
      </div>
    </div>

    <!-- 标签导航 -->
    <div class="tab-nav">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        :class="['tab-item', { active: currentTab === tab.id }]"
        @click="switchTab(tab.id)"
      >
        <span class="tab-label">{{ tab.label }}</span>
        <span v-if="unreadCounts[tab.id] > 0" class="tab-badge">{{ unreadCounts[tab.id] }}</span>
      </button>
    </div>

    <!-- ---- 点赞列表 ---- -->
    <div v-if="currentTab === 'like'" class="notification-list">
      <div v-for="item in likeNotifications" :key="item.id" :class="['notif-item', { unread: item.unread }]" @click="viewArticle(item.id)">
        <img :src="item.user.avatar || defaultAvatar" :alt="item.user.name" class="notif-avatar" />
        <div class="notif-body">
          <div class="notif-text">
            <strong class="notif-username">{{ item.user.name }}</strong>
            <span class="notif-action">赞了你的文章</span>
            <span class="notif-target">《{{ item.target }}》</span>
          </div>
          <span class="notif-time">{{ formatTime(item.time) }}</span>
        </div>
        <div class="notif-dot" v-if="item.unread"></div>
      </div>
    </div>

    <!-- ---- 收藏列表 ---- -->
    <div v-else-if="currentTab === 'favorite'" class="notification-list">
      <div v-for="item in favoriteNotifications" :key="item.id" :class="['notif-item', { unread: item.unread }]" @click="viewArticle(item.id)">
        <img :src="item.user.avatar || defaultAvatar" :alt="item.user.name" class="notif-avatar" />
        <div class="notif-body">
          <div class="notif-text">
            <strong class="notif-username">{{ item.user.name }}</strong>
            <span class="notif-action">收藏了你的文章</span>
            <span class="notif-target">《{{ item.target }}》</span>
          </div>
          <span class="notif-time">{{ formatTime(item.time) }}</span>
        </div>
        <div class="notif-dot" v-if="item.unread"></div>
      </div>
    </div>

    <!-- ---- 评论列表 ---- -->
    <div v-else-if="currentTab === 'comment'" class="notification-list">
      <div v-for="item in commentNotifications" :key="item.id" :class="['notif-item', { unread: item.unread }]" @click="viewArticle(item.id)">
        <img :src="item.user.avatar || defaultAvatar" :alt="item.user.name" class="notif-avatar" />
        <div class="notif-body">
          <div class="notif-text">
            <strong class="notif-username">{{ item.user.name }}</strong>
            <span class="notif-action">评论了你的文章</span>
            <span class="notif-target">《{{ item.target }}》</span>
          </div>
          <p class="notif-comment-preview">"{{ item.comment }}"</p>
          <span class="notif-time">{{ formatTime(item.time) }}</span>
        </div>
        <div class="notif-dot" v-if="item.unread"></div>
      </div>
    </div>

    <!-- ---- 私信列表 ---- -->
    <div v-else-if="currentTab === 'dm'" class="notification-list">
      <div v-for="item in dmList" :key="item.id" :class="['notif-item dm-item', { unread: item.unread }]" @click="viewDM(item.id)">
        <div class="dm-avatar-wrapper">
          <img :src="item.user.avatar || defaultAvatar" :alt="item.user.name" class="notif-avatar" />
        </div>
        <div class="notif-body">
          <div class="dm-header">
            <strong class="notif-username">{{ item.user.name }}</strong>
            <span class="notif-time">{{ formatTime(item.time) }}</span>
          </div>
          <p class="notif-dm-preview">{{ item.message }}</p>
        </div>
        <div class="notif-dot" v-if="item.unread"></div>
      </div>
    </div>

    <!-- ---- 新增粉丝列表 ---- -->
    <div v-else-if="currentTab === 'follower'" class="notification-list">
      <div v-for="item in followerNotifications" :key="item.id" :class="['notif-item', { unread: item.unread }]">
        <img :src="item.user.avatar || defaultAvatar" :alt="item.user.name" class="notif-avatar" />
        <div class="notif-body">
          <div class="notif-text">
            <strong class="notif-username">{{ item.user.name }}</strong>
            <span class="notif-action">关注了你</span>
          </div>
          <span class="notif-bio">{{ item.user.bio }}</span>
          <span class="notif-time">{{ formatTime(item.time) }}</span>
        </div>
        <button
          :class="['btn-follow', { followed: item.followed }]"
          @click.stop="toggleFollow(item)"
        >
          {{ item.followed ? '已关注' : '回关' }}
        </button>
        <div class="notif-dot" v-if="item.unread"></div>
      </div>
    </div>

    <!-- ---- 系统通知列表 ---- -->
    <div v-else-if="currentTab === 'system'" class="notification-list">
      <div v-for="item in systemNotifications" :key="item.id" :class="['notif-item system-item', { unread: item.unread }]">
        <div class="system-icon" :class="item.type">
          <!-- info -->
          <svg v-if="item.type === 'info'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/>
          </svg>
          <!-- success -->
          <svg v-else-if="item.type === 'success'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>
          </svg>
          <!-- warning -->
          <svg v-else-if="item.type === 'warning'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>
        </div>
        <div class="notif-body">
          <div class="system-header">
            <strong class="system-title">{{ item.title }}</strong>
            <span class="notif-time">{{ formatTime(item.time) }}</span>
          </div>
          <p class="system-content">{{ item.content }}</p>
        </div>
        <div class="notif-dot" v-if="item.unread"></div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="currentList.length === 0" class="empty-state">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="empty-icon">
        <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/>
      </svg>
      <p>暂无通知</p>
    </div>
  </section>
</template>

<style scoped lang="scss">
.message-feed {
  min-width: 0;
  height: 100%;
  overflow-y: auto;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
    transform: translateY(-2px);
  }

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(249, 115, 22, 0.3);
    border-radius: 4px;

    &:hover {
      background: #F97316;
    }
  }
}

// 头部
.feed-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: 1px solid transparent;
  border-radius: 6px;
  background: transparent;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;

  svg {
    width: 16px;
    height: 16px;
  }

  &:hover {
    border-color: #F97316;
    color: #F97316;
  }
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;

  svg {
    width: 20px;
    height: 20px;
    stroke: #F97316;
  }

  h2 {
    font-size: 18px;
    font-weight: 700;
    color: #1a1a1a;
    margin: 0;
  }
}

// 标签导航
.tab-nav {
  display: flex;
  gap: 4px;
  margin-bottom: 16px;
  padding: 4px;
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.02);
}

.tab-item {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 6px;
  border: none;
  border-radius: 8px;
  background: transparent;
  font-size: 13px;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    color: #333;
    background: rgba(249, 115, 22, 0.04);
  }

  &.active {
    background: #fff;
    color: #F97316;
    font-weight: 600;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  .tab-badge {
    min-width: 18px;
    height: 18px;
    padding: 0 5px;
    background: #F97316;
    border-radius: 9px;
    color: white;
    font-size: 10px;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: center;
    line-height: 1;
  }
}

// 通知列表
.notification-list {
  display: flex;
  flex-direction: column;
}

.notif-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;

  &:hover {
    background: rgba(249, 115, 22, 0.03);
    padding-left: 16px;
  }

  &.unread {
    background: rgba(249, 115, 22, 0.04);
  }

  + .notif-item {
    border-top: 1px solid #f8f8f8;
  }
}

.notif-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.notif-body {
  flex: 1;
  min-width: 0;
}

.notif-text {
  font-size: 14px;
  line-height: 1.5;
  color: #333;
  margin-bottom: 4px;

  .notif-username {
    color: #1a1a1a;
    font-weight: 600;
  }

  .notif-action {
    color: #555;
    margin: 0 4px;
  }

  .notif-target {
    color: #F97316;
    cursor: pointer;

    &:hover {
      text-decoration: underline;
    }
  }
}

.notif-time {
  font-size: 12px;
  color: #999;
}

.notif-comment-preview {
  margin: 6px 0 4px;
  padding: 8px 12px;
  background: rgba(0, 0, 0, 0.02);
  border-radius: 6px;
  font-size: 13px;
  line-height: 1.5;
  color: #666;
  font-style: italic;
}

.notif-dot {
  position: absolute;
  top: 18px;
  right: 12px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #F97316;
  flex-shrink: 0;
}

// 私信专属样式
.dm-item {
  align-items: center;
}

.dm-avatar-wrapper {
  position: relative;
}

.dm-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.notif-dm-preview {
  font-size: 13px;
  color: #888;
  line-height: 1.5;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 380px;
}

// 粉丝专属样式
.notif-bio {
  display: block;
  font-size: 12px;
  color: #999;
  margin: 4px 0;
}

.btn-follow {
  padding: 6px 14px;
  border: 1px solid #F97316;
  border-radius: 6px;
  background: transparent;
  color: #F97316;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  flex-shrink: 0;
  align-self: center;

  &:hover {
    background: #F97316;
    color: white;
    box-shadow: 0 4px 12px rgba(249, 115, 22, 0.25);
    transform: translateY(-1px);
  }

  &.followed {
    border-color: #e5e7eb;
    color: #999;
    background: #f9fafb;

    &:hover {
      background: #f3f4f6;
      color: #666;
      box-shadow: none;
    }
  }
}

// 系统通知专属样式
.system-item {
  .system-icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    svg {
      width: 20px;
      height: 20px;
    }

    &.info {
      background: rgba(59, 130, 246, 0.1);
      color: #3B82F6;
    }

    &.success {
      background: rgba(16, 185, 129, 0.1);
      color: #10B981;
    }

    &.warning {
      background: rgba(245, 158, 11, 0.1);
      color: #F59E0B;
    }
  }

  .system-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 4px;
  }

  .system-title {
    font-size: 14px;
    color: #1a1a1a;
  }

  .system-content {
    font-size: 13px;
    color: #888;
    line-height: 1.5;
    margin: 0;
  }
}

// 空状态
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;

  .empty-icon {
    width: 56px;
    height: 56px;
    margin-bottom: 12px;
    stroke: #ddd;
  }

  p {
    font-size: 14px;
    margin: 0;
  }
}
</style>
