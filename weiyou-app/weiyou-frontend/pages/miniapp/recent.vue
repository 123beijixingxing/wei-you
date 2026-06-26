<template>
  <view class="page-shell">
    <view class="hero-card">
      <text class="title-lg">最近使用的小程序</text>
      <text class="body-sm muted">这里展示真实接口返回的小程序最近使用记录，便于继续扩展收藏、搜索和容器化运行。</text>
    </view>

    <view class="input-chip search-chip">
      <text>搜索</text>
      <input v-model="keyword" class="search-input" placeholder="搜索小程序名称" />
    </view>

    <view v-for="item in filteredApps" :key="item.appId" class="panel-lite recent-card">
      <image class="avatar" :src="item.iconUrl" mode="aspectFill" />
      <view class="recent-meta">
        <text class="title-md">{{ item.appName }}</text>
        <text class="body-sm muted">{{ item.path }}</text>
        <text class="body-sm muted">最近使用：{{ item.lastUsedAt }}{{ item.favorite ? ' · 已收藏' : '' }}</text>
      </view>
      <view class="recent-actions">
        <view class="ghost-button recent-button" @click="openApp(item)">打开</view>
        <view class="ghost-button recent-button" @click="removeApp(item)">移除</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { miniAppApi } from "@/api/modules";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const apps = ref([]);
const keyword = ref("");

const filteredApps = computed(() => {
  const text = keyword.value.trim().toLowerCase();
  if (!text) {
    return apps.value;
  }
  return apps.value.filter((item) => (item.appName || "").toLowerCase().includes(text));
});

onShow(() => {
  loadRecent();
});

async function loadRecent() {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    apps.value = await miniAppApi.recent();
  } catch (error) {
    uni.showToast({
      title: error.message || "加载最近使用失败",
      icon: "none"
    });
  }
}

function openApp(item) {
  uni.navigateTo({
    url: `/pages/miniapp/open?appId=${item.appId}&title=${encodeURIComponent(item.appName)}`
  });
}

async function removeApp(item) {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    const list = await miniAppApi.removeRecent({ appId: item.appId });
    apps.value = list || [];
    uni.showToast({
      title: "已移除",
      icon: "success"
    });
  } catch (error) {
    uni.showToast({
      title: error.message || "移除失败",
      icon: "none"
    });
  }
}
</script>

<style scoped lang="css">
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

.recent-card {
  display: flex;
  gap: 16rpx;
  align-items: center;
  margin-top: 10rpx;
  padding: 16rpx 18rpx;
}

.recent-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.recent-actions {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.recent-button {
  min-width: 96rpx;
  text-align: center;
}
</style>
