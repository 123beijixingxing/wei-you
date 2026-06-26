<template>
  <view class="page-shell">
    <view class="hero-card">
      <text class="title-lg">通知中心</text>
      <text class="body-sm muted">这里聚合系统通知、朋友圈提醒与服务消息，联调版展示后端真实通知列表。</text>
    </view>

    <view class="input-chip search-chip">
      <text>筛选</text>
      <picker class="filter-picker" :range="types" range-key="label" @change="handleTypeChange">
        <view class="filter-value">{{ currentTypeLabel }}</view>
      </picker>
    </view>

    <view class="panel-lite notice-list">
      <view v-for="item in notices" :key="item.noticeId" class="notice-card" @click="markRead(item)">
        <view class="notice-head">
          <view class="notice-main">
            <text class="title-md">{{ item.title }}</text>
            <text class="body-sm muted notice-content">{{ item.content }}</text>
            <text class="body-sm muted">{{ item.createdAt }}</text>
          </view>
          <view class="notice-side">
            <text class="body-sm muted">{{ item.noticeType }}</text>
            <view class="notice-state" :class="item.readStatus ? 'is-read' : 'is-unread'">{{ item.readStatus ? '已读' : '未读' }}</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { noticeApi } from "@/api/modules";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const notices = ref([]);
const type = ref("");
const types = [
  { label: "全部", value: "" },
  { label: "系统", value: "system" },
  { label: "朋友圈", value: "moment" }
];

const currentTypeLabel = computed(() => types.find((item) => item.value === type.value)?.label || "全部");

onShow(() => {
  loadNotices();
});

async function loadNotices() {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    const page = await noticeApi.list(type.value);
    notices.value = page?.list || [];
  } catch (error) {
    uni.showToast({
      title: error.message || "加载通知失败",
      icon: "none"
    });
  }
}

async function markRead(item) {
  if (item.readStatus) {
    return;
  }
  try {
    const result = await noticeApi.read({ noticeId: item.noticeId });
    notices.value = notices.value.map((notice) => {
      if (notice.noticeId !== item.noticeId) {
        return notice;
      }
      return {
        ...notice,
        readStatus: result?.readStatus ?? 1
      };
    });
  } catch (error) {
    uni.showToast({
      title: error.message || "标记失败",
      icon: "none"
    });
  }
}

function handleTypeChange(event) {
  const index = Number(event?.detail?.value || 0);
  type.value = types[index]?.value || "";
  loadNotices();
}
</script>

<style scoped lang="css">
.search-chip {
  margin-top: 18rpx;
}

.filter-picker {
  flex: 1;
}

.filter-value {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  font-size: 26rpx;
}

.panel-lite {
  border-radius: 18rpx;
  background: #fff;
  border: 1rpx solid rgba(0, 0, 0, 0.05);
}

.notice-list {
  margin-top: 12rpx;
  overflow: hidden;
}

.notice-card {
  padding: 18rpx 20rpx;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.notice-card:last-child {
  border-bottom: none;
}

.notice-head {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
}

.notice-main {
  flex: 1;
}

.notice-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10rpx;
}

.notice-content {
  display: block;
  margin: 12rpx 0 10rpx;
  line-height: 1.7;
}

.notice-state {
  padding: 4rpx 10rpx;
  border-radius: 999rpx;
  font-size: 20rpx;
}

.is-read {
  color: var(--wy-subtext);
  background: rgba(0, 0, 0, 0.05);
}

.is-unread {
  color: var(--wy-primary);
  background: rgba(31, 138, 112, 0.08);
}
</style>
