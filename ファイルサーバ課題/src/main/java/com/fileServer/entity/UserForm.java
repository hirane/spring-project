package com.fileServer.entity;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserForm {
	/**
	 * ユーザ登録のフォーム取得用
	 */

	/*public interface Group01{}
	public interface Group02{}
	//指定したグループ順にバリデーションを実行しエラーが発生したら次の処理は実行しない
	@GroupSequence({Group01.class,Group02.class})
	public interface All{}
	*/

	//ユーザID
	/*@NotEmpty(groups={Group01.class})
	@Email(groups={Group02.class})*/
	@NotEmpty
	@Email
	private String userId;

	//ユーザ名
	/*@NotEmpty(groups={Group01.class})
	@Pattern(regexp="[0-9a-zA-Z]{1,32}", groups={Group02.class})*/
	@NotEmpty
	@Pattern(regexp="[0-9a-zA-Z]{1,32}")
	private String userName;

	//パスワード
	/*	@NotEmpty(groups={Group01.class})
		@Pattern(regexp="[0-9a-zA-Z]{1,32}", groups={Group02.class})*/
	@NotEmpty
	@Pattern(regexp="[0-9a-zA-Z]{1,32}")
	private String password;

	//確認用パスワード
	/*	@NotEmpty(groups={Group01.class})
		@Pattern(regexp="[0-9a-zA-Z]{1,32}", groups={Group02.class})*/
	@NotEmpty
	@Pattern(regexp="[0-9a-zA-Z]{1,32}")
	private String conPassword;
}