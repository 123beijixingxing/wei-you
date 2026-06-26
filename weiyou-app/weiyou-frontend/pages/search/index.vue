<template>
  <view class="page-shell wechat-shell">
    <view class="wechat-header simple">
      <text class="wechat-title">搜一搜</text>
    </view>

    <view class="wechat-search-box">
      <text class="search-icon">搜</text>
      <input v-model="keyword" class="wechat-search-input" placeholder="输入关键词后回车搜索" @input="handleKeywordChange" @confirm="runSearch" />
    </view>

    <view v-if="suggestions.length" class="panel-lite result-panel">
      <view v-for="item in suggestions" :key="item" class="simple-row" @click="applySuggestion(item)">
        <text class="simple-label">{{ item }}</text>
        <text class="utility-arrow">›</text>
      </view>
    </view>

    <view v-if="results.length" class="panel-lite result-panel">
      <view v-for="item in results" :key="`${item.bizType}-${item.bizId}`" class="result-item" @click="openResult(item)">
        <image class="avatar" :src="item.cover || 'https://weiyou.local/avatar/default.png'" mode="aspectFill" />
        <view class="result-meta">
          <text class="title-md">{{ item.title }}</text>
          <text class="body-sm muted">{{ item.subtitle }}</text>
        </view>
        <text class="utility-arrow">›</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from "vue";
import { searchApi } from "@/api/modules";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const keyword = ref("");
const suggestions = ref([]);
const results = ref([]);

async function handleKeywordChange(event) {
  const value = (event?.detail?.value || keyword.value || "").trim();
  keyword.value = value;
  if (!value || !userStore.requireAuth()) {
    suggestions.value = [];
    return;
  }
  try {
    suggestions.value = await searchApi.suggest(value);
  } catch (error) {
    suggestions.value = [];
  }
}

async function runSearch() {
  const value = keyword.value.trim();
  if (!value || !userStore.requireAuth()) {
    return;
  }
  try {
    const page = await searchApi.global(value);
    results.value = page?.list || [];
  } catch (error) {
    uni.showToast({
      title: error.message || "搜索失败",
      icon: "none"
    });
  }
}

function applySuggestion(text) {
  keyword.value = text;
  runSearch();
}

function openResult(item) {
  if (!item.routePath) {
    return;
  }
  uni.navigateTo({ url: item.routePath });
}
</script>

<style scoped lang="css">
.wechat-header.simple {
  padding: 18rpx 6rpx 12rpx;
}

.wechat-shell {
  padding-top: 0;
}

.wechat-title {
  font-size: 34rpx;
  font-weight: 700;
}

.wechat-search-box {
  display: flex;
  align-items: center;
  gap: 12rpx;
  height: 72rpx;
  padding: 0 20rpx;
  border-radius: 18rpx;
  background: #ffffff;
  border: 1rpx solid rgba(0, 0, 0, 0.05);
}

.search-icon {
  font-size: 22rpx;
  color: var(--wy-subtext);
}

.wechat-search-input {
  flex: 1;
  height: 100%;
  font-size: 26rpx;
}

.panel-lite {
  border-radius: 18rpx;
  background: #fff;
  border: 1rpx solid rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.result-panel {
  margin-top: 14rpx;
}

.simple-row,
.result-item {
  min-height: 88rpx;
  padding: 16rpx 18rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.simple-row:last-child,
.result-item:last-child {
  border-bottom: none;
}

.simple-label {
  flex: 1;
  font-size: 28rpx;
  color: var(--wy-text);
}

.result-meta {
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
