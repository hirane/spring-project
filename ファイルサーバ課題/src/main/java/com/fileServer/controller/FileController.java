package com.fileServer.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fileServer.entity.DbUsersDetails;
import com.fileServer.entity.FileForm;
import com.fileServer.entity.HttpForm;
import com.fileServer.service.FileService;


@Controller
public class FileController {

	//serviceクラスのロジック部を参照
	@Autowired
	FileService fileService;
	//ワーキングディレクトリ(トップディレクトリ)のパスを参照する
	@Value("${WORKING_DIRECTORY}")
	private String fileServerDirectory;
	@Value("${spring.servlet.multipart.max-file-size}")
	private String maxFileSizeStr;
	@Value("${spring.servlet.multipart.max-request-size}")
	private String maxRequestSizeStr;

	@ModelAttribute("fileForm")
	public FileForm fileFormInit(@AuthenticationPrincipal DbUsersDetails loginUser) {
		FileForm fileForm = new FileForm();
		fileForm.setHomeDirectory(fileServerDirectory);
		fileForm.setUserAuth(loginUser.getUsers().getAuthority());
		fileForm.setUserName(loginUser.getUsers().getUserName());
		return fileForm;
	}

	@ModelAttribute("httpForm")
	public HttpForm httpFormInit() {
		return new HttpForm();
	}



	//ホームディレクトリ表示メソッド
	@RequestMapping("/fileView/top")
	public String moveToFileViewTop(Model model, FileForm fileForm, HttpForm httpForm) {
		fileForm.setThisDirectoryPath(fileServerDirectory);
		fileService.dispFileList(fileServerDirectory, fileForm);
		return "fileView";
	}


	//最大アップロードファイルサイズオーバーの場合
	@RequestMapping("/fileView/filesize-error")
	public String fileSizeError(Model model, RedirectAttributes ra) {
//		long maxFileSize = Long.parseLong(maxFileSizeStr.replaceAll("[^\\d]", ""));
		ra.addFlashAttribute("hasUploadError");
		ra.addFlashAttribute("hasProcessErrors", true);
		ra.addFlashAttribute("processErrMsg", "アップロード上限のファイルサイズ " + maxFileSizeStr + " を超えるファイルが含まれています。お手数ですがやり直してください。トップに戻ります。");
		return "redirect:/fileView/top";
	}


	//最大リクエストサイズオーバーの場合
	@RequestMapping("/fileView/maxsize-error")
	public String maxSizeError(Model model, RedirectAttributes ra) {
//		long maxRequestSize = Long.parseLong(maxRequestSizeStr.replaceAll("[^\\d]", ""));
		ra.addFlashAttribute("hasUploadError");
		ra.addFlashAttribute("hasProcessErrors", true);
		ra.addFlashAttribute("processErrMsg", "一度のアップロード上限 " + maxRequestSizeStr + " を超えています。お手数ですがやり直してください。トップに戻ります。");
		return "redirect:/fileView/top";
	}


	//2階層目以降のディレクトリ表示メソッド
	@RequestMapping("/fileView")
	public String moveToFileView(Model model, @Validated FileForm fileForm, BindingResult br, HttpForm httpForm) {
		File dirPath = new File(fileForm.getThisDirectoryPath());
		//現在階層のパスが空、ファイルサーバ外、もしくは指定パスが存在しない場合は現在階層パスにトップ画面のパスをセット
		if(br.hasFieldErrors("thisDirectoryPath")) {
			System.out.println("階層移動時にディレクトリ情報が取得できません");
			fileForm.setThisDirectoryPath(fileServerDirectory);
			model.addAttribute("thisDirectoryPathErr", true);
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return "fileView";
		}
		if(!dirPath.toString().contains(fileServerDirectory) || !dirPath.exists() || dirPath.isFile()) {
			System.out.println("階層移動時に不正なパスが設定されています");
			fileForm.setThisDirectoryPath(fileServerDirectory);
			model.addAttribute("hasProcessErrors", true);
			model.addAttribute("processErrMsg", "不正な移動先が設定されています。トップに戻ります。");
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return "fileView";
		}

		int hasErr = fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
		if(hasErr == 1) {
			System.out.println("指定されたフォルダへのアクセス権がありません");
			model.addAttribute("hasProcessErrors", true);
			model.addAttribute("processErrMsg", "指定されたフォルダへのアクセス権がありません。トップに戻ります。");
			fileService.dispFileList(fileServerDirectory, fileForm);
		}
		return "fileView";

	}




