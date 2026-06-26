package com.weiyou.chat.controller;

import com.weiyou.chat.app.service.ChatPersistenceService;
import com.weiyou.chat.domain.entity.ConversationEntity;
import com.weiyou.chat.domain.entity.ConversationUserEntity;
import com.weiyou.chat.domain.entity.MessageRecordEntity;
import com.weiyou.common.security.context.LoginUser;
import com.weiyou.common.security.context.UserContext;
import com.weiyou.common.web.exception.GlobalExceptionHandler;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatControllerTest {

    private ChatPersistenceService chatPersistenceService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        chatPersistenceService = mock(ChatPersistenceService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ChatController(chatPersistenceService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        UserContext.set(new LoginUser(10001L, "tester", "device-demo-1"));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void shouldListPersistedConversations() throws Exception {
        ConversationEntity conversation = new ConversationEntity();
        conversation.setConversationId(90001L);
        conversation.setConversationType(1);
        conversation.setLastMsgDigest("hello");
        conversation.setLastMsgTime(LocalDateTime.of(2026, 5, 11, 9, 30));

        ConversationUserEntity relation = new ConversationUserEntity();
        relation.setConversationId(90001L);
        relation.setUserId(10001L);
        relation.setUnreadCount(2);
        relation.setTopFlag(1);
        relation.setMuteFlag(0);

        when(chatPersistenceService.listConversations(10001L))
                .thenReturn(List.of(new ChatPersistenceService.ConversationAggregate(conversation, relation)));

        mockMvc.perform(get("/chat/conversation/list?pageSize=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].conversationId").value(90001))
                .andExpect(jsonPath("$.data.list[0].title").value("会话-90001"))
                .andExpect(jsonPath("$.data.list[0].unreadCount").value(2));
    }

    @Test
    void shouldSendMessageUsingCurrentUser() throws Exception {
        MessageRecordEntity entity = new MessageRecordEntity();
        entity.setMessageId(70003L);
        entity.setConversationId(90001L);
        entity.setClientMsgId("cmsg-test-1");
        entity.setSenderUserId(10001L);
        entity.setMsgType(1);
        entity.setContentJson("{\"text\":\"hello\"}");
        entity.setSendStatus(1);
        entity.setSendTime(LocalDateTime.of(2026, 5, 11, 10, 0));

        when(chatPersistenceService.appendMessage(anyLong(), anyLong(), anyInt(), anyString(), isNull(), any()))
                .thenReturn(entity);

        mockMvc.perform(post("/chat/message/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "conversationId": 90001,
                                  "msgType": 1,
                                  "clientMsgId": "cmsg-test-1",
                                  "content": {
                                    "text": "hello"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.messageId").value(70003))
                .andExpect(jsonPath("$.data.clientMsgId").value("cmsg-test-1"))
                .andExpect(jsonPath("$.data.sendStatus").value(1));

        ArgumentCaptor<Map<String, Object>> contentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(chatPersistenceService).appendMessage(eq(10001L), eq(90001L), eq(1), eq("cmsg-test-1"), isNull(), contentCaptor.capture());
        Object text = contentCaptor.getValue().get("text");
        org.junit.jupiter.api.Assertions.assertEquals("hello", text);
    }
}
