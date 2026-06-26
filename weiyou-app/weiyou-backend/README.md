# 微友后端骨架

当前目录提供 `微友` 的 Java 后端多模块脚手架，目标是先跑通账号、通讯录、聊天、朋友圈、钱包等核心域的基础工程组织。

## 目录

```text
weiyou-backend/
  pom.xml
  weiyou-common/
  weiyou-modules/
  weiyou-boot/
```

## 启动说明

- `weiyou-app-server`: `http://localhost:8080/api`
- `weiyou-im-gateway`: `ws://localhost:8081/ws/chat`
- `weiyou-gateway`: `http://localhost:8090`

## 推荐命令

```bash
mvn -pl weiyou-boot/weiyou-app-server -am spring-boot:run
mvn -pl weiyou-boot/weiyou-im-gateway -am spring-boot:run
```

## Docker Compose

在 `weiyou-app` 目录执行：

```bash
docker compose up --build
```

- `app-server`: `http://localhost:8080/api`
- `im-gateway`: `ws://localhost:8081/ws/chat`
- `gateway`: `http://localhost:8090`
- `frontend`: `http://localhost:8088`
- 推荐前端统一通过 `gateway` 或 `frontend` 反向代理访问 `/api` 与 `/ws`

Smoke check：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-check.ps1
```

可选负向鉴权校验：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-check.ps1 -IncludeNegativeChecks
```

可选限流命中校验：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-check.ps1 -IncludeRateLimitChecks
```

- 当前 smoke check 已覆盖登录、资料、通讯录、发现页、功能中心、公众号关注/文章点赞、小程序收藏/最近使用移除、会话、图片上传、朋友圈图文发布/评论、钱包、转账、红包和 WebSocket 实时消息
- 对关注、点赞、收藏、评论等写操作，smoke check 会追加状态回查，确认读接口已同步反映更新结果
- 也会验证取消关注、取消收藏、取消点赞后的反向状态回查
- 同时会校验公众号列表、小程序收藏列表和朋友圈评论内容是否能在列表类读接口中正确回查
- 也会校验功能中心返回的公众号列表、小程序最近使用、小程序收藏列表入口路由配置
- 同时会校验搜一搜建议、综合搜索与扫一扫解析接口
- 也会校验通知已读和设备列表/设备下线链路
- 通讯录模块已补充好友搜索、发起申请、查看新的朋友与处理申请的联调能力
- smoke check 已覆盖联系人搜索、发起好友申请与处理申请状态回查
- 群聊列表接口 `/group/my/list` 已接通，便于前端查看群聊概览
- 功能模块已补充收藏、卡包、表情等个人中心接口，用于前端联调个人高频入口
- 群聊模块已补充群聊列表、建群、群详情、邀请成员等联调能力
- 负向校验会额外检查 gateway 对未带 token 的 API 与 WebSocket 请求返回未授权
- 限流校验会额外检查 gateway 对钱包接口的 Redis 限流命中返回 `429`
- 后端当前已补充 `gateway` 限流、Redis 路由器、WebSocket 处理器等测试，执行 `mvn test` 可回归核心链路

## 认证与数据库

- 登录接口会返回 `accessToken` 和 `refreshToken`。
- 受保护接口需带 `Authorization: Bearer <accessToken>`。
- 刷新令牌使用 `/api/auth/token/refresh`。
- IM 长连接使用 `ws://localhost:8081/ws/chat?token=<accessToken>&deviceId=<deviceId>`。
- 演示账号见 `weiyou-app/weiyou-backend/sql/README.md`。
- 本地联调请求脚本见 `weiyou-app/weiyou-backend/http/weiyou-local.http`。
- PowerShell curl 联调脚本见 `weiyou-app/weiyou-backend/http/weiyou-local-curl.ps1`。
- Postman collection 见 `weiyou-app/weiyou-backend/postman/weiyou-local.postman_collection.json`。
- Postman environment 见 `weiyou-app/weiyou-backend/postman/weiyou-local.postman_environment.json`。
- 默认数据库与 Redis 连接可通过以下环境变量覆盖：

```bash
WEIYOU_DB_URL=jdbc:mysql://localhost:3306/weiyou_account?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
WEIYOU_DB_USERNAME=root
WEIYOU_DB_PASSWORD=root
WEIYOU_REDIS_HOST=localhost
WEIYOU_REDIS_PORT=6379
```

- IM 在线会话元数据会同步写入 Redis
- IM 消息接收事件会通过 Redis Pub/Sub 做跨实例路由
- 钱包转账、发红包会使用 Redis 做短时幂等防重
- 朋友圈图片发布现在会先走 `/api/media/upload/local` 本地上传，再以返回的图片 URL 创建动态
- Gateway 会统一补 `X-Trace-Id`、记录访问日志，并对受保护接口做基础 Bearer Token 校验
- Gateway 还会对 `/api/**` 与 `/ws/**` 做基础 Redis 限流，默认每 60 秒每个客户端 120 次请求
- Gateway 也支持按接口粒度细化限流规则，例如登录、钱包、WebSocket 可配置更严格阈值
- 限流身份维度支持 `IP`、`TOKEN`、`USER_ID`、`AUTO`，可按规则单独配置
