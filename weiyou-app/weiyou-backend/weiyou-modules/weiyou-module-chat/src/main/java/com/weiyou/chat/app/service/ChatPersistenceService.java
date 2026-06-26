package com.weiyou.chat.app.service;

import com.weiyou.chat.domain.entity.ConversationEntity;
import com.weiyou.chat.domain.entity.ConversationUserEntity;
import com.weiyou.chat.domain.entity.MessageRecordEntity;
import java.util.List;
import java.util.Map;

public interface ChatPersistenceService {

    List<ConversationAggregate> listConversations(Long userId);

    ConversationAggregate getConversation(Long userId, Long conversationId);

    ConversationEntity openSingleConversation(Long userId, Long targetUserId);

    List<MessageRecordEntity> listMessages(Long userId, Long conversationId);

    List<Long> listConversationUserIds(Long conversationId);

    MessageRecordEntity appendMessage(Long userId, Long conversationId, Integer msgType, String clientMsgId, Long replyMessageId, Map<String, Object> content);

    void markRead(Long userId, Long conversationId, Long messageId);

    MessageRecordEntity revokeMessage(Long userId, Long conversationId, Long messageId);

    ConversationUserEntity updateConversationSetting(Long userId, Long conversationId, Boolean top, Boolean mute, Boolean markUnread);

    ConversationUserEntity clearConversation(Long userId, Long conversationId);

    record ConversationAggregate(ConversationEntity conversation, ConversationUserEntity conversationUser) {
    }
}
