<!--
  用户主页右侧栏
  展示安全设置入口（修改密码），文章总数和粉丝数统计
  @author aceFelix
-->
<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  userInfo: { type: Object, default: () => ({}) }
})

const router = useRouter()

const goToResetPassword = () => {
  router.push('/admin/user/resetPassword')
}
</script>

<template>
  <aside class="profile-right">
    <div class="security-card">
      <div class="card-header">
        <h3 class="card-title">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
          </svg>
          账号安全
        </h3>
      </div>

      <div class="security-list">
        <div class="security-item">
          <div class="security-info">
            <span class="security-name">登录密码</span>
            <span class="security-desc">定期修改密码可以保护账号安全</span>
          </div>
          <button class="btn-security" @click="goToResetPassword">
            修改密码
          </button>
        </div>

        <div class="security-item">
          <div class="security-info">
            <span class="security-name">账号状态</span>
            <span class="security-desc">{{ userInfo.status === 'ACTIVE' ? '正常使用中' : '账号异常' }}</span>
          </div>
          <span class="status-badge" :class="{ active: userInfo.status === 'ACTIVE' }">
            {{ userInfo.status === 'ACTIVE' ? '正常' : '异常' }}
          </span>
        </div>

        <div class="security-item">
          <div class="security-info">
            <span class="security-name">绑定手机</span>
            <span class="security-desc">{{ userInfo.phone || '未绑定手机号' }}</span>
          </div>
          <span class="status-hint">{{ userInfo.phone ? '已绑定' : '未绑定' }}</span>
        </div>

        <div class="security-item">
          <div class="security-info">
            <span class="security-name">安全评分</span>
            <span class="security-desc">基于你的安全设置综合评估</span>
          </div>
          <span class="score-badge">85 分</span>
        </div>
      </div>
    </div>

    <div class="tips-card">
      <h3 class="card-title">安全建议</h3>
      <ul class="tips-list">
        <li>定期修改登录密码</li>
        <li>绑定手机号增强安全性</li>
        <li>不要在公共场合泄露个人信息</li>
        <li>开启登录二次验证</li>
        <li>使用强密码并定期更换</li>
        <li>避免使用公共 WiFi 登录</li>
        <li>及时退出登录状态</li>
        <li>检查账号异常登录记录</li>
      </ul>
    </div>

    <div class="activity-card">
      <h3 class="card-title">最近活动</h3>
      <div class="activity-list">
        <div class="activity-item">
          <span class="activity-time">今天 14:30</span>
          <span class="activity-text">修改了个人简介</span>
        </div>
        <div class="activity-item">
          <span class="activity-time">昨天 09:15</span>
          <span class="activity-text">发布了新文章《Vue3 响应式原理》</span>
        </div>
        <div class="activity-item">
          <span class="activity-time">3天前</span>
          <span class="activity-text">更新了头像图片</span>
        </div>
        <div class="activity-item">
          <span class="activity-time">1周前</span>
          <span class="activity-text">绑定了手机号码</span>
        </div>
        <div class="activity-item">
          <span class="activity-time">2周前</span>
          <span class="activity-text">修改了登录密码</span>
        </div>
      </div>
    </div>

    <div class="help-card">
      <h3 class="card-title">帮助与支持</h3>
      <ul class="help-list">
        <li><a href="#">常见问题 FAQ</a></li>
        <li><a href="#">联系客服支持</a></li>
        <li><a href="#">反馈问题建议</a></li>
        <li><a href="#">用户使用协议</a></li>
        <li><a href="#">隐私政策说明</a></li>
      </ul>
    </div>
  </aside>
</template>

<style scoped lang="scss">
.profile-right {
  height: 100%;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
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

.security-card,
.tips-card,
.activity-card,
.help-card {
  border-radius: 12px;
  padding: 20px;
  background: transparent;
  box-shadow: none;

  &:hover {
    box-shadow: none;
    transform: none;
  }
}

.card-header {
  margin-bottom: 16px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
  margin-bottom: 4px;

  svg {
    width: 20px;
    height: 20px;
    stroke: #F97316;
  }
}

.security-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.security-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  transition: all 0.2s;

  &:hover {
    border-color: #F97316;
    box-shadow: 0 4px 12px rgba(249, 115, 22, 0.1);
  }

  .security-info {
    display: flex;
    flex-direction: column;
    gap: 4px;

    .security-name {
      font-size: 14px;
      font-weight: 600;
      color: #1a1a1a;
    }

    .security-desc {
      font-size: 12px;
      color: #888;
    }
  }

  .btn-security {
    padding: 6px 14px;
    border-radius: 6px;
    font-size: 12px;
    font-weight: 500;
    transition: all 0.2s;
    border: 1px solid #F97316;
    color: #F97316;
    background: transparent;
    cursor: pointer;

    &:hover {
      background: #F97316;
      color: white;
      box-shadow: 0 4px 12px rgba(249, 115, 22, 0.25);
    }
  }

  .status-badge {
    padding: 4px 12px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 500;
    background: #fef3c7;
    color: #d97706;

    &.active {
      background: #dcfce7;
      color: #16a34a;
    }
  }

  .status-hint {
    font-size: 12px;
    color: #888;
  }

  .score-badge {
    padding: 4px 12px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 600;
    background: rgba(249, 115, 22, 0.1);
    color: #F97316;
  }
}

.tips-card {
  .tips-list {
    list-style: none;
    padding: 0;
    margin: 0;

    li {
      font-size: 13px;
      color: #888;
      padding: 8px 0;
      transition: all 0.2s;

      &:hover {
        padding-left: 8px;
        color: #F97316;
      }

      &::before {
        content: '•';
        margin-right: 8px;
        color: #F97316;
        font-weight: bold;
      }
    }
  }
}

.activity-card {
  .activity-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .activity-item {
    display: flex;
    flex-direction: column;
    gap: 4px;
    padding: 10px;
    border-radius: 6px;
    background: rgba(0, 0, 0, 0.02);
    transition: all 0.2s;

    &:hover {
      background: rgba(249, 115, 22, 0.05);
    }

    .activity-time {
      font-size: 11px;
      color: #999;
    }

    .activity-text {
      font-size: 13px;
      color: #555;
      line-height: 1.4;
    }
  }
}

.help-card {
  .help-list {
    list-style: none;
    padding: 0;
    margin: 0;

    li {
      a {
        display: block;
        padding: 8px 0;
        font-size: 13px;
        color: #555;
        text-decoration: none;
        transition: all 0.2s;

        &:hover {
          color: #F97316;
          padding-left: 8px;
        }

        &::before {
          content: '→';
          margin-right: 8px;
          color: #F97316;
          opacity: 0;
          transition: opacity 0.2s;
        }

       :hover::before {
          opacity: 1;
        }
      }
    }
  }
}
</style>
