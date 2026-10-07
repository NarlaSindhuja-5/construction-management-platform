@echo off
TITLE BuildPro Cloud - Local Server Launcher
COLOR 0A
echo ===============================================================================
echo          BUILDPRO CLOUD - LOCAL LAUNCHER
echo ===============================================================================
echo.
cd /d "%~dp0\construction-management-platform\backend"
echo Starting Backend API Server & Web App...
echo Local URL:    http://localhost:5000
echo Network URL:  http://192.168.0.104:5000 (accessible on same Wi-Fi)
echo.
node server.js
pause
