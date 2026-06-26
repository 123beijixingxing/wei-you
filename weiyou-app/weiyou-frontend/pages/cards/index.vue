<template>
  <view class="page-shell">
    <view class="wechat-header simple">
      <text class="wechat-title">卡包</text>
    </view>

    <view class="input-chip search-chip">
      <text>筛选</text>
      <picker :range="cardTypeOptions" range-key="label" @change="changeCardType">
        <view class="picker-text">{{ currentCardTypeLabel }}</view>
      </picker>
    </view>

    <view v-for="item in cards" :key="item.cardId" class="panel-lite card-item">
      <view class="card-header">
        <text class="title-md">{{ item.title }}</text>
        <text class="body-sm muted">{{ item.status }}</text>
      </view>
      <text class="body-sm muted">{{ item.provider }}</text>
      <text class="body-sm muted">有效期：{{ item.expireText }}</text>
      <view class="card-actions">
        <view class="ghost-button" :class="item.status === '已使用' ? 'is-disabled' : ''" @click="useCard(item)">
          {{ item.status === '已使用' ? '已核销' : '立即使用' }}
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { cardApi } from "@/api/modules";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const cards = ref([]);
const cardType = ref("");
const cardTypeOptions = [
  { label: "全部", value: "" },
  { label: "优惠券", value: "coupon" },
  { label: "会员卡", value: "member" },
  { label: "交通卡", value: "transport" }
];

const currentCardTypeLabel = computed(() => {
  return cardTypeOptions.find((item) => item.value === cardType.value)?.label || "全部";
});

onShow(() => {
  loadCards();
});

async function loadCards() {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    cards.value = await cardApi.list(cardType.value);
  } catch (error) {
    uni.showToast({ title: error.message || "加载卡包失败", icon: "none" });
  }
}

function changeCardType(event) {
  const selected = cardTypeOptions[event.detail.value];
  cardType.value = selected?.value || "";
  loadCards();
}

async function useCard(item) {
  if (item.status === "已使用") {
    return;
  }
  try {
    const updated = await cardApi.use({ cardId: item.cardId });
    cards.value = cards.value.map((current) => current.cardId === item.cardId ? updated : current);
    uni.showToast({ title: "卡券已核销", icon: "success" });
  } catch (error) {
    uni.showToast({ title: error.message || "操作失败", icon: "none" });
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

.search-chip {
  margin-top: 18rpx;
}

.picker-text {
  font-size: 26rpx;
  color: var(--wy-primary);
  font-weight: 700;
}

.panel-lite {
  border-radius: 18rpx;
  background: #fff;
  border: 1rpx solid rgba(0, 0, 0, 0.05);
}

.card-item {
  margin-top: 10rpx;
  padding: 18rpx 20rpx;
}

.card-header {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 12rpx;
}

.card-actions {
  margin-top: 16rpx;
}

.is-disabled {
  opacity: 0.7;
}
</style>
