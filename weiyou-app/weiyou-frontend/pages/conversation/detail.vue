<template>
  <view class="conversation-page">
    <view v-if="searchVisible" class="search-bar">
      <input v-model="searchKeyword" class="search-input-inner" placeholder="搜索本会话消息内容" />
      <text class="search-result">{{ searchKeyword.trim() ? `${filteredMessages.length} 条` : '全部消息' }}</text>
      <text class="search-cancel" @click="clearSearch">取消</text>
    </view>

    <view v-if="pinnedMessage" class="pinned-bar">
      <view class="pinned-content" @click="scrollToMessage(pinnedMessage)">
        <text class="pinned-label">置顶消息</text>
        <text class="pinned-text">{{ pinnedMessage.previewText || pinnedMessage.content }}</text>
      </view>
      <text class="pinned-clear" @click="clearPinnedMessage">清除</text>
    </view>

    <scroll-view class="message-scroll" scroll-y :scroll-into-view="scrollAnchorId" :scroll-with-animation="true">
      <view class="message-list">
        <template v-for="entry in displayMessages" :key="entry.key">
          <view v-if="entry.kind === 'time'" class="time-divider-wrap">
            <text class="time-divider">{{ entry.label }}</text>
          </view>
          <view v-else :id="`message-${messageKey(entry.message)}`" class="message-row" :class="entry.message.self ? 'self' : 'other'">
            <view class="bubble" :class="[entry.message.failed ? 'bubble-failed' : '', isSelected(entry.message) ? 'bubble-selected' : '']" @click="handleMessageBubbleClick(entry.message)" @longpress="openMessageActions(entry.message)">
              <text class="name" v-if="!entry.message.self">{{ entry.message.senderName }}</text>

              <view v-if="entry.message.replyPreviewText" class="reply-quote">
                <text class="reply-name">回复 {{ entry.message.replySenderName || '消息' }}</text>
                <text class="reply-text">{{ entry.message.replyPreviewText }}</text>
              </view>

              <text v-if="entry.message.type === 'text'" class="content">{{ entry.message.content }}</text>

              <text v-else-if="entry.message.type === 'recalled'" class="recalled-text">{{ entry.message.content }}</text>

              <image
                v-else-if="entry.message.type === 'image'"
                class="image-message"
                :src="entry.message.imageUrl"
                mode="aspectFill"
                @click.stop="previewMessageImage(entry.message)"
              />

              <view v-else-if="entry.message.type === 'file'" class="file-card" @click.stop="openFileMessage(entry.message)">
                <view class="file-icon">文</view>
                <view class="file-meta">
                  <text class="file-name">{{ entry.message.fileName || entry.message.content }}</text>
                  <text class="file-size">{{ entry.message.fileSizeText || '文件消息' }}</text>
                </view>
              </view>

              <view v-else-if="entry.message.type === 'voice'" class="voice-card" @click.stop="playVoiceMessage(entry.message)">
                <view class="voice-icon">▶</view>
                <view class="voice-meta">
                  <text class="voice-label">语音消息</text>
                  <text class="voice-duration">{{ entry.message.durationText || '1"' }}</text>
                </view>
                <text v-if="playingMessageKey === (entry.message.clientMsgId || entry.message.id)" class="voice-playing">播放中</text>
              </view>

              <view v-else-if="entry.message.type === 'video'" class="video-card" @click.stop="handleVideoTap(entry.message)">
                <video
                  class="video-message"
                  :src="entry.message.videoUrl"
                  :poster="entry.message.coverUrl || ''"
                  object-fit="cover"
                  :controls="true"
                />
                <text class="video-duration">{{ entry.message.durationText || '视频消息' }}</text>
              </view>

              <view v-else-if="entry.message.type === 'location'" class="location-card" @click.stop="openLocationMessage(entry.message)">
                <view class="location-icon">位</view>
                <view class="location-meta">
                  <text class="location-name">{{ entry.message.locationName || entry.message.content }}</text>
                  <text class="location-address">{{ entry.message.address || '点击查看位置' }}</text>
                </view>
              </view>

              <view v-else-if="entry.message.type === 'card'" class="card-message" @click.stop="openCardMessage(entry.message)">
                <image class="card-avatar" :src="entry.message.cardAvatar || 'https://weiyou.local/avatar/default.png'" mode="aspectFill" />
                <view class="card-meta">
                  <text class="card-name">{{ entry.message.cardNickname || entry.message.content }}</text>
                  <text class="card-subtitle">{{ entry.message.weiyouNo || '微友名片' }}</text>
                  <text class="card-subtitle">{{ entry.message.city || '未设置城市' }}</text>
                </view>
              </view>

              <text v-else class="content">{{ entry.message.content }}</text>

              <view class="meta-line">
                <text class="time">{{ entry.message.time }}</text>
                <text v-if="entry.message.pending" class="status sending">发送中</text>
                <text v-else-if="entry.message.failed || entry.message.queued" class="status failed" @click.stop="retryMessage(entry.message)">
                  {{ entry.message.queued ? '待重试' : '发送失败' }}
                </text>
                <text v-if="selectionMode" class="select-indicator">{{ isSelected(entry.message) ? '已选' : '点选' }}</text>
              </view>
            </view>
          </view>
        </template>
        <view id="bottom-anchor" class="bottom-anchor" />
      </view>
    </scroll-view>

    <view class="composer">
      <view class="composer-tools">
        <view class="tool-chip" @click="chooseImageMessage">图片</view>
        <view class="tool-chip" @click="captureImageMessage">拍照</view>
        <view class="tool-chip" @click="chooseVideoMessage">视频</view>
        <view class="tool-chip" @click="chooseFileMessage">文件</view>
        <view class="tool-chip" @click="chooseLocationMessage">位置</view>
        <view class="tool-chip" @click="chooseCardMessage">名片</view>
        <view class="tool-chip" :class="emojiPanelVisible ? 'tool-chip-active' : ''" @click="toggleEmojiPanel">表情</view>
        <view class="tool-chip" :class="searchVisible ? 'tool-chip-active' : ''" @click="toggleSearch">搜索</view>
        <view class="tool-chip" @click="openConversationSettings">设置</view>
        <view class="tool-chip" :class="selectionMode ? 'tool-chip-active' : ''" @click="toggleSelectionMode">多选</view>
        <view class="tool-chip" :class="recording ? 'tool-chip-recording' : ''" @click="toggleVoiceRecord">
          {{ recording ? `停止 ${recordingSeconds}s` : '语音' }}
        </view>
      </view>
      <view v-if="selectionMode" class="selection-bar">
        <text class="selection-count">已选 {{ selectedMessages.length }} 条</text>
        <view class="selection-actions">
          <view class="ghost-button mini-select-btn" @click="collectSelectedMessages">收藏</view>
          <view class="ghost-button mini-select-btn" @click="forwardSelectedMessages">转发</view>
          <view class="ghost-button mini-select-btn" @click="revokeSelectedMessages">撤回</view>
          <view class="ghost-button mini-select-btn" @click="cancelSelectionMode">取消</view>
        </view>
      </view>
      <view v-if="emojiPanelVisible" class="emoji-panel">
        <view v-for="emoji in emojiList" :key="emoji" class="emoji-item" @click="quickSendEmoji(emoji)">{{ emoji }}</view>
      </view>
      <view v-if="replyTarget" class="reply-bar">
        <view class="reply-bar-content">
          <text class="reply-bar-title">回复 {{ replyTarget.replySenderName || '消息' }}</text>
          <text class="reply-bar-text">{{ replyTarget.replyPreviewText }}</text>
        </view>
        <text class="reply-bar-cancel" @click="clearReplyTarget">取消</text>
      </view>
      <view class="composer-main">
        <input v-model="inputValue" class="composer-input" placeholder="输入消息、链接、待办或文件说明" confirm-type="send" @confirm="submitMessage" />
        <view class="action-button composer-button" @click="submitMessage">发送</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, nextTick, ref, watch } from "vue";
