@echo off
TITLE BuildPro Cloud - Push to GitHub
COLOR 0B
echo ===============================================================================
echo          BUILDPRO CLOUD - SAVE TO GITHUB (NarlaSindhuja-5)
echo ===============================================================================
echo.
cd /d "%~dp0"

echo [1/3] Adding all files to Git...
"C:\Program Files\Git\cmd\git.exe" add .

echo [2/3] Checking git commit status...
"C:\Program Files\Git\cmd\git.exe" commit -m "feat: complete end-to-end construction management & resource booking platform" 2>nul
if %ERRORLEVEL% EQU 0 (
    echo Committed latest changes.
) else (
    echo Working tree is already up to date.
)

echo.
echo [3/3] Pushing to GitHub (https://github.com/NarlaSindhuja-5/construction-management-platform)...
echo.
echo NOTE: If you have not created this repository on GitHub yet:
echo   1. Open: https://github.com/new
echo   2. Repository name: construction-management-platform
echo   3. Set to Public
echo   4. Click "Create repository" (leave README/license unchecked)
echo.
"C:\Program Files\Git\cmd\git.exe" push -u origin main

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ===============================================================================
    echo SUCCESS! Your project has been uploaded to:
    echo https://github.com/NarlaSindhuja-5/construction-management-platform
    echo ===============================================================================
) else (
    echo.
    echo -------------------------------------------------------------------------------
    echo If GitHub says 'Repository not found', please make sure you created the repo
    echo at: https://github.com/new
    echo with the name: construction-management-platform
    echo -------------------------------------------------------------------------------
)

echo.
pause
