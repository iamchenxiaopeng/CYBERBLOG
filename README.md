<div align="center">

# 🌐 CYBERBLOG — 赛博朋克个人博客

**一个基于 Vue3 + Spring Boot 3 构建的全栈赛博朋克风格博客系统**

[![Vue3](https://img.shields.io/badge/Vue-3.4-42b883)](https://vuejs.org)
[![SpringBoot](https://img.shields.io/badge/Spring_Boot-3.2-6db33f)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)](https://mysql.com)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)](https://docs.docker.com/compose)

</div>

---

## 🚀 一键启动

> **唯一前置条件：安装并启动 [Docker Desktop](https://www.docker.com/products/docker-desktop)**

```bash
# Windows
start.bat

# Linux / macOS
bash start.sh
```

首次运行会自动拉取镜像、编译代码（约 3-5 分钟），之后启动只需 30 秒。

**启动后访问：**

| 地址 | 说明 |
|------|------|
| **http://localhost** | 博客前端 |
| http://localhost:8080/doc.html | 后端 Swagger 文档 |
| http://localhost:9001 | MinIO 控制台（minioadmin/minioadmin123） |

---

## 🛠️ 常用命令

```bash
# 停止所有服务
start.bat stop          # Windows
bash start.sh stop      # Linux/Mac

# 重建并重启（代码有改动后用）
start.bat restart
bash start.sh restart

# 查看实时日志
start.bat logs
bash start.sh logs

# 本地开发模式（只启动 MySQL/Redis/MinIO，手动跑前后端）
start.bat dev
bash start.sh dev
```

---

## ✨ 功能特性

| 功能 | 说明 |
|------|------|
| 🔐 **用户注册/登录** | 一键注册即登录，JWT 无状态认证 |
| 📝 **Markdown 渲染** | 上传 .md 文件或在线手写，代码高亮 |
| 💗 **点赞系统** | 登录/游客均可点赞，防重复切换 |
| 💬 **评论系统** | 支持二级嵌套回复，删除自己的评论 |
| 🔍 **全文搜索** | 按标题和摘要关键词搜索文章 |
| 🎨 **赛博朋克UI** | 霓虹配色、扫描线动效、Glitch 特效 |
| 📦 **文件存储** | MinIO 对象存储 MD 原文和图片 |

---

## 📁 项目结构

```
myblog/
├── docker-compose.yml       # 全栈 Docker 编排（5个服务）
├── start.sh / start.bat     # ← 一键启动脚本
├── README.md
├── backend/                 # Spring Boot 3 后端
│   ├── Dockerfile           # 多阶段构建：Maven 编译 + JRE 运行
│   └── src/main/
│       ├── java/com/cyberblog/backend/
│       │   ├── controller/  # AuthController ArticleController 等
│       │   ├── service/     # 业务逻辑层
│       │   ├── entity/      # 数据库实体
│       │   ├── security/    # JWT 工具 + 过滤器
│       │   └── config/      # Security MinIO MyBatisPlus 配置
│       └── resources/
│           └── application.yml   # 支持环境变量覆盖
└── frontend/                # Vue 3 + Vite + TypeScript 前端
    ├── Dockerfile           # Node 构建 + nginx 托管
    ├── nginx.conf           # SPA fallback + /api 反代
    └── src/
        ├── api/             # Axios 封装
        ├── stores/          # Pinia (user)
        ├── views/           # 页面组件
        └── styles/          # 赛博朋克 CSS 主题
```

---

## 🔧 本地开发（不用 Docker 跑前后端）

先启动中间件：
```bash
start.bat dev     # 只启动 MySQL + Redis + MinIO
```

然后分别启动：
```bash
# 后端（需要 Java 17+ 和 Maven）
cd backend
mvn spring-boot:run

# 前端（需要 Node 18+）
cd frontend
npm install
npm run dev       # 访问 http://localhost:5173
```

---

## 🐳 Docker 服务说明

| 服务 | 镜像 | 端口 | 说明 |
|------|------|------|------|
| `frontend` | 自建（nginx） | 80 | Vue3 + nginx |
| `backend` | 自建（temurin-17） | 8080 | Spring Boot |
| `mysql` | mysql:8.0 | 3306 | 数据库 |
| `redis` | redis:7-alpine | 6379 | 缓存/点赞计数 |
| `minio` | minio/minio | 9000/9001 | 对象存储 |

所有服务均配置 **healthcheck**，启动顺序自动保证：`MySQL/Redis/MinIO` → `backend` → `frontend`

---

## 📡 主要 API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 注册（一键注册，返回 JWT） |
| POST | `/api/auth/login` | 登录 |
| GET  | `/api/articles` | 文章列表（分页+搜索） |
| GET  | `/api/articles/{id}` | 文章详情 |
| POST | `/api/articles` | 手写发布文章 |
| POST | `/api/articles/upload` | 上传 MD 文件发布 |
| POST | `/api/likes/{type}/{id}` | 点赞/取消（支持游客） |
| POST | `/api/articles/{id}/comments` | 发表评论 |

---

## 🏗️ 后续扩展建议

1. **全文搜索**：引入 Elasticsearch
2. **图片上传**：写作页面支持粘贴/拖拽图片直传 MinIO
3. **OAuth 登录**：接入 GitHub / Google 第三方登录
4. **邮件通知**：评论/点赞时邮件通知作者
