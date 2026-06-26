-- 微友本地联调初始化数据
-- 执行前请先运行 docs/需求设计文档/微友数据库DDL初稿.sql

SET NAMES utf8mb4;

DELETE FROM `weiyou_wallet`.`wy_wallet_bill` WHERE `user_id` IN (10001, 10002, 10003);
DELETE FROM `weiyou_wallet`.`wy_wallet_transaction` WHERE `payer_user_id` IN (10001, 10002, 10003) OR `payee_user_id` IN (10001, 10002, 10003);
DELETE FROM `weiyou_wallet`.`wy_red_packet_receive` WHERE `receiver_user_id` IN (10001, 10002, 10003);
DELETE FROM `weiyou_wallet`.`wy_red_packet_item` WHERE `red_packet_no` IN ('RP202605110001');
DELETE FROM `weiyou_wallet`.`wy_red_packet` WHERE `sender_user_id` IN (10001, 10002, 10003);
DELETE FROM `weiyou_wallet`.`wy_wallet_account` WHERE `user_id` IN (10001, 10002, 10003);

DELETE FROM `weiyou_content`.`wy_moment_media` WHERE `moment_id` IN (30001, 30002);
DELETE FROM `weiyou_content`.`wy_moment_like` WHERE `moment_id` IN (30001, 30002);
DELETE FROM `weiyou_content`.`wy_moment_comment` WHERE `moment_id` IN (30001, 30002);
DELETE FROM `weiyou_content`.`wy_moment_post` WHERE `moment_id` IN (30001, 30002);

DELETE FROM `weiyou_im`.`wy_message_00` WHERE `conversation_id` IN (90001, 90002);
DELETE FROM `weiyou_im`.`wy_conversation_user` WHERE `conversation_id` IN (90001, 90002);
DELETE FROM `weiyou_im`.`wy_conversation` WHERE `conversation_id` IN (90001, 90002);

DELETE FROM `weiyou_relation`.`wy_group_member` WHERE `group_id` IN (90002);
DELETE FROM `weiyou_relation`.`wy_group_notice` WHERE `group_id` IN (90002);
DELETE FROM `weiyou_relation`.`wy_group_info` WHERE `group_id` IN (90002);
DELETE FROM `weiyou_relation`.`wy_friend_request` WHERE `request_id` IN (50001, 50002);
DELETE FROM `weiyou_relation`.`wy_friend_relation` WHERE `owner_user_id` IN (10001, 10002, 10003);

DELETE FROM `weiyou_account`.`wy_user_status` WHERE `user_id` IN (10001, 10002, 10003);
DELETE FROM `weiyou_account`.`wy_user_qrcode` WHERE `user_id` IN (10001, 10002, 10003);
DELETE FROM `weiyou_account`.`wy_user_privacy_setting` WHERE `user_id` IN (10001, 10002, 10003);
DELETE FROM `weiyou_account`.`wy_user_device` WHERE `user_id` IN (10001, 10002, 10003);
DELETE FROM `weiyou_account`.`wy_user_profile` WHERE `user_id` IN (10001, 10002, 10003);
DELETE FROM `weiyou_account`.`wy_user_account` WHERE `user_id` IN (10001, 10002, 10003);

