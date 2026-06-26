package com.weiyou.moment.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.weiyou.common.core.exception.BusinessException;
import com.weiyou.moment.app.service.MomentPersistenceService;
import com.weiyou.moment.domain.entity.MomentCommentEntity;
import com.weiyou.moment.domain.entity.MomentMediaEntity;
import com.weiyou.moment.domain.entity.MomentPostEntity;
import com.weiyou.moment.infra.persistence.mapper.MomentCommentMapper;
import com.weiyou.moment.infra.persistence.mapper.MomentMediaMapper;
import com.weiyou.moment.infra.persistence.mapper.MomentPostMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MomentPersistenceServiceImpl implements MomentPersistenceService {

    private final MomentPostMapper momentPostMapper;
    private final MomentCommentMapper momentCommentMapper;
    private final MomentMediaMapper momentMediaMapper;

    public MomentPersistenceServiceImpl(MomentPostMapper momentPostMapper,
                                        MomentCommentMapper momentCommentMapper,
                                        MomentMediaMapper momentMediaMapper) {
        this.momentPostMapper = momentPostMapper;
        this.momentCommentMapper = momentCommentMapper;
        this.momentMediaMapper = momentMediaMapper;
    }

    @Override
    public List<MomentAggregate> listTimeline(Long userId) {
        List<MomentPostEntity> posts = momentPostMapper.selectList(
                new LambdaQueryWrapper<MomentPostEntity>()
                        .orderByDesc(MomentPostEntity::getCreatedAt)
                        .last("limit 20")
        );
        if (posts.isEmpty()) {
          return List.of();
        }
        List<Long> momentIds = posts.stream().map(MomentPostEntity::getMomentId).toList();
        Map<Long, List<MomentMediaEntity>> mediaMap = momentMediaMapper.selectList(
                new LambdaQueryWrapper<MomentMediaEntity>()
                        .in(MomentMediaEntity::getMomentId, momentIds)
                        .orderByAsc(MomentMediaEntity::getSortNo)
        ).stream().collect(Collectors.groupingBy(MomentMediaEntity::getMomentId));
        Map<Long, List<MomentCommentEntity>> commentMap = momentCommentMapper.selectList(
                new LambdaQueryWrapper<MomentCommentEntity>()
                        .in(MomentCommentEntity::getMomentId, momentIds)
                        .eq(MomentCommentEntity::getStatus, 0)
                        .orderByAsc(MomentCommentEntity::getCreatedAt)
        ).stream().collect(Collectors.groupingBy(MomentCommentEntity::getMomentId));
        return posts.stream()
                .map(post -> new MomentAggregate(
                        post,
                        mediaMap.getOrDefault(post.getMomentId(), Collections.emptyList()),
                        commentMap.getOrDefault(post.getMomentId(), Collections.emptyList())
                ))
                .toList();
    }

    @Override
    @Transactional
    public MomentAggregate createMoment(Long userId, String content, List<String> mediaUrls, String visibleScope) {
        MomentPostEntity entity = new MomentPostEntity();
        entity.setMomentId(System.currentTimeMillis());
        entity.setAuthorUserId(userId);
        entity.setContentText(content);
        entity.setMediaCount(mediaUrls == null ? 0 : mediaUrls.size());
        entity.setVisibleType(mapVisibleType(visibleScope));
        entity.setCommentCount(0);
        entity.setLikeCount(0);
        entity.setPublishStatus(0);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        momentPostMapper.insert(entity);

        List<MomentMediaEntity> mediaList = (mediaUrls == null ? List.<String>of() : mediaUrls).stream()
                .filter(url -> url != null && !url.isBlank())
                .map(new Function<String, MomentMediaEntity>() {
                    private int sortNo = 0;

                    @Override
                    public MomentMediaEntity apply(String url) {
                        MomentMediaEntity media = new MomentMediaEntity();
                        media.setMomentId(entity.getMomentId());
                        media.setSortNo(++sortNo);
                        media.setMediaType(1);
                        media.setMediaUrl(url);
                        media.setCoverUrl(url);
                        momentMediaMapper.insert(media);
                        return media;
                    }
                })
                .toList();

        return new MomentAggregate(entity, mediaList, List.of());
    }

    private Integer mapVisibleType(String visibleScope) {
        if (visibleScope == null || visibleScope.isBlank()) {
            return 0;
        }
        return switch (visibleScope) {
            case "private" -> 1;
            case "partial" -> 2;
            case "exclude" -> 3;
            default -> 0;
        };
    }

    @Override
    @Transactional
    public int toggleLike(Long momentId, String action) {
        MomentPostEntity entity = momentPostMapper.selectOne(
                new LambdaQueryWrapper<MomentPostEntity>()
                        .eq(MomentPostEntity::getMomentId, momentId)
                        .last("limit 1")
        );
        if (entity == null) {
            throw new BusinessException(404, "moment not found");
        }
        int likeCount = entity.getLikeCount() == null ? 0 : entity.getLikeCount();
        if ("unlike".equalsIgnoreCase(action)) {
            likeCount = Math.max(0, likeCount - 1);
        } else {
            likeCount = likeCount + 1;
        }
        entity.setLikeCount(likeCount);
        entity.setUpdatedAt(LocalDateTime.now());
        momentPostMapper.updateById(entity);
        return likeCount;
    }

    @Override
    @Transactional
    public CommentAggregate addComment(Long userId, Long momentId, String content, Long replyCommentId) {
        MomentPostEntity entity = momentPostMapper.selectOne(
                new LambdaQueryWrapper<MomentPostEntity>()
                        .eq(MomentPostEntity::getMomentId, momentId)
                        .last("limit 1")
        );
        if (entity == null) {
            throw new BusinessException(404, "moment not found");
        }
        int commentCount = entity.getCommentCount() == null ? 0 : entity.getCommentCount();
        MomentCommentEntity replyComment = null;
        if (replyCommentId != null) {
            replyComment = momentCommentMapper.selectOne(
                    new LambdaQueryWrapper<MomentCommentEntity>()
                            .eq(MomentCommentEntity::getCommentId, replyCommentId)
                            .eq(MomentCommentEntity::getStatus, 0)
                            .last("limit 1")
            );
            if (replyComment == null) {
                throw new BusinessException(404, "reply comment not found");
            }
        }
        MomentCommentEntity comment = new MomentCommentEntity();
        comment.setCommentId(System.currentTimeMillis());
        comment.setMomentId(momentId);
        comment.setCommentUserId(userId);
        comment.setReplyCommentId(replyCommentId);
        comment.setReplyUserId(replyComment == null ? null : replyComment.getCommentUserId());
        comment.setContentText(content);
        comment.setStatus(0);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        momentCommentMapper.insert(comment);
        entity.setCommentCount(commentCount + 1);
        entity.setUpdatedAt(LocalDateTime.now());
        momentPostMapper.updateById(entity);
        return new CommentAggregate(comment, entity.getCommentCount());
    }

    @Override
    @Transactional
    public int deleteComment(Long userId, Long commentId) {
        MomentCommentEntity comment = momentCommentMapper.selectOne(
                new LambdaQueryWrapper<MomentCommentEntity>()
                        .eq(MomentCommentEntity::getCommentId, commentId)
                        .eq(MomentCommentEntity::getStatus, 0)
                        .last("limit 1")
        );
        if (comment == null) {
            throw new BusinessException(404, "comment not found");
        }
        if (!userId.equals(comment.getCommentUserId())) {
            throw new BusinessException(403, "no permission to delete comment");
        }
        MomentPostEntity entity = momentPostMapper.selectOne(
                new LambdaQueryWrapper<MomentPostEntity>()
                        .eq(MomentPostEntity::getMomentId, comment.getMomentId())
                        .last("limit 1")
        );
        if (entity == null) {
            throw new BusinessException(404, "moment not found");
        }
        comment.setStatus(1);
        comment.setUpdatedAt(LocalDateTime.now());
        momentCommentMapper.updateById(comment);

        int commentCount = entity.getCommentCount() == null ? 0 : entity.getCommentCount();
        entity.setCommentCount(Math.max(0, commentCount - 1));
        entity.setUpdatedAt(LocalDateTime.now());
        momentPostMapper.updateById(entity);
        return entity.getCommentCount();
    }
}
