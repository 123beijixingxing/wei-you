package com.weiyou.moment.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weiyou.moment.domain.entity.MomentPostEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MomentPostMapper extends BaseMapper<MomentPostEntity> {
}
