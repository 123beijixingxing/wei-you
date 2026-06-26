param(
    [string]$GatewayBaseUrl = "http://localhost:8090",
    [string]$FrontendUrl = "http://localhost:8088",
    [string]$WsBaseUrl = "",
    [string]$Mobile = "13800000001",
    [string]$Password = "123456",
    [string]$DeviceId = "device-smoke-10001",
    [long]$ConversationId = 90001,
    [long]$TargetUserId = 10002,
    [long]$GroupId = 90002,
    [long]$SeedRedPacketId = 81001,
    [long]$OfficialId = 20001,
    [string]$MiniAppId = "miniapp-demo-001",
    [switch]$IncludeNegativeChecks,
    [switch]$IncludeRateLimitChecks
)

$ErrorActionPreference = "Stop"

$GatewayBaseUrl = $GatewayBaseUrl -replace "localhost", "127.0.0.1"
$FrontendUrl = $FrontendUrl -replace "localhost", "127.0.0.1"
if (-not $WsBaseUrl) {
    $WsBaseUrl = ("$GatewayBaseUrl" -replace "^http", "ws") + "/ws/chat"
} else {
    $WsBaseUrl = $WsBaseUrl -replace "localhost", "127.0.0.1"
}

function Write-Step {
    param([string]$Text)
    Write-Host "[smoke] $Text" -ForegroundColor Cyan
}

function Invoke-JsonRequest {
    param(
        [string]$Method,
        [string]$Url,
        [object]$Body,
        [string]$AccessToken
    )

    $headers = @{}
    if ($AccessToken) {
        $headers["Authorization"] = "Bearer $AccessToken"
    }

    if ($null -ne $Body) {
        $jsonBody = $Body | ConvertTo-Json -Depth 10
        return Invoke-RestMethod -Method $Method -Uri $Url -Headers $headers -Body $jsonBody -ContentType "application/json"
    }

    return Invoke-RestMethod -Method $Method -Uri $Url -Headers $headers
}

function Invoke-FileUpload {
    param(
        [string]$Url,
        [string]$FilePath,
        [string]$AccessToken,
        [string]$BizType = "moment"
    )

    $arguments = @(
        "-sS",
        "-X", "POST",
        $Url,
        "-H", "Authorization: Bearer $AccessToken",
        "-F", "bizType=$BizType",
        "-F", "file=@$FilePath;type=image/png"
    )
    $raw = & curl.exe @arguments
    if ($LASTEXITCODE -ne 0) {
        throw "file upload request failed"
    }
    return $raw | ConvertFrom-Json
}

function Invoke-HttpStatusCode {
    param(
        [string]$Method,
        [string]$Url,
        [object]$Body,
        [hashtable]$Headers
    )

    $arguments = @("-sS", "-o", "NUL", "-w", "%{http_code}", "-X", $Method, $Url)
    if ($Headers) {
        foreach ($entry in $Headers.GetEnumerator()) {
            $arguments += @("-H", "$($entry.Key): $($entry.Value)")
        }
    }
    if ($null -ne $Body) {
        $jsonBody = $Body | ConvertTo-Json -Depth 10 -Compress
        $arguments += @("-H", "Content-Type: application/json", "--data-raw", $jsonBody)
    }
    $statusCode = & curl.exe @arguments
    if ($LASTEXITCODE -ne 0) {
        throw "http status probe failed"
    }
    return [int]$statusCode
}

function Receive-WebSocketJson {
    param(
        [System.Net.WebSockets.ClientWebSocket]$Socket,
        [int]$TimeoutSeconds = 10
    )

    $buffer = New-Object byte[] 8192
    $segment = [ArraySegment[byte]]::new($buffer)
    $cts = [System.Threading.CancellationTokenSource]::new()
    $cts.CancelAfter([TimeSpan]::FromSeconds($TimeoutSeconds))
    $builder = New-Object System.Text.StringBuilder

    while ($true) {
        $result = $Socket.ReceiveAsync($segment, $cts.Token).GetAwaiter().GetResult()
        if ($result.MessageType -eq [System.Net.WebSockets.WebSocketMessageType]::Close) {
            throw "websocket closed unexpectedly"
        }
        $null = $builder.Append([System.Text.Encoding]::UTF8.GetString($buffer, 0, $result.Count))
        if ($result.EndOfMessage) {
            break
        }
    }

    return ($builder.ToString() | ConvertFrom-Json)
}

function Send-WebSocketJson {
    param(
        [System.Net.WebSockets.ClientWebSocket]$Socket,
        [object]$Payload
    )

    $json = $Payload | ConvertTo-Json -Depth 10 -Compress
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($json)
    $segment = [ArraySegment[byte]]::new($bytes)
    $Socket.SendAsync($segment, [System.Net.WebSockets.WebSocketMessageType]::Text, $true, [System.Threading.CancellationToken]::None).GetAwaiter().GetResult()
}

Write-Step "Checking frontend root"
$frontendResponse = Invoke-WebRequest -Uri $FrontendUrl -Method GET
if ($frontendResponse.StatusCode -ne 200) {
    throw "frontend root check failed"
}

