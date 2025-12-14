# RelaMind 部署指南

## 📦 GitHub Packages vs Releases

### GitHub Packages（包管理）
- **用途**：发布可复用的依赖包（Maven、NPM、Docker镜像等）
- **你的项目**：适合发布 **Docker镜像** 到 GitHub Container Registry (ghcr.io)
- **访问方式**：`ghcr.io/用户名/仓库名/镜像名:标签`

### GitHub Releases（版本发布）
- **用途**：标记稳定版本，提供下载和发布说明
- **你的项目**：适合发布版本标签（如 v1.0.0），附带Docker镜像链接
- **访问方式**：GitHub仓库的 Releases 页面

## 🐳 Docker 镜像发布方式

### 方式一：使用预构建镜像（推荐）⭐

**优点**：
- ✅ 用户无需构建环境（JDK、Maven、Node.js）
- ✅ 快速启动，直接拉取使用
- ✅ 镜像经过CI/CD验证，更稳定
- ✅ 支持版本管理（latest、v1.0.0等）

**使用步骤**：

1. **登录 GitHub Container Registry**（首次使用需要）
   ```bash
   echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin
   # 或者使用个人访问令牌
   docker login ghcr.io -u USERNAME -p YOUR_TOKEN
   ```

2. **使用预构建镜像启动**
   ```bash
   # 设置镜像地址（替换为你的GitHub用户名和仓库名）
   export GITHUB_USER=your-username
   export GITHUB_REPO=relamind
   
   # 使用生产配置启动
   docker-compose -f docker-compose.prod.yml up -d
   ```

3. **或者直接修改 docker-compose.prod.yml 中的镜像地址**
   ```yaml
   backend:
     image: ghcr.io/your-username/relamind/backend:latest
   frontend:
     image: ghcr.io/your-username/relamind/frontend:latest
   ```

### 方式二：本地构建镜像

**优点**：
- ✅ 可以自定义构建参数
- ✅ 不依赖网络拉取镜像
- ✅ 适合开发调试

**使用步骤**：

```bash
# 使用开发配置（会本地构建镜像）
docker-compose up -d --build
```

## 🚀 自动构建和发布流程

### 1. GitHub Actions 自动构建

已配置 `.github/workflows/docker-build-publish.yml`，会在以下情况自动构建：

- ✅ 推送到 `main`/`master` 分支 → 构建并发布 `latest` 标签
- ✅ 推送版本标签（如 `v1.0.0`）→ 构建并发布版本标签
- ✅ 创建 Pull Request → 仅构建，不发布（用于测试）
- ✅ 手动触发 → 在 Actions 页面手动运行

### 2. 发布版本标签

```bash
# 1. 创建并推送版本标签
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0

# 2. GitHub Actions 会自动：
#    - 构建 Docker 镜像
#    - 发布到 ghcr.io
#    - 创建 GitHub Release（如果配置了）
```

### 3. 查看发布的镜像

1. 访问：`https://github.com/你的用户名/relamind/pkgs/container/backend`
2. 或使用 Docker 命令：
   ```bash
   docker pull ghcr.io/your-username/relamind/backend:latest
   docker pull ghcr.io/your-username/relamind/frontend:latest
   ```

## 📋 镜像标签说明

GitHub Actions 会自动创建以下标签：

- `latest` - 主分支的最新版本
- `main` - 主分支构建
- `v1.0.0` - 版本标签
- `v1.0` - 主版本号
- `main-abc1234` - 提交SHA前缀

## 🔐 权限设置

### 公开仓库
- 镜像默认公开，任何人都可以拉取
- 无需登录即可使用

### 私有仓库
- 需要登录 GitHub Container Registry
- 需要个人访问令牌（Personal Access Token）

## 📝 使用示例

### 快速开始（使用预构建镜像）

```bash
# 1. 克隆仓库
git clone https://github.com/your-username/relamind.git
cd relamind

# 2. 配置环境变量
cat > .env << EOF
DASHSCOPE_API_KEY=your-api-key-here
POSTGRES_PASSWORD=your-strong-password
CASSANDRA_PASSWORD=your-strong-password
GITHUB_USER=your-username
GITHUB_REPO=relamind
EOF

# 3. 启动服务（使用预构建镜像）
docker-compose -f docker-compose.prod.yml up -d

# 4. 查看日志
docker-compose -f docker-compose.prod.yml logs -f
```

### 本地开发（本地构建）

```bash
# 使用开发配置，会本地构建镜像
docker-compose up -d --build
```

## 🎯 推荐方案

**对于你的项目，推荐：**

1. ✅ **使用 GitHub Actions 自动构建和发布 Docker 镜像**
2. ✅ **用户使用预构建镜像**（docker-compose.prod.yml）
3. ✅ **发布版本时创建 GitHub Release**，附带镜像链接

这样用户只需要：
- 克隆代码
- 配置环境变量
- 运行 `docker-compose -f docker-compose.prod.yml up -d`

无需安装 JDK、Maven、Node.js 等构建工具！

