-- 微友数据库 DDL 初稿
-- 版本：v0.1
-- 说明：
-- 1. 面向 MySQL 8.0。
-- 2. 采用逻辑分库设计，可部署在同一实例中。
-- 3. 消息表、消息回执表采用分表方案，这里仅给出 _00 模板表，01-63 可按相同结构复制生成。
-- 4. 当前为设计初稿，正式落库前建议结合业务代码再补充枚举值、归档策略、审计字段和初始化数据。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `weiyou_account` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `weiyou_relation` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `weiyou_im` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `weiyou_content` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `weiyou_wallet` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `weiyou_ecosystem` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `weiyou_system` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- =========================================================
-- weiyou_account
-- =========================================================
USE `weiyou_account`;

CREATE TABLE IF NOT EXISTS `wy_user_account` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `weiyou_no` VARCHAR(32) NOT NULL COMMENT '微友号',
  `mobile` VARCHAR(32) NOT NULL COMMENT '手机号',
  `mobile_mask` VARCHAR(32) DEFAULT NULL COMMENT '脱敏手机号',
  `password_hash` VARCHAR(128) NOT NULL COMMENT '登录密码哈希',
  `password_salt` VARCHAR(64) DEFAULT NULL COMMENT '密码盐值',
  `register_source` VARCHAR(32) NOT NULL COMMENT '注册来源 app/h5/mp',
  `account_status` TINYINT NOT NULL DEFAULT 0 COMMENT '账号状态 0正常 1冻结 2注销',
  `realname_status` TINYINT NOT NULL DEFAULT 0 COMMENT '实名状态 0未实名 1已实名 2审核中 3失败',
  `last_login_at` DATETIME(3) DEFAULT NULL COMMENT '最近登录时间',
  `last_login_ip` VARCHAR(64) DEFAULT NULL COMMENT '最近登录IP',
  `last_login_city` VARCHAR(64) DEFAULT NULL COMMENT '最近登录城市',
  `ext_json` JSON DEFAULT NULL COMMENT '扩展信息',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  UNIQUE KEY `uk_weiyou_no` (`weiyou_no`),
  UNIQUE KEY `uk_mobile` (`mobile`),
  KEY `idx_status_login` (`account_status`, `last_login_at`),
  KEY `idx_realname_status` (`realname_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户账号主表';

CREATE TABLE IF NOT EXISTS `wy_user_profile` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `nickname` VARCHAR(64) NOT NULL COMMENT '昵称',
  `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
  `gender` TINYINT NOT NULL DEFAULT 0 COMMENT '性别 0未知 1男 2女',
  `birthday` DATE DEFAULT NULL COMMENT '生日',
  `country_code` VARCHAR(16) DEFAULT NULL COMMENT '国家码',
  `country_name` VARCHAR(64) DEFAULT NULL COMMENT '国家',
  `province_name` VARCHAR(64) DEFAULT NULL COMMENT '省份',
  `city_name` VARCHAR(64) DEFAULT NULL COMMENT '城市',
  `signature` VARCHAR(255) DEFAULT NULL COMMENT '个性签名',
  `status_text` VARCHAR(128) DEFAULT NULL COMMENT '状态文案快照',
  `moment_cover_url` VARCHAR(255) DEFAULT NULL COMMENT '朋友圈封面',
  `wechat_shake_text` VARCHAR(128) DEFAULT NULL COMMENT '拍一拍文案',
  `ext_json` JSON DEFAULT NULL COMMENT '扩展信息',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_nickname` (`nickname`),
  KEY `idx_city_name` (`city_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户资料表';

CREATE TABLE IF NOT EXISTS `wy_user_device` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `device_id` VARCHAR(64) NOT NULL COMMENT '设备ID',
  `device_type` VARCHAR(32) NOT NULL COMMENT '设备类型 ios/android/web/pc',
  `device_model` VARCHAR(128) DEFAULT NULL COMMENT '设备型号',
  `system_version` VARCHAR(64) DEFAULT NULL COMMENT '系统版本',
  `client_version` VARCHAR(64) DEFAULT NULL COMMENT '客户端版本',
  `push_token` VARCHAR(255) DEFAULT NULL COMMENT '推送标识',
  `login_ip` VARCHAR(64) DEFAULT NULL COMMENT '登录IP',
  `login_city` VARCHAR(64) DEFAULT NULL COMMENT '登录城市',
  `last_login_at` DATETIME(3) DEFAULT NULL COMMENT '最后登录时间',
  `trust_status` TINYINT NOT NULL DEFAULT 0 COMMENT '可信状态 0普通 1可信 2风险',
  `online_status` TINYINT NOT NULL DEFAULT 0 COMMENT '在线状态 0离线 1在线',
  `ext_json` JSON DEFAULT NULL COMMENT '扩展信息',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_id` (`device_id`),
  KEY `idx_user_status` (`user_id`, `online_status`),
  KEY `idx_user_login_time` (`user_id`, `last_login_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户设备表';

CREATE TABLE IF NOT EXISTS `wy_user_privacy_setting` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `add_friend_policy` TINYINT NOT NULL DEFAULT 0 COMMENT '加好友策略',
  `mobile_searchable` TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许手机号搜索',
  `weiyou_no_searchable` TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许微友号搜索',
  `contact_match_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否开启通讯录匹配',
  `moment_visible_days` TINYINT NOT NULL DEFAULT 0 COMMENT '朋友圈可见天数 0全部 3三天 30一个月',
  `moment_allow_stranger_view` TINYINT NOT NULL DEFAULT 0 COMMENT '是否允许陌生人查看',
  `blacklist_switch` TINYINT NOT NULL DEFAULT 1 COMMENT '黑名单功能开关',
  `ext_json` JSON DEFAULT NULL COMMENT '扩展配置',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户隐私设置表';

CREATE TABLE IF NOT EXISTS `wy_user_qrcode` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `qrcode_type` TINYINT NOT NULL DEFAULT 1 COMMENT '二维码类型 1长期 2动态',
  `ticket` VARCHAR(128) NOT NULL COMMENT '二维码票据',
  `scene_value` VARCHAR(128) DEFAULT NULL COMMENT '场景值',
  `expire_at` DATETIME(3) DEFAULT NULL COMMENT '过期时间',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0有效 1失效',
  `ext_json` JSON DEFAULT NULL COMMENT '扩展信息',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ticket` (`ticket`),
  KEY `idx_user_type_status` (`user_id`, `qrcode_type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户二维码表';

CREATE TABLE IF NOT EXISTS `wy_user_status` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `status_code` VARCHAR(32) NOT NULL COMMENT '状态编码',
  `status_text` VARCHAR(128) DEFAULT NULL COMMENT '状态文案',
  `status_icon` VARCHAR(255) DEFAULT NULL COMMENT '状态图标',
  `expire_at` DATETIME(3) DEFAULT NULL COMMENT '失效时间',
  `ext_json` JSON DEFAULT NULL COMMENT '扩展信息',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户状态表';

-- =========================================================
-- weiyou_relation
-- =========================================================
USE `weiyou_relation`;

CREATE TABLE IF NOT EXISTS `wy_friend_relation` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `owner_user_id` BIGINT UNSIGNED NOT NULL COMMENT '关系拥有者',
  `friend_user_id` BIGINT UNSIGNED NOT NULL COMMENT '好友用户ID',
  `remark` VARCHAR(64) DEFAULT NULL COMMENT '好友备注',
  `source` VARCHAR(32) DEFAULT NULL COMMENT '来源 手机号/扫码/群聊/名片',
  `star_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否星标好友',
  `moment_permission` TINYINT NOT NULL DEFAULT 0 COMMENT '朋友圈权限',
  `chat_background_url` VARCHAR(255) DEFAULT NULL COMMENT '聊天背景图',
  `relation_status` TINYINT NOT NULL DEFAULT 0 COMMENT '关系状态 0正常 1删除 2拉黑',
  `ext_json` JSON DEFAULT NULL COMMENT '扩展信息',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_owner_friend` (`owner_user_id`, `friend_user_id`),
  KEY `idx_owner_status` (`owner_user_id`, `relation_status`),
  KEY `idx_friend_user_id` (`friend_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友关系表';

CREATE TABLE IF NOT EXISTS `wy_friend_request` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `request_id` BIGINT UNSIGNED NOT NULL COMMENT '申请ID',
  `from_user_id` BIGINT UNSIGNED NOT NULL COMMENT '发起人',
  `to_user_id` BIGINT UNSIGNED NOT NULL COMMENT '接收人',
  `apply_message` VARCHAR(255) DEFAULT NULL COMMENT '申请文案',
  `source` VARCHAR(32) DEFAULT NULL COMMENT '申请来源',
  `apply_status` TINYINT NOT NULL DEFAULT 0 COMMENT '申请状态 0待处理 1通过 2拒绝 3失效',
  `handled_at` DATETIME(3) DEFAULT NULL COMMENT '处理时间',
  `handled_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '处理人',
  `ext_json` JSON DEFAULT NULL COMMENT '扩展信息',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_request_id` (`request_id`),
  KEY `idx_to_status_time` (`to_user_id`, `apply_status`, `created_at`),
  KEY `idx_from_time` (`from_user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友申请记录表';

CREATE TABLE IF NOT EXISTS `wy_contact_tag` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tag_id` BIGINT UNSIGNED NOT NULL COMMENT '标签ID',
  `owner_user_id` BIGINT UNSIGNED NOT NULL COMMENT '拥有者用户ID',
  `tag_name` VARCHAR(64) NOT NULL COMMENT '标签名称',
  `member_count` INT NOT NULL DEFAULT 0 COMMENT '成员数',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `ext_json` JSON DEFAULT NULL COMMENT '扩展信息',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_id` (`tag_id`),
  KEY `idx_owner_name` (`owner_user_id`, `tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通讯录标签表';

CREATE TABLE IF NOT EXISTS `wy_contact_tag_member` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tag_id` BIGINT UNSIGNED NOT NULL COMMENT '标签ID',
  `friend_user_id` BIGINT UNSIGNED NOT NULL COMMENT '好友用户ID',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_friend` (`tag_id`, `friend_user_id`),
  KEY `idx_friend_user_id` (`friend_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签成员关系表';

CREATE TABLE IF NOT EXISTS `wy_blacklist` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `owner_user_id` BIGINT UNSIGNED NOT NULL COMMENT '拥有者用户ID',
  `blocked_user_id` BIGINT UNSIGNED NOT NULL COMMENT '被拉黑用户ID',
  `block_reason` VARCHAR(255) DEFAULT NULL COMMENT '拉黑原因',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_owner_blocked` (`owner_user_id`, `blocked_user_id`),
  KEY `idx_blocked_user_id` (`blocked_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='黑名单表';

CREATE TABLE IF NOT EXISTS `wy_group_info` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `group_id` BIGINT UNSIGNED NOT NULL COMMENT '群ID',
  `group_no` VARCHAR(64) NOT NULL COMMENT '群编号',
  `group_name` VARCHAR(128) NOT NULL COMMENT '群名称',
  `group_avatar` VARCHAR(255) DEFAULT NULL COMMENT '群头像',
  `owner_user_id` BIGINT UNSIGNED NOT NULL COMMENT '群主用户ID',
  `member_count` INT NOT NULL DEFAULT 0 COMMENT '群成员数',
  `join_policy` TINYINT NOT NULL DEFAULT 0 COMMENT '入群策略',
  `mute_all_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '全员禁言',
  `group_status` TINYINT NOT NULL DEFAULT 0 COMMENT '群状态 0正常 1解散 2封禁',
  `ext_json` JSON DEFAULT NULL COMMENT '扩展信息',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_id` (`group_id`),
  UNIQUE KEY `uk_group_no` (`group_no`),
  KEY `idx_owner_status` (`owner_user_id`, `group_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群主表';

CREATE TABLE IF NOT EXISTS `wy_group_member` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `group_id` BIGINT UNSIGNED NOT NULL COMMENT '群ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '成员用户ID',
  `role_type` TINYINT NOT NULL DEFAULT 0 COMMENT '角色 0成员 1管理员 2群主',
  `join_source` VARCHAR(32) DEFAULT NULL COMMENT '入群来源',
  `join_status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0正常 1退出 2移除',
  `mute_until` DATETIME(3) DEFAULT NULL COMMENT '禁言截止时间',
  `group_nickname` VARCHAR(64) DEFAULT NULL COMMENT '群内昵称',
  `joined_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '入群时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_user` (`group_id`, `user_id`),
  KEY `idx_user_status` (`user_id`, `join_status`),
  KEY `idx_group_role` (`group_id`, `role_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群成员关系表';

CREATE TABLE IF NOT EXISTS `wy_group_notice` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `group_id` BIGINT UNSIGNED NOT NULL COMMENT '群ID',
  `notice_content` TEXT NOT NULL COMMENT '群公告内容',
  `publisher_user_id` BIGINT UNSIGNED NOT NULL COMMENT '发布人',
  `published_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '发布时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群公告表';

-- =========================================================
-- weiyou_im
-- =========================================================
USE `weiyou_im`;

CREATE TABLE IF NOT EXISTS `wy_conversation` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `conversation_id` BIGINT UNSIGNED NOT NULL COMMENT '会话ID',
  `conversation_no` VARCHAR(64) NOT NULL COMMENT '会话编号',
  `conversation_type` TINYINT NOT NULL COMMENT '会话类型 1单聊 2群聊 3服务通知',
  `biz_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '业务ID 群ID或服务主体ID',
  `last_msg_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '最后消息ID',
  `last_msg_time` DATETIME(3) DEFAULT NULL COMMENT '最后消息时间',
  `last_msg_digest` VARCHAR(255) DEFAULT NULL COMMENT '最后消息摘要',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '会话状态',
  `ext_json` JSON DEFAULT NULL COMMENT '扩展信息',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_conversation_id` (`conversation_id`),
  UNIQUE KEY `uk_conversation_no` (`conversation_no`),
  KEY `idx_type_msg_time` (`conversation_type`, `last_msg_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话主表';

CREATE TABLE IF NOT EXISTS `wy_conversation_user` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `conversation_id` BIGINT UNSIGNED NOT NULL COMMENT '会话ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `unread_count` INT NOT NULL DEFAULT 0 COMMENT '未读数',
  `top_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶',
  `mute_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否免打扰',
  `mark_unread_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否标为未读',
  `draft_content` VARCHAR(500) DEFAULT NULL COMMENT '草稿摘要',
  `last_read_seq_no` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '最后已读序列号',
  `clear_before_time` DATETIME(3) DEFAULT NULL COMMENT '清空聊天记录时间点',
  `sort_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '排序时间',
  `ext_json` JSON DEFAULT NULL COMMENT '扩展配置',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_conversation_user` (`conversation_id`, `user_id`),
  KEY `idx_user_sort` (`user_id`, `top_flag`, `sort_time`),
  KEY `idx_user_unread` (`user_id`, `unread_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户会话关系表';

CREATE TABLE IF NOT EXISTS `wy_message_00` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `message_id` BIGINT UNSIGNED NOT NULL COMMENT '消息ID',
  `conversation_id` BIGINT UNSIGNED NOT NULL COMMENT '会话ID',
  `seq_no` BIGINT UNSIGNED NOT NULL COMMENT '会话内递增序列',
  `client_msg_id` VARCHAR(64) NOT NULL COMMENT '客户端幂等消息ID',
  `sender_user_id` BIGINT UNSIGNED NOT NULL COMMENT '发送者用户ID',
  `msg_type` TINYINT NOT NULL COMMENT '消息类型',
  `content_json` JSON NOT NULL COMMENT '消息体JSON',
  `reply_msg_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '引用消息ID',
  `send_status` TINYINT NOT NULL DEFAULT 0 COMMENT '发送状态',
  `send_time` DATETIME(3) NOT NULL COMMENT '发送时间',
  `ext_json` JSON DEFAULT NULL COMMENT '扩展字段',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_message_id` (`message_id`),
  UNIQUE KEY `uk_client_msg` (`client_msg_id`),
  UNIQUE KEY `uk_conv_seq` (`conversation_id`, `seq_no`),
  KEY `idx_conversation_time` (`conversation_id`, `send_time`),
  KEY `idx_sender_time` (`sender_user_id`, `send_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息分表模板_00';

CREATE TABLE IF NOT EXISTS `wy_message_receipt_00` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `message_id` BIGINT UNSIGNED NOT NULL COMMENT '消息ID',
  `conversation_id` BIGINT UNSIGNED NOT NULL COMMENT '会话ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `delivery_status` TINYINT NOT NULL DEFAULT 0 COMMENT '送达状态',
  `read_status` TINYINT NOT NULL DEFAULT 0 COMMENT '已读状态',
  `delivered_at` DATETIME(3) DEFAULT NULL COMMENT '送达时间',
  `read_at` DATETIME(3) DEFAULT NULL COMMENT '已读时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_msg_user` (`message_id`, `user_id`),
  KEY `idx_conversation_user` (`conversation_id`, `user_id`),
  KEY `idx_user_read_status` (`user_id`, `read_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息回执分表模板_00';

CREATE TABLE IF NOT EXISTS `wy_message_recall_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `message_id` BIGINT UNSIGNED NOT NULL COMMENT '消息ID',
  `conversation_id` BIGINT UNSIGNED NOT NULL COMMENT '会话ID',
  `operator_id` BIGINT UNSIGNED NOT NULL COMMENT '操作人',
  `recall_reason` VARCHAR(255) DEFAULT NULL COMMENT '撤回原因',
  `recall_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '撤回时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_message_id` (`message_id`),
  KEY `idx_conversation_time` (`conversation_id`, `recall_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息撤回记录表';

CREATE TABLE IF NOT EXISTS `wy_chat_draft` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `conversation_id` BIGINT UNSIGNED NOT NULL COMMENT '会话ID',
  `draft_content` VARCHAR(1000) DEFAULT NULL COMMENT '草稿内容',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_conversation` (`user_id`, `conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天草稿表';

CREATE TABLE IF NOT EXISTS `wy_online_session` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `device_id` VARCHAR(64) NOT NULL COMMENT '设备ID',
  `channel_id` VARCHAR(128) NOT NULL COMMENT '连接通道ID',
  `connect_ip` VARCHAR(64) DEFAULT NULL COMMENT '连接IP',
  `online_status` TINYINT NOT NULL DEFAULT 0 COMMENT '在线状态',
  `heartbeat_at` DATETIME(3) DEFAULT NULL COMMENT '最近心跳时间',
  `connected_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '连接时间',
  `disconnected_at` DATETIME(3) DEFAULT NULL COMMENT '断开时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_device` (`user_id`, `device_id`),
  UNIQUE KEY `uk_channel_id` (`channel_id`),
  KEY `idx_online_status` (`online_status`, `heartbeat_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='在线会话表';

-- =========================================================
-- weiyou_content
-- =========================================================
USE `weiyou_content`;

CREATE TABLE IF NOT EXISTS `wy_moment_post` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `moment_id` BIGINT UNSIGNED NOT NULL COMMENT '动态ID',
  `author_user_id` BIGINT UNSIGNED NOT NULL COMMENT '发布者ID',
  `content_text` TEXT COMMENT '动态正文',
  `media_count` INT NOT NULL DEFAULT 0 COMMENT '媒体数量',
  `visible_type` TINYINT NOT NULL DEFAULT 0 COMMENT '可见范围',
  `location_name` VARCHAR(255) DEFAULT NULL COMMENT '地点名称',
  `longitude` DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
  `latitude` DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
  `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `publish_status` TINYINT NOT NULL DEFAULT 0 COMMENT '发布状态',
  `ext_json` JSON DEFAULT NULL COMMENT '扩展信息',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '发布时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_moment_id` (`moment_id`),
  KEY `idx_author_time` (`author_user_id`, `created_at`),
  KEY `idx_publish_status_time` (`publish_status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='朋友圈动态主表';

CREATE TABLE IF NOT EXISTS `wy_moment_media` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `moment_id` BIGINT UNSIGNED NOT NULL COMMENT '动态ID',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `media_type` TINYINT NOT NULL COMMENT '媒体类型 1图片 2视频',
  `media_url` VARCHAR(255) NOT NULL COMMENT '媒体地址',
  `cover_url` VARCHAR(255) DEFAULT NULL COMMENT '封面地址',
  `width` INT DEFAULT NULL COMMENT '宽度',
  `height` INT DEFAULT NULL COMMENT '高度',
  `duration_ms` INT DEFAULT NULL COMMENT '时长 毫秒',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_moment_sort` (`moment_id`, `sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='朋友圈媒体表';

CREATE TABLE IF NOT EXISTS `wy_moment_comment` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `comment_id` BIGINT UNSIGNED NOT NULL COMMENT '评论ID',
  `moment_id` BIGINT UNSIGNED NOT NULL COMMENT '动态ID',
  `comment_user_id` BIGINT UNSIGNED NOT NULL COMMENT '评论用户ID',
  `reply_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '被回复用户ID',
  `reply_comment_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '父评论ID',
  `content_text` VARCHAR(500) NOT NULL COMMENT '评论内容',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '评论时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_id` (`comment_id`),
  KEY `idx_moment_time` (`moment_id`, `created_at`),
  KEY `idx_user_time` (`comment_user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='朋友圈评论表';

CREATE TABLE IF NOT EXISTS `wy_moment_like` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `moment_id` BIGINT UNSIGNED NOT NULL COMMENT '动态ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '点赞用户ID',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_moment_user` (`moment_id`, `user_id`),
  KEY `idx_user_time` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='朋友圈点赞表';

CREATE TABLE IF NOT EXISTS `wy_moment_timeline` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `owner_user_id` BIGINT UNSIGNED NOT NULL COMMENT '时间线拥有者',
  `moment_id` BIGINT UNSIGNED NOT NULL COMMENT '动态ID',
  `author_user_id` BIGINT UNSIGNED NOT NULL COMMENT '动态作者',
  `sort_time` DATETIME(3) NOT NULL COMMENT '排序时间',
  `visible_status` TINYINT NOT NULL DEFAULT 0 COMMENT '可见状态',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_owner_moment` (`owner_user_id`, `moment_id`),
  KEY `idx_owner_sort` (`owner_user_id`, `sort_time`),
  KEY `idx_author_sort` (`author_user_id`, `sort_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='朋友圈时间线表';

-- =========================================================
-- weiyou_wallet
-- =========================================================
USE `weiyou_wallet`;

CREATE TABLE IF NOT EXISTS `wy_wallet_account` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `wallet_account_id` BIGINT UNSIGNED NOT NULL COMMENT '钱包账户ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `available_balance` BIGINT NOT NULL DEFAULT 0 COMMENT '可用余额 分',
  `frozen_balance` BIGINT NOT NULL DEFAULT 0 COMMENT '冻结余额 分',
  `total_income` BIGINT NOT NULL DEFAULT 0 COMMENT '累计收入 分',
  `total_expense` BIGINT NOT NULL DEFAULT 0 COMMENT '累计支出 分',
  `wallet_status` TINYINT NOT NULL DEFAULT 0 COMMENT '钱包状态',
  `realname_status` TINYINT NOT NULL DEFAULT 0 COMMENT '实名状态',
  `pay_password_status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付密码状态',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wallet_account_id` (`wallet_account_id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_status_update` (`wallet_status`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='钱包账户表';

CREATE TABLE IF NOT EXISTS `wy_wallet_transaction` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `transaction_id` BIGINT UNSIGNED NOT NULL COMMENT '交易ID',
  `transaction_no` VARCHAR(64) NOT NULL COMMENT '交易单号',
  `biz_type` VARCHAR(32) NOT NULL COMMENT '业务类型 transfer/red_packet/recharge/withdraw',
  `payer_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '付款人',
  `payee_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '收款人',
  `amount_fen` BIGINT NOT NULL COMMENT '金额 分',
  `currency_code` VARCHAR(8) NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `transaction_status` TINYINT NOT NULL DEFAULT 0 COMMENT '交易状态',
  `idempotency_key` VARCHAR(64) NOT NULL COMMENT '幂等键',
  `biz_order_no` VARCHAR(64) DEFAULT NULL COMMENT '业务订单号',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `finish_time` DATETIME(3) DEFAULT NULL COMMENT '完成时间',
  `ext_json` JSON DEFAULT NULL COMMENT '扩展字段',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transaction_id` (`transaction_id`),
  UNIQUE KEY `uk_transaction_no` (`transaction_no`),
  UNIQUE KEY `uk_idempotency_key` (`idempotency_key`),
  KEY `idx_payer_time` (`payer_user_id`, `created_at`),
  KEY `idx_payee_time` (`payee_user_id`, `created_at`),
  KEY `idx_biz_status_time` (`biz_type`, `transaction_status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='钱包交易流水主表';

CREATE TABLE IF NOT EXISTS `wy_wallet_bill` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `bill_id` BIGINT UNSIGNED NOT NULL COMMENT '账单ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `transaction_no` VARCHAR(64) NOT NULL COMMENT '交易单号',
  `bill_type` VARCHAR(32) NOT NULL COMMENT '账单类型',
  `income_expense_type` TINYINT NOT NULL COMMENT '收支类型 1收入 2支出',
  `amount_fen` BIGINT NOT NULL COMMENT '金额 分',
  `bill_time` DATETIME(3) NOT NULL COMMENT '账单时间',
  `biz_title` VARCHAR(128) DEFAULT NULL COMMENT '账单标题',
  `biz_subtitle` VARCHAR(255) DEFAULT NULL COMMENT '账单副标题',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bill_id` (`bill_id`),
  KEY `idx_user_bill_time` (`user_id`, `bill_time`),
  KEY `idx_transaction_no` (`transaction_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='钱包账单表';

CREATE TABLE IF NOT EXISTS `wy_wallet_bank_card` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `card_id` BIGINT UNSIGNED NOT NULL COMMENT '银行卡记录ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `bank_card_no_enc` VARCHAR(255) NOT NULL COMMENT '银行卡号密文',
  `bank_card_mask` VARCHAR(64) NOT NULL COMMENT '银行卡号脱敏',
  `bank_code` VARCHAR(32) NOT NULL COMMENT '银行编码',
  `bank_name` VARCHAR(64) DEFAULT NULL COMMENT '银行名称',
  `card_type` TINYINT NOT NULL DEFAULT 1 COMMENT '卡类型',
  `bind_status` TINYINT NOT NULL DEFAULT 0 COMMENT '绑卡状态',
  `bind_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '绑卡时间',
  `unbind_time` DATETIME(3) DEFAULT NULL COMMENT '解绑时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_card_id` (`card_id`),
  KEY `idx_user_status` (`user_id`, `bind_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='银行卡绑定表';

CREATE TABLE IF NOT EXISTS `wy_red_packet` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `red_packet_id` BIGINT UNSIGNED NOT NULL COMMENT '红包ID',
  `red_packet_no` VARCHAR(64) NOT NULL COMMENT '红包单号',
  `sender_user_id` BIGINT UNSIGNED NOT NULL COMMENT '发红包用户ID',
  `group_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '群ID 单聊为空',
  `packet_type` TINYINT NOT NULL DEFAULT 1 COMMENT '红包类型 1普通 2拼手气',
  `total_amount_fen` BIGINT NOT NULL COMMENT '红包总金额 分',
  `packet_count` INT NOT NULL DEFAULT 1 COMMENT '红包个数',
  `greeting` VARCHAR(128) DEFAULT NULL COMMENT '祝福语',
  `packet_status` TINYINT NOT NULL DEFAULT 0 COMMENT '红包状态 0可领取 1已领完 2已过期 3已退回',
  `expire_at` DATETIME(3) DEFAULT NULL COMMENT '过期时间',
  `finished_at` DATETIME(3) DEFAULT NULL COMMENT '领完时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_red_packet_id` (`red_packet_id`),
  UNIQUE KEY `uk_red_packet_no` (`red_packet_no`),
  KEY `idx_sender_status_time` (`sender_user_id`, `packet_status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='红包主表';

CREATE TABLE IF NOT EXISTS `wy_red_packet_item` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `red_packet_no` VARCHAR(64) NOT NULL COMMENT '红包单号',
  `item_no` INT NOT NULL COMMENT '拆分序号',
  `amount_fen` BIGINT NOT NULL COMMENT '金额 分',
  `receive_status` TINYINT NOT NULL DEFAULT 0 COMMENT '领取状态',
  `receiver_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '领取人',
  `received_at` DATETIME(3) DEFAULT NULL COMMENT '领取时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_red_packet_item` (`red_packet_no`, `item_no`),
  KEY `idx_receiver_user_id` (`receiver_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='红包拆分明细表';

CREATE TABLE IF NOT EXISTS `wy_red_packet_receive` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `red_packet_no` VARCHAR(64) NOT NULL COMMENT '红包单号',
  `receiver_user_id` BIGINT UNSIGNED NOT NULL COMMENT '领取用户ID',
  `receive_amount_fen` BIGINT NOT NULL COMMENT '领取金额 分',
  `rank_no` INT DEFAULT NULL COMMENT '手气排行',
  `received_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '领取时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_red_packet_user` (`red_packet_no`, `receiver_user_id`),
  KEY `idx_receiver_time` (`receiver_user_id`, `received_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='红包领取记录表';

-- =========================================================
-- weiyou_ecosystem
-- =========================================================
USE `weiyou_ecosystem`;

CREATE TABLE IF NOT EXISTS `wy_feature_discovery_config` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `city_code` VARCHAR(16) NOT NULL COMMENT '城市编码',
  `feature_code` VARCHAR(64) NOT NULL COMMENT '功能编码',
  `feature_name` VARCHAR(64) NOT NULL COMMENT '功能名称',
  `icon_url` VARCHAR(255) DEFAULT NULL COMMENT '图标地址',
  `route_path` VARCHAR(255) DEFAULT NULL COMMENT '路由地址',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0关闭 1开启',
  `ext_json` JSON DEFAULT NULL COMMENT '扩展配置',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_city_feature` (`city_code`, `feature_code`),
  KEY `idx_city_sort` (`city_code`, `sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发现页配置表';

CREATE TABLE IF NOT EXISTS `wy_miniapp` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `app_id` VARCHAR(64) NOT NULL COMMENT '小程序AppID',
  `app_name` VARCHAR(128) NOT NULL COMMENT '小程序名称',
  `developer_id` BIGINT UNSIGNED NOT NULL COMMENT '开发者ID',
  `category_name` VARCHAR(64) DEFAULT NULL COMMENT '分类名称',
  `icon_url` VARCHAR(255) DEFAULT NULL COMMENT '图标地址',
  `intro_text` VARCHAR(255) DEFAULT NULL COMMENT '简介',
  `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态',
  `publish_status` TINYINT NOT NULL DEFAULT 0 COMMENT '发布状态',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_id` (`app_id`),
  KEY `idx_developer_status` (`developer_id`, `publish_status`),
  KEY `idx_app_name` (`app_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小程序主表';

-- =========================================================
-- weiyou_system
-- =========================================================
USE `weiyou_system`;

CREATE TABLE IF NOT EXISTS `wy_notice` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `notice_id` BIGINT UNSIGNED NOT NULL COMMENT '通知ID',
  `target_user_id` BIGINT UNSIGNED NOT NULL COMMENT '目标用户ID',
  `notice_type` VARCHAR(32) NOT NULL COMMENT '通知类型',
  `biz_id` VARCHAR(64) DEFAULT NULL COMMENT '业务ID',
  `title` VARCHAR(128) DEFAULT NULL COMMENT '标题',
  `content_text` VARCHAR(500) DEFAULT NULL COMMENT '内容摘要',
  `read_status` TINYINT NOT NULL DEFAULT 0 COMMENT '已读状态',
  `push_status` TINYINT NOT NULL DEFAULT 0 COMMENT '推送状态',
  `payload_json` JSON DEFAULT NULL COMMENT '通知载荷',
  `read_at` DATETIME(3) DEFAULT NULL COMMENT '已读时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notice_id` (`notice_id`),
  KEY `idx_target_type_time` (`target_user_id`, `notice_type`, `created_at`),
  KEY `idx_target_read` (`target_user_id`, `read_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知表';

CREATE TABLE IF NOT EXISTS `wy_feedback` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `feedback_id` BIGINT UNSIGNED NOT NULL COMMENT '反馈ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `feedback_type` VARCHAR(32) DEFAULT NULL COMMENT '反馈类型',
  `content_text` VARCHAR(1000) NOT NULL COMMENT '反馈内容',
  `contact_info` VARCHAR(128) DEFAULT NULL COMMENT '联系方式',
  `image_urls_json` JSON DEFAULT NULL COMMENT '截图列表',
  `process_status` TINYINT NOT NULL DEFAULT 0 COMMENT '处理状态',
  `processed_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '处理人',
  `processed_at` DATETIME(3) DEFAULT NULL COMMENT '处理时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_feedback_id` (`feedback_id`),
  KEY `idx_user_time` (`user_id`, `created_at`),
  KEY `idx_process_status` (`process_status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户反馈表';

CREATE TABLE IF NOT EXISTS `wy_media_asset` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `media_id` BIGINT UNSIGNED NOT NULL COMMENT '媒体ID',
  `owner_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '拥有者用户ID',
  `biz_type` VARCHAR(32) NOT NULL COMMENT '业务类型 avatar/chat/moment/wallet',
  `media_type` VARCHAR(32) NOT NULL COMMENT '媒体类型 image/video/audio/file',
  `origin_name` VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
  `storage_key` VARCHAR(255) NOT NULL COMMENT '对象存储Key',
  `media_url` VARCHAR(255) NOT NULL COMMENT '访问地址',
  `cover_url` VARCHAR(255) DEFAULT NULL COMMENT '封面地址',
  `content_type` VARCHAR(128) DEFAULT NULL COMMENT '内容类型',
  `file_size` BIGINT DEFAULT NULL COMMENT '文件大小',
  `width` INT DEFAULT NULL COMMENT '宽度',
  `height` INT DEFAULT NULL COMMENT '高度',
  `duration_ms` INT DEFAULT NULL COMMENT '时长',
  `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_media_id` (`media_id`),
  KEY `idx_owner_biz_type` (`owner_user_id`, `biz_type`),
  KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='媒体资源表';

SET FOREIGN_KEY_CHECKS = 1;