	//アップロード処理
	@PostMapping("/upload")
	public String upload(Model model, @Validated FileForm fileForm, BindingResult fileFormBr, @Validated HttpForm httpForm, BindingResult httpFormBr) {
		//アップロードするディレクトリが存在しない
		if(fileFormBr.hasFieldErrors("thisDirectoryPath") && httpForm.getMultipartFile() != null) {
			System.out.println("現在のディレクトリ情報が取得できませんでした。アップロード先フォルダ情報が取得できませんでした");
			model.addAttribute("thisDirectoryPathErr", true);
			fileForm.setThisDirectoryPath(fileServerDirectory);
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return "fileView";
		}

		String originalThisDirectoryPath = fileForm.getThisDirectoryPath();
		//現在の階層のパスがファイルサーバ外のディレクトリが指定されていた場合、もしくは現在の階層が存在しない場合はトップディレクトリのパスをセット
		if(originalThisDirectoryPath == null || !originalThisDirectoryPath.contains(fileServerDirectory)) {
			System.out.println("不正な情報が送信されました。アップロード処理に失敗しました。");
			model.addAttribute("hasProcessErrors", true);
			model.addAttribute("processErrMsg", "不正な情報が送信されました。アップロード処理に失敗しました。");
			fileForm.setThisDirectoryPath(fileServerDirectory);
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return "fileView";
		}

		//ファイルが選択されていなかった場合
		if(httpFormBr.hasErrors()) {
			System.err.println(httpFormBr.getFieldErrors("multipartFile"));
			model.addAttribute("multipartFileErr", true);
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return "fileView";
		}

		//アップロードするディレクトリが存在しない場合
		if(!new File(originalThisDirectoryPath).exists() || new File(originalThisDirectoryPath).isFile()) {
			System.out.println("アップロードするディレクトリが存在しません");
			model.addAttribute("hasProcessErrors", true);
			model.addAttribute("processErrMsg", "アップロードするディレクトリが存在しません。アップロード処理に失敗しました。");
			fileForm.setThisDirectoryPath(fileServerDirectory);
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return "fileView";
		}

		//処理の失敗数のカウント
		int failedNum = 0;
		//同名ファイルが存在しなかった場合
		if(Objects.equals(fileForm.getIsExistSameName(), "notExist")) {
			for(MultipartFile file: httpForm.getMultipartFile()) {
				failedNum += fileService.uploadLogic(fileForm.getThisDirectoryPath(), file, fileForm.getUserName(), null, null);
			}
		}
		//同名ファイルを上書きする場合は削除して、同名ファイルをアップロードする
		else {
			for(MultipartFile file: httpForm.getMultipartFile()) {
				failedNum += fileService.adjustLocalFile(fileForm.getThisDirectoryPath(), file, fileForm.getUserName());
			}
		}

		if(failedNum == 0) {
			System.out.println("アップロード処理が完了しました");
		} else if(failedNum == httpForm.getMultipartFile().length) {
			System.out.println("全件のアップロードに失敗しました");
			model.addAttribute("hasProcessErrors", true);
			model.addAttribute("processErrMsg", "全件のアップロードに失敗しました");
		} else {
			System.out.println(httpForm.getMultipartFile().length + "件中" + failedNum + "件のアップロード処理に失敗しました");
			model.addAttribute("hasProcessErrors", true);
			model.addAttribute("processErrMsg", httpForm.getMultipartFile().length + "件中" + failedNum + "件のアップロード処理に失敗しました");
		}
		fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
		return "fileView";
	}


