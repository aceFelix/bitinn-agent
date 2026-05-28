<!--
  用户主页主内容区
  支持编辑个人资料、Tab 切换（详情/资源/贡献）
  @author aceFelix
-->
<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { userInfoUpdateService } from '@/api/user'

const props = defineProps({
  userInfo: { type: Object, default: () => ({}) },
  saving: { type: Boolean, default: false }
})

const emit = defineEmits(['save'])

const tabs = [
  { id: 'detail', label: '我的详情' },
  { id: 'resources', label: '我的资源' },
  { id: 'contribution', label: '我的贡献' }
]

const activeTab = ref('detail')
const isEditing = ref(false)

const editForm = ref({
  nickname: '',
  email: '',
  bio: '',
  phone: ''
})

const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { pattern: /^\S{1,10}$/, message: '昵称必须是1-10位非空字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  bio: [
    { max: 200, message: '简介最多200个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const formRef = ref(null)

const enterEditMode = () => {
  editForm.value = {
    nickname: props.userInfo.nickname || '',
    email: props.userInfo.email || '',
    bio: props.userInfo.bio || '',
    phone: props.userInfo.phone || ''
  }
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
  formRef.value?.clearValidate()
}

const handleSave = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  emit('save', {
    ...editForm.value,
    id: props.userInfo.id
  })
}

const switchTab = (tabId) => {
  activeTab.value = tabId
  isEditing.value = false
}
</script>

<template>
  <section class="profile-main">
    <div class="tab-nav">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        :class="['tab-btn', { active: activeTab === tab.id }]"
        @click="switchTab(tab.id)"
      >
        {{ tab.label }}
      </button>
    </div>

    <div class="tab-content">
      <template v-if="activeTab === 'detail'">
        <div class="detail-card">
          <div class="card-header">
            <h3 class="card-title">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              {{ isEditing ? '编辑资料' : '个人资料' }}
            </h3>
            <button
              v-if="!isEditing"
              class="btn-edit"
              @click="enterEditMode"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
              </svg>
              <span>编辑资料</span>
            </button>
          </div>

          <div v-if="!isEditing" class="info-display">
            <div class="info-item">
              <span class="info-label">用户名</span>
              <span class="info-value">{{ userInfo.username }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">昵称</span>
              <span class="info-value">{{ userInfo.nickname }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">邮箱</span>
              <span class="info-value">{{ userInfo.email }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">手机号</span>
              <span class="info-value">{{ userInfo.phone || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">个人简介</span>
              <span class="info-value bio-value">{{ userInfo.bio || '未设置' }}</span>
            </div>
          </div>

          <el-form
            v-else
            ref="formRef"
            :model="editForm"
            :rules="rules"
            label-position="top"
            class="edit-form"
          >
            <el-form-item label="昵称" prop="nickname">
              <el-input
                v-model="editForm.nickname"
                placeholder="请输入昵称"
                maxlength="10"
                show-word-limit
              />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input
                v-model="editForm.email"
                placeholder="请输入邮箱"
                type="email"
              />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input
                v-model="editForm.phone"
                placeholder="请输入手机号"
                maxlength="11"
              />
            </el-form-item>
            <el-form-item label="个人简介" prop="bio">
              <el-input
                v-model="editForm.bio"
                type="textarea"
                :rows="4"
                placeholder="介绍一下你自己..."
                maxlength="200"
                show-word-limit
                resize="none"
              />
            </el-form-item>
            <div class="form-actions">
              <el-button @click="cancelEdit">取消</el-button>
              <el-button type="primary" :loading="saving" @click="handleSave">
                保存修改
              </el-button>
            </div>
          </el-form>
        </div>
      </template>

      <template v-else-if="activeTab === 'resources'">
        <div class="resources-list">
          <div class="resource-item">
            <div class="resource-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
            </div>
            <div class="resource-info">
              <h4 class="resource-title">Vue3 组件库开发指南</h4>
              <p class="resource-desc">基于 Vue3 + TypeScript 的企业级组件库开发实践</p>
              <span class="resource-meta">2024-01-15 · 项目</span>
            </div>
          </div>

          <div class="resource-item">
            <div class="resource-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
            </div>
            <div class="resource-info">
              <h4 class="resource-title">Spring Boot 微服务架构</h4>
              <p class="resource-desc">从零搭建生产级微服务系统的完整教程</p>
              <span class="resource-meta">2024-02-20 · 教程</span>
            </div>
          </div>

          <div class="resource-item">
            <div class="resource-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
            </div>
            <div class="resource-info">
              <h4 class="resource-title">Python 数据分析实战</h4>
              <p class="resource-desc">使用 Pandas、NumPy 和 Matplotlib 进行数据可视化</p>
              <span class="resource-meta">2024-03-10 · 笔记</span>
            </div>
          </div>

          <div class="resource-item">
            <div class="resource-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
            </div>
            <div class="resource-info">
              <h4 class="resource-title">Docker 容器化部署方案</h4>
              <p class="resource-desc">多环境 Docker Compose 编排与 CI/CD 集成</p>
              <span class="resource-meta">2024-04-05 · 方案</span>
            </div>
          </div>

          <div class="resource-item">
            <div class="resource-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
            </div>
            <div class="resource-info">
              <h4 class="resource-title">React Native 跨平台开发</h4>
              <p class="resource-desc">一套代码同时运行 iOS 和 Android 的最佳实践</p>
              <span class="resource-meta">2024-04-18 · 项目</span>
            </div>
          </div>
        </div>
      </template>

      <template v-else-if="activeTab === 'contribution'">
        <div class="contribution-list">
          <div class="contribution-item">
            <div class="contrib-badge published">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
            </div>
            <div class="contrib-info">
              <h4 class="contrib-title">深入理解 Vue3 响应式原理</h4>
              <p class="contrib-desc">详细解析 Proxy 和 Reflect 在 Vue3 中的应用</p>
              <div class="contrib-stats">
                <span class="stat"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg> 1.2k 浏览</span>
                <span class="stat"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg> 89 点赞</span>
                <span class="stat"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg> 23 评论</span>
              </div>
            </div>
          </div>

          <div class="contribution-item">
            <div class="contrib-badge published">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
            </div>
            <div class="contrib-info">
              <h4 class="contrib-title">Spring Boot 性能优化实战</h4>
              <p class="contrib-desc">从数据库查询到缓存策略的全方位优化指南</p>
              <div class="contrib-stats">
                <span class="stat"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg> 986 浏览</span>
                <span class="stat"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg> 67 点赞</span>
                <span class="stat"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg> 15 评论</span>
              </div>
            </div>
          </div>

          <div class="contribution-item">
            <div class="contrib-badge draft">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
              </svg>
            </div>
            <div class="contrib-info">
              <h4 class="contrib-title">TypeScript 高级类型技巧（草稿）</h4>
              <p class="contrib-desc">条件类型、映射类型和模板字面量类型的深度应用</p>
              <div class="contrib-stats">
                <span class="stat status-draft">草稿 · 最后编辑于 2024-04-20</span>
              </div>
            </div>
          </div>

          <div class="contribution-item">
            <div class="contrib-badge published">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
            </div>
            <div class="contrib-info">
              <h4 class="contrib-title">Docker 多阶段构建优化</h4>
              <p class="contrib-desc">如何将镜像体积减少 80% 的实用技巧</p>
              <div class="contrib-stats">
                <span class="stat"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg> 754 浏览</span>
                <span class="stat"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg> 52 点赞</span>
                <span class="stat"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg> 8 评论</span>
              </div>
            </div>
          </div>

          <div class="contribution-summary">
            <div class="summary-item">
              <span class="summary-label">总发布</span>
              <span class="summary-value">{{ userInfo.articleCount || 12 }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">总浏览</span>
              <span class="summary-value">8.5k</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">总获赞</span>
              <span class="summary-value">{{ userInfo.likeCount || 368 }}</span>
            </div>
          </div>
        </div>
      </template>
    </div>
  </section>
</template>

<style scoped lang="scss">
.profile-main {
  min-width: 0;
  height: 100%;
  overflow-y: auto;
  border-radius: 12px;
  padding: 16px;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);

  &:hover {
    box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
    transform: translateY(-2px);
  }

  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-track { background: transparent; }
  &::-webkit-scrollbar-thumb { background: rgba(249, 115, 22, 0.3); border-radius: 4px; &:hover { background: #F97316; } }
}

.tab-nav {
  display: flex;
  gap: 4px;
  margin-bottom: 16px;
  background: transparent;
  border-radius: 12px;
  padding: 4px;
}

.tab-btn {
  flex: 1;
  padding: 10px 16px;
  background: transparent;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
    transform: translateY(-2px);
  }

  &.active {
    font-weight: 600;
    box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
    transform: translateY(-2px);
    color: #F97316;
  }
}

.tab-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-card {
  border-radius: 12px;
  padding: 24px;
  background: transparent;
  box-shadow: none;

  &:hover {
    box-shadow: none;
    transform: none;
  }
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;

  .card-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #1a1a1a;
    margin: 0;

    svg {
      width: 20px;
      height: 20px;
      stroke: #F97316;
    }
  }

  .btn-edit {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 6px 14px;
    border-radius: 6px;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    border: 1px solid #F97316;
    background: transparent;
    color: #F97316;

    svg {
      width: 14px;
      height: 14px;
    }

    &:hover {
      background: #F97316;
      color: white;
      box-shadow: 0 4px 12px rgba(249, 115, 22, 0.25);

      svg { stroke: white; }
    }
  }
}

.info-display {
  display: flex;
  flex-direction: column;
  gap: 16px;

  .info-item {
    display: flex;
    align-items: flex-start;
    gap: 16px;

    .info-label {
      width: 80px;
      flex-shrink: 0;
      font-size: 14px;
      color: #888;
      font-weight: 500;
    }

    .info-value {
      flex: 1;
      font-size: 14px;
      color: #1a1a1a;
      word-break: break-all;

      &.bio-value {
        line-height: 1.6;
        color: #666;
      }
    }
  }
}

.edit-form {
  :deep(.el-form-item__label) {
    font-weight: 500;
    color: #1a1a1a;
    padding-bottom: 4px;
  }

  :deep(.el-input__wrapper),
  :deep(.el-textarea__inner) {
    border-radius: 8px;
    box-shadow: 0 0 0 1px #e5e7eb inset;
    transition: all 0.2s;

    &:hover,
    &:focus {
      box-shadow: 0 0 0 1px #F97316 inset;
    }
  }

  :deep(.el-textarea__inner) {
    border-radius: 8px;
  }

  .form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 8px;

    .el-button {
      border-radius: 6px;
      transition: all 0.2s;
    }

    .el-button--primary {
      background: #F97316;
      border-color: #F97316;

      &:hover {
        background: #ea580c;
        border-color: #ea580c;
        box-shadow: 0 4px 12px rgba(249, 115, 22, 0.25);
      }
    }
  }
}

.placeholder-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 60px 20px;
  border-radius: 12px;
  background: transparent;
  box-shadow: none;

  &:hover {
    box-shadow: none;
    transform: none;
  }

  svg {
    width: 56px;
    height: 56px;
    opacity: 0.3;
    stroke: #888;
  }

  p { font-size: 15px; font-weight: 500; color: #666; }
  span { font-size: 12px; color: #bbb; }
}

.resources-list,
.contribution-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.resource-item,
.contribution-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  transition: all 0.2s;

  &:hover {
    border-color: #F97316;
    box-shadow: 0 4px 12px rgba(249, 115, 22, 0.1);
  }
}

.resource-icon,
.contrib-badge {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  svg {
    width: 24px;
    height: 24px;
  }
}

.resource-icon {
  background: rgba(249, 115, 22, 0.1);

  svg { stroke: #F97316; }
}

.contrib-badge {
  &.published {
    background: rgba(34, 197, 94, 0.1);
    svg { stroke: #22c55e; }
  }

  &.draft {
    background: rgba(234, 179, 8, 0.1);
    svg { stroke: #eab308; }
  }
}

.resource-info,
.contrib-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.resource-title,
.contrib-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;

  &:hover {
    color: #F97316;
    cursor: pointer;
  }
}

.resource-desc,
.contrib-desc {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}

.resource-meta {
  font-size: 12px;
  color: #888;
}

.contrib-stats {
  display: flex;
  gap: 16px;
  margin-top: 4px;

  .stat {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #888;

    svg {
      width: 14px;
      height: 14px;
    }

    &.status-draft {
      color: #eab308;
      font-weight: 500;
    }
  }
}

.contribution-summary {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;

  .summary-item {
    text-align: center;
    padding: 12px;
    border-radius: 8px;
    background: rgba(249, 115, 22, 0.05);

    .summary-label {
      display: block;
      font-size: 12px;
      color: #888;
      margin-bottom: 4px;
    }

    .summary-value {
      display: block;
      font-size: 20px;
      font-weight: 700;
      color: #F97316;
    }
  }
}
</style>
