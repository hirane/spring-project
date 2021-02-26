package com.fileServer.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Users {

	//ユーザID
	@NotBlank
	//Email形式
	@Pattern(regexp="^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$")
	@Size(max = 254)
	private String userId;

	//ユーザ名
	@NotBlank
	@Pattern(regexp="[0-9a-zA-Z]{0,32}")
	private String userName;

	//パスワード
	@NotBlank
	@Pattern(regexp="[0-9a-zA-Z]{0,32}")
	private String password;

	//確認用パスワード
	@NotBlank
	@Pattern(regexp="[0-9a-zA-Z]{0,32}")
	private String conPassword;

	//ユーザ権限
	private int authority;

}
