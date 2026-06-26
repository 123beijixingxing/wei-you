import { defineStore } from "pinia";
import { appApi, featureApi, momentApi, walletApi } from "@/api/modules";
import { useUserStore } from "@/stores/user";
import {
  mockDiscoverySections,
  mockMoments,
  mockWallet,
  mockWorkbenchSections
} from "@/mock/seed";

const BOOTSTRAP_PROMPT_KEY = "weiyou_bootstrap_prompt_signature";

function formatFen(amountFen) {
  const value = Number(amountFen || 0) / 100;
  return value.toFixed(2);
}

function formatBillAmount(item) {
  const prefix = item.incomeExpenseType === 1 ? "+" : "-";
  return `${prefix}${formatFen(item.amountFen)}`;
}

function formatBillTime(value) {
  if (!value) {
    return "";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return String(value);
  }
  const month = `${date.getMonth() + 1}`.padStart(2, "0");
  const day = `${date.getDate()}`.padStart(2, "0");
  const hour = `${date.getHours()}`.padStart(2, "0");
  const minute = `${date.getMinutes()}`.padStart(2, "0");
  return `${month}-${day} ${hour}:${minute}`;
}

function mapDiscoverySections(items = []) {
  return [
    {
      title: "发现入口",
      items: items.map((item) => ({
        code: item.featureCode,
        title: item.featureName,
        subtitle: item.routePath || "能力入口",
        route: item.routePath || "/pages/feature-hub/index"
      }))
    }
  ];
}

function mapWorkbenchSections(items = []) {
  return [
    {
      title: "超级应用能力",
      items: items.map((item) => ({
        code: item.featureCode,
        title: item.featureName,
        subtitle: item.routePath || "业务入口",
        route: item.routePath || "/pages/feature-hub/index",
        status: item.enabled ? "在线" : "维护中"
      }))
    }
  ];
}

function mapMoments(items = []) {
  return items.map((item) => ({
    id: item.momentId,
    author: {
      name: item.nickname || `用户${item.authorUserId}`,
      avatar: item.avatar || "https://weiyou.local/avatar/default.png"
    },
    content: item.content || "",
    images: Array.isArray(item.mediaUrls)
      ? item.mediaUrls
      : (item.mediaList || []).map((media) => media.url || media.coverUrl).filter(Boolean),
    comments: (item.commentList || []).map((comment) => ({
      id: comment.commentId,
      userId: comment.userId,
      userName: comment.userName,
      content: comment.content,
      replyCommentId: comment.replyCommentId,
      replyUserId: comment.replyUserId,
      replyUserName: comment.replyUserName,
      createdAt: comment.createdAt
    })),
    time: formatBillTime(item.createdAt),
    location: "朋友圈动态",
    likeCount: item.likeCount || 0,
    commentCount: item.commentCount || 0,
    liked: false
  }));
}

function buildBootstrapSignature(config, currentVersion) {
  return [
    currentVersion,
    config?.latestVersion || "",
    config?.forceUpgrade ? "1" : "0",
    config?.agreementVersion || "",
    config?.noticeText || ""
  ].join("|");
}

function buildBootstrapMessage(config, currentVersion) {
  const messageList = [];
  const latestVersion = config?.latestVersion || currentVersion;
  if (config?.forceUpgrade || latestVersion !== currentVersion) {
    messageList.push(`当前版本：${currentVersion}\n最新版本：${latestVersion}`);
  }
  if (config?.agreementVersion) {
    messageList.push(`协议版本：${config.agreementVersion}`);
  }
  if (config?.noticeText) {
    messageList.push(config.noticeText);
  }
  return messageList.join("\n\n");
}

