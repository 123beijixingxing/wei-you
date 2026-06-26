# 微友 Java 后端模块与数据库设计

## 1. 文档说明

本文档用于在 `docs/需求设计文档/微友开发需求文档.md` 和 `docs/需求设计文档/微友前端页面与接口清单.md` 的基础上，继续细化 `Java` 后端工程组织方式、模块边界和核心数据库表结构，供架构设计、后端开发、接口联调、测试设计和后续拆服务使用。

本文档默认采用以下实现口径：

- 前端：`uni-app + Vue 3 + Pinia`
- 后端：`Java 21 + Spring Boot 3 + Spring Cloud Gateway + Spring Security`
- 存储：`MySQL 8 + Redis + Elasticsearch + Object Storage`
- 实时链路：`Netty/WebSocket + Kafka/RocketMQ`
- 一期策略：`模块化单体 + 独立 IM 网关`

## 2. 后端建设策略

### 2.1 分阶段架构口径

- 一期优先交付统一 App API、账号中心、关系链、聊天、朋友圈、基础发现页、设置中心、媒体服务。
- 一期后端以 Maven 多模块组织代码，但对外可以先部署为少量进程，降低研发和运维成本。
- 二期将高并发和高一致性模块拆分，包括 `im-service`、`wallet-service`、`content-service`、`search-service`。
- 三期补齐开放平台、直播、小程序平台、推荐系统、审核风控、运营后台。

### 2.2 一期建议部署形态

| 进程 | 作用 | 说明 |
| --- | --- | --- |
| `weiyou-gateway` | HTTP 网关 | 统一鉴权、路由、限流、灰度、日志追踪 |
| `weiyou-app-server` | App 业务主服务 | 承载账号、用户、关系链、朋友圈、发现、设置等 REST 接口 |
| `weiyou-im-gateway` | IM 长连接服务 | 负责 WebSocket、在线状态、消息 ACK、通话信令 |
| `weiyou-admin-server` | 运营后台服务 | 运营、审核、风控、配置中心 |
| `weiyou-job` | 定时任务服务 | 对账、补偿、清理、延迟任务、数据聚合 |

### 2.3 二期建议拆分形态

- `account-service`：登录注册、设备、实名、安全、隐私
- `relation-service`：好友、标签、黑名单、群成员关系
- `im-service`：会话、消息、回执、撤回、离线投递
- `moment-service`：朋友圈动态、评论、点赞、可见范围
- `wallet-service`：零钱、银行卡、账单、转账、红包
- `ecosystem-service`：公众号、小程序、视频号、开放能力
- `media-service`：上传、转码、缩略图、语音转写
- `audit-risk-service`：内容审核、设备风控、交易风控
- `search-service`：综合搜索、聊天搜索、文章搜索、联想词

## 3. Java 工程目录设计

### 3.1 推荐 Maven 多模块结构

```text
weiyou-backend/
  pom.xml
  weiyou-bom/
  weiyou-common/
    weiyou-common-core/
    weiyou-common-web/
    weiyou-common-security/
    weiyou-common-redis/
    weiyou-common-mq/
    weiyou-common-storage/
    weiyou-common-search/
    weiyou-common-mybatis/
    weiyou-common-id/
  weiyou-starter/
    weiyou-starter-web/
    weiyou-starter-security/
    weiyou-starter-logging/
    weiyou-starter-swagger/
  weiyou-modules/
    weiyou-module-auth/
    weiyou-module-user/
    weiyou-module-relation/
    weiyou-module-chat/
    weiyou-module-group/
    weiyou-module-moment/
    weiyou-module-wallet/
    weiyou-module-feature/
    weiyou-module-official/
    weiyou-module-miniapp/
    weiyou-module-channel/
    weiyou-module-notice/
    weiyou-module-media/
    weiyou-module-search/
    weiyou-module-audit/
    weiyou-module-feedback/
  weiyou-boot/
    weiyou-gateway/
    weiyou-app-server/
    weiyou-im-gateway/
    weiyou-admin-server/
    weiyou-job/
  docs/
```

### 3.2 启动模块职责

| 模块 | 作用 | 依赖业务模块 |
| --- | --- | --- |
| `weiyou-gateway` | 统一入口、JWT 校验、限流、路由转发、灰度发布 | 无直接业务依赖 |
| `weiyou-app-server` | App 业务 REST API 聚合层 | `auth`、`user`、`relation`、`chat`、`group`、`moment`、`wallet`、`feature`、`notice`、`media` |
| `weiyou-im-gateway` | WebSocket、在线状态、消息分发、ACK、音视频信令 | `chat`、`group`、`notice`、`auth` |
| `weiyou-admin-server` | 运营后台、审核后台、配置中心、数据管理 | `audit`、`wallet`、`official`、`miniapp`、`channel`、`notice` |
| `weiyou-job` | 定时任务、延迟任务、补偿任务、对账、索引修复 | `wallet`、`moment`、`chat`、`search`、`media` |

