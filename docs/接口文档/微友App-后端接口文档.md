# 微友 App api

## 1.修订记录

| 版本 | 日期       | 修订人   | 说明 |
| ---- | ---------- | -------- | ---- |
| V1.0 | 2026-05-11 | OpenCode | 初稿 |

## 2.说明

1. 接口 `context-path`：`/api`
2. 当前文档依据 `docs/需求设计文档/微友开发需求文档.md` 与后端控制器代码生成，优先以后端代码实际暴露接口为准。
3. 统一响应结构如下：

```json
{
  "code": 0,
  "message": "ok",
  "traceId": "3f4b9d6e8a2c4f1b",
  "data": {}
}
```

4. 分页响应中 `data` 结构如下：

```json
{
  "code": 0,
  "message": "ok",
  "traceId": "3f4b9d6e8a2c4f1b",
  "data": {
    "list": [],
    "pageNo": 1,
    "pageSize": 20,
    "total": 0,
    "hasMore": false,
    "nextCursor": null
  }
}
```

5. 鉴权规则：除匿名接口外，其余接口均需携带 `Authorization: Bearer {accessToken}`。
6. 匿名接口如下：
   - `POST /api/auth/login/password`
   - `POST /api/auth/login/sms`
   - `POST /api/auth/register`
   - `POST /api/auth/sms/send`
   - `POST /api/auth/token/refresh`
   - `GET /api/app/bootstrap`
7. 参数校验基于 `@Valid`、`@NotBlank`、`@NotNull`、`@NotEmpty`；未标注字段默认为非必填。
8. 异常规则：参数校验失败通常返回 `400`，未登录/令牌无效通常返回 `401`，业务异常返回对应业务码，服务异常返回 `500`。
9. 时间字段当前主要为 ISO-8601 字符串，实际格式以后端序列化结果为准。
10. 需求文档中已规划但当前未提供 HTTP Controller 的模块，如 `channel`、`audit-risk` 等，不在本次接口文档范围内。
11. 文档中已明确标注 `demo/stub/no-op/预留字段未生效` 的接口，表示当前代码已暴露接口，但业务实现尚未完全落地。
12. 配套补充文档：
    - 字段联调说明：`docs/接口文档/微友App-前端联调字段说明.md`
    - 错误码与状态码：`docs/接口文档/微友App-错误码与状态码说明.md`
    - OpenAPI 草稿：`docs/接口文档/微友App-openapi.yaml`
    - Postman Collection：`docs/接口文档/微友App-postman-collection.json`
    - Postman 环境变量：`docs/接口文档/微友App-postman-local-environment.json`

## 3.接口

### 3.1 认证与账号

#### 3.1.1 密码登录

- **Method**: POST

- **Uri**: `/api/auth/login/password`

- **Param**: NONE

- **Header**: NONE

- **RequestBody**:

  ```json
  {
    "mobile": "13800138000",
    "password": "123456",
    "deviceId": "ios-iphone15pm-001"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "8db2df7b4d8541c9",
    "data": {
      "userId": 10001,
      "accessToken": "eyJhbGciOiJIUzI1NiJ9.access",
      "refreshToken": "eyJhbGciOiJIUzI1NiJ9.refresh",
      "expireIn": 7200,
      "needDeviceVerify": false
    }
  }
  ```

#### 3.1.2 短信验证码登录

- **Method**: POST

- **Uri**: `/api/auth/login/sms`

- **Param**: NONE

- **Header**: NONE

- **RequestBody**:

  ```json
  {
    "mobile": "13800138000",
    "smsCode": "123456",
    "deviceId": "android-huawei-p70-001"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "ef3c6f65db7f4db5",
    "data": {
      "userId": 10002,
      "accessToken": "eyJhbGciOiJIUzI1NiJ9.access",
      "refreshToken": "eyJhbGciOiJIUzI1NiJ9.refresh",
      "expireIn": 7200,
      "needDeviceVerify": false
    }
  }
  ```

#### 3.1.3 注册

- **Method**: POST

- **Uri**: `/api/auth/register`

- **Param**: NONE

- **Header**: NONE

- **RequestBody**:

  ```json
  {
    "mobile": "13800138000",
    "smsCode": "123456",
    "password": "123456",
    "deviceId": "ios-iphone15pm-001"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "8c56d9cc92e04992",
    "data": {
      "userId": 10003,
      "accessToken": "eyJhbGciOiJIUzI1NiJ9.access",
      "refreshToken": "eyJhbGciOiJIUzI1NiJ9.refresh",
      "expireIn": 7200,
      "needDeviceVerify": false
    }
  }
  ```

#### 3.1.4 发送短信验证码

- **Method**: POST

- **Uri**: `/api/auth/sms/send`

- **Param**: NONE

- **Header**: NONE

- **RequestBody**:

  ```json
  {
    "mobile": "13800138000",
    "scene": "LOGIN"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "ef1bbfef0f0f4d38",
    "data": null
  }
  ```

#### 3.1.5 刷新 token

- **Method**: POST

- **Uri**: `/api/auth/token/refresh`

- **Param**: NONE

- **Header**: NONE

- **RequestBody**:

  ```json
  {
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.refresh"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "a0fa2749a8e34241",
    "data": {
      "userId": 10001,
      "accessToken": "eyJhbGciOiJIUzI1NiJ9.new-access",
      "refreshToken": "eyJhbGciOiJIUzI1NiJ9.new-refresh",
      "expireIn": 7200,
      "needDeviceVerify": false
    }
  }
  ```

#### 3.1.6 退出登录

- **Method**: POST

- **Uri**: `/api/auth/logout`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；当前按 `deviceId` 执行下线 |

- **RequestBody**:

  ```json
  {
    "deviceId": "ios-iphone15pm-001"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "a7f3adcc8be34737",
    "data": null
  }
  ```

#### 3.1.7 查询登录设备列表

- **Method**: GET

- **Uri**: `/api/auth/device/list`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；优先查库，空则返回 demo 数据 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "ddcf57b172524a57",
    "data": [
      {
        "deviceId": "device-app-001",
        "deviceType": "android",
        "deviceModel": "Xiaomi 14",
        "systemVersion": "Android 15",
        "loginCity": "Shenzhen",
        "loginIp": "120.22.11.10",
        "lastLoginAt": "2026-05-11T09:30:00Z",
        "online": true
      },
      {
        "deviceId": "device-web-001",
        "deviceType": "web",
        "deviceModel": "Chrome",
        "systemVersion": "126.0",
        "loginCity": "Guangzhou",
        "loginIp": "183.2.10.21",
        "lastLoginAt": "2026-05-11T08:30:00Z",
        "online": false
      }
    ]
  }
  ```

#### 3.1.8 下线指定设备

- **Method**: POST

- **Uri**: `/api/auth/device/offline`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；当前未校验设备归属 |

- **RequestBody**:

  ```json
  {
    "deviceId": "device-web-001"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "ec4f80393b5541da",
    "data": null
  }
  ```

### 3.2 用户资料

#### 3.2.1 查询我的资料

- **Method**: GET

- **Uri**: `/api/user/profile/me`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；当前支持 `type/startDate/endDate` 过滤 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "6837ecfabd0b4452",
    "data": {
      "userId": 10001,
      "weiyouNo": "weiyou_10001",
      "nickname": "微友产品体验官",
      "avatar": "https://weiyou.local/avatar/10001.png",
      "gender": 1,
      "city": "深圳",
      "signature": "让连接更近一点",
      "statusText": "开会中",
      "qrcodeUrl": "https://weiyou.local/qrcode/10001.png"
    }
  }
  ```

#### 3.2.2 查询用户资料详情

- **Method**: GET

- **Uri**: `/api/user/profile/detail`