import { onLoad, onShow, onUnload } from "@dcloudio/uni-app";
import { contactApi, mediaApi, userApi } from "@/api/modules";
import { useChatStore } from "@/stores/chat";
import { useUserStore } from "@/stores/user";

const emojiList = ["😀", "😂", "🥳", "❤️", "👍", "🎉", "👏", "😎", "📍", "🔥"];

function createRecorderManagerSafe() {
  try {
    if (typeof uni.getRecorderManager === "function") {
      return uni.getRecorderManager();
    }
  } catch (error) {
    return null;
  }
  return null;
}

function createAudioContextSafe() {
  try {
    if (typeof uni.createInnerAudioContext === "function") {
      return uni.createInnerAudioContext();
    }
  } catch (error) {
    return null;
  }
  return null;
}

function buildDisplayMessages(messages = []) {
  const result = [];
  let previousLabel = "";
  messages.forEach((message, index) => {
    const label = message.time || "刚刚";
    if (label !== previousLabel) {
      result.push({
        kind: "time",
        key: `time-${index}-${label}`,
        label
      });
      previousLabel = label;
    }
    result.push({
      kind: "message",
      key: `message-${message.clientMsgId || message.id}`,
      message
    });
  });
  return result;
}

function extractFilePayload(result) {
  const tempFile = result?.tempFiles?.[0] || result?.tempFile || null;
  if (!tempFile) {
    return null;
  }
  const filePath = tempFile.path || tempFile.tempFilePath || tempFile.filePath || "";
  const fileName = tempFile.name || (filePath.split("/").pop() || "未命名文件");
  const ext = fileName.includes(".") ? fileName.split(".").pop().toLowerCase() : "";
  return {
    fileUrl: filePath,
    fileName,
    fileSize: Number(tempFile.size || 0),
    ext
  };
}

function messageKey(message) {
  return String(message.clientMsgId || message.id || "");
}

const chatStore = useChatStore();
const userStore = useUserStore();
const conversationId = ref(90001);
const inputValue = ref("");
const sending = ref(false);
const draftReady = ref(false);
const scrollAnchorId = ref("bottom-anchor");
const recording = ref(false);
const recordingSeconds = ref(0);
const playingMessageKey = ref("");
const emojiPanelVisible = ref(false);
const selectionMode = ref(false);
const selectedMessageKeys = ref([]);
const searchVisible = ref(false);
const searchKeyword = ref("");
const replyTarget = ref(null);

const recorderManager = createRecorderManagerSafe();
const audioContext = createAudioContextSafe();
let recordingTimer = null;

