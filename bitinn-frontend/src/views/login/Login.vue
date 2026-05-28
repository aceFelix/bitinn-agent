<!--
  登录/注册页面
  包含品牌展示区和登录/注册表单
  @author aceFelix
-->
<script setup>
import LoginBrand from '@/components/login/LoginBrand.vue'
import AuthCard from '@/components/login/AuthCard.vue'
import AuthForm from '@/components/login/AuthForm.vue'
</script>

<template>
  <div class="auth-container">
    <LoginBrand />
    <AuthCard>
      <AuthForm />
    </AuthCard>
  </div>
</template>

<style scoped lang="scss">
.auth-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}
</style>

<style>
html, body {
  margin: 0 !important;
  padding: 0 !important;
  overflow: hidden !important;
}
</style>