- **Param**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | userId | Long | 用户 ID | N | 不传则查询当前登录用户 |

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "21f954af770b467f",
    "data": {
      "userId": 10002,
      "weiyouNo": "weiyou_10002",
      "nickname": "阿泽",
      "avatar": "https://weiyou.local/avatar/10002.png",
      "gender": 1,
      "city": "深圳",
      "signature": "让连接更近一点",
      "statusText": "开会中",
      "qrcodeUrl": "https://weiyou.local/qrcode/10002.png"
    }
  }
  ```

#### 3.2.3 更新资料

- **Method**: POST

- **Uri**: `/api/user/profile/update`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**:

  ```json
  {
    "nickname": "小微新昵称",
    "avatar": "https://weiyou.local/avatar/10001-new.png",
    "gender": 2,
    "city": "杭州",
    "signature": "认真生活，认真聊天"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "3a57193c7afd482a",
    "data": null
  }
  ```

#### 3.2.4 获取个人二维码

- **Method**: GET

- **Uri**: `/api/user/qrcode/get`

- **Param**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | dynamic | boolean | 是否动态二维码 | N | 默认 `false` |

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "50f2d98a6f0e4e1b",
    "data": {
      "ticket": "ticket-10001",
      "qrcodeUrl": "https://weiyou.local/qrcode/10001.png",
      "dynamic": true,
      "expireAt": "2026-12-31T23:59:59Z"
    }
  }
  ```

#### 3.2.5 更新状态

- **Method**: POST

- **Uri**: `/api/user/status/update`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；`expireAt` 当前未落库 |

- **RequestBody**:

  ```json
  {
    "statusCode": "BUSY",
    "statusText": "开会中",
    "expireAt": "2026-05-11T18:00:00Z"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "a3fc12ef03c54bbf",
    "data": null
  }
  ```

#### 3.2.6 查询用户设置

- **Method**: GET

- **Uri**: `/api/user/setting/detail`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "fb8baef72cce42cb",
    "data": {
      "userId": 10001,
      "messageNotification": true,
      "momentNotification": true,
      "officialNotification": true,
      "soundEnabled": true,
      "vibrationEnabled": true,
      "addByPhone": true,
      "addByWeiyouNo": true,
      "groupInviteConfirm": false,
      "hideMyMoments": false,
      "hideLocation": false
    }
  }
  ```

#### 3.2.7 更新用户设置

- **Method**: POST

- **Uri**: `/api/user/setting/update`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；支持部分字段更新 |

- **RequestBody**:

  ```json
  {
    "messageNotification": true,
    "momentNotification": true,
    "officialNotification": false,
    "soundEnabled": true,
    "vibrationEnabled": false,
    "addByPhone": true,
    "addByWeiyouNo": true,
    "groupInviteConfirm": false,
    "hideMyMoments": false,
    "hideLocation": true
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "ae0c9a91986f4f26",
    "data": {
      "userId": 10001,
      "messageNotification": true,
      "momentNotification": true,
      "officialNotification": false,
      "soundEnabled": true,
      "vibrationEnabled": false,
      "addByPhone": true,
      "addByWeiyouNo": true,
      "groupInviteConfirm": false,
      "hideMyMoments": false,
      "hideLocation": true
    }
  }
  ```

### 3.3 通讯录与好友

#### 3.3.1 查询通讯录列表

- **Method**: GET

- **Uri**: `/api/contact/list`

- **Param**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | keyword | String | 搜索关键字 | N | 当前 demo 返回 |
  | letter | String | 首字母筛选 | N | 当前 demo 返回 |

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "bdf157bf6cb943fa",
    "data": {
      "list": [
        {
          "userId": 10002,
          "nickname": "阿泽",
          "remark": "同事阿泽",
          "avatar": "https://weiyou.local/avatar/10002.png",
          "letter": "A",
          "star": true
        },
        {
          "userId": 10003,
          "nickname": "小林",
          "remark": "产品小林",
          "avatar": "https://weiyou.local/avatar/10003.png",
          "letter": "X",
          "star": false
        }
      ],
      "pageNo": 1,
      "pageSize": 20,
      "total": 2,
      "hasMore": false,
      "nextCursor": null
    }
  }
  ```

#### 3.3.2 发起好友申请

- **Method**: POST

- **Uri**: `/api/contact/friend/apply`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；当前无持久化逻辑 |

- **RequestBody**:

  ```json
  {
    "targetUserId": 10005,
    "remark": "我是小微",
    "source": "search"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "9f3e78d172ed4326",
    "data": null
  }
  ```

#### 3.3.3 查询好友申请列表

- **Method**: GET

- **Uri**: `/api/contact/friend/request/list`

- **Param**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | status | Integer | 申请状态 | N | 当前 demo 返回 |

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "74efcc7ef9354f4b",
    "data": {
      "list": [
        {
          "requestId": 50001,
          "fromUserId": 10008,
          "nickname": "小宇",
          "avatar": "https://weiyou.local/avatar/10008.png",
          "applyMessage": "来自项目群",
          "status": 0,
          "createdAt": "2026-05-11T10:12:00Z"
        }
      ],
      "pageNo": 1,
      "pageSize": 20,
      "total": 1,
      "hasMore": false,
      "nextCursor": null
    }
  }
  ```

#### 3.3.4 处理好友申请

- **Method**: POST

- **Uri**: `/api/contact/friend/request/handle`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；当前无处理逻辑 |

- **RequestBody**:

  ```json
  {
    "requestId": 50001,
    "action": "ACCEPT"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "32a5b268af7e49df",
    "data": null
  }
  ```

### 3.4 会话与消息

#### 3.4.1 查询会话列表

- **Method**: GET

- **Uri**: `/api/chat/conversation/list`

- **Param**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | cursor | String | 游标 | N | 当前未实际分页 |
  | pageSize | int | 每页数量 | N | 默认 `20`；优先查库，空则返回 demo 数据 |

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "bd17bc899cf948c6",
    "data": {
      "list": [
        {
          "conversationId": 90001,
          "conversationType": 1,
          "title": "阿泽",
          "avatar": "https://weiyou.local/avatar/10002.png",
          "unreadCount": 2,
          "top": true,
          "mute": false,
          "lastMessageDigest": "明天下午对齐方案",
          "lastMessageTime": "2026-05-11T10:30:00Z"
        },
        {
          "conversationId": 90002,
          "conversationType": 2,
          "title": "微友产品群",
          "avatar": "https://weiyou.local/group/90002.png",
          "unreadCount": 8,
          "top": false,
          "mute": true,
          "lastMessageDigest": "[图片]",
          "lastMessageTime": "2026-05-11T10:25:00Z"
        }
      ],
      "pageNo": 1,
      "pageSize": 20,
      "total": 2,
      "hasMore": false,
      "nextCursor": null
    }
  }
  ```

#### 3.4.2 打开单聊会话

- **Method**: POST

- **Uri**: `/api/chat/conversation/open-single`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；若会话不存在会自动创建 |

- **RequestBody**:

  ```json
  {
    "targetUserId": 10008
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "7a6fbb30e0b04aa1",
    "data": {
      "conversationId": 1715589900000,
      "targetUserId": 10008,
      "conversationType": 1
    }
  }
  ```

#### 3.4.3 查询会话设置

- **Method**: GET

- **Uri**: `/api/chat/conversation/detail`

