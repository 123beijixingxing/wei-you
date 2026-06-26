package com.weiyou.group.app.service.impl;

import com.weiyou.group.app.service.GroupPersistenceService;
import com.weiyou.group.domain.entity.GroupInfoEntity;
import com.weiyou.group.domain.entity.GroupMemberEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class GroupPersistenceServiceImpl implements GroupPersistenceService {

    @Override
    public Optional<GroupInfoEntity> findGroupById(Long groupId) {
        return Optional.empty();
    }

    @Override
    public List<GroupMemberEntity> listMembers(Long groupId) {
        return List.of();
    }
}
