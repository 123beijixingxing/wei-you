const MODULE_META = {
  "登录与启动": {
    description: "账号进入、冷启动、登录态恢复与首次引导。",
    focus: ["冷启动", "账号认证", "协议确认"]
  },
  "即时通讯": {
    description: "消息列表、单聊详情、会话设置与会话状态反馈。",
    focus: ["消息发送", "多媒体", "会话管理"]
  },
  "社交关系": {
    description: "联系人、好友申请、群聊组织与关系沉淀。",
    focus: ["搜索找人", "新的朋友", "群管理"]
  },
  "发现与内容": {
    description: "朋友圈、公众号、搜索、扫一扫与通知消费。",
    focus: ["内容流", "阅读转化", "搜索导航"]
  },
  "支付与交易": {
    description: "钱包、转账、红包、账单与卡包能力。",
    focus: ["支付操作", "交易回查", "余额安全"]
  },
  "生态与服务": {
    description: "功能中心、小程序、收藏与表情商店等扩展能力。",
    focus: ["能力矩阵", "轻服务", "内容收藏"]
  },
  "个人中心": {
    description: "个人资料、二维码、状态表达与个人资产入口。",
    focus: ["身份展示", "资料编辑", "个人资产"]
  },
  "设置与安全": {
    description: "通知、隐私、设备、安全与退出登录管理。",
    focus: ["开关配置", "设备安全", "隐私边界"]
  }
};

