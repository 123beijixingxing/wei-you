package com.weiyou.group.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiyou.common.mybatis.entity.BaseEntity;

@TableName("weiyou_relation.wy_group_info")
public class GroupInfoEntity extends BaseEntity {

    private Long groupId;
    private String groupNo;
    private String groupName;
    private String groupAvatar;
    private Long ownerUserId;
    private Integer memberCount;
    private Integer joinPolicy;
    private Integer muteAllFlag;
    private Integer groupStatus;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupNo() {
        return groupNo;
    }

    public void setGroupNo(String groupNo) {
        this.groupNo = groupNo;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupAvatar() {
        return groupAvatar;
    }

    public void setGroupAvatar(String groupAvatar) {
        this.groupAvatar = groupAvatar;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public Integer getJoinPolicy() {
        return joinPolicy;
    }

    public void setJoinPolicy(Integer joinPolicy) {
        this.joinPolicy = joinPolicy;
    }

    public Integer getMuteAllFlag() {
        return muteAllFlag;
    }

    public void setMuteAllFlag(Integer muteAllFlag) {
        this.muteAllFlag = muteAllFlag;
    }

    public Integer getGroupStatus() {
        return groupStatus;
    }

    public void setGroupStatus(Integer groupStatus) {
        this.groupStatus = groupStatus;
    }
}
