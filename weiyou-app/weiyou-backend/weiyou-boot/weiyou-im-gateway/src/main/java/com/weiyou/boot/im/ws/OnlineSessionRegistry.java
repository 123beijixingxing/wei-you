package com.weiyou.boot.im.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weiyou.common.security.context.LoginUser;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class OnlineSessionRegistry {

    private static final Logger log = LoggerFactory.getLogger(OnlineSessionRegistry.class);
    private static final Duration SESSION_TTL = Duration.ofMinutes(2);
    private static final String SESSION_KEY_PREFIX = "wy:im:session:";
    private static final String USER_SESSION_SET_PREFIX = "wy:im:user:sessions:";

    private final Map<String, SessionBinding> sessions = new ConcurrentHashMap<>();
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public OnlineSessionRegistry(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public SessionBinding register(LoginUser loginUser, WebSocketSession session) {
        String now = Instant.now().toString();
        SessionBinding binding = new SessionBinding(loginUser, session, now, now);
        sessions.put(session.getId(), binding);
        persist(binding);
        return binding;
    }

    public void remove(String sessionId) {
        SessionBinding binding = sessions.remove(sessionId);
        if (binding != null) {
          removePersisted(binding);
        }
    }

    public Optional<SessionBinding> get(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    public void touch(String sessionId) {
        SessionBinding binding = sessions.get(sessionId);
        if (binding != null) {
            binding.setLastHeartbeatAt(Instant.now().toString());
            persist(binding);
        }
    }

    public Collection<SessionBinding> all() {
        return sessions.values();
    }

    public List<SessionBinding> findByUserIds(Collection<Long> userIds) {
        return sessions.values().stream()
                .filter(binding -> userIds.contains(binding.getLoginUser().userId()))
                .collect(Collectors.toList());
    }

    public static class SessionBinding {

        private final LoginUser loginUser;
        private final WebSocketSession session;
        private final String connectedAt;
        private String lastHeartbeatAt;

        public SessionBinding(LoginUser loginUser, WebSocketSession session, String connectedAt, String lastHeartbeatAt) {
            this.loginUser = loginUser;
            this.session = session;
            this.connectedAt = connectedAt;
            this.lastHeartbeatAt = lastHeartbeatAt;
        }

        public LoginUser getLoginUser() {
            return loginUser;
        }

        public WebSocketSession getSession() {
            return session;
        }

        public String getConnectedAt() {
            return connectedAt;
        }

        public String getLastHeartbeatAt() {
            return lastHeartbeatAt;
        }

        public void setLastHeartbeatAt(String lastHeartbeatAt) {
            this.lastHeartbeatAt = lastHeartbeatAt;
        }
    }

    private void persist(SessionBinding binding) {
        try {
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("sessionId", binding.getSession().getId());
            metadata.put("userId", binding.getLoginUser().userId());
            metadata.put("deviceId", binding.getLoginUser().deviceId());
            metadata.put("connectedAt", binding.getConnectedAt());
            metadata.put("lastHeartbeatAt", binding.getLastHeartbeatAt());
            String sessionKey = sessionKey(binding.getSession().getId());
            String userSessionsKey = userSessionsKey(binding.getLoginUser().userId());
            stringRedisTemplate.opsForValue().set(sessionKey, objectMapper.writeValueAsString(metadata), SESSION_TTL);
            stringRedisTemplate.opsForSet().add(userSessionsKey, binding.getSession().getId());
            stringRedisTemplate.expire(userSessionsKey, SESSION_TTL);
        } catch (Exception exception) {
            log.warn("failed to persist online session {}", binding.getSession().getId(), exception);
        }
    }

    private void removePersisted(SessionBinding binding) {
        try {
            String sessionId = binding.getSession().getId();
            stringRedisTemplate.delete(sessionKey(sessionId));
            String userSessionsKey = userSessionsKey(binding.getLoginUser().userId());
            stringRedisTemplate.opsForSet().remove(userSessionsKey, sessionId);
        } catch (Exception exception) {
            log.warn("failed to remove persisted online session {}", binding.getSession().getId(), exception);
        }
    }

    private String sessionKey(String sessionId) {
        return SESSION_KEY_PREFIX + sessionId;
    }

    private String userSessionsKey(Long userId) {
        return USER_SESSION_SET_PREFIX + userId;
    }
}
