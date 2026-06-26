# 微友App 前端联调字段说明

## 1.文档说明

本文档是 `docs/接口文档/微友App-后端接口文档.md` 的联调补充版，重点说明：

- 前端请求时各字段该怎么传
- 后端 `data` 中各字段代表什么
- 哪些字段是 demo 值、预留值或当前未完全生效
- HTTP 接口与 WebSocket 实时消息如何组合使用

说明来源：

- `docs/需求设计文档/微友开发需求文档.md`
- 当前后端控制器、服务实现与统一异常处理代码

## 2.全局联调约定

### 2.1 地址

| 类型 | 地址 | 说明 |
| ---- | ---- | ---- |
| HTTP | `http://{host}:8080/api` | 常规业务接口 |
| WebSocket | `ws://{host}:8081/ws/chat` | IM 实时消息通道 |

### 2.2 鉴权

| 项 | 说明 |
| --- | ---- |
| HTTP 鉴权方式 | `Authorization: Bearer {accessToken}` |
| WebSocket 鉴权方式 | query 参数传 `token={accessToken}` |
| 匿名接口 | 登录、注册、发短信、刷新 token、App 启动配置 |

### 2.3 统一响应

```json
{
  "code": 0,
  "message": "ok",
  "traceId": "trace-id",
  "data": {}
}
```

字段说明：

| 字段 | 类型 | 说明 | 前端处理建议 |
| ---- | ---- | ---- | ------------ |
| code | Integer | 业务码，`0` 表示成功 | 不为 `0` 时按失败处理 |
| message | String | 提示信息 | toast 或错误弹窗可直接展示 |
| traceId | String | 请求追踪 ID | 便于排查问题 |
| data | Object/Array/null | 业务数据 | 按接口定义解析 |

### 2.4 分页响应

```json
{
  "list": [],
  "pageNo": 1,
  "pageSize": 20,
  "total": 0,
  "hasMore": false,
  "nextCursor": null
}
```

字段说明：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| list | Array | 当前页数据 |
| pageNo | Integer | 当前页码 |
| pageSize | Integer | 每页数量 |
| total | Long | 总记录数 |
| hasMore | Boolean | 是否还有更多 |
| nextCursor | String/null | 游标分页扩展位，当前多数接口未实际使用 |

### 2.5 时间与 ID

| 项 | 说明 |
| --- | ---- |
| 时间字段 | 当前多数为 ISO-8601 字符串 |
| Long 型 ID | 前端若使用 JavaScript，建议统一按字符串安全处理展示层 ID |
| clientMsgId | 客户端自生成消息 ID，便于发送中、重试、回执匹配 |

## 3.模块字段说明

### 3.1 认证与账号

#### 3.1.1 接口清单

| 接口 | Method | Uri | 鉴权 | 说明 |
| ---- | ------ | --- | ---- | ---- |
| 密码登录 | POST | `/api/auth/login/password` | 否 | 标准密码登录 |
| 短信验证码登录 | POST | `/api/auth/login/sms` | 否 | 当前短信码未真正校验 |
| 注册 | POST | `/api/auth/register` | 否 | 当前已存在手机号会直接返回旧账号 |
| 发送短信验证码 | POST | `/api/auth/sms/send` | 否 | 当前 no-op |
| 刷新 token | POST | `/api/auth/token/refresh` | 否 | 仅接受 refresh token |
| 退出登录 | POST | `/api/auth/logout` | 是 | 按 `deviceId` 下线 |
| 查询登录设备列表 | GET | `/api/auth/device/list` | 是 | 空库时返回 demo 数据 |
| 下线指定设备 | POST | `/api/auth/device/offline` | 是 | 当前未校验设备归属 |

#### 3.1.2 LoginTokenData

适用接口：密码登录、短信登录、注册、刷新 token。

| 字段 | 类型 | 说明 | 备注 |
| ---- | ---- | ---- | ---- |
| userId | Long | 用户 ID | 当前用户主键 |
| accessToken | String | 访问令牌 | 用于后续 HTTP / WebSocket 鉴权 |
| refreshToken | String | 刷新令牌 | 用于续期 |
| expireIn | Integer | accessToken 有效期秒数 | 当前固定 `7200` |
| needDeviceVerify | Boolean | 是否需要设备二次校验 | 当前固定 `false` |

#### 3.1.3 PasswordLoginRequest

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| mobile | String | 是 | 手机号 |
| password | String | 是 | 明文密码，后端内部做密码校验 |
| deviceId | String | 是 | 设备唯一标识 |

