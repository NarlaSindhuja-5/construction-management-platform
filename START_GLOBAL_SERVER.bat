@echo off
TITLE BuildPro Cloud - Global Server Launcher
COLOR 0B
echo ===============================================================================
echo          BUILDPRO CLOUD - END-TO-END CONSTRUCTION MANAGEMENT PLATFORM
echo ===============================================================================
echo.
echo [1/3] Checking MySQL database connectivity...
cd /d "%~dp0\construction-management-platform\backend"

echo [2/3] Starting Express Backend Server on port 5000...
start "BuildPro Backend Server (Port 5000)" cmd /k "node server.js"

echo Waiting 3 seconds for server to initialize...
timeout /t 3 /nobreak >nul

echo [3/3] Launching Global Cloudflare Public Tunnel...
echo.
echo Once started, copy the 'https://*.trycloudflare.com' URL below and share it
echo with judges, evaluators, faculty, or open it on your mobile phone!
echo.
echo ===============================================================================
.\bin\cloudflared.exe tunnel --url http://127.0.0.1:5000
pause
