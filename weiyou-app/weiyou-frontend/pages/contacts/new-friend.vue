<template>
  <view class="page-shell">
    <view class="hero-card">
      <text class="title-lg">新的朋友</text>
      <text class="body-sm muted">这里展示最新的好友申请，并支持通过或拒绝。</text>
    </view>

    <view v-for="item in requests" :key="item.requestId" class="panel request-card">
      <image class="avatar" :src="item.avatar" mode="aspectFill" />
      <view class="request-meta">
        <text class="title-md">{{ item.nickname }}</text>
        <text class="body-sm muted">{{ item.applyMessage }}</text>
        <text class="body-sm muted">状态：{{ statusText(item.status) }}</text>
      </view>
      <view v-if="item.status === 0" class="request-actions">
        <view class="ghost-button" @click="handle(item, 'reject')">拒绝</view>
        <view class="action-button" @click="handle(item, 'accept')">通过</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { contactApi } from "@/api/modules";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const requests = ref([]);

onShow(() => {
  loadRequests();
});

async function loadRequests() {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    const page = await contactApi.friendRequests();
    requests.value = page?.list || [];
  } catch (error) {
    uni.showToast({
      title: error.message || "加载好友申请失败",
      icon: "none"
    });
  }
}

async function handle(item, action) {
  try {
    const result = await contactApi.handleFriendRequest({
      requestId: item.requestId,
      action
    });
    requests.value = requests.value.map((request) => {
      if (request.requestId !== item.requestId) {
        return request;
      }
      return result || request;
    });
    uni.showToast({
      title: action === "accept" ? "已通过" : "已拒绝",
      icon: "success"
    });
  } catch (error) {
    uni.showToast({
      title: error.message || "处理失败",
      icon: "none"
    });
  }
}

function statusText(status) {
  return ({ 0: "待处理", 1: "已通过", 2: "已拒绝" })[status] || "未知";
}
</script>

<style scoped lang="css">
.request-card {
  display: flex;
  gap: 18rpx;
  align-items: center;
  margin-top: 18rpx;
}

.request-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.request-actions {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}
</style>
