package com.fileServer.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{

	@Autowired
    UserDetailsService userDetailsService;
	
	@Override
	protected void configure(HttpSecurity http)throws Exception{
		http.formLogin()
			//ログインページを指定
			.loginPage("/login")
			//認証処理に移るためのURL(ログインフォームのaction先と同じに設定)
			.loginProcessingUrl("/authenticate")
			//ログインフォームのユーザ名入力欄name
		    .usernameParameter("userName")
		    //ログインフォームのパスワード入力欄name
		    .passwordParameter("password")
		    //ログイン成功時の遷移先
		    .defaultSuccessUrl("/")
		    //ログインページのアクセスは全員許可する
		    .permitAll();

		http.csrf().disable().authorizeRequests()
			//各リクエストのアクセス権限　※全リクエスト全権限に許可
			//新規登録画面遷移
		 	.antMatchers("/regist").permitAll()
		 	//ユーザ管理画面遷移
		 	.antMatchers("/userList").permitAll()
		 	//ユーザ権限変更
		 	.antMatchers("/editAuth").permitAll()
		 	//ユーザ削除
		 	.antMatchers("/delete").permitAll()
		 	//ユーザ情報更新画面遷移
		 	.antMatchers("/updateUser").permitAll()
		 	//ユーザ名更新
		 	.antMatchers("/updateName").permitAll()
		 	//ユーザパスワード更新
		 	.antMatchers("/updatePassword").permitAll()
		 	//ファイル一覧画面遷移
		 	.antMatchers("/fileView").permitAll()
		 	//すべてのリクエストは認証されたユーザのみ承認
		 	.anyRequest().authenticated();
	}

		@Bean
		PasswordEncoder passwordEncoder(){
			return new BCryptPasswordEncoder();
		}
		
		@Autowired
    	void configureAuthenticationManager(AuthenticationManagerBuilder auth) throws Exception {
        	auth.userDetailsService(userDetailsService)
            	.passwordEncoder(passwordEncoder());
    }
}