- **Param**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | conversationId | Long | 会话 ID | Y |  |

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "c21318d5b3e84f74",
    "data": {
      "conversationId": 90001,
      "conversationType": 1,
      "title": "会话-90001",
      "avatar": "https://weiyou.local/avatar/default.png",
      "top": false,
      "mute": false,
      "markUnread": false,
      "unreadCount": 0,
      "clearBeforeTime": null
    }
  }
  ```

#### 3.4.4 更新会话设置

- **Method**: POST

- **Uri**: `/api/chat/conversation/setting/update`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**:

  ```json
  {
    "conversationId": 90001,
    "top": true,
    "mute": true,
    "markUnread": false
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "bd0c6cbca0d742f1",
    "data": {
      "conversationId": 90001,
      "conversationType": 1,
      "title": "会话-90001",
      "avatar": "https://weiyou.local/avatar/default.png",
      "top": true,
      "mute": true,
      "markUnread": false,
      "unreadCount": 0,
      "clearBeforeTime": null
    }
  }
  ```

#### 3.4.5 清空聊天记录

- **Method**: POST

- **Uri**: `/api/chat/conversation/clear`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；清空后仅保留清空时间之后的新消息 |

- **RequestBody**:

  ```json
  {
    "conversationId": 90001
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "eddb9dc7d59d4d2a",
    "data": {
      "conversationId": 90001,
      "conversationType": 1,
      "title": "会话-90001",
      "avatar": "https://weiyou.local/avatar/default.png",
      "top": true,
      "mute": true,
      "markUnread": false,
      "unreadCount": 0,
      "clearBeforeTime": "2026-05-18T12:30:00"
    }
  }
  ```

#### 3.4.6 查询聊天记录

- **Method**: GET

- **Uri**: `/api/chat/message/history`

- **Param**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | conversationId | Long | 会话 ID | Y |  |
  | cursor | String | 游标 | N | 当前未实际参与分页 |
  | pageSize | int | 每页数量 | N | 默认 `20` |

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；查库时 `content` 可能包装为 `{raw: contentJson}` |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "cbad5114a92144b7",
    "data": {
      "list": [
        {
          "messageId": 70001,
          "clientMsgId": "cmsg_70001",
          "conversationId": 90001,
          "senderUserId": 10001,
          "msgType": 1,
          "content": {
            "text": "你好，欢迎来到微友"
          },
          "sendStatus": 1,
          "sendTime": "2026-05-11T10:20:00Z",
          "readStatus": 1
        },
        {
          "messageId": 70002,
          "clientMsgId": "cmsg_70002",
          "conversationId": 90001,
          "senderUserId": 10002,
          "msgType": 1,
          "content": {
            "text": "收到，稍后我来处理"
          },
          "sendStatus": 1,
          "sendTime": "2026-05-11T10:21:00Z",
          "readStatus": 1
        }
      ],
      "pageNo": 1,
      "pageSize": 20,
      "total": 2,
      "hasMore": false,
      "nextCursor": null
    }
  }
  ```

#### 3.4.7 发送消息

- **Method**: POST

- **Uri**: `/api/chat/message/send`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；会话不存在时会自动创建单聊会话 |

- **RequestBody**:

  ```json
  {
    "conversationId": 90001,
    "msgType": 1,
    "clientMsgId": "cmsg_70003",
    "replyMessageId": 70001,
    "content": {
      "text": "中午一起吃饭吗？",
      "replyPreviewText": "你好，欢迎来到微友",
      "replySenderName": "陈微",
      "replyType": "text"
    }
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "404e57eea6944486",
    "data": {
      "messageId": 70003,
      "clientMsgId": "cmsg_70003",
      "sendStatus": 1,
      "sendTime": "2026-05-11T10:35:00Z"
    }
  }
  ```

#### 3.4.8 搜索会话消息

- **Method**: GET

- **Uri**: `/api/chat/message/search`

- **Param**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | conversationId | Long | 会话 ID | Y |  |
  | keyword | String | 搜索关键字 | Y | 当前按消息 JSON 文本包含匹配 |
  | pageSize | int | 每页数量 | N | 默认 `20` |

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "3f11a2fdc7424f8d",
    "data": {
      "list": [
        {
          "messageId": 70001,
          "clientMsgId": "cmsg_70001",
          "conversationId": 90001,
          "senderUserId": 10001,
          "msgType": 1,
          "replyMessageId": null,
          "content": {
            "text": "你好，欢迎来到微友"
          },
          "sendStatus": 1,
          "sendTime": "2026-05-11T10:20:00Z",
          "readStatus": 1
        }
      ],
      "pageNo": 1,
      "pageSize": 20,
      "total": 1,
      "hasMore": false,
      "nextCursor": null
    }
  }
  ```

#### 3.4.9 标记已读

- **Method**: POST

- **Uri**: `/api/chat/message/read`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**:

  ```json
  {
    "conversationId": 90001,
    "messageId": 70003
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "4597b0a55c944220",
    "data": null
  }
  ```

#### 3.4.10 撤回消息

- **Method**: POST

- **Uri**: `/api/chat/message/revoke`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；当前仅支持撤回自己发送的消息 |

- **RequestBody**:

  ```json
  {
    "conversationId": 90001,
    "messageId": 70003
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "4dfdb0f7ca7b46c4",
    "data": {
      "messageId": 70003,
      "conversationId": 90001,
      "revoked": true
    }
  }
  ```

### 3.5 群聊

#### 3.5.1 创建群聊

- **Method**: POST

- **Uri**: `/api/group/create`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；当前 `groupId` 为示例生成值 |

- **RequestBody**:

  ```json
  {
    "groupName": "项目沟通群",
    "memberIds": [10002, 10003, 10004]
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "c643df736bbc4544",
    "data": {
      "groupId": 90002,
      "groupName": "项目沟通群",
      "groupAvatar": "https://weiyou.local/group/90002.png",
      "ownerUserId": 10001,
      "memberCount": 12,
      "muteAll": false,
      "joinPolicy": 0,
      "notice": "新群已创建，快来完善群公告。"
    }
  }
  ```

#### 3.5.2 查询群详情

- **Method**: GET

- **Uri**: `/api/group/detail`

- **Param**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | groupId | Long | 群 ID | Y | 当前返回内存态群详情 |

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "9241fd1545fe43e4",
    "data": {
      "groupId": 90002,
      "groupName": "微友产品群",
      "groupAvatar": "https://weiyou.local/group/90002.png",
      "ownerUserId": 10001,
      "memberCount": 12,
      "muteAll": false,
      "joinPolicy": 0,
      "notice": "18:00 前同步一期接口联调进度。"
    }
  }
  ```

#### 3.5.3 邀请成员入群

- **Method**: POST

- **Uri**: `/api/group/member/invite`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；会更新群成员数量与成员列表 |

- **RequestBody**:

  ```json
  {
    "groupId": 90002,
    "memberIds": [10005, 10006]
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "93e6856b540d4efc",
    "data": {
      "groupId": 90002,
      "groupName": "微友产品群",
      "groupAvatar": "https://weiyou.local/group/90002.png",
      "ownerUserId": 10001,
      "memberCount": 14,
      "muteAll": false,
      "joinPolicy": 0,
      "notice": "18:00 前同步一期接口联调进度。"
    }
  }
  ```

#### 3.5.4 查询群成员列表

- **Method**: GET

- **Uri**: `/api/group/member/list`

