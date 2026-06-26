# 微友前端 API 层说明

## 目录

- `api/http.js`：统一 `uni.request` 封装，自动带 `Bearer token`
- `api/modules.js`：按业务模块导出的 API 方法
- `api/fallbacks.js`：保留的本地兼容数据工具，可用于后续弱网或离线演示扩展

## 当前约定

1. HTTP 默认请求后端 `http://localhost:8080/api`
2. WebSocket 默认连接 `ws://localhost:8081/ws/chat`
3. 可通过环境变量覆盖：
   - `VITE_API_BASE_URL`
   - `VITE_WS_BASE_URL`
4. 当业务接口返回 `401` 时，请求层会优先尝试调用 `/auth/token/refresh` 自动续期，续期成功后自动重试一次原请求
5. 若刷新失败，会清空本地会话并跳回登录页

## 使用方式

```js
import { authApi, chatApi, walletApi } from "@/api/modules";

const session = await authApi.loginByPassword({
  mobile: "13800138000",
  password: "123456",
  deviceId: "device-demo-001"
});

const conversations = await chatApi.conversations();
const wallet = await walletApi.overview();
```

## 兼容说明

当前页面使用的 API 已经优先对接后端真实接口；若后续需要离线演示或 mock 模式，可再复用 `api/fallbacks.js` 做本地兜底。

## 启动阶段

- `App.vue` 启动时会先拉取 `/app/bootstrap`
- 若公告、协议版本或升级提示有变化，会弹一次启动提示
- 如果本地已有登录态，会继续自动恢复用户信息并建立 WebSocket 连接
- WebSocket 异常断开后会自动重连，恢复后刷新会话与当前激活会话消息
- 聊天消息在发送失败时会保留本地失败状态，后续可手动重发；暂未送达的消息会放入待重试队列
- 待重试消息与失败消息会写入本地存储；如果应用重启，聊天页仍能恢复这些消息状态
- 聊天输入草稿按会话维度持久化，切页或重启后会自动恢复
- 聊天页支持图片与文件消息，发送前会先调用 `/media/upload/local` 上传，再把返回的 URL 作为消息体发给聊天接口
- 聊天页支持语音消息，发送前同样会先上传音频文件；录音依赖 `uni.getRecorderManager`，H5 环境通常不可用
- 聊天页支持视频消息，发送前会上传视频文件，并把时长、尺寸、封面等元数据一起发送
- 聊天页支持拍照发图、位置消息、联系人名片消息，以及一键表情快捷发送
- 聊天页支持多选消息、消息撤回、转发到其他会话，以及将消息快捷创建为收藏内容
- 聊天页支持引用回复、会话内关键字搜索，以及本地置顶单条消息
- 聊天设置页支持会话置顶、免打扰、标为未读与清空聊天记录
- 群聊模块支持管理员设置、单成员禁言、我的群昵称修改，以及群主侧的成员昵称代改
- 联系人资料页支持调用 `/chat/conversation/open-single` 动态打开单聊；好友申请通过后会反映到通讯录列表
- 卡包模块支持 `/card/list` 与 `/card/use`；表情商店支持 `/emoji/store/list`、下载、卸载和启用当前表情包
- 扫一扫模块支持解析 `user:`、`group:`、`official:`、`miniapp:`、`pay:` 等二维码内容；前端支持本地扫码记录与摄像头扫码入口
- 钱包账单接口 `/wallet/bill/list` 已支持前端按 `type/startDate/endDate` 进行筛选
- 用户设置支持 `/user/setting/detail` 与 `/user/setting/update`，前端已接入消息通知和隐私设置页
