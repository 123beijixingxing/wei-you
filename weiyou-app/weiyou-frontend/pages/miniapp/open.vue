<template>
  <view class="page-shell">
    <view class="hero-card">
      <text class="title-lg">{{ miniApp.appName }}</text>
      <text class="body-sm muted">通过真实接口拿到小程序会话后，这里先展示打开结果与参数，后续可继续扩展成容器页。</text>
    </view>

    <view class="panel-lite miniapp-list">
      <view class="miniapp-row" @click="toggleFavorite">
        <text class="miniapp-label">{{ miniApp.favorite ? "取消收藏" : "收藏小程序" }}</text>
        <text class="utility-arrow">›</text>
      </view>
      <view class="miniapp-row" @click="goFavorites">
        <text class="miniapp-label">收藏列表</text>
        <text class="utility-arrow">›</text>
      </view>
      <view class="miniapp-row" @click="goRecent">
        <text class="miniapp-label">最近使用</text>
        <text class="utility-arrow">›</text>
      </view>
    </view>

    <view class="panel miniapp-panel">
      <text class="title-md">打开结果</text>
      <text class="body-sm muted detail-line">AppId：{{ miniApp.appId }}</text>
      <text class="body-sm muted detail-line">Path：{{ miniApp.path }}</text>
      <text class="body-sm muted detail-line">Session：{{ miniApp.sessionToken }}</text>
      <text class="body-sm muted detail-line">Expire：{{ miniApp.expireAt }}</text>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { miniAppApi } from "@/api/modules";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const appId = ref("miniapp-demo-001");
const miniApp = reactive({
  appId: "miniapp-demo-001",
  appName: "微友商城",
  sessionToken: "",
  path: "/pages/index/index",
  expireAt: "",
  favorite: false
});

onLoad((query) => {
  appId.value = query?.appId || "miniapp-demo-001";
  if (query?.title) {
    miniApp.appName = query.title;
  }
  openMiniApp();
});

async function openMiniApp() {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    const data = await miniAppApi.open({
      appId: appId.value,
      path: "/pages/index/index",
      scene: "feature-hub"
    });
    Object.assign(miniApp, data || {});
  } catch (error) {
    uni.showToast({
      title: error.message || "打开小程序失败",
      icon: "none"
    });
  }
}

function goRecent() {
  uni.navigateTo({
    url: "/pages/miniapp/recent"
  });
}

function goFavorites() {
  uni.navigateTo({
    url: "/pages/miniapp/favorites"
  });
}

async function toggleFavorite() {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    const action = miniApp.favorite ? "unfavorite" : "favorite";
    const result = await miniAppApi.toggleFavorite({
      appId: appId.value,
      action
    });
    miniApp.favorite = result?.favorite ?? miniApp.favorite;
    uni.showToast({
      title: miniApp.favorite ? "已收藏" : "已取消收藏",
      icon: "success"
    });
  } catch (error) {
    uni.showToast({
      title: error.message || "操作失败",
      icon: "none"
    });
  }
}
</script>

<style scoped lang="css">
.miniapp-panel {
  margin-top: 22rpx;
}

.panel-lite {
  border-radius: 18rpx;
  background: #fff;
  border: 1rpx solid rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.miniapp-list {
  margin-top: 18rpx;
}

.miniapp-row {
  min-height: 92rpx;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.miniapp-row:last-child {
  border-bottom: none;
}

.miniapp-label {
  font-size: 28rpx;
  color: var(--wy-text);
}

.utility-arrow {
  font-size: 30rpx;
  color: #a0a0a0;
}

.detail-line {
  display: block;
  margin-top: 12rpx;
  word-break: break-all;
}
</style>
