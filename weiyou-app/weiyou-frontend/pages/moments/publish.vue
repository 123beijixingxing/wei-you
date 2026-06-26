<template>
  <view class="page-shell">
    <view class="hero-card">
      <text class="title-lg">发布朋友圈</text>
      <text class="body-sm muted">支持文字与图片动态发布，联调时会先上传图片再创建动态。</text>
      <view class="hero-stats">
        <view class="stat-card">
          <text class="stat-value">{{ content.length }}</text>
          <text class="stat-label">已写字数</text>
        </view>
        <view class="stat-card">
          <text class="stat-value">{{ mediaUrls.length }}</text>
          <text class="stat-label">图片数量</text>
        </view>
      </view>
    </view>

    <view class="panel publish-panel">
      <textarea
        v-model="content"
        class="publish-textarea"
        maxlength="300"
        placeholder="此刻想和朋友分享什么？"
      />
      <view class="media-toolbar">
        <view class="ghost-button" @click="chooseImages">添加图片</view>
        <text class="body-sm muted">最多 9 张，当前 {{ mediaUrls.length }} 张</text>
      </view>
      <view v-if="mediaUrls.length" class="media-grid">
        <view v-for="(image, index) in mediaUrls" :key="image" class="media-card">
          <image class="media-image" :src="image" mode="aspectFill" @click="previewImage(index)" />
          <view class="media-remove" @click="removeImage(index)">×</view>
        </view>
      </view>
      <view class="publish-footer">
        <text class="body-sm muted">{{ content.length }}/300 · {{ mediaUrls.length }} 图</text>
        <view class="action-button" :class="submitting ? 'is-disabled' : ''" @click="submitMoment">
          {{ submitting ? "发布中..." : "发布动态" }}
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from "vue";
import { mediaApi, momentApi } from "@/api/modules";
import { useAppStore } from "@/stores/app";
import { useUserStore } from "@/stores/user";

const appStore = useAppStore();
const userStore = useUserStore();
const content = ref("");
const mediaUrls = ref([]);
const submitting = ref(false);

function chooseImages() {
  const remain = 9 - mediaUrls.value.length;
  if (remain <= 0) {
    uni.showToast({
      title: "最多选择 9 张图片",
      icon: "none"
    });
    return;
  }
  uni.chooseImage({
    count: remain,
    sizeType: ["compressed"],
    success: (res) => {
      mediaUrls.value = [...mediaUrls.value, ...(res.tempFilePaths || [])].slice(0, 9);
    }
  });
}

function previewImage(index) {
  uni.previewImage({
    urls: mediaUrls.value,
    current: mediaUrls.value[index]
  });
}

function removeImage(index) {
  mediaUrls.value = mediaUrls.value.filter((_, itemIndex) => itemIndex !== index);
}

async function submitMoment() {
  const value = content.value.trim();
  if (!userStore.requireAuth()) {
    return;
  }
  if ((!value && !mediaUrls.value.length) || submitting.value) {
    uni.showToast({
      title: "请输入文字或选择图片",
      icon: "none"
    });
    return;
  }
  submitting.value = true;
  try {
    const uploadedMediaUrls = [];
    for (const imagePath of mediaUrls.value) {
      if (/^https?:\/\//.test(imagePath)) {
        uploadedMediaUrls.push(imagePath);
        continue;
      }
      const uploadResult = await mediaApi.uploadLocalImage(imagePath, "moment");
      uploadedMediaUrls.push(uploadResult.url || uploadResult.coverUrl);
    }
    await momentApi.create({
      content: value,
      mediaUrls: uploadedMediaUrls,
      visibleScope: "public"
    });
    await appStore.fetchMoments();
    content.value = "";
    mediaUrls.value = [];
    uni.showToast({
      title: "发布成功",
      icon: "success"
    });
    setTimeout(() => {
      uni.navigateBack();
    }, 400);
  } catch (error) {
    uni.showToast({
      title: error.message || "发布失败",
      icon: "none"
    });
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped lang="css">
.hero-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
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

.publish-panel {
  margin-top: 22rpx;
}

.publish-textarea {
  width: 100%;
  min-height: 260rpx;
  font-size: 28rpx;
  line-height: 1.6;
}

.publish-footer {
  display: flex;
  gap: 16rpx;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  margin-top: 22rpx;
}

.media-toolbar {
  display: flex;
  gap: 16rpx;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  margin-top: 18rpx;
}

.media-toolbar .ghost-button,
.publish-footer .action-button,
.media-image,
.media-remove {
  cursor: pointer;
}

.media-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8rpx;
  margin-top: 14rpx;
}

.media-card {
  position: relative;
}

.media-image {
  width: 100%;
  height: 176rpx;
  border-radius: 14rpx;
}

.media-remove {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  width: 42rpx;
  height: 42rpx;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
}

.is-disabled {
  opacity: 0.7;
}

@media (max-width: 899px) {
  .hero-stats {
    grid-template-columns: 1fr;
  }
}
</style>
