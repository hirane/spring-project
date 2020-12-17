package com.fileServer.controller;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.fileServer.entity.FileData;
import com.fileServer.mapper.FileMapper;

@Controller
public class FileController {

	@Autowired
	//ファイルの検索に関するメソッドを参照する
	FileMapper fileMapper;
	//ユーザ情報検索に関するメソッドを参照する
//	@Autowired
//	private UserMapper userMapper;
	//作成・更新者情報を自動で取得するため
//	@Autowired
//	private LoginMapper loginMapper;


	/* Mapping元のURL
	/logout
	 * → logoutメソッド？
	/move *3
	 * → ファイルサーバ画面へ
	 * → ユーザ情報更新画面へ
	 * → ユーザ管理画面へ
	/upload
	 * → ファイル単位とフォルダ全体
	/download *2種
	 * → ファイルを選択して、選択したものを全てダウンロード
	 * → ディレクトリ内のファイルを全てダウンロード
	/makeDirectory
	 * → パスの追加
	/changeDirectory
	 * → 選択されたパスを一番下に持つ場所へ移動
	/changeName
	 * → パスの一番最後だけを上書き(パスの上書き)
	/moveDirectory
	 * → パスの上書き
	/delete
	 * → 削除のメソッド
	 * → lo_unlinkなどをSQL側で使用か
	*/


	/*
	 * パスについて、デフォルトでアップロードされる先がeclipseのワークフォルダのプロジェクトドルだ名の直下になっている
	 * →ただし、エクスプローラーなどからは見えていないので、ファイル移動などの際にエクスプローラーを開いて選ばせる方法は不可か
	 *
	 * プロジェクトフォルダの直下を相対的なhomeディレクトリとみなす方法を要検討
	 * String homePath = new File(".").getAbsoluteFile().getParent();
	 * →homePathまでのパスを全て切り取ってディレクトリ名を"/"にして、以下を相対パスに変換して保存するのはどうか
	 *
	 * 上部ディレクトリ階層の "Top" = homePath
	 * homePathを初期状態では一覧表示させているが、階層を作って、そこを一覧表示させる方法を検討する必要あり
	 *
	 *
	 * */


	//ログイン機能実装後削除します
	@GetMapping("/")
	@Transactional(readOnly=false)
	public ModelAndView toMoveView(Model model) {
		ModelAndView mav = new ModelAndView();
		List<FileData> fileList = fileMapper.findAll();
		mav.addObject("files", fileList);
		mav.setViewName("fileView");
//		model.addAttribute("files", fileList);
		return mav;
//		return "fileView";
	}


	//ファイルサーバ画面トップに移動
	@GetMapping("/move")
	public ModelAndView toMoveView(@RequestParam("moveButton") String fromWhere) {
		ModelAndView mav = new ModelAndView();
		List<FileData> fileList = fileMapper.findAll();
		/*
		 * ファイルサーバ画面: 権限情報(authority)を取得するためユーザ情報にアクセス
		 * ユーザ情報更新画面・ユーザ管理画面: ユーザ情報表示のため
		 */
//		List<User> userList = fileMapper.findAll();
		mav.addObject("files", fileList);
//		mav.addObject("users", userList);
		//どのボタンからのリクエスト化で遷移先の変更
		if(fromWhere.equals("ファイルサーバ")) {
			mav.setViewName("fileView");
		} else if(fromWhere.equals("ユーザ情報更新")) {
			mav.setViewName("account");
		} else if(fromWhere.equals("ユーザ管理")) {
			mav.setViewName("userList");
		}
		return mav;
	}



