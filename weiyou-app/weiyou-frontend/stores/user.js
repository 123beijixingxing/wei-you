import { defineStore } from "pinia";
import { authApi, userApi } from "@/api/modules";
import { mockProfile } from "@/mock/seed";
import { useChatStore } from "@/stores/chat";
import {
  clearSession,
  ensureDeviceId,
  getAccessToken,
  getDeviceId,
  getRefreshToken,
  isLoggedIn,
  redirectToLogin,
  saveSession
} from "@/utils/session";

const PROFILE_STORAGE_KEY = "weiyou_profile";

function defaultProfile() {
  return {
    ...mockProfile,
    wechatId: mockProfile.wechatId || "weiyou_demo"
  };
}

function readStoredProfile() {
  return uni.getStorageSync(PROFILE_STORAGE_KEY) || defaultProfile();
}

function mapProfile(profile) {
  return {
    id: profile?.userId || mockProfile.id,
    nickname: profile?.nickname || mockProfile.nickname,
    wechatId: profile?.weiyouNo || mockProfile.wechatId,
    avatar: profile?.avatar || mockProfile.avatar,
    city: profile?.city || mockProfile.city,
    signature: profile?.signature || mockProfile.signature
  };
}

export const useUserStore = defineStore("user", {
  state: () => ({
    token: getAccessToken(),
    refreshToken: getRefreshToken(),
    profile: readStoredProfile(),
    loading: false,
    stats: [
      {
        label: "收藏",
        value: 36,
        route: "/pages/collection/index",
        desc: "查看收藏内容"
      },
      {
        label: "朋友圈",
        value: 128,
        route: "/pages/moments/index",
        desc: "查看已发布动态"
      },
      {
        label: "卡包",
        value: 7,
        route: "/pages/cards/index",
        desc: "查看卡券与会员卡"
      },
      {
        label: "表情",
        value: 24,
        route: "/pages/emoji/store",
        desc: "查看表情包"
      }
    ]
  }),
  getters: {
    isAuthenticated(state) {
      return Boolean(state.token);
    }
  },
  actions: {
    async login(mobile, password) {
      this.loading = true;
      try {
        const deviceId = ensureDeviceId();
        const session = await authApi.loginByPassword({
          mobile,
          password,
          deviceId
        });
        this.token = session.accessToken || "";
        this.refreshToken = session.refreshToken || "";
        saveSession({
          accessToken: this.token,
          refreshToken: this.refreshToken,
          deviceId
        });
        await this.fetchProfile();
        try {
          await useChatStore().ensureSocketConnected();
        } catch (error) {
          console.warn("Failed to connect socket after password login", error);
        }
        return session;
      } finally {
        this.loading = false;
      }
    },
    async loginWithSms(mobile, smsCode) {
      this.loading = true;
      try {
        const deviceId = ensureDeviceId();
        const session = await authApi.loginBySms({
          mobile,
          smsCode,
          deviceId
        });
        this.token = session.accessToken || "";
        this.refreshToken = session.refreshToken || "";
        saveSession({
          accessToken: this.token,
          refreshToken: this.refreshToken,
          deviceId
        });
        await this.fetchProfile();
        try {
          await useChatStore().ensureSocketConnected();
        } catch (error) {
          console.warn("Failed to connect socket after sms login", error);
        }
        return session;
      } finally {
        this.loading = false;
      }
    },
    async register(mobile, smsCode, password) {
      this.loading = true;
      try {
        const deviceId = ensureDeviceId();
        const session = await authApi.register({
          mobile,
          smsCode,
          password,
          deviceId
        });
        this.token = session.accessToken || "";
        this.refreshToken = session.refreshToken || "";
        saveSession({
          accessToken: this.token,
          refreshToken: this.refreshToken,
          deviceId
        });
        await this.fetchProfile();
        try {
          await useChatStore().ensureSocketConnected();
        } catch (error) {
          console.warn("Failed to connect socket after register", error);
        }
        return session;
      } finally {
        this.loading = false;
      }
    },
    async sendSmsCode(mobile, scene) {
      return authApi.sendSms({
        mobile,
        scene
      });
    },
    async fetchProfile() {
      const profile = await userApi.me();
      this.profile = mapProfile(profile);
      uni.setStorageSync(PROFILE_STORAGE_KEY, this.profile);
      return this.profile;
    },
    async updateProfile(payload) {
      await userApi.updateProfile(payload);
      await this.fetchProfile();
    },
    async updateStatus(payload) {
      await userApi.updateStatus(payload);
      await this.fetchProfile();
    },
    async restore() {
      this.token = getAccessToken();
      this.refreshToken = getRefreshToken();
      this.profile = readStoredProfile();
      if (this.token) {
        try {
          await this.fetchProfile();
        } catch (error) {
          this.logout(false);
          throw error;
        }
        try {
          await useChatStore().ensureSocketConnected();
        } catch (error) {
          console.warn("Failed to restore socket session", error);
        }
      }
    },
    logout(redirect = true) {
      const deviceId = getDeviceId();
      if (this.token && deviceId) {
        authApi.logout({ deviceId }).catch(() => {});
      }
      Promise.resolve().then(() => {
        const chatStore = useChatStore();
        chatStore.disconnectSocket();
        chatStore.clearTransientState();
        chatStore.clearAllDrafts();
        chatStore.clearAllPinnedMessages();
      }).catch(() => {});
      this.token = "";
      this.refreshToken = "";
      this.profile = defaultProfile();
      clearSession();
      uni.removeStorageSync(PROFILE_STORAGE_KEY);
      if (redirect) {
        redirectToLogin();
      }
    },
    requireAuth() {
      if (!isLoggedIn()) {
        this.logout();
        return false;
      }
      this.token = getAccessToken();
      this.refreshToken = getRefreshToken();
      return true;
    }
  }
});
