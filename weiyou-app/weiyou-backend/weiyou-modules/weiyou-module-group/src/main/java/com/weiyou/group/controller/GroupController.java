package com.weiyou.group.controller;

import com.weiyou.common.core.api.ApiResponse;
import com.weiyou.common.security.context.UserContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/group")
public class GroupController {

    private static final AtomicLong GROUP_ID_SEQUENCE = new AtomicLong(90003L);

    private final Map<Long, GroupAggregate> groups = new ConcurrentHashMap<>();

    public GroupController() {
        GroupAggregate productGroup = new GroupAggregate(
                90002L,
                "微友产品群",
                "https://weiyou.local/group/90002.png",
                10001L,
                false,
                0,
                "18:00 前同步一期接口联调进度。"
        );
        productGroup.members().add(new GroupMemberData(10001L, "陈微", "https://weiyou.local/avatar/10001.png", "群主", "陈微", true, true, false));
        productGroup.members().add(new GroupMemberData(10011L, "周铭", "https://weiyou.local/avatar/10011.png", "产品负责人", "周铭", false, true, false));
        productGroup.members().add(new GroupMemberData(10012L, "赵越", "https://weiyou.local/avatar/10012.png", "设计", "赵越", false, false, false));

        GroupAggregate opsGroup = new GroupAggregate(
                90003L,
                "微友运营群",
                "https://weiyou.local/group/90003.png",
                10001L,
                true,
                1,
                "今天整理活动排期。"
        );
        opsGroup.members().add(new GroupMemberData(10001L, "陈微", "https://weiyou.local/avatar/10001.png", "群主", "陈微", true, true, false));
        opsGroup.members().add(new GroupMemberData(10021L, "林夏", "https://weiyou.local/avatar/10021.png", "活动运营", "林夏", false, false, true));
        opsGroup.members().add(new GroupMemberData(10022L, "安禾", "https://weiyou.local/avatar/10022.png", "内容运营", "安禾", false, false, false));

        groups.put(productGroup.groupId(), productGroup);
        groups.put(opsGroup.groupId(), opsGroup);
    }

    @GetMapping("/my/list")
    public ApiResponse<List<GroupListItem>> myGroups(@RequestParam(required = false) String keyword) {
        Long currentUserId = UserContext.requireUserId();
        String text = keyword == null ? "" : keyword.trim().toLowerCase();
        List<GroupListItem> list = groups.values().stream()
                .filter(item -> item.members().stream().anyMatch(member -> member.userId().equals(currentUserId)))
                .filter(item -> text.isBlank() || item.groupName().toLowerCase().contains(text))
                .sorted(Comparator.comparing(GroupAggregate::groupId).reversed())
                .map(this::toListItem)
                .toList();
        return ApiResponse.ok(list);
    }

