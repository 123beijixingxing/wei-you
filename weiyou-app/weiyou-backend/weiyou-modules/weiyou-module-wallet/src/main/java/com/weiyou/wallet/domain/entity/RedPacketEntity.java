package com.weiyou.wallet.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiyou.common.mybatis.entity.BaseEntity;
import java.time.LocalDateTime;

@TableName("weiyou_wallet.wy_red_packet")
public class RedPacketEntity extends BaseEntity {

    private Long redPacketId;
    private String redPacketNo;
    private Long senderUserId;
    private Long groupId;
    private Integer packetType;
    private Long totalAmountFen;
    private Integer packetCount;
    private String greeting;
    private Integer packetStatus;
    private LocalDateTime expireAt;
    private LocalDateTime finishedAt;

    public Long getRedPacketId() {
        return redPacketId;
    }

    public void setRedPacketId(Long redPacketId) {
        this.redPacketId = redPacketId;
    }

    public String getRedPacketNo() {
        return redPacketNo;
    }

    public void setRedPacketNo(String redPacketNo) {
        this.redPacketNo = redPacketNo;
    }

    public Long getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(Long senderUserId) {
        this.senderUserId = senderUserId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Integer getPacketType() {
        return packetType;
    }

    public void setPacketType(Integer packetType) {
        this.packetType = packetType;
    }

    public Long getTotalAmountFen() {
        return totalAmountFen;
    }

    public void setTotalAmountFen(Long totalAmountFen) {
        this.totalAmountFen = totalAmountFen;
    }

    public Integer getPacketCount() {
        return packetCount;
    }

    public void setPacketCount(Integer packetCount) {
        this.packetCount = packetCount;
    }

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    public Integer getPacketStatus() {
        return packetStatus;
    }

    public void setPacketStatus(Integer packetStatus) {
        this.packetStatus = packetStatus;
    }

    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }
}