const PAGES = [
  {
    id: "auth-login",
    module: "登录与启动",
    route: "pages/auth/login",
    title: "登录 / 注册",
    summary: "统一承载密码登录、短信登录、注册开户与登录失效回跳。",
    entry: ["冷启动默认进入", "登录态过期自动回跳", "从退出登录返回"],
    primary: ["密码登录", "短信登录", "注册新账号", "验证码发送"],
    states: ["空表单", "短信倒计时", "提交中", "登录成功回跳"],
    interactions: [
      "切换登录模式时保留手机号和已输入上下文，减少重复输入。",
      "主按钮文案随当前模式变化，明确用户下一步动作。",
      "登录成功后优先回到用户原目标页面，否则进入微信首页。"
    ],
    related: ["chat-home", "discover-home", "me-home"]
  },
  {
    id: "chat-home",
    module: "即时通讯",
    route: "pages/chat/index",
    title: "微信消息首页",
    summary: "展示最近会话、未读数、快捷入口与会话搜索。",
    entry: ["登录成功后的默认主 Tab", "通知点击回到会话首页", "底部 Tab 切换进入"],
    primary: ["搜索会话", "打开单聊", "打开群聊", "进入快捷入口"],
    states: ["会话列表有数据", "无会话空状态", "Socket 未连接", "搜索过滤"],
    interactions: [
      "顶部摘要同步展示未读消息、会话数量和连接状态。",
      "会话卡同时承载置顶、免打扰、未读角标等关系状态。",
      "无会话时提供添加朋友、扫一扫等推荐动作，而不是纯留白。"
    ],
    related: ["conversation-detail", "contacts-add-friend", "scan-home"]
  },
  {
    id: "conversation-detail",
    module: "即时通讯",
    route: "pages/conversation/detail",
    title: "单聊 / 群聊详情",
    summary: "承载文本、图片、视频、文件、语音、位置、名片等消息形态。",
    entry: ["从消息首页打开会话", "从联系人资料发消息", "从群详情回到聊天"],
    primary: ["发送文本", "发送多媒体", "回复消息", "搜索消息", "多选消息"],
    states: ["置顶消息", "回复态", "搜索态", "录音中", "发送失败重试"],
    interactions: [
      "消息气泡区分自己与对方，支持引用回复和撤回后的回显。",
      "底部工具条整合图片、文件、位置、名片、表情和语音输入。",
      "桌面 H5 下输入区保持底部固定，并限制消息宽度提升阅读性。"
    ],
    related: ["chat-home", "conversation-settings", "contacts-profile"]
  },
  {
    id: "conversation-settings",
    module: "即时通讯",
    route: "pages/conversation/settings",
    title: "聊天设置",
    summary: "围绕置顶、免打扰、标未读和清空聊天记录的会话级设置页。",
    entry: ["会话详情右上角进入", "消息页工具条进入"],
    primary: ["置顶会话", "开启免打扰", "标为未读", "清空聊天记录"],
    states: ["会话开关读写", "未读数展示", "清空记录确认", "处理中"],
    interactions: [
      "会话开关即时写回后端，并同步刷新消息首页状态。",
      "危险动作与普通设置分区展示，避免误触。",
      "记录清空后仍保留会话本身，后续新消息从新时间点开始。"
    ],
    related: ["conversation-detail", "chat-home"]
  },
  {
    id: "contacts-home",
    module: "社交关系",
    route: "pages/contacts/index",
    title: "通讯录首页",
    summary: "按字母分组呈现联系人，同时提供新的朋友、添加朋友和群聊入口。",
    entry: ["底部 Tab 进入", "加好友成功后回到通讯录", "从群聊入口进入"],
    primary: ["搜索联系人", "查看联系人资料", "查看新的朋友", "添加朋友", "查看群聊"],
    states: ["联系人分组", "无联系人空状态", "搜索过滤", "星标好友概览"],
    interactions: [
      "顶部概览区汇总联系人数量、分组字母和星标好友。",
      "联系人卡强调头像、备注和在线状态，支持直接进入资料页。",
      "空状态提供添加朋友和群聊入口，引导继续构建社交关系。"
    ],
    related: ["contacts-profile", "contacts-add-friend", "contacts-new-friend", "group-list"]
  },
  {
    id: "contacts-profile",
    module: "社交关系",
    route: "pages/contacts/profile",
    title: "联系人资料",
    summary: "展示好友资料、备注、城市、签名，并可加好友或发起会话。",
    entry: ["通讯录点击联系人", "搜索结果点击联系人", "群成员名片进入"],
    primary: ["加为好友", "发消息", "查看微友号", "查看城市与签名"],
    states: ["已是好友", "陌生人资料", "会话已存在", "会话需新建"],
    interactions: [
      "未加好友时主操作强调发起申请，已加好友则切换为发消息。",
      "资料页与会话页通过 conversationId 打通，减少重复建会话。",
      "用户标签、备注与状态文案帮助建立关系认知。"
    ],
    related: ["contacts-home", "contacts-add-friend", "conversation-detail"]
  },
  {
    id: "contacts-add-friend",
    module: "社交关系",
    route: "pages/contacts/add-friend",
    title: "添加朋友",
    summary: "基于昵称、微友号、城市搜索候选人并发起好友申请。",
    entry: ["通讯录首页入口", "新的朋友页二次操作", "搜索结果跳转"],
    primary: ["输入关键词", "查看候选人资料", "发送好友申请"],
    states: ["未输入关键词", "有搜索结果", "无结果空状态", "申请成功"],
    interactions: [
      "搜索关键词可覆盖昵称、微友号和城市，方便多维度找人。",
      "候选人卡片突出昵称、城市和简介，减少误加。",
      "申请动作默认附带自我介绍，提升通过率。"
    ],
    related: ["contacts-new-friend", "contacts-profile", "contacts-home"]
  },
  {
    id: "contacts-new-friend",
    module: "社交关系",
    route: "pages/contacts/new-friend",
    title: "新的朋友",
    summary: "查看待处理好友申请，并执行通过、忽略等操作。",
    entry: ["通讯录首页入口", "通知中心跳转", "添加朋友后回查"],
    primary: ["查看申请人", "通过申请", "忽略申请", "回看处理结果"],
    states: ["待处理申请", "已通过", "已忽略", "空状态"],
    interactions: [
      "申请列表按时间逆序展示，方便优先处理最新关系请求。",
      "处理结果即时回写，并在通讯录中同步出现新联系人。",
      "通过后可立即进入资料页或发起会话。"
    ],
    related: ["contacts-home", "contacts-profile", "contacts-add-friend"]
  },
  {
    id: "group-list",
    module: "社交关系",
    route: "pages/group/list",
    title: "群聊列表",
    summary: "汇总用户所在群聊的名称、成员规模、最近动态与入口状态。",
    entry: ["通讯录群聊入口", "功能中心群工具入口"],
    primary: ["查看群详情", "打开群会话", "发起新群聊"],
    states: ["已有群聊列表", "暂无群聊", "群搜索结果"],
    interactions: [
      "群卡片应同时呈现群名、人数、最近一条消息摘要。",
      "从列表可一跳进入群详情，也可直接返回群会话。",
      "空状态建议给出发起群聊的主入口。"
    ],
    related: ["group-detail", "group-create", "conversation-detail"]
  },
  {
    id: "group-detail",
    module: "社交关系",
    route: "pages/group/detail",
    title: "群详情",
    summary: "管理群信息、群成员、邀请入口和群工具配置。",
    entry: ["群聊列表进入", "会话详情中的群设置进入"],
    primary: ["查看群成员", "邀请成员", "编辑群信息", "回到群聊天"],
    states: ["普通成员视角", "管理员视角", "邀请成功", "成员为空"],
    interactions: [
      "群头像、群名、人数与群公告应作为头部核心信息。",
      "邀请成员、群公告、免打扰等需要分区展示，避免混乱。",
      "群详情和聊天页应形成双向跳转关系。"
    ],
    related: ["group-list", "group-create", "conversation-detail"]
  },
  {
    id: "group-create",
    module: "社交关系",
    route: "pages/group/create",
    title: "发起群聊",
    summary: "选择联系人并创建新群，为协作、活动、项目沟通提供起点。",
    entry: ["群聊列表主操作", "通讯录群聊入口", "功能中心协同模块"],
    primary: ["选择成员", "填写群名", "创建群聊"],
    states: ["初始空选择", "已选择多人", "创建中", "创建成功"],
    interactions: [
      "创建群聊应先选成员，再补充群名称与默认群资料。",
      "成功后直接进入群详情或群聊天，提高转化效率。",
      "成员选择支持滚动搜索和快速勾选。"
    ],
    related: ["group-list", "group-detail", "conversation-detail"]
  },
  {
    id: "discover-home",
    module: "发现与内容",
    route: "pages/discover/index",
    title: "发现首页",
    summary: "整合朋友圈、视频号、小程序、扫一扫、搜一搜等内容与服务入口。",
    entry: ["底部 Tab 进入", "从运营消息推荐进入", "登录后日常浏览入口"],
    primary: ["进入朋友圈", "进入扫一扫", "进入搜一搜", "进入功能中心"],
    states: ["能力入口有数据", "能力入口为空", "入口状态变化"],
    interactions: [
      "顶部统计展示能力分区数量、入口总数与当前联调端信息。",
      "发现页强调入口导航而非沉浸消费，适合大面积卡片布局。",
      "能力入口点击后跳往不同内容或服务模块，并保留回退路径。"
    ],
    related: ["moments-home", "scan-home", "search-home", "feature-hub"]
  },
  {
    id: "moments-home",
    module: "发现与内容",
    route: "pages/moments/index",
    title: "朋友圈时间线",
    summary: "承载动态内容流、点赞、评论、回复和图片预览。",
    entry: ["发现页进入", "通知中心评论提醒进入", "发布成功后回流"],
    primary: ["浏览动态", "点赞/取消赞", "发表评论", "回复评论", "发布动态"],
    states: ["时间线有内容", "空时间线", "图片九宫格", "评论展开"],
    interactions: [
      "动态卡同时承载作者信息、文案、媒体、点赞与评论统计。",
      "点赞与评论需要支持读写回查，保证社交反馈可信。",
      "桌面 H5 下内容卡宜拉开纵向层次，避免过度拥挤。"
    ],
    related: ["moments-publish", "notice-center", "profile-status"]
  },
  {
    id: "moments-publish",
    module: "发现与内容",
    route: "pages/moments/publish",
    title: "发布朋友圈",
    summary: "发布文字与图片动态，并在成功后回到时间线确认展示。",
    entry: ["朋友圈首页发布按钮", "发现页快捷动作"],
    primary: ["输入文案", "选择图片", "删除图片", "发布动态"],
    states: ["纯文本", "图文混合", "上传中", "发布成功"],
    interactions: [
      "文本字数与图片数量需要实时回显，帮助控制内容量。",
      "图片上传完成后再统一创建动态，避免出现半成品内容。",
      "发布成功后自动返回时间线并能看到最新动态。"
    ],
    related: ["moments-home", "collection-home"]
  },
  {
    id: "official-list",
    module: "发现与内容",
    route: "pages/official/list",
    title: "公众号列表",
    summary: "浏览可关注公众号、进入公众号详情并触发运营内容消费。",
    entry: ["发现页生态入口", "功能中心内容模块"],
    primary: ["查看公众号详情", "快速关注", "进入文章历史"],
    states: ["已关注", "未关注", "搜索过滤", "推荐列表"],
    interactions: [
      "列表卡片突出公众号定位、更新频率与关注状态。",
      "关注后需要回查按钮状态，避免重复关注。",
      "从列表到详情、历史消息、文章阅读形成完整内容漏斗。"
    ],
    related: ["official-detail", "official-history", "official-article"]
  },
  {
    id: "official-detail",
    module: "发现与内容",
    route: "pages/official/detail",
    title: "公众号详情",
    summary: "承载公众号简介、关注状态、能力入口与最近内容预览。",
    entry: ["公众号列表进入", "搜索结果进入", "扫一扫公众号二维码进入"],
    primary: ["关注/取关", "查看历史消息", "打开最新文章"],
    states: ["已关注", "未关注", "详情加载完成"],
    interactions: [
      "详情页需要明确公众号定位、服务能力和内容更新价值。",
      "关注与取关属于高频状态操作，按钮反馈必须即时。",
      "文章入口应始终在首屏可见，降低内容消费门槛。"
    ],
    related: ["official-list", "official-history", "official-article"]
  },
  {
    id: "official-history",
    module: "发现与内容",
    route: "pages/official/history",
    title: "公众号历史消息",
    summary: "按时间倒序查看公众号历史推文和专题内容。",
    entry: ["公众号详情进入", "文章页返回公众号继续浏览"],
    primary: ["按时间查看文章", "进入文章详情", "回到公众号主页"],
    states: ["有历史消息", "空历史", "分页继续加载"],
    interactions: [
      "历史消息适合时间线或文章列表呈现，突出标题与摘要。",
      "文章打开后返回历史消息时要保持浏览位置。",
      "列表项可以附带已读/已收藏等状态提示。"
    ],
    related: ["official-detail", "official-article"]
  },
  {
    id: "official-article",
    module: "发现与内容",
    route: "pages/official/article",
    title: "公众号文章详情",
    summary: "负责文章阅读、点赞、收藏与来源回跳。",
    entry: ["公众号历史消息进入", "公众号详情推荐文章进入", "搜索结果进入"],
    primary: ["阅读正文", "点赞/取消点赞", "收藏文章", "返回公众号"],
    states: ["正文加载", "点赞成功", "取消点赞", "收藏成功"],
    interactions: [
      "正文阅读区需要高可读性排版，保留文章来源信息。",
      "点赞与收藏均需回查状态，形成真实内容互动。",
      "文章底部可引导回到公众号主页或查看更多历史内容。"
    ],
    related: ["official-history", "collection-home", "official-detail"]
  },
  {
    id: "search-home",
    module: "发现与内容",
    route: "pages/search/index",
    title: "搜一搜",
    summary: "统一承接联系人、文章、小程序、服务等综合搜索能力。",
    entry: ["发现页快捷入口", "聊天页搜索扩展", "扫一扫解析后的继续搜索"],
    primary: ["查看搜索建议", "输入关键词", "浏览综合结果", "进入目标内容"],
    states: ["默认建议", "综合结果", "分类结果", "无结果"],
    interactions: [
      "搜索建议在未输入时提供最近热词和快捷入口。",
      "结果页需要按联系人、文章、小程序等分块组织。",
      "点击结果后仍可快速返回继续搜索，不丢失关键词。"
    ],
    related: ["scan-home", "miniapp-open", "official-article", "contacts-profile"]
  },
  {
    id: "scan-home",
    module: "发现与内容",
    route: "pages/scan/index",
    title: "扫一扫",
    summary: "处理二维码/条码解析，并根据目标类型打开人、群、公众号或支付动作。",
    entry: ["发现页快捷入口", "聊天页工具入口", "钱包场景跳转"],
    primary: ["打开相机扫描", "解析结果", "跳转目标页面"],
    states: ["扫描中", "识别成功", "无法识别", "权限不足"],
    interactions: [
      "识别结果应明确类型，例如个人名片、群二维码、公众号、小程序、支付码。",
      "解析失败要给出可理解的补救动作，例如重新扫描或手动搜索。",
      "扫码后的落地页面必须支持继续回退到扫一扫。"
    ],
    related: ["contacts-profile", "official-detail", "miniapp-open", "wallet-transfer"]
  },
  {
    id: "notice-center",
    module: "发现与内容",
    route: "pages/notice/index",
    title: "通知中心",
    summary: "汇总系统通知、朋友圈提醒、申请消息和服务运营通知。",
    entry: ["发现页与我的入口", "推送消息落地页"],
    primary: ["浏览通知", "标记已读", "跳转相关内容"],
    states: ["未读通知", "已读通知", "空状态"],
    interactions: [
      "通知卡需要说明来源模块和跳转目标，避免用户理解成本。",
      "已读状态要即时更新，并在下次进入时保持一致。",
      "评论提醒、好友申请和服务通知都应支持一跳直达。"
    ],
    related: ["moments-home", "contacts-new-friend", "official-article"]
  },
  {
    id: "wallet-home",
    module: "支付与交易",
    route: "pages/wallet/index",
    title: "服务与钱包",
    summary: "呈现零钱余额、月收支、支付能力入口与最近账单。",
    entry: ["我的页面进入", "功能中心支付模块进入"],
    primary: ["好友转账", "红包操作", "查看全部账单", "进入银行卡"],
    states: ["余额正常", "有最近账单", "无账单空状态"],
    interactions: [
      "钱包首页优先呈现余额与月度收支，帮助用户快速建立金额感知。",
      "支付能力入口使用卡片化组织，避免与交易流水混在一起。",
      "最近账单应可回溯到账单明细页继续筛选。"
    ],
    related: ["wallet-transfer", "wallet-red-packet", "wallet-bills", "cards-home"]
  },
  {
    id: "wallet-transfer",
    module: "支付与交易",
    route: "pages/wallet/transfer",
    title: "好友转账",
    summary: "发起好友转账并同步生成交易流水。",
    entry: ["钱包首页入口", "联系人资料支付入口", "聊天内转账入口"],
    primary: ["填写收款人", "输入金额", "填写备注", "确认转账"],
    states: ["金额预设", "提交中", "转账成功", "金额校验失败"],
    interactions: [
      "金额输入提供快捷预设，降低低频输入成本。",
      "转账成功后要能在账单明细中回查对应流水。",
      "收款人信息和金额应在提交前形成最后确认心智。"
    ],
    related: ["wallet-home", "wallet-bills", "contacts-profile"]
  },
  {
    id: "wallet-red-packet",
    module: "支付与交易",
    route: "pages/wallet/red-packet",
    title: "红包创建 / 打开",
    summary: "负责群红包或单人红包的创建、领取与结果回显。",
    entry: ["钱包首页红包入口", "群聊场景发红包"],
    primary: ["选择红包类型", "填写金额和数量", "填写祝福语", "创建红包", "打开红包"],
    states: ["普通红包", "拼手气红包", "已领取", "已领完"],
    interactions: [
      "红包类型切换需要动态影响金额和数量输入规则。",
      "创建成功后应返回红包编号或分享卡片，便于后续领取。",
      "打开红包后要展示领取金额、名次和剩余状态。"
    ],
    related: ["wallet-home", "conversation-detail", "wallet-bills"]
  },
  {
    id: "wallet-bills",
    module: "支付与交易",
    route: "pages/wallet/bills",
    title: "账单明细",
    summary: "按类型、起止日期筛选账单，并查看收入/支出结果。",
    entry: ["钱包首页最近账单", "转账成功后的回查入口"],
    primary: ["切换类型", "选择开始日期", "选择结束日期", "重置筛选", "查看账单"],
    states: ["有结果", "无结果空状态", "过滤中"],
    interactions: [
      "筛选器应始终可见，便于高频回查账单。",
      "收入与支出需要在视觉上明显区分，减少误读。",
      "没有结果时引导用户回到转账或红包场景继续产生数据。"
    ],
    related: ["wallet-home", "wallet-transfer", "wallet-red-packet"]
  },
  {
    id: "cards-home",
    module: "支付与交易",
    route: "pages/cards/index",
    title: "卡包",
    summary: "承载银行卡、优惠券、会员卡等支付周边资产。",
    entry: ["钱包首页入口", "我的页面资产入口"],
    primary: ["查看银行卡", "查看会员卡", "查看优惠券"],
    states: ["有卡片", "无卡片", "卡片过期"],
    interactions: [
      "卡包更适合卡片栈布局，强调可用状态和失效时间。",
      "从卡包可回到钱包主场，也可进一步跳转支付页面。",
      "不同卡类型应在视觉层级上有明确差异。"
    ],
    related: ["wallet-home", "wallet-transfer"]
  },
  {
    id: "feature-hub",
    module: "生态与服务",
    route: "pages/feature-hub/index",
    title: "功能中心",
    summary: "以矩阵方式盘点微信式超级应用能力，便于查阅和继续扩展。",
    entry: ["发现页入口", "我的页面入口", "运营跳转入口"],
    primary: ["按模块浏览能力", "进入目标功能", "查看能力状态"],
    states: ["能力矩阵有数据", "能力为空", "在线 / 预留状态"],
    interactions: [
      "能力卡需要同时说明标题、作用、状态与对应入口。",
      "模块标题帮助产品和研发快速盘点能力边界。",
      "功能中心本质是全局导航，应尽量降低学习成本。"
    ],
    related: ["discover-home", "wallet-home", "miniapp-open"]
  },
  {
    id: "miniapp-open",
    module: "生态与服务",
    route: "pages/miniapp/open",
    title: "小程序打开页",
    summary: "展示小程序简介、收藏状态、最近使用状态和主内容入口。",
    entry: ["搜一搜结果进入", "发现页入口进入", "最近使用或收藏进入"],
    primary: ["打开小程序", "收藏 / 取消收藏", "返回最近使用"],
    states: ["已收藏", "未收藏", "已记录最近使用"],
    interactions: [
      "打开小程序时需顺带写入最近使用队列，便于后续回访。",
      "收藏状态要在详情页和收藏列表中保持一致。",
      "打开页承担轻量介绍和跳转，不应信息过载。"
    ],
    related: ["miniapp-recent", "miniapp-favorites", "search-home"]
  },
  {
    id: "miniapp-recent",
    module: "生态与服务",
    route: "pages/miniapp/recent",
    title: "最近使用的小程序",
    summary: "回顾最近打开的小程序，并支持移除记录。",
    entry: ["小程序主页入口", "发现页生态入口"],
    primary: ["再次打开小程序", "移除最近使用", "进入收藏"],
    states: ["有最近使用", "无最近使用"],
    interactions: [
      "最近使用列表优先强调连续访问效率，降低再次查找成本。",
      "移除后需要在最近使用列表中即时消失。",
      "同一个小程序在最近使用和收藏之间要形成可跳转关系。"
    ],
    related: ["miniapp-open", "miniapp-favorites"]
  },
  {
    id: "miniapp-favorites",
    module: "生态与服务",
    route: "pages/miniapp/favorites",
    title: "收藏的小程序",
    summary: "查看用户主动收藏的小程序列表，并支持取消收藏。",
    entry: ["小程序打开页收藏后进入", "发现页生态入口"],
    primary: ["打开已收藏小程序", "取消收藏", "查看最近使用"],
    states: ["有收藏", "无收藏", "取消收藏后回查"],
    interactions: [
      "收藏列表强调稳定资产感，适合网格或列表混排。",
      "取消收藏后需同步反映到详情页和收藏列表。",
      "收藏与最近使用共同构成轻服务复访链路。"
    ],
    related: ["miniapp-open", "miniapp-recent"]
  },
  {
    id: "collection-home",
    module: "生态与服务",
    route: "pages/collection/index",
    title: "收藏",
    summary: "统一承接文章、图片、链接等收藏内容的查看与管理。",
    entry: ["我的页面入口", "文章收藏成功后跳转"],
    primary: ["切换收藏类型", "查看收藏内容", "删除收藏"],
    states: ["有收藏内容", "无收藏内容", "删除后回查"],
    interactions: [
      "收藏页需要按内容类型分层，避免不同媒介混杂。",
      "删除动作最好支持轻确认，避免误删高频内容。",
      "收藏页既是个人资产，也承担跨模块内容回访作用。"
    ],
    related: ["official-article", "me-home"]
  },
  {
    id: "emoji-store",
    module: "生态与服务",
    route: "pages/emoji/store",
    title: "表情商店",
    summary: "浏览表情包、下载表情资源并回到聊天场景使用。",
    entry: ["我的页面入口", "聊天表情面板扩展入口"],
    primary: ["浏览表情包", "下载表情包", "返回聊天使用"],
    states: ["可下载", "已下载", "下载中"],
    interactions: [
      "表情商店强调封面、数量、风格标签和下载状态。",
      "下载成功后需要回写聊天面板可用资源。",
      "商店页面应兼顾内容发现与快速返回聊天。"
    ],
    related: ["conversation-detail", "me-home"]
  },
  {
    id: "me-home",
    module: "个人中心",
    route: "pages/me/index",
    title: "我",
    summary: "承载个人资料、资产入口、常用功能与退出登录。",
    entry: ["底部 Tab 进入", "登录成功后查看个人资产"],
    primary: ["编辑资料", "查看二维码", "状态设置", "进入钱包", "进入设置"],
    states: ["资料已加载", "资料刷新成功", "退出登录"],
    interactions: [
      "头部资料区应同时承载昵称、微友号、城市、签名与统计值。",
      "常用入口需要和钱包、收藏、通知、设置形成稳定映射。",
      "退出登录属于高风险动作，应与普通入口明显区分。"
    ],
    related: ["profile-edit", "profile-qrcode", "profile-status", "wallet-home", "settings-home"]
  },
  {
    id: "profile-edit",
    module: "个人中心",
    route: "pages/profile/edit",
    title: "编辑资料",
    summary: "编辑昵称、城市、签名与头像地址，并实时预览修改结果。",
    entry: ["我的页面编辑资料入口", "首次完善资料入口"],
    primary: ["修改昵称", "修改城市", "修改签名", "修改头像地址", "保存资料"],
    states: ["初始回填", "重置表单", "保存成功", "保存失败"],
    interactions: [
      "表单上方资料预览区帮助用户提前感知修改效果。",
      "重置操作回到当前后端已保存状态，而不是清空。",
      "保存成功后回到“我”页面应立即可见最新资料。"
    ],
    related: ["me-home", "profile-qrcode"]
  },
  {
    id: "profile-qrcode",
    module: "个人中心",
    route: "pages/profile/qrcode",
    title: "我的二维码",
    summary: "展示个人二维码名片，用于线下加好友或扫码打开资料页。",
    entry: ["我的页面二维码入口", "名片分享入口"],
    primary: ["查看二维码", "保存二维码", "给好友查看"],
    states: ["静态码", "动态码", "加载失败"],
    interactions: [
      "二维码页强调身份识别与扫码可达性，视觉上应保持居中聚焦。",
      "若支持动态码，需要有明显的有效期或刷新提示。",
      "二维码页可以连带展示头像、昵称、微友号增强辨识度。"
    ],
    related: ["scan-home", "me-home"]
  },
  {
    id: "profile-status",
    module: "个人中心",
    route: "pages/profile/status",
    title: "状态设置",
    summary: "管理在线状态、心情文案、位置状态与展示时效。",
    entry: ["我的页面状态设置入口", "朋友圈或资料页快捷入口"],
    primary: ["选择状态模板", "编辑状态文案", "设置有效期", "保存状态"],
    states: ["默认状态", "自定义状态", "过期状态"],
    interactions: [
      "状态页应兼顾模板选择和个性化编辑，降低表达门槛。",
      "保存状态后需要在个人资料和相关入口同步展示。",
      "状态失效后应自动回退到默认展示。"
    ],
    related: ["me-home", "moments-home"]
  },
  {
    id: "settings-home",
    module: "设置与安全",
    route: "pages/settings/index",
    title: "设置中心",
    summary: "集中管理通知、隐私、资料、设备与退出登录。",
    entry: ["我的页面设置入口", "安全提醒跳转"],
    primary: ["切换通知开关", "进入隐私设置", "进入设备管理", "退出登录"],
    states: ["开关已加载", "保存成功", "保存失败"],
    interactions: [
      "头部统计概览当前开启项数量，帮助快速感知安全配置状态。",
      "常用设置应按资料、通知、隐私、设备、账户风险分层。",
      "退出登录要与普通功能卡分离。"
    ],
    related: ["settings-notifications", "settings-privacy", "settings-devices", "me-home"]
  },
  {
    id: "settings-notifications",
    module: "设置与安全",
    route: "pages/settings/notifications",
    title: "消息通知设置",
    summary: "细化聊天、朋友圈、公众号等通知提醒方式。",
    entry: ["设置中心通知入口"],
    primary: ["打开 / 关闭通知", "按场景管理提醒"],
    states: ["全部开启", "部分关闭", "保存中"],
    interactions: [
      "通知设置要兼顾全局开关与场景级开关。",
      "变更后需即时回查保存结果。",
      "关闭高频通知时应提示对后续体验的影响。"
    ],
    related: ["settings-home", "notice-center"]
  },
  {
    id: "settings-privacy",
    module: "设置与安全",
    route: "pages/settings/privacy",
    title: "隐私设置",
    summary: "管理加好友方式、展示范围和隐私边界。",
    entry: ["设置中心隐私入口"],
    primary: ["控制手机号加好友", "控制名片展示", "控制其他隐私项"],
    states: ["默认隐私策略", "修改后回写", "权限受限提示"],
    interactions: [
      "隐私项文案要说清楚影响范围，不能只给技术开关名。",
      "修改后应在资料页、搜索、加好友链路中体现。",
      "重要隐私变更要优先强调不可逆或影响面。"
    ],
    related: ["settings-home", "contacts-add-friend", "contacts-profile"]
  },
  {
    id: "settings-devices",
    module: "设置与安全",
    route: "pages/settings/devices",
    title: "设备管理",
    summary: "查看当前登录设备、历史设备并执行下线控制。",
    entry: ["设置中心设备入口", "异地登录安全提醒"],
    primary: ["查看设备列表", "下线指定设备", "确认当前设备"],
    states: ["当前设备", "历史设备", "设备已下线"],
    interactions: [
      "设备列表要区分当前设备和其他设备，避免误下线自己。",
      "下线后必须有明确结果反馈，并支持回查状态。",
      "设备信息建议包含机型、系统、最近活跃时间和地点。"
    ],
    related: ["settings-home", "auth-login"]
  }
];

