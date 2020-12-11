package com.fileServer.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.fileServer.entity.User;

@Mapper
public interface UserMapper {

	    @Select("SELECT * FROM user_info")
	    public List<User> findAll();
}