if (audioContext) {
  audioContext.onEnded(() => {
    playingMessageKey.value = "";
  });
  audioContext.onStop(() => {
    playingMessageKey.value = "";
  });
  audioContext.onError(() => {
    playingMessageKey.value = "";
  });
}

if (recorderManager) {
  recorderManager.onStop(async (result) => {
    stopRecordingTimer();
    recording.value = false;
    const tempFilePath = result?.tempFilePath;
    if (!tempFilePath) {
      return;
    }
    sending.value = true;
    try {
      const uploadResult = await mediaApi.uploadLocalFile(tempFilePath, "chat-voice");
      await chatStore.sendVoiceMessage(conversationId.value, {
        voiceUrl: uploadResult.url || tempFilePath,
        durationMs: Number(result?.duration || 0),
        fileName: uploadResult.fileName || "voice.mp3"
      }, replyTarget.value);
      clearReplyTarget();
      scrollToBottom();
    } catch (error) {
      uni.showToast({
        title: error.message || "语音发送失败",
        icon: "none"
      });
    } finally {
      sending.value = false;
    }
  });
  recorderManager.onError((error) => {
    stopRecordingTimer();
    recording.value = false;
    uni.showToast({
      title: error?.errMsg || "录音失败",
      icon: "none"
    });
  });
}

const currentMessages = computed(() => chatStore.messages[conversationId.value] || []);
const filteredMessages = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase();
  if (!keyword) {
    return currentMessages.value;
  }
  return currentMessages.value.filter((message) => {
    const bag = [
      message.content,
      message.previewText,
      message.fileName,
      message.locationName,
      message.address,
      message.cardNickname,
      message.replyPreviewText,
      message.replySenderName,
      message.senderName
    ].filter(Boolean).join(" ").toLowerCase();
    return bag.includes(keyword);
  });
});
const displayMessages = computed(() => buildDisplayMessages(filteredMessages.value));
const selectedMessages = computed(() => {
  const keySet = new Set(selectedMessageKeys.value);
  return currentMessages.value.filter((item) => keySet.has(messageKey(item)));
});
const pinnedMessage = computed(() => {
  const key = chatStore.getPinnedMessageKey(conversationId.value);
  if (!key) {
    return null;
  }
  return currentMessages.value.find((item) => messageKey(item) === key) || null;
});

watch(displayMessages, () => {
  scrollToBottom();
}, { deep: true });

watch(inputValue, (value) => {
  if (!draftReady.value) {
    return;
  }
  chatStore.setDraft(conversationId.value, value);
});

onLoad((query) => {
  if (query && query.id) {
    conversationId.value = Number(query.id);
  }
});

onShow(async () => {
  if (!userStore.requireAuth()) {
    return;
  }
  try {
    chatStore.openConversation(conversationId.value);
    selectionMode.value = false;
    selectedMessageKeys.value = [];
    searchVisible.value = false;
    searchKeyword.value = "";
    replyTarget.value = null;
    inputValue.value = chatStore.getDraft(conversationId.value);
    draftReady.value = true;
    await chatStore.fetchMessages(conversationId.value);
    scrollToBottom();
  } catch (error) {
    uni.showToast({
      title: error.message || "加载消息失败",
      icon: "none"
    });
  }
});

onUnload(() => {
  draftReady.value = false;
  stopRecordingTimer();
  if (recording.value && recorderManager) {
    try {
      recorderManager.stop();
    } catch (error) {
      console.warn("stop recorder warning", error);
    }
  }
  if (audioContext) {
    try {
      audioContext.stop();
      audioContext.destroy();
    } catch (error) {
      console.warn("destroy audio context warning", error);
    }
  }
  chatStore.clearActiveConversation();
});

function stopRecordingTimer() {
  if (recordingTimer) {
    clearInterval(recordingTimer);
    recordingTimer = null;
  }
  recordingSeconds.value = 0;
}

function startRecordingTimer() {
  stopRecordingTimer();
  recordingSeconds.value = 0;
  recordingTimer = setInterval(() => {
    recordingSeconds.value += 1;
  }, 1000);
}

function scrollToBottom() {
  nextTick(() => {
    scrollAnchorId.value = "";
    setTimeout(() => {
      scrollAnchorId.value = "bottom-anchor";
    }, 0);
  });
}

function isSelected(message) {
  return selectedMessageKeys.value.includes(messageKey(message));
}

function toggleSelectionMode() {
  selectionMode.value = !selectionMode.value;
  if (!selectionMode.value) {
    selectedMessageKeys.value = [];
    return;
  }
  emojiPanelVisible.value = false;
  searchVisible.value = false;
}

function cancelSelectionMode() {
  selectionMode.value = false;
  selectedMessageKeys.value = [];
}

function toggleSearch() {
  searchVisible.value = !searchVisible.value;
  if (!searchVisible.value) {
    searchKeyword.value = "";
  }
  emojiPanelVisible.value = false;
  selectionMode.value = false;
  selectedMessageKeys.value = [];
}

function clearSearch() {
  searchVisible.value = false;
  searchKeyword.value = "";
}

function buildReplyTargetFromMessage(message) {
  return {
    replyMessageId: Number(message.id || 0),
    replyPreviewText: message.previewText || message.content || "消息",
    replySenderName: message.senderName || "消息",
    replyType: message.type || "text"
  };
}

