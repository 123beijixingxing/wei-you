<template>
  <view class="login-page">
    <view class="mask mask-left" />
    <view class="mask mask-right" />
    <view class="login-card">
      <text class="eyebrow">WeiYou</text>
      <text class="title-xl">微友</text>
      <text class="body-md intro">像熟悉的即时通讯体验一样自然，但更适合继续深度开发。</text>

      <view class="mode-tabs">
        <view
          v-for="item in modes"
          :key="item.value"
          class="mode-tab"
          :class="mode === item.value ? 'is-active' : ''"
          @click="mode = item.value"
        >
          {{ item.label }}
        </view>
      </view>

      <view class="field">
        <text class="field__label">手机号</text>
        <input v-model="form.mobile" class="field__input" placeholder="输入手机号" />
      </view>

      <view v-if="mode === 'password' || mode === 'register'" class="field">
        <text class="field__label">密码</text>
        <input v-model="form.password" class="field__input" password placeholder="输入密码" />
      </view>

      <view v-if="mode === 'sms' || mode === 'register'" class="field">
        <text class="field__label">验证码</text>
        <view class="code-row">
          <input v-model="form.smsCode" class="field__input code-input" placeholder="输入验证码" />
          <view class="ghost-button code-button" @click="sendSmsCode">{{ smsCountdown > 0 ? `${smsCountdown}s` : '发送验证码' }}</view>
        </view>
      </view>

      <view class="actions">
        <view class="action-button" :class="submitting ? 'is-disabled' : ''" @click="submit">
          {{ submitting ? '提交中...' : submitLabel }}
        </view>
      </view>

      <text class="hint">演示账号：13800000001 / 123456。短信登录与注册可填写任意 6 位验证码完成联调。</text>
    </view>
  </view>
</template>

<script setup>
import { computed, onBeforeUnmount, reactive, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const submitting = ref(false);
const mode = ref("password");
const smsCountdown = ref(0);
let timer = null;

const modes = [
  { label: "密码登录", value: "password" },
  { label: "短信登录", value: "sms" },
  { label: "注册", value: "register" }
];

const form = reactive({
  mobile: "13800000001",
  password: "123456",
  smsCode: "123456"
});

const submitLabel = computed(() => {
  if (mode.value === "sms") {
    return "短信登录";
  }
  if (mode.value === "register") {
    return "注册并进入微友";
  }
  return "进入微友";
});

onShow(() => {
  if (userStore.isAuthenticated) {
    navigateHome();
  }
});

onBeforeUnmount(() => {
  clearTimer();
});

async function submit() {
  if (submitting.value) {
    return;
  }
  const mobile = form.mobile.trim();
  const password = form.password.trim();
  const smsCode = form.smsCode.trim();
  if (!mobile) {
    uni.showToast({ title: "请输入手机号", icon: "none" });
    return;
  }
  if ((mode.value === "password" || mode.value === "register") && !password) {
    uni.showToast({ title: "请输入密码", icon: "none" });
    return;
  }
  if ((mode.value === "sms" || mode.value === "register") && !smsCode) {
    uni.showToast({ title: "请输入验证码", icon: "none" });
    return;
  }

  submitting.value = true;
  try {
    if (mode.value === "password") {
      await userStore.login(mobile, password);
    } else if (mode.value === "sms") {
      await userStore.loginWithSms(mobile, smsCode);
    } else {
      await userStore.register(mobile, smsCode, password);
    }
    uni.showToast({ title: "登录成功", icon: "success" });
    navigateHome();
  } catch (error) {
    uni.showToast({ title: error.message || "操作失败", icon: "none" });
  } finally {
    submitting.value = false;
  }
}

async function sendSmsCode() {
  if (smsCountdown.value > 0) {
    return;
  }
  const mobile = form.mobile.trim();
  if (!mobile) {
    uni.showToast({ title: "请先输入手机号", icon: "none" });
    return;
  }
  try {
    await userStore.sendSmsCode(mobile, mode.value === "register" ? "register" : "login");
    uni.showToast({ title: "验证码已发送", icon: "success" });
    smsCountdown.value = 60;
    timer = setInterval(() => {
      smsCountdown.value -= 1;
      if (smsCountdown.value <= 0) {
        clearTimer();
      }
    }, 1000);
  } catch (error) {
    uni.showToast({ title: error.message || "发送失败", icon: "none" });
  }
}

function clearTimer() {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
  if (smsCountdown.value < 0) {
    smsCountdown.value = 0;
  }
}

function navigateHome() {
  if (typeof window !== "undefined" && window.location) {
    window.location.href = `${window.location.origin}/#/pages/chat/index`;
    return;
  }
  uni.reLaunch({
    url: "/pages/chat/index"
  });
}
</script>

<style scoped lang="css">
.login-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48rpx 24rpx;
  overflow: hidden;
  background: linear-gradient(180deg, #f2efe8 0%, #f7f7f7 46%, #eef3f0 100%);
}

.mask {
  position: absolute;
  width: 300rpx;
  height: 300rpx;
  border-radius: 50%;
  filter: blur(14rpx);
}

.mask-left {
  top: -40rpx;
  left: -70rpx;
  background: rgba(203, 124, 70, 0.20);
}

.mask-right {
  right: -70rpx;
  bottom: 140rpx;
  background: rgba(13, 92, 82, 0.16);
}

.login-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 680rpx;
  min-width: 0;
  padding: 34rpx 24rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.94);
  border: 1rpx solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 12rpx 30rpx rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.mode-tabs,
