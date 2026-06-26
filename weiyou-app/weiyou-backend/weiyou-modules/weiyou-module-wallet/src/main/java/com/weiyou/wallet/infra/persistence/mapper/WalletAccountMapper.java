package com.weiyou.wallet.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weiyou.wallet.domain.entity.WalletAccountEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WalletAccountMapper extends BaseMapper<WalletAccountEntity> {
}