- **Param**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | groupId | Long | 群 ID | Y |  |

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "2b324d0b8f0d4b1a",
    "data": [
      {
        "userId": 10001,
        "nickname": "陈微",
        "avatar": "https://weiyou.local/avatar/10001.png",
        "role": "群主",
        "groupNickname": "陈微",
        "owner": true,
        "admin": true,
        "muted": false
      },
      {
        "userId": 10011,
        "nickname": "周铭",
        "avatar": "https://weiyou.local/avatar/10011.png",
        "role": "产品负责人",
        "groupNickname": "周铭",
        "owner": false,
        "admin": true,
        "muted": false
      }
    ]
  }
  ```

#### 3.5.5 更新群公告

- **Method**: POST

- **Uri**: `/api/group/notice/update`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**:

  ```json
  {
    "groupId": 90002,
    "notice": "今天 18:00 前同步二期群设置联调进度。"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "9d6a9a83c67a4a47",
    "data": {
      "groupId": 90002,
      "groupName": "微友产品群",
      "groupAvatar": "https://weiyou.local/group/90002.png",
      "ownerUserId": 10001,
      "memberCount": 12,
      "muteAll": false,
      "joinPolicy": 0,
      "notice": "今天 18:00 前同步二期群设置联调进度。"
    }
  }
  ```

#### 3.5.6 更新群设置

- **Method**: POST

- **Uri**: `/api/group/setting/update`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**:

  ```json
  {
    "groupId": 90002,
    "muteAll": true,
    "joinPolicy": 1
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "87b179eeb7ae4224",
    "data": {
      "groupId": 90002,
      "groupName": "微友产品群",
      "groupAvatar": "https://weiyou.local/group/90002.png",
      "ownerUserId": 10001,
      "memberCount": 12,
      "muteAll": true,
      "joinPolicy": 1,
      "notice": "18:00 前同步一期接口联调进度。"
    }
  }
  ```

#### 3.5.7 设置管理员

- **Method**: POST

- **Uri**: `/api/group/admin/update`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；当前仅群主可操作 |

- **RequestBody**:

  ```json
  {
    "groupId": 90002,
    "userId": 10012,
    "admin": true
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "5c20f1f2b30f4b33",
    "data": {
      "groupId": 90002,
      "groupName": "微友产品群",
      "groupAvatar": "https://weiyou.local/group/90002.png",
      "ownerUserId": 10001,
      "memberCount": 12,
      "muteAll": true,
      "joinPolicy": 1,
      "notice": "18:00 前同步一期接口联调进度。"
    }
  }
  ```

#### 3.5.8 单独禁言成员

- **Method**: POST

- **Uri**: `/api/group/member/mute`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；当前群主或管理员可操作，不能禁言群主 |

- **RequestBody**:

  ```json
  {
    "groupId": 90002,
    "userId": 10012,
    "muted": true
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "cd3916e4d5f24e2e",
    "data": {
      "groupId": 90002,
      "groupName": "微友产品群",
      "groupAvatar": "https://weiyou.local/group/90002.png",
      "ownerUserId": 10001,
      "memberCount": 12,
      "muteAll": true,
      "joinPolicy": 1,
      "notice": "18:00 前同步一期接口联调进度。"
    }
  }
  ```

#### 3.5.9 更新群昵称

- **Method**: POST

- **Uri**: `/api/group/member/nickname/update`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；当前支持成员修改自己的群昵称，群主也可代改 |

- **RequestBody**:

  ```json
  {
    "groupId": 90002,
    "userId": 10012,
    "groupNickname": "设计小赵"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "c533a9ef63ab4b95",
    "data": {
      "groupId": 90002,
      "groupName": "微友产品群",
      "groupAvatar": "https://weiyou.local/group/90002.png",
      "ownerUserId": 10001,
      "memberCount": 12,
      "muteAll": true,
      "joinPolicy": 1,
      "notice": "18:00 前同步一期接口联调进度。"
    }
  }
  ```

#### 3.5.10 移除群成员

- **Method**: POST

- **Uri**: `/api/group/member/remove`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；当前不允许直接移除群主 |

- **RequestBody**:

  ```json
  {
    "groupId": 90002,
    "userId": 10011
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "c2e48a7f58f64d2d",
    "data": {
      "groupId": 90002,
      "groupName": "微友产品群",
      "groupAvatar": "https://weiyou.local/group/90002.png",
      "ownerUserId": 10001,
      "memberCount": 11,
      "muteAll": false,
      "joinPolicy": 0,
      "notice": "18:00 前同步一期接口联调进度。"
    }
  }
  ```

#### 3.5.11 转让群主

- **Method**: POST

- **Uri**: `/api/group/owner/transfer`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；目标成员必须已在群中 |

- **RequestBody**:

  ```json
  {
    "groupId": 90002,
    "targetUserId": 10011
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "88b7c0c35d5a4974",
    "data": {
      "groupId": 90002,
      "groupName": "微友产品群",
      "groupAvatar": "https://weiyou.local/group/90002.png",
      "ownerUserId": 10011,
      "memberCount": 12,
      "muteAll": false,
      "joinPolicy": 0,
      "notice": "18:00 前同步一期接口联调进度。"
    }
  }
  ```

#### 3.5.12 退出群聊

- **Method**: POST

- **Uri**: `/api/group/leave`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；群主退出时会自动转让或解散群聊 |

- **RequestBody**:

  ```json
  {
    "groupId": 90002
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "c8d8c2d86f1b44ac",
    "data": {
      "groupId": 90002,
      "left": true,
      "newOwnerUserId": 10011,
      "deleted": false
    }
  }
  ```

### 3.6 朋友圈

#### 3.6.1 查询朋友圈时间线

- **Method**: GET

- **Uri**: `/api/moment/timeline`

- **Param**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | cursor | String | 游标 | N | 当前 demo；未实际使用 |
  | pageSize | int | 每页数量 | N | 默认 `20` |

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "f9ef862b37a44098",
    "data": {
      "list": [
        {
          "momentId": 30001,
          "authorUserId": 10002,
          "nickname": "阿泽",
          "avatar": "https://weiyou.local/avatar/10002.png",
          "content": "今天把需求评审过了一遍。",
          "mediaUrls": [
            "https://weiyou.local/moment/30001-1.png"
          ],
          "likeCount": 12,
          "commentCount": 3,
          "createdAt": "2026-05-11T10:00:00Z"
        },
        {
          "momentId": 30002,
          "authorUserId": 10003,
          "nickname": "小林",
          "avatar": "https://weiyou.local/avatar/10003.png",
          "content": "版本节奏推进中。",
          "mediaUrls": [],
          "likeCount": 6,
          "commentCount": 1,
          "createdAt": "2026-05-11T09:30:00Z"
        }
      ],
      "pageNo": 1,
      "pageSize": 20,
      "total": 2,
      "hasMore": false,
      "nextCursor": null
    }
  }
  ```

#### 3.6.2 发布朋友圈

- **Method**: POST

- **Uri**: `/api/moment/create`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；`visibleScope` 当前未实际使用 |

- **RequestBody**:

  ```json
  {
    "content": "今天的晚霞很好看",
    "mediaUrls": [
      "https://weiyou.local/moment/upload/sky-1.jpg"
    ],
    "visibleScope": "ALL"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "948d20458df04284",
    "data": {
      "momentId": 30003,
      "authorUserId": 10001,
      "nickname": "微友产品体验官",
      "avatar": "https://weiyou.local/avatar/10001.png",
      "content": "今天的晚霞很好看",
      "mediaUrls": [
        "https://weiyou.local/moment/upload/sky-1.jpg"
      ],
      "likeCount": 0,
      "commentCount": 0,
      "createdAt": "2026-05-11T18:30:00Z"
    }
  }
  ```

#### 3.6.3 点赞/取消点赞

- **Method**: POST

- **Uri**: `/api/moment/like`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；当前返回最新点赞数 |

- **RequestBody**:

  ```json
  {
    "momentId": 30001,
    "action": "LIKE"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "e62fc0f9419d43d6",
    "data": {
      "momentId": 30001,
      "action": "LIKE",
      "likeCount": 13
    }
  }
  ```

#### 3.6.4 新增朋友圈评论

- **Method**: POST

- **Uri**: `/api/moment/comment/create`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；支持回复指定评论 |

- **RequestBody**:

  ```json
  {
    "momentId": 30001,
    "content": "这个功能已经可联调。",
    "replyCommentId": 50001
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "0f84fb2d5e164f5b",
    "data": {
      "momentId": 30001,
      "commentId": 50002,
      "content": "这个功能已经可联调。",
      "replyCommentId": 50001,
      "replyUserId": 10001,
      "commentCount": 4,
      "createdAt": "2026-05-13T10:20:00Z"
    }
  }
  ```

#### 3.6.5 删除朋友圈评论

- **Method**: POST

- **Uri**: `/api/moment/comment/delete`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；按评论 ID 删除 |

