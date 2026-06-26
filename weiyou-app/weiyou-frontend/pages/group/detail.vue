<template>
  <view class="page-shell">
    <view class="hero-card group-hero">
      <image class="avatar-lg" :src="detail.groupAvatar || 'https://weiyou.local/group/default.png'" mode="aspectFill" />
      <text class="title-lg">{{ detail.groupName }}</text>
      <text class="body-sm muted">群成员：{{ detail.memberCount || 0 }} · 群主：{{ detail.ownerUserId }}</text>
      <view class="hero-tags">
        <text class="status-tag">{{ detail.muteAll ? '全员禁言' : '自由发言' }}</text>
        <text class="status-tag">{{ joinPolicyLabel }}</text>
      </view>
    </view>

    <view class="panel detail-panel">
      <view class="panel-head">
        <text class="title-md">群公告</text>
        <text class="body-sm muted">可直接修改并保存</text>
      </view>
      <textarea v-model="noticeText" class="notice-textarea" maxlength="200" placeholder="输入群公告内容" />
      <view class="panel-actions">
        <view class="action-button" :class="savingNotice ? 'is-disabled' : ''" @click="saveNotice">
          {{ savingNotice ? '保存中...' : '保存公告' }}
        </view>
      </view>
    </view>

    <view class="panel detail-panel">
      <view class="panel-head">
        <text class="title-md">群设置</text>
        <text class="body-sm muted">群管理能力演示</text>
      </view>
      <view class="setting-row">
        <text class="body-md">发言模式</text>
        <view class="switch-row">
          <view class="ghost-button mini-btn" @click="toggleMuteAll">{{ detail.muteAll ? '取消禁言' : '开启禁言' }}</view>
        </view>
      </view>
      <view class="setting-row">
        <text class="body-md">入群方式</text>
        <view class="policy-row">
          <view class="policy-chip" :class="detail.joinPolicy === 0 ? 'policy-chip-active' : ''" @click="changeJoinPolicy(0)">自由加入</view>
          <view class="policy-chip" :class="detail.joinPolicy === 1 ? 'policy-chip-active' : ''" @click="changeJoinPolicy(1)">需审核</view>
        </view>
      </view>
    </view>

    <view class="panel detail-panel">
      <view class="panel-head">
        <text class="title-md">我的群昵称</text>
        <text class="body-sm muted">支持自定义在本群中的昵称</text>
      </view>
      <view class="field">
        <text class="field__label">群昵称</text>
        <input v-model="myGroupNickname" class="field__input" placeholder="输入我的群昵称" />
      </view>
      <view class="panel-actions">
        <view class="action-button" :class="savingNickname ? 'is-disabled' : ''" @click="saveMyGroupNickname">
          {{ savingNickname ? '保存中...' : '保存群昵称' }}
        </view>
      </view>
    </view>

    <view class="panel detail-panel">
      <view class="panel-head">
        <text class="title-md">群成员</text>
        <text class="body-sm muted">{{ members.length }} 位成员</text>
      </view>
      <view class="member-grid">
        <view v-for="member in members" :key="member.userId" class="member-card" @click="handleMemberAction(member)">
          <image class="avatar" :src="member.avatar || 'https://weiyou.local/avatar/default.png'" mode="aspectFill" />
          <text class="member-name">{{ member.nickname }}</text>
          <text class="member-role">{{ member.groupNickname || member.nickname }}</text>
          <view class="member-badges">
            <text class="member-badge owner-badge" v-if="member.owner">群主</text>
            <text class="member-badge admin-badge" v-else-if="member.admin">管理员</text>
            <text class="member-badge mute-badge" v-if="member.muted">已禁言</text>
          </view>
          <text class="member-role muted">{{ member.role || '成员' }}</text>
          <text v-if="isCurrentOwner && !member.owner" class="member-action">管理</text>
        </view>
      </view>
    </view>

    <view class="panel invite-panel">
      <view class="panel-head">
        <text class="title-md">邀请成员</text>
        <text class="body-sm muted">输入成员 ID 进行邀请</text>
      </view>
      <view class="field">
        <text class="field__label">成员 ID（逗号分隔）</text>
        <input v-model="memberIdsText" class="field__input" placeholder="例如 10011,10012" />
      </view>
      <view class="panel-actions">
        <view class="action-button" :class="submitting ? 'is-disabled' : ''" @click="inviteMembers">{{ submitting ? '邀请中...' : '邀请成员' }}</view>
      </view>
    </view>

    <view class="panel danger-panel">
      <view class="panel-head">
        <text class="title-md">退出群聊</text>
        <text class="body-sm muted">当前支持成员主动退出</text>
      </view>
      <view class="danger-button" :class="leaving ? 'is-disabled' : ''" @click="leaveGroup">
        {{ leaving ? '退出中...' : (isCurrentOwner ? '退出群聊（自动转让）' : '退出群聊') }}
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { groupApi } from "@/api/modules";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const groupId = ref(90002);
const submitting = ref(false);
const savingNotice = ref(false);
const savingNickname = ref(false);
const leaving = ref(false);
const members = ref([]);
const memberIdsText = ref("");
const noticeText = ref("");
const myGroupNickname = ref("");
const detail = reactive({
  groupId: 90002,
  groupName: "",
  groupAvatar: "",
  ownerUserId: 0,
  memberCount: 0,
  muteAll: false,
  joinPolicy: 0,
  notice: ""
});