const FLOWS = [
  {
    id: "flow-login",
    title: "登录激活主线",
    module: "登录与启动",
    goal: "完成登录并回到微信核心首页。",
    pages: ["auth-login", "chat-home", "discover-home", "me-home"],
    steps: [
      { title: "登录 / 注册", page: "auth-login", action: "选择密码登录或短信登录", feedback: "成功后生成会话令牌并恢复用户上下文" },
      { title: "进入微信首页", page: "chat-home", action: "展示最近会话和快捷入口", feedback: "未读数、连接状态、空会话引导都可见" },
      { title: "浏览发现入口", page: "discover-home", action: "检查扫一扫、搜一搜、朋友圈入口", feedback: "确认发现页能力矩阵完整" },
      { title: "查看个人中心", page: "me-home", action: "验证个人资料和资产入口", feedback: "形成完整初始留存闭环" }
    ]
  },
  {
    id: "flow-add-friend",
    title: "加好友到发起会话",
    module: "社交关系",
    goal: "完成搜索找人、通过申请并发起单聊。",
    pages: ["contacts-add-friend", "contacts-new-friend", "contacts-profile", "conversation-detail"],
    steps: [
      { title: "搜索候选好友", page: "contacts-add-friend", action: "输入昵称、微友号或城市", feedback: "返回候选人列表并可发起申请" },
      { title: "处理新的朋友", page: "contacts-new-friend", action: "通过好友申请", feedback: "处理结果写回并进入联系人关系链" },
      { title: "查看联系人资料", page: "contacts-profile", action: "确认昵称、城市、签名", feedback: "可继续直接发起聊天" },
      { title: "进入会话详情", page: "conversation-detail", action: "发送第一条消息", feedback: "消息写入会话历史并回显" }
    ]
  },
  {
    id: "flow-group",
    title: "群聊组织与协同",
    module: "社交关系",
    goal: "创建群聊并进入群管理。",
    pages: ["group-create", "group-list", "group-detail", "conversation-detail"],
    steps: [
      { title: "发起群聊", page: "group-create", action: "选择成员并创建群", feedback: "创建成功生成群 ID 和群资料" },
      { title: "查看群聊列表", page: "group-list", action: "确认新群已加入列表", feedback: "可直接跳往详情或聊天" },
      { title: "查看群详情", page: "group-detail", action: "查看成员、公告、邀请入口", feedback: "群管理信息完整" },
      { title: "回到群聊天", page: "conversation-detail", action: "进入群消息场景", feedback: "协同沟通链路闭环" }
    ]
  },
  {
    id: "flow-moments",
    title: "朋友圈发布与互动",
    module: "发现与内容",
    goal: "发布动态并完成点赞、评论、回复。",
    pages: ["moments-publish", "moments-home", "notice-center"],
    steps: [
      { title: "发布动态", page: "moments-publish", action: "输入文案并上传图片", feedback: "成功创建新动态并回流时间线" },
      { title: "时间线回看", page: "moments-home", action: "查看新动态、点赞与评论", feedback: "点赞数和评论数可即时回写" },
      { title: "通知中心提醒", page: "notice-center", action: "接收评论和互动通知", feedback: "从通知可重新回到动态内容" }
    ]
  },
  {
    id: "flow-official",
    title: "公众号内容消费链",
    module: "发现与内容",
    goal: "完成关注公众号、阅读文章、点赞收藏。",
    pages: ["official-list", "official-detail", "official-history", "official-article", "collection-home"],
    steps: [
      { title: "浏览公众号列表", page: "official-list", action: "选择目标公众号", feedback: "进入详情页了解定位" },
      { title: "查看公众号详情", page: "official-detail", action: "关注公众号", feedback: "关注状态立即更新" },
      { title: "浏览历史消息", page: "official-history", action: "选择某篇文章", feedback: "进入文章详情阅读" },
      { title: "文章互动", page: "official-article", action: "点赞、收藏文章", feedback: "互动状态可回查" },
      { title: "收藏沉淀", page: "collection-home", action: "查看已收藏内容", feedback: "形成内容资产闭环" }
    ]
  },
  {
    id: "flow-search-miniapp",
    title: "搜索 / 扫码到小程序服务",
    module: "生态与服务",
    goal: "完成搜索、扫码、打开小程序和收藏回访。",
    pages: ["search-home", "scan-home", "miniapp-open", "miniapp-recent", "miniapp-favorites"],
    steps: [
      { title: "搜一搜查找服务", page: "search-home", action: "输入关键词获取建议和结果", feedback: "定位到目标小程序或服务入口" },
      { title: "扫一扫解析", page: "scan-home", action: "识别二维码类型", feedback: "根据类型跳转公众号 / 小程序 / 资料页" },
      { title: "打开小程序", page: "miniapp-open", action: "查看简介并进入服务", feedback: "同时写入最近使用" },
      { title: "查看最近使用", page: "miniapp-recent", action: "确认最近访问记录", feedback: "支持移除无用记录" },
      { title: "收藏小程序", page: "miniapp-favorites", action: "查看收藏结果", feedback: "后续形成稳定复访入口" }
    ]
  },
  {
    id: "flow-wallet",
    title: "钱包交易闭环",
    module: "支付与交易",
    goal: "从钱包首页进入转账、红包，再回到账单回查。",
    pages: ["wallet-home", "wallet-transfer", "wallet-bills", "wallet-red-packet", "cards-home"],
    steps: [
      { title: "进入钱包首页", page: "wallet-home", action: "查看余额和最近账单", feedback: "建立资金概览" },
      { title: "发起转账", page: "wallet-transfer", action: "填写收款人、金额、备注", feedback: "返回交易流水编号" },
      { title: "回查账单", page: "wallet-bills", action: "按类型和日期筛选", feedback: "能找到刚刚的转账记录" },
      { title: "红包操作", page: "wallet-red-packet", action: "创建并打开红包", feedback: "红包结果和流水同步写入" },
      { title: "查看卡包", page: "cards-home", action: "浏览银行卡和卡券", feedback: "支付周边资产完整可见" }
    ]
  },
  {
    id: "flow-profile-settings",
    title: "个人资料与安全配置",
    module: "设置与安全",
    goal: "编辑资料、查看二维码、修改状态并管理安全设置。",
    pages: ["me-home", "profile-edit", "profile-qrcode", "profile-status", "settings-home", "settings-devices", "settings-privacy"],
    steps: [
      { title: "从“我”进入个人中心", page: "me-home", action: "查看资料、资产和设置入口", feedback: "定位下一步编辑目标" },
      { title: "编辑资料", page: "profile-edit", action: "修改昵称、城市、签名、头像", feedback: "保存后“我”页即时更新" },
      { title: "查看二维码", page: "profile-qrcode", action: "展示个人二维码名片", feedback: "支持线下社交和扫码打开" },
      { title: "设置个人状态", page: "profile-status", action: "选择在线状态与文案", feedback: "资料页同步展示" },
      { title: "管理设置与设备", page: "settings-home", action: "调整通知、隐私与设备管理", feedback: "变更可即时读写回查" },
      { title: "检查设备安全", page: "settings-devices", action: "下线其他设备", feedback: "降低账户安全风险" }
    ]
  }
];