- **RequestBody**:

  ```json
  {
    "momentId": 30001,
    "commentId": 50002
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "f2f0de3e0b5d4d47",
    "data": {
      "commentId": 50002,
      "momentId": 30001,
      "commentCount": 3
    }
  }
  ```

### 3.7 钱包与支付

#### 3.7.1 查询钱包概览

- **Method**: GET

- **Uri**: `/api/wallet/overview`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；优先查库，空则返回 demo 数据 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "65952677a6724955",
    "data": {
      "availableBalanceFen": 128000,
      "frozenBalanceFen": 0,
      "realnameStatus": 1,
      "payPasswordStatus": 1,
      "bankCardCount": 3
    }
  }
  ```

#### 3.7.2 查询账单列表

- **Method**: GET

- **Uri**: `/api/wallet/bill/list`

- **Param**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | type | String | 账单类型 | N | 当前支持按 `transfer/income/red_packet` 等类型筛选 |
  | pageNo | int | 页码 | N | 默认 `1` |
  | startDate | String | 开始日期 | N | 支持 `yyyy-MM-dd` / ISO 时间格式 |
  | endDate | String | 结束日期 | N | 支持 `yyyy-MM-dd` / ISO 时间格式 |

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "d61adf0e2338456f",
    "data": {
      "list": [
        {
          "billId": 60001,
          "transactionNo": "TX202605110001",
          "billType": "transfer",
          "amountFen": 8800,
          "incomeExpenseType": 2,
          "billTitle": "转账给阿泽",
          "billTime": "2026-05-11T09:00:00Z"
        },
        {
          "billId": 60002,
          "transactionNo": "TX202605110002",
          "billType": "income",
          "amountFen": 12000,
          "incomeExpenseType": 1,
          "billTitle": "收到转账",
          "billTime": "2026-05-11T09:50:00Z"
        }
      ],
      "pageNo": 1,
      "pageSize": 20,
      "total": 2,
      "hasMore": false,
      "nextCursor": null
    }
  }
  ```

#### 3.7.3 创建转账

- **Method**: POST

- **Uri**: `/api/wallet/transfer/create`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；不能给自己转账，金额需大于 `0`，余额需充足 |

- **RequestBody**:

  ```json
  {
    "targetUserId": 10002,
    "amountFen": 5200,
    "remark": "午餐AA"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "9a8546cc32da4568",
    "data": {
      "transactionId": 92001,
      "transactionNo": "TX202605110101",
      "status": 1,
      "amountFen": 5200
    }
  }
  ```

#### 3.7.4 创建红包

- **Method**: POST

- **Uri**: `/api/wallet/red-packet/create`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；总额需大于等于份数；发包钱包必须存在 |

- **RequestBody**:

  ```json
  {
    "amountFen": 1000,
    "count": 5,
    "type": 1,
    "greeting": "恭喜发财",
    "groupId": 90002
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "c6430f432be841b8",
    "data": {
      "redPacketId": 93001,
      "redPacketNo": "RP202605110001",
      "senderUserId": 10001,
      "packetType": 1,
      "totalAmountFen": 1000,
      "count": 5,
      "status": 0,
      "expireAt": "2026-05-12T10:00:00Z"
    }
  }
  ```

#### 3.7.5 打开红包

- **Method**: POST

- **Uri**: `/api/wallet/red-packet/open`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；重复领取直接返回原记录，红包过期/抢完/已结束会返回业务异常 |

- **RequestBody**:

  ```json
  {
    "redPacketId": 93001
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "3a1a6713db844ad0",
    "data": {
      "redPacketNo": "RP202605110001",
      "receiverUserId": 10002,
      "receiveAmountFen": 188,
      "rankNo": 2,
      "status": 1
    }
  }
  ```

### 3.8 发现能力

#### 3.8.1 查询发现页能力

- **Method**: GET

- **Uri**: `/api/feature/discovery`

- **Param**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | cityCode | String | 城市编码 | N | 当前 demo 返回 |

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "8ea457bd9b8b4181",
    "data": [
      {
        "featureCode": "moment",
        "featureName": "朋友圈",
        "iconUrl": "https://weiyou.local/icons/moment.png",
        "routePath": "/pages/moments/index",
        "enabled": true
      },
      {
        "featureCode": "scan",
        "featureName": "扫一扫",
        "iconUrl": "https://weiyou.local/icons/scan.png",
        "routePath": "/pages/scan/index",
        "enabled": true
      },
      {
        "featureCode": "search",
        "featureName": "搜一搜",
        "iconUrl": "https://weiyou.local/icons/search.png",
        "routePath": "/pages/search/index",
        "enabled": true
      }
    ]
  }
  ```

#### 3.8.2 查询功能中心能力

- **Method**: GET

- **Uri**: `/api/feature/workbench`

- **Param**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | scene | String | 场景值 | N | 当前 demo 返回 |

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "8ea2d1d7db2544f1",
    "data": [
      {
        "featureCode": "miniapp",
        "featureName": "小程序",
        "iconUrl": "https://weiyou.local/icons/miniapp.png",
        "routePath": "/pages/miniapp/recent",
        "enabled": true
      },
      {
        "featureCode": "wallet",
        "featureName": "钱包",
        "iconUrl": "https://weiyou.local/icons/wallet.png",
        "routePath": "/pages/wallet/index",
        "enabled": true
      }
    ]
  }
  ```

### 3.9 扫一扫

#### 3.9.1 解析扫码结果

- **Method**: POST

- **Uri**: `/api/scan/resolve`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；当前固定返回用户二维码解析结果 |

- **RequestBody**:

  ```json
  {
    "scanCode": "weiyou://qrcode/user/10002",
    "scene": "scan_add_friend"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "1905c85e1b664d30",
    "data": {
      "scanType": "user_qrcode",
      "scanToken": "scan-token-demo",
      "title": "微友名片",
      "actionType": "open_profile",
      "payload": {
        "userId": 10002,
        "routePath": "/pages/contacts/profile?id=10002"
      }
    }
  }
  ```

### 3.10 App 启动

#### 3.10.1 查询启动配置

- **Method**: GET

- **Uri**: `/api/app/bootstrap`

- **Param**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | clientType | String | 客户端类型 | Y | 如 `iOS`、`Android` |
  | version | String | 当前版本号 | Y | 如 `0.1.0` |

- **Header**: NONE

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "6592d0f4f43f48c9",
    "data": {
      "latestVersion": "0.1.0",
      "forceUpgrade": false,
      "agreementVersion": "2026.05",
      "noticeText": "welcome to weiyou"
    }
  }
  ```

### 3.11 媒体

#### 3.11.1 获取上传策略

- **Method**: POST

- **Uri**: `/api/media/upload/policy`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；当前仅生成对象存储 key |

- **RequestBody**:

  ```json
  {
    "bizType": "moment",
    "fileName": "photo.jpg",
    "contentType": "image/jpeg"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "f37f00fb1d0647f3",
    "data": {
      "uploadUrl": "https://oss.weiyou.local/upload",
      "storageKey": "moment/2026/05/11/photo.jpg",
      "method": "PUT",
      "expireAt": "2026-05-11T23:59:59Z"
    }
  }
  ```

#### 3.11.2 本地文件上传

- **Method**: POST

- **Uri**: `/api/media/upload/local`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |
  | Content-Type | multipart/form-data | 文件上传表单 | Y | 通过 multipart 上传 |

- **RequestBody**:

  `multipart/form-data`

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | file | Binary | 上传文件 | Y | 图片、文件均可 |
  | bizType | String | 业务类型 | N | 默认 `moment`，聊天可传 `chat-image` / `chat-file` |

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "abc2af11bbca4c18",
    "data": {
      "mediaId": 1715588812345,
      "bizType": "chat-image",
      "url": "http://localhost:8080/uploads/chat-image/20260515/6fd0c2d6-8b1f-4a4d-a6ab-11a1a1a1a1a1.jpg",
      "coverUrl": "http://localhost:8080/uploads/chat-image/20260515/6fd0c2d6-8b1f-4a4d-a6ab-11a1a1a1a1a1.jpg",
      "originName": "photo.jpg",
      "size": 182736,
      "uploadedAt": "2026-05-15T10:20:00"
    }
  }
  ```

