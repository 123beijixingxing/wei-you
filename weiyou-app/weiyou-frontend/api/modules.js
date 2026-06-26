import { request, upload } from "./http";

function buildQuery(params = {}) {
  const text = Object.entries(params)
    .filter(([, value]) => value !== null && typeof value !== "undefined" && value !== "")
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
    .join("&");
  return text ? `?${text}` : "";
}

function withQuery(url, params) {
  return `${url}${buildQuery(params)}`;
}

function fileNameFromPath(filePath) {
  const normalized = String(filePath || "").replace(/\\/g, "/");
  const segments = normalized.split("/");
  return segments[segments.length - 1] || `upload-${Date.now()}.jpg`;
}

function inferContentType(filePath) {
  const lowerPath = String(filePath || "").toLowerCase();
  if (lowerPath.endsWith(".png")) {
    return "image/png";
  }
  if (lowerPath.endsWith(".webp")) {
    return "image/webp";
  }
  if (lowerPath.endsWith(".gif")) {
    return "image/gif";
  }
  return "image/jpeg";
}

function inferFileContentType(filePath) {
  const lowerPath = String(filePath || "").toLowerCase();
  if (lowerPath.endsWith(".mp4")) {
    return "video/mp4";
  }
  if (lowerPath.endsWith(".mov")) {
    return "video/quicktime";
  }
  if (lowerPath.endsWith(".webm")) {
    return "video/webm";
  }
  if (lowerPath.endsWith(".pdf")) {
    return "application/pdf";
  }
  if (lowerPath.endsWith(".doc")) {
    return "application/msword";
  }
  if (lowerPath.endsWith(".docx")) {
    return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
  }
  if (lowerPath.endsWith(".xls")) {
    return "application/vnd.ms-excel";
  }
  if (lowerPath.endsWith(".xlsx")) {
    return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
  }
  if (lowerPath.endsWith(".ppt")) {
    return "application/vnd.ms-powerpoint";
  }
  if (lowerPath.endsWith(".pptx")) {
    return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
  }
  if (lowerPath.endsWith(".zip")) {
    return "application/zip";
  }
  if (lowerPath.endsWith(".txt") || lowerPath.endsWith(".md")) {
    return "text/plain";
  }
  return "application/octet-stream";
}

function normalizeContactListArgs(payload, letter) {
  if (typeof payload === "object" && payload !== null) {
    return {
      keyword: payload.keyword || "",
      letter: payload.letter || ""
    };
  }
  return {
    keyword: payload || "",
    letter: letter || ""
  };
}

function normalizeWalletBillArgs(payload) {
  if (typeof payload === "object" && payload !== null) {
    return {
      pageNo: payload.pageNo || 1,
      type: payload.type || "",
      startDate: payload.startDate || "",
      endDate: payload.endDate || ""
    };
  }
  return {
    pageNo: payload || 1
  };
}

export const authApi = {
  // 密码登录
  loginByPassword(data) {
    return request({
      url: "/auth/login/password",
      method: "POST",
      data,
      skipAuth: true
    });
  },
  // 短信验证码登录
  loginBySms(data) {
    return request({
      url: "/auth/login/sms",
      method: "POST",
      data,
      skipAuth: true
    });
  },
  // 注册
  register(data) {
    return request({
      url: "/auth/register",
      method: "POST",
      data,
      skipAuth: true
    });
  },
  // 发送短信验证码
  sendSms(data) {
    return request({
      url: "/auth/sms/send",
      method: "POST",
      data,
      skipAuth: true
    });
  },
  // 刷新登录令牌
  refreshToken(data) {
    return request({
      url: "/auth/token/refresh",
      method: "POST",
      data,
      skipAuth: true
    });
  },
  // 获取登录设备列表
  devices() {
    return request({
      url: "/auth/device/list"
    });
  },
  // 下线指定设备
  offlineDevice(data) {
    return request({
      url: "/auth/device/offline",
      method: "POST",
      data
    });
  },
  // 退出登录
  logout(data) {
    return request({
      url: "/auth/logout",
      method: "POST",
      data
    });
  }
};