export const useAppStore = defineStore("app", {
  state: () => ({
    discoverySections: mockDiscoverySections,
    workbenchSections: mockWorkbenchSections,
    moments: mockMoments,
    wallet: { ...mockWallet },
    walletLoading: false,
    bootstrapConfig: {
      latestVersion: "0.1.0",
      forceUpgrade: false,
      agreementVersion: "",
      noticeText: ""
    },
    bootstrapReady: false
  }),
  actions: {
    async fetchBootstrapConfig(payload = {}) {
      const clientType = payload.clientType || this.resolveClientType();
      const version = payload.version || "0.1.0";
      const result = await appApi.bootstrap(clientType, version);
      this.bootstrapConfig = {
        latestVersion: result?.latestVersion || version,
        forceUpgrade: Boolean(result?.forceUpgrade),
        agreementVersion: result?.agreementVersion || "",
        noticeText: result?.noticeText || ""
      };
      this.bootstrapReady = true;
      return this.bootstrapConfig;
    },
    async presentBootstrapPrompt(currentVersion = "0.1.0") {
      const config = this.bootstrapConfig;
      const latestVersion = config?.latestVersion || currentVersion;
      const hasPromptContent = Boolean(config?.noticeText || config?.agreementVersion || config?.forceUpgrade || latestVersion !== currentVersion);
      if (!hasPromptContent) {
        return false;
      }
      const signature = buildBootstrapSignature(config, currentVersion);
      if (uni.getStorageSync(BOOTSTRAP_PROMPT_KEY) === signature) {
        return false;
      }
      const title = config?.forceUpgrade || latestVersion !== currentVersion ? "版本更新提示" : "启动公告";
      const content = buildBootstrapMessage(config, currentVersion);
      return new Promise((resolve) => {
        uni.showModal({
          title,
          content,
          showCancel: !config?.forceUpgrade,
          confirmText: config?.forceUpgrade ? "知道了" : "我知道了",
          cancelText: "稍后",
          success: () => {
            uni.setStorageSync(BOOTSTRAP_PROMPT_KEY, signature);
            resolve(true);
          },
          fail: () => resolve(false)
        });
      });
    },
    resolveClientType() {
      const platform = uni.getSystemInfoSync?.()?.platform || "h5";
      if (platform === "ios") {
        return "iOS";
      }
      if (platform === "android") {
        return "Android";
      }
      return "H5";
    },
    async fetchWorkbenchSections() {
      const list = await featureApi.workbench();
      this.workbenchSections = mapWorkbenchSections(list || []);
      return this.workbenchSections;
    },
    async fetchDiscoverySections() {
      const list = await featureApi.discovery();
      this.discoverySections = mapDiscoverySections(list || []);
      return this.discoverySections;
    },
    async fetchMoments() {
      const page = await momentApi.timeline();
      this.moments = mapMoments(page?.list || []);
      return this.moments;
    },
    async toggleMomentLike(momentId) {
      const target = this.moments.find((item) => item.id === momentId);
      if (!target) {
        return null;
      }
      const action = target.liked ? "unlike" : "like";
      const result = await momentApi.toggleLike({
        momentId,
        action
      });
      this.moments = this.moments.map((item) => {
        if (item.id !== momentId) {
          return item;
        }
        const nextLiked = action === "like";
        return {
          ...item,
          liked: nextLiked,
          likeCount: result?.likeCount ?? Math.max(0, (item.likeCount || 0) + (nextLiked ? 1 : -1))
        };
      });
      return result;
    },
    async addMomentComment(momentId, content, replyCommentId = null) {
      const userStore = useUserStore();
      const result = await momentApi.comment({
        momentId,
        content,
        replyCommentId
      });
      this.moments = this.moments.map((item) => {
        if (item.id !== momentId) {
          return item;
        }
        return {
          ...item,
          commentCount: result?.commentCount ?? ((item.commentCount || 0) + 1),
          comments: [
            ...(item.comments || []),
            {
              id: result?.commentId || Date.now(),
              userId: userStore.profile.id,
              userName: userStore.profile.nickname,
              content,
              replyCommentId: result?.replyCommentId || replyCommentId,
              replyUserId: result?.replyUserId || null,
              replyUserName: replyCommentId
                ? (item.comments || []).find((comment) => comment.id === replyCommentId)?.userName || null
                : null,
              createdAt: result?.createdAt || new Date().toISOString()
            }
          ].slice(-5)
        };
      });
      return result;
    },
    async deleteMomentComment(momentId, commentId) {
      const result = await momentApi.deleteComment({
        momentId,
        commentId
      });
      this.moments = this.moments.map((item) => {
        if (item.id !== momentId) {
          return item;
        }
        return {
          ...item,
          commentCount: result?.commentCount ?? Math.max(0, item.commentCount - 1),
          comments: (item.comments || []).filter((comment) => comment.id !== commentId)
        };
      });
      return result;
    },
    async fetchWalletData() {
      this.walletLoading = true;
      try {
        const [overview, billPage] = await Promise.all([
          walletApi.overview(),
          walletApi.bills(1)
        ]);
        const bills = billPage?.list || [];
        const monthlyIncome = bills
          .filter((item) => item.incomeExpenseType === 1)
          .reduce((sum, item) => sum + Number(item.amountFen || 0), 0);
        const monthlySpend = bills
          .filter((item) => item.incomeExpenseType === 2)
          .reduce((sum, item) => sum + Number(item.amountFen || 0), 0);

        this.wallet = {
          ...mockWallet,
          balance: formatFen(overview?.availableBalanceFen),
          monthlySpend: formatFen(monthlySpend),
          monthlyIncome: formatFen(monthlyIncome),
          bills: bills.map((item) => ({
            title: item.billTitle || item.billType,
            amount: formatBillAmount(item),
            time: formatBillTime(item.billTime),
            status: item.incomeExpenseType === 1 ? "已入账" : "支付成功"
          }))
        };
        return this.wallet;
      } finally {
        this.walletLoading = false;
      }
    }
  }
});
