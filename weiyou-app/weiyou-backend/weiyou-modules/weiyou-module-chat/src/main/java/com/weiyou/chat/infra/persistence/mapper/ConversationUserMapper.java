package com.weiyou.chat.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weiyou.chat.domain.entity.ConversationUserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConversationUserMapper extends BaseMapper<ConversationUserEntity> {
}