INSERT INTO `weiyou_account`.`wy_user_account`
(`user_id`, `weiyou_no`, `mobile`, `mobile_mask`, `password_hash`, `password_salt`, `register_source`, `account_status`, `realname_status`, `last_login_at`, `last_login_ip`, `last_login_city`, `version`, `is_deleted`, `created_by`, `updated_by`, `created_at`, `updated_at`)
VALUES
(10001, 'weiyou_10001', '13800000001', '138****0001', '$2a$10$2d.LGMPvZKgYynqVd6k0ue0Zb.fBFzNQDG14ME7PWAOHlBBjNb4sy', NULL, 'app', 0, 1, '2026-05-11 09:00:00.000', '127.0.0.1', '深圳', 0, 0, 0, 0, '2026-05-10 10:00:00.000', '2026-05-11 09:00:00.000'),
(10002, 'weiyou_10002', '13800000002', '138****0002', '$2a$10$2d.LGMPvZKgYynqVd6k0ue0Zb.fBFzNQDG14ME7PWAOHlBBjNb4sy', NULL, 'app', 0, 1, '2026-05-11 09:05:00.000', '127.0.0.1', '广州', 0, 0, 0, 0, '2026-05-10 10:05:00.000', '2026-05-11 09:05:00.000'),
(10003, 'weiyou_10003', '13800000003', '138****0003', '$2a$10$2d.LGMPvZKgYynqVd6k0ue0Zb.fBFzNQDG14ME7PWAOHlBBjNb4sy', NULL, 'app', 0, 0, '2026-05-11 08:50:00.000', '127.0.0.1', '杭州', 0, 0, 0, 0, '2026-05-10 10:10:00.000', '2026-05-11 08:50:00.000');

INSERT INTO `weiyou_account`.`wy_user_profile`
(`user_id`, `nickname`, `avatar_url`, `gender`, `country_code`, `country_name`, `province_name`, `city_name`, `signature`, `status_text`, `moment_cover_url`, `wechat_shake_text`, `version`, `is_deleted`, `created_by`, `updated_by`, `created_at`, `updated_at`)
VALUES
(10001, '微友产品体验官', 'https://weiyou.local/avatar/10001.png', 1, 'CN', '中国', '广东', '深圳', '让连接更近一点', '开会中', 'https://weiyou.local/cover/10001.jpg', '轻拍一下', 0, 0, 0, 0, '2026-05-10 10:00:00.000', '2026-05-11 09:00:00.000'),
(10002, '阿泽', 'https://weiyou.local/avatar/10002.png', 1, 'CN', '中国', '广东', '广州', '今天继续冲刺', '忙碌中', 'https://weiyou.local/cover/10002.jpg', '别拍了', 0, 0, 0, 0, '2026-05-10 10:05:00.000', '2026-05-11 09:05:00.000'),
(10003, '小林', 'https://weiyou.local/avatar/10003.png', 2, 'CN', '中国', '浙江', '杭州', '版本节奏推进中', '在线', 'https://weiyou.local/cover/10003.jpg', '收到', 0, 0, 0, 0, '2026-05-10 10:10:00.000', '2026-05-11 08:50:00.000');

INSERT INTO `weiyou_account`.`wy_user_device`
(`user_id`, `device_id`, `device_type`, `device_model`, `system_version`, `client_version`, `push_token`, `login_ip`, `login_city`, `last_login_at`, `trust_status`, `online_status`, `version`, `is_deleted`, `created_by`, `updated_by`, `created_at`, `updated_at`)
VALUES
(10001, 'device-demo-android-10001', 'android', 'Xiaomi 14', 'Android 15', '0.1.0', NULL, '127.0.0.1', '深圳', '2026-05-11 09:00:00.000', 1, 1, 0, 0, 0, 0, '2026-05-11 09:00:00.000', '2026-05-11 09:00:00.000'),
(10001, 'device-demo-web-10001', 'web', 'Chrome', '126.0', '0.1.0', NULL, '127.0.0.1', '深圳', '2026-05-11 08:40:00.000', 0, 0, 0, 0, 0, 0, '2026-05-11 08:40:00.000', '2026-05-11 08:40:00.000'),
(10002, 'device-demo-android-10002', 'android', 'iPhone 15', 'iOS 18', '0.1.0', NULL, '127.0.0.1', '广州', '2026-05-11 09:05:00.000', 1, 1, 0, 0, 0, 0, '2026-05-11 09:05:00.000', '2026-05-11 09:05:00.000');

