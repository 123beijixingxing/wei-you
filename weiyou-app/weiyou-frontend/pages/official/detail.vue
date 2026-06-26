<template>
  <view class="page-shell">
    <view class="hero-card official-card">
      <image class="avatar-lg" :src="official.avatar" mode="aspectFill" />
      <view class="official-meta">
        <text class="title-lg">{{ official.name }}</text>
        <text class="body-sm muted">{{ official.verified ? "已认证服务号" : "未认证" }}</text>
        <text class="body-sm muted">{{ official.intro }}</text>
      </view>
    </view>

    <view class="panel-lite official-list">
      <view class="official-row" @click="toggleFollow">
        <text class="official-label">{{ official.followed ? "取消关注" : "关注公众号" }}</text>
        <text class="utility-arrow">›</text>
      </view>
      <view class="official-row" @click="goList">
        <text class="official-label">公众号列表</text>
        <text class="utility-arrow">›</text>
      </view>
      <view class="official-row" @click="goHistory">
        <text class="official-label">历史消息</text>
        <text class="utility-arrow">›</text>
      </view>
      <view class="official-row" @click="goArticle">
        <text class="official-label">查看推荐文章</text>
        <text class="utility-arrow">›</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { officialApi } from "@/api/modules";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const officialId = ref(20001);
const official = reactive({
  officialId: 20001,
  name: "微友服务号",
  avatar: "https://weiyou.local/official/service.png",
  intro: "微友官方服务、活动与消息通知入口",
  verified: true,
  followed: true
});

onLoad((query) => {
  officialId.value = Number(query?.officialId || 20001);
  loadOfficial();
});

async function loadOfficial() {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    const data = await officialApi.detail(officialId.value);
    Object.assign(official, data || {});
  } catch (error) {
    uni.showToast({
      title: error.message || "加载公众号失败",
      icon: "none"
    });
  }
}

function goHistory() {
  uni.navigateTo({
    url: `/pages/official/history?officialId=${officialId.value}`
  });
}

function goList() {
  uni.navigateTo({
    url: "/pages/official/list"
  });
}

function goArticle() {
  uni.navigateTo({
    url: `/pages/official/article?articleId=300001&officialId=${officialId.value}`
  });
}

async function toggleFollow() {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    const action = official.followed ? "unfollow" : "follow";
    const result = await officialApi.follow({
      officialId: officialId.value,
      action
    });
    official.followed = result?.followed ?? official.followed;
    uni.showToast({
      title: official.followed ? "已关注" : "已取消",
      icon: "success"
    });
  } catch (error) {
    uni.showToast({
      title: error.message || "操作失败",
      icon: "none"
    });
  }
}
</script>

<style scoped lang="css">
.official-card {
  display: flex;
  gap: 18rpx;
  align-items: center;
}

.official-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.panel-lite {
  border-radius: 18rpx;
  background: #fff;
  border: 1rpx solid rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.official-list {
  margin-top: 18rpx;
}

.official-row {
  min-height: 92rpx;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.official-row:last-child {
  border-bottom: none;
}

.official-label {
  font-size: 28rpx;
  color: var(--wy-text);
}

.utility-arrow {
  font-size: 30rpx;
  color: #a0a0a0;
}
</style>
