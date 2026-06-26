package com.weiyou.wallet.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiyou.common.mybatis.entity.BaseEntity;
import java.time.LocalDateTime;

@TableName("weiyou_wallet.wy_red_packet_receive")
public class RedPacketReceiveEntity extends BaseEntity {

    private String redPacketNo;
    private Long receiverUserId;
    private Long receiveAmountFen;
    private Integer rankNo;
    private LocalDateTime receivedAt;

    public String getRedPacketNo() {
        return redPacketNo;
    }

    public void setRedPacketNo(String redPacketNo) {
        this.redPacketNo = redPacketNo;
    }

    public Long getReceiverUserId() {
        return receiverUserId;
    }

    public void setReceiverUserId(Long receiverUserId) {
        this.receiverUserId = receiverUserId;
    }

    public Long getReceiveAmountFen() {
        return receiveAmountFen;
    }

    public void setReceiveAmountFen(Long receiveAmountFen) {
        this.receiveAmountFen = receiveAmountFen;
    }

    public Integer getRankNo() {
        return rankNo;
    }

    public void setRankNo(Integer rankNo) {
        this.rankNo = rankNo;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }
}
