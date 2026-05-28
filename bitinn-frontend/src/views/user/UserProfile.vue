<!--
  用户个人主页
  三栏布局：左侧个人信息、中间内容区、右侧统计信息
  支持编辑资料和上传头像
  @author aceFelix
-->
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserInfoService, userInfoUpdateService, userAvatarUpdateService } from '@/api/user'
import { userInfoStore } from '@/stores/userInfo'
import { uploadFile } from '@/api/upload'
import UserProfileLeft from '@/components/user/UserProfileLeft.vue'
import UserProfileMain from '@/components/user/UserProfileMain.vue'
import UserProfileRight from '@/components/user/UserProfileRight.vue'

const router = useRouter()
const userStore = userInfoStore()

const loading = ref(true)
const saving = ref(false)
const uploading = ref(false)

const userInfo = ref({
  id: null,
  username: '',
  nickname: '',
  email: '',
  userPic: '',
  bio: '',
  phone: '',
  role: '',
  status: '',
  createTime: '',
  updateTime: '',
  articleCount: 0,
  fanCount: 0
})

const fileInputRef = ref(null)

const fetchUserInfo = async () => {
  loading.value = true
  try {
    const res = await getUserInfoService()
    if (res.data) {
      userInfo.value = { ...userInfo.value, ...res.data }
      userStore.setUserInfo(res.data)
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

const triggerFileInput = () => {
  fileInputRef.value?.click()
}

const handleAvatarChange = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return
  }

  uploading.value = true
  try {
    const res = await uploadFile(file)
    const avatarUrl = res.data
    await userAvatarUpdateService(avatarUrl)

    userInfo.value.userPic = avatarUrl
    userStore.setUserInfo({
      ...userStore.userInfo,
      userPic: avatarUrl
    })

    ElMessage.success('头像更新成功')
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败')
  } finally {
    uploading.value = false
  }
}

const handleSaveProfile = async (editData) => {
  saving.value = true
  try {
    await userInfoUpdateService(editData)

    userInfo.value.nickname = editData.nickname
    userInfo.value.email = editData.email
    userInfo.value.bio = editData.bio
    userInfo.value.phone = editData.phone

    userStore.setUserInfo({
      ...userStore.userInfo,
      nickname: editData.nickname,
      email: editData.email,
      bio: editData.bio,
      phone: editData.phone
    })

    ElMessage.success('个人信息更新成功')
  } catch (error) {
    console.error('更新失败:', error)
    ElMessage.error('更新失败，请重试')
  } finally {
    saving.value = false
  }
}

const goBack = () => {
  router.push('/')
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<template>
  <div class="profile-page">
    <header class="navbar">
      <div class="nav-brand" @click="goBack">
        <div class="logo">
          <span class="logo-bracket">&lt;</span>
          <span class="logo-text">BitInn</span>
          <span class="logo-bracket">/&gt;</span>
        </div>
      </div>
      <button class="btn-back" @click="goBack">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
        <span>返回首页</span>
      </button>
    </header>

    <main class="profile-grid">
      <UserProfileLeft
        :user-info="userInfo"
        :uploading="uploading"
        @upload-avatar="triggerFileInput"
      />

      <UserProfileMain
        :user-info="userInfo"
        :saving="saving"
        @save="handleSaveProfile"
      />

      <UserProfileRight :user-info="userInfo" />
    </main>

    <input
      ref="fileInputRef"
      type="file"
      accept="image/*"
      style="display: none"
      @change="handleAvatarChange"
    />
  </div>
</template>

<style scoped lang="scss">
.profile-page {
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  z-index: 1000;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
  }

  .nav-brand {
    cursor: pointer;

    .logo {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 24px;
      font-weight: 700;

      .logo-bracket {
        font-family: 'JetBrains Mono', 'Fira Code', monospace;
      }

      .logo-text {
        text-shadow: 0 0 10px rgba(249, 115, 22, 0.5);
        color: #F97316;
      }
    }
  }

  .btn-back {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    border: 1px solid #F97316;
    background: transparent;
    color: #F97316;

    svg {
      width: 16px;
      height: 16px;
    }

    &:hover {
      box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
      transform: translateY(-2px);
      background: #F97316;
      color: white;

      svg { stroke: white; }
    }
  }
}

.profile-grid {
  display: grid;
  grid-template-columns: 260px 1fr 300px;
  gap: 24px;
  max-width: 1400px;
  margin: 0 auto;
  padding: 84px 24px 24px;
  height: 100vh;
  overflow: hidden;
  box-sizing: border-box;
}

@media (max-width: 1200px) {
  .profile-grid {
    grid-template-columns: 240px 1fr 280px;
    gap: 16px;
    padding: 84px 16px 16px;
  }
}

@media (max-width: 992px) {
  .profile-grid {
    grid-template-columns: 1fr;
    height: auto;
    overflow: visible;
  }
}
</style>
