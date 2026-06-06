#!/usr/bin/env bash
# ============================================================
# CyberBlog 一键启动脚本
# 用法：bash start.sh [dev|prod|stop|restart|logs]
# ============================================================

set -e

ACTION=${1:-prod}
COMPOSE_FILE="$(dirname "$0")/docker-compose.yml"

check_docker() {
  if ! command -v docker &>/dev/null; then
    echo "[ERROR] Docker 未安装，请先安装 Docker Desktop"
    exit 1
  fi
  if ! docker info &>/dev/null; then
    echo "[ERROR] Docker 未运行，请启动 Docker Desktop"
    exit 1
  fi
}

case "$ACTION" in
  prod)
    check_docker
    echo "================================================"
    echo "  🚀 CyberBlog — 一键启动（生产模式）"
    echo "================================================"
    echo "[1/3] 构建并启动所有服务（首次构建约 3-5 分钟）..."
    docker compose -f "$COMPOSE_FILE" up --build -d
    echo ""
    echo "[2/3] 等待服务健康检查完成..."
    sleep 5
    docker compose -f "$COMPOSE_FILE" ps
    echo ""
    echo "[3/3] 完成！访问地址："
    echo "  🌐 博客前端:  http://localhost"
    echo "  🔧 后端 API:  http://localhost:8080/doc.html"
    echo "  🗄️  MinIO:    http://localhost:9001  (minioadmin/minioadmin123)"
    echo ""
    echo "  查看日志: bash start.sh logs"
    echo "  停止服务: bash start.sh stop"
    ;;
  dev)
    check_docker
    echo "================================================"
    echo "  🔧 CyberBlog — 启动中间件（本地开发模式）"
    echo "================================================"
    docker compose -f "$COMPOSE_FILE" up mysql redis minio -d
    echo ""
    echo "中间件已启动，请手动启动后端和前端："
    echo "  后端: cd backend && mvn spring-boot:run"
    echo "  前端: cd frontend && npm run dev"
    echo ""
    echo "  访问: http://localhost:5173"
    ;;
  stop)
    check_docker
    echo "停止所有 CyberBlog 服务..."
    docker compose -f "$COMPOSE_FILE" down
    echo "所有服务已停止。"
    ;;
  restart)
    check_docker
    echo "重启所有 CyberBlog 服务..."
    docker compose -f "$COMPOSE_FILE" down
    docker compose -f "$COMPOSE_FILE" up --build -d
    echo "重启完成。"
    ;;
  logs)
    check_docker
    docker compose -f "$COMPOSE_FILE" logs -f --tail=100
    ;;
  *)
    echo "用法: bash start.sh [prod|dev|stop|restart|logs]"
    echo "  prod     — 一键构建并启动所有服务（默认）"
    echo "  dev      — 只启动中间件，供本地开发"
    echo "  stop     — 停止所有服务"
    echo "  restart  — 重建并重启"
    echo "  logs     — 实时查看日志"
    ;;
esac