### 3.3 业务模块职责

| 模块 | 包名建议 | 核心职责 | 主要依赖 |
| --- | --- | --- | --- |
| `weiyou-module-auth` | `com.weiyou.auth` | 注册登录、JWT、RefreshToken、设备管理、实名安全 | MySQL、Redis、MQ |
| `weiyou-module-user` | `com.weiyou.user` | 用户资料、头像、二维码名片、状态、隐私设置 | MySQL、OSS |
| `weiyou-module-relation` | `com.weiyou.relation` | 好友关系、标签、黑名单、好友申请 | MySQL、Redis |
| `weiyou-module-chat` | `com.weiyou.chat` | 会话、消息、回执、撤回、草稿、同步序列 | MySQL、Redis、MQ、ES |
| `weiyou-module-group` | `com.weiyou.group` | 群信息、群成员、群公告、加群审批、二维码 | MySQL、Redis |
| `weiyou-module-moment` | `com.weiyou.moment` | 朋友圈发布、互动、时间线、可见范围 | MySQL、Redis、OSS、MQ |
| `weiyou-module-wallet` | `com.weiyou.wallet` | 零钱、账单、银行卡、转账、红包、支付安全 | MySQL、Redis、MQ |
| `weiyou-module-feature` | `com.weiyou.feature` | 发现页配置、功能中心配置、城市服务入口 | MySQL、Redis |
| `weiyou-module-official` | `com.weiyou.official` | 公众号、文章、关注关系、服务消息 | MySQL、ES |
| `weiyou-module-miniapp` | `com.weiyou.miniapp` | 小程序元数据、版本、最近使用、收藏 | MySQL、Redis |
| `weiyou-module-channel` | `com.weiyou.channel` | 视频号、直播、创作者内容 | MySQL、OSS、MQ、ES |
| `weiyou-module-notice` | `com.weiyou.notice` | 系统通知、服务通知、推送投递记录 | MySQL、MQ |
| `weiyou-module-media` | `com.weiyou.media` | 上传策略、媒体资源、转码任务、图片压缩 | MySQL、OSS、MQ |
| `weiyou-module-search` | `com.weiyou.search` | 联想词、综合搜索、热搜词 | ES、Redis |
| `weiyou-module-audit` | `com.weiyou.audit` | 内容审核、风险事件、敏感词、举报处理 | MySQL、MQ、OCR/ASR |
| `weiyou-module-feedback` | `com.weiyou.feedback` | 用户反馈、工单、回访记录 | MySQL、OSS |

### 3.4 单模块内部目录模板

建议每个业务模块都遵循同一套分层结构，降低团队协作成本。

```text
weiyou-module-chat/
  src/main/java/com/weiyou/chat/
    controller/
    app/
      service/
      assembler/
    domain/
      entity/
      service/
      repository/
      event/
    infra/
      persistence/
        mapper/
        po/
      mq/
      redis/
      ws/
      external/
    dto/
    vo/
    convert/
    enums/
    constant/
    config/
```

### 3.5 分层职责约定

| 层级 | 职责 | 说明 |
| --- | --- | --- |
| `controller` | 接口入参、鉴权声明、返回体组装 | 不写核心业务逻辑 |
| `app.service` | 用例编排、事务控制、跨模块协同 | 面向接口场景组织流程 |
| `domain.entity` | 核心领域对象 | 聚焦业务属性和业务规则 |
| `domain.service` | 领域规则实现 | 如撤回条件、红包拆分、好友关系判断 |
| `domain.repository` | 领域仓储接口 | 隔离持久化技术细节 |
| `infra.persistence` | Mapper、PO、SQL | 负责数据库落地 |
| `infra.mq/redis/ws` | 中间件接入 | 负责缓存、消息、长连接实现 |
| `dto/vo/convert` | 数据转换层 | 避免直接暴露数据库对象 |

### 3.6 公共能力模块建议

| 公共模块 | 作用 |
| --- | --- |
| `weiyou-common-core` | 通用异常、枚举、Result 返回体、分页模型、时间工具 |
| `weiyou-common-web` | MVC 配置、全局异常、参数校验、TraceId 注入 |
| `weiyou-common-security` | JWT 解析、权限注解、当前用户上下文 |
| `weiyou-common-redis` | Redis Key 定义、缓存模板、分布式锁封装 |
| `weiyou-common-mq` | MQ Producer/Consumer 基类、消息封装、重试策略 |
| `weiyou-common-storage` | OSS/MinIO 上传、签名 URL、媒体元数据封装 |
| `weiyou-common-search` | ES 索引模板、搜索请求模型、高亮封装 |
| `weiyou-common-id` | 雪花 ID 或号段 ID 生成器 |

