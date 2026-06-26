<template>
  <view class="page-shell">
    <view class="hero-card">
      <text class="title-lg">群聊列表</text>
      <text class="body-sm muted">这里展示当前账号已加入的群聊，支持查看群详情并继续扩展群成员、公告和管理能力。</text>
      <view class="hero-actions">
        <view class="action-button" @click="goCreate">发起群聊</view>
      </view>
    </view>

    <view class="input-chip search-chip">
      <text>搜索</text>
      <input v-model="keyword" class="search-input" placeholder="搜索群名称" @confirm="loadGroups" />
    </view>

    <view class="panel-lite group-list">
      <view v-for="item in groups" :key="item.groupId" class="group-row" @click="openGroup(item)">
        <image class="avatar" :src="item.groupAvatar" mode="aspectFill" />
        <view class="group-meta">
          <text class="title-md">{{ item.groupName }}</text>
          <text class="body-sm muted">成员：{{ item.memberCount }}</text>
          <text class="body-sm muted group-notice">公告：{{ item.notice }}</text>
        </view>
        <text class="utility-arrow">›</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { groupApi } from "@/api/modules";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const keyword = ref("");
const groups = ref([]);

onShow(() => {
  loadGroups();
});

async function loadGroups() {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    groups.value = await groupApi.list(keyword.value.trim());
  } catch (error) {
    uni.showToast({
      title: error.message || "加载群聊失败",
      icon: "none"
    });
  }
}

function openGroup(item) {
  uni.navigateTo({
    url: `/pages/group/detail?groupId=${item.groupId}`
  });
}

function goCreate() {
  uni.navigateTo({
    url: "/pages/group/create"
  });
}
</script>

<style scoped lang="css">
.hero-actions {
  margin-top: 16rpx;
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
  overflow: hidden;
}

.group-list {
  margin-top: 12rpx;
}

.group-row {
  min-height: 108rpx;
  padding: 18rpx 20rpx;
  display: flex;
  gap: 16rpx;
  align-items: center;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.group-row:last-child {
  border-bottom: none;
}

.group-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.group-notice {
  line-height: 1.5;
}

.utility-arrow {
  font-size: 30rpx;
  color: #a0a0a0;
}
</style>
