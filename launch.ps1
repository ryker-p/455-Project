param(
    [switch]$SkipDocker,
    [string]$DbUser = "root",
    [string]$DbPassword = "password",
    [int]$DbPort = 3306,
    [string]$DbUrl = ""
)

$ErrorActionPreference = "Stop"

# Build DbUrl if not provided
if ([string]::IsNullOrEmpty($DbUrl)) {
    $DbUrl = "jdbc:mysql://localhost:$DbPort/emr_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
}

$RootDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$BackendDir = Join-Path $RootDir "emr-backend"
$FrontendDir = Join-Path $RootDir "emr-frontend"
$BackendOutLog = Join-Path $RootDir "backend.out.log"
$BackendErrLog = Join-Path $RootDir "backend.err.log"

function Write-LaunchLog {
    param([string]$Message)
    Write-Host ""
    Write-Host "[launch] $Message"
}

function Require-Command {
    param([string]$Name)

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Missing required command: $Name"
    }
}

Require-Command "npm.cmd"

if (-not $SkipDocker) {
    Require-Command "docker"

    Write-LaunchLog "Starting MySQL with Docker Compose..."
    Set-Location $RootDir
    docker compose up -d
    if ($LASTEXITCODE -ne 0) {
        throw "Docker Compose failed. Open Docker Desktop, wait until it is running, then run .\launch.ps1 again. If you want to use local MySQL instead, run .\launch.ps1 -SkipDocker -DbPassword your_mysql_password"
    }
} else {
    Write-LaunchLog "Skipping Docker. Backend will use local MySQL on localhost:$DbPort."
    Require-Command "mysql"

    Write-LaunchLog "Creating local MySQL database emr_db if needed..."
    $PreviousMysqlPassword = $env:MYSQL_PWD
    $env:MYSQL_PWD = $DbPassword
    mysql -u $DbUser -e "CREATE DATABASE IF NOT EXISTS emr_db;"
    $MysqlExitCode = $LASTEXITCODE
    $env:MYSQL_PWD = $PreviousMysqlPassword

    if ($MysqlExitCode -ne 0) {
        throw "Could not create or access emr_db with the provided MySQL credentials. Check -DbUser and -DbPassword."
    }
}

Write-LaunchLog "Starting Spring Boot backend on http://localhost:8080 ..."
$BackendCommand = Join-Path $BackendDir "mvnw.cmd"
if (-not (Test-Path $BackendCommand)) {
    throw "Could not find backend Maven wrapper at $BackendCommand"
}

if (Test-Path $BackendOutLog) {
    Remove-Item $BackendOutLog
}
if (Test-Path $BackendErrLog) {
    Remove-Item $BackendErrLog
}

$env:DB_URL = $DbUrl
$env:DB_USER = $DbUser
$env:DB_PASSWORD = $DbPassword

$BackendProcess = Start-Process `
    -FilePath $BackendCommand `
    -ArgumentList "spring-boot:run" `
    -WorkingDirectory $BackendDir `
    -RedirectStandardOutput $BackendOutLog `
    -RedirectStandardError $BackendErrLog `
    -PassThru

Write-LaunchLog "Waiting for backend port 8080..."
$BackendReady = $false
for ($i = 1; $i -le 30; $i++) {
    Start-Sleep -Seconds 2

    if ($BackendProcess.HasExited) {
        throw "Backend stopped before it opened port 8080. Check backend.out.log and backend.err.log in the project folder for the full error."
    }

    $Connection = Test-NetConnection -ComputerName "localhost" -Port 8080 -InformationLevel Quiet -WarningAction SilentlyContinue
    if ($Connection) {
        $BackendReady = $true
        break
    }
}

if (-not $BackendReady) {
    throw "Backend process is running, but port 8080 did not open. Check backend.out.log and backend.err.log in the project folder for startup details."
}

Write-LaunchLog "Preparing React frontend..."
Set-Location $FrontendDir
if (-not (Test-Path (Join-Path $FrontendDir "node_modules"))) {
    npm.cmd install
}

Write-LaunchLog "Starting React frontend on http://localhost:5173 ..."
$FrontendProcess = Start-Process `
    -FilePath "npm.cmd" `
    -ArgumentList "run", "dev" `
    -WorkingDirectory $FrontendDir `
    -PassThru

Write-LaunchLog "Launch complete."
Write-Host ""
Write-Host "Frontend: http://localhost:5173"
Write-Host "Backend:  http://localhost:8080"
Write-Host ""
Write-Host "Backend process id:  $($BackendProcess.Id)"
Write-Host "Frontend process id: $($FrontendProcess.Id)"
Write-Host ""
Write-Host "To stop them later, run:"
Write-Host "Stop-Process -Id $($BackendProcess.Id),$($FrontendProcess.Id)"