## 4. 核心技术选型建议

### 4.1 框架与组件

- ORM 建议采用 `MyBatis-Plus`，复杂查询直接写 XML 或注解 SQL。
- 对象转换建议使用 `MapStruct`。
- 参数校验使用 `jakarta validation`。
- 分布式锁建议使用 `Redisson`。
- 定时任务建议使用 `XXL-JOB` 或 `Spring Scheduling + MQ 延迟队列`。
- API 文档建议使用 `SpringDoc OpenAPI`。

### 4.2 即时消息链路

- REST 接口负责历史消息、会话列表、媒体上传、消息搜索。
- `weiyou-im-gateway` 负责 WebSocket 长连接、在线用户路由、ACK、回执。
- `chat` 模块负责消息落库、会话聚合、离线补偿、同步序列。
- 发送链路建议为：客户端发送 -> IM 网关鉴权 -> 投递聊天服务 -> 持久化 -> MQ 分发 -> 推送接收方 -> ACK 返回发送方。

### 4.3 钱包交易链路

- 所有支付金额均使用 `BIGINT` 分为单位。
- 转账、红包、提现、充值必须使用幂等号。
- 交易主流程必须记录余额日志、交易流水、账单视图和风险事件。
- 对账任务由 `weiyou-job` 定时执行，异常单进入人工处理池。

## 5. 数据库设计总则

### 5.1 逻辑库划分

建议按业务域拆逻辑库。第一阶段可以部署在同一 MySQL 实例中，第二阶段按流量独立拆库。

| 逻辑库 | 说明 |
| --- | --- |
| `weiyou_account` | 账号、资料、设备、隐私、安全 |
| `weiyou_relation` | 好友、标签、黑名单、群关系 |
| `weiyou_im` | 会话、消息、回执、在线状态 |
| `weiyou_content` | 朋友圈、视频号、直播、公众号 |
| `weiyou_wallet` | 钱包、账单、红包、银行卡 |
| `weiyou_ecosystem` | 小程序、发现页配置、功能中心 |
| `weiyou_system` | 通知、反馈、媒体、审核、运营日志 |

### 5.2 统一字段约定

除纯关联表外，业务表建议统一具备以下字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `BIGINT` | 主键 ID |
| `created_at` | `DATETIME(3)` | 创建时间 |
| `updated_at` | `DATETIME(3)` | 更新时间 |
| `created_by` | `BIGINT` | 创建人 |
| `updated_by` | `BIGINT` | 更新人 |
| `is_deleted` | `TINYINT` | 逻辑删除标记，0 未删除，1 已删除 |
| `version` | `INT` | 乐观锁版本号，可选 |

### 5.3 命名规范

- 表名前缀统一为 `wy_`。
- 主实体表使用名词，如 `wy_user_account`、`wy_group_info`。
- 关系表使用 `relation`、`member`、`mapping`、`record`、`log`、`item` 等后缀。
- 状态类字段使用 `status`、`biz_status`、`audit_status` 区分。
- JSON 扩展字段统一使用 `ext_json`，避免大量零散备用字段。

### 5.4 分库分表建议

- `wy_message` 建议按 `conversation_id` 取模分 32 或 64 张表。
- `wy_message_receipt` 可跟随消息表同规则分表，减少跨表压力。
- `wy_wallet_transaction` 如交易量明显上升，可按月分表或按账号分库。
- `wy_moment_timeline` 可按 `owner_user_id` 水平分片。

### 5.5 索引设计原则

- 列表查询必须有联合索引支撑，避免全表扫描。
- 高频唯一约束必须落库，如手机号、微友号、`client_msg_id`、会话成员唯一键。
- 长文本内容不建普通索引，统一走 ES 或前缀索引。
- 删除标记参与查询时，建议放入联合索引尾部。

### 5.6 数据安全要求

- 手机号、身份证号、银行卡号建议密文存储，同时保留脱敏展示字段。
- 密码与支付密码必须强哈希，禁止可逆存储。
- 实名资料、OCR 结果、风控标签应隔离访问权限。
- 交易和实名相关表需保留完整审计日志。

## 6. 逻辑库与核心表清单

### 6.1 `weiyou_account`

