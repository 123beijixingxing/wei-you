package com.weiyou.group.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weiyou.group.domain.entity.GroupInfoEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GroupInfoMapper extends BaseMapper<GroupInfoEntity> {
}
