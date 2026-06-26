<template>
  <view class="page-shell">
    <view class="wechat-header simple">
      <text class="wechat-title">表情商店</text>
    </view>

    <view v-for="item in packages" :key="item.packageId" class="panel-lite emoji-item">
      <view class="emoji-meta">
        <text class="title-md">{{ item.title }}</text>
        <text class="body-sm muted">{{ item.summary }}</text>
        <text class="body-sm muted">{{ item.active ? "当前使用中" : (item.downloaded ? "已下载" : "未下载") }}</text>
      </view>
      <view class="emoji-actions">
        <view
          class="ghost-button"
          :class="item.downloaded ? '' : 'action-download'"
          @click="download(item)"
        >{{ item.downloaded ? '已下载' : '下载' }}</view>
        <view v-if="item.downloaded && !item.active" class="ghost-button" @click="activate(item)">设为当前</view>
        <view v-if="item.downloaded && !item.active" class="ghost-button danger-button" @click="remove(item)">卸载</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { emojiApi } from "@/api/modules";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const packages = ref([]);

onShow(() => {
  loadPackages();
});

async function loadPackages() {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    packages.value = await emojiApi.list();
  } catch (error) {
    uni.showToast({ title: error.message || "加载表情失败", icon: "none" });
  }
}

async function download(item) {
  if (item.downloaded) {
    return;
  }
  try {
    const updated = await emojiApi.download({ packageId: item.packageId });
    packages.value = packages.value.map((current) => current.packageId === item.packageId ? updated : current);
    uni.showToast({ title: "已下载", icon: "success" });
  } catch (error) {
    uni.showToast({ title: error.message || "下载失败", icon: "none" });
  }
}

async function activate(item) {
  try {
    packages.value = await emojiApi.activate({ packageId: item.packageId });
    uni.showToast({ title: "已设为当前表情", icon: "success" });
  } catch (error) {
    uni.showToast({ title: error.message || "设置失败", icon: "none" });
  }
}

async function remove(item) {
  try {
    const updated = await emojiApi.remove({ packageId: item.packageId });
    packages.value = packages.value.map((current) => current.packageId === item.packageId ? updated : current);
    uni.showToast({ title: "已卸载", icon: "success" });
  } catch (error) {
    uni.showToast({ title: error.message || "卸载失败", icon: "none" });
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

.panel-lite {
  border-radius: 18rpx;
  background: #fff;
  border: 1rpx solid rgba(0, 0, 0, 0.05);
}

.emoji-item {
  display: flex;
  gap: 16rpx;
  align-items: center;
  justify-content: space-between;
  margin-top: 10rpx;
  padding: 16rpx 18rpx;
}

.emoji-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.emoji-actions {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.action-download {
  color: var(--wy-primary);
}

.danger-button {
  color: #c54d43;
}
</style>
