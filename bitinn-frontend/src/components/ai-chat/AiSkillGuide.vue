<!--
  AI 技能引导弹窗
  用户选择技能后，展示该技能的详细配置表单（文章创作、简历制作等）
  @author aceFelix
-->
<script setup>
import { ref, reactive, watch, computed } from 'vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  skill: { type: Object, default: null }
})

const emit = defineEmits(['close', 'submit'])

const form = reactive({
  articleType: '技术文章',
  topic: '',
  selectedTags: [],
  style: '专业正式',
  audience: '中级',
  length: '中等 (~3000字)',
  extraRequirements: ''
})

const articleTypes = [
  { name: '技术文章', icon: '💻', color: '#F97316', desc: '分享技术经验、教程、最佳实践' },
  { name: '学习笔记', icon: '📝', color: '#10B981', desc: '记录学习心得、知识点整理' },
  { name: '问题讨论', icon: '💬', color: '#EF4444', desc: '提出技术问题、寻求解决方案' },
  { name: '项目分享', icon: '🚀', color: '#8B5CF6', desc: '展示个人项目、开源作品' }
]

const allTags = [
  { name: 'JavaScript', color: '#F7DF1E' }, { name: 'Python', color: '#3776AB' },
  { name: 'Java', color: '#007396' }, { name: 'C/C++', color: '#00599C' },
  { name: 'PHP', color: '#777BB4' }, { name: 'C#', color: '#512BD4' },
  { name: 'TypeScript', color: '#3178C6' }, { name: 'Vue', color: '#42B883' },
  { name: 'React', color: '#61DAFB' }, { name: 'Go', color: '#00ADD8' },
  { name: 'Rust', color: '#DEA584' }, { name: 'Docker', color: '#2496ED' },
  { name: 'Linux', color: '#FCC624' }, { name: 'Django', color: '#092E20' },
  { name: 'FastAPI', color: '#009688' }, { name: 'Flask', color: '#000000' },
  { name: 'SpringBoot', color: '#6DB33F' }, { name: 'SpringCloud', color: '#6DB33F' },
  { name: 'SpringAI', color: '#6DB33F' }, { name: 'Langchain', color: '#1C3C3C' },
  { name: 'Langchain4j', color: '#4B5563' }, { name: 'PyTorch', color: '#EE4C2C' },
  { name: 'TensorFlow', color: '#FF6F00' }, { name: 'Scikit-learn', color: '#F7931E' },
  { name: 'Taro', color: '#3CB371' }, { name: 'Uni-app', color: '#2B9939' },
  { name: 'Android', color: '#3DDC84' }, { name: 'iOS', color: '#007AFF' },
  { name: 'MySQL', color: '#4479A1' }, { name: 'Redis', color: '#DC382D' },
  { name: '微服务', color: '#9B59B6' }, { name: '算法', color: '#E74C3C' }
]

const styles = [
  { value: '专业正式', desc: '用词准确，结构严谨' },
  { value: '轻松口语化', desc: '通俗易懂，接地气' },
  { value: '学术严谨', desc: '引用规范，论证严密' }
]
const audiences = [
  { value: '入门', desc: '零基础，需要解释基本概念' },
  { value: '中级', desc: '有 1-3 年经验，熟悉基础' },
  { value: '专家', desc: '资深从业者，关注深度和前沿' }
]
const lengths = [
  { value: '简短 (~1500字)', desc: '快速阅读，核心要点' },
  { value: '中等 (~3000字)', desc: '结构完整，内容充实' },
  { value: '详细 (~5000字)', desc: '深入剖析，案例丰富' }
]

const skillConfig = computed(() => {
  if (!props.skill) return null
  const configs = {
    article: { title: '文章创作', icon: 'article', color: '#F97316', useGuide: true }
  }
  return configs[props.skill.icon] || null
})

const resetForm = () => {
  form.articleType = '技术文章'
  form.topic = ''
  form.selectedTags = []
  form.style = '专业正式'
  form.audience = '中级'
  form.length = '中等 (~3000字)'
  form.extraRequirements = ''
}

