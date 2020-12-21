package com.fileServer.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import com.fileServer.entity.DbUsersDetails;
import com.fileServer.entity.FileData;
import com.fileServer.mapper.FileMapper;

@Service
public class FileService {

	@Autowired
	//ファイルの検索に関するメソッドを参照する
	FileMapper fileMapper;

	//フォルダがあるかを確認し、なければ作成
	public void makeDirectory(String directoryPath) {
		//指定されたパスをFileクラスでnew
		File homeDirectory = new File(directoryPath);
		//指定されたパスまでのフォルダがなければ作成
		homeDirectory.mkdirs();

		//allFilesメソッドの情報を入れるためのリスト
		List<FileData> list = new ArrayList<>();
		list = allFiles(homeDirectory, list);
		//filesテーブルのデータを全て削除
		fileMapper.clearFilesTable();
		//空のfilesテーブルに存在する分のファイルのデータを書き込む
		for(int i = 0; i < list.size(); i++) {
			fileMapper.insert(list.get(i));
		}
	}

	/*
	再帰処理で全フォルダとファイルのDB上のデータを取得
	実データはあってもDBにデータが存在しない場合(エクスプローラー上にだけ存在する場合)はunlnownを入れる
	DB上にデータはあっても実物はない場合(手動でエクスプローラーで消した場合など)は削除
	*/
//	List<FileData> allFilesList;
	public List<FileData> allFiles(File directory, List<FileData> allFilesList) {
		//データの一覧を取得
		File[] files = directory.listFiles();
		//存在するフォルダ・ファイル数分だけ繰り返し
		for(File file: files) {
			//フォルダの場合は再帰処理で、その下の階層を見に行く
			if(file.isDirectory()) {
				//該当ファイルがDBに存在するかをチェック
				boolean isExist = fileMapper.isExistFile(file.getAbsolutePath());
				//存在する場合
				if(isExist) {
					//該当ファイルのDBの情報をdataに入れる
					FileData data = fileMapper.findByFilePath(file.getAbsolutePath());
					allFilesList.add(data);
				}
				//存在しない場合
				else {
					//ファイル名とファイルパス以外はunknownで埋める
					FileData data = new FileData();
					data.setFileName(file.getName());
					data.setFilePath(file.getAbsolutePath());
					data.setCreateDate("Unknown Date");
					data.setCreateUser("Unknown User");
					data.setUpdateDate("Unknown Date");
					data.setUpdateUser("Unknown User");
					allFilesList.add(data);
				}
				//再帰処理
				allFiles(file, allFilesList);
			}
			//ファイルの場合はファイル数分だけ繰り返す(処理はフォルダの処理と同じ)
			else {
				boolean isExist = fileMapper.isExistFile(file.getAbsolutePath());
				if(isExist) {
					FileData data = fileMapper.findByFilePath(file.getAbsolutePath());
					allFilesList.add(data);
				} else {
					FileData data = new FileData();
					data.setFileName(file.getName());
					data.setFilePath(file.getAbsolutePath());
					data.setCreateDate("Unknown Date");
					data.setCreateUser("Unknown User");
					data.setUpdateDate("Unknown Date");
					data.setUpdateUser("Unknown User");
					allFilesList.add(data);
				}
			}
		}
		return allFilesList;
	}


