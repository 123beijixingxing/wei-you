package com.weiyou.user.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.weiyou.user.app.service.UserPersistenceService;
import com.weiyou.user.domain.entity.UserProfileEntity;
import com.weiyou.user.infra.persistence.mapper.UserProfileMapper;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UserPersistenceServiceImpl implements UserPersistenceService {

    private static final ConcurrentHashMap<Long, UserProfileEntity> FALLBACK_PROFILES = new ConcurrentHashMap<>();

    private final UserProfileMapper userProfileMapper;

    public UserPersistenceServiceImpl(UserProfileMapper userProfileMapper) {
        this.userProfileMapper = userProfileMapper;
        FALLBACK_PROFILES.computeIfAbsent(10001L, this::buildDemoProfile);
        FALLBACK_PROFILES.computeIfAbsent(10002L, this::buildFriendProfile);
    }

    @Override
    public Optional<UserProfileEntity> findProfileByUserId(Long userId) {
        try {
            return Optional.ofNullable(userProfileMapper.selectOne(
                    new LambdaQueryWrapper<UserProfileEntity>()
                            .eq(UserProfileEntity::getUserId, userId)
                            .last("limit 1")
            ));
        } catch (RuntimeException exception) {
            return Optional.ofNullable(FALLBACK_PROFILES.get(userId));
        }
    }

    @Override
    public UserProfileEntity saveOrUpdateProfile(Long userId, String nickname, String avatar, Integer gender, String city, String signature) {
        try {
            UserProfileEntity entity = findProfileByUserId(userId).orElseGet(UserProfileEntity::new);
            if (entity.getUserId() == null) {
                entity.setUserId(userId);
            }
            entity.setNickname(nickname == null || nickname.isBlank() ? defaultNickname(userId) : nickname);
            entity.setAvatarUrl(avatar == null || avatar.isBlank() ? entity.getAvatarUrl() : avatar);
            entity.setGender(gender == null ? entity.getGender() : gender);
            entity.setCityName(city == null || city.isBlank() ? entity.getCityName() : city);
            entity.setProvinceName(entity.getProvinceName() == null ? city : entity.getProvinceName());
            entity.setSignature(signature == null ? entity.getSignature() : signature);
            if (entity.getId() == null) {
                userProfileMapper.insert(entity);
            } else {
                userProfileMapper.updateById(entity);
            }
            return entity;
        } catch (RuntimeException exception) {
            UserProfileEntity entity = FALLBACK_PROFILES.computeIfAbsent(userId, this::buildFallbackProfile);
            entity.setNickname(nickname == null || nickname.isBlank() ? defaultNickname(userId) : nickname);
            entity.setAvatarUrl(avatar == null || avatar.isBlank() ? entity.getAvatarUrl() : avatar);
            entity.setGender(gender == null ? entity.getGender() : gender);
            entity.setCityName(city == null || city.isBlank() ? entity.getCityName() : city);
            entity.setProvinceName(entity.getProvinceName() == null ? city : entity.getProvinceName());
            entity.setSignature(signature == null ? entity.getSignature() : signature);
            return entity;
        }
    }

    @Override
    public UserProfileEntity updateStatus(Long userId, String statusCode, String statusText, String expireAt) {
        try {
            UserProfileEntity entity = findProfileByUserId(userId).orElseGet(UserProfileEntity::new);
            if (entity.getUserId() == null) {
                entity.setUserId(userId);
                entity.setNickname(defaultNickname(userId));
            }
            entity.setStatusText(statusText == null || statusText.isBlank() ? statusCode : statusText);
            if (entity.getId() == null) {
                userProfileMapper.insert(entity);
            } else {
                userProfileMapper.updateById(entity);
            }
            return entity;
        } catch (RuntimeException exception) {
            UserProfileEntity entity = FALLBACK_PROFILES.computeIfAbsent(userId, this::buildFallbackProfile);
            entity.setStatusText(statusText == null || statusText.isBlank() ? statusCode : statusText);
            return entity;
        }
    }

    private String defaultNickname(Long userId) {
        return "微友用户" + userId;
    }

    private UserProfileEntity buildDemoProfile(Long userId) {
        UserProfileEntity entity = buildFallbackProfile(userId);
        entity.setNickname("微友产品体验官");
        entity.setAvatarUrl("https://weiyou.local/avatar/10001.png");
        entity.setCityName("深圳");
        entity.setProvinceName("广东");
        entity.setSignature("让连接更近一点");
        entity.setStatusText("在线");
        return entity;
    }

    private UserProfileEntity buildFriendProfile(Long userId) {
        UserProfileEntity entity = buildFallbackProfile(userId);
        entity.setNickname("阿泽");
        entity.setAvatarUrl("https://weiyou.local/avatar/10002.png");
        entity.setCityName("广州");
        entity.setProvinceName("广东");
        entity.setSignature("今天继续冲刺");
        entity.setStatusText("忙碌中");
        return entity;
    }

    private UserProfileEntity buildFallbackProfile(Long userId) {
        UserProfileEntity entity = new UserProfileEntity();
        entity.setUserId(userId);
        entity.setNickname(defaultNickname(userId));
        entity.setAvatarUrl("https://weiyou.local/avatar/default.png");
        entity.setGender(0);
        entity.setCountryCode("CN");
        entity.setCountryName("中国");
        entity.setProvinceName("未知");
        entity.setCityName("未知");
        entity.setSignature("这个人很低调，还没有签名。");
        entity.setStatusText("在线");
        return entity;
    }
}
