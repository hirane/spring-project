package com.fileServer.entity;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * DBから取得したユーザー情報を格納するクラス
 */
public class DbUsersDetails extends User {

	private final Users users;

	public DbUsersDetails(Users users, Collection<? extends GrantedAuthority> authorities) {
		super(users.getUserId(), users.getPassword(), true, true, true, true, authorities);
		this.users = users;
	}

	public Users getUsers() {
		return users;
	}

}