.field,
.code-row,
.actions,
.hint {
  width: 100%;
  min-width: 0;
}

.eyebrow {
  display: inline-block;
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(13, 92, 82, 0.08);
  color: var(--wy-primary);
  font-size: 22rpx;
  font-weight: 700;
}

.intro {
  display: block;
  margin-top: 18rpx;
  color: var(--wy-subtext);
}

.mode-tabs {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180rpx, 1fr));
  gap: 10rpx;
  margin-top: 24rpx;
}

.mode-tab {
  cursor: pointer;
  min-height: 68rpx;
  padding: 0 12rpx;
  border-radius: 16rpx;
  background: #f6f6f6;
  border: 1rpx solid rgba(0, 0, 0, 0.05);
  color: var(--wy-subtext);
  font-size: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  min-width: 0;
  line-height: 1.3;
  word-break: keep-all;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mode-tab.is-active {
  background: rgba(31, 138, 112, 0.12);
  border-color: rgba(31, 138, 112, 0.10);
  color: var(--wy-primary);
  font-weight: 700;
}

.field {
  margin-top: 22rpx;
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
  min-width: 0;
  box-sizing: border-box;
}

.code-row {
  display: flex;
  gap: 12rpx;
  align-items: stretch;
  flex-wrap: wrap;
}

.code-input {
  flex: 1 1 0;
  min-width: 0;
}

.code-button {
  min-width: 168rpx;
  max-width: 220rpx;
  flex: 0 1 188rpx;
  justify-content: center;
  white-space: nowrap;
  box-sizing: border-box;
}

.actions {
  margin-top: 28rpx;
}

.actions .action-button {
  width: 100%;
  max-width: 100%;
  min-width: 0;
  padding: 0 18rpx;
  box-sizing: border-box;
  justify-content: center;
  cursor: pointer;
}

.hint {
  display: block;
  margin-top: 16rpx;
  color: var(--wy-subtext);
  font-size: 22rpx;
  line-height: 1.7;
  word-break: break-word;
}

.is-disabled {
  opacity: 0.7;
}

@media (min-width: 900px) {
  .login-page {
    padding: 28px 20px;
  }

  .login-card {
    max-width: 520px;
    padding: 28px;
    border-radius: 24px;
    box-shadow: 0 20px 44px rgba(0, 0, 0, 0.06);
  }
}

@media (max-width: 480px) {
  .login-page {
    padding: 24rpx 18rpx;
  }

  .login-card {
    padding: 24rpx 16rpx;
    border-radius: 20rpx;
  }

  .mode-tabs {
    grid-template-columns: 1fr;
  }

  .code-row {
    flex-direction: column;
  }

  .code-button,
  .actions .action-button {
    width: 100%;
    max-width: none;
    flex: none;
  }
}

@media (min-width: 481px) and (max-width: 760px) {
  .login-card {
    padding: 28rpx 18rpx;
  }

  .mode-tab {
    font-size: 22rpx;
  }

  .code-button {
    min-width: 148rpx;
    flex-basis: 164rpx;
  }
}

@media (hover: hover) {
  .mode-tab:hover {
    background: rgba(31, 138, 112, 0.10);
  }

  .actions .action-button:hover {
    background: #18765f;
  }
}
</style>
