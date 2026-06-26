<template>
  <view class="page-shell">
    <view class="wechat-header simple">
      <text class="wechat-title">编辑资料</text>
      <text class="body-sm muted header-subtitle">保存后会同步刷新“我”页面展示内容。</text>
    </view>

    <view class="hero-card">
      <view class="profile-preview">
        <image class="avatar-lg" :src="form.avatar || userStore.profile.avatar" mode="aspectFill" />
        <view class="preview-meta">
          <text class="title-md">{{ form.nickname || '未填写昵称' }}</text>
          <text class="body-sm muted">{{ form.city || '未填写城市' }}</text>
          <text class="body-sm muted">{{ form.signature || '保存后会同步到“我”页面签名展示。' }}</text>
        </view>
      </view>
    </view>

    <view class="panel form-panel">
      <view class="field">
        <text class="field__label">昵称</text>
        <input v-model="form.nickname" class="field__input" placeholder="输入昵称" />
      </view>
      <view class="field">
        <text class="field__label">城市</text>
        <input v-model="form.city" class="field__input" placeholder="输入城市" />
      </view>
      <view class="field">
        <text class="field__label">签名</text>
        <textarea v-model="form.signature" class="field__textarea" maxlength="120" placeholder="输入个性签名" />
      </view>
      <view class="field">
        <text class="field__label">头像地址</text>
        <input v-model="form.avatar" class="field__input" placeholder="可填写图片 URL" />
      </view>
      <view class="actions-row">
        <view class="ghost-button" @click="resetForm">重置</view>
        <view class="action-button" :class="saving ? 'is-disabled' : ''" @click="saveProfile">{{ saving ? '保存中...' : '保存资料' }}</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const saving = ref(false);
const form = reactive({
  nickname: "",
  city: "",
  signature: "",
  avatar: ""
});

onShow(() => {
  hydrateForm();
});

function hydrateForm() {
  const profile = userStore.profile;
  form.nickname = profile.nickname || "";
  form.city = profile.city || "";
  form.signature = profile.signature || "";
  form.avatar = profile.avatar || "";
}

function resetForm() {
  hydrateForm();
}

async function saveProfile() {
  if (!userStore.requireAuth() || saving.value) {
    return;
  }
  saving.value = true;
  try {
    await userStore.updateProfile({
      nickname: form.nickname.trim(),
      city: form.city.trim(),
      signature: form.signature.trim(),
      avatar: form.avatar.trim()
    });
    uni.showToast({
      title: "资料已保存",
      icon: "success"
    });
  } catch (error) {
    uni.showToast({
      title: error.message || "保存失败",
      icon: "none"
    });
  } finally {
    saving.value = false;
  }
}
</script>

<style scoped lang="css">
.form-panel {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-top: 12rpx;
}

.wechat-header.simple {
  padding: 18rpx 6rpx 12rpx;
}

.wechat-title {
  font-size: 34rpx;
  font-weight: 700;
}

.header-subtitle {
  display: block;
  margin-top: 8rpx;
}

.profile-preview {
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.preview-meta {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
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

.field__textarea {
  width: 100%;
  min-height: 180rpx;
  padding: 20rpx 24rpx;
  border-radius: 18rpx;
  background: rgba(255, 255, 255, 0.96);
  border: 1rpx solid rgba(0, 0, 0, 0.05);
  line-height: 1.6;
}

.actions-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.actions-row .ghost-button,
.actions-row .action-button {
  cursor: pointer;
}

.is-disabled {
  opacity: 0.7;
}

@media (max-width: 899px) {
  .profile-preview {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
