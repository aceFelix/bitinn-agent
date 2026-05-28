# BitInn — AI 驱动的开发者社区后端

<p align="center">
  <strong>Spring Boot 3 + Spring AI + MongoDB + MySQL + Redis + Elasticsearch + RabbitMQ</strong>
</p>

---

## 📖 项目简介

BitInn（比特客栈）是一个面向开发者的技术社区平台，集成了 **AI 大模型智能对话**、**文章创作与发布**、**社交互动** 和 **全文搜索** 等核心功能。

AI 聊天是项目核心亮点，支持 **四种对话模式**（普通对话 / 图片识别 / 图片生成 / AI Agent 专业推理），通过 **SSE（Server-Sent Events）** 实现流式回答实时推送，并内置标题自动生成、对话摘要压缩、**多模型切换**（通义千问 3.6 / 3.7 / DeepSeek-V4-Pro）等高级功能。

---

## 🏗️ 技术架构

```
                     Vue 3 前端 (Vite)
                           │
              Axios HTTP  +  SSE EventSource
                           │
┌──────────────────────────┼──────────────────────────────┐
│            Spring Boot 3 (Java 17)                       │
│                                                          │
│  ┌──────────────────────────────────────────────────┐   │
│  │          Spring Security + JWT 无状态认证          │   │
│  │   JwtAuthenticationFilter → ThreadLocal → claims   │   │
│  ├──────────────────────────────────────────────────┤   │
│  │  Controller 层                                     │   │
│  │  Chat / Article / User / Interaction / OssSign      │   │
│  ├──────────────────────────────────────────────────┤   │
│  │  Service 层                                        │   │
│  │  ChatAsyncService (@Async) / ArticleService        │   │
│  │  InteractionService / TitleService / SummaryService │   │
│  │  ModelRouter / ImageGenService / FileProcessService │   │
│  ├──────────────────────────────────────────────────┤   │
│  │  中间件 & 跨存储同步                                │   │
│  │  RabbitMQ(DataSync) │ RateLimiter │ XssFilter      │   │
│  ├──────────────────────────────────────────────────┤   │
│  │  MySQL        MongoDB       Redis       ES    OSS  │   │
│  │  用户/文章     AI对话/消息   缓存/计数   搜索   存储  │   │
│  └──────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────┘
```

---

## 🚀 核心特性

### AI 智能对话

| 模式 | 说明 | 模型 |
|------|------|------|
| 普通模式 | 技术问答、编程帮助 | Qwen3.6 / Qwen3.7 / DeepSeek-V4 |
| 专业模式 | ReAct Agent 多步推理，支持工具调用 | DashScope + ReactAgent |
| 识图模式 | 上传图片让 AI 识别内容并回答 | Qwen3.6-Plus（多模态） |
| 生图模式 | 文生图、图片编辑 | Qwen-Image-2.0-Pro |

- **SSE 流式推送**：逐 token 实时推送，打字效果，TTFB < 2s
- **多模型路由**：ModelRouter 统一封装 DashScope 和 DeepSeek 两个后端
- **标题自动生成**：第 2/5 轮对话后取上下文通过 AI 生成标题
- **对话摘要压缩**：消息数 > 30 时压缩早期消息为摘要，节省 Token
- **文件处理**：Apache Tika 解析 PDF/Word/TXT，图片 Base64 编码

### 文章系统

- 基于 Vditor 的 Markdown 编辑器，支持所见即所得
- **Feed 流**：recommend（热度）/ latest（最新）/ hot（热门）三种排序
- **全文搜索**：Elasticsearch 8 + IK 中文分词，高亮搜索 + 搜索建议 + 热门关键词
- **热度分**：综合阅读量、点赞、收藏、评论、分享的定时计算 + 时间衰减

### 社交互动

- 点赞 / 收藏 / 关注 / 评论 / 转发，均带通知推送
- **高并发计数**：Redis Hash 存储，异步 RabbitMQ 回写 MySQL
- 批量查询优化，避免 N+1 问题

### 安全与并发

- **JWT 无状态认证**：Token 本地解析 + Redis 黑名单机制
- **BCrypt 密码加密**：自动加盐，不可逆
- **登录限制**：5 次失败锁定 30 分钟
- **注册防护**：分布式锁 + 幂等键 + UNIQUE 约束兜底
- **API 限流**：滑动窗口，全局 120 req/min/IP，登录 20 req/min/IP
- **XSS 过滤**：请求参数 HTML 转义
- **CORS 收紧**：显式白名单配置
- **文件上传校验**：10MB 限制 + MIME 白名单
- **@Async 线程池隔离**：显式配置 core/max/queue，拒绝策略 CallerRunsPolicy
- **SSE 连接管理**：ConcurrentHashMap + 全局上限 500 + 5 分钟超时

