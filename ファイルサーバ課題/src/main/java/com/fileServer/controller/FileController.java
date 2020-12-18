package com.fileServer.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

import com.fileServer.entity.DbUsersDetails;
import com.fileServer.entity.FileData;
import com.fileServer.mapper.FileMapper;

@Controller
public class FileController {

	@Autowired
	//ファイルの検索に関するメソッドを参照する
	FileMapper fileMapper;

	//トップのファイル一覧画面
	@SuppressWarnings("unused")
	@PostMapping("/fileView")
	@Transactional(readOnly=false)
	//directoryPathとしてfilePathを受け取って、パラメーターがnullならホームディレクトリ表示
	public String toMoveView(@RequestParam("directoryPath") String directoryPath, Model model) {
//		ModelAndView mav = new ModelAndView();
//		List<FileData> fileList = fileMapper.findAll();
		//カレントディレクトリ(プロジェクトフォルダ)の絶対パス取得
		String tmpPath = new File(".").getAbsoluteFile().getParent();
		//カレントディレクトリのフォルダ一覧をlist変数へ
		File[] metaList = new File(tmpPath).listFiles();
		//「FileServer」というフォルダがなければ作成
		File homeDirectory = null;
		boolean isExist = false;
		for(File ls: metaList) {
			if(Arrays.toString(metaList).contains(tmpPath + "\\" + "FileServer")) {
				isExist = true;
				break;
			}
		}
		if(isExist == false) {
			homeDirectory = new File(tmpPath + "\\" + "FileServer");
			homeDirectory.mkdir();
		}

		File[] dispList = null;
		//他のディレクトリ一覧に遷移するとき
		if(directoryPath != null) {
			dispList = new File(directoryPath).listFiles();
		} else {
			//トップディレクトリを表示するとき
			directoryPath = new File(tmpPath + "\\" + "FileServer").getAbsolutePath();
			dispList = new File(directoryPath).listFiles();
		}
		//ファイル・フォルダまでのフルパスを保存
//		List<File> path = new ArrayList<>();
		//表示用のファイルの名前を保存
//		List<String> dispName = new ArrayList<>();

		//送るデータを格納
		//dispList.lengthはファイル・フォルダの個数
		FileData[] sendData = new FileData[dispList.length];
		//表示用ファイルリスト
		List<String> forDispFileName = new ArrayList<>();
		//表示用フォルダリスト
		List<String> forDispDirName = new ArrayList<>();
		//フォルダ上、ファイル下でソートして表示したい
		List<String> dispText = new ArrayList<>();
		for(int i = 0; i < dispList.length; i++) {
			//個別のファイルについて
			FileData file = new FileData();
			//パスの最後の\までのみとマッチさせる
//			Matcher matcher = Pattern.compile("^.*\\\\").matcher(directoryPath);
			//matcherを空白にして、パスの最後(ファイル名 or フォルダ名)のみ取得
//			String lastStr = matcher.replaceAll("");
			//パスの最後(ファイル名 or フォルダ名)のみ取得
			String dispStr = dispList[i].getName();
			//lastStrに.が含まれる場合は表示用ファイルリストに、含まれない場合は表示用フォルダリストにadd
			if(dispStr.contains(".")) {
				forDispFileName.add(dispStr);
			} else {
				forDispDirName.add(dispStr);
			}
			file.setFileName(dispStr);
			file.setFilePath(directoryPath + "\\" + dispStr);

			file.setUpdateDate(dispStr);

			sendData[i] = file;
		}

//		mav.addObject("path", homeDirectory);
		model.addAttribute("files", sendData);
		return "fileView";
	}



	@SuppressWarnings("unused")
	@GetMapping("/fileView")
	@Transactional(readOnly=false)
	//directoryPathとしてfilePathを受け取って、パラメーターがnullならホームディレクトリ表示
	public String toHomeDirectory(Model model) {
//		ModelAndView mav = new ModelAndView();
//		List<FileData> fileList = fileMapper.findAll();
		//カレントディレクトリ(プロジェクトフォルダ)の絶対パス取得
		String tmpPath = new File(".").getAbsoluteFile().getParent();
		//カレントディレクトリのフォルダ一覧をlist変数へ
		File[] metaList = new File(tmpPath).listFiles();
		//「FileServer」というフォルダがなければ作成
		File homeDirectory = null;
		boolean isExist = false;
		for(File ls: metaList) {
			if(Arrays.toString(metaList).contains(tmpPath + "\\" + "FileServer")) {
				isExist = true;
				break;
			}
		}
		if(isExist == false) {
			homeDirectory = new File(tmpPath + "\\" + "FileServer");
			homeDirectory.mkdir();
		}

		File[] dispList = null;
		//他のディレクトリ一覧に遷移するとき
		String directoryPath = new File(tmpPath + "\\" + "FileServer").getAbsolutePath();
		dispList = new File(directoryPath).listFiles();
		//ファイル・フォルダまでのフルパスを保存
//		List<File> path = new ArrayList<>();
		//表示用のファイルの名前を保存
//		List<String> dispName = new ArrayList<>();

		//送るデータを格納
		//dispList.lengthはファイル・フォルダの個数
		FileData[] sendData = new FileData[dispList.length];
		//表示用ファイルリスト
		List<String> forDispFileName = new ArrayList<>();
		//表示用フォルダリスト
		List<String> forDispDirName = new ArrayList<>();
		//フォルダ上、ファイル下でソートして表示したい
		List<String> dispText = new ArrayList<>();
		for(int i = 0; i < dispList.length; i++) {
			//個別のファイルについて
			FileData file = new FileData();
			//パスの最後の\までのみとマッチさせる
//			Matcher matcher = Pattern.compile("^.*\\\\").matcher(directoryPath);
			//matcherを空白にして、パスの最後(ファイル名 or フォルダ名)のみ取得
//			String lastStr = matcher.replaceAll("");
			//パスの最後(ファイル名 or フォルダ名)のみ取得
			String dispStr = dispList[i].getName();
			//lastStrに.が含まれる場合は表示用ファイルリストに、含まれない場合は表示用フォルダリストにadd
			if(dispStr.contains(".")) {
				forDispFileName.add(dispStr);
			} else {
				forDispDirName.add(dispStr);
			}
			file.setFileName(dispStr);
			file.setFilePath(directoryPath + "\\" + dispStr);

			file.setUpdateDate(dispStr);

			sendData[i] = file;
		}

//		mav.addObject("path", homeDirectory);
		model.addAttribute("files", sendData);
		return "fileView";
	}


