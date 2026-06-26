<template>
  <view class="page-shell">
    <view class="hero-card qrcode-card">
      <image class="avatar-lg" :src="userStore.profile.avatar" mode="aspectFill" />
      <text class="title-lg">{{ userStore.profile.nickname }}</text>
      <text class="body-sm muted">微友号：{{ userStore.profile.wechatId }}</text>
    </view>

    <view class="panel qrcode-panel">
      <image class="qrcode-image" :src="qrcode.qrcodeUrl || fallbackImage" mode="aspectFit" />
      <text class="body-sm muted detail-line">票据：{{ qrcode.ticket || '未生成' }}</text>
      <text class="body-sm muted detail-line">{{ qrcode.dynamic ? '动态二维码' : '长期二维码' }} · {{ qrcode.expireAt || '长期有效' }}</text>
    </view>

    <view class="actions-row">
      <view class="ghost-button" @click="loadQrcode(false)">长期码</view>
      <view class="action-button" @click="loadQrcode(true)">动态码</view>
    </view>
  </view>
</template>

<script setup>
import { reactive } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { userApi } from "@/api/modules";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const fallbackImage = "https://weiyou.local/qrcode/default.png";
const qrcode = reactive({
  ticket: "",
  qrcodeUrl: "",
  dynamic: false,
  expireAt: ""
});

onShow(() => {
  loadQrcode(false);
});

async function loadQrcode(dynamic) {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    const data = await userApi.qrcode(dynamic);
    Object.assign(qrcode, data || {}, { dynamic });
  } catch (error) {
    uni.showToast({ title: error.message || "加载二维码失败", icon: "none" });
  }
}
</script>

<style scoped lang="css">
.qrcode-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.qrcode-panel {
  margin-top: 22rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.qrcode-image {
  width: 440rpx;
  height: 440rpx;
  border-radius: 24rpx;
  background: #fff;
}

.detail-line {
  display: block;
  margin-top: 14rpx;
}

.actions-row {
  display: flex;
  gap: 16rpx;
  margin-top: 22rpx;
}
</style>