watch(() => props.visible, (val) => {
  if (val) resetForm()
})

const isTagSelected = (tag) => form.selectedTags.includes(tag.name)

const toggleTag = (tag) => {
  const idx = form.selectedTags.indexOf(tag.name)
  if (idx > -1) {
    form.selectedTags.splice(idx, 1)
  } else if (form.selectedTags.length < 5) {
    form.selectedTags.push(tag.name)
  }
}

const buildPrompt = () => {
  const config = skillConfig.value
  if (!config) return ''

  const type = form.articleType
  const topic = form.topic.trim()
  const tagStr = form.selectedTags.join('、')
  const style = form.style
  const audience = form.audience
  const length = form.length
  const extra = form.extraRequirements.trim()

  let prompt = `你是一位资深技术写作者。请写一篇关于「${topic}」的${type}。\n\n`

  prompt += `- 类型：${type}\n`
  prompt += `- 写作风格：${style}\n`
  prompt += `- 目标读者：${audience}读者`
  if (tagStr) prompt += `\n- 关键词/标签：${tagStr}`
  prompt += `\n- 篇幅：${length}`

  if (extra) {
    prompt += `\n- 额外要求：${extra}`
  }

  prompt += `\n\n请按以下结构输出：\n1. 引言（背景 + 本文目标）\n2. 核心概念与原理\n3. 实战步骤 / 详细内容\n4. 最佳实践与注意事项\n5. 总结与展望`

  if (type === '学习笔记') {
    prompt += `\n\n额外要求：请以学习者的视角组织内容，包含知识脉络梳理和易错点提醒。`
  } else if (type === '问题讨论') {
    prompt += `\n\n额外要求：请先分析问题根因，再给出多种解决方案并对比优劣。`
  } else if (type === '项目分享') {
    prompt += `\n\n额外要求：请包含项目背景、技术选型理由、核心实现细节和踩坑经验。`
  }

  return prompt
}

const handleSubmit = () => {
  if (!form.topic.trim()) return
  emit('submit', buildPrompt())
}

const handleClose = () => {
  emit('close')
}

const handleOverlayClick = (e) => {
  if (e.target === e.currentTarget) handleClose()
}

const handleKeydown = (e) => {
  if (e.key === 'Escape') handleClose()
}

const getIconSvg = (iconType) => {
  const icons = {
    article: '<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2-13"/><line x1="16" y1="17" x2="8" y2-17"/>'
  }
  return icons[iconType] || ''
}
</script>

