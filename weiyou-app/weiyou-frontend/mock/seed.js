export const mockProfile = {
  id: 10001,
  nickname: "陈微",
  wechatId: "weiyou_chenwei",
  avatar: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=300&q=80",
  city: "杭州",
  signature: "把聊天、生活、支付和协作放进一个温和的超级应用里。"
};

export const mockConversations = [
  {
    id: "chat-001",
    title: "产品群",
    avatar: "https://images.unsplash.com/photo-1521737604893-d14cc237f11d?auto=format&fit=crop&w=300&q=80",
    preview: "今晚把新版本体验问题收一下，明早同步节奏。",
    time: "20:32",
    unread: 9,
    muted: false,
    pinned: true,
    type: "group"
  },
  {
    id: "chat-002",
    title: "林夏",
    avatar: "https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?auto=format&fit=crop&w=300&q=80",
    preview: "我把旅行照片传你了，记得挑几张发朋友圈。",
    time: "18:10",
    unread: 2,
    muted: false,
    pinned: false,
    type: "single"
  },
  {
    id: "chat-003",
    title: "文件传输助手",
    avatar: "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=300&q=80",
    preview: "拖入文件、图片或收藏内容，跨端同步更方便。",
    time: "昨天",
    unread: 0,
    muted: true,
    pinned: false,
    type: "assistant"
  },
  {
    id: "chat-004",
    title: "设计协作",
    avatar: "https://images.unsplash.com/photo-1517048676732-d65bc937f952?auto=format&fit=crop&w=300&q=80",
    preview: "新图标集已经整理到 Figma，欢迎直接评论。",
    time: "昨天",
    unread: 3,
    muted: true,
    pinned: false,
    type: "group"
  }
];

export const mockMessages = {
  "chat-001": [
    {
      id: "msg-001",
      conversationId: "chat-001",
      senderId: 10011,
      senderName: "周铭",
      content: "大家晚点把反馈统一发群里，避免遗漏。",
      type: "text",
      time: "20:01",
      self: false
    },
    {
      id: "msg-002",
      conversationId: "chat-001",
      senderId: 10001,
      senderName: "陈微",
      content: "我会把首页、聊天、发现三块整理成清单。",
      type: "text",
      time: "20:08",
      self: true
    },
    {
      id: "msg-003",
      conversationId: "chat-001",
      senderId: 10017,
      senderName: "沈知",
      content: "OK，我补一下钱包与服务入口的建议稿。",
      type: "text",
      time: "20:12",
      self: false
    }
  ],
  "chat-002": [
    {
      id: "msg-011",
      conversationId: "chat-002",
      senderId: 10002,
      senderName: "林夏",
      content: "周末去看展吗？顺便把上次拍的照片给你。",
      type: "text",
      time: "17:55",
      self: false
    },
    {
      id: "msg-012",
      conversationId: "chat-002",
      senderId: 10001,
      senderName: "陈微",
      content: "可以，晚上我也把路线发你。",
      type: "text",
      time: "17:58",
      self: true
    }
  ],
  "chat-003": [
    {
      id: "msg-021",
      conversationId: "chat-003",
      senderId: 0,
      senderName: "文件传输助手",
      content: "支持跨设备同步图片、笔记、文件与收藏内容。",
      type: "text",
      time: "昨天",
      self: false
    }
  ],
  "chat-004": [
    {
      id: "msg-031",
      conversationId: "chat-004",
      senderId: 10008,
      senderName: "赵越",
      content: "品牌页配色我想再压一点饱和度。",
      type: "text",
      time: "昨天",
      self: false
    }
  ]
};