	//個別ファイルダウンロード
	@RequestMapping("/download")
	public String download(Model model, @Validated FileForm fileForm, BindingResult br, HttpServletResponse response, HttpForm httpForm) {
		String originalThisDirectoryPath = fileForm.getThisDirectoryPath();
		//現在階層のパスが空、ファイルサーバ外、もしくは指定パスが存在しない場合は現在階層パスにトップ画面のパスをセット
		if(br.hasFieldErrors("thisDirectoryPath") || !originalThisDirectoryPath.contains(fileServerDirectory)
				|| !new File(originalThisDirectoryPath).exists() || new File(originalThisDirectoryPath).isFile()) {
			System.out.println("個別ファイルダウンロード時に、現在のディレクトリ情報が取得できない、もしくは不正なパスが設定されています");
			fileForm.setThisDirectoryPath(fileServerDirectory);
		}

		//ダウンロード対象のファイルパスが空の場合
		if(br.hasFieldErrors("downloadFilePath")) {
			System.out.println("ダウンロード対象のファイルが選択されていません");
			model.addAttribute("downloadFilePathErr", true);
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return "fileView";
		}

		File filePath = new File(fileForm.getDownloadFilePath());
		//ダウンロード対象がファイルサーバ外のファイル、もしくは存在しない、もしくは現在階層に存在しない、もしくはフォルダパスの場合
		if(!filePath.toString().contains(fileServerDirectory) || !filePath.exists()
				|| !Objects.equals(originalThisDirectoryPath, filePath.getParent()) || filePath.isDirectory()) {
			System.out.println("ダウンロード対象に、不正なファイルパスが指定されています。");
			model.addAttribute("hasProcessErrors", true);
			model.addAttribute("processErrMsg", "ダウンロード対象に、不正なファイルパスが指定されています。");
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return "fileView";
		}

		/*
		new Thread(() -> {
			fileService.downloadLogic(fileForm.getDownloadFilePath(), response);
		}).start();
		 */
		if(fileService.downloadLogic(fileForm.getDownloadFilePath(), response) == 0) {
			System.out.println("個別ファイルダウンロードが完了しました");
		} else {
			System.out.println("個別ファイルダウンロードに失敗しました");
//			model.addAttribute("hasProcessErrors", true);
//			model.addAttribute("processErrMsg", "ダウンロードに失敗しました");
//			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
//			return "fileView";
		}
		//Responseを直接指定しているため、画面遷移先はnullを指定
		return null;
	}


	//複数ファイルダウンロード
	@RequestMapping("/downloadMultiFiles")
	public String downloadMultiFiles(Model model, @Validated FileForm fileForm, BindingResult br, HttpServletResponse response, HttpForm httpForm) {
		String originalThisDirectoryPath = fileForm.getThisDirectoryPath();
		//現在階層のパスが空、ファイルサーバ外、もしくは指定パスが存在しない場合は現在階層パスにトップ画面のパスをセット
		if(br.hasFieldErrors("thisDirectoryPath") || !originalThisDirectoryPath.contains(fileServerDirectory)
				|| !new File(originalThisDirectoryPath).exists() || new File(originalThisDirectoryPath).isFile()) {
			System.out.println("複数ファイルダウンロード時に、現在のディレクトリ情報が取得できない、もしくは不正なパスが設定されています");
			fileForm.setThisDirectoryPath(fileServerDirectory);
		}

		//ファイルが選択されていない場合(listのサイズが0)
		if(br.hasFieldErrors("downloadFilePathList")) {
			System.out.println("ダウンロード対象のファイルが1件も選択されていません");
			model.addAttribute("downloadFilePathListErr", true);
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return "fileView";
		}

		//選択された複数ファイルに不正な情報がないか確認
		//不正な情報が含まれればtrueに
		boolean includeIllegal = false;
		//選択されたファイルの件数
		int totalFilesNum = fileForm.getDownloadFilePathList().size();
		//エラーのあったファイル数カウント
		int errCount = 0;
		for(int i = 0; i < totalFilesNum; i++) {
			File path = new File(fileForm.getDownloadFilePathList().get(i));
			//個別のファイルで、ファイルパスが空、ファイルサーバ外に存在する、存在しないファイル、現在階層に存在しない、もしくはフォルダのパスが含まれる場合
			if(br.hasFieldErrors("downloadFilePathList[" + i + "]") || !path.toString().contains(fileServerDirectory) || !path.exists()
					|| !Objects.equals(originalThisDirectoryPath, path.getParent()) || path.isDirectory()) {
				includeIllegal = true;
				errCount++;
			}
		}
		if(includeIllegal) {
			System.out.println(totalFilesNum + "件中" + errCount + "件、ダウンロード対象にパスが不正なファイルが指定されています。");
			model.addAttribute("hasProcessErrors", true);
			model.addAttribute("processErrMsg", totalFilesNum + "件中" + errCount + "件のダウンロード対象にパスが不正なファイルが指定されています。");
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return "fileView";
		}

		//選択されたファイルが1ファイルの場合、個別ダウンロードロジックへ
		if(totalFilesNum == 1) {
			if(fileService.downloadLogic(fileForm.getDownloadFilePath(), response) == 0) {
				System.out.println("個別ファイルダウンロードが完了しました");
			} else {
				System.out.println("個別ファイルダウンロードに失敗しました");
			}
		} else {
			//2ファイル以上の場合はzip圧縮してダウンロードするロジックへ
			if(fileService.downloadZipLogic(fileForm.getDownloadFilePathList(), response) == 0) {
				System.out.println("zip化した複数ファイルダウンロードが完了しました");
			} else {
				System.out.println("zip化した複数ファイルダウンロードに失敗しました");
			}
		}
		return null;
	}


