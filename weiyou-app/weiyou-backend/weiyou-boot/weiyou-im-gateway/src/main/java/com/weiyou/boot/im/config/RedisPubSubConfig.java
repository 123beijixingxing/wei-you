package com.weiyou.boot.im.config;

import com.weiyou.boot.im.ws.ChatRedisRouter;
import com.weiyou.boot.im.ws.ChatRedisSubscriber;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration(proxyBeanMethods = false)
public class RedisPubSubConfig {

    @Bean
    @ConditionalOnProperty(prefix = "weiyou.im", name = "redis-pubsub-enabled", havingValue = "true", matchIfMissing = true)
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory,
                                                                       ChatRedisSubscriber chatRedisSubscriber) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(chatRedisSubscriber, new ChannelTopic(ChatRedisRouter.MESSAGE_RECEIVE_TOPIC));
        return container;
    }
}
