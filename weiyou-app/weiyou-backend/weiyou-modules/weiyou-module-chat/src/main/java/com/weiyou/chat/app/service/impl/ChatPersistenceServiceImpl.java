package com.weiyou.chat.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weiyou.chat.app.service.ChatPersistenceService;
import com.weiyou.chat.domain.entity.ConversationEntity;
import com.weiyou.chat.domain.entity.ConversationUserEntity;
import com.weiyou.chat.domain.entity.MessageRecordEntity;
import com.weiyou.chat.infra.persistence.mapper.ConversationMapper;
import com.weiyou.chat.infra.persistence.mapper.ConversationUserMapper;
import com.weiyou.chat.infra.persistence.mapper.MessageRecordMapper;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatPersistenceServiceImpl implements ChatPersistenceService {

    private final ConversationMapper conversationMapper;
    private final ConversationUserMapper conversationUserMapper;
    private final MessageRecordMapper messageRecordMapper;
    private final ObjectMapper objectMapper;

    public ChatPersistenceServiceImpl(ConversationMapper conversationMapper,
                                      ConversationUserMapper conversationUserMapper,
                                      MessageRecordMapper messageRecordMapper,
                                      ObjectMapper objectMapper) {
        this.conversationMapper = conversationMapper;
        this.conversationUserMapper = conversationUserMapper;
        this.messageRecordMapper = messageRecordMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<ConversationAggregate> listConversations(Long userId) {
        List<ConversationUserEntity> relationList = conversationUserMapper.selectList(
                new LambdaQueryWrapper<ConversationUserEntity>()
                        .eq(ConversationUserEntity::getUserId, userId)
                        .orderByDesc(ConversationUserEntity::getTopFlag, ConversationUserEntity::getSortTime)
        );
        if (relationList.isEmpty()) {
            return List.of();
        }
        List<Long> conversationIds = relationList.stream().map(ConversationUserEntity::getConversationId).toList();
        Map<Long, ConversationEntity> conversationMap = conversationMapper.selectList(
                new LambdaQueryWrapper<ConversationEntity>().in(ConversationEntity::getConversationId, conversationIds)
        ).stream().collect(Collectors.toMap(ConversationEntity::getConversationId, Function.identity()));
        return relationList.stream()
                .map(item -> new ConversationAggregate(conversationMap.get(item.getConversationId()), item))
                .filter(item -> item.conversation() != null)
                .toList();
    }

    @Override
    public ConversationAggregate getConversation(Long userId, Long conversationId) {
        ConversationEntity conversation = conversationMapper.selectOne(
                new LambdaQueryWrapper<ConversationEntity>()
                        .eq(ConversationEntity::getConversationId, conversationId)
                        .last("limit 1")
        );
        if (conversation == null) {
            return null;
        }
        ConversationUserEntity relation = ensureConversationUser(conversationId, userId, 0L);
        return new ConversationAggregate(conversation, relation);
    }

    @Override
    @Transactional
    public ConversationEntity openSingleConversation(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new IllegalArgumentException("cannot open conversation with self");
        }
        long relationKey = buildSingleConversationKey(userId, targetUserId);
        ConversationEntity conversation = conversationMapper.selectOne(
                new LambdaQueryWrapper<ConversationEntity>()
                        .eq(ConversationEntity::getConversationType, 1)
                        .eq(ConversationEntity::getBizId, relationKey)
                        .last("limit 1")
        );
        if (conversation == null) {
          conversation = new ConversationEntity();
          conversation.setConversationId(System.currentTimeMillis());
          conversation.setConversationNo("conv_" + conversation.getConversationId());
          conversation.setConversationType(1);
          conversation.setBizId(relationKey);
          conversation.setStatus(0);
          conversationMapper.insert(conversation);
        }
        ensureConversationUser(conversation.getConversationId(), userId, 0L);
        ensureConversationUser(conversation.getConversationId(), targetUserId, 0L);
        return conversation;
    }

    @Override
    public List<MessageRecordEntity> listMessages(Long userId, Long conversationId) {
        ConversationUserEntity relation = ensureConversationUser(conversationId, userId, 0L);
        LambdaQueryWrapper<MessageRecordEntity> wrapper = new LambdaQueryWrapper<MessageRecordEntity>()
                .eq(MessageRecordEntity::getConversationId, conversationId)
                .orderByDesc(MessageRecordEntity::getSeqNo)
                .last("limit 50");
        if (relation.getClearBeforeTime() != null) {
            wrapper.gt(MessageRecordEntity::getSendTime, relation.getClearBeforeTime());
        }
        return messageRecordMapper.selectList(wrapper).stream().sorted(Comparator.comparing(MessageRecordEntity::getSeqNo)).toList();
    }

    @Override
    public List<Long> listConversationUserIds(Long conversationId) {
        return conversationUserMapper.selectList(
                new LambdaQueryWrapper<ConversationUserEntity>()
                        .eq(ConversationUserEntity::getConversationId, conversationId)
        ).stream().map(ConversationUserEntity::getUserId).distinct().toList();
    }

    @Override
    @Transactional
    public MessageRecordEntity appendMessage(Long userId, Long conversationId, Integer msgType, String clientMsgId, Long replyMessageId, Map<String, Object> content) {
        LocalDateTime now = LocalDateTime.now();
        ConversationEntity conversation = conversationMapper.selectOne(
                new LambdaQueryWrapper<ConversationEntity>()
                        .eq(ConversationEntity::getConversationId, conversationId)
                        .last("limit 1")
        );
        if (conversation == null) {
            conversation = new ConversationEntity();
            conversation.setConversationId(conversationId);
            conversation.setConversationNo("conv_" + conversationId);
            conversation.setConversationType(1);
            conversation.setStatus(0);
            conversationMapper.insert(conversation);
        }

        MessageRecordEntity lastMessage = messageRecordMapper.selectOne(
                new LambdaQueryWrapper<MessageRecordEntity>()
                        .eq(MessageRecordEntity::getConversationId, conversationId)
                        .orderByDesc(MessageRecordEntity::getSeqNo)
                        .last("limit 1")
        );
        long nextSeq = lastMessage == null || lastMessage.getSeqNo() == null ? 1L : lastMessage.getSeqNo() + 1L;
        MessageRecordEntity message = new MessageRecordEntity();
        message.setMessageId(System.currentTimeMillis());
        message.setConversationId(conversationId);
        message.setSeqNo(nextSeq);
        message.setClientMsgId(clientMsgId);
        message.setSenderUserId(userId);
        message.setMsgType(msgType);
        message.setReplyMsgId(replyMessageId);
        message.setContentJson(toJson(content));
        message.setSendStatus(1);
        message.setSendTime(now);
        messageRecordMapper.insert(message);

        conversation.setLastMsgId(message.getMessageId());
        conversation.setLastMsgTime(now);
        conversation.setLastMsgDigest(buildDigest(msgType, content));
        conversationMapper.updateById(conversation);

        ConversationUserEntity relation = ensureConversationUser(conversationId, userId, nextSeq);
        relation.setSortTime(now);
        relation.setLastReadSeqNo(nextSeq);
        conversationUserMapper.updateById(relation);

        return message;
    }

    @Override
    @Transactional
    public void markRead(Long userId, Long conversationId, Long messageId) {
        ConversationUserEntity relation = conversationUserMapper.selectOne(
                new LambdaQueryWrapper<ConversationUserEntity>()
                        .eq(ConversationUserEntity::getConversationId, conversationId)
                        .eq(ConversationUserEntity::getUserId, userId)
                        .last("limit 1")
        );
        if (relation == null) {
            return;
        }
        MessageRecordEntity message = messageRecordMapper.selectOne(
                new LambdaQueryWrapper<MessageRecordEntity>()
                        .eq(MessageRecordEntity::getMessageId, messageId)
                        .last("limit 1")
        );
        relation.setUnreadCount(0);
        if (message != null) {
            relation.setLastReadSeqNo(message.getSeqNo());
        }
        conversationUserMapper.updateById(relation);
    }

    @Override
    @Transactional
    public MessageRecordEntity revokeMessage(Long userId, Long conversationId, Long messageId) {
        MessageRecordEntity message = messageRecordMapper.selectOne(
                new LambdaQueryWrapper<MessageRecordEntity>()
                        .eq(MessageRecordEntity::getMessageId, messageId)
                        .eq(MessageRecordEntity::getConversationId, conversationId)
                        .last("limit 1")
        );
        if (message == null) {
            throw new IllegalArgumentException("message not found");
        }
        if (!userId.equals(message.getSenderUserId())) {
            throw new IllegalArgumentException("cannot revoke others message");
        }
        message.setMsgType(1);
        message.setContentJson(toJson(Map.of(
                "text", "消息已撤回",
                "recalled", true
        )));
        messageRecordMapper.updateById(message);

        ConversationEntity conversation = conversationMapper.selectOne(
                new LambdaQueryWrapper<ConversationEntity>()
                        .eq(ConversationEntity::getConversationId, conversationId)
                        .last("limit 1")
        );
        if (conversation != null && messageId.equals(conversation.getLastMsgId())) {
            conversation.setLastMsgDigest("[消息已撤回]");
            conversationMapper.updateById(conversation);
        }
        return message;
    }

    @Override
    @Transactional
    public ConversationUserEntity updateConversationSetting(Long userId, Long conversationId, Boolean top, Boolean mute, Boolean markUnread) {
        ConversationUserEntity relation = ensureConversationUser(conversationId, userId, 0L);
        if (top != null) {
            relation.setTopFlag(Boolean.TRUE.equals(top) ? 1 : 0);
        }
        if (mute != null) {
            relation.setMuteFlag(Boolean.TRUE.equals(mute) ? 1 : 0);
        }
        if (markUnread != null) {
            relation.setMarkUnreadFlag(Boolean.TRUE.equals(markUnread) ? 1 : 0);
            relation.setUnreadCount(Boolean.TRUE.equals(markUnread) ? Math.max(relation.getUnreadCount() == null ? 0 : relation.getUnreadCount(), 1) : 0);
        }
        relation.setSortTime(LocalDateTime.now());
        conversationUserMapper.updateById(relation);
        return relation;
    }

    @Override
    @Transactional
    public ConversationUserEntity clearConversation(Long userId, Long conversationId) {
        ConversationUserEntity relation = ensureConversationUser(conversationId, userId, 0L);
        relation.setClearBeforeTime(LocalDateTime.now());
        relation.setUnreadCount(0);
        conversationUserMapper.updateById(relation);
        return relation;
    }

    private String toJson(Map<String, Object> content) {
        try {
            return objectMapper.writeValueAsString(content);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to serialize message content", exception);
        }
    }

    private String buildDigest(Integer msgType, Map<String, Object> content) {
        if (msgType != null && msgType == 1 && content.get("text") != null) {
            return String.valueOf(content.get("text"));
        }
        if (msgType != null && msgType == 2) {
            return "[图片]";
        }
        if (msgType != null && msgType == 3) {
            Object fileName = content.get("fileName");
            return fileName == null ? "[文件]" : "[文件] " + fileName;
        }
        if (msgType != null && msgType == 4) {
            Object durationSec = content.get("durationSec");
            return durationSec == null ? "[语音]" : "[语音] " + durationSec + "\"";
        }
        if (msgType != null && msgType == 5) {
            Object durationSec = content.get("durationSec");
            return durationSec == null ? "[视频]" : "[视频] " + durationSec + "\"";
        }
        if (msgType != null && msgType == 6) {
            Object locationName = content.get("locationName");
            return locationName == null ? "[位置]" : "[位置] " + locationName;
        }
        if (msgType != null && msgType == 7) {
            Object cardNickname = content.get("cardNickname");
            return cardNickname == null ? "[名片]" : "[名片] " + cardNickname;
        }
        return "[消息]";
    }

    private ConversationUserEntity ensureConversationUser(Long conversationId, Long userId, Long lastReadSeqNo) {
        ConversationUserEntity relation = conversationUserMapper.selectOne(
                new LambdaQueryWrapper<ConversationUserEntity>()
                        .eq(ConversationUserEntity::getConversationId, conversationId)
                        .eq(ConversationUserEntity::getUserId, userId)
                        .last("limit 1")
        );
        if (relation != null) {
            return relation;
        }
        relation = new ConversationUserEntity();
        relation.setConversationId(conversationId);
        relation.setUserId(userId);
        relation.setUnreadCount(0);
        relation.setTopFlag(0);
        relation.setMuteFlag(0);
        relation.setMarkUnreadFlag(0);
        relation.setLastReadSeqNo(lastReadSeqNo);
        relation.setSortTime(LocalDateTime.now());
        conversationUserMapper.insert(relation);
        return relation;
    }

    private long buildSingleConversationKey(Long firstUserId, Long secondUserId) {
        long left = Math.min(firstUserId, secondUserId);
        long right = Math.max(firstUserId, secondUserId);
        return left * 1_000_000L + right;
    }
}
