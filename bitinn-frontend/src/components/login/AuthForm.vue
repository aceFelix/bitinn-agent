<!--
  登录/注册表单
  支持登录、注册、忘记密码三种模式切换，表单验证和提交
  @author aceFelix
-->
<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userLoginService, userRegisterService } from '@/api/user'
import { useTokenStore } from '@/stores/token'
import { userInfoStore } from '@/stores/userInfo'

const router = useRouter()
const tokenStore = useTokenStore()
const userStore = userInfoStore()

const viewMode = defineModel({ default: 'login' })

const isLogin = computed(() => viewMode.value === 'login')
const isRegister = computed(() => viewMode.value === 'register')
const isForgot = computed(() => viewMode.value === 'forgot')

const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const email = ref('')
const verificationCode = ref('')

// 密码显示/隐藏状态
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const showNewPassword = ref(false)
const showForgotNewPassword = ref(false)
const showForgotConfirmPassword = ref(false)

const countdown = ref(0)
const isSendingCode = ref(false)
const isSubmitting = ref(false)

const toggleMode = () => {
  viewMode.value = viewMode.value === 'login' ? 'register' : 'login'
  resetForm()
}

const goToForgot = () => {
  viewMode.value = 'forgot'
  resetForm()
}

const backToLogin = () => {
  viewMode.value = 'login'
  resetForm()
}

const resetForm = () => {
  username.value = ''
  password.value = ''
  confirmPassword.value = ''
  email.value = ''
  verificationCode.value = ''
  countdown.value = 0
  showPassword.value = false
  showConfirmPassword.value = false
  showNewPassword.value = false
  showForgotNewPassword.value = false
  showForgotConfirmPassword.value = false
}

const sendVerificationCode = async () => {
  if (!email.value) {
    ElMessage.warning('请先输入邮箱地址')
    return
  }
  if (countdown.value > 0) return

  isSendingCode.value = true
  console.log('发送验证码到:', email.value)

  setTimeout(() => {
    isSendingCode.value = false
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
    ElMessage.success('验证码已发送，请查收邮件')
  }, 1000)
}

const handleSubmit = async () => {
  if (isSubmitting.value) return
  isSubmitting.value = true
  
  try {
    if (viewMode.value === 'login') {
      const res = await userLoginService({ username: username.value, password: password.value })
      tokenStore.setToken(res.data)
      const userInfoRes = await import('@/api/user').then(m => m.getUserInfoService())
      userStore.setUserInfo(userInfoRes.data)
      ElMessage.success('登录成功！')
      router.push('/')
    } else if (viewMode.value === 'register') {
      if (password.value !== confirmPassword.value) {
        ElMessage.warning('两次输入的密码不一致')
        return
      }
      if (password.value.length < 6 || password.value.length > 20) {
        ElMessage.warning('密码长度必须在6-20位之间')
        return
      }
      if (username.value.length < 3 || username.value.length > 16) {
        ElMessage.warning('用户名长度必须在3-16位之间')
        return
      }
      await userRegisterService({
        username: username.value,
        password: password.value,
        email: email.value
      })
      ElMessage.success('注册成功！请登录')
      backToLogin()
    } else if (viewMode.value === 'forgot') {
      if (password.value !== confirmPassword.value) {
        ElMessage.warning('两次输入的密码不一致')
        return
      }
      console.log('重置密码:', email.value, verificationCode.value, password.value)
      ElMessage.success('密码重置成功！')
      backToLogin()
    }
  } catch (error) {
    console.error('操作失败:', error)
  } finally {
    isSubmitting.value = false
  }
}

const socialLogin = (provider) => {
  window.location.href = `/api/oauth2/authorization/${provider}`
}
</script>

