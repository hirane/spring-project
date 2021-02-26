package com.fileServer.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.fileServer.entity.Users;

@Mapper
public interface UserMapper {

	/**
	 * Select処理
	 * ユーザリスト全件取得
	 * マスタユーザ(authority=0)を始めに取得し、以下はidの昇順にソート
	 */
//	@Select("SELECT user_id as userId, user_name as userName, password, authority FROM users ORDER BY authority=0 desc, user_id asc")
	public List<Users> findAll();

	/**
	 * Select処理
	 * ユーザIDをキにユーザ情報を取得
	 * @param userId
	 * @return
	 */
//	@Select("SELECT user_id as userId, user_name as userName, password, authority FROM users WHERE user_id=#{userId}")
	public Users findById(String userId);


	/**
	 * Insert処理
	 * 新規ユーザ登録
	 */
//	@Insert("INSERT INTO users ( user_id, user_name, password, authority ) "
//			+ " VALUES ( #{userId}, #{userName}, #{password}, #{authority})")
	public void insertUserInfo(Users users);

	/**
	 * Update処理
	 * ユーザ権限変更
	 * @param newAuthority
	 */
//	@Update("UPDATE users SET authority=#{newAuthority} WHERE user_id=#{updateId}")
	public void updateAuthority(String updateId, int newAuthority);

	/**
	 * Delete処理
	 * ユーザ削除
	 * @param deleteId
	 */
//	@Delete("DELETE FROM users WHERE user_id=#{deleteId}")
	public void delete(String deleteId);

	/**
	 * Update処理
	 * ユーザ名変更
	 * @param newName
	 * @param userId
	 */
//	@Update("UPDATE users SET user_name=#{newName} WHERE user_id=#{userId}")
	public void updateName(String newName, String userId);

	/**
	 * Update処理
	 * パスワード変更
	 * @param newPassword
	 * @param userId
	 */
//	@Update("UPDATE users SET password=#{newPassword} WHERE user_id=#{userId}")
	public void updatePassword(String newPassword, String userId);

	/**
	 * Select処理
	 * ユーザIDの重複チェック
	 * @param userId
	 * @return
	 */
//	@Select("SELECT COUNT(*) FROM users WHERE user_id=#{userId}")
	public int findUserId(String userId);

	/**
	 * Select処理
	 * ユーザ名の重複チェック
	 * @param userName
	 * @return
	 */
//	@Select("SELECT EXISTS (SELECT user_name FROM users WHERE user_name = #{userName});")
	public boolean findUserName(String userName);

	/**
	 * Update処理
	 * ユーザ名更新時にファイルのユーザ情報も更新
	 * @param
	 * @return
	 */
	public void updateFilesUser(String newUserName, String oldUserName);


}
