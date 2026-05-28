<!--
  登录页品牌展示区
  展示代码编辑器动画效果和品牌标语，点击返回首页
  @author aceFelix
-->
<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<template>
  <div class="auth-brand">
    <div class="logo-animation" @click="goHome">
      <div class="code-editor">
        <div class="editor-header">
          <div class="window-controls">
            <span class="dot red"></span>
            <span class="dot yellow"></span>
            <span class="dot green"></span>
          </div>
          <span class="file-name">BitInn.java</span>
        </div>
        <div class="editor-body">
          <div class="line-numbers">
            <span v-for="n in 8" :key="n">{{ n }}</span>
          </div>
          <div class="code-content">
            <div class="code-line">
              <span class="keyword">public</span>
              <span class="keyword">class</span>
              <span class="class-name">BitInn</span>
              <span class="bracket">{</span>
            </div>
            <div class="code-line indent-1">
              <span class="comment">// 开发者技术社区</span>
            </div>
            <div class="code-line indent-1">
              <span class="keyword">private</span>
              <span class="type">String</span>
              <span class="variable">mission</span>
              <span class="operator">=</span>
              <span class="string">"分享知识"</span>
              <span class="semicolon">;</span>
            </div>
            <div class="code-line indent-1">
              <span class="keyword">private</span>
              <span class="type">String</span>
              <span class="variable">vision</span>
              <span class="operator">=</span>
              <span class="string">"共同成长"</span>
              <span class="semicolon">;</span>
            </div>
            <div class="code-line indent-1">
              <span class="keyword">public</span>
              <span class="type">void</span>
              <span class="method">join</span>
              <span class="bracket">(</span>
              <span class="type">Developer</span>
              <span class="variable">you</span>
              <span class="bracket">)</span>
              <span class="bracket">{</span>
            </div>
            <div class="code-line indent-2">
              <span class="variable">community</span>
              <span class="operator">.</span>
              <span class="method">welcome</span>
              <span class="bracket">(</span>
              <span class="variable">you</span>
              <span class="bracket">)</span>
              <span class="semicolon">;</span>
            </div>
            <div class="code-line indent-1">
              <span class="bracket">}</span>
            </div>
            <div class="code-line">
              <span class="bracket">}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="brand-text">
        <h1 class="brand-title">
          <span class="bracket">&lt;</span>
          <span class="brand-name">BitInn</span>
          <span class="bracket">/&gt;</span>
        </h1>
        <p class="brand-desc">开发者技术社区</p>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.auth-brand {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(53, 146, 196, 0.03) 0%, transparent 70%);
    animation: pulse 4s ease-in-out infinite;
  }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.1); opacity: 0.8; }
}

.logo-animation {
  position: relative;
  z-index: 1;
  cursor: pointer;
  transition: transform 0.3s;

  &:hover {
    transform: scale(1.02);
  }
}

// 代码编辑器样式
.code-editor {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(249, 115, 22, 0.3);
  width: 380px;
  margin-bottom: 32px;
  transition: all 0.3s;

  &:hover {
    box-shadow: 0 25px 80px rgba(249, 115, 22, 0.4), 0 0 0 1px rgba(249, 115, 22, 0.2);
  }
}

.editor-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
}

.window-controls {
  display: flex;
  gap: 8px;

  .dot {
    width: 12px;
    height: 12px;
    border-radius: 50%;

    &.red { background: #FF5F56; }
    &.yellow { background: #FFBD2E; }
    &.green { background: #27CA40; }
  }
}

.file-name {
  flex: 1;
  text-align: center;
  font-size: 13px;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
}

.editor-body {
  display: flex;
  padding: 16px;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  font-size: 14px;
  line-height: 1.8;
}

.line-numbers {
  display: flex;
  flex-direction: column;
  padding-right: 16px;
  margin-right: 16px;
  text-align: right;
  user-select: none;
}

.code-content {
  flex: 1;
}

.code-line {
  white-space: nowrap;

  &.indent-1 { padding-left: 24px; }
  &.indent-2 { padding-left: 48px; }
}

// 代码语法高亮
.keyword { color: #CC7832; }
.type { color: #BBB529; }
.class-name { color: #A9B7C6; font-weight: 600; }
.variable { color: #9876AA; }
.string { color: #6A8759; }
.comment { color: #808080; font-style: italic; }
.method { color: #FFC66D; }
.operator { color: #A9B7C6; }
.bracket { color: #E8E8E8; }
.semicolon { color: #CC7832; }

// 品牌文字
.brand-text {
  text-align: center;
}

.brand-title {
  font-size: 48px;
  font-weight: 700;
  margin-bottom: 12px;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;

  .bracket {
    color: #000000;
  }

  .brand-name {
    color: #000000;
    text-shadow: 0 0 20px rgba(53, 146, 196, 0.5);
  }
}

.brand-desc {
  font-size: 18px;
  letter-spacing: 2px;
}

// 响应式
@media (max-width: 900px) {
  .auth-brand {
    min-height: 300px;
    padding: 24px;
  }

  .code-editor {
    width: 100%;
    max-width: 340px;
    margin-bottom: 24px;
  }

  .brand-title {
    font-size: 36px;
  }
}
</style>
