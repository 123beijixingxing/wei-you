<template>
  <view class="page-shell">
    <view class="hero-card">
      <text class="title-lg">账单明细</text>
      <text class="body-sm muted">支持按账单类型和日期范围筛选真实账单记录。</text>
      <view class="hero-stats">
        <view class="stat-card">
          <text class="stat-value">{{ bills.length }}</text>
          <text class="stat-label">当前结果</text>
        </view>
        <view class="stat-card">
          <text class="stat-value income">{{ incomeTotal }}</text>
          <text class="stat-label">收入合计</text>
        </view>
        <view class="stat-card">
          <text class="stat-value expense">{{ expenseTotal }}</text>
          <text class="stat-label">支出合计</text>
        </view>
      </view>
    </view>

    <view class="panel filter-panel">
      <view class="filter-row">
        <text class="body-md">类型</text>
        <picker :range="typeOptions" range-key="label" @change="changeType">
          <view class="picker-chip">{{ currentTypeLabel }}</view>
        </picker>
      </view>
      <view class="filter-row">
        <text class="body-md">开始日期</text>
        <picker mode="date" :value="filters.startDate" @change="changeStartDate">
          <view class="picker-chip">{{ filters.startDate || '不限' }}</view>
        </picker>
      </view>
      <view class="filter-row">
        <text class="body-md">结束日期</text>
        <picker mode="date" :value="filters.endDate" @change="changeEndDate">
          <view class="picker-chip">{{ filters.endDate || '不限' }}</view>
        </picker>
      </view>
      <view class="filter-actions">
        <view class="ghost-button mini-btn" @click="resetFilters">重置</view>
        <view class="action-button mini-btn" :class="loading ? 'is-disabled' : ''" @click="loadBills">查询</view>
      </view>
    </view>

    <view class="section-title">
      <text>账单列表</text>
      <text class="section-subtitle">{{ bills.length }} 条</text>
    </view>

    <view v-if="bills.length" v-for="bill in bills" :key="bill.billId || `${bill.title}-${bill.time}`" class="panel bill-card">
      <view>
        <text class="title-md">{{ bill.title || bill.billTitle }}</text>
        <text class="body-sm muted bill-line">{{ bill.time || bill.billTime }} · {{ bill.status || bill.billType }}</text>
      </view>
      <text class="bill-amount" :class="String(bill.amount || '').startsWith('+') ? 'income' : 'expense'">{{ bill.amount }}</text>
    </view>

    <view v-else class="panel empty-panel">
      <text class="title-md">当前筛选下没有账单</text>
      <text class="body-sm muted empty-copy">可以重置筛选，或先去转账、红包与钱包页生成新的交易记录。</text>
      <view class="empty-actions">
        <view class="ghost-button mini-btn" @click="resetFilters">重置筛选</view>
        <view class="action-button mini-btn" @click="goTransfer">去转账</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { walletApi } from "@/api/modules";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const loading = ref(false);
const bills = ref([]);
const filters = reactive({
  type: "",
  startDate: "",
  endDate: ""
});

const typeOptions = [
  { label: "全部", value: "" },
  { label: "转账", value: "transfer" },
  { label: "收入", value: "income" },
  { label: "红包", value: "red_packet" }
];

const currentTypeLabel = computed(() => typeOptions.find((item) => item.value === filters.type)?.label || "全部");
const incomeTotal = computed(() => formatAmount(bills.value.filter((item) => String(item.amount || "").startsWith("+")).reduce((sum, item) => sum + Math.abs(Number(item.amount || 0)), 0)));
const expenseTotal = computed(() => formatAmount(bills.value.filter((item) => String(item.amount || "").startsWith("-")).reduce((sum, item) => sum + Math.abs(Number(item.amount || 0)), 0)));

onShow(() => {
  loadBills();
});

function mapBill(item) {
  const amountFen = Number(item.amountFen || 0) / 100;
  return {
    billId: item.billId,
    title: item.billTitle,
    billType: item.billType,
    time: item.billTime,
    status: item.incomeExpenseType === 1 ? "已入账" : "支付成功",
    amount: `${item.incomeExpenseType === 1 ? '+' : '-'}${amountFen.toFixed(2)}`
  };
}

function formatAmount(value) {
  return `${Number(value || 0).toFixed(2)}`;
}

async function loadBills() {
  if (!userStore.requireAuth() || loading.value) {
    return;
  }
  loading.value = true;
  try {
    const page = await walletApi.bills({
      pageNo: 1,
      type: filters.type,
      startDate: filters.startDate,
      endDate: filters.endDate
    });
    bills.value = (page?.list || []).map(mapBill);
  } catch (error) {
    uni.showToast({ title: error.message || "加载账单失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

function changeType(event) {
  filters.type = typeOptions[event.detail.value]?.value || "";
}

function changeStartDate(event) {
  filters.startDate = event.detail.value;
}

function changeEndDate(event) {
  filters.endDate = event.detail.value;
}

function resetFilters() {
  filters.type = "";
  filters.startDate = "";
  filters.endDate = "";
  loadBills();
}

function goTransfer() {
  uni.navigateTo({ url: "/pages/wallet/transfer" });
}
</script>

<style scoped lang="css">
.hero-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16rpx;
  margin-top: 28rpx;
}

.stat-card {
  padding: 20rpx;
  border-radius: 22rpx;
  background: rgba(13, 92, 82, 0.06);
}

.stat-value {
  display: block;
  font-size: 30rpx;
  font-weight: 800;
}

.stat-label {
  display: block;
  margin-top: 10rpx;
  color: var(--wy-subtext);
  font-size: 22rpx;
}

.filter-panel {
  margin-top: 22rpx;
}

.filter-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 16rpx;
}

.picker-chip {
  padding: 12rpx 18rpx;
  border-radius: 16rpx;
  background: #f6f6f6;
  color: var(--wy-primary);
  font-size: 24rpx;
  font-weight: 700;
}

.filter-actions {
  display: flex;
  gap: 14rpx;
  justify-content: flex-end;
}

.filter-actions .ghost-button,
.filter-actions .action-button {
  cursor: pointer;
}

.mini-btn {
  height: 68rpx;
  padding: 0 24rpx;
}

.bill-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 14rpx;
}

.bill-line {
  display: block;
  margin-top: 10rpx;
}

.bill-amount {
  font-size: 30rpx;
  font-weight: 800;
}

.income {
  color: #178a58;
}

.expense {
  color: #c54d43;
}

.is-disabled {
  opacity: 0.7;
}

.empty-panel {
  margin-top: 14rpx;
}

.empty-copy {
  display: block;
  margin-top: 12rpx;
}

.empty-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
  margin-top: 20rpx;
}

.empty-actions .ghost-button,
.empty-actions .action-button {
  cursor: pointer;
}

@media (max-width: 899px) {
  .hero-stats {
    grid-template-columns: 1fr;
  }

  .filter-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