| 表名 | 作用 | 关键字段 | 关键索引 |
| --- | --- | --- | --- |
| `wy_user_account` | 账号主表 | `user_id`、`mobile`、`password_hash`、`account_status` | `uk_mobile`、`uk_weiyou_no` |
| `wy_user_profile` | 用户资料表 | `user_id`、`nickname`、`avatar_url`、`signature` | `uk_user_id` |
| `wy_user_device` | 登录设备表 | `user_id`、`device_id`、`device_type`、`last_login_at` | `uk_device_id`、`idx_user_status` |
| `wy_user_login_log` | 登录日志 | `user_id`、`login_ip`、`login_city`、`login_result` | `idx_user_time` |
| `wy_user_privacy_setting` | 隐私配置 | `user_id`、`add_friend_policy`、`moment_policy` | `uk_user_id` |
| `wy_user_qrcode` | 个人二维码 | `user_id`、`qrcode_type`、`ticket`、`expire_at` | `uk_ticket` |
| `wy_user_status` | 用户状态 | `user_id`、`status_code`、`status_text`、`expire_at` | `uk_user_id` |
| `wy_user_realname` | 实名信息 | `user_id`、`real_name_enc`、`id_card_enc`、`audit_status` | `uk_user_id` |

### 6.2 `weiyou_relation`

| 表名 | 作用 | 关键字段 | 关键索引 |
| --- | --- | --- | --- |
| `wy_friend_relation` | 好友关系表 | `owner_user_id`、`friend_user_id`、`remark`、`source` | `uk_owner_friend` |
| `wy_friend_request` | 好友申请记录 | `from_user_id`、`to_user_id`、`apply_status` | `idx_to_status_time` |
| `wy_contact_tag` | 标签主表 | `owner_user_id`、`tag_name` | `idx_owner_name` |
| `wy_contact_tag_member` | 标签成员关系 | `tag_id`、`friend_user_id` | `uk_tag_friend` |
| `wy_blacklist` | 黑名单 | `owner_user_id`、`blocked_user_id` | `uk_owner_blocked` |
| `wy_address_book_import_task` | 通讯录导入任务 | `user_id`、`task_status`、`matched_count` | `idx_user_time` |

### 6.3 `weiyou_im`

| 表名 | 作用 | 关键字段 | 关键索引 |
| --- | --- | --- | --- |
| `wy_conversation` | 会话主表 | `conversation_id`、`conversation_type`、`biz_id`、`last_msg_id` | `uk_conversation_no` |
| `wy_conversation_user` | 用户会话关系表 | `conversation_id`、`user_id`、`unread_count`、`top_flag` | `uk_conversation_user`、`idx_user_sort` |
| `wy_message_00~63` | 消息分表 | `conversation_id`、`seq_no`、`sender_id`、`msg_type` | `uk_conv_seq`、`uk_client_msg` |
| `wy_message_receipt_00~63` | 消息回执分表 | `message_id`、`user_id`、`read_status` | `uk_msg_user` |
| `wy_message_recall_log` | 撤回记录 | `message_id`、`operator_id`、`recall_reason` | `uk_message_id` |
| `wy_chat_draft` | 草稿表 | `user_id`、`conversation_id`、`draft_content` | `uk_user_conversation` |
| `wy_online_session` | 在线会话 | `user_id`、`device_id`、`channel_id`、`online_status` | `uk_user_device` |
| `wy_push_task` | 离线推送任务 | `user_id`、`push_type`、`biz_id`、`push_status` | `idx_user_status_time` |

### 6.4 `weiyou_relation` 群聊子域

| 表名 | 作用 | 关键字段 | 关键索引 |
| --- | --- | --- | --- |
| `wy_group_info` | 群主表 | `group_id`、`group_name`、`owner_user_id`、`group_status` | `uk_group_no` |
| `wy_group_member` | 群成员关系 | `group_id`、`user_id`、`role_type`、`mute_until` | `uk_group_user` |
| `wy_group_notice` | 群公告 | `group_id`、`notice_content`、`published_at` | `uk_group_id` |
| `wy_group_join_request` | 入群申请 | `group_id`、`apply_user_id`、`audit_status` | `idx_group_status_time` |
| `wy_group_qrcode` | 群二维码 | `group_id`、`ticket`、`expire_at` | `uk_ticket` |

### 6.5 `weiyou_content`

