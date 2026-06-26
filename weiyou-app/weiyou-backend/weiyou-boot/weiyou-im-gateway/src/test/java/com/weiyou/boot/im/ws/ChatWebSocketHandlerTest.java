package com.weiyou.boot.im.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weiyou.chat.app.service.ChatPersistenceService;
import com.weiyou.chat.domain.entity.MessageRecordEntity;
import com.weiyou.common.security.context.LoginUser;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatWebSocketHandlerTest {

    @Test
    void shouldSendConnectAckOnConnectionEstablished() throws Exception {
        ChatPersistenceService chatService = mock(ChatPersistenceService.class);
        ChatRedisRouter router = mock(ChatRedisRouter.class);
        OnlineSessionRegistry registry = mock(OnlineSessionRegistry.class);
        ChatWebSocketHandler handler = new ChatWebSocketHandler(chatService, router, new ObjectMapper(), registry);

        WebSocketSession session = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(AuthenticatedHandshakeInterceptor.ATTR_LOGIN_USER, new LoginUser(10001L, "tester", "device-a"));
        when(session.getAttributes()).thenReturn(attributes);
        when(session.getId()).thenReturn("session-1");
        when(registry.register(any(), eq(session))).thenReturn(
                new OnlineSessionRegistry.SessionBinding(new LoginUser(10001L, "tester", "device-a"), session, "connected", "heartbeat")
        );

        handler.afterConnectionEstablished(session);

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session).sendMessage(captor.capture());
        Assertions.assertTrue(captor.getValue().getPayload().contains("CONNECT_ACK"));
    }

    @Test
    void shouldAppendMessageAndPublishRedisEvent() throws Exception {
        ChatPersistenceService chatService = mock(ChatPersistenceService.class);
        ChatRedisRouter router = mock(ChatRedisRouter.class);
        OnlineSessionRegistry registry = mock(OnlineSessionRegistry.class);
        ChatWebSocketHandler handler = new ChatWebSocketHandler(chatService, router, new ObjectMapper(), registry);

        WebSocketSession session = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(AuthenticatedHandshakeInterceptor.ATTR_LOGIN_USER, new LoginUser(10001L, "tester", "device-a"));
        when(session.getAttributes()).thenReturn(attributes);
        when(session.getId()).thenReturn("session-1");

        MessageRecordEntity message = new MessageRecordEntity();
        message.setMessageId(70003L);
        message.setConversationId(90001L);
        message.setClientMsgId("req-001");
        message.setSenderUserId(10001L);
        message.setMsgType(1);
        message.setSendStatus(1);
        message.setSendTime(LocalDateTime.of(2026, 5, 13, 10, 0));
        when(chatService.appendMessage(eq(10001L), eq(90001L), eq(1), eq("req-001"), org.mockito.ArgumentMatchers.<Long>isNull(), org.mockito.ArgumentMatchers.<Map<String, Object>>any())).thenReturn(message);
        when(chatService.listConversationUserIds(90001L)).thenReturn(List.of(10001L, 10002L));

        handler.handleTextMessage(session, new TextMessage("""
                {
                  "event": "MESSAGE_SEND",
                  "requestId": "req-001",
                  "data": {
                    "conversationId": 90001,
                    "msgType": 1,
                    "content": {
                      "text": "hello"
                    }
                  }
                }
                """));

        ArgumentCaptor<Map<String, Object>> contentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(chatService).appendMessage(eq(10001L), eq(90001L), eq(1), eq("req-001"), org.mockito.ArgumentMatchers.<Long>isNull(), contentCaptor.capture());
        Assertions.assertEquals("hello", contentCaptor.getValue().get("text"));
        verify(router).publishMessageReceive(eq(List.of(10001L, 10002L)), any());

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session).sendMessage(captor.capture());
        Assertions.assertTrue(captor.getValue().getPayload().contains("MESSAGE_ACK"));
    }

    @Test
    void shouldRespondHeartbeat() throws Exception {
        ChatPersistenceService chatService = mock(ChatPersistenceService.class);
        ChatRedisRouter router = mock(ChatRedisRouter.class);
        OnlineSessionRegistry registry = mock(OnlineSessionRegistry.class);
        ChatWebSocketHandler handler = new ChatWebSocketHandler(chatService, router, new ObjectMapper(), registry);

        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getAttributes()).thenReturn(Map.of(AuthenticatedHandshakeInterceptor.ATTR_LOGIN_USER, new LoginUser(10001L, "tester", "device-a")));
        when(session.getId()).thenReturn("session-1");

        handler.handleTextMessage(session, new TextMessage("""
                {
                  "event": "HEARTBEAT",
                  "requestId": "hb-1",
                  "data": {}
                }
                """));

        verify(registry).touch("session-1");
        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session).sendMessage(captor.capture());
        Assertions.assertTrue(captor.getValue().getPayload().contains("HEARTBEAT"));
    }
}
