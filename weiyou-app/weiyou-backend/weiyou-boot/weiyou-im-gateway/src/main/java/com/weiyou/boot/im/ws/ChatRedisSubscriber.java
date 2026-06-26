package com.weiyou.boot.im.ws;

import java.nio.charset.StandardCharsets;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class ChatRedisSubscriber implements MessageListener {

    private final ChatRedisRouter chatRedisRouter;

    public ChatRedisSubscriber(ChatRedisRouter chatRedisRouter) {
        this.chatRedisRouter = chatRedisRouter;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        chatRedisRouter.onTopicMessage(new String(message.getBody(), StandardCharsets.UTF_8));
    }
}