    @PostMapping("/create")
    public ApiResponse<GroupDetailData> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        long ownerUserId = UserContext.requireUserId();
        long groupId = GROUP_ID_SEQUENCE.incrementAndGet();
        GroupAggregate aggregate = new GroupAggregate(
                groupId,
                request.groupName(),
                "https://weiyou.local/group/default.png",
                ownerUserId,
                false,
                0,
                "新群已创建，快来完善群公告。"
        );
        aggregate.members().add(new GroupMemberData(ownerUserId, "当前用户", "https://weiyou.local/avatar/default.png", "群主", "我", true, true, false));
        request.memberIds().stream().distinct().forEach(memberId -> aggregate.members().add(new GroupMemberData(
                memberId,
                "成员" + memberId,
                "https://weiyou.local/avatar/default.png",
                "已邀请",
                "成员" + memberId,
                false,
                false,
                false
        )));
        groups.put(groupId, aggregate);
        return ApiResponse.ok(toDetail(aggregate));
    }

    @GetMapping("/detail")
    public ApiResponse<GroupDetailData> getGroupDetail(@RequestParam Long groupId) {
        return ApiResponse.ok(toDetail(requireGroup(groupId)));
    }

    @GetMapping("/member/list")
    public ApiResponse<List<GroupMemberData>> getGroupMembers(@RequestParam Long groupId) {
        GroupAggregate aggregate = requireGroup(groupId);
        List<GroupMemberData> list = aggregate.members().stream()
                .sorted(Comparator.comparing(GroupMemberData::owner).reversed().thenComparing(GroupMemberData::userId))
                .toList();
        return ApiResponse.ok(list);
    }

    @PostMapping("/member/invite")
    public ApiResponse<GroupDetailData> inviteMembers(@Valid @RequestBody InviteMemberRequest request) {
        GroupAggregate aggregate = requireGroup(request.groupId());
        request.memberIds().stream().distinct().forEach(memberId -> {
            boolean exists = aggregate.members().stream().anyMatch(item -> item.userId().equals(memberId));
            if (!exists) {
                aggregate.members().add(new GroupMemberData(memberId, "成员" + memberId,
                        "https://weiyou.local/avatar/default.png", "已邀请", "成员" + memberId, false, false, false));
            }
        });
        return ApiResponse.ok(toDetail(aggregate));
    }

    @PostMapping("/admin/update")
    public ApiResponse<GroupDetailData> updateAdmin(@Valid @RequestBody UpdateGroupAdminRequest request) {
        GroupAggregate aggregate = requireGroup(request.groupId());
        ensureOwner(aggregate);
        List<GroupMemberData> updatedMembers = aggregate.members().stream()
                .map(item -> item.userId().equals(request.userId())
                        ? new GroupMemberData(item.userId(), item.nickname(), item.avatar(), item.role(), item.groupNickname(), item.owner(), Boolean.TRUE.equals(request.admin()), item.muted())
                        : item)
                .toList();
        aggregate.replaceMembers(updatedMembers);
        return ApiResponse.ok(toDetail(aggregate));
    }

    @PostMapping("/member/mute")
    public ApiResponse<GroupDetailData> updateMemberMute(@Valid @RequestBody UpdateGroupMemberMuteRequest request) {
        GroupAggregate aggregate = requireGroup(request.groupId());
        ensureOwnerOrAdmin(aggregate);
        if (aggregate.ownerUserId().equals(request.userId())) {
            throw new IllegalArgumentException("cannot mute owner");
        }
        List<GroupMemberData> updatedMembers = aggregate.members().stream()
                .map(item -> item.userId().equals(request.userId())
                        ? new GroupMemberData(item.userId(), item.nickname(), item.avatar(), item.role(), item.groupNickname(), item.owner(), item.admin(), Boolean.TRUE.equals(request.muted()))
                        : item)
                .toList();
        aggregate.replaceMembers(updatedMembers);
        return ApiResponse.ok(toDetail(aggregate));
    }

    @PostMapping("/member/nickname/update")
    public ApiResponse<GroupDetailData> updateMemberNickname(@Valid @RequestBody UpdateGroupNicknameRequest request) {
        GroupAggregate aggregate = requireGroup(request.groupId());
        Long currentUserId = UserContext.requireUserId();
        if (!currentUserId.equals(request.userId()) && !aggregate.ownerUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("no permission to update nickname");
        }
        List<GroupMemberData> updatedMembers = aggregate.members().stream()
                .map(item -> item.userId().equals(request.userId())
                        ? new GroupMemberData(item.userId(), item.nickname(), item.avatar(), item.role(), request.groupNickname().trim(), item.owner(), item.admin(), item.muted())
                        : item)
                .toList();
        aggregate.replaceMembers(updatedMembers);
        return ApiResponse.ok(toDetail(aggregate));
    }

    @PostMapping("/member/remove")
    public ApiResponse<GroupDetailData> removeMember(@Valid @RequestBody RemoveGroupMemberRequest request) {
        GroupAggregate aggregate = requireGroup(request.groupId());
        if (aggregate.ownerUserId().equals(request.userId())) {
            throw new IllegalArgumentException("cannot remove owner before transfer");
        }
        boolean removed = aggregate.members().removeIf(item -> item.userId().equals(request.userId()));
        if (!removed) {
            throw new IllegalArgumentException("member not found");
        }
        return ApiResponse.ok(toDetail(aggregate));
    }

    @PostMapping("/owner/transfer")
    public ApiResponse<GroupDetailData> transferOwner(@Valid @RequestBody TransferGroupOwnerRequest request) {
        GroupAggregate aggregate = requireGroup(request.groupId());
        boolean exists = aggregate.members().stream().anyMatch(item -> item.userId().equals(request.targetUserId()));
        if (!exists) {
            throw new IllegalArgumentException("target member not found");
        }
        aggregate.ownerUserId(request.targetUserId());
            List<GroupMemberData> updatedMembers = aggregate.members().stream()
                    .map(item -> new GroupMemberData(
                            item.userId(),
                            item.nickname(),
                            item.avatar(),
                            item.userId().equals(request.targetUserId()) ? "群主" : item.role(),
                            item.groupNickname(),
                            item.userId().equals(request.targetUserId()),
                            item.userId().equals(request.targetUserId()) || item.admin(),
                            item.muted()
                    ))
                    .toList();
        aggregate.replaceMembers(updatedMembers);
        return ApiResponse.ok(toDetail(aggregate));
    }

    @PostMapping("/leave")
    public ApiResponse<GroupLeaveResult> leaveGroup(@Valid @RequestBody LeaveGroupRequest request) {
        Long currentUserId = UserContext.requireUserId();
        GroupAggregate aggregate = requireGroup(request.groupId());
        boolean exists = aggregate.members().stream().anyMatch(item -> item.userId().equals(currentUserId));
        if (!exists) {
            return ApiResponse.ok(new GroupLeaveResult(request.groupId(), true, null, false));
        }

        Long nextOwnerUserId = null;
        boolean deleted = false;
        if (aggregate.members().size() == 1) {
            groups.remove(request.groupId());
            deleted = true;
        } else if (aggregate.ownerUserId().equals(currentUserId)) {
            GroupMemberData nextOwner = aggregate.members().stream()
                    .filter(item -> !item.userId().equals(currentUserId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("next owner not found"));
            nextOwnerUserId = nextOwner.userId();
            aggregate.ownerUserId(nextOwnerUserId);
            final Long finalNextOwnerUserId = nextOwnerUserId;
            List<GroupMemberData> updatedMembers = aggregate.members().stream()
                    .filter(item -> !item.userId().equals(currentUserId))
                    .map(item -> new GroupMemberData(
                            item.userId(),
                            item.nickname(),
                            item.avatar(),
                            item.userId().equals(finalNextOwnerUserId) ? "群主" : item.role(),
                            item.groupNickname(),
                            item.userId().equals(finalNextOwnerUserId),
                            item.userId().equals(finalNextOwnerUserId) || item.admin(),
                            item.muted()
                    ))
                    .toList();
            aggregate.replaceMembers(updatedMembers);
        } else {
            aggregate.members().removeIf(item -> item.userId().equals(currentUserId));
        }
        return ApiResponse.ok(new GroupLeaveResult(request.groupId(), true, nextOwnerUserId, deleted));
    }

    @PostMapping("/notice/update")
    public ApiResponse<GroupDetailData> updateNotice(@Valid @RequestBody UpdateGroupNoticeRequest request) {
        GroupAggregate aggregate = requireGroup(request.groupId());
        aggregate.notice(request.notice().trim());
        return ApiResponse.ok(toDetail(aggregate));
    }

    @PostMapping("/setting/update")
    public ApiResponse<GroupDetailData> updateSetting(@Valid @RequestBody UpdateGroupSettingRequest request) {
        GroupAggregate aggregate = requireGroup(request.groupId());
        if (request.muteAll() != null) {
            aggregate.muteAll(request.muteAll());
        }
        if (request.joinPolicy() != null) {
            aggregate.joinPolicy(request.joinPolicy());
        }
        return ApiResponse.ok(toDetail(aggregate));
    }

    private GroupAggregate requireGroup(Long groupId) {
        return groups.computeIfAbsent(groupId, id -> {
            GroupAggregate aggregate = new GroupAggregate(
                    id,
                    "微友新群聊",
                    "https://weiyou.local/group/default.png",
                    UserContext.requireUserId(),
                    false,
                    0,
                    "欢迎来到新的群聊。"
            );
            aggregate.members().add(new GroupMemberData(UserContext.requireUserId(), "当前用户", "https://weiyou.local/avatar/default.png", "群主", "我", true, true, false));
            return aggregate;
        });
    }

    private void ensureOwner(GroupAggregate aggregate) {
        if (!aggregate.ownerUserId().equals(UserContext.requireUserId())) {
            throw new IllegalArgumentException("only owner can operate");
        }
    }

    private void ensureOwnerOrAdmin(GroupAggregate aggregate) {
        Long currentUserId = UserContext.requireUserId();
        boolean manager = aggregate.ownerUserId().equals(currentUserId)
                || aggregate.members().stream().anyMatch(item -> item.userId().equals(currentUserId) && Boolean.TRUE.equals(item.admin()));
        if (!manager) {
            throw new IllegalArgumentException("only owner or admin can operate");
        }
    }

    private GroupListItem toListItem(GroupAggregate aggregate) {
        return new GroupListItem(
                aggregate.groupId(),
                aggregate.groupName(),
                aggregate.groupAvatar(),
                aggregate.members().size(),
                aggregate.notice()
        );
    }

    private GroupDetailData toDetail(GroupAggregate aggregate) {
        return new GroupDetailData(
                aggregate.groupId(),
                aggregate.groupName(),
                aggregate.groupAvatar(),
                aggregate.ownerUserId(),
                aggregate.members().size(),
                aggregate.muteAll(),
                aggregate.joinPolicy(),
                aggregate.notice()
        );
    }

    public record CreateGroupRequest(@NotBlank String groupName, @NotEmpty List<Long> memberIds) {
    }

    public record InviteMemberRequest(@NotNull Long groupId, @NotEmpty List<Long> memberIds) {
    }

    public record UpdateGroupNoticeRequest(@NotNull Long groupId, @NotBlank String notice) {
    }

    public record UpdateGroupSettingRequest(@NotNull Long groupId, Boolean muteAll, Integer joinPolicy) {
    }

    public record UpdateGroupAdminRequest(@NotNull Long groupId, @NotNull Long userId, Boolean admin) {
    }

    public record UpdateGroupMemberMuteRequest(@NotNull Long groupId, @NotNull Long userId, Boolean muted) {
    }

    public record UpdateGroupNicknameRequest(@NotNull Long groupId, @NotNull Long userId, @NotBlank String groupNickname) {
    }

    public record RemoveGroupMemberRequest(@NotNull Long groupId, @NotNull Long userId) {
    }

    public record TransferGroupOwnerRequest(@NotNull Long groupId, @NotNull Long targetUserId) {
    }

    public record LeaveGroupRequest(@NotNull Long groupId) {
    }

    public record GroupDetailData(Long groupId, String groupName, String groupAvatar, Long ownerUserId,
                                  Integer memberCount, Boolean muteAll, Integer joinPolicy, String notice) {
    }

    public record GroupListItem(Long groupId, String groupName, String groupAvatar, Integer memberCount, String notice) {
    }

    public record GroupMemberData(Long userId, String nickname, String avatar, String role, String groupNickname, Boolean owner, Boolean admin, Boolean muted) {
    }

    public record GroupLeaveResult(Long groupId, Boolean left, Long newOwnerUserId, Boolean deleted) {
    }

    private static final class GroupAggregate {

        private final Long groupId;
        private final String groupName;
        private final String groupAvatar;
        private Long ownerUserId;
        private final List<GroupMemberData> members = new ArrayList<>();
        private boolean muteAll;
        private int joinPolicy;
        private String notice;

        private GroupAggregate(Long groupId,
                               String groupName,
                               String groupAvatar,
                               Long ownerUserId,
                               boolean muteAll,
                               int joinPolicy,
                               String notice) {
            this.groupId = groupId;
            this.groupName = groupName;
            this.groupAvatar = groupAvatar;
            this.ownerUserId = ownerUserId;
            this.muteAll = muteAll;
            this.joinPolicy = joinPolicy;
            this.notice = notice;
        }

        public Long groupId() {
            return groupId;
        }

        public String groupName() {
            return groupName;
        }

        public String groupAvatar() {
            return groupAvatar;
        }

        public Long ownerUserId() {
            return ownerUserId;
        }

        public void ownerUserId(Long ownerUserId) {
            this.ownerUserId = ownerUserId;
        }

        public List<GroupMemberData> members() {
            return members;
        }

        public boolean muteAll() {
            return muteAll;
        }

        public int joinPolicy() {
            return joinPolicy;
        }

        public String notice() {
            return notice;
        }

        public void muteAll(Boolean next) {
            this.muteAll = Boolean.TRUE.equals(next);
        }

        public void joinPolicy(Integer next) {
            if (next != null) {
                this.joinPolicy = next;
            }
        }

        public void notice(String next) {
            this.notice = next;
        }

        public void replaceMembers(List<GroupMemberData> nextMembers) {
            this.members.clear();
            this.members.addAll(nextMembers);
        }
    }
}
