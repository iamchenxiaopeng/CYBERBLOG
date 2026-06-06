package com.cyberblog.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyberblog.backend.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
    @Update("UPDATE articles SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(Long id);

    @Update("UPDATE articles SET like_count = like_count + #{delta} WHERE id = #{id}")
    void updateLikeCount(Long id, int delta);

    @Update("UPDATE articles SET comment_count = comment_count + #{delta} WHERE id = #{id}")
    void updateCommentCount(Long id, int delta);
}
