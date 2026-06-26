<template>
  <view class="page-shell">
    <view class="wechat-header simple">
      <text class="wechat-title">发起群聊</text>
    </view>

    <view class="panel form-panel">
      <view class="field">
        <text class="field__label">群名称</text>
        <input v-model="form.groupName" class="field__input" placeholder="例如 微友产品群" />
      </view>
      <view class="field">
        <text class="field__label">成员 ID（逗号分隔）</text>
        <input v-model="form.memberIdsText" class="field__input" placeholder="例如 10011,10012" />
      </view>
      <view class="actions-row">
        <view class="ghost-button" @click="form.memberIdsText = ''">清空成员</view>
        <view class="action-button" :class="submitting ? 'is-disabled' : ''" @click="createGroup">{{ submitting ? '创建中...' : '创建群聊' }}</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from "vue";
import { groupApi } from "@/api/modules";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const submitting = ref(false);
const form = reactive({
  groupName: "微友产品群",
  memberIdsText: "10011,10012"
});

async function createGroup() {
  if (!userStore.requireAuth() || submitting.value) {
    return;
  }
  const memberIds = form.memberIdsText.split(",").map((item) => Number(item.trim())).filter(Boolean);
  if (!form.groupName.trim() || !memberIds.length) {
    uni.showToast({ title: "请输入完整群信息", icon: "none" });
    return;
  }
  submitting.value = true;
  try {
    const data = await groupApi.create({
      groupName: form.groupName.trim(),
      memberIds
    });
    uni.showToast({ title: "建群成功", icon: "success" });
    uni.navigateTo({ url: `/pages/group/detail?groupId=${data.groupId}` });
  } catch (error) {
    uni.showToast({ title: error.message || "建群失败", icon: "none" });
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped lang="css">
.form-panel {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-top: 8rpx;
}

.wechat-header.simple {
  padding: 18rpx 6rpx 12rpx;
}

.wechat-title {
  font-size: 34rpx;
  font-weight: 700;
}

.field__label {
  display: block;
  margin-bottom: 12rpx;
  font-size: 24rpx;
  color: var(--wy-subtext);
}

.field__input {
  width: 100%;
  height: 92rpx;
  padding: 0 24rpx;
  border-radius: 18rpx;
  background: rgba(255, 255, 255, 0.96);
  border: 1rpx solid rgba(0, 0, 0, 0.05);
}

.actions-row {
  display: flex;
  gap: 12rpx;
  margin-top: 6rpx;
}

.is-disabled {
  opacity: 0.7;
}
</style>
