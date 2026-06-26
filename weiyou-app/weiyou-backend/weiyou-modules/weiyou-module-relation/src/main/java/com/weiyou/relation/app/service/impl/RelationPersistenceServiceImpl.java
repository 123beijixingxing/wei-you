package com.weiyou.relation.app.service.impl;

import com.weiyou.relation.app.service.RelationPersistenceService;
import com.weiyou.relation.domain.entity.FriendRelationEntity;
import com.weiyou.relation.domain.entity.FriendRequestEntity;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RelationPersistenceServiceImpl implements RelationPersistenceService {

    @Override
    public List<FriendRelationEntity> listFriendRelations(Long userId) {
        return List.of();
    }

    @Override
    public List<FriendRequestEntity> listFriendRequests(Long userId) {
        return List.of();
    }
}
