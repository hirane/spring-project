package com.fileServer.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.fileServer.entity.Users;

@Mapper
public interface LoginMapper {

    //USERテーブルからユーザ名とパスワードを取得。
    public Users findUsers(String name);
}