#### 3.1.4 SmsLoginRequest

| 字段 | 类型 | 必填 | 说明 | 备注 |
| ---- | ---- | ---- | ---- | ---- |
| mobile | String | 是 | 手机号 |  |
| smsCode | String | 是 | 短信验证码 | 当前未真正校验 |
| deviceId | String | 是 | 设备唯一标识 |  |

#### 3.1.5 RegisterRequest

| 字段 | 类型 | 必填 | 说明 | 备注 |
| ---- | ---- | ---- | ---- | ---- |
| mobile | String | 是 | 手机号 |  |
| smsCode | String | 是 | 短信验证码 | 当前未真正校验 |
| password | String | 是 | 登录密码 |  |
| deviceId | String | 否 | 设备唯一标识 | 为空时后端自动生成 |

#### 3.1.6 SmsSendRequest

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| mobile | String | 是 | 手机号 |
| scene | String | 是 | 发送场景，如登录、注册 |

#### 3.1.7 RefreshTokenRequest

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| refreshToken | String | 是 | 刷新令牌 |

#### 3.1.8 LogoutRequest / DeviceOfflineRequest

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| deviceId | String | 是 | 目标设备 ID |

#### 3.1.9 DeviceSession

适用接口：查询登录设备列表。

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| deviceId | String | 设备 ID |
| deviceType | String | 设备类型，如 `android`、`web` |
| deviceModel | String | 设备型号 |
| systemVersion | String | 系统版本 |
| loginCity | String | 登录城市 |
| loginIp | String | 登录 IP |
| lastLoginAt | String | 最近登录时间 |
| online | Boolean | 是否在线 |

#### 3.1.10 前端联调提示

1. `smsCode` 当前是占位字段，联调时可先按固定输入处理。
2. 注册接口当前不会对“手机号已存在”报错，而是直接返回已有账号登录态。
3. 登录成功后建议本地同时持久化：`userId`、`accessToken`、`refreshToken`、`deviceId`。

### 3.2 用户资料

#### 3.2.1 接口清单

| 接口 | Method | Uri | 鉴权 | 说明 |
| ---- | ------ | --- | ---- | ---- |
| 查询我的资料 | GET | `/api/user/profile/me` | 是 | 当前用户资料 |
| 查询用户资料详情 | GET | `/api/user/profile/detail` | 是 | `userId` 为空时查自己 |
| 更新资料 | POST | `/api/user/profile/update` | 是 | 资料局部更新 |
| 获取个人二维码 | GET | `/api/user/qrcode/get` | 是 | 支持动态码参数 |
| 更新状态 | POST | `/api/user/status/update` | 是 | `expireAt` 当前未落库 |

#### 3.2.2 UserProfileData

| 字段 | 类型 | 说明 | 备注 |
| ---- | ---- | ---- | ---- |
| userId | Long | 用户 ID |  |
| weiyouNo | String | 微友号 | 当前由后端拼装，如 `weiyou_10001` |
| nickname | String | 昵称 |  |
| avatar | String | 头像 URL |  |
| gender | Integer | 性别值 | 当前后端未给出字典说明 |
| city | String | 城市 | 由城市名或省份名回填 |
| signature | String | 个性签名 |  |
| statusText | String | 状态文案 | 如“开会中” |
| qrcodeUrl | String | 二维码图片地址 |  |

#### 3.2.3 UpdateProfileRequest

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| nickname | String | 否 | 昵称 |
| avatar | String | 否 | 头像 URL |
| gender | Integer | 否 | 性别 |
| city | String | 否 | 城市 |
| signature | String | 否 | 个性签名 |

#### 3.2.4 QrcodeData

| 字段 | 类型 | 说明 | 备注 |
| ---- | ---- | ---- | ---- |
| ticket | String | 二维码票据 | 可作为缓存键或场景标识 |
| qrcodeUrl | String | 二维码图片 URL |  |
| dynamic | Boolean | 是否动态二维码 | 由 query 参数决定 |
| expireAt | String | 过期时间 | 当前固定示例值 |

#### 3.2.5 UpdateStatusRequest

| 字段 | 类型 | 必填 | 说明 | 备注 |
| ---- | ---- | ---- | ---- | ---- |
| statusCode | String | 是 | 状态编码 | 如 `BUSY` |
| statusText | String | 否 | 状态文案 | 如“开会中” |
| expireAt | String | 否 | 状态过期时间 | 当前未实际生效 |

