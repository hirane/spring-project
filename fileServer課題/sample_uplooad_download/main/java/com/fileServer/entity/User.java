package com.fileServer.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {

	//ユーザID
	private String user_id;

	//ユーザ名
	private String user_name;

	//パスワード
	private String password;

	//権限
	private String role;

}
