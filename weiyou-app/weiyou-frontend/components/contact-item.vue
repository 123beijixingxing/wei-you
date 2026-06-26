<template>
  <view class="contact-item panel" @click="emit('click')">
    <image class="avatar" :src="contact.avatar" mode="aspectFill" />
    <view class="meta">
      <view class="title-row">
        <text class="name">{{ contact.name }}</text>
        <text class="tag">{{ contact.tag }}</text>
      </view>
      <text class="alias">{{ contact.alias }}</text>
    </view>
    <view class="status" :class="contact.online ? 'online' : 'offline'" />
  </view>
</template>

<script setup>
defineProps({
  contact: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(["click"]);
</script>

<style scoped lang="css">
.contact-item {
  display: flex;
  align-items: center;
  gap: 18rpx;
  padding: 20rpx 22rpx;
  cursor: pointer;
}

.meta {
  flex: 1;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.name {
  font-size: 28rpx;
  font-weight: 700;
}

.tag {
  padding: 4rpx 10rpx;
  border-radius: 999rpx;
  background: rgba(31, 138, 112, 0.08);
  color: var(--wy-primary);
  font-size: 20rpx;
}

.alias {
  display: block;
  margin-top: 8rpx;
  color: var(--wy-subtext);
  font-size: 24rpx;
}

.status {
  width: 18rpx;
  height: 18rpx;
  border-radius: 50%;
}

.online {
  background: #3ab77c;
}

.offline {
  background: #c2c8c4;
}

@media (hover: hover) {
  .contact-item:hover {
    border-color: rgba(31, 138, 112, 0.12);
  }
}
</style>
