package com.fileServer.entity;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class FileForm {

	//現在閲覧しているディレクトリ階層
	@NotBlank(message = "対象のディレクトリ情報が取得できませんでした。処理を完了できませんでした。")
	private String thisDirectoryPath;

	//新しいフォルダ名
	//新規フォルダ作成時
	@NotBlank(message = "名前が入力されていません")
	@Pattern(regexp = "[^\\\\/:*?\"<>|]*", message = "「\\ / : * ? \" < > |」は名前に使用できません")
	@Size(max = 255, message = "フォルダ名が長すぎます")
	private String newDirectoryName;

	//現在のパス(thisDirectoryPath)と新しいファイル名(newDirectoryName)を足した文字列
	@Size(max = 259, message = "パスが長すぎるか、階層が深すぎます")
	private String newDirectoryPath;


	//リネームする対象のファイル・フォルダパス
	@NotBlank(message = "名称変更する対象が選択されていません")
	private String renameTargetPath;

	//新しいファイル名・新しいフォルダ名
	//ファイル名・フォルダ名リネーム時
	@NotBlank(message = "名前が入力されていません")
	//@NotBlankを付けているため0文字以上の繰り返し
	@Pattern(regexp = "[^\\\\/:*?\"<>|]*", message = "「\\ / : * ? \" < > |」は名前に使用できません")
	@Size(max = 255, message = "変更後の名前が長すぎます")
	private String afterName;

	//現在のパス(thisDirectoryPath)と新しいファイル名(afterName)を足した文字列
	@Size(max = 259, message = "変更後の名前でのパスが長すぎます")
	private String afterPath;


	//移動処理の際の、移動したいファイル・フォルダのフルパス
	@NotBlank(message = "移動対象が選択されていません")
	private String departure;

	//移動処理の際の、移動先のディレクトリのパス
	@NotBlank(message = "移動先のフォルダが選択されていません")
	private String destination;

	//移動後のパス(destination)とファイル名(departure.getName())を足した文字列
	@Size(max = 259, message = "移動後のパスが長すぎるか、階層が深すぎます")
	private String destinationFilePath;


	//デリート対象のパス
	@NotBlank(message = "削除対象が選択されていません")
	private String deleteTargetPath;


	//アップロード時のファイルでバリデーション
//	@FileRequired
//	@FileSize(max = 5)
//	@RequestSize(max = 8)
//	private MultipartFile[] multipartFile;

	//アップロード時に同名のファイルが存在しているか
	private String isExistSameName;


	//単体ファイルダウンロード時の対象ファイルパス
	@NotBlank(message = "ダウンロード対象のファイルが選択されていません")
	private String downloadFilePath;

	//複数ファイルダウンロード時の対象ファイルパスのリスト
	@NotEmpty(message = "ダウンロード対象のファイルが1件も選択されていません")
	private List<@NotBlank String> downloadFilePathList;

	//フォルダ内一括ダウンロード時の対象フォルダパス
	@NotBlank(message = "ダウンロード対象のフォルダが選択されていません")
	private String downloadDirectoryPath;


	//移動先表示用にフォルダのみの全情報のリスト
	private List<FileData> allDirList;
	//階層表示用にtopから現在階層までの情報を持ったリスト
	private List<FileData> directoryLevels;
	//表示対象のディレクトリのフォルダ一覧
	private List<FileData> forDispDirectoryNameList;
	//表示対象のディレクトリのファイル一覧
	private List<FileData> forDispFileNameList;

	//――――――以下コンストラクタで値を決める変数――――――

	//上部階層表示で"Top"表示用
	private String homeDirectory;

	//ユーザの権限
	private int userAuth;

	//ユーザ名
	private String userName;




}
