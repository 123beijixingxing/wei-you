package com.weiyou.moment.app.service;

import com.weiyou.moment.domain.entity.MomentPostEntity;
import com.weiyou.moment.domain.entity.MomentMediaEntity;
import com.weiyou.moment.domain.entity.MomentCommentEntity;
import java.util.List;

public interface MomentPersistenceService {

    List<MomentAggregate> listTimeline(Long userId);

    MomentAggregate createMoment(Long userId, String content, List<String> mediaUrls, String visibleScope);

    int toggleLike(Long momentId, String action);

    CommentAggregate addComment(Long userId, Long momentId, String content, Long replyCommentId);

    int deleteComment(Long userId, Long commentId);

    record MomentAggregate(MomentPostEntity post, List<MomentMediaEntity> mediaList, List<MomentCommentEntity> commentList) {
    }

    record CommentAggregate(MomentCommentEntity comment, int commentCount) {
    }
}