Write-Step "Checking gateway health"
$gatewayHealth = Invoke-RestMethod -Uri "$GatewayBaseUrl/actuator/health" -Method GET
if ($gatewayHealth.status -ne "UP") {
    throw "gateway health check failed"
}

Write-Step "Logging in through gateway"
$login = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/auth/login/password" -Body @{
    mobile = $Mobile
    password = $Password
    deviceId = $DeviceId
}
if ($login.code -ne 0 -or -not $login.data.accessToken) {
    throw "login failed"
}

$accessToken = $login.data.accessToken

Write-Step "Fetching current profile"
$profile = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/user/profile/me" -AccessToken $accessToken
if ($profile.code -ne 0) {
    throw "profile check failed"
}

Write-Step "Fetching user qrcode"
$qrcode = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/user/qrcode/get?dynamic=false" -AccessToken $accessToken
if ($qrcode.code -ne 0 -or -not $qrcode.data.qrcodeUrl) {
    throw "qrcode check failed"
}

Write-Step "Updating user status"
$statusText = "smoke-status-$(Get-Date -Format 'HHmmss')"
$statusUpdate = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/user/status/update" -AccessToken $accessToken -Body @{
    statusCode = "smoke"
    statusText = $statusText
    expireAt = ""
}
if ($statusUpdate.code -ne 0) {
    throw "status update failed"
}

Write-Step "Re-checking user profile status"
$profileAfterStatus = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/user/profile/me" -AccessToken $accessToken
if ($profileAfterStatus.code -ne 0 -or $profileAfterStatus.data.statusText -ne $statusText) {
    throw "status readback failed"
}

Write-Step "Fetching user setting detail"
$settingDetail = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/user/setting/detail" -AccessToken $accessToken
if ($settingDetail.code -ne 0 -or $null -eq $settingDetail.data.messageNotification) {
    throw "setting detail check failed"
}

Write-Step "Updating user notification setting"
$updatedNotificationEnabled = -not [bool]$settingDetail.data.messageNotification
$settingUpdate = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/user/setting/update" -AccessToken $accessToken -Body @{
    messageNotification = $updatedNotificationEnabled
}
if ($settingUpdate.code -ne 0 -or $settingUpdate.data.messageNotification -ne $updatedNotificationEnabled) {
    throw "setting update check failed"
}

Write-Step "Re-checking user setting detail"
$settingDetailAfterUpdate = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/user/setting/detail" -AccessToken $accessToken
if ($settingDetailAfterUpdate.code -ne 0 -or $settingDetailAfterUpdate.data.messageNotification -ne $updatedNotificationEnabled) {
    throw "setting readback failed"
}

Write-Step "Updating privacy setting"
$updatedPhoneSearch = -not [bool]$settingDetailAfterUpdate.data.addByPhone
$privacyUpdate = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/user/setting/update" -AccessToken $accessToken -Body @{
    addByPhone = $updatedPhoneSearch
}
if ($privacyUpdate.code -ne 0 -or $privacyUpdate.data.addByPhone -ne $updatedPhoneSearch) {
    throw "privacy setting update failed"
}

Write-Step "Re-checking privacy setting"
$settingDetailAfterPrivacy = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/user/setting/detail" -AccessToken $accessToken
if ($settingDetailAfterPrivacy.code -ne 0 -or $settingDetailAfterPrivacy.data.addByPhone -ne $updatedPhoneSearch) {
    throw "privacy setting readback failed"
}

Write-Step "Fetching contact list"
$contactList = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/contact/list" -AccessToken $accessToken
if ($contactList.code -ne 0 -or -not $contactList.data.list -or $contactList.data.list.Count -lt 1) {
    throw "contact list check failed"
}

Write-Step "Searching contact candidates"
$contactSearch = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/contact/search?keyword=ada" -AccessToken $accessToken
if ($contactSearch.code -ne 0 -or -not $contactSearch.data -or $contactSearch.data.Count -lt 1) {
    throw "contact search check failed"
}
$friendTargetUserId = [long]$contactSearch.data[0].userId

Write-Step "Applying friend request"
$friendApply = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/contact/friend/apply" -AccessToken $accessToken -Body @{
    targetUserId = $friendTargetUserId
    remark = "smoke add friend"
    source = "smoke-check"
}
if ($friendApply.code -ne 0 -or -not $friendApply.data.requestId) {
    throw "friend apply check failed"
}

Write-Step "Fetching new friend list"
$friendRequestList = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/contact/friend/request/list" -AccessToken $accessToken
if ($friendRequestList.code -ne 0 -or -not $friendRequestList.data.list -or $friendRequestList.data.list.Count -lt 1) {
    throw "friend request list check failed"
}
$friendRequestId = [long]$friendApply.data.requestId
$friendRequestReadback = $false
foreach ($request in $friendRequestList.data.list) {
    if ($request.requestId -eq $friendRequestId) {
        $friendRequestReadback = $true
        break
    }
}
if (-not $friendRequestReadback) {
    throw "friend request readback failed"
}

Write-Step "Handling friend request"
$friendRequestHandle = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/contact/friend/request/handle" -AccessToken $accessToken -Body @{
    requestId = $friendRequestId
    action = "accept"
}
if ($friendRequestHandle.code -ne 0 -or $friendRequestHandle.data.status -ne 1) {
    throw "friend request handle check failed"
}

