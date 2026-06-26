package com.weiyou.moment.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weiyou.moment.domain.entity.MomentMediaEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MomentMediaMapper extends BaseMapper<MomentMediaEntity> {
}
