# CyberBlog 自动化部署技术说明

> 本文档详细说明项目如何实现「一行命令启动」的完整机制，涵盖 Docker 容器化、多阶段构建、healthcheck 依赖链、nginx 反代、环境变量注入等核心技术。

---

## 一、整体架构：从「手动五步」到「一键启动」

### 改造前（手动模式）

```
手动步骤：
① docker-compose up -d           ← 启动 MySQL / Redis / MinIO
② mvn clean package -DskipTests  ← 编译后端（需本地安装 Java + Maven）
③ java -jar backend.jar          ← 启动后端
④ npm install && npm run dev     ← 启动前端开发服务器
⑤ 手动配置各种连接地址
```

问题：需要本地安装 Java 17、Maven、Node.js；连接地址写死在配置文件；没有启动顺序保障。

### 改造后（容器化模式）

```
一行命令：
start.bat  ←（或 bash start.sh）

内部自动完成：
① 检查 Docker 是否运行
② docker compose up --build -d
   ├─ MySQL 容器启动 + 自动建表（挂载 init.sql）
   ├─ Redis 容器启动
   ├─ MinIO 容器启动
   ├─ 等待三者健康检查通过 ↓
   ├─ 后端容器：Maven 编译 → Spring Boot 启动 → 健康检查通过 ↓
   └─ 前端容器：npm build → nginx 静态托管 + 反代 API
```

唯一依赖：**Docker Desktop**（已在机器上安装）。

---

## 二、核心文件清单

```
myblog/
├── docker-compose.yml          ← 编排 5 个服务，配健康检查依赖链
├── start.bat                   ← Windows 一键脚本（封装 docker compose 指令）
├── start.sh                    ← Linux/macOS 一键脚本
│
├── backend/
│   ├── Dockerfile              ← 多阶段构建：Maven 编译 + JRE 运行
│   ├── .dockerignore           ← 排除 target/ 避免污染镜像缓存
│   └── src/main/resources/
│       ├── application.yml     ← 所有连接配置支持环境变量覆盖
│       └── init.sql            ← MySQL 自动建表 SQL
│
└── frontend/
    ├── Dockerfile              ← 多阶段构建：Node 编译 + nginx 托管
    ├── nginx.conf              ← SPA fallback + /api/ 反代到后端容器
    └── .dockerignore           ← 排除 node_modules / dist
```

---

## 三、技术细节一：多阶段 Docker 构建

### 3.1 后端 Dockerfile（两阶段）

```dockerfile
# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# 【关键优化】先只复制 pom.xml，利用 Docker 层缓存
# 只有 pom.xml 变化时才重新下载依赖，源码修改不触发重下依赖
COPY pom.xml .
RUN mvn dependency:go-offline -q

# 再复制源码并编译（跳过测试，加快速度）
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2: Run ─────────────────────────────────────────────────────────────
# 最终镜像只含 JRE（比完整 JDK 小 300MB+）
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=build /app/target/cyberblog-backend-1.0.0.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**要点说明：**
- **两阶段构建**：编译阶段（含 Maven、JDK）只是临时容器，最终镜像只有 JRE + JAR，体积从约 700MB 降至约 200MB。
- **层缓存优化**：`pom.xml` 单独复制，第二次构建若只改了 Java 代码，Maven 依赖层直接复用缓存，构建时间从 5 分钟降到 30 秒。

### 3.2 前端 Dockerfile（两阶段）

```dockerfile
# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM node:20-alpine AS build

WORKDIR /app

# 同样先复制 package.json，利用层缓存
COPY package.json package-lock.json* ./
RUN npm install --frozen-lockfile

COPY . .
RUN npm run build           # Vite 打包输出到 dist/

# ── Stage 2: Serve with nginx ────────────────────────────────────────────────
FROM nginx:alpine

