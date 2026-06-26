<template>
  <view class="page-shell">
    <view class="hero-card">
      <text class="title-lg">{{ detail.title || '聊天设置' }}</text>
      <text class="body-sm muted">调整会话置顶、免打扰、标为未读与清空聊天记录。</text>
      <view class="hero-stats">
        <view class="stat-card">
          <text class="stat-value">{{ activeFlags }}</text>
          <text class="stat-label">已启用项</text>
        </view>
        <view class="stat-card">
          <text class="stat-value">{{ detail.unreadCount || 0 }}</text>
          <text class="stat-label">未读数量</text>
        </view>
        <view class="stat-card">
          <text class="stat-value">{{ detail.clearBeforeTime ? '已清理' : '保留中' }}</text>
          <text class="stat-label">记录状态</text>
        </view>
      </view>
    </view>

    <view class="panel detail-panel">
      <view class="setting-row">
        <text class="body-md">会话置顶</text>
        <view class="ghost-button mini-btn" @click="toggleSetting('top', !detail.top)">{{ detail.top ? '取消置顶' : '置顶会话' }}</view>
      </view>
      <view class="setting-row">
        <text class="body-md">消息免打扰</text>
        <view class="ghost-button mini-btn" @click="toggleSetting('mute', !detail.mute)">{{ detail.mute ? '关闭免打扰' : '开启免打扰' }}</view>
      </view>
      <view class="setting-row">
        <text class="body-md">标为未读</text>
        <view class="ghost-button mini-btn" @click="toggleSetting('markUnread', !detail.markUnread)">{{ detail.markUnread ? '取消未读' : '标为未读' }}</view>
      </view>
      <text class="body-sm muted detail-text">当前未读数：{{ detail.unreadCount || 0 }}</text>
      <text v-if="detail.clearBeforeTime" class="body-sm muted detail-text">已清空至：{{ detail.clearBeforeTime }}</text>
    </view>

    <view class="panel danger-panel">
      <view class="panel-head">
        <text class="title-md">清空聊天记录</text>
        <text class="body-sm muted">清空后，本会话历史记录将从当前时间重新开始显示。</text>
      </view>
      <view class="danger-button" :class="loading ? 'is-disabled' : ''" @click="clearConversationHistory">
        {{ loading ? '处理中...' : '清空当前聊天记录' }}
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { chatApi } from "@/api/modules";
import { useChatStore } from "@/stores/chat";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const chatStore = useChatStore();
const conversationId = ref(0);
const loading = ref(false);
const detail = reactive({
  conversationId: 0,
  conversationType: 1,
  title: "",
  avatar: "",
  top: false,
  mute: false,
  markUnread: false,
  unreadCount: 0,
  clearBeforeTime: ""
});

const activeFlags = computed(() => [detail.top, detail.mute, detail.markUnread].filter(Boolean).length);

onLoad(async (query) => {
  conversationId.value = Number(query?.conversationId || 0);
  await loadDetail();
});

async function loadDetail() {
  if (!userStore.requireAuth() || !conversationId.value) {
    return;
  }
  try {
    const data = await chatApi.conversationDetail(conversationId.value);
    Object.assign(detail, data || {});
  } catch (error) {
    uni.showToast({ title: error.message || "加载聊天设置失败", icon: "none" });
  }
}

async function toggleSetting(key, value) {
  if (!userStore.requireAuth() || loading.value) {
    return;
  }
  loading.value = true;
  try {
    const payload = {
      conversationId: conversationId.value,
      top: key === "top" ? value : null,
      mute: key === "mute" ? value : null,
      markUnread: key === "markUnread" ? value : null
    };
    const data = await chatApi.updateConversationSetting(payload);
    Object.assign(detail, data || {});
    await chatStore.fetchConversations();
    uni.showToast({ title: "设置已更新", icon: "success" });
  } catch (error) {
    uni.showToast({ title: error.message || "更新设置失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

async function clearConversationHistory() {
  if (!userStore.requireAuth() || loading.value) {
    return;
  }
  loading.value = true;
  try {
    const data = await chatApi.clearConversation({ conversationId: conversationId.value });
    Object.assign(detail, data || {});
    chatStore.messages = {
      ...chatStore.messages,
      [conversationId.value]: []
    };
    chatStore.syncTransientState();
    await chatStore.fetchConversations();
    uni.showToast({ title: "已清空聊天记录", icon: "success" });
  } catch (error) {
    uni.showToast({ title: error.message || "清空聊天失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped lang="css">
.detail-panel,
.danger-panel {
  margin-top: 22rpx;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
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

.setting-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18rpx;
  margin-bottom: 18rpx;
}

.mini-btn {
  height: 68rpx;
  padding: 0 24rpx;
  cursor: pointer;
}

.detail-text {
  display: block;
  margin-top: 10rpx;
}

.panel-head {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.danger-button {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 82rpx;
  margin-top: 20rpx;
  border-radius: 999rpx;
  background: rgba(197, 77, 67, 0.12);
  color: #c54d43;
  font-size: 28rpx;
  font-weight: 700;
  cursor: pointer;
}

.is-disabled {
  opacity: 0.7;
}

@media (max-width: 899px) {
  .hero-stats {
    grid-template-columns: 1fr;
  }

  .setting-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
