package com.weiyou.wallet.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiyou.common.mybatis.entity.BaseEntity;

@TableName("weiyou_wallet.wy_wallet_account")
public class WalletAccountEntity extends BaseEntity {

    private Long walletAccountId;
    private Long userId;
    private Long availableBalance;
    private Long frozenBalance;
    private Long totalIncome;
    private Long totalExpense;
    private Integer walletStatus;
    private Integer realnameStatus;
    private Integer payPasswordStatus;

    public Long getWalletAccountId() {
        return walletAccountId;
    }

    public void setWalletAccountId(Long walletAccountId) {
        this.walletAccountId = walletAccountId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(Long availableBalance) {
        this.availableBalance = availableBalance;
    }

    public Long getFrozenBalance() {
        return frozenBalance;
    }

    public void setFrozenBalance(Long frozenBalance) {
        this.frozenBalance = frozenBalance;
    }

    public Long getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(Long totalIncome) {
        this.totalIncome = totalIncome;
    }

    public Long getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(Long totalExpense) {
        this.totalExpense = totalExpense;
    }

    public Integer getWalletStatus() {
        return walletStatus;
    }

    public void setWalletStatus(Integer walletStatus) {
        this.walletStatus = walletStatus;
    }

    public Integer getRealnameStatus() {
        return realnameStatus;
    }

    public void setRealnameStatus(Integer realnameStatus) {
        this.realnameStatus = realnameStatus;
    }

    public Integer getPayPasswordStatus() {
        return payPasswordStatus;
    }

    public void setPayPasswordStatus(Integer payPasswordStatus) {
        this.payPasswordStatus = payPasswordStatus;
    }
}
