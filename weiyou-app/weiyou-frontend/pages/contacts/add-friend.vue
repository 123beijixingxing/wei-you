<template>
  <view class="page-shell">
    <view class="wechat-header simple">
      <text class="wechat-title">添加朋友</text>
    </view>

    <view class="input-chip search-chip">
      <text>搜索</text>
      <input v-model="keyword" class="search-input" placeholder="搜索昵称、微友号、城市" @confirm="runSearch" />
    </view>

    <view v-if="results.length" v-for="item in results" :key="item.userId" class="panel-lite user-card">
      <image class="avatar" :src="item.avatar" mode="aspectFill" />
      <view class="user-meta">
        <text class="title-md">{{ item.nickname }}</text>
        <text class="body-sm muted">微友号：{{ item.weiyouNo }}</text>
        <text class="body-sm muted">{{ item.city }} · {{ item.summary }}</text>
      </view>
      <view class="ghost-button" @click="applyFriend(item)">申请</view>
    </view>

    <view v-else-if="keyword.trim()" class="panel empty-panel">
      <text class="title-md">没有找到匹配的候选好友</text>
      <text class="body-sm muted empty-copy">试试搜索昵称、微友号或城市，例如 `Ada`、`wy_ada`、`上海`。</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from "vue";
import { contactApi } from "@/api/modules";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const keyword = ref("");
const results = ref([]);

async function runSearch() {
  const value = keyword.value.trim();
  if (!value || !userStore.requireAuth()) {
    return;
  }
  try {
    results.value = await contactApi.search(value);
  } catch (error) {
    uni.showToast({
      title: error.message || "搜索失败",
      icon: "none"
    });
  }
}

async function applyFriend(item) {
  try {
    await contactApi.applyFriend({
      targetUserId: item.userId,
      remark: `你好，我是 ${userStore.profile.nickname}`,
      source: "search"
    });
    uni.showToast({
      title: "已发送申请",
      icon: "success"
    });
  } catch (error) {
    uni.showToast({
      title: error.message || "申请失败",
      icon: "none"
    });
  }
}
</script>

<style scoped lang="css">
.wechat-header.simple {
  padding: 18rpx 6rpx 12rpx;
}

.wechat-title {
  font-size: 34rpx;
  font-weight: 700;
}

.search-chip {
  margin-top: 18rpx;
}

.search-input {
  flex: 1;
  height: 100%;
  font-size: 26rpx;
}

.panel-lite {
  border-radius: 18rpx;
  background: #fff;
  border: 1rpx solid rgba(0, 0, 0, 0.05);
}

.user-card {
  display: flex;
  gap: 16rpx;
  align-items: center;
  margin-top: 10rpx;
  padding: 16rpx 18rpx;
}

.user-card .ghost-button {
  cursor: pointer;
}

.user-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.empty-panel {
  margin-top: 12rpx;
}

.empty-copy {
  display: block;
  margin-top: 12rpx;
}

</style>
