<!--
  代码主题选择器
  切换 AI 聊天中代码块的高亮主题（GitHub Dark / Monokai / One Dark Pro 等）
  @author aceFelix
-->
<script setup>
import { ref, onMounted } from 'vue'

const props = defineProps({
  modelValue: { type: String, default: 'github-dark' }
})

const emit = defineEmits(['update:modelValue'])

const themes = [
  {
    id: 'github-dark',
    name: 'GitHub Dark',
    bg: '#0d1117',
    preview: { keyword: '#ff7b72', string: '#a5d6ff', func: '#d2a8ff', comment: '#8b949e', builtin: '#ffa657' }
  },
  {
    id: 'monokai-sublime',
    name: 'Monokai',
    bg: '#27272a',
    preview: { keyword: '#f92672', string: '#e6db74', func: '#a6e22e', comment: '#75715e', builtin: '#66d9ef' }
  },
  {
    id: 'dracula',
    name: 'Dracula',
    bg: '#282a36',
    preview: { keyword: '#ff79c6', string: '#f1fa8c', func: '#50fa7b', comment: '#6272a4', builtin: '#8be9fd' }
  },
  {
    id: 'atom-one-dark',
    name: 'Atom One Dark',
    bg: '#282c34',
    preview: { keyword: '#c678dd', string: '#98c379', func: '#61afef', comment: '#5c6370', builtin: '#e5c07b' }
  },
  {
    id: 'vs2015',
    name: 'VS Code Dark',
    bg: '#1e1e1e',
    preview: { keyword: '#569cd6', string: '#ce9178', func: '#dcdcaa', comment: '#6a9955', builtin: '#4ec9b0' }
  },
  {
    id: 'nord',
    name: 'Nord',
    bg: '#2e3440',
    preview: { keyword: '#81a1c1', string: '#a3be8c', func: '#88c0d0', comment: '#616e88', builtin: '#ebcb8b' }
  },
  {
    id: 'solarized-dark',
    name: 'Solarized Dark',
    bg: '#002b36',
    preview: { keyword: '#859900', string: '#2aa198', func: '#268bd2', comment: '#586e75', builtin: '#b58900' }
  },
  {
    id: 'tokyo-night-dark',
    name: 'Tokyo Night',
    bg: '#1a1b26',
    preview: { keyword: '#bb9af7', string: '#9ece6a', func: '#7aa2f7', comment: '#565f89', builtin: '#e0af68' }
  }
]

const selectedId = ref(props.modelValue)

const selectTheme = (id) => {
  selectedId.value = id
  emit('update:modelValue', id)
}

onMounted(() => {
  const saved = localStorage.getItem('ai-code-theme')
  if (saved) {
    selectedId.value = saved
    emit('update:modelValue', saved)
  }
})

const handleSelect = (id) => {
  selectTheme(id)
  localStorage.setItem('ai-code-theme', id)
}
</script>

<template>
  <div class="code-theme-selector">
    <label class="theme-label">代码主题</label>
    <div class="theme-grid">
      <div
        v-for="theme in themes"
        :key="theme.id"
        :class="['theme-card', { active: selectedId === theme.id }]"
        @click="handleSelect(theme.id)"
      >
        <div class="theme-preview" :style="{ background: theme.bg }">
          <span :style="{ color: theme.preview.keyword }">public</span>
          <span :style="{ color: theme.preview.string }">"</span>
          <span :style="{ color: theme.preview.func }">hello</span>
          <span :style="{ color: theme.preview.string }">"</span>
          <span style="color:#888">{</span>
          <span :style="{ color: theme.preview.comment }">// code</span>
          <span style="color:#888">}</span>
        </div>
        <span class="theme-name">{{ theme.name }}</span>
        <svg v-if="selectedId === theme.id" class="check-icon" viewBox="0 0 24 24" fill="none" stroke="#F97316" stroke-width="2.5">
          <polyline points="20 6 9 17 4 12"/>
        </svg>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.code-theme-selector {
  display: flex;
  flex-direction: column;
  gap: 10px;

  .theme-label {
    font-size: 13px;
    font-weight: 600;
    color: #333;
  }

  .theme-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 10px;

    .theme-card {
      position: relative;
      cursor: pointer;
      border-radius: 8px;
      overflow: hidden;
      border: 2px solid transparent;
      background: #f8f9fa;
      transition: all 0.2s ease;

      &:hover {
        border-color: rgba(249, 115, 22, 0.3);
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
      }

      &.active {
        border-color: #F97316;
        box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.15);
      }

      .theme-preview {
        padding: 10px 12px;
        font-family: 'SF Mono', Menlo, Consolas, monospace;
        font-size: 11px;
        line-height: 1.7;
        white-space: nowrap;
        overflow: hidden;
      }

      .theme-name {
        display: block;
        text-align: center;
        padding: 6px 4px;
        font-size: 11px;
        font-weight: 500;
        color: #555;
        background: #fff;
        border-top: 1px solid #eee;
      }

      .check-icon {
        position: absolute;
        top: 6px;
        right: 6px;
        width: 16px;
        height: 16px;
        background: white;
        border-radius: 50%;
        padding: 2px;
        z-index: 1;
      }
    }
  }

  @media (max-width: 480px) {
    .theme-grid {
      grid-template-columns: repeat(2, 1fr);
    }
  }
}
</style>