	@Transactional
	public void dispFileList(String directoryPath, Model model) {
		//指定されたパスのディレクトリがなければ作成する
		makeDirectory(directoryPath);

		File[] dispList = new File(directoryPath).listFiles();
		Arrays.sort(dispList);

		//表示用ファイルリスト
		List<FileData> forDispFileName = new ArrayList<>();
		//表示用フォルダリスト
		List<FileData> forDispDirName = new ArrayList<>();
		//dispList.lengthはファイル・フォルダの個数
		for(int i = 0; i < dispList.length; i++) {
			//対象ディレクトリに含まれる個別のファイルについて
			FileData file = new FileData();
			//パスの最後(ファイル名 or フォルダ名)のみ取得
			String dispStr = dispList[i].getName();
			//dispStrに.が含まれる場合は表示用ファイルリストに、含まれない場合は表示用フォルダリストにadd
			file.setFileName(dispStr);
			file.setFilePath(directoryPath + "\\" + dispStr);
			//SQLで、該当するfile_pathのデータを取得して、データをセット
			FileData data = fileMapper.findByFilePath(file.getFilePath());
			file.setUpdateDate(data.getUpdateDate());
			file.setUpdateUser(data.getUpdateUser());
			if(dispStr.contains(".")) {
				//ファイルの場合
				forDispFileName.add(file);
			} else {
				//フォルダの場合
				forDispDirName.add(file);
			}
		}

		String nowDirectory = new File(".").getAbsoluteFile().getParent() + "\\FileServer";
		model.addAttribute("homeDirectory", nowDirectory);
		//ディレクトリ階層表示のためのリスト
		List<FileData> nowDirectories = new ArrayList<>();
		//ホームディレクトリ以外を表示する場合
		if(!((directoryPath).equals(nowDirectory))) {
			//"~~\FileServer" までのパスを空白に
			String lowerLevelDirectories = directoryPath.replace(nowDirectory + "\\" , "");
			//パスの残り部分を"\"で分割
			String[] lowerLevelDirectory = lowerLevelDirectories.split("\\\\");
			for(String dir: lowerLevelDirectory) {
				FileData file = new FileData();
				//階層に表示する名前
				file.setFileName(dir);
				//ビューから返す時のパス情報
				nowDirectory = nowDirectory + "\\" + dir;
				file.setFilePath(nowDirectory);
				//階層数分リストにadd
				nowDirectories.add(file);
			}
		}
		//ディレクトリ階層表示のためのリスト
		model.addAttribute("directoryLevels", nowDirectories);
		//アップロードするときの階層情報を保持させる変数
		model.addAttribute("currentPath", directoryPath);
		//フォルダ表示用のリスト
		model.addAttribute("directories", forDispDirName);
		//ファイル表示用のリスト
		model.addAttribute("files", forDispFileName);
	}

	@Transactional
	//アップロード処理
	public void uploadLogic(String uploadPath, MultipartFile[] multipartFile, DbUsersDetails loginUser) {
		FileData fileData = new FileData();
		//複数ファイルアップロード
		for (int i = 0; i < multipartFile.length; i++) {
			//送られてきたファイル名が入るセット
			fileData.setFileName(multipartFile[i].getOriginalFilename());
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
			//格納先ディレクトリ、ファイル名を指定してファイルオブジェクトを作成
			File convFile = new File(uploadPath,multipartFile[i].getOriginalFilename());
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
			fileData.setFilePath(uploadPath + "\\" + fileData.getFileName());
			//DBに1件追加
			fileMapper.insert(fileData);
		}
	}

