# 微友本地联调 SQL

执行顺序：

1. 先执行 `docs/需求设计文档/微友数据库DDL初稿.sql`
2. 再执行 `weiyou-app/weiyou-backend/sql/02-init-demo-data.sql`

默认演示账号：

- 手机号：`13800000001`
- 密码：`123456`
- 设备 ID 示例：`device-demo-android-10001`

备用账号：

- 手机号：`13800000002`
- 密码：`123456`

可直接联调的演示数据：

- 单聊会话：`90001`
- 群聊会话：`90002`
- 红包 ID：`81001`

说明：

- 当前脚手架使用单数据源连接同一 MySQL 实例下的多个逻辑库，因此 SQL 中都使用了 `库名.表名` 形式。
- 登录成功后可用返回的 `accessToken` 调用 HTTP 接口，也可用于连接 WebSocket：

```text
ws://localhost:8081/ws/chat?token=<accessToken>&deviceId=<deviceId>
```
