<template>
  <view class="page-shell">
    <view class="hero-card profile-card">
      <image class="avatar-lg" :src="profile.avatar" mode="aspectFill" />
      <view class="profile-meta">
        <text class="title-lg">{{ profile.nickname }}</text>
        <text class="body-sm muted">微友号：{{ profile.wechatId }}</text>
        <text class="body-sm muted">{{ profile.city }} · {{ profile.signature }}</text>
      </view>
    </view>

    <view class="panel detail-panel">
      <text class="title-md">资料概览</text>
      <text class="body-sm muted detail-line">用户 ID：{{ profile.id }}</text>
      <text class="body-sm muted detail-line">当前资料来自真实后端接口</text>
      <text class="body-sm muted detail-line">好友状态：{{ isFriend ? '已是好友' : '可添加' }}</text>
    </view>

    <view class="actions-row">
      <view class="ghost-button" @click="refreshProfile">刷新资料</view>
      <view v-if="!isFriend" class="ghost-button" @click="applyFriend">加为好友</view>
      <view class="action-button" @click="openConversation">发消息</view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { chatApi, contactApi, userApi } from "@/api/modules";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const userId = ref(0);
const conversationId = ref(0);
const isFriend = ref(false);
const profile = reactive({
  id: 0,
  nickname: "",
  wechatId: "",
  avatar: "https://weiyou.local/avatar/default.png",
  city: "",
  signature: ""
});

onLoad((query) => {
  userId.value = Number(query?.id || 0);
  conversationId.value = Number(query?.conversationId || 0);
  loadProfile();
});

async function loadProfile() {
  if (!userStore.requireAuth() || !userId.value) {
    return;
  }
  try {
    const [data, contactPage] = await Promise.all([
      userApi.detail(userId.value),
      contactApi.list("")
    ]);
    profile.id = data?.userId || userId.value;
    profile.nickname = data?.nickname || `用户${userId.value}`;
    profile.wechatId = data?.weiyouNo || `weiyou_${userId.value}`;
    profile.avatar = data?.avatar || "https://weiyou.local/avatar/default.png";
    profile.city = data?.city || "未设置";
    profile.signature = data?.signature || "这个人很低调，还没有签名。";
    isFriend.value = Boolean((contactPage?.list || []).some((item) => Number(item.userId) === Number(userId.value)));
  } catch (error) {
    uni.showToast({
      title: error.message || "加载联系人失败",
      icon: "none"
    });
  }
}

function refreshProfile() {
  loadProfile();
}

async function applyFriend() {
  try {
    await contactApi.applyFriend({
      targetUserId: userId.value,
      remark: `你好，我是 ${userStore.profile.nickname}`,
      source: "profile"
    });
    uni.showToast({ title: "已发送申请", icon: "success" });
  } catch (error) {
    uni.showToast({
      title: error.message || "申请失败",
      icon: "none"
    });
  }
}

async function openConversation() {
  try {
    let nextConversationId = conversationId.value;
    if (!nextConversationId) {
      const result = await chatApi.openSingleConversation(userId.value);
      nextConversationId = Number(result?.conversationId || 0);
      conversationId.value = nextConversationId;
    }
    if (!nextConversationId) {
      uni.showToast({ title: "创建会话失败", icon: "none" });
      return;
    }
    uni.navigateTo({
      url: `/pages/conversation/detail?id=${nextConversationId}`
    });
  } catch (error) {
    uni.showToast({
      title: error.message || "打开会话失败",
      icon: "none"
    });
  }
}
</script>

<style scoped lang="css">
.profile-card {
  display: flex;
  gap: 24rpx;
  align-items: center;
}

.profile-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.detail-panel {
  margin-top: 22rpx;
}

.detail-line {
  display: block;
  margin-top: 12rpx;
}

.actions-row {
  display: flex;
  gap: 16rpx;
  margin-top: 22rpx;
}
</style>
