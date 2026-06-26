<template>
  <view class="page-shell">
    <view class="wechat-header simple">
      <text class="wechat-title">红包操作</text>
      <text class="body-sm muted header-subtitle">支持发红包与领取红包，方便直接联调钱包链路。</text>
    </view>

    <view class="panel form-panel">
      <text class="title-md">发红包</text>
      <view class="field">
        <text class="field__label">群会话 ID</text>
        <input v-model="sendForm.groupId" class="field__input" placeholder="例如 90002" />
      </view>
      <view class="field">
        <text class="field__label">金额（元）</text>
        <input v-model="sendForm.amountYuan" class="field__input" type="digit" placeholder="例如 3.00" />
      </view>
      <view class="field">
        <text class="field__label">个数</text>
        <input v-model="sendForm.count" class="field__input" type="number" placeholder="例如 3" />
      </view>
      <view class="field">
        <text class="field__label">祝福语</text>
        <input v-model="sendForm.greeting" class="field__input" placeholder="填写红包文案" />
      </view>
      <view class="action-button" :class="sending ? 'is-disabled' : ''" @click="createRedPacket">
        {{ sending ? "发送中..." : "发送红包" }}
      </view>
    </view>

    <view class="panel form-panel">
      <text class="title-md">领取红包</text>
      <view class="field">
        <text class="field__label">红包 ID</text>
        <input v-model="openForm.redPacketId" class="field__input" placeholder="例如 81001" />
      </view>
      <view class="ghost-button" :class="opening ? 'is-disabled' : ''" @click="openRedPacket">
        {{ opening ? "领取中..." : "领取红包" }}
      </view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from "vue";
import { walletApi } from "@/api/modules";
import { useAppStore } from "@/stores/app";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const appStore = useAppStore();
const sending = ref(false);
const opening = ref(false);
const sendForm = reactive({
  groupId: "90002",
  amountYuan: "3.00",
  count: "3",
  greeting: "大家辛苦了"
});
const openForm = reactive({
  redPacketId: "81001"
});

async function createRedPacket() {
  if (!userStore.requireAuth() || sending.value) {
    return;
  }
  const amountFen = Math.round(Number(sendForm.amountYuan) * 100);
  const count = Number(sendForm.count);
  const groupId = Number(sendForm.groupId);
  if (!amountFen || !count || !groupId) {
    uni.showToast({
      title: "请填写完整红包信息",
      icon: "none"
    });
    return;
  }
  sending.value = true;
  try {
    const result = await walletApi.createRedPacket({
      amountFen,
      count,
      type: 2,
      greeting: sendForm.greeting,
      groupId
    });
    await appStore.fetchWalletData();
    openForm.redPacketId = String(result.redPacketId || openForm.redPacketId);
    uni.showToast({
      title: "红包已发送",
      icon: "success"
    });
  } catch (error) {
    uni.showToast({
      title: error.message || "红包发送失败",
      icon: "none"
    });
  } finally {
    sending.value = false;
  }
}

async function openRedPacket() {
  if (!userStore.requireAuth() || opening.value) {
    return;
  }
  const redPacketId = Number(openForm.redPacketId);
  if (!redPacketId) {
    uni.showToast({
      title: "请输入红包 ID",
      icon: "none"
    });
    return;
  }
  opening.value = true;
  try {
    const result = await walletApi.openRedPacket({ redPacketId });
    await appStore.fetchWalletData();
    uni.showToast({
      title: `已领取 ${(Number(result.receiveAmountFen || 0) / 100).toFixed(2)} 元`,
      icon: "success"
    });
  } catch (error) {
    uni.showToast({
      title: error.message || "领取失败",
      icon: "none"
    });
  } finally {
    opening.value = false;
  }
}
</script>

<style scoped lang="css">
.form-panel {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-top: 10rpx;
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

.is-disabled {
  opacity: 0.7;
}
</style>
