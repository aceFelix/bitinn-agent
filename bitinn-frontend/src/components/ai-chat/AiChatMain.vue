<!--
  AI 聊天主面板
  核心聊天组件：消息发送、SSE 流式接收、Markdown 渲染、代码高亮、文件上传
  @author aceFelix
-->
<script setup>
import { ref, nextTick, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { sendMessage, sendMessageWithFiles, buildSSEUrl, getMessages, createConversation, getConversation } from '@/api/chat'
import AiSkillGuide from './AiSkillGuide.vue'
import { useTokenStore } from '@/stores/token'
import { userInfoStore } from '@/stores/userInfo'
import defaultAvatar from '@/assets/user-avatar1.jpg'
import hljs from 'highlight.js/lib/core'
import java from 'highlight.js/lib/languages/java'
import javascript from 'highlight.js/lib/languages/javascript'
import typescript from 'highlight.js/lib/languages/typescript'
import python from 'highlight.js/lib/languages/python'
import go from 'highlight.js/lib/languages/go'
import rust from 'highlight.js/lib/languages/rust'
import cpp from 'highlight.js/lib/languages/cpp'
import c_ from 'highlight.js/lib/languages/c'
import xml from 'highlight.js/lib/languages/xml'
import css from 'highlight.js/lib/languages/css'
import scss from 'highlight.js/lib/languages/scss'
import json from 'highlight.js/lib/languages/json'
import yaml from 'highlight.js/lib/languages/yaml'
import bash from 'highlight.js/lib/languages/bash'
import sql from 'highlight.js/lib/languages/sql'
import php from 'highlight.js/lib/languages/php'
import ruby from 'highlight.js/lib/languages/ruby'
import swift from 'highlight.js/lib/languages/swift'
import kotlin from 'highlight.js/lib/languages/kotlin'
import markdown from 'highlight.js/lib/languages/markdown'
import plaintext from 'highlight.js/lib/languages/plaintext'
import 'highlight.js/styles/github-dark.css'

const themeConfig = {
  'github-dark': { bg: '#0d1117', headerBg: '#161b22', colors: { keyword: '#ff7b72', string: '#a5d6ff', func: '#d2a8ff', comment: '#8b949e', number: '#79c0ff', builtin: '#ffa657', type: '#ffa657', variable: '#ffa657', attr: '#79c0ff', meta: '#8b949e', regexp: '#7ee787', title: '#d2a8ff', default: '#c9d1d9' } },
  'monokai-sublime': { bg: '#27272a', headerBg: '#2d2d30', colors: { keyword: '#f92672', string: '#e6db74', func: '#a6e22e', comment: '#75715e', number: '#ae81ff', builtin: '#66d9ef', type: '#66d9ef', variable: '#fd971f', attr: '#a6e22e', meta: '#75715e', regexp: '#e6db74', title: '#a6e22e', default: '#f8f8f2' } },
  'dracula': { bg: '#282a36', headerBg: '#21222c', colors: { keyword: '#ff79c6', string: '#f1fa8c', func: '#50fa7b', comment: '#6272a4', number: '#bd93f9', builtin: '#8be9fd', type: '#8be9fd', variable: '#50fa7b', attr: '#50fa7b', meta: '#6272a4', regexp: '#f1fa8c', title: '#50fa7b', default: '#f8f8f2' } },
  'atom-one-dark': { bg: '#282c34', headerBg: '#21252b', colors: { keyword: '#c678dd', string: '#98c379', func: '#61afef', comment: '#5c6370', number: '#d19a66', builtin: '#e5c07b', type: '#e5c07b', variable: '#e06c75', attr: '#d19a66', meta: '#5c6370', regexp: '#98c379', title: '#61afef', default: '#abb2bf' } },
  'vs2015': { bg: '#1e1e1e', headerBg: '#252526', colors: { keyword: '#569cd6', string: '#ce9178', func: '#dcdcaa', comment: '#6a9955', number: '#b5cea8', builtin: '#4ec9b0', type: '#4ec9b0', variable: '#9cdcfe', attr: '#9cdcfe', meta: '#6a9955', regexp: '#d16969', title: '#dcdcaa', default: '#d4d4d4' } },
  'nord': { bg: '#2e3440', headerBg: '#242933', colors: { keyword: '#81a1c1', string: '#a3be8c', func: '#88c0d0', comment: '#616e88', number: '#b48ead', builtin: '#8fbcbb', type: '#8fbcbb', variable: '#bf616a', attr: '#88c0d0', meta: '#616e88', regexp: '#a3be8c', title: '#88c0d0', default: '#eceff4' } },
  'solarized-dark': { bg: '#002b36', headerBg: '#00232c', colors: { keyword: '#859900', string: '#2aa198', func: '#268bd2', comment: '#586e75', number: '#d33682', builtin: '#b58900', type: '#b58900', variable: '#268bd2', attr: '#b58900', meta: '#586e75', regexp: '#dc322f', title: '#268bd2', default: '#839496' } },
  'tokyo-night-dark': { bg: '#1a1b26', headerBg: '#151620', colors: { keyword: '#bb9af7', string: '#9ece6a', func: '#7aa2f7', comment: '#565f89', number: '#ff9e64', builtin: '#e0af68', type: '#e0af68', variable: '#c0caf5', attr: '#7aa2f7', meta: '#565f89', regexp: '#9ece6a', title: '#7aa2f7', default: '#a9b1d6' } }
}

hljs.registerLanguage('java', java)
hljs.registerLanguage('javascript', javascript)
hljs.registerLanguage('typescript', typescript)
hljs.registerLanguage('python', python)
hljs.registerLanguage('go', go)
hljs.registerLanguage('rust', rust)
hljs.registerLanguage('cpp', cpp)
hljs.registerLanguage('c', c_)
hljs.registerLanguage('html', xml)
hljs.registerLanguage('xml', xml)
hljs.registerLanguage('css', css)
hljs.registerLanguage('scss', scss)
hljs.registerLanguage('json', json)
hljs.registerLanguage('yaml', yaml)
hljs.registerLanguage('bash', bash)
hljs.registerLanguage('shell', bash)
hljs.registerLanguage('sql', sql)
hljs.registerLanguage('php', php)
hljs.registerLanguage('ruby', ruby)
hljs.registerLanguage('swift', swift)
hljs.registerLanguage('kotlin', kotlin)
hljs.registerLanguage('markdown', markdown)
hljs.registerLanguage('plaintext', plaintext)

const props = defineProps({
  conversationId: { type: String, default: null },
  codeTheme: { type: String, default: 'github-dark' },
  initialMode: { type: String, default: 'normal' },
  initialModel: { type: String, default: 'qwen3.6-plus' }
})

const emit = defineEmits(['conversation-created', 'message-sent', 'title-updated'])

const currentTheme = ref('github-dark')
const currentThemeStyle = ref(themeConfig['github-dark'])

const applyThemeColors = (themeId) => {
  const config = themeConfig[themeId]
  if (!config) return

  const root = document.documentElement
  const prefix = '--hljs-'

  Object.entries(config.colors).forEach(([key, val]) => {
    root.style.setProperty(prefix + key, val)
  })

  currentTheme.value = themeId
  currentThemeStyle.value = config
}

const tokenStore = useTokenStore()
const userStore = userInfoStore()

const messages = ref([])
const inputMessage = ref('')
const isLoading = ref(false)
const chatContainer = ref(null)
const eventSource = ref(null)
const skipLoadMessages = ref(false)
const currentMode = ref('normal')
const currentModel = ref('qwen3.6-plus')

const MODEL_CONFIG = {
  normal: {
    models: [
      { id: 'deepseek-v4-pro', name: 'DeepSeek-V4-Pro', type: '文本', provider: 'DeepSeek' },
      { id: 'qwen3.7-max', name: 'Qwen3.7-Max', type: '文本', provider: '阿里云百炼' },
      { id: 'qwen3.6-plus', name: 'Qwen3.6-Plus', type: '视觉文本', provider: '阿里云百炼' }
    ],
    defaultModel: 'qwen3.6-plus'
  },
  vision: {
    models: [
      { id: 'qwen3.6-plus', name: 'Qwen3.6-Plus', type: '视觉文本', provider: '阿里云百炼' }
    ],
    defaultModel: 'qwen3.6-plus'
  },
  professional: {
    models: [
      { id: 'deepseek-v4-pro', name: 'DeepSeek-V4-Pro', type: '文本', provider: 'DeepSeek' },
      { id: 'qwen3.7-max', name: 'Qwen3.7-Max', type: '文本', provider: '阿里云百炼' },
      { id: 'qwen3.6-plus', name: 'Qwen3.6-Plus', type: '视觉文本', provider: '阿里云百炼' }
    ],
    defaultModel: 'qwen3.6-plus'
  },
  'image-gen': {
    models: [
      { id: 'qwen-image-2.0-pro', name: 'Qwen-Image-2.0-Pro', type: '视觉', provider: '阿里云百炼' }
    ],
    defaultModel: 'qwen-image-2.0-pro'
  }
}

const availableModels = computed(() => {
  return MODEL_CONFIG[currentMode.value]?.models || []
})

const isModelSelectionDisabled = computed(() => {
  return currentMode.value === 'vision' || currentMode.value === 'image-gen'
})

const uploadedFiles = ref([])

const showSkillGuide = ref(false)
const currentSkill = ref(null)
const pendingSkill = ref(null)

const userId = computed(() => userStore.userInfo?.id)
const userAvatar = computed(() => {
  const avatar = userStore.userInfo?.userPic || ''
  return avatar || defaultAvatar
})

const isVisionMode = computed(() => currentMode.value === 'vision')
const isImageGenMode = computed(() => currentMode.value === 'image-gen')

const switchMode = (mode) => {
  currentMode.value = mode
  currentModel.value = MODEL_CONFIG[mode]?.defaultModel || 'qwen3.6-plus'
}

const connectSSE = () => {
  if (!userId.value || !tokenStore.token) return

  if (eventSource.value) {
    eventSource.value.close()
  }

  const url = buildSSEUrl(userId.value)
  const es = new EventSource(url)

  es.addEventListener('connected', () => {})

  es.addEventListener('add', (event) => {
    const lastMsg = messages.value[messages.value.length - 1]
    if (lastMsg && lastMsg.role === 'assistant') {
      lastMsg.content += event.data
      nextTick(scrollToBottom)
    }
  })

  es.addEventListener('finish', () => {
    isLoading.value = false
    const lastMsg = messages.value[messages.value.length - 1]
    if (lastMsg && lastMsg.role === 'assistant') {
      lastMsg.time = formatTime()
    }
    nextTick(scrollToBottom)
  })

  es.addEventListener('title_update', (event) => {
    if (event.data && props.conversationId) {
      emit('title-updated', { conversationId: props.conversationId, title: event.data })
    }
  })

  es.addEventListener('error', (event) => {
    if (event.data) {
      ElMessage.error(event.data)
    }
    isLoading.value = false
    const lastMsg = messages.value[messages.value.length - 1]
    if (lastMsg && lastMsg.role === 'assistant' && !lastMsg.content) {
      lastMsg.content = 'AI服务暂时不可用，请稍后重试'
      lastMsg.time = formatTime()
    }
  })

  es.onerror = () => {}

  eventSource.value = es
}

const loadMessages = async () => {
  if (!props.conversationId) return
  try {
    const res = await getMessages(props.conversationId)
    messages.value = (res.data || []).map(msg => ({
      id: msg.id,
      role: msg.role,
      content: msg.content || '',
      time: formatTime(msg.createdAt)
    }))
    nextTick(scrollToBottom)
  } catch (e) {}
}

const loadConversationMode = async (conversationId) => {
  if (!conversationId) return
  try {
    const res = await getConversation(conversationId)
    if (res.data && res.data.mode) {
      currentMode.value = res.data.mode
    } else {
      currentMode.value = 'normal'
    }
    if (res.data && res.data.model) {
      currentModel.value = res.data.model
    } else {
      currentModel.value = MODEL_CONFIG[currentMode.value]?.defaultModel || 'qwen3.6-plus'
    }
  } catch (e) {
    currentMode.value = 'normal'
    currentModel.value = 'qwen3.6-plus'
  }
}

const send = async () => {
  const content = inputMessage.value.trim()
  if (!content) return
  if (isLoading.value) return

  const files = [...uploadedFiles.value]
  inputMessage.value = ''
  uploadedFiles.value = []

  let convId = props.conversationId
  if (!convId) {
    try {
      const res = await createConversation(null, currentModel.value, currentMode.value)
      convId = res.data.id
      skipLoadMessages.value = true
      emit('conversation-created', convId)
    } catch (e) {
      ElMessage.error('创建会话失败')
      return
    }
  }

  const userMsg = {
    id: 'user_' + Date.now(),
    role: 'user',
    content: (() => {
      if (files.length > 0) {
        const fileNames = files.map(f => f.name).join(', ')
        return content ? `${content}\n\n[附件: ${fileNames}]` : `[附件: ${fileNames}]`
      }
      return content
    })(),
    time: formatTime()
  }
  messages.value.push(userMsg)

  const aiMsg = {
    id: 'ai_' + Date.now(),
    role: 'assistant',
    content: '',
    time: ''
  }
  messages.value.push(aiMsg)

  isLoading.value = true
  await nextTick()
  scrollToBottom()

  try {
    if (files.length > 0) {
      const formData = new FormData()
      formData.append('conversationId', convId)
      formData.append('content', content)
      files.forEach(file => { formData.append('files', file.raw || file) })
      await sendMessageWithFiles(convId, formData)
    } else {
      const skillToSend = currentSkill.value?.icon || currentSkill.value
      await sendMessage(convId, content, null, skillToSend)
    }
    currentSkill.value = null
    emit('message-sent')
  } catch (e) {
    isLoading.value = false
    const lastMsg = messages.value[messages.value.length - 1]
    if (lastMsg && lastMsg.role === 'assistant') {
      lastMsg.content = '发送失败，请重试'
      lastMsg.time = formatTime()
    }
  }
}

const maxFileSize = 10 * 1024 * 1024

const getAllowedTypes = () => {
  if (isVisionMode.value || isImageGenMode.value) {
    return ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  }
  return ['application/pdf', 'text/plain', 'text/markdown']
}

const handleFileSelect = (e) => {
  const files = Array.from(e.target.files || [])
  const allowedTypes = getAllowedTypes()
  files.forEach(file => {
    if (!allowedTypes.includes(file.type)) {
      ElMessage.warning(`不支持的文件类型: ${file.name}`)
      return
    }
    if (file.size > maxFileSize) {
      ElMessage.warning(`文件过大(最大10MB): ${file.name}`)
      return
    }
    const preview = URL.createObjectURL(file)
    const isImage = file.type.startsWith('image/')
    uploadedFiles.value.push({
      name: file.name,
      size: file.size,
      type: file.type,
      raw: file,
      preview,
      isImage
    })
  })
  e.target.value = ''
}

const removeFile = (index) => {
  const file = uploadedFiles.value[index]
  if (file.preview) URL.revokeObjectURL(file.preview)
  uploadedFiles.value.splice(index, 1)
}

const formatFileSize = (bytes) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

const scrollToBottom = () => {
  const el = chatContainer.value
  if (el) el.scrollTop = el.scrollHeight
}

const handleKeyDown = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    send()
  }
}