Write-Step "Fetching group list"
$groupList = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/group/my/list" -AccessToken $accessToken
if ($groupList.code -ne 0 -or -not $groupList.data -or $groupList.data.Count -lt 1) {
    throw "group list check failed"
}

Write-Step "Creating group through gateway"
$createdGroup = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/group/create" -AccessToken $accessToken -Body @{
    groupName = "smoke-group"
    memberIds = @(10011, 10012)
}
if ($createdGroup.code -ne 0 -or -not $createdGroup.data.groupId) {
    throw "group create check failed"
}

Write-Step "Fetching created group detail"
$groupDetail = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/group/detail?groupId=$($createdGroup.data.groupId)" -AccessToken $accessToken
if ($groupDetail.code -ne 0 -or $groupDetail.data.groupId -ne $createdGroup.data.groupId) {
    throw "group detail check failed"
}

Write-Step "Inviting group members"
$groupInvite = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/group/member/invite" -AccessToken $accessToken -Body @{
    groupId = $createdGroup.data.groupId
    memberIds = @(10013, 10014)
}
if ($groupInvite.code -ne 0 -or $groupInvite.data.memberCount -lt 5) {
    throw "group invite check failed"
}

Write-Step "Fetching device list"
$deviceList = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/auth/device/list" -AccessToken $accessToken
if ($deviceList.code -ne 0 -or -not $deviceList.data -or $deviceList.data.Count -lt 1) {
    throw "device list check failed"
}

Write-Step "Fetching discovery features"
$discovery = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/feature/discovery?cityCode=440300" -AccessToken $accessToken
if ($discovery.code -ne 0 -or -not $discovery.data -or $discovery.data.Count -lt 1) {
    throw "discovery feature check failed"
}

Write-Step "Fetching search suggestions"
$searchSuggest = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/search/suggest?keyword=weiyou" -AccessToken $accessToken
if ($searchSuggest.code -ne 0 -or -not $searchSuggest.data -or $searchSuggest.data.Count -lt 1) {
    throw "search suggest check failed"
}

Write-Step "Fetching search results"
$searchResult = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/search/global?keyword=weiyou&pageNo=1" -AccessToken $accessToken
if ($searchResult.code -ne 0 -or -not $searchResult.data.list -or $searchResult.data.list.Count -lt 1) {
    throw "search global check failed"
}

Write-Step "Resolving scan result"
$scanResolve = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/scan/resolve" -AccessToken $accessToken -Body @{
    scanCode = "user:10002"
    scene = "smoke-check"
}
if ($scanResolve.code -ne 0 -or -not $scanResolve.data.payload.routePath) {
    throw "scan resolve check failed"
}

Write-Step "Fetching workbench features"
$workbench = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/feature/workbench" -AccessToken $accessToken
if ($workbench.code -ne 0 -or -not $workbench.data -or $workbench.data.Count -lt 1) {
    throw "workbench feature check failed"
}
$workbenchHasOfficialList = $false
$workbenchHasMiniAppRecent = $false
$workbenchHasMiniAppFavorites = $false
foreach ($entry in $workbench.data) {
    if ($entry.routePath -eq "/pages/official/list") {
        $workbenchHasOfficialList = $true
    }
    if ($entry.routePath -eq "/pages/miniapp/recent") {
        $workbenchHasMiniAppRecent = $true
    }
    if ($entry.routePath -eq "/pages/miniapp/favorites") {
        $workbenchHasMiniAppFavorites = $true
    }
}
if (-not $workbenchHasOfficialList -or -not $workbenchHasMiniAppRecent -or -not $workbenchHasMiniAppFavorites) {
    throw "workbench route mapping check failed"
}

Write-Step "Fetching collection list"
$collectionList = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/collection/list" -AccessToken $accessToken
if ($collectionList.code -ne 0 -or $null -eq $collectionList.data.list) {
    throw "collection list check failed"
}

if ($collectionList.data.list.Count -gt 0) {
    Write-Step "Deleting one collection item"
    $deletedCollection = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/collection/delete" -AccessToken $accessToken -Body @{
        collectionId = $collectionList.data.list[0].collectionId
    }
    if ($deletedCollection.code -ne 0) {
        throw "collection delete check failed"
    }
}

Write-Step "Fetching card list"
$cardList = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/card/list" -AccessToken $accessToken
if ($cardList.code -ne 0 -or -not $cardList.data -or $cardList.data.Count -lt 1) {
    throw "card list check failed"
}

Write-Step "Fetching emoji store list"
$emojiList = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/emoji/store/list" -AccessToken $accessToken
if ($emojiList.code -ne 0 -or -not $emojiList.data -or $emojiList.data.Count -lt 1) {
    throw "emoji list check failed"
}

Write-Step "Downloading emoji package"
$emojiPackageId = $emojiList.data[0].packageId
$emojiDownload = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/emoji/package/download" -AccessToken $accessToken -Body @{
    packageId = $emojiPackageId
}
if ($emojiDownload.code -ne 0 -or -not $emojiDownload.data.downloaded) {
    throw "emoji download check failed"
}