const pageMap = Object.fromEntries(PAGES.map((page) => [page.id, page]));

function moduleGroups() {
  const result = {};
  PAGES.forEach((page) => {
    if (!result[page.module]) {
      result[page.module] = [];
    }
    result[page.module].push(page);
  });
  return result;
}

function relatedPages(ids) {
  return (ids || []).map((id) => pageMap[id]).filter(Boolean);
}

function tagHtml(values, className = "detail-chip") {
  return values.map((value) => `<span class="${className}">${value}</span>`).join("");
}

function listHtml(values) {
  return `<ul class="detail-list">${values.map((value) => `<li>${value}</li>`).join("")}</ul>`;
}

function mockBlock(title, items) {
  return `
    <div class="mock-block">
      <h5>${title}</h5>
      <ul class="mock-list">${items.map((item) => `<li>${item}</li>`).join("")}</ul>
    </div>
  `;
}

function detailGroup(title, values) {
  return `
    <div class="detail-group">
      <h3>${title}</h3>
      ${listHtml(values)}
    </div>
  `;
}

function createSummaryStats() {
  const modules = Object.keys(moduleGroups());
  return [
    { value: String(PAGES.length), label: "功能页面" },
    { value: String(modules.length), label: "一级模块" },
    { value: String(FLOWS.length), label: "关键流程" },
    { value: String(PAGES.filter((page) => page.primary.length >= 4).length), label: "深交互页" }
  ];
}

