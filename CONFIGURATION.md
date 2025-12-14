# RelaMind 配置说明

## 🔐 重要说明：镜像中不包含敏感信息

**关键点**：
- ✅ Docker 镜像只包含**代码和依赖**，不包含任何配置或敏感信息
- ✅ 所有配置（API Key、密码等）通过**环境变量在运行时注入**
- ✅ 用户可以完全自定义所有配置，无需修改镜像

## 📝 配置方式

### 方式一：使用 .env 文件（推荐）

1. **复制配置模板**
   ```bash
   cp .env.example .env
   ```

2. **编辑 .env 文件**
   ```bash
   # 必须配置
   DASHSCOPE_API_KEY=sk-your-actual-api-key-here
   
   # 可选：修改数据库密码（建议修改）
   POSTGRES_PASSWORD=your-strong-password-123
   CASSANDRA_PASSWORD=your-strong-password-123
   ```

3. **启动服务**
   ```bash
   docker-compose -f docker-compose.yml up -d
   ```

### 方式二：直接设置环境变量

```bash
export DASHSCOPE_API_KEY=sk-your-actual-api-key-here
export POSTGRES_PASSWORD=your-strong-password-123
docker-compose -f docker-compose.yml up -d
```

## 🔑 配置项说明

### 必填配置

| 环境变量 | 说明 | 示例 |
|---------|------|------|
| `DASHSCOPE_API_KEY` | 阿里云 DashScope API Key | `sk-xxxxxxxxxxxxx` |

### 可选配置（有默认值）

#### 数据库配置

| 环境变量 | 默认值 | 说明 |
|---------|--------|------|
| `POSTGRES_USER` | `relamind` | PostgreSQL 用户名 |
| `POSTGRES_PASSWORD` | `relamind123` | PostgreSQL 密码 ⚠️ **建议修改** |
| `POSTGRES_DB` | `relamind` | PostgreSQL 数据库名 |
| `CASSANDRA_USERNAME` | `cassandra` | Cassandra 用户名 |
| `CASSANDRA_PASSWORD` | `cassandra` | Cassandra 密码 ⚠️ **建议修改** |
| `CASSANDRA_CLUSTER_NAME` | `RelaMind` | Cassandra 集群名 |
| `CASSANDRA_DC` | `datacenter1` | Cassandra 数据中心名 |
| `CASSANDRA_RACK` | `rack1` | Cassandra 机架名 |

#### GitHub 镜像配置（使用预构建镜像时）

| 环境变量 | 默认值 | 说明 |
|---------|--------|------|
| `GITHUB_USER` | `your-username` | GitHub 用户名 |
| `GITHUB_REPO` | `RelaMind` | GitHub 仓库名 |

#### 其他配置

| 环境变量 | 说明 |
|---------|------|
| `SEARCH_API_KEY` | 搜索 API Key（如果使用搜索功能） |

## 🔄 修改配置后如何生效

### 修改数据库密码

```bash
# 1. 修改 .env 文件中的密码
POSTGRES_PASSWORD=new-password-123
CASSANDRA_PASSWORD=new-password-123

# 2. 停止服务
docker-compose -f docker-compose.yml down

# 3. 删除旧数据卷（⚠️ 会删除所有数据）
docker-compose -f docker-compose.yml down -v

# 4. 重新启动（会使用新密码创建数据库）
docker-compose -f docker-compose.yml up -d
```

### 修改 API Key

```bash
# 1. 修改 .env 文件
DASHSCOPE_API_KEY=new-api-key-here

# 2. 重启后端服务即可（无需删除数据）
docker-compose -f docker-compose.yml restart backend
```

## 🎯 默认值说明

### 为什么有默认值？

- **方便快速启动**：用户可以直接运行，无需立即配置所有项
- **开发测试**：适合快速测试和开发环境
- **生产环境**：**强烈建议修改所有默认密码**

### 默认值使用场景

| 场景 | 是否使用默认值 | 建议 |
|------|---------------|------|
| 本地开发测试 | ✅ 可以 | 使用默认值快速启动 |
| 生产环境 | ❌ 不建议 | **必须修改所有密码和敏感信息** |
| 共享服务器 | ❌ 不建议 | **必须修改所有密码** |

## 🔒 安全建议

1. **生产环境必须修改**：
   - ✅ `POSTGRES_PASSWORD`
   - ✅ `CASSANDRA_PASSWORD`
   - ✅ `DASHSCOPE_API_KEY`（使用自己的 Key）

2. **保护 .env 文件**：
   ```bash
   # 设置文件权限（仅所有者可读）
   chmod 600 .env
   ```

3. **不要提交 .env 到 Git**：
   - ✅ `.env` 已在 `.gitignore` 中
   - ✅ 只提交 `.env.example` 作为模板

## 📋 配置示例

### 最小配置（仅必填项）

```bash
# .env
DASHSCOPE_API_KEY=sk-your-actual-key
```

### 完整配置（生产环境推荐）

```bash
# .env
# AI API Key
DASHSCOPE_API_KEY=sk-your-actual-key
SEARCH_API_KEY=your-search-key

# 数据库配置（使用强密码）
POSTGRES_USER=relamind
POSTGRES_PASSWORD=StrongP@ssw0rd!2024
POSTGRES_DB=relamind

CASSANDRA_USERNAME=cassandra
CASSANDRA_PASSWORD=StrongP@ssw0rd!2024
CASSANDRA_CLUSTER_NAME=RelaMind

# GitHub 镜像配置
GITHUB_USER=your-username
GITHUB_REPO=RelaMind
```

## ❓ 常见问题

### Q: 镜像已经构建好了，配置不是已经固定了吗？

**A: 不是的！** Docker 镜像只包含代码，配置通过环境变量在**运行时注入**：

```
镜像内容：
├── 编译后的 JAR 文件
├── JRE 运行环境
└── 启动脚本

❌ 不包含：
├── application.yml（配置在运行时通过环境变量注入）
├── API Key
└── 数据库密码
```

### Q: 如何验证配置是否正确？

```bash
# 查看后端容器的环境变量
docker exec relamind-backend env | grep SPRING

# 查看日志确认配置
docker-compose -f docker-compose.yml logs backend | grep -i "api-key\|datasource"
```

### Q: 修改配置后需要重新构建镜像吗？

**A: 不需要！** 只需：
1. 修改 `.env` 文件
2. 重启服务：`docker-compose restart backend`

镜像本身不需要重新构建。

