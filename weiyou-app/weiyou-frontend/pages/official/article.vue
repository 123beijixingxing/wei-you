<template>
  <view class="page-shell">
    <view class="wechat-header article-header">
      <text class="wechat-title">{{ article.title }}</text>
      <text class="body-sm muted">{{ article.publishAt }}</text>
    </view>

    <view class="hero-card article-hero">
      <image class="article-banner" :src="article.cover" mode="aspectFill" />
    </view>

    <view class="panel article-panel">
      <text class="body-md article-summary">{{ article.summary }}</text>
      <rich-text :nodes="article.contentHtml" />
    </view>

    <view class="actions-row">
      <view class="ghost-button" @click="backToHistory">返回历史消息</view>
      <view class="action-button" :class="liked ? 'is-liked' : ''" @click="toggleLike">{{ liked ? "已点赞" : "点赞" }} · {{ article.likeCount || 0 }}</view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { officialApi } from "@/api/modules";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const articleId = ref(300001);
const officialId = ref(20001);
const liked = ref(false);
const article = reactive({
  title: "",
  publishAt: "",
  cover: "https://weiyou.local/official/article-cover-1.png",
  summary: "",
  contentHtml: "",
  likeCount: 0
});

onLoad((query) => {
  articleId.value = Number(query?.articleId || 300001);
   officialId.value = Number(query?.officialId || 20001);
  loadArticle();
});

async function loadArticle() {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    const data = await officialApi.article(articleId.value);
    Object.assign(article, data || {});
  } catch (error) {
    uni.showToast({
      title: error.message || "加载文章失败",
      icon: "none"
    });
  }
}

async function toggleLike() {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    const action = liked.value ? "unlike" : "like";
    const result = await officialApi.likeArticle({
      articleId: articleId.value,
      action
    });
    liked.value = action === "like";
    article.likeCount = result?.likeCount ?? article.likeCount;
  } catch (error) {
    uni.showToast({
      title: error.message || "点赞失败",
      icon: "none"
    });
  }
}

function backToHistory() {
  uni.navigateTo({
    url: `/pages/official/history?officialId=${officialId.value}`
  });
}
</script>

<style scoped lang="css">
.article-header {
  padding: 18rpx 6rpx 12rpx;
}

.wechat-title {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  line-height: 1.4;
}

.article-banner {
  width: 100%;
  height: 260rpx;
  border-radius: 18rpx;
  margin-top: 16rpx;
}

.article-panel {
  margin-top: 16rpx;
}

.article-summary {
  display: block;
  margin-bottom: 18rpx;
  line-height: 1.75;
}

.actions-row {
  display: flex;
  gap: 12rpx;
  margin-top: 18rpx;
}

.is-liked {
  opacity: 0.85;
}
</style>
