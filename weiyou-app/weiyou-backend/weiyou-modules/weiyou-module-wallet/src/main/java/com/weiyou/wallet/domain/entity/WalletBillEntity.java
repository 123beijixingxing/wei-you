package com.weiyou.wallet.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiyou.common.mybatis.entity.BaseEntity;
import java.time.LocalDateTime;

@TableName("weiyou_wallet.wy_wallet_bill")
public class WalletBillEntity extends BaseEntity {

    private Long billId;
    private Long userId;
    private String transactionNo;
    private String billType;
    private Integer incomeExpenseType;
    private Long amountFen;
    private LocalDateTime billTime;
    private String bizTitle;
    private String bizSubtitle;

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTransactionNo() {
        return transactionNo;
    }

    public void setTransactionNo(String transactionNo) {
        this.transactionNo = transactionNo;
    }

    public String getBillType() {
        return billType;
    }

    public void setBillType(String billType) {
        this.billType = billType;
    }

    public Integer getIncomeExpenseType() {
        return incomeExpenseType;
    }

    public void setIncomeExpenseType(Integer incomeExpenseType) {
        this.incomeExpenseType = incomeExpenseType;
    }

    public Long getAmountFen() {
        return amountFen;
    }

    public void setAmountFen(Long amountFen) {
        this.amountFen = amountFen;
    }

    public LocalDateTime getBillTime() {
        return billTime;
    }

    public void setBillTime(LocalDateTime billTime) {
        this.billTime = billTime;
    }

    public String getBizTitle() {
        return bizTitle;
    }

    public void setBizTitle(String bizTitle) {
        this.bizTitle = bizTitle;
    }

    public String getBizSubtitle() {
        return bizSubtitle;
    }

    public void setBizSubtitle(String bizSubtitle) {
        this.bizSubtitle = bizSubtitle;
    }
}
