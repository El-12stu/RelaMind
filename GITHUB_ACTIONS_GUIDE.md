# GitHub Actions 操作指南

## 📋 目录

1. [自动构建和发布 Docker 镜像](#自动构建和发布-docker-镜像)
2. [创建 GitHub Release](#创建-github-release)
3. [完整操作流程](#完整操作流程)

---

## 🐳 自动构建和发布 Docker 镜像

### 前置准备

1. **确保 GitHub Actions 已启用**
   - 进入仓库 Settings → Actions → General
   - 确保 "Allow all actions and reusable workflows" 已启用

2. **检查工作流文件**
   - 确保 `.github/workflows/docker-build-publish.yml` 已提交到仓库

### 自动触发构建

GitHub Actions 会在以下情况自动构建和发布镜像：

#### 1. 推送到主分支

```bash
# 推送到 main 或 master 分支
git push origin main

# GitHub Actions 会自动：
# - 构建后端和前端镜像
# - 发布到 ghcr.io/你的用户名/RelaMind/backend:latest
# - 发布到 ghcr.io/你的用户名/RelaMind/frontend:latest
```

#### 2. 推送版本标签

```bash
# 创建版本标签
git tag -a v1.0.0 -m "Release version 1.0.0"

# 推送标签
git push origin v1.0.0

# GitHub Actions 会自动：
# - 构建镜像
# - 发布多个标签：v1.0.0, v1.0, latest
```

#### 3. 手动触发

1. 进入 GitHub 仓库
2. 点击 **Actions** 标签
3. 选择 **Build and Publish Docker Images** 工作流
4. 点击 **Run workflow** 按钮
5. 选择分支，点击 **Run workflow**

### 查看构建状态

1. 进入仓库的 **Actions** 页面
2. 查看最新的工作流运行状态
3. 点击运行记录查看详细日志

### 查看发布的镜像

1. 进入仓库主页
2. 点击右侧 **Packages**（或访问 `https://github.com/你的用户名/RelaMind/pkgs`）
3. 可以看到两个包：
   - `backend` - 后端镜像
   - `frontend` - 前端镜像

### 镜像地址格式

发布后的镜像地址：
- 后端：`ghcr.io/你的用户名/RelaMind/backend:latest`
- 前端：`ghcr.io/你的用户名/RelaMind/frontend:latest`

### 镜像标签说明

| 触发方式 | 生成的标签 |
|---------|-----------|
| 推送到 main 分支 | `latest`, `main` |
| 推送标签 v1.0.0 | `v1.0.0`, `v1.0`, `latest` |
| Pull Request | `pr-123`（仅构建，不发布） |

---

## 🏷️ 创建 GitHub Release

### 方式一：通过 GitHub 网页创建（推荐）

1. **进入 Releases 页面**
   - 访问：`https://github.com/你的用户名/RelaMind/releases`
   - 或点击仓库右侧的 **Releases** → **Create a new release**

2. **填写 Release 信息**
   - **Tag version**：选择或创建新标签（如 `v1.0.0`）
   - **Release title**：发布标题（如 `RelaMind v1.0.0`）
   - **Description**：发布说明（可以使用 Markdown）

3. **添加发布说明模板**：
   ```markdown
   ## 🎉 RelaMind v1.0.0

   ### ✨ 新功能
   - 功能1
   - 功能2

   ### 🐛 修复
   - 修复1
   - 修复2

   ### 📦 Docker 镜像
   - 后端：`ghcr.io/你的用户名/RelaMind/backend:v1.0.0`
   - 前端：`ghcr.io/你的用户名/RelaMind/frontend:v1.0.0`

   ### 🚀 快速开始
   ```bash
   git clone https://github.com/你的用户名/RelaMind.git
   cd RelaMind
   cp env.example .env
   # 编辑 .env 文件，配置 API Key
   docker-compose -f docker-compose.prod.yml up -d
   ```

   ### 📚 文档
   - [部署指南](DEPLOYMENT.md)
   - [配置说明](CONFIGURATION.md)
   ```

4. **附加文件**（可选）
   - 可以附加源码 ZIP、JAR 文件等

5. **发布**
   - 点击 **Publish release** 按钮

### 方式二：通过 Git 命令创建

```bash
# 1. 创建并推送标签
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0

# 2. 在 GitHub 网页上创建 Release
#    访问 https://github.com/你的用户名/RelaMind/releases/new
#    选择刚才创建的标签 v1.0.0
#    填写发布说明
#    点击 Publish release
```

### 方式三：使用 GitHub CLI（gh）

```bash
# 安装 GitHub CLI（如果未安装）
# macOS: brew install gh
# Windows: winget install GitHub.cli

# 登录
gh auth login

# 创建 Release
gh release create v1.0.0 \
  --title "RelaMind v1.0.0" \
  --notes "## 🎉 RelaMind v1.0.0

### ✨ 新功能
- 功能1
- 功能2

### 📦 Docker 镜像
- 后端：\`ghcr.io/你的用户名/RelaMind/backend:v1.0.0\`
- 前端：\`ghcr.io/你的用户名/RelaMind/frontend:v1.0.0\`"
```

---

## 🎯 完整操作流程

### 第一次发布

#### 步骤 1：准备代码

```bash
# 1. 确保所有更改已提交
git add .
git commit -m "准备发布 v1.0.0"
git push origin main
```

#### 步骤 2：等待镜像构建完成

1. 进入 **Actions** 页面
2. 等待 "Build and Publish Docker Images" 工作流完成
3. 确认镜像已成功发布到 Packages

#### 步骤 3：创建版本标签

```bash
# 创建标签
git tag -a v1.0.0 -m "Release version 1.0.0"

# 推送标签（这会触发构建带版本标签的镜像）
git push origin v1.0.0
```

#### 步骤 4：创建 GitHub Release

1. 访问 `https://github.com/你的用户名/RelaMind/releases/new`
2. 选择标签 `v1.0.0`
3. 填写发布说明
4. 点击 **Publish release**

### 后续版本发布

```bash
# 1. 更新代码并提交
git add .
git commit -m "更新内容"
git push origin main

# 2. 创建新版本标签
git tag -a v1.1.0 -m "Release version 1.1.0"
git push origin v1.1.0

# 3. 在 GitHub 网页创建 Release
#    访问 https://github.com/你的用户名/RelaMind/releases/new
```

---

## 🔍 验证发布

### 验证 Docker 镜像

```bash
# 拉取镜像（替换为你的用户名）
docker pull ghcr.io/你的用户名/RelaMind/backend:latest
docker pull ghcr.io/你的用户名/RelaMind/frontend:latest

# 查看镜像
docker images | grep relamind
```

### 验证 GitHub Release

1. 访问 `https://github.com/你的用户名/RelaMind/releases`
2. 确认 Release 已创建
3. 检查发布说明和标签

---

## ❓ 常见问题

### Q: 镜像构建失败怎么办？

**A: 检查以下几点：**
1. 查看 Actions 日志，找到错误信息
2. 检查 Dockerfile 是否正确
3. 检查是否有语法错误
4. 确保所有依赖文件都已提交

### Q: 如何修改镜像地址？

**A: 修改 `.github/workflows/docker-build-publish.yml`：**
```yaml
env:
  BACKEND_IMAGE_NAME: ${{ github.repository }}/backend
  FRONTEND_IMAGE_NAME: ${{ github.repository }}/frontend
```

### Q: 如何让镜像公开？

**A: 默认情况下：**
- 公开仓库的镜像自动公开
- 私有仓库的镜像需要手动设置

**设置方法：**
1. 进入 Packages 页面
2. 点击镜像包
3. 点击 **Package settings**
4. 在 **Danger Zone** 中点击 **Change visibility** → **Make public**

### Q: 如何删除旧版本镜像？

**A: 在 Packages 页面：**
1. 进入镜像包页面
2. 点击版本号
3. 点击 **Delete version**

---

## 📝 发布检查清单

发布前请确认：

- [ ] 代码已提交并推送到 GitHub
- [ ] 所有测试通过
- [ ] README.md 已更新
- [ ] 版本号已更新（如需要）
- [ ] `.github/workflows/docker-build-publish.yml` 已提交
- [ ] `docker-compose.prod.yml` 中的镜像地址已更新
- [ ] `env.example` 文件已更新
- [ ] 发布说明已准备好

---

## 🎉 完成！

发布成功后，用户可以：

1. 从 GitHub 克隆仓库
2. 使用预构建镜像快速启动
3. 查看 Release 页面了解版本信息

享受自动化的便利吧！🚀