function renderIndex() {
  const summaryGrid = document.getElementById("summary-grid");
  const moduleGrid = document.getElementById("module-grid");
  const flowHighlight = document.getElementById("flow-highlight");
  const groups = moduleGroups();

  summaryGrid.innerHTML = createSummaryStats().map((stat) => `
    <div class="mini-stat">
      <strong>${stat.value}</strong>
      <span>${stat.label}</span>
    </div>
  `).join("");

  moduleGrid.innerHTML = Object.entries(groups).map(([moduleName, pages]) => {
    const meta = MODULE_META[moduleName];
    return `
      <a class="module-card" href="./all-pages.html#${pages[0].id}">
        <p class="eyebrow">${moduleName}</p>
        <h4>${pages.length} 个页面</h4>
        <p>${meta.description}</p>
        <div class="tag-row">${tagHtml(meta.focus, "module-chip")}</div>
      </a>
    `;
  }).join("");

  flowHighlight.innerHTML = FLOWS.map((flow) => `
    <a class="flow-card" href="./flows.html#${flow.id}">
      <div>
        <p class="eyebrow">${flow.module}</p>
        <h4>${flow.title}</h4>
      </div>
      <p>${flow.goal}</p>
      <div class="tag-row">${flow.pages.slice(0, 3).map((id) => pageMap[id]?.title || id).map((title) => `<span class="flow-chip">${title}</span>`).join("")}</div>
    </a>
  `).join("");
}

