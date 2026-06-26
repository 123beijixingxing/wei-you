package com.weiyou.auth.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.weiyou.auth.app.service.AuthPersistenceService;
import com.weiyou.auth.domain.entity.UserAccountEntity;
import com.weiyou.auth.domain.entity.UserDeviceEntity;
import com.weiyou.auth.infra.persistence.mapper.UserAccountMapper;
import com.weiyou.auth.infra.persistence.mapper.UserDeviceMapper;
import com.weiyou.common.core.exception.BusinessException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthPersistenceServiceImpl implements AuthPersistenceService {

    private static final AtomicLong USER_ID_SEQUENCE = new AtomicLong(110000L);
    private static final ConcurrentHashMap<String, UserAccountEntity> FALLBACK_ACCOUNTS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, UserDeviceEntity> FALLBACK_DEVICES = new ConcurrentHashMap<>();

    private final UserAccountMapper userAccountMapper;
    private final UserDeviceMapper userDeviceMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthPersistenceServiceImpl(UserAccountMapper userAccountMapper,
                                      UserDeviceMapper userDeviceMapper,
                                      PasswordEncoder passwordEncoder) {
        this.userAccountMapper = userAccountMapper;
        this.userDeviceMapper = userDeviceMapper;
        this.passwordEncoder = passwordEncoder;
        FALLBACK_ACCOUNTS.computeIfAbsent("13800000001", key -> buildFallbackAccount(10001L, key, "微友产品体验官", passwordEncoder.encode("123456")));
        FALLBACK_ACCOUNTS.computeIfAbsent("13800000002", key -> buildFallbackAccount(10002L, key, "阿泽", passwordEncoder.encode("123456")));
        FALLBACK_DEVICES.computeIfAbsent("device-demo-android-10001", key -> buildFallbackDevice(10001L, key, "android"));
        UserDeviceEntity secondaryDevice = FALLBACK_DEVICES.computeIfAbsent("device-demo-web-10001", key -> buildFallbackDevice(10001L, key, "web"));
        secondaryDevice.setOnlineStatus(1);
        secondaryDevice.setDeviceModel("demo-web");
    }

    @Override
    public Optional<UserAccountEntity> findAccountByMobile(String mobile) {
        try {
            return Optional.ofNullable(userAccountMapper.selectOne(
                    new LambdaQueryWrapper<UserAccountEntity>()
                            .eq(UserAccountEntity::getMobile, mobile)
                            .last("limit 1")
            ));
        } catch (RuntimeException exception) {
            return Optional.ofNullable(FALLBACK_ACCOUNTS.get(mobile));
        }
    }

    @Override
    public Optional<UserAccountEntity> authenticateByPassword(String mobile, String rawPassword) {
        Optional<UserAccountEntity> optional = findAccountByMobile(mobile);
        if (optional.isEmpty()) {
            return Optional.empty();
        }
        UserAccountEntity entity = optional.get();
        if (!passwordEncoder.matches(rawPassword, entity.getPasswordHash())) {
            throw new BusinessException(401, "mobile or password incorrect");
        }
        return Optional.of(entity);
    }

    @Override
    public List<UserDeviceEntity> listUserDevices(Long userId) {
        try {
            return userDeviceMapper.selectList(
                    new LambdaQueryWrapper<UserDeviceEntity>()
                            .eq(UserDeviceEntity::getUserId, userId)
                            .orderByDesc(UserDeviceEntity::getLastLoginAt)
            );
        } catch (RuntimeException exception) {
            return FALLBACK_DEVICES.values().stream()
                    .filter(item -> userId.equals(item.getUserId()))
                    .sorted((left, right) -> right.getLastLoginAt().compareTo(left.getLastLoginAt()))
                    .toList();
        }
    }

    @Override
    public UserAccountEntity registerAccount(String mobile, String password, String registerSource, String deviceId) {
        try {
            Optional<UserAccountEntity> existing = findAccountByMobile(mobile);
            if (existing.isPresent()) {
                touchLoginDevice(existing.get().getUserId(), deviceId, "app");
                return existing.get();
            }

            UserAccountEntity entity = new UserAccountEntity();
            long userId = USER_ID_SEQUENCE.incrementAndGet();
            entity.setUserId(userId);
            entity.setWeiyouNo("weiyou_" + userId);
            entity.setMobile(mobile);
            entity.setPasswordHash(passwordEncoder.encode(password));
            entity.setRegisterSource(registerSource);
            entity.setAccountStatus(0);
            entity.setRealnameStatus(0);
            entity.setLastLoginAt(LocalDateTime.now());
            entity.setLastLoginIp("127.0.0.1");
            entity.setLastLoginCity("local");
            userAccountMapper.insert(entity);

            touchLoginDevice(userId, deviceId, "app");
            return entity;
        } catch (RuntimeException exception) {
            UserAccountEntity existing = FALLBACK_ACCOUNTS.get(mobile);
            if (existing != null) {
                touchLoginDevice(existing.getUserId(), deviceId, "app");
                return existing;
            }
            long userId = USER_ID_SEQUENCE.incrementAndGet();
            UserAccountEntity entity = buildFallbackAccount(userId, mobile, "微友用户" + userId, passwordEncoder.encode(password));
            FALLBACK_ACCOUNTS.put(mobile, entity);
            touchLoginDevice(userId, deviceId, "app");
            return entity;
        }
    }

    @Override
    public UserDeviceEntity touchLoginDevice(Long userId, String deviceId, String deviceType) {
        try {
            UserDeviceEntity entity = userDeviceMapper.selectOne(
                    new LambdaQueryWrapper<UserDeviceEntity>()
                            .eq(UserDeviceEntity::getDeviceId, deviceId)
                            .last("limit 1")
            );
            if (entity == null) {
                entity = buildFallbackDevice(userId, deviceId, deviceType);
                userDeviceMapper.insert(entity);
                return entity;
            }
            entity.setUserId(userId);
            entity.setDeviceType(deviceType == null ? entity.getDeviceType() : deviceType);
            entity.setLastLoginAt(LocalDateTime.now());
            entity.setOnlineStatus(1);
            entity.setLoginIp("127.0.0.1");
            entity.setLoginCity("local");
            userDeviceMapper.updateById(entity);
            return entity;
        } catch (RuntimeException exception) {
            UserDeviceEntity entity = FALLBACK_DEVICES.get(deviceId);
            if (entity == null) {
                entity = buildFallbackDevice(userId, deviceId, deviceType);
                FALLBACK_DEVICES.put(deviceId, entity);
                return entity;
            }
            entity.setUserId(userId);
            entity.setDeviceType(deviceType == null ? entity.getDeviceType() : deviceType);
            entity.setLastLoginAt(LocalDateTime.now());
            entity.setOnlineStatus(1);
            entity.setLoginIp("127.0.0.1");
            entity.setLoginCity("local");
            return entity;
        }
    }

    @Override
    public void offlineDevice(String deviceId) {
        try {
            UserDeviceEntity entity = userDeviceMapper.selectOne(
                    new LambdaQueryWrapper<UserDeviceEntity>()
                            .eq(UserDeviceEntity::getDeviceId, deviceId)
                            .last("limit 1")
            );
            if (entity == null) {
                return;
            }
            entity.setOnlineStatus(0);
            userDeviceMapper.updateById(entity);
        } catch (RuntimeException exception) {
            UserDeviceEntity entity = FALLBACK_DEVICES.get(deviceId);
            if (entity != null) {
                entity.setOnlineStatus(0);
            }
        }
    }

    private UserAccountEntity buildFallbackAccount(Long userId, String mobile, String nickname, String passwordHash) {
        UserAccountEntity entity = new UserAccountEntity();
        entity.setUserId(userId);
        entity.setWeiyouNo("weiyou_" + userId);
        entity.setMobile(mobile);
        entity.setPasswordHash(passwordHash);
        entity.setRegisterSource("demo");
        entity.setAccountStatus(0);
        entity.setRealnameStatus(1);
        entity.setLastLoginAt(LocalDateTime.now());
        entity.setLastLoginIp("127.0.0.1");
        entity.setLastLoginCity("local");
        return entity;
    }

    private UserDeviceEntity buildFallbackDevice(Long userId, String deviceId, String deviceType) {
        UserDeviceEntity entity = new UserDeviceEntity();
        entity.setUserId(userId);
        entity.setDeviceId(deviceId);
        entity.setDeviceType(deviceType == null ? "app" : deviceType);
        entity.setDeviceModel("demo-device");
        entity.setSystemVersion("demo-os");
        entity.setClientVersion("0.1.0");
        entity.setLoginIp("127.0.0.1");
        entity.setLoginCity("local");
        entity.setLastLoginAt(LocalDateTime.now());
        entity.setTrustStatus(1);
        entity.setOnlineStatus(1);
        return entity;
    }
}
