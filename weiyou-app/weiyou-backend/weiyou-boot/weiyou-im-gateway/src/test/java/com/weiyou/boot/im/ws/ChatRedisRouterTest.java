package com.weiyou.boot.im.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weiyou.common.security.context.LoginUser;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatRedisRouterTest {

    @Test
    void shouldPublishRedisTopicMessage() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        OnlineSessionRegistry registry = mock(OnlineSessionRegistry.class);
        ChatRedisRouter router = new ChatRedisRouter(redisTemplate, new ObjectMapper(), registry);

        router.publishMessageReceive(List.of(10001L, 10002L), Map.of("event", "MESSAGE_RECEIVE"));

        verify(redisTemplate).convertAndSend(eq(ChatRedisRouter.MESSAGE_RECEIVE_TOPIC), any(String.class));
    }

    @Test
    void shouldDeliverRedisTopicMessageToLocalSessions() throws Exception {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        OnlineSessionRegistry registry = mock(OnlineSessionRegistry.class);
        ObjectMapper objectMapper = new ObjectMapper();
        ChatRedisRouter router = new ChatRedisRouter(redisTemplate, objectMapper, registry);

        WebSocketSession session = mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);
        OnlineSessionRegistry.SessionBinding binding =
                new OnlineSessionRegistry.SessionBinding(new LoginUser(10001L, "tester", "device-a"), session, "now", "now");
        when(registry.findByUserIds(any())).thenReturn(List.of(binding));

        String message = objectMapper.writeValueAsString(Map.of(
                "recipientUserIds", List.of(10001L),
                "payload", Map.of("event", "MESSAGE_RECEIVE", "data", Map.of("conversationId", 90001L))
        ));

        router.onTopicMessage(message);

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session).sendMessage(captor.capture());
        Assertions.assertTrue(captor.getValue().getPayload().contains("MESSAGE_RECEIVE"));
    }
}
