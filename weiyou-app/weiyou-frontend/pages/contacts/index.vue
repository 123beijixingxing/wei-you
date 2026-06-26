<template>
  <view class="page-shell wechat-shell">
    <view class="wechat-header simple">
      <text class="wechat-title">通讯录</text>
    </view>

    <view class="wechat-search">
      <view class="wechat-search-box">
        <text class="search-icon">搜</text>
        <input v-model="keyword" class="wechat-search-input" placeholder="搜索好友、备注或标签" @confirm="loadContacts" />
      </view>
    </view>

    <view class="utility-list panel-lite">
      <view class="utility-row" @click="go('/pages/contacts/new-friend')">
        <text class="utility-label">新的朋友</text>
        <text class="utility-arrow">›</text>
      </view>
      <view class="utility-row" @click="go('/pages/contacts/add-friend')">
        <text class="utility-label">添加朋友</text>
        <text class="utility-arrow">›</text>
      </view>
      <view class="utility-row" @click="go('/pages/group/list')">
        <text class="utility-label">群聊</text>
        <text class="utility-arrow">›</text>
      </view>
    </view>

    <view v-if="groups.length" class="wechat-contact-list">
      <view v-for="group in groups" :key="group.title" class="group-block">
        <view class="group-label">{{ group.title }}</view>
        <view class="group-panel panel-lite">
          <contact-item v-for="item in group.items" :key="item.id" :contact="item" @click="openProfile(item)" />
        </view>
      </view>
    </view>

    <view v-else class="panel empty-panel">
      <text class="title-md">还没有联系人数据</text>
      <text class="body-sm muted empty-copy">可以先通过新的朋友、添加朋友或群聊入口开始补齐社交关系链。</text>
      <view class="empty-actions">
        <view class="action-button" @click="go('/pages/contacts/add-friend')">添加朋友</view>
        <view class="ghost-button" @click="go('/pages/group/list')">查看群聊</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import ContactItem from "@/components/contact-item.vue";
import { contactApi } from "@/api/modules";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const keyword = ref("");
const contacts = ref([]);

const groups = computed(() => {
  const map = new Map();
  contacts.value.forEach((item) => {
    const letter = item.letter || "#";
    if (!map.has(letter)) {
      map.set(letter, []);
    }
    map.get(letter).push(item);
  });
  return Array.from(map.entries()).map(([title, items]) => ({ title, items }));
});

onShow(() => {
  loadContacts();
});

async function loadContacts() {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    const page = await contactApi.list(keyword.value.trim());
    contacts.value = (page?.list || []).map((item) => ({
      id: item.userId,
      name: item.nickname,
      alias: item.remark || `用户 ${item.userId}`,
      tag: item.star ? "星标" : "好友",
      online: true,
      avatar: item.avatar || "https://weiyou.local/avatar/default.png",
      letter: item.letter || "#",
      conversationId: Number(item.userId) === 10002 ? 90001 : 0
    }));
  } catch (error) {
    uni.showToast({ title: error.message || "加载通讯录失败", icon: "none" });
  }
}

function openProfile(item) {
  uni.navigateTo({
    url: `/pages/contacts/profile?id=${item.id}&conversationId=${item.conversationId || 0}`
  });
}

function go(url) {
  uni.navigateTo({ url });
}
</script>

<style scoped lang="css">
.wechat-shell {
  padding-top: 0;
}

.wechat-header.simple {
  padding: 18rpx 6rpx 12rpx;
}

.wechat-title {
  font-size: 34rpx;
  font-weight: 700;
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

.panel-lite {
  border-radius: 18rpx;
  background: #fff;
  border: 1rpx solid rgba(0, 0, 0, 0.05);
}

.utility-list {
  overflow: hidden;
}

.utility-row {
  height: 92rpx;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.utility-row:last-child {
  border-bottom: none;
}

.utility-label {
  font-size: 28rpx;
  color: var(--wy-text);
}

.utility-arrow {
  font-size: 30rpx;
  color: #a0a0a0;
}

.wechat-contact-list {
  margin-top: 18rpx;
}

.group-block + .group-block {
  margin-top: 16rpx;
}

.group-label {
  padding: 0 8rpx 10rpx;
  font-size: 22rpx;
  color: var(--wy-subtext);
}

.group-panel {
  overflow: hidden;
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
  gap: 12rpx;
  margin-top: 18rpx;
}
</style>
