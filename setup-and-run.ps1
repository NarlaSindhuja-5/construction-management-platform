# ==============================================================================
# E2E Construction Management System - Quick Start & DB Connection Test Script
# ==============================================================================

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  E2E Construction Management System - Database & Backend " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Prompt for MySQL root password
$password = Read-Host "Enter your MySQL root password"

# 2. Check and import schema.sql
Write-Host "`n[Step 1/3] Initializing MySQL Database (e2e_construction_db)..." -ForegroundColor Yellow
$schemaPath = Join-Path $PSScriptRoot "database\schema.sql"

if (Test-Path $schemaPath) {
    if ([string]::IsNullOrEmpty($password)) {
        & mysql -u root -e "source $schemaPath"
    } else {
        & mysql -u root "-p$password" -e "source $schemaPath"
    }

    if ($LASTEXITCODE -eq 0) {
        Write-Host "Database and schema created successfully!" -ForegroundColor Green
    } else {
        Write-Host "Error running schema.sql. Please check your MySQL password." -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "Schema file not found at: $schemaPath" -ForegroundColor Red
    exit 1
}

# 3. Set environment variable for current session
Write-Host "`n[Step 2/3] Configuring environment variables..." -ForegroundColor Yellow
$env:DB_HOST = "localhost"
$env:DB_PORT = "3306"
$env:DB_NAME = "e2e_construction_db"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = $password

Write-Host "Environment variables configured for MySQL." -ForegroundColor Green

# 4. Start Spring Boot backend
Write-Host "`n[Step 3/3] Starting Spring Boot application..." -ForegroundColor Yellow
Write-Host "Backend will run on: http://localhost:8080" -ForegroundColor Cyan
Write-Host "Health check endpoint: http://localhost:8080/api/health`n" -ForegroundColor Cyan

$backendDir = Join-Path $PSScriptRoot "backend"
Set-Location $backendDir
& mvn spring-boot:run
