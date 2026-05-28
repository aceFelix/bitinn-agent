<!--
  修改密码页面
  验证原密码后设置新密码（6-20 位）
  @author aceFelix
-->
<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { userResetPasswordService } from '@/api/user'

const form = ref({
  oldPwd: '',
  newPwd: '',
  rePwd: ''
})

const loading = ref(false)

const handleSubmit = async () => {
  if (!form.value.oldPwd) {
    ElMessage.warning('请输入原密码')
    return
  }
  if (!form.value.newPwd || form.value.newPwd.length < 6 || form.value.newPwd.length > 20) {
    ElMessage.warning('新密码长度必须在6-20位之间')
    return
  }
  if (form.value.newPwd !== form.value.rePwd) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }

  loading.value = true
  try {
    await userResetPasswordService(form.value)
    ElMessage.success('密码修改成功')
    form.value = { oldPwd: '', newPwd: '', rePwd: '' }
  } catch (error) {
    console.error('修改密码失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page">
    <h2>重置密码</h2>
    <el-form :model="form" label-width="100px" class="form">
      <el-form-item label="原密码">
        <el-input v-model="form.oldPwd" type="password" placeholder="请输入原密码" show-password />
      </el-form-item>
      <el-form-item label="新密码">
        <el-input v-model="form.newPwd" type="password" placeholder="请输入新密码(6-20位)" show-password />
      </el-form-item>
      <el-form-item label="确认新密码">
        <el-input v-model="form.rePwd" type="password" placeholder="请再次输入新密码" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSubmit" :loading="loading">修改密码</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.page {
  padding: 20px;
}

.form {
  max-width: 400px;
  margin-top: 20px;
}
</style>
