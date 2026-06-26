const OFFICIAL_FOLLOW_KEY = "weiyou_official_follow_state";
const OFFICIAL_ARTICLE_LIKE_KEY = "weiyou_official_article_like_state";
const MINI_APP_RECENT_KEY = "weiyou_mini_app_recent";
const MINI_APP_FAVORITE_KEY = "weiyou_mini_app_favorite";

const OFFICIAL_ACCOUNTS = [
  {
    officialId: 20001,
    name: "微友服务号",
    avatar: "https://weiyou.local/official/service.png",
    intro: "微友官方服务、活动与消息通知入口",
    verified: true,
    followed: true
  },
  {
    officialId: 20002,
    name: "微友出行",
    avatar: "https://weiyou.local/official/travel.png",
    intro: "行程提醒、车票进度和城市出行服务通知。",
    verified: true,
    followed: false
  },
  {
    officialId: 20003,
    name: "微友生活",
    avatar: "https://weiyou.local/official/life.png",
    intro: "城市活动、附近优惠和生活灵感内容精选。",
    verified: false,
    followed: false
  }
];

const OFFICIAL_ARTICLES = [
  {
    articleId: 300001,
    officialId: 20001,
    title: "微友支付体验升级说明",
    summary: "围绕账单可读性、支付成功反馈和红包体验做了一轮优化。",
    cover: "https://weiyou.local/official/article-cover-1.png",
    publishAt: "2026-05-11 10:00",
    likeCount: 126,
    contentHtml: "<h3>升级内容</h3><p>本次版本主要优化支付状态反馈、账单摘要表达与红包打开动效占位能力。</p><p>后续还会继续补齐多端同步和风控提示。</p>"
  },
  {
    articleId: 300002,
    officialId: 20001,
    title: "微友即时通讯能力演进路线",
    summary: "从基础单聊群聊，到多端同步、回执和音视频能力的阶段规划。",
    cover: "https://weiyou.local/official/article-cover-2.png",
    publishAt: "2026-05-10 18:30",
    likeCount: 88,
    contentHtml: "<h3>演进路线</h3><p>先打通 HTTP + WebSocket 的联调链路，再逐步接入可靠投递、回执和消息撤回能力。</p>"
  },
  {
    articleId: 300101,
    officialId: 20002,
    title: "周末出行提醒合集",
    summary: "汇总出行高峰、路线建议与车票状态通知体验。",
    cover: "https://weiyou.local/official/article-cover-3.png",
    publishAt: "2026-05-09 09:20",
    likeCount: 42,
    contentHtml: "<h3>周末出行</h3><p>高峰时段建议提前安排路线，后续将接入城市服务能力。</p>"
  }
];

const MINI_APP_LIBRARY = [
  {
    appId: "wx-demo-001",
    appName: "微友商城",
    iconUrl: "https://weiyou.local/miniapp/shop.png",
    path: "/pages/index/index"
  },
  {
    appId: "wx-demo-002",
    appName: "微友打车",
    iconUrl: "https://weiyou.local/miniapp/travel.png",
    path: "/pages/home/index"
  },
  {
    appId: "wx-demo-003",
    appName: "微友会议室",
    iconUrl: "https://weiyou.local/miniapp/meeting.png",
    path: "/pages/booking/index"
  }
];

function clone(value) {
  return JSON.parse(JSON.stringify(value));
}

function nowText() {
  return new Date().toISOString();
}

function readStorage(key, fallbackValue) {
  const value = uni.getStorageSync(key);
  if (value === "" || value === null || typeof value === "undefined") {
    return clone(fallbackValue);
  }
  try {
    return clone(value);
  } catch (error) {
    return clone(fallbackValue);
  }
}

function writeStorage(key, value) {
  uni.setStorageSync(key, clone(value));
}

function readOfficialFollowState() {
  return readStorage(OFFICIAL_FOLLOW_KEY, {});
}

function writeOfficialFollowState(state) {
  writeStorage(OFFICIAL_FOLLOW_KEY, state);
}

function readOfficialArticleLikeState() {
  return readStorage(OFFICIAL_ARTICLE_LIKE_KEY, {});
}

function writeOfficialArticleLikeState(state) {
  writeStorage(OFFICIAL_ARTICLE_LIKE_KEY, state);
}

function readMiniAppRecentList() {
  return readStorage(MINI_APP_RECENT_KEY, [
    {
      appId: "wx-demo-001",
      path: "/pages/index/index",
      lastUsedAt: "2026-05-11 10:30"
    },
    {
      appId: "wx-demo-002",
      path: "/pages/home/index",
      lastUsedAt: "2026-05-10 18:05"
    }
  ]);
}

function writeMiniAppRecentList(list) {
  writeStorage(MINI_APP_RECENT_KEY, list);
}

function readMiniAppFavoriteIds() {
  return readStorage(MINI_APP_FAVORITE_KEY, ["wx-demo-001"]);
}

function writeMiniAppFavoriteIds(ids) {
  writeStorage(MINI_APP_FAVORITE_KEY, ids);
}

function resolveOfficialBase(officialId) {
  return OFFICIAL_ACCOUNTS.find((item) => Number(item.officialId) === Number(officialId)) || {
    officialId: Number(officialId) || 20001,
    name: "微友服务号",
    avatar: "https://weiyou.local/official/service.png",
    intro: "微友官方服务、活动与消息通知入口",
    verified: true,
    followed: false
  };
}

