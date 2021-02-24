package com.fileServer.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fileServer.entity.FileData;
import com.fileServer.entity.FileForm;
import com.fileServer.mapper.FileMapper;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;

@Service
public class FileService {

	//ファイルの検索に関するメソッドを参照する
	@Autowired
	FileMapper fileMapper;
	//ワーキングディレクトリ(トップディレクトリ)のパスを参照する
	@Value("${WORKING_DIRECTORY}")
	private String fileServerDirectory;


	@Transactional
	public int dispFileList(String directoryPath, FileForm fileForm) {
		//指定されたパスのディレクトリがなければ作成する
		init(directoryPath);
		File[] dispList = new File(directoryPath).listFiles();
		//フォルダにアクセス権がない場合
		if(dispList == null) {
			return 1;
		}
		Arrays.sort(dispList);

		//表示用ファイルリスト
		List<FileData> forDispFileNameList = new ArrayList<>();
		//表示用フォルダリスト
		List<FileData> forDispDirectoryNameList = new ArrayList<>();
		//ファイルとフォルダに分けてリストに詰める
		for(File dispItem: dispList) {
			FileData data = fileMapper.findFileAllInfo(dispItem.getAbsolutePath());
			if(dispItem.isFile()) {
				//ファイルの場合
				forDispFileNameList.add(data);
			} else {
				//フォルダの場合
				forDispDirectoryNameList.add(data);
			}
		}

		String top = fileServerDirectory;
		//ディレクトリ階層表示のためのリスト
		List<FileData> directoryLevels = new ArrayList<>();
		//ホームディレクトリ以外を表示する場合
		if(!(directoryPath.equals(fileServerDirectory))) {
			//"FileServer"ディレクトリまでのパスを空白に
			String lowerLevelDirectory = directoryPath.replace(top + File.separator , "");
			//パスの残り部分を"\"で分割
			String[] lowerLevelDirectories = lowerLevelDirectory.split("\\\\");
			for(String dir: lowerLevelDirectories) {
				FileData file = new FileData();
				//階層に表示する名前
				file.setFileName(dir);
				//ビューから返す時のパス情報
				top += File.separator + dir;
				file.setFilePath(top);
				//階層数分リストにadd
				directoryLevels.add(file);
			}
		}

		//ファイル・フォルダ移動用に、全フォルダのリスト取得
		List<FileData> allDirList = new ArrayList<>();
		FileData file = new FileData();
		file.setFileId(-1);
		file.setFileName("Top");
		file.setFilePath(fileServerDirectory);
		allDirList.add(file);
		allDirList = getAllDirectoriesInfo(new File(fileServerDirectory), allDirList);


		//全フォルダのリスト、DBからfile_id、file_name、file_path情報含む
		//移動先選択ポップアップ用
		fileForm.setAllDirList(allDirList);
		//ディレクトリ階層表示のためのリスト
		fileForm.setDirectoryLevels(directoryLevels);
		//フォルダ表示用のリスト、DBの全情報含む
		fileForm.setForDispDirectoryNameList(forDispDirectoryNameList);
		//ファイル表示用のリスト、DBの全情報含む
		fileForm.setForDispFileNameList(forDispFileNameList);
		return 0;
	}


	//フォルダがあるかを確認し、なければ作成
	public void init(String directoryPath) {
		//指定されたパスをFileクラスでnew
		File targetDirectory = new File(directoryPath);
		//指定されたパスまでのフォルダがなければ作成
		targetDirectory.mkdirs();

		//allFilesメソッドの情報を入れるためのマップ
		HashMap<String, FileData> allFileInfo = new HashMap<String, FileData>();
		//Topのディレクトリのパス
		File homeDirectory = new File(fileServerDirectory);
		//リストに存在する全ファイルのデータを詰める
		allFileInfo = getAllInfo(homeDirectory, allFileInfo);
		//filesテーブルのデータを全て削除
		fileMapper.clearFilesTable();
		//空のfilesテーブルに存在する分のファイルのデータを書き込む
		for(FileData data: allFileInfo.values()) {
			fileMapper.insert(data);
		}
	}