export const userApi = {
  // 获取当前用户资料
  me() {
    return request({
      url: "/user/profile/me"
    });
  },
  // 获取指定用户资料详情
  detail(userId) {
    return request({
      url: withQuery("/user/profile/detail", { userId })
    });
  },
  // 更新个人资料
  updateProfile(data) {
    return request({
      url: "/user/profile/update",
      method: "POST",
      data
    });
  },
  // 获取个人二维码
  qrcode(dynamic = false) {
    return request({
      url: withQuery("/user/qrcode/get", { dynamic })
    });
  },
  // 更新个人状态
  updateStatus(data) {
    return request({
      url: "/user/status/update",
      method: "POST",
      data
    });
  },
  // 获取设置详情
  settingDetail() {
    return request({
      url: "/user/setting/detail"
    });
  },
  // 更新设置项
  updateSetting(data) {
    return request({
      url: "/user/setting/update",
      method: "POST",
      data
    });
  }
};

export const contactApi = {
  // 获取通讯录列表
  list(payload = "", letter = "") {
    return request({
      url: withQuery("/contact/list", normalizeContactListArgs(payload, letter))
    });
  },
  // 搜索联系人或候选好友
  search(keyword) {
    return request({
      url: withQuery("/contact/search", { keyword })
    });
  },
  // 发起好友申请
  applyFriend(data) {
    return request({
      url: "/contact/friend/apply",
      method: "POST",
      data
    });
  },
  // 获取新的朋友列表
  friendRequests(status) {
    return request({
      url: withQuery("/contact/friend/request/list", { status })
    });
  },
  // 处理好友申请
  handleFriendRequest(data) {
    return request({
      url: "/contact/friend/request/handle",
      method: "POST",
      data
    });
  }
};

export const chatApi = {
  // 获取会话列表
  conversations(params = {}) {
    return request({
      url: withQuery("/chat/conversation/list", {
        cursor: params.cursor || "",
        pageSize: params.pageSize || 20
      })
    });
  },
  // 打开单聊会话
  openSingleConversation(targetUserId) {
    return request({
      url: "/chat/conversation/open-single",
      method: "POST",
      data: { targetUserId }
    });
  },
  // 获取会话详情
  conversationDetail(conversationId) {
    return request({
      url: withQuery("/chat/conversation/detail", { conversationId })
    });
  },
  // 更新会话设置
  updateConversationSetting(data) {
    return request({
      url: "/chat/conversation/setting/update",
      method: "POST",
      data
    });
  },
  // 清空会话记录
  clearConversation(data) {
    return request({
      url: "/chat/conversation/clear",
      method: "POST",
      data
    });
  },
  // 获取消息历史
  messages(conversationId, params = {}) {
    return request({
      url: withQuery("/chat/message/history", {
        conversationId,
        cursor: params.cursor || "",
        pageSize: params.pageSize || 20
      })
    });
  },
  // 搜索会话内消息
  searchMessages(conversationId, keyword, pageSize = 20) {
    return request({
      url: withQuery("/chat/message/search", {
        conversationId,
        keyword,
        pageSize
      })
    });
  },
  // 发送消息
  sendMessage(data) {
    return request({
      url: "/chat/message/send",
      method: "POST",
      data
    });
  },
  // 标记消息已读
  markRead(data) {
    return request({
      url: "/chat/message/read",
      method: "POST",
      data
    });
  },
  // 撤回消息
  revokeMessage(data) {
    return request({
      url: "/chat/message/revoke",
      method: "POST",
      data
    });
  }
};

