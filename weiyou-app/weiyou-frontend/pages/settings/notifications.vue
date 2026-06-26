<template>
  <view class="page-shell">
    <view class="hero-card">
      <text class="title-lg">消息通知</text>
      <text class="body-sm muted">管理聊天提醒、朋友圈提醒、公众号提醒，以及声音和震动偏好。</text>
    </view>

    <view class="panel setting-panel">
      <view v-for="item in notificationItems" :key="item.key" class="setting-row">
        <view>
          <text class="title-md">{{ item.title }}</text>
          <text class="body-sm muted setting-desc">{{ item.desc }}</text>
        </view>
        <switch :checked="Boolean(form[item.key])" color="#0d5c52" @change="toggle(item.key, $event.detail.value)" />
      </view>
    </view>
  </view>
</template>

<script setup>
import { reactive } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { userApi } from "@/api/modules";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const form = reactive({
  messageNotification: true,
  momentNotification: true,
  officialNotification: true,
  soundEnabled: true,
  vibrationEnabled: true
});

const notificationItems = [
  { key: "messageNotification", title: "聊天消息提醒", desc: "接收单聊、群聊的新消息提醒" },
  { key: "momentNotification", title: "朋友圈提醒", desc: "接收点赞、评论等动态通知" },
  { key: "officialNotification", title: "公众号提醒", desc: "接收服务号和订阅内容通知" },
  { key: "soundEnabled", title: "通知声音", desc: "来消息时播放提示音" },
  { key: "vibrationEnabled", title: "通知震动", desc: "来消息时触发设备震动" }
];

onShow(() => {
  loadSettings();
});

async function loadSettings() {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    const data = await userApi.settingDetail();
    Object.assign(form, data || {});
  } catch (error) {
    uni.showToast({ title: error.message || "加载通知设置失败", icon: "none" });
  }
}

async function toggle(key, value) {
  if (!userStore.requireAuth()) {
    return;
  }
  form[key] = value;
  try {
    const data = await userApi.updateSetting({ [key]: value });
    Object.assign(form, data || {});
  } catch (error) {
    form[key] = !value;
    uni.showToast({ title: error.message || "保存失败", icon: "none" });
  }
}
</script>

<style scoped lang="css">
.setting-panel {
  margin-top: 22rpx;
}

.setting-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18rpx;
  padding: 20rpx 0;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.setting-row:last-child {
  border-bottom: none;
}

.setting-desc {
  display: block;
  margin-top: 8rpx;
}
</style>