<template>
  <Teleport to="body">
    <transition name="guide-fade">
      <div
        v-if="visible && skillConfig && skillConfig.useGuide"
        class="guide-overlay"
        @click.self="handleOverlayClick"
        @keydown="handleKeydown"
      >
        <div class="guide-dialog">
          <div class="guide-header">
            <div class="guide-title-row">
              <span class="guide-icon" :style="{ background: `${skillConfig.color}15`, color: skillConfig.color }">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" v-html="getIconSvg(skillConfig.icon)"></svg>
              </span>
              <div>
                <h2 class="guide-title">{{ skillConfig.title }}</h2>
                <p class="guide-subtitle">填写以下信息，AI 将为你生成高质量内容</p>
              </div>
            </div>
            <button class="guide-close" @click="handleClose">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>

          <div class="guide-body">
            <div class="form-group">
              <label class="form-label">
                文章类型
                <span class="required">*</span>
              </label>
              <div class="type-grid">
                <button
                  v-for="t in articleTypes"
                  :key="t.name"
                  :class="['type-card', { active: form.articleType === t.name }]"
                  :style="form.articleType === t.name ? { borderColor: t.color, background: `${t.color}08` } : {}"
                  @click="form.articleType = t.name"
                >
                  <span class="type-icon">{{ t.icon }}</span>
                  <span class="type-name" :style="{ color: form.articleType === t.name ? t.color : '' }">{{ t.name }}</span>
                  <span class="type-desc">{{ t.desc }}</span>
                </button>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">
                主题 / 标题
                <span class="required">*</span>
              </label>
              <input
                v-model="form.topic"
                type="text"
                class="form-input"
                placeholder="例如：Spring Boot 微服务架构实践"
                @keydown.enter="handleSubmit"
              />
            </div>

            <div class="form-group">
              <label class="form-label">
                文章标签
                <span class="tag-hint">（最多选 5 个）</span>
              </label>
              <div class="tag-cloud">
                <button
                  v-for="tag in allTags"
                  :key="tag.name"
                  :class="['tag-chip', { active: isTagSelected(tag) }]"
                  :style="isTagSelected(tag) ? { borderColor: tag.color, color: tag.color, background: `${tag.color}15` } : {}"
                  @click="toggleTag(tag)"
                >
                  {{ tag.name }}
                </button>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label class="form-label">写作风格</label>
                <div class="chip-group vertical">
                  <button
                    v-for="s in styles"
                    :key="s.value"
                    :class="['chip', { active: form.style === s.value }]"
                    @click="form.style = s.value"
                  >
                    <span class="chip-label">{{ s.value }}</span>
                    <span class="chip-desc">{{ s.desc }}</span>
                  </button>
                </div>
              </div>

              <div class="form-group">
                <label class="form-label">目标读者</label>
                <div class="chip-group vertical">
                  <button
                    v-for="a in audiences"
                    :key="a.value"
                    :class="['chip', { active: form.audience === a.value }]"
                    @click="form.audience = a.value"
                  >
                    <span class="chip-label">{{ a.value }}</span>
                    <span class="chip-desc">{{ a.desc }}</span>
                  </button>
                </div>
              </div>

              <div class="form-group">
                <label class="form-label">篇幅偏好</label>
                <div class="chip-group vertical">
                  <button
                    v-for="l in lengths"
                    :key="l.value"
                    :class="['chip', { active: form.length === l.value }]"
                    @click="form.length = l.value"
                  >
                    <span class="chip-label">{{ l.value }}</span>
                    <span class="chip-desc">{{ l.desc }}</span>
                  </button>
                </div>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">额外要求</label>
              <textarea
                v-model="form.extraRequirements"
                class="form-textarea"
                placeholder="如：需要 Mermaid 流程图、对比表格、代码示例等"
                rows="3"
              ></textarea>
            </div>

            <div class="prompt-preview" v-if="form.topic.trim()">
              <div class="preview-header">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="preview-icon">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                </svg>
                <span>生成的 Prompt 预览</span>
              </div>
              <pre class="preview-content">{{ buildPrompt() }}</pre>
            </div>
          </div>

          <div class="guide-footer">
            <button class="btn-cancel" @click="handleClose">取消</button>
            <button
              class="btn-submit"
              :disabled="!form.topic.trim()"
              :style="{ background: skillConfig.color }"
              @click="handleSubmit"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="22" y1="2" x2="11" y2="13"/>
                <polygon points="22 2 15 22 11 13 2 9 22 2"/>
              </svg>
              <span>填入输入框</span>
            </button>
          </div>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<style scoped lang="scss">
.guide-overlay {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
}

.guide-dialog {
  width: 720px;
  max-height: 85vh;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: dialog-in 0.25s ease-out;
}

