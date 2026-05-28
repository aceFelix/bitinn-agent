<!--
  修改个人信息页面
  编辑昵称和邮箱，更新后同步到 Pinia Store
  @author aceFelix
-->
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { userInfoUpdateService, getUserInfoService } from '@/api/user'
import { userInfoStore } from '@/stores/userInfo'

const userStore = userInfoStore()

const userInfo = ref({
  nickname: '',
  email: ''
})

const loading = ref(false)

const fetchUserInfo = async () => {
  try {
    const res = await getUserInfoService()
    userInfo.value = {
      nickname: res.data.nickname,
      email: res.data.email
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

const handleSubmit = async () => {
  loading.value = true
  try {
    await userInfoUpdateService(userInfo.value)
    userStore.setUserInfo({
      ...userStore.userInfo,
      nickname: userInfo.value.nickname,
      email: userInfo.value.email
    })
    ElMessage.success('更新成功')
  } catch (error) {
    console.error('更新失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<template>
  <div class="page">
    <h2>用户信息</h2>
    <el-form :model="userInfo" label-width="80px" class="form">
      <el-form-item label="昵称">
        <el-input v-model="userInfo.nickname" placeholder="请输入昵称" maxlength="10" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="userInfo.email" placeholder="请输入邮箱" type="email" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSubmit" :loading="loading">保存修改</el-button>
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
