<template>
  <view class="quick-entry" @click="emit('click')">
    <view class="icon-wrap">{{ icon }}</view>
    <text class="title">{{ title }}</text>
    <text class="desc">{{ desc }}</text>
    <text v-if="status" class="status">{{ status }}</text>
  </view>
</template>

<script setup>
defineProps({
  icon: {
    type: String,
    default: "*"
  },
  title: {
    type: String,
    required: true
  },
  desc: {
    type: String,
    default: ""
  },
  status: {
    type: String,
    default: ""
  }
});

const emit = defineEmits(["click"]);
</script>

<style scoped lang="css">
.quick-entry {
  position: relative;
  min-height: 168rpx;
  padding: 22rpx;
  border-radius: 20rpx;
  background: #fff;
  border: 1rpx solid rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  cursor: pointer;
}

.icon-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64rpx;
  height: 64rpx;
  border-radius: 18rpx;
  background: rgba(31, 138, 112, 0.10);
  color: var(--wy-primary);
  font-size: 28rpx;
  font-weight: 800;
}

.title {
  display: block;
  margin-top: 18rpx;
  font-size: 27rpx;
  font-weight: 700;
}

.desc {
  display: block;
  margin-top: 8rpx;
  min-height: 56rpx;
  color: var(--wy-subtext);
  font-size: 22rpx;
  line-height: 1.5;
}

.status {
  display: inline-block;
  margin-top: 14rpx;
  padding: 6rpx 12rpx;
  border-radius: 999rpx;
  background: rgba(31, 138, 112, 0.08);
  color: var(--wy-primary);
  font-size: 20rpx;
  font-weight: 700;
}

@media (hover: hover) {
  .quick-entry:hover {
    border-color: rgba(31, 138, 112, 0.12);
    box-shadow: 0 10rpx 22rpx rgba(0, 0, 0, 0.05);
  }
}
</style>
