package com.weiyou.chat.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weiyou.chat.domain.entity.MessageRecordEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageRecordMapper extends BaseMapper<MessageRecordEntity> {
}
