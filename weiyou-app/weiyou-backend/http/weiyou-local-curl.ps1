param(
    [string]$BaseUrl = "http://localhost:8080/api",
    [string]$Mobile = "13800000001",
    [string]$Password = "123456",
    [string]$DeviceId = "device-demo-android-10001",
    [long]$ConversationId = 90001,
    [long]$TargetUserId = 10002,
    [long]$SeededRedPacketId = 81001,
    [switch]$IncludeWrites
)

$ErrorActionPreference = "Stop"

function Invoke-WeiyouRequest {
    param(
        [string]$Method,
        [string]$Path,
        [object]$Body,
        [string]$AccessToken
    )

    $url = if ($Path.StartsWith("http")) { $Path } else { "$BaseUrl$Path" }
    $arguments = @("-sS", "-X", $Method, $url, "-H", "Accept: application/json")

    if ($AccessToken) {
        $arguments += @("-H", "Authorization: Bearer $AccessToken")
    }

    if ($null -ne $Body) {
        $jsonBody = $Body | ConvertTo-Json -Depth 10 -Compress
        $arguments += @("-H", "Content-Type: application/json", "--data-raw", $jsonBody)
    }

    $raw = & curl.exe @arguments
    if ($LASTEXITCODE -ne 0) {
        throw "curl request failed: $Method $url"
    }

    try {
        return $raw | ConvertFrom-Json -Depth 20
    } catch {
        return $raw
    }
}

function Show-Section {
    param(
        [string]$Title,
        [object]$Payload
    )

    Write-Host ""
    Write-Host "=== $Title ===" -ForegroundColor Cyan
    if ($Payload -is [string]) {
        Write-Host $Payload
        return
    }
    $Payload | ConvertTo-Json -Depth 20
}

Write-Host "Weiyou local curl script starting..." -ForegroundColor Green
Write-Host "Base URL: $BaseUrl"
Write-Host "Login mobile: $Mobile"

$bootstrap = Invoke-WeiyouRequest -Method "GET" -Path "/app/bootstrap?clientType=app&version=0.1.0"
Show-Section -Title "Bootstrap" -Payload $bootstrap

$login = Invoke-WeiyouRequest -Method "POST" -Path "/auth/login/password" -Body @{
    mobile = $Mobile
    password = $Password
    deviceId = $DeviceId
}
Show-Section -Title "Password Login" -Payload $login

$accessToken = $login.data.accessToken
$refreshToken = $login.data.refreshToken

if ([string]::IsNullOrWhiteSpace($accessToken)) {
    throw "login did not return accessToken"
}

Write-Host ""
Write-Host "Access token acquired." -ForegroundColor Green
Write-Host "Refresh token acquired." -ForegroundColor Green

$refresh = Invoke-WeiyouRequest -Method "POST" -Path "/auth/token/refresh" -Body @{
    refreshToken = $refreshToken
}
Show-Section -Title "Refresh Token" -Payload $refresh

$profile = Invoke-WeiyouRequest -Method "GET" -Path "/user/profile/me" -AccessToken $accessToken
Show-Section -Title "Current Profile" -Payload $profile

$devices = Invoke-WeiyouRequest -Method "GET" -Path "/auth/device/list" -AccessToken $accessToken
Show-Section -Title "Device List" -Payload $devices

$conversations = Invoke-WeiyouRequest -Method "GET" -Path "/chat/conversation/list?pageSize=20" -AccessToken $accessToken
Show-Section -Title "Conversation List" -Payload $conversations

$messages = Invoke-WeiyouRequest -Method "GET" -Path "/chat/message/history?conversationId=$ConversationId&pageSize=20" -AccessToken $accessToken
Show-Section -Title "Message History" -Payload $messages

$walletOverview = Invoke-WeiyouRequest -Method "GET" -Path "/wallet/overview" -AccessToken $accessToken
Show-Section -Title "Wallet Overview" -Payload $walletOverview

$walletBills = Invoke-WeiyouRequest -Method "GET" -Path "/wallet/bill/list?pageNo=1" -AccessToken $accessToken
Show-Section -Title "Wallet Bills" -Payload $walletBills

if ($IncludeWrites) {
    $sendMessage = Invoke-WeiyouRequest -Method "POST" -Path "/chat/message/send" -AccessToken $accessToken -Body @{
        conversationId = $ConversationId
        msgType = 1
        clientMsgId = "cmsg-local-ps1-001"
        content = @{ text = "hello from PowerShell curl script" }
    }
    Show-Section -Title "Send Message" -Payload $sendMessage

    $transfer = Invoke-WeiyouRequest -Method "POST" -Path "/wallet/transfer/create" -AccessToken $accessToken -Body @{
        targetUserId = $TargetUserId
        amountFen = 100
        remark = "demo transfer from ps1"
    }
    Show-Section -Title "Transfer Create" -Payload $transfer

    $createRedPacket = Invoke-WeiyouRequest -Method "POST" -Path "/wallet/red-packet/create" -AccessToken $accessToken -Body @{
        amountFen = 300
        count = 3
        type = 2
        greeting = "good luck"
        groupId = 90002
    }
    Show-Section -Title "Create Red Packet" -Payload $createRedPacket

    $openSeededRedPacket = Invoke-WeiyouRequest -Method "POST" -Path "/wallet/red-packet/open" -AccessToken $accessToken -Body @{
        redPacketId = $SeededRedPacketId
    }
    Show-Section -Title "Open Seeded Red Packet" -Payload $openSeededRedPacket
} else {
    Write-Host ""
    Write-Host "Write operations skipped. Re-run with -IncludeWrites to test chat send, transfer and red packet." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "WebSocket example:" -ForegroundColor Green
Write-Host "ws://localhost:8081/ws/chat?token=$accessToken&deviceId=$DeviceId"
Write-Host '{"event":"MESSAGE_SEND","requestId":"ws-msg-001","data":{"conversationId":90001,"msgType":1,"content":{"text":"hello ws"}}}'
