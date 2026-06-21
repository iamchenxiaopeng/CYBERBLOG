#!/usr/bin/env bash
# ============================================================
# CyberBlog 服务器故障排查与修复脚本
# 用法：bash fix-login.sh [check|fix|rebuild|logs]
# ============================================================

set -e

COMPOSE_FILE="$(dirname "$0")/docker-compose.yml"
PROJECT_DIR="$(dirname "$0")"

echo "================================================"
echo "  🔧 CyberBlog 登录问题诊断工具"
echo "================================================"

ACTION=${1:-check}

case "$ACTION" in

  check)
    echo ""
    echo "=== [1/5] 容器状态 ==="
    docker compose -f "$COMPOSE_FILE" ps -a 2>/dev/null || {
      echo "[ERROR] 无法连接 Docker，请确认 Docker 服务正在运行"
      exit 1
    }

    echo ""
    echo "=== [2/5] 后端日志（最近 50 行）==="
    docker compose -f "$COMPOSE_FILE" logs backend --tail=50 2>/dev/null || echo "[WARN] 无后端日志"

    echo ""
    echo "=== [3/5] 测试后端 API 连通性 ==="
    if curl -sf http://localhost:8080/api/articles?page=1\&size=1 >/dev/null 2>&1; then
      echo "[OK] 后端 API 可访问 (http://localhost:8080)"
    else
      echo "[FAIL] 后端 API 无响应！可能原因："
      echo "  - 后端容器未启动或崩溃"
      echo "  - 数据库连接失败"
      echo "  - Java 版本不兼容"
      echo ""
      echo "建议运行: bash fix-login.sh logs  查看详细错误"
    fi

    echo ""
    echo "=== [4/5] 测试登录接口 ==="
    # 前端现在传输 SHA-256 hash 后的密码，所以这里也用 SHA-256 测试
    SHA256_PWD=$(echo -n "Admin@2077" | sha256sum 2>/dev/null | awk '{print $1}' || \
                 python3 -c "import hashlib;print(hashlib.sha256(b'Admin@2077').hexdigest())" 2>/dev/null || \
                 echo "58746ecce1f92666809aff06d38b7161709581f2fb9fb4015eefda45a6bc0ddd")

    LOGIN_RESULT=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST \
      http://localhost:8080/api/auth/login \
      -H "Content-Type: application/json" \
      -d "{\"username\":\"admin\",\"password\":\"$SHA256_PWD\"}" 2>/dev/null)
    HTTP_CODE=$(echo "$LOGIN_RESULT" | grep "HTTP_CODE:" | cut -d: -f2)
    BODY=$(echo "$LOGIN_RESULT" | grep -v "HTTP_CODE:")

    echo "HTTP 状态码: $HTTP_CODE"
    echo "响应内容: $BODY"

    if [ "$HTTP_CODE" = "200" ]; then
      echo "[OK] 登录接口正常！前端问题请检查 nginx 配置或浏览器控制台"
    elif [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "403" ]; then
      echo "[FAIL] 认证失败！原因："
      echo "  - admin 账号不存在 → 运行 bash fix-login.sh fix 初始化账号"
      echo "  - 密码不匹配 → 数据库中可能是明文密码，需要重新 hash"
    else
      echo "[WARN] 返回 $HTTP_CODE，查看上方日志获取详情"
    fi

    echo ""
    echo "=== [5/5] 数据库密码格式检查 ==="
    docker exec cyberblog-mysql mysql -uroot -pcyberblog123 cyberblog \
      -e "SELECT id, username, LEFT(password_hash, 20) as pwd_prefix, role FROM users;" 2>/dev/null \
      || echo "[INFO] 无法查询数据库（容器可能未运行）"

    echo ""
    echo "=== 诊断完成 ==="
    ;;

  fix)
    echo ""
    echo "=== 修复方案：重新初始化管理员账号 ==="
    echo ""
    echo "注意：新版本前端传输的是 SHA-256(password)，"
    echo "数据库存储格式为 BCrypt(SHA-256(密码明文))"
    echo ""

    # Admin@2077 → SHA-256 → BCrypt
    # SHA-256("Admin@2077") = 58746ecce1f92666809aff06d38b7161709581f2fb9fb4015eefda45a6bc0ddd
    # BCrypt(上述值) = $2b$10$k3rd0r6.vhlqTbD342cVx.QtPuJ/jowPcnh01UX9ijKitL.qyCn86
    BCRYPT_HASH='$2b$10$k3rd0r6.vhlqTbD342cVx.QtPuJ/jowPcnh01UX9ijKitL.qyCn86'

    echo "正在将 admin 密码重置为 BCrypt(SHA-256) 格式..."
    docker exec cyberblog-mysql mysql -uroot -pcyberblog123 cyberblog \
      -e "
        INSERT INTO users (username, email, password_hash, avatar_url, bio, role, created_at, updated_at)
        VALUES ('admin', 'admin@cyberblog.local', '$BCRYPT_HASH', '/avatar-default.svg', '赛博博客系统管理员', 'admin', NOW(), NOW())
        ON DUPLICATE KEY UPDATE password_hash = '$BCRYPT_HASH', role = 'admin';
      " 2>/dev/null && echo "[OK] 管理员账号已修复" || echo "[ERROR] 修复失败，请检查 MySQL 容器状态"

    # 验证（用 SHA-256 后的密码测试，模拟前端行为）
    echo ""
    echo "验证登录（使用 SHA-256 加密后的密码）..."
    sleep 1
    SHA256_PWD=$(echo -n "Admin@2077" | sha256sum 2>/dev/null | awk '{print $1}' || \
                 python3 -c "import hashlib;print(hashlib.sha256(b'Admin@2077').hexdigest())" 2>/dev/null || \
                 echo "58746ecce1f92666809aff06d38b7161709581f2fb9fb4015eefda45a6bc0ddd")
    curl -s -X POST http://localhost:8080/api/auth/login \
      -H "Content-Type: application/json" \
      -d "{\"username\":\"admin\",\"password\":\"$SHA256_PWD\"}"
    ;;

  rebuild)
    echo ""
    echo "=== 重建并重启所有服务（清除缓存）==="
    echo ""
    docker compose -f "$COMPOSE_FILE" down
    docker compose -f "$COMPOSE_FILE" build --no-cache
    docker compose -f "$COMPOSE_FILE" up -d
    echo ""
    echo "等待服务启动（约 30-60 秒）..."
    sleep 15
    echo ""
    echo "容器状态："
    docker compose -f "$COMPOSE_FILE" ps
    echo ""
    echo "完成！访问 http://你的服务器IP 测试登录"
    ;;

  logs)
    echo "=== 后端实时日志（Ctrl+C 退出）==="
    docker compose -f "$COMPOSE_FILE" logs -f backend --tail=100
    ;;

  *)
    echo "用法: bash fix-login.sh [command]"
    echo ""
    echo "命令:"
    echo "  check   — 全面诊断（默认）"
    echo "  fix     — 修复 admin 密码为 BCrypt 格式"
    echo "  rebuild — 无缓存重建并重启全部服务"
    echo "  logs    — 查看后端实时日志"
    ;;
esac
