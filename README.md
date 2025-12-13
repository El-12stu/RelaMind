# RelaMind - AI 个人成长伙伴

> 记录成长，理解自己，成为更好的你

RelaMind 是一个基于 AI 的个人成长轨迹分析系统，帮助用户通过记录生活、回顾历史、分析模式来更好地理解自己，实现持续成长。

## ✨ 产品定位

RelaMind 定位为"你的 AI 成长伙伴"，通过以下方式帮助用户：

- **日常陪伴**：倾听用户心声，给予情感支持和理解
- **引导记录**：鼓励用户记录生活点滴，养成记录习惯
- **成长分析**：基于历史记录分析成长模式，提供个性化洞察
- **主动关怀**：记住用户的重要信息，适时主动关心

## 🎯 核心功能

### 1. 智能对话系统

- **日常陪伴聊天**：多轮对话、情感支持、引导记录
- **智能意图识别**：基于 LLM 自动判断用户意图，智能路由到最合适的处理模式
- **对话记忆持久化**：基于 Cassandra 存储对话历史，实现长期记忆

### 2. 历史记录检索（RAG）

- **向量化存储**：使用 PgVector 向量数据库存储用户的日记、笔记等历史记录
- **语义检索**：基于 RAG 技术，从历史记录中检索相关信息
- **时间线查询**：回答"3年前的今天我在做什么？"、"我去年这个时候在做什么？"等问题
- **模式分析**：分析用户的成长模式，识别行为规律
- **个性化建议**：基于历史数据给出针对性建议

### 3. 智能意图识别

- **基于 LLM 的分类**：使用结构化输出进行意图分类，提高准确率
- **置信度机制**：当识别置信度较低时，主动询问用户确认
- **准确率优化**：记录识别准确率，持续优化模型

### 4. 工具调用能力

- **联网搜索**：实时搜索最新信息
- **文件操作**：保存、读取用户文件
- **网页抓取**：提取网页内容
- **资源下载**：下载图片、文档等资源
- **PDF 生成**：生成个性化报告
- **终端操作**：执行代码脚本

### 5. MCP 服务集成

- **地图服务**：查找附近地点、规划路线
- **图片搜索**：从特定网站搜索图片
- **扩展能力**：支持自定义 MCP 服务

### 6. 自主规划智能体

基于 ReAct 模式的智能体，可以根据用户需求自主推理和行动，完成复杂任务。

## 🛠️ 技术栈

### 后端技术

- **Java 21** + **Spring Boot 3**
- **Spring AI**：AI 应用开发框架
- **LangChain4j**：AI 应用开发工具库
- **Cassandra**：对话记忆持久化存储
- **PgVector**：向量数据库，用于 RAG 检索
- **Spring AI ChatClient**：多轮对话、Advisor、ChatMemory
- **Spring AI RAG**：知识库检索增强生成
- **Spring AI Tool Calling**：工具调用能力
- **Spring AI MCP**：模型上下文协议支持

### AI 能力

- **大模型接入**：支持多种 AI 大模型（通义千问、GPT 等）
- **Prompt 工程**：优化的提示词设计
- **结构化输出**：使用 Entity 进行结构化数据提取
- **流式响应**：SSE 流式传输，提升用户体验
- **多模态支持**：支持文本、图片等多种输入

### 工具与库

- **Kryo**：高性能序列化
- **Jsoup**：网页抓取
- **iText**：PDF 生成
- **Knife4j**：API 文档生成

### 前端技术

- **Vue 3**：前端框架
- **Vite**：构建工具
- **SSE**：服务器推送事件，实现流式响应

### 部署

- **Docker**：容器化部署
- **Serverless**：支持 Serverless 部署

##  项目结构

```
RelaMind/
├── src/main/java/io/el12stu/RelaMind/
│   ├── app/              # 核心应用逻辑
│   │   └── RelaMindApp.java
│   ├── agent/            # AI 智能体
│   │   ├── Manus.java    # 自主规划智能体
│   │   └── ReActAgent.java
│   ├── advisor/          # Spring AI Advisor
│   │   ├── SensitiveWordAdvisor.java
│   │   └── ...
│   ├── chatmemory/       # 对话记忆
│   │   └── CassandraBasedChatMemory.java
│   ├── rag/              # RAG 相关
│   │   ├── RelaMindAppDocumentLoader.java
│   │   └── QueryRewriter.java
│   ├── service/          # 业务服务
│   │   ├── IntentDetectionService.java
│   │   └── SensitiveWordService.java
│   ├── tools/            # 工具定义
│   │   ├── WebSearchTool.java
│   │   ├── FileOperationTool.java
│   │   └── ...
│   └── controller/       # API 接口
│       └── AiController.java
├── src/main/resources/
│   ├── application.yml    # 配置文件
│   └── document/         # 知识库文档
└── ai-agent-frontend/ # 前端项目
    └── src/
        └── views/
            └── LoveMaster.vue
```

## 🚀 快速开始

### 环境要求

- JDK 21+
- Maven 3.6+
- PostgreSQL（支持 PgVector）
- Cassandra（可选，用于对话记忆）

### 配置说明

1. 配置 AI 大模型 API Key（在 `application.yml` 中）
2. 配置数据库连接（PostgreSQL、Cassandra）
3. 配置向量数据库（PgVector）

### 运行项目

```bash
# 后端
mvn spring-boot:run

# 前端
cd ai-agent-frontend
npm install
npm run dev
```

## 📝 使用示例

### 普通聊天

```
用户：今天心情不错
AI：太好了！能分享一下是什么让你心情这么好吗？要不要记录下来，这样以后可以回顾一下这些美好的时刻。
```

### 历史记录查询（RAG）

```
用户：我去年这个时候在做什么？
AI：根据你2023年3月的记录，当时你正在准备一个重要的项目，还记录了一些关于工作压力的思考...
```

### 工具调用

```
用户：帮我搜索一下今天的天气
AI：[调用搜索工具] 根据搜索结果，今天天气晴朗，温度...
```

## 🎨 产品特色

1. **智能路由**：自动识别用户意图，选择最合适的处理方式
2. **长期记忆**：基于向量数据库实现长期记忆，真正理解用户
3. **个性化**：基于用户历史数据提供个性化建议
4. **可扩展**：支持自定义工具和 MCP 服务
5. **持续优化**：记录准确率，持续优化意图识别

## 📄 许可证

本项目采用 [MIT License](LICENSE) 许可证。


---

**RelaMind** - 记录成长，理解自己，成为更好的你 ✨

