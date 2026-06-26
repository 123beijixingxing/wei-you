<template>
  <view class="page-shell wechat-shell">
    <view class="wechat-header simple">
      <text class="wechat-title">发现</text>
    </view>

    <view v-if="discoverySections.length" class="discover-list panel-lite">
      <view v-for="section in discoverySections" :key="section.title">
        <view v-for="item in section.items" :key="item.code" class="discover-row" @click="open(item.route)">
          <view class="discover-main">
            <view class="discover-icon">{{ item.title.slice(0, 1) }}</view>
            <view class="discover-meta">
              <text class="discover-title">{{ item.title }}</text>
              <text class="discover-desc">{{ item.subtitle }}</text>
            </view>
          </view>
          <text class="utility-arrow">›</text>
        </view>
      </view>
    </view>

    <view v-else class="panel empty-panel">
      <text class="title-md">发现页入口还没加载出来</text>
      <text class="body-sm muted empty-copy">可以先从扫一扫、搜一搜和功能中心继续联调，确认生态入口已联通。</text>
      <view class="empty-actions">
        <view class="action-button" @click="open('/pages/scan/index')">扫一扫</view>
        <view class="ghost-button" @click="open('/pages/feature-hub/index')">功能中心</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onShow } from "@dcloudio/uni-app";
import { storeToRefs } from "pinia";
import { useAppStore } from "@/stores/app";
import { useUserStore } from "@/stores/user";

const appStore = useAppStore();
const userStore = useUserStore();
const { discoverySections } = storeToRefs(appStore);

onShow(async () => {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    await appStore.fetchDiscoverySections();
  } catch (error) {
    uni.showToast({ title: error.message || "加载发现页失败", icon: "none" });
  }
});

function open(url) {
  uni.navigateTo({ url });
}
</script>

<style scoped lang="css">
.wechat-shell {
  padding-top: 0;
}

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
  overflow: hidden;
}

.discover-row {
  min-height: 104rpx;
  padding: 18rpx 22rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.discover-row:last-child {
  border-bottom: none;
}

.discover-main {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
}

.discover-icon {
  width: 56rpx;
  height: 56rpx;
  border-radius: 16rpx;
  background: rgba(31, 138, 112, 0.10);
  color: var(--wy-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  font-weight: 700;
}

.discover-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.discover-title {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--wy-text);
}

.discover-desc {
  font-size: 22rpx;
  color: var(--wy-subtext);
}

.utility-arrow {
  font-size: 30rpx;
  color: #a0a0a0;
}

.empty-panel {
  margin-top: 18rpx;
}

.empty-copy {
  display: block;
  margin-top: 12rpx;
}

.empty-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 18rpx;
}
</style>
