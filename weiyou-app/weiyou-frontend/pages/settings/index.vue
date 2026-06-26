<template>
  <view class="page-shell wechat-shell">
    <view class="wechat-header simple">
      <text class="wechat-title">设置</text>
    </view>

    <view class="panel-lite settings-list">
      <view class="settings-row" @click="go('/pages/profile/edit')">
        <text class="settings-label">编辑资料</text>
        <text class="utility-arrow">›</text>
      </view>
      <view class="settings-row" @click="go('/pages/profile/qrcode')">
        <text class="settings-label">我的二维码</text>
        <text class="utility-arrow">›</text>
      </view>
      <view class="settings-row" @click="go('/pages/profile/status')">
        <text class="settings-label">状态设置</text>
        <text class="utility-arrow">›</text>
      </view>
      <view class="settings-row" @click="go('/pages/settings/notifications')">
        <text class="settings-label">消息通知</text>
        <text class="utility-arrow">›</text>
      </view>
      <view class="settings-row" @click="go('/pages/settings/privacy')">
        <text class="settings-label">隐私设置</text>
        <text class="utility-arrow">›</text>
      </view>
      <view class="settings-row" @click="go('/pages/notice/index')">
        <text class="settings-label">通知中心</text>
        <text class="utility-arrow">›</text>
      </view>
      <view class="settings-row" @click="go('/pages/settings/devices')">
        <text class="settings-label">设备管理</text>
        <text class="utility-arrow">›</text>
      </view>
    </view>

    <view class="panel setting-panel">
      <view class="setting-row">
        <view>
          <text class="title-md">消息通知</text>
          <text class="body-sm muted">控制基础通知提醒开关</text>
        </view>
        <switch :checked="settings.messageNotification" color="#1f8a70" @change="updateSetting('messageNotification', $event.detail.value)" />
      </view>

      <view class="setting-row">
        <view>
          <text class="title-md">朋友圈提醒</text>
          <text class="body-sm muted">控制朋友圈互动提醒</text>
        </view>
        <switch :checked="settings.momentNotification" color="#1f8a70" @change="updateSetting('momentNotification', $event.detail.value)" />
      </view>

      <view class="setting-row">
        <view>
          <text class="title-md">公众号提醒</text>
          <text class="body-sm muted">控制服务号和订阅通知提醒</text>
        </view>
        <switch :checked="settings.officialNotification" color="#1f8a70" @change="updateSetting('officialNotification', $event.detail.value)" />
      </view>
    </view>

    <view class="logout-wrap">
      <view class="danger-button" @click="logout">退出登录</view>
    </view>
  </view>
</template>

<script setup>
import { reactive } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { userApi } from "@/api/modules";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const settings = reactive({
  messageNotification: true,
  momentNotification: true,
  officialNotification: true
});

onShow(() => {
  loadSettings();
});

async function loadSettings() {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    const data = await userApi.settingDetail();
    Object.assign(settings, data || {});
  } catch (error) {
    uni.showToast({ title: error.message || "加载设置失败", icon: "none" });
  }
}

async function updateSetting(field, value) {
  if (!userStore.requireAuth()) {
    return;
  }
  settings[field] = value;
  try {
    const data = await userApi.updateSetting({ [field]: value });
    Object.assign(settings, data || {});
  } catch (error) {
    uni.showToast({ title: error.message || "保存设置失败", icon: "none" });
    await loadSettings();
  }
}

function go(url) {
  uni.navigateTo({ url });
}

function logout() {
  userStore.logout();
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

.settings-list {
  margin-bottom: 18rpx;
}

.settings-row {
  min-height: 92rpx;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.settings-row:last-child {
  border-bottom: none;
}

.settings-label {
  font-size: 28rpx;
  color: var(--wy-text);
}

.utility-arrow {
  font-size: 30rpx;
  color: #a0a0a0;
}

.setting-panel {
  margin-top: 18rpx;
}

.setting-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16rpx;
  padding: 18rpx 0;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.setting-row:last-child {
  border-bottom: none;
}

.logout-wrap {
  margin-top: 24rpx;
}

.danger-button {
  height: 76rpx;
  border-radius: 18rpx;
  background: #fff;
  border: 1rpx solid rgba(197, 77, 67, 0.15);
  color: #c54d43;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  font-weight: 700;
}
</style>