	//ディレクトリダウンロード
	@RequestMapping("/downloadDir")
	public String downloadDirectory(Model model, @Validated FileForm fileForm, BindingResult br, HttpServletResponse response, HttpForm httpForm) {
		String originalThisDirectoryPath = fileForm.getThisDirectoryPath();
		//現在階層のパスが空、ファイルサーバ外、もしくは指定パスが存在しない場合は現在階層パスにトップ画面のパスをセット
		if(br.hasFieldErrors("thisDirectoryPath") || !originalThisDirectoryPath.contains(fileServerDirectory)
				|| !new File(originalThisDirectoryPath).exists() || new File(originalThisDirectoryPath).isFile()) {
			System.out.println("フォルダダウンロード時に、現在のディレクトリ情報が取得できない、もしくは不正なパスが設定されています");
			fileForm.setThisDirectoryPath(fileServerDirectory);
		}

		//ダウンロード対象のフォルダパスが空の場合
		if(br.hasFieldErrors("downloadDirectoryPath")) {
			System.out.println("ダウンロード対象のファイルが選択されていません");
			model.addAttribute("downloadDirectoryPathErr", true);
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return "fileView";
		}

		File dirPath = new File(fileForm.getDownloadDirectoryPath());
		//ダウンロード対象がファイルサーバ外のフォルダ、もしくは存在しない、もしくは現在階層に存在しない、もしくはファイルパスの場合
		if(!dirPath.toString().contains(fileServerDirectory) || !dirPath.exists()
				|| !Objects.equals(originalThisDirectoryPath, dirPath.getParent()) || dirPath.isFile()) {
			System.out.println("ダウンロード対象に、不正なフォルダパスが指定されています。");
			model.addAttribute("hasProcessErrors", true);
			model.addAttribute("processErrMsg", "ダウンロード対象に、不正なフォルダパスが指定されています。");
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return "fileView";
		}

		//List型の引数に合わせるため要素数1のリスト作成
		List<String> downloadDirectoryPath = new ArrayList<String>(Arrays.asList(fileForm.getDownloadDirectoryPath()));

		//zip圧縮してダウンロード
		//		new Thread(() -> {
		//			fileService.downloadZipLogic(downloadDirectoryPath, response);
		//		}).start();
		if(fileService.downloadZipLogic(downloadDirectoryPath, response) == 0) {
			System.out.println("zip化したフォルダダウンロードが完了しました");
		} else {
			System.out.println("zip化したフォルダダウンロードに失敗しました");
		}
		return null;
	}


