# CyberBlog 项目长期记录

## 项目概述
赛博朋克风格个人博客，全容器化，一行命令启动。
- 前端：Vue3 + Vite + TypeScript → Docker(nginx:80)
- 后端：Spring Boot 3 + Java 17 → Docker(8080)
- 中间件：MySQL 8 / Redis 7 / MinIO（docker-compose）

## 一键启动
- Windows: `start.bat`  Linux/Mac: `bash start.sh`
- 访问: http://localhost（nginx 80）/ http://localhost:8080（API）
- 首次启动约 3-5 分钟（Maven 编译 + npm build）
- healthcheck 依赖链：MySQL/Redis/MinIO → backend → frontend

## 技术选型
- ORM：MyBatis-Plus 3.5.7（含分页插件）
- JWT：jjwt 0.12.6
- 文件存储：MinIO（bucket: cyberblog，公共读策略）
- API 文档：Knife4j（访问 /doc.html）
- MD 渲染：marked + highlight.js（atom-one-dark 主题）

## 关键配置
- DB: cyberblog / cyberblog123
- MinIO: minioadmin / minioadmin123
- JWT secret 在 application.yml（生产需替换）

## 已实现功能
- 用户注册/登录（JWT，一键注册）
- 文章 CRUD + MD 文件上传 + 关键词搜索 + 分页
- 点赞（切换，防重复）/ 二级评论
- 赛博朋克 CSS 主题（扫描线 + Glitch + 霓虹）

## 待完成
- 全文搜索（Elasticsearch 可选）
- 图片上传功能（写作页面）
- OAuth 登录