export const groupApi = {
  // 获取我的群聊列表
  list(keyword = "") {
    return request({
      url: withQuery("/group/my/list", { keyword })
    });
  },
  // 创建群聊
  create(data) {
    return request({
      url: "/group/create",
      method: "POST",
      data
    });
  },
  // 获取群详情
  detail(groupId) {
    return request({
      url: withQuery("/group/detail", { groupId })
    });
  },
  // 邀请群成员
  inviteMembers(data) {
    return request({
      url: "/group/member/invite",
      method: "POST",
      data
    });
  },
  // 获取群成员列表
  members(groupId) {
    return request({
      url: withQuery("/group/member/list", { groupId })
    });
  },
  // 更新群公告
  updateNotice(data) {
    return request({
      url: "/group/notice/update",
      method: "POST",
      data
    });
  },
  // 更新群设置
  updateSetting(data) {
    return request({
      url: "/group/setting/update",
      method: "POST",
      data
    });
  },
  // 设置或取消管理员
  updateAdmin(data) {
    return request({
      url: "/group/admin/update",
      method: "POST",
      data
    });
  },
  // 设置成员禁言
  updateMemberMute(data) {
    return request({
      url: "/group/member/mute",
      method: "POST",
      data
    });
  },
  // 修改群昵称
  updateNickname(data) {
    return request({
      url: "/group/member/nickname/update",
      method: "POST",
      data
    });
  },
  // 移除群成员
  removeMember(data) {
    return request({
      url: "/group/member/remove",
      method: "POST",
      data
    });
  },
  // 转让群主
  transferOwner(data) {
    return request({
      url: "/group/owner/transfer",
      method: "POST",
      data
    });
  },
  // 退出群聊
  leave(data) {
    return request({
      url: "/group/leave",
      method: "POST",
      data
    });
  }
};

export const momentApi = {
  // 获取朋友圈时间线
  timeline(params = {}) {
    return request({
      url: withQuery("/moment/timeline", {
        cursor: params.cursor || "",
        pageSize: params.pageSize || 20
      })
    });
  },
  // 点赞或取消点赞
  toggleLike(data) {
    return request({
      url: "/moment/like",
      method: "POST",
      data
    });
  },
  // 发表评论或回复
  comment(data) {
    return request({
      url: "/moment/comment/create",
      method: "POST",
      data
    });
  },
  // 删除评论
  deleteComment(data) {
    return request({
      url: "/moment/comment/delete",
      method: "POST",
      data
    });
  },
  // 发布朋友圈动态
  create(data) {
    return request({
      url: "/moment/create",
      method: "POST",
      data
    });
  }
};

export const walletApi = {
  // 获取钱包概览
  overview() {
    return request({
      url: "/wallet/overview"
    });
  },
  // 获取账单列表
  bills(payload = 1) {
    return request({
      url: withQuery("/wallet/bill/list", normalizeWalletBillArgs(payload))
    });
  },
  // 发起转账
  transfer(data) {
    return request({
      url: "/wallet/transfer/create",
      method: "POST",
      data
    });
  },
  // 创建红包
  createRedPacket(data) {
    return request({
      url: "/wallet/red-packet/create",
      method: "POST",
      data
    });
  },
  // 领取红包
  openRedPacket(data) {
    return request({
      url: "/wallet/red-packet/open",
      method: "POST",
      data
    });
  }
};

export const collectionApi = {
  // 获取收藏列表
  list(type = "", keyword = "", pageNo = 1) {
    return request({
      url: withQuery("/collection/list", { type, keyword, pageNo })
    });
  },
  // 删除收藏项
  deleteItem(data) {
    return request({
      url: "/collection/delete",
      method: "POST",
      data
    });
  },
  // 新建收藏项
  createItem(data) {
    return request({
      url: "/collection/create",
      method: "POST",
      data
    });
  }
};

export const cardApi = {
  // 获取卡包列表
  list(cardType = "") {
    return request({
      url: withQuery("/card/list", { cardType })
    });
  },
  // 核销卡券
  use(data) {
    return request({
      url: "/card/use",
      method: "POST",
      data
    });
  }
};

export const emojiApi = {
  // 获取表情商店列表
  list() {
    return request({
      url: "/emoji/store/list"
    });
  },
  // 下载表情包
  download(data) {
    return request({
      url: "/emoji/package/download",
      method: "POST",
      data
    });
  },
  // 卸载表情包
  remove(data) {
    return request({
      url: "/emoji/package/remove",
      method: "POST",
      data
    });
  },
  // 设为当前表情包
  activate(data) {
    return request({
      url: "/emoji/package/activate",
      method: "POST",
      data
    });
  }
};

export const noticeApi = {
  // 获取通知列表
  list(type = "", cursor = "") {
    return request({
      url: withQuery("/notice/list", { type, cursor })
    });
  },
  // 标记通知已读
  read(data) {
    return request({
      url: "/notice/read",
      method: "POST",
      data
    });
  }
};