### 3.3 通讯录与好友

#### 3.3.1 接口清单

| 接口 | Method | Uri | 鉴权 | 说明 |
| ---- | ------ | --- | ---- | ---- |
| 查询通讯录列表 | GET | `/api/contact/list` | 是 | 当前 demo 数据 |
| 发起好友申请 | POST | `/api/contact/friend/apply` | 是 | 当前 no-op |
| 查询好友申请列表 | GET | `/api/contact/friend/request/list` | 是 | 当前 demo 数据 |
| 处理好友申请 | POST | `/api/contact/friend/request/handle` | 是 | 当前 no-op |

#### 3.3.2 ContactItem

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| userId | Long | 联系人用户 ID |
| nickname | String | 昵称 |
| remark | String | 备注名 |
| avatar | String | 头像 URL |
| letter | String | 索引首字母 |
| star | Boolean | 是否星标联系人 |

#### 3.3.3 FriendApplyRequest

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| targetUserId | Long | 是 | 目标用户 ID |
| remark | String | 否 | 申请备注 |
| source | String | 否 | 来源场景，如搜索、群聊 |

#### 3.3.4 FriendRequestItem

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| requestId | Long | 好友申请 ID |
| fromUserId | Long | 申请发起人 ID |
| nickname | String | 申请人昵称 |
| avatar | String | 申请人头像 |
| applyMessage | String | 申请文案 |
| status | Integer | 申请状态 |
| createdAt | String | 申请时间 |

#### 3.3.5 HandleFriendRequest

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| requestId | Long | 是 | 申请记录 ID |
| action | String | 是 | 处理动作 |

### 3.4 会话与消息

#### 3.4.1 接口清单

| 接口 | Method | Uri | 鉴权 | 说明 |
| ---- | ------ | --- | ---- | ---- |
| 查询会话列表 | GET | `/api/chat/conversation/list` | 是 | 优先查库，空则 demo |
| 查询聊天记录 | GET | `/api/chat/message/history` | 是 | 历史消息建议倒序拉取再正序渲染 |
| 发送消息 | POST | `/api/chat/message/send` | 是 | 会话不存在时自动建单聊 |
| 标记已读 | POST | `/api/chat/message/read` | 是 | 清空当前会话未读 |

#### 3.4.2 ConversationItem

| 字段 | 类型 | 说明 | 备注 |
| ---- | ---- | ---- | ---- |
| conversationId | Long | 会话 ID |  |
| conversationType | Integer | 会话类型 | `1` 单聊，`2` 群聊 |
| title | String | 会话标题 |  |
| avatar | String | 会话头像 |  |
| unreadCount | Integer | 未读数 |  |
| top | Boolean | 是否置顶 |  |
| mute | Boolean | 是否免打扰 |  |
| lastMessageDigest | String | 最后一条消息摘要 | 文本消息可直接展示 |
| lastMessageTime | String | 最后一条消息时间 |  |

#### 3.4.3 MessageItem

| 字段 | 类型 | 说明 | 备注 |
| ---- | ---- | ---- | ---- |
| messageId | Long | 消息 ID |  |
| clientMsgId | String | 客户端消息 ID | 便于消息状态对账 |
| conversationId | Long | 会话 ID |  |
| senderUserId | Long | 发送者用户 ID |  |
| msgType | Integer | 消息类型 | 当前明确使用 `1` 文本 |
| content | Object | 消息体 | 查库时可能是 `{raw: "json字符串"}` |
| sendStatus | Integer | 发送状态 | 当前成功一般为 `1` |
| sendTime | String | 发送时间 |  |
| readStatus | Integer | 已读状态 | 示例中 `0/1` |

#### 3.4.4 SendMessageRequest

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| conversationId | Long | 是 | 会话 ID |
| msgType | Integer | 是 | 消息类型 |
| clientMsgId | String | 是 | 客户端自定义消息 ID |
| content | Object | 是 | 消息内容对象 |

文本消息建议：

```json
{
  "conversationId": 90001,
  "msgType": 1,
  "clientMsgId": "cmsg_70003",
  "content": {
    "text": "中午一起吃饭吗？"
  }
}
```

#### 3.4.5 MessageSendResult

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| messageId | Long | 服务端正式消息 ID |
| clientMsgId | String | 客户端消息 ID |
| sendStatus | Integer | 发送状态 |
| sendTime | String | 发送时间 |

#### 3.4.6 MessageReadRequest

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| conversationId | Long | 是 | 会话 ID |
| messageId | Long | 是 | 已读到的消息 ID |

