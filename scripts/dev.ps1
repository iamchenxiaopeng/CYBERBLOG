# ============================================
# CyberBlog 开发环境启动脚本
# ============================================
# 启动 Docker 中间件服务
# 然后本地运行前后端进行调试
# ============================================

param(
    [switch]$All,        # 启动所有服务（包括前后端 Docker 容器）
    [switch]$Stop,       # 停止所有服务
    [switch]$Clean,      # 清理所有容器和数据
    [switch]$Status,     # 查看服务状态
    [switch]$Logs        # 查看日志
)

$ErrorActionPreference = "Stop"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   CyberBlog 开发环境管理脚本" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# 项目根目录
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectRoot

# 颜色函数
function Write-Success { param($msg) Write-Host "✅ $msg" -ForegroundColor Green }
function Write-Info { param($msg) Write-Host "ℹ️  $msg" -ForegroundColor Blue }
function Write-Warn { param($msg) Write-Host "⚠️  $msg" -ForegroundColor Yellow }
function Write-Error { param($msg) Write-Host "❌ $msg" -ForegroundColor Red }

# 查看状态
if ($Status) {
    Write-Host "`n📊 Docker 容器状态：`n" -ForegroundColor Cyan
    docker ps -a --filter "name=cyberblog" --format "table {{.Names}}`t{{.Status}}`t{{.Ports}}"
    
    Write-Host "`n📦 Docker 卷状态：`n" -ForegroundColor Cyan
    docker volume ls --filter "name=cyberblog" --format "{{.Name}}`t{{.Mountpoint}}"
    
    Write-Host "`n🌐 本地服务端口：`n" -ForegroundColor Cyan
    Write-Host "  - MySQL:   localhost:3306"
    Write-Host "  - Redis:   localhost:6379"
    Write-Host "  - MinIO:   localhost:9000 (API), localhost:9001 (Console)"
    Write-Host "  - Backend: localhost:8080"
    Write-Host "  - Frontend: localhost:5173 (开发模式)"
    exit
}

# 查看日志
if ($Logs) {
    Write-Host "`n📝 查看容器日志（Ctrl+C 退出）：`n" -ForegroundColor Cyan
    docker logs -f cyberblog-mysql --tail 50
    exit
}

# 停止服务
if ($Stop) {
    Write-Info "停止所有服务..."
    
    # 停止 Docker 容器
    docker-compose -f docker-compose.yml stop 2>$null
    docker-compose -f docker-compose.dev.yml stop 2>$null
    
    Write-Success "所有服务已停止"
    exit
}

# 清理服务
if ($Clean) {
    Write-Warn "清理所有容器和数据..."
    
    # 停止并删除容器
    docker-compose -f docker-compose.yml down -v 2>$null
    docker-compose -f docker-compose.dev.yml down -v 2>$null
    
    Write-Success "清理完成"
    exit
}

# 启动所有服务（完整 Docker 部署）
if ($All) {
    Write-Info "启动完整 Docker 部署（前后端 + 中间件）..."
    
    docker-compose -f docker-compose.yml up -d
    
    Write-Host "`n" -NoNewline
    Write-Success "所有服务已启动！"
    Write-Host "`n🌐 访问地址：" -ForegroundColor Cyan
    Write-Host "  - 前端博客: http://localhost"
    Write-Host "  - 后端 API: http://localhost:8080"
    Write-Host "  - API 文档: http://localhost:8080/doc.html"
    Write-Host "  - MinIO:    http://localhost:9001"
    exit
}

# 启动开发环境（仅中间件 + 本地前后端）
Write-Host "`n🚀 启动开发环境" -ForegroundColor Cyan
Write-Host "  - Docker: MySQL, Redis, MinIO"
Write-Host "  - 本地:  后端 (8080), 前端 (5173)`n" -ForegroundColor Gray

# 启动 Docker 中间件
Write-Info "启动 Docker 中间件服务..."
docker-compose -f docker-compose.dev.yml up -d

# 等待 MySQL 健康检查
Write-Info "等待 MySQL 启动..."
$maxAttempts = 30
$attempt = 0
while ($attempt -lt $maxAttempts) {
    $health = docker inspect --format='{{.State.Health.Status}}' cyberblog-mysql 2>$null
    if ($health -eq "healthy") {
        break
    }
    Start-Sleep -Seconds 2
    $attempt++
    Write-Host "." -NoNewline
}
Write-Host "`n"

# 等待 MinIO 健康检查
Write-Info "等待 MinIO 启动..."
Start-Sleep -Seconds 5

# 验证服务状态
Write-Host "`n📊 服务状态：`n" -ForegroundColor Cyan
docker ps --filter "name=cyberblog" --format "  {{.Names}}: {{.Status}}"

Write-Host "`n" -NoNewline
Write-Success "Docker 中间件已就绪！"

Write-Host "`n📋 下一步操作：" -ForegroundColor Yellow
Write-Host "  1. 启动后端：" -ForegroundColor Gray
Write-Host "     cd backend" -ForegroundColor Gray
Write-Host "     mvn spring-boot:run" -ForegroundColor Gray
Write-Host ""
Write-Host "  2. 启动前端：" -ForegroundColor Gray
Write-Host "     cd frontend" -ForegroundColor Gray
Write-Host "     npm install" -ForegroundColor Gray
Write-Host "     npm run dev" -ForegroundColor Gray

Write-Host "`n🌐 服务地址：" -ForegroundColor Cyan
Write-Host "  - MySQL:   localhost:3306 (root/cyberblog123)"
Write-Host "  - Redis:   localhost:6379"
Write-Host "  - MinIO:   http://localhost:9000"
Write-Host "  - Console: http://localhost:9001 (minioadmin/minioadmin123)"
Write-Host "  - Backend: http://localhost:8080"
Write-Host "  - Frontend: http://localhost:5173`n"