	//新規ディレクトリ作成
	@PostMapping("/makeDirectory")
	public String makeDirectory(Model model, @Validated FileForm fileForm, BindingResult br, HttpForm httpForm) {
		//フォルダ作成するディレクトリが存在しない
		if(br.hasFieldErrors("thisDirectoryPath")) {
			System.out.println("現在のディレクトリ情報が取得できませんでした。作成先フォルダが存在しません");
			model.addAttribute("thisDirectoryPathErr", true);
			fileForm.setThisDirectoryPath(fileServerDirectory);
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return "fileView";
		}

		String originalThisDirectoryPath = fileForm.getThisDirectoryPath();
		//現在の階層のパスがファイルサーバ外のディレクトリが指定されていた場合、もしくは現在の階層が存在しない場合はトップディレクトリのパスをセット
		if(!originalThisDirectoryPath.contains(fileServerDirectory) || !new File(originalThisDirectoryPath).exists()
				|| new File(originalThisDirectoryPath).isFile()) {
			System.out.println("不正な情報が送信されました。フォルダ作成に失敗しました。");
			model.addAttribute("hasProcessErrors", true);
			model.addAttribute("processErrMsg", "不正な情報が送信されました。フォルダ作成に失敗しました。");
			fileForm.setThisDirectoryPath(fileServerDirectory);
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return "fileView";
		}

		if(br.hasFieldErrors("newDirectoryName") || br.hasFieldErrors("newDirectoryPath")) {
			System.out.println("新規フォルダ名で異常値が入力されました");
			model.addAttribute("isRedisplayMkdir", true);
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return "fileView";
		}

		//入力されたフォルダ名の前後にスペースがあれば削除
		String newDirectoryName = fileForm.getNewDirectoryName().strip();

		//同名ファイルの存在確認
		int sameNameCheck = fileService.checkSameName(newDirectoryName, originalThisDirectoryPath);
		if(sameNameCheck == 1) {
			System.out.println("その名前は既に使用されています");
			model.addAttribute("isRedisplayMkdir", true);
			model.addAttribute("hasMkdirErr", true);
			model.addAttribute("errMsg", "その名前は既に使用されています");
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return "fileView";
		} else if(sameNameCheck == 2) {
			System.out.println("新規フォルダを作成するディレクトリが存在しません");
			model.addAttribute("hasProcessErrors", true);
			model.addAttribute("processErrMsg", "新規フォルダを作成するディレクトリが存在しません");
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return "fileView";
		} else if(sameNameCheck == 3) {
			System.out.println("対象フォルダへのアクセスが拒否されています");
			model.addAttribute("hasProcessErrors", true);
			model.addAttribute("processErrMsg", "対象フォルダへのアクセスが拒否されています");
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return "fileView";
		}

		//currentPathに名前newDirectoryNameのディレクトリ作成処理
		if(fileService.makeNewDirectory(fileForm.getThisDirectoryPath(), newDirectoryName, fileForm.getUserName())) {
			System.out.println("フォルダが作成されました");
		} else {
			System.out.println("フォルダ作成に失敗しました");
			model.addAttribute("hasProcessErrors", true);
			model.addAttribute("processErrMsg", "フォルダ作成に失敗しました");
		}
		fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
		return "fileView";
	}


