<div align="center">
  <pre>
██████╗ ██╗████████╗██╗███╗   ██╗███╗   ██╗
██╔══██╗██║╚══██╔══╝██║████╗  ██║████╗  ██║
██████╔╝██║   ██║   ██║██╔██╗ ██║██╔██╗ ██║
██╔══██╗██║   ██║   ██║██║╚██╗██║██║╚██╗██║
██████╔╝██║   ██║   ██║██║ ╚████║██║ ╚████║
╚═════╝ ╚═╝   ╚═╝   ╚═╝╚═╝  ╚═══╝╚═╝  ╚═══╝
  </pre>
</div>

<h1 align="center">BitInn — AI 驱动的开发者社区</h1>

<p align="center">
  <strong>🚀 静态体验：<a href="https://bitinn-vue.pages.dev">bitinn-vue.pages.dev</a></strong>
</p>


<p align="center">
  <a href="https://openjdk.org/projects/jdk/"><img src="https://img.shields.io/badge/Java-17-orange?logo=openjdk" alt="Java 17"></a>
  <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/Spring_Boot-3.5.8-brightgreen?logo=springboot" alt="Spring Boot"></a>
  <a href="https://spring.io/projects/spring-ai"><img src="https://img.shields.io/badge/Spring_AI-1.1.2-6DB33F?logo=spring" alt="Spring AI"></a>
  <a href="https://www.java2ai.com/"><img src="https://img.shields.io/badge/Spring_AI_Alibaba-1.1.2-blue?logo=alibabacloud" alt="Spring AI Alibaba"></a>
  <a href="https://bailian.console.aliyun.com/cn-beijing/#/home"><img src="https://img.shields.io/badge/DashScope-Qwen-FF6A00?logo=alibabacloud" alt="DashScope"></a>
  <a href="https://www.deepseek.com/"><img src="https://img.shields.io/badge/DeepSeek-V4-536DFE?logo=deepseek" alt="DeepSeek"></a>
</p>

<p align="center">
  <a href="https://vuejs.org/"><img src="https://img.shields.io/badge/Vue-3.5-4FC08D?logo=vuedotjs" alt="Vue"></a>
  <a href="https://vitejs.cn/vite3-cn/"><img src="https://img.shields.io/badge/Vite-6-646CFF?logo=vite" alt="Vite"></a>
  <img src="https://img.shields.io/badge/License-MIT_NC-red" alt="License">
  <img src="https://img.shields.io/badge/Status-Alpha-orange" alt="Status">
  <img src="https://img.shields.io/badge/Version-v0.x-inactive" alt="Version">
</p>

---

## 📖 项目简介

**BitInn**（比特客栈）是一个面向开发者的技术社区平台，集成了 **AI 大模型智能对话**、**文章创作与发布**、**社交互动** 和 **全文搜索** 等核心功能。

项目采用前后端分离架构：

| 端 | 技术栈 | 目录 |
|------|------|------|
| **后端** | Java 17 + Spring Boot 3 + Spring AI + MongoDB + MySQL + Redis + ES + RabbitMQ | [bitinn-backend/](./bitinn-backend/) |
| **前端** | Vue 3 + Vite + Element Plus + Pinia + Vditor | [bitinn-frontend/](./bitinn-frontend/) |

---

> ⚠️ **版本说明**：项目目前处于 **Alpha 阶段（v0.x）**，核心架构已搭建完成，但部分功能模块和页面仍在开发完善中，后续将持续迭代更新。欢迎关注项目进展！

---

## 🚀 核心亮点

### 🤖 AI 智能对话

4 种对话模式 × 多模型切换 × 工具调用 × SSE 流式推送 

| 模式 | 说明 | 模型 |
|------|------|------|
| 普通模式 | 技术问答、编程帮助 | Qwen3.6-Plus / Qwen3.7-Max / DeepSeek-V4 |
| 专业模式 | ReAct Agent 多步推理 + 工具调用 | DashScope + ReactAgent |
| 识图模式 | 上传图片让 AI 识别内容 | Qwen3.6-Plus（多模态） |
| 生图模式 | 文生图、图片编辑 | Qwen-Image-2.0-Pro |

### 📝 文章系统

- Vditor Markdown 编辑器（所见即所得 + 分屏预览）
- Feed 流（recommend / latest / hot 三种排序 + 无限滚动）
- 全文搜索（Elasticsearch + IK 中文分词）

### 💬 社交互动

- 点赞 / 收藏 / 关注 / 评论 / 转发 + 消息通知
- 高并发计数：Redis Hash 异步回写 MySQL
- 用户个人主页

---

## 🏗️ 技术架构

```
                    bitinn-frontend (Vue 3)
                    https://bitinn-vue.pages.dev
                           │
              Axios HTTP  +  SSE EventSource
                           │
┌──────────────────────────┼──────────────────────────────┐
│            bitinn (Spring Boot 3)                        │
│                   http://localhost:8000                  │
│                                                          │
│   Controller → Service → ModelRouter / ChatAsyncService  │
│                          │                               │
│   ┌──────────────────────┼──────────────────────────┐   │
│   │   MySQL      MongoDB     Redis     ES    OSS     │   │
│   │   业务数据    AI对话      缓存/计数   搜索   存储   │   │
│   └──────────────────────────────────────────────────┘   │
│                          │                               │
│            RabbitMQ 跨存储异步数据同步                     │
└──────────────────────────────────────────────────────────┘
```

---

## 📂 项目结构

```
bitinn/
├── bitinn-backend/                    # 后端 Spring Boot 项目
│   ├── src/main/java/         #   源代码
│   ├── src/main/resources/    #   配置 / MyBatis XML / 系统提示词
│   ├── pom.xml                #   Maven 依赖
│   ├── README.md              #   后端文档
│   └── LICENSE                #   开源证书
├── bitinn-frontend/                # 前端 Vue 3 项目
│   ├── src/                   #   源代码
│   ├── package.json           #   依赖配置
│   ├── vite.config.js         #   Vite 配置
│   ├── README.md              #   前端文档
│   └── LICENSE                #   开源证书
└── README.md                  # 本文件
```

---

## ⚡ 快速开始

### 环境要求

| 组件 | 后端 | 前端 |
|------|------|------|
| 运行环境 | JDK 17+ / Maven 3.9+ | Node.js 22+ / Yarn 1.22+ |
| 数据库 | MySQL 9 + MongoDB 8 | — |
| 中间件 | Redis Stack 7 + ES 8 + RabbitMQ 4 | — |
| 云服务 | 阿里云 OSS + DashScope + DeepSeek | — |

### 启动后端

```powershell
cd bitinn-backend
cp src/main/resources/application-local.yml.example application-local.yml
# 编辑 application-local.yml 填入本地环境配置
mvn spring-boot:run -DskipTests
# 默认端口 8000，Swagger: http://localhost:8000/swagger-ui.html
```

### 启动前端

```bash
cd bitinn-frontend
yarn install
yarn dev
# 默认端口 5173
```

---

## 📋 子项目文档

| 文档 | 路径 |
|------|------|
| 后端 README | [bitinn-backend/README.md](./bitinn-backend/README.md) |
| 前端 README | [bitinn-frontend/README.md](./bitinn-frontend/README.md) |

---

## 📄 License

本项目（bitinn-backend + bitinn-frontend）均采用 **MIT-NC License** 进行许可。

- ✅ **个人学习、研究、教育**用途自由使用
- ✅ **开源社区**贡献和分享
- ❌ **未经授权的商业用途**禁止
- 📧 商业授权请联系：aceFelix