Write-Step "Fetching notice list"
$noticeList = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/notice/list" -AccessToken $accessToken
if ($noticeList.code -ne 0 -or -not $noticeList.data.list -or $noticeList.data.list.Count -lt 1) {
    throw "notice list check failed"
}
$noticeId = [long]$noticeList.data.list[0].noticeId

Write-Step "Marking notice as read"
$noticeRead = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/notice/read" -AccessToken $accessToken -Body @{
    noticeId = $noticeId
}
if ($noticeRead.code -ne 0 -or $noticeRead.data.readStatus -ne 1) {
    throw "notice read check failed"
}

Write-Step "Re-checking notice read state"
$noticeListAfterRead = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/notice/list" -AccessToken $accessToken
$noticeReadback = $false
foreach ($notice in $noticeListAfterRead.data.list) {
    if ($notice.noticeId -eq $noticeId -and $notice.readStatus -eq 1) {
        $noticeReadback = $true
        break
    }
}
if (-not $noticeReadback) {
    throw "notice readback failed"
}

Write-Step "Fetching official account detail"
$officialDetail = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/official/account/detail?officialId=$OfficialId" -AccessToken $accessToken
if ($officialDetail.code -ne 0 -or $officialDetail.data.officialId -ne $OfficialId) {
    throw "official detail check failed"
}

Write-Step "Fetching official account list"
$officialList = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/official/account/list" -AccessToken $accessToken
if ($officialList.code -ne 0 -or -not $officialList.data -or $officialList.data.Count -lt 1) {
    throw "official list check failed"
}
$officialListContainsTarget = $false
foreach ($item in $officialList.data) {
    if ($item.officialId -eq $OfficialId) {
        $officialListContainsTarget = $true
        break
    }
}
if (-not $officialListContainsTarget) {
    throw "official list does not contain target account"
}

Write-Step "Following official account through gateway"
$officialFollow = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/official/account/follow" -AccessToken $accessToken -Body @{
    officialId = $OfficialId
    action = "follow"
}
if ($officialFollow.code -ne 0 -or -not $officialFollow.data.followed) {
    throw "official follow check failed"
}

Write-Step "Re-checking official account follow state"
$officialDetailAfterFollow = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/official/account/detail?officialId=$OfficialId" -AccessToken $accessToken
if ($officialDetailAfterFollow.code -ne 0 -or -not $officialDetailAfterFollow.data.followed) {
    throw "official follow state readback failed"
}

Write-Step "Fetching official article history"
$officialHistory = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/official/article/history?officialId=$OfficialId" -AccessToken $accessToken
if ($officialHistory.code -ne 0 -or -not $officialHistory.data.list -or $officialHistory.data.list.Count -lt 1) {
    throw "official history check failed"
}
$articleId = [long]$officialHistory.data.list[0].articleId

Write-Step "Fetching official article detail"
$officialArticle = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/official/article/detail?articleId=$articleId" -AccessToken $accessToken
if ($officialArticle.code -ne 0 -or $officialArticle.data.articleId -ne $articleId) {
    throw "official article detail check failed"
}

Write-Step "Liking official article through gateway"
$officialLike = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/official/article/like" -AccessToken $accessToken -Body @{
    articleId = $articleId
    action = "like"
}
if ($officialLike.code -ne 0 -or -not $officialLike.data.likeCount) {
    throw "official article like check failed"
}

Write-Step "Re-checking official article like count"
$officialArticleAfterLike = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/official/article/detail?articleId=$articleId" -AccessToken $accessToken
if ($officialArticleAfterLike.code -ne 0 -or $officialArticleAfterLike.data.likeCount -lt $officialLike.data.likeCount) {
    throw "official article like readback failed"
}

Write-Step "Undo official account follow through gateway"
$officialUnfollow = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/official/account/follow" -AccessToken $accessToken -Body @{
    officialId = $OfficialId
    action = "unfollow"
}
if ($officialUnfollow.code -ne 0 -or $officialUnfollow.data.followed) {
    throw "official unfollow check failed"
}

Write-Step "Re-checking official account unfollow state"
$officialDetailAfterUnfollow = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/official/account/detail?officialId=$OfficialId" -AccessToken $accessToken
if ($officialDetailAfterUnfollow.code -ne 0 -or $officialDetailAfterUnfollow.data.followed) {
    throw "official unfollow readback failed"
}

Write-Step "Undo official article like through gateway"
$officialUnlike = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/official/article/like" -AccessToken $accessToken -Body @{
    articleId = $articleId
    action = "unlike"
}
if ($officialUnlike.code -ne 0) {
    throw "official article unlike check failed"
}

Write-Step "Re-checking official article unlike count"
$officialArticleAfterUnlike = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/official/article/detail?articleId=$articleId" -AccessToken $accessToken
if ($officialArticleAfterUnlike.code -ne 0 -or $officialArticleAfterUnlike.data.likeCount -gt $officialArticleAfterLike.data.likeCount) {
    throw "official article unlike readback failed"
}

