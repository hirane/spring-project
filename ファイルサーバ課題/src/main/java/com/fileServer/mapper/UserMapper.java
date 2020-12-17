package com.fileServer.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.fileServer.entity.Users;

@Mapper
public interface UserMapper {

	/**
	 * ユーザリスト全件取得
	 * マスタユーザ(authority=0)を始めに取得し、以下はidの昇順にソート
	 */
	@Select("SELECT user_id as userId, user_name as userName, password, authority FROM users ORDER BY authority=0 desc, user_id asc")
	public List<Users> findAll();

	/**
	 * 新規ユーザ登録
	 */
	@Insert("INSERT INTO users ( user_id, user_name, password, authority ) "
			+ " VALUES ( #{userId}, #{userName}, #{password}, #{authority})")
	public void insertUserInfo(Users users);

	/**
	 * ユーザ権限変更
	 * @param newAuthority
	 */
	@Update("UPDATE users SET authority=#{newAuthority} WHERE user_id=#{updateId}")
	public void updateAuthority(String updateId, int newAuthority);

	/**
	 * ユーザ削除
	 * @param deleteId
	 */
	@Delete("DELETE FROM users WHERE user_id=#{deleteId}")
	public void delete(String deleteId);

	/**
	 * ユーザ名変更
	 * @param newName
	 * @param userId
	 */
	@Update("UPDATE users SET user_name=#{newName} WHERE user_id=#{userId}")
	public void updateName(String newName, String userId);

	/**
	 * パスワード変更
	 * @param newPassword
	 * @param userId
	 */
	@Update("UPDATE users SET password=#{newPassword} WHERE user_id=#{userId}")
	public void updatePassword(String newPassword, String userId);

	/**
	 * ユーザIDの重複チェック
	 * @param userId
	 * @return
	 */
	@Select("SELECT COUNT(*) FROM users WHERE user_id=#{userId}")
	public int findUserId(String userId);

	/**
	 * ユーザ名の重複チェック
	 * @param userId
	 * @return
	 */
	@Select("SELECT COUNT(*) FROM users WHERE user_name=#{userName}")
	public int findUserName(String userName);

}
