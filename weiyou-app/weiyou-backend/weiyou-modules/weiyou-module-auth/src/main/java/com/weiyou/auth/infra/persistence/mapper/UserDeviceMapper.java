package com.weiyou.auth.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weiyou.auth.domain.entity.UserDeviceEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDeviceMapper extends BaseMapper<UserDeviceEntity> {
}