| 表名 | 作用 | 关键字段 | 关键索引 |
| --- | --- | --- | --- |
| `wy_moment_post` | 朋友圈动态主表 | `moment_id`、`author_user_id`、`content_text`、`visible_type` | `idx_author_time` |
| `wy_moment_media` | 朋友圈媒体表 | `moment_id`、`media_type`、`media_url` | `idx_moment_sort` |
| `wy_moment_comment` | 评论表 | `moment_id`、`comment_user_id`、`reply_comment_id` | `idx_moment_time` |
| `wy_moment_like` | 点赞表 | `moment_id`、`user_id` | `uk_moment_user` |
| `wy_moment_visible_rule` | 可见范围规则表 | `moment_id`、`rule_type`、`target_user_id` | `idx_moment_rule` |
| `wy_moment_timeline` | 粉丝时间线表 | `owner_user_id`、`moment_id`、`sort_time` | `idx_owner_sort` |
| `wy_moment_notice` | 朋友圈提醒 | `target_user_id`、`moment_id`、`notice_type` | `idx_target_read` |
| `wy_channel_video` | 视频号视频主表 | `video_id`、`author_user_id`、`cover_url`、`publish_status` | `idx_author_publish_time` |
| `wy_channel_video_like` | 视频点赞表 | `video_id`、`user_id` | `uk_video_user` |
| `wy_live_room` | 直播间主表 | `room_id`、`anchor_user_id`、`room_status` | `idx_anchor_status` |
| `wy_live_room_member` | 直播间成员表 | `room_id`、`user_id`、`member_role` | `uk_room_user` |
| `wy_live_message` | 直播消息表 | `room_id`、`sender_user_id`、`message_type` | `idx_room_time` |
| `wy_official_account` | 公众号主表 | `official_id`、`name`、`verify_status` | `uk_account_no`、`idx_name` |
| `wy_official_article` | 公众号文章 | `official_id`、`article_title`、`publish_at` | `idx_official_publish_time` |
| `wy_official_follow_relation` | 关注关系 | `official_id`、`user_id` | `uk_official_user` |

### 6.6 `weiyou_wallet`

| 表名 | 作用 | 关键字段 | 关键索引 |
| --- | --- | --- | --- |
| `wy_wallet_account` | 钱包账户表 | `user_id`、`available_balance`、`frozen_balance` | `uk_user_id` |
| `wy_wallet_balance_log` | 余额变更日志 | `user_id`、`transaction_id`、`change_amount` | `idx_user_time` |
| `wy_wallet_transaction` | 交易流水主表 | `transaction_no`、`biz_type`、`payer_user_id`、`payee_user_id`、`amount_fen` | `uk_transaction_no`、`idx_user_time` |
| `wy_wallet_bill` | 账单展示表 | `user_id`、`transaction_no`、`bill_type`、`bill_time` | `idx_user_bill_time` |
| `wy_wallet_bank_card` | 银行卡绑定表 | `user_id`、`bank_card_no_enc`、`bank_code` | `idx_user_status` |
| `wy_wallet_transfer` | 转账记录 | `transaction_no`、`sender_user_id`、`receiver_user_id` | `uk_transaction_no` |
| `wy_red_packet` | 红包主表 | `red_packet_no`、`sender_user_id`、`total_amount_fen`、`packet_count` | `uk_red_packet_no` |
| `wy_red_packet_item` | 红包拆分明细 | `red_packet_no`、`item_no`、`amount_fen` | `uk_red_packet_item` |
| `wy_red_packet_receive` | 红包领取记录 | `red_packet_no`、`receiver_user_id`、`receive_amount_fen` | `uk_red_packet_user` |
| `wy_wallet_risk_event` | 钱包风控事件 | `user_id`、`transaction_no`、`risk_level` | `idx_user_risk_time` |

### 6.7 `weiyou_ecosystem`

| 表名 | 作用 | 关键字段 | 关键索引 |
| --- | --- | --- | --- |
| `wy_feature_discovery_config` | 发现页配置 | `city_code`、`feature_code`、`sort_no` | `idx_city_sort` |
| `wy_feature_workbench_config` | 功能中心配置 | `scene_code`、`feature_code`、`status` | `idx_scene_sort` |
| `wy_miniapp` | 小程序主表 | `app_id`、`app_name`、`developer_id`、`audit_status` | `uk_app_id` |
| `wy_miniapp_version` | 小程序版本表 | `app_id`、`version_no`、`publish_status` | `uk_app_version` |
| `wy_miniapp_recent` | 最近使用记录 | `user_id`、`app_id`、`last_used_at` | `uk_user_app` |
| `wy_miniapp_favorite` | 收藏记录 | `user_id`、`app_id` | `uk_user_app` |

### 6.8 `weiyou_system`

| 表名 | 作用 | 关键字段 | 关键索引 |
| --- | --- | --- | --- |
| `wy_notice` | 系统通知表 | `notice_type`、`target_user_id`、`biz_id`、`read_status` | `idx_target_type_time` |
| `wy_notice_read` | 通知读取记录 | `notice_id`、`user_id`、`read_at` | `uk_notice_user` |
| `wy_feedback` | 用户反馈表 | `user_id`、`content`、`process_status` | `idx_user_time` |
| `wy_media_asset` | 媒体资源表 | `media_id`、`owner_user_id`、`media_type`、`storage_key` | `uk_media_id` |
| `wy_media_process_task` | 媒体处理任务 | `media_id`、`task_type`、`task_status` | `idx_media_status` |
| `wy_audit_task` | 审核任务表 | `biz_type`、`biz_id`、`audit_status`、`risk_level` | `idx_biz_status` |
| `wy_sensitive_word` | 敏感词表 | `word_text`、`word_type`、`status` | `uk_word_text` |
| `wy_operation_log` | 操作审计日志 | `operator_id`、`biz_type`、`operation_type` | `idx_operator_time` |