### 跨存储数据同步

- RabbitMQ 异步同步 MySQL ↔ Redis ↔ ES 三端数据
- 消费者并发 3~8 线程，失败重试 3 次
- 定时对账校验数据一致性（每 30 分钟 + 每日凌晨 3 点全量）

---

## 📦 技术栈

| 组件 | 版本 / 实现 | 用途 |
|------|------------|------|
| JDK | 17 | — |
| Spring Boot | 3.5.8 | 核心框架 |
| Spring AI | 1.1.2 | AI 调用抽象层 + ReAct Agent |
| spring-ai-alibaba | 1.1.2.3 | 阿里云百炼（DashScope）集成 |
| spring-ai-deepseek | 1.1.2 | DeepSeek 集成 |
| Spring Security | — | 认证鉴权 |
| MyBatis | 3.0.4 | MySQL ORM |
| MySQL | 9.7.0 | 业务数据（用户/文章/评论） |
| MongoDB | 8.2.7 | AI 对话数据（Conversation/Message） |
| Redis Stack | 7.2.0-v8 | 缓存 / 计数 / 分布式锁 / 向量存储 |
| Elasticsearch | 8.19.14 | 文章全文搜索 + IK 中文分词 |
| RabbitMQ | 4.1.4-management | 跨存储异步数据同步 |
| 阿里云 OSS | — | 图片/文件对象存储 |
| Apache Tika | — | 文档内容提取（PDF/Word） |
| Bucket4j | 8.17.0 | 令牌桶限流 |
| auth0 java-jwt | 4.4.0 | JWT 生成与解析 |
| PageHelper | 1.4.7 | MyBatis 分页 |
| SpringDoc OpenAPI | 2.8.16 | Swagger API 文档 |

---

## 📁 项目结构

```
bitinn-backend/
├── src/main/java/com/itniuma/bitinn/
│   ├── annotation/           # 自定义注解（@State 校验）
│   ├── config/               # 配置类
│   │   ├── SecurityConfig     # Spring Security + BCrypt
│   │   ├── AsyncConfig        # @Async 线程池配置
│   │   ├── CorsConfig         # 跨域白名单
│   │   ├── WebMvcConfig       # 拦截器注册（限流）
│   │   ├── ChatClientConfig   # Spring AI ChatClient Bean
│   │   ├── AgentConfig        # ReAct Agent 定义
│   │   ├── ElasticsearchConfig # ES 索引自动创建
│   │   ├── RabbitMQConfig     # 消息队列定义
│   │   └── SkillPromptConfig  # AI 技能提示词
│   ├── controller/            # HTTP 接口
│   │   ├── user/              # 用户注册/登录/信息
│   │   ├── article/           # 文章/分类/上传
│   │   ├── aichat/            # AI 聊天 + SSE 连接
│   │   ├── interaction/       # 点赞/收藏/关注/评论
│   │   ├── notification/      # 通知查询
│   │   ├── search/            # 全文搜索
│   │   └── oss/               # OSS 签名
│   ├── service/               # 业务逻辑
│   │   ├── ai/                # AI 模型路由/生图/文件处理/标题/摘要
│   │   ├── aichat/            # 聊天消息处理 + 异步推送
│   │   ├── article/           # 文章/分类/标签服务
│   │   ├── interaction/       # 互动操作 + 计数管理
│   │   ├── notification/      # 通知服务
│   │   ├── search/            # ES 数据同步/搜索服务
│   │   ├── cache/             # Feed 缓存服务
│   │   ├── mq/                # RabbitMQ 生产者/消费者
│   │   ├── oss/               # OSS 签名服务
│   │   └── user/              # 用户服务
│   ├── repository/            # MongoDB Repository
│   │   ├── ConversationRepository
│   │   ├── MessageRepository
│   │   ├── PromptTemplateRepository
│   │   └── ArticleSearchRepository (ES)
│   ├── mapper/                # MyBatis Mapper
│   ├── pojo/                  # 实体/DTO/VO
│   │   ├── entity/            # MySQL 实体
│   │   ├── mongo/             # MongoDB 文档
│   │   └── document/          # ES 文档
│   ├── filter/                # JWT 认证过滤器 / XSS 过滤器
│   ├── interceptor/           # 限流拦截器
│   ├── scheduler/             # 定时任务（热度分/计数回写/数据对账）
│   ├── listener/              # 连接池预热监听器
│   ├── tool/                  # AI Agent 工具（EmailTool）
│   ├── utils/                 # JWT / Redis / SSE / ThreadLocal
│   ├── validation/            # 自定义参数校验
│   └── enums/                 # 枚举（SSEMessageType / UserRole 等）
├── src/main/resources/
│   ├── application.yml        # 主配置
│   ├── application-local.yml  # 本地环境配置
│   ├── mapper/                # MyBatis XML
│   └── systemprompt/          # AI Agent 系统提示词
└── pom.xml
```