function setReplyTarget(message) {
  if (!message || message.type === "recalled") {
    return;
  }
  replyTarget.value = buildReplyTargetFromMessage(message);
  selectionMode.value = false;
  selectedMessageKeys.value = [];
}

function clearReplyTarget() {
  replyTarget.value = null;
}

function scrollToMessage(message) {
  nextTick(() => {
    scrollAnchorId.value = `message-${messageKey(message)}`;
  });
}

function handleMessageBubbleClick(message) {
  if (!selectionMode.value) {
    return;
  }
  const key = messageKey(message);
  if (isSelected(message)) {
    selectedMessageKeys.value = selectedMessageKeys.value.filter((item) => item !== key);
    return;
  }
  selectedMessageKeys.value = [...selectedMessageKeys.value, key];
}

async function collectSelectedMessages() {
  if (!selectedMessages.value.length) {
    uni.showToast({ title: "请先选择消息", icon: "none" });
    return;
  }
  await collectMessages(selectedMessages.value, true);
}

async function collectMessages(messages, exitSelection = false) {
  try {
    for (const message of messages) {
      await chatStore.collectMessage(message);
    }
    uni.showToast({ title: `已收藏 ${messages.length} 条`, icon: "success" });
    if (exitSelection) {
      cancelSelectionMode();
    }
  } catch (error) {
    uni.showToast({ title: error.message || "收藏失败", icon: "none" });
  }
}

async function forwardSelectedMessages() {
  if (!selectedMessages.value.length) {
    uni.showToast({ title: "请先选择消息", icon: "none" });
    return;
  }
  await forwardMessages(selectedMessages.value, true);
}

async function forwardMessages(messages, exitSelection = false) {
  try {
    const conversations = chatStore.conversations.length ? chatStore.conversations : await chatStore.fetchConversations();
    if (!conversations.length) {
      uni.showToast({ title: "暂无可转发会话", icon: "none" });
      return;
    }
    uni.showActionSheet({
      itemList: conversations.map((item) => item.title),
      success: async ({ tapIndex }) => {
        const target = conversations[tapIndex];
        if (!target) {
          return;
        }
        try {
          const ordered = [...messages];
          for (const message of ordered) {
            await chatStore.forwardMessage(target.id, message);
          }
          uni.showToast({ title: `已转发到 ${target.title}`, icon: "success" });
          if (exitSelection) {
            cancelSelectionMode();
          }
        } catch (error) {
          uni.showToast({ title: error.message || "转发失败", icon: "none" });
        }
      }
    });
  } catch (error) {
    uni.showToast({ title: error.message || "加载会话失败", icon: "none" });
  }
}

async function revokeSelectedMessages() {
  if (!selectedMessages.value.length) {
    uni.showToast({ title: "请先选择消息", icon: "none" });
    return;
  }
  await revokeMessages(selectedMessages.value, true);
}

async function revokeMessages(messages, exitSelection = false) {
  const revocable = messages.filter((item) => item.self && !item.pending && item.type !== "recalled");
  if (!revocable.length) {
    uni.showToast({ title: "仅支持撤回自己已发送的消息", icon: "none" });
    return;
  }
  try {
    for (const message of revocable) {
      await chatStore.revokeMessage(conversationId.value, message.id);
    }
    uni.showToast({ title: `已撤回 ${revocable.length} 条`, icon: "success" });
    if (exitSelection) {
      cancelSelectionMode();
    }
  } catch (error) {
    uni.showToast({ title: error.message || "撤回失败", icon: "none" });
  }
}

function togglePinMessage(message) {
  const currentKey = chatStore.getPinnedMessageKey(conversationId.value);
  if (currentKey && currentKey === messageKey(message)) {
    chatStore.clearPinnedMessage(conversationId.value);
    uni.showToast({ title: "已取消置顶", icon: "success" });
    return;
  }
  chatStore.pinMessage(conversationId.value, message);
  uni.showToast({ title: "已置顶消息", icon: "success" });
}

function clearPinnedMessage() {
  chatStore.clearPinnedMessage(conversationId.value);
}

function openConversationSettings() {
  uni.navigateTo({
    url: `/pages/conversation/settings?conversationId=${conversationId.value}`
  });
}

function openMessageActions(message) {
  if (selectionMode.value) {
    handleMessageBubbleClick(message);
    return;
  }
  const currentPinKey = chatStore.getPinnedMessageKey(conversationId.value);
  const options = ["回复", currentPinKey === messageKey(message) ? "取消置顶" : "置顶消息", "收藏", "转发"];
  if (message.self && !message.pending && message.type !== "recalled") {
    options.push("撤回");
  }
  uni.showActionSheet({
    itemList: options,
    success: async ({ tapIndex }) => {
      const action = options[tapIndex];
      if (action === "回复") {
        setReplyTarget(message);
        return;
      }
      if (action === "置顶消息" || action === "取消置顶") {
        togglePinMessage(message);
        return;
      }
      if (action === "收藏") {
        await collectMessages([message], false);
        return;
      }
      if (action === "转发") {
        await forwardMessages([message], false);
        return;
      }
      if (action === "撤回") {
        await revokeMessages([message], false);
      }
    }
  });
}

