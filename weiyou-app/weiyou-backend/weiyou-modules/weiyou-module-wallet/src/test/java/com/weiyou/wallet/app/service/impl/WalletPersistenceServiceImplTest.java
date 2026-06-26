package com.weiyou.wallet.app.service.impl;

import com.weiyou.common.core.exception.BusinessException;
import com.weiyou.wallet.app.service.WalletIdempotencyGuard;
import com.weiyou.wallet.app.service.WalletPersistenceService.RedPacketOpenPayload;
import com.weiyou.wallet.domain.entity.RedPacketEntity;
import com.weiyou.wallet.domain.entity.RedPacketItemEntity;
import com.weiyou.wallet.domain.entity.RedPacketReceiveEntity;
import com.weiyou.wallet.domain.entity.WalletAccountEntity;
import com.weiyou.wallet.domain.entity.WalletBillEntity;
import com.weiyou.wallet.domain.entity.WalletTransactionEntity;
import com.weiyou.wallet.infra.persistence.mapper.RedPacketItemMapper;
import com.weiyou.wallet.infra.persistence.mapper.RedPacketMapper;
import com.weiyou.wallet.infra.persistence.mapper.RedPacketReceiveMapper;
import com.weiyou.wallet.infra.persistence.mapper.WalletAccountMapper;
import com.weiyou.wallet.infra.persistence.mapper.WalletBillMapper;
import com.weiyou.wallet.infra.persistence.mapper.WalletTransactionMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletPersistenceServiceImplTest {

    @Mock
    private WalletAccountMapper walletAccountMapper;
    @Mock
    private WalletTransactionMapper walletTransactionMapper;
    @Mock
    private WalletBillMapper walletBillMapper;
    @Mock
    private RedPacketMapper redPacketMapper;
    @Mock
    private RedPacketItemMapper redPacketItemMapper;
    @Mock
    private RedPacketReceiveMapper redPacketReceiveMapper;
    @Mock
    private WalletIdempotencyGuard walletIdempotencyGuard;

    private WalletPersistenceServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WalletPersistenceServiceImpl(
                walletAccountMapper,
                walletTransactionMapper,
                walletBillMapper,
                redPacketMapper,
                redPacketItemMapper,
                redPacketReceiveMapper,
                walletIdempotencyGuard
        );
    }

    @Test
    void shouldTransferWithBalanceUpdates() {
        WalletAccountEntity payer = walletAccount(10001L, 1000L);
        WalletAccountEntity payee = walletAccount(10002L, 200L);

        when(walletIdempotencyGuard.getResult(any(), any())).thenReturn(java.util.Optional.empty());
        when(walletIdempotencyGuard.tryAcquire(any(), any(), any())).thenReturn(true);
        when(walletAccountMapper.selectOne(any())).thenReturn(payer, payee);

        WalletTransactionEntity transaction = service.createTransfer(10001L, 10002L, 100L, "demo");

        Assertions.assertEquals(900L, payer.getAvailableBalance());
        Assertions.assertEquals(300L, payee.getAvailableBalance());
        Assertions.assertEquals(100L, payer.getTotalExpense());
        Assertions.assertEquals(100L, payee.getTotalIncome());
        Assertions.assertEquals("transfer", transaction.getBizType());
        verify(walletAccountMapper, times(2)).updateById(any(WalletAccountEntity.class));
        verify(walletTransactionMapper).insert(any(WalletTransactionEntity.class));
        verify(walletBillMapper, times(2)).insert(any(WalletBillEntity.class));
    }

    @Test
    void shouldRejectTransferWhenBalanceInsufficient() {
        WalletAccountEntity payer = walletAccount(10001L, 50L);
        WalletAccountEntity payee = walletAccount(10002L, 200L);

        when(walletIdempotencyGuard.getResult(any(), any())).thenReturn(java.util.Optional.empty());
        when(walletIdempotencyGuard.tryAcquire(any(), any(), any())).thenReturn(true);
        when(walletAccountMapper.selectOne(any())).thenReturn(payer, payee);

        Assertions.assertThrows(BusinessException.class,
                () -> service.createTransfer(10001L, 10002L, 100L, "demo"));

        verify(walletTransactionMapper, never()).insert(any(WalletTransactionEntity.class));
        verify(walletBillMapper, never()).insert(any(WalletBillEntity.class));
    }

    @Test
    void shouldCreateRedPacketAndSplitItems() {
        WalletAccountEntity sender = walletAccount(10001L, 1000L);
        when(walletIdempotencyGuard.getResult(any(), any())).thenReturn(java.util.Optional.empty());
        when(walletIdempotencyGuard.tryAcquire(any(), any(), any())).thenReturn(true);
        when(walletAccountMapper.selectOne(any())).thenReturn(sender);

        RedPacketEntity redPacket = service.createRedPacket(10001L, 300L, 3, 2, "good luck", 90002L);

        Assertions.assertEquals(700L, sender.getAvailableBalance());
        Assertions.assertEquals(300L, sender.getTotalExpense());
        Assertions.assertEquals(300L, redPacket.getTotalAmountFen());
        verify(redPacketItemMapper, times(3)).insert(any(RedPacketItemEntity.class));
        verify(walletTransactionMapper).insert(any(WalletTransactionEntity.class));
        verify(walletBillMapper).insert(any(WalletBillEntity.class));
    }

    @Test
    void shouldOpenRedPacketOnceAndCreditWallet() {
        RedPacketEntity redPacket = new RedPacketEntity();
        redPacket.setRedPacketId(81001L);
        redPacket.setRedPacketNo("RP202605110001");
        redPacket.setSenderUserId(10001L);
        redPacket.setTotalAmountFen(300L);
        redPacket.setPacketCount(3);
        redPacket.setPacketStatus(0);
        redPacket.setGreeting("good luck");
        redPacket.setExpireAt(LocalDateTime.now().plusDays(1));

        RedPacketItemEntity item = new RedPacketItemEntity();
        item.setRedPacketNo("RP202605110001");
        item.setItemNo(1);
        item.setAmountFen(100L);
        item.setReceiveStatus(0);

        WalletAccountEntity receiver = walletAccount(10002L, 500L);

        when(redPacketMapper.selectOne(any())).thenReturn(redPacket);
        when(redPacketReceiveMapper.selectOne(any())).thenReturn(null);
        when(redPacketItemMapper.selectOne(any())).thenReturn(item);
        when(walletAccountMapper.selectOne(any())).thenReturn(receiver);
        when(redPacketItemMapper.selectCount(any())).thenReturn(0L);

        RedPacketOpenPayload payload = service.openRedPacket(10002L, 81001L);

        Assertions.assertEquals(600L, receiver.getAvailableBalance());
        Assertions.assertEquals(100L, receiver.getTotalIncome());
        Assertions.assertEquals(1, item.getReceiveStatus());
        Assertions.assertEquals(10002L, item.getReceiverUserId());
        Assertions.assertEquals(1, payload.redPacket().getPacketStatus());
        Assertions.assertEquals(100L, payload.receiveRecord().getReceiveAmountFen());
        verify(redPacketItemMapper).updateById(item);
        verify(walletAccountMapper).updateById(receiver);
        verify(walletBillMapper).insert(any(WalletBillEntity.class));
        verify(redPacketMapper).updateById(redPacket);
        verify(redPacketReceiveMapper).insert(any(RedPacketReceiveEntity.class));
    }

    @Test
    void shouldReturnExistingTransferWhenIdempotentResultExists() {
        WalletTransactionEntity existed = new WalletTransactionEntity();
        existed.setTransactionNo("TX_EXISTED_001");
        existed.setBizType("transfer");
        existed.setAmountFen(100L);

        when(walletIdempotencyGuard.getResult(any(), any())).thenReturn(java.util.Optional.of("TX_EXISTED_001"));
        when(walletTransactionMapper.selectOne(any())).thenReturn(existed);

        WalletTransactionEntity result = service.createTransfer(10001L, 10002L, 100L, "demo");

        Assertions.assertEquals("TX_EXISTED_001", result.getTransactionNo());
        verify(walletIdempotencyGuard, never()).tryAcquire(any(), any(), any());
        verify(walletTransactionMapper, never()).insert(any(WalletTransactionEntity.class));
    }

    private WalletAccountEntity walletAccount(Long userId, Long availableBalance) {
        WalletAccountEntity entity = new WalletAccountEntity();
        entity.setWalletAccountId(userId + 80000L);
        entity.setUserId(userId);
        entity.setAvailableBalance(availableBalance);
        entity.setFrozenBalance(0L);
        entity.setTotalIncome(0L);
        entity.setTotalExpense(0L);
        entity.setWalletStatus(0);
        entity.setRealnameStatus(1);
        entity.setPayPasswordStatus(1);
        return entity;
    }
}
