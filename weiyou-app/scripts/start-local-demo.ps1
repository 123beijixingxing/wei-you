param(
    [int]$AppServerPort = 18080,
    [int]$ImGatewayPort = 18081,
    [int]$GatewayPort = 18091,
    [int]$FrontendPort = 18088,
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$appRoot = Split-Path $scriptDir -Parent
$backendRoot = Join-Path $appRoot "weiyou-backend"
$frontendRoot = Join-Path $appRoot "weiyou-frontend"
$runLogDir = Join-Path $appRoot "run-logs"

New-Item -ItemType Directory -Force -Path $runLogDir | Out-Null

function Write-Step {
    param([string]$Text)
    Write-Host "[local-demo] $Text" -ForegroundColor Cyan
}

function Stop-Port {
    param([int]$Port)
    try {
        $processIds = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction Stop | Select-Object -ExpandProperty OwningProcess -Unique
        foreach ($processId in $processIds) {
            Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
        }
    } catch {
    }
}

function Start-BackgroundCommand {
    param(
        [string]$Name,
        [string]$WorkingDirectory,
        [string]$CommandLine
    )

    $process = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", $CommandLine -WorkingDirectory $WorkingDirectory -WindowStyle Hidden -PassThru
    Set-Content -LiteralPath (Join-Path $runLogDir "$Name.pid") -Value $process.Id
    return $process.Id
}

if (-not $SkipBuild) {
    Write-Step "Packaging backend modules"
    & mvn -q -DskipTests -pl weiyou-boot/weiyou-app-server,weiyou-boot/weiyou-im-gateway,weiyou-boot/weiyou-gateway -am package
    if ($LASTEXITCODE -ne 0) {
        throw "Backend package failed"
    }

    if (-not (Test-Path (Join-Path $frontendRoot "node_modules"))) {
        Write-Step "Installing frontend dependencies"
        & npm install --legacy-peer-deps
        if ($LASTEXITCODE -ne 0) {
            throw "Frontend install failed"
        }
    }
}

Write-Step "Stopping old listeners"
@($AppServerPort, $ImGatewayPort, $GatewayPort, $FrontendPort) | ForEach-Object { Stop-Port $_ }

$appServerJar = Join-Path $backendRoot "weiyou-boot\weiyou-app-server\target\weiyou-app-server-0.1.0-SNAPSHOT.jar"
$imGatewayJar = Join-Path $backendRoot "weiyou-boot\weiyou-im-gateway\target\weiyou-im-gateway-0.1.0-SNAPSHOT.jar"
$gatewayJar = Join-Path $backendRoot "weiyou-boot\weiyou-gateway\target\weiyou-gateway-0.1.0-SNAPSHOT.jar"

if (-not (Test-Path $appServerJar) -or -not (Test-Path $imGatewayJar) -or -not (Test-Path $gatewayJar)) {
    throw "Packaged backend jars were not found. Run without -SkipBuild first."
}

Write-Step "Starting app-server on $AppServerPort"
$appServerLog = Join-Path $runLogDir "app-server-$AppServerPort.log"
$appServerCmd = "set SERVER_PORT=$AppServerPort&& set WEIYOU_REDIS_HOST=localhost&& set WEIYOU_REDIS_PORT=6379&& java -jar `"$appServerJar`" > `"$appServerLog`" 2>&1"
$appServerPid = Start-BackgroundCommand -Name "app-server-$AppServerPort" -WorkingDirectory $appRoot -CommandLine $appServerCmd

Write-Step "Starting im-gateway on $ImGatewayPort"
$imGatewayLog = Join-Path $runLogDir "im-gateway-$ImGatewayPort.log"
$imGatewayCmd = "set SERVER_PORT=$ImGatewayPort&& set WEIYOU_IM_REDIS_PUBSUB_ENABLED=false&& set MANAGEMENT_HEALTH_REDIS_ENABLED=false&& set MANAGEMENT_HEALTH_DB_ENABLED=false&& set WEIYOU_REDIS_HOST=localhost&& set WEIYOU_REDIS_PORT=6379&& java -jar `"$imGatewayJar`" > `"$imGatewayLog`" 2>&1"
$imGatewayPid = Start-BackgroundCommand -Name "im-gateway-$ImGatewayPort" -WorkingDirectory $appRoot -CommandLine $imGatewayCmd

Write-Step "Starting gateway on $GatewayPort"
$gatewayLog = Join-Path $runLogDir "gateway-$GatewayPort.log"
$gatewayCmd = "set SERVER_PORT=$GatewayPort&& set WEIYOU_APP_SERVER_URL=http://127.0.0.1:$AppServerPort&& set WEIYOU_IM_GATEWAY_URL=ws://127.0.0.1:$ImGatewayPort&& set WEIYOU_GATEWAY_RATE_LIMIT_ENABLED=false&& set MANAGEMENT_HEALTH_REDIS_ENABLED=false&& set WEIYOU_REDIS_HOST=localhost&& set WEIYOU_REDIS_PORT=6379&& java -jar `"$gatewayJar`" > `"$gatewayLog`" 2>&1"
$gatewayPid = Start-BackgroundCommand -Name "gateway-$GatewayPort" -WorkingDirectory $appRoot -CommandLine $gatewayCmd

Write-Step "Starting frontend dev server on $FrontendPort"
$frontendLog = Join-Path $runLogDir "frontend-$FrontendPort.log"
$frontendCmd = "set VITE_GATEWAY_TARGET=http://127.0.0.1:$GatewayPort&& npm run dev:h5 -- --host 0.0.0.0 --port $FrontendPort > `"$frontendLog`" 2>&1"
$frontendPid = Start-BackgroundCommand -Name "frontend-$FrontendPort" -WorkingDirectory $frontendRoot -CommandLine $frontendCmd

Start-Sleep -Seconds 10

Write-Host "";
Write-Host "Local demo started:" -ForegroundColor Green
Write-Host "- Frontend: http://127.0.0.1:$FrontendPort"
Write-Host "- Gateway: http://127.0.0.1:$GatewayPort"
Write-Host "- App API: http://127.0.0.1:$AppServerPort/api"
Write-Host "- IM WS: ws://127.0.0.1:$ImGatewayPort/ws/chat"
Write-Host "- PIDs: frontend=$frontendPid, gateway=$gatewayPid, app=$appServerPid, im=$imGatewayPid"