	//名称変更
	@PostMapping("/changeName")
	public String changeName(Model model, @Validated FileForm fileForm, BindingResult br, HttpForm httpForm) {

		if(br.hasFieldErrors("thisDirectoryPath")) {
			System.out.println("現在のディレクトリ情報が取得できませんでした");
			model.addAttribute("thisDirectoryPathErr", true);
			fileForm.setThisDirectoryPath(fileServerDirectory);
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return "fileView";
		}

		String originalThisDirectoryPath = fileForm.getThisDirectoryPath();
		//現在の階層のパスがファイルサーバ外のディレクトリが指定されていた場合、もしくは現在階層のパスが存在しない場合はトップディレクトリのパスをセット
		if(!originalThisDirectoryPath.contains(fileServerDirectory) || !new File(originalThisDirectoryPath).exists()
				|| new File(originalThisDirectoryPath).isFile()) {
			System.out.println("不正な情報が送信されました。名称変更処理に失敗しました。");
			model.addAttribute("hasProcessErrors", true);
			model.addAttribute("processErrMsg", "不正な情報が送信されました。名称変更処理に失敗しました。");
			fileForm.setThisDirectoryPath(fileServerDirectory);
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return "fileView";
		}

		//リネーム対象のパスが異常値の場合
		if(br.hasFieldErrors("renameTargetPath")) {
			System.out.println("名称変更する対象が選択されていないか既に存在しません");
			model.addAttribute("renameTargetPathErr", true);
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return "fileView";
		}

		//現在の階層情報が不正な場合は現在階層にトップディレクトリのパスをセット
		if(!originalThisDirectoryPath.equals(new File(fileForm.getRenameTargetPath()).getParent()) ||
				!fileForm.getRenameTargetPath().contains(fileServerDirectory) || !new File(fileForm.getRenameTargetPath()).exists()) {
			System.out.println("不正な情報が送信されました。名称変更処理に失敗しました。");
			model.addAttribute("hasProcessErrors", true);
			model.addAttribute("processErrMsg", "不正な情報が送信されました。名称変更処理に失敗しました。");
			fileForm.setThisDirectoryPath(fileServerDirectory);
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return "fileView";
		}

		if(br.hasFieldErrors("afterName") || br.hasFieldErrors("afterPath")) {
			System.out.println("リネーム後の名称で異常値が入力されました");
			model.addAttribute("isRedisplayRename", true);
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return "fileView";
		}

		//入力されたフォルダ名・ファイル名の前後にスペースがあれば削除
		String afterName = fileForm.getAfterName().strip();

		//拡張子判断→入力されたファイル名で拡張子が消えていたら付与する
		afterName = fileService.checkExtension(fileForm.getRenameTargetPath(), afterName);

		//同名ファイルの存在確認
		int sameNameCheck = fileService.checkSameName(afterName, new File(fileForm.getRenameTargetPath()).getParent());
		if(sameNameCheck == 1) {
			System.out.println("その名前は既に使用されています");
			model.addAttribute("isRedisplayRename", true);
			model.addAttribute("hasRenameErr", true);
			model.addAttribute("errMsg", "その名前は既に使用されています");
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return "fileView";
		} else if(sameNameCheck == 2) {
			System.out.println("リネームする対象が存在するディレクトリが存在しません");
			model.addAttribute("hasProcessErrors", true);
			model.addAttribute("processErrMsg", "リネームする対象が存在するディレクトリが存在しません");
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return "fileView";
		} else if(sameNameCheck == 3) {
			System.out.println("リネーム対象フォルダへのアクセスが拒否されています");
			model.addAttribute("hasProcessErrors", true);
			model.addAttribute("processErrMsg", "リネーム対象フォルダへのアクセスが拒否されています");
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return "fileView";
		}

		//リネーム処理
		int result = fileService.rename(fileForm.getRenameTargetPath(),
				new File(fileForm.getRenameTargetPath()).getParent(), afterName, fileForm.getUserName());
		if(result == 3) {
			//フォルダリネーム処理成功
			System.out.println("フォルダ名変更が完了しました");
		} else if(result == 4) {
			//ファイルリネーム処理成功
			System.out.println("ファイル名変更が完了しました");
		} else {
			//名称変更時にエラー
			System.out.println("ファイル操作エラー。名称変更に失敗しました");
			model.addAttribute("hasProcessErrors", true);
			model.addAttribute("processErrMsg", "名称変更に失敗しました");
		}
		fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
		return "fileView";
	}