function renderAllPages() {
  const moduleFilters = document.getElementById("module-filters");
  const pageList = document.getElementById("page-list");
  const pageCount = document.getElementById("page-count");
  const pageSearch = document.getElementById("page-search");
  const pageDetailHeader = document.getElementById("page-detail-header");
  const pageDetailPanel = document.getElementById("page-detail-panel");
  const pagePreviewPanel = document.getElementById("page-preview-panel");
  const modules = ["全部", ...Object.keys(moduleGroups())];
  const state = {
    module: "全部",
    keyword: "",
    selected: pageMap[location.hash.replace(/^#/, "")] ? location.hash.replace(/^#/, "") : PAGES[0].id
  };

  function filteredPages() {
    return PAGES.filter((page) => {
      const moduleMatch = state.module === "全部" || page.module === state.module;
      const bag = [page.title, page.route, page.module, page.summary, ...page.entry, ...page.primary, ...page.states, ...page.interactions].join(" ").toLowerCase();
      const keywordMatch = !state.keyword || bag.includes(state.keyword.toLowerCase());
      return moduleMatch && keywordMatch;
    });
  }

  function ensureSelected(list) {
    if (!list.some((page) => page.id === state.selected)) {
      state.selected = list[0]?.id || PAGES[0].id;
    }
  }

  function updateHash() {
    history.replaceState(null, "", `#${state.selected}`);
  }

  function renderFilters() {
    moduleFilters.innerHTML = modules.map((moduleName) => `
      <button class="module-chip ${state.module === moduleName ? "is-active" : ""}" data-module="${moduleName}">${moduleName}</button>
    `).join("");
    moduleFilters.querySelectorAll("button").forEach((button) => {
      button.addEventListener("click", () => {
        state.module = button.dataset.module;
        rerender();
      });
    });
  }

  function renderList(list) {
    pageCount.textContent = `${list.length} 页`;
    pageList.innerHTML = list.map((page) => `
      <div class="page-list-item ${page.id === state.selected ? "is-active" : ""}" data-id="${page.id}">
        <p class="eyebrow">${page.module}</p>
        <h4>${page.title}</h4>
        <p class="page-meta">${page.route}</p>
        <p class="page-meta">${page.summary}</p>
      </div>
    `).join("");
    pageList.querySelectorAll(".page-list-item").forEach((item) => {
      item.addEventListener("click", () => {
        state.selected = item.dataset.id;
        rerender();
      });
    });
  }

  function renderDetail(page) {
    pageDetailHeader.innerHTML = `
      <p class="eyebrow">${page.module}</p>
      <h2>${page.title}</h2>
      <p class="detail-copy">${page.summary}</p>
      <div class="detail-chip-row">
        <span class="page-pill">${page.route}</span>
        <span class="detail-chip">${page.primary.length} 个主操作</span>
        <span class="detail-chip">${page.states.length} 个关键状态</span>
      </div>
    `;

    const related = relatedPages(page.related);

    pageDetailPanel.innerHTML = `
      <div class="meta-grid">
        <div class="detail-group">
          <h3>进入方式</h3>
          ${listHtml(page.entry)}
        </div>
        <div class="detail-group">
          <h3>主操作</h3>
          ${listHtml(page.primary)}
        </div>
      </div>
      ${detailGroup("状态切面", page.states)}
      ${detailGroup("交互要点", page.interactions)}
      <div class="detail-group">
        <h3>关联页面</h3>
        <div class="tag-row">${related.map((item) => `<a class="detail-chip" href="#${item.id}">${item.title}</a>`).join("")}</div>
      </div>
    `;
    pageDetailPanel.querySelectorAll("a.detail-chip").forEach((link) => {
      link.addEventListener("click", (event) => {
        event.preventDefault();
        state.selected = link.getAttribute("href").replace(/^#/, "");
        rerender();
      });
    });
  }

  function renderPreview(page) {
    const related = relatedPages(page.related);
    pagePreviewPanel.innerHTML = `
      <p class="eyebrow">Prototype Preview</p>
      <h3>桌面 / 手机双端阅读视角</h3>
      <p class="mock-caption">使用统一的手机框架预览该页面的信息结构、主操作和关键状态。</p>
      <div class="phone-frame">
        <div class="phone-screen">
          <div class="phone-header">
            <p class="eyebrow">${page.module}</p>
            <div class="phone-title-row">
              <h4>${page.title}</h4>
              <span class="detail-chip">${page.route}</span>
            </div>
            <p class="mock-note">${page.summary}</p>
          </div>
          <div class="phone-body">
            <div class="mock-stats">
              <div class="stat-badge"><strong>${page.entry.length}</strong><span>进入方式</span></div>
              <div class="stat-badge"><strong>${page.primary.length}</strong><span>主操作</span></div>
              <div class="stat-badge"><strong>${page.states.length}</strong><span>关键状态</span></div>
            </div>
            <div class="mock-action-row">${page.primary.slice(0, 4).map((action) => `<span class="mock-chip">${action}</span>`).join("")}</div>
            <div class="phone-section-grid">
              ${mockBlock("进入方式", page.entry)}
              ${mockBlock("状态切面", page.states)}
              ${mockBlock("交互要点", page.interactions)}
            </div>
            <div class="mock-footer">关联页面：${related.map((item) => item.title).join(" / ") || "无"}</div>
          </div>
        </div>
      </div>
    `;
  }

  function rerender() {
    const list = filteredPages();
    ensureSelected(list);
    const page = pageMap[state.selected] || list[0] || PAGES[0];
    renderFilters();
    renderList(list);
    renderDetail(page);
    renderPreview(page);
    updateHash();
  }

  pageSearch.addEventListener("input", () => {
    state.keyword = pageSearch.value.trim();
    rerender();
  });

  rerender();
}

function renderFlows() {
  const flowLibrary = document.getElementById("flow-library");
  flowLibrary.innerHTML = FLOWS.map((flow) => `
    <article class="flow-card" id="${flow.id}">
      <div>
        <p class="eyebrow">${flow.module}</p>
        <h4>${flow.title}</h4>
      </div>
      <p>${flow.goal}</p>
      <div class="tag-row">${flow.pages.map((id) => `<a class="flow-chip" href="./all-pages.html#${id}">${pageMap[id]?.title || id}</a>`).join("")}</div>
      <div class="flow-steps">
        ${flow.steps.map((step) => `
          <div class="flow-step">
            <h5 class="flow-step-title">${step.title}</h5>
            <p class="step-caption"><strong>${pageMap[step.page]?.title || step.page}</strong> · ${step.action}</p>
            <p class="step-caption">结果：${step.feedback}</p>
          </div>
        `).join("")}
      </div>
    </article>
  `).join("");
}

function initPrototype() {
  const view = document.body.dataset.view;
  if (view === "index") {
    renderIndex();
  }
  if (view === "all-pages") {
    renderAllPages();
  }
  if (view === "flows") {
    renderFlows();
  }
}

document.addEventListener("DOMContentLoaded", initPrototype);