#### 3.4.7 前端联调提示

1. 列表页：优先拉 `conversation/list`。
2. 进入会话页：拉 `message/history`，同时建立 WebSocket。
3. 发送消息：可以先本地插入临时消息，再调用 HTTP 或 WebSocket 发消息，收到 ACK 后回填状态。
4. 当前后端对 `msgType` 的枚举定义很少，前端如果要扩展图片/文件，需要先和后端统一协议。

### 3.5 群聊

#### 3.5.1 接口清单

| 接口 | Method | Uri | 鉴权 | 说明 |
| ---- | ------ | --- | ---- | ---- |
| 创建群聊 | POST | `/api/group/create` | 是 | 当前返回 demo `groupId` |
| 查询群详情 | GET | `/api/group/detail` | 是 | 当前 demo 返回 |
| 邀请成员入群 | POST | `/api/group/member/invite` | 是 | 当前 no-op |

#### 3.5.2 CreateGroupRequest / InviteMemberRequest

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| groupName | String | 创建群时必填 | 群名称 |
| groupId | Long | 邀请成员时必填 | 群 ID |
| memberIds | Array<Long> | 是 | 用户 ID 列表，不能为空 |

#### 3.5.3 GroupDetailData

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| groupId | Long | 群 ID |
| groupName | String | 群名称 |
| groupAvatar | String | 群头像 |
| ownerUserId | Long | 群主 ID |
| memberCount | Integer | 成员数 |
| muteAll | Boolean | 是否全员禁言 |
| joinPolicy | Integer | 入群策略 |

### 3.6 朋友圈

#### 3.6.1 接口清单

| 接口 | Method | Uri | 鉴权 | 说明 |
| ---- | ------ | --- | ---- | ---- |
| 查询朋友圈时间线 | GET | `/api/moment/timeline` | 是 | 当前 demo；`cursor` 未生效 |
| 发布朋友圈 | POST | `/api/moment/create` | 是 | `visibleScope` 预留 |
| 点赞/取消点赞 | POST | `/api/moment/like` | 是 | 当前 no-op |

#### 3.6.2 MomentItem

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| momentId | Long | 动态 ID |
| authorUserId | Long | 作者用户 ID |
| nickname | String | 作者昵称 |
| avatar | String | 作者头像 |
| content | String | 动态文案 |
| mediaUrls | Array<String> | 媒体地址列表 |
| likeCount | Integer | 点赞数 |
| commentCount | Integer | 评论数 |
| createdAt | String | 发布时间 |

#### 3.6.3 CreateMomentRequest

| 字段 | 类型 | 必填 | 说明 | 备注 |
| ---- | ---- | ---- | ---- | ---- |
| content | String | 否 | 文字内容 | 可为空 |
| mediaUrls | Array<String> | 否 | 图片/视频 URL 列表 | 可为空 |
| visibleScope | String | 否 | 可见范围 | 当前未实际生效 |

#### 3.6.4 LikeMomentRequest

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| momentId | Long | 是 | 动态 ID |
| action | String | 是 | 点赞动作，当前仅校验非空 |

### 3.7 钱包与支付

#### 3.7.1 接口清单

| 接口 | Method | Uri | 鉴权 | 说明 |
| ---- | ------ | --- | ---- | ---- |
| 查询钱包概览 | GET | `/api/wallet/overview` | 是 | 优先查库，空则 demo |
| 查询账单列表 | GET | `/api/wallet/bill/list` | 是 | 当前筛选条件未完全生效 |
| 创建转账 | POST | `/api/wallet/transfer/create` | 是 | 真实扣减余额逻辑已接入 |
| 创建红包 | POST | `/api/wallet/red-packet/create` | 是 | 真实扣减余额与拆红包逻辑已接入 |
| 打开红包 | POST | `/api/wallet/red-packet/open` | 是 | 支持重复打开返回原记录 |

#### 3.7.2 WalletOverview

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| availableBalanceFen | Long | 可用余额，单位分 |
| frozenBalanceFen | Long | 冻结余额，单位分 |
| realnameStatus | Integer | 实名状态 |
| payPasswordStatus | Integer | 支付密码状态 |
| bankCardCount | Integer | 银行卡数量 |

#### 3.7.3 BillItem

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| billId | Long | 账单 ID |
| transactionNo | String | 交易流水号 |
| billType | String | 账单类型，如 `transfer`、`income`、`red_packet` |
| amountFen | Long | 金额，单位分 |
| incomeExpenseType | Integer | 收入支出方向 |
| billTitle | String | 账单标题 |
| billTime | String | 账单时间 |