async function submitMessage() {
  const content = inputValue.value.trim();
  if (!content || sending.value) {
    return;
  }
  emojiPanelVisible.value = false;
  sending.value = true;
  inputValue.value = "";
  chatStore.clearDraft(conversationId.value);
  try {
    await chatStore.sendMessage(conversationId.value, content, replyTarget.value);
    clearReplyTarget();
    scrollToBottom();
  } catch (error) {
    uni.showToast({
      title: error.message || "发送失败，已加入待重试",
      icon: "none"
    });
  } finally {
    sending.value = false;
  }
}

async function sendQuickText(text) {
  if (!text || sending.value) {
    return;
  }
  emojiPanelVisible.value = false;
  sending.value = true;
  try {
    await chatStore.sendMessage(conversationId.value, text, replyTarget.value);
    clearReplyTarget();
    scrollToBottom();
  } catch (error) {
    uni.showToast({
      title: error.message || "发送失败，已加入待重试",
      icon: "none"
    });
  } finally {
    sending.value = false;
  }
}

function toggleEmojiPanel() {
  emojiPanelVisible.value = !emojiPanelVisible.value;
}

function quickSendEmoji(emoji) {
  sendQuickText(emoji);
}

async function chooseImageMessage() {
  if (sending.value) {
    return;
  }
  emojiPanelVisible.value = false;
  uni.chooseImage({
    count: 1,
    sizeType: ["compressed"],
    success: async (result) => {
      const filePath = result?.tempFilePaths?.[0];
      if (!filePath) {
        return;
      }
      const fileMeta = result?.tempFiles?.[0] || {};
      sending.value = true;
      try {
        const uploadResult = await mediaApi.uploadLocalImage(filePath, "chat-image");
        await chatStore.sendImageMessage(conversationId.value, {
          imageUrl: uploadResult.url || filePath,
          coverUrl: uploadResult.coverUrl || uploadResult.url || filePath,
          width: fileMeta.width || 0,
          height: fileMeta.height || 0,
          fileName: uploadResult.fileName || fileMeta.name || "image.jpg"
        }, replyTarget.value);
        clearReplyTarget();
        scrollToBottom();
      } catch (error) {
        uni.showToast({
          title: error.message || "图片发送失败",
          icon: "none"
        });
      } finally {
        sending.value = false;
      }
    }
  });
}

async function captureImageMessage() {
  if (sending.value) {
    return;
  }
  emojiPanelVisible.value = false;
  uni.chooseImage({
    count: 1,
    sizeType: ["compressed"],
    sourceType: ["camera"],
    success: async (result) => {
      const filePath = result?.tempFilePaths?.[0];
      if (!filePath) {
        return;
      }
      const fileMeta = result?.tempFiles?.[0] || {};
      sending.value = true;
      try {
        const uploadResult = await mediaApi.uploadLocalImage(filePath, "chat-camera");
        await chatStore.sendImageMessage(conversationId.value, {
          imageUrl: uploadResult.url || filePath,
          coverUrl: uploadResult.coverUrl || uploadResult.url || filePath,
          width: fileMeta.width || 0,
          height: fileMeta.height || 0,
          fileName: uploadResult.fileName || fileMeta.name || "camera.jpg"
        }, replyTarget.value);
        clearReplyTarget();
        scrollToBottom();
      } catch (error) {
        uni.showToast({
          title: error.message || "拍照发送失败",
          icon: "none"
        });
      } finally {
        sending.value = false;
      }
    }
  });
}

async function chooseVideoMessage() {
  if (sending.value || typeof uni.chooseVideo !== "function") {
    if (typeof uni.chooseVideo !== "function") {
      uni.showToast({
        title: "当前端不支持视频选择",
        icon: "none"
      });
    }
    return;
  }
  uni.chooseVideo({
    compressed: true,
    maxDuration: 60,
    success: async (result) => {
      const filePath = result?.tempFilePath;
      if (!filePath) {
        return;
      }
      sending.value = true;
      try {
        const uploadResult = await mediaApi.uploadLocalVideo(filePath, "chat-video");
        await chatStore.sendVideoMessage(conversationId.value, {
          videoUrl: uploadResult.url || filePath,
          coverUrl: result?.thumbTempFilePath || uploadResult.coverUrl || uploadResult.url || "",
          durationSec: Number(result?.duration || 0),
          width: Number(result?.width || 0),
          height: Number(result?.height || 0),
          fileName: uploadResult.fileName || "video.mp4",
          fileSize: Number(result?.size || uploadResult.size || 0)
        }, replyTarget.value);
        clearReplyTarget();
        scrollToBottom();
      } catch (error) {
        uni.showToast({
          title: error.message || "视频发送失败",
          icon: "none"
        });
      } finally {
        sending.value = false;
      }
    }
  });
}

async function chooseFileMessage() {
  if (sending.value || typeof uni.chooseFile !== "function") {
    if (typeof uni.chooseFile !== "function") {
      uni.showToast({
        title: "当前端不支持文件选择",
        icon: "none"
      });
    }
    return;
  }
  emojiPanelVisible.value = false;
  uni.chooseFile({
    count: 1,
    success: async (result) => {
      const filePayload = extractFilePayload(result);
      if (!filePayload) {
        return;
      }
      sending.value = true;
      try {
        const uploadResult = await mediaApi.uploadLocalFile(filePayload.fileUrl, "chat-file");
        await chatStore.sendFileMessage(conversationId.value, {
          fileUrl: uploadResult.url || filePayload.fileUrl,
          fileName: uploadResult.fileName || filePayload.fileName,
          fileSize: uploadResult.size || filePayload.fileSize,
          ext: filePayload.ext
        }, replyTarget.value);
        clearReplyTarget();
        scrollToBottom();
      } catch (error) {
        uni.showToast({
          title: error.message || "文件发送失败",
          icon: "none"
        });
      } finally {
        sending.value = false;
      }
    }
  });
}