<template>
  <div>
    <div class="auth-header">
      <h2 class="auth-title">
        {{ isForgot ? '重置密码' : (isLogin ? '欢迎回来' : '创建账号') }}
      </h2>
      <p class="auth-subtitle">
        {{ isForgot ? '通过邮箱验证重置您的密码' : (isLogin ? '登录以继续你的技术之旅' : '加入我们的开发者社区') }}
      </p>
    </div>

    <form class="auth-form" @submit.prevent="handleSubmit">
      <!-- 登录模式 -->
      <template v-if="isLogin">
        <div class="form-group inline">
          <label class="form-label">用户名：</label>
          <div class="input-wrapper">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            <input v-model="username" type="text" class="form-input" placeholder="请输入用户名" required />
          </div>
        </div>

        <div class="form-group inline">
          <label class="form-label">密码：</label>
          <div class="input-wrapper">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            <input v-model="password" :type="showPassword ? 'text' : 'password'" class="form-input has-toggle" placeholder="请输入密码" required />
            <button type="button" class="toggle-password" @click="showPassword = !showPassword" tabindex="-1">
              <svg v-if="showPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                <line x1="1" y1="1" x2="23" y2="23"/>
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                <circle cx="12" cy="12" r="3"/>
              </svg>
            </button>
          </div>
        </div>

        <div class="form-options">
          <label class="remember-me">
            <input type="checkbox" />
            <span>记住我</span>
          </label>
          <button type="button" class="forgot-password" @click="goToForgot">忘记密码？</button>
        </div>
      </template>

      <!-- 忘记密码模式 -->
      <template v-else-if="isForgot">
        <div class="form-group inline">
          <label class="form-label">邮箱：</label>
          <div class="input-wrapper">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
              <polyline points="22,6 12,13 2,6"/>
            </svg>
            <input v-model="email" type="email" class="form-input" placeholder="请输入注册邮箱" required />
          </div>
        </div>

        <div class="form-group inline">
          <label class="form-label">验证码：</label>
          <div class="input-wrapper verification-wrapper">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            <input v-model="verificationCode" type="text" class="form-input" placeholder="请输入验证码" maxlength="6" required />
            <button type="button" class="btn-verification" :disabled="countdown > 0 || isSendingCode" @click="sendVerificationCode">
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </button>
          </div>
        </div>

        <div class="form-group inline">
          <label class="form-label">新密码：</label>
          <div class="input-wrapper">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            <input v-model="password" :type="showForgotNewPassword ? 'text' : 'password'" class="form-input has-toggle" placeholder="请输入新密码" required />
            <button type="button" class="toggle-password" @click="showForgotNewPassword = !showForgotNewPassword" tabindex="-1">
              <svg v-if="showForgotNewPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                <line x1="1" y1="1" x2="23" y2="23"/>
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                <circle cx="12" cy="12" r="3"/>
              </svg>
            </button>
          </div>
        </div>

        <div class="form-group inline">
          <label class="form-label">确认密码：</label>
          <div class="input-wrapper">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            <input v-model="confirmPassword" :type="showForgotConfirmPassword ? 'text' : 'password'" class="form-input has-toggle" placeholder="请再次输入新密码" required />
            <button type="button" class="toggle-password" @click="showForgotConfirmPassword = !showForgotConfirmPassword" tabindex="-1">
              <svg v-if="showForgotConfirmPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                <line x1="1" y1="1" x2="23" y2="23"/>
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                <circle cx="12" cy="12" r="3"/>
              </svg>
            </button>
          </div>
        </div>
      </template>

      <!-- 注册模式 -->
      <template v-else-if="isRegister">
        <div class="form-group inline">
          <label class="form-label">用户名：</label>
          <div class="input-wrapper">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            <input v-model="username" type="text" class="form-input" placeholder="请输入用户名" required />
          </div>
        </div>

        <div class="form-group inline">
          <label class="form-label">邮箱：</label>
          <div class="input-wrapper">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
              <polyline points="22,6 12,13 2,6"/>
            </svg>
            <input v-model="email" type="email" class="form-input" placeholder="请输入邮箱" required />
          </div>
        </div>

        <div class="form-group inline">
          <label class="form-label">密码：</label>
          <div class="input-wrapper">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            <input v-model="password" :type="showPassword ? 'text' : 'password'" class="form-input has-toggle" placeholder="请输入密码" required />
            <button type="button" class="toggle-password" @click="showPassword = !showPassword" tabindex="-1">
              <svg v-if="showPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                <line x1="1" y1="1" x2="23" y2="23"/>
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                <circle cx="12" cy="12" r="3"/>
              </svg>
            </button>
          </div>
        </div>

        <div class="form-group inline">
          <label class="form-label">确认密码：</label>
          <div class="input-wrapper">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            <input v-model="confirmPassword" :type="showConfirmPassword ? 'text' : 'password'" class="form-input has-toggle" placeholder="请再次输入密码" required />
            <button type="button" class="toggle-password" @click="showConfirmPassword = !showConfirmPassword" tabindex="-1">
              <svg v-if="showConfirmPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                <line x1="1" y1="1" x2="23" y2="23"/>
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                <circle cx="12" cy="12" r="3"/>
              </svg>
            </button>
          </div>
        </div>
      </template>

      <button type="submit" class="btn-submit" :disabled="isSubmitting">
        <span v-if="isSubmitting" class="loading-spinner"></span>
        {{ isSubmitting ? '处理中...' : (isForgot ? '重置密码' : (isLogin ? '登录' : '注册')) }}
      </button>
    </form>

    <!-- 社交登录 -->
    <div v-if="isLogin" class="social-login">
      <div class="divider">
        <span>其他登录方式</span>
      </div>
      <div class="social-icons">
        <button class="social-btn github" @click="socialLogin('github')" title="GitHub 登录">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
          </svg>
        </button>
        <button class="social-btn gitee" @click="socialLogin('gitee')" title="Gitee 登录">
          <svg viewBox="0 0 1024 1024" fill="currentColor">
            <path d="M512 1024C229.2224 1024 0 794.7776 0 512S229.2224 0 512 0s512 229.2224 512 512-229.2224 512-512 512z m259.1488-568.8832H480.4096a25.2928 25.2928 0 0 0-25.2928 25.2928l-0.0256 63.2064c0 13.952 11.3152 25.2928 25.2672 25.2928h177.024c13.9776 0 25.2928 11.3152 25.2928 25.2672v12.6464a75.8528 75.8528 0 0 1-75.8528 75.8528H366.592a25.2928 25.2928 0 0 1-25.2672-25.2928v-240.1792a75.8528 75.8528 0 0 1 75.8272-75.8528h353.9456a25.2928 25.2928 0 0 0 25.2672-25.2928l0.0768-63.2064a25.2928 25.2928 0 0 0-25.2672-25.2928H417.152a189.6192 189.6192 0 0 0-189.6192 189.6448v353.9456c0 13.9776 11.3152 25.2928 25.2928 25.2928h372.9408a170.6496 170.6496 0 0 0 170.6496-170.6496v-145.408a25.2928 25.2928 0 0 0-25.2928-25.2672z"/>
          </svg>
        </button>
        <button class="social-btn wechat" @click="socialLogin('wechat')" title="微信登录">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178A1.17 1.17 0 0 1 4.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178 1.17 1.17 0 0 1-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.942 2.453 3.666 4.229 6.884 4.229.826 0 1.622-.12 2.361-.336a.722.722 0 0 1 .598.082l1.584.926a.272.272 0 0 0 .14.047c.134 0 .24-.111.24-.247 0-.06-.023-.12-.038-.177l-.327-1.233a.582.582 0 0 1-.023-.156.49.49 0 0 1 .201-.398C23.024 18.48 24 16.82 24 14.98c0-3.21-2.931-5.837-6.656-6.088V8.89c-.135-.01-.27-.027-.407-.032zm-2.53 3.274c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.97-.982zm4.844 0c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.969-.982z"/>
          </svg>
        </button>
        <button class="social-btn qq" @click="socialLogin('qq')" title="QQ 登录">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M12.003 2c-2.265 0-6.29 1.364-6.29 7.325v1.195S3.55 14.96 3.55 17.474c0 .665.17 1.025.281 1.025.114 0 .902-.484 1.748-2.072 0 0-.18 2.197 1.904 3.967 0 0-1.77.495-1.77 1.182 0 .686 4.078.43 6.29.43 2.21 0 6.287.257 6.287-.43 0-.687-1.768-1.182-1.768-1.182 2.085-1.77 1.905-3.967 1.905-3.967.845 1.588 1.634 2.072 1.746 2.072.111 0 .283-.36.283-1.025 0-2.514-2.166-6.954-2.166-6.954V9.325C18.29 3.364 14.268 2 12.003 2z"/>
          </svg>
        </button>
      </div>
    </div>

    <div class="auth-footer">
      <p class="toggle-text" v-if="isForgot">
        想起密码了？
        <button type="button" class="btn-toggle" @click="backToLogin">返回登录</button>
      </p>
      <p class="toggle-text" v-else>
        {{ isLogin ? '还没有账号？' : '已有账号？' }}
        <button type="button" class="btn-toggle" @click="toggleMode">
          {{ isLogin ? '立即注册' : '立即登录' }}
        </button>
      </p>
    </div>
  </div>