#### 3.7.4 TransferCreateRequest / TransferResult

请求：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| targetUserId | Long | 是 | 收款人用户 ID |
| amountFen | Long | 是 | 转账金额，单位分 |
| remark | String | 否 | 备注 |

返回：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| transactionId | Long | 交易 ID |
| transactionNo | String | 交易流水号 |
| status | Integer | 交易状态 |
| amountFen | Long | 交易金额，单位分 |

#### 3.7.5 RedPacketCreateRequest / RedPacketDetail

请求：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| amountFen | Long | 是 | 红包总金额，单位分 |
| count | Integer | 是 | 红包个数 |
| type | Integer | 是 | 红包类型 |
| greeting | String | 否 | 祝福语 |
| groupId | Long | 否 | 群红包时的群 ID |

返回：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| redPacketId | Long | 红包 ID |
| redPacketNo | String | 红包流水号 |
| senderUserId | Long | 发红包用户 ID |
| packetType | Integer | 红包类型 |
| totalAmountFen | Long | 红包总金额，单位分 |
| count | Integer | 红包个数 |
| status | Integer | 红包状态 |
| expireAt | String | 过期时间 |

#### 3.7.6 RedPacketOpenRequest / RedPacketOpenResult

请求：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| redPacketId | Long | 是 | 红包 ID |

返回：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| redPacketNo | String | 红包流水号 |
| receiverUserId | Long | 领取人用户 ID |
| receiveAmountFen | Long | 领取金额，单位分 |
| rankNo | Integer | 第几个领取 |
| status | Integer | 红包状态 |

#### 3.7.7 前端联调提示

1. 钱包金额统一按“分”传输与展示，前端展示时自行格式化为元。
2. 红包状态、实名状态、支付密码状态当前是整型字段，建议前端本地建立枚举映射。
3. 转账、发红包接口失败时，`message` 直接可用于 toast 展示。

### 3.8 发现能力

#### 3.8.1 接口清单

| 接口 | Method | Uri | 鉴权 | 说明 |
| ---- | ------ | --- | ---- | ---- |
| 查询发现页能力 | GET | `/api/feature/discovery` | 是 | 当前 demo 返回 |
| 查询功能中心能力 | GET | `/api/feature/workbench` | 是 | 当前 demo 返回 |

#### 3.8.2 FeatureItem

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| featureCode | String | 能力编码 |
| featureName | String | 能力名称 |
| iconUrl | String | 图标 URL |
| routePath | String | 前端跳转路由 |
| enabled | Boolean | 是否启用 |

### 3.9 扫一扫

#### 3.9.1 ScanResolveRequest / ScanResult

请求：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| scanCode | String | 是 | 扫码内容 |
| scene | String | 否 | 扫码场景 |

返回：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| scanType | String | 扫码结果类型 |
| scanToken | String | 扫码结果令牌 |
| title | String | 标题 |
| actionType | String | 前端后续动作 |
| payload | Object | 扩展载荷 |

### 3.10 App 启动

#### 3.10.1 BootstrapData

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| latestVersion | String | 最新版本号 |
| forceUpgrade | Boolean | 是否强制升级 |
| agreementVersion | String | 协议版本 |
| noticeText | String | 启动公告文案 |

### 3.11 媒体

#### 3.11.1 UploadPolicyRequest / UploadPolicyData

请求：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| bizType | String | 是 | 业务类型，如 `moment`、`chat` |
| fileName | String | 是 | 原始文件名 |
| contentType | String | 是 | 文件 MIME 类型 |

返回：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| uploadUrl | String | 上传地址 |
| storageKey | String | 对象存储 key |
| method | String | 上传方法 |
| expireAt | String | 策略过期时间 |

### 3.12 搜索

#### 3.12.1 搜索联想词

请求参数：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| keyword | String | 是 | 搜索关键字 |
| bizType | String | 否 | 业务类型 |

返回：`Array<String>`。

#### 3.12.2 SearchItem

适用接口：全局搜索。

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| bizType | String | 结果业务类型 |
| bizId | String | 结果业务 ID |
| title | String | 主标题 |
| subtitle | String | 副标题 |
| cover | String | 封面图 |
| routePath | String | 前端跳转路由 |

### 3.13 通知

