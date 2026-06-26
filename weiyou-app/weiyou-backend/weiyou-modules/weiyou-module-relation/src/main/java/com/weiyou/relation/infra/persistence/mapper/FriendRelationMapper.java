package com.weiyou.relation.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weiyou.relation.domain.entity.FriendRelationEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FriendRelationMapper extends BaseMapper<FriendRelationEntity> {
}
