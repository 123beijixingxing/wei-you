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

    <view class="panel-lite me-list">
      <view class="me-row" @click="go('/pages/profile/edit')">
        <text class="me-label">编辑资料</text>
        <text class="utility-arrow">›</text>
      </view>
      <view class="me-row" @click="go('/pages/profile/qrcode')">
        <text class="me-label">我的二维码</text>
        <text class="utility-arrow">›</text>
      </view>
      <view class="me-row" @click="go('/pages/profile/status')">
        <text class="me-label">状态设置</text>
        <text class="utility-arrow">›</text>
      </view>
      <view class="me-row" @click="go('/pages/wallet/index')">
        <text class="me-label">服务与钱包</text>
        <text class="utility-arrow">›</text>
      </view>
      <view class="me-row" @click="go('/pages/notice/index')">
        <text class="me-label">通知中心</text>
        <text class="utility-arrow">›</text>
      </view>
      <view class="me-row" @click="go('/pages/settings/index')">
        <text class="me-label">设置中心</text>
        <text class="utility-arrow">›</text>
      </view>
    </view>

    <view class="section-title">
      <text>常用功能</text>
      <text class="section-subtitle">个人入口</text>
    </view>

    <view class="grid-2">
      <quick-entry
        v-for="item in stats"
        :key="item.label"
        :icon="item.label.slice(0, 1)"
        :title="item.label"
        :desc="item.desc || `当前共 ${item.value} 项内容`"
        @click="go(item.route)"
      />
    </view>

    <view class="actions-row">
      <view class="ghost-button" @click="refreshProfile">刷新资料</view>
      <view class="danger-button" @click="logout">退出登录</view>
    </view>
  </view>
</template>

<script setup>
import { onShow } from "@dcloudio/uni-app";
import { storeToRefs } from "pinia";
import QuickEntry from "@/components/quick-entry.vue";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const { profile, stats } = storeToRefs(userStore);

onShow(async () => {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    await userStore.fetchProfile();
  } catch (error) {
    uni.showToast({
      title: error.message || "加载资料失败",
      icon: "none"
    });
  }
});

function go(url) {
  uni.navigateTo({ url });
}

async function refreshProfile() {
  try {
    await userStore.fetchProfile();
    uni.showToast({
      title: "已刷新",
      icon: "success"
    });
  } catch (error) {
    uni.showToast({
      title: error.message || "刷新失败",
      icon: "none"
    });
  }
}

function logout() {
  userStore.logout();
}
</script>

<style scoped lang="css">
.profile-card {
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.profile-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.panel-lite {
  border-radius: 18rpx;
  background: #fff;
  border: 1rpx solid rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.me-list {
  margin-top: 18rpx;
}

.me-row {
  min-height: 92rpx;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.me-row:last-child {
  border-bottom: none;
}

.me-label {
  font-size: 28rpx;
  color: var(--wy-text);
}

.utility-arrow {
  font-size: 30rpx;
  color: #a0a0a0;
}

.actions-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 20rpx;
}

.actions-row .ghost-button,
.danger-button {
  cursor: pointer;
}

.danger-button {
  height: 72rpx;
  padding: 0 24rpx;
  border-radius: 18rpx;
  background: rgba(197, 77, 67, 0.10);
  border: 1rpx solid rgba(197, 77, 67, 0.12);
  color: #c54d43;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 700;
}

@media (max-width: 899px) {
  .profile-card {
    align-items: flex-start;
  }
}
</style>
