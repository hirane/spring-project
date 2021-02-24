package com.fileServer.service;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fileServer.entity.DbUsersDetails;
import com.fileServer.entity.Users;
import com.fileServer.mapper.LoginMapper;

@Service
public class DbUsersDetailsService implements UserDetailsService {

    @Autowired
    LoginMapper loginMapper;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userId)
            throws UsernameNotFoundException {
        //DBからユーザ情報を取得。
        Users users = Optional.ofNullable(loginMapper.findUsers(userId))
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));

        return new DbUsersDetails(users, getAuthorities(users));
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