async function chooseLocationMessage() {
  if (sending.value) {
    return;
  }
  emojiPanelVisible.value = false;
  const sendLocation = async (payload) => {
    sending.value = true;
    try {
      await chatStore.sendLocationMessage(conversationId.value, payload, replyTarget.value);
      clearReplyTarget();
      scrollToBottom();
    } catch (error) {
      uni.showToast({
        title: error.message || "位置发送失败",
        icon: "none"
      });
    } finally {
      sending.value = false;
    }
  };
  if (typeof uni.chooseLocation === "function") {
    uni.chooseLocation({
      success: (result) => {
        sendLocation({
          locationName: result.name || "位置消息",
          address: result.address || "",
          latitude: result.latitude,
          longitude: result.longitude
        });
      },
      fail: async () => {
        if (typeof uni.getLocation === "function") {
          uni.getLocation({
            type: "gcj02",
            success: (result) => {
              sendLocation({
                locationName: "当前位置",
                address: `纬度 ${result.latitude.toFixed(4)}，经度 ${result.longitude.toFixed(4)}`,
                latitude: result.latitude,
                longitude: result.longitude
              });
            }
          });
        }
      }
    });
    return;
  }
  if (typeof uni.getLocation === "function") {
    uni.getLocation({
      type: "gcj02",
      success: (result) => {
        sendLocation({
          locationName: "当前位置",
          address: `纬度 ${result.latitude.toFixed(4)}，经度 ${result.longitude.toFixed(4)}`,
          latitude: result.latitude,
          longitude: result.longitude
        });
      },
      fail: () => {
        uni.showToast({
          title: "当前环境不支持位置选择",
          icon: "none"
        });
      }
    });
  }
}

async function chooseCardMessage() {
  if (sending.value) {
    return;
  }
  emojiPanelVisible.value = false;
  try {
    const page = await contactApi.list("");
    const candidates = (page?.list || []).slice(0, 8);
    if (!candidates.length) {
      uni.showToast({ title: "暂无可发送的联系人", icon: "none" });
      return;
    }
    uni.showActionSheet({
      itemList: candidates.map((item) => item.nickname || `用户${item.userId}`),
      success: async ({ tapIndex }) => {
        const selected = candidates[tapIndex];
        if (!selected) {
          return;
        }
        sending.value = true;
        try {
          let detail = null;
          try {
            detail = await userApi.detail(selected.userId);
          } catch (error) {
            detail = null;
          }
          await chatStore.sendCardMessage(conversationId.value, {
            cardUserId: selected.userId,
            cardNickname: detail?.nickname || selected.nickname || `用户${selected.userId}`,
            cardAvatar: detail?.avatar || selected.avatar || "",
            weiyouNo: detail?.weiyouNo || `weiyou_${selected.userId}`,
            city: detail?.city || "未设置城市",
            signature: detail?.signature || "这个人很低调，还没有签名。"
          }, replyTarget.value);
          clearReplyTarget();
          scrollToBottom();
        } catch (error) {
          uni.showToast({
            title: error.message || "名片发送失败",
            icon: "none"
          });
        } finally {
          sending.value = false;
        }
      }
    });
  } catch (error) {
    uni.showToast({
      title: error.message || "加载联系人失败",
      icon: "none"
    });
  }
}

function toggleVoiceRecord() {
  if (!recorderManager) {
    uni.showToast({
      title: "当前端暂不支持录音",
      icon: "none"
    });
    return;
  }
  if (sending.value) {
    return;
  }
  if (recording.value) {
    try {
      recorderManager.stop();
    } catch (error) {
      stopRecordingTimer();
      recording.value = false;
      uni.showToast({
        title: error?.message || "结束录音失败",
        icon: "none"
      });
    }
    return;
  }
  try {
    recorderManager.start({
      duration: 60000,
      format: "mp3"
    });
    recording.value = true;
    startRecordingTimer();
  } catch (error) {
    stopRecordingTimer();
    recording.value = false;
    uni.showToast({
      title: error?.message || "开始录音失败",
      icon: "none"
    });
  }
}

function previewMessageImage(message) {
  if (selectionMode.value) {
    handleMessageBubbleClick(message);
    return;
  }
  if (!message.imageUrl) {
    return;
  }
  uni.previewImage({
    urls: [message.imageUrl],
    current: message.imageUrl
  });
}

function playVoiceMessage(message) {
  if (selectionMode.value) {
    handleMessageBubbleClick(message);
    return;
  }
  if (!audioContext || !message.voiceUrl) {
    uni.showToast({
      title: "当前语音不可播放",
      icon: "none"
    });
    return;
  }
  const key = message.clientMsgId || message.id;
  if (playingMessageKey.value === key) {
    audioContext.stop();
    playingMessageKey.value = "";
    return;
  }
  playingMessageKey.value = key;
  audioContext.src = message.voiceUrl;
  audioContext.play();
}

function handleVideoTap(message) {
  if (selectionMode.value) {
    handleMessageBubbleClick(message);
  }
}

