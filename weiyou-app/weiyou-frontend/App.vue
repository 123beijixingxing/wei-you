<template>
  <!-- #ifdef H5 -->
  <router-view />
  <!-- #endif -->
</template>

<script setup>
import { onLaunch, onShow } from "@dcloudio/uni-app";
import { useAppStore } from "@/stores/app";
import { useChatStore } from "@/stores/chat";
import { useUserStore } from "@/stores/user";

const APP_VERSION = "0.1.0";

onLaunch(async () => {
  console.log("WeiYou app launched");
  const appStore = useAppStore();
  const userStore = useUserStore();
  try {
    await appStore.fetchBootstrapConfig({ version: APP_VERSION });
    await appStore.presentBootstrapPrompt(APP_VERSION);
  } catch (error) {
    console.warn("Failed to fetch bootstrap config", error);
  }
  try {
    await userStore.restore();
  } catch (error) {
    console.warn("Failed to restore user session", error);
  }
});

onShow(() => {
  const userStore = useUserStore();
  if (!userStore.isAuthenticated) {
    return;
  }
  useChatStore().ensureSocketConnected().catch((error) => {
    console.warn("Failed to reconnect socket on app show", error);
  });
});
</script>

<style>
:root,
page {
  --wy-primary: #1f8a70;
  --wy-primary-soft: #eef8f4;
  --wy-accent: #d18a4a;
  --wy-bg: #ededed;
  --wy-surface: #ffffff;
  --wy-text: #111111;
  --wy-subtext: #737373;
  --wy-line: rgba(17, 17, 17, 0.08);
}

.title-xl {
  font-size: 52rpx;
  font-weight: 800;
  line-height: 1.2;
}

.title-lg {
  font-size: 36rpx;
  font-weight: 700;
  line-height: 1.3;
}

.title-md {
  font-size: 30rpx;
  font-weight: 700;
}

.body-md {
  font-size: 28rpx;
  line-height: 1.55;
}

.body-sm {
  font-size: 24rpx;
  line-height: 1.6;
}

page {
  background: var(--wy-bg);
  color: var(--wy-text);
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
}

body {
  background: var(--wy-bg);
}

view,
text,
button,
input,
textarea {
  box-sizing: border-box;
}

.page-shell {
  min-height: 100vh;
  width: 100%;
  max-width: 980px;
  margin: 0 auto;
  padding: 24rpx 18rpx 44rpx;
  background: var(--wy-bg);
}

.hero-card,
.panel {
  border-radius: 22rpx;
  background: var(--wy-surface);
  border: 1rpx solid var(--wy-line);
  box-shadow: 0 6rpx 18rpx rgba(0, 0, 0, 0.03);
}

.hero-card {
  padding: 26rpx 24rpx;
}

.panel {
  padding: 22rpx 24rpx;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 24rpx 0 14rpx;
  gap: 16rpx;
  font-size: 28rpx;
  font-weight: 700;
}

.section-subtitle {
  color: var(--wy-subtext);
  font-size: 22rpx;
}

.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 34rpx;
  height: 34rpx;
  padding: 0 10rpx;
  border-radius: 999rpx;
  background: #d54b46;
  color: #fff;
  font-size: 20rpx;
  font-weight: 700;
}

.input-chip {
  display: flex;
  align-items: center;
  gap: 12rpx;
  height: 76rpx;
  padding: 0 22rpx;
  border-radius: 18rpx;
  background: #f6f6f6;
  border: 1rpx solid rgba(0, 0, 0, 0.04);
}

.action-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 72rpx;
  padding: 0 28rpx;
  border-radius: 18rpx;
  background: var(--wy-primary);
  color: #fff;
  font-size: 26rpx;
  font-weight: 700;
}

.ghost-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 72rpx;
  padding: 0 24rpx;
  border-radius: 18rpx;
  background: #f6f6f6;
  border: 1rpx solid rgba(0, 0, 0, 0.05);
  color: var(--wy-primary);
  font-size: 24rpx;
  font-weight: 600;
}

.panel-lite {
  border-radius: 18rpx;
  background: #fff;
  border: 1rpx solid rgba(0, 0, 0, 0.05);
}

.simple-list {
  border-radius: 18rpx;
  background: #fff;
  border: 1rpx solid rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.simple-row {
  min-height: 88rpx;
  padding: 16rpx 18rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.simple-row:last-child {
  border-bottom: none;
}

.simple-label {
  flex: 1;
  font-size: 28rpx;
  color: var(--wy-text);
}

.utility-arrow {
  font-size: 30rpx;
  color: #a0a0a0;
}

.avatar {
  width: 84rpx;
  height: 84rpx;
  border-radius: 20rpx;
  object-fit: cover;
}

.avatar-lg {
  width: 112rpx;
  height: 112rpx;
  border-radius: 24rpx;
  object-fit: cover;
}

.muted {
  color: var(--wy-subtext);
}

.grid-2 {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 18rpx;
}

.grid-3 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18rpx;
}

@media (min-width: 900px) {
  .page-shell {
    padding: 22px 18px 56px;
  }

  .grid-2 {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .grid-3 {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (prefers-reduced-motion: no-preference) {
  .hero-card,
  .panel,
  .action-button,
  .ghost-button {
    transition: background-color 160ms ease, border-color 160ms ease, box-shadow 160ms ease;
  }
}
</style>
