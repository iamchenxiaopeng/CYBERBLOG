@echo off
:: ============================================
:: CyberBlog Dev Environment Script
:: ============================================

:: Set UTF-8 encoding
chcp 65001 >nul

setlocal enabledelayedexpansion

set "SCRIPT_DIR=%~dp0"
set "PROJECT_DIR=%SCRIPT_DIR%.."
cd /d "%PROJECT_DIR%"

if "%1"=="stop" goto stop_cmd
if "%1"=="clean" goto clean_cmd
if "%1"=="status" goto status_cmd
if "%1"=="all" goto all_cmd
if "%1"=="help" goto help_cmd

goto dev_cmd

:dev_cmd
cls
echo.
echo ========================================
echo    CyberBlog Dev Environment
echo ========================================
echo.
echo [1/2] Starting Docker services...
echo.
docker-compose -f docker-compose.dev.yml up -d

if !errorlevel! neq 0 (
    echo.
    echo Error: Docker not running!
    echo Please start Docker Desktop first.
    pause
    exit /b 1
)

echo.
echo [2/2] Waiting for services...
timeout /t 15 /nobreak >nul

cls
echo.
echo ========================================
echo    Docker Services Ready!
echo ========================================
echo.
echo Next steps:
echo.
echo 1. Start Backend:
echo    cd backend
echo    mvn spring-boot:run
echo.
echo 2. Start Frontend:
echo    cd frontend
echo    npm run dev
echo.
echo Service Addresses:
echo    MySQL:    localhost:3306
echo    Redis:    localhost:6379
echo    MinIO:    localhost:9000
echo    Backend:  localhost:8080
echo    Frontend: localhost:5173
echo.
pause
goto :eof

:all_cmd
cls
echo.
echo ========================================
echo    Starting Full Docker Deployment
echo ========================================
echo.
docker-compose -f docker-compose.yml up -d

echo.
echo ========================================
echo    All Services Started!
echo ========================================
echo.
echo Access URLs:
echo    Frontend: http://localhost
echo    Backend:  http://localhost:8080
echo    Docs:     http://localhost:8080/doc.html
echo    MinIO:    http://localhost:9001
echo.
pause
goto :eof

:stop_cmd
cls
echo.
echo Stopping all services...
docker-compose -f docker-compose.yml stop
docker-compose -f docker-compose.dev.yml stop
echo.
echo All services stopped!
pause
goto :eof

:clean_cmd
cls
echo.
echo WARNING: This will delete all containers and data!
set "CONFIRM="
set /p "CONFIRM=Are you sure? (y/n): "
if /i not "!CONFIRM!"=="y" (
    echo Operation cancelled.
    pause
    goto :eof
)
echo.
echo Cleaning all containers and volumes...
docker-compose -f docker-compose.yml down -v
docker-compose -f docker-compose.dev.yml down -v
echo.
echo Cleanup completed!
pause
goto :eof

:status_cmd
cls
echo.
echo ========================================
echo    Service Status
echo ========================================
echo.
echo Docker Containers:
echo ----------------------------------------
docker ps -a --filter "name=cyberblog"
echo.
echo Service Addresses:
echo ----------------------------------------
echo    MySQL:    localhost:3306
echo    Redis:    localhost:6379
echo    MinIO:    localhost:9000
echo    Backend:  localhost:8080
echo    Frontend: localhost:5173
echo.
pause
goto :eof

:help_cmd
cls
echo.
echo ========================================
echo    CyberBlog Dev Script Help
echo ========================================
echo.
echo Usage:
echo.
echo   dev.bat          - Start dev environment (middleware only)
echo   dev.bat all      - Start full deployment (all services)
echo   dev.bat stop     - Stop all services
echo   dev.bat clean    - Delete all containers and data
echo   dev.bat status   - Show service status
echo   dev.bat help     - Show this help
echo.
pause
goto :eof
