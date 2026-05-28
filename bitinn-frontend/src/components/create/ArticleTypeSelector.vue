<!--
  文章类型选择器
  展示可选的文章类型（技术文章、学习笔记、项目分享等），每种类型附带标签推荐
  @author aceFelix
-->
<script setup>
import { ref, nextTick } from 'vue'

const colors = ['#3592C4', '#499C54', '#CC7832', '#9876AA', '#E06C75', '#56B6C2', '#C586C0', '#D19A66']
const icons = ['📄', '🎯', '💡', '🔧', '📊', '🔬', '⚡', '🌟']

// 文章类型选项
const articleTypes = ref([
  {
    id: 'tech',
    name: '技术文章',
    description: '分享技术经验、教程、最佳实践',
    icon: '💻',
    color: '#3592C4',
    tags: ['Java', 'Python', 'Vue', 'React', 'Spring Boot']
  },
  {
    id: 'note',
    name: '学习笔记',
    description: '记录学习心得、知识点整理',
    icon: '📝',
    color: '#499C54',
    tags: ['读书笔记', '源码分析', '算法', '设计模式']
  },
  {
    id: 'discussion',
    name: '问题讨论',
    description: '提出技术问题、寻求解决方案',
    icon: '💬',
    color: '#CC7832',
    tags: ['Bug解决', '架构设计', '性能优化']
  },
  {
    id: 'project',
    name: '项目分享',
    description: '展示个人项目、开源作品',
    icon: '🚀',
    color: '#9876AA',
    tags: ['开源项目', '工具推荐', '实战案例']
  }
])

// 选中的文章类型
const selectedType = ref(null)

// 自定义类型输入
const showCustomForm = ref(false)
const customName = ref('')
const customDesc = ref('')
const customIconIndex = ref(0)
const customColorIndex = ref(0)
let customIdCounter = 5

// 选择文章类型
const selectType = (type) => {
  selectedType.value = type
}

// 显示自定义类型表单
const showCustomTypeForm = () => {
  showCustomForm.value = true
}

// 添加自定义类型
const addCustomType = () => {
  const name = customName.value.trim()
  const desc = customDesc.value.trim()
  if (!name) return
  const newType = {
    id: 'custom_' + (customIdCounter++),
    name,
    description: desc || '自定义类型',
    icon: icons[customIconIndex.value],
    color: colors[customColorIndex.value],
    tags: [],
    isCustom: true
  }
  articleTypes.value.push(newType)
  selectType(newType)
  resetCustomForm()
}

// 删除自定义类型
const removeCustomType = (type) => {
  const index = articleTypes.value.findIndex(t => t.id === type.id)
  if (index > -1) {
    articleTypes.value.splice(index, 1)
    if (selectedType.value?.id === type.id) {
      selectedType.value = null
    }
  }
}

// 取消自定义表单
const cancelCustomForm = () => {
  resetCustomForm()
}

// 重置自定义表单
const resetCustomForm = () => {
  customName.value = ''
  customDesc.value = ''
  customIconIndex.value = 0
  customColorIndex.value = 0
  showCustomForm.value = false
}

// 暴露选中类型给父组件
const getSelectedType = () => selectedType.value

defineExpose({
  getSelectedType,
  selectedType
})
</script>

