<template>
  <view class="page-shell">
    <view class="hero-card">
      <text class="title-lg">设备管理</text>
      <text class="body-sm muted">这里展示当前账号最近登录设备，便于继续扩展下线设备、可信设备管理等能力。</text>
    </view>

    <view v-for="item in devices" :key="item.deviceId" class="panel device-card">
      <view class="device-head">
        <text class="title-md">{{ item.deviceModel || item.deviceType }}</text>
        <text class="body-sm status-chip" :class="item.online ? 'online' : 'offline'">{{ item.online ? "在线" : "离线" }}</text>
      </view>
      <text class="body-sm muted">设备类型：{{ item.deviceType }}</text>
      <text class="body-sm muted">系统版本：{{ item.systemVersion || '未知' }}</text>
      <text class="body-sm muted">登录城市：{{ item.loginCity || '未知' }}</text>
      <text class="body-sm muted">最近登录：{{ item.lastLoginAt || '未知' }}</text>
      <view v-if="item.online" class="device-actions">
        <view class="ghost-button" @click="offline(item)">下线设备</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { authApi } from "@/api/modules";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const devices = ref([]);

onShow(() => {
  loadDevices();
});

async function loadDevices() {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    devices.value = await authApi.devices();
  } catch (error) {
    uni.showToast({
      title: error.message || "加载设备失败",
      icon: "none"
    });
  }
}

async function offline(item) {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    await authApi.offlineDevice({ deviceId: item.deviceId });
    devices.value = devices.value.map((device) => {
      if (device.deviceId !== item.deviceId) {
        return device;
      }
      return {
        ...device,
        online: false
      };
    });
    uni.showToast({
      title: "设备已下线",
      icon: "success"
    });
  } catch (error) {
    uni.showToast({
      title: error.message || "下线失败",
      icon: "none"
    });
  }
}
</script>

<style scoped lang="css">
.device-card {
  margin-top: 12rpx;
}

.device-actions {
  margin-top: 14rpx;
}

.device-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 10rpx;
}

.status-chip {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
}

.online {
  color: #0d5c52;
  background: rgba(13, 92, 82, 0.12);
}

.offline {
  color: #8b8f8c;
  background: rgba(108, 116, 111, 0.12);
}
</style>
