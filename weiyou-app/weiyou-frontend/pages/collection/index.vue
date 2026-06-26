<template>
  <view class="page-shell">
    <view class="wechat-header simple">
      <text class="wechat-title">收藏</text>
    </view>

    <view class="input-chip search-chip">
      <text>搜索</text>
      <input v-model="keyword" class="search-input" placeholder="搜索收藏标题或摘要" @confirm="loadCollection" />
    </view>

    <view v-for="item in items" :key="item.collectionId" class="panel-lite collection-card">
      <image v-if="item.cover" class="collection-cover" :src="item.cover" mode="aspectFill" />
      <view class="collection-meta">
        <text class="title-md">{{ item.title }}</text>
        <text class="body-sm muted">{{ item.summary }}</text>
        <text class="body-sm muted">{{ item.type }} · {{ item.createdAt }}</text>
      </view>
      <view class="ghost-button" @click="removeItem(item)">删除</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { collectionApi } from "@/api/modules";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const keyword = ref("");
const items = ref([]);

onShow(() => {
  loadCollection();
});

async function loadCollection() {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    const page = await collectionApi.list("", keyword.value.trim(), 1);
    items.value = page?.list || [];
  } catch (error) {
    uni.showToast({ title: error.message || "加载收藏失败", icon: "none" });
  }
}

async function removeItem(item) {
  try {
    items.value = await collectionApi.deleteItem({ collectionId: item.collectionId });
    uni.showToast({ title: "已删除", icon: "success" });
  } catch (error) {
    uni.showToast({ title: error.message || "删除失败", icon: "none" });
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

.collection-card {
  display: flex;
  gap: 16rpx;
  align-items: center;
  margin-top: 10rpx;
  padding: 16rpx 18rpx;
}

.collection-cover {
  width: 140rpx;
  height: 140rpx;
  border-radius: 18rpx;
  flex-shrink: 0;
}

.collection-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}
</style>
