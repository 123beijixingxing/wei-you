<template>
  <view class="page-shell">
    <view class="wechat-header simple">
      <text class="wechat-title">收藏的小程序</text>
    </view>

    <view v-for="item in apps" :key="item.appId" class="panel-lite favorite-card" @click="openApp(item)">
      <image class="avatar" :src="item.iconUrl" mode="aspectFill" />
      <view class="favorite-meta">
        <text class="title-md">{{ item.appName }}</text>
        <text class="body-sm muted">{{ item.path }}</text>
        <text class="body-sm muted">{{ item.favorite ? "已收藏" : "未收藏" }}</text>
      </view>
      <text class="utility-arrow">›</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { miniAppApi } from "@/api/modules";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const apps = ref([]);

onShow(() => {
  loadFavorites();
});

async function loadFavorites() {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    apps.value = await miniAppApi.favorites();
  } catch (error) {
    uni.showToast({
      title: error.message || "加载收藏列表失败",
      icon: "none"
    });
  }
}

function openApp(item) {
  uni.navigateTo({
    url: `/pages/miniapp/open?appId=${item.appId}&title=${encodeURIComponent(item.appName)}`
  });
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

.panel-lite {
  border-radius: 18rpx;
  background: #fff;
  border: 1rpx solid rgba(0, 0, 0, 0.05);
}

.favorite-card {
  display: flex;
  gap: 16rpx;
  align-items: center;
  margin-top: 10rpx;
  padding: 16rpx 18rpx;
}

.favorite-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.utility-arrow {
  font-size: 30rpx;
  color: #a0a0a0;
}
</style>
