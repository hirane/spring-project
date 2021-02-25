package com.fileServer.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fileServer.entity.Users;

@Controller
public class LoginController {

	//@PostConstructorで初期ユーザデータ作成した方がいいか？





	//DbUsersDetailsServiceクラスで使用するためのgetter/setter
	private HttpSession session;
	public HttpSession getSession() {
		return session;
	}
	public void setSession(HttpSession session) {
		this.session = session;
	}


	@ModelAttribute("users")
	public Users init() {
		return new Users();
	}


	/**
	 * ログイン画面 に遷移する。
	 * 初期遷移時
	 */
	@GetMapping("/login")
	public String showLoginForm(Model model) {
		//ログイン画面。
		return "login";
	}


	//フォーム送信時
	@PostMapping("/login")
	public String checkParam(Model model, @Validated Users users, BindingResult br, HttpServletRequest req) {
		if(br.hasFieldErrors("userId") || br.hasFieldErrors("password")) {
			//ユーザIDで検出されたバリデーションエラーがリストとして入る
			List<FieldError> errors = br.getFieldErrors("userId");
			//ユーザIDフィールドでエラーがなかった場合はビューに遷移
			if(errors == null || errors.size() == 0) {
				return "login";
			}

			//ユーザIDのバリデーション数を適応したい順番に配列に格納
			String[] validationKind = {"NotBlank", "Pattern", "Size"};
			//かけたいバリデーションの順にループでエラーがあったかを確認
			for(int i = 0; i < validationKind.length; i++) {
				//存在するエラーの分だけループする
				for(FieldError errorCode: errors) {
					if(validationKind[i].equals(errorCode.getCode())) {
						//"NotBlank"のエラーが含まれていた場合
						if(i == 0) {
							model.addAttribute("errIdMsg", "入力されていません");
							return "login";
						}
						//"Pattern"のエラーが含まれていた場合
						else if(i == 1) {
							model.addAttribute("errIdMsg", "Email形式で入力してください");
							return "login";
						}
						//"Size"のエラーが含まれていた場合
						else {
							model.addAttribute("errIdMsg", "半角英数字254文字以内で入力してください");
							return "login";
						}
					}
				}
			}
		}
		HttpSession session = req.getSession();
		session.setAttribute("userId", users.getUserId());
		setSession(session);
		model.addAttribute("notValid", true);
		return "login";
	}


	//セッションタイムアウト時
	@RequestMapping("/timeout")
	public String redirectLoginForm(RedirectAttributes redirectAttributes) {
		//現状、ログアウトしてもこのメッセージが出るため、要確認
		redirectAttributes.addFlashAttribute("isTimeout", true);
		redirectAttributes.addFlashAttribute("timeoutMsg", "セッションがタイムアウトしました。ログインし直してください。");
		//ログイン画面。
		return "redirect:/login";
	}


	//ユーザ名がDBに存在しない場合、もしくはパスワードが異なる場合
	@RequestMapping("/login-error")
	public String loginError(Users users, HttpSession session, RedirectAttributes redirectAttributes) {

		//ユーザIDを戻すためにセット
		users.setUserId(session.getAttribute("userId").toString());
		//セッション情報から削除
		session.removeAttribute("userId");
		//エラー内容の文字列
		String errKind = session.getAttribute("errReason").toString();
		//セッション情報から削除
		session.removeAttribute("errReason");
		//ユーザIDが存在する場合はパスワード欄下にメッセージ
		if(errKind.equals("パスワードが間違っています")) {
			redirectAttributes.addFlashAttribute("validPassword", true);
		}
		//ユーザIDが存在しない場合はユーザID欄下にメッセージ
		else if(errKind.equals("指定されたユーザは存在しません")) {
			redirectAttributes.addFlashAttribute("validUserId", true);
		}
		//そのほかのエラー
		else {
			redirectAttributes.addFlashAttribute("isErr", true);
		}
		redirectAttributes.addFlashAttribute("errMsg", errKind);
		redirectAttributes.addFlashAttribute(users);
		return "redirect:/login";
	}

}