INSERT INTO `weiyou_account`.`wy_user_privacy_setting`
(`user_id`, `add_friend_policy`, `mobile_searchable`, `weiyou_no_searchable`, `contact_match_enabled`, `moment_visible_days`, `moment_allow_stranger_view`, `blacklist_switch`, `version`, `is_deleted`, `created_by`, `updated_by`, `created_at`, `updated_at`)
VALUES
(10001, 0, 1, 1, 1, 0, 0, 1, 0, 0, 0, 0, '2026-05-10 10:00:00.000', '2026-05-11 09:00:00.000'),
(10002, 0, 1, 1, 1, 3, 0, 1, 0, 0, 0, 0, '2026-05-10 10:05:00.000', '2026-05-11 09:05:00.000');

INSERT INTO `weiyou_account`.`wy_user_status`
(`user_id`, `status_code`, `status_text`, `status_icon`, `expire_at`, `created_at`, `updated_at`)
VALUES
(10001, 'meeting', '开会中', 'https://weiyou.local/status/meeting.png', '2026-05-12 12:00:00.000', '2026-05-11 09:00:00.000', '2026-05-11 09:00:00.000'),
(10002, 'busy', '忙碌中', 'https://weiyou.local/status/busy.png', '2026-05-12 12:00:00.000', '2026-05-11 09:05:00.000', '2026-05-11 09:05:00.000');

INSERT INTO `weiyou_relation`.`wy_friend_relation`
(`owner_user_id`, `friend_user_id`, `remark`, `source`, `star_flag`, `moment_permission`, `relation_status`, `version`, `is_deleted`, `created_by`, `updated_by`, `created_at`, `updated_at`)
VALUES
(10001, 10002, '阿泽', 'group', 1, 0, 0, 0, 0, 0, 0, '2026-05-10 11:00:00.000', '2026-05-11 09:00:00.000'),
(10002, 10001, '体验官', 'group', 0, 0, 0, 0, 0, 0, 0, '2026-05-10 11:00:00.000', '2026-05-11 09:05:00.000'),
(10001, 10003, '小林', 'scan', 0, 0, 0, 0, 0, 0, 0, '2026-05-10 11:10:00.000', '2026-05-11 08:55:00.000');

INSERT INTO `weiyou_relation`.`wy_friend_request`
(`request_id`, `from_user_id`, `to_user_id`, `apply_message`, `source`, `apply_status`, `handled_at`, `handled_by`, `created_at`, `updated_at`)
VALUES
(50001, 10003, 10001, '来自项目群', 'group', 0, NULL, NULL, '2026-05-11 08:56:00.000', '2026-05-11 08:56:00.000');

INSERT INTO `weiyou_relation`.`wy_group_info`
(`group_id`, `group_no`, `group_name`, `group_avatar`, `owner_user_id`, `member_count`, `join_policy`, `mute_all_flag`, `group_status`, `version`, `is_deleted`, `created_by`, `updated_by`, `created_at`, `updated_at`)
VALUES
(90002, 'group_90002', '微友产品群', 'https://weiyou.local/group/90002.png', 10001, 3, 0, 0, 0, 0, 0, 0, 0, '2026-05-10 13:00:00.000', '2026-05-11 09:00:00.000');

INSERT INTO `weiyou_relation`.`wy_group_member`
(`group_id`, `user_id`, `role_type`, `join_source`, `join_status`, `mute_until`, `group_nickname`, `joined_at`, `created_at`, `updated_at`)
VALUES
(90002, 10001, 2, 'create', 0, NULL, '体验官', '2026-05-10 13:00:00.000', '2026-05-10 13:00:00.000', '2026-05-11 09:00:00.000'),
(90002, 10002, 0, 'invite', 0, NULL, '阿泽', '2026-05-10 13:05:00.000', '2026-05-10 13:05:00.000', '2026-05-11 09:05:00.000'),
(90002, 10003, 0, 'invite', 0, NULL, '小林', '2026-05-10 13:10:00.000', '2026-05-10 13:10:00.000', '2026-05-11 08:50:00.000');

INSERT INTO `weiyou_relation`.`wy_group_notice`
(`group_id`, `notice_content`, `publisher_user_id`, `published_at`, `updated_at`)
VALUES
(90002, '今天 18:00 前同步一期接口联调进度。', 10001, '2026-05-11 09:00:00.000', '2026-05-11 09:00:00.000');

