package com.fileServer.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Users {

	//ユーザID
    private String userId;
    //ユーザ名
    private String userName;
    //パスワード
    private String password;
    //ユーザ権限
    private int authority;
}
