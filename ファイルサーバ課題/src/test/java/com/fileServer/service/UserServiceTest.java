package com.fileServer.service;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fileServer.entity.UserForm;
import com.fileServer.entity.Users;

@SpringBootTest
public class UserServiceTest {

	@Autowired
	private UserService us;

	//1-006
	@Test
	public void 正常系_insertUserInfo_DBテーブルに追加_1レコード追加される() {
		//引数のuserFormインスタンスに値を設定
		UserForm userForm = new UserForm();
		userForm.setUserId("test@new");
		userForm.setUserName("new");
		userForm.setPassword("new");

		//テスト対象の処理実行
		us.insertUserInfo(userForm);
	}

	//1－007
	@Test
	public void 正常系_isDuplicatedUserId_ユーザID重複なし_false() {
		//テスト対象の処理実行
		boolean result = us.isDuplicatedUserId("a@aa");

		//検証
		assertThat(result, is(false));
	}

	//1－008
	@Test
	public void 異常系_isDuplicatedUserId_ユーザID重複あり_true() {
		//テスト対象の処理実行
		boolean result = us.isDuplicatedUserId("a@a");

		//検証
		assertThat(result, is(true));
	}

	//1－009
	@Test
	public void 正常系_isDuplicatedUserName_ユーザ名重複なし_false() {
		//テスト対象の処理実行
		boolean result = us.isDuplicatedUserName("aab");

		//検証
		assertThat(result, is(false));
	}

	//1－010
	@Test
	public void 異常系_isDuplicatedUserName_ユーザ名重複あり_true() {
		//テスト対象の処理実行
		boolean result = us.isDuplicatedUserName("aaa");

		//検証
		assertThat(result, is(true));
	}

	//2-008
	@Test
	public void 正常系_updateName_ユーザ名を更新_DBテーブル更新() {
		//引数のユーザID,ユーザ名を設定
		String userId = "test@test";
		String newName = "rename";

		//テスト対象の処理実行
		us.updateName(newName, userId);
	}

	//2-009
	@Test
	public void 正常系_updatePassword_パスワードを更新_DBテーブル更新() {
		//引数のユーザID,パスワードを設定
		String userId = "test@test";
		String newPassword = "testtest";

		//テスト対象の処理実行
		us.updatePassword(newPassword, userId);
	}

	//2-010
	@Test
	public void 正常系_findById_条件なし_usersテーブルのユーザIDに紐づくレコードを取得() {
		//引数のuserIdを設定
		String userId = "test@test";

		//テスト対象の処理実行
		Users users = us.findById(userId);

		//検証
		System.out.println("ユーザID：" + users.getUserId());
		System.out.println("ユーザ名：" + users.getUserName());
		System.out.println("パスワード：" + users.getPassword());
		System.out.println("権限：" + users.getAuthority());
	}

	//3-002
	@Test
	public void 正常系_findAll_条件なし_usersテーブル全件リスト() {
		List<Users> list = new ArrayList<Users>();
		//テスト対象の処理実行
		list = us.findAll();

		//検証
		for (Users testList : list) {
			testList.getUserId();
			System.out.println("ユーザID：" + testList.getUserId());
			System.out.println("ユーザ名：" + testList.getUserName());
			System.out.println("パスワード：" + testList.getPassword());
			System.out.println("権限：" + testList.getAuthority());
		}
	}

	//3-004
	@Test
	public void 正常系_updateAuthority_条件なし_usersテーブルのIDに紐づく権限を変更() {
		//引数のユーザIDを設定
		String updateId = "test@test";
		int newAuthority = 1;

		//テスト対象の処理実行
		us.updateAuthority(updateId,newAuthority);
	}

	//3-006
	@Test
	public void 正常系_delete_条件なし_usersテーブルのIDに紐づくレコードを削除() {
		//引数のユーザIDを設定
		String deleteId = "test@delete";

		//テスト対象の処理実行
		us.delete(deleteId);
	}
}