---

## ⚡ 快速开始

### 环境要求

| 组件 | 最低版本 |
|------|---------|
| JDK | 17+ |
| Maven | 3.9+ |
| MySQL | 9.5+ |
| MongoDB | 8.2.0+ |
| Redis Stack | 7.0+ |
| Elasticsearch | 8.19.0+ | |
| RabbitMQ | 4.1.0+ |



### 配置

复制 `src/main/resources/application-local.yml.example` 为 `application-local.yml`，填入本地环境配置：

```yaml
server:
  port: 8000

datasource:
  host: localhost
  port: 3306
  database: bitinn
  username: root
  password: your_password
  path: ?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true

redis:
  host: localhost
  port: 6379
  password: your_password

mongodb:
  host: localhost
  port: 27017
  database: bitinn_ai
  username: root
  password: your_password
  authentication-database: admin

rabbitmq:
  host: localhost
  port: 5972
  username: admin
  password: your_password

elasticsearch:
  uris: http://localhost:6200
  username: your_username
  password: your_password

website:
  domain: http://localhost:5173

aliyun-oss:
  access-key: your_oss_access_key
  secret-key: your_oss_secret_key
  end-point: oss-cn-hangzhou.aliyuncs.com
  bucket-name: your_bucket_name
  domain: https://your_bucket.oss-cn-hangzhou.aliyuncs.com/
  base-path: base/
```

> 敏感配置（API Key 等）建议通过环境变量注入：
> ```
> DASHSCOPE_API_KEY=sk-xxx
> DEEPSEEK_API_KEY=sk-xxx
> ```

### 启动

```powershell
# 编译验证
mvn compile -q

# 启动服务（端口 8000）
mvn spring-boot:run -DskipTests
```

启动成功标志：
```
所有连接池预热完成，服务已就绪！
AI聊天线程池初始化完成: core=4, max=16, queue=100
```

### API 文档

启动后访问：`http://localhost:8000/swagger-ui.html`

---

## 🔧 开发指引

### 目录约定

| 约定 | 说明 |
|------|------|
| `controller` | HTTP 接口，只做参数校验和调用 Service |
| `service/impl` | 业务逻辑实现，内部可调用其他 Service |
| `repository` | MongoDB / ES 数据访问（Spring Data） |
| `mapper` | MySQL 数据访问（MyBatis） |
| `pojo/entity` | MySQL 实体类 |
| `pojo/mongo` | MongoDB 文档类 |
| `pojo/document` | ES 索引文档类 |
| `config` | Spring 配置类 |
| `utils` | 通用工具类，不依赖业务 |

### 代码规范

- 所有类/接口顶部添加 Javadoc，包含 `@author aceFelix`
- 所有方法添加 Javadoc，描述参数、返回值及功能
- 核心逻辑添加行内注释说明意图
- 修改代码后执行 `mvn compile -q` 验证编译通过

### 扩展 AI Agent 工具

1. 创建工具类，方法用 `@Tool` + `@ToolParam` 注解
2. 在 `AgentConfig.agent()` 中通过 `.methodTools()` 注册
3. AI 会自动识别工具并调用

### 添加新技能

在 `SkillPromptConfig.SKILL_PROMPTS` Map 中新增一条即可：

```java
public static final Map<String, String> SKILL_PROMPTS = Map.of(
    "article", "你是一位资深技术写作者...",
    "new_skill", "你的新技能提示词..."
);
```

---

## 📄 License

本项目采用 **知识共享署名-非商业性使用-相同方式共享 4.0 国际许可协议（CC BY-NC-SA 4.0）** 进行许可。

- ✅ **个人学习、研究、教育**用途自由使用
- ✅ **开源社区**贡献和分享
- ❌ **未经授权的商业用途**禁止
- 📧 商业授权请联系：aceFelix

详见 [LICENSE](./LICENSE) 文件。