	@Transactional
	//ダウンロード処理
	//ファイルパスはビューから取得
	public int downloadLogic(String filePath, HttpServletResponse response) {
		final int DOWNLOADSUCCESS = 0;
		final int DOWNLOADFAILED = 1;
		//読み込み、書き込み用オブジェクトを作成
		InputStream is = null;
		OutputStream os = null;

		//ダウンロード対象のファイルオブジェクトを作成
		File dlFileObj = new File(filePath);
		//ダウンロード時のファイル名を指定
		String fileName = "";
		//半角スペースは+に置換されるので、半角をエンコードされない文字"\\*"に置換
		String tmpName = dlFileObj.getName();
		Pattern tmpPattern = Pattern.compile(" ");
		String tmpStr = tmpPattern.matcher(tmpName).replaceAll("\\*");
		try {
			fileName = URLEncoder.encode(tmpStr, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		//仮に置いた"\\*"を半角スペースに戻す
		Pattern replacePattern = Pattern.compile("\\*");
		//ダウンロード時のファイル名を完全に元に戻す
		String downloadName = replacePattern.matcher(fileName).replaceAll(" ");

		//レスポンスにダウンロードファイルの情報を設定
		//バイナリー形式を指定
		response.setContentType("application/octet-stream");
		response.setHeader("Cache-Control", "private");
		response.setHeader("Pragma", "");
		//ダウンロード時のファイル名を指定
		response.setHeader("Content-Disposition", "attachment;filename=" + downloadName);

		try {
			//対象ファイルが存在しない場合の処理
			if (!(dlFileObj.exists())) {
				System.out.println("ファイルが存在しません");
				//ビューにメッセージを返す
				return DOWNLOADFAILED;
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
		return DOWNLOADSUCCESS;
	}


	@Transactional
	//フォルダダウンロード処理
	//https://tanakakns.hatenablog.com/entry/20130424/1366806184
	//http://mslabo.sakura.ne.jp/WordPress/make/processing%E3%80%80%E9%80%86%E5%BC%95%E3%81%8D%E3%83%AA%E3%83%95%E3%82%A1%E3%83%AC%E3%83%B3%E3%82%B9/zip%E5%9C%A7%E7%B8%AE%E3%82%92%E8%A1%8C%E3%81%86%E3%81%AB%E3%81%AF%E6%A8%99%E6%BA%96%E3%83%A9%E3%82%A4%E3%83%96%E3%83%A9%E3%83%AA%E7%B7%A8/
	public void downloadDirLogic(String filePath, HttpServletResponse response) throws IOException {
		//ダウンロード対象のファイルオブジェクトを作成
		File dlFileObj = new File(filePath);
		//ダウンロード時のファイル名を指定
		String fileName = "";
		//半角スペースは+に置換されるので、半角をエンコードされない文字"\\*"に置換
		String tmpName = dlFileObj.getName();
		Pattern tmpPattern = Pattern.compile(" ");
		String tmpStr = tmpPattern.matcher(tmpName).replaceAll("\\*");
		try {
			fileName = URLEncoder.encode(tmpStr, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		//仮に置いた"\\*"を半角スペースに戻す
		Pattern replacePattern = Pattern.compile("\\*");
		//ダウンロード時のファイル名を完全に元に戻す
		String downloadName = replacePattern.matcher(fileName).replaceAll(" ");

		//レスポンスにダウンロードファイルの情報を設定
		response.setContentType("application/octet-stream;charset=MS932");
		//ダウンロード時のファイル名を指定
		response.setHeader("Content-Disposition", "attachment;filename=" + downloadName + ".zip");
		//バイナリー形式を指定
		response.setHeader("Content-Transfer-Encoding", "binary");

		//allFilesメソッドの情報を入れるためのリスト
		List<FileData> list = new ArrayList<>();
		list = allFiles(dlFileObj, list);

		OutputStream os = response.getOutputStream();
		// ZIP出力オブジェクトを取得（日本語の文字化けに対応するために文字コードは Shift-JIS を指定）
		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(os), Charset.forName("Shift_JIS"));

		for(int i = 0; i < list.size(); i++) {
			if(!(list.get(i).getFileName().contains("."))) {
				continue;
			}

			//パスから削除する部分
			String deleteStr = new File(filePath).getParent();
			String entryName = list.get(i).getFilePath().replace(deleteStr + "\\" , "");

			ZipEntry ze = new ZipEntry(entryName);
			zos.putNextEntry(ze);

			InputStream in = new BufferedInputStream(new FileInputStream(list.get(i).getFilePath()));

			byte[] b = new byte[8192];
			int len;
			while((len = in.read(b)) != -1) {
				zos.write(b, 0, len);
			}
			in.close();
			zos.closeEntry();
		}
		zos.close();
	}



}
