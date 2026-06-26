<template>
  <view class="chat-item panel" @click="openConversation">
    <image class="avatar" :src="conversation.avatar" mode="aspectFill" />
    <view class="meta">
      <view class="row top-row">
        <text class="title">{{ conversation.title }}</text>
        <text class="time">{{ conversation.time }}</text>
      </view>
      <view class="row bottom-row">
        <text class="preview">{{ conversation.preview }}</text>
        <view class="right-state">
          <text v-if="conversation.muted" class="muted mini">免打扰</text>
          <text v-if="conversation.pinned" class="pin">置顶</text>
          <text v-if="conversation.unread" class="badge">{{ conversation.unread }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  conversation: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(["open"]);

function openConversation() {
  emit("open", props.conversation);
}
</script>

<style scoped lang="css">
.chat-item {
  display: flex;
  gap: 18rpx;
  padding: 20rpx 22rpx;
  cursor: pointer;
}

.meta {
  flex: 1;
  min-width: 0;
}

.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.top-row {
  margin-bottom: 10rpx;
}

.title {
  font-size: 28rpx;
  font-weight: 700;
}

.time,
.mini {
  font-size: 22rpx;
  color: var(--wy-subtext);
}

.preview {
  flex: 1;
  font-size: 24rpx;
  color: var(--wy-subtext);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.right-state {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-left: 20rpx;
}

.pin {
  padding: 4rpx 10rpx;
  border-radius: 999rpx;
  background: rgba(209, 138, 74, 0.12);
  color: var(--wy-accent);
  font-size: 20rpx;
  font-weight: 700;
}

@media (hover: hover) {
  .chat-item:hover {
    border-color: rgba(31, 138, 112, 0.12);
  }
}
</style>