const formatTime = (dateStr) => {
  if (!dateStr) return new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  return new Date(dateStr).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const escapeHtml = (text) => {
  return text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;')
}

const formatMessage = (content) => {
  if (!content) return ''

  return content
    .replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img src="$2" alt="$1" class="generated-image" loading="lazy" />')
    .replace(/```(\w*)\n([\s\S]*?)```/g, (_match, lang, code) => {
      const langName = lang || 'plaintext'
      const cleanCode = code.trimEnd()
      const escapedCode = escapeHtml(cleanCode)
      const codeId = 'code_' + Math.random().toString(36).slice(2, 9)

      let highlighted
      try {
        highlighted = hljs.highlight(cleanCode, { language: langName }).value
      } catch (e) {
        highlighted = escapedCode
      }

      return `<div class="shiki-code-block" data-lang="${langName}" data-id="${codeId}"><div class="shiki-header"><span class="shiki-lang">${langName}</span><button class="shiki-copy-btn" onclick="window.__copyCode('${codeId}')" title="复制代码"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/></svg><span class="copy-text">复制</span></button></div><pre class="hljs-pre"><code class="hljs-code">${highlighted}</code></pre><textarea class="shiki-raw" id="${codeId}" readonly>${escapedCode}</textarea></div>`
    })
    .replace(/`([^`\n]+)`/g, '<code class="inline-code">$1</code>')
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br>')
}

const copyMsg = (text) => {
  navigator.clipboard.writeText(text || '').then(() => ElMessage.success('已复制'))
}

watch(() => props.conversationId, (newId) => {
  if (!newId) {
    messages.value = []
    isLoading.value = false
    currentMode.value = 'normal'
    currentModel.value = 'qwen3.6-plus'
    return
  }
  if (skipLoadMessages.value) {
    skipLoadMessages.value = false
    return
  }
  currentMode.value = props.initialMode || 'normal'
  currentModel.value = props.initialModel || 'qwen3.6-plus'
  console.log('[Watch] convId=', newId, 'initialMode=', props.initialMode, 'setMode=', currentMode.value)
  loadMessages()
}, { immediate: true })

watch(() => props.codeTheme, (newTheme) => {
  if (newTheme && newTheme !== currentTheme.value) {
    applyThemeColors(newTheme)
  }
}, { immediate: false })

onMounted(() => {
  window.__copyCode = (id) => {
    const textarea = document.getElementById(id)
    if (textarea) {
      navigator.clipboard.writeText(textarea.value).then(() => {
        const btn = textarea.parentElement.querySelector('.shiki-copy-btn')
        if (btn) {
          btn.classList.add('copied')
          btn.querySelector('.copy-text').textContent = '已复制'
        }
        setTimeout(() => {
          if (btn) {
            btn.classList.remove('copied')
            const copyText = btn.querySelector('.copy-text')
            if (copyText) copyText.textContent = '复制'
          }
        }, 2000)
      })
    }
  }
  applyThemeColors(props.codeTheme)
  connectSSE()
  loadMessages()
})

onUnmounted(() => {
  if (eventSource.value) eventSource.value.close()
  delete window.__copyCode
})

const SKILL_PROMPTS = {
  resume: '请帮我优化简历，我的目标岗位是[在此输入岗位]，以下是我的经历：',
  code: '请帮我写一段代码实现以下功能：[在此描述需求]，要求代码完整可运行',
  data: '请帮我分析以下数据，给出趋势总结和可视化建议：',
  translate: '请帮我把以下内容翻译成[目标语言]：\n[在此粘贴内容]',
  summary: '请帮我总结以下内容的要点，提炼核心信息：\n[在此粘贴内容]'
}

const handleSkillSelect = (skill) => {
  if (skill.icon === 'article') {
    currentSkill.value = skill
    showSkillGuide.value = true
    return
  }
  const promptTemplate = SKILL_PROMPTS[skill.icon]
  if (promptTemplate) {
    switchMode('professional')
    inputMessage.value = promptTemplate
    nextTick(() => {
      const textarea = document.querySelector('.message-input')
      if (textarea) textarea.focus()
    })
  }
}

const handleSkillGuideSubmit = (prompt) => {
  showSkillGuide.value = false
  switchMode('professional')
  inputMessage.value = prompt
  nextTick(() => {
    const textarea = document.querySelector('.message-input')
    if (textarea) textarea.focus()
  })
}

const handleQuickAction = (text) => {
  inputMessage.value = text
  nextTick(() => send())
}

defineExpose({ handleSkillSelect, handleQuickAction, handleSkillGuideSubmit })
</script>

<template>
  <section class="chat-main">
    <div ref="chatContainer" class="chat-messages">
      <div v-if="messages.length === 0" class="empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="empty-icon">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          <line x1="9" y1="10" x2="15" y2="10"/>
          <line x1="12" y1="7" x2="12" y2="13"/>
        </svg>
        <h3 class="empty-title">开始和 AI 小B 对话吧</h3>
        <p class="empty-desc">我可以帮你解答技术问题、编写代码、撰写文章等</p>
        <div class="mode-selector">
          <button
            :class="['mode-btn', { active: currentMode === 'normal' }]"
            @click="switchMode('normal')"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="mode-icon">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
            <span class="mode-label">普通模式</span>
            <span class="mode-desc">文字对话</span>
          </button>
          <button
            :class="['mode-btn', { active: currentMode === 'vision' }]"
            @click="switchMode('vision')"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="mode-icon">
              <rect x="3" y="3" width="18" height="18" rx="2"/>
              <circle cx="8.5" cy="8.5" r="1.5"/>
              <path d="M21 15l-5-5L5 21"/>
            </svg>
            <span class="mode-label">识图模式</span>
            <span class="mode-desc">图片识别</span>
          </button>
          <button
            :class="['mode-btn', { active: currentMode === 'professional' }]"
            @click="switchMode('professional')"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="mode-icon">
              <path d="M12 2L2 7l10 5 10-5-10-5z"/>
              <path d="M2 17l10 5 10-5"/>
              <path d="M2 12l10 5 10-5"/>
            </svg>
            <span class="mode-label">专业模式</span>
            <span class="mode-desc">工具调用</span>
          </button>
          <button
            :class="['mode-btn', { active: currentMode === 'image-gen' }]"
            @click="switchMode('image-gen')"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="mode-icon">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <circle cx="8.5" cy="8.5" r="1.5"/>
              <polyline points="21 15 16 10 5 21"/>
            </svg>
            <span class="mode-label">生图模式</span>
            <span class="mode-desc">AI绘画</span>
          </button>
        </div>
        <div v-if="availableModels.length > 0" class="model-selector">
          <label class="model-label">选择模型</label>
          <div class="model-options">
            <button
              v-for="model in availableModels"
              :key="model.id"
              :class="['model-btn', { active: currentModel === model.id }]"
              :disabled="isModelSelectionDisabled"
              @click="currentModel = model.id"
            >
              <span class="model-name">{{ model.name }}</span>
              <span class="model-type">{{ model.type }}</span>
              <span class="model-provider">{{ model.provider }}</span>
            </button>
          </div>
        </div>
      </div>

      <div
        v-for="message in messages"
        :key="message.id"
        :class="['msg-row', message.role]"
      >
        <template v-if="message.role === 'assistant'">
          <div class="msg-avatar ai-avatar">
            <svg viewBox="0 0 24 24" fill="none" stroke="#F97316" stroke-width="1.8">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              <line x1="9" y1="10" x2="15" y2="10"/>
              <line x1="12" y1="7" x2="12" y2="13"/>
            </svg>
          </div>
          <div class="msg-bubble ai-bubble">
            <div class="msg-body" v-html="formatMessage(message.content)" :style="{ '--code-bg': currentThemeStyle.bg, '--header-bg': currentThemeStyle.headerBg }"></div>
            <div v-if="isLoading && !message.content && message === messages[messages.length - 1]" class="typing-dots">
              <span></span><span></span><span></span>
            </div>
            <div v-if="message.time" class="msg-time-row">
              <span class="msg-time">{{ message.time }}</span>
              <button class="btn-copy-msg" title="复制全部" @click="copyMsg(message.content)">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/></svg>
              </button>
            </div>
          </div>
        </template>

        <template v-else>
          <div class="msg-avatar user-avatar">
            <img :src="userAvatar" alt="user" class="avatar-img" />
          </div>
          <div class="msg-bubble user-bubble">
            <div class="msg-body">{{ message.content }}</div>
            <div v-if="message.time" class="msg-time-row">
              <span class="msg-time">{{ message.time }}</span>
            </div>
          </div>
        </template>
      </div>
    </div>

    <div class="chat-input-area">
      <div v-if="currentMode !== 'normal' && messages.length > 0" class="mode-badge">
        <svg v-if="currentMode === 'vision'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="mode-badge-icon">
          <rect x="3" y="3" width="18" height="18" rx="2"/>
          <circle cx="8.5" cy="8.5" r="1.5"/>
          <path d="M21 15l-5-5L5 21"/>
        </svg>
        <svg v-else-if="currentMode === 'professional'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="mode-badge-icon">
          <path d="M12 2L2 7l10 5 10-5-10-5z"/>
          <path d="M2 17l10 5 10-5"/>
          <path d="M2 12l10 5 10-5"/>
        </svg>
        <svg v-else-if="currentMode === 'image-gen'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="mode-badge-icon">
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
          <circle cx="8.5" cy="8.5" r="1.5"/>
          <polyline points="21 15 16 10 5 21"/>
        </svg>
        <span>{{ { vision: '识图模式', professional: '专业模式', 'image-gen': '生图模式' }[currentMode] }}</span>
        <span class="model-badge-name">{{ currentModel }}</span>
      </div>
      <div v-if="uploadedFiles.length > 0" class="file-preview-list">
        <div v-for="(file, index) in uploadedFiles" :key="index" class="file-preview-item">
          <img v-if="file.isImage" :src="file.preview" class="file-thumb" />
          <div v-else class="file-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
            </svg>
          </div>
          <span class="file-info">{{ file.name }} ({{ formatFileSize(file.size) }})</span>
          <button class="btn-remove-file" @click="removeFile(index)">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
      </div>

      <div class="input-wrapper">
        <div class="input-container">
          <label class="btn-upload" :title="(isVisionMode || isImageGenMode) ? '上传图片' : '上传文件'">
            <svg v-if="isVisionMode || isImageGenMode" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="18" height="18" rx="2"/>
              <circle cx="8.5" cy="8.5" r="1.5"/>
              <path d="M21 15l-5-5L5 21"/>
            </svg>
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21.44 11.05l-9.19 9.19a6 6 0 01-8.49-8.49l9.19-9.19a4 4 0 015.66 5.66l-9.2 9.19a2 2 0 01-2.83-2.83l8.49-8.48"/>
            </svg>
            <input type="file" multiple :accept="(isVisionMode || isImageGenMode) ? '.jpg,.jpeg,.png,.gif,.webp' : '.pdf,.txt,.md'" @change="handleFileSelect" hidden />
          </label>

          <textarea
            v-model="inputMessage"
            :placeholder="isImageGenMode ? '描述你想要生成的图片... (Enter 发送)' : isVisionMode ? '输入消息，上传图片让AI识别... (Enter 发送)' : '输入消息... (Enter 发送，Shift+Enter 换行)'"
            @keydown="handleKeyDown"
            rows="1"
            class="message-input"
            :disabled="isLoading"
          ></textarea>
        </div>

        <button
          class="btn-send"
          :disabled="(!inputMessage.trim() && uploadedFiles.length === 0) || isLoading"
          @click="send"
        >
          <svg v-if="!isLoading" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="22" y1="2" x2="11" y2="13"/>
            <polygon points="22 2 15 22 11 13 2 9 22 2"/>
          </svg>
          <div v-else class="loading-spinner small"></div>
        </button>
      </div>
      <div class="input-hints">
        <span class="hint">AI 可能会产生错误信息，请核实重要内容</span>
      </div>
    </div>
  </section>

  <AiSkillGuide
    :visible="showSkillGuide"
    :skill="currentSkill"
    @close="showSkillGuide = false"
    @submit="handleSkillGuideSubmit"
  />
</template>


<style scoped lang="scss">
.chat-main {
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  background: white;

  &:hover {
    box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
    transform: translateY(-2px);
  }
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;

  &::-webkit-scrollbar { width: 6px; }
  &::-webkit-scrollbar-track { background: #f8f9fa; }
  &::-webkit-scrollbar-thumb { background: rgba(249, 115, 22, 0.3); border-radius: 3px; &:hover { background: #F97316; } }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999;

  .empty-icon {
    width: 56px;
    height: 56px;
    stroke: #ddd;
    margin-bottom: 16px;
  }

  .empty-title {
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin: 0 0 8px;
  }

  .empty-desc {
    font-size: 14px;
    color: #999;
    margin: 0 0 24px;
  }

  .mode-selector {
    display: flex;
    gap: 12px;
    margin-top: 8px;

    .mode-btn {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 6px;
      padding: 16px 20px;
      border-radius: 12px;
      border: 2px solid #e5e7eb;
      background: white;
      cursor: pointer;
      transition: all 0.25s ease;
      min-width: 110px;

      .mode-icon {
        width: 28px;
        height: 28px;
        stroke: #9ca3af;
        transition: stroke 0.25s ease;
      }

      .mode-label {
        font-size: 14px;
        font-weight: 600;
        color: #6b7280;
        transition: color 0.25s ease;
      }

      .mode-desc {
        font-size: 11px;
        color: #9ca3af;
      }

      &:hover:not(.disabled) {
        border-color: #F97316;
        box-shadow: 0 4px 16px rgba(249, 115, 22, 0.15);
        transform: translateY(-2px);

        .mode-icon { stroke: #F97316; }
        .mode-label { color: #F97316; }
      }

      &.active {
        border-color: #F97316;
        background: linear-gradient(135deg, rgba(249, 115, 22, 0.08), rgba(249, 115, 22, 0.03));
        box-shadow: 0 4px 16px rgba(249, 115, 22, 0.2);

        .mode-icon { stroke: #F97316; }
        .mode-label { color: #F97316; }
      }

      &.disabled {
        opacity: 0.45;
        cursor: not-allowed;
        border-style: dashed;
      }
    }
  }

  .model-selector {
    margin-top: 20px;
    text-align: center;

    .model-label {
      font-size: 13px;
      font-weight: 600;
      color: #6b7280;
      margin-bottom: 10px;
      display: block;
    }

    .model-options {
      display: flex;
      gap: 8px;
      justify-content: center;
      flex-wrap: wrap;

      .model-btn {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 8px 14px;
        border-radius: 20px;
        border: 1.5px solid #e5e7eb;
        background: white;
        cursor: pointer;
        transition: all 0.2s ease;
        font-size: 12px;

        .model-name {
          font-weight: 600;
          color: #374151;
        }

        .model-type {
          padding: 1px 6px;
          border-radius: 8px;
          background: #f3f4f6;
          color: #6b7280;
          font-size: 10px;
        }

        .model-provider {
          color: #9ca3af;
          font-size: 10px;
        }

        &:hover:not(:disabled) {
          border-color: #F97316;
          box-shadow: 0 2px 8px rgba(249, 115, 22, 0.15);

          .model-name { color: #F97316; }
        }

        &.active {
          border-color: #F97316;
          background: linear-gradient(135deg, rgba(249, 115, 22, 0.08), rgba(249, 115, 22, 0.03));

          .model-name { color: #F97316; }
          .model-type {
            background: rgba(249, 115, 22, 0.1);
            color: #F97316;
          }
        }

        &:disabled {
          opacity: 0.6;
          cursor: not-allowed;
        }
      }
    }
  }
}

.loading-spinner {
  width: 28px;
  height: 28px;
  border: 3px solid rgba(249, 115, 22, 0.15);
  border-top-color: #F97316;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;

  &.small {
    width: 16px;
    height: 16px;
    border-width: 2px;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.msg-row {
  display: flex;
  gap: 10px;
  max-width: 85%;

  &.assistant { align-self: flex-start; }
  &.user {
    align-self: flex-end;
    flex-direction: row-reverse;
  }
}

.msg-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;

  svg { width: 20px; height: 20px; }

  .avatar-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  &.ai-avatar { background: #fff5eb; }
  &.user-avatar {
    background: linear-gradient(135deg, #F97316, #ea580c);
    border: 2px solid rgba(249, 115, 22, 0.3);
  }
}

.msg-bubble {
  padding: 12px 16px;
  border-radius: 12px;
  position: relative;

  .msg-body {
    font-size: 14px;
    line-height: 1.7;
    word-break: break-word;

    :deep(.shiki-code-block) {
      position: relative;
      margin: 10px 0;
      border-radius: 10px;
      overflow: hidden;
      border: 1px solid rgba(255, 255, 255, 0.1);
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.4);

      .shiki-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 8px 14px;
        background: var(--header-bg, #161b22);
        border-bottom: 1px solid rgba(255, 255, 255, 0.1);

        .shiki-lang {
          font-size: 11px;
          font-weight: 600;
          text-transform: uppercase;
          letter-spacing: 0.05em;
          color: #888;
          font-family: 'SF Mono', Menlo, Consolas, monospace;
        }

        .shiki-copy-btn {
          display: inline-flex;
          align-items: center;
          gap: 4px;
          padding: 4px 10px;
          border: 1px solid rgba(255, 255, 255, 0.15);
          border-radius: 6px;
          background: transparent;
          color: #888;
          cursor: pointer;
          font-size: 11px;
          transition: all 0.2s ease;
          font-family: inherit;

          svg {
            width: 13px;
            height: 13px;
            flex-shrink: 0;
          }

          .copy-text { transition: opacity 0.2s; }

          &:hover {
            color: #fff;
            border-color: rgba(255, 255, 255, 0.25);
            background: rgba(255, 255, 255, 0.08);
          }

          &.copied {
            color: #7ee787;
            border-color: rgba(126, 231, 135, 0.3);
            background: rgba(126, 231, 135, 0.1);
          }
        }
      }

      .shiki-raw {
        position: absolute;
        left: -9999px;
        top: 0;
        opacity: 0;
        pointer-events: none;
      }

      .hljs-pre {
        margin: 0 !important;
        padding: 14px 18px !important;
        background: var(--code-bg, #0d1117) !important;
        border-radius: 0 !important;
        overflow-x: auto;
        font-size: 13px !important;
        line-height: 1.65 !important;

        .hljs,
        .hljs-code {
          font-family: 'SF Mono', Menlo, Consolas, 'Liberation Mono', monospace !important;
          font-size: inherit !important;
          background: transparent !important;
          color: var(--hljs-default, #c9d1d9) !important;
          padding: 0 !important;

          & > * {
            font-family: inherit !important;
          }
        }

        .hljs { color: var(--hljs-default, #c9d1d9); }
        .hljs-comment,
        .hljs-quote { color: var(--hljs-comment, #8b949e); font-style: italic; }
        .hljs-keyword,
        .hljs-selector-tag,
        .hljs-type { color: var(--hljs-keyword, #ff7b72); }
        .hljs-string,
        .hljs-addition { color: var(--hljs-string, #a5d6ff); }
        .hljs-number,
        .hljs-literal { color: var(--hljs-number, #79c0ff); }
        .hljs-built_in,
        .hljs-builtin-name { color: var(--hljs-builtin, #ffa657); }
        .hljs-title,
        .hljs-section,
        .hljs-selector-id { color: var(--hljs-title, #d2a8ff); }
        .hljs-function .hljs-title,
        .hljs-title.function_ { color: var(--hljs-func, #d2a8ff); }
        .hljs-variable,
        .hljs-template-variable,
        .hljs-attr { color: var(--hljs-variable, #ffa657); }
        .hljs-class .hljs-title,
        .hljs-title.class_ { color: var(--hljs-type, #ffa657); }
        .hljs-params { color: var(--hljs-default, #c9d1d9); }
        .hljs-meta { color: var(--hljs-meta, #8b949e); }
        .hljs-regexp,
        .hljs-symbol { color: var(--hljs-regexp, #7ee787); }
        .hljs-deletion { color: #ffa198; background: rgba(248,81,73,0.1); }
        .hljs-addition { color: #aff5b4; background: rgba(46,160,67,0.15); }
        .hljs-emphasis { font-style: italic; }
        .hljs-strong { font-weight: bold; }
        .hljs-doctag { color: var(--hljs-meta, #8b949e); }
        .hljs-selector-class,
        .hljs-selector-attr { color: var(--hljs-regexp, #7ee787); }
        .hljs-property,
        .hljs-attribute { color: var(--hljs-attr, #79c0ff); }
        .hljs-subst { color: var(--hljs-default, #c9d1d9); }
        .hljs-bullet { color: #f2cc60; }
        .hljs-link { color: #58a6ff; text-decoration: underline; }
      }
    }

    :deep(.inline-code) {
      background: rgba(249, 115, 22, 0.08);
      color: #ea580c;
      padding: 2px 6px;
      border-radius: 4px;
      font-size: 13px;
      font-family: 'SF Mono', Menlo, Consolas, monospace;
    }

    :deep(strong) {
      color: #1a1a1a;
    }

    :deep(.generated-image) {
      max-width: 100%;
      border-radius: 10px;
      margin: 10px 0;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
        transform: scale(1.02);
      }
    }
  }

  .msg-time-row {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 6px;

    .msg-time {
      font-size: 11px;
      color: #999;
    }

    .btn-copy-msg {
      width: 24px;
      height: 24px;
      display: flex;
      align-items: center;
      justify-content: center;
      border: none;
      background: transparent;
      border-radius: 4px;
      cursor: pointer;
      color: #999;
      transition: all 0.2s;

      &:hover {
        color: #F97316;
        background: rgba(249, 115, 22, 0.1);
      }
    }
  }

  &.ai-bubble {
    background: #f8f9fa;
    border-top-left-radius: 4px;
  }

  &.user-bubble {
    background: linear-gradient(135deg, #F97316, #ea580c);
    color: white;
    border-top-right-radius: 4px;

    .msg-body { color: white; }
    .msg-time { color: rgba(255, 255, 255, 0.7); }
  }
}

.typing-dots {
  display: flex;
  gap: 4px;
  padding: 4px 0;

  span {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #ccc;
    animation: typing 1.4s infinite;

    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-4px); opacity: 1; }
}

.chat-input-area {
  border-top: 1px solid #f0f0f0;
  padding: 16px 20px;

  .mode-badge {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 10px;
    border-radius: 12px;
    background: linear-gradient(135deg, rgba(249, 115, 22, 0.1), rgba(249, 115, 22, 0.05));
    border: 1px solid rgba(249, 115, 22, 0.3);
    margin-bottom: 8px;
    font-size: 12px;
    color: #F97316;
    font-weight: 500;

    .mode-badge-icon {
      width: 14px;
      height: 14px;
      stroke: #F97316;
    }

    .model-badge-name {
      padding: 1px 6px;
      border-radius: 6px;
      background: rgba(249, 115, 22, 0.15);
      font-size: 10px;
      color: #ea580c;
      font-weight: 500;
    }
  }

  .input-wrapper {
    display: flex;
    gap: 10px;
    align-items: flex-end;

    .input-container {
      flex: 1;
      position: relative;
      display: flex;
      align-items: flex-end;
    }

    .btn-upload {
      position: absolute;
      left: 12px;
      bottom: 10px;
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 6px;
      border: none;
      background: transparent;
      cursor: pointer;
      transition: all 0.2s;
      flex-shrink: 0;
      z-index: 1;

      svg { width: 18px; height: 18px; stroke: #888; }

      &:hover {
        background: rgba(249, 115, 22, 0.05);
        svg { stroke: #F97316; }
      }
    }

    .message-input {
      flex: 1;
      padding: 12px 16px 12px 48px;
      border: 1px solid #e5e7eb;
      border-radius: 10px;
      font-size: 14px;
      resize: none;
      outline: none;
      font-family: inherit;
      line-height: 1.5;
      max-height: 120px;
      transition: border-color 0.2s;

      &:focus {
        border-color: #F97316;
        box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.1);
      }

      &:disabled { background: #f8f9fa; }
      &::placeholder { color: #bbb; }
    }

    .btn-send {
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      border: none;
      border-radius: 10px;
      background: linear-gradient(135deg, #F97316, #ea580c);
      color: white;
      cursor: pointer;
      transition: all 0.2s;
      flex-shrink: 0;

      svg { width: 18px; height: 18px; }

      &:hover:not(:disabled) {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(249, 115, 22, 0.4);
      }

      &:disabled { opacity: 0.4; cursor: not-allowed; }
    }
  }

  .input-hints {
    text-align: center;
    margin-top: 8px;

    .hint { font-size: 12px; color: #bbb; }
  }

  .file-preview-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 10px;

    .file-preview-item {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 6px 10px;
      background: #f0f4ff;
      border: 1px solid #d4e0f7;
      border-radius: 8px;
      max-width: 260px;

      .file-thumb {
        width: 32px;
        height: 32px;
        object-fit: cover;
        border-radius: 4px;
        flex-shrink: 0;
      }

      .file-icon {
        width: 32px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #e8edf5;
        border-radius: 4px;
        flex-shrink: 0;

        svg {
          width: 16px;
          height: 16px;
          stroke: #6b7fa3;
        }
      }

      .file-info {
        font-size: 12px;
        color: #555;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        flex: 1;
        min-width: 0;
      }

      .btn-remove-file {
        width: 20px;
        height: 20px;
        display: flex;
        align-items: center;
        justify-content: center;
        border: none;
        background: transparent;
        cursor: pointer;
        border-radius: 4px;
        padding: 0;
        flex-shrink: 0;
        transition: all 0.15s;

        svg { width: 12px; height: 12px; stroke: #999; }

        &:hover {
          background: rgba(239, 68, 68, 0.1);
          svg { stroke: #ef4444; }
        }
      }
    }
  }
}
</style>