const joinPolicyLabel = computed(() => detail.joinPolicy === 1 ? "需审核入群" : "自由加入");
const currentUserId = computed(() => Number(userStore.profile.id || 0));
const isCurrentOwner = computed(() => Number(detail.ownerUserId || 0) === currentUserId.value);
const currentMember = computed(() => members.value.find((item) => Number(item.userId) === currentUserId.value) || null);
const isCurrentAdmin = computed(() => Boolean(currentMember.value?.admin) || isCurrentOwner.value);

onLoad(async (query) => {
  groupId.value = Number(query?.groupId || 90002);
  await loadAll();
});

async function loadAll() {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    const [groupDetail, memberList] = await Promise.all([
      groupApi.detail(groupId.value),
      groupApi.members(groupId.value)
    ]);
    Object.assign(detail, groupDetail || {});
    noticeText.value = detail.notice || "";
    members.value = memberList || [];
    myGroupNickname.value = currentMember.value?.groupNickname || userStore.profile.nickname || "";
  } catch (error) {
    uni.showToast({ title: error.message || "加载群详情失败", icon: "none" });
  }
}

async function refreshMembers() {
  members.value = await groupApi.members(groupId.value);
  myGroupNickname.value = currentMember.value?.groupNickname || userStore.profile.nickname || "";
}

async function handleMemberAction(member) {
  if (!member) {
    return;
  }
  if (Number(member.userId) === currentUserId.value) {
    myGroupNickname.value = member.groupNickname || member.nickname || "";
    uni.showToast({ title: "可在“我的群昵称”中修改", icon: "none" });
    return;
  }
  if (!isCurrentAdmin.value || member.owner) {
    uni.showToast({ title: member.owner ? "当前群主" : `${member.nickname}`, icon: "none" });
    return;
  }
  const actions = [
    member.admin ? "取消管理员" : "设为管理员",
    member.muted ? "取消单独禁言" : "单独禁言",
    "设置群昵称"
  ];
  if (isCurrentOwner.value) {
    actions.push("转让群主", "移出群聊");
  }
  uni.showActionSheet({
    itemList: actions,
    success: async ({ tapIndex }) => {
      const action = actions[tapIndex];
      if (action === "设为管理员" || action === "取消管理员") {
        await toggleAdmin(member);
        return;
      }
      if (action === "单独禁言" || action === "取消单独禁言") {
        await toggleMemberMute(member);
        return;
      }
      if (action === "设置群昵称") {
        await updateMemberNickname(member);
        return;
      }
      if (action === "转让群主") {
        await transferOwner(member);
        return;
      }
      if (action === "移出群聊") {
        await removeMember(member);
      }
    }
  });
}

async function toggleAdmin(member) {
  try {
    const data = await groupApi.updateAdmin({
      groupId: groupId.value,
      userId: member.userId,
      admin: !member.admin
    });
    Object.assign(detail, data || {});
    await refreshMembers();
    uni.showToast({ title: member.admin ? `已取消 ${member.nickname} 管理员` : `已设 ${member.nickname} 为管理员`, icon: "success" });
  } catch (error) {
    uni.showToast({ title: error.message || "更新管理员失败", icon: "none" });
  }
}

async function toggleMemberMute(member) {
  try {
    const data = await groupApi.updateMemberMute({
      groupId: groupId.value,
      userId: member.userId,
      muted: !member.muted
    });
    Object.assign(detail, data || {});
    await refreshMembers();
    uni.showToast({ title: member.muted ? `已取消禁言 ${member.nickname}` : `已禁言 ${member.nickname}`, icon: "success" });
  } catch (error) {
    uni.showToast({ title: error.message || "更新禁言失败", icon: "none" });
  }
}

