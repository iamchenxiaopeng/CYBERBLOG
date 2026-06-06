package com.cyberblog.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyberblog.backend.common.BusinessException;
import com.cyberblog.backend.dto.CommentDTO;
import com.cyberblog.backend.entity.Comment;
import com.cyberblog.backend.entity.Like;
import com.cyberblog.backend.entity.User;
import com.cyberblog.backend.mapper.ArticleMapper;
import com.cyberblog.backend.mapper.CommentMapper;
import com.cyberblog.backend.mapper.LikeMapper;
import com.cyberblog.backend.mapper.UserMapper;
import com.cyberblog.backend.service.CommentService;
import com.cyberblog.backend.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;
    private final LikeMapper likeMapper;

    @Override
    public List<CommentVO> listByArticle(Long articleId, Long currentUserId) {
        List<Comment> all = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getArticleId, articleId)
                        .orderByAsc(Comment::getCreatedAt));
        // group: parentId=0 is root, others are replies
        Map<Long, List<CommentVO>> replyMap = all.stream()
                .filter(c -> c.getParentId() != null && c.getParentId() > 0)
                .map(c -> toVO(c, currentUserId))
                .collect(Collectors.groupingBy(CommentVO::getParentId));
        List<CommentVO> roots = all.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .map(c -> {
                    CommentVO vo = toVO(c, currentUserId);
                    vo.setReplies(replyMap.getOrDefault(c.getId(), new ArrayList<>()));
                    return vo;
                })
                .collect(Collectors.toList());
        return roots;
    }

    @Override
    public CommentVO addComment(Long articleId, CommentDTO dto, Long userId) {
        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setUserId(userId);
        comment.setParentId(dto.getParentId() == null ? 0L : dto.getParentId());
        comment.setContent(dto.getContent());
        comment.setLikeCount(0);
        commentMapper.insert(comment);
        articleMapper.updateCommentCount(articleId, 1);
        return toVO(comment, userId);
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) throw new BusinessException(404, "评论不存在");
        if (!comment.getUserId().equals(userId)) throw new BusinessException(403, "无权删除");
        commentMapper.deleteById(commentId);
        articleMapper.updateCommentCount(comment.getArticleId(), -1);
    }

    private CommentVO toVO(Comment comment, Long currentUserId) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setArticleId(comment.getArticleId());
        vo.setUserId(comment.getUserId());
        vo.setParentId(comment.getParentId());
        vo.setContent(comment.getContent());
        vo.setLikeCount(comment.getLikeCount());
        vo.setCreatedAt(comment.getCreatedAt());
        vo.setReplies(new ArrayList<>());
        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setAvatarUrl(user.getAvatarUrl());
        }
        if (currentUserId != null) {
            Long count = likeMapper.selectCount(new LambdaQueryWrapper<Like>()
                    .eq(Like::getUserId, currentUserId)
                    .eq(Like::getTargetId, comment.getId())
                    .eq(Like::getTargetType, "comment"));
            vo.setLiked(count > 0);
        } else {
            vo.setLiked(false);
        }
        return vo;
    }
}
