<template>
  <view class="page-shell wechat-shell">
    <view class="wechat-header">
      <text class="wechat-title">微友</text>
      <view class="wechat-header-actions">
        <view class="header-dot" />
        <view class="header-dot" />
      </view>
    </view>

    <view class="wechat-search">
      <view class="wechat-search-box">
        <text class="search-icon">搜</text>
        <input v-model="keyword" class="wechat-search-input" placeholder="搜索好友、群聊、消息、小程序" />
      </view>
    </view>

    <view class="shortcut-strip">
      <view class="shortcut-item" @click="go('/pages/contacts/new-friend')">新的朋友</view>
      <view class="shortcut-item" @click="go('/pages/contacts/add-friend')">添加朋友</view>
      <view class="shortcut-item" @click="go('/pages/scan/index')">扫一扫</view>
      <view class="shortcut-item" @click="go('/pages/wallet/index')">钱包</view>
    </view>

    <view v-if="filteredConversations.length" class="wechat-list">
      <chat-item
        v-for="item in filteredConversations"
        :key="item.id"
        :conversation="item"
        @open="openConversation"
      />
    </view>

    <view v-else class="panel empty-panel">
      <text class="title-md">还没有最近会话</text>
      <text class="body-sm muted empty-copy">可以先从添加朋友、扫一扫或朋友圈入口开始触发第一条会话。</text>
      <view class="empty-actions">
        <view class="ghost-button" @click="go('/pages/contacts/add-friend')">添加朋友</view>
        <view class="action-button" @click="go('/pages/scan/index')">扫一扫</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { storeToRefs } from "pinia";
import ChatItem from "@/components/chat-item.vue";
import { useChatStore } from "@/stores/chat";
import { useUserStore } from "@/stores/user";

const chatStore = useChatStore();
const userStore = useUserStore();
const keyword = ref("");
const { conversations } = storeToRefs(chatStore);

onShow(async () => {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    await Promise.all([
      userStore.fetchProfile(),
      chatStore.fetchConversations()
    ]);
    chatStore.ensureSocketConnected().catch((error) => {
      console.warn("Socket connect warning on chat index", error);
    });
  } catch (error) {
    uni.showToast({ title: error.message || "加载会话失败", icon: "none" });
  }
});

const filteredConversations = computed(() => {
  if (!keyword.value.trim()) {
    return conversations.value;
  }
  return conversations.value.filter((item) => `${item.title} ${item.preview}`.toLowerCase().includes(keyword.value.trim().toLowerCase()));
});

function openConversation(conversation) {
  chatStore.openConversation(conversation.id);
  uni.navigateTo({ url: `/pages/conversation/detail?id=${conversation.id}` });
}

function go(url) {
  uni.navigateTo({ url });
}
</script>

<style scoped lang="css">
.wechat-shell {
  padding-top: 0;
}

.wechat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18rpx 6rpx 12rpx;
}

.wechat-title {
  font-size: 34rpx;
  font-weight: 700;
}

.wechat-header-actions {
  display: flex;
  gap: 12rpx;
}

.header-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: rgba(17, 17, 17, 0.24);
}

.wechat-search {
  padding: 0 0 14rpx;
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

.shortcut-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10rpx;
  margin-bottom: 14rpx;
}

.shortcut-item {
  height: 68rpx;
  border-radius: 16rpx;
  background: #ffffff;
  border: 1rpx solid rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 23rpx;
  color: var(--wy-subtext);
}

.wechat-list {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

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
  gap: 12rpx;
  margin-top: 18rpx;
}
</style>
