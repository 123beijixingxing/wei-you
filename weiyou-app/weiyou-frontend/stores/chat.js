import { defineStore } from "pinia";
import { chatApi, collectionApi } from "@/api/modules";
import { useUserStore } from "@/stores/user";
import { chatSocket } from "@/utils/chat-socket";
import { ensureDeviceId, getAccessToken } from "@/utils/session";

const CHAT_TRANSIENT_KEY = "weiyou_chat_transient_state";
const CHAT_DRAFT_KEY = "weiyou_chat_draft_state";
const CHAT_PIN_KEY = "weiyou_chat_pin_state";
const MESSAGE_ACK_TIMEOUT = 8000;
const pendingAckTimers = new Map();

function formatDateTime(value) {
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

function formatFileSize(size) {
  const value = Number(size || 0);
  if (!value) {
    return "";
  }
  if (value < 1024) {
    return `${value}B`;
  }
  if (value < 1024 * 1024) {
    return `${(value / 1024).toFixed(1)}KB`;
  }
  return `${(value / (1024 * 1024)).toFixed(1)}MB`;
}

function formatVoiceDuration(durationMs) {
  const ms = Number(durationMs || 0);
  const totalSeconds = Math.max(1, Math.round(ms / 1000) || 1);
  if (totalSeconds < 60) {
    return `${totalSeconds}"`;
  }
  const minute = Math.floor(totalSeconds / 60);
  const second = `${totalSeconds % 60}`.padStart(2, "0");
  return `${minute}:${second}`;
}

function parseMessageContent(content) {
  if (!content) {
    return {};
  }
  if (typeof content.raw === "string") {
    try {
      return JSON.parse(content.raw);
    } catch (error) {
      return {
        text: content.raw
      };
    }
  }
  if (typeof content === "object") {
    return content;
  }
  return {
    text: String(content)
  };
}

function legacyMsgType(type) {
  if (type === "image") {
    return 2;
  }
  if (type === "file") {
    return 3;
  }
  if (type === "voice") {
    return 4;
  }
  if (type === "video") {
    return 5;
  }
  if (type === "location") {
    return 6;
  }
  if (type === "card") {
    return 7;
  }
  return 1;
}

function buildStoredBody(item) {
  if (item.body && typeof item.body === "object") {
    return item.body;
  }
  if ((item.msgType || legacyMsgType(item.type)) === 2) {
    return {
      imageUrl: item.imageUrl || item.content || "",
      coverUrl: item.imageUrl || item.content || ""
    };
  }
  if ((item.msgType || legacyMsgType(item.type)) === 3) {
    return {
      fileUrl: item.fileUrl || "",
      fileName: item.fileName || item.content || "未命名文件",
      fileSize: item.fileSize || 0,
      ext: item.ext || ""
    };
  }
  if ((item.msgType || legacyMsgType(item.type)) === 4) {
    return {
      voiceUrl: item.voiceUrl || "",
      durationMs: item.durationMs || 0,
      durationSec: item.durationSec || 0,
      fileName: item.fileName || "voice.mp3"
    };
  }
  if ((item.msgType || legacyMsgType(item.type)) === 5) {
    return {
      videoUrl: item.videoUrl || "",
      coverUrl: item.coverUrl || "",
      durationMs: item.durationMs || 0,
      durationSec: item.durationSec || 0,
      width: item.width || 0,
      height: item.height || 0,
      fileName: item.fileName || "video.mp4",
      fileSize: item.fileSize || 0
    };
  }
  if ((item.msgType || legacyMsgType(item.type)) === 6) {
    return {
      locationName: item.locationName || item.content || "位置消息",
      address: item.address || "",
      latitude: item.latitude || 0,
      longitude: item.longitude || 0
    };
  }
  if ((item.msgType || legacyMsgType(item.type)) === 7) {
    return {
      cardUserId: item.cardUserId || item.userId || 0,
      cardNickname: item.cardNickname || item.content || "联系人名片",
      cardAvatar: item.cardAvatar || "",
      weiyouNo: item.weiyouNo || "",
      city: item.city || "",
      signature: item.signature || ""
    };
  }
  return {
    text: item.content || ""
  };
}

function resolveMessagePresentation(msgType, rawContent) {
  const body = parseMessageContent(rawContent);
  const replyMessageId = Number(body.replyMessageId || body.quoteMessageId || 0) || null;
  const replyPreviewText = body.replyPreviewText || body.quotePreviewText || "";
  const replySenderName = body.replySenderName || body.quoteSenderName || "";
  const replyType = body.replyType || body.quoteType || "";
  if (body.recalled) {
    return {
      msgType: 1,
      type: "recalled",
      content: body.text || "消息已撤回",
      previewText: "[消息已撤回]",
      replyMessageId,
      replyPreviewText,
      replySenderName,
      replyType,
      body: {
        ...body,
        text: body.text || "消息已撤回",
        recalled: true
      }
    };
  }
  if (msgType === 2) {
    const imageUrl = body.imageUrl || body.url || body.coverUrl || body.fileUrl || body.localPath || "";
    return {
      msgType: 2,
      type: "image",
      content: "[图片]",
      previewText: "[图片]",
      replyMessageId,
      replyPreviewText,
      replySenderName,
      replyType,
      imageUrl,
      body: {
        ...body,
        imageUrl,
        coverUrl: body.coverUrl || imageUrl
      }
    };
  }
  if (msgType === 3) {
    const fileName = body.fileName || body.name || "未命名文件";
    const fileSize = Number(body.fileSize || body.size || 0);
    const fileUrl = body.fileUrl || body.url || body.localPath || "";
    return {
      msgType: 3,
      type: "file",
      content: fileName,
      previewText: `[文件] ${fileName}`,
      replyMessageId,
      replyPreviewText,
      replySenderName,
      replyType,
      fileName,
      fileSize,
      fileSizeText: formatFileSize(fileSize),
      fileUrl,
      body: {
        ...body,
        fileName,
        fileSize,
        fileUrl
      }
    };
  }
  if (msgType === 4) {
    const durationMs = Number(body.durationMs || (Number(body.durationSec || 0) * 1000) || 0);
    const voiceUrl = body.voiceUrl || body.url || body.fileUrl || body.localPath || "";
    const durationText = formatVoiceDuration(durationMs);
    return {
      msgType: 4,
      type: "voice",
      content: "[语音]",
      previewText: `[语音] ${durationText}`,
      replyMessageId,
      replyPreviewText,
      replySenderName,
      replyType,
      voiceUrl,
      durationMs,
      durationSec: Math.max(1, Math.round(durationMs / 1000) || 1),
      durationText,
      body: {
        ...body,
        voiceUrl,
        durationMs,
        durationSec: Math.max(1, Math.round(durationMs / 1000) || 1),
        fileName: body.fileName || "voice.mp3"
      }
    };
  }
  if (msgType === 5) {
    const durationMs = Number(body.durationMs || (Number(body.durationSec || 0) * 1000) || 0);
    const videoUrl = body.videoUrl || body.url || body.fileUrl || body.localPath || "";
    const durationText = formatVoiceDuration(durationMs);
    const fileSize = Number(body.fileSize || body.size || 0);
    return {
      msgType: 5,
      type: "video",
      content: "[视频]",
      previewText: `[视频] ${durationText}`,
      replyMessageId,
      replyPreviewText,
      replySenderName,
      replyType,
      videoUrl,
      coverUrl: body.coverUrl || body.poster || "",
      durationMs,
      durationSec: Math.max(1, Math.round(durationMs / 1000) || 1),
      durationText,
      width: Number(body.width || 0),
      height: Number(body.height || 0),
      fileName: body.fileName || "video.mp4",
      fileSize,
      fileSizeText: formatFileSize(fileSize),
      body: {
        ...body,
        videoUrl,
        coverUrl: body.coverUrl || body.poster || "",
        durationMs,
        durationSec: Math.max(1, Math.round(durationMs / 1000) || 1),
        width: Number(body.width || 0),
        height: Number(body.height || 0),
        fileName: body.fileName || "video.mp4",
        fileSize
      }
    };
  }
  if (msgType === 6) {
    const locationName = body.locationName || body.name || body.title || "位置消息";
    return {
      msgType: 6,
      type: "location",
      content: locationName,
      previewText: `[位置] ${locationName}`,
      replyMessageId,
      replyPreviewText,
      replySenderName,
      replyType,
      locationName,
      address: body.address || "",
      latitude: Number(body.latitude || 0),
      longitude: Number(body.longitude || 0),
      body: {
        ...body,
        locationName,
        address: body.address || "",
        latitude: Number(body.latitude || 0),
        longitude: Number(body.longitude || 0)
      }
    };
  }
  if (msgType === 7) {
    const cardNickname = body.cardNickname || body.nickname || body.name || "联系人名片";
    return {
      msgType: 7,
      type: "card",
      content: cardNickname,
      previewText: `[名片] ${cardNickname}`,
      replyMessageId,
      replyPreviewText,
      replySenderName,
      replyType,
      cardUserId: Number(body.cardUserId || body.userId || 0),
      cardNickname,
      cardAvatar: body.cardAvatar || body.avatar || "",
      weiyouNo: body.weiyouNo || "",
      city: body.city || "",
      signature: body.signature || "",
      body: {
        ...body,
        cardUserId: Number(body.cardUserId || body.userId || 0),
        cardNickname,
        cardAvatar: body.cardAvatar || body.avatar || "",
        weiyouNo: body.weiyouNo || "",
        city: body.city || "",
        signature: body.signature || ""
      }
    };
  }
  const text = typeof body.text === "string" ? body.text : "[消息]";
  return {
    msgType: 1,
    type: "text",
    content: text,
    previewText: text,
    replyMessageId,
    replyPreviewText,
    replySenderName,
    replyType,
    body: {
      ...body,
      text
    }
  };
}

function normalizeMessageRecord(record, currentUserId, selfName) {
  const presentation = resolveMessagePresentation(record.msgType, record.content);
  return {
    id: record.messageId,
    clientMsgId: record.clientMsgId,
    conversationId: Number(record.conversationId),
    senderId: record.senderUserId,
    senderName: record.senderUserId === currentUserId ? selfName : `用户${record.senderUserId}`,
    time: formatDateTime(record.sendTime),
    self: record.senderUserId === currentUserId,
    pending: false,
    failed: false,
    queued: false,
    errorMessage: "",
    ...presentation
  };
}

function cloneMessageBody(body = {}) {
  return JSON.parse(JSON.stringify(body || {}));
}

function buildReplyBody(replyMeta) {
  if (!replyMeta || !replyMeta.replyMessageId) {
    return {};
  }
  return {
    replyMessageId: Number(replyMeta.replyMessageId),
    replyPreviewText: replyMeta.replyPreviewText || "",
    replySenderName: replyMeta.replySenderName || "",
    replyType: replyMeta.replyType || "text"
  };
}

function buildCollectionPayload(message) {
  if (message.type === "image") {
    return {
      type: "image",
      title: "聊天图片",
      cover: message.imageUrl || "",
      summary: message.senderName || "图片消息"
    };
  }
  if (message.type === "file") {
    return {
      type: "file",
      title: message.fileName || "文件消息",
      cover: "",
      summary: message.fileSizeText || "聊天文件"
    };
  }
  if (message.type === "voice") {
    return {
      type: "audio",
      title: "语音消息",
      cover: "",
      summary: message.durationText || "聊天语音"
    };
  }
  if (message.type === "video") {
    return {
      type: "video",
      title: "视频消息",
      cover: message.coverUrl || "",
      summary: message.durationText || "聊天视频"
    };
  }
  if (message.type === "location") {
    return {
      type: "location",
      title: message.locationName || "位置消息",
      cover: "",
      summary: message.address || "聊天位置"
    };
  }
  if (message.type === "card") {
    return {
      type: "card",
      title: message.cardNickname || "联系人名片",
      cover: message.cardAvatar || "",
      summary: message.weiyouNo || message.city || "聊天名片"
    };
  }
  return {
    type: "note",
    title: (message.content || "聊天消息").slice(0, 24),
    cover: "",
    summary: message.content || "聊天消息"
  };
}

function mergeServerAndLocalMessages(serverList = [], localList = []) {
  const merged = [...serverList];
  localList
    .filter((item) => item.pending || item.failed || item.queued)
    .forEach((item) => {
      const exists = merged.some((serverItem) => {
        return serverItem.clientMsgId === item.clientMsgId || serverItem.id === item.id;
      });
      if (!exists) {
        merged.push(item);
      }
    });
  return merged;
}

function readTransientState() {
  const stored = uni.getStorageSync(CHAT_TRANSIENT_KEY);
  if (!stored || typeof stored !== "object") {
    return {
      messages: {},
      outgoingQueue: []
    };
  }
  const messages = {};
  const outgoingQueue = Array.isArray(stored.outgoingQueue)
    ? stored.outgoingQueue.map((item) => ({
        conversationId: Number(item.conversationId),
        clientMsgId: item.clientMsgId
      })).filter((item) => item.conversationId && item.clientMsgId)
    : [];

  Object.entries(stored.messages || {}).forEach(([conversationId, list]) => {
    if (!Array.isArray(list) || !list.length) {
      return;
    }
    messages[conversationId] = list.map((item) => {
      const wasPending = Boolean(item.pending);
      const presentation = resolveMessagePresentation(item.msgType || legacyMsgType(item.type), buildStoredBody(item));
      return {
        id: item.id || item.clientMsgId,
        clientMsgId: item.clientMsgId,
        conversationId: Number(item.conversationId || conversationId),
        senderId: item.senderId,
        senderName: item.senderName,
        time: item.time || formatDateTime(new Date().toISOString()),
        self: item.self !== false,
        pending: false,
        failed: wasPending ? true : Boolean(item.failed),
        queued: wasPending ? true : Boolean(item.queued),
        errorMessage: wasPending ? "等待重发" : (item.errorMessage || ""),
        ...presentation
      };
    });
  });

  Object.entries(messages).forEach(([conversationId, list]) => {
    list.forEach((item) => {
      if ((item.queued || item.failed) && !outgoingQueue.some((queueItem) => queueItem.clientMsgId === item.clientMsgId)) {
        outgoingQueue.push({
          conversationId: Number(conversationId),
          clientMsgId: item.clientMsgId
        });
      }
    });
  });

  return {
    messages,
    outgoingQueue
  };
}

function writeTransientState(payload) {
  uni.setStorageSync(CHAT_TRANSIENT_KEY, payload);
}

function clearTransientStateStorage() {
  uni.removeStorageSync(CHAT_TRANSIENT_KEY);
}

function extractTransientMessages(messages = {}) {
  const next = {};
  Object.entries(messages).forEach(([conversationId, list]) => {
    const transientList = (list || [])
      .filter((item) => item.pending || item.failed || item.queued)
      .map((item) => ({
        id: item.id,
        clientMsgId: item.clientMsgId,
        conversationId: Number(item.conversationId || conversationId),
        senderId: item.senderId,
        senderName: item.senderName,
        content: item.content,
        type: item.type,
        msgType: item.msgType || legacyMsgType(item.type),
        previewText: item.previewText || item.content,
        body: item.body || buildStoredBody(item),
        imageUrl: item.imageUrl || "",
        fileName: item.fileName || "",
        fileSize: item.fileSize || 0,
        fileUrl: item.fileUrl || "",
        voiceUrl: item.voiceUrl || "",
        durationMs: item.durationMs || 0,
        durationSec: item.durationSec || 0,
        durationText: item.durationText || "",
        videoUrl: item.videoUrl || "",
        coverUrl: item.coverUrl || "",
        width: item.width || 0,
        height: item.height || 0,
        locationName: item.locationName || "",
        address: item.address || "",
        latitude: item.latitude || 0,
        longitude: item.longitude || 0,
        cardUserId: item.cardUserId || 0,
        cardNickname: item.cardNickname || "",
        cardAvatar: item.cardAvatar || "",
        weiyouNo: item.weiyouNo || "",
        city: item.city || "",
        signature: item.signature || "",
        replyMessageId: item.replyMessageId || null,
        replyPreviewText: item.replyPreviewText || "",
        replySenderName: item.replySenderName || "",
        replyType: item.replyType || "",
        time: item.time,
        self: item.self,
        pending: Boolean(item.pending),
        failed: Boolean(item.failed),
        queued: Boolean(item.queued),
        errorMessage: item.errorMessage || ""
      }));
    if (transientList.length) {
      next[conversationId] = transientList;
    }
  });
  return next;
}

function readDraftState() {
  const stored = uni.getStorageSync(CHAT_DRAFT_KEY);
  if (!stored || typeof stored !== "object") {
    return {};
  }
  return Object.fromEntries(
    Object.entries(stored)
      .map(([conversationId, value]) => [conversationId, String(value || "")])
      .filter(([, value]) => value.trim())
  );
}

function writeDraftState(payload) {
  uni.setStorageSync(CHAT_DRAFT_KEY, payload);
}

function clearDraftStateStorage() {
  uni.removeStorageSync(CHAT_DRAFT_KEY);
}

function readPinState() {
  const stored = uni.getStorageSync(CHAT_PIN_KEY);
  if (!stored || typeof stored !== "object") {
    return {};
  }
  return stored;
}

function writePinState(payload) {
  uni.setStorageSync(CHAT_PIN_KEY, payload);
}

function clearPinStateStorage() {
  uni.removeStorageSync(CHAT_PIN_KEY);
}

export const useChatStore = defineStore("chat", {
  state: () => {
    const transientState = readTransientState();
    const draftState = readDraftState();
    const pinState = readPinState();
    return {
      conversations: [],
      messages: transientState.messages,
      loadingConversations: false,
      loadingMessages: false,
      socketConnected: false,
      socketConnecting: false,
      socketReconnecting: false,
      socketReconnectAttempt: 0,
      socketSessionId: "",
      lastHeartbeatAt: "",
      lastSocketError: "",
      activeConversationId: null,
      socketListenersReady: false,
      outgoingQueue: transientState.outgoingQueue,
      drafts: draftState,
      pinnedMessages: pinState
    };
  },
  getters: {
    unreadCount(state) {
      return state.conversations.reduce((sum, item) => sum + (item.unread || 0), 0);
    },
    socketStatusText(state) {
      if (state.socketConnected) {
        return "实时已连接";
      }
      if (state.socketReconnecting) {
        return `自动重连 ${state.socketReconnectAttempt || 1}`;
      }
      if (state.socketConnecting) {
        return "连接中";
      }
      if (state.lastSocketError) {
        return "连接断开";
      }
      return "未连接";
    }
  },
  actions: {
    schedulePendingTimeout(conversationId, clientMsgId) {
      this.clearPendingTimeout(clientMsgId);
      const timer = setTimeout(() => {
        this.markMessageFailed(Number(conversationId), clientMsgId, "发送超时，点击重试", true);
      }, MESSAGE_ACK_TIMEOUT);
      pendingAckTimers.set(clientMsgId, timer);
    },
    clearPendingTimeout(clientMsgId) {
      const timer = pendingAckTimers.get(clientMsgId);
      if (timer) {
        clearTimeout(timer);
        pendingAckTimers.delete(clientMsgId);
      }
    },
    clearAllPendingTimeouts() {
      pendingAckTimers.forEach((timer) => clearTimeout(timer));
      pendingAckTimers.clear();
    },
    syncTransientState() {
      const messages = extractTransientMessages(this.messages);
      const validKeys = new Set(
        Object.values(messages)
          .flat()
          .map((item) => item.clientMsgId)
          .filter(Boolean)
      );
      this.outgoingQueue = this.outgoingQueue.filter((item) => validKeys.has(item.clientMsgId));
      if (!Object.keys(messages).length && !this.outgoingQueue.length) {
        clearTransientStateStorage();
        return;
      }
      writeTransientState({
        messages,
        outgoingQueue: this.outgoingQueue
      });
    },
    clearTransientState() {
      this.clearAllPendingTimeouts();
      this.messages = {};
      this.outgoingQueue = [];
      clearTransientStateStorage();
    },
    syncDraftState() {
      const nextDrafts = Object.fromEntries(
        Object.entries(this.drafts).filter(([, value]) => String(value || "").trim())
      );
      this.drafts = nextDrafts;
      if (!Object.keys(nextDrafts).length) {
        clearDraftStateStorage();
        return;
      }
      writeDraftState(nextDrafts);
    },
    getDraft(conversationId) {
      return this.drafts[String(conversationId)] || "";
    },
    setDraft(conversationId, value) {
      this.drafts = {
        ...this.drafts,
        [String(conversationId)]: String(value || "")
      };
      this.syncDraftState();
    },
    clearDraft(conversationId) {
      const nextDrafts = { ...this.drafts };
      delete nextDrafts[String(conversationId)];
      this.drafts = nextDrafts;
      this.syncDraftState();
    },
    clearAllDrafts() {
      this.drafts = {};
      clearDraftStateStorage();
    },
    syncPinState() {
      if (!Object.keys(this.pinnedMessages || {}).length) {
        clearPinStateStorage();
        return;
      }
      writePinState(this.pinnedMessages);
    },
    getPinnedMessageKey(conversationId) {
      return this.pinnedMessages[String(conversationId)] || "";
    },
    pinMessage(conversationId, message) {
      const key = String(message.clientMsgId || message.id || "");
      this.pinnedMessages = {
        ...this.pinnedMessages,
        [String(conversationId)]: key
      };
      this.syncPinState();
    },
    clearPinnedMessage(conversationId) {
      const next = { ...this.pinnedMessages };
      delete next[String(conversationId)];
      this.pinnedMessages = next;
      this.syncPinState();
    },
    clearAllPinnedMessages() {
      this.pinnedMessages = {};
      clearPinStateStorage();
    },
    setupSocketListeners() {
      if (this.socketListenersReady) {
        return;
      }
      chatSocket.subscribe((payload) => this.handleSocketEvent(payload));
      this.socketListenersReady = true;
    },
    applyTransientConversationSnapshots() {
      Object.entries(this.messages).forEach(([conversationId, list]) => {
        const transientList = (list || []).filter((item) => item.pending || item.failed || item.queued);
        if (!transientList.length) {
          return;
        }
        const lastItem = transientList[transientList.length - 1];
        this.updateConversationSnapshot(Number(conversationId), lastItem.previewText || lastItem.content, lastItem.time, true);
      });
    },
    async ensureSocketConnected() {
      this.setupSocketListeners();
      if (this.socketConnected) {
        return true;
      }
      const token = getAccessToken();
      const deviceId = ensureDeviceId();
      if (!token) {
        return false;
      }
      this.socketConnecting = true;
      this.lastSocketError = "";
      try {
        await chatSocket.connect({ token, deviceId });
        this.socketConnected = true;
        return true;
      } finally {
        if (!this.socketConnected) {
          this.socketConnecting = false;
        }
      }
    },
    disconnectSocket() {
      chatSocket.disconnect();
      this.clearAllPendingTimeouts();
      this.socketConnected = false;
      this.socketConnecting = false;
      this.socketReconnecting = false;
      this.socketReconnectAttempt = 0;
      this.socketSessionId = "";
      this.lastHeartbeatAt = "";
      this.lastSocketError = "";
    },
    async fetchConversations() {
      this.loadingConversations = true;
      try {
        const result = await chatApi.conversations();
        this.conversations = (result?.list || []).map((item) => ({
          id: Number(item.conversationId),
          title: item.title || `会话-${item.conversationId}`,
          avatar: item.avatar || "https://weiyou.local/avatar/default.png",
          preview: item.lastMessageDigest || "暂无消息",
          time: formatDateTime(item.lastMessageTime),
          unread: item.unreadCount || 0,
          muted: Boolean(item.mute),
          pinned: Boolean(item.top),
          type: item.conversationType === 2 ? "group" : "single"
        }));
        this.applyTransientConversationSnapshots();
        return this.conversations;
      } finally {
        this.loadingConversations = false;
      }
    },
    openConversation(id) {
      const normalizedId = Number(id);
      this.activeConversationId = normalizedId;
      this.conversations = this.conversations.map((item) => {
        if (Number(item.id) === normalizedId) {
          return {
            ...item,
            unread: 0
          };
        }
        return item;
      });
    },
    clearActiveConversation() {
      this.activeConversationId = null;
    },
    async fetchMessages(conversationId) {
      this.loadingMessages = true;
      try {
        const normalizedId = Number(conversationId);
        const userStore = useUserStore();
        const currentUserId = userStore.profile.id;
        const localMessages = this.messages[normalizedId] || [];
        const result = await chatApi.messages(normalizedId);
        const serverList = (result?.list || []).map((item) => normalizeMessageRecord({
          ...item,
          conversationId: normalizedId
        }, currentUserId, userStore.profile.nickname));
        const list = mergeServerAndLocalMessages(serverList, localMessages);
        this.messages = {
          ...this.messages,
          [normalizedId]: list
        };
        this.syncTransientState();
        const lastServerMessage = serverList[serverList.length - 1];
        if (lastServerMessage) {
          await chatApi.markRead({
            conversationId: normalizedId,
            messageId: lastServerMessage.id
          });
          this.openConversation(normalizedId);
        }
        try {
          await this.ensureSocketConnected();
        } catch (error) {
          console.warn("Failed to keep socket connected while fetching messages", error);
        }
        return list;
      } finally {
        this.loadingMessages = false;
      }
    },
    createOutgoingMessage(conversationId, meta) {
      const normalizedId = Number(conversationId);
      const userStore = useUserStore();
      const requestId = `cmsg-${Date.now()}`;

      return {
        id: requestId,
        clientMsgId: requestId,
        conversationId: normalizedId,
        senderId: userStore.profile.id,
        senderName: userStore.profile.nickname,
        ...meta,
        time: formatDateTime(new Date().toISOString()),
        self: true,
        pending: true,
        failed: false,
        queued: false,
        errorMessage: ""
      };
    },
    async sendMessage(conversationId, content, replyMeta = null) {
      const normalizedId = Number(conversationId);
      const optimisticMessage = this.createOutgoingMessage(conversationId, {
        ...resolveMessagePresentation(1, {
          text: content,
          ...buildReplyBody(replyMeta)
        })
      });
      this.pushMessage(normalizedId, optimisticMessage);
      this.updateConversationSnapshot(normalizedId, optimisticMessage.previewText, optimisticMessage.time, true);
      await this.deliverOutgoingMessage(optimisticMessage);
      return optimisticMessage;
    },
    async sendImageMessage(conversationId, imageMeta, replyMeta = null) {
      const body = {
        imageUrl: imageMeta.imageUrl || imageMeta.url,
        coverUrl: imageMeta.coverUrl || imageMeta.imageUrl || imageMeta.url,
        width: imageMeta.width || 0,
        height: imageMeta.height || 0,
        fileName: imageMeta.fileName || "image.jpg",
        ...buildReplyBody(replyMeta)
      };
      const optimisticMessage = this.createOutgoingMessage(conversationId, resolveMessagePresentation(2, body));
      this.pushMessage(Number(conversationId), optimisticMessage);
      this.updateConversationSnapshot(Number(conversationId), optimisticMessage.previewText, optimisticMessage.time, true);
      await this.deliverOutgoingMessage(optimisticMessage);
      return optimisticMessage;
    },
    async sendFileMessage(conversationId, fileMeta, replyMeta = null) {
      const body = {
        fileUrl: fileMeta.fileUrl || fileMeta.url || fileMeta.path,
        fileName: fileMeta.fileName || fileMeta.name || "未命名文件",
        fileSize: Number(fileMeta.fileSize || fileMeta.size || 0),
        ext: fileMeta.ext || "",
        ...buildReplyBody(replyMeta)
      };
      const optimisticMessage = this.createOutgoingMessage(conversationId, resolveMessagePresentation(3, body));
      this.pushMessage(Number(conversationId), optimisticMessage);
      this.updateConversationSnapshot(Number(conversationId), optimisticMessage.previewText, optimisticMessage.time, true);
      await this.deliverOutgoingMessage(optimisticMessage);
      return optimisticMessage;
    },
    async sendVoiceMessage(conversationId, voiceMeta, replyMeta = null) {
      const durationMs = Number(voiceMeta.durationMs || (Number(voiceMeta.durationSec || 0) * 1000) || 0);
      const body = {
        voiceUrl: voiceMeta.voiceUrl || voiceMeta.url || voiceMeta.path,
        durationMs,
        durationSec: Math.max(1, Math.round(durationMs / 1000) || 1),
        fileName: voiceMeta.fileName || "voice.mp3",
        ...buildReplyBody(replyMeta)
      };
      const optimisticMessage = this.createOutgoingMessage(conversationId, resolveMessagePresentation(4, body));
      this.pushMessage(Number(conversationId), optimisticMessage);
      this.updateConversationSnapshot(Number(conversationId), optimisticMessage.previewText, optimisticMessage.time, true);
      await this.deliverOutgoingMessage(optimisticMessage);
      return optimisticMessage;
    },
    async sendVideoMessage(conversationId, videoMeta, replyMeta = null) {
      const durationMs = Number(videoMeta.durationMs || (Number(videoMeta.durationSec || 0) * 1000) || 0);
      const body = {
        videoUrl: videoMeta.videoUrl || videoMeta.url || videoMeta.path,
        coverUrl: videoMeta.coverUrl || videoMeta.poster || "",
        durationMs,
        durationSec: Math.max(1, Math.round(durationMs / 1000) || 1),
        width: Number(videoMeta.width || 0),
        height: Number(videoMeta.height || 0),
        fileName: videoMeta.fileName || "video.mp4",
        fileSize: Number(videoMeta.fileSize || 0),
        ...buildReplyBody(replyMeta)
      };
      const optimisticMessage = this.createOutgoingMessage(conversationId, resolveMessagePresentation(5, body));
      this.pushMessage(Number(conversationId), optimisticMessage);
      this.updateConversationSnapshot(Number(conversationId), optimisticMessage.previewText, optimisticMessage.time, true);
      await this.deliverOutgoingMessage(optimisticMessage);
      return optimisticMessage;
    },
    async sendLocationMessage(conversationId, locationMeta, replyMeta = null) {
      const body = {
        locationName: locationMeta.locationName || locationMeta.name || "位置消息",
        address: locationMeta.address || "",
        latitude: Number(locationMeta.latitude || 0),
        longitude: Number(locationMeta.longitude || 0),
        ...buildReplyBody(replyMeta)
      };
      const optimisticMessage = this.createOutgoingMessage(conversationId, resolveMessagePresentation(6, body));
      this.pushMessage(Number(conversationId), optimisticMessage);
      this.updateConversationSnapshot(Number(conversationId), optimisticMessage.previewText, optimisticMessage.time, true);
      await this.deliverOutgoingMessage(optimisticMessage);
      return optimisticMessage;
    },
    async sendCardMessage(conversationId, cardMeta, replyMeta = null) {
      const body = {
        cardUserId: Number(cardMeta.cardUserId || cardMeta.userId || 0),
        cardNickname: cardMeta.cardNickname || cardMeta.nickname || "联系人名片",
        cardAvatar: cardMeta.cardAvatar || cardMeta.avatar || "",
        weiyouNo: cardMeta.weiyouNo || "",
        city: cardMeta.city || "",
        signature: cardMeta.signature || "",
        ...buildReplyBody(replyMeta)
      };
      const optimisticMessage = this.createOutgoingMessage(conversationId, resolveMessagePresentation(7, body));
      this.pushMessage(Number(conversationId), optimisticMessage);
      this.updateConversationSnapshot(Number(conversationId), optimisticMessage.previewText, optimisticMessage.time, true);
      await this.deliverOutgoingMessage(optimisticMessage);
      return optimisticMessage;
    },
    async forwardMessage(targetConversationId, sourceMessage) {
      const meta = resolveMessagePresentation(sourceMessage.msgType || legacyMsgType(sourceMessage.type), cloneMessageBody(sourceMessage.body || buildStoredBody(sourceMessage)));
      const optimisticMessage = this.createOutgoingMessage(targetConversationId, meta);
      this.pushMessage(Number(targetConversationId), optimisticMessage);
      this.updateConversationSnapshot(Number(targetConversationId), optimisticMessage.previewText, optimisticMessage.time, true);
      await this.deliverOutgoingMessage(optimisticMessage);
      return optimisticMessage;
    },
    async revokeMessage(conversationId, messageId) {
      const normalizedId = Number(conversationId);
      const current = this.messages[normalizedId] || [];
      const target = current.find((item) => String(item.id) === String(messageId) || item.clientMsgId === messageId);
      if (!target) {
        throw new Error("message not found");
      }
      if (!target.self) {
        throw new Error("只能撤回自己发送的消息");
      }
      await chatApi.revokeMessage({
        conversationId: normalizedId,
        messageId: Number(target.id)
      });
      this.applyLocalRevoke(normalizedId, target.clientMsgId || String(target.id));
      return true;
    },
    async collectMessage(message) {
      return collectionApi.createItem(buildCollectionPayload(message));
    },
    applyLocalRevoke(conversationId, messageKey) {
      const current = this.messages[conversationId] || [];
      const next = current.map((item) => {
        if (String(item.id) === String(messageKey) || item.clientMsgId === messageKey) {
          return {
            ...item,
            ...resolveMessagePresentation(1, {
              text: "消息已撤回",
              recalled: true
            }),
            pending: false,
            failed: false,
            queued: false,
            errorMessage: ""
          };
        }
        return item;
      });
      this.messages = {
        ...this.messages,
        [conversationId]: next
      };
      const lastMessage = next[next.length - 1];
      if (lastMessage) {
        this.updateConversationSnapshot(conversationId, lastMessage.previewText || lastMessage.content, lastMessage.time, true);
      }
      this.syncTransientState();
    },
    async retryMessage(conversationId, clientMsgId) {
      const normalizedId = Number(conversationId);
      const current = this.messages[normalizedId] || [];
      const target = current.find((item) => item.clientMsgId === clientMsgId || String(item.id) === String(clientMsgId));
      if (!target) {
        return null;
      }
      this.markMessageState(normalizedId, target.clientMsgId, {
        pending: true,
        failed: false,
        queued: false,
        errorMessage: ""
      });
      this.dequeueMessage(target.clientMsgId);
      await this.deliverOutgoingMessage({
        ...target,
        clientMsgId: target.clientMsgId
      });
      return true;
    },
    async flushOutgoingQueue() {
      const queue = [...this.outgoingQueue];
      for (const item of queue) {
        try {
          await this.retryMessage(item.conversationId, item.clientMsgId);
        } catch (error) {
          console.warn("Failed to flush outgoing chat message", error);
        }
      }
    },
    async deliverOutgoingMessage(message) {
      const normalizedId = Number(message.conversationId);
      const payload = {
        conversationId: normalizedId,
        msgType: message.msgType || legacyMsgType(message.type),
        clientMsgId: message.clientMsgId,
        replyMessageId: message.replyMessageId || null,
        content: message.body || { text: message.content }
      };

      try {
        const connected = await this.ensureSocketConnected();
        if (connected) {
          this.markMessageState(normalizedId, message.clientMsgId, {
            pending: true,
            failed: false,
            queued: false,
            errorMessage: ""
          });
          await chatSocket.send({
            event: "MESSAGE_SEND",
            requestId: message.clientMsgId,
            data: {
              conversationId: normalizedId,
              msgType: payload.msgType,
              replyMessageId: payload.replyMessageId,
              content: payload.content
            }
          });
          this.schedulePendingTimeout(normalizedId, message.clientMsgId);
          return true;
        }
      } catch (socketError) {
        console.warn("Socket send failed, fallback to HTTP", socketError);
      }

      try {
        const response = await chatApi.sendMessage(payload);
        this.applyDirectSendSuccess(normalizedId, message.clientMsgId, response);
        return true;
      } catch (error) {
        this.markMessageFailed(normalizedId, message.clientMsgId, error.message || "发送失败", true);
        throw error;
      }
    },
    applyDirectSendSuccess(conversationId, clientMsgId, response) {
      this.clearPendingTimeout(clientMsgId);
      this.dequeueMessage(clientMsgId);
      this.markMessageState(conversationId, clientMsgId, {
        id: response?.messageId || clientMsgId,
        clientMsgId,
        pending: false,
        failed: false,
        queued: false,
        errorMessage: "",
        time: formatDateTime(response?.sendTime)
      });
    },
    markMessageFailed(conversationId, clientMsgId, errorMessage, queued = false) {
      this.clearPendingTimeout(clientMsgId);
      this.markMessageState(conversationId, clientMsgId, {
        pending: false,
        failed: true,
        queued,
        errorMessage: errorMessage || "发送失败"
      });
      if (queued) {
        this.enqueueMessage(conversationId, clientMsgId);
      }
    },
    markMessageState(conversationId, clientMsgId, patch) {
      const current = this.messages[conversationId] || [];
      this.messages = {
        ...this.messages,
        [conversationId]: current.map((item) => {
          if (item.clientMsgId === clientMsgId || String(item.id) === String(clientMsgId)) {
            return {
              ...item,
              ...patch
            };
          }
          return item;
        })
      };
      this.syncTransientState();
    },
    enqueueMessage(conversationId, clientMsgId) {
      if (this.outgoingQueue.some((item) => item.clientMsgId === clientMsgId)) {
        return;
      }
      this.outgoingQueue = [
        ...this.outgoingQueue,
        {
          conversationId: Number(conversationId),
          clientMsgId
        }
      ];
      this.syncTransientState();
    },
    dequeueMessage(clientMsgId) {
      this.outgoingQueue = this.outgoingQueue.filter((item) => item.clientMsgId !== clientMsgId);
      this.syncTransientState();
    },
    handleSocketEvent(payload) {
      const userStore = useUserStore();
      switch (payload?.event) {
        case "SOCKET_OPEN":
          this.socketConnected = true;
          this.socketConnecting = false;
          this.socketReconnecting = false;
          this.socketReconnectAttempt = 0;
          this.lastSocketError = "";
          break;
        case "SOCKET_CLOSE":
        case "SOCKET_ERROR":
          this.socketConnected = false;
          this.socketConnecting = false;
          this.lastSocketError = payload?.data?.reason || payload?.data?.errMsg || "socket disconnected";
          break;
        case "SOCKET_CLOSED":
          this.socketConnected = false;
          this.socketConnecting = false;
          this.socketReconnecting = false;
          this.socketReconnectAttempt = 0;
          this.lastSocketError = "";
          break;
        case "SOCKET_RECONNECT_SCHEDULED":
          this.socketReconnecting = true;
          this.socketReconnectAttempt = payload?.data?.attempt || 1;
          this.lastSocketError = payload?.data?.reason || this.lastSocketError;
          break;
        case "SOCKET_RECONNECTING":
          this.socketReconnecting = true;
          this.socketConnecting = true;
          this.socketReconnectAttempt = payload?.data?.attempt || this.socketReconnectAttempt || 1;
          break;
        case "SOCKET_RECONNECTED":
          this.socketReconnecting = false;
          this.socketReconnectAttempt = 0;
          this.socketConnecting = false;
          this.refreshAfterReconnect();
          this.flushOutgoingQueue();
          break;
        case "SOCKET_RECONNECT_GIVEUP":
        case "SOCKET_RECONNECT_ABORTED":
          this.socketReconnecting = false;
          this.socketConnecting = false;
          break;
        case "CONNECT_ACK":
          this.socketConnected = true;
          this.socketConnecting = false;
          this.socketReconnecting = false;
          this.socketReconnectAttempt = 0;
          this.socketSessionId = payload?.data?.sessionId || "";
          this.lastHeartbeatAt = payload?.data?.serverTime || "";
          this.lastSocketError = "";
          if (this.outgoingQueue.length) {
            this.flushOutgoingQueue();
          }
          break;
        case "HEARTBEAT":
          this.lastHeartbeatAt = payload?.data?.serverTime || "";
          break;
        case "MESSAGE_ACK":
          this.applyAck(payload?.data || {});
          break;
        case "EVENT_ACK":
          this.applyEventAck(payload?.data || {});
          break;
        case "MESSAGE_RECEIVE":
          this.mergeIncomingMessage(payload?.data?.message, userStore.profile.id);
          break;
        default:
          break;
      }
    },
    async refreshAfterReconnect() {
      try {
        await this.fetchConversations();
        if (this.activeConversationId) {
          await this.fetchMessages(this.activeConversationId);
        }
      } catch (error) {
        console.warn("Failed to refresh chat state after reconnect", error);
      }
    },
    applyAck(data) {
      const conversationId = Number(data.conversationId);
      this.clearPendingTimeout(data.clientMsgId || data.requestId);
      this.dequeueMessage(data.clientMsgId || data.requestId);
      const current = this.messages[conversationId] || [];
      this.messages = {
        ...this.messages,
        [conversationId]: current.map((item) => {
          if (item.clientMsgId === data.clientMsgId || item.id === data.requestId) {
            return {
              ...item,
              id: data.messageId || item.id,
              clientMsgId: data.clientMsgId || item.clientMsgId,
              pending: false,
              failed: false,
              queued: false,
              errorMessage: "",
              time: formatDateTime(data.sendTime || item.time)
            };
          }
          return item;
        })
      };
      this.syncTransientState();
    },
    applyEventAck(data) {
      if (data.event !== "MESSAGE_SEND") {
        return;
      }
      const clientMsgId = data.requestId;
      const matchedQueueItem = this.outgoingQueue.find((item) => item.clientMsgId === clientMsgId);
      const matchedConversationId = matchedQueueItem?.conversationId || Object.keys(this.messages).find((conversationId) => {
        return (this.messages[conversationId] || []).some((item) => item.clientMsgId === clientMsgId);
      });
      if (!matchedConversationId) {
        return;
      }
      this.markMessageFailed(Number(matchedConversationId), clientMsgId, data.message || "发送失败", false);
    },
    mergeIncomingMessage(message, currentUserId) {
      if (!message) {
        return;
      }
      const conversationId = Number(message.conversationId);
      this.clearPendingTimeout(message.clientMsgId);
      const userStore = useUserStore();
      const normalizedMessage = normalizeMessageRecord(message, currentUserId, userStore.profile.nickname);
      const current = this.messages[conversationId] || [];
      const existingIndex = current.findIndex((item) => item.id === normalizedMessage.id || item.clientMsgId === normalizedMessage.clientMsgId);
      if (existingIndex >= 0) {
        const next = [...current];
        next[existingIndex] = {
          ...next[existingIndex],
          ...normalizedMessage
        };
        this.messages = {
          ...this.messages,
          [conversationId]: next
        };
        this.syncTransientState();
      } else {
        this.pushMessage(conversationId, normalizedMessage);
      }
      const isActiveConversation = this.activeConversationId === conversationId;
      this.updateConversationSnapshot(conversationId, normalizedMessage.previewText || normalizedMessage.content, normalizedMessage.time, isActiveConversation || normalizedMessage.self);
    },
    pushMessage(conversationId, message) {
      const current = this.messages[conversationId] || [];
      this.messages = {
        ...this.messages,
        [conversationId]: [...current, message]
      };
      this.syncTransientState();
    },
    updateConversationSnapshot(conversationId, preview, time, readCurrent) {
      let found = false;
      this.conversations = this.conversations.map((item) => {
        if (Number(item.id) === Number(conversationId)) {
          found = true;
          return {
            ...item,
            preview,
            time,
            unread: readCurrent ? 0 : (item.unread || 0) + 1
          };
        }
        return item;
      });
      if (!found) {
        this.conversations = [
          {
            id: Number(conversationId),
            title: `会话-${conversationId}`,
            avatar: "https://weiyou.local/avatar/default.png",
            preview,
            time,
            unread: readCurrent ? 0 : 1,
            muted: false,
            pinned: false,
            type: "single"
          },
          ...this.conversations
        ];
      }
    }
  }
});
