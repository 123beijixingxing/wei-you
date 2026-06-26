# 微友一键启动

## Docker Compose

在 `weiyou-app` 目录执行：

```bash
docker compose up --build
```

启动后可执行 smoke check：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-check.ps1
```

可选负向校验：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-check.ps1 -IncludeNegativeChecks
```

可选限流命中校验：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-check.ps1 -IncludeRateLimitChecks
```

- 该脚本会检查前端首页、gateway 健康、登录、资料、通讯录、发现页、功能中心、公众号关注/文章点赞、小程序收藏/最近使用移除、图片上传、朋友圈图文发布/评论、转账、红包以及 WebSocket 收发
- 对关注、点赞、收藏、评论这类动作，脚本还会追加一次状态回查，确认后端读接口已反映最新结果
- 脚本还会验证取消关注、取消收藏、取消点赞后的反向状态回查
- 也会检查公众号列表、小程序收藏列表以及朋友圈评论内容是否能在读接口中回查到
- 还会校验功能中心里公众号列表、小程序最近使用、小程序收藏列表等入口路由是否配置正确
- 也会校验搜一搜建议、综合搜索和扫一扫解析链路
- 还会校验通知已读与设备列表/设备下线这类账户侧辅助能力
- 可继续通过新的朋友、添加朋友页面联调好友申请链路
- smoke check 也已覆盖联系人搜索、发起好友申请与处理申请链路
- 通讯录现在支持添加朋友、新的朋友与申请处理联调
- 群聊列表页也已接通，可从通讯录入口查看群聊概要
- 现在也支持建群、查看群详情和邀请成员的联调链路
- 个人中心常用入口已补齐收藏、卡包、表情等联调页面
- 个人二维码与状态设置链路也已接通，可通过 smoke check 回查状态更新结果
- 设置中心也已接通真实开关项读写与回查
- 通知设置与隐私设置开关也会在 smoke check 中做读写回查
- 加 `-IncludeNegativeChecks` 后还会校验受保护接口未带 token 返回 `401`，以及未带 token 的 WebSocket 握手被拒绝
- 加 `-IncludeRateLimitChecks` 后会额外验证 gateway 对钱包接口的限流命中返回 `429`

## 服务端口

- 前端 H5：`http://localhost:8088`
- Gateway：`http://localhost:8090`
- App API：`http://localhost:8080/api`
- IM WebSocket：`ws://localhost:8081/ws/chat`
- MySQL：`localhost:3306`
- Redis：`localhost:6379`

## 说明

- MySQL 启动时会自动执行 `../docs/需求设计文档/微友数据库DDL初稿.sql`
- 也会自动执行 `weiyou-backend/sql/02-init-demo-data.sql`
- 演示账号：`13800000001 / 123456`
- Redis 会用于 IM 在线会话元数据、跨实例消息路由以及钱包操作幂等控制
- Gateway 会统一接管 `/api` 与 `/ws`，并提供基础限流、鉴权和 trace 透传
- 通过 `http://localhost:8088` 访问前端时，`/api` 与 `/ws` 会自动反向代理到 gateway

## 本地联调脚本

当本机没有 Docker、MySQL、Redis 的完整环境时，可使用本地联调脚本启动备用端口模式：

启动：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start-local-demo.ps1
```

停止：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\stop-local-demo.ps1
```

本地备用端口模式下的 smoke check 示例：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-check.ps1 -GatewayBaseUrl "http://localhost:19090" -FrontendUrl "http://localhost:19088" -WsBaseUrl "ws://localhost:19090/ws/chat"
```
