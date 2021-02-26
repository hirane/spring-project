package com.fileServer.config;

/**
 * 定数クラス
 *
 * @since
 * @author
 * @version
 */
public class Const {

	//============================================
	//ビュー名定数
	//============================================
	/**
	 * ログイン画面HTML
	 */
	public static final String LOGIN = "login";

	/**
	 * 登録画面HTML
	 */
	public static final String REGIST = "regist";

	/**
	 * ファイル一覧画面HTML
	 */
	public static final String FILEVIEW = "fileView";

	/**
	 * ユーザ情報更新画面HTML
	 */
	public static final String ACCOUNT = "account";

	/**
	 * ユーザ管理画面HTML
	 */
	public static final String USERLIST = "userList";

	/**
	 * "/login"へリダイレクト
	 */
	public static final String REDIRECT_LOGIN = "redirect:/login";

	/**
	 * "/fileView/top"へリダイレクト
	 */
	public static final String REDIRECT_FILEVIEW_TOP = "redirect:/fileView/top";

	/**
	 * "/userList"へリダイレクト
	 */
	public static final String REDIRECT_USERLIST = "redirect:/userList";

	/**
	 * "/registView"へリダイレクト
	 */
	public static final String REDIRECT_REGIST = "redirect:/registView";

	/**
	 * "/registView"へリダイレクト
	 */
	public static final String REDIRECT_UPDATE_USER = "redirect:/updateUser";


	//============================================
	//フィールド名定数
	//============================================

	/**
	 * FileFormクラスの"thisDirectoryPath"フィールド
	 */
	public static final String THIS_DIRECTORY_PATH = "thisDirectoryPath";

	//============================================
	//ユーザ関係定数
	//============================================

	/**
	 * 更新者権限
	 */
	public static final int WRITABLE_AUTH = 1;

	/**
	 * 閲覧者権限
	 */
	public static final int READABLE_AUTH = 2;

	//============================================
	//ファイル関係定数
	//============================================

	/**
	 * アクセス権限がないフォルダが対象の場合
	 */
	public static final int ACCESS_DENIED_ERR = 1;

	/**
	 * 処理成功
	 */
	public static final int PROCESS_SUCCESS = 0;

	/**
	 * 処理失敗
	 */
	public static final int PROCESS_FAILED = 1;

	/**
	 * システムのTempフォルダに一時フォルダ作成が失敗した場合
	 */
	public static final int TMP_MKDIR_FAILED = 1;

	/**
	 * Zip化処理に失敗した場合
	 */
	public static final int ZIPPED_PROCESS_ERR = 2;

	/**
	 * Zipファイルの読み取り・書き込み時にエラーが起きた場合
	 */
	public static final int ZIPFILE_STREAM_ERR = 3;

	/**
	 * 同名ファイル確認時に対象ファイル・フォルダが存在しない場合
	 */
	public static final int CHK_SAMENAME_TARGET_NOT_EXIST = 2;

	/**
	 * 同名ファイル・フォルダが存在している場合
	 */
	public static final int CHK_SAMENAME_EXIST = 3;

	/**
	 * フォルダ移動処理成功
	 */
	public static final int MOVE_DIR_SUCCESS = 2;

	/**
	 * ファイル移動処理成功
	 */
	public static final int MOVE_FILE_SUCCESS = 3;

	/**
	 * フォルダリネーム成功
	 */
	public static final int RENAME_DIR_SUCCESS = 4;

	/**
	 * ファイルリネーム成功
	 */
	public static final int RENAME_FILE_SUCCESS = 5;

	/**
	 * 一般的なバリデーションエラーを上部に表示するフラグ
	 */
	public static final String DISP_PROCESS_ERR_TRUE = "hasProcessErrors";

	/**
	 * 一般的なバリデーションエラーメッセージと対応するThymeleaf用変数
	 */
	public static final String PROCESS_ERR_MSG = "processErrMsg";

	/**
	 * DB書き込み時の日付フォーマット
	 */
	public static final String DATE_FORMAT = "yyyy/MM/dd HH:mm";

	/**
	 * 作成者・更新者が不明の場合にDBに書き込む値
	 */
	public static final String UNKNOWN_USER = "Unknown User";

	/**
	 * 作成日・更新日が不明の場合にDBに書き込む値
	 */
	public static final String UNKNOWN_DATE = "Unknown Date";

	/**
	 * 文字コードUTF-8
	 */
	public static final String UTF8 = "UTF-8";

	/**
	 * 文字コードMS932
	 */
	public static final String MS932 = "MS932";

	/**
	 * Zip一時ファイル作成時の作成フォルダのプレフィックス
	 */
	public static final String TMP_ZIP_PREFIX = "zipoutput_tmp_";

	/**
	 * Zipファイル拡張子
	 */
	public static final String ZIP_EXTENSION = ".zip";



}