#### 3.13.1 NoticeItem

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| noticeId | Long | 通知 ID |
| noticeType | String | 通知类型 |
| title | String | 标题 |
| content | String | 内容 |
| readStatus | Integer | 已读状态 |
| createdAt | String | 创建时间 |

### 3.14 公众号

#### 3.14.1 OfficialAccountDetail

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| officialId | Long | 公众号 ID |
| name | String | 名称 |
| avatar | String | 头像 |
| intro | String | 简介 |
| verified | Boolean | 是否认证 |
| followed | Boolean | 是否已关注 |

### 3.15 小程序

#### 3.15.1 MiniAppOpenRequest / MiniAppSession

请求：

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| appId | String | 是 | 小程序 appId |
| path | String | 否 | 页面路径 |
| scene | String | 否 | 打开场景 |

返回：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| appId | String | 小程序 appId |
| appName | String | 小程序名称 |
| sessionToken | String | 会话令牌 |
| path | String | 实际打开路径 |
| expireAt | String | 过期时间 |

### 3.16 反馈

#### 3.16.1 FeedbackCreateRequest

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| content | String | 是 | 反馈内容 |
| images | Array<String> | 否 | 图片地址列表 |
| contact | String | 否 | 联系方式 |

### 3.17 IM WebSocket

#### 3.17.1 连接方式

```text
ws://{host}:8081/ws/chat?token={accessToken}&deviceId={deviceId}
```

#### 3.17.2 WebSocket 通用报文结构

客户端发包：

```json
{
  "event": "MESSAGE_SEND",
  "requestId": "cmsg_70003",
  "data": {}
}
```

字段说明：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| event | String | 事件名 |
| requestId | String | 请求标识，建议唯一 |
| data | Object | 业务载荷 |

#### 3.17.3 CONNECT_ACK

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| userId | Long | 当前连接用户 ID |
| deviceId | String | 当前连接设备 ID |
| sessionId | String | WebSocket 会话 ID |
| connectedAt | String | 连接建立时间 |
| lastHeartbeatAt | String | 最近心跳时间 |
| serverTime | String | 服务端时间 |

#### 3.17.4 MESSAGE_SEND.data

| 字段 | 类型 | 必填 | 说明 |
| ---- | ---- | ---- | ---- |
| conversationId | Long | 是 | 会话 ID |
| msgType | Integer | 否 | 消息类型，默认按文本处理 |
| content | Object | 是 | 消息体 |

文本消息建议：

```json
{
  "event": "MESSAGE_SEND",
  "requestId": "cmsg_70003",
  "data": {
    "conversationId": 90001,
    "msgType": 1,
    "content": {
      "text": "中午一起吃饭吗？"
    }
  }
}
```

#### 3.17.5 MESSAGE_ACK.data

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| requestId | String | 原请求 ID |
| conversationId | Long | 会话 ID |
| messageId | Long | 服务端消息 ID |
| clientMsgId | String | 客户端消息 ID |
| sendStatus | Integer | 发送状态 |
| sendTime | String | 发送时间 |
| serverTime | String | 服务端时间 |

#### 3.17.6 MESSAGE_RECEIVE.data.message

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| messageId | Long | 消息 ID |
| clientMsgId | String | 客户端消息 ID |
| conversationId | Long | 会话 ID |
| senderUserId | Long | 发送人 ID |
| msgType | Integer | 消息类型 |
| content | Object | 消息内容 |
| sendStatus | Integer | 发送状态 |
| sendTime | String | 发送时间 |

#### 3.17.7 HEARTBEAT

客户端发：

```json
{
  "event": "HEARTBEAT",
  "requestId": "hb-001"
}
```

服务端回：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| serverTime | String | 服务端时间 |
| sessionId | String | 当前连接会话 ID |

#### 3.17.8 EVENT_ACK

适用场景：

- 非法消息体
- 未识别事件

字段说明：

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| requestId | String | 对应请求 ID |
| event | String | 事件名 |
| sendStatus | Integer | 失败时可能返回 `0` |
| message | String | 错误信息 |
| serverTime | String | 服务端时间 |

## 4.前端联调建议

1. 登录后先缓存 token，再请求资料、会话、通讯录等初始化接口。
2. 会话页建议采用“HTTP 拉历史 + WebSocket 收实时”。
3. 钱包金额统一使用“分”进行计算，避免浮点误差。
4. 当前很多模块仍是 demo 数据，联调时优先验证字段结构、交互流程和状态切换，不要直接依赖业务真实性。
5. 若前端需要更严格的枚举字典，建议下一步由后端补充字典接口或常量文档。
