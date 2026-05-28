<!--
  设置面板
  包含通用设置、隐私设置和其他扩展的 Tab 切换面板
  @author aceFelix
-->
<script setup>
import { ref } from 'vue'

// 当前激活的Tab
const activeTab = ref('general')

// Tab配置
const tabs = [
  { id: 'general', label: '通用设置', icon: 'general' },
  { id: 'privacy', label: '隐私设置', icon: 'privacy' },
  { id: 'extension', label: '其他扩展', icon: 'extension' }
]

// 通用设置项
const generalSettings = ref([
  {
    id: 'theme',
    label: '主题模式',
    description: '选择您喜欢的界面主题',
    type: 'select',
    value: 'light',
    options: [
      { label: '浅色模式', value: 'light' },
      { label: '深色模式', value: 'dark' },
      { label: '跟随系统', value: 'system' }
    ]
  },
  {
    id: 'language',
    label: '语言',
    description: '选择界面显示语言',
    type: 'select',
    value: 'zh',
    options: [
      { label: '简体中文', value: 'zh' },
      { label: 'English', value: 'en' }
    ]
  },
  {
    id: 'fontSize',
    label: '字体大小',
    description: '调整文章阅读字体大小',
    type: 'select',
    value: 'medium',
    options: [
      { label: '小', value: 'small' },
      { label: '中', value: 'medium' },
      { label: '大', value: 'large' }
    ]
  },
  {
    id: 'autoSave',
    label: '自动保存草稿',
    description: '编辑文章时自动保存草稿',
    type: 'toggle',
    value: true
  },
  {
    id: 'notification',
    label: '消息通知',
    description: '接收新消息和互动提醒',
    type: 'toggle',
    value: true
  }
])

// 隐私设置项
const privacySettings = ref([
  {
    id: 'profileVisible',
    label: '个人资料可见',
    description: '允许其他用户查看您的个人资料',
    type: 'toggle',
    value: true
  },
  {
    id: 'articleVisible',
    label: '文章公开',
    description: '发布的文章默认对所有用户可见',
    type: 'toggle',
    value: true
  },
  {
    id: 'followApproval',
    label: '关注需审核',
    description: '其他用户关注您时需要您的确认',
    type: 'toggle',
    value: false
  },
  {
    id: 'showOnline',
    label: '显示在线状态',
    description: '向其他用户展示您的在线状态',
    type: 'toggle',
    value: true
  },
  {
    id: 'searchEngine',
    label: '允许搜索引擎收录',
    description: '允许搜索引擎索引您的公开文章',
    type: 'toggle',
    value: true
  }
])

// 扩展设置项
const extensionSettings = ref([
  {
    id: 'shortcut',
    label: '快捷键支持',
    description: '启用键盘快捷键提升编辑效率',
    type: 'toggle',
    value: true
  },
  {
    id: 'codeHighlight',
    label: '代码高亮',
    description: '文章中的代码块启用语法高亮',
    type: 'toggle',
    value: true
  },
  {
    id: 'mathSupport',
    label: '数学公式支持',
    description: '支持 LaTeX 数学公式渲染',
    type: 'toggle',
    value: false
  },
  {
    id: 'mermaid',
    label: 'Mermaid 图表',
    description: '支持 Mermaid 流程图和图表',
    type: 'toggle',
    value: false
  },
  {
    id: 'exportPdf',
    label: 'PDF 导出',
    description: '允许将文章导出为 PDF 格式',
    type: 'toggle',
    value: true
  }
])

// 切换Tab
const switchTab = (tabId) => {
  activeTab.value = tabId
}

// 切换开关
const toggleSetting = (setting) => {
  setting.value = !setting.value
}

// 获取当前Tab的设置项
const currentSettings = computed(() => {
  switch (activeTab.value) {
    case 'general':
      return generalSettings.value
    case 'privacy':
      return privacySettings.value
    case 'extension':
      return extensionSettings.value
    default:
      return []
  }
})
</script>

<template>
  <section class="content-feed">
    <div class="feed-header">
      <div class="sort-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          :class="['sort-tab', { active: activeTab === tab.id }]"
          @click="switchTab(tab.id)"
        >
          {{ tab.label }}
        </button>
      </div>
    </div>

    <div class="settings-list">
      <div
        v-for="setting in currentSettings"
        :key="setting.id"
        class="setting-item"
      >
        <div class="setting-info">
          <div class="setting-label">{{ setting.label }}</div>
          <div class="setting-desc">{{ setting.description }}</div>
        </div>
        <div class="setting-control">
          <select
            v-if="setting.type === 'select'"
            v-model="setting.value"
            class="setting-select"
          >
            <option
              v-for="opt in setting.options"
              :key="opt.value"
              :value="opt.value"
            >
              {{ opt.label }}
            </option>
          </select>
          <div
            v-else-if="setting.type === 'toggle'"
            :class="['toggle-switch', { active: setting.value }]"
            @click="toggleSetting(setting)"
          >
            <div class="toggle-slider"></div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.content-feed {
  min-width: 0;
  height: 100%;
  overflow-y: auto;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
    transform: translateY(-2px);
  }

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(249, 115, 22, 0.3);
    border-radius: 4px;

    &:hover {
      background: #F97316;
    }
  }
}

.feed-header {
  border-radius: 12px;
  padding: 4px;
  margin-bottom: 16px;
}

.sort-tabs {
  display: flex;
  gap: 4px;
}

.sort-tab {
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

.settings-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-radius: 10px;
  transition: all 0.2s;

  &:hover {
    background: #fafafa;
  }
}

.setting-info {
  flex: 1;
  min-width: 0;

  .setting-label {
    font-size: 14px;
    font-weight: 500;
    color: #1a1a1a;
    margin-bottom: 4px;
  }

  .setting-desc {
    font-size: 12px;
    color: #888;
  }
}

.setting-control {
  flex-shrink: 0;
  margin-left: 16px;
}

.setting-select {
  padding: 8px 32px 8px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 13px;
  color: #1a1a1a;
  background: white;
  cursor: pointer;
  transition: all 0.2s;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%23666' stroke-width='2'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 10px center;

  &:hover {
    border-color: #F97316;
  }

  &:focus {
    outline: none;
    border-color: #F97316;
    box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.1);
  }
}

.toggle-switch {
  width: 44px;
  height: 24px;
  border-radius: 12px;
  background: #e0e0e0;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  flex-shrink: 0;

  .toggle-slider {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: white;
    position: absolute;
    top: 2px;
    left: 2px;
    transition: all 0.2s;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  }

  &.active {
    background: #F97316;

    .toggle-slider {
      left: 22px;
    }
  }
}

@media (max-width: 768px) {
  .sort-tab {
    padding: 8px 12px;
    font-size: 13px;
  }

  .setting-item {
    padding: 12px 16px;
  }
}
</style>
