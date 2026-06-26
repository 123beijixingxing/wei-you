<template>
  <view class="page-shell">
    <view class="wechat-header simple">
      <text class="wechat-title">扫一扫</text>
    </view>

    <view class="panel-lite scan-panel">
      <view class="field">
        <text class="field__label">扫码内容</text>
        <input v-model="scanCode" class="field__input" placeholder="例如 user:10002 / group:90002 / pay:10002" />
      </view>
      <view class="actions-row">
        <view class="ghost-button" @click="fillDemoCode">填充示例</view>
        <view class="ghost-button" @click="scanByCamera">摄像头扫码</view>
        <view class="action-button" @click="resolveCode">解析二维码</view>
      </view>
    </view>

    <view v-if="scanResult" class="panel-lite result-panel">
      <view class="result-row">
        <text class="result-label">类型</text>
        <text class="result-value">{{ scanResult.scanType }}</text>
      </view>
      <view class="result-row">
        <text class="result-label">标题</text>
        <text class="result-value">{{ scanResult.title }}</text>
      </view>
      <view class="result-row">
        <text class="result-label">动作</text>
        <text class="result-value">{{ scanResult.actionType }}</text>
      </view>
      <view class="actions-row result-actions">
        <view class="ghost-button" @click="copyScanCode">复制内容</view>
        <view class="action-button" @click="openResultRoute">打开结果页</view>
      </view>
    </view>

    <view v-if="history.length" class="panel-lite history-panel">
      <view class="panel-head">
        <text class="title-md">扫码记录</text>
        <text class="body-sm muted">{{ history.length }} 条</text>
      </view>
      <view v-for="item in history" :key="item.id" class="history-item" @click="reuseHistory(item)">
        <view class="history-main">
          <text class="title-md">{{ item.title }}</text>
          <text class="body-sm muted">{{ item.scanType }} · {{ item.time }}</text>
          <text class="body-sm muted history-code">{{ item.scanCode }}</text>
        </view>
        <text class="history-action">复用</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { scanApi } from "@/api/modules";
import { useUserStore } from "@/stores/user";

const SCAN_HISTORY_KEY = "weiyou_scan_history";

const userStore = useUserStore();
const scanCode = ref("");
const scanResult = ref(null);
const history = ref([]);

onShow(() => {
  history.value = readHistory();
});

function readHistory() {
  const stored = uni.getStorageSync(SCAN_HISTORY_KEY);
  if (!Array.isArray(stored)) {
    return [];
  }
  return stored;
}

function writeHistory(list) {
  uni.setStorageSync(SCAN_HISTORY_KEY, list);
}

function appendHistory(result, rawCode) {
  const next = [
    {
      id: Date.now(),
      scanCode: rawCode,
      scanType: result.scanType,
      title: result.title,
      actionType: result.actionType,
      payload: result.payload || {},
      time: new Date().toLocaleString("zh-CN")
    },
    ...history.value.filter((item) => item.scanCode !== rawCode)
  ].slice(0, 20);
  history.value = next;
  writeHistory(next);
}

function fillDemoCode() {
  scanCode.value = "user:10002";
}

async function resolveCode() {
  await resolveCodeValue(scanCode.value.trim(), "manual");
}

async function resolveCodeValue(value, scene) {
  if (!value || !userStore.requireAuth()) {
    uni.showToast({
      title: "请输入扫码内容",
      icon: "none"
    });
    return;
  }
  try {
    scanResult.value = await scanApi.resolve({
      scanCode: value,
      scene
    });
    appendHistory(scanResult.value, value);
  } catch (error) {
    uni.showToast({
      title: error.message || "解析失败",
      icon: "none"
    });
  }
}

function scanByCamera() {
  if (typeof uni.scanCode !== "function") {
    uni.showToast({
      title: "当前环境暂不支持摄像头扫码",
      icon: "none"
    });
    return;
  }
  uni.scanCode({
    onlyFromCamera: true,
    success: async (result) => {
      const code = result?.result || "";
      scanCode.value = code;
      await resolveCodeValue(code, "camera");
    },
    fail: () => {
      uni.showToast({
        title: "扫码已取消",
        icon: "none"
      });
    }
  });
}

function openResultRoute() {
  const payload = scanResult.value?.payload || {};
  const route = payload.routePath;
  if (route) {
    uni.navigateTo({ url: route });
    return;
  }
  if (scanResult.value?.actionType === "show_link" && payload.url) {
    uni.setClipboardData({ data: payload.url });
    uni.showToast({ title: "链接已复制", icon: "none" });
    return;
  }
  if (scanResult.value?.actionType === "show_raw" && payload.content) {
    uni.showModal({
      title: "扫码原始内容",
      content: payload.content,
      showCancel: false,
      confirmText: "知道了"
    });
  }
}

function copyScanCode() {
  const value = scanCode.value.trim();
  if (!value) {
    return;
  }
  uni.setClipboardData({ data: value });
}

function reuseHistory(item) {
  scanCode.value = item.scanCode;
  scanResult.value = {
    scanType: item.scanType,
    title: item.title,
    actionType: item.actionType,
    payload: item.payload || {}
  };
}
</script>

<style scoped lang="css">
.scan-panel,
.result-panel,
.history-panel {
  margin-top: 14rpx;
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
  flex-wrap: wrap;
  margin-top: 22rpx;
}

.result-actions {
  margin-top: 18rpx;
}

.detail-line {
  display: block;
  margin-top: 12rpx;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12rpx;
}


.result-row {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
  padding: 6rpx 0;
}

.result-label {
  font-size: 24rpx;
  color: var(--wy-subtext);
}

.result-value {
  flex: 1;
  text-align: right;
  font-size: 24rpx;
  color: var(--wy-text);
}

.history-item {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
  padding: 18rpx 0;
  border-top: 1rpx solid rgba(0, 0, 0, 0.05);
}

.history-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.history-code {
  word-break: break-all;
}

.history-action {
  color: var(--wy-primary);
  font-size: 24rpx;
  font-weight: 700;
}
</style>
