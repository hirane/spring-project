package com.fileServer.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fileServer.config.Const;
import com.fileServer.entity.DbUsersDetails;
import com.fileServer.entity.Users;
import com.fileServer.service.UserService;

//@Slf4j
@Controller
public class UserController {

	@Autowired
	UserService userService;

	//ーーーーーーーーーーーーーユーザ管理画面ーーーーーーーーーーーーーーーーー

	/**
	 * 「/userList」アクセス時：DBに登録済みのユーザリストを取得し、ユーザ管理画面に渡す
	 * @param mav
	 * @return mav
	 */
	@RequestMapping("/userList")
	public String showUserList(@ModelAttribute("userForm") Users userForm, Model model, @AuthenticationPrincipal DbUsersDetails loginUser){
		//ユーザ情報を詰める
		List<Users> userlist = userService.findAll();
		model.addAttribute("userlist", userlist);
		model.addAttribute("userAuth", loginUser.getUsers().getAuthority());
		return Const.USERLIST;
	}

	/**
	 * 「/editAuth」アクセス時：ユーザ権限更新処理
	 * @param newAuthority
	 * @return userListページ
	 */
	@PostMapping("/editAuth")
	public String editAuthority(@ModelAttribute("userForm") Users userForm, @AuthenticationPrincipal DbUsersDetails loginUser, Model model) {
		//select要素で1か2以外が送信された場合、更新されない
		if(userForm.getAuthority() != Const.WRITABLE_AUTH && userForm.getAuthority() != Const.READABLE_AUTH) {
//			log.warn("ユーザ権限の更新で異常値が入力されました。");
			model.addAttribute("errMsg", "更新に失敗しました");
			model.addAttribute("authTarget", userForm.getUserId());
			return showUserList(userForm, model, loginUser);
		}

		userService.updateAuthority(userForm.getUserId(), userForm.getAuthority());
		return Const.REDIRECT_USERLIST;
	}

	/**
	 * 「/delete」アクセス時：ユーザ削除処理
	 * @param deleteId
	 * @param mav
	 * @return userListページ
	 */
	@PostMapping("/delete")
	public String deleteUser(@ModelAttribute("userForm") Users userForm, @AuthenticationPrincipal DbUsersDetails loginUser, Model model) {
		Users user = userService.findById(userForm.getUserId());
		//マスタ権限のユーザが削除対象になった場合、削除されない
		if(user.getAuthority() == 0) {
//			log.warn("削除不可のユーザを削除しようとしました。");
			model.addAttribute("errMsg", "削除不可のユーザです");
			model.addAttribute("deleteTarget", userForm.getUserId());
			return showUserList(userForm, model, loginUser);
		}

		userService.delete(userForm.getUserId());
//		model.addAttribute("successMsg", "削除しました");
		return Const.REDIRECT_USERLIST;
	}

	//ーーーーーーーーーーーーーユーザ登録画面ーーーーーーーーーーーーーーーーー
	/**
	*「/regist」GETアクセス時：登録画面に遷移
	* @return mav
	*/
	@RequestMapping("/registView")
	public String moveToRegistForm(Model model) {
		model.addAttribute("userForm", new Users());
		return Const.REGIST;
	}

