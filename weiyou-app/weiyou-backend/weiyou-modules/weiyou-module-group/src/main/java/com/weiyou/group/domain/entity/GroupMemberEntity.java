package com.weiyou.group.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.weiyou.common.mybatis.entity.BaseEntity;
import java.time.LocalDateTime;

@TableName("weiyou_relation.wy_group_member")
public class GroupMemberEntity extends BaseEntity {

    private Long groupId;
    private Long userId;
    private Integer roleType;
    private String joinSource;
    private Integer joinStatus;
    private LocalDateTime muteUntil;
    private String groupNickname;
    private LocalDateTime joinedAt;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getRoleType() {
        return roleType;
    }

    public void setRoleType(Integer roleType) {
        this.roleType = roleType;
    }

    public String getJoinSource() {
        return joinSource;
    }

    public void setJoinSource(String joinSource) {
        this.joinSource = joinSource;
    }

    public Integer getJoinStatus() {
        return joinStatus;
    }

    public void setJoinStatus(Integer joinStatus) {
        this.joinStatus = joinStatus;
    }

    public LocalDateTime getMuteUntil() {
        return muteUntil;
    }

    public void setMuteUntil(LocalDateTime muteUntil) {
        this.muteUntil = muteUntil;
    }

    public String getGroupNickname() {
        return groupNickname;
    }

    public void setGroupNickname(String groupNickname) {
        this.groupNickname = groupNickname;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
