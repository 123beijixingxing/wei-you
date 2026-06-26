package com.weiyou.relation.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiyou.common.mybatis.entity.BaseEntity;
import java.time.LocalDateTime;

@TableName("weiyou_relation.wy_friend_request")
public class FriendRequestEntity extends BaseEntity {

    private Long requestId;
    private Long fromUserId;
    private Long toUserId;
    private String applyMessage;
    private String source;
    private Integer applyStatus;
    private LocalDateTime handledAt;
    private Long handledBy;

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    public String getApplyMessage() {
        return applyMessage;
    }

    public void setApplyMessage(String applyMessage) {
        this.applyMessage = applyMessage;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Integer getApplyStatus() {
        return applyStatus;
    }

    public void setApplyStatus(Integer applyStatus) {
        this.applyStatus = applyStatus;
    }

    public LocalDateTime getHandledAt() {
        return handledAt;
    }

    public void setHandledAt(LocalDateTime handledAt) {
        this.handledAt = handledAt;
    }

    public Long getHandledBy() {
        return handledBy;
    }

    public void setHandledBy(Long handledBy) {
        this.handledBy = handledBy;
    }
}