	//ファイル・フォルダ移動
	//departure: 移動したいファイル・フォルダのフルパス
	//renameByMoveCurrentPath: 移動元ファイルのあるディレクトリパス
	//destination: 移動したいディレクトリのパス
	@PostMapping("/moveDirectory")
	public String moveDirectory(Model model, @Validated FileForm fileForm, BindingResult br, RedirectAttributes redirectAttributes, HttpForm httpForm) {

		if(br.hasFieldErrors("thisDirectoryPath")) {
			System.out.println("現在のディレクトリ情報が取得できませんでした。移動元のフォルダが存在しません");
			model.addAttribute("thisDirectoryPathErr", true);
			fileForm.setThisDirectoryPath(fileServerDirectory);
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return "fileView";
		}

		String originalThisDirectoryPath = fileForm.getThisDirectoryPath();
		//現在の階層のパスがファイルサーバ外のディレクトリが指定されていた場合、もしくは現在階層のパスが存在しない場合はトップディレクトリのパスをセット
		if(!originalThisDirectoryPath.contains(fileServerDirectory) || !new File(originalThisDirectoryPath).exists()
				|| new File(originalThisDirectoryPath).isFile()) {
			System.out.println("不正な情報が送信されました。移動処理に失敗しました。");
			model.addAttribute("hasProcessErrors", true);
			model.addAttribute("processErrMsg", "不正な情報が送信されました。移動処理に失敗しました。");
			fileForm.setThisDirectoryPath(fileServerDirectory);
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return "fileView";
		}

		//移動対象情報がない場合
		if(br.hasFieldErrors("departure")) {
			System.out.println("移動対象が選択されていません");
			model.addAttribute("departureErr", true);
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return "fileView";
		}

		//移動先ディレクトリ情報がない場合
		if(br.hasFieldErrors("destination")) {
			System.out.println("移動先のフォルダが選択されていません。");
			model.addAttribute("isRedisplayMove", true);
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return "fileView";
		}

		//移動先のパスが259文字制限オーバーの場合
		if(br.hasFieldErrors("destinationFilePath")) {
			System.out.println("移動後のパスが長すぎるか、階層が深すぎます。");
			model.addAttribute("isRedisplayMove", true);
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return "fileView";
		}

		//選択対象もしくは移動先がファイルサーバ外だった場合はエラーを返す
		if(!fileForm.getDeparture().contains(fileServerDirectory) || !fileForm.getDestination().contains(fileServerDirectory)) {
			System.out.println("移動対象もしくは移動先情報が不正です");
			model.addAttribute("hasProcessErrors", true);
			model.addAttribute("processErrMsg", "移動対象もしくは移動先情報が不正です。移動処理に失敗しました。");
			fileForm.setThisDirectoryPath(fileServerDirectory);
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return "fileView";
		}

		File departureFile = new File(fileForm.getDeparture());
		File destinationFile = new File(fileForm.getDestination());

		//現在の階層情報が不正な場合は現在階層にトップディレクトリのパスをセット
		if(!originalThisDirectoryPath.equals(departureFile.getParent())) {
			fileForm.setThisDirectoryPath(fileServerDirectory);
		}

		//移動対象が実際に存在しているか
		if(!departureFile.exists()) {
			//移動対象が存在しない
			System.out.println("移動対象が存在しません");
			model.addAttribute("hasProcessErrors", true);
			model.addAttribute("processErrMsg", "移動対象が存在しません");
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return "fileView";
		}

		//移動先ディレクトリが実際に存在しているか
		if(!destinationFile.exists() || destinationFile.isFile()) {
			//移動先ディレクトリが存在しない
			System.out.println("移動先ディレクトリが存在しません");
			model.addAttribute("isRedisplayMove", true);
			model.addAttribute("hasMoveErr", true);
			model.addAttribute("errMsg", "移動先ディレクトリが存在しません");
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return "fileView";
		}

		//移動の成否確認
		//フォルダの場合
		if(departureFile.isDirectory()) {
			//既にフォルダが存在している階層が同じ場合
			if(departureFile.getParent().equals(fileForm.getDestination())) {
				System.out.println("フォルダは同一階層から移動されません");
				model.addAttribute("isRedisplayMove", true);
				model.addAttribute("hasMoveErr", true);
				model.addAttribute("errMsg", "フォルダは同一階層から移動されません");
				fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
				return "fileView";
			}
			//移動元フォルダを、移動元フォルダの下位階層に移動しようとしたとき
			else if(fileForm.getDestination().contains(fileForm.getDeparture())) {
				System.out.println("サブフォルダには移動できません");
				model.addAttribute("isRedisplayMove", true);
				model.addAttribute("hasMoveErr", true);
				model.addAttribute("errMsg", "サブフォルダには移動できません");
				fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
				return "fileView";
			}
			//ファイルの場合
		} else {
			//既にファイルが存在している階層が同じ場合
			if(departureFile.getParent().equals(fileForm.getDestination())) {
				System.out.println("ファイルは同一階層から移動されません");
				model.addAttribute("isRedisplayMove", true);
				model.addAttribute("hasMoveErr", true);
				model.addAttribute("errMsg", "ファイルは同一階層から移動されません");
				fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
				return "fileView";
			}
		}

		//同名ファイルの存在確認
		int sameNameCheck = fileService.checkSameName(departureFile.getName(), fileForm.getDestination());
		if(sameNameCheck == 1) {
			System.out.println("その名前は既に使用されています");
			model.addAttribute("isRedisplayMove", true);
			model.addAttribute("hasMoveErr", true);
			model.addAttribute("errMsg", "移動先でその名前は既に使用されています");
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return "fileView";
		} else if(sameNameCheck == 3) {
			System.out.println("移動先ディレクトリにアクセスが拒否されています");
			model.addAttribute("hasProcessErrors", true);
			model.addAttribute("processErrMsg", "移動先ディレクトリにアクセスが拒否されています");
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return "fileView";
		}

		int result = fileService.rename(fileForm.getDeparture(), fileForm.getDestination(), departureFile.getName(), fileForm.getUserName());
		if(result == 1) {
			//フォルダ移動処理成功
			System.out.println("フォルダ移動が完了しました");
		} else if(result == 2) {
			//ファイル移動処理成功
			System.out.println("ファイル移動が完了しました");
		} else {
			System.out.println("ファイル操作エラー。移動に失敗しました");
			model.addAttribute("hasProcessErrors", true);
			model.addAttribute("processErrMsg", "移動に失敗しました");
		}
		fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
		return "fileView";
	}