<template>
  <section class="type-section">
    <h2 class="section-title">选择文章类型</h2>
    <div class="type-grid">
      <div
        v-for="type in articleTypes"
        :key="type.id"
        class="type-card"
        :class="{ active: selectedType?.id === type.id }"
        @click="selectType(type)"
      >
        <div v-if="type.isCustom" class="custom-remove" @click.stop="removeCustomType(type)">×</div>
        <div class="type-icon" :style="{ background: type.color + '20', color: type.color }">
          {{ type.icon }}
        </div>
        <h3 class="type-name">{{ type.name }}</h3>
        <p class="type-desc">{{ type.description }}</p>
        <div class="type-tags" v-if="type.tags.length > 0">
          <span v-for="tag in type.tags.slice(0, 3)" :key="tag" class="tag">
            {{ tag }}
          </span>
        </div>
      </div>
    </div>

    <!-- 自定义按钮/表单 -->
    <div class="custom-type-area">
      <button v-if="!showCustomForm" class="btn-add-custom" @click="showCustomTypeForm">
        + 自定义类型
      </button>
      <div v-else class="custom-form">
        <input v-model="customName" class="custom-input" placeholder="类型名称（必填）" />
        <textarea v-model="customDesc" class="custom-textarea" placeholder="类型描述（选填）"></textarea>
        <div class="custom-options">
          <div class="option-group">
            <label>图标：</label>
            <div class="icon-picker">
              <button
                v-for="(icon, idx) in icons"
                :key="idx"
                :class="{ active: customIconIndex === idx }"
                @click="customIconIndex = idx"
              >{{ icon }}</button>
            </div>
          </div>
          <div class="option-group">
            <label>颜色：</label>
            <div class="color-picker">
              <button
                v-for="(color, idx) in colors"
                :key="idx"
                :class="{ active: customColorIndex === idx }"
                :style="{ background: color }"
                @click="customColorIndex = idx"
              ></button>
            </div>
          </div>
        </div>
        <div class="form-actions">
          <button class="btn-confirm" @click="addCustomType">确认添加</button>
          <button class="btn-cancel" @click="cancelCustomForm">取消</button>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.type-section {
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
    margin-bottom: 16px;
    font-size: 18px;
    font-weight: 600;
  }

  .type-grid {
    flex: 1;
    overflow-y: auto;
    padding: 0 24px;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
    align-content: start;
    scrollbar-width: none;
    -ms-overflow-style: none;

    &::-webkit-scrollbar { display: none; }
  }

  .custom-type-area {
    flex-shrink: 0;
    padding: 16px 24px 24px;
    margin-top: 20px;
    border-top: 1px dashed rgba(0, 0, 0, 0.12);
  }
}

.type-card {
  position: relative;
  border: 2px solid transparent;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
  }

  &.active {
    box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
    transform: translateY(-2px);
  }
}

.type-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-bottom: 12px;
}

.type-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}

.type-desc {
  font-size: 13px;
  margin-bottom: 12px;
  line-height: 1.5;
}

.type-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;

  .tag {
    padding: 3px 8px;
    border-radius: 4px;
    font-size: 11px;
  }
}

.custom-remove {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #E06C75;
    color: white;
  }
}

.btn-add-custom {
  width: 100%;
  padding: 10px;
  border: 1px dashed #F97316;
  border-radius: 10px;
  font-size: 14px;
  color: #F97316;
  cursor: pointer;
  transition: all 0.2s;
  background: transparent;

  &:hover {
    background: rgba(249, 115, 22, 0.08);
    box-shadow: 0 4px 12px rgba(249, 115, 22, 0.15);
  }
}

.custom-form {
  padding: 16px;
  border: 1px solid rgba(249, 115, 22, 0.3);
  border-radius: 12px;
  background: rgba(249, 115, 22, 0.03);

  .custom-input,
  .custom-textarea {
    width: 100%;
    padding: 8px 12px;
    border: 1px solid #ddd;
    border-radius: 8px;
    font-size: 13px;
    outline: none;
    transition: all 0.2s;
    margin-bottom: 10px;
    box-sizing: border-box;

    &:focus {
      border-color: #F97316;
      box-shadow: 0 4px 12px rgba(249, 115, 22, 0.15);
    }

    &::placeholder {
      color: #999;
    }
  }

  .custom-textarea {
    min-height: 50px;
    resize: vertical;
  }
}

.custom-options {
  .option-group {
    margin-bottom: 10px;

    label {
      font-size: 12px;
      color: #666;
      margin-bottom: 4px;
      display: block;
    }
  }

  .icon-picker,
  .color-picker {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;

    button {
      width: 30px;
      height: 30px;
      border: 2px solid transparent;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 16px;
      background: white;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

      &:hover {
        transform: scale(1.1);
      }

      &.active {
        border-color: #F97316;
        box-shadow: 0 2px 8px rgba(249, 115, 22, 0.3);
      }
    }
  }

  .color-picker button {
    background: var(--bg);
  }
}

.form-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;

  .btn-confirm,
  .btn-cancel {
    flex: 1;
    padding: 8px;
    border-radius: 8px;
    font-size: 13px;
    cursor: pointer;
    transition: all 0.2s;
  }

  .btn-confirm {
    background: #F97316;
    color: white;
    border: none;

    &:hover {
      box-shadow: 0 4px 12px rgba(249, 115, 22, 0.3);
      transform: translateY(-1px);
    }
  }

  .btn-cancel {
    background: transparent;
    border: 1px solid #ddd;
    color: #666;

    &:hover {
      border-color: #999;
    }
  }
}
</style>