INSERT INTO `weiyou_im`.`wy_conversation`
(`conversation_id`, `conversation_no`, `conversation_type`, `biz_id`, `last_msg_id`, `last_msg_time`, `last_msg_digest`, `status`, `created_at`, `updated_at`)
VALUES
(90001, 'conv_90001', 1, NULL, 70002, '2026-05-11 09:12:00.000', '收到，稍后我来处理', 0, '2026-05-10 14:00:00.000', '2026-05-11 09:12:00.000'),
(90002, 'conv_90002', 2, 90002, 70003, '2026-05-11 09:15:00.000', '今天同步下版本节奏', 0, '2026-05-10 14:05:00.000', '2026-05-11 09:15:00.000');

INSERT INTO `weiyou_im`.`wy_conversation_user`
(`conversation_id`, `user_id`, `unread_count`, `top_flag`, `mute_flag`, `mark_unread_flag`, `draft_content`, `last_read_seq_no`, `clear_before_time`, `sort_time`, `created_at`, `updated_at`)
VALUES
(90001, 10001, 0, 1, 0, 0, NULL, 2, NULL, '2026-05-11 09:12:00.000', '2026-05-10 14:00:00.000', '2026-05-11 09:12:00.000'),
(90001, 10002, 0, 0, 0, 0, NULL, 2, NULL, '2026-05-11 09:12:00.000', '2026-05-10 14:00:00.000', '2026-05-11 09:12:00.000'),
(90002, 10001, 1, 0, 1, 0, NULL, 1, NULL, '2026-05-11 09:15:00.000', '2026-05-10 14:05:00.000', '2026-05-11 09:15:00.000'),
(90002, 10002, 0, 0, 0, 0, NULL, 1, NULL, '2026-05-11 09:15:00.000', '2026-05-10 14:05:00.000', '2026-05-11 09:15:00.000'),
(90002, 10003, 0, 0, 0, 0, NULL, 1, NULL, '2026-05-11 09:15:00.000', '2026-05-10 14:05:00.000', '2026-05-11 09:15:00.000');

INSERT INTO `weiyou_im`.`wy_message_00`
(`message_id`, `conversation_id`, `seq_no`, `client_msg_id`, `sender_user_id`, `msg_type`, `content_json`, `reply_msg_id`, `send_status`, `send_time`, `ext_json`, `created_at`)
VALUES
(70001, 90001, 1, 'cmsg_70001', 10001, 1, '{"text":"你好，欢迎来到微友"}', NULL, 1, '2026-05-11 09:10:00.000', NULL, '2026-05-11 09:10:00.000'),
(70002, 90001, 2, 'cmsg_70002', 10002, 1, '{"text":"收到，稍后我来处理"}', NULL, 1, '2026-05-11 09:12:00.000', NULL, '2026-05-11 09:12:00.000'),
(70003, 90002, 1, 'cmsg_70003', 10001, 1, '{"text":"今天同步下版本节奏"}', NULL, 1, '2026-05-11 09:15:00.000', NULL, '2026-05-11 09:15:00.000');

INSERT INTO `weiyou_content`.`wy_moment_post`
(`moment_id`, `author_user_id`, `content_text`, `media_count`, `visible_type`, `location_name`, `longitude`, `latitude`, `comment_count`, `like_count`, `publish_status`, `ext_json`, `is_deleted`, `created_at`, `updated_at`)
VALUES
(30001, 10002, '今天把需求评审过了一遍。', 1, 0, '广州天河', 113.361200, 23.124630, 3, 12, 0, NULL, 0, '2026-05-11 08:30:00.000', '2026-05-11 08:30:00.000'),
(30002, 10003, '版本节奏推进中。', 0, 0, '杭州滨江', 120.212010, 30.208400, 1, 6, 0, NULL, 0, '2026-05-11 07:30:00.000', '2026-05-11 07:30:00.000');

