<template>
  <view class="page-shell">
    <view class="wallet-hero hero-card">
      <text class="body-sm muted">零钱</text>
      <text class="balance">¥ {{ wallet.balance }}</text>
      <view class="wallet-summary">
        <text class="body-sm muted">本月支出 ¥ {{ wallet.monthlySpend }}</text>
        <text class="body-sm muted">本月收入 ¥ {{ wallet.monthlyIncome }}</text>
      </view>
    </view>

    <view class="panel-lite wallet-list">
      <view class="wallet-row" @click="go('/pages/wallet/transfer')">
        <text class="wallet-label">好友转账</text>
        <text class="utility-arrow">›</text>
      </view>
      <view class="wallet-row" @click="go('/pages/wallet/red-packet')">
        <text class="wallet-label">红包操作</text>
        <text class="utility-arrow">›</text>
      </view>
      <view class="wallet-row" @click="go('/pages/wallet/bills')">
        <text class="wallet-label">全部账单</text>
        <text class="utility-arrow">›</text>
      </view>
      <view class="wallet-row" @click="go('/pages/cards/index')">
        <text class="wallet-label">卡包</text>
        <text class="utility-arrow">›</text>
      </view>
    </view>

    <view class="section-title">
      <text>最近账单</text>
      <text class="section-subtitle">{{ wallet.bills.length }} 条</text>
    </view>
    <view v-if="wallet.bills.length" class="bill-list">
      <view v-for="bill in wallet.bills" :key="`${bill.title}-${bill.time}`" class="panel-lite bill-item" @click="go('/pages/wallet/bills')">
        <view>
          <text class="title-md">{{ bill.title }}</text>
          <text class="body-sm muted bill-time">{{ bill.time }} · {{ bill.status }}</text>
        </view>
        <text class="bill-amount">{{ bill.amount }}</text>
      </view>
    </view>
    <view v-else class="panel empty-panel">
      <text class="title-md">还没有账单记录</text>
      <text class="body-sm muted empty-copy">钱包能力已经联通，可以先发起转账、红包或打开卡包生成第一笔交易。</text>
      <view class="empty-actions">
        <view class="action-button" @click="go('/pages/wallet/transfer')">发起转账</view>
        <view class="ghost-button" @click="go('/pages/cards/index')">查看卡包</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onShow } from "@dcloudio/uni-app";
import { storeToRefs } from "pinia";
import { useAppStore } from "@/stores/app";
import { useUserStore } from "@/stores/user";

const appStore = useAppStore();
const userStore = useUserStore();
const { wallet } = storeToRefs(appStore);

onShow(async () => {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    await appStore.fetchWalletData();
  } catch (error) {
    uni.showToast({
      title: error.message || "加载钱包失败",
      icon: "none"
    });
  }
});

function go(url) {
  uni.navigateTo({ url });
}

</script>

<style scoped lang="css">
.wallet-hero {
  background: linear-gradient(180deg, #ffffff 0%, #f9fcfb 100%);
  color: var(--wy-text);
}

.wallet-list,
.empty-actions .ghost-button,
.empty-actions .action-button,
.bill-item {
  cursor: pointer;
}

.balance {
  display: block;
  margin-top: 10rpx;
  font-size: 56rpx;
  font-weight: 800;
}

.wallet-summary {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  margin-top: 14rpx;
}

.panel-lite {
  border-radius: 18rpx;
  background: #fff;
  border: 1rpx solid rgba(0, 0, 0, 0.05);
}

.wallet-list {
  margin-top: 18rpx;
  overflow: hidden;
}

.wallet-row {
  min-height: 92rpx;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.wallet-row:last-child {
  border-bottom: none;
}

.wallet-label {
  font-size: 28rpx;
  color: var(--wy-text);
}

.utility-arrow {
  font-size: 30rpx;
  color: #a0a0a0;
}

.bill-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.bill-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18rpx 20rpx;
}

.bill-time {
  display: block;
  margin-top: 10rpx;
}

.bill-amount {
  font-size: 30rpx;
  font-weight: 800;
}

.empty-panel {
  margin-top: 18rpx;
}

.empty-copy {
  display: block;
  margin-top: 12rpx;
}

.empty-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
  margin-top: 24rpx;
}

@media (max-width: 899px) {
  .hero-stats {
    grid-template-columns: 1fr;
  }
}
</style>
