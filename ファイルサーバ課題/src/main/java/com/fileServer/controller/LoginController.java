package com.fileServer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

	@RequestMapping("/timeout")
	public String redirectLoginForm(Model model, RedirectAttributes redirectAttributes) {
		//現状、ログアウトしてもこのメッセージが出るため、要確認
		redirectAttributes.addFlashAttribute("isTimeout", true);
		redirectAttributes.addFlashAttribute("timeoutMsg", "セッションがタイムアウトしました。ログインし直してください。");
		//ログイン画面。
		return "redirect:/login";
	}


	@RequestMapping("/login-error")
	public String loginError(Model model, RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("isErr", true);
		redirectAttributes.addFlashAttribute("errMsg", "ログインに失敗しました");
		return "redirect:/login";
	}

/*
	@RequestMapping("/error")
	public String error(RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("isErr", true);
		redirectAttributes.addFlashAttribute("errMsg", "エラーが発生しましたのでログインしなおしてください");
		return "redirect:/logut";
	}
*/



	/*
	@RequestMapping("/loginProcess")
	public String loginProcess(@ModelAttribute("loginForm") Users user, BindingResult br, Model model, RedirectAttributes redirectAttributes) {
		if(br.hasErrors()) {
			return "login";
		}



		redirectAttributes.addFlashAttribute("isErr", true);
		redirectAttributes.addFlashAttribute("errMsg", "入力ミス");
		return "redirect:/login";
	}
	*/



	//@PostConstructorで初期ユーザデータ作成した方がいいか？


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