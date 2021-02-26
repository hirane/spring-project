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

import com.fileServer.config.Const;
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
		return Const.FILEVIEW;
	}


	//最大アップロードファイルサイズオーバーの場合
	@RequestMapping("/fileView/filesize-error")
	public String fileSizeError(Model model, RedirectAttributes redirectAttributes) {
//		long maxFileSize = Long.parseLong(maxFileSizeStr.replaceAll("[^\\d]", ""));
		redirectAttributes.addFlashAttribute("hasUploadError");
		redirectAttributes.addFlashAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
		redirectAttributes.addFlashAttribute(Const.PROCESS_ERR_MSG, "アップロード上限のファイルサイズ " + maxFileSizeStr + " を超えるファイルが含まれています。お手数ですがやり直してください。トップに戻ります。");
		return Const.REDIRECT_FILEVIEW_TOP;
	}


	//最大リクエストサイズオーバーの場合
	@RequestMapping("/fileView/maxsize-error")
	public String maxSizeError(Model model, RedirectAttributes redirectAttributes) {
//		long maxRequestSize = Long.parseLong(maxRequestSizeStr.replaceAll("[^\\d]", ""));
		redirectAttributes.addFlashAttribute("hasUploadError");
		redirectAttributes.addFlashAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
		redirectAttributes.addFlashAttribute(Const.PROCESS_ERR_MSG, "一度のアップロード上限 " + maxRequestSizeStr + " を超えています。お手数ですがやり直してください。トップに戻ります。");
		return Const.REDIRECT_FILEVIEW_TOP;
	}


	//2階層目以降のディレクトリ表示メソッド
	@RequestMapping("/fileView")
	public String moveToFileView(Model model, @Validated FileForm fileForm, BindingResult br, HttpForm httpForm) {
		File dirPath = new File(fileForm.getThisDirectoryPath());
		//現在階層のパスが空、ファイルサーバ外、もしくは指定パスが存在しない場合は現在階層パスにトップ画面のパスをセット
		if(br.hasFieldErrors(Const.THIS_DIRECTORY_PATH)) {
			fileForm.setThisDirectoryPath(fileServerDirectory);
			model.addAttribute("thisDirectoryPathErr", true);
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return Const.FILEVIEW;
		}
		if(!dirPath.toString().contains(fileServerDirectory) || !dirPath.exists() || dirPath.isFile()) {
			fileForm.setThisDirectoryPath(fileServerDirectory);
			model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
			model.addAttribute(Const.PROCESS_ERR_MSG, "不正な移動先が設定されています。トップに戻ります。");
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return Const.FILEVIEW;
		}

		int hasErr = fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
		if(hasErr == Const.ACCESS_DENIED_ERR) {
			model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
			model.addAttribute(Const.PROCESS_ERR_MSG, "指定されたフォルダへのアクセス権がありません。トップに戻ります。");
			fileService.dispFileList(fileServerDirectory, fileForm);
		}
		return Const.FILEVIEW;

	}




	//アップロード処理
	@PostMapping("/upload")
	public String upload(Model model, @Validated FileForm fileForm, BindingResult fileFormBr, @Validated HttpForm httpForm, BindingResult httpFormBr) {
		//アップロードするディレクトリが存在しない
		if(fileFormBr.hasFieldErrors(Const.THIS_DIRECTORY_PATH) && httpForm.getMultipartFile() != null) {
			model.addAttribute("thisDirectoryPathErr", true);
			fileForm.setThisDirectoryPath(fileServerDirectory);
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return Const.FILEVIEW;
		}

		String originalThisDirectoryPath = fileForm.getThisDirectoryPath();
		//現在の階層のパスがファイルサーバ外のディレクトリが指定されていた場合、もしくは現在の階層が存在しない場合はトップディレクトリのパスをセット
		if(originalThisDirectoryPath == null || !originalThisDirectoryPath.contains(fileServerDirectory)) {
			model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
			model.addAttribute(Const.PROCESS_ERR_MSG, "不正な情報が送信されました。アップロード処理に失敗しました。");
			fileForm.setThisDirectoryPath(fileServerDirectory);
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return Const.FILEVIEW;
		}

		//ファイルが選択されていなかった場合
		if(httpFormBr.hasErrors()) {
			model.addAttribute("multipartFileErr", true);
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return Const.FILEVIEW;
		}

		//アップロードするディレクトリが存在しない場合
		if(!new File(originalThisDirectoryPath).exists() || new File(originalThisDirectoryPath).isFile()) {
			model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
			model.addAttribute(Const.PROCESS_ERR_MSG, "アップロードするディレクトリが存在しません。アップロード処理に失敗しました。");
			fileForm.setThisDirectoryPath(fileServerDirectory);
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return Const.FILEVIEW;
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

		if(failedNum == httpForm.getMultipartFile().length) {
			model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
			model.addAttribute(Const.PROCESS_ERR_MSG, "全件のアップロードに失敗しました。");
		} else if(failedNum != 0) {
			model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
			model.addAttribute(Const.PROCESS_ERR_MSG, httpForm.getMultipartFile().length + "件中" + failedNum + "件のアップロード処理に失敗しました。");
		}
		fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
		return Const.FILEVIEW;
	}


	//個別ファイルダウンロード
	@RequestMapping("/download")
	public String download(Model model, @Validated FileForm fileForm, BindingResult br, HttpServletResponse response, HttpForm httpForm) {
		String originalThisDirectoryPath = fileForm.getThisDirectoryPath();
		//現在階層のパスが空、ファイルサーバ外、もしくは指定パスが存在しない場合は現在階層パスにトップ画面のパスをセット
		if(br.hasFieldErrors(Const.THIS_DIRECTORY_PATH) || !originalThisDirectoryPath.contains(fileServerDirectory)
				|| !new File(originalThisDirectoryPath).exists() || new File(originalThisDirectoryPath).isFile()) {
			fileForm.setThisDirectoryPath(fileServerDirectory);
		}

		//ダウンロード対象のファイルパスが空の場合
		if(br.hasFieldErrors("downloadFilePath")) {
			model.addAttribute("downloadFilePathErr", true);
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return Const.FILEVIEW;
		}

		File filePath = new File(fileForm.getDownloadFilePath());
		//ダウンロード対象がファイルサーバ外のファイル、もしくは存在しない、もしくは現在階層に存在しない、もしくはフォルダパスの場合
		if(!filePath.toString().contains(fileServerDirectory) || !filePath.exists()
				|| !Objects.equals(originalThisDirectoryPath, filePath.getParent()) || filePath.isDirectory()) {
			model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
			model.addAttribute(Const.PROCESS_ERR_MSG, "ダウンロード対象に、不正なファイルパスが指定されています。");
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return Const.FILEVIEW;
		}

		/*
		new Thread(() -> {
			fileService.downloadLogic(fileForm.getDownloadFilePath(), response);
		}).start();
		 */
//		if(fileService.downloadLogic(fileForm.getDownloadFilePath(), response) == Const.PROCESS_FAILED) {
//			model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
//			model.addAttribute(Const.PROCESS_ERR_MSG, "ダウンロードに失敗しました。");
//			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
//			return Const.FILEVIEW;
//		}
		fileService.downloadLogic(fileForm.getDownloadFilePath(), response);
		//Responseを直接指定しているため、画面遷移先はnullを指定
		return null;
	}


	//複数ファイルダウンロード
	@RequestMapping("/downloadMultiFiles")
	public String downloadMultiFiles(Model model, @Validated FileForm fileForm, BindingResult br, HttpServletResponse response, HttpForm httpForm) {
		String originalThisDirectoryPath = fileForm.getThisDirectoryPath();
		//現在階層のパスが空、ファイルサーバ外、もしくは指定パスが存在しない場合は現在階層パスにトップ画面のパスをセット
		if(br.hasFieldErrors(Const.THIS_DIRECTORY_PATH) || !originalThisDirectoryPath.contains(fileServerDirectory)
				|| !new File(originalThisDirectoryPath).exists() || new File(originalThisDirectoryPath).isFile()) {
			fileForm.setThisDirectoryPath(fileServerDirectory);
		}

		//ファイルが選択されていない場合(listのサイズが0)
		if(br.hasFieldErrors("downloadFilePathList")) {
			model.addAttribute("downloadFilePathListErr", true);
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return Const.FILEVIEW;
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
			model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
			model.addAttribute(Const.PROCESS_ERR_MSG, totalFilesNum + "件中" + errCount + "件のダウンロード対象にパスが不正なファイルが指定されています。");
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return Const.FILEVIEW;
		}

		//選択されたファイルが1ファイルの場合、個別ダウンロードロジックへ
		if(totalFilesNum == 1) {
			fileService.downloadLogic(fileForm.getDownloadFilePath(), response);
//			if(fileService.downloadLogic(fileForm.getDownloadFilePath(), response) == Const.PROCESS_FAILED) {
//				model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
//				model.addAttribute(Const.PROCESS_ERR_MSG, "個別ファイルダウンロードに失敗しました。");
//				fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
//				return Const.FILEVIEW;
//			}
		} else {
			//2ファイル以上の場合はzip圧縮してダウンロードするロジックへ
			fileService.downloadZipLogic(fileForm.getDownloadFilePathList(), response);
//			if(fileService.downloadZipLogic(fileForm.getDownloadFilePathList(), response) == Const.PROCESS_FAILED) {
//				model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
//				model.addAttribute(Const.PROCESS_ERR_MSG, "zip化した複数ファイルダウンロードに失敗しました。");
//				fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
//				return Const.FILEVIEW;
//			}
		}
		return null;
	}


	//ディレクトリダウンロード
	@RequestMapping("/downloadDir")
	public String downloadDirectory(Model model, @Validated FileForm fileForm, BindingResult br, HttpServletResponse response, HttpForm httpForm) {
		String originalThisDirectoryPath = fileForm.getThisDirectoryPath();
		//現在階層のパスが空、ファイルサーバ外、もしくは指定パスが存在しない場合は現在階層パスにトップ画面のパスをセット
		if(br.hasFieldErrors(Const.THIS_DIRECTORY_PATH) || !originalThisDirectoryPath.contains(fileServerDirectory)
				|| !new File(originalThisDirectoryPath).exists() || new File(originalThisDirectoryPath).isFile()) {
			fileForm.setThisDirectoryPath(fileServerDirectory);
		}

		//ダウンロード対象のフォルダパスが空の場合
		if(br.hasFieldErrors("downloadDirectoryPath")) {
			model.addAttribute("downloadDirectoryPathErr", true);
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return Const.FILEVIEW;
		}

		File dirPath = new File(fileForm.getDownloadDirectoryPath());
		//ダウンロード対象がファイルサーバ外のフォルダ、もしくは存在しない、もしくは現在階層に存在しない、もしくはファイルパスの場合
		if(!dirPath.toString().contains(fileServerDirectory) || !dirPath.exists()
				|| !Objects.equals(originalThisDirectoryPath, dirPath.getParent()) || dirPath.isFile()) {
			model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
			model.addAttribute(Const.PROCESS_ERR_MSG, "ダウンロード対象に、不正なフォルダパスが指定されています。");
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return Const.FILEVIEW;
		}

		//List型の引数に合わせるため要素数1のリスト作成
		List<String> downloadDirectoryPath = new ArrayList<String>(Arrays.asList(fileForm.getDownloadDirectoryPath()));

		//zip圧縮してダウンロード
		//		new Thread(() -> {
		//			fileService.downloadZipLogic(downloadDirectoryPath, response);
		//		}).start();
		fileService.downloadZipLogic(downloadDirectoryPath, response);
		return null;
	}


	//新規ディレクトリ作成
	@PostMapping("/makeDirectory")
	public String makeDirectory(Model model, @Validated FileForm fileForm, BindingResult br, HttpForm httpForm) {
		//フォルダ作成するディレクトリが存在しない
		if(br.hasFieldErrors(Const.THIS_DIRECTORY_PATH)) {
			model.addAttribute("thisDirectoryPathErr", true);
			fileForm.setThisDirectoryPath(fileServerDirectory);
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return Const.FILEVIEW;
		}

		String originalThisDirectoryPath = fileForm.getThisDirectoryPath();
		//現在の階層のパスがファイルサーバ外のディレクトリが指定されていた場合、もしくは現在の階層が存在しない場合はトップディレクトリのパスをセット
		if(!originalThisDirectoryPath.contains(fileServerDirectory) || !new File(originalThisDirectoryPath).exists()
				|| new File(originalThisDirectoryPath).isFile()) {
			model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
			model.addAttribute(Const.PROCESS_ERR_MSG, "不正な情報が送信されました。フォルダ作成に失敗しました。");
			fileForm.setThisDirectoryPath(fileServerDirectory);
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return Const.FILEVIEW;
		}

		if(br.hasFieldErrors("newDirectoryName") || br.hasFieldErrors("newDirectoryPath")) {
			model.addAttribute("isRedisplayMkdir", true);
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return Const.FILEVIEW;
		}

		//入力されたフォルダ名の前後にスペースがあれば削除
		String newDirectoryName = fileForm.getNewDirectoryName().strip();

		//同名ファイルの存在確認
		int sameNameCheck = fileService.checkSameName(newDirectoryName, originalThisDirectoryPath);
		if(sameNameCheck == Const.CHK_SAMENAME_EXIST) {
			model.addAttribute("isRedisplayMkdir", true);
			model.addAttribute("hasMkdirErr", true);
			model.addAttribute("errMsg", "その名前は既に使用されています。");
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return Const.FILEVIEW;
		} else if(sameNameCheck == Const.CHK_SAMENAME_TARGET_NOT_EXIST) {
			model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
			model.addAttribute(Const.PROCESS_ERR_MSG, "新規フォルダを作成するディレクトリが存在しません。");
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return Const.FILEVIEW;
		} else if(sameNameCheck == Const.ACCESS_DENIED_ERR) {
			model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
			model.addAttribute(Const.PROCESS_ERR_MSG, "対象フォルダへのアクセスが拒否されています。");
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return Const.FILEVIEW;
		}

		//currentPathに名前newDirectoryNameのディレクトリ作成処理
		if(fileService.makeNewDirectory(fileForm.getThisDirectoryPath(), newDirectoryName, fileForm.getUserName()) == Const.PROCESS_FAILED) {
			model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
			model.addAttribute(Const.PROCESS_ERR_MSG, "フォルダ作成に失敗しました。");
		}
		fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
		return Const.FILEVIEW;
	}


	//名称変更
	@PostMapping("/changeName")
	public String changeName(Model model, @Validated FileForm fileForm, BindingResult br, HttpForm httpForm) {

		if(br.hasFieldErrors(Const.THIS_DIRECTORY_PATH)) {
			model.addAttribute("thisDirectoryPathErr", true);
			fileForm.setThisDirectoryPath(fileServerDirectory);
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return Const.FILEVIEW;
		}

		String originalThisDirectoryPath = fileForm.getThisDirectoryPath();
		//現在の階層のパスがファイルサーバ外のディレクトリが指定されていた場合、もしくは現在階層のパスが存在しない場合はトップディレクトリのパスをセット
		if(!originalThisDirectoryPath.contains(fileServerDirectory) || !new File(originalThisDirectoryPath).exists()
				|| new File(originalThisDirectoryPath).isFile()) {
			model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
			model.addAttribute(Const.PROCESS_ERR_MSG, "不正な情報が送信されました。名称変更処理に失敗しました。");
			fileForm.setThisDirectoryPath(fileServerDirectory);
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return Const.FILEVIEW;
		}

		//リネーム対象のパスが異常値の場合
		if(br.hasFieldErrors("renameTargetPath")) {
			model.addAttribute("renameTargetPathErr", true);
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return Const.FILEVIEW;
		}

		//現在の階層情報が不正な場合は現在階層にトップディレクトリのパスをセット
		if(!originalThisDirectoryPath.equals(new File(fileForm.getRenameTargetPath()).getParent()) ||
				!fileForm.getRenameTargetPath().contains(fileServerDirectory) || !new File(fileForm.getRenameTargetPath()).exists()) {
			model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
			model.addAttribute(Const.PROCESS_ERR_MSG, "不正な情報が送信されました。名称変更処理に失敗しました。");
			fileForm.setThisDirectoryPath(fileServerDirectory);
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return Const.FILEVIEW;
		}

		if(br.hasFieldErrors("afterName") || br.hasFieldErrors("afterPath")) {
			model.addAttribute("isRedisplayRename", true);
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return Const.FILEVIEW;
		}

		//入力されたフォルダ名・ファイル名の前後にスペースがあれば削除
		String afterName = fileForm.getAfterName().strip();

		//拡張子判断→入力されたファイル名で拡張子が消えていたら付与する
		afterName = fileService.checkExtension(fileForm.getRenameTargetPath(), afterName);

		//同名ファイルの存在確認
		int sameNameCheck = fileService.checkSameName(afterName, new File(fileForm.getRenameTargetPath()).getParent());
		if(sameNameCheck == Const.CHK_SAMENAME_EXIST) {
			model.addAttribute("isRedisplayRename", true);
			model.addAttribute("hasRenameErr", true);
			model.addAttribute("errMsg", "その名前は既に使用されています。");
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return Const.FILEVIEW;
		} else if(sameNameCheck == Const.CHK_SAMENAME_TARGET_NOT_EXIST) {
			model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
			model.addAttribute(Const.PROCESS_ERR_MSG, "リネームする対象が存在するディレクトリが存在しません。");
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return Const.FILEVIEW;
		} else if(sameNameCheck == Const.ACCESS_DENIED_ERR) {
			model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
			model.addAttribute(Const.PROCESS_ERR_MSG, "リネーム対象フォルダへのアクセスが拒否されています。");
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return Const.FILEVIEW;
		}

		//リネーム処理
		int result = fileService.rename(fileForm.getRenameTargetPath(),
				new File(fileForm.getRenameTargetPath()).getParent(), afterName, fileForm.getUserName());
		if(result != Const.RENAME_DIR_SUCCESS && result != Const.RENAME_FILE_SUCCESS) {
			model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
			model.addAttribute(Const.PROCESS_ERR_MSG, "名称変更に失敗しました。");
		}
		fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
		return Const.FILEVIEW;
	}



	//ファイル・フォルダ移動
	//departure: 移動したいファイル・フォルダのフルパス
	//renameByMoveCurrentPath: 移動元ファイルのあるディレクトリパス
	//destination: 移動したいディレクトリのパス
	@PostMapping("/moveDirectory")
	public String moveDirectory(Model model, @Validated FileForm fileForm, BindingResult br, RedirectAttributes redirectAttributes, HttpForm httpForm) {

		if(br.hasFieldErrors(Const.THIS_DIRECTORY_PATH)) {
			model.addAttribute("thisDirectoryPathErr", true);
			fileForm.setThisDirectoryPath(fileServerDirectory);
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return Const.FILEVIEW;
		}

		String originalThisDirectoryPath = fileForm.getThisDirectoryPath();
		//現在の階層のパスがファイルサーバ外のディレクトリが指定されていた場合、もしくは現在階層のパスが存在しない場合はトップディレクトリのパスをセット
		if(!originalThisDirectoryPath.contains(fileServerDirectory) || !new File(originalThisDirectoryPath).exists()
				|| new File(originalThisDirectoryPath).isFile()) {
			model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
			model.addAttribute(Const.PROCESS_ERR_MSG, "不正な情報が送信されました。移動処理に失敗しました。");
			fileForm.setThisDirectoryPath(fileServerDirectory);
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return Const.FILEVIEW;
		}

		//移動対象情報がない場合
		if(br.hasFieldErrors("departure")) {
			model.addAttribute("departureErr", true);
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return Const.FILEVIEW;
		}

		//移動先ディレクトリ情報がない場合
		if(br.hasFieldErrors("destination")) {
			model.addAttribute("isRedisplayMove", true);
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return Const.FILEVIEW;
		}

		//移動先のパスが259文字制限オーバーの場合
		if(br.hasFieldErrors("destinationFilePath")) {
			model.addAttribute("isRedisplayMove", true);
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return Const.FILEVIEW;
		}

		//選択対象もしくは移動先がファイルサーバ外だった場合はエラーを返す
		if(!fileForm.getDeparture().contains(fileServerDirectory) || !fileForm.getDestination().contains(fileServerDirectory)) {
			model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
			model.addAttribute(Const.PROCESS_ERR_MSG, "移動対象もしくは移動先情報が不正です。移動処理に失敗しました。");
			fileForm.setThisDirectoryPath(fileServerDirectory);
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return Const.FILEVIEW;
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
			model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
			model.addAttribute(Const.PROCESS_ERR_MSG, "移動対象が存在しません。");
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return Const.FILEVIEW;
		}

		//移動先ディレクトリが実際に存在しているか
		if(!destinationFile.exists() || destinationFile.isFile()) {
			//移動先ディレクトリが存在しない
			model.addAttribute("isRedisplayMove", true);
			model.addAttribute("hasMoveErr", true);
			model.addAttribute("errMsg", "移動先ディレクトリが存在しません。");
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return Const.FILEVIEW;
		}

		//移動の成否確認
		//フォルダの場合
		if(departureFile.isDirectory()) {
			//既にフォルダが存在している階層が同じ場合
			if(departureFile.getParent().equals(fileForm.getDestination())) {
				model.addAttribute("isRedisplayMove", true);
				model.addAttribute("hasMoveErr", true);
				model.addAttribute("errMsg", "フォルダは同一階層から移動されません。");
				fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
				return Const.FILEVIEW;
			}
			//移動元フォルダを、移動元フォルダの下位階層に移動しようとしたとき
			else if(fileForm.getDestination().contains(fileForm.getDeparture())) {
				model.addAttribute("isRedisplayMove", true);
				model.addAttribute("hasMoveErr", true);
				model.addAttribute("errMsg", "サブフォルダには移動できません。");
				fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
				return Const.FILEVIEW;
			}
			//ファイルの場合
		} else {
			//既にファイルが存在している階層が同じ場合
			if(departureFile.getParent().equals(fileForm.getDestination())) {
				model.addAttribute("isRedisplayMove", true);
				model.addAttribute("hasMoveErr", true);
				model.addAttribute("errMsg", "ファイルは同一階層から移動されません。");
				fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
				return Const.FILEVIEW;
			}
		}

		//同名ファイルの存在確認
		int sameNameCheck = fileService.checkSameName(departureFile.getName(), fileForm.getDestination());
		if(sameNameCheck == Const.CHK_SAMENAME_EXIST) {
			model.addAttribute("isRedisplayMove", true);
			model.addAttribute("hasMoveErr", true);
			model.addAttribute("errMsg", "移動先でその名前は既に使用されています。");
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return Const.FILEVIEW;
		} else if(sameNameCheck == Const.ACCESS_DENIED_ERR) {
			model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
			model.addAttribute(Const.PROCESS_ERR_MSG, "移動先ディレクトリにアクセスが拒否されています。");
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return Const.FILEVIEW;
		}

		int result = fileService.rename(fileForm.getDeparture(), fileForm.getDestination(), departureFile.getName(), fileForm.getUserName());
		if(result != Const.MOVE_DIR_SUCCESS && result != Const.MOVE_FILE_SUCCESS) {
			model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
			model.addAttribute(Const.PROCESS_ERR_MSG, "移動に失敗しました。");
		}
		fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
		return Const.FILEVIEW;
	}


	//ファイル・フォルダ削除
	@PostMapping("/deleteFile")
	public String deleteFile(Model model, @Validated FileForm fileForm, BindingResult br, HttpForm httpForm) {
		if(br.hasFieldErrors(Const.THIS_DIRECTORY_PATH)) {
			model.addAttribute("thisDirectoryPathErr", true);
			fileForm.setThisDirectoryPath(fileServerDirectory);
			fileService.dispFileList(fileForm.getHomeDirectory(), fileForm);
			return Const.FILEVIEW;
		}

		String originalThisDirectoryPath = fileForm.getThisDirectoryPath();
		//現在の階層のパスがファイルサーバ外のディレクトリが指定されていた場合、もしくは現在階層のパスが存在しない場合はトップディレクトリのパスをセット
		if(!originalThisDirectoryPath.contains(fileServerDirectory) || !new File(originalThisDirectoryPath).exists()
				|| new File(originalThisDirectoryPath).isFile()) {
			fileForm.setThisDirectoryPath(fileServerDirectory);
		}

		if(br.hasFieldErrors("deleteTargetPath")) {
			model.addAttribute("deleteTargetPathErr", true);
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return Const.FILEVIEW;
		}

		File target = new File(fileForm.getDeleteTargetPath());

		//削除対象のパスがファイルサーバ外のディレクトリが指定されていた場合、もしくは現在の階層のパスが削除対象のあるフォルダと異なる場合
		if(!fileForm.getDeleteTargetPath().contains(fileServerDirectory) || !target.getParent().equals(originalThisDirectoryPath)) {
			model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
			model.addAttribute(Const.PROCESS_ERR_MSG, "不正な情報が送信されました。削除処理に失敗しました。");
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return Const.FILEVIEW;
		}

		//String currentPath = target.getParent();
		//削除対象が存在するか
		if(!target.exists()) {
			model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
			model.addAttribute(Const.PROCESS_ERR_MSG, "削除対象が既に存在しません。");
			fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
			return Const.FILEVIEW;
		}

		//削除処理
		//削除処理に失敗した件数をカウント
		int failedNum = fileService.delete(fileForm.getDeleteTargetPath());
		if(failedNum != 0) {
			model.addAttribute(Const.DISP_PROCESS_ERR_TRUE, true);
			model.addAttribute(Const.PROCESS_ERR_MSG, failedNum + "件の削除処理に失敗しました。");
		}
		fileService.dispFileList(fileForm.getThisDirectoryPath(), fileForm);
		return Const.FILEVIEW;
	}


}
