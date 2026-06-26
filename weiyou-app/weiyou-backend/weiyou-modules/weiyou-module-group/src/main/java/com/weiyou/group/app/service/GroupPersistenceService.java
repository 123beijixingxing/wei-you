package com.weiyou.group.app.service;

import com.weiyou.group.domain.entity.GroupInfoEntity;
import com.weiyou.group.domain.entity.GroupMemberEntity;
import java.util.List;
import java.util.Optional;

public interface GroupPersistenceService {

    Optional<GroupInfoEntity> findGroupById(Long groupId);

    List<GroupMemberEntity> listMembers(Long groupId);
}