### 3.12 搜索

#### 3.12.1 搜索联想词

- **Method**: GET

- **Uri**: `/api/search/suggest`

- **Param**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | keyword | String | 搜索关键字 | Y | 当前 demo 返回 |
  | bizType | String | 业务类型 | N | 当前 demo 返回 |

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "1e5d72c4fbd44cfa",
    "data": [
      "阿泽",
      "阿泽 朋友圈",
      "阿泽 小程序"
    ]
  }
  ```

#### 3.12.2 全局搜索

- **Method**: GET

- **Uri**: `/api/search/global`

- **Param**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | keyword | String | 搜索关键字 | Y | 当前 demo 返回 |
  | bizType | String | 业务类型 | N | 当前 demo 返回 |
  | pageNo | int | 页码 | N | 默认 `1` |

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "2e9325c4c663446a",
    "data": {
      "list": [
        {
          "bizType": "contact",
          "bizId": "10002",
          "title": "阿泽",
          "subtitle": "通讯录联系人",
          "cover": "https://weiyou.local/avatar/10002.png",
          "routePath": "/pages/contacts/profile?id=10002"
        },
        {
          "bizType": "official",
          "bizId": "20001",
          "title": "微友服务号",
          "subtitle": "公众号",
          "cover": "https://weiyou.local/official/service.png",
          "routePath": "/pages/official/detail?id=20001"
        }
      ],
      "pageNo": 1,
      "pageSize": 20,
      "total": 2,
      "hasMore": false,
      "nextCursor": null
    }
  }
  ```

### 3.13 通知

#### 3.13.1 查询通知列表

- **Method**: GET

- **Uri**: `/api/notice/list`

- **Param**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | type | String | 通知类型 | N | 当前 demo 返回 |
  | cursor | String | 游标 | N | 当前 demo；未实际分页 |

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "0432d1b96227471c",
    "data": {
      "list": [
        {
          "noticeId": 40001,
          "noticeType": "system",
          "title": "系统通知",
          "content": "欢迎使用微友后端骨架",
          "readStatus": 0,
          "createdAt": "2026-05-11T08:00:00Z"
        },
        {
          "noticeId": 40002,
          "noticeType": "moment",
          "title": "朋友圈提醒",
          "content": "阿泽评论了你的动态",
          "readStatus": 0,
          "createdAt": "2026-05-11T09:20:00Z"
        }
      ],
      "pageNo": 1,
      "pageSize": 20,
      "total": 2,
      "hasMore": false,
      "nextCursor": null
    }
  }
  ```

#### 3.13.2 标记通知已读

- **Method**: POST

- **Uri**: `/api/notice/read`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**:

  ```json
  {
    "noticeId": 40001
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "e42ea4ef8e3a4d45",
    "data": {
      "noticeId": 40001,
      "readStatus": 1
    }
  }
  ```

### 3.14 公众号

#### 3.14.1 查询公众号列表

- **Method**: GET

- **Uri**: `/api/official/account/list`

- **Param**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | keyword | String | 搜索关键字 | N | 当前按名称模糊匹配 |

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "8c2b56f9ee8e4325",
    "data": [
      {
        "officialId": 20001,
        "name": "微友服务号",
        "avatar": "https://weiyou.local/official/service.png",
        "intro": "微友官方服务、活动与消息通知入口",
        "verified": true,
        "followed": true
      },
      {
        "officialId": 20002,
        "name": "微友通知助手",
        "avatar": "https://weiyou.local/official/notice.png",
        "intro": "系统消息、版本更新与安全提醒",
        "verified": true,
        "followed": true
      }
    ]
  }
  ```

#### 3.14.2 查询公众号详情

- **Method**: GET

- **Uri**: `/api/official/account/detail`

- **Param**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | officialId | Long | 公众号 ID | Y | 当前 demo 返回 |

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "d361ca61b781472f",
    "data": {
      "officialId": 88001,
      "name": "微友服务号",
      "avatar": "https://weiyou.local/official/service.png",
      "intro": "微友官方服务、活动与消息通知入口",
      "verified": true,
      "followed": true
    }
  }
  ```

#### 3.14.3 查询公众号历史文章

- **Method**: GET

- **Uri**: `/api/official/article/history`

- **Param**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | officialId | Long | 公众号 ID | Y |  |
  | cursor | String | 游标 | N | 当前未实际参与分页 |

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "87f1d9c72fca4332",
    "data": {
      "list": [
        {
          "articleId": 300001,
          "officialId": 20001,
          "title": "微友版本更新周报",
          "summary": "本周完成登录、聊天、钱包与朋友圈主链路联调。",
          "cover": "https://weiyou.local/official/article-cover-1.png",
          "publishAt": "2026-05-13T09:00:00Z",
          "likeCount": 18
        }
      ],
      "pageNo": 1,
      "pageSize": 20,
      "total": 1,
      "hasMore": false,
      "nextCursor": null
    }
  }
  ```

#### 3.14.4 查询公众号文章详情

- **Method**: GET

- **Uri**: `/api/official/article/detail`

- **Param**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | articleId | Long | 文章 ID | Y |  |

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "2356dfe9a5e346d1",
    "data": {
      "articleId": 300001,
      "officialId": 20001,
      "title": "微友版本更新周报",
      "summary": "微友后端和 uni-app 前端已经打通登录、聊天、钱包、朋友圈、功能中心等核心链路。",
      "contentHtml": "<p>本周完成了登录态、HTTP 联调、WebSocket 实时消息...</p>",
      "cover": "https://weiyou.local/official/article-cover-1.png",
      "publishAt": "2026-05-13T09:00:00Z",
      "likeCount": 18
    }
  }
  ```

#### 3.14.5 点赞公众号文章

- **Method**: POST

- **Uri**: `/api/official/article/like`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**:

  ```json
  {
    "articleId": 300001,
    "action": "like"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "6372d31fdca54d70",
    "data": {
      "articleId": 300001,
      "action": "like",
      "likeCount": 19
    }
  }
  ```

#### 3.14.6 关注/取消关注公众号

- **Method**: POST

- **Uri**: `/api/official/account/follow`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**:

  ```json
  {
    "officialId": 20001,
    "action": "follow"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "e8bdad55a6534567",
    "data": {
      "officialId": 20001,
      "action": "follow",
      "followed": true
    }
  }
  ```

### 3.15 小程序

#### 3.15.1 查询最近使用小程序

- **Method**: GET

- **Uri**: `/api/miniapp/recent/list`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "cf6646b381b64af8",
    "data": [
      {
        "appId": "miniapp-demo-001",
        "appName": "微友商城",
        "iconUrl": "https://weiyou.local/miniapp/mall.png",
        "path": "/pages/index/index",
        "lastUsedAt": "2026-05-13T08:00:00Z",
        "favorite": true
      }
    ]
  }
  ```

#### 3.15.2 查询收藏小程序

- **Method**: GET