Write-Step "Fetching miniapp recent list"
$miniAppRecent = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/miniapp/recent/list" -AccessToken $accessToken
if ($miniAppRecent.code -ne 0 -or -not $miniAppRecent.data -or $miniAppRecent.data.Count -lt 1) {
    throw "miniapp recent check failed"
}

Write-Step "Opening miniapp session"
$miniAppOpen = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/miniapp/open" -AccessToken $accessToken -Body @{
    appId = $MiniAppId
    path = "/pages/index/index"
    scene = "smoke-check"
}
if ($miniAppOpen.code -ne 0 -or $miniAppOpen.data.appId -ne $MiniAppId) {
    throw "miniapp open check failed"
}

Write-Step "Favoriting miniapp through gateway"
$miniAppFavorite = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/miniapp/favorite/toggle" -AccessToken $accessToken -Body @{
    appId = $MiniAppId
    action = "favorite"
}
if ($miniAppFavorite.code -ne 0 -or -not $miniAppFavorite.data.favorite) {
    throw "miniapp favorite check failed"
}

Write-Step "Re-opening miniapp to verify favorite state"
$miniAppOpenAfterFavorite = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/miniapp/open" -AccessToken $accessToken -Body @{
    appId = $MiniAppId
    path = "/pages/index/index"
    scene = "smoke-check-favorite"
}
if ($miniAppOpenAfterFavorite.code -ne 0 -or -not $miniAppOpenAfterFavorite.data.favorite) {
    throw "miniapp favorite readback failed"
}

Write-Step "Fetching miniapp favorite list"
$miniAppFavoriteList = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/miniapp/favorite/list" -AccessToken $accessToken
if ($miniAppFavoriteList.code -ne 0 -or -not $miniAppFavoriteList.data -or $miniAppFavoriteList.data.Count -lt 1) {
    throw "miniapp favorite list check failed"
}
$favoriteListContainsTarget = $false
foreach ($item in $miniAppFavoriteList.data) {
    if ($item.appId -eq $MiniAppId -and $item.favorite) {
        $favoriteListContainsTarget = $true
        break
    }
}
if (-not $favoriteListContainsTarget) {
    throw "miniapp favorite list does not contain target app"
}

Write-Step "Undo miniapp favorite through gateway"
$miniAppUnfavorite = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/miniapp/favorite/toggle" -AccessToken $accessToken -Body @{
    appId = $MiniAppId
    action = "unfavorite"
}
if ($miniAppUnfavorite.code -ne 0 -or $miniAppUnfavorite.data.favorite) {
    throw "miniapp unfavorite check failed"
}

Write-Step "Re-checking miniapp favorite list after unfavorite"
$miniAppFavoriteListAfterUnfavorite = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/miniapp/favorite/list" -AccessToken $accessToken
$favoriteListStillContainsTarget = $false
foreach ($item in $miniAppFavoriteListAfterUnfavorite.data) {
    if ($item.appId -eq $MiniAppId) {
        $favoriteListStillContainsTarget = $true
        break
    }
}
if ($favoriteListStillContainsTarget) {
    throw "miniapp favorite list still contains unfavorited app"
}

Write-Step "Re-opening miniapp to verify unfavorite state"
$miniAppOpenAfterUnfavorite = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/miniapp/open" -AccessToken $accessToken -Body @{
    appId = $MiniAppId
    path = "/pages/index/index"
    scene = "smoke-check-unfavorite"
}
if ($miniAppOpenAfterUnfavorite.code -ne 0 -or $miniAppOpenAfterUnfavorite.data.favorite) {
    throw "miniapp unfavorite readback failed"
}

Write-Step "Removing miniapp recent item through gateway"
$miniAppRecentAfterRemove = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/miniapp/recent/remove" -AccessToken $accessToken -Body @{
    appId = "miniapp-demo-002"
}
if ($miniAppRecentAfterRemove.code -ne 0) {
    throw "miniapp recent remove check failed"
}
$removedMiniAppStillExists = $false
foreach ($item in $miniAppRecentAfterRemove.data) {
    if ($item.appId -eq "miniapp-demo-002") {
        $removedMiniAppStillExists = $true
        break
    }
}
if ($removedMiniAppStillExists) {
    throw "removed miniapp still exists in recent list"
}

Write-Step "Fetching conversation list"
$conversationList = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/chat/conversation/list?pageSize=20" -AccessToken $accessToken
if ($conversationList.code -ne 0) {
    throw "conversation list check failed"
}

Write-Step "Fetching wallet overview"
$walletOverview = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/wallet/overview" -AccessToken $accessToken
if ($walletOverview.code -ne 0) {
    throw "wallet overview check failed"
}

Write-Step "Uploading local image through gateway"
$tempImagePath = Join-Path ([System.IO.Path]::GetTempPath()) ("weiyou-smoke-" + [System.Guid]::NewGuid().ToString("N") + ".png")
$imageBytes = [Convert]::FromBase64String("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO2p8XQAAAAASUVORK5CYII=")
[System.IO.File]::WriteAllBytes($tempImagePath, $imageBytes)
$uploadedImage = Invoke-FileUpload -Url "$GatewayBaseUrl/api/media/upload/local" -FilePath $tempImagePath -AccessToken $accessToken -BizType "moment"
Remove-Item -LiteralPath $tempImagePath -ErrorAction SilentlyContinue
if ($uploadedImage.code -ne 0 -or -not $uploadedImage.data.url) {
    throw "local image upload failed"
}