</template>

<style scoped lang="scss">
.auth-header {
  text-align: center;
  margin-bottom: 24px;
}

.auth-title {
  font-size: 26px;
  font-weight: 600;
  margin-bottom: 6px;
}

.auth-subtitle {
  font-size: 14px;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;

  &.inline {
    flex-direction: row;
    align-items: center;
    gap: 12px;

    .form-label {
      flex-shrink: 0;
      margin-bottom: 0;
      width: auto;
    }

    .input-wrapper {
      flex: 1;
    }

    .form-input {
      height: 44px;
    }

    &:hover .form-label {
      box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
      transform: translateY(-2px);
    }

    &:has(.form-input:focus) .form-label {
      box-shadow: 0 8px 24px rgba(249, 115, 22, 0.4);
      transform: translateY(-2px);
    }
  }
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 12px;
  border-radius: 8px;
  min-width: 80px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 12px;
  width: 18px;
  height: 18px;
  pointer-events: none;
  transition: all 0.2s;
  z-index: 2;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;

  &:hover .input-icon,
  &:focus-within .input-icon {
    stroke: #F97316;
  }
}

.form-input {
  width: 100%;
  height: 42px;
  padding: 0 14px 0 40px;
  border-radius: 8px;
  font-size: 14px;
  border: 1px solid transparent;
  transition: all 0.2s;

  &.has-toggle {
    padding-right: 42px;
  }

  &:focus {
    outline: none;
    background: #fff9f5;
    box-shadow: inset 0 2px 6px rgba(249, 115, 22, 0.2);
    transform: scale(1.01);
    border-color: #F97316;
  }

  &:hover:not(:focus) {
    background: #fff9f5;
    border-color: #F97316;
    box-shadow: inset 0 2px 6px rgba(249, 115, 22, 0.15);
  }
}

