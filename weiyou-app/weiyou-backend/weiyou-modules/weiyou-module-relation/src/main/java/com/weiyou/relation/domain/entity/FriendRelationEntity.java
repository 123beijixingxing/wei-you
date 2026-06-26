package com.weiyou.relation.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiyou.common.mybatis.entity.BaseEntity;

@TableName("weiyou_relation.wy_friend_relation")
public class FriendRelationEntity extends BaseEntity {

    private Long ownerUserId;
    private Long friendUserId;
    private String remark;
    private String source;
    private Integer starFlag;
    private Integer momentPermission;
    private Integer relationStatus;

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public Long getFriendUserId() {
        return friendUserId;
    }

    public void setFriendUserId(Long friendUserId) {
        this.friendUserId = friendUserId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Integer getStarFlag() {
        return starFlag;
    }

    public void setStarFlag(Integer starFlag) {
        this.starFlag = starFlag;
    }

    public Integer getMomentPermission() {
        return momentPermission;
    }

    public void setMomentPermission(Integer momentPermission) {
        this.momentPermission = momentPermission;
    }

    public Integer getRelationStatus() {
        return relationStatus;
    }

    public void setRelationStatus(Integer relationStatus) {
        this.relationStatus = relationStatus;
    }
}