export const featureApi = {
  // 获取功能中心入口
  workbench(scene = "") {
    return request({
      url: withQuery("/feature/workbench", { scene })
    });
  },
  // 获取发现页入口
  discovery(cityCode = "440300") {
    return request({
      url: withQuery("/feature/discovery", { cityCode })
    });
  }
};

export const searchApi = {
  // 获取搜索建议
  suggest(keyword, bizType = "") {
    return request({
      url: withQuery("/search/suggest", { keyword, bizType })
    });
  },
  // 综合搜索
  global(keyword, bizType = "", pageNo = 1) {
    return request({
      url: withQuery("/search/global", { keyword, bizType, pageNo })
    });
  }
};

export const scanApi = {
  // 解析扫码内容
  resolve(data) {
    return request({
      url: "/scan/resolve",
      method: "POST",
      data
    });
  }
};

export const officialApi = {
  // 获取公众号列表
  list(keyword = "") {
    return request({
      url: withQuery("/official/account/list", { keyword })
    });
  },
  // 获取公众号详情
  detail(officialId) {
    return request({
      url: withQuery("/official/account/detail", { officialId })
    });
  },
  // 获取公众号历史消息
  history(officialId, cursor = "") {
    return request({
      url: withQuery("/official/article/history", { officialId, cursor })
    });
  },
  // 获取公众号文章详情
  article(articleId) {
    return request({
      url: withQuery("/official/article/detail", { articleId })
    });
  },
  // 点赞公众号文章
  likeArticle(data) {
    return request({
      url: "/official/article/like",
      method: "POST",
      data
    });
  },
  // 关注或取消关注公众号
  follow(data) {
    return request({
      url: "/official/account/follow",
      method: "POST",
      data
    });
  }
};

export const miniAppApi = {
  // 获取最近使用的小程序
  recent() {
    return request({
      url: "/miniapp/recent/list"
    });
  },
  // 获取收藏的小程序列表
  favorites() {
    return request({
      url: "/miniapp/favorite/list"
    });
  },
  // 打开小程序
  open(data) {
    return request({
      url: "/miniapp/open",
      method: "POST",
      data
    });
  },
  // 移除最近使用记录
  removeRecent(data) {
    return request({
      url: "/miniapp/recent/remove",
      method: "POST",
      data
    });
  },
  // 收藏或取消收藏小程序
  toggleFavorite(data) {
    return request({
      url: "/miniapp/favorite/toggle",
      method: "POST",
      data
    });
  }
};

export const mediaApi = {
  // 获取上传策略
  uploadPolicy(data) {
    return request({
      url: "/media/upload/policy",
      method: "POST",
      data
    });
  },
  // 上传本地图片
  async uploadLocalImage(filePath, bizType = "moment") {
    const result = await upload({
      url: "/media/upload/local",
      filePath,
      name: "file",
      formData: {
        bizType
      }
    });
    return {
      ...result,
      fileName: result?.originName || fileNameFromPath(filePath),
      contentType: inferContentType(filePath),
      uploaded: true,
      localPreview: false
    };
  },
  // 上传本地文件
  async uploadLocalFile(filePath, bizType = "chat-file") {
    const result = await upload({
      url: "/media/upload/local",
      filePath,
      name: "file",
      formData: {
        bizType
      }
    });
    return {
      ...result,
      fileName: result?.originName || fileNameFromPath(filePath),
      contentType: inferFileContentType(filePath),
      uploaded: true,
      localPreview: false
    };
  },
  // 上传本地视频
  async uploadLocalVideo(filePath, bizType = "chat-video") {
    const result = await upload({
      url: "/media/upload/local",
      filePath,
      name: "file",
      formData: {
        bizType
      }
    });
    return {
      ...result,
      fileName: result?.originName || fileNameFromPath(filePath),
      contentType: inferFileContentType(filePath),
      uploaded: true,
      localPreview: false
    };
  }
};

export const appApi = {
  // 获取应用启动配置
  bootstrap(clientType = "iOS", version = "0.1.0") {
    return request({
      url: withQuery("/app/bootstrap", { clientType, version }),
      skipAuth: true
    });
  }
};