- **Uri**: `/api/miniapp/favorite/list`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "65abf57b24314959",
    "data": [
      {
        "appId": "miniapp-demo-001",
        "appName": "微友商城",
        "iconUrl": "https://weiyou.local/miniapp/mall.png",
        "path": "/pages/index/index",
        "lastUsedAt": "2026-05-13T08:00:00Z",
        "favorite": true
      }
    ]
  }
  ```

#### 3.15.3 打开小程序

- **Method**: POST

- **Uri**: `/api/miniapp/open`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；`path` 为空默认 `/pages/index/index` |

- **RequestBody**:

  ```json
  {
    "appId": "wx-demo-001",
    "path": "/pages/home/index",
    "scene": "from_chat"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "938b5226a16f44b9",
    "data": {
      "appId": "wx-demo-001",
      "appName": "微友商城",
      "sessionToken": "miniapp-session-demo-token",
      "path": "/pages/home/index",
      "expireAt": "2026-12-31T23:59:59Z",
      "favorite": false
    }
  }
  ```

#### 3.15.4 移除最近使用小程序

- **Method**: POST

- **Uri**: `/api/miniapp/recent/remove`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**:

  ```json
  {
    "appId": "miniapp-demo-001"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "4826d0c9d34b4403",
    "data": []
  }
  ```

#### 3.15.5 收藏/取消收藏小程序

- **Method**: POST

- **Uri**: `/api/miniapp/favorite/toggle`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**:

  ```json
  {
    "appId": "miniapp-demo-001",
    "action": "favorite"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "2f27de6ee0a1494f",
    "data": {
      "appId": "miniapp-demo-001",
      "action": "favorite",
      "favorite": true
    }
  }
  ```

### 3.16 反馈

#### 3.16.1 提交反馈

- **Method**: POST

- **Uri**: `/api/feedback/create`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；当前 no-op |

- **RequestBody**:

  ```json
  {
    "content": "聊天页面偶发白屏，请帮忙排查",
    "images": [
      "https://weiyou.local/feedback/shot-1.png"
    ],
    "contact": "13800138000"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "695f9df2efc24a1c",
    "data": null
  }
  ```

#### 3.16.2 创建收藏

- **Method**: POST

- **Uri**: `/api/collection/create`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；适用于聊天消息、图片、视频等快捷收藏 |

- **RequestBody**:

  ```json
  {
    "type": "note",
    "title": "聊天消息收藏",
    "cover": "",
    "summary": "今晚把接口联调说明同步给大家。"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "a3dc709ad4474ef4",
    "data": {
      "collectionId": 700010,
      "type": "note",
      "title": "聊天消息收藏",
      "cover": "",
      "summary": "今晚把接口联调说明同步给大家。",
      "createdAt": "2026-05-18T12:00:00Z"
    }
  }
  ```

### 3.17 IM WebSocket

#### 3.17.1 建立 WebSocket 连接

- **Method**: WEBSOCKET

- **Uri**: `ws://{host}:8081/ws/chat`

- **Param**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | token | String | accessToken | Y | 通过 query 传递，握手阶段校验 |
  | deviceId | String | 设备 ID | N | 不传则取 token 中的 deviceId |

- **Header**: NONE

- **RequestBody**: NONE

- **Response**:

  连接成功后，服务端首帧返回 `CONNECT_ACK`：

  ```json
  {
    "event": "CONNECT_ACK",
    "data": {
      "userId": 10001,
      "deviceId": "ios-iphone15pm-001",
      "sessionId": "ws-session-001",
      "connectedAt": "2026-05-11T10:00:00Z",
      "lastHeartbeatAt": "2026-05-11T10:00:00Z",
      "serverTime": "2026-05-11T10:00:00Z"
    }
  }
  ```

#### 3.17.2 WebSocket 心跳

- **Method**: WEBSOCKET

- **Uri**: `ws://{host}:8081/ws/chat`

- **Param**: NONE

- **Header**: NONE

- **RequestBody**:

  ```json
  {
    "event": "HEARTBEAT",
    "requestId": "hb-001"
  }
  ```

- **Response**:

  ```json
  {
    "event": "HEARTBEAT",
    "data": {
      "serverTime": "2026-05-11T10:05:00Z",
      "sessionId": "ws-session-001"
    }
  }
  ```

#### 3.17.3 发送消息事件

- **Method**: WEBSOCKET

- **Uri**: `ws://{host}:8081/ws/chat`

- **Param**: NONE

- **Header**: NONE

- **RequestBody**:

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

- **Response**:

  发送成功后，当前连接先收到 `MESSAGE_ACK`：

  ```json
  {
    "event": "MESSAGE_ACK",
    "data": {
      "requestId": "cmsg_70003",
      "conversationId": 90001,
      "messageId": 70003,
      "clientMsgId": "cmsg_70003",
      "sendStatus": 1,
      "sendTime": "2026-05-11T10:35:00Z",
      "serverTime": "2026-05-11T10:35:00Z"
    }
  }
  ```

#### 3.17.4 服务端消息广播事件

- **Method**: WEBSOCKET

- **Uri**: `ws://{host}:8081/ws/chat`

- **Param**: NONE

- **Header**: NONE

- **RequestBody**: NONE

- **Response**:

  消息持久化后，服务端会向会话参与者广播 `MESSAGE_RECEIVE`：

  ```json
  {
    "event": "MESSAGE_RECEIVE",
    "data": {
      "conversationId": 90001,
      "message": {
        "messageId": 70003,
        "clientMsgId": "cmsg_70003",
        "conversationId": 90001,
        "senderUserId": 10001,
        "msgType": 1,
        "content": {
          "text": "中午一起吃饭吗？"
        },
        "sendStatus": 1,
        "sendTime": "2026-05-11T10:35:00Z"
      }
    }
  }
  ```

#### 3.17.5 WebSocket 异常与兜底响应

- **Method**: WEBSOCKET

- **Uri**: `ws://{host}:8081/ws/chat`

- **Param**: NONE

- **Header**: NONE

- **RequestBody**:

  无效消息示例：

  ```json
  {
    "event": "MESSAGE_SEND",
    "requestId": "bad-001",
    "data": {
      "conversationId": null,
      "content": {}
    }
  }
  ```

- **Response**:

  参数不合法时，服务端返回 `EVENT_ACK`：

  ```json
  {
    "event": "EVENT_ACK",
    "data": {
      "requestId": "bad-001",
      "event": "MESSAGE_SEND",
      "sendStatus": 0,
      "message": "invalid message payload",
      "serverTime": "2026-05-11T10:36:00Z"
    }
  }
  ```

  未识别事件时，服务端返回通用 `EVENT_ACK`：

  ```json
  {
    "event": "EVENT_ACK",
    "data": {
      "requestId": "evt-001",
      "event": "CUSTOM_EVENT",
      "serverTime": "2026-05-11T10:36:30Z"
    }
  }
  ```

### 3.18 卡包

#### 3.18.1 查询卡包列表

- **Method**: GET

- **Uri**: `/api/card/list`

- **Param**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | cardType | String | 卡券类型 | N | 当前支持 `coupon/member/transport` |

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "1d3c1a0fb87f4c62",
    "data": [
      {
        "cardId": 800001,
        "cardType": "coupon",
        "title": "咖啡 8 折券",
        "provider": "微友咖啡馆",
        "expireText": "2026-12-31",
        "status": "未使用"
      },
      {
        "cardId": 800002,
        "cardType": "member",
        "title": "微友会员卡",
        "provider": "微友服务",
        "expireText": "长期有效",
        "status": "已激活"
      }
    ]
  }
  ```

#### 3.18.2 使用卡券

- **Method**: POST

- **Uri**: `/api/card/use`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**:

  ```json
  {
    "cardId": 800001
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "f448ba2f16754e52",
    "data": {
      "cardId": 800001,
      "cardType": "coupon",
      "title": "咖啡 8 折券",
      "provider": "微友咖啡馆",
      "expireText": "2026-12-31",
      "status": "已使用"
    }
  }
  ```

### 3.19 表情商店

#### 3.19.1 查询表情商店列表

- **Method**: GET

- **Uri**: `/api/emoji/store/list`

- **Param**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | categoryId | String | 分类 ID | N | 当前未细分分类 |
  | pageNo | int | 页码 | N | 当前返回全量列表 |

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**: NONE

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "c8155255ce9148b2",
    "data": [
      {
        "packageId": "emoji-pack-001",
        "title": "微友默认表情",
        "summary": "轻松沟通的一组常用表情",
        "downloaded": true,
        "active": true
      },
      {
        "packageId": "emoji-pack-002",
        "title": "办公摸鱼包",
        "summary": "适合群聊和同事斗图",
        "downloaded": false,
        "active": false
      }
    ]
  }
  ```

