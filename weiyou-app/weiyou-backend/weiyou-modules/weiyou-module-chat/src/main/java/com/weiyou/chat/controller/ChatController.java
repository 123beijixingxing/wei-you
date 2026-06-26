package com.weiyou.chat.controller;

import com.weiyou.chat.app.service.ChatPersistenceService;
import com.weiyou.chat.domain.entity.MessageRecordEntity;
import com.weiyou.common.core.api.ApiResponse;
import com.weiyou.common.core.api.PageResponse;
import com.weiyou.common.security.context.UserContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ConcurrentHashMap<Long, Long> fallbackSingleConversationMap = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<ConversationItem> fallbackConversations = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<MessageItem> fallbackMessages = new CopyOnWriteArrayList<>();

    private final ChatPersistenceService chatPersistenceService;

    public ChatController(ChatPersistenceService chatPersistenceService) {
        this.chatPersistenceService = chatPersistenceService;
    }

    @GetMapping("/conversation/list")
    public ApiResponse<PageResponse<ConversationItem>> listConversations(@RequestParam(required = false) String cursor,
                                                                         @RequestParam(defaultValue = "20") int pageSize) {
        Long currentUserId = UserContext.requireUserId();
        try {
            List<ConversationItem> persisted = chatPersistenceService.listConversations(currentUserId).stream()
                    .map(item -> new ConversationItem(
                            item.conversation().getConversationId(),
                            item.conversation().getConversationType(),
                            item.conversation().getConversationType() != null && item.conversation().getConversationType() == 2
                                    ? "群聊-" + item.conversation().getConversationId()
                                    : "会话-" + item.conversation().getConversationId(),
                            item.conversation().getConversationType() != null && item.conversation().getConversationType() == 2
                                    ? "https://weiyou.local/group/default.png"
                                    : "https://weiyou.local/avatar/default.png",
                            item.conversationUser().getUnreadCount(),
                            item.conversationUser().getTopFlag() != null && item.conversationUser().getTopFlag() == 1,
                            item.conversationUser().getMuteFlag() != null && item.conversationUser().getMuteFlag() == 1,
                            item.conversation().getLastMsgDigest(),
                            item.conversation().getLastMsgTime() == null ? null : item.conversation().getLastMsgTime().toString()
                    ))
                    .toList();
            if (!persisted.isEmpty() || !fallbackConversations.isEmpty()) {
                return ApiResponse.ok(PageResponse.of(mergeConversationItems(persisted), 1, pageSize, mergeConversationItems(persisted).size(), false, null));
            }
        } catch (RuntimeException exception) {
            if (!fallbackConversations.isEmpty()) {
                List<ConversationItem> list = new ArrayList<>(fallbackConversations);
                return ApiResponse.ok(PageResponse.of(list, 1, pageSize, list.size(), false, null));
            }
        }
        List<ConversationItem> list = List.of(
                new ConversationItem(90001L, 1, "阿泽", "https://weiyou.local/avatar/10002.png", 2, true, false, "明天下午对齐方案", Instant.now().toString()),
                new ConversationItem(90002L, 2, "微友产品群", "https://weiyou.local/group/90002.png", 8, false, true, "[图片]", Instant.now().minusSeconds(300).toString())
        );
        return ApiResponse.ok(PageResponse.of(list, 1, pageSize, list.size(), false, null));
    }

    @PostMapping("/conversation/open-single")
    public ApiResponse<ConversationOpenResult> openSingleConversation(@Valid @RequestBody OpenSingleConversationRequest request) {
        try {
            var conversation = chatPersistenceService.openSingleConversation(UserContext.requireUserId(), request.targetUserId());
            rememberFallbackConversation(conversation.getConversationId(), request.targetUserId(), conversation.getConversationType());
            return ApiResponse.ok(new ConversationOpenResult(conversation.getConversationId(), request.targetUserId(), conversation.getConversationType()));
        } catch (RuntimeException exception) {
            Long conversationId = fallbackSingleConversationMap.computeIfAbsent(request.targetUserId(), key -> System.currentTimeMillis());
            rememberFallbackConversation(conversationId, request.targetUserId(), 1);
            return ApiResponse.ok(new ConversationOpenResult(conversationId, request.targetUserId(), 1));
        }
    }

    @GetMapping("/conversation/detail")
    public ApiResponse<ConversationSettingData> getConversationDetail(@RequestParam Long conversationId) {
        try {
            var aggregate = chatPersistenceService.getConversation(UserContext.requireUserId(), conversationId);
            if (aggregate == null) {
                return ApiResponse.ok(null);
            }
            return ApiResponse.ok(toConversationSettingData(aggregate));
        } catch (RuntimeException exception) {
            return ApiResponse.ok(new ConversationSettingData(conversationId, 1, "会话-" + conversationId, "https://weiyou.local/avatar/default.png", false, false, false, 0, null));
        }
    }

    @PostMapping("/conversation/setting/update")
    public ApiResponse<ConversationSettingData> updateConversationSetting(@Valid @RequestBody ConversationSettingRequest request) {
        Long currentUserId = UserContext.requireUserId();
        try {
            chatPersistenceService.updateConversationSetting(currentUserId, request.conversationId(), request.top(), request.mute(), request.markUnread());
            var aggregate = chatPersistenceService.getConversation(currentUserId, request.conversationId());
            return ApiResponse.ok(toConversationSettingData(aggregate));
        } catch (RuntimeException exception) {
            return ApiResponse.ok(new ConversationSettingData(request.conversationId(), 1, "会话-" + request.conversationId(), "https://weiyou.local/avatar/default.png", request.top(), request.mute(), request.markUnread(), request.markUnread() != null && request.markUnread() ? 1 : 0, null));
        }
    }

    @PostMapping("/conversation/clear")
    public ApiResponse<ConversationSettingData> clearConversation(@Valid @RequestBody ConversationClearRequest request) {
        Long currentUserId = UserContext.requireUserId();
        try {
            chatPersistenceService.clearConversation(currentUserId, request.conversationId());
            var aggregate = chatPersistenceService.getConversation(currentUserId, request.conversationId());
            return ApiResponse.ok(toConversationSettingData(aggregate));
        } catch (RuntimeException exception) {
            return ApiResponse.ok(new ConversationSettingData(request.conversationId(), 1, "会话-" + request.conversationId(), "https://weiyou.local/avatar/default.png", false, false, false, 0, Instant.now().toString()));
        }
    }

    @GetMapping("/message/history")
    public ApiResponse<PageResponse<MessageItem>> listMessageHistory(@RequestParam Long conversationId,
                                                                     @RequestParam(required = false) String cursor,
                                                                     @RequestParam(defaultValue = "20") int pageSize) {
        Long currentUserId = UserContext.requireUserId();
        try {
            List<MessageItem> persisted = chatPersistenceService.listMessages(currentUserId, conversationId).stream()
                    .map(this::toMessageItem)
                    .toList();
            List<MessageItem> merged = mergeMessages(conversationId, persisted);
            if (!merged.isEmpty()) {
                return ApiResponse.ok(PageResponse.of(merged, 1, pageSize, merged.size(), false, null));
            }
        } catch (RuntimeException exception) {
            List<MessageItem> fallback = mergeMessages(conversationId, List.of());
            if (!fallback.isEmpty()) {
                return ApiResponse.ok(PageResponse.of(fallback, 1, pageSize, fallback.size(), false, null));
            }
        }
        List<MessageItem> list = List.of(
                new MessageItem(70001L, "cmsg_70001", conversationId, currentUserId, 1, null, Map.of("text", "你好，欢迎来到微友"), 1, Instant.now().minusSeconds(120).toString(), 1),
                new MessageItem(70002L, "cmsg_70002", conversationId, 10002L, 1, null, Map.of("text", "收到，稍后我来处理"), 1, Instant.now().minusSeconds(60).toString(), 1)
        );
        return ApiResponse.ok(PageResponse.of(list, 1, pageSize, list.size(), false, null));
    }

    @GetMapping("/message/search")
    public ApiResponse<PageResponse<MessageItem>> searchMessages(@RequestParam Long conversationId,
                                                                 @RequestParam String keyword,
                                                                 @RequestParam(defaultValue = "20") int pageSize) {
        try {
            String text = keyword.trim().toLowerCase();
            List<MessageItem> list = chatPersistenceService.listMessages(UserContext.requireUserId(), conversationId).stream()
                    .filter(item -> item.getContentJson() != null && item.getContentJson().toLowerCase().contains(text))
                    .map(this::toMessageItem)
                    .toList();
            return ApiResponse.ok(PageResponse.of(list, 1, pageSize, list.size(), false, null));
        } catch (RuntimeException exception) {
            return ApiResponse.ok(PageResponse.of(List.of(), 1, pageSize, 0, false, null));
        }
    }

    @PostMapping("/message/send")
    public ApiResponse<MessageSendResult> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        Long currentUserId = UserContext.requireUserId();
        try {
            MessageRecordEntity entity = chatPersistenceService.appendMessage(
                    currentUserId,
                    request.conversationId(),
                    request.msgType(),
                    request.clientMsgId(),
                    request.replyMessageId(),
                    request.content()
            );
            MessageSendResult result = new MessageSendResult(entity.getMessageId(), request.clientMsgId(), entity.getSendStatus(), entity.getSendTime().toString());
            rememberFallbackMessage(currentUserId, request, result);
            return ApiResponse.ok(result);
        } catch (RuntimeException exception) {
            MessageSendResult result = new MessageSendResult(System.currentTimeMillis(), request.clientMsgId(), 1, Instant.now().toString());
            rememberFallbackMessage(currentUserId, request, result);
            return ApiResponse.ok(result);
        }
    }

    @PostMapping("/message/read")
    public ApiResponse<Void> markRead(@Valid @RequestBody MessageReadRequest request) {
        try {
            chatPersistenceService.markRead(UserContext.requireUserId(), request.conversationId(), request.messageId());
        } catch (RuntimeException exception) {
            // local fallback
        }
        updateFallbackConversationUnread(request.conversationId(), 0);
        return ApiResponse.ok();
    }

    @PostMapping("/message/revoke")
    public ApiResponse<MessageRevokeResult> revokeMessage(@Valid @RequestBody MessageRevokeRequest request) {
        try {
            MessageRecordEntity entity = chatPersistenceService.revokeMessage(UserContext.requireUserId(), request.conversationId(), request.messageId());
            updateFallbackMessageAsRecalled(request.conversationId(), request.messageId());
            return ApiResponse.ok(new MessageRevokeResult(entity.getMessageId(), request.conversationId(), true));
        } catch (RuntimeException exception) {
            updateFallbackMessageAsRecalled(request.conversationId(), request.messageId());
            return ApiResponse.ok(new MessageRevokeResult(request.messageId(), request.conversationId(), true));
        }
    }

    private List<ConversationItem> mergeConversationItems(List<ConversationItem> persisted) {
        List<ConversationItem> merged = new ArrayList<>(fallbackConversations);
        merged.addAll(persisted.stream().filter(item -> merged.stream().noneMatch(existing -> existing.conversationId().equals(item.conversationId()))).toList());
        return merged;
    }

    private List<MessageItem> mergeMessages(Long conversationId, List<MessageItem> persisted) {
        List<MessageItem> merged = new ArrayList<>(fallbackMessages.stream()
                .filter(item -> item.conversationId().equals(conversationId))
                .toList());
        merged.addAll(persisted.stream().filter(item -> merged.stream().noneMatch(existing -> existing.messageId().equals(item.messageId())
                || existing.clientMsgId().equals(item.clientMsgId()))).toList());
        return merged;
    }

    private void rememberFallbackConversation(Long conversationId, Long targetUserId, Integer conversationType) {
        ConversationItem item = new ConversationItem(
                conversationId,
                conversationType,
                fallbackConversationTitle(targetUserId),
                fallbackConversationAvatar(targetUserId, conversationType),
                0,
                false,
                false,
                "",
                Instant.now().toString()
        );
        fallbackConversations.removeIf(existing -> existing.conversationId().equals(conversationId));
        fallbackConversations.add(0, item);
    }

    private void rememberFallbackMessage(Long currentUserId, SendMessageRequest request, MessageSendResult result) {
        rememberFallbackConversation(request.conversationId(), 10002L, 1);
        MessageItem message = new MessageItem(
                result.messageId(),
                request.clientMsgId(),
                request.conversationId(),
                currentUserId,
                request.msgType(),
                request.replyMessageId(),
                request.content(),
                result.sendStatus(),
                result.sendTime(),
                0
        );
        fallbackMessages.removeIf(existing -> existing.conversationId().equals(request.conversationId())
                && (existing.messageId().equals(message.messageId()) || existing.clientMsgId().equals(message.clientMsgId())));
        fallbackMessages.add(message);
        fallbackConversations.replaceAll(existing -> {
            if (!existing.conversationId().equals(request.conversationId())) {
                return existing;
            }
            return new ConversationItem(
                    existing.conversationId(),
                    existing.conversationType(),
                    existing.title(),
                    existing.avatar(),
                    existing.unreadCount(),
                    existing.top(),
                    existing.mute(),
                    buildMessageDigest(request.msgType(), request.content()),
                    result.sendTime()
            );
        });
    }

    private void updateFallbackConversationUnread(Long conversationId, Integer unreadCount) {
        fallbackConversations.replaceAll(existing -> {
            if (!existing.conversationId().equals(conversationId)) {
                return existing;
            }
            return new ConversationItem(
                    existing.conversationId(),
                    existing.conversationType(),
                    existing.title(),
                    existing.avatar(),
                    unreadCount,
                    existing.top(),
                    existing.mute(),
                    existing.lastMessageDigest(),
                    existing.lastMessageTime()
            );
        });
    }

    private void updateFallbackMessageAsRecalled(Long conversationId, Long messageId) {
        fallbackMessages.replaceAll(existing -> {
            if (!existing.conversationId().equals(conversationId) || !existing.messageId().equals(messageId)) {
                return existing;
            }
            return new MessageItem(
                    existing.messageId(),
                    existing.clientMsgId(),
                    existing.conversationId(),
                    existing.senderUserId(),
                    1,
                    existing.replyMessageId(),
                    Map.of("text", "消息已撤回", "recalled", true),
                    existing.sendStatus(),
                    existing.sendTime(),
                    existing.readStatus()
            );
        });
    }

    private String fallbackConversationTitle(Long targetUserId) {
        return switch (String.valueOf(targetUserId)) {
            case "10002" -> "阿泽";
            case "10003" -> "小林";
            case "10008" -> "小宇";
            case "10009" -> "Ada";
            case "10010" -> "Bob";
            default -> "会话-" + targetUserId;
        };
    }

    private String fallbackConversationAvatar(Long targetUserId, Integer conversationType) {
        if (conversationType != null && conversationType == 2) {
            return "https://weiyou.local/group/default.png";
        }
        return "https://weiyou.local/avatar/" + targetUserId + ".png";
    }

    private String buildMessageDigest(Integer msgType, Map<String, Object> content) {
        if (msgType != null) {
            if (msgType == 2) {
                return "[图片]";
            }
            if (msgType == 3) {
                return "[文件]";
            }
            if (msgType == 4) {
                return "[语音]";
            }
            if (msgType == 5) {
                return "[视频]";
            }
            if (msgType == 6) {
                return "[位置]";
            }
            if (msgType == 7) {
                return "[名片]";
            }
        }
        Object text = content.get("text");
        return text == null ? "[消息]" : String.valueOf(text);
    }

    private MessageItem toMessageItem(MessageRecordEntity entity) {
        return new MessageItem(
                entity.getMessageId(),
                entity.getClientMsgId(),
                entity.getConversationId(),
                entity.getSenderUserId(),
                entity.getMsgType(),
                entity.getReplyMsgId(),
                Map.of("raw", entity.getContentJson() == null ? "{}" : entity.getContentJson()),
                entity.getSendStatus(),
                entity.getSendTime() == null ? null : entity.getSendTime().toString(),
                0
        );
    }

    private ConversationSettingData toConversationSettingData(ChatPersistenceService.ConversationAggregate aggregate) {
        if (aggregate == null) {
            return null;
        }
        return new ConversationSettingData(
                aggregate.conversation().getConversationId(),
                aggregate.conversation().getConversationType(),
                aggregate.conversation().getConversationType() != null && aggregate.conversation().getConversationType() == 2
                        ? "群聊-" + aggregate.conversation().getConversationId()
                        : "会话-" + aggregate.conversation().getConversationId(),
                aggregate.conversation().getConversationType() != null && aggregate.conversation().getConversationType() == 2
                        ? "https://weiyou.local/group/default.png"
                        : "https://weiyou.local/avatar/default.png",
                aggregate.conversationUser().getTopFlag() != null && aggregate.conversationUser().getTopFlag() == 1,
                aggregate.conversationUser().getMuteFlag() != null && aggregate.conversationUser().getMuteFlag() == 1,
                aggregate.conversationUser().getMarkUnreadFlag() != null && aggregate.conversationUser().getMarkUnreadFlag() == 1,
                aggregate.conversationUser().getUnreadCount(),
                aggregate.conversationUser().getClearBeforeTime() == null ? null : aggregate.conversationUser().getClearBeforeTime().toString()
        );
    }

    public record SendMessageRequest(@NotNull Long conversationId, @NotNull Integer msgType,
                                     @NotBlank String clientMsgId, Long replyMessageId, @NotNull Map<String, Object> content) {
    }

    public record OpenSingleConversationRequest(@NotNull Long targetUserId) {
    }

    public record ConversationSettingRequest(@NotNull Long conversationId, Boolean top, Boolean mute, Boolean markUnread) {
    }

    public record ConversationClearRequest(@NotNull Long conversationId) {
    }

    public record MessageReadRequest(@NotNull Long conversationId, @NotNull Long messageId) {
    }

    public record MessageRevokeRequest(@NotNull Long conversationId, @NotNull Long messageId) {
    }

    public record ConversationItem(Long conversationId, Integer conversationType, String title, String avatar,
                                   Integer unreadCount, Boolean top, Boolean mute, String lastMessageDigest,
                                   String lastMessageTime) {
    }

    public record MessageItem(Long messageId, String clientMsgId, Long conversationId, Long senderUserId,
                              Integer msgType, Long replyMessageId, Map<String, Object> content, Integer sendStatus,
                              String sendTime, Integer readStatus) {
    }

    public record MessageSendResult(Long messageId, String clientMsgId, Integer sendStatus, String sendTime) {
    }

    public record ConversationOpenResult(Long conversationId, Long targetUserId, Integer conversationType) {
    }

    public record ConversationSettingData(Long conversationId, Integer conversationType, String title, String avatar,
                                          Boolean top, Boolean mute, Boolean markUnread, Integer unreadCount, String clearBeforeTime) {
    }

    public record MessageRevokeResult(Long messageId, Long conversationId, Boolean revoked) {
    }
}
