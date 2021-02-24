package com.fileServer.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fileServer.entity.Users;
import com.fileServer.mapper.UserMapper;

@Service
@Transactional
public class UserService {
	@Autowired
	UserMapper userMapper;

	@Autowired
	PasswordEncoder passwordEncoder;

	/**
	* 登録済みユーザリストをDBから全件取得
	* @return
	*/
	public List<Users> findAll() {
		List<Users> userList = new ArrayList<Users>();
		userList = userMapper.findAll();
		return userList;
	}

	/**
	 * 新規ユーザ登録情報をDBに保存
	 * @param user
	 */
	public void insertUserInfo(Users userForm) {
		//パスワードをハッシュ化
		String hashedPassword = passwordEncoder.encode(userForm.getPassword());
		//DB登録用のUserインスタンス作成
		Users users = new Users();
		users.setUserId(userForm.getUserId());
		users.setUserName(userForm.getUserName());
		users.setPassword(hashedPassword);
		//ユーザ作成時初期権限は'閲覧者(2)'
		users.setAuthority(2);
		//ユーザ登録情報をDBに保存
		userMapper.insertUserInfo(users);
	}

	/**
	 * ユーザIDをキーにユーザ情報を取得
	 * @param userId
	 * @return
	 */
	public Users findById(String userId){
		return userMapper.findById(userId);
	}

	/**
	 * DBユーザ権限変更
	 * @param newAuthority
	 */
	public void updateAuthority(String updateId, int newAuthority) {
		userMapper.updateAuthority(updateId, newAuthority);
	}

	/**
	 * DBユーザ削除
	 * @param deleteId
	 */
	public void delete(String deleteId) {
		userMapper.delete(deleteId);
	}

	/**
	 * DBユーザ名変更
	 * @param newName
	 * @param userId
	 */
	public void updateName(String newName, String userId) {
		userMapper.updateName(newName, userId);
	}

	/**
	 * DBパスワード変更
	 * @param newPassword
	 * @param userId
	 */
	public void updatePassword(String newPassword, String userId) {
		//パスワードをハッシュ化
		String newHashedPassword = passwordEncoder.encode(newPassword);
		userMapper.updatePassword(newHashedPassword, userId);
	}

	/**
	 * ユーザIDの重複チェック
	 * @param userId
	 * @return
	 */
	public boolean isDuplicatedUserId(String userId) {
		//入力ユーザIDに一致する数を返す
		int result = userMapper.findUserId(userId);
		//0のとき重複なしでfalseを返す
		if (result == 0) {
			return false;
		}
		//以外0のとき重複ありでtrueを返す
		return true;
	}

	/**
	 * ユーザ名の重複チェック
	 * @param userId
	 * @return
	 */
	/*
	public boolean isDuplicatedUserName(String userName) {
		//入力ユーザ名に一致する数を返す
		int result = userMapper.findUserName(userName);
		//0のとき重複なしでfalseを返す
		if (result == 0) {
			return false;
		}
		//以外0のとき重複ありでtrueを返す
		return true;
	}
	*/

}