	//ファイル・フォルダ削除
	@PostMapping("/deleteFile")
	public String deleteFile(Model model, @Validated FileForm fileForm, BindingResult br, HttpForm httpForm) {
		if(br.hasFieldErrors("thisDirectoryPath")) {
			System.out.println("現在のディレクトリ情報が取得できませんでした");
			model.addAttribute("thisDirectoryPathErr", true);
			fileForm.setThisDirectoryPath(fileServerDirectory);
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return "fileView";
		}

		String originalThisDirectoryPath = fileForm.getThisDirectoryPath();
		//現在の階層のパスがファイルサーバ外のディレクトリが指定されていた場合、もしくは現在階層のパスが存在しない場合はトップディレクトリのパスをセット
		if(!originalThisDirectoryPath.contains(fileServerDirectory) || !new File(originalThisDirectoryPath).exists()
				|| new File(originalThisDirectoryPath).isFile()) {
			fileForm.setThisDirectoryPath(fileServerDirectory);
		}

		if(br.hasFieldErrors("deleteTargetPath")) {
			System.out.println("削除対象が選択されていません");
			model.addAttribute("deleteTargetPathErr", true);
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return "fileView";
		}

		File target = new File(fileForm.getDeleteTargetPath());

		//削除対象のパスがファイルサーバ外のディレクトリが指定されていた場合、もしくは現在の階層のパスが削除対象のあるフォルダと異なる場合
		if(!fileForm.getDeleteTargetPath().contains(fileServerDirectory) || !target.getParent().equals(originalThisDirectoryPath)) {
			System.out.println("削除処理に失敗しました");
			model.addAttribute("hasProcessErrors", true);
			model.addAttribute("processErrMsg", "不正な情報が送信されました。削除処理に失敗しました。");
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return "fileView";
		}

		//String currentPath = target.getParent();
		//削除対象が存在するか
		if(!target.exists()) {
			System.out.println("削除対象が既に存在しません");
			model.addAttribute("hasProcessErrors", true);
			model.addAttribute("processErrMsg", "削除対象が既に存在しません");
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return "fileView";
		}

		//削除処理に失敗した件数をカウント
		int failedNum = fileService.delete(fileForm.getDeleteTargetPath());
		//削除処理
		if(failedNum == 0) {
			System.out.println("削除処理に成功しました");
		} else {
			System.out.println(failedNum + "件の削除処理に失敗しました");
			model.addAttribute("hasProcessErrors", true);
			model.addAttribute("processErrMsg", failedNum + "件の削除処理に失敗しました");
		}
		fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
		return "fileView";
	}


}
