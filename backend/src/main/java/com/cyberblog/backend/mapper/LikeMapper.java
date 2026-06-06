package com.cyberblog.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyberblog.backend.entity.Like;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LikeMapper extends BaseMapper<Like> {
}
