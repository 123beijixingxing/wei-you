<template>
  <view class="page-shell">
    <view class="wechat-header simple">
      <text class="wechat-title">好友转账</text>
      <text class="body-sm muted header-subtitle">收款人 {{ form.targetUserId }} · 金额 ¥ {{ amountPreview }}</text>
    </view>

    <view class="panel form-panel">
      <view class="field">
        <text class="field__label">收款用户 ID</text>
        <input v-model="form.targetUserId" class="field__input" placeholder="例如 10002" />
      </view>
      <view class="field">
        <text class="field__label">金额（元）</text>
        <input v-model="form.amountYuan" class="field__input" type="digit" placeholder="例如 1.00" />
        <view class="amount-presets">
          <view v-for="item in amountPresets" :key="item" class="ghost-button preset-chip" @click="form.amountYuan = item">{{ item }}</view>
        </view>
      </view>
      <view class="field">
        <text class="field__label">备注</text>
        <input v-model="form.remark" class="field__input" placeholder="填写转账说明" />
      </view>
      <view class="action-button" :class="submitting ? 'is-disabled' : ''" @click="submitTransfer">
        {{ submitting ? "转账中..." : "确认转账" }}
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { walletApi } from "@/api/modules";
import { useAppStore } from "@/stores/app";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const appStore = useAppStore();
const submitting = ref(false);
const form = reactive({
  targetUserId: "10002",
  amountYuan: "1.00",
  remark: "演示转账"
});
const amountPresets = ["1.00", "8.88", "18.80", "66.00"];
const amountPreview = computed(() => {
  const value = Number(form.amountYuan || 0);
  return Number.isFinite(value) ? value.toFixed(2) : "0.00";
});

onLoad((query) => {
  if (query?.targetUserId) {
    form.targetUserId = String(query.targetUserId);
  }
});

async function submitTransfer() {
  if (!userStore.requireAuth()) {
    return;
  }
  if (submitting.value) {
    return;
  }
  const targetUserId = Number(form.targetUserId);
  const amountFen = Math.round(Number(form.amountYuan) * 100);
  if (!targetUserId || !amountFen) {
    uni.showToast({
      title: "请填写正确金额和收款人",
      icon: "none"
    });
    return;
  }
  submitting.value = true;
  try {
    const result = await walletApi.transfer({
      targetUserId,
      amountFen,
      remark: form.remark
    });
    await appStore.fetchWalletData();
    uni.showToast({
      title: `转账成功 ${result.transactionNo}`,
      icon: "success"
    });
  } catch (error) {
    uni.showToast({
      title: error.message || "转账失败",
      icon: "none"
    });
  } finally {
    submitting.value = false;
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

.header-subtitle {
  display: block;
  margin-top: 8rpx;
}

.form-panel {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
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

.amount-presets {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 14rpx;
}

.preset-chip,
.action-button {
  cursor: pointer;
}

.action-button {
  width: 100%;
  justify-content: center;
}

.is-disabled {
  opacity: 0.7;
}

</style>
