<!--
  登录/注册卡片容器
  提供白色卡片样式包装，通过 slot 嵌入表单内容
  @author aceFelix
-->
<template>
  <div class="auth-form-section">
    <div class="auth-card">
      <slot></slot>
    </div>
  </div>
</template>

<style scoped lang="scss">
.auth-form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.auth-card {
  width: 100%;
  max-width: 420px;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  transition: all 0.3s;
}
</style>