function openLocationMessage(message) {
  if (selectionMode.value) {
    handleMessageBubbleClick(message);
    return;
  }
  if (typeof uni.openLocation === "function" && message.latitude && message.longitude) {
    uni.openLocation({
      latitude: Number(message.latitude),
      longitude: Number(message.longitude),
      name: message.locationName || "位置消息",
      address: message.address || ""
    });
    return;
  }
  const detail = [message.locationName || message.content, message.address || "暂无地址信息"].filter(Boolean).join("\n");
  uni.showModal({
    title: "位置消息",
    content: detail,
    showCancel: false,
    confirmText: "知道了"
  });
}

function openCardMessage(message) {
  if (selectionMode.value) {
    handleMessageBubbleClick(message);
    return;
  }
  if (message.cardUserId) {
    uni.navigateTo({
      url: `/pages/contacts/profile?id=${message.cardUserId}&conversationId=0`
    });
    return;
  }
  const detail = [
    message.cardNickname || message.content,
    message.weiyouNo || "",
    message.city || "",
    message.signature || ""
  ].filter(Boolean).join("\n");
  uni.showModal({
    title: "名片消息",
    content: detail,
    showCancel: false,
    confirmText: "知道了"
  });
}

function openFileMessage(message) {
  if (selectionMode.value) {
    handleMessageBubbleClick(message);
    return;
  }
  const detail = [message.fileName || message.content, message.fileSizeText || "未记录文件大小", message.fileUrl || ""].filter(Boolean).join("\n");
  uni.showModal({
    title: "文件消息",
    content: detail,
    showCancel: false,
    confirmText: "知道了"
  });
}

async function retryMessage(message) {
  if (message.pending) {
    return;
  }
  try {
    await chatStore.retryMessage(conversationId.value, message.clientMsgId);
    scrollToBottom();
  } catch (error) {
    uni.showToast({
      title: error.message || "重发失败",
      icon: "none"
    });
  }
}
</script>

<style scoped lang="css">
.conversation-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  width: 100%;
  max-width: 1280px;
  margin: 0 auto;
  padding: 24rpx 24rpx 0;
  background: linear-gradient(180deg, #f8f4ec 0%, #efe9dc 100%);
}

.search-bar,
.pinned-bar {
  display: flex;
  align-items: center;
  gap: 14rpx;
  padding: 16rpx 18rpx;
  border-radius: 22rpx;
  background: rgba(255, 255, 255, 0.84);
  border: 1rpx solid rgba(13, 92, 82, 0.08);
  margin-bottom: 16rpx;
}

.search-input-inner {
  flex: 1;
  height: 72rpx;
  padding: 0 20rpx;
  border-radius: 999rpx;
  background: rgba(13, 92, 82, 0.06);
}

.search-result,
.search-cancel,
.pinned-label,
.pinned-clear {
  font-size: 22rpx;
}

.search-cancel,
.pinned-clear {
  color: var(--wy-primary);
  font-weight: 700;
  cursor: pointer;
}

.pinned-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.pinned-label {
  color: var(--wy-accent);
  font-weight: 700;
}

.pinned-text {
  font-size: 24rpx;
  color: var(--wy-subtext);
}

.message-scroll {
  flex: 1;
  min-height: 0;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  padding-bottom: 212rpx;
}

.time-divider-wrap {
  display: flex;
  justify-content: center;
}

.time-divider {
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  background: rgba(0, 0, 0, 0.05);
  color: var(--wy-subtext);
  font-size: 20rpx;
}

.message-row {
  display: flex;
}

.other {
  justify-content: flex-start;
}

.self {
  justify-content: flex-end;
}

.bubble {
  max-width: 76%;
  padding: 18rpx 18rpx 14rpx;
  border-radius: 18rpx;
  background: rgba(255, 255, 255, 0.98);
  border: 1rpx solid rgba(0, 0, 0, 0.04);
}

.self .bubble {
  background: #b7f27a;
  color: #1b2a1e;
}

.bubble-failed {
  box-shadow: 0 0 0 2rpx rgba(197, 77, 67, 0.18);
}

.bubble-selected {
  box-shadow: 0 0 0 2rpx rgba(31, 138, 112, 0.18);
}

.name {
  display: block;
  margin-bottom: 8rpx;
  font-size: 20rpx;
  color: var(--wy-subtext);
}

.content {
  font-size: 26rpx;
  line-height: 1.55;
}

.recalled-text {
  font-size: 26rpx;
  line-height: 1.6;
  color: var(--wy-subtext);
}

.reply-quote {
  margin-bottom: 10rpx;
  padding: 12rpx 14rpx;
  border-radius: 14rpx;
  background: rgba(0, 0, 0, 0.05);
}

.self .reply-quote {
  background: rgba(255, 255, 255, 0.16);
}

.reply-name {
  display: block;
  font-size: 22rpx;
  font-weight: 700;
}

.reply-text {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
  opacity: 0.82;
  line-height: 1.5;
}

.image-message {
  width: 320rpx;
  height: 320rpx;
  border-radius: 16rpx;
  background: rgba(0, 0, 0, 0.06);
  cursor: pointer;
}

.file-card {
  display: flex;
  align-items: center;
  gap: 18rpx;
  min-width: 300rpx;
}

