package com.fileServer.controller;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.fileServer.entity.DbUsersDetails;
import com.fileServer.service.FileService;


@Controller
public class FileController {

	@Autowired
	//serviceクラスのロジック部を参照
	FileService fileService;

	//2階層目以降のディレクトリ表示メソッド
	@PostMapping("/fileView")
	public String toMoveView(@RequestParam("directoryPath") String directoryPath, Model model, @AuthenticationPrincipal DbUsersDetails loginUser) {
		fileService.dispFileList(directoryPath, model);
		model.addAttribute("userAuth", loginUser.getusers().getAuthority());
		return "fileView";
	}


	//ホームディレクトリ表示メソッド
	@GetMapping("/fileView")
	public String toMoveView(Model model, @AuthenticationPrincipal DbUsersDetails loginUser) {
		String directoryPath = new File(".").getAbsoluteFile().getParent() + "\\FileServer";
		fileService.dispFileList(directoryPath, model);
		model.addAttribute("userAuth", loginUser.getusers().getAuthority());
		return "fileView";
	}


	//ファイル・フォルダのアップロード(DBへのINSERT処理)
	//アップロード時に、対応可能な拡張子のみ判断して、対象外のものはエラーではねるのがいいか
	//→アップロード不可能なものが多いのでどうにかならないか。PDFくらいは可能にしたいが
	@PostMapping("/upload")
	public String upload(@RequestParam("uploadFile")String uploadPath, @RequestPart MultipartFile[] multipartFile, Model model,
			@AuthenticationPrincipal DbUsersDetails loginUser) {
		fileService.uploadLogic(uploadPath, multipartFile, loginUser);
		return toMoveView(uploadPath, model, loginUser);
	}


	@RequestMapping("/download")
	public String download(@RequestParam("download") String filePath, HttpServletResponse response) {
		if(fileService.downloadLogic(filePath, response) == 1) {
			System.out.println("ファイルが存在しません");
			return null;
		} else {
			//Responseを直接指定しているため、画面遷移先はnullを指定
			return null;
		}
	}

	//ディレクトリダウンロード
	@RequestMapping("/downloadDir")
	public String downloadDirectory(@RequestParam("downloadDir") String filePath, HttpServletResponse response) throws IOException {
		fileService.downloadDirLogic(filePath, response);
		return null;
	}


	/*
	//名称変更
	//ファイルパスを受け取って、一番下の部分を変更
	@PostMapping("/changeName")
	public String changeName() {

	}

	//移動
	//パスの変更→方法は未定
	@PostMapping("/moveDirectory")
	public String moveDirectory() {

	}

	//削除
	//フォルダの場合は再帰で全て削除。SQLのWHERE句はfile_path
	@PostMapping("/delete")
	public String delete() {

	}
	*/

}