function applyOfficialFollowState(item) {
  const followState = readOfficialFollowState();
  return {
    ...item,
    followed: typeof followState[item.officialId] === "boolean" ? followState[item.officialId] : Boolean(item.followed)
  };
}

function resolveMiniAppBase(appId) {
  return MINI_APP_LIBRARY.find((item) => item.appId === appId) || {
    appId,
    appName: "微友小程序",
    iconUrl: "https://weiyou.local/miniapp/default.png",
    path: "/pages/index/index"
  };
}

function decorateMiniApp(item) {
  const base = resolveMiniAppBase(item.appId);
  const favoriteIds = readMiniAppFavoriteIds();
  return {
    ...base,
    ...item,
    favorite: favoriteIds.includes(item.appId)
  };
}

export function listOfficialAccounts(keyword = "") {
  const text = String(keyword || "").trim().toLowerCase();
  const list = OFFICIAL_ACCOUNTS.map(applyOfficialFollowState);
  if (!text) {
    return list;
  }
  return list.filter((item) => {
    return item.name.toLowerCase().includes(text) || item.intro.toLowerCase().includes(text);
  });
}

export function resolveOfficialAccount(officialId) {
  return applyOfficialFollowState(resolveOfficialBase(officialId));
}

export function toggleOfficialFollow(officialId, action) {
  const state = readOfficialFollowState();
  const nextFollowed = action === "follow";
  state[officialId] = nextFollowed;
  writeOfficialFollowState(state);
  return {
    officialId: Number(officialId),
    followed: nextFollowed
  };
}

export function listOfficialArticles(officialId) {
  const likeState = readOfficialArticleLikeState();
  return OFFICIAL_ARTICLES.filter((item) => Number(item.officialId) === Number(officialId)).map((item) => {
    const state = likeState[item.articleId] || {};
    return {
      ...item,
      likeCount: typeof state.likeCount === "number" ? state.likeCount : item.likeCount
    };
  });
}

export function resolveOfficialArticle(articleId) {
  const likeState = readOfficialArticleLikeState();
  const article = OFFICIAL_ARTICLES.find((item) => Number(item.articleId) === Number(articleId)) || OFFICIAL_ARTICLES[0];
  const state = likeState[article.articleId] || {};
  return {
    ...article,
    likeCount: typeof state.likeCount === "number" ? state.likeCount : article.likeCount,
    liked: Boolean(state.liked)
  };
}

export function toggleOfficialArticleLike(articleId, action) {
  const state = readOfficialArticleLikeState();
  const article = resolveOfficialArticle(articleId);
  const liked = action === "like";
  state[article.articleId] = {
    liked,
    likeCount: liked ? article.likeCount + 1 : Math.max(0, article.likeCount - 1)
  };
  writeOfficialArticleLikeState(state);
  return {
    articleId: article.articleId,
    liked,
    likeCount: state[article.articleId].likeCount
  };
}

export function listRecentMiniApps() {
  return readMiniAppRecentList().map(decorateMiniApp);
}

export function listFavoriteMiniApps() {
  const favoriteIds = readMiniAppFavoriteIds();
  const recentMap = new Map(readMiniAppRecentList().map((item) => [item.appId, item]));
  return favoriteIds.map((appId) => decorateMiniApp(recentMap.get(appId) || { appId, lastUsedAt: "", path: resolveMiniAppBase(appId).path }));
}

export function recordRecentMiniApp(session) {
  const recent = readMiniAppRecentList().filter((item) => item.appId !== session.appId);
  const nextItem = {
    appId: session.appId,
    appName: session.appName,
    path: session.path,
    lastUsedAt: nowText()
  };
  recent.unshift(nextItem);
  writeMiniAppRecentList(recent.slice(0, 12));
  return decorateMiniApp(nextItem);
}

export function removeRecentMiniApp(appId) {
  const next = readMiniAppRecentList().filter((item) => item.appId !== appId);
  writeMiniAppRecentList(next);
  return next.map(decorateMiniApp);
}

export function toggleFavoriteMiniApp(appId, action, appMeta = {}) {
  const favoriteIds = new Set(readMiniAppFavoriteIds());
  if (action === "favorite") {
    favoriteIds.add(appId);
  } else {
    favoriteIds.delete(appId);
  }
  writeMiniAppFavoriteIds([...favoriteIds]);

  if (appMeta.appId) {
    const recent = readMiniAppRecentList();
    const exists = recent.some((item) => item.appId === appId);
    if (!exists) {
      recent.unshift({
        appId,
        appName: appMeta.appName,
        path: appMeta.path || resolveMiniAppBase(appId).path,
        lastUsedAt: nowText()
      });
      writeMiniAppRecentList(recent.slice(0, 12));
    }
  }

  return {
    appId,
    favorite: favoriteIds.has(appId)
  };
}

export function createMomentCommentFallback(data) {
  return {
    commentId: Date.now(),
    replyCommentId: data?.replyCommentId || null,
    createdAt: nowText()
  };
}

export function deleteMomentCommentFallback(data) {
  return {
    commentId: data?.commentId || null,
    deleted: true
  };
}

export function markNoticeReadFallback(data) {
  return {
    noticeId: data?.noticeId || null,
    readStatus: 1
  };
}
