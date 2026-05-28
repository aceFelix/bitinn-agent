<!--
  首页右侧边栏
  展示推荐用户列表、创作入口和关注用户功能
  @author aceFelix
-->
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 推荐用户（静态演示数据，已缓存）
const recommendedUsers = ref([
  { name: '技术大咖', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=10', bio: '全栈开发者' },
  { name: '代码诗人', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=11', bio: '开源贡献者' },
  { name: '架构师', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=12', bio: '云原生专家' }
])

// 导航到创作引导页
const startCreate = () => {
  router.push('/create')
}

// 关注用户（防抖防止重复点击）
let followDebounceTimer = null
const followUser = (user) => {
  clearTimeout(followDebounceTimer)
  followDebounceTimer = setTimeout(() => {
    console.log('关注用户:', user.name)
  }, 500)
}
</script>

<template>
  <aside class="sidebar-right">
    <!-- 社区介绍 -->
    <div class="card community-card">
      <h3 class="card-title">
        <span class="logo-bracket">&lt;</span>
        <span class="logo-text">欢迎来到BitInn</span>
        <span class="logo-bracket">/&gt;</span>
      </h3>
      <p class="card-desc">
        一个为开发者打造的技术社区，在这里分享知识、交流经验、共同成长。
      </p>
      <button class="btn-primary" @click="startCreate">开始创作</button>
    </div>

    <!-- 推荐关注 -->
    <div class="card">
      <h3 class="card-title">推荐关注</h3>
      <div class="users-list">
        <div 
          v-for="user in recommendedUsers" 
          :key="user.name"
          class="user-item"
        >
          <img :src="user.avatar" :alt="user.name" class="user-avatar">
          <div class="user-info">
            <span class="user-name">{{ user.name }}</span>
            <span class="user-bio">{{ user.bio }}</span>
          </div>
          <button class="btn-follow" @click="followUser(user)">关注</button>
        </div>
      </div>
    </div>

    <!-- 社区规则 -->
    <div class="card rules-card">
      <h3 class="card-title">社区公约</h3>
      <ul class="rules-list">
        <li>尊重他人，友善交流</li>
        <li>分享有价值的技术内容</li>
        <li>禁止发布垃圾信息</li>
        <li>保护个人隐私</li>
      </ul>
    </div>

    <!-- 页脚链接 -->
    <footer class="footer-links">
      <a href="#">关于我们</a>
      <a href="#">使用条款</a>
      <a href="#">隐私政策</a>
      <a href="#">联系我们</a>
      <p class="copyright">© 2026 BitInn</p>
    </footer>
  </aside>
</template>

<style scoped lang="scss">
.sidebar-right {
  height: 100%;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
  border: none;
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
}

.card {
  border: none;
  border-radius: 10px;
  padding: 16px;
  transition: all 0.2s;
  
  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  }
  
  .card-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 12px;
    display: flex;
    align-items: center;
    gap: 8px;
    
  }
  
  .card-desc {
    font-size: 14px;
    line-height: 1.6;
    margin-bottom: 16px;
  }
}

.community-card {
  text-align: center;

  .card-title {
    justify-content: center;
  }

  .btn-primary {
    width: 100%;
    padding: 10px 16px;
    border-radius: 8px;
    color: white;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    border: none;
    background: #F97316;
    
    &:hover {
      box-shadow: 0 8px 24px rgba(249, 115, 22, 0.4);
      transform: translateY(-2px);
    }
  }
}

.users-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.2s;
  
  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  }
  
  .user-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
  }
  
  .user-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 2px;
    
    .user-name {
      font-size: 14px;
      font-weight: 500;
    }
    
    .user-bio {
      font-size: 12px;
    }
  }
  
  .btn-follow {
    padding: 6px 12px;
    background: transparent;
    border: 1px solid #F97316;
    border-radius: 6px;
    font-size: 12px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      box-shadow: 0 8px 24px rgba(249, 115, 22, 0.4);
      transform: translateY(-2px);
      background: #F97316;
      color: white;
    }
  }
}

.rules-card {
  .rules-list {
    list-style: none;
    padding: 0;
    margin: 0;
    
    li {
      font-size: 13px;
      padding: 8px 0;
      transition: all 0.2s;
      
      &:last-child {
        border-bottom: none;
      }
      
      &:hover {
        padding-left: 8px;
      }
      
      &::before {
        content: '•';
        margin-right: 8px;
      }
    }
  }
}

.footer-links {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 16px;

  a {
    font-size: 12px;
    text-decoration: none;
    transition: color 0.2s;
    color: #1a1a1a;

    &:hover {
      text-decoration: underline;
      color: #F97316;
    }
  }

  .copyright {
    width: 100%;
    font-size: 12px;
    margin-top: 8px;
    color: #1a1a1a;
  }
}
</style>
