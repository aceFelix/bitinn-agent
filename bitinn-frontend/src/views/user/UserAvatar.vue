<!--
  修改头像页面
  支持输入头像 URL 并预览，更新后同步到 Pinia Store
  @author aceFelix
-->
<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { userAvatarUpdateService } from '@/api/user'
import { userInfoStore } from '@/stores/userInfo'

const userStore = userInfoStore()

const avatarUrl = ref('')
const loading = ref(false)

const handleUpdate = async () => {
  if (!avatarUrl.value) {
    ElMessage.warning('请输入头像地址')
    return
  }

  loading.value = true
  try {
    await userAvatarUpdateService(avatarUrl.value)
    userStore.setUserInfo({
      ...userStore.userInfo,
      userPic: avatarUrl.value
    })
    ElMessage.success('头像更新成功')
  } catch (error) {
    console.error('更新头像失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page">
    <h2>修改头像</h2>
    <el-form label-width="80px" class="form">
      <el-form-item label="头像地址">
        <el-input v-model="avatarUrl" placeholder="请输入头像图片地址" />
      </el-form-item>
      <el-form-item label="预览">
        <el-avatar :size="80" :src="avatarUrl || undefined">
          <el-icon><User /></el-icon>
        </el-avatar>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleUpdate" :loading="loading">更新头像</el-button>
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
