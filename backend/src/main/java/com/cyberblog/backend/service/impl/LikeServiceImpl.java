package com.cyberblog.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyberblog.backend.entity.Like;
import com.cyberblog.backend.mapper.ArticleMapper;
import com.cyberblog.backend.mapper.CommentMapper;
import com.cyberblog.backend.mapper.LikeMapper;
import com.cyberblog.backend.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeMapper likeMapper;
    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public boolean toggleLike(Long userId, String guestId, Long targetId, String targetType) {
        // 构建查询条件：同一用户（登录用 userId，未登录用 guestId）对同一目标的点赞记录
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<Like>()
                .eq(Like::getTargetId, targetId)
                .eq(Like::getTargetType, targetType);

        if (userId != null) {
            wrapper.eq(Like::getUserId, userId);
        } else {
            wrapper.eq(Like::getGuestId, guestId);
        }

        Like existing = likeMapper.selectOne(wrapper);

        if (existing != null) {
            // 已点赞 → 取消点赞
            likeMapper.deleteById(existing.getId());
            updateCount(targetId, targetType, -1);
            return false;
        } else {
            // 未点赞 → 点赞
            Like like = new Like();
            like.setUserId(userId);
            like.setGuestId(guestId);
            like.setTargetId(targetId);
            like.setTargetType(targetType);
            likeMapper.insert(like);
            updateCount(targetId, targetType, 1);
            return true;
        }
    }

    private void updateCount(Long targetId, String targetType, int delta) {
        if ("article".equals(targetType)) {
            articleMapper.updateLikeCount(targetId, delta);
        } else if ("comment".equals(targetType)) {
            var comment = commentMapper.selectById(targetId);
            if (comment != null) {
                comment.setLikeCount(Math.max(0, comment.getLikeCount() + delta));
                commentMapper.updateById(comment);
            }
        }
    }
}
