<template>
  <view class="page-shell">
    <view class="hero-card">
      <text class="title-lg">朋友圈</text>
      <text class="body-sm muted">图文动态、点赞评论、地理位置与私密分组，是社交熟人关系中最重要的内容流之一。</text>
      <view class="hero-stats">
        <view class="stat-card">
          <text class="stat-value">{{ moments.length }}</text>
          <text class="stat-label">动态数量</text>
        </view>
        <view class="stat-card">
          <text class="stat-value">{{ totalLikes }}</text>
          <text class="stat-label">累计点赞</text>
        </view>
        <view class="stat-card">
          <text class="stat-value">{{ totalComments }}</text>
          <text class="stat-label">累计评论</text>
        </view>
      </view>
      <view class="hero-actions">
        <view class="action-button" @click="goPublish">发布动态</view>
        <view class="ghost-button" @click="go('/pages/profile/status')">状态设置</view>
      </view>
    </view>

    <view v-if="moments.length" v-for="item in moments" :key="item.id" class="moment-card panel">
      <view class="moment-head">
        <image class="avatar" :src="item.author.avatar" mode="aspectFill" />
        <view class="moment-meta">
          <text class="title-md">{{ item.author.name }}</text>
          <text class="body-sm muted">{{ item.location }} · {{ item.time }}</text>
        </view>
      </view>
      <text class="body-md moment-content">{{ item.content }}</text>
      <view v-if="item.images.length" class="moment-grid">
        <image
          v-for="image in item.images"
          :key="image"
          :src="image"
          mode="aspectFill"
          class="moment-image"
          @click="previewImages(item.images, image)"
        />
      </view>
      <view class="moment-footer">
        <text class="body-sm muted footer-action" @click="toggleLike(item)">{{ item.liked ? "取消赞" : "点赞" }} · {{ item.likeCount }}</text>
        <text class="body-sm muted footer-action" @click="commentMoment(item)">评论 · {{ item.commentCount }}</text>
      </view>
      <view v-if="item.comments && item.comments.length" class="comment-list">
        <view v-for="comment in item.comments" :key="comment.id" class="comment-item">
          <view class="comment-main">
            <text class="comment-author">{{ comment.userName }}：</text>
            <text class="comment-content">{{ comment.replyUserName ? `回复 ${comment.replyUserName}：` : "" }}{{ comment.content }}</text>
          </view>
          <view class="comment-actions">
            <text class="comment-action" @click="replyComment(item, comment)">回复</text>
            <text v-if="comment.userId === userStore.profile.id" class="comment-action danger" @click="deleteComment(item, comment)">删除</text>
          </view>
        </view>
      </view>
    </view>

    <view v-else class="panel empty-panel">
      <text class="title-md">还没有朋友圈动态</text>
      <text class="body-sm muted empty-copy">可以先发布一条测试动态，联调点赞、评论和图片预览链路。</text>
      <view class="empty-actions">
        <view class="action-button" @click="goPublish">发布动态</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { storeToRefs } from "pinia";
import { useAppStore } from "@/stores/app";
import { useUserStore } from "@/stores/user";

const appStore = useAppStore();
const userStore = useUserStore();
const { moments } = storeToRefs(appStore);
const totalLikes = computed(() => moments.value.reduce((sum, item) => sum + Number(item.likeCount || 0), 0));
const totalComments = computed(() => moments.value.reduce((sum, item) => sum + Number(item.commentCount || 0), 0));

onShow(async () => {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    await appStore.fetchMoments();
  } catch (error) {
    uni.showToast({
      title: error.message || "加载朋友圈失败",
      icon: "none"
    });
  }
});

function goPublish() {
  uni.navigateTo({
    url: "/pages/moments/publish"
  });
}

function go(url) {
  uni.navigateTo({ url });
}

function previewImages(images, current) {
  uni.previewImage({
    urls: images,
    current
  });
}

async function toggleLike(item) {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    await appStore.toggleMomentLike(item.id);
  } catch (error) {
    uni.showToast({
      title: error.message || "操作失败",
      icon: "none"
    });
  }
}

function commentMoment(item) {
  openCommentDialog(item);
}

function replyComment(item, comment) {
  openCommentDialog(item, comment);
}

function openCommentDialog(item, replyComment = null) {
  if (!userStore.requireAuth()) {
    return;
  }
  uni.showModal({
    title: replyComment ? `回复 ${replyComment.userName}` : "发表评论",
    editable: true,
    placeholderText: "说点什么...",
    success: async (result) => {
      const content = (result.content || "").trim();
      if (!result.confirm || !content) {
        return;
      }
      try {
        await appStore.addMomentComment(item.id, content, replyComment?.id || null);
        uni.showToast({
          title: "评论成功",
          icon: "success"
        });
      } catch (error) {
        uni.showToast({
          title: error.message || "评论失败",
          icon: "none"
        });
      }
    }
  });
}

function deleteComment(item, comment) {
  if (!userStore.requireAuth()) {
    return;
  }
  uni.showModal({
    title: "删除评论",
    content: "确认删除这条评论吗？",
    success: async (result) => {
      if (!result.confirm) {
        return;
      }
      try {
        await appStore.deleteMomentComment(item.id, comment.id);
        uni.showToast({
          title: "已删除",
          icon: "success"
        });
      } catch (error) {
        uni.showToast({
          title: error.message || "删除失败",
          icon: "none"
        });
      }
    }
  });
}
</script>

<style scoped lang="css">
.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 16rpx;
}

.hero-actions .ghost-button,
.hero-actions .action-button,
.empty-actions .action-button,
.footer-action,
.comment-action,
.moment-image {
  cursor: pointer;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12rpx;
  margin-top: 22rpx;
}

.stat-card {
  padding: 16rpx 18rpx;
  border-radius: 18rpx;
  background: #f6f6f6;
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

.moment-card {
  margin-top: 14rpx;
}

.moment-head {
  display: flex;
  gap: 18rpx;
  align-items: center;
}

.moment-meta {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.moment-content {
  display: block;
  margin-top: 14rpx;
}

.moment-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8rpx;
  margin-top: 14rpx;
}

.moment-image {
  width: 100%;
  height: 176rpx;
  border-radius: 14rpx;
}

.moment-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 14rpx;
}

.footer-action {
  color: var(--wy-primary);
}

.comment-list {
  margin-top: 12rpx;
  padding: 14rpx 16rpx;
  border-radius: 14rpx;
  background: #f6f6f6;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.comment-item {
  font-size: 24rpx;
  color: var(--wy-subtext);
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
}

.comment-author {
  color: var(--wy-primary);
}

.comment-main {
  flex: 1;
  line-height: 1.6;
}

.comment-actions {
  display: flex;
  gap: 12rpx;
  align-items: center;
}

.comment-action {
  color: var(--wy-primary);
}

.danger {
  color: #c54d43;
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