	//ファイルのダウンロード → 複数個選択したときにどのように値を取るかは要検討
	//仮で、ファイル横にダウンロードボタンをつけて個別ダウンロードにしています
	/*
	 * 複数ダウンロードの場合の案1
	 * チェックボックスにチェックを入れるたびにコントローラに飛ばして、対象のidをListに入れて、
	 * 左のダウンロードが押されたときにListの要素をループでダウンロードする処理をさせる
	 *
	 * */
	@RequestMapping("/downloadDir")
	@Transactional(readOnly = false)
	public String download(@RequestParam("downloadDir") long id, HttpServletResponse response){
		//ファイルのパスは、例えば「C:\\Users\\fujit\\GoogleDrive\\javaProject\\fileServer\\sampleExcel - コピー.xlsx」など
		//ダウンロード対象のファイルデータを取得
		FileData file = fileMapper.findById(id);

		//ファイルの絶対パス取得
		File path = new File(file.getFileName());
		String absFilePath = path.getAbsolutePath();

		//レスポンスにダウンロードファイルの情報を設定
		//バイナリー形式を指定
		response.setContentType("application/octet-stream");
		response.setHeader("Cache-Control", "private");
		response.setHeader("Pragma", "");
		//ダウンロード時のファイル名を指定
		//→現状のままだとスペースがあった場合に+が挿入されてしまっている
		response.setHeader("Content-Disposition","attachment;filename=\"" + getFileName(absFilePath) + "\"");

		//ダウンロードファイルへ出力
		try{
			OutputStream os = response.getOutputStream();
			InputStream is = file.getFileObj();
			byte[] buff = new byte[8192];
			int len = 0;
			//入力ストリームのバイトが終わりに達するまで取得し、出力ストリームに書き込む
			//終わりに達したら-1を返す
			while ((len = is.read(buff, 0, buff.length)) != -1) {
				os.write(buff, 0, len);
			}
			//バッファにためたデータを書き込む
			os.flush();
		}catch(Exception e){
			System.err.println(e);
		}

		//Responseを直接指定しているため、画面遷移先はnullを指定
		return null;
	}

	//ダウンロード時のファイル名をパスから取得
	private String getFileName(String filePath){
		String fileName = "";
		if(filePath != null && !"".equals(filePath)){
			try{
				//ファイル名をUTF-8でエンコードして指定
				fileName = URLEncoder.encode(new File(filePath).getName(), "UTF-8");
			}catch(Exception e){
				System.err.println(e);
				return "";
			}
		}
		return fileName;
	}





	//フォルダのダウンロード
//	@RequestMapping("/downloadDir")
//	publilc String download(@RequestParam("downloadDir") String fileId, HttpServletResponse response) {
//
//	}







	//ファイル・フォルダのアップロード(DBへのINSERT処理)
	//アップロード時に、対応可能な拡張子のみ判断して、対象外のものはエラーではねるのがいいか
	//→アップロード不可能なものが多いのでどうにかならないか。PDFくらいは可能にしたいが
	@PostMapping("/upload")
//	@RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Transactional(readOnly = false)
	public ModelAndView upload(@RequestPart MultipartFile[] multipartFile, Model model/*,
			@AuthenticationPrincipal DbUsersDetails loginUser*/) {
		for (MultipartFile file: multipartFile) {
			FileData fileData = new FileData();
			//最大値IDを取得
			long maxId = fileMapper.getMaxId();
			//追加するデータを作成
			fileData.setFileId(maxId + 1);
			fileData.setFileName(file.getOriginalFilename());
			try{
				fileData.setFileObj(file.getInputStream());
			}catch(Exception e){
				System.err.println(e);
			}
			//現在の日時を取得
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			String now = sdf.format(date);
			//作成日と更新日をセット
			fileData.setCreateDate(now);
			fileData.setUpdateDate(now);
			/* ログインユーザのユーザ名をセット
			fileData.setCreateUser(loginUser.getUserName());
			fileData.setUpdateUser(loginUser.getUserName());
			*/
			//以下2行、仮で設定
			fileData.setCreateUser("作成者テスト");
			fileData.setUpdateUser("更新者テスト");

			String currentPath = new File(".").getAbsoluteFile().getParent();
			fileData.setFilePath(currentPath + "\\" + file.getOriginalFilename());

			//DBに1件追加
			fileMapper.insert(fileData);
		}
		//更新したDBからファイルリストを生成しビューに表示
		return toMoveView(model);

	}





	//ファイル・フォルダの上書きの場合




	/*
	ファイルパスをs.split("\\")で分けて配列に入れて、それを繰り返し表示する
	各要素でmatches("\.")==trueであればファイルの扱い
	→フォルダ作成時に、フォルダ名に「.」があればエラーにするのがいいか
	*/


//	ファイル名(パスの最後だけなので、ファイル名でもフォルダ名でもOK)だけ検索正規表現
//	→ Windowsは [^\\]+$
//	→ Linuxは [^/]+$
//
//	フォルダ名だけ検索正規表現
//	→ Windowsは ^.*\\
//	→ Linuxは^.*/






}
