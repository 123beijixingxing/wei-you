<template>
  <view class="page-shell">
    <view class="hero-card">
      <text class="title-lg">公众号列表</text>
      <text class="body-sm muted">这里汇总可关注的公众号入口，便于后续继续扩展分类、排序和更多服务号能力。</text>
    </view>

    <view class="input-chip search-chip">
      <text>搜索</text>
      <input v-model="keyword" class="search-input" placeholder="搜索公众号名称" />
    </view>

    <view v-for="item in filteredAccounts" :key="item.officialId" class="panel-lite official-card" @click="openDetail(item)">
      <image class="avatar" :src="item.avatar" mode="aspectFill" />
      <view class="official-meta">
        <text class="title-md">{{ item.name }}</text>
        <text class="body-sm muted">{{ item.intro }}</text>
        <text class="body-sm muted">{{ item.followed ? "已关注" : "未关注" }}</text>
      </view>
      <text class="utility-arrow">›</text>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { officialApi } from "@/api/modules";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const keyword = ref("");
const accounts = ref([]);

const filteredAccounts = computed(() => {
  const text = keyword.value.trim().toLowerCase();
  if (!text) {
    return accounts.value;
  }
  return accounts.value.filter((item) => (item.name || "").toLowerCase().includes(text));
});

onShow(() => {
  loadAccounts();
});

async function loadAccounts() {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    accounts.value = await officialApi.list();
  } catch (error) {
    uni.showToast({
      title: error.message || "加载公众号列表失败",
      icon: "none"
    });
  }
}

function openDetail(item) {
  uni.navigateTo({
    url: `/pages/official/detail?officialId=${item.officialId}`
  });
}
</script>

<style scoped lang="css">
.search-chip {
  margin-top: 18rpx;
}

.search-input {
  flex: 1;
  height: 100%;
  font-size: 26rpx;
}

.panel-lite {
  border-radius: 18rpx;
  background: #fff;
  border: 1rpx solid rgba(0, 0, 0, 0.05);
}

.official-card {
  display: flex;
  gap: 16rpx;
  align-items: center;
  margin-top: 10rpx;
  padding: 16rpx 18rpx;
}

.official-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.utility-arrow {
  font-size: 30rpx;
  color: #a0a0a0;
}
</style>
