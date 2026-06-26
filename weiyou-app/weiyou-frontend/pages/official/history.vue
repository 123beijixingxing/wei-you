<template>
  <view class="page-shell">
    <view class="wechat-header simple">
      <text class="wechat-title">历史消息</text>
    </view>

    <view class="input-chip search-chip">
      <text>搜索</text>
      <input v-model="keyword" class="search-input" placeholder="搜索文章标题或摘要" />
    </view>

    <view v-for="item in filteredArticles" :key="item.articleId" class="panel-lite article-card" @click="openArticle(item)">
      <image class="article-cover" :src="item.cover" mode="aspectFill" />
      <view class="article-meta">
        <text class="title-md">{{ item.title }}</text>
        <text class="body-sm muted article-summary">{{ item.summary }}</text>
        <text class="body-sm muted">{{ item.publishAt }} · {{ item.likeCount || 0 }} 赞</text>
      </view>
      <text class="utility-arrow">›</text>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { officialApi } from "@/api/modules";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const officialId = ref(20001);
const articles = ref([]);
const keyword = ref("");

const filteredArticles = computed(() => {
  const text = keyword.value.trim().toLowerCase();
  if (!text) {
    return articles.value;
  }
  return articles.value.filter((item) => {
    return (item.title || "").toLowerCase().includes(text) || (item.summary || "").toLowerCase().includes(text);
  });
});

onLoad((query) => {
  officialId.value = Number(query?.officialId || 20001);
  loadHistory();
});

async function loadHistory() {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    const page = await officialApi.history(officialId.value);
    articles.value = page?.list || [];
  } catch (error) {
    uni.showToast({
      title: error.message || "加载历史消息失败",
      icon: "none"
    });
  }
}

function openArticle(item) {
  uni.navigateTo({
    url: `/pages/official/article?articleId=${item.articleId}&officialId=${item.officialId}`
  });
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

.article-card {
  display: flex;
  gap: 16rpx;
  align-items: center;
  margin-top: 10rpx;
  padding: 16rpx 18rpx;
}

.article-cover {
  width: 180rpx;
  height: 180rpx;
  border-radius: 20rpx;
  flex-shrink: 0;
}

.article-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.article-summary {
  line-height: 1.6;
}

.utility-arrow {
  font-size: 30rpx;
  color: #a0a0a0;
}
</style>
