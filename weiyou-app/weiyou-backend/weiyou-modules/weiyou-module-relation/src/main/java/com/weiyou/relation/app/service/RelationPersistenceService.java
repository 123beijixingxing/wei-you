package com.weiyou.relation.app.service;

import com.weiyou.relation.domain.entity.FriendRelationEntity;
import com.weiyou.relation.domain.entity.FriendRequestEntity;
import java.util.List;

public interface RelationPersistenceService {

    List<FriendRelationEntity> listFriendRelations(Long userId);

    List<FriendRequestEntity> listFriendRequests(Long userId);
}
