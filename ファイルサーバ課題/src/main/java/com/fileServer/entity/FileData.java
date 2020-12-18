package com.fileServer.entity;

import java.io.Serializable;

import lombok.Data;

@Data
public class FileData implements Serializable {

//	シリアルバージョンID
	private static final long serialVersionUID = 1L;

//	ファイルID→SERIAL型のため自動採番
//	DB確認したところ、SERIAL型で作成してもinteger型になってしまっていて、原因不明
	private int fileId;

//	ファイル名
	private String fileName;

//	ファイル
//	private InputStream fileObj;

//	ファイルのフルパス→階層の取得に使用
	private String filePath;

//	登録日時
	private String createDate;

//	登録者
	private String createUser;

//	更新日時
	private String updateDate;

//	更新者
	private String updateUser;


//	ファイルアップロード時に使用
//	うまくいかなかったので保留
//	参考にしたURL: https://qiita.com/MizoguchiKenji/items/0aa1f2b385e73c36c24d
//	private MultipartFile multipartFile;

}