async function updateMemberNickname(member) {
  uni.showModal({
    title: `设置 ${member.nickname} 的群昵称`,
    editable: true,
    placeholderText: "输入新的群昵称",
    content: member.groupNickname || member.nickname || "",
    success: async (result) => {
      if (!result.confirm) {
        return;
      }
      const value = String(result.content || "").trim();
      if (!value) {
        uni.showToast({ title: "群昵称不能为空", icon: "none" });
        return;
      }
      try {
        const data = await groupApi.updateNickname({
          groupId: groupId.value,
          userId: member.userId,
          groupNickname: value
        });
        Object.assign(detail, data || {});
        await refreshMembers();
        uni.showToast({ title: "群昵称已更新", icon: "success" });
      } catch (error) {
        uni.showToast({ title: error.message || "更新群昵称失败", icon: "none" });
      }
    }
  });
}

async function transferOwner(member) {
  try {
    const data = await groupApi.transferOwner({
      groupId: groupId.value,
      targetUserId: member.userId
    });
    Object.assign(detail, data || {});
    await refreshMembers();
    uni.showToast({ title: `已转让给 ${member.nickname}`, icon: "success" });
  } catch (error) {
    uni.showToast({ title: error.message || "转让失败", icon: "none" });
  }
}

async function removeMember(member) {
  try {
    const data = await groupApi.removeMember({
      groupId: groupId.value,
      userId: member.userId
    });
    Object.assign(detail, data || {});
    await refreshMembers();
    uni.showToast({ title: `已移出 ${member.nickname}`, icon: "success" });
  } catch (error) {
    uni.showToast({ title: error.message || "移除失败", icon: "none" });
  }
}

async function saveMyGroupNickname() {
  if (!userStore.requireAuth() || savingNickname.value) {
    return;
  }
  const value = myGroupNickname.value.trim();
  if (!value) {
    uni.showToast({ title: "请输入群昵称", icon: "none" });
    return;
  }
  savingNickname.value = true;
  try {
    const data = await groupApi.updateNickname({
      groupId: groupId.value,
      userId: currentUserId.value,
      groupNickname: value
    });
    Object.assign(detail, data || {});
    await refreshMembers();
    uni.showToast({ title: "群昵称已保存", icon: "success" });
  } catch (error) {
    uni.showToast({ title: error.message || "保存群昵称失败", icon: "none" });
  } finally {
    savingNickname.value = false;
  }
}

async function saveNotice() {
  if (!userStore.requireAuth() || savingNotice.value) {
    return;
  }
  const value = noticeText.value.trim();
  if (!value) {
    uni.showToast({ title: "请输入群公告", icon: "none" });
    return;
  }
  savingNotice.value = true;
  try {
    const data = await groupApi.updateNotice({
      groupId: groupId.value,
      notice: value
    });
    Object.assign(detail, data || {});
    noticeText.value = detail.notice || value;
    uni.showToast({ title: "公告已更新", icon: "success" });
  } catch (error) {
    uni.showToast({ title: error.message || "保存失败", icon: "none" });
  } finally {
    savingNotice.value = false;
  }
}

async function toggleMuteAll() {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    const data = await groupApi.updateSetting({
      groupId: groupId.value,
      muteAll: !detail.muteAll
    });
    Object.assign(detail, data || {});
    uni.showToast({ title: detail.muteAll ? "已开启全员禁言" : "已取消全员禁言", icon: "none" });
  } catch (error) {
    uni.showToast({ title: error.message || "更新设置失败", icon: "none" });
  }
}

async function changeJoinPolicy(joinPolicy) {
  if (!userStore.requireAuth() || detail.joinPolicy === joinPolicy) {
    return;
  }
  try {
    const data = await groupApi.updateSetting({
      groupId: groupId.value,
      joinPolicy
    });
    Object.assign(detail, data || {});
    uni.showToast({ title: joinPolicy === 1 ? "已改为需审核" : "已改为自由加入", icon: "none" });
  } catch (error) {
    uni.showToast({ title: error.message || "更新入群方式失败", icon: "none" });
  }
}

async function inviteMembers() {
  if (!userStore.requireAuth() || submitting.value) {
    return;
  }
  const memberIds = memberIdsText.value.split(",").map((item) => Number(item.trim())).filter(Boolean);
  if (!memberIds.length) {
    uni.showToast({ title: "请输入成员 ID", icon: "none" });
    return;
  }
  submitting.value = true;
  try {
    const data = await groupApi.inviteMembers({ groupId: groupId.value, memberIds });
    Object.assign(detail, data || {});
    memberIdsText.value = "";
    await refreshMembers();
    uni.showToast({ title: "邀请成功", icon: "success" });
  } catch (error) {
    uni.showToast({ title: error.message || "邀请失败", icon: "none" });
  } finally {
    submitting.value = false;
  }
}

