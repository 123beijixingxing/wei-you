# 微友App 错误码与状态码说明

## 1.文档说明

本文档基于当前后端代码整理：

- 统一响应体 `ApiResponse`
- 全局异常处理 `GlobalExceptionHandler`
- 业务异常 `BusinessException`
- `auth`、`chat`、`wallet`、`group`、`notice` 等模块中实际出现的状态字段

适用场景：

- 前端联调时统一处理错误提示
- 定义本地状态枚举映射
- 识别哪些状态是“代码中已明确存在”，哪些只是“当前示例返回值”

## 2.统一返回规则

### 2.1 通用响应结构

```json
{
  "code": 0,
  "message": "ok",
  "traceId": "trace-id",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| code | Integer | 业务状态码 |
| message | String | 提示文案 |
| traceId | String | 请求追踪 ID |
| data | Object/Array/null | 返回数据 |

### 2.2 code 处理规则

| code | 含义 | 前端建议 |
| ---- | ---- | -------- |
| 0 | 成功 | 正常解析 `data` |
| 400 | 请求参数非法或业务参数错误 | toast / 表单校验提示 |
| 401 | 未登录、token 无效或 token 类型错误 | 清理登录态并跳转登录页 |
| 404 | 资源不存在 | toast 提示并回退或刷新 |
| 500 | 服务端异常 | 展示“系统繁忙，请稍后重试” |

## 3.错误码说明

### 3.1 全局错误

| code | message 示例 | 来源 | 说明 |
| ---- | ------------ | ---- | ---- |
| 400 | `mobile must not be blank` | 参数校验 | `@Valid` / `@NotBlank` / `@NotNull` 失败时返回第一个字段错误 |
| 500 | `internal error` | 全局异常处理 | 未捕获异常且 message 为空 |
| 500 | `具体异常文案` | 全局异常处理 | 未捕获异常且 message 不为空 |

补充说明：

- 参数校验错误的 `message` 不是固定字典，而是“字段名 + 校验提示”。
- 前端不建议依赖完整英文报错做逻辑判断，建议主要依赖 `code`，文案仅用于提示。

### 3.2 认证模块错误

| code | message | 触发场景 | 前端建议 |
| ---- | ------- | -------- | -------- |
| 401 | `mobile or password incorrect` | 密码登录失败 | 用户名/密码错误提示 |
| 401 | `invalid token` | token 解析失败、签名错误、已过期 | 重新登录 |
| 401 | `invalid token type` | 例如用 accessToken 调 refresh 接口 | 重新刷新登录态 |

说明：

- 短信登录与注册当前不会校验 `smsCode`。
- `logout` 与 `device/offline` 当前即使设备不存在也不会报错，接口直接成功返回。

### 3.3 钱包模块错误

| code | message | 触发场景 | 前端建议 |
| ---- | ------- | -------- | -------- |
| 400 | `user required` | 用户 ID 缺失 | 视为登录态异常，重新初始化 |
| 400 | `cannot transfer to self` | 给自己转账 | 直接 toast 提示 |
| 400 | `invalid transfer amount` | 转账金额为空、0、负数 | 阻止提交并校验金额 |
| 400 | `insufficient balance` | 可用余额不足 | 提示余额不足 |
| 400 | `invalid red packet amount` | 红包总金额非法 | 阻止提交 |
| 400 | `invalid red packet count` | 红包个数非法 | 阻止提交 |
| 400 | `red packet amount too small` | 红包总额小于个数 | 提示最小金额规则 |
| 400 | `red packet already finished` | 红包已结束 | 提示红包已结束 |
| 400 | `red packet expired` | 红包过期 | 提示红包已过期 |
| 400 | `red packet empty` | 红包被抢完 | 提示已领完 |
| 404 | `wallet account not found` | 转账付款方钱包不存在 | 提示未开通钱包 |
| 404 | `red packet not found` | 红包不存在 | 提示红包无效 |

说明：

- 钱包模块是当前业务逻辑最完整的模块之一，前端可以直接依据 `message` 做用户提示。
- 当前没有单独的支付密码校验错误码，也没有风控错误码。

### 3.4 WebSocket 相关错误

#### 3.4.1 握手失败

| 方式 | 触发条件 | 结果 |
| ---- | -------- | ---- |
| HTTP/WS 握手阶段 | 缺少 `token` | `401 Unauthorized` |
| HTTP/WS 握手阶段 | token 非法/过期 | `401 Unauthorized` |

#### 3.4.2 事件级失败

| event | 字段 | 值 | 说明 |
| ----- | ---- | --- | ---- |
| EVENT_ACK | sendStatus | `0` | 当前消息事件处理失败 |
| EVENT_ACK | message | `invalid message payload` | 发送消息载荷不合法 |

## 4.状态字段说明

### 4.1 认证与设备状态

#### 4.1.1 needDeviceVerify

| 字段 | 类型 | 当前值 | 说明 |
| ---- | ---- | ------ | ---- |
| needDeviceVerify | Boolean | `false` | 当前后端固定返回 `false` |

#### 4.1.2 设备在线状态

接口对外返回布尔值：

| 字段 | 类型 | 取值 | 说明 |
| ---- | ---- | ---- | ---- |
| online | Boolean | `true` / `false` | 设备是否在线 |

底层持久化使用整型：

| 存储字段 | 取值 | 说明 |
| -------- | ---- | ---- |
| onlineStatus | `1` | 在线 |
| onlineStatus | `0` | 离线 |

### 4.2 用户资料状态

#### 4.2.1 dynamic

| 字段 | 类型 | 取值 | 说明 |
| ---- | ---- | ---- | ---- |
| dynamic | Boolean | `true` / `false` | 是否动态二维码 |

#### 4.2.2 statusCode

| 字段 | 类型 | 当前规则 | 说明 |
| ---- | ---- | -------- | ---- |
| statusCode | String | 仅校验非空 | 前端可自行约定如 `BUSY`、`FREE`、`MEETING` |

说明：

- `statusCode` 当前没有后端固定字典。
- `expireAt` 当前未实际落库，不建议前端依赖该字段做精确倒计时逻辑。

### 4.3 通讯录与好友状态

#### 4.3.1 star

| 字段 | 类型 | 取值 | 说明 |
| ---- | ---- | ---- | ---- |
| star | Boolean | `true` / `false` | 是否星标好友 |

#### 4.3.2 好友申请状态

| 字段 | 类型 | 当前可见值 | 说明 |
| ---- | ---- | ---------- | ---- |
| status | Integer | `0` | 当前 demo 数据可理解为待处理 |

说明：

- 当前后端没有输出明确的好友申请状态字典。
- `action` 字段仅要求非空，后端未强约束 `ACCEPT/REJECT` 枚举。

### 4.4 会话与消息状态

#### 4.4.1 conversationType

| 值 | 说明 | 来源 |
| --- | ---- | ---- |
| 1 | 单聊 | 代码中显式创建默认单聊会话 |
| 2 | 群聊 | 会话列表中显式用于群聊标题/头像分支 |

#### 4.4.2 msgType

| 值 | 说明 | 备注 |
| --- | ---- | ---- |
| 1 | 文本消息 | 当前代码中唯一明确识别并生成摘要的类型 |
| 其他整数 | 扩展消息类型 | 当前后端未提供正式字典 |

#### 4.4.3 sendStatus

| 值 | 说明 | 适用场景 |
| --- | ---- | -------- |
| 1 | 发送成功 | HTTP 发消息成功、WebSocket ACK 成功 |
| 0 | 发送失败 | WebSocket `EVENT_ACK` 中的非法载荷场景 |

#### 4.4.4 readStatus

| 值 | 说明 | 备注 |
| --- | ---- | ---- |
| 0 | 未读 | 历史消息示例常见值 |
| 1 | 已读 | 已读消息 |

#### 4.4.5 top / mute

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| top | Boolean | 是否置顶会话 |
| mute | Boolean | 是否免打扰 |

### 4.5 群聊状态

#### 4.5.1 muteAll

| 字段 | 类型 | 取值 | 说明 |
| ---- | ---- | ---- | ---- |
| muteAll | Boolean | `true` / `false` | 是否全员禁言 |

#### 4.5.2 joinPolicy

| 字段 | 类型 | 当前值 | 说明 |
| ---- | ---- | ------ | ---- |
| joinPolicy | Integer | `0` | 当前代码只返回 `0`，未定义更细字典 |

### 4.6 朋友圈状态

#### 4.6.1 点赞动作 action

| 字段 | 类型 | 当前规则 | 说明 |
| ---- | ---- | -------- | ---- |
| action | String | 仅校验非空 | 当前后端未定义固定枚举 |

#### 4.6.2 visibleScope

| 字段 | 类型 | 当前状态 | 说明 |
| ---- | ---- | -------- | ---- |
| visibleScope | String | 预留字段 | 当前接口接收但未实际生效 |

### 4.7 钱包与支付状态

#### 4.7.1 incomeExpenseType

| 值 | 说明 | 适用场景 |
| --- | ---- | -------- |
| 1 | 收入 | 收到转账、领取红包 |
| 2 | 支出 | 转账支出、发红包 |

#### 4.7.2 transactionStatus

| 值 | 说明 | 备注 |
| --- | ---- | ---- |
| 1 | 成功 | 当前代码落库只看到成功态 |

#### 4.7.3 packetStatus

| 值 | 说明 | 触发时机 |
| --- | ---- | -------- |
| 0 | 进行中 | 红包创建后、尚未领完 |
| 1 | 已结束/已领完 | 红包被领完后更新 |
| 2 | 已过期 | 打开红包时发现已过期后更新 |

#### 4.7.4 walletStatus

| 值 | 说明 | 备注 |
| --- | ---- | ---- |
| 0 | 默认/正常初始态 | 当前自动创建钱包时使用 |

#### 4.7.5 realnameStatus

| 值 | 说明 | 备注 |
| --- | ---- | ---- |
| 0 | 未实名 | 注册后默认值、自动建钱包默认值 |
| 1 | 已实名 | 概览示例返回可见 |

#### 4.7.6 payPasswordStatus

| 值 | 说明 | 备注 |
| --- | ---- | ---- |
| 0 | 未设置支付密码 | 自动建钱包默认值 |
| 1 | 已设置支付密码 | 钱包概览示例值 |

#### 4.7.7 packetType

| 字段 | 类型 | 当前规则 | 说明 |
| ---- | ---- | -------- | ---- |
| packetType / type | Integer | 后端接收整数 | 当前未定义普通红包/拼手气红包等正式枚举 |

### 4.8 发现、搜索、通知等状态

#### 4.8.1 enabled

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| enabled | Boolean | 能力入口是否启用 |

#### 4.8.2 readStatus

| 字段 | 类型 | 当前可见值 | 说明 |
| ---- | ---- | ---------- | ---- |
| readStatus | Integer | `0` | 当前通知示例为未读 |

#### 4.8.3 verified / followed

| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| verified | Boolean | 公众号是否认证 |
| followed | Boolean | 当前用户是否已关注 |

## 5.WebSocket 事件字典

| event | 方向 | 说明 |
| ----- | ---- | ---- |
| CONNECT_ACK | 服务端 -> 客户端 | 建连成功回执 |
| HEARTBEAT | 双向 | 心跳请求/心跳响应 |
| MESSAGE_SEND | 客户端 -> 服务端 | 发送消息 |
| MESSAGE_ACK | 服务端 -> 客户端 | 消息发送成功确认 |
| MESSAGE_RECEIVE | 服务端 -> 客户端 | 消息广播下发 |
| EVENT_ACK | 服务端 -> 客户端 | 非法事件/兜底确认 |

## 6.前端处理建议

### 6.1 HTTP 错误处理建议

| code | 前端处理 |
| ---- | -------- |
| 400 | 展示后端返回文案；表单页优先定位字段问题 |
| 401 | 清理 `accessToken`/`refreshToken`，跳登录页 |
| 404 | 提示资源不存在，可返回上页或刷新列表 |
| 500 | 统一展示通用错误文案，并记录 `traceId` |

### 6.2 钱包场景建议

1. `amountFen` 必须在前端先做正整数校验。
2. 收到 `insufficient balance` 时直接提示余额不足，不要重试提交。
3. 红包接口建议根据 `packetStatus` 做页面状态渲染：进行中、已领完、已过期。

### 6.3 WebSocket 场景建议

1. 建连失败 `401` 视为登录态失效。
2. `MESSAGE_SEND` 时本地消息可先置为“发送中”。
3. 收到 `MESSAGE_ACK` 后再把本地消息状态改为成功。
4. 收到 `EVENT_ACK` 且 `sendStatus=0` 时，将本地消息标记为失败并允许重发。

### 6.4 枚举维护建议

当前后端还没有提供完整字典接口，建议前端先本地维护一份轻量枚举映射，至少覆盖：

- `conversationType`
- `msgType`
- `incomeExpenseType`
- `packetStatus`
- `payPasswordStatus`
- `realnameStatus`

后续若后端补充字典接口或常量文件，再统一切换为服务端标准枚举。