INSERT INTO `weiyou_content`.`wy_moment_media`
(`moment_id`, `sort_no`, `media_type`, `media_url`, `cover_url`, `width`, `height`, `duration_ms`, `created_at`)
VALUES
(30001, 1, 1, 'https://weiyou.local/moment/30001-1.png', NULL, 1080, 1440, NULL, '2026-05-11 08:30:00.000');

INSERT INTO `weiyou_wallet`.`wy_wallet_account`
(`wallet_account_id`, `user_id`, `available_balance`, `frozen_balance`, `total_income`, `total_expense`, `wallet_status`, `realname_status`, `pay_password_status`, `version`, `created_at`, `updated_at`)
VALUES
(91001, 10001, 128000, 0, 328000, 200000, 0, 1, 1, 0, '2026-05-10 15:00:00.000', '2026-05-11 09:00:00.000'),
(91002, 10002, 56000, 0, 156000, 100000, 0, 1, 1, 0, '2026-05-10 15:05:00.000', '2026-05-11 09:05:00.000');

INSERT INTO `weiyou_wallet`.`wy_wallet_transaction`
(`transaction_id`, `transaction_no`, `biz_type`, `payer_user_id`, `payee_user_id`, `amount_fen`, `currency_code`, `transaction_status`, `idempotency_key`, `biz_order_no`, `remark`, `finish_time`, `ext_json`, `created_at`, `updated_at`)
VALUES
(80001, 'TX202605110001', 'transfer', 10001, 10002, 8800, 'CNY', 1, 'idem-80001', NULL, '转账给阿泽', '2026-05-11 07:30:00.000', NULL, '2026-05-11 07:29:50.000', '2026-05-11 07:30:00.000'),
(80002, 'TX202605110002', 'income', 10002, 10001, 12000, 'CNY', 1, 'idem-80002', NULL, '收到转账', '2026-05-11 08:20:00.000', NULL, '2026-05-11 08:19:55.000', '2026-05-11 08:20:00.000');

INSERT INTO `weiyou_wallet`.`wy_wallet_bill`
(`bill_id`, `user_id`, `transaction_no`, `bill_type`, `income_expense_type`, `amount_fen`, `bill_time`, `biz_title`, `biz_subtitle`, `created_at`)
VALUES
(60001, 10001, 'TX202605110001', 'transfer', 2, 8800, '2026-05-11 07:30:00.000', '转账给阿泽', '演示数据', '2026-05-11 07:30:00.000'),
(60002, 10001, 'TX202605110002', 'income', 1, 12000, '2026-05-11 08:20:00.000', '收到转账', '演示数据', '2026-05-11 08:20:00.000'),
(60003, 10002, 'TX202605110001', 'transfer', 1, 8800, '2026-05-11 07:30:00.000', '收到转账', '来自体验官', '2026-05-11 07:30:00.000');

INSERT INTO `weiyou_wallet`.`wy_red_packet`
(`red_packet_id`, `red_packet_no`, `sender_user_id`, `group_id`, `packet_type`, `total_amount_fen`, `packet_count`, `greeting`, `packet_status`, `expire_at`, `finished_at`, `created_at`, `updated_at`)
VALUES
(81001, 'RP202605110001', 10001, 90002, 2, 3000, 3, '大家辛苦了', 0, '2026-05-12 09:00:00.000', NULL, '2026-05-11 09:00:00.000', '2026-05-11 09:00:00.000');

INSERT INTO `weiyou_wallet`.`wy_red_packet_item`
(`red_packet_no`, `item_no`, `amount_fen`, `receive_status`, `receiver_user_id`, `received_at`, `created_at`)
VALUES
('RP202605110001', 1, 1000, 0, NULL, NULL, '2026-05-11 09:00:00.000'),
('RP202605110001', 2, 1000, 0, NULL, NULL, '2026-05-11 09:00:00.000'),
('RP202605110001', 3, 1000, 0, NULL, NULL, '2026-05-11 09:00:00.000');
