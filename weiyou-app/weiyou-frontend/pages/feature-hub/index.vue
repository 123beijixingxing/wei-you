<template>
  <view class="page-shell">
    <view class="hero-card">
      <text class="title-lg">{{ pageTitle }}</text>
      <text class="body-sm muted">这里统一展示社交、支付、服务与生态入口，便于在一个页面内快速进入高频模块。</text>
      <view class="hero-stats">
        <view class="stat-card">
          <text class="stat-value">{{ workbenchSections.length }}</text>
          <text class="stat-label">能力分组</text>
        </view>
        <view class="stat-card">
          <text class="stat-value">{{ totalFeatures }}</text>
          <text class="stat-label">全部能力</text>
        </view>
        <view class="stat-card">
          <text class="stat-value">{{ onlineFeatures }}</text>
          <text class="stat-label">在线能力</text>
        </view>
      </view>
    </view>

    <view v-if="workbenchSections.length" v-for="section in workbenchSections" :key="section.title" class="panel section-panel">
      <view class="section-title">
        <text>{{ section.title }}</text>
        <text class="section-subtitle">{{ section.items.length }} 个能力</text>
      </view>
      <view class="grid-2">
        <quick-entry
          v-for="item in section.items"
          :key="item.code"
          :icon="item.title.slice(0, 1)"
          :title="item.title"
          :desc="item.subtitle"
          :status="item.status"
          @click="go(item.route)"
        />
      </view>
    </view>

    <view v-else class="panel empty-panel">
      <text class="title-md">功能中心还没有拉到能力数据</text>
      <text class="body-sm muted empty-copy">可以先回到发现页、钱包或聊天页继续联调，等能力矩阵同步后再统一校验。</text>
      <view class="empty-actions">
        <view class="action-button" @click="go('/pages/discover/index')">发现页</view>
        <view class="ghost-button" @click="go('/pages/wallet/index')">钱包</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onLoad, onShow } from "@dcloudio/uni-app";
import { storeToRefs } from "pinia";
import QuickEntry from "@/components/quick-entry.vue";
import { useAppStore } from "@/stores/app";
import { useUserStore } from "@/stores/user";
import { safeSwitchTab } from "@/utils/navigation";

const appStore = useAppStore();
const userStore = useUserStore();
const { workbenchSections } = storeToRefs(appStore);
const title = ref("");

const pageTitle = computed(() => title.value || "微友功能中心");
const totalFeatures = computed(() => workbenchSections.value.reduce((sum, section) => sum + (section.items?.length || 0), 0));
const onlineFeatures = computed(() => workbenchSections.value.reduce((sum, section) => sum + (section.items || []).filter((item) => item.status === "在线").length, 0));

onLoad((query) => {
  title.value = query?.title || "";
});

onShow(async () => {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    await appStore.fetchWorkbenchSections();
  } catch (error) {
    uni.showToast({
      title: error.message || "加载功能中心失败",
      icon: "none"
    });
  }
});

function go(url) {
  if (url.startsWith("/pages/feature-hub")) {
    uni.navigateTo({ url });
    return;
  }
  if (url.startsWith("/pages/chat") || url.startsWith("/pages/contacts") || url.startsWith("/pages/discover") || url.startsWith("/pages/me")) {
    safeSwitchTab(url);
    return;
  }
  uni.navigateTo({ url });
}
</script>

<style scoped lang="css">
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

.section-panel,
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
  margin-top: 24rpx;
}

.empty-actions .ghost-button,
.empty-actions .action-button {
  cursor: pointer;
}

@media (max-width: 899px) {
  .hero-stats {
    grid-template-columns: 1fr;
  }
}
</style>
