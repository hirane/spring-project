package com.fileServer.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fileServer.validation.MyAuthenticationFailureHandler;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{

	@Autowired
	UserDetailsService userDetailsService;

	//cssとjsとimg以下のフォルダはセキュリティ設定を無視する(ログインにかかわらず使用可能)
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/css/**", "/js/**", "/img/**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests()
		//各リクエストのアクセス権限
		//ログイン画面は全ユーザがアクセス許可
		.antMatchers("/login*").permitAll()
		//新規登録画面遷移
		.antMatchers("/regist*").permitAll()
		//ユーザ管理画面遷移・ユーザ権限変更・ユーザ削除
		.antMatchers("/userList", "/editAuth", "/delete").hasAuthority("AUTHORITY_0")
		//ユーザ情報更新画面遷移・ユーザ名更新・ユーザパスワード更新
		.antMatchers("/updateUser", "/updateName", "/updatePassword").hasAnyAuthority("AUTHORITY_0", "AUTHORITY_1", "AUTHORITY_2")
		//ファイル一覧画面遷移・ダウンロード
		.antMatchers("/fileView/top", "/fileView", "/download", "/downloadMultiFiles", "/downloadDir").hasAnyAuthority("AUTHORITY_0", "AUTHORITY_1", "AUTHORITY_2")
		//アップロード・リネーム・移動・フォルダ作成・削除
		.antMatchers("/fileView/filesize-error", "/fileView/maxsize-error", "/upload", "/changeName", "/moveDirectory", "/makeDirectory", "/deleteFile").hasAnyAuthority("AUTHORITY_0", "AUTHORITY_1")
		//ログイン画面へリダイレクト
//		.antMatchers("/timeout").permitAll()
		//すべてのリクエストは認証されたユーザのみ承認
		.anyRequest().authenticated()
		//セッションタイムアウト時にログイン画面へリダイレクト
		.and()
		.sessionManagement().invalidSessionUrl("/timeout");

		http.formLogin()
		//ログインページを指定
		.loginPage("/login")
		//認証処理に移るためのURL(ログインフォームのaction先と同じに設定)
		.loginProcessingUrl("/authenticate")
		//ログイン失敗時の遷移先
//		.failureUrl("/login-error")
		.failureHandler(new MyAuthenticationFailureHandler("/login-error"))
		//ログインフォームのユーザ名入力欄name
		.usernameParameter("userId")
		//ログインフォームのパスワード入力欄name
		.passwordParameter("password")
		//ログイン成功時の遷移先
		.defaultSuccessUrl("/fileView/top", true)
		//ログインページのアクセスは全員許可する
		.permitAll();

		http.logout()
		//セッションの値を削除する
		.invalidateHttpSession(true)
		//cookie削除
		.deleteCookies("JSESSIONID")
		//ログアウト時の遷移先
		.logoutSuccessUrl("/login");
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