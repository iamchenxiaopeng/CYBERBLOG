package com.cyberblog.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cyberblog.backend.dto.ArticleDTO;
import com.cyberblog.backend.vo.ArticleVO;
import org.springframework.web.multipart.MultipartFile;

public interface ArticleService {
    IPage<ArticleVO> listArticles(int page, int size, String keyword, Long currentUserId);
    ArticleVO getArticle(Long id, Long currentUserId);
    ArticleVO createArticle(ArticleDTO dto, Long userId);
    ArticleVO uploadMarkdown(MultipartFile file, String title, String tags, String coverImg, Long userId);
    ArticleVO updateArticle(Long id, ArticleDTO dto, Long userId);
    void deleteArticle(Long id, Long userId);
}
