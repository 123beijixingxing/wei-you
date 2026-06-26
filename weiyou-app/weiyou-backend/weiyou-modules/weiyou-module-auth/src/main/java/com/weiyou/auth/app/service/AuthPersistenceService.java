package com.weiyou.auth.app.service;

import com.weiyou.auth.domain.entity.UserAccountEntity;
import com.weiyou.auth.domain.entity.UserDeviceEntity;
import java.util.List;
import java.util.Optional;

public interface AuthPersistenceService {

    Optional<UserAccountEntity> findAccountByMobile(String mobile);

    Optional<UserAccountEntity> authenticateByPassword(String mobile, String rawPassword);

    List<UserDeviceEntity> listUserDevices(Long userId);

    UserAccountEntity registerAccount(String mobile, String password, String registerSource, String deviceId);

    UserDeviceEntity touchLoginDevice(Long userId, String deviceId, String deviceType);

    void offlineDevice(String deviceId);
}