.toggle-password {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  z-index: 2;

  svg {
    width: 18px;
    height: 18px;
    stroke: #999;
    transition: stroke 0.2s;
  }

  &:hover svg {
    stroke: #F97316;
  }
}

.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;

  input[type="checkbox"] {
    width: 16px;
    height: 16px;
    cursor: pointer;
    accent-color: #F97316;
  }
}

.forgot-password {
  text-decoration: none;
  transition: all 0.2s;
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;

  &:hover {
    box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
    transform: translateY(-2px);
    color: #F97316;
  }
}

.btn-submit {
  height: 42px;
  border-radius: 8px;
  color: white;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  margin-top: 4px;
  border: none;
  background: #F97316;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;

  &:hover:not(:disabled) {
    box-shadow: 0 8px 24px rgba(249, 115, 22, 0.4);
    transform: translateY(-2px);
  }

  &:active:not(:disabled) {
    transform: translateY(0);
  }

  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }

  .loading-spinner {
    width: 16px;
    height: 16px;
    border: 2px solid rgba(255, 255, 255, 0.3);
    border-top-color: white;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.verification-wrapper {
  display: flex;
  gap: 10px;

  .form-input {
    flex: 1;
  }
}

.btn-verification {
  height: 42px;
  padding: 0 14px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  border: 1px solid #F97316;
  background: transparent;

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  &:hover:not(:disabled) {
    box-shadow: 0 8px 24px rgba(249, 115, 22, 0.4);
    transform: translateY(-2px);
    background: #F97316;
    color: white;
  }
}

.social-login {
  margin-top: 16px;
}

.divider {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  
  &::before,
  &::after {
    content: '';
    flex: 1;
    height: 1px;
  }
  
  span {
    font-size: 13px;
    white-space: nowrap;
  }
}

.social-icons {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.social-btn {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;

  svg {
    width: 22px;
    height: 22px;
    transition: color 0.2s;
  }
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    
    svg {
      color: white;
    }
  }
  
  &.github:hover {
    background: #24292e;
  }
  
  &.gitee:hover {
    background: #c71d23;
  }
  
  &.wechat:hover {
    background: #07c160;
  }
  
  &.qq:hover {
    background: #12b7f5;
  }
}

.auth-footer {
  margin-top: 16px;
  text-align: center;
}

.toggle-text {
  font-size: 14px;
}

.btn-toggle {
  background: none;
  border: none;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  padding: 0;
  margin-left: 4px;
  transition: color 0.2s;

  &:hover {
    color: #F97316;
    text-decoration: underline;
  }
}
</style>