	/*
	再帰処理で全フォルダとファイルのDB上のデータを取得
	実データはあってもDBにデータが存在しない場合(エクスプローラー上にだけ存在する場合)はunlnownを入れる
	DB上にデータはあっても実物はない場合(手動でエクスプローラーで消した場合など)は削除
	 */
	//List<FileData> allFilesList;
	public HashMap<String, FileData> getAllInfo(File directory, HashMap<String, FileData> allFilesList) {
		//ディレクトリが存在しない場合
		if(!directory.exists()) {
			return allFilesList;
		}
		//ファイルとフォルダの一覧を取得
		File[] files = directory.listFiles();
		//アクセスが拒否されている場合
		if(files == null) {
			return allFilesList;
		}

		//存在するフォルダ・ファイル数分だけ繰り返し
		for(File file: files) {
			//フォルダの場合は再帰処理で、その下の階層を見に行く
			if(file.isDirectory()) {
				//該当ファイルがDBに存在するかをチェック
				boolean isExist = fileMapper.isExistFile(file.getAbsolutePath());
				//存在する場合
				if(isExist) {
					//該当ファイルのDBの情報をdataに入れる
					FileData data = fileMapper.findFileAllInfo(file.getAbsolutePath());
					allFilesList.put(file.getAbsolutePath(), data);
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
					allFilesList.put(file.getAbsolutePath(), data);
				}
				//再帰処理
				getAllInfo(file, allFilesList);
			}
			//ファイルの場合はファイル数分だけ繰り返す(処理はフォルダの処理と同じ)
			else {
				boolean isExist = fileMapper.isExistFile(file.getAbsolutePath());
				if(isExist) {
					FileData data = fileMapper.findFileAllInfo(file.getAbsolutePath());
					allFilesList.put(file.getAbsolutePath(), data);
				} else {
					FileData data = new FileData();
					data.setFileName(file.getName());
					data.setFilePath(file.getAbsolutePath());
					data.setCreateDate("Unknown Date");
					data.setCreateUser("Unknown User");
					data.setUpdateDate("Unknown Date");
					data.setUpdateUser("Unknown User");
					allFilesList.put(file.getAbsolutePath(), data);
				}
			}
		}
		return allFilesList;
	}


	public List<FileData> getAllDirectoriesInfo(File directory, List<FileData> allDirList) {
		//		List<FileData> filesList = new ArrayList<>(0);
		//		List<FileData> directoriesList = new ArrayList<>(0);
		if(!directory.exists()) {
			return allDirList;
		}
		File[] files = directory.listFiles();
		if(files == null) {
			return allDirList;
		}
		for(File file: files) {
			//フォルダの場合は再帰処理で、その下の階層を見に行く
			if(file.isDirectory()) {
				FileData data = fileMapper.findFileIdNamePath(file.getAbsolutePath());
				allDirList.add(data);
				getAllDirectoriesInfo(file, allDirList);
			}
		}
		return allDirList;
	}


	@Async
	@Transactional
	//アップロード処理
	//処理失敗なら1、成功なら0を返す
	public int uploadLogic(String uploadPath, MultipartFile multipartFile, String userName, FileData data, File oldFile) {
		//処理失敗数をカウント
//		int uploadFalseCount = 0;
		//アップロードファイル名
		String uploadFileName = multipartFile.getOriginalFilename();
		//現在のディレクトリに取得したファイル名でファイルオブジェクト作成
		try(FileOutputStream os = new FileOutputStream(new File(uploadPath, uploadFileName));) {
			//MultipartFileのByteを取得し、ファイルの中身を書き込み
			os.write(multipartFile.getBytes());
		} catch (IOException e) {
			System.out.println("アップロード時のエラーです");
			e.printStackTrace();
			//エラーの場合はファイル名を元に戻す
			if(oldFile != null) {
				String originalPath = oldFile.getAbsolutePath();
				originalPath = originalPath.replaceAll("_old$", "");
				oldFile.renameTo(new File(originalPath));
			}
			return 1;
		}

		//DB書き込み用の現在の日時を取得
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		String nowTime = sdf.format(new Date());

		//DB書き込み処理
		//上書き処理の場合
		if(data != null) {
			//DBのupdate文対象とするデータ検索のためのfile_path文字列
			String searchKeyPath = oldFile.getAbsolutePath();
			searchKeyPath = searchKeyPath.replaceAll("_old$", "");
			//一時的にリネームした同名のファイルを削除
			oldFile.delete();

			//DB登録用のデータセット
			data.setUpdateDate(nowTime);
			data.setUpdateUser(userName);
			//更新日と更新者のみ更新
			fileMapper.updateByRename(searchKeyPath, data);
		}
		//新規データの場合
		else {
			data = new FileData();
			//DBにデータが存在しないが実ファイルは存在する場合
			if(oldFile != null) {
				oldFile.delete();
			}
			//送られてきたファイル名が入るセット
			data.setFileName(uploadFileName);
			//作成日と更新日をセット
			data.setCreateDate(nowTime);
			data.setUpdateDate(nowTime);
			//ログインユーザのユーザ名を取得してセット
			data.setCreateUser(userName);
			data.setUpdateUser(userName);
			//格納先ディレクトリ、ファイル名を指定してファイルオブジェクトを作成
			//取得したディレクトリにファイル名を最後に追加したpath
			data.setFilePath(uploadPath + File.separator + uploadFileName);
			//DBに1件追加
			fileMapper.insert(data);
		}
		return 0;
	}