## 7. 关键表结构明细

### 7.1 `wy_user_account`

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| `id` | `BIGINT` | PK | 主键 |
| `user_id` | `BIGINT` | `NOT NULL` | 用户 ID |
| `weiyou_no` | `VARCHAR(32)` | `NOT NULL` | 微友号 |
| `mobile` | `VARCHAR(32)` | `NOT NULL` | 手机号 |
| `password_hash` | `VARCHAR(128)` | `NOT NULL` | 登录密码哈希 |
| `register_source` | `VARCHAR(32)` | `NOT NULL` | 注册来源 |
| `account_status` | `TINYINT` | `NOT NULL` | 账号状态，0 正常，1 冻结，2 注销 |
| `realname_status` | `TINYINT` | `NOT NULL` | 实名状态 |
| `last_login_at` | `DATETIME(3)` | `NULL` | 最近登录时间 |
| `created_at` | `DATETIME(3)` | `NOT NULL` | 创建时间 |
| `updated_at` | `DATETIME(3)` | `NOT NULL` | 更新时间 |

索引建议：

- `UNIQUE KEY uk_mobile (mobile)`
- `UNIQUE KEY uk_weiyou_no (weiyou_no)`
- `KEY idx_status_login (account_status, last_login_at)`

### 7.2 `wy_conversation`

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| `id` | `BIGINT` | PK | 主键 |
| `conversation_id` | `BIGINT` | `NOT NULL` | 会话 ID |
| `conversation_no` | `VARCHAR(64)` | `NOT NULL` | 会话唯一编号 |
| `conversation_type` | `TINYINT` | `NOT NULL` | 1 单聊，2 群聊，3 服务通知 |
| `biz_id` | `BIGINT` | `NULL` | 群 ID 或业务对象 ID |
| `last_msg_id` | `BIGINT` | `NULL` | 最后一条消息 ID |
| `last_msg_time` | `DATETIME(3)` | `NULL` | 最后一条消息时间 |
| `last_msg_digest` | `VARCHAR(255)` | `NULL` | 最后消息摘要 |
| `status` | `TINYINT` | `NOT NULL` | 会话状态 |
| `created_at` | `DATETIME(3)` | `NOT NULL` | 创建时间 |
| `updated_at` | `DATETIME(3)` | `NOT NULL` | 更新时间 |

索引建议：

- `UNIQUE KEY uk_conversation_no (conversation_no)`
- `KEY idx_type_msg_time (conversation_type, last_msg_time)`

### 7.3 `wy_conversation_user`

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| `id` | `BIGINT` | PK | 主键 |
| `conversation_id` | `BIGINT` | `NOT NULL` | 会话 ID |
| `user_id` | `BIGINT` | `NOT NULL` | 用户 ID |
| `unread_count` | `INT` | `NOT NULL` | 未读数 |
| `top_flag` | `TINYINT` | `NOT NULL` | 是否置顶 |
| `mute_flag` | `TINYINT` | `NOT NULL` | 是否免打扰 |
| `draft_content` | `VARCHAR(500)` | `NULL` | 草稿摘要 |
| `last_read_seq_no` | `BIGINT` | `NOT NULL` | 最后已读序列号 |
| `clear_before_time` | `DATETIME(3)` | `NULL` | 清空聊天记录时间点 |
| `sort_time` | `DATETIME(3)` | `NOT NULL` | 排序时间 |
| `created_at` | `DATETIME(3)` | `NOT NULL` | 创建时间 |
| `updated_at` | `DATETIME(3)` | `NOT NULL` | 更新时间 |

索引建议：

- `UNIQUE KEY uk_conversation_user (conversation_id, user_id)`
- `KEY idx_user_sort (user_id, top_flag, sort_time)`

### 7.4 `wy_message_00~63`

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| `id` | `BIGINT` | PK | 主键 |
| `message_id` | `BIGINT` | `NOT NULL` | 消息 ID |
| `conversation_id` | `BIGINT` | `NOT NULL` | 会话 ID |
| `seq_no` | `BIGINT` | `NOT NULL` | 会话内递增序列 |
| `client_msg_id` | `VARCHAR(64)` | `NOT NULL` | 客户端幂等消息 ID |
| `sender_user_id` | `BIGINT` | `NOT NULL` | 发送者 |
| `msg_type` | `TINYINT` | `NOT NULL` | 文本、图片、语音、视频等 |
| `content_json` | `JSON` | `NOT NULL` | 消息内容 JSON |
| `reply_msg_id` | `BIGINT` | `NULL` | 引用消息 ID |
| `send_status` | `TINYINT` | `NOT NULL` | 发送状态 |
| `send_time` | `DATETIME(3)` | `NOT NULL` | 发送时间 |
| `ext_json` | `JSON` | `NULL` | 扩展信息 |
| `created_at` | `DATETIME(3)` | `NOT NULL` | 创建时间 |

