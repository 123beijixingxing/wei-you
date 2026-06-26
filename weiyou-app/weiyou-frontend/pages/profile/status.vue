<template>
  <view class="page-shell">
    <view class="wechat-header simple">
      <text class="wechat-title">状态设置</text>
      <text class="body-sm muted header-subtitle">设置当前状态文案并同步到真实后端。</text>
    </view>

    <view class="panel form-panel">
      <view class="tag-row">
        <view
          v-for="item in presets"
          :key="item.code"
          class="tag-chip"
          :class="form.statusCode === item.code ? 'is-selected' : ''"
          @click="selectPreset(item)"
        >
          {{ item.label }}
        </view>
      </view>

      <view class="field">
        <text class="field__label">状态文案</text>
        <input v-model="form.statusText" class="field__input" placeholder="输入自定义状态文案" />
      </view>

      <view class="actions-row">
        <view class="ghost-button" @click="resetForm">重置</view>
        <view class="action-button" :class="saving ? 'is-disabled' : ''" @click="saveStatus">{{ saving ? '保存中...' : '保存状态' }}</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const saving = ref(false);
const presets = [
  { code: "online", label: "在线", text: "在线" },
  { code: "busy", label: "忙碌中", text: "忙碌中" },
  { code: "meeting", label: "开会中", text: "开会中" },
  { code: "travel", label: "在路上", text: "在路上" }
];
const form = reactive({
  statusCode: "online",
  statusText: "在线"
});

onShow(() => {
  resetForm();
});

function selectPreset(item) {
  form.statusCode = item.code;
  form.statusText = item.text;
}

function resetForm() {
  form.statusCode = "online";
  form.statusText = userStore.profile.signature ? userStore.profile.signature : "在线";
}

async function saveStatus() {
  if (!userStore.requireAuth() || saving.value) {
    return;
  }
  saving.value = true;
  try {
    await userStore.updateStatus({
      statusCode: form.statusCode,
      statusText: form.statusText.trim() || form.statusCode,
      expireAt: ""
    });
    await userStore.fetchProfile();
    uni.showToast({ title: "状态已更新", icon: "success" });
  } catch (error) {
    uni.showToast({ title: error.message || "保存失败", icon: "none" });
  } finally {
    saving.value = false;
  }
}
</script>

<style scoped lang="css">
.form-panel {
  margin-top: 8rpx;
}

.wechat-header.simple {
  padding: 18rpx 6rpx 12rpx;
}

.wechat-title {
  font-size: 34rpx;
  font-weight: 700;
}

.header-subtitle {
  display: block;
  margin-top: 8rpx;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.tag-chip {
  padding: 12rpx 20rpx;
  border-radius: 16rpx;
  background: #f6f6f6;
  border: 1rpx solid rgba(0, 0, 0, 0.05);
  color: var(--wy-primary);
  font-size: 24rpx;
}

.is-selected {
  background: rgba(13, 92, 82, 0.16);
}

.field {
  margin-top: 24rpx;
}

.field__label {
  display: block;
  margin-bottom: 12rpx;
  font-size: 24rpx;
  color: var(--wy-subtext);
}

.field__input {
  width: 100%;
  height: 92rpx;
  padding: 0 24rpx;
  border-radius: 18rpx;
  background: rgba(255, 255, 255, 0.96);
  border: 1rpx solid rgba(0, 0, 0, 0.05);
}

.actions-row {
  display: flex;
  gap: 16rpx;
  margin-top: 22rpx;
}

.is-disabled {
  opacity: 0.7;
}
</style>
