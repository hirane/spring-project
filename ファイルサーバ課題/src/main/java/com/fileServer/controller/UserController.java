package com.fileServer.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.fileServer.entity.UserForm;
import com.fileServer.entity.Users;
import com.fileServer.service.UserService;

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
	@GetMapping("/userList")
	public ModelAndView showUserList(ModelAndView mav) throws UsernameNotFoundException {
		List<Users> userlist = new ArrayList<Users>();
		;
		userlist = userService.findAll();
		mav.addObject("userlist", userlist);
		mav.setViewName("userList");
		return mav;
	}

	/**
	 * 「/editAuth」アクセス時：ユーザ権限更新処理
	 * @param newAuthority
	 * @return userListページ
	 */
	@PostMapping("/editAuth")
	public ModelAndView editAuthority(@RequestParam("updateId") String updateId,
			@RequestParam("editAuth") int newAuthority) {
		userService.updateAuthority(updateId, newAuthority);
		ModelAndView mav = new ModelAndView();
		return showUserList(mav);
	}

	/**
	 * 「/delete」アクセス時：ユーザ削除処理
	 * @param deleteId
	 * @param mav
	 * @return userListページ
	 */
	@PostMapping("/delete")
	public ModelAndView deleteUser(@RequestParam("deleteId") String deleteId, ModelAndView mav) {
		userService.delete(deleteId);
		mav.addObject("successMsg", "削除しました");
		return showUserList(mav);
	}

	//ーーーーーーーーーーーーーユーザ登録画面ーーーーーーーーーーーーーーーーー
	/**
	*「/regist」GETアクセス時：登録画面に遷移
	* @return mav
	*/
	@GetMapping("/regist")
	public ModelAndView moveToRegistForm() {
		ModelAndView mav = new ModelAndView();
		UserForm userForm = new UserForm();
		mav.addObject("userForm", userForm);
		mav.addObject("isRegistered", false);
		mav.setViewName("regist");
		return mav;
	}

	/**
	 *「/regist」POSTアクセス時：フォームの入力値チェック後にDB登録処理
	 * @param formUser フォームからの入力情報を取り込んだUserインスタンス
	 * @param conPassword 確認用パスワード
	 * @param mav
	 * @return mav
	 */
	@PostMapping("/regist")
	/*public ModelAndView userRegist(@ModelAttribute("userForm") @Validated(UserForm.All.class) UserForm userForm, BindingResult br, ModelAndView mav){*/
	public ModelAndView userRegist(@ModelAttribute("userForm") @Validated UserForm userForm, BindingResult br,
			ModelAndView mav) {
		//登録完了メッセージ出力用(非表示)
		mav.addObject("isRegistered", false);
		//遷移先画面
		mav.setViewName("regist");

		//バリデーションチェック
		//入力制限の違反する場合
		if (br.hasErrors()) {
			return mav;
		}

		//既存ユーザとの重複チェック
		//ユーザIDが既に存在する場合
		if (userService.isDuplicatedUserId(userForm.getUserId())) {
			mav.addObject("isErr", true);
			mav.addObject("errMsg", "このユーザIDは既に使用されています");
			return mav;
			//ユーザ名が既に存在する場合
		} else if (userService.isDuplicatedUserName(userForm.getUserName())) {
			mav.addObject("isErr", true);
			mav.addObject("errMsg", "このユーザ名は既に使用されています");
			return mav;
		}

		//パスワードと確認パスワードの入力一致チェック
		//一致しない場合
		if (!(userService.conPasswordCheck(userForm.getPassword(), userForm.getConPassword()))) {
			mav.addObject("isErr", true);
			mav.addObject("errMsg", "パスワードが一致しません");
			return mav;
		}

		//DBにユーザ情報を登録
		userService.insertUserInfo(userForm);
		//登録完了メッセージ出力用(表示)
		mav.addObject("isRegistered", true);
		return mav;
	}

	//ーーーーーーーーーーーーーユーザ情報更新画面ーーーーーーーーーーーーーーーーー

	/**
	 * 「/updateUser」アクセス時：ユーザ情報更新画面に遷移
	 * @param loginUser
	 * @param mav
	 * @return
	 */
	@GetMapping("/updateUser")
	//セッションからログインユーザ情報を取得しUserインスタンスをビューに返す
	public ModelAndView showUserUpdate(@AuthenticationPrincipal User sessionUser, ModelAndView mav) {
		//セッションのユーザIDをキーにログインユーザ情報を取得
		Users loginUser = userService.findById(sessionUser.getUsername());
		mav.addObject("loginUser", loginUser);
		mav.setViewName("account");
		return mav;
	}

	/**
	 * 「/updateName」アクセス時：ユーザ名変更
	 * @param newName
	 * @param session
	 * @param mav
	 * @return
	 */
	@PostMapping("/updateName")
	public ModelAndView updateUserName(@RequestParam("userName") String newName, @AuthenticationPrincipal User sessionUser,
			ModelAndView mav) {
		//更新完了通知表示用(非表示)
		mav.addObject("isUpdated", false);
		//既存ユーザとの重複チェック
		//ユーザ名が既に存在する場合
		if (userService.isDuplicatedUserName(newName)) {
			mav.addObject("isErr", true);
			mav.addObject("errMsg", "このユーザ名は既に使用されています");
			return showUserUpdate(sessionUser,mav);
		}
		//ログインユーザのIDをセッションから取得
		String userId = sessionUser.getUsername();
		//変更対象IDと変更後ユーザ名をUpdateNameメソッドに渡す
		userService.updateName(newName, userId);
		//更新完了通知表示用(表示)
		mav.addObject("isUpdated", true);
		return showUserUpdate(sessionUser,mav);
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
	public ModelAndView updatePassword(@RequestParam("password") String newPassword,
			@RequestParam("conPassword") String newConPassword,@AuthenticationPrincipal User sessionUser, ModelAndView mav) {
		//パスワードと確認パスワードの入力一致チェック
		//一致しない場合
		if (!(userService.conPasswordCheck(newPassword, newConPassword))) {
			mav.addObject("isErr", true);
			mav.addObject("errMsg", "パスワードが一致しません");
			//更新完了通知表示用(非表示)
			mav.addObject("isUpdated", false);
			return showUserUpdate(sessionUser,mav);
		}
		//ログインユーザのIDをセッションから取得
		String userId = sessionUser.getUsername();
		//変更対象IDと変更後ユーザ名をUpdateNameメソッドに渡す
		userService.updatePassword(newPassword, userId);
		//更新完了通知表示用(表示)
		mav.addObject("isUpdated", true);
		return showUserUpdate(sessionUser,mav);
	}

	//ーーーーーーーーーーーーーファイル表示画面ーーーーーーーーーーーーーーーーー

	/*FileControllerで処理しているため削除
	 *     *//**
			* 「/FileView」アクセス時：ファイル表示画面に遷移
			* @param model
			* @return
			*//*
				@GetMapping("/FileView")
				public String showFileView(Model model){
				return "fileView";
				}*/
}