索引建议：

- `UNIQUE KEY uk_conv_seq (conversation_id, seq_no)`
- `UNIQUE KEY uk_client_msg (client_msg_id)`
- `KEY idx_conversation_time (conversation_id, send_time)`
- `KEY idx_sender_time (sender_user_id, send_time)`

分表建议：

- 路由规则：`tableIndex = conversation_id % 64`
- 归档策略：超长期历史消息可转冷存储或归档表

### 7.5 `wy_group_info`

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| `id` | `BIGINT` | PK | 主键 |
| `group_id` | `BIGINT` | `NOT NULL` | 群 ID |
| `group_name` | `VARCHAR(128)` | `NOT NULL` | 群名称 |
| `owner_user_id` | `BIGINT` | `NOT NULL` | 群主用户 ID |
| `group_avatar` | `VARCHAR(255)` | `NULL` | 群头像 |
| `member_count` | `INT` | `NOT NULL` | 群成员数 |
| `join_policy` | `TINYINT` | `NOT NULL` | 入群策略 |
| `mute_all_flag` | `TINYINT` | `NOT NULL` | 是否全员禁言 |
| `group_status` | `TINYINT` | `NOT NULL` | 群状态 |
| `created_at` | `DATETIME(3)` | `NOT NULL` | 创建时间 |
| `updated_at` | `DATETIME(3)` | `NOT NULL` | 更新时间 |

索引建议：

- `UNIQUE KEY uk_group_id (group_id)`
- `KEY idx_owner_status (owner_user_id, group_status)`

### 7.6 `wy_moment_post`

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| `id` | `BIGINT` | PK | 主键 |
| `moment_id` | `BIGINT` | `NOT NULL` | 动态 ID |
| `author_user_id` | `BIGINT` | `NOT NULL` | 发布者 |
| `content_text` | `TEXT` | `NULL` | 动态正文 |
| `media_count` | `INT` | `NOT NULL` | 媒体数量 |
| `visible_type` | `TINYINT` | `NOT NULL` | 公开、私密、部分可见、不让谁看 |
| `location_name` | `VARCHAR(255)` | `NULL` | 地点名称 |
| `longitude` | `DECIMAL(10,6)` | `NULL` | 经度 |
| `latitude` | `DECIMAL(10,6)` | `NULL` | 纬度 |
| `comment_count` | `INT` | `NOT NULL` | 评论数 |
| `like_count` | `INT` | `NOT NULL` | 点赞数 |
| `publish_status` | `TINYINT` | `NOT NULL` | 发布状态 |
| `created_at` | `DATETIME(3)` | `NOT NULL` | 发布时间 |

索引建议：

- `KEY idx_author_time (author_user_id, created_at DESC)`
- `KEY idx_publish_status_time (publish_status, created_at DESC)`

### 7.7 `wy_wallet_account`

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| `id` | `BIGINT` | PK | 主键 |
| `wallet_account_id` | `BIGINT` | `NOT NULL` | 钱包账户 ID |
| `user_id` | `BIGINT` | `NOT NULL` | 用户 ID |
| `available_balance` | `BIGINT` | `NOT NULL` | 可用余额，单位分 |
| `frozen_balance` | `BIGINT` | `NOT NULL` | 冻结余额，单位分 |
| `total_income` | `BIGINT` | `NOT NULL` | 累计收入 |
| `total_expense` | `BIGINT` | `NOT NULL` | 累计支出 |
| `wallet_status` | `TINYINT` | `NOT NULL` | 钱包状态 |
| `realname_status` | `TINYINT` | `NOT NULL` | 实名状态 |
| `created_at` | `DATETIME(3)` | `NOT NULL` | 创建时间 |
| `updated_at` | `DATETIME(3)` | `NOT NULL` | 更新时间 |

索引建议：

- `UNIQUE KEY uk_user_id (user_id)`
- `KEY idx_status_update (wallet_status, updated_at)`

