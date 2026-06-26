<template>
  <view class="page-shell">
    <view class="hero-card">
      <text class="title-lg">隐私设置</text>
      <text class="body-sm muted">控制添加好友方式、群邀请确认，以及朋友圈和位置信息展示偏好。</text>
    </view>

    <view class="panel setting-panel">
      <view v-for="item in privacyItems" :key="item.key" class="setting-row">
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
  addByPhone: true,
  addByWeiyouNo: true,
  groupInviteConfirm: false,
  hideMyMoments: false,
  hideLocation: false
});

const privacyItems = [
  { key: "addByPhone", title: "允许通过手机号添加我", desc: "关闭后，他人无法通过手机号搜索到你" },
  { key: "addByWeiyouNo", title: "允许通过微友号添加我", desc: "关闭后，微友号搜索将不可见" },
  { key: "groupInviteConfirm", title: "群邀请需确认", desc: "开启后，被邀请进群前需手动确认" },
  { key: "hideMyMoments", title: "隐藏我的朋友圈入口", desc: "他人查看资料页时不展示朋友圈入口" },
  { key: "hideLocation", title: "隐藏我的位置信息", desc: "资料和名片中不主动展示位置信息" }
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
    uni.showToast({ title: error.message || "加载隐私设置失败", icon: "none" });
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