Write-Step "Creating image moment through gateway"
$momentText = "smoke image moment $(Get-Date -Format 'yyyyMMddHHmmss')"
$createdMoment = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/moment/create" -AccessToken $accessToken -Body @{
    content = $momentText
    mediaUrls = @($uploadedImage.data.url)
    visibleScope = "public"
}
if ($createdMoment.code -ne 0 -or -not $createdMoment.data.momentId) {
    throw "moment create failed"
}

Write-Step "Checking moment timeline"
$momentTimeline = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/moment/timeline?pageSize=20" -AccessToken $accessToken
if ($momentTimeline.code -ne 0) {
    throw "moment timeline check failed"
}
$momentFound = $false
foreach ($item in $momentTimeline.data.list) {
    if ($item.content -eq $momentText) {
        if (-not $item.mediaList -or $item.mediaList.Count -lt 1) {
            throw "created moment media list missing"
        }
        $momentFound = $true
        break
    }
}
if (-not $momentFound) {
    throw "created moment not found in timeline"
}

Write-Step "Liking created moment through gateway"
$momentLike = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/moment/like" -AccessToken $accessToken -Body @{
    momentId = $createdMoment.data.momentId
    action = "like"
}
if ($momentLike.code -ne 0 -or -not $momentLike.data.likeCount) {
    throw "moment like check failed"
}

Write-Step "Undo moment like through gateway"
$momentUnlike = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/moment/like" -AccessToken $accessToken -Body @{
    momentId = $createdMoment.data.momentId
    action = "unlike"
}
if ($momentUnlike.code -ne 0) {
    throw "moment unlike check failed"
}

Write-Step "Re-checking moment unlike state"
$momentTimelineAfterUnlike = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/moment/timeline?pageSize=20" -AccessToken $accessToken
$momentUnlikeReadback = $false
foreach ($item in $momentTimelineAfterUnlike.data.list) {
    if ($item.momentId -eq $createdMoment.data.momentId -and $item.likeCount -le $momentLike.data.likeCount) {
        $momentUnlikeReadback = $true
        break
    }
}
if (-not $momentUnlikeReadback) {
    throw "moment unlike readback failed"
}

Write-Step "Commenting moment through gateway"
$momentComment = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/moment/comment/create" -AccessToken $accessToken -Body @{
    momentId = $createdMoment.data.momentId
    content = "smoke comment"
}
if ($momentComment.code -ne 0 -or -not $momentComment.data.commentCount) {
    throw "moment comment check failed"
}

Write-Step "Re-checking moment comment count"
$momentTimelineAfterComment = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/moment/timeline?pageSize=20" -AccessToken $accessToken
if ($momentTimelineAfterComment.code -ne 0) {
    throw "moment timeline recheck failed"
}
$commentReadbackPassed = $false
$commentContentReadback = $false
$commentId = $momentComment.data.commentId
foreach ($item in $momentTimelineAfterComment.data.list) {
    if ($item.momentId -eq $createdMoment.data.momentId) {
        if ($item.commentCount -ge $momentComment.data.commentCount) {
            foreach ($comment in $item.commentList) {
                if ($comment.commentId -eq $commentId) {
                    $commentReadbackPassed = $true
                    break
                }
            }
        }
        foreach ($comment in $item.commentList) {
            if ($comment.content -eq "smoke comment") {
                $commentContentReadback = $true
                break
            }
        }
    }
}
if (-not $commentReadbackPassed) {
    throw "moment comment readback failed"
}
if (-not $commentContentReadback) {
    throw "moment comment content readback failed"
}

Write-Step "Replying to moment comment through gateway"
$momentReply = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/moment/comment/create" -AccessToken $accessToken -Body @{
    momentId = $createdMoment.data.momentId
    content = "smoke reply"
    replyCommentId = $commentId
}
if ($momentReply.code -ne 0 -or -not $momentReply.data.commentId) {
    throw "moment reply check failed"
}

Write-Step "Re-checking moment reply content"
$momentTimelineAfterReply = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/moment/timeline?pageSize=20" -AccessToken $accessToken
$replyReadbackPassed = $false
foreach ($item in $momentTimelineAfterReply.data.list) {
    if ($item.momentId -eq $createdMoment.data.momentId) {
        foreach ($comment in $item.commentList) {
            if ($comment.commentId -eq $momentReply.data.commentId -and $comment.replyCommentId -eq $commentId) {
                $replyReadbackPassed = $true
                break
            }
        }
    }
}
if (-not $replyReadbackPassed) {
    throw "moment reply readback failed"
}

Write-Step "Deleting reply comment through gateway"
$deleteReply = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/moment/comment/delete" -AccessToken $accessToken -Body @{
    momentId = $createdMoment.data.momentId
    commentId = $momentReply.data.commentId
}
if ($deleteReply.code -ne 0) {
    throw "moment reply delete check failed"
}

