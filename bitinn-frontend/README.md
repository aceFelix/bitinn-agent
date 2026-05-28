# BitInn — AI 驱动的开发者社区前端

<p align="center">
  <strong>Vue 3 + Vite + Element Plus + Pinia + Vditor</strong>
</p>

---

## 📖 项目简介

BitInn（比特客栈）前端是一个面向开发者的技术社区客户端，提供 **AI 智能对话**、**文章创作与浏览**、**社交互动**、**全文搜索** 等核心功能。

前端对接后端 Spring Boot 服务，AI 聊天通过 **SSE（Server-Sent Events）** 实现流式回答的逐字打字效果，支持四种对话模式的切换和附件上传。

---

## 🏗️ 功能架构

```
┌──────────────────────────────────────────────────────────┐
│                      Vue 3 应用                           │
│                                                          │
│  ┌─────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐ │
│  │  AIChat │  │ Article  │  │   Login  │  │  Profile │ │
│  │  3栏布局  │  │ Editor   │  │  AuthForm │  │ 3栏布局  │ │
│  └────┬────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘ │
│       │            │              │              │       │
│  ┌────┴────────────┴──────────────┴──────────────┴────┐  │
│  │               Utils & API Layer                     │  │
│  │  apiClient.js（去重/取消/缓存）  │  ossSign.js      │  │
│  │  request.js（Axios 拦截器）      │  api/*.js        │  │
│  └──────────────────────┬──────────────────────────────┘  │
│                         │                                 │
│  ┌──────────────────────┴──────────────────────────────┐  │
│  │               Pinia Stores                           │  │
│  │  token.js（JWT 持久化）  │  userInfo.js（用户信息）  │  │
│  └─────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────┘
                           │
                Axios HTTP  +  SSE EventSource
                           │
                Spring Boot 后端 (8000)
```

---

## 🚀 核心特性

### AI 智能聊天

| 功能 | 说明 |
|------|------|
| 三栏布局 | 左：会话列表 | 中：聊天面板 | 右：技能引导 |
| 四种模式 | 普通对话 / 图片识别（上传图片） / 图片生成 / AI Agent 专业推理 |
| SSE 流式推送 | EventSource 长连接，逐 token 实时渲染，打字效果 |
| 代码高亮 | highlight.js，支持 10+ 语言，可切换 GitHub Dark / Monokai 等主题 |
| 技能引导 | 点击技能卡片 → AiSkillGuide 弹窗配置参数 → 注入专用 System Prompt |
| 附件上传 | 支持图片（识图/生图编辑）和文档（PDF/Word/TXT/Markdown） |

### 文章系统

| 功能 | 说明 |
|------|------|
| Markdown 编辑器 | 基于 Vditor，所见即所得 + 分屏预览 + 大纲导航 |
| 创作入口 | 选择文章类型（技术文章/学习笔记/项目分享等）→ 标签 → 开始写作 |
| 发布弹窗 | 设置标题、分类、标签、封面图、可见性 |
| Feed 流 | 首页三栏：左侧导航 + 中间文章流（推荐/最新/热门） + 右侧边栏 |
| 文章详情 | 三栏：左侧目录/互动数据 + 中间正文/评论 + 右侧作者信息 |
| 无限滚动 | 基于 IntersectionObserver 的哨兵元素自动加载更多 |
| 骨架屏 | 加载中显示带渐变动画的骨架卡片 |

### 社交互动

- 点赞 / 收藏 / 关注 / 评论 / 分享
- 实时消息通知（消息弹窗 → 消息面板，支持分类 Tab）
- 用户个人主页（左侧个人信息 + 中间内容 + 右侧统计）

### 搜索

- 全文搜索（Elasticsearch），支持关键词高亮
- 搜索建议（前缀补全）
- 热门搜索关键词

### 性能优化

