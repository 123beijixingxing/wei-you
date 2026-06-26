package com.weiyou.common.security.context;

public record LoginUser(Long userId, String nickname, String deviceId) {
}
