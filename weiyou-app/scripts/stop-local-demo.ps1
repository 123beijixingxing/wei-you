param(
    [int]$AppServerPort = 18080,
    [int]$ImGatewayPort = 18081,
    [int]$GatewayPort = 18091,
    [int]$FrontendPort = 18088
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$appRoot = Split-Path $scriptDir -Parent
$runLogDir = Join-Path $appRoot "run-logs"

function Write-Step {
    param([string]$Text)
    Write-Host "[local-demo] $Text" -ForegroundColor Cyan
}

function Stop-ByPidFile {
    param([string]$Name)
    $pidFile = Join-Path $runLogDir "$Name.pid"
    if (-not (Test-Path $pidFile)) {
        return
    }
    $processId = Get-Content -LiteralPath $pidFile -ErrorAction SilentlyContinue
    if ($processId) {
        Stop-Process -Id ([int]$processId) -Force -ErrorAction SilentlyContinue
    }
    Remove-Item -LiteralPath $pidFile -ErrorAction SilentlyContinue
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

Write-Step "Stopping local demo processes"
Stop-ByPidFile "frontend-$FrontendPort"
Stop-ByPidFile "gateway-$GatewayPort"
Stop-ByPidFile "app-server-$AppServerPort"
Stop-ByPidFile "im-gateway-$ImGatewayPort"

@($FrontendPort, $GatewayPort, $AppServerPort, $ImGatewayPort) | ForEach-Object { Stop-Port $_ }

Write-Host "Local demo stopped." -ForegroundColor Green
