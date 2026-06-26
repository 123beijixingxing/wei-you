package com.weiyou.common.security.token;

public record TokenPayload(Long userId, String deviceId, long expireAt, String tokenType) {
}