export const mockContactGroups = [
  {
    title: "常用入口",
    items: [
      {
        id: 1,
        name: "新的朋友",
        avatar: "https://images.unsplash.com/photo-1516321497487-e288fb19713f?auto=format&fit=crop&w=300&q=80",
        alias: "申请与通知",
        tag: "系统",
        online: true
      },
      {
        id: 2,
        name: "群聊",
        avatar: "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=300&q=80",
        alias: "团队与兴趣群",
        tag: "社交",
        online: true
      },
      {
        id: 3,
        name: "标签",
        avatar: "https://images.unsplash.com/photo-1492724441997-5dc865305da7?auto=format&fit=crop&w=300&q=80",
        alias: "人群分组管理",
        tag: "效率",
        online: false
      },
      {
        id: 4,
        name: "公众号",
        avatar: "https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=300&q=80",
        alias: "服务通知与内容订阅",
        tag: "生态",
        online: true
      }
    ]
  },
  {
    title: "A",
    items: [
      {
        id: 101,
        name: "安禾",
        avatar: "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=300&q=80",
        alias: "摄影搭子",
        tag: "好友",
        online: true
      }
    ]
  },
  {
    title: "L",
    items: [
      {
        id: 102,
        name: "林夏",
        avatar: "https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?auto=format&fit=crop&w=300&q=80",
        alias: "旅行和展览",
        tag: "好友",
        online: true
      }
    ]
  },
  {
    title: "Z",
    items: [
      {
        id: 103,
        name: "周铭",
        avatar: "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=300&q=80",
        alias: "产品负责人",
        tag: "同事",
        online: false
      },
      {
        id: 104,
        name: "赵越",
        avatar: "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=300&q=80",
        alias: "设计师",
        tag: "同事",
        online: true
      }
    ]
  }
];

export const mockMoments = [
  {
    id: "moment-001",
    author: {
      name: "林夏",
      avatar: "https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?auto=format&fit=crop&w=300&q=80"
    },
    content: "周末的展览和咖啡都很好，城市在傍晚的时候最柔和。",
    images: [
      "https://images.unsplash.com/photo-1494526585095-c41746248156?auto=format&fit=crop&w=600&q=80",
      "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=600&q=80",
      "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=600&q=80"
    ],
    time: "1小时前",
    location: "杭州·天目里",
    likeCount: 31,
    commentCount: 8
  },
  {
    id: "moment-002",
    author: {
      name: "周铭",
      avatar: "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=300&q=80"
    },
    content: "新版本体验评审结束，准备把复杂路径再打磨一轮。",
    images: [],
    time: "今天 10:28",
    location: "公司会议室",
    likeCount: 12,
    commentCount: 4
  }
];

export const mockWallet = {
  balance: "2688.50",
  monthlySpend: "4821.00",
  monthlyIncome: "9300.00",
  actions: [
    {
      title: "收付款",
      icon: "￥",
      desc: "扫码、被扫、向朋友付款"
    },
    {
      title: "零钱",
      icon: "余",
      desc: "余额管理与提现"
    },
    {
      title: "银行卡",
      icon: "卡",
      desc: "绑卡、扣款顺序、限额"
    },
    {
      title: "账单",
      icon: "单",
      desc: "月度支出与分类明细"
    }
  ],
  bills: [
    {
      title: "咖啡店消费",
      amount: "-32.00",
      time: "今天 08:41",
      status: "支付成功"
    },
    {
      title: "朋友转账",
      amount: "+188.00",
      time: "昨天 20:15",
      status: "已收款"
    },
    {
      title: "城市出行",
      amount: "-18.50",
      time: "昨天 18:10",
      status: "支付成功"
    }
  ]
};

