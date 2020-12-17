package com.fileServer.entity;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

/**
 * DBから取得したユーザー情報を格納するクラス
 */
public class DbUsersDetails extends org.springframework.security.core.userdetails.User {

    private final Users users;

    public DbUsersDetails(Users users,
            Collection<GrantedAuthority> authorities) {

        super(users.getUser_name(), users.getPassword(),
                true, true, true, true, authorities);

        this.users = users;
    }

    public Users getusers() {
        return users;
    }

}
