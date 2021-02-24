package com.fileServer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LoginController {

	/**
	 * ログイン画面 に遷移する。
	 */
	@RequestMapping("/login")
	public String showLoginForm(Model model) {

		//ログイン画面。
		return "login";
	}

	/**FileControllerで処理しているため削除
	 * メインページに遷移する。
	 * ログインが成功した場合、このメソッドが呼び出される。
	 *//*
		@RequestMapping("/")
		public String login(Model model) {

		 //メインページ。
		 return "fileView";
		}*/

	/**UserControllerで処理しているため削除
	 * 新規登録画面 に遷移する。
	 *//*
		@RequestMapping("/regist")
		public String showNewUserForm() {

		//新規登録画面
		return "regist";
		}*/

}