@keyframes dialog-in {
  from { opacity: 0; transform: scale(0.95) translateY(10px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

.guide-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 24px 28px 16px;
  border-bottom: 1px solid #f0f0f0;

  .guide-title-row {
    display: flex;
    align-items: center;
    gap: 14px;
  }

  .guide-icon {
    width: 44px;
    height: 44px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    svg { width: 22px; height: 22px; }
  }

  .guide-title {
    font-size: 20px;
    font-weight: 700;
    color: #1a1a1a;
    margin: 0 0 2px;
  }

  .guide-subtitle {
    font-size: 13px;
    color: #999;
    margin: 0;
  }

  .guide-close {
    width: 36px;
    height: 36px;
    border-radius: 8px;
    border: none;
    background: #f5f5f5;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;
    flex-shrink: 0;

    svg { width: 18px; height: 18px; stroke: #666; }

    &:hover {
      background: #fee2e2;
      svg { stroke: #ef4444; }
    }
  }
}

.guide-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px 28px;
  display: flex;
  flex-direction: column;
  gap: 20px;

  &::-webkit-scrollbar { width: 6px; }
  &::-webkit-scrollbar-track { background: transparent; }
  &::-webkit-scrollbar-thumb { background: #e0e0e0; border-radius: 3px; }
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  font-weight: 600;
  color: #333;

  .required { color: #ef4444; margin-left: 2px; }

  .tag-hint {
    font-weight: 400;
    color: #999;
    font-size: 12px;
    margin-left: 4px;
  }
}

.type-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.type-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 14px 10px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  background: white;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: #ccc;
    transform: translateY(-2px);
  }

  &.active {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }

  .type-icon {
    font-size: 26px;
  }

  .type-name {
    font-size: 14px;
    font-weight: 600;
    color: #333;
  }

  .type-desc {
    font-size: 11px;
    color: #999;
    text-align: center;
    line-height: 1.3;
  }
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
}

.tag-chip {
  padding: 5px 12px;
  border: 1.5px solid #e5e7eb;
  border-radius: 18px;
  background: white;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.15s;

  &:hover {
    border-color: #bbb;
  }

  &.active {
    font-weight: 600;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  }
}

.form-input {
  padding: 10px 14px;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  font-size: 14px;
  color: #1a1a1a;
  outline: none;
  transition: border-color 0.2s;

  &:focus { border-color: #F97316; }
  &::placeholder { color: #bbb; }
}

.form-textarea {
  padding: 10px 14px;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  font-size: 14px;
  color: #1a1a1a;
  outline: none;
  resize: vertical;
  font-family: inherit;
  transition: border-color 0.2s;

  &:focus { border-color: #F97316; }
  &::placeholder { color: #bbb; }
}

.chip-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;

  &.vertical {
    flex-direction: column;

    .chip { justify-content: flex-start; }
  }
}

.chip {
  padding: 8px 14px;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  background: white;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  gap: 2px;

  &:hover {
    border-color: #ccc;
    background: #fafafa;
  }

  &.active {
    border-color: #F97316;
    background: #fff7ed;
    color: #F97316;
    font-weight: 600;
  }

  .chip-label { font-size: 14px; }
  .chip-desc { font-size: 11px; color: #999; font-weight: 400; }
  &.active .chip-desc { color: #fdba74; }
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 16px;
}

.prompt-preview {
  background: #1e1e1e;
  border-radius: 12px;
  overflow: hidden;

  .preview-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 16px;
    background: #2a2a2a;
    font-size: 13px;
    color: #aaa;

    .preview-icon { width: 14px; height: 14px; }
  }

  .preview-content {
    margin: 0;
    padding: 14px 16px;
    font-size: 13px;
    line-height: 1.7;
    color: #a5d6ff;
    white-space: pre-wrap;
    font-family: 'JetBrains Mono', 'Fira Code', monospace;
    max-height: 200px;
    overflow-y: auto;

    &::-webkit-scrollbar { width: 4px; }
    &::-webkit-scrollbar-thumb { background: #444; border-radius: 2px; }
  }
}

.guide-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 28px;
  border-top: 1px solid #f0f0f0;

  .btn-cancel {
    padding: 10px 24px;
    border: 2px solid #e5e7eb;
    border-radius: 10px;
    background: white;
    font-size: 14px;
    color: #666;
    cursor: pointer;
    transition: all 0.2s;

    &:hover { background: #f5f5f5; color: #333; }
  }

  .btn-submit {
    padding: 10px 24px;
    border: none;
    border-radius: 10px;
    font-size: 14px;
    font-weight: 600;
    color: white;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 8px;
    transition: all 0.2s;

    svg { width: 16px; height: 16px; }

    &:hover:not(:disabled) {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(249, 115, 22, 0.4);
    }

    &:disabled { opacity: 0.5; cursor: not-allowed; }
  }
}

.guide-fade-enter-active,
.guide-fade-leave-active { transition: opacity 0.2s ease; }
.guide-fade-enter-from,
.guide-fade-leave-to { opacity: 0; }
</style>
