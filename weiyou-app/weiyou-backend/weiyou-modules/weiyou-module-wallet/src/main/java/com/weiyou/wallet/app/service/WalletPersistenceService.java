package com.weiyou.wallet.app.service;

import com.weiyou.wallet.domain.entity.WalletAccountEntity;
import com.weiyou.wallet.domain.entity.WalletBillEntity;
import com.weiyou.wallet.domain.entity.RedPacketEntity;
import com.weiyou.wallet.domain.entity.RedPacketReceiveEntity;
import com.weiyou.wallet.domain.entity.WalletTransactionEntity;
import java.util.List;
import java.util.Optional;

public interface WalletPersistenceService {

    Optional<WalletAccountEntity> findWalletByUserId(Long userId);

    List<WalletTransactionEntity> listTransactions(Long userId);

    List<WalletBillEntity> listBills(Long userId);

    WalletTransactionEntity createTransfer(Long userId, Long targetUserId, Long amountFen, String remark);

    RedPacketEntity createRedPacket(Long userId, Long amountFen, Integer count, Integer type, String greeting, Long groupId);

    Optional<RedPacketEntity> findRedPacketById(Long redPacketId);

    RedPacketOpenPayload openRedPacket(Long userId, Long redPacketId);

    record RedPacketOpenPayload(RedPacketEntity redPacket, RedPacketReceiveEntity receiveRecord) {
    }
}
