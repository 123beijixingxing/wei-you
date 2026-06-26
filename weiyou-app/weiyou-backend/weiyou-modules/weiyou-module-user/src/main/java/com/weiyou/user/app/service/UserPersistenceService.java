package com.weiyou.user.app.service;

import com.weiyou.user.domain.entity.UserProfileEntity;
import java.util.Optional;

public interface UserPersistenceService {

    Optional<UserProfileEntity> findProfileByUserId(Long userId);

    UserProfileEntity saveOrUpdateProfile(Long userId, String nickname, String avatar, Integer gender, String city, String signature);

    UserProfileEntity updateStatus(Long userId, String statusCode, String statusText, String expireAt);
}
