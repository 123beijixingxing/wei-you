# 微友架构说明

## 1. 总体设计

项目采用“前端超级应用 + 后端模块化单体”的第一阶段架构：

- 前端：`uni-app + Vue 3 + Pinia`
- 后端：`Spring Boot 3 + REST API + WebSocket`
- 第一阶段目标：先完成可运行的业务骨架与模块边界
- 第二阶段目标：把高并发和高复杂度模块逐步拆为服务

## 2. 为什么先做模块化单体

高仿微信类应用的功能极多，如果一开始就拆微服务，前期开发成本会显著升高。当前目录设计先把边界定义清楚：

- `auth`：登录、会话、设备、账号安全
- `user`：个人资料、设置、隐私、状态
- `contact`：好友、标签、黑名单、群成员
- `chat`：单聊、群聊、消息、会话、WebSocket
- `moment`：朋友圈、评论、点赞、图片视频动态
- `wallet`：余额、账单、收付款、卡包
- `feature`：发现页、视频号、小程序、公众号等能力入口编排

后续只需要把这些模块迁移到独立服务，前端与 API 契约可以基本保持不变。

## 3. 建议的生产级拆分

如果继续扩展，建议演进为：

- `gateway-service`：统一网关、鉴权、限流、灰度
- `account-service`：登录、设备、关系链、隐私设置
- `im-service`：会话、消息、离线投递、已读回执
- `moment-service`：动态流、评论、点赞、推荐
- `payment-service`：钱包、账单、商户、交易风控
- `ecosystem-service`：小程序、公众号、视频号、开放平台
- `media-service`：图片、视频、语音、文件、转码
- `audit-service`：内容审核、举报、风控、反作弊

## 4. 前端页面分层

- `pages/chat`：消息主入口
- `pages/contacts`：社交关系链入口
- `pages/discover`：发现与生态入口
- `pages/me`：个人中心
- `pages/conversation`：消息详情与交互区
- `pages/moments`：朋友圈内容流
- `pages/wallet`：支付与资产概览
- `pages/feature-hub`：所有微信式能力聚合页

## 5. 数据流建议

- 页面进入后先从 Pinia 取缓存
- 同时发起 API 请求刷新数据
- 即时聊天场景采用“REST 拉历史 + WebSocket 收实时”模式
- 钱包、隐私、安全等敏感能力必须引入独立鉴权、签名与风控校验

## 6. 重点技术建议

- 聊天消息：Redis Stream / Kafka + WebSocket 网关
- 图片视频：对象存储 + CDN + 转码服务
- 音视频通话：WebRTC + TURN/STUN
- 搜索：Elasticsearch
- 钱包交易：强一致数据库事务 + 幂等 + 对账系统
- 内容审核：OCR / ASR / 图像审核 / 文本审核

## 7. 本次交付范围

本次代码重点在：

- 生成完整目录架构
- 建好高频页面骨架
- 给出可直接继续开发的前后端核心代码
- 预留面向全功能社交超级应用的扩展接口
