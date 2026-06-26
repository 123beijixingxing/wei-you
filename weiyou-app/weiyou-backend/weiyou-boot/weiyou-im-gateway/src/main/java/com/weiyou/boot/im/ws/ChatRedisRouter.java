package com.weiyou.boot.im.ws;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
public class ChatRedisRouter {

    public static final String MESSAGE_RECEIVE_TOPIC = "wy:im:topic:message_receive";

    private static final Logger log = LoggerFactory.getLogger(ChatRedisRouter.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final OnlineSessionRegistry onlineSessionRegistry;

    public ChatRedisRouter(StringRedisTemplate stringRedisTemplate,
                           ObjectMapper objectMapper,
                           OnlineSessionRegistry onlineSessionRegistry) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.onlineSessionRegistry = onlineSessionRegistry;
    }

    public void publishMessageReceive(Collection<Long> recipientUserIds, Map<String, Object> payload) {
        try {
            String message = objectMapper.writeValueAsString(Map.of(
                    "recipientUserIds", recipientUserIds,
                    "payload", payload
            ));
            stringRedisTemplate.convertAndSend(MESSAGE_RECEIVE_TOPIC, message);
        } catch (Exception exception) {
            log.warn("failed to publish chat message via redis, falling back to local delivery", exception);
            try {
                String payloadText = objectMapper.writeValueAsString(payload);
                deliverToLocalSessions(recipientUserIds, payloadText);
            } catch (Exception innerException) {
                throw new IllegalStateException("failed to publish chat message", innerException);
            }
        }
    }

    public void onTopicMessage(String rawMessage) {
        try {
            Map<String, Object> envelope = objectMapper.readValue(rawMessage, MAP_TYPE);
            List<Long> recipientUserIds = toLongList(envelope.get("recipientUserIds"));
            Object payload = envelope.get("payload");
            String payloadText = objectMapper.writeValueAsString(payload);
            deliverToLocalSessions(recipientUserIds, payloadText);
        } catch (Exception exception) {
            log.warn("failed to route redis chat event", exception);
        }
    }

    private void deliverToLocalSessions(Collection<Long> recipientUserIds, String payloadText) throws Exception {
        for (OnlineSessionRegistry.SessionBinding binding : onlineSessionRegistry.findByUserIds(recipientUserIds)) {
            WebSocketSession session = binding.getSession();
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(payloadText));
            }
        }
    }

    private List<Long> toLongList(Object value) {
        if (!(value instanceof Collection<?> collection)) {
            return List.of();
        }
        return collection.stream()
                .map(item -> {
                    if (item instanceof Number number) {
                        return number.longValue();
                    }
                    try {
                        return Long.parseLong(Objects.toString(item));
                    } catch (NumberFormatException exception) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
