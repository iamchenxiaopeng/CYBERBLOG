package com.cyberblog.backend.service;

import com.cyberblog.backend.dto.CommentDTO;
import com.cyberblog.backend.vo.CommentVO;

import java.util.List;

public interface CommentService {
    List<CommentVO> listByArticle(Long articleId, Long currentUserId);
    CommentVO addComment(Long articleId, CommentDTO dto, Long userId);
    void deleteComment(Long commentId, Long userId);
}