	//ファイル・フォルダのアップロード(DBへのINSERT処理)
	//アップロード時に、対応可能な拡張子のみ判断して、対象外のものはエラーではねるのがいいか
	//→アップロード不可能なものが多いのでどうにかならないか。PDFくらいは可能にしたいが
	@PostMapping("/upload")
	@Transactional(readOnly = false)
	public ModelAndView upload(@RequestPart MultipartFile[] multipartFile, Model model,
			@AuthenticationPrincipal DbUsersDetails loginUser) {
		FileData fileData = new FileData();
		//複数ファイルアップロード
		for (int i = 0; i < multipartFile.length; i++) {
			//最大値IDを取得
			long maxId = fileMapper.getMaxId();
			//追加するデータを作成
			fileData.setFileId(maxId + 1);
			//送られてきたファイル名が入るセット
			fileData.setFileName(multipartFile[i].getOriginalFilename());
			try{
				//ファイルのデータをバイナリーコードにする
				fileData.setFileObj(multipartFile[i].getInputStream());
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
			//ログインユーザのユーザ名を取得してセット
			fileData.setCreateUser(loginUser.getusers().getUserName());
			fileData.setUpdateUser(loginUser.getusers().getUserName());
			//アップロードするディレクトリ取得(後ほどトップのディレクトリ名に変更する)
			String uploadDir = new File(".").getAbsoluteFile().getParent();
			//格納先ディレクトリ、ファイル名を指定してファイルオブジェクトを作成
			File convFile = new File(uploadDir,multipartFile[i].getOriginalFilename());
			try {
				//上記指定の情報で実ファイル作成
				convFile.createNewFile();
				FileOutputStream os = new FileOutputStream(convFile);
				//MultipartFileのByteを取得し、ファイルの中身を書き込み
				os.write(multipartFile[i].getBytes());
				os.close();
			} catch (Exception e) {
				System.out.println(e);
			}
			//取得したディレクトリにファイル名を最後に追加したpath
			uploadDir = uploadDir + "\\" + fileData.getFileName();
			fileData.setFilePath(uploadDir);
			//DBに1件追加
			fileMapper.insert(fileData);
		}
		//更新したDBからファイルリストを生成しビューに表示
		return toMoveView(model);
	}


	@RequestMapping("/downloadDir")
	@Transactional(readOnly = false)
	public String download(@RequestParam("downloadDir") String filePath, HttpServletResponse response) {
		//ファイルのパスは、例えば「C:\\Users\\fujit\\GoogleDrive\\javaProject\\fileServer\\sampleExcel - コピー.xlsx」など
		//ファイルパスはビューから取得

		//読み込み、書き込み用オブジェクトを作成
		InputStream is = null;
		OutputStream os = null;

		//ダウンロード対象のファイルオブジェクトを作成
		File dlFileObj = new File(filePath);

		//レスポンスにダウンロードファイルの情報を設定
		//バイナリー形式を指定
		response.setContentType("application/octet-stream");
		response.setHeader("Cache-Control", "private");
		response.setHeader("Pragma", "");
		//ダウンロード時のファイル名を指定→※要エンコード
		response.setHeader("Content-Disposition", "attachment;filename=\"" + dlFileObj.getName() + "\"");

		try {
			//対象ファイルが存在しない場合の処理
			if (!(dlFileObj.exists())) {
				System.out.println("ファイルが存在しません");
				//ビューにメッセージを返す
				return null;
			}

			//ダウンロードファイルへ出力
			is = new FileInputStream(dlFileObj);
			os = response.getOutputStream();
			byte[] buff = new byte[8192];
			int len = 0;
			//入力ストリームのバイトが終わりに達するまで取得し、出力ストリームに書き込む
			//終わりに達したら-1を返す
			while ((len = is.read(buff, 0, buff.length)) != -1) {
				os.write(buff, 0, len);
			}
			//バッファにためたデータを書き込む
			os.flush();

		} catch (Exception e) {
			System.err.println(e);
		} finally {
			try {
				//クローズ処理
				is.close();
				os.close();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		//Responseを直接指定しているため、画面遷移先はnullを指定
		return null;
	}



//	ファイル名(パスの最後だけなので、ファイル名でもフォルダ名でもOK)だけ検索正規表現
//	→ Windowsは [^\\]+$
//	→ Linuxは [^/]+$
//
//	フォルダ名だけ検索正規表現
//	→ Windowsは ^.*\\
//	→ Linuxは^.*/


}
