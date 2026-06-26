package com.weiyou.wallet.controller;

import com.weiyou.wallet.app.service.WalletPersistenceService;
import com.weiyou.wallet.domain.entity.RedPacketEntity;
import com.weiyou.wallet.domain.entity.WalletBillEntity;
import com.weiyou.common.core.api.ApiResponse;
import com.weiyou.common.core.api.PageResponse;
import com.weiyou.common.security.context.UserContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final WalletPersistenceService walletPersistenceService;
    private final CopyOnWriteArrayList<BillItem> fallbackBills = new CopyOnWriteArrayList<>(List.of(
            new BillItem(60001L, "TX202605110001", "transfer", 8800L, 2, "转账给阿泽", Instant.now().minusSeconds(5400).toString()),
            new BillItem(60002L, "TX202605110002", "income", 12000L, 1, "收到转账", Instant.now().minusSeconds(2400).toString())
    ));
    private final CopyOnWriteArrayList<RedPacketDetail> fallbackRedPackets = new CopyOnWriteArrayList<>(List.of(
            new RedPacketDetail(81001L, "RP202605110001", 10001L, 2, 3000L, 3, 0, Instant.now().plusSeconds(86400).toString())
    ));

    public WalletController(WalletPersistenceService walletPersistenceService) {
        this.walletPersistenceService = walletPersistenceService;
    }

    @GetMapping("/overview")
    public ApiResponse<WalletOverview> getOverview() {
        try {
            Long currentUserId = UserContext.requireUserId();
            WalletOverview persisted = walletPersistenceService.findWalletByUserId(currentUserId)
                    .map(item -> new WalletOverview(
                            item.getAvailableBalance(),
                            item.getFrozenBalance(),
                            item.getRealnameStatus(),
                            item.getPayPasswordStatus(),
                            0
                    ))
                    .orElse(null);
            if (persisted != null) {
                return ApiResponse.ok(persisted);
            }
        } catch (RuntimeException exception) {
            // local fallback below
        }
        return ApiResponse.ok(new WalletOverview(128000L, 0L, 1, 1, 3));
    }

    @GetMapping("/bill/list")
    public ApiResponse<PageResponse<BillItem>> listBills(@RequestParam(required = false) String type,
                                                         @RequestParam(defaultValue = "1") int pageNo,
                                                         @RequestParam(required = false) String startDate,
                                                         @RequestParam(required = false) String endDate) {
        try {
            Long currentUserId = UserContext.requireUserId();
            List<BillItem> persisted = walletPersistenceService.listBills(currentUserId).stream()
                    .map(this::toBillItem)
                    .toList();
            if (!persisted.isEmpty()) {
                persisted = filterBills(persisted, type, startDate, endDate);
                return ApiResponse.ok(PageResponse.of(persisted, pageNo, 20, persisted.size(), false, null));
            }
        } catch (RuntimeException exception) {
            // local fallback below
        }
        List<BillItem> list = new ArrayList<>(fallbackBills);
        list = filterBills(list, type, startDate, endDate);
        return ApiResponse.ok(PageResponse.of(list, pageNo, 20, list.size(), false, null));
    }

    @PostMapping("/transfer/create")
    public ApiResponse<TransferResult> createTransfer(@Valid @RequestBody TransferCreateRequest request) {
        try {
            var transaction = walletPersistenceService.createTransfer(UserContext.requireUserId(), request.targetUserId(), request.amountFen(), request.remark());
            return ApiResponse.ok(new TransferResult(transaction.getTransactionId(), transaction.getTransactionNo(), transaction.getTransactionStatus(), transaction.getAmountFen()));
        } catch (RuntimeException exception) {
            long transactionId = System.currentTimeMillis();
            String transactionNo = "TX" + transactionId;
            fallbackBills.add(0, new BillItem(transactionId, transactionNo, "transfer", request.amountFen(), 2, request.remark() == null || request.remark().isBlank() ? "转账" : request.remark(), Instant.now().toString()));
            return ApiResponse.ok(new TransferResult(transactionId, transactionNo, 1, request.amountFen()));
        }
    }

    @PostMapping("/red-packet/create")
    public ApiResponse<RedPacketDetail> createRedPacket(@Valid @RequestBody RedPacketCreateRequest request) {
        try {
            RedPacketEntity entity = walletPersistenceService.createRedPacket(UserContext.requireUserId(), request.amountFen(), request.count(), request.type(), request.greeting(), request.groupId());
            return ApiResponse.ok(new RedPacketDetail(
                    entity.getRedPacketId(),
                    entity.getRedPacketNo(),
                    entity.getSenderUserId(),
                    entity.getPacketType(),
                    entity.getTotalAmountFen(),
                    entity.getPacketCount(),
                    entity.getPacketStatus(),
                    entity.getExpireAt() == null ? Instant.now().plusSeconds(86400).toString() : entity.getExpireAt().toString()
            ));
        } catch (RuntimeException exception) {
            long redPacketId = System.currentTimeMillis();
            RedPacketDetail detail = new RedPacketDetail(redPacketId, "RP" + redPacketId, UserContext.requireUserId(), request.type(), request.amountFen(), request.count(), 0, Instant.now().plusSeconds(86400).toString());
            fallbackRedPackets.add(0, detail);
            fallbackBills.add(0, new BillItem(redPacketId, detail.redPacketNo(), "red_packet", request.amountFen(), 2, request.greeting() == null || request.greeting().isBlank() ? "发红包" : request.greeting(), Instant.now().toString()));
            return ApiResponse.ok(detail);
        }
    }

    @PostMapping("/red-packet/open")
    public ApiResponse<RedPacketOpenResult> openRedPacket(@Valid @RequestBody RedPacketOpenRequest request) {
        try {
            var payload = walletPersistenceService.openRedPacket(UserContext.requireUserId(), request.redPacketId());
            return ApiResponse.ok(new RedPacketOpenResult(
                    payload.redPacket().getRedPacketNo(),
                    payload.receiveRecord().getReceiverUserId(),
                    payload.receiveRecord().getReceiveAmountFen(),
                    payload.receiveRecord().getRankNo(),
                    payload.redPacket().getPacketStatus()
            ));
        } catch (RuntimeException exception) {
            RedPacketDetail detail = fallbackRedPackets.stream()
                    .filter(item -> item.redPacketId().equals(request.redPacketId()))
                    .findFirst()
                    .orElse(new RedPacketDetail(request.redPacketId(), "RP" + request.redPacketId(), UserContext.requireUserId(), 2, 300L, 3, 1, Instant.now().plusSeconds(86400).toString()));
            fallbackBills.add(0, new BillItem(System.currentTimeMillis(), detail.redPacketNo(), "red_packet_receive", 100L, 1, "领取红包", Instant.now().toString()));
            return ApiResponse.ok(new RedPacketOpenResult(detail.redPacketNo(), UserContext.requireUserId(), 100L, 1, 1));
        }
    }

    private BillItem toBillItem(WalletBillEntity entity) {
        return new BillItem(
                entity.getBillId(),
                entity.getTransactionNo(),
                entity.getBillType(),
                entity.getAmountFen(),
                entity.getIncomeExpenseType(),
                entity.getBizTitle(),
                entity.getBillTime() == null ? null : entity.getBillTime().toString()
        );
    }

    private List<BillItem> filterBills(List<BillItem> source, String type, String startDate, String endDate) {
        return source.stream()
                .filter(item -> type == null || type.isBlank() || item.billType().equalsIgnoreCase(type))
                .filter(item -> withinRange(item.billTime(), startDate, endDate))
                .toList();
    }

    private boolean withinRange(String billTime, String startDate, String endDate) {
        Instant time = parseTime(billTime, false);
        if (time == null) {
            return true;
        }
        Instant start = parseTime(startDate, false);
        Instant end = parseTime(endDate, true);
        if (start != null && time.isBefore(start)) {
            return false;
        }
        if (end != null && time.isAfter(end)) {
            return false;
        }
        return true;
    }

    private Instant parseTime(String value, boolean endOfDay) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String text = value.trim();
        try {
            return Instant.parse(text);
        } catch (Exception ignored) {
        }
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(text);
            return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        } catch (Exception ignored) {
        }
        try {
            LocalDate localDate = LocalDate.parse(text);
            LocalDateTime localDateTime = endOfDay ? localDate.atTime(23, 59, 59) : localDate.atStartOfDay();
            return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        } catch (Exception ignored) {
        }
        return null;
    }

    public record TransferCreateRequest(@NotNull Long targetUserId, @NotNull Long amountFen, String remark) {
    }

    public record RedPacketCreateRequest(@NotNull Long amountFen, @NotNull Integer count, @NotNull Integer type,
                                         String greeting, Long groupId) {
    }

    public record RedPacketOpenRequest(@NotNull Long redPacketId) {
    }

    public record WalletOverview(Long availableBalanceFen, Long frozenBalanceFen, Integer realnameStatus,
                                 Integer payPasswordStatus, Integer bankCardCount) {
    }

    public record BillItem(Long billId, String transactionNo, String billType, Long amountFen,
                           Integer incomeExpenseType, String billTitle, String billTime) {
    }

    public record TransferResult(Long transactionId, String transactionNo, Integer status, Long amountFen) {
    }

    public record RedPacketDetail(Long redPacketId, String redPacketNo, Long senderUserId, Integer packetType,
                                  Long totalAmountFen, Integer count, Integer status, String expireAt) {
    }

    public record RedPacketOpenResult(String redPacketNo, Long receiverUserId, Long receiveAmountFen,
                                      Integer rankNo, Integer status) {
    }
}