async function leaveGroup() {
  if (!userStore.requireAuth() || leaving.value) {
    return;
  }
  leaving.value = true;
  try {
    const result = await groupApi.leave({ groupId: groupId.value });
    const message = result?.deleted
      ? "群聊已解散"
      : (result?.newOwnerUserId ? `已退出，群主转让给 ${result.newOwnerUserId}` : "已退出群聊");
    uni.showToast({ title: message, icon: "success" });
    setTimeout(() => {
      uni.navigateBack();
    }, 400);
  } catch (error) {
    uni.showToast({ title: error.message || "退出群聊失败", icon: "none" });
  } finally {
    leaving.value = false;
  }
}
</script>

<style scoped lang="css">
.group-hero {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12rpx;
}

.hero-tags {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
}

.status-tag {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: rgba(13, 92, 82, 0.08);
  color: var(--wy-primary);
  font-size: 22rpx;
  font-weight: 700;
}

.detail-panel,
.invite-panel {
  margin-top: 22rpx;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16rpx;
}

.notice-textarea {
  width: 100%;
  min-height: 180rpx;
  margin-top: 18rpx;
  padding: 18rpx 22rpx;
  border-radius: 22rpx;
  background: rgba(255, 255, 255, 0.92);
  border: 1rpx solid rgba(13, 92, 82, 0.08);
  font-size: 28rpx;
  line-height: 1.7;
}

.panel-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 14rpx;
}

.setting-row {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  margin-top: 16rpx;
}

.switch-row,
.policy-row {
  display: flex;
  gap: 14rpx;
  flex-wrap: wrap;
}

.mini-btn {
  height: 64rpx;
  padding: 0 22rpx;
}

.policy-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 150rpx;
  height: 64rpx;
  padding: 0 20rpx;
  border-radius: 16rpx;
  background: #f6f6f6;
  color: var(--wy-primary);
  font-size: 24rpx;
  font-weight: 700;
}

.policy-chip-active {
  background: var(--wy-primary);
  color: #fff;
}

.member-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12rpx;
  margin-top: 16rpx;
}

.member-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 16rpx 12rpx;
  border-radius: 16rpx;
  background: #f6f6f6;
}

.member-badges {
  display: flex;
  gap: 8rpx;
  flex-wrap: wrap;
  justify-content: center;
}

.member-badge {
  padding: 4rpx 10rpx;
  border-radius: 999rpx;
  font-size: 18rpx;
  font-weight: 700;
}

.owner-badge {
  background: rgba(203, 124, 70, 0.14);
  color: var(--wy-accent);
}

.admin-badge {
  background: rgba(13, 92, 82, 0.12);
  color: var(--wy-primary);
}

.mute-badge {
  background: rgba(197, 77, 67, 0.12);
  color: #c54d43;
}

.member-action {
  font-size: 20rpx;
  color: var(--wy-accent);
  font-weight: 700;
}

.member-name {
  font-size: 24rpx;
  font-weight: 700;
  text-align: center;
}

.member-role {
  font-size: 20rpx;
  color: var(--wy-subtext);
  text-align: center;
}

.field {
  margin-top: 16rpx;
}

.field__label {
  display: block;
  margin-bottom: 12rpx;
  font-size: 24rpx;
  color: var(--wy-subtext);
}

.field__input {
  width: 100%;
  height: 92rpx;
  padding: 0 24rpx;
  border-radius: 18rpx;
  background: rgba(255, 255, 255, 0.96);
  border: 1rpx solid rgba(0, 0, 0, 0.05);
}

.field__textarea {
  width: 100%;
  min-height: 180rpx;
  padding: 20rpx 24rpx;
  border-radius: 18rpx;
  background: rgba(255, 255, 255, 0.96);
  border: 1rpx solid rgba(0, 0, 0, 0.05);
  line-height: 1.6;
}

.is-disabled {
  opacity: 0.7;
}

.danger-panel {
  margin-top: 18rpx;
}

.danger-button {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 72rpx;
  margin-top: 16rpx;
  border-radius: 18rpx;
  background: rgba(197, 77, 67, 0.10);
  color: #c54d43;
  font-size: 26rpx;
  font-weight: 700;
}
</style>
