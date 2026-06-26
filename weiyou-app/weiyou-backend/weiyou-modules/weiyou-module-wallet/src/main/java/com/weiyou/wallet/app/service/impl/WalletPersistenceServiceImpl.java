package com.weiyou.wallet.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.weiyou.common.core.exception.BusinessException;
import com.weiyou.wallet.app.service.WalletIdempotencyGuard;
import com.weiyou.wallet.domain.entity.RedPacketEntity;
import com.weiyou.wallet.domain.entity.RedPacketItemEntity;
import com.weiyou.wallet.domain.entity.RedPacketReceiveEntity;
import com.weiyou.wallet.app.service.WalletPersistenceService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class WalletPersistenceServiceImpl implements WalletPersistenceService {

    private static final java.time.Duration IDEMPOTENCY_TTL = java.time.Duration.ofMinutes(2);

    private final WalletAccountMapper walletAccountMapper;
    private final WalletTransactionMapper walletTransactionMapper;
    private final WalletBillMapper walletBillMapper;
    private final RedPacketMapper redPacketMapper;
    private final RedPacketItemMapper redPacketItemMapper;
    private final RedPacketReceiveMapper redPacketReceiveMapper;
    private final WalletIdempotencyGuard walletIdempotencyGuard;

    public WalletPersistenceServiceImpl(WalletAccountMapper walletAccountMapper,
                                        WalletTransactionMapper walletTransactionMapper,
                                        WalletBillMapper walletBillMapper,
                                        RedPacketMapper redPacketMapper,
                                        RedPacketItemMapper redPacketItemMapper,
                                        RedPacketReceiveMapper redPacketReceiveMapper,
                                        WalletIdempotencyGuard walletIdempotencyGuard) {
        this.walletAccountMapper = walletAccountMapper;
        this.walletTransactionMapper = walletTransactionMapper;
        this.walletBillMapper = walletBillMapper;
        this.redPacketMapper = redPacketMapper;
        this.redPacketItemMapper = redPacketItemMapper;
        this.redPacketReceiveMapper = redPacketReceiveMapper;
        this.walletIdempotencyGuard = walletIdempotencyGuard;
    }

    @Override
    public Optional<WalletAccountEntity> findWalletByUserId(Long userId) {
        return Optional.ofNullable(walletAccountMapper.selectOne(
                new LambdaQueryWrapper<WalletAccountEntity>()
                        .eq(WalletAccountEntity::getUserId, userId)
                        .last("limit 1")
        ));
    }

    @Override
    public List<WalletTransactionEntity> listTransactions(Long userId) {
        return walletTransactionMapper.selectList(
                new LambdaQueryWrapper<WalletTransactionEntity>()
                        .and(wrapper -> wrapper.eq(WalletTransactionEntity::getPayerUserId, userId)
                                .or()
                                .eq(WalletTransactionEntity::getPayeeUserId, userId))
                        .orderByDesc(WalletTransactionEntity::getCreatedAt)
                        .last("limit 50")
        );
    }

    @Override
    public List<WalletBillEntity> listBills(Long userId) {
        return walletBillMapper.selectList(
                new LambdaQueryWrapper<WalletBillEntity>()
                        .eq(WalletBillEntity::getUserId, userId)
                        .orderByDesc(WalletBillEntity::getBillTime)
                        .last("limit 50")
        );
    }

    @Override
    @Transactional
    public WalletTransactionEntity createTransfer(Long userId, Long targetUserId, Long amountFen, String remark) {
        validateTransfer(userId, targetUserId, amountFen);
        String operationKey = transferOperationKey(userId, targetUserId, amountFen, remark);
        Optional<WalletTransactionEntity> existing = walletIdempotencyGuard.getResult("transfer", operationKey)
                .flatMap(this::findTransactionByNo);
        if (existing.isPresent()) {
            return existing.get();
        }
        if (!walletIdempotencyGuard.tryAcquire("transfer", operationKey, IDEMPOTENCY_TTL)) {
            return walletIdempotencyGuard.getResult("transfer", operationKey)
                    .flatMap(this::findTransactionByNo)
                    .orElseThrow(() -> new BusinessException(409, "duplicate transfer request"));
        }
        long transactionId = System.currentTimeMillis();
        String transactionNo = "TX" + transactionId;
        LocalDateTime now = LocalDateTime.now();
        try {
            WalletAccountPair lockedPair = lockAccountsForTransfer(userId, targetUserId);
            WalletAccountEntity payer = lockedPair.payer();
            WalletAccountEntity payee = lockedPair.payee();
            debitWallet(payer, amountFen);
            creditWallet(payee, amountFen);
            walletAccountMapper.updateById(payer);
            walletAccountMapper.updateById(payee);

            WalletTransactionEntity transaction = new WalletTransactionEntity();
            transaction.setTransactionId(transactionId);
            transaction.setTransactionNo(transactionNo);
            transaction.setBizType("transfer");
            transaction.setPayerUserId(userId);
            transaction.setPayeeUserId(targetUserId);
            transaction.setAmountFen(amountFen);
            transaction.setCurrencyCode("CNY");
            transaction.setTransactionStatus(1);
            transaction.setIdempotencyKey("idem-" + transactionId);
            transaction.setRemark(remark);
            transaction.setFinishTime(now);
            walletTransactionMapper.insert(transaction);

            walletBillMapper.insert(buildBill(userId, transactionNo, "transfer", 2, amountFen, now, "转账支出", remark));
            walletBillMapper.insert(buildBill(targetUserId, transactionNo, "transfer", 1, amountFen, now, "转账收入", remark));
            walletIdempotencyGuard.saveResult("transfer", operationKey, transactionNo, IDEMPOTENCY_TTL);
            return transaction;
        } catch (RuntimeException exception) {
            walletIdempotencyGuard.release("transfer", operationKey);
            throw exception;
        }
    }

    @Override
    @Transactional
    public RedPacketEntity createRedPacket(Long userId, Long amountFen, Integer count, Integer type, String greeting, Long groupId) {
        validateRedPacket(amountFen, count);
        String operationKey = redPacketOperationKey(userId, amountFen, count, type, greeting, groupId);
        Optional<RedPacketEntity> existing = walletIdempotencyGuard.getResult("red_packet_create", operationKey)
                .flatMap(this::findRedPacketByNo);
        if (existing.isPresent()) {
            return existing.get();
        }
        if (!walletIdempotencyGuard.tryAcquire("red_packet_create", operationKey, IDEMPOTENCY_TTL)) {
            return walletIdempotencyGuard.getResult("red_packet_create", operationKey)
                    .flatMap(this::findRedPacketByNo)
                    .orElseThrow(() -> new BusinessException(409, "duplicate red packet request"));
        }
        long redPacketId = System.currentTimeMillis();
        String redPacketNo = "RP" + redPacketId;
        LocalDateTime now = LocalDateTime.now();
        try {
            WalletAccountEntity senderWallet = requireLockedWalletAccount(userId);
            debitWallet(senderWallet, amountFen);
            walletAccountMapper.updateById(senderWallet);

            RedPacketEntity entity = new RedPacketEntity();
            entity.setRedPacketId(redPacketId);
            entity.setRedPacketNo(redPacketNo);
            entity.setSenderUserId(userId);
            entity.setGroupId(groupId);
            entity.setPacketType(type);
            entity.setTotalAmountFen(amountFen);
            entity.setPacketCount(count);
            entity.setGreeting(greeting);
            entity.setPacketStatus(0);
            entity.setExpireAt(now.plusDays(1));
            redPacketMapper.insert(entity);

            for (RedPacketItemEntity item : buildRedPacketItems(redPacketNo, amountFen, count, now)) {
                redPacketItemMapper.insert(item);
            }

            WalletTransactionEntity transaction = new WalletTransactionEntity();
            transaction.setTransactionId(redPacketId);
            transaction.setTransactionNo(redPacketNo);
            transaction.setBizType("red_packet");
            transaction.setPayerUserId(userId);
            transaction.setPayeeUserId(groupId);
            transaction.setAmountFen(amountFen);
            transaction.setCurrencyCode("CNY");
            transaction.setTransactionStatus(1);
            transaction.setIdempotencyKey("idem-" + redPacketNo);
            transaction.setRemark(greeting);
            transaction.setFinishTime(now);
            walletTransactionMapper.insert(transaction);

            walletBillMapper.insert(buildBill(userId, redPacketNo, "red_packet", 2, amountFen, now, "发红包", greeting));
            walletIdempotencyGuard.saveResult("red_packet_create", operationKey, redPacketNo, IDEMPOTENCY_TTL);
            return entity;
        } catch (RuntimeException exception) {
            walletIdempotencyGuard.release("red_packet_create", operationKey);
            throw exception;
        }
    }

    @Override
    public Optional<RedPacketEntity> findRedPacketById(Long redPacketId) {
        return Optional.ofNullable(redPacketMapper.selectOne(
                new LambdaQueryWrapper<RedPacketEntity>()
                        .eq(RedPacketEntity::getRedPacketId, redPacketId)
                        .last("limit 1")
        ));
    }

    private Optional<RedPacketEntity> findRedPacketByNo(String redPacketNo) {
        return Optional.ofNullable(redPacketMapper.selectOne(
                new LambdaQueryWrapper<RedPacketEntity>()
                        .eq(RedPacketEntity::getRedPacketNo, redPacketNo)
                        .last("limit 1")
        ));
    }

    private Optional<WalletTransactionEntity> findTransactionByNo(String transactionNo) {
        return Optional.ofNullable(walletTransactionMapper.selectOne(
                new LambdaQueryWrapper<WalletTransactionEntity>()
                        .eq(WalletTransactionEntity::getTransactionNo, transactionNo)
                        .last("limit 1")
        ));
    }

    @Override
    @Transactional
    public RedPacketOpenPayload openRedPacket(Long userId, Long redPacketId) {
        RedPacketEntity packet = lockRedPacketById(redPacketId)
                .orElseThrow(() -> new BusinessException(404, "red packet not found"));
        if (packet.getPacketStatus() != null && packet.getPacketStatus() != 0) {
            throw new BusinessException(400, "red packet already finished");
        }
        if (packet.getExpireAt() != null && packet.getExpireAt().isBefore(LocalDateTime.now())) {
            packet.setPacketStatus(2);
            redPacketMapper.updateById(packet);
            throw new BusinessException(400, "red packet expired");
        }

        RedPacketReceiveEntity existed = lockReceiveRecord(packet.getRedPacketNo(), userId).orElse(null);
        if (existed != null) {
            return new RedPacketOpenPayload(packet, existed);
        }

        RedPacketItemEntity item = lockNextAvailableRedPacketItem(packet.getRedPacketNo()).orElse(null);
        if (item == null) {
            packet.setPacketStatus(1);
            packet.setFinishedAt(LocalDateTime.now());
            redPacketMapper.updateById(packet);
            throw new BusinessException(400, "red packet empty");
        }

        LocalDateTime now = LocalDateTime.now();
        item.setReceiveStatus(1);
        item.setReceiverUserId(userId);
        item.setReceivedAt(now);
        redPacketItemMapper.updateById(item);

        RedPacketReceiveEntity receive = new RedPacketReceiveEntity();
        receive.setRedPacketNo(packet.getRedPacketNo());
        receive.setReceiverUserId(userId);
        receive.setReceiveAmountFen(item.getAmountFen());
        receive.setRankNo(item.getItemNo());
        receive.setReceivedAt(now);
        redPacketReceiveMapper.insert(receive);

        WalletAccountEntity receiverWallet = getOrCreateLockedWalletAccount(userId);
        creditWallet(receiverWallet, item.getAmountFen());
        walletAccountMapper.updateById(receiverWallet);

        walletBillMapper.insert(buildBill(userId, packet.getRedPacketNo(), "red_packet_receive", 1,
                item.getAmountFen(), now, "领取红包", packet.getGreeting()));

        long remainCount = redPacketItemMapper.selectCount(
                new LambdaQueryWrapper<RedPacketItemEntity>()
                        .eq(RedPacketItemEntity::getRedPacketNo, packet.getRedPacketNo())
                        .eq(RedPacketItemEntity::getReceiveStatus, 0)
        );
        if (remainCount == 0) {
            packet.setPacketStatus(1);
            packet.setFinishedAt(now);
            redPacketMapper.updateById(packet);
        }
        return new RedPacketOpenPayload(packet, receive);
    }

    private void validateTransfer(Long userId, Long targetUserId, Long amountFen) {
        if (userId == null || targetUserId == null) {
            throw new BusinessException(400, "user required");
        }
        if (userId.equals(targetUserId)) {
            throw new BusinessException(400, "cannot transfer to self");
        }
        if (amountFen == null || amountFen <= 0) {
            throw new BusinessException(400, "invalid transfer amount");
        }
    }

    private void validateRedPacket(Long amountFen, Integer count) {
        if (amountFen == null || amountFen <= 0) {
            throw new BusinessException(400, "invalid red packet amount");
        }
        if (count == null || count <= 0) {
            throw new BusinessException(400, "invalid red packet count");
        }
        if (amountFen < count) {
            throw new BusinessException(400, "red packet amount too small");
        }
    }

    private WalletAccountEntity requireWalletAccount(Long userId) {
        return findWalletByUserId(userId)
                .orElseThrow(() -> new BusinessException(404, "wallet account not found"));
    }

    private WalletAccountEntity requireLockedWalletAccount(Long userId) {
        return lockWalletAccountByUserId(userId)
                .orElseThrow(() -> new BusinessException(404, "wallet account not found"));
    }

    private WalletAccountEntity getOrCreateWalletAccount(Long userId) {
        return findWalletByUserId(userId).orElseGet(() -> {
            WalletAccountEntity entity = new WalletAccountEntity();
            entity.setWalletAccountId(System.nanoTime());
            entity.setUserId(userId);
            entity.setAvailableBalance(0L);
            entity.setFrozenBalance(0L);
            entity.setTotalIncome(0L);
            entity.setTotalExpense(0L);
            entity.setWalletStatus(0);
            entity.setRealnameStatus(0);
            entity.setPayPasswordStatus(0);
            walletAccountMapper.insert(entity);
            return entity;
        });
    }

    private WalletAccountEntity getOrCreateLockedWalletAccount(Long userId) {
        Optional<WalletAccountEntity> locked = lockWalletAccountByUserId(userId);
        if (locked.isPresent()) {
            return locked.get();
        }
        getOrCreateWalletAccount(userId);
        return requireLockedWalletAccount(userId);
    }

    private Optional<WalletAccountEntity> lockWalletAccountByUserId(Long userId) {
        return Optional.ofNullable(walletAccountMapper.selectOne(
                new LambdaQueryWrapper<WalletAccountEntity>()
                        .eq(WalletAccountEntity::getUserId, userId)
                        .last("limit 1 for update")
        ));
    }

    private Optional<RedPacketEntity> lockRedPacketById(Long redPacketId) {
        return Optional.ofNullable(redPacketMapper.selectOne(
                new LambdaQueryWrapper<RedPacketEntity>()
                        .eq(RedPacketEntity::getRedPacketId, redPacketId)
                        .last("limit 1 for update")
        ));
    }

    private Optional<RedPacketReceiveEntity> lockReceiveRecord(String redPacketNo, Long userId) {
        return Optional.ofNullable(redPacketReceiveMapper.selectOne(
                new LambdaQueryWrapper<RedPacketReceiveEntity>()
                        .eq(RedPacketReceiveEntity::getRedPacketNo, redPacketNo)
                        .eq(RedPacketReceiveEntity::getReceiverUserId, userId)
                        .last("limit 1 for update")
        ));
    }

    private Optional<RedPacketItemEntity> lockNextAvailableRedPacketItem(String redPacketNo) {
        return Optional.ofNullable(redPacketItemMapper.selectOne(
                new LambdaQueryWrapper<RedPacketItemEntity>()
                        .eq(RedPacketItemEntity::getRedPacketNo, redPacketNo)
                        .eq(RedPacketItemEntity::getReceiveStatus, 0)
                        .orderByAsc(RedPacketItemEntity::getItemNo)
                        .last("limit 1 for update")
        ));
    }

    private WalletAccountPair lockAccountsForTransfer(Long payerUserId, Long payeeUserId) {
        Long firstUserId = Math.min(payerUserId, payeeUserId);
        Long secondUserId = Math.max(payerUserId, payeeUserId);

        WalletAccountEntity first = firstUserId.equals(payerUserId)
                ? requireLockedWalletAccount(firstUserId)
                : getOrCreateLockedWalletAccount(firstUserId);
        WalletAccountEntity second = secondUserId.equals(payerUserId)
                ? requireLockedWalletAccount(secondUserId)
                : getOrCreateLockedWalletAccount(secondUserId);

        WalletAccountEntity payer = payerUserId.equals(firstUserId) ? first : second;
        WalletAccountEntity payee = payeeUserId.equals(firstUserId) ? first : second;
        return new WalletAccountPair(payer, payee);
    }

    private void debitWallet(WalletAccountEntity entity, Long amountFen) {
        long available = entity.getAvailableBalance() == null ? 0L : entity.getAvailableBalance();
        if (available < amountFen) {
            throw new BusinessException(400, "insufficient balance");
        }
        entity.setAvailableBalance(available - amountFen);
        entity.setTotalExpense((entity.getTotalExpense() == null ? 0L : entity.getTotalExpense()) + amountFen);
    }

    private void creditWallet(WalletAccountEntity entity, Long amountFen) {
        entity.setAvailableBalance((entity.getAvailableBalance() == null ? 0L : entity.getAvailableBalance()) + amountFen);
        entity.setTotalIncome((entity.getTotalIncome() == null ? 0L : entity.getTotalIncome()) + amountFen);
    }

    private List<RedPacketItemEntity> buildRedPacketItems(String redPacketNo, Long totalAmountFen, Integer count, LocalDateTime now) {
        List<RedPacketItemEntity> items = new ArrayList<>();
        long base = totalAmountFen / count;
        long remain = totalAmountFen % count;
        for (int i = 1; i <= count; i++) {
            RedPacketItemEntity item = new RedPacketItemEntity();
            item.setRedPacketNo(redPacketNo);
            item.setItemNo(i);
            item.setAmountFen(base + (i <= remain ? 1 : 0));
            item.setReceiveStatus(0);
            item.setCreatedAt(now);
            items.add(item);
        }
        return items;
    }

    private record WalletAccountPair(WalletAccountEntity payer, WalletAccountEntity payee) {
    }

    private WalletBillEntity buildBill(Long userId, String transactionNo, String billType, Integer incomeExpenseType,
                                       Long amountFen, LocalDateTime billTime, String billTitle, String billSubtitle) {
        WalletBillEntity bill = new WalletBillEntity();
        bill.setBillId(System.nanoTime());
        bill.setUserId(userId);
        bill.setTransactionNo(transactionNo);
        bill.setBillType(billType);
        bill.setIncomeExpenseType(incomeExpenseType);
        bill.setAmountFen(amountFen);
        bill.setBillTime(billTime);
        bill.setBizTitle(billTitle);
        bill.setBizSubtitle(billSubtitle);
        return bill;
    }

    private String transferOperationKey(Long userId, Long targetUserId, Long amountFen, String remark) {
        return userId + ":" + targetUserId + ":" + amountFen + ":" + safeText(remark);
    }

    private String redPacketOperationKey(Long userId, Long amountFen, Integer count, Integer type, String greeting, Long groupId) {
        return userId + ":" + amountFen + ":" + count + ":" + type + ":" + safeText(greeting) + ":" + groupId;
    }

    private String safeText(String text) {
        return text == null ? "" : text.trim();
    }
}
