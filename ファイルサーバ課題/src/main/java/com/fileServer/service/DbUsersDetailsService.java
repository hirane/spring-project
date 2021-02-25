package com.fileServer.service;

import java.util.Collection;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fileServer.controller.LoginController;
import com.fileServer.entity.DbUsersDetails;
import com.fileServer.entity.Users;
import com.fileServer.mapper.LoginMapper;

@Service
public class DbUsersDetailsService implements UserDetailsService {

	@Autowired
	LoginMapper loginMapper;
	@Autowired
	LoginController loginController;


	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
		//DBからユーザ情報を取得
		//取得できない場合はUsernameNotFoundExceptionにthrowされる
		HttpSession session = loginController.getSession();
		Users user = null;
		try {
			user = loginMapper.findUsers(userId);
		} catch(Exception e) {
			session.setAttribute("existsId", false);
			throw new UsernameNotFoundException("DB検索中のエラーです");
		}
		//DBに該当IDのユーザが存在しない場合
		if(user == null) {
			session.setAttribute("existsId", false);
			throw new UsernameNotFoundException("ユーザが見つかりません");
		}
		session.setAttribute("existsId", true);
		return new DbUsersDetails(user, getAuthorities(user));
//		*/
	}

	/**
	 * 認証が通った時にこのユーザに与える権限の範囲を設定する。
	 * @param users DBから取得したユーザ情報。
	 * @return 権限の範囲のリスト。
	 */
	private Collection<? extends GrantedAuthority> getAuthorities(Users users) {

		String authority = "AUTHORITY_" + users.getAuthority();

		//認可が通った時にこのユーザに与える権限の範囲を設定する。
		return AuthorityUtils.createAuthorityList(authority);
	}

}