# 把 dist/ 静态文件放到 nginx 默认根目录
COPY --from=build /app/dist /usr/share/nginx/html
# 覆盖 nginx 默认配置（支持 SPA + 反代）
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
```

**要点说明：**
- 最终镜像只有 nginx + 静态 HTML/JS/CSS，不含 Node.js 运行时，体积约 30MB。
- `--frozen-lockfile` 确保 npm 版本锁定，避免因包版本漂移导致构建失败。

---

## 四、技术细节二：nginx 反向代理

前端和后端在不同容器，浏览器却只访问一个地址（`http://localhost`）。nginx 在中间做路由分发：

```nginx
server {
    listen 80;

    # ① 静态资源直接返回（Vue 打包后的 HTML/JS/CSS）
    location / {
        root /usr/share/nginx/html;
        # Vue Router history 模式必须：所有未匹配路径都返回 index.html
        try_files $uri $uri/ /index.html;
    }

    # ② /api/ 开头的请求反代到后端容器
    # "backend" 是 docker-compose 中的服务名，Docker 内网 DNS 自动解析
    location /api/ {
        proxy_pass http://backend:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_connect_timeout 60s;
        proxy_read_timeout 60s;
    }

    # ③ 静态资源 7 天缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf)$ {
        expires 7d;
        add_header Cache-Control "public, immutable";
    }

    # ④ gzip 压缩响应
    gzip on;
    gzip_types text/plain text/css application/json application/javascript;
}
```

**核心原理：**
- Docker Compose 内部有独立的虚拟网络，所有服务名（`mysql`、`redis`、`backend`）可以直接作为主机名互相访问。
- 前端 `axios` 的 `baseURL: '/api'` 是相对路径，浏览器发请求到 `http://localhost/api/xxx`，nginx 收到后转发给 `http://backend:8080/api/xxx`，对浏览器完全透明（同源请求，无跨域问题）。

---

## 五、技术细节三：healthcheck 依赖链

这是保证服务启动顺序的核心机制。Docker Compose 原生的 `depends_on` 只保证容器**启动顺序**，不等待服务**就绪**。加上 `condition: service_healthy` 才能真正等到服务可用。

```
启动顺序（由 healthcheck 驱动）：

MySQL ──────── [healthcheck: mysqladmin ping]  ──► healthy ─┐
Redis ──────── [healthcheck: redis-cli ping]   ──► healthy ─┤
MinIO ──────── [healthcheck: curl /health]     ──► healthy ─┘
                                                              │
                                                              ▼
                          backend ─── [depends_on: 三者 healthy]
                          [healthcheck: GET /api/articles] ──► healthy
                                                              │
                                                              ▼
                          frontend ─── [depends_on: backend healthy]
                          [nginx 启动并监听 80 端口] ──► 可访问
```

**各服务 healthcheck 配置：**

| 服务 | 健康检查命令 | 等待逻辑 |
|------|------------|---------|
| mysql | `mysqladmin ping -h localhost` | interval 10s, retries 10, start_period 30s |
| redis | `redis-cli ping` | interval 10s, retries 5 |
| minio | `curl -f http://localhost:9000/minio/health/live` | interval 10s, retries 5 |
| backend | `wget http://localhost:8080/api/articles?page=1` | interval 15s, retries 8, start_period 60s |

**为什么 backend 要 `start_period: 60s`？**  
Spring Boot 启动要加载 MyBatis、建立数据库连接池、检查 Redis……需要 30-60 秒。`start_period` 内的失败不计入 retries，避免还在初始化就被判为不健康。

---

## 六、技术细节四：环境变量注入

后端 `application.yml` 所有连接配置都改成了「有默认值的环境变量」写法：

```yaml
# 语法：${ENV_VAR_NAME:默认值}

spring:
  datasource:
    # 本地开发用 localhost，容器内用 Docker 服务名 mysql
    url: ${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/cyberblog...}
    username: ${SPRING_DATASOURCE_USERNAME:root}
    password: ${SPRING_DATASOURCE_PASSWORD:cyberblog123}

  data:
    redis:
      host: ${SPRING_DATA_REDIS_HOST:localhost}

minio:
  endpoint: ${MINIO_ENDPOINT:http://localhost:9000}
```