### 7.8 `wy_wallet_transaction`

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| `id` | `BIGINT` | PK | 主键 |
| `transaction_id` | `BIGINT` | `NOT NULL` | 交易 ID |
| `transaction_no` | `VARCHAR(64)` | `NOT NULL` | 交易单号 |
| `biz_type` | `VARCHAR(32)` | `NOT NULL` | 转账、红包、充值、提现等 |
| `payer_user_id` | `BIGINT` | `NULL` | 付款人 |
| `payee_user_id` | `BIGINT` | `NULL` | 收款人 |
| `amount_fen` | `BIGINT` | `NOT NULL` | 交易金额 |
| `currency_code` | `VARCHAR(8)` | `NOT NULL` | 币种 |
| `transaction_status` | `TINYINT` | `NOT NULL` | 待支付、成功、失败、关闭 |
| `idempotency_key` | `VARCHAR(64)` | `NOT NULL` | 幂等键 |
| `biz_order_no` | `VARCHAR(64)` | `NULL` | 业务订单号 |
| `finish_time` | `DATETIME(3)` | `NULL` | 完成时间 |
| `ext_json` | `JSON` | `NULL` | 扩展字段 |
| `created_at` | `DATETIME(3)` | `NOT NULL` | 创建时间 |
| `updated_at` | `DATETIME(3)` | `NOT NULL` | 更新时间 |

索引建议：

- `UNIQUE KEY uk_transaction_no (transaction_no)`
- `UNIQUE KEY uk_idempotency_key (idempotency_key)`
- `KEY idx_payer_time (payer_user_id, created_at DESC)`
- `KEY idx_payee_time (payee_user_id, created_at DESC)`
- `KEY idx_biz_status_time (biz_type, transaction_status, created_at DESC)`

## 8. Redis、MQ 与搜索索引建议

### 8.1 Redis Key 建议

| Key | 说明 |
| --- | --- |
| `wy:auth:token:{userId}:{deviceId}` | 登录态缓存 |
| `wy:user:profile:{userId}` | 用户资料缓存 |
| `wy:chat:conv:list:{userId}` | 会话列表缓存 |
| `wy:chat:unread:{userId}` | 全局未读数 |
| `wy:chat:online:{userId}:{deviceId}` | 在线连接信息 |
| `wy:wallet:paycode:{userId}` | 动态付款码 |
| `wy:feature:discover:{cityCode}` | 发现页配置缓存 |
| `wy:lock:redpacket:{redPacketNo}` | 红包并发锁 |

### 8.2 MQ Topic 建议

| Topic | 说明 |
| --- | --- |
| `wy-chat-send-topic` | 消息发送事件 |
| `wy-chat-offline-push-topic` | 离线推送任务 |
| `wy-moment-publish-topic` | 动态发布时间线扩散 |
| `wy-wallet-transaction-topic` | 钱包交易事件 |
| `wy-wallet-reconcile-topic` | 对账补偿事件 |
| `wy-media-process-topic` | 媒体转码、压缩、OCR |
| `wy-audit-task-topic` | 审核任务投递 |
| `wy-search-index-topic` | 搜索索引更新 |

### 8.3 Elasticsearch 索引建议

| 索引名 | 说明 |
| --- | --- |
| `wy_chat_message` | 聊天记录搜索 |
| `wy_user_global_search` | 联系人和用户搜索 |
| `wy_official_article` | 公众号文章搜索 |
| `wy_miniapp_search` | 小程序搜索 |
| `wy_channel_video_search` | 视频号搜索 |
| `wy_hot_query_word` | 热搜和联想词 |

## 9. 开发落地顺序建议

### 9.1 第一阶段必须落地

- `auth`、`user`、`relation`、`chat`、`group`、`moment`、`notice`、`media`
- 关键表：`wy_user_account`、`wy_friend_relation`、`wy_conversation`、`wy_conversation_user`、`wy_message_00~63`、`wy_moment_post`
- 关键中间件：`Redis`、`OSS`、`WebSocket`

### 9.2 第二阶段重点补齐

- `wallet`、`search`、`feature`、`feedback`
- 关键表：`wy_wallet_account`、`wy_wallet_transaction`、`wy_wallet_bill`、`wy_red_packet`
- 关键中间件：`MQ`、`Elasticsearch`

### 9.3 第三阶段平台化扩展

- `official`、`miniapp`、`channel`、`audit`
- 关键能力：开放平台、直播、推荐、风控、审核后台、运营配置中心

## 10. 结论

- `微友` 后端一期最适合采用“多模块单体 + 独立 IM 网关”的工程模式，既能支撑快速开发，也为后续拆分服务保留边界。
- 数据库设计必须优先保证账号、关系链、消息、钱包四类核心链路的唯一性、一致性和可扩展性。
- 消息、红包、交易、时间线是最容易成为瓶颈的领域，设计阶段就应预留分表、缓存、消息队列和补偿机制。
- 后续若进入正式编码阶段，建议下一步继续输出 `OpenAPI` 文档、SQL DDL 初稿和后端模块脚手架目录。
