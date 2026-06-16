package com.cyberblog.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyberblog.backend.common.BusinessException;
import com.cyberblog.backend.dto.ArticleDTO;
import com.cyberblog.backend.entity.Article;
import com.cyberblog.backend.entity.Like;
import com.cyberblog.backend.entity.User;
import com.cyberblog.backend.mapper.ArticleMapper;
import com.cyberblog.backend.mapper.LikeMapper;
import com.cyberblog.backend.mapper.UserMapper;
import com.cyberblog.backend.service.ArticleService;
import com.cyberblog.backend.service.MinioService;
import com.cyberblog.backend.vo.ArticleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final LikeMapper likeMapper;
    private final MinioService minioService;

    @Override
    public IPage<ArticleVO> listArticles(int page, int size, String keyword, Long currentUserId, String guestId) {
        Page<Article> pageReq = new Page<>(page, size);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, "published")
                .orderByDesc(Article::getCreatedAt);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Article::getTitle, keyword).or().like(Article::getSummary, keyword));
        }
        IPage<Article> articlePage = articleMapper.selectPage(pageReq, wrapper);
        return articlePage.convert(a -> toVO(a, currentUserId, guestId, false));
    }

    @Override
    public ArticleVO getArticle(Long id, Long currentUserId, String guestId) {
        Article article = articleMapper.selectById(id);
        if (article == null) throw new BusinessException(404, "文章不存在");
        articleMapper.incrementViewCount(id);
        article.setViewCount(article.getViewCount() + 1);
        return toVO(article, currentUserId, guestId, true);
    }

    @Override
    public ArticleVO createArticle(ArticleDTO dto, Long userId) {
        Article article = new Article();
        article.setUserId(userId);
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setContentType(dto.getContentType());
        article.setCoverImg(dto.getCoverImg() == null ? "" : dto.getCoverImg());
        article.setSummary(buildSummary(dto.getContent()));
        article.setTags(dto.getTags() == null ? "" : dto.getTags());
        article.setStatus(dto.getStatus());
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0);
        articleMapper.insert(article);
        return toVO(article, userId, null, false);
    }

    @Override
    public ArticleVO uploadMarkdown(MultipartFile file, String title, String tags, String coverImg, Long userId) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            String fileUrl = minioService.upload(file, "articles/");
            Article article = new Article();
            article.setUserId(userId);
            article.setTitle(StringUtils.hasText(title) ? title : file.getOriginalFilename().replace(".md", ""));
            article.setContent(content);
            article.setContentType("markdown");
            article.setFileUrl(fileUrl);
            article.setCoverImg(coverImg == null ? "" : coverImg);
            article.setSummary(buildSummary(content));
            article.setTags(tags == null ? "" : tags);
            article.setStatus("published");
            article.setViewCount(0);
            article.setLikeCount(0);
            article.setCommentCount(0);
            articleMapper.insert(article);
            return toVO(article, userId, null, false);
        } catch (IOException e) {
            throw new BusinessException("文件读取失败: " + e.getMessage());
        }
    }

    @Override
    public ArticleVO updateArticle(Long id, ArticleDTO dto, Long userId) {
        Article article = articleMapper.selectById(id);
        if (article == null) throw new BusinessException(404, "文章不存在");
        if (!article.getUserId().equals(userId)) throw new BusinessException(403, "无权限修改");
        if (StringUtils.hasText(dto.getTitle())) article.setTitle(dto.getTitle());
        if (dto.getContent() != null) {
            article.setContent(dto.getContent());
            article.setSummary(buildSummary(dto.getContent()));
        }
        if (dto.getTags() != null) article.setTags(dto.getTags());
        if (dto.getStatus() != null) article.setStatus(dto.getStatus());
        if (dto.getCoverImg() != null) article.setCoverImg(dto.getCoverImg());
        articleMapper.updateById(article);
        return toVO(article, userId, null, false);
    }

    @Override
    public void deleteArticle(Long id, Long userId) {
        Article article = articleMapper.selectById(id);
        if (article == null) throw new BusinessException(404, "文章不存在");
        // 查询当前用户，判断是否管理员（username 为 admin）
        User currentUser = userMapper.selectById(userId);
        boolean isAdmin = currentUser != null && "admin".equals(currentUser.getUsername());
        // 只有文章作者或管理员可以删除
        if (!article.getUserId().equals(userId) && !isAdmin) {
            throw new BusinessException(403, "无权限删除");
        }
        articleMapper.deleteById(id);
    }

    private String buildSummary(String content) {
        if (content == null) return "";
        String plain = content.replaceAll("#+\\s", "").replaceAll("\\*+", "")
                .replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", "$1")
                .replaceAll("`[^`]+`", "").replaceAll("\\n+", " ").trim();
        return plain.length() > 200 ? plain.substring(0, 200) + "..." : plain;
    }

    private ArticleVO toVO(Article article, Long currentUserId, String guestId, boolean withContent) {
        ArticleVO vo = new ArticleVO();
        vo.setId(article.getId());
        vo.setUserId(article.getUserId());
        vo.setTitle(article.getTitle());
        if (withContent) vo.setContent(article.getContent());
        vo.setContentType(article.getContentType());
        vo.setFileUrl(article.getFileUrl());
        vo.setCoverImg(article.getCoverImg());
        vo.setSummary(article.getSummary());
        vo.setTags(article.getTags());
        vo.setStatus(article.getStatus());
        vo.setViewCount(article.getViewCount());
        vo.setLikeCount(article.getLikeCount());
        vo.setCommentCount(article.getCommentCount());
        vo.setCreatedAt(article.getCreatedAt());
        vo.setUpdatedAt(article.getUpdatedAt());
        // fill author info
        User user = userMapper.selectById(article.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setAvatarUrl(user.getAvatarUrl());
        }
        // check liked: 已登录按 userId 查，未登录按 guestId 查
        if (currentUserId != null) {
            Long likeCount = likeMapper.selectCount(new LambdaQueryWrapper<Like>()
                    .eq(Like::getUserId, currentUserId)
                    .eq(Like::getTargetId, article.getId())
                    .eq(Like::getTargetType, "article"));
            vo.setLiked(likeCount > 0);
        } else if (guestId != null) {
            Long likeCount = likeMapper.selectCount(new LambdaQueryWrapper<Like>()
                    .eq(Like::getGuestId, guestId)
                    .eq(Like::getTargetId, article.getId())
                    .eq(Like::getTargetType, "article"));
            vo.setLiked(likeCount > 0);
        } else {
            vo.setLiked(false);
        }
        return vo;
    }
}