	//既存の同名ファイルを上書きする場合
	@Transactional
	public int adjustLocalFile(String uploadPath, MultipartFile uploadFile, String userName) {
		//アップロードされるファイルのファイル名取得
		String uploadFileName = uploadFile.getOriginalFilename();
		//アップロードするパスの既存ファイルと名称比較
		File[] localFiles = new File(uploadPath).listFiles();
		//アップロードフォルダにアクセス権がない場合
		if(localFiles == null) {
			return 1;
		}
		for(File localFile: localFiles) {
			//同名ファイルが存在していた場合
			if(uploadFileName.equalsIgnoreCase(localFile.getName())) {
				//DBのデータを検索
				FileData data = fileMapper.findFileAllInfo(localFile.getAbsolutePath());
				//同名ファイル・フォルダの情報がDBに存在しない場合
				if(data != null) {
					//DB上、大文字小文字が区別されるため、アップロードファイルの名前に合わせる
					data.setFileName(uploadFileName);
					data.setFilePath(uploadPath + File.separator + uploadFileName);
				}
				//一時的にリネームし、アップロードできた場合に削除、失敗した場合に戻すため
				if(localFile.renameTo(new File(localFile + "_old"))) {
					File localTempFile = new File(localFile.getAbsoluteFile() + "_old");
					return uploadLogic(uploadPath, uploadFile, userName, data, localTempFile);
				} else {
					//仮のファイル名にリネームができなかった場合
					return 1;
				}
			}
		}
		//同名ファイルが存在しないファイルだった場合
		return uploadLogic(uploadPath, uploadFile, userName, null, null);
	}




//	@Transactional
	//ダウンロード処理
	//ファイルパスはビューから取得
	public int downloadLogic(String filePath, HttpServletResponse response) {
		//ダウンロード対象のファイルオブジェクトを作成
		File downloadFileObj = new File(filePath);
		//ダウンロード時のファイル名を指定
		String fileName = "";
		//半角スペースは+に置換されるので、半角をエンコードされない文字"\\*"に置換
		String tmpName = downloadFileObj.getName();
		Pattern tmpPattern = Pattern.compile(" ");
		String tmpStr = tmpPattern.matcher(tmpName).replaceAll("\\*");
		try {
			fileName = URLEncoder.encode(tmpStr, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			System.out.println("個別ファイル、ファイル名エンコードでエラーが発生しました");
			e.printStackTrace();
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

		//読み込み、書き込み用オブジェクトを作成
		try(InputStream is = new FileInputStream(downloadFileObj); OutputStream os = response.getOutputStream()) {
			//ダウンロードファイルへ出力
			IOUtils.copy(is, os);
			/*
			byte[] buff = new byte[8192];
			int len = 0;
			//入力ストリームのバイトが終わりに達するまで取得し、出力ストリームに書き込む
			//終わりに達したら-1を返す
			while ((len = is.read(buff, 0, buff.length)) != -1) {
				os.write(buff, 0, len);
			}
			//バッファにためたデータを書き込む
			os.flush();
			*/
		} catch (IOException e) {
			System.out.println("個別ファイルの読み込み・書き込み時にエラーが発生しました");
			e.printStackTrace();
			return 1;
		}
		return 0;
	}


//	@Transactional
	//フォルダダウンロード処理
	public int downloadZipLogic(List<String> targetPath, HttpServletResponse response) {
		//ダウンロード時のファイル名を指定
		String fileName = "";
		//ダウンロード対象のファイルオブジェクト変数
		File dlDirectoryObj = null;
		//ダウンロード対象がフォルダであるか
		boolean isDirectory = targetPath.size() == 1;

		//フォルダダウンロードの場合
		if(isDirectory) {
			//ダウンロードするフォルダのパス
			String filePath = targetPath.get(0);
			//ダウンロード対象のファイルオブジェクトを作成
			dlDirectoryObj = new File(filePath);
			//半角スペースは+に置換されるので、半角をエンコードされない文字"\\*"に置換
			String tmpName = dlDirectoryObj.getName();
			Pattern tmpPattern = Pattern.compile(" ");
			fileName = tmpPattern.matcher(tmpName).replaceAll("\\*");
		}
		//複数ファイルダウンロードの場合
		else {
			//ダウンロードファイル名は「files_ダウンロード日時.zip」
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			String nowTime = sdf.format(new Date());
			fileName = "files_" + nowTime;
		}

		//ファイル名エンコード
		try {
			fileName = URLEncoder.encode(fileName, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			System.out.println("zip化メソッド、ファイル名エンコードでエラーが発生しました");
			e1.printStackTrace();
//			return 1;
		}
		//仮に置いた"\\*"を半角スペースに戻す
		Pattern replacePattern = Pattern.compile("\\*");
		//フォルダダウンロード時のファイル名を完全に元に戻す
		String downloadName = replacePattern.matcher(fileName).replaceAll(" ");

		//レスポンスにダウンロードファイルの情報を設定
		response.setContentType("application/octet-stream;charset=MS932");
		//ダウンロード時のファイル名を指定
		response.setHeader("Content-Disposition", "attachment;filename=" + downloadName + ".zip");
		//バイナリー形式を指定
		response.setHeader("Content-Transfer-Encoding", "binary");


		//zip化する際のパラメータ指定
		ZipParameters param = new ZipParameters();
		//パスワード無し
		param.setEncryptFiles(false);
		//ファイルを圧縮する
		param.setCompressionMethod(CompressionMethod.DEFLATE);
		//低圧縮率で速度優先→もしくはNORMALに設定
		param.setCompressionLevel(CompressionLevel.FASTEST);

		//システムの一時フォルダパスを取得
		String tmpdir = System.getProperty("java.io.tmpdir");
		Path tempPath = null;
		try {
			//一時ファイルのパスの終わりがセパレーターの場合。Windowsでの動作を想定
			if(tmpdir.endsWith(File.separator)) {
				//システムの一時フォルダにzipファイルを出力する一時フォルダ作成
				tempPath = Files.createTempDirectory(Paths.get(tmpdir), "zipoutput_tmp_");
			}
			//一時ファイルのパスの終わりがセパレーターでない場合。Linuxでの動作を想定
			else {
				//システムの一時フォルダにzipファイルを出力する一時フォルダ作成
				tempPath = Files.createTempDirectory(Paths.get(tmpdir), File.separator + "zipoutput_tmp_");
			}
		} catch(IOException e) {
			System.out.println("一時フォルダ作成に失敗しました");
			e.printStackTrace();
			return 1;
		}

		//出力先フォルダ・ファイル名を設定
		String outZipFileName = tempPath.toString() + File.separator + downloadName + ".zip";
		//ZipFileクラスインスタンス生成
		ZipFile zip = new ZipFile(outZipFileName);
		//ファイル名の文字化け対策
		zip.setCharset(Charset.forName("MS932"));
		try {
			//フォルダの場合
			if(isDirectory) {
				//指定されたフォルダをzipに圧縮
				zip.addFolder(dlDirectoryObj, param);
			}
			//複数ファイルの場合
			else {
				List<File> dlFiles = new ArrayList<>();
				for(String path: targetPath) {
					File file = new File(path);
					dlFiles.add(file);
				}
				//複数ファイルをzipに圧縮
				zip.addFiles(dlFiles, param);
			}
		} catch(ZipException ze) {
			System.out.println("zip化処理中にエラーが発生しました");
			ze.printStackTrace();
			return 2;
		}

		//httpダウンロード
		//一時フォルダに作成されたzipファイルを読み取ってダウンロード
		try(OutputStream os = response.getOutputStream();
				InputStream is = new FileInputStream(zip.getFile())) {
//			IOUtils.copy(is, os);
			IOUtils.copyLarge(is, os);
		} catch(IOException e) {
			System.out.println("zipファイルの読み出し・書き込み時にエラーが発生しました");
			e.printStackTrace();
			return 3;
		}
		//一時フォルダに作成したzipファイルを削除
		finally {
			if(new File(outZipFileName).exists()) {
				new File(outZipFileName).delete();
				new File(outZipFileName).getParentFile().delete();
			}
		}
		//ダウンロード成功時
		return 0;
	}





























	@Transactional
	public int rename(String beforeTargetPath, String destination, String newTargetName, String updateUser) {
		//移動後のファイル・フォルダのパスの設定: 「移動先フォルダパス + \ + 移動するファイル・フォルダ名」
		//リネーム後のファイル・フォルダのパスの設定: 「移動先(変更なし)フォルダパス + \ + 新しいファイル・フォルダ名」
		String newFilePath = destination + File.separator + newTargetName;
		//対象ファイル・フォルダのDBの情報取得
		FileData data = fileMapper.findFileAllInfo(beforeTargetPath);
		//データが存在しなかった場合は0を返す
		if(data == null) {
			return 0;
		}
		//対象フォルダの下層ファイル・フォルダについて、DBから取得した情報を入れるためのMap
		HashMap<String, FileData> allFileInfo = new HashMap<String, FileData>();
		//選択対象がディレクトリで、かつ移動処理の場合true
		boolean isRename = new File(beforeTargetPath).getParent().equals(destination);
		boolean isDirectory = new File(beforeTargetPath).isDirectory();
		//リネーム・移動処理の前に、DBに入れるための下層フォルダ・ファイルの情報を取得
		if(isDirectory) {
			allFileInfo = getAllInfo(new File(beforeTargetPath), allFileInfo);
		}

		//beforeTargetPathが存在しないパスの場合
		if(Objects.equals(allFileInfo, null)) {
			return 0;
		}

		//ファイル移動 or ファイル名変更
		try {
			Path from = Paths.get(beforeTargetPath);
			Path to = Paths.get(newFilePath);
			Files.move(from, to);
		} catch(IOException e) {
			e.printStackTrace();
			//移動・リネーム処理に失敗した場合、0を返す
			return 0;
		}

		//ファイル操作に成功した場合、DB情報の更新
		//対象ファイル・フォルダのFileDataクラス情報を変更する
		//現在時刻の取得
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		String nowTime = sdf.format(new Date());
		data.setFilePath(newFilePath);
		data.setFileName(newTargetName);
		//更新日時(現在時刻)セット
		data.setUpdateDate(nowTime);
		//ログインユーザ名セット
		data.setUpdateUser(updateUser);
		//SQLでUPDATE文実行
		fileMapper.updateByRename(beforeTargetPath, data);

		//フォルダ移動の処理
		if(!isRename && isDirectory) {
			//replaceStrをdestinationに置き換える
			String replaceStr = new File(beforeTargetPath).getParent();

			//DBの情報更新
			for(Iterator<Map.Entry<String, FileData>> iterator = allFileInfo.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry<String, FileData> entry = iterator.next();
				FileData fileData = entry.getValue();
				String newSubFilePath = fileData.getFilePath().replace(replaceStr, destination);
				//変更後のファイルパスセット
				fileData.setFilePath(newSubFilePath);
				//更新日時(現在時刻)セット
				fileData.setUpdateDate(nowTime);
				//ログインユーザ名セット
				fileData.setUpdateUser(updateUser);
				//SQLでUPDATE文実行
				fileMapper.updateByRename(entry.getKey(), fileData);
			}
			return 1;
		}
		//ファイル移動の処理
		else if(!isRename && !isDirectory) {
			return 2;
		}
		//フォルダリネームの処理
		else if(isRename && isDirectory) {
			//DBの情報更新
			for(Iterator<Map.Entry<String, FileData>> iterator = allFileInfo.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry<String, FileData> entry = iterator.next();
				FileData fileData = entry.getValue();
				String newSubFilePath = fileData.getFilePath().replace(beforeTargetPath, newFilePath);
				//変更後のファイルパスセット
				fileData.setFilePath(newSubFilePath);
				//更新日時(現在時刻)セット
//				fileData.setUpdateDate(nowTime);
				//ログインユーザ名セット
//				fileData.setUpdateUser(updateUser);
				//SQLでUPDATE文実行
				fileMapper.updateByRename(entry.getKey(), fileData);
			}
			return 3;
		}
		//ファイルリネームの処理
		else {
			return 4;
		}
	}


	//新規ディレクトリ作成処理
	@Transactional
	public boolean makeNewDirectory(String targetPath, String newDirectoryName, String createUser) {
		File newDirectoryPath = new File(targetPath + File.separator + newDirectoryName);
		//フォルダ作成できなかった場合、失敗を返す
		if(!newDirectoryPath.mkdir()) {
			return false;
		}

		//ファイル作成に成功した場合、DB情報の更新
		FileData data = new FileData();
		data.setFilePath(newDirectoryPath.toString());
		data.setFileName(newDirectoryName);
		data.setCreateUser(createUser);
		data.setUpdateUser(createUser);
		//現在時刻の取得
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		String nowTime = sdf.format(new Date());
		data.setCreateDate(nowTime);
		data.setUpdateDate(nowTime);
		fileMapper.insert(data);
		return true;
	}


	//削除処理
	public int delete(String deleteTargetPath) {
		File target = new File(deleteTargetPath);
		//カウントを0に初期化
		deleteFalseCount = 0;
		//処理に失敗した件数を返す
		return deleteRecursive(target);
	}

	private int deleteFalseCount = 0;
	//再帰処理用削除メソッド
	@Transactional
	public int deleteRecursive(File target) {
		if(!target.exists()) {
			deleteFalseCount++;
			return deleteFalseCount;
		}

		if(target.isDirectory()) {
			File[] fileList = target.listFiles();
			//削除対象フォルダにアクセスできない場合
			if(fileList == null) {
				deleteFalseCount++;
				return deleteFalseCount;
			}
			for(File file: fileList) {
				deleteRecursive(file);
			}
		}

		if(target.delete()) {
			fileMapper.delete(target.getAbsolutePath());
			return deleteFalseCount;
		} else {
			deleteFalseCount++;
			return deleteFalseCount;
		}
	}


	//同名ファイル・フォルダが存在するか確認
	//現状ファイル名とフォルダ名の区別をしていないため、同一階層内には同名のファイルとフォルダが同時存在しない
	public int checkSameName(String targetName, String searchDirectoryPath) {
		//調査する対象フォルダが存在しない場合2を返す
		if(!new File(searchDirectoryPath).exists()) {
			return 2;
		}
		File[] destFiles = new File(searchDirectoryPath).listFiles();
		//フォルダへのアクセスができない場合3を返す
		if(destFiles == null) {
			return 3;
		}
		//同名フォルダ・ファイルが存在する場合1、同名フォルダ・ファイルが存在しない場合0
		for(File destFile: destFiles) {
			//同名ファイルが存在する場合
			if(destFile.getName().equalsIgnoreCase(targetName)) {
				return 1;
			}
		}
		//同名ファイルが存在しない場合
		return 0;
	}


	//ファイル名変換時に、拡張子判断を行う
	public String checkExtension(String beforeTargetPath, String targetName) {
		File target = new File(beforeTargetPath);
		//ディレクトリの場合は拡張子判断無しで入力されたフォルダ名を返す
		if(target.isDirectory()) {
			return targetName;
		}

		//ファイル名の場合は拡張子がついているかを判断
		String originalFileName = target.getName();
		//元のファイル名に拡張子がついていなかった場合
		if(!originalFileName.contains(".")) {
			return targetName;
		}

		//元のファイル名に拡張子がついてる場合、元の拡張子を取得
		String originalExtension = target.getName().substring(target.getName().lastIndexOf("."));
		//拡張子を追加したファイル名
		String newFileName = targetName + originalExtension;
		//拡張子を追加したファイルパス
		String newFilePath = target.getParent() + File.separator + newFileName;
		//新しいファイル名に拡張子がない場合、元ファイルの拡張子を追加する
		if(!targetName.contains(".")) {
			//ただし、拡張子を付加したファイル名もしくはファイルパスが文字数制限を超える場合は拡張子を付けないファイル名を返す
			if(newFileName.length() > 255 || newFilePath.length() > 259) {
				return targetName;
			} else {
				return newFileName;
			}
		}
		//拡張子でなくとも"."をファイル名に含んでいれば入力されたファイル名を返す
		else {
			return targetName;
		}
	}



}
