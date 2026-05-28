<!--
  标签选择器
  展示推荐标签列表，支持多选和搜索过滤，优先从后端 API 获取推荐标签
  @author aceFelix
-->
<script setup>
import { ref, onMounted, nextTick } from 'vue'
import request from '@/utils/request'

// 接收父组件传递的选中状态
const props = defineProps({
  hasSelectedType: {
    type: Boolean,
    default: false
  }
})

// 原始推荐标签（固定不变）
const originalTags = ['JavaScript', 'Python', 'Java', 'C/C++', 'PHP', 'C#', 'TypeScript', 'Vue', 'React', 'Go', 'Rust', 'Docker', 'Linux', 'Django', 'FastAPI', 'Flask', 'SpringBoot', 'SpringCloud', 'SpringAI', 'Langchain', 'Langchain4j', 'PyTorch', 'TensorFlow', 'Scikit-learn', 'Taro', 'Uni-app', 'Android', 'iOS', 'HarmonyOS', 'MySQL', 'Redis', '微服务', '算法']

// 推荐标签（优先从后端API获取，失败则用本地数据）
const recommendedTags = ref([...originalTags])

// 选中的标签
const selectedTags = ref([])

// 自定义标签输入
const showCustomInput = ref(false)
const customTagInput = ref(null)
const customTagText = ref('')

// 从后端获取标签列表
const fetchTags = async () => {
  try {
    const result = await request.get('/tag')
    if (result.code === 200 && result.data && result.data.length > 0) {
      recommendedTags.value = result.data.map(t => t.tagName)
    }
  } catch (error) {
    console.warn('获取标签失败，使用本地数据:', error)
  }
}

onMounted(() => {
  fetchTags()
})

// 切换标签选择
const toggleTag = (tag) => {
  const index = selectedTags.value.indexOf(tag)
  if (index > -1) {
    selectedTags.value.splice(index, 1)
    if (!originalTags.includes(tag)) {
      const recIndex = recommendedTags.value.indexOf(tag)
      if (recIndex > -1) {
        recommendedTags.value.splice(recIndex, 1)
      }
    }
  } else {
    selectedTags.value.push(tag)
  }
}

// 显示自定义输入框
const showCustomTagInput = () => {
  showCustomInput.value = true
  nextTick(() => {
    customTagInput.value?.focus()
  })
}

// 添加自定义标签
const addCustomTag = () => {
  const tag = customTagText.value.trim()
  if (!tag) {
    showCustomInput.value = false
    return
  }
  if (!recommendedTags.value.includes(tag)) {
    recommendedTags.value.push(tag)
  }
  if (!selectedTags.value.includes(tag)) {
    selectedTags.value.push(tag)
  }
  customTagText.value = ''
  showCustomInput.value = false
}

// 取消自定义输入
const cancelCustomTag = () => {
  customTagText.value = ''
  showCustomInput.value = false
}

// 暴露选中标签给父组件
const getSelectedTags = () => selectedTags.value

defineExpose({
  getSelectedTags,
  selectedTags
})

// 定义事件
const emit = defineEmits(['start-edit'])

// 开始编辑
const startEdit = () => {
  emit('start-edit')
}
</script>

<template>
  <section class="tags-section">
    <h2 class="section-title">添加标签</h2>
    <p class="section-desc">选择合适的标签，让更多人发现你的文章</p>
    <div class="tags-content">
      <div class="tags-cloud">
        <button
          v-for="tag in recommendedTags"
          :key="tag"
          class="tag-btn"
          :class="{ active: selectedTags.includes(tag) }"
          @click="toggleTag(tag)"
        >
          {{ tag }}
        </button>
        <button
          v-if="!showCustomInput"
          class="tag-btn tag-btn-custom"
          @click="showCustomTagInput"
        >
          + 自定义
        </button>
        <input
          v-else
          ref="customTagInput"
          v-model="customTagText"
          class="custom-tag-input"
          placeholder="输入标签后按回车"
          @keyup.enter="addCustomTag"
          @keyup.escape="cancelCustomTag"
          @blur="cancelCustomTag"
        />
      </div>
      <div class="selected-tags" v-if="selectedTags.length > 0">
        <span class="selected-label">已选择：</span>
        <span
          v-for="tag in selectedTags"
          :key="tag"
          class="selected-tag"
          @click="toggleTag(tag)"
        >
          {{ tag }}
          <span class="remove">×</span>
        </span>
      </div>
    </div>

    <!-- 开始编辑按钮 -->
    <div class="edit-action">
      <button 
        class="btn-start-edit" 
        :class="{ active: hasSelectedType }"
        @click="startEdit"
        :disabled="!hasSelectedType"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
          <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
        </svg>
        开始编辑
      </button>
    </div>
  </section>
</template>

<style scoped lang="scss">
.tags-section {
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  transition: all 0.2s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(249, 115, 22, 0.25);
  }

  .section-title {
    flex-shrink: 0;
    padding: 24px 24px 0;
    margin-bottom: 8px;
    font-size: 18px;
    font-weight: 600;
  }

  .section-desc {
    flex-shrink: 0;
    padding: 0 24px;
    margin-bottom: 16px;
    font-size: 14px;
  }
}

.tags-content {
  flex: 1;
  overflow-y: auto;
  padding: 0 24px;
  scrollbar-width: none;
  -ms-overflow-style: none;

  &::-webkit-scrollbar { display: none; }
}

.tags-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-btn {
  padding: 8px 16px;
  border: 1px solid #F97316;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  background: transparent;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  }

  &.active {
    color: white;
    background: #F97316;
    box-shadow: 0 4px 12px rgba(249, 115, 22, 0.3);
  }

  &.tag-btn-custom {
    border-style: dashed;
    color: #F97316;

    &:hover {
      background: rgba(249, 115, 22, 0.08);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    }
  }
}

.custom-tag-input {
  padding: 8px 14px;
  border: 1px dashed #F97316;
  border-radius: 20px;
  font-size: 14px;
  outline: none;
  background: white;
  min-width: 140px;
  transition: all 0.2s;

  &:focus {
    border-style: solid;
    box-shadow: 0 4px 12px rgba(249, 115, 22, 0.25);

    &::placeholder {
      color: #999;
    }
  }
}

.selected-tags {
  margin-top: 20px;
  padding-top: 20px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.selected-label {
  font-size: 13px;
}

.selected-tag {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  color: #1a1a1a;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s;

  .remove {
    font-size: 14px;
    opacity: 0.8;
  }
}

.edit-action {
  padding: 16px 24px 24px;
  display: flex;
  justify-content: center;
  flex-shrink: 0;
}

.btn-start-edit {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: transparent;
  color: #999;
  border: 1px solid #ddd;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: not-allowed;
  transition: all 0.2s;

  svg {
    width: 18px;
    height: 18px;
  }

  &.active {
    background: #F97316;
    color: white;
    border-color: #F97316;
    cursor: pointer;

    &:hover {
      box-shadow: 0 8px 24px rgba(249, 115, 22, 0.4);
      transform: translateY(-2px);
    }
  }
}
</style>
