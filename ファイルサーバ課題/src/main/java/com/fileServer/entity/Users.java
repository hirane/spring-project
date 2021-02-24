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
//	@Email
	//@Emailは全角でも登録できてしまう
	@Pattern(regexp="^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$")
	@Size(max = 254)
	private String userId;

	//ユーザ名
	@NotBlank(message = "入力されていません")
	@Pattern(regexp="[0-9a-zA-Z]{0,32}", message = "半角英数字32文字以内で入力してください")
	private String userName;

	//パスワード
	@NotBlank(message = "入力されていません")
	@Pattern(regexp="[0-9a-zA-Z]{0,32}", message = "半角英数字32文字以内で入力してください")
	private String password;

	//確認用パスワード
	@NotBlank(message = "入力されていません")
	@Pattern(regexp="[0-9a-zA-Z]{0,32}", message = "半角英数字32文字以内で入力してください")
	private String conPassword;

	//ユーザ権限
	private int authority;

	//権限変更時変数
//	private int editAuth;

	/*
	 *
    private String role;

    public String getRole() {
    	return this.role;
    }

    public void setRole(int authority) {
    	if(this.getAuthority() == 0) {
    		this.role = "ROLE_MASTER";
    	} else if(this.getAuthority() == 1) {
    		this.role = "ROLE_WRITABLE";
    	} else {
    		this.role = "ROLE_READABLE";
    	}
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> authorityList = new ArrayList<>();
		authorityList.add(new SimpleGrantedAuthority("ROLE_" + this.authority));
		return authorityList;
	}
	 */



}