.file-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 74rpx;
  height: 74rpx;
  border-radius: 18rpx;
  background: rgba(13, 92, 82, 0.12);
  color: var(--wy-primary);
  font-size: 28rpx;
  font-weight: 800;
}

.self .file-icon {
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
}

.file-meta {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  min-width: 0;
}

.file-name {
  font-size: 26rpx;
  font-weight: 700;
  line-height: 1.5;
}

.file-size {
  font-size: 22rpx;
  opacity: 0.78;
}

.voice-card {
  display: flex;
  align-items: center;
  gap: 16rpx;
  min-width: 240rpx;
}

.voice-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 68rpx;
  height: 68rpx;
  border-radius: 18rpx;
  background: rgba(13, 92, 82, 0.12);
  color: var(--wy-primary);
  font-size: 28rpx;
  font-weight: 800;
}

.self .voice-icon {
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
}

.voice-meta {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.voice-label {
  font-size: 26rpx;
  font-weight: 700;
}

.voice-duration,
.voice-playing {
  font-size: 22rpx;
  opacity: 0.78;
}

.video-card {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.video-message {
  width: 360rpx;
  height: 240rpx;
  border-radius: 22rpx;
  background: rgba(0, 0, 0, 0.12);
}

.video-duration {
  font-size: 22rpx;
  opacity: 0.78;
}

.location-card,
.card-message {
  display: flex;
  align-items: center;
  gap: 16rpx;
  min-width: 280rpx;
}

.location-icon,
.card-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 18rpx;
  flex-shrink: 0;
}

.location-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(203, 124, 70, 0.14);
  color: var(--wy-accent);
  font-size: 28rpx;
  font-weight: 800;
}

.card-avatar {
  object-fit: cover;
}

.location-meta,
.card-meta {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  min-width: 0;
}

.location-name,
.card-name {
  font-size: 26rpx;
  font-weight: 700;
}

.location-address,
.card-subtitle {
  font-size: 22rpx;
  opacity: 0.78;
  line-height: 1.5;
}

.time {
  font-size: 20rpx;
  opacity: 0.75;
}

.meta-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18rpx;
  margin-top: 10rpx;
}

.status {
  font-size: 20rpx;
  font-weight: 600;
}

.select-indicator {
  font-size: 20rpx;
  color: var(--wy-accent);
}

.sending {
  opacity: 0.82;
}

.failed {
  color: #c54d43;
}

.self .failed {
  color: #ffe2de;
}

.composer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  padding: 14rpx 18rpx 22rpx;
  background: rgba(245, 245, 245, 0.98);
  border-top: 1rpx solid rgba(0, 0, 0, 0.05);
}

.composer-tools {
  display: flex;
  gap: 10rpx;
  flex-wrap: wrap;
}

.tool-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 94rpx;
  height: 56rpx;
  padding: 0 18rpx;
  border-radius: 14rpx;
  background: #ffffff;
  border: 1rpx solid rgba(0, 0, 0, 0.05);
  color: var(--wy-primary);
  font-size: 22rpx;
  font-weight: 700;
  cursor: pointer;
}

.tool-chip-recording {
  background: rgba(197, 77, 67, 0.12);
  color: #c54d43;
}

.tool-chip-active {
  background: var(--wy-primary);
  color: #fff;
}

.emoji-panel {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12rpx;
}

.selection-bar {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.selection-count {
  font-size: 24rpx;
  color: var(--wy-subtext);
}

.selection-actions {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
}

.reply-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
  padding: 12rpx 14rpx;
  border-radius: 14rpx;
  background: #ffffff;
  border: 1rpx solid rgba(0, 0, 0, 0.05);
}

.reply-bar-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.reply-bar-title {
  font-size: 22rpx;
  color: var(--wy-primary);
  font-weight: 700;
}

.reply-bar-text {
  font-size: 22rpx;
  color: var(--wy-subtext);
  line-height: 1.5;
}

.reply-bar-cancel {
  font-size: 22rpx;
  color: var(--wy-accent);
  font-weight: 700;
  cursor: pointer;
}

.mini-select-btn {
  height: 64rpx;
  padding: 0 22rpx;
}

.emoji-item {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 72rpx;
  border-radius: 14rpx;
  background: rgba(255, 255, 255, 0.9);
  font-size: 36rpx;
  cursor: pointer;
}

.composer-main {
  display: flex;
  gap: 10rpx;
  align-items: center;
}

.composer-input {
  flex: 1;
  height: 72rpx;
  padding: 0 18rpx;
  border-radius: 18rpx;
  background: #fff;
  border: 1rpx solid rgba(0, 0, 0, 0.05);
}

.composer-button {
  flex-shrink: 0;
}

.bottom-anchor {
  width: 100%;
  height: 2rpx;
}

@media (min-width: 900px) {
  .conversation-page {
    padding: 24px 24px 0;
  }

  .search-bar,
  .pinned-bar,
  .message-list {
    width: 100%;
    max-width: 1120px;
    margin-left: auto;
    margin-right: auto;
  }

  .composer {
    left: 50%;
    right: auto;
    width: min(100%, 1120px);
    transform: translateX(-50%);
    padding: 16px 20px 24px;
    border-radius: 22px 22px 0 0;
    box-shadow: 0 -10px 30px rgba(67, 59, 42, 0.08);
  }

  .bubble {
    max-width: 60%;
  }

  .video-message {
    width: 320px;
    height: 214px;
  }
}
</style>