| 机制 | 实现 | 路径 |
|------|------|------|
| 图片懒加载 | IntersectionObserver 自定义指令 `v-lazy-load` | [lazyLoad.js](file:///e:/2.MyProjects/MyAgentChat/bitinn-dev/bitinn-vue/src/directives/lazyLoad.js) |
| 请求去重 | 5 秒内相同请求复用结果 | [apiClient.js](file:///e:/2.MyProjects/MyAgentChat/bitinn-dev/bitinn-vue/src/utils/apiClient.js) |
| 请求取消 | 路由跳转 / Tab 切换时 AbortController 取消未完成请求 | [apiClient.js](file:///e:/2.MyProjects/MyAgentChat/bitinn-dev/bitinn-vue/src/utils/apiClient.js) |
| 请求缓存 | 分类列表 10 分钟 / Feed 2 分钟内存缓存 | [apiClient.js](file:///e:/2.MyProjects/MyAgentChat/bitinn-dev/bitinn-vue/src/utils/apiClient.js) |
| OSS 签名缓存 | 签名 URL 55 分钟内存缓存，减少请求 | [ossSign.js](file:///e:/2.MyProjects/MyAgentChat/bitinn-dev/bitinn-vue/src/utils/ossSign.js) |
| 路由懒加载 | `() => import()` 按需加载页面组件 | [router/index.js](file:///e:/2.MyProjects/MyAgentChat/bitinn-dev/bitinn-vue/src/router/index.js) |
| 组件按需导入 | Element Plus 组件手动 import，减少包体积 | [main.js](file:///e:/2.MyProjects/MyAgentChat/bitinn-dev/bitinn-vue/src/main.js) |

---

## 📦 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.5.x | 渐进式框架（Composition API） |
| Vite | 6.x | 构建工具（HMR 热更新） |
| Vue Router | 4.x | SPA 路由 + 懒加载 + 路由守卫 |
| Pinia | 3.x | 状态管理（token / userInfo 持久化） |
| Element Plus | 2.x | UI 组件库（按需导入） |
| Axios | 1.x | HTTP 请求 + 拦截器 |
| Vditor | 3.x | Markdown 编辑器（所见即所得） |
| highlight.js | 11.x | 代码语法高亮 |
| Sass | 1.x | CSS 预处理器 |
| unplugin-auto-import | 0.18.x | 自动导入 Vue API |
| unplugin-vue-components | 0.27.x | Element Plus 按需导入 |

---

## 📁 项目结构

```
bitinn-forntend/
├── src/
│   ├── api/                          # API 接口封装
│   │   ├── article.js                #   文章/分类/搜索
│   │   ├── chat.js                   #   AI 聊天/会话/SSE 连接
│   │   ├── upload.js                 #   文件上传
│   │   └── user.js                   #   用户注册/登录/信息
│   ├── assets/                       # 静态资源（头像/封面等）
│   ├── components/
│   │   ├── ai-chat/                  # AI 聊天组件
│   │   │   ├── AiChatLeft.vue        #   左侧会话列表
│   │   │   ├── AiChatMain.vue        #   中间聊天面板（核心）
│   │   │   ├── AiChatRight.vue       #   右侧技能列表
│   │   │   ├── AiSkillGuide.vue      #   技能配置弹窗
│   │   │   └── CodeThemeSelector.vue #   代码主题选择器
│   │   ├── article/                  # 文章组件
│   │   │   ├── ArticleDetailContent.vue   # 正文+评论
│   │   │   ├── ArticleDetailRight.vue     # 作者信息+推荐
│   │   │   ├── ArticleDetailSidebar.vue   # 目录+互动数据
│   │   │   └── PublishDialog.vue          # 发布弹窗
│   │   ├── common/                   # 通用组件
│   │   │   ├── ArticleCard.vue       #   文章卡片
│   │   │   ├── ArticleCardSkeleton.vue # 骨架屏
│   │   │   └── UserCard.vue          #   用户弹出卡片
│   │   ├── create/                   # 创作入口组件
│   │   │   ├── ArticleTypeSelector.vue # 文章类型选择
│   │   │   ├── TagSelector.vue       #   标签选择
│   │   │   └── DraftBox.vue          #   草稿箱
│   │   ├── home/                     # 首页组件
│   │   │   ├── ArticleFeed.vue       #   文章 Feed 流
│   │   │   ├── LeftSidebar.vue       #   左侧导航
│   │   │   ├── RightSidebar.vue      #   右侧边栏
│   │   │   ├── leftoption/           #   我的文章/点赞/收藏/关注
│   │   │   ├── message/              #   消息面板/弹窗
│   │   │   ├── search/               #   搜索结果页
│   │   │   └── set/                  #   设置面板
│   │   ├── login/                    # 登录组件
│   │   │   ├── AuthCard.vue          #   卡片容器
│   │   │   ├── AuthForm.vue          #   登录/注册/忘记密码表单
│   │   │   └── LoginBrand.vue        #   品牌展示区
│   │   └── user/                     # 用户主页组件
│   │       ├── UserProfileLeft.vue   #   左侧个人信息
│   │       ├── UserProfileMain.vue   #   中间内容区
│   │       └── UserProfileRight.vue  #   右侧统计
│   ├── directives/
│   │   └── lazyLoad.js               # 图片懒加载指令
│   ├── router/
│   │   └── index.js                  # 路由配置 + 路由守卫
│   ├── stores/
│   │   ├── token.js                  # Token 状态（持久化）
│   │   └── userInfo.js              # 用户信息状态（持久化）
│   ├── styles/
│   │   └── colors.css                # 全局主题色
│   ├── utils/
│   │   ├── apiClient.js              # 增强 API 客户端（去重/取消/缓存）
│   │   ├── ossSign.js                # OSS 签名 URL 工具
│   │   └── request.js                # Axios 实例（拦截器）
│   ├── views/
│   │   ├── AiChat.vue                # AI 聊天页
│   │   ├── Home.vue                  # 首页
│   │   ├── article/                  # 文章页面
│   │   │   ├── ArticleDetail.vue     #   文章详情
│   │   │   ├── ArticleEdit.vue       #   文章编辑
│   │   │   └── StartCreate.vue       #   创作入口
│   │   ├── login/
│   │   │   └── Login.vue             # 登录/注册页
│   │   └── user/
│   │       ├── UserAvatar.vue        #   修改头像
│   │       ├── UserInfo.vue          #   修改信息
│   │       ├── UserProfile.vue       #   个人主页
│   │       └── UserResetPassword.vue #   修改密码
│   ├── App.vue                       # 根组件（router-view）
│   └── main.js                       # 应用入口
├── index.html                        # HTML 模板
├── vite.config.js                    # Vite 配置
└── package.json                      # 项目依赖
```

---

## ⚡ 快速开始

### 环境要求

| 组件 | 最低版本 |
|------|---------|
| Node.js | 18.x+ |
| npm | 9.x+ |
| yarn | 1.22.x+ |

### 安装依赖

```bash
yarn install
```

### 开发模式

```bash
yarn dev
```

默认启动 `http://localhost:5173`，确保后端服务已运行在 `http://localhost:8000`。

### 生产构建

```bash
yarn build
```

构建产物输出到 `dist/` 目录。

### 编译验证（CI / 代码修改后）

```bash
npx vite build --mode development
```

---

## 🔧 开发指引

### 目录约定

| 目录 | 职责 |
|------|------|
| `views/` | 页面级组件，对应路由，负责组装子组件 |
| `components/` | 可复用组件，按功能模块分子目录 |
| `api/` | 后端接口封装，一个模块一个文件 |
| `stores/` | Pinia 状态管理，需要持久化的数据（token、userInfo） |
| `utils/` | 纯工具函数，不依赖 Vue（request、apiClient、ossSign） |
| `directives/` | 自定义指令（懒加载） |
| `router/` | 路由配置 + 全局守卫 |

### 代码规范

- 组件顶部添加 HTML 注释，说明组件功能和职责
- 函数使用 JSDoc 风格注释，描述参数和返回值
- 复杂逻辑添加行内注释
- 修改代码后执行 `npx vite build --mode development` 验证编译通过

### SSE 流式聊天原理

```
前端 EventSource("/api/sse/connect?userId=&token=")
  │
  ├─ ADD 事件     → 逐 chunk 追加到 Markdown 渲染区
  ├─ FINISH 事件  → 标记对话完成
  ├─ TITLE_UPDATE → 更新会话列表标题
  ├─ SUMMARY_UPDATE → 更新会话摘要
  └─ ERROR 事件   → 显示错误提示
```

---

## 📄 License

本项目采用 **MIT-NC License** 进行许可。

- ✅ **个人学习、研究、教育**用途自由使用
- ✅ **开源社区**贡献和分享
- ❌ **未经授权的商业用途**禁止
- 📧 商业授权请联系：aceFelix

详见 [LICENSE](./LICENSE) 文件。
