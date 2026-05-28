/**
 * Vue Router 路由配置
 * 使用路由懒加载优化首屏性能，包含公开页面和后台管理页面的路由守卫
 * @author aceFelix
 */
import { createRouter, createWebHistory } from 'vue-router'
import { useTokenStore } from '@/stores/token'

// 路由懒加载：首屏只加载必要资源，非必要路由按需加载
const LoginVue = () => import('@/views/login/Login.vue')
const HomeVue = () => import('@/views/Home.vue')
const StartCreateVue = () => import('@/views/article/StartCreate.vue')
const ArticleEditVue = () => import('@/views/article/ArticleEdit.vue')
const ArticleDetailVue = () => import('@/views/article/ArticleDetail.vue')
const UserAvatarVue = () => import('@/views/user/UserAvatar.vue')
const UserInfoVue = () => import('@/views/user/UserInfo.vue')
const UserResetPasswordVue = () => import('@/views/user/UserResetPassword.vue')
const UserProfileVue = () => import('@/views/user/UserProfile.vue')
const AiChatVue = () => import('@/views/AiChat.vue')

const routes = [
  { path: '/login', component: LoginVue },
  { path: '/', component: HomeVue },
  { path: '/create', component: StartCreateVue },
  { path: '/article/edit', component: ArticleEditVue },
  { path: '/article/:id', component: ArticleDetailVue },
  { path: '/profile', component: UserProfileVue },
  { path: '/ai-chat', component: AiChatVue },
  { path: '/admin/user/avatar', component: UserAvatarVue },
  { path: '/admin/user/info', component: UserInfoVue },
  { path: '/admin/user/resetPassword', component: UserResetPasswordVue },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const tokenStore = useTokenStore()

  if (to.path === '/login') {
    return tokenStore.token ? '/' : true
  }

  const publicPages = ['/', '/create', '/ai-chat']
  const isPublicPage = publicPages.includes(to.path) || to.path.startsWith('/article/')
  
  if (isPublicPage) {
    return true
  }

  return tokenStore.token ? true : '/login'
})

export default router
