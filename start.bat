@echo off
chcp 65001 >nul 2>&1
setlocal

set "ACTION=%~1"
if "%ACTION%"=="" set "ACTION=prod"

docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not running. Please start Docker Desktop first.
    pause
    exit /b 1
)

if /i "%ACTION%"=="prod"    goto PROD
if /i "%ACTION%"=="dev"     goto DEV
if /i "%ACTION%"=="stop"    goto STOP
if /i "%ACTION%"=="restart" goto RESTART
if /i "%ACTION%"=="logs"    goto LOGS
goto USAGE

:PROD
echo ================================================
echo   CyberBlog - Start All Services
echo ================================================
echo [1/3] Building and starting all services...
echo       (First build takes 3-5 min, subsequent starts ~30s)
echo.
docker compose up --build -d
if errorlevel 1 (
    echo.
    echo [ERROR] Build or start failed. Run: .\start.bat logs
    goto END
)
echo.
echo [2/3] Waiting for services to become healthy...
timeout /t 5 /nobreak >nul
docker compose ps
echo.
echo [3/3] Done! Open in browser:
echo.
echo   Frontend : http://localhost
echo   API Docs : http://localhost:8080/doc.html
echo   MinIO    : http://localhost:9001  (minioadmin / minioadmin123)
echo.
echo   View logs : .\start.bat logs
echo   Stop all  : .\start.bat stop
goto END

:DEV
echo Starting middleware only (dev mode)...
docker compose up mysql redis minio -d
echo.
echo Middleware started. Run backend and frontend manually:
echo   Backend : cd backend ^&^& mvn spring-boot:run
echo   Frontend: cd frontend ^&^& npm run dev
echo   Visit   : http://localhost:5173
goto END

:STOP
echo Stopping all CyberBlog services...
docker compose down
echo All services stopped.
goto END

:RESTART
echo Rebuilding and restarting...
docker compose down
docker compose up --build -d
echo Restart complete.
goto END

:LOGS
docker compose logs -f --tail=100
goto END

:USAGE
echo.
echo Usage: .\start.bat [prod^|dev^|stop^|restart^|logs]
echo.
echo   prod     - Build and start all services (default)
echo   dev      - Start middleware only (for local development)
echo   stop     - Stop all services
echo   restart  - Rebuild and restart all services
echo   logs     - Follow live logs (Ctrl+C to exit)
echo.

:END
endlocal