	/**
	 *「/regist」POSTアクセス時：フォームの入力値チェック後にDB登録処理
	 * @param formUser フォームからの入力情報を取り込んだUserインスタンス
	 * @param conPassword 確認用パスワード
	 * @param mav
	 * @return mav
	 */
	@RequestMapping("/regist")
	public String userRegist(@ModelAttribute("userForm") @Validated Users userForm, BindingResult br, Model model, RedirectAttributes redirectAttributes) {
		//バリデーションチェック
		//入力制限の違反する場合
		if (br.hasErrors()) {
			//ユーザIDで検出されたバリデーションエラーがリストとして入る
			List<FieldError> errors = br.getFieldErrors("userId");
			//ユーザIDフィールドでエラーがなかった場合はビューに遷移
			if(errors.size() == 0) {
				return Const.REGIST;
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
							model.addAttribute("errIdMsg", "入力されていません。");
						}
						//"Pattern"のエラーが含まれていた場合
						else if(i == 1) {
							model.addAttribute("errIdMsg", "Email形式で入力してください。");
						}
						//"Size"のエラーが含まれていた場合
						else {
							model.addAttribute("errIdMsg", "半角英数字254文字以内で入力してください。");
						}
						return Const.REGIST;
					}
				}
			}
		}

		//既存ユーザとの重複もしくはパスワードの不一致でエラーがあるか判断する変数
		boolean hasErr = false;
		//既存ユーザとの重複チェック
		//ユーザIDが既に存在する場合
		if (userService.isDuplicatedUserId(userForm.getUserId())) {
			model.addAttribute("isUsedIdErr", true);
			model.addAttribute("errIdMsg", "このユーザIDは既に使用されています。");
//			model.addAttribute("isRegistered", false);
			hasErr = true;
		}
		//ユーザ名が既に存在する場合
		if (userService.findUserName(userForm.getUserName())) {
			model.addAttribute("isUsedNameErr", true);
			model.addAttribute("errNameMsg", "このユーザ名は既に使用されています。");
			hasErr = true;
		}
		//パスワードと確認パスワードの入力一致チェック
		//一致しない場合
		if (!(userForm.getPassword().equals(userForm.getConPassword()))) {
			model.addAttribute("isEqualPwErr", true);
			model.addAttribute("errPwMsg", "パスワードが一致しません。");
			hasErr = true;
		}
		//既存ユーザとの重複もしくはパスワードの不一致があった場合はまとめて返す
		if(hasErr) {
			return Const.REGIST;
		}

		//エラーがない場合は登録
		//DBにユーザ情報を登録
		userService.insertUserInfo(userForm);
		//登録完了メッセージ出力用(表示)
		redirectAttributes.addFlashAttribute("isRegistered", true);
		return Const.REDIRECT_REGIST;
	}

	//ーーーーーーーーーーーーーユーザ情報更新画面ーーーーーーーーーーーーーーーーー

	/**
	 * 「/updateUser」アクセス時：ユーザ情報更新画面に遷移
	 * @param loginUser
	 * @param mav
	 * @return
	 */
	@RequestMapping("/updateUser")
	//セッションからログインユーザ情報を取得しUserインスタンスをビューに返す
	public String showUserUpdate(@AuthenticationPrincipal DbUsersDetails loginUser, Model model) {
		//セッションのユーザIDをキーにログインユーザ情報を取得

		Users loginUserInfo = userService.findById(loginUser.getUsers().getUserId());
		model.addAttribute("userForm", loginUserInfo);
		model.addAttribute("userAuth", loginUser.getUsers().getAuthority());
		return Const.ACCOUNT;
	}

	/**
	 * 「/updateName」アクセス時：ユーザ名変更
	 * @param newName
	 * @param session
	 * @param mav
	 * @return
	 */
	@PostMapping("/updateName")
	public String updateUserName(@ModelAttribute("userForm") @Validated Users userForm, BindingResult br,
			@AuthenticationPrincipal DbUsersDetails loginUser, Model model, RedirectAttributes redirectAttributes) {
		userForm.setUserId(loginUser.getUsers().getUserId());

		if(br.hasFieldErrors("userName")) {
			userForm.setUserName(loginUser.getUsers().getUserName());
			model.addAttribute("userAuth", loginUser.getUsers().getAuthority());
			model.addAttribute("isDispNameErr", true);

			return Const.ACCOUNT;
		}

		//登録済みユーザ名と入力値が異なるかチェック
		//変更がない場合
		if(userForm.getUserName().equals(loginUser.getUsers().getUserName())) {
			model.addAttribute("isChangeNameErr", true);
			model.addAttribute("errMsg", "ユーザ名に変更がありません。");
			model.addAttribute("userAuth", loginUser.getUsers().getAuthority());
			return Const.ACCOUNT;
		}
		//既存ユーザとの重複チェック
		//ユーザ名が既に存在する場合
		if (userService.findUserName(userForm.getUserName())) {
			model.addAttribute("isChangeNameErr", true);
			model.addAttribute("errMsg", "このユーザ名は既に使用されています。");
			model.addAttribute("userAuth", loginUser.getUsers().getAuthority());
			return Const.ACCOUNT;
		}

		//ログインユーザのIDをセッションから取得
		String userId = loginUser.getUsername();
		//変更対象IDと変更後ユーザ名をUpdateNameメソッドに渡す
		userService.updateName(userForm.getUserName(), userId);
		//ファイルのcreate_userとupdate_userを更新
		userService.updateFilesUser(userForm.getUserName(), loginUser.getUsers().getUserName());
		//更新完了通知表示用(表示)
		redirectAttributes.addFlashAttribute("isUpdated", true);
		loginUser.getUsers().setUserName(userForm.getUserName());
		return Const.REDIRECT_UPDATE_USER;
	}

	/**
	 * パスワード変更
	 * @param newPassword
	 * @param newConPassword
	 * @param session
	 * @param mav
	 * @return
	 */
	@PostMapping("/updatePassword")
	public String updatePassword(@ModelAttribute("userForm") @Validated Users userForm, BindingResult br,
			@AuthenticationPrincipal DbUsersDetails loginUser, Model model, RedirectAttributes redirectAttributes) {
		userForm.setUserId(loginUser.getUsers().getUserId());
		userForm.setUserName(loginUser.getUsers().getUserName());

		if(br.hasFieldErrors("password") || br.hasFieldErrors("conPassword")) {
			model.addAttribute("userAuth", loginUser.getUsers().getAuthority());
			model.addAttribute("isDispPwErr", true);
			model.addAttribute("isDispConPwErr", true);
			return Const.ACCOUNT;
		}

		//パスワードと確認パスワードの入力一致チェック
		//一致しない場合
		if (!(userForm.getPassword().equals(userForm.getConPassword()))) {
			model.addAttribute("isChangePwErr", true);
			model.addAttribute("errMsg", "パスワードが一致しません。");
			model.addAttribute("userAuth", loginUser.getUsers().getAuthority());
			//更新完了通知表示用(非表示)
			return Const.ACCOUNT;
		}

		//登録済みユーザ名と入力値が異なるかチェック
		//変更がない場合
		//BCryptPasswordEncoderでハッシュ化されたパスワードと入力された平文パスワードを比較
		PasswordEncoder pe = new BCryptPasswordEncoder();
		if(pe.matches(userForm.getPassword(), loginUser.getUsers().getPassword())) {
			model.addAttribute("isChangePwErr", true);
			model.addAttribute("errMsg", "パスワードに変更がありません。");
			model.addAttribute("userAuth", loginUser.getUsers().getAuthority());
			return Const.ACCOUNT;
		}

		//ログインユーザのIDをセッションから取得
		String userId = loginUser.getUsers().getUserId();
		//変更対象IDと変更後ユーザ名をUpdateNameメソッドに渡す
		userService.updatePassword(userForm.getPassword(), userId);
		//パスワードセット
		loginUser.getUsers().setPassword(pe.encode(userForm.getPassword()));
		//更新完了通知表示用(表示)
		redirectAttributes.addFlashAttribute("isUpdated", true);
		return Const.REDIRECT_UPDATE_USER;
	}

}