#### 3.19.2 下载表情包

- **Method**: POST

- **Uri**: `/api/emoji/package/download`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**:

  ```json
  {
    "packageId": "emoji-pack-002"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "7d3498d5175c4b17",
    "data": {
      "packageId": "emoji-pack-002",
      "title": "办公摸鱼包",
      "summary": "适合群聊和同事斗图",
      "downloaded": true,
      "active": false
    }
  }
  ```

#### 3.19.3 卸载表情包

- **Method**: POST

- **Uri**: `/api/emoji/package/remove`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录 |

- **RequestBody**:

  ```json
  {
    "packageId": "emoji-pack-002"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "0e26a344dcca47f6",
    "data": {
      "packageId": "emoji-pack-002",
      "title": "办公摸鱼包",
      "summary": "适合群聊和同事斗图",
      "downloaded": false,
      "active": false
    }
  }
  ```

#### 3.19.4 启用表情包

- **Method**: POST

- **Uri**: `/api/emoji/package/activate`

- **Param**: NONE

- **Header**:

  | key | value | 说明 | 是否必填 | 备注 |
  | --- | ----- | ---- | -------- | ---- |
  | Authorization | Bearer {accessToken} | 访问令牌 | Y | 需已登录；仅已下载表情包可启用 |

- **RequestBody**:

  ```json
  {
    "packageId": "emoji-pack-002"
  }
  ```

- **Response**:

  ```json
  {
    "code": 0,
    "message": "ok",
    "traceId": "46d58d563a1544da",
    "data": [
      {
        "packageId": "emoji-pack-001",
        "title": "微友默认表情",
        "summary": "轻松沟通的一组常用表情",
        "downloaded": true,
        "active": false
      },
      {
        "packageId": "emoji-pack-002",
        "title": "办公摸鱼包",
        "summary": "适合群聊和同事斗图",
        "downloaded": true,
        "active": true
      }
    ]
  }
  ```

## 4.通用错误码

| code | message 示例 | 说明 | 常见触发场景 |
| ---- | ------------ | ---- | ------------ |
| 0 | ok | 成功 | 请求处理成功 |
| 400 | invalid request | 参数校验或业务参数不合法 | `@Valid` 校验失败、金额非法、请求体缺字段 |
| 400 | cannot transfer to self | 不允许给自己转账 | `POST /api/wallet/transfer/create` |
| 400 | invalid transfer amount | 转账金额非法 | 转账金额为空或小于等于 0 |
| 400 | insufficient balance | 余额不足 | 转账、发红包扣款时余额不足 |
| 400 | invalid red packet amount | 红包金额非法 | 红包总金额为空或小于等于 0 |
| 400 | invalid red packet count | 红包个数非法 | 红包个数为空或小于等于 0 |
| 400 | red packet amount too small | 红包总金额过小 | 总金额小于红包个数 |
| 400 | red packet already finished | 红包已结束 | 红包状态不再是进行中 |
| 400 | red packet expired | 红包已过期 | 红包超过有效期 |
| 400 | red packet empty | 红包已被抢完 | 无剩余红包明细可领取 |
| 401 | mobile or password incorrect | 账号或密码错误 | 密码登录校验失败 |
| 401 | invalid token | token 无效 | accessToken / refreshToken 解析失败或已过期 |
| 401 | invalid token type | token 类型不匹配 | 用 accessToken 调刷新接口等 |
| 404 | wallet account not found | 钱包账户不存在 | 付款钱包未开通 |
| 404 | red packet not found | 红包不存在 | 打开不存在的红包 |
| 500 | internal error | 服务内部错误 | 未捕获异常 |

补充说明：

- 参数校验失败时，后端会优先返回第一个字段错误，格式通常为：`字段名 + 空格 + 校验消息`，例如 `mobile must not be blank`。
- WebSocket 握手阶段如果缺少 `token` 或 token 无效，握手直接返回 `401`，不会进入正常连接态。

## 5.关键字段与枚举说明

### 5.1 认证相关

| 字段 | 取值/类型 | 说明 | 备注 |
| ---- | --------- | ---- | ---- |
| accessToken | String | 访问令牌 | 默认有效期 7200 秒 |
| refreshToken | String | 刷新令牌 | 默认有效期 2592000 秒 |
| expireIn | Integer | accessToken 过期秒数 | 当前固定返回 `7200` |
| needDeviceVerify | Boolean | 是否需要设备二次验证 | 当前代码固定返回 `false` |
| online | Boolean | 设备在线状态 | 来源于设备登录态，`true/false` |

### 5.2 会话与消息

| 字段 | 取值/类型 | 说明 | 备注 |
| ---- | --------- | ---- | ---- |
| conversationType | Integer | `1` 单聊，`2` 群聊 | 当前代码只显式用到这两类 |
| msgType | Integer | `1` 文本消息 | 其他类型当前未在代码中定义明确枚举，保留扩展 |
| sendStatus | Integer | `1` 发送成功 | WebSocket 非法消息回执中会出现 `0` |
| readStatus | Integer | `0` 未读，`1` 已读 | 当前历史消息返回值以示例/查询结果为准 |
| clientMsgId | String | 客户端消息 ID | HTTP 接口由请求体传入；WebSocket 默认复用 `requestId` |

### 5.3 群聊相关

| 字段 | 取值/类型 | 说明 | 备注 |
| ---- | --------- | ---- | ---- |
| muteAll | Boolean | 是否全员禁言 | `true/false` |
| joinPolicy | Integer | 入群策略 | 当前代码仅返回 `0`，未定义更细枚举常量 |

### 5.4 钱包与支付

| 字段 | 取值/类型 | 说明 | 备注 |
| ---- | --------- | ---- | ---- |
| incomeExpenseType | Integer | `1` 收入，`2` 支出 | 账单方向标记 |
| transactionStatus | Integer | `1` 成功 | 当前落库只看到成功态 |
| packetStatus | Integer | `0` 进行中，`1` 已领完/已结束，`2` 已过期 | 红包状态 |
| payPasswordStatus | Integer | `0` 未设置，`1` 已设置 | 钱包安全状态 |
| realnameStatus | Integer | `0` 未实名，`1` 已实名 | 钱包/账号实名状态 |
| packetType | Integer | 红包类型 | 当前接口接收整型，但代码未定义明确枚举说明 |

### 5.5 其他补充字段

| 字段 | 取值/类型 | 说明 | 备注 |
| ---- | --------- | ---- | ---- |
| status | Integer | 好友申请状态 | 当前 demo 数据返回 `0`，可理解为待处理 |
| dynamic | Boolean | 是否动态二维码 | `true` 动态码，`false` 普通码 |
| enabled | Boolean | 能力是否启用 | 发现页/功能中心入口开关 |

## 6.联调注意事项

1. HTTP 接口统一走 `http://{host}:8080/api`，WebSocket 连接走 `ws://{host}:8081/ws/chat`，两者端口不同。
2. 当前部分模块仍为 demo/stub/no-op 实现，适合前端联调页面与流程，不适合作为生产业务判定依据。
3. `contact`、`feature`、`notice`、`official`、`search` 等模块目前多数返回演示数据，筛选参数不一定真实生效。
4. `chat` 模块 HTTP 与 WebSocket 并存，建议前端采用“HTTP 拉历史 + WebSocket 收实时”的方式联调。
5. `wallet` 模块是当前最接近真实业务的模块之一，但仍缺少幂等控制、支付密码校验、风控校验、交易状态流转等生产级能力。
6. `register`、`login/sms`、`sms/send` 当前未真正校验短信验证码；联调时应避免把该逻辑误认为已完成。
7. 若需要前端严格按枚举渲染，建议下一步把文档中的“推断枚举”沉淀为后端常量或字典接口，避免前后端语义偏差。
