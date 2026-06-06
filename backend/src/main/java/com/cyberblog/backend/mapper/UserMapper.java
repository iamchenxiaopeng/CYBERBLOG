package com.cyberblog.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyberblog.backend.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
