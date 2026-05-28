<!--
  AI 聊天右侧面板
  展示可用的 AI 技能列表，引导用户选择技能
  @author aceFelix
-->
<script setup>
import { ref } from 'vue'

const skills = ref([
  {
    id: 1,
    icon: 'article',
    title: '文章创作',
    description: '智能生成技术文章、博客内容、产品文档等',
    color: '#F97316',
    tags: ['写作', 'SEO优化', '多语言'],
    popular: true
  },
  {
    id: 2,
    icon: 'resume',
    title: '简历制作',
    description: '专业简历撰写、优化和个性化定制',
    color: '#3B82F6',
    tags: ['模板', 'ATS优化', '中英文'],
    popular: true
  },
  {
    id: 3,
    icon: 'code',
    title: '代码助手',
    description: '代码编写、调试、重构和代码审查',
    color: '#10B981',
    tags: ['多语言', '调试', '最佳实践'],
    popular: true
  },
  {
    id: 4,
    icon: 'data',
    title: '数据分析',
    description: '数据可视化、报表生成、趋势分析',
    color: '#8B5CF6',
    tags: ['图表', '报告', '预测'],
    popular: false
  },
  {
    id: 5,
    icon: 'translate',
    title: '翻译助手',
    description: '高质量多语言翻译和本地化支持',
    color: '#EC4899',
    tags: ['100+语言', '专业术语', '语境'],
    popular: false
  },
  {
    id: 6,
    icon: 'summary',
    title: '内容摘要',
    description: '长文摘要、关键点提取、思维导图',
    color: '#F59E0B',
    tags: ['快速阅读', '要点', '结构化'],
    popular: false
  }
])

const quickActions = ref([
  { id: 1, text: '帮我写一个雪花算法代码', icon: 'code' },
  { id: 2, text: '生成一份Java项目 README 模板文档', icon: 'article' },
  { id: 3, text: '帮我写一个Vue3项目规范skill', icon: 'code' },
  { id: 4, text: '生成一份Java面试自我介绍', icon: 'resume' }
])

const emit = defineEmits(['select-skill', 'quick-action'])

const selectedSkillId = ref(null)

const selectSkill = (skill) => {
  selectedSkillId.value = skill.id
  emit('select-skill', skill)
}

const triggerQuickAction = (action) => {
  emit('quick-action', action.text)
}

const getIconPath = (iconType) => {
  const icons = {
    article: '<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>',
    resume: '<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M12 18v-6"/><path d="M9 15l3-3 3 3"/>',
    code: '<polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/>',
    data: '<path d="M21.21 15.89A10 10 0 1 1 8 2.83"/><path d="M22 12A10 10 0 0 0 12 2v10z"/>',
    translate: '<path d="m5 8 6 6"/><path d="m4 14 6-6 2-3"/><path d="M2 5h12"/><path d="M7 2h1"/><path d="m22 22-5-10-5 10"/><path d="M14 18h6"/>',
    summary: '<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>'
  }
  return icons[iconType] || icons.article
}
</script>