export const mockDiscoverySections = [
  {
    title: "内容与社交",
    items: [
      {
        code: "moments",
        title: "朋友圈",
        subtitle: "图文视频动态、评论、点赞",
        route: "/pages/moments/index"
      },
      {
        code: "channels",
        title: "视频号",
        subtitle: "短视频、直播、推荐流",
        route: "/pages/feature-hub/index?title=视频号"
      },
      {
        code: "search",
        title: "搜一搜",
        subtitle: "人、群、文章、小程序搜索",
        route: "/pages/feature-hub/index?title=搜一搜"
      }
    ]
  },
  {
    title: "工具与生态",
    items: [
      {
        code: "scan",
        title: "扫一扫",
        subtitle: "扫码支付、登录、识别设备",
        route: "/pages/feature-hub/index?title=扫一扫"
      },
      {
        code: "miniapp",
        title: "小程序",
        subtitle: "轻应用分发与服务闭环",
        route: "/pages/feature-hub/index?title=小程序"
      },
      {
        code: "official",
        title: "公众号",
        subtitle: "内容订阅、服务通知、客服消息",
        route: "/pages/feature-hub/index?title=公众号"
      }
    ]
  }
];

export const mockWorkbenchSections = [
  {
    title: "即时通讯",
    items: [
      {
        code: "im-single",
        title: "单聊",
        subtitle: "文本、图片、语音、文件、位置",
        route: "/pages/chat/index",
        status: "已建骨架"
      },
      {
        code: "im-group",
        title: "群聊",
        subtitle: "群公告、群管理、群工具",
        route: "/pages/chat/index",
        status: "已建骨架"
      },
      {
        code: "im-call",
        title: "音视频通话",
        subtitle: "后续接入 WebRTC",
        route: "/pages/feature-hub/index?title=音视频通话",
        status: "预留扩展"
      }
    ]
  },
  {
    title: "社交关系",
    items: [
      {
        code: "contacts",
        title: "好友与标签",
        subtitle: "备注、分组、拉黑、隐私权限",
        route: "/pages/contacts/index",
        status: "已建骨架"
      },
      {
        code: "moments",
        title: "朋友圈",
        subtitle: "动态流、评论、点赞、发布",
        route: "/pages/moments/index",
        status: "已建骨架"
      },
      {
        code: "status",
        title: "状态",
        subtitle: "在线状态、心情、位置状态",
        route: "/pages/feature-hub/index?title=状态",
        status: "预留扩展"
      }
    ]
  },
  {
    title: "商业与支付",
    items: [
      {
        code: "wallet",
        title: "钱包",
        subtitle: "余额、账单、卡包、银行卡",
        route: "/pages/wallet/index",
        status: "已建骨架"
      },
      {
        code: "pay",
        title: "支付",
        subtitle: "收付款、商户、风控、交易链路",
        route: "/pages/wallet/index",
        status: "已建骨架"
      },
      {
        code: "service",
        title: "生活服务",
        subtitle: "城市服务、出行、缴费、票务",
        route: "/pages/feature-hub/index?title=生活服务",
        status: "预留扩展"
      }
    ]
  },
  {
    title: "内容生态",
    items: [
      {
        code: "miniapp",
        title: "小程序",
        subtitle: "轻应用容器、授权、消息能力",
        route: "/pages/feature-hub/index?title=小程序",
        status: "预留扩展"
      },
      {
        code: "official",
        title: "公众号",
        subtitle: "内容分发、客服、订阅通知",
        route: "/pages/feature-hub/index?title=公众号",
        status: "预留扩展"
      },
      {
        code: "channels",
        title: "视频号",
        subtitle: "短视频、直播、电商导流",
        route: "/pages/feature-hub/index?title=视频号",
        status: "预留扩展"
      }
    ]
  },
  {
    title: "办公与设备",
    items: [
      {
        code: "assistant",
        title: "文件传输助手",
        subtitle: "跨端同步资料与收藏",
        route: "/pages/chat/index",
        status: "已建骨架"
      },
      {
        code: "device",
        title: "设备管理",
        subtitle: "登录设备、通知、授权、安全控制",
        route: "/pages/feature-hub/index?title=设备管理",
        status: "预留扩展"
      },
      {
        code: "office",
        title: "企业协同",
        subtitle: "审批、待办、内部通讯录入口",
        route: "/pages/feature-hub/index?title=企业协同",
        status: "预留扩展"
      }
    ]
  }
];