Write-Step "Re-checking reply comment deletion"
$momentTimelineAfterDelete = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/moment/timeline?pageSize=20" -AccessToken $accessToken
$replyDeleteReadback = $true
foreach ($item in $momentTimelineAfterDelete.data.list) {
    if ($item.momentId -eq $createdMoment.data.momentId) {
        foreach ($comment in $item.commentList) {
            if ($comment.commentId -eq $momentReply.data.commentId) {
                $replyDeleteReadback = $false
                break
            }
        }
    }
}
if (-not $replyDeleteReadback) {
    throw "moment reply delete readback failed"
}

Write-Step "Creating transfer through gateway"
$transfer = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/wallet/transfer/create" -AccessToken $accessToken -Body @{
    targetUserId = $TargetUserId
    amountFen = 100
    remark = "smoke transfer"
}
if ($transfer.code -ne 0 -or -not $transfer.data.transactionNo) {
    throw "transfer create failed"
}

Write-Step "Checking wallet bills"
$walletBills = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/wallet/bill/list?pageNo=1" -AccessToken $accessToken
if ($walletBills.code -ne 0) {
    throw "wallet bill check failed"
}
$transferFound = $false
foreach ($bill in $walletBills.data.list) {
    if ($bill.transactionNo -eq $transfer.data.transactionNo) {
        $transferFound = $true
        break
    }
}
if (-not $transferFound) {
    throw "created transfer not found in wallet bills"
}

Write-Step "Creating red packet through gateway"
$createdRedPacket = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/wallet/red-packet/create" -AccessToken $accessToken -Body @{
    amountFen = 300
    count = 3
    type = 2
    greeting = "smoke red packet"
    groupId = $GroupId
}
if ($createdRedPacket.code -ne 0 -or -not $createdRedPacket.data.redPacketId) {
    throw "red packet create failed"
}

Write-Step "Opening seeded red packet through gateway"
$openedSeedRedPacket = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/wallet/red-packet/open" -AccessToken $accessToken -Body @{
    redPacketId = $SeedRedPacketId
}
if ($openedSeedRedPacket.code -ne 0 -or -not $openedSeedRedPacket.data.redPacketNo) {
    throw "seed red packet open failed"
}

Write-Step "Opening newly created red packet through gateway"
$openedNewRedPacket = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/wallet/red-packet/open" -AccessToken $accessToken -Body @{
    redPacketId = $createdRedPacket.data.redPacketId
}
if ($openedNewRedPacket.code -ne 0 -or -not $openedNewRedPacket.data.redPacketNo) {
    throw "new red packet open failed"
}
if ($openedNewRedPacket.data.redPacketNo -ne $createdRedPacket.data.redPacketNo) {
    throw "opened red packet does not match created red packet"
}

Write-Step "Opening websocket through gateway"
$wsUrl = "{0}?token={1}&deviceId={2}" -f $WsBaseUrl, [Uri]::EscapeDataString($accessToken), [Uri]::EscapeDataString($DeviceId)
$wsResult = & java ".\scripts\WsSmokeCheck.java" $wsUrl $ConversationId
if ($LASTEXITCODE -ne 0 -or (($wsResult -join "") -notmatch "WS OK|WS ACK ONLY")) {
    throw "websocket smoke check failed"
}

Write-Step "Offlining secondary device through gateway"
$targetOfflineDeviceId = $null
$deviceOfflineReadback = $null
foreach ($device in $deviceList.data) {
    if ($device.deviceId -ne $DeviceId) {
        $targetOfflineDeviceId = $device.deviceId
        break
    }
}
if ($targetOfflineDeviceId) {
    $offlineResult = Invoke-JsonRequest -Method POST -Url "$GatewayBaseUrl/api/auth/device/offline" -AccessToken $accessToken -Body @{
        deviceId = $targetOfflineDeviceId
    }
    if ($offlineResult.code -ne 0) {
        throw "device offline check failed"
    }

    Write-Step "Re-checking device offline state"
    $deviceListAfterOffline = Invoke-JsonRequest -Method GET -Url "$GatewayBaseUrl/api/auth/device/list" -AccessToken $accessToken
    $deviceOfflineReadback = $false
    foreach ($device in $deviceListAfterOffline.data) {
        if ($device.deviceId -eq $targetOfflineDeviceId -and $device.online -eq $false) {
            $deviceOfflineReadback = $true
            break
        }
    }
    if (-not $deviceOfflineReadback) {
        throw "device offline readback failed"
    }
}

if ($IncludeNegativeChecks) {
    Write-Step "Running negative auth checks"
    $protectedStatus = Invoke-HttpStatusCode -Method "GET" -Url "$GatewayBaseUrl/api/user/profile/me"
    if ($protectedStatus -ne 401) {
        throw "expected protected API to return 401, got $protectedStatus"
    }

    $wsRejected = $false
    $unauthorizedSocket = [System.Net.WebSockets.ClientWebSocket]::new()
    try {
        $unauthorizedUri = [Uri]::new(("$GatewayBaseUrl" -replace "^http", "ws") + "/ws/chat?deviceId=$([Uri]::EscapeDataString($DeviceId))")
        $unauthorizedSocket.ConnectAsync($unauthorizedUri, [System.Threading.CancellationToken]::None).GetAwaiter().GetResult()
    } catch {
        $wsRejected = $true
    } finally {
        $unauthorizedSocket.Dispose()
    }
    if (-not $wsRejected) {
        throw "expected websocket without token to be rejected"
    }
}