<template>
  <aside class="skills-sidebar">
    <div class="section-header">
      <h3 class="section-title">AI 技能</h3>
      <span class="section-subtitle">选择一个技能开始对话</span>
    </div>

    <div class="skills-grid">
      <div
        v-for="skill in skills"
        :key="skill.id"
        :class="['skill-card', { popular: skill.popular, active: selectedSkillId === skill.id }]"
        @click="selectSkill(skill)"
      >
        <div class="skill-icon" :style="{ background: `${skill.color}15`, color: skill.color }">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" v-html="getIconPath(skill.icon)"></svg>
          <span v-if="skill.popular" class="popular-badge">热门</span>
        </div>
        <div class="skill-info">
          <h4 class="skill-title">{{ skill.title }}</h4>
          <p class="skill-desc">{{ skill.description }}</p>
          <div class="skill-tags">
            <span
              v-for="(tag, index) in skill.tags"
              :key="index"
              class="tag"
              :style="{ background: `${skill.color}10`, color: skill.color }"
            >
              {{ tag }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <div class="quick-actions">
      <h4 class="actions-title">快捷操作</h4>
      <div class="action-list">
        <button
          v-for="action in quickActions"
          :key="action.id"
          class="action-item"
          @click="triggerQuickAction(action)"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="action-icon">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
          <span>{{ action.text }}</span>
        </button>
      </div>
    </div>

    <div class="ai-info-card">
      <div class="ai-avatar-large">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
      </div>
      <h4 class="ai-name">AI 小B</h4>
      <p class="ai-desc">基于大语言模型的智能助手</p>
      <div class="ai-stats">
        <div class="stat">
          <span class="stat-value">100+</span>
          <span class="stat-label">技能</span>
        </div>
        <div class="stat">
          <span class="stat-value">24/7</span>
          <span class="stat-label">在线</span>
        </div>
        <div class="stat">
          <span class="stat-value">&lt;1s</span>
          <span class="stat-label">响应</span>
        </div>
      </div>
    </div>
  </aside>
</template>

<style scoped lang="scss">
.skills-sidebar {
  height: 100%;
  overflow-y: auto;
  border-radius: 12px;
  padding: 16px;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  background: white;
  display: flex;
  flex-direction: column;
  gap: 20px;

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

.section-header {
  .section-title {
    font-size: 18px;
    font-weight: 700;
    color: #1a1a1a;
    margin: 0 0 4px 0;
  }

  .section-subtitle {
    font-size: 13px;
    color: #888;
  }
}

.skills-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.skill-card {
  display: flex;
  gap: 12px;
  padding: 14px;
  border-radius: 10px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    transform: translateY(-2px);

    .skill-icon {
      transform: scale(1.05);
    }
  }

  &.active {
    box-shadow: 0 6px 16px rgba(249, 115, 22, 0.3);
  }

  &.popular {
    border-color: #fff5eb;
  }

  .skill-icon {
    width: 48px;
    height: 48px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    flex-shrink: 0;
    transition: all 0.2s;

    svg {
      width: 24px;
      height: 24px;
    }

    .popular-badge {
      position: absolute;
      top: -6px;
      right: -6px;
      padding: 2px 6px;
      border-radius: 8px;
      font-size: 9px;
      font-weight: 600;
      background: linear-gradient(135deg, #F97316, #ea580c);
      color: white;
    }
  }

  .skill-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;

    .skill-title {
      font-size: 14px;
      font-weight: 600;
      color: #1a1a1a;
      margin: 0;
    }

    .skill-desc {
      font-size: 12px;
      color: #666;
      line-height: 1.4;
      margin: 0;
    }

    .skill-tags {
      display: flex;
      gap: 4px;
      flex-wrap: wrap;
      margin-top: 4px;

      .tag {
        padding: 2px 8px;
        border-radius: 10px;
        font-size: 11px;
        font-weight: 500;
      }
    }
  }
}

.quick-actions {
  .actions-title {
    font-size: 15px;
    font-weight: 600;
    color: #1a1a1a;
    margin: 0 0 12px 0;
  }

  .action-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .action-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 12px;
    border-radius: 8px;
    border: 1px solid #e5e7eb;
    background: transparent;
    cursor: pointer;
    transition: all 0.2s;
    text-align: left;
    font-size: 13px;
    color: #555;

    &:hover {
      border-color: #F97316;
      background: rgba(249, 115, 22, 0.05);
      color: #F97316;
      transform: translateX(4px);
    }

    .action-icon {
      width: 18px;
      height: 18px;
      stroke: currentColor;
      flex-shrink: 0;
    }
  }
}

.ai-info-card {
  padding: 20px;
  border-radius: 12px;
  background: linear-gradient(135deg, #FFF5EB 0%, #FFE8D6 100%);
  text-align: center;

  .ai-avatar-large {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    background: linear-gradient(135deg, #F97316, #ea580c);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 12px;
    box-shadow: 0 4px 16px rgba(249, 115, 22, 0.3);

    svg {
      width: 32px;
      height: 32px;
      stroke: white;
    }
  }

  .ai-name {
    font-size: 18px;
    font-weight: 700;
    color: #1a1a1a;
    margin: 0 0 4px 0;
  }

  .ai-desc {
    font-size: 12px;
    color: #888;
    margin: 0 0 16px 0;
  }

  .ai-stats {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;

    .stat {
      display: flex;
      flex-direction: column;
      gap: 2px;

      .stat-value {
        font-size: 16px;
        font-weight: 700;
        color: #F97316;
      }

      .stat-label {
        font-size: 11px;
        color: #888;
      }
    }
  }
}
</style>
