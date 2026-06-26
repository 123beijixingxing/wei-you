package com.weiyou.chat.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiyou.common.mybatis.entity.BaseEntity;
import java.time.LocalDateTime;

@TableName("weiyou_im.wy_conversation_user")
public class ConversationUserEntity extends BaseEntity {

    private Long conversationId;
    private Long userId;
    private Integer unreadCount;
    private Integer topFlag;
    private Integer muteFlag;
    private Integer markUnreadFlag;
    private String draftContent;
    private Long lastReadSeqNo;
    private LocalDateTime clearBeforeTime;
    private LocalDateTime sortTime;

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Integer getTopFlag() {
        return topFlag;
    }

    public void setTopFlag(Integer topFlag) {
        this.topFlag = topFlag;
    }

    public Integer getMuteFlag() {
        return muteFlag;
    }

    public void setMuteFlag(Integer muteFlag) {
        this.muteFlag = muteFlag;
    }

    public Integer getMarkUnreadFlag() {
        return markUnreadFlag;
    }

    public void setMarkUnreadFlag(Integer markUnreadFlag) {
        this.markUnreadFlag = markUnreadFlag;
    }

    public String getDraftContent() {
        return draftContent;
    }

    public void setDraftContent(String draftContent) {
        this.draftContent = draftContent;
    }

    public Long getLastReadSeqNo() {
        return lastReadSeqNo;
    }

    public void setLastReadSeqNo(Long lastReadSeqNo) {
        this.lastReadSeqNo = lastReadSeqNo;
    }

    public LocalDateTime getClearBeforeTime() {
        return clearBeforeTime;
    }

    public void setClearBeforeTime(LocalDateTime clearBeforeTime) {
        this.clearBeforeTime = clearBeforeTime;
    }

    public LocalDateTime getSortTime() {
        return sortTime;
    }

    public void setSortTime(LocalDateTime sortTime) {
        this.sortTime = sortTime;
    }
}
