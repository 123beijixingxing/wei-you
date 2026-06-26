package com.weiyou.boot.im.ws;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weiyou.chat.app.service.ChatPersistenceService;
import com.weiyou.chat.domain.entity.MessageRecordEntity;
import com.weiyou.common.security.context.LoginUser;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ChatPersistenceService chatPersistenceService;
    private final ChatRedisRouter chatRedisRouter;
    private final ObjectMapper objectMapper;
    private final OnlineSessionRegistry onlineSessionRegistry;

    public ChatWebSocketHandler(ChatPersistenceService chatPersistenceService,
                                ChatRedisRouter chatRedisRouter,
                                ObjectMapper objectMapper,
                                OnlineSessionRegistry onlineSessionRegistry) {
        this.chatPersistenceService = chatPersistenceService;
        this.chatRedisRouter = chatRedisRouter;
        this.objectMapper = objectMapper;
        this.onlineSessionRegistry = onlineSessionRegistry;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        LoginUser loginUser = (LoginUser) session.getAttributes().get(AuthenticatedHandshakeInterceptor.ATTR_LOGIN_USER);
        if (loginUser == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("unauthorized"));
            return;
        }
        OnlineSessionRegistry.SessionBinding binding = onlineSessionRegistry.register(loginUser, session);
        session.sendMessage(new TextMessage(toJson(event(
                "CONNECT_ACK",
                Map.of(
                        "userId", loginUser.userId(),
                        "deviceId", loginUser.deviceId(),
                        "sessionId", session.getId(),
                        "connectedAt", binding.getConnectedAt(),
                        "lastHeartbeatAt", binding.getLastHeartbeatAt(),
                        "serverTime", Instant.now().toString()
                )
        ))));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> request = readMap(message.getPayload());
        String event = String.valueOf(request.getOrDefault("event", "MESSAGE_SEND"));
        String requestId = String.valueOf(request.getOrDefault("requestId", ""));
        LoginUser loginUser = (LoginUser) session.getAttributes().get(AuthenticatedHandshakeInterceptor.ATTR_LOGIN_USER);

        if ("HEARTBEAT".equals(event)) {
            onlineSessionRegistry.touch(session.getId());
            session.sendMessage(new TextMessage(toJson(event("HEARTBEAT", Map.of(
                    "serverTime", Instant.now().toString(),
                    "sessionId", session.getId()
            )))));
            return;
        }

        if ("MESSAGE_SEND".equals(event)) {
            handleMessageSend(session, loginUser, requestId, request);
            return;
        }

        session.sendMessage(new TextMessage(toJson(event(
                "EVENT_ACK",
                Map.of(
                        "requestId", requestId,
                        "event", event,
                        "serverTime", Instant.now().toString()
                )
        ))));
    }

    private void handleMessageSend(WebSocketSession session,
                                   LoginUser loginUser,
                                   String requestId,
                                   Map<String, Object> request) throws Exception {
        if (loginUser == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("unauthorized"));
            return;
        }
        Map<String, Object> data = asMap(request.get("data"));
        Long conversationId = parseLong(data.get("conversationId"));
        Long replyMessageId = parseLong(data.get("replyMessageId"));
        Integer msgType = parseInteger(data.getOrDefault("msgType", 1));
        Map<String, Object> content = asMap(data.get("content"));
        if (conversationId == null || content.isEmpty()) {
            session.sendMessage(new TextMessage(toJson(event(
                    "EVENT_ACK",
                    Map.of(
                            "requestId", requestId,
                            "event", "MESSAGE_SEND",
                            "sendStatus", 0,
                            "message", "invalid message payload",
                            "serverTime", Instant.now().toString()
                    )
            ))));
            return;
        }

        MessageRecordEntity message;
        try {
            message = chatPersistenceService.appendMessage(
                    loginUser.userId(),
                    conversationId,
                    msgType,
                    requestId,
                    replyMessageId,
                    content
            );
        } catch (RuntimeException exception) {
            message = new MessageRecordEntity();
            message.setMessageId(System.currentTimeMillis());
            message.setConversationId(conversationId);
            message.setClientMsgId(requestId);
            message.setSenderUserId(loginUser.userId());
            message.setMsgType(msgType);
            message.setReplyMsgId(replyMessageId);
            message.setSendStatus(1);
            message.setSendTime(java.time.LocalDateTime.now());
        }
        session.sendMessage(new TextMessage(toJson(event(
                "MESSAGE_ACK",
                Map.of(
                        "requestId", requestId,
                        "conversationId", conversationId,
                        "messageId", message.getMessageId(),
                        "clientMsgId", requestId,
                        "sendStatus", message.getSendStatus(),
                        "sendTime", message.getSendTime() == null ? null : message.getSendTime().toString(),
                        "serverTime", Instant.now().toString()
                )
        ))));

        session.sendMessage(new TextMessage(toJson(event(
                "MESSAGE_RECEIVE",
                Map.of(
                        "conversationId", conversationId,
                        "message", Map.of(
                                "messageId", message.getMessageId(),
                                "clientMsgId", requestId,
                                "conversationId", conversationId,
                                "senderUserId", loginUser.userId(),
                                "msgType", msgType,
                                "replyMessageId", replyMessageId,
                                "content", content,
                                "sendStatus", message.getSendStatus(),
                                "sendTime", message.getSendTime() == null ? null : message.getSendTime().toString()
                        )
                )
        ))));

        publishMessageReceive(message, content);
    }

    private void publishMessageReceive(MessageRecordEntity message, Map<String, Object> content) {
        List<Long> participantUserIds;
        try {
            participantUserIds = chatPersistenceService.listConversationUserIds(message.getConversationId());
        } catch (RuntimeException exception) {
            participantUserIds = List.of(message.getSenderUserId());
        }
        if (participantUserIds.isEmpty()) {
            participantUserIds = List.of(message.getSenderUserId());
        }
        Map<String, Object> payload = event(
                "MESSAGE_RECEIVE",
                Map.of(
                        "conversationId", message.getConversationId(),
                        "message", Map.of(
                                "messageId", message.getMessageId(),
                                "clientMsgId", message.getClientMsgId(),
                                "conversationId", message.getConversationId(),
                                "senderUserId", message.getSenderUserId(),
                                "msgType", message.getMsgType(),
                                "replyMessageId", message.getReplyMsgId(),
                                "content", content,
                                "sendStatus", message.getSendStatus(),
                                "sendTime", message.getSendTime() == null ? null : message.getSendTime().toString()
                        )
                )
        );
        if (participantUserIds.size() == 1 && participantUserIds.contains(message.getSenderUserId())) {
            String payloadText = toJson(payload);
            for (OnlineSessionRegistry.SessionBinding binding : onlineSessionRegistry.findByUserIds(participantUserIds)) {
                try {
                    if (binding.getSession().isOpen()) {
                        binding.getSession().sendMessage(new TextMessage(payloadText));
                    }
                } catch (Exception ignored) {
                }
            }
            return;
        }
        chatRedisRouter.publishMessageReceive(participantUserIds, payload);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        onlineSessionRegistry.remove(session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        onlineSessionRegistry.remove(session.getId());
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR.withReason(exception.getMessage()));
        }
    }

    private Map<String, Object> event(String name, Map<String, Object> data) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event", name);
        payload.put("data", data);
        return payload;
    }

    private Map<String, Object> readMap(String text) {
        try {
            return objectMapper.readValue(text, MAP_TYPE);
        } catch (Exception exception) {
            return Map.of("event", "MESSAGE_SEND", "raw", text);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(Objects.toString(value));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Integer parseInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(Objects.toString(value));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("failed to serialize websocket payload", exception);
        }
    }
}