if ($IncludeRateLimitChecks) {
    Write-Step "Running wallet rate limit checks through gateway"
    $rateLimited = $false
    $rateLimitStatus = 0
    for ($i = 1; $i -le 45; $i++) {
        $rateLimitStatus = Invoke-HttpStatusCode -Method "GET" -Url "$GatewayBaseUrl/api/wallet/overview" -Headers @{
            Authorization = "Bearer $accessToken"
        }
        if ($rateLimitStatus -eq 429) {
            $rateLimited = $true
            break
        }
    }
    if (-not $rateLimited) {
        throw "expected gateway rate limiting to return 429 for wallet overview"
    }
}

Write-Step "Smoke check passed"
Write-Host (ConvertTo-Json @{
    frontend = $FrontendUrl
    gateway = $GatewayBaseUrl
    userId = $login.data.userId
    qrcodeUrl = $qrcode.data.qrcodeUrl
    profileStatusReadback = $profileAfterStatus.data.statusText
    notificationSettingReadback = $settingDetailAfterUpdate.data.messageNotification
    privacyPhoneSearchReadback = $settingDetailAfterPrivacy.data.addByPhone
    contactCount = ($contactList.data.list | Measure-Object).Count
    contactSearchCount = ($contactSearch.data | Measure-Object).Count
    friendRequestReadback = $friendRequestReadback
    friendRequestHandledStatus = $friendRequestHandle.data.status
    groupCount = ($groupList.data | Measure-Object).Count
    createdGroupId = $createdGroup.data.groupId
    createdGroupMemberCount = $groupInvite.data.memberCount
    deviceCount = ($deviceList.data | Measure-Object).Count
    deviceOfflineReadback = $deviceOfflineReadback
    discoveryCount = ($discovery.data | Measure-Object).Count
    searchSuggestCount = ($searchSuggest.data | Measure-Object).Count
    searchResultCount = ($searchResult.data.list | Measure-Object).Count
    scanActionType = $scanResolve.data.actionType
    workbenchCount = ($workbench.data | Measure-Object).Count
    workbenchOfficialList = $workbenchHasOfficialList
    workbenchMiniAppRecent = $workbenchHasMiniAppRecent
    workbenchMiniAppFavorites = $workbenchHasMiniAppFavorites
    collectionCount = ($collectionList.data.list | Measure-Object).Count
    cardCount = ($cardList.data | Measure-Object).Count
    emojiCount = ($emojiList.data | Measure-Object).Count
    emojiDownloaded = $emojiDownload.data.downloaded
    noticeCount = ($noticeList.data.list | Measure-Object).Count
    noticeReadback = $noticeReadback
    officialId = $officialDetail.data.officialId
    officialListCount = ($officialList.data | Measure-Object).Count
    officialFollowed = $officialFollow.data.followed
    officialFollowedReadback = $officialDetailAfterFollow.data.followed
    officialUnfollowedReadback = -not $officialDetailAfterUnfollow.data.followed
    officialArticleId = $officialArticle.data.articleId
    officialArticleLikeCount = $officialLike.data.likeCount
    officialArticleLikeReadback = $officialArticleAfterLike.data.likeCount
    officialArticleUnlikeReadback = $officialArticleAfterUnlike.data.likeCount
    miniAppId = $miniAppOpen.data.appId
    miniAppFavorite = $miniAppFavorite.data.favorite
    miniAppFavoriteReadback = $miniAppOpenAfterFavorite.data.favorite
    miniAppUnfavoriteReadback = -not $miniAppOpenAfterUnfavorite.data.favorite
    miniAppFavoriteListCount = ($miniAppFavoriteList.data | Measure-Object).Count
    miniAppFavoriteListAfterUnfavorite = -not $favoriteListStillContainsTarget
    conversationCount = ($conversationList.data.list | Measure-Object).Count
    walletBalanceFen = $walletOverview.data.availableBalanceFen
    uploadedImageUrl = $uploadedImage.data.url
    createdMomentId = $createdMoment.data.momentId
    momentLikeCount = $momentLike.data.likeCount
    momentUnlikeReadback = $momentUnlikeReadback
    momentCommentCount = $momentComment.data.commentCount
    momentCommentReadback = $commentReadbackPassed
    momentCommentContentReadback = $commentContentReadback
    momentReplyReadback = $replyReadbackPassed
    momentReplyDeleteReadback = $replyDeleteReadback
    transferTransactionNo = $transfer.data.transactionNo
    createdRedPacketNo = $createdRedPacket.data.redPacketNo
    openedSeedRedPacketNo = $openedSeedRedPacket.data.redPacketNo
    openedNewRedPacketNo = $openedNewRedPacket.data.redPacketNo
    websocketResult = ($wsResult -join "")
    negativeChecks = [bool]$IncludeNegativeChecks
    rateLimitChecks = [bool]$IncludeRateLimitChecks
} -Depth 10)