`docker-compose.yml` 的 `environment` 节点注入对应的值（使用容器内网服务名）：

```yaml
backend:
  environment:
    SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/cyberblog...
    SPRING_DATA_REDIS_HOST: redis
    MINIO_ENDPOINT: http://minio:9000
```

**好处：** 同一套代码和配置文件，在本地（`localhost`）和 Docker 容器（`mysql`）环境下都能运行，无需修改配置文件。

---

## 七、技术细节五：MySQL 自动建表

`docker-compose.yml` 把 SQL 文件挂载到 MySQL 容器的初始化目录：

```yaml
mysql:
  volumes:
    - ./backend/src/main/resources/init.sql:/docker-entrypoint-initdb.d/init.sql
```

MySQL 官方镜像规定：容器首次启动时，会自动执行 `/docker-entrypoint-initdb.d/` 目录下的所有 `.sql` 文件。

所以 `init.sql` 里的 `CREATE TABLE IF NOT EXISTS ...` 会在 MySQL 启动时自动执行，完成 `users`、`articles`、`comments`、`likes` 四张表的创建——不需要手动连库执行任何 SQL。

---

## 八、技术细节六：.dockerignore 优化

两个 `.dockerignore` 文件防止无效文件进入 Docker 构建上下文：

**`frontend/.dockerignore`：**
```
node_modules    ← 几百 MB，容器内会重新 npm install
dist            ← 构建输出，容器内会重新 npm run build
.git
*.log
```

**`backend/.dockerignore`：**
```
target          ← 本地编译产物，容器内会重新 mvn package
*.log
.git
```

如果不加 `.dockerignore`，`node_modules`（约 200-500MB）会被上传到 Docker 守护进程，导致每次构建都非常慢。

---

## 九、一键脚本功能说明

`start.bat` / `start.sh` 封装了 4 个常用操作：

```bash
# 生产模式（默认）：构建所有服务并后台启动
start.bat
start.bat prod

# 开发模式：只启动 MySQL/Redis/MinIO，手动跑前后端（热重载调试）
start.bat dev

# 停止所有服务
start.bat stop

# 代码修改后重建（等于 stop + build + start）
start.bat restart

# 实时查看所有容器日志（Ctrl+C 退出）
start.bat logs
```

脚本首先检查 Docker 是否运行，给出友好的错误提示，避免出现难以理解的 Docker 报错。

---

## 十、访问地址汇总

启动成功后，访问以下地址：

| 服务 | 地址 | 说明 |
|------|------|------|
| 博客前端 | http://localhost | nginx 80 端口 |
| 后端 API 文档 | http://localhost:8080/doc.html | Knife4j Swagger |
| MinIO 控制台 | http://localhost:9001 | 账号 minioadmin / minioadmin123 |
| MySQL | localhost:3306 | cyberblog / cyberblog123 |
| Redis | localhost:6379 | 无密码 |

---

## 十一、常见问题

**Q: 首次启动很慢？**  
A: 正常。首次需要拉取 5 个 Docker 镜像（约 1-2GB）+ Maven 下载依赖 + npm install，总计 3-5 分钟。之后启动只需 30 秒左右（镜像和依赖已缓存）。

**Q: `docker: command not found`？**  
A: 需要安装 [Docker Desktop](https://www.docker.com/products/docker-desktop/)，安装后重启终端。

**Q: 端口 80 / 8080 / 3306 被占用？**  
A: 修改 `docker-compose.yml` 中对应服务的 `ports` 左侧端口（宿主机端口），例如把 `"80:80"` 改为 `"8888:80"`，然后访问 `http://localhost:8888`。

**Q: 改了后端代码如何更新？**  
A: 运行 `start.bat restart`，Docker 会重新编译后端镜像并重启服务。

**Q: 数据会丢失吗？**  
A: 不会。MySQL、Redis、MinIO 的数据都挂载到 Docker named volume（`mysql_data`、`redis_data`、`minio_data`），`docker compose down` 不会删除 volume，数据持久保留。只有执行 `docker compose down -v` 才会清